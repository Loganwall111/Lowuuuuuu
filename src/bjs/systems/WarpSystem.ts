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
 *
 * WHY THE STREAKS USED TO BE INVISIBLE AT WARP. The streaks were advanced by
 * the raw world speed: `travel = speed * dt`. The tube they live in is only
 * `depth` (170) units long, so as soon as speed passed about 10,000 u/s each
 * streak moved further in one frame than the entire tube is deep and was
 * recycled immediately - at full warp, 54,000 times over per frame. Every
 * streak therefore reappeared at a fresh random position every single frame,
 * which is uncorrelated noise, not motion. What you saw was the streaks
 * getting LONGER (length keys off `amount`, which does saturate) while never
 * appearing to travel. The faster you went, the less it moved.
 *
 * The fix is that apparent motion has to be bounded. Beyond a reference
 * speed the streaks advance on a logarithmic curve rather than a linear one,
 * so the tube keeps flowing at a readable rate however absurd the real
 * velocity is. This is the same reason a starfield in a film does not
 * actually run at lightspeed: past a point, more is indistinguishable from
 * noise.
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
  /**
   * Speed at which apparent flow stops being linear, in world units/sec.
   *
   * Below this, streaks move at true speed. Above it they move on a log
   * curve, because a streak that crosses the whole tube in one frame
   * carries no information about motion at all.
   */
  flowRef: number;
  /** Tube lengths per second the flow saturates toward. */
  flowMax: number;
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
  depth: 170,
  // Linear up to a brisk cruise; a log curve past it. 900 matches `full`,
  // so the flow starts bending exactly where the effect reaches full
  // strength and there is no visible seam between the two regimes.
  flowRef: 900,
  // Six tube-lengths a second at the top. Fast enough to read as violent,
  // slow enough that individual streaks are still resolvable rather than
  // strobing into uniform static.
  flowMax: 6
};

/**
 * How far the streaks should APPEAR to move this frame.
 *
 * Linear below flowRef so ordinary flight is honest, then logarithmic, so
 * that going ten times faster still looks faster without the tube
 * degenerating into per-frame random noise.
 */
export function apparentFlow(speed: number, o: WarpOptions): number {
  const s = Number.isFinite(speed) ? Math.max(0, speed) : 0;
  const ref = Math.max(1e-6, o.flowRef);
  const cap = Math.max(0, o.flowMax) * Math.max(1e-6, o.depth * 2);
  if (s <= ref) {
    // True speed, scaled so it meets the curve continuously at s == ref.
    return Math.min(s, cap);
  }
  const linear = Math.min(ref, cap);
  // log1p keeps this finite for any input, including warp's ~1.1e9 u/s.
  const extra = Math.log1p((s - ref) / ref) / Math.log1p(1e6 / ref);
  return linear + (cap - linear) * Math.min(1, Math.max(0, extra));
}

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
  // Scratch math reused on every update. At 60 fps these replace more than
  // 400 short-lived vector/matrix objects per second during warp.
  private fwdScratch = new Vector3(0, 0, 1);
  private upScratch = new Vector3(0, 1, 0);
  private rightScratch = new Vector3(1, 0, 0);
  private trueUpScratch = new Vector3(0, 1, 0);
  private quatScratch = new Quaternion();
  private scaleScratch = new Vector3(1, 1, 1);
  private posScratch = new Vector3();
  private matrixScratch = Matrix.Identity();
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
  /**
   * Apparent flow last frame, world units/sec.
   *
   * Exposed so the screen-space tunnel advances in lockstep with the
   * streaks. If the two used different rates they would visibly slide
   * against each other.
   */
  get flow(): number { return this.lastFlow; }
  private lastFlow = 0;

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
    // sqrt() makes the radial distribution uniform over the disc area
    // rather than piling streaks up around the axis, which is what made
    // the old tube look like a sparse cylinder instead of a rush of stars.
    const r = this.opts.radius * (0.18 + Math.sqrt(Math.random()) * 0.82);
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

    const fwd = this.fwdScratch;
    if (forward.lengthSquared() > 1e-9) forward.normalizeToRef(fwd);
    else fwd.set(0, 0, 1);
    // Build a frame around the view direction without transient vectors.
    const up = this.upScratch;
    if (Math.abs(fwd.y) > 0.95) up.set(1, 0, 0);
    else up.set(0, 1, 0);
    const right = this.rightScratch;
    Vector3.CrossToRef(up, fwd, right);
    right.normalize();
    const trueUp = this.trueUpScratch;
    Vector3.CrossToRef(fwd, right, trueUp);
    trueUp.normalize();

    // Apparent flow, not raw speed: see the note at the top of the file.
    const flowRate = apparentFlow(speed, this.opts);
    this.lastFlow = flowRate;
    const travel = flowRate * dt;
    const depth = this.opts.depth;
    const q = this.quatScratch;
    const scale = this.scaleScratch;
    const pos = this.posScratch;
    const m = this.matrixScratch;

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

      // Streak length grows with speed: the core of the effect. Thickness
      // shrinks as it stretches, so a streak reads as a drawn-out point of
      // light rather than a lengthening stick.
      st.len = 1 + this.amount * 46 * (0.4 + st.bright * 0.6);
      const thin = 1 - this.amount * 0.45;
      scale.set(thin, thin, st.len);

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
