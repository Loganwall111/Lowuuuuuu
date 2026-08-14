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
// Colour now goes through galaxyGasColor(), which dispatches to the
// photoreal palette or the rare neon one. nebulaColor is still the anomaly
// branch, it is just no longer named directly at this call site.
ok('gas is sampled from the shared 3D noise field',
  /nebulaDensity\(/.test(src) && /galaxyGasColor\(/.test(src));

// -------------------------------------------------------- the depth problem
// A 50,000-unit structure cannot be drawn through a 4,000-unit far plane,
// and widening the main camera to reach it would wreck depth precision at
// the surface scale (minZ 0.05 against maxZ 50000 is a ratio of 1e6).
// The depth problem was originally solved with a second camera at
// 500..200,000. That is the RIGHT answer in isolation and the WRONG one
// here: DefaultRenderingPipeline is attached to the main camera only, and
// a post-process pipeline blits its own framebuffer over the backbuffer,
// erasing whatever the first camera drew. The galaxy rendered every frame
// and was then painted over - it vanished completely.
//
// It is now drawn by the ordinary camera, with each point remapped along
// its true view direction into a shell inside the existing far plane.
// These assertions pin that, and specifically forbid the regression.
ok('there is no second camera for a post-process to paint over',
  !/new UniversalCamera\(\s*'galaxyCam'/.test(src));
ok('the galaxy renders on the single main camera',
  /scene\.activeCameras = null/.test(src));
ok('the proxy shell fits inside the main camera far plane', (() => {
  const o = src.match(/PROXY_OUTER = (\d+)/);
  return o && Number(o[1]) < 4000;
})());
ok('the shell is far enough out to sit behind the scene', (() => {
  const i = src.match(/PROXY_INNER = (\d+)/);
  return i && Number(i[1]) > 1500;
})());
ok('direction is preserved, so only distance is compressed',
  /Direction is preserved exactly|direction is computed from/.test(src));
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

// -------------------------------------- exactly ONE Milky Way, not two
// The dome used to paint a galaxy band for ordinary space. Now that the
// Milky Way is real geometry, a painted band would be a second copy at
// infinite distance: it could never move as you flew toward it, so the
// real galaxy would visibly slide against a stuck duplicate.
{
  const dome = fs.readFileSync('src/bjs/shaders/CosmicSkyShader.ts', 'utf8');
  const ordinary = (dome.match(
    /if \(medium < 0\.5\)\{[\s\S]*?\} else if/) || [''])[0];
  ok('ordinary space does not paint a galaxy band',
    !/skyGalaxy\(/.test(ordinary));
  ok('the deep field that remains is not our galaxy',
    /skyDeepField\(/.test(dome) && /NOT our Milky Way/.test(dome));
  ok('the real galaxy is the only Milky Way',
    /THE MILKY WAY IS REAL GEOMETRY|NO PAINTED GALAXY/.test(dome));
}

// ------------------------------- the galaxy belongs to ordinary space
ok('the field can be hidden', /setVisible\(on: boolean\)/.test(src));
ok('hiding the galaxy also clears its fog',
  /if \(!this\.visible\) \{ scene\.fogMode = 0; return; \}/.test(src));
ok('the app hides it outside ordinary space',
  /this\.galaxyField\.setVisible\(verse\.medium === 'stars'\)/.test(app));

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
    // This assertion used to require an ABSOLUTE channel spread > 0.02,
    // which conflates "has a hue" with "is bright". Babylon's EXP fog lerps
    // the entire frame toward fogColor, so a fog colour bright enough to
    // pass that test painted the whole sky mid-grey and buried the stars -
    // the exact defect in the reported screenshot. What actually matters is
    // that the fog keeps its HUE while staying dark, so both are tested.
    ok('fog keeps a hue rather than going neutral grey', (() => {
      const s = fogStateAt(0, 0, 0);
      if (s.density <= 0) return false;
      const [r, g, b] = s.color;
      const mx = Math.max(r, g, b), mn = Math.min(r, g, b);
      return mx > 1e-5 && (mx - mn) / mx > 0.12;
    })());
    ok('fog is dark enough not to wash the sky out', (() => {
      const s = fogStateAt(0, 0, 0);
      const [r, g, b] = s.color;
      // Luminance of the densest fog in the galaxy must stay well below the
      // mid grey that swallowed the starfield.
      return r * 0.3 + g * 0.6 + b * 0.1 < 0.08;
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
