/**
 * SonarCursor — the pointer, as a spacecraft instrument.
 *
 * The default arrow is the single most office-software thing on the screen
 * and it undercuts everything else. This replaces it with a sonar/tracking
 * reticle: a fine crosshair, a rotating bearing ring, four tracking ticks,
 * and a ping that expands outward when you click.
 *
 * NO IMAGE FILES. The cursor is an inline SVG data URI built from a
 * template, per the project rule that nothing visual ships as a bitmap.
 * That also means it can be recoloured and resized at runtime without a
 * round trip to a file, which is what makes the state variants below cheap.
 *
 * WHY A DOM ELEMENT AND NOT PURELY `cursor:url(...)`. A CSS cursor cannot
 * animate. The sweep and the click ping need real animation, so the visible
 * cursor is a DOM element that follows the pointer, and the native cursor
 * is hidden underneath it. The data-URI form is still generated and used as
 * a fallback for the brief moment before the first pointermove, so the
 * pointer is never invisible.
 */

/** What the cursor is currently indicating. */
export type CursorState = 'idle' | 'target' | 'zoom' | 'grab';

export interface SonarCursorOptions {
  /** Overall size in CSS pixels. */
  size: number;
  /** Ring colour. */
  accent: string;
}

export const DEFAULT_SONAR: SonarCursorOptions = {
  size: 34,
  accent: '#00f0ff'
};

/**
 * Builds the static SVG used as the CSS fallback cursor.
 *
 * Kept deliberately simple - a ring, a crosshair and four ticks. The
 * animated parts live in the DOM element, because a data URI cannot move.
 */
export function sonarCursorSVG(o: SonarCursorOptions = DEFAULT_SONAR): string {
  const s = Math.max(8, Math.round(o.size));
  const c = s / 2;
  const r = c * 0.52;
  const tick = c * 0.30;
  // A finer targeting reticle: an open ring, a crosshair, and a diamond core
  // instead of a plain dot - the "lock-on" look without any image files.
  const dia = r * 0.42;
  return '<svg xmlns="http://www.w3.org/2000/svg" width="' + s + '" height="' + s
    + '" viewBox="0 0 ' + s + ' ' + s + '">'
    + '<circle cx="' + c + '" cy="' + c + '" r="' + r.toFixed(2)
    + '" fill="none" stroke="' + o.accent + '" stroke-width="1.1" opacity="0.8"/>'
    + '<circle cx="' + c + '" cy="' + c + '" r="' + (r * 0.36).toFixed(2)
    + '" fill="none" stroke="' + o.accent + '" stroke-width="0.7" opacity="0.45"/>'
    + '<path d="M' + c.toFixed(2) + ' ' + (c - dia).toFixed(2) + ' L'
    + (c + dia).toFixed(2) + ' ' + c.toFixed(2) + ' L' + c.toFixed(2) + ' ' + (c + dia).toFixed(2)
    + ' L' + (c - dia).toFixed(2) + ' ' + c.toFixed(2) + ' Z'
    + '" fill="none" stroke="' + o.accent + '" stroke-width="1" opacity="0.95"/>'
    + '<path d="M' + c + ' ' + (c - r - tick).toFixed(2) + 'V' + (c - r * 0.55).toFixed(2)
    + 'M' + c + ' ' + (c + r + tick).toFixed(2) + 'V' + (c + r * 0.55).toFixed(2)
    + 'M' + (c - r - tick).toFixed(2) + ' ' + c + 'H' + (c - r * 0.55).toFixed(2)
    + 'M' + (c + r + tick).toFixed(2) + ' ' + c + 'H' + (c + r * 0.55).toFixed(2)
    + '" stroke="' + o.accent + '" stroke-width="1.1" opacity="0.9"/>'
    + '</svg>';
}

/** The SVG as a CSS `cursor: url(...)` value, hotspot centred. */
export function sonarCursorCSS(o: SonarCursorOptions = DEFAULT_SONAR): string {
  const svg = sonarCursorSVG(o);
  const c = Math.round(Math.max(8, o.size) / 2);
  // encodeURIComponent rather than base64: smaller, and it survives being
  // read in a stylesheet without a decoding step.
  return 'url("data:image/svg+xml,' + encodeURIComponent(svg) + '") ' + c + ' ' + c + ', crosshair';
}

/** Markup for the live, animated cursor element. */
export const SONAR_CURSOR_HTML = `
  <span class="sc-ring"></span>
  <span class="sc-sweep"></span>
  <span class="sc-dot"></span>
  <span class="sc-tick n"></span><span class="sc-tick s"></span>
  <span class="sc-tick w"></span><span class="sc-tick e"></span>
  <span class="sc-ping"></span>
`;

export class SonarCursor {
  private el: HTMLElement | null = null;
  private on = false;
  private state: CursorState = 'idle';

  get enabled(): boolean { return this.on; }
  get current(): CursorState { return this.state; }

  mount(parent: HTMLElement = document.body): void {
    if (this.el) return;
    const el = document.createElement('div');
    el.className = 'sonar-cursor';
    el.innerHTML = SONAR_CURSOR_HTML;
    parent.appendChild(el);
    this.el = el;

    // The transform is written straight into the handler. Batching it into a
    // rAF left the cursor a whole frame behind the pointer, and on a heavy
    // WebGL loop that frame is 30-50ms - exactly the "laggy cursor" that was
    // reported. A transform on a `will-change: transform` element with
    // `pointer-events: none` is a compositor-only update: it never triggers
    // layout or paint, so writing it per event is cheap, not janky.
    window.addEventListener('pointermove', this.onMove, { passive: true });
    window.addEventListener('pointerdown', this.onDown, { passive: true });
    this.setEnabled(true);
  }

  private onMove = (e: PointerEvent): void => {
    if (!this.on || !this.el) return;
    this.el.style.transform =
      'translate3d(' + e.clientX + 'px,' + e.clientY + 'px,0) translate(-50%,-50%)';
  };

  /** Fires the expanding ping ring. */
  private onDown = (): void => {
    if (!this.el || !this.on) return;
    const ping = this.el.querySelector<HTMLElement>('.sc-ping');
    if (!ping) return;
    // Restarting a CSS animation requires the class to actually leave the
    // element between runs; a reflow read is the standard way to force it.
    ping.classList.remove('go');
    void ping.offsetWidth;
    ping.classList.add('go');
  };

  /** Switches the cursor's indicated state. */
  setState(s: CursorState): void {
    if (this.state === s) return;
    this.state = s;
    if (this.el) this.el.dataset.state = s;
  }

  setEnabled(v: boolean): void {
    this.on = v;
    if (this.el) this.el.style.display = v ? '' : 'none';
    // The native cursor is hidden only while ours is up, so turning this
    // off can never leave the player with no pointer at all.
    document.body?.classList.toggle('sonar-on', v);
  }

  dispose(): void {
    window.removeEventListener('pointermove', this.onMove);
    window.removeEventListener('pointerdown', this.onDown);
    this.el?.remove();
    this.el = null;
    document.body?.classList.remove('sonar-on');
  }
}
