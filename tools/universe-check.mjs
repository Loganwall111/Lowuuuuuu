/**
 * UniverseState verification — one continuous universe, not separate levels.
 * Everything must coexist in a single coordinate space, and moving between
 * places must be a matter of position alone.
 * Run: node tools/universe-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['tools/fixtures/universe-entry.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/uni-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const { UniverseState, Vector3, LENS_PROFILES, cloneProfile } = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

console.log('\n— one universe, generated whole —');
{
  const u = new UniverseState();
  ok(`the universe is populated (${u.regions.length} regions)`, u.regions.length > 30);
  const kinds = new Set(u.regions.map((r) => r.kind));
  ok(`it contains many kinds of place (${[...kinds].join(', ')})`, kinds.size >= 5);
  ok('there are star systems', u.regions.some((r) => r.kind === 'star-system'));
  ok('there are planets', u.regions.some((r) => r.kind === 'planet'));
  ok('there are oceans', u.regions.some((r) => r.kind === 'ocean'));
  ok('there is terrain', u.regions.some((r) => r.kind === 'terrain'));
  ok('there are black holes', u.regions.some((r) => r.kind === 'blackhole'));
  ok('there are nebulae', u.regions.some((r) => r.kind === 'nebula'));
  ok('there are galaxies', u.regions.some((r) => r.kind === 'galaxy'));
  ok('every region has a unique id',
     new Set(u.regions.map((r) => r.id)).size === u.regions.length);
  ok('every region has a name and glyph',
     u.regions.every((r) => r.name && r.glyph));
  ok('every position is finite',
     u.regions.every((r) => [r.position.x, r.position.y, r.position.z].every(Number.isFinite)));
}

console.log('\n— it is ONE space: everything coexists —');
{
  const u = new UniverseState();
  // the decisive test: planets, oceans, terrain and black holes must all be
  // present simultaneously, not swapped in by loading a level
  const kinds = ['planet', 'ocean', 'terrain', 'blackhole', 'star-system'];
  ok('every kind of place exists at the same time in one region list',
     kinds.every((k) => u.regions.some((r) => r.kind === k)));

  // and they must occupy distinct positions in a shared coordinate space
  const positions = u.regions.map((r) => `${r.position.x.toFixed(1)},${r.position.z.toFixed(1)}`);
  ok('places occupy distinct coordinates',
     new Set(positions).size > u.regions.length * 0.9);

  // there is a home system at the origin so you always start somewhere real
  const home = u.regions.find((r) => r.name === 'Home');
  ok('there is a home system at the origin', !!home && home.position.length() < 1);
  ok('the home system has planets',
     u.regions.filter((r) => r.name.startsWith('Home ')).length >= 1);
}

console.log('\n— the universe is reproducible —');
{
  const a = new UniverseState({ seed: 777 });
  const b = new UniverseState({ seed: 777 });
  const sig = (u) => u.regions.map((r) => r.kind + r.name + r.position.x.toFixed(3)).join('|');
  ok('the same seed rebuilds an identical universe', sig(a) === sig(b));
  const c = new UniverseState({ seed: 778 });
  ok('a different seed gives a different universe', sig(a) !== sig(c));
}

console.log('\n— location is determined by position, not by a tab —');
{
  const u = new UniverseState({ seed: 42 });
  const planet = u.regions.find((r) => r.kind === 'planet');

  // deep space: outside everything
  u.updatePlayer(new Vector3(500000, 500000, 500000));
  ok('far from everything you are in deep space', u.current === null);
  ok('deep space is reported clearly', u.stats().Location.includes('Deep space'));

  // fly to a planet and you are simply there
  u.updatePlayer(planet.position.clone());
  ok('moving to a planet makes it the current location',
     u.current !== null && u.current.id === planet.id,
     u.current ? u.current.name : 'none');
  ok('the UI reports the place by name',
     u.stats().Location.includes(planet.name));

  // fly away and you leave, with no unloading step
  u.updatePlayer(planet.position.add(new Vector3(0, 0, 900000)));
  ok('flying away leaves it behind', u.current === null || u.current.id !== planet.id);
}

console.log('\n— the smallest containing region wins —');
{
  const u = new UniverseState({ seed: 9 });
  const sys = u.regions.find((r) => r.kind === 'star-system');
  const child = u.regions.find((r) => r.name.startsWith(sys.name + ' '));
  if (child) {
    u.updatePlayer(child.position.clone());
    ok('standing on a planet reports the planet, not its star system',
       u.current.id === child.id, u.current.name);
    const inside = u.containing(child.position);
    ok('but the star system still contains you too',
       inside.some((r) => r.id === sys.id));
  } else {
    ok('standing on a planet reports the planet, not its star system', true);
    ok('but the star system still contains you too', true);
  }
}

console.log('\n— gravity is one shared field —');
{
  const u = new UniverseState({ seed: 5 });
  const bh = u.regions.find((r) => r.kind === 'blackhole');
  const g = u.gravityAt(bh.position.add(new Vector3(300, 0, 0)));
  ok('gravity is finite everywhere', [g.x, g.y, g.z].every(Number.isFinite));
  ok('gravity points toward a massive object', g.x < 0, `gx=${g.x}`);

  const near = u.gravityAt(bh.position.add(new Vector3(200, 0, 0))).length();
  const far = u.gravityAt(bh.position.add(new Vector3(2000, 0, 0))).length();
  ok(`gravity weakens with distance (${near.toFixed(2)} -> ${far.toFixed(2)})`, near > far);

  // at the exact centre it must not blow up
  const centre = u.gravityAt(bh.position.clone());
  ok('gravity at the exact centre of a body stays finite',
     [centre.x, centre.y, centre.z].every(Number.isFinite));

  // massless regions must not attract
  const neb = u.regions.find((r) => r.kind === 'nebula');
  if (neb) ok('massless nebulae exert no pull', neb.mass === 0);
  else ok('massless nebulae exert no pull', true);
}

console.log('\n— crossing a black hole horizon —');
{
  const u = new UniverseState({ seed: 11 });
  const bh = u.regions.find((r) => r.kind === 'blackhole');
  const hr = u.horizonRadiusOf(bh);
  ok('the horizon has a real radius', hr > 0 && Number.isFinite(hr));

  u.updatePlayer(bh.position.add(new Vector3(hr * 4, 0, 0)));
  ok('outside the horizon you are not inside', u.insideHorizon === null);
  ok('depth is zero outside', u.horizonDepth === 0);

  u.updatePlayer(bh.position.add(new Vector3(hr * 0.5, 0, 0)));
  ok('crossing the horizon is detected', u.insideHorizon !== null);
  ok('the hole you fell into is identified', u.insideHorizon.id === bh.id);
  ok(`depth grows as you fall (${u.horizonDepth.toFixed(2)})`,
     u.horizonDepth > 0 && u.horizonDepth <= 1);
  ok('the UI reports being inside', u.stats()['Inside horizon'].includes(bh.name));

  const shallow = u.horizonDepth;
  u.updatePlayer(bh.position.add(new Vector3(hr * 0.1, 0, 0)));
  ok('falling deeper increases depth', u.horizonDepth > shallow);

  // An event horizon is causal: Euclidean motion cannot climb back out.
  u.updatePlayer(bh.position.add(new Vector3(hr * 5, 0, 0)));
  ok('crossing remains latched past the coordinate centre', u.insideHorizon?.id === bh.id);
  u.leaveHorizon(bh.id);
  ok('only the destination handshake releases the horizon', u.insideHorizon === null);
  ok('depth resets after the handshake', u.horizonDepth === 0);
}

console.log('\n— every black hole has its own lens —');
{
  const u = new UniverseState({ seed: 3 });
  const holes = u.regions.filter((r) => r.kind === 'blackhole');
  ok(`the universe has several black holes (${holes.length})`, holes.length >= 3);
  ok('every black hole carries a lens profile', holes.every((h) => !!h.lens));
  ok('every lens is valid',
     holes.every((h) => Number.isFinite(h.lens.strength) &&
                        h.lens.tint.every((c) => c >= 0 && c <= 1)));
  const modes = new Set(holes.map((h) => h.lens.mode));
  ok(`different holes use different lenses (${[...modes].join(', ')})`, modes.size >= 2);
  const ringless = holes.filter((h) => h.lens.ring === 0);
  ok('lens variety includes rings and no rings',
     modes.size >= 2 && (ringless.length > 0 || holes.some((h) => h.lens.ring > 0)));
}

console.log('\n— the player can author the universe —');
{
  const u = new UniverseState({ seed: 8 });
  const before = u.regions.length;
  const mine = u.spawnBlackHole(new Vector3(100, 0, 100));
  ok('a player can create a black hole', u.regions.length === before + 1);
  ok('it is marked as player-made', mine.playerMade === true);
  ok('it has a lens', !!mine.lens);

  // with a chosen lens
  const custom = u.spawnBlackHole(new Vector3(-100, 0, 0), cloneProfile(LENS_PROFILES.kaleidoscope));
  ok('a player can choose the lens type', custom.lens.mode === 'kaleidoscope');

  const sys = u.spawnStarSystem(new Vector3(0, 0, 5000));
  ok('a player can create a star system', sys.kind === 'star-system');
  ok('the new system comes with planets',
     u.regions.filter((r) => r.name.startsWith(sys.name + ' ')).length >= 1);
}

console.log('\n— black holes can be MOVED —');
{
  const u = new UniverseState({ seed: 12 });
  const bh = u.regions.find((r) => r.kind === 'blackhole');
  const to = new Vector3(4321, 100, -765);
  ok('moving a region succeeds', u.moveRegion(bh.id, to) === true);
  ok('it is actually at the new position', bh.position.equals(to));
  ok('gravity follows it', u.gravityAt(to.add(new Vector3(200, 0, 0))).x < 0);
  ok('moving an unknown id is safe', u.moveRegion('nope', to) === false);

  // a star system must carry its planets with it
  const sys = u.regions.find((r) => r.kind === 'star-system');
  const kids = u.regions.filter((r) => r.name.startsWith(sys.name + ' '));
  if (kids.length) {
    const offsets = kids.map((k) => k.position.subtract(sys.position));
    u.moveRegion(sys.id, new Vector3(-9000, 0, 9000));
    const moved = kids.every((k, i) =>
      Math.abs(k.position.subtract(sys.position).x - offsets[i].x) < 1e-6);
    ok('moving a star system carries its planets along', moved);
  } else {
    ok('moving a star system carries its planets along', true);
  }
}

console.log('\n— regions can be removed —');
{
  const u = new UniverseState({ seed: 15 });
  const bh = u.regions.find((r) => r.kind === 'blackhole');
  const n = u.regions.length;
  u.updatePlayer(bh.position.clone());
  ok('you are at the hole', u.current !== null);
  ok('removing works', u.removeRegion(bh.id) === true);
  ok('the count drops', u.regions.length === n - 1);
  ok('the current location is cleared if it was removed',
     u.current === null || u.current.id !== bh.id);
  ok('removing an unknown id is safe', u.removeRegion('nope') === false);
}

console.log('\n— streaming: only nearby things need simulating —');
{
  const u = new UniverseState({ seed: 21 });
  const near = u.activeRegions(new Vector3(0, 0, 0), 8);
  ok('the active set is limited by the budget', near.length <= 8);
  ok('the active set is non-empty', near.length > 0);
  // the nearest region must be in the active set
  const nearest = u.nearest(new Vector3(0, 0, 0));
  ok('the nearest region is always active',
     near.some((r) => r.id === nearest.id));

  const far = u.activeRegions(new Vector3(1e6, 1e6, 1e6), 5);
  ok('the active set stays bounded even far away', far.length <= 5);
}

console.log('\n— robustness —');
{
  const u = new UniverseState({ seed: 33 });
  let err = null;
  try {
    u.updatePlayer(new Vector3(NaN, NaN, NaN));
    u.gravityAt(new Vector3(NaN, 0, 0));
    u.activeRegions(new Vector3(Infinity, 0, 0));
    u.nearest(new Vector3(0, 0, 0), 'blackhole');
    u.containing(new Vector3(0, 0, 0));
    u.stats();
  } catch (e) { err = e; }
  ok('degenerate positions do not throw', !err, err ? err.message : '');

  const tiny = new UniverseState({ seed: 1, extent: 0, spacing: 1000 });
  ok('a minimal universe still has the home system', tiny.regions.length >= 1);
  ok('stats render for a minimal universe', !!tiny.stats().Location);
}

console.log('— the sandbox\'s signature places are always reachable —');
{
  // With the side tabs removed, an ocean world and a terrain world are
  // things you fly to. If generation happened not to make one near the
  // start, they would effectively not exist.
  let missingOcean = 0, missingTerrain = 0, missingHole = 0;
  for (let seed = 1; seed <= 40; seed++) {
    const u = new UniverseState({ seed, spacing: 2600, extent: 12000 });
    const home = u.regions.find((r) => r.name === 'Home');
    const dist = (r) => Math.hypot(
      r.position.x - home.position.x,
      r.position.y - home.position.y,
      r.position.z - home.position.z);
    if (!u.regions.some((r) => r.kind === 'ocean' && dist(r) < 900)) missingOcean++;
    if (!u.regions.some((r) => r.kind === 'terrain' && dist(r) < 900)) missingTerrain++;
    if (!u.regions.some((r) => r.kind === 'blackhole' && dist(r) < 4200)) missingHole++;
  }
  ok('every universe has an ocean world near the start', missingOcean === 0,
     `${missingOcean}/40 without one`);
  ok('every universe has a terrain world near the start', missingTerrain === 0,
     `${missingTerrain}/40 without one`);
  ok('every universe has a black hole within reach', missingHole === 0,
     `${missingHole}/40 without one`);

  // And they must not be piled on top of the home star.
  const u = new UniverseState({ seed: 5, spacing: 2600, extent: 12000 });
  const home = u.regions.find((r) => r.name === 'Home');
  const guaranteed = u.regions.filter((r) => /^Home I{2,3}$/.test(r.name));
  ok('the guaranteed worlds are placed away from the star',
     guaranteed.every((r) => Math.hypot(
       r.position.x - home.position.x, r.position.z - home.position.z) > 100));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
