/**
 * galaxyfog-check — the volumetric galaxy fog.
 *
 * The fog is the one thing in the project that cannot be judged by reading
 * the source: whether it looks like cloud or like a grey sheet is decided
 * by numbers that only appear after 48 integration steps. So this harness
 * transliterates the SHIPPED fragment shader and measures its real output.
 *
 * The constants are parsed out of the shader text rather than duplicated
 * here, so the model cannot silently drift away from the code it checks.
 *
 * Three regressions are pinned, each one a bug that actually shipped:
 *   - alpha 1.000 everywhere: the fog was an opaque sheet, which is what
 *     read as a flat white mask bleaching the core.
 *   - saturation 0.04: integrating many hues averages them to grey.
 *   - a lift on the sky tint that made "black" space navy.
 */

import fs from 'node:fs';

let passed = 0, failed = 0;
const ok = (name, cond, extra) => {
  if (cond) { passed++; console.log('  PASS  ' + name); }
  else { failed++; console.log('FAIL: ' + name + (extra ? ' — ' + extra : '')); }
};

const SHADER = 'src/bjs/shaders/GalaxyFogShader.ts';
if (!fs.existsSync(SHADER)) {
  console.log('FAIL: ' + SHADER + ' is missing');
  console.log('0 passed, 1 failed');
  process.exit(1);
}
const src = fs.readFileSync(SHADER, 'utf8');
const frag = src.split('GALAXY_FOG_FRAG = `')[1].split('`')[0];

// ------------------------------------------------- constants from source
const num = (re) => { const m = frag.match(re); return m ? parseFloat(m[1]) : NaN; };
const SECTOR_FREQ = num(/SECTOR_FREQ = ([\d.]+)/);
const TINT_AMOUNT = num(/TINT_AMOUNT = ([\d.]+)/);
const SAT_REC = num(/SATURATION_RECOVERY = ([\d.]+)/);
const EXTC = num(/density \* ([\d.e-]+);/);
const STEPS = num(/STEPS = (\d+)/);

ok('the shader declares every tuning constant',
  [SECTOR_FREQ, TINT_AMOUNT, SAT_REC, EXTC, STEPS].every(Number.isFinite));

