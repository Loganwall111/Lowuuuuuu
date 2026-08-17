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
import { ChunkStreamer } from './ChunkedUniverse';

/**
 * Shortest distance from point p to the segment a->b.
 *
 * Exported so the swept horizon test can be verified without a scene.
 * This is what turns "am I inside the hole this instant" into "did my
 * path pass through the hole", which is the difference between catching
 * a crossing and tunnelling straight through it at warp.
 */
export function segmentSphereFirstHit(
  a: Vector3, b: Vector3, center: Vector3, radius: number
): number | null {
  const dx=b.x-a.x, dy=b.y-a.y, dz=b.z-a.z;
  const ox=a.x-center.x, oy=a.y-center.y, oz=a.z-center.z;
  const aa=dx*dx+dy*dy+dz*dz;
  const cc=ox*ox+oy*oy+oz*oz-radius*radius;
  if (cc <= 0) return 0;
  if (!(aa > 1e-12)) return null;
  const bb=2*(ox*dx+oy*dy+oz*dz);
  const disc=bb*bb-4*aa*cc;
  if (disc < 0) return null;
  const root=Math.sqrt(disc);
  const t0=(-bb-root)/(2*aa), t1=(-bb+root)/(2*aa);
  if (t0 >= 0 && t0 <= 1) return t0;
  if (t1 >= 0 && t1 <= 1) return t1;
  return null;
}

export function segmentPointDistance(a: Vector3, b: Vector3, p: Vector3): number {
  const abx = b.x - a.x, aby = b.y - a.y, abz = b.z - a.z;
  const apx = p.x - a.x, apy = p.y - a.y, apz = p.z - a.z;
  const len2 = abx * abx + aby * aby + abz * abz;
  if (!(len2 > 1e-12)) {
    return Math.sqrt(apx * apx + apy * apy + apz * apz);
  }
  let t = (apx * abx + apy * aby + apz * abz) / len2;
  t = Math.max(0, Math.min(1, t));
  const cx = a.x + abx * t, cy = a.y + aby * t, cz = a.z + abz * t;
  const dx = p.x - cx, dy = p.y - cy, dz = p.z - cz;
  return Math.sqrt(dx * dx + dy * dy + dz * dz);
}
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
  /** True for the one supermassive hole at a galaxy's centre. */
  galacticCore?: boolean;
  /** Deterministic live orbit around another region. */
  orbitParentId?: string;
  orbitRadius?: number;
  orbitPhase?: number;
  orbitRate?: number;
  orbitInclination?: number;
}

export interface UniverseOptions {
  seed: number;
  /** How far apart star systems are generated. */
  spacing: number;
  /** Radius around the origin that is populated at startup. */
  extent: number;
}

/**
 * Regions inside this radius are the hand-built ones: the home system and
 * the guaranteed ocean/terrain/black hole near it. Everything beyond is
 * generated on demand and has no edge.
 */
export const CORE_RADIUS = 14800;

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

/**
 * The galactic plane. Supermassive cores sit exactly here.
 *
 * Not a tolerance band - an exact value. A core defines the plane its disc
 * orbits in, so any deviation at all is the core being in the wrong place.
 */
export const GALACTIC_PLANE_Y = 0;

export class UniverseState {
  opts: UniverseOptions;
  regions: Region[] = [];
  /**
   * The hand-built heart of the universe: the home system and its
   * guaranteed neighbours. Kept separate so streaming can replace the
   * outer regions without ever disturbing these.
   */
  private coreRegions: Region[] = [];
  /**
   * Everything past the core, generated in chunks as you approach and
   * forgotten as you leave. This is what makes space endless: no total is
   * ever stored, so there is no size to run out of.
   */
  streamer = new ChunkStreamer();
  /** Set false to pin the universe to its hand-built core, for tests. */
  streaming = true;
  /** Where the player is right now, in universe coordinates. */
  playerPos = new Vector3(0, 0, -220);
  /** The region the player is currently inside, if any. */
  current: Region | null = null;
  /** Set when the player crosses a horizon; drives the look-back view. */
  insideHorizon: Region | null = null;
  /** Where the player was last frame, for swept collision tests. */
  readonly lastPlayerPos = new Vector3();
  /** False until the first updatePlayer, so frame one has no phantom sweep. */
  private playerStarted = false;
  horizonDepth = 0;
  /** Once gameplay begins an interior descent, geometry can no longer eject it. */
  private latchedHorizonId: string | null = null;

  private seq = 0;
  private orbitalTime = 0;

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
    this.orbitalTime = 0;
    const rng = makeRng(this.opts.seed);
    const { spacing, extent } = this.opts;

    // ---- the home star system, always at the origin so you start somewhere ----
    const home = this.addStarSystem(new Vector3(0, 0, 0), rng, 'Home');

