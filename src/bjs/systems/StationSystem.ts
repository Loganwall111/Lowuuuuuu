/**
 * StationSystem — procedural space stations you can fly to and walk inside.
 *
 * Stations are generated from a seed like everything else, so each one is
 * its own place: a different module layout, a different ring count, a
 * different set of windows looking back down at the planet it orbits.
 *
 * Modules are simple primitives assembled by rule rather than modelled, so
 * a station is cheap to build and every one is unique. The interior is a
 * real walkable volume: floors are collidable surfaces the walk mode's
 * ground probe can answer against.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Scene } from '@babylonjs/core/scene';

export type ModuleKind = 'hub' | 'ring' | 'spar' | 'pod' | 'array' | 'dock' | 'cupola';

export interface StationModule {
  kind: ModuleKind;
  mesh: Mesh;
  /** Local offset from the station origin. */
  offset: Vector3;
  radius: number;
  /** Walkable floor height in local space, if this module has an interior. */
  floorY: number | null;
}

export interface StationSpec {
  id: string;
  name: string;
  seed: number;
  /** World position. */
  position: Vector3;
  modules: StationModule[];
  /** Overall bounding radius, for docking approach and culling. */
  radius: number;
  /** Rotation rate of the habitat ring, rad/s. */
  spin: number;
}

const PREFIX = ['Kepler', 'Tsiolkovsky', 'Anders', 'Vostok', 'Meridian',
  'Halcyon', 'Kuiper', 'Sagan', 'Ozma', 'Terminus'];
const SUFFIX = ['Station', 'Outpost', 'Platform', 'Array', 'Waypoint', 'Yard'];

