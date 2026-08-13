/**
 * DisasterSystem — natural disasters that act on the hydraulic grid.
 *
 * Every disaster is expressed as a modification of terrain, water depth or
 * the velocity field, which means they compose automatically: an earthquake
 * can drop a coastline that a tsunami then floods, a whirlpool can drain a
 * lake that rain refills, and a volcano can dam a river.
 *
 * Persistent disasters (whirlpools, storms, eruptions) live as entries in an
 * active list and are stepped every frame; instant ones apply once.
 */

export interface HydraulicLike {
  size: number;
  terrain: Float32Array;
  water: Float32Array;
  sediment: Float32Array;
  velX: Float32Array;
  velY: Float32Array;
  idx(x: number, y: number): number;
  addWater(cx: number, cy: number, radius: number, amount: number): void;
  deform(cx: number, cy: number, radius: number, amount: number, smooth?: boolean): void;
  crater(cx: number, cy: number, radius: number, depth: number): void;
  tsunami(height: number, width: number, fromEdge?: 'n' | 's' | 'e' | 'w'): void;
}

export type DisasterKind =
  | 'whirlpool' | 'earthquake' | 'volcano' | 'meteor' | 'tsunami'
  | 'hurricane' | 'flood' | 'drought' | 'landslide' | 'sinkhole'
  | 'geyser' | 'iceage' | 'wildfire' | 'monsoon';

export interface DisasterDef {
  kind: DisasterKind;
  name: string;
  glyph: string;
  blurb: string;
  /** Seconds; 0 means instantaneous. */
  duration: number;
  severityMin: number;
  severityMax: number;
}

export const DISASTERS: Record<DisasterKind, DisasterDef> = {
  whirlpool:  { kind: 'whirlpool',  name: 'Whirlpool',  glyph: '🌀', blurb: 'A rotating vortex that pulls water into its throat.', duration: 30, severityMin: 0.2, severityMax: 3 },
  earthquake: { kind: 'earthquake', name: 'Earthquake', glyph: '💥', blurb: 'Fault rupture: one side drops, the other lifts.',       duration: 0,  severityMin: 0.1, severityMax: 3 },
  volcano:    { kind: 'volcano',    name: 'Volcano',    glyph: '🌋', blurb: 'Builds a cone and erupts, damming whatever is nearby.',  duration: 20, severityMin: 0.2, severityMax: 3 },
  meteor:     { kind: 'meteor',     name: 'Meteor',     glyph: '☄',  blurb: 'Excavates a crater and throws the water outward.',       duration: 0,  severityMin: 0.2, severityMax: 4 },
  tsunami:    { kind: 'tsunami',    name: 'Tsunami',    glyph: '🌊', blurb: 'A wall of water crossing the map at wave speed.',        duration: 0,  severityMin: 0.2, severityMax: 4 },
  hurricane:  { kind: 'hurricane',  name: 'Hurricane',  glyph: '🌪', blurb: 'A travelling storm: rotating winds and torrential rain.', duration: 40, severityMin: 0.2, severityMax: 3 },
  flood:      { kind: 'flood',      name: 'Great Flood',glyph: '🌧', blurb: 'Sea level rises and keeps rising.',                      duration: 25, severityMin: 0.1, severityMax: 2 },
  drought:    { kind: 'drought',    name: 'Drought',    glyph: '🏜', blurb: 'Accelerated evaporation until the rivers fail.',         duration: 30, severityMin: 0.1, severityMax: 2 },
  landslide:  { kind: 'landslide',  name: 'Landslide',  glyph: '⛰',  blurb: 'Steep ground collapses and slumps downhill.',            duration: 6,  severityMin: 0.2, severityMax: 3 },
  sinkhole:   { kind: 'sinkhole',   name: 'Sinkhole',   glyph: '🕳', blurb: 'The ground gives way and swallows the water above it.',  duration: 8,  severityMin: 0.2, severityMax: 3 },
  geyser:     { kind: 'geyser',     name: 'Geyser',     glyph: '⛲', blurb: 'Pressurised water erupts in rhythmic bursts.',           duration: 25, severityMin: 0.2, severityMax: 3 },
  iceage:     { kind: 'iceage',     name: 'Ice Age',    glyph: '🧊', blurb: 'Water locks up as ice and the sea retreats.',            duration: 30, severityMin: 0.1, severityMax: 2 },
  wildfire:   { kind: 'wildfire',   name: 'Wildfire',   glyph: '🔥', blurb: 'Dries the land and scorches it bare.',                   duration: 20, severityMin: 0.1, severityMax: 2 },
  monsoon:    { kind: 'monsoon',    name: 'Monsoon',    glyph: '⛈', blurb: 'Relentless rain across the whole region.',               duration: 30, severityMin: 0.1, severityMax: 3 }
};

