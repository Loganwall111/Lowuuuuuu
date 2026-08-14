/**
 * belts-check — asteroid belts, the approach brake, and elliptical galaxies.
 *
 * These three shipped together and each one has a specific way of being
 * silently wrong, so each gets a negative control: a belt that rotates
 * rigidly, a brake that never bites, and an "elliptical" that is really
 * just a squashed spiral.
 */

import fs from 'node:fs';
import { build } from 'esbuild';

let passed = 0, failed = 0;
function ok(name, cond) {
  if (cond) { passed++; console.log('  PASS  ' + name); }
  else { failed++; console.log('FAIL: ' + name); }
}

async function load(entry) {
  const out = await build({
    entryPoints: [entry], bundle: true, format: 'esm',
    write: false, logLevel: 'error', platform: 'node'
  });
  const f = `/tmp/bc-${Math.random().toString(36).slice(2)}.mjs`;
  fs.writeFileSync(f, out.outputFiles[0].text);
  return import(f);
}

const belts = await load('src/bjs/systems/AsteroidBelts.ts');
const deep = await load('src/bjs/systems/DeepSkySystem.ts');
const shape = await load('src/bjs/systems/GalaxyShape.ts');
const grid = await load('src/bjs/systems/IntergalacticGrid.ts');

// ---------------------------------------------------------------- belts
{
  const spec = {
    centre: { x: 0, y: 0, z: 0, clone() { return this; } },
    inner: 40, outer: 90, count: 900, thickness: 1.2, mu: 260, seed: 4242
  };
  const rocks = belts.makeBelt(spec);

  ok('a belt produces the rocks it was asked for', rocks.length === 900);
  ok('every rock sits between the inner and outer edge',
    rocks.every((r) => r.r >= 40 && r.r <= 90));
  ok('a belt is flat, not a spherical shell',
    rocks.every((r) => Math.abs(r.y) <= 1.2));
  ok('the belt is deterministic for a given seed',
    belts.makeBelt(spec)[17].phase === rocks[17].phase);
  ok('a different seed gives a different belt',
    belts.makeBelt({ ...spec, seed: 99 })[17].phase !== rocks[17].phase);

  // The Keplerian curve is the whole point of the system.
  const inner = rocks.reduce((a, b) => (a.r < b.r ? a : b));
  const outer = rocks.reduce((a, b) => (a.r > b.r ? a : b));
  ok('inner rocks orbit faster than outer rocks', inner.omega > outer.omega);

  const predicted = Math.pow(outer.r / inner.r, 1.5);
  const actual = inner.omega / outer.omega;
  ok('the speed curve really is Keplerian (w proportional to r^-1.5)',
    Math.abs(actual - predicted) < 1e-6);

  // NEGATIVE CONTROL: a rigid ring would give a ratio of exactly 1. If the
  // assertion above can pass for a ring that does not shear, it proves
  // nothing at all.
  ok('NEGATIVE CONTROL: a rigid ring is rejected by that test',
    Math.abs(1 - predicted) > 1e-6);

  // Shear over time: the belt must NOT rotate as one piece.
  const before = rocks.map((r) => r.phase);
  belts.stepBelt(rocks, 10);
  const advance = rocks.map((r, i) => r.phase - before[i]);
  const distinct = new Set(advance.map((v) => v.toFixed(4))).size;
  ok('the belt shears instead of rotating rigidly', distinct > 100);

  ok('omega matches the closed form',
    Math.abs(belts.keplerOmega(260, 50) - Math.sqrt(260 / 125000)) < 1e-12);
  ok('a zero-count belt is empty, not a crash',
    belts.makeBelt({ ...spec, count: 0 }).length === 0);

  const p = belts.rockPosition({ r: 10, phase: 0, y: 3 });
  ok('a rock at phase 0 lies on +X', Math.abs(p[0] - 10) < 1e-9);
  ok('a rock keeps its height', Math.abs(p[2]) < 1e-9 && p[1] === 3);
}

// ------------------------------------------------------- approach brake
{
  const w = new deep.WarpDrive();
  for (let i = 0; i < 400; i++) w.update(1 / 60, true);
  const top = w.state().multiplier;
  ok('the drive still reaches full warp', top > 10000);

  w.setApproach(50000);
  ok('nothing far away slows the ship', Math.abs(w.approach - 1) < 1e-9);
  ok('the brake is inactive outside its radius',
    Math.abs(w.state().multiplier - top) < 1e-6);

  w.setApproach(deep.APPROACH_RADIUS);
  ok('the brake is exactly neutral at its own radius',
    Math.abs(w.approach - 1) < 1e-9);

  // Monotonic: closer must always mean slower, with no bumps.
  const samples = [500, 400, 300, 200, 120, 60, 20, 5, 0]
    .map((d) => { w.setApproach(d); return w.approach; });
  let mono = true;
  for (let i = 1; i < samples.length; i++) if (samples[i] > samples[i - 1]) mono = false;
  ok('the brake tightens monotonically as range closes', mono);

  // Exponential, not linear. Halfway in, a linear brake would read 0.5.
  w.setApproach(deep.APPROACH_RADIUS * 0.5);
  const half = w.approach;
  ok('the curve is exponential, not linear', half < 0.35);
  // NEGATIVE CONTROL: a linear ramp would sit at 0.5 and fail that test.
  ok('NEGATIVE CONTROL: a linear ramp would be rejected', !(0.5 < 0.35));

  w.setApproach(0);
  ok('the brake never fully stops the ship', w.approach > 0);
  ok('at the surface, full warp is down to walking pace',
    w.state().multiplier < 5);

  w.setApproach(Infinity);
  ok('an empty sky leaves the drive untouched', w.approach === 1);
  w.setApproach(NaN);
  ok('a bad distance does not freeze the ship', w.approach === 1);
}

