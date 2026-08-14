/**
 * HoleInterior — what is actually inside a black hole, and how far in it is.
 *
 * Crossing a horizon used to be a state flag: one frame you were outside,
 * the next you were "inside" and nothing further happened. That is not what
 * falling into a black hole should feel like. The interior is a place with
 * extent, and reaching the far end of it is a journey of thousands of units
 * that you fly, not a threshold you trip.
 *
 * The model here is a single scalar — how far you have fallen past the
 * horizon — turned into everything a renderer or HUD needs:
 *
 *   - the throat, where the outside universe is still a bright window behind
 *     you and the walls are only beginning to close;
 *   - the deep fall, where that window shrinks to a point;
 *   - for some holes, a *second* lens far inside, wrapped around a small
 *     white dot: the singularity itself, which you can choose to fly into;
 *   - and finally arrival, in a dimension chosen by which of those routes
 *     you took.
 *
 * Three destinations exist:
 *
 *   - Every ordinary hole opens onto a procedural dimension, seeded from the
 *     hole, so falling all the way down always lands you somewhere.
 *   - Passing through the exposed singularity of a nested hole puts you in
 *     the Dust Stream instead of the dimension you would otherwise have got.
 *   - One rare hole in the universe is Gargantua. It arrives in darkness and
 *     then drops you into the Library Realm.
 *
 * Nothing here touches Babylon or the DOM. It is arithmetic over a seed and
 * a distance, so every branch above can be asserted directly.
 */

/* ------------------------------- rng ------------------------------- */

/** Deterministic per-hole RNG, matching HoleProfiles' xorshift. */
function rng(seed: number): () => number {
  let s = (seed >>> 0) || 1;
  return () => {
    s ^= s << 13; s >>>= 0;
    s ^= s >> 17;
    s ^= s << 5; s >>>= 0;
    return s / 4294967296;
  };
}

/* ----------------------------- rarity ----------------------------- */

/**
 * How rare the Gargantua hole is.
 *
 * Rare enough that finding one means something, common enough that a player
 * who explores will actually meet one rather than only hear about it. At
 * this rate roughly one hole in 128 is Gargantua.
 */
export const GARGANTUA_CHANCE = 1 / 128;

/**
 * Whether this specific hole is the Interstellar-style Gargantua.
 *
 * Deterministic in the seed, and rolled from a stream of its own (the seed
 * is salted) so it is independent of every other property the hole draws.
 * A hole that re-rolled its identity when you looked away would make the
 * find meaningless.
 */
export function isGargantua(seed: number): boolean {
  return rng((seed >>> 0) ^ 0x6a09e667)() < GARGANTUA_CHANCE;
}

/* ------------------------------ the plan ------------------------------ */

/** Where a completed fall puts you. */
export interface InteriorDestination {
  /** 'library' and 'dust' are named realms; 'procedural' rolls a dimension. */
  kind: 'library' | 'dust' | 'procedural';
  /** Archetype id for named realms; empty for procedural. */
  realm: string;
  /** Seed handed to the dimension generator. */
  seed: number;
  /** Depth handed to the dimension generator; deeper = stranger. */
  depth: number;
  /** Copy for the arrival toast. */
  blurb: string;
}

/** The fixed character of one hole's interior, derived from its seed. */
export interface InteriorPlan {
  seed: number;
  /** True for the one rare Interstellar hole. */
  gargantua: boolean;
  /**
   * Total distance from the horizon to the far end, in world units.
   *
   * Thousands, deliberately: the user asked to fly "very very very deep",
   * and a fall you can complete in two seconds is a doorway, not a descent.
   */
  depth: number;
  /**
   * True when this hole has a second warped lens far inside it, wrapped
   * around an exposed singularity you can fly through. Not every hole does.
   */
  nested: boolean;
  /** Distance at which the nested lens starts to become visible. */
  nestedAt: number;
  /** Apparent radius of the white singularity dot, world units. */
  singularityRadius: number;
  /** How wide a miss still counts as passing through the singularity. */
  singularityCapture: number;
}

