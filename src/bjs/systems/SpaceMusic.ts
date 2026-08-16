/**
 * SpaceMusic — generative ambient score, and the satellite's own noise.
 *
 * Three separate things live here, because the user asked for three
 * separate things and they must be switchable independently:
 *
 *   1. MUSIC. Slow, generative, tonal. Not a loop of a file - there are no
 *      audio assets in this project, the same way there are no image
 *      assets in the sky. It is built from oscillators over a fixed set of
 *      scale degrees, with note choice driven by a seeded PRNG.
 *
 *   2. SATELLITE HUM. The conceit of the satellite HUD carried into sound:
 *      a soft low hum with a very slight amplitude vibration, framed as
 *      coming from the platform the camera is mounted on rather than from
 *      space or from the ship. Space itself stays silent.
 *
 *   3. BLACK HOLE WIND. A very faint filtered noise that rises only near a
 *      horizon. Not a roar - the brief was explicit that it should be
 *      barely there.
 *
 * WHY THE VIBRATION IS NOT A SINE WAVE. The project rule is no periodic
 * time loops, and a tremolo is the definition of one. The hum's vibration
 * is instead driven by a slow random walk: it drifts, it never repeats, and
 * it has no detectable period. It reads as a real machine rather than as an
 * LFO, which is exactly the difference asked for.
 *
 * NOTHING IS CREATED UNTIL START. Browsers refuse audio before a gesture,
 * and building an AudioContext at import time produces a console warning
 * and a suspended context on every load.
 */

/** A scale, in semitones from the root. */
export type ScaleName = 'aeolian' | 'dorian' | 'lydian' | 'pentatonic';

export const SCALES: Record<ScaleName, number[]> = {
  // Natural minor: the default. Spacious and slightly melancholy without
  // being funereal.
  aeolian: [0, 2, 3, 5, 7, 8, 10],
  // Minor with a raised sixth - lifts the mood without going major.
  dorian: [0, 2, 3, 5, 7, 9, 10],
  // Major with a raised fourth. Used for the bright, wondrous moments.
  lydian: [0, 2, 4, 6, 7, 9, 11],
  // Five notes, no semitone clashes: nothing can sound wrong.
  pentatonic: [0, 3, 5, 7, 10]
};

export interface MusicSettings {
  /** Master music gain, 0..1. */
  volume: number;
  /** Root note in Hz. A2 by default - low enough to stay out of the way. */
  rootHz: number;
  /** Which scale to draw notes from. */
  scale: ScaleName;
  /** Seconds between note onsets, before humanisation. */
  notePeriod: number;
  /** How long a note takes to fade in and out, seconds. */
  attack: number;
  release: number;
  /** Satellite hum gain, 0..1. */
  humVolume: number;
  /** Hum fundamental, Hz. */
  humHz: number;
  /** Depth of the hum's amplitude vibration, 0..1. */
  vibration: number;
  /** Black hole wind gain, 0..1. */
  windVolume: number;
  /** Distance at which the wind becomes audible, world units. */
  windRange: number;
}

export const DEFAULT_MUSIC: MusicSettings = {
  volume: 0.34,
  // E2. Deep enough to sit in the chest rather than the ear - the vast,
  // underwater register a Subnautica-style score lives in.
  rootHz: 82.4,
  scale: 'aeolian',
  // Very sparse: a note every five-and-a-bit seconds, with a long bloom,
  // so the melody drifts rather than plays - the difference between a
  // "soundtrack" and an ambient pressure.
  notePeriod: 5.4,
  attack: 2.8,
  release: 5.2,
  humVolume: 0.22,
  // 34 Hz. Felt more than heard, which is the point of framing it as the
  // satellite's own vibration.
  humHz: 34,
  vibration: 0.18,
  windVolume: 0.30,
  windRange: 2600
};

/**
 * Frequency of a scale degree.
 *
 * Equal temperament: each semitone is the twelfth root of two. Octaves are
 * handled by the degree running past the end of the scale, so a melody can
 * wander upward without any special casing.
 */
