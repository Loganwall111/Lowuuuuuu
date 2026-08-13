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

console.log('\n— throwing things at planets —');
{
  const iEntry = join(dir, 'imp.js');
  const iOut = join(dir, 'imp.mjs');
  writeFileSync(iEntry,
    "export * from '/home/user/Low/src/bjs/systems/ImpactorSystem.ts';");
  execFileSync('/home/user/Low/node_modules/.bin/esbuild',
    [iEntry, '--bundle', '--format=esm', '--platform=browser',
     '--outfile=' + iOut], { stdio: 'pipe' });
  const I = await import(iOut);

  // Screen size must compress 27 orders of magnitude of real mass into a
  // range you can actually see, while preserving the ordering.
  const rPebble = I.ImpactorSystem.visualRadius(0.2);
  const rMoon = I.ImpactorSystem.visualRadius(7.3e22);
  const rNeutron = I.ImpactorSystem.visualRadius(4.0e26);
  ok('a heavier object draws larger', rMoon > rPebble && rNeutron > rMoon);
  ok('even the heaviest object stays a sane size on screen', rNeutron <= 8);
  ok('even the lightest object stays visible', rPebble >= 0.18);
  ok('visual size is never NaN',
    Number.isFinite(I.ImpactorSystem.visualRadius(0)) &&
    Number.isFinite(I.ImpactorSystem.visualRadius(-5)));

  // Planet mass scales with the cube of radius at constant density, which is
  // what makes a gas giant genuinely tougher than a rocky world rather than
  // merely bigger on screen.
  const EARTH_VIS = 1.15;
  const massOf = (visR) => 5.97e24 * Math.pow(visR / EARTH_VIS, 3);
  const radOf = (visR) => 6.371e6 * (visR / EARTH_VIS);
  const ophion = massOf(2.9), cinder = massOf(0.62);
  ok('a gas giant far outmasses a small rocky world', ophion / cinder > 100);

  const T = await import(out);
  const rock = T.throwableById('asteroid');
  const hitSmall = T.computeImpact(rock, 20000, cinder, radOf(0.62));
  const hitGiant = T.computeImpact(rock, 20000, ophion, radOf(2.9));
  ok('the same rock does proportionally more damage to a smaller world',
    hitSmall.bindingFraction > hitGiant.bindingFraction,
    hitSmall.bindingFraction.toExponential(2) + ' vs ' +
    hitGiant.bindingFraction.toExponential(2));

  ok('gravity in world units matches the universe constant', I.WORLD_G === 42);
  ok('strays are eventually reaped', I.MAX_AGE > 0 && I.MAX_AGE <= 600);

  // Composition must map to a colour, and every catalogue entry must have one.
  const comps = new Set(T.THROWABLES.map((t) => t.composition));
  let allTinted = true;
  for (const c of comps) {
    const col = I.ImpactorSystem.tint({ composition: c });
    if (!col || !Number.isFinite(col.r)) allTinted = false;
  }
  ok('every composition in the catalogue has a colour', allTinted);
}

