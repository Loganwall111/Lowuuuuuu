/**
 * PlanetSurfaceSystem — every planet gets its own working surface.
 *
 * The old shape of this app was "physics engines on the sides": a Terraform
 * mode with water, an Ocean mode with waves, a Sandbox with gravity. That
 * makes the simulation a set of exhibits rather than a universe.
 *
 * Here, each planet owns a persistent surface record: its own terrain, its
 * own hydraulic solver (so rain, rivers, drainage and erosion are that
 * planet's, not a global mode), its own weather including tornadoes that
 * stir the water, and its own creatures.
 *
 * Surfaces are created lazily on approach and can be evicted when you leave,
 * because a universe of 10,000 planets cannot all be simulated at once. The
 * seed makes eviction safe: re-entering rebuilds the same world.
 */

import { HydraulicSystem } from './HydraulicSystem';
import { speciesFor, type Species } from './LifeSystem';
import {
  applySculpt, surfaceToGrid, type SculptTool
} from './SculptSystem';
import {
  tidalLocked, subStellarPoint, subsurfaceOcean, weatherFor
} from './WorldDynamics';

/** How detailed a planet's surface grid is. */
export const SURFACE_GRID = 96;

export type Climate = 'ocean' | 'temperate' | 'arid' | 'frozen' | 'volcanic' | 'exotic';

export interface Tornado {
  /** Grid coordinates. */
  x: number;
  y: number;
  /** Radius in cells. */
  radius: number;
  strength: number;
  /** Seconds left before it dissipates. */
  life: number;
  vx: number;
  vy: number;
}

export interface SurfaceProfile {
  climate: Climate;
  /** Fraction of the surface under water at rest, 0..1. */
  seaLevel: number;
  /** Rainfall rate driving the hydraulic solver. */
  rainfall: number;
  /** How readily terrain erodes. */
  erosion: number;
  /** Storms per minute. */
  storminess: number;
  /** Surface gravity, scales flow speed. */
  gravity: number;
  /** Deepest ocean depth in world units. */
  oceanDepth: number;
  species: Species[];
  /** A frozen world can hide a liquid ocean beneath its ice. */
  subsurfaceOcean: boolean;
  /** How deep that hidden ocean sits, world units. */
  subsurfaceDepth: number;
  /** Close-in worlds keep one face toward their star. */
  tidalLocked: boolean;
  /** The (u, v) of the sub-stellar point on a locked world. */
  subStellar: [number, number];
}

export interface PlanetSurface {
  id: string;
  seed: number;
  profile: SurfaceProfile;
  hydro: HydraulicSystem;
  tornadoes: Tornado[];
  /** Seconds of simulation this surface has accumulated. */
  age: number;
  /** Set while the player is close enough to warrant full simulation. */
  active: boolean;
}

