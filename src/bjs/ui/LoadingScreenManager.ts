/**
 * LoadingScreenManager — a self-contained five-act transition scene.
 *
 * This manager deliberately owns every loading concern: DOM, timeline,
 * procedural canvases, native speech and cleanup. The title menu and Babylon
 * scene only receive a completion callback, so neither knows about the
 * cinematic's internal stages.
 */

export const LOADING_TELEMETRY = [
  'INITIALIZING SPACE WALK...',
  'INITIALIZING PLAYGROUND WORLD...',
  'WORLD ACTIVE...',
  'BLEEDING UNIVERSES TOGETHER...',
  'LOADED...',
  'SPAWNING CAMERA...',
  'CAMERA CALIBRATED...',
  'LOADING SYSTEMS CALIBRATED...',
  'VITALS STABILIZE.'
] as const;

export const LOADING_MATRIX = [
  'EXOSUIT BOOTING UP...',
  'OXYGEN TANKS STORED, READY FOR LIFT...',
  '3, 2, 1...',
  'TANKS IN HASH!'
] as const;

const LOADING_TELEMETRY_TEXT = LOADING_TELEMETRY.join('\n');
const DURATION = 10.8;
const MATRIX_END = 2.05;
const SHATTER_END = 4.35;
const DEFROST_END = 6.15;
const ENGAGE_AT = 9.35;

interface Fragment {
  x: number; y: number;
  ox: number; oy: number;
  vx: number; vy: number;
  size: number;
  shape: 0 | 1;
  delay: number;
  spin: number;
  hue: number;
}

interface Star {
  x: number; y: number; z: number; seed: number;
}

export class LoadingScreenManager {
  private el: HTMLDivElement | null = null;
  private fx: HTMLCanvasElement | null = null;
  private frost: HTMLCanvasElement | null = null;
  private fragments: Fragment[] = [];
  private stars: Star[] = [];
  private running = false;
  private startAt = 0;
  private raf = 0;
  private onDone: (() => void) | null = null;
  private backgroundDone = true;
  private speechDone = true;
  private lastTyped = '';
  private spoken = false;
  /** Frost is deliberately 15 fps; high-frequency grain was starving WebGL. */
  private lastFrostDraw = -Infinity;
  private frostCleared = false;
  private resizeHandler = () => this.resize();
  private keyHandler = (e: KeyboardEvent) => {
    // Loading is an atomic transition. Consume bypass keys so neither the
    // menu nor pause layer can surface over the active cinematic.
    if (e.key === 'Escape' || e.key === 'Enter') e.preventDefault();
  };

  get isRunning(): boolean { return this.running; }

  /** Starts once. A second request cannot layer two load scenes together. */
  start(onDone?: () => void, backgroundTask?: () => Promise<void>): void {
    if (this.running || this.el) return;
    this.running = true;
    this.onDone = onDone ?? null;
    this.backgroundDone = !backgroundTask;
    this.speechDone = false;
    this.build();
    this.resize();
    this.seedGeometry();
    // Speech begins with Stage 4 (not while the player is still choosing a
    // mode on the menu, and not over the earlier matrix diagnostics).
    this.spoken = false;
    this.lastFrostDraw = -Infinity;
    this.frostCleared = false;
    window.addEventListener('resize', this.resizeHandler, { passive: true });
    window.addEventListener('keydown', this.keyHandler);
    this.startAt = performance.now();
    this.raf = requestAnimationFrame(this.tick);

    // Give the browser one complete paint with the z-9999 canvas before any
    // synchronous shader compiler work can occupy the main thread.
    if (backgroundTask) window.setTimeout(() => {
      void Promise.resolve().then(backgroundTask).catch((e) => {
        console.warn('Background launch preparation was degraded:', e);
      }).finally(() => { this.backgroundDone = true; });
    }, 50);
  }

