/**
 * UI verification — WindowManager behaviour and Shell wiring, driven in a
 * real DOM (jsdom). Specifically asserts the P0 defects from the build
 * directive are fixed: black screen, panels covering the sim, dead close
 * buttons, lingering overlays and trapped input.
 *
 * Run: node tools/ui-check.mjs
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
global.requestAnimationFrame = (cb) => setTimeout(() => cb(Date.now()), 16);
dom.window.Element.prototype.setPointerCapture = function () {};
dom.window.Element.prototype.releasePointerCapture = function () {};
dom.window.HTMLCanvasElement.prototype.getContext = () => ({
  clearRect(){}, beginPath(){}, moveTo(){}, lineTo(){}, stroke(){}, fill(){}, arc(){},
  fillRect(){}, createLinearGradient: () => ({ addColorStop(){} }),
  createRadialGradient: () => ({ addColorStop(){} }),
  set fillStyle(v){}, set strokeStyle(v){}, set lineWidth(v){}
});

async function load(entry, name) {
  const out = await build({
    entryPoints: [entry], bundle: true, format: 'esm', write: false, logLevel: 'error'
  });
  const f = `/tmp/${name}-${Date.now()}.mjs`;
  fs.writeFileSync(f, out.outputFiles[0].text);
  return import(f);
}

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};
const click = (el) => el.dispatchEvent(new dom.window.MouseEvent('click', { bubbles: true }));
const key = (k) => dom.window.dispatchEvent(new dom.window.KeyboardEvent('keydown', { key: k, bubbles: true }));

/* ============================ WindowManager ============================ */

const { WindowManager } = await load('src/bjs/ui/WindowManager.ts', 'wm');
const css = fs.readFileSync('src/bjs/ui/styles.ts', 'utf8');

console.log('\n=== WindowManager ===');
const wm = new WindowManager(document.body);
let renders = 0;
wm.register({ id: 'a', title: 'Alpha', glyph: 'A', width: 300, height: 200,
  render: (b) => { renders++; b.innerHTML = '<button id="inner">x</button>'; } });
wm.register({ id: 'b', title: 'Beta', width: 300, render: (b) => { b.textContent = 'beta'; } });

console.log('\n— required API —');
for (const m of ['Open','Close','Toggle','Minimize','Maximize','BringToFront','SendToBack','Reset','IsOpen','IsVisible'])
  ok(`${m}()`, typeof wm[m] === 'function');

console.log('\n— P0: sim never covered or input-trapped —');
ok('.wm-layer is pointer-events:none', /\.wm-layer\{[^}]*pointer-events:none/.test(css));
ok('.wm-win re-enables pointer events only for itself', /\.wm-win\{[^}]*pointer-events:auto/.test(css));

console.log('\n— P0: open/close actually work —');
wm.Open('a');
const el = document.querySelector('[data-wid="a"]');
ok('Open() opens and renders', wm.IsVisible('a') && el.style.display === 'flex' && renders === 1);
click(el.querySelector('[data-act="close"]'));
ok('X button closes', !wm.IsOpen('a') && el.style.display === 'none');

console.log('\n— P0: nothing lingers after close —');
ok('body cleared', el.querySelector('.wm-body').innerHTML === '');
ok('no stray content left in document', document.getElementById('inner') === null);

console.log('\n— minimize / maximize —');
wm.Open('a'); wm.Minimize('a');
ok('Minimize keeps it open but hidden', wm.IsOpen('a') && !wm.IsVisible('a'));
const dockBtns = document.querySelectorAll('.wm-dockbtn');
ok('restorable from dock', dockBtns.length === 1);
click(dockBtns[0]);
ok('dock restores it', wm.IsVisible('a'));
wm.Maximize('a');
ok('maximize leaves the sim visible (<60% width)',
   parseInt(el.style.width) < dom.window.innerWidth * 0.6);
wm.Maximize('a');
ok('maximize toggles back', !el.classList.contains('wm-max'));

console.log('\n— stacking, reset, escape —');
wm.Open('b'); wm.BringToFront('a');
ok('BringToFront raises z', +el.style.zIndex > +document.querySelector('[data-wid="b"]').style.zIndex);
wm.SendToBack('a');
ok('SendToBack lowers z', +el.style.zIndex < +document.querySelector('[data-wid="b"]').style.zIndex);
wm.Open('a'); wm.Open('b');
key('Escape');
ok('Escape closes top-most (never traps)', !(wm.IsVisible('a') && wm.IsVisible('b')));
wm.Reset();
ok('Reset closes everything', !wm.IsOpen('a') && !wm.IsOpen('b'));

