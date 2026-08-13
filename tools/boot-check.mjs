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
  // A 2D context stub that tolerates ANY drawing call. Listing methods by
  // hand meant every new bit of canvas art (save/restore/ellipse/filter...)
  // broke the harness with "c.<x> is not a function" - a fake failure that
  // says nothing about the product. Unknown methods become no-ops.
  const known = {
    getImageData: (x, y, w = 1, h = 1) => ({
      data: new Uint8ClampedArray(Math.max(1, w * h * 4)), width: w, height: h
    }),
    createImageData: (w = 1, h = 1) => ({
      data: new Uint8ClampedArray(Math.max(1, w * h * 4)), width: w, height: h
    }),
    createLinearGradient: () => ({ addColorStop(){} }),
    createRadialGradient: () => ({ addColorStop(){} }),
    createConicGradient: () => ({ addColorStop(){} }),
    createPattern: () => null,
    measureText: (t = '') => ({ width: String(t).length * 6 }),
    isPointInPath: () => false,
    getLineDash: () => []
  };
  return new Proxy(known, {
    get(target, prop) {
      if (prop in target) return target[prop];
      // Any other property read is either a settable style field or a
      // drawing command; hand back a harmless no-op function.
      return () => {};
    },
    set() { return true; }
  });
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
const Vector3Distance = (a, b) =>
  Math.hypot(a.x - b.x, a.y - b.y, a.z - b.z);
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

