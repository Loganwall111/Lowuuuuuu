/**
 * LensProfiles — how each black hole bends light.
 *
 * Real black holes all lens the same way, but this is a sandbox, so the
 * deflection law itself is a parameter. A profile describes the shape of the
 * distortion, whether a photon ring forms at all, how the shadow is warped,
 * and what colour the light picks up on the way past.
 *
 * The shader reads these as plain numbers, so a fully custom profile is just
 * a struct the player can edit — nothing here is hard-coded per hole.
 */

export type LensMode =
  | 'schwarzschild'   // textbook 1/b deflection
  | 'soft'            // gentle, wide, no sharp ring
  | 'ringless'        // deflection without any photon ring
  | 'sharp'           // violent deflection very close in
  | 'hexagonal'       // alien: six-fold symmetric distortion
  | 'spiral'          // alien: light is dragged around the axis
  | 'shattered'       // alien: the image breaks into facets
  | 'kaleidoscope'    // alien: repeated mirrored copies
  | 'inverted'        // alien: light bends away instead of toward
  | 'rippled'         // alien: concentric shells of deflection
  | 'prismatic'       // alien: wavelength-dependent splitting
  | 'flat';           // no lensing at all, just a dark disc

export interface LensProfile {
  mode: LensMode;
  name: string;
  glyph: string;
  blurb: string;
  /** Overall deflection multiplier. */
  strength: number;
  /** How quickly deflection falls off with distance; 1 = physical 1/b. */
  falloff: number;
  /** Photon-ring brightness. 0 means no ring at all. */
  ring: number;
  /** Ring radius as a multiple of the horizon. */
  ringRadius: number;
  /** Angular symmetry for alien modes: 0 = none, 6 = six-fold, etc. */
  symmetry: number;
  /** Depth of the angular modulation, 0..1. */
  distortion: number;
  /** Rotational drag applied to the image, in radians at the horizon. */
  twist: number;
  /** Chromatic separation; splits the image by wavelength. */
  chroma: number;
  /** Tint picked up by lensed light. */
  tint: [number, number, number];
  /** Shadow edge softness. */
  softness: number;
}

/** Numeric encoding of the mode, sent to the shader. Order is load-bearing. */
export const LENS_MODE_ID: Record<LensMode, number> = {
  schwarzschild: 0,
  soft: 1,
  ringless: 2,
  sharp: 3,
  hexagonal: 4,
  spiral: 5,
  shattered: 6,
  kaleidoscope: 7,
  inverted: 8,
  rippled: 9,
  prismatic: 10,
  flat: 11
};

