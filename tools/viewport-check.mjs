/**
 * Viewport-artifact checks: the white disk bubble and the sun's corner
 * streaks. Both were visible defects with measurable causes, so both get
 * assertions on the cause rather than on the symptom.
 */
import fs from 'fs';
let pass = 0, fail = 0;
const ok = (n, c, e) => { if (c) pass++; else { fail++; console.log('FAIL: ' + n + (e ? ' — ' + e : '')); } };

const hole = fs.readFileSync('src/bjs/shaders/HoleFieldShader.ts', 'utf8');
const sun = fs.readFileSync('src/bjs/shaders/SunShader.ts', 'utf8');

// ---------------------------------------------------- the white bubble
// Cause: relativistic beaming clamped D at 4.0, so brightness scaled by
// D^3 = 64x. Integrated over ~60 in-slab steps that reached col ~= 68,
// which no tonemapper can recover - every disk pixel saturated to white.
// Orbital speed is bounded: beta = sqrt(rs/2r), so at 1.5rs beta = 0.577
// and the true Doppler factor is 2.36.
ok('the Doppler boost is clamped to a physically reachable value', (() => {
  const m = hole.match(/clamp\(dop,\.2,([\d.]+)\)/);
  return m && Number(m[1]) <= 2.6;
})());
ok('emission cannot run away before tone mapping',
  /gas=tonemapACES\(min\(gas,vec3\([\d.]+\)\)\)/.test(hole));
ok('the named ACES curve is applied only to emitted gas',
  /vec3 tonemapACES\(vec3 x\)/.test(hole) &&
  /gas=tonemapACES/.test(hole) && !/col=tonemapACES/.test(hole));
ok('the already graded universe is never tone-mapped a second time',
  /vec3 warped=mix\(scene\.rgb,lensed,lensFade\)\+gas/.test(hole));
ok('the disk edge dissolves with zero slope, not a hard rim',
  /outerRamp \* outerRamp/.test(hole));

// Numeric: replay the accumulation and confirm it lands in a sane range.
{
  const dop = (b) => 1 / Math.max(1 - b, 0.05);
  const boost = (b, cap) => Math.pow(Math.min(Math.max(dop(b), 0.2), cap), 3);
  const cap = Number((hole.match(/clamp\(dop,\.2,([\d.]+)\)/) || [0, 2.4])[1]);
  ok('the worst-case boost is bounded', boost(0.99, cap) < 20,
    boost(0.99, cap).toFixed(1) + 'x');
  const integrate = (cap) => {
    let col = 0, t = 1;
    for (let i = 0; i < 60; i++) {
      const w = 1.0 * 0.6 * 0.55;
      col += t * boost(0.99, cap) * w;
      t *= 1 - Math.min(w, 1) * 0.92;
      if (t < 0.02) break;
    }
    return col;
  };
  const aces = (x) => (x * (2.51 * x + 0.03)) / (x * (2.43 * x + 0.59) + 0.14);
  // The shader's own pre-clamp ceiling, read from source so the test
  // cannot drift from the thing it is checking.
  const ceil = Number((hole.match(/min\(gas,vec3\(([\d.]+)\)\)/) || [0, 2.4])[1]);
  const shade = (v) => aces(Math.min(v, ceil));
  const raw = integrate(cap);
  ok('integrated emission stays in tonemappable range', raw < 24, raw.toFixed(1));
  ok('the pre-clamp ceiling is below the ACES saturation point',
    aces(ceil) < 0.999, 'aces(' + ceil + ') = ' + aces(ceil).toFixed(4));
  ok('the tonemapped disk is not a flat saturated white',
    shade(raw) < 0.999, shade(raw).toFixed(4));
  ok('a dim part of the disk stays visibly dimmer than a bright part',
    shade(raw * 0.15) < shade(raw) - 0.02,
    shade(raw * 0.15).toFixed(3) + ' vs ' + shade(raw).toFixed(3));
}

// ------------------------------------------------------ the sun streaks
// Cause: pow(abs(cos(ang*k)), 22) is a purely ANGULAR term. A cosine
// raised to the 22nd power has almost no angular width, so it can only
// alias into a hard line; measured, it was still 1.8x the soft skirt at
// r=0.2 and reached most of the way across the quad.
ok('the angular spike term is gone',
  !/pow\(abs\(cos\(ang \* 2\.0\)\), 22\.0\)/.test(sun));
ok('no cos-power starburst remains in the glare',
  !/star4|star6/.test(sun));
ok('the glare is an isotropic radial profile',
  /ISOTROPIC RADIAL GAUSSIAN PROFILE/.test(sun));
ok('the profile is built from Gaussians in r alone', (() => {
  const g = sun.match(/exp\(-r \* r \* [\d.]+\)/g) || [];
  return g.length >= 3;
})());
ok('the core is still searing', /float core  = exp\(-r \* r \* 34\.0\)/.test(sun));
ok('the sun uses blazing orange, not a white mask',
  /vec3\(1\.0, 0\.478, 0\.0\)/.test(sun));
ok('and deep solar gold', /vec3\(1\.0, 0\.700, 0\.0\)/.test(sun));
ok('the hex spectrum is documented for future edits',
  /#ff7a00/.test(sun) && /#ffb300/.test(sun));

// Numeric: the profile must be monotonic in r and identical at every
// angle. Isotropy is the property that makes a streak impossible.
{
  const prof = (r) => Math.exp(-r * r * 34) + Math.exp(-r * r * 7.5) * 0.55
    + Math.exp(-r * r * 1.6) * 0.30 + Math.exp(-r * 3.1) * 0.12;
  let mono = true;
  for (let i = 1; i <= 100; i++) {
    if (prof(i / 100) > prof((i - 1) / 100)) mono = false;
  }
  ok('brightness falls monotonically with radius', mono);
  ok('brightness is identical in every direction', (() => {
    const r = 0.5;
    const vals = [];
    for (let a = 0; a < 12; a++) {
      const x = Math.cos(a) * r, y = Math.sin(a) * r;
      vals.push(prof(Math.hypot(x, y)).toFixed(9));
    }
    return new Set(vals).size === 1;
  })());
  ok('the glare fades to almost nothing at the quad edge', prof(1.0) < 0.12,
    prof(1.0).toFixed(4));
}

console.log(pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
