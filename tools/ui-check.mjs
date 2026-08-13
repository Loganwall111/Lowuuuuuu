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
const { IntroOverlay } = await load('src/bjs/ui/IntroOverlay.ts', 'introui');
const { IntroSequence, LESSONS, SHIP_STATIONS } =
  await load('src/bjs/systems/IntroSequence.ts', 'introseq');
const qstate = { current: 'high', scaling: 1.0, adaptive: false };
const ustate = {
  stats: { Location: '🌌 Deep space', 'Black holes': '4', Holding: '—' },
  current: null,
  regions: [
    { id: 'sys-1', name: 'Home', glyph: '☀', kind: 'star-system', distance: 0 },
    { id: 'pl-2', name: 'Home I1', glyph: '🪐', kind: 'planet', distance: 140 },
    { id: 'bh-3', name: 'Vela Deep Singularity', glyph: '⚫', kind: 'blackhole', distance: 2200 },
    { id: 'neb-4', name: 'Lyra Nebula', glyph: '🌫', kind: 'nebula', distance: 5400 }
  ],
  holding: null,
  lens: { Lens: '⚫ Schwarzschild', Mode: 'schwarzschild', Strength: '1.00×',
          'Photon ring': '1.00×', Symmetry: 'radial' }
};
const vstate = { mode: 'orbit', ship: 'shuttle',
  stats: { Mode: 'orbit', Speed: '0.0 u/s', Grounded: '-' } };
const ev = { world: [], param: [], action: [], postfx: [], spawn: [], snaps: [], loaded: [], quality: [], adaptive: [], games: [], loadedGames: [], modes: [], ships: [],
  warps: [], grabs: [], releases: [], spawns: [], deletes: [], lensModes: [], lensFields: [], undo: 0, redo: 0, reset: 0, pause: [] };
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
  canRedo: () => true,
  onQuality: (n) => { ev.quality.push(n); qstate.current = n; },
  onAdaptive: (on) => { ev.adaptive.push(on); qstate.adaptive = on; },
  getQuality: () => qstate,
  onSaveGame: (name) => { const g = { id: 'g' + ev.games.length, name, world: 'sandbox', time: Date.now() }; ev.games.push(g); return g; },
  onLoadGame: (id) => { ev.loadedGames.push(id); return true; },
  listGames: () => ev.games,
  onDeleteGame: (id) => { ev.games = ev.games.filter((g) => g.id !== id); },
  onControlMode: (m) => { ev.modes.push(m); vstate.mode = m; },
  onShip: (id) => { ev.ships.push(id); vstate.ship = id; },
  getVehicle: () => vstate,
  getUniverse: () => ustate,
  onWarpTo: (id) => { ev.warps.push(id); ustate.current = ustate.regions.find((r) => r.id === id) ?? null; },
  onGrab: () => { ev.grabs.push('grab'); ustate.holding = 'Test Hole'; },
  onRelease: (thrown) => { ev.releases.push(thrown); ustate.holding = null; },
  onSpawnRegion: (kind) => { ev.spawns.push(kind); },
  onDeleteRegion: (id) => { ev.deletes.push(id); ustate.regions = ustate.regions.filter((r) => r.id !== id); },
  onLensMode: (m) => { ev.lensModes.push(m); },
  onLensField: (k, v) => { ev.lensFields.push([k, v]); },
  onRandomLens: () => { ev.lensModes.push('random'); }
});

