/**
 * OrbitTraffic — Earth's own neighbourhood, in orbit where it belongs.
 *
 * The home world is a place, and a place has history above it: the
 * International Space Station, a field of satellites, Hubble and Webb, and
 * the Apollo stack on its way out. This keeps all of them in real orbits
 * around the home world, so the sky above Earth is alive with the machines
 * humanity actually put there. The Apollo command module is solid - you can
 * fly out, match its orbit and land on it.
 *
 * The orbit maths is pure and exported, so it can be tested without a GPU;
 * the renderer is thin procedural geometry, no textures.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { TransformNode } from '@babylonjs/core/Meshes/transformNode';
import type { Scene } from '@babylonjs/core/scene';
import type { SolidSphere } from './PlanetLanding';

/** One orbit: radius, inclination, rate, phase. */
export interface Orbit {
  radius: number;
  inclination: number;
  speed: number;   // radians / sec
  phase: number;
}

/** A position on a circular inclined orbit around a centre. */
export function orbitPosition(
  o: Orbit, t: number,
  cx: number, cy: number, cz: number
): [number, number, number] {
  const ang = o.phase + o.speed * t;
  const x = Math.cos(ang) * o.radius;
  const y = 0;
  const z = Math.sin(ang) * o.radius;
  const ci = Math.cos(o.inclination), si = Math.sin(o.inclination);
  const y2 = z * si;
  const z2 = z * ci;
  return [cx + x, cy + y2, cz + z2];
}

/** The ISS's own inclination, as a reference point for the field. */
export const ISS_ORBIT: Orbit = { radius: 1.6, inclination: 0.9, speed: 0.35, phase: 0 };

/** A GPS-style constellation: six planes, four satellites each. */
export const GPS_PLANES = 6;
export const GPS_PER_PLANE = 4;
/** The named stations and eyes humanity has put up, in orbit order. */
export const NAMED_STATIONS = ['ISS', 'Skylab', 'Mir', 'Tiangong', 'Hubble', 'Webb', 'Apollo'];

/** Deterministic 0..1 hash. */
function hash01(seed: number): number {
  let h = seed >>> 0 || 1;
  h = Math.imul(h ^ (h >>> 16), 2246822519) >>> 0;
  h = Math.imul(h ^ (h >>> 13), 3266489917) >>> 0;
  return ((h ^ (h >>> 16)) >>> 0) / 4294967296;
}

export interface Artifact {
  id: string;
  orbit: Orbit;
  /** Root node holding the geometry. */
  root: TransformNode;
  /** Solid bodies this artifact contributes (Apollo, the ISS). */
  solid: SolidSphere | null;
}

export class OrbitTraffic {
  private scene: Scene | null = null;
  private artifacts: Artifact[] = [];
  private t = 0;
  private built = false;

  get count(): number { return this.artifacts.length; }

  attach(scene: Scene): void {
    this.scene = scene;
  }

