/**
 * TimeRewind — scrub the last few seconds.
 *
 * A supernova, an impact, a comet rounding the sun: the moment is over
 * before you can look at it twice. This records a rolling buffer of the
 * player's motion and lets them step back a short way along their own path,
 * so a dramatic instant can be replayed. It rewinds the *player*, not the
 * whole universe - the honest, cheap version of a time machine.
 *
 * Pure ring buffer, no Babylon, testable exactly.
 */

export interface MotionSample {
  x: number;
  y: number;
  z: number;
  vx: number;
  vy: number;
  vz: number;
}

export class TimeRewind {
  private frames: MotionSample[] = [];
  /** Maximum seconds of history to keep (at ~60fps that is 60x this). */
  window: number;
  /** Frame interval the caller records at, seconds. */
  step: number;

  constructor(windowSeconds = 8, fps = 60) {
    this.window = windowSeconds;
    this.step = 1 / Math.max(1, fps);
  }

  /** Records one sample, capping the buffer to the window. */
  record(x: number, y: number, z: number, vx = 0, vy = 0, vz = 0): void {
    if (![x, y, z].every(Number.isFinite)) return;
    this.frames.push({ x, y, z, vx, vy, vz });
    const max = Math.ceil(this.window / this.step);
    if (this.frames.length > max) this.frames.shift();
  }

  /** How far back we can rewind, in seconds. */
  reachable(): number {
    return this.frames.length * this.step;
  }

  /**
   * Rewinds the player a short way along their own path.
   *
   * Returns the sample to restore and the distance rewound, or null when
   * there is no history. The restored state zeroes velocity so the rewind
   * does not immediately re-run whatever the player was doing.
   */
  rewind(seconds: number): { state: MotionSample; rewound: number } | null {
    const want = Math.max(0, seconds);
    if (!this.frames.length || want <= 0) return null;
    const back = Math.min(this.frames.length - 1, Math.round(want / this.step));
    const sample = this.frames[this.frames.length - 1 - back];
    if (!sample) return null;
    // Everything after the restore point is consumed, like a tape rewinding.
    this.frames.length -= back;
    return {
      state: { ...sample, vx: 0, vy: 0, vz: 0 },
      rewound: back * this.step
    };
  }

  clear(): void {
    this.frames.length = 0;
  }

  stats(): Record<string, string> {
    return {
      'Rewind window': this.reachable().toFixed(1) + 's'
    };
  }
}