console.log('\n— the people who live there —');
{
  const sEntry = join(dir, 'set.js');
  const sOut = join(dir, 'set.mjs');
  writeFileSync(sEntry,
    "export * from '/home/user/Low/src/bjs/systems/SettlerSystem.ts';");
  execFileSync('/home/user/Low/node_modules/.bin/esbuild',
    [sEntry, '--bundle', '--format=esm', '--platform=browser',
     '--outfile=' + sOut], { stdio: 'pipe' });
  const S = await import(sOut);

  // Determinism: the same planet must always have the same people, or
  // "the botanist on Terrapor" means nothing between visits.
  const a1 = S.settlersFor(40917, 6);
  const a2 = S.settlersFor(40917, 6);
  ok('a planet always has the same people',
    a1.map((s) => s.name + s.trade).join('|') ===
    a2.map((s) => s.name + s.trade).join('|'));
  ok('different planets have different people',
    S.settlersFor(1, 6)[0].name !== S.settlersFor(2, 6)[0].name);
  ok('the requested number of people appear', a1.length === 6);
  ok('nobody shares a name on one planet',
    new Set(a1.map((s) => s.name)).size === a1.length);
  ok('everyone has a real trade',
    a1.every((s) => S.TRADES.includes(s.trade)));
  ok('asking for nobody yields nobody', S.settlersFor(5, 0).length === 0);
  ok('a negative population is not an error', S.settlersFor(5, -3).length === 0);

  // Mood must be ordered and total.
  ok('mood spans terrified to delighted',
    S.moodOf(-1) === 'terrified' && S.moodOf(1) === 'delighted');
  ok('indifference reads as neutral', S.moodOf(0) === 'neutral');
  let moodOk = true;
  for (let r = -1; r <= 1; r += 0.05) {
    if (typeof S.moodOf(r) !== 'string') moodOk = false;
  }
  ok('every possible regard maps to a mood', moodOk);

  // The point of the system: they remember what you did.
  const sys = new S.SettlerSystem();
  sys.settlers = S.settlersFor(40917, 6);
  const before = sys.settlers[0].regard;
  sys.witnessed(2);
  ok('dropping a moon on people makes them hate you',
    sys.settlers[0].regard < before);
  ok('shattering a world bottoms out their regard',
    S.moodOf(sys.settlers[0].regard) === 'terrified');
  ok('regard never runs below -1',
    sys.settlers.every((s) => s.regard >= -1));
  sys.pleased(5);
  ok('regard never runs above +1',
    sys.settlers.every((s) => s.regard <= 1));
  const calm = new S.SettlerSystem();
  calm.settlers = S.settlersFor(40917, 6);
  const r0 = calm.settlers[0].regard;
  calm.witnessed(0);
  ok('a harmless bounce does not upset anyone',
    calm.settlers[0].regard === r0);

  // They must actually say something, and it must change with mood.
  const happy = { ...a1[0], regard: 1 };
  const scared = { ...a1[0], regard: -1 };
  ok('people say something', typeof S.speak(a1[0], 0) === 'string' &&
    S.speak(a1[0], 0).length > 0);
  ok('what they say depends on how they feel',
    S.speak(happy, 0) !== S.speak(scared, 0));
  let varied = new Set();
  for (let t = 0; t < 8; t++) varied.add(S.speak(happy, t));
  ok('they do not repeat one line forever', varied.size > 1);
}

