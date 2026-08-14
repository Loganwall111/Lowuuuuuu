/**
 * HoleDescent — the controller that turns crossing a horizon into a fall.
 *
 * A black hole's horizon is a couple of dozen world units across, but its
 * interior is thousands deep. That is not a cheat: it is the one place where
 * "bigger on the inside" is literally true, because the radial direction
 * inside a horizon is timelike — distance inward is something you travel
 * through, not a coordinate you already occupy.
 *
 * So this system holds a separate odometer. Crossing the horizon starts it;
 * every frame it adds however far the player actually moved inward, and the
 * result drives the shader (how closed the exit window is, whether the inner
 * lens has resolved, whether the white dot is visible) and eventually hands
 * back a destination.
 *
 * Two things make it a journey rather than a cutscene:
 *
 *   - You steer. Moving inward advances the fall; turning around and burning
 *     outward actually reduces it, and near the horizon that gets you out.
 *   - You aim. If the hole has a nested singularity, flying through the dot
 *     lands you in the Dust Stream instead of where the fall would otherwise
 *     have ended.
 *
 * Kept free of Babylon so the whole descent can be simulated in a test.
 */

import {
  interiorPlan, fallState, destinationFor, throughSingularity,
  type InteriorPlan, type FallState, type InteriorDestination
} from './HoleInterior';

/** Minimal vector, so this system does not depend on the engine. */
export interface Vec3Like {
  x: number;
  y: number;
  z: number;
}

function sub(a: Vec3Like, b: Vec3Like): Vec3Like {
  return { x: a.x - b.x, y: a.y - b.y, z: a.z - b.z };
}
function len(v: Vec3Like): number {
  return Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
}
function norm(v: Vec3Like): Vec3Like {
  const l = len(v);
  if (!(l > 1e-9)) return { x: 0, y: 0, z: 1 };
  return { x: v.x / l, y: v.y / l, z: v.z / l };
}
function dot(a: Vec3Like, b: Vec3Like): number {
  return a.x * b.x + a.y * b.y + a.z * b.z;
}
function finite(v: Vec3Like | null | undefined): boolean {
  return !!v && Number.isFinite(v.x) && Number.isFinite(v.y) && Number.isFinite(v.z);
}

/**
 * How much the fall is amplified relative to how far the player flies.
 *
 * Without this, crossing thousands of units of interior at ordinary flight
 * speed would take far too long to be fun. With it, a sustained burn inward
 * covers the interior in a reasonable time while still requiring a real,
 * deliberate journey rather than a moment.
 */
export const INFALL_GAIN = 26;

/**
 * Speed the hole drags you inward at, in interior units per second, even if
 * you do nothing. You are inside a black hole; standing still is not an
 * option that physics offers.
 */
export const TIDAL_DRIFT = 140;

/** What the app needs to act on after a frame of falling. */
export interface DescentUpdate {
  /** The live state of the fall, for the shader and the HUD. */
  state: FallState;
  /** Set on the frame the fall completes. Null otherwise. */
  arrived: InteriorDestination | null;
  /** True on the frame the player escapes back out through the horizon. */
  escaped: boolean;
}

export class HoleDescent {
  /** The hole being fallen into, or null when outside. */
  private plan: InteriorPlan | null = null;
  /** Region id of that hole, so re-entering the same one is recognised. */
  private holeId: string | null = null;
  /** Distance fallen past the horizon, in interior units. */
  private fallen = 0;
  /** Where the player was last frame, for measuring real movement. */
  private prev: Vec3Like | null = null;
  /** Direction from the horizon crossing toward the centre. */
  private inward: Vec3Like = { x: 0, y: 0, z: 1 };
  /**
   * World-space centre of the hole.
   *
   * Needed because the miss distance is measured about the hole's own axis.
   * Projecting the raw world position onto the inward direction silently
   * assumed the hole was at the origin, which is true of exactly one hole in
   * the universe and wrong for every other.
   */
  private center: Vec3Like = { x: 0, y: 0, z: 0 };
  /** Set once a destination has been handed out, so it fires exactly once. */
  private delivered = false;

  /** True while the player is inside a horizon. */
  get active(): boolean {
    return this.plan !== null;
  }

  /** The interior being fallen through, for the HUD. Null when outside. */
  get interior(): InteriorPlan | null {
    return this.plan;
  }

  /** How far in, in interior units. */
  get distance(): number {
    return this.fallen;
  }

  /** The current state of the fall, or null when outside a horizon. */
  get state(): FallState | null {
    return this.plan ? fallState(this.plan, this.fallen) : null;
  }

