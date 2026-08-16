/**
 * ChunkedUniverse — space that keeps going.
 *
 * The universe used to be 141 regions baked into an array at startup. Past
 * about fifteen thousand units there was nothing, and at full warp you
 * crossed the whole populated volume in a fiftieth of a second.
 *
 * This generates space in cubic chunks on demand instead. A chunk's contents
 * are a pure function of its integer coordinates and the universe seed, so:
 *
 *   - there is no edge, and no total to store;
 *   - the same chunk always contains the same stars, whether you arrive from
 *     the left, from the right, or ten hours later;
 *   - nothing needs saving, because nothing was ever authored.
 *
 * The thing to avoid is *visible* repetition. A naive hash of (x,y,z) gives
 * you infinite space that all looks identical, which is worse than a small
 * universe because it actively tells you the world is fake. Two defences:
 *
 *   1. Every chunk hashes to its own independent RNG stream, so no two
 *      chunks share a layout. There is no tile to spot.
 *   2. Structure is imposed at scales far larger than one chunk - filaments,
 *      voids and superclusters from low-frequency noise - so the sky has
 *      shape at every zoom level rather than being uniform static.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import type { Region, RegionKind } from './UniverseState';
import { GALACTIC_PLANE_Y } from './UniverseState';
import { latticeGalaxiesInChunk } from './IntergalacticGrid';

/** Edge length of one chunk, world units. */
export const CHUNK_SIZE = 2600;

/** Chunks kept loaded in each direction from the viewer. */
export const DEFAULT_VIEW_CHUNKS = 3;

export interface ChunkCoord { cx: number; cy: number; cz: number; }

export interface ChunkedOptions {
  seed: number;
  chunkSize: number;
  /** Radius in chunks to keep resident. */
  viewChunks: number;
  /** Average regions per occupied chunk. */
  density: number;
}

export const DEFAULT_CHUNKED: ChunkedOptions = {
  seed: 20260813,
  chunkSize: CHUNK_SIZE,
  viewChunks: DEFAULT_VIEW_CHUNKS,
  density: 1.15
};

/* -------------------------------------------------------------------------- */
/*  Hashing                                                                    */
/* -------------------------------------------------------------------------- */

/**
 * Integer hash of a chunk coordinate plus the universe seed.
 *
 * The large odd multipliers are the usual trick for decorrelating nearby
 * inputs: without them (1,0,0) and (0,1,0) produce visibly similar chunks
 * and the grid becomes obvious.
 */
export function hashChunk(cx: number, cy: number, cz: number, seed: number): number {
  // Folded into a finite domain on purpose. `| 0` already wrapped at 32
  // bits, and past 2^53 a chunk index and its neighbour are the same float
  // anyway - so beyond a certain distance genuinely-novel space is not
  // physically representable, whatever generator is used.
  //
  // Rather than let that decay into garbage (neighbouring chunks collapsing
  // to identical content, or NaN), the domain is folded explicitly. Space
  // repeats on a period of SUPER_PERIOD chunks - but each repetition is
  // offset and re-mixed by its supercell index, so the *same layout never
  // lands in the same arrangement twice*. It reads as endless variety
  // because the repeat distance is 1.4e13 units: crossing one period at
  // full warp takes about eight months.
  const wx = foldCoord(cx);
  const wy = foldCoord(cy);
  const wz = foldCoord(cz);
  let h = seed >>> 0;
  h = Math.imul(h ^ wx, 0x27d4eb2d) >>> 0;
  h = Math.imul(h ^ wy, 0x165667b1) >>> 0;
  h = Math.imul(h ^ wz, 0x9e3779b1) >>> 0;
  h ^= h >>> 15;
  h = Math.imul(h, 0x85ebca6b) >>> 0;
  h ^= h >>> 13;
  return h >>> 0;
}

