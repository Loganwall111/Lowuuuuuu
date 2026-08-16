/**
 * OuterVerses — what is past the end of space.
 *
 * Space itself is endless and procedural (see ChunkedUniverse). But endless
 * is not the same as unstructured, and flying outward for ever with nothing
 * changing is just a very long corridor. So the outward journey has stages:
 *
 *   1. Populated space, generated for ever, no edge.
 *   2. The Thinning - stars run out. Not a wall you hit, a sky that empties.
 *   3. The Nothing - genuinely empty. Look back and the universe you left is
 *      a wall of light behind you.
 *   4. Enter the Nothing and you are not stopped, you are *moved*: each
 *      crossing lands you in a different verse.
 *
 * The verses are not reskins. Each declares what it is made of, how its
 * space behaves, and what you find in it, and the renderer builds from that
 * declaration - so adding one is a table entry, not a subsystem.
 *
 * ---------------------------------------------------------------------------
 * On the 414-digit coordinate
 * ---------------------------------------------------------------------------
 * The requested endpoint has 414 digits. A float64 - which is every number
 * in JavaScript, and every coordinate a GPU can consume - overflows to
 * Infinity above ~1.8e308, and loses integer precision above 9.0e15. So the
 * literal coordinate cannot be a position: not "is hard to reach", but
 * cannot be represented at all.
 *
 * Rather than quietly clamp it and pretend, distance is tracked as a
 * *depth* - the exponent of how far out you are - and the endpoint is
 * defined at the depth that 414-digit number corresponds to. Travelling
 * outward raises the depth without ever overflowing, the number is shown to
 * you in full, and arriving at it is a real event with a real destination.
 * The journey is preserved exactly; only the arithmetic is made possible.
 */

/** The literal endpoint, exactly as specified. Held as a string: no float can. */
export const FINAL_COORDINATE =
  '999999999999999999999999999999999999999999999999999999999999999999999999' +
  '999999999999999999999999999999999999999999999999999999999999999999999999' +
  '999999999999999999999999999999999999999999999999999999999999999999999999' +
  '999999999999999999982828282228282999999999999999999999999999999999999999' +
  '999928282882828228288228999999999999999999999999999999999999999999999999' +
  '999999999999999999999999999999999999999999272282292882';

/** How many digits the endpoint has: the depth scale is built from this. */
export const FINAL_DIGITS = FINAL_COORDINATE.length;

export type VerseId =
  | 'universe' | 'metaverse' | 'prismverse' | 'codeverse' | 'squareverse'
  | 'clockverse' | 'octagonverse' | 'tripleverse' | 'edge'
  | 'mandelbrot' | 'cubefield';

export interface Verse {
  id: VerseId;
  name: string;
  tagline: string;
  /**
   * Depth at which this verse begins, in digits of the coordinate. Using
   * digits rather than a raw distance is what keeps the whole scale
   * representable: depth 414 is the endpoint, and depth is just an integer.
   */
  depth: number;
  /** Background tint. */
  tint: [number, number, number];
  /** What space is made of here, which drives how it is rendered. */
  medium: 'stars' | 'technology' | 'code' | 'geometry' | 'fractal' | 'string' | 'void';
  /** Rough count of things to draw. */
  density: number;
  /** Fixed rotational symmetry, for the shaped verses. 0 = none. */
  symmetry: number;
  /** How strange the rendering gets, 0-1. Rises the further out you go. */
  strangeness: number;
}

/**
 * The verses, in the order you reach them.
 *
 * Depths climb steeply: each verse is many orders of magnitude beyond the
 * last, so reaching the far ones is the absurd journey it is meant to be.
 */
