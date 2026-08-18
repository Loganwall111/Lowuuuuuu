/**
 * MouseLook — fly the universe with the mouse.
 *
 * Free-fly detaches Babylon's arc-rotate camera (its handlers would fight
 * the vehicle), which left the player with keyboard-only turning and no
 * zoom at all. That reads as being stuck.
 *
 * This supplies the missing half of the controls:
 *  - drag, or raw-delta hardware pointer-lock, to look around
 *  - the wheel to throttle up and down
 *
 * Two look lanes feed the same camera:
 *  - `consume()`  — normalised -1..1 look axes for drag mode (and the
 *                   compatibility API VehicleSystem understands);
 *  - `consumeSteer()` — ABSOLUTE raw-delta steering for pointer lock: each
 *                   movementX/movementY pixel maps 1:1 onto the look-at
 *                   steering matrices (camera.rotation.y / camera.rotation.x)
 *                   and can never touch planet positions or orbit updates.
 *
 * Pointer lock is requested on the rendering canvas container itself; the
 * browser's pointerlockerror path is handled so a refused lock degrades to
 * drag-look instead of freezing input.
 */

export interface MouseLookOptions {
  /** Radians of turn per pixel of movement. */
  sensitivity: number;
  /** Multiplier applied while pointer-locked. */
  lockedBoost: number;
  /** Invert vertical look. */
  invertY: boolean;
  /** How quickly look input decays once the mouse stops (per second). */
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

  /** Accumulated look this frame, consumed by `consume()`. */
  private dx = 0;
  private dy = 0;
  /**
   * Raw pointer-lock deltas, consumed by `consumeSteer()`.
   *
   * Kept separate from the rate accumulator so pointer-lock steering can map
   * one raw pixel delta onto one absolute camera-rotation angle. This is the
   * hardware path: movementX/movementY are copied in verbatim (clamped only
   * against synthetic/garbage events) and never reinterpreted as velocity,
   * so a locked mouse pivots the look matrices 1:1.
   */
  private rawDx = 0;
  private rawDy = 0;
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