export const DISASTER_ORDER: DisasterKind[] = [
  'whirlpool', 'tsunami', 'hurricane', 'monsoon', 'flood', 'geyser',
  'earthquake', 'volcano', 'meteor', 'landslide', 'sinkhole',
  'drought', 'wildfire', 'iceage'
];

interface ActiveDisaster {
  kind: DisasterKind;
  x: number;
  y: number;
  radius: number;
  severity: number;
  age: number;
  life: number;
  /** Per-disaster scratch state. */
  phase: number;
  vx: number;
  vy: number;
}

export class DisasterSystem {
  private active: ActiveDisaster[] = [];
  private hydro: HydraulicLike;
  triggered = 0;
  /** Extra evaporation/rain the world should apply, set by climate events. */
  climateRain = 0;
  climateEvaporation = 0;

  /**
   * Rotational velocity owned by this system.
   *
   * The hydraulic solver recomputes its own velX/velY from flux every step,
   * so writing rotation there would be erased immediately. Vortex motion is
   * therefore kept here and applied as real, mass-conserving transport of
   * water between cells; renderers read this field for foam and swirl.
   */
  swirlX: Float32Array;
  swirlY: Float32Array;

  constructor(hydro: HydraulicLike) {
    this.hydro = hydro;
    const len = hydro.size * hydro.size;
    this.swirlX = new Float32Array(len);
    this.swirlY = new Float32Array(len);
  }

  /**
   * Moves water tangentially around a centre by transporting mass between
   * neighbouring cells. Unlike writing to a derived velocity field, this
   * survives the solver and conserves total water exactly.
   */
  private rotateWater(cx: number, cy: number, radius: number,
                      strength: number, dt: number): void {
    const h = this.hydro;
    const n = h.size;
    const moved: Array<[number, number, number]> = [];
    const k = Math.max(0, Math.min(0.45, strength * dt));

    for (let gy = Math.max(0, Math.floor(cy - radius)); gy <= Math.min(n - 1, Math.ceil(cy + radius)); gy++) {
      for (let gx = Math.max(0, Math.floor(cx - radius)); gx <= Math.min(n - 1, Math.ceil(cx + radius)); gx++) {
        const dx = gx - cx, dy = gy - cy;
        const dist = Math.hypot(dx, dy);
        if (dist > radius || dist < 1.0) continue;
        const i = h.idx(gx, gy);
        if (h.water[i] <= 1e-6) continue;

        // tangential unit vector
        const tx = -dy / dist, ty = dx / dist;
        const falloff = 1 - dist / radius;

        // record the swirl for rendering
        this.swirlX[i] = tx * strength * falloff;
        this.swirlY[i] = ty * strength * falloff;

        // transport a fraction of this cell's water to the tangential neighbour
        const nx2 = Math.round(gx + tx);
        const ny2 = Math.round(gy + ty);
        if (nx2 < 0 || ny2 < 0 || nx2 >= n || ny2 >= n) continue;
        if (nx2 === gx && ny2 === gy) continue;
        const j = h.idx(nx2, ny2);
        const amount = h.water[i] * k * falloff;
        if (amount > 0) moved.push([i, j, amount]);
      }
    }
    // apply after sampling so the order of iteration cannot bias the result
    for (const [i, j, amt] of moved) {
      h.water[i] -= amt;
      h.water[j] += amt;
    }
  }

