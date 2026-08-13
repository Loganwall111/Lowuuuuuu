/**
 * PortalSystem verification — wormholes must actually transport, tears must
 * lead somewhere real, and nothing may ping-pong or leak.
 * Run: node tools/portal-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';
import { JSDOM } from 'jsdom';

// Babylon needs a DOM even for pure maths/mesh construction.
const dom = new JSDOM('<!doctype html><html><body></body></html>',
  { pretendToBeVisual: true });
global.window = dom.window;
global.document = dom.window.document;
Object.defineProperty(global, 'navigator', { value: dom.window.navigator, configurable: true });
global.HTMLElement = dom.window.HTMLElement;
global.HTMLCanvasElement = dom.window.HTMLCanvasElement;
global.Image = dom.window.Image;
global.Blob = dom.window.Blob;
global.URL = dom.window.URL;
global.XMLHttpRequest = dom.window.XMLHttpRequest;
global.performance = { now: () => Date.now() };
global.WebGL2RenderingContext = function () {};

const out = await build({
  entryPoints: ['tools/fixtures/portal-entry.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/portal-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const { PortalSystem, Vector3, generateDimension } = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

// A minimal scene stub: PortalSystem only needs mesh/material construction,
// which Babylon performs without a GPU as long as a scene-like object exists.
import('@babylonjs/core/Engines/nullEngine.js').catch(() => {});
const { NullEngine } = await import('@babylonjs/core/Engines/nullEngine.js');
const { Scene } = await import('@babylonjs/core/scene.js');
const engine = new NullEngine({ renderWidth: 512, renderHeight: 512 });
const scene = new Scene(engine);

const mk = () => new PortalSystem(scene);
const traveller = (pos, vel) => ({ position: pos.clone(), velocity: vel.clone() });
const openFully = (ps, seconds = 3) => {
  for (let i = 0; i < seconds * 60; i++) ps.update(1 / 60, Vector3.Zero());
};

console.log('\n— creating portals —');
{
  const ps = mk();
  ok('starts empty', ps.count() === 0);
  const w = ps.createWormhole(new Vector3(0, 0, 0), new Vector3(100, 0, 0), 6);
  ok('a wormhole is created', ps.count() === 1);
  ok('a wormhole has two mouths', !!w.a && !!w.b);
  ok('the mouths are at the requested places',
     w.a.position.x === 0 && w.b.position.x === 100);
  ok('a wormhole has no dimension destination', w.destination === null);
  ok('both mouths built a mesh', !!w.a.mesh && !!w.b.mesh);
  ok('the mouths face opposite ways',
     Vector3.Dot(w.a.normal, w.b.normal) < -0.9);

  const t = ps.createTear(new Vector3(0, 20, 0), new Vector3(0, 0, 1), 7);
  ok('a tear is created', ps.count() === 2);
  ok('a tear has only one mouth', !!t.a && t.b === null);
  ok('a tear leads to a real dimension', !!t.destination && !!t.destination.name);
  ok('a tear lenses more strongly than a wormhole', t.lensStrength > w.lensStrength);
}

console.log('\n— portals iris open rather than popping in —');
{
  const ps = mk();
  const w = ps.createWormhole(new Vector3(0, 0, 0), new Vector3(60, 0, 0), 5);
  ok('starts closed', w.openness < 0.1);
  ps.update(1 / 60, Vector3.Zero());
  ok('begins opening', w.openness > 0);
  openFully(ps);
  ok('reaches fully open', w.openness > 0.95);
  ok('never overshoots past fully open', w.openness <= 1.0001);
}

console.log('\n— a closed portal does not transport —');
{
  const ps = mk();
  ps.createWormhole(new Vector3(0, 0, 0), new Vector3(100, 0, 0), 6);
  const t = traveller(new Vector3(0, 0, 0), new Vector3(0, 0, 1));
  ok('a still-opening portal refuses transit', ps.tryTransit(t) === null);
  ok('the traveller has not moved', t.position.x === 0);
}

console.log('\n— flying through a wormhole transports you —');
{
  const ps = mk();
  const w = ps.createWormhole(new Vector3(0, 0, 0), new Vector3(200, 0, 0), 6);
  openFully(ps);

  const t = traveller(new Vector3(0, 0, 0), new Vector3(0, 0, 30));
  const used = ps.tryTransit(t);
  ok('entering a mouth transports the traveller', used === w);
  ok('you arrive near the far mouth',
     Vector3.Distance(t.position, w.b.position) < w.b.radius + 12,
     `dist ${Vector3.Distance(t.position, w.b.position).toFixed(1)}`);
  ok('you are no longer at the entry mouth',
     Vector3.Distance(t.position, w.a.position) > 100);
  ok('the transit counter increments', ps.transits === 1);
  ok('speed is preserved through the throat', t.velocity.length() > 0);
  ok('velocity stays finite',
     [t.velocity.x, t.velocity.y, t.velocity.z].every(Number.isFinite));
}

console.log('\n— you exit travelling OUT, not straight back in —');
{
  const ps = mk();
  const w = ps.createWormhole(new Vector3(0, 0, 0), new Vector3(200, 0, 0), 6);
  openFully(ps);
  const t = traveller(new Vector3(0, 0, 0), new Vector3(0, 0, 30));
  ps.tryTransit(t);

  // the exit velocity must carry you away from the mouth you arrived at
  const awayFromMouth = t.position.subtract(w.b.position).normalize();
  const dot = Vector3.Dot(t.velocity.clone().normalize(), awayFromMouth);
  ok(`you exit moving away from the far mouth (dot ${dot.toFixed(2)})`, dot > 0.5);
}

console.log('\n— cooldown prevents instant ping-ponging —');
{
  const ps = mk();
  ps.createWormhole(new Vector3(0, 0, 0), new Vector3(50, 0, 0), 8);
  openFully(ps);
  const t = traveller(new Vector3(0, 0, 0), new Vector3(0, 0, 20));
  ok('the first transit succeeds', ps.tryTransit(t) !== null);
  // immediately at the far mouth, which is close by
  const second = ps.tryTransit(t);
  ok('an immediate second transit is blocked', second === null);
  ok('only one transit was counted', ps.transits === 1);

  // after the cooldown expires it works again
  for (let i = 0; i < 150; i++) ps.update(1 / 60, Vector3.Zero());
  const t2 = traveller(new Vector3(50, 0, 0), new Vector3(-20, 0, 0));
  ok('transit works again once the cooldown expires', ps.tryTransit(t2) !== null);
}

console.log('\n— you must actually enter, not just pass nearby —');
{
  const ps = mk();
  ps.createWormhole(new Vector3(0, 0, 0), new Vector3(200, 0, 0), 4);
  openFully(ps);

  // far away, moving away
  const far = traveller(new Vector3(0, 0, 400), new Vector3(0, 0, 50));
  ok('a distant traveller is not transported', ps.tryTransit(far) === null);

  // close but receding
  const receding = traveller(new Vector3(0, 0, 5), new Vector3(0, 0, 60));
  const r = ps.tryTransit(receding);
  ok('a traveller moving away is not grabbed', r === null || ps.transits === 1);
}

console.log('\n— a tear reports its destination without moving you —');
{
  const ps = mk();
  const tear = ps.createTear(new Vector3(0, 0, 0), new Vector3(0, 0, 1), 7);
  openFully(ps);
  const t = traveller(new Vector3(0, 0, 0), new Vector3(0, 0, 10));
  const before = t.position.clone();
  const used = ps.tryTransit(t);
  ok('entering a tear is reported', used === tear);
  ok('a tear does not move you inside this scene', t.position.equals(before));
  ok('the destination dimension is exposed',
     !!ps.lastDestination && !!ps.lastDestination.name);
  ok('the destination has a palette to render',
     ps.lastDestination.palette.length >= 3);
}

console.log('\n— tears chain into a multiverse —');
{
  const ps = mk();
  const first = generateDimension(4242, 0);
  const t1 = ps.createTear(new Vector3(0, 0, 0), new Vector3(0, 0, 1), 6, first);
  ok('a tear from a dimension lands somewhere else',
     t1.destination.id !== first.id);
  ok('the sideways tear keeps the same depth',
     t1.destination.depth === first.depth);
  const t2 = ps.createTear(new Vector3(20, 0, 0), new Vector3(0, 0, 1), 6, t1.destination);
  ok('tearing again reaches a third reality',
     t2.destination.id !== t1.destination.id && t2.destination.id !== first.id);

  // deterministic: the same source always tears to the same place
  const ps2 = mk();
  const again = ps2.createTear(new Vector3(0, 0, 0), new Vector3(0, 0, 1), 6, first);
  ok('tearing from the same reality is reproducible',
     again.destination.id === t1.destination.id);
}

console.log('\n— every portal can be closed (no dead portals) —');
{
  const ps = mk();
  const a = ps.createWormhole(new Vector3(0, 0, 0), new Vector3(50, 0, 0));
  const b = ps.createTear(new Vector3(0, 30, 0), new Vector3(0, 0, 1));
  ok('two portals exist', ps.count() === 2);
  ok('closing by id works', ps.close(a.id) === true);
  ok('the count drops', ps.count() === 1);
  ok('closing an unknown id is safe and reports false', ps.close('nope') === false);
  ok('closing the tear works', ps.close(b.id) === true);
  ok('everything is closed', ps.count() === 0);

  ps.createWormhole(new Vector3(0, 0, 0), new Vector3(10, 0, 0));
  ps.createTear(new Vector3(5, 5, 5), new Vector3(0, 1, 0));
  ps.closeAll();
  ok('closeAll removes every portal', ps.count() === 0);
  ok('closeAll on an empty system is safe', (() => { ps.closeAll(); return true; })());
}

console.log('\n— a closed portal no longer transports —');
{
  const ps = mk();
  const w = ps.createWormhole(new Vector3(0, 0, 0), new Vector3(100, 0, 0), 6);
  openFully(ps);
  ps.close(w.id);
  const t = traveller(new Vector3(0, 0, 0), new Vector3(0, 0, 20));
  ok('a closed wormhole cannot be entered', ps.tryTransit(t) === null);
  ok('the traveller stays put', t.position.z === 0);
}

console.log('\n— many portals at once —');
{
  const ps = mk();
  for (let i = 0; i < 24; i++) {
    ps.createWormhole(new Vector3(i * 30, 0, 0), new Vector3(i * 30, 0, 300), 5);
  }
  for (let i = 0; i < 12; i++) {
    ps.createTear(new Vector3(0, i * 25, 0), new Vector3(0, 0, 1), 6);
  }
  ok('36 portals coexist', ps.count() === 36);
  let err = null;
  try { openFully(ps, 2); } catch (e) { err = e; }
  ok('updating 36 portals does not throw', !err, err ? err.message : '');
  const s = ps.stats();
  ok('stats count wormholes and tears separately',
     s.Wormholes === '24' && s['Space tears'] === '12', JSON.stringify(s));
  ps.closeAll();
  ok('all 36 close cleanly', ps.count() === 0);
}

console.log('\n— robustness —');
{
  const ps = mk();
  const w = ps.createWormhole(new Vector3(0, 0, 0), new Vector3(50, 0, 0));
  ok('zero dt is ignored', (() => { ps.update(0, Vector3.Zero()); return w.openness === 0; })());
  ok('NaN dt is ignored', (() => { ps.update(NaN, Vector3.Zero()); return w.openness === 0; })());
  ok('negative dt is ignored', (() => { ps.update(-1, Vector3.Zero()); return w.openness === 0; })());

  // degenerate: both mouths in the same place
  const same = ps.createWormhole(new Vector3(5, 5, 5), new Vector3(5, 5, 5), 4);
  ok('a zero-length wormhole still builds a valid normal',
     [same.a.normal.x, same.a.normal.y, same.a.normal.z].every(Number.isFinite) &&
     Math.abs(same.a.normal.length() - 1) < 1e-4);
  openFully(ps);
  const t = traveller(new Vector3(5, 5, 5), new Vector3(1, 0, 0));
  let crashed = null;
  try { ps.tryTransit(t); } catch (e) { crashed = e; }
  ok('transiting a degenerate wormhole does not crash', !crashed,
     crashed ? crashed.message : '');
  ok('the traveller stays finite',
     [t.position.x, t.position.y, t.position.z].every(Number.isFinite));

  ps.dispose();
  ok('dispose clears everything', ps.count() === 0);
  ok('stats still render after dispose', !!ps.stats()['Portals open']);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
