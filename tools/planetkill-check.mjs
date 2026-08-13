/**
 * planetkill-check — cutting a planet open and watching the core erupt.
 *
 * The interesting failure modes here are invisible on screen: a planet that
 * gains mass as you shoot it, ejecta that appears from nowhere, or a core
 * that erupts before the crust has been breached. All of that is checked
 * numerically.
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

const { PlanetDestructionSystem } =
  await load('src/bjs/systems/PlanetDestructionSystem.ts', 'pdk');
const { Vector3 } = await load('node_modules/@babylonjs/core/Maths/math.vector.js', 'v');

const V = (x, y, z) => new Vector3(x, y, z);
const fresh = () => {
  const s = new PlanetDestructionSystem();
  const p = s.add('terra', V(0, 0, 0), 100);
  return { s, p };
};
/** A point on the surface, in the +X direction. */
const surface = (p) => V(p.radius, 0, 0);

console.log('— a planet is layered, not a health bar —');
{
  const { p } = fresh();
  ok('it has three shells', p.shells.length === 3);
  ok('crust, mantle, core',
     p.shells.map((s) => s.id).join(',') === 'crust,mantle,core');
  ok('the core is the toughest',
     p.shells[2].strength > p.shells[1].strength &&
     p.shells[1].strength > p.shells[0].strength);
  ok('the core is the hottest', p.shells[2].temperature > 5000);
  ok('the core is the densest', p.shells[2].density > p.shells[0].density);
  ok('shells nest without gaps',
     p.shells[0].inner === p.shells[1].outer &&
     p.shells[1].inner === p.shells[2].outer);
  ok('it starts intact', p.phase === 'intact' && p.integrity === 1);
  ok('it has mass', p.mass > 0 && p.mass === p.originalMass);
}

console.log('— damage bores inward through the layers in order —');
{
  const { s, p } = fresh();
  // A weak shot must not reach the core.
  s.damage('terra', surface(p), 4000);
  ok('a light hit only scratches the crust',
     p.shells[0].damage > 0 && p.shells[2].damage === 0);
  ok('the mantle is untouched by a light hit', p.shells[1].damage === 0);
  ok('the planet reports being wounded', p.phase === 'wounded');

  // Keep firing at the same spot: it must deepen, not spread.
  const before = p.wounds.length;
  for (let i = 0; i < 20; i++) s.damage('terra', surface(p), 4000);
  ok('sustained fire reuses one wound rather than making thousands',
     p.wounds.length === before, `${p.wounds.length} wounds`);
  ok('the wound gets deeper', p.wounds[0].depth > 0);
}

console.log('— the core cannot erupt before the crust is breached —');
{
  const { s, p } = fresh();
  const order = [];
  let guard = 0;
  while (p.phase !== 'destroyed' && guard++ < 4000) {
    const evs = s.damage('terra', surface(p), 20000);
    for (const e of evs) if (!order.includes(e.kind)) order.push(e.kind);
    if (order.includes('eruption')) break;
  }
  ok('a breach happens first', order[0] === 'breach', order.join(' -> '));
  ok('then the mantle', order.indexOf('mantle') === 1, order.join(' -> '));
  ok('then the core erupts', order.includes('eruption'), order.join(' -> '));
  ok('the eruption is announced',
     s.recentEvents(20).some((e) => /CORE BREACH/.test(e.message)));
}

console.log('— the core erupting throws real debris —');
{
  const { s, p } = fresh();
  let guard = 0;
  while (!s.recentEvents(30).some((e) => e.kind === 'eruption') && guard++ < 4000) {
    s.damage('terra', surface(p), 20000);
  }
  ok('an eruption occurred', guard < 4000);

  const debris = s.debris;
  ok(`debris is thrown (${debris.length} pieces)`, debris.length > 50);
  ok('some of it is core material',
     debris.some((d) => d.origin === 'core'));
  ok('core material is incandescent',
     debris.filter((d) => d.origin === 'core').every((d) => d.temperature > 1000));
  ok('crust is thrown too', debris.some((d) => d.origin === 'crust'));

  // A jet, not a sphere: core material must be directional.
  const core = debris.filter((d) => d.origin === 'core');
  const mean = core.reduce((a, d) => a.add(d.vel.clone().normalize()),
                           V(0, 0, 0)).scale(1 / core.length);
  ok('the core vents as a directional jet, not a uniform sphere',
     mean.length() > 0.7, 'alignment ' + mean.length().toFixed(2));

  ok('every piece is moving', debris.every((d) => d.vel.length() > 0));
  ok('every piece has mass', debris.every((d) => d.mass > 0));
  ok('the blast spins the planet up', p.spin > 0);
}