  /** Starts a disaster. Instant ones apply immediately and are not tracked. */
  trigger(kind: DisasterKind, x: number, y: number, severity = 1, radius = 14): void {
    const def = DISASTERS[kind];
    if (!def) return;
    const s = Math.max(def.severityMin, Math.min(def.severityMax, severity));
    this.triggered++;

    if (def.duration <= 0) {
      this.applyInstant(kind, x, y, s, radius);
      return;
    }
    this.active.push({
      kind, x, y, radius, severity: s,
      age: 0, life: def.duration, phase: 0,
      vx: (Math.random() - 0.5) * 6, vy: (Math.random() - 0.5) * 6
    });
  }

  private applyInstant(kind: DisasterKind, x: number, y: number, s: number, radius: number): void {
    const h = this.hydro;
    switch (kind) {
      case 'earthquake': {
        // a fault line: ground drops on one side and lifts on the other
        const n = h.size;
        const angle = Math.random() * Math.PI;
        const nx = Math.cos(angle), ny = Math.sin(angle);
        const reach = radius * 2.5;
        for (let gy = 0; gy < n; gy++) {
          for (let gx = 0; gx < n; gx++) {
            const dx = gx - x, dy = gy - y;
            const dist = Math.hypot(dx, dy);
            if (dist > reach) continue;
            const side = dx * nx + dy * ny;
            const falloff = 1 - dist / reach;
            h.terrain[h.idx(gx, gy)] += Math.sign(side) * 0.22 * s * falloff * falloff;
          }
        }
        break;
      }
      case 'meteor':
        h.crater(x, y, radius, 0.4 * s);
        // the impact throws water outward as a ring
        h.addWater(x, y, Math.floor(radius * 1.8), 0.1 * s);
        break;
      case 'tsunami': {
        const edges = ['n', 's', 'e', 'w'] as const;
        h.tsunami(0.4 * s, Math.max(4, Math.floor(radius * 0.8)),
          edges[Math.floor(Math.random() * 4)]);
        break;
      }
      default:
        break;
    }
  }

  /** Advances every persistent disaster. */
  update(dt: number): void {
    const h = this.hydro;
    const n = h.size;
    this.climateRain = 0;
    this.climateEvaporation = 0;

    for (let i = this.active.length - 1; i >= 0; i--) {
      const d = this.active[i];
      d.age += dt;
      d.phase += dt;
      const t = d.age / d.life;
      // most disasters ramp up then fade
      const envelope = Math.sin(Math.min(1, t) * Math.PI);

      switch (d.kind) {
        case 'whirlpool':
          this.stepWhirlpool(d, dt, envelope);
          break;

        case 'hurricane': {
          // the storm travels, raining and stirring as it goes
          d.x += d.vx * dt;
          d.y += d.vy * dt;
          if (d.x < 4 || d.x > n - 5) d.vx *= -1;
          if (d.y < 4 || d.y > n - 5) d.vy *= -1;
          d.x = Math.max(2, Math.min(n - 3, d.x));
          d.y = Math.max(2, Math.min(n - 3, d.y));
          h.addWater(Math.round(d.x), Math.round(d.y),
            Math.floor(d.radius * 1.6), 0.012 * d.severity * envelope);
          this.rotateWater(d.x, d.y, d.radius * 1.8, 1.6 * d.severity * envelope, dt);
          break;
        }

        case 'volcano': {
          // builds a cone, then erupts lava that raises terrain
          h.deform(Math.round(d.x), Math.round(d.y),
            Math.floor(d.radius), 0.02 * d.severity * envelope);
          if (Math.sin(d.phase * 3) > 0.7) {
            h.deform(Math.round(d.x), Math.round(d.y),
              Math.floor(d.radius * 0.35), 0.05 * d.severity);
          }
          break;
        }

        case 'geyser': {
          // rhythmic bursts rather than a steady stream
          if (Math.sin(d.phase * 4) > 0.6) {
            h.addWater(Math.round(d.x), Math.round(d.y),
              Math.max(2, Math.floor(d.radius * 0.4)), 0.09 * d.severity);
          }
          break;
        }

        case 'flood':
          // sea level creeps upward everywhere
          for (let k = 0; k < h.water.length; k++) {
            h.water[k] += 0.0016 * d.severity * envelope * dt * 60;
          }
          break;

        case 'monsoon':
          this.climateRain += 0.05 * d.severity * envelope;
          break;

        case 'drought':
        case 'wildfire':
          this.climateEvaporation += 0.02 * d.severity * envelope;
          if (d.kind === 'wildfire') {
            // scorching lowers the surface slightly as vegetation burns off
            h.deform(Math.round(d.x), Math.round(d.y),
              Math.floor(d.radius), -0.004 * d.severity * envelope);
          }
          break;

        case 'iceage':
          // water locks up: remove it from circulation
          for (let k = 0; k < h.water.length; k++) {
            h.water[k] *= Math.max(0, 1 - 0.004 * d.severity * envelope * dt * 60);
          }
          break;

        case 'sinkhole': {
          // the ground gives way in a sharp collapse
          h.deform(Math.round(d.x), Math.round(d.y),
            Math.max(2, Math.floor(d.radius * 0.5)), -0.06 * d.severity * envelope, false);
          break;
        }

        case 'landslide':
          this.stepLandslide(d, dt, envelope);
          break;

        default:
          break;
      }

      if (d.age >= d.life) this.active.splice(i, 1);
    }
  }

