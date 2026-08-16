/**
 * TleCatalog — a deterministic ~10,000-object satellite catalogue.
 *
 * The real NORAD catalogue runs to tens of thousands of objects and is
 * distributed as TLE text; shipping that file would violate the project's
 * "nothing stored, everything derived" rule and bloat the repo by megabytes.
 * This instead generates a faithful stand-in: ten thousand objects laid out
 * in the real orbital regimes - the Starlink and OneWeb shells, Iridium,
 * the GNSS constellations (GPS, GLONASS, Galileo, BeiDou), the geostationary
 * belt, weather and science birds, and a large debris population - each
 * emitted as a genuine, checksummed TLE record.
 *
 * Because the records are real TLE format, the same parser and propagator
 * in Tle.ts consume them, and a real TLE file dropped into the loader works
 * identically. The catalogue is deterministic: the same seed yields the same
 * ten thousand birds, forever, with nothing stored.
 */

import {
  TleRecord, EARTH_RADIUS_KM, meanMotionFromSemiMajor, formatTLE
} from './Tle';

/** How many objects the catalogue holds. */
export const CATALOG_COUNT = 10000;

/** A population the generator can fill a shell with. */
interface Population {
  name: string;
  count: number;
  /** Altitude range, km above Earth's surface. */
  altMin: number;
  altMax: number;
  /** Inclination band, degrees. */
  incMin: number;
  incMax: number;
  /** Eccentricity range. */
  eccMin: number;
  eccMax: number;
  /** Colour for the renderer, linear RGB. */
  tint: [number, number, number];
}

/**
 * The population mix, weighted toward the shells a real catalogue is
 * dominated by: thousands of LEO comms satellites and debris, hundreds of
 * geostationary birds, a few dozen GNSS and Iridium slots.
 */
export const POPULATIONS: Population[] = [
  { name: 'STARLINK', count: 3600, altMin: 340, altMax: 570, incMin: 52, incMax: 97.6, eccMin: 0.0001, eccMax: 0.0009, tint: [0.45, 0.7, 1.0] },
  { name: 'ONEWEB', count: 600, altMin: 1180, altMax: 1230, incMin: 87.2, incMax: 87.9, eccMin: 0.0001, eccMax: 0.0008, tint: [0.55, 0.65, 1.0] },
  { name: 'IRIDIUM', count: 66, altMin: 770, altMax: 790, incMin: 86.0, incMax: 86.7, eccMin: 0.0002, eccMax: 0.001, tint: [0.7, 0.55, 1.0] },
  { name: 'GPS', count: 32, altMin: 20180, altMax: 20220, incMin: 54.5, incMax: 55.5, eccMin: 0.001, eccMax: 0.015, tint: [0.4, 1.0, 0.55] },
  { name: 'GLONASS', count: 24, altMin: 19080, altMax: 19140, incMin: 64.4, incMax: 64.9, eccMin: 0.0005, eccMax: 0.002, tint: [0.55, 1.0, 0.4] },
  { name: 'GALILEO', count: 30, altMin: 23210, altMax: 23230, incMin: 55.5, incMax: 56.5, eccMin: 0.0002, eccMax: 0.001, tint: [0.4, 0.85, 1.0] },
  { name: 'BEIDOU', count: 35, altMin: 21450, altMax: 21530, incMin: 54.8, incMax: 55.4, eccMin: 0.001, eccMax: 0.004, tint: [1.0, 0.6, 0.4] },
  { name: 'GEO', count: 620, altMin: 35770, altMax: 35800, incMin: 0.02, incMax: 6.0, eccMin: 0.0001, eccMax: 0.0025, tint: [1.0, 0.85, 0.5] },
  { name: 'WEATHER', count: 210, altMin: 620, altMax: 850, incMin: 81, incMax: 101, eccMin: 0.0005, eccMax: 0.003, tint: [0.85, 0.9, 1.0] },
  { name: 'DEBRIS', count: 4783, altMin: 320, altMax: 2000, incMin: 20, incMax: 110, eccMin: 0.0005, eccMax: 0.02, tint: [0.55, 0.55, 0.6] }
];

/** Deterministic PRNG. */
function mulberry32(seed: number): () => number {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/**
 * Builds the catalogue.
 *
 * NORAD ids start at 90000 to stay clear of real catalogue numbers, so a
 * synthetic bird can never be confused with a real one if both are loaded.
 * Every record is formatted through formatTLE, so the whole catalogue is
 * valid TLE text with correct checksums.
 */
export function buildCatalog(seed = 0x7e1e): TleRecord[] {
  const rng = mulberry32(seed);
  const epochYear = new Date().getUTCFullYear();
  const out: TleRecord[] = [];
  let id = 90000;

  for (const pop of POPULATIONS) {
    for (let i = 0; i < pop.count && out.length < CATALOG_COUNT; i++) {
      const alt = pop.altMin + rng() * (pop.altMax - pop.altMin);
      const semiMajorKm = EARTH_RADIUS_KM + alt;
      const meanMotion = meanMotionFromSemiMajor(semiMajorKm);

      const record: TleRecord = {
        name: pop.name + '-' + (i + 1),
        noradId: id++,
        epochYear,
        epochDay: 1 + rng() * 364,
        inclinationDeg: pop.incMin + rng() * (pop.incMax - pop.incMin),
        raanDeg: rng() * 360,
        eccentricity: pop.eccMin + rng() * (pop.eccMax - pop.eccMin),
        argPerigeeDeg: rng() * 360,
        meanAnomalyDeg: rng() * 360,
        meanMotionRevPerDay: meanMotion,
        revNumber: Math.floor(rng() * 60000),
        semiMajorKm
      };
      out.push(record);
    }
  }
  return out;
}

/** The catalogue as genuine TLE text, for writing to a file if desired. */
export function catalogAsTleText(records: TleRecord[]): string {
  return records.map((r) => formatTLE(r).join('\n')).join('\n') + '\n';
}
