/**
 * surface-check — every planet owns its own working surface.
 *
 * The point of this system is that hydrology, weather and life belong to
 * individual planets rather than to a global "water mode". These assertions
 * are mostly about that independence, plus the physical plausibility of the
 * tornado/whirlpool coupling.
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

const { PlanetSurfaceSystem, profileFor, SURFACE_GRID } =
  await load('src/bjs/systems/PlanetSurfaceSystem.ts', 'surface');

console.log('— each planet has its own character —');
{
  const a = profileFor(1001);
  const b = profileFor(1001);
  const c = profileFor(2002);
  ok('a seed always gives the same planet',
     JSON.stringify({ ...a, species: a.species.map((s) => s.name) }) ===
     JSON.stringify({ ...b, species: b.species.map((s) => s.name) }));
  ok('different seeds give different planets',
     a.climate !== c.climate || Math.abs(a.seaLevel - c.seaLevel) > 1e-6);

  // sweep a lot of seeds: every value must stay physical
  const climates = new Set();
  let bad = 0;
  for (let i = 1; i < 3000; i++) {
    const p = profileFor(i);
    climates.add(p.climate);
    if (!(p.seaLevel >= 0 && p.seaLevel <= 1)) bad++;
    if (!(p.rainfall >= 0) || !Number.isFinite(p.rainfall)) bad++;
    if (!(p.gravity > 0) || !Number.isFinite(p.gravity)) bad++;
    if (!(p.oceanDepth > 0)) bad++;
    if (!p.species.length) bad++;
  }
  ok('every generated planet is physically sane', bad === 0, bad + ' bad values');
  ok(`many climates occur (${[...climates].join(', ')})`, climates.size >= 4);
  ok('exotic planets get their own climate', profileFor(5, true).climate === 'exotic');

  // an arid world must actually be drier than an ocean world
  let aridRain = 0, oceanRain = 0, na = 0, no = 0;
  for (let i = 1; i < 2000; i++) {
    const p = profileFor(i);
    if (p.climate === 'arid') { aridRain += p.rainfall; na++; }
    if (p.climate === 'ocean') { oceanRain += p.rainfall; no++; }
  }
  ok('arid worlds really are drier than ocean worlds',
     na > 0 && no > 0 && (aridRain / na) < (oceanRain / no),
     `arid ${(aridRain / na).toFixed(2)} vs ocean ${(oceanRain / no).toFixed(2)}`);
}

console.log('\n— life matches the world it lives on —');
{
  // An ocean world should not be populated by desert crawlers.
  let oceanJelly = 0, volcCentipede = 0, checked = 0;
  for (let i = 1; i < 1500; i++) {
    const p = profileFor(i);
    if (p.climate === 'ocean') {
      checked++;
      if (p.species.some((s) => s.plan === 'jelly')) oceanJelly++;
    }
    if (p.climate === 'volcanic' && p.species.some((s) => s.plan === 'centipede')) {
      volcCentipede++;
    }
  }
  ok(`ocean worlds grow jellyfish (${oceanJelly}/${checked})`, oceanJelly > 0);
  ok(`volcanic worlds grow centipedes (${volcCentipede} found)`, volcCentipede > 0);

  const arid = [];
  for (let i = 1; i < 1500 && arid.length < 40; i++) {
    const p = profileFor(i);
    if (p.climate === 'arid') arid.push(p);
  }
  ok('arid worlds never grow jellyfish',
     arid.every((p) => !p.species.some((s) => s.plan === 'jelly')));
}

console.log('\n— surfaces are per planet, not global —');
{
  const sys = new PlanetSurfaceSystem();
  const p1 = sys.acquire('planet-a', 111);
  const p2 = sys.acquire('planet-b', 222);

  ok('two planets get two surfaces', p1 !== p2 && sys.count === 2);
  ok('each has its own solver', p1.hydro !== p2.hydro);
  ok('the grid is the expected size', p1.hydro.size === SURFACE_GRID);
  ok('asking twice returns the same surface', sys.acquire('planet-a', 111) === p1);

  // Terrain must not be flat, or there is nothing to drain.
  const t = p1.hydro.terrain;
  let lo = Infinity, hi = -Infinity;
  for (const v of t) { if (v < lo) lo = v; if (v > hi) hi = v; }
  ok(`terrain has real relief (${lo.toFixed(1)}..${hi.toFixed(1)})`, hi - lo > 3);
  ok('terrain is all finite', t.every((v) => Number.isFinite(v)));

  // Simulating one planet must not touch the other.
  const beforeB = p2.hydro.totalWater();
  for (let i = 0; i < 60; i++) sys.step('planet-a', 1 / 60);
  ok('simulating one planet leaves the other alone',
     Math.abs(p2.hydro.totalWater() - beforeB) < 1e-9);
  ok('the simulated planet accumulated time', p1.age > 0.9 && p2.age === 0);

  ok('water stays finite while draining',
     p1.hydro.water.every((v) => Number.isFinite(v) && v >= -1e-6));
}

console.log('\n— tornadoes stir the water —');
{
  const sys = new PlanetSurfaceSystem();
  const s = sys.acquire('storm', 9090);
  // Give it a guaranteed body of water to work with.
  s.hydro.water.fill(2);

  const before = s.hydro.totalWater();
  const t = sys.spawnTornado('storm', 40, 40);
  ok('a tornado can be spawned', !!t && s.tornadoes.length === 1);
  ok('it has a sane size and lifetime', t.radius > 0 && t.life > 0);

  for (let i = 0; i < 120; i++) sys.step('storm', 1 / 60);

  ok('water is still finite after a storm',
     s.hydro.water.every((v) => Number.isFinite(v)));
  ok('no cell went meaningfully negative',
     s.hydro.water.every((v) => v > -1e-3));

  // The solver itself rains and evaporates, so allow drift but not
  // creation/destruction on the tornado's account.
  const after = s.hydro.totalWater();
  const drift = Math.abs(after - before) / before;
  ok(`a whirlpool moves water without inventing it (drift ${(drift * 100).toFixed(2)}%)`,
     drift < 0.35, `${before.toFixed(0)} -> ${after.toFixed(0)}`);

  // Velocity field should show rotation.
  const spun = [...s.hydro.velX].some((v) => Math.abs(v) > 1e-4) ||
               [...s.hydro.velY].some((v) => Math.abs(v) > 1e-4);
  ok('the water visibly rotates', spun);

  // Tornadoes must stay on the map.
  const n = s.hydro.size;
  const escaped = s.tornadoes.filter((x) => x.x < 0 || x.y < 0 || x.x > n || x.y > n);
  ok('storms stay on the planet', escaped.length === 0);

  // And they must expire.
  for (let i = 0; i < 60 * 120; i++) sys.step('storm', 1 / 60);
  ok('storms eventually blow themselves out', s.tornadoes.length < 7);
}

console.log('\n— memory stays bounded —');
{
  const sys = new PlanetSurfaceSystem();
  sys.maxResident = 4;
  for (let i = 0; i < 40; i++) sys.acquire('p' + i, i * 7 + 1);
  ok(`only a few surfaces stay resident (${sys.count})`, sys.count <= 4);

  // Revisiting rebuilds identically from the seed - eviction is safe.
  const first = sys.acquire('stable', 424242);
  const sig1 = [...first.hydro.terrain.slice(0, 32)].join(',');
  const climate1 = first.profile.climate;
  for (let i = 0; i < 40; i++) sys.acquire('junk' + i, i + 5000);
  sys.release('stable');
  const again = sys.acquire('stable', 424242);
  ok('a revisited planet is the same planet',
     [...again.hydro.terrain.slice(0, 32)].join(',') === sig1 &&
     again.profile.climate === climate1);

  const st = sys.stats();
  ok('reports resident surfaces', 'Surfaces resident' in st);
  ok('reports active storms', 'Active storms' in st);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
