/**
 * GalacticSeasons — the sky changes over the long run.
 *
 * In a real night sky, the constellations and the Milky Way band slowly
 * precess as the home system orbits the galactic core - a change measured in
 * months, not frames. This models that: an extremely slow drift in the
 * background starfield's orientation, plus a "season" that ticks over every
 * few minutes so the long-term change is actually visible in a session.
 *
 * The pure half is the timing: the app applies the returned precession angle
 * to the layered background sky. A still sky costs nothing; a sky that
 * imperceptibly turns is what makes the universe feel like it is moving even
 * when you are not.
 */

export const SEASON_PERIOD = 480;      // seconds per season
export const PRECESSION_PERIOD = 2400; // seconds for one full sky turn

/** Which of four seasons the universe is in, from elapsed seconds. */
export function seasonIndex(t: number): number {
  const s = Number.isFinite(t) ? Math.max(0, t) : 0;
  return Math.floor(s / SEASON_PERIOD) % 4;
}

export const SEASON_NAMES = ['Perihelion', 'Approach', 'Aphelion', 'Retreat'];

export function seasonLabel(t: number): string {
  return SEASON_NAMES[seasonIndex(t)];
}

/**
 * The precession angle of the background sky, in radians.
 *
 * Runs one full turn over PRECESSION_PERIOD seconds, so over a session the
 * faint stars behind the reachable universe very slowly wheel around the
 * player. Deliberately a linear function of time: it must never jump.
 */
export function precessionAngle(t: number): number {
  const s = Number.isFinite(t) ? Math.max(0, t) : 0;
  return ((s % PRECESSION_PERIOD) / PRECESSION_PERIOD) * Math.PI * 2;
}

/** A readable one-liner for the HUD / telemetry. */
export function describeSeason(t: number): string {
  return SEASON_NAMES[seasonIndex(t)] + ' · ' +
    Math.round(precessionAngle(t) * 180 / Math.PI) + '°';
}
