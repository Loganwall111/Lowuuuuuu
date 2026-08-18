/**
 * MouseLook — the unified Input & Navigation Core (rebuilt from blank slate).
 *
 * A SINGULAR mouse-tracking engine: every pointermove event on the canvas
 * reads the browser's hardware movementX / movementY deltas and accumulates
 * them into one raw steering lane. The lane is drained exactly once per
 * frame by consumeSteer() and mapped strictly onto the look-vector
 * orientation angles — camera.rotation.y for X, camera.rotation.x for Y —
 * so the camera matrix rotates fluidly under all conditions.
 *
 * The engine is COMPLETELY INDEPENDENT of Pointer Lock. Hardware deltas are
 * delivered on ordinary pointermove events whether the browser granted a
 * lock or an iframe security context refused it, giving 100% viewing
 * freedom to orbit and look at the Milky Way either way.
 *
 * HARD RULES:
 *  - only movementX / movementY are ever read; absolute cursor coordinates
 *    are never consulted;
 *  - steering touches ONLY the orientation angles — never starfield, sky,
 *    shader or planet state — so the background stays perfectly rigid;
 *  - input never special-cases document focus: no re-lock, re-steer or
 *    exception paths exist.
 */

export interface MouseLookOptions {
  /** Radians of turn per pixel of movement. */
  sensitivity: number;
  /** Multiplier applied while pointer-locked. */
  lockedBoost: number;
  /** Invert vertical look. */
  invertY: boolean;
  /** How quickly the legacy rate lane decays (per second). */
  damping: number;
  /** Wheel notches -> throttle scale. */
  wheelStep: number;
}

export const DEFAULT_MOUSELOOK: MouseLookOptions = {
  sensitivity: 0.0022,
  lockedBoost: 1.35,
  invertY: false,
  damping: 12,
  wheelStep: 0.16
};

export class MouseLook {
  opts: MouseLookOptions = { ...DEFAULT_MOUSELOOK };

  /** The raw hardware-delta steering lane, drained by consumeSteer(). */
  private rawDx = 0;
  private rawDy = 0;
  /** Legacy rate lane (drag-look), kept for API compatibility. */
  private dx = 0;
  private dy = 0;
  /** Throttle multiplier driven by the wheel, 0.05x .. 20x. */
  private throttle = 1;
  /** Optical zoom, 1 = normal, higher = magnified. Shift+wheel. */
  private zoom = 1;
  private dragging = false;
  private locked = false;
  private enabled = true;
  private el: HTMLElement | null = null;
  private detachers: Array<() => void> = [];

  get isDragging(): boolean { return this.dragging; }
  get isLocked(): boolean { return this.locked; }
  get throttleScale(): number { return this.throttle; }
  /** Magnification factor; feed this into the camera FOV. */
  get zoomScale(): number { return this.zoom; }

  setZoom(v: number): void {
    this.zoom = Math.max(1, Math.min(60, Number.isFinite(v) ? v : 1));
  }
  resetZoom(): void { this.zoom = 1; }

  setEnabled(on: boolean): void {
    this.enabled = on;
    if (!on) { this.dx = 0; this.dy = 0; this.rawDx = 0; this.rawDy = 0; this.dragging = false; }
  }

  setThrottle(v: number): void {
    this.throttle = Math.max(0.05, Math.min(20, Number.isFinite(v) ? v : 1));
  }

  /** Feeds compositor/touch-pad deltas into the SAME raw steering lane. */
  injectLook(dx: number, dy: number): void {
    if (!this.enabled) return;
    const cap = (v: number) => Math.max(-400, Math.min(400, Number.isFinite(v) ? v : 0));
    const mx = cap(dx);
    const my = cap(dy);
    this.rawDx = cap(this.rawDx + mx);
    this.rawDy = cap(this.rawDy + my);
  }

