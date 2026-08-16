/**
 * SupernovaSystem — stars that actually end.
 *
 * The celestial catalog lists supernova remnants, but nothing in the
 * universe ever *becomes* one: every star is immortal, so the catalogue's
 * most spectacular entry is scenery rather than an event. This makes it an
 * event. A star near the player can go supernova - on its own, on a rare
 * seeded schedule, or because the player destabilised it - and the system
 * drives the whole show: a flash that peaks and fades, and a freshly-minted
 * remnant left where the star used to be.
 *
 * The engine half (bloom flash, the toast, the codex entry) lives in the
 * app; this class owns only the timing and the intensity envelope, so the
 * physics of the event can be tested without a GPU.
 */

export type NovaPhase = 'quiet' | 'flaring' | 'afterglow';

export interface NovaEvent {
  /** Name of the body that went, for the toast and codex. */
  name: string;
  /** 0..1 brightness of the flash, rising then falling. */
  flash: number;
  phase: NovaPhase;
}

export interface NovaOptions {
  /** Mean seconds between spontaneous supernovae near the player. */
  meanInterval: number;
  /** Seconds the flash takes to reach full brightness. */
  rise: number;
  /** Seconds the afterglow takes to die away. */
  fall: number;
}

export const DEFAULT_NOVA: NovaOptions = {
  meanInterval: 240,
  rise: 0.35,
  fall: 2.4
};

export class SupernovaSystem {
  opts: NovaOptions;
  private countdown: number;
  private phase: NovaPhase = 'quiet';
  private clock = 0;
  /** The name of the most recent body to go. */
  last = '';

  constructor(seed = 1, opts: Partial<NovaOptions> = {}) {
    this.opts = { ...DEFAULT_NOVA, ...opts };
    // First fire happens somewhere inside the first interval, not instantly.
    const r = (seed >>> 0 || 1) / 4294967296;
    this.countdown = this.opts.meanInterval * (0.35 + r * 0.9);
  }

  /** The player forces the nearest star to blow. */
  trigger(name = 'a nearby star'): void {
    if (this.phase !== 'quiet') return;
    this.last = name;
    this.phase = 'flaring';
    this.clock = 0;
  }

  /**
   * Advances the spontaneous countdown and the flash envelope. Returns an
   * event object; `phase` is 'flaring' exactly on the frame it fires.
   */
  tick(dt: number): NovaEvent {
    if (!Number.isFinite(dt) || dt <= 0) return this.now();
    this.countdown -= dt;

    if (this.phase === 'quiet' && this.countdown <= 0) {
      this.trigger('a nearby star');
    }

    if (this.phase !== 'quiet') {
      this.clock += dt;
      if (this.phase === 'flaring' && this.clock >= this.opts.rise) {
        this.phase = 'afterglow';
        this.clock = 0;
      }
      if (this.phase === 'afterglow' && this.clock >= this.opts.fall) {
        this.phase = 'quiet';
        this.clock = 0;
        this.countdown = this.opts.meanInterval * (0.7 + Math.random() * 0.6);
      }
    }
    return this.now();
  }

  now(): NovaEvent {
    let flash = 0;
    if (this.phase === 'flaring') {
      flash = Math.min(1, this.clock / Math.max(this.opts.rise, 1e-3));
    } else if (this.phase === 'afterglow') {
      flash = Math.max(0, 1 - this.clock / Math.max(this.opts.fall, 1e-3));
    }
    return { name: this.last, flash, phase: this.phase };
  }

  get active(): boolean { return this.phase !== 'quiet'; }

  stats(): Record<string, string> {
    return {
      'Supernova': this.phase === 'quiet'
        ? 'quiet'
        : this.phase === 'flaring' ? '⚡ FLARING' : 'afterglow'
    };
  }
}