/* ================================ Shell ================================ */

console.log('\n\n=== Shell ===');
document.body.innerHTML = '<canvas id="renderCanvas"></canvas>';
document.head.innerHTML = '';

const { Shell, WORLDS } = await load('src/bjs/ui/Shell.ts', 'shell');
const ev = { world: [], param: [], action: [], postfx: [], spawn: [], snaps: [], loaded: [], undo: 0, redo: 0, reset: 0, pause: [] };
const shell = new Shell({
  onWorld: (id) => ev.world.push(id),
  onParam: (k, v) => ev.param.push([k, v]),
  onAction: (k) => ev.action.push(k),
  onMode: () => {},
  onReset: () => ev.reset++,
  onPause: (p) => ev.pause.push(p),
  onPostFX: (k, v) => ev.postfx.push([k, v]),
  onSpawn: (id, sc) => ev.spawn.push([id, sc]),
  onUndo: () => { ev.undo++; return 'undone'; },
  onRedo: () => { ev.redo++; return 'redone'; },
  onSaveSnapshot: (label) => { const s2 = { id: 's' + ev.snaps.length, label, time: Date.now() }; ev.snaps.push(s2); return s2; },
  onLoadSnapshot: (id) => { ev.loaded.push(id); return true; },
  listSnapshots: () => ev.snaps,
  canUndo: () => ev.undo === 0 ? true : true,
  canRedo: () => true
});

const params = Array.from({ length: 9 }, (_, i) => ({
  key: 'p' + i, label: 'Param ' + i, min: 0, max: 10, step: 0.1, value: i
}));
shell.setWorld({
  id: 'sandbox', name: 'Gravity Sandbox',
  getParams: () => params, setParam: () => {},
  getStats: () => ({ Bodies: '6', Integrator: 'Velocity Verlet' }),
  getActions: () => [{ key: 'clear', label: 'Clear All', glyph: '🧹' }],
  runAction: () => {}, build: async () => {}, update: () => {}, dispose: () => {}
});

console.log('\n— chrome renders (not a black screen) —');
ok('boot overlay exists', !!document.querySelector('.boot'));
shell.progress(50, 'half');
ok('progress bar updates', document.getElementById('bootFill').style.width === '50%');
shell.hideBoot();
ok('boot dismisses', document.querySelector('.boot').classList.contains('gone'));
ok('top bar rendered', !!document.querySelector('.topbar'));
ok('HUD rendered', !!document.querySelector('.hud'));
ok('canvas still present', !!document.getElementById('renderCanvas'));
ok(`all ${WORLDS.length} worlds listed`, document.querySelectorAll('#worldSeg button').length === WORLDS.length);
ok('gravity sandbox is present', WORLDS.some((w) => w.id === 'sandbox'));

console.log('\n— progressive disclosure —');
shell.wm.Open('controls');
const sliders = () => document.querySelectorAll('[data-wid="controls"] input[type=range]');
shell.setMode('simple');   const nS = sliders().length;
shell.setMode('advanced'); const nA = sliders().length;
shell.setMode('expert');   const nE = sliders().length;
ok(`simple(${nS}) < advanced(${nA}) < expert(${nE})`, nS < nA && nA < nE);
ok('expert shows every parameter', nE === params.length);

console.log('\n— controls are wired —');
const s0 = sliders()[0];
s0.value = '7.5';
s0.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
ok('slider emits onParam with correct key/value',
   ev.param.length && ev.param[0][0] === 'p0' && Math.abs(ev.param[0][1] - 7.5) < 1e-6);
const clearBtn = [...document.querySelectorAll('[data-wid="controls"] .btn')]
  .find((b) => b.textContent.includes('Clear All'));
ok('world action rendered', !!clearBtn);
click(clearBtn);
ok('action emits onAction', ev.action.includes('clear'));

