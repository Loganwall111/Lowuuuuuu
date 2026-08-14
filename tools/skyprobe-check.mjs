/**
 * Sky cubemap checks.
 *
 * A live cubemap is easy to get wrong in two opposite directions: capture
 * too rarely and reflections show a stale or black sky, capture too often
 * and six extra renders a frame destroy the budget. These assertions pin
 * both ends, plus the render-list rule that keeps a capture cheap.
 *
 * Run: node tools/skyprobe-check.mjs
 */
import { execFileSync } from 'child_process';
import fs from 'fs';

let pass = 0, fail = 0;
const ok = (name, cond, extra) => {
  if (cond) pass++;
  else { fail++; console.log('FAIL: ' + name + (extra ? ' — ' + extra : '')); }
};

const SRC = 'src/bjs/systems/SkyProbe.ts';
if (!fs.existsSync(SRC)) {
  console.log('FAIL: ' + SRC + ' is missing');
  console.log('0 passed, 1 failed');
  process.exit(1);
}
const src = fs.readFileSync(SRC, 'utf8');
const app = fs.readFileSync('src/bjs/App.ts', 'utf8');

// ------------------------------------------------------------ cheapness
ok('the probe renders only the sky, never the scene',
  /probe\.renderList = \[skyMesh\]/.test(src));
ok('the probe does not refresh itself every frame',
  /probe\.refreshRate = 0/.test(src));
ok('capture is driven by staleness, not by the clock',
  /probeIsStale\(this\.lastKey, key\)/.test(src));
ok('the face size is modest enough to be affordable', (() => {
  const m = src.match(/PROBE_SIZE = (\d+)/);
  return m && Number(m[1]) <= 512;
})());

// --------------------------------------------------------- correctness
ok('the cubemap is captured once at attach, not left black',
  /this\.captureNow\(\);/.test(src) && /CAPTURE ONCE, IMMEDIATELY/.test(src));
ok('a failed capture keeps the previous sky rather than blacking out',
  /if \(!this\.captureNow\(\)\) return false;/.test(src));
ok('a missing probe never throws', /if \(!probe\) return false/.test(src));
ok('probe failure cannot stop the scene rendering',
  /Sky cubemap unavailable/.test(src));

// ------------------------------------------------------- app integration
ok('the app builds the probe from the sky dome',
  /this\.skyProbe\.attach\(this\.scene, this\.cosmicSky\.mesh\)/.test(app));
ok('the cubemap drives scene ambient light',
  /this\.scene\.environmentTexture = envTex/.test(app));
ok('the probe follows the viewer', /this\.skyProbe\.setCenter\(eye\)/.test(app));
ok('the probe is refreshed from the same state as the dome',
  /this\.cosmicSky\.current\.medium/.test(app));
ok('the sky state cannot be mutated from outside',
  /get current\(\): Readonly<SkyState>/.test(
    fs.readFileSync('src/bjs/systems/CosmicSky.ts', 'utf8')));

// ------------------------------------------------- the staleness policy
{
  const out = '/tmp/skyprobe-' + Date.now() + '.mjs';
  try {
    execFileSync('npx', ['esbuild', SRC, '--bundle', '--format=esm',
      '--platform=node', '--log-level=error', '--outfile=' + out], { stdio: 'pipe' });
    const { probeIsStale, ZOOM_REFRESH_RATIO } = await import(out);

    const base = {
      medium: 'stars', symmetry: 0, strangeness: 0,
      tint: [0.1, 0.2, 0.3], zoom: 1
    };
    const key = (o) => ({ ...base, ...o });

    ok('a first capture is always needed', probeIsStale(null, base) === true);
    ok('an unchanged sky is never recaptured',
      probeIsStale(base, key({})) === false);
    ok('a new verse medium forces a recapture',
      probeIsStale(base, key({ medium: 'code' })) === true);
    ok('a new symmetry forces a recapture',
      probeIsStale(base, key({ symmetry: 8 })) === true);
    ok('a new tint forces a recapture',
      probeIsStale(base, key({ tint: [0.9, 0.1, 0.1] })) === true);
    ok('a new strangeness forces a recapture',
      probeIsStale(base, key({ strangeness: 0.8 })) === true);
    ok('an imperceptible tint drift does not',
      probeIsStale(base, key({ tint: [0.1005, 0.2, 0.3] })) === false);

    // The zoom rule must be scale-free, or deep zoom recaptures every frame
    // and shallow zoom never recaptures at all.
    ok('a big zoom step forces a recapture',
      probeIsStale(base, key({ zoom: ZOOM_REFRESH_RATIO * 1.1 })) === true);
    ok('a tiny zoom step does not',
      probeIsStale(base, key({ zoom: 1.001 })) === false);
    ok('the zoom rule is a ratio, so it behaves the same at any depth', (() => {
      for (const z of [1, 100, 10000, 100000]) {
        const from = { ...base, zoom: z };
        if (probeIsStale(from, key({ zoom: z * 1.001 })) !== false) return false;
        if (probeIsStale(from, key({ zoom: z * 1.5 })) !== true) return false;
      }
      return true;
    })());
    ok('zooming out is as stale as zooming in',
      probeIsStale({ ...base, zoom: 100 }, key({ zoom: 10 })) === true);
    ok('a zero zoom cannot produce a nonsense ratio',
      typeof probeIsStale({ ...base, zoom: 0 }, key({ zoom: 0 })) === 'boolean');
    ok('a NaN zoom recaptures rather than wedging',
      probeIsStale(base, key({ zoom: NaN })) === true);

    // Cost ceiling: how often would this fire during a real fractal dive?
    // The zoom rate is exponential, so count the captures across the whole
    // usable range and check the total is bounded.
    const steps = Math.ceil(
      Math.log(2e5) / Math.log(ZOOM_REFRESH_RATIO));
    ok('a full fractal dive costs a bounded number of captures',
      steps < 250, steps + ' captures across the entire zoom range');

    fs.unlinkSync(out);
  } catch (e) {
    ok('the probe bundles and its refresh policy behaves', false, e.message);
  }
}

console.log(pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
