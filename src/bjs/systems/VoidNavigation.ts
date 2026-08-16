/**
 * VoidNavigation — getting stranded in the multiverse, and charting a way out.
 *
 * Crossing a horizon is not always a clean trip. Sometimes the fall does not
 * thread you into the realm you were headed for: it dumps you in an
 * uncharted alternate universe, and the only way back to home space is to
 * navigate the procedural wormholes that thread that place. This is the
 * pure probability-and-arithmetic half of that - the app owns the warp and
 * the wormholes.
 *
 * Deliberately timer-free: nothing here counts seconds. Stranding is a
 * deterministic roll over the hole's seed and the warp factor at the moment
 * of crossing, so the same hole at the same speed always strands the same
 * way, and the void itself is driven purely by coordinates (the existing
 * interior distance), never by a clock.
 */

/** The neon warning shown once you are inside the horizon. */
export const HORIZON_WARNING =
  'WARNING: HORIZON CROSSED. EVENT HORIZON BOUNDS STABLE. ' +
  'MULTIVERSE ISOLATION IMPACT IMMINENT.';

/** Deterministic 0..1 hash. */
function hash01(seed: number): number {
  let h = seed >>> 0 || 1;
  h = Math.imul(h ^ (h >>> 16), 2246822519) >>> 0;
  h = Math.imul(h ^ (h >>> 13), 3266489917) >>> 0;
  return ((h ^ (h >>> 16)) >>> 0) / 4294967296;
}

/**
 * How likely a crossing strands you, for a given warp factor.
 *
 * The faster you enter, the more violently spacetime tears, so higher warp
 * means a higher chance of being thrown somewhere uncharted. The base sits
 * low enough that an ordinary, careful entry is usually a clean one.
 */
export function strandingChance(warpFactor: number): number {
  const w = Number.isFinite(warpFactor) ? Math.max(1, warpFactor) : 1;
  return Math.min(0.55, 0.10 + Math.log10(w) * 0.06);
}

/**
 * The deterministic stranding roll for one crossing.
 *
 * True when the ship ends up stranded in an uncharted universe instead of
 * arriving cleanly. Same hole, same speed, same result - forever.
 */
export function shouldStrand(seed: number, warpFactor: number): boolean {
  const roll = hash01((seed >>> 0) ^ 0x5eed);
  return roll < strandingChance(warpFactor);
}

/**
 * How deep into the stranger end of the dimension scale a stranding throws
 * you. Deeper = more chaotic, so being stranded is genuinely disorienting.
 */
export function strandedDepth(seed: number): number {
  return 7 + Math.floor(hash01((seed >>> 0) ^ 0xd157) * 3);
}

/**
 * The wormholes you must navigate home are seeded from the stranding, so the
 * same stranding always offers the same way out.
 */
export function strandedWormholeSeed(seed: number): number {
  return ((seed >>> 0) ^ 0x14071) >>> 0;
}
