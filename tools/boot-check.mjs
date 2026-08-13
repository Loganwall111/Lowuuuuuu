/**
 * Boot verification — runs the real App.init() against a mocked WebGL2
 * context to surface any exception that would leave the boot overlay up
 * (which reads to the user as "black screen + dead buttons").
 */
import { JSDOM } from 'jsdom';
import { build } from 'esbuild';
import fs from 'fs';

const dom = new JSDOM(
  '<!DOCTYPE html><html><head></head><body><canvas id="renderCanvas"></canvas></body></html>',
  { pretendToBeVisual: true, url: 'http://localhost:8080/' }
);

global.window = dom.window;
global.document = dom.window.document;
global.HTMLElement = dom.window.HTMLElement;
// Babylon loads textures through XHR; jsdom provides one, expose it globally
global.XMLHttpRequest = dom.window.XMLHttpRequest;
global.Image = dom.window.Image;
global.Blob = dom.window.Blob;
global.URL = dom.window.URL;
Object.defineProperty(global, 'navigator', {
  value: dom.window.navigator, configurable: true, writable: true
});
// Use Node's performance, not jsdom's (jsdom's recurses under Babylon).
const _t0 = Date.now();
global.performance = { now: () => Date.now() - _t0 };
global.requestAnimationFrame = (cb) => setTimeout(() => cb(Date.now()), 16);
global.cancelAnimationFrame = (id) => clearTimeout(id);
dom.window.Element.prototype.setPointerCapture = function () {};
dom.window.Element.prototype.releasePointerCapture = function () {};

/* ---- a WebGL2 context mock broad enough for Babylon's engine boot ---- */
function makeGL() {
  const obj = () => ({});
  const gl = new Proxy({}, {
    get(t, prop) {
      if (prop in t) return t[prop];
      // constants Babylon reads
      if (typeof prop === 'string' && /^[A-Z0-9_]+$/.test(prop)) return 1;
      return (...args) => {
        const p = String(prop);
        if (p === 'getExtension') return null;
        if (p === 'getParameter') return 8;
        if (p === 'getShaderPrecisionFormat') return { precision: 23, rangeMin: 127, rangeMax: 127 };
        if (p === 'getSupportedExtensions') return [];
        if (p.startsWith('create')) return obj();
        if (p === 'getProgramParameter' || p === 'getShaderParameter') return true;
        if (p === 'getProgramInfoLog' || p === 'getShaderInfoLog') return '';
        if (p === 'getUniformLocation') return obj();
        if (p === 'getAttribLocation') return 0;
        if (p === 'checkFramebufferStatus') return 36053; // COMPLETE
        return null;
      };
    }
  });
  return gl;
}
global.WebGL2RenderingContext = function () {};
global.WebGLRenderingContext = function () {};
dom.window.WebGL2RenderingContext = global.WebGL2RenderingContext;

dom.window.HTMLCanvasElement.prototype.getContext = function (type) {
  if (String(type).startsWith('webgl')) return makeGL();
  return {
    clearRect(){}, beginPath(){}, moveTo(){}, lineTo(){}, stroke(){}, fill(){}, arc(){},
    fillRect(){}, putImageData(){}, drawImage(){},
    getImageData: () => ({ data: new Uint8ClampedArray(4) }),
    createImageData: () => ({ data: new Uint8ClampedArray(4) }),
    createLinearGradient: () => ({ addColorStop(){} }),
    createRadialGradient: () => ({ addColorStop(){} }),
    measureText: () => ({ width: 10 }),
    set fillStyle(v){}, set strokeStyle(v){}, set lineWidth(v){}, set font(v){}
  };
};

const errors = [];
const origError = console.error;
console.error = (...a) => { errors.push(a.map(String).join(' ')); };

