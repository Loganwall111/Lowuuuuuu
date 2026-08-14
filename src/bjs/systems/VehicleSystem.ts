/**
 * VehicleSystem — player-controlled flight and surface walking.
 *
 * One controller drives three modes:
 *   'orbit'  — the existing camera, untouched.
 *   'fly'    — 6-degree-of-freedom ship with thrust, inertia and roll.
 *   'walk'   — surface locomotion with gravity, ground clamping and jumping.
 *
 * Flight is inertial: thrust changes velocity, and velocity persists. That
 * means the ship obeys the same physics as everything else and can be pushed
 * by beams or pulled by gravity wells.
 */

import { Vector3, Quaternion, Matrix } from '@babylonjs/core/Maths/math.vector';

/**
 * 'freefly' is the Space Engine style camera: no ship, no inertia, direct
 * motion at a speed that scales with what you are near, so you can cross a
 * solar system and then inspect a rock without changing anything.
 */
export type ControlMode = 'orbit' | 'fly' | 'walk' | 'freefly';

export interface ShipSpec {
  id: string;
  name: string;
  glyph: string;
  thrust: number;
  maxSpeed: number;
  /** Fraction of velocity retained per second; <1 bleeds speed off. */
  damping: number;
  turnRate: number;
  rollRate: number;
  boost: number;
  note: string;
}

export const SHIPS: Record<string, ShipSpec> = {
  shuttle: {
    id: 'shuttle', name: 'Shuttle', glyph: '🚀', thrust: 42, maxSpeed: 95,
    damping: 0.55, turnRate: 1.7, rollRate: 2.4, boost: 3.0,
    note: 'Forgiving and stable. A good place to start.'
  },
  interceptor: {
    id: 'interceptor', name: 'Interceptor', glyph: '🛩', thrust: 88, maxSpeed: 210,
    damping: 0.22, turnRate: 2.9, rollRate: 4.2, boost: 4.5,
    note: 'Very fast, very little drag. Easy to overshoot.'
  },
  hauler: {
    id: 'hauler', name: 'Hauler', glyph: '🛳', thrust: 26, maxSpeed: 60,
    damping: 0.8, turnRate: 0.9, rollRate: 1.1, boost: 1.8,
    note: 'Heavy and slow to turn, but hard to destabilise.'
  },
  saucer: {
    id: 'saucer', name: 'Stolen Saucer', glyph: '🛸', thrust: 120, maxSpeed: 300,
    damping: 0.1, turnRate: 4.5, rollRate: 6.0, boost: 6.0,
    note: 'Alien inertial dampers. Absurd, and barely controllable.'
  }
};

export interface WalkSpec {
  walkSpeed: number;
  runSpeed: number;
  jumpSpeed: number;
  gravity: number;
  eyeHeight: number;
  /** Ground friction, applied per second. */
  friction: number;
}

export const DEFAULT_WALK: WalkSpec = {
  walkSpeed: 8, runSpeed: 18, jumpSpeed: 12,
  gravity: 18, eyeHeight: 1.7, friction: 9
};

/** Per-frame input, normalised so the caller decides the key bindings. */
export interface VehicleInput {
  forward: number;   // -1..1
  right: number;     // -1..1
  up: number;        // -1..1
  yaw: number;       // -1..1
  pitch: number;     // -1..1
  roll: number;      // -1..1
  boost: boolean;
  brake: boolean;
  jump: boolean;
  run: boolean;
}

export function emptyInput(): VehicleInput {
  return {
    forward: 0, right: 0, up: 0, yaw: 0, pitch: 0, roll: 0,
    boost: false, brake: false, jump: false, run: false
  };
}

/** Terrain query: returns ground height and normal at a world position. */
export type GroundProbe = (x: number, z: number) => { height: number; normal: Vector3 } | null;

export class VehicleController {
  mode: ControlMode = 'orbit';
  position = new Vector3(0, 0, -60);
  velocity = Vector3.Zero();
  orientation = Quaternion.Identity();

  ship: ShipSpec = SHIPS.shuttle;
  walk: WalkSpec = { ...DEFAULT_WALK };