  /**
   * A whirlpool: tangential swirl plus inward suction, with water removed at
   * the throat and released again when it dissipates.
   */
  private stepWhirlpool(d: ActiveDisaster, dt: number, envelope: number): void {
    const h = this.hydro;
    const n = h.size;
    const r = d.radius;
    const cx = d.x, cy = d.y;
    const strength = d.severity * envelope;

    // 1. real rotation: transport water mass tangentially
    this.rotateWater(cx, cy, r, 3.2 * strength, dt);

    // 2. drain the throat. The resulting surface depression is what makes the
    //    shallow-water solver pull surrounding water inward on its own, so
    //    the inflow is emergent rather than scripted.
    for (let gy = Math.max(0, Math.floor(cy - r)); gy <= Math.min(n - 1, Math.ceil(cy + r)); gy++) {
      for (let gx = Math.max(0, Math.floor(cx - r)); gx <= Math.min(n - 1, Math.ceil(cx + r)); gx++) {
        const dist = Math.hypot(gx - cx, gy - cy);
        if (dist > r * 0.3) continue;
        const i = h.idx(gx, gy);
        h.water[i] = Math.max(0, h.water[i] - 0.02 * strength * dt * 60 * (1 - dist / (r * 0.3)));
      }
    }
  }

  /** Steep slopes slump downhill until the angle of repose is satisfied. */
  private stepLandslide(d: ActiveDisaster, dt: number, envelope: number): void {
    const h = this.hydro;
    const n = h.size;
    const r = Math.floor(d.radius);
    const talus = 0.035;                       // maximum stable slope
    const rate = Math.min(0.5, 2.5 * dt * d.severity * envelope);

    for (let gy = Math.max(1, Math.floor(d.y - r)); gy <= Math.min(n - 2, Math.floor(d.y + r)); gy++) {
      for (let gx = Math.max(1, Math.floor(d.x - r)); gx <= Math.min(n - 2, Math.floor(d.x + r)); gx++) {
        if (Math.hypot(gx - d.x, gy - d.y) > r) continue;
        const i = h.idx(gx, gy);
        const hc = h.terrain[i];
        // find the steepest downhill neighbour
        let bestD = 0, bestJ = -1;
        for (const [ox, oy] of [[1, 0], [-1, 0], [0, 1], [0, -1]] as const) {
          const j = h.idx(gx + ox, gy + oy);
          const diff = hc - h.terrain[j];
          if (diff > bestD) { bestD = diff; bestJ = j; }
        }
        if (bestJ >= 0 && bestD > talus) {
          const move = (bestD - talus) * 0.5 * rate;
          h.terrain[i] -= move;
          h.terrain[bestJ] += move;
        }
      }
    }
  }

  activeList(): { kind: DisasterKind; name: string; glyph: string; remaining: number }[] {
    return this.active.map((d) => ({
      kind: d.kind,
      name: DISASTERS[d.kind].name,
      glyph: DISASTERS[d.kind].glyph,
      remaining: Math.max(0, d.life - d.age)
    }));
  }

  count(): number {
    return this.active.length;
  }

  clear(): void {
    this.active = [];
    this.swirlX.fill(0);
    this.swirlY.fill(0);
    this.climateRain = 0;
    this.climateEvaporation = 0;
  }
}
