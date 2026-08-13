/**
 * CosmicScaleSystem — what happens when you keep going out.
 *
 * Fly far enough from the centre and you leave the universe entirely. Rather
 * than hitting an invisible wall, you pass into the next tier up: the
 * multiverse, then the metaverse (which turns out to be code), then stranger
 * things still.
 *
 * Each tier is a *place* with its own scale, look and contents. The tiers are
 * a ladder, so this is one mechanic rather than a series of special cases,
 * and going back inward reverses it exactly.
 */

export type TierId =
  | 'universe' | 'multiverse' | 'metaverse' | 'multimultiverse'
  | 'molecular' | 'source';

export interface CosmicTier {
  id: TierId;
  name: string;
  /** Shown when you cross into it. */
  tagline: string;
  /** Distance from origin at which you leave this tier outward. */
  boundary: number;
  /**
   * How much the world shrinks when you ascend. Everything you knew becomes
   * one object at the next tier up.
   */
  scaleFactor: number;
  /** Dominant colour, used for fog and ambient so each tier reads apart. */
  tint: [number, number, number];
  /** What floats around at this level. */
  inhabitants: string;
  /** Ambient density: how full of stuff the tier looks. */
  density: number;
}

/**
 * The ladder, innermost first. Order is load-bearing: index is depth.
 */
/**
 * Boundaries are enormous because space no longer has an edge: regions are
 * generated for ever in every direction, so a tier has to be genuinely far
 * away or you would cross the whole nesting in a few seconds of warp.
 *
 * At full warp (648,000 u/s) the first boundary is about nine seconds out
 * and the last is about thirty years. Getting to The Source is meant to be
 * absurd - that is the joke - but every step of the way is populated.
 */
export const TIERS: CosmicTier[] = [
  {
    id: 'universe',
    name: 'The Universe',
    tagline: 'Stars, worlds, and the space between them.',
    boundary: 6000000,
    scaleFactor: 1,
    tint: [0.06, 0.10, 0.22],
    inhabitants: 'galaxies',
    density: 1
  },
  {
    id: 'multiverse',
    name: 'The Multiverse',
    tagline: 'Your universe is one bubble among uncountable others.',
    boundary: 240000000,
    scaleFactor: 0.012,
    tint: [0.22, 0.09, 0.30],
    inhabitants: 'universe bubbles',
    density: 0.8
  },
  {
    id: 'metaverse',
    name: 'The Metaverse',
    tagline: 'It was all running on something. Here is the something.',
    boundary: 9600000000,
    scaleFactor: 0.010,
    tint: [0.04, 0.26, 0.20],
    inhabitants: 'lattices of executing code',
    density: 1.4
  },
  {
    id: 'multimultiverse',
    name: 'The Multi-Multiverse',
    tagline: 'Metaverses drift here in shoals. None of them notice you.',
    boundary: 384000000000,
    scaleFactor: 0.008,
    tint: [0.30, 0.16, 0.05],
    inhabitants: 'drifting metaverses',
    density: 0.6
  },
  {
    id: 'molecular',
    name: 'The Molecular Field',
    tagline: 'Everything above is a molecule in something larger.',
    boundary: 15360000000000,
    scaleFactor: 0.006,
    tint: [0.10, 0.20, 0.34],
    inhabitants: 'bonded molecules the size of multiverses',
    density: 1.8
  },
  {
    id: 'source',
    name: 'The Source',
    tagline: 'There is no further out. It loops.',
    // The last tier wraps back to the first: the universe is inside itself.
    boundary: 614400000000000,
    scaleFactor: 0.004,
    tint: [0.44, 0.42, 0.16],
    inhabitants: 'the beginning, seen from outside',
    density: 2.4
  }
];

export interface ScaleState {
  /** Index into TIERS. */
  depth: number;
  tier: CosmicTier;
  /** 0..1 how close to the outward boundary of this tier. */
  approach: number;
  /** True on the frame a boundary was crossed. */
  changed: boolean;
  /** +1 ascended, -1 descended, 0 no change. */
  direction: number;
}

export class CosmicScaleSystem {
  private depth = 0;
  /** Accumulated scale so callers can shrink the world as you ascend. */
  private worldScale = 1;
  /** Guards against flapping at a boundary. */
  private hysteresis = 0.08;
  private crossings = 0;

  get tier(): CosmicTier { return TIERS[this.depth]; }
  get tierDepth(): number { return this.depth; }
  get scale(): number { return this.worldScale; }
  get totalCrossings(): number { return this.crossings; }

  /**
   * Updates which tier the player is in from their distance to the origin.
   *
   * @param distance distance from the current tier's centre
   */
  update(distance: number): ScaleState {
    const d = Number.isFinite(distance) ? Math.abs(distance) : 0;
    const before = this.depth;
    const tier = TIERS[this.depth];

    // ---- outward: leave this tier ----
    if (d > tier.boundary && this.depth < TIERS.length - 1) {
      this.depth++;
      this.worldScale *= TIERS[this.depth].scaleFactor;
      this.crossings++;
    } else if (d > tier.boundary && this.depth === TIERS.length - 1) {
      // The ladder loops: past the last tier you re-enter the first, which
      // is the "infinite zoom out" - the universe contains itself.
      this.depth = 0;
      this.worldScale = 1;
      this.crossings++;
    } else if (this.depth > 0) {
      // ---- inward: fall back down a tier ----
      const inner = TIERS[this.depth - 1];
      // Come back in well short of the boundary so you cannot flicker.
      const reentry = inner.boundary * (1 - this.hysteresis);
      const scaled = d / Math.max(TIERS[this.depth].scaleFactor, 1e-9);
      if (scaled < reentry) {
        this.worldScale /= Math.max(TIERS[this.depth].scaleFactor, 1e-9);
        this.depth--;
        this.crossings++;
      }
    }

    const now = TIERS[this.depth];
    const approach = Math.max(0, Math.min(1, d / Math.max(now.boundary, 1)));

    return {
      depth: this.depth,
      tier: now,
      approach,
      changed: this.depth !== before,
      direction: this.depth === before ? 0 : (this.depth > before ? 1 : -1)
    };
  }

  /** Jumps straight to a tier. Used by debug and by save/load. */
  setDepth(depth: number): void {
    const d = Math.max(0, Math.min(TIERS.length - 1, Math.floor(depth)));
    let scale = 1;
    for (let i = 1; i <= d; i++) scale *= TIERS[i].scaleFactor;
    this.depth = d;
    this.worldScale = scale;
  }

  reset(): void {
    this.depth = 0;
    this.worldScale = 1;
    this.crossings = 0;
  }

  stats(): Record<string, string> {
    const t = this.tier;
    return {
      'Scale tier': t.name,
      'Tier depth': `${this.depth + 1} / ${TIERS.length}`,
      'Inhabited by': t.inhabitants,
      'World scale': this.worldScale < 0.001
        ? this.worldScale.toExponential(1)
        : this.worldScale.toFixed(3)
    };
  }
}
