/**
 * SpaceAudio — the procedural sound matrix.
 *
 * Every sound here is synthesised from oscillators and filtered noise. There
 * are no audio files, which means no download cost and no asset that can go
 * missing, and it lets the sound respond continuously to simulation state
 * rather than triggering fixed clips.
 *
 * Three voices, each tied to something the player is actually doing:
 *
 *   - Hum: a low sine bed whose pitch rises with velocity. This is what makes
 *     speed audible when there is no scenery close enough to sweep past.
 *   - Warp: bandpass-filtered noise that sweeps upward while the drive spools.
 *   - Singularity: a sub-bass rumble that grows as you near a black hole.
 *
 * Everything is gain-ramped rather than switched. An abrupt gain change is
 * heard as a click, which is the usual reason synthesised audio sounds cheap.
 *
 * Browsers refuse to start an AudioContext before a user gesture, so this
 * class must tolerate being constructed early, failing to start, and being
 * resumed later on the first click or keypress.
 */

/** Tunables, all in Hz or 0..1 gain. */
export interface AudioSettings {
  master: number;
  hum: number;
  warp: number;
  singularity: number;
  /** Electromagnetic hiss near dense star fields. */
  static: number;
  /** Hum pitch at rest and at full speed. */
  humBaseHz: number;
  humTopHz: number;
  /** Speed, world units/sec, that counts as "full speed" for pitch mapping. */
  humSpeedRef: number;
  /** Distance at which a black hole first becomes audible, world units. */
  singularityRange: number;
}

export const DEFAULT_AUDIO: AudioSettings = {
  master: 0.55,
  hum: 0.5,
  warp: 0.6,
  singularity: 0.75,
  static: 0.4,
  humBaseHz: 55,
  humTopHz: 80,
  humSpeedRef: 400,
  singularityRange: 900
};

/**
 * Maps speed to hum frequency.
 *
 * Pitch perception is logarithmic, so a linear speed-to-Hz map sounds like
 * nothing is happening at low speed and then lurches. A square root spreads
 * the audible change across the range that actually gets used.
 */
export function humFrequency(speed: number, s: AudioSettings = DEFAULT_AUDIO): number {
  if (!Number.isFinite(speed) || speed <= 0) return s.humBaseHz;
  const t = Math.min(1, Math.sqrt(speed / Math.max(1e-6, s.humSpeedRef)));
  return s.humBaseHz + (s.humTopHz - s.humBaseHz) * t;
}

/**
 * Proximity gain for the singularity rumble: 0 far away, 1 at the horizon.
 *
 * Squared falloff, so the growth is slow while approaching and steep at the
 * end - the same shape as the gravity that is supposedly causing it.
 */
export function singularityGain(distance: number, s: AudioSettings = DEFAULT_AUDIO): number {
  if (!Number.isFinite(distance)) return 0;
  const r = Math.max(0, s.singularityRange);
  if (r <= 0 || distance >= r) return 0;
  const t = 1 - Math.max(0, distance) / r;
  return Math.min(1, t * t);
}

export class SpaceAudio {
  private ctx: AudioContext | null = null;
  private master: GainNode | null = null;

  private humOsc: OscillatorNode | null = null;
  private humGain: GainNode | null = null;

  private warpSrc: AudioBufferSourceNode | null = null;
  private warpFilter: BiquadFilterNode | null = null;
  private warpGain: GainNode | null = null;

  private subOsc: OscillatorNode | null = null;
  private subGain: GainNode | null = null;

  private staticSrc: AudioBufferSourceNode | null = null;
  private staticFilter: BiquadFilterNode | null = null;
  private staticGain: GainNode | null = null;

  settings: AudioSettings;
  started = false;
  /** Set when the browser refused to give us audio, so the UI can say so. */
  unavailable = false;
  muted = false;

  constructor(settings: Partial<AudioSettings> = {}) {
    this.settings = { ...DEFAULT_AUDIO, ...settings };
  }

