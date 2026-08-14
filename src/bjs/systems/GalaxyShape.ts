/**
 * GalaxyShape — the structure behind the far starfield.
 *
 * A uniform shell of stars is the reason the outer sky "drops into a
 * repeating pattern": there is no structure in it, so every direction looks
 * statistically identical and the eye reads it as wallpaper. Real skies do
 * not look like that. Ours has a bright band across it, because we sit
 * inside a flattened disc and are looking the long way through it.
 *
 * So this module does not decorate a sphere. It builds an actual galaxy in
 * its own coordinate space - logarithmic arms, a central bulge, a thin disc,
 * a sparse halo - places the observer off-centre inside the disc, and then
 * projects what they would see onto the background shell. The band, the
 * bright core in one direction, and the thinning toward the galactic poles
 * all fall out of that geometry rather than being drawn in by hand.
 *
 * Engine-free on purpose: this is arithmetic, and arithmetic should be
 * testable without a GPU.
 */

/** Where a generated star belongs structurally. */
export type StarClass = 'arm' | 'bulge' | 'halo';

export interface GalaxyConfig {
  /** Number of spiral arms. Two is Milky-Way-like; four is common too. */
  arms: number;
  /**
   * Winding tightness. The angle a star sits at grows with the logarithm of
   * its radius, so this is the constant in that relation: small values wind
   * loosely, large values coil the arms tightly around the core.
   */
  armFactor: number;
  /** Angular scatter around the arm centre line, radians. */
  armSpread: number;
  /** Inner and outer radius of the disc, galaxy units. */
  innerBound: number;
  outerBound: number;
  /**
   * Disc half-thickness as a fraction of radius. Real discs are
   * extraordinarily thin - this is what bundles the stars into a plane
   * instead of a fat torus.
   */
  thickness: number;
  /** Radius of the central bulge. */
  bulgeRadius: number;
  /** Share of stars in the bulge and in the halo; the rest go to the arms. */
  bulgeFraction: number;
  haloFraction: number;
}

export const MILKY_WAY: GalaxyConfig = {
  arms: 4,
  armFactor: 4.2,
  // Tight enough that the arms read as arms. At 0.34 the scatter was two
  // thirds of the gap between arms, so they smeared into a uniform disc
  // with only a 1.7x overdensity - structure a viewer would never see.
  armSpread: 0.16,
  innerBound: 900,
  outerBound: 16000,
  thickness: 0.05,
  bulgeRadius: 2200,
  bulgeFraction: 0.16,
  haloFraction: 0.13
};

/**
 * The defining relation of a logarithmic spiral: angle grows with ln(radius).
 *
 * Every arm is the same curve rotated by a whole fraction of a turn, which
 * is why arms are evenly spaced but never parallel.
 */
export function logSpiralAngle(
  radius: number, innerBound: number, armFactor: number, armOffset: number
): number {
  const r = Math.max(radius, 1e-6);
  const inner = Math.max(innerBound, 1e-6);
  return armFactor * Math.log(r / inner) + armOffset;
}

/** Normally distributed sample from two uniforms (Box-Muller). */
export function gaussian(u1: number, u2: number): number {
  const a = Math.max(u1, 1e-9);
  return Math.sqrt(-2 * Math.log(a)) * Math.cos(2 * Math.PI * u2);
}

export interface GalaxyStar {
  x: number; y: number; z: number;
  kind: StarClass;
  /** Relative brightness, 0..1. The core is meant to blaze. */
  bright: number;
}

/**
 * One star, in galaxy space, with the galactic centre at the origin and the
 * disc lying in the XZ plane.
 */
