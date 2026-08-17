/**
 * SpaceDust — the near-field motes that make space a place you are inside.
 *
 * A sky full of infinitely distant points has no parallax and therefore no
 * sense of motion: flying forward changes nothing close to you, which is a
 * large part of why space can feel empty and flat. A real cockpit has a
 * constant drift of fine dust and ice motes sliding past the canopy - the
 * one depth cue that reads at every speed, from docking to warp.
 *
 * This is a shell of faint additive points that follows the player and is
 * re-seeded as they travel. Points are placed in a shell around the eye and
 * then left in world space until the eye has moved far enough to leave them
 * behind, at which point a new shell is scattered ahead. The result is
 * genuine parallax: near motes stream past fast, far ones drift, and the
 * whole field never runs out no matter how far you fly.
 *
 * Same render rules as every other sky layer: additive, depth-write off,
 * rendering group 0, never fogged, never picked - it can only ever add
 * light, never occlude or be occluded.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color4 } from '@babylonjs/core/Maths/math.color';
import { PointsCloudSystem } from '@babylonjs/core/Particles/pointsCloudSystem';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';

export interface SpaceDustOptions {
  /** How many motes are alive at once. */
  count: number;
  /** Inner and outer radius of the dust shell around the eye. */
  inner: number;
  outer: number;
  /** Re-seed once the eye has travelled this far. */
  rebuildDistance: number;
  /** Drawn point size, pixels. */
  size: number;
}

export const DEFAULT_DUST: SpaceDustOptions = {
  count: 480,
  inner: 14,
  outer: 150,
  rebuildDistance: 90,
  size: 1.5
};

/** Deterministic hash RNG so a reseed scatters the same way every session. */
function rng(seed: number): () => number {
  let s = seed >>> 0 || 1;
  return () => {
    s = Math.imul(s, 1664525) + 1013904223 >>> 0;
    return s / 4294967296;
  };
}

export class SpaceDust {
  opts: SpaceDustOptions;
  private scene: Scene | null = null;
  private pcs: PointsCloudSystem | null = null;
  private mesh: Mesh | null = null;
  private lastEye = new Vector3(1e12, 1e12, 1e12);
  private seed = 0x9d1c4e77;
  private generation = 0;

  constructor(opts: Partial<SpaceDustOptions> = {}) {
    this.opts = { ...DEFAULT_DUST, ...opts };
  }

  get isBuilt(): boolean { return this.mesh !== null; }
  setEnabled(on: boolean): void { this.mesh?.setEnabled(on); }

  attach(scene: Scene): void {
    this.scene = scene;
  }

  /** Builds the point cloud once; positions are rewritten as you travel. */
  async build(): Promise<void> {
    const scene = this.scene;
    if (!scene || this.pcs) return;
    const generation = ++this.generation;

    const pcs = new PointsCloudSystem('spaceDust', this.opts.size, scene);
    pcs.addPoints(this.opts.count, (p: any) => {
      p.position = new Vector3(0, -10000, 0);
      p.color = new Color4(0, 0, 0, 0);
    });

    const mesh = await pcs.buildMeshAsync();
    if (!mesh) return;
    if (generation !== this.generation || scene !== this.scene) {
      try { mesh.dispose(); pcs.dispose(); } catch { /* superseded */ }
      return;
    }

    mesh.renderingGroupId = 0;
    mesh.isPickable = false;
    mesh.applyFog = false;
    mesh.alwaysSelectAsActiveMesh = true;

    const m = mesh.material as any;
    if (m) {
      m.disableLighting = true;
      // Never write depth: the dust must composite as light, never occlude.
      m.disableDepthWrite = true;
      m.forceDepthWrite = false;
      m.needDepthPrePass = false;
      // Additive, with alpha nudged off 1.0 so the blender actually arms.
      m.alpha = 0.999;
      m.alphaMode = 1; // Constants.ALPHA_ADD
      m.backFaceCulling = false;
    }

    this.mesh = mesh;
    this.pcs = pcs;
    this.seedAround(new Vector3(0, 0, 0), true);
  }

  /** Scatters motes in a shell around a point, writing the vertex buffer. */
  private seedAround(eye: Vector3, force = false): void {
    if (!this.mesh) return;
    const data = this.mesh.getVerticesData('position');
    if (!data) return;
    const rand = rng((this.seed = (this.seed + 7919) >>> 0));
    const colors = this.mesh.getVerticesData('color');

    for (let i = 0; i < this.opts.count; i++) {
      // Uniform direction on the sphere.
      const u = rand() * 2 - 1;
      const phi = rand() * Math.PI * 2;
      const s = Math.sqrt(Math.max(0, 1 - u * u));
      const dx = s * Math.cos(phi);
      const dy = u;
      const dz = s * Math.sin(phi);
      // Cube-root keeps the shell volumetrically uniform, not shell-hugging.
      const t = Math.cbrt(rand());
      const r = this.opts.inner + (this.opts.outer - this.opts.inner) * t;

      data[i * 3] = eye.x + dx * r;
      data[i * 3 + 1] = eye.y + dy * r;
      data[i * 3 + 2] = eye.z + dz * r;

      if (colors) {
        // Faint blue-white dust, occasionally a warm mote or a bright spark.
        const warm = rand() < 0.14;
        const bright = rand() < 0.06;
        const a = (0.05 + rand() * 0.12) * (bright ? 1.8 : 1);
        colors[i * 4] = warm ? 0.85 : 0.72;
        colors[i * 4 + 1] = warm ? 0.80 : 0.84;
        colors[i * 4 + 2] = warm ? 0.62 : 1.0;
        colors[i * 4 + 3] = Math.min(0.6, a);
      }
    }

    this.mesh.updateVerticesData('position', data, false, false);
    if (colors) this.mesh.updateVerticesData('color', colors, false, false);
    this.mesh.refreshBoundingInfo();
    this.lastEye.copyFrom(eye);
  }

  /** Re-seeds once the eye has travelled far enough. */
  update(eye: Vector3): void {
    if (!this.mesh || !this.scene) return;
    if (Vector3.Distance(eye, this.lastEye) < this.opts.rebuildDistance) return;
    this.seedAround(eye);
  }

  stats(): Record<string, string> {
    return { 'Space dust': this.mesh ? String(this.opts.count) : 'off' };
  }

  dispose(): void {
    this.generation++;
    try { this.pcs?.dispose(); } catch { /* already gone */ }
    try { this.mesh?.dispose(); } catch { /* already gone */ }
    this.pcs = null;
    this.mesh = null;
    this.scene = null;
  }
}
