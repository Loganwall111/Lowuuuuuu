/**
 * DimensionSystem — endless procedural dimensions beyond the event horizon.
 *
 * Every dimension is generated from a single 64-bit-ish integer seed, so the
 * space of dimensions is effectively infinite while remaining perfectly
 * reproducible: the same seed always yields the same world, which is what
 * makes "go back through the same tear" work.
 *
 * A dimension is described by a DimensionSpec — a bundle of palette, physics
 * overrides, geometry rules, fog, and named traits. Traits are what make each
 * one feel authored rather than random: 'inverted-gravity', 'flesh',
 * 'time-reversed', 'cubist', 'upside-down', and so on. Rendering code reads
 * the spec; it never needs to know which dimension it is drawing.
 */

/* ------------------------- deterministic RNG ------------------------- */

/** Mulberry32: small, fast, and fully reproducible from a 32-bit seed. */
export function makeRng(seed: number): () => number {
  let a = seed >>> 0;
  return function () {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = a;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/** Hashes a string into a 32-bit seed, so dimensions can be named. */
export function hashSeed(s: string): number {
  let h = 2166136261 >>> 0;
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i);
    h = Math.imul(h, 16777619) >>> 0;
  }
  return h >>> 0;
}

/* ------------------------------ vocabulary ------------------------------ */

export type DimensionTrait =
  | 'psychedelic' | 'flesh' | 'bloodstream' | 'neural' | 'cubist'
  | 'upside-down' | 'inverted-gravity' | 'time-reversed' | 'liquid'
  | 'crystalline' | 'void' | 'fractal' | 'jellyfish' | 'clockwork'
  | 'paper' | 'neon' | 'bone' | 'fungal' | 'oceanic' | 'molten'
  | 'glitched' | 'monochrome' | 'giant' | 'miniature' | 'mirror'
  | 'library' | 'tesseract' | 'dust' | 'streaming';

export const ALL_TRAITS: DimensionTrait[] = [
  'psychedelic', 'flesh', 'bloodstream', 'neural', 'cubist',
  'upside-down', 'inverted-gravity', 'time-reversed', 'liquid',
  'crystalline', 'void', 'fractal', 'jellyfish', 'clockwork',
  'paper', 'neon', 'bone', 'fungal', 'oceanic', 'molten',
  'glitched', 'monochrome', 'giant', 'miniature', 'mirror',
  'library', 'tesseract', 'dust', 'streaming'
];

/** Themed archetypes. Depth biases which of these can appear. */
interface Archetype {
  id: string;
  name: string;
  glyph: string;
  traits: DimensionTrait[];
  palette: [number, number, number][];
  fogDensity: number;
  ambient: number;
  minDepth: number;
  blurb: string;
  /**
   * Never chosen by an ordinary roll, at any depth.
   *
   * Some places must be arrived at rather than stumbled into. The Library
   * exists behind one specific black hole in the whole universe, and the
   * Dust Stream is only on the far side of a singularity. If the random
   * draw could produce them they would stop being destinations and become
   * scenery, so they are excluded from the pool and reached by name.
   */
  summonedOnly?: boolean;
}

