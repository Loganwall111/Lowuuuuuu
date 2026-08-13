/**
 * DestructionSystem — impact consequences.
 *
 * Turns collision energy into visible, physical results: fragmentation into
 * debris that keeps orbiting, expanding shockwave rings, and glowing impact
 * flashes. Fragment count and spread derive from the actual kinetic energy
 * and the material's fracture coefficient, so a rubber duck and a neutron
 * star behave very differently through the same code path.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';

export interface Fragment {
  pos: Vector3;
  vel: Vector3;
  mass: number;
  radius: number;
}

interface Shockwave {
  mesh: Mesh;
  age: number;
  life: number;
  maxRadius: number;
}

interface Flash {
  mesh: Mesh;
  age: number;
  life: number;
  size: number;
}

export class DestructionSystem {
  private waves: Shockwave[] = [];
  private flashes: Flash[] = [];
  private scene: Scene;
  private craters = 0;
  private totalEnergy = 0;

  constructor(scene: Scene) {
    this.scene = scene;
  }

  /**
   * Computes how a body breaks apart. Returns fragment descriptors for the
   * caller to turn into real simulated bodies.
   */
  fragment(pos: Vector3, vel: Vector3, mass: number, radius: number,
           energy: number, fracture: number): Fragment[] {
    // more energy and more brittle material -> more, smaller pieces
    const n = Math.max(0, Math.min(14, Math.round(Math.cbrt(energy) * fracture * 1.6)));
    if (n < 2) return [];

    const frags: Fragment[] = [];
    const fragMass = (mass * 0.62) / n;   // some mass is vaporised
    const spread = Math.min(26, 4 + Math.cbrt(energy) * 1.4);

    for (let i = 0; i < n; i++) {
      // distribute roughly evenly on a sphere
      const phi = Math.acos(1 - (2 * (i + 0.5)) / n);
      const theta = Math.PI * (1 + Math.sqrt(5)) * i;
      const dir = new Vector3(
        Math.sin(phi) * Math.cos(theta),
        Math.sin(phi) * Math.sin(theta),
        Math.cos(phi)
      );
      const jitter = 0.75 + Math.random() * 0.5;
      frags.push({
        pos: pos.add(dir.scale(radius * 1.05)),
        vel: vel.add(dir.scale(spread * jitter)),
        mass: fragMass * (0.6 + Math.random() * 0.8),
        radius: Math.cbrt(fragMass) * 1.5 * 0.8
      });
    }
    this.craters++;
    this.totalEnergy += energy;
    return frags;
  }

  /** Expanding ring marking an impact. */
  shockwave(pos: Vector3, radius: number, energy: number, tint: [number, number, number] = [1, 0.65, 0.25]): void {
    const maxRadius = radius * (2.5 + Math.min(8, Math.cbrt(energy) * 0.7));
    const mesh = MeshBuilder.CreateTorus('shock', {
      diameter: radius * 2, thickness: radius * 0.16, tessellation: 40
    }, this.scene);
    const mat = new StandardMaterial('shockMat', this.scene);
    mat.emissiveColor = new Color3(tint[0], tint[1], tint[2]);
    mat.diffuseColor = Color3.Black();
    mat.specularColor = Color3.Black();
    mat.disableLighting = true;
    mat.alpha = 0.85;
    mat.backFaceCulling = false;
    mesh.material = mat;
    mesh.position.copyFrom(pos);
    mesh.rotation.x = Math.random() * Math.PI;
    mesh.rotation.z = Math.random() * Math.PI;
    mesh.isPickable = false;
    this.waves.push({ mesh, age: 0, life: 1.1, maxRadius });
  }

  /** Bright flash at the point of impact. */
  flash(pos: Vector3, size: number, tint: [number, number, number] = [1, 0.85, 0.5]): void {
    const mesh = MeshBuilder.CreateSphere('flash', { diameter: size * 2, segments: 14 }, this.scene);
    const mat = new StandardMaterial('flashMat', this.scene);
    mat.emissiveColor = new Color3(tint[0], tint[1], tint[2]);
    mat.diffuseColor = Color3.Black();
    mat.specularColor = Color3.Black();
    mat.disableLighting = true;
    mat.alpha = 0.9;
    mesh.material = mat;
    mesh.position.copyFrom(pos);
    mesh.isPickable = false;
    this.flashes.push({ mesh, age: 0, life: 0.45, size });
  }

  /** Convenience: the full visual package for an impact. */
  impact(pos: Vector3, radius: number, energy: number): void {
    this.flash(pos, radius * 1.6);
    this.shockwave(pos, radius, energy);
  }

  update(dt: number): void {
    for (let i = this.waves.length - 1; i >= 0; i--) {
      const w = this.waves[i];
      w.age += dt;
      const t = w.age / w.life;
      if (t >= 1) {
        w.mesh.material?.dispose();
        w.mesh.dispose();
        this.waves.splice(i, 1);
        continue;
      }
      const ease = 1 - Math.pow(1 - t, 3);
      w.mesh.scaling.setAll(1 + ease * (w.maxRadius / Math.max(w.mesh.getBoundingInfo().boundingSphere.radius, 0.001)) * 0.5);
      (w.mesh.material as StandardMaterial).alpha = 0.85 * (1 - t);
    }

    for (let i = this.flashes.length - 1; i >= 0; i--) {
      const f = this.flashes[i];
      f.age += dt;
      const t = f.age / f.life;
      if (t >= 1) {
        f.mesh.material?.dispose();
        f.mesh.dispose();
        this.flashes.splice(i, 1);
        continue;
      }
      f.mesh.scaling.setAll(1 + t * 2.2);
      (f.mesh.material as StandardMaterial).alpha = 0.9 * (1 - t);
    }
  }

  getStats(): { craters: number; energy: number; active: number } {
    return {
      craters: this.craters,
      energy: this.totalEnergy,
      active: this.waves.length + this.flashes.length
    };
  }

  dispose(): void {
    this.waves.forEach((w) => { w.mesh.material?.dispose(); w.mesh.dispose(); });
    this.flashes.forEach((f) => { f.mesh.material?.dispose(); f.mesh.dispose(); });
    this.waves = [];
    this.flashes = [];
  }
}