/** How likely a hole is to contain a nested lens and singularity. */
export const NESTED_CHANCE = 0.42;

/** Shallowest and deepest an interior can be, world units. */
export const MIN_DEPTH = 4200;
export const MAX_DEPTH = 26000;

/**
 * Works out the interior of one hole.
 *
 * Deterministic: the same hole always has the same depth, the same nested
 * lens or lack of one, and the same destination, so a player can go back.
 */
export function interiorPlan(seed: number): InteriorPlan {
  const s = seed >>> 0;
  const r = rng(s ^ 0x9e3779b9);
  const gargantua = isGargantua(s);

  // Gargantua is deep even by these standards.
  const span = MAX_DEPTH - MIN_DEPTH;
  const depth = gargantua
    ? MAX_DEPTH * (0.85 + r() * 0.15)
    : MIN_DEPTH + r() * span;

  const nested = !gargantua && r() < NESTED_CHANCE;
  // The inner lens appears in the last third or so of the fall, so there is
  // a long stretch of nothing but the closing dark before it resolves.
  const nestedAt = depth * (0.55 + r() * 0.2);

  const singularityRadius = 0.6 + r() * 2.4;

  return {
    seed: s,
    gargantua,
    depth,
    nested,
    nestedAt,
    singularityRadius,
    // Generous relative to the dot: aiming at a two-unit point from
    // thousands of units away is not a test of skill worth setting.
    singularityCapture: singularityRadius * 9
  };
}

/**
 * Where this hole puts you, given whether you went through the singularity.
 *
 * Kept separate from the plan because it is a function of what the player
 * did, not only of which hole they picked.
 */
export function destinationFor(
  plan: InteriorPlan, throughSingularity: boolean
): InteriorDestination {
  if (plan.gargantua) {
    return {
      kind: 'library',
      realm: 'library',
      seed: plan.seed ^ 0x11bad,
      depth: 4,
      blurb: 'You fall out of the dark into a structure made of moments.'
    };
  }
  if (throughSingularity && plan.nested) {
    return {
      kind: 'dust',
      realm: 'duststream',
      seed: plan.seed ^ 0xd057,
      depth: 3,
      blurb: 'Through the singularity. Dust, going somewhere, taking you with it.'
    };
  }
  // Depth grows with how deep the hole was, so bigger holes open onto
  // stranger places.
  const depth = Math.max(0, Math.min(9,
    Math.floor((plan.depth - MIN_DEPTH) / (MAX_DEPTH - MIN_DEPTH) * 7)));
  return {
    kind: 'procedural',
    realm: '',
    seed: plan.seed ^ 0x5eed,
    depth,
    blurb: 'The horizon closes behind you and somewhere else opens.'
  };
}

/* ------------------------------ the fall ------------------------------ */

export type FallPhase =
  /** Still outside; nothing has happened yet. */
  | 'outside'
  /** Just past the horizon: the way back is a wide bright window. */
  | 'throat'
  /** Long dark middle. The window is a dot. */
  | 'deep'
  /** A second lens has resolved ahead, with a white point at its centre. */
  | 'nested'
  /** Close enough to the singularity to fly into it. */
  | 'singularity'
  /** Gargantua only: total darkness before the Library. */
  | 'darkness'
  /** The fall is over; a destination is available. */
  | 'arrived';

