/**
 * BlackHoleTypes — the taxonomy of singularities.
 *
 * Each type carries its own physics parameters, which feed the same geodesic
 * integrator the renderer already uses. The differences are real: a Kerr hole
 * drags frames and flattens its shadow, a charged hole has two horizons, a
 * primordial hole is tiny and evaporates, and a wormhole mouth has no horizon
 * at all so light passes through instead of being captured.
 */

export type HoleKind =
  | 'schwarzschild' | 'kerr' | 'extremal-kerr' | 'charged'
  | 'primordial' | 'supermassive' | 'intermediate' | 'binary'
  | 'naked' | 'wormhole' | 'white' | 'quasar';

export interface BlackHoleType {
  kind: HoleKind;
  name: string;
  glyph: string;
  blurb: string;
  /** Solar masses; drives the Schwarzschild radius. */
  mass: number;
  /** Dimensionless spin a* in [0,1]; 1 is maximal. */
  spin: number;
  /** Dimensionless charge Q in [0,1]. */
  charge: number;
  /** Multiplier on the deflection integrator. */
  lensStrength: number;
  /** Accretion disc brightness, 0 = no disc. */
  discBrightness: number;
  /** Inner disc temperature tint. */
  discTint: [number, number, number];
  /** Relativistic jets along the spin axis. */
  jets: boolean;
  /** Photon-sphere glow intensity. */
  photonRing: number;
  /** If false, light is not captured (wormhole mouths, naked singularities). */
  hasHorizon: boolean;
  /** Hawking evaporation rate, only meaningful for tiny holes. */
  evaporation: number;
  note: string;
}

export const BLACK_HOLES: Record<HoleKind, BlackHoleType> = {
  schwarzschild: {
    kind: 'schwarzschild', name: 'Schwarzschild', glyph: '⚫',
    blurb: 'The classic. No spin, no charge, a perfectly circular shadow.',
    mass: 10, spin: 0, charge: 0, lensStrength: 1.0, discBrightness: 0.8,
    discTint: [1.0, 0.72, 0.35], jets: false, photonRing: 1.0,
    hasHorizon: true, evaporation: 0,
    note: 'Shadow is exactly 2.6 times the horizon diameter.'
  },
  kerr: {
    kind: 'kerr', name: 'Kerr (Rotating)', glyph: '🌀',
    blurb: 'Spinning. Frame dragging skews the shadow and brightens one side.',
    mass: 15, spin: 0.7, charge: 0, lensStrength: 1.25, discBrightness: 1.0,
    discTint: [1.0, 0.78, 0.42], jets: true, photonRing: 1.2,
    hasHorizon: true, evaporation: 0,
    note: 'Doppler beaming makes the approaching side far brighter.'
  },
  'extremal-kerr': {
    kind: 'extremal-kerr', name: 'Extremal Kerr', glyph: '💫',
    blurb: 'Spinning as fast as physics allows. The shadow goes lopsided.',
    mass: 20, spin: 0.998, charge: 0, lensStrength: 1.6, discBrightness: 1.3,
    discTint: [0.85, 0.9, 1.0], jets: true, photonRing: 1.6,
    hasHorizon: true, evaporation: 0,
    note: 'At a*=1 the horizon and the photon sphere nearly touch.'
  },
  charged: {
    kind: 'charged', name: 'Reissner-Nordström', glyph: '⚡',
    blurb: 'Electrically charged, so it has two horizons nested inside it.',
    mass: 12, spin: 0, charge: 0.8, lensStrength: 0.85, discBrightness: 0.5,
    discTint: [0.6, 0.8, 1.0], jets: false, photonRing: 0.9,
    hasHorizon: true, evaporation: 0,
    note: 'Between the two horizons, space and time swap roles twice.'
  },
  primordial: {
    kind: 'primordial', name: 'Primordial', glyph: '🔹',
    blurb: 'Older than stars and no bigger than an atom. It is evaporating.',
    mass: 0.0001, spin: 0.2, charge: 0, lensStrength: 0.35, discBrightness: 0.15,
    discTint: [0.7, 0.95, 1.0], jets: false, photonRing: 0.4,
    hasHorizon: true, evaporation: 1.0,
    note: 'Hawking radiation is fierce; it will detonate rather than fade.'
  },
  supermassive: {
    kind: 'supermassive', name: 'Supermassive', glyph: '🕳',
    blurb: 'Millions of suns. Crossing the horizon would be uneventful.',
    mass: 4000000, spin: 0.55, charge: 0, lensStrength: 2.4, discBrightness: 1.5,
    discTint: [1.0, 0.85, 0.6], jets: true, photonRing: 1.4,
    hasHorizon: true, evaporation: 0,
    note: 'Tidal forces at the horizon are gentler than Earth gravity.'
  },
  intermediate: {
    kind: 'intermediate', name: 'Intermediate', glyph: '⬤',
    blurb: 'The awkward middle child. A few thousand solar masses.',
    mass: 5000, spin: 0.4, charge: 0, lensStrength: 1.5, discBrightness: 0.9,
    discTint: [1.0, 0.68, 0.4], jets: false, photonRing: 1.1,
    hasHorizon: true, evaporation: 0,
    note: 'Rare, and nobody fully agrees how they form.'
  },
  binary: {
    kind: 'binary', name: 'Binary Pair', glyph: '⚭',
    blurb: 'Two holes orbiting each other, lensing each other as they go.',
    mass: 30, spin: 0.6, charge: 0, lensStrength: 1.35, discBrightness: 1.1,
    discTint: [1.0, 0.6, 0.5], jets: true, photonRing: 1.3,
    hasHorizon: true, evaporation: 0,
    note: 'They will spiral in and merge, ringing spacetime like a bell.'
  },
  naked: {
    kind: 'naked', name: 'Naked Singularity', glyph: '✴',
    blurb: 'Spin so extreme the horizon vanished. You can see the singularity.',
    mass: 18, spin: 1.4, charge: 0.3, lensStrength: 2.0, discBrightness: 1.2,
    discTint: [1.0, 0.95, 0.85], jets: true, photonRing: 0.6,
    hasHorizon: false, evaporation: 0,
    note: 'Almost certainly forbidden by cosmic censorship. Enjoy it anyway.'
  },
  wormhole: {
    kind: 'wormhole', name: 'Traversable Wormhole', glyph: '🌐',
    blurb: 'A throat, not a pit. Light goes through and out the far side.',
    mass: 8, spin: 0, charge: 0, lensStrength: 1.8, discBrightness: 0.3,
    discTint: [0.5, 0.85, 1.0], jets: false, photonRing: 0.8,
    hasHorizon: false, evaporation: 0,
    note: 'Held open by exotic matter. You can see the other sky through it.'
  },
  white: {
    kind: 'white', name: 'White Hole', glyph: '⚪',
    blurb: 'A black hole running backwards. Nothing can fall in.',
    mass: 14, spin: 0.3, charge: 0, lensStrength: -1.1, discBrightness: 1.6,
    discTint: [1.0, 1.0, 0.95], jets: true, photonRing: 1.5,
    hasHorizon: false, evaporation: 0,
    note: 'Matter is expelled. Light is pushed away instead of pulled in.'
  },
  quasar: {
    kind: 'quasar', name: 'Quasar', glyph: '🔆',
    blurb: 'Feeding so hard it outshines its entire host galaxy.',
    mass: 900000000, spin: 0.9, charge: 0, lensStrength: 2.8, discBrightness: 3.0,
    discTint: [0.85, 0.92, 1.0], jets: true, photonRing: 1.8,
    hasHorizon: true, evaporation: 0,
    note: 'The jets are visible from billions of light years away.'
  }
};