  /**
   * Binds to a canvas. Safe to call again; the previous binding is removed.
   */
  attach(el: HTMLElement): void {
    this.detach();
    this.el = el;

    const isUI = (t: EventTarget | null): boolean => {
      // Never steal drags that belong to a panel, slider or button.
      const n = t as HTMLElement | null;
      return !!(n && typeof n.closest === 'function' &&
        n.closest('.wm-win, .topbar, .hud, .menu-root, .dock'));
    };

    const onDown = (e: PointerEvent) => {
      if (!this.enabled || e.button !== 0 || isUI(e.target)) return;
      this.dragging = true;
      try { el.setPointerCapture?.(e.pointerId); } catch { /* not fatal */ }
    };
    const onUp = (e: PointerEvent) => {
      this.dragging = false;
      // Drop any pending rate-lane motion so a release cannot keep turning.
      this.dx = 0; this.dy = 0;
      try { el.releasePointerCapture?.(e.pointerId); } catch { /* not fatal */ }
    };
    const onMove = (e: PointerEvent) => {
      if (!this.enabled) return;
      // THE ONE AND ONLY LOOK SOURCE: the browser's hardware movementX /
      // movementY deltas, accumulated unconditionally on every pointermove
      // — lock or no lock, drag or no drag. This is what makes the view
      // steer identically in a full tab and inside a security-restricted
      // iframe. Absolute cursor coordinates are never read.
      const CAP = 400;
      const cl = (v: number) => Math.max(-CAP, Math.min(CAP, Number.isFinite(v) ? v : 0));
      const mx = cl(e.movementX || 0);
      const my = cl(e.movementY || 0);
      this.rawDx = cl(this.rawDx + mx);
      this.rawDy = cl(this.rawDy + my);
      // Legacy rate lane: only while locked or dragging.
      if (this.locked || this.dragging) {
        this.dx = cl(this.dx + mx);
        this.dy = cl(this.dy + my);
      }
    };
    const onWheel = (e: WheelEvent) => {
      if (!this.enabled || isUI(e.target)) return;
      e.preventDefault();
      const dir = e.deltaY > 0 ? -1 : 1;
      // Shift+wheel is an optical zoom - the "spyglass" for picking out a
      // distant planet. Plain wheel is the throttle.
      if (e.shiftKey) {
        this.setZoom(this.zoom * Math.exp(dir * this.opts.wheelStep * 1.4));
      } else {
        // Exponential so one flick spans a big range, like a real throttle.
        this.setThrottle(this.throttle * Math.exp(dir * this.opts.wheelStep));
      }
    };
    const onLockChange = () => {
      const d = el.ownerDocument;
      this.locked = !!d && d.pointerLockElement === el;
      if (this.locked) {
        // Fresh lock = fresh deltas, so the first locked frame never gets
        // an unasked-for flick.
        this.dragging = false;
        this.dx = 0; this.dy = 0;
        this.rawDx = 0; this.rawDy = 0;
      }
    };
    const onLeave = () => { this.dragging = false; };

    el.addEventListener('pointerdown', onDown as EventListener);
    el.addEventListener('pointerup', onUp as EventListener);
    el.addEventListener('pointercancel', onUp as EventListener);
    el.addEventListener('pointerleave', onLeave);
    el.addEventListener('pointermove', onMove as EventListener);
    el.addEventListener('wheel', onWheel as EventListener, { passive: false });
    const doc = el.ownerDocument;
    doc?.addEventListener('pointerlockchange', onLockChange);

    this.detachers = [
      () => el.removeEventListener('pointerdown', onDown as EventListener),
      () => el.removeEventListener('pointerup', onUp as EventListener),
      () => el.removeEventListener('pointercancel', onUp as EventListener),
      () => el.removeEventListener('pointerleave', onLeave),
      () => el.removeEventListener('pointermove', onMove as EventListener),
      () => el.removeEventListener('wheel', onWheel as EventListener),
      () => doc?.removeEventListener('pointerlockchange', onLockChange)
    ];
  }

