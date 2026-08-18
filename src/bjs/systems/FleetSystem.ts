/**
 * FleetSystem — flying a ship, and launching ships of your own.
 *
 * Two things this adds that the sandbox did not have:
 *
 *  1. A ship you pilot, with a first-person seat inside it and an external
 *     chase view, switchable at any moment. The camera is derived from the
 *     ship's own transform in both cases, so the two views can never
 *     disagree about where the ship is or which way it is pointing.
 *
 *  2. Fleets you launch. A fleet is not decoration: every vessel carries
 *     mass, and enough mass in one place is a gravitational source like any
 *     other. Pack a large enough fleet into a small enough volume and it
 *     will hold itself together - and start pulling on you.
 *
 * As always the interesting behaviour is computed, not scripted. The
 * question "how many ships do I need before the fleet has its own gravity?"
 * has a real answer, and it comes out of these functions.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';
import { toRenderRef } from './RenderOrigin';

/** One class of vessel. */
export interface ShipClass {
  id: string;
  name: string;
  glyph: string;
  /** Mass, kg. */
  mass: number;
  /** Length, m. */
  length: number;
  /** Cruise speed, world units/sec. */
  speed: number;
  /** How tightly it holds formation, 0-1. */
  discipline: number;
}

export const SHIP_CLASSES: ShipClass[] = [
  { id: 'scout',      name: 'Scout',       glyph: '🛩', mass: 4.0e4,  length: 18,   speed: 240, discipline: 0.55 },
  { id: 'fighter',    name: 'Fighter',     glyph: '✈', mass: 1.2e5,  length: 26,   speed: 300, discipline: 0.7 },
  { id: 'frigate',    name: 'Frigate',     glyph: '🚀', mass: 8.0e6,  length: 140,  speed: 160, discipline: 0.82 },
  { id: 'cruiser',    name: 'Cruiser',     glyph: '🛸', mass: 9.0e7,  length: 420,  speed: 110, discipline: 0.9 },
  { id: 'dreadnought',name: 'Dreadnought', glyph: '🛰', mass: 4.0e9,  length: 1800, speed: 60,  discipline: 0.96 },
  // Deliberately absurd, and therefore the one that makes its own gravity.
  { id: 'worldship',  name: 'World Ship',  glyph: '🏛', mass: 2.0e18, length: 90000, speed: 24, discipline: 0.99 }
];

const BY_ID = new Map(SHIP_CLASSES.map((s) => [s.id, s]));
export function shipClass(id: string): ShipClass | null { return BY_ID.get(id) ?? null; }

/* -------------------------------------------------------------------------- */
/*  Camera modes                                                               */
/* -------------------------------------------------------------------------- */

export type ViewMode = 'cockpit' | 'chase' | 'orbit-ship';

export interface ShipView {
  position: Vector3;
  target: Vector3;
  /** Field of view in radians; the cockpit is tighter. */
  fov: number;
}

/**
 * Where the camera goes for a given view of a ship.
 *
 * Both views are built from the same forward/up basis, so switching between
 * them cannot introduce a discontinuity in where the ship appears to be
 * pointing - the classic bug when cockpit and chase cameras are maintained
 * separately.
 */
export function shipView(
  mode: ViewMode, position: Vector3, forward: Vector3, up: Vector3, length: number
): ShipView {
  const f = forward.lengthSquared() > 1e-9 ? forward.normalize() : new Vector3(0, 0, 1);
  const u = up.lengthSquared() > 1e-9 ? up.normalize() : new Vector3(0, 1, 0);

  if (mode === 'cockpit') {
    // Seated at the front, eyes just above the centreline.
    const eye = position
      .add(f.scale(length * 0.36))
      .add(u.scale(length * 0.05));
    return { position: eye, target: eye.add(f.scale(1000)), fov: 0.82 };
  }

  if (mode === 'orbit-ship') {
    // Standing off to admire it, which is the whole point of a big ship.
    const side = Vector3.Cross(u, f).normalize();
    return {
      position: position.add(side.scale(length * 2.2)).add(u.scale(length * 0.7)),
      target: position.clone(),
      fov: 0.9
    };
  }

  // Chase: behind and above, looking slightly ahead of the ship so you can
  // see where you are going rather than where you have been.
  return {
    position: position.subtract(f.scale(length * 2.6)).add(u.scale(length * 0.85)),
    target: position.add(f.scale(length * 1.5)),
    fov: 0.95
  };
}

/* -------------------------------------------------------------------------- */
/*  Fleet gravity                                                              */
/* -------------------------------------------------------------------------- */

export const G = 6.674e-11;

