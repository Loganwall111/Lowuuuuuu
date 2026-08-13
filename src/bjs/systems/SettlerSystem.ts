/**
 * SettlerSystem — the people who already live there.
 *
 * LifeSystem gives a planet wildlife. This gives it inhabitants: named
 * individuals with a trade, an opinion about you, and something to say that
 * depends on where they are and what you have been doing to their world.
 *
 * They are generated from the planet's own seed, so a given planet always
 * has the same people on it and two players describing "the botanist on
 * Terrapor" mean the same person. Nothing is authored per-planet.
 *
 * Their mood is not decoration either: throw a moon at the planet they live
 * on and the survivors will have noticed.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';

export type Trade =
  | 'botanist' | 'geologist' | 'engineer' | 'pilot'
  | 'archivist' | 'cook' | 'medic' | 'prospector';

export const TRADES: Trade[] = [
  'botanist', 'geologist', 'engineer', 'pilot',
  'archivist', 'cook', 'medic', 'prospector'
];

/** How an inhabitant feels about you, worst to best. */
export type Mood = 'terrified' | 'wary' | 'neutral' | 'friendly' | 'delighted';

export interface Settler {
  id: string;
  name: string;
  trade: Trade;
  /** Where they stand, relative to the planet centre. */
  position: Vector3;
  /** -1 hostile, 0 indifferent, +1 delighted. */
  regard: number;
  /** Their body in the scene, when they have one. */
  mesh?: Mesh;
  /** Seconds since they last said something. */
  quiet: number;
}

/* -------------------------------------------------------------------------- */
/*  Names                                                                      */
/* -------------------------------------------------------------------------- */

const FIRST = [
  'Ada', 'Bex', 'Cyra', 'Dov', 'Elke', 'Finn', 'Gale', 'Hana', 'Iver',
  'Juno', 'Kesh', 'Lark', 'Mio', 'Nima', 'Oren', 'Pell', 'Quill', 'Rook',
  'Sable', 'Tove', 'Umi', 'Vale', 'Wren', 'Xan', 'Yuki', 'Zev'
];
const LAST = [
  'Ashgrove', 'Bellweather', 'Corvid', 'Dunmore', 'Etchsel', 'Frostlin',
  'Garrow', 'Halloway', 'Ivorsen', 'Jarrek', 'Kestrel', 'Lowmede',
  'Marrow', 'Nyquist', 'Ostrand', 'Pyrehill'
];