export const LENS_PROFILES: Record<LensMode, LensProfile> = {
  schwarzschild: {
    mode: 'schwarzschild', name: 'Schwarzschild', glyph: '⚫',
    blurb: 'Textbook general relativity. A clean Einstein ring.',
    strength: 1.0, falloff: 1.0, ring: 1.0, ringRadius: 1.5,
    symmetry: 0, distortion: 0, twist: 0, chroma: 0,
    tint: [1, 1, 1], softness: 0.04
  },
  soft: {
    mode: 'soft', name: 'Soft Halo', glyph: '🌫',
    blurb: 'Wide, gentle bending. The ring smears into a halo.',
    strength: 0.75, falloff: 0.55, ring: 0.35, ringRadius: 2.2,
    symmetry: 0, distortion: 0, twist: 0, chroma: 0,
    tint: [1, 0.95, 0.88], softness: 0.35
  },
  ringless: {
    mode: 'ringless', name: 'Ringless', glyph: '⭕',
    blurb: 'Light bends, but no photon ring ever forms. Just a void.',
    strength: 0.9, falloff: 1.2, ring: 0.0, ringRadius: 0,
    symmetry: 0, distortion: 0, twist: 0, chroma: 0,
    tint: [0.85, 0.88, 1], softness: 0.12
  },
  sharp: {
    mode: 'sharp', name: 'Razor Edge', glyph: '🔪',
    blurb: 'Almost no bending until the very edge, then violent deflection.',
    strength: 1.6, falloff: 2.4, ring: 1.5, ringRadius: 1.25,
    symmetry: 0, distortion: 0, twist: 0, chroma: 0,
    tint: [1, 1, 1], softness: 0.008
  },
  hexagonal: {
    mode: 'hexagonal', name: 'Hexagonal', glyph: '⬢',
    blurb: 'Alien geometry: the shadow has six sides and six bright spurs.',
    strength: 1.1, falloff: 1.0, ring: 0.9, ringRadius: 1.6,
    symmetry: 6, distortion: 0.45, twist: 0, chroma: 0.1,
    tint: [0.8, 1, 0.9], softness: 0.03
  },
  spiral: {
    mode: 'spiral', name: 'Spiral Drag', glyph: '🌀',
    blurb: 'Light is wound around the axis before it escapes.',
    strength: 1.25, falloff: 0.95, ring: 0.8, ringRadius: 1.7,
    symmetry: 0, distortion: 0.3, twist: 2.6, chroma: 0.05,
    tint: [1, 0.85, 0.95], softness: 0.06
  },
  shattered: {
    mode: 'shattered', name: 'Shattered', glyph: '🪞',
    blurb: 'The lensed image breaks into hard angular facets.',
    strength: 1.15, falloff: 1.1, ring: 0.5, ringRadius: 1.45,
    symmetry: 11, distortion: 0.85, twist: 0.4, chroma: 0.25,
    tint: [0.9, 0.95, 1], softness: 0.005
  },
  kaleidoscope: {
    mode: 'kaleidoscope', name: 'Kaleidoscope', glyph: '🔮',
    blurb: 'Reality repeats around the hole in mirrored wedges.',
    strength: 1.3, falloff: 0.9, ring: 1.1, ringRadius: 1.8,
    symmetry: 8, distortion: 1.0, twist: 1.2, chroma: 0.4,
    tint: [1, 0.8, 1], softness: 0.02
  },
  inverted: {
    mode: 'inverted', name: 'Repulsive', glyph: '🔃',
    blurb: 'Light is pushed away. The sky bulges outward instead of pinching.',
    strength: -1.0, falloff: 1.0, ring: 0.3, ringRadius: 2.6,
    symmetry: 0, distortion: 0, twist: 0, chroma: 0,
    tint: [0.85, 0.9, 1], softness: 0.1
  },
  rippled: {
    mode: 'rippled', name: 'Rippled Shells', glyph: '💠',
    blurb: 'Concentric shells of alternating deflection, like a stone in water.',
    strength: 1.05, falloff: 1.0, ring: 0.7, ringRadius: 1.55,
    symmetry: 0, distortion: 0.6, twist: 0, chroma: 0.15,
    tint: [0.85, 0.95, 1], softness: 0.05
  },
  prismatic: {
    mode: 'prismatic', name: 'Prismatic', glyph: '🌈',
    blurb: 'Each wavelength bends differently. The stars smear into spectra.',
    strength: 1.1, falloff: 1.0, ring: 0.85, ringRadius: 1.5,
    symmetry: 0, distortion: 0.2, twist: 0.3, chroma: 1.0,
    tint: [1, 1, 1], softness: 0.04
  },
  flat: {
    mode: 'flat', name: 'No Lensing', glyph: '⬛',
    blurb: 'Gravity without optics. A plain hole punched in the sky.',
    strength: 0.0, falloff: 1.0, ring: 0.0, ringRadius: 0,
    symmetry: 0, distortion: 0, twist: 0, chroma: 0,
    tint: [1, 1, 1], softness: 0.02
  }
};

export const LENS_ORDER: LensMode[] = [
  'schwarzschild', 'soft', 'ringless', 'sharp', 'rippled', 'prismatic',
  'hexagonal', 'spiral', 'shattered', 'kaleidoscope', 'inverted', 'flat'
];

/** Alien modes, for the "surprise me" generator. */
export const ALIEN_MODES: LensMode[] = [
  'hexagonal', 'spiral', 'shattered', 'kaleidoscope', 'inverted',
  'rippled', 'prismatic'
];

/** A deep copy, so a hole can freely mutate its own profile. */
export function cloneProfile(p: LensProfile): LensProfile {
  return { ...p, tint: [p.tint[0], p.tint[1], p.tint[2]] };
}