  /**
   * Begins a fall. Safe to call every frame: it only takes effect the first
   * time for a given hole, so the caller does not have to track edges.
   */
  begin(holeId: string, seed: number, at: Vec3Like, center: Vec3Like): void {
    if (this.holeId === holeId && this.plan) return;
    this.holeId = holeId;
    this.plan = interiorPlan(seed);
    this.fallen = 0;
    this.delivered = false;
    this.prev = finite(at) ? { ...at } : null;
    // Inward is fixed at the moment of crossing rather than recomputed each
    // frame: once inside, "toward the centre" stops being a direction you
    // can point away from, and recomputing it made the fall reverse itself
    // whenever the player drifted past the centre point.
    this.inward = finite(at) && finite(center)
      ? norm(sub(center, at))
      : { x: 0, y: 0, z: 1 };
    this.center = finite(center) ? { ...center } : { x: 0, y: 0, z: 0 };
  }

  /** Ends the fall and forgets the hole. Called on escape or on arrival. */
  end(): void {
    this.plan = null;
    this.holeId = null;
    this.fallen = 0;
    this.prev = null;
    this.delivered = false;
  }

  /**
   * Advances the fall by one frame.
   *
   * `pos` is the player's world position. Movement along the inward
   * direction advances the descent; movement back out reduces it. The hole
   * also drags you in on its own, so doing nothing still means falling.
   */
  update(dt: number, pos: Vec3Like): DescentUpdate {
    const plan = this.plan;
    if (!plan) {
      return { state: OUTSIDE, arrived: null, escaped: false };
    }
    const step = Number.isFinite(dt) ? Math.max(0, Math.min(0.25, dt)) : 0;

    if (finite(pos)) {
      if (this.prev) {
        // Only motion along the inward axis counts. Flying sideways inside a
        // horizon gets you nowhere, which is correct and also stops a player
        // orbiting to avoid the ending.
        const moved = sub(pos, this.prev);
        const along = dot(moved, this.inward);
        if (Number.isFinite(along)) this.fallen += along * INFALL_GAIN;
      }
      this.prev = { ...pos };
    }

    // Spacetime does the rest.
    this.fallen += TIDAL_DRIFT * step;

    // Burning hard enough outward near the entrance gets you back out. This
    // is only possible in the first stretch; past that the exit window has
    // closed and the fall is one-way, which is the whole point of a horizon.
    if (this.fallen < 0) {
      this.end();
      return { state: OUTSIDE, arrived: null, escaped: true };
    }

    const state = fallState(plan, this.fallen);

    if (state.complete && !this.delivered) {
      this.delivered = true;
      // Whether the player threaded the singularity is decided at the moment
      // of arrival, using how far off the fall axis they finished.
      const off = this.offAxis(pos);
      const through = throughSingularity(plan, state, off);
      return { state, arrived: destinationFor(plan, through), escaped: false };
    }

    return { state, arrived: null, escaped: false };
  }

  /**
   * How far the player is from the axis of the fall.
   *
   * Measured perpendicular to the inward direction from where they entered,
   * which is what decides whether they hit the singularity or sail past it.
   */
  private offAxis(pos: Vec3Like): number {
    if (!finite(pos)) return 0;
    // The axis runs through the hole's centre along `inward`. Measure the
    // offset from the centre, then strip the component along the axis; what
    // is left is the perpendicular miss distance.
    const rel = sub(pos, this.center);
    const along = dot(rel, this.inward);
    const proj = {
      x: this.inward.x * along,
      y: this.inward.y * along,
      z: this.inward.z * along
    };
    const off = len(sub(rel, proj));
    return Number.isFinite(off) ? off : 0;
  }

  /**
   * Everything the black hole shader needs this frame.
   *
   * Returned as plain numbers so the world can be driven without it knowing
   * anything about holes, plans or destinations.
   */
  shaderState(): {
    inside: number; exitWindow: number; nestedLens: number;
    singularity: number; darkness: number;
  } {
    const st = this.state;
    if (!st || !this.plan) {
      return { inside: 0, exitWindow: 1, nestedLens: 0, singularity: 0, darkness: 0 };
    }
    // Gargantua blacks out over the last stretch, so the Library arrives out
    // of nothing rather than out of a starfield.
    const darkness = this.plan.gargantua
      ? Math.max(0, Math.min(1, (st.progress - 0.72) / 0.28))
      : 0;
    return {
      inside: st.inside,
      exitWindow: st.exitWindow,
      nestedLens: st.nestedLens,
      singularity: st.singularity,
      darkness
    };
  }
}

/** The state reported when no fall is in progress. */
const OUTSIDE: FallState = {
  phase: 'outside', progress: 0, inside: 0, exitWindow: 1,
  nestedLens: 0, singularity: 0, remaining: 0, complete: false
};
