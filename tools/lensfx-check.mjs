/**
 * Universal lensing checks.
 *
 *  1. The post-process GLSL parses with a real GLSL parser, and every
 *     uniform the shader declares is actually bound from TypeScript.
 *  2. trackMany() projects real holes with real Babylon math: on-screen
 *     selection, behind-camera rejection, nearest-first slot budgeting.
 *
 * The point of (2) is that "universal" lensing means several holes bend the
 * same frame. A single-lens pass looks fine in a screenshot of one hole and
 * is obviously wrong the moment two are visible.
 *
 * Run: node tools/lens-check.mjs
 */
import { parser } from '@shaderfrog/glsl-parser';
import { execFileSync } from 'child_process';
import fs from 'fs';

let pass = 0, fail = 0;
const ok = (name, cond, extra) => {
  if (cond) { pass++; }
  else { fail++; console.log('FAIL: ' + name + (extra ? ' — ' + extra : '')); }
};

const SRC = 'src/bjs/systems/LensFX.ts';
if (!fs.existsSync(SRC)) {
  console.log('FAIL: ' + SRC + ' is missing');
  console.log('0 passed, 1 failed');
  process.exit(1);
}
const src = fs.readFileSync(SRC, 'utf8');

// ---------------------------------------------------------------- 1. GLSL
const m = src.match(/const\s+LENS_FRAG\s*=\s*`([\s\S]*?)`;/m);
ok('LENS_FRAG source found', !!m);