function mulberry32(seed: number): () => number {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/** Names a station deterministically. */
export function stationName(seed: number): string {
  const rng = mulberry32(seed ^ 0x1b873593);
  const p = PREFIX[Math.floor(rng() * PREFIX.length)];
  const s = SUFFIX[Math.floor(rng() * SUFFIX.length)];
  const n = 1 + Math.floor(rng() * 40);
  return `${p} ${s} ${n}`;
}

export class StationSystem {
  private scene: Scene;
  private stations: StationSpec[] = [];
  private mats: StandardMaterial[] = [];
  private hullMat: StandardMaterial | null = null;
  private glassMat: StandardMaterial | null = null;
  private lightMat: StandardMaterial | null = null;
  private t = 0;

  constructor(scene: Scene) {
    this.scene = scene;
    this.buildMaterials();
  }

  get count(): number { return this.stations.length; }
  get list(): readonly StationSpec[] { return this.stations; }

  private buildMaterials(): void {
    const hull = new StandardMaterial('stnHull', this.scene);
    hull.diffuseColor = new Color3(0.62, 0.64, 0.68);
    hull.specularColor = new Color3(0.5, 0.52, 0.56);
    hull.specularPower = 48;
    // Never pure black on the unlit side.
    hull.emissiveColor = new Color3(0.05, 0.055, 0.07);
    this.hullMat = hull;

    // Glass: reflective windows looking back at the planet.
    const glass = new StandardMaterial('stnGlass', this.scene);
    glass.diffuseColor = new Color3(0.04, 0.09, 0.15);
    glass.specularColor = new Color3(1, 1, 1);
    glass.specularPower = 220;
    glass.emissiveColor = new Color3(0.06, 0.13, 0.22);
    glass.alpha = 0.55;
    glass.backFaceCulling = false;
    this.glassMat = glass;

    const lit = new StandardMaterial('stnLight', this.scene);
    lit.emissiveColor = new Color3(1.0, 0.86, 0.55);
    lit.disableLighting = true;
    this.lightMat = lit;

    this.mats.push(hull, glass, lit);
  }

  /**
   * Builds a station at a position. Layout, module count and ring count all
   * come from the seed, so no two are the same.
   */
  spawn(id: string, seed: number, position: Vector3, scale = 1): StationSpec {
    const rng = mulberry32(seed);
    const modules: StationModule[] = [];

    const add = (
      kind: ModuleKind, mesh: Mesh, offset: Vector3, radius: number, floorY: number | null
    ) => {
      mesh.position.copyFrom(position.add(offset));
      mesh.isPickable = true;
      modules.push({ kind, mesh, offset, radius, floorY });
    };

    // ---- central hub: always present, always walkable ----
    const hubR = (5 + rng() * 4) * scale;
    const hub = MeshBuilder.CreateCylinder('stnHub_' + id,
      { diameter: hubR * 2, height: hubR * 1.5, tessellation: 20 }, this.scene);
    hub.material = this.hullMat;
    add('hub', hub, Vector3.Zero(), hubR, -hubR * 0.75);

    // ---- habitat rings ----
    const rings = 1 + Math.floor(rng() * 3);
    for (let i = 0; i < rings; i++) {
      const rr = hubR * (2.2 + i * 1.3);
      const torus = MeshBuilder.CreateTorus('stnRing_' + id + '_' + i,
        { diameter: rr * 2, thickness: hubR * 0.5, tessellation: 40 }, this.scene);
      torus.material = this.hullMat;
      const y = (rng() - 0.5) * hubR * 2;
      add('ring', torus, new Vector3(0, y, 0), rr, y);

      // Spars connecting the ring to the hub.
      const spars = 3 + Math.floor(rng() * 3);
      for (let sIdx = 0; sIdx < spars; sIdx++) {
        const a = (sIdx / spars) * Math.PI * 2;
        const spar = MeshBuilder.CreateBox('stnSpar_' + id + '_' + i + '_' + sIdx,
          { width: rr, height: hubR * 0.18, depth: hubR * 0.18 }, this.scene);
        spar.material = this.hullMat;
        spar.rotation.y = -a;
        add('spar', spar,
          new Vector3(Math.cos(a) * rr * 0.5, y, Math.sin(a) * rr * 0.5),
          rr * 0.5, null);
      }
    }

    // ---- habitation pods ----
    const pods = 2 + Math.floor(rng() * 5);
    for (let i = 0; i < pods; i++) {
      const a = rng() * Math.PI * 2;
      const d = hubR * (1.6 + rng() * 2.4);
      const pr = hubR * (0.35 + rng() * 0.35);
      const pod = MeshBuilder.CreateCapsule('stnPod_' + id + '_' + i,
        { radius: pr, height: pr * 3.4, tessellation: 12 }, this.scene);
      pod.material = this.hullMat;
      pod.rotation.z = Math.PI / 2;
      add('pod', pod,
        new Vector3(Math.cos(a) * d, (rng() - 0.5) * hubR * 2.4, Math.sin(a) * d),
        pr, null);
    }

    // ---- solar arrays ----
    const arrays = 2 + Math.floor(rng() * 4);
    for (let i = 0; i < arrays; i++) {
      const a = (i / arrays) * Math.PI * 2 + rng();
      const d = hubR * (3 + rng() * 2);
      const panel = MeshBuilder.CreateBox('stnArray_' + id + '_' + i,
        { width: hubR * 2.6, height: hubR * 0.06, depth: hubR * 1.1 }, this.scene);
      const pm = new StandardMaterial('stnPanel_' + id + '_' + i, this.scene);
      pm.diffuseColor = new Color3(0.09, 0.13, 0.32);
      pm.specularColor = new Color3(0.7, 0.75, 0.9);
      pm.emissiveColor = new Color3(0.03, 0.05, 0.12);
      panel.material = pm;
      this.mats.push(pm);
      panel.rotation.y = a;
      add('array', panel,
        new Vector3(Math.cos(a) * d, (rng() - 0.5) * hubR, Math.sin(a) * d),
        hubR * 1.3, null);
    }

    // ---- cupola: the window you look down at the planet from ----
    const cup = MeshBuilder.CreateSphere('stnCupola_' + id,
      { diameter: hubR * 1.25, segments: 16 }, this.scene);
    cup.material = this.glassMat;
    add('cupola', cup, new Vector3(0, -hubR * 1.1, 0), hubR * 0.62, -hubR * 1.1);

    // ---- docking port ----
    const dock = MeshBuilder.CreateCylinder('stnDock_' + id,
      { diameter: hubR * 0.7, height: hubR * 1.2, tessellation: 12 }, this.scene);
    dock.material = this.lightMat;
    add('dock', dock, new Vector3(0, hubR * 1.3, 0), hubR * 0.35, null);

    let radius = hubR;
    for (const m of modules) {
      radius = Math.max(radius, m.offset.length() + m.radius);
    }

    const spec: StationSpec = {
      id,
      name: stationName(seed),
      seed,
      position: position.clone(),
      modules,
      radius,
      spin: 0.03 + rng() * 0.07
    };
    this.stations.push(spec);
    return spec;
  }

  /** Slowly rotates habitat rings. */
  update(dt: number): void {
    this.t += dt;
    for (const st of this.stations) {
      for (const m of st.modules) {
        if (m.kind === 'ring' || m.kind === 'spar') {
          m.mesh.rotation.y += st.spin * dt;
        }
      }
    }
  }

  /** The station nearest a point, if any is within `maxDist`. */
  nearest(pos: Vector3, maxDist = Infinity): StationSpec | null {
    let best: StationSpec | null = null;
    let bestD = maxDist;
    for (const st of this.stations) {
      const d = Vector3.Distance(pos, st.position);
      if (d < bestD) { bestD = d; best = st; }
    }
    return best;
  }

  /** True when a point is inside a station's envelope, i.e. you are aboard. */
  isAboard(pos: Vector3): StationSpec | null {
    for (const st of this.stations) {
      if (Vector3.Distance(pos, st.position) < st.radius) return st;
    }
    return null;
  }

  /**
   * Walkable floor height at a world position, or null if there is no deck
   * there. Lets the existing walk mode work aboard a station unchanged.
   */
  floorAt(x: number, z: number): { height: number; normal: Vector3 } | null {
    for (const st of this.stations) {
      for (const m of st.modules) {
        if (m.floorY === null) continue;
        const cx = st.position.x + m.offset.x;
        const cz = st.position.z + m.offset.z;
        const d = Math.hypot(x - cx, z - cz);
        if (d <= m.radius) {
          return {
            height: st.position.y + m.floorY,
            normal: new Vector3(0, 1, 0)
          };
        }
      }
    }
    return null;
  }

  remove(id: string): boolean {
    const i = this.stations.findIndex((s) => s.id === id);
    if (i < 0) return false;
    this.stations[i].modules.forEach((m) => m.mesh.dispose());
    this.stations.splice(i, 1);
    return true;
  }

  stats(): Record<string, string> {
    return {
      'Stations': String(this.stations.length),
      'Station modules': String(
        this.stations.reduce((n, s) => n + s.modules.length, 0))
    };
  }

  dispose(): void {
    this.stations.forEach((s) => s.modules.forEach((m) => m.mesh.dispose()));
    this.stations = [];
    this.mats.forEach((m) => m.dispose());
    this.mats = [];
  }
}
