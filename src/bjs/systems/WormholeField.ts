/**
 * WormholeField — a universe threaded with traversable wormholes.
 *
 * Where a black hole is an ending, a wormhole is a shortcut: two points in
 * space sewn together, or a door into a dimension. This places a field of
 * them throughout the universe and renders each one its own way:
 *
 *   - bridge        an Einstein-Rosen pair. A see-through, gravitationally
 *                   lensed disc at each end; flying in at one end ejects you
 *                   out the far end, light-years away, speed preserved.
 *   - ring          the same bridge, dressed with a machined torus frame -
 *                   an engineered gate rather than a natural one.
 *   - interstellar  a glowing golden sphere wrapped in an orbiting halo,
 *                   like the tesseract in *Interstellar*. Crossing it hands
 *                   you into a generated dimension rather than a twin point.
 *
 * Placement and type are deterministic from the universe seed, so the same
 * wormholes are always in the same places. Rendering reuses PortalSystem's
 * lensed see-through shader; only the dressing and the transit rules live
 * here.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { TransformNode } from '@babylonjs/core/Meshes/transformNode';
import type { Scene } from '@babylonjs/core/scene';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import { PortalSystem } from './PortalSystem';
import { generateDimension } from './DimensionSystem';

export type WormholeType = 'bridge' | 'ring' | 'interstellar' | 'cosmic-tear';

export interface WormholeSpec {
  id: string;
  type: WormholeType;
  /** Mouth A (and, for bridges, mouth B). */
  ax: number; ay: number; az: number;
  bx: number; by: number; bz: number;
  radius: number;
  /** Seed for the destination sky / dimension. */
  seed: number;
}

export const WORMHOLE_COUNT = 8;

/** Deterministic 0..1 hash. */
function hash01(seed: number): number {
  let h = seed >>> 0 || 1;
  h = Math.imul(h ^ (h >>> 16), 2246822519) >>> 0;
  h = Math.imul(h ^ (h >>> 13), 3266489917) >>> 0;
  return ((h ^ (h >>> 16)) >>> 0) / 4294967296;
}

/**
 * The wormhole field for a universe seed.
 *
 * Bridges and rings come in pairs whose two ends sit far apart, so crossing
 * one genuinely shortcuts a huge distance. Interstellar wormholes are a
 * single mouth that opens onto a dimension. Deterministic: same seed, same
 * field, forever.
 */
export function wormholeSpecs(seed: number, count = WORMHOLE_COUNT): WormholeSpec[] {
  const out: WormholeSpec[] = [];
  // One wormhole is always an interstellar gate, so every universe has the
  // rare find. Which one it is comes from the seed, so it is still a hunt.
  const gateIndex = Math.floor(hash01((seed >>> 0) ^ 0x9e3779b9) * count) % count;
  let i = 0;
  while (out.length < count) {
    const s = (seed ^ Math.imul(++i, 2654435761)) >>> 0;
    // Interstellar wormholes are rarer than the engineered ones; the one
    // guaranteed gate above is the exception.
    const roll = hash01(s + 1);
    const type: WormholeType =
      out.length === gateIndex ? 'interstellar'
        // Cosmic Tears are genuinely exceptional: no guaranteed instance,
        // and only a 2.5% roll among an already sparse wormhole field.
        : roll < 0.025 ? 'cosmic-tear'
          : roll < 0.18 ? 'interstellar'
            : roll < 0.36 ? 'ring'
              : 'bridge';

    const place = (off: number): [number, number, number] => {
      const r = 500 + hash01(s + off) * 5500;
      const u = hash01(s + off + 7) * 2 - 1;
      const phi = hash01(s + off + 13) * Math.PI * 2;
      const w = Math.sqrt(Math.max(0, 1 - u * u));
      return [w * Math.cos(phi) * r, u * r * 0.4, w * Math.sin(phi) * r];
    };

    const [ax, ay, az] = place(2);
    if (type === 'interstellar' || type === 'cosmic-tear') {
      out.push({
        id: 'wh' + out.length, type, ax, ay, az,
        bx: ax, by: ay, bz: az, radius: 7 + hash01(s + 3) * 5, seed: s
      });
    } else {
      const [bx, by, bz] = place(4);
      out.push({
        id: 'wh' + out.length, type, ax, ay, az, bx, by, bz,
        radius: 5 + hash01(s + 3) * 5, seed: s
      });
    }
  }
  return out;
}

