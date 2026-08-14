/**
 * Reachable-galaxy checks.
 *
 * The galaxy used to be a backdrop: a dome at infinite distance plus point
 * shells locked to the camera. Backdrops translate with you, so no amount
 * of flying ever arrives, and because the shells sat at radius 2,000-3,800
 * while the scene lives much nearer, ordinary objects kept drawing in
 * front of the "sky". These assertions pin the properties that make the
 * galaxy a real place instead: honest coordinates, no camera lock, a depth
 * range that can actually contain it, and fog that depends on where you
 * are rather than on a global constant.
 *
 * Run: node tools/galaxyfield-check.mjs
 */
import { execFileSync } from 'child_process';
import fs from 'fs';

let pass = 0, fail = 0;
const ok = (name, cond, extra) => {
  if (cond) pass++;
  else { fail++; console.log('FAIL: ' + name + (extra ? ' — ' + extra : '')); }
};

const SRC = 'src/bjs/systems/GalaxyField.ts';
if (!fs.existsSync(SRC)) {
  console.log('FAIL: ' + SRC + ' is missing');
  console.log('0 passed, 1 failed');
  process.exit(1);
}
const src = fs.readFileSync(SRC, 'utf8');
const app = fs.readFileSync('src/bjs/App.ts', 'utf8');
const sky = fs.readFileSync('src/bjs/systems/LayeredSky.ts', 'utf8');

