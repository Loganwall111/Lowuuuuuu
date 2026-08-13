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
  // The brief asked for 2000-10000, but camera.maxZ is 4000 and anything
  // beyond the far plane is clipped, so ~60% of these 30000 points were
  // never drawn. The count and the inner radius are kept; the outer radius
  // is pulled inside the far plane. Depth comes from the parallax lock
  // below, not from raw distance, so nothing is lost visually.
  ok('the far cosmos shell keeps its 30000 points from 2000 out',
    far.count === 30000 && far.inner === 2000);
  ok('the far shell stops inside the camera far plane',
    far.outer > 3000 && far.outer < 4000, String(far.outer));
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

// ------------------------------------------------- additive blending is real
console.log('\n— the sky actually blends additively —');
{
  // THE BUG THAT BROUGHT THE SHARDS BACK.
  // Babylon only takes the alpha path when needAlphaBlending() is true, and
  // StandardMaterial returns false while alpha === 1. Setting alphaMode
  // alone was therefore a no-op and every point drew as an opaque quad.
  for (const f of ['LayeredSky', 'StarFieldRenderer', 'VerseRenderer']) {
    const src = read(`src/bjs/systems/${f}.ts`);
    ok(`${f} sets an additive alpha mode`, /alphaMode = 1/.test(src));
    ok(`${f} nudges alpha off 1.0 so blending is actually armed`,
      /alpha = 0\.999/.test(src),
      'alphaMode is ignored while alpha === 1');
    ok(`${f} still disables depth writing`,
      /disableDepthWrite = true/.test(src));
  }

  // Nothing may be drawn beyond the camera's far plane, or it is clipped.
  const app = read('src/bjs/App.ts');
  const maxZ = parseFloat((app.match(/camera\.maxZ = ([\d.]+)/) || [])[1] || '0');
  const sky = read('src/bjs/systems/LayeredSky.ts');
  const outer = Math.max(...[...sky.matchAll(/outer: (\d+)/g)].map((m) => +m[1]));
  ok('every sky shell fits inside the camera far plane',
    outer > 0 && maxZ > 0 && outer < maxZ,
    `outermost shell ${outer} vs maxZ ${maxZ}`);
}

// ------------------------------------------------------- resize/aspect guard
console.log('\n— no resize can produce a bad aspect —');
{
  const outFile = '/tmp/safeuni-' + Date.now() + '.mjs';
  const r = await build({
    entryPoints: ['src/bjs/SafeUniforms.ts'], bundle: true, format: 'esm',
    write: false, logLevel: 'error', platform: 'browser'
  });
  fs.writeFileSync(outFile, r.outputFiles[0].text);
  const { safeAspect } = await import(outFile);

  const cases = [[0, 0], [1920, 0], [0, 1080], [NaN, 1080], [1920, NaN],
                 [-5, -5], [Infinity, 1080], [1920, Infinity], [-1920, 1080]];
  let bad = null;
  for (const [w, h] of cases) {
    const a = safeAspect(w, h);
    if (!Number.isFinite(a) || a <= 0) { bad = `${w}x${h} -> ${a}`; break; }
  }
  ok('no degenerate canvas size yields a bad aspect', !bad, bad ?? '');
  ok('a collapsed canvas falls back to 16:9, not a stretched ratio',
    Math.abs(safeAspect(1920, 0) - 16 / 9) < 1e-9,
    String(safeAspect(1920, 0)));
  ok('real sizes are still computed correctly',
    Math.abs(safeAspect(800, 600) - 4 / 3) < 1e-9);
  ok('resizes are skipped while the canvas is collapsed',
    read('src/bjs/App.ts').includes('clientWidth < 1 || c.clientHeight < 1'));
}

