/**
 * SculptSystem — the planet-painter brush.
 *
 * The terrain and hydrology already know how to deform, crater and flood;
 * this is the thin, testable layer on top that maps a *tool* onto those
 * primitives, so "raise a mountain here" is one call the app can bind to a
 * key, and one call the tests can pin down. Nothing here touches Babylon:
 * it operates on the same grid interface the hydraulic solver exposes, so
 * sculpting the planet you are standing on and simulating its water are the
 * same object.
 */

export type SculptTool = 'raise' | 'lower' | 'dig' | 'melt' | 'crater' | 'flood';

export interface HydroLike {
  deform(cx: number, cy: number, radius: number, amount: number, smooth?: boolean): void;
  crater(cx: number, cy: number, radius: number, depth: number): void;
  addWater(cx: number, cy: number, radius: number, amount: number): void;
}

export interface SculptToolSpec {
  id: SculptTool;
  label: string;
  glyph: string;
  blurb: string;
}

export const SCULPT_TOOLS: SculptToolSpec[] = [
  { id: 'raise', label: 'Raise Terrain', glyph: '⛰', blurb: 'Lift mountains.' },
  { id: 'lower', label: 'Lower Terrain', glyph: '🕳', blurb: 'Sink the ground.' },
  { id: 'dig', label: 'Dig Ocean', glyph: '🌊', blurb: 'Carve a sea.' },
  { id: 'melt', label: 'Melt Ice', glyph: '💧', blurb: 'Flood with meltwater.' },
  { id: 'crater', label: 'Bomb', glyph: '☄', blurb: 'Impact crater.' },
  { id: 'flood', label: 'Flood', glyph: '🌧', blurb: 'Rain into a basin.' }
];

export function sculptTool(id: string): SculptToolSpec {
  return SCULPT_TOOLS.find((t) => t.id === id) ?? SCULPT_TOOLS[0];
}

/**
 * Applies a sculpt stroke to a hydrology grid.
 *
 * `melt` raises water (the ice becomes sea) rather than terrain, which is
 * what "melting an ice cap" means at the heightfield level.
 */
export function applySculpt(
  hydro: HydroLike, tool: SculptTool,
  cx: number, cy: number, radius: number, strength: number
): void {
  const r = Math.max(1, radius);
  const s = Number.isFinite(strength) ? strength : 1;
  switch (tool) {
    case 'raise':
      hydro.deform(cx, cy, r, s * 0.8, true);
      break;
    case 'lower':
      hydro.deform(cx, cy, r, -s * 0.8, true);
      break;
    case 'dig':
      // A broad bowl, then the water table drains into it next step.
      hydro.deform(cx, cy, r, -s * 1.4, true);
      hydro.crater(cx, cy, r, s * 0.5);
      break;
    case 'melt':
      hydro.deform(cx, cy, r, -s * 0.3, true);
      hydro.addWater(cx, cy, r, s * 0.5);
      break;
    case 'crater':
      hydro.crater(cx, cy, r, s * 1.2);
      break;
    case 'flood':
      hydro.addWater(cx, cy, r, s * 0.6);
      break;
  }
}

/**
 * Maps a surface direction to the grid coordinates of the heightfield.
 *
 * The terrain grid is a flat 2D heightfield, but the walker stands on a
 * sphere. The equirectangular mapping is the bridge: the outward surface
 * normal at the player's feet is the radial direction, which maps directly
 * onto (u, v) and therefore onto a grid cell. This is the same mapping the
 * planet shader uses to place its albedo texture, so a stroke lands where
 * the player is looking.
 */
export function surfaceToGrid(
  nx: number, ny: number, nz: number, gridSize: number
): { x: number; y: number } {
  const n = Math.max(2, gridSize);
  const u = Math.atan2(nz, nx) / (2 * Math.PI) + 0.5;
  const v = Math.acos(Math.max(-1, Math.min(1, ny))) / Math.PI;
  return {
    x: Math.round(((u % 1) + 1) % 1 * (n - 1)),
    y: Math.round(Math.max(0, Math.min(1, v)) * (n - 1))
  };
}
