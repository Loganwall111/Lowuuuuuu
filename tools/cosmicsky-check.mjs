/**
 * Procedural sky checks.
 *
 * The complaint these guard against is "it's just a bunch of stars, there's
 * no fog like the actual Milky Way, and no nebulae". So these assertions
 * measure STRUCTURE in the sky function: that a bright band exists, that
 * dark dust lanes cut through it (absorption, which is what makes a galaxy
 * look like a galaxy rather than a smudge), that coloured emission regions
 * appear, and that each verse medium produces a visibly different sky.
 *
 * The GLSL is ported to JS here so the maths can be measured without a GPU.
 * Where a value is checked against the shader text, it is checked against
 * the real source, so the port cannot drift silently.
 *
 * Run: node tools/cosmicsky-check.mjs
 */
import { execFileSync } from 'child_process';
import fs from 'fs';

let pass = 0, fail = 0;
const ok = (name, cond, extra) => {
  if (cond) pass++;
  else { fail++; console.log('FAIL: ' + name + (extra ? ' — ' + extra : '')); }
};

const SHADER = 'src/bjs/shaders/CosmicSkyShader.ts';
const SYSTEM = 'src/bjs/systems/CosmicSky.ts';
for (const f of [SHADER, SYSTEM]) {
  if (!fs.existsSync(f)) {
    console.log('FAIL: ' + f + ' is missing');
    console.log('0 passed, 1 failed');
    process.exit(1);
  }
}
const shaderSrc = fs.readFileSync(SHADER, 'utf8');
const glsl = (shaderSrc.match(/COSMIC_SKY_GLSL\s*=\s*`([\s\S]*?)`;/m) || [])[1] || '';

