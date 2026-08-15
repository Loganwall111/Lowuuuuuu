/**
 * SpeedGears — a manual gearbox for the flight model.
 *
 * WHY THIS EXISTS. Free-flight speed is derived from how far away the
 * nearest solid body is (VehicleController.setScaleSpeed), so that the same
 * controls work for inspecting a pebble and for crossing a galaxy. That
 * autoscaling is good, but on its own it left the player with NO manual
 * authority over their own velocity, and out in deep space it produced
 * numbers that make the galaxy impossible to look at:
 *
 *     distance to nearest body     resulting flySpeed
 *              1,000 u                  124 u/s
 *             50,000 u                1,719 u/s
 *            500,000 u               12,266 u/s
 *
 * On top of that, holding forward spools the warp drive to a 90,000x
 * multiplier. Combined, a single tap of the thrust key in intergalactic
 * space moves the camera further in one frame than the entire galaxy disc
 * is thick - the disc slab is 12,000 units, and one frame at 12,266 u/s
 * under even modest warp clears it completely. The result is that the
 * player is always either far away or already past, which reads exactly as
 * "the game only has warp speed".
 *
 * A gear does two independent things, and it needs both to fix that:
 *
 *   1. It scales the autoscaled base speed, so the player can choose to
 *      creep regardless of what the autoscaler wants.
 *   2. It sets a CEILING on the warp multiplier. This is the important
 *      half. Warp engages automatically from held thrust, so without a
 *      ceiling the drive re-enters warp the moment you push forward and no
 *      amount of base-speed scaling can hold you at a manoeuvring speed.
 *
 * Kept as pure data and pure functions with no engine imports, so the whole
 * gearbox is testable without a GPU.
 */

/** Identifier for a gear. */
export type GearId = 'impulse' | 'cruise' | 'hyper';

export interface Gear {
  id: GearId;
  /** Short name shown on the HUD. */
  label: string;
  /** The hotkey that selects it. */
  key: string;
  /** Multiplies the autoscaled base flight speed. */
  speedMul: number;
  /**
   * Largest warp multiplier this gear will allow.
   *
   * 1 means the warp drive is held fully disengaged: thrust still moves
   * you, but the drive cannot spool up behind your back. This is what
   * makes the low gears actually usable for arriving somewhere.
   */
  warpCeiling: number;
  /** One-line description, shown in the shift notification. */
  blurb: string;
}

/**
 * The gearbox.
 *
 * Three gears rather than a continuous slider: the point is to be able to
 * change velocity regime instantly and predictably with one keypress, which
 * a slider cannot do while you are flying.
 */
export const GEARS: Record<GearId, Gear> = {
  impulse: {
    id: 'impulse', label: 'IMPULSE', key: '1',
    speedMul: 0.05,
    // No warp at all. Orbiting and landing need the drive to stay off.
    warpCeiling: 1,
    blurb: 'orbital + landing'
  },
  cruise: {
    id: 'cruise', label: 'CRUISE', key: '2',
    speedMul: 1,
    // Sub-warp by definition: this is the gear for actually looking at
    // things at interplanetary range.
    warpCeiling: 1,
    blurb: 'interplanetary sub-warp'
  },
  hyper: {
    id: 'hyper', label: 'HYPER', key: '3',
    speedMul: 100,
    // The full drive, for crossing the 260,000-unit cell grid.
    warpCeiling: Infinity,
    blurb: 'intergalactic warp'
  }
};

/** Gear order, for the HUD and for cycling. */
export const GEAR_ORDER: GearId[] = ['impulse', 'cruise', 'hyper'];

/** The gear the player starts in. */
export const DEFAULT_GEAR: GearId = 'cruise';

/** Maps a pressed key to a gear, or null if the key is not a gear key. */
export function gearForKey(key: string): GearId | null {
  const k = String(key).toLowerCase();
  for (const id of GEAR_ORDER) if (GEARS[id].key === k) return id;
  return null;
}

/** True if this id names a real gear. */
export function isGearId(v: string): v is GearId {
  return Object.prototype.hasOwnProperty.call(GEARS, v);
}

/**
 * The gearbox state.
 *
 * Deliberately tiny: one field. It is a class rather than a bare variable
 * so the HUD, the flight loop and the tests all read the same source of
 * truth instead of each keeping their own copy.
 */
export class SpeedGearbox {
  private gear: GearId = DEFAULT_GEAR;
  /** Set when the gear changes, so the caller can notify once. */
  private dirty = false;

  get current(): GearId { return this.gear; }
  get spec(): Gear { return GEARS[this.gear]; }

  /**
   * Selects a gear. Returns true only if it actually changed, so callers
   * can fire a notification without having to remember the old value.
   */
  select(id: GearId): boolean {
    if (!isGearId(id) || id === this.gear) return false;
    this.gear = id;
    this.dirty = true;
    return true;
  }

  /** Handles a raw keypress. Returns true if a gear change happened. */
  handleKey(key: string): boolean {
    const id = gearForKey(key);
    return id ? this.select(id) : false;
  }

  /** Reads and clears the change flag. */
  consumeChange(): boolean {
    const d = this.dirty;
    this.dirty = false;
    return d;
  }

  /** Applies the gear to an autoscaled base speed. */
  applySpeed(baseSpeed: number): number {
    if (!Number.isFinite(baseSpeed)) return 0;
    // A floor, so IMPULSE never becomes a dead stick at close range: 0.05x
    // of a 37 u/s close-quarters base is under 2 u/s, which reads as broken
    // controls rather than as slow flight.
    return Math.max(0.5, baseSpeed * this.spec.speedMul);
  }

  /**
   * Clamps a warp multiplier to what this gear permits.
   *
   * This is the half that actually stops the "everything is warp speed"
   * problem, because warp is engaged by held thrust rather than by an
   * explicit control.
   */
  clampWarp(multiplier: number): number {
    if (!Number.isFinite(multiplier)) return 1;
    return Math.max(1, Math.min(multiplier, this.spec.warpCeiling));
  }

  /** Whether the warp drive is allowed to engage at all in this gear. */
  get warpAllowed(): boolean { return this.spec.warpCeiling > 1; }

  /** The notification text shown when the gear shifts. */
  message(): string {
    const s = this.spec;
    const mul = s.speedMul >= 1
      ? String(s.speedMul) + 'x'
      : s.speedMul.toFixed(2) + 'x';
    return 'GEAR ' + s.label + ' — ' + mul + ' — ' + s.blurb
      + (s.warpCeiling > 1 ? '' : ' — warp held off');
  }
}
