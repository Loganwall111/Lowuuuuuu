/**
 * WarpSystem — the visual language of going fast.
 *
 * Flying between stars means crossing distances where nothing appears to
 * move, which reads as standing still. Streaking the starfield past the
 * camera gives speed a felt quality and tells you the throttle is working.
 *
 * The effect is driven purely by *current speed*, so it engages and fades on
 * its own as you accelerate and slow down. Nothing else needs to know it
 * exists.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { Matrix, Quaternion } from '@babylonjs/core/Maths/math.vector';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Scene } from '@babylonjs/core/scene';

export interface WarpOptions {
  /** Speed at which streaks begin to appear, in world units/sec. */
  threshold: number;
  /** Speed at which the effect is at full strength. */
  full: number;
  /** How many streaks to maintain. */
  count: number;
  /** Radius of the tube of streaks around the camera. */
  radius: number;
  /** How far ahead/behind streaks live. */
  depth: number;
}

export const DEFAULT_WARP: WarpOptions = {
  // Free-fly cruises at ~60 u/s near objects and only climbs into the
  // hundreds in deep space, so a 220 threshold meant the streaks almost
  // never appeared and flying felt inert. They now begin at a speed you
  // actually reach and reach full strength while still manoeuvring.
  threshold: 45,
  full: 900,
  count: 320,
  radius: 30,
  depth: 170
};

interface Streak {
  /** Position relative to the camera. */
  off: Vector3;
  len: number;
  bright: number;
}

export class WarpSystem {
  private scene: Scene;
  private mesh: Mesh | null = null;
  private mat: StandardMaterial | null = null;
  private streaks: Streak[] = [];
  private data: Float32Array | null = null;
  /** 0..1 how engaged the effect currently is. */
  private amount = 0;
  private enabled = true;
  opts: WarpOptions = { ...DEFAULT_WARP };

  constructor(scene: Scene, opts: Partial<WarpOptions> = {}) {
    this.scene = scene;
    this.opts = { ...DEFAULT_WARP, ...opts };
    this.build();
  }

  /** Current engagement, 0 = off, 1 = full warp. Useful for telemetry. */
  get intensity(): number { return this.amount; }
  get active(): boolean { return this.amount > 0.01; }

  setEnabled(on: boolean): void {
    this.enabled = on;
    if (!on && this.mesh) this.mesh.setEnabled(false);
  }

  private build(): void {
    // A single thin box, thin-instanced per streak: one draw call.
    const m = MeshBuilder.CreateBox('warpStreak', { width: 0.06, height: 0.06, depth: 1 }, this.scene);
    const mat = new StandardMaterial('warpM', this.scene);
    mat.emissiveColor = new Color3(0.72, 0.84, 1.0);
    mat.disableLighting = true;
    // Additive so streaks glow instead of occluding the stars behind them.
    mat.alpha = 0.85;
    mat.alphaMode = 1; // ADD
    mat.backFaceCulling = false;
    m.material = mat;
    m.isPickable = false;
    m.alwaysSelectAsActiveMesh = true;
    m.infiniteDistance = false;
    m.setEnabled(false);
    // Never let streaks be culled or occlude UI-critical geometry.
    m.renderingGroupId = 0;

    this.mesh = m;
    this.mat = mat;

    for (let i = 0; i < this.opts.count; i++) this.streaks.push(this.spawn());
    this.data = new Float32Array(this.opts.count * 16);
  }

  private spawn(): Streak {
    const a = Math.random() * Math.PI * 2;
    // Bias toward the edges so the centre of the view stays readable.
    const r = this.opts.radius * (0.35 + Math.random() * 0.65);
    return {
      off: new Vector3(Math.cos(a) * r, Math.sin(a) * r,
        (Math.random() * 2 - 1) * this.opts.depth),
      len: 1,
      bright: 0.35 + Math.random() * 0.65
    };
  }

  /**
   * Advances the effect.
   *
   * @param dt      seconds
   * @param speed   current speed in world units/sec
   * @param eye     camera position
   * @param forward unit vector the camera is looking along
   */
  update(dt: number, speed: number, eye: Vector3, forward: Vector3): void {
    if (!this.mesh || !this.enabled) return;

    const { threshold, full } = this.opts;
    const raw = full > threshold
      ? (speed - threshold) / (full - threshold)
      : 0;
    const target = Math.max(0, Math.min(1, Number.isFinite(raw) ? raw : 0));

    // Ease in and out so the effect never pops on.
    this.amount += (target - this.amount) * Math.min(1, dt * 3.5);
    if (this.amount < 0.01) {
      this.mesh.setEnabled(false);
      return;
    }
    this.mesh.setEnabled(true);

    const fwd = forward.lengthSquared() > 1e-9 ? forward.normalize() : new Vector3(0, 0, 1);
    // Build a frame around the view direction to place streaks in.
    const up = Math.abs(fwd.y) > 0.95 ? new Vector3(1, 0, 0) : new Vector3(0, 1, 0);
    const right = Vector3.Cross(up, fwd).normalize();
    const trueUp = Vector3.Cross(fwd, right).normalize();

    const travel = Math.max(speed, 0) * dt;
    const depth = this.opts.depth;
    const q = new Quaternion();
    const scale = new Vector3(1, 1, 1);
    const pos = new Vector3();
    const m = Matrix.Identity();

    // Point every streak along the direction of travel.
    Quaternion.FromLookDirectionRHToRef(fwd, trueUp, q);

    for (let i = 0; i < this.streaks.length; i++) {
      const st = this.streaks[i];
      // Move backwards past the camera.
      st.off.z -= travel;
      if (st.off.z < -depth) {
        // Recycle to the front rather than allocating.
        const a = Math.random() * Math.PI * 2;
        const r = this.opts.radius * (0.35 + Math.random() * 0.65);
        st.off.x = Math.cos(a) * r;
        st.off.y = Math.sin(a) * r;
        st.off.z = depth;
        st.bright = 0.35 + Math.random() * 0.65;
      }

      // Streak length grows with speed: the core of the effect.
      st.len = 1 + this.amount * 46 * (0.4 + st.bright * 0.6);
      scale.set(1, 1, st.len);

      pos.set(
        eye.x + right.x * st.off.x + trueUp.x * st.off.y + fwd.x * st.off.z,
        eye.y + right.y * st.off.x + trueUp.y * st.off.y + fwd.y * st.off.z,
        eye.z + right.z * st.off.x + trueUp.z * st.off.y + fwd.z * st.off.z
      );

      Matrix.ComposeToRef(scale, q, pos, m);
      m.copyToArray(this.data!, i * 16);
    }

    this.mesh.thinInstanceSetBuffer('matrix', this.data!, 16, false);
    if (this.mat) this.mat.alpha = 0.25 + this.amount * 0.6;
  }

  stats(): Record<string, string> {
    return {
      'Warp': this.amount > 0.01 ? (this.amount * 100).toFixed(0) + '%' : 'off'
    };
  }

  dispose(): void {
    this.mesh?.dispose();
    this.mat?.dispose();
    this.mesh = null;
    this.mat = null;
    this.streaks = [];
    this.data = null;
  }
}
