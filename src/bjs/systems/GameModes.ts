/**
 * GameModes — Explorer and Sandbox, and what actually differs between them.
 *
 * The app had one mode that tried to be both: you could fly to a galaxy and
 * also hurl a moon at the Earth, with no line between sightseeing and
 * physics. That made the universe feel unsafe to explore (anything could be
 * flung at you) and the physics feel weightless (nothing was ever at stake).
 *
 * So there are two modes, and the difference is a capability set rather than
 * two copies of the game:
 *
 *   Explorer — the universe as a place. You fly, you land, you look. Black
 *   holes lens light and you can fall into one, but nothing is torn apart
 *   around you and nothing you are not steering moves on its own.
 *
 *   Sandbox — the universe as an experiment. Throwing, grabbing, dragging
 *   black holes bodily through space, rewinding time, and the thing that
 *   makes a black hole frightening: tidal disruption. Fly a ship too close
 *   and it is pulled in and spaghettified; let a planet drift inside the
 *   Roche limit and it is torn into a stream of debris.
 *
 * Everything here is data and arithmetic. The renderer asks what is allowed
 * and how stretched a body should be; it never asks which mode it is in.
 */

/* ------------------------------ the modes ------------------------------ */

export type GameMode = 'explorer' | 'sandbox';

export const GAME_MODES: GameMode[] = ['explorer', 'sandbox'];

/** What a mode permits. Systems gate on these, never on the mode name. */
export interface ModeCapabilities {
  /** Hurling objects at worlds. */
  throwing: boolean;
  /** Picking bodies up and moving them by hand. */
  grabbing: boolean;
  /** Dragging a black hole bodily through space. */
  moveBlackHoles: boolean;
  /** Rewinding and replaying the simulation. */
  timeTravel: boolean;
  /** Tidal disruption: ships and planets torn apart near a horizon. */
  spaghettification: boolean;
  /** Lasers, impactors and the rest of the destruction kit. */
  destruction: boolean;
  /** Spawning bodies out of nothing. */
  spawning: boolean;
  /** Raising, lowering and flooding a planet's terrain by hand. */
  sculpting: boolean;
  /** Falling through a horizon into the dimensions beyond. */
  enterHoles: boolean;
  /** Flying, landing and walking. Always true; both modes are open world. */
  travel: boolean;
}

export interface ModeInfo {
  id: GameMode;
  name: string;
  glyph: string;
  tagline: string;
  blurb: string;
  caps: ModeCapabilities;
}

export const MODES: Record<GameMode, ModeInfo> = {
  explorer: {
    id: 'explorer',
    name: 'Explorer',
    glyph: '🔭',
    tagline: 'The universe as a place',
    blurb:
      'Fly anywhere, land on anything, fall into a black hole and come out ' +
      'somewhere else. Nothing is torn apart around you.',
    caps: {
      throwing: false,
      grabbing: false,
      moveBlackHoles: false,
      timeTravel: false,
      spaghettification: false,
      destruction: false,
      spawning: false,
      sculpting: false,
      enterHoles: true,
      travel: true
    }
  },
  sandbox: {
    id: 'sandbox',
    name: 'Sandbox',
    glyph: '🌌',
    tagline: 'The universe as an experiment',
    blurb:
      'Full physics. Throw moons at planets, drag black holes through space, ' +
      'wind time backwards, and watch anything that strays too close to a ' +
      'horizon be pulled in and stretched into a thread.',
    caps: {
      throwing: true,
      grabbing: true,
      moveBlackHoles: true,
      timeTravel: true,
      spaghettification: true,
      destruction: true,
      spawning: true,
      sculpting: true,
      enterHoles: true,
      travel: true
    }
  }
};

/** Capabilities for a mode. Unknown ids fall back to Explorer, the safe one. */
export function capabilities(mode: GameMode): ModeCapabilities {
  return (MODES[mode] ?? MODES.explorer).caps;
}

/** Whether one named capability is available. */
export function can(mode: GameMode, cap: keyof ModeCapabilities): boolean {
  return capabilities(mode)[cap] === true;
}

/* -------------------------- tidal disruption -------------------------- */

/**
 * A body that a black hole can act on.
 *
 * Deliberately minimal: this models ships, planets, moons and thrown rocks
 * with the same three numbers, because tidal physics does not care what
 * something is, only how big and how strongly bound it is.
 */
export interface TidalBody {
  /** Half-length along the radial direction, world units. */
  size: number;
  /**
   * How strongly the body holds itself together, 0..1.
   *
   * 1 is a solid ship hull, ~0.35 a rocky planet, ~0.1 a dust cloud. Sets
   * how deep it can go before it comes apart.
   */
  cohesion: number;
  /** Mass, only used to decide how fast it is drawn in. */
  mass: number;
}

export const SHIP: TidalBody = { size: 12, cohesion: 1.0, mass: 1e5 };
/**
 * Cohesion 0.12, not 0.35: a planet is held together by its own gravity
 * rather than by material strength, which is far weaker at that scale. It
 * is torn apart well outside the distance a rigid hull survives.
 */
export const ROCKY_PLANET: TidalBody = { size: 900, cohesion: 0.12, mass: 6e24 };

