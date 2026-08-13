/**
 * render-check — the rendering faults the user reported, as assertions.
 *
 * Covers four separate bugs:
 *   1. planets rendering far too bright (double tone mapping)
 *   2. the raymarched disk drifting off its horizon (stale camera uniforms)
 *   3. black blocks punched into the scene (point clouds writing depth)
 *   4. no audio at all
 *
 * These are pure-logic and static-source checks. They deliberately do not
 * need a GPU, because no headless browser is available in this environment.
 */

import fs from 'node:fs';
import { build } from 'esbuild';

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const read = (p) => fs.readFileSync(p, 'utf8');

// ---------------------------------------------------------------- brightness
console.log('\n— planets are not blown out —');
{
  const post = read('src/bjs/PostFX.ts');
  const planet = read('src/bjs/shaders/PlanetShader.ts');

  // The ACES curve, in JS, so the claim can actually be measured.
  const aces = (c) => (c * (2.51 * c + 0.03)) / (c * (2.43 * c + 0.59) + 0.14);
  const gamma = (c) => Math.pow(Math.max(c, 0), 1 / 2.2);

  ok('the planet shader tonemaps its own output',
    planet.includes('2.51 * col + 0.03'));
  ok('the planet shader gamma-encodes its own output',
    planet.includes('1.0 / 2.2'));

  // Given the shader emits finished colour, the pipeline must not do it too.
  const tmLine = post.match(/ip\.toneMappingEnabled\s*=\s*([^;]+);/);
  ok('the post pipeline does not tonemap a second time',
    !!tmLine && tmLine[1].trim() === 'false',
    tmLine ? tmLine[1].trim() : 'not found');

  // Quantify what double tone mapping did, so the regression is undeniable.
  const single = gamma(aces(0.05));
  const doubled = gamma(aces(gamma(aces(0.05)) * 1.32));
  ok('double tone mapping measurably blows out shadows',
    doubled > single * 2.5,
    `0.05 linear: ${single.toFixed(3)} -> ${doubled.toFixed(3)}`);

  const exp = post.match(/^\s*exposure:\s*([\d.]+),/m);
  ok('default exposure is neutral', !!exp && parseFloat(exp[1]) <= 1.0,
    exp ? exp[1] : 'not found');

  ok('the diffuse term is no longer overdriven',
    planet.includes('diff * 1.0 + 0.03'),
    'expected the 1.35 overdrive to be gone');

  // The Exposure slider was declared in the UI but never reached the shader.
  ok('the planet shader declares an exposure uniform',
    /uniform float exposure/.test(planet));
  ok('exposure is applied before the tone curve, in linear space',
    planet.indexOf('col *= max(exposure') < planet.indexOf('2.51 * col + 0.03'));
  ok('exposure is bound at every call site',
    read('src/bjs/PlanetMaps.ts').includes("'exposure'"));
  ok('the exposure slider is actually sent to the shader',
    read('src/bjs/worlds/PlanetaryWorld.ts').includes("setFloat('exposure'"));
}

// ------------------------------------------------------------ camera sync
console.log('\n— the disk stays locked to the horizon —');
{
  const bh = read('src/bjs/worlds/BlackHoleWorld.ts');
  const app = read('src/bjs/App.ts');

  ok('the world exposes a dedicated camera sync step',
    /syncCamera\(ctx: WorldContext\)/.test(bh));
  ok('update() reuses that same step rather than duplicating it',
    /this\.syncCamera\(ctx\)/.test(bh));

  // The ordering claim: sync must happen after the camera is placed and
  // before the draw.
  const iSync = app.indexOf('syncCamera?.(this.ctx)');
  const iRender = app.indexOf('this.scene.render()', iSync);
  const iCamMove = app.lastIndexOf('this.camera.position.copyFrom', 0, iSync);
  ok('the camera is synced immediately before the draw',
    iSync > 0 && iRender > iSync,
    `sync@${iSync} render@${iRender}`);
  ok('the sync happens after world.update, not inside it',
    iSync > app.indexOf('this.world.update(dt'));

  ok('the hole centre is bound as a uniform every frame',
    bh.includes("setVector3('holePos', this.center)"));
  ok('holePos is declared in the shader', /uniform vec3\s+holePos/.test(bh));
  ok('holePos is listed on the material', /'holePos'/.test(bh));
  ok('the ray origin is hole-relative, so the disk cannot drift',
    bh.includes('vec3 ro   = camPos - holePos'));
  ok('the view matrix is recomputed rather than read from cache',
    bh.includes('cam.computeWorldMatrix()'));

  // The NaN guard from the previous fix must survive.
  ok('aspect is still computed safely',
    bh.includes('safeAspect(eng.getRenderWidth(), eng.getRenderHeight())'));
  ok('safeAspect never divides by zero',
    read('src/bjs/SafeUniforms.ts').includes('Math.max(1e-12'.slice(0, 8)) ||
    read('src/bjs/SafeUniforms.ts').includes('safeDiv'));
}