const ARCHETYPES: Archetype[] = [
  {
    id: 'kaleidoscope', name: 'Kaleidoscope', glyph: '🌈',
    traits: ['psychedelic', 'fractal', 'mirror'],
    palette: [[1, 0.2, 0.7], [0.3, 1, 0.9], [1, 0.9, 0.2], [0.6, 0.3, 1]],
    fogDensity: 0.012, ambient: 0.7, minDepth: 0,
    blurb: 'Colour with no object to belong to.'
  },
  {
    id: 'bloodstream', name: 'The Bloodstream', glyph: '🩸',
    traits: ['bloodstream', 'flesh', 'liquid'],
    palette: [[0.7, 0.05, 0.1], [0.95, 0.25, 0.25], [0.35, 0.02, 0.06], [1, 0.5, 0.45]],
    fogDensity: 0.045, ambient: 0.35, minDepth: 1,
    blurb: 'Warm, wet, and moving in one direction.'
  },
  {
    id: 'cortex', name: 'The Cortex', glyph: '🧠',
    traits: ['neural', 'flesh', 'fractal'],
    palette: [[0.95, 0.7, 0.75], [0.6, 0.3, 0.45], [1, 0.85, 0.85], [0.4, 0.15, 0.3]],
    fogDensity: 0.03, ambient: 0.45, minDepth: 2,
    blurb: 'Something in here is thinking, and it is not you.'
  },
  {
    id: 'cubist', name: 'Cubist Reality', glyph: '🧊',
    traits: ['cubist', 'crystalline', 'monochrome'],
    palette: [[0.85, 0.88, 0.95], [0.35, 0.45, 0.6], [1, 1, 1], [0.15, 0.2, 0.3]],
    fogDensity: 0.008, ambient: 0.6, minDepth: 1,
    blurb: 'Every curve has been outlawed. Planets are cubes.'
  },
  {
    id: 'inverted', name: 'The Inverted City', glyph: '🙃',
    traits: ['upside-down', 'inverted-gravity', 'glitched'],
    palette: [[0.2, 0.3, 0.5], [0.8, 0.75, 0.6], [0.1, 0.12, 0.2], [1, 0.65, 0.3]],
    fogDensity: 0.02, ambient: 0.4, minDepth: 2,
    blurb: 'The buildings hang down. So do you.'
  },
  {
    id: 'jellyfish', name: 'The Jellyfish Expanse', glyph: '🎐',
    traits: ['jellyfish', 'oceanic', 'liquid', 'neon'],
    palette: [[0.35, 0.8, 0.95], [0.75, 0.4, 1], [0.2, 0.95, 0.8], [1, 0.6, 0.9]],
    fogDensity: 0.035, ambient: 0.5, minDepth: 1,
    blurb: 'Everything that exists here is an alien jellyfish.'
  },
  {
    id: 'clockwork', name: 'The Clockwork', glyph: '⚙',
    traits: ['clockwork', 'crystalline', 'time-reversed'],
    palette: [[0.8, 0.65, 0.3], [0.45, 0.35, 0.15], [1, 0.9, 0.6], [0.2, 0.15, 0.08]],
    fogDensity: 0.015, ambient: 0.45, minDepth: 3,
    blurb: 'Time is a machine here, and it is running backwards.'
  },
  {
    id: 'bonefield', name: 'The Bone Field', glyph: '🦴',
    traits: ['bone', 'void', 'monochrome'],
    palette: [[0.9, 0.87, 0.78], [0.5, 0.47, 0.4], [1, 0.98, 0.92], [0.15, 0.14, 0.12]],
    fogDensity: 0.025, ambient: 0.3, minDepth: 3,
    blurb: 'Something enormous died here. Possibly a universe.'
  },
  {
    id: 'fungal', name: 'The Fungal Deep', glyph: '🍄',
    traits: ['fungal', 'neon', 'liquid'],
    palette: [[0.3, 1, 0.45], [0.7, 0.25, 0.9], [0.95, 0.9, 0.3], [0.1, 0.3, 0.2]],
    fogDensity: 0.04, ambient: 0.35, minDepth: 2,
    blurb: 'It is all one organism, and it noticed you.'
  },
  {
    id: 'molten', name: 'The Forge', glyph: '🌋',
    traits: ['molten', 'giant'],
    palette: [[1, 0.35, 0.05], [1, 0.75, 0.2], [0.35, 0.05, 0], [1, 0.95, 0.7]],
    fogDensity: 0.05, ambient: 0.55, minDepth: 2,
    blurb: 'Where matter is poured before it is given a shape.'
  },
  {
    id: 'papercraft', name: 'Papercraft', glyph: '📄',
    traits: ['paper', 'cubist', 'miniature'],
    palette: [[1, 0.95, 0.85], [0.9, 0.6, 0.4], [0.4, 0.7, 0.9], [0.25, 0.22, 0.2]],
    fogDensity: 0.006, ambient: 0.8, minDepth: 1,
    blurb: 'Reality, but folded. Do not get it wet.'
  },
  {
    id: 'voidgap', name: 'The Gap', glyph: '⬛',
    traits: ['void', 'monochrome', 'glitched'],
    palette: [[0.06, 0.06, 0.09], [0.25, 0.25, 0.35], [0.5, 0.5, 0.7], [0.02, 0.02, 0.03]],
    fogDensity: 0.002, ambient: 0.12, minDepth: 4,
    blurb: 'The space between dimensions. Nothing was built here.'
  },
  {
    id: 'primordial', name: 'The Primordial', glyph: '🌀',
    traits: ['time-reversed', 'liquid', 'fractal', 'giant'],
    palette: [[0.15, 0.35, 0.55], [0.6, 0.85, 0.9], [0.05, 0.1, 0.2], [0.9, 0.95, 1]],
    fogDensity: 0.03, ambient: 0.4, minDepth: 5,
    blurb: 'You have gone deep enough to arrive before things began.'
  },

  /* --------------------- summoned-only destinations --------------------- */

  {
    // Behind the one Gargantua-class hole in the universe. You arrive in
    // darkness and fall for a while before the shelves resolve around you.
    id: 'library', name: 'The Library Realm', glyph: '📚',
    traits: ['library', 'tesseract', 'cubist', 'time-reversed'],
    palette: [[0.82, 0.66, 0.38], [0.16, 0.13, 0.10], [1, 0.88, 0.62], [0.04, 0.035, 0.03]],
    fogDensity: 0.018, ambient: 0.22, minDepth: 0,
    blurb: 'Every moment is a shelf. You are behind all of them at once.',
    summonedOnly: true
  },
  {
    // Through the singularity itself, rather than merely past the horizon.
    id: 'duststream', name: 'The Dust Stream', glyph: '✨',
    traits: ['dust', 'streaming', 'void', 'giant'],
    palette: [[0.95, 0.86, 0.72], [0.55, 0.45, 0.35], [1, 0.96, 0.9], [0.03, 0.03, 0.04]],
    fogDensity: 0.055, ambient: 0.5, minDepth: 0,
    blurb: 'Matter on its way to somewhere else, and now so are you.',
    summonedOnly: true
  }
];