  private build(): void {
    const root = document.createElement('div');
    root.className = 'omni-boot loading-screen-manager stage-matrix';
    root.setAttribute('role', 'status');
    root.setAttribute('aria-live', 'polite');
    root.innerHTML = `
      <div class="omni-warp" aria-hidden="true"><i></i></div>
      <canvas class="omni-frost" aria-hidden="true"></canvas>
      <canvas class="omni-canvas" aria-hidden="true"></canvas>
      <div class="omni-grid" aria-hidden="true"></div>
      <div class="omni-vignette" aria-hidden="true"></div>
      <div class="omni-chrome" aria-hidden="true">
        <span>AEON // EXOSUIT OS</span><span id="omniPhase">01 MATRIX LINK</span>
      </div>
      <section class="omni-stage">
        <div class="omni-matrix">
          <small>NEURAL HANDSHAKE · SECURE CHANNEL 77-A</small>
          <div class="omni-line">${LOADING_MATRIX[0]}</div>
          <div class="omni-line dim">${LOADING_MATRIX[1]}</div>
          <div class="omni-count">3&nbsp;&nbsp;2&nbsp;&nbsp;1</div>
          <div class="omni-line accent">${LOADING_MATRIX[3]}</div>
          <div class="omni-bar"><i id="omniFill"></i><b id="omniPct">00%</b></div>
        </div>
        <div class="omni-telemetry">
          <span class="omni-bracket l"></span>
          <div class="omni-ticker" id="omniTicker"></div>
          <span class="omni-bracket r"></span>
        </div>
        <div class="omni-engage"><small>FLIGHT CONTROL // ONLINE</small>SPACE JOURNEY ACTIVE</div>
      </section>
      <div class="omni-skip">BACKGROUND STREAM // SYNCHRONIZED</div>`;
    document.body.appendChild(root);
    this.el = root;
    this.fx = root.querySelector('.omni-canvas');
    this.frost = root.querySelector('.omni-frost');
  }

  private resize(): void {
    const dpr = Math.min(1.5, window.devicePixelRatio || 1);
    for (const c of [this.fx, this.frost]) {
      if (!c) continue;
      c.width = Math.max(1, Math.floor(innerWidth * dpr));
      c.height = Math.max(1, Math.floor(innerHeight * dpr));
      c.style.width = innerWidth + 'px';
      c.style.height = innerHeight + 'px';
    }
    if (this.running) this.seedGeometry();
  }

  /** A screen-filling lattice; the wave reaches each fragment left-to-right. */
  private seedGeometry(): void {
    const w = this.fx?.width ?? innerWidth;
    const h = this.fx?.height ?? innerHeight;
    this.fragments.length = 0;
    const gap = Math.max(14, Math.round(Math.min(w, h) / 42));
    for (let y = gap / 2; y < h; y += gap) {
      for (let x = gap / 2; x < w; x += gap) {
        const jx = x + (Math.random() - .5) * gap * .4;
        const jy = y + (Math.random() - .5) * gap * .4;
        const dx = jx - w * .5, dy = jy - h * .5;
        const len = Math.max(1, Math.hypot(dx, dy));
        const impulse = 1.2 + Math.random() * 5.5;
        this.fragments.push({
          x: jx, y: jy, ox: jx, oy: jy,
          vx: dx / len * impulse + (Math.random() - .5) * 2.2,
          vy: dy / len * impulse + (Math.random() - .5) * 2.2,
          size: 1 + Math.random() * 2.7,
          shape: Math.random() > .46 ? 1 : 0,
          delay: (jx / w) * .72 + Math.random() * .16,
          spin: (Math.random() - .5) * .15,
          hue: 185 + Math.random() * 75
        });
      }
    }
    this.stars = Array.from({ length: 150 }, (_, i) => ({
      x: Math.random() * w, y: Math.random() * h,
      z: .08 + Math.random() * .92, seed: i * 17.17
    }));
  }

