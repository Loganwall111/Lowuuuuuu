/**
 * PortalSystem — player-made wormholes.
 *
 * A portal is a linked pair of mouths. Each mouth renders the sky of the
 * *other* end through the lensing shader, so you can look through and see
 * where you would arrive before committing. Flying a body (or the camera)
 * into a mouth transports it to the twin, preserving speed and re-orienting
 * the velocity so you exit travelling "out" of the far mouth rather than
 * immediately falling back in.
 *
 * Portals can also link to a procedural dimension rather than to a twin,
 * which is how a space tear differs from an ordinary wormhole.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { Effect } from '@babylonjs/core/Materials/effect';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Scene } from '@babylonjs/core/scene';
import { PORTAL_SHADER, PORTAL_VERT, PORTAL_FRAG } from '../shaders/PortalShader';
import { generateDimension, tearSideways, type DimensionSpec } from './DimensionSystem';
import { toRenderRef } from './RenderOrigin';

export type PortalKind = 'wormhole' | 'tear';

export interface PortalMouth {
  id: string;
  mesh: Mesh;
  position: Vector3;
  /** The direction you exit travelling, i.e. the mouth's facing. */
  normal: Vector3;
  radius: number;
}

export interface Portal {
  id: string;
  kind: PortalKind;
  a: PortalMouth;
  /** A tear has no twin: it opens onto a dimension instead. */
  b: PortalMouth | null;
  /** Where a tear leads. */
  destination: DimensionSpec | null;
  openness: number;
  targetOpenness: number;
  lensStrength: number;
  throatMass: number;
  age: number;
  /**
   * Transports blocked briefly after a jump, to stop instant ping-ponging.
   *
   * This is PER TRAVELLER. A single shared number meant that any asteroid
   * drifting through a rift locked out everyone else for 1.5s - including
   * the player, whose dimension jump then silently did nothing. The number
   * below is kept only for display and for the iris animation; the real
   * gate is the per-traveller map on PortalSystem.
   */
  cooldown: number;
}

export interface Traveller {
  position: Vector3;
  velocity: Vector3;
  /**
   * Stable identity for cooldown bookkeeping.
   *
   * Callers that rebuild their traveller object every frame (the player camera
   * does) MUST pass this, or the system cannot tell one frame's traveller from
   * the next. Objects that persist can leave it undefined and be keyed by
   * reference.
   */
  key?: string;
}

let shaderRegistered = false;

function registerShader(): void {
  if (shaderRegistered) return;
  Effect.ShadersStore[PORTAL_SHADER + 'VertexShader'] = PORTAL_VERT;
  Effect.ShadersStore[PORTAL_SHADER + 'FragmentShader'] = PORTAL_FRAG;
  shaderRegistered = true;
}

export class PortalSystem {
  private scene: Scene;
  private portals: Portal[] = [];
  private seq = 0;
  private time = 0;
  /** Incremented whenever anything travels; the UI reads it. */
  transits = 0;
  lastDestination: DimensionSpec | null = null;
  /**
   * Per-portal, per-traveller cooldowns, keyed weakly so travellers that are
   * destroyed do not leak. Shared state here was a real bug: debris falling
   * through a tear consumed the cooldown the player needed.
   */
  private travellerCooldown = new WeakMap<object, Map<string, number>>();
  /** Cooldowns for travellers identified by a stable string key. */
  private keyedCooldown = new Map<string, number>();

  constructor(scene: Scene) {
    this.scene = scene;
    registerShader();
  }

  /* ------------------------------- building ------------------------------- */