// ------------------------------------------------------------ structure
ok('the fog is raymarched, not a billboard', /for \(int i = 0; i < STEPS/.test(frag));
ok('it integrates front-to-back with transmittance', /trans \*= exp\(-ext\)/.test(frag));
ok('it dithers the entry point to kill banding', /jitter/.test(frag));
ok('it early-outs when the medium is opaque', /if \(trans < 0\.004\) break;/.test(frag));
ok('dust absorbs as well as emits', /dustAt/.test(frag));
ok('there is no image sampler anywhere in the fog',
  !/sampler2D|samplerCube|texture2D|textureCube/.test(frag));
ok('there is no sine-driven time loop', !/sin\s*\(\s*time/.test(frag));
ok('tone mapping runs on luminance, not per channel',
  /dot\(acc, vec3\(0\.2126/.test(frag));
ok('a hard anti-bleach clamp exists', /0\.94 \/ pk/.test(frag));
ok('saturation is recovered after integration',
  /mean \+ \(col - mean\)/.test(frag));

// ------------------------------------------------ transliterated shader
const innerR = 2000, outerR = 50000, thickness = 0.06;
const arms = 2, armFactor = 2.6, density = 1;
const clamp = (v, a, b) => Math.max(a, Math.min(b, v));
const ss = (e0, e1, x) => { const t = clamp((x - e0) / (e1 - e0), 0, 1); return t * t * (3 - 2 * t); };

function hash33(x, y, z) {
  const d = (p, q, r) => x * p + y * q + z * r;
  const f = (v) => { const s = Math.sin(v) * 43758.5453123; return (s - Math.floor(s)) * 2 - 1; };
  return [f(d(127.1, 311.7, 74.7)), f(d(269.5, 183.3, 246.1)), f(d(113.5, 271.9, 124.6))];
}
function vnoise(x, y, z) {
  const fl = Math.floor, fr = (v) => v - Math.floor(v);
  const ix = fl(x), iy = fl(y), iz = fl(z), fx = fr(x), fy = fr(y), fz = fr(z);
  const u = (v) => v * v * (3 - 2 * v), ux = u(fx), uy = u(fy), uz = u(fz);
  const G = (dx, dy, dz) => {
    const h = hash33(ix + dx, iy + dy, iz + dz);
    return h[0] * (fx - dx) + h[1] * (fy - dy) + h[2] * (fz - dz);
  };
  const m = (a, b, t) => a + (b - a) * t;
  return m(m(m(G(0, 0, 0), G(1, 0, 0), ux), m(G(0, 1, 0), G(1, 1, 0), ux), uy),
    m(m(G(0, 0, 1), G(1, 0, 1), ux), m(G(0, 1, 1), G(1, 1, 1), ux), uy), uz) * 0.5 + 0.5;
}
function fbm(x, y, z, o) {
  let s = 0, a = 0.5, n = 0;
  for (let i = 0; i < o; i++) { s += vnoise(x, y, z) * a; n += a; a *= 0.5; x *= 2.07; y *= 2.07; z *= 2.07; }
  return n > 0 ? s / n : 0;
}
const DISC_HEIGHT = num(/DISC_HEIGHT = ([\d.]+)/);
const ARM_SHARPNESS = num(/ARM_SHARPNESS = ([\d.]+)/);
const ARM_FLOOR = num(/ARM_FLOOR = ([\d.]+)/);
const DISC_GAIN = num(/DISC_GAIN = ([\d.]+)/);
const BULGE_RADIUS = num(/BULGE_RADIUS = ([\d.]+)/);
const BULGE_GAIN = num(/BULGE_GAIN = ([\d.]+)/);
const BULGE_FLATTEN = num(/BULGE_FLATTEN = ([\d.]+)/);
const DUST_FREQ = num(/DUST_FREQ = ([\d.]+)/);
const DUST_SHARPNESS = num(/DUST_SHARPNESS = ([\d.]+)/);
const DUST_CUT = num(/DUST_CUT = ([\d.]+)/);
const DUST_THRESHOLD = num(/DUST_THRESHOLD = ([\d.]+)/);

function galaxyDensity(px, py, pz) {
  const r = Math.hypot(px, pz);
  const rim = 1 - ss(outerR * 0.86, outerR * 1.30, r);
  if (rim <= 0) return 0;
  const h = Math.max(outerR * DISC_HEIGHT + r * 0.012, outerR * 0.008);
  const plane = Math.exp(-(py * py) / (2 * h * h));
  const ang = Math.atan2(pz, px);
  const arm = armFactor * Math.log(Math.max(r, innerR) / Math.max(innerR, 1));
  const armWave = Math.cos(ang * arms - arm * arms) * 0.5 + 0.5;
  let armMask = ARM_FLOOR + (1 - ARM_FLOOR) * Math.pow(armWave, ARM_SHARPNESS);
  armMask = 1 + (armMask - 1) * ss(innerR * 0.5, outerR * 0.22, r);
  const radial = Math.exp(-r / (outerR * 0.42)) * rim;
  const sc = 1.6 / outerR;
  let clump = fbm(px * sc * 2.2, py * sc * 2.2, pz * sc * 2.2, 5);
  clump = 0.35 + 0.65 * ss(0.10, 0.88, clump);
  const disc = plane * radial * armMask * clump * DISC_GAIN;
  const br = Math.hypot(px, py / BULGE_FLATTEN, pz);
  const bulge = Math.exp(-Math.pow(br / Math.max(outerR * BULGE_RADIUS, 1), 1.7)) * BULGE_GAIN;
  return clamp(disc + bulge, 0, 1);
}
function dustAt(px, py, pz) {
  const r = Math.hypot(px, pz);
  const ang = Math.atan2(pz, px);
  const wind = armFactor * Math.log(Math.max(r, innerR) / Math.max(innerR, 1));
  const sh = ang - wind;
  const f = DUST_FREQ / outerR;
  const n = fbm(Math.cos(sh) * r * f, py * 2.4 * f, Math.sin(sh) * r * f, 5);
  let ridged = 1 - Math.abs(n - 0.5) * 2;
  ridged = ss(DUST_THRESHOLD, 1, ridged);
  const band = ss(innerR * 0.7, outerR * 0.30, r) * (1 - ss(outerR * 0.72, outerR * 1.05, r));
  const layer = Math.exp(-(py * py) / (2 * Math.pow(outerR * 0.022, 2)));
  return Math.pow(Math.max(ridged, 0), DUST_SHARPNESS) * band * layer;
}
function gasColor(px, py, pz, d) {
  const r = Math.hypot(px, pz), t = clamp(r / outerR, 0, 1);
  const m3 = (a, b, k) => [a[0] + (b[0] - a[0]) * k, a[1] + (b[1] - a[1]) * k, a[2] + (b[2] - a[2]) * k];
  const CORE = [1.00, 0.90, 0.60], DISC = [0.50, 0.64, 1.00], HALO = [0.12, 0.14, 0.40];
  let base = t < 0.32 ? m3(CORE, DISC, t / 0.32) : m3(DISC, HALO, (t - 0.32) / 0.68);
  const sf = SECTOR_FREQ / outerR;
  const f1 = fbm(px * sf + 4.1, py * sf + 4.1, pz * sf + 4.1, 3);
  const f2 = fbm(px * sf * 1.3 - 11.7, py * sf * 1.3 - 11.7, pz * sf * 1.3 - 11.7, 3);
  const f3 = fbm(px * sf * 1.7 + 27.3, py * sf * 1.7 + 27.3, pz * sf * 1.7 + 27.3, 3);
  const CR = [1.00, 0.13, 0.26], TE = [0.06, 0.92, 0.88], OR = [1.00, 0.52, 0.08];
  const w1 = Math.pow(ss(0.35, 0.75, f1), 3);
  const w2 = Math.pow(ss(0.35, 0.75, f2), 3);
  const w3 = Math.pow(ss(0.35, 0.75, f3), 3);
  const wsum = w1 + w2 + w3;
  if (wsum > 1e-4) {
    const hue = [(CR[0] * w1 + TE[0] * w2 + OR[0] * w3) / wsum,
      (CR[1] * w1 + TE[1] * w2 + OR[1] * w3) / wsum,
      (CR[2] * w1 + TE[2] * w2 + OR[2] * w3) / wsum];
    base = m3(base, hue, ss(0, 0.55, wsum) * TINT_AMOUNT * ss(0.06, 0.34, t));
  }
  base = m3(base, base.map((v) => v * 1.22), ss(0.25, 0.95, d));
  const peak = Math.max(...base);
  if (peak > 1) base = base.map((v) => v / peak);
  return base;
}
function galaxySpan(ro, rd, R) {
  const b = ro[0] * rd[0] + ro[1] * rd[1] + ro[2] * rd[2];
  const c = ro[0] * ro[0] + ro[1] * ro[1] + ro[2] * ro[2] - R * R;
  let h = b * b - c;
  if (h < 0) return [1, -1];
  h = Math.sqrt(h);
  return [-b - h, -b + h];
}
function march(camPos, dir, marchFar, satRec = SAT_REC) {
  const R = outerR * 1.30;
  const span = galaxySpan(camPos, dir, R);
  if (span[1] < span[0]) return { col: [0, 0, 0], alpha: 0 };
  const t0 = Math.max(span[0], 0), t1 = Math.min(span[1], Math.max(marchFar, 1));
  if (t1 <= t0) return { col: [0, 0, 0], alpha: 0 };
  const far = t1 - t0, dt = far / STEPS;
  let acc = [0, 0, 0], trans = 1;
  for (let i = 0; i < STEPS; i++) {
    const s = t0 + (i + 0.5) * dt;
    const p = [camPos[0] + dir[0] * s, camPos[1] + dir[1] * s, camPos[2] + dir[2] * s];
    const d = galaxyDensity(p[0], p[1], p[2]);
    if (d > 0.002) {
      const ext = (d * 0.85 + dustAt(p[0], p[1], p[2]) * 1.9) * dt * density * EXTC;
      const absorbed = 1 - Math.exp(-ext);
      const dust = dustAt(p[0], p[1], p[2]);
      const emit = gasColor(p[0], p[1], p[2], d).map((v) => v * (1 - DUST_CUT * dust));
      for (let k = 0; k < 3; k++) acc[k] += emit[k] * trans * absorbed;
      trans *= Math.exp(-ext);
      if (trans < 0.004) break;
    }
  }
  const lum = acc[0] * 0.2126 + acc[1] * 0.7152 + acc[2] * 0.0722;
  const mapped = lum / (lum + 0.85);
  let col = lum > 1e-5 ? acc.map((v) => v * (mapped / lum)) : [0, 0, 0];
  const mean = (col[0] + col[1] + col[2]) / 3;
  col = col.map((v) => Math.max(0, mean + (v - mean) * (1 + satRec)));
  const pk = Math.max(...col);
  if (pk > 0.94) col = col.map((v) => v * 0.94 / pk);
  col = col.map((v) => Math.pow(Math.max(v, 0), 1 / 2.2));
  return { col, alpha: clamp(1 - trans, 0, 1) };
}

const sat = (c) => { const mx = Math.max(...c), mn = Math.min(...c); return mx > 1e-6 ? (mx - mn) / mx : 0; };
const SAMPLES = [
  [-26000, 300, 0], [-14000, 200, 9000], [-30000, -400, -16000],
  [6000, 300, 20000], [-20000, 0, -8000], [-9000, 600, 14000]
];
const results = SAMPLES.map((p) => march(p, [1, 0, 0], 130000));

// ------------------------------------------------------- NOT AN OPAQUE SHEET
// The bug: extinction 0.0016 over a ~2,700-unit step saturated instantly,
// so alpha was 1.000 everywhere and the disc was a solid white mask.
ok('the fog is translucent, never a solid sheet',
  results.every((r) => r.alpha < 0.95),
  'max alpha ' + Math.max(...results.map((r) => r.alpha)).toFixed(3));
ok('the fog is still substantial enough to see',
  results.some((r) => r.alpha > 0.3));
// NEGATIVE CONTROL: the old coefficient must FAIL the test above, or the
// test is not actually detecting the regression it was written for.
{
  const old = (() => {
    const dt = 130000 / STEPS;
    let trans = 1;
    for (let i = 0; i < STEPS; i++) {
      const s = (i + 0.5) * dt;
      const p = [-26000 + s, 300, 0];
      const d = galaxyDensity(p[0], p[1], p[2]);
      if (d > 0.002) {
        const ext = (d * 0.85 + dustAt(p[0], p[1], p[2]) * 1.9) * dt * 0.0016;
        trans *= Math.exp(-ext);
        if (trans < 0.004) break;
      }
    }
    return 1 - trans;
  })();
  ok('NEGATIVE CONTROL: the old extinction really was opaque', old > 0.99,
    'old alpha ' + old.toFixed(4));
}

// ------------------------------------------------------------ NOT GREY
const meanSat = results.reduce((s, r) => s + sat(r.col), 0) / results.length;
ok('the fog carries real colour, not grey', meanSat > 0.25,
  'mean saturation ' + meanSat.toFixed(3));

let spread = 0;
for (let i = 0; i < results.length; i++)
  for (let j = i + 1; j < results.length; j++)
    spread = Math.max(spread, Math.hypot(
      results[i].col[0] - results[j].col[0],
      results[i].col[1] - results[j].col[1],
      results[i].col[2] - results[j].col[2]));
ok('different sectors are visibly different colours', spread > 0.25,
  'spread ' + spread.toFixed(3));

// NEGATIVE CONTROL: without the saturation recovery the field IS grey, so
// the assertion above is measuring the fix rather than passing for free.
{
  // Re-march with the recovery disabled, rather than trying to invert it:
  // gamma and the 0.94 clamp are not invertible, and a wrong inverse made
  // this control read 0.281 and fail.
  const flat = SAMPLES.map((p) => march(p, [1, 0, 0], 130000, 0));
  const flatSat = flat.reduce((s, r) => s + sat(r.col), 0) / flat.length;
  ok('NEGATIVE CONTROL: without recovery the fog is grey', flatSat < 0.25,
    'ungraded saturation ' + flatSat.toFixed(3));
  ok('the recovery is what supplies the colour', meanSat > flatSat * 1.5,
    meanSat.toFixed(3) + ' vs ' + flatSat.toFixed(3));
}

// -------------------------------------------------------- NEVER BLEACHES
ok('no sample bleaches to white',
  results.every((r) => Math.max(...r.col) <= 0.9401));
// Most sightlines must keep a hue, but not every single one.
//
// A ray fired radially through the galactic centre crosses ~100,000 units
// and every colour sector on the way, so it genuinely averages to near
// grey - that is correct physics, not a bug, and demanding that EVERY ray
// be vivid would pin a false requirement into the harness.
ok('the large majority of sightlines keep a hue',
  results.filter((r) => sat(r.col) > 0.15).length >= results.length - 1,
  results.map((r) => sat(r.col).toFixed(2)).join(' '));

// ------------------------------------------------------ VACUUM IS BLACK
const empty = march([-26000, 90000, 0], [0, 1, 0], 130000);
ok('empty space above the disc contributes nothing',
  empty.alpha < 1e-6 && Math.max(...empty.col) < 1e-6);

// --------------------------------------------------------- SMOOTHNESS
// Cloud, not grain: neighbouring points must not differ wildly.
{
  let worst = 0;
  for (let i = 0; i < 240; i++) {
    const x = -30000 + i * 60;
    const a = galaxyDensity(x, 120, 4000);
    const b = galaxyDensity(x + 60, 120, 4000);
    worst = Math.max(worst, Math.abs(a - b));
  }
  ok('the density field is continuous, not grainy', worst < 0.12,
    'largest neighbouring jump ' + worst.toFixed(4));
}
ok('the rim fades out instead of cutting off', /smoothstep\(outerR \* 0\.86/.test(frag));
ok('the disc height is a fixed fraction of the galaxy, not of local radius',
  /outerR \* DISC_HEIGHT \+ r \* 0\.012/.test(frag));

// ------------------------------------------------------- STAR OCCLUSION
{
  const field = fs.readFileSync('src/bjs/systems/GalaxyField.ts', 'utf8');
  ok('star brightness is driven by the local fog density',
    /gasDensity'?, vis\)/.test(field) || /setFloat\('gasDensity', vis\)/.test(field));
  ok('the occlusion ramp is smooth, not a hard switch',
    /smoothstep01\(0\.16, 0\.62, dens\)/.test(field));
  ok('the fog density used for occlusion is the real field',
    /const dens = fogAt\(eye\.x, eye\.y, eye\.z\)/.test(field));
}

// -------------------------------------------------------- BLACK VACUUM
{
  const sky = fs.readFileSync('src/bjs/shaders/CosmicSkyShader.ts', 'utf8');
  ok('ordinary space gets no tint lift at all',
    /if \(medium >= 0\.5\) col \+= tint \* 0\.30;/.test(sky));
  ok('the exotic verses keep their floor', /col \+= tint \* 0\.30/.test(sky));

  for (const w of ['src/bjs/worlds/PlanetaryWorld.ts', 'src/bjs/worlds/SandboxWorld.ts']) {
    const t = fs.readFileSync(w, 'utf8');
    ok(w.split('/').pop() + ' clears to pure black',
      /clearColor = new Color4\(0, 0, 0, 1\)/.test(t));
  }
}

// ------------------------------------------- VISIBLE FROM OUTSIDE (turn N)
// The regression: marchFar was a fixed 130,000 units over 48 steps, giving
// a 2,708-unit step, while the disc is only 600-3,000 units thick. Seen
// face-on a ray therefore crossed the whole galaxy in UNDER ONE SAMPLE and
// alpha measured 0.003 - the galaxy nearly vanished from outside, while
// still looking dense edge-on where the ray runs along the disc.
{
  ok('the march is clipped to the galaxy volume', /galaxySpan\(camPos, dir, R\)/.test(frag));
  ok('a ray that misses the galaxy costs nothing',
    /if \(span\.y < span\.x\) \{ gl_FragColor = vec4\(0\.0\); return; \}/.test(frag));

  // Sample the brightest point on each ring. Fixed sightlines are no
  // longer valid: now that the arms are sharp, an arbitrary ray may pass
  // through an inter-arm GAP, and a near-empty gap is correct behaviour
  // rather than a faint galaxy.
  const ringPeak = (rr, camY) => {
    let best = 0;
    for (let a = 0; a < 360; a += 15) {
      const rad = a * Math.PI / 180;
      const m = march([Math.cos(rad) * rr, camY, Math.sin(rad) * rr], [0, 1, 0], 400000);
      if (m.alpha > best) best = m.alpha;
    }
    return best;
  };
  ok('the galaxy is clearly visible face-on from outside',
    ringPeak(0, -70000) > 0.15 && ringPeak(18000, -90000) > 0.10,
    'core ' + ringPeak(0, -70000).toFixed(3) + ' arm ' + ringPeak(18000, -90000).toFixed(3));

  // NEGATIVE CONTROL, measured where the failure actually was.
  //
  // Two separate defects were tangled together here. Close in, the fixed
  // 130,000-unit march does reach the disc and looks fine - which is why a
  // naive control passed and had to be rewritten. The failure is at RANGE:
  // the march always started at the camera and ran a fixed length, so once
  // the galaxy was further away than that length the ray stopped short and
  // never touched it at all. Clipping to the bounding sphere puts every
  // sample inside the galaxy no matter how far away the viewer is.
  {
    const sweep = (clipped, camY) => {
      const origin = [8000, camY, 0], dir = [0, 1, 0];
      let t0 = 0, far = 130000;
      if (clipped) {
        const span = galaxySpan(origin, dir, outerR * 1.30);
        if (span[1] < span[0]) return 0;
        t0 = Math.max(span[0], 0);
        far = Math.min(span[1], 400000) - t0;
        if (far <= 0) return 0;
      }
      const dt = far / STEPS;
      let trans = 1;
      for (let i = 0; i < STEPS; i++) {
        const s = t0 + (i + 0.5) * dt;
        const p = [origin[0], camY + s, 0];
        const d = galaxyDensity(p[0], p[1], p[2]);
        if (d > 0.002) {
          const ext = (d * 0.85 + dustAt(p[0], p[1], p[2]) * 1.9) * dt * EXTC;
          trans *= Math.exp(-ext);
        }
      }
      return 1 - trans;
    };
    const FAR = -190000;
    const before = sweep(false, FAR), after = sweep(true, FAR);
    ok('NEGATIVE CONTROL: the fixed-length march never reached a distant galaxy',
      before < 0.01, 'unclipped alpha ' + before.toFixed(4));
    ok('clipping keeps a distant galaxy visible', after > 0.05,
      'clipped ' + after.toFixed(4) + ' vs unclipped ' + before.toFixed(4));
    ok('the galaxy does not fade out as you back away',
      Math.abs(sweep(true, -90000) - sweep(true, -260000)) < 0.08);
  }
}

// ------------------------------------------------- THE BULGE (turn N)
// Without a bulge term the disc envelope collapses to ~600 units near the
// axis, so a face-on ray through the CENTRE crossed less gas than one
// through the arms and the brightest part of a galaxy rendered dimmest.
{
  ok('a central bulge exists', /float bulge = exp/.test(frag));
  ok('the bulge is a spheroid, not part of the thin disc',
    /length\(vec3\(p\.x, p\.y \/ BULGE_FLATTEN, p\.z\)\)/.test(frag));

  const lum = (c) => c[0] * 0.2126 + c[1] * 0.7152 + c[2] * 0.0722;
  const at = (rr) => march([rr, -70000, 0], [0, 1, 0], 400000);
  const core = at(0), mid = at(20000), outer = at(34000);

  ok('the core is the brightest part of the galaxy',
    lum(core.col) > lum(mid.col) && lum(core.col) > lum(outer.col),
    'core ' + lum(core.col).toFixed(3) + ' mid ' + lum(mid.col).toFixed(3));
  ok('the core is creamy gold, not blue',
    core.col[0] >= core.col[1] && core.col[1] >= core.col[2],
    core.col.map((v) => v.toFixed(2)).join(','));
  ok('the core still does not bleach to white',
    Math.max(...core.col) <= 0.9401);
  ok('the arms keep their emission colour', sat(outer.col) > 0.25,
    'arm saturation ' + sat(outer.col).toFixed(3));
  ok('brightness falls off toward the rim', lum(outer.col) > lum(at(46000).col));

  // The bulge must not swallow the whole galaxy.
  ok('the bulge is confined to the centre',
    galaxyDensity(0, 0, 0) > galaxyDensity(40000, 0, 0));
}

// ------------------------------------------------ ARM CONTRAST (turn N)
{
  ok('the arms follow a logarithmic spiral',
    /armFactor \* log\(max\(r, innerR\) \/ max\(innerR, 1\.0\)\)/.test(frag));
  ok('the arm mask is sharpened by a high power',
    /pow\(armWave, ARM_SHARPNESS\)/.test(frag) && ARM_SHARPNESS >= 3);
  ok('the inter-arm floor is low enough to leave dark vacuum',
    ARM_FLOOR <= 0.12, 'floor ' + ARM_FLOOR);
  ok('the spiral fades out inside the bulge rather than winding into it',
    /mix\(1\.0, armMask, smoothstep\(innerR \* 0\.5/.test(frag));
}

// ------------------------------------ CORES ON THE PLANE (turn N)
{
  const us = fs.readFileSync('src/bjs/systems/UniverseState.ts', 'utf8');
  const cu = fs.readFileSync('src/bjs/systems/ChunkedUniverse.ts', 'utf8');
  ok('the galactic plane is a named constant', /GALACTIC_PLANE_Y = 0/.test(us));
  ok('authored cores are pinned to the plane',
    /new Vector3\(at\.x, GALACTIC_PLANE_Y, at\.z\)/.test(us));
  ok('streamed cores are pinned to the plane',
    /new Vector3\(g\.x, GALACTIC_PLANE_Y, g\.z\)/.test(cu));
  ok('chunk-scale galaxy regions no longer exist',
    !/kind = roll < 0\.38 \? 'galaxy'/.test(cu));
}

// ------------------------------------- APPROACH GLARE CLAMP (turn N)
{
  const app = fs.readFileSync('src/bjs/App.ts', 'utf8');
  ok('bloom is pulled down as a horizon closes',
    /bloomBeforeHorizon \* \(1 - 0\.82 \* proximity\)/.test(app));
  ok('the pre-clamp bloom is captured once, not re-read each frame',
    /if \(!this\.bloomClamped\) \{\s*\n\s*this\.bloomBeforeHorizon = this\.postfx\.settings\.bloom;/.test(app));
  ok('bloom is restored on leaving', /this\.postfx\.set\('bloom', this\.bloomBeforeHorizon\);/.test(app));
  ok('the clamp is driven by the same depth as the transition',
    /const depth = this\.universe\.horizonDepth;/.test(app));

  // The ramp itself, simulated: it must fall smoothly, hold stable, and
  // restore exactly - a naive version that re-reads the live setting
  // ratchets bloom to zero and never comes back.
  let bloom = 0.55, clamped = false, before = 0.55;
  const step = (prox) => {
    if (prox > 0.001) {
      if (!clamped) { before = bloom; clamped = true; }
      bloom = before * (1 - 0.82 * prox);
    } else if (clamped) { bloom = before; clamped = false; }
    return bloom;
  };
  const ramp = [0, 0.25, 0.5, 0.75, 1].map(step);
  let mono = true;
  for (let i = 1; i < ramp.length; i++) if (ramp[i] > ramp[i - 1]) mono = false;
  ok('the glare falls monotonically as the horizon closes', mono);
  ok('bloom never reaches zero: a hole still glows', ramp[ramp.length - 1] > 0);
  ok('the frame is not blown out at the crossing', ramp[ramp.length - 1] < 0.2);
  let held;
  for (let i = 0; i < 100; i++) held = step(1);
  ok('holding at the horizon does not ratchet bloom away',
    Math.abs(held - ramp[ramp.length - 1]) < 1e-9);
  ok('bloom is restored exactly on leaving', Math.abs(step(0) - 0.55) < 1e-9);
}

// ============================ SPIRAL STRUCTURE (turn O) ============================
// The regression this replaces: a bulge at 0.30 x outerR with amplitude 9.0
// clamped density to 1.0 across HALF the galaxy radius. A saturated sphere
// has no structure at all - arms cannot show through something already at
// maximum density - which is what produced the airbrushed cotton-candy blob.
{
  // ---- the blob must not come back ----
  let sat1 = 0, tot = 0;
  for (let x = -50000; x <= 50000; x += 2000)
    for (let z = -50000; z <= 50000; z += 2000) {
      if (galaxyDensity(x, 0, z) >= 0.999) sat1++;
      tot++;
    }
  ok('the galaxy is not a saturated blob', sat1 / tot < 0.05,
    (100 * sat1 / tot).toFixed(1) + '% of the disc pinned at max density');

  // NEGATIVE CONTROL: the old bulge really did saturate half the galaxy.
  {
    let oldSat = 0, oldTot = 0;
    for (let x = -50000; x <= 50000; x += 2000)
      for (let z = -50000; z <= 50000; z += 2000) {
        const br = Math.hypot(x, 0, z);
        if (Math.exp(-Math.pow(br / (outerR * 0.30), 1.6)) * 9.0 >= 1.0) oldSat++;
        oldTot++;
      }
    ok('NEGATIVE CONTROL: the old bulge saturated a huge fraction of the disc',
      oldSat / oldTot > 0.15, (100 * oldSat / oldTot).toFixed(1) + '%');
  }

  // ---- the arms are a real logarithmic spiral ----
  // Peak-density angle must advance linearly with log(radius). Unwrapped
  // modulo 2pi/arms, because an N-arm pattern repeats at that period -
  // unwrapping modulo 2pi instead reports a spurious R^2 of 0.04.
  const period = 2 * Math.PI / arms;
  const pts = [];
  for (let r = 8000; r <= 44000; r += 1500) {
    let best = -1, bestA = 0;
    for (let a = 0; a < 360; a += 0.5) {
      const rad = a * Math.PI / 180;
      const d = galaxyDensity(Math.cos(rad) * r, 0, Math.sin(rad) * r);
      if (d > best) { best = d; bestA = rad; }
    }
    pts.push([Math.log(r), bestA]);
  }
  for (let i = 1; i < pts.length; i++) {
    while (pts[i][1] - pts[i - 1][1] > period / 2) pts[i][1] -= period;
    while (pts[i][1] - pts[i - 1][1] < -period / 2) pts[i][1] += period;
  }
  const n2 = pts.length;
  const sx = pts.reduce((q, p) => q + p[0], 0), sy = pts.reduce((q, p) => q + p[1], 0);
  const sxx = pts.reduce((q, p) => q + p[0] * p[0], 0);
  const sxy = pts.reduce((q, p) => q + p[0] * p[1], 0);
  const slope = (n2 * sxy - sx * sy) / (n2 * sxx - sx * sx);
  const icept = (sy - slope * sx) / n2;
  let ssr = 0, sst = 0; const my = sy / n2;
  for (const p of pts) { const pr = slope * p[0] + icept; ssr += (p[1] - pr) ** 2; sst += (p[1] - my) ** 2; }
  const r2 = 1 - ssr / sst;

  ok('the density ridge really is a logarithmic spiral', r2 > 0.95,
    'R^2 ' + r2.toFixed(4));
  ok('the winding rate matches armFactor', Math.abs(slope - armFactor) < 0.35,
    'slope ' + slope.toFixed(3) + ' vs ' + armFactor);

  // ---- arms vs gaps ----
  for (const rr of [12000, 22000, 34000]) {
    const vals = [];
    for (let a = 0; a < 360; a += 5) {
      const rad = a * Math.PI / 180;
      vals.push(galaxyDensity(Math.cos(rad) * rr, 0, Math.sin(rad) * rr));
    }
    const mx = Math.max(...vals), mn = Math.min(...vals);
    ok('arms stand out sharply from the gaps at r=' + rr,
      mx / Math.max(mn, 1e-6) > 6, 'contrast ' + (mx / Math.max(mn, 1e-6)).toFixed(1) + 'x');
  }
}

// ============================ DUST LANES (turn O) ============================
{
  ok('the dust field is sheared along the spiral', /float sheared = ang - wind;/.test(frag));
  ok('dust is cut by a threshold, not just a power',
    /ridged = smoothstep\(DUST_THRESHOLD, 1\.0, ridged\)/.test(frag));
  ok('dust subtracts from the emission, not only from transmittance',
    /emit \*= 1\.0 - DUST_CUT \* dust;/.test(frag));
  ok('dust hugs the mid-plane more tightly than the gas', /float layer = exp/.test(frag));

  let dmax = 0, dsum = 0, dcnt = 0;
  for (let x = -40000; x <= 40000; x += 800)
    for (let z = -40000; z <= 40000; z += 800) {
      const d = dustAt(x, 0, z);
      dmax = Math.max(dmax, d); dsum += d; dcnt++;
    }
  const dmean = dsum / dcnt;
  ok('dust forms localised lanes rather than a blanket', dmean < 0.20,
    'mean coverage ' + dmean.toFixed(3));
  ok('the lanes are deep where they do fall', dmax > 0.6, 'max ' + dmax.toFixed(3));

  // NEGATIVE CONTROL: without the threshold, ridged noise covers the disc.
  // fbm concentrates around 0.5, so 1-|n-0.5|*2 sits near 1.0 nearly
  // everywhere - measured 80% coverage, a grey wash rather than filaments.
  {
    let uns = 0, unc = 0;
    for (let x = -40000; x <= 40000; x += 1600)
      for (let z = -40000; z <= 40000; z += 1600) {
        const r = Math.hypot(x, z);
        const ang = Math.atan2(z, x);
        const wind = armFactor * Math.log(Math.max(r, innerR) / Math.max(innerR, 1));
        const sh = ang - wind, f = DUST_FREQ / outerR;
        const nn = fbm(Math.cos(sh) * r * f, 0, Math.sin(sh) * r * f, 5);
        const ridged = 1 - Math.abs(nn - 0.5) * 2;
        const band = ss(innerR * 0.7, outerR * 0.30, r) * (1 - ss(outerR * 0.72, outerR * 1.05, r));
        uns += Math.pow(Math.max(ridged, 0), DUST_SHARPNESS) * band; unc++;
      }
    ok('NEGATIVE CONTROL: unthresholded ridged noise is a blanket',
      uns / unc > 0.35, 'unthresholded mean ' + (uns / unc).toFixed(3));
  }
}

// ============================ FLAT PLANE (turn O) ============================
{
  ok('disc height is a fixed fraction of the galaxy radius',
    DISC_HEIGHT <= 0.05, 'DISC_HEIGHT ' + DISC_HEIGHT);
  let maxY = 0;
  for (let y = 0; y <= 40000; y += 250) if (galaxyDensity(9000, y, 0) > 0.02) maxY = y;
  ok('the galaxy is a flat plane, not a vertical cloud', maxY / outerR < 0.15,
    'reaches y=' + maxY + ' against radius ' + outerR);
  const flat = galaxyDensity(14000, 0, 0);
  ok('density falls off sharply with height',
    galaxyDensity(14000, 6000, 0) < flat * 0.5);
}

console.log(`\n${passed} passed, ${failed} failed`);
process.exit(failed ? 1 : 0);