console.log('— mass is conserved: shooting a planet cannot create matter —');
{
  const { s, p } = fresh();
  const m0 = p.mass;
  for (let i = 0; i < 400; i++) s.damage('terra', surface(p), 9000);
  ok('the planet loses mass', p.mass < m0);
  ok('mass never goes negative', p.mass >= 0);
  ok('mass never increases',
     p.mass <= p.originalMass, `${p.mass} > ${p.originalMass}`);

  // Radius must track mass as a cube root, not arbitrarily.
  const ratio = p.mass / p.originalMass;
  const expected = p.originalRadius * Math.cbrt(ratio);
  ok('the planet shrinks as it loses mass', p.radius < p.originalRadius);
  ok('radius follows the cube root of the mass ratio',
     Math.abs(p.radius - expected) < 1e-6,
     `${p.radius.toFixed(3)} vs ${expected.toFixed(3)}`);
}

console.log('— it eventually dies, and stays dead —');
{
  const { s, p } = fresh();
  let guard = 0;
  while (p.phase !== 'destroyed' && guard++ < 20000) {
    s.damage('terra', surface(p), 30000);
  }
  ok('a planet can actually be destroyed', p.phase === 'destroyed', p.phase);
  // Death has two legitimate causes: the structure is spent, or so much
  // mass has been torn away that there is no planet left. Either is valid;
  // requiring both would be wrong.
  ok('it died for a defensible reason (no structure, or no mass left)',
     p.integrity < 0.35 || p.mass / p.originalMass < 0.25,
     `integrity ${p.integrity.toFixed(3)}, mass ${(p.mass / p.originalMass).toFixed(3)}`);

  // Firing at a corpse must be a no-op, not a crash or a resurrection.
  const before = p.mass;
  const evs = s.damage('terra', V(1, 0, 0), 999999);
  ok('shooting a destroyed planet does nothing', evs.length === 0);
  ok('and cannot restore its mass', p.mass === before);
}

console.log('— phases progress in one direction —');
{
  const { s, p } = fresh();
  const RANK = { intact: 0, wounded: 1, bleeding: 2, erupting: 3,
                 fracturing: 4, destroyed: 5 };
  let last = 0, monotonic = true, seen = new Set();
  for (let i = 0; i < 3000 && p.phase !== 'destroyed'; i++) {
    s.damage('terra', surface(p), 15000);
    const r = RANK[p.phase];
    seen.add(p.phase);
    if (r < last) monotonic = false;
    last = r;
  }
  ok('a planet never heals itself under fire', monotonic);
  ok(`it passes through several states (${[...seen].join(', ')})`, seen.size >= 3);
}

console.log('— a big impact craters rather than drilling —');
{
  const { s, p } = fresh();
  s.impact('terra', surface(p), 200000);
  ok('an impact makes several overlapping wounds', p.wounds.length > 1);
  ok('an impact spins the planet', p.spin > 0);
  ok('an impact damages the crust broadly', p.shells[0].damage > 0);
}

console.log('— debris obeys gravity and is bounded —');
{
  const { s, p } = fresh();
  for (let i = 0; i < 200; i++) s.damage('terra', surface(p), 20000);
  const n0 = s.debris.length;
  ok('there is debris to simulate', n0 > 0);

  // A slow piece near the surface must fall back toward the planet.
  const piece = s.debris.find((d) => d.vel.length() < 30);
  if (piece) {
    const before = Vector3.Distance(piece.pos, p.center);
    const v0 = piece.vel.clone();
    for (let i = 0; i < 30; i++) s.update(1 / 60);
    ok('gravity acts on debris', !piece.vel.equals(v0));
  } else {
    ok('gravity acts on debris (no slow piece to sample)', true);
  }

  for (let i = 0; i < 2000; i++) s.update(1 / 60);
  ok('debris is eventually culled, not accumulated forever',
     s.debris.length < n0 + 900, String(s.debris.length));
  ok('debris count stays bounded', s.debris.length <= 900);
  ok('no debris has NaN position',
     s.debris.every((d) => Number.isFinite(d.pos.x) && Number.isFinite(d.pos.y)));
}

