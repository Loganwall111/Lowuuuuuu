/**
 * AlienFleetSystem — giant alien ships that come and take a planet apart.
 *
 * A fleet is not scripted. Each ship is a small autonomous agent with a
 * state machine — approach, orbit, charge, fire, withdraw — and a weapon
 * that deposits energy into PlanetDestructionSystem. The interesting
 * behaviour (ships converging on a wound, a world coming apart under
 * sustained bombardment) falls out of that rather than being animated.
 *
 * Ship geometry is procedural and seeded, so a fleet looks like a fleet:
 * the same design language, different silhouettes.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import type { PlanetDestructionSystem } from './PlanetDestructionSystem';

export type ShipClass = 'harvester' | 'lance' | 'dreadnought' | 'swarmer';
export type ShipState = 'approach' | 'orbit' | 'charge' | 'fire' | 'withdraw';

export interface ShipDef {
  cls: ShipClass;
  label: string;
  /** Hull length in world units. These are meant to be enormous. */
  size: number;
  /** Energy per second delivered while firing. */
  power: number;
  /** Seconds spent charging before each shot. */
  chargeTime: number;
  /** Seconds a shot lasts. */
  burstTime: number;
  /** How far out it holds station, in planet radii. */
  standoff: number;
  speed: number;
  beamColor: [number, number, number];
}

export const SHIP_CLASSES: Record<ShipClass, ShipDef> = {
  harvester: {
    cls: 'harvester', label: 'Harvester', size: 140, power: 26000,
    chargeTime: 2.4, burstTime: 4.0, standoff: 3.2, speed: 90,
    beamColor: [0.4, 1.0, 0.55]
  },
  lance: {
    cls: 'lance', label: 'Lance', size: 90, power: 42000,
    chargeTime: 3.4, burstTime: 1.6, standoff: 5.0, speed: 150,
    beamColor: [1.0, 0.25, 0.2]
  },
  dreadnought: {
    cls: 'dreadnought', label: 'Dreadnought', size: 320, power: 90000,
    chargeTime: 5.5, burstTime: 6.0, standoff: 2.4, speed: 45,
    beamColor: [0.85, 0.35, 1.0]
  },
  swarmer: {
    cls: 'swarmer', label: 'Swarmer', size: 26, power: 5200,
    chargeTime: 0.8, burstTime: 1.2, standoff: 1.8, speed: 220,
    beamColor: [1.0, 0.75, 0.2]
  }
};

export interface AlienShip {
  id: string;
  def: ShipDef;
  pos: Vector3;
  vel: Vector3;
  /** Unit vector the ship is pointing. */
  facing: Vector3;
  state: ShipState;
  /** Seconds spent in the current state. */
  stateTime: number;
  /** Which planet it is attacking. */
  target: string | null;
  /** The exact point on the planet it is aiming at. */
  aim: Vector3;
  /** 0..1 weapon charge. */
  charge: number;
  alive: boolean;
  /** Set while the beam should be drawn. */
  firing: boolean;
}

export interface FleetEvent {
  kind: 'arrive' | 'openfire' | 'withdraw';
  ship: string;
  message: string;
}