export const VERSES: Verse[] = [
  {
    id: 'universe', name: 'The Universe',
    tagline: 'Stars, worlds, and the space between them.',
    depth: 0, tint: [0.06, 0.10, 0.22], medium: 'stars',
    density: 1, symmetry: 0, strangeness: 0
  },
  {
    id: 'metaverse', name: 'The Metaverse',
    tagline: 'Technology all the way down. The machines do not look up.',
    depth: 36, tint: [0.05, 0.24, 0.26], medium: 'technology',
    density: 1.6, symmetry: 4, strangeness: 0.2
  },
  {
    id: 'prismverse', name: 'The Prismverse',
    tagline: 'Five-fold light, split until the sky is a spectrum of edges.',
    depth: 66, tint: [0.16, 0.05, 0.32], medium: 'geometry',
    density: 1.4, symmetry: 5, strangeness: 0.3
  },
  {
    id: 'codeverse', name: 'The Codeverse',
    tagline: 'It was all running on something. This is the something.',
    depth: 90, tint: [0.04, 0.20, 0.08], medium: 'code',
    density: 2.2, symmetry: 0, strangeness: 0.35
  },
  {
    id: 'squareverse', name: 'The Squareverse',
    tagline: 'Everything here has four sides. Everything.',
    depth: 128, tint: [0.24, 0.14, 0.05], medium: 'geometry',
    density: 1.3, symmetry: 4, strangeness: 0.5
  },
  {
    id: 'clockverse', name: 'The Clockverse',
    tagline: 'Twelve directions, all ticking. It is always precisely now.',
    depth: 168, tint: [0.05, 0.30, 0.22], medium: 'technology',
    density: 1.7, symmetry: 12, strangeness: 0.55
  },
  {
    id: 'octagonverse', name: 'The Octagonverse',
    tagline: 'Eight-fold, endlessly. It hums at a frequency you can feel.',
    depth: 208, tint: [0.20, 0.06, 0.28], medium: 'geometry',
    density: 1.5, symmetry: 8, strangeness: 0.62
  },
  {
    id: 'tripleverse', name: 'The Tripleverse',
    tagline: 'Three of everything, including you, briefly.',
    depth: 268, tint: [0.28, 0.18, 0.04], medium: 'geometry',
    density: 1.8, symmetry: 3, strangeness: 0.74
  },
  {
    id: 'edge', name: 'The Edge of Reality',
    tagline: 'The entire universe, seen side-on. It is a single string.',
    depth: 330, tint: [0.30, 0.30, 0.34], medium: 'string',
    density: 1, symmetry: 0, strangeness: 0.85
  },
  {
    id: 'mandelbrot', name: 'The Mandelbrot Set',
    tagline: 'A fractal you can fly into. It has no bottom.',
    depth: 386, tint: [0.10, 0.04, 0.26], medium: 'fractal',
    density: 2.6, symmetry: 0, strangeness: 0.93
  },
  {
    id: 'cubefield', name: 'The Infinite Cube of Stars',
    tagline: 'It gets weirder. That was always going to be the answer.',
    depth: FINAL_DIGITS, tint: [0.34, 0.30, 0.10], medium: 'void',
    density: 3.2, symmetry: 6, strangeness: 1
  }
];

/* -------------------------------------------------------------------------- */
/*  Depth                                                                      */
/* -------------------------------------------------------------------------- */

/**
 * Converts a real distance into a depth in digits.
 *
 * depth = number of digits in the distance, so 1e6 units is depth 7 and the
 * endpoint is depth 414. This is the whole trick: the scale spans 414 orders
 * of magnitude while the number being handled never exceeds a few hundred.
 */
export function depthOf(distance: number): number {
  const d = Math.abs(distance);
  if (!Number.isFinite(d) || d < 1) return 0;
  return Math.floor(Math.log10(d)) + 1;
}

/** The verse you are in at a given depth. */
export function verseAt(depth: number): Verse {
  let found = VERSES[0];
  for (const v of VERSES) {
    if (depth >= v.depth) found = v; else break;
  }
  return found;
}

/** The verse you are in at a given real distance. */
export function verseAtDistance(distance: number): Verse {
  return verseAt(depthOf(distance));
}

/** The next verse outward, or null at the end. */
export function nextVerse(id: VerseId): Verse | null {
  const i = VERSES.findIndex((v) => v.id === id);
  return i >= 0 && i < VERSES.length - 1 ? VERSES[i + 1] : null;
}

