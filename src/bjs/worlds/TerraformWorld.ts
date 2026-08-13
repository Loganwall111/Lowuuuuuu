/**
 * TerraformWorld — a living landscape driven by the hydraulic solver.
 *
 * The terrain mesh and water mesh are both rebuilt from the simulation grid
 * every frame, so what you see is literally the state of the fluid solver:
 * rivers carve their own valleys, lakes fill basins, tsunamis run inland and
 * flood low ground, and rain slowly erodes mountains into sediment fans.
 *
 * The painter tools (raise, lower, smooth, dig, water, rain, tsunami, meteor)
 * all operate on the same grid, so every tool composes with every other one.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import { VertexData } from '@babylonjs/core/Meshes/mesh.vertexData';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { HemisphericLight } from '@babylonjs/core/Lights/hemisphericLight';
import { DirectionalLight } from '@babylonjs/core/Lights/directionalLight';
import { PointerEventTypes } from '@babylonjs/core/Events/pointerEvents';
import type { Observer } from '@babylonjs/core/Misc/observable';
import type { PointerInfo } from '@babylonjs/core/Events/pointerEvents';
import { HydraulicSystem } from '../systems/HydraulicSystem';
import { DisasterSystem, DISASTERS, DISASTER_ORDER, type DisasterKind } from '../systems/DisasterSystem';
import { fbmCPU, ridgedCPU } from '../Noise';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';

type Tool = 'raise' | 'lower' | 'smooth' | 'dig' | 'water' | 'drain';

const GRID = 128;         // solver resolution
const SPAN = 120;         // world units across
const HEIGHT = 26;        // vertical exaggeration

export class TerraformWorld implements World {
  id = 'terraform';
  name = 'Terraform';

  private ctx!: WorldContext;
  private hydro!: HydraulicSystem;
  private land!: Mesh;
  private sea!: Mesh;
  private landMat!: StandardMaterial;
  private seaMat!: StandardMaterial;
  private pointerObs: Observer<PointerInfo> | null = null;
  private painting = false;
  private tool: Tool = 'raise';
  private t = 0;
  private accum = 0;
  private seed = Math.random() * 1000;
  private meteors = 0;
  private disasters!: DisasterSystem;

  private p = {
    rain: 0.0,
    evaporation: 0.04,
    erosion: 0.35,
    seaLevel: 0.30,
    flowSpeed: 1.0,
    severity: 1.5,
    brushSize: 10,
    brushStrength: 1.0,
    showWater: 1
  };

  async build(ctx: WorldContext): Promise<void> {
    this.ctx = ctx;
    const scene = ctx.scene;

    const hemi = new HemisphericLight('hemi', new Vector3(0.2, 1, 0.1), scene);
    hemi.intensity = 0.45;
    hemi.groundColor = new Color3(0.16, 0.14, 0.2);

    const sun = new DirectionalLight('sun', new Vector3(-0.55, -0.72, 0.42), scene);
    sun.intensity = 2.1;
    sun.diffuse = new Color3(1.0, 0.95, 0.86);

    this.hydro = new HydraulicSystem({
      size: GRID, cell: SPAN / GRID,
      evaporation: this.p.evaporation * 0.01,
      erosion: this.p.erosion,
      deposition: this.p.erosion * 0.7,
      rain: 0
    });

    this.disasters = new DisasterSystem(this.hydro);

    this.generateLandscape();

    // ---- meshes ----
    this.land = new Mesh('land', scene);
    this.landMat = new StandardMaterial('landMat', scene);
    this.landMat.specularColor = new Color3(0.06, 0.06, 0.06);
    this.landMat.diffuseColor = new Color3(1, 1, 1);
    this.land.material = this.landMat;

    this.sea = new Mesh('sea', scene);
    this.seaMat = new StandardMaterial('seaMat', scene);
    this.seaMat.diffuseColor = new Color3(0.06, 0.22, 0.38);
    this.seaMat.specularColor = new Color3(0.9, 0.95, 1.0);
    this.seaMat.specularPower = 96;
    this.seaMat.emissiveColor = new Color3(0.01, 0.05, 0.09);
    this.seaMat.alpha = 0.82;
    this.sea.material = this.seaMat;

    this.rebuildLand();
    this.rebuildWater();

    this.installPointer();
    ctx.setCameraTarget(Vector3.Zero(), 110);
  }

  /** Mountains from ridged noise, plus continental shelves from fbm. */
  private generateLandscape(): void {
    const s = this.seed;
    this.hydro.generateTerrain((nx, ny) => {
      const x = nx * 3.2, y = ny * 3.2;
      // broad continents
      const base = fbmCPU(x * 0.8, y * 0.8, s, 5) * 0.55;
      // mountain ridges
      const ridge = Math.pow(Math.max(0, ridgedCPU(x * 1.6, y * 1.6, s + 40, 5)), 1.6) * 0.75;
      // large-scale tilt so water has somewhere to go
      const tilt = (1 - ny) * 0.12;
      let h = base + ridge * (0.35 + base) + tilt;
      // soften the borders into a basin so water pools instead of vanishing
      const edge = Math.min(nx, ny, 1 - nx, 1 - ny);
      h *= 0.35 + 0.65 * Math.min(1, edge * 5);
      return h;
    });
    this.hydro.setSeaLevel(this.p.seaLevel);
  }

  /* ------------------------------ mesh building ------------------------------ */

  private rebuildLand(): void {
    const n = GRID;
    const pos = new Float32Array(n * n * 3);
    const col = new Float32Array(n * n * 4);
    const nrm = new Float32Array(n * n * 3);
    const step = SPAN / (n - 1);

    for (let y = 0; y < n; y++) {
      for (let x = 0; x < n; x++) {
        const i = y * n + x;
        const h = this.hydro.terrain[i];
        pos[i * 3] = -SPAN / 2 + x * step;
        pos[i * 3 + 1] = h * HEIGHT;
        pos[i * 3 + 2] = -SPAN / 2 + y * step;

        // slope for rock exposure
        const hl = this.hydro.terrain[y * n + Math.max(0, x - 1)];
        const hr = this.hydro.terrain[y * n + Math.min(n - 1, x + 1)];
        const hd = this.hydro.terrain[Math.max(0, y - 1) * n + x];
        const hu = this.hydro.terrain[Math.min(n - 1, y + 1) * n + x];
        const slope = Math.min(1, Math.hypot(hr - hl, hu - hd) * HEIGHT * 0.6);

        const c = this.terrainColor(h, slope, this.hydro.water[i], this.hydro.sediment[i]);
        col[i * 4] = c[0]; col[i * 4 + 1] = c[1]; col[i * 4 + 2] = c[2]; col[i * 4 + 3] = 1;
      }
    }

    const idx = this.buildIndices(n);
    const vd = new VertexData();
    vd.positions = pos as unknown as number[];
    vd.indices = idx;
    vd.colors = col as unknown as number[];
    VertexData.ComputeNormals(pos, idx, nrm);
    vd.normals = nrm as unknown as number[];
    vd.applyToMesh(this.land, true);
    this.land.useVertexColors = true;
    this.landMat.diffuseColor = new Color3(1, 1, 1);
  }

  /** Height/slope/wetness driven colouring: sand, grass, rock, snow. */
  private terrainColor(h: number, slope: number, water: number, sed: number): [number, number, number] {
    const snowLine = 0.62, rockLine = 0.34, sandLine = this.p.seaLevel + 0.015;
    let r: number, g: number, b: number;

    if (h > snowLine) {
      const t = Math.min(1, (h - snowLine) / 0.25);
      r = 0.62 + 0.34 * t; g = 0.66 + 0.32 * t; b = 0.72 + 0.28 * t;
      // steep faces stay rocky even up high
      r -= slope * 0.3; g -= slope * 0.3; b -= slope * 0.28;
    } else if (h > rockLine) {
      const t = (h - rockLine) / (snowLine - rockLine);
      r = 0.30 + t * 0.22; g = 0.34 - t * 0.06; b = 0.26 - t * 0.02;
      r += slope * 0.14; g += slope * 0.12; b += slope * 0.10;
    } else if (h > sandLine) {
      const t = (h - sandLine) / Math.max(rockLine - sandLine, 0.001);
      r = 0.20 + t * 0.12; g = 0.40 - t * 0.06; b = 0.16 + t * 0.08;
      r += slope * 0.22; g += slope * 0.06; b += slope * 0.08;
    } else {
      r = 0.52; g = 0.46; b = 0.34;               // sand / lakebed
    }

    // fresh sediment lightens the ground
    const sd = Math.min(0.25, sed * 6);
    r += sd; g += sd * 0.9; b += sd * 0.6;
    // submerged ground darkens
    if (water > 0.005) {
      const w = Math.min(0.6, water * 2.5);
      r *= 1 - w * 0.55; g *= 1 - w * 0.4; b *= 1 - w * 0.15;
    }
    return [Math.max(0, Math.min(1, r)), Math.max(0, Math.min(1, g)), Math.max(0, Math.min(1, b))];
  }

  private rebuildWater(): void {
    const n = GRID;
    const step = SPAN / (n - 1);
    const pos = new Float32Array(n * n * 3);
    const col = new Float32Array(n * n * 4);
    const nrm = new Float32Array(n * n * 3);

    for (let y = 0; y < n; y++) {
      for (let x = 0; x < n; x++) {
        const i = y * n + x;
        const w = this.hydro.water[i];
        const surface = (this.hydro.terrain[i] + w) * HEIGHT;
        pos[i * 3] = -SPAN / 2 + x * step;
        pos[i * 3 + 1] = surface + 0.02;
        pos[i * 3 + 2] = -SPAN / 2 + y * step;

        // deep water is darker; fast water turns to white foam
        const speed = Math.hypot(this.hydro.velX[i], this.hydro.velY[i]);
        const foam = Math.min(1, speed * 0.16);
        const depth = Math.min(1, w * 3.2);
        col[i * 4]     = 0.10 + foam * 0.85 - depth * 0.08;
        col[i * 4 + 1] = 0.36 + foam * 0.6 - depth * 0.22;
        col[i * 4 + 2] = 0.55 + foam * 0.45 - depth * 0.25;
        // dry cells are fully transparent, so no water sheet over dry land
        col[i * 4 + 3] = w < 0.004 ? 0 : Math.min(0.9, 0.32 + depth * 0.55 + foam * 0.3);
      }
    }

    const idx = this.buildIndices(GRID);
    const vd = new VertexData();
    vd.positions = pos as unknown as number[];
    vd.indices = idx;
    vd.colors = col as unknown as number[];
    VertexData.ComputeNormals(pos, idx, nrm);
    vd.normals = nrm as unknown as number[];
    vd.applyToMesh(this.sea, true);
    this.sea.useVertexColors = true;
    this.sea.hasVertexAlpha = true;
    this.sea.isVisible = this.p.showWater > 0.5;
  }

  private indexCache: number[] | null = null;
  private buildIndices(n: number): number[] {
    if (this.indexCache) return this.indexCache;
    const idx: number[] = [];
    for (let y = 0; y < n - 1; y++) {
      for (let x = 0; x < n - 1; x++) {
        const a = y * n + x, b = a + 1, c = a + n, d = c + 1;
        idx.push(a, c, b, b, c, d);
      }
    }
    this.indexCache = idx;
    return idx;
  }

  /* -------------------------------- painting -------------------------------- */

  private installPointer(): void {
    const scene = this.ctx.scene;
    this.pointerObs = scene.onPointerObservable.add((pi) => {
      const ev = pi.event as PointerEvent;
      if (pi.type === PointerEventTypes.POINTERDOWN && ev.button === 0 && ev.shiftKey) {
        this.painting = true;
        this.paintAt(ev);
      } else if (pi.type === PointerEventTypes.POINTERMOVE && this.painting) {
        this.paintAt(ev);
      } else if (pi.type === PointerEventTypes.POINTERUP) {
        this.painting = false;
      }
    });
  }

  /** Maps the pointer ray onto the grid and applies the active tool. */
  private paintAt(_ev: PointerEvent): void {
    const scene = this.ctx.scene;
    const pick = scene.pick(scene.pointerX, scene.pointerY,
      (m) => m === this.land || m === this.sea);
    if (!pick?.hit || !pick.pickedPoint) return;
    const gx = Math.round(((pick.pickedPoint.x + SPAN / 2) / SPAN) * (GRID - 1));
    const gy = Math.round(((pick.pickedPoint.z + SPAN / 2) / SPAN) * (GRID - 1));
    this.applyTool(gx, gy);
  }

  applyTool(gx: number, gy: number): void {
    const r = this.p.brushSize;
    const s = this.p.brushStrength;
    switch (this.tool) {
      case 'raise':  this.hydro.deform(gx, gy, r, 0.035 * s); break;
      case 'lower':  this.hydro.deform(gx, gy, r, -0.035 * s); break;
      case 'dig':    this.hydro.deform(gx, gy, r, -0.09 * s); break;
      case 'water':  this.hydro.addWater(gx, gy, r, 0.05 * s); break;
      case 'drain':  this.drain(gx, gy, r); break;
      case 'smooth': this.smooth(gx, gy, r, 0.35 * s); break;
    }
  }

  private drain(cx: number, cy: number, radius: number): void {
    const n = GRID;
    for (let y = Math.max(0, cy - radius); y < Math.min(n, cy + radius + 1); y++) {
      for (let x = Math.max(0, cx - radius); x < Math.min(n, cx + radius + 1); x++) {
        if (Math.hypot(x - cx, y - cy) > radius) continue;
        this.hydro.water[y * n + x] *= 0.72;
      }
    }
  }

  /** Box-average the terrain under the brush — erases jagged artefacts. */
  private smooth(cx: number, cy: number, radius: number, amount: number): void {
    const n = GRID;
    const t = this.hydro.terrain;
    const copy = new Float32Array(t);
    for (let y = Math.max(1, cy - radius); y < Math.min(n - 1, cy + radius + 1); y++) {
      for (let x = Math.max(1, cx - radius); x < Math.min(n - 1, cx + radius + 1); x++) {
        const d = Math.hypot(x - cx, y - cy);
        if (d > radius) continue;
        const i = y * n + x;
        const avg = (copy[i - 1] + copy[i + 1] + copy[i - n] + copy[i + n] +
                     copy[i - n - 1] + copy[i - n + 1] + copy[i + n - 1] + copy[i + n + 1]) / 8;
        const w = amount * (1 - d / radius);
        t[i] = t[i] * (1 - w) + avg * w;
      }
    }
  }

  /* --------------------------------- update --------------------------------- */

  update(dt: number, _ctx: WorldContext): void {
    this.t += dt;

    this.disasters.update(Math.min(dt, 0.05) * this.p.flowSpeed);

    // disasters can demand extra rain or evaporation on top of the sliders
    this.hydro.opts.rain = this.p.rain * 0.02 + this.disasters.climateRain * 0.02;
    this.hydro.opts.evaporation =
      this.p.evaporation * 0.01 + this.disasters.climateEvaporation * 0.01;
    this.hydro.opts.erosion = this.p.erosion;
    this.hydro.opts.deposition = this.p.erosion * 0.7;

    // fixed-step the solver so behaviour does not depend on framerate
    const fixed = 1 / 120;
    this.accum += Math.min(dt, 0.05) * this.p.flowSpeed;
    let steps = 0;
    while (this.accum >= fixed && steps < 8) {
      this.hydro.step(fixed);
      this.accum -= fixed;
      steps++;
    }

    this.rebuildLand();
    this.rebuildWater();
  }

  /* ------------------------------- UI surface ------------------------------- */

  getParams(): WorldParam[] {
    return [
      { key: 'rain', label: 'Rainfall', min: 0, max: 3, step: 0.05, value: this.p.rain },
      { key: 'evaporation', label: 'Evaporation', min: 0, max: 3, step: 0.05, value: this.p.evaporation },
      { key: 'erosion', label: 'Erosion', min: 0, max: 1.5, step: 0.02, value: this.p.erosion },
      { key: 'flowSpeed', label: 'Flow Speed', min: 0, max: 3, step: 0.05, value: this.p.flowSpeed, unit: '×' },
      { key: 'seaLevel', label: 'Sea Level', min: 0, max: 1, step: 0.01, value: this.p.seaLevel },
      { key: 'severity', label: 'Disaster Severity', min: 0.2, max: 3, step: 0.1, value: this.p.severity, unit: '×' },
      { key: 'brushSize', label: 'Brush Size', min: 2, max: 30, step: 1, value: this.p.brushSize },
      { key: 'brushStrength', label: 'Brush Strength', min: 0.1, max: 3, step: 0.1, value: this.p.brushStrength },
      { key: 'showWater', label: 'Show Water', min: 0, max: 1, step: 1, value: this.p.showWater }
    ];
  }

  setParam(key: string, value: number): void {
    (this.p as Record<string, number>)[key] = value;
    if (key === 'seaLevel') this.hydro.setSeaLevel(value);
  }

  getActions(): WorldAction[] {
    return [
      { key: 'tool:raise', label: 'Raise Land', glyph: '⛰' },
      { key: 'tool:lower', label: 'Lower Land', glyph: '🕳' },
      { key: 'tool:smooth', label: 'Smooth', glyph: '🫓' },
      { key: 'tool:dig', label: 'Dig Canyon', glyph: '⛏' },
      { key: 'tool:water', label: 'Pour Water', glyph: '💧' },
      { key: 'tool:drain', label: 'Drain Water', glyph: '🌵' },
      { key: 'river', label: 'Start a River', glyph: '🏞' },
      ...DISASTER_ORDER.map((k) => ({
        key: 'dis:' + k,
        label: DISASTERS[k].name,
        glyph: DISASTERS[k].glyph
      })),
      { key: 'apocalypse', label: 'APOCALYPSE', glyph: '☠' },
      { key: 'calm', label: 'Stop All Disasters', glyph: '🕊' },
      { key: 'regen', label: 'New Landscape', glyph: '🎲' },
      { key: 'flatten', label: 'Flatten All', glyph: '🧹' }
    ];
  }

  runAction(key: string, _ctx: WorldContext): void {
    if (key.startsWith('tool:')) {
      this.tool = key.slice(5) as Tool;
      return;
    }
    const n = GRID;
    const rnd = (a: number, b: number) => a + Math.random() * (b - a);

    if (key.startsWith('dis:')) {
      const kind = key.slice(4) as DisasterKind;
      // aim at the wettest area for water disasters, otherwise anywhere
      const cx = Math.floor(rnd(18, n - 18));
      const cy = Math.floor(rnd(18, n - 18));
      this.disasters.trigger(kind, cx, cy, this.p.severity, 16);
      return;
    }
    if (key === 'apocalypse') {
      for (const k of DISASTER_ORDER) {
        this.disasters.trigger(k, Math.floor(rnd(15, n - 15)), Math.floor(rnd(15, n - 15)),
          this.p.severity, 14);
      }
      return;
    }
    if (key === 'calm') {
      this.disasters.clear();
      return;
    }

    if (key === 'river') {
      // spring on the highest ground: it will carve its own path downhill
      let best = -Infinity, bx = 0, by = 0;
      for (let y = 8; y < n - 8; y++) {
        for (let x = 8; x < n - 8; x++) {
          const h = this.hydro.terrain[y * n + x];
          if (h > best) { best = h; bx = x; by = y; }
        }
      }
      this.hydro.addWater(bx, by, 5, 1.4);
    } else if (key === 'tsunami') {
      const edges = ['n', 's', 'e', 'w'] as const;
      this.hydro.tsunami(0.55, 10, edges[Math.floor(Math.random() * 4)]);
    } else if (key === 'flood') {
      this.hydro.setSeaLevel(Math.min(1, this.p.seaLevel + 0.22));
      this.p.seaLevel = Math.min(1, this.p.seaLevel + 0.22);
    } else if (key === 'meteor') {
      const cx = Math.floor(rnd(20, n - 20)), cy = Math.floor(rnd(20, n - 20));
      this.hydro.crater(cx, cy, rnd(10, 22), rnd(0.25, 0.6));
      this.hydro.addWater(cx, cy, 14, 0.2);     // displaced water splashes out
      this.meteors++;
    } else if (key === 'volcano') {
      const cx = Math.floor(rnd(25, n - 25)), cy = Math.floor(rnd(25, n - 25));
      this.hydro.deform(cx, cy, 22, 0.85);
      this.hydro.deform(cx, cy, 5, -0.35);      // caldera
    } else if (key === 'drought') {
      for (let i = 0; i < this.hydro.water.length; i++) this.hydro.water[i] *= 0.35;
    } else if (key === 'regen') {
      this.seed = Math.random() * 1000;
      this.disasters = new DisasterSystem(this.hydro);

    this.generateLandscape();
      this.hydro.sediment.fill(0);
    } else if (key === 'flatten') {
      this.hydro.terrain.fill(0.2);
      this.hydro.water.fill(0);
      this.hydro.sediment.fill(0);
    }
  }

  getStats(): Record<string, string> {
    const cov = this.hydro.coverage() * 100;
    return {
      'Grid': GRID + '×' + GRID,
      'Cells': String(GRID * GRID),
      'Water coverage': cov.toFixed(1) + '%',
      'Deepest point': this.hydro.maxDepth().toFixed(2),
      'Total water': this.hydro.totalWater().toFixed(0),
      'Active tool': this.tool,
      'Meteor impacts': String(this.meteors),
      'Active disasters': String(this.disasters?.count() ?? 0),
      'Disasters caused': String(this.disasters?.triggered ?? 0),
      'Hint': 'Shift+drag to paint'
    };
  }

  /**
   * Ground height probe used by walk mode, so you can land on this planet and
   * walk over the terrain the solver is actively eroding.
   */
  sampleGround(x: number, z: number): { height: number; normal: Vector3 } | null {
    const gx = ((x + SPAN / 2) / SPAN) * (GRID - 1);
    const gz = ((z + SPAN / 2) / SPAN) * (GRID - 1);
    if (gx < 0 || gz < 0 || gx > GRID - 1 || gz > GRID - 1) return null;

    // bilinear sample so walking is smooth rather than stepped
    const x0 = Math.floor(gx), z0 = Math.floor(gz);
    const x1 = Math.min(GRID - 1, x0 + 1), z1 = Math.min(GRID - 1, z0 + 1);
    const fx = gx - x0, fz = gz - z0;
    const t = this.hydro.terrain;
    const h00 = t[z0 * GRID + x0], h10 = t[z0 * GRID + x1];
    const h01 = t[z1 * GRID + x0], h11 = t[z1 * GRID + x1];
    const height = (h00 * (1 - fx) + h10 * fx) * (1 - fz)
                 + (h01 * (1 - fx) + h11 * fx) * fz;

    return { height: height * HEIGHT, normal: new Vector3(0, 1, 0) };
  }

  /* ------------------------------ state capture ------------------------------ */

  captureState(): unknown {
    return {
      p: { ...this.p },
      terrain: Array.from(this.hydro.terrain),
      water: Array.from(this.hydro.water)
    };
  }

  restoreState(state: any): void {
    if (!state) return;
    Object.assign(this.p, state.p ?? {});
    if (state.terrain) this.hydro.terrain.set(state.terrain);
    if (state.water) this.hydro.water.set(state.water);
    this.hydro.sediment.fill(0);
  }

  dispose(): void {
    if (this.pointerObs) this.ctx.scene.onPointerObservable.remove(this.pointerObs);
    this.pointerObs = null;
    this.land?.material?.dispose();
    this.sea?.material?.dispose();
    this.land?.dispose();
    this.sea?.dispose();
  }
}
