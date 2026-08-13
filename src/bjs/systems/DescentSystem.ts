/**
 * DescentSystem — falling out of orbit onto a world.
 *
 * The point of this system is the moment the user actually asked for: a
 * planet that stops being a dot and becomes the ground. That only feels
 * right if the numbers underneath it are real, so this is modelled rather
 * than animated:
 *
 *   - Atmospheric density falls off exponentially with altitude, using the
 *     barometric scale height H = kT/mg. That single curve is what makes the
 *     upper atmosphere feel thin and the last few kilometres feel thick.
 *   - Drag is the standard ½ρv²Cd A, so terminal velocity emerges from the
 *     balance of drag and gravity instead of being typed in.
 *   - Entry heating follows the Sutton-Graves form, q ∝ √(ρ) v³, which is
 *     why re-entry glow appears high and fades long before touchdown.
 *   - Apparent planet size is the true angular diameter, 2·asin(R/d), so the
 *     world fills the sky on its own as you approach.
 *
 * Nothing here touches Babylon. It is pure numbers, so it can be tested
 * exactly and reused by any renderer, world or HUD.
 */

/** A world you can fall towards. */
export interface DescentBody {
  /** Surface radius, in world units (kilometres by convention). */
  radius: number;
  /** Surface gravity, m/s². */
  gravity: number;
  /** Sea-level atmospheric density, kg/m³. Zero for an airless body. */
  seaLevelDensity: number;
  /** Barometric scale height, km. Larger = puffier atmosphere. */
  scaleHeight: number;
  /** Altitude at which the atmosphere is considered to begin, km. */
  atmosphereTop: number;
  /** Sky tint at the surface, linear RGB 0-1. */
  skyColor: [number, number, number];
}

/** The falling object. */
export interface Faller {
  /** Mass, kg. */
  mass: number;
  /** Reference cross-sectional area, m². */
  area: number;
  /** Drag coefficient. ~1 for a person, ~0.3 for a capsule. */
  dragCoefficient: number;
  /** Nose radius, m. Only affects heating. */
  noseRadius: number;
}

/** Everything a renderer or HUD needs for one instant of a descent. */
export interface DescentState {
  /** Altitude above the surface, km. */
  altitude: number;
  /** Speed along the fall, m/s. */
  speed: number;
  /** Local air density, kg/m³. */
  density: number;
  /** Fraction of the way through the atmosphere, 1 at the top, 0 at ground. */
  atmosphereFraction: number;
  /** Angular diameter of the planet as seen from here, radians. */
  apparentDiameter: number;
  /** How much of the visible sky the planet covers, 0-1. */
  skyCoverage: number;
  /** Heating rate, W/cm². */
  heatFlux: number;
  /** 0-1 glow factor for re-entry plasma. */
  reentryGlow: number;
  /** Sky colour at this altitude, fading to black in space. */
  skyColor: [number, number, number];
  /** Which part of the fall this is, for HUD copy. */
  phase: DescentPhase;
  /** True once the surface has been reached. */
  landed: boolean;
}

export type DescentPhase =
  | 'space'        // above the atmosphere entirely
  | 'entry'        // in the thin upper air, heating up
  | 'descent'      // thick air, slowing hard
  | 'approach'     // subsonic, ground detail resolving
  | 'landed';

/** Earth-like defaults, so a body can be described by only what differs. */
export const EARTHLIKE: DescentBody = {
  radius: 6371,
  gravity: 9.81,
  seaLevelDensity: 1.225,
  scaleHeight: 8.5,
  atmosphereTop: 100,
  skyColor: [0.35, 0.55, 0.92]
};

/**
 * Barometric density at an altitude.
 *
 * ρ(h) = ρ₀ · e^(−h/H). Below the surface the density is clamped to the
 * sea-level value rather than growing without bound.
 */
export function densityAt(body: DescentBody, altitudeKm: number): number {
  if (!(body.seaLevelDensity > 0)) return 0;
  if (!Number.isFinite(altitudeKm)) return 0;
  if (altitudeKm <= 0) return body.seaLevelDensity;
  const h = Math.max(body.scaleHeight, 1e-6);
  return body.seaLevelDensity * Math.exp(-altitudeKm / h);
}

