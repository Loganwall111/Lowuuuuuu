/**
 * cosmos-check — the new sky, warp, fleet and throwable systems.
 *
 * These assertions check physics against known values wherever a known
 * value exists: Earth's gravitational binding energy, escape velocity,
 * c^2 as the antimatter yield, the inverse-square law. Where the number is
 * a design decision rather than a fact, the test pins the behaviour that
 * decision was made to produce (a beam that cuts kilometres per second, a
 * warp that crosses the map in seconds) so that tuning it later has to be
 * deliberate.
 */

import { execFileSync } from 'node:child_process';
import { mkdtempSync, writeFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

let pass = 0, fail = 0;
const ok = (name, cond, detail = '') => {
  if (cond) { pass++; console.log('  PASS  ' + name); }
  else { fail++; console.log('  FAIL  ' + name + (detail ? ' — ' + detail : '')); }
};
const near = (a, b, tol) => Math.abs(a - b) <= tol;

// Bundle the systems so they can be exercised for real rather than grepped.
// The entry must live inside the repo or esbuild cannot resolve @babylonjs
// from node_modules; a temp directory outside the tree fails to bundle.
const dir = mkdtempSync(join('/home/user/Low/tools', 'cosmos-'));
const entry = join(dir, 'entry.js');
const out = join(dir, 'bundle.mjs');
writeFileSync(entry, `
export * from '/home/user/Low/src/bjs/systems/DeepSkySystem.ts';
export * from '/home/user/Low/src/bjs/systems/ThrowableSystem.ts';
export * from '/home/user/Low/src/bjs/systems/FleetSystem.ts';
export { Vector3 } from '@babylonjs/core/Maths/math.vector';
`);
execFileSync('/home/user/Low/node_modules/.bin/esbuild',
  [entry, '--bundle', '--format=esm', '--platform=browser', '--outfile=' + out],
  { stdio: 'pipe' });
const M = await import(out);

console.log('\n— throwable impact physics —');
{
  const EARTH_M = 5.97e24, EARTH_R = 6.371e6;
  const be = M.bindingEnergy(EARTH_M, EARTH_R);
  // Textbook value for Earth is 2.24e32 J.
  ok('Earth binding energy matches the textbook 2.24e32 J',
    near(be / 1e32, 2.24, 0.05), be.toExponential(3));

  // Binding energy is 3GM^2/5R, so doubling mass quadruples it.
  const dbl = M.bindingEnergy(EARTH_M * 2, EARTH_R);
  ok('binding energy scales as mass squared', near(dbl / be, 4, 1e-6));
  // ...and halving the radius doubles it.
  const half = M.bindingEnergy(EARTH_M, EARTH_R / 2);
  ok('binding energy scales inversely with radius', near(half / be, 2, 1e-6));

  ok('antimatter yield per kg is c squared',
    near(M.YIELD_PER_KG.antimatter / 8.98e16, 1, 0.01));

  // A pebble cannot hurt a planet; a neutron star fragment must destroy it.
  const pebble = M.computeImpact(M.throwableById('pebble'), 20000, EARTH_M, EARTH_R);
  ok('a pebble bounces off a planet', pebble.outcome === 'bounce', pebble.outcome);
  const ns = M.computeImpact(M.throwableById('neutron'), 20000, EARTH_M, EARTH_R);
  ok('a neutron star fragment shatters a planet', ns.outcome === 'shattered', ns.outcome);
  ok('shattering means exceeding binding energy', ns.bindingFraction >= 1);

  // The regression this suite exists for: a moon delivers 6.5% of Earth's
  // binding energy, which must outrank a mere "extinction" event.
  const moon = M.computeImpact(M.throwableById('moon'), 20000, EARTH_M, EARTH_R);
  ok('a moon impact is ranked by binding energy, not megatons',
    moon.outcome === 'crust-loss', moon.outcome);
  ok('the moon impact is below the shatter threshold', moon.bindingFraction < 1);

  // Outcome must never soften as energy rises.
  const RANK = ['bounce', 'crater', 'regional', 'extinction', 'crust-loss', 'shattered'];
  let monotone = true, prev = -1;
  for (const speed of [10, 100, 1e3, 1e4, 1e5, 1e6, 1e7]) {
    const r = M.computeImpact(M.throwableById('asteroid'), speed, EARTH_M, EARTH_R);
    const idx = RANK.indexOf(r.outcome);
    if (idx < prev) monotone = false;
    prev = idx;
  }
  ok('outcome severity never decreases as impact speed rises', monotone);

  // Kinetic energy must rise as the square of speed.
  const a = M.computeImpact(M.throwableById('bus'), 1000, EARTH_M, EARTH_R);
  const b = M.computeImpact(M.throwableById('bus'), 2000, EARTH_M, EARTH_R);
  ok('kinetic energy quadruples when speed doubles',
    near(b.kinetic / a.kinetic, 4, 1e-6));

  ok('every throwable has positive mass and radius',
    M.THROWABLES.every((t) => t.mass > 0 && t.radius > 0));
  ok('throwable ids are unique',
    new Set(M.THROWABLES.map((t) => t.id)).size === M.THROWABLES.length);
  ok('the catalogue spans at least fifteen orders of magnitude in mass',
    Math.log10(Math.max(...M.THROWABLES.map((t) => t.mass)) /
               Math.min(...M.THROWABLES.map((t) => t.mass))) > 15);
  ok('the wacky behaviours survive into the catalogue',
    ['orbit', 'burrow', 'grow', 'split', 'devour'].every(
      (bh) => M.THROWABLES.some((t) => t.behaviour === bh)));
}

console.log('\n— mining beam —');
{
  // Design target: a canyon in seconds, a planet in hours.
  const perSec = M.boreDepth(M.DEFAULT_MINING, 1);
  ok('the beam cuts roughly a kilometre per second',
    perSec > 300 && perSec < 5000, perSec.toFixed(0) + ' m/s');
  ok('bore depth is linear in time',
    near(M.boreDepth(M.DEFAULT_MINING, 10) / perSec, 10, 1e-6));
  const hours = M.timeToPierce(M.DEFAULT_MINING, 6.371e6) / 3600;
  ok('piercing an Earth-sized planet takes hours, not seconds or weeks',
    hours > 0.5 && hours < 24, hours.toFixed(1) + ' h');
  ok('a bigger world takes longer to pierce',
    M.timeToPierce(M.DEFAULT_MINING, 1e7) > M.timeToPierce(M.DEFAULT_MINING, 1e6));
}

console.log('\n— the sky —');
{
  // Brightness must fall off with distance, and a galaxy must outshine a rock.
  const galaxyLum = M.LUMINOSITY.galaxy, planetLum = M.LUMINOSITY.planet;
  ok('a galaxy is far more luminous than a planet', galaxyLum / planetLum > 1e4);
  const b1 = M.apparentBrightness(galaxyLum, 1000);
  const b2 = M.apparentBrightness(galaxyLum, 10000);
  ok('apparent brightness falls with distance', b2 < b1);
  ok('brightness is clamped to one', M.apparentBrightness(1e30, 1) <= 1);

  // Angular size: an object at its own radius subtends a right angle...
  ok('angular size grows as you approach',
    M.angularSize(100, 200) > M.angularSize(100, 2000));
  ok('angular size is bounded by pi', M.angularSize(100, 100) <= Math.PI + 1e-9);

  // The sky is sorted by brightness, not distance: a distant galaxy should
  // outrank a nearby pebble of a planet, which is what makes the sky read
  // correctly rather than looking like a proximity list.
  const V = (x, y, z) => new M.Vector3(x, y, z);
  // Chosen so the two orderings genuinely disagree. Note that a *very*
  // close planet legitimately outshines a distant galaxy - Venus outshines
  // Andromeda from Earth - so the distances here are picked such that the
  // galaxy is both farther away and brighter. Sorting by distance would put
  // the planet first; sorting by brightness must not.
  const objs = [
    { id: 'near-planet', kind: 'planet', position: V(0, 0, 400),
      radius: 10, luminosity: M.LUMINOSITY.planet, color: { r: 1, g: 1, b: 1 } },
    { id: 'far-galaxy', kind: 'galaxy', position: V(0, 0, 5000),
      radius: 2600, luminosity: M.LUMINOSITY.galaxy, color: { r: 1, g: 1, b: 1 } }
  ];
  const sky = M.visibleSky(objs, V(0, 0, 0), 10);
  ok('the sky is ordered by brightness rather than proximity',
    sky.length > 0 && sky[0].id === 'far-galaxy',
    sky.map((s) => s.id).join(','));
  ok('the visible sky respects its budget',
    M.visibleSky(objs, V(0, 0, 0), 1).length <= 1);
}

console.log('\n— flying into a galaxy —');
{
  const V = (x, y, z) => new M.Vector3(x, y, z);
  const centre = V(0, 0, 0), R = 2600;
  const outside = M.galacticMedium(V(0, 0, R * 2), centre, R);
  ok('there is no fog outside a galaxy', !outside.inside && outside.fogDensity === 0);

  const edge = M.galacticMedium(V(0, 0, R * 0.99), centre, R);
  const mid = M.galacticMedium(V(0, 0, R * 0.5), centre, R);
  const core = M.galacticMedium(V(0, 0, 0), centre, R);
  ok('entering a galaxy puts you inside it', edge.inside && core.inside);
  ok('fog thickens toward the core',
    core.fogDensity > mid.fogDensity && mid.fogDensity > edge.fogDensity);
  ok('fog is continuous at the boundary', edge.fogDensity < 1e-4,
    String(edge.fogDensity));
  ok('stars crowd toward the core', core.starDensity > edge.starDensity);
  ok('fog colour is a valid rgb triple',
    core.fogColor.length === 3 &&
    core.fogColor.every((c) => c >= 0 && c <= 1));
}

console.log('\n— warp drive —');
{
  const d = new M.WarpDrive();
  ok('the drive starts cold', d.stats !== undefined && !d.update(0.016, false).engaged);

  // Spooling: a brief tap must not warp you across the map.
  const tap = new M.WarpDrive();
  let s = tap.update(0.5, true);
  ok('a brief tap does not engage the drive', !s.engaged, JSON.stringify(s));

  // Holding thrust must build, and keep building.
  const hold = new M.WarpDrive();
  let last = 0, rising = true;
  for (let i = 0; i < 600; i++) {
    const st = hold.update(0.016, true);
    if (st.multiplier < last - 1e-9) rising = false;
    last = st.multiplier;
  }
  ok('holding thrust builds speed continuously', rising);
  ok('sustained thrust reaches extreme speed', last > 100, 'x' + last.toFixed(0));
  ok('the multiplier is capped', last <= M.DEFAULT_WARP_DRIVE.topMultiplier + 1e-6);

  // Releasing must bleed off rather than stop dead.
  const before = hold.update(0.016, false).multiplier;
  for (let i = 0; i < 30; i++) hold.update(0.016, false);
  const after = hold.update(0.016, false).multiplier;
  ok('releasing thrust decays the warp', after < before);

  const dis = new M.WarpDrive();
  for (let i = 0; i < 300; i++) dis.update(0.016, true);
  dis.disengage();
  ok('disengaging drops out of warp instantly', !dis.update(0.016, false).engaged);

  // A dropped or negative frame time must not corrupt the drive.
  const odd = new M.WarpDrive();
  odd.update(-1, true); odd.update(NaN, true); odd.update(0, true);
  ok('bad frame times cannot corrupt the drive',
    Number.isFinite(odd.update(0.016, true).multiplier));
}

console.log('\n— fleets and their gravity —');
{
  // Escape velocity from Earth is 11.2 km/s. This validates the formula.
  const g = M.fleetGravity(5.97e24, 6.371e6);
  ok('escape velocity of an Earth-mass sphere is 11.2 km/s',
    near(g.escapeVelocity / 1000, 11.19, 0.05),
    (g.escapeVelocity / 1000).toFixed(2) + ' km/s');
  ok('surface gravity of an Earth-mass sphere is 9.8 m/s^2',
    near(g.surfaceGravity, 9.81, 0.05), g.surfaceGravity.toFixed(2));

  // Inverse square: double the radius, quarter the gravity.
  const a = M.fleetGravity(1e18, 1000), b = M.fleetGravity(1e18, 2000);
  ok('fleet gravity obeys the inverse square law',
    near(a.surfaceGravity / b.surfaceGravity, 4, 1e-6));

  ok('a handful of fighters has no meaningful gravity',
    !M.fleetGravity(M.shipClass('fighter').mass * 50, 500).significant);
  ok('a world ship makes gravity you can feel',
    M.fleetGravity(M.shipClass('worldship').mass, 5000).significant);

  ok('scouts can never reach meaningful gravity in a tight formation',
    M.shipsForGravity(M.shipClass('scout'), 5000) > 1e6);
  ok('one world ship suffices',
    M.shipsForGravity(M.shipClass('worldship'), 5000) <= 1);
  ok('a larger formation needs more mass',
    M.shipsForGravity(M.shipClass('cruiser'), 10000) >
    M.shipsForGravity(M.shipClass('cruiser'), 1000));

  ok('every ship class has positive mass, length and speed',
    M.SHIP_CLASSES.every((c) => c.mass > 0 && c.length > 0 && c.speed > 0));
  ok('ship discipline is a fraction',
    M.SHIP_CLASSES.every((c) => c.discipline > 0 && c.discipline <= 1));
  ok('ship class ids are unique',
    new Set(M.SHIP_CLASSES.map((c) => c.id)).size === M.SHIP_CLASSES.length);
  ok('an unknown ship class resolves to null', M.shipClass('nope') === null);
}

console.log('\n— flight instruments —');
{
  // Pure formatters, testable without a DOM.
  const hudEntry = join(dir, 'hud.js');
  const hudOut = join(dir, 'hud.mjs');
  writeFileSync(hudEntry,
    "export * from '/home/user/Low/src/bjs/ui/FlightHUD.ts';");
  execFileSync('/home/user/Low/node_modules/.bin/esbuild',
    [hudEntry, '--bundle', '--format=esm', '--platform=browser',
     '--outfile=' + hudOut], { stdio: 'pipe' });
  const H = await import(hudOut);

  ok('north reads as N', H.compassPoint(0) === 'N');
  ok('a quarter turn reads as east', H.compassPoint(Math.PI / 2) === 'E');
  ok('half a turn reads as south', H.compassPoint(Math.PI) === 'S');
  ok('compass wraps past a full turn', H.compassPoint(Math.PI * 2.001) === 'N');
  ok('negative headings still resolve',
    ['N', 'NW', 'NE'].includes(H.compassPoint(-0.01)));

  ok('heading is reported in degrees', H.headingDegrees(Math.PI) === 180);
  ok('heading is always 0-359', H.headingDegrees(Math.PI * 4) === 0);
  let inRange = true;
  for (let a2 = -20; a2 < 20; a2 += 0.37) {
    const d = H.headingDegrees(a2);
    if (!(d >= 0 && d <= 359)) inRange = false;
  }
  ok('no heading ever falls outside 0-359', inRange);

  // Coordinates must be sign-prefixed and stable in width, or the numbers
  // jitter sideways as they change - the worst thing a HUD can do.
  ok('coordinates always carry a sign',
    H.formatCoord(5).startsWith('+') && H.formatCoord(-5).startsWith('-'));
  ok('large coordinates are abbreviated',
    H.formatCoord(2.5e6).includes('M') && H.formatCoord(2500).includes('k'));
  ok('coordinates survive nonsense input', H.formatCoord(NaN) === '+0.0');

  ok('speed escalates to fractions of c', H.formatSpeed(600).includes('c'));
  ok('slow speeds stay in world units', H.formatSpeed(5).includes('u/s'));
  ok('distance escalates to light years', H.formatDistance(1e6).includes('ly'));
  ok('mid distances read in AU', H.formatDistance(5000).includes('AU'));

  ok('every HUD group defaults to a boolean',
    Object.values(H.DEFAULT_HUD_ELEMENTS).every((v) => typeof v === 'boolean'));
  ok('the HUD exposes the groups the user asked to toggle',
    ['coordinates', 'attitude', 'velocity', 'warp', 'target', 'reticle']
      .every((k) => k in H.DEFAULT_HUD_ELEMENTS));
}

rmSync(dir, { recursive: true, force: true });

console.log('\n' + pass + ' passed, ' + fail + ' failed');
if (fail) process.exit(1);
