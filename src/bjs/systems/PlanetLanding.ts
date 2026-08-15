/**
 * PlanetLanding — rigid-body collision and gravity anchoring for the player.
 *
 * The flight controller was free to move through anything: free-fly and ship
 * flight integrated position directly from input, with no idea that a planet
 * was in the way, so a player could fly straight through a world and out the
 * far side. This module is the solid half of the fix - pure arithmetic, no
 * Babylon, no meshes - so it can be tested exactly and reused by whichever
 * world exposes solid bodies.
 *
 * A "solid sphere" is a position plus a surface radius. Collision is a
 * penetration push-out along the outward normal plus removal of any inward
 * velocity component, which is what turns "clip through the planet" into
 * "stop on the surface". Anchoring is the same sphere described as a ground:
 * a surface point and an outward normal that walk mode can stand on and be
 * pulled toward by gravity, so landing is seamless rather than a teleport.
 *
 * Everything here is a pure function of numbers. The caller (App) owns the
 * Babylon vectors and feeds this their components.
 */

/** A body the player can collide with and stand on. */
export interface SolidSphere {
  id: string;
  x: number;
  y: number;
  z: number;
  /** Surface radius, world units. */
  radius: number;
  /** Mass, in the same simulation units as the rest of the universe. */
  mass: number;
  /** Habitable worlds get gentler, more welcoming landing behaviour. */
  habitable?: boolean;
}

/** A resolved contact: which sphere, which direction it pushed, how deep. */
export interface Contact {
  id: string;
  /** Outward unit normal at the contact point. */
  nx: number;
  ny: number;
  nz: number;
  /** Penetration depth that was resolved, world units. */
  depth: number;
}

/** Result of resolving one frame of collisions. */
export interface Resolved {
  x: number;
  y: number;
  z: number;
  vx: number;
  vy: number;
  vz: number;
  contacts: Contact[];
}

/** The nearest sphere by surface distance (distance minus radius). */
export function nearestSolid(
  spheres: readonly SolidSphere[], x: number, y: number, z: number
): SolidSphere | null {
  let best: SolidSphere | null = null;
  let bestD = Infinity;
  for (const s of spheres) {
    if (!(s.radius > 0)) continue;
    const d = Math.hypot(x - s.x, y - s.y, z - s.z) - s.radius;
    if (d < bestD) { bestD = d; best = s; }
  }
  return best;
}

/**
 * The point on a sphere's surface nearest a probe position, and the outward
 * unit normal there. Used as the ground under a walker.
 */
export function surfaceProbe(
  s: SolidSphere, x: number, y: number, z: number
): { px: number; py: number; pz: number; nx: number; ny: number; nz: number } {
  const dx = x - s.x, dy = y - s.y, dz = z - s.z;
  const d = Math.hypot(dx, dy, dz);
  if (!(d > 1e-6)) {
    // Degenerate: the probe sits at the planet's core. Pick any sane axis.
    return {
      px: s.x, py: s.y + s.radius, pz: s.z,
      nx: 0, ny: 1, nz: 0
    };
  }
  const nx = dx / d, ny = dy / d, nz = dz / d;
  return {
    px: s.x + nx * s.radius,
    py: s.y + ny * s.radius,
    pz: s.z + nz * s.radius,
    nx, ny, nz
  };
}

/**
 * Resolves penetration against every solid sphere, and cancels any inward
 * velocity so the player stops on the surface instead of tunnelling through.
 *
 * Runs a few passes so a body squeezed between two spheres ends up outside
 * both rather than oscillating. Returns a fresh position/velocity; the
 * inputs are never mutated.
 */
export function resolveCollisions(
  spheres: readonly SolidSphere[],
  px: number, py: number, pz: number,
  vx: number, vy: number, vz: number,
  margin = 0.25,
  passes = 3
): Resolved {
  let x = px, y = py, z = pz;
  let ox = vx, oy = vy, oz = vz;
  const contacts: Contact[] = [];

  for (let p = 0; p < passes; p++) {
    let moved = false;
    for (const s of spheres) {
      if (!(s.radius > 0)) continue;
      const dx = x - s.x, dy = y - s.y, dz = z - s.z;
      const d = Math.hypot(dx, dy, dz);
      const min = s.radius + margin;
      if (d >= min) continue;

      let nx = 0, ny = 1, nz = 0;
      if (d > 1e-6) {
        nx = dx / d; ny = dy / d; nz = dz / d;
        x = s.x + nx * min;
        y = s.y + ny * min;
        z = s.z + nz * min;
      } else {
        // Exactly at the centre: there is no meaningful normal. Push upward.
        x = s.x; y = s.y + min; z = s.z;
        nx = 0; ny = 1; nz = 0;
      }

      // Kill only the component of velocity driving the player INTO the
      // surface; tangential motion (sliding along it) is preserved.
      const vn = ox * nx + oy * ny + oz * nz;
      if (vn < 0) {
        ox -= vn * nx; oy -= vn * ny; oz -= vn * nz;
      }
      contacts.push({ id: s.id, nx, ny, nz, depth: min - d });
      moved = true;
    }
    if (!moved) break;
  }

  return { x, y, z, vx: ox, vy: oy, vz: oz, contacts };
}

/**
 * Gravitational acceleration toward a single body, softened near its core so
 * the pull cannot explode into NaN, and capped so a small dense body does not
 * yank the player out of the universe.
 */
export function gravityAccel(
  s: SolidSphere, x: number, y: number, z: number, G = 42
): { gx: number; gy: number; gz: number } {
  const dx = s.x - x, dy = s.y - y, dz = s.z - z;
  const d2 = dx * dx + dy * dy + dz * dz;
  if (!(d2 > 1e-6)) return { gx: 0, gy: 0, gz: 0 };
  const d = Math.sqrt(d2);
  const soft = Math.max(s.radius * 0.05, 2);
  let g = (G * Math.max(s.mass, 0)) / (d2 + soft * soft);
  if (g > 60) g = 60;   // the anchor should feel firm, never violent
  return { gx: (dx / d) * g, gy: (dy / d) * g, gz: (dz / d) * g };
}

/** The solid sphere directly under a probe, if it is within `maxAlt`. */
export function bodyUnderneath(
  spheres: readonly SolidSphere[], x: number, y: number, z: number,
  maxAlt: number
): SolidSphere | null {
  const s = nearestSolid(spheres, x, y, z);
  if (!s) return null;
  const alt = Math.hypot(x - s.x, y - s.y, z - s.z) - s.radius;
  return alt <= maxAlt ? s : null;
}

/**
 * A ground-probe answer for walking on a sphere: the surface point and its
 * outward normal, returned in the same shape a flat-ground probe uses (a
 * `height` plus a `normal` plus an exact `point`), so one walk integrator can
 * stand on a station deck, a garage floor or a planet without special cases.
 */
export function planetGround(
  spheres: readonly SolidSphere[],
  x: number, y: number, z: number,
  maxAlt?: number
): {
  height: number;
  nx: number; ny: number; nz: number;
  px: number; py: number; pz: number;
} | null {
  const s = nearestSolid(spheres, x, y, z);
  if (!s) return null;
  const alt = Math.hypot(x - s.x, y - s.y, z - s.z) - s.radius;
  const limit = maxAlt ?? Math.max(s.radius * 3, 8);
  if (alt > limit) return null;
  const probe = surfaceProbe(s, x, y, z);
  return {
    height: probe.py,
    nx: probe.nx, ny: probe.ny, nz: probe.nz,
    px: probe.px, py: probe.py, pz: probe.pz
  };
}
