/**
 * FlightHUD — the instrument panel you fly by.
 *
 * The old readout was three chips in a corner. This is a proper flight HUD:
 * navigation coordinates, heading and pitch, a warp charge bar, throttle,
 * and a target readout, laid out the way a cockpit is laid out rather than
 * the way a web page is.
 *
 * Every element can be switched off individually. Someone who wants a clean
 * screenshot should be able to get one without losing the ability to fly,
 * so the groups are independent rather than one all-or-nothing toggle.
 *
 * Nothing here polls; the app pushes values in each frame and the HUD only
 * touches the DOM when a value has actually changed. Writing to textContent
 * sixty times a second for numbers that did not move is the usual reason
 * HUDs cost frames.
 */

export interface HUDElements {
  coordinates: boolean;
  attitude: boolean;
  velocity: boolean;
  warp: boolean;
  target: boolean;
  fleet: boolean;
  reticle: boolean;
}

export const DEFAULT_HUD_ELEMENTS: HUDElements = {
  coordinates: true,
  attitude: true,
  velocity: true,
  warp: true,
  target: true,
  fleet: false,
  reticle: true
};

export interface FlightData {
  x: number; y: number; z: number;
  /** Heading in radians, 0 = +Z. */
  heading: number;
  /** Pitch in radians. */
  pitch: number;
  /** Speed in world units/sec. */
  speed: number;
  /** Throttle 0-1. */
  throttle: number;
  /** Warp charge 0-1. */
  warpCharge: number;
  /** Warp speed multiplier, 1 when not warping. */
  warpMultiplier: number;
  /** Name of the nearest place. */
  locale: string;
  /** Distance to it, world units. */
  localeDistance: number;
  /** Ships in your fleet. */
  fleetSize: number;
  /** Fleet gravity, m/s^2. */
  fleetGravity: number;
}

export const EMPTY_FLIGHT: FlightData = {
  x: 0, y: 0, z: 0, heading: 0, pitch: 0, speed: 0, throttle: 0,
  warpCharge: 0, warpMultiplier: 1, locale: 'Deep space',
  localeDistance: 0, fleetSize: 0, fleetGravity: 0
};

/** Compass point for a heading in radians. */
export function compassPoint(heading: number): string {
  const P = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW'];
  const deg = ((heading * 180) / Math.PI % 360 + 360) % 360;
  return P[Math.round(deg / 45) % 8];
}

/** Heading in degrees, always 0-359. */
export function headingDegrees(heading: number): number {
  return Math.round(((heading * 180) / Math.PI % 360 + 360) % 360) % 360;
}

/**
 * Coordinates, formatted the way a navigation computer would.
 *
 * Fixed width with an explicit sign so the numbers do not jitter sideways
 * as they change - the single most distracting thing a HUD can do.
 */
export function formatCoord(v: number): string {
  const n = Number.isFinite(v) ? v : 0;
  const a = Math.abs(n);
  let s: string;
  if (a >= 1e6) s = (a / 1e6).toFixed(2) + 'M';
  else if (a >= 1e3) s = (a / 1e3).toFixed(2) + 'k';
  else s = a.toFixed(1);
  return (n < 0 ? '-' : '+') + s;
}

export class FlightHUD {
  elements: HUDElements = { ...DEFAULT_HUD_ELEMENTS };
  private root: HTMLDivElement | null = null;
  private cache = new Map<string, string>();
  private visible = true;

  mount(parent: HTMLElement = document.body): void {
    if (this.root) return;
    const el = document.createElement('div');
    el.className = 'fhud';
    el.innerHTML = `
      <div class="fhud-reticle" data-g="reticle">
        <svg viewBox="0 0 120 120" width="120" height="120">
          <circle cx="60" cy="60" r="34" class="fh-ring"/>
          <circle cx="60" cy="60" r="2.5" class="fh-dot"/>
          <path d="M60 14 L60 26 M60 94 L60 106 M14 60 L26 60 M94 60 L106 60" class="fh-tick"/>
          <path d="M26 34 A 44 44 0 0 1 94 34" class="fh-arc"/>
        </svg>
      </div>

      <div class="fhud-left">
        <div class="fh-block" data-g="coordinates">
          <div class="fh-label">Navigation</div>
          <div class="fh-coords">
            <span class="fh-ax">X</span><span class="fh-num" id="fhX">+0.0</span>
            <span class="fh-ax">Y</span><span class="fh-num" id="fhY">+0.0</span>
            <span class="fh-ax">Z</span><span class="fh-num" id="fhZ">+0.0</span>
          </div>
        </div>
        <div class="fh-block" data-g="attitude">
          <div class="fh-label">Attitude</div>
          <div class="fh-row">
            <span class="fh-big" id="fhHdg">000°</span>
            <span class="fh-cmp" id="fhCmp">N</span>
            <span class="fh-sub">PITCH <b id="fhPit">+0°</b></span>
          </div>
        </div>
      </div>

      <div class="fhud-right">
        <div class="fh-block" data-g="velocity">
          <div class="fh-label">Velocity</div>
          <div class="fh-big fh-accent" id="fhSpd">0 u/s</div>
          <div class="fh-bar"><i id="fhThr" style="width:0%"></i></div>
        </div>
        <div class="fh-block fh-warp" data-g="warp">
          <div class="fh-label">Warp Drive <b id="fhWMul"></b></div>
          <div class="fh-bar fh-bar-warp"><i id="fhWrp" style="width:0%"></i></div>
        </div>
        <div class="fh-block" data-g="target">
          <div class="fh-label">Nearest</div>
          <div class="fh-tgt" id="fhLoc">Deep space</div>
          <div class="fh-sub" id="fhLocD">—</div>
        </div>
        <div class="fh-block" data-g="fleet">
          <div class="fh-label">Fleet</div>
          <div class="fh-row"><span class="fh-num" id="fhFleet">0</span>
            <span class="fh-sub" id="fhFleetG">—</span></div>
        </div>
      </div>`;
    parent.appendChild(el);
    this.root = el;
    this.applyElements();
  }

