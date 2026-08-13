/**
 * BeamSystem — giant energy beams that genuinely affect the simulation.
 *
 * A beam is not a decal: it performs a real ray/sphere intersection against
 * every body each frame and then applies a physical effect (heat, impulse,
 * attraction, repulsion, freezing, fracture). Because the effect is expressed
 * as force + heat on a generic body, every beam type works on every object in
 * the catalogue - planets, ducks, moons, black holes - with no special cases.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';

export type BeamKind =
  | 'laser' | 'plasma' | 'heat' | 'freeze'
  | 'gravity' | 'tractor' | 'repulsor' | 'push' | 'disintegrate';

export interface BeamDef {
  kind: BeamKind;
  label: string;
  glyph: string;
  color: [number, number, number];
  /** Energy per second delivered to whatever it touches. */
  power: number;
  /** Positive pulls toward the emitter, negative pushes away. */
  pull: number;
  /** Fraction of energy that becomes heat. */
  heat: number;
  note: string;
}

export const BEAMS: Record<BeamKind, BeamDef> = {
  laser:        { kind: 'laser',        label: 'Laser',        glyph: '🔴', color: [1.0, 0.15, 0.12], power: 60,  pull: 0,     heat: 1.0, note: 'Cuts and burns' },
  plasma:       { kind: 'plasma',       label: 'Plasma',       glyph: '🟣', color: [0.75, 0.35, 1.0], power: 90,  pull: -0.4,  heat: 1.0, note: 'Melts and blasts' },
  heat:         { kind: 'heat',         label: 'Heat Ray',     glyph: '🟠', color: [1.0, 0.55, 0.1],  power: 45,  pull: 0,     heat: 1.6, note: 'Boils oceans' },
  freeze:       { kind: 'freeze',       label: 'Freeze Ray',   glyph: '🔵', color: [0.45, 0.85, 1.0], power: 30,  pull: 0,     heat: -1.4, note: 'Cools and slows' },
  gravity:      { kind: 'gravity',      label: 'Gravity Beam', glyph: '🟢', color: [0.4, 1.0, 0.6],   power: 20,  pull: 2.4,   heat: 0,   note: 'Warps local gravity' },
  tractor:      { kind: 'tractor',      label: 'Tractor Beam', glyph: '🔗', color: [0.35, 0.8, 1.0],  power: 15,  pull: 3.2,   heat: 0,   note: 'Pulls anything' },
  repulsor:     { kind: 'repulsor',     label: 'Repulsor',     glyph: '💨', color: [1.0, 0.8, 0.3],   power: 15,  pull: -3.2,  heat: 0,   note: 'Pushes anything' },
  push:         { kind: 'push',         label: 'Impulse',      glyph: '👊', color: [0.9, 0.9, 1.0],   power: 40,  pull: -6.0,  heat: 0.1, note: 'Punches objects away' },
  disintegrate: { kind: 'disintegrate', label: 'Disintegrator', glyph: '☠', color: [1.0, 0.25, 0.85], power: 240, pull: 0,     heat: 2.5, note: 'Deletes matter' }
};

export interface BeamTarget {
  pos: Vector3;
  vel: Vector3;
  radius: number;
  mass: number;
  /** Accumulated heat 0..1; at 1 the body is destroyed. */
  heat: number;
  alive: boolean;
}

export interface BeamHit {
  target: BeamTarget;
  energy: number;
  point: Vector3;
}

/** A single active beam instance. */
export class Beam {
  def: BeamDef;
  origin: Vector3;
  direction: Vector3;
  width: number;
  range: number;
  ttl: number;
  private core: Mesh | null = null;
  private glow: Mesh | null = null;
  private scene: Scene;

  constructor(scene: Scene, def: BeamDef, origin: Vector3, direction: Vector3,
              width = 1.2, range = 400, ttl = 1.6) {
    this.scene = scene;
    this.def = def;
    this.origin = origin.clone();
    this.direction = direction.normalize();
    this.width = width;
    this.range = range;
    this.ttl = ttl;
    this.buildMesh();
  }

  private buildMesh(): void {
    const c = this.def.color;

    const make = (diameter: number, alpha: number, emissive: number): Mesh => {
      const m = MeshBuilder.CreateCylinder('beam', {
        height: this.range, diameter, tessellation: 16
      }, this.scene);
      const mat = new StandardMaterial('beamMat', this.scene);
      mat.emissiveColor = new Color3(c[0] * emissive, c[1] * emissive, c[2] * emissive);
      mat.diffuseColor = Color3.Black();
      mat.specularColor = Color3.Black();
      mat.alpha = alpha;
      mat.disableLighting = true;
      mat.backFaceCulling = false;
      m.material = mat;
      m.isPickable = false;
      return m;
    };

    this.core = make(this.width, 0.95, 1.7);
    this.glow = make(this.width * 3.4, 0.24, 0.9);
    this.orient();
  }

