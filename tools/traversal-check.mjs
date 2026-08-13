/**
 * traversal-check — the space elevator and the portal gun.
 *
 * Both are physics, so both are testable without a renderer: the elevator's
 * geometry falls out of the geostationary radius, and the portal gun is a
 * rotation between two frames.
 */
import { build } from 'esbuild';
import fs from 'fs';

const load = async (entry, tag) => {
  const out = await build({
    entryPoints: [entry], bundle: true, format: 'esm', write: false, logLevel: 'error'
  });
  const f = `/tmp/${tag}-${Date.now()}.mjs`;
  fs.writeFileSync(f, out.outputFiles[0].text);
  return import(f);
};

let pass = 0, fail = 0;
const ok = (n, c, e = '') => {
  c ? (pass++, console.log('  PASS  ' + n))
    : (fail++, console.log('  FAIL  ' + n + (e ? ' :: ' + e : '')));
};

const { ElevatorSystem, geostationaryRadius, netAcceleration } =
  await load('src/bjs/systems/ElevatorSystem.ts', 'elev');
const { PortalGunSystem, rotateThrough } =
  await load('src/bjs/systems/PortalGunSystem.ts', 'pgun');
const { Vector3 } = await load('node_modules/@babylonjs/core/Maths/math.vector.js', 'v');
const V = (x, y, z) => new Vector3(x, y, z);

console.log('— the elevator is built from real orbital mechanics —');
{
  // Earth-like: mu and omega chosen so geo lands near the real ratio.
  const mu = 398600, omega = 7.292e-5, R = 6371;
  const geo = geostationaryRadius(mu, omega);
  ok(`geostationary radius is computed, not authored (${geo.toFixed(0)} km)`,
     Math.abs(geo - 42164) < 400, geo.toFixed(0));
  ok('it sits well above the surface', geo > R * 5);

  // The defining property: net force is zero exactly there.
  ok('net acceleration is zero at the geostationary radius',
     Math.abs(netAcceleration(mu, omega, geo)) < 1e-9);
  ok('below it you are pulled down', netAcceleration(mu, omega, geo * 0.5) < 0);
  ok('above it you are flung outward', netAcceleration(mu, omega, geo * 1.5) > 0);

  // Monotonic through the zero crossing, or the ride would feel wrong.
  let rising = true, prev = -Infinity;
  for (let r = R; r < geo * 2; r += geo / 60) {
    const a = netAcceleration(mu, omega, r);
    if (a < prev - 1e-12) rising = false;
    prev = a;
  }
  ok('felt gravity changes smoothly all the way up', rising);
}

console.log('— building one —');
{
  const sys = new ElevatorSystem();
  const e = sys.build('earth', V(0, 0, 0), 6371, 398600, 7.292e-5);
  ok('an elevator can be built', !!e);
  ok('the counterweight is beyond geostationary',
     e.counterweightRadius > e.geoRadius);
  ok('the tether spans surface to counterweight',
     Math.abs(e.length - (e.counterweightRadius - e.surfaceRadius)) < 1e-6);

  // Ends and station.
  const base = sys.positionAt(e, 0);
  const top = sys.positionAt(e, 1);
  ok('the base sits on the surface',
     Math.abs(base.length() - e.surfaceRadius) < 1e-6, String(base.length()));
  ok('the top sits at the counterweight',
     Math.abs(top.length() - e.counterweightRadius) < 1e-6);

  const sf = sys.stationFraction(e);
  ok('the station is partway up the tether', sf > 0 && sf < 1, String(sf));
  ok('the station is at zero net gravity',
     Math.abs(sys.feltGravity(e, sf)) < 1e-6);
  ok('riders are pressed down near the bottom', sys.feltGravity(e, 0.05) < 0);
  ok('riders are pulled outward above the station', sys.feltGravity(e, 0.99) > 0);

  // A slow spinner does not make an elevator impossible - it makes it
  // absurdly long, because geostationary orbit is enormously far out.
  const slow = sys.build('sluggish', V(0, 0, 0), 6371, 398600, 1e-7);
  ok('a slow-spinning world needs a far longer tether',
     slow && slow.length > e.length * 10, slow ? slow.length.toFixed(0) : 'null');

  // What IS impossible: geostationary orbit inside the planet itself.
  const tooFast = sys.build('spinner', V(0, 0, 0), 60000, 398600, 6e-4);
  ok('a world whose geostationary orbit is underground gets no elevator',
     tooFast === null);

  // A fast spinner gets a short one - the structure follows the physics.
  const fast = sys.build('fast', V(0, 0, 0), 3000, 398600, 6e-4);
  ok('a fast-spinning world gets a shorter tether',
     fast && fast.geoRadius < 42164, fast ? String(fast.geoRadius) : 'null');
}

console.log('— riding it —');
{
  const sys = new ElevatorSystem();
  const e = sys.build('earth', V(0, 0, 0), 6371, 398600, 7.292e-5);
  const car = sys.addCar(e.id, 2000);
  ok('a car can be added', !!car && car.state === 'docked-surface');

  const seen = new Set();
  for (let i = 0; i < 400; i++) { sys.update(0.5); seen.add(car.state); }
  ok('the car leaves the ground', seen.has('ascending'));
  ok('the car reaches orbit', seen.has('docked-orbit'));
  ok('the car comes back down', seen.has('descending'));
  ok('the car stays on the tether', car.t >= 0 && car.t <= 1);

  [0, -1, NaN, Infinity].forEach((bad) => sys.update(bad));
  ok('a bad timestep cannot break the ride', Number.isFinite(car.t));
  ok('adding a car to a missing elevator is safe', sys.addCar('nope') === null);
}

