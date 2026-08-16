/**
 * CivilizationSystem — a species you can watch grow up.
 *
 * Most sandboxes give you a planet with city lights on the night side and
 * leave it at that: the lights are decoration, and the people they imply
 * never change. This turns that into a time-lapse. A settlement advances
 * through technological stages on its own clock - stone, bronze, towns,
 * radio, spaceflight, contact - and each stage changes how bright its cities
 * burn and what it might do next. Watching a world you landed on earlier
 * reach the radio age when you return is the whole point.
 *
 * Pure numbers, no Babylon: population grows logistically, and a stage turns
 * over when the population has supported it for long enough. Collapse is a
 * small, seeded risk at every stage, so not every species makes it.
 */

export type TechStage =
  | 'stone' | 'bronze' | 'town' | 'radio' | 'spaceflight' | 'contact' | 'collapse';

export interface StageSpec {
  id: TechStage;
  label: string;
  glyph: string;
  /** Population (0..1) required to hold this stage. */
  minPop: number;
  /** How much of the night side lights up at this stage. */
  lights: number;
  blurb: string;
}

export const STAGES: StageSpec[] = [
  { id: 'stone', label: 'Stone Age', glyph: '🪨', minPop: 0.02, lights: 0.04, blurb: 'Fires on the night side. The first stories.' },
  { id: 'bronze', label: 'Bronze Age', glyph: '🏺', minPop: 0.16, lights: 0.16, blurb: 'Settlements along the rivers.' },
  { id: 'town', label: 'Towns', glyph: '🏘', minPop: 0.34, lights: 0.34, blurb: 'Roads, markets, and the first cities.' },
  { id: 'radio', label: 'Radio Age', glyph: '📡', minPop: 0.55, lights: 0.60, blurb: 'Their signals reach the stars.' },
  { id: 'spaceflight', label: 'Spaceflight', glyph: '🚀', minPop: 0.75, lights: 0.85, blurb: 'They leave their world.' },
  { id: 'contact', label: 'Contact', glyph: '🛰', minPop: 0.92, lights: 1.0, blurb: 'They notice you.' },
  { id: 'collapse', label: 'Collapse', glyph: '🏚', minPop: 0, lights: 0.03, blurb: 'The lights go out.' }
];

export interface CivilizationState {
  stage: TechStage;
  stageIndex: number;
  /** Abstract population, 0..1. */
  population: number;
  /** Progress toward the next stage, 0..1. */
  progress: number;
  /** Night-side city light intensity, 0..1. */
  cityLights: number;
  collapsed: boolean;
}

export interface CivOptions {
  /** How fast the whole arc plays out. 1 = a full run in ~30 minutes. */
  rate: number;
  /** Per-stage chance (0..1) of collapse rather than advancing. */
  collapseChance: number;
}

export const DEFAULT_CIV: CivOptions = { rate: 1, collapseChance: 0.04 };

/** Deterministic RNG, so one seed always walks the same path. */
function mulberry32(seed: number): () => number {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

export class CivilizationSystem {
  opts: CivOptions;
  private rng: () => number;
  private population = 0.015;
  private stageIndex = 0;
  private collapsed = false;

  constructor(seed = 1, opts: Partial<CivOptions> = {}) {
    this.opts = { ...DEFAULT_CIV, ...opts };
    this.rng = mulberry32(seed);
  }

  /** Advances the time-lapse. Returns the new stage when one turns over. */
  step(dt: number): TechStage | null {
    if (!Number.isFinite(dt) || dt <= 0 || this.collapsed) return null;

    // Logistic growth with a slow, stage-independent ceiling pull.
    const growth = 0.16 * this.population * (1 - this.population);
    this.population = Math.min(1, Math.max(0.01, this.population + growth * dt));

    const next = STAGES[Math.min(this.stageIndex + 1, STAGES.length - 1)];
    if (this.population >= next.minPop) {
      // Collapse risk: a fresh seeded roll at every stage turn, so the
      // outcome is stable per seed but a civilisation can still fall.
      const roll = this.rng();
      if (next.id !== 'collapse' && roll < this.opts.collapseChance) {
        this.collapsed = true;
        this.stageIndex = STAGES.length - 1;
        return 'collapse';
      }
      if (this.stageIndex < STAGES.length - 2) {
        this.stageIndex++;
        return STAGES[this.stageIndex].id;
      }
    }
    return null;
  }

  state(): CivilizationState {
    const s = STAGES[this.stageIndex];
    const next = STAGES[Math.min(this.stageIndex + 1, STAGES.length - 1)];
    const span = Math.max(next.minPop - s.minPop, 1e-3);
    return {
      stage: s.id,
      stageIndex: this.stageIndex,
      population: this.population,
      progress: this.collapsed ? 0 : Math.min(1, Math.max(0, (this.population - s.minPop) / span)),
      cityLights: s.lights,
      collapsed: this.collapsed
    };
  }

  /** Just the light level, for the planet shader's city lights. */
  lights(): number { return this.collapsed ? 0.03 : STAGES[this.stageIndex].lights; }

  get stage(): TechStage { return STAGES[this.stageIndex].id; }
  get isCollapsed(): boolean { return this.collapsed; }

  stats(): Record<string, string> {
    const s = this.state();
    return {
      'Civilization': STAGES[this.stageIndex].glyph + ' ' + STAGES[this.stageIndex].label,
      'Population': (s.population * 100).toFixed(0) + '%',
      'Stage progress': (s.progress * 100).toFixed(0) + '%'
    };
  }
}
