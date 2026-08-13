/**
 * PlanetDestructionSystem — breaking a whole world, and watching the physics.
 *
 * A planet here is not a health bar. It is three concentric shells — crust,
 * mantle, core — each with its own mass, strength and temperature. Damage is
 * deposited at a point on the surface and bores inward. Once a wound reaches
 * the mantle the planet starts bleeding magma; once it reaches the core, the
 * core depressurises and erupts, and the crust above it is thrown off as
 * debris on real ballistic trajectories.
 *
 * Everything is conserved: the mass that leaves the planet is exactly the
 * mass that appears as ejecta, and the kinetic energy of that ejecta comes
 * out of the energy that was deposited. That makes the whole thing testable
 * without a renderer, which matters because the interesting failure mode
 * (a planet losing or gaining mass as you shoot it) is invisible on screen.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';

export type ShellId = 'crust' | 'mantle' | 'core';

export interface Shell {
  id: ShellId;
  /** Outer radius as a fraction of planet radius, 0..1. */
  outer: number;
  /** Inner radius as a fraction of planet radius, 0..1. */
  inner: number;
  /** Energy needed to remove one unit of volume. Higher = tougher. */
  strength: number;
  /** Kelvin-ish. The core is what makes the eruption spectacular. */
  temperature: number;
  /** Fraction of this shell that has been destroyed, 0..1. */
  damage: number;
  density: number;
}

/** A hole bored into the planet by sustained fire. */
export interface Wound {
  /** Unit vector from the planet centre to the entry point. */
  direction: Vector3;
  /** How deep, as a fraction of the planet radius. */
  depth: number;
  /** Angular half-width of the hole, radians. */
  width: number;
  /** Total energy poured in here. */
  energy: number;
  /** Deepest shell this wound has opened. */
  reached: ShellId;
}

export interface Ejecta {
  pos: Vector3;
  vel: Vector3;
  mass: number;
  radius: number;
  /** Glowing mantle/core material cools over time. */
  temperature: number;
  /** Where it came from, which decides its colour. */
  origin: ShellId;
  age: number;
}

export type PlanetPhase =
  | 'intact'        // scratched at worst
  | 'wounded'       // crust breached
  | 'bleeding'      // mantle exposed, magma escaping
  | 'erupting'      // core breached, depressurising
  | 'fracturing'    // structural integrity failing
  | 'destroyed';    // gone

export interface PlanetBody {
  id: string;
  center: Vector3;
  /** Current radius. Shrinks as mass is lost. */
  radius: number;
  /** Radius it started at, for damage reporting. */
  originalRadius: number;
  mass: number;
  originalMass: number;
  shells: Shell[];
  wounds: Wound[];
  phase: PlanetPhase;
  /** 0..1 across the whole body. At 1 the planet comes apart. */
  integrity: number;
  /** Spin, which rises as the body is knocked about asymmetrically. */
  spin: number;
}

export interface DestructionEvent {
  kind: 'breach' | 'mantle' | 'eruption' | 'fracture' | 'destroyed';
  planet: string;
  /** Where it happened, in world space. */
  at: Vector3;
  energy: number;
  message: string;
}

const SHELL_TEMPLATE: Omit<Shell, 'damage'>[] = [
  { id: 'crust',  outer: 1.00, inner: 0.86, strength: 1.0,  temperature: 290,  density: 2.9 },
  { id: 'mantle', outer: 0.86, inner: 0.30, strength: 2.4,  temperature: 2100, density: 4.5 },
  { id: 'core',   outer: 0.30, inner: 0.00, strength: 6.0,  temperature: 5800, density: 11.0 }
];

/** Volume of a spherical shell between two fractional radii, for radius R. */
function shellVolume(R: number, inner: number, outer: number): number {
  const k = (4 / 3) * Math.PI * R * R * R;
  return k * (outer * outer * outer - inner * inner * inner);
}

export class PlanetDestructionSystem {
  private planets: PlanetBody[] = [];
  private ejecta: Ejecta[] = [];
  private events: DestructionEvent[] = [];
  /** Ejecta beyond this many planet radii stop being tracked. */
  private cullRadius = 60;
  private maxEjecta = 900;
  private totalEnergy = 0;

  get bodies(): readonly PlanetBody[] { return this.planets; }
  get debris(): readonly Ejecta[] { return this.ejecta; }
  get energyDelivered(): number { return this.totalEnergy; }

