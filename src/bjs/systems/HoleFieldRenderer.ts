/**
 * HoleFieldRenderer — black holes you can actually fly to.
 *
 * The reported bug: travelling to a black hole in the open universe gives an
 * "absolute black void with no assets visible", as though you were trapped
 * inside it.
 *
 * The cause was not a crash. A black hole out in the universe was only ever
 * a point of light in the star field plus a screen-space lensing pass. There
 * was no horizon sphere, no accretion disk, nothing with geometry - so there
 * was nothing to arrive at. Worse, the lensing pass paints everything inside
 * the horizon radius as `tint * 0.05`, and as you close in that radius grows
 * past the corner of the screen. Every pixel then fails the "inside" test at
 * once and the whole frame becomes a near-black wash. That is the void.
 *
 * BlackHoleWorld already renders a proper hole, but it is a separate world
 * you load from a menu - it is not what you meet when flying. This module
 * closes that gap: it gives every nearby hole real geometry, built from the
 * same BlackHoleBody that guarantees the horizon sphere and the disk share
 * one centre and therefore cannot drift apart.
 *
 * Only holes close enough to see are built, and they are released once you
 * leave, so an endless universe does not accumulate meshes.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';
import { BlackHoleBody, rollAnomaly } from './BlackHoleBody';

/** A hole the renderer has been asked to draw. */
export interface HoleSpec {
  id: string;
  position: Vector3;
  /** Horizon radius in world units. */
  horizon: number;
  /** Stable per-hole seed, so a given hole is always the same hole. */
  seed: number;
}

export interface HoleFieldOptions {
  /**
   * Build a hole once it is within this many horizon radii.
   *
   * Generous, because a supermassive hole is visible from a very long way
   * off and popping into existence would be worse than the cost of drawing
   * a few extra triangles.
   */
  buildWithin: number;
  /** Release it again past this many radii. Larger than buildWithin so a
   *  hole hovering at the boundary does not thrash. */
  releaseBeyond: number;
  /** Maximum holes with geometry at once. */
  maxLive: number;
}

export const DEFAULT_HOLEFIELD: HoleFieldOptions = {
  buildWithin: 320,
  releaseBeyond: 460,
  maxLive: 6
};

/** Disk inner edge, as a multiple of the horizon radius. */
export const DISK_INNER = 2.6;
/** Disk outer edge, as a multiple of the horizon radius. */
export const DISK_OUTER = 9.0;

interface LiveHole {
  id: string;
  body: BlackHoleBody;
  disk: Mesh;
  glow: Mesh;
  isAnomaly: boolean;
}

/**
 * Distance in horizon radii, which is the only scale that matters here: a
 * hole is "close" relative to its own size, not in absolute units.
 */
export function radiiAway(eye: Vector3, hole: HoleSpec): number {
  const h = Math.max(hole.horizon, 1e-6);
  return Vector3.Distance(eye, hole.position) / h;
}

export class HoleFieldRenderer {
  opts: HoleFieldOptions;
  private scene: Scene | null = null;
  private live = new Map<string, LiveHole>();

  constructor(opts: Partial<HoleFieldOptions> = {}) {
    this.opts = { ...DEFAULT_HOLEFIELD, ...opts };
  }

  attach(scene: Scene): void {
    this.scene = scene;
  }

  /** Holes currently drawn. */
  get count(): number { return this.live.size; }

  /** True if this hole has geometry right now. */
  has(id: string): boolean { return this.live.has(id); }

  /** Whether a built hole rolled as a fractured anomaly. */
  isAnomaly(id: string): boolean {
    return this.live.get(id)?.isAnomaly ?? false;
  }

  /**
   * Reports whether a hole's sphere and disk share a centre.
   *
   * Exposed so a test can assert the anti-drift guarantee against the real
   * meshes rather than trusting that the code looks right.
   */
  isLocked(id: string, epsilon = 1e-6): boolean {
    const h = this.live.get(id);
    if (!h) return true;
    if (!h.body.isLocked(epsilon)) return false;
    const c = h.body.center;
    // Compare local positions, the same quantity BlackHoleBody.isLocked
    // uses. getAbsolutePosition() reads the cached world matrix, which is
    // only refreshed when the mesh is rendered - in a headless scene, or on
    // any frame before the first render, it still holds the origin. That
    // reports drift where none exists. None of these meshes is parented, so
    // local position IS world position and the comparison is exact.
    return Vector3.Distance(h.disk.position, c) <= epsilon &&
           Vector3.Distance(h.glow.position, c) <= epsilon;
  }

