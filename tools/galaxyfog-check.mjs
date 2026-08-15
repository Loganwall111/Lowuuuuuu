/**
 * galaxyfog-check — the raymarched photorealistic galaxy.
 *
 * The fog is the one thing in the project that cannot be judged by reading
 * the source: whether it looks like a galaxy or like a violet smudge is
 * decided by numbers that only appear after 48 integration steps. So this
 * harness transliterates the SHIPPED fragment shader and measures its real
 * output.
 *
 * The constants are parsed out of the shader text rather than duplicated
 * here, so the model cannot silently drift away from the code it checks.
 *
 * WHAT IS PINNED HERE. Every one of these is a defect that actually
 * shipped and was found by rendering the shader offline and measuring the
 * image, not by reading the code:
 *
 *   - UNDERSAMPLING. 48 steps across a 130,000-unit bounding sphere is a
 *     2,700-unit step, but the disc is ~1,500 thick, so a face-on ray took
 *     LESS THAN ONE sample inside the disc and every fine feature was
 *     stepped over. This is why the galaxy was a soft smudge.
 *   - AN UNTRUNCATED BULGE lit the entire sky to luminance 0.14, including
 *     sightlines pointing away from the galaxy. Deep space was grey.
 *   - A SMOOTH OLD-STAR PEDESTAL. With the old population not following
 *     the arms, measured ring contrast collapsed from 11:1 to 2.6:1.
 *   - GAMMA LIFTING THE FLOOR. From inside the disc, linear contrast was
 *     already 45:1, but 1/2.2 gamma turns linear 0.016 into sRGB 0.16, so
 *     faint inter-arm gas washed the whole screen grey.
 *   - RING DEGENERACY. arms * armFactor * ln(outer/inner) / 2pi is the
 *     number of radial cycles; the old 4-arm/4.2 config gave 7.71 and
 *     rendered as concentric rings rather than as spiral arms.
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
const DISC_HEIGHT = num(/DISC_HEIGHT = ([\d.]+)/);
const DISC_SCALE = num(/DISC_SCALE = ([\d.]+)/);
const ARM_SHARPNESS = num(/ARM_SHARPNESS = ([\d.]+)/);
const ARM_FLOOR = num(/ARM_FLOOR = ([\d.]+)/);
const ARM_NOISE = num(/ARM_NOISE = ([\d.]+)/);
const BULGE_RE = num(/BULGE_RE = ([\d.]+)/);
const BULGE_POW = num(/BULGE_POW = ([\d.]+)/);
const BULGE_GAIN = num(/BULGE_GAIN = ([\d.]+)/);
const BULGE_CUT_IN = num(/BULGE_CUT_IN = ([\d.]+)/);
const BULGE_CUT_OUT = num(/BULGE_CUT_OUT = ([\d.]+)/);
const NUCLEUS_RE = num(/NUCLEUS_RE = ([\d.]+)/);
const NUCLEUS_POW = num(/NUCLEUS_POW = ([\d.]+)/);
const NUCLEUS_GAIN = num(/NUCLEUS_GAIN = ([\d.]+)/);
const DUST_FREQ = num(/DUST_FREQ = ([\d.]+)/);
const DUST_PHASE = num(/DUST_PHASE = ([\d.]+)/);
const DUST_BAND = num(/DUST_BAND = ([\d.]+)/);
const DUST_GAIN = num(/DUST_GAIN = ([\d.]+)/);
const DUST_HEIGHT = num(/DUST_HEIGHT = ([\d.]+)/);
const HII_FREQ = num(/HII_FREQ = ([\d.]+)/);
const HII_THRESH = num(/HII_THRESH = ([\d.]+)/);
const HII_GAIN = num(/HII_GAIN = ([\d.]+)/);
const YOUNG_GAIN = num(/YOUNG_GAIN = ([\d.]+)/);
const YOUNG_RING = num(/YOUNG_RING = ([\d.]+)/);
const YOUNG_R0 = num(/YOUNG_R0 = ([\d.]+)/);
const YOUNG_RW = num(/YOUNG_RW = ([\d.]+)/);
const YOUNG_H = num(/YOUNG_H = ([\d.]+)/);
const OLD_ARM = num(/OLD_ARM = ([\d.]+)/);
const K_GAS = num(/K_GAS = ([\d.]+)/);
const K_DUST = num(/K_DUST = ([\d.]+)/);
const SIGMA = num(/SIGMA = ([\d.e-]+);/);
const EMIT = num(/EMIT = ([\d.e-]+);/);
const DUST_CUT = num(/DUST_CUT = ([\d.]+)/);
const WHITE = num(/WHITE = ([\d.]+)/);
const TOE = num(/TOE = ([\d.]+)/);
const SAT_REC = num(/SATURATION_RECOVERY = ([\d.]+)/);
const SLAB_H = num(/SLAB_H = ([\d.]+)/);
const ANOMALY_FREQ = num(/ANOMALY_FREQ = ([\d.]+)/);
const ANOMALY_THRESHOLD = num(/ANOMALY_THRESHOLD = ([\d.]+)/);
const ANOMALY_GAIN = num(/ANOMALY_GAIN = ([\d.]+)/);
const STEPS = num(/STEPS = (\d+)/);

const vec3 = (name) => {
  const m = frag.match(new RegExp(name + ' = vec3\\(([\\d.]+), ([\\d.]+), ([\\d.]+)\\)'));
  return m ? [parseFloat(m[1]), parseFloat(m[2]), parseFloat(m[3])] : null;
};
const C_NUCLEUS = vec3('C_NUCLEUS'), C_BULGE = vec3('C_BULGE'),
  C_INNER = vec3('C_INNER'), C_ARM = vec3('C_ARM'), C_OUTER = vec3('C_OUTER'),
  C_HII = vec3('C_HII'), C_HA = vec3('C_HA'), C_OIII = vec3('C_OIII'),
  EXT_RGB = vec3('EXT_RGB');

const ALL = [DISC_HEIGHT, DISC_SCALE, ARM_SHARPNESS, ARM_FLOOR, ARM_NOISE,
  BULGE_RE, BULGE_POW, BULGE_GAIN, BULGE_CUT_IN, BULGE_CUT_OUT, NUCLEUS_RE,
  NUCLEUS_POW, NUCLEUS_GAIN, DUST_FREQ, DUST_PHASE, DUST_BAND, DUST_GAIN,
  DUST_HEIGHT, HII_FREQ, HII_THRESH, HII_GAIN, YOUNG_GAIN, YOUNG_RING,
  YOUNG_R0, YOUNG_RW, YOUNG_H, OLD_ARM, K_GAS, K_DUST, SIGMA, EMIT, DUST_CUT,
  WHITE, TOE, SAT_REC, SLAB_H, ANOMALY_FREQ, ANOMALY_THRESHOLD, ANOMALY_GAIN,
  STEPS];

ok('the shader declares every tuning constant', ALL.every(Number.isFinite));
ok('the shader declares every palette colour',
  [C_NUCLEUS, C_BULGE, C_INNER, C_ARM, C_OUTER, C_HII, C_HA, C_OIII, EXT_RGB]
    .every((c) => c && c.length === 3));

// ------------------------------------------------------------ structure
ok('the fog is raymarched, not a billboard', /for \(int i = 0; i < STEPS/.test(frag));
ok('it integrates front-to-back with transmittance', /trans \*= exp\(-sigma \* EXT_RGB\)/.test(frag));
ok('it dithers the entry point to kill banding', /jitter/.test(frag));
ok('it early-outs when the medium is opaque', /if \(dot\(trans, vec3\(1\.0\)\) < 0\.01\) break;/.test(frag));
ok('there is no image sampler anywhere in the fog',
  !/sampler2D|samplerCube|texture2D|textureCube/.test(frag));
ok('there is no sine-driven time loop', !/sin\s*\(\s*time/.test(frag));
ok('tone mapping runs on luminance, not per channel',
  /dot\(acc, vec3\(0\.2126/.test(frag));
ok('saturation is recovered after integration',
  /mean \+ \(col - mean\)/.test(frag));

// ------------------------------------------------ transliterated shader
const innerR = 2800, outerR = 50000, arms = 2, armFactor = 2.6, density = 1;
const clamp = (v, a, b) => Math.max(a, Math.min(b, v));
const ss = (e0, e1, x) => { const t = clamp((x - e0) / (e1 - e0), 0, 1); return t * t * (3 - 2 * t); };
const mix = (a, b, t) => a + (b - a) * t;
const mix3 = (a, b, t) => [mix(a[0], b[0], t), mix(a[1], b[1], t), mix(a[2], b[2], t)];
const lum = (c) => c[0] * 0.2126 + c[1] * 0.7152 + c[2] * 0.0722;
const sat = (c) => { const mx = Math.max(...c), mn = Math.min(...c); return mx > 1e-6 ? (mx - mn) / mx : 0; };

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
  const L = (a, b, t) => a + (b - a) * t;
  return L(
    L(L(G(0,0,0), G(1,0,0), ux), L(G(0,1,0), G(1,1,0), ux), uy),
    L(L(G(0,0,1), G(1,0,1), ux), L(G(0,1,1), G(1,1,1), ux), uy), uz) * 0.5 + 0.5;
}
function fbm(x, y, z, o) {
  let s = 0, a = 0.5, n = 0;
  for (let i = 0; i < o; i++) { s += vnoise(x, y, z) * a; n += a; a *= 0.5; x *= 2.07; y *= 2.07; z *= 2.07; }
  return n > 0 ? s / n : 0;
}

function armPhase(px, py, pz, r) {
  const ang = Math.atan2(pz, px);
  const wind = armFactor * Math.log(Math.max(r, innerR) / innerR);
  const k = 1.9 / outerR;
  const jitter = (fbm(px * k, py * k * 2.0, pz * k, 3) - 0.5) * ARM_NOISE;
  return ang * arms - wind * arms + jitter * arms;
}
function discProfile(px, py, pz, r) {
  const h = outerR * DISC_HEIGHT + r * 0.010;
  const plane = Math.exp(-(py * py) / (2 * h * h));
  const radial = Math.exp(-r / (outerR * DISC_SCALE));
  const rim = 1 - ss(outerR * 0.88, outerR * 1.25, r);
  return plane * radial * rim;
}
function armMask(px, py, pz, r) {
  const wave = Math.cos(armPhase(px, py, pz, r)) * 0.5 + 0.5;
  let m = ARM_FLOOR + (1 - ARM_FLOOR) * Math.pow(wave, ARM_SHARPNESS);
  const bk = 0.9 / outerR;
  m *= 0.55 + 0.95 * fbm(px * bk + 3.7, py * bk, pz * bk - 2.1, 3);
  return mix(1, m, ss(innerR * 0.5, outerR * 0.20, r));
}
function bulgeAt(px, py, pz) {
  const br = Math.hypot(px, py / 0.62, pz);
  const core = Math.exp(-Math.pow(br / (outerR * BULGE_RE), BULGE_POW));
  const cut = 1 - ss(outerR * BULGE_CUT_IN, outerR * BULGE_CUT_OUT, br);
  return core * cut * BULGE_GAIN;
}
function nucleusAt(px, py, pz) {
  const nr = Math.hypot(px, py / 0.80, pz);
  return Math.exp(-Math.pow(nr / (outerR * NUCLEUS_RE), NUCLEUS_POW)) * NUCLEUS_GAIN;
}
function gasAt(px, py, pz) {
  const r = Math.hypot(px, pz);
  const disc = discProfile(px, py, pz, r) * armMask(px, py, pz, r);
  const k = 2.4 / outerR;
  const clump = 0.18 + 0.82 * ss(0.15, 0.85, fbm(px * k, py * k * 2, pz * k, 4));
  return disc * clump;
}
function dustAt(px, py, pz) {
  const r = Math.hypot(px, pz);
  const wave = Math.cos(armPhase(px, py, pz, r) + DUST_PHASE) * 0.5 + 0.5;
  const lane = Math.pow(wave, DUST_BAND);
  const k = DUST_FREQ / outerR;
  const rag = ss(0.32, 0.78, fbm(px * k, py * k * 3.0, pz * k, 4));
  const h = outerR * DUST_HEIGHT;
  const layer = Math.exp(-(py * py) / (2 * h * h));
  const band = ss(outerR * 0.02, outerR * 0.14, r) * (1 - ss(outerR * 0.70, outerR * 1.0, r));
  return clamp(lane * rag * layer * band * DUST_GAIN, 0, 1);
}
function anomalyStrand(px, py, pz) {
  const r = Math.hypot(px, pz);
  const t = clamp(r / outerR, 0, 1);
  const wave = Math.cos(armPhase(px, py, pz, r)) * 0.5 + 0.5;
  const gaps = Math.pow(1 - wave, 2.0);
  const k = ANOMALY_FREQ / outerR;
  const sn = fbm(px * k + 51.7, py * k, pz * k + 51.7, 4);
  const strand = ss(ANOMALY_THRESHOLD, 1.0, 1 - Math.abs(sn - 0.5) * 2);
  const band = ss(0.05, 0.20, t) * (1 - ss(0.72, 1.05, t));
  const h = outerR * DISC_HEIGHT * 1.4;
  const layer = Math.exp(-(py * py) / (2 * h * h));
  return clamp(strand * gaps * band * layer, 0, 1);
}
function emissionAt(px, py, pz, anom = 0) {
  const r = Math.hypot(px, pz);
  const t = clamp(r / outerR, 0, 1);
  const disc = discProfile(px, py, pz, r);
  const armM = armMask(px, py, pz, r);

  const oldDisc = disc * 0.9 * mix(1, armM, OLD_ARM);
  const oldAmt = bulgeAt(px, py, pz) + oldDisc;
  let oldCol = mix3(C_BULGE, C_INNER, ss(0.02, 0.22, t));
  oldCol = mix3(oldCol, C_OUTER, ss(0.25, 0.85, t));

  const ridge = Math.pow(clamp((armM - ARM_FLOOR) / (1 - ARM_FLOOR), 0, 1), 1.4);
  const ringBoost = 1 + YOUNG_RING * Math.exp(-Math.pow((t - YOUNG_R0) / YOUNG_RW, 2));
  const yh = outerR * YOUNG_H;
  const thin = Math.exp(-(py * py) / (2 * yh * yh));
  const youngAmt = disc * thin * ridge * YOUNG_GAIN * ringBoost * ss(0.05, 0.20, t);

  const hk = HII_FREQ / outerR;
  const hn = fbm(px * hk + 17.3, py * hk * 3, pz * hk + 4.1, 3);
  const hii = ss(HII_THRESH, 0.95, hn) * ridge * disc * HII_GAIN * ringBoost;

  const nuc = nucleusAt(px, py, pz);
  const col = [0, 0, 0];
  for (let k = 0; k < 3; k++) {
    col[k] = oldCol[k] * oldAmt + C_NUCLEUS[k] * nuc + C_ARM[k] * youngAmt + C_HII[k] * hii;
  }
  if (anom > 0.5) {
    const strand = anomalyStrand(px, py, pz);
    const pick = fbm(px * (0.5 / outerR) + 8.3, py * (0.5 / outerR), pz * (0.5 / outerR), 3);
    const neon = mix3(C_HA, C_OIII, ss(0.40, 0.60, pick));
    for (let k = 0; k < 3; k++) col[k] += neon[k] * strand * ANOMALY_GAIN;
  }
  return col;
}
function galaxySpan(ro, rd, R) {
  const b = ro[0] * rd[0] + ro[1] * rd[1] + ro[2] * rd[2];
  const c = ro[0] * ro[0] + ro[1] * ro[1] + ro[2] * ro[2] - R * R;
  let h = b * b - c;
  if (h < 0) return [1, -1];
  h = Math.sqrt(h);
  return [-b - h, -b + h];
}
/** @param slab false disables slab clipping, for the negative control. */
function march(camPos, dir, anom = 0, jitter = 0.5, slab = true, toe = TOE) {
  const R = outerR * 1.30;
  const sp = galaxySpan(camPos, dir, R);
  if (sp[1] < sp[0]) return { col: [0, 0, 0], alpha: 0 };
  let t0 = Math.max(sp[0], 0), t1 = sp[1];
  if (slab) {
    const S = outerR * SLAB_H;
    if (Math.abs(dir[1]) > 1e-6) {
      const a = (-S - camPos[1]) / dir[1], b = (S - camPos[1]) / dir[1];
      t0 = Math.max(t0, Math.min(a, b)); t1 = Math.min(t1, Math.max(a, b));
    } else if (Math.abs(camPos[1]) > S) return { col: [0, 0, 0], alpha: 0 };
  }
  if (t1 <= t0) return { col: [0, 0, 0], alpha: 0 };
  const dt = (t1 - t0) / STEPS;
  const acc = [0, 0, 0], trans = [1, 1, 1];
  for (let i = 0; i < STEPS; i++) {
    const s = t0 + (i + jitter) * dt;
    const p = [camPos[0] + dir[0] * s, camPos[1] + dir[1] * s, camPos[2] + dir[2] * s];
    const g = gasAt(p[0], p[1], p[2]);
    const d = dustAt(p[0], p[1], p[2]);
    const e = emissionAt(p[0], p[1], p[2], anom);
    if (g < 1e-4 && e[0] + e[1] + e[2] < 1e-4) continue;
    const sigma = (g * K_GAS + d * K_DUST) * dt * density * SIGMA;
    const blocked = 1 - DUST_CUT * d;
    for (let k = 0; k < 3; k++) {
      acc[k] += e[k] * blocked * trans[k] * dt * EMIT;
      trans[k] *= Math.exp(-sigma * EXT_RGB[k]);
    }
    if (trans[0] + trans[1] + trans[2] < 0.01) break;
  }
  const L = lum(acc);
  let mapped = L * (1 + L / (WHITE * WHITE)) / (1 + L);
  mapped *= (L * L) / (L * L + toe * toe);
  let col = L > 1e-6 ? acc.map((v) => v * (mapped / L)) : [0, 0, 0];
  const mean = (col[0] + col[1] + col[2]) / 3;
  col = col.map((v) => Math.max(0, mean + (v - mean) * (1 + SAT_REC)));
  // Normalise AFTER the recovery: recovery pushes channels apart and can
  // otherwise undo a clamp applied before it.
  const pk = Math.max(...col);
  if (pk > 1) col = col.map((v) => v / pk);
  col = col.map((v) => Math.pow(Math.max(v, 0), 1 / 2.2));
  return { col, alpha: clamp(1 - (trans[0] + trans[1] + trans[2]) / 3, 0, 1) };
}

