/**
 * mouse-check — you must never be stuck.
 *
 * Free-fly detaches Babylon's arc camera, and for a while nothing replaced
 * its mouse handling, so the player could not look around or zoom at all.
 * These assertions exist so that cannot silently come back.
 */
import { JSDOM } from 'jsdom';
import { build } from 'esbuild';
import fs from 'fs';

const dom = new JSDOM('<!doctype html><html><body><canvas id="c"></canvas></body></html>',
  { pretendToBeVisual: true });
globalThis.window = dom.window;
globalThis.document = dom.window.document;
// Node 22 exposes navigator as a getter-only global.
Object.defineProperty(globalThis, 'navigator', { value: dom.window.navigator, configurable: true });
globalThis.HTMLElement = dom.window.HTMLElement;
globalThis.PointerEvent = dom.window.PointerEvent ?? dom.window.MouseEvent;
globalThis.WheelEvent = dom.window.WheelEvent;

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

const { MouseLook } = await load('src/bjs/systems/MouseLook.ts', 'mouselook');

const canvas = document.getElementById('c');
// jsdom has no pointer capture.
canvas.setPointerCapture = () => {};
canvas.releasePointerCapture = () => {};

const move = (el, dx, dy) => {
  const e = new dom.window.MouseEvent('pointermove', { bubbles: true });
  Object.defineProperty(e, 'movementX', { value: dx });
  Object.defineProperty(e, 'movementY', { value: dy });
  el.dispatchEvent(e);
};
const down = (el) => {
  const e = new dom.window.MouseEvent('pointerdown', { bubbles: true, button: 0 });
  Object.defineProperty(e, 'pointerId', { value: 1 });
  el.dispatchEvent(e);
};
const up = (el) => {
  const e = new dom.window.MouseEvent('pointerup', { bubbles: true, button: 0 });
  Object.defineProperty(e, 'pointerId', { value: 1 });
  el.dispatchEvent(e);
};
const wheel = (el, dy) =>
  el.dispatchEvent(new dom.window.WheelEvent('wheel', { deltaY: dy, bubbles: true, cancelable: true }));

console.log('— looking around —');
{
  const ml = new MouseLook();
  ml.attach(canvas);

  // Moving without dragging must do nothing, or the view drifts on its own.
  move(canvas, 40, 20);
  let out = ml.consume(1 / 60);
  ok('the view does not move unless you are dragging',
     Math.abs(out.yaw) < 1e-9 && Math.abs(out.pitch) < 1e-9);

  down(canvas);
  move(canvas, 60, 0);
  out = ml.consume(1 / 60);
  ok(`dragging turns the view (yaw ${out.yaw.toFixed(3)})`, out.yaw > 0.01);

  down(canvas);
  move(canvas, -60, 0);
  out = ml.consume(1 / 60);
  ok('dragging the other way turns the other way', out.yaw < -0.01);

  down(canvas);
  move(canvas, 0, 50);
  out = ml.consume(1 / 60);
  ok('dragging vertically pitches the view', Math.abs(out.pitch) > 0.01);

  // A violent flick must not spin the camera out of control.
  down(canvas);
  move(canvas, 99999, 99999);
  out = ml.consume(1 / 60);
  ok('a violent flick is clamped, never wild',
     Math.abs(out.yaw) <= 1 && Math.abs(out.pitch) <= 1);

  // Releasing must stop the turn.
  up(canvas);
  for (let i = 0; i < 30; i++) ml.consume(1 / 60);
  move(canvas, 80, 80);
  out = ml.consume(1 / 60);
  ok('releasing the button stops the turn',
     Math.abs(out.yaw) < 1e-6 && Math.abs(out.pitch) < 1e-6);

  // Inverted look
  const inv = new MouseLook();
  inv.opts.invertY = true;
  inv.attach(canvas);
  down(canvas); move(canvas, 0, 40);
  const a = inv.consume(1 / 60);
  inv.detach();
  const norm = new MouseLook();
  norm.attach(canvas);
  down(canvas); move(canvas, 0, 40);
  const b = norm.consume(1 / 60);
  ok('invert Y actually inverts', Math.sign(a.pitch) === -Math.sign(b.pitch));
  norm.detach();
  ml.detach();
}

console.log('\n— the wheel is a real throttle —');
{
  const ml = new MouseLook();
  ml.attach(canvas);
  const base = ml.throttleScale;
  ok('throttle starts at 1x', Math.abs(base - 1) < 1e-9);

  wheel(canvas, -100);           // scroll up
  ok(`scrolling up speeds you up (${ml.throttleScale.toFixed(2)}x)`,
     ml.throttleScale > base);

  const fast = ml.throttleScale;
  wheel(canvas, 100);            // scroll down
  ok('scrolling down slows you down', ml.throttleScale < fast);

  // It must not run away to zero or infinity.
  for (let i = 0; i < 400; i++) wheel(canvas, -100);
  ok(`throttle is capped at the top (${ml.throttleScale.toFixed(1)}x)`,
     Number.isFinite(ml.throttleScale) && ml.throttleScale <= 20);
  for (let i = 0; i < 800; i++) wheel(canvas, 100);
  ok(`throttle has a floor above zero (${ml.throttleScale.toFixed(3)}x)`,
     ml.throttleScale >= 0.05);

  ml.setThrottle(NaN);
  ok('a bad throttle value cannot break it', Number.isFinite(ml.throttleScale));
  ml.detach();
}