  /** Registers a planet as destructible. */
  add(id: string, center: Vector3, radius: number, density = 1): PlanetBody {
    const shells: Shell[] = SHELL_TEMPLATE.map((s) => ({ ...s, damage: 0 }));
    let mass = 0;
    for (const s of shells) {
      mass += shellVolume(radius, s.inner, s.outer) * s.density * density;
    }
    const body: PlanetBody = {
      id, center: center.clone(), radius, originalRadius: radius,
      mass, originalMass: mass, shells, wounds: [],
      phase: 'intact', integrity: 1, spin: 0
    };
    this.planets.push(body);
    return body;
  }

  get(id: string): PlanetBody | null {
    return this.planets.find((p) => p.id === id) ?? null;
  }

  /**
   * Deposits energy at a point on (or near) a planet. This is what a laser,
   * a ship's cannon or an impact all call — one path, so every weapon
   * damages a world the same believable way.
   *
   * @param at      world-space point the damage is aimed at
   * @param energy  joules-ish; bigger bores deeper
   * @param spread  angular half-width of the wound, radians
   */
  damage(id: string, at: Vector3, energy: number, spread = 0.08): DestructionEvent[] {
    const p = this.get(id);
    const out: DestructionEvent[] = [];
    if (!p || p.phase === 'destroyed' || !(energy > 0)) return out;

    this.totalEnergy += energy;

    // Direction from the centre toward the impact.
    const toward = at.subtract(p.center);
    const len = toward.length();
    const dir = len > 1e-6 ? toward.scale(1 / len) : new Vector3(0, 1, 0);

    // Reuse a nearby wound rather than making a new one for every frame of
    // a sustained beam, or a held trigger would create thousands.
    let w = p.wounds.find((k) => Vector3.Dot(k.direction, dir) > Math.cos(spread * 2.2));
    if (!w) {
      w = { direction: dir, depth: 0, width: spread, energy: 0,
            reached: 'crust' };
      p.wounds.push(w);
    }
    w.energy += energy;
    w.width = Math.max(w.width, spread);

    // ---- bore inward ----
    // Each shell resists in proportion to its strength and the volume the
    // beam has to remove, so a wide beam bores more slowly than a tight one.
    let remaining = energy;
    const coneArea = Math.max(w.width * w.width, 1e-4);

    for (const shell of p.shells) {
      if (remaining <= 0) break;
      // Skip shells the wound is already through.
      if (w.depth >= 1 - shell.inner) continue;

      const depthIntoShell = Math.max(0, (1 - shell.inner) - Math.max(w.depth, 1 - shell.outer));
      if (depthIntoShell <= 0) continue;

      // Energy to clear the rest of this shell along the cone.
      const volume = coneArea * depthIntoShell * p.radius * p.radius * p.radius;
      const cost = volume * shell.strength * 40;

      if (remaining >= cost) {
        // Punch clean through this shell.
        remaining -= cost;
        w.depth = 1 - shell.inner;
        w.reached = shell.id;
        shell.damage = Math.min(1, shell.damage + coneArea * 2.4);
        this.removeMass(p, volume * shell.density);

        const ev = this.shellBreached(p, shell, at, energy);
        if (ev) out.push(ev);
      } else {
        // Partial progress.
        const frac = remaining / Math.max(cost, 1e-9);
        w.depth += depthIntoShell * frac;
        shell.damage = Math.min(1, shell.damage + coneArea * 2.4 * frac);
        this.removeMass(p, volume * frac * shell.density);
        remaining = 0;
      }
    }

    // Splash some crust off the rim of the hole regardless of depth.
    this.throwEjecta(p, dir, energy * 0.14, 'crust', at);

    const prev: PlanetPhase = p.phase;
    // reassess mutates the phase, which the compiler cannot see through the
    // early-return narrowing above, so read it back explicitly.
    const now: PlanetPhase = this.reassess(p);
    if (now !== prev && now === 'destroyed') {
      out.push(this.emit('destroyed', p, at, energy,
        `${p.id} has been destroyed`));
    }
    return out;
  }

  /** Fires the events and eruptions that go with opening a given shell. */
  private shellBreached(
    p: PlanetBody, shell: Shell, at: Vector3, energy: number
  ): DestructionEvent | null {
    if (shell.id === 'crust') {
      return this.emit('breach', p, at, energy,
        `Crust breached on ${p.id}`);
    }
    if (shell.id === 'mantle') {
      // Magma under pressure escapes through the hole.
      const dir = at.subtract(p.center).normalize();
      this.throwEjecta(p, dir, energy * 0.55, 'mantle', at);
      return this.emit('mantle', p, at, energy,
        `Mantle exposed on ${p.id} - magma venting`);
    }
    // Core. This is the one worth watching.
    const dir = at.subtract(p.center).normalize();
    this.erupt(p, dir, at);
    return this.emit('eruption', p, at, energy,
      `CORE BREACH on ${p.id} - it is coming apart`);
  }

