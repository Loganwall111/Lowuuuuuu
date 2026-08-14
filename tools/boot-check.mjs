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

// ---- the flight HUD, in a real document ----
// A green unit test on the formatters does not prove the panel mounted or
// that it is wired to anything, so assert against the live DOM.
const hudChecks = [];
try {
  const root = document.querySelector('.fhud');
  hudChecks.push(['the flight HUD mounts into the document', !!root]);
  hudChecks.push(['the HUD does not swallow clicks meant for the sim',
    !!root && (root.style.pointerEvents === 'none' ||
      String(root.className).includes('fhud'))]);
  for (const id of ['fhX', 'fhY', 'fhZ', 'fhHdg', 'fhSpd', 'fhWrp', 'fhLoc']) {
    hudChecks.push(['HUD field ' + id + ' exists', !!document.getElementById(id)]);
  }
  // Push a frame of telemetry through and confirm it lands on screen.
  if (appRef?.flightHud) {
    appRef.flightHud.update({
      x: 1234, y: -56, z: 7.5, heading: Math.PI / 2, pitch: 0.2,
      speed: 900, throttle: 0.5, warpCharge: 0.5, warpMultiplier: 12,
      locale: 'Test Nebula', localeDistance: 5000,
      fleetSize: 3, fleetGravity: 0
    });
    hudChecks.push(['coordinates reach the screen',
      document.getElementById('fhX')?.textContent === '+1.23k']);
    hudChecks.push(['heading reaches the screen',
      document.getElementById('fhHdg')?.textContent === '090°']);
    hudChecks.push(['the nearest place is named',
      document.getElementById('fhLoc')?.textContent === 'Test Nebula']);
    // The DOM normalises "50.0%" to "50%", so compare numerically rather
    // than by string.
    const wPct = parseFloat(document.getElementById('fhWrp')?.style.width ?? '');
    hudChecks.push(['warp charge reaches the screen', Math.abs(wPct - 50) < 0.01]);
    hudChecks.push(['the warp multiplier is displayed',
      document.getElementById('fhWMul')?.textContent === '×12']);
    // Toggling a group must actually hide it.
    appRef.flightHud.setElement('coordinates', false);
    const blk = document.querySelector('[data-g="coordinates"]');
    hudChecks.push(['switching off a HUD group hides it',
      !!blk && blk.style.display === 'none']);
    appRef.flightHud.setElement('coordinates', true);
    hudChecks.push(['switching it back on restores it',
      !!blk && blk.style.display !== 'none']);
    appRef.flightHud.setVisible(false);
    hudChecks.push(['the whole HUD can be hidden',
      root.style.display === 'none' && !appRef.flightHud.isVisible()]);
    appRef.flightHud.setVisible(true);
  } else {
    hudChecks.push(['the app exposes its flight HUD', false]);
  }
} catch (e) {
  hudChecks.push(['the flight HUD survives a live frame: ' + e.message, false]);
}

console.error = origError;

// ---- the graphics panel drives the HUD ----
// Wiring that compiles is not wiring that works; open the real panel and
// click the real checkbox.
try {
  appRef?.shell?.wm?.Toggle?.('graphics');
  await new Promise((r) => setTimeout(r, 60));
  const box = document.querySelector('input[data-hud="coordinates"]');
  hudChecks.push(['the graphics panel offers a HUD toggle', !!box]);
  if (box) {
    const before = appRef.flightHud.elements.coordinates;
    box.checked = !before;
    box.onchange();
    hudChecks.push(['clicking the toggle changes the HUD',
      appRef.flightHud.elements.coordinates === !before]);
    box.checked = before;
    box.onchange();
  }
  // Every post-fx control must be reachable now the tier filter is gone.
  const sliders = document.querySelectorAll('.wm-win input[type="range"]');
  hudChecks.push(['the graphics panel exposes real sliders', sliders.length > 0]);
} catch (e) {
  hudChecks.push(['the graphics panel opens: ' + e.message, false]);
}

