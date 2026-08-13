/**
 * ConstructionSystem — building on planets, and cutting them open.
 *
 * Two halves of the same idea: the terrain is *material*, so you can add to
 * it and take it away. Both operate on the same per-planet height grid the
 * hydraulic solver uses, which means anything you build immediately affects
 * water flow, and anything you carve immediately fills or drains.
 *
 * That coupling is the point. A wall you raise dams a river. A trench a
 * laser cuts becomes a canyon the water finds on its own.
 */

import type { HydraulicSystem } from './HydraulicSystem';

export type StructureKind = 'wall' | 'tower' | 'dome' | 'platform' | 'pillar' | 'ramp';

export interface Structure {
  id: number;
  kind: StructureKind;
  /** Grid coordinates. */
  x: number;
  y: number;
  radius: number;
  height: number;
  /** Terrain heights before this was placed, for undo. */
  restore: Float32Array;
  /** Cell indices the restore array corresponds to. */
  cells: Int32Array;
}

export interface StructureSpec {
  kind: StructureKind;
  label: string;
  glyph: string;
  radius: number;
  height: number;
  blurb: string;
}

export const STRUCTURES: Record<StructureKind, StructureSpec> = {
  wall: {
    kind: 'wall', label: 'Wall', glyph: '🧱', radius: 3, height: 9,
    blurb: 'A straight barrier. Dams rivers and redirects floods.'
  },
  tower: {
    kind: 'tower', label: 'Tower', glyph: '🗼', radius: 2.5, height: 26,
    blurb: 'Tall and narrow. Visible from orbit if you build enough.'
  },
  dome: {
    kind: 'dome', label: 'Dome', glyph: '⛺', radius: 8, height: 12,
    blurb: 'A smooth shell. Sheds water to its rim.'
  },
  platform: {
    kind: 'platform', label: 'Platform', glyph: '⬜', radius: 9, height: 6,
    blurb: 'A flat mesa. Stands above the flood line.'
  },
  pillar: {
    kind: 'pillar', label: 'Pillar', glyph: '🪨', radius: 1.6, height: 34,
    blurb: 'A needle of rock. Mostly for the view.'
  },
  ramp: {
    kind: 'ramp', label: 'Ramp', glyph: '📐', radius: 7, height: 14,
    blurb: 'A slope. Water runs down it exactly as you would expect.'
  }
};

export const STRUCTURE_ORDER: StructureKind[] = [
  'wall', 'tower', 'dome', 'platform', 'pillar', 'ramp'
];

export class ConstructionSystem {
  private hydro: HydraulicSystem;
  private structures: Structure[] = [];
  private nextId = 1;
  /** Total material added and removed, for telemetry. */
  private built = 0;
  private carved = 0;

  constructor(hydro: HydraulicSystem) {
    this.hydro = hydro;
  }

  get count(): number { return this.structures.length; }
  get list(): readonly Structure[] { return this.structures; }

  /** Swaps the terrain this system operates on (e.g. on arriving at a planet). */
  retarget(hydro: HydraulicSystem): void {
    this.hydro = hydro;
    this.structures = [];
  }

  /**
   * Places a structure, raising the terrain into its shape.
   * Returns null if the position is off the grid.
   */
  build(kind: StructureKind, x: number, y: number, scale = 1): Structure | null {
    const spec = STRUCTURES[kind];
    if (!spec) return null;
    const n = this.hydro.size;
    if (!(x >= 0 && y >= 0 && x <= n - 1 && y <= n - 1)) return null;

    const radius = Math.max(0.8, spec.radius * scale);
    const height = spec.height * scale;

    const x0 = Math.max(0, Math.floor(x - radius - 1));
    const x1 = Math.min(n - 1, Math.ceil(x + radius + 1));
    const y0 = Math.max(0, Math.floor(y - radius - 1));
    const y1 = Math.min(n - 1, Math.ceil(y + radius + 1));

    const cells: number[] = [];
    const restore: number[] = [];

    for (let gy = y0; gy <= y1; gy++) {
      for (let gx = x0; gx <= x1; gx++) {
        const dx = gx - x;
        const dy = gy - y;
        const d = Math.hypot(dx, dy);
        const profile = this.profile(kind, dx, dy, d, radius);
        if (profile <= 0) continue;

        const i = gy * n + gx;
        cells.push(i);
        restore.push(this.hydro.terrain[i]);
        const add = height * profile;
        this.hydro.terrain[i] += add;
        this.built += add;
      }
    }

    if (!cells.length) return null;

    const st: Structure = {
      id: this.nextId++,
      kind, x, y, radius, height,
      restore: Float32Array.from(restore),
      cells: Int32Array.from(cells)
    };
    this.structures.push(st);
    return st;
  }

