/**
 * Leaderboard — a seeded hall of explorers.
 *
 * There is no server, so the "leaderboard" is local and honest about it: a
 * deterministic table of synthetic explorers generated from the universe
 * seed, with the player slotted in by a real score computed from what they
 * actually did (distance travelled, discoveries, milestones, challenges).
 * It exists so the sandbox has a reason to push - can you climb past the
 * generated names? - without pretending to be online.
 *
 * Pure arithmetic, no DOM, testable without a scene.
 */

export interface LeaderRow {
  rank: number;
  name: string;
  score: number;
  /** True when this row is the player. */
  you: boolean;
}

export interface PlayerStats {
  /** Total distance travelled, world units. */
  distance: number;
  discoveries: number;
  milestones: number;
  challenges: number;
}

/** The player's score, from what they actually did. */
export function playerScore(s: PlayerStats): number {
  const d = Number.isFinite(s.distance) ? Math.max(0, s.distance) : 0;
  const discoveries = Math.max(0, s.discoveries ?? 0);
  const milestones = Math.max(0, s.milestones ?? 0);
  const challenges = Math.max(0, s.challenges ?? 0);
  return Math.round(
    Math.log10(1 + d) * 1200 +
    discoveries * 150 +
    milestones * 400 +
    challenges * 250
  );
}

const NAMES = ['Vela Runner', 'Orion Drifter', 'Cygnus Bound', 'Lyra Widow',
  'Draco Chaser', 'Corvus Eye', 'Aquila Skim', 'Tucana Long', 'Perseus Wake',
  'Cetus Deep', 'Norma Veil', 'Carina Fast'];

/** Deterministic 0..1 hash. */
function hash01(seed: number): number {
  let h = seed >>> 0 || 1;
  h = Math.imul(h ^ (h >>> 16), 2246822519) >>> 0;
  h = Math.imul(h ^ (h >>> 13), 3266489917) >>> 0;
  return ((h ^ (h >>> 16)) >>> 0) / 4294967296;
}

/**
 * Builds the leaderboard: a seeded list of explorers, with the player
 * slotted in by score and the top ten returned, best first.
 */
export function leaderboard(seed: number, stats: PlayerStats): LeaderRow[] {
  const rows: LeaderRow[] = [];
  for (let i = 0; i < 24; i++) {
    const s = (seed ^ Math.imul(i + 1, 2654435761)) >>> 0;
    const name = NAMES[Math.floor(hash01(s + 1) * NAMES.length) % NAMES.length];
    rows.push({
      rank: 0,
      name,
      score: 200 + Math.round(hash01(s + 2) * 9000),
      you: false
    });
  }
  rows.push({ rank: 0, name: 'YOU', score: playerScore(stats), you: true });
  rows.sort((a, b) => b.score - a.score);
  return rows.slice(0, 10).map((r, i) => ({ ...r, rank: i + 1 }));
}
