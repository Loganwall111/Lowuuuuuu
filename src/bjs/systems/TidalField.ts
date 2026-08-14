/**
 * TidalField — makes spaghettification something you watch, not something
 * you are told about.
 *
 * GameModes works out how stretched a body should be. This applies that to
 * real meshes: anything registered here is scaled along the line to the
 * nearest black hole and squeezed across it, pulled inward, and finally
 * removed when it crosses the horizon.
 *
 * The stretch axis is the radial direction, so a ship falling in elongates
 * toward the hole rather than along whatever axis it was modelled on. That
 * is the whole visual: a thing becoming a thread pointed at a point.
 *
 * Only active when the mode allows it. In Explorer mode `update` is never
 * called and every body keeps the scale it was built with, which is why
 * leaving sandbox has to restore them rather than merely stop stretching.
 */

import { Vector3, Quaternion } from '@babylonjs/core/Maths/math.vector';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import { tidalState, type TidalBody, type TidalState } from './GameModes';

/** A hole that can tear things apart. */
export interface TidalSource {
  position: Vector3;
  /** Horizon radius, world units. */
  horizon: number;
}

/** Something being acted on. */
interface Victim {
  id: string;
  mesh: Mesh;
  body: TidalBody;
  /** The scale it had before any stretching, so it can be restored. */
  baseScale: Vector3;
  /** Its own rotation, preserved and re-applied around the stretch. */
  state: TidalState;
  /** True once it has crossed a horizon and been hidden. */
  consumed: boolean;
}

export class TidalField {
  private victims = new Map<string, Victim>();
  /** Bodies consumed since the last drain, for the caller to report. */
  private eaten: string[] = [];

  /**
   * Registers a mesh as something a black hole can tear apart.
   *
   * Idempotent: re-registering the same id updates the body but keeps the
   * original scale, so a mesh cannot slowly shrink through repeated adds.
   */
  add(id: string, mesh: Mesh, body: TidalBody): void {
    const existing = this.victims.get(id);
    if (existing) {
      existing.body = body;
      existing.mesh = mesh;
      return;
    }
    this.victims.set(id, {
      id,
      mesh,
      body,
      baseScale: mesh.scaling.clone(),
      state: {
        stress: 0, stretch: 1, squeeze: 1, disrupting: false,
        shredded: 0, pull: 0, consumed: false
      },
      consumed: false
    });
  }

  /** Stops tracking one body, restoring its shape. */
  remove(id: string): void {
    const v = this.victims.get(id);
    if (v) this.restore(v);
    this.victims.delete(id);
  }

  /** How many bodies are being tracked. */
  get count(): number {
    return this.victims.size;
  }

  /** The state of one body, for the HUD. */
  stateOf(id: string): TidalState | null {
    return this.victims.get(id)?.state ?? null;
  }

  /**
   * Restores every body to its original shape and forgets them.
   *
   * Called when leaving sandbox mode. Without it, a planet caught mid-stretch
   * would stay a thread forever in Explorer mode.
   */
  clear(): void {
    for (const v of this.victims.values()) this.restore(v);
    this.victims.clear();
    this.eaten = [];
  }

  /** Ids consumed since this was last called. Clears the list. */
  drainConsumed(): string[] {
    const out = this.eaten;
    this.eaten = [];
    return out;
  }

  /**
   * Applies one frame of tidal physics.
   *
   * `sources` is every black hole close enough to matter. Each body is acted
   * on by the one whose field is strongest, which for the 1/r³ tidal term is
   * effectively always the nearest — but computing it properly costs nothing
   * and means two holes near each other behave sensibly.
   */
  update(dt: number, sources: TidalSource[], enabled: boolean): void {
    if (!enabled) return;
    const step = Number.isFinite(dt) ? Math.max(0, Math.min(0.1, dt)) : 0;
    if (!sources.length) {
      // Nothing nearby: let anything mid-stretch relax back to its own shape.
      for (const v of this.victims.values()) {
        if (!v.consumed && v.state.stretch > 1.001) this.relax(v, step);
      }
      return;
    }

    for (const v of this.victims.values()) {
      if (v.consumed) continue;
      const mesh = v.mesh;
      if (!mesh || mesh.isDisposed?.()) continue;

      // strongest field wins
      let best: TidalSource | null = null;
      let bestStress = -1;
      let bestDist = 0;
      for (const s of sources) {
        const d = Vector3.Distance(mesh.position, s.position);
        const st = tidalState(v.body, d, s.horizon, true);
        if (st.stress > bestStress) {
          bestStress = st.stress;
          best = s;
          bestDist = d;
        }
      }
      if (!best) continue;

      const st = tidalState(v.body, bestDist, best.horizon, true);
      v.state = st;

      if (st.consumed) {
        // Across the horizon. It is gone; there is no rendering of what
        // happens next, because nothing gets out to be rendered.
        v.consumed = true;
        mesh.setEnabled(false);
        this.eaten.push(v.id);
        continue;
      }

      // ---- drawn inward ----
      const toHole = best.position.subtract(mesh.position);
      const dist = toHole.length();
      if (dist > 1e-6) {
        const dir = toHole.scale(1 / dist);
        mesh.position.addInPlace(dir.scale(st.pull * step));

        // ---- stretched along the radial direction ----
        // Babylon scales in local space, so the mesh is first rotated to put
        // its local Y along the radial direction; the stretch then points at
        // the hole no matter how the body was modelled or which way it flew
        // in.
        if (st.stretch > 1.001) {
          const up = dir.scale(-1);
          const ref = Math.abs(up.y) < 0.95
            ? new Vector3(0, 1, 0) : new Vector3(1, 0, 0);
          const right = Vector3.Cross(ref, up).normalize();
          const fwd = Vector3.Cross(up, right).normalize();
          if (!mesh.rotationQuaternion) {
            mesh.rotationQuaternion = Quaternion.Identity();
          }
          Quaternion.FromLookDirectionLHToRef(fwd, up, mesh.rotationQuaternion);
        }
      }

      const b = v.baseScale;
      mesh.scaling.set(
        b.x * st.squeeze,
        b.y * st.stretch,
        b.z * st.squeeze
      );
    }
  }

  /** Eases a body back toward its own shape once it is out of danger. */
  private relax(v: Victim, dt: number): void {
    const k = Math.min(1, dt * 3);
    const b = v.baseScale;
    v.mesh.scaling.set(
      v.mesh.scaling.x + (b.x - v.mesh.scaling.x) * k,
      v.mesh.scaling.y + (b.y - v.mesh.scaling.y) * k,
      v.mesh.scaling.z + (b.z - v.mesh.scaling.z) * k
    );
    v.state = {
      stress: 0, stretch: 1, squeeze: 1, disrupting: false,
      shredded: 0, pull: 0, consumed: false
    };
  }

  /** Puts one body back exactly as it was. */
  private restore(v: Victim): void {
    const m = v.mesh;
    if (!m || m.isDisposed?.()) return;
    m.scaling.copyFrom(v.baseScale);
    m.setEnabled(true);
  }

  dispose(): void {
    this.victims.clear();
    this.eaten = [];
  }
}