    // There are no side tabs any more: an ocean world, a terrain world and a
    // black hole are *places*. Guarantee one of each within easy reach of
    // the start, or a new player could fly for a long time without finding
    // the things the sandbox is built around.
    this.ensureNearHome(home, rng);

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
        // SINGULARITIES BELONG AT GALACTIC CENTRES.
        //
        // Black holes used to be scattered across this lattice at 16% of
        // occupied cells, which put them adrift in empty intergalactic
        // space with nothing around them. Real supermassive holes sit at
        // the centre of a galaxy, so that is the only place the lattice
        // may create one; addGalaxy() places the hole at its own core.
        const roll = rng();
        if (roll < 0.68) this.addStarSystem(p, rng);
        else if (roll < 0.86) this.addNebula(p, rng);
        else this.addGalaxy(p, rng);
      }
    }

    // Everything above is the authored core. Past it, space is generated on
    // demand from the same seed and never ends.
    this.coreRegions = [...this.regions];
    this.streamer = new ChunkStreamer({ seed: this.opts.seed });
    this.streamer.clear();
  }

  /**
   * Streams the endless part of space around a viewpoint.
   *
   * The core is always present; chunked regions are appended for wherever
   * you happen to be. Chunks far from the core are skipped so the authored
   * neighbourhood is never doubled up with generated stars.
   */
  streamAround(eye: Vector3): boolean {
    if (!this.streaming) return false;
    const changed = this.streamer.update(eye);
    if (!changed) return false;
    const streamed = this.streamer.regions()
      .filter((r) => r.position.length() > CORE_RADIUS);
    this.regions = this.coreRegions.concat(streamed);
    return true;
  }

  /**
   * Guarantees an ocean world, a terrain world and a black hole close to the
   * origin. Called once at generation; if the random pass already produced
   * one nearby, nothing is added.
   */
  private ensureNearHome(home: Region, rng: () => number): void {
    const near = (kind: RegionKind, within: number) =>
      this.regions.some((r) => r.kind === kind &&
        Vector3.Distance(r.position, home.position) < within);

    const place = (kind: RegionKind, name: string, glyph: string,
                   dist: number, surface: number) => {
      const a = rng() * Math.PI * 2;
      this.regions.push({
        id: this.nextId('pl'),
        kind,
        name,
        glyph,
        position: home.position.add(new Vector3(
          Math.cos(a) * dist, (rng() - 0.5) * 40, Math.sin(a) * dist)),
        radius: surface * 4.5,
        mass: 60 + rng() * 400,
        seed: Math.floor(rng() * 0xffffffff) >>> 0,
        surfaceRadius: surface
      });
    };

    if (!near('ocean', 900)) place('ocean', 'Home II', '🌊', 320, 58);
    if (!near('terrain', 900)) place('terrain', 'Home III', '⛰', 520, 52);
    // The player must always have a black hole within reach, or the whole
    // singularity half of the game is gated behind a long random search.
    // But a lone hole floating in empty space is exactly the "random
    // sparkle" being removed, so this one is given the galaxy it deserves:
    // addGalaxy places a supermassive core at its own centre, which
    // satisfies both rules at once.
    if (!near('blackhole', 4200)) {
      this.addGalaxy(home.position.add(new Vector3(
        (rng() - 0.5) * 600, 0, -2600 - rng() * 600)), rng);
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
      // Planetary bodies use a larger astronomical presentation scale so
      // perspective growth is readable over interplanetary approach.
      const surface = 34 + rng() * 72;
      const inclination = (rng() - 0.5) * 0.24;
      this.regions.push({
        id: this.nextId('pl'),
        kind,
        name: sys.name + ' ' + 'IVXLC'.charAt(i % 5) + (i + 1),
        glyph: kind === 'ocean' ? '🌊' : kind === 'terrain' ? '⛰' : '🪐',
        position: at.add(new Vector3(Math.cos(ang) * orbit,
          Math.sin(ang) * orbit * Math.sin(inclination), Math.sin(ang) * orbit)),
        radius: surface * 4.5,
        mass: 60 + rng() * 400,
        seed: Math.floor(rng() * 0xffffffff) >>> 0,
        surfaceRadius: surface,
        orbitParentId: sys.id,
        orbitRadius: orbit,
        orbitPhase: ang,
        orbitRate: 0.012 * Math.pow(120 / orbit, 1.5),
        orbitInclination: inclination
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
    const types = ['Emission', 'Reflection', 'Dark Molecular', 'Supernova Veil',
      'Bipolar', 'Wolf-Rayet', 'Proto-Planetary', 'Ionized Oxygen'];
    const subtype = types[Math.floor(rng() * types.length) % types.length];
    const r: Region = {
      id: this.nextId('neb'),
      kind: 'nebula',
      name: nameFor(rng) + ' ' + subtype + ' Nebula',
      glyph: '🌫',
      position: at.clone(),
      radius: 1400,
      mass: 0,
      seed: Math.floor(rng() * 0xffffffff) >>> 0,
      data: { subtype, turbulence: rng(), ionization: rng(), filamentScale: rng() }
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
    // Every galaxy has one supermassive singularity at its exact centre.
    // This is the ONLY way space itself creates a black hole; the player
    // can still place their own anywhere via spawnBlackHole().
    //
    // THE CORE IS PINNED TO THE GALACTIC PLANE. A singularity is what the
    // disc orbits, so it defines y = 0 rather than floating relative to it.
    // Inheriting the region's own y let cores sit hundreds of units above
    // or below the plane - a visible drift, because the disc is only ~600
    // units thick there, so half a thickness reads as "hovering off to one
    // side of the galaxy" rather than sitting in its heart.
    const core = this.addBlackHole(
      new Vector3(at.x, GALACTIC_PLANE_Y, at.z), rng);
    core.name = r.name.replace(' Galaxy', '') + ' Core';
    core.mass = 60000 + rng() * 90000;
    core.galacticCore = true;
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
    if (this.latchedHorizonId === id) this.latchedHorizonId = null;
    return true;
  }

  /**
   * Clears the "inside a horizon" state for one hole.
   *
   * Called when a descent completes: the player has left through the far
   * side, so they are no longer inside that hole even though their world
   * coordinates have not changed. Without this the next frame would start a
   * fresh fall into the hole they just came out of.
   */
  latchHorizon(id: string): void {
    if (this.insideHorizon?.id === id) this.latchedHorizonId = id;
  }

  leaveHorizon(id: string): void {
    if (this.insideHorizon?.id === id) {
      this.insideHorizon = null;
      this.horizonDepth = 0;
    }
    if (this.latchedHorizonId === id) this.latchedHorizonId = null;
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

  /** Advances every authored planetary orbit from deterministic parameters. */
  advanceOrbits(dt: number): void {
    if (!Number.isFinite(dt) || dt <= 0) return;
    this.orbitalTime += Math.min(dt, 0.1);
    const byId = new Map(this.regions.map((r) => [r.id, r]));
    for (const r of this.regions) {
      if (!r.orbitParentId || !(r.orbitRadius! > 0)) continue;
      const parent = byId.get(r.orbitParentId);
      if (!parent) continue;
      const a = (r.orbitPhase ?? 0) + this.orbitalTime * (r.orbitRate ?? 0);
      const radius = r.orbitRadius!;
      const inc = r.orbitInclination ?? 0;
      r.position.set(
        parent.position.x + Math.cos(a) * radius,
        parent.position.y + Math.sin(a) * radius * Math.sin(inc),
        parent.position.z + Math.sin(a) * radius);
    }
  }

  /**
   * Updates where the player is. Returns the region they are now in, which
   * is what the UI shows instead of a tab.
   */
  updatePlayer(pos: Vector3): Region | null {
    // Keep the previous position before overwriting it: the horizon test
    // below needs the segment travelled, not just where we ended up.
    //
    // On the FIRST call there is no previous position - playerPos is still
    // the zero vector - so the "segment travelled" would be a line from the
    // world origin to wherever the player actually is. That line can pass
    // straight through a black hole the player is nowhere near, and the
    // swept test would report them as already inside its horizon on frame
    // one. Seeding both endpoints to the true starting position makes the
    // first step a zero-length segment, which can only ever be inside a
    // horizon the player genuinely is inside.
    if (!this.playerStarted) {
      this.playerStarted = true;
      this.playerPos.copyFrom(pos);
    }
    this.lastPlayerPos.copyFrom(this.playerPos);
    this.playerPos.copyFrom(pos);
    const inside = this.containing(pos);
    // the smallest containing region wins, so a planet beats its star system
    inside.sort((a, b) => a.radius - b.radius);
    this.current = inside[0] ?? null;

    // ---- unified irreversible horizon capture ----
    // Solve the segment/sphere quadratic, not a frame-end proximity sample.
    // Once a future-directed worldline intersects an event horizon it cannot
    // become exterior again; only leaveHorizon(), after the non-Euclidean
    // destination handshake, may release this latch.
    if (this.insideHorizon && this.latchedHorizonId === this.insideHorizon.id) {
      this.horizonDepth = Math.max(this.horizonDepth, 1);
    } else {
      const endpointHole = inside.find((r) => r.kind === 'blackhole') ?? null;
      const bh = endpointHole ?? this.sweptHole(this.lastPlayerPos, pos);
      if (bh) {
        const horizon = this.horizonRadiusOf(bh);
        const endD = Vector3.Distance(pos, bh.position);
        const hit = segmentSphereFirstHit(this.lastPlayerPos, pos, bh.position, horizon);
        if (endD <= horizon || hit !== null) {
          this.insideHorizon = bh;
          this.latchedHorizonId = bh.id;
          this.horizonDepth = endD <= horizon
            ? Math.max(0, Math.min(1, 1-endD/Math.max(horizon,1e-3)))
            : 1;
        }
      }
    }

    return this.current;
  }

  /**
   * Any black hole whose horizon the segment a->b passes through.
   *
   * Only holes are considered, and only their horizons, so this stays
   * cheap: it is a point-to-segment distance per hole.
   */
  private sweptHole(a: Vector3, b: Vector3): Region | null {
    let best: Region | null = null;
    let firstT = Infinity;
    for (const r of this.regions) {
      if (r.kind !== 'blackhole') continue;
      const t = segmentSphereFirstHit(a, b, r.position, this.horizonRadiusOf(r));
      if (t !== null && t < firstT) {
        firstT = t;
        best = r;
      }
    }
    return best;
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
