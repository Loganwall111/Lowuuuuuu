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

export const CURRENT_UPDATE = 'UPDATE 3';
export const CURRENT_UPDATE_NAME = 'THE NEXT GENERATION UPDATE';

export const RELEASES: PatchRelease[] = [
  {
    version: '3',
    name: 'THE NEXT GENERATION UPDATE',
    tagline: 'Full Babylon.js 9.21, WebGPU-first rendering, floating worlds, and a rebuilt multiverse.',
    entries: [
      { title:'WebGPU-first full-engine renderer',tag:'improved',body:'The complete standard Babylon.js engine now attempts WebGPU first and falls back safely to WebGL2. No Babylon Lite runtime or data-model migration is used.' },
      { title:'Next Generation Render Graph',tag:'new',body:'A four-stage Babylon Node Render Graph companion now validates Base Geometry, Deep Sky, Singularity Volume, and Final Composite dependencies while the standard Scene graph preserves every custom shader and gameplay system.' },
      { title:'Floating-origin universe',tag:'new',body:'True universe coordinates remain double precision while every GPU-facing planet, fleet, portal, comet, anomaly, galaxy and black hole is rendered near a quantized local origin. Billion-unit travel no longer requires hiding physical meshes.' },
      { title:'All black-hole families restored',tag:'improved',body:'Twelve lens laws and seven physical accretion families now drive the open-world renderer, including ringless, shattered, kaleidoscopic, repulsive, rippled and prismatic singularities.' },
      { title:'Non-Euclidean interiors',tag:'improved',body:'Turn backward to see the universe you left, sideways into horizon flow, or deeper toward nested folds and the destination. Twenty-to-thirty-minute ordinary descents and Warp DR compression remain intact.' },
      { title:'Cosmic Tears and The Balge',tag:'new',body:'Rare gravitationally mirrored tears can cross the universe or reach more than fifty procedural dimensions, including the lightning-filled Balge and its polar energy vortex.' },
      { title:'Self-healing Dimensional Drifts',tag:'new',body:'Temporary jagged rifts now open near star systems, preview another procedural planet, transport the ship, then stitch themselves closed and fade to exactly zero.' },
      { title:'Seven galaxy classes',tag:'improved',body:'Photoreal, barred, flocculent, lenticular, irregular, elliptical and anomaly galaxies use distinct structures, gas layouts, nuclei and dust behavior.' },
      { title:'Living orbital cosmos',tag:'improved',body:'Planets orbit their stars, asteroid belts shear on Keplerian curves, comets follow ellipses, fleets navigate, accretion disks rotate and real TLE satellites propagate continuously.' },
      { title:'Real-time 3D universe prologue',tag:'new',body:'Creating a universe can play a dedicated Babylon scene with terrain, pilot, rocket, Matrix transformation, wormhole flight, voxel hands, white flash and title reveal. Rejoining skips it.' },
      { title:'Inspector v2 diagnostics',tag:'new',body:'Press F9 to load Babylon Inspector v2 on demand with scene, mesh, material, camera, texture and render-graph diagnostics without adding Inspector cost to normal gameplay.' },
      { title:'Clean cinematic image',tag:'fixed',body:'Film grain, chromatic aberration and sharpening remain available but default off. Celestial duplicate uniforms, reserved GLSL words, black frames, lens bubbles and overbright planet instances were corrected.' }
    ]
  },
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