/**
 * Terminal velocity: where drag exactly cancels weight.
 *
 * v = √(2mg / ρ Cd A). Returns Infinity in vacuum, which is correct - there
 * is nothing to stop you.
 */
export function terminalVelocity(body: DescentBody, faller: Faller, altitudeKm: number): number {
  const rho = densityAt(body, altitudeKm);
  const denom = rho * faller.dragCoefficient * faller.area;
  if (denom <= 1e-12) return Infinity;
  return Math.sqrt((2 * faller.mass * body.gravity) / denom);
}

/**
 * Sutton-Graves convective heating at the stagnation point, W/cm².
 *
 * q = k √(ρ/Rn) v³, with the classic Earth constant. This is why a capsule
 * glows high up where the air is thin but fast, and stops glowing long
 * before it lands.
 */
export function heatFlux(body: DescentBody, faller: Faller, altitudeKm: number, speed: number): number {
  const rho = densityAt(body, altitudeKm);
  if (rho <= 0 || !(speed > 0)) return 0;
  const rn = Math.max(faller.noseRadius, 1e-3);
  const k = 1.7415e-4;
  // With SI inputs this form yields W/m2; the conventional way to quote
  // entry heating is W/cm2, hence the 1e4. Checked against Apollo 4:
  // Rn 4.69 m at 11 km/s near 65 km gives ~259 W/cm2, which matches the
  // published peak of a few hundred W/cm2.
  const q = (k * Math.sqrt(rho / rn) * Math.pow(speed, 3)) / 1e4;

  // Sutton-Graves describes a hypersonic shock layer. Applied literally it
  // claims serious heating for something merely drifting downward - a
  // parachutist at 50 m/s would come out at 13 W/cm2, when the actual
  // stagnation temperature rise there (v^2/2cp) is about one kelvin.
  // Below roughly Mach 5 there is no shock layer to speak of, so fade the
  // term out rather than reporting heat that does not exist.
  return q * hypersonicFactor(speed);
}

/**
 * How hypersonic the flow is, 0-1. Zero below the speed of sound, one at
 * Mach 5 and above, smooth in between.
 */
export function hypersonicFactor(speed: number, soundSpeed = 340): number {
  if (!(speed > 0) || !Number.isFinite(speed)) return 0;
  const mach = speed / Math.max(soundSpeed, 1e-6);
  const t = (mach - 1) / 4;                       // 0 at Mach 1, 1 at Mach 5
  const c = Math.min(1, Math.max(0, t));
  return c * c * (3 - 2 * c);                     // smoothstep
}

/**
 * True angular diameter of the body from a given altitude.
 *
 * At the surface this approaches π (the planet is the entire lower sky);
 * far away it shrinks toward zero. Using asin rather than a linear ramp is
 * what makes a planet swell convincingly on approach.
 */
export function apparentDiameter(body: DescentBody, altitudeKm: number): number {
  const d = body.radius + Math.max(altitudeKm, 0);
  if (d <= body.radius) return Math.PI;
  const ratio = Math.min(1, body.radius / d);
  return 2 * Math.asin(ratio);
}

/** Fraction of the full sphere of view filled by the planet, 0-1. */
export function skyCoverage(body: DescentBody, altitudeKm: number): number {
  // Solid angle of a sphere of angular radius θ is 2π(1 − cos θ);
  // the full sky is 4π steradians.
  const theta = apparentDiameter(body, altitudeKm) / 2;
  return (2 * Math.PI * (1 - Math.cos(theta))) / (4 * Math.PI);
}

/** Which phase of the fall an altitude and speed correspond to. */
export function phaseFor(body: DescentBody, altitudeKm: number, speed: number): DescentPhase {
  if (altitudeKm <= 0) return 'landed';
  if (altitudeKm > body.atmosphereTop) return 'space';
  // Roughly the speed of sound at altitude; below it the fall is calm.
  if (speed > 340 && altitudeKm > body.atmosphereTop * 0.25) return 'entry';
  if (altitudeKm > body.atmosphereTop * 0.06) return 'descent';
  return 'approach';
}