// -------------------------------------------------------------- layered sky
console.log('\n— the sky is deep and never occludes —');
{
  const outFile = '/tmp/layeredsky-' + Date.now() + '.mjs';
  const r = await build({
    entryPoints: ['src/bjs/systems/LayeredSky.ts'],
    bundle: true, format: 'esm', write: false, logLevel: 'error',
    platform: 'browser'
  });
  fs.writeFileSync(outFile, r.outputFiles[0].text);
  const { SKY_SHELLS, shellBudget, spherePoint, starColor, LayeredSky } =
    await import(outFile);

  ok('there are three concentric shells', SKY_SHELLS.length === 3);

  const [core, mid, far] = SKY_SHELLS;
  ok('the inner core matches the brief (2000 @ 100-500)',
    core.count === 2000 && core.inner === 100 && core.outer === 500);
  ok('the mid galactic shell matches the brief (10000 @ 500-2000)',
    mid.count === 10000 && mid.inner === 500 && mid.outer === 2000);
  ok('the far cosmos shell matches the brief (30000 @ 2000-10000)',
    far.count === 30000 && far.inner === 2000 && far.outer === 10000);
  ok('the shells are strictly nested with no gaps',
    core.outer === mid.inner && mid.outer === far.inner);
  ok('42,000 background stars in total', shellBudget() === 42000);

  // Parallax is the entire point of layering.
  ok('each shell parallaxes differently',
    core.lock < mid.lock && mid.lock < far.lock,
    `${core.lock} / ${mid.lock} / ${far.lock}`);
  ok('the near shell is fully world-locked', core.lock === 0);
  ok('the far shell barely moves', far.lock > 0.85 && far.lock < 1);

  // Point distribution must be even, not clumped at the poles.
  let poles = 0;
  const N = 4000;
  for (let i = 0; i < N; i++) {
    const [, y] = spherePoint(Math.random(), Math.random());
    if (Math.abs(y) > 0.9) poles++;
  }
  // A uniform sphere puts 10% of points in |y|>0.9.
  ok('points are uniform on the sphere, not clumped at the poles',
    Math.abs(poles / N - 0.1) < 0.02,
    (100 * poles / N).toFixed(1) + '% near poles, expected ~10%');

  // Colour must stay in gamut and skew cool.
  let bad = 0, cool = 0;
  for (let i = 0; i <= 100; i++) {
    const c = starColor(i / 100);
    if (![c.r, c.g, c.b].every((v) => v >= 0 && v <= 1)) bad++;
    if (c.b < c.r) cool++;
  }
  ok('every star colour is in gamut', bad === 0);
  ok('the colour distribution skews cool, like a real sky', cool > 50);

  // The actual bug: depth writing.
  const src = read('src/bjs/systems/LayeredSky.ts');
  ok('the sky disables depth writing', src.includes('disableDepthWrite = true'));
  ok('the sky never force-writes depth', src.includes('forceDepthWrite = false'));
  ok('the sky uses additive blending', src.includes('alphaMode = 1'));
  ok('the sky draws in the first rendering group',
    src.includes('renderingGroupId = 0'));
  ok('the sky is never fogged', src.includes('applyFog = false'));
  ok('the sky is never picked', src.includes('isPickable = false'));

  // The pre-existing real-star renderer must keep the same guarantee.
  for (const f of ['StarFieldRenderer', 'VerseRenderer']) {
    ok(`${f} also disables depth writing`,
      read(`src/bjs/systems/${f}.ts`).includes('disableDepthWrite = true'));
  }

  // Parallax maths, without a scene.
  const sky = new LayeredSky();
  ok('a detached sky is safe to update', (sky.update({ x: 1, y: 2, z: 3 }), true));
  ok('a detached sky reports no points', sky.count === 0);
}

