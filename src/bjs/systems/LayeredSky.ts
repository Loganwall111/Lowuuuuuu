/**
 * LayeredSky — the three-shell background starfield.
 *
 * SpaceEngine's depth cue does not come from one sphere of stars. It comes
 * from several concentric shells at different radii: when you move, the near
 * shell slides quickly, the middle shell drifts, and the far shell is nearly
 * fixed. That differential motion is parallax, and it is the single strongest
 * signal that you are inside a volume rather than inside a painted box.
 *
 * This module is deliberately NOT a replacement for StarFieldRenderer. That
 * renderer draws real, reachable regions at their true bearings - every point
 * in it is somewhere you can actually fly to. This one draws the anonymous
 * background haze that no one will ever visit. Mixing the two would mean
 * either inventing fake destinations or throwing away real ones, so they stay
 * separate and are drawn together.
 *
 * Two rules make the sky safe:
 *
 *   - It never writes depth. A point cloud that writes depth at its shell
 *     radius silently culls everything behind it, which shows up as black
 *     blocks punched through the scene.
 *   - It never occludes. Rendering group 0, no fog, never picked.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { PointsCloudSystem } from '@babylonjs/core/Particles/pointsCloudSystem';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';

/** One concentric shell of background stars. */
export interface ShellSpec {
  name: string;
  /** How many points this shell contributes. */
  count: number;
  /** Inner and outer radius, world units. */
  inner: number;
  outer: number;
  /** Drawn point size, pixels. */
  size: number;
  /**
   * How strongly this shell follows the camera. 0 pins it to world space
   * (full parallax), 1 locks it to the eye (no parallax at all).
   *
   * The far shell is almost fully locked because a real object 10,000 units
   * away genuinely does not shift when you move 50 units, and letting it
   * drift would make the universe feel small.
   */
  lock: number;
}

/**
 * The three shells.
 *
 * Counts are weighted toward the far shell because that is what fills the
 * sky: a real night sky is mostly faint distant stars with a handful of
 * bright near ones. Sizes shrink with distance for the same reason.
 */
export const SKY_SHELLS: ShellSpec[] = [
  { name: 'core', count: 2000, inner: 100, outer: 500, size: 2.6, lock: 0.0 },
  { name: 'mid', count: 10000, inner: 500, outer: 2000, size: 1.8, lock: 0.55 },
  { name: 'far', count: 30000, inner: 2000, outer: 10000, size: 1.2, lock: 0.92 }
];

/** Total points across every shell. */
export function shellBudget(shells: ShellSpec[] = SKY_SHELLS): number {
  return shells.reduce((n, s) => n + s.count, 0);
}

/** Deterministic hash-based RNG, so a given seed always yields the same sky. */
function rng(seed: number): () => number {
  let s = seed >>> 0 || 1;
  return () => {
    s ^= s << 13; s >>>= 0;
    s ^= s >> 17;
    s ^= s << 5; s >>>= 0;
    return s / 4294967296;
  };
}

/**
 * A point uniformly distributed on a sphere.
 *
 * Naive `theta = rand * PI` clumps points at the poles. Sampling the cosine
 * of the polar angle uniformly is what actually gives an even sky.
 */
export function spherePoint(u: number, v: number): [number, number, number] {
  const cosT = 2 * u - 1;
  const sinT = Math.sqrt(Math.max(0, 1 - cosT * cosT));
  const phi = 2 * Math.PI * v;
  return [sinT * Math.cos(phi), cosT, sinT * Math.sin(phi)];
}

/**
 * Stellar colour from a temperature-like roll.
 *
 * Real star colour is a blackbody curve. Most stars are dim red dwarfs, so
 * the distribution is deliberately skewed cool - an even spread of colours
 * reads as a cartoon, not a sky.
 */
export function starColor(t: number): Color3 {
  if (t < 0.76) {
    // cool: deep orange through warm white
    const k = t / 0.76;
    return new Color3(1.0, 0.62 + k * 0.32, 0.42 + k * 0.42);
  }
  // hot: white through blue
  const k = (t - 0.76) / 0.24;
  return new Color3(1.0 - k * 0.34, 1.0 - k * 0.12, 1.0);
}

