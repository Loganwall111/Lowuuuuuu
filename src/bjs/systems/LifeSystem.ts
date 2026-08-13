/**
 * LifeSystem — surface fauna that makes a planet feel inhabited.
 *
 * When you land somewhere and walk around, an empty heightfield reads as a
 * tech demo. Creatures wandering the terrain are what make it a place. This
 * is deliberately a *system*, not a per-world gimmick: any world that can
 * answer "how high is the ground at (x,z)?" can host life.
 *
 * Design notes:
 *  - Creatures are thin-instanced so a few hundred cost one draw call.
 *  - Behaviour is a tiny wander/flee state machine, not pathfinding. It only
 *    has to look alive from walking distance.
 *  - Species are generated from a seed, so every planet's life is its own.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { Matrix, Quaternion } from '@babylonjs/core/Maths/math.vector';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Scene } from '@babylonjs/core/scene';

/** How the ground is queried. Any world can supply this. */
export type GroundProbe = (x: number, z: number) => { height: number; normal: Vector3 } | null;

export type BodyPlan = 'grazer' | 'strider' | 'hopper' | 'crawler' | 'floater';

export interface Species {
  name: string;
  plan: BodyPlan;
  /** Body size in world units. */
  size: number;
  colour: Color3;
  /** Cruise speed, world units per second. */
  speed: number;
  /** How far away the player has to be before it panics. */
  shy: number;
  /** Herd size when spawned. */
  herd: number;
}

interface Critter {
  pos: Vector3;
  heading: number;
  speedMul: number;
  /** Seconds until it picks a new direction. */
  think: number;
  /** Bob phase so a herd does not move in lockstep. */
  phase: number;
  fleeing: number;
  species: number;
}

const PLAN_NAMES: Record<BodyPlan, string[]> = {
  grazer: ['Lowback', 'Mudgrazer', 'Tussock Beast', 'Palegrazer'],
  strider: ['Stilt Strider', 'Longlimb', 'Ridgewalker', 'Pale Strider'],
  hopper: ['Dustshrew', 'Springlegs', 'Pocket Hopper', 'Gravel Flea'],
  crawler: ['Plated Crawler', 'Silt Crawler', 'Rock Louse', 'Shalebug'],
  floater: ['Driftbell', 'Sky Medusa', 'Gasbag', 'Lumen Drifter']
};

const PLANS: BodyPlan[] = ['grazer', 'strider', 'hopper', 'crawler', 'floater'];

