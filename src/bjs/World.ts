/**
 * World — the contract every simulation world implements.
 */

import type { Scene } from '@babylonjs/core/scene';
import type { ArcRotateCamera } from '@babylonjs/core/Cameras/arcRotateCamera';
import type { Vector3 } from '@babylonjs/core/Maths/math.vector';

export interface WorldParam {
  key: string;
  label: string;
  min: number;
  max: number;
  step: number;
  value: number;
  unit?: string;
}

export interface WorldAction {
  key: string;
  label: string;
  glyph?: string;
}

export interface WorldContext {
  scene: Scene;
  camera: ArcRotateCamera;
  setCameraTarget(target: Vector3, radius: number): void;
  /**
   * The place the player just travelled to, if any.
   *
   * Worlds used to assume they were always built at the origin. That is fine
   * when a world IS the universe, but these worlds are views onto one region
   * of a much bigger universe: flying to a hole at (-4.7, 0, -2722) and then
   * rendering it at (0, 0, 0) left the player ~837 units from their target,
   * staring into empty space with nothing on screen. Nothing threw, so it
   * read as a frozen screen rather than an error.
   */
  focus?: {
    /** World-space centre of the region that was travelled to. */
    position: Vector3;
    /** Region radius, for framing the camera sensibly. */
    radius: number;
    /**
     * Region mass, where the region has one.
     *
     * Without this a world renders whatever mass it was written with. The
     * black hole world defaulted to 1.0 while real regions carry 4,000-44,000,
     * so the hole it drew was ~2 px across on arrival and all the player saw
     * was its bloom: a small white blob.
     */
    mass?: number;
  } | null;
  /**
   * Travel to a procedural dimension. A world calls this when the player
   * enters a space tear or falls through a black hole, so the destination
   * seen through the portal is the one they arrive in. Optional so worlds
   * can be built and tested without an app around them.
   */
  enterDimension?(seed: number, depth: number): void;
}

export interface World {
  id: string;
  name: string;
  /**
   * True when the world renders its own black hole and must be the only
   * source of one.
   *
   * BlackHoleWorld raymarches the hole, its accretion disk and its lensing
   * together from a single position in one shader. The global geometry hole
   * field also builds holes from the universe's region list, and in that
   * world the two are different objects at different positions - which is
   * why a bare black sphere appeared on one side of the screen while the
   * lensed disk sat on the other. When this is set, App suppresses the
   * geometry field so the world's hole is the only one.
   */
  ownsBlackHole?: boolean;
  build(ctx: WorldContext): Promise<void>;
  update(dt: number, ctx: WorldContext): void;
  getParams(): WorldParam[];
  setParam(key: string, value: number): void;
  getStats(): Record<string, string>;
  getActions?(): WorldAction[];
  runAction?(key: string, ctx: WorldContext): void;
  dispose(): void;
}
