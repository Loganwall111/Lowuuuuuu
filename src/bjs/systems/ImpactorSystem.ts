/**
 * ImpactorSystem — throwing things at planets and watching what happens.
 *
 * The catalogue and the physics live in ThrowableSystem; this is the part
 * that puts a projectile in the world, flies it under gravity, and turns
 * the arrival into something you can see.
 *
 * The projectile is a real body on a real trajectory, so a bad throw misses
 * and a fast one arrives before you can follow it. Nothing is on rails.
 *
 * The wacky items are not special cases bolted on the side: `behaviour` is
 * a field on the catalogue entry, and everything with a behaviour runs
 * through the same update as everything else. That is how an octopus that
 * orbits and devours a planet costs about fifteen lines rather than its own
 * subsystem.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';
import {
  computeImpact, throwableById, type Throwable, type ImpactResult
} from './ThrowableSystem';

/** A body a projectile can hit. */
export interface ImpactTarget {
  id: string;
  position: Vector3;
  /** Visual radius in world units. */
  radius: number;
  /** Physical mass in kg, for the impact maths. */
  mass: number;
  /** Physical radius in metres, for binding energy. */
  physicalRadius: number;
}

/** A projectile in flight. */
export interface Projectile {
  id: string;
  spec: Throwable;
  mesh: Mesh;
  velocity: Vector3;
  /** Seconds it has been alive, so strays can be reaped. */
  age: number;
  /** Behaviour state for the odd ones. */
  phase: number;
  /** The body it has latched onto, for orbiters and devourers. */
  attached: ImpactTarget | null;
}

export interface ImpactEvent {
  projectile: Projectile;
  target: ImpactTarget;
  result: ImpactResult;
  /** Where it struck, in world space. */
  at: Vector3;
}

/** Gravitational constant in world units, matching UniverseState. */
export const WORLD_G = 42;

/** How long a projectile may drift before it is reaped, seconds. */
export const MAX_AGE = 180;

export class ImpactorSystem {
  projectiles: Projectile[] = [];
  /** Impacts since the world loaded, newest last. */
  log: ImpactEvent[] = [];
  private scene: Scene | null = null;
  private counter = 0;
  private onImpact: ((e: ImpactEvent) => void) | null = null;

  attach(scene: Scene, onImpact?: (e: ImpactEvent) => void): void {
    this.scene = scene;
    this.onImpact = onImpact ?? null;
  }

  /**
   * Throws something.
   *
   * Size on screen is the log of the real mass: a neutron star fragment is
   * twenty-two orders of magnitude heavier than a pebble, so linear scaling
   * would make everything either invisible or larger than the solar system.
   */
  throwAt(id: string, from: Vector3, direction: Vector3, speed = 40): Projectile | null {
    const scene = this.scene;
    const spec = throwableById(id);
    if (!scene || !spec) return null;

    const dir = direction.lengthSquared() > 1e-9
      ? direction.normalize() : new Vector3(0, 0, 1);

    const size = ImpactorSystem.visualRadius(spec.mass);
    const mesh = MeshBuilder.CreateSphere(
      'proj' + (++this.counter), { diameter: size * 2, segments: 12 }, scene);
    mesh.position = from.add(dir.scale(size * 2 + 1));

    const mat = new StandardMaterial(mesh.name + 'm', scene);
    const c = ImpactorSystem.tint(spec);
    mat.diffuseColor = c;
    mat.emissiveColor = c.scale(spec.composition === 'antimatter' ? 0.9 : 0.25);
    mat.specularColor = new Color3(0.2, 0.2, 0.2);
    mesh.material = mat;

    const p: Projectile = {
      id: mesh.name,
      spec,
      mesh,
      velocity: dir.scale(speed),
      age: 0,
      phase: 0,
      attached: null
    };
    this.projectiles.push(p);
    return p;
  }

  /** Screen size from real mass, compressed logarithmically. */
  static visualRadius(mass: number): number {
    const m = Math.max(mass, 1e-6);
    // 0.2 kg -> ~0.2 units; 1e26 kg -> ~6 units.
    return Math.max(0.18, Math.min(8, 0.18 + Math.log10(m / 0.2) * 0.24));
  }

  /** Colour by what it is made of. */
  static tint(t: Throwable): Color3 {
    switch (t.composition) {
      case 'ice':        return new Color3(0.72, 0.88, 1.0);
      case 'iron':       return new Color3(0.62, 0.60, 0.58);
      case 'gas':        return new Color3(0.95, 0.78, 0.42);
      case 'organic':    return new Color3(0.78, 0.36, 0.52);
      case 'exotic':     return new Color3(0.62, 0.42, 1.0);
      case 'antimatter': return new Color3(1.0, 0.42, 0.85);
      default:           return new Color3(0.55, 0.48, 0.42);
    }
  }

