/**
 * Tle — a real Two-Line Element parser and propagator.
 *
 * TLE is the data format every satellite catalogue in the world uses: two
 * 69-column text lines carrying the Keplerian mean elements of an orbit.
 * This module parses genuine TLE text and propagates it to a position at any
 * instant, so the engine can consume real catalogue data - NORAD, CelesTrak,
 * Space-Track - or the deterministic synthetic catalogue in TleCatalog.
 *
 * Propagation is the simplified Keplerian form: the mean anomaly advances at
 * the mean motion, Kepler's equation is solved for the eccentric anomaly, and
 * the resulting perifocal position is rotated by argument of perigee,
 * inclination and right ascension. It is deliberately NOT the full SGP4
 * model: there are no J2 secular drifts, no drag, and no short-period lunar
 * terms, so positions are accurate to "looks right" over the short windows a
 * game renders, not to sub-kilometre conjunction standards. The parser and
 * format, however, are the real thing - a genuine TLE dropped into the
 * loader propagates to a plausible orbit in the correct shell.
 */

/** Earth's gravitational parameter, km^3/s^2. */
export const MU_KM = 398600.4418;
/** Earth's equatorial radius, km. Used to turn altitudes into semi-major axes. */
export const EARTH_RADIUS_KM = 6378.137;

export interface TleRecord {
  name: string;
  noradId: number;
  /** Full four-digit epoch year. */
  epochYear: number;
  /** Epoch day-of-year, including fraction. */
  epochDay: number;
  inclinationDeg: number;
  raanDeg: number;
  eccentricity: number;
  argPerigeeDeg: number;
  meanAnomalyDeg: number;
  /** Mean motion, revolutions per day. */
  meanMotionRevPerDay: number;
  revNumber: number;
  /** Semi-major axis in km, derived from the mean motion. */
  semiMajorKm: number;
}

/* ----------------------------- parsing ----------------------------- */

/** Two-digit year -> four-digit, using the standard 1957-2056 window. */
function fullYear(two: number): number {
  return two < 57 ? 2000 + two : 1900 + two;
}

/** Reads a fixed-width numeric field, tolerating leading spaces. */
function field(line: string, start: number, len: number): number {
  const s = line.slice(start, start + len).trim();
  if (!s) return 0;
  return Number(s);
}

/** Parses three lines of a TLE: the name line and the two element lines. */
export function parseTLE(lines: [string, string, string]): TleRecord | null {
  const name = (lines[0] ?? '').trim();
  const l1 = lines[1] ?? '';
  const l2 = lines[2] ?? '';
  if (l1.length < 69 || l2.length < 69) return null;

  const noradId = Math.floor(field(l1, 2, 5));
  const epochYear = fullYear(Math.floor(field(l1, 18, 2)));
  const epochDay = field(l1, 20, 12);

  const inclinationDeg = field(l2, 8, 8);
  const raanDeg = field(l2, 17, 8);
  // Eccentricity is stored without its leading "0." - seven implied decimals.
  const eccentricity = Math.floor(field(l2, 26, 7)) / 1e7;
  const argPerigeeDeg = field(l2, 34, 8);
  const meanAnomalyDeg = field(l2, 43, 8);
  const meanMotionRevPerDay = field(l2, 52, 11);
  const revNumber = Math.floor(field(l2, 63, 5));

  if (!Number.isFinite(meanMotionRevPerDay) || meanMotionRevPerDay <= 0) return null;

  return {
    name: name || ('NORAD ' + noradId),
    noradId,
    epochYear,
    epochDay,
    inclinationDeg,
    raanDeg,
    eccentricity,
    argPerigeeDeg,
    meanAnomalyDeg,
    meanMotionRevPerDay,
    revNumber,
    semiMajorKm: semiMajorFromMeanMotion(meanMotionRevPerDay)
  };
}

/* ------------------------- format (synthetic) ------------------------- */

/** The mod-10 checksum: sum the digit characters, minus signs count as one. */
export function tleChecksum(body: string): number {
  let sum = 0;
  for (let i = 0; i < body.length; i++) {
    const c = body.charCodeAt(i);
    if (c >= 48 && c <= 57) sum += c - 48;
    else if (c === 45) sum += 1;      // '-' counts as 1
  }
  return sum % 10;
}

/** Right-justified numeric field, padded with leading zeros. */
function fix(n: number, width: number, decimals: number): string {
  const sign = n < 0 ? '-' : '';
  const a = Math.abs(n);
  const scale = Math.pow(10, decimals);
  const digits = String(Math.round(a * scale)).padStart(decimals + 1, '0');
  const intPart = digits.slice(0, digits.length - decimals);
  const fracPart = digits.slice(digits.length - decimals);
  const s = sign + intPart + (decimals ? '.' + fracPart : '');
  return s.slice(0, width).padStart(width, '0');
}