  /** Walk mode state. */
  grounded = false;
  private yaw = 0;
  private pitch = 0;
  /** Deliberate roll, only changed while the roll keys are held. */
  private roll = 0;

  /**
   * How far the player can drop and still be considered on the ground.
   * Without this you go airborne on every downhill step.
   */
  stepDown = 1.2;

  /** Distance travelled, for the UI. */
  odometer = 0;

  /** Free-fly speed, in units per second. Scales enormously. */
  flySpeed = 60;
  /** Multiplier applied while boosting in free-fly. */
  flyBoost = 12;

  setMode(m: ControlMode): void {
    if (this.mode === m) return;
    this.mode = m;
    this.velocity.setAll(0);
    // Both walk and free-fly steer with explicit yaw/pitch angles, so the
    // current orientation is decomposed on entry. Without this the view
    // snaps when you change mode, because the angles and the quaternion
    // disagree about where you are facing.
    const e = this.orientation.toEulerAngles();
    if (m === 'walk') {
      this.yaw = e.y;
      this.pitch = 0;
      this.roll = 0;
      this.grounded = false;
    } else if (m === 'freefly') {
      this.yaw = e.y;
      this.pitch = Math.max(-1.5533, Math.min(1.5533, e.x));
      // Entering free-fly always levels the horizon. Arriving mid-barrel-roll
      // and being unable to tell which way is up is not a feature.
      this.roll = 0;
      this.orientation = Quaternion.RotationYawPitchRoll(this.yaw, this.pitch, 0);
    }
  }

  setShip(id: string): void {
    if (SHIPS[id]) this.ship = SHIPS[id];
  }

  /** Local axes derived from the current orientation. */
  axes(): { fwd: Vector3; right: Vector3; up: Vector3 } {
    const m = new Matrix();
    Matrix.FromQuaternionToRef(this.orientation, m);
    return {
      right: Vector3.TransformNormal(new Vector3(1, 0, 0), m),
      up: Vector3.TransformNormal(new Vector3(0, 1, 0), m),
      fwd: Vector3.TransformNormal(new Vector3(0, 0, 1), m)
    };
  }

  update(dt: number, input: VehicleInput, ground?: GroundProbe): void {
    if (dt <= 0 || !Number.isFinite(dt)) return;
    const before = this.position.clone();

    if (this.mode === 'fly') this.updateFly(dt, input);
    else if (this.mode === 'freefly') this.updateFreeFly(dt, input);
    else if (this.mode === 'walk') this.updateWalk(dt, input, ground);

    if (this.mode !== 'orbit') {
      this.odometer += Vector3.Distance(before, this.position);
    }
  }

  /* --------------------------------- flight --------------------------------- */

  private updateFly(dt: number, i: VehicleInput): void {
    const s = this.ship;

    // --- rotation: applied in local space so roll behaves correctly ---
    const rot = Quaternion.RotationYawPitchRoll(
      i.yaw * s.turnRate * dt,
      i.pitch * s.turnRate * dt,
      -i.roll * s.rollRate * dt);
    this.orientation = this.orientation.multiply(rot);
    this.orientation.normalize();

    const { fwd, right, up } = this.axes();

    // --- thrust ---
    const power = s.thrust * (i.boost ? s.boost : 1);
    const accel = Vector3.Zero();
    accel.addInPlace(fwd.scale(i.forward * power));
    accel.addInPlace(right.scale(i.right * power * 0.6));
    accel.addInPlace(up.scale(i.up * power * 0.6));
    this.velocity.addInPlace(accel.scale(dt));

    // --- braking and drag. Inertial: velocity persists without input. ---
    if (i.brake) {
      this.velocity.scaleInPlace(Math.max(0, 1 - 3.5 * dt));
    } else if (s.damping > 0) {
      this.velocity.scaleInPlace(Math.max(0, 1 - s.damping * dt));
    }

    const cap = s.maxSpeed * (i.boost ? s.boost : 1);
    const sp = this.velocity.length();
    if (sp > cap) this.velocity.scaleInPlace(cap / sp);

    this.position.addInPlace(this.velocity.scale(dt));
  }

  /* -------------------------------- free fly -------------------------------- */

