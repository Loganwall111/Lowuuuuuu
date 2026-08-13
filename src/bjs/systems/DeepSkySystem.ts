/**
 * DeepSkySystem — the stars you can actually fly to.
 *
 * Until now the night sky was a painted texture on a sphere. It looked
 * plausible until you flew: the stars never parallaxed, never grew, and
 * nothing you could see was anywhere you could go. That is the difference
 * between a backdrop and a universe.
 *
 * This renders every region the universe knows about as a real point of
 * light at its real position. The faint star near the horizon IS a star
 * system with planets, at that bearing, that distance away. Fly for long
 * enough and it resolves into a disc, then a sun. Nothing is faked.
 *
 * Two things make that affordable:
 *
 *  - Brightness follows the inverse-square law, so distance is conveyed by
 *    the physics rather than by an artistic fudge. Anything that falls below
 *    perceptual threshold is simply not drawn.
 *  - Everything is one points cloud with per-vertex colour, so ten thousand
 *    distant objects cost a single draw call.
 *
 * It also answers "why can I not see the galaxy from the ground?" - because
 * the sky was a texture. Now the sky is the universe, so a galaxy is visible
 * from a planet's surface exactly as it should be.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';

/** Anything in the sky that can also be visited. */
export interface SkyObject {
  id: string;
  kind: string;
  position: Vector3;
  /** Physical radius, world units. */
  radius: number;
  /** Intrinsic brightness. A galaxy is far more luminous than a planet. */
  luminosity: number;
  /** Emitted colour. */
  color: Color3;
}

/** How a sky object should be drawn from where the viewer is standing. */
export interface SkySample {
  id: string;
  /** Distance from the eye, world units. */
  distance: number;
  /** Apparent brightness after inverse-square falloff, 0-1. */
  brightness: number;
  /** Angular size in radians. */
  angularSize: number;
  /** Colour scaled by brightness, ready to write into a vertex buffer. */
  color: Color4;
  /** True once the object is close enough to be a disc rather than a point. */
  resolved: boolean;
}

/**
 * Intrinsic luminosity per kind of place. These are relative numbers, chosen
 * so that a galaxy remains visible across the whole map while a planet only
 * shows up once you are in its neighbourhood - which is exactly how the real
 * sky behaves.
 */
export const LUMINOSITY: Record<string, number> = {
  galaxy: 4.2e9,
  nebula: 6.0e8,
  'star-system': 9.0e6,
  blackhole: 2.2e6,   // the accretion disc, not the hole
  dimension: 1.4e6,
  ocean: 2.0e4,
  terrain: 1.6e4,
  planet: 1.8e4,
  'deep-space': 0
};

/** Characteristic colour per kind. */
export const SKY_COLOR: Record<string, [number, number, number]> = {
  galaxy: [0.86, 0.88, 1.0],
  nebula: [0.95, 0.55, 0.85],
  'star-system': [1.0, 0.94, 0.82],
  blackhole: [1.0, 0.62, 0.28],
  dimension: [0.62, 1.0, 0.86],
  ocean: [0.45, 0.70, 1.0],
  terrain: [0.75, 0.65, 0.5],
  planet: [0.8, 0.8, 0.85],
  'deep-space': [0.5, 0.5, 0.5]
};

/** Below this apparent brightness an object is not worth drawing. */
export const VISIBILITY_FLOOR = 1e-4;

/**
 * Apparent brightness from the inverse-square law, normalised so that a
 * bright nearby object saturates at 1.
 *
 * Real magnitude systems are logarithmic; using the raw physical falloff
 * would make everything either blinding or invisible, so the result is
 * passed through a gentle power curve. The *ordering* and the relative
 * spacing still come from the physics.
 */
export function apparentBrightness(luminosity: number, distance: number): number {
  if (!(luminosity > 0)) return 0;
  const d = Math.max(distance, 1e-3);
  const raw = luminosity / (4 * Math.PI * d * d);
  if (!Number.isFinite(raw)) return 0;
  // Perceptual compression: doubling distance dims, but not to nothing.
  return Math.min(1, Math.pow(raw, 0.36) * 0.02);
}

/** Angular diameter of an object of this radius at this distance. */
export function angularSize(radius: number, distance: number): number {
  const d = Math.max(distance, 1e-6);
  if (radius >= d) return Math.PI;
  return 2 * Math.asin(Math.min(1, radius / d));
}

/**
 * Samples one object from a viewpoint.
 *
 * `resolved` marks the moment an object stops being a dot and becomes a
 * shape - about a fifth of a degree, roughly where the human eye stops
 * seeing a point source.
 */
export function sampleSky(obj: SkyObject, eye: Vector3): SkySample {
  const distance = Vector3.Distance(obj.position, eye);
  const brightness = apparentBrightness(obj.luminosity, distance);
  const size = angularSize(obj.radius, distance);
  const c = obj.color;
  return {
    id: obj.id,
    distance,
    brightness,
    angularSize: size,
    color: new Color4(c.r * brightness, c.g * brightness, c.b * brightness, brightness),
    resolved: size > 0.0035
  };
}

/**
 * Everything worth drawing from this viewpoint, brightest first.
 *
 * Sorting by brightness rather than distance means the budget is spent on
 * what the eye would actually notice: a distant galaxy outranks a nearby
 * dark rock, which is correct.
 */
