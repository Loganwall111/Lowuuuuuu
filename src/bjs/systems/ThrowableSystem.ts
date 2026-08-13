/**
 * ThrowableSystem — the things you throw at planets.
 *
 * The loop this project is built around is CREATE, EXPERIMENT, BREAK,
 * OBSERVE, CHANGE VARIABLES. That only works if there is a deep bench of
 * things to throw and the outcome is computed rather than scripted.
 *
 * Every entry below is a real physical body: it has mass, radius and a
 * composition, and what happens when it meets a planet is worked out from
 * kinetic energy against the target's binding energy. A rubber duck the
 * size of a moon is funny AND is genuinely a moon-sized mass, so it does
 * moon-sized damage. Nothing here is a special case.
 */

/** What a throwable is made of, which decides how it behaves on impact. */
export type Composition =
  | 'rock' | 'ice' | 'iron' | 'gas' | 'organic' | 'exotic' | 'antimatter';

export interface Throwable {
  id: string;
  name: string;
  glyph: string;
  /** Mass in kilograms. */
  mass: number;
  /** Radius in metres. */
  radius: number;
  composition: Composition;
  /** Flavour text shown in the catalogue. */
  note: string;
  /** Some objects do something other than simply hit. */
  behaviour?: 'orbit' | 'burrow' | 'grow' | 'split' | 'devour';
}

/**
 * How much energy per kilogram each composition releases beyond raw kinetic
 * impact. Antimatter is E=mc^2; the rest are chemical or nil.
 */
export const YIELD_PER_KG: Record<Composition, number> = {
  rock: 0,
  ice: 0,
  iron: 0,
  gas: 4.5e7,        // roughly hydrogen combustion
  organic: 1.6e7,
  exotic: 3.0e12,
  antimatter: 8.98e16 // c^2
};

/**
 * The catalogue. Deliberately spans twenty orders of magnitude in mass, so
 * the same physics produces both "a pebble bounces" and "the crust is gone".
 */
export const THROWABLES: Throwable[] = [
  // ---- ordinary matter, increasing absurdity ----
  { id: 'pebble', name: 'Pebble', glyph: '🪨', mass: 0.2, radius: 0.03,
    composition: 'rock', note: 'A perfectly ordinary stone. Start here.' },
  { id: 'anvil', name: 'Anvil', glyph: '🔨', mass: 90, radius: 0.4,
    composition: 'iron', note: 'Heavier than it looks, as always.' },
  { id: 'piano', name: 'Grand Piano', glyph: '🎹', mass: 480, radius: 1.4,
    composition: 'organic', note: 'Falls with a chord.' },
  { id: 'bus', name: 'City Bus', glyph: '🚌', mass: 12000, radius: 6,
    composition: 'iron', note: 'Route terminates at the mantle.' },
  { id: 'duck', name: 'Colossal Rubber Duck', glyph: '🦆', mass: 4.2e14, radius: 3200,
    composition: 'organic', note: 'Bath toy scaled to a small moon. Still squeaks.' },
  { id: 'teapot', name: 'Utah Teapot', glyph: '🫖', mass: 8.1e12, radius: 900,
    composition: 'rock', note: 'Rendered at last in physical form.' },

  // ---- astronomical ----
  { id: 'asteroid', name: 'Iron Asteroid', glyph: '☄', mass: 3.2e15, radius: 5000,
    composition: 'iron', note: 'A modest city-killer.' },
  { id: 'comet', name: 'Long-Period Comet', glyph: '🌠', mass: 2.2e14, radius: 4000,
    composition: 'ice', note: 'Mostly ice. Arrives fast, leaves a tail.' },
  { id: 'moon', name: 'Captured Moon', glyph: '🌑', mass: 7.3e22, radius: 1.7e6,
    composition: 'rock', note: 'A moon borrowed from elsewhere. They will notice.' },
  { id: 'neutron', name: 'Neutron Star Fragment', glyph: '⭐', mass: 4.0e26, radius: 900,
    composition: 'exotic', note: 'A teaspoon weighs a billion tonnes.' },
  { id: 'antimatter', name: 'Antimatter Sphere', glyph: '⚛', mass: 1.0e6, radius: 12,
    composition: 'antimatter', note: 'One tonne of this is not one tonne of anything else.' },

  // ---- things that misbehave on purpose ----
  { id: 'octopus', name: 'Deep-Space Octopus', glyph: '🐙', mass: 9.1e20, radius: 240000,
    composition: 'organic', behaviour: 'devour',
    note: 'Arrives from the dark, takes up orbit, and begins to feed.' },
  { id: 'tentacle', name: 'Planetary Tentacle', glyph: '🦑', mass: 3.4e18, radius: 60000,
    composition: 'organic', behaviour: 'grow',
    note: 'Erupts from the crust and keeps growing. Not native.' },
  { id: 'driller', name: 'Self-Replicating Driller', glyph: '⛏', mass: 2.0e6, radius: 40,
    composition: 'iron', behaviour: 'burrow',
    note: 'Digs toward the core, making copies as it goes.' },
  { id: 'seed', name: 'Von Neumann Seed', glyph: '🌱', mass: 1.2e4, radius: 3,
    composition: 'exotic', behaviour: 'split',
    note: 'One becomes two. Two becomes four.' },
  { id: 'swarm', name: 'Orbital Swarm', glyph: '🛰', mass: 6.0e11, radius: 90000,
    composition: 'iron', behaviour: 'orbit',
    note: 'Does not land. Encircles, and waits.' }
];

const BY_ID = new Map(THROWABLES.map((t) => [t.id, t]));

export function throwableById(id: string): Throwable | null {
  return BY_ID.get(id) ?? null;
}

