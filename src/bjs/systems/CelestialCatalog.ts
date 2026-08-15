/**
 * CelestialCatalog — what else is out there besides stars and holes.
 *
 * The universe had four things in it: star systems, black holes, nebulae
 * and galaxies. That is why deep space read as empty even when it was
 * technically populated - four archetypes over an infinite volume means
 * every region looks like every other region.
 *
 * This adds a catalog of further bodies. It is deliberately a DATA TABLE
 * plus pure derivation functions rather than a pile of special cases: each
 * archetype declares how big it is, how bright, what colour, how rare, and
 * what it does, and one generator turns a seed into a populated field. Any
 * new object is a row in the table, not a new code path.
 *
 * Every value here is derived from a seed. Nothing is placed by hand and
 * nothing is stored, which is what lets an unbounded universe have this
 * much in it without a database behind it.
 */

/** The archetypes. */
export type CelestialKind =
  | 'pulsar'          // rotating neutron star, sweeping beams
  | 'quasar'          // hyperluminous active nucleus
  | 'magnetar'        // extreme magnetic field, violent flares
  | 'comet'           // icy body with an ion + dust tail
  | 'meteor-swarm'    // a drifting shoal of rock
  | 'binary-star'     // two suns about a common centre
  | 'red-giant'       // bloated late-stage star
  | 'white-dwarf'     // dense stellar remnant
  | 'brown-dwarf'     // failed star, barely lit
  | 'protostar'       // still collapsing, wrapped in its own cloud
  | 'supernova'       // expanding shell of a dead star
  | 'planetary-nebula'// shed envelope, bright and symmetric
  | 'globular-cluster'// ancient dense ball of stars
  | 'open-cluster'    // loose young association
  | 'rogue-planet'    // unbound, starless
  | 'ice-giant'       // cold outer-system giant
  | 'gas-giant'       // banded, with a ring system
  | 'asteroid-field'  // scattered rock belt
  | 'crystal-field'   // refractive shards, an oddity
  | 'derelict'        // an abandoned structure
  | 'wormhole-mouth'  // one end of a tunnel
  | 'dyson-swarm';    // a star being harvested

export interface CelestialSpec {
  kind: CelestialKind;
  /** Shown in the navigator. */
  label: string;
  /** Single glyph for the HUD and lists. */
  glyph: string;
  /**
   * Relative frequency. These are weights, not probabilities: the
   * generator normalises them, so adding a row cannot silently break the
   * distribution of everything else.
   */
  weight: number;
  /** Body radius range, world units. */
  minRadius: number;
  maxRadius: number;
  /** Emitted light, 0 = dark body, 1 = blinding. */
  luminosity: number;
  /** Base colour as linear RGB 0..1. */
  tint: [number, number, number];
  /** Mass in the same arbitrary units the rest of the universe uses. */
  mass: number;
  /** True if it should contribute to the gravity field meaningfully. */
  massive: boolean;
  /** One-line description shown when you approach. */
  blurb: string;
}

/**
 * The table.
 *
 * Weights are tuned so the sky is mostly ordinary - clusters, dwarfs,
 * asteroid fields - with the spectacular objects rare enough that finding
 * one is an event. A quasar you meet every ten minutes is scenery; one you
 * meet twice a session is a landmark.
 */