export interface FleetGravity {
  /** Total mass of the fleet, kg. */
  mass: number;
  /** Radius of the volume it occupies, m. */
  radius: number;
  /** Surface gravity at that radius, m/s^2. */
  surfaceGravity: number;
  /** Speed needed to escape the fleet, m/s. */
  escapeVelocity: number;
  /** True once the fleet's pull is something a person would notice. */
  significant: boolean;
  /** True when the fleet is dense enough to collapse under its own weight. */
  selfBinding: boolean;
}

/**
 * The gravity of a fleet packed into a sphere.
 *
 * g = GM/r^2 and v_esc = sqrt(2GM/r) - no special cases. "Significant" is
 * set at 0.01 m/s^2, about a thousandth of a gee, which is roughly where a
 * drifting object's motion visibly curves rather than staying straight.
 */
export function fleetGravity(totalMass: number, radius: number): FleetGravity {
  const m = Math.max(0, totalMass);
  const r = Math.max(radius, 1e-6);
  const g = (G * m) / (r * r);
  const esc = Math.sqrt((2 * G * m) / r);
  return {
    mass: m,
    radius: r,
    surfaceGravity: g,
    escapeVelocity: esc,
    significant: g > 0.01,
    // Once escape velocity exceeds a vessel's own cruise speed, ships cannot
    // leave under their own power: the formation is gravitationally bound.
    selfBinding: esc > 300
  };
}

/**
 * How many ships of a class it takes for the fleet to have noticeable
 * gravity at a given formation radius.
 *
 * Returns Infinity when it is not achievable - which is the honest answer
 * for a squadron of scouts, however many you launch.
 */
export function shipsForGravity(
  cls: ShipClass, formationRadius: number, targetG = 0.01
): number {
  const r = Math.max(formationRadius, 1e-6);
  const massNeeded = (targetG * r * r) / G;
  const n = Math.ceil(massNeeded / cls.mass);
  return Number.isFinite(n) && n > 0 ? n : Infinity;
}

/* -------------------------------------------------------------------------- */
/*  The fleet itself                                                           */
/* -------------------------------------------------------------------------- */

export interface Vessel {
  id: string;
  cls: ShipClass;
  position: Vector3;
  velocity: Vector3;
  /** Slot in the formation, relative to the fleet centre. */
  slot: Vector3;
  /** Its body in the scene, if the fleet has been given one. */
  mesh?: Mesh;
}

export interface FleetOptions {
  /** How tightly ships pack together, m between slots. */
  spacing: number;
  /** How hard they correct back to formation. */
  cohesion: number;
}

export const DEFAULT_FLEET: FleetOptions = { spacing: 120, cohesion: 1.8 };

/**
 * A launched fleet.
 *
 * Formation is a golden-angle spiral on a sphere, which distributes any
 * number of ships evenly without the clumping you get from nested rings and
 * without needing a special case for small counts.
 */
export class Fleet {
  vessels: Vessel[] = [];
  center = new Vector3(0, 0, 0);
  destination: Vector3 | null = null;
  opts: FleetOptions;
  private counter = 0;
  private scene: Scene | null = null;
  /** One material per class, shared by every ship of that class. */
  private mats = new Map<string, StandardMaterial>();

  constructor(opts: Partial<FleetOptions> = {}) {
    this.opts = { ...DEFAULT_FLEET, ...opts };
  }

  /**
   * Gives the fleet a scene to appear in. Without one it still simulates -
   * which is what makes the physics testable headlessly - but draws nothing.
   */
  attach(scene: Scene): void {
    this.scene = scene;
    this.mats.clear();
  }

  /** Screen size for a class, compressed so a world ship is not a wall. */
  static visualSize(lengthMetres: number): number {
    return Math.max(0.6, Math.min(60, Math.cbrt(lengthMetres) * 1.6));
  }

  private materialFor(cls: ShipClass): StandardMaterial | null {
    if (!this.scene) return null;
    const found = this.mats.get(cls.id);
    if (found) return found;
    const m = new StandardMaterial('fleet-' + cls.id, this.scene);
    // Bigger ships read colder and dimmer; the little ones glow hot.
    const t = Math.min(1, Math.log10(cls.mass) / 18);
    m.diffuseColor = new Color3(0.55 - t * 0.25, 0.62 - t * 0.2, 0.78);
    m.emissiveColor = new Color3(0.16 + (1 - t) * 0.5, 0.35, 0.75);
    m.specularColor = new Color3(0.4, 0.45, 0.5);
    this.mats.set(cls.id, m);
    return m;
  }

