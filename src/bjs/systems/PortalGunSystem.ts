/**
 * PortalGunSystem — shoot two holes in the universe and walk between them.
 *
 * Two portals, A and B. Anything entering one leaves the other with its
 * speed preserved and its direction rotated by the difference between the
 * two portal orientations. That rotation is the whole trick: walk into a
 * floor portal and you come out of a wall portal moving sideways.
 *
 * Portals here are not scoped to a world. A pair can span two different
 * places, which is what makes the gun interesting in a universe where the
 * ocean world and a black hole are both just destinations.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';

export type PortalSlot = 'a' | 'b';

export interface Portal {
  slot: PortalSlot;
  /** Centre of the opening. */
  position: Vector3;
  /** Unit normal: the direction you come *out* facing. */
  normal: Vector3;
  radius: number;
  /** Which place this portal is in, so a pair can bridge two worlds. */
  worldId: string;
  /** Set false while it is being placed or has been dissolved. */
  open: boolean;
}

export interface TeleportResult {
  /** Where the traveller ended up. */
  position: Vector3;
  /** Their new velocity, rotated into the exit portal's frame. */
  velocity: Vector3;
  /** Which portal they came out of. */
  exit: PortalSlot;
  /** The world they are now in, which may differ from where they started. */
  worldId: string;
}

export interface Traveller {
  position: Vector3;
  velocity: Vector3;
  /** Used to stop instant re-entry after coming out the other side. */
  radius?: number;
}

/**
 * Carries a velocity through a portal pair.
 *
 * A single frame-to-frame rotation cannot express this correctly, because
 * the two natural test cases disagree under one rotation: a floor portal
 * feeding a wall portal, and a pair of portals facing the same way. The
 * physically honest formulation is to decompose the velocity relative to
 * the entry portal and rebuild it in the exit portal's frame:
 *
 *   - the component *into* the entry face becomes the component *out of*
 *     the exit face (you always emerge moving forwards);
 *   - the sideways component is carried across unchanged in magnitude.
 *
 * Speed is conserved exactly, which is the property that matters.
 */
export function rotateThrough(v: Vector3, fromN: Vector3, toN: Vector3): Vector3 {
  const a = fromN.clone().normalize();
  const b = toN.clone().normalize();

  // How fast we are driving into the entry face (positive when entering).
  const into = -Vector3.Dot(v, a);

  // Whatever is left is tangential to the entry plane.
  const tangent = v.subtract(a.scale(Vector3.Dot(v, a)));
  const tanLen = tangent.length();

  // Rebuild: forwards out of the exit face, plus the sideways part mapped
  // into the exit plane.
  let out = b.scale(into);

  if (tanLen > 1e-9) {
    // Map the entry plane's tangent onto the exit plane by rotating about
    // the axis between the two normals. If the normals are parallel the
    // tangent already lies in the exit plane.
    const dot = Math.max(-1, Math.min(1, Vector3.Dot(a, b)));
    let mapped: Vector3;
    if (dot > 0.999999) {
      mapped = tangent;
    } else if (dot < -0.999999) {
      const up = Math.abs(a.y) > 0.9 ? new Vector3(1, 0, 0) : new Vector3(0, 1, 0);
      const axis = Vector3.Cross(a, up).normalize();
      mapped = rodrigues(tangent, axis, Math.PI);
    } else {
      const axis = Vector3.Cross(a, b).normalize();
      mapped = rodrigues(tangent, axis, Math.acos(dot));
    }
    out = out.add(mapped);
  }

  // Guard the degenerate case where a caller passes a zero velocity.
  if (!Number.isFinite(out.x) || !Number.isFinite(out.y) || !Number.isFinite(out.z)) {
    return b.scale(v.length());
  }
  return out;
}

/** Rotates v around a unit axis by angle radians. */
function rodrigues(v: Vector3, axis: Vector3, angle: number): Vector3 {
  const c = Math.cos(angle), s = Math.sin(angle);
  const cross = Vector3.Cross(axis, v);
  const dot = Vector3.Dot(axis, v);
  return new Vector3(
    v.x * c + cross.x * s + axis.x * dot * (1 - c),
    v.y * c + cross.y * s + axis.y * dot * (1 - c),
    v.z * c + cross.z * s + axis.z * dot * (1 - c)
  );
}