/** Everything that does something unusual on arrival. */
export function exoticThrowables(): Throwable[] {
  return THROWABLES.filter((t) => !!t.behaviour);
}

/* -------------------------------------------------------------------------- */
/*  Impact                                                                     */
/* -------------------------------------------------------------------------- */

export interface ImpactResult {
  /** Kinetic energy delivered, joules. */
  kinetic: number;
  /** Additional energy released by the material itself, joules. */
  release: number;
  /** Total, joules. */
  total: number;
  /** Equivalent in megatons of TNT, because joules mean nothing to anyone. */
  megatons: number;
  /** Crater diameter in metres, 0 if the impactor merely bounced. */
  craterDiameter: number;
  /** Fraction of the target's gravitational binding energy delivered, 0-1+. */
  bindingFraction: number;
  /** What actually happened, in words. */
  outcome: 'bounce' | 'crater' | 'regional' | 'extinction' | 'crust-loss' | 'shattered';
  description: string;
}

/**
 * Gravitational binding energy of a uniform sphere: U = 3GM^2 / 5R.
 *
 * This is the honest measure of "what would it take to destroy this
 * planet" - the energy needed to disperse it against its own gravity.
 */
export function bindingEnergy(mass: number, radius: number): number {
  const G = 6.674e-11;
  if (!(mass > 0) || !(radius > 0)) return 0;
  return (3 * G * mass * mass) / (5 * radius);
}

/**
 * Works out what happens when a throwable meets a body.
 *
 * @param t          what was thrown
 * @param speed      impact speed, m/s
 * @param targetMass target mass, kg
 * @param targetR    target radius, m
 */
export function computeImpact(
  t: Throwable, speed: number, targetMass: number, targetR: number
): ImpactResult {
  const v = Math.max(0, Number.isFinite(speed) ? speed : 0);
  const kinetic = 0.5 * t.mass * v * v;
  const release = t.mass * (YIELD_PER_KG[t.composition] ?? 0);
  const total = kinetic + release;
  const megatons = total / 4.184e15;

  const binding = bindingEnergy(targetMass, targetR);
  const bindingFraction = binding > 0 ? total / binding : Infinity;

  // Crater scaling: diameter goes as roughly the sixth root of energy, the
  // standard empirical result for hypervelocity impacts.
  const craterDiameter = total > 1e9 ? 1.8 * Math.pow(total / 1e9, 1 / 6) * 40 : 0;

  let outcome: ImpactResult['outcome'];
  let description: string;

  if (bindingFraction >= 1) {
    outcome = 'shattered';
    description = 'The target is unbound. Nothing holds it together any more.';
  } else if (bindingFraction >= 0.01) {
    // A percent of a planet's binding energy is already colossal - this is
    // the Moon-forming-impact regime, which resurfaces a world entirely.
    // The old threshold of 0.1 let events like that fall through to the
    // megaton test and be reported as a mere "extinction".
    outcome = 'crust-loss';
    description = bindingFraction >= 0.1
      ? 'The crust is stripped and the mantle is exposed.'
      : 'A mantle-deep wound. The surface is remade and the core shows through.';
  } else if (megatons >= 1e5) {
    outcome = 'extinction';
    description = 'A global event. The atmosphere burns.';
  } else if (megatons >= 100) {
    outcome = 'regional';
    description = 'Regional devastation, and a crater you can see from orbit.';
  } else if (craterDiameter > 1) {
    outcome = 'crater';
    description = 'A crater, some noise, and a lot of dust.';
  } else {
    outcome = 'bounce';
    description = 'Barely a mark. Try something with more behind it.';
  }

  return { kinetic, release, total, megatons, craterDiameter, bindingFraction, outcome, description };
}

/* -------------------------------------------------------------------------- */
/*  Mining                                                                     */
/* -------------------------------------------------------------------------- */

export interface MiningBeam {
  /** Beam power, watts. */
  power: number;
  /** Beam radius at the surface, metres. */
  radius: number;
}

/**
 * Beam power is a gameplay decision constrained by real numbers. At 5e12 W
 * the beam bored two centimetres a second - a drill, not the planet-cutter
 * this is meant to be. At 8e20 W it went through an Earth-sized world in
 * three seconds, which removes any skill from aiming it.
 *
 * 2.2e17 W sits where it should: roughly a kilometre a second, so you carve
 * a visible canyon in seconds, reach the mantle in under a minute, and need
 * about two and a half hours of sustained fire to bore clean through a
 * planet. Big enough to feel powerful, slow enough that where you point it
 * matters.
 */
export const DEFAULT_MINING: MiningBeam = { power: 2.2e17, radius: 45 };

/**
 * How deep a beam bores in a given time.
 *
 * Energy is spent vaporising rock: depth follows from the beam's power, the
 * area it covers, and the enthalpy of vaporisation of silicate rock
 * (~1.1e7 J/kg at a density of ~3000 kg/m^3). Hold the trigger longer and
 * you go deeper, in a way that is actually calculable.
 */
export function boreDepth(beam: MiningBeam, seconds: number): number {
  const ROCK_DENSITY = 3000;
  const VAPORISATION = 1.1e7;
  if (!(beam.power > 0) || !(seconds > 0) || !(beam.radius > 0)) return 0;
  const area = Math.PI * beam.radius * beam.radius;
  const energy = beam.power * seconds;
  const massRemoved = energy / VAPORISATION;
  const volume = massRemoved / ROCK_DENSITY;
  return volume / area;
}

/** Seconds of continuous fire needed to bore clean through a body. */
export function timeToPierce(beam: MiningBeam, bodyRadius: number): number {
  const perSecond = boreDepth(beam, 1);
  if (perSecond <= 0) return Infinity;
  return (bodyRadius * 2) / perSecond;
}