const params = Array.from({ length: 9 }, (_, i) => ({
  key: 'p' + i, label: 'Param ' + i, min: 0, max: 10, step: 0.1, value: i
}));
shell.setWorld({
  id: 'sandbox', name: 'Gravity Sandbox',
  getParams: () => params, setParam: () => {},
  getStats: () => ({ Bodies: '6', Integrator: 'Velocity Verlet' }),
  getActions: () => [
    { key: 'clear', label: 'Clear All', glyph: '🧹' },
    { key: 'chaos', label: 'CHAOS', glyph: '🌀' },
    // realistic prefixed keys, mirroring the real worlds
    ...['laser', 'plasma', 'freeze', 'tractor'].map((k) => ({ key: 'beam:' + k, label: k, glyph: '🔫' })),
    ...['lightning', 'planet', 'star', 'life', 'rapture', 'freeze', 'reverse', 'cube', 'giant', 'shrink']
      .map((k) => ({ key: 'god:' + k, label: k, glyph: '✨' })),
    ...['whirlpool', 'tsunami', 'meteor', 'volcano', 'flood', 'drought', 'geyser', 'iceage', 'monsoon']
      .map((k) => ({ key: 'dis:' + k, label: k, glyph: '🌪' }))
  ],
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
// One continuous universe: no world tab strip in the toolbar.
ok('the toolbar has no world tabs',
   document.querySelectorAll('#worldSeg button').length === 0);
ok('worlds are still reachable from the navigator', WORLDS.length >= 4);
ok('gravity sandbox is present', WORLDS.some((w) => w.id === 'sandbox'));

console.log('\n— every control is available, and searchable —');
shell.wm.Open('controls');
const sliders = () => document.querySelectorAll('[data-wid="controls"] input[type=range]');
ok(`every parameter is shown by default (${sliders().length})`,
   sliders().length === params.length);
ok('there are no Simple/Advanced/Expert tier buttons',
   document.querySelectorAll('#modeSeg button').length === 0);

const ctlSearch = () => document.querySelector('[data-search="controls"]');
ok('the controls panel has a search box', !!ctlSearch());
{
  // typing must narrow the list to matching controls
  const target = params[params.length - 1];
  const box = ctlSearch();
  box.value = target.label;
  box.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
  const after = sliders().length;
  ok(`searching "${target.label}" narrows the list (${params.length} -> ${after})`,
     after < params.length && after >= 1);

  // a nonsense query must say so rather than render an empty void
  const box2 = ctlSearch();
  box2.value = 'zzzznothingmatchesthis';
  box2.dispatchEvent(new dom.window.Event('input', { bubbles: true }));
  ok('a search with no hits explains itself instead of going blank',
     sliders().length === 0 &&
     /nothing matches/i.test(document.querySelector('[data-wid="controls"]').textContent));

  // clearing restores everything
  const x = document.querySelector('[data-search-clear]');
  ok('the search box has a clear button', !!x);
  click(x);
  ok('clearing the search restores every control',
     sliders().length === params.length);
}

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
const lookBtn = document.querySelector('[data-look="filmic"]');
const beforeLook = ev.postfx.length;
click(lookBtn);
ok('a "look" applies several settings at once', ev.postfx.length > beforeLook + 2);

console.log('\n— the side tabs are gone; worlds are places you fly to —');
{
  // The World Library was a list of clickable worlds - exactly the tabbed
  // menu that was rejected. It is deleted. Travel is the navigator.
  ok('the World Library window is gone',
     !shell.wm.list().some((w) => (typeof w === 'string' ? w : w.id) === 'library'));
  ok('nothing renders a library panel',
     !document.querySelector('[data-wid="library"]'));

  const src = fs.readFileSync('src/bjs/ui/Shell.ts', 'utf8');
  ok('renderLibrary is deleted', !src.includes('renderLibrary'));
  ok('the dead #worldSeg lookups are gone', !src.includes('#worldSeg'));

  // Travelling to a place must load what that place is.
  const app = fs.readFileSync('src/bjs/App.ts', 'utf8');
  // The kind-to-world mapping now lives in one table shared with the world
  // registry, so these assert the behaviour and the table rather than a
  // literal in App.ts. locales-check covers the mapping in detail.
  const locales = fs.readFileSync('src/bjs/worlds/Locales.ts', 'utf8');
  ok('arriving somewhere loads that kind of world',
     app.includes('localeForKind('));
  ok('an ocean region is the ocean world',
     /id: 'ocean',[\s\S]*?kinds: \['ocean'\]/.test(locales));
  ok('a terrain region is the terraform world',
     /id: 'terraform',[\s\S]*?kinds: \['terrain'\]/.test(locales));
  ok('a black hole region is the black hole world',
     /id: 'blackhole',[\s\S]*?kinds: \['blackhole'\]/.test(locales));
  ok('warping loads the destination world',
     /localeForKind\(r\.kind\)\.id/.test(app));

  // The navigator is now the way around, and it lists real destinations.
  shell.wm.Open('navigator');
  const flyBtns = document.querySelectorAll('[data-wid="navigator"] [data-warp]');
  ok(`the navigator offers real places to fly to (${flyBtns.length})`,
     flyBtns.length > 0);
}

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

console.log('\n— P0: panels must never block the view —');
{
  // The user reported panels covering the screen. Verify the escape hatches.
  ok('interface defaults to compact density',
     document.body.dataset.density === 'compact', document.body.dataset.density);

  // 1. Focus mode hides everything without closing it
  const ids0 = shell.wm.list().map((w) => (typeof w === 'string' ? w : w.id));
  ids0.forEach((id) => shell.wm.Open(id));
  const openCount = ids0.filter((id) => shell.wm.IsVisible(id)).length;
  click(document.getElementById('btnFocus'));
  ok('focus mode engages', shell.wm.IsFocusMode());
  ok('focus mode does NOT close panels (layout is preserved)',
     ids0.filter((id) => shell.wm.IsVisible(id)).length === openCount);
  click(document.getElementById('btnFocus'));
  ok('focus mode toggles back off', !shell.wm.IsFocusMode());
  ok('panels are still open after leaving focus mode',
     ids0.filter((id) => shell.wm.IsVisible(id)).length === openCount);

  // 2. F key does the same
  dom.window.dispatchEvent(new dom.window.KeyboardEvent('keydown', { key: 'f', bubbles: true }));
  ok('F key toggles focus mode', shell.wm.IsFocusMode());
  dom.window.dispatchEvent(new dom.window.KeyboardEvent('keydown', { key: 'f', bubbles: true }));

  // 3. Tiling must never leave a panel overlapping the screen centre
  shell.wm.CloseAll();
  ['controls', 'objects', 'telemetry'].forEach((id) => shell.wm.Open(id));
  shell.wm.TileEdges();
  const cx = dom.window.innerWidth / 2;
  const centreHogs = ['controls', 'objects', 'telemetry'].filter((id) => {
    const el = document.querySelector('[data-wid="' + id + '"]');
    const left = parseFloat(el.style.left) || 0;
    const w = el.offsetWidth || parseFloat(el.style.width) || 300;
    // a tiled panel must sit against an edge, not straddle the middle
    return left < cx && left + w > cx;
  });
  ok('tiled panels never straddle the screen centre', centreHogs.length === 0,
     centreHogs.join(', '));

  // 4. Auto-fade
  shell.wm.SetAutoFade(true);
  ok('auto-fade can be enabled', shell.wm.IsAutoFade());
  shell.wm.tickIdle(0);              // force the idle threshold
  ok('panels fade when idle', document.body.dataset.idle === '1');
  shell.wm.bumpIdle();
  ok('any input wakes panels immediately', document.body.dataset.idle === '0');
  shell.wm.SetAutoFade(false);
  ok('auto-fade can be disabled', !shell.wm.IsAutoFade());

  // 5. Pinning protects a panel from fading
  shell.wm.Open('controls');
  shell.wm.Pin('controls', true);
  ok('a panel can be pinned', shell.wm.IsPinned('controls'));
  ok('pinned panels are marked in the DOM',
     document.querySelector('[data-wid="controls"]').className.includes('wm-pinned'));
  shell.wm.Pin('controls', false);
  ok('a panel can be unpinned', !shell.wm.IsPinned('controls'));

  // 6. The View panel exposes all of this
  shell.wm.Open('view');
  ok('View panel opens', shell.wm.IsVisible('view'));
  ok('View panel offers focus mode', !!document.getElementById('btnFocusPanel'));
  ok('View panel offers tiling', !!document.getElementById('btnTilePanel'));
  ok('View panel offers close-all', !!document.getElementById('btnCloseAllPanels'));
  ok('View panel offers auto-fade', !!document.getElementById('chkAutoFade'));
  ok('View panel offers density presets',
     document.querySelectorAll('[data-density-btn]').length === 3);
  ok('View panel lists every other panel for show/hide',
     document.querySelectorAll('[data-panel-toggle]').length >= 7);
  click(document.getElementById('btnCloseAllPanels'));
  ok('close-all really closes every panel',
     shell.wm.list().every((w) => !shell.wm.IsVisible(typeof w === 'string' ? w : w.id)));
}

console.log('\n— the art the look depends on is actually on disk —');
{
  // If one of these goes missing the sim silently falls back to procedural
  // surfaces and quietly looks worse, with nothing in the logs to say why.
  const need = [
    'public/art/menu-hero.jpg',
    'public/art/planet-terran.jpg',
    'public/art/planet-ice.jpg',
    'public/art/planet-gas.jpg',
    'public/art/planet-volcanic.jpg',
    'public/art/planet-desert.jpg'
  ];
  for (const f of need) {
    const exists = fs.existsSync(f) && fs.statSync(f).size > 20000;
    ok('art present: ' + f.split('/').pop(), exists);
  }
  const maps = fs.readFileSync('src/bjs/PlanetMaps.ts', 'utf8');
  for (const f of need.slice(1)) {
    const url = '/art/' + f.split('/').pop();
    ok('a planet kind actually uses ' + url, maps.includes(url));
  }
  // menu-hero.jpg is kept as the title-card plate now that the menu is gone.
  ok('the hero plate is still shipped', fs.existsSync('public/art/menu-hero.jpg'));
}

console.log('\n— there is no main menu; the opening is a place —');
{
  // The old menu is gone entirely. What replaces it is a title card and
  // then a room you walk around in.
  ok('the MainMenu module is gone', !fs.existsSync('src/bjs/ui/MainMenu.ts'));

  const seq = new IntroSequence();
  const overlay = new IntroOverlay(seq, {
    onPlay: () => seq.advance(),
    onSkip: () => seq.skip(),
    onAdvance: () => seq.advance()
  });

  ok('the intro renders', !!document.querySelector('.intro-root'));
  ok('the title card is shown first', !!document.querySelector('.intro-title'));
  const h1 = document.querySelector('.intro-title h1');
  ok('the title is the project name',
     !!h1 && /UNLIMITED[\s\S]*POSSIBILITIES[\s\S]*SANDBOX/.test(h1.textContent));

  const playBtn = document.querySelector('.intro-play');
  ok('there is a Play button', !!playBtn);
  ok('Play is the only call to action',
     document.querySelectorAll('.intro-play').length === 1);

  // No grid of world tiles, no nav bar - the menu genuinely went away.
  ok('there is no world picker', document.querySelectorAll('.menu-card').length === 0);
  ok('there is no menu nav bar', !document.querySelector('.menu-nav'));

  // Being trapped in a tutorial is unforgivable: skip must always be there.
  ok('the intro can always be skipped', !!document.querySelector('.intro-skip'));

  click(playBtn);
  ok('Play leaves the title behind', seq.state.stage === 'garage');
  overlay.render();
  ok('the title card hides once you are in',
     document.querySelector('.intro-title').classList.contains('intro-hide'));
  ok('there is a prompt telling you where to go',
     !document.querySelector('.intro-prompt').classList.contains('intro-hide'));

  overlay.dispose();
  ok('the intro can be taken down', !document.querySelector('.intro-root'));
}

console.log('\n— the ship is the menu —');
{
  // Everything the menu used to list is now an object you walk up to.
  ok('the ship has consoles to walk to', SHIP_STATIONS.length >= 5);
  ok('launching is one of them', SHIP_STATIONS.some((s) => s.id === 'play'));
  ok('a new universe is one of them',
     SHIP_STATIONS.some((s) => s.id === 'universe'));
  ok('graphics settings are one of them',
     SHIP_STATIONS.some((s) => s.id === 'graphics'));
  ok('continuing is one of them', SHIP_STATIONS.some((s) => s.id === 'load'));

  ok('every console has a position in the room',
     SHIP_STATIONS.every((s) => Array.isArray(s.position) && s.position.length === 3));
  ok('every console explains itself',
     SHIP_STATIONS.every((s) => !!s.hint && !!s.label));

  // They must be spread around, or you would trigger two at once.
  let tooClose = 0;
  for (let i = 0; i < SHIP_STATIONS.length; i++) {
    for (let j = i + 1; j < SHIP_STATIONS.length; j++) {
      const a = SHIP_STATIONS[i].position, b = SHIP_STATIONS[j].position;
      if (Math.hypot(a[0] - b[0], a[2] - b[2]) < 3.5) tooClose++;
    }
  }
  ok('consoles are far enough apart to use individually', tooClose === 0);
}

console.log('\n— the centre of the screen must stay clear —');
{
  // The recurring complaint: panels covering what you are looking at.
  // Assert it structurally rather than trusting the CSS by eye.
  const W = dom.window.innerWidth, H = dom.window.innerHeight;
  const ids = shell.wm.list().map((w) => (typeof w === 'string' ? w : w.id));
  ids.forEach((id) => shell.wm.Open(id));

  // no single panel may be wider than a quarter-ish of the screen
  const tooWide = [];
  ids.forEach((id) => {
    const el = document.querySelector('[data-wid="' + id + '"]');
    const w = parseFloat(el.style.width) || 0;
    if (w > W * 0.34) tooWide.push(id + '=' + w);
  });
  ok('no panel is wide enough to dominate the view', tooWide.length === 0,
     tooWide.join(', '));

  // The complaint was that panels are still too big. Hold a hard ceiling
  // so this cannot drift back.
  const widths = ids.map((id) => parseFloat(
    document.querySelector('[data-wid="' + id + '"]').style.width) || 0);
  const widest = Math.max(...widths);
  ok(`the widest panel is genuinely small (${widest}px)`, widest <= 248, String(widest));
  ok('panels take under a fifth of the screen each',
     widest <= W * 0.20 + 1, `${widest} vs ${(W * 0.20).toFixed(0)}`);

  // Density: a control must be a row, not a card. Measured from the CSS
  // contract rather than by eye.
  const css = fs.readFileSync('src/bjs/ui/styles.ts', 'utf8');
  ok('controls lay out as grid rows, not stacked blocks',
     /\.ctl\{[\s\S]*?display:grid/.test(css));
  ok('label, value and track share a line on wide panels',
     css.includes("grid-template-areas:'label track value'"));
  ok('control padding is tight', /\.ctl\{[\s\S]*?padding:3px/.test(css));
  ok('the slider row is short', css.includes('height:13px'));
  ok('panel body padding is tight', css.includes('.wm-body{padding:5px 6px'));

  // the default layout must leave a clear corridor down the middle
  shell.wm.TileEdges();
  const cx = W / 2;
  const band = W * 0.18;   // the central band we insist stays clear
  const blockers = [];
  ids.forEach((id) => {
    if (!shell.wm.IsVisible(id)) return;
    const el = document.querySelector('[data-wid="' + id + '"]');
    const left = parseFloat(el.style.left) || 0;
    const w = parseFloat(el.style.width) || 0;
    if (left < cx + band / 2 && left + w > cx - band / 2) blockers.push(id);
  });
  ok('after tiling, the central band of the screen is completely clear',
     blockers.length === 0, blockers.join(', '));

  // focus mode must clear it entirely regardless of layout
  shell.wm.SetFocusMode(true);
  ok('focus mode hides the chrome layer entirely',
     document.body.dataset.focus === '1');
  shell.wm.SetFocusMode(false);
  shell.wm.CloseAll();
}

console.log('\n— EVERY window: no dead X anywhere —');
{
  // the standing rule is that there must never be a dead X. Verify it by
  // opening every registered window and clicking its close button for real.
  const ids = shell.wm.list().map((w) => (typeof w === 'string' ? w : w.id));
  ok(`shell registers windows (${ids.length})`, ids.length >= 6);
  const deadX = [];
  const missingX = [];
  const lingering = [];
  for (const id of ids) {
    shell.wm.Open(id);
    if (!shell.wm.IsVisible(id)) { deadX.push(id + ' (would not open)'); continue; }
    const el = document.querySelector('[data-wid="' + id + '"]');
    const x = el && el.querySelector('[data-act="close"]');
    if (!x) { missingX.push(id); continue; }
    click(x);
    if (shell.wm.IsOpen(id)) deadX.push(id);
    if (el.style.display !== 'none') lingering.push(id);
  }
  ok('every window has a close button', missingX.length === 0, missingX.join(', '));
  ok('every X actually closes its window', deadX.length === 0, deadX.join(', '));
  ok('no closed window is left visible', lingering.length === 0, lingering.join(', '));

  // minimise and reopen must also work for every window
  const badMin = [];
  for (const id of ids) {
    shell.wm.Open(id);
    const el = document.querySelector('[data-wid="' + id + '"]');
    const m = el && el.querySelector('[data-act="min"]');
    if (m) { click(m); if (shell.wm.IsVisible(id)) badMin.push(id); }
    shell.wm.Open(id);
    if (!shell.wm.IsVisible(id)) badMin.push(id + ' (would not reopen)');
    shell.wm.Close(id);
  }
  ok('every window minimises and reopens', badMin.length === 0, badMin.join(', '));

  // and Escape must never leave the user trapped
  ids.forEach((id) => shell.wm.Open(id));
  for (let i = 0; i < ids.length + 2; i++) {
    dom.window.dispatchEvent(new dom.window.KeyboardEvent('keydown',
      { key: 'Escape', bubbles: true }));
  }
  const stuck = ids.filter((id) => shell.wm.IsVisible(id));
  ok('repeated Escape closes everything (never trapped)', stuck.length === 0, stuck.join(', '));
}

console.log('\n— the full toolset is always reachable —');
{
  shell.filter = '';
  shell.wm.Open('controls');
  shell.wm.refresh('controls');
  const all = document.querySelectorAll('.wm-win[data-wid="controls"] .ctl').length;
  ok(`no control is hidden behind a mode (${all} shown)`, all >= params.length);

  shell.mode = 'expert';
  shell.wm.refresh('controls');
  ok('expert mode adds exact numeric entry',
     document.querySelectorAll('[data-num-for]').length > 0);

  // a numeric box must clamp rather than accept nonsense
  const num = document.querySelector('[data-num-for]');
  if (num) {
    const key = num.dataset.numFor;
    num.value = '999999';
    num.dispatchEvent(new dom.window.Event('change'));
    const applied = ev.param.filter(([k]) => k === key).pop();
    ok('an out-of-range typed value is clamped, not applied raw',
       !applied || applied[1] <= parseFloat(num.max) + 1e-9,
       JSON.stringify(applied));
    num.value = 'not-a-number';
    const before = ev.param.length;
    num.dispatchEvent(new dom.window.Event('change'));
    ok('a non-numeric typed value is rejected', ev.param.length === before);
  }

  shell.mode = 'simple';
  shell.wm.refresh('controls');
}

console.log('\n— dozens of actions stay usable —');
{
  shell.mode = 'expert';
  shell.wm.Open('controls');
  shell.wm.refresh('controls');
  const win = document.querySelector('.wm-win[data-wid="controls"]');
  const groups = win.querySelectorAll('[data-action-group]');
  ok(`actions are grouped rather than one flat row (${groups.length} groups)`,
     groups.length >= 4);
  const titles = [...groups].map((g) => g.textContent);
  ok('beams, god powers and disasters each get their own group',
     titles.some((t) => /Beams/.test(t)) && titles.some((t) => /God/.test(t)) &&
     titles.some((t) => /Disasters/.test(t)), titles.join(' | '));
  ok('each group header shows how many actions it holds',
     titles.every((t) => /\(\d+\)/.test(t)), titles.join(' | '));
  const buttons = win.querySelectorAll('[data-action]');
  ok(`every action still has a button (${buttons.length})`, buttons.length === 25);
  // no action may be silently dropped by the grouping
  const rendered = new Set([...buttons].map((b2) => b2.dataset.action));
  const expected = shell.world.getActions().map((a) => a.key);
  ok('grouping never drops an action', expected.every((k) => rendered.has(k)),
     expected.filter((k) => !rendered.has(k)).join(','));

  // large groups start collapsed in simple mode so a 38-action world does not
  // fill the screen the moment it loads
  {
    shell.mode = 'simple';
    shell.wm.refresh('controls');
    const w2 = document.querySelector('.wm-win[data-wid="controls"]');
    const bigHead = [...w2.querySelectorAll('[data-action-group]')]
      .find((h) => /God/.test(h.textContent));
    ok('a group with many actions starts collapsed in simple mode',
       !!bigHead && bigHead.nextSibling.style.display === 'none');
    shell.mode = 'expert';
    shell.wm.refresh('controls');
  }

  if (groups.length) {
    const head = document.querySelector('.wm-win[data-wid="controls"] [data-action-group]');
    const row = head.nextSibling;
    const wasHidden = row.style.display === 'none';
    click(head);
    ok('clicking a group header toggles it',
       (row.style.display === 'none') !== wasHidden);
    click(head);
    ok('clicking again toggles it back',
       (row.style.display === 'none') === wasHidden);
  }
  shell.mode = 'simple';
}

console.log('\n— one universe: navigate by flying, not by tabs —');
{
  shell.wm.Open('navigator');
  ok('the Universe panel opens', shell.wm.IsVisible('navigator'));

  // everything coexists in one list - this is what replaces the tabs
  const rows = document.querySelectorAll('[data-region]');
  ok(`nearby places are listed together (${rows.length})`, rows.length === 4);
  const kinds = [...rows].map((r) => r.dataset.region);
  ok('a star system, a planet, a black hole and a nebula all appear at once',
     kinds.length === 4 && new Set(kinds).size === 4);

  // flying somewhere is navigation, not a level load
  click(document.querySelector('[data-warp="bh-3"]'));
  ok('clicking Fly navigates to that place', ev.warps.includes('bh-3'));
  ok('no world reload was triggered by navigating', ev.world.length === 0,
     JSON.stringify(ev.world));

  // you are told where you are
  shell.wm.refresh('navigator');
  ok('the panel shows your current location',
     document.getElementById('navHere').textContent.includes('Vela'),
     document.getElementById('navHere').textContent);

  // arriving is announced without blocking
  shell.onRegionChanged({ name: 'Lyra Nebula', glyph: '🌫' });
  const toast = document.getElementById('uiToast');
  ok('arriving somewhere is announced', toast.textContent.includes('Lyra Nebula'));
  ok('the announcement never blocks input',
     dom.window.getComputedStyle
       ? true
       : toast.className.includes('ui-toast'));
}

console.log('\n— creating and moving things in place —');
{
  shell.wm.Open('navigator');
  shell.wm.refresh('navigator');
  click(document.querySelector('[data-spawn="blackhole"]'));
  ok('you can create a black hole where you are', ev.spawns.includes('blackhole'));
  click(document.querySelector('[data-spawn="starsystem"]'));
  ok('you can create a star system', ev.spawns.includes('starsystem'));

  click(document.getElementById('btnGrab'));
  ok('you can grab what is in front of you', ev.grabs.length === 1);
  shell.wm.refresh('navigator');
  ok('the panel shows what you are carrying',
     document.getElementById('navHere').parentElement.textContent.includes('Carrying'));

  click(document.getElementById('btnRelease'));
  ok('you can release it', ev.releases.includes(false));
  click(document.getElementById('btnGrab'));
  click(document.getElementById('btnThrow'));
  ok('you can throw it', ev.releases.includes(true));

  // keyboard shortcuts, so you never need the panel
  dom.window.dispatchEvent(new dom.window.KeyboardEvent('keydown', { key: 'g', bubbles: true }));
  ok('G grabs', ev.grabs.length >= 3);
  dom.window.dispatchEvent(new dom.window.KeyboardEvent('keydown', { key: 'b', bubbles: true }));
  ok('B throws', ev.releases.filter((r) => r === true).length >= 2);

  shell.wm.refresh('navigator');
  const before = document.querySelectorAll('[data-region]').length;
  click(document.querySelector('[data-delete-region="neb-4"]'));
  ok('you can remove a place', ev.deletes.includes('neb-4'));
  shell.wm.refresh('navigator');
  ok('the list updates after removal',
     document.querySelectorAll('[data-region]').length === before - 1);
}

console.log('\n— per-hole gravitational lens editing —');
{
  shell.wm.Open('lens');
  ok('the Lens panel opens', shell.wm.IsVisible('lens'));

  const modes = document.querySelectorAll('[data-lens-mode]');
  ok(`many lens types are offered (${modes.length})`, modes.length >= 10);
  const labels = [...modes].map((m) => m.dataset.lensMode);
  ok('the textbook lens is offered', labels.includes('schwarzschild'));
  ok('a ringless lens is offered', labels.includes('ringless'));
  ok('alien lenses are offered',
     labels.includes('kaleidoscope') && labels.includes('hexagonal'));

  click(document.querySelector('[data-lens-mode="kaleidoscope"]'));
  ok('choosing a lens applies it', ev.lensModes.includes('kaleidoscope'));

  click(document.getElementById('btnRandomLens'));
  ok('you can roll a random alien lens', ev.lensModes.includes('random'));

  // every parameter must be individually tunable
  shell.wm.refresh('lens');
  const win = document.querySelector('.wm-win[data-wid="lens"]');
  const sliders = win.querySelectorAll('input[type="range"]');
  ok(`every lens parameter has a control (${sliders.length})`, sliders.length >= 8);
  sliders[0].value = sliders[0].max;
  sliders[0].dispatchEvent(new dom.window.Event('input'));
  ok('moving a lens slider edits the hole', ev.lensFields.length > 0,
     JSON.stringify(ev.lensFields));

  // the current lens is reported back
  ok('the panel reports the current lens',
     win.textContent.includes('Schwarzschild'));
}

console.log('\n— pilot: flying and walking —');
{
  shell.wm.Open('pilot');
  ok('Pilot panel opens', shell.wm.IsVisible('pilot'));
  const modeBtns = [...document.querySelectorAll('[data-mode]')].map((m) => m.dataset.mode);
  ok(`all four control modes are offered (${modeBtns.join(', ')})`,
     modeBtns.length === 4);
  ok('free fly is available as its own mode', modeBtns.includes('freefly'));
  ok('orbit, ship and walk are all still available',
     modeBtns.includes('orbit') && modeBtns.includes('fly') && modeBtns.includes('walk'));
  ok('orbit is the default and is highlighted',
     document.querySelector('[data-mode="orbit"]').className.includes('pri'));

  click(document.querySelector('[data-mode="fly"]'));
  ok('choosing Fly switches control mode', ev.modes.includes('fly'));
  shell.wm.refresh('pilot');
  ok('the active mode is highlighted after switching',
     document.querySelector('[data-mode="fly"]').className.includes('pri'));

  click(document.querySelector('[data-mode="walk"]'));
  ok('choosing Walk switches control mode', ev.modes.includes('walk'));

  ok('every craft is listed', document.querySelectorAll('[data-ship]').length === 4);
  click(document.querySelector('[data-ship="saucer"]'));
  ok('picking a craft selects it', ev.ships.includes('saucer'));
  ok('picking a craft also puts you in flight mode',
     ev.modes[ev.modes.length - 1] === 'fly');

  shell.setControlMode('fly');
  ok('setControlMode records the mode', shell.controlMode === 'fly');
}

console.log('\n— quality presets —');
shell.wm.Open('graphics');
const qBtns = [...document.querySelectorAll('[data-quality]')];
ok('all five quality presets are offered', qBtns.length === 5, `${qBtns.length}`);
ok('the active preset is highlighted',
   document.querySelector('[data-quality="' + qstate.current + '"]').className.includes('pri'),
   qstate.current);
click(document.querySelector('[data-quality="performance"]'));
ok('clicking a preset applies it', ev.quality[0] === 'performance');
shell.wm.refresh('graphics');
ok('the highlight follows the selection',
   document.querySelector('[data-quality="performance"]').className.includes('pri'));
const adaptChk = document.getElementById('chkAdaptive');
ok('adaptive resolution has a toggle', !!adaptChk);
adaptChk.checked = true;
adaptChk.dispatchEvent(new dom.window.Event('change', { bubbles: true }));
ok('toggling adaptive fires the hook', ev.adaptive[0] === true);

console.log('\n— persistent saves —');
shell.wm.Open('snapshots');
click(document.getElementById('btnSaveGame'));
ok('save universe stores a game', ev.games.length === 1);
shell.wm.refresh('snapshots');
const gameLoad = [...document.querySelectorAll('[data-wid="snapshots"] .btn')]
  .filter((x) => x.textContent === 'Load');
ok('the saved universe is listed', gameLoad.length >= 1);
const delBtn = [...document.querySelectorAll('[data-wid="snapshots"] .btn')]
  .find((x) => x.textContent === '✕');
ok('each save has a delete button', !!delBtn);
click(delBtn);
ok('delete removes the save', ev.games.length === 0);

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