  /** Native Web Speech is optional; visual telemetry remains authoritative. */
  private speakVitals(): void {
    try {
      const synth = window.speechSynthesis;
      const U = window.SpeechSynthesisUtterance;
      if (!synth || !U) { this.speechDone = true; return; }
      const u = new U(LOADING_TELEMETRY.map((x) => x.replace(/\.\.\.$/, '.')).join(' '));
      u.onend = () => { this.speechDone = true; };
      u.onerror = () => { this.speechDone = true; };
      u.pitch = 0.9;
      u.rate = 0.82;
      u.volume = 0.95;
      const voices = synth.getVoices?.() ?? [];
      const pick = voices.find((v) => /samantha|zira|aria|jenny|karen|veena|moira/i.test(v.name))
        ?? voices.find((v) => /^en[-_]gb/i.test(v.lang))
        ?? voices.find((v) => /^en/i.test(v.lang));
      if (pick) u.voice = pick;
      synth.cancel();
      synth.speak(u);
    } catch {
      // Kiosk/headless browsers often omit speech synthesis. Never block boot.
      this.speechDone = true;
    }
  }

  private tick = (now: number): void => {
    if (!this.el) return;
    const t = (now - this.startAt) / 1000;
    this.draw(t);
    this.drive(t);
    const ready = t >= DURATION && this.backgroundDone && this.speechDone;
    // Speech engines and GPU drivers are external systems. A hard ceiling
    // guarantees neither can strand the player on the loading scene.
    if (!ready && t < 28) this.raf = requestAnimationFrame(this.tick);
    else this.finish();
  };

  private draw(t: number): void {
    const c = this.fx, frost = this.frost;
    if (!c || !frost) return;
    const g = c.getContext('2d'), fg = frost.getContext('2d');
    if (!g || !fg) return;
    const w = c.width, h = c.height;
    g.clearRect(0, 0, w, h);

    // Warp defrost: long lines brake with an aggressive quartic curve and
    // finish as stable points, revealing the destination behind the frost.
    const warpT = Math.max(0, Math.min(1, (t - MATRIX_END) / (DEFROST_END - MATRIX_END)));
    const velocity = Math.pow(1 - warpT, 4);
    g.globalCompositeOperation = 'lighter';
    for (const s of this.stars) {
      const dx = s.x - w / 2, dy = s.y - h / 2;
      const n = Math.max(1, Math.hypot(dx, dy));
      const len = 2 + velocity * (120 + s.z * 580);
      g.beginPath();
      g.moveTo(s.x - dx / n * len, s.y - dy / n * len);
      g.lineTo(s.x, s.y);
      g.strokeStyle = `rgba(${120 + s.z * 100},${190 + s.z * 55},255,${.22 + s.z * .6})`;
      g.lineWidth = .7 + s.z * 1.5;
      g.stroke();
      if (warpT > .72) {
        g.fillStyle = `rgba(220,245,255,${(warpT - .72) * 2.8})`;
        g.fillRect(s.x, s.y, 1 + s.z * 1.8, 1 + s.z * 1.8);
      }
    }

    // Dense geometry starts as a resolved display matrix. A travelling heat
    // front releases each tile, then radial impulse and drag shatter it out.
    const shatter = Math.max(0, (t - MATRIX_END) / (SHATTER_END - MATRIX_END));
    for (const p of this.fragments) {
      const local = Math.max(0, shatter - p.delay);
      if (local > 0) {
        const accel = Math.min(1, local * 3.8);
        p.x += p.vx * (1 + accel * 4.2);
        p.y += p.vy * (1 + accel * 4.2);
        p.vx *= .997; p.vy *= .997;
      }
      const fade = local > 0 ? Math.max(0, 1 - local * .72) : Math.min(.5, shatter * 1.6);
      g.fillStyle = `hsla(${p.hue},95%,68%,${fade})`;
      g.save();
      g.translate(p.x, p.y);
      g.rotate(local * p.spin * 18);
      if (p.shape === 0) {
        g.beginPath(); g.arc(0, 0, p.size, 0, Math.PI * 2); g.fill();
      } else g.fillRect(-p.size, -p.size, p.size * 2, p.size * 2);
      g.restore();
    }
    g.globalCompositeOperation = 'source-over';

    // Frost is a secondary veil, so update it at 15 fps instead of forcing
    // tens of thousands of 2D draw calls alongside every WebGL frame.
    const heat = Math.max(0, Math.min(1, (t - 2.55) / 3.45));
    if (heat >= 1) {
      if (!this.frostCleared) {
        fg.clearRect(0, 0, frost.width, frost.height);
        this.frostCleared = true;
      }
    } else if (t - this.lastFrostDraw >= 1 / 15) {
      this.lastFrostDraw = t;
      fg.clearRect(0, 0, frost.width, frost.height);
      fg.fillStyle = `rgba(185,220,255,${.055 * (1 - heat)})`;
      const cell = 12;
      for (let y = 0; y < h; y += cell) {
        for (let x = 0; x < w; x += cell) {
          const n = Math.sin(x * 12.9898 + y * 78.233 + Math.floor(t * 9)) * 43758.5453;
          if ((n - Math.floor(n)) > .58 + heat * .35) fg.fillRect(x, y, cell, cell);
        }
      }
      fg.strokeStyle = `rgba(190,235,255,${.3 * (1 - heat)})`;
      for (let i = 0; i < 36; i++) {
        const x = ((i * 83.71) % w);
        const y = ((i * 151.3 + heat * h * (1.2 + (i % 4) * .16)) % (h + 160)) - 80;
        fg.lineWidth = 1 + (i % 3);
        fg.beginPath(); fg.moveTo(x, y - 8 - heat * 44); fg.lineTo(x, y); fg.stroke();
        fg.beginPath(); fg.arc(x, y, 2 + (i % 4), 0, Math.PI * 2); fg.stroke();
      }
    }
  }

