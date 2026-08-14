/**
 * HoleProfiles — the physical character of one black hole.
 *
 * `LensProfile` already describes how a hole bends light. This describes the
 * hole itself: how big the shadow is, whether it has an accretion disk at
 * all, how thick and how hot that disk is, how fast it spins, how far it is
 * tilted, and how strongly relativistic beaming shows.
 *
 * Two things drove this.
 *
 * 1. Every hole in the universe looked identical, because the nearby-hole
 *    renderer built the same three meshes (a black sphere, an orange torus
 *    and a glow) for all of them. Real holes differ enormously, and some
 *    have no disk at all — a starved hole is just a shadow against the sky.
 *
 * 2. The disk was an infinitely thin plane, so edge-on it vanished to a
 *    line. An accretion disk is a thick, turbulent torus of gas.
 *
 * Everything here is derived from the hole's seed, so a given hole is always
 * the same hole, and there is no table of hand-authored types to run out of.
 */

/** Broad families, used for naming and for sensible parameter ranges. */
export type HoleClass =
  | 'schwarzschild'   // classic, moderate disk
  | 'kerr'            // fast spin, hot compressed disk
  | 'starved'         // no disk at all: a shadow and nothing else
  | 'blazar'          // furious, thick, very bright
  | 'ancient'         // huge shadow, cool dim disk
  | 'shrouded'        // thick choking torus, dim core
  | 'exotic';         // anomalous geometry, strange tints

export const HOLE_CLASSES: HoleClass[] = [
  'schwarzschild', 'kerr', 'starved', 'blazar', 'ancient', 'shrouded', 'exotic'
];

export interface HoleProfile {
  /** Which family this hole belongs to. */
  cls: HoleClass;
  /** Human-readable, for the HUD. */
  label: string;

  /**
   * Disk luminosity. Zero means this hole has NO accretion disk: the
   * raymarcher skips the disk entirely and you see only the shadow and the
   * lensed sky behind it.
   */
  diskBright: number;
  /** Inner edge, in Schwarzschild radii. */
  diskInner: number;
  /** Outer edge, in Schwarzschild radii. */
  diskOuter: number;
  /**
   * Vertical half-thickness of the disk, in Schwarzschild radii.
   *
   * The old shader tested for a single plane crossing, which is a sheet of
   * zero thickness — edge-on it disappeared. This gives the disk a real
   * volume to march through.
   */
  diskThickness: number;
  /** Orbital speed multiplier. */
  spin: number;
  /** Tilt of the disk plane, radians. */
  diskTilt: number;
  /** Relativistic beaming strength; 0 disables Doppler shading. */
  doppler: number;
  /** Turbulence in the disk, 0..1. */
  turbulence: number;
  /** Disk colour temperature bias: <1 cooler/redder, >1 hotter/bluer. */
  temperature: number;
}

/** True when this hole has no accretion disk whatsoever. */
export function isDiskless(p: HoleProfile): boolean {
  return p.diskBright <= 0;
}

/** Deterministic hash-based RNG so a seed always yields the same hole. */
function rng(seed: number): () => number {
  let s = (seed >>> 0) || 1;
  return () => {
    s ^= s << 13; s >>>= 0;
    s ^= s >> 17;
    s ^= s << 5; s >>>= 0;
    return s / 4294967296;
  };
}

const CLASS_LABEL: Record<HoleClass, string> = {
  schwarzschild: 'Schwarzschild',
  kerr: 'Kerr',
  starved: 'Starved',
  blazar: 'Blazar',
  ancient: 'Ancient',
  shrouded: 'Shrouded',
  exotic: 'Exotic'
};

/**
 * Weighted class draw.
 *
 * Diskless holes are deliberately common enough to be encountered (the user
 * asked for holes with no disk at all) without being the norm.
 */
const CLASS_WEIGHTS: Array<[HoleClass, number]> = [
  ['schwarzschild', 0.26],
  ['kerr', 0.20],
  ['starved', 0.14],
  ['blazar', 0.12],
  ['ancient', 0.12],
  ['shrouded', 0.10],
  ['exotic', 0.06]
];

function pickClass(r: () => number): HoleClass {
  const x = r();
  let acc = 0;
  for (const [cls, w] of CLASS_WEIGHTS) {
    acc += w;
    if (x < acc) return cls;
  }
  return 'schwarzschild';
}

