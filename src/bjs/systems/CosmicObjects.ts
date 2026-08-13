/**
 * CosmicObjects — large-scale phenomena that sit inside the sandbox:
 * wormhole pairs, procedural galaxies and nebulae.
 *
 * Galaxies use a single instanced/point cloud so a 20,000-star galaxy costs
 * one draw call. Wormholes are genuine teleport pairs: anything entering one
 * mouth is re-emitted from the other with its velocity rotated, which the
 * n-body solver then continues to integrate normally.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { PointsCloudSystem } from '@babylonjs/core/Particles/pointsCloudSystem';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';

/* ------------------------------- Wormholes ------------------------------- */

export interface WormholeEndpoint {
  pos: Vector3;
  radius: number;
  ring: Mesh;
  disc: Mesh;
}

export interface Teleportable {
  pos: Vector3;
  vel: Vector3;
  radius: number;
}

export class Wormhole {
  a: WormholeEndpoint;
  b: WormholeEndpoint;
  private t = 0;
  transfers = 0;
  /** Bodies recently teleported, so they are not caught in a loop. */
  private cooldown = new WeakMap<object, number>();

  constructor(scene: Scene, posA: Vector3, posB: Vector3, radius = 6) {
    this.a = this.makeEnd(scene, posA, radius, [0.45, 0.75, 1.0]);
    this.b = this.makeEnd(scene, posB, radius, [1.0, 0.55, 0.85]);
  }

  private makeEnd(scene: Scene, pos: Vector3, radius: number,
                  tint: [number, number, number]): WormholeEndpoint {
    const ring = MeshBuilder.CreateTorus('whRing', {
      diameter: radius * 2, thickness: radius * 0.16, tessellation: 48
    }, scene);
    const rm = new StandardMaterial('whRingMat', scene);
    rm.emissiveColor = new Color3(tint[0], tint[1], tint[2]);
    rm.diffuseColor = Color3.Black();
    rm.specularColor = Color3.Black();
    rm.disableLighting = true;
    ring.material = rm;
    ring.position.copyFrom(pos);
    ring.isPickable = false;

    const disc = MeshBuilder.CreateDisc('whDisc', { radius: radius * 0.94, tessellation: 48 }, scene);
    const dm = new StandardMaterial('whDiscMat', scene);
    dm.emissiveColor = new Color3(tint[0] * 0.35, tint[1] * 0.35, tint[2] * 0.5);
    dm.diffuseColor = Color3.Black();
    dm.specularColor = Color3.Black();
    dm.disableLighting = true;
    dm.alpha = 0.65;
    dm.backFaceCulling = false;
    disc.material = dm;
    disc.position.copyFrom(pos);
    disc.isPickable = false;

    return { pos: pos.clone(), radius, ring, disc };
  }

  /** Teleports anything that enters a mouth to the opposite one. */
  process(bodies: Teleportable[], now: number): void {
    const check = (from: WormholeEndpoint, to: WormholeEndpoint) => {
      for (const body of bodies) {
        const cd = this.cooldown.get(body as object) ?? 0;
        if (now < cd) continue;
        if (Vector3.Distance(body.pos, from.pos) > from.radius + body.radius) continue;

        // re-emit from the far mouth, preserving speed
        const offset = body.pos.subtract(from.pos);
        body.pos.copyFrom(to.pos.add(offset.scale(-1)));
        // nudge outward so it does not immediately re-enter
        const outward = body.vel.length() > 1e-4
          ? body.vel.clone().normalize()
          : new Vector3(1, 0, 0);
        body.pos.addInPlace(outward.scale(to.radius * 1.4));
        this.cooldown.set(body as object, now + 1.2);
        this.transfers++;
      }
    };
    check(this.a, this.b);
    check(this.b, this.a);
  }

  update(dt: number): void {
    this.t += dt;
    for (const e of [this.a, this.b]) {
      e.ring.rotation.z += dt * 0.9;
      e.disc.rotation.z -= dt * 1.4;
      const pulse = 1 + Math.sin(this.t * 3) * 0.05;
      e.ring.scaling.setAll(pulse);
    }
  }

  dispose(): void {
    for (const e of [this.a, this.b]) {
      e.ring.material?.dispose();
      e.disc.material?.dispose();
      e.ring.dispose();
      e.disc.dispose();
    }
  }
}

/* -------------------------------- Galaxies -------------------------------- */

export type GalaxyKind = 'spiral' | 'barred' | 'elliptical' | 'irregular' | 'ring';

export class Galaxy {
  private pcs: PointsCloudSystem;
  private mesh: Mesh | null = null;
  kind: GalaxyKind;
  center: Vector3;
  starCount: number;