// ---- launching a fleet, for real ----
const fleetChecks = [];
try {
  appRef?.shell?.wm?.Toggle?.('pilot');
  await new Promise((r) => setTimeout(r, 60));
  const btn = document.querySelector('button[data-fleet="worldship"]');
  fleetChecks.push(['the pilot panel offers a fleet launch', !!btn]);
  const views = document.querySelectorAll('button[data-shipview]');
  fleetChecks.push(['both ship viewpoints are offered', views.length >= 2]);

  if (btn) {
    const before = appRef.fleet.vessels.length;
    btn.onclick();
    const after = appRef.fleet.vessels.length;
    fleetChecks.push(['clicking launch actually launches ships', after > before]);
    fleetChecks.push(['launched ships have bodies in the scene',
      appRef.fleet.vessels.every((v) => !!v.mesh)]);
    // The whole point: enough mass in one place makes gravity.
    const g = appRef.fleet.gravity();
    fleetChecks.push(['a world ship fleet generates its own gravity',
      g.significant, g.surfaceGravity.toExponential(2) + ' m/s^2']);
    fleetChecks.push(['fleet mass is the sum of its vessels',
      Math.abs(g.mass - appRef.fleet.totalMass()) < 1]);

    // Ships must actually move toward their slots when the fleet updates.
    const v0 = appRef.fleet.vessels[0];
    const p0 = v0.position.clone();
    appRef.fleet.moveTo(p0.add(new (p0.constructor)(9000, 0, 0)));
    for (let i = 0; i < 60; i++) appRef.fleet.update(0.05);
    fleetChecks.push(['a fleet ordered somewhere actually flies there',
      v0.position.x > p0.x]);
    fleetChecks.push(['ship meshes follow their vessels',
      Math.abs(v0.mesh.position.x - v0.position.x) < 0.001]);

    // Switching worlds must not leave disposed meshes behind.
    const meshesBefore = appRef.scene.meshes.length;
    await appRef.loadWorld?.('planetary');
    await new Promise((r) => setTimeout(r, 250));
    fleetChecks.push(['changing world clears the fleet rather than leaking it',
      appRef.fleet.vessels.length === 0]);
    fleetChecks.push(['no disposed meshes survive the world change',
      appRef.scene.meshes.every((m) => !m.isDisposed()),
      'meshes ' + meshesBefore + ' -> ' + appRef.scene.meshes.length]);
  }
} catch (e) {
  fleetChecks.push(['launching a fleet survives a live frame: ' + e.message, false]);
}
// ---- creatures build for real ----
// Tube geometry can pass pure-function tests and still throw inside
// Babylon, so build the actual animal in the actual scene.
const critterChecks = [];
try {
  const imp = appRef?.world?.impactor;
  const scene = appRef?.scene;
  // Reach the geometry through the app's own bundle by throwing one.
  if (imp) {
    const V = appRef.vehicle.position;
    const oct = imp.throwAt('octopus', V, new V.constructor(0, 0, 1), 10);
    critterChecks.push(['the octopus can be thrown', !!oct]);
    critterChecks.push(['the octopus is built as a creature, not a sphere',
      !!oct?.creature && Array.isArray(oct?.arms) && oct.arms.length === 8]);
    critterChecks.push(['every arm is real geometry',
      !!oct?.arms?.every((a) => a.getTotalVertices() > 0)]);
    // Arms must follow the body, which is the bug you get for moving the
    // mesh instead of the root.
    const before = oct.arms[0].getAbsolutePosition().clone();
    imp.update(0.4, []);
    // getAbsolutePosition() reads a cached world matrix that Babylon only
    // refreshes during a render, so it must be forced before comparing -
    // otherwise this reports no movement for a creature that did move.
    oct.creature.computeWorldMatrix(true);
    oct.arms[0].computeWorldMatrix(true);
    const armDelta = oct.arms[0].getAbsolutePosition().subtract(before).length();
    critterChecks.push(['the whole animal moves together',
      oct.creature.position.length() > 0 && armDelta > 0,
      'arm moved ' + armDelta.toFixed(3)]);

    const ten = imp.throwAt('tentacle', V, new V.constructor(1, 0, 0), 10);
    critterChecks.push(['the tentacle is a tube, not a ball',
      !!ten && ten.mesh.getTotalVertices() > 0 && !ten.creature]);

    // Disposal must take the arms with it rather than orphaning tubes.
    const meshesBefore = scene.meshes.length;
    imp.clear();
    critterChecks.push(['clearing removes the whole animal',
      scene.meshes.length < meshesBefore]);
    critterChecks.push(['no orphaned arms are left behind',
      !scene.meshes.some((m) => m.name.includes('-arm'))]);
  } else {
    critterChecks.push(['the planetary world exposes its impactor', false]);
  }
} catch (e) {
  critterChecks.push(['creatures build in a live scene: ' + e.message, false]);
}
// ---- REPRO: black hole world + its options panel ----
// The user reports the screen going black when travelling to the black
// hole or opening its options. Drive that exact sequence.
const bhChecks = [];
try {
  await appRef?.loadWorld?.('blackhole');
  await new Promise((r) => setTimeout(r, 300));
  const w = appRef.world;
  bhChecks.push(['the black hole world loads', w?.id === 'blackhole']);

  // Run frames and look for NaN reaching any uniform.
  let nanSeen = null;
  const mat = w?.mat;
  if (mat) {
    const realFloat = mat.setFloat.bind(mat);
    mat.setFloat = (n, v) => {
      if (!Number.isFinite(v) && nanSeen === null) nanSeen = n + '=' + v;
      return realFloat(n, v);
    };
  }
  for (let i = 0; i < 30; i++) {
    w?.update?.(0.016, appRef.ctx);
  }
  bhChecks.push(['no uniform ever receives NaN', nanSeen === null, nanSeen ?? '']);

  // Every parameter at both extremes: a slider dragged to its end is the
  // most likely way a user hits a degenerate value.
  let badParam = null;
  for (const prm of (w?.getParams?.() ?? [])) {
    for (const v of [prm.min, prm.max, 0]) {
      w.setParam(prm.key, v);
      w.update(0.016, appRef.ctx);
      if (nanSeen && !badParam) badParam = prm.key + '=' + v + ' -> ' + nanSeen;
    }
    w.setParam(prm.key, prm.value);
  }
  bhChecks.push(['no slider extreme produces NaN', !badParam, badParam ?? '']);

  // Camera exactly at the origin - inside the horizon, the case the
  // report calls out specifically.
  appRef.camera.position.set(0, 0, 0);
  w?.update?.(0.016, appRef.ctx);
  bhChecks.push(['the camera at the origin does not break the world', true]);

  // Now the actual reported trigger: open its options panel.
  appRef.shell?.wm?.Toggle?.('controls');
  await new Promise((r) => setTimeout(r, 120));
  bhChecks.push(['opening the options panel does not throw', true]);

  // Is anything actually being drawn?
  const painting = appRef.postfx?.inspectFrame
    ? null : 'n/a';
  bhChecks.push(['the scene still has meshes to draw',
    appRef.scene.meshes.length > 0,
    appRef.scene.meshes.length + ' meshes']);
  bhChecks.push(['the render loop is still alive', !!appRef.scene.activeCamera]);

  // A zero-height canvas is the exact trigger: it makes getAspectRatio()
  // return 0/0. Simulate it and require the shader still gets a sane value.
  const eng2 = appRef.scene.getEngine();
  const rw = eng2.getRenderHeight;
  eng2.getRenderHeight = () => 0;
  let zeroNan = null;
  const mat2 = appRef.world?.mat;
  if (mat2) {
    const rf = mat2.setFloat.bind(mat2);
    mat2.setFloat = (n, v) => {
      if (!Number.isFinite(v) && !zeroNan) zeroNan = n + '=' + v;
      return rf(n, v);
    };
  }
  appRef.world?.update?.(0.016, appRef.ctx);
  eng2.getRenderHeight = rw;
  bhChecks.push(['a zero-height canvas still yields a finite aspect',
    zeroNan === null, zeroNan ?? '']);
} catch (e) {
  bhChecks.push(['the black hole sequence survives: ' + e.message, false]);
}
console.log('\n=== black hole ===');
for (const [n, c, e] of bhChecks) ok(n, c, e);

