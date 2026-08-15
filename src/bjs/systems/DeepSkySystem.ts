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
  return Math.min(1, Math.pow(raw, 0.36) * 0.026);
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


/* ----------------------- 3D simplex noise ------------------------------- */
/*
 * Used to break up the interstellar medium.
 *
 * A galaxy whose density depends only on distance from the core is a set of
 * perfectly smooth concentric shells - which is exactly why the fog reads as
 * a hard boundary rather than as cloud. Real nebulae are filamentary, so the
 * radial falloff is modulated by a noise field sampled in world space.
 *
 * Simplex rather than Perlin: it has no directional bias, so the clouds do
 * not line up with the coordinate axes, and it is cheaper in 3D.
 */

const SIMPLEX_GRAD: ReadonlyArray<readonly [number, number, number]> = [
  [1, 1, 0], [-1, 1, 0], [1, -1, 0], [-1, -1, 0],
  [1, 0, 1], [-1, 0, 1], [1, 0, -1], [-1, 0, -1],
  [0, 1, 1], [0, -1, 1], [0, 1, -1], [0, -1, -1]
];

/** Integer hash with avalanche, so neighbouring cells are uncorrelated. */
function hash3(i: number, j: number, k: number): number {
  let h = Math.imul(i, 0x27d4eb2d) ^ Math.imul(j, 0x165667b1) ^ Math.imul(k, 0x9e3779b1);
  h ^= h >>> 15; h = Math.imul(h, 0x85ebca6b);
  h ^= h >>> 13; h = Math.imul(h, 0xc2b2ae35);
  h ^= h >>> 16;
  return h >>> 0;
}

/** 3D simplex noise, roughly in [-1, 1]. */
export function simplex3(x: number, y: number, z: number): number {
  const F3 = 1 / 3, G3 = 1 / 6;
  const s = (x + y + z) * F3;
  const i = Math.floor(x + s), j = Math.floor(y + s), k = Math.floor(z + s);
  const t = (i + j + k) * G3;
  const x0 = x - (i - t), y0 = y - (j - t), z0 = z - (k - t);

  // Which of the six tetrahedra we are in.
  let i1, j1, k1, i2, j2, k2;
  if (x0 >= y0) {
    if (y0 >= z0)      { i1=1;j1=0;k1=0; i2=1;j2=1;k2=0; }
    else if (x0 >= z0) { i1=1;j1=0;k1=0; i2=1;j2=0;k2=1; }
    else               { i1=0;j1=0;k1=1; i2=1;j2=0;k2=1; }
  } else {
    if (y0 < z0)       { i1=0;j1=0;k1=1; i2=0;j2=1;k2=1; }
    else if (x0 < z0)  { i1=0;j1=1;k1=0; i2=0;j2=1;k2=1; }
    else               { i1=0;j1=1;k1=0; i2=1;j2=1;k2=0; }
  }

  const c = [
    [x0, y0, z0, i, j, k],
    [x0 - i1 + G3, y0 - j1 + G3, z0 - k1 + G3, i + i1, j + j1, k + k1],
    [x0 - i2 + 2 * G3, y0 - j2 + 2 * G3, z0 - k2 + 2 * G3, i + i2, j + j2, k + k2],
    [x0 - 1 + 3 * G3, y0 - 1 + 3 * G3, z0 - 1 + 3 * G3, i + 1, j + 1, k + 1]
  ];

  let n = 0;
  for (const [dx, dy, dz, ii, jj, kk] of c) {
    let t0 = 0.6 - dx * dx - dy * dy - dz * dz;
    if (t0 <= 0) continue;
    const g = SIMPLEX_GRAD[hash3(ii, jj, kk) % 12];
    t0 *= t0;
    n += t0 * t0 * (g[0] * dx + g[1] * dy + g[2] * dz);
  }
  return Math.max(-1, Math.min(1, 32 * n));
}