  constructor(scene: Scene, kind: GalaxyKind, center: Vector3,
              radius = 220, starCount = 14000) {
    this.kind = kind;
    this.center = center.clone();
    this.starCount = starCount;
    this.pcs = new PointsCloudSystem('galaxy', 2, scene);

    const arms = kind === 'barred' ? 2 : 4;
    const rnd = (a: number, b: number) => a + Math.random() * (b - a);

    this.pcs.addPoints(starCount, (particle: any) => {
      let x = 0, y = 0, z = 0;
      let t = Math.pow(Math.random(), 0.62);           // denser toward the core
      const r = t * radius;

      if (kind === 'spiral' || kind === 'barred') {
        const arm = Math.floor(Math.random() * arms);
        const wind = kind === 'barred' ? 2.4 : 3.4;
        let ang = (arm / arms) * Math.PI * 2 + (r / radius) * wind;
        // scatter perpendicular to the arm, wider further out
        const scatter = (Math.random() - 0.5) * (0.34 + (r / radius) * 0.5);
        ang += scatter;
        let rr = r;
        if (kind === 'barred' && r < radius * 0.32) {
          // straight central bar
          const bar = (Math.random() - 0.5) * radius * 0.62;
          x = bar; z = (Math.random() - 0.5) * radius * 0.09;
          y = (Math.random() - 0.5) * radius * 0.045;
          particle.position = new Vector3(this.center.x + x, this.center.y + y, this.center.z + z);
          particle.color = this.tint(0.2);
          return;
        }
        x = Math.cos(ang) * rr;
        z = Math.sin(ang) * rr;
        y = (Math.random() - 0.5) * radius * 0.06 * (1 - t * 0.7);
      } else if (kind === 'elliptical') {
        const u = Math.random() * Math.PI * 2;
        const v = Math.acos(2 * Math.random() - 1);
        x = Math.sin(v) * Math.cos(u) * r;
        y = Math.sin(v) * Math.sin(u) * r * 0.62;
        z = Math.cos(v) * r * 0.82;
      } else if (kind === 'ring') {
        const ang = Math.random() * Math.PI * 2;
        const rr = radius * rnd(0.65, 1.0);
        x = Math.cos(ang) * rr;
        z = Math.sin(ang) * rr;
        y = (Math.random() - 0.5) * radius * 0.05;
        t = 0.8;
      } else {
        // irregular: clumpy blobs
        const cx = rnd(-1, 1) * radius * 0.6;
        const cz = rnd(-1, 1) * radius * 0.6;
        x = cx + rnd(-1, 1) * radius * 0.3;
        z = cz + rnd(-1, 1) * radius * 0.3;
        y = rnd(-1, 1) * radius * 0.12;
      }

      particle.position = new Vector3(this.center.x + x, this.center.y + y, this.center.z + z);
      particle.color = this.tint(t);
    });

    this.pcs.buildMeshAsync().then((m) => {
      this.mesh = m;
      m.isPickable = false;
      m.alwaysSelectAsActiveMesh = true;
    });
  }

  /** Core stars run hot/white, outskirts cooler and bluer. */
  private tint(t: number): Color4 {
    if (t < 0.25) return new Color4(1.0, 0.93, 0.78, 1);
    if (t < 0.55) return new Color4(1.0, 0.85, 0.62, 1);
    if (t < 0.8)  return new Color4(0.72, 0.82, 1.0, 1);
    return new Color4(0.55, 0.68, 1.0, 1);
  }

  dispose(): void {
    this.pcs.dispose();
    this.mesh?.dispose();
  }
}

/* --------------------------------- Nebula --------------------------------- */

export class Nebula {
  private pcs: PointsCloudSystem;
  private mesh: Mesh | null = null;

  constructor(scene: Scene, center: Vector3, radius = 160, count = 7000,
              tint: [number, number, number] = [0.6, 0.35, 0.95]) {
    this.pcs = new PointsCloudSystem('nebula', 3, scene);
    this.pcs.addPoints(count, (particle: any) => {
      // clumped gaussian-ish cloud
      const g = () => (Math.random() + Math.random() + Math.random() - 1.5) / 1.5;
      const p = new Vector3(g() * radius, g() * radius * 0.55, g() * radius);
      particle.position = center.add(p);
      const d = p.length() / radius;
      const a = Math.max(0.15, 1 - d);
      particle.color = new Color4(
        tint[0] * (0.6 + Math.random() * 0.6),
        tint[1] * (0.6 + Math.random() * 0.6),
        tint[2] * (0.7 + Math.random() * 0.5),
        a * 0.75
      );
    });
    this.pcs.buildMeshAsync().then((m) => {
      this.mesh = m;
      m.isPickable = false;
    });
  }

  dispose(): void {
    this.pcs.dispose();
    this.mesh?.dispose();
  }
}