// A viewpoint outside the galaxy, looking at it from a three-quarter angle
// the way the reference photograph does.
const EYE = [30000, 26000, 36000];
const norm = (v) => { const l = Math.hypot(...v); return v.map((x) => x / l); };
const toward = (p) => norm([p[0] - EYE[0], p[1] - EYE[1], p[2] - EYE[2]]);

// ==================== GRAND-DESIGN GEOMETRY, NOT RINGS ====================
// The number of radial cycles a spiral shows across its disc is
// arms * armFactor * ln(outerBound/innerBound) / 2pi. Too many and adjacent
// windings merge and the galaxy renders as concentric rings.
{
  const field = fs.readFileSync('src/bjs/systems/GalaxyField.ts', 'utf8');
  const cfg = field.split('export const FIELD_GALAXY')[1].split('};')[0];
  const a = parseFloat(cfg.match(/arms: ([\d.]+)/)[1]);
  const af = parseFloat(cfg.match(/armFactor: ([\d.]+)/)[1]);
  ok('the rendered galaxy overrides the arm count explicitly',
    Number.isFinite(a) && Number.isFinite(af), 'arms ' + a + ' armFactor ' + af);
  ok('the harness tests the geometry the app actually renders',
    a === arms && af === armFactor,
    'app ' + a + '/' + af + ' vs harness ' + arms + '/' + armFactor);

  const cycles = (r) => a * af * Math.log(50000 / 2800) / (2 * Math.PI);
  ok('the spiral does not wind into concentric rings', cycles() < 4.0,
    cycles().toFixed(2) + ' radial cycles');
  const pitch = Math.atan(1 / af) * 180 / Math.PI;
  ok('the pitch angle is grand-design, not tightly coiled',
    pitch > 15 && pitch < 30, pitch.toFixed(1) + ' degrees');

  // NEGATIVE CONTROL: the previous configuration really did degenerate.
  const oldCycles = 4 * 4.2 * Math.log(50000 / 2800) / (2 * Math.PI);
  ok('NEGATIVE CONTROL: the old 4-arm config wound into rings', oldCycles > 6,
    oldCycles.toFixed(2) + ' cycles at arms=4 armFactor=4.2');
}