function mulberry32(seed: number): () => number {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/** Builds a plausible species set for a planet from its seed. */
export function speciesFor(seed: number, count = 3): Species[] {
  const rng = mulberry32(seed);
  const out: Species[] = [];
  for (let i = 0; i < count; i++) {
    const plan = PLANS[Math.floor(rng() * PLANS.length)];
    const names = PLAN_NAMES[plan];
    const size = plan === 'floater' ? 1.2 + rng() * 2.2
      : plan === 'strider' ? 1.4 + rng() * 2.6
        : 0.5 + rng() * 1.1;
    out.push({
      name: names[Math.floor(rng() * names.length)],
      plan,
      size,
      colour: new Color3(0.25 + rng() * 0.6, 0.25 + rng() * 0.5, 0.25 + rng() * 0.55),
      speed: plan === 'hopper' ? 4 + rng() * 5 : 1.2 + rng() * 3.2,
      shy: 6 + rng() * 16,
      herd: plan === 'grazer' ? 6 + Math.floor(rng() * 10) : 2 + Math.floor(rng() * 6)
    });
  }
  return out;
}

export class LifeSystem {
  private scene: Scene;
  private ground: GroundProbe;
  private species: Species[] = [];
  private critters: Critter[] = [];
  private meshes: Mesh[] = [];
  private mats: StandardMaterial[] = [];
  /** Half-width of the area life is spawned across. */
  private span: number;
  private enabled = true;

  constructor(scene: Scene, ground: GroundProbe, span = 60) {
    this.scene = scene;
    this.ground = ground;
    this.span = span;
  }

  get population(): number { return this.critters.length; }
  get speciesCount(): number { return this.species.length; }
  /** Names of the species currently alive, for the telemetry panel. */
  get speciesNames(): string[] { return this.species.map((s) => s.name); }

  setEnabled(on: boolean): void {
    this.enabled = on;
    this.meshes.forEach((m) => { m.setEnabled(on); });
  }

  /** Wipes existing life and populates the world from `seed`. */
  populate(seed: number, speciesCount = 3): void {
    this.clear();
    this.species = speciesFor(seed, Math.max(1, speciesCount));

    this.species.forEach((sp, si) => {
      const mesh = this.buildBody(sp, si);
      this.meshes.push(mesh);

      for (let i = 0; i < sp.herd; i++) {
        const pos = this.findSpawn();
        if (!pos) continue;
        this.critters.push({
          pos,
          heading: Math.random() * Math.PI * 2,
          speedMul: 0.75 + Math.random() * 0.5,
          think: Math.random() * 3,
          phase: Math.random() * Math.PI * 2,
          fleeing: 0,
          species: si
        });
      }
    });
    this.syncInstances();
  }

  /** A body built from primitives - readable at walking distance. */
  private buildBody(sp: Species, si: number): Mesh {
    const s = sp.size;
    let mesh: Mesh;
    switch (sp.plan) {
      case 'strider':
        mesh = MeshBuilder.CreateCapsule('life_' + si,
          { radius: s * 0.28, height: s * 1.9, subdivisions: 2, tessellation: 8 }, this.scene);
        break;
      case 'hopper':
        mesh = MeshBuilder.CreateSphere('life_' + si,
          { diameter: s * 0.9, segments: 8 }, this.scene);
        break;
      case 'crawler':
        mesh = MeshBuilder.CreateBox('life_' + si,
          { width: s * 1.3, height: s * 0.4, depth: s * 0.8 }, this.scene);
        break;
      case 'floater':
        mesh = MeshBuilder.CreateSphere('life_' + si,
          { diameter: s, segments: 10 }, this.scene);
        break;
      default:
        mesh = MeshBuilder.CreateCapsule('life_' + si,
          { radius: s * 0.42, height: s * 1.1, subdivisions: 2, tessellation: 8 }, this.scene);
    }

    const mat = new StandardMaterial('lifeM_' + si, this.scene);
    mat.diffuseColor = sp.colour;
    // Never fully black under any lighting - a silhouette still reads.
    mat.emissiveColor = sp.plan === 'floater'
      ? sp.colour.scale(0.55)
      : sp.colour.scale(0.10);
    mat.specularColor = new Color3(0.12, 0.12, 0.12);
    this.mats.push(mat);

    mesh.material = mat;
    mesh.isPickable = false;
    mesh.alwaysSelectAsActiveMesh = true;
    // One draw call per species regardless of herd size.
    mesh.thinInstanceEnablePicking = false;
    return mesh;
  }

  private findSpawn(): Vector3 | null {
    for (let tries = 0; tries < 12; tries++) {
      const x = (Math.random() * 2 - 1) * this.span;
      const z = (Math.random() * 2 - 1) * this.span;
      const g = this.ground(x, z);
      if (g) return new Vector3(x, g.height, z);
    }
    return null;
  }

  /**
   * Advances every critter. `playerPos` may be null (no one to flee from).
   */
  update(dt: number, playerPos: Vector3 | null): void {
    if (!this.enabled || !this.critters.length) return;
    const step = Math.min(dt, 0.05);   // stay stable if a frame hitches

    for (const c of this.critters) {
      const sp = this.species[c.species];
      if (!sp) continue;

      c.think -= step;
      c.phase += step * (sp.plan === 'hopper' ? 7 : 2.4);

      // Flee if the player crowds them.
      if (playerPos) {
        const dx = c.pos.x - playerPos.x;
        const dz = c.pos.z - playerPos.z;
        const d2 = dx * dx + dz * dz;
        if (d2 < sp.shy * sp.shy) {
          c.heading = Math.atan2(dz, dx);
          c.fleeing = 1.6;
        }
      }
      if (c.fleeing > 0) c.fleeing -= step;

      if (c.think <= 0) {
        c.heading += (Math.random() - 0.5) * 1.7;
        c.think = 1.2 + Math.random() * 3.4;
      }

      const spd = sp.speed * c.speedMul * (c.fleeing > 0 ? 2.6 : 1);
      const nx = c.pos.x + Math.cos(c.heading) * spd * step;
      const nz = c.pos.z + Math.sin(c.heading) * spd * step;

      const g = this.ground(nx, nz);
      if (!g) {
        // Walked off the edge of the world: turn back inward.
        c.heading += Math.PI * (0.6 + Math.random() * 0.8);
        continue;
      }
      c.pos.x = nx;
      c.pos.z = nz;

      // Floaters drift above the terrain; everything else is clamped to it.
      const bob = sp.plan === 'hopper'
        ? Math.abs(Math.sin(c.phase)) * sp.size * 0.7
        : Math.sin(c.phase) * sp.size * 0.05;
      c.pos.y = sp.plan === 'floater'
        ? g.height + sp.size * 2.4 + Math.sin(c.phase * 0.5) * sp.size * 0.5
        : g.height + sp.size * 0.5 + bob;
    }
    this.syncInstances();
  }

  /** Pushes critter transforms into the thin-instance buffers. */
  private syncInstances(): void {
    for (let si = 0; si < this.meshes.length; si++) {
      const mesh = this.meshes[si];
      const mine = this.critters.filter((c) => c.species === si);
      if (!mine.length) { mesh.thinInstanceCount = 0; continue; }

      const data = new Float32Array(mine.length * 16);
      const q = new Quaternion();
      const scl = Vector3.One();
      const m = Matrix.Identity();
      for (let i = 0; i < mine.length; i++) {
        const c = mine[i];
        Quaternion.FromEulerAnglesToRef(0, -c.heading, 0, q);
        Matrix.ComposeToRef(scl, q, c.pos, m);
        m.copyToArray(data, i * 16);
      }
      mesh.thinInstanceSetBuffer('matrix', data, 16, false);
    }
  }

  /** Removes every creature but keeps the system reusable. */
  clear(): void {
    this.meshes.forEach((m) => m.dispose());
    this.mats.forEach((m) => m.dispose());
    this.meshes = [];
    this.mats = [];
    this.critters = [];
    this.species = [];
  }

  stats(): Record<string, string> {
    return {
      'Life: species': String(this.species.length),
      'Life: population': String(this.critters.length),
      'Life: nearby': this.species.length
        ? this.species.map((s) => s.name).slice(0, 3).join(', ')
        : '—'
    };
  }

  dispose(): void { this.clear(); }
}