  /**
   * Direct, weightless motion. Unlike ship flight there is no inertia: you
   * stop the moment you release the key, which is what makes it usable for
   * inspecting things at wildly different scales.
   */
  private updateFreeFly(dt: number, i: VehicleInput): void {
    // Free-fly used to compound a delta quaternion into the orientation every
    // frame. Yaw and pitch do not commute, so combining them repeatedly
    // injects roll that was never asked for: the horizon slowly tilts, and
    // once it has, "left" is no longer level and looking around feels like
    // wrestling the camera. It also drifts differently depending on frame
    // rate, which is why it felt worse on a slow machine.
    //
    // Tracking yaw and pitch as plain angles - exactly as walk mode already
    // did - keeps the horizon level no matter how you move the mouse. Pitch
    // is clamped just short of vertical so you can never flip over the pole
    // and end up inverted.
    this.yaw += i.yaw * 1.6 * dt;
    this.pitch = Math.max(-1.5533, Math.min(1.5533, this.pitch + i.pitch * 1.6 * dt));

    // Roll stays available for flying, but it is deliberate rather than
    // accumulated: hold Q/E and it rolls, release and it stays put.
    this.roll += -i.roll * 2.0 * dt;

    this.orientation = Quaternion.RotationYawPitchRoll(this.yaw, this.pitch, this.roll);

    const { fwd, right, up } = this.axes();
    const speed = this.flySpeed * (i.boost ? this.flyBoost : 1) * (i.brake ? 0.08 : 1);

    const move = Vector3.Zero();
    move.addInPlace(fwd.scale(i.forward));
    move.addInPlace(right.scale(i.right));
    move.addInPlace(up.scale(i.up));

    const len = move.length();
    if (len > 1e-6) {
      move.scaleInPlace(speed / len);
      this.position.addInPlace(move.scale(dt));
    }
    // velocity is reported for the HUD but is not integrated
    this.velocity.copyFrom(move);
  }

  /**
   * Sets a sensible cruising speed for the scale you are working at, so the
   * same controls work for inspecting a pebble and crossing a galaxy.
   */
  setScaleSpeed(distanceToNearest: number): void {
    const d = Number.isFinite(distanceToNearest) ? Math.abs(distanceToNearest) : 100;
    // Sub-linear scaling. A straight d*0.55 meant that merely being 1000
    // units from a planet gave you 560 u/s, so close manoeuvring felt like
    // being fired out of a cannon. sqrt keeps precision near things while
    // still letting speed climb into the thousands in deep space.
    const near = Math.max(0, d);
    this.flySpeed = Math.max(2, Math.min(60000, Math.sqrt(near) * 3.2 + near * 0.02 + 3));
  }

  /* ---------------------------------- walk ---------------------------------- */

  private updateWalk(dt: number, i: VehicleInput, ground?: GroundProbe): void {
    const w = this.walk;

    this.yaw += i.yaw * 2.2 * dt;
    this.pitch = Math.max(-1.45, Math.min(1.45, this.pitch + i.pitch * 2.2 * dt));
    this.orientation = Quaternion.RotationYawPitchRoll(this.yaw, this.pitch, 0);

    // movement is horizontal only: looking up must not launch you
    const cy = Math.cos(this.yaw), sy = Math.sin(this.yaw);
    const fwd = new Vector3(sy, 0, cy);
    const right = new Vector3(cy, 0, -sy);

    const speed = i.run ? w.runSpeed : w.walkSpeed;
    const wish = fwd.scale(i.forward).add(right.scale(i.right));
    const wl = wish.length();
    if (wl > 1e-5) wish.scaleInPlace(speed / wl);

    if (this.grounded) {
      // snap horizontal velocity toward the wish direction
      const f = Math.min(1, w.friction * dt);
      this.velocity.x += (wish.x - this.velocity.x) * f;
      this.velocity.z += (wish.z - this.velocity.z) * f;
      if (i.jump) {
        this.velocity.y = w.jumpSpeed;
        this.grounded = false;
      }
    } else {
      // reduced air control, so jumps commit
      this.velocity.x += (wish.x - this.velocity.x) * Math.min(1, 1.5 * dt);
      this.velocity.z += (wish.z - this.velocity.z) * Math.min(1, 1.5 * dt);
    }

    this.velocity.y -= w.gravity * dt;
    this.position.addInPlace(this.velocity.scale(dt));

    // ---- ground clamping ----
    const g = ground ? ground(this.position.x, this.position.z) : { height: 0, normal: new Vector3(0, 1, 0) };
    if (g) {
      const floor = g.height + w.eyeHeight;
      const wasGrounded = this.grounded;

      if (this.position.y <= floor) {
        this.position.y = floor;
        if (this.velocity.y < 0) this.velocity.y = 0;
        this.grounded = true;
      } else if (wasGrounded && this.velocity.y <= 0 &&
                 this.position.y - floor <= this.stepDown) {
        // Walking downhill must not launch you off every slope. If we were on
        // the ground and the ground is only slightly below, stick to it.
        this.position.y = floor;
        this.velocity.y = 0;
        this.grounded = true;
      } else {
        this.grounded = false;
      }
    }
  }

