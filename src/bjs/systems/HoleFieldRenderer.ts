/**
 * HoleFieldRenderer — black holes you fly past, rendered as shaders.
 *
 * A black hole is not an object. It is a region of spacetime, and the only
 * honest way to draw one is to trace where light goes. The previous version
 * built three meshes per hole - an opaque black sphere, an orange torus and
 * a glow sphere - which was wrong in every way that matters:
 *
 *   - it could not lens, because the opaque core was drawn ON TOP of the
 *     lensed background, hiding the bend exactly at the rim where you look
 *     for it;
 *   - the "accretion disk" was a solid ring of geometry rather than a thick
 *     volume of gas, so it read as an empty orange line;
 *   - every hole in the universe was identical, because the meshes took no
 *     per-hole parameters;
 *   - you could not enter it, because a sphere has a surface.
 *
 * This renders each nearby hole with the same physics the Singularity world
 * uses: a camera-facing quad carrying a raymarcher that integrates photon
 * paths, so light genuinely wraps the horizon, the disk is volumetric, and
 * the shadow is the absence of light rather than a black-painted mesh.
 *
 * Each hole's character comes from its own seed through HoleProfiles, so no
 * two look alike and a meaningful share of them have no accretion disk at
 * all. Only holes close enough to see are built, and they are released when
 * you leave, so an endless universe does not accumulate anything.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { Effect } from '@babylonjs/core/Materials/effect';
import { Matrix } from '@babylonjs/core/Maths/math.vector';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';
import { rollAnomaly } from './BlackHoleBody';
import { holeProfile, isDiskless, type HoleProfile } from './HoleProfiles';
import { HOLE_FIELD_SHADER, registerHoleFieldShader } from '../shaders/HoleFieldShader';

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
   * one more quad.
   */
  buildWithin: number;
  /** Release it again past this many radii. Larger than buildWithin so a
   *  hole hovering at the boundary does not thrash. */
  releaseBeyond: number;
  /** Maximum holes drawn at once. */
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

/**
 * How wide the quad is, in horizon radii.
 *
 * It must comfortably cover everything the raymarcher can draw - the shadow,
 * the photon ring, the lensed sky around it and the outer disk - or the
 * effect is clipped to a visible square. The shader fades to nothing well
 * inside this bound so the quad's own edge is never seen.
 */
export const QUAD_RADII = 26;

interface LiveHole {
  id: string;
  quad: Mesh;
  mat: ShaderMaterial;
  center: Vector3;
  horizon: number;
  isAnomaly: boolean;
  profile: HoleProfile;
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
  private t = 0;

  constructor(opts: Partial<HoleFieldOptions> = {}) {
    this.opts = { ...DEFAULT_HOLEFIELD, ...opts };
  }

  attach(scene: Scene): void {
    this.scene = scene;
    registerHoleFieldShader();
  }

  /** Holes currently drawn. */
  get count(): number { return this.live.size; }

  /** True if this hole is being drawn right now. */
  has(id: string): boolean { return this.live.has(id); }

  /** Whether a built hole rolled as a fractured anomaly. */
  isAnomaly(id: string): boolean {
    return this.live.get(id)?.isAnomaly ?? false;
  }

  /** The physical profile a built hole is using. */
  profileOf(id: string): HoleProfile | null {
    return this.live.get(id)?.profile ?? null;
  }

  /** True when this hole has no accretion disk at all. */
  isDiskless(id: string): boolean {
    const h = this.live.get(id);
    return h ? isDiskless(h.profile) : false;
  }

  /**
   * Reports whether everything belonging to a hole shares one centre.
   *
   * There is only one object per hole now, so this cannot drift by
   * construction - but the check is kept because it is what the regression
   * tests assert, and it still catches a quad left behind at the origin.
   */
  isLocked(id: string, epsilon = 1e-6): boolean {
    const h = this.live.get(id);
    if (!h) return true;
    return Vector3.Distance(h.quad.position, h.center) <= epsilon;
  }

