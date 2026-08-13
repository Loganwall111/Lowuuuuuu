/**
 * QualitySystem — quality presets plus adaptive scaling.
 *
 * Presets set a hardware scaling level and post-processing budget. On top of
 * that, an adaptive controller watches the frame time and walks the scaling
 * level up or down to defend a target framerate, with hysteresis and a
 * cooldown so it settles instead of oscillating.
 */

export type QualityName = 'performance' | 'balanced' | 'high' | 'cinematic' | 'experimental';

export interface QualityPreset {
  name: QualityName;
  label: string;
  glyph: string;
  /** Babylon hardware scaling: >1 renders below native and upscales. */
  scaling: number;
  bloom: boolean;
  grain: boolean;
  chromatic: boolean;
  sharpen: boolean;
  fxaa: boolean;
  /** Multiplier applied to particle and star counts. */
  detail: number;
  shadows: boolean;
  maxBodies: number;
  note: string;
}

export const QUALITY: Record<QualityName, QualityPreset> = {
  performance: {
    name: 'performance', label: 'Performance', glyph: '⚡', scaling: 1.6,
    bloom: false, grain: false, chromatic: false, sharpen: false, fxaa: true,
    detail: 0.45, shadows: false, maxBodies: 120,
    note: 'Highest framerate. Effects off.'
  },
  balanced: {
    name: 'balanced', label: 'Balanced', glyph: '⚖', scaling: 1.2,
    bloom: true, grain: false, chromatic: false, sharpen: true, fxaa: true,
    detail: 0.75, shadows: false, maxBodies: 200,
    note: 'Good visuals at a steady framerate.'
  },
  high: {
    name: 'high', label: 'High', glyph: '✨', scaling: 1.0,
    bloom: true, grain: true, chromatic: true, sharpen: true, fxaa: true,
    detail: 1.0, shadows: true, maxBodies: 300,
    note: 'Native resolution with the full effect stack.'
  },
  cinematic: {
    name: 'cinematic', label: 'Cinematic', glyph: '🎬', scaling: 0.85,
    bloom: true, grain: true, chromatic: true, sharpen: true, fxaa: true,
    detail: 1.5, shadows: true, maxBodies: 420,
    note: 'Supersampled. For screenshots and slow motion.'
  },
  experimental: {
    name: 'experimental', label: 'Experimental', glyph: '🔬', scaling: 0.7,
    bloom: true, grain: true, chromatic: true, sharpen: true, fxaa: true,
    detail: 2.2, shadows: true, maxBodies: 650,
    note: 'Everything at maximum. Expect your fans to spin up.'
  }
};

export const QUALITY_ORDER: QualityName[] = [
  'performance', 'balanced', 'high', 'cinematic', 'experimental'
];

export interface AdaptiveOptions {
  targetFps: number;
  /** Framerate below which quality is reduced. */
  lowFps: number;
  /** Framerate above which quality may be raised. */
  highFps: number;
  minScaling: number;
  maxScaling: number;
  cooldown: number;
  step: number;
}

export const DEFAULT_ADAPTIVE: AdaptiveOptions = {
  targetFps: 60, lowFps: 42, highFps: 75,
  minScaling: 0.7, maxScaling: 2.0, cooldown: 1.5, step: 0.1
};

export class QualitySystem {
  current: QualityName = 'high';
  scaling: number;
  adaptive = false;
  opts: AdaptiveOptions;

  private samples: number[] = [];
  private cooldownLeft = 0;
  private windowSize = 30;
  changes = 0;

  constructor(initial: QualityName = 'high', opts: Partial<AdaptiveOptions> = {}) {
    this.current = QUALITY[initial] ? initial : 'high';
    this.scaling = QUALITY[this.current].scaling;
    this.opts = { ...DEFAULT_ADAPTIVE, ...opts };
  }

  preset(): QualityPreset {
    return QUALITY[this.current];
  }

  set(name: QualityName): QualityPreset {
    if (!QUALITY[name]) return this.preset();
    this.current = name;
    this.scaling = QUALITY[name].scaling;
    this.samples = [];
    this.cooldownLeft = this.opts.cooldown;
    return this.preset();
  }

  /** Steps one level up or down the preset ladder. */
  shift(dir: 1 | -1): QualityPreset {
    const i = QUALITY_ORDER.indexOf(this.current);
    const next = QUALITY_ORDER[Math.max(0, Math.min(QUALITY_ORDER.length - 1, i + dir))];
    return this.set(next);
  }

  /**
   * Feeds a frame time. Returns the new scaling if it changed, else null.
   * Uses a rolling median so a single stutter cannot trigger a change.
   */
  sample(dtSeconds: number): number | null {
    if (!this.adaptive || dtSeconds <= 0 || !Number.isFinite(dtSeconds)) return null;

    this.samples.push(1 / dtSeconds);
    if (this.samples.length > this.windowSize) this.samples.shift();

    if (this.cooldownLeft > 0) {
      this.cooldownLeft -= dtSeconds;
      return null;
    }
    if (this.samples.length < this.windowSize) return null;

    const sorted = [...this.samples].sort((a, b) => a - b);
    const median = sorted[Math.floor(sorted.length / 2)];
    const before = this.scaling;

    if (median < this.opts.lowFps && this.scaling < this.opts.maxScaling) {
      // render at lower resolution to recover speed
      this.scaling = Math.min(this.opts.maxScaling, this.scaling + this.opts.step);
    } else if (median > this.opts.highFps && this.scaling > this.opts.minScaling) {
      this.scaling = Math.max(this.opts.minScaling, this.scaling - this.opts.step);
    } else {
      return null;
    }

    this.scaling = Math.round(this.scaling * 1000) / 1000;
    if (this.scaling === before) return null;

    this.cooldownLeft = this.opts.cooldown;
    this.samples = [];
    this.changes++;
    return this.scaling;
  }

  medianFps(): number {
    if (!this.samples.length) return 0;
    const s = [...this.samples].sort((a, b) => a - b);
    return s[Math.floor(s.length / 2)];
  }

  reset(): void {
    this.samples = [];
    this.cooldownLeft = 0;
    this.scaling = QUALITY[this.current].scaling;
  }
}
