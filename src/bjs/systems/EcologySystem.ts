/**
 * EcologySystem — life that eats, flees and follows the light.
 *
 * Species on a planet so far just wander. This adds the two dynamics that
 * turn wandering into an ecosystem:
 *
 *   - Predator/prey. The classic Lotka-Volterra pair: prey breed and get
 *     eaten, predators starve without prey, and the two populations rise and
 *     fall out of phase. Two numbers per planet, stepped at frame rate.
 *   - Migration. Life follows the day/night terminator: grazers drift toward
 *     the lit side at dawn and away from the harsh midday, which is why a
 *     world's creatures cluster where the light is soft rather than
 *     scattering evenly.
 *
 * Pure arithmetic, no Babylon, so it is testable and can run for any planet.
 */

export interface EcologyParams {
  /** Prey growth rate. */
  preyGrowth: number;
  /** Predation rate: how fast predators convert prey. */
  predation: number;
  /** Predator death rate. */
  predatorDeath: number;
  /** Predator reproduction per prey eaten. */
  predatorEfficiency: number;
}

export const DEFAULT_ECOLOGY: EcologyParams = {
  // Tuned so the pair genuinely oscillates: the prey overshoots, the
  // predators boom, the prey crashes, and the cycle repeats - the signature
  // predator/prey wave rather than a damped crawl to a fixed point.
  preyGrowth: 0.6,
  predation: 0.5,
  predatorDeath: 0.3,
  predatorEfficiency: 0.4
};

export interface EcologyState {
  prey: number;
  predator: number;
  /** 0..1 how strongly life is drawn toward the terminator right now. */
  migration: number;
}

export class EcologySystem {
  opts: EcologyParams;
  private prey: number;
  private predator: number;

  constructor(prey = 1.0, predator = 0.5, opts: Partial<EcologyParams> = {}) {
    this.opts = { ...DEFAULT_ECOLOGY, ...opts };
    this.prey = Math.max(0, Math.min(3, prey));
    this.predator = Math.max(0, Math.min(3, predator));
  }

  /** Advances one step of the predator/prey cycle. */
  step(dt: number): void {
    if (!Number.isFinite(dt) || dt <= 0) return;
    const p = this.opts;
    const x = this.prey;
    const y = this.predator;

    // Semi-implicit Euler: the new prey feeds the predator term directly, so
    // a long frame cannot overshoot into a negative population.
    const dx = (p.preyGrowth * x - p.predation * x * y) * dt;
    const dy = (p.predatorEfficiency * p.predation * x * y - p.predatorDeath * y) * dt;

    this.prey = Math.max(0.001, Math.min(3, x + dx));
    this.predator = Math.max(0.001, Math.min(3, y + dy));
  }

  /**
   * How hard life is pulled toward the soft light near the terminator.
   *
   * `daylight` is 0 (deep night) .. 1 (high noon). Life avoids both the
   * frozen dark and the harsh noon, peaking toward the dawn/dusk band.
   */
  static migration(daylight: number): number {
    const d = Math.max(0, Math.min(1, Number.isFinite(daylight) ? daylight : 0.5));
    // Two gaussians centred at dawn (0.25) and dusk (0.75).
    const dawn = Math.exp(-((d - 0.25) * (d - 0.25)) / 0.02);
    const dusk = Math.exp(-((d - 0.75) * (d - 0.75)) / 0.02);
    return Math.max(0, Math.min(1, (dawn + dusk) * 0.7));
  }

  state(daylight = 0.5): EcologyState {
    return {
      prey: this.prey,
      predator: this.predator,
      migration: EcologySystem.migration(daylight)
    };
  }

  stats(): Record<string, string> {
    return {
      'Prey population': (this.prey * 100).toFixed(0) + '%',
      'Predator population': (this.predator * 100).toFixed(0) + '%'
    };
  }
}
