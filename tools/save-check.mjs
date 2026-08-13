/**
 * SaveSystem verification — durability, corruption resistance and recovery.
 * A bad save must never be able to stop the app from starting.
 * Run: node tools/save-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/SaveSystem.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/save-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const { SaveSystem, SAVE_VERSION } = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

class Store {
  constructor() { this.m = new Map(); this.failOn = null; }
  getItem(k) { return this.m.has(k) ? this.m.get(k) : null; }
  setItem(k, v) {
    if (this.failOn && this.failOn(k, v)) { const e = new Error('QuotaExceededError'); e.name = 'QuotaExceededError'; throw e; }
    this.m.set(k, v);
  }
  removeItem(k) { this.m.delete(k); }
}

console.log('\n— save and load round-trip —');
{
  const s = new SaveSystem(new Store());
  const e = s.save('My Universe', 'sandbox', { bodies: [1, 2, 3], gravity: 2.5 });
  ok('save returns an entry with an id', !!e?.id);
  ok('save records the world', e.world === 'sandbox');
  ok('save is version-stamped', e.version === SAVE_VERSION);
  const back = s.load(e.id);
  ok('load returns the entry', !!back);
  ok('payload survives the round-trip',
     JSON.stringify(back.data) === JSON.stringify({ bodies: [1, 2, 3], gravity: 2.5 }));
  ok('the save appears in the index', s.list().some((x) => x.id === e.id));
}

console.log('\n— the index tracks multiple saves newest-first —');
{
  const s = new SaveSystem(new Store());
  const a = s.save('one', 'sandbox', 1);
  const b = s.save('two', 'ocean', 2);
  const c = s.save('three', 'terraform', 3);
  ok('all three are indexed', s.list().length === 3);
  ok('list is newest first', s.list()[0].id === c.id, s.list()[0].name);
  s.remove(b.id);
  ok('removal drops it from the index', s.list().length === 2);
  ok('removed save no longer loads', s.load(b.id) === null);
  ok('other saves are untouched', !!s.load(a.id) && !!s.load(c.id));
}

console.log('\n— corrupt data is survived, not thrown —');
{
  const store = new Store();
  const s = new SaveSystem(store);
  const e = s.save('good', 'sandbox', { x: 1 });
  // truncate the stored JSON, simulating a partial write
  store.setItem('ups.save.v1.' + e.id, '{"id":"' + e.id + '","dat');
  let threw = false;
  let r;
  try { r = s.load(e.id); } catch { threw = true; }
  ok('loading corrupt data does not throw', !threw);
  ok('loading corrupt data returns null', r === null);
  ok('the corruption is reported', !!s.lastError);
  ok('the corrupt entry is purged from the index',
     !s.list().some((x) => x.id === e.id));
}

console.log('\n— a corrupt index cannot break listing —');
{
  const store = new Store();
  const s = new SaveSystem(store);
  store.setItem('ups.save.v1.index', 'not json at all');
  let threw = false;
  let l;
  try { l = s.list(); } catch { threw = true; }
  ok('list() survives a corrupt index', !threw);
  ok('list() returns an empty array', Array.isArray(l) && l.length === 0);
  ok('saving still works afterwards', !!s.save('fresh', 'sandbox', {}));
}

console.log('\n— version mismatches are rejected, not misread —');
{
  const store = new Store();
  const s = new SaveSystem(store);
  const e = s.save('old', 'sandbox', { v: 'ancient' });
  store.setItem('ups.save.v1.' + e.id,
    JSON.stringify({ id: e.id, name: 'old', world: 'sandbox', time: 1, version: 999, data: {} }));
  ok('a future version is refused', s.load(e.id) === null);
  ok('the mismatch is reported', /version/.test(s.lastError ?? ''));
}

console.log('\n— autosave and crash recovery —');
{
  const s = new SaveSystem(new Store());
  ok('no autosave exists initially', s.recover() === null);
  const wrote = s.autosave(() => ({ world: 'sandbox', data: { t: 42 } }));
  ok('autosave writes', wrote === true);
  const rec = s.recover();
  ok('recovery finds the autosave', !!rec);
  ok('recovered payload is intact', rec.data.t === 42);
  ok('recovered world is intact', rec.world === 'sandbox');
  s.clearAutosave();
  ok('autosave can be cleared', s.recover() === null);
}

console.log('\n— autosave is rate-limited —');
{
  const s = new SaveSystem(new Store());
  s.autosaveInterval = 5;
  let writes = 0;
  const cap = () => { writes++; return { world: 'sandbox', data: {} }; };
  for (let i = 0; i < 4; i++) s.tick(1, cap);        // 4s elapsed
  ok('no write before the interval elapses', writes === 0, `${writes}`);
  s.tick(1.5, cap);                                   // crosses 5s
  ok('one write after the interval', writes === 1, `${writes}`);
  for (let i = 0; i < 4; i++) s.tick(1, cap);
  ok('the timer resets after writing', writes === 1, `${writes}`);
  s.tick(2, cap);
  ok('it writes again on the next interval', writes === 2, `${writes}`);
}

console.log('\n— a failing capture must not crash autosave —');
{
  const s = new SaveSystem(new Store());
  let threw = false;
  try {
    s.autosave(() => { throw new Error('world exploded'); });
  } catch { threw = true; }
  ok('a throwing capture is contained', !threw);
  ok('null capture is handled', s.autosave(() => null) === false);
}

console.log('\n— quota exhaustion drops the oldest save and retries —');
{
  const store = new Store();
  const s = new SaveSystem(store);
  const first = s.save('oldest', 'sandbox', { n: 1 });
  s.save('second', 'sandbox', { n: 2 });
  // now reject any further new writes until something is freed
  let allow = false;
  store.failOn = (k) => !allow && k.startsWith('ups.save.v1.sv') && !store.m.has(k);
  store.failOn = (k) => {
    if (store.m.has(k)) return false;
    if (!k.startsWith('ups.save.v1.sv')) return false;
    // fail the first attempt only
    if (!store._triedOnce) { store._triedOnce = true; return true; }
    return false;
  };
  const third = s.save('third', 'sandbox', { n: 3 });
  ok('the save eventually succeeds', !!third);
  ok('the oldest save was evicted', s.load(first.id) === null);
}

console.log('\n— blocked storage falls back to memory —');
{
  const s = new SaveSystem();      // no localStorage in node
  const e = s.save('memory only', 'sandbox', { ok: true });
  ok('saving works without localStorage', !!e);
  ok('loading works without localStorage', s.load(e.id).data.ok === true);
}

console.log('\n— preferences —');
{
  const s = new SaveSystem(new Store());
  const def = { quality: 'high', showHud: true };
  ok('missing prefs return the fallback',
     JSON.stringify(s.getPrefs(def)) === JSON.stringify(def));
  s.setPrefs({ quality: 'cinematic' });
  const p = s.getPrefs(def);
  ok('stored prefs override the fallback', p.quality === 'cinematic');
  ok('unset keys keep their default', p.showHud === true);
}

console.log('\n— export and import —');
{
  const a = new SaveSystem(new Store());
  a.save('alpha', 'sandbox', { v: 1 });
  a.save('beta', 'ocean', { v: 2 });
  const json = a.exportAll();
  ok('export produces parseable JSON', (() => { try { JSON.parse(json); return true; } catch { return false; } })());
  const b = new SaveSystem(new Store());
  const n = b.importAll(json);
  ok('import restores both saves', n === 2, `${n}`);
  ok('imported saves are listed', b.list().length === 2);
  ok('importing garbage returns 0', b.importAll('}{ not json') === 0);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
