/**
 * PatchNotes — what changed, in the player's words rather than the commit
 * log's.
 *
 * Kept as structured data rather than a slab of HTML so the same content
 * can be shown in the title screen, in a settings panel, or anywhere else
 * without being rewritten. Entries are newest-first; the title screen shows
 * the head of the list.
 */

export interface PatchEntry {
  /** Short heading. */
  title: string;
  /** Category, used for the coloured tag. */
  tag: 'new' | 'fixed' | 'improved';
  /** What the player will actually notice. */
  body: string;
}

export interface PatchRelease {
  /** Version identifier, e.g. "2". */
  version: string;
  /** The release's name. */
  name: string;
  /** One-line summary. */
  tagline: string;
  entries: PatchEntry[];
}

export const CURRENT_UPDATE = 'UPDATE 2';
export const CURRENT_UPDATE_NAME = 'BETTER COSMOS';

export const RELEASES: PatchRelease[] = [
  {
    version: '2',
    name: 'BETTER COSMOS',
    tagline: 'A fuller sky, a working warp, and instruments worth reading.',
    entries: [
      {
        title: 'Satellite HUD',
        tag: 'new',
        body: 'The flight instruments are now framed as a downlink from a '
          + 'tracking satellite: corner brackets, a scanning sweep, signal '
          + 'strength and an uplink state. The old instruments are still '
          + 'there as the Legacy theme, in Settings › HUD Style.'
      },
      {
        title: 'Warp actually looks like warp',
        tag: 'fixed',
        body: 'Streaks were advanced by raw world speed, so past about '
          + '10,000 u/s each one crossed the entire streak tube in a single '
          + 'frame and was recycled - at full warp, 54,000 times over. Every '
          + 'streak was therefore in a fresh random place every frame, which '
          + 'is noise, not motion. All you saw was the streaks getting '
          + 'longer. Apparent flow is now bounded, so the tube keeps moving '
          + 'at a readable rate however fast you are really going, and a '
          + 'full-screen radial rush with chromatic fringing has been added '
          + 'behind it.'
      },
      {
        title: 'Manual velocity gears',
        tag: 'new',
        body: 'Press 1, 2 or 3 for Impulse, Cruise or Hyper. Speed used to '
          + 'be derived entirely from how far away the nearest object was, '
          + 'which handed out 12,266 u/s in deep space - enough to cross the '
          + 'galaxy in under a second with no way to ask for less.'
      },
      {
        title: 'The bubble around black holes is gone',
        tag: 'fixed',
        body: 'The lensed-sky term stayed faintly opaque out to about 22 '
          + 'horizon radii, so every hole was wrapped in a huge translucent '
          + 'disc. It now fades out by around 7 radii - inside the disk\u2019s '
          + 'own outer edge - so nothing extends past the object you can '
          + 'see. The Einstein ring and the wrap around the shadow are '
          + 'untouched.'
      },
      {
        title: 'You can get inside a black hole',
        tag: 'fixed',
        body: 'Crossing a horizon was detected correctly and then thrown '
          + 'away one frame later: a single frame at cruise covers 204 units '
          + 'and a horizon is 9 to 90 units across, so your own inertia put '
          + 'you back outside before the descent could begin. Release now '
          + 'depends on how fast you are moving, not just where you are, so '
          + 'a genuine climb-out still works but a flythrough keeps you.'
      },
      {
        title: '22 new kinds of celestial object',
        tag: 'new',
        body: 'Pulsars, quasars, magnetars, comets, meteor swarms, binary '
          + 'stars, red giants, white and brown dwarfs, protostars, '
          + 'supernova remnants, planetary nebulae, globular and open '
          + 'clusters, rogue planets, ice and gas giants, asteroid and '
          + 'crystal fields, derelicts, wormhole mouths and Dyson swarms. '
          + 'All procedural, all from one seeded lattice, drawn in a single '
          + 'draw call.'
      },
      {
        title: 'Sound',
        tag: 'new',
        body: 'A generative ambient score - written live from a scale and a '
          + 'weighted random walk, not a looped file. Space itself stays '
          + 'silent; the soft low hum and its slight vibration are framed as '
          + 'coming from the satellite you are watching through. A very '
          + 'faint wind rises near a horizon. All three switch off '
          + 'independently in Settings › Sound.'
      },
      {
        title: 'Sonar cursor',
        tag: 'new',
        body: 'The pointer is now a tracking reticle with a rotating bearing '
          + 'sweep and a ping on click, instead of the operating system '
          + 'arrow.'
      }
    ]
  }
];

/** The release shown by default. */
export function latestRelease(): PatchRelease {
  return RELEASES[0];
}

/** Counts entries by tag, for the summary line. */
export function countByTag(r: PatchRelease): Record<string, number> {
  const out: Record<string, number> = { new: 0, fixed: 0, improved: 0 };
  for (const e of r.entries) out[e.tag] = (out[e.tag] ?? 0) + 1;
  return out;
}