/**
 * Realms that exist but are never randomly rolled. Reached with
 * `namedDimension`, which is what the black hole interior calls when you
 * pass through the singularity or fall into the Gargantua hole.
 */
export const SUMMONED_IDS = ARCHETYPES.filter((a) => a.summonedOnly).map((a) => a.id);

/* ------------------------------- the spec ------------------------------- */

export interface DimensionSpec {
  seed: number;
  depth: number;
  id: string;
  name: string;
  glyph: string;
  blurb: string;
  traits: DimensionTrait[];
  /** rgb 0..1 */
  palette: [number, number, number][];
  fogColor: [number, number, number];
  fogDensity: number;
  ambient: number;
  /** Physics overrides applied while inside. */
  gravity: number;
  timeScale: number;
  /** Negative means time runs backwards. */
  timeDirection: 1 | -1;
  /** Shape vocabulary the geometry builder should draw from. */
  shapes: string[];
  objectCount: number;
  objectScale: number;
  /** How psychedelic the post-processing should get, 0..1. */
  weirdness: number;
  /** Era offset in years; negative is the past. */
  timeEra: number;
}

const SHAPE_SETS: Record<string, string[]> = {
  psychedelic: ['torus', 'sphere', 'torusknot', 'icosphere'],
  flesh: ['blob', 'capsule', 'sphere', 'tube'],
  bloodstream: ['capsule', 'blob', 'disc', 'tube'],
  neural: ['tube', 'icosphere', 'blob'],
  cubist: ['box', 'box', 'box', 'octahedron'],
  'upside-down': ['box', 'cylinder', 'plane'],
  jellyfish: ['dome', 'capsule', 'tube', 'sphere'],
  clockwork: ['cylinder', 'torus', 'gear', 'box'],
  bone: ['capsule', 'cylinder', 'blob'],
  fungal: ['dome', 'cylinder', 'sphere', 'blob'],
  molten: ['blob', 'sphere', 'icosphere'],
  paper: ['plane', 'box', 'triangle'],
  crystalline: ['octahedron', 'icosphere', 'box'],
  void: ['icosphere', 'plane'],
  fractal: ['icosphere', 'torusknot', 'octahedron'],
  liquid: ['sphere', 'blob', 'disc'],
  library: ['box', 'box', 'plane', 'shelf'],
  tesseract: ['box', 'plane', 'octahedron'],
  dust: ['grain', 'grain', 'grain', 'icosphere'],
  streaming: ['grain', 'disc', 'blob'],
  default: ['sphere', 'box', 'torus', 'capsule']
};

function pick<T>(rng: () => number, arr: T[]): T {
  return arr[Math.floor(rng() * arr.length) % arr.length];
}

/**
 * Builds a dimension from a seed and a depth.
 * Depth increases as the player falls further through nested black holes; it
 * unlocks stranger archetypes and eventually pushes time backwards.
 */
export function generateDimension(seed: number, depth = 0): DimensionSpec {
  const rng = makeRng(seed);
  const d = Math.max(0, Math.floor(depth));

  // deeper falls unlock stranger places, but shallow ones stay possible.
  // Summoned-only realms are excluded: they are destinations, not scenery.
  const eligible = ARCHETYPES.filter((a) => a.minDepth <= d && !a.summonedOnly);
  const pool = eligible.length ? eligible : [ARCHETYPES[0]];
  const arch = pool[Math.floor(rng() * pool.length) % pool.length];
  return realise(arch, rng, seed, d);
}

/**
 * Builds one specific realm by archetype id, bypassing the random draw.
 *
 * This is how the Library and the Dust Stream are reached: the caller knows
 * exactly where the player is going, but the realm is still generated from a
 * seed so its palette and contents vary between visits to different holes.
 * Falls back to a normal roll if the id is unknown, so a bad string can
 * never strand the player in an empty world.
 */
