/**
 * BlackHoleFeeding — the hole eats, and you can see it.
 *
 * A black hole that never gains mass is a hole with no history. In Sandbox,
 * whole worlds are dragged in and torn apart; this watches those events and
 * turns each one into a feeding: a flare that peaks and fades as the hole
 * brightens, which is the closest the sim can get to watching an accretion
 * disk respond to a meal. It also counts feedings for the challenge system.
 *
 * Pure timing and counting - the app owns the bloom/lens response.
 */

export interface FeedEvent {
  /** 0..1 how bright the disk flare is right now. */
  flare: number;
  /** Number of bodies fed so far. */
  fed: number;
}

export class BlackHoleFeeding {
  private flare = 0;
  private fedCount = 0;
  private decay: number;

  constructor(decay = 0.9) {
    this.decay = decay;
  }

  /** A body has crossed the horizon. Re-arms the flare. */
  feed(): void {
    this.flare = 1;
    this.fedCount++;
  }

  /** Advances the flare envelope. */
  tick(dt: number): FeedEvent {
    if (!Number.isFinite(dt) || dt <= 0) return this.now();
    this.flare = Math.max(0, this.flare - dt * this.decay);
    return this.now();
  }

  now(): FeedEvent {
    return { flare: Math.min(1, this.flare), fed: this.fedCount };
  }

  get count(): number { return this.fedCount; }

  stats(): Record<string, string> {
    return { 'Bodies fed': String(this.fedCount) };
  }
}