function mulberry32(seed: number): () => number {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

export class AlienFleetSystem {
  private ships: AlienShip[] = [];
  private events: FleetEvent[] = [];
  private destruction: PlanetDestructionSystem | null = null;
  private nextId = 0;

  constructor(destruction?: PlanetDestructionSystem) {
    this.destruction = destruction ?? null;
  }

  get fleet(): readonly AlienShip[] { return this.ships; }
  get size(): number { return this.ships.filter((s) => s.alive).length; }

  /** Lets the app wire destruction in after construction. */
  retarget(d: PlanetDestructionSystem): void { this.destruction = d; }

  /**
   * Sends a fleet at a planet. Composition comes from the seed, so a given
   * seed always produces the same attack.
   */
  spawnFleet(
    seed: number, targetId: string, targetCenter: Vector3,
    targetRadius: number, count = 5
  ): AlienShip[] {
    const rng = mulberry32(seed);
    const kinds: ShipClass[] = ['harvester', 'lance', 'dreadnought', 'swarmer'];
    const out: AlienShip[] = [];

    for (let i = 0; i < count; i++) {
      // Dreadnoughts are rare; swarmers are common.
      const roll = rng();
      const cls: ShipClass =
        roll > 0.92 ? 'dreadnought'
          : roll > 0.66 ? 'lance'
            : roll > 0.34 ? 'harvester'
              : 'swarmer';
      const def = SHIP_CLASSES[cls];

      // Arrive from a random direction, well outside the planet.
      const a = rng() * Math.PI * 2;
      const b = (rng() - 0.5) * Math.PI * 0.7;
      const dir = new Vector3(
        Math.cos(b) * Math.cos(a), Math.sin(b), Math.cos(b) * Math.sin(a)
      ).normalize();
      const dist = targetRadius * (12 + rng() * 10);

      const ship: AlienShip = {
        id: `alien-${this.nextId++}`,
        def,
        pos: targetCenter.add(dir.scale(dist)),
        vel: new Vector3(0, 0, 0),
        facing: dir.scale(-1),
        state: 'approach',
        stateTime: 0,
        target: targetId,
        aim: targetCenter.clone(),
        charge: 0,
        alive: true,
        firing: false
      };
      this.ships.push(ship);
      out.push(ship);
      void kinds;
    }
    this.emit('arrive', out[0]?.id ?? 'fleet',
      `${count} alien ships are converging on ${targetId}`);
    return out;
  }

  /**
   * Runs the fleet. Ships close on their target, hold station, charge, and
   * fire into the planet — which is what actually breaks it open.
   */
  update(dt: number, planetOf: (id: string) => { center: Vector3; radius: number } | null): void {
    if (!(dt > 0) || !Number.isFinite(dt)) return;

    for (const s of this.ships) {
      if (!s.alive) continue;
      s.stateTime += dt;
      s.firing = false;

      const tgt = s.target ? planetOf(s.target) : null;
      if (!tgt || tgt.radius <= 0) {
        // The planet is gone. Nothing left to shoot.
        if (s.state !== 'withdraw') {
          s.state = 'withdraw';
          s.stateTime = 0;
          this.emit('withdraw', s.id, `${s.def.label} is leaving - nothing left`);
        }
      }

      switch (s.state) {
        case 'approach': {
          if (!tgt) break;
          const toward = tgt.center.subtract(s.pos);
          const d = toward.length();
          const hold = tgt.radius * s.def.standoff;
          s.facing = toward.normalize();
          if (d > hold) {
            s.vel = s.facing.scale(s.def.speed);
            s.pos.addInPlace(s.vel.scale(dt));
          } else {
            s.state = 'orbit';
            s.stateTime = 0;
          }
          break;
        }

        case 'orbit': {
          if (!tgt) break;
          this.holdStation(s, tgt, dt);
          // Pick a spot to bore into. Ships prefer the existing wound, so a
          // fleet concentrates fire instead of scattering it.
          if (s.stateTime > 0.8) {
            s.aim = this.chooseAim(s, tgt);
            s.state = 'charge';
            s.stateTime = 0;
            s.charge = 0;
          }
          break;
        }

        case 'charge': {
          if (!tgt) break;
          this.holdStation(s, tgt, dt);
          s.charge = Math.min(1, s.stateTime / s.def.chargeTime);
          if (s.charge >= 1) {
            s.state = 'fire';
            s.stateTime = 0;
            this.emit('openfire', s.id, `${s.def.label} opens fire`);
          }
          break;
        }

        case 'fire': {
          if (!tgt) break;
          this.holdStation(s, tgt, dt);
          s.firing = true;
          s.facing = s.aim.subtract(s.pos).normalize();
          // This is the whole point: the ship damages the actual planet.
          this.destruction?.damage(s.target!, s.aim, s.def.power * dt, 0.07);
          if (s.stateTime > s.def.burstTime) {
            s.state = 'orbit';
            s.stateTime = 0;
            s.charge = 0;
          }
          break;
        }

        case 'withdraw': {
          // Leave, then stop existing so the fleet does not grow forever.
          s.pos.addInPlace(s.facing.scale(-s.def.speed * 2 * dt));
          if (s.stateTime > 12) s.alive = false;
          break;
        }
      }
    }

    // Reap the departed.
    if (this.ships.some((s) => !s.alive)) {
      this.ships = this.ships.filter((s) => s.alive);
    }
  }

  /** Keeps a ship circling at its standoff distance. */
  private holdStation(
    s: AlienShip, tgt: { center: Vector3; radius: number }, dt: number
  ): void {
    const toward = tgt.center.subtract(s.pos);
    const d = Math.max(toward.length(), 1e-6);
    const hold = tgt.radius * s.def.standoff;
    const radial = toward.scale(1 / d);

    // Drift in or out toward the standoff distance.
    const err = d - hold;
    const inOut = radial.scale(Math.max(-1, Math.min(1, err / hold)) * s.def.speed * 0.4);

    // And slide sideways so it orbits rather than hovering.
    const up = Math.abs(radial.y) > 0.9 ? new Vector3(1, 0, 0) : new Vector3(0, 1, 0);
    const tangent = Vector3.Cross(radial, up).normalize();
    const lateral = tangent.scale(s.def.speed * 0.25);

    s.vel = inOut.add(lateral);
    s.pos.addInPlace(s.vel.scale(dt));
    s.facing = radial;
  }

  /**
   * Where to shoot. Ships converge on a wound that is already open, because
   * a fleet drilling one hole is far more destructive — and far better to
   * watch — than five ships scratching five spots.
   */
  private chooseAim(
    s: AlienShip, tgt: { center: Vector3; radius: number }
  ): Vector3 {
    const body = this.destruction?.get(s.target ?? '');
    const wound = body?.wounds?.[0];
    if (wound) {
      return tgt.center.add(wound.direction.scale(tgt.radius));
    }
    // No wound yet: aim at the point facing this ship.
    const dir = s.pos.subtract(tgt.center).normalize();
    return tgt.center.add(dir.scale(tgt.radius));
  }

  private emit(kind: FleetEvent['kind'], ship: string, message: string): void {
    this.events.push({ kind, ship, message });
    if (this.events.length > 40) this.events.shift();
  }

  recentEvents(n = 5): FleetEvent[] { return this.events.slice(-n); }

  /** Orders the whole fleet home. */
  recall(): void {
    for (const s of this.ships) {
      if (s.alive && s.state !== 'withdraw') {
        s.state = 'withdraw';
        s.stateTime = 0;
      }
    }
  }

  clear(): void {
    this.ships = [];
    this.events = [];
  }

  stats(): Record<string, string> {
    const firing = this.ships.filter((s) => s.firing).length;
    return {
      'Alien ships': String(this.size),
      'Ships firing': String(firing)
    };
  }

  dispose(): void { this.clear(); }
}