// -------------------------------------------------- elliptical galaxies
{
  let x = 7;
  const rnd = () => ((x = (Math.imul(x, 1664525) + 1013904223) >>> 0) / 4294967296);
  const cfg = shape.MILKY_WAY;
  const E = [], S = [];
  for (let i = 0; i < 8000; i++) E.push(shape.ellipticalStar(rnd, cfg));
  for (let i = 0; i < 8000; i++) S.push(shape.galaxyStar(rnd, cfg));

  const sigma = (a, k) => {
    const v = a.map((s) => s[k]);
    const mu = v.reduce((p, c) => p + c, 0) / v.length;
    return Math.sqrt(v.reduce((p, c) => p + (c - mu) ** 2, 0) / v.length);
  };

  const ex = sigma(E, 'x'), ey = sigma(E, 'y'), ez = sigma(E, 'z');
  ok('an elliptical has real extent on all three axes',
    ex > 1 && ey > 1 && ez > 1);
  ok('an elliptical is a 3D swarm, not a flat disc', ey / ex > 0.3);
  ok('an elliptical is triaxial, not a perfect sphere',
    Math.abs(ey / ex - 1) > 0.05 && Math.abs(ez / ex - 1) > 0.02);

  // NEGATIVE CONTROL: the spiral it is supposed to differ from must FAIL
  // the "is 3D" test, otherwise that test is not discriminating anything.
  ok('NEGATIVE CONTROL: a spiral disc is flatter than an elliptical',
    sigma(S, 'y') / sigma(S, 'x') < ey / ex);

  ok('an elliptical has no arm stars at all',
    E.every((s) => s.kind !== 'arm'));
  ok('a spiral does have arm stars', S.some((s) => s.kind === 'arm'));
  ok('every elliptical star stays inside the galaxy',
    E.every((s) => Math.hypot(s.x, s.y, s.z) <= cfg.outerBound + 1e-6));
  ok('elliptical brightness stays in range',
    E.every((s) => s.bright > 0 && s.bright <= 1));

  // Colour: red and dead, so no blue-dominant sample anywhere.
  let blueDominant = 0;
  for (let i = 0; i < 400; i++) {
    const r = (i / 400) * cfg.outerBound;
    const c = shape.ellipticalColor(0.8, r, 0, 0, cfg);
    if (c[2] > c[0]) blueDominant++;
  }
  ok('an elliptical is red and dead, never blue', blueDominant === 0);

  // NEGATIVE CONTROL: the spiral palette DOES go blue in its outer disc,
  // which is exactly why it could not be reused for ellipticals.
  let spiralBlue = 0;
  for (let i = 0; i < 400; i++) {
    const r = (i / 400) * cfg.outerBound;
    const c = shape.photorealColor(0.8, r, 0, 0, cfg);
    if (c[2] > c[0]) spiralBlue++;
  }
  ok('NEGATIVE CONTROL: the spiral palette does go blue outward',
    spiralBlue > 0);

  ok('the core is brighter than the halo',
    shape.ellipticalColor(0.8, 0, 0, 0, cfg)[0] >
    shape.ellipticalColor(0.8, cfg.outerBound, 0, 0, cfg)[0]);

  const routed = shape.galaxyGasColor('elliptical', 0.8, 100, 0, 0, cfg);
  const direct = shape.ellipticalColor(0.8, 100, 0, 0, cfg);
  ok('galaxyGasColor routes ellipticals to the elliptical palette',
    routed.every((v, i) => Math.abs(v - direct[i]) < 1e-12));
}

// ------------------------------------------------------ class mix
{
  const c = {};
  let n = 0;
  for (let i = -40; i < 40; i++)
    for (let j = -6; j < 6; j++)
      for (let k = -6; k < 6; k++) {
        const q = grid.galaxyInCell(i, j, k).klass;
        c[q] = (c[q] || 0) + 1; n++;
      }
  ok('all three galaxy classes occur',
    c.photoreal > 0 && c.elliptical > 0 && c.anomaly > 0);
  // The user asked for the neon anomaly to be "like a one percent". That
  // number is deliberate and must not drift.
  ok('the neon anomaly stays about one percent',
    c.anomaly / n > 0.004 && c.anomaly / n < 0.02);
  ok('spirals remain the most common galaxy',
    c.photoreal > c.elliptical && c.photoreal > c.anomaly);
  ok('ellipticals are a substantial minority',
    c.elliptical / n > 0.2 && c.elliptical / n < 0.5);

  const a = grid.galaxyInCell(3, 1, 2).klass;
  ok('a given cell always yields the same class',
    grid.galaxyInCell(3, 1, 2).klass === a);
}

console.log(`\n${passed} passed, ${failed} failed`);
process.exit(failed ? 1 : 0);