  /** Builds every artifact around the home world. Safe to call once. */
  build(seed = 0xea47): void {
    const scene = this.scene;
    if (!scene || this.built) return;

    // ---- the ISS: modules, a truss and four solar wings. Solid, so you
    //      can fly out, match its orbit and walk along it. ----
    this.artifacts.push({
      id: 'ISS',
      orbit: { ...ISS_ORBIT },
      root: this.buildISS(scene),
      solid: { id: 'ISS', x: 0, y: 0, z: 0, radius: 0.7, mass: 0.05 }
    });

    // ---- the other stations: Skylab, Mir, Tiangong ----
    const stationDefs: Array<[string, number, number]> = [
      ['Skylab', 1.45, 0.7], ['Mir', 1.5, 0.55], ['Tiangong', 1.35, 0.6]
    ];
    stationDefs.forEach(([id, radius, incl], i) => {
      this.artifacts.push({
        id,
        orbit: { radius, inclination: incl, speed: 0.32 + i * 0.03, phase: i * 1.7 },
        root: this.buildStation(scene, id),
        solid: { id, x: 0, y: 0, z: 0, radius: 0.5, mass: 0.03 }
      });
    });

    // ---- the satellite field: small bodies with two panels each ----
    for (let i = 0; i < 22; i++) {
      const s = (seed ^ Math.imul(i + 1, 2654435761)) >>> 0;
      const orbit: Orbit = {
        radius: 1.15 + hash01(s + 1) * 1.1,
        inclination: (hash01(s + 2) - 0.5) * 2.4,
        speed: 0.25 + hash01(s + 3) * 0.5,
        phase: hash01(s + 4) * Math.PI * 2
      };
      this.artifacts.push({
        id: 'Sat-' + (i + 1),
        orbit,
        root: this.buildSatellite(scene, 'sat' + i, hash01(s + 5)),
        solid: null
      });
    }

    // ---- Hubble and Webb, the two famous eyes ----
    this.artifacts.push({
      id: 'Hubble',
      orbit: { radius: 1.7, inclination: 0.5, speed: 0.3, phase: 1.2 },
      root: this.buildTelescope(scene, 'hubble', false),
      solid: null
    });
    this.artifacts.push({
      id: 'Webb',
      orbit: { radius: 2.2, inclination: 0.2, speed: 0.22, phase: 2.6 },
      root: this.buildTelescope(scene, 'webb', true),
      solid: null
    });

    // ---- the GPS constellation: six planes, four satellites each ----
    for (let p = 0; p < GPS_PLANES; p++) {
      for (let k = 0; k < GPS_PER_PLANE; k++) {
        this.artifacts.push({
          id: 'GPS-' + p + '-' + k,
          orbit: {
            radius: 1.2,
            inclination: 0.96 + (p * 0.08),
            speed: 0.38,
            phase: (k / GPS_PER_PLANE) * Math.PI * 2 + p * 0.5
          },
          root: this.buildSatellite(scene, 'gps' + p + k, 0.5),
          solid: null
        });
      }
    }

    // ---- the Apollo stack: command module, service module, LM ----
    const apollo = this.buildApollo(scene);
    this.artifacts.push({
      id: 'Apollo',
      orbit: { radius: 1.9, inclination: 0.6, speed: 0.28, phase: 3.8 },
      root: apollo.root,
      solid: apollo.solid
    });

    this.built = true;
  }

  private buildISS(scene: Scene): TransformNode {
    const root = new TransformNode('iss', scene);
    const steel = new StandardMaterial('issM', scene);
    steel.diffuseColor = new Color3(0.9, 0.9, 0.92);
    steel.specularColor = new Color3(0.4, 0.4, 0.45);

    const body = MeshBuilder.CreateCylinder('issBody', { diameter: 0.32, height: 1.2, tessellation: 12 }, scene);
    body.parent = root;
    body.material = steel;

    const truss = MeshBuilder.CreateBox('issTruss', { size: 0.14 }, scene);
    truss.parent = root;
    truss.scaling.set(1, 0.2, 8);
    truss.material = steel;

    const gold = new StandardMaterial('issGold', scene);
    gold.diffuseColor = new Color3(0.75, 0.5, 0.2);
    gold.emissiveColor = new Color3(0.3, 0.18, 0.05);
    for (let i = 0; i < 4; i++) {
      const wing = MeshBuilder.CreatePlane('issWing' + i, { size: 0.9 }, scene);
      wing.parent = root;
      wing.rotation.y = (i * Math.PI) / 2;
      wing.position.x = Math.cos((i * Math.PI) / 2) * 2.2;
      wing.position.z = Math.sin((i * Math.PI) / 2) * 2.2;
      wing.material = gold;
    }
    return root;
  }

  private buildSatellite(scene: Scene, id: string, tint: number): TransformNode {
    const root = new TransformNode(id, scene);
    const m = new StandardMaterial(id + 'M', scene);
    m.diffuseColor = new Color3(0.7, 0.72, 0.78);
    m.specularColor = new Color3(0.4, 0.4, 0.45);
    const body = MeshBuilder.CreateBox(id + 'B', { size: 0.12 }, scene);
    body.parent = root;
    body.material = m;
    const gold = new StandardMaterial(id + 'G', scene);
    gold.diffuseColor = new Color3(0.7, 0.5 + tint * 0.3, 0.25);
    gold.emissiveColor = new Color3(0.2, 0.12 + tint * 0.1, 0.04);
    for (let i = 0; i < 2; i++) {
      const panel = MeshBuilder.CreatePlane(id + 'P' + i, { size: 0.34 }, scene);
      panel.parent = root;
      panel.rotation.y = i * Math.PI;
      panel.position.x = (i === 0 ? 0.22 : -0.22);
      panel.material = gold;
    }
    return root;
  }