export function galaxyStar(rand: () => number, cfg: GalaxyConfig = MILKY_WAY): GalaxyStar {
  const arms = Math.max(1, Math.floor(cfg.arms));
  const roll = rand();

  if (roll < cfg.bulgeFraction) {
    // Bulge: a dense, roughly spheroidal core. The cube of a uniform gives a
    // steep central concentration, which is what makes it read as a blaze
    // rather than a ball.
    const r = cfg.bulgeRadius * Math.pow(rand(), 2.2);
    const cosT = 2 * rand() - 1;
    const sinT = Math.sqrt(Math.max(0, 1 - cosT * cosT));
    const phi = 2 * Math.PI * rand();
    return {
      x: r * sinT * Math.cos(phi),
      // Even the bulge is squashed along the galactic axis.
      y: r * cosT * 0.62,
      z: r * sinT * Math.sin(phi),
      kind: 'bulge',
      bright: 0.55 + rand() * 0.45
    };
  }

  if (roll < cfg.bulgeFraction + cfg.haloFraction) {
    // Halo: old, sparse, spherical. Without it the sky outside the band
    // would be completely empty, which looks like a rendering failure
    // rather than like intergalactic space.
    const r = cfg.innerBound + (cfg.outerBound * 1.15 - cfg.innerBound) * Math.cbrt(rand());
    const cosT = 2 * rand() - 1;
    const sinT = Math.sqrt(Math.max(0, 1 - cosT * cosT));
    const phi = 2 * Math.PI * rand();
    return {
      x: r * sinT * Math.cos(phi),
      y: r * cosT,
      z: r * sinT * Math.sin(phi),
      kind: 'halo',
      bright: 0.12 + rand() * 0.3
    };
  }

  // Disc star on a spiral arm.
  //
  // Radius is drawn with a square-root bias so the inner disc is denser,
  // matching the exponential falloff of a real disc closely enough that the
  // arms crowd near the core and fray at the rim.
  const t = Math.sqrt(rand());
  const radius = cfg.innerBound + (cfg.outerBound - cfg.innerBound) * t;

  const armIndex = Math.floor(rand() * arms) % arms;
  const armOffset = (2 * Math.PI * armIndex) / arms;
  const base = logSpiralAngle(radius, cfg.innerBound, cfg.armFactor, armOffset);

  // Scatter across the arm, wider at the rim where arms visibly fray.
  const spread = cfg.armSpread * (0.55 + 0.9 * t);
  const angle = base + gaussian(rand(), rand()) * spread;

  // Thin disc: Gaussian in height with a scale proportional to radius.
  const height = gaussian(rand(), rand()) * radius * cfg.thickness;

  return {
    x: radius * Math.cos(angle),
    y: height,
    z: radius * Math.sin(angle),
    kind: 'arm',
    bright: 0.22 + Math.pow(rand(), 1.8) * 0.78
  };
}

/** Where the observer sits inside the disc, in galaxy units. */
export function observerPosition(cfg: GalaxyConfig = MILKY_WAY): [number, number, number] {
  // Well out along the disc, like the Sun in the Orion Spur. Being off-centre
  // is what puts the bright core in ONE direction instead of all of them.
  return [cfg.outerBound * 0.52, 0, 0];
}

export interface ProjectedStar {
  x: number; y: number; z: number;
  kind: StarClass;
  bright: number;
  /** Distance from the observer in galaxy units, before remapping. */
  distance: number;
}

/**
 * Projects a galaxy-space star onto the background shell.
 *
 * The shell only carries direction and a depth ordering - the actual galaxy
 * is far larger than the camera's far plane, so true distances are remapped
 * into the shell's radius band. Nearer stars land on the inner shell radius
 * and so parallax the most, which preserves the depth cue that made the
 * layered sky work in the first place.
 */
export function projectToShell(
  star: GalaxyStar,
  observer: [number, number, number],
  inner: number,
  outer: number,
  cfg: GalaxyConfig = MILKY_WAY
): ProjectedStar {
  const dx = star.x - observer[0];
  const dy = star.y - observer[1];
  const dz = star.z - observer[2];
  const d = Math.sqrt(dx * dx + dy * dy + dz * dz);

  if (!(d > 1e-6)) {
    // Degenerate: the star is on top of the observer. Park it on the inner
    // shell in an arbitrary but finite direction rather than dividing by
    // zero and spraying NaN through the vertex buffer.
    return { x: inner, y: 0, z: 0, kind: star.kind, bright: star.bright, distance: 0 };
  }

  // Normalised depth across the galaxy's full extent, compressed with a
  // square root so the near half of the disc does not collapse onto the
  // inner shell.
  const span = Math.max(cfg.outerBound * 2.2, 1e-6);
  const k = Math.sqrt(Math.min(1, d / span));
  const r = inner + (outer - inner) * k;

  return {
    x: (dx / d) * r,
    y: (dy / d) * r,
    z: (dz / d) * r,
    kind: star.kind,
    bright: star.bright,
    distance: d
  };
}

// ---------------------------------------------------------------- nebulae

/** Deterministic 3D hash in 0..1. */
function hash3(x: number, y: number, z: number): number {
  let h = Math.imul(x | 0, 374761393) ^ Math.imul(y | 0, 668265263) ^ Math.imul(z | 0, 2147483647);
  h = Math.imul(h ^ (h >>> 13), 1274126177);
  return ((h ^ (h >>> 16)) >>> 0) / 4294967296;
}

