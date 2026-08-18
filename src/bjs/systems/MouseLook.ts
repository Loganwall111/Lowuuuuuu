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
 *
 * EMBEDDED-PREVIEW FALLBACK: cross-origin iframes frequently deny pointer
 * lock (no `allow="pointer-lock"` permission policy), so the lock request
 * fails even though the app is fully interactive. When that happens this
 * class switches to FREE LOOK: bare mouse movement (no buttons) steers the
 * camera from relative clientX/clientY deltas, which works everywhere. The
 * switch is one-way per binding and announces itself via `onLockRefused`.
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
  /**
   * True once a pointer-lock request has been refused or the API is missing
   * in this environment. One-way latch: a later successful lock clears the
   * *active* fallback, but the latch itself stays set so a second refusal
   * cannot silently switch modes twice.
   */
  private lockBroken = false;
  /**
   * Free-look mode: bare mouse movement steers via relative client deltas.
   * Engaged only after pointer lock proves unavailable (refused / missing).
   */
  private fallbackActive = false;
  /** Last clientX/clientY in free-look mode, for computing relative deltas. */
  private lastClientX: number | null = null;
  private lastClientY: number | null = null;
  /** Fired once when free-look fallback engages, so the shell can announce it. */
  onLockRefused: (() => void) | null = null;
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
  /** True when free-look (pointer-lock refused) is steering the camera. */
  get fallbackSteer(): boolean { return this.fallbackActive; }
  get throttleScale(): number { return this.throttle; }
  /** Magnification factor; feed this into the camera FOV. */
  get zoomScale(): number { return this.zoom; }

  setZoom(v: number): void {
    this.zoom = Math.max(1, Math.min(60, Number.isFinite(v) ? v : 1));
  }
  resetZoom(): void { this.zoom = 1; }

  setEnabled(on: boolean): void {
    this.enabled = on;
    if (!on) {
      this.dx = 0; this.dy = 0; this.rawDx = 0; this.rawDy = 0;
      this.dragging = false; this.lastClientX = null; this.lastClientY = null;
    }
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
    this.lockBroken = false;
    this.fallbackActive = false;
    this.lastClientX = null;
    this.lastClientY = null;

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
      // Pointer lock reports movement continuously; dragging only while held;
      // free-look (pointer lock refused) steers on bare movement.
      if (!this.locked && !this.dragging && !this.fallbackActive) return;
      // Cap the accumulator: a single absurd movementX (or a synthetic
      // event) must not be able to bank a huge turn.
      const CAP = 400;
      const cl = (v: number) => Math.max(-CAP, Math.min(CAP, Number.isFinite(v) ? v : 0));
      let mx: number;
      let my: number;
      if (this.locked || this.dragging) {
        mx = cl(e.movementX || 0);
        my = cl(e.movementY || 0);
      } else {
        // FREE LOOK: pointer lock is unavailable in this environment, so the
        // hardware deltas never arrive. Steer from relative clientX/clientY
        // movement instead — the same absolute-pixel semantics, minus the
        // lock. Works in embedded previews that deny pointer lock.
        const cx = e.clientX, cy = e.clientY;
        if (this.lastClientX === null || this.lastClientY === null) {
          this.lastClientX = cx; this.lastClientY = cy;
          mx = 0; my = 0;
        } else {
          mx = cl(cx - this.lastClientX);
          my = cl(cy - this.lastClientY);
          this.lastClientX = cx; this.lastClientY = cy;
        }
      }
      this.dx = cl(this.dx + mx);
      this.dy = cl(this.dy + my);
      // The absolute raw-delta lane for steering. movementX/Y (or free-look
      // client deltas) are stored verbatim so consumeSteer() can map pixels
      // onto look angles directly.
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
        this.fallbackActive = false;
        this.lastClientX = null; this.lastClientY = null;
        this.dx = 0; this.dy = 0;
        this.rawDx = 0; this.rawDy = 0;
      }
    };
    const onLockError = () => {
      // The browser refused the lock request (transient activation expired,
      // permission denied, another element holds the lock, or a cross-origin
      // iframe lacks the pointer-lock permission policy). Fall back to free
      // look so the mouse still steers, instead of leaving input frozen.
      this.locked = false;
      this.engageFallback();
    };
    const onLeave = () => { this.dragging = false; };

    el.addEventListener('pointerdown', onDown as EventListener);
    el.addEventListener('pointerup', onUp as EventListener);
    el.addEventListener('pointercancel', onUp as EventListener);
    el.addEventListener('pointerleave', onLeave);
    el.addEventListener('pointermove', onMove as EventListener);
    el.addEventListener('wheel', onWheel as EventListener, { passive: false });
    const doc = el.ownerDocument;
    // pointerlockerror may be fired on the document (spec) or the element
    // (some engines); listen on both so a refusal is never missed.
    el.addEventListener('pointerlockerror', onLockError as EventListener);
    doc?.addEventListener('pointerlockchange', onLockChange);
    doc?.addEventListener('pointerlockerror', onLockError);

    this.detachers = [
      () => el.removeEventListener('pointerdown', onDown as EventListener),
      () => el.removeEventListener('pointerup', onUp as EventListener),
      () => el.removeEventListener('pointercancel', onUp as EventListener),
      () => el.removeEventListener('pointerleave', onLeave),
      () => el.removeEventListener('pointermove', onMove as EventListener),
      () => el.removeEventListener('wheel', onWheel as EventListener),
      () => el.removeEventListener('pointerlockerror', onLockError as EventListener),
      () => doc?.removeEventListener('pointerlockchange', onLockChange),
      () => doc?.removeEventListener('pointerlockerror', onLockError)
    ];
  }

  /**
   * Switches to free-look after pointer lock proves unusable. One-way latch:
   * only the first refusal announces itself, so a barrage of lock errors
   * cannot toast repeatedly.
   */
  private engageFallback(): void {
    if (this.lockBroken) return;
    this.lockBroken = true;
    this.fallbackActive = true;
    this.dragging = false;
    this.lastClientX = null;
    this.lastClientY = null;
    this.dx = 0; this.dy = 0;
    this.rawDx = 0; this.rawDy = 0;
    this.onLockRefused?.();
  }

  detach(): void {
    this.detachers.forEach((d) => { try { d(); } catch { /* ignore */ } });
    this.detachers = [];
    this.el = null;
  }

  /** Requests pointer lock for continuous look without holding the button. */
  requestLock(): void {
    if (!this.el) return;
    // No Pointer Lock API at all (some embedded webviews): skip straight to
    // free look so the mouse still steers.
    if (typeof this.el.requestPointerLock !== 'function') {
      this.engageFallback();
      return;
    }
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
        void (result as Promise<void>).catch(() => {
          // Refused (typically a cross-origin iframe without the
          // pointer-lock permission policy). Free look keeps the mouse
          // steering instead of leaving the view frozen.
          this.engageFallback();
        });
      }
    } catch {
      // Synchronous refusal (SecurityError etc.). Same free-look fallback.
      this.engageFallback();
    }
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
   * Returns this frame's RAW steering deltas as absolute look angles in
   * radians and clears the raw accumulator. Call once per frame while locked
   * or in free-look.
   *
   * This is the strict mouse->look path: each movementX/movementY pixel (or
   * free-look client delta) maps onto exactly one increment of the look-at
   * steering matrices (camera.rotation.y for X, camera.rotation.x for Y) via
   * the heading the camera is rebuilt from. There is no rate integration and
   * no coupling to planet/Keplerian position updates — the deltas can only
   * ever pivot the view.
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
      'Mouse look': this.locked ? 'locked'
        : this.fallbackActive ? 'free look'
          : this.dragging ? 'dragging' : 'idle'
    };
  }
}
