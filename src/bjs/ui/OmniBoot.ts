/**
 * OmniBoot — the multi-stage cinematic load-out.
 *
 * The moment the player commits to the sandbox (Play, a universe preset, or
 * skipping the intro), the cockpit runs its own boot choreography instead of
 * a silent cut. Five stages unfold on one timeline:
 *
 *   1. MATRIX BAR      a neon tracking bar fills under cascading boot text.
 *   2. DISINTEGRATING  a canvas field of tiny circles and squares shatters
 *      GEOMETRY        outward from the centre.
 *   3. WARP DEFROST    a frosted glass veil over a warp funnel, melting as
 *                      the star lines snap back to points.
 *   4. VITALS SPEECH   a ticker streams telemetry lines while the Web Speech
 *                      API reads them aloud in the suit's own voice.
 *   5. JOURNEY         "SPACE JOURNEY ACTIVE" flares, the overlay fades out,
 *      ENGAGED         and native pointer-lock look is requested.
 *
 * The whole thing is skippable in spirit: it self-disposes, can never block
 * the game past its own short lifetime, and every external dependency
 * (speech synthesis, canvas) is guarded so a missing API degrades to the
 * visual alone rather than throwing.
 */

export const OMNI_TICKER_LINES = [
  'INITIALIZING SPACE WALK...',
  'INITIALIZING PLAYGROUND WORLD...',
  'WORLD ACTIVE...',
  'BLEEDING UNIVERSES TOGETHER...',
  'LOADED...',
  'SPAWNING CAMERA...',
  'CAMERA CALIBRATED...',
  'LOADING SYSTEMS CALIBRATED...',
  'VITALS STABILIZE.'
];

export const OMNI_BOOT_LINES = [
  'EXOSUIT BOOTING UP...',
  'OXYGEN TANKS STORED, READY FOR LIFT...',
  'TANKS IN HASH!'
];

/** Total runtime, seconds. */
const OMNI_DURATION = 4.4;

interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  size: number;
  shape: number;   // 0 = circle, 1 = square
  life: number;
}

export class OmniBoot {
  private el: HTMLDivElement | null = null;
  private canvas: HTMLCanvasElement | null = null;
  private particles: Particle[] = [];
  private running = false;
  private onDone: (() => void) | null = null;
  private startTime = 0;
  private raf = 0;
  private spoken = false;

  get isRunning(): boolean { return this.running; }

  /** Launches the boot sequence. Idempotent: a second call is a no-op. */
  start(onDone?: () => void): void {
    if (this.running || this.el) return;
    this.running = true;
    this.onDone = onDone ?? null;
    this.build();
    this.speak();
    this.seedParticles();
    this.startTime = performance.now();
    this.raf = requestAnimationFrame(this.tick);
  }

  /* ------------------------------- build ------------------------------- */

  private build(): void {
    const el = document.createElement('div');
    el.className = 'omni-boot';
    el.innerHTML = `
      <canvas class="omni-canvas" aria-hidden="true"></canvas>
      <div class="omni-warp" aria-hidden="true"></div>
      <div class="omni-grid" aria-hidden="true"></div>
      <div class="omni-stage">
        <div class="omni-matrix">
          <div class="omni-line">${OMNI_BOOT_LINES[0]}</div>
          <div class="omni-line dim">${OMNI_BOOT_LINES[1]}</div>
          <div class="omni-count">3 · 2 · 1</div>
          <div class="omni-line accent">${OMNI_BOOT_LINES[2]}</div>
          <div class="omni-bar"><i id="omniFill"></i></div>
        </div>
        <div class="omni-ticker" id="omniTicker"></div>
        <div class="omni-engage">SPACE JOURNEY ACTIVE</div>
      </div>`;
    document.body.appendChild(el);
    this.el = el;
    this.canvas = el.querySelector('canvas');
    if (this.canvas) {
      this.canvas.width = window.innerWidth;
      this.canvas.height = window.innerHeight;
    }
  }

  /** The suit's voice: native Web Speech, tuned to read like an onboard AI. */
  private speak(): void {
    try {
      const w = window as unknown as {
        speechSynthesis?: { speak: (u: unknown) => void; cancel?: () => void; getVoices?: () => Array<{ lang: string; name: string }> };
        SpeechSynthesisUtterance?: new (text: string) => {
          pitch: number; rate: number; voice: unknown; text: string;
        };
      };
      const synth = w.speechSynthesis;
      const U = w.SpeechSynthesisUtterance;
      if (!synth || !U) return;

      const text = OMNI_TICKER_LINES.map((l) => l.replace(/\.\.\.$/, '.')).join(' ');
      const u = new U(text);
      u.pitch = 1.05;       // bright, but not cartoonish
      u.rate = 0.95;        // measured, deliberate
      const voices = synth.getVoices?.() ?? [];
      // Prefer a female US English voice - the classic shipboard AI register.
      const pick = voices.find((v) => /female|samantha|zira|aria|jenny|google us english/i.test(v.name))
        ?? voices.find((v) => /en[-_]us/i.test(v.lang))
        ?? voices[0];
      if (pick) u.voice = pick;
      try { synth.cancel?.(); } catch { /* no queue to cancel */ }
      synth.speak(u);
    } catch {
      // Speech unavailable: the ticker still carries every line visually.
    }
  }