export class LayeredSky {
  private scene: Scene | null = null;
  private systems: PointsCloudSystem[] = [];
  private meshes: Mesh[] = [];
  private specs: ShellSpec[];
  private seed: number;
  /** Points currently drawn, across all shells. */
  count = 0;
  /** Shell meshes, exposed so callers can verify their render state. */
  get shellMeshes(): Mesh[] { return this.meshes.slice(); }

  constructor(specs: ShellSpec[] = SKY_SHELLS, seed = 1337) {
    this.specs = specs;
    this.seed = seed;
  }

  attach(scene: Scene): void {
    this.scene = scene;
  }

  /** Builds every shell. Safe to call once; call dispose() first to rebuild. */
  async build(): Promise<void> {
    const scene = this.scene;
    if (!scene || this.systems.length) return;

    for (let si = 0; si < this.specs.length; si++) {
      const spec = this.specs[si];
      const rand = rng(this.seed + si * 7919);
      const pcs = new PointsCloudSystem('sky_' + spec.name, spec.size, scene);

      pcs.addPoints(spec.count, (p: any) => {
        const [x, y, z] = spherePoint(rand(), rand());
        // Cube-root keeps the points volumetrically uniform between the two
        // radii instead of bunching them against the inner surface.
        const t = Math.cbrt(rand());
        const r = spec.inner + (spec.outer - spec.inner) * t;
        p.position = new Vector3(x * r, y * r, z * r);

        const c = starColor(rand());
        // Faint stars dominate: a few bright ones read as depth, an evenly
        // bright field reads as noise.
        const mag = 0.25 + Math.pow(rand(), 2.4) * 0.75;
        p.color = new Color4(c.r * mag, c.g * mag, c.b * mag, 1);
      });

      const mesh = await pcs.buildMeshAsync();
      if (mesh) {
        this.applySkyState(mesh);
        mesh.metadata = { shell: spec.name, lock: spec.lock };
        this.meshes.push(mesh);
      }
      this.systems.push(pcs);
      this.count += spec.count;
    }
  }

  /**
   * The render state that makes a point cloud a backdrop instead of an
   * occluder. Every rule here maps to a specific way the sky can break the
   * scene, so they are applied in one place rather than at each call site.
   */
  private applySkyState(mesh: Mesh): void {
    // Drawn before everything else.
    mesh.renderingGroupId = 0;
    mesh.isPickable = false;
    mesh.applyFog = false;
    // The sky surrounds the camera, so frustum culling it is pointless work
    // and can pop the whole shell out of view at shell boundaries.
    mesh.alwaysSelectAsActiveMesh = true;
    mesh.infiniteDistance = false;

    const m = mesh.material as any;
    if (!m) return;
    m.disableLighting = true;
    // THE FIX FOR THE BLACK BLOCKS. Without this the shell writes opaque
    // depth at its radius and depth-culls every real object behind it.
    m.disableDepthWrite = true;
    m.forceDepthWrite = false;
    m.needDepthPrePass = false;
    // Additive: overlapping stars brighten each other rather than fighting
    // over the pixel, and pure black contributes nothing, so there is no
    // dark quad around any point.
    m.alphaMode = 1; // ALPHA_ADD
    m.separateCullingPass = false;
    m.backFaceCulling = false;
  }

  /**
   * Slides each shell toward the eye by its lock factor, producing parallax.
   * Cheap: three transform writes, no geometry touched.
   */
  update(eye: Vector3): void {
    for (const mesh of this.meshes) {
      const lock = (mesh.metadata?.lock as number) ?? 0;
      if (lock <= 0) continue;
      mesh.position.set(eye.x * lock, eye.y * lock, eye.z * lock);
    }
  }

  dispose(): void {
    for (const p of this.systems) { try { p.dispose(); } catch { /* already gone */ } }
    this.systems = [];
    this.meshes = [];
    this.count = 0;
  }
}