console.log('\n— space that does not end —');
{
  const cEntry = join(dir, 'chunk.js');
  const cOut = join(dir, 'chunk.mjs');
  writeFileSync(cEntry, `
export * from '/home/user/Low/src/bjs/systems/ChunkedUniverse.ts';
export { UniverseState, CORE_RADIUS } from '/home/user/Low/src/bjs/systems/UniverseState.ts';
export { TIERS } from '/home/user/Low/src/bjs/systems/CosmicScaleSystem.ts';
export { Vector3 } from '@babylonjs/core/Maths/math.vector';
`);
  execFileSync('/home/user/Low/node_modules/.bin/esbuild',
    [cEntry, '--bundle', '--format=esm', '--platform=browser',
     '--outfile=' + cOut], { stdio: 'pipe' });
  const C = await import(cOut);
  const V3 = C.Vector3;

  // Determinism is what allows infinity without storage: a chunk must
  // rebuild identically however you arrive at it.
  const sig = (ch) => ch.regions.map((r) =>
    r.kind + r.name + r.position.x.toFixed(4) + r.mass.toFixed(2)).join('|');
  ok('a chunk is identical every time it is generated',
    sig(C.generateChunk(5, -2, 9, C.DEFAULT_CHUNKED)) ===
    sig(C.generateChunk(5, -2, 9, C.DEFAULT_CHUNKED)));
  ok('a chunk a trillion units out is still deterministic',
    sig(C.generateChunk(400000000, 17, -900000, C.DEFAULT_CHUNKED)) ===
    sig(C.generateChunk(400000000, 17, -900000, C.DEFAULT_CHUNKED)));
  ok('different chunks hold different things',
    sig(C.generateChunk(1, 0, 0, C.DEFAULT_CHUNKED)) !==
    sig(C.generateChunk(0, 1, 0, C.DEFAULT_CHUNKED)));
  ok('a different universe seed gives a different chunk',
    sig(C.generateChunk(3, 3, 3, C.DEFAULT_CHUNKED)) !==
    sig(C.generateChunk(3, 3, 3, { ...C.DEFAULT_CHUNKED, seed: 999 })));

  // THE question: does it visibly tile? Fingerprint the layout of many
  // chunks spread over millions of units and look for repeats.
  const seen = new Set();
  let dups = 0, sampled = 0;
  for (let i = 0; i < 1500; i++) {
    const cx = ((i * 7919) % 2000003) - 1000000;
    const cy = ((i * 104729) % 1999993) - 1000000;
    const cz = ((i * 15485863) % 2000029) - 1000000;
    const ch = C.generateChunk(cx, cy, cz, C.DEFAULT_CHUNKED);
    if (!ch.regions.length) continue;
    sampled++;
    // Positions relative to the chunk, so only the *pattern* is compared.
    const rel = ch.regions.map((r) => r.kind +
      (r.position.x - cx * C.CHUNK_SIZE).toFixed(1) +
      (r.position.y - cy * C.CHUNK_SIZE).toFixed(1)).join('|');
    if (seen.has(rel)) dups++; else seen.add(rel);
  }
  ok('space never repeats its layout, even across millions of units',
    dups === 0, dups + ' repeats in ' + sampled + ' chunks');

  // ---- the fold ----
  // Past 2^53 a chunk index and its neighbour are the same float, so truly
  // novel space is not representable however clever the generator. The
  // domain is folded on purpose and each repetition re-mixed, which is what
  // keeps the repeat invisible.
  ok('coordinates fold into a finite domain',
    C.foldCoord(1e300) >= 0 && C.foldCoord(1e300) < C.SUPER_PERIOD);
  ok('negative coordinates fold to positive',
    C.foldCoord(-5) >= 0 && C.foldCoord(-C.SUPER_PERIOD - 3) >= 0);
  ok('nonsense coordinates fold safely',
    C.foldCoord(NaN) === 0 && C.foldCoord(Infinity) === 0);
  ok('the repeat period is long enough to be a journey',
    C.SUPER_PERIOD * C.CHUNK_SIZE > 1e10,
    (C.SUPER_PERIOD * C.CHUNK_SIZE / 648000 / 3600).toFixed(1) + ' h at full warp');
  ok('chunks one full period apart are NOT identical', (() => {
    const sg = (ch) => ch.regions.map((r) => r.kind + r.name +
      r.position.x.toFixed(1)).join('|');
    return sg(C.generateChunk(7, 3, 11, C.DEFAULT_CHUNKED)) !==
           sg(C.generateChunk(7 + C.SUPER_PERIOD, 3, 11, C.DEFAULT_CHUNKED));
  })());
  ok('different repetitions get different supercell indices',
    C.superIndex(1, 0, 0) !== C.superIndex(1 + C.SUPER_PERIOD, 0, 0));
  ok('chunks within one repetition share a supercell',
    C.superIndex(1, 0, 0) === C.superIndex(2, 0, 0));

  // Large-scale structure: there must be voids and clusters, not static.
  let minD = 1, maxD = 0;
  for (let i = 0; i < 3000; i++) {
    const p2 = new V3((i * 9973) % 4000000 - 2000000,
                      (i * 3571) % 4000000 - 2000000,
                      (i * 6151) % 4000000 - 2000000);
    const d = C.cosmicDensity(p2, C.DEFAULT_CHUNKED.seed, C.CHUNK_SIZE);
    minD = Math.min(minD, d); maxD = Math.max(maxD, d);
  }
  ok('there are genuine voids in space', minD < 0.05, minD.toFixed(3));
  ok('there are genuine superclusters', maxD > 0.7, maxD.toFixed(3));

  // Structure must be *smooth*, or it is noise rather than cosmology.
  let coherent = 0, trials = 0;
  for (let i = 0; i < 300; i++) {
    const p2 = new V3((i * 7717) % 900000, (i * 5843) % 900000, (i * 4211) % 900000);
    const near = new V3(p2.x + C.CHUNK_SIZE, p2.y, p2.z);
    const far = new V3(p2.x + C.CHUNK_SIZE * 400, p2.y, p2.z);
    const dn = Math.abs(C.cosmicDensity(p2, 20260813, C.CHUNK_SIZE) -
                        C.cosmicDensity(near, 20260813, C.CHUNK_SIZE));
    const df = Math.abs(C.cosmicDensity(p2, 20260813, C.CHUNK_SIZE) -
                        C.cosmicDensity(far, 20260813, C.CHUNK_SIZE));
    trials++;
    if (dn <= df) coherent++;
  }
  ok('nearby space is more alike than distant space (real structure)',
    coherent / trials > 0.8, (100 * coherent / trials).toFixed(0) + '%');

  // Memory must not grow with distance travelled.
  const st = new C.ChunkStreamer();
  st.update(new V3(0, 0, 0));
  const early = st.residentCount;
  for (let i = 0; i < 400; i++) st.update(new V3(i * C.CHUNK_SIZE * 11, 0, i * 900));
  ok('flying millions of units does not grow memory',
    st.residentCount <= early * 1.5 + 4,
    early + ' -> ' + st.residentCount + ' chunks resident');
  ok('the streamer really did generate as it went', st.generated > 1000);
  ok('regions are always available wherever you are',
    st.regions().length > 0);
  ok('revisiting a chunk does not regenerate it', (() => {
    const s2 = new C.ChunkStreamer();
    s2.update(new V3(0, 0, 0));
    const g = s2.generated;
    s2.update(new V3(10, 0, 10));
    return s2.generated === g;
  })());

  // The live universe must actually be endless.
  const u = new C.UniverseState();
  const atOrigin = u.regions.length;
  ok('the hand-built core still exists', atOrigin > 100);
  let alwaysPopulated = true;
  let nearestEver = 0;
  for (const d of [5e4, 5e5, 5e6, 1e8, 1e10, 1e12]) {
    const eye = new V3(d, 0, 0);
    u.streamAround(eye);
    const n = u.nearest(eye);
    if (!n) { alwaysPopulated = false; break; }
    nearestEver = Math.max(nearestEver,
      Math.hypot(n.position.x - d, n.position.y, n.position.z));
  }
  ok('there is always something nearby, however far out you go',
    alwaysPopulated);
  ok('the nearest thing is always within reach, not a speck on the horizon',
    nearestEver < C.CHUNK_SIZE * 4, nearestEver.toFixed(0) + ' units');
  ok('the core is never duplicated by generated space', (() => {
    const u2 = new C.UniverseState();
    u2.streamAround(new V3(0, 0, 0));
    const ids = u2.regions.map((r) => r.id);
    return new Set(ids).size === ids.length;
  })());

  // Tier boundaries must be far enough apart to be journeys.
  const WARP = 720 * 900;
  ok('tier boundaries increase outward',
    C.TIERS.every((t, i) => i === 0 || t.boundary > C.TIERS[i - 1].boundary));
  ok('the first tier is seconds away, not instant',
    C.TIERS[0].boundary / WARP > 5);
  ok('the outermost tier takes an absurdly long time to reach',
    C.TIERS[C.TIERS.length - 1].boundary / WARP > 3.15e7,
    (C.TIERS[C.TIERS.length - 1].boundary / WARP / 3.15e7).toFixed(1) + ' years at full warp');
  ok('populated space extends past the first tier boundary', (() => {
    const u3 = new C.UniverseState();
    const eye = new V3(C.TIERS[0].boundary * 1.5, 0, 0);
    u3.streamAround(eye);
    return !!u3.nearest(eye);
  })());
}