  /** A small station: a habitat drum, a few modules and a solar truss. */
  private buildStation(scene: Scene, id: string): TransformNode {
    const root = new TransformNode(id, scene);
    const m = new StandardMaterial(id + 'M', scene);
    m.diffuseColor = new Color3(0.88, 0.9, 0.93);
    m.specularColor = new Color3(0.4, 0.4, 0.45);

    const drum = MeshBuilder.CreateCylinder(id + 'D', { diameter: 0.34, height: 0.5, tessellation: 12 }, scene);
    drum.parent = root;
    drum.material = m;
    const mod = MeshBuilder.CreateBox(id + 'X', { size: 0.16 }, scene);
    mod.parent = root;
    mod.position.x = 0.3;
    mod.material = m;

    const gold = new StandardMaterial(id + 'G', scene);
    gold.diffuseColor = new Color3(0.75, 0.5, 0.2);
    gold.emissiveColor = new Color3(0.25, 0.15, 0.04);
    for (let i = 0; i < 2; i++) {
      const wing = MeshBuilder.CreatePlane(id + 'W' + i, { size: 0.7 }, scene);
      wing.parent = root;
      wing.rotation.y = i * Math.PI;
      wing.position.x = (i === 0 ? -0.6 : 0.6);
      wing.material = gold;
    }
    return root;
  }

  private buildTelescope(scene: Scene, id: string, webb: boolean): TransformNode {
    const root = new TransformNode(id, scene);
    const m = new StandardMaterial(id + 'M', scene);
    m.diffuseColor = new Color3(0.85, 0.85, 0.88);
    m.specularColor = new Color3(0.35, 0.35, 0.4);
    const tube = MeshBuilder.CreateCylinder(id + 'T', { diameter: 0.22, height: 0.7, tessellation: 12 }, scene);
    tube.parent = root;
    tube.material = m;
    if (webb) {
      // Webb: a gold hexagonal mirror plate.
      const gold = new StandardMaterial(id + 'G', scene);
      gold.diffuseColor = new Color3(0.8, 0.62, 0.25);
      gold.emissiveColor = new Color3(0.3, 0.2, 0.05);
      const mirror = MeshBuilder.CreateCylinder(id + 'M', { diameter: 0.5, height: 0.04, tessellation: 6 }, scene);
      mirror.parent = root;
      mirror.position.y = 0.38;
      mirror.material = gold;
    }
    return root;
  }

  private buildApollo(scene: Scene): { root: TransformNode; solid: SolidSphere } {
    const root = new TransformNode('apollo', scene);
    const m = new StandardMaterial('apolloM', scene);
    m.diffuseColor = new Color3(0.9, 0.92, 0.94);
    m.specularColor = new Color3(0.4, 0.4, 0.45);

    const cm = MeshBuilder.CreateCylinder('apolloCM', { diameter: 0.3, height: 0.24, tessellation: 16 }, scene);
    cm.parent = root;
    cm.material = m;
    const sm = MeshBuilder.CreateCylinder('apolloSM', { diameter: 0.3, height: 0.4, tessellation: 16 }, scene);
    sm.parent = root;
    sm.position.y = -0.32;
    sm.material = m;
    const lm = MeshBuilder.CreateBox('apolloLM', { size: 0.2 }, scene);
    lm.parent = root;
    lm.position.y = -0.75;
    lm.material = m;

    return { root, solid: { id: 'Apollo', x: 0, y: 0, z: 0, radius: 0.4, mass: 0.02 } };
  }

  /** Advances every orbit around the home world's centre. */
  update(dt: number, center: Vector3): void {
    if (!Number.isFinite(dt) || dt <= 0) return;
    this.t += dt;
    for (const a of this.artifacts) {
      const p = orbitPosition(a.orbit, this.t, center.x, center.y, center.z);
      a.root.position.set(p[0], p[1], p[2]);
      a.root.rotation.y += dt * 0.4;
      if (a.solid) {
        a.solid.x = p[0];
        a.solid.y = p[1];
        a.solid.z = p[2];
      }
    }
  }

  /** Solid bodies (Apollo), so the player can land and walk on it. */
  solids(): SolidSphere[] {
    const out: SolidSphere[] = [];
    for (const a of this.artifacts) {
      if (a.solid) out.push(a.solid);
    }
    return out;
  }

  stats(): Record<string, string> {
    return { 'Orbital artifacts': this.built ? String(this.artifacts.length) : 'off' };
  }

  dispose(): void {
    for (const a of this.artifacts) a.root.dispose(false, true);
    this.artifacts = [];
    this.built = false;
    this.scene = null;
  }
}