export function visibleSky(objects: SkyObject[], eye: Vector3, budget = 6000): SkySample[] {
  const out: SkySample[] = [];
  for (const o of objects) {
    const s = sampleSky(o, eye);
    if (s.brightness >= VISIBILITY_FLOOR) out.push(s);
  }
  out.sort((a, b) => b.brightness - a.brightness);
  return out.length > budget ? out.slice(0, budget) : out;
}

/* -------------------------------------------------------------------------- */
/*  Being inside a galaxy                                                      */
/* -------------------------------------------------------------------------- */

/** What it looks like inside a galaxy's volume. */
export interface GalacticMedium {
  /** True when the eye is within the galaxy's disc. */
  inside: boolean;
  /** 0 at the rim, 1 at the core. */
  depth: number;
  /** Exponential fog density for the interstellar medium. */
  fogDensity: number;
  /** Fog colour, warming toward the core where the old stars are. */
  fogColor: [number, number, number];
  /** Extra star density multiplier: the core is crowded. */
  starDensity: number;
}

/**
 * How thick the interstellar medium is where the eye currently is.
 *
 * Flying into a galaxy should feel like flying into weather: the void
 * gradually fills with light and dust rather than switching over at a
 * boundary. Density rises toward the core, which is also where the stars
 * crowd together, so the two reinforce each other.
 */
export function galacticMedium(
  eye: Vector3, center: Vector3, radius: number, maxFog = 0.0016
): GalacticMedium {
  const r = Math.max(radius, 1e-6);
  const d = Vector3.Distance(eye, center);
  if (!(d < r)) {
    return {
      inside: false, depth: 0, fogDensity: 0,
      fogColor: [0, 0, 0], starDensity: 1
    };
  }
  // Smooth, not linear: the outskirts stay clear and it thickens fast in
  // the inner third, which is how a spiral galaxy actually looks.
  const t = 1 - d / r;
  const depth = t * t * (3 - 2 * t);
  return {
    inside: true,
    depth,
    fogDensity: maxFog * depth,
    // Blue-white in the arms, warmer and dustier toward the bulge.
    fogColor: [
      0.10 + 0.42 * depth,
      0.13 + 0.28 * depth,
      0.24 + 0.20 * depth
    ],
    starDensity: 1 + depth * 14
  };
}

/* -------------------------------------------------------------------------- */
/*  Warp                                                                       */
/* -------------------------------------------------------------------------- */

/** State of the warp drive. */
export interface WarpState {
  /** 0-1 charge. */
  charge: number;
  /** Multiplier applied to normal flight speed. */
  multiplier: number;
  /** 0-1, how hard the streak effect should be driven. */
  streak: number;
  /** True once the drive is doing something the player can feel. */
  engaged: boolean;
}

export interface WarpDriveOptions {
  /** Seconds of continuous thrust before the drive starts to build. */
  spool: number;
  /** Seconds from first build to full charge. */
  rampUp: number;
  /** How fast charge bleeds away once you stop, per second. */
  decay: number;
  /** Speed multiplier at full charge. */
  topMultiplier: number;
}

export const DEFAULT_WARP_DRIVE: WarpDriveOptions = {
  spool: 1.1,
  rampUp: 5.5,
  // Bleeding off faster than it builds means you can always stop, which
  // matters when the thing can cross a galaxy in seconds.
  decay: 0.85,
  topMultiplier: 900
};

/**
 * The warp drive: hold thrust and the universe opens up.
 *
 * Crossing interstellar distances at normal flight speed is a screensaver.
 * This makes sustained thrust meaningful - keep pushing and you accelerate
 * without limit, ease off and you drop straight back to something you can
 * manoeuvre with.
 *
 * Charge is deliberately not linear in time. It ramps as a cubic, so the
 * first seconds feel like a drive spooling up and the last like something
 * genuinely running away with you.
 */
export class WarpDrive {
  opts: WarpDriveOptions;
  private held = 0;
  private chargeValue = 0;

  constructor(opts: Partial<WarpDriveOptions> = {}) {
    this.opts = { ...DEFAULT_WARP_DRIVE, ...opts };
  }

  get charge(): number { return this.chargeValue; }

  /**
   * @param dt        seconds
   * @param thrusting whether the player is holding forward
   */
  update(dt: number, thrusting: boolean): WarpState {
    if (!Number.isFinite(dt) || dt <= 0) return this.state();

    if (thrusting) {
      this.held += dt;
      const past = this.held - this.opts.spool;
      if (past > 0) {
        const target = Math.min(1, past / Math.max(this.opts.rampUp, 1e-6));
        // Approach the target rather than snapping to it.
        this.chargeValue += (target - this.chargeValue) * Math.min(1, dt * 1.6);
      }
    } else {
      this.held = 0;
      this.chargeValue -= this.opts.decay * dt;
    }
    this.chargeValue = Math.max(0, Math.min(1, this.chargeValue));
    return this.state();
  }

  /** Drops out of warp immediately. */
  disengage(): void {
    this.held = 0;
    this.chargeValue = 0;
  }

  state(): WarpState {
    const c = this.chargeValue;
    // Cubic: gentle at first, then it really goes.
    const curve = c * c * c;
    return {
      charge: c,
      multiplier: 1 + curve * (this.opts.topMultiplier - 1),
      streak: Math.min(1, c * 1.25),
      engaged: c > 0.02
    };
  }

  stats(): Record<string, string> {
    const s = this.state();
    return {
      'Warp charge': (s.charge * 100).toFixed(0) + '%',
      'Warp factor': s.engaged ? s.multiplier.toFixed(0) + 'x' : 'idle'
    };
  }
}