  /**
   * Builds, moves and releases holes for the current viewpoint.
   * Cheap enough to call every frame: it only touches what changed.
   */
  update(eye: Vector3, holes: HoleSpec[]): void {
    if (!this.scene) return;

    // Nearest first, so a limited budget is spent on what you can actually
    // see rather than on whatever happened to come first in the list.
    const ranked = holes
      .map((h) => ({ h, d: radiiAway(eye, h) }))
      .sort((a, b) => a.d - b.d);

    const wanted = new Set<string>();
    for (const { h, d } of ranked) {
      if (wanted.size >= this.opts.maxLive) break;
      if (d <= this.opts.buildWithin) wanted.add(h.id);
    }

    // Release anything that has drifted out of range.
    for (const [id, lh] of [...this.live]) {
      const still = ranked.find((r) => r.h.id === id);
      if (!still || still.d > this.opts.releaseBeyond || !wanted.has(id)) {
        this.destroy(lh);
        this.live.delete(id);
      }
    }

    // Build or reposition the rest.
    for (const { h } of ranked) {
      if (!wanted.has(h.id)) continue;
      const existing = this.live.get(h.id);
      if (existing) {
        // One call moves the sphere, the disk and the glow together, so
        // there is no window in which they disagree.
        this.place(existing, h.position);
      } else {
        this.live.set(h.id, this.create(h));
      }
    }
  }

  /**
   * Moves a hole. Every piece is written from the same Vector3 in the same
   * call - this is the structural reason the disk cannot float away from
   * the horizon while the camera moves.
   */
  private place(lh: LiveHole, to: Vector3): void {
    lh.body.setCenter(to);
    lh.disk.position.copyFrom(to);
    lh.glow.position.copyFrom(to);
  }

  private create(spec: HoleSpec): LiveHole {
    const scene = this.scene!;
    const hz = Math.max(spec.horizon, 0.05);

    // The anomaly roll is per-hole and derived from that hole's own seed,
    // so it is stable across visits and cannot leak into other holes.
    const isAnomaly = rollAnomaly(spec.seed);

    const body = new BlackHoleBody({
      center: spec.position,
      diskInner: hz * DISK_INNER,
      seed: spec.seed,
      isAnomaly
    });
    body.build(scene, 'bhHorizon_' + spec.id);

    // ---- accretion disk ----
    // A torus rather than a flat disc: seen edge-on a flat disc vanishes to
    // a line, which is the "empty gray hole in the middle" look.
    const disk = MeshBuilder.CreateTorus('bhDisk_' + spec.id, {
      diameter: hz * (DISK_INNER + DISK_OUTER),
      thickness: hz * (DISK_OUTER - DISK_INNER) * 0.55,
      tessellation: 64
    }, scene);
    const dm = new StandardMaterial('bhDiskM_' + spec.id, scene);
    // Self-luminous: an accretion disk is not lit by anything, it glows.
    dm.emissiveColor = new Color3(1.0, 0.62, 0.28);
    dm.diffuseColor = Color3.Black();
    dm.specularColor = Color3.Black();
    dm.disableLighting = true;
    // Additive, and alpha nudged off 1.0 - alphaMode is ignored while
    // alpha === 1, so without this the disk draws as an opaque ring.
    dm.alpha = 0.999;
    dm.alphaMode = 1;
    dm.disableDepthWrite = true;
    dm.backFaceCulling = false;
    disk.material = dm;
    disk.position.copyFrom(spec.position);
    disk.rotation.x = Math.PI / 2;
    disk.isPickable = false;

    // ---- outer glow ----
    // Sells the hole from far away, where the disk is only a few pixels.
    const glow = MeshBuilder.CreateSphere('bhGlow_' + spec.id, {
      diameter: hz * DISK_OUTER * 1.6, segments: 24
    }, scene);
    const gm = new StandardMaterial('bhGlowM_' + spec.id, scene);
    gm.emissiveColor = new Color3(0.22, 0.11, 0.05);
    gm.diffuseColor = Color3.Black();
    gm.specularColor = Color3.Black();
    gm.disableLighting = true;
    gm.alpha = 0.14;
    gm.alphaMode = 1;
    gm.disableDepthWrite = true;
    gm.backFaceCulling = false;
    glow.material = gm;
    glow.position.copyFrom(spec.position);
    glow.isPickable = false;

    return { id: spec.id, body, disk, glow, isAnomaly };
  }

  private destroy(lh: LiveHole): void {
    try { lh.body.dispose(); } catch { /* already gone */ }
    try { lh.disk.material?.dispose(); lh.disk.dispose(); } catch { /* gone */ }
    try { lh.glow.material?.dispose(); lh.glow.dispose(); } catch { /* gone */ }
  }

  dispose(): void {
    for (const lh of this.live.values()) this.destroy(lh);
    this.live.clear();
  }
}