  /* ------------------------------ particles ----------------------------- */

  private seedParticles(): void {
    const w = window.innerWidth, h = window.innerHeight;
    for (let i = 0; i < 170; i++) {
      const ang = Math.random() * Math.PI * 2;
      const speed = 0.4 + Math.random() * 3.2;
      this.particles.push({
        x: w / 2 + (Math.random() - 0.5) * 40,
        y: h / 2 + (Math.random() - 0.5) * 40,
        vx: Math.cos(ang) * speed,
        vy: Math.sin(ang) * speed,
        size: 1 + Math.random() * 3,
        shape: Math.random() < 0.5 ? 0 : 1,
        life: 1
      });
    }
  }

  /* -------------------------------- tick -------------------------------- */

  private tick = (): void => {
    if (!this.el) return;
    const t = (performance.now() - this.startTime) / 1000;
    this.draw(t);
    this.driveStages(t);
    if (t < OMNI_DURATION) {
      this.raf = requestAnimationFrame(this.tick);
    } else {
      this.finish();
    }
  };

  private draw(t: number): void {
    const c = this.canvas;
    if (!c) return;
    const g = c.getContext('2d');
    if (!g) return;
    g.clearRect(0, 0, c.width, c.height);

    // The disintegrating geometry: outward shatter for the first two thirds,
    // then a violent deceleration as the star lines snap back.
    const phase = Math.min(1, t / 2.0);
    const melt = 1 - Math.min(1, Math.max(0, (t - 1.4) / 0.9));
    for (const p of this.particles) {
      p.x += p.vx * phase * (0.3 + melt);
      p.y += p.vy * phase * (0.3 + melt);
      const a = Math.max(0, 1 - t / (OMNI_DURATION * 0.8));
      const hue = 190 + Math.sin(p.x * 0.01 + t * 3) * 30;
      g.fillStyle = `hsla(${hue}, 90%, 62%, ${a})`;
      if (p.shape === 0) {
        g.beginPath();
        g.arc(p.x, p.y, p.size, 0, Math.PI * 2);
        g.fill();
      } else {
        g.fillRect(p.x, p.y, p.size * 1.6, p.size * 1.6);
      }
    }

    // The warp funnel: a handful of streaks collapsing into point stars.
    g.strokeStyle = 'rgba(0,240,255,0.5)';
    g.lineWidth = 1;
    for (let i = 0; i < 40; i++) {
      const ang = (i / 40) * Math.PI * 2 + t * 0.6;
      const cx = c.width / 2, cy = c.height / 2;
      const len = 40 + Math.sin(t * 4 + i) * 200 * (1 - phase);
      g.beginPath();
      g.moveTo(cx, cy);
      g.lineTo(cx + Math.cos(ang) * len, cy + Math.sin(ang) * len);
      g.stroke();
    }
  }

  private driveStages(t: number): void {
    const el = this.el;
    if (!el) return;

    // Stage 1: the matrix bar fills over the first second.
    const fill = el.querySelector<HTMLElement>('#omniFill');
    if (fill) fill.style.width = Math.min(100, (t / 1.1) * 100).toFixed(0) + '%';

    // Stage 4: the telemetry ticker, one line every ~0.4s.
    const ticker = el.querySelector<HTMLElement>('#omniTicker');
    if (ticker) {
      const idx = Math.min(OMNI_TICKER_LINES.length - 1, Math.floor(t / 0.4));
      let html = '';
      for (let i = 0; i <= idx; i++) html += '<div>' + OMNI_TICKER_LINES[i] + '</div>';
      if (ticker.innerHTML !== html) ticker.innerHTML = html;
    }

    // Stage 5: the final flare, near the end.
    el.classList.toggle('engage', t > OMNI_DURATION - 1.1);
    el.classList.toggle('fading', t > OMNI_DURATION - 0.5);
  }

  private finish(): void {
    cancelAnimationFrame(this.raf);
    this.raf = 0;
    this.running = false;
    try { this.el?.remove(); } catch { /* gone */ }
    this.el = null;
    this.canvas = null;
    this.particles = [];
    this.onDone?.();
    this.onDone = null;
  }

  dispose(): void {
    cancelAnimationFrame(this.raf);
    try { this.el?.remove(); } catch { /* gone */ }
    this.el = null;
    this.canvas = null;
    this.running = false;
    this.onDone = null;
  }
}