export class PortalGunSystem {
  private portals: Record<PortalSlot, Portal | null> = { a: null, b: null };
  /** Travellers that just came through, so they cannot immediately loop. */
  private cooldown = new Map<Traveller, number>();
  private teleports = 0;

  get a(): Portal | null { return this.portals.a; }
  get b(): Portal | null { return this.portals.b; }
  get count(): number { return (this.a ? 1 : 0) + (this.b ? 1 : 0); }
  get linked(): boolean { return !!(this.a?.open && this.b?.open); }
  get teleportCount(): number { return this.teleports; }

  /**
   * Fires a portal. Placing a portal replaces the previous one in that slot,
   * which is what makes the gun feel like a gun rather than an inventory.
   */
  fire(
    slot: PortalSlot, position: Vector3, normal: Vector3,
    worldId = 'universe', radius = 2.2
  ): Portal | null {
    const n = normal.clone();
    const len = n.length();
    // A zero normal has no orientation, so there is nothing to place.
    if (!(len > 1e-6) || !Number.isFinite(position.x)) return null;

    const portal: Portal = {
      slot,
      position: position.clone(),
      normal: n.scale(1 / len),
      radius: Math.max(0.4, radius),
      worldId,
      open: true
    };
    this.portals[slot] = portal;
    return portal;
  }

  /** Removes one portal. The pair stops working until it is replaced. */
  clear(slot: PortalSlot): void { this.portals[slot] = null; }

  clearAll(): void {
    this.portals.a = null;
    this.portals.b = null;
    this.cooldown.clear();
  }

  /**
   * Tests a traveller against both portals and moves them if they went
   * through one. Returns null if nothing happened.
   *
   * Entry requires actually passing through the opening: within the disc,
   * and moving into the front face. Brushing past the edge does nothing.
   */
  tryTeleport(t: Traveller, dt = 0): TeleportResult | null {
    // Tick down the re-entry lockout.
    const cd = this.cooldown.get(t) ?? 0;
    if (cd > 0) {
      const left = cd - Math.max(dt, 0);
      if (left > 0) { this.cooldown.set(t, left); return null; }
      this.cooldown.delete(t);
    }

    if (!this.linked) return null;

    for (const slot of ['a', 'b'] as PortalSlot[]) {
      const entry = this.portals[slot];
      const exit = this.portals[slot === 'a' ? 'b' : 'a'];
      if (!entry?.open || !exit?.open) continue;

      const rel = t.position.subtract(entry.position);
      const along = Vector3.Dot(rel, entry.normal);

      // Must be at the plane, not merely near the portal.
      const reach = Math.max(t.radius ?? 0.5, 0.5);
      if (Math.abs(along) > reach) continue;

      // Must be inside the opening.
      const planar = rel.subtract(entry.normal.scale(along));
      if (planar.length() > entry.radius) continue;

      // Must be moving into the front of it.
      if (Vector3.Dot(t.velocity, entry.normal) >= 0) continue;

      // ---- through we go ----
      const newVel = rotateThrough(t.velocity, entry.normal, exit.normal);
      // Emerge just clear of the exit face so we do not re-trigger.
      const out = exit.position.add(exit.normal.scale(reach * 1.6));

      t.position.copyFrom(out);
      t.velocity.copyFrom(newVel);
      this.cooldown.set(t, 0.35);
      this.teleports++;

      return {
        position: out.clone(),
        velocity: newVel.clone(),
        exit: exit.slot,
        worldId: exit.worldId
      };
    }
    return null;
  }

  /** True when the pair bridges two different places. */
  bridgesWorlds(): boolean {
    return !!(this.a && this.b && this.a.worldId !== this.b.worldId);
  }

  stats(): Record<string, string> {
    return {
      'Portals placed': String(this.count),
      'Portal link': this.linked
        ? (this.bridgesWorlds() ? 'cross-world' : 'active')
        : 'incomplete',
      'Trips taken': String(this.teleports)
    };
  }

  dispose(): void { this.clearAll(); }
}