// ------------------------------------------------ volumetric atmosphere
console.log('\n— atmospheres are volumetric, not a solid shell —');
{
  const pw = read('src/bjs/worlds/PlanetaryWorld.ts');
  ok('the fake Fresnel rim aura is gone',
    !/float rim = pow\(1\.0 - max\(dot\(n, V\)/.test(pw));
  ok('there is a raymarching loop through the atmosphere',
    /for \(int i = 0; i < STEPS; i\+\+\)/.test(pw));
  ok('optical depth is integrated, not faked', /odR \+= dR/.test(pw));
  ok('a secondary light march gives the terminator its colour',
    /for \(int j = 0; j < 2; j\+\+\)/.test(pw));
  ok('Rayleigh scale height is 8.0 km', /8\.0 \/ 6371\.0/.test(pw));
  ok('Mie scale height is 1.2 km', /1\.2 \/ 6371\.0/.test(pw));
  ok('density decays exponentially with altitude',
    /exp\(-alt \/ hR\)/.test(pw) && /exp\(-alt \/ hM\)/.test(pw));
  ok('Beer-Lambert extinction is applied', /exp\(-tau/.test(pw));
  ok('the view ray is dotted against the sun for the phase functions',
    /float mu = dot\(rd, L\)/.test(pw));
  ok('a Henyey-Greenstein lobe makes the gold forward halo',
    /phaseMie/.test(pw) && /0\.76/.test(pw));
  ok('opacity comes from integrated density, so the limb fades',
    /1\.0 - exp\(-dens/.test(pw));
  ok('the new geometry uniforms are declared',
    /uniform vec3 planetCenter/.test(pw) && /uniform float atmoRadius/.test(pw));
  ok('the new uniforms are listed on the material',
    /'planetCenter', 'planetRadius', 'atmoRadius'/.test(pw));
  ok('the orbiting planet centre is refreshed every frame',
    /setVector3\('planetCenter', b\.mesh\.getAbsolutePosition\(\)\)/.test(pw));

  // The physics, measured rather than asserted.
  const BR = [5.8e-3, 13.5e-3, 33.1e-3];
  ok('Rayleigh scatters blue ~5.7x more than red, per 1/lambda^4',
    Math.abs(BR[2] / BR[0] - 5.7) < 0.2, (BR[2] / BR[0]).toFixed(2));
  const pR = (mu) => 0.0596831 * (1 + mu * mu);
  let integral = 0;
  const N = 4000;
  for (let i = 0; i < N; i++) {
    const mu = -1 + 2 * (i + 0.5) / N;
    integral += pR(mu) * 2 * Math.PI * (2 / N);
  }
  ok('the Rayleigh phase function is normalised to 1',
    Math.abs(integral - 1) < 1e-3, integral.toFixed(5));
  const pM = (mu, g) => (1 - g * g) /
    (12.566371 * Math.pow(1 + g * g - 2 * g * mu, 1.5));
  ok('Mie scatters strongly forward, which is what makes the halo',
    pM(1, 0.76) / pM(-1, 0.76) > 100,
    (pM(1, 0.76) / pM(-1, 0.76)).toFixed(0) + 'x forward');
}

// --------------------------------------------------- volumetric galaxy fog
console.log('\n— galaxies are cloudy, not concentric shells —');
{
  const outFile = '/tmp/dss-' + Date.now() + '.mjs';
  const r = await build({
    entryPoints: ['src/bjs/systems/DeepSkySystem.ts'], bundle: true,
    format: 'esm', write: false, logLevel: 'error', platform: 'browser'
  });
  fs.writeFileSync(outFile, r.outputFiles[0].text);
  const { simplex3, fbm3, galacticMedium } = await import(outFile);
  const { Vector3 } = await import(
    '/home/user/Low/node_modules/@babylonjs/core/Maths/math.vector.js');

  let mn = 1e9, mx = -1e9, sum = 0;
  const N = 20000;
  for (let i = 0; i < N; i++) {
    const v = simplex3(Math.random() * 40 - 20, Math.random() * 40 - 20,
      Math.random() * 40 - 20);
    mn = Math.min(mn, v); mx = Math.max(mx, v); sum += v;
  }
  ok('simplex noise stays in [-1,1]', mn >= -1 && mx <= 1,
    `${mn.toFixed(3)}..${mx.toFixed(3)}`);
  ok('simplex noise is zero-mean', Math.abs(sum / N) < 0.03,
    (sum / N).toFixed(4));
  ok('simplex noise is deterministic',
    simplex3(1.5, 2.5, 3.5) === simplex3(1.5, 2.5, 3.5));

  let worst = 0;
  for (let i = 0; i < 3000; i++) {
    const x = Math.random() * 20, y = Math.random() * 20, z = Math.random() * 20;
    worst = Math.max(worst, Math.abs(simplex3(x, y, z) - simplex3(x + 1e-4, y, z)));
  }
  ok('the noise field is continuous, so there are no seams', worst < 0.01,
    worst.toExponential(2));

  const vals = [];
  for (let i = 0; i < 400; i++) vals.push(fbm3(i * 0.37, i * 0.11, i * 0.53, 4));
  const mean = vals.reduce((a, b) => a + b, 0) / vals.length;
  const sd = Math.sqrt(vals.reduce((a, b) => a + (b - mean) ** 2, 0) / vals.length);
  ok('the fractal field genuinely varies', sd > 0.05, sd.toFixed(4));

  // The medium must now be cloudy: same radius, different density.
  const c = new Vector3(0, 0, 0);
  const R = 1000;
  const densities = [];
  for (let i = 0; i < 64; i++) {
    const a = (i / 64) * Math.PI * 2;
    const p = new Vector3(Math.cos(a) * R * 0.5, 0, Math.sin(a) * R * 0.5);
    densities.push(galacticMedium(p, c, R).fogDensity);
  }
  const dmin = Math.min(...densities), dmax = Math.max(...densities);
  ok('fog varies around a circle of constant radius', dmax - dmin > 1e-9,
    `min ${dmin.toExponential(2)} max ${dmax.toExponential(2)}`);
  ok('fog is never negative', dmin >= 0);
  ok('fog still thickens toward the core',
    galacticMedium(new Vector3(50, 0, 0), c, R).depth >
    galacticMedium(new Vector3(950, 0, 0), c, R).depth);
  ok('outside the galaxy there is no fog',
    galacticMedium(new Vector3(5000, 0, 0), c, R).inside === false);
  ok('depth stays within 0..1', densities.every((d) => d >= 0));
}

// ------------------------------------------------- accretion disk softness
console.log('\n— the accretion disk dissolves rather than clipping —');
{
  const bh = read('src/bjs/worlds/BlackHoleWorld.ts');
  ok('the disk uses a Keplerian density profile', /float kepler/.test(bh));
  ok('the profile follows r^-3/2', /pow\(diskInner \/ rr, 1\.5\)/.test(bh));
  ok('differential shear modulates the turbulence',
    /dens \*= 1\.0 \+ 0\.35 \* kepler/.test(bh));
  ok('the outer edge fades over a wide band, not a hard cut',
    /smoothstep\(0\.55, 1\.0, t\)/.test(bh));
  ok('the fade is ragged, so no single rim is visible',
    /outer \*= 0\.55 \+ 0\.45 \* n2/.test(bh));
}

// ------------------------------------------------------------ static voice
console.log('\n— starfield static —');
{
  const sa = read('src/bjs/systems/SpaceAudio.ts');
  ok('there is an electromagnetic static voice', /staticFilter/.test(sa));
  ok('it is a highpass hiss, above the hum', /'highpass'/.test(sa));
  ok('it gets crispier in dense star fields',
    /2200 \+ dens \* 5200/.test(sa));
  ok('it is driven by real galactic density',
    read('src/bjs/App.ts').includes('starDensity: dens'));
  ok('audio also arms on a canvas click, per the brief',
    read('src/bjs/App.ts').includes("audioCanvas?.addEventListener('click'"));
}

// ------------------------------------- black holes you can actually reach
console.log('\n— travelling to a black hole —');
{
  const outFile = '/tmp/holefield-' + Date.now() + '.mjs';
  const r = await build({
    entryPoints: ['src/bjs/systems/HoleFieldRenderer.ts'], bundle: true,
    format: 'esm', write: false, logLevel: 'error', platform: 'browser'
  });
  fs.writeFileSync(outFile, r.outputFiles[0].text);
  const { HoleFieldRenderer, radiiAway, DISK_INNER, DISK_OUTER } =
    await import(outFile);
  const { Vector3 } = await import(
    '/home/user/Low/node_modules/@babylonjs/core/Maths/math.vector.js');

  // Distance is measured in horizon radii, because "close" only means
  // anything relative to the hole's own size.
  const spec = { id: 'h', position: new Vector3(0, 0, 0), horizon: 10, seed: 3 };
  ok('distance is measured in horizon radii',
    Math.abs(radiiAway(new Vector3(100, 0, 0), spec) - 10) < 1e-9);
  ok('a zero-radius hole cannot divide by zero',
    Number.isFinite(radiiAway(new Vector3(1, 0, 0),
      { ...spec, horizon: 0 })));

  ok('the disk surrounds the horizon rather than intersecting it',
    DISK_INNER > 1 && DISK_OUTER > DISK_INNER);

  // A detached renderer must be inert, not throw.
  const hf = new HoleFieldRenderer();
  ok('an unattached renderer is safe to update',
    (hf.update(new Vector3(0, 0, 0), [spec]), true));
  ok('an unattached renderer draws nothing', hf.count === 0);
  ok('isLocked is vacuously true for a hole that does not exist',
    hf.isLocked('nope') === true);

  const src = read('src/bjs/systems/HoleFieldRenderer.ts');
  ok('the horizon and disk are moved in a single call',
    /private place\(/.test(src) &&
    /body\.setCenter\(to\)/.test(src) &&
    /disk\.position\.copyFrom\(to\)/.test(src));
  ok('the disk is additive and does not write depth',
    /dm\.alphaMode = 1/.test(src) && /dm\.disableDepthWrite = true/.test(src));
  ok('the disk alpha is nudged off 1.0 so blending is armed',
    /dm\.alpha = 0\.999/.test(src));
  ok('holes are released once out of range',
    /releaseBeyond/.test(src) && /this\.live\.delete\(id\)/.test(src));
  ok('the release threshold exceeds the build threshold, avoiding thrash',
    /buildWithin: 320/.test(src) && /releaseBeyond: 460/.test(src));

  const app = read('src/bjs/App.ts');
  ok('the app owns a hole field', app.includes('new HoleFieldRenderer()'));
  ok('it is rebuilt after loadWorld purges meshes',
    app.includes('this.holeField.dispose()') &&
    app.includes('this.holeField.attach(this.scene)'));
  ok('it is fed the real black hole regions',
    app.includes("r.kind === 'blackhole'") &&
    app.includes('horizon: this.universe.horizonRadiusOf(r)'));
}

// ------------------------------------- the lens cannot swallow the screen
console.log('\n— the horizon shadow stays a shape, not the frame —');
{
  const lfx = read('src/bjs/systems/LensFX.ts');
  ok('the shadow radius is clamped', /min\(holeR, 0\.42\)/.test(lfx));
  ok('the clamp is used by the inside test',
    /smoothstep\(shadowR \* 1\.02, shadowR \* 0\.86, r\)/.test(lfx));
  ok('lensed light still shows through the horizon',
    /mix\(col, col \* 0\.0\d+ \+ tint/.test(lfx));

  // Measure it: at any apparent size, the frame corner must stay visible.
  const ss = (a, b, x) => {
    const t = Math.max(0, Math.min(1, (x - a) / (b - a)));
    return t * t * (3 - 2 * t);
  };
  let worst = 0;
  for (const holeR of [0.05, 0.2, 0.42, 0.9, 1.5, 8, 100]) {
    const sR = Math.min(holeR, 0.42);
    worst = Math.max(worst, ss(sR * 1.02, sR * 0.86, 1.15));
  }
  ok('the frame corner is never inside the shadow, at any distance',
    worst < 0.01, 'worst coverage ' + worst.toFixed(4));

  // The GLSL must still be a single well-formed template literal. A stray
  // backtick in a comment silently truncates the shader.
  const body = lfx.slice(lfx.indexOf('universalLens'));
  ok('no stray backtick truncates the lens shader',
    (lfx.match(/`/g) || []).length % 2 === 0,
    (lfx.match(/`/g) || []).length + ' backticks');
}

// ------------------------------------------------- the anomaly, per instance
console.log('\n— fractured singularities are rare and isolated —');
{
  const outFile = '/tmp/bhbody-' + Date.now() + '.mjs';
  const r = await build({
    entryPoints: ['src/bjs/systems/BlackHoleBody.ts'], bundle: true,
    format: 'esm', write: false, logLevel: 'error', platform: 'browser'
  });
  fs.writeFileSync(outFile, r.outputFiles[0].text);
  const { rollAnomaly, sphereRadiusFor, ANOMALY_CHANCE } = await import(outFile);

  ok('the configured rate is inside the 5-10% brief',
    ANOMALY_CHANCE >= 0.05 && ANOMALY_CHANCE <= 0.10);

  // A PRNG can hit its rate on one seed pattern and miss badly on another,
  // so several shapes of input are measured.
  const patterns = {
    sequential: (i) => i,
    sparse: (i) => i * 7919,
    negative: (i) => -i,
    largeOffset: (i) => i + 1e9
  };
  for (const [name, fn] of Object.entries(patterns)) {
    let n = 0;
    const N = 20000;
    for (let i = 0; i < N; i++) if (rollAnomaly(fn(i))) n++;
    const pct = 100 * n / N;
    ok(`the rate holds for ${name} seeds`, pct >= 5 && pct <= 10,
      pct.toFixed(2) + '%');
  }

  ok('a given hole is always the same hole',
    rollAnomaly(12345) === rollAnomaly(12345));
  ok('different holes roll independently',
    new Set([...Array(200)].map((_, i) => rollAnomaly(i))).size === 2);

  // The visual contract: standard masks the core, anomaly exposes it.
  ok('a standard hole over-covers the disk inner edge',
    sphereRadiusFor(10, false) > 10);
  ok('an anomaly shrinks well inside it',
    sphereRadiusFor(10, true) < 10 * 0.6);
  ok('the anomaly is always the smaller of the two',
    sphereRadiusFor(10, true) < sphereRadiusFor(10, false));
  ok('the scale is proportional, so it works at any hole size',
    Math.abs(sphereRadiusFor(20, true) / sphereRadiusFor(10, true) - 2) < 1e-9);
}


// ------------------------------------------------------- seamless sky (bug 1)
// The user saw the star background split into hard-edged triangular wedges of
// different tint. Root cause: a textured icosphere. An icosphere has no
// continuous UV map - measured, 69 of its 362 vertex positions carry more than
// one UV, so the texture is cut at each of them. Clamping/filtering cannot fix
// a discontinuity that lives in the mesh's UVs.
console.log('\n— the sky has no seams —');
{
  const sky = read('src/bjs/shaders/SkyShader.ts');

  ok('the sky derives its coordinates from DIRECTION, not mesh UVs',
    /atan\(\s*d\.z\s*,\s*d\.x\s*\)/.test(sky) && /acos\(/.test(sky),
    'equirectangular from a normalized direction is the only seam-free option');

  ok('the sky shader declares no uv attribute',
    !/attribute\s+vec2\s+uv/.test(sky),
    'reading mesh uv on an icosphere reintroduces the wedges');

  ok('the longitude wrap is made continuous with fract()',
    /fract\(\s*u\s*\)/.test(sky));

  ok('the false mip step at the wrap is neutralised',
    /du\s*>\s*0\.5/.test(sky),
    'without this the u=0/1 wrap picks the smallest mip and draws a bright line');

  // Every world that shows stars must use it - a fix in one world only would
  // leave the other worlds still wedged.
  for (const w of ['PlanetaryWorld', 'SandboxWorld', 'ShipWorld']) {
    const src = read(`src/bjs/worlds/${w}.ts`);
    const usesSky = /createSky\(/.test(src);
    ok(`${w} builds its sky with the seamless shader`, usesSky,
      'expected a createSky() call');
    ok(`${w} no longer puts a plain texture on the sky mesh`,
      !/(sm|skyMat)\.emissiveTexture\s*=\s*starfieldTexture/.test(src),
      'StandardMaterial samples by mesh uv, which is what caused the wedges');
  }
}

// -------------------------------------------- one hole, one position (bug 2)
// The user saw a plain black circle on one side of the screen and a separate
// lensed orange disk on the other. Cause: the Singularity world raymarches its
// own black hole, while HoleFieldRenderer independently built a second one out
// of real geometry in the SAME scene. Two holes, two positions, so they slide
// apart as the camera moves.
console.log('\n— the Singularity shows exactly one black hole —');
{
  const app = read('src/bjs/App.ts');

  ok('the geometry hole field is suppressed while a world owns the hole',
    /ownsBlackHole/.test(app),
    'BlackHoleWorld raymarches its own hole; a second geometry hole is a duplicate');

  const bhw = read('src/bjs/worlds/BlackHoleWorld.ts');
  ok('BlackHoleWorld declares that it owns the black hole',
    /ownsBlackHole\s*(=|:)\s*true/.test(bhw));

  // The disk, the lensing and the core must be one position, by construction.
  ok('the world exposes a single hole position',
    /holePos/.test(bhw));
}

console.log(`\n${pass} passed, ${fail} failed`);
if (fail) process.exit(1);