export const CELESTIALS: Record<CelestialKind, CelestialSpec> = {
  'pulsar': {
    kind: 'pulsar', label: 'Pulsar', glyph: '⊙', weight: 22,
    minRadius: 6, maxRadius: 14, luminosity: 0.85,
    tint: [0.70, 0.86, 1.00], mass: 2600, massive: true,
    blurb: 'A neutron star sweeping the dark with a beam it cannot switch off.'
  },
  'quasar': {
    kind: 'quasar', label: 'Quasar', glyph: '✦', weight: 3,
    minRadius: 40, maxRadius: 120, luminosity: 1.0,
    tint: [1.00, 0.92, 0.70], mass: 90000, massive: true,
    blurb: 'An active nucleus outshining the galaxy that hosts it.'
  },
  'magnetar': {
    kind: 'magnetar', label: 'Magnetar', glyph: '⚡', weight: 7,
    minRadius: 5, maxRadius: 11, luminosity: 0.78,
    tint: [0.85, 0.72, 1.00], mass: 3100, massive: true,
    blurb: 'A magnetic field strong enough to rearrange atoms at a distance.'
  },
  'comet': {
    kind: 'comet', label: 'Comet', glyph: '☄', weight: 46,
    minRadius: 2, maxRadius: 7, luminosity: 0.30,
    tint: [0.72, 0.92, 0.96], mass: 8, massive: false,
    blurb: 'Ice and dust, streaming away from whatever sun it last passed.'
  },
  'meteor-swarm': {
    kind: 'meteor-swarm', label: 'Meteor Swarm', glyph: '⁂', weight: 52,
    minRadius: 60, maxRadius: 220, luminosity: 0.10,
    tint: [0.62, 0.58, 0.54], mass: 40, massive: false,
    blurb: 'A shoal of rock on a shared orbit, still travelling together.'
  },
  'binary-star': {
    kind: 'binary-star', label: 'Binary Star', glyph: '❨❩', weight: 30,
    minRadius: 40, maxRadius: 95, luminosity: 0.92,
    tint: [1.00, 0.88, 0.66], mass: 4200, massive: true,
    blurb: 'Two suns locked around a point that belongs to neither.'
  },
  'red-giant': {
    kind: 'red-giant', label: 'Red Giant', glyph: '◍', weight: 26,
    minRadius: 120, maxRadius: 320, luminosity: 0.70,
    tint: [1.00, 0.44, 0.24], mass: 2800, massive: true,
    blurb: 'A star in its long, bloated decline, shedding its own outer layers.'
  },
  'white-dwarf': {
    kind: 'white-dwarf', label: 'White Dwarf', glyph: '•', weight: 34,
    minRadius: 4, maxRadius: 10, luminosity: 0.66,
    tint: [0.92, 0.96, 1.00], mass: 1900, massive: true,
    blurb: 'What is left when a star stops arguing with gravity.'
  },
  'brown-dwarf': {
    kind: 'brown-dwarf', label: 'Brown Dwarf', glyph: '◑', weight: 30,
    minRadius: 22, maxRadius: 60, luminosity: 0.12,
    tint: [0.58, 0.30, 0.22], mass: 700, massive: true,
    blurb: 'Too large to be a planet, too small to ever have ignited.'
  },
  'protostar': {
    kind: 'protostar', label: 'Protostar', glyph: '◌', weight: 16,
    minRadius: 70, maxRadius: 180, luminosity: 0.48,
    tint: [1.00, 0.66, 0.42], mass: 1500, massive: true,
    blurb: 'Still falling inward. It has not decided what it will be yet.'
  },
  'supernova': {
    kind: 'supernova', label: 'Supernova Remnant', glyph: '✺', weight: 9,
    minRadius: 260, maxRadius: 900, luminosity: 0.62,
    tint: [0.96, 0.40, 0.58], mass: 300, massive: false,
    blurb: 'An expanding shell, still moving outward from an old catastrophe.'
  },
  'planetary-nebula': {
    kind: 'planetary-nebula', label: 'Planetary Nebula', glyph: '◎', weight: 14,
    minRadius: 150, maxRadius: 480, luminosity: 0.52,
    tint: [0.40, 0.94, 0.82], mass: 120, massive: false,
    blurb: 'A shed envelope, lit from inside by the remnant that shed it.'
  },
  'globular-cluster': {
    kind: 'globular-cluster', label: 'Globular Cluster', glyph: '❋', weight: 12,
    minRadius: 320, maxRadius: 1100, luminosity: 0.58,
    tint: [1.00, 0.86, 0.62], mass: 26000, massive: true,
    blurb: 'A hundred thousand ancient stars, bound since before the disc formed.'
  },
  'open-cluster': {
    kind: 'open-cluster', label: 'Open Cluster', glyph: '⁘', weight: 24,
    minRadius: 200, maxRadius: 620, luminosity: 0.45,
    tint: [0.72, 0.84, 1.00], mass: 3400, massive: true,
    blurb: 'Young, bright, and already drifting apart.'
  },
  'rogue-planet': {
    kind: 'rogue-planet', label: 'Rogue Planet', glyph: '☾', weight: 28,
    minRadius: 18, maxRadius: 55, luminosity: 0.02,
    tint: [0.20, 0.22, 0.30], mass: 260, massive: true,
    blurb: 'Thrown out of whatever system made it. No sun, no year, no seasons.'
  },
  'ice-giant': {
    kind: 'ice-giant', label: 'Ice Giant', glyph: '❄', weight: 30,
    minRadius: 40, maxRadius: 95, luminosity: 0.06,
    tint: [0.44, 0.72, 0.94], mass: 620, massive: true,
    blurb: 'Methane blue, banded, and colder than anything has a right to be.'
  },
  'gas-giant': {
    kind: 'gas-giant', label: 'Gas Giant', glyph: '◕', weight: 34,
    minRadius: 60, maxRadius: 160, luminosity: 0.08,
    tint: [0.86, 0.70, 0.46], mass: 1400, massive: true,
    blurb: 'Storm bands wider than most worlds, and a ring system to match.'
  },
  'asteroid-field': {
    kind: 'asteroid-field', label: 'Asteroid Field', glyph: '⋰', weight: 44,
    minRadius: 180, maxRadius: 700, luminosity: 0.05,
    tint: [0.54, 0.50, 0.46], mass: 90, massive: false,
    blurb: 'The rubble of a world that never finished assembling.'
  },
  'crystal-field': {
    kind: 'crystal-field', label: 'Crystal Field', glyph: '◈', weight: 6,
    minRadius: 120, maxRadius: 420, luminosity: 0.40,
    tint: [0.66, 0.96, 1.00], mass: 60, massive: false,
    blurb: 'Refractive shards, kilometres long, all facing the same way.'
  },
  'derelict': {
    kind: 'derelict', label: 'Derelict', glyph: '⌗', weight: 10,
    minRadius: 25, maxRadius: 140, luminosity: 0.14,
    tint: [0.60, 0.66, 0.70], mass: 40, massive: false,
    blurb: 'Someone built this. Nothing aboard has moved in a very long time.'
  },
  'wormhole-mouth': {
    kind: 'wormhole-mouth', label: 'Wormhole Mouth', glyph: '◉', weight: 4,
    minRadius: 30, maxRadius: 80, luminosity: 0.55,
    tint: [0.72, 0.46, 1.00], mass: 1800, massive: true,
    blurb: 'A hole in the geometry. The far side is somewhere else entirely.'
  },
  'dyson-swarm': {
    kind: 'dyson-swarm', label: 'Dyson Swarm', glyph: '⌾', weight: 2,
    minRadius: 200, maxRadius: 560, luminosity: 0.74,
    tint: [1.00, 0.78, 0.36], mass: 5200, massive: true,
    blurb: 'A star most of the way through being taken apart on purpose.'
  }
};