console.log('\n— shift+wheel is an optical zoom —');
{
  const ml = new MouseLook();
  ml.attach(canvas);
  ok('zoom starts at 1x', Math.abs(ml.zoomScale - 1) < 1e-9);

  const shiftWheel = (dy) => canvas.dispatchEvent(
    new dom.window.WheelEvent('wheel', { deltaY: dy, shiftKey: true, bubbles: true, cancelable: true }));

  const throttleBefore = ml.throttleScale;
  shiftWheel(-100);
  ok(`shift+wheel magnifies (${ml.zoomScale.toFixed(2)}x)`, ml.zoomScale > 1);
  ok('and does not touch the throttle',
     Math.abs(ml.throttleScale - throttleBefore) < 1e-9);

  const plain = ml.zoomScale;
  wheel(canvas, -100);
  ok('plain wheel does not touch the zoom',
     Math.abs(ml.zoomScale - plain) < 1e-9);

  for (let i = 0; i < 200; i++) shiftWheel(-100);
  ok(`zoom is capped (${ml.zoomScale.toFixed(1)}x)`,
     Number.isFinite(ml.zoomScale) && ml.zoomScale <= 60);
  for (let i = 0; i < 400; i++) shiftWheel(100);
  ok('zoom never goes below 1x', ml.zoomScale >= 1);

  shiftWheel(-100);
  ml.resetZoom();
  ok('zoom can be snapped back', Math.abs(ml.zoomScale - 1) < 1e-9);
  ml.detach();
}

console.log('\n— flying near things is not a cannon —');
{
  // Speed scales with how far away the nearest thing is. It used to be
  // linear, so being 500 units out gave 283 u/s and close work was
  // unusable. It must stay gentle near things and still climb far away.
  const src = fs.readFileSync('src/bjs/systems/VehicleSystem.ts', 'utf8');
  ok('speed scaling is sub-linear', src.includes('Math.sqrt(near)'));

  const speedAt = (d) => Math.max(2, Math.min(60000, Math.sqrt(d) * 3.2 + d * 0.02 + 3));
  ok(`close manoeuvring is slow (${speedAt(20).toFixed(0)} u/s at 20 units)`,
     speedAt(20) < 30);
  ok(`mid range is moderate (${speedAt(500).toFixed(0)} u/s at 500)`,
     speedAt(500) < 150);
  ok(`deep space is still fast (${speedAt(100000).toFixed(0)} u/s)`,
     speedAt(100000) > 1500);
  ok('speed always increases with distance',
     speedAt(10) < speedAt(100) && speedAt(100) < speedAt(10000));
}

console.log('\n— it never fights the interface —');
{
  const panel = document.createElement('div');
  panel.className = 'wm-win';
  const slider = document.createElement('input');
  slider.type = 'range';
  panel.appendChild(slider);
  document.body.appendChild(panel);

  const ml = new MouseLook();
  ml.attach(canvas);

  // Dragging a slider inside a panel must not also fly the ship.
  const e = new dom.window.MouseEvent('pointerdown', { bubbles: true, button: 0 });
  Object.defineProperty(e, 'pointerId', { value: 2 });
  slider.dispatchEvent(e);
  ok('dragging a panel control does not start a camera drag', !ml.isDragging);

  const before = ml.throttleScale;
  slider.dispatchEvent(new dom.window.WheelEvent('wheel', { deltaY: -100, bubbles: true, cancelable: true }));
  ok('scrolling inside a panel does not change the throttle',
     Math.abs(ml.throttleScale - before) < 1e-9);

  ml.setEnabled(false);
  down(canvas); move(canvas, 100, 100);
  const out = ml.consume(1 / 60);
  ok('it can be disabled entirely',
     Math.abs(out.yaw) < 1e-9 && Math.abs(out.pitch) < 1e-9);

  ml.detach();
  const after = ml.throttleScale;
  wheel(canvas, -100);
  ok('detaching removes every listener', Math.abs(ml.throttleScale - after) < 1e-9);
}

console.log('\n— wired into the app —');
{
  const app = fs.readFileSync('src/bjs/App.ts', 'utf8');
  ok('the app owns a MouseLook', app.includes('new MouseLook'));
  ok('it is attached to the render canvas', /this\.mouse\.attach\(/.test(app));
  ok('look axes reach the vehicle', /this\.mouse\.consume\(/.test(app));
  ok('the wheel scales real flight speed',
     app.includes('this.mouse.throttleScale'));
  ok('there is a pointer-lock key', /toggleLock\(\)/.test(app));
  ok('optical zoom drives the real camera FOV',
     app.includes('this.mouse.zoomScale') && app.includes('this.camera.fov'));
  ok('zoom can be reset with a key', app.includes('resetZoom()'));

  const v = fs.readFileSync('src/bjs/systems/VehicleSystem.ts', 'utf8');
  ok('free-fly is still the launch default', fs.readFileSync('src/bjs/App.ts', 'utf8')
     .includes("setControlMode('freefly')"));
  ok('the vehicle still accepts yaw/pitch input', /yaw:\s*number/.test(v));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
