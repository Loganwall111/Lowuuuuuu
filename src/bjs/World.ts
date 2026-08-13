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
