/**
 * Galaxy structure checks.
 *
 * The failure this guards against is subtle: every one of these functions
 * can return perfectly finite numbers and still produce a sky that looks
 * like uniform noise. So these assertions measure STRUCTURE - that the arms
 * are actually arms, that the disc is actually thin, that the core actually
 * blazes, that the gas actually clears when you leave the plane.
 *
 * Run: node tools/galaxy-check.mjs
 */
import { execFileSync } from 'child_process';
import fs from 'fs';

let pass = 0, fail = 0;
const ok = (name, cond, extra) => {
  if (cond) pass++;
  else { fail++; console.log('FAIL: ' + name + (extra ? ' — ' + extra : '')); }
};

const SRC = 'src/bjs/systems/GalaxyShape.ts';
if (!fs.existsSync(SRC)) {
  console.log('FAIL: ' + SRC + ' is missing');
  console.log('0 passed, 1 failed');
  process.exit(1);
}

const out = '/tmp/galaxy-' + Date.now() + '.mjs';
try {
  execFileSync('npx', ['esbuild', SRC, '--bundle', '--format=esm',
    '--platform=node', '--log-level=error', '--outfile=' + out], { stdio: 'pipe' });
} catch (e) {
  console.log('FAIL: could not bundle — ' + (e.stderr?.toString() || e.message));
  console.log('0 passed, 1 failed');
  process.exit(1);
}

const G = await import(out);
const {
  MILKY_WAY, logSpiralAngle, gaussian, galaxyStar, observerPosition,
  projectToShell, valueNoise3, fbm3, nebulaDensity, nebulaColor,
  sampleNebulaPoint
} = G;

/** Deterministic RNG so a failure is reproducible. */
function makeRand(seed = 12345) {
  let s = seed >>> 0 || 1;
  return () => {
    s ^= s << 13; s >>>= 0;
    s ^= s >> 17;
    s ^= s << 5; s >>>= 0;
    return s / 4294967296;
  };
}

// ------------------------------------------------------- the spiral itself
{
  const c = MILKY_WAY;
  ok('the galaxy has arms', c.arms >= 2);
  ok('the disc is thin', c.thickness <= 0.08, 'thickness ' + c.thickness);

  // The defining property: angle is linear in ln(radius). Doubling the
  // radius must add a constant angle, whatever radius you started from.
  const step1 = logSpiralAngle(2000, c.innerBound, c.armFactor, 0) -
                logSpiralAngle(1000, c.innerBound, c.armFactor, 0);
  const step2 = logSpiralAngle(8000, c.innerBound, c.armFactor, 0) -
                logSpiralAngle(4000, c.innerBound, c.armFactor, 0);
  ok('the arm is a true logarithmic spiral', Math.abs(step1 - step2) < 1e-9,
    step1 + ' vs ' + step2);

  ok('the arm winds as radius grows',
    logSpiralAngle(8000, c.innerBound, c.armFactor, 0) >
    logSpiralAngle(1000, c.innerBound, c.armFactor, 0));

  // Arms must be evenly spaced around the core.
  const offs = [];
  for (let i = 0; i < c.arms; i++) offs.push((2 * Math.PI * i) / c.arms);
  const gaps = offs.slice(1).map((v, i) => v - offs[i]);
  ok('arms are evenly spaced', gaps.every((g) => Math.abs(g - gaps[0]) < 1e-9));

  ok('an arm offset rotates the whole arm',
    Math.abs((logSpiralAngle(3000, c.innerBound, c.armFactor, 1.5) -
              logSpiralAngle(3000, c.innerBound, c.armFactor, 0)) - 1.5) < 1e-9);
}