const fade = (t: number) => t * t * (3 - 2 * t);
const lerp = (a: number, b: number, t: number) => a + (b - a) * t;

/** Trilinearly interpolated value noise. */
export function valueNoise3(x: number, y: number, z: number): number {
  const xi = Math.floor(x), yi = Math.floor(y), zi = Math.floor(z);
  const xf = fade(x - xi), yf = fade(y - yi), zf = fade(z - zi);

  const c000 = hash3(xi, yi, zi),         c100 = hash3(xi + 1, yi, zi);
  const c010 = hash3(xi, yi + 1, zi),     c110 = hash3(xi + 1, yi + 1, zi);
  const c001 = hash3(xi, yi, zi + 1),     c101 = hash3(xi + 1, yi, zi + 1);
  const c011 = hash3(xi, yi + 1, zi + 1), c111 = hash3(xi + 1, yi + 1, zi + 1);

  return lerp(
    lerp(lerp(c000, c100, xf), lerp(c010, c110, xf), yf),
    lerp(lerp(c001, c101, xf), lerp(c011, c111, xf), yf),
    zf
  );
}

/** Several octaves of value noise, for cloud structure rather than mush. */
export function fbm3(x: number, y: number, z: number, octaves = 4): number {
  let sum = 0, amp = 0.5, norm = 0, f = 1;
  for (let i = 0; i < Math.max(1, octaves); i++) {
    sum += valueNoise3(x * f, y * f, z * f) * amp;
    norm += amp;
    amp *= 0.5;
    f *= 2.07;      // not exactly 2, to avoid axis-aligned repetition
  }
  return norm > 0 ? sum / norm : 0;
}

/**
 * Gas density at a point in galaxy space, 0..1.
 *
 * Interstellar gas is not spread evenly through a galaxy: it collapses into
 * the plane and piles up along the arms, which is exactly where star
 * formation happens. Density therefore peaks in the disc and falls to true
 * emptiness above it and beyond the rim, so flying out of the galactic plane
 * genuinely clears the view instead of dimming a uniform fog.
 */
export function nebulaDensity(
  x: number, y: number, z: number, cfg: GalaxyConfig = MILKY_WAY
): number {
  const radius = Math.sqrt(x * x + z * z);

  // Vertical confinement: gas is thinner than the stars are.
  const scaleHeight = Math.max(radius * cfg.thickness * 0.8, 1e-6);
  const plane = Math.exp(-(y * y) / (2 * scaleHeight * scaleHeight));

  // Radial extent: nothing inside the core, fading out past the rim.
  const inner = 1 - Math.exp(-radius / Math.max(cfg.innerBound, 1e-6));
  const outer = 1 - Math.min(1, Math.max(0, (radius - cfg.outerBound * 0.75) /
    Math.max(cfg.outerBound * 0.45, 1e-6)));

  // Clumping. Scaled so one noise cell spans a sizeable chunk of the disc.
  const s = 1 / Math.max(cfg.outerBound * 0.16, 1e-6);
  const clouds = fbm3(x * s, y * s * 2.4, z * s, 4);

  const d = plane * inner * Math.max(0, outer) * Math.pow(clouds, 1.7) * 2.1;
  return Math.min(1, Math.max(0, d));
}

/**
 * Emission colour for gas at a given density and position.
 *
 * A real emission nebula is not one colour smeared about. It is a handful of
 * distinct ionisation species, each radiating at its own fixed wavelength,
 * and they separate in space because they need different amounts of energy
 * to light up. That separation is the whole reason nebula photographs look
 * the way they do, so it is what this function models.
 *
 *   H-alpha  (656nm) - hydrogen, deep crimson. The bulk of the gas, and the
 *                      colour of the big diffuse arm complexes.
 *   O-III    (501nm) - doubly ionised oxygen, teal-green. Needs hard UV, so
 *                      it only appears near hot young stars.
 *   S-II     (672nm) - sulphur, dull orange-red. Rims and shock fronts.
 *   Reflection       - cold dusty blue where nothing is ionised at all and
 *                      the dust is simply scattering starlight.
 *
 * The previous version interpolated a single warm/cool pair, so every point
 * in the galaxy landed somewhere on one straight line through colour space
 * between (0.62,0.16,0.30) and (0.16,0.13,0.48). Both endpoints are
 * magenta-violet, which is why the mean hue measured (1.00, 0.33, 0.82) and
 * why the gas read as a flat pink wash with no variety in it.
 *
 * Species are chosen by their own noise field at a DIFFERENT frequency to
 * the density field, which is what makes the colour run in long flowing
 * strips along the arms rather than turning over point by point.
 */
