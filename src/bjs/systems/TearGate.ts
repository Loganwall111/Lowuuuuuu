/**
 * TearGate — flying through a rift, rather than clicking a button labelled
 * "go deeper".
 *
 * Every dimension hangs a few tears in its sky: rings of light that lead one
 * level further down. They existed before this module, but they were scenery
 * — the only way to actually descend was a button in a side panel, which
 * makes an infinite stack of realities feel like a menu rather than a place.
 *
 * This turns them into doors. A tear is a ring with a position, a radius and
 * a facing. You go through it by physically passing through the disc it
 * encloses: the segment you travelled this frame has to cross the tear's
 * plane, and the crossing point has to land inside the ring. Skimming past
 * the edge does nothing. Flying at the back of it works exactly as well as
 * the front, because a hole in reality does not have a preferred side.
 *
 * Detecting the crossing against the *segment* rather than the current
 * position is what makes it reliable at speed. At warp a frame can advance
 * thousands of units, so a proximity test would simply never fire — the
 * player would be in front of the tear on one frame and far behind it on the
 * next, having "missed" a ring they flew straight through.
 *
 * No Babylon here: it is geometry over plain numbers, so every case below is
 * asserted directly in tools/teargate-check.mjs.
 */

/** Minimal vector, so this system does not depend on the engine. */
export interface Vec3Like {
  x: number;
  y: number;
  z: number;
}

/** One rift you can fall through. */
export interface Tear {
  id: string;
  /** Centre of the ring, world space. */
  position: Vec3Like;
  /** Radius of the opening, world units. */
  radius: number;
  /** Unit normal of the ring's plane. */
  normal: Vec3Like;
}

/** A tear that was passed through this frame. */
export interface TearCrossing {
  id: string;
  /** How far from the centre of the ring you passed, 0 = dead centre. */
  offset: number;
  /** 0..1 along the frame's movement segment where the crossing happened. */
  t: number;
}

function sub(a: Vec3Like, b: Vec3Like): Vec3Like {
  return { x: a.x - b.x, y: a.y - b.y, z: a.z - b.z };
}
function dot(a: Vec3Like, b: Vec3Like): number {
  return a.x * b.x + a.y * b.y + a.z * b.z;
}
function len(v: Vec3Like): number {
  return Math.sqrt(dot(v, v));
}
function finite(v: Vec3Like | null | undefined): boolean {
  return !!v && Number.isFinite(v.x) && Number.isFinite(v.y) && Number.isFinite(v.z);
}

/**
 * Did the movement from `from` to `to` pass through this tear?
 *
 * Returns the crossing, or null. The test is a segment-plane intersection
 * followed by a radius check, so it is exact at any speed and cannot be
 * tunnelled through.
 */
export function crossesTear(
  tear: Tear, from: Vec3Like, to: Vec3Like
): TearCrossing | null {
  if (!tear || !finite(from) || !finite(to)) return null;
  if (!finite(tear.position) || !finite(tear.normal)) return null;
  if (!Number.isFinite(tear.radius) || tear.radius <= 0) return null;

  const n = tear.normal;
  const nl = len(n);
  if (!(nl > 1e-9)) return null;
  const nx = n.x / nl, ny = n.y / nl, nz = n.z / nl;
  const un = { x: nx, y: ny, z: nz };

  // Signed distance from each endpoint to the tear's plane.
  const d0 = dot(sub(from, tear.position), un);
  const d1 = dot(sub(to, tear.position), un);
  if (!Number.isFinite(d0) || !Number.isFinite(d1)) return null;

  // Both endpoints on the same side means no crossing. Touching the plane
  // exactly (d === 0) counts, so a tear you stop dead inside still fires.
  if (d0 > 0 && d1 > 0) return null;
  if (d0 < 0 && d1 < 0) return null;
  // Moving entirely within the plane: no crossing to speak of.
  if (d0 === 0 && d1 === 0) return null;

  const denom = d0 - d1;
  if (Math.abs(denom) < 1e-12) return null;
  const t = d0 / denom;
  if (!Number.isFinite(t) || t < 0 || t > 1) return null;

  // Where the path met the plane.
  const hit = {
    x: from.x + (to.x - from.x) * t,
    y: from.y + (to.y - from.y) * t,
    z: from.z + (to.z - from.z) * t
  };
  const offset = len(sub(hit, tear.position));
  if (!Number.isFinite(offset) || offset > tear.radius) return null;

  return { id: tear.id, offset, t };
}

/**
 * Watches a set of tears and reports the first one flown through.
 *
 * Stateful only in that it remembers where the player was last frame, which
 * is what makes the segment test possible. `arm()` is called when a set of
 * tears is built, and resets that memory so a world rebuild cannot report a
 * bogus crossing from the old position to the new one.
 */
export class TearGate {
  private tears: Tear[] = [];
  private prev: Vec3Like | null = null;
  /**
   * Frames to ignore after a crossing.
   *
   * Arriving in a new dimension places you near its centre, and its tears
   * are rebuilt around you; without a short cooldown a single flight could
   * chain several descents in a handful of frames.
   */
  private cooldown = 0;

  /** How long to ignore crossings after one fires, in seconds. */
  static readonly COOLDOWN = 1.2;

  /** Replaces the tear set and forgets the previous position. */
  arm(tears: Tear[]): void {
    this.tears = Array.isArray(tears) ? tears.filter(Boolean) : [];
    this.prev = null;
    this.cooldown = TearGate.COOLDOWN;
  }

  /** How many tears are being watched. */
  get count(): number {
    return this.tears.length;
  }

  /** True while crossings are being ignored. */
  get armed(): boolean {
    return this.cooldown <= 0;
  }

  /** Forgets everything. */
  clear(): void {
    this.tears = [];
    this.prev = null;
    this.cooldown = 0;
  }

  /**
   * Advances one frame. Returns the tear flown through, or null.
   *
   * The first frame after arm() can never report a crossing, because there
   * is no previous position to draw a segment from.
   */
  update(dt: number, pos: Vec3Like): TearCrossing | null {
    const step = Number.isFinite(dt) ? Math.max(0, Math.min(0.25, dt)) : 0;
    if (this.cooldown > 0) this.cooldown = Math.max(0, this.cooldown - step);

    if (!finite(pos)) return null;
    const from = this.prev;
    this.prev = { x: pos.x, y: pos.y, z: pos.z };
    if (!from) return null;
    if (this.cooldown > 0) return null;

    for (const tear of this.tears) {
      const hit = crossesTear(tear, from, pos);
      if (hit) {
        // One descent per crossing.
        this.cooldown = TearGate.COOLDOWN;
        return hit;
      }
    }
    return null;
  }
}