console.log('\n— things with arms —');
{
  const gEntry = join(dir, 'geo.js');
  const gOut = join(dir, 'geo.mjs');
  writeFileSync(gEntry,
    "export * from '/home/user/Low/src/bjs/systems/CreatureGeometry.ts';");
  execFileSync('/home/user/Low/node_modules/.bin/esbuild',
    [gEntry, '--bundle', '--format=esm', '--platform=browser',
     '--outfile=' + gOut], { stdio: 'pipe' });
  const G = await import(gOut);

  const spec = { ...G.DEFAULT_ARM, length: 4, baseRadius: 0.2 };
  const path = G.armPath(spec, 0);
  ok('an arm has the requested number of segments', path.length === spec.segments);
  ok('an arm starts at the body', path[0].length() < 0.001);
  ok('an arm actually reaches outward',
    path[path.length - 1].length() > spec.length * 0.3,
    path[path.length - 1].length().toFixed(2));

  // The curve must be smooth: no segment may jump further than the others,
  // or the tube kinks.
  let maxStep = 0, minStep = Infinity;
  for (let i = 1; i < path.length; i++) {
    const d = path[i].subtract(path[i - 1]).length();
    maxStep = Math.max(maxStep, d); minStep = Math.min(minStep, d);
  }
  ok('the arm curve has no kinks', maxStep / Math.max(minStep, 1e-6) < 6,
    'step ratio ' + (maxStep / minStep).toFixed(2));

  // Arms must taper, and never to exactly zero (a zero-radius tube is
  // degenerate geometry).
  ok('an arm is thickest at the base',
    G.armRadius(spec, 0) > G.armRadius(spec, 0.5));
  ok('an arm tapers to a tip',
    G.armRadius(spec, 1) < G.armRadius(spec, 0) * 0.15);
  ok('the tip still has non-zero thickness', G.armRadius(spec, 1) > 0);
  ok('radius is clamped outside 0-1',
    G.armRadius(spec, 2) > 0 && G.armRadius(spec, -1) > 0);
  let tapers = true;
  for (let t = 0; t < 1; t += 0.05) {
    if (G.armRadius(spec, t + 0.05) > G.armRadius(spec, t) + 1e-9) tapers = false;
  }
  ok('thickness never increases toward the tip', tapers);

  // Arms must move, and not all in lockstep.
  const t0 = G.armPath(spec, 0), t1 = G.armPath(spec, 1.7);
  ok('arms move over time',
    t0.some((p, i) => Vector3Dist(p, t1[i]) > 0.01));
  const armA = G.armPath({ ...spec, phase: 0 }, 1);
  const armB = G.armPath({ ...spec, phase: 2.1 }, 1);
  ok('arms do not all move in lockstep',
    armA.some((p, i) => Vector3Dist(p, armB[i]) > 0.01));

  // Motion must stay bounded - an arm that drifts is a bug you only see
  // after a minute of watching.
  let drift = 0;
  for (let t = 0; t < 400; t += 3.7) {
    const pth = G.armPath(spec, t);
    drift = Math.max(drift, pth[pth.length - 1].length());
  }
  ok('arms never drift away from the body over time',
    drift < spec.length * 1.6, drift.toFixed(2));

  ok('the octopus defaults to eight arms', G.DEFAULT_OCTOPUS.arms === 8);
  ok('deep-sea creatures glow', G.DEFAULT_OCTOPUS.glow > 0);
}