console.log('\n— graphics panel —');
shell.wm.Open('graphics');
ok('graphics window opens', shell.wm.IsVisible('graphics'));
const gS = document.querySelectorAll('[data-wid="graphics"] input[type=range]');
ok('graphics exposes sliders', gS.length > 0);
gS[0].value = '1.4';
gS[0].dispatchEvent(new dom.window.Event('input', { bubbles: true }));
ok('graphics slider emits onPostFX', ev.postfx.length > 0, JSON.stringify(ev.postfx));
const lookBtn = [...document.querySelectorAll('[data-wid="graphics"] .btn')]
  .find((b) => b.textContent.includes('Cinematic'));
const beforeLook = ev.postfx.length;
click(lookBtn);
ok('a "look" applies several settings at once', ev.postfx.length > beforeLook + 2);

console.log('\n— library search —');
shell.wm.Open('library');
const cards = () => document.querySelectorAll('[data-wid="library"] .card');
ok(`library lists ${WORLDS.length} worlds`, cards().length === WORLDS.length);
const search = document.querySelector('[data-wid="library"] .search');
search.value = 'lensing';
search.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
ok('search narrows to the black hole', cards().length === 1, `got ${cards().length}`);
search.value = 'collision';
search.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
ok('search finds the sandbox by tag', cards().length === 1);
search.value = 'zzz';
search.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
ok('empty state instead of a blank panel',
   document.querySelector('[data-wid="library"] .note')?.textContent.includes('No matches'));

console.log('\n— telemetry & global controls —');
shell.tickHud(60, 'Gravity Sandbox');
ok('HUD shows FPS', document.getElementById('hFps').textContent.includes('60'));
shell.wm.Open('telemetry');
ok('telemetry shows world stats',
   document.querySelector('[data-wid="telemetry"]').textContent.includes('Verlet'));
click(document.getElementById('btnPause'));
ok('pause emits onPause(true)', ev.pause.at(-1) === true);
click(document.getElementById('btnReset'));
ok('reset emits onReset', ev.reset > 0);

console.log('\n— keyboard —');
shell.wm.CloseAll();
key('1'); ok('"1" opens Controls', shell.wm.IsVisible('controls'));
key('5'); ok('"5" opens Graphics', shell.wm.IsVisible('graphics'));
key('h');
ok('"h" clears the screen for the sim',
   !shell.wm.IsVisible('controls') && !shell.wm.IsVisible('graphics'));

console.log('\n— object catalogue tray —');
shell.wm.Open('objects');
ok('objects window opens', shell.wm.IsVisible('objects'));
const objCards = () => document.querySelectorAll('[data-wid="objects"] .card');
ok(`tray renders objects (${objCards().length})`, objCards().length > 20);
const objSearch = document.querySelector('[data-wid="objects"] .search');
objSearch.value = 'duck';
objSearch.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
ok('object search filters', objCards().length >= 1 && objCards().length < 10,
   `got ${objCards().length}`);
click(objCards()[0]);
ok('clicking an object emits onSpawn', ev.spawn.length > 0, JSON.stringify(ev.spawn));
objSearch.value = '';
objSearch.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
const randomBtn = [...document.querySelectorAll('[data-wid="objects"] .btn')]
  .find((b) => b.textContent.includes('Random Object'));
const beforeRand = ev.spawn.length;
click(randomBtn);
ok('random object button spawns', ev.spawn.length > beforeRand);

console.log('\n— snapshots & undo/redo —');
shell.wm.Open('snapshots');
ok('snapshots window opens', shell.wm.IsVisible('snapshots'));
click(document.getElementById('btnUndo'));
ok('undo button fires onUndo', ev.undo > 0);
click(document.getElementById('btnRedo'));
ok('redo button fires onRedo', ev.redo > 0);
const saveBtn = [...document.querySelectorAll('[data-wid="snapshots"] .btn')]
  .find((b) => b.textContent.includes('Save Current State'));
click(saveBtn);
ok('save creates a snapshot', ev.snaps.length === 1);
shell.wm.refresh('snapshots');
const loadBtn = [...document.querySelectorAll('[data-wid="snapshots"] .btn')]
  .find((b) => b.textContent === 'Load');
ok('saved snapshot is listed with a Load button', !!loadBtn);
click(loadBtn);
ok('load fires onLoadSnapshot', ev.loaded.length === 1);
dom.window.dispatchEvent(new dom.window.KeyboardEvent('keydown',
  { key: 'z', ctrlKey: true, bubbles: true }));
ok('Ctrl+Z triggers undo', ev.undo > 1);

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
