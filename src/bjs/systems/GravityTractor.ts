/**
 * GravityTractor — steering a comet with your ship's own gravity.
 *
 * The ship has mass, so it has gravity. Park beside a comet and hold on: the
 * mutual pull slowly bends the comet's path. This is the pure half of comet
 * redirection - the inverse-square pull between ship and comet, softened near
 * contact so it cannot blow up, and the deflection that pull accumulates
 * into. The renderer owns the comet; this owns the numbers, so the physics
 * can be tested without a GPU.
 */

export interface Body {
  mass: number;
  x: number;
  y: number;
  z: number;
}

/**
 * Gravitational acceleration the ship exerts on the comet.
 *
 * Inverse-square, softened inside a few units so a collision with the
 * singularity of the field cannot produce NaN or a wild launch.
 */
export function tractorAccel(ship: Body, comet: Body, G = 42): { ax: number; ay: number; az: number } {
  const dx = ship.x - comet.x, dy = ship.y - comet.y, dz = ship.z - comet.z;
  const d2 = dx * dx + dy * dy + dz * dz;
  if (!(d2 > 1e-6)) return { ax: 0, ay: 0, az: 0 };
  const d = Math.sqrt(d2);
  const soft = 3;
  let g = (G * Math.max(ship.mass, 0)) / (d2 + soft * soft);
  if (g > 30) g = 30;
  return { ax: (dx / d) * g, ay: (dy / d) * g, az: (dz / d) * g };
}

/**
 * How hard the tractor is pulling, 0..1.
 *
 * 1 when the ship is right beside the comet, falling off with distance.
 * Used to scale the deflection and to drive the "tractor engaged" readout.
 */
export function tractorStrength(ship: Body, comet: Body, reach = 200): number {
  const d = Math.hypot(ship.x - comet.x, ship.y - comet.y, ship.z - comet.z);
  const t = 1 - Math.min(1, d / reach);
  return Math.max(0, t);
}

/**
 * The accumulated deflection (in radians of orbital phase) from holding the
 * tractor for `dt` seconds at a given pull strength.
 */
export function deflectFrom(strength: number, dt: number): number {
  if (!Number.isFinite(strength) || !Number.isFinite(dt) || dt <= 0) return 0;
  // A few minutes of solid towing bends the orbit by a visible amount.
  return Math.max(0, Math.min(1, strength)) * dt * 0.004;
}