// ---- the opening sequence (there is no main menu any more) ----
const intro = document.querySelector('.intro-root');
console.log('\n=== opening sequence ===');
console.log('  intro present   :', !!intro);
ok('the opening is shown after boot', !!intro);
ok('the old main menu is gone', !document.querySelector('.menu-root'));
if (intro) {
  const title = intro.querySelector('.intro-title');
  ok('a title card is shown', !!title);
  const play = intro.querySelector('.intro-play');
  ok('there is a Play button', !!play);
  ok('the intro can always be skipped', !!intro.querySelector('.intro-skip'));

  // Clicking Play must put you in the world and stop blocking the sim.
  play.dispatchEvent(new dom.window.MouseEvent('click', { bubbles: true }));
  await new Promise((r) => setTimeout(r, 900));
  const titleAfter = document.querySelector('.intro-title');
  ok('Play dismisses the title card (cannot block the sim)',
     !titleAfter || titleAfter.classList.contains('intro-hide'));

  // Skipping must remove the overlay entirely, leaving nothing over the canvas.
  if (appRef?.intro) {
    appRef.intro.skip();
    appRef.introUI?.dispose?.();
    await new Promise((r) => setTimeout(r, 400));
  }
  ok('skipping removes the overlay completely',
     !document.querySelector('.intro-root'));
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
console.log('\n=== the render loop must actually run ===');
// This is the black-screen test. Everything above can pass while the canvas
// stays black, because a throw *inside* the render loop kills the frame
// without touching the DOM. Babylon's tree-shaken build is full of
// side-effect imports (Ray in particular) whose absence only ever shows up
// here. So: capture the real frame callback and run it.
if (appRef) {
  const eng = appRef.engine;
  let frameFn = null;
  const realRun = eng.runRenderLoop.bind(eng);
  eng.runRenderLoop = (fn) => { frameFn = fn; };
  try { appRef.start(); } catch (e) { /* reported below */ }
  eng.runRenderLoop = realRun;

  ok('the app registers a render loop', typeof frameFn === 'function');

  if (frameFn) {
    let firstErr = null, ran = 0;
    for (let i = 0; i < 120; i++) {
      try { frameFn(); ran++; }
      catch (e) { firstErr = String(e && e.stack ? e.stack : e); break; }
    }
    ok('the first frame renders without throwing (black-screen guard)',
       ran > 0, firstErr || '');
    ok(`120 frames run without throwing (${ran})`, ran === 120, firstErr || '');

    // The scene must have something in it and must not be clearing to black
    // by accident - a black clear colour with no meshes is the other way
    // this fails.
    const sc = appRef.scene;
    ok('the scene has geometry to draw', sc.meshes.length > 0);
    const cc = sc.clearColor;
    ok('the scene is not clearing to pure black with nothing drawn',
       sc.meshes.some((m) => m.isVisible) || (cc.r + cc.g + cc.b) > 0.01);

    // Every world must survive frames too, since each rebuilds the
    // post-process chain on load.
    for (const id of ['garage', 'ship', 'planetary', 'blackhole']) {
      let e2 = null;
      try {
        await appRef.loadWorld(id);
        for (let i = 0; i < 20; i++) frameFn();
      } catch (e) { e2 = String(e).slice(0, 90); }
      ok(`world "${id}" renders frames without throwing`, !e2, e2 || '');
    }
  }
}

console.log('\n=== world loading ===');
if (appRef) {
  const worlds = ['sandbox', 'planetary', 'ocean', 'terraform', 'blackhole', 'dimension',
                  'garage', 'ship'];
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

      // EVERY action in EVERY world must be safe to click
      const acts = w.getActions ? w.getActions() : [];
      const bad = [];
      for (const a of acts) {
        try {
          w.runAction(a.key, appRef.ctx);
          w.update(1 / 60, appRef.ctx);
          w.getStats();
        } catch (e) {
          bad.push(a.key + ': ' + (e && e.message ? e.message : e));
        }
      }
      ok(`world "${id}": all ${acts.length} actions run safely`, bad.length === 0,
         bad.slice(0, 3).join(' | '));

      // and every parameter must accept its full documented range
      const pErr = [];
      for (const prm of (w.getParams ? w.getParams() : [])) {
        for (const v of [prm.min, prm.max, (prm.min + prm.max) / 2]) {
          try {
            w.setParam(prm.key, v);
            w.update(1 / 60, appRef.ctx);
          } catch (e) {
            pErr.push(prm.key + '=' + v + ': ' + (e && e.message ? e.message : e));
          }
        }
      }
      ok(`world "${id}": every parameter accepts its full range`, pErr.length === 0,
         pErr.slice(0, 3).join(' | '));
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

    // ---- portals and space tears, end to end through the real app ----
    await appRef.loadWorld('sandbox');
    await new Promise((r) => setTimeout(r, 120));
    {
      const sw = appRef.world;
      let perr = null;
      try {
        sw.runAction('portal:wormhole', appRef.ctx);
        sw.runAction('portal:tear', appRef.ctx);
        for (let i = 0; i < 200; i++) sw.update(1 / 60, appRef.ctx);
      } catch (e) { perr = e; }
      ok('opening a wormhole and a tear in the live app is safe', !perr,
         perr ? String(perr.message) : '');
      const st = sw.getStats();
      ok('the sandbox reports open portals',
         Number(st['Portals open']) >= 2, JSON.stringify(st['Portals open']));
      ok('wormholes and tears are counted separately',
         Number(st['Wormholes']) >= 1 && Number(st['Space tears']) >= 1);

      // a tear must be able to actually take the player somewhere
      let travelled = null;
      const realEnter = appRef.ctx.enterDimension;
      appRef.ctx.enterDimension = (seed, depth) => { travelled = { seed, depth }; };
      // fly the camera into the tear
      const tear = sw.portals ? null : null;
      try {
        // drive many frames with the camera moved onto each portal mouth
        const sys = sw['portals'] ?? null;
        if (sys) {
          for (const prt of sys.list()) {
            if (prt.kind !== 'tear') continue;
            prt.openness = 1;
            appRef.camera.position.copyFrom(prt.a.position);
            sw.update(1 / 60, appRef.ctx);
            sw.update(1 / 60, appRef.ctx);
          }
        }
      } catch (e) { /* reported below */ }
      ok('flying into a space tear triggers travel to its dimension',
         !!travelled && Number.isFinite(travelled.seed), JSON.stringify(travelled));
      appRef.ctx.enterDimension = realEnter;

      sw.runAction('portal:close', appRef.ctx);
      ok('Close All Portals really closes them',
         Number(sw.getStats()['Portals open']) === 0);
    }

    // ---- one continuous universe, in the live app ----
    {
      const u = appRef.universe;
      ok('the app owns a populated universe', u && u.regions.length > 20);
      const kinds = new Set(u.regions.map((r) => r.kind));
      ok('every kind of place coexists in one universe',
         ['star-system', 'planet', 'blackhole'].every((k) => kinds.has(k)),
         [...kinds].join(','));

      // Flying somewhere loads what that place IS. This replaced the World
      // Library: an ocean world is a destination, not a menu entry. The
      // universe itself is still one continuous space - the regions below
      // are unchanged by travelling, which is the property that matters.
      const regionsBefore = u.regions.length;
      const target = u.regions.find((r) => r.kind === 'blackhole');
      appRef.warpTo(target.id);
      // loadWorld is async; give it a moment to swap in.
      await new Promise((r) => setTimeout(r, 600));
      ok('flying to a black hole loads the black hole world',
         appRef.currentId === 'blackhole', String(appRef.currentId));
      ok('travelling never destroys the universe around you',
         u.regions.length === regionsBefore);
      ok('flying to a place moves the camera near it',
         Vector3Distance(appRef.camera.position, target.position) <
           Math.max(target.radius * 3, 100),
         String(Vector3Distance(appRef.camera.position, target.position)));

      // grabbing and moving a black hole
      const before = target.position.clone();
      appRef.grab.grabAt(
        { id: target.id, name: target.name, position: target.position, radius: target.radius },
        appRef.camera.position);
      ok('a black hole can be grabbed', appRef.grab.isHolding());
      // use the real Vector3 class rather than a hand-rolled stub
      const V3 = appRef.camera.position.constructor;
      appRef.grab.update(1 / 60, appRef.camera.position, new V3(1, 0, 0));
      ok('moving a grabbed black hole is safe',
         [target.position.x, target.position.y, target.position.z].every(Number.isFinite));
      appRef.grab.release();
      ok('it can be released', !appRef.grab.isHolding());
      target.position.copyFrom(before);

      // crossing a horizon must set up the look-back view
      const hr = u.horizonRadiusOf(target);
      const V3u = target.position.constructor;
      u.updatePlayer(target.position.add(new V3u(hr * 5, 0, 0)));
      ok('outside the horizon nothing is flagged', u.insideHorizon === null);
      u.updatePlayer(target.position.add(new V3u(hr * 0.4, 0, 0)));
      ok('crossing the horizon is detected in the live app', u.insideHorizon !== null);
      ok('the fall depth is reported', u.horizonDepth > 0);
      ok('the stats say you are inside',
         u.stats()['Inside horizon'].includes(target.name));
      u.updatePlayer(target.position.add(new V3u(hr * 9, 0, 0)));
      ok('you can climb back out', u.insideHorizon === null);
    }

    // ---- the black hole renderer accepts an interior view and any lens ----
    {
      await appRef.loadWorld('blackhole');
      await new Promise((r) => setTimeout(r, 120));
      const bw = appRef.world;
      ok('the black hole world exposes the interior view',
         typeof bw.setInterior === 'function');
      let ierr = null;
      try {
        for (const d of [0, 0.5, 1, -5, 99, NaN]) {
          const V3b = appRef.camera.position.constructor;
          bw.setInterior(d, new V3b(0, 0, -1));
          bw.update(1 / 60, appRef.ctx);
        }
      } catch (e) { ierr = e; }
      ok('every interior depth including NaN is safe', !ierr, ierr ? ierr.message : '');

      // every lens type must render without throwing
      const lensActions = bw.getActions().filter((a) => a.key.startsWith('lens:'));
      ok(`every lens type is offered as an action (${lensActions.length})`,
         lensActions.length >= 11);
      const lensErr = [];
      for (const a of lensActions) {
        try {
          bw.runAction(a.key, appRef.ctx);
          bw.update(1 / 60, appRef.ctx);
          bw.getStats();
        } catch (e) { lensErr.push(a.key + ': ' + e.message); }
      }
      ok('every lens type renders without throwing', lensErr.length === 0,
         lensErr.slice(0, 3).join(' | '));
    }

    // entering a dimension by seed must land in exactly that dimension
    {
      await appRef.enterDimension(123456, 3);
      await new Promise((r) => setTimeout(r, 120));
      ok('enterDimension loads the dimension world', appRef.world.id === 'dimension');
      const spec = appRef.world.currentSpec();
      ok('it lands on the requested seed', spec.seed === 123456, String(spec.seed));
      ok('it lands at the requested depth', spec.depth === 3, String(spec.depth));
    }

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
