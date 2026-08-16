/**
 * wormhole-check — traversable wormholes, orbital traffic and alien wanderers.
 *
 * The three new "lived-in universe" systems are pure arithmetic below their
 * renderers, so their logic is pinned here without a GPU:
 *
 *   - the wormhole field is deterministic, typed, and always finite,
 *   - an inclined circular orbit is a stable, bounded loop,
 *   - a rare alien sighting is present for a bounded window, then gone.
 *
 * Run: node tools/wormhole-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const load = async (entry) => {
  const out = await build({
    entryPoints: ['src/bjs/systems/' + entry],
    bundle: true, format: 'esm', write: false, logLevel: 'error'
  });
  const f = `/tmp/${entry.replace(/\W/g, '_')}-${Date.now()}.mjs`;
  fs.writeFileSync(f, out.outputFiles[0].text);
  return import(f);
};

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const W = await load('WormholeField.ts');
console.log('\n— the wormhole field is deterministic and typed —');
{
  const a = W.wormholeSpecs(1234);
  const b = W.wormholeSpecs(1234);
  ok('the same seed gives the same wormholes', JSON.stringify(a) === JSON.stringify(b));
  ok('the field has the promised count', a.length === W.WORMHOLE_COUNT);
  ok('every wormhole is finite and physical', a.every((w) =>
    [w.ax, w.ay, w.az, w.bx, w.by, w.bz, w.radius].every(Number.isFinite) &&
    w.radius > 0 && w.id && w.type));
  const types = new Set(a.map((w) => w.type));
  ok('multiple wormhole types exist in the field', types.size >= 2, [...types].join(','));
  ok('an interstellar gate appears somewhere in the field', types.has('interstellar'));
  ok('bridge wormholes genuinely span two points', a.some((w) =>
    w.type !== 'interstellar' &&
    Math.hypot(w.ax - w.bx, w.ay - w.by, w.az - w.bz) > 100));
  ok('different seeds give different fields',
    JSON.stringify(W.wormholeSpecs(4321)) !== JSON.stringify(a));
}

const O = await load('OrbitTraffic.ts');
console.log('\n— an inclined orbit is a stable, bounded loop —');
{
  const orbit = O.ISS_ORBIT;
  const r = Math.hypot(...O.orbitPosition(orbit, 5, 0, 0, 0));
  ok('the orbit stays at its radius', Math.abs(r - orbit.radius) < 1e-6, String(r));
  let minY = Infinity, maxY = -Infinity, minZ = Infinity, maxZ = -Infinity;
  for (let i = 0; i < 400; i++) {
    const p = O.orbitPosition(orbit, i * 0.05, 0, 0, 0);
    minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]);
    minZ = Math.min(minZ, p[2]); maxZ = Math.max(maxZ, p[2]);
  }
  ok('the inclined orbit rises and falls', maxY - minY > 0.1);
  ok('and stays bounded on every axis',
    maxY - minY < orbit.radius * 2.1 && maxZ - minZ < orbit.radius * 2.1);
  ok('an orbit translated to a centre follows it', (() => {
    const p = O.orbitPosition(orbit, 0, 100, 0, 0);
    return Math.abs(Math.hypot(p[0] - 100, p[1], p[2]) - orbit.radius) < 1e-6;
  })());
}

const A = await load('AlienTraffic.ts');
console.log('\n— alien sightings are rare, brief, and real —');
{
  // A sighting is present for a bounded window, then gone again.
  let seen = false, ever = false;
  for (let t = 0; t < A.SIGHTING_INTERVAL * 4; t += 4) {
    const s = A.wandererAt(7, t);
    if (s.present) { seen = true; ever = true; }
  }
  ok('a ship appears within a few intervals', ever);
  ok('a sighting is not continuous', (() => {
    let breaks = 0, last = false;
    for (let t = 0; t < A.SIGHTING_INTERVAL * 3; t += 2) {
      const now = A.wandererAt(7, t).present;
      if (last && !now) breaks++;
      last = now;
    }
    return breaks > 0;
  })());
  ok('when present, the ship sits far out', (() => {
    for (let t = 0; t < A.SIGHTING_INTERVAL * 4; t += 2) {
      const s = A.wandererAt(7, t);
      if (s.present && Math.hypot(s.x, s.y, s.z) < 600) return false;
    }
    return true;
  })());
  ok('the schedule is deterministic',
    JSON.stringify(A.wandererAt(7, 1000)) === JSON.stringify(A.wandererAt(7, 1000)));
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