  /** Writes only when the value actually changed. */
  private put(id: string, text: string): void {
    if (this.cache.get(id) === text) return;
    this.cache.set(id, text);
    const el = document.getElementById(id);
    if (el) el.textContent = text;
  }

  private width(id: string, pct: number): void {
    const key = id + ':w';
    const v = Math.max(0, Math.min(100, pct)).toFixed(1);
    if (this.cache.get(key) === v) return;
    this.cache.set(key, v);
    const el = document.getElementById(id);
    if (el) (el as HTMLElement).style.width = v + '%';
  }

  update(d: FlightData): void {
    if (!this.root || !this.visible) return;
    const e = this.elements;

    if (e.coordinates) {
      this.put('fhX', formatCoord(d.x));
      this.put('fhY', formatCoord(d.y));
      this.put('fhZ', formatCoord(d.z));
    }
    if (e.attitude) {
      this.put('fhHdg', String(headingDegrees(d.heading)).padStart(3, '0') + '°');
      this.put('fhCmp', compassPoint(d.heading));
      const p = Math.round((d.pitch * 180) / Math.PI);
      this.put('fhPit', (p >= 0 ? '+' : '') + p + '°');
    }
    if (e.velocity) {
      this.put('fhSpd', formatSpeed(d.speed));
      this.width('fhThr', d.throttle * 100);
    }
    if (e.warp) {
      this.width('fhWrp', d.warpCharge * 100);
      this.put('fhWMul', d.warpMultiplier > 1.01
        ? '×' + Math.round(d.warpMultiplier) : '');
      this.root.classList.toggle('warping', d.warpMultiplier > 1.01);
    }
    if (e.target) {
      this.put('fhLoc', d.locale);
      this.put('fhLocD', formatDistance(d.localeDistance));
    }
    if (e.fleet) {
      this.put('fhFleet', String(d.fleetSize));
      this.put('fhFleetG', d.fleetGravity > 0.01
        ? d.fleetGravity.toFixed(2) + ' m/s²' : 'no gravity');
    }
  }

  /** Shows or hides one group. */
  setElement(name: keyof HUDElements, on: boolean): void {
    this.elements[name] = on;
    this.applyElements();
  }

  /** Hides or shows the whole HUD. */
  setVisible(on: boolean): void {
    this.visible = on;
    if (this.root) this.root.style.display = on ? '' : 'none';
  }

  isVisible(): boolean { return this.visible; }

  private applyElements(): void {
    if (!this.root) return;
    for (const [k, on] of Object.entries(this.elements)) {
      this.root.querySelectorAll<HTMLElement>('[data-g="' + k + '"]')
        .forEach((n) => { n.style.display = on ? '' : 'none'; });
    }
  }

  dispose(): void {
    this.root?.remove();
    this.root = null;
    this.cache.clear();
  }
}

/** Speed in world units/sec -> readable string, up to fractions of c. */
export function formatSpeed(v: number): string {
  const n = Number.isFinite(v) ? Math.abs(v) : 0;
  const C = 300;
  if (n >= C * 1000) return (n / C).toExponential(1) + 'c';
  if (n >= C) return (n / C).toFixed(n / C >= 10 ? 0 : 2) + 'c';
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k u/s';
  if (n >= 10) return n.toFixed(0) + ' u/s';
  return n.toFixed(1) + ' u/s';
}

/** Distance in world units -> readable string. */
export function formatDistance(d: number): string {
  const n = Number.isFinite(d) ? Math.abs(d) : 0;
  if (n >= 63241) return (n / 63241).toFixed(2) + ' ly';
  if (n >= 1000) return (n / 1000).toFixed(2) + ' AU';
  if (n >= 10) return n.toFixed(0) + ' u';
  return n.toFixed(1) + ' u';
}