/** Every field a player can tune, with safe bounds for the UI. */
export const LENS_FIELDS: Array<{
  key: keyof LensProfile; label: string; min: number; max: number; step: number;
}> = [
  { key: 'strength',   label: 'Lens Strength',   min: -3,  max: 4,   step: 0.05 },
  { key: 'falloff',    label: 'Falloff',         min: 0.2, max: 3.5, step: 0.05 },
  { key: 'ring',       label: 'Photon Ring',     min: 0,   max: 3,   step: 0.05 },
  { key: 'ringRadius', label: 'Ring Radius',     min: 0,   max: 4,   step: 0.05 },
  { key: 'symmetry',   label: 'Symmetry Folds',  min: 0,   max: 16,  step: 1    },
  { key: 'distortion', label: 'Distortion',      min: 0,   max: 1.5, step: 0.02 },
  { key: 'twist',      label: 'Frame Twist',     min: -6,  max: 6,   step: 0.05 },
  { key: 'chroma',     label: 'Chromatic Split', min: 0,   max: 2,   step: 0.02 },
  { key: 'softness',   label: 'Edge Softness',   min: 0,   max: 0.6, step: 0.005 }
];

/** Clamps a profile into the ranges the shader can handle. */
export function sanitizeProfile(p: LensProfile): LensProfile {
  const c = cloneProfile(p);
  for (const f of LENS_FIELDS) {
    const v = c[f.key] as number;
    const n = Number.isFinite(v) ? Math.max(f.min, Math.min(f.max, v)) : f.min;
    (c as unknown as Record<string, number>)[f.key as string] = n;
  }
  // Math.min(1, NaN) is NaN, so a non-finite channel would slip straight
  // through a naive clamp. Check finiteness first.
  const chan = (v: number): number =>
    Number.isFinite(v) ? Math.max(0, Math.min(1, v)) : 0.5;
  c.tint = [chan(c.tint[0]), chan(c.tint[1]), chan(c.tint[2])];
  // never let the tint go fully black, or lensed light disappears entirely
  if (c.tint[0] + c.tint[1] + c.tint[2] < 0.15) c.tint = [0.4, 0.45, 0.5];
  return c;
}

/** Builds a random alien profile — no two holes need look alike. */
export function randomAlienProfile(rand: () => number = Math.random): LensProfile {
  const mode = ALIEN_MODES[Math.floor(rand() * ALIEN_MODES.length) % ALIEN_MODES.length];
  const base = cloneProfile(LENS_PROFILES[mode]);
  const jitter = (v: number, amt: number) => v + (rand() - 0.5) * amt;
  base.strength = jitter(base.strength, 1.2);
  base.falloff = Math.max(0.25, jitter(base.falloff, 0.8));
  base.ring = Math.max(0, jitter(base.ring, 1.0));
  base.ringRadius = Math.max(0, jitter(base.ringRadius, 0.9));
  base.symmetry = Math.max(0, Math.round(jitter(base.symmetry, 8)));
  base.distortion = Math.max(0, jitter(base.distortion, 0.6));
  base.twist = jitter(base.twist, 3.5);
  base.chroma = Math.max(0, jitter(base.chroma, 0.7));
  base.tint = [
    0.45 + rand() * 0.55,
    0.45 + rand() * 0.55,
    0.45 + rand() * 0.55
  ];
  base.name = 'Anomalous ' + base.name;
  return sanitizeProfile(base);
}

export function describeProfile(p: LensProfile): Record<string, string> {
  return {
    'Lens': p.glyph + ' ' + p.name,
    'Mode': p.mode,
    'Strength': p.strength.toFixed(2) + '×',
    'Photon ring': p.ring <= 0 ? 'none' : p.ring.toFixed(2) + '×',
    'Symmetry': p.symmetry > 0 ? p.symmetry + '-fold' : 'radial',
    'Twist': p.twist.toFixed(2) + ' rad',
    'Chromatic': p.chroma <= 0 ? 'none' : p.chroma.toFixed(2)
  };
}
