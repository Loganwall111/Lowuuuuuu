/**
 * HydraulicSystem verification — proves the shallow-water solver is a real
 * fluid simulation: water conserves, runs downhill, pools in basins, and
 * erosion moves material rather than inventing it.
 * Run: node tools/water-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/HydraulicSystem.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/hydro-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const { HydraulicSystem } = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const mk = (o = {}) => new HydraulicSystem({
  size: 48, evaporation: 0, erosion: 0, deposition: 0, rain: 0, ...o
});

console.log('\n— mass conservation (no rain, no evaporation) —');
{
  const h = mk();
  h.generateTerrain((nx, ny) => Math.sin(nx * 6) * 0.4 + Math.cos(ny * 5) * 0.3);
  h.addWater(24, 24, 10, 1.0);
  const before = h.totalWater();
  for (let i = 0; i < 200; i++) h.step(0.02);
  const after = h.totalWater();
  const drift = Math.abs(after - before) / before;
  ok(`water is conserved over 200 steps (drift ${(drift * 100).toFixed(3)}%)`,
     drift < 0.02, `before=${before.toFixed(3)} after=${after.toFixed(3)}`);
  ok('no water was created', after <= before * 1.02);
}

console.log('\n— water never goes negative —');
{
  const h = mk();
  h.generateTerrain((nx) => nx * 4);          // steep ramp
  h.addWater(40, 24, 6, 2.0);
  for (let i = 0; i < 300; i++) h.step(0.03);
  let neg = 0;
  for (let i = 0; i < h.water.length; i++) if (h.water[i] < 0) neg++;
  ok('no cell has negative depth', neg === 0, `${neg} negative cells`);
}

console.log('\n— water flows downhill —');
{
  const h = mk();
  // ramp descending toward +x
  h.generateTerrain((nx) => (1 - nx) * 5);
  h.addWater(8, 24, 4, 1.5);
  const startCentroid = () => {
    let sx = 0, sw = 0;
    for (let y = 0; y < h.size; y++)
      for (let x = 0; x < h.size; x++) {
        const w = h.water[h.idx(x, y)];
        sx += x * w; sw += w;
      }
    return sx / Math.max(sw, 1e-9);
  };
  const c0 = startCentroid();
  for (let i = 0; i < 250; i++) h.step(0.02);
  const c1 = startCentroid();
  ok(`water centroid moved downhill (+x): ${c0.toFixed(1)} -> ${c1.toFixed(1)}`,
     c1 > c0 + 1, `c0=${c0} c1=${c1}`);
}

console.log('\n— water pools in a basin instead of escaping —');
{
  const h = mk();
  // bowl: high at the edges, low in the middle
  h.generateTerrain((nx, ny) => {
    const dx = nx - 0.5, dy = ny - 0.5;
    return Math.sqrt(dx * dx + dy * dy) * 8;
  });
  h.addWater(24, 24, 8, 0.6);
  const before = h.totalWater();
  for (let i = 0; i < 300; i++) h.step(0.02);
  ok('basin retains its water', h.totalWater() > before * 0.97);
  // deepest point should be near the centre
  let best = -1, bx = 0, by = 0;
  for (let y = 0; y < h.size; y++)
    for (let x = 0; x < h.size; x++) {
      const w = h.water[h.idx(x, y)];
      if (w > best) { best = w; bx = x; by = y; }
    }
  ok(`deepest water settled near the basin centre (${bx},${by})`,
     Math.hypot(bx - 24, by - 24) < 8, `at ${bx},${by}`);
}

console.log('\n— a flat surface stays flat (no spurious currents) —');
{
  const h = mk();
  h.generateTerrain(() => 0);
  h.setSeaLevel(1.0);
  const d0 = h.maxDepth();
  for (let i = 0; i < 120; i++) h.step(0.02);
  const spread = h.maxDepth() - d0;
  ok(`still water stays level (max deviation ${spread.toExponential(2)})`,
     Math.abs(spread) < 1e-3, `${spread}`);
}

console.log('\n— sea level floods low ground only —');
{
  const h = mk();
  h.generateTerrain((nx) => nx * 2);       // 0 .. 2
  h.setSeaLevel(1.0);
  ok('low ground is submerged', h.water[h.idx(2, 24)] > 0.5);
  ok('high ground stays dry', h.water[h.idx(46, 24)] === 0);
  const cov = h.coverage();
  ok(`coverage is roughly half the map (${(cov * 100).toFixed(0)}%)`,
     cov > 0.35 && cov < 0.65, `${cov}`);
}

console.log('\n— tsunami propagates inward from an edge —');
{
  const h = mk();
  h.generateTerrain(() => 0);
  h.setSeaLevel(0.5);
  const base = h.totalWater();
  h.tsunami(3.0, 5, 'w');
  ok('tsunami adds water at the western edge', h.totalWater() > base);
  const edgeBefore = h.water[h.idx(2, 24)];
  const midBefore = h.water[h.idx(30, 24)];
  // shallow-water wave speed is sqrt(g*depth) cells/s, so give the wave
  // enough simulated time to actually cross the map
  const speed = Math.sqrt(9.81 * 0.5);
  const need = 30 / speed;                 // seconds to reach cell 30
  const dt = 0.01, steps = Math.ceil((need * 1.6) / dt);
  for (let i = 0; i < steps; i++) h.step(dt);
  ok('the wave crest leaves the edge', h.water[h.idx(2, 24)] < edgeBefore);
  ok(`water arrives inland after ${(steps * dt).toFixed(1)}s of travel`,
     h.water[h.idx(30, 24)] > midBefore,
     `mid=${h.water[h.idx(30, 24)].toFixed(4)} was ${midBefore.toFixed(4)}`);
}

console.log('\n— erosion moves material, it does not invent it —');
{
  const h = new HydraulicSystem({
    size: 48, evaporation: 0, rain: 0, erosion: 0.5, deposition: 0.4
  });
  h.generateTerrain((nx) => (1 - nx) * 6);
  h.addWater(6, 24, 5, 2.0);
  const t0 = h.totalTerrain();
  let sed0 = 0;
  for (let i = 0; i < h.sediment.length; i++) sed0 += h.sediment[i];
  for (let i = 0; i < 250; i++) h.step(0.02);
  let sed1 = 0;
  for (let i = 0; i < h.sediment.length; i++) sed1 += h.sediment[i];
  const t1 = h.totalTerrain();
  const total0 = t0 + sed0, total1 = t1 + sed1;
  const drift = Math.abs(total1 - total0) / Math.abs(total0);
  ok(`terrain + suspended sediment is conserved (drift ${(drift * 100).toFixed(3)}%)`,
     drift < 0.02, `${total0.toFixed(2)} -> ${total1.toFixed(2)}`);
  ok('erosion actually changed the terrain', Math.abs(t1 - t0) > 1e-6);
}

console.log('\n— evaporation removes water —');
{
  const h = new HydraulicSystem({ size: 32, evaporation: 0.01, erosion: 0, deposition: 0 });
  h.generateTerrain(() => 0);
  h.setSeaLevel(1.0);
  const b = h.totalWater();
  for (let i = 0; i < 100; i++) h.step(0.02);
  ok('water level drops as it evaporates', h.totalWater() < b);
  ok('evaporation never goes below zero', h.totalWater() >= 0);
}

console.log('\n— rain adds water —');
{
  const h = new HydraulicSystem({ size: 32, evaporation: 0, rain: 0.05, erosion: 0, deposition: 0 });
  h.generateTerrain(() => 0);
  const b = h.totalWater();
  for (let i = 0; i < 50; i++) h.step(0.02);
  ok('rain accumulates', h.totalWater() > b);
}

console.log('\n— craters and deformation —');
{
  const h = mk();
  h.generateTerrain(() => 1.0);
  const centre0 = h.terrain[h.idx(24, 24)];
  h.crater(24, 24, 8, 2.0);
  ok('crater digs a bowl', h.terrain[h.idx(24, 24)] < centre0);
  ok('crater raises an ejecta rim', h.terrain[h.idx(24 + 10, 24)] > 1.0);

  const before = h.terrain[h.idx(10, 10)];
  h.deform(10, 10, 5, 1.5);
  ok('deform raises terrain', h.terrain[h.idx(10, 10)] > before);
  h.deform(10, 10, 5, -1.5);
  ok('deform lowers terrain back',
     Math.abs(h.terrain[h.idx(10, 10)] - before) < 1e-5);
}

console.log('\n— stability under an extreme step —');
{
  const h = mk();
  h.generateTerrain((nx, ny) => Math.sin(nx * 20) * Math.cos(ny * 20) * 3);
  h.addWater(24, 24, 12, 5);
  for (let i = 0; i < 100; i++) h.step(0.05);
  let bad = 0;
  for (let i = 0; i < h.water.length; i++)
    if (!Number.isFinite(h.water[i]) || !Number.isFinite(h.terrain[i])) bad++;
  ok('no NaN or Infinity in the field', bad === 0, `${bad} bad cells`);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