/**
 * Period of the repeat, in chunks.
 *
 * 5,381,203 chunks x 2600 units is about 1.4e10 units - roughly six hours
 * of continuous flight at full warp to cross once, and the layout that
 * recurs after it is re-mixed anyway. Prime, so the three axes cannot
 * resonate into a visible grid.
 */
export const SUPER_PERIOD = 5381203;

/**
 * Which repetition of the pattern a chunk falls in.
 *
 * This is what makes the fold invisible. Two chunks a whole period apart
 * hash the same, but they get different supercell indices, so their
 * contents are redistributed and their names differ. The structure recurs;
 * the actual sky never does.
 */
export function superIndex(cx: number, cy: number, cz: number): number {
  const q = (c: number) =>
    Number.isFinite(c) ? Math.floor(Math.round(c) / SUPER_PERIOD) : 0;
  let h = 0x811c9dc5;
  h = Math.imul(h ^ (q(cx) | 0), 0x01000193) >>> 0;
  h = Math.imul(h ^ (q(cy) | 0), 0x01000193) >>> 0;
  h = Math.imul(h ^ (q(cz) | 0), 0x01000193) >>> 0;
  return h >>> 0;
}

/** Folds an unbounded chunk index into the period, safely for any input. */
export function foldCoord(c: number): number {
  if (!Number.isFinite(c)) return 0;
  // Math.round first: past 2^53 the value is not an exact integer, and a
  // fractional index would hash inconsistently.
  const v = Math.round(c) % SUPER_PERIOD;
  return (v < 0 ? v + SUPER_PERIOD : v) | 0;
}

