/**
 * CreatureGeometry — bodies for the things that are not rocks.
 *
 * Most throwables are lumps and a sphere is an honest picture of them. The
 * octopus and the planetary tentacle are not lumps, and drawing them as
 * spheres wasted the best items in the catalogue.
 *
 * Arms are built as tapered tubes along a curve rather than modelled, so
 * one function covers every size from a 60 km tentacle to a creature the
 * size of a small moon, and the arms can be re-curled each frame from a
 * phase value. Nothing here is a fixed mesh with baked-in poses.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { TransformNode } from '@babylonjs/core/Meshes/transformNode';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';

export interface ArmOptions {
  /** Number of points along the arm. More is smoother and costlier. */
  segments: number;
  /** Length of the arm in world units. */
  length: number;
  /** Thickness at the base. */
  baseRadius: number;
  /** How far the arm curls, radians over its length. */
  curl: number;
  /** Phase offset so arms do not move in lockstep. */
  phase: number;
}

export const DEFAULT_ARM: ArmOptions = {
  segments: 10,
  length: 4,
  baseRadius: 0.22,
  curl: 1.9,
  phase: 0
};

/**
 * The path of one arm, curling outward and downward from the body.
 *
 * A cubic falloff on thickness gives the taper a real limb has: most of the
 * narrowing happens near the tip rather than evenly along its length.
 */
export function armPath(opts: ArmOptions, time = 0): Vector3[] {
  const pts: Vector3[] = [];
  const n = Math.max(2, Math.floor(opts.segments));
  for (let i = 0; i < n; i++) {
    const t = i / (n - 1);
    // Curl increases along the arm, and breathes slowly with time.
    const a = opts.curl * t * t + Math.sin(time * 0.7 + opts.phase + t * 2.2) * 0.28;
    const reach = opts.length * t;
    pts.push(new Vector3(
      Math.sin(a) * reach * 0.55,
      -Math.sin(a * 1.15) * reach * 0.62 - t * opts.length * 0.18,
      Math.cos(a) * reach * 0.88
    ));
  }
  return pts;
}

/** Thickness at a point along the arm, tapering to a tip. */
export function armRadius(opts: ArmOptions, t: number): number {
  const u = Math.max(0, Math.min(1, t));
  return Math.max(opts.baseRadius * 0.06, opts.baseRadius * Math.pow(1 - u, 1.6));
}

export interface CreatureOptions {
  /** How many arms. */
  arms: number;
  /** Overall scale in world units. */
  size: number;
  /** Body colour. */
  color: Color3;
  /** How much it glows; deep-sea things are lit from within. */
  glow: number;
}

export const DEFAULT_OCTOPUS: CreatureOptions = {
  arms: 8,
  size: 1,
  color: new Color3(0.52, 0.16, 0.42),
  glow: 0.45
};

/**
 * Builds an octopus: a domed mantle with tapered arms beneath it.
 *
 * Returns the root node. Arms are parented to it, so the whole animal can
 * be moved and scaled as one thing by whatever is driving it.
 */
export function buildOctopus(
  scene: Scene, name: string, opts: Partial<CreatureOptions> = {}
): { root: TransformNode; arms: Mesh[]; body: Mesh; material: StandardMaterial } {
  const o = { ...DEFAULT_OCTOPUS, ...opts };
  const root = new TransformNode(name, scene);

  const mat = new StandardMaterial(name + '-mat', scene);
  mat.diffuseColor = o.color;
  // Lit from within: these live where there is no light to reflect.
  mat.emissiveColor = o.color.scale(o.glow);
  mat.specularColor = new Color3(0.35, 0.3, 0.38);
  mat.specularPower = 24;

  // The mantle: a squashed sphere reads as a head far better than a ball.
  const body = MeshBuilder.CreateSphere(
    name + '-body', { diameter: o.size * 1.6, segments: 16 }, scene);
  body.scaling.set(1, 0.78, 1.12);
  body.material = mat;
  body.parent = root;
  body.isPickable = false;

  const arms: Mesh[] = [];
  const count = Math.max(0, Math.floor(o.arms));
  for (let i = 0; i < count; i++) {
    const spin = (i / Math.max(1, count)) * Math.PI * 2;
    const spec: ArmOptions = {
      ...DEFAULT_ARM,
      length: o.size * 3.4,
      baseRadius: o.size * 0.2,
      phase: spin
    };
    const path = armPath(spec, 0);
    const arm = MeshBuilder.CreateTube(name + '-arm' + i, {
      path,
      radiusFunction: (i2: number) => armRadius(spec, i2 / (path.length - 1)),
      tessellation: 7,
      cap: 1,
      updatable: true
    }, scene);
    arm.material = mat;
    arm.parent = root;
    arm.rotation.y = spin;
    arm.position.y = -o.size * 0.42;
    arm.isPickable = false;
    (arm as any).__armSpec = spec;
    arms.push(arm);
  }

  return { root, arms, body, material: mat };
}

/**
 * Re-curls the arms for the current time.
 *
 * Tubes are rebuilt in place with `instance`, which reuses the existing
 * vertex buffers - rebuilding the mesh outright every frame would allocate
 * continuously and stutter.
 */
export function animateArms(arms: Mesh[], time: number, scene: Scene): void {
  for (const arm of arms) {
    const spec = (arm as any).__armSpec as ArmOptions | undefined;
    if (!spec) continue;
    const path = armPath(spec, time);
    try {
      MeshBuilder.CreateTube(arm.name, {
        path,
        radiusFunction: (i: number) => armRadius(spec, i / (path.length - 1)),
        tessellation: 7,
        cap: 1,
        instance: arm as any
      }, scene);
    } catch {
      // A disposed mesh mid-teardown is not worth crashing a frame over.
    }
  }
}

/**
 * A single tentacle erupting from a surface, for the 'grow' behaviour.
 *
 * Built along +Y so the caller can orient it with the surface normal and it
 * will point away from the planet without any extra maths.
 */
export function buildTentacle(
  scene: Scene, name: string, size = 1, color?: Color3
): { mesh: Mesh; material: StandardMaterial } {
  const mat = new StandardMaterial(name + '-mat', scene);
  const c = color ?? new Color3(0.44, 0.2, 0.5);
  mat.diffuseColor = c;
  mat.emissiveColor = c.scale(0.3);
  mat.specularColor = new Color3(0.3, 0.28, 0.34);

  const spec: ArmOptions = {
    segments: 12,
    length: size * 6,
    baseRadius: size * 0.34,
    curl: 1.15,
    phase: 0
  };
  // Vertical, with a lean: a tentacle standing perfectly straight looks
  // like a pole.
  const path: Vector3[] = [];
  for (let i = 0; i < spec.segments; i++) {
    const t = i / (spec.segments - 1);
    const bend = spec.curl * t * t;
    path.push(new Vector3(
      Math.sin(bend) * spec.length * 0.28,
      t * spec.length,
      Math.cos(bend * 0.7) * spec.length * 0.14 - spec.length * 0.14
    ));
  }
  const mesh = MeshBuilder.CreateTube(name, {
    path,
    radiusFunction: (i: number) => armRadius(spec, i / (path.length - 1)),
    tessellation: 8,
    cap: 1,
    updatable: true
  }, scene);
  mesh.material = mat;
  mesh.isPickable = false;
  (mesh as any).__armSpec = spec;
  return { mesh, material: mat };
}