  /** Where the camera should look, given the current orientation. */
  /**
   * Attitude, for instruments. Read-only: yaw and pitch stay private
   * because letting anything outside write them is how camera state drifts
   * out of sync with the transform.
   */
  attitude(): { yaw: number; pitch: number; roll: number } {
    return { yaw: this.yaw, pitch: this.pitch, roll: this.roll };
  }

  lookTarget(): Vector3 {
    const { fwd } = this.axes();
    return this.position.add(fwd.scale(10));
  }

  speed(): number {
    return this.velocity.length();
  }

  /**
   * Turns the craft to look at a point.
   *
   * Needed because the camera is re-derived from this orientation every
   * frame: aiming the camera alone is undone on the very next frame. After
   * warping to a place, the player was left facing exactly backwards
   * (dot = -1) with the destination behind them - which also switched off
   * gravitational lensing, since it ignores holes behind the camera.
   */
  faceTowards(target: Vector3): void {
    const d = target.subtract(this.position);
    const len = d.length();
    if (!Number.isFinite(len) || len < 1e-6) return;
    d.scaleInPlace(1 / len);
    // Yaw about +Y, pitch from the vertical component. Matches the
    // RotationYawPitchRoll convention used everywhere else in this class,
    // so the camera basis and the instruments stay consistent.
    this.yaw = Math.atan2(d.x, d.z);
    this.pitch = Math.max(-1.5533, Math.min(1.5533, -Math.asin(d.y)));
    this.roll = 0;
    this.orientation = Quaternion.RotationYawPitchRoll(this.yaw, this.pitch, 0);
  }

  /** Puts the controller somewhere safe and stationary. */
  teleport(pos: Vector3): void {
    this.position.copyFrom(pos);
    this.velocity.setAll(0);
  }

  reset(): void {
    this.position.set(0, 0, -60);
    this.velocity.setAll(0);
    this.orientation = Quaternion.Identity();
    this.yaw = 0;
    this.pitch = 0;
    this.grounded = false;
    this.odometer = 0;
  }

  stats(): Record<string, string> {
    return {
      'Mode': this.mode,
      'Craft': this.ship.glyph + ' ' + this.ship.name,
      'Speed': this.speed().toFixed(1) + ' u/s',
      'Position': `${this.position.x.toFixed(0)}, ${this.position.y.toFixed(0)}, ${this.position.z.toFixed(0)}`,
      'Grounded': this.mode === 'walk' ? (this.grounded ? 'yes' : 'airborne') : '—',
      'Distance': this.odometer.toFixed(0) + ' u'
    };
  }
}

/** Maps a keyboard state set into a VehicleInput. */
export function inputFromKeys(keys: Set<string>): VehicleInput {
  const on = (k: string) => keys.has(k);
  const ax = (pos: string, neg: string) => (on(pos) ? 1 : 0) - (on(neg) ? 1 : 0);
  return {
    forward: ax('w', 's'),
    right: ax('d', 'a'),
    up: ax('r', 'f'),
    yaw: ax('arrowright', 'arrowleft'),
    pitch: ax('arrowdown', 'arrowup'),
    roll: ax('e', 'q'),
    boost: on('shift'),
    brake: on('x'),
    jump: on(' '),
    run: on('shift')
  };
}
