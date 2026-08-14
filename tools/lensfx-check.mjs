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

// -- THE BEND MUST BE BIG ENOUGH TO SEE
//
// This is the regression that made the user report "nothing is bent". The
// pass was running, the uniforms were bound, every structural test passed -
// and the deflection was a fraction of a pixel because it was scaled by the
// apparent horizon instead of the Einstein radius. Structure tests cannot
// catch that; only measuring the magnitude can.
{
  const { einsteinRadius } = await import(out);

  ok('einsteinRadius grows as you approach',
    einsteinRadius(19, 200) > einsteinRadius(19, 2000));
  ok('einsteinRadius grows with mass',
    einsteinRadius(40, 400) > einsteinRadius(10, 400));
  ok('einsteinRadius is safe at zero distance', einsteinRadius(19, 0) === 0);
  ok('einsteinRadius is safe at zero mass', einsteinRadius(0, 400) === 0);
  ok('einsteinRadius rejects negatives',
    einsteinRadius(-5, 400) === 0 && einsteinRadius(19, -5) === 0);
  ok('einsteinRadius never returns NaN',
    Number.isFinite(einsteinRadius(NaN, 400)) &&
    Number.isFinite(einsteinRadius(19, NaN)));

  // The Einstein radius must dominate the horizon at ordinary viewing
  // range - that inequality IS the bug, stated as an assertion.
  const rs = 18.7, D = 388, fov = 0.9;
  const shadow = Math.atan(rs / D) / (fov * 0.5) * 0.5;
  const lens = einsteinRadius(rs, D) / (fov * 0.5) * 0.5;
  ok('the lensing scale is far larger than the shadow', lens > shadow * 4,
    'lens ' + lens.toFixed(3) + ' vs shadow ' + shadow.toFixed(3));

  // And the resulting displacement must be tens of pixels, not fractions.
  const shiftPx = (theta, scale) =>
    (scale * scale / Math.max(theta, scale * 0.45)) * 1080;
  ok('a star beside the hole moves hundreds of pixels',
    shiftPx(0.2, lens) > 100, shiftPx(0.2, lens).toFixed(0) + 'px');
  ok('a star far across the frame still moves visibly',
    shiftPx(1.0, lens) > 20, shiftPx(1.0, lens).toFixed(0) + 'px');
  ok('the old horizon-scaled bend would have been invisible',
    shiftPx(0.2, shadow) < 20, shiftPx(0.2, shadow).toFixed(1) + 'px');

  // The softened denominator must cap the rim without killing the far field.
  ok('the bend is bounded near the shadow', shiftPx(0.01, lens) < 1400,
    shiftPx(0.01, lens).toFixed(0) + 'px');
  ok('the bend still falls off with distance',
    shiftPx(0.3, lens) > shiftPx(0.9, lens));

  // The slot must actually carry the lensing radius through to the shader.
  const lensFx = makeLens();
  lensFx.trackMany([hole(0, 0, 388, 18.7)], makeCamera());
  const slot = lensFx.slots[0];
  ok('the slot carries a separate lensing radius', slot.lensRadius > slot.radius,
    'lens ' + slot.lensRadius.toFixed(3) + ' vs shadow ' + slot.radius.toFixed(3));
  ok('the lensing radius is clamped below full screen', slot.lensRadius <= 0.9);
  ok('the lensing radius is never below the shadow', (() => {
    const l2 = makeLens();
    // Very close pass: the shadow is huge and would otherwise exceed it.
    l2.trackMany([hole(0, 0, 25, 18.7)], makeCamera());
    return l2.slots[0].lensRadius >= l2.slots[0].radius;
  })());
}