export function nebulaColor(
  density: number, x: number, y: number, z: number, cfg: GalaxyConfig = MILKY_WAY
): [number, number, number] {
  const d = Math.min(1, Math.max(0, density));
  const radius = Math.sqrt(x * x + z * z);

  // ---- the ionisation field ----
  // Low frequency along the plane, high frequency vertically: that anisotropy
  // is what stretches the colour into strips that follow the disc instead of
  // breaking it into isotropic blobs.
  const s = 1 / Math.max(cfg.outerBound * 0.28, 1e-6);
  const ion = fbm3(x * s + 31.7, y * s * 5.0, z * s - 12.3, 4);

  // A second, finer field decides where the hard-UV pockets sit. O-III is
  // rare and clustered, so it is gated behind a threshold rather than mixed
  // in everywhere.
  const t2 = 1 / Math.max(cfg.outerBound * 0.09, 1e-6);
  const hot = fbm3(x * t2 - 7.1, y * t2 * 3.0, z * t2 + 19.4, 3);

  // Ionisation needs energy, and the energy comes from the crowded inner
  // disc. Out at the rim the same gas is cold and just scatters light.
  const excitation = Math.exp(-radius / Math.max(cfg.outerBound * 0.55, 1e-6));

  // ---- the emission lines, as actual colours ----
  const HA:   [number, number, number] = [0.95, 0.14, 0.22];   // crimson
  const OIII: [number, number, number] = [0.10, 0.85, 0.72];   // teal
  const SII:  [number, number, number] = [0.90, 0.42, 0.20];   // orange
  const DUST: [number, number, number] = [0.22, 0.30, 0.62];   // cold blue

  // Base: hydrogen almost everywhere, sulphur creeping in where the noise
  // runs high, cold dust taking over past the ionisation front.
  // Thresholds come from the measured distribution of these fields, not from
  // guesswork: averaging octaves pulls fbm toward its mean, so `ion` and
  // `hot` actually span about 0.12..0.88 with a median near 0.50 rather than
  // filling 0..1. Gating at 0.58 against that distribution is why an earlier
  // pass produced literally zero teal points.
  const sulphur = Math.min(1, Math.max(0, (ion - 0.56) * 3.0));
  let r = lerp(HA[0], SII[0], sulphur);
  let g = lerp(HA[1], SII[1], sulphur);
  let b = lerp(HA[2], SII[2], sulphur);

  const cold = 1 - excitation;
  r = lerp(r, DUST[0], cold * 0.8);
  g = lerp(g, DUST[1], cold * 0.8);
  b = lerp(b, DUST[2], cold * 0.8);

  // O-III overrides wherever a hot pocket coincides with real gas. It is the
  // colour that makes a nebula field look photographed rather than tinted,
  // precisely because it is the one that is NOT red.
  const oiii = Math.min(1, Math.max(0, (hot - 0.50) * 3.4))
             * (0.45 + 0.55 * excitation) * 1.25;
  r = lerp(r, OIII[0], Math.min(0.85, oiii));
  g = lerp(g, OIII[1], Math.min(0.85, oiii));
  b = lerp(b, OIII[2], Math.min(0.85, oiii));

  // Brightness follows density, so wisps stay wisps.
  const mag = Math.pow(d, 0.75);
  return [r * mag, g * mag, b * mag];
}

/**
 * Rejection-samples a gas point where the density field is actually thick.
 *
 * Returns null when the attempt lands somewhere empty; the caller retries.
 * Sampling the field directly rather than scattering points evenly and
 * fading them is what keeps the clouds shaped like clouds.
 */
export function sampleNebulaPoint(
  rand: () => number, cfg: GalaxyConfig = MILKY_WAY, threshold = 0.16
): { x: number; y: number; z: number; density: number } | null {
  const radius = cfg.innerBound + (cfg.outerBound - cfg.innerBound) * Math.sqrt(rand());
  const angle = 2 * Math.PI * rand();
  const y = gaussian(rand(), rand()) * radius * cfg.thickness * 1.1;
  const x = radius * Math.cos(angle);
  const z = radius * Math.sin(angle);

  const d = nebulaDensity(x, y, z, cfg);
  if (d < threshold) return null;
  return { x, y, z, density: d };
}
