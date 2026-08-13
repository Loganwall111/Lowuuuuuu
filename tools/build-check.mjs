/**
 * build-check — building on planets and cutting them open.
 *
 * The important property is that construction and destruction both edit the
 * same terrain the water solver reads, so a wall really dams a river and a
 * laser trench really floods. These assertions check the terrain maths and
 * that undo genuinely restores what was there.
 */
import { build } from 'esbuild';
import fs from 'fs';

const load = async (entry, tag) => {
  const out = await build({
    entryPoints: [entry], bundle: true, format: 'esm', write: false, logLevel: 'error'
  });
  const f = `/tmp/${tag}-${Date.now()}.mjs`;
  fs.writeFileSync(f, out.outputFiles[0].text);
  return import(f);
};

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const { ConstructionSystem, STRUCTURES, STRUCTURE_ORDER } =
  await load('src/bjs/systems/ConstructionSystem.ts', 'construct');
const { HydraulicSystem } = await load('src/bjs/systems/HydraulicSystem.ts', 'hydro');

const mkHydro = () => {
  const h = new HydraulicSystem({ size: 64 });
  h.terrain.fill(20);      // flat plain to build on
  return h;
};

console.log('— every structure is buildable —');
{
  ok('there are several things to build', STRUCTURE_ORDER.length >= 6);

  for (const kind of STRUCTURE_ORDER) {
    const h = mkHydro();
    const cs = new ConstructionSystem(h);
    const before = Array.from(h.terrain);
    const st = cs.build(kind, 32, 32);
    ok(`"${STRUCTURES[kind].label}" builds`, !!st);

    const raised = h.terrain.reduce((n, v, i) => n + (v > before[i] + 1e-6 ? 1 : 0), 0);
    ok(`"${STRUCTURES[kind].label}" actually raises terrain (${raised} cells)`, raised > 0);
    ok(`"${STRUCTURES[kind].label}" leaves terrain finite`,
       h.terrain.every((v) => Number.isFinite(v)));

    // Undo must restore exactly.
    cs.undo();
    const exact = h.terrain.every((v, i) => Math.abs(v - before[i]) < 1e-6);
    ok(`"${STRUCTURES[kind].label}" undoes exactly`, exact);
  }
}

console.log('\n— building off the edge of the world —');
{
  const h = mkHydro();
  const cs = new ConstructionSystem(h);
  ok('cannot build off the grid',
     cs.build('tower', -50, -50) === null && cs.build('tower', 9999, 9999) === null);
  ok('nothing was recorded', cs.count === 0);

  // Building at the very edge must clip, not crash or wrap.
  const st = cs.build('dome', 0, 0);
  ok('building at the corner is clipped safely', !!st &&
     h.terrain.every((v) => Number.isFinite(v)));
}

console.log('\n— lasers cut terrain —');
{
  const h = mkHydro();
  const cs = new ConstructionSystem(h);
  const before = Array.from(h.terrain);

  const removed = cs.carveLine(8, 32, 56, 32, 3, 10);
  ok(`a laser removes material (${removed.toFixed(0)})`, removed > 0);

  const lowered = h.terrain.reduce((n, v, i) => n + (v < before[i] - 1e-6 ? 1 : 0), 0);
  ok(`it cuts a trench across the map (${lowered} cells)`, lowered > 20);

  ok('terrain stays finite after cutting',
     h.terrain.every((v) => Number.isFinite(v)));
  ok('nothing is cut below the floor of the world',
     h.terrain.every((v) => v >= 0));

  // The trench must be deepest along its centre line.
  const n = h.size;
  const mid = h.terrain[32 * n + 32];
  const off = h.terrain[20 * n + 32];
  ok('the cut is deepest along the beam', mid < off);

  // A crater
  const h2 = mkHydro();
  const cs2 = new ConstructionSystem(h2);
  const r = cs2.carveAt(32, 32, 6, 12);
  ok('a point blast leaves a crater', r > 0 && h2.terrain[32 * 64 + 32] < 20);
  ok('the crater has sloped walls, not a cliff',
     h2.terrain[32 * 64 + 32] < h2.terrain[32 * 64 + 37]);
}

