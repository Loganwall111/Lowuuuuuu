/**
 * Shared double-precision simulation origin / camera-local render transform.
 * Simulation coordinates never change; only GPU-facing positions are offset.
 */
import { Vector3 } from '@babylonjs/core/Maths/math.vector';

const ORIGIN = new Vector3();
const SCRATCH = new Vector3();
export const FLOATING_ORIGIN_TRIGGER = 250000;
export const FLOATING_ORIGIN_GRID = 100000;

export function renderOrigin(): Vector3 { return ORIGIN; }
export function toRenderRef(world: Vector3, out: Vector3): Vector3 {
  out.set(world.x - ORIGIN.x, world.y - ORIGIN.y, world.z - ORIGIN.z);
  return out;
}
export function toRender(world: Vector3): Vector3 {
  return new Vector3(world.x - ORIGIN.x, world.y - ORIGIN.y, world.z - ORIGIN.z);
}
export function toWorldRef(local: Vector3, out: Vector3): Vector3 {
  out.set(local.x + ORIGIN.x, local.y + ORIGIN.y, local.z + ORIGIN.z);
  return out;
}
/** Returns the origin delta that all existing root meshes must subtract. */
export function followRenderOrigin(eye: Vector3): Vector3 | null {
  const lx=eye.x-ORIGIN.x,ly=eye.y-ORIGIN.y,lz=eye.z-ORIGIN.z;
  if(Math.max(Math.abs(lx),Math.abs(ly),Math.abs(lz))<FLOATING_ORIGIN_TRIGGER)return null;
  const nx=Math.round(eye.x/FLOATING_ORIGIN_GRID)*FLOATING_ORIGIN_GRID;
  const ny=Math.round(eye.y/FLOATING_ORIGIN_GRID)*FLOATING_ORIGIN_GRID;
  const nz=Math.round(eye.z/FLOATING_ORIGIN_GRID)*FLOATING_ORIGIN_GRID;
  SCRATCH.set(nx-ORIGIN.x,ny-ORIGIN.y,nz-ORIGIN.z);
  if(SCRATCH.lengthSquared()<1)return null;
  ORIGIN.set(nx,ny,nz);
  return SCRATCH.clone();
}
export function resetRenderOrigin(): void { ORIGIN.setAll(0); }