/**
 * Sky colour at altitude: the body's own sky, fading to black as the air
 * thins. An airless world is black from the ground up, which is right.
 */
export function skyColorAt(body: DescentBody, altitudeKm: number): [number, number, number] {
  const rho = densityAt(body, altitudeKm);
  const t = body.seaLevelDensity > 0
    ? Math.min(1, Math.max(0, rho / body.seaLevelDensity))
    : 0;
  // Air scatters roughly with density, but the eye is not linear; a mild
  // curve keeps a thin sky from looking like a black one.
  const k = Math.pow(t, 0.65);
  return [body.skyColor[0] * k, body.skyColor[1] * k, body.skyColor[2] * k];
}

/** Reads out the full state of a descent without advancing it. */
export function sampleDescent(
  body: DescentBody, faller: Faller, altitudeKm: number, speed: number
): DescentState {
  const alt = Math.max(0, altitudeKm);
  const q = heatFlux(body, faller, alt, speed);
  return {
    altitude: alt,
    speed,
    density: densityAt(body, alt),
    atmosphereFraction: Math.min(1, Math.max(0, alt / Math.max(body.atmosphereTop, 1e-6))),
    apparentDiameter: apparentDiameter(body, alt),
    skyCoverage: skyCoverage(body, alt),
    heatFlux: q,
    // 50 W/cm² is a hard re-entry; normalise against that for the glow.
    reentryGlow: Math.min(1, q / 50),
    skyColor: skyColorAt(body, alt),
    phase: phaseFor(body, alt, speed),
    landed: alt <= 0
  };
}

/**
 * A live fall. Integrates altitude and speed under gravity and drag.
 *
 * Kept deliberately simple (semi-implicit Euler) because it is stepped at
 * frame rate and stability matters more than formal order of accuracy: drag
 * is applied against the *new* speed, which cannot overshoot into negative
 * damping the way explicit Euler can at large dt.
 */
export class Descent {
  altitude: number;
  speed: number;
  landed = false;

  constructor(
    public body: DescentBody,
    public faller: Faller,
    startAltitudeKm: number,
    startSpeed = 0
  ) {
    this.altitude = Math.max(0, startAltitudeKm);
    this.speed = Math.max(0, startSpeed);
  }

  /** Advances the fall. Ignores nonsense timesteps. */
  step(dt: number): DescentState {
    if (!Number.isFinite(dt) || dt <= 0) return this.state();
    if (this.landed) return this.state();

    // Substep so a long frame cannot tunnel through the whole atmosphere.
    const steps = Math.min(8, Math.max(1, Math.ceil(dt / 0.05)));
    const h = dt / steps;

    for (let i = 0; i < steps && !this.landed; i++) {
      const rho = densityAt(this.body, this.altitude);
      const drag = 0.5 * rho * this.speed * this.speed *
                   this.faller.dragCoefficient * this.faller.area;
      const accel = this.body.gravity - drag / Math.max(this.faller.mass, 1e-9);

      this.speed = Math.max(0, this.speed + accel * h);
      // Altitude is in km, speed in m/s.
      this.altitude -= (this.speed * h) / 1000;

      if (this.altitude <= 0) {
        this.altitude = 0;
        this.landed = true;
      }
    }
    return this.state();
  }

  /** Current state, without advancing. */
  state(): DescentState {
    const s = sampleDescent(this.body, this.faller, this.altitude, this.speed);
    s.landed = this.landed || this.altitude <= 0;
    if (s.landed) s.phase = 'landed';
    return s;
  }

  /** HUD lines. */
  stats(): Record<string, string> {
    const s = this.state();
    return {
      'Altitude': s.altitude > 10
        ? s.altitude.toFixed(0) + ' km'
        : (s.altitude * 1000).toFixed(0) + ' m',
      'Fall speed': s.speed.toFixed(0) + ' m/s',
      'Sky filled': (s.skyCoverage * 100).toFixed(0) + '%',
      'Phase': s.phase
    };
  }
}
