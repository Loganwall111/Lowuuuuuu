/**
 * UniverseState — one continuous universe, not a set of separate levels.
 *
 * Everything the player can visit lives in a single coordinate space at all
 * times: stars, planets, black holes, oceans, terrain, portals. There is no
 * "loading a world". Moving between them is just flying, and what you are
 * near determines what is simulated in detail.
 *
 * A Region is a place in that universe. Regions have a position and a radius
 * of influence; the active region is simply the nearest one you are inside.
 * That is what replaces the tab bar.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { makeRng, hashSeed } from './DimensionSystem';
import {
  LENS_PROFILES, cloneProfile, randomAlienProfile, sanitizeProfile,
  type LensProfile
} from './LensProfiles';

export type RegionKind =
  | 'deep-space' | 'star-system' | 'planet' | 'ocean' | 'terrain'
  | 'blackhole' | 'nebula' | 'galaxy' | 'dimension';

export interface Region {
  id: string;
  kind: RegionKind;
  name: string;
  glyph: string;
  position: Vector3;
  /** Everything within this radius counts as "here". */
  radius: number;
  /** Gravitational mass, in simulation units. */
  mass: number;
  seed: number;
  /** Black holes carry their own lens. */
  lens?: LensProfile;
  /** Planets/oceans carry a surface radius for landing and walking. */
  surfaceRadius?: number;
  /** Arbitrary per-kind data. */
  data?: Record<string, number | string | boolean>;
  /** Whether the player created this, so it can be deleted or moved. */
  playerMade?: boolean;
}

export interface UniverseOptions {
  seed: number;
  /** How far apart star systems are generated. */
  spacing: number;
  /** Radius around the origin that is populated at startup. */
  extent: number;
}

export const DEFAULT_UNIVERSE: UniverseOptions = {
  seed: 20260813,
  spacing: 2600,
  extent: 12000
};

const NAME_A = ['Kepler', 'Vela', 'Cygnus', 'Lyra', 'Orion', 'Draco', 'Corvus',
  'Aquila', 'Hydra', 'Tucana', 'Phoenix', 'Perseus', 'Auriga', 'Cetus',
  'Norma', 'Pyxis', 'Volans', 'Carina', 'Dorado', 'Fornax'];
const NAME_B = ['Prime', 'Reach', 'Deep', 'Gate', 'Drift', 'Verge', 'Span',
  'Hollow', 'Crown', 'Rift', 'Expanse', 'Cradle', 'Sink', 'Bloom'];

function nameFor(rng: () => number): string {
  const a = NAME_A[Math.floor(rng() * NAME_A.length) % NAME_A.length];
  const b = NAME_B[Math.floor(rng() * NAME_B.length) % NAME_B.length];
  return a + ' ' + b + '-' + (100 + Math.floor(rng() * 899));
}

export class UniverseState {
  opts: UniverseOptions;
  regions: Region[] = [];
  /** Where the player is right now, in universe coordinates. */
  playerPos = new Vector3(0, 0, -220);
  /** The region the player is currently inside, if any. */
  current: Region | null = null;
  /** Set when the player crosses a horizon; drives the look-back view. */
  insideHorizon: Region | null = null;
  horizonDepth = 0;

  private seq = 0;

  constructor(opts: Partial<UniverseOptions> = {}) {
    this.opts = { ...DEFAULT_UNIVERSE, ...opts };
    this.generate();
  }

  /* ------------------------------ generation ------------------------------ */

  /**
   * Throws the current universe away and builds a brand new one.
   *
   * Passing no seed picks a random one, which is what "NEW UNIVERSE" in the
   * menu does. Returns the seed so it can be shown or saved.
   */
  reseed(seed?: number): number {
    const next = Number.isFinite(seed as number)
      ? (seed as number) >>> 0
      : (Math.floor(Math.random() * 0xffffffff) >>> 0);
    this.opts.seed = next;
    this.generate();
    return next;
  }

  /** Populates the universe deterministically from the seed. */
  generate(): void {
    this.regions = [];
    this.seq = 0;
    const rng = makeRng(this.opts.seed);
    const { spacing, extent } = this.opts;

    // ---- the home star system, always at the origin so you start somewhere ----
    this.addStarSystem(new Vector3(0, 0, 0), rng, 'Home');

    // ---- surrounding star systems on a jittered lattice ----
    const steps = Math.max(1, Math.floor(extent / spacing));
    for (let ix = -steps; ix <= steps; ix++) {
      for (let iz = -steps; iz <= steps; iz++) {
        if (ix === 0 && iz === 0) continue;
        // not every lattice cell is occupied; space should feel sparse
        if (rng() > 0.55) continue;
        const p = new Vector3(
          ix * spacing + (rng() - 0.5) * spacing * 0.6,
          (rng() - 0.5) * spacing * 0.35,
          iz * spacing + (rng() - 0.5) * spacing * 0.6);
        const roll = rng();
        if (roll < 0.62) this.addStarSystem(p, rng);
        else if (roll < 0.78) this.addBlackHole(p, rng);
        else if (roll < 0.9) this.addNebula(p, rng);
        else this.addGalaxy(p, rng);
      }
    }
  }

