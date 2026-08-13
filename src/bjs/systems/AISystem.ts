/**
 * AISystem — autonomous craft with steering behaviours and a state machine.
 *
 * Ships are not on rails. Each one runs a small behaviour tree (patrol →
 * pursue → attack → flee → crash) driven by classic steering forces (seek,
 * flee, arrive, wander, separation). Because steering outputs an acceleration
 * that is integrated like any other body, ships obey the same physics as
 * everything else and can be shoved by beams, caught in gravity wells, or
 * smashed by a thrown piano.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';

export type ShipState = 'patrol' | 'pursue' | 'attack' | 'flee' | 'crash' | 'dead';

export interface ShipTarget {
  pos: Vector3;
  radius: number;
  mass: number;
}

export interface ShipConfig {
  maxSpeed: number;
  maxForce: number;
  aggression: number;   // 0..1, how eagerly it closes in
  health: number;
  attackRange: number;
  fleeHealth: number;   // flees below this fraction of max health
}

export const SHIP_PRESETS: Record<string, ShipConfig> = {
  scout:    { maxSpeed: 34, maxForce: 44, aggression: 0.45, health: 30,  attackRange: 55, fleeHealth: 0.35 },
  fighter:  { maxSpeed: 28, maxForce: 36, aggression: 0.85, health: 60,  attackRange: 45, fleeHealth: 0.2 },
  bomber:   { maxSpeed: 17, maxForce: 20, aggression: 1.0,  health: 120, attackRange: 30, fleeHealth: 0.0 },
  mothership: { maxSpeed: 9, maxForce: 11, aggression: 0.7, health: 400, attackRange: 80, fleeHealth: 0.0 }
};

export class Ship {
  pos: Vector3;
  vel: Vector3;
  acc = Vector3.Zero();
  state: ShipState = 'patrol';
  cfg: ShipConfig;
  health: number;
  maxHealth: number;
  mesh: Mesh;
  radius = 1.6;
  kind: string;
  target: ShipTarget | null = null;
  private wanderAngle = Math.random() * Math.PI * 2;
  private stateTime = 0;
  /** Set when the ship commits to a suicide dive. */
  crashTarget: Vector3 | null = null;

  constructor(scene: Scene, kind: string, pos: Vector3, tint: Color3) {
    this.kind = kind;
    this.cfg = SHIP_PRESETS[kind] ?? SHIP_PRESETS.fighter;
    this.health = this.cfg.health;
    this.maxHealth = this.cfg.health;
    this.pos = pos.clone();
    this.vel = new Vector3(
      (Math.random() - 0.5) * 8, (Math.random() - 0.5) * 3, (Math.random() - 0.5) * 8);
    this.mesh = this.buildMesh(scene, kind, tint);
    this.mesh.position.copyFrom(this.pos);
  }

  private buildMesh(scene: Scene, kind: string, tint: Color3): Mesh {
    const scale = kind === 'mothership' ? 3.4 : kind === 'bomber' ? 1.8 : 1.0;
    this.radius = 1.6 * scale;

    // classic saucer: tapered hull plus a dome
    const hull = MeshBuilder.CreateCylinder('hull', {
      diameterTop: 0.7 * scale, diameterBottom: 3.2 * scale,
      height: 0.55 * scale, tessellation: 18
    }, scene);
    const dome = MeshBuilder.CreateSphere('dome', {
      diameter: 1.5 * scale, segments: 12, slice: 0.5
    }, scene);
    dome.position.y = 0.26 * scale;
    const rim = MeshBuilder.CreateTorus('rim', {
      diameter: 3.2 * scale, thickness: 0.16 * scale, tessellation: 20
    }, scene);
    rim.position.y = -0.22 * scale;

    const merged = Mesh.MergeMeshes([hull, dome, rim], true, true, undefined, false, false)
      ?? hull;
    const mat = new StandardMaterial('shipMat', scene);
    mat.diffuseColor = tint.scale(0.5);
    mat.emissiveColor = tint.scale(0.55);
    mat.specularColor = new Color3(0.9, 0.9, 1.0);
    mat.specularPower = 64;
    merged.material = mat;
    merged.name = 'ship_' + kind;
    return merged;
  }

  /* ---------------------------- steering behaviours ---------------------------- */

  private truncate(v: Vector3, max: number): Vector3 {
    const l = v.length();
    return l > max && l > 1e-6 ? v.scale(max / l) : v;
  }

  private seek(target: Vector3): Vector3 {
    const desired = target.subtract(this.pos);
    const d = desired.length();
    if (d < 1e-6) return Vector3.Zero();
    return this.truncate(
      desired.scale(this.cfg.maxSpeed / d).subtract(this.vel), this.cfg.maxForce);
  }

  private flee(target: Vector3): Vector3 {
    return this.seek(this.pos.scale(2).subtract(target));
  }

  /** Seek that slows down on approach so the ship does not overshoot. */
  private arrive(target: Vector3, slowRadius: number): Vector3 {
    const desired = target.subtract(this.pos);
    const d = desired.length();
    if (d < 1e-6) return Vector3.Zero();
    const speed = d < slowRadius ? this.cfg.maxSpeed * (d / slowRadius) : this.cfg.maxSpeed;
    return this.truncate(desired.scale(speed / d).subtract(this.vel), this.cfg.maxForce);
  }

  private wander(dt: number): Vector3 {
    this.wanderAngle += (Math.random() - 0.5) * 3.2 * dt;
    const fwd = this.vel.length() > 0.1 ? this.vel.clone().normalize() : new Vector3(1, 0, 0);
    const centre = this.pos.add(fwd.scale(9));
    const offset = new Vector3(
      Math.cos(this.wanderAngle) * 6,
      Math.sin(this.wanderAngle * 0.7) * 2.4,
      Math.sin(this.wanderAngle) * 6);
    return this.seek(centre.add(offset)).scale(0.55);
  }

  /** Keeps a formation from collapsing into one point. */
  private separate(others: Ship[], radius: number): Vector3 {
    let steer = Vector3.Zero();
    let n = 0;
    for (const o of others) {
      if (o === this || o.state === 'dead') continue;
      const d = Vector3.Distance(this.pos, o.pos);
      if (d > 0 && d < radius) {
        steer.addInPlace(this.pos.subtract(o.pos).normalize().scale(1 / d));
        n++;
      }
    }
    if (n === 0) return Vector3.Zero();
    return this.truncate(steer.scale(1 / n).scale(this.cfg.maxSpeed), this.cfg.maxForce);
  }

  /* ------------------------------ state machine ------------------------------ */

  think(dt: number, fleet: Ship[]): void {
    if (this.state === 'dead') return;
    this.stateTime += dt;

    const hp = this.health / this.maxHealth;
    const dist = this.target ? Vector3.Distance(this.pos, this.target.pos) : Infinity;

    // transitions
    if (this.state !== 'crash') {
      if (hp <= 0) {
        this.setState('crash');
        this.crashTarget = this.target ? this.target.pos.clone() : null;
      } else if (hp < this.cfg.fleeHealth && this.state !== 'flee') {
        this.setState('flee');
      } else if (this.state === 'flee' && hp > this.cfg.fleeHealth + 0.25) {
        this.setState('patrol');
      } else if (this.target && this.state === 'patrol'
                 && dist < this.cfg.attackRange * 3.5 && Math.random() < this.cfg.aggression) {
        this.setState('pursue');
      } else if (this.state === 'pursue' && dist < this.cfg.attackRange) {
        this.setState('attack');
      } else if (this.state === 'attack' && dist > this.cfg.attackRange * 1.6) {
        this.setState('pursue');
      } else if (this.state === 'pursue' && !this.target) {
        this.setState('patrol');
      }
    }

    // behaviour per state
    let steer = Vector3.Zero();
    switch (this.state) {
      case 'patrol':
        steer = this.wander(dt);
        break;
      case 'pursue':
        if (this.target) {
          // lead the target rather than chasing its current position
          steer = this.seek(this.target.pos);
        }
        break;
      case 'attack':
        if (this.target) {
          // strafe: orbit the target instead of ramming it
          const toT = this.target.pos.subtract(this.pos);
          const side = Vector3.Cross(toT, new Vector3(0, 1, 0)).normalize();
          const orbit = this.target.pos.add(side.scale(this.cfg.attackRange * 0.8));
          steer = this.arrive(orbit, 20);
        }
        break;
      case 'flee':
        steer = this.target ? this.flee(this.target.pos) : this.wander(dt);
        break;
      case 'crash':
        if (this.crashTarget) {
          steer = this.seek(this.crashTarget).scale(2.2);   // full commitment
        }
        break;
    }

    steer.addInPlace(this.separate(fleet, 7).scale(1.35));
    this.acc.copyFrom(steer);
  }

  integrate(dt: number): void {
    if (this.state === 'dead') return;
    this.vel.addInPlace(this.acc.scale(dt));
    const cap = this.state === 'crash' ? this.cfg.maxSpeed * 1.8 : this.cfg.maxSpeed;
    const sp = this.vel.length();
    if (sp > cap) this.vel.scaleInPlace(cap / sp);
    this.pos.addInPlace(this.vel.scale(dt));
    this.mesh.position.copyFrom(this.pos);

    // bank into the turn and spin the saucer
    this.mesh.rotation.y += dt * (this.state === 'crash' ? 9 : 1.6);
    const lateral = Vector3.Dot(this.acc, Vector3.Cross(
      this.vel.length() > 0.01 ? this.vel.clone().normalize() : new Vector3(1, 0, 0),
      new Vector3(0, 1, 0)));
    this.mesh.rotation.z = Math.max(-0.7, Math.min(0.7, -lateral * 0.02));

    if (this.state === 'crash' && this.mesh.material) {
      const m = this.mesh.material as StandardMaterial;
      m.emissiveColor = new Color3(1, 0.35 + Math.sin(this.stateTime * 30) * 0.25, 0.1);
    }
  }

  setState(s: ShipState): void {
    if (this.state === s) return;
    this.state = s;
    this.stateTime = 0;
  }

  damage(amount: number): void {
    this.health -= amount;
    if (this.health <= 0 && this.state !== 'crash' && this.state !== 'dead') {
      this.setState('crash');
      this.crashTarget = this.target ? this.target.pos.clone() : this.pos.add(new Vector3(0, -50, 0));
    }
  }

  dispose(): void {
    this.state = 'dead';
    this.mesh.material?.dispose();
    this.mesh.dispose();
  }
}

