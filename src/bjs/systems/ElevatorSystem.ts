/**
 * ElevatorSystem — the ride from a planet's surface to orbit.
 *
 * A space elevator is a tether anchored at the equator and held taut by a
 * counterweight beyond geostationary altitude. The interesting part is that
 * the physics is real and legible: below the geostationary radius gravity
 * wins and the cable hangs; above it centrifugal force wins and the cable
 * is flung outward. The station sits exactly where those cancel.
 *
 * That single number - the geostationary radius - decides everything about
 * the structure, so it is computed rather than authored.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';

export type CarState = 'docked-surface' | 'ascending' | 'docked-orbit' | 'descending';

export interface ElevatorSpec {
  id: string;
  /** Which planet it is anchored to. */
  planetId: string;
  /** Planet centre in world space. */
  center: Vector3;
  /** Planet surface radius. */
  surfaceRadius: number;
  /** Standard gravitational parameter, GM. Drives the geostationary radius. */
  mu: number;
  /** Rotation rate of the planet, rad/s. */
  omega: number;
  /** Where the base sits, as a unit vector from the centre. */
  anchor: Vector3;
  /** Radius at which orbital period matches the planet's day. */
  geoRadius: number;
  /** Where the counterweight sits; must be beyond geoRadius. */
  counterweightRadius: number;
  /** Total tether length from surface to counterweight. */
  length: number;
}

export interface Car {
  id: string;
  elevator: string;
  /** 0 at the surface, 1 at the orbital station. */
  t: number;
  state: CarState;
  /** Units per second along the tether. */
  speed: number;
  /** Seconds spent waiting at a terminal. */
  dwell: number;
}

/**
 * Net specific force on a mass riding the tether at radius r: gravity in,
 * centrifugal out. Negative means it is pulled down, positive means it is
 * flung up. Zero exactly at the geostationary radius.
 */
export function netAcceleration(mu: number, omega: number, r: number): number {
  if (!(r > 0) || !Number.isFinite(r)) return 0;
  return omega * omega * r - mu / (r * r);
}

/** Radius where a circular orbit takes exactly one planetary rotation. */
export function geostationaryRadius(mu: number, omega: number): number {
  if (!(mu > 0) || !(omega > 0)) return 0;
  return Math.cbrt(mu / (omega * omega));
}

export class ElevatorSystem {
  private elevators: ElevatorSpec[] = [];
  private cars: Car[] = [];
  private nextId = 0;

  get list(): readonly ElevatorSpec[] { return this.elevators; }
  get carList(): readonly Car[] { return this.cars; }

  /**
   * Builds an elevator on a planet. The geostationary radius comes out of
   * the planet's own gravity and spin, so a fast-spinning world gets a
   * short elevator and a slow one gets an enormous tether.
   */
  build(
    planetId: string, center: Vector3, surfaceRadius: number,
    mu: number, omega: number, anchor?: Vector3
  ): ElevatorSpec | null {
    const geo = geostationaryRadius(mu, omega);

    // If the geostationary radius is inside the planet, a tether cannot be
    // held up by rotation at all - the structure is impossible.
    if (!(geo > surfaceRadius)) return null;

    // The counterweight must sit beyond geo so the whole cable stays in
    // tension. A little past is enough.
    const counterweight = geo * 1.45;

    const spec: ElevatorSpec = {
      id: 'elev-' + this.nextId++,
      planetId,
      center: center.clone(),
      surfaceRadius,
      mu,
      omega,
      anchor: (anchor ?? new Vector3(0, 1, 0)).normalize(),
      geoRadius: geo,
      counterweightRadius: counterweight,
      length: counterweight - surfaceRadius
    };
    this.elevators.push(spec);
    return spec;
  }

  get(id: string): ElevatorSpec | null {
    return this.elevators.find((e) => e.id === id) ?? null;
  }

  /** Puts a car on a tether, waiting at the surface. */
  addCar(elevatorId: string, speed = 40): Car | null {
    if (!this.get(elevatorId)) return null;
    const car: Car = {
      id: 'car-' + this.nextId++,
      elevator: elevatorId,
      t: 0,
      state: 'docked-surface',
      speed,
      dwell: 0
    };
    this.cars.push(car);
    return car;
  }

  /** World position of a point along the tether, 0..1. */
  positionAt(spec: ElevatorSpec, t: number): Vector3 {
    const f = Math.max(0, Math.min(1, Number.isFinite(t) ? t : 0));
    const r = spec.surfaceRadius + f * spec.length;
    return spec.center.add(spec.anchor.scale(r));
  }

  /** Radius of a point along the tether. */
  radiusAt(spec: ElevatorSpec, t: number): number {
    const f = Math.max(0, Math.min(1, Number.isFinite(t) ? t : 0));
    return spec.surfaceRadius + f * spec.length;
  }

  /**
   * What a rider feels at fraction t, in m/s^2-ish units. Negative is
   * "pressed into the floor"; it passes through zero at the station and
   * reverses above it, which is the whole point of the ride.
   */
  feltGravity(spec: ElevatorSpec, t: number): number {
    return netAcceleration(spec.mu, spec.omega, this.radiusAt(spec, t));
  }

  /** Fraction along the tether where the geostationary station sits. */
  stationFraction(spec: ElevatorSpec): number {
    if (spec.length <= 0) return 1;
    return Math.max(0, Math.min(1,
      (spec.geoRadius - spec.surfaceRadius) / spec.length));
  }

  /** Runs the cars. They shuttle up and down, pausing at each end. */
  update(dt: number): void {
    if (!(dt > 0) || !Number.isFinite(dt)) return;

    for (const car of this.cars) {
      const spec = this.get(car.elevator);
      if (!spec || spec.length <= 0) continue;
      const per = car.speed / spec.length;   // fraction per second

      switch (car.state) {
        case 'docked-surface':
          car.dwell += dt;
          if (car.dwell > 3) { car.state = 'ascending'; car.dwell = 0; }
          break;
        case 'ascending':
          car.t = Math.min(1, car.t + per * dt);
          if (car.t >= 1) { car.state = 'docked-orbit'; car.dwell = 0; }
          break;
        case 'docked-orbit':
          car.dwell += dt;
          if (car.dwell > 4) { car.state = 'descending'; car.dwell = 0; }
          break;
        case 'descending':
          car.t = Math.max(0, car.t - per * dt);
          if (car.t <= 0) { car.state = 'docked-surface'; car.dwell = 0; }
          break;
      }
    }
  }

  /** Sends a car to a specific fraction, for "ride to orbit" style actions. */
  sendTo(carId: string, t: number): boolean {
    const car = this.cars.find((c) => c.id === carId);
    if (!car) return false;
    const target = Math.max(0, Math.min(1, t));
    car.state = target > car.t ? 'ascending' : 'descending';
    car.dwell = 0;
    return true;
  }

  remove(id: string): boolean {
    const i = this.elevators.findIndex((e) => e.id === id);
    if (i < 0) return false;
    this.elevators.splice(i, 1);
    this.cars = this.cars.filter((c) => c.elevator !== id);
    return true;
  }

  stats(): Record<string, string> {
    return {
      'Space elevators': String(this.elevators.length),
      'Elevator cars': String(this.cars.length)
    };
  }

  dispose(): void {
    this.elevators = [];
    this.cars = [];
  }
}
