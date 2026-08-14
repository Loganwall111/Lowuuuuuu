/**
 * RegionTides — what a black hole does to the worlds around it.
 *
 * TidalField stretches individual meshes: ships, thrown rocks, anything with
 * geometry you can scale. Planets are not that. Out in the open universe a
 * planet is a region in a point cloud, so "spaghettify the planet mesh" has
 * nothing to grab. What actually has to happen to a planet is coarser and
 * more consequential: it is dragged out of its orbit, torn into a stream of
 * debris, and finally eaten.
 *
 * So this operates on regions rather than meshes. Each frame it asks, for
 * every world close enough to a hole to care: how badly is this being pulled
 * apart, where has it been dragged to, and is it gone yet?
 *
 * The physics is the same Roche-limit model TidalField uses, so a planet and
 * a ship agree about what "too close" means — they just suffer differently.
 * Sandbox mode only; in Explorer nothing here is ever called, which is what
 * makes the universe safe to sightsee in.
 *
 * Pure arithmetic over plain objects, so the whole thing is testable without
 * an engine.
 */

import { tidalState, type TidalBody } from './GameModes';

/** The subset of a Region this system needs. */
export interface TidalRegion {
  id: string;
  kind: string;
  name: string;
  /** Mutated in place: this is how a world gets dragged toward a hole. */
  position: { x: number; y: number; z: number };
  mass: number;
  radius: number;
  /** Visible body radius, where the region has one. */
  surfaceRadius?: number;
}

/** A hole doing the pulling. */
export interface TidalHole {
  id: string;
  position: { x: number; y: number; z: number };
  /** Horizon radius, world units. */
  horizon: number;
}

/** What happened to one world this frame. */
export interface RegionTideResult {
  id: string;
  name: string;
  /** Which hole is acting on it. */
  holeId: string;
  /** 0 = untouched, 1 = at the Roche limit, >1 = coming apart. */
  stress: number;
  /** How far through disruption, 0..1. At 1 it is a debris stream. */
  shredded: number;
  /** True the frame it crosses the horizon and should be removed. */
  consumed: boolean;
  /** True while it is being torn apart but still exists. */
  disrupting: boolean;
  /** How far it moved toward the hole this frame, world units. */
  drawnIn: number;
}

/**
 * How strongly a world holds itself together, by kind.
 *
 * A gas giant is barely bound at all and streams away first; a rocky planet
 * lasts longer; a star's core is dense enough to survive well inside the
 * distance that shreds a planet. These are the numbers that decide the order
 * things come apart in, which is the whole spectacle.
 */
export function cohesionOf(kind: string): number {
  switch (kind) {
    case 'nebula': return 0.02;      // already diffuse
    case 'ocean': return 0.10;
    case 'planet': return 0.12;
    case 'terrain': return 0.14;
    case 'star-system': return 0.30; // the star itself is dense
    case 'galaxy': return 0.05;
    default: return 0.15;
  }
}

/** Turns a region into something the tidal model understands. */
export function bodyFor(r: TidalRegion): TidalBody {
  return {
    size: Math.max(1, r.surfaceRadius ?? r.radius * 0.1),
    cohesion: cohesionOf(r.kind),
    mass: Math.max(1, r.mass)
  };
}

/**
 * Worlds that can be torn apart.
 *
 * Black holes are excluded — a hole is not disrupted by another hole at the
 * scales this game plays at, and letting one eat another would quietly
 * delete the thing the player came to look at. Dimensions are excluded
 * because they are not places in this space.
 */
export function isVulnerable(kind: string): boolean {
  return kind !== 'blackhole' && kind !== 'dimension' && kind !== 'deep-space';
}

/**
 * How far out a hole is worth considering, in horizon radii.
 *
 * The tidal term falls as 1/r³, so past this it contributes nothing but
 * cost. Generous enough that a loosely bound nebula still starts to stream
 * before you can see the hole clearly.
 */