/** Everything a renderer, shader or HUD needs for one instant of the fall. */
export interface FallState {
  phase: FallPhase;
  /** 0 at the horizon, 1 at the far end. */
  progress: number;
  /**
   * How much of the view the interior has taken over, 0..1. Drives the
   * shader's `insideAmt`.
   */
  inside: number;
  /**
   * Angular size of the window back to the universe you came from, 0..1.
   * Starts wide and shrinks; never quite reaches zero until arrival, so
   * you can always look back at where you were.
   */
  exitWindow: number;
  /** Strength of the second lens ahead, 0 when this hole has none. */
  nestedLens: number;
  /** Brightness of the white singularity dot, 0..1. */
  singularity: number;
  /** Distance still to fall, world units. Straight into the HUD. */
  remaining: number;
  /** True once a destination should be handed to the app. */
  complete: boolean;
}

/**
 * Turns "how far have I fallen" into the full state of the descent.
 *
 * `fallen` is measured from the horizon inward and clamped, so feeding it
 * garbage (a NaN from a bad frame, a negative from a rounding slip) yields
 * the outside state rather than a broken view.
 */
export function fallState(plan: InteriorPlan, fallen: number): FallState {
  const d = Number.isFinite(fallen) ? Math.max(0, fallen) : 0;
  const depth = Math.max(1, plan.depth);
  const progress = Math.min(1, d / depth);

  // The exit window closes fast at first, then lingers as a point. An
  // exponential rather than a line, because that is how an aperture that is
  // closing at a constant angular rate actually reads.
  const exitWindow = Math.max(0, Math.exp(-progress * 4.2));

  // The interior takes over the view quickly, but not instantly.
  const inside = Math.min(1, Math.pow(progress, 0.45) * 1.08);

  let nestedLens = 0;
  let singularity = 0;
  if (plan.nested && d >= plan.nestedAt) {
    const span = Math.max(1, depth - plan.nestedAt);
    const t = Math.min(1, (d - plan.nestedAt) / span);
    nestedLens = t;
    // The white dot only becomes visible once the inner lens has formed
    // around it, which is what makes it read as a singularity rather than a
    // star that happens to be there.
    singularity = Math.max(0, (t - 0.25) / 0.75);
  }

  const remaining = Math.max(0, depth - d);
  const complete = progress >= 1;

  let phase: FallPhase;
  if (d <= 0) phase = 'outside';
  else if (complete) phase = 'arrived';
  else if (plan.gargantua && progress > 0.72) phase = 'darkness';
  else if (singularity > 0.55) phase = 'singularity';
  else if (nestedLens > 0) phase = 'nested';
  else if (progress > 0.18) phase = 'deep';
  else phase = 'throat';

  return {
    phase, progress, inside, exitWindow, nestedLens, singularity,
    remaining, complete
  };
}

/**
 * Whether the player is inside the singularity's capture radius.
 *
 * `offAxis` is how far they are from the axis of the fall — you have to
 * actually aim at the dot to go through it, otherwise you sail past and get
 * the ordinary dimension at the bottom.
 */
export function throughSingularity(
  plan: InteriorPlan, state: FallState, offAxis: number
): boolean {
  if (!plan.nested) return false;
  if (state.singularity < 0.55) return false;
  const off = Number.isFinite(offAxis) ? Math.abs(offAxis) : Infinity;
  return off <= plan.singularityCapture;
}

/** Human-readable summary of a descent, for the HUD. */
export function describeFall(
  plan: InteriorPlan, state: FallState
): Record<string, string> {
  const label: Record<FallPhase, string> = {
    outside: 'outside the horizon',
    throat: 'through the throat',
    deep: 'in the deep fall',
    nested: 'a second lens ahead',
    singularity: 'the singularity is right there',
    darkness: 'darkness',
    arrived: 'arrived'
  };
  return {
    'Descent': label[state.phase],
    'Depth': Math.round(state.progress * 100) + '%',
    'Remaining': Math.round(state.remaining).toLocaleString() + ' u',
    'Way back': state.exitWindow > 0.01
      ? 'visible (' + Math.round(state.exitWindow * 100) + '%)'
      : 'gone',
    'Interior': plan.gargantua
      ? 'anomalous — this one is not like the others'
      : plan.nested ? 'nested singularity' : 'simple'
  };
}