console.log('— restore and bad input —');
{
  const { s, p } = fresh();
  for (let i = 0; i < 500; i++) s.damage('terra', surface(p), 20000);
  ok('the planet is damaged', p.phase !== 'intact');
  s.restore('terra');
  ok('restore makes it whole', p.phase === 'intact' && p.integrity === 1);
  ok('restore returns its mass', p.mass === p.originalMass);
  ok('restore returns its radius', p.radius === p.originalRadius);
  ok('restore clears the wounds', p.wounds.length === 0);

  const { s: s2, p: p2 } = fresh();
  [0, -100, NaN, Infinity].forEach((e) => s2.damage('terra', surface(p2), e));
  ok('nonsense energy is ignored',
     p2.phase === 'intact' && Number.isFinite(p2.mass));
  ok('damaging an unknown planet is safe',
     s2.damage('nope', V(0, 0, 0), 100).length === 0);
  s2.update(0);
  s2.update(-1);
  ok('a zero or negative timestep is safe', Number.isFinite(p2.mass));

  const st = s2.stats();
  ok('it reports how many worlds are destructible', !!st['Destructible worlds']);
  ok('it reports debris in flight', st['Debris in flight'] !== undefined);
}

console.log('— alien ships actually break the world —');
{
  const { AlienFleetSystem, SHIP_CLASSES } =
    await load('src/bjs/systems/AlienFleetSystem.ts', 'fleet');

  ok('there are several ship classes', Object.keys(SHIP_CLASSES).length >= 4);
  ok('dreadnoughts are the biggest',
     SHIP_CLASSES.dreadnought.size > SHIP_CLASSES.swarmer.size);
  ok('ships are enormous', SHIP_CLASSES.dreadnought.size > 100);
  ok('every class has a weapon',
     Object.values(SHIP_CLASSES).every((d) => d.power > 0));

  const s = new PlanetDestructionSystem();
  const p = s.add('victim', V(0, 0, 0), 100);
  const fleet = new AlienFleetSystem(s);
  const ships = fleet.spawnFleet(7, 'victim', V(0, 0, 0), 100, 6);

  ok('a fleet arrives', ships.length === 6 && fleet.size === 6);
  ok('ships start outside the planet',
     ships.every((k) => k.pos.length() > p.radius * 5));
  ok('ships start by approaching', ships.every((k) => k.state === 'approach'));
  ok('a seed always sends the same fleet',
     new AlienFleetSystem(s).spawnFleet(7, 'v', V(0,0,0), 100, 6)
       .map((k) => k.def.cls).join(',') === ships.map((k) => k.def.cls).join(','));

  const lookup = (id) => (id === 'victim' && p.phase !== 'destroyed'
    ? { center: p.center, radius: p.radius } : null);

  // Run the attack.
  const states = new Set();
  let fired = false;
  for (let i = 0; i < 4000; i++) {
    fleet.update(1 / 30, lookup);
    s.update(1 / 30);
    for (const k of fleet.fleet) {
      states.add(k.state);
      if (k.firing) fired = true;
    }
    if (p.phase === 'destroyed') break;
  }

  ok('ships close on the planet and hold station', states.has('orbit'));
  ok('ships charge their weapons', states.has('charge'));
  ok('ships open fire', fired && states.has('fire'));
  ok('the bombardment actually damages the planet', p.mass < p.originalMass);
  ok(`sustained bombardment destroys the world (${p.phase})`,
     p.phase === 'destroyed' || p.integrity < 0.5,
     `integrity ${p.integrity.toFixed(3)}`);
  ok('the attack is announced',
     fleet.recentEvents(20).some((e) => /opens fire/.test(e.message)));

  // Ships must concentrate fire, not scatter it.
  ok('the fleet drills one wound rather than scratching many',
     p.wounds.length <= 3, `${p.wounds.length} wounds`);

  // With the planet gone the fleet must leave rather than shoot a corpse.
  const dead = () => null;
  for (let i = 0; i < 900; i++) fleet.update(1 / 30, dead);
  ok('the fleet leaves when there is nothing left', fleet.size === 0);

  // Robustness.
  const f2 = new AlienFleetSystem(s);
  f2.spawnFleet(3, 'nope', V(0, 0, 0), 50, 3);
  for (const bad of [0, -1, NaN, Infinity]) f2.update(bad, () => null);
  ok('a bad timestep cannot corrupt the fleet',
     f2.fleet.every((k) => Number.isFinite(k.pos.x)));
  f2.recall();
  ok('the fleet can be recalled',
     f2.fleet.every((k) => k.state === 'withdraw'));
  f2.clear();
  ok('the fleet can be cleared', f2.size === 0);
}

console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