  /**
   * Builds, moves and releases holes for the current viewpoint.
   * Cheap enough to call every frame: it only touches what changed.
   */
  update(eye: Vector3, holes: HoleSpec[], dt = 0): void {
    if (!this.scene) return;
    this.t += dt;

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
        this.place(existing, h.position);
      } else {
        const built = this.build(h);
        if (built) this.live.set(h.id, built);
      }
    }

    // Face every quad at the eye and refresh its per-frame uniforms.
    for (const lh of this.live.values()) this.orient(lh, eye);
  }

  /** Points a hole's quad at the viewer and updates its uniforms. */
  private orient(lh: LiveHole, eye: Vector3): void {
    const toEye = eye.subtract(lh.center);
    const d = toEye.length();
    if (d > 1e-6) {
      // Billboard: the quad always faces the camera, so the raymarched hole
      // is never seen edge-on. The shader works in world space, so the quad
      // is only a canvas - rotating it does not rotate the physics.
      lh.quad.lookAt(eye);
    }
    lh.mat.setVector3('camPos', eye);
    lh.mat.setVector3('holePos', lh.center);
    lh.mat.setFloat('time', this.t);
  }

  /** Moves an existing hole. One call, one position: nothing can separate. */
  private place(lh: LiveHole, to: Vector3): void {
    lh.center.copyFrom(to);
    lh.quad.position.copyFrom(to);
  }

  private build(spec: HoleSpec): LiveHole | null {
    const scene = this.scene;
    if (!scene) return null;

    const hz = Math.max(spec.horizon, 1e-6);

    // The anomaly roll is per-hole and derived from that hole's own seed,
    // so it is stable across visits and cannot leak into other holes.
    const isAnomaly = rollAnomaly(spec.seed);
    // Everything else about how this hole looks also comes from its seed,
    // which is what makes each one different.
    const profile = holeProfile(spec.seed);

    const quad = MeshBuilder.CreatePlane('bhQuad_' + spec.id, {
      size: hz * QUAD_RADII * 2
    }, scene);

    const mat = new ShaderMaterial(
      'bhQuadM_' + spec.id, scene, HOLE_FIELD_SHADER, {
        attributes: ['position', 'uv'],
        uniforms: [
          'worldViewProjection', 'world', 'camPos', 'holePos', 'time',
          'rs', 'quadRadius', 'diskInner', 'diskOuter', 'diskThickness',
          'diskBright', 'diskTilt', 'spin', 'dopplerAmt', 'diskTemp',
          'turbulence', 'horizonCover'
        ]
      });

    mat.setFloat('rs', hz);
    mat.setFloat('quadRadius', hz * QUAD_RADII);
    // The profile speaks in Schwarzschild radii; the shader wants world units.
    mat.setFloat('diskInner', profile.diskInner * hz);
    mat.setFloat('diskOuter', profile.diskOuter * hz);
    mat.setFloat('diskThickness', profile.diskThickness * hz);
    mat.setFloat('diskBright', profile.diskBright);
    mat.setFloat('diskTilt', profile.diskTilt);
    mat.setFloat('spin', profile.spin);
    mat.setFloat('dopplerAmt', profile.doppler);
    mat.setFloat('diskTemp', profile.temperature);
    mat.setFloat('turbulence', profile.turbulence);
    // A fractured anomaly pulls its shadow in tight; a standard hole covers
    // the disk's inner edge.
    mat.setFloat('horizonCover', isAnomaly ? 0.42 : 1.06);

    // Additive over whatever is already drawn, and never occluding it: the
    // hole must not punch a hole in the scene, it must bend what is behind.
    mat.backFaceCulling = false;
    mat.alpha = 0.999;
    mat.alphaMode = 2; // ALPHA_COMBINE: the shader carries its own coverage
    mat.needAlphaBlending = () => true;
    mat.disableDepthWrite = true;
    mat.fogEnabled = false;

    quad.material = mat;
    quad.position.copyFrom(spec.position);
    quad.isPickable = false;
    quad.applyFog = false;
    // Drawn after opaque geometry so it composites over the sky behind it.
    quad.renderingGroupId = 1;

    return {
      id: spec.id,
      quad,
      mat,
      center: spec.position.clone(),
      horizon: hz,
      isAnomaly,
      profile
    };
  }

  private destroy(lh: LiveHole): void {
    try { lh.mat.dispose(); } catch { /* already gone */ }
    try { lh.quad.dispose(); } catch { /* already gone */ }
  }

  dispose(): void {
    for (const lh of this.live.values()) this.destroy(lh);
    this.live.clear();
  }
}