  /**
   * Advances every projectile.
   *
   * Gravity from all targets is summed, so a throw can be slung around one
   * planet into another - which is the sort of thing worth discovering by
   * accident rather than being told about.
   */
  update(dt: number, targets: ImpactTarget[]): void {
    if (!Number.isFinite(dt) || dt <= 0) return;

    for (let i = this.projectiles.length - 1; i >= 0; i--) {
      const p = this.projectiles[i];
      p.age += dt;

      if (p.age > MAX_AGE) { this.destroy(i); continue; }

      // Behaviour-carrying items do their own thing once attached.
      if (p.attached && p.spec.behaviour) {
        this.behave(p, dt);
        continue;
      }

      // Sum gravity from every body.
      const acc = Vector3.Zero();
      for (const t of targets) {
        const d = t.position.subtract(p.mesh.position);
        const r2 = Math.max(d.lengthSquared(), t.radius * t.radius * 0.25);
        acc.addInPlace(d.normalize().scale((WORLD_G * t.mass) / r2));
      }
      p.velocity.addInPlace(acc.scale(dt));
      p.mesh.position.addInPlace(p.velocity.scale(dt));

      // Did it arrive?
      for (const t of targets) {
        const dist = Vector3.Distance(p.mesh.position, t.position);
        if (dist > t.radius + ImpactorSystem.visualRadius(p.spec.mass)) continue;

        // Impact speed in m/s: world speed is scaled to something plausible
        // so the energies mean what they say.
        const speed = p.velocity.length() * 500;
        const result = computeImpact(p.spec, speed, t.mass, t.physicalRadius);
        const at = p.mesh.position.clone();

        if (p.spec.behaviour === 'orbit' || p.spec.behaviour === 'devour') {
          // These do not land, they take up residence.
          p.attached = t;
          p.phase = 0;
          this.emit({ projectile: p, target: t, result, at });
        } else {
          this.emit({ projectile: p, target: t, result, at });
          this.destroy(i);
        }
        break;
      }
    }
  }

  /**
   * The odd ones. Each behaviour is a few lines because they all ride the
   * same projectile update rather than owning a subsystem apiece.
   */
  private behave(p: Projectile, dt: number): void {
    const t = p.attached;
    if (!t) return;
    p.phase += dt;

    switch (p.spec.behaviour) {
      case 'orbit': {
        // Circles the body it reached, staying just above the surface.
        const r = t.radius * 1.35;
        const a = p.phase * 0.6;
        p.mesh.position.set(
          t.position.x + Math.cos(a) * r,
          t.position.y + Math.sin(a * 0.7) * r * 0.3,
          t.position.z + Math.sin(a) * r
        );
        break;
      }
      case 'devour': {
        // Spirals in and swells as it goes, which is exactly as unsettling
        // as it sounds and costs nothing extra to compute.
        const r = t.radius * Math.max(0.35, 1.6 - p.phase * 0.05);
        const a = p.phase * 0.9;
        p.mesh.position.set(
          t.position.x + Math.cos(a) * r,
          t.position.y,
          t.position.z + Math.sin(a) * r
        );
        const grow = 1 + Math.min(6, p.phase * 0.12);
        p.mesh.scaling.setAll(grow);
        break;
      }
      case 'grow': {
        p.mesh.scaling.setAll(1 + Math.min(9, p.phase * 0.3));
        break;
      }
      default:
        break;
    }
  }

  private emit(e: ImpactEvent): void {
    this.log.push(e);
    if (this.log.length > 64) this.log.shift();
    this.onImpact?.(e);
  }

  private destroy(index: number): void {
    const p = this.projectiles[index];
    if (!p) return;
    try { p.mesh.material?.dispose(); p.mesh.dispose(); } catch { /* gone */ }
    this.projectiles.splice(index, 1);
  }

  /** Removes everything in flight. */
  clear(): void {
    for (let i = this.projectiles.length - 1; i >= 0; i--) this.destroy(i);
    this.log.length = 0;
  }

  stats(): Record<string, string> {
    const last = this.log[this.log.length - 1];
    return {
      'In flight': String(this.projectiles.length),
      'Impacts': String(this.log.length),
      'Last impact': last
        ? last.result.megatons.toExponential(1) + ' Mt · ' + last.result.outcome
        : '—'
    };
  }

  dispose(): void {
    this.clear();
    this.scene = null;
    this.onImpact = null;
  }
}