// ------------------------------------------------ it is a place, not a wall
ok('the galaxy is not locked to the camera',
  !/position\.set\(eye\.x \* lock/.test(src));
ok('stars are placed at real coordinates, not projected onto a shell',
  !/projectToShell/.test(src) && /p\.position = new Vector3\(/.test(src));
ok('the galaxy spans a real distance', (() => {
  const i = src.match(/FIELD_INNER = (\d+)/);
  const o = src.match(/FIELD_OUTER = (\d+)/);
  return i && o && Number(i[1]) >= 2000 && Number(o[1]) >= 50000;
})());
ok('the star count survives the move to 3D', /STAR_COUNT = 30000/.test(src));
ok('the gas arrays survive too', /GAS_COUNT = 9000/.test(src));
ok('stars follow the shared logarithmic spiral', /galaxyStar\(/.test(src));
ok('gas is sampled from the shared 3D noise field',
  /nebulaDensity\(/.test(src) && /nebulaColor\(/.test(src));

// -------------------------------------------------------- the depth problem
// A 50,000-unit structure cannot be drawn through a 4,000-unit far plane,
// and widening the main camera to reach it would wreck depth precision at
// the surface scale (minZ 0.05 against maxZ 50000 is a ratio of 1e6).
ok('the galaxy has its own camera', /new UniversalCamera\(\s*'galaxyCam'/.test(src));
ok('that camera can actually reach the far edge', (() => {
  const far = src.match(/GALAXY_FAR = (\d+)/);
  const outer = src.match(/FIELD_OUTER = (\d+)/);
  return far && outer && Number(far[1]) > Number(outer[1]) * 2;
})());
ok('its near plane is far enough out to keep depth precision', (() => {
  const near = src.match(/GALAXY_NEAR = (\d+)/);
  return near && Number(near[1]) >= 100;
})());
ok('the galaxy is on a layer of its own', /GALAXY_LAYER = 0x20000000/.test(src));
ok('the layer is outside Babylon default mask, so nothing else changes',
  (0x20000000 & 0x0fffffff) === 0);
ok('the main camera is excluded from that layer',
  /main\.layerMask = main\.layerMask & ~GALAXY_LAYER/.test(src));
ok('the galaxy draws before the main scene',
  /activeCameras = \[cam, main\]/.test(src));
ok('the second pass does not wipe the first',
  /autoClear = false/.test(src));
ok('the galaxy never writes depth, so real objects composite in front',
  /disableDepthWrite = true/.test(src));

// --------------------------------------------------- the old dome is gone
ok('the point shells no longer carry a galaxy',
  !/galaxy: true/.test(sky));
ok('the point shells no longer carry gas', !/gas: \d/.test(sky));
ok('the app builds the real galaxy',
  /this\.galaxyField\.attach\(this\.scene, this\.camera\)/.test(app) &&
  /this\.galaxyField\.build\(\)/.test(app));
ok('the app drives it every frame',
  /this\.galaxyField\.update\(eye,/.test(app));

// ------------------------------------------------------------- behaviour
{
  const out = '/tmp/galaxyfield-' + Date.now() + '.mjs';
  try {
    execFileSync('npx', ['esbuild', SRC, '--bundle', '--format=esm',
      '--platform=node', '--log-level=error', '--outfile=' + out], { stdio: 'pipe' });
    const mod = await import(out);
    const { fogAt, fogStateAt, GALAXY_CENTER, GalaxyField, FIELD_OUTER,
      makeRng } = mod;

    // THE CENTRING BUG. Centring the galaxy on the world origin put 4,876
    // of 30,000 stars within 4,000 units of the home system - the galactic
    // core would have been sitting on top of the planets.
    ok('the galaxy is not centred on the playable scene',
      Math.abs(GALAXY_CENTER[0]) > 10000, String(GALAXY_CENTER[0]));
    ok('the player starts in a spiral arm, at the origin', (() => {
      const h = GalaxyField.homePosition();
      return Math.hypot(h[0], h[1], h[2]) < 1;
    })());

    // Fog must be a function of WHERE YOU ARE.
    ok('there is no fog outside the galaxy',
      fogAt(GALAXY_CENTER[0] - FIELD_OUTER * 1.6, 0, 0) === 0);
    ok('there is fog inside the disc', fogAt(0, 0, 0) > 0.05);
    ok('fog thins as you climb out of the plane', (() => {
      const inPlane = fogAt(-8000, 0, 0);
      const above = fogAt(-8000, 6000, 0);
      return inPlane > above;
    })());
    ok('fog varies along a flight path, rather than being one flat value',
      (() => {
        const vals = [0, -6000, -12000, -20000, -34000]
          .map((x) => fogAt(x, 0, 0));
        return new Set(vals.map((v) => v.toFixed(3))).size >= 4;
      })());
    ok('you can fly out the far side into empty space', (() => {
      // March outward and confirm fog ends at zero and stays there.
      for (let r = FIELD_OUTER * 1.5; r < FIELD_OUTER * 3; r += 5000) {
        if (fogAt(GALAXY_CENTER[0] - r, 0, 0) !== 0) return false;
      }
      return true;
    })());
    ok('fog is coloured from the nebula palette, not grey', (() => {
      const s = fogStateAt(0, 0, 0);
      if (s.density <= 0) return false;
      const [r, g, b] = s.color;
      return Math.max(r, g, b) - Math.min(r, g, b) > 0.02;
    })());
    ok('fog outside the galaxy has no colour to apply',
      fogStateAt(GALAXY_CENTER[0] - FIELD_OUTER * 2, 0, 0).density === 0);

    // Robustness: a NaN position must not poison the fog state.
    ok('a NaN position yields no fog', fogAt(NaN, 0, 0) === 0);
    ok('an infinite position yields no fog', fogAt(Infinity, 0, 0) === 0);
    ok('fog is always a valid fraction', (() => {
      for (let i = 0; i < 400; i++) {
        const v = fogAt((i - 200) * 900, ((i % 7) - 3) * 800, (i % 11) * 700);
        if (!(v >= 0 && v <= 1)) return false;
      }
      return true;
    })());

    // The field must be deterministic, or the galaxy changes shape between
    // sessions and no location is ever findable twice.
    ok('the galaxy is the same place every session', (() => {
      const a = makeRng(7), b = makeRng(7);
      for (let i = 0; i < 50; i++) if (a() !== b()) return false;
      return true;
    })());

    fs.unlinkSync(out);
  } catch (e) {
    ok('the galaxy field bundles and behaves', false, e.message);
  }
}

console.log(pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
