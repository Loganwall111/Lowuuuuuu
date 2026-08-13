/**
 * HydraulicSystem — grid-based shallow-water simulation with erosion.
 *
 * This is a real fluid solver, not an animated texture. It integrates the
 * shallow-water equations on a height grid:
 *
 *   flux  f  += g * (terrain+water height difference) * dt      (acceleration)
 *   water h  -= net outflow * dt                                (continuity)
 *
 * Because flow is driven by the total surface gradient, water genuinely runs
 * downhill, pools in basins, carves rivers, forms lakes and spreads as a
 * tsunami when displaced. Sediment is picked up by fast flow and redeposited
 * where flow slows, which erodes valleys over time.
 */

export interface HydraulicOptions {
  size: number;        // grid resolution (size x size)
  cell: number;        // world units per cell
  gravity: number;
  friction: number;
  evaporation: number;
  erosion: number;
  deposition: number;
  rain: number;
}

export const DEFAULT_HYDRO: HydraulicOptions = {
  size: 128,
  cell: 1.0,
  gravity: 9.81,
  friction: 0.985,
  evaporation: 0.0004,
  erosion: 0.30,
  deposition: 0.22,
  rain: 0.0
};

export class HydraulicSystem {
  readonly size: number;
  opts: HydraulicOptions;

  /** Terrain height per cell. */
  terrain: Float32Array;
  /** Water depth per cell (0 = dry). */
  water: Float32Array;
  /** Suspended sediment. */
  sediment: Float32Array;
  /** Outflow flux in each of the 4 directions. */
  private fL: Float32Array;
  private fR: Float32Array;
  private fT: Float32Array;
  private fB: Float32Array;
  /** Velocity field, used for erosion strength and shading. */
  velX: Float32Array;
  velY: Float32Array;

  constructor(options: Partial<HydraulicOptions> = {}) {
    this.opts = { ...DEFAULT_HYDRO, ...options };
    const n = this.opts.size;
    this.size = n;
    const len = n * n;
    this.terrain = new Float32Array(len);
    this.water = new Float32Array(len);
    this.sediment = new Float32Array(len);
    this.fL = new Float32Array(len);
    this.fR = new Float32Array(len);
    this.fT = new Float32Array(len);
    this.fB = new Float32Array(len);
    this.velX = new Float32Array(len);
    this.velY = new Float32Array(len);
  }

  idx(x: number, y: number): number {
    return y * this.size + x;
  }

  /** Fills the terrain from a height function over normalised coords. */
  generateTerrain(fn: (nx: number, ny: number) => number): void {
    const n = this.size;
    for (let y = 0; y < n; y++) {
      for (let x = 0; x < n; x++) {
        this.terrain[this.idx(x, y)] = fn(x / (n - 1), y / (n - 1));
      }
    }
  }

  /** Floods everything below `level` so oceans and lakes appear. */
  setSeaLevel(level: number): void {
    const len = this.size * this.size;
    for (let i = 0; i < len; i++) {
      const d = level - this.terrain[i];
      this.water[i] = d > 0 ? d : 0;
    }
  }

  /** Adds water in a disc — a spring, a rainstorm cell, or a spill. */
  addWater(cx: number, cy: number, radius: number, amount: number): void {
    const n = this.size;
    const r2 = radius * radius;
    for (let y = Math.max(0, cy - radius | 0); y < Math.min(n, cy + radius + 1); y++) {
      for (let x = Math.max(0, cx - radius | 0); x < Math.min(n, cx + radius + 1); x++) {
        const dx = x - cx, dy = y - cy;
        const d2 = dx * dx + dy * dy;
        if (d2 > r2) continue;
        const falloff = 1 - Math.sqrt(d2) / radius;
        this.water[this.idx(x, y)] += amount * falloff;
      }
    }
  }

  /** Raises or lowers terrain — the planet-painter brush and impact craters. */
  deform(cx: number, cy: number, radius: number, amount: number, smooth = true): void {
    const n = this.size;
    for (let y = Math.max(0, cy - radius | 0); y < Math.min(n, cy + radius + 1); y++) {
      for (let x = Math.max(0, cx - radius | 0); x < Math.min(n, cx + radius + 1); x++) {
        const dx = x - cx, dy = y - cy;
        const d = Math.sqrt(dx * dx + dy * dy);
        if (d > radius) continue;
        const t = 1 - d / radius;
        const f = smooth ? t * t * (3 - 2 * t) : t;
        this.terrain[this.idx(x, y)] += amount * f;
      }
    }
  }

  /** Impact crater: a bowl with a raised rim, and it displaces water. */
  crater(cx: number, cy: number, radius: number, depth: number): void {
    const n = this.size;
    for (let y = Math.max(0, cy - radius * 1.6 | 0); y < Math.min(n, cy + radius * 1.6 + 1); y++) {
      for (let x = Math.max(0, cx - radius * 1.6 | 0); x < Math.min(n, cx + radius * 1.6 + 1); x++) {
        const dx = x - cx, dy = y - cy;
        const d = Math.sqrt(dx * dx + dy * dy) / radius;
        if (d > 1.6) continue;
        const i = this.idx(x, y);
        if (d < 1) {
          this.terrain[i] -= depth * (1 - d * d);          // bowl
        } else {
          this.terrain[i] += depth * 0.28 * (1.6 - d) / 0.6; // ejecta rim
        }
      }
    }
  }