  private makeMouth(id: string, pos: Vector3, normal: Vector3, radius: number,
                    seed: number, tint: [Color3, Color3], rim: Color3,
                    kind: PortalKind): PortalMouth {
    const disc = MeshBuilder.CreateDisc(id, { radius, tessellation: 64 }, this.scene);
    disc.position.copyFrom(pos);

    // face the disc along its normal
    const n = normal.clone().normalize();
    const up = Math.abs(n.y) > 0.95 ? new Vector3(0, 0, 1) : new Vector3(0, 1, 0);
    const right = Vector3.Cross(up, n).normalize();
    const trueUp = Vector3.Cross(n, right).normalize();
    disc.rotation = Vector3.RotationFromAxis(right, trueUp, n);

    let mat: ShaderMaterial | StandardMaterial;
    try {
      const sm = new ShaderMaterial(id + 'Mat', this.scene, PORTAL_SHADER, {
        attributes: ['position', 'normal', 'uv'],
        uniforms: [
          'world', 'worldViewProjection', 'camPos', 'time', 'throatMass',
          'lensStrength', 'openness', 'rimColor', 'destTintA', 'destTintB',
          'destSeed', 'exposure'
        ]
      });
      sm.setColor3('rimColor', rim);
      sm.setColor3('destTintA', tint[0]);
      sm.setColor3('destTintB', tint[1]);
      sm.setFloat('destSeed', seed);
      sm.setFloat('exposure', 1.15);
      sm.backFaceCulling = false;
      mat = sm;
    } catch {
      // Never leave a black disc if the shader fails to compile.
      const fb = new StandardMaterial(id + 'Fallback', this.scene);
      fb.emissiveColor = kind === 'tear' ? new Color3(0.9, 0.3, 1) : new Color3(0.3, 0.8, 1);
      fb.disableLighting = true;
      fb.backFaceCulling = false;
      mat = fb;
    }
    disc.material = mat;
    disc.isPickable = false;

    return { id, mesh: disc, position: pos.clone(), normal: n, radius };
  }

  /** Opens a linked pair of mouths you can fly between. */
  createWormhole(from: Vector3, to: Vector3, radius = 6,
                 lensStrength = 1.4): Portal {
    const id = 'wh' + (++this.seq);
    const dir = to.subtract(from);
    const n = dir.lengthSquared() > 1e-6 ? dir.normalize() : new Vector3(0, 0, 1);
    const seed = (Math.random() * 1000) | 0;

    const warm = new Color3(0.45, 0.85, 1.0);
    const cool = new Color3(0.75, 0.5, 1.0);
    const rim = new Color3(0.5, 0.9, 1.0);

    const a = this.makeMouth(id + 'a', from, n, radius, seed, [warm, cool], rim, 'wormhole');
    const b = this.makeMouth(id + 'b', to, n.scale(-1), radius, seed + 1,
      [cool, warm], rim, 'wormhole');

    const p: Portal = {
      id, kind: 'wormhole', a, b, destination: null,
      openness: 0, targetOpenness: 1,
      lensStrength, throatMass: 1.0, age: 0, cooldown: 0
    };
    this.portals.push(p);
    return p;
  }

  /**
   * Rips a tear in reality: a single mouth opening onto a procedural
   * dimension rather than onto a twin.
   */
  createTear(at: Vector3, facing: Vector3, radius = 7,
             from?: DimensionSpec | null): Portal {
    const id = 'tr' + (++this.seq);
    const dest = from
      ? tearSideways(from)
      : generateDimension((Math.random() * 0xffffffff) >>> 0, 0);

    const pal = dest.palette;
    const tintA = new Color3(pal[0][0], pal[0][1], pal[0][2]);
    const tintB = new Color3(pal[1][0], pal[1][1], pal[1][2]);
    const rimSrc = pal[2] ?? pal[0];
    const rim = new Color3(
      Math.min(1, rimSrc[0] + 0.3), Math.min(1, rimSrc[1] + 0.3), Math.min(1, rimSrc[2] + 0.3));

    const a = this.makeMouth(id + 'a', at, facing, radius, dest.seed % 997,
      [tintA, tintB], rim, 'tear');

    const p: Portal = {
      id, kind: 'tear', a, b: null, destination: dest,
      openness: 0, targetOpenness: 1,
      // tears lens more violently than engineered wormholes
      lensStrength: 1.8 + dest.weirdness * 1.2,
      throatMass: 1.4, age: 0, cooldown: 0
    };
    this.portals.push(p);
    return p;
  }

  /* -------------------------------- update -------------------------------- */

  update(dt: number, camPos: Vector3): void {
    if (!Number.isFinite(dt) || dt <= 0) return;
    this.time += dt;

    for (const p of this.portals) {
      p.age += dt;
      if (p.cooldown > 0) p.cooldown = Math.max(0, p.cooldown - dt);

      // portals iris open rather than popping into existence
      p.openness += (p.targetOpenness - p.openness) * Math.min(1, dt * 2.4);

      for (const m of [p.a, p.b]) {
        if (!m) continue;
        const mat = m.mesh.material as ShaderMaterial;
        if (mat && typeof (mat as any).setFloat === 'function' && (mat as any).setVector3) {
          try {
            mat.setVector3('camPos', camPos);
            mat.setFloat('time', this.time);
            mat.setFloat('throatMass', p.throatMass);
            mat.setFloat('lensStrength', p.lensStrength);
            mat.setFloat('openness', Math.max(0.02, p.openness));
          } catch {
            // a uniform failure must never kill the frame
          }
        }
        // the disc always faces the viewer enough to be visible
        toRenderRef(m.position, m.mesh.position);
      }
    }
  }

