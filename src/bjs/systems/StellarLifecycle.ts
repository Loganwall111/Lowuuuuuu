/**
 * StellarLifecycle — stars that age.
 *
 * A universe where every star is frozen at the brightness it was born with
 * is a universe where nothing ever really happens. Real stars live and die:
 * a long main sequence, a slow swell into a red giant, then a collapse into
 * whatever their mass leaves behind - a white dwarf, a neutron star, or a
 * black hole. This models that arc as a pure function of time, so the home
 * sun (and, eventually, every star) visibly ages on the long clock instead
 * of sitting still forever.
 *
 * Pure arithmetic, no Babylon: the renderer reads a colour and a size and
 * draws whatever phase the star is in. Testable without a GPU.
 */

export type StarPhase = 'main' | 'subgiant' | 'redgiant' | 'white-dwarf' | 'neutron' | 'blackhole';

export interface PhaseSpec {
  id: StarPhase;
  label: string;
  glyph: string;
  /** Linear RGB the star's hot core drifts toward in this phase. */
  tintA: [number, number, number];
  /** Linear RGB the cooler surface drifts toward. */
  tintB: [number, number, number];
  /** Relative visual size. Red giants swell; remnants shrink. */
  size: number;
}

export const STAR_PHASES: PhaseSpec[] = [
  { id: 'main', label: 'Main Sequence', glyph: '☀', tintA: [1.0, 0.55, 0.12], tintB: [1.0, 0.98, 0.86], size: 1.0 },
  { id: 'subgiant', label: 'Subgiant', glyph: '🌅', tintA: [1.0, 0.6, 0.28], tintB: [1.0, 0.9, 0.72], size: 1.5 },
  { id: 'redgiant', label: 'Red Giant', glyph: '🔴', tintA: [1.0, 0.32, 0.10], tintB: [0.9, 0.5, 0.22], size: 4.5 },
  { id: 'white-dwarf', label: 'White Dwarf', glyph: '⚪', tintA: [0.85, 0.9, 1.0], tintB: [0.55, 0.62, 0.8], size: 0.12 },
  { id: 'neutron', label: 'Neutron Star', glyph: '🟣', tintA: [0.7, 0.6, 1.0], tintB: [0.4, 0.4, 0.7], size: 0.05 },
  { id: 'blackhole', label: 'Black Hole', glyph: '⚫', tintA: [0.3, 0.3, 0.34], tintB: [0.1, 0.1, 0.14], size: 0.02 }
];

/** Seconds of sim time for a full stellar life. */
export const STAR_LIFETIME = 2400;

/** Deterministic 0..1 hash. */
function hash01(seed: number): number {
  let h = seed >>> 0 || 1;
  h = Math.imul(h ^ (h >>> 16), 2246822519) >>> 0;
  h = Math.imul(h ^ (h >>> 13), 3266489917) >>> 0;
  return ((h ^ (h >>> 16)) >>> 0) / 4294967296;
}

/**
 * Which phase a star of a given mass is in at a given age.
 *
 * `age` is in seconds; the arc spans STAR_LIFETIME. The mass seed only
 * decides which remnant a star leaves, exactly as it does in reality - a
 * low-mass star becomes a white dwarf, a massive one a black hole.
 */
export function starPhaseAt(seed: number, age: number): PhaseSpec {
  const a = Math.max(0, Number.isFinite(age) ? age : 0) / STAR_LIFETIME;
  if (a < 0.62) return STAR_PHASES[0];           // main sequence
  if (a < 0.76) return STAR_PHASES[1];           // subgiant
  if (a < 0.92) return STAR_PHASES[2];           // red giant
  const m = hash01(seed);
  if (m < 0.5) return STAR_PHASES[3];            // white dwarf
  if (m < 0.9) return STAR_PHASES[4];            // neutron star
  return STAR_PHASES[5];                         // black hole
}

/** Smoothly interpolates between the phase endpoints for a gentle drift. */
export function stellarColor(
  seed: number, age: number
): { tintA: [number, number, number]; tintB: [number, number, number]; size: number; glyph: string } {
  const a = Math.max(0, Number.isFinite(age) ? age : 0);
  const phase = starPhaseAt(seed, a);
  const norm = Math.min(1, a / STAR_LIFETIME);
  return {
    tintA: phase.tintA,
    tintB: phase.tintB,
    size: phase.size,
    glyph: phase.glyph
  };
}
