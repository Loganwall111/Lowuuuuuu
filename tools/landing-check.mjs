/**
 * landing-check — solid-body collision and gravity anchoring.
 *
 * Flight used to fly straight through planets because nothing ever tested the
 * player's position against a surface. PlanetLanding is the pure-arithmetic
 * fix, so its behaviour is verified here without a GPU:
 *
 *   - penetration is pushed back out along the outward normal,
 *   - inward velocity is cancelled while tangential sliding survives,
 *   - a probe sitting at a planet's core never produces NaN,
 *   - the ground probe returns the surface point and outward normal a walker
 *     anchors to,
 *   - gravity points at the centre and is always finite.
 *
 * Run: node tools/landing-check.mjs
 */
import { build } from 'esbuild';
import fs from 'fs';

const out = await build({
  entryPoints: ['src/bjs/systems/PlanetLanding.ts'],
  bundle: true, format: 'esm', write: false, logLevel: 'error'
});
const f = `/tmp/landing-${Date.now()}.mjs`;
fs.writeFileSync(f, out.outputFiles[0].text);
const {
  nearestSolid, surfaceProbe, resolveCollisions, gravityAccel, planetGround, bodyUnderneath
} = await import(f);

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const earth = { id: 'earth', x: 0, y: 0, z: 0, radius: 10, mass: 600, habitable: true };
const moon = { id: 'moon', x: 40, y: 0, z: 0, radius: 3, mass: 8, habitable: false };

console.log('\n— nearest solid by surface distance —');
{
  ok('picks the closer surface, not the closer centre',
    nearestSolid([earth, moon], 5, 0, 0) === earth);
  ok('picks the moon when near it',
    nearestSolid([earth, moon], 39, 0, 0) === moon);
  ok('ignores zero-radius junk', nearestSolid([{ ...earth, radius: 0 }], 2, 0, 0) === null);
}

console.log('\n— collision pushes you out, never lets you tunnel —');
{
  // Falling straight onto the surface at (10,0,0).
  const r = resolveCollisions([earth], 8, 0, 0, -50, 0, 0);
  ok('position is pushed back to the surface (plus margin)',
    Math.abs(Math.hypot(r.x, r.y, r.z) - 10.25) < 1e-9,
    `r=${Math.hypot(r.x, r.y, r.z).toFixed(3)}`);
  ok('inward velocity is cancelled', Math.abs(r.vx) < 1e-9, `vx=${r.vx}`);
  ok('the contact reports the sphere', r.contacts.length === 1 && r.contacts[0].id === 'earth');
  ok('the contact normal is outward', r.contacts[0].nx > 0.9);
}

console.log('\n— tangential sliding survives the stop —');
{
  // Skimming past the surface with sideways motion.
  const r = resolveCollisions([earth], 10.1, 0, 0, 0, 0, 12);
  ok('sideways (tangential) velocity is preserved', Math.abs(r.vz - 12) < 1e-9, `vz=${r.vz}`);
  ok('and is pushed back out', Math.hypot(r.x, r.y, r.z) >= 10.25 - 1e-9);
}

console.log('\n— a probe at the core cannot blow up —');
{
  const r = resolveCollisions([earth], 0, 0, 0, 0, 0, 0);
  ok('a body at the exact centre resolves to a finite position',
    [r.x, r.y, r.z].every(Number.isFinite));
  ok('and is ejected to the surface', Math.abs(Math.hypot(r.x, r.y, r.z) - 10.25) < 1e-9);
  const s = surfaceProbe(earth, 0, 0, 0);
  ok('the surface probe at the core returns a unit outward normal',
    Math.abs(Math.hypot(s.nx, s.ny, s.nz) - 1) < 1e-9);
}

console.log('\n— two spheres squeeze the player out of both —');
{
  const r = resolveCollisions([earth, moon], 12, 0, 0, 0, 0, 0);
  const d1 = Math.hypot(r.x - earth.x, r.y, r.z) - earth.radius;
  const d2 = Math.hypot(r.x - moon.x, r.y, r.z) - moon.radius;
  ok('ends up outside both bodies', d1 >= -1e-9 && d2 >= -1e-9,
    `d1=${d1.toFixed(3)} d2=${d2.toFixed(3)}`);
}

console.log('\n— the ground probe anchors a walker to the sphere —');
{
  const g = planetGround([earth], 6, 8, 0);
  ok('a nearby probe returns ground', !!g);
  ok('the ground point sits on the surface',
    Math.abs(Math.hypot(g.px, g.py, g.pz) - earth.radius) < 1e-9);
  ok('the normal is unit and outward',
    Math.abs(Math.hypot(g.nx, g.ny, g.nz) - 1) < 1e-9 &&
    Math.abs(g.nx - 0.6) < 1e-9 && Math.abs(g.ny - 0.8) < 1e-9);
  ok('far from any body there is no ground',
    planetGround([earth, moon], 1000, 0, 0) === null);
}

console.log('\n— gravity pulls toward the centre, and stays finite —');
{
  const g = gravityAccel(earth, 20, 0, 0);
  ok('gravity points at the planet', g.gx < 0 && Math.abs(g.gy) < 1e-9);
  ok('gravity at the core does not explode',
    (() => { const c = gravityAccel(earth, 0, 0, 0); return [c.gx, c.gy, c.gz].every(Number.isFinite); })());
  ok('gravity is capped, never violent',
    Math.hypot(g.gx, g.gy, g.gz) <= 60);
  ok('a massless body exerts nothing',
    (() => { const c = gravityAccel({ ...earth, mass: 0 }, 20, 0, 0); return Math.hypot(c.gx, c.gy, c.gz) === 0; })());
}

console.log('\n— the body directly under the walker —');
{
  ok('finds the body beneath within range', bodyUnderneath([earth, moon], 6, 0, 0, 5) === earth);
  ok('does not claim a body too far below', bodyUnderneath([earth, moon], 16, 0, 0, 1) === null);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