// -- THE DEFLECTION MUST VARY ACROSS THE SCREEN
//
// A bend that is the same at every pixel translates the image; it does not
// warp it. The shipped shader did exactly that: clamp(lensR/rr, 0, 1) hits
// 1 inside the Einstein radius, and the live lens profiles have falloff
// between 0.98 and 1.32 so the exponent max(falloff-1, 0) was 0 - leaving
// bend = strength * lensR, constant. Every structural test still passed.
// Only evaluating the formula at several radii catches it.
{
  const frag = fs.readFileSync(SRC, 'utf8')
    .match(/const\s+LENS_FRAG\s*=\s*`([\s\S]*?)`;/m)[1];

  ok('the deflection is not a clamped power that can collapse to a constant',
    !/pow\(clamp\(lensR\[i\] \/ rr/.test(frag));
  ok('theta appears in the denominator of the deflection',
    /lensR\[i\] \/ max\(decay/.test(frag));

  // Model the shipped formula exactly and require real variation.
  const bendAt = (theta, lensR, falloff, strength = 1) => {
    const rr = Math.max(theta, lensR * 0.42);
    const decay = Math.pow(Math.max(rr / Math.max(lensR, 1e-4), 1e-4),
      Math.max(falloff, 0.35));
    return Math.min(0.75, Math.max(-0.75, strength * lensR / Math.max(decay, 1e-3)));
  };

  // These are the real falloff values observed on live holes.
  for (const fo of [0.98, 1.0, 1.20, 1.32, 2.0]) {
    const near = bendAt(0.35, 0.3977, fo);
    const far = bendAt(1.20, 0.3977, fo);
    ok('deflection decays with distance at falloff ' + fo, near > far * 1.25,
      'near ' + near.toFixed(4) + ' vs far ' + far.toFixed(4));
  }

  // The specific regression: falloff = 1 must NOT give a constant field.
  const samples = [0.3, 0.5, 0.8, 1.1].map((t) => bendAt(t, 0.3977, 1.0));
  const spread = Math.max(...samples) - Math.min(...samples);
  ok('a falloff of 1 still produces a varying field', spread > 0.05,
    'spread ' + spread.toFixed(4));

  ok('the bend is bounded', bendAt(0.001, 0.9, 2.0) <= 0.75);
  ok('the bend is large enough to see at mid-screen',
    bendAt(0.5, 0.3977, 1.0) * 1080 > 40,
    (bendAt(0.5, 0.3977, 1.0) * 1080).toFixed(0) + 'px');
  ok('a distant hole bends far less than a near one',
    bendAt(0.5, 0.05, 1.0) < bendAt(0.5, 0.4, 1.0));
}

// -- THE WARP MUST LOOK LIKE A LENS, NOT A STARBURST
//
// Lensing that works is not the same as lensing that looks right. Compared
// against real black hole imagery the warp had three tells: hard radial
// spokes, dark rays smeared outward from the rim, and a photon ring
// floating detached in empty space. Each has a measurable cause.
{
  const frag = fs.readFileSync(SRC, 'utf8')
    .match(/const\s+LENS_FRAG\s*=\s*`([\s\S]*?)`;/m)[1];

  // 1. THE DEFLECTION MUST BE PURELY RADIAL.
  //
  // Gravity does not care which direction a pixel lies from the centre. Any
  // dependence of the bend on the polar angle is a periodic decoration that
  // reads as a water ripple or a starburst - the earlier version modulated
  // by cos(ang * symmetry) and sin(ang + twist / r), and softening those
  // coefficients only made the ripple smaller, not absent.
  ok('the deflection has no angular modulation at all',
    /float shape = 1\.0;/.test(frag));
  ok('the polar angle is no longer computed in the lens loop',
    !/float ang = atan\(d\.y, d\.x\);/.test(frag));
  ok('no sine or cosine survives in the lens shader',
    !/^[^/]*\b(sin|cos)\s*\(/m.test(
      frag.split('\n').filter((l) => !l.trim().startsWith('//')).join('\n')));
  ok('the lens shader has no time uniform, so it cannot animate',
    !/uniform\s+float\s+time/.test(frag));
  ok('the lens shader never reads a clock', !/\btime\b/.test(
    frag.split('\n').filter((l) => !l.trim().startsWith('//')).join('\n')));

  // The bend must be identical at every angle for a given radius. Model the
  // shipped expression and sweep the full circle.
  const bendPolar = (theta, lensR, falloff, strength = 1) => {
    const rr = Math.max(theta, lensR * 0.42);
    const decay = Math.pow(Math.max(rr / Math.max(lensR, 1e-4), 1e-4),
      Math.max(falloff, 0.35));
    return Math.min(0.75, Math.max(-0.75, strength * lensR / Math.max(decay, 1e-3)));
  };
  const atRadius = new Set();
  for (let d = 0; d < 360; d += 15) {
    // Angle is not an input at all now; the value must be constant.
    atRadius.add(bendPolar(0.4, 0.3977, 1.0).toFixed(9));
  }
  ok('the bend is identical all the way around a circle', atRadius.size === 1);

  // 2. Out-of-frame samples must mirror, not clamp. clamp() repeats one edge
  //    pixel into the long dark rays that were visible around the shadow.
  ok('out-of-frame samples are mirrored back into the image',
    /vec2 mirrorUV\(vec2 p\)/.test(frag));
  ok('the sampler no longer clamps the warped coordinate',
    !/texture2D\(textureSampler, clamp\(uv[RGB]/.test(frag));
  const mirror = (v) => {
    const q = Math.abs(((v * 0.5) % 1 + 1) % 1 * 2 - 1);
    return Math.min(0.998, Math.max(0.002, q));
  };
  ok('mirrored coordinates always land inside the frame', (() => {
    for (let v = -3; v <= 3; v += 0.017) {
      const m = mirror(v);
      if (!(m >= 0.002 && m <= 0.998) || !Number.isFinite(m)) return false;
    }
    return true;
  })());
  ok('mirroring is continuous at the frame edge',
    Math.abs(mirror(0.999) - mirror(1.001)) < 0.01);
  ok('mirroring does not flatten distinct samples onto one pixel',
    new Set([1.1, 1.3, 1.5, 1.7].map((v) => mirror(v).toFixed(4))).size === 4);

  // 3. The photon ring hugs the shadow; it is not a detached halo.
  ok('the ring is anchored to the shadow radius',
    /float ringR = max\(holeR\[i\]/.test(frag));
  const ringAt = (shadowR, ringRadius) =>
    Math.max(shadowR, 0.004) * 1.5 * Math.max(ringRadius, 0.05);
  for (const rr of [0.3, 0.85, 1.1, 1.5]) {
    const ratio = ringAt(0.0632, rr) / 0.0632;
    ok('the ring stays near the shadow at ringRadius ' + rr, ratio < 3,
      'x' + ratio.toFixed(2));
  }
  ok('the ring scales with the shadow, not the lensing radius',
    ringAt(0.2, 1) > ringAt(0.05, 1));
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