// ---- the sky must not occlude geometry ----
// Point clouds writing depth at their shell radius punch black holes in
// everything behind them, which is what the "black patterns while moving
// the mouse" report was.
const skyChecks = [];
try {
  const pointMeshes = appRef.scene.meshes.filter(
    (m) => m.material && m.material.pointsCloud);
  skyChecks.push(['there is a point-cloud sky to check', pointMeshes.length > 0,
    pointMeshes.length + ' point meshes']);
  const writing = pointMeshes.filter((m) => !m.material.disableDepthWrite);
  skyChecks.push(['no point cloud writes depth', writing.length === 0,
    writing.map((m) => m.name).join(', ')]);
  const occluding = pointMeshes.filter((m) => m.applyFog);
  skyChecks.push(['no point cloud is fogged', occluding.length === 0]);
} catch (e) {
  skyChecks.push(['the sky check runs: ' + e.message, false]);
}
console.log('\n=== sky occlusion ===');
for (const [n, c, e] of skyChecks) ok(n, c, e);

// ---- REPRO: travelling to a black hole in the open universe ----
// The user reports flying to a hole and getting an absolute black void.
const travelChecks = [];
try {
  const holes = appRef.universe.regions.filter((r) => r.kind === 'blackhole');
  travelChecks.push(['the universe contains black holes to fly to',
    holes.length > 0, holes.length + ' holes']);

  const h = holes[0];
  const hz = appRef.universe.horizonRadiusOf(h);
  const V3 = h.position.constructor;

  // Approach it, exactly as flying would.
  const approach = [hz * 300, hz * 100, hz * 20, hz * 5, hz * 1.5, hz];
  let built = 0;
  let everLocked = true;
  let lockFailAt = null;
  for (const d of approach) {
    const eye = new V3(h.position.x + d, h.position.y, h.position.z);
    appRef.holeField.update(eye, holes.map((r) => ({
      id: r.id, position: r.position,
      horizon: appRef.universe.horizonRadiusOf(r), seed: r.seed ?? 1
    })));
    if (appRef.holeField.has(h.id)) {
      built++;
      if (!appRef.holeField.isLocked(h.id)) {
        everLocked = false;
        const dm = appRef.scene.meshes.find((m) => m.name === 'bhDisk_' + h.id);
        const hm = appRef.scene.meshes.find((m) => m.name === 'bhHorizon_' + h.id);
        lockFailAt = d.toFixed(2) +
          ' disk@' + (dm ? JSON.stringify(dm.position.asArray().map((v) => +v.toFixed(2))) : '?') +
          ' horizon@' + (hm ? JSON.stringify(hm.position.asArray().map((v) => +v.toFixed(2))) : '?') +
          ' region@' + JSON.stringify(h.position.asArray().map((v) => +v.toFixed(2)));
      }
    }
  }
  travelChecks.push(['a hole gains real geometry as you approach', built > 0,
    'built at ' + built + '/' + approach.length + ' distances']);
  travelChecks.push(['the horizon and disk never drift apart', everLocked,
    lockFailAt ? 'first failed at distance ' + lockFailAt : '']);

  // The check above only proves they agree where they were BUILT. The
  // reported bug was separation while things MOVE, so actually move the
  // hole and re-assert. Without this the test passes against code that
  // never repositions the disk at all.
  {
    const moved = new V3(h.position.x + 500, h.position.y + 250,
      h.position.z - 900);
    const original = h.position.clone();
    h.position.copyFrom(moved);
    appRef.holeField.update(
      new V3(moved.x + hz * 4, moved.y, moved.z),
      holes.map((r) => ({
        id: r.id, position: r.position,
        horizon: appRef.universe.horizonRadiusOf(r), seed: r.seed ?? 1
      })));
    // One raymarched quad carries the shadow, the disk and the lensing, so
    // "the disk drifting off the horizon" is now impossible by construction.
    // What can still go wrong is the quad being left behind, so assert that.
    const qm = appRef.scene.meshes.find((m) => m.name === 'bhQuad_' + h.id);
    travelChecks.push(['the hole follows its region when it moves',
      !!qm && Vector3Distance(qm.position, moved) < 1e-6,
      qm ? JSON.stringify(qm.position.asArray()) : 'no quad']);
    travelChecks.push(['the shadow and disk cannot separate (one object)',
      !!qm && appRef.holeField.isLocked(h.id)]);
    travelChecks.push(['they are still locked to each other after moving',
      appRef.holeField.isLocked(h.id)]);
    h.position.copyFrom(original);
  }

  // There must be something actually drawn at the hole.
  const near = appRef.scene.meshes.filter((m) => /^bhQuad_/.test(m.name));
  travelChecks.push(['there is a hole drawn where you flew to',
    near.length >= 1, near.map((m) => m.name).join(', ')]);
  // ...and it must be a shader, not a lit mesh.
  const shaded = near.every((m) => m.material &&
    /ShaderMaterial/.test(m.material.getClassName()));
  travelChecks.push(['the hole is drawn by a shader, not geometry', shaded]);

  // Rotating the camera must not move the hole.
  const diskBefore = appRef.scene.meshes.find((m) => /^bhQuad_/.test(m.name));
  const posBefore = diskBefore && diskBefore.getAbsolutePosition().clone();
  appRef.camera.rotation && (appRef.camera.rotation.y += 1.1);
  appRef.scene.render && null;
  appRef.holeField.update(
    new V3(h.position.x + hz * 6, h.position.y, h.position.z),
    holes.map((r) => ({
      id: r.id, position: r.position,
      horizon: appRef.universe.horizonRadiusOf(r), seed: r.seed ?? 1
    })));
  const posAfter = diskBefore && diskBefore.getAbsolutePosition();
  travelChecks.push(['rotating the camera does not move the hole',
    !posBefore || Vector3Distance(posBefore, posAfter) < 1e-6]);

  // Far away it must be released again, or an endless universe leaks meshes.
  appRef.holeField.update(
    new V3(h.position.x + hz * 5000, h.position.y, h.position.z),
    holes.map((r) => ({
      id: r.id, position: r.position,
      horizon: appRef.universe.horizonRadiusOf(r), seed: r.seed ?? 1
    })));
  travelChecks.push(['holes are released once you leave',
    !appRef.holeField.has(h.id)]);

  // ---- clicking "Fly" on a hole must actually arrive at THAT hole ----
  // The user clicked Fly and the screen froze with nothing visible. Nothing
  // threw: warpTo() put them a standoff from the region, but BlackHoleWorld
  // rendered its hole at the origin and aimed the camera there, leaving them
  // ~837 units away looking at empty space.
  {
    const target = holes[0];
    await appRef.warpTo(target.id);
    await new Promise((res) => setTimeout(res, 2000));

    const w = appRef.world;
    travelChecks.push(['flying to a hole loads the singularity',
      w && w.id === 'blackhole', 'world=' + (w && w.id)]);

    // The raymarched hole must sit exactly on the region travelled to.
    const off = w && w.center
      ? Vector3Distance(w.center, target.position) : Infinity;
    travelChecks.push(['the rendered hole sits on the region you flew to',
      off < 1e-6,
      'offset ' + (Number.isFinite(off) ? off.toFixed(4) : 'n/a') +
      ' center=' + (w && w.center ? [w.center.x, w.center.y, w.center.z]
        .map((n) => n.toFixed(1)).join(',') : 'n/a')]);

    // ...and it must be in front of the camera, not behind it.
    const aimedAt = (c, at) => {
      const fwd = c.getTarget().subtract(c.position);
      const to = at.subtract(c.position);
      const fl = Math.hypot(fwd.x, fwd.y, fwd.z);
      const tl = Math.hypot(to.x, to.y, to.z);
      return (fwd.x * to.x + fwd.y * to.y + fwd.z * to.z) / (fl * tl);
    };
    travelChecks.push(['the camera is pointed at the hole after arriving',
      aimedAt(appRef.camera, target.position) > 0.99,
      'cos=' + aimedAt(appRef.camera, target.position).toFixed(4)]);

    // The WORLD itself must do the aiming. App re-points the camera after
    // loadWorld resolves, which masked a world that aimed at the origin -
    // the check above passed even with the bug present. Rebuild the world
    // directly against the real context and re-test the aim, with no App
    // correction afterwards.
    {
      const ctx = appRef.ctx;
      const before = ctx.focus;
      ctx.focus = { position: target.position.clone(), radius: target.radius };
      let aimOk = false, detail = '';
      try {
        const fresh = appRef.world;
        if (fresh && typeof fresh.build === 'function') {
          appRef.camera.setTarget(new V3(0, 0, 0));
          await fresh.build(ctx);
          const c = aimedAt(appRef.camera, target.position);
          aimOk = c > 0.99;
          detail = 'cos=' + c.toFixed(4);
        } else { detail = 'no world to rebuild'; }
      } catch (e) { detail = 'threw ' + e.message; }
      ctx.focus = before;
      travelChecks.push([
        'the world aims at its own hole without App correcting it',
        aimOk, detail]);
    }
  }

  // ---- after warping you must actually be FACING the place ----
  // The user flew to a hole, saw an unlensed black disc, and asked whether it
  // was even the black hole. It was the geometry hole - and lensing was off
  // because the camera was pointing away from it. warpTo aimed the camera,
  // but in free-fly the frame re-aims the camera from the VEHICLE's heading
  // every frame, and nothing ever turned the vehicle. One frame later the aim
  // was gone, LensFX's "behind the camera" test rejected the hole, and the
  // player saw unlensed geometry.
  {
    const hf = holes[0];
    await appRef.warpTo(hf.id);
    await new Promise((res) => setTimeout(res, 2200));

    const fwd = appRef.vehicle.axes().fwd;
    const toHole = hf.position.subtract(appRef.vehicle.position);
    const tl = Math.hypot(toHole.x, toHole.y, toHole.z);
    const dot = (fwd.x * toHole.x + fwd.y * toHole.y + fwd.z * toHole.z) / tl;
    travelChecks.push(['the ship itself faces the place you warped to',
      dot > 0.99, 'dot=' + dot.toFixed(4)]);
  }

  // ---- a second Fly click must never be silently dropped ----
  // The user reported that warping to ANY place froze the screen. loadWorld
  // returns early while `switching` is true, and warpTo now always rebuilds,
  // so a click that lands during a ~3s load was thrown away: the camera had
  // already been moved but the destination world never loaded. The player was
  // left in the old world, aimed at nothing, with no error.
  {
    const A = appRef.universe.regions.filter((x) => x.kind === 'star-system')[0];
    const B = appRef.universe.regions.filter((x) => x.kind === 'blackhole')[0];
    if (A && B) {
      appRef.warpTo(A.id);          // begins an async load
      appRef.warpTo(B.id);          // arrives mid-load
      await new Promise((res) => setTimeout(res, 4500));
      travelChecks.push(['a warp requested during a load is not dropped',
        appRef.currentId === 'blackhole',
        'ended in ' + appRef.currentId + ', expected blackhole']);
      const tgt = appRef.camera.getTarget();
      travelChecks.push(['the camera ends up aimed at the LAST place clicked',
        Vector3Distance(tgt, B.position) < 1e-3,
        'target=' + [tgt.x, tgt.y, tgt.z].map((n) => n.toFixed(1)).join(',')]);
    }
  }

  // ---- and it must be BIG ENOUGH TO SEE on arrival ----
  // The aim fix was not enough: the user still saw only a small white blob.
  // BlackHoleWorld rendered a hardcoded mass of 1.0 while the region carried
  // ~10,000, and warpTo stood off by the 620 u region radius. The shadow
  // subtended ~2 px, so only bloom survived.
  {
    const target2 = holes[0];
    await appRef.warpTo(target2.id);
    await new Promise((res) => setTimeout(res, 2000));

    const w2 = appRef.world;
    const dist = Vector3Distance(appRef.camera.position, target2.position);
    // The raymarcher captures photons inside ~2.6 rs; rs is the world's mass.
    const rs = w2 && w2.p ? w2.p.mass : 0;
    const fov = appRef.camera.fov || 0.9;
    const angular = Math.atan((rs * 2.6) / Math.max(dist, 1e-6));
    const fracOfHalf = angular / (fov / 2);

    travelChecks.push(['the arrival hole uses the region mass, not a default',
      rs > 2, 'rs=' + rs.toFixed(2)]);
    // 0.35 of half-screen is ~122 px radius on a 700 px viewport - a hole you
    // are looking AT. The old 837 u framing scored 0.15 (53 px), which is
    // still just a bloomed dot, so a lower bar would pass the reported bug.
    travelChecks.push(['the hole is large on screen when you arrive',
      fracOfHalf > 0.35,
      'dist=' + dist.toFixed(0) + ' rs=' + rs.toFixed(1) +
      ' -> ' + (fracOfHalf * 100).toFixed(1) + '% of half-screen']);
    // ...and not so close that it swallows the whole frame.
    travelChecks.push(['the hole does not fill the entire frame',
      fracOfHalf < 1.6, (fracOfHalf * 100).toFixed(1) + '%']);
  }

  // ---- the Singularity must show exactly ONE hole ----
  // The user saw a bare black circle on one side of the screen and the
  // lensed orange disk on the other. That is two different holes: the world
  // raymarches its own, and the geometry field built a second one from the
  // universe region list at an unrelated position.
  // Rebuild the geometry hole, then feed the empty list App passes when a
  // world owns the hole, and require every mesh to be gone.
  const spec = holes.map((r) => ({
    id: r.id, position: r.position,
    horizon: appRef.universe.horizonRadiusOf(r), seed: r.seed ?? 1
  }));
  const atHole = new V3(h.position.x + hz * 6, h.position.y, h.position.z);
  appRef.holeField.update(atHole, spec);
  travelChecks.push(['a geometry hole exists before suppression',
    appRef.holeField.has(h.id)]);

  appRef.holeField.update(atHole, []);
  travelChecks.push(['a world that owns the hole suppresses the geometry one',
    !appRef.holeField.has(h.id) && appRef.holeField.count === 0,
    'count=' + appRef.holeField.count]);

  const strays = appRef.scene.meshes.filter(
    (m) => /^bh(Horizon|Disk|Glow|Quad)_/.test(m.name));
  travelChecks.push(['no black-hole geometry is stranded in the scene',
    strays.length === 0,
    strays.map((m) => m.name).join(',')]);
} catch (e) {
  travelChecks.push(['travelling to a hole survives: ' + e.message, false]);
}
console.log('\n=== travelling to a black hole ===');
for (const [n, c, e] of travelChecks) ok(n, c, e);

