/**
 * polish-check — the AAA pass.
 *
 * Covers the round of fixes driven by playing the thing: camera roll drift,
 * the starfield pole warp, warp streaks that never triggered, the garage
 * door and its people, the play button, the branding, and the false
 * "screen is black" report that appeared over a working scene.
 */

import { readFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import { mkdtempSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

let pass = 0, fail = 0;
const ok = (name, cond, detail) => {
  if (cond) { pass++; console.log('  PASS  ' + name); }
  else { fail++; console.log('  FAIL  ' + name + (detail ? '  -> ' + detail : '')); }
};

const vehicle = readFileSync(new URL('../src/bjs/systems/VehicleSystem.ts', import.meta.url), 'utf8');
const planetary = readFileSync(new URL('../src/bjs/worlds/PlanetaryWorld.ts', import.meta.url), 'utf8');
const warp = readFileSync(new URL('../src/bjs/systems/WarpSystem.ts', import.meta.url), 'utf8');
const garage = readFileSync(new URL('../src/bjs/worlds/GarageWorld.ts', import.meta.url), 'utf8');
const intro = readFileSync(new URL('../src/bjs/ui/IntroOverlay.ts', import.meta.url), 'utf8');
const shell = readFileSync(new URL('../src/bjs/ui/Shell.ts', import.meta.url), 'utf8');
const app = readFileSync(new URL('../src/bjs/App.ts', import.meta.url), 'utf8');
const postfx = readFileSync(new URL('../src/bjs/PostFX.ts', import.meta.url), 'utf8');

console.log('\npolish: controls, sky, garage, and the false alarm');

/* ---------------- 1. looking around must not drift --------------------- */

ok('free-fly no longer compounds a delta quaternion',
   !/this\.orientation = this\.orientation\.multiply\(rot\)[\s\S]{0,200}freefly/.test(vehicle));
ok('free-fly tracks yaw and pitch as angles',
   /this\.yaw \+= i\.yaw \* 1\.6 \* dt/.test(vehicle) &&
   /this\.pitch = Math\.max\(-1\.5533/.test(vehicle));
ok('pitch is clamped short of vertical so you cannot flip over',
   /1\.5533/.test(vehicle));
ok('roll is deliberate rather than accumulated',
   /Deliberate roll/.test(vehicle));
ok('the orientation is rebuilt from angles each frame',
   /Quaternion\.RotationYawPitchRoll\(this\.yaw, this\.pitch, this\.roll\)/.test(vehicle));
ok('switching modes decomposes the current orientation',
   /toEulerAngles\(\)/.test(vehicle) && /m === 'freefly'/.test(vehicle));
ok('entering free-fly levels the horizon', /this\.roll = 0/.test(vehicle));

// The actual maths: repeatedly composing yaw and pitch injects roll, which is
// the bug. Prove it, then prove the euler form does not.
{
  const q = { x: 0, y: 0, z: 0, w: 1 };
  const mul = (a, b) => ({
    x: a.w * b.x + a.x * b.w + a.y * b.z - a.z * b.y,
    y: a.w * b.y - a.x * b.z + a.y * b.w + a.z * b.x,
    z: a.w * b.z + a.x * b.y - a.y * b.x + a.z * b.w,
    w: a.w * b.w - a.x * b.x - a.y * b.y - a.z * b.z
  });
  const fromYPR = (y, p, r) => {
    const hy = y / 2, hp = p / 2, hr = r / 2;
    const cy = Math.cos(hy), sy = Math.sin(hy);
    const cp = Math.cos(hp), sp = Math.sin(hp);
    const cr = Math.cos(hr), sr = Math.sin(hr);
    return { x: cy * sp * cr + sy * cp * sr, y: sy * cp * cr - cy * sp * sr,
             z: cy * cp * sr - sy * sp * cr, w: cy * cp * cr + sy * sp * sr };
  };
  // Simulate 400 frames of simultaneous yaw+pitch, the old way.
  let acc = { x: 0, y: 0, z: 0, w: 1 };
  for (let i = 0; i < 400; i++) acc = mul(acc, fromYPR(0.01, 0.007, 0));
  // Extract roll from the accumulated quaternion.
  const sinr = 2 * (acc.w * acc.z + acc.x * acc.y);
  const cosr = 1 - 2 * (acc.y * acc.y + acc.z * acc.z);
  const drift = Math.abs(Math.atan2(sinr, cosr));
  ok('composing rotations really does inject roll (the bug)',
     drift > 0.05, drift.toFixed(4) + ' rad of unrequested roll');

  // Measuring "roll" by extracting an euler angle is unreliable near the
  // pitch limit, where the decomposition trades yaw for roll. What actually
  // matters visually is whether the camera's right-hand axis stays
  // horizontal - that IS the horizon being level - so measure that instead.
  const rightVector = (q) => {
    // First column of the rotation matrix: the local +X axis in world space.
    const { x, y, z, w } = q;
    return {
      x: 1 - 2 * (y * y + z * z),
      y: 2 * (x * y + z * w),
      z: 2 * (x * z - y * w)
    };
  };

  const CLAMP = 1.5533;
  for (const [yaw, pitch] of [[0.4, 0.3], [4.0, 1.2], [12.0, -0.9], [1.0, CLAMP]]) {
    const r = rightVector(fromYPR(yaw, Math.max(-CLAMP, Math.min(CLAMP, pitch)), 0));
    ok(`the horizon stays level at yaw ${yaw}, pitch ${pitch}`,
       Math.abs(r.y) < 1e-9, 'right.y = ' + r.y.toExponential(2));
  }

  // The old accumulating form tilts the horizon, which is the actual defect.
  const rAcc = rightVector(acc);
  ok('the old accumulating form tilted the horizon',
     Math.abs(rAcc.y) > 0.01, 'right.y = ' + rAcc.y.toFixed(4));
}

/* ---------------- 2. the sky ------------------------------------------- */

// There is no sky object at all any more.
//
// The wedges the user kept seeing were not UV seams: they were the sky
// sphere's own triangles silhouetted against the star volume. Any finite
// mesh wrapped around the camera does this. Space is now drawn purely by the
// three point-cloud shells in LayeredSky, which is also what a real
// procedural star renderer does.
{
  const sky = readFileSync('src/bjs/systems/LayeredSky.ts', 'utf8');

  for (const w of ['PlanetaryWorld', 'SandboxWorld', 'ShipWorld']) {
    const src = readFileSync(`src/bjs/worlds/${w}.ts`, 'utf8');
    ok(`${w} has no sky mesh`,
       !/createSky\(/.test(src) &&
       !/CreateIcoSphere\(['"`]sky/.test(src) &&
       !/CreateSphere\(['"`](sky|shipSky)/.test(src));
    ok(`${w} paints no starfield texture`, !/starfieldTexture/.test(src));
  }

  ok('the stars are a real 3D point cloud', /PointsCloudSystem/.test(sky));
  ok('the point shells never write depth',
     /disableDepthWrite\s*=\s*true/.test(sky));
  ok('the point shells blend additively', /alphaMode\s*=\s*1/.test(sky));

  // Every shell must sit inside the camera far plane or it is clipped away.
  const maxZ = parseInt((app.match(/maxZ = (\d+)/) || [])[1] || '0', 10);
  const outers = [...sky.matchAll(/outer:\s*(\d+)/g)].map((m) => parseInt(m[1], 10));
  ok('every star shell sits inside the camera far plane',
     outers.length === 3 && maxZ > 0 && Math.max(...outers) <= maxZ,
     'outers ' + outers.join(',') + ' vs maxZ ' + maxZ);
}

/* ---------------- 3. speed you can see --------------------------------- */

{
  const thr = parseInt((warp.match(/threshold:\s*(\d+)/) || [])[1] || '0', 10);
  const full = parseInt((warp.match(/full:\s*(\d+)/) || [])[1] || '0', 10);
  const cruise = parseInt((vehicle.match(/flySpeed = (\d+)/) || [])[1] || '0', 10);

  ok('warp streaks begin below normal cruising speed',
     thr < cruise, 'threshold ' + thr + ' vs cruise ' + cruise);
  ok('full warp is reachable without deep space',
     full <= 1000, 'full at ' + full);
  ok('the threshold is still above a standstill', thr > 5);
  ok('the reasoning is recorded', /almost never appeared|inert/i.test(warp));
}

/* ---------------- 4. the garage ---------------------------------------- */

ok('the garage door is a textured sectional door',
   /garage-door\.jpg/.test(garage));
ok('it is built from panels', /doorPanel_/.test(garage));
ok('the portal is set into the door',
   /Set into the garage door itself/.test(garage));
ok('the portal sits at the door, not out in the room',
   /portal\.position\.set\(0, 3\.15, 25\.7\)/.test(garage));
ok('the people have a fabric texture', /npc-suit\.jpg/.test(garage));
ok('skin tones vary across the cast', /const tone = \[/.test(garage));
ok('they have visors so they read as technicians', /npcVisor_/.test(garage));
ok('the visor follows the head it belongs to',
   /s\.visor\.position\.y/.test(garage) && /s\.visor\.rotation\.y = turn/.test(garage));
ok('the floor is a floating slab, not an endless plane',
   /floating slab/i.test(garage));
ok('the whole door rises together', /this\.doorRise/.test(garage));
ok('each panel keeps its own offset while rising',
   /_baseY/.test(garage));

/* ---------------- 5. the play button and branding ---------------------- */

ok('the play button has a bevel and inner light',
   /inset 0 1px 0 rgba\(255,255,255/.test(intro));
ok('it has a specular sweep on hover', /\.intro-play::before/.test(intro));
ok('the sweep animates across the plate',
   /translateX\(-130%\)/.test(intro) && /translateX\(130%\)/.test(intro));
ok('it responds to hover with motion', /translateY\(-3px\)/.test(intro));
ok('it has a pressed state', /\.intro-play:active/.test(intro));
ok('it is keyboard accessible', /focus-visible/.test(intro));

ok('the name LOW is gone from the boot screen', !/>LOW</.test(shell));
ok('the name LOW is gone from the brand mark', !/brand-name">LOW/.test(shell));
ok('the project is named correctly',
   /UNLIMITED POSSIBILITIES/.test(shell) || /UNLIMITED/.test(shell));

/* ---------------- 6. no more false black-screen reports ---------------- */

ok('the timer no longer samples pixels',
   /Deliberately do not\s*\n\s*\/\/ sample pixels here/.test(app) ||
   /deliberately do not/i.test(app));
ok('the reason is recorded so it is not undone',
   /composited buffer/i.test(app));
ok('the timer stops once it has seen frames',
   /clearInterval\(this\.watchdogTimer\)/.test(app));
ok('a report needs consecutive black samples',
   /blackFrameStreak/.test(app));
ok('one black frame is not enough to report',
   /this\.blackFrameStreak < 2/.test(app));
ok('a good frame clears suspicion entirely',
   /report\.painting[\s\S]{0,200}blackFrameStreak = 0/.test(app));
ok('pixels are sampled at several points in the first seconds',
   /watchdogFrames === 90 \|\| this\.watchdogFrames === 150/.test(app));

/* ---------------- 7. the HDR grade ------------------------------------- */

// These two assertions used to require exposure 1.32 and pipeline-side ACES.
// Both were wrong, and together they WERE the "planets are extremely bright"
// bug: every world shader already ends with the ACES curve followed by
// pow(1/2.2), so the pipeline was tonemapping an already-tonemapped, already
// gamma-encoded image and then scaling it by 1.32. Measured effect: +190% at
// 0.05 luminance. The grade now lives in the shaders, and the pipeline stays
// photometrically neutral. See tools/render-check.mjs for the measurement.
ok('the ACES constant is still available to the pipeline',
   /TONEMAPPING_ACES/.test(postfx));
ok('the pipeline does not tonemap on top of the shaders',
   /toneMappingEnabled = false/.test(postfx));
ok('bloom is stronger now that it reaches the screen',
   /bloom: 1\.42/.test(postfx));
ok('exposure is neutral, because the shaders own the grade',
   /exposure: 1(\.0)?,/.test(postfx));

// The grade must stay inside the bounds the black-screen check enforces.
{
  const v = parseFloat((postfx.match(/vignette: ([\d.]+)/) || [])[1] || '9');
  const e = parseFloat((postfx.match(/exposure: ([\d.]+)/) || [])[1] || '0');
  const c = parseFloat((postfx.match(/contrast: ([\d.]+)/) || [])[1] || '0');
  ok('the vignette stays safe', v <= 0.5, String(v));
  ok('exposure stays above the darkness floor', e > 0.3, String(e));
  ok('contrast stays in a sane range', c > 0.5 && c < 2, String(c));
}

/* ---------------- 8. the art exists ------------------------------------ */

{
  const fs = await import('node:fs');
  ok('the garage door texture is shipped',
     fs.existsSync('public/art/garage-door.jpg'));
  ok('the uniform texture is shipped',
     fs.existsSync('public/art/npc-suit.jpg'));
  const size = fs.statSync('public/art/garage-door.jpg').size;
  ok('the door texture is a real image', size > 10000, size + ' bytes');
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