export const INFLUENCE_RADII = 260;

export class RegionTides {
  /** Ids consumed since the last drain. */
  private eaten: RegionTideResult[] = [];

  /**
   * Applies one frame of tidal physics to every world near a hole.
   *
   * Mutates region positions in place — being dragged toward a hole IS the
   * region moving, not a rendering trick, so everything that reads region
   * positions (the map, the star field, the navigator) follows for free.
   *
   * Returns only the regions actually affected, so a caller can drive a HUD
   * without filtering thousands of untouched ones.
   */
  update(
    dt: number,
    regions: TidalRegion[],
    holes: TidalHole[],
    enabled: boolean
  ): RegionTideResult[] {
    if (!enabled) return [];
    if (!Array.isArray(regions) || !Array.isArray(holes)) return [];
    if (!regions.length || !holes.length) return [];
    const step = Number.isFinite(dt) ? Math.max(0, Math.min(0.1, dt)) : 0;

    const out: RegionTideResult[] = [];

    for (const r of regions) {
      if (!r || !isVulnerable(r.kind)) continue;
      if (!Number.isFinite(r.position?.x)) continue;

      // Strongest field wins. Nearly always the nearest hole, but computing
      // it properly costs nothing and behaves correctly for a binary pair.
      let best: TidalHole | null = null;
      let bestStress = -1;
      let bestDist = 0;
      const body = bodyFor(r);

      for (const h of holes) {
        if (!h || !Number.isFinite(h.horizon) || h.horizon <= 0) continue;
        const d = distance(r.position, h.position);
        if (!Number.isFinite(d)) continue;
        if (d > h.horizon * INFLUENCE_RADII) continue;
        const st = tidalState(body, d, h.horizon, true);
        if (st.stress > bestStress) {
          bestStress = st.stress;
          best = h;
          bestDist = d;
        }
      }
      if (!best) continue;

      const st = tidalState(body, bestDist, best.horizon, true);
      // Nothing worth reporting until it is actually being strained.
      if (st.stress < 0.02 && !st.consumed) continue;

      const res: RegionTideResult = {
        id: r.id,
        name: r.name,
        holeId: best.id,
        stress: st.stress,
        shredded: st.shredded,
        consumed: st.consumed,
        disrupting: st.disrupting,
        drawnIn: 0
      };

      if (st.consumed) {
        this.eaten.push(res);
        out.push(res);
        continue;
      }

      // ---- dragged in ----
      const dx = best.position.x - r.position.x;
      const dy = best.position.y - r.position.y;
      const dz = best.position.z - r.position.z;
      const len = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (len > 1e-6 && step > 0) {
        const move = Math.min(st.pull * step, len * 0.5);
        r.position.x += (dx / len) * move;
        r.position.y += (dy / len) * move;
        r.position.z += (dz / len) * move;
        res.drawnIn = move;
      }

      out.push(res);
    }

    return out;
  }

  /** Worlds eaten since this was last called. Clears the list. */
  drainConsumed(): RegionTideResult[] {
    const out = this.eaten;
    this.eaten = [];
    return out;
  }

  /** Forgets any pending reports. Called when leaving sandbox mode. */
  clear(): void {
    this.eaten = [];
  }
}

function distance(
  a: { x: number; y: number; z: number },
  b: { x: number; y: number; z: number }
): number {
  const dx = a.x - b.x, dy = a.y - b.y, dz = a.z - b.z;
  return Math.sqrt(dx * dx + dy * dy + dz * dz);
}

/** Human-readable line for a world being torn apart, for the HUD. */
export function describeRegionTide(r: RegionTideResult): string {
  if (r.consumed) return r.name + ' has fallen past the horizon';
  if (r.shredded >= 0.99) return r.name + ' is a stream of debris';
  if (r.disrupting) return r.name + ' is breaking apart';
  if (r.stress > 0.5) return r.name + ' is being pulled out of its orbit';
  return r.name + ' is straining';
}
