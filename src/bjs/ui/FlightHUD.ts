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

import { GEAR_ORDER, GEARS, type GearId } from '../systems/SpeedGears';
import {
  DEFAULT_HUD_THEME, HUD_THEMES, hudTheme, signalBars, signalStrength,
  type HudThemeId
} from './HudTheme';

export interface HUDElements {
  coordinates: boolean;
  attitude: boolean;
  velocity: boolean;
  warp: boolean;
  target: boolean;
  fleet: boolean;
  reticle: boolean;
  /** The manual speed-gear shifter across the top of the screen. */
  gears: boolean;
}

export const DEFAULT_HUD_ELEMENTS: HUDElements = {
  coordinates: true,
  attitude: true,
  velocity: true,
  warp: true,
  target: true,
  fleet: false,
  reticle: true,
  gears: true
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
  /** Which speed gear is engaged. */
  gear?: GearId;
}

export const EMPTY_FLIGHT: FlightData = {
  x: 0, y: 0, z: 0, heading: 0, pitch: 0, speed: 0, throttle: 0,
  warpCharge: 0, warpMultiplier: 1, locale: 'Deep space',
  localeDistance: 0, fleetSize: 0, fleetGravity: 0, gear: 'cruise'
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

/**
 * The bits of a descent the HUD needs.
 *
 * Structural rather than an import of HoleInterior's types: the HUD should
 * not depend on the physics module, only on the shape of what it is handed.
 */
export interface DescentStateLike {
  phase: string;
  progress: number;
  remaining: number;
  exitWindow: number;
}

export interface DescentPlanLike {
  gargantua: boolean;
  nested: boolean;
}

export class FlightHUD {
  elements: HUDElements = { ...DEFAULT_HUD_ELEMENTS };
  private root: HTMLDivElement | null = null;
  private cache = new Map<string, string>();
  private visible = true;

  mount(parent: HTMLElement = document.body): void {
    if (this.root) return;
    const el = document.createElement('div');
    el.className = 'fhud ' + hudTheme(this.theme).className;
    const ticks = this.headingTicksHtml();
    const bars = this.pitchBarsHtml();
    el.innerHTML = `
      <!-- Cockpit canopy: a glass layer blended over the scene, so the
           instruments read as projected on the inside of a ship's window
           rather than as flat stickers on the screen. -->
      <div class="fhud-canopy" aria-hidden="true"></div>

      <!-- Flight director: pitch ladder, heading tape and horizon, the
           centre-of-screen instruments a pilot actually flies by. -->
      <div class="fhud-director" data-g="attitude" aria-hidden="true">
        <div class="fh-hdg-tape"><div class="fh-hdg-inner" id="fhHdgTape">${ticks}</div></div>
        <div class="fh-pitch"><div class="fh-pitch-inner" id="fhPitch">${bars}</div></div>
        <div class="fh-horizon"></div>
        <div class="fh-fd"></div>
      </div>

      <div class="fhud-reticle" data-g="reticle">
        <svg viewBox="0 0 120 120" width="120" height="120">
          <circle cx="60" cy="60" r="34" class="fh-ring"/>
          <circle cx="60" cy="60" r="2.5" class="fh-dot"/>
          <path d="M60 14 L60 26 M60 94 L60 106 M14 60 L26 60 M94 60 L106 60" class="fh-tick"/>
          <path d="M26 34 A 44 44 0 0 1 94 34" class="fh-arc"/>
        </svg>
      </div>

      <!-- Satellite frame furniture. Present in the DOM for both themes;
           the theme class is what makes it visible, so switching themes
           is a class change rather than a rebuild. -->
      <div class="fhud-frame" aria-hidden="true">
        <span class="fh-corner tl"></span><span class="fh-corner tr"></span>
        <span class="fh-corner bl"></span><span class="fh-corner br"></span>
        <span class="fh-scan"></span>
      </div>

      <div class="fhud-uplink">
        <span class="fh-up-dot"></span>
        <span class="fh-up-id" id="fhUpId">SAT·ORB-1</span>
        <span class="fh-up-sig" id="fhUpSig">▮▮▮▯▯</span>
        <span class="fh-up-mode" id="fhUpMode">TRACKING</span>
      </div>

      <div class="fhud-gears" data-g="gears">
        <div class="fh-gear-label">Velocity Gear</div>
        <div class="fh-gear-set" id="fhGears">${GEAR_ORDER.map((id, i) => {
          const g = GEARS[id];
          const mul = g.speedMul >= 1 ? g.speedMul + 'x' : g.speedMul.toFixed(2) + 'x';
          return '<button type="button" class="fh-gear" data-gear="' + id
            + '" title="' + g.blurb + '">'
            + '<span class="fh-gear-key">' + g.key + '</span>'
            + '<span class="fh-gear-name">' + g.label + '</span>'
            + '<span class="fh-gear-mul">' + mul + '</span>'
            + '</button>' + (i < GEAR_ORDER.length - 1
              ? '<span class="fh-gear-sep"></span>' : '');
        }).join('')}</div>
      </div>
      <div class="fhud-notice" id="fhNotice"></div>

      <div class="fhud-descent" id="fhDescent">
        <div class="fh-desc-phase" id="fhDescPhase">FALLING</div>
        <div class="fh-desc-sub" id="fhDescSub"></div>
        <div class="fh-desc-bar"><i id="fhDescBar" style="width:0%"></i></div>
        <div class="fh-desc-exit" id="fhDescExit"></div>
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
      </div>

      <!-- Bottom telemetry ticker: the ship's own status line. -->
      <div class="fhud-status">
        <span class="fh-st-seg" id="fhStSys">SYS&nbsp;NOMINAL</span>
        <span class="fh-st-seg" id="fhStLoc">DEEP SPACE</span>
        <span class="fh-st-seg fh-st-right" id="fhStSpd">0 u/s</span>
      </div>`;
    parent.appendChild(el);
    this.root = el;
    // The HUD is pointer-events:none as a whole so it never eats clicks
    // meant for the world; the gear buttons opt back in individually.
    el.querySelectorAll<HTMLElement>('.fh-gear').forEach((b) => {
      b.addEventListener('click', () => {
        const id = b.dataset.gear;
        if (id && this.onGear) this.onGear(id as GearId);
      });
    });
    this.applyElements();
  }

  /** The heading tape: a row of ticks ±180° either side of the nose. */
  private headingTicksHtml(): string {
    let out = '';
    for (let deg = -180; deg <= 180; deg += 10) {
      const abs = ((deg % 360) + 360) % 360;
      const labeled = abs % 30 === 0;
      const label = abs % 90 === 0
        ? (abs === 0 ? 'N' : abs === 90 ? 'E' : abs === 180 ? 'S' : 'W')
        : String(abs);
      out += '<span class="fh-hdg-tick' + (labeled ? ' lbl' : '') +
        '" style="left:calc(50% + ' + (deg * 3) + 'px)">' +
        (labeled ? '<i>' + label + '</i>' : '') + '</span>';
    }
    return out;
  }

  /** The pitch ladder: one bar every 10° of pitch. */
  private pitchBarsHtml(): string {
    let out = '';
    for (let deg = 80; deg >= -80; deg -= 10) {
      out += '<div class="fh-ladder-line' + (deg === 0 ? ' zero' : '') +
        '" style="top:calc(50% - ' + (deg * 4) + 'px)">' +
        '<span>' + Math.abs(deg) + '</span></div>';
    }
    return out;
  }

  /** Drives the pitch ladder and heading tape, only on a real change. */
  private setDirector(pitch: number, heading: number): void {
    // Pitch ladder: one px per degree of pitch, so the bar matching your
    // current pitch sits exactly on the horizon line.
    const pd = Math.round((pitch * 180) / Math.PI * 4) / 4;
    const pV = (pd * 4).toFixed(1);
    if (this.cache.get('pitch') !== pV) {
      this.cache.set('pitch', pV);
      const el = document.getElementById('fhPitch');
      if (el) el.style.transform = 'translateY(' + pV + 'px)';
    }
    // Heading tape: 3px per degree, translated so the nose stays centred.
    const hd = headingDegrees(heading);
    const hV = (-hd * 3).toFixed(1);
    if (this.cache.get('hdg') !== hV) {
      this.cache.set('hdg', hV);
      const el = document.getElementById('fhHdgTape');
      if (el) el.style.transform = 'translateX(' + hV + 'px)';
    }
  }

  /**
   * Called when the player clicks a gear button.
   *
   * The HUD does not own the gearbox - it only reports what it is told and
   * asks for changes - so that the keyboard path and the click path go
   * through exactly the same code in the app.
   */
  onGear: ((id: GearId) => void) | null = null;

  /**
   * Brief telemetry line under the gear row.
   *
   * Separate from the app-wide toast: this one is anchored to the flight
   * instruments, styled as terminal telemetry, and must not displace or
   * compete with a system message.
   */
  notify(msg: string): void {
    const n = this.root?.querySelector<HTMLElement>('#fhNotice');
    if (!n) return;
    n.textContent = msg;
    n.classList.add('on');
    if (this.noticeTimer !== null) clearTimeout(this.noticeTimer);
    this.noticeTimer = setTimeout(() => n.classList.remove('on'), 1900) as unknown as number;
  }

  private noticeTimer: number | null = null;

  /** Writes only when the value actually changed. */
  private put(id: string, text: string): void {
    if (this.cache.get(id) === text) return;
    this.cache.set(id, text);
    const el = document.getElementById(id);
    if (el) el.textContent = text;
  }

  /**
   * Same as put(), but for the few readouts that carry markup.
   *
   * Kept separate rather than switching put() to innerHTML: everything else
   * on the HUD is live telemetry that must never be parsed as HTML.
   */
  private putHtml(id: string, html: string): void {
    if (this.cache.get(id) === html) return;
    this.cache.set(id, html);
    const el = document.getElementById(id);
    if (el) el.innerHTML = html;
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
    // Writes happen even while the HUD is display-hidden, so that the moment
    // it is revealed it already shows live values instead of stale ones.
    if (!this.root) return;
    const e = this.elements;

    if (e.coordinates) {
      this.put('fhX', formatCoord(d.x));
      this.put('fhY', formatCoord(d.y));
      this.put('fhZ', formatCoord(d.z));
    }
    if (e.gears) this.showGear(d.gear ?? 'cruise');

    // ---- satellite uplink strip ----
    // Only touched in the satellite theme; legacy has no such readout and
    // writing to hidden nodes every frame is wasted DOM work.
    if (hudTheme(this.theme).uplink) {
      const sig = signalStrength(d.localeDistance);
      this.put('fhUpSig', signalBars(sig));
      this.put('fhUpMode',
        d.warpMultiplier > 1.5 ? 'REACQUIRING'
          : d.localeDistance > 1e5 ? 'DEEP FIELD' : 'TRACKING');
    }
    if (e.attitude) {
      this.put('fhHdg', String(headingDegrees(d.heading)).padStart(3, '0') + '°');
      this.put('fhCmp', compassPoint(d.heading));
      const p = Math.round((d.pitch * 180) / Math.PI);
      this.put('fhPit', (p >= 0 ? '+' : '') + p + '°');
      this.setDirector(d.pitch, d.heading);
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

    // ---- bottom status ticker ----
    this.put('fhStLoc', d.locale);
    this.put('fhStSpd', formatSpeed(d.speed));
    this.put('fhStSys', d.warpMultiplier > 1.5
      ? 'WARP ' + Math.round(d.warpMultiplier) + '×'
      : d.localeDistance > 1e5 ? 'CRUISE' : 'SYS NOMINAL');
  }

  /**
   * Drives the descent readout.
   *
   * Called with (null, null) whenever no fall is in progress, which hides
   * the panel — so the instrument exists only while it means something.
   */
  setDescent(plan: DescentPlanLike | null, state: DescentStateLike | null): void {
    if (!this.root) return;
    const panel = this.root.querySelector('#fhDescent') as HTMLElement | null;
    if (!panel) return;
    if (!plan || !state || state.phase === 'outside') {
      panel.classList.remove('on', 'singular');
      return;
    }
    panel.classList.add('on');
    panel.classList.toggle('singular',
      state.phase === 'singularity' || state.phase === 'darkness');

    const PHASE: Record<string, string> = {
      throat: 'PAST THE HORIZON',
      deep: 'FALLING',
      nested: 'SOMETHING BELOW YOU',
      singularity: 'SINGULARITY — FLY INTO IT',
      darkness: 'DARKNESS',
      arrived: 'ARRIVING'
    };
    this.put('fhDescPhase', PHASE[state.phase] ?? 'FALLING');

    const sub = state.phase === 'singularity'
      ? 'hold your line to pass through it'
      : state.phase === 'nested'
        ? 'a second lens is opening ahead'
        : formatDistance(state.remaining) + ' to the far side';
    this.put('fhDescSub', sub);

    const bar = this.root.querySelector('#fhDescBar') as HTMLElement | null;
    if (bar) bar.style.width = Math.round(state.progress * 100) + '%';

    this.putHtml('fhDescExit', state.exitWindow > 0.02
      ? 'the way back is still open — <b>' +
        Math.round(state.exitWindow * 100) + '%</b>'
      : 'the way back has closed');
  }

  /** Shows or hides one group. */
  /**
   * Switches instrument skin.
   *
   * The frame furniture is always in the DOM, so this is a class swap and
   * costs nothing. Rebuilding the HUD would drop the cache and cause a
   * full re-write of every readout on the next frame.
   */
  setTheme(id: HudThemeId): void {
    const spec = hudTheme(id);
    if (this.theme === spec.id) return;
    this.theme = spec.id;
    if (!this.root) return;
    for (const t of Object.values(HUD_THEMES)) {
      this.root.classList.toggle(t.className, t.id === spec.id);
    }
  }

  get currentTheme(): HudThemeId { return this.theme; }
  private theme: HudThemeId = DEFAULT_HUD_THEME;

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

  /** Marks the active gear button. Cheap: only touches it on a change. */
  private showGear(id: GearId): void {
    if (this.cache.get('gear') === id) return;
    this.cache.set('gear', id);
    this.root?.querySelectorAll<HTMLElement>('.fh-gear').forEach((b) => {
      b.classList.toggle('on', b.dataset.gear === id);
    });
  }

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