// ======================== DEEP SPACE IS PITCH BLACK ========================
// The regression: a Sersic bulge with index < 1 has a tail that never
// reaches zero, and it lit the ENTIRE sky to luminance 0.14 - including
// sightlines pointing directly away from the galaxy.
{
  const away = march(EYE, norm([1, 0.9, 1]));
  const above = march([0, 90000, 0], [0, 1, 0]);
  ok('a sightline pointing away from the galaxy is perfectly black',
    lum(away.col) < 1e-6, 'luminance ' + lum(away.col).toFixed(6));
  ok('empty space above the disc contributes nothing',
    above.alpha < 1e-6 && Math.max(...above.col) < 1e-6);
  ok('the bulge is explicitly truncated', /float cut = 1\.0 - smoothstep/.test(frag));

  // NEGATIVE CONTROL: without the truncation the bulge leaks light across
  // the whole disc and beyond. Measured as the INTEGRAL along a ray rather
  // than one far-field sample: a single sample at 200k is small, but the
  // march sums 48 of them and it is the sum that lit the sky.
  const rawTail = (r0, r1) => {
    let sum = 0;
    for (let i = 0; i < 48; i++) {
      const br = r0 + (r1 - r0) * (i + 0.5) / 48;
      sum += Math.exp(-Math.pow(br / (outerR * BULGE_RE), BULGE_POW)) * BULGE_GAIN;
    }
    return sum;
  };
  const cutTail = (r0, r1) => {
    let sum = 0;
    for (let i = 0; i < 48; i++) {
      const br = r0 + (r1 - r0) * (i + 0.5) / 48;
      sum += bulgeAt(br, 0, 0);
    }
    return sum;
  };
  ok('NEGATIVE CONTROL: the untruncated bulge lights the whole outer disc',
    rawTail(20000, 68000) > 1.0, 'untruncated ' + rawTail(20000, 68000).toFixed(3));
  ok('the truncation removes that leak', cutTail(20000, 68000) < 0.05,
    'truncated ' + cutTail(20000, 68000).toFixed(4));
  ok('the truncation zeroes the far field entirely', bulgeAt(200000, 0, 0) === 0);
}