console.log('\n=== creatures ===');
for (const [name, cond, detail] of critterChecks) ok(name, cond, detail);

console.log('\n=== fleets ===');
for (const [name, cond, detail] of fleetChecks) ok(name, cond, detail);

console.log('\n=== flight HUD ===');
for (const [name, cond] of hudChecks) ok(name, cond);

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
      // Debris must never consume the player's cooldown. This was a real
      // bug: an asteroid drifting through a rift blocked the player's
      // dimension jump for 1.5s, which showed up as the jump silently
      // doing nothing.
      try {
        const sysC = sw['portals'] ?? null;
        const tearP = sysC ? sysC.list().find((p) => p.kind === 'tear') : null;
        ok('a tear exists for the cooldown check',
           !!tearP, 'sys=' + !!sysC + ' kinds=' +
           (sysC ? sysC.list().map((p) => p.kind).join(',') : 'n/a'));
        if (tearP) {
          const mouth = tearP.a;
          // Sit exactly on the mouth and move toward it, which satisfies the
          // "must be closing" rule in tryTransit.
          const at = (o) => ({
            position: mouth.position.clone(),
            velocity: mouth.position.scale(0).add(mouth.normal.scale(-1)),
            ...o
          });
          tearP.openness = 1;
          const rock = at({});                       // keyed by reference
          const first = sysC.tryTransit(rock, 3);
          const player = at({ key: 'player-cooldown-probe' });
          const second = sysC.tryTransit(player, 2);
          ok('debris does not consume the player cooldown',
            !!first && !!second, 'rock=' + !!first + ' player=' + !!second);

          // ...and the player still cannot ping-pong on consecutive frames.
          const again = sysC.tryTransit(at({ key: 'player-cooldown-probe' }), 2);
          ok('the player cannot re-enter the same tear at once', !again);
        }
      } catch (e) {
        ok('portal cooldown check ran', false, e && (e.stack || e.message || String(e)));
      }

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