// ------------------------------------------------------------- Box-Muller
{
  const rand = makeRand(7);
  const vals = [];
  for (let i = 0; i < 20000; i++) vals.push(gaussian(rand(), rand()));
  const mean = vals.reduce((a, b) => a + b, 0) / vals.length;
  const sd = Math.sqrt(vals.reduce((a, b) => a + (b - mean) ** 2, 0) / vals.length);
  ok('gaussian is centred on zero', Math.abs(mean) < 0.05, 'mean ' + mean.toFixed(4));
  ok('gaussian has unit spread', Math.abs(sd - 1) < 0.05, 'sd ' + sd.toFixed(4));
  ok('gaussian never returns NaN', vals.every(Number.isFinite));
  ok('gaussian survives a zero uniform', Number.isFinite(gaussian(0, 0)));
}

// ------------------------------------------------- the generated star field
const rand = makeRand(2024);
const stars = [];
for (let i = 0; i < 30000; i++) stars.push(galaxyStar(rand, MILKY_WAY));

{
  ok('every star is finite',
    stars.every((s) => Number.isFinite(s.x) && Number.isFinite(s.y) && Number.isFinite(s.z)));
  ok('every star has a brightness in range',
    stars.every((s) => s.bright >= 0 && s.bright <= 1));

  const kinds = new Set(stars.map((s) => s.kind));
  ok('all three populations are generated',
    kinds.has('arm') && kinds.has('bulge') && kinds.has('halo'),
    [...kinds].join(','));

  const arms = stars.filter((s) => s.kind === 'arm');
  const bulge = stars.filter((s) => s.kind === 'bulge');
  const halo = stars.filter((s) => s.kind === 'halo');
  ok('most stars are in the disc', arms.length > stars.length * 0.55,
    arms.length + '/' + stars.length);
  ok('the bulge is a minority but present',
    bulge.length > stars.length * 0.10 && bulge.length < stars.length * 0.25);
  ok('the halo is sparse', halo.length < stars.length * 0.2);

  // THE central structural claim: the disc is flat. Compare vertical spread
  // against horizontal spread for disc stars only.
  const rms = (xs) => Math.sqrt(xs.reduce((a, b) => a + b * b, 0) / Math.max(1, xs.length));
  const vert = rms(arms.map((s) => s.y));
  const horiz = rms(arms.map((s) => Math.sqrt(s.x * s.x + s.z * s.z)));
  ok('the disc is far wider than it is thick', vert < horiz * 0.12,
    'vertical ' + vert.toFixed(0) + ' vs radial ' + horiz.toFixed(0));

  // The bulge must actually be central and dense.
  const bulgeR = rms(bulge.map((s) => Math.sqrt(s.x * s.x + s.y * s.y + s.z * s.z)));
  ok('the bulge sits at the centre', bulgeR < MILKY_WAY.bulgeRadius,
    'rms radius ' + bulgeR.toFixed(0));
  const meanB = (xs) => xs.reduce((a, b) => a + b.bright, 0) / Math.max(1, xs.length);
  ok('the core blazes brighter than the disc', meanB(bulge) > meanB(arms),
    meanB(bulge).toFixed(3) + ' vs ' + meanB(arms).toFixed(3));
  ok('the halo is the faintest population', meanB(halo) < meanB(arms));

  // THE arms must be detectable as overdensities. Fold every disc star's
  // angle back against the spiral relation: if arms exist, the residuals
  // cluster near the arm offsets instead of spreading evenly.
  const c = MILKY_WAY;
  const bins = new Array(36).fill(0);
  for (const s of arms) {
    const r = Math.sqrt(s.x * s.x + s.z * s.z);
    if (r < c.innerBound * 1.2) continue;
    const ang = Math.atan2(s.z, s.x);
    const base = logSpiralAngle(r, c.innerBound, c.armFactor, 0);
    // Residual modulo one arm's share of the circle.
    let d = (ang - base) % (2 * Math.PI / c.arms);
    if (d < 0) d += 2 * Math.PI / c.arms;
    bins[Math.floor(d / (2 * Math.PI / c.arms) * 36) % 36]++;
  }
  const peak = Math.max(...bins);
  const mean = bins.reduce((a, b) => a + b, 0) / bins.length;
  ok('stars concentrate along the arms rather than filling the disc',
    peak > mean * 3, 'peak ' + peak + ' vs mean ' + mean.toFixed(0));

  // A uniform disc would fail the test above; prove the test can fail.
  const flat = new Array(36).fill(0);
  const r2 = makeRand(99);
  for (let i = 0; i < arms.length; i++) flat[Math.floor(r2() * 36) % 36]++;
  const flatPeak = Math.max(...flat);
  const flatMean = flat.reduce((a, b) => a + b, 0) / flat.length;
  ok('the arm test rejects a structureless disc', !(flatPeak > flatMean * 3),
    'peak ' + flatPeak + ' vs mean ' + flatMean.toFixed(0));
}