function mulberry32(seed: number): () => number {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

const CLIMATES: Climate[] = ['ocean', 'temperate', 'arid', 'frozen', 'volcanic'];

/** Derives a planet's character from its seed - stable across visits. */
export function profileFor(seed: number, exotic = false): SurfaceProfile {
  const rng = mulberry32(seed);
  const climate: Climate = exotic ? 'exotic' : CLIMATES[Math.floor(rng() * CLIMATES.length)];

  // Each climate implies a different hydrology, which is the whole point:
  // an arid world drains almost instantly, an ocean world barely at all.
  const base = {
    ocean: { sea: 0.72, rain: 1.5, ero: 0.30, depth: 42 },
    temperate: { sea: 0.42, rain: 1.1, ero: 0.40, depth: 22 },
    arid: { sea: 0.08, rain: 0.15, ero: 0.55, depth: 5 },
    frozen: { sea: 0.30, rain: 0.35, ero: 0.12, depth: 14 },
    volcanic: { sea: 0.14, rain: 0.45, ero: 0.70, depth: 9 },
    exotic: { sea: 0.35, rain: 1.0, ero: 0.35, depth: 18 }
  }[climate];

  // The world's deeper character: subsurface oceans, tidal locking, weather.
  const sub = subsurfaceOcean(seed, climate);
  const locked = climate !== 'exotic' && tidalLocked(seed, 12 + rng() * 70);

  return {
    climate,
    seaLevel: Math.max(0, Math.min(1, base.sea + (rng() - 0.5) * 0.16)),
    rainfall: Math.max(0, base.rain * (0.6 + rng() * 0.9)),
    erosion: Math.max(0, base.ero * (0.7 + rng() * 0.7)),
    storminess: rng() * (climate === 'arid' ? 0.4 : 1.6),
    gravity: 0.4 + rng() * 1.9,
    oceanDepth: base.depth * (0.7 + rng() * 0.8),
    // Life native to this climate: jellyfish on ocean worlds, centipedes in
    // volcanic trenches, rather than the same creatures everywhere.
    species: speciesFor(
      seed ^ 0x5f3759df,
      climate === 'frozen' ? 2 : 2 + Math.floor(rng() * 5),
      climate
    ),
    subsurfaceOcean: sub.present,
    subsurfaceDepth: sub.depth,
    tidalLocked: locked,
    subStellar: subStellarPoint(seed)
  };
}

export class PlanetSurfaceSystem {
  private surfaces = new Map<string, PlanetSurface>();
  /** Most surfaces kept resident at once. */
  maxResident = 6;
  /** Total simulated seconds across every surface, for telemetry. */
  private totalAge = 0;

  get count(): number { return this.surfaces.size; }
  get ids(): string[] { return [...this.surfaces.keys()]; }

  /** Returns the surface for a planet, building it on first approach. */
  acquire(id: string, seed: number, exotic = false): PlanetSurface {
    const hit = this.surfaces.get(id);
    if (hit) return hit;

    const profile = profileFor(seed, exotic);
    const hydro = new HydraulicSystem({
      size: SURFACE_GRID,
      // Gravity genuinely changes how the water behaves on each world.
      gravity: 9.81 * profile.gravity,
      rain: profile.rainfall * 0.02,
      erosion: profile.erosion,
      deposition: profile.erosion * 0.7
    });

    this.generateTerrain(hydro, seed, profile);

    const surface: PlanetSurface = {
      id, seed, profile, hydro, tornadoes: [], age: 0, active: false
    };
    this.surfaces.set(id, surface);
    this.evictIfNeeded(id);
    return surface;
  }

  /** Whether a planet's surface is currently resident in memory. */
  has(id: string): boolean { return this.surfaces.has(id); }
  get(id: string): PlanetSurface | null { return this.surfaces.get(id) ?? null; }

  /** Fills the terrain grid with mountains, basins and an ocean. */
  private generateTerrain(hydro: HydraulicSystem, seed: number, profile: SurfaceProfile): void {
    const n = hydro.size;
    const rng = mulberry32(seed ^ 0x9e3779b9);

    // A few octaves of value noise: cheap, deterministic, no dependencies.
    const oct = 5;
    const amp: number[] = [];
    const freq: number[] = [];
    const off: number[] = [];
    for (let o = 0; o < oct; o++) {
      amp.push(Math.pow(0.5, o));
      freq.push(Math.pow(2, o) * (1.5 + rng()));
      off.push(rng() * 100);
    }

    const noise2 = (x: number, y: number, o: number): number => {
      // Smooth periodic noise so the surface wraps without a seam.
      const f = freq[o];
      return Math.sin((x * f + off[o]) * 6.283) * Math.cos((y * f + off[o] * 1.3) * 6.283);
    };

    let lo = Infinity, hi = -Infinity;
    const raw = new Float32Array(n * n);
    for (let y = 0; y < n; y++) {
      for (let x = 0; x < n; x++) {
        const u = x / n, v = y / n;
        let h = 0;
        for (let o = 0; o < oct; o++) h += noise2(u, v, o) * amp[o];
        // Ridged component gives mountain chains rather than rolling blobs.
        h += (1 - Math.abs(noise2(u, v, 1))) * 0.55;
        raw[y * n + x] = h;
        if (h < lo) lo = h;
        if (h > hi) hi = h;
      }
    }

    const span = hi - lo || 1;
    const relief = profile.climate === 'ocean' ? 0.7
      : profile.climate === 'volcanic' ? 1.35 : 1.0;
    for (let i = 0; i < raw.length; i++) {
      hydro.terrain[i] = ((raw[i] - lo) / span) * 26 * relief;
    }

    // Fill everything below sea level: this planet's actual ocean.
    hydro.setSeaLevel(profile.seaLevel);
  }

  /**
   * Sculpts the surface of a planet.
   *
   * The `normal` is the outward surface direction at the player's feet; it
   * maps through the equirectangular projection to the heightfield cell
   * where the stroke lands. The hydraulic solver then erodes and drains it
   * on the next step, so the world responds to the stroke like a world, not
   * like a paint program.
   */
  sculpt(
    id: string, tool: SculptTool,
    nx: number, ny: number, nz: number,
    radius: number, strength: number
  ): boolean {
    const s = this.surfaces.get(id);
    if (!s) return false;
    const g = surfaceToGrid(nx, ny, nz, s.hydro.size);
    applySculpt(s.hydro, tool, g.x, g.y, radius, strength);
    return true;
  }

  /** The current weather on a planet at time t, for fog and telemetry. */
  weather(id: string, t: number): { kind: string; visibility: number } {
    const s = this.surfaces.get(id);
    if (!s) return { kind: 'clear', visibility: 1 };
    const w = weatherFor(s.seed, s.profile.climate, t);
    return { kind: w.kind, visibility: w.visibility };
  }

  /** Spawns a tornado on a planet. Returns it so callers can track it. */
  spawnTornado(id: string, x?: number, y?: number): Tornado | null {
    const s = this.surfaces.get(id);
    if (!s) return null;
    const n = s.hydro.size;
    const t: Tornado = {
      x: x ?? Math.random() * n,
      y: y ?? Math.random() * n,
      radius: 4 + Math.random() * 10,
      strength: 0.6 + Math.random() * 2.0,
      life: 12 + Math.random() * 40,
      vx: (Math.random() - 0.5) * 6,
      vy: (Math.random() - 0.5) * 6
    };
    s.tornadoes.push(t);
    return t;
  }

  /**
   * Advances one planet's surface.
   *
   * Water, weather and erosion are all this planet's own: two worlds
   * simulated side by side drain and storm differently.
   */
  step(id: string, dt: number): void {
    const s = this.surfaces.get(id);
    if (!s) return;
    const step = Math.max(0, Math.min(dt, 0.05));
    s.age += step;
    this.totalAge += step;

    // Weather: storms arrive on their own schedule per planet.
    if (s.profile.storminess > 0 &&
        Math.random() < s.profile.storminess * step * 0.05 &&
        s.tornadoes.length < 6) {
      this.spawnTornado(id);
    }

    const n = s.hydro.size;
    for (let i = s.tornadoes.length - 1; i >= 0; i--) {
      const t = s.tornadoes[i];
      t.life -= step;
      if (t.life <= 0) { s.tornadoes.splice(i, 1); continue; }

      // Wander, and bounce off the edges rather than escaping the grid.
      t.x += t.vx * step;
      t.y += t.vy * step;
      if (t.x < 0) { t.x = 0; t.vx = Math.abs(t.vx); }
      if (t.y < 0) { t.y = 0; t.vy = Math.abs(t.vy); }
      if (t.x > n - 1) { t.x = n - 1; t.vx = -Math.abs(t.vx); }
      if (t.y > n - 1) { t.y = n - 1; t.vy = -Math.abs(t.vy); }

      this.stirWater(s, t, step);
    }

    // The planet's own hydrology.
    s.hydro.step(step);
  }

  /**
   * A tornado lifts water into a rotating column and drops it around the
   * rim, so you can watch it carve and flood as it crosses the map.
   */
  private stirWater(s: PlanetSurface, t: Tornado, dt: number): void {
    const n = s.hydro.size;
    const r = Math.max(1, t.radius);
    const x0 = Math.max(0, Math.floor(t.x - r));
    const x1 = Math.min(n - 1, Math.ceil(t.x + r));
    const y0 = Math.max(0, Math.floor(t.y - r));
    const y1 = Math.min(n - 1, Math.ceil(t.y + r));

    let lifted = 0;
    const pull = Math.min(0.9, t.strength * dt * 1.4);

    for (let y = y0; y <= y1; y++) {
      for (let x = x0; x <= x1; x++) {
        const dx = x - t.x, dy = y - t.y;
        const d = Math.hypot(dx, dy);
        if (d > r) continue;
        const i = y * n + x;
        // Draw water toward the eye.
        const take = s.hydro.water[i] * pull * (1 - d / r);
        if (take > 0) {
          s.hydro.water[i] -= take;
          lifted += take;
        }
        // Spin the velocity field so the surface shows the rotation.
        if (d > 0.001) {
          const swirl = t.strength * (1 - d / r) * 6;
          s.hydro.velX[i] += (-dy / d) * swirl * dt;
          s.hydro.velY[i] += (dx / d) * swirl * dt;
        }
      }
    }

    // Everything lifted comes back down around the rim: mass is conserved,
    // which is what makes a whirlpool feel physical rather than decorative.
    if (lifted > 0) {
      let ring = 0;
      const cells: number[] = [];
      for (let y = y0; y <= y1; y++) {
        for (let x = x0; x <= x1; x++) {
          const d = Math.hypot(x - t.x, y - t.y);
          if (d <= r && d > r * 0.62) { cells.push(y * n + x); ring++; }
        }
      }
      if (ring > 0) {
        const each = lifted / ring;
        for (const i of cells) s.hydro.water[i] += each;
      } else {
        // Degenerate tornado (radius under a cell): give it straight back.
        const i = Math.min(n * n - 1,
          Math.max(0, Math.round(t.y) * n + Math.round(t.x)));
        s.hydro.water[i] += lifted;
      }
    }
  }

  /** Marks which planet the player is at; others idle. */
  setActive(id: string | null): void {
    for (const [key, s] of this.surfaces) s.active = key === id;
  }

  /** Drops the least relevant surface once over budget. */
  private evictIfNeeded(keep: string): void {
    while (this.surfaces.size > this.maxResident) {
      let victim: string | null = null;
      for (const [id, s] of this.surfaces) {
        if (id === keep || s.active) continue;
        victim = id;
        break;
      }
      if (!victim) break;      // everything is in use; keep them all
      this.surfaces.delete(victim);
    }
  }

  /** Forgets a planet's surface. Re-entering regenerates it from the seed. */
  release(id: string): boolean { return this.surfaces.delete(id); }

  stats(): Record<string, string> {
    let storms = 0;
    let water = 0;
    for (const s of this.surfaces.values()) {
      storms += s.tornadoes.length;
      water += s.hydro.totalWater();
    }
    return {
      'Surfaces resident': String(this.surfaces.size),
      'Active storms': String(storms),
      'Water simulated': water.toFixed(0),
      'Surface-seconds': this.totalAge.toFixed(0)
    };
  }

  dispose(): void { this.surfaces.clear(); }
}
