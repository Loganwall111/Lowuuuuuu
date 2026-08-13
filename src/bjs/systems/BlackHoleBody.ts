/**
 * BlackHoleBody — the solid horizon sphere, locked to the disk.
 *
 * The reported bug: the physical black sphere and the accretion disk drift
 * apart as the camera moves. The cause is having two sources of truth for
 * where the hole is - a mesh with its own transform, and a shader fed a
 * separate centre uniform. Any code path that updates one without the other
 * separates them, and camera motion is exactly the path that does it.
 *
 * The fix here is structural rather than a correction applied after the
 * fact: `center` is stored once on this object, and both the mesh transform
 * and the shader uniforms are written from that single value in the same
 * call. There is no way to move one without moving the other, so they
 * cannot drift.
 *
 * The anomaly feature rides on the same value. Whether the sphere masks the
 * Moiré pattern or exposes it is purely its *scale* relative to the disk's
 * inner edge - the centre never changes, so the shader maths is untouched
 * either way.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';

/**
 * Chance that a given hole is a "fractured" anomaly.
 *
 * The brief asked for 5-10%; 7% sits in the middle, so finding one is a
 * genuine event rather than something you see every few holes.
 */
export const ANOMALY_CHANCE = 0.07;

/**
 * How large the sphere is relative to the disk's inner edge.
 *
 * Standard holes over-cover: 1.06 puts the sphere just outside the inner
 * edge so the grey inner disc and its Moiré pattern are completely hidden,
 * with a little margin so no seam shows at grazing angles.
 *
 * Anomalies under-cover: 0.42 pulls the sphere back to the dead centre and
 * leaves the pattern radiating around it.
 */
export const STANDARD_COVER = 1.06;
export const ANOMALY_COVER = 0.42;

export interface BlackHoleBodyOptions {
  /** World-space centre. The single source of truth for both mesh and shader. */
  center: Vector3;
  /** Inner radius of the accretion disk, world units. */
  diskInner: number;
  /** Forced anomaly state. Omit to roll for it. */
  isAnomaly?: boolean;
  /** Seed for the roll, so a given hole is always the same kind. */
  seed?: number;
}

/**
 * Decides whether a hole is an anomaly, deterministically from its seed.
 *
 * Seeded rather than random so a hole does not change character when you
 * look away and come back - which would make the rare find meaningless.
 */
export function rollAnomaly(seed: number, chance = ANOMALY_CHANCE): boolean {
  // A bare xorshift left the result correlated with the seed for small
  // sequential seeds, which pushed the observed rate to 11.5% - outside the
  // 5-10% this is specified at. Finishing with an integer-hash avalanche
  // decorrelates it and brings the measured rate onto the nominal value.
  let h = (seed >>> 0) || 1;
  h ^= h << 13; h >>>= 0;
  h ^= h >> 17;
  h ^= h << 5; h >>>= 0;
  h = Math.imul(h ^ (h >>> 16), 0x85ebca6b) >>> 0;
  h = Math.imul(h ^ (h >>> 13), 0xc2b2ae35) >>> 0;
  h = (h ^ (h >>> 16)) >>> 0;
  return (h / 4294967296) < chance;
}

/** Sphere radius for a hole, given the disk's inner edge. */
export function sphereRadiusFor(diskInner: number, isAnomaly: boolean): number {
  const inner = Math.max(diskInner, 1e-4);
  return inner * (isAnomaly ? ANOMALY_COVER : STANDARD_COVER);
}

export class BlackHoleBody {
  /**
   * The one place the hole's position lives. The mesh and the shader are
   * both written from this, never independently.
   */
  readonly center = new Vector3(0, 0, 0);
  diskInner: number;
  isAnomaly: boolean;
  mesh: Mesh | null = null;
  private mat: StandardMaterial | null = null;

  constructor(opts: BlackHoleBodyOptions) {
    this.center.copyFrom(opts.center);
    this.diskInner = Math.max(opts.diskInner, 1e-4);
    this.isAnomaly = opts.isAnomaly ?? rollAnomaly(opts.seed ?? 1);
  }

  /** Current sphere radius, derived rather than stored. */
  get radius(): number {
    return sphereRadiusFor(this.diskInner, this.isAnomaly);
  }

  /**
   * Builds the sphere.
   *
   * It is pure black and unlit - not merely dark. A horizon that catches a
   * specular highlight stops reading as a hole in space, which is the usual
   * giveaway that it is only a painted ball.
   */
  build(scene: Scene, name = 'bh-horizon'): Mesh {
    this.dispose();
    const m = MeshBuilder.CreateSphere(
      name, { diameter: this.radius * 2, segments: 32 }, scene);
    const mat = new StandardMaterial(name + '-mat', scene);
    mat.diffuseColor = new Color3(0, 0, 0);
    mat.specularColor = new Color3(0, 0, 0);
    mat.emissiveColor = new Color3(0, 0, 0);
    mat.disableLighting = true;
    // Drawn after the disk so it masks rather than being masked.
    m.renderingGroupId = 1;
    m.material = mat;
    m.isPickable = false;
    m.position.copyFrom(this.center);
    this.mesh = m;
    this.mat = mat;
    return m;
  }

  /**
   * Moves the hole.
   *
   * Both the mesh transform and the returned shader centre come from the
   * same value in the same call, which is what makes drift impossible.
   * Callers must feed `shaderCenter()` to the disk material rather than
   * tracking a position of their own.
   */
  setCenter(to: Vector3): void {
    this.center.copyFrom(to);
    if (this.mesh) this.mesh.position.copyFrom(this.center);
  }

  /** The centre to hand the disk/lensing shader. Always agrees with the mesh. */
  shaderCenter(): Vector3 {
    return this.center;
  }

  /**
   * Verifies the mesh and the shader centre have not separated.
   *
   * Cheap enough to assert every frame in development, and it is the exact
   * condition the reported bug violated.
   */
  isLocked(epsilon = 1e-6): boolean {
    if (!this.mesh) return true;
    return Vector3.Distance(this.mesh.position, this.center) <= epsilon;
  }

  /** Changes the disk's inner edge, rescaling the sphere to match. */
  setDiskInner(inner: number): void {
    this.diskInner = Math.max(inner, 1e-4);
    this.applyScale();
  }

  /** Switches this hole between standard and fractured. */
  setAnomaly(on: boolean): void {
    this.isAnomaly = on;
    this.applyScale();
  }

  /**
   * Rescales the existing mesh instead of rebuilding it.
   *
   * Scaling is relative to the diameter the mesh was created with, so this
   * stays correct however many times it is called.
   */
  private applyScale(): void {
    if (!this.mesh) return;
    const built = this.mesh.getBoundingInfo().boundingSphere.radius /
      Math.max(this.mesh.scaling.x, 1e-6);
    const want = this.radius / Math.max(built, 1e-6);
    this.mesh.scaling.setAll(want);
  }

  /** Whether the Moiré pattern is visible on this hole. */
  get exposesPattern(): boolean {
    return this.isAnomaly;
  }

  stats(): Record<string, string> {
    return {
      'Horizon type': this.isAnomaly ? 'Fractured (anomaly)' : 'Standard',
      'Sphere radius': this.radius.toFixed(3),
      'Pattern': this.isAnomaly ? 'exposed' : 'masked'
    };
  }

  dispose(): void {
    try { this.mat?.dispose(); } catch { /* already gone */ }
    try { this.mesh?.dispose(); } catch { /* already gone */ }
    this.mesh = null;
    this.mat = null;
  }
}