  /** Launches `count` ships of a class, centred on `at`. */
  launch(cls: ShipClass, count: number, at: Vector3): Vessel[] {
    const n = Math.max(0, Math.floor(count));
    const made: Vessel[] = [];
    this.center.copyFrom(at);

    const golden = Math.PI * (3 - Math.sqrt(5));
    for (let i = 0; i < n; i++) {
      // Even distribution over a sphere.
      const y = n === 1 ? 0 : 1 - (i / (n - 1)) * 2;
      const rad = Math.sqrt(Math.max(0, 1 - y * y));
      const theta = golden * i;
      const shell = this.opts.spacing * Math.cbrt(n) * 0.5;
      const slot = new Vector3(
        Math.cos(theta) * rad * shell,
        y * shell,
        Math.sin(theta) * rad * shell
      );
      const v: Vessel = {
        id: 'v' + (++this.counter),
        cls,
        position: at.add(slot),
        velocity: Vector3.Zero(),
        slot
      };
      if (this.scene) {
        const size = Fleet.visualSize(cls.length);
        // A stretched box reads as a hull at any distance and costs almost
        // nothing next to a modelled ship.
        const mesh = MeshBuilder.CreateBox(
          'ship-' + v.id,
          { width: size * 0.34, height: size * 0.22, depth: size },
          this.scene);
        toRenderRef(v.position,mesh.position);
        mesh.isPickable = false;
        const mat = this.materialFor(cls);
        if (mat) mesh.material = mat;
        v.mesh = mesh;
      }
      this.vessels.push(v);
      made.push(v);
    }
    return made;
  }

  /** Total mass of everything launched. */
  totalMass(): number {
    let m = 0;
    for (const v of this.vessels) m += v.cls.mass;
    return m;
  }

  /** Radius of the volume the fleet occupies. */
  formationRadius(): number {
    if (!this.vessels.length) return 0;
    let max = 0;
    for (const v of this.vessels) {
      max = Math.max(max, Vector3.Distance(v.position, this.center));
    }
    return Math.max(max, 1);
  }

  /** The fleet's own gravity, right now. */
  gravity(): FleetGravity {
    return fleetGravity(this.totalMass(), this.formationRadius());
  }

  /** Sends the whole formation somewhere. */
  moveTo(dest: Vector3): void {
    this.destination = dest.clone();
  }

  /** Disbands the fleet. */
  clear(): void {
    for (const v of this.vessels) {
      try { v.mesh?.dispose(); } catch { /* already gone */ }
    }
    this.vessels.length = 0;
    this.destination = null;
  }

  /** Drops the scene and everything in it. */
  dispose(): void {
    this.clear();
    for (const m of this.mats.values()) {
      try { m.dispose(); } catch { /* already gone */ }
    }
    this.mats.clear();
    this.scene = null;
  }

  /**
   * Advances the fleet. Ships fly toward their slot in the formation while
   * the formation itself flies toward its destination.
   */
  update(dt: number): void {
    if (!Number.isFinite(dt) || dt <= 0 || !this.vessels.length) return;

    // Move the formation centre toward the destination at the speed of its
    // slowest ship, so nothing is ever left behind.
    if (this.destination) {
      let slowest = Infinity;
      for (const v of this.vessels) slowest = Math.min(slowest, v.cls.speed);
      const toDest = this.destination.subtract(this.center);
      const d = toDest.length();
      if (d < 1) {
        this.destination = null;
      } else {
        const step = Math.min(d, slowest * dt);
        this.center.addInPlace(toDest.scale(step / d));
      }
    }

    for (const v of this.vessels) {
      const want = this.center.add(v.slot);
      const delta = want.subtract(v.position);
      // Discipline decides how tightly a class holds station.
      const k = this.opts.cohesion * v.cls.discipline;
      v.velocity.addInPlace(delta.scale(k * dt));
      // Damping, or they oscillate about their slots forever.
      v.velocity.scaleInPlace(Math.max(0, 1 - 2.4 * dt));
      v.position.addInPlace(v.velocity.scale(dt));

      if (v.mesh) {
        toRenderRef(v.position,v.mesh.position);
        // Face the way it is travelling, so a moving fleet looks like one.
        if (v.velocity.lengthSquared() > 1e-4) {
          const f = v.velocity.normalize();
          v.mesh.rotation.y = Math.atan2(f.x, f.z);
          v.mesh.rotation.x = -Math.asin(Math.max(-1, Math.min(1, f.y)));
        }
      }
    }
  }

  stats(): Record<string, string> {
    const g = this.gravity();
    return {
      'Fleet size': String(this.vessels.length),
      'Fleet mass': this.vessels.length ? g.mass.toExponential(2) + ' kg' : '—',
      'Fleet gravity': g.significant
        ? g.surfaceGravity.toFixed(3) + ' m/s²'
        : 'negligible',
      'Bound': g.selfBinding ? 'yes - ships cannot leave' : 'no'
    };
  }
}
