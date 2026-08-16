/**
 * GasDive — falling into a gas giant.
 *
 * You cannot land on a gas giant; there is no ground. What you can do is
 * *descend*: the planet's gravity pulls you down through a stack of cloud
 * decks, each denser than the last, until drag balances gravity and you
 * reach terminal velocity in a world with no bottom. The descent crosses
 * named layers - the ammonia decks, the storm belt, and (if you go deep
 * enough) the region where the pressure would crush a ship. Crossing each
 * one is an event the HUD and the field guide can show.
 *
 * Pure arithmetic, no Babylon, so the descent is testable exactly.
 */

export interface GasLayer {
  name: string;
  /** Altitude (0 = the arbitrary "surface" reference) at which this layer starts. */
  top: number;
  /** Air density at this layer, relative units. */
  density: number;
}

export const GAS_LAYERS: GasLayer[] = [
  { name: 'upper haze', top: 9000, density: 0.06 },
  { name: 'ammonia decks', top: 6000, density: 0.22 },
  { name: 'storm belt', top: 3000, density: 0.6 },
  { name: 'metallic hydrogen', top: 500, density: 1.4 }
];

export interface GasDiveState {
  altitude: number;
  speed: number;
  density: number;
  pressure: number;
  layer: string;
  /** True once drag has matched gravity and the fall is steady. */
  terminal: boolean;
}

export class GasDive {
  altitude: number;
  speed = 0;
  private layerIndex = 0;
  /** Fired once per crossing, drained by the caller. */
  events: string[] = [];

  constructor(public gravity = 24, startAltitude = 10000) {
    this.altitude = startAltitude;
  }

  /** Advances the fall. Ignores nonsense timesteps. */
  step(dt: number): void {
    if (!Number.isFinite(dt) || dt <= 0) return;

    const layer = GAS_LAYERS[this.layerIndex] ?? GAS_LAYERS[GAS_LAYERS.length - 1];
    // Drag rises with density; gravity is constant. Terminal velocity
    // emerges from the balance, exactly like the atmospheric descent.
    const drag = 0.5 * layer.density * this.speed * this.speed * 0.02;
    const accel = this.gravity - drag;
    this.speed = Math.max(0, this.speed + accel * dt);
    this.altitude -= this.speed * dt;

    // Crossing a deck boundary fires exactly one event.
    while (this.layerIndex < GAS_LAYERS.length - 1 &&
           this.altitude < GAS_LAYERS[this.layerIndex + 1].top) {
      this.layerIndex++;
      this.events.push(GAS_LAYERS[this.layerIndex].name);
    }
  }

  state(): GasDiveState {
    const layer = GAS_LAYERS[this.layerIndex] ?? GAS_LAYERS[GAS_LAYERS.length - 1];
    const terminal = Math.abs(this.speed) > 0 && this.speed > this.gravity / (layer.density * 0.02) * 0.8;
    return {
      altitude: this.altitude,
      speed: this.speed,
      density: layer.density,
      pressure: layer.density * Math.max(0, GAS_LAYERS[0].top - this.altitude) * 0.01,
      layer: layer.name,
      terminal: terminal
    };
  }

  /** Drains and returns the layers crossed since the last call. */
  drainEvents(): string[] {
    const out = this.events;
    this.events = [];
    return out;
  }

  stats(): Record<string, string> {
    const s = this.state();
    return {
      'Gas dive': s.layer,
      'Depth': Math.max(0, s.altitude).toFixed(0) + ' u',
      'Fall speed': s.speed.toFixed(0) + ' u/s'
    };
  }
}