console.log('\n— construction and water share the same ground —');
{
  // This is the whole point: a wall must actually change where water goes.
  const h = mkHydro();
  // Tilt the plain so water runs downhill.
  const n = h.size;
  for (let y = 0; y < n; y++) {
    for (let x = 0; x < n; x++) h.terrain[y * n + x] = 30 - y * 0.35;
  }
  const cs = new ConstructionSystem(h);

  // Flood the top edge and let it run.
  const pour = () => { for (let x = 0; x < n; x++) h.water[2 * n + x] = 4; };

  // Measure how much water gets PAST the wall line, summed over the whole
  // downhill side. Sampling one row at one instant missed the front
  // entirely and made the test pass/fail for the wrong reason.
  const downhillOf = (grid, fromRow) => {
    let t = 0;
    for (let y = fromRow; y < n; y++) {
      for (let x = 0; x < n; x++) t += grid[y * n + x];
    }
    return t;
  };

  pour();
  for (let i = 0; i < 250; i++) h.step(1 / 60);
  const openFlow = downhillOf(h.water, 30);

  // Same again, but with a wall across the slope.
  const h2 = mkHydro();
  for (let y = 0; y < n; y++) {
    for (let x = 0; x < n; x++) h2.terrain[y * n + x] = 30 - y * 0.35;
  }
  const cs2 = new ConstructionSystem(h2);
  // A continuous barrier across the slope, not a dotted line.
  for (let x = 0; x <= n; x += 2) cs2.build('wall', x, 25, 2.5);
  for (let x = 0; x < n; x++) h2.water[2 * n + x] = 4;
  for (let i = 0; i < 250; i++) h2.step(1 / 60);
  const dammedFlow = downhillOf(h2.water, 30);

  ok(`a wall really dams the flow (${openFlow.toFixed(1)} -> ${dammedFlow.toFixed(1)})`,
     dammedFlow < openFlow, 'the wall changed nothing');

  ok('water stays finite around structures',
     h2.water.every((v) => Number.isFinite(v) && v > -1e-6));
}

console.log('\n— bookkeeping —');
{
  const h = mkHydro();
  const cs = new ConstructionSystem(h);
  const before = Array.from(h.terrain);

  cs.build('tower', 20, 20);
  cs.build('dome', 40, 40);
  cs.build('wall', 30, 10);
  ok('structures are tracked', cs.count === 3);

  const st = cs.stats();
  ok('reports what has been built', st['Structures'] === '3');
  ok('reports material moved', parseFloat(st['Material built']) > 0);

  cs.removeAll();
  ok('clearing removes everything', cs.count === 0);
  ok('and restores the original terrain exactly',
     h.terrain.every((v, i) => Math.abs(v - before[i]) < 1e-6));

  // Overlapping builds must still undo cleanly, newest first.
  const a = cs.build('platform', 30, 30);
  const b = cs.build('tower', 30, 30);
  cs.remove(b.id);
  cs.remove(a.id);
  ok('overlapping structures unwind correctly',
     h.terrain.every((v, i) => Math.abs(v - before[i]) < 1e-6));

  ok('removing something that is gone is harmless', cs.remove(9999) === false);
  ok('undo on an empty site is harmless', cs.undo() === false);
}

console.log('\n— reachable in the game —');
{
  const tw = fs.readFileSync('src/bjs/worlds/TerraformWorld.ts', 'utf8');
  ok('terraform can build', tw.includes('new ConstructionSystem'));
  ok('structures are offered as actions', tw.includes("'build:'"));
  ok('there is a laser trench action', tw.includes("'laser:cut'"));
  ok('there is a bore action', tw.includes("'laser:bore'"));
  ok('building can be undone from the UI', tw.includes("what === 'undo'"));
  ok('structures report into telemetry', tw.includes('this.construct?.stats()'));
  ok('it is disposed with the world', tw.includes('this.construct?.dispose()'));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