  /** Directional wave — the tsunami tool. */
  tsunami(height: number, width: number, fromEdge: 'n' | 's' | 'e' | 'w' = 'w'): void {
    const n = this.size;
    for (let i = 0; i < n; i++) {
      for (let w = 0; w < width; w++) {
        let x = 0, y = 0;
        if (fromEdge === 'w') { x = w; y = i; }
        else if (fromEdge === 'e') { x = n - 1 - w; y = i; }
        else if (fromEdge === 'n') { x = i; y = w; }
        else { x = i; y = n - 1 - w; }
        const fall = 1 - w / width;
        this.water[this.idx(x, y)] += height * fall;
      }
    }
  }

  /** One simulation step of the shallow-water solver. */
  step(dt: number): void {
    const n = this.size;
    const { gravity, friction, evaporation, erosion, deposition, rain, cell } = this.opts;
    const A = cell * cell;

    if (rain > 0) {
      const len = n * n;
      for (let i = 0; i < len; i++) this.water[i] += rain * dt;
    }

    // ---- 1. flux update: water accelerates down the total height gradient ----
    for (let y = 0; y < n; y++) {
      for (let x = 0; x < n; x++) {
        const i = this.idx(x, y);
        const h = this.terrain[i] + this.water[i];

        const flow = (nx: number, ny: number, prev: number): number => {
          if (nx < 0 || ny < 0 || nx >= n || ny >= n) return 0;   // closed boundary
          const j = this.idx(nx, ny);
          const dh = h - (this.terrain[j] + this.water[j]);
          const f = (prev + gravity * dh * dt) * friction;
          return f > 0 ? f : 0;
        };

        this.fL[i] = flow(x - 1, y, this.fL[i]);
        this.fR[i] = flow(x + 1, y, this.fR[i]);
        this.fT[i] = flow(x, y - 1, this.fT[i]);
        this.fB[i] = flow(x, y + 1, this.fB[i]);

        // never drain a cell below empty
        const total = (this.fL[i] + this.fR[i] + this.fT[i] + this.fB[i]) * dt;
        const avail = this.water[i] * A;
        if (total > avail && total > 0) {
          const k = avail / total;
          this.fL[i] *= k; this.fR[i] *= k; this.fT[i] *= k; this.fB[i] *= k;
        }
      }
    }

    // ---- 2. continuity: apply net flux ----
    for (let y = 0; y < n; y++) {
      for (let x = 0; x < n; x++) {
        const i = this.idx(x, y);
        let inflow = 0;
        if (x > 0)     inflow += this.fR[this.idx(x - 1, y)];
        if (x < n - 1) inflow += this.fL[this.idx(x + 1, y)];
        if (y > 0)     inflow += this.fB[this.idx(x, y - 1)];
        if (y < n - 1) inflow += this.fT[this.idx(x, y + 1)];
        const outflow = this.fL[i] + this.fR[i] + this.fT[i] + this.fB[i];

        const dV = (inflow - outflow) * dt;
        const before = this.water[i];
        this.water[i] = Math.max(0, before + dV / A);

        // velocity from the horizontal flux difference
        const vx = ((x > 0 ? this.fR[this.idx(x - 1, y)] : 0) - this.fL[i]
                  + this.fR[i] - (x < n - 1 ? this.fL[this.idx(x + 1, y)] : 0)) * 0.5;
        const vy = ((y > 0 ? this.fB[this.idx(x, y - 1)] : 0) - this.fT[i]
                  + this.fB[i] - (y < n - 1 ? this.fT[this.idx(x, y + 1)] : 0)) * 0.5;
        const depth = Math.max((before + this.water[i]) * 0.5, 0.001);
        this.velX[i] = vx / depth;
        this.velY[i] = vy / depth;
      }
    }

    // ---- 3. erosion & deposition: fast water carves, slow water fills ----
    if (erosion > 0 || deposition > 0) {
      const len = n * n;
      for (let i = 0; i < len; i++) {
        if (this.water[i] < 0.0005) {
          // dry cell drops whatever it was carrying
          this.terrain[i] += this.sediment[i];
          this.sediment[i] = 0;
          continue;
        }
        const speed = Math.hypot(this.velX[i], this.velY[i]);
        const capacity = speed * this.water[i] * 0.55;
        if (capacity > this.sediment[i]) {
          const take = (capacity - this.sediment[i]) * erosion * dt * 6;
          this.terrain[i] -= take;
          this.sediment[i] += take;
        } else {
          const drop = (this.sediment[i] - capacity) * deposition * dt * 6;
          this.terrain[i] += drop;
          this.sediment[i] -= drop;
        }
      }
    }

    // ---- 4. evaporation ----
    if (evaporation > 0) {
      const len = n * n;
      const k = Math.max(0, 1 - evaporation * dt * 60);
      for (let i = 0; i < len; i++) this.water[i] *= k;
    }
  }

  /* ------------------------------ measurements ------------------------------ */

  totalWater(): number {
    let s = 0;
    for (let i = 0; i < this.water.length; i++) s += this.water[i];
    return s;
  }

  totalTerrain(): number {
    let s = 0;
    for (let i = 0; i < this.terrain.length; i++) s += this.terrain[i];
    return s;
  }

  maxDepth(): number {
    let m = 0;
    for (let i = 0; i < this.water.length; i++) if (this.water[i] > m) m = this.water[i];
    return m;
  }

  /** Fraction of the grid covered by meaningful water. */
  coverage(): number {
    let c = 0;
    for (let i = 0; i < this.water.length; i++) if (this.water[i] > 0.01) c++;
    return c / this.water.length;
  }
}