/** Fractal simplex: several octaves, so clouds have detail at every scale. */
export function fbm3(x: number, y: number, z: number, octaves = 4): number {
  let sum = 0, amp = 0.5, freq = 1, norm = 0;
  for (let o = 0; o < octaves; o++) {
    sum += simplex3(x * freq, y * freq, z * freq) * amp;
    norm += amp;
    amp *= 0.5; freq *= 2.07;   // non-integer lacunarity avoids echoes
  }
  return sum / Math.max(norm, 1e-9);
}

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
  const smooth = t * t * (3 - 2 * t);

  // Break the smooth radial falloff with a noise field sampled in world
  // space. Without this the medium is a set of perfect concentric shells,
  // which is what makes the fog look like a hard geometric boundary instead
  // of cloud. Sampling in world space (not eye space) means the clouds stay
  // put as you fly through them rather than swimming with the camera.
  const scale = 3.2 / r;
  const n = fbm3(eye.x * scale, eye.y * scale, eye.z * scale, 4);
  // Map to a multiplier centred on 1: thin lanes and dense filaments, but
  // never negative and never so thick it becomes a wall.
  const clouds = Math.max(0.25, Math.min(1.9, 1 + n * 0.85));

  const depth = Math.max(0, Math.min(1, smooth * clouds));
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

/** Distance at which the approach brake starts to bite. */
export const APPROACH_RADIUS = 500;
/**
 * The brake never fully stops you, but it must bring 90,000x down to
 * something you can actually fly with.
 *
 * At 0.02 the floor still left ~1,300x - about 2,000 units per frame, when
 * a planet is tens of units across, so you would still shoot straight past.
 * 1/90000 brings full warp back to roughly 1x at the surface, which is
 * walking pace next to a world.
 */
export const APPROACH_FLOOR = 1 / 90000;

export const DEFAULT_WARP_DRIVE: WarpDriveOptions = {
  spool: 1.1,
  rampUp: 5.5,
  // Bleeding off faster than it builds means you can always stop, which
  // matters when the thing can cross a galaxy in seconds.
  decay: 0.85,
  // 100x the old 900.
  //
  // This is only sane because the universe is now unbounded: galaxies
  // repeat forever on a 260,000-unit cell grid, so there is no rim to
  // overshoot into empty nothing. At 90,000x a shuttle covers roughly
  // 8.5 million units a second, which crosses a 50,000-unit galaxy in a
  // blink and reaches the next one in well under a second - which is the
  // point, because at the old speed the gap between galaxies was a
  // half-minute of staring at black.
  //
  // The cubic charge curve is what keeps this controllable: you only get
  // the top multiplier after holding thrust through the full ramp, and
  // decay outruns build so releasing always drops you back to something
  // you can manoeuvre with.
  topMultiplier: 90000
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
  private approachScale = 1;

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
    const raw = 1 + curve * (this.opts.topMultiplier - 1);
    return {
      charge: c,
      multiplier: raw * this.approachScale,
      streak: Math.min(1, c * 1.25),
      engaged: c > 0.02
    };
  }

  /**
   * Exponential deceleration on approach.
   *
   * At full warp the ship covers ~142,000 units per frame, and a planet is
   * a few tens of units across, so without this you cannot arrive anywhere
   * - you are always either far away or already past. The brake is applied
   * to the MULTIPLIER rather than to position, so control stays smooth and
   * the player never gets shoved.
   *
   * The curve is exponential in the ratio of distance to the braking
   * radius, which means the closer you get the harder it bites, and it
   * approaches but never reaches zero: you can always still move.
   */
  setApproach(distance: number, brakeRadius = APPROACH_RADIUS): void {
    if (!Number.isFinite(distance) || distance < 0) {
      this.approachScale = 1;
      return;
    }
    const r = Math.max(brakeRadius, 1e-3);
    if (distance >= r) { this.approachScale = 1; return; }
    const t = distance / r;                 // 0 at the surface, 1 at the edge
    // exp curve: 0.02x hard against the body, easing back to 1x at r.
    const e = (Math.exp(t * 3.2) - 1) / (Math.exp(3.2) - 1);
    this.approachScale = Math.max(APPROACH_FLOOR, e);
  }

  /** Current approach brake, 1 when nothing is near. */
  get approach(): number { return this.approachScale; }

  stats(): Record<string, string> {
    const s = this.state();
    return {
      'Warp charge': (s.charge * 100).toFixed(0) + '%',
      'Warp factor': s.engaged ? s.multiplier.toFixed(0) + 'x' : 'idle'
    };
  }
}
