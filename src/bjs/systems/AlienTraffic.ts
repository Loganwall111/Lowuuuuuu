/**
 * AlienTraffic — very rare, very large ships that pass through.
 *
 * The alien fleets that arrive to dismantle a world are an event you cause.
 * This is the opposite: a lone, enormous craft that appears on its own
 * schedule, crosses the sky far away, and is gone - the sense that the
 * universe is inhabited by something that is not waiting for you. It is
 * deliberately rare: minutes of quiet between sightings, so seeing one is a
 * genuine moment rather than background traffic.
 *
 * The schedule is pure and deterministic from the universe seed, so the
 * same sighting happens at the same time every session; only the ship mesh
 * touches Babylon.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Scene } from '@babylonjs/core/scene';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';

export interface WandererState {
  /** True while a ship is currently passing through. */
  present: boolean;
  /** World position of the ship, updated along its arc. */
  x: number; y: number; z: number;
  /** Which of a few enormous hull classes this is. */
  cls: number;
}

/** Minutes between sightings. Rare, but it does happen. */
export const SIGHTING_INTERVAL = 480;
/** How long a pass takes, seconds. */
export const PASS_DURATION = 180;

/** Deterministic 0..1 hash. */
function hash01(seed: number): number {
  let h = seed >>> 0 || 1;
  h = Math.imul(h ^ (h >>> 16), 2246822519) >>> 0;
  h = Math.imul(h ^ (h >>> 13), 3266489917) >>> 0;
  return ((h ^ (h >>> 16)) >>> 0) / 4294967296;
}

/**
 * The pure schedule. At time `t` (seconds), is a wanderer passing, and
 * where along its great arc does it sit?
 */
export function wandererAt(seed: number, t: number): WandererState {
  const s = Math.max(0, Number.isFinite(t) ? t : 0);
  const slot = Math.floor(s / SIGHTING_INTERVAL);
  const local = s - slot * SIGHTING_INTERVAL;

  // Not every slot has a ship; a seeded draw skips most of them, so the
  // gap between sightings is long and irregular.
  const roll = hash01((seed ^ Math.imul(slot + 1, 2654435761)) >>> 0);
  if (roll > 0.34) {
    return { present: false, x: 0, y: 0, z: 0, cls: 0 };
  }
  if (local > PASS_DURATION) {
    return { present: false, x: 0, y: 0, z: 0, cls: 0 };
  }

  const cls = Math.floor(hash01((seed ^ Math.imul(slot + 7, 2246822519)) >>> 0) * 3);
  const k = local / PASS_DURATION;
  // A long arc across the sky, far out, so the ship is enormous but distant.
  const ang = -Math.PI * 0.8 + k * Math.PI * 1.6;
  const dist = 1200 + cls * 600;
  const y = (k - 0.5) * 500;
  return {
    present: true,
    x: Math.cos(ang) * dist,
    y,
    z: Math.sin(ang) * dist,
    cls
  };
}

export class AlienTraffic {
  private scene: Scene | null = null;
  private mesh: Mesh | null = null;
  private t = 0;
  private seed = 1;
  private last = false;

  get visible(): boolean { return this.last; }

  attach(scene: Scene, seed: number): void {
    this.scene = scene;
    this.seed = seed >>> 0;
  }

  /** Builds the lone hull once; it is shown only while a ship is passing. */
  build(): void {
    const scene = this.scene;
    if (!scene || this.mesh) return;
    const m = new StandardMaterial('alienTrafficM', scene);
    m.emissiveColor = new Color3(0.2, 0.65, 0.9);
    m.diffuseColor = new Color3(0.08, 0.1, 0.14);
    m.specularColor = Color3.Black();

    const root = MeshBuilder.CreateBox('alienTraffic', { size: 1 }, scene);
    root.scaling.set(90, 12, 26);      // an enormous, elongated hull
    root.material = m;
    root.isPickable = false;
    root.setEnabled(false);
    this.mesh = root;
  }

  update(dt: number, eye: Vector3): void {
    if (!Number.isFinite(dt) || dt <= 0) return;
    this.t += dt;
    const st = wandererAt(this.seed, this.t);
    this.last = st.present;
    if (!this.mesh) return;
    this.mesh.setEnabled(st.present);
    if (st.present) {
      // Drift slowly, always far out, and face the direction of travel.
      this.mesh.position.set(st.x, st.y, st.z);
      this.mesh.rotation.y = Math.atan2(st.x, st.z) + this.t * 0.02;
    }
  }

  stats(): Record<string, string> {
    return { 'Alien traffic': this.last ? 'a ship is passing' : 'quiet' };
  }

  dispose(): void {
    this.mesh?.dispose();
    this.mesh = null;
    this.scene = null;
  }
}