console.log('— the portal gun links two points —');
{
  const g = new PortalGunSystem();
  ok('nothing is linked at first', !g.linked && g.count === 0);

  g.fire('a', V(0, 0, 0), V(0, 0, 1));
  ok('one portal is not a link', !g.linked && g.count === 1);
  g.fire('b', V(100, 0, 0), V(0, 0, 1));
  ok('two portals make a link', g.linked && g.count === 2);

  // Firing again replaces rather than accumulates.
  g.fire('a', V(5, 0, 0), V(0, 0, 1));
  ok('firing replaces the portal in that slot', g.count === 2);
  ok('and it moved', g.a.position.x === 5);

  ok('a portal with no orientation cannot be placed',
     g.fire('a', V(0, 0, 0), V(0, 0, 0)) === null);
}

console.log('— walking through —');
{
  const g = new PortalGunSystem();
  g.fire('a', V(0, 0, 0), V(0, 0, 1));
  g.fire('b', V(500, 0, 0), V(0, 0, 1));

  // Moving into the front face of A.
  const t = { position: V(0, 0, 0.2), velocity: V(0, 0, -8), radius: 0.5 };
  const r = g.tryTeleport(t, 1 / 60);
  ok('walking into a portal moves you', !!r);
  ok('you come out at the other portal',
     r && Math.abs(r.position.x - 500) < 5, r ? String(r.position.x) : '');
  ok('your speed is preserved',
     Math.abs(t.velocity.length() - 8) < 1e-6, String(t.velocity.length()));

  // Not entering: moving away from the portal.
  const away = { position: V(0, 0, 0.2), velocity: V(0, 0, 8), radius: 0.5 };
  ok('walking away from a portal does nothing', g.tryTeleport(away, 1/60) === null);

  // Missing the opening.
  const wide = { position: V(40, 0, 0.2), velocity: V(0, 0, -8), radius: 0.5 };
  ok('missing the opening does nothing', g.tryTeleport(wide, 1/60) === null);

  // No infinite loop: emerging must not immediately re-enter.
  const loop = { position: V(0, 0, 0.2), velocity: V(0, 0, -8), radius: 0.5 };
  let trips = 0;
  for (let i = 0; i < 600; i++) if (g.tryTeleport(loop, 1 / 60)) trips++;
  ok(`emerging does not re-trigger endlessly (${trips} trips)`, trips < 40);
}

console.log('— the rotation is the whole trick —');
{
  // Floor portal (facing up) to wall portal (facing +x): falling down one
  // must come out moving sideways.
  const out = rotateThrough(V(0, -10, 0), V(0, 1, 0), V(1, 0, 0));
  ok('speed is conserved through the rotation',
     Math.abs(out.length() - 10) < 1e-6, String(out.length()));
  ok('falling into a floor portal exits a wall portal sideways',
     out.x > 9.9, out.toString());

  // Two portals whose normals both point +z are back-to-back, not a
  // straight-through pipe: you enter A against its normal and must emerge
  // from B along its normal, so +z is correct. Anything else would mean
  // walking out backwards.
  const same = rotateThrough(V(0, 0, -5), V(0, 0, 1), V(0, 0, 1));
  ok('you always emerge moving forwards out of the exit face',
     Math.abs(same.z - 5) < 1e-6, same.toString());
  ok('and speed is unchanged', Math.abs(same.length() - 5) < 1e-6);

  // Sideways motion is carried across rather than discarded.
  const strafe = rotateThrough(V(3, 0, -4), V(0, 0, 1), V(0, 0, 1));
  ok('sideways momentum is preserved through a portal',
     Math.abs(strafe.x - 3) < 1e-6 && Math.abs(strafe.length() - 5) < 1e-6,
     strafe.toString());

  // Directly opposed portals must not produce NaN from a zero cross product.
  const opp = rotateThrough(V(1, 0, 0), V(0, 0, 1), V(0, 0, -1));
  ok('opposed portals do not produce NaN',
     Number.isFinite(opp.x) && Number.isFinite(opp.y) && Number.isFinite(opp.z));
  ok('and still conserve speed', Math.abs(opp.length() - 1) < 1e-6);
}

console.log('— portals can bridge two different places —');
{
  const g = new PortalGunSystem();
  g.fire('a', V(0, 0, 0), V(0, 0, 1), 'ocean');
  g.fire('b', V(0, 0, 0), V(0, 0, 1), 'blackhole');
  ok('a pair can span two worlds', g.bridgesWorlds());

  const t = { position: V(0, 0, 0.2), velocity: V(0, 0, -6), radius: 0.5 };
  const r = g.tryTeleport(t, 1 / 60);
  ok('going through takes you to the other world',
     r && r.worldId === 'blackhole', r ? r.worldId : 'no trip');

  const st = g.stats();
  ok('it reports a cross-world link', st['Portal link'] === 'cross-world');
  g.clearAll();
  ok('portals can be cleared', g.count === 0 && !g.linked);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
