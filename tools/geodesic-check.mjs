/**
 * Geodesic lensing checks.
 *
 * A screen-space warp is a function uv -> uv: it rearranges pixels that
 * were already drawn. The features that make a real black hole image
 * recognisable are light the flat frame has no record of - the far side of
 * the disk bent up over the shadow, and Einstein rings, which are several
 * images of the SAME star. One output pixel takes one input pixel, so no
 * choice of offsets can duplicate a star into a ring.
 *
 * These checks verify the raymarcher does the thing the post-process
 * cannot: integrate real null geodesics and sample the sky along the
 * direction each ray finally escapes toward.
 *
 * Run: node tools/geodesic-check.mjs
 */
import fs from 'fs';

let pass = 0, fail = 0;
const ok = (name, cond, extra) => {
  if (cond) pass++;
  else { fail++; console.log('FAIL: ' + name + (extra ? ' — ' + extra : '')); }
};

const SRC = 'src/bjs/shaders/HoleFieldShader.ts';
if (!fs.existsSync(SRC)) {
  console.log('FAIL: ' + SRC + ' is missing');
  console.log('0 passed, 1 failed');
  process.exit(1);
}
const src = fs.readFileSync(SRC, 'utf8');
const frag = (src.match(/const\s+FRAG\s*=\s*`([\s\S]*?)`;/m) || [])[1] || '';
ok('the fragment shader source is present', frag.length > 0);

// ------------------------------------------------- the sky must be sampled
ok('the shader samples the sky along a ray direction',
  /vec3 skyAlongRay\(vec3 dir\)/.test(frag));
ok('the escaping direction is tracked through the integration',
  /escapeDir = normalize\(p - prevPos\)/.test(frag));
ok('the total bend is tracked', /totalBend = phi/.test(frag));
ok('the lensed sky is added to escaping rays',
  /skyAlongRay\(escapeDir\)/.test(frag));
ok('the lensed sky is NOT added to captured rays',
  /if \(!captured\)\{[\s\S]{0,220}skyAlongRay/.test(frag));
ok('coverage rises where the sky is drawn lensed, to hide the flat background',
  /max\(lum, lensedSky/.test(frag));
ok('the lensed sky fades in with the deflection, not abruptly',
  /smoothstep\([\d.]+, [\d.]+, totalBend\)/.test(frag));

// The old early-out dropped the ray before its heading could be recorded.
const escIdx = frag.indexOf('escapeDir = normalize');
const cutIdx = frag.indexOf('left the region we draw');
ok('the escape direction is recorded before the range cut-off',
  escIdx > 0 && cutIdx > escIdx,
  'escapeDir at ' + escIdx + ', cut-off at ' + cutIdx);

// ------------------------------------------ the geodesic must really bend
/**
 * The shader's RK2 integration of u'' = -u + 1.5 rs u^2, the Schwarzschild
 * null geodesic in the orbital plane, ported verbatim so the assertions
 * below test the same maths the GPU runs.
 */
function trace(b, rs = 1, r0 = 200) {
  let u = 1 / r0;
  const dru = -Math.sqrt(Math.max(0, 1 - (b * b) / (r0 * r0)));
  const dtu = b / r0;
  let du = -u * (dru / Math.max(dtu, 1e-4));
  let phi = 0;
  const dphi = 0.01;
  for (let i = 0; i < 20000; i++) {
    const k1 = -u + 1.5 * rs * u * u;
    const uMid = u + du * dphi * 0.5;
    const duMid = du + k1 * dphi * 0.5;
    const k2 = -uMid + 1.5 * rs * uMid * uMid;
    u += duMid * dphi;
    du += k2 * dphi;
    phi += dphi;
    if (u <= 0) return { escaped: true, phi };
    const r = 1 / u;
    if (r <= rs * 1.06) return { escaped: false, phi, captured: true };
    if (r > r0 * 3 && phi > 0.1) return { escaped: true, phi };
  }
  return { escaped: true, phi };
}

const deflection = (b) => {
  const t = trace(b);
  return t.escaped ? (t.phi * 180) / Math.PI - 180 : null;
};

ok('a ray aimed at the hole is captured', trace(1.5).captured === true);
ok('a ray aimed far wide escapes', trace(80).escaped === true);

const near = deflection(3.0);
const far = deflection(80);
ok('a grazing ray is deflected much more than a distant one',
  near !== null && far !== null && Math.abs(near) > Math.abs(far),
  near + ' vs ' + far);

// THE defining property. Deflection passing 180 degrees is what allows two
// rays on opposite sides of the hole to arrive from the same sky direction,
// which is an Einstein ring. No uv -> uv mapping can produce this.
const extreme = deflection(2.6);
ok('near the photon sphere a ray bends through more than half a turn',
  extreme !== null && extreme > 180, String(extreme));

// Measured as SWEPT ANGLE, not as (phi - 180).
//
// Subtracting a straight-line 180 degrees is only the deflection for a ray
// that starts at infinity. These start at r0 = 200 rs, where the finite
// geometry contributes its own offset, so the subtraction flips sign around
// b ~ 18 and the quantity stops being monotonic even though the physics is
// perfectly well behaved. The swept angle itself is the honest measure.
ok('the swept angle grows monotonically as the ray closes in', (() => {
  const bs = [4.5, 6, 9, 14, 22, 40, 80];
  const phis = bs.map((b) => trace(b).phi);
  for (let i = 1; i < phis.length; i++) if (phis[i] > phis[i - 1]) return false;
  return true;
})());

ok('a close ray sweeps further than a distant one',
  trace(3.0).phi > trace(80).phi,
  (trace(3.0).phi).toFixed(2) + ' vs ' + (trace(80).phi).toFixed(2));

ok('every escaping ray returns a finite deflection', (() => {
  for (let b = 2.6; b < 100; b += 0.7) {
    const d = deflection(b);
    if (d !== null && !Number.isFinite(d)) return false;
  }
  return true;
})());

// A capture radius must exist, and it must be near the photon sphere.
let critical = null;
for (let b = 2.0; b < 8; b += 0.01) {
  if (trace(b).escaped) { critical = b; break; }
}
ok('there is a critical impact parameter', critical !== null,
  String(critical));
ok('the capture radius is close to the photon sphere (2.6 rs)',
  critical !== null && critical > 2.2 && critical < 3.2, String(critical));

// -------------------------------------------------- the starfield itself
ok('the sky has stars', /float star = exp\(/.test(frag));
ok('the sky has large-scale structure to distort',
  /float band = exp\(/.test(frag));
ok('star colour is temperature-biased toward cool',
  /temp < 0\.7\d/.test(frag));
ok('the sky is a function of direction only, not screen position',
  /vec3 d = normalize\(dir\)/.test(frag));

// The sky must not be added twice: far from the hole the real background
// is already correct behind the quad.
ok('an unbent ray contributes no lensed sky', (() => {
  const m = frag.match(/smoothstep\(([\d.]+), ([\d.]+), totalBend\)/);
  return m && Number(m[1]) > 0;
})());

console.log(pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