  private nextId(prefix: string): string {
    return prefix + '-' + (++this.seq);
  }

  addStarSystem(at: Vector3, rng: () => number, label?: string): Region {
    const seed = Math.floor(rng() * 0xffffffff) >>> 0;
    const sys: Region = {
      id: this.nextId('sys'),
      kind: 'star-system',
      name: label ?? nameFor(rng),
      glyph: '☀',
      position: at.clone(),
      radius: 900,
      mass: 1200 + rng() * 2600,
      seed
    };
    this.regions.push(sys);

    // planets orbiting it, each a place you can actually go
    const n = 1 + Math.floor(rng() * 5);
    for (let i = 0; i < n; i++) {
      const orbit = 120 + i * (70 + rng() * 90);
      const ang = rng() * Math.PI * 2;
      const kindRoll = rng();
      const kind: RegionKind = kindRoll < 0.25 ? 'ocean'
        : kindRoll < 0.55 ? 'terrain' : 'planet';
      const surface = 18 + rng() * 42;
      this.regions.push({
        id: this.nextId('pl'),
        kind,
        name: sys.name + ' ' + 'IVXLC'.charAt(i % 5) + (i + 1),
        glyph: kind === 'ocean' ? '🌊' : kind === 'terrain' ? '⛰' : '🪐',
        position: at.add(new Vector3(Math.cos(ang) * orbit, (rng() - 0.5) * 40,
          Math.sin(ang) * orbit)),
        radius: surface * 4.5,
        mass: 60 + rng() * 400,
        seed: Math.floor(rng() * 0xffffffff) >>> 0,
        surfaceRadius: surface
      });
    }
    return sys;
  }

  addBlackHole(at: Vector3, rng: () => number, lens?: LensProfile): Region {
    // most holes out in the wild are exotic; that is the point of the sandbox
    const profile = lens
      ? sanitizeProfile(lens)
      : (rng() < 0.45
          ? cloneProfile(LENS_PROFILES.schwarzschild)
          : randomAlienProfile(rng));
    const r: Region = {
      id: this.nextId('bh'),
      kind: 'blackhole',
      name: nameFor(rng) + ' Singularity',
      glyph: '⚫',
      position: at.clone(),
      radius: 620,
      mass: 4000 + rng() * 40000,
      seed: Math.floor(rng() * 0xffffffff) >>> 0,
      lens: profile
    };
    this.regions.push(r);
    return r;
  }

  addNebula(at: Vector3, rng: () => number): Region {
    const r: Region = {
      id: this.nextId('neb'),
      kind: 'nebula',
      name: nameFor(rng) + ' Nebula',
      glyph: '🌫',
      position: at.clone(),
      radius: 1400,
      mass: 0,
      seed: Math.floor(rng() * 0xffffffff) >>> 0
    };
    this.regions.push(r);
    return r;
  }

  addGalaxy(at: Vector3, rng: () => number): Region {
    const r: Region = {
      id: this.nextId('gal'),
      kind: 'galaxy',
      name: nameFor(rng) + ' Galaxy',
      glyph: '🌌',
      position: at.clone(),
      radius: 2600,
      mass: 90000,
      seed: Math.floor(rng() * 0xffffffff) >>> 0
    };
    this.regions.push(r);
    return r;
  }

  /* ------------------------------- authoring ------------------------------- */

  /** Player-created objects are first-class regions. */
  spawnBlackHole(at: Vector3, lens?: LensProfile, mass = 9000): Region {
    const rng = makeRng(hashSeed(at.toString() + this.seq));
    const r = this.addBlackHole(at, rng, lens);
    r.mass = mass;
    r.playerMade = true;
    r.name = 'Your Singularity ' + this.seq;
    return r;
  }

  spawnStarSystem(at: Vector3): Region {
    const rng = makeRng(hashSeed('sys' + at.toString() + this.seq));
    const r = this.addStarSystem(at, rng);
    r.playerMade = true;
    return r;
  }

