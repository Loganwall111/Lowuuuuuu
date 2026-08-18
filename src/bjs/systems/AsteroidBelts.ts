/**
 * AsteroidBelts — orbiting debris rings around star systems and planets.
 *
 * A belt is not scenery pinned to a transform: every rock carries its own
 * orbital radius, phase and inclination, and advances on a Keplerian curve
 * (w proportional to r^-1.5). That single rule is what makes a belt read as
 * real - the inner edge visibly shears ahead of the outer edge over time,
 * which no constant-speed ring ever does.
 *
 * Instancing matters here. A belt of 900 rocks as 900 meshes would cost 900
 * draw calls; as thin instances of one mesh it costs one. That is the
 * difference between a belt being affordable around every planet and being
 * affordable nowhere.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { Matrix, Quaternion } from '@babylonjs/core/Maths/math.vector';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { renderOrigin } from './RenderOrigin';
import type { Scene } from '@babylonjs/core/scene';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';

/** One rock's orbit. Pure data, so the maths is testable without a GPU. */
export interface Rock {
  /** Orbital radius from the belt's centre. */
  r: number;
  /** Current angle, radians. */
  phase: number;
  /** Height above the orbital plane. */
  y: number;
  /** Size of the rock. */
  scale: number;
  /** Angular velocity, radians per second. */
  omega: number;
  /** Its own tumble. */
  spin: number;
  tilt: number;
}

export interface BeltSpec {
  /** Centre of the belt in world space. */
  centre: Vector3;
  /** Inner and outer radius. */
  inner: number;
  outer: number;
  /** How many rocks. */
  count: number;
  /** Vertical half-thickness; belts are flat. */
  thickness: number;
  /** Mass of the primary, which sets the orbital speed scale. */
  mu: number;
  seed: number;
}

/** Deterministic LCG, matching the convention used elsewhere. */
export function beltRng(seed: number): () => number {
  let x = seed >>> 0;
  return () => ((x = (Math.imul(x, 1664525) + 1013904223) >>> 0) / 4294967296);
}

/**
 * Keplerian angular velocity.
 *
 * w = sqrt(mu / r^3). The exponent is the whole point: double the radius
 * and the rock takes 2.83x as long to go round, so a belt shears rather
 * than rotating rigidly.
 */
export function keplerOmega(mu: number, r: number): number {
  const rr = Math.max(r, 1e-6);
  return Math.sqrt(Math.max(mu, 0) / (rr * rr * rr));
}

/** Builds the orbital data for a belt. Engine-free and deterministic. */
export function makeBelt(spec: BeltSpec): Rock[] {
  const rnd = beltRng(spec.seed);
  const rocks: Rock[] = [];
  const n = Math.max(0, Math.floor(spec.count));
  for (let i = 0; i < n; i++) {
    // Square-root bias so the belt is denser at its inner edge, the way
    // a real debris disc is, rather than evenly spread.
    const t = Math.sqrt(rnd());
    const r = spec.inner + (spec.outer - spec.inner) * t;
    rocks.push({
      r,
      phase: rnd() * Math.PI * 2,
      // Thickness scales with radius: belts flare slightly outward.
      y: (rnd() - 0.5) * 2 * spec.thickness * (0.4 + 0.6 * t),
      scale: 0.25 + Math.pow(rnd(), 2.2) * 1.5,
      omega: keplerOmega(spec.mu, r),
      spin: (rnd() - 0.5) * 2.2,
      tilt: rnd() * Math.PI
    });
  }
  return rocks;
}

/** Advances every rock. Pure, so the shear can be asserted directly. */
export function stepBelt(rocks: Rock[], dt: number): void {
  for (const k of rocks) {
    k.phase += k.omega * dt;
    if (k.phase > Math.PI * 2) k.phase -= Math.PI * 2;
    k.tilt += k.spin * dt * 0.35;
  }
}

/** World position of a rock relative to its belt centre. */
export function rockPosition(k: Rock): [number, number, number] {
  return [Math.cos(k.phase) * k.r, k.y, Math.sin(k.phase) * k.r];
}

interface LiveBelt {
  mesh: Mesh;
  rocks: Rock[];
  centre: Vector3;
  matrices: Float32Array;
}

/**
 * Renders belts as thin instances.
 *
 * One low-poly rock mesh is reused for every asteroid in every belt around
 * a given primary, so the whole belt is a single draw call.
 */
export class AsteroidBeltSystem {
  private scene: Scene | null = null;
  private belts: LiveBelt[] = [];
  private t = 0;

  attach(scene: Scene): void {
    this.detach();
    this.scene = scene;
  }

  /** Total rocks currently simulated. */
  get rockCount(): number {
    return this.belts.reduce((s, b) => s + b.rocks.length, 0);
  }

  get beltCount(): number {
    return this.belts.length;
  }

  add(spec: BeltSpec, tint: [number, number, number] = [0.42, 0.38, 0.34]): void {
    const scene = this.scene;
    if (!scene) return;
    try {
      const rocks = makeBelt(spec);
      if (!rocks.length) return;

      // Low poly on purpose: at belt distances a rock is a few pixels, and
      // the silhouette is all that survives.
      const proto = MeshBuilder.CreatePolyhedron(
        'beltRock', { type: 1, size: 1 }, scene);
      const mat = new StandardMaterial('beltRockM', scene);
      mat.diffuseColor = new Color3(tint[0], tint[1], tint[2]);
      mat.specularColor = new Color3(0.05, 0.05, 0.05);
      proto.material = mat;
      proto.isPickable = false;
      proto.alwaysSelectAsActiveMesh = true;
      proto.metadata={...(proto.metadata??{}),floatingOriginManaged:true};

      const matrices = new Float32Array(rocks.length * 16);
      const belt: LiveBelt = { mesh: proto, rocks, centre: spec.centre.clone(), matrices };
      this.writeMatrices(belt);
      proto.thinInstanceSetBuffer('matrix', matrices, 16, false);

      this.belts.push(belt);
    } catch (e) {
      // A belt is decoration; never let it cost the frame.
      console.warn('Asteroid belt unavailable:', e);
    }
  }

  private writeMatrices(belt: LiveBelt): void {
    const q = Quaternion.Identity();
    const m = Matrix.Identity();
    const origin=renderOrigin();
    for (let i = 0; i < belt.rocks.length; i++) {
      const k = belt.rocks[i];
      const [x, y, z] = rockPosition(k);
      Quaternion.RotationYawPitchRollToRef(k.tilt, k.tilt * 0.7, 0, q);
      Matrix.ComposeToRef(
        new Vector3(k.scale, k.scale, k.scale), q,
        new Vector3(belt.centre.x+x-origin.x,belt.centre.y+y-origin.y,belt.centre.z+z-origin.z),m);
      m.copyToArray(belt.matrices, i * 16);
    }
  }

  update(dt: number): void {
    if (!this.belts.length) return;
    this.t += dt;
    for (const b of this.belts) {
      stepBelt(b.rocks, dt);
      this.writeMatrices(b);
      try {
        b.mesh.thinInstanceBufferUpdated('matrix');
      } catch { /* mesh disposed mid-frame */ }
    }
  }

  detach(): void {
    for (const b of this.belts) {
      try { b.mesh.material?.dispose(); } catch { /* gone */ }
      try { b.mesh.dispose(); } catch { /* gone */ }
    }
    this.belts = [];
    this.scene = null;
  }

  stats(): Record<string, string> {
    return {
      'Asteroid belts': String(this.beltCount),
      'Belt rocks': String(this.rockCount)
    };
  }
}
