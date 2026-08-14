/**
 * HoleDescent verification — falling into a hole must be a journey the
 * player steers, that always ends somewhere, and that fires its arrival
 * exactly once.
 * Run: node tools/descentctl-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/HoleDescent.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/descentctl-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const { HoleDescent, INFALL_GAIN, TIDAL_DRIFT } = await import(f);

const plans = await (async () => {
  const o = await build({
    entryPoints: ['src/bjs/systems/HoleInterior.ts'],
    bundle: true, format: 'esm', write: false, logLevel: 'error'
  });
  const p = `/tmp/hi-${Date.now()}.mjs`;
  fs.writeFileSync(p, o.outputFiles[0].text);
  return import(p);
})();

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const V = (x, y, z) => ({ x, y, z });
/** Seed of an ordinary (non-Gargantua, non-nested) hole. */
const simpleSeed = (() => {
  for (let i = 1; i < 100000; i++) {
    const p = plans.interiorPlan(i);
    if (!p.gargantua && !p.nested) return i;
  }
})();
const nestedSeed = (() => {
  for (let i = 1; i < 100000; i++) {
    const p = plans.interiorPlan(i);
    if (!p.gargantua && p.nested) return i;
  }
})();
const gargSeed = (() => {
  for (let i = 1; i < 1000000; i++) if (plans.interiorPlan(i).gargantua) return i;
})();

console.log('\n— a descent starts only when you cross a horizon —');
{
  const d = new HoleDescent();
  ok('a fresh controller is not falling', !d.active);
  ok('updating while outside is inert', d.update(0.016, V(0, 0, 0)).state.phase === 'outside');
  ok('no destination is produced while outside',
     d.update(0.016, V(0, 0, 0)).arrived === null);

  d.begin('bh-1', simpleSeed, V(0, 0, -100), V(0, 0, 0));
  ok('crossing the horizon begins a fall', d.active);
  ok('the fall knows which interior it is in', !!d.interior);
  ok('it starts at the horizon', d.distance === 0);
}

console.log('\n— begin() is idempotent, so it can be called every frame —');
{
  const d = new HoleDescent();
  d.begin('bh-1', simpleSeed, V(0, 0, -100), V(0, 0, 0));
  d.update(0.1, V(0, 0, -90));
  const was = d.distance;
  d.begin('bh-1', simpleSeed, V(0, 0, -90), V(0, 0, 0));
  ok('re-beginning the same hole does not reset the fall', d.distance === was,
     `${was} -> ${d.distance}`);
  d.begin('bh-2', simpleSeed, V(0, 0, -90), V(0, 0, 0));
  ok('entering a different hole does start a new fall', d.distance === 0);
}

console.log('\n— you fall even if you do nothing —');
{
  const d = new HoleDescent();
  d.begin('bh-1', simpleSeed, V(0, 0, -100), V(0, 0, 0));
  const before = d.distance;
  d.update(0.1, V(0, 0, -100));
  ok('standing still still falls', d.distance > before);
  ok('drift is the declared rate',
     Math.abs(d.distance - before - TIDAL_DRIFT * 0.1) < 1e-6,
     String(d.distance - before));

  // A frame delta of one second means the tab was stalled, not that the
  // player fell for a second. Left unclamped, alt-tabbing would teleport
  // them to the bottom of the hole.
  const e = new HoleDescent();
  e.begin('bh-1', simpleSeed, V(0, 0, -100), V(0, 0, 0));
  e.update(30, V(0, 0, -100));
  ok('a huge frame delta is clamped rather than skipping the whole descent',
     e.distance <= TIDAL_DRIFT * 0.25 + 1e-6, String(e.distance));
}

console.log('\n— flying inward advances the fall; sideways does not —');
{
  const d = new HoleDescent();
  d.begin('bh-1', simpleSeed, V(0, 0, -100), V(0, 0, 0));
  d.update(0, V(0, 0, -90));   // 10 units inward, no time passing
  ok('moving inward advances the descent by the gain',
     Math.abs(d.distance - 10 * INFALL_GAIN) < 1e-6, String(d.distance));

  const e = new HoleDescent();
  e.begin('bh-1', simpleSeed, V(0, 0, -100), V(0, 0, 0));
  e.update(0, V(50, 0, -100));  // pure sideways
  ok('moving sideways gets you nowhere', Math.abs(e.distance) < 1e-6);
}

console.log('\n— burning back out near the entrance escapes —');
{
  const d = new HoleDescent();
  d.begin('bh-1', simpleSeed, V(0, 0, -100), V(0, 0, 0));
  const r = d.update(0, V(0, 0, -140));   // hard burn outward
  ok('a hard burn outward escapes the horizon', r.escaped === true);
  ok('escaping ends the fall', !d.active);
  ok('escaping produces no destination', r.arrived === null);
}

console.log('\n— deep inside, the fall is one way —');
{
  const d = new HoleDescent();
  d.begin('bh-1', simpleSeed, V(0, 0, -100), V(0, 0, 0));
  // fall a long way in
  for (let i = 0; i < 40; i++) d.update(0.5, V(0, 0, -100));
  const deep = d.distance;
  ok('a long fall covers real distance', deep > 1000, String(deep));
  const r = d.update(0, V(0, 0, -140));   // same burn as before
  ok('the same burn no longer escapes', r.escaped === false);
  ok('and the fall continues', d.active);
}