  detach(): void {
    this.detachers.forEach((d) => { try { d(); } catch { /* ignore */ } });
    this.detachers = [];
    this.el = null;
  }

  /** Requests hardware pointer lock for the captured-mouse experience when
   *  the environment allows it. Steering works regardless of the outcome. */
  requestLock(): void {
    if (!this.el || typeof this.el.requestPointerLock !== 'function') return;
    try {
      // Browsers reject (asynchronously) once transient user activation has
      // expired. Loading-screen completion is timer-driven, so defer to the
      // existing canvas-click handler instead of creating an unhandled
      // NotAllowedError that the global boot guard mistakes for a failure.
      const activation = (navigator as unknown as { userActivation?: { isActive?: boolean } })
        .userActivation;
      if (activation && !activation.isActive) return;
      const result = this.el.requestPointerLock() as unknown as Promise<void> | void;
      if (result && typeof (result as Promise<void>).catch === 'function') {
        void (result as Promise<void>).catch(() => { /* deltas still steer */ });
      }
    } catch { /* deltas still steer */ }
  }

  exitLock(): void {
    try { this.el?.ownerDocument?.exitPointerLock?.(); } catch { /* unsupported */ }
  }

  toggleLock(): void {
    if (this.locked) this.exitLock(); else this.requestLock();
  }

  /**
   * Legacy rate lane (drag-look). Returns -1..1 look axes and clears the
   * accumulator. Kept for API compatibility; the app steers through
   * consumeSteer().
   */
  consume(dt: number): { yaw: number; pitch: number } {
    if (!this.enabled) return { yaw: 0, pitch: 0 };

    const boost = this.locked ? this.opts.lockedBoost : 1;
    const s = this.opts.sensitivity * boost;

    const step = Math.max(dt, 1 / 240);
    let yaw = (this.dx * s) / step;
    let pitch = (this.dy * s) / step;
    if (this.opts.invertY) pitch = -pitch;

    const clamp = (v: number) => Math.max(-1, Math.min(1, v));
    const out = {
      yaw: clamp(Number.isFinite(yaw) ? yaw : 0),
      pitch: clamp(Number.isFinite(pitch) ? pitch : 0)
    };

    const keep = Math.max(0, 1 - this.opts.damping * dt);
    this.dx *= keep;
    this.dy *= keep;
    if (Math.abs(this.dx) < 0.01) this.dx = 0;
    if (Math.abs(this.dy) < 0.01) this.dy = 0;

    return out;
  }

  /**
   * Drains the raw hardware-delta lane into absolute look angles in radians.
   * Call once per frame. Each movementX / movementY pixel maps 1:1 onto the
   * look-vector orientation angles (camera.rotation.y for X, camera.rotation
   * .x for Y). Lock-independent, rate-free, and it never touches starfield,
   * shader or planet state — the deltas can only ever pivot the view.
   */
  consumeSteer(): { yaw: number; pitch: number } {
    if (!this.enabled) { this.rawDx = 0; this.rawDy = 0; return { yaw: 0, pitch: 0 }; }
    const s = this.opts.sensitivity * (this.locked ? this.opts.lockedBoost : 1);
    // A single absurd (or synthetic) delta is clamped so a violent flick
    // turns the view, it does not whip it.
    const cap = (v: number) => Math.max(-0.6, Math.min(0.6, Number.isFinite(v) ? v : 0));
    let yaw = cap(this.rawDx * s);
    let pitch = cap(this.rawDy * s);
    if (this.opts.invertY) pitch = -pitch;
    this.rawDx = 0;
    this.rawDy = 0;
    return { yaw, pitch };
  }

  stats(): Record<string, string> {
    return {
      'Throttle': this.throttle.toFixed(2) + '×',
      'Zoom': this.zoom > 1.01 ? this.zoom.toFixed(1) + '×' : '1×',
      'Mouse look': this.locked ? 'locked' : this.dragging ? 'dragging' : 'live'
    };
  }
}