// ------------------------------------------------------ the sky is shared
ok('the sky GLSL is exported for reuse', glsl.length > 500);
ok('the black hole lens samples the exact rendered sky frame', (() => {
  const hole = fs.readFileSync('src/bjs/shaders/HoleFieldShader.ts', 'utf8');
  return hole.includes('textureSampler') && hole.includes('sourceUv');
})());
ok('the sky dome and the hole use one entry point',
  /vec3 cosmicSky\(vec3 dir/.test(glsl));
ok('the hole cannot diverge from sky uniforms', (() => {
  const r = fs.readFileSync('src/bjs/systems/HoleFieldRenderer.ts', 'utf8');
  return r.includes("'textureSampler'") && !r.includes("'skyMedium'");
})());
ok('the app drives sky and holes from the same verse state', (() => {
  const app = fs.readFileSync('src/bjs/App.ts', 'utf8');
  return app.includes('this.cosmicSky.setState(') && app.includes('this.holeField.setSky(');
})());

// ------------------------------------------------ port of the GLSL maths
const fract = (x) => x - Math.floor(x);
function hash(x, y, z) {
  let px = fract(x * 0.3183099 + 0.71);
  let py = fract(y * 0.3183099 + 0.113);
  let pz = fract(z * 0.3183099 + 0.419);
  px *= 17; py *= 17; pz *= 17;
  return fract(px * py * pz * (px + py + pz));
}
function noise(x, y, z) {
  const ix = Math.floor(x), iy = Math.floor(y), iz = Math.floor(z);
  let fx = x - ix, fy = y - iy, fz = z - iz;
  fx = fx * fx * (3 - 2 * fx); fy = fy * fy * (3 - 2 * fy); fz = fz * fz * (3 - 2 * fz);
  const L = (a, b, t) => a + (b - a) * t;
  return L(
    L(L(hash(ix, iy, iz), hash(ix + 1, iy, iz), fx),
      L(hash(ix, iy + 1, iz), hash(ix + 1, iy + 1, iz), fx), fy),
    L(L(hash(ix, iy, iz + 1), hash(ix + 1, iy, iz + 1), fx),
      L(hash(ix, iy + 1, iz + 1), hash(ix + 1, iy + 1, iz + 1), fx), fy),
    fz);
}
function fbm(x, y, z, oct) {
  let sum = 0, amp = 0.5, norm = 0;
  for (let i = 0; i < oct; i++) {
    sum += noise(x, y, z) * amp; norm += amp; amp *= 0.5;
    x *= 2.03; y *= 2.03; z *= 2.03;
  }
  return norm > 0 ? sum / norm : 0;
}
function ridge(x, y, z, oct) {
  let sum = 0, amp = 0.5, norm = 0;
  for (let i = 0; i < oct; i++) {
    const n = 1 - Math.abs(noise(x, y, z) * 2 - 1);
    sum += n * n * amp; norm += amp; amp *= 0.5;
    x *= 2.11; y *= 2.11; z *= 2.11;
  }
  return norm > 0 ? sum / norm : 0;
}
const smoothstep = (a, b, x) => {
  const t = Math.max(0, Math.min(1, (x - a) / (b - a)));
  return t * t * (3 - 2 * t);
};

/** Port of skyGalaxy. */
function galaxy(dx, dy, dz, bandTilt = 0.42) {
  const c = Math.cos(bandTilt), s = Math.sin(bandTilt);
  const gx = dx, gy = dy * c - dz * s, gz = dy * s + dz * c;
  const h = gy;
  const band = Math.exp(-h * h * 26);
  const core = Math.pow(Math.max(0, gx * 0.5 + 0.5), 3);
  const bulge = Math.exp(-(h * h * 60 + Math.pow(1 - core, 2) * 8));
  const grain = fbm(gx * 5.5, gy * 5.5, gz * 5.5, 5);
  let glow = band * (0.45 + 0.55 * grain) * (0.55 + core * 0.85);
  glow += bulge * 1.5;
  let col = [0.62 * glow * 0.5, 0.66 * glow * 0.5, 0.78 * glow * 0.5];
  col = [col[0] + 0.55 * bulge * 0.55, col[1] + 0.42 * bulge * 0.55, col[2] + 0.26 * bulge * 0.55];
  let dust = ridge(gx * 7 + 3.3, gy * 7 + 3.3, gz * 7 + 3.3, 5);
  dust *= Math.exp(-h * h * 42);
  dust = smoothstep(0.35, 0.95, dust);
  col = col.map((v) => v * (1 - dust * 0.92));
  let neb = fbm(gx * 3.1 + 19.7, gy * 3.1 + 19.7, gz * 3.1 + 19.7, 4);
  neb = smoothstep(0.52, 0.95, neb) * Math.exp(-h * h * 30);
  let neb2 = fbm(gx * 6.3 - 7.1, gy * 6.3 - 7.1, gz * 6.3 - 7.1, 4);
  neb2 = smoothstep(0.58, 1.0, neb2) * Math.exp(-h * h * 34);
  col[0] += 0.85 * neb * 0.42 * (0.4 + core);
  col[1] += 0.18 * neb * 0.42 * (0.4 + core);
  col[2] += 0.24 * neb * 0.42 * (0.4 + core);
  col[0] += 0.14 * neb2 * 0.30;
  col[1] += 0.42 * neb2 * 0.30;
  col[2] += 0.62 * neb2 * 0.30;
  let cirrus = fbm(gx * 2.2 - 41, gy * 2.2 - 41, gz * 2.2 - 41, 4);
  cirrus = smoothstep(0.62, 1.0, cirrus);
  col[0] += 0.10 * cirrus * 0.5;
  col[1] += 0.09 * cirrus * 0.5;
  col[2] += 0.17 * cirrus * 0.5;
  return { col, dust, neb, neb2 };
}

/** Directions spread over the sphere. */
function dirs(n) {
  const out = [];
  for (let i = 0; i < n; i++) {
    const y = 1 - (2 * (i + 0.5)) / n;
    const r = Math.sqrt(Math.max(0, 1 - y * y));
    const phi = i * 2.399963;
    out.push([r * Math.cos(phi), y, r * Math.sin(phi)]);
  }
  return out;
}
const lum = (c) => c[0] * 0.299 + c[1] * 0.587 + c[2] * 0.114;

// ------------------------------------------------------------- the galaxy
{
  const D = dirs(4000);
  const samples = D.map((d) => ({ d, g: galaxy(...d) }));

  ok('the sky is never NaN',
    samples.every((s) => s.g.col.every((v) => Number.isFinite(v))));
  ok('the sky is never negative',
    samples.every((s) => s.g.col.every((v) => v >= 0)));

  // A BAND, not an even glow. Compare brightness in the galactic plane
  // against brightness toward the poles, in the rotated frame.
  const c = Math.cos(0.42), s = Math.sin(0.42);
  const height = (d) => d[1] * c - d[2] * s;
  const inPlane = samples.filter((x) => Math.abs(height(x.d)) < 0.08);
  const polar = samples.filter((x) => Math.abs(height(x.d)) > 0.6);
  const meanL = (arr) => arr.reduce((a, x) => a + lum(x.g.col), 0) / Math.max(1, arr.length);
  ok('there is a bright band across the sky', meanL(inPlane) > meanL(polar) * 3,
    meanL(inPlane).toFixed(4) + ' vs ' + meanL(polar).toFixed(4));

  // THE MISSING PIECE: dust lanes. Absorption, not addition.
  ok('the galaxy has dust lanes', /col \*= 1\.0 - dust/.test(glsl));
  const dusty = inPlane.filter((x) => x.g.dust > 0.4);
  ok('dust actually covers part of the band', dusty.length > 0,
    dusty.length + ' of ' + inPlane.length + ' samples');
  ok('dust darkens what it covers', (() => {
    const clear = inPlane.filter((x) => x.g.dust < 0.05);
    if (!dusty.length || !clear.length) return false;
    return meanL(dusty) < meanL(clear);
  })());
  ok('dust hugs the galactic plane',
    samples.filter((x) => x.g.dust > 0.3)
      .every((x) => Math.abs(height(x.d)) < 0.45));

  // Emission nebulae, in colour.
  const emitting = inPlane.filter((x) => x.g.neb > 0.15 || x.g.neb2 > 0.15);
  ok('there are emission nebulae in the plane', emitting.length > 0,
    emitting.length + ' samples');
  ok('nebulae are coloured, not grey', (() => {
    const coloured = emitting.filter((x) => {
      const [r, g, b] = x.g.col;
      const mx = Math.max(r, g, b), mn = Math.min(r, g, b);
      return mx > 0.02 && (mx - mn) / mx > 0.25;
    });
    return coloured.length > emitting.length * 0.4;
  })(), emitting.length + ' emitting');
  ok('both hydrogen-red and oxygen-teal regions exist',
    /0\.85, 0\.18, 0\.24/.test(glsl) && /0\.14, 0\.42, 0\.62/.test(glsl));

  // The sky must vary a lot - the complaint was that it looked uniform.
  const ls = samples.map((x) => lum(x.g.col));
  const mean = ls.reduce((a, b) => a + b, 0) / ls.length;
  const sd = Math.sqrt(ls.reduce((a, b) => a + (b - mean) ** 2, 0) / ls.length);
  ok('the sky has strong large-scale variation', sd > mean * 0.6,
    'sd ' + sd.toFixed(4) + ' vs mean ' + mean.toFixed(4));
  ok('the sky is not a flat wash',
    new Set(ls.map((v) => v.toFixed(3))).size > 100);

  // High-latitude cirrus keeps the poles off pure black.
  ok('the sky away from the band is not pure black',
    polar.some((x) => lum(x.g.col) > 0.001));
}

// ------------------------------------------------------- the Mandelbrot
{
  function mandel(cx, cy, MAX = 96) {
    let zx = 0, zy = 0, i = 0;
    for (; i < MAX; i++) {
      const t = zx * zx - zy * zy + cx;
      zy = 2 * zx * zy + cy; zx = t;
      if (zx * zx + zy * zy > 256) break;
    }
    return i;
  }
  ok('the escape-time loop is in the shader',
    /z = vec2\(z\.x \* z\.x - z\.y \* z\.y, 2\.0 \* z\.x \* z\.y\) \+ c/.test(glsl));
  ok('points inside the set render black', /if \(iter >= MAX_ITER\) return vec3\(0\.0\)/.test(glsl));
  ok('the iteration count is smoothed, so bands are continuous',
    /log2\(max\(log2\(length\(z\)\), 1e-4\)\)/.test(glsl));

  // The zoom centre must sit somewhere with structure at every scale, or
  // deep zoom lands in a featureless void.
  const cx0 = -0.743643887037151, cy0 = 0.131825904205330;
  ok('the zoom centre is on the boundary of the set', mandel(cx0, cy0) >= 96);
  // Iterations must scale with magnification or deep zoom goes blank.
  ok('the iteration budget grows with zoom',
    /budget = clamp\(96\.0 \+ log2\(max\(zoom, 1\.0\)\) \* 22\.0/.test(glsl));
  const budgetFor = (z) => Math.max(96, Math.min(340, 96 + Math.log2(Math.max(z, 1)) * 22));
  for (const z of [1, 100, 10000, 200000]) {
    const counts = new Set();
    for (let i = 0; i < 400; i++) {
      const a = (i % 20) / 20 - 0.5, b = Math.floor(i / 20) / 20 - 0.5;
      counts.add(mandel(cx0 + (a * 1.2) / z, cy0 + (b * 1.2) / z, budgetFor(z)));
    }
    ok('the fractal still has detail at zoom ' + z, counts.size > 8,
      counts.size + ' distinct iteration counts');
  }
  // And the zoom must stop before it outruns that budget.
  ok('max zoom stays inside the range that still resolves detail', (() => {
    const sys = fs.readFileSync(SYSTEM, 'utf8');
    const m = sys.match(/MAX_ZOOM = ([\de.+]+)/);
    return m && Number(m[1]) <= 5e5;
  })());
}

// ------------------------------------------- verse mediums are different
{
  ok('the codeverse has falling data columns', /vec3 skyCode\(vec3 d, float t\)/.test(glsl));
  ok('code streams scroll over time', /d\.y \* 22\.0 \+ t \* speed/.test(glsl));
  ok('code streams have bright leading characters', /float head = step\(/.test(glsl));
  ok('the shapeverses render a lattice', /vec3 skyLattice\(vec3 d, float symmetry/.test(glsl));
  ok('the lattice honours each verse symmetry', /6\.28318 \/ sym/.test(glsl));
  ok('the lattice rotates', /t \* 0\.05/.test(glsl));
  ok('every medium is branched on', (() => {
    for (const m of [0, 1, 2, 3, 4, 5, 6]) {
      if (!new RegExp('medium < ' + (m + 0.5).toFixed(1)).test(glsl) && m < 6) return false;
    }
    return true;
  })());
  ok('the tint is a floor, not a flat wash', /col \+= tint \* 0\.30/.test(glsl));
}

// ------------------------------------------------------- the dome itself
{
  const sys = fs.readFileSync(SYSTEM, 'utf8');
  ok('the dome never writes depth', /disableDepthWrite = true/.test(sys));
  ok('the dome draws before everything', /renderingGroupId = 0/.test(sys));
  ok('the dome is not pickable', /isPickable = false/.test(sys));
  ok('the dome ignores fog', /applyFog = false/.test(sys));
  ok('the dome is never frustum-culled', /alwaysSelectAsActiveMesh = true/.test(sys));
  ok('the dome is seen from inside', /flipFaces\(true\)/.test(sys));
  ok('the dome stays inside the far plane', (() => {
    const m = sys.match(/SKY_RADIUS = (\d+)/);
    return m && Number(m[1]) * 1 < 4000;
  })());
  ok('a failed sky cannot stop the scene rendering',
    /Procedural sky unavailable/.test(sys));
}

// ----------------------------------------------------------- zoom control
{
  const out = '/tmp/cosmicsky-' + Date.now() + '.mjs';
  try {
    execFileSync('npx', ['esbuild', SYSTEM, '--bundle', '--format=esm',
      '--platform=node', '--log-level=error', '--outfile=' + out], { stdio: 'pipe' });
    const { advanceZoom, MAX_ZOOM } = await import(out);

    ok('zoom only advances in the fractal verse',
      advanceZoom(1, 0.1, true, 'stars') === 1);
    ok('zoom holds when not thrusting',
      advanceZoom(4, 0.1, false, 'fractal') === 4);
    ok('zoom grows while thrusting',
      advanceZoom(1, 0.1, true, 'fractal') > 1);
    ok('zoom is bounded',
      advanceZoom(MAX_ZOOM, 10, true, 'fractal') <= MAX_ZOOM);
    ok('zoom never drops below 1',
      advanceZoom(0.001, 0.1, true, 'fractal') >= 1);
    ok('zoom survives a NaN dt',
      Number.isFinite(advanceZoom(2, NaN, true, 'fractal')));
    ok('zoom survives a NaN input',
      Number.isFinite(advanceZoom(NaN, 0.1, true, 'fractal')));
    ok('a huge dt cannot explode the zoom',
      Number.isFinite(advanceZoom(2, 1e6, true, 'fractal')));
    ok('leaving the fractal verse resets the zoom',
      advanceZoom(500, 0.1, true, 'code') === 1);
    fs.unlinkSync(out);
  } catch (e) {
    ok('the sky system bundles and its zoom behaves', false, e.message);
  }
}

console.log(pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
