/**
 * StarFieldRenderer — draws the universe as the sky.
 *
 * The companion to DeepSkySystem: that module decides what is visible and
 * how bright, this one puts it on screen. Every point drawn here is a real
 * region at its real position, so the sky parallaxes correctly as you move
 * and any star you can see is somewhere you can go.
 *
 * All of it is one PointsCloudSystem with per-point colour, so ten thousand
 * distant objects cost a single draw call. Points are rebuilt when the
 * viewpoint moves far enough to matter rather than every frame, because at
 * interstellar distances a few hundred units of travel changes nothing
 * perceptible.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { PointsCloudSystem } from '@babylonjs/core/Particles/pointsCloudSystem';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';
import { visibleSky, LUMINOSITY, SKY_COLOR, type SkyObject } from './DeepSkySystem';

export interface StarFieldOptions {
  /** Maximum points drawn at once. */
  budget: number;
  /** How far the eye must move before the field is rebuilt, world units. */
  rebuildDistance: number;
  /**
   * Radius of the shell the points are projected onto. They are placed at
   * their true bearing but a fixed distance, so distant objects stay inside
   * the camera's far plane and cannot be clipped away.
   */
  shell: number;
}

export const DEFAULT_STARFIELD: StarFieldOptions = {
  budget: 7000,
  rebuildDistance: 260,
  shell: 3400
};

export class StarFieldRenderer {
  opts: StarFieldOptions;
  private scene: Scene | null = null;
  private pcs: PointsCloudSystem | null = null;
  private mesh: Mesh | null = null;
  private lastEye = new Vector3(1e12, 1e12, 1e12);
  private built = 0;
  private generation = 0;
  /** Points currently drawn. */
  count = 0;

  constructor(opts: Partial<StarFieldOptions> = {}) {
    this.opts = { ...DEFAULT_STARFIELD, ...opts };
  }

  attach(scene: Scene): void {
    this.scene = scene;
  }

  /**
   * Turns the universe's regions into sky objects.
   *
   * Luminosity and colour come from the kind of place it is, so a galaxy
   * outshines a planet by five orders of magnitude without anything being
   * hand-placed.
   */
  static toSkyObjects(regions: Array<{
    id: string; kind: string; position: Vector3; radius: number;
  }>): SkyObject[] {
    const out: SkyObject[] = [];
    for (const r of regions) {
      const lum = LUMINOSITY[r.kind] ?? 0;
      if (lum <= 0) continue;
      const c = SKY_COLOR[r.kind] ?? [1, 1, 1];
      out.push({
        id: r.id,
        kind: r.kind,
        position: r.position,
        radius: r.radius,
        luminosity: lum,
        color: new Color3(c[0], c[1], c[2])
      });
    }
    return out;
  }

  /**
   * Rebuilds the field if the eye has moved far enough to change it.
   * Returns true if a rebuild happened.
   */
  update(objects: SkyObject[], eye: Vector3): boolean {
    if (!this.scene) return false;
    if (Vector3.Distance(eye, this.lastEye) < this.opts.rebuildDistance) return false;
    this.lastEye.copyFrom(eye);
    this.rebuild(objects, eye);
    return true;
  }

  /** Forces a rebuild regardless of how far the eye has moved. */
  rebuild(objects: SkyObject[], eye: Vector3): void {
    const scene = this.scene;
    if (!scene) return;

    const samples = visibleSky(objects, eye, this.opts.budget);
    this.dispose(false);
    const generation = ++this.generation;

    if (!samples.length) { this.count = 0; return; }

    // Index the objects so each sample can be placed at its true bearing.
    const byId = new Map(objects.map((o) => [o.id, o]));
    const shell = this.opts.shell;

    const pcs = new PointsCloudSystem('starfield' + (++this.built), 1, scene);
    pcs.addPoints(samples.length, (particle: any, i: number) => {
      const s = samples[i];
      const o = byId.get(s.id);
      if (!o) return;
      // True direction, fixed radius: correct bearing, never clipped.
      const dir = o.position.subtract(eye);
      const len = dir.length();
      const unit = len > 1e-6 ? dir.scale(1 / len) : new Vector3(0, 0, 1);
      particle.position = unit.scale(shell).add(eye);
      particle.color = s.color;
    });

    void pcs.buildMeshAsync().then((mesh) => {
      if (!mesh) return;
      if (generation !== this.generation || scene !== this.scene) {
        try { mesh.dispose(); pcs.dispose(); } catch { /* superseded build */ }
        return;
      }
      this.mesh = mesh;
      mesh.renderingGroupId = 0;
      // The sky must never occlude anything. Points were writing opaque
      // depth at their shell radius, so real geometry behind that shell was
      // depth-culled and punched out as black patches - and because the
      // points are rebuilt as the camera moves, the holes swarmed with
      // mouse movement. Depth-write off makes the sky a pure backdrop.
      mesh.isPickable = false;
      mesh.applyFog = false;
      mesh.alwaysSelectAsActiveMesh = true;
      const m = mesh.material as any;
      if (m) {
        m.disableLighting = true;
        m.pointSize = 2.3;
        // Depth-write off is the actual fix for the black patches.
        m.disableDepthWrite = true;
        m.forceDepthWrite = false;
        m.needDepthPrePass = false;
        // Additive, so a star can only ever add light. alpha must be nudged
        // off 1.0 or needAlphaBlending() stays false and Babylon ignores
        // alphaMode entirely, leaving opaque quads behind every point.
        m.alpha = 0.999;
        m.alphaMode = 1; // Constants.ALPHA_ADD
      }
    });

    this.pcs = pcs;
    this.count = samples.length;
  }

  stats(): Record<string, string> {
    return { 'Stars drawn': String(this.count) };
  }

  dispose(full = true): void {
    this.generation++;
    try { this.mesh?.dispose(); } catch { /* already gone */ }
    try { this.pcs?.dispose(); } catch { /* already gone */ }
    this.mesh = null;
    this.pcs = null;
    if (full) { this.scene = null; this.count = 0; }
  }
}
