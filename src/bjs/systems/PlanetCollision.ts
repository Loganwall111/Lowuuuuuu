/**
 * PlanetCollision — two worlds becoming one.
 *
 * Dragging a planet onto another should not leave them clipped inside each
 * other like a rendering bug: it should be an *event*. This is the pure
 * arithmetic behind that event - detecting when two solid worlds overlap,
 * and merging them with mass and volume conserved, exactly the way the
 * physics-check suite already verifies inelastic merges. The app owns the
 * theatre (the bloom flash, the toast, the codex entry); this owns the maths.
 */

export interface SolidWorld {
  id: string;
  name: string;
  x: number;
  y: number;
  z: number;
  radius: number;
  mass: number;
}

export interface Overlap {
  a: SolidWorld;
  b: SolidWorld;
  /** How deep they interpenetrate, world units. */
  depth: number;
}

/** The merged result of two overlapping worlds. */
export interface MergeResult {
  name: string;
  radius: number;
  mass: number;
}

/**
 * Finds the deepest overlap between any pair of solid worlds.
 *
 * Returns null when every pair is clear. O(n²) over the worlds present,
 * which for a universe of a few hundred is negligible and only runs when a
 * world has actually been dragged somewhere.
 */
export function findDeepestOverlap(worlds: SolidWorld[], margin = 0): Overlap | null {
  let best: Overlap | null = null;
  for (let i = 0; i < worlds.length; i++) {
    for (let j = i + 1; j < worlds.length; j++) {
      const a = worlds[i], b = worlds[j];
      if (!(a.radius > 0) || !(b.radius > 0)) continue;
      const d = Math.hypot(a.x - b.x, a.y - b.y, a.z - b.z);
      const depth = a.radius + b.radius + margin - d;
      if (depth > 0 && (!best || depth > best.depth)) {
        best = { a, b, depth };
      }
    }
  }
  return best;
}

/**
 * Merges two worlds into one.
 *
 * Mass is summed; radius conserves volume (cube-root of the sum of cubes),
 * so a world that swallows another genuinely grows rather than merely
 * renaming itself. Returns the merged body, with the larger world's identity.
 */
export function mergeResult(a: SolidWorld, b: SolidWorld): MergeResult {
  const r = Math.cbrt(a.radius ** 3 + b.radius ** 3);
  return {
    name: a.radius >= b.radius ? a.name : b.name,
    radius: r,
    mass: a.mass + b.mass
  };
}
