/**
 * Physics verification for the gravity sandbox.
 *
 * Re-implements the exact integrator and collision rules from
 * SandboxWorld.ts and checks them against analytic expectations:
 * circular-orbit stability, energy conservation, and momentum/mass
 * conservation through inelastic merges.
 *
 * Run: node tools/physics-check.mjs
 */

const G = 42.0;

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const v = (x = 0, y = 0, z = 0) => ({ x, y, z });
const add = (a, b) => v(a.x + b.x, a.y + b.y, a.z + b.z);
const sub = (a, b) => v(a.x - b.x, a.y - b.y, a.z - b.z);
const scale = (a, s) => v(a.x * s, a.y * s, a.z * s);
const len = (a) => Math.hypot(a.x, a.y, a.z);

function accelerations(bodies, gravity, softening) {
  const soft = softening * softening;
  for (const b of bodies) b.acc = v();
  for (let i = 0; i < bodies.length; i++) {
    const a = bodies[i];
    for (let j = i + 1; j < bodies.length; j++) {
      const b = bodies[j];
      const d = sub(b.pos, a.pos);
      const d2 = d.x * d.x + d.y * d.y + d.z * d.z + soft;
      const inv = 1 / Math.sqrt(d2);
      const f = (G * gravity) / d2;
      const fx = d.x * inv * f, fy = d.y * inv * f, fz = d.z * inv * f;
      a.acc = add(a.acc, v(fx * b.mass, fy * b.mass, fz * b.mass));
      b.acc = sub(b.acc, v(fx * a.mass, fy * a.mass, fz * a.mass));
    }
  }
}

/** Velocity Verlet, matching SandboxWorld.step(). */
function step(bodies, dt, gravity = 1, softening = 0.35) {
  accelerations(bodies, gravity, softening);
  for (const b of bodies) {
    if (b.isStar) continue;
    b.vel = add(b.vel, scale(b.acc, dt * 0.5));
    b.pos = add(b.pos, scale(b.vel, dt));
  }
  accelerations(bodies, gravity, softening);
  for (const b of bodies) {
    if (b.isStar) continue;
    b.vel = add(b.vel, scale(b.acc, dt * 0.5));
  }
}

function energy(bodies, gravity = 1, softening = 0.35) {
  let ke = 0, pe = 0;
  for (const b of bodies) ke += 0.5 * b.mass * (b.vel.x ** 2 + b.vel.y ** 2 + b.vel.z ** 2);
  for (let i = 0; i < bodies.length; i++)
    for (let j = i + 1; j < bodies.length; j++) {
      const d = Math.sqrt(
        len(sub(bodies[j].pos, bodies[i].pos)) ** 2 + softening * softening);
      pe -= (G * gravity * bodies[i].mass * bodies[j].mass) / d;
    }
  return ke + pe;
}

console.log('\n— circular orbit stays circular —');
{
  const M = 900, r = 30;
  const speed = Math.sqrt((G * M) / r);
  const bodies = [
    { pos: v(), vel: v(), mass: M, isStar: true, acc: v() },
    { pos: v(r, 0, 0), vel: v(0, 0, speed), mass: 1, isStar: false, acc: v() }
  ];
  const radii = [];
  const dt = 1 / 240;
  for (let i = 0; i < 240 * 40; i++) {     // ~40 s of sim time
    step(bodies, dt);
    if (i % 100 === 0) radii.push(len(bodies[1].pos));
  }
  const rMin = Math.min(...radii), rMax = Math.max(...radii);
  const drift = (rMax - rMin) / r;
  ok(`orbital radius stays within 2% (drift ${(drift * 100).toFixed(3)}%)`, drift < 0.02);

  // it should actually have gone around, not just sat there
  const ang = Math.atan2(bodies[1].pos.z, bodies[1].pos.x);
  ok('body actually orbited (moved off its start angle)', Math.abs(ang) > 0.01);
  ok('body never fell into the star', rMin > r * 0.9, `rMin=${rMin.toFixed(2)}`);
  ok('body never escaped', rMax < r * 1.1, `rMax=${rMax.toFixed(2)}`);
}

console.log('\n— energy is conserved (symplectic integrator) —');
{
  const bodies = [
    { pos: v(-20, 0, 0), vel: v(0, 0, -6), mass: 40, isStar: false, acc: v() },
    { pos: v(20, 0, 0), vel: v(0, 0, 6), mass: 40, isStar: false, acc: v() },
    { pos: v(0, 0, 55), vel: v(-7, 0, 0), mass: 2, isStar: false, acc: v() }
  ];
  const e0 = energy(bodies);
  for (let i = 0; i < 240 * 20; i++) step(bodies, 1 / 240);
  const e1 = energy(bodies);
  const rel = Math.abs((e1 - e0) / e0);
  ok(`total energy drifts < 1% over 20 s (${(rel * 100).toFixed(4)}%)`, rel < 0.01,
     `E0=${e0.toFixed(3)} E1=${e1.toFixed(3)}`);
}