  /**
   * The core depressurises. Superheated material blows out through the
   * wound channel in a jet, and the crust around the opening is thrown
   * clear. The jet is narrow because it is escaping through a hole.
   */
  private erupt(p: PlanetBody, dir: Vector3, at: Vector3): void {
    const core = p.shells[2];
    // Energy stored in the core scales with how much of it is left.
    const stored = p.mass * 0.06 * (1 - core.damage);

    // The jet: fast, narrow, incandescent.
    for (let i = 0; i < 60; i++) {
      const spread = 0.22;
      const v = this.jitter(dir, spread);
      const speed = 40 + Math.random() * 190;
      this.ejecta.push({
        pos: at.clone(),
        vel: v.scale(speed),
        mass: stored * 0.004,
        radius: p.radius * (0.006 + Math.random() * 0.02),
        temperature: core.temperature * (0.7 + Math.random() * 0.4),
        origin: 'core',
        age: 0
      });
    }

    // The surrounding crust is lifted off the opening.
    for (let i = 0; i < 40; i++) {
      const v = this.jitter(dir, 0.85);
      const speed = 12 + Math.random() * 60;
      this.ejecta.push({
        pos: p.center.add(v.scale(p.radius)),
        vel: v.scale(speed),
        mass: p.mass * 0.0008,
        radius: p.radius * (0.01 + Math.random() * 0.045),
        temperature: 400 + Math.random() * 900,
        origin: 'crust',
        age: 0
      });
    }

    core.damage = Math.min(1, core.damage + 0.28);
    // An asymmetric blast spins the planet up.
    p.spin += 0.4 + Math.random() * 0.6;
    this.trimEjecta();
  }

  /** Throws debris off the surface. Mass comes out of the planet. */
  private throwEjecta(
    p: PlanetBody, dir: Vector3, energy: number, from: ShellId, at: Vector3
  ): void {
    if (energy <= 0) return;
    const n = Math.min(24, Math.max(1, Math.floor(energy / 260)));
    const shell = p.shells.find((s) => s.id === from) ?? p.shells[0];

    for (let i = 0; i < n; i++) {
      const v = this.jitter(dir, from === 'mantle' ? 0.4 : 0.7);
      // Kinetic energy comes out of the energy deposited: E = 1/2 m v^2.
      const mass = p.mass * 0.00012;
      const speed = Math.sqrt((2 * (energy / n) * 0.25) / Math.max(mass, 1e-9));
      this.ejecta.push({
        pos: at.clone(),
        vel: v.scale(Math.min(speed, 320)),
        mass,
        radius: p.radius * (0.004 + Math.random() * 0.018),
        temperature: shell.temperature * (0.5 + Math.random() * 0.5),
        origin: from,
        age: 0
      });
      this.removeMass(p, mass);
    }
    this.trimEjecta();
  }

  /** A random direction within `spread` radians of `dir`. */
  private jitter(dir: Vector3, spread: number): Vector3 {
    const a = Math.random() * Math.PI * 2;
    const r = Math.random() * spread;
    // Build an orthonormal basis around dir.
    const up = Math.abs(dir.y) > 0.9 ? new Vector3(1, 0, 0) : new Vector3(0, 1, 0);
    const t1 = Vector3.Cross(dir, up).normalize();
    const t2 = Vector3.Cross(dir, t1).normalize();
    return dir
      .add(t1.scale(Math.cos(a) * r))
      .add(t2.scale(Math.sin(a) * r))
      .normalize();
  }

  /** Mass leaves the planet, and the planet shrinks to match. */
  private removeMass(p: PlanetBody, m: number): void {
    if (!(m > 0)) return;
    p.mass = Math.max(0, p.mass - m);
    // Radius tracks the cube root of the mass ratio, as it should.
    const ratio = p.originalMass > 0 ? p.mass / p.originalMass : 0;
    p.radius = p.originalRadius * Math.cbrt(Math.max(ratio, 0));
  }