export const HOLE_ORDER: HoleKind[] = [
  'schwarzschild', 'kerr', 'extremal-kerr', 'charged', 'intermediate',
  'supermassive', 'quasar', 'binary', 'primordial', 'naked', 'wormhole', 'white'
];

/** Schwarzschild radius in simulation units (normalised, not metres). */
export function horizonRadius(t: BlackHoleType): number {
  // log-scaled so a 9-order-of-magnitude mass range stays on screen
  const base = Math.log10(Math.max(t.mass, 1e-6) + 1) * 0.9 + 1;
  if (!t.hasHorizon) return base * 0.55;
  // charge and spin both shrink the horizon: r = M + sqrt(M^2 - a^2 - Q^2)
  const a = Math.min(t.spin, 1);
  const q = Math.min(t.charge, 1);
  const disc = 1 - a * a - q * q;
  const shrink = disc > 0 ? (1 + Math.sqrt(disc)) / 2 : 0.5;
  return base * shrink;
}

/** Inner-stable-circular-orbit radius; the accretion disc starts here. */
export function iscoRadius(t: BlackHoleType): number {
  const rs = horizonRadius(t);
  // prograde ISCO shrinks from 3rs toward 0.5rs as spin approaches 1
  const a = Math.min(Math.max(t.spin, 0), 1);
  return rs * (3 - 2.5 * a * a);
}

/** Photon sphere radius. */
export function photonSphere(t: BlackHoleType): number {
  const rs = horizonRadius(t);
  const a = Math.min(Math.max(t.spin, 0), 1);
  return rs * (1.5 - 0.5 * a);
}

/** How strongly this hole bends light, fed to the geodesic integrator. */
export function deflectionScale(t: BlackHoleType): number {
  return t.lensStrength * (1 + t.spin * 0.35) * (1 - t.charge * 0.25);
}

export function describeHole(t: BlackHoleType): Record<string, string> {
  const fmtMass = (m: number) =>
    m >= 1e6 ? (m / 1e6).toFixed(1) + ' million M☉'
    : m >= 1000 ? (m / 1000).toFixed(1) + ' thousand M☉'
    : m < 0.01 ? m.toExponential(1) + ' M☉'
    : m.toFixed(1) + ' M☉';
  return {
    'Type': t.glyph + ' ' + t.name,
    'Mass': fmtMass(t.mass),
    'Spin a*': t.spin.toFixed(3) + (t.spin > 1 ? ' (over-extremal)' : ''),
    'Charge Q': t.charge.toFixed(2),
    'Horizon': t.hasHorizon ? horizonRadius(t).toFixed(2) : 'none',
    'Photon sphere': photonSphere(t).toFixed(2),
    'ISCO': iscoRadius(t).toFixed(2),
    'Jets': t.jets ? 'yes' : 'no',
    'Note': t.note
  };
}
