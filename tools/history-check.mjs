/**
 * HistorySystem verification — undo/redo correctness and snapshot round-trips.
 * Run: node tools/history-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/HistorySystem.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/hist-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const { HistorySystem } = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

// a target whose state is a simple counter + list
const world = {
  value: 0,
  items: [],
  captureState() { return { value: this.value, items: [...this.items] }; },
  restoreState(s) { this.value = s.value; this.items = [...s.items]; }
};

const h = new HistorySystem(10);
h.attach(world);

console.log('\n— undo restores the previous state —');
ok('nothing to undo initially', !h.canUndo());
h.push('set 1'); world.value = 1;
h.push('set 2'); world.value = 2;
ok('undo is available after actions', h.canUndo());
h.undo();
ok('undo returns to value 1', world.value === 1, `got ${world.value}`);
h.undo();
ok('second undo returns to value 0', world.value === 0, `got ${world.value}`);
ok('undo stack is exhausted', !h.canUndo());
ok('undo on empty stack is safe', h.undo() === null);

console.log('\n— redo replays —');
ok('redo is available', h.canRedo());
h.redo();
ok('redo restores value 1', world.value === 1, `got ${world.value}`);
h.redo();
ok('redo restores value 2', world.value === 2, `got ${world.value}`);
ok('redo stack is exhausted', !h.canRedo());

console.log('\n— a new action clears the redo branch —');
h.undo();
ok('redo available after undo', h.canRedo());
h.push('new branch'); world.value = 99;
ok('pushing a new action clears redo', !h.canRedo());

console.log('\n— deep copy: mutating live state must not corrupt history —');
world.items = ['a'];
h.push('with a');
world.items.push('b');
h.undo();
ok('restored list is unaffected by later mutation',
   world.items.length === 1 && world.items[0] === 'a',
   JSON.stringify(world.items));

console.log('\n— named snapshots —');
world.value = 42; world.items = ['x', 'y'];
const snap = h.save('experiment A');
ok('save returns a snapshot with an id', !!snap && !!snap.id);
world.value = 7; world.items = [];
ok('snapshot appears in the list', h.list().some((s) => s.id === snap.id));
h.load(snap.id);
ok('loading a snapshot restores value', world.value === 42, `got ${world.value}`);
ok('loading a snapshot restores the list',
   world.items.length === 2, JSON.stringify(world.items));
ok('loading pushes an undo point', h.canUndo());
h.remove(snap.id);
ok('snapshot can be removed', !h.list().some((s) => s.id === snap.id));
ok('loading a missing snapshot is safe', h.load('nope') === false);

console.log('\n— stack limit is enforced —');
const h2 = new HistorySystem(5);
h2.attach(world);
for (let i = 0; i < 20; i++) { h2.push('a' + i); world.value = i; }
let depth = 0;
while (h2.canUndo()) { h2.undo(); depth++; if (depth > 50) break; }
ok(`undo depth is capped at the limit (${depth})`, depth <= 5, `depth=${depth}`);

console.log('\n— detached history is inert —');
const h3 = new HistorySystem(5);
h3.push('noop');
ok('push without a target is safe', !h3.canUndo());
ok('undo without a target is safe', h3.undo() === null);

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