  /** Recomputes phase and integrity from shell damage. */
  private reassess(p: PlanetBody): PlanetPhase {
    const [crust, mantle, core] = p.shells;
    // Weighted by how structural each layer is.
    p.integrity = Math.max(0, 1 - (
      crust.damage * 0.20 + mantle.damage * 0.35 + core.damage * 0.45
    ));

    const massLeft = p.originalMass > 0 ? p.mass / p.originalMass : 0;

    if (p.integrity <= 0.06 || massLeft < 0.25) p.phase = 'destroyed';
    else if (p.integrity < 0.35) p.phase = 'fracturing';
    else if (core.damage > 0.01) p.phase = 'erupting';
    else if (mantle.damage > 0.01) p.phase = 'bleeding';
    else if (crust.damage > 0.01) p.phase = 'wounded';
    else p.phase = 'intact';
    return p.phase;
  }

  private emit(
    kind: DestructionEvent['kind'], p: PlanetBody, at: Vector3,
    energy: number, message: string
  ): DestructionEvent {
    const ev: DestructionEvent = { kind, planet: p.id, at: at.clone(), energy, message };
    this.events.push(ev);
    if (this.events.length > 60) this.events.shift();
    return ev;
  }

  /**
   * A single enormous impact — an alien ship ramming a world, or a moon
   * dropped on it. Distributes the energy across a wide area rather than
   * boring a pinhole.
   */
  impact(id: string, at: Vector3, energy: number): DestructionEvent[] {
    const p = this.get(id);
    if (!p) return [];
    const out: DestructionEvent[] = [];
    // Several overlapping wounds make a crater rather than a drill hole.
    const dir = at.subtract(p.center).normalize();
    for (let i = 0; i < 5; i++) {
      const v = this.jitter(dir, 0.3);
      const point = p.center.add(v.scale(p.radius));
      out.push(...this.damage(id, point, energy / 5, 0.30));
    }
    p.spin += energy / (p.mass * 400 + 1);
    return out;
  }

  /** Advances ejecta under the gravity of the planets they came from. */
  update(dt: number): void {
    if (dt <= 0) return;

    for (const p of this.planets) {
      // A fracturing planet keeps shedding material on its own.
      if (p.phase === 'fracturing' && Math.random() < dt * 3) {
        const v = this.jitter(
          new Vector3(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5)
            .normalize(), 1.2);
        this.throwEjecta(p, v, 900, 'mantle', p.center.add(v.scale(p.radius)));
      }
    }

    for (let i = this.ejecta.length - 1; i >= 0; i--) {
      const e = this.ejecta[i];
      e.age += dt;

      // Gravity from every surviving planet.
      for (const p of this.planets) {
        if (p.phase === 'destroyed') continue;
        const d = p.center.subtract(e.pos);
        const r2 = Math.max(d.lengthSquared(), p.radius * p.radius * 0.25);
        const g = (p.mass * 0.00004) / r2;
        e.vel.addInPlace(d.normalize().scale(g * dt));
      }

      e.pos.addInPlace(e.vel.scale(dt));
      // Incandescent material cools as it flies.
      e.temperature = Math.max(20, e.temperature - dt * 220);

      // Cull anything that has left the area or fallen back in.
      let cull = e.age > 60;
      for (const p of this.planets) {
        if (Vector3.Distance(e.pos, p.center) > p.originalRadius * this.cullRadius) {
          cull = true;
        }
      }
      if (cull) this.ejecta.splice(i, 1);
    }
  }

  private trimEjecta(): void {
    // Oldest first, so a fresh eruption is always fully visible.
    while (this.ejecta.length > this.maxEjecta) this.ejecta.shift();
  }

  recentEvents(n = 6): DestructionEvent[] {
    return this.events.slice(-n);
  }

  /** Puts a planet back the way it was. */
  restore(id: string): boolean {
    const p = this.get(id);
    if (!p) return false;
    p.shells = SHELL_TEMPLATE.map((s) => ({ ...s, damage: 0 }));
    p.wounds = [];
    p.mass = p.originalMass;
    p.radius = p.originalRadius;
    p.integrity = 1;
    p.phase = 'intact';
    p.spin = 0;
    return true;
  }

  remove(id: string): boolean {
    const i = this.planets.findIndex((p) => p.id === id);
    if (i < 0) return false;
    this.planets.splice(i, 1);
    return true;
  }

  stats(): Record<string, string> {
    const dying = this.planets.filter(
      (p) => p.phase !== 'intact' && p.phase !== 'destroyed').length;
    const dead = this.planets.filter((p) => p.phase === 'destroyed').length;
    return {
      'Destructible worlds': String(this.planets.length),
      'Worlds damaged': String(dying),
      'Worlds destroyed': String(dead),
      'Debris in flight': String(this.ejecta.length)
    };
  }

  clear(): void {
    this.ejecta = [];
    this.events = [];
  }

  dispose(): void {
    this.planets = [];
    this.clear();
  }
}