console.log('\n— every fall reaches a destination, exactly once —');
{
  for (const [label, seed] of [['simple', simpleSeed], ['nested', nestedSeed],
                               ['gargantua', gargSeed]]) {
    const d = new HoleDescent();
    d.begin('bh-x', seed, V(0, 0, -100), V(0, 0, 0));
    let arrivals = 0, frames = 0, dest = null;
    while (frames++ < 20000) {
      const r = d.update(0.05, V(0, 0, -100 + frames * 4));
      if (r.arrived) { arrivals++; dest = r.arrived; }
      if (arrivals && frames > 12000) break;
    }
    ok(`a ${label} hole always lands somewhere`, arrivals >= 1, `arrivals=${arrivals}`);
    ok(`a ${label} hole delivers its destination exactly once`, arrivals === 1,
       `arrivals=${arrivals}`);
    if (label === 'gargantua') {
      ok('Gargantua lands in the Library Realm', dest && dest.realm === 'library',
         JSON.stringify(dest));
    }
  }
}

console.log('\n— threading the singularity changes where you come out —');
{
  const p = plans.interiorPlan(nestedSeed);
  // dead on the axis
  const a = new HoleDescent();
  a.begin('bh-n', nestedSeed, V(0, 0, -100), V(0, 0, 0));
  let hit = null;
  for (let i = 0; i < 20000 && !hit; i++) {
    const r = a.update(0.05, V(0, 0, -100 + i * 4));
    if (r.arrived) hit = r.arrived;
  }
  ok('flying straight down the axis passes through the singularity',
     hit && hit.realm === 'duststream', JSON.stringify(hit));

  // far off the axis
  const b = new HoleDescent();
  b.begin('bh-n', nestedSeed, V(0, 0, -100), V(0, 0, 0));
  let miss = null;
  const off = p.singularityCapture * 20;
  for (let i = 0; i < 20000 && !miss; i++) {
    const r = b.update(0.05, V(off, 0, -100 + i * 4));
    if (r.arrived) miss = r.arrived;
  }
  ok('flying wide of it lands in an ordinary dimension instead',
     miss && miss.kind === 'procedural', JSON.stringify(miss));
}

console.log('\n— the shader is told a coherent story —');
{
  const d = new HoleDescent();
  d.begin('bh-1', nestedSeed, V(0, 0, -100), V(0, 0, 0));
  const seen = [];
  for (let i = 0; i < 4000; i++) {
    d.update(0.05, V(0, 0, -100 + i * 4));
    const s = d.shaderState();
    seen.push(s);
    for (const k of ['inside', 'exitWindow', 'nestedLens', 'singularity', 'darkness']) {
      if (!Number.isFinite(s[k]) || s[k] < 0 || s[k] > 1) {
        seen.bad = `${k}=${s[k]}`;
        break;
      }
    }
    if (seen.bad) break;
  }
  ok('every shader value stays inside 0..1', !seen.bad, seen.bad || '');
  ok('the exit window closes as the fall proceeds',
     seen[10].exitWindow > seen[400].exitWindow);
  ok('the interior takes over the view', seen[400].inside > seen[10].inside);
  ok('the nested lens eventually resolves', seen.some((s) => s.nestedLens > 0.2));
  ok('the white dot eventually appears', seen.some((s) => s.singularity > 0.5));
  ok('an ordinary hole never goes fully dark', seen.every((s) => s.darkness === 0));
}

console.log('\n— Gargantua goes dark before it arrives —');
{
  const d = new HoleDescent();
  d.begin('bh-g', gargSeed, V(0, 0, -100), V(0, 0, 0));
  let maxDark = 0, sawLight = false;
  for (let i = 0; i < 20000; i++) {
    const r = d.update(0.05, V(0, 0, -100 + i * 4));
    const s = d.shaderState();
    if (s.darkness < 0.01 && s.inside > 0.1) sawLight = true;
    maxDark = Math.max(maxDark, s.darkness);
    if (r.arrived) break;
  }
  ok('there is a lit stretch before the darkness', sawLight);
  ok('Gargantua fades to full darkness', maxDark > 0.99, String(maxDark));
}

console.log('\n— the outside state is inert, and garbage cannot break a fall —');
{
  const d = new HoleDescent();
  const s = d.shaderState();
  ok('outside, the shader is told nothing is happening',
     s.inside === 0 && s.nestedLens === 0 && s.singularity === 0 && s.darkness === 0);

  let bad = [];
  const e = new HoleDescent();
  e.begin('bh-1', simpleSeed, V(0, 0, -100), V(0, 0, 0));
  for (const dt of [NaN, -1, 0, 1e9, Infinity]) {
    for (const pos of [V(NaN, 0, 0), V(0, Infinity, 0), null, undefined, V(0, 0, 0)]) {
      try {
        const r = e.update(dt, pos);
        if (!Number.isFinite(e.distance)) bad.push(`dt=${dt} distance=${e.distance}`);
        if (!r.state || !r.state.phase) bad.push(`dt=${dt} no phase`);
      } catch (err) {
        bad.push(`dt=${dt} threw ${err.message}`);
      }
    }
  }
  ok('a NaN frame or a null position cannot corrupt the descent',
     bad.length === 0, bad.slice(0, 3).join(' | '));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