  /**
   * Tests a traveller against every portal and transports it if it entered.
   * Returns the portal used, or null.
   *
   * Entry requires actually crossing the disc, not merely being near it:
   * the traveller must be within the mouth's radius AND moving toward it.
   */
  tryTransit(t: Traveller, dtRadius = 1.5): Portal | null {
    const now = this.time;
    for (const p of this.portals) {
      // An unopened portal is not enterable by anyone.
      if (p.openness < 0.5) continue;
      // Only THIS traveller's recent use of THIS portal blocks it.
      if (this.cooldownUntil(t, p) > now) continue;

      for (const m of [p.a, p.b]) {
        if (!m) continue;
        const toMouth = t.position.subtract(m.position);
        const dist = toMouth.length();
        if (dist > m.radius + dtRadius) continue;

        // must be heading into the mouth, not drifting past it
        const closing = Vector3.Dot(t.velocity, m.position.subtract(t.position));
        if (closing <= 0 && dist > m.radius * 0.5) continue;

        const other = m === p.a ? p.b : p.a;

        if (p.kind === 'tear' || !other) {
          // a tear does not move you within this scene; the caller handles
          // the dimension change. Report it and stop.
          this.lastDestination = p.destination;
          this.transits++;
          this.markUsed(t, p);
          return p;
        }

        // ---- transport to the twin ----
        const speed = t.velocity.length();
        // exit travelling out of the far mouth, preserving speed
        const exitDir = other.normal.scale(-1).normalize();
        t.position.copyFrom(other.position.add(exitDir.scale(other.radius * 0.9 + 2)));
        t.velocity.copyFrom(exitDir.scale(Math.max(speed, 6)));

        this.transits++;
        this.markUsed(t, p);
        return p;
      }
    }
    return null;
  }

  /**
   * Records that one traveller just used one portal, so it cannot immediately
   * bounce back through. Also drives the visible cooldown readout.
   */
  private markUsed(t: Traveller, p: Portal): void {
    if (t.key) {
      this.keyedCooldown.set(t.key + '|' + p.id, this.time + 1.5);
    } else {
      let mine = this.travellerCooldown.get(t as object);
      if (!mine) { mine = new Map(); this.travellerCooldown.set(t as object, mine); }
      mine.set(p.id, this.time + 1.5);
    }
    p.cooldown = 1.5;
  }

  /** When this traveller may next use this portal. */
  private cooldownUntil(t: Traveller, p: Portal): number {
    if (t.key) return this.keyedCooldown.get(t.key + '|' + p.id) ?? 0;
    const mine = this.travellerCooldown.get(t as object);
    return mine ? (mine.get(p.id) ?? 0) : 0;
  }

  /** Closes and disposes one portal. Every portal must be closable. */
  close(id: string): boolean {
    const i = this.portals.findIndex((p) => p.id === id);
    if (i < 0) return false;
    const p = this.portals[i];
    for (const m of [p.a, p.b]) {
      if (!m) continue;
      m.mesh.material?.dispose();
      m.mesh.dispose();
    }
    this.portals.splice(i, 1);
    return true;
  }

  closeAll(): void {
    for (const p of [...this.portals]) this.close(p.id);
    this.portals = [];
  }

  list(): Portal[] {
    return this.portals;
  }

  count(): number {
    return this.portals.length;
  }

  stats(): Record<string, string> {
    const tears = this.portals.filter((p) => p.kind === 'tear').length;
    return {
      'Portals open': String(this.portals.length),
      'Wormholes': String(this.portals.length - tears),
      'Space tears': String(tears),
      'Transits': String(this.transits),
      'Last destination': this.lastDestination
        ? this.lastDestination.glyph + ' ' + this.lastDestination.name
        : '—'
    };
  }

  dispose(): void {
    this.closeAll();
  }
}