  /** Shape function per structure kind, returns 0..1. */
  private profile(kind: StructureKind, dx: number, dy: number, d: number, r: number): number {
    switch (kind) {
      case 'wall':
        // A bar rather than a disc: long in x, thin in y.
        if (Math.abs(dy) > r || Math.abs(dx) > r * 4) return 0;
        return 1 - Math.abs(dy) / r * 0.35;
      case 'tower':
      case 'pillar':
        return d > r ? 0 : 1;
      case 'dome': {
        if (d > r) return 0;
        // Hemisphere, so water sheds off it.
        const t = d / r;
        return Math.sqrt(Math.max(0, 1 - t * t));
      }
      case 'platform':
        // Flat top with a short skirt so it is not a floating slab.
        if (d > r) return 0;
        return d < r * 0.82 ? 1 : (r - d) / (r * 0.18);
      case 'ramp': {
        if (d > r) return 0;
        // Linear rise across x, so it is walkable.
        return Math.max(0, Math.min(1, (dx + r) / (2 * r)));
      }
      default:
        return d > r ? 0 : 1;
    }
  }

  /** Removes a structure and restores the terrain underneath it. */
  remove(id: number): boolean {
    const idx = this.structures.findIndex((s) => s.id === id);
    if (idx < 0) return false;
    const st = this.structures[idx];
    for (let k = 0; k < st.cells.length; k++) {
      this.hydro.terrain[st.cells[k]] = st.restore[k];
    }
    this.structures.splice(idx, 1);
    return true;
  }

  /** Removes the most recent structure. */
  undo(): boolean {
    const last = this.structures[this.structures.length - 1];
    return last ? this.remove(last.id) : false;
  }

  removeAll(): void {
    // Newest first, so overlapping builds restore in the right order.
    for (let i = this.structures.length - 1; i >= 0; i--) {
      const st = this.structures[i];
      for (let k = 0; k < st.cells.length; k++) {
        this.hydro.terrain[st.cells[k]] = st.restore[k];
      }
    }
    this.structures = [];
  }

  /**
   * Cuts terrain away along a line: a laser fired across the surface.
   *
   * Returns how much material was removed. Because this edits the same grid
   * the water solver reads, the cut immediately floods or drains.
   */
  carveLine(
    x0: number, y0: number, x1: number, y1: number,
    width = 2.5, depth = 8, melt = true
  ): number {
    const n = this.hydro.size;
    const dx = x1 - x0;
    const dy = y1 - y0;
    const len = Math.hypot(dx, dy);
    if (!Number.isFinite(len)) return 0;

    const steps = Math.max(1, Math.ceil(len));
    let removed = 0;

    for (let s = 0; s <= steps; s++) {
      const t = s / steps;
      const cx = x0 + dx * t;
      const cy = y0 + dy * t;
      removed += this.carveAt(cx, cy, width, depth, melt);
    }
    this.carved += removed;
    return removed;
  }

  /** Cuts a crater at a point. Returns material removed. */
  carveAt(cx: number, cy: number, radius: number, depth: number, melt = true): number {
    const n = this.hydro.size;
    const x0 = Math.max(0, Math.floor(cx - radius));
    const x1 = Math.min(n - 1, Math.ceil(cx + radius));
    const y0 = Math.max(0, Math.floor(cy - radius));
    const y1 = Math.min(n - 1, Math.ceil(cy + radius));
    let removed = 0;

    for (let gy = y0; gy <= y1; gy++) {
      for (let gx = x0; gx <= x1; gx++) {
        const d = Math.hypot(gx - cx, gy - cy);
        if (d > radius) continue;
        const i = gy * n + gx;
        // Smooth falloff so the cut has walls, not a cliff.
        const f = Math.cos((d / radius) * Math.PI * 0.5);
        const cut = depth * f * f;
        const before = this.hydro.terrain[i];
        // Never punch below the floor of the world.
        const after = Math.max(0, before - cut);
        this.hydro.terrain[i] = after;
        removed += before - after;

        // A laser leaves a melted lip: some material refreezes at the rim.
        if (melt && d > radius * 0.75) {
          this.hydro.terrain[i] += (before - after) * 0.18;
        }
      }
    }
    return removed;
  }

  /** Total material added and removed, for the telemetry panel. */
  stats(): Record<string, string> {
    return {
      'Structures': String(this.structures.length),
      'Material built': this.built.toFixed(0),
      'Material carved': this.carved.toFixed(0)
    };
  }

  dispose(): void {
    this.structures = [];
  }
}