  /** Feeds compositor touch-pad deltas into the same raw look accumulator. */
  injectLook(dx: number, dy: number): void {
    if (!this.enabled) return;
    const cap = (v: number) => Math.max(-400, Math.min(400, Number.isFinite(v) ? v : 0));
    this.dx = cap(this.dx + cap(dx));
    this.dy = cap(this.dy + cap(dy));
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
      // Drop any pending motion. Without this a violent flick keeps turning
      // the camera for a second after you let go, which feels broken.
      if (!this.locked) { this.dx = 0; this.dy = 0; }
      try { el.releasePointerCapture?.(e.pointerId); } catch { /* not fatal */ }
    };
    const onMove = (e: PointerEvent) => {
      if (!this.enabled) return;
      // Pointer lock reports movement continuously; dragging only while held.
      if (!this.locked && !this.dragging) return;
      // Cap the accumulator: a single absurd movementX (or a synthetic
      // event) must not be able to bank a huge turn.
      const CAP = 400;
      const cl = (v: number) => Math.max(-CAP, Math.min(CAP, Number.isFinite(v) ? v : 0));
      const mx = cl(e.movementX || 0);
      const my = cl(e.movementY || 0);
      this.dx = cl(this.dx + mx);
      this.dy = cl(this.dy + my);
      // The absolute raw-delta lane for pointer lock. movementX/Y are the
      // hardware deltas the browser reports while locked; they are stored
      // verbatim so consumeSteer() can map pixels onto look angles directly.
      this.rawDx = cl(this.rawDx + mx);
      this.rawDy = cl(this.rawDy + my);
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
        // Fresh lock = fresh deltas. Anything accumulated while the cursor
        // was free was drag input, and dumping it into the first locked
        // frame would add an unasked-for flick of camera rotation.
        this.dragging = false;
        this.dx = 0; this.dy = 0;
        this.rawDx = 0; this.rawDy = 0;
      }
    };
    const onLockError = () => {
      // The browser refused the lock request (transient activation expired,
      // permission denied, another element holds the lock). Fall back to
      // drag-look without breaking anything; the next click retries.
      this.locked = false;
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
    doc?.addEventListener('pointerlockerror', onLockError);

    this.detachers = [
      () => el.removeEventListener('pointerdown', onDown as EventListener),
      () => el.removeEventListener('pointerup', onUp as EventListener),
      () => el.removeEventListener('pointercancel', onUp as EventListener),
      () => el.removeEventListener('pointerleave', onLeave),
      () => el.removeEventListener('pointermove', onMove as EventListener),
      () => el.removeEventListener('wheel', onWheel as EventListener),
      () => doc?.removeEventListener('pointerlockchange', onLockChange),
      () => doc?.removeEventListener('pointerlockerror', onLockError)
    ];
  }

  detach(): void {
    this.detachers.forEach((d) => { try { d(); } catch { /* ignore */ } });
    this.detachers = [];
    this.el = null;
  }

  /** Requests pointer lock for continuous look without holding the button. */
  requestLock(): void {
    try {
      // Browsers reject (asynchronously) once transient user activation has
      // expired. Loading-screen completion is timer-driven, so defer to the
      // existing canvas-click handler instead of creating an unhandled
      // NotAllowedError that the global boot guard mistakes for a failure.
      const activation = (navigator as unknown as { userActivation?: { isActive?: boolean } })
        .userActivation;
      if (activation && !activation.isActive) return;
      const result = this.el?.requestPointerLock?.() as unknown as Promise<void> | void;
      if (result && typeof (result as Promise<void>).catch === 'function') {
        void (result as Promise<void>).catch(() => { /* next click retries */ });
      }
    } catch { /* unsupported */ }
  }

  exitLock(): void {
    try { this.el?.ownerDocument?.exitPointerLock?.(); } catch { /* unsupported */ }
  }

  toggleLock(): void {
    if (this.locked) this.exitLock(); else this.requestLock();
  }

  /**
   * Returns this frame's look axes as -1..1 and clears the accumulator.
   * Call once per frame.
   */
  consume(dt: number): { yaw: number; pitch: number } {
    if (!this.enabled) return { yaw: 0, pitch: 0 };

    const boost = this.locked ? this.opts.lockedBoost : 1;
    const s = this.opts.sensitivity * boost;

    // Convert pixels to a rate, then clamp so a violent flick cannot spin
    // the camera wildly.
    const step = Math.max(dt, 1 / 240);
    let yaw = (this.dx * s) / step;
    let pitch = (this.dy * s) / step;
    if (this.opts.invertY) pitch = -pitch;

    const clamp = (v: number) => Math.max(-1, Math.min(1, v));
    const out = {
      yaw: clamp(Number.isFinite(yaw) ? yaw : 0),
      pitch: clamp(Number.isFinite(pitch) ? pitch : 0)
    };

    // Decay rather than hard-zero, so motion feels weighty instead of snappy.
    const keep = Math.max(0, 1 - this.opts.damping * dt);
    this.dx *= keep;
    this.dy *= keep;
    if (Math.abs(this.dx) < 0.01) this.dx = 0;
    if (Math.abs(this.dy) < 0.01) this.dy = 0;

    return out;
  }

  /**
   * Returns this frame's RAW pointer-lock deltas as absolute look angles in
   * radians and clears the raw accumulator. Call once per frame while locked.
   *
   * This is the strict mouse->look path: each movementX/movementY pixel maps
   * onto exactly one increment of the look-at steering matrices
   * (camera.rotation.y for X, camera.rotation.x for Y) via the heading the
   * camera is rebuilt from. There is no rate integration and no coupling to
   * planet/Keplerian position updates — the deltas can only ever pivot the
   * view.
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
      'Mouse look': this.locked ? 'locked' : this.dragging ? 'dragging' : 'idle'
    };
  }
}