/** Deterministic stream from a hash. */
export function streamFrom(hash: number): () => number {
  let a = (hash >>> 0) || 1;
  return () => {
    a = (a + 0x6D2B79F5) >>> 0;
    let t = a;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/** Smooth value noise in 3-D from the chunk hash, used for large structure. */
function valueNoise(x: number, y: number, z: number, seed: number): number {
  const xi = Math.floor(x), yi = Math.floor(y), zi = Math.floor(z);
  const xf = x - xi, yf = y - yi, zf = z - zi;
  const fade = (t: number) => t * t * (3 - 2 * t);
  const u = fade(xf), v = fade(yf), w = fade(zf);
  const at = (i: number, j: number, k: number) =>
    hashChunk(i, j, k, seed) / 4294967296;
  const lerp = (a: number, b: number, t: number) => a + (b - a) * t;
  const x00 = lerp(at(xi, yi, zi), at(xi + 1, yi, zi), u);
  const x10 = lerp(at(xi, yi + 1, zi), at(xi + 1, yi + 1, zi), u);
  const x01 = lerp(at(xi, yi, zi + 1), at(xi + 1, yi, zi + 1), u);
  const x11 = lerp(at(xi, yi + 1, zi + 1), at(xi + 1, yi + 1, zi + 1), u);
  return lerp(lerp(x00, x10, v), lerp(x01, x11, v), w);
}

/**
 * How crowded space is at a point, 0 (void) to 1 (supercluster core).
 *
 * Two octaves at very low frequency, so the structure spans hundreds of
 * chunks. This is what stops infinite space from looking like uniform
 * static: there are genuine voids you can fly through and genuine walls of
 * galaxies, at a scale far larger than anything one chunk could express.
 */
export function cosmicDensity(pos: Vector3, seed: number, chunkSize: number): number {
  const s = 1 / (chunkSize * 42);
  const a = valueNoise(pos.x * s, pos.y * s, pos.z * s, seed);
  const b = valueNoise(pos.x * s * 3.7, pos.y * s * 3.7, pos.z * s * 3.7, seed ^ 0x5bf03635);
  // Sharpened so voids are genuinely empty rather than merely thinner.
  const raw = a * 0.72 + b * 0.28;
  return Math.max(0, Math.min(1, Math.pow(raw, 1.7) * 1.9));
}

/* -------------------------------------------------------------------------- */
/*  Naming                                                                     */
/* -------------------------------------------------------------------------- */

const CAT = ['Kepler', 'Vela', 'Cygnus', 'Lyra', 'Orion', 'Draco', 'Corvus',
  'Aquila', 'Hydra', 'Tucana', 'Phoenix', 'Perseus', 'Auriga', 'Cetus',
  'Norma', 'Pyxis', 'Volans', 'Carina', 'Dorado', 'Fornax', 'Antlia',
  'Caelum', 'Grus', 'Indus', 'Lepus', 'Mensa', 'Octans', 'Reticulum'];

/**
 * Names derived from position, so a star's name is a fact about where it is
 * rather than a counter that depends on visit order.
 */
export function nameFor(kind: RegionKind, cx: number, cy: number, cz: number, i: number): string {
  const h = (hashChunk(cx, cy, cz, i * 7919 + 13) ^
             Math.imul(superIndex(cx, cy, cz), 0x85ebca6b)) >>> 0;
  const cat = CAT[h % CAT.length];
  const num = (h >>> 8) % 9000 + 100;
  const suffix = kind === 'galaxy' ? ' Cluster'
    : kind === 'nebula' ? ' Nebula'
      : kind === 'blackhole' ? ' Singularity'
        : '';
  return cat + '-' + num + suffix;
}

/* -------------------------------------------------------------------------- */
/*  Generation                                                                 */
/* -------------------------------------------------------------------------- */

export interface Chunk {
  key: string;
  coord: ChunkCoord;
  regions: Region[];
}

export function chunkKey(cx: number, cy: number, cz: number): string {
  return cx + ',' + cy + ',' + cz;
}

/** Which chunk a world position falls in. */
export function chunkAt(pos: Vector3, chunkSize: number): ChunkCoord {
  return {
    cx: Math.floor(pos.x / chunkSize),
    cy: Math.floor(pos.y / chunkSize),
    cz: Math.floor(pos.z / chunkSize)
  };
}

/**
 * Builds one chunk. Pure: same coordinates and seed always give the same
 * contents, which is what lets the universe be infinite without being
 * stored.
 */
export function generateChunk(
  cx: number, cy: number, cz: number, opts: ChunkedOptions
): Chunk {
  const { seed, chunkSize, density } = opts;
  // Which repetition of the pattern this chunk belongs to. Chunks in
  // different supercells share a base layout but are re-mixed by this, so
  // the repeat is never recognisable: the same "corner" of the pattern
  // comes back with its contents redistributed and renamed.
  const supercell = superIndex(cx, cy, cz);
  const rng = streamFrom(hashChunk(cx, cy, cz, seed) ^ Math.imul(supercell, 0x9e3779b1));
  const regions: Region[] = [];

  const origin = new Vector3(cx * chunkSize, cy * chunkSize, cz * chunkSize);
  const centre = new Vector3(
    origin.x + chunkSize * 0.5, origin.y + chunkSize * 0.5, origin.z + chunkSize * 0.5);

  // Large-scale structure decides whether anything lives here at all.
  const field = cosmicDensity(centre, seed, chunkSize);
  const expected = density * field * 2.2;
  let count = Math.floor(expected);
  if (rng() < expected - count) count++;

  for (let i = 0; i < count; i++) {
    const pos = new Vector3(
      origin.x + rng() * chunkSize,
      origin.y + rng() * chunkSize,
      origin.z + rng() * chunkSize);

    // What kind of thing it is depends on how crowded the neighbourhood is:
    // Galaxies cluster in dense regions. Black holes are NOT scattered
    // through the void any more - a supermassive singularity belongs at the
    // centre of a galaxy, so 'galaxy' carries one implicitly and the loose
    // 'blackhole' rolls are gone. This is what stopped holes appearing as
    // random sparkles in otherwise empty intergalactic space.
    const roll = rng();
    let kind: RegionKind;
    if (field > 0.72) {
      // NO 'galaxy' REGION HERE.
      //
      // Chunks are 2,600 units across, but a galaxy is drawn on the
      // 260,000-unit intergalactic lattice with a radius of ~50,000. A
      // "galaxy" region rolled at chunk scale is a hundred times smaller
      // than the thing it claims to be and lands nowhere near any drawn
      // galaxy - and because every galaxy region carries a central
      // singularity, that is exactly what was scattering black holes above
      // and below the visible disc. Galaxies come from the lattice
      // (galaxiesNear) and nowhere else.
      kind = roll < 0.64 ? 'nebula' : 'star-system';
    } else if (field > 0.34) {
      kind = roll < 0.58 ? 'star-system' : roll < 0.76 ? 'nebula' : 'planet';
    } else {
      kind = roll < 0.50 ? 'star-system' : roll < 0.70 ? 'planet'
        : roll < 0.86 ? 'ocean' : 'terrain';
    }

    const made = makeRegion(kind, pos, rng, cx, cy, cz, i);
    regions.push(made);

  }

  // ---- galactic cores ----
  //
  // A supermassive singularity sits at the exact centre of a DRAWN galaxy,
  // and the drawn galaxies come from the intergalactic lattice. So the core
  // is emitted by whichever chunk happens to contain that lattice centre,
  // rather than being rolled per chunk. That is what makes the hole line up
  // with the bright core you can actually see, instead of floating in empty
  // space above or below the disc.
  for (const g of latticeGalaxiesInChunk(origin, chunkSize)) {
    // Pinned to the galactic plane, exactly as in the authored core: the
    // singularity is what the disc orbits, so it defines y = 0 rather than
    // drifting relative to it.
    const core = makeRegion('blackhole', new Vector3(g.x, GALACTIC_PLANE_Y, g.z),
      streamFrom(g.seed >>> 0), cx, cy, cz, 5);
    core.name = 'Galaxy ' + g.ix + '.' + g.iy + '.' + g.iz + ' Core';
    // Scaled to the galaxy it anchors, so a big galaxy has a big core.
    core.mass = 60000 + (g.radius / 50000) * 90000;
    core.galacticCore = true;
    regions.push(core);
  }

  return { key: chunkKey(cx, cy, cz), coord: { cx, cy, cz }, regions };
}

/** Region parameters by kind, matching the hand-built universe's scales. */
function makeRegion(
  kind: RegionKind, position: Vector3, rng: () => number,
  cx: number, cy: number, cz: number, i: number
): Region {
  const id = 'c' + cx + '_' + cy + '_' + cz + '_' + i;
  const name = nameFor(kind, cx, cy, cz, i);

  switch (kind) {
    case 'galaxy':
      return { id, kind, name, glyph: '🌌', position,
        radius: 2200 + rng() * 900, mass: 70000 + rng() * 45000,
        seed: (hashChunk(cx, cy, cz, i * 31 + 7) % 100000) };
    case 'nebula':
      return { id, kind, name, glyph: '☁', position,
        radius: 620 + rng() * 460, mass: 900 + rng() * 1400,
        seed: (hashChunk(cx, cy, cz, i * 37 + 11) % 100000) };
    case 'blackhole':
      return { id, kind, name, glyph: '🕳', position,
        radius: 190 + rng() * 260, mass: 6000 + rng() * 14000,
        seed: (hashChunk(cx, cy, cz, i * 41 + 3) % 100000) };
    case 'star-system':
      return { id, kind, name, glyph: '☀', position,
        radius: 300 + rng() * 240, mass: 2600 + rng() * 3400,
        seed: (hashChunk(cx, cy, cz, i * 43 + 5) % 100000) };
    case 'ocean':
      return { id, kind, name, glyph: '🌊', position,
        radius: 62 + rng() * 40, mass: 240 + rng() * 220,
        surfaceRadius: 30 + rng() * 16,
        seed: (hashChunk(cx, cy, cz, i * 47 + 17) % 100000) };
    case 'terrain':
      return { id, kind, name, glyph: '⛰', position,
        radius: 62 + rng() * 40, mass: 240 + rng() * 220,
        surfaceRadius: 30 + rng() * 16,
        seed: (hashChunk(cx, cy, cz, i * 53 + 19) % 100000) };
    default:
      return { id, kind: 'planet', name, glyph: '🪐', position,
        radius: 54 + rng() * 44, mass: 200 + rng() * 260,
        surfaceRadius: 26 + rng() * 18,
        seed: (hashChunk(cx, cy, cz, i * 59 + 23) % 100000) };
  }
}

/* -------------------------------------------------------------------------- */
/*  Streaming                                                                  */
/* -------------------------------------------------------------------------- */

/**
 * Keeps the chunks near the viewer resident and forgets the rest.
 *
 * Because generation is pure, forgetting a chunk costs nothing: flying back
 * rebuilds it identically. Memory stays flat no matter how far you travel.
 */
export class ChunkStreamer {
  opts: ChunkedOptions;
  private chunks = new Map<string, Chunk>();
  private lastCoord: ChunkCoord = { cx: NaN, cy: NaN, cz: NaN };
  /** Chunks generated since the streamer was created. */
  generated = 0;

  constructor(opts: Partial<ChunkedOptions> = {}) {
    this.opts = { ...DEFAULT_CHUNKED, ...opts };
  }

  /** Chunks currently in memory. */
  get residentCount(): number { return this.chunks.size; }

  /** Every region currently loaded. */
  regions(): Region[] {
    const out: Region[] = [];
    for (const c of this.chunks.values()) {
      for (const r of c.regions) out.push(r);
    }
    return out;
  }

  /**
   * Loads what is near `eye` and drops what is not.
   * Returns true when the resident set changed.
   */
  update(eye: Vector3): boolean {
    const c = chunkAt(eye, this.opts.chunkSize);
    if (c.cx === this.lastCoord.cx && c.cy === this.lastCoord.cy &&
        c.cz === this.lastCoord.cz) {
      return false;
    }
    this.lastCoord = c;

    const v = this.opts.viewChunks;
    const wanted = new Set<string>();
    for (let dx = -v; dx <= v; dx++) {
      for (let dy = -v; dy <= v; dy++) {
        for (let dz = -v; dz <= v; dz++) {
          // Spherical rather than cubic, so the loaded volume has no corners
          // that pop into view sooner than the faces.
          if (dx * dx + dy * dy + dz * dz > v * v + v) continue;
          const key = chunkKey(c.cx + dx, c.cy + dy, c.cz + dz);
          wanted.add(key);
          if (!this.chunks.has(key)) {
            this.chunks.set(key,
              generateChunk(c.cx + dx, c.cy + dy, c.cz + dz, this.opts));
            this.generated++;
          }
        }
      }
    }

    // Map iterators remain valid while deleting the current entry; avoid
    // materialising a temporary key array on every cell boundary crossing.
    for (const key of this.chunks.keys()) {
      if (!wanted.has(key)) this.chunks.delete(key);
    }
    return true;
  }

  /** Forces a specific chunk to exist, for tests and for teleporting. */
  ensure(cx: number, cy: number, cz: number): Chunk {
    const key = chunkKey(cx, cy, cz);
    let c = this.chunks.get(key);
    if (!c) {
      c = generateChunk(cx, cy, cz, this.opts);
      this.chunks.set(key, c);
      this.generated++;
    }
    return c;
  }

  clear(): void {
    this.chunks.clear();
    this.lastCoord.cx = NaN;
    this.lastCoord.cy = NaN;
    this.lastCoord.cz = NaN;
  }

  stats(): Record<string, string> {
    return {
      'Chunks resident': String(this.chunks.size),
      'Regions loaded': String(this.regions().length),
      'Chunks generated': String(this.generated)
    };
  }
}