const out = await build({
  entryPoints: ['src/bjs/App.ts'], bundle: true, format: 'esm',
  write: false, logLevel: 'error', platform: 'browser'
});
const f = `/tmp/app-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, origError('  PASS  ' + n))
    : (fail++, origError('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

let thrown = null;
let appRef = null;
try {
  const { App } = await import(f);
  const app = new App();
  appRef = app;
  await app.init();
} catch (e) {
  thrown = e;
}

// init() dismisses the overlay on a short timer; let it elapse.
await new Promise((r) => setTimeout(r, 900));

console.error = origError;

console.log('\n=== App.init() ===');
if (thrown) {
  console.log('  init threw:', thrown && thrown.message ? thrown.message : String(thrown));
  console.log('  stack:', String(thrown && thrown.stack).split('\n').slice(1, 6).join('\n'));
}
ok('App.init() completes without throwing', !thrown);

const boot = document.querySelector('.boot');
console.log('\n=== boot overlay state ===');
console.log('  overlay present :', !!boot);
console.log('  has .gone class :', boot ? boot.classList.contains('gone') : 'n/a');
console.log('  message         :', document.getElementById('bootMsg')?.textContent);

ok('boot overlay is dismissed after init (not covering the app)',
   !boot || boot.classList.contains('gone'));

// ---- main menu ----
const menu = document.querySelector('.menu-root');
console.log('\n=== main menu ===');
console.log('  menu present    :', !!menu);
ok('main menu is shown after boot', !!menu);
if (menu) {
  const cards = menu.querySelectorAll('.menu-card');
  ok(`menu offers world cards (${cards.length})`, cards.length >= 4);
  ok('menu has a quick-start button', !!menu.querySelector('#mQuick'));
  // clicking must dismiss it and never leave a blocking layer
  menu.querySelector('#mQuick').dispatchEvent(
    new dom.window.MouseEvent('click', { bubbles: true }));
  await new Promise((r) => setTimeout(r, 900));
  const still = document.querySelector('.menu-root');
  ok('menu is removed after choosing (cannot block the sim)', !still);
}

// ---- nothing opaque is left covering the canvas ----
const blockers = [...document.body.children].filter((el) => {
  const s2 = dom.window.getComputedStyle(el);
  return s2.position === 'fixed' && s2.pointerEvents !== 'none' &&
         el.id !== 'renderCanvas' && !el.classList.contains('wm-layer') &&
         !el.classList.contains('topbar') && !el.classList.contains('hud') &&
         !el.classList.contains('wm-dock');
});
console.log('  full-screen blockers:', blockers.map((b) => b.className || b.id));
ok('no leftover element is covering the canvas', blockers.length === 0,
   blockers.map((b) => b.className || b.id).join(', '));

// ---- every world must load without throwing and without blanking the UI ----
console.log('\n=== world loading ===');
if (appRef) {
  const worlds = ['sandbox', 'planetary', 'ocean', 'terraform', 'blackhole', 'dimension'];
  for (const id of worlds) {
    let werr = null;
    try {
      await appRef.loadWorld(id);
      await new Promise((r) => setTimeout(r, 120));
    } catch (e) {
      werr = e;
    }
    ok(`world "${id}" loads without throwing`, !werr,
       werr ? String(werr && werr.message ? werr.message : werr) : '');

    if (!werr) {
      const w = appRef.world;
      ok(`world "${id}" reports a name`, !!(w && w.name), String(w && w.name));
      // a world must not leave a blocking overlay behind
      const b2 = document.querySelector('.boot');
      ok(`world "${id}" leaves no boot overlay covering the view`,
         !b2 || b2.classList.contains('gone'));
      // its UI surface must be describable without throwing
      let uiErr = null;
      try {
        w.getParams();
        w.getStats();
        if (w.getActions) w.getActions();
      } catch (e) { uiErr = e; }
      ok(`world "${id}" exposes params, stats and actions safely`, !uiErr,
         uiErr ? String(uiErr.message) : '');
      // one simulation step must not throw
      let stepErr = null;
      try { w.update(1 / 60, appRef.ctx); } catch (e) { stepErr = e; }
      ok(`world "${id}" survives a simulation step`, !stepErr,
         stepErr ? String(stepErr.message) : '');
    }
  }

  // ---- every registered action must be safe to click ----
  console.log('\n=== actions ===');
  try {
    await appRef.loadWorld('sandbox');
    await new Promise((r) => setTimeout(r, 120));
    const w = appRef.world;
    const actions = w.getActions ? w.getActions() : [];
    ok(`sandbox exposes actions (${actions.length})`, actions.length > 0);
    const broken = [];
    for (const a of actions) {
      try {
        w.runAction(a.key, appRef.ctx);
        w.update(1 / 60, appRef.ctx);
      } catch (e) {
        broken.push(a.key + ': ' + (e && e.message ? e.message : e));
      }
    }
    ok('every action runs without throwing', broken.length === 0, broken.join(' | '));

    // dimension travel rebuilds the entire world, so sweep it too
    await appRef.loadWorld('dimension');
    await new Promise((r) => setTimeout(r, 120));
    const dw = appRef.world;
    const dActions = dw.getActions ? dw.getActions() : [];
    const dBroken = [];
    for (const a of dActions) {
      try {
        dw.runAction(a.key, appRef.ctx);
        dw.update(1 / 60, appRef.ctx);
        dw.getStats();
      } catch (e) {
        dBroken.push(a.key + ': ' + (e && e.message ? e.message : e));
      }
    }
    ok(`every dimension action runs without throwing (${dActions.length})`,
       dBroken.length === 0, dBroken.join(' | '));

    // repeated deep travel must not leak or throw
    let travelErr = null;
    try {
      for (let i = 0; i < 25; i++) dw.runAction('deeper', appRef.ctx);
      for (let i = 0; i < 10; i++) dw.runAction('tear', appRef.ctx);
      dw.update(1 / 60, appRef.ctx);
    } catch (e) { travelErr = e; }
    ok('35 consecutive dimension jumps stay stable', !travelErr,
       travelErr ? String(travelErr.message) : '');
  } catch (e) {
    ok('action sweep completed', false, String(e && e.message ? e.message : e));
  }
}

if (errors.length) {
  console.log('\n=== console.error output during boot ===');
  errors.slice(0, 12).forEach((e) => console.log('  ' + e.slice(0, 300)));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