/**
 * Builds the profile for one hole.
 *
 * Deterministic in `seed`: the same hole always looks the same, however many
 * times you fly away and come back.
 */
export function holeProfile(seed: number): HoleProfile {
  const r = rng(seed);
  const cls = pickClass(r);
  const jit = (base: number, spread: number) => base + (r() - 0.5) * spread;

  // Defaults, then per-class character. Ranges are wide on purpose: the point
  // is that no two holes read the same.
  let diskBright = jit(1.25, 0.7);
  let diskInner = jit(3.0, 0.9);
  let diskOuter = jit(14.0, 7.0);
  let diskThickness = jit(0.55, 0.4);
  let spin = jit(1.0, 0.9);
  let doppler = jit(1.0, 0.7);
  let turbulence = jit(0.45, 0.5);
  let temperature = jit(1.0, 0.5);

  switch (cls) {
    case 'kerr':
      // Fast spin drags the inner edge inward and compresses the disk.
      diskInner = jit(2.3, 0.4);
      diskThickness = jit(0.28, 0.16);
      spin = jit(2.3, 0.9);
      doppler = jit(1.8, 0.6);
      temperature = jit(1.5, 0.4);
      break;
    case 'starved':
      // No disk at all. Just a shadow and the lensed sky behind it.
      diskBright = 0;
      diskThickness = 0;
      turbulence = 0;
      doppler = 0;
      break;
    case 'blazar':
      diskBright = jit(2.6, 0.9);
      diskOuter = jit(20.0, 8.0);
      diskThickness = jit(1.5, 0.7);
      turbulence = jit(0.85, 0.3);
      temperature = jit(1.7, 0.5);
      break;
    case 'ancient':
      diskBright = jit(0.45, 0.3);
      diskOuter = jit(26.0, 10.0);
      diskThickness = jit(0.9, 0.5);
      spin = jit(0.35, 0.3);
      temperature = jit(0.55, 0.25);
      break;
    case 'shrouded':
      diskBright = jit(0.9, 0.4);
      diskInner = jit(4.5, 1.2);
      diskThickness = jit(2.4, 1.0);
      turbulence = jit(0.9, 0.25);
      temperature = jit(0.7, 0.3);
      break;
    case 'exotic':
      diskBright = r() < 0.3 ? 0 : jit(1.6, 1.4);
      diskInner = jit(3.6, 2.4);
      diskOuter = jit(18.0, 14.0);
      diskThickness = jit(1.2, 1.6);
      spin = jit(1.6, 2.6);
      doppler = jit(1.4, 1.6);
      turbulence = jit(0.7, 0.6);
      temperature = jit(1.2, 1.4);
      break;
    default:
      break;
  }

  // Clamp into ranges the shader can actually integrate. diskBright is
  // allowed to reach exactly zero, because "no disk" is a real hole.
  diskBright = Math.max(0, diskBright);
  diskInner = Math.max(2.05, diskInner);
  diskOuter = Math.max(diskInner + 1.5, diskOuter);
  diskThickness = Math.max(0, Math.min(4, diskThickness));
  spin = Math.max(0, Math.min(4, spin));
  doppler = Math.max(0, Math.min(3, doppler));
  turbulence = Math.max(0, Math.min(1, turbulence));
  temperature = Math.max(0.2, Math.min(2.5, temperature));

  // A disk with no brightness has no geometry either; keep the two agreeing
  // so nothing downstream has to special-case it twice.
  if (diskBright <= 0) diskThickness = 0;

  return {
    cls,
    label: CLASS_LABEL[cls],
    diskBright,
    diskInner,
    diskOuter,
    diskThickness,
    spin,
    diskTilt: r() * Math.PI * 2,
    doppler,
    turbulence,
    temperature
  };
}

/** Short description for the HUD. */
export function describeHole(p: HoleProfile): Record<string, string> {
  return {
    'Class': p.label,
    'Accretion disk': isDiskless(p)
      ? 'none — starved'
      : p.diskBright.toFixed(2) + '× luminosity',
    'Disk thickness': p.diskThickness <= 0
      ? '—' : p.diskThickness.toFixed(2) + ' rs',
    'Spin': p.spin.toFixed(2) + '×'
  };
}