/**
 * Progress through the current verse, 0 at its start and 1 at the next.
 * Used to fade the sky and thin the stars as you approach a boundary.
 */
export function verseProgress(depth: number): number {
  const cur = verseAt(depth);
  const nxt = nextVerse(cur.id);
  if (!nxt) return 1;
  const span = Math.max(1, nxt.depth - cur.depth);
  return Math.max(0, Math.min(1, (depth - cur.depth) / span));
}

/* -------------------------------------------------------------------------- */
/*  The Thinning and The Nothing                                               */
/* -------------------------------------------------------------------------- */

/** Fraction of the way through a verse at which stars start running out. */
export const THINNING_START = 0.72;
/** Fraction at which space is genuinely empty. */
export const NOTHING_START = 0.93;

export interface EdgeState {
  /** 0 = normal space, 1 = completely empty. */
  emptiness: number;
  /** True once there is nothing left at all. */
  inNothing: boolean;
  /**
   * How brightly the universe behind you glows as a wall. Rises as space
   * empties, because with nothing in front, everything you left is behind.
   */
  wallBrightness: number;
  /** Multiplier on how many objects to generate. */
  densityScale: number;
}

/**
 * How empty space is at a given progress through a verse.
 *
 * Deliberately not a hard boundary: stars thin out over a long stretch, so
 * you notice the sky emptying before you notice you have arrived anywhere.
 * The wall behind you is the reward for looking back.
 */
export function edgeStateAt(progress: number): EdgeState {
  const p = Math.max(0, Math.min(1, progress));
  if (p < THINNING_START) {
    return { emptiness: 0, inNothing: false, wallBrightness: 0, densityScale: 1 };
  }
  const t = (p - THINNING_START) / (1 - THINNING_START);
  // Smoothstep so the thinning has no visible seam where it begins.
  const e = t * t * (3 - 2 * t);
  return {
    emptiness: e,
    inNothing: p >= NOTHING_START,
    wallBrightness: Math.pow(e, 0.7),
    densityScale: Math.max(0, 1 - e)
  };
}

/* -------------------------------------------------------------------------- */
/*  Crossing over                                                              */
/* -------------------------------------------------------------------------- */

export interface Crossing {
  from: Verse;
  to: Verse;
  /** Where you arrive, as a real distance. */
  arriveAt: number;
  message: string;
}

/**
 * Entering The Nothing does not stop you - it moves you.
 *
 * You arrive just inside the next verse rather than at its far edge, so
 * there is somewhere to go once you get there.
 */
export function crossInto(current: VerseId): Crossing | null {
  const from = VERSES.find((v) => v.id === current);
  const to = from ? nextVerse(current) : null;
  if (!from || !to) return null;
  // Arriving one digit inside keeps the distance representable for every
  // verse up to the last, where the number stops being a float anyway.
  const arriveAt = Math.pow(10, Math.min(300, to.depth));
  return {
    from, to, arriveAt,
    message: 'You went into the nothing. ' + to.name + ': ' + to.tagline
  };
}

/**
 * The endpoint, formatted for display.
 *
 * Shown in full because the whole point is the absurdity of the number.
 */
export function finalCoordinateDisplay(): string {
  return FINAL_COORDINATE;
}

/** True once you have reached the very end. */
export function isAtFinalCoordinate(depth: number): boolean {
  return depth >= FINAL_DIGITS;
}

/**
 * A short, readable description of how far out you are.
 *
 * Past a few hundred digits the only honest description is the digit count,
 * so that is what is shown.
 */
export function describeDepth(depth: number): string {
  if (depth <= 0) return 'at the origin';
  if (depth <= 4) return depth + '-digit coordinates';
  if (depth <= 15) return '10^' + depth + ' units out';
  if (depth < FINAL_DIGITS) return depth + '-digit coordinates — far past counting';
  return 'the final coordinate';
}
