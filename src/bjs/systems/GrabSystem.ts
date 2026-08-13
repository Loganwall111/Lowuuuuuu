/**
 * GrabSystem — pick things up and move them around the universe.
 *
 * Works on anything with a position: black holes, stars, planets, portals.
 * The grabbed object is held at a fixed distance in front of the camera and
 * follows it, so dragging a supermassive black hole through a solar system
 * is a matter of looking somewhere else. Releasing can either drop it dead
 * or throw it with the velocity it was moving at.
 *
 * This is deliberately separate from any one world: it operates on a
 * Grabbable interface, so the same code moves a region, a body or a portal.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';

export interface Grabbable {
  id: string;
  name: string;
  position: Vector3;
  /** Optional: grabbing something with velocity should zero it while held. */
  velocity?: Vector3;
  /** Used to decide what the cursor is over. */
  radius: number;
}

export interface GrabTarget extends Grabbable {
  /** Distance from the camera when picked, so it keeps its depth. */
  holdDistance?: number;
}

export class GrabSystem {
  held: GrabTarget | null = null;
  private holdDist = 0;
  private lastPos = new Vector3(0, 0, 0);
  /** Velocity the held object is being moved at, for throwing. */
  private carryVel = new Vector3(0, 0, 0);
  /** Multiplier applied to the carry velocity when thrown. */
  throwStrength = 1.0;
  /** How far the cursor ray can reach. */
  reach = 1e9;

  grabs = 0;
  throws = 0;

  /**
   * Finds the best object under a ray. Uses angular size rather than raw
   * distance, so a distant supermassive hole is still easy to grab while a
   * nearby pebble does not steal the pick.
   */
  pick(origin: Vector3, direction: Vector3, candidates: Grabbable[]): Grabbable | null {
    const dir = direction.clone();
    const dl = dir.length();
    if (dl < 1e-9) return null;
    dir.scaleInPlace(1 / dl);

    let best: Grabbable | null = null;
    let bestScore = -Infinity;

    for (const c of candidates) {
      const to = c.position.subtract(origin);
      const along = Vector3.Dot(to, dir);
      if (along <= 0 || along > this.reach) continue;      // behind, or too far

      const closest = origin.add(dir.scale(along));
      const miss = Vector3.Distance(closest, c.position);
      const r = Math.max(c.radius, 1e-4);
      if (miss > r * 1.6) continue;                        // ray misses it

      // prefer things the ray passes closest to, relative to their size,
      // and break ties by proximity
      const score = (1 - miss / (r * 1.6)) * 1000 - along * 1e-6;
      if (score > bestScore) { bestScore = score; best = c; }
    }
    return best;
  }

  /** Picks the object under the ray and holds it. */
  grab(origin: Vector3, direction: Vector3, candidates: Grabbable[]): Grabbable | null {
    const target = this.pick(origin, direction, candidates);
    if (!target) return null;
    this.grabAt(target, origin);
    return target;
  }

  /** Holds a specific object. */
  grabAt(target: GrabTarget, origin: Vector3): void {
    this.held = target;
    this.holdDist = Math.max(target.radius * 1.5, Vector3.Distance(origin, target.position));
    this.lastPos.copyFrom(target.position);
    this.carryVel.setAll(0);
    if (target.velocity) target.velocity.setAll(0);
    this.grabs++;
  }

  /** Moves the held object to stay in front of the camera. */
  update(dt: number, origin: Vector3, direction: Vector3): void {
    if (!this.held) return;
    if (!Number.isFinite(dt) || dt <= 0) return;

    const dir = direction.clone();
    const dl = dir.length();
    if (dl < 1e-9) return;
    dir.scaleInPlace(1 / dl);

    const want = origin.add(dir.scale(this.holdDist));
    if (![want.x, want.y, want.z].every(Number.isFinite)) return;

    // track how fast we are carrying it, so a throw feels right
    this.carryVel.copyFrom(want.subtract(this.lastPos).scale(1 / dt));
    this.lastPos.copyFrom(want);

    this.held.position.copyFrom(want);
    // held objects do not drift under their own momentum
    if (this.held.velocity) this.held.velocity.setAll(0);
  }

  /** Pushes the held object further away or pulls it closer. */
  adjustDistance(delta: number): void {
    if (!this.held) return;
    const scale = Math.max(1, this.holdDist * 0.15);
    this.holdDist = Math.max(this.held.radius * 1.2, this.holdDist + delta * scale);
  }

  /** Lets go, leaving the object where it is. */
  release(): Grabbable | null {
    const h = this.held;
    if (h && h.velocity) h.velocity.setAll(0);
    this.held = null;
    this.carryVel.setAll(0);
    return h;
  }

  /** Lets go, imparting the velocity it was being carried at. */
  throwIt(): Grabbable | null {
    const h = this.held;
    if (!h) return null;
    if (h.velocity) {
      const v = this.carryVel.scale(this.throwStrength);
      // guard against a NaN slipping into the physics
      if ([v.x, v.y, v.z].every(Number.isFinite)) h.velocity.copyFrom(v);
      else h.velocity.setAll(0);
    }
    this.held = null;
    this.carryVel.setAll(0);
    this.throws++;
    return h;
  }

  isHolding(): boolean {
    return this.held !== null;
  }

  stats(): Record<string, string> {
    return {
      'Holding': this.held ? this.held.name : '—',
      'Hold distance': this.held ? this.holdDist.toFixed(0) + ' u' : '—',
      'Grabbed': String(this.grabs),
      'Thrown': String(this.throws)
    };
  }
}