// ==================== SAMPLES LAND INSIDE THE DISC ====================
// The regression: 48 steps across a 130,000-unit sphere is a 2,700-unit
// step while the disc is ~1,500 thick, so a face-on ray took under ONE
// sample in the material and stepped over every dust lane and arm ridge.
{
  ok('the march is clipped to a slab around the disc',
    /float SLAB = outerR \* SLAB_H;/.test(frag));
  ok('the slab is thin relative to the galaxy', SLAB_H < 0.2, 'SLAB_H ' + SLAB_H);
  ok('a ray that misses the galaxy costs nothing',
    /if \(span\.y < span\.x\) \{ gl_FragColor = vec4\(0\.0\); return; \}/.test(frag));

  // How many samples actually land in gas, face-on, with and without the
  // slab clip? This is the defect measured directly.
  const countInside = (slab) => {
    const cam = [6000, -70000, 4000], dir = [0, 1, 0];
    const R = outerR * 1.30;
    const sp = galaxySpan(cam, dir, R);
    let t0 = Math.max(sp[0], 0), t1 = sp[1];
    if (slab) {
      const S = outerR * SLAB_H;
      const a = (-S - cam[1]) / dir[1], b = (S - cam[1]) / dir[1];
      t0 = Math.max(t0, Math.min(a, b)); t1 = Math.min(t1, Math.max(a, b));
    }
    const dt = (t1 - t0) / STEPS;
    let n = 0;
    for (let i = 0; i < STEPS; i++) {
      const s = t0 + (i + 0.5) * dt;
      if (gasAt(cam[0], cam[1] + s, cam[2]) > 0.01) n++;
    }
    return n;
  };
  const withSlab = countInside(true), without = countInside(false);
  ok('slab clipping puts most samples inside the disc', withSlab >= 20,
    withSlab + '/' + STEPS + ' samples in gas');
  ok('NEGATIVE CONTROL: without it a face-on ray barely samples the disc',
    without <= 4, without + '/' + STEPS + ' samples in gas unclipped');
  ok('the clip is a large improvement, not a rounding difference',
    withSlab > without * 4, withSlab + ' vs ' + without);
}

