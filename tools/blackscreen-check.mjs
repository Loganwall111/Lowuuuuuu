/**
 * blackscreen-check — the failure mode that keeps shipping.
 *
 * A black screen passes every other test in this repo: init() resolves, the
 * DOM is right, the scene has meshes. The failure lives in WebGL and in CSS
 * opacity, neither of which jsdom executes. These assertions attack the
 * causes directly instead.
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

console.log('— the render loop must survive a throwing subsystem —');
{
  const app = fs.readFileSync('src/bjs/App.ts', 'utf8');
  ok('the frame is wrapped in a try/catch',
     /runRenderLoop\(\(\) => \{[\s\S]{0,400}?try \{/.test(app));
  ok('a failed frame still tries to render something',
     /catch[\s\S]{0,300}?this\.scene\.render\(\)/.test(app));
  ok('frame errors are counted', app.includes('frameErrors'));
  ok('the first error is reported once, not 60x a second',
     app.includes('this.frameErrors === 1'));
  ok('a persistently failing effect gets switched off',
     app.includes('this.lensfx.detach()'));
}

console.log('— Babylon side-effect imports that cause black screens —');
{
  const eng = fs.readFileSync('src/bjs/Engine.ts', 'utf8');
  // Ray is the one that bit us: getForwardRay/scene.pick throw without it.
  ok('Ray is imported for its side effect',
     eng.includes("import '@babylonjs/core/Culling/ray'"));
  ok('standardMaterial is imported',
     eng.includes("@babylonjs/core/Materials/standardMaterial"));

  // Nothing may call getForwardRay in a per-frame path without that import.
  const lens = fs.readFileSync('src/bjs/systems/LensFX.ts', 'utf8');
  // Only the *call* matters; the comment explaining why we avoid it is fine.
  ok('LensFX does not construct a Ray every frame',
     !/(?<!\/\/[^\n]*)camera\.getForwardRay\s*\??\.\s*\(/.test(
       lens.split('\n').filter((l) => !l.trim().startsWith('*') &&
                                       !l.trim().startsWith('//')).join('\n')));
  ok('LensFX derives forward from the view matrix instead',
     lens.includes('vm[2]') && lens.includes('vm[10]'));
}

console.log('— post-processing must never black out the frame —');
{
  const { DEFAULT_POSTFX, POSTFX_PARAMS } =
    await load('src/bjs/PostFX.ts', 'pfx');
  const src = fs.readFileSync('src/bjs/PostFX.ts', 'utf8');

  ok('the vignette weight is bounded', src.includes('Math.min(1.5'));
  ok('the vignette cannot be driven to black',
     DEFAULT_POSTFX.vignette <= 0.5, String(DEFAULT_POSTFX.vignette));
  ok('exposure is never zero', DEFAULT_POSTFX.exposure > 0.3);
  ok('contrast is sane', DEFAULT_POSTFX.contrast > 0.5 && DEFAULT_POSTFX.contrast < 2);

  // Every slider's minimum must still leave a visible picture: an exposure
  // slider that can reach 0 is a black screen with extra steps.
  const expo = POSTFX_PARAMS.find((p) => p.key === 'exposure');
  ok('exposure cannot be dragged to zero', expo && expo.min > 0,
     expo ? String(expo.min) : 'missing');

  // Each stage is individually guarded so one driver quirk cannot kill all.
  ok('each post-process stage is guarded independently',
     (src.match(/guard\(/g) || []).length >= 6);
  ok('a failed pipeline falls back to no post-processing',
     src.includes('Post-processing unavailable'));
}

console.log('— no full-screen overlay may be an opaque black curtain —');
{
  const intro = fs.readFileSync('src/bjs/ui/IntroOverlay.ts', 'utf8');
  // Pull every rgba(...) alpha used on a full-screen element.
  // Only full-screen layers can black out the view. A small opaque dialogue
  // box is fine; an inset:0 element at 86% black is not.
  const titleBlock = intro.slice(intro.indexOf('.intro-title{'),
                                 intro.indexOf('.intro-title h1'));
  const alphas = [...titleBlock.matchAll(
    /rgba\(\s*\d+\s*,\s*\d+\s*,\s*\d+\s*,\s*([\d.]+)\s*\)/g)]
    .map((m) => parseFloat(m[1]));
  const opaque = alphas.filter((a) => a > 0.7);
  ok(`the title card is a vignette, not a curtain (max alpha ${Math.max(...alphas)})`,
     opaque.length === 0, opaque.join(','));

  // And it must genuinely cover the whole screen, so this is the one that
  // matters most.
  ok('the title card is full-screen, hence must stay translucent',
     /\.intro-title\{[\s\S]*?inset:0/.test(intro));
  ok('the sim stays visible behind the title',
     intro.includes('.intro-title{') && !intro.includes('rgba(4,7,16,.86)'));

  const styles = fs.readFileSync('src/bjs/ui/styles.ts', 'utf8');
  ok('the boot overlay is removed, not just faded',
     fs.readFileSync('src/bjs/ui/Shell.ts', 'utf8').includes('el.remove()'));
  ok('the canvas sits behind the UI, not under an opaque layer',
     styles.includes('#renderCanvas{position:fixed;inset:0') &&
     styles.includes('z-index:0'));
}

console.log('— the watchdog that explains a black screen —');
{
  const { inspectFrame } = await load('src/bjs/RenderWatchdog.ts', 'wd');

  const base = {
    meshCount: () => 50, frameErrors: () => 0, firstError: () => 'none', fps: () => 60
  };
  const fakeCanvas = (w, h) => ({
    width: w, height: h,
    getBoundingClientRect: () => ({ width: w, height: h })
  });

  // No context at all.
  let r = inspectFrame({ ...base, canvas: fakeCanvas(800, 600), gl: null });
  ok('it detects a missing WebGL context', !r.painting && /WebGL context/.test(r.diagnosis));

  // Zero-sized canvas: a very common cause and easy to misread as a GPU bug.
  r = inspectFrame({ ...base, canvas: fakeCanvas(0, 0), gl: {} });
  ok('it detects a zero-sized canvas', !r.painting && /zero size/.test(r.diagnosis));

  // A lost context.
  r = inspectFrame({
    ...base, canvas: fakeCanvas(800, 600),
    gl: { isContextLost: () => true }
  });
  ok('it detects a lost context', !r.painting && /context was lost/.test(r.diagnosis));

  // A real read that comes back all zeros = genuinely black.
  const blackGl = {
    RGBA: 1, UNSIGNED_BYTE: 2, isContextLost: () => false,
    readPixels: (x, y, w2, h2, f, t, out) => { out[0] = out[1] = out[2] = 0; out[3] = 255; }
  };
  r = inspectFrame({ ...base, canvas: fakeCanvas(800, 600), gl: blackGl });
  ok('it detects an all-black frame', !r.painting);
  ok('and explains it when frames are throwing',
     /throwing/.test(inspectFrame({
       ...base, frameErrors: () => 5, firstError: () => 'Ray needed',
       canvas: fakeCanvas(800, 600), gl: blackGl
     }).diagnosis));
  ok('and explains an empty scene',
     /no meshes/.test(inspectFrame({
       ...base, meshCount: () => 0, canvas: fakeCanvas(800, 600), gl: blackGl
     }).diagnosis));

  // A lit frame must read as fine.
  const litGl = {
    RGBA: 1, UNSIGNED_BYTE: 2, isContextLost: () => false,
    readPixels: (x, y, w2, h2, f, t, out) => { out[0] = 240; out[1] = 242; out[2] = 247; out[3] = 255; }
  };
  r = inspectFrame({ ...base, canvas: fakeCanvas(800, 600), gl: litGl });
  ok('a white garage reads as rendering normally', r.painting);
  ok('and it says so', /normally/.test(r.diagnosis));

  // Deep space is dark but not black - it must NOT be a false positive.
  const spaceGl = {
    RGBA: 1, UNSIGNED_BYTE: 2, isContextLost: () => false,
    readPixels: (x, y, w2, h2, f, t, out) => {
      // mostly near-black with occasional stars
      const star = ((x * 7 + y * 13) % 40) === 0;
      const v = star ? 200 : 3;
      out[0] = v; out[1] = v; out[2] = v + 2; out[3] = 255;
    }
  };
  r = inspectFrame({ ...base, canvas: fakeCanvas(800, 600), gl: spaceGl });
  ok('a starfield is not mistaken for a black screen', r.painting,
     'lum ' + r.luminance.toFixed(4));

  ok('it never throws on a broken gl object',
     (() => {
       try {
         inspectFrame({
           ...base, canvas: fakeCanvas(800, 600),
           gl: { isContextLost: () => false, readPixels: () => { throw new Error('x'); } }
         });
         return true;
       } catch { return false; }
     })());
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