/** What the tidal field is doing to one body, right now. */
export interface TidalState {
  /** Tidal stress relative to what the body can bear. 1 = at the limit. */
  stress: number;
  /**
   * How stretched the body is along the radial direction, ≥ 1.
   *
   * This is the number a renderer scales geometry by, which is what makes
   * spaghettification visible rather than merely reported.
   */
  stretch: number;
  /** Matching squeeze across the radial direction, ≤ 1. Volume is preserved. */
  squeeze: number;
  /** True once the body has passed its limit and is coming apart. */
  disrupting: boolean;
  /** How far through disruption it is, 0..1. At 1 it is a debris stream. */
  shredded: number;
  /** Inward acceleration toward the hole, world units/s². */
  pull: number;
  /** True once it has crossed the horizon and is gone. */
  consumed: boolean;
}

/** A body sitting peacefully, unaffected. Returned whenever physics is off. */
export const CALM: TidalState = {
  stress: 0, stretch: 1, squeeze: 1, disrupting: false,
  shredded: 0, pull: 0, consumed: false
};

/**
 * How far out a body of this cohesion starts to come apart, in horizon radii.
 *
 * This is the Roche limit in spirit: the tidal field goes as M/r³, so the
 * distance at which it overwhelms a body's self-gravity scales as the cube
 * root of the ratio. A loosely bound planet is shredded far outside the
 * horizon; a rigid ship survives almost to the edge.
 */
export function rocheRadii(body: TidalBody): number {
  const c = Math.max(0.02, Math.min(1, body.cohesion));
  // 12 horizon radii for dust, ~2.2 for a solid hull.
  return 2.0 + 10.0 / Math.cbrt(c * 120);
}

/**
 * Tidal state for a body at a given distance from a hole.
 *
 * `distance` and `horizon` are in the same world units. In Explorer mode the
 * caller simply does not call this — but it is also safe to call with
 * `enabled = false`, which returns CALM, so a renderer can stay branch-free.
 */
export function tidalState(
  body: TidalBody,
  distance: number,
  horizon: number,
  enabled = true
): TidalState {
  if (!enabled) return CALM;
  // Math.max(1e-6, NaN) is NaN, so a bad horizon has to be rejected outright
  // rather than clamped. A single NaN frame here would otherwise reach the
  // renderer as a NaN scale factor and the body would vanish.
  if (!Number.isFinite(horizon) || horizon <= 0) return CALM;
  const hz = Math.max(1e-6, horizon);
  const d = Number.isFinite(distance) ? Math.max(0, distance) : Infinity;
  if (!Number.isFinite(d)) return CALM;

  const radii = d / hz;
  const limit = rocheRadii(body);

  // Inside the horizon there is nothing left to model.
  if (radii <= 1) {
    return {
      stress: Infinity, stretch: 1, squeeze: 1, disrupting: true,
      shredded: 1, pull: 0, consumed: true
    };
  }

  // Tidal stress ~ 1/r³, normalised so stress = 1 exactly at the Roche limit.
  const stress = Math.pow(limit / radii, 3);

  if (stress < 1) {
    // Outside the limit: intact, but already being drawn in.
    return {
      stress,
      stretch: 1 + stress * 0.35,
      squeeze: 1 / Math.sqrt(1 + stress * 0.35),
      disrupting: false,
      shredded: 0,
      pull: pullAt(radii, hz),
      consumed: false
    };
  }

  // Past the limit the body stretches without bound. Capped so a renderer
  // never gets an absurd scale factor, but the cap is high enough that it
  // genuinely reads as a thread rather than an oval.
  const over = Math.min(1, (stress - 1) / 6);
  const stretch = Math.min(40, 1 + Math.pow(stress, 1.35));
  return {
    stress,
    stretch,
    squeeze: 1 / Math.sqrt(stretch),
    disrupting: true,
    shredded: over,
    pull: pullAt(radii, hz),
    consumed: false
  };
}

/** Inward acceleration, ~1/r², scaled to feel right at play distances. */
function pullAt(radii: number, horizon: number): number {
  const r = Math.max(1.0001, radii);
  return (horizon * 26) / (r * r);
}

/**
 * Whether a body at this distance is doomed — past the point where its own
 * thrust could plausibly get it out. Used to warn the player before it is
 * too late, which is the difference between a physics toy and a trap.
 */
export function isDoomed(body: TidalBody, distance: number, horizon: number): boolean {
  const st = tidalState(body, distance, horizon, true);
  return st.consumed || st.stress >= 1;
}

/** Human-readable summary, for the HUD. */
export function describeTidal(st: TidalState): Record<string, string> {
  return {
    'Tidal stress': st.consumed ? 'consumed'
      : st.stress < 0.01 ? 'negligible'
      : (st.stress * 100).toFixed(0) + '% of limit',
    'Elongation': st.stretch.toFixed(2) + '×',
    'Status': st.consumed ? 'gone'
      : st.shredded >= 1 ? 'a stream of debris'
      : st.disrupting ? 'coming apart'
      : st.stress > 0.5 ? 'straining'
      : 'intact'
  };
}