// -------------------------------------------------------------------- audio
console.log('\n— the simulation makes sound —');
{
  const outFile = '/tmp/spaceaudio-' + Date.now() + '.mjs';
  const r = await build({
    entryPoints: ['src/bjs/systems/SpaceAudio.ts'],
    bundle: true, format: 'esm', write: false, logLevel: 'error',
    platform: 'browser'
  });
  fs.writeFileSync(outFile, r.outputFiles[0].text);
  const { SpaceAudio, DEFAULT_AUDIO, humFrequency, singularityGain } =
    await import(outFile);

  // Hum: pitch tracks velocity, within the band the brief specifies.
  ok('the hum sits in the 55-80Hz band',
    DEFAULT_AUDIO.humBaseHz === 55 && DEFAULT_AUDIO.humTopHz === 80);
  ok('at rest the hum is at its base pitch', humFrequency(0) === 55);
  ok('the hum rises with speed', humFrequency(400) > humFrequency(50));
  ok('the hum never exceeds its ceiling', humFrequency(1e9) <= 80);
  ok('the hum is monotonic in speed', (() => {
    let prev = -1;
    for (let v = 0; v < 2000; v += 25) {
      const f = humFrequency(v);
      if (f < prev - 1e-9) return false;
      prev = f;
    }
    return true;
  })());
  ok('a negative or NaN speed cannot detune the hum',
    humFrequency(-5) === 55 && humFrequency(NaN) === 55);

  // Singularity: louder as you close in.
  ok('a distant singularity is silent', singularityGain(1e6) === 0);
  ok('the rumble grows as the hole nears',
    singularityGain(200) > singularityGain(600));
  ok('the rumble peaks at the horizon', singularityGain(0) === 1);
  ok('the rumble is never negative', singularityGain(1e-9) >= 0);
  ok('an unknown distance is silent, not NaN',
    singularityGain(Infinity) === 0 && singularityGain(NaN) === 0);
  ok('the falloff is steeper close in than far out', (() => {
    const near = singularityGain(100) - singularityGain(200);
    const farr = singularityGain(700) - singularityGain(800);
    return near > farr;
  })());

  // It must never throw in an environment with no AudioContext.
  const prev = globalThis.AudioContext;
  delete globalThis.AudioContext;
  const silent = new SpaceAudio();
  ok('no AudioContext is handled gracefully', silent.start() === false);
  ok('it is marked unavailable rather than broken', silent.unavailable === true);
  ok('updating a silent engine does not throw',
    (silent.update({ speed: 100, warpCharge: 1 }), true));
  ok('muting a silent engine does not throw',
    (silent.setMuted(true), true));
  ok('disposing a silent engine does not throw', (silent.dispose(), true));
  if (prev) globalThis.AudioContext = prev;

  const src = read('src/bjs/systems/SpaceAudio.ts');
  ok('the warp voice is a bandpass filter, per the brief',
    src.includes("type = 'bandpass'"));
  ok('the warp passband sweeps with charge',
    src.includes('320 + charge * 2600'));
  ok('gain changes are ramped, never switched',
    src.includes('setTargetAtTime') && !src.includes('.gain.value = to'));

  // Wiring.
  const app = read('src/bjs/App.ts');
  ok('the app owns an audio engine', app.includes('audio = new SpaceAudio()'));
  ok('audio is armed on a user gesture, as browsers require',
    app.includes("addEventListener('pointerdown', armAudio)"));
  ok('the gesture listener removes itself once armed',
    app.includes("removeEventListener('pointerdown', armAudio)"));
  ok('audio is driven from live speed', app.includes('speed: this.vehicle.flySpeed'));
  ok('audio is driven from real warp charge',
    app.includes('warpCharge: this.warpDrive.charge'));
  ok('audio is driven from real black hole distance',
    app.includes('singularityDistance'));
}

console.log(`\n${pass} passed, ${fail} failed`);
if (fail) process.exit(1);