/** Formats a record into a genuine, checksummed 3-line TLE. */
export function formatTLE(r: TleRecord): [string, string, string] {
  const epochYear2 = String(((r.epochYear % 100) + 100) % 100).padStart(2, '0');
  const epochDay = r.epochDay.toFixed(8).padStart(12, '0');

  // Line 1, column-exact (0-indexed): norad 2-6, class 7, intl desig 9-16,
  // epoch year 18-19, epoch day 20-31, derivatives 33-60, elset 64-67.
  const l1 =
    '1 ' +
    String(r.noradId).padStart(5, '0') +
    'U' +
    ' ' + '00' + '000' + 'A' + '   ' +
    epochYear2 + epochDay +
    ' ' + ' .00000000' +
    ' ' + ' 00000-0' +
    ' ' + ' 00000-0' +
    ' ' + '0' +
    ' ' + ' 999';

  const body1 = l1.slice(0, 68);
  const line1 = body1 + tleChecksum(body1);

  const ecc7 = String(Math.round(Math.min(1, Math.max(0, r.eccentricity)) * 1e7))
    .padStart(7, '0');
  const mm = fix(r.meanMotionRevPerDay, 11, 8);
  const rev = String(Math.floor(r.revNumber) % 100000).padStart(5, '0');

  // Line 2, column-exact (0-indexed): norad 2-6, inc 8-15, raan 17-24,
  // ecc 26-32, argp 34-41, mean anomaly 43-50, mean motion 52-62, rev 63-67.
  const l2 =
    '2 ' +
    String(r.noradId).padStart(5, '0') + ' ' +
    fix(r.inclinationDeg, 8, 4) + ' ' +
    fix(r.raanDeg, 8, 4) + ' ' +
    ecc7 + ' ' +
    fix(r.argPerigeeDeg, 8, 4) + ' ' +
    fix(r.meanAnomalyDeg, 8, 4) + ' ' +
    mm + rev;

  const body2 = l2.slice(0, 68);
  const line2 = body2 + tleChecksum(body2);

  return [r.name, line1, line2];
}

/* --------------------------- propagation --------------------------- */

/** Semi-major axis in km from a mean motion in rev/day. */
export function semiMajorFromMeanMotion(revPerDay: number): number {
  const n = revPerDay * (2 * Math.PI) / 86400;   // rad / s
  return Math.cbrt(MU_KM / (n * n));
}

/** Mean motion in rev/day from a semi-major axis in km. */
export function meanMotionFromSemiMajor(aKm: number): number {
  const n = Math.sqrt(MU_KM / Math.max(aKm, 1) ** 3);
  return n * 86400 / (2 * Math.PI);
}

const DEG = Math.PI / 180;

/** The epoch as a JS date, for live propagation against the real clock. */
export function tleEpochDate(r: TleRecord): Date {
  const day = Math.floor(r.epochDay);
  const frac = r.epochDay - day;
  const ms = Date.UTC(r.epochYear, 0, 1) + (day - 1) * 86400000 + frac * 86400000;
  return new Date(ms);
}

/** Minutes between the record's epoch and a reference instant. */
export function minutesSinceEpoch(r: TleRecord, now: Date): number {
  const t = tleEpochDate(r).getTime();
  return (now.getTime() - t) / 60000;
}

/**
 * The position of a satellite at `minutes` after its epoch, in km, in the
 * Earth-centred inertial frame of the TLE. Pure and allocation-free.
 */
export function tlePositionKm(
  r: TleRecord, minutes: number,
  out: [number, number, number] = [0, 0, 0]
): [number, number, number] {
  const n = r.meanMotionRevPerDay * (2 * Math.PI) / 1440;   // rad / min
  const M = r.meanAnomalyDeg * DEG + n * minutes;

  // Solve Kepler's equation for the eccentric anomaly.
  const e = r.eccentricity;
  let E = M;
  for (let i = 0; i < 6; i++) {
    E = E - (E - e * Math.sin(E) - M) / (1 - e * Math.cos(E));
  }

  const nu = Math.atan2(Math.sqrt(1 - e * e) * Math.sin(E), Math.cos(E) - e);
  const rad = r.semiMajorKm * (1 - e * Math.cos(E));

  const u = nu + r.argPerigeeDeg * DEG;   // argument of latitude
  const i = r.inclinationDeg * DEG;
  const O = r.raanDeg * DEG;
  const cu = Math.cos(u), su = Math.sin(u);
  const ci = Math.cos(i), si = Math.sin(i);
  const cO = Math.cos(O), sO = Math.sin(O);

  const x = rad * (cO * cu - sO * su * ci);
  const y = rad * (sO * cu + cO * su * ci);
  const z = rad * (su * si);

  out[0] = x; out[1] = y; out[2] = z;
  return out;
}
