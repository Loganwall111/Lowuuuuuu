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
import {
  MILKY_WAY, galaxyStar, observerPosition, projectToShell,
  sampleNebulaPoint, nebulaColor, projectToShell as projectGas
} from './GalaxyShape';

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
   * Draw this shell as a real galaxy - logarithmic arms, a thin disc and a
   * blazing core - rather than an even scatter.
   *
   * An evenly-scattered shell is statistically identical in every
   * direction, which is exactly why the outer sky "drops into a repeating
   * pattern": there is nothing in it to recognise. Only the far shell gets
   * this, because the near shells are supposed to be local stars.
   */
  galaxy?: boolean;
  /** Points of glowing interstellar gas to mix into this shell. */
  gas?: number;
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
  // These shells exist for ONE reason the sky dome cannot serve: parallax.
  // A dome is infinitely far away, so it cannot shift as you fly. Points at
  // a finite radius do, and that motion is the whole depth cue of space.
  //
  // They used to carry the galaxy too - band, arms and gas - which put a
  // SECOND Milky Way in front of the dome's. 51,000 additive points drew
  // after the dome in the same group and washed its dust lanes and nebulae
  // out to a uniform sparkle, which is exactly the "just a bunch of stars,
  // no fog" the sky kept looking like. The galaxy now lives only in the
  // dome, where it can do absorption; these shells are foreground stars and
  // nothing else, at a fraction of the count and brightness.
  { name: 'core', count: 900, inner: 100, outer: 500, size: 2.2, lock: 0.0 },
  { name: 'mid', count: 2600, inner: 500, outer: 2000, size: 1.5, lock: 0.55 },
  // Outer radius deliberately sits inside the camera's far plane (maxZ is
  // 4000); a shell drawn beyond it is clipped. The depth cue comes from the
  // per-shell parallax lock, not from raw distance.
  { name: 'far', count: 5200, inner: 2000, outer: 3800, size: 1.1, lock: 0.92 }
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
  private generation = 0;
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
    const generation = ++this.generation;

    for (let si = 0; si < this.specs.length; si++) {
      const spec = this.specs[si];
      const rand = rng(this.seed + si * 7919);
      const pcs = new PointsCloudSystem('sky_' + spec.name, spec.size, scene);

      const observer = observerPosition(MILKY_WAY);

      if (spec.galaxy) {
        // A real galaxy, seen from inside it. The band across the sky, the
        // bright core in one direction and the empty galactic poles are all
        // consequences of the geometry rather than painted decoration.
        pcs.addPoints(spec.count, (p: any) => {
          const star = galaxyStar(rand, MILKY_WAY);
          const proj = projectToShell(star, observer, spec.inner, spec.outer, MILKY_WAY);
          p.position = new Vector3(proj.x, proj.y, proj.z);

          const c = starColor(rand());
          // The bulge is hot and crowded; the halo is old and red. Using the
          // structural class here is what makes the core read as a core.
          const tone = star.kind === 'bulge'
            ? new Color3(Math.min(1, c.r * 1.05), c.g, Math.min(1, c.b * 1.12))
            : c;
          const mag = star.bright * (0.3 + Math.pow(rand(), 2.0) * 0.7);
          p.color = new Color4(tone.r * mag, tone.g * mag, tone.b * mag, 1);
        });
      } else {
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
      }

      if (spec.gas && spec.gas > 0) {
        // Interstellar gas, sampled where the density field is actually
        // thick. Scattering points evenly and fading them by density would
        // give an even haze; rejection sampling gives clouds with edges.
        //
        // Points that miss are simply parked far off the shell's inner
        // radius rather than skipped, because a PointsCloudSystem needs a
        // fixed count - so the miss budget is bounded and cheap.
        pcs.addPoints(spec.gas, (p: any) => {
          let hit = null;
          for (let tries = 0; tries < 12 && !hit; tries++) {
            hit = sampleNebulaPoint(rand, MILKY_WAY, 0.16);
          }
          if (!hit) {
            // No cloud found: hide this point at the origin with zero alpha.
            p.position = new Vector3(0, 0, 0);
            p.color = new Color4(0, 0, 0, 0);
            return;
          }
          const proj = projectGas(
            { x: hit.x, y: hit.y, z: hit.z, kind: 'arm', bright: 1 },
            observer, spec.inner, spec.outer, MILKY_WAY);
          p.position = new Vector3(proj.x, proj.y, proj.z);
          const [r, g, b] = nebulaColor(hit.density, hit.x, hit.y, hit.z, MILKY_WAY);
          // Gas is dim and additive: it must tint the sky, never wall it off.
          p.color = new Color4(r, g, b, Math.min(0.5, hit.density * 0.55));
        });
      }

      const mesh = await pcs.buildMeshAsync();
      if (generation !== this.generation || scene !== this.scene) {
        try { mesh?.dispose(); pcs.dispose(); } catch { /* superseded build */ }
        return;
      }
      if (mesh) {
        this.applySkyState(mesh);
        mesh.metadata = { shell: spec.name, lock: spec.lock };
        this.meshes.push(mesh);
      }
      this.systems.push(pcs);
      this.count += spec.count + (spec.gas ?? 0);
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

    // Additive blending, so overlapping stars brighten each other and pure
    // black contributes nothing - no dark quad around any point.
    //
    // Setting alphaMode ALONE DOES NOT WORK, and that was the bug behind the
    // shards coming back. Babylon only takes the alpha path when
    // needAlphaBlending() is true, and StandardMaterial returns false while
    // alpha === 1, so the blend mode was silently ignored and every point
    // drew as an opaque quad. Verified against a real NullEngine material:
    //   alpha=1,     alphaMode=1 -> needAlphaBlending() falsy  (ignored)
    //   alpha=0.999, alphaMode=1 -> needAlphaBlending() true   (applied)
    // Nudging alpha off 1.0 is what actually arms the blender; the visual
    // difference of 0.001 is nil because the mode is additive anyway.
    m.alpha = 0.999;
    m.alphaMode = 1; // Constants.ALPHA_ADD
    m.separateCullingPass = false;
    m.backFaceCulling = false;
  }

  /**
   * Slides each shell toward the eye by its lock factor, producing parallax.
   * Cheap: three transform writes, no geometry touched.
   */
  update(eye: Vector3): void {
    const extreme = Math.max(Math.abs(eye.x), Math.abs(eye.y), Math.abs(eye.z)) > 1e6;
    for (const mesh of this.meshes) {
      const lock = (mesh.metadata?.lock as number) ?? 0;
      if (lock <= 0) continue;
      if (extreme) {
        // At intergalactic coordinates Float32 cannot represent the shell's
        // hundreds-of-unit radius next to the camera's hundreds of millions.
        // Translation-free mode trades tiny parallax for a guaranteed sky.
        mesh.infiniteDistance = true;
        mesh.position.setAll(0);
      } else {
        mesh.infiniteDistance = false;
        mesh.position.set(eye.x * lock, eye.y * lock, eye.z * lock);
      }
    }
  }

  dispose(): void {
    this.generation++;
    for (const p of this.systems) { try { p.dispose(); } catch { /* already gone */ } }
    this.systems = [];
    this.meshes = [];
    this.count = 0;
  }
}