/** Where a mouth sits and which direction it faces, for the renderer. */
export function wormholeFacing(a: [number, number, number], b: [number, number, number]): Vector3 {
  const v = new Vector3(b[0] - a[0], b[1] - a[1], b[2] - a[2]);
  return v.lengthSquared() > 1e-6 ? v.normalize() : new Vector3(0, 0, 1);
}

export class WormholeField {
  private scene: Scene | null = null;
  private portals: PortalSystem | null = null;
  private specs: WormholeSpec[] = [];
  private frames: TransformNode[] = [];
  private orbs: TransformNode[] = [];
  private built = false;

  get count(): number { return this.specs.length; }

  attach(scene: Scene): void {
    this.scene = scene;
  }

  /** Builds the field (portals + dressing). Safe to call once per scene. */
  build(seed: number): void {
    const scene = this.scene;
    if (!scene || this.built) return;
    this.specs = wormholeSpecs(seed);
    this.portals = new PortalSystem(scene);

    for (const spec of this.specs) {
      const a: [number, number, number] = [spec.ax, spec.ay, spec.az];
      const b: [number, number, number] = [spec.bx, spec.by, spec.bz];

      if (spec.type === 'interstellar' || spec.type === 'cosmic-tear') {
        // A one-sided tear opens onto a deterministic procedural dimension.
        // Cosmic Tears receive supercritical mass so the portal shader folds
        // the destination into repeated, infinitely receding lens images.
        const facing = new Vector3(0, 0, 1);
        const tear = this.portals.createTear(
          new Vector3(spec.ax, spec.ay, spec.az), facing, spec.radius,
          generateDimension(spec.seed >>> 0, 0));
        if (spec.type === 'cosmic-tear') {
          tear.lensStrength = 4.6;
          tear.throatMass = 2.8;
        } else {
          // The tesseract dressing belongs only to the ordinary gate. A
          // Cosmic Tear is entirely lensing, with no manufactured frame.
          this.buildOrb(scene, spec);
        }
        continue;
      }

      // Bridge or ring: a linked pair.
      this.portals.createWormhole(
        new Vector3(spec.ax, spec.ay, spec.az),
        new Vector3(spec.bx, spec.by, spec.bz),
        spec.radius, 1.4);

      if (spec.type === 'ring') {
        this.buildRing(scene, spec, a, b);
      }
    }

    this.built = true;
  }

  /** A glowing golden sphere with an orbiting halo - the Interstellar gate. */
  private buildOrb(scene: Scene, spec: WormholeSpec): void {
    const root = new TransformNode('wormholeOrb' + spec.id, scene);
    root.position.set(spec.ax, spec.ay, spec.az);
    const r = spec.radius;

    const orb = MeshBuilder.CreateSphere('whOrb' + spec.id,
      { diameter: r * 1.1, segments: 24 }, scene);
    orb.parent = root;
    const om = new StandardMaterial('whOrbM' + spec.id, scene);
    om.emissiveColor = new Color3(1.0, 0.82, 0.45);
    om.diffuseColor = Color3.Black();
    om.specularColor = Color3.Black();
    om.disableLighting = true;
    om.alpha = 0.92;
    om.backFaceCulling = false;
    orb.material = om;

    const halo = MeshBuilder.CreateTorus('whHalo' + spec.id,
      { diameter: r * 2.4, thickness: r * 0.09, tessellation: 40 }, scene);
    halo.parent = root;
    const hm = new StandardMaterial('whHaloM' + spec.id, scene);
    hm.emissiveColor = new Color3(0.5, 0.85, 1.0);
    hm.diffuseColor = Color3.Black();
    hm.specularColor = Color3.Black();
    hm.disableLighting = true;
    hm.backFaceCulling = false;
    halo.material = hm;

    this.orbs.push(root);
  }

