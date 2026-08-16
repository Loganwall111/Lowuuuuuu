/**
 * HudTheme — which instrument skin the flight HUD wears.
 *
 * Two themes ship, and they are genuinely different readouts rather than
 * two colour schemes over the same boxes:
 *
 *   'legacy'    the original corner blocks. Kept because it is compact and
 *               some people just want coordinates and a speed number.
 *   'satellite' the default. The conceit is that you are not looking at the
 *               world directly - you are looking at a downlink from an
 *               observation satellite that is tracking your ship. So the
 *               HUD is framed as a telemetry feed: corner brackets, a
 *               scanning sweep, signal strength, an uplink identifier, and
 *               readouts labelled the way a ground station labels them.
 *
 * WHY A THEME REGISTRY RATHER THAN AN IF. The HUD already had one element
 * visibility system; bolting a second skin on with conditionals would have
 * meant every future readout needing to know about both layouts. A theme is
 * a small descriptor - a class name and a set of capability flags - so the
 * HUD asks the theme what to draw and the two layouts cannot drift into
 * each other.
 */

export type HudThemeId = 'suit' | 'satellite' | 'legacy';

export interface HudThemeSpec {
  id: HudThemeId;
  label: string;
  /** One-line description for the settings panel. */
  blurb: string;
  /** Root class applied to the HUD element. */
  className: string;
  /** Whether this theme draws the satellite frame furniture. */
  frame: boolean;
  /** Whether this theme draws the sweeping scan line. */
  sweep: boolean;
  /** Whether this theme shows the uplink/signal strip. */
  uplink: boolean;
}

export const HUD_THEMES: Record<HudThemeId, HudThemeSpec> = {
  suit: {
    id: 'suit',
    label: 'Exosuit',
    blurb: 'A powered-armor cockpit: helmet visor, reactor core and suit telemetry',
    className: 'fhud-suit',
    frame: true,
    sweep: true,
    uplink: false
  },
  satellite: {
    id: 'satellite',
    label: 'Satellite Uplink',
    blurb: 'Orbital telemetry downlink with tracking frame and scan sweep',
    className: 'fhud-satellite',
    frame: true,
    sweep: true,
    uplink: true
  },
  legacy: {
    id: 'legacy',
    label: 'Legacy',
    blurb: 'The original compact corner instruments',
    className: 'fhud-legacy',
    frame: false,
    sweep: false,
    uplink: false
  }
};

export const HUD_THEME_ORDER: HudThemeId[] = ['suit', 'satellite', 'legacy'];
export const DEFAULT_HUD_THEME: HudThemeId = 'suit';

export function isHudTheme(v: string): v is HudThemeId {
  return Object.prototype.hasOwnProperty.call(HUD_THEMES, v);
}

export function hudTheme(id: string): HudThemeSpec {
  return isHudTheme(id) ? HUD_THEMES[id] : HUD_THEMES[DEFAULT_HUD_THEME];
}

/**
 * Signal strength shown on the uplink strip, 0..1.
 *
 * Framed as "how good is the downlink from the tracking satellite", which
 * physically means: how far are you from the last thing it had a fix on.
 * This is flavour, but it is honest flavour - it is derived from real
 * distance rather than being a decorative number that wanders on a timer.
 */
export function signalStrength(distanceToNearest: number): number {
  const d = Number.isFinite(distanceToNearest) ? Math.max(0, distanceToNearest) : Infinity;
  if (!Number.isFinite(d)) return 0.05;
  // Full bars within a planetary system, tailing off across interstellar
  // distances, never quite reaching zero because the feed still resolves.
  const t = 1 - Math.min(1, Math.log10(1 + d / 200) / 4.2);
  return Math.max(0.05, Math.min(1, t));
}

/** Signal strength as a 5-bar readout string. */
export function signalBars(strength: number): string {
  const s = Number.isFinite(strength) ? Math.max(0, Math.min(1, strength)) : 0;
  const n = Math.max(1, Math.round(s * 5));
  return '▮'.repeat(n) + '▯'.repeat(5 - n);
}