function Vector3Dist(a, b) {
  return Math.hypot(a.x - b.x, a.y - b.y, a.z - b.z);
}

console.log('\n— the end of the universe —');
{
  const oEntry = join(dir, 'outer.js');
  const oOut = join(dir, 'outer.mjs');
  writeFileSync(oEntry, `
export * from '/home/user/Low/src/bjs/systems/OuterVerses.ts';
export * from '/home/user/Low/src/bjs/systems/VerseRenderer.ts';
export * from '/home/user/Low/src/bjs/systems/BlackHoleBody.ts';
`);
  execFileSync('/home/user/Low/node_modules/.bin/esbuild',
    [oEntry, '--bundle', '--format=esm', '--platform=browser',
     '--outfile=' + oOut], { stdio: 'pipe' });
  const O = await import(oOut);

  // The coordinate, exactly as the user gave it.
  const EXACT =
    '9999999999999999999999999999999999999999999999999999999999999999999999' +
    '9999999999999999999999999999999999999999999999999999999999999999999999' +
    '9999999999999999999999999999999999999999999999999999999999999999999999' +
    '9999999999999999999999999828282822282829999999999999999999999999999999' +
    '9999999999992828288282822828822899999999999999999999999999999999999999' +
    '9999999999999999999999999999999999999999999999999999272282292882';
  ok('the final coordinate is stored exactly as given',
    O.FINAL_COORDINATE === EXACT, O.FINAL_COORDINATE.length + ' digits');
  ok('the final coordinate really is 414 digits', O.FINAL_DIGITS === 414);
  // The reason depth exists at all.
  ok('the coordinate genuinely overflows a float64',
    !Number.isFinite(Number(O.FINAL_COORDINATE)));
  ok('but its depth is a small integer', O.FINAL_DIGITS < 1000);

  // Depth must be monotonic and never blow up anywhere on the scale.
  ok('depth counts digits', O.depthOf(1) === 1 && O.depthOf(999) === 3 &&
    O.depthOf(1000) === 4);
  ok('depth at the origin is zero', O.depthOf(0) === 0);
  ok('depth handles nonsense', O.depthOf(NaN) === 0 && O.depthOf(-5) === 1);
  let mono = true, prev = -1;
  for (let e = 0; e < 300; e += 3) {
    const d = O.depthOf(Math.pow(10, e));
    if (d < prev) mono = false;
    prev = d;
  }
  ok('depth never decreases as you travel outward', mono);
  let allResolve = true;
  for (let d = 0; d <= O.FINAL_DIGITS; d++) {
    const v = O.verseAt(d);
    if (!v || !Number.isFinite(O.verseProgress(d))) allResolve = false;
  }
  ok('every depth from 0 to the end resolves without overflow', allResolve);

  // Verse ordering.
  ok('verses are ordered outward',
    O.VERSES.every((v, i) => i === 0 || v.depth > O.VERSES[i - 1].depth));
  ok('you start in the universe', O.verseAt(0).id === 'universe');
  ok('the last verse sits at the final coordinate',
    O.VERSES[O.VERSES.length - 1].depth === O.FINAL_DIGITS);
  ok('the user\'s verses all exist',
    ['metaverse', 'codeverse', 'squareverse', 'octagonverse', 'tripleverse',
     'edge', 'mandelbrot', 'cubefield'].every(
      (id) => O.VERSES.some((v) => v.id === id)));
  ok('reaching the end is detected',
    O.isAtFinalCoordinate(O.FINAL_DIGITS) && !O.isAtFinalCoordinate(1));
  ok('strangeness increases outward',
    O.VERSES.every((v, i) => i === 0 || v.strangeness >= O.VERSES[i - 1].strangeness));

  // The Nothing: space must empty smoothly, not hit a wall.
  ok('normal space is not empty', O.edgeStateAt(0).emptiness === 0);
  ok('space is completely empty at the boundary',
    O.edgeStateAt(1).emptiness > 0.99);
  ok('you can look back at a wall of light',
    O.edgeStateAt(0.98).wallBrightness > 0.5);
  let smoothEmpty = true, last = -1;
  for (let p2 = 0; p2 <= 1; p2 += 0.01) {
    const e = O.edgeStateAt(p2).emptiness;
    if (e < last - 1e-9) smoothEmpty = false;
    last = e;
  }
  ok('space thins smoothly rather than hitting a wall', smoothEmpty);
  ok('there are fewer objects as space empties',
    O.edgeStateAt(0.99).densityScale < O.edgeStateAt(0.5).densityScale);

  // Crossing must move you somewhere real.
  const c = O.crossInto('universe');
  ok('entering the nothing moves you to the next verse',
    !!c && c.to.id === 'metaverse');
  ok('you arrive somewhere representable',
    !!c && Number.isFinite(c.arriveAt) && c.arriveAt > 0);
  ok('every verse but the last can be crossed out of',
    O.VERSES.slice(0, -1).every((v) => !!O.crossInto(v.id)));
  ok('there is nothing past the final verse',
    O.crossInto(O.VERSES[O.VERSES.length - 1].id) === null);

  // The verses must actually look different from each other.
  const shape = (id, n = 600) => {
    const v = O.VERSES.find((x) => x.id === id);
    const pts = O.versePoints(v, n, 1000, 7);
    const xs = pts.map((p2) => p2.position.x);
    const ys = pts.map((p2) => p2.position.y);
    return {
      n: pts.length,
      spanY: Math.max(...ys) - Math.min(...ys),
      spanX: Math.max(...xs) - Math.min(...xs),
      lattice: new Set(xs.map((x) => x.toFixed(2))).size
    };
  };
  const edge = shape('edge'), sq = shape('squareverse'), uni = shape('universe');
  ok('the edge of reality really is a string',
    edge.spanY / edge.spanX < 0.02,
    'Y/X ratio ' + (edge.spanY / edge.spanX).toFixed(4));
  ok('the geometric verses snap to a lattice',
    sq.lattice < uni.lattice / 8,
    sq.lattice + ' distinct vs ' + uni.lattice + ' in open space');
  ok('ordinary space does not snap to a lattice', uni.lattice > 400);
  ok('every verse generates something to look at',
    O.VERSES.every((v) => O.versePoints(v, 400, 1000, 3).length > 0));

  // Mandelbrot must be the real set, not a squiggle.
  ok('the origin is inside the Mandelbrot set',
    O.mandelbrotEscape(0, 0) === 0);
  ok('-1+0i is inside the set', O.mandelbrotEscape(-1, 0) === 0);
  ok('2+2i escapes immediately', O.mandelbrotEscape(2, 2) > 0);
  ok('the set is symmetric about the real axis',
    O.mandelbrotEscape(-0.5, 0.4) === O.mandelbrotEscape(-0.5, -0.4));

  // ---- black hole horizon ----
  ok('anomalies are rare but real', (() => {
    let hits = 0;
    const N = 20000;
    for (let i = 0; i < N; i++) if (O.rollAnomaly(i)) hits++;
    const pct = hits / N;
    return pct > 0.04 && pct < 0.11;
  })(), (() => {
    let hits = 0;
    for (let i = 0; i < 20000; i++) if (O.rollAnomaly(i)) hits++;
    return (100 * hits / 20000).toFixed(2) + '%';
  })());
  ok('a given hole is always the same kind',
    O.rollAnomaly(12345) === O.rollAnomaly(12345));
  ok('a standard horizon masks the inner disc',
    O.sphereRadiusFor(3.2, false) > 3.2);
  ok('a fractured horizon exposes the pattern',
    O.sphereRadiusFor(3.2, true) < 3.2);
  ok('the anomaly shrinks toward the dead centre',
    O.ANOMALY_COVER < 0.5 && O.STANDARD_COVER > 1);
  ok('horizon radius always scales with the disk',
    O.sphereRadiusFor(10, false) > O.sphereRadiusFor(5, false));

  // The reported bug: sphere and disk must not be able to separate.
  const body = new O.BlackHoleBody({
    center: { x: 1, y: 2, z: 3, copyFrom() {}, clone() { return this; } },
    diskInner: 3.2, isAnomaly: false
  });
  ok('a hole with no mesh is trivially locked', body.isLocked());
  ok('the shader centre is the same object as the body centre',
    body.shaderCenter() === body.center);
}

rmSync(dir, { recursive: true, force: true });

console.log('\n' + pass + ' passed, ' + fail + ' failed');
if (fail) process.exit(1);