export const CELESTIAL_KINDS: CelestialKind[] =
  Object.keys(CELESTIALS) as CelestialKind[];

/** Total weight, computed once. */
export const TOTAL_WEIGHT: number =
  CELESTIAL_KINDS.reduce((s, k) => s + CELESTIALS[k].weight, 0);

/** Deterministic integer hash. Same inputs, same universe, forever. */
export function hashCelestial(x: number, y: number, z: number, salt = 0): number {
  let h = (x | 0) * 2654435761 + (y | 0) * 2246822519
    + (z | 0) * 3266489917 + (salt | 0) * 668265263;
  h = (h ^ (h >>> 15)) >>> 0;
  h = Math.imul(h, 2246822519) >>> 0;
  return (h ^ (h >>> 13)) >>> 0;
}

/** A 0..1 channel from a hash. */
export function channel(h: number, i: number): number {
  let v = (h + Math.imul(i + 1, 2654435761)) >>> 0;
  v = Math.imul(v ^ (v >>> 15), 2246822519) >>> 0;
  v = (v ^ (v >>> 13)) >>> 0;
  return v / 4294967296;
}

/**
 * Picks an archetype from a 0..1 roll, respecting the weights.
 *
 * Walks the table in declaration order so the mapping from roll to kind is
 * stable: adding a row at the end cannot change what an existing seed
 * produces for any roll below the new row's slice.
 */
export function kindForRoll(roll: number): CelestialKind {
  const r = Math.max(0, Math.min(0.999999, Number.isFinite(roll) ? roll : 0))
    * TOTAL_WEIGHT;
  let acc = 0;
  for (const k of CELESTIAL_KINDS) {
    acc += CELESTIALS[k].weight;
    if (r < acc) return k;
  }
  return CELESTIAL_KINDS[CELESTIAL_KINDS.length - 1];
}

export interface CelestialBody {
  kind: CelestialKind;
  spec: CelestialSpec;
  id: string;
  x: number; y: number; z: number;
  radius: number;
  mass: number;
  seed: number;
  /** Per-body colour, varied from the archetype's base tint. */
  tint: [number, number, number];
  /** Per-body brightness multiplier. */
  brightness: number;
  /** Rotation rate, radians/sec. Zero for fields that do not spin. */
  spin: number;
}

export interface FieldOptions {
  /** Edge length of one placement cell, world units. */
  cellSize: number;
  /** Expected bodies per cell. */
  density: number;
  /** Universe seed, so a reseed changes everything. */
  seed: number;
}