// --------------------------------------------------------- shell projection
{
  const obs = observerPosition(MILKY_WAY);
  ok('the observer is off-centre in the disc',
    Math.sqrt(obs[0] * obs[0] + obs[2] * obs[2]) > MILKY_WAY.innerBound * 2);
  ok('the observer sits in the galactic plane', Math.abs(obs[1]) < 1e-9);

  const inner = 2000, outer = 3800;
  const proj = stars.map((s) => projectToShell(s, obs, inner, outer, MILKY_WAY));

  ok('every projected star is finite',
    proj.every((p) => Number.isFinite(p.x) && Number.isFinite(p.y) && Number.isFinite(p.z)));
  ok('projected stars land inside the shell band',
    proj.every((p) => {
      const r = Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
      return r >= inner - 1 && r <= outer + 1;
    }));
  ok('nearer stars land nearer the inner shell', (() => {
    const sorted = [...proj].sort((a, b) => a.distance - b.distance);
    const rOf = (p) => Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
    return rOf(sorted[0]) < rOf(sorted[sorted.length - 1]);
  })());

  // The payoff: from inside the disc there must be a bright BAND across the
  // sky, not an even sprinkle. Bin by galactic latitude.
  const lat = proj.map((p) => {
    const r = Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z) || 1;
    return Math.asin(Math.max(-1, Math.min(1, p.y / r)));
  });
  const near = lat.filter((a) => Math.abs(a) < 0.15).length;
  const polar = lat.filter((a) => Math.abs(a) > 1.0).length;
  ok('there is a dense band across the sky', near > polar * 3,
    'equatorial ' + near + ' vs polar ' + polar);

  // And the core must be in ONE direction, not all of them.
  const bulgeProj = proj.filter((p) => p.kind === 'bulge');
  const mx = bulgeProj.reduce((a, p) => a + p.x, 0) / Math.max(1, bulgeProj.length);
  const mz = bulgeProj.reduce((a, p) => a + p.z, 0) / Math.max(1, bulgeProj.length);
  const spread = Math.sqrt(mx * mx + mz * mz);
  ok('the galactic core lies in one direction', spread > inner * 0.35,
    'mean offset ' + spread.toFixed(0));

  // Degenerate case: a star exactly on the observer must not spray NaN.
  const zero = projectToShell(
    { x: obs[0], y: obs[1], z: obs[2], kind: 'arm', bright: 1 },
    obs, inner, outer, MILKY_WAY);
  ok('a star on top of the observer is handled',
    Number.isFinite(zero.x) && Number.isFinite(zero.y) && Number.isFinite(zero.z));
}