export function degreeToHz(
  degree: number, s: MusicSettings = DEFAULT_MUSIC
): number {
  const scale = SCALES[s.scale] ?? SCALES.aeolian;
  const n = scale.length;
  const d = Math.round(Number.isFinite(degree) ? degree : 0);
  // Floor division, so negative degrees go DOWN an octave rather than
  // wrapping to the top of the same one.
  const octave = Math.floor(d / n);
  const idx = ((d % n) + n) % n;
  const semis = scale[idx] + octave * 12;
  return Math.max(1, s.rootHz * Math.pow(2, semis / 12));
}

/**
 * Wind gain near a black hole: 0 far away, 1 at the horizon.
 *
 * Cubic rather than the squared curve used for the singularity rumble, so
 * it stays almost silent until genuinely close. The brief asked for "very
 * very faint", and a gentler curve would have it audible across half a
 * system.
 */
export function windGain(
  distance: number, s: MusicSettings = DEFAULT_MUSIC
): number {
  if (!Number.isFinite(distance)) return 0;
  const r = Math.max(0, s.windRange);
  if (r <= 0 || distance >= r) return 0;
  const t = 1 - Math.max(0, distance) / r;
  return Math.min(1, t * t * t);
}

/** Deterministic PRNG, so a seed always writes the same piece. */
function mulberry(seed: number): () => number {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/**
 * Chooses the next scale degree.
 *
 * A weighted random walk rather than uniform choice. Small steps are much
 * more likely than leaps, which is the single cheapest way to make
 * generated music sound composed instead of random. The range is clamped
 * so it cannot wander into inaudibility over a long session.
 */
export function nextDegree(current: number, rnd: () => number): number {
  const r = rnd();
  // -1, 0, +1 most of the time; occasional third or fifth leaps.
  const step = r < 0.30 ? -1
    : r < 0.58 ? 1
      : r < 0.70 ? 0
        : r < 0.80 ? 2
          : r < 0.88 ? -2
            : r < 0.94 ? 4
              : -4;
  return Math.max(-7, Math.min(14, current + step));
}

export class SpaceMusic {
  settings: MusicSettings;
  private ctx: AudioContext | null = null;
  private musicGain: GainNode | null = null;
  private humGain: GainNode | null = null;
  private humOsc: OscillatorNode | null = null;
  private humVib: GainNode | null = null;
  private windGainNode: GainNode | null = null;
  private windSrc: AudioBufferSourceNode | null = null;
  private windFilter: BiquadFilterNode | null = null;

  private rnd: () => number;
  private degree = 0;
  private sinceNote = 0;
  /** Slow random walk driving the hum vibration. Never periodic. */
  private vibWalk = 0;
  private vibTarget = 0;

  private musicOn = true;
  private humOn = true;
  private windOn = true;
  private started = false;

  constructor(settings: Partial<MusicSettings> = {}, seed = 0x5eed) {
    this.settings = { ...DEFAULT_MUSIC, ...settings };
    this.rnd = mulberry(seed);
  }

  get running(): boolean { return this.started; }
  get musicEnabled(): boolean { return this.musicOn; }
  get humEnabled(): boolean { return this.humOn; }
  get windEnabled(): boolean { return this.windOn; }

  setMusicEnabled(v: boolean): void {
    this.musicOn = v;
    if (this.musicGain && this.ctx) {
      this.ramp(this.musicGain.gain, v ? this.settings.volume : 0, 0.6);
    }
  }

  setHumEnabled(v: boolean): void {
    this.humOn = v;
    if (this.humGain && this.ctx) {
      this.ramp(this.humGain.gain, v ? this.settings.humVolume : 0, 0.6);
    }
  }

  setWindEnabled(v: boolean): void {
    this.windOn = v;
    if (!v && this.windGainNode && this.ctx) {
      this.ramp(this.windGainNode.gain, 0, 0.4);
    }
  }

  /** Linear ramp helper; never an instant set, which clicks. */
  private ramp(p: AudioParam, to: number, secs: number): void {
    if (!this.ctx) return;
    const t = this.ctx.currentTime;
    try {
      p.cancelScheduledValues(t);
      p.setValueAtTime(p.value, t);
      p.linearRampToValueAtTime(Math.max(0, to), t + Math.max(0.01, secs));
    } catch {
      // A detached context throws; there is nothing useful to do about it
      // and it must not take the frame down.
    }
  }

  /**
   * Builds the graph. Must be called from a user gesture.
   *
   * @param ctx an existing AudioContext to share, or null to make one.
   */
  start(ctx?: AudioContext | null): boolean {
    if (this.started) return true;
    try {
      const Ctor = (globalThis as any).AudioContext
        ?? (globalThis as any).webkitAudioContext;
      if (!ctx && !Ctor) return false;
      const c = ctx ?? new Ctor();
      this.ctx = c;

      // ---- music bus ----
      const mg = c.createGain();
      mg.gain.value = this.musicOn ? this.settings.volume : 0;
      // A low-pass and a long feedback echo give the sparse notes a vast,
      // underwater tail - the Subnautica trick of making two soft notes
      // fill a whole chamber instead of plinking in a dry room.
      const lp = c.createBiquadFilter();
      lp.type = 'lowpass';
      lp.frequency.value = 1800;
      lp.Q.value = 0.4;
      const echo = c.createDelay(4.0);
      echo.delayTime.value = 0.66;
      const fb = c.createGain();
      fb.gain.value = 0.38;
      const wet = c.createGain();
      wet.gain.value = 0.55;
      mg.connect(lp);
      lp.connect(c.destination);
      lp.connect(echo);
      echo.connect(fb);
      fb.connect(echo);
      echo.connect(wet);
      wet.connect(c.destination);
      this.musicGain = mg;

      // ---- satellite hum ----
      // Two nodes: a steady oscillator, and a gain the random walk writes
      // to. Keeping the vibration in its own node means the hum's level
      // and its vibration can be adjusted independently.
      const osc = c.createOscillator();
      osc.type = 'sine';
      osc.frequency.value = this.settings.humHz;
      const vib = c.createGain();
      vib.gain.value = 1;
      const hg = c.createGain();
      hg.gain.value = this.humOn ? this.settings.humVolume : 0;
      osc.connect(vib);
      vib.connect(hg);
      hg.connect(c.destination);
      osc.start();
      this.humOsc = osc;
      this.humVib = vib;
      this.humGain = hg;

      // ---- black hole wind ----
      // Looping white noise through a low-pass. Generated into a buffer
      // rather than shipped as a file, same rule as everything else.
      const len = Math.floor(c.sampleRate * 2.5);
      const buf = c.createBuffer(1, len, c.sampleRate);
      const data = buf.getChannelData(0);
      const wr = mulberry(0xa11ce);
      // Brown-ish noise: integrating white noise tilts the spectrum down,
      // which is much closer to wind than raw white hiss.
      let last = 0;
      for (let i = 0; i < len; i++) {
        const white = wr() * 2 - 1;
        last = (last + white * 0.02) / 1.02;
        data[i] = last * 3.2;
      }
      const src = c.createBufferSource();
      src.buffer = buf;
      src.loop = true;
      const filt = c.createBiquadFilter();
      filt.type = 'lowpass';
      filt.frequency.value = 420;
      filt.Q.value = 0.6;
      const wg = c.createGain();
      wg.gain.value = 0;
      src.connect(filt);
      filt.connect(wg);
      wg.connect(c.destination);
      src.start();
      this.windSrc = src;
      this.windFilter = filt;
      this.windGainNode = wg;

      this.started = true;
      return true;
    } catch {
      this.started = false;
      return false;
    }
  }

  /**
   * Advances the score.
   *
   * @param dt          seconds
   * @param holeDistance distance to the nearest black hole, or Infinity
   */
  update(dt: number, holeDistance = Infinity): void {
    if (!this.started || !this.ctx) return;
    if (!Number.isFinite(dt) || dt <= 0) return;

    // ---- hum vibration: a random walk, never an LFO ----
    // The target is re-picked occasionally and the value eases toward it,
    // so the amplitude drifts continuously with no detectable period.
    if (this.rnd() < dt * 0.7) {
      this.vibTarget = (this.rnd() * 2 - 1) * this.settings.vibration;
    }
    this.vibWalk += (this.vibTarget - this.vibWalk) * Math.min(1, dt * 1.1);
    if (this.humVib) {
      // Clamped so the hum can dip and swell but never gate to silence.
      this.humVib.gain.value = Math.max(0.35, Math.min(1.6, 1 + this.vibWalk));
    }

    // ---- wind ----
    if (this.windGainNode) {
      const g = this.windOn
        ? windGain(holeDistance, this.settings) * this.settings.windVolume
        : 0;
      // Short ramp: this changes every frame, so a long one would lag the
      // approach badly.
      this.ramp(this.windGainNode.gain, g, 0.25);
      if (this.windFilter) {
        // Opens up as you get closer, so it gets not just louder but
        // wider - the sound of something actually approaching.
        const t = this.windOn ? windGain(holeDistance, this.settings) : 0;
        this.windFilter.frequency.value = 300 + t * 900;
      }
    }

    // ---- notes ----
    if (!this.musicOn) return;
    this.sinceNote += dt;
    // Humanised interval, so onsets are not on a metronome.
    const period = this.settings.notePeriod * (0.72 + this.rnd() * 0.56);
    if (this.sinceNote >= period) {
      this.sinceNote = 0;
      this.degree = nextDegree(this.degree, this.rnd);
      // A soft sine pad for the body of the score...
      this.playNote(degreeToHz(this.degree, this.settings), 1, 'sine');
      // ...and a low triangle a fifth below, sometimes, for weight.
      if (this.rnd() < 0.34) {
        this.playNote(degreeToHz(this.degree - 4, this.settings), 0.5, 'triangle');
      }
    }
  }

  /**
   * One note: a soft oscillator with a long swell and a long decay.
   *
   * Nodes are created per note and disposed when it ends. That sounds
   * wasteful but is exactly how the Web Audio API is meant to be used -
   * an OscillatorNode is a one-shot, and reusing one means managing
   * envelope state by hand for no benefit.
   */
  private playNote(hz: number, level = 1, type: 'sine' | 'triangle' = 'sine'): void {
    const c = this.ctx;
    if (!c || !this.musicGain) return;
    try {
      const osc = c.createOscillator();
      // Sine reads as a distant pad, triangle as a faint bell with a few
      // odd harmonics - both soft, neither a test tone.
      osc.type = type;
      osc.frequency.value = Math.max(20, hz);

      const g = c.createGain();
      g.gain.value = 0;

      const t = c.currentTime;
      const a = this.settings.attack;
      const r = this.settings.release;
      const peak = 0.22 * level;
      g.gain.setValueAtTime(0, t);
      g.gain.linearRampToValueAtTime(peak, t + a);
      g.gain.linearRampToValueAtTime(0, t + a + r);

      osc.connect(g);
      g.connect(this.musicGain);
      osc.start(t);
      osc.stop(t + a + r + 0.1);
      osc.onended = () => {
        try { osc.disconnect(); g.disconnect(); } catch { /* already gone */ }
      };
    } catch {
      // A note failing must never break the frame.
    }
  }

  stats(): Record<string, string> {
    return {
      'Music': !this.started ? 'idle' : this.musicOn ? 'on' : 'off',
      'Hum': this.humOn ? 'on' : 'off'
    };
  }

  dispose(): void {
    try {
      this.humOsc?.stop();
      this.windSrc?.stop();
    } catch { /* not started */ }
    this.humOsc = null;
    this.windSrc = null;
    this.musicGain = null;
    this.humGain = null;
    this.humVib = null;
    this.windGainNode = null;
    this.windFilter = null;
    this.started = false;
  }
}