  /**
   * Builds the graph. Call from a user gesture; calling earlier is harmless
   * but the context will stay suspended until resume() lands one.
   */
  start(): boolean {
    if (this.started || this.unavailable) return this.started;
    const Ctor = (globalThis as any).AudioContext ?? (globalThis as any).webkitAudioContext;
    if (!Ctor) { this.unavailable = true; return false; }

    try {
      const ctx: AudioContext = new Ctor();
      this.ctx = ctx;

      const master = ctx.createGain();
      master.gain.value = this.settings.master;
      master.connect(ctx.destination);
      this.master = master;

      // ---- ambient hum ----
      const humOsc = ctx.createOscillator();
      humOsc.type = 'sine';
      humOsc.frequency.value = this.settings.humBaseHz;
      const humGain = ctx.createGain();
      humGain.gain.value = 0;
      // A gentle lowpass keeps the sine from sounding like a test tone.
      const humLp = ctx.createBiquadFilter();
      humLp.type = 'lowpass';
      humLp.frequency.value = 220;
      humOsc.connect(humLp); humLp.connect(humGain); humGain.connect(master);
      humOsc.start();
      this.humOsc = humOsc; this.humGain = humGain;

      // ---- warp field: filtered noise ----
      // Two seconds of noise looped is indistinguishable from continuous
      // noise and costs a fraction of the memory.
      const frames = Math.floor(ctx.sampleRate * 2);
      const buf = ctx.createBuffer(1, frames, ctx.sampleRate);
      const data = buf.getChannelData(0);
      let last = 0;
      for (let i = 0; i < frames; i++) {
        // Brown-ish noise: integrating white noise tilts energy downward,
        // which reads as a powerful field rather than a hiss.
        last = (last + (Math.random() * 2 - 1) * 0.02);
        last = Math.max(-1, Math.min(1, last * 0.996));
        data[i] = last * 3.2;
      }
      const warpSrc = ctx.createBufferSource();
      warpSrc.buffer = buf; warpSrc.loop = true;
      const warpFilter = ctx.createBiquadFilter();
      warpFilter.type = 'bandpass';
      warpFilter.frequency.value = 320;
      warpFilter.Q.value = 6;
      const warpGain = ctx.createGain();
      warpGain.gain.value = 0;
      warpSrc.connect(warpFilter); warpFilter.connect(warpGain); warpGain.connect(master);
      warpSrc.start();
      this.warpSrc = warpSrc; this.warpFilter = warpFilter; this.warpGain = warpGain;

      // ---- electromagnetic starfield static ----
      // Sonified sensor data rather than a sound anything could make in
      // vacuum: denser star fields mean more incident radiation, so the
      // hiss brightens. Shares the noise buffer with the warp voice - the
      // character comes from the filter, not the source.
      const staticSrc = ctx.createBufferSource();
      staticSrc.buffer = buf; staticSrc.loop = true;
      const staticFilter = ctx.createBiquadFilter();
      // Highpass, so it sits well above the hum and never muddies it.
      staticFilter.type = 'highpass';
      staticFilter.frequency.value = 2200;
      staticFilter.Q.value = 0.7;
      const staticGain = ctx.createGain();
      staticGain.gain.value = 0;
      staticSrc.connect(staticFilter); staticFilter.connect(staticGain);
      staticGain.connect(master);
      staticSrc.start();
      this.staticSrc = staticSrc; this.staticFilter = staticFilter;
      this.staticGain = staticGain;

      // ---- singularity sub-bass ----
      const subOsc = ctx.createOscillator();
      subOsc.type = 'sine';
      subOsc.frequency.value = 32;
      const subGain = ctx.createGain();
      subGain.gain.value = 0;
      subOsc.connect(subGain); subGain.connect(master);
      subOsc.start();
      this.subOsc = subOsc; this.subGain = subGain;

      this.started = true;
      return true;
    } catch {
      // No audio hardware, or a policy block. The simulation runs silent.
      this.unavailable = true;
      return false;
    }
  }

  /** Resumes a context suspended by autoplay policy. */
  resume(): void {
    try { void this.ctx?.resume?.(); } catch { /* nothing to resume */ }
  }

  private ramp(param: AudioParam | undefined, to: number, seconds = 0.12): void {
    if (!param || !this.ctx) return;
    const t = this.ctx.currentTime;
    try {
      param.cancelScheduledValues(t);
      // setTargetAtTime glides exponentially and never clicks, unlike a
      // direct assignment.
      param.setTargetAtTime(to, t, Math.max(0.01, seconds));
    } catch { /* param not automatable in this engine */ }
  }

  /**
   * Per-frame update. All three voices are driven from live state, so the
   * caller only has to hand over what it already knows.
   */
  update(state: {
    speed?: number;
    warping?: boolean;
    warpCharge?: number;
    singularityDistance?: number;
    /** 0-1, how crowded the local star field is. */
    starDensity?: number;
  }): void {
    if (!this.started || !this.ctx) return;
    const s = this.settings;
    const gate = this.muted ? 0 : 1;

    // hum
    const speed = Number.isFinite(state.speed as number) ? (state.speed as number) : 0;
    this.ramp(this.humOsc?.frequency, humFrequency(speed, s), 0.25);
    this.ramp(this.humGain?.gain, s.hum * 0.22 * gate, 0.4);

    // warp
    const charge = Math.max(0, Math.min(1, state.warpCharge ?? (state.warping ? 1 : 0)));
    this.ramp(this.warpGain?.gain, s.warp * 0.16 * charge * gate, 0.18);
    // Sweeping the passband upward is what makes it read as acceleration
    // rather than just louder noise.
    this.ramp(this.warpFilter?.frequency, 320 + charge * 2600, 0.3);

    // starfield static: louder AND crisper in crowded space
    const dens = Math.max(0, Math.min(1, state.starDensity ?? 0));
    this.ramp(this.staticGain?.gain, s.static * 0.05 * dens * gate, 0.6);
    // Raising the corner frequency is what "crispier" means acoustically:
    // more of the low end is filtered away, leaving only the fine hiss.
    this.ramp(this.staticFilter?.frequency, 2200 + dens * 5200, 0.7);

    // singularity
    const g = singularityGain(state.singularityDistance ?? Infinity, s);
    this.ramp(this.subGain?.gain, s.singularity * 0.3 * g * gate, 0.35);
    // Deepens as you fall in.
    this.ramp(this.subOsc?.frequency, 32 - g * 12, 0.5);
  }

  setMuted(m: boolean): void {
    this.muted = m;
    if (!this.started) return;
    this.ramp(this.master?.gain, m ? 0 : this.settings.master, 0.2);
  }

  setMaster(v: number): void {
    this.settings.master = Math.max(0, Math.min(1, v));
    if (!this.muted) this.ramp(this.master?.gain, this.settings.master, 0.1);
  }

  dispose(): void {
    try {
      this.humOsc?.stop(); this.warpSrc?.stop();
      this.subOsc?.stop(); this.staticSrc?.stop();
    } catch { /* already stopped */ }
    try { void this.ctx?.close?.(); } catch { /* already closed */ }
    this.ctx = null; this.started = false;
  }
}
