/**
 * comet-check — comets on real elliptical orbits.
 *
 * A comet that never moves is a rock. This pins the Kepler orbit model the
 * renderer runs on: eccentric anomaly solves to a valid true position, the
 * orbit is an ellipse in the star's frame, the tail streams away from the
 * star, and the whole family is deterministic.
 *
 * Run: node tools/comet-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/CometSystem.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/comet-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const { cometSpecs, cometState, eccentricAnomaly, COMET_COUNT } = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

console.log('\n— the comet family is deterministic and sane —');
{
  const a = cometSpecs(7);
  const b = cometSpecs(7);
  ok('a seed gives the same comets back', JSON.stringify(a) === JSON.stringify(b));
  ok('the family has the promised size', a.length === COMET_COUNT);
  ok('every comet is physically plausible', a.every((c) =>
    c.a > 0 && c.e >= 0 && c.e < 1 && c.period > 0 &&
    c.head > 0 && c.tint.length === 3));
  ok('different seeds give different orbits',
    JSON.stringify(cometSpecs(8)) !== JSON.stringify(cometSpecs(9)));
}

console.log('\n— Kepler solves to a valid position —');
{
  const c = cometSpecs(3)[0];
  const p = cometState(c, 12.5);
  ok('the position is finite', [p.x, p.y, p.z].every(Number.isFinite));
  // Radius must lie between perihelion and apohelion.
  const peri = c.a * (1 - c.e), apo = c.a * (1 + c.e);
  ok('the radius stays inside the ellipse bounds',
    p.radius >= peri * 0.999 && p.radius <= apo * 1.001,
    `r=${p.radius.toFixed(1)} vs [${peri.toFixed(1)},${apo.toFixed(1)}]`);
  ok('the tail is a unit direction', Math.abs(Math.hypot(p.tx, p.ty, p.tz) - 1) < 1e-6);
  // Tail points away from the star: dot(position, tail) must be positive.
  ok('the tail streams away from the star',
    p.x * p.tx + p.y * p.ty + p.z * p.tz > 0);
}

console.log('\n— the orbit is an ellipse in the star frame —');
{
  const c = cometSpecs(11)[0];
  // Sample a full period; every radius must stay in bounds and the body must
  // actually come around (angle covers a full turn).
  const peri = c.a * (1 - c.e), apo = c.a * (1 + c.e);
  let min = Infinity, max = -Infinity, angleSpan = 0, last = null;
  const N = 360;
  for (let i = 0; i <= N; i++) {
    const t = (i / N) * c.period;
    const p = cometState(c, t);
    min = Math.min(min, p.radius);
    max = Math.max(max, p.radius);
    if (last) {
      const d = Math.atan2(p.y - 0, p.x - 0) - last;
      angleSpan += Math.atan2(Math.sin(d), Math.cos(d));
    }
    last = Math.atan2(p.y, p.x);
  }
  ok('radius sweeps the full range over one period',
    min <= peri * 1.01 && max >= apo * 0.99,
    `min=${min.toFixed(1)} max=${max.toFixed(1)}`);
  ok('the comet comes all the way around the star',
    Math.abs(angleSpan) > 6, angleSpan.toFixed(2) + ' rad');
}

console.log('\n— activity peaks at perihelion —');
{
  const c = cometSpecs(21)[0];
  const peri = c.a * (1 - c.e);
  // Find the time of closest approach and confirm activity is highest there.
  let best = { t: 0, r: Infinity, act: 0 };
  for (let i = 0; i < 2000; i++) {
    const t = (i / 2000) * c.period;
    const p = cometState(c, t);
    if (p.radius < best.r) best = { t, r: p.radius, act: p.activity };
  }
  ok('perihelion lands on the perihelion distance', Math.abs(best.r - peri) < peri * 0.02,
    `r=${best.r.toFixed(1)} vs ${peri.toFixed(1)}`);
  ok('the comet is most active at perihelion', best.act > 0.95);
  const far = cometState(c, (best.t + c.period / 2) % c.period);
  ok('and quiet at apohelion', far.activity < 0.3);
}

console.log('\n— eccentric anomaly is a stable solver —');
{
  for (let i = 0; i < 200; i++) {
    const e = 0.1 + (i / 200) * 0.8;
    const M = (i % 360) * Math.PI / 180;
    const E = eccentricAnomaly(M, e);
    const resid = E - e * Math.sin(E) - M;
    if (Math.abs(resid) > 1e-6) {
      ok('eccentric anomaly satisfies Kepler to 1e-6', false, `resid=${resid}`);
      console.log(`\n${pass} passed, ${fail} failed`);
      process.exit(1);
    }
  }
  ok('eccentric anomaly satisfies Kepler to 1e-6 across the table', true);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