export const DEFAULT_FIELD: FieldOptions = {
  cellSize: 9000,
  // A touch denser than before: enough that a cell is rarely empty and
  // rarely crowded, and so the space between the home worlds carries a
  // steady traffic of pulsars, comets, clusters and nebulae rather than
  // the occasional lonely spark.
  density: 2.3,
  seed: 1
};

/**
 * Every body in one cell of the placement lattice.
 *
 * Pure: identical inputs give identical output on any machine, in any
 * session, forever. Nothing is stored - the universe is recomputed from
 * integer coordinates whenever it is needed.
 */
export function bodiesInCell(
  cx: number, cy: number, cz: number, o: FieldOptions = DEFAULT_FIELD
): CelestialBody[] {
  const out: CelestialBody[] = [];
  const h = hashCelestial(cx, cy, cz, o.seed);
  // Poisson-ish count from the density, without a factorial.
  const n = Math.floor(o.density + channel(h, 0) * 1.4);
  for (let i = 0; i < n; i++) {
    const bh = hashCelestial(cx, cy, cz, o.seed + 7919 * (i + 1));
    const spec = CELESTIALS[kindForRoll(channel(bh, 1))];
    // Jitter inside the cell, kept off the walls so neighbouring cells
    // cannot produce two bodies touching across a boundary.
    const jx = 0.14 + channel(bh, 2) * 0.72;
    const jy = 0.14 + channel(bh, 3) * 0.72;
    const jz = 0.14 + channel(bh, 4) * 0.72;
    const t = channel(bh, 5);
    const radius = spec.minRadius + (spec.maxRadius - spec.minRadius) * t;
    // Colour varies per body so two pulsars are not the same pulsar.
    const warp = (channel(bh, 6) - 0.5) * 0.22;
    const tint: [number, number, number] = [
      Math.max(0, Math.min(1, spec.tint[0] * (1 + warp))),
      Math.max(0, Math.min(1, spec.tint[1] * (1 + warp * 0.4))),
      Math.max(0, Math.min(1, spec.tint[2] * (1 - warp)))
    ];
    out.push({
      kind: spec.kind,
      spec,
      id: 'ce-' + cx + '.' + cy + '.' + cz + '.' + i,
      x: (cx + jx) * o.cellSize,
      y: (cy + jy) * o.cellSize,
      z: (cz + jz) * o.cellSize,
      radius,
      // Mass scales with the body's actual size rather than being flat per
      // archetype, so a big red giant really does pull harder than a small
      // one and the gravity field stays consistent with what you can see.
      mass: spec.mass * (0.6 + t * 0.8),
      seed: bh,
      tint,
      brightness: 0.6 + channel(bh, 7) * 0.7,
      spin: spec.kind === 'pulsar' || spec.kind === 'magnetar'
        ? 1.4 + channel(bh, 8) * 5.2
        : channel(bh, 8) * 0.12
    });
  }
  return out;
}

/**
 * Every body within `range` of a point.
 *
 * Walks only the cells the sphere actually touches, so cost scales with
 * the view distance rather than with the size of the universe.
 */
export function bodiesNear(
  px: number, py: number, pz: number, range: number,
  o: FieldOptions = DEFAULT_FIELD
): CelestialBody[] {
  const cs = Math.max(1, o.cellSize);
  const r = Math.max(0, range);
  const lo = (v: number) => Math.floor((v - r) / cs);
  const hi = (v: number) => Math.floor((v + r) / cs);
  const out: CelestialBody[] = [];
  const r2 = r * r;
  for (let cx = lo(px); cx <= hi(px); cx++) {
    for (let cy = lo(py); cy <= hi(py); cy++) {
      for (let cz = lo(pz); cz <= hi(pz); cz++) {
        for (const b of bodiesInCell(cx, cy, cz, o)) {
          const dx = b.x - px, dy = b.y - py, dz = b.z - pz;
          if (dx * dx + dy * dy + dz * dz <= r2) out.push(b);
        }
      }
    }
  }
  return out;
}

/** The nearest body to a point, or null if the sphere is empty. */
export function nearestBody(
  px: number, py: number, pz: number, range: number,
  o: FieldOptions = DEFAULT_FIELD
): CelestialBody | null {
  let best: CelestialBody | null = null;
  let bestD = Infinity;
  for (const b of bodiesNear(px, py, pz, range, o)) {
    const dx = b.x - px, dy = b.y - py, dz = b.z - pz;
    const d = dx * dx + dy * dy + dz * dz;
    if (d < bestD) { bestD = d; best = b; }
  }
  return best;
}
