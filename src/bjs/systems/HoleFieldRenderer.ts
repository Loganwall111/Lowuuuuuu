/**
 * UnifiedSingularityRenderer
 *
 * One full-screen geodesic pass owns the nearest open-universe singularity.
 * There are deliberately no camera-facing quads, spheres, transparent cards,
 * duplicated cores, or refraction meshes. Consequently there is no geometry
 * edge that can become a floating bubble and no billboard transform that can
 * make a black hole follow the camera.
 */
import { Matrix, Vector2, Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { Effect } from '@babylonjs/core/Materials/effect';
import { Texture } from '@babylonjs/core/Materials/Textures/texture';
import { PostProcess } from '@babylonjs/core/PostProcesses/postProcess';
import type { Scene } from '@babylonjs/core/scene';
import { holeProfile, type HoleProfile } from './HoleProfiles';
import { rollAnomaly } from './BlackHoleBody';
import {
  LENS_MODE_ID, LENS_ORDER, LENS_PROFILES, type LensProfile
} from './LensProfiles';
import { HOLE_FIELD_SHADER, registerHoleFieldShader } from '../shaders/HoleFieldShader';
import { toRenderRef } from './RenderOrigin';

export interface HoleSpec {
  id: string;
  position: Vector3;
  horizon: number;
  seed: number;
  lens?: LensProfile;
}

export interface HoleFieldOptions {
  buildWithin: number;
  releaseBeyond: number;
  maxLive: number;
}

export const DEFAULT_HOLEFIELD: HoleFieldOptions = {
  buildWithin: 64,
  releaseBeyond: 84,
  maxLive: 1
};

export const DISK_INNER = 2.6;
export const DISK_OUTER = 9.0;
/** Retained as the geodesic pass's radial influence, not a geometry size. */
export const QUAD_RADII = 11.5;

export function radiiAway(eye: Vector3, hole: HoleSpec): number {
  return Vector3.Distance(eye, hole.position) / Math.max(hole.horizon, 1e-6);
}

interface ActiveSingularity {
  spec: HoleSpec;
  profile: HoleProfile;
  lens: LensProfile;
  anomaly: boolean;
}

export class HoleFieldRenderer {
  opts: HoleFieldOptions;
  private scene: Scene | null = null;
  private pass: PostProcess | null = null;
  private active: ActiveSingularity | null = null;
  private center = new Vector2(-10, -10);
  private resolution = new Vector2(1, 1);
  private lensTint = new Color3(1, 1, 1);
  private localHole = new Vector3();
  private screenHorizon = 0;
  private t = 0;
  private failed = false;
  private errorObserver: any = null;

  // Kept for API compatibility with verse updates. The new pass lenses the
  // already-rendered scene itself, so it never needs to synthesize a second
  // camera-relative sky.
  sky: { medium: string; symmetry: number; tint: [number, number, number];
    strangeness: number; zoom: number } = {
      medium: 'stars', symmetry: 0, tint: [0.06, 0.10, 0.22],
      strangeness: 0, zoom: 1
    };

  constructor(opts: Partial<HoleFieldOptions> = {}) {
    this.opts = { ...DEFAULT_HOLEFIELD, ...opts };
  }

  attach(scene: Scene): void {
    this.dispose();
    this.scene = scene;
    this.failed = false;
    registerHoleFieldShader(Effect.ShadersStore);
    const engine = scene.getEngine();
    this.errorObserver = engine.onEffectErrorObservable.add(({ effect, errors }) => {
      let source = '';
      try {
        const fx = effect as any;
        source = fx.getFragmentShaderSource?.() ?? fx._fragmentSourceCode ?? String(fx.name ?? '');
      } catch { /* unavailable source */ }
      if (!source.includes('u_holeEnabled')) return;
      this.failed = true;
      this.active = null;
      console.warn('Black-hole lens shader disabled; base universe preserved:', errors);
      setTimeout(() => { this.pass?.dispose(); this.pass = null; }, 0);
    });
  }

  /** Compiles only when a visible nearby hole actually needs the effect. */
  private ensurePass(): void {
    const scene = this.scene;
    const camera = scene?.activeCamera;
    if (this.pass || this.failed || !scene || !camera) return;
    try {
      // PostProcess automatically binds the previous frame as 'textureSampler'.
      this.pass = new PostProcess(
        HOLE_FIELD_SHADER, HOLE_FIELD_SHADER,
        ['center', 'resolution', 'horizon', 'time', 'u_holeEnabled', 'spin',
          'diskInner', 'diskOuter', 'diskTilt', 'diskBright', 'temperature', 'seed',
          'lensMode', 'lensStrength', 'lensFalloff', 'ringAmount', 'ringRadius',
          'lensSymmetry', 'lensDistortion', 'lensTwist', 'lensChroma',
          'lensTint', 'lensSoftness'],
        null, 1, camera, Texture.BILINEAR_SAMPLINGMODE, scene.getEngine(), false);
      this.pass.onApply = (fx) => {
        const a = this.active;
        fx.setFloat2('center', this.center.x, this.center.y);
        fx.setFloat2('resolution', this.resolution.x, this.resolution.y);
        fx.setFloat('horizon', this.screenHorizon);
        fx.setFloat('time', this.t);
        fx.setFloat('u_holeEnabled', a ? 1 : 0);
        fx.setFloat('spin', a?.profile.spin ?? 0);
        fx.setFloat('diskInner', a?.profile.diskInner ?? DISK_INNER);
        fx.setFloat('diskOuter', a?.profile.diskOuter ?? DISK_OUTER);
        fx.setFloat('diskTilt', a?.profile.diskTilt ?? .5);
        fx.setFloat('diskBright', a?.profile.diskBright ?? 0);
        fx.setFloat('temperature', a?.profile.temperature ?? .5);
        fx.setFloat('seed', ((a?.spec.seed ?? 0) % 997) / 997);
        const l = a?.lens ?? LENS_PROFILES.schwarzschild;
        fx.setFloat('lensMode', LENS_MODE_ID[l.mode] ?? 0);
        fx.setFloat('lensStrength', l.strength);
        fx.setFloat('lensFalloff', l.falloff);
        fx.setFloat('ringAmount', l.ring);
        fx.setFloat('ringRadius', l.ringRadius);
        fx.setFloat('lensSymmetry', l.symmetry);
        fx.setFloat('lensDistortion', l.distortion);
        fx.setFloat('lensTwist', l.twist);
        fx.setFloat('lensChroma', l.chroma);
        this.lensTint.set(l.tint[0], l.tint[1], l.tint[2]);
        fx.setColor3('lensTint', this.lensTint);
        fx.setFloat('lensSoftness', l.softness);
      };
    } catch (e) {
      this.failed = true;
      this.pass?.dispose();
      this.pass = null;
      console.warn('Black-hole lens unavailable; base universe preserved:', e);
    }
  }

  get count(): number { return this.active ? 1 : 0; }
  has(id: string): boolean { return this.active?.spec.id === id; }
  isAnomaly(id: string): boolean {
    return this.active?.spec.id === id ? this.active.anomaly : false;
  }
  profileOf(id: string): HoleProfile | null {
    return this.active?.spec.id === id ? this.active.profile : null;
  }
  /** Shadow, disk and lens are uniforms in one pass and cannot drift. */
  isLocked(id: string): boolean { return this.active?.spec.id === id; }

  setSky(next: Partial<typeof this.sky>): void {
    this.sky = { ...this.sky, ...next } as typeof this.sky;
  }

  update(eye: Vector3, holes: readonly HoleSpec[]): void {
    const scene = this.scene;
    const camera = scene?.activeCamera;
    if (!scene || !camera || this.failed) return;
    this.t += Math.max(0, scene.getEngine().getDeltaTime() / 1000);

    let nearest: HoleSpec | null = null;
    let nearestRadii = Infinity;
    for (const h of holes) {
      const away = radiiAway(eye, h);
      if (away < nearestRadii && away <= this.opts.buildWithin) {
        nearest = h;
        nearestRadii = away;
      }
    }
    if (!nearest) {
      this.active = null;
      this.screenHorizon = 0;
      return;
    }

    if (this.active?.spec.id !== nearest.id) {
      this.active = {
        spec: nearest,
        profile: holeProfile(nearest.seed),
        lens: nearest.lens ?? LENS_PROFILES[
          LENS_ORDER[(nearest.seed >>> 0) % LENS_ORDER.length]],
        anomaly: rollAnomaly(nearest.seed)
      };
    } else {
      this.active.spec = nearest;
      if (nearest.lens) this.active.lens = nearest.lens;
    }

    this.ensurePass();
    if (!this.pass) return;

    const engine = scene.getEngine();
    const width = Math.max(1, engine.getRenderWidth());
    const height = Math.max(1, engine.getRenderHeight());
    this.resolution.set(width, height);

    const toHole = nearest.position.subtract(eye);
    const distance = Math.max(1e-5, toHole.length());
    const forward = camera.getForwardRay(1).direction;
    if (Vector3.Dot(toHole, forward) <= 0) {
      this.center.set(-10, -10);
      this.screenHorizon = 0;
      return;
    }

    const viewport = camera.viewport.toGlobal(width, height);
    toRenderRef(nearest.position, this.localHole);
    const projected = Vector3.Project(
      this.localHole, Matrix.Identity(), scene.getTransformMatrix(), viewport);
    this.center.set(projected.x / width, projected.y / height);

    // Convert the physical angular radius into vertical viewport UV units.
    // This makes approach growth geometric, independent of canvas aspect.
    const angular = Math.atan2(Math.max(nearest.horizon, 1e-5), distance);
    this.screenHorizon = Math.min(.74,
      .5 * Math.tan(angular) / Math.max(.02, Math.tan(camera.fov * .5)));
  }

  dispose(): void {
    if (this.errorObserver && this.scene) {
      try { this.scene.getEngine().onEffectErrorObservable.remove(this.errorObserver); }
      catch { /* engine already disposed */ }
    }
    this.errorObserver = null;
    this.pass?.dispose();
    this.pass = null;
    this.active = null;
    this.scene = null;
    this.screenHorizon = 0;
    this.failed = false;
  }
}