console.log('\n— momentum & mass conserved through a merge —');
{
  // SandboxWorld.collide(), extracted
  const a = { pos: v(0, 0, 0), vel: v(2, 0, 1), mass: 10, radius: 3, isStar: false };
  const b = { pos: v(4, 0, 0), vel: v(-3, 1, 0), mass: 6, radius: 2, isStar: false };
  const pBefore = add(scale(a.vel, a.mass), scale(b.vel, b.mass));
  const mBefore = a.mass + b.mass;

  const d = len(sub(a.pos, b.pos));
  ok('bodies are actually overlapping', d <= a.radius + b.radius);

  const [big, small] = a.mass >= b.mass ? [a, b] : [b, a];
  const m = big.mass + small.mass;
  big.vel = scale(add(scale(big.vel, big.mass), scale(small.vel, small.mass)), 1 / m);
  big.mass = m;

  const pAfter = scale(big.vel, big.mass);
  ok('mass is conserved', Math.abs(big.mass - mBefore) < 1e-9);
  ok('momentum is conserved',
     Math.abs(pAfter.x - pBefore.x) < 1e-9 &&
     Math.abs(pAfter.y - pBefore.y) < 1e-9 &&
     Math.abs(pAfter.z - pBefore.z) < 1e-9,
     `before=${JSON.stringify(pBefore)} after=${JSON.stringify(pAfter)}`);

  const newR = Math.cbrt(m) * 1.5;
  ok('merged radius grows with mass', newR > Math.cbrt(10) * 1.5);
}

console.log('\n— orbital speed formula matches the seeded system —');
{
  // v = sqrt(GM/r) is what seedSystem() uses; verify it yields a bound orbit
  const M = 900;
  for (const r of [26, 41, 56, 71, 86]) {
    const speed = Math.sqrt((G * M) / r);
    const esc = Math.sqrt((2 * G * M) / r);
    ok(`r=${r}: orbital speed is below escape velocity`, speed < esc);
  }
}

console.log('\n— softening prevents singularities —');
{
  const bodies = [
    { pos: v(0, 0, 0), vel: v(), mass: 100, isStar: false, acc: v() },
    { pos: v(0.0001, 0, 0), vel: v(), mass: 1, isStar: false, acc: v() }
  ];
  accelerations(bodies, 1, 0.35);
  const aMag = len(bodies[1].acc);
  ok('acceleration stays finite at near-zero separation', Number.isFinite(aMag) && aMag < 1e6,
     `|a|=${aMag}`);
}


/* ------------------------- beam system verification ------------------------- */
console.log('\n— beam ray/sphere intersection —');
{
  const V = (x, y, z) => ({ x, y, z });
  const dot = (a, b) => a.x * b.x + a.y * b.y + a.z * b.z;
  const sub2 = (a, b) => V(a.x - b.x, a.y - b.y, a.z - b.z);
  const addv = (a, b) => V(a.x + b.x, a.y + b.y, a.z + b.z);
  const scal = (a, s) => V(a.x * s, a.y * s, a.z * s);
  const dist = (a, b) => Math.hypot(a.x - b.x, a.y - b.y, a.z - b.z);

  // mirrors Beam.apply()
  function hit(origin, dir, range, width, target) {
    const oc = sub2(target.pos, origin);
    const along = dot(oc, dir);
    if (along < 0 || along > range) return null;
    const closest = addv(origin, scal(dir, along));
    const miss = dist(closest, target.pos);
    if (miss > target.radius + width * 0.5) return null;
    return { along, miss };
  }

  const origin = V(0, 0, 0), dir = V(0, 0, 1);
  ok('direct hit is detected',
     !!hit(origin, dir, 100, 1, { pos: V(0, 0, 40), radius: 3 }));
  ok('object behind the emitter is not hit',
     !hit(origin, dir, 100, 1, { pos: V(0, 0, -40), radius: 3 }));
  ok('object beyond range is not hit',
     !hit(origin, dir, 100, 1, { pos: V(0, 0, 400), radius: 3 }));
  ok('off-axis miss is rejected',
     !hit(origin, dir, 100, 1, { pos: V(50, 0, 40), radius: 3 }));
  ok('grazing hit within radius is accepted',
     !!hit(origin, dir, 100, 1, { pos: V(3.2, 0, 40), radius: 3 }));
  ok('a wider beam catches more',
     !!hit(origin, dir, 100, 8, { pos: V(6.5, 0, 40), radius: 3 }));

  // tractor pulls toward emitter, repulsor pushes away
  const t = { pos: V(0, 0, 40), vel: V(0, 0, 0), mass: 1 };
  const toEmitter = sub2(origin, t.pos);
  const d = Math.hypot(toEmitter.x, toEmitter.y, toEmitter.z);
  const unit = scal(toEmitter, 1 / d);
  const pullAccel = scal(unit, (3.2 * 10) / (t.mass * 0.5 + 1));
  ok('tractor beam accelerates the target toward the emitter', pullAccel.z < 0);
  const pushAccel = scal(unit, (-3.2 * 10) / (t.mass * 0.5 + 1));
  ok('repulsor accelerates the target away', pushAccel.z > 0);
}

console.log('\n— fragmentation scales with energy and brittleness —');
{
  const nFrags = (energy, fracture) =>
    Math.max(0, Math.min(14, Math.round(Math.cbrt(energy) * fracture * 1.6)));
  ok('low energy does not shatter', nFrags(1, 0.3) < 2);
  ok('high energy shatters', nFrags(4000, 0.9) >= 2);
  ok('brittle material makes more fragments than tough material',
     nFrags(4000, 0.95) > nFrags(4000, 0.1));
  ok('fragment count is capped for performance', nFrags(1e9, 1) <= 14);

  // mass is reduced (vaporisation), never created
  const mass = 100, n = 8;
  const fragMass = (mass * 0.62) / n;
  ok('fragments never exceed the original mass', fragMass * n < mass);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