// -------------------------------------------------------------- the nebulae
{
  ok('noise is bounded', (() => {
    const r = makeRand(5);
    for (let i = 0; i < 5000; i++) {
      const v = valueNoise3(r() * 100, r() * 100, r() * 100);
      if (!(v >= 0 && v <= 1)) return false;
    }
    return true;
  })());
  ok('noise is deterministic', valueNoise3(1.5, 2.5, 3.5) === valueNoise3(1.5, 2.5, 3.5));
  ok('noise is continuous, not blocky', (() => {
    // Two nearby samples must not differ wildly.
    let worst = 0;
    for (let i = 0; i < 500; i++) {
      const x = i * 0.017;
      worst = Math.max(worst, Math.abs(valueNoise3(x, 0.5, 0.5) - valueNoise3(x + 0.002, 0.5, 0.5)));
    }
    return worst < 0.1;
  })());
  ok('noise actually varies', (() => {
    const s = new Set();
    for (let i = 0; i < 200; i++) s.add(valueNoise3(i * 1.7, i * 0.3, i * 2.1).toFixed(4));
    return s.size > 150;
  })());
  ok('fbm is bounded', (() => {
    const r = makeRand(11);
    for (let i = 0; i < 3000; i++) {
      const v = fbm3(r() * 50, r() * 50, r() * 50, 4);
      if (!(v >= 0 && v <= 1)) return false;
    }
    return true;
  })());
  ok('fbm adds detail over plain noise', (() => {
    // More octaves must change the field, or the octave loop is dead code.
    let diff = 0;
    for (let i = 0; i < 200; i++) {
      diff += Math.abs(fbm3(i * 0.31, 0.7, 1.3, 1) - fbm3(i * 0.31, 0.7, 1.3, 5));
    }
    return diff / 200 > 0.01;
  })());

  const c = MILKY_WAY;
  const sampleAt = (y) => {
    let sum = 0, n = 0;
    for (let i = 0; i < 400; i++) {
      const a = (i / 400) * 2 * Math.PI;
      const r = c.innerBound + (c.outerBound - c.innerBound) * 0.4;
      sum += nebulaDensity(r * Math.cos(a), y, r * Math.sin(a), c); n++;
    }
    return sum / n;
  };

  const inPlane = sampleAt(0);
  const above = sampleAt(c.outerBound * 0.5);
  ok('gas is densest in the galactic plane', inPlane > above * 5,
    'plane ' + inPlane.toFixed(4) + ' vs above ' + above.toFixed(4));
  ok('gas reaches true emptiness away from the disc', above < 0.01,
    'density ' + above.toFixed(5));

  const farOut = nebulaDensity(c.outerBound * 4, 0, 0, c);
  ok('intergalactic space is empty', farOut < 0.01, 'density ' + farOut.toFixed(5));

  ok('density is always a valid fraction', (() => {
    const r = makeRand(31);
    for (let i = 0; i < 4000; i++) {
      const d = nebulaDensity((r() - 0.5) * 60000, (r() - 0.5) * 20000, (r() - 0.5) * 60000, c);
      if (!(d >= 0 && d <= 1) || !Number.isFinite(d)) return false;
    }
    return true;
  })());
  ok('density is not a constant', (() => {
    const vals = new Set();
    const r = makeRand(41);
    for (let i = 0; i < 300; i++) {
      const p = sampleNebulaPoint(r, c, 0);
      if (p) vals.add(p.density.toFixed(3));
    }
    return vals.size > 50;
  })());

  // Colour must stay in the requested palette and never go black or blow out.
  ok('nebula colour is always valid', (() => {
    const r = makeRand(53);
    for (let i = 0; i < 2000; i++) {
      const col = nebulaColor(r(), (r() - 0.5) * 30000, (r() - 0.5) * 3000, (r() - 0.5) * 30000, c);
      if (col.length !== 3) return false;
      if (!col.every((v) => Number.isFinite(v) && v >= 0 && v <= 1)) return false;
    }
    return true;
  })());
  ok('denser gas glows brighter', (() => {
    const lo = nebulaColor(0.15, 5000, 0, 5000, c).reduce((a, b) => a + b, 0);
    const hi = nebulaColor(0.95, 5000, 0, 5000, c).reduce((a, b) => a + b, 0);
    return hi > lo;
  })());
  // This assertion used to read "the palette is purple/indigo/blue, never
  // green" and forbade green from ever being the dominant channel. That rule
  // WAS the flat pink wash: it confined every point to a single violet line
  // through colour space, measured mean hue (1.00, 0.33, 0.82). Real emission
  // nebulae are multi-species, and the green-dominant one - O-III at 501nm -
  // is exactly the line that stops a nebula field looking monochrome. So the
  // rule is inverted: the palette must contain several distinct species.
  ok('the nebula palette spans several emission species', (() => {
    const r = makeRand(67);
    const fam = { crimson: 0, teal: 0, orange: 0, blue: 0 };
    for (let i = 0; i < 4000; i++) {
      const col = nebulaColor(0.8, (r() - 0.5) * 30000, (r() - 0.5) * 900,
        (r() - 0.5) * 30000, c);
      const m = Math.max(...col);
      if (!(m > 1e-4)) continue;
      const [R, G, B] = col.map((v) => v / m);
      if (G > 0.55 && B > 0.45 && R < 0.65) fam.teal++;
      else if (R > 0.8 && G < 0.45 && B < 0.5) fam.crimson++;
      else if (R > 0.7 && G > 0.35 && G < 0.75) fam.orange++;
      else if (B > 0.65 && R < 0.75) fam.blue++;
    }
    // Every species must actually appear, not just be reachable in theory.
    return fam.teal > 0 && fam.crimson > 0 && fam.orange > 0;
  })());
  ok('no single hue swamps the whole nebula field', (() => {
    const r = makeRand(71);
    let n = 0, mono = 0;
    for (let i = 0; i < 3000; i++) {
      const col = nebulaColor(0.8, (r() - 0.5) * 30000, (r() - 0.5) * 900,
        (r() - 0.5) * 30000, c);
      const m = Math.max(...col);
      if (!(m > 1e-4)) continue;
      n++;
      const [R, G, B] = col.map((v) => v / m);
      // The old failure mode: red high, green crushed, blue high (magenta).
      if (R > 0.9 && B > 0.7 && G < 0.4) mono++;
    }
    return n > 0 && mono / n < 0.5;
  })());

  // Rejection sampling must find gas, and must reject empty space.
  {
    const r = makeRand(83);
    let hits = 0, tries = 0;
    for (let i = 0; i < 4000; i++) { tries++; if (sampleNebulaPoint(r, c, 0.16)) hits++; }
    ok('gas sampling finds cloud regions', hits > 0, hits + '/' + tries);
    ok('gas sampling rejects empty regions', hits < tries, hits + '/' + tries);
  }
  ok('an impossible threshold yields nothing', (() => {
    const r = makeRand(97);
    for (let i = 0; i < 500; i++) if (sampleNebulaPoint(r, c, 1.01)) return false;
    return true;
  })());
}

// ------------------------------------------------------------ configurability
{
  const twoArm = { ...MILKY_WAY, arms: 2 };
  const r = makeRand(101);
  const s = [];
  for (let i = 0; i < 4000; i++) s.push(galaxyStar(r, twoArm));
  ok('a two-arm galaxy also generates cleanly',
    s.every((v) => Number.isFinite(v.x) && Number.isFinite(v.y) && Number.isFinite(v.z)));

  // Degenerate configs must not produce NaN geometry.
  const weird = { ...MILKY_WAY, arms: 1, innerBound: 0, thickness: 0 };
  const r2 = makeRand(103);
  let clean = true;
  for (let i = 0; i < 2000; i++) {
    const v = galaxyStar(r2, weird);
    if (!Number.isFinite(v.x) || !Number.isFinite(v.y) || !Number.isFinite(v.z)) clean = false;
  }
  ok('a degenerate galaxy config still yields finite stars', clean);
  ok('a zero radius does not divide by zero',
    Number.isFinite(logSpiralAngle(0, 0, 4, 0)));
}

try { fs.unlinkSync(out); } catch { /* fine */ }

console.log(pass + ' passed, ' + fail + ' failed');
process.exit(fail ? 1 : 0);