/** Fleet manager: spawns ships, assigns targets, reports crashes. */
export class AISystem {
  ships: Ship[] = [];
  private scene: Scene;
  crashes = 0;

  constructor(scene: Scene) {
    this.scene = scene;
  }

  spawn(kind: string, pos: Vector3, tint?: Color3): Ship {
    const colors = [
      new Color3(0.35, 1.0, 0.55), new Color3(0.5, 0.75, 1.0),
      new Color3(1.0, 0.45, 0.85), new Color3(1.0, 0.8, 0.3)
    ];
    const c = tint ?? colors[Math.floor(Math.random() * colors.length)];
    const s = new Ship(this.scene, kind, pos, c);
    this.ships.push(s);
    return s;
  }

  /** Sends a whole formation at a target. */
  invade(count: number, target: ShipTarget, from: Vector3): Ship[] {
    const made: Ship[] = [];
    const kinds = ['scout', 'fighter', 'fighter', 'bomber'];
    for (let i = 0; i < count; i++) {
      const off = new Vector3(
        (Math.random() - 0.5) * 40, (Math.random() - 0.5) * 18, (Math.random() - 0.5) * 40);
      const s = this.spawn(kinds[i % kinds.length], from.add(off));
      s.target = target;
      s.setState('pursue');
      made.push(s);
    }
    return made;
  }

  setTarget(t: ShipTarget | null): void {
    for (const s of this.ships) s.target = t;
  }

  /**
   * Advances the fleet. Returns ships that reached their crash target this
   * frame so the caller can turn them into explosions.
   */
  update(dt: number): Ship[] {
    const impacts: Ship[] = [];
    for (const s of this.ships) {
      s.think(dt, this.ships);
      s.integrate(dt);

      if (s.state === 'crash' && s.target) {
        if (Vector3.Distance(s.pos, s.target.pos) < s.target.radius + s.radius) {
          impacts.push(s);
        }
      }
    }
    for (const s of impacts) {
      s.dispose();
      this.crashes++;
    }
    if (impacts.length) this.ships = this.ships.filter((s) => s.state !== 'dead');
    return impacts;
  }

  count(): number {
    return this.ships.length;
  }

  states(): Record<string, number> {
    const out: Record<string, number> = {};
    for (const s of this.ships) out[s.state] = (out[s.state] ?? 0) + 1;
    return out;
  }

  clear(): void {
    this.ships.forEach((s) => s.dispose());
    this.ships = [];
  }

  dispose(): void {
    this.clear();
  }
}