  private orient(): void {
    const mid = this.origin.add(this.direction.scale(this.range * 0.5));
    for (const m of [this.core, this.glow]) {
      if (!m) continue;
      m.position.copyFrom(mid);
      // align the cylinder's +Y axis with the beam direction
      const up = new Vector3(0, 1, 0);
      const axis = Vector3.Cross(up, this.direction);
      const angle = Math.acos(Math.max(-1, Math.min(1, Vector3.Dot(up, this.direction))));
      if (axis.lengthSquared() > 1e-8) {
        m.rotationQuaternion = null;
        m.rotation = Vector3.Zero();
        m.rotate(axis.normalize(), angle);
      }
    }
  }

  aim(origin: Vector3, direction: Vector3): void {
    this.origin.copyFrom(origin);
    this.direction = direction.normalize();
    this.orient();
  }

  /**
   * Applies the beam to every target it intersects and returns the hits.
   * Uses a real ray/sphere test, so grazing shots miss and thick beams
   * catch more objects.
   */
  apply(targets: BeamTarget[], dt: number): BeamHit[] {
    const hits: BeamHit[] = [];
    const d = this.direction;

    for (const t of targets) {
      if (!t.alive) continue;
      const oc = t.pos.subtract(this.origin);
      const along = Vector3.Dot(oc, d);
      if (along < 0 || along > this.range) continue;           // behind or beyond
      const closest = this.origin.add(d.scale(along));
      const miss = Vector3.Distance(closest, t.pos);
      if (miss > t.radius + this.width * 0.5) continue;        // ray misses

      // energy falls off with distance and with how far off-axis the hit is
      const falloff = 1 - (along / this.range) * 0.55;
      const centrality = 1 - Math.min(1, miss / Math.max(t.radius, 0.001)) * 0.5;
      const energy = this.def.power * falloff * centrality * dt;

      // ---- heat ----
      if (this.def.heat !== 0) {
        t.heat = Math.max(0, Math.min(1.4, t.heat + (energy * this.def.heat) / (t.mass * 4 + 12)));
      }

      // ---- impulse: pull toward / push away from the emitter ----
      if (this.def.pull !== 0) {
        const toEmitter = this.origin.subtract(t.pos);
        const dist = Math.max(toEmitter.length(), 0.001);
        const dir = toEmitter.scale(1 / dist);
        const a = (this.def.pull * energy) / (t.mass * 0.5 + 1);
        t.vel.addInPlace(dir.scale(a));
      }

      hits.push({ target: t, energy, point: closest });
    }
    return hits;
  }

  update(dt: number): boolean {
    this.ttl -= dt;
    const fade = Math.max(0, Math.min(1, this.ttl / 0.35));
    for (const m of [this.core, this.glow]) {
      if (m?.material) {
        const base = m === this.core ? 0.95 : 0.24;
        (m.material as StandardMaterial).alpha = base * fade;
      }
    }
    // subtle pulse so beams read as energetic
    const pulse = 1 + Math.sin(performance.now() * 0.02) * 0.08;
    this.core?.scaling.set(pulse, 1, pulse);
    return this.ttl > 0;
  }

  dispose(): void {
    this.core?.material?.dispose();
    this.glow?.material?.dispose();
    this.core?.dispose();
    this.glow?.dispose();
    this.core = null;
    this.glow = null;
  }
}

/** Owns all live beams for a world. */
export class BeamSystem {
  private beams: Beam[] = [];
  private scene: Scene;

  constructor(scene: Scene) {
    this.scene = scene;
  }

  fire(kind: BeamKind, origin: Vector3, direction: Vector3,
       width = 1.2, range = 400, ttl = 1.6): Beam {
    const b = new Beam(this.scene, BEAMS[kind], origin, direction, width, range, ttl);
    this.beams.push(b);
    return b;
  }

  update(dt: number, targets: BeamTarget[]): BeamHit[] {
    const all: BeamHit[] = [];
    for (let i = this.beams.length - 1; i >= 0; i--) {
      const b = this.beams[i];
      all.push(...b.apply(targets, dt));
      if (!b.update(dt)) {
        b.dispose();
        this.beams.splice(i, 1);
      }
    }
    return all;
  }

  get count(): number {
    return this.beams.length;
  }

  clear(): void {
    this.beams.forEach((b) => b.dispose());
    this.beams = [];
  }

  dispose(): void {
    this.clear();
  }
}