// ==================== TWO STELLAR POPULATIONS ====================
// A single radius-keyed ramp cannot make a galaxy look real: the gold and
// the blue in the reference are not two ends of one gradient, they are two
// populations with different spatial distributions.
{
  ok('an old population is modelled', /float oldAmt = bulgeAt\(p\) \+ oldDisc;/.test(frag));
  ok('a young population is modelled', /float youngAmt = disc \* thin \* ridge/.test(frag));
  ok('the young population is confined to a thin layer',
    YOUNG_H < DISC_HEIGHT, 'young ' + YOUNG_H + ' vs disc ' + DISC_HEIGHT);
  ok('the young population peaks in a star-forming ring', YOUNG_RING > 1);
  ok('HII knots are modelled', /float hii = smoothstep\(HII_THRESH/.test(frag));

  // The core must be gold and the outer arms blue - the defining colour
  // signature of the reference photograph.
  const core = march(EYE, toward([0, 0, 0]));
  ok('the core is creamy gold: red >= green >= blue',
    core.col[0] >= core.col[1] && core.col[1] >= core.col[2],
    core.col.map((v) => v.toFixed(3)).join(' '));
  ok('the core is warm, not white', core.col[0] - core.col[2] > 0.15,
    'r-b ' + (core.col[0] - core.col[2]).toFixed(3));

  // Sample the arm ring where young stars dominate, taking the bluest
  // point around the ring: an arbitrary sightline may cross a gap.
  let bluest = null;
  for (let a = 0; a < 360; a += 10) {
    const rad = a * Math.PI / 180;
    const p = [Math.cos(rad) * 21000, 40, Math.sin(rad) * 21000];
    const m = march([p[0], -70000, p[2]], [0, 1, 0]);
    if (!bluest || (m.col[2] - m.col[0]) > (bluest.col[2] - bluest.col[0])) bluest = m;
  }
  ok('the arms carry a blue-white population',
    bluest.col[2] > bluest.col[0],
    bluest.col.map((v) => v.toFixed(3)).join(' '));
  ok('the core is brighter than the arms',
    lum(core.col) > lum(bluest.col) * 1.3,
    lum(core.col).toFixed(3) + ' vs ' + lum(bluest.col).toFixed(3));

  // The old population must FOLLOW the arms. With a smooth old disc the
  // measured ring contrast collapsed from 11:1 to 2.6:1.
  ok('the old population follows the arms', OLD_ARM > 0.5, 'OLD_ARM ' + OLD_ARM);
  const ringRatio = (r, oldArm) => {
    let mx = 0, mn = 1e9;
    for (let i = 0; i < 90; i++) {
      const a = i / 90 * Math.PI * 2;
      const px = Math.cos(a) * r, pz = Math.sin(a) * r;
      const disc = discProfile(px, 0, pz, r);
      const armM = armMask(px, 0, pz, r);
      const e = lum([1, 0.83, 0.53]) * disc * 0.9 * mix(1, armM, oldArm);
      if (e > mx) mx = e;
      if (e < mn) mn = e;
    }
    return mx / Math.max(mn, 1e-9);
  };
  ok('the arms stand out from the gaps in the old population',
    ringRatio(20000, OLD_ARM) > 6, ringRatio(20000, OLD_ARM).toFixed(1) + ':1');
  ok('NEGATIVE CONTROL: a smooth old disc washes the arms out',
    ringRatio(20000, 0) < 1.5, ringRatio(20000, 0).toFixed(2) + ':1');
}

// ==================== DUST LANES THAT ACTUALLY BLOCK LIGHT ====================
// The regression: dust removed 0.5% of the light passing through it, so
// the lanes were invisible and the disc read as a uniform haze.
{
  ok('extinction is chromatic', /trans \*= exp\(-sigma \* EXT_RGB\)/.test(frag));
  ok('dust blocks blue harder than red', EXT_RGB[2] > EXT_RGB[0],
    EXT_RGB.join('/'));
  ok('dust also blanks the gas it runs through',
    /float blocked = 1\.0 - DUST_CUT \* d;/.test(frag));
  ok('dust sits off the arm ridge, where a density wave compresses it',
    DUST_PHASE > 0.2 && /armPhase\(p, r\) \+ DUST_PHASE/.test(frag));
  ok('dust hugs the mid-plane more tightly than the gas',
    DUST_HEIGHT < DISC_HEIGHT, DUST_HEIGHT + ' vs ' + DISC_HEIGHT);

  // Optical depth through a lane must be substantial.
  const tauThrough = (px, pz) => {
    const S = outerR * SLAB_H, N = 96, dt = 2 * S / N;
    let tau = 0;
    for (let i = 0; i < N; i++) {
      const y = -S + (i + 0.5) * dt;
      tau += (gasAt(px, y, pz) * K_GAS + dustAt(px, y, pz) * K_DUST) * dt * SIGMA;
    }
    return tau;
  };
  let worstTau = 0;
  for (let a = 0; a < 360; a += 6) {
    const rad = a * Math.PI / 180;
    worstTau = Math.max(worstTau, tauThrough(Math.cos(rad) * 18000, Math.sin(rad) * 18000));
  }
  ok('a dust lane is optically thick enough to be seen', worstTau > 0.25,
    'peak vertical tau ' + worstTau.toFixed(3));
  // NEGATIVE CONTROL: the old shader's dust coefficient. It used
  // (dust * 1.9) with an extinction scale of 5e-5, against this shader's
  // K_DUST 26 with SIGMA 6e-5 - so the old optical depth is the measured
  // one scaled by (1.9 * 5e-5) / (K_DUST * SIGMA).
  const oldScale = (1.9 * 5e-5) / (K_DUST * SIGMA);
  // Expressed as transmittance, which is what the eye actually judges: a
  // lane is only visible if it removes a large fraction of the light
  // behind it.
  const oldTrans = Math.exp(-worstTau * oldScale);
  const newTrans = Math.exp(-worstTau);
  ok('NEGATIVE CONTROL: at the old cross-section a lane barely dimmed anything',
    oldTrans > 0.75, 'old lane still passes ' + (100 * oldTrans).toFixed(0) + '% of the light');
  ok('a lane now genuinely blocks the light behind it',
    newTrans < 0.15, 'lane passes ' + (100 * newTrans).toFixed(1) + '% of the light');

  // Lanes must be localised filaments, not a blanket over the disc.
  let n = 0, cov = 0, dsum = 0, dmax = 0;
  for (let i = 0; i < 4000; i++) {
    const a = (i * 2.39996) % (Math.PI * 2);
    const r = 6000 + ((i * 7919) % 34000);
    const y = ((i * 104729) % 1600) - 800;
    const d = dustAt(Math.cos(a) * r, y, Math.sin(a) * r);
    n++; dsum += d; if (d > 0.25) cov++; if (d > dmax) dmax = d;
  }
  ok('dust forms localised lanes rather than a blanket', dsum / n < 0.32,
    'mean ' + (dsum / n).toFixed(3));
  ok('the lanes are deep where they do fall', dmax > 0.6, 'max ' + dmax.toFixed(3));
  ok('the lanes cover a real fraction of the disc',
    cov / n > 0.10 && cov / n < 0.55, (100 * cov / n).toFixed(1) + '%');
}

// ==================== NOT A GREY WASH FROM INSIDE ====================
// The regression, and the one the user reported: from inside the disc the
// linear contrast was already 45:1, but gamma 1/2.2 lifts a linear 0.016
// to sRGB 0.16, so faint inter-arm gas washed the whole screen grey.
{
  ok('a filmic toe crushes the faint floor to black',
    /mapped \*= \(lum \* lum\) \/ \(lum \* lum \+ TOE \* TOE\);/.test(frag));

  const inside = [26000, 900, 0];
  const core = march(inside, [-1, 0, 0]);
  const zenith = march(inside, [0, 1, 0]);
  const high = march(inside, norm([0.3, 1, 0]));

  ok('the galactic core is bright from inside the disc', lum(core.col) > 0.4,
    'core ' + lum(core.col).toFixed(3));
  ok('the sky away from the disc stays dark', lum(zenith.col) < 0.12,
    'zenith ' + lum(zenith.col).toFixed(3));
  ok('there is a strong band-to-sky contrast from inside',
    lum(core.col) > lum(zenith.col) * 8,
    (lum(core.col) / Math.max(lum(zenith.col), 1e-6)).toFixed(1) + ':1');
  ok('the sky darkens smoothly with elevation, no hard edge',
    lum(high.col) < lum(core.col) && lum(zenith.col) <= lum(high.col) + 0.02);

  // NEGATIVE CONTROL: without the toe the same view IS a grey wash.
  const flatZen = march(inside, [0, 1, 0], 0, 0.5, true, 0);
  ok('NEGATIVE CONTROL: without the toe the sky washes grey',
    lum(flatZen.col) > lum(zenith.col) * 2.5,
    'untoed zenith ' + lum(flatZen.col).toFixed(3) + ' vs ' + lum(zenith.col).toFixed(3));
}

// ==================== THE GALAXY IS VISIBLE FROM OUTSIDE ====================
{
  const ringPeak = (rr, camY) => {
    let best = 0;
    for (let a = 0; a < 360; a += 15) {
      const rad = a * Math.PI / 180;
      const m = march([Math.cos(rad) * rr, camY, Math.sin(rad) * rr], [0, 1, 0]);
      if (m.alpha > best) best = m.alpha;
    }
    return best;
  };
  // Assert on LIGHT, not on coverage. Under premultiplied blending what
  // the eye sees is the emitted colour; alpha only says how much of the
  // background was blocked. The core is deliberately translucent - stars
  // must remain visible through it - so a bright core legitimately has a
  // low alpha, and testing alpha here measured the wrong quantity.
  const ringLight = (rr, camY) => {
    let best = 0;
    for (let a = 0; a < 360; a += 15) {
      const rad = a * Math.PI / 180;
      const m = march([Math.cos(rad) * rr, camY, Math.sin(rad) * rr], [0, 1, 0]);
      best = Math.max(best, lum(m.col));
    }
    return best;
  };
  ok('the galaxy is clearly visible face-on from outside',
    ringLight(0, -70000) > 0.30 && ringLight(18000, -90000) > 0.10,
    'core ' + ringLight(0, -70000).toFixed(3) + ' arm ' + ringLight(18000, -90000).toFixed(3));
  ok('the galaxy does not fade out as you back away',
    Math.abs(ringPeak(18000, -90000) - ringPeak(18000, -260000)) < 0.08);

  const core = march(EYE, toward([0, 0, 0]));
  ok('the fog is translucent, never a solid sheet', core.alpha < 0.98,
    'core alpha ' + core.alpha.toFixed(3));
  // The peak channel may reach 1.0 - that is the normalise, not a bleach.
  // A bleach is all three channels together, which would mean the hue was
  // lost; the saturation assertion below is what actually pins that.
  ok('no sample bleaches to flat white',
    Math.min(...core.col) < 0.92, core.col.map((v) => v.toFixed(3)).join(' '));
  ok('the core keeps a hue rather than going white', sat(core.col) > 0.15,
    'saturation ' + sat(core.col).toFixed(3));
}

// ==================== SMOOTH, CONTINUOUS MEDIUM ====================
{
  let worst = 0;
  for (let i = 0; i < 240; i++) {
    const x = -30000 + i * 60;
    worst = Math.max(worst, Math.abs(gasAt(x, 120, 4000) - gasAt(x + 60, 120, 4000)));
  }
  ok('the density field is continuous, not grainy', worst < 0.12,
    'largest neighbouring jump ' + worst.toFixed(4));
  ok('the rim fades out instead of cutting off',
    /1\.0 - smoothstep\(outerR \* 0\.88, outerR \* 1\.25, r\)/.test(frag));
  ok('the disc height is a fixed fraction of the galaxy, not of local radius',
    /outerR \* DISC_HEIGHT \+ r \* 0\.010/.test(frag));
  ok('the arms are perturbed so they are ragged, not machined',
    ARM_NOISE > 0.5 && /float jitter = \(fbm/.test(frag));
  ok('the arms are broken out of lock-step so they are not rings',
    /m \*= 0\.55 \+ 0\.95 \* brk;/.test(frag));

  // Flatness: the disc must be a plane, not a cone or a vertical cloud.
  let maxY = 0;
  for (let i = 0; i < 900; i++) {
    const a = (i * 2.39996) % (Math.PI * 2);
    const r = 2000 + ((i * 7919) % 44000);
    for (let y = 0; y < 20000; y += 400) {
      if (gasAt(Math.cos(a) * r, y, Math.sin(a) * r) > 0.02) maxY = Math.max(maxY, y);
    }
  }
  ok('the galaxy is a flat plane, not a vertical cloud', maxY / outerR < 0.15,
    'reaches y=' + maxY);
}

// ==================== THE NUCLEUS BLAZES ====================
{
  ok('a distinct nucleus term exists', /float nucleusAt\(vec3 p\)/.test(frag));
  ok('the nucleus is far tighter than the bulge', NUCLEUS_RE < BULGE_RE / 3,
    NUCLEUS_RE + ' vs ' + BULGE_RE);
  ok('the nucleus outshines the bulge at the centre',
    nucleusAt(0, 0, 0) > bulgeAt(0, 0, 0),
    nucleusAt(0, 0, 0).toFixed(2) + ' vs ' + bulgeAt(0, 0, 0).toFixed(2));

  const at = (rr) => march([rr, -70000, 0], [0, 1, 0]);
  const core = at(0), mid = at(20000), outer = at(34000);
  ok('the core is the brightest part of the galaxy',
    lum(core.col) > lum(mid.col) && lum(core.col) > lum(outer.col),
    'core ' + lum(core.col).toFixed(3) + ' mid ' + lum(mid.col).toFixed(3));
  ok('the core is creamy gold, not blue',
    core.col[0] >= core.col[1] && core.col[1] >= core.col[2],
    core.col.map((v) => v.toFixed(2)).join(','));
  ok('brightness falls off toward the rim', lum(outer.col) > lum(at(46000).col));

  // Smooth falloff: no visible ring or shelf around the blaze. Measured
  // only inside the bulge - past ~8,000 units the spiral arms begin and
  // their contrast reads as a false ring artefact.
  let worstStep = 0;
  for (let r = 0; r < 7000; r += 100) {
    const a = lum(emissionAt(r, 0, 0));
    const b = lum(emissionAt(r + 100, 0, 0));
    worstStep = Math.max(worstStep, Math.abs(a - b) / Math.max(a, 1e-6));
  }
  ok('the blaze falls off smoothly rather than ending on a ring',
    worstStep < 0.35, 'largest relative step ' + worstStep.toFixed(3));
}

// ==================== CLASS-C ANOMALY: AN OVERLAY ====================
{
  ok('the anomaly is an overlay on a normal galaxy, not a replacement',
    /col \+= neon \* strand \* ANOMALY_GAIN;/.test(frag));
  ok('the strands are placed in the inter-arm gaps',
    /float gaps = pow\(1\.0 - wave, 2\.0\);/.test(frag));
  ok('the photoreal layout is the base for every class',
    !/anomaly > 0\.5 \? .*C_HA/.test(frag));

  // The anomaly must be visibly different across many sightlines.
  let diff = 0, total = 0, magenta = 0, teal = 0;
  for (let ri = 0; ri < 9; ri++) {
    for (let ai = 0; ai < 6; ai++) {
      const r = 8000 + ri * 4000, a = ai / 6 * Math.PI * 2;
      const cam = [Math.cos(a) * r, -70000, Math.sin(a) * r];
      const p = march(cam, [0, 1, 0], 0);
      const q = march(cam, [0, 1, 0], 1);
      total++;
      const d = Math.hypot(p.col[0] - q.col[0], p.col[1] - q.col[1], p.col[2] - q.col[2]);
      if (d > 0.02) diff++;
      if (q.col[0] > q.col[1] * 1.15 && q.col[2] > q.col[1] * 1.05) magenta++;
      if (q.col[1] > q.col[0] * 1.1 && q.col[2] > q.col[0] * 1.05) teal++;
    }
  }
  ok('an anomaly galaxy looks clearly different from a photoreal one',
    diff / total > 0.35, (100 * diff / total).toFixed(0) + '% of sightlines differ');
  ok('both H-alpha magenta and O-III teal appear', magenta > 0 && teal > 0,
    magenta + ' magenta, ' + teal + ' teal');

  // The strands belong BETWEEN the arms, not over them.
  let inGap = 0, onArm = 0;
  for (let i = 0; i < 3000; i++) {
    const a = (i * 2.39996) % (Math.PI * 2);
    const r = 8000 + ((i * 7919) % 28000);
    const px = Math.cos(a) * r, pz = Math.sin(a) * r;
    const s = anomalyStrand(px, 0, pz);
    if (s < 0.01) continue;
    if (armMask(px, 0, pz, r) > 0.5) onArm += s; else inGap += s;
  }
  ok('the strands thread between the arms rather than over them',
    inGap > onArm * 2, inGap.toFixed(1) + ' in gaps vs ' + onArm.toFixed(1) + ' on arms');

  // A photoreal galaxy and an anomaly must share the same gold nucleus.
  const n0 = march([0, -70000, 0], [0, 1, 0], 0);
  const n1 = march([0, -70000, 0], [0, 1, 0], 1);
  ok('an anomaly keeps its gold nucleus',
    Math.hypot(n0.col[0] - n1.col[0], n0.col[1] - n1.col[1], n0.col[2] - n1.col[2]) < 0.02);
}

// ==================== THE ANOMALY IS REACHABLE ====================
{
  const field = fs.readFileSync('src/bjs/systems/GalaxyField.ts', 'utf8');
  ok('the fog adopts the galaxy the player is actually inside',
    /const host = nearestGalaxy\(eye\.x, eye\.y, eye\.z\)/.test(field));
  ok('its class drives the anomaly uniform',
    /setFloat\('anomaly', klass === 'anomaly' \? 1 : 0\)/.test(field));
  ok('the uniform is only rewritten when it changes',
    /if \(klass !== this\.fogClass\)/.test(field));
  ok('intergalactic space falls back to the home look',
    /inHost \? host\.galaxy\.klass : HOME_CLASS/.test(field));
  ok('the innermost stars merge into the volumetric nucleus',
    /NUCLEUS_MERGE_INNER/.test(field) && /NUCLEUS_MERGE_OUTER/.test(field));
  ok('the merge is a ramp, so there is no hard edge',
    /smoothstep01\(NUCLEUS_MERGE_INNER, NUCLEUS_MERGE_OUTER, rc\)/.test(field));
}

// ==================== STAR OCCLUSION AND BLACK VACUUM ====================
{
  const field = fs.readFileSync('src/bjs/systems/GalaxyField.ts', 'utf8');
  ok('star brightness is driven by the local fog density',
    /gasDensity'?, vis\)/.test(field) || /setFloat\('gasDensity', vis\)/.test(field));
  ok('the occlusion ramp is smooth, not a hard switch',
    /smoothstep01\(0\.16, 0\.62, dens\)/.test(field));
  ok('the fog density used for occlusion is the real field',
    /const dens = fogAt\(eye\.x, eye\.y, eye\.z\)/.test(field));

  const sky = fs.readFileSync('src/bjs/shaders/CosmicSkyShader.ts', 'utf8');
  ok('ordinary space gets no tint lift at all',
    /if \(medium >= 0\.5\) col \+= tint \* 0\.30;/.test(sky));
  for (const w of ['src/bjs/worlds/PlanetaryWorld.ts', 'src/bjs/worlds/SandboxWorld.ts']) {
    const t = fs.readFileSync(w, 'utf8');
    ok(w.split('/').pop() + ' clears to pure black',
      /clearColor = new Color4\(0, 0, 0, 1\)/.test(t));
  }
}

// ==================== CORES ON THE GALACTIC PLANE ====================
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

// ==================== APPROACH GLARE CLAMP ====================
{
  const app = fs.readFileSync('src/bjs/App.ts', 'utf8');
  ok('bloom is pulled down as a horizon closes',
    /bloomBeforeHorizon \* \(1 - 0\.82 \* proximity\)/.test(app));
  ok('the pre-clamp bloom is captured once, not re-read each frame',
    /if \(!this\.bloomClamped\) \{\s*\n\s*this\.bloomBeforeHorizon = this\.postfx\.settings\.bloom;/.test(app));
  ok('bloom is restored on leaving', /this\.postfx\.set\('bloom', this\.bloomBeforeHorizon\);/.test(app));
  ok('the clamp is driven by the same depth as the transition',
    /const depth = this\.universe\.horizonDepth;/.test(app));

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

console.log(`\n${passed} passed, ${failed} failed`);
process.exit(failed ? 1 : 0);