  /** A machined torus frame around a bridge pair. */
  private buildRing(
    scene: Scene, spec: WormholeSpec,
    a: [number, number, number], b: [number, number, number]
  ): void {
    const pairs: Array<[[number, number, number], [number, number, number]]> =
      [[a, b], [b, a]];
    for (const [x, other] of pairs) {
      const tag = other === b ? 'a' : 'b';
      const root = new TransformNode('whRing' + spec.id + tag, scene);
      root.position.set(x[0], x[1], x[2]);
      const facing = wormholeFacing(x, other);
      // Orient the torus flat against the mouth.
      const up = Math.abs(facing.y) > 0.95 ? new Vector3(0, 0, 1) : new Vector3(0, 1, 0);
      const right = Vector3.Cross(up, facing).normalize();
      const trueUp = Vector3.Cross(facing, right).normalize();
      root.rotation = Vector3.RotationFromAxis(right, trueUp, facing);

      const ring = MeshBuilder.CreateTorus('whRingMesh' + spec.id + tag,
        { diameter: spec.radius * 2.6, thickness: spec.radius * 0.12, tessellation: 48 }, scene);
      ring.parent = root;
      const rm = new StandardMaterial('whRingM' + spec.id, scene);
      rm.emissiveColor = new Color3(0.35, 0.75, 1.0);
      rm.diffuseColor = Color3.Black();
      rm.specularColor = Color3.Black();
      rm.disableLighting = true;
      rm.backFaceCulling = false;
      ring.material = rm;
      this.frames.push(root);
    }
  }

  /** Advances portal animation and returns the nearest mouth. */
  update(dt: number, eye: Vector3): void {
    this.portals?.update(dt, eye);
    for (const orb of this.orbs) {
      orb.rotation.y += dt * 0.4;
      orb.rotation.x += dt * 0.13;
    }
    for (const fr of this.frames) fr.rotation.y += dt * 0.25;
  }

  /**
   * Tries to move the player through a wormhole.
   *
   * Returns 'moved' when a bridge/ring teleported them (their position and
   * velocity are already rewritten), 'dimension' when they entered an
   * interstellar gate (the caller should load `seed`/`depth`), or null.
   */
  tryTransit(
    player: { position: Vector3; velocity: Vector3; radius: number }
  ): { kind: 'moved' | 'dimension'; seed?: number; depth?: number } | null {
    const portals = this.portals;
    if (!portals) return null;

    // Interstellar spheres are entered by proximity, not by crossing a disc.
    for (let i = 0; i < this.specs.length; i++) {
      const spec = this.specs[i];
      if (spec.type !== 'interstellar') continue;
      const d = Math.hypot(
        player.position.x - spec.ax,
        player.position.y - spec.ay,
        player.position.z - spec.az);
      if (d <= spec.radius * 1.6) {
        return { kind: 'dimension', seed: spec.seed >>> 0, depth: 3 };
      }
    }

    const used = portals.tryTransit({
      position: player.position,
      velocity: player.velocity,
      key: 'player'
    });
    if (!used) return null;
    if (used.kind === 'tear') {
      const dest = portals.lastDestination;
      return { kind: 'dimension', seed: dest?.seed ?? 1, depth: dest?.depth ?? 0 };
    }
    return { kind: 'moved' };
  }

  stats(): Record<string, string> {
    return { 'Wormholes': this.built ? String(this.specs.length) : 'off' };
  }

  dispose(): void {
    this.portals?.closeAll();
    this.portals = null;
    for (const f of this.frames) f.dispose(false, true);
    for (const o of this.orbs) o.dispose(false, true);
    this.frames = [];
    this.orbs = [];
    this.specs = [];
    this.built = false;
    this.scene = null;
  }
}
