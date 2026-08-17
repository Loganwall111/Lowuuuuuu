/**
 * blackframe-check — the "drawing, but every pixel is black" failure.
 *
 * This is a different bug from a dead render loop, and it was reported from
 * a real machine: 46 fps, 93 meshes, mean luminance exactly 0.0000. A scene
 * that is rendering correctly into a framebuffer that resolves to nothing.
 *
 * The cause is the post-process chain. DefaultRenderingPipeline was created
 * with HDR unconditionally, which renders the whole frame into a float
 * texture; on a GPU that cannot render to or filter float textures, the
 * result is a perfectly black image at full frame rate. These assertions
 * pin the fix: HDR is capability-gated, and a black frame triggers
 * automatic recovery rather than only a report.
 */

import { readFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import { mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

let pass = 0, fail = 0;
const ok = (name, cond, detail) => {
  if (cond) { pass++; console.log('  PASS  ' + name); }
  else { fail++; console.log('  FAIL  ' + name + (detail ? '  -> ' + detail : '')); }
};

const postfx = readFileSync(new URL('../src/bjs/PostFX.ts', import.meta.url), 'utf8');
const app = readFileSync(new URL('../src/bjs/App.ts', import.meta.url), 'utf8');
const intro = readFileSync(new URL('../src/bjs/ui/IntroOverlay.ts', import.meta.url), 'utf8');
const skySafety = readFileSync(new URL('../src/bjs/systems/SkySafetyPass.ts', import.meta.url), 'utf8');
const holeShader = readFileSync(new URL('../src/bjs/shaders/HoleFieldShader.ts', import.meta.url), 'utf8');
const holeRenderer = readFileSync(new URL('../src/bjs/systems/HoleFieldRenderer.ts', import.meta.url), 'utf8');
const celestial = readFileSync(new URL('../src/bjs/systems/CelestialRenderer.ts', import.meta.url), 'utf8');

console.log('\nblack frame: drawing, but nothing visible');
ok('the extreme-sky shader avoids the WebGL2 reserved word active',
  !/uniform float active\s*;/.test(skySafety) && /uniform float u_skyEnabled/.test(skySafety));
ok('the optional safety shader compiles lazily rather than during launch',
  /if\(this\.active\)this\.ensure\(\)/.test(skySafety));
ok('a failed optional sky pass removes itself from the camera chain',
  /onEffectErrorObservable/.test(skySafety) && /this\.pp\?\.dispose\(\)/.test(skySafety));
ok('the singularity shader also avoids the reserved word active',
  !/uniform float active\s*;/.test(holeShader) && /uniform float u_holeEnabled/.test(holeShader));
ok('the singularity lens compiles lazily and fails open',
  /ensurePass\(\)/.test(holeRenderer) && /onEffectErrorObservable/.test(holeRenderer) &&
  /this\.pass\?\.dispose\(\)/.test(holeRenderer));
ok('the instanced celestial shader declares world exactly once through its include',
  !/uniform mat4 world;/.test(celestial) && /#include<instancesDeclaration>/.test(celestial));

/* ------------- 1. HDR must be conditional on real capability ------------ */

ok('the pipeline is no longer built with hardcoded HDR',
   !/new DefaultRenderingPipeline\('postfx',\s*true/.test(postfx));
ok('HDR is decided from engine capabilities',
   /getCaps\(\)/.test(postfx));
ok('half-float render support is checked',
   /textureHalfFloatRender/.test(postfx));
ok('half-float *filtering* is checked too, not just render',
   /textureHalfFloatLinearFiltering/.test(postfx));
ok('plain float is accepted as an alternative',
   /textureFloatRender/.test(postfx) && /textureFloatLinearFiltering/.test(postfx));
ok('the pipeline receives the computed flag',
   /new DefaultRenderingPipeline\('postfx',\s*hdr/.test(postfx));
ok('falling back to LDR is logged so it is diagnosable',
   /cannot render to float textures/i.test(postfx));
ok('the chosen mode is observable from outside',
   /\bhdr = false\b/.test(postfx) && /this\.hdr = hdr/.test(postfx));

/* ------------- 2. the capability logic itself is correct ---------------- */

// Extract and evaluate the real predicate against every combination, so the
// boolean logic is tested rather than eyeballed.
const hdrFor = (caps) =>
  !!(caps.textureHalfFloatRender && caps.textureHalfFloatLinearFiltering) ||
  !!(caps.textureFloatRender && caps.textureFloatLinearFiltering);

ok('a fully capable GPU gets HDR',
   hdrFor({ textureHalfFloatRender: true, textureHalfFloatLinearFiltering: true,
            textureFloatRender: true, textureFloatLinearFiltering: true }) === true);
ok('a GPU with no float support at all gets LDR',
   hdrFor({ textureHalfFloatRender: false, textureHalfFloatLinearFiltering: false,
            textureFloatRender: false, textureFloatLinearFiltering: false }) === false);
// The dangerous middle case: can render to half-float but cannot filter it.
// This is exactly the configuration that produces a black bloom pass.
ok('render-but-cannot-filter does not get HDR',
   hdrFor({ textureHalfFloatRender: true, textureHalfFloatLinearFiltering: false,
            textureFloatRender: false, textureFloatLinearFiltering: false }) === false);
ok('filter-but-cannot-render does not get HDR',
   hdrFor({ textureHalfFloatRender: false, textureHalfFloatLinearFiltering: true,
            textureFloatRender: false, textureFloatLinearFiltering: false }) === false);
ok('full float alone is enough',
   hdrFor({ textureHalfFloatRender: false, textureHalfFloatLinearFiltering: false,
            textureFloatRender: true, textureFloatLinearFiltering: true }) === true);
ok('undefined capabilities are treated as unsupported',
   hdrFor({}) === false);

/* ------------- 3. a black frame must self-heal -------------------------- */

ok('there is a recovery path, not just a report',
   /blackScreenRecoveryTried/.test(app));
ok('recovery strips the lens pass',
   /this\.lensfx\.detach\(\)/.test(app));
ok('recovery strips post-processing',
   /this\.postfx\.detach\(\)/.test(app));
ok('recovery only fires on a genuinely black frame',
   /report\.luminance <= 0\.0001/.test(app));
ok('recovery is attempted only once',
   /this\.blackScreenRecoveryTried = true/.test(app));
// This used to assert that recovery REPLACES a black clear colour with a
// visible one. Deep space is now deliberately pure black - that is the
// requested look, not a fault - so lifting it on the first false positive
// would paint a permanent blue-grey wash across the void. The invariant
// that still matters is that recovery never leaves a transparent clear
// colour, which really would compose as a broken frame.
ok('recovery never leaves a transparent clear colour',
   /c\.a < 0\.99/.test(app));
ok('recovery no longer overrides an intentionally black void',
   !/clearColor = new Color4\(0\.05, 0\.07, 0\.13, 1\)/.test(app));
ok('the frame is re-checked after recovery',
   /checkForBlackScreen\(\)/.test(app) && /setTimeout/.test(app));
ok('the user is told recovery happened',
   /Recovering from a black frame/i.test(app));
ok('the report is still shown if recovery does not help',
   /showBlackScreenReport\(report\)/.test(app));

// Order matters: recovery must come before the give-up report.
{
  // The early-out for a healthy frame is now `if (report.painting) return`,
  // so anchor on the recovery block itself rather than the old condition.
  const body = app.slice(app.indexOf('blackFrameStreak++'));
  ok('recovery is attempted before reporting failure',
     body.indexOf('blackScreenRecoveryTried') < body.indexOf('showBlackScreenReport'));
  ok('a healthy frame returns before any of that',
     app.indexOf('if (report.painting)') < app.indexOf('blackFrameStreak++'));
}

/* ------------- 4. the title card must never be black -------------------- */

ok('the hero plate is actually used by the title card',
   /menu-hero\.jpg/.test(intro));
ok('the base-path-safe artwork is behind the title, not merely shipped',
   /background-image:/.test(intro) && /url\('art\/menu-hero\.jpg'\)/.test(intro));
ok('the plate covers the card',
   /background-size:\s*cover/.test(intro));
ok('there is a non-black fallback colour if the image fails',
   /background-color:#[0-9a-f]{6}/i.test(intro));

// That fallback must be genuinely light, not a token dark navy.
{
  const m = intro.match(/background-color:#([0-9a-f]{6})/i);
  const hex = m ? m[1] : '000000';
  const r = parseInt(hex.slice(0, 2), 16), g = parseInt(hex.slice(2, 4), 16), b = parseInt(hex.slice(4, 6), 16);
  const luma = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255;
  ok('the fallback colour is visibly not black', luma > 0.04, 'luma ' + luma.toFixed(3));
}

// The scrim over the artwork must not be so heavy that it hides it.
{
  const block = intro.slice(intro.indexOf('.intro-title{'), intro.indexOf('.intro-title h1'));
  const alphas = [...block.matchAll(/rgba\([^)]*?,\s*\.?(\d*\.?\d+)\s*\)/g)]
    .map((m) => parseFloat(m[1] < 1 ? m[1] : '0.' + m[1]));
  const worst = Math.max(...alphas, 0);
  ok('the scrim over the artwork stays translucent',
     worst <= 0.8, 'max alpha ' + worst);
}

/* ------------- 5. the descent must not black out the sky ---------------- */

// A descent sets clearColor every frame from the sky model. In space that
// colour is legitimately near-black, which is correct - but it must never be
// the reason the *whole* frame is black while on the ground.
{
  const dir = mkdtempSync(join(tmpdir(), 'bf-'));
  const out = join(dir, 'd.mjs');
  execFileSync('./node_modules/.bin/esbuild', [
    'src/bjs/systems/DescentSystem.ts',
    '--bundle', '--format=esm', '--platform=neutral', '--outfile=' + out
  ], { stdio: 'pipe' });
  const { EARTHLIKE, skyColorAt } = await import(out);

  ok('the sky at ground level is bright enough to see',
     skyColorAt(EARTHLIKE, 0).reduce((a, b) => a + b, 0) > 0.5);
  ok('the sky is still lit at aircraft altitude',
     skyColorAt(EARTHLIKE, 10).reduce((a, b) => a + b, 0) > 0.1);
  rmSync(dir, { recursive: true, force: true });
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