/** Deterministic RNG, so a planet always has the same people. */
function rng(seed: number): () => number {
  let a = (seed >>> 0) || 1;
  return () => {
    a += 0x6D2B79F5;
    let t = a;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/**
 * Generates the population of a planet from its seed.
 *
 * The same seed always produces the same people, so they persist across
 * visits without anything being stored.
 */
export function settlersFor(seed: number, count = 6): Settler[] {
  const r = rng(seed);
  const out: Settler[] = [];
  const used = new Set<string>();

  for (let i = 0; i < Math.max(0, count); i++) {
    let name = FIRST[Math.floor(r() * FIRST.length)] + ' ' +
               LAST[Math.floor(r() * LAST.length)];
    // Two people with the same name on one rock is confusing, not charming.
    let guard = 0;
    while (used.has(name) && guard++ < 8) {
      name = FIRST[Math.floor(r() * FIRST.length)] + ' ' +
             LAST[Math.floor(r() * LAST.length)];
    }
    used.add(name);

    out.push({
      id: 'settler-' + seed + '-' + i,
      name,
      trade: TRADES[Math.floor(r() * TRADES.length)],
      position: new Vector3(r() * 2 - 1, r() * 2 - 1, r() * 2 - 1),
      // Most people start indifferent, a few start warm.
      regard: (r() - 0.35) * 0.5,
      quiet: 0
    });
  }
  return out;
}

/** Regard as a mood. */
export function moodOf(regard: number): Mood {
  if (regard <= -0.6) return 'terrified';
  if (regard <= -0.2) return 'wary';
  if (regard < 0.3) return 'neutral';
  if (regard < 0.7) return 'friendly';
  return 'delighted';
}

/* -------------------------------------------------------------------------- */
/*  What they say                                                              */
/* -------------------------------------------------------------------------- */

const LINES: Record<Mood, string[]> = {
  terrified: [
    'You are the reason we have a drill for this.',
    'Please. Whatever it is. Not today.',
    'We saw what you did to the last one.'
  ],
  wary: [
    'Keep your hands where the sky can see them.',
    'We had a quiet century going, you know.',
    'Do not touch the core. People keep touching the core.'
  ],
  neutral: [
    'You are a long way from anywhere.',
    'Weather holds, mostly. Gravity holds, entirely.',
    'Mind the fauna. It does not mind you.'
  ],
  friendly: [
    'Good to see a new face out here.',
    'If you are going up, take a reading for us.',
    'There is a ridge east of here worth the walk.'
  ],
  delighted: [
    'You came back! Everyone, look who it is.',
    'Take whatever you need. We owe you one.',
    'They still talk about what you did up there.'
  ]
};

/** Trade-specific remarks, which is what makes them feel placed. */
const TRADE_LINES: Record<Trade, string> = {
  botanist:   'Third generation of these seeds. They finally like it here.',
  geologist:  'The strata here should not exist. I am delighted and annoyed.',
  engineer:   'Everything works. That is how I know something is about to not.',
  pilot:      'I could get you up in eleven minutes. Nine, if you do not mind the noise.',
  archivist:  'Someone has to write down what happened. Usually to us.',
  cook:       'Everything grown here tastes faintly of iron. You get used to it.',
  medic:      'Low gravity is murder on the spine. Nobody believes me until it is.',
  prospector: 'There is something heavy under the north ridge. I can feel it.'
};

/**
 * What a settler says right now.
 *
 * Mood first, then trade, so the same person sounds like themselves across
 * moods rather than reciting one fixed line forever.
 */
export function speak(s: Settler, tick = 0): string {
  const mood = moodOf(s.regard);
  const pool = LINES[mood];
  const line = pool[Math.abs(Math.floor(tick)) % pool.length];
  // A quarter of the time they talk shop instead.
  return (Math.abs(Math.floor(tick)) % 4 === 3) ? TRADE_LINES[s.trade] : line;
}

/* -------------------------------------------------------------------------- */
/*  The system                                                                 */
/* -------------------------------------------------------------------------- */

export class SettlerSystem {
  settlers: Settler[] = [];
  private scene: Scene | null = null;
  private mat: StandardMaterial | null = null;
  /** The last thing anyone said, for the UI. */
  lastLine = '';
  private tick = 0;

  attach(scene: Scene): void {
    this.scene = scene;
  }

  /**
   * Puts a population on a body of the given radius.
   *
   * They are placed on the surface by normalising their generated direction,
   * so they always stand on the sphere rather than floating near it.
   */
  populate(seed: number, center: Vector3, radius: number, count = 6): void {
    this.clear();
    const scene = this.scene;
    this.settlers = settlersFor(seed, count);

    if (!scene) return;
    if (!this.mat) {
      this.mat = new StandardMaterial('settler-mat', scene);
      this.mat.diffuseColor = new Color3(0.82, 0.76, 0.66);
      this.mat.emissiveColor = new Color3(0.16, 0.14, 0.11);
    }

    for (const s of this.settlers) {
      const dir = s.position.lengthSquared() > 1e-6
        ? s.position.normalize() : new Vector3(0, 1, 0);
      const h = Math.max(0.4, radius * 0.06);
      s.position = center.add(dir.scale(radius + h * 0.5));

      const m = MeshBuilder.CreateCapsule(
        s.id, { radius: h * 0.28, height: h }, scene);
      m.position.copyFrom(s.position);
      // Stand upright relative to the planet, not the world.
      m.lookAt(center);
      m.rotation.x += Math.PI / 2;
      m.material = this.mat;
      m.isPickable = true;
      s.mesh = m;
    }
  }

  /**
   * Something happened to their planet.
   *
   * Regard drops with the severity of what you did, so people remember. It
   * is clamped rather than allowed to run away: there is no coming back
   * from dropping a moon on someone, but there is no infinite hole either.
   */
  witnessed(severity: number): void {
    const d = Math.max(0, Math.min(2, severity));
    for (const s of this.settlers) {
      s.regard = Math.max(-1, Math.min(1, s.regard - d));
    }
    if (this.settlers.length && d > 0.25) {
      this.lastLine = this.settlers[0].name + ': ' + speak(this.settlers[0], this.tick);
    }
  }

  /** A kindness, or simply time passing without incident. */
  pleased(amount = 0.1): void {
    for (const s of this.settlers) {
      s.regard = Math.max(-1, Math.min(1, s.regard + Math.max(0, amount)));
    }
  }

  /** The settler nearest a point, if they are within range. */
  nearest(to: Vector3, within = 6): Settler | null {
    let best: Settler | null = null;
    let bestD = within;
    for (const s of this.settlers) {
      const d = Vector3.Distance(s.position, to);
      if (d < bestD) { bestD = d; best = s; }
    }
    return best;
  }

  /** Talks to whoever is closest. Returns what they said. */
  talkTo(pos: Vector3, within = 6): string | null {
    const s = this.nearest(pos, within);
    if (!s) return null;
    s.quiet = 0;
    const line = s.name + ' (' + s.trade + '): ' + speak(s, this.tick);
    this.lastLine = line;
    return line;
  }

  update(dt: number): void {
    if (!Number.isFinite(dt) || dt <= 0) return;
    this.tick += dt * 0.2;
    for (const s of this.settlers) s.quiet += dt;
  }

  stats(): Record<string, string> {
    if (!this.settlers.length) return { 'Inhabitants': 'none' };
    const avg = this.settlers.reduce((a, s) => a + s.regard, 0) / this.settlers.length;
    return {
      'Inhabitants': String(this.settlers.length),
      'They feel': moodOf(avg),
      'Last said': this.lastLine || '—'
    };
  }

  clear(): void {
    for (const s of this.settlers) {
      try { s.mesh?.dispose(); } catch { /* already gone */ }
    }
    this.settlers = [];
  }

  dispose(): void {
    this.clear();
    try { this.mat?.dispose(); } catch { /* already gone */ }
    this.mat = null;
    this.scene = null;
    this.lastLine = '';
  }
}
