/**
 * dynamics-check — the remaining stages: sculpting, world dynamics, diving,
 * seasons, collisions, comet steering, rewind and derelict logs.
 *
 * Each module is pure and tested without a GPU:
 *   - the sculpt brush maps tools onto the hydrology primitives,
 *   - subsurface oceans / tidal locking / weather are deterministic,
 *   - a gas dive descends and crosses layers exactly once,
 *   - seasons and precession cycle slowly,
 *   - planet collisions conserve mass and volume,
 *   - the gravity tractor pulls and deflects,
 *   - rewind scrubs the player's own path,
 *   - derelict logs are stable per seed.
 *
 * Run: node tools/dynamics-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const load = async (entry) => {
  const out = await build({
    entryPoints: ['src/bjs/systems/' + entry],
    bundle: true, format: 'esm', write: false, logLevel: 'error'
  });
  const f = `/tmp/${entry.replace(/\W/g, '_')}-${Date.now()}.mjs`;
  fs.writeFileSync(f, out.outputFiles[0].text);
  return import(f);
};

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const S = await load('SculptSystem.ts');
console.log('\n— the sculpt brush —');
{
  const hydro = { terrain: [], water: [], deform() {}, crater() {}, addWater() {} };
  const calls = [];
  const h = {
    deform: (x, y, r, a, s) => calls.push(['deform', r, a]),
    crater: (x, y, r, d) => calls.push(['crater', r, d]),
    addWater: (x, y, r, a) => calls.push(['water', r, a])
  };
  S.applySculpt(h, 'raise', 10, 10, 5, 2);
  ok('raise calls deform with positive amount', calls[0][2] > 0);
  S.applySculpt(h, 'lower', 10, 10, 5, 2);
  ok('lower calls deform with negative amount', calls[calls.length - 1][2] < 0);
  S.applySculpt(h, 'melt', 10, 10, 5, 2);
  ok('melt raises water (the ice becomes sea)',
    calls.some((c) => c[0] === 'water' && c[2] > 0));
  S.applySculpt(h, 'crater', 10, 10, 5, 2);
  ok('crater calls the crater primitive', calls.some((c) => c[0] === 'crater'));

  const g = S.surfaceToGrid(1, 0, 0, 96);
  ok('the surface direction maps into the grid',
    g.x >= 0 && g.x < 96 && g.y >= 0 && g.y < 96);
  ok('the equator maps to the grid centre',
    Math.abs(S.surfaceToGrid(0, 0, 1, 96).y - 47.5) < 1);
}

const W = await load('WorldDynamics.ts');
console.log('\n— subsurface oceans, tidal locking, weather —');
{
  ok('a frozen world can hide an ocean', (() => {
    let found = false;
    for (let i = 1; i < 400; i++) {
      const s = W.subsurfaceOcean(i, 'frozen');
      if (s.present && s.depth > 0) { found = true; break; }
    }
    return found;
  })());
  ok('a non-frozen world has no subsurface ocean',
    W.subsurfaceOcean(5, 'arid').present === false &&
    W.subsurfaceOcean(5, 'ocean').present === false);
  ok('subsurface oceans are deterministic',
    JSON.stringify(W.subsurfaceOcean(77, 'frozen')) === JSON.stringify(W.subsurfaceOcean(77, 'frozen')));

  ok('close-in worlds lock', W.tidalLocked(3, 10) === true);
  ok('distant worlds do not lock', W.tidalLocked(3, 500) === false);
  ok('the day side is bright and the night side is dark', (() => {
    const sub = W.subStellarPoint(42);
    const day = W.daylightAt(sub[0], sub[1], sub);
    const far = W.daylightAt((sub[0] + 0.5) % 1, sub[1], sub);
    return day > 0.9 && far < 0.2;
  })());

  ok('weather is deterministic', (() => {
    const a = W.weatherFor(9, 'arid', 30);
    const b = W.weatherFor(9, 'arid', 30);
    return JSON.stringify(a) === JSON.stringify(b);
  })());
  ok('arid worlds can brew dust storms', (() => {
    for (let t = 0; t < 400; t += 0.25) {
      if (W.weatherFor(11, 'arid', t).kind === 'dust') return true;
    }
    return false;
  })());
  ok('visibility never goes out of range', (() => {
    for (let i = 0; i < 200; i++) {
      const w = W.weatherFor(i, 'frozen', i * 3);
      if (w.visibility < 0 || w.visibility > 1 || !Number.isFinite(w.wind)) return false;
    }
    return true;
  })());
}

const G = await load('GasDive.ts');
console.log('\n— gas-giant sky-diving —');
{
  const d = new G.GasDive(24, 10000);
  ok('starts high in the upper haze', d.state().layer === 'upper haze');
  for (let i = 0; i < 4000; i++) d.step(0.05);
  const s = d.state();
  ok('it descends', s.altitude < 10000);
  ok('it picks up speed under gravity', s.speed > 0);
  const events = d.drainEvents();
  ok('it crosses the cloud decks', events.length > 0, events.join(','));
  ok('each deck is crossed exactly once',
    new Set(events).size === events.length);
  ok('it ends in the deepest layer', s.layer === 'metallic hydrogen');
}

const SE = await load('GalacticSeasons.ts');
console.log('\n— galactic seasons —');
{
  ok('seasons cycle through four names',
    [0, 1, 2, 3].every((i) => SE.seasonIndex(i * SE.SEASON_PERIOD) === i));
  ok('the season repeats after a full cycle',
    SE.seasonIndex(SE.SEASON_PERIOD * 4) === 0);
  ok('precession is slow and continuous', (() => {
    const a = SE.precessionAngle(10);
    const b = SE.precessionAngle(11);
    return Math.abs(b - a) < 0.01 && SE.precessionAngle(0) === 0;
  })());
}

const P = await load('PlanetCollision.ts');
console.log('\n— planet collisions —');
{
  const w = [
    { id: 'a', name: 'Alpha', x: 0, y: 0, z: 0, radius: 10, mass: 100 },
    { id: 'b', name: 'Beta', x: 40, y: 0, z: 0, radius: 10, mass: 100 },
    { id: 'c', name: 'Gamma', x: 12, y: 0, z: 0, radius: 8, mass: 50 }
  ];
  const o = P.findDeepestOverlap(w);
  ok('the deepest overlap is found', !!o && o.a.id === 'a' && o.b.id === 'c');
  ok('clear worlds produce no overlap',
    P.findDeepestOverlap([w[0], w[1]]) === null);
  const m = P.mergeResult(w[0], w[2]);
  ok('mass is conserved', m.mass === 150);
  ok('volume is conserved', Math.abs(m.radius ** 3 - (10 ** 3 + 8 ** 3)) < 1e-6);
  ok('the larger keeps its name', m.name === 'Alpha');
}

const T = await load('GravityTractor.ts');
console.log('\n— the gravity tractor —');
{
  const ship = { mass: 50, x: 0, y: 0, z: 0 };
  const comet = { mass: 1, x: 20, y: 0, z: 0 };
  const a = T.tractorAccel(ship, comet);
  // The comet sits at +20; the pull is toward the ship at 0, so it points -x.
  ok('the pull is toward the ship', a.ax < 0 && Math.abs(a.ay) < 1e-9);
  ok('a heavier ship pulls harder',
    T.tractorAccel({ mass: 5, x: 0, y: 0, z: 0 }, comet).ax > a.ax);
  ok('the pull at the core is finite',
    T.tractorAccel(ship, ship).ax === 0);
  ok('strength is 1 beside the comet and 0 far away',
    T.tractorStrength(ship, ship, 200) > 0.99 &&
    T.tractorStrength(ship, { mass: 1, x: 1000, y: 0, z: 0 }, 200) === 0);
  ok('deflection accumulates with hold time',
    T.deflectFrom(1, 60) > T.deflectFrom(1, 1));
}

const R = await load('TimeRewind.ts');
console.log('\n— time rewind —');
{
  const r = new R.TimeRewind(8, 60);
  for (let i = 0; i < 120; i++) r.record(i, 0, 0);
  ok('history accumulates', r.reachable() > 1.5);
  const out = r.rewind(1);
  ok('rewind returns an earlier position', out && out.state.x < 120);
  ok('rewind reports the distance travelled back', out && out.rewound > 0);
  ok('rewind zeroes velocity', out && out.state.vx === 0);
  ok('rewinding an empty buffer returns null',
    new R.TimeRewind().rewind(1) === null);
}

const D = await load('DerelictLog.ts');
console.log('\n— derelict found logs —');
{
  const a = D.derelictLog(1234);
  const b = D.derelictLog(1234);
  const c = D.derelictLog(9999);
  ok('a log is a complete story',
    a.title && a.crew && a.fate && a.body.length > 20);
  ok('the same wreck tells the same story',
    JSON.stringify(a) === JSON.stringify(b));
  ok('different wrecks tell different stories',
    a.body !== c.body || a.title !== c.title);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