  /** Moves a region. Black holes can be dragged around the universe. */
  moveRegion(id: string, to: Vector3): boolean {
    const r = this.byId(id);
    if (!r) return false;
    const delta = to.subtract(r.position);
    r.position.copyFrom(to);
    // a star system carries its planets with it
    if (r.kind === 'star-system') {
      for (const o of this.regions) {
        if (o === r) continue;
        if (o.name.startsWith(r.name + ' ')) o.position.addInPlace(delta);
      }
    }
    return true;
  }

  removeRegion(id: string): boolean {
    const i = this.regions.findIndex((r) => r.id === id);
    if (i < 0) return false;
    this.regions.splice(i, 1);
    if (this.current?.id === id) this.current = null;
    if (this.insideHorizon?.id === id) {
      this.insideHorizon = null;
      this.horizonDepth = 0;
    }
    return true;
  }

  byId(id: string): Region | null {
    return this.regions.find((r) => r.id === id) ?? null;
  }

  /* -------------------------------- queries -------------------------------- */

  /** The nearest region of any kind. */
  nearest(pos: Vector3, kind?: RegionKind): Region | null {
    let best: Region | null = null;
    let bestD = Infinity;
    for (const r of this.regions) {
      if (kind && r.kind !== kind) continue;
      const d = Vector3.DistanceSquared(pos, r.position);
      if (d < bestD) { bestD = d; best = r; }
    }
    return best;
  }

  /** Every region whose sphere of influence contains this point. */
  containing(pos: Vector3): Region[] {
    return this.regions.filter(
      (r) => Vector3.Distance(pos, r.position) <= r.radius);
  }

  /** Regions worth simulating in detail right now. */
  activeRegions(pos: Vector3, budget = 12): Region[] {
    return [...this.regions]
      .map((r) => ({ r, d: Vector3.Distance(pos, r.position) - r.radius }))
      .sort((a, b) => a.d - b.d)
      .slice(0, budget)
      .map((x) => x.r);
  }

  /**
   * Updates where the player is. Returns the region they are now in, which
   * is what the UI shows instead of a tab.
   */
  updatePlayer(pos: Vector3): Region | null {
    this.playerPos.copyFrom(pos);
    const inside = this.containing(pos);
    // the smallest containing region wins, so a planet beats its star system
    inside.sort((a, b) => a.radius - b.radius);
    this.current = inside[0] ?? null;

    // ---- horizon crossing ----
    const bh = inside.find((r) => r.kind === 'blackhole');
    if (bh) {
      const d = Vector3.Distance(pos, bh.position);
      const horizon = this.horizonRadiusOf(bh);
      if (d <= horizon) {
        this.insideHorizon = bh;
        // 0 at the horizon, 1 at the singularity
        this.horizonDepth = Math.max(0, Math.min(1, 1 - d / Math.max(horizon, 1e-3)));
      } else if (this.insideHorizon?.id === bh.id) {
        this.insideHorizon = null;
        this.horizonDepth = 0;
      }
    } else if (this.insideHorizon) {
      this.insideHorizon = null;
      this.horizonDepth = 0;
    }

    return this.current;
  }

  horizonRadiusOf(r: Region): number {
    return Math.max(2, Math.cbrt(Math.max(r.mass, 1)) * 0.9);
  }

  /**
   * Net gravitational acceleration at a point, from every massive region.
   * This is what makes the universe continuous: the same field acts on the
   * player, on thrown objects, and on light.
   */
  gravityAt(pos: Vector3, G = 42): Vector3 {
    const acc = new Vector3(0, 0, 0);
    for (const r of this.regions) {
      if (r.mass <= 0) continue;
      const d = r.position.subtract(pos);
      const dist2 = d.lengthSquared();
      if (dist2 < 1e-6) continue;
      const soft = Math.max(r.radius * 0.05, 4);
      const f = (G * r.mass) / (dist2 + soft * soft);
      acc.addInPlace(d.normalize().scale(f));
    }
    return acc;
  }

  stats(): Record<string, string> {
    const count = (k: RegionKind) => this.regions.filter((r) => r.kind === k).length;
    return {
      'Location': this.current
        ? this.current.glyph + ' ' + this.current.name
        : '🌌 Deep space',
      'Star systems': String(count('star-system')),
      'Planets': String(count('planet') + count('ocean') + count('terrain')),
      'Black holes': String(count('blackhole')),
      'Nebulae': String(count('nebula')),
      'Galaxies': String(count('galaxy')),
      'Total objects': String(this.regions.length),
      'Inside horizon': this.insideHorizon
        ? this.insideHorizon.name + ' (' + Math.round(this.horizonDepth * 100) + '%)'
        : 'no'
    };
  }
}