export function namedDimension(id: string, seed: number, depth = 0): DimensionSpec {
  const arch = ARCHETYPES.find((a) => a.id === id);
  if (!arch) return generateDimension(seed, depth);
  const rng = makeRng(seed);
  return realise(arch, rng, seed, Math.max(0, Math.floor(depth)));
}

/** Turns a chosen archetype into a full spec. Shared by both entry points. */
function realise(
  arch: Archetype, rng: () => number, seed: number, d: number
): DimensionSpec {
  // every dimension mixes its archetype traits with one wildcard
  const traits: DimensionTrait[] = [...arch.traits];
  const wild = pick(rng, ALL_TRAITS);
  if (!traits.includes(wild)) traits.push(wild);

  // deep dimensions start running backwards through time
  const reversed = traits.includes('time-reversed') || (d >= 6 && rng() < 0.55);
  if (reversed && !traits.includes('time-reversed')) traits.push('time-reversed');

  const inverted = traits.includes('inverted-gravity');

  // palette: archetype base, shifted by the seed so no two are identical
  const shift = rng() * 0.28 - 0.14;
  const palette = arch.palette.map(([r, g, b]) => {
    const j = (v: number) => Math.max(0, Math.min(1, v + shift * (rng() - 0.3)));
    return [j(r), j(g), j(b)] as [number, number, number];
  });

  const shapeKey = traits.find((t) => SHAPE_SETS[t]) ?? 'default';
  const shapes = SHAPE_SETS[shapeKey] ?? SHAPE_SETS.default;

  const gravityBase = 0.25 + rng() * 2.4;
  const weirdness = Math.max(0, Math.min(1,
    0.25 + d * 0.09 + (traits.includes('psychedelic') ? 0.4 : 0) + rng() * 0.2));

  return {
    seed: seed >>> 0,
    depth: d,
    id: arch.id + '-' + (seed >>> 0).toString(36),
    name: arch.name,
    glyph: arch.glyph,
    blurb: arch.blurb,
    traits,
    palette,
    fogColor: palette[palette.length - 1],
    fogDensity: arch.fogDensity * (0.6 + rng() * 0.9),
    ambient: arch.ambient,
    gravity: inverted ? -gravityBase : gravityBase,
    timeScale: 0.35 + rng() * 1.9,
    timeDirection: reversed ? -1 : 1,
    shapes,
    objectCount: Math.floor(40 + rng() * 150 + d * 12),
    objectScale: traits.includes('giant') ? 3.2 + rng() * 4
               : traits.includes('miniature') ? 0.2 + rng() * 0.3
               : 0.6 + rng() * 1.8,
    weirdness,
    // going deeper walks you backwards through history
    timeEra: reversed ? -Math.floor(Math.pow(d, 2.1) * 120 + rng() * 400) : 0
  };
}

/**
 * The chain of dimensions reached by falling through successive black holes.
 * Deterministic: the same origin seed always produces the same journey, so a
 * player can retrace their steps.
 */
export function dimensionChain(originSeed: number, depth: number): DimensionSpec[] {
  const out: DimensionSpec[] = [];
  let s = originSeed >>> 0;
  for (let i = 0; i <= depth; i++) {
    out.push(generateDimension(s, i));
    // derive the next seed from the current one
    s = (Math.imul(s ^ (i + 1), 2246822519) + 3266489917) >>> 0;
  }
  return out;
}

/** The seed reached by descending one more level from a given dimension. */
export function descend(spec: DimensionSpec): DimensionSpec {
  const next = (Math.imul(spec.seed ^ (spec.depth + 1), 2246822519) + 3266489917) >>> 0;
  return generateDimension(next, spec.depth + 1);
}

/** A sideways tear: same depth, different reality. */
export function tearSideways(spec: DimensionSpec): DimensionSpec {
  const next = (Math.imul(spec.seed ^ 0x9e3779b9, 2654435761) + 1013904223) >>> 0;
  return generateDimension(next, spec.depth);
}

/** Human-readable summary for the UI. */
export function describeDimension(s: DimensionSpec): Record<string, string> {
  return {
    'Dimension': s.glyph + ' ' + s.name,
    'Seed': (s.seed >>> 0).toString(36).toUpperCase(),
    'Depth': String(s.depth),
    'Traits': s.traits.join(', '),
    'Gravity': (s.gravity < 0 ? 'inverted ' : '') + Math.abs(s.gravity).toFixed(2) + '×',
    'Time': s.timeDirection < 0
      ? 'running backwards (' + s.timeScale.toFixed(2) + '×)'
      : 'forwards (' + s.timeScale.toFixed(2) + '×)',
    'Era': s.timeEra < 0 ? Math.abs(s.timeEra) + ' years before now' : 'the present',
    'Weirdness': Math.round(s.weirdness * 100) + '%'
  };
}