if (m) {
  const frag = m[1];

  try {
    parser.parse(frag);
    ok('lens fragment shader parses', true);
  } catch (e) {
    ok('lens fragment shader parses', false, e.message);
  }

  // Declared uniforms, array or scalar.
  const declared = [...frag.matchAll(/^\s*uniform\s+\w+\s+([A-Za-z_]\w*)\s*(\[|;)/gm)]
    .map((x) => x[1]);

  ok('shader declares a hole array', /uniform\s+vec2\s+holeUV\s*\[/.test(frag));
  ok('shader has a per-slot enable flag', declared.includes('holeOn'));
  ok('MAX_HOLES is a compile-time constant',
    /const\s+int\s+MAX_HOLES\s*=\s*(\d+)\s*;/.test(frag));

  // GLSL ES 1.00 forbids indexing a uniform array with a non-constant
  // expression on many drivers; the loop counter is the allowed exception.
  const bound = frag.match(/for\s*\(\s*int\s+i\s*=\s*0\s*;\s*i\s*<\s*MAX_HOLES/);
  ok('slots are walked by a constant-bounded loop', !!bound);

  // Every uniform must be written from TypeScript, or it silently stays 0
  // and the lens quietly does nothing.
  for (const u of declared) {
    if (u === 'textureSampler') continue;
    const setInTs = new RegExp("set(?:Array[23]?|Float[234]?)\\('" + u + "'").test(src);
    ok('uniform bound from TS: ' + u, setInTs);
  }

  // The shader must sample the framebuffer it was handed - that is what
  // makes real planets and nebulae bend, rather than a procedural sky.
  ok('samples the rendered scene', /texture2D\(textureSampler/.test(frag));

  // Deflections must accumulate, not overwrite.
  ok('deflections from several holes add', /totalOff\s*\+=/.test(frag));

  // The shadow floor that stops a dead black rectangle must survive.
  ok('shadow keeps a lensed trace (never a flat fill)',
    /mix\(col,\s*col\s*\*\s*0\.06/.test(frag));

  const maxHoles = Number(frag.match(/const\s+int\s+MAX_HOLES\s*=\s*(\d+)/)[1]);
  const maxLenses = Number(src.match(/MAX_LENSES\s*=\s*(\d+)/)[1]);
  ok('MAX_LENSES matches the shader\'s MAX_HOLES', maxHoles === maxLenses,
    'shader ' + maxHoles + ' vs ts ' + maxLenses);
}

// ------------------------------------------------------------- 2. runtime
const out = '/tmp/lensfx-' + Date.now() + '.mjs';
try {
  execFileSync('npx', ['esbuild', SRC, '--bundle', '--format=esm',
    '--platform=node', '--log-level=error', '--outfile=' + out], { stdio: 'pipe' });
} catch (e) {
  console.log('FAIL: could not bundle LensFX — ' + (e.stderr?.toString() || e.message));
  console.log(pass + ' passed, ' + (fail + 1) + ' failed');
  process.exit(1);
}

const { LensFX, MAX_LENSES } = await import(out);
const { Vector3 } = await import('@babylonjs/core/Maths/math.vector.js');
const { Matrix } = await import('@babylonjs/core/Maths/math.vector.js');

/**
 * A camera at the origin looking down +Z, with a real perspective matrix.
 * trackMany reads only these members, so a full Babylon camera is not
 * needed and would drag in an engine.
 */
function makeCamera(pos = new Vector3(0, 0, 0)) {
  const target = pos.add(new Vector3(0, 0, 1));
  const view = Matrix.LookAtLH(pos, target, new Vector3(0, 1, 0));
  const proj = Matrix.PerspectiveFovLH(0.9, 16 / 9, 0.05, 4000);
  return {
    position: pos,
    fov: 0.9,
    getViewMatrix: () => view,
    getProjectionMatrix: () => proj
  };
}

function makeLens() {
  const lens = new LensFX();
  // Stand in for attach(): trackMany needs a scene for the engine size and
  // a compiled pass. Private fields are ordinary properties at runtime.
  lens.scene = { getEngine: () => ({ getRenderWidth: () => 1920, getRenderHeight: () => 1080 }) };
  lens.pp = {};
  lens.compileWatch = -1;      // "already verified good"
  return lens;
}

const hole = (x, y, z, horizon = 40) =>
  ({ center: new Vector3(x, y, z), horizon, profile: null });

// -- a hole dead ahead is tracked
{
  const lens = makeLens();
  lens.trackMany([hole(0, 0, 600)], makeCamera());
  ok('hole ahead is lensed', lens.isActive === true);
  ok('one hole fills one slot', lens.activeCount === 1);
  ok('centred hole lands mid-screen',
    Math.abs(lens.slots[0].uv.x - 0.5) < 0.01 && Math.abs(lens.slots[0].uv.y - 0.5) < 0.01,
    JSON.stringify(lens.slots[0].uv));
}

// -- a hole behind the camera bends nothing
{
  const lens = makeLens();
  lens.trackMany([hole(0, 0, -600)], makeCamera());
  ok('hole behind camera is ignored', lens.isActive === false && lens.activeCount === 0);
}

// -- THE POINT: several holes at once
{
  const lens = makeLens();
  lens.trackMany([hole(-120, 0, 600), hole(120, 0, 600), hole(0, 90, 700)], makeCamera());
  ok('three visible holes all lens', lens.activeCount === 3);
  const xs = lens.slots.map((s) => s.uv.x);
  ok('the holes occupy distinct screen positions', new Set(xs.map((v) => v.toFixed(3))).size === 3);
}

// -- more holes than slots: nearest wins
{
  const lens = makeLens();
  const many = [];
  for (let i = 0; i < MAX_LENSES + 4; i++) many.push(hole(0, 0, 400 + i * 300));
  lens.trackMany(many, makeCamera());
  ok('slot budget is respected', lens.activeCount === MAX_LENSES);
  const radii = lens.slots.map((s) => s.radius);
  const sortedDesc = [...radii].sort((a, b) => b - a);
  ok('nearest holes are the ones kept',
    JSON.stringify(radii) === JSON.stringify(sortedDesc), JSON.stringify(radii));
}

// -- apparent size grows as you close in
{
  const far = makeLens(); far.trackMany([hole(0, 0, 2000)], makeCamera());
  const near = makeLens(); near.trackMany([hole(0, 0, 200)], makeCamera());
  ok('closer hole has a bigger lens', near.slots[0].radius > far.slots[0].radius,
    near.slots[0].radius + ' vs ' + far.slots[0].radius);
  ok('lens radius stays clamped', near.slots[0].radius <= 1.5);
}

// -- off-screen holes are dropped, but only well off-screen
{
  const lens = makeLens();
  lens.trackMany([hole(9000, 0, 300)], makeCamera());
  ok('far off-screen hole is dropped', lens.activeCount === 0);
}

// -- clear() really clears
{
  const lens = makeLens();
  lens.trackMany([hole(0, 0, 600)], makeCamera());
  lens.clear();
  ok('clear() empties every slot', lens.activeCount === 0 && lens.isActive === false);
}

// -- a stale slot must not survive a frame with fewer holes
{
  const lens = makeLens();
  lens.trackMany([hole(-100, 0, 600), hole(100, 0, 600)], makeCamera());
  lens.trackMany([hole(-100, 0, 600)], makeCamera());
  ok('slots do not leak between frames', lens.activeCount === 1);
}

// -- single-hole track() still works (the old call site)
{
  const lens = makeLens();
  lens.track(new Vector3(0, 0, 600), 40, makeCamera(), null);
  ok('legacy track() still lenses', lens.isActive === true && lens.activeCount === 1);
}

// -- garbage in, no crash
{
  const lens = makeLens();
  const cam = makeCamera();
  for (const bad of [null, undefined, [], [null], [undefined],
                     [{ center: null, horizon: 4, profile: null }],
                     [{ center: new Vector3(NaN, NaN, NaN), horizon: 40, profile: null }],
                     [{ center: new Vector3(0, 0, 600), horizon: NaN, profile: null }],
                     [{ center: new Vector3(0, 0, 600), horizon: 0, profile: null }]]) {
    try {
      lens.trackMany(bad, cam);
      ok('survives bad input: ' + JSON.stringify(bad ?? null), true);
    } catch (e) {
      ok('survives bad input: ' + JSON.stringify(bad ?? null), false, e.message);
    }
    ok('bad input never produces a NaN lens',
      lens.slots.every((s) => Number.isFinite(s.radius) &&
        Number.isFinite(s.uv.x) && Number.isFinite(s.uv.y)));
  }
}

// -- no scene attached: silent no-op, not a throw
{
  const lens = new LensFX();
  try {
    lens.trackMany([hole(0, 0, 600)], makeCamera());
    ok('detached lens is a no-op', lens.isActive === false);
  } catch (e) {
    ok('detached lens is a no-op', false, e.message);
  }
}

// -- stats report the multi-hole count
{
  const lens = makeLens();
  lens.trackMany([hole(-120, 0, 600), hole(120, 0, 600)], makeCamera());
  const st = lens.stats();
  ok('stats expose how many holes are lensing', st['Holes lensing'] === '2', JSON.stringify(st));
}

try { fs.unlinkSync(out); } catch { /* fine */ }

console.log(pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
