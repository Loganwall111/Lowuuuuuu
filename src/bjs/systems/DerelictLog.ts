/**
 * DerelictLog — found stories aboard dead ships.
 *
 * The catalog lists derelicts, but a derelict that cannot be boarded is a
 * silhouette. Boarding one reveals a seeded log: the last words of the
 * people who built it, assembled from fragments so no two derelicts tell
 * the same story. Pure text generation from a seed - deterministic, so the
 * same wreck always yields the same log, and testable without a GPU.
 */

export interface DerelictLog {
  title: string;
  body: string;
  crew: string;
  fate: string;
}

const SHIPS = ['Ore Hauler', 'Survey Liner', 'Colony Ship', 'Long-Range Probe',
  'Hospital Barge', 'Salvage Cutter', 'Seed Vessel', 'Listening Post'];

const CREWS = ['fourteen hands', 'a skeleton crew of nine', 'two hundred colonists',
  'the last eleven of a vanished fleet', 'a single, patient warden', 'a family of six'];

const DISASTERS = ['the air scrubbers gave out', 'a fuel fire in the spine',
  'they dropped too close to the star', 'the jump never ended', 'something they picked up woke',
  'the hull froze solid', 'a mutiny no one wrote down'];

const LAST_WORDS = [
  'The air is thin now, but the view is good. If anyone finds this, we made it farther than we were told we would.',
  'We are not lost. We are exactly where the map said, and the map was wrong.',
  'Do not open the forward hold. There was a reason it was sealed.',
  'The star is beautiful from here. We should have stayed at a distance.',
  'Engineer says we can still run the beacon. Nobody has to know it is our last power.',
  'We buried the manifest. If you find it, the cargo is yours - it was never really ours.',
  'Six weeks of quiet, then the lights came back on by themselves.'
];

/** Deterministic 0..1 hash. */
function hash01(seed: number): number {
  let h = seed >>> 0 || 1;
  h = Math.imul(h ^ (h >>> 16), 2246822519) >>> 0;
  h = Math.imul(h ^ (h >>> 13), 3266489917) >>> 0;
  return ((h ^ (h >>> 16)) >>> 0) / 4294967296;
}

/** The found log aboard a derelict, assembled from its seed. */
export function derelictLog(seed: number): DerelictLog {
  const pick = <T>(arr: T[], off: number): T =>
    arr[Math.floor(hash01(seed + off * 7919) * arr.length) % arr.length];

  const ship = pick(SHIPS, 1);
  const crew = pick(CREWS, 2);
  const disaster = pick(DISASTERS, 3);
  const words = pick(LAST_WORDS, 4);
  const name = 'Hull ' + Math.floor(hash01(seed + 5) * 9000 + 1000);

  return {
    title: ship + ' ' + name,
    crew,
    fate: disaster,
    body: 'Log entry, final. ' + words + ' — ' + crew +
      ' were aboard when ' + disaster + '. The beacon still pings.'
  };
}