  private drive(t: number): void {
    const el = this.el;
    if (!el) return;
    if (!this.spoken && t >= 5.55) {
      this.spoken = true;
      this.speakVitals();
    }
    const stage = t < MATRIX_END ? 'matrix'
      : t < SHATTER_END ? 'shatter'
        : t < DEFROST_END ? 'defrost'
          : t < ENGAGE_AT ? 'vitals' : 'engage';
    el.className = 'omni-boot loading-screen-manager stage-' + stage
      + (t > DURATION - .6 ? ' fading' : '')
      + (stage === 'engage' ? ' engage' : '');

    const labels: Record<string, string> = {
      matrix: '01 MATRIX LINK', shatter: '02 DISPLAY DECOUPLE',
      defrost: '03 VECTOR BRAKE', vitals: '04 LIFE SUPPORT',
      engage: '05 JOURNEY ENGAGED'
    };
    const phase = el.querySelector<HTMLElement>('#omniPhase');
    if (phase) phase.textContent = labels[stage];

    const progress = Math.min(1, t / DURATION);
    const fill = el.querySelector<HTMLElement>('#omniFill');
    const pct = el.querySelector<HTMLElement>('#omniPct');
    if (fill) fill.style.width = Math.round(progress * 100) + '%';
    if (pct) pct.textContent = String(Math.round(progress * 100)).padStart(2, '0') + '%';

    // Character-level typewriter, including the exact line punctuation.
    const telemetryT = Math.max(0, t - 5.55);
    const chars = Math.min(LOADING_TELEMETRY_TEXT.length, Math.floor(telemetryT * 46));
    const typed = LOADING_TELEMETRY_TEXT.slice(0, chars);
    if (typed !== this.lastTyped) {
      this.lastTyped = typed;
      const ticker = el.querySelector<HTMLElement>('#omniTicker');
      if (ticker) {
        ticker.textContent = typed;
        ticker.scrollTop = ticker.scrollHeight;
      }
    }
  }

  private finish(): void {
    if (!this.el && !this.running) return;
    cancelAnimationFrame(this.raf);
    this.raf = 0;
    window.removeEventListener('resize', this.resizeHandler);
    window.removeEventListener('keydown', this.keyHandler);
    const done = this.onDone;
    this.onDone = null;
    this.running = false;
    const old = this.el;
    old?.classList.add('fading');
    window.setTimeout(() => old?.remove(), 560);
    this.el = null;
    this.fx = null;
    this.frost = null;
    this.fragments.length = 0;
    this.stars.length = 0;
    done?.();
  }

  dispose(): void {
    cancelAnimationFrame(this.raf);
    window.removeEventListener('resize', this.resizeHandler);
    window.removeEventListener('keydown', this.keyHandler);
    this.el?.remove();
    this.el = null;
    this.fx = null;
    this.frost = null;
    this.running = false;
    this.onDone = null;
  }
}
