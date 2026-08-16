/**
 * WorldDynamics — the live character a planet wears beyond its terrain.
 *
 * Three separate dynamics that make a world read as a specific *place*
 * rather than a heightfield with a climate label:
 *
 *   - Tidal locking. Close-in worlds orbit with one face forever toward
 *     their star: a scorched day side, a frozen night side, and a habitable
 *     twilight band between them.
 *   - Subsurface oceans. Some ice worlds hide a liquid ocean under their
 *     shell, which is where the interesting life is.
 *   - Weather. Each climate produces its own weather over time - dust
 *     storms on arid worlds, blizzards on frozen ones, rain bands on
 *     ocean and temperate worlds.
 *
 * All of it is pure, deterministic arithmetic from a seed, so the same
 * world wears the same character on every visit.
 */

export type WeatherKind = 'clear' | 'rain' | 'dust' | 'blizzard';

export interface WeatherState {
  kind: WeatherKind;
  /** 0..1 how hard the weather is right now. */
  intensity: number;
  /** 0..1 how much it cuts visibility (fog). */
  visibility: number;
  /** Wind speed, world units / sec, signed. */
  wind: number;
}

/** Deterministic 0..1 hash. */
function hash01(seed: number): number {
  let h = seed >>> 0 || 1;
  h = Math.imul(h ^ (h >>> 16), 2246822519) >>> 0;
  h = Math.imul(h ^ (h >>> 13), 3266489917) >>> 0;
  return ((h ^ (h >>> 16)) >>> 0) / 4294967296;
}

/**
 * Whether a world is tidally locked.
 *
 * Locking is a function of how close the world orbits: the closer, the
 * stronger the tidal drag and the faster the spin is synchronised. Kept
 * deterministic from the seed so a given world is always locked (or not).
 */
export function tidalLocked(seed: number, orbitRadius: number): boolean {
  const r = Math.max(1, orbitRadius);
  const threshold = 30 + hash01(seed + 9001) * 60;
  return r < threshold;
}

/** The sub-stellar point (u, v) of a locked world, the spot that faces the star. */
export function subStellarPoint(seed: number): [number, number] {
  return [hash01(seed + 17), 0.5 + (hash01(seed + 23) - 0.5) * 0.4];
}

/**
 * How much daylight a grid cell receives on a locked world.
 *
 * 1 at the sub-stellar point, falling to 0 on the far side. The gradient
 * between them is the habitable twilight band.
 */
export function daylightAt(u: number, v: number, sub: [number, number]): number {
  const du = Math.min(Math.abs(u - sub[0]), 1 - Math.abs(u - sub[0]));
  const dv = v - sub[1];
  const ang = Math.sqrt(du * du + dv * dv);
  return Math.max(0, Math.min(1, Math.cos(ang * Math.PI)));
}

/** Whether a frozen world hides a liquid ocean beneath its ice. */
export function subsurfaceOcean(seed: number, climate: string): { present: boolean; depth: number } {
  if (climate !== 'frozen') return { present: false, depth: 0 };
  const roll = hash01(seed + 7919);
  if (roll < 0.45) return { present: false, depth: 0 };
  return { present: true, depth: 8 + hash01(seed + 104729) * 60 };
}

/** The weather a climate produces at time t, deterministic per seed. */
export function weatherFor(seed: number, climate: string, t: number): WeatherState {
  const time = Math.max(0, t);
  // A slow storm "cell" that wanders: one low-frequency noise channel per
  // climate, so storms arrive and clear on their own schedule.
  const a = hash01(seed + Math.floor(time * 0.05));
  const b = hash01(seed + 991 + Math.floor(time * 0.05) * 7);

  const storm = a * (0.7 + 0.3 * Math.sin(time * 0.4 + seed));
  switch (climate) {
    case 'arid':
      return dust(storm);
    case 'frozen':
      return { kind: storm > 0.62 ? 'blizzard' : 'clear', intensity: storm, visibility: 1 - storm * 0.7, wind: (b - 0.5) * 8 };
    case 'ocean':
    case 'temperate':
      return { kind: storm > 0.5 ? 'rain' : 'clear', intensity: storm, visibility: 1 - storm * 0.4, wind: (b - 0.5) * 6 };
    case 'volcanic':
      return { kind: storm > 0.7 ? 'dust' : 'clear', intensity: storm, visibility: 1 - storm * 0.6, wind: (b - 0.5) * 5 };
    default:
      return { kind: 'clear', intensity: 0, visibility: 1, wind: 0 };
  }
}

/** Dust storm builder: harsh, long-lived, half the sky gone. */
function dust(storm: number): WeatherState {
  if (storm < 0.55) return { kind: 'clear', intensity: 0, visibility: 1, wind: 0 };
  const i = (storm - 0.55) / 0.45;
  return { kind: 'dust', intensity: i, visibility: 1 - i * 0.85, wind: 4 + i * 14 };
}
