/**
 * DimensionWorld — the place you arrive after falling through a black hole.
 *
 * Geometry, palette, fog, gravity and time direction all come from a
 * DimensionSpec, so this one world renders an unlimited number of distinct
 * realities without any per-dimension special cases. Falling through a tear
 * inside a dimension descends further (and eventually backwards through
 * time); tearing sideways moves to a different reality at the same depth.
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { Mesh } from '@babylonjs/core/Meshes/mesh';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { HemisphericLight } from '@babylonjs/core/Lights/hemisphericLight';
import { PointLight } from '@babylonjs/core/Lights/pointLight';
import { Scene } from '@babylonjs/core/scene';
import {
  generateDimension, descend, tearSideways, describeDimension,
  makeRng, type DimensionSpec
} from '../systems/DimensionSystem';
import type { World, WorldContext, WorldParam, WorldAction } from '../World';

interface Drifter {
  mesh: Mesh;
  spin: Vector3;
  orbit: number;
  orbitSpeed: number;
  bobAmp: number;
  bobPhase: number;
  base: Vector3;
}

export class DimensionWorld implements World {
  id = 'dimension';
  name = 'Dimension';

  private ctx!: WorldContext;
  private spec!: DimensionSpec;
  private drifters: Drifter[] = [];
  private lights: (HemisphericLight | PointLight)[] = [];
  private tears: Mesh[] = [];
  private t = 0;
  private history: DimensionSpec[] = [];

  private p = {
    gravity: 1.0,
    timeScale: 1.0,
    density: 1.0,
    scale: 1.0,
    weirdness: 1.0,
    fog: 1.0,
    spinRate: 1.0,
    driftRate: 1.0
  };

  /** Seed for the very first dimension; changes on every fresh entry. */
  entrySeed = (Math.random() * 0xffffffff) >>> 0;
  entryDepth = 0;

  async build(ctx: WorldContext): Promise<void> {
    this.ctx = ctx;
    this.spec = generateDimension(this.entrySeed, this.entryDepth);
    this.history = [this.spec];
    this.name = this.spec.glyph + ' ' + this.spec.name;
    this.realise();
    ctx.setCameraTarget(Vector3.Zero(), 70);
  }

  /* ---------------------------- building a reality ---------------------------- */

  private clearWorld(): void {
    this.drifters.forEach((d) => { d.mesh.material?.dispose(); d.mesh.dispose(); });
    this.drifters = [];
    this.tears.forEach((m) => { m.material?.dispose(); m.dispose(); });
    this.tears = [];
    this.lights.forEach((l) => l.dispose());
    this.lights = [];
  }

  /** Instantiates the current spec: fog, lights, geometry, exits. */
  private realise(): void {
    const scene = this.ctx.scene;
    const s = this.spec;
    const rng = makeRng(s.seed ^ 0x5f3759df);

    this.clearWorld();

    // ---- atmosphere. Never black: always tinted by the palette. ----
    const fc = s.fogColor;
    scene.fogMode = Scene.FOGMODE_EXP2;
    scene.fogDensity = s.fogDensity * this.p.fog;
    scene.fogColor = new Color3(fc[0], fc[1], fc[2]);
    scene.clearColor = new Color4(
      Math.max(0.02, fc[0] * 0.5), Math.max(0.02, fc[1] * 0.5),
      Math.max(0.03, fc[2] * 0.5), 1);

    // ---- lighting from the palette, so every dimension reads differently ----
    const hemi = new HemisphericLight('dimHemi', new Vector3(0.1, 1, 0.2), scene);
    hemi.intensity = s.ambient;
    const pa = s.palette[0], pb = s.palette[1];
    hemi.diffuse = new Color3(pa[0], pa[1], pa[2]);
    hemi.groundColor = new Color3(pb[0] * 0.5, pb[1] * 0.5, pb[2] * 0.5);
    this.lights.push(hemi);

    for (let i = 0; i < 3; i++) {
      const c = s.palette[(i + 1) % s.palette.length];
      const l = new PointLight('dimPt' + i,
        new Vector3((rng() - 0.5) * 120, (rng() - 0.5) * 80, (rng() - 0.5) * 120), scene);
      l.diffuse = new Color3(c[0], c[1], c[2]);
      l.intensity = 0.6 + rng() * 0.8;
      l.range = 260;
      this.lights.push(l);
    }

    // ---- the inhabitants ----
    const count = Math.min(420, Math.floor(s.objectCount * this.p.density));
    for (let i = 0; i < count; i++) {
      this.drifters.push(this.makeDrifter(rng, i));
    }

    // ---- exits: tears you can fall through ----
    for (let i = 0; i < 3; i++) this.tears.push(this.makeTear(rng, i));

    this.name = s.glyph + ' ' + s.name;
  }

  /** Builds one inhabitant using the dimension's shape vocabulary. */
  private makeDrifter(rng: () => number, i: number): Drifter {
    const s = this.spec;
    const scene = this.ctx.scene;
    const shape = s.shapes[Math.floor(rng() * s.shapes.length) % s.shapes.length];
    const sz = (0.7 + rng() * 2.6) * s.objectScale * this.p.scale;

    let mesh: Mesh;
    switch (shape) {
      case 'box':
        mesh = MeshBuilder.CreateBox('d', { size: sz }, scene); break;
      case 'torus':
        mesh = MeshBuilder.CreateTorus('d', { diameter: sz * 2, thickness: sz * 0.4, tessellation: 20 }, scene); break;
      case 'torusknot':
        mesh = MeshBuilder.CreateTorusKnot('d', { radius: sz, tube: sz * 0.28, radialSegments: 48, tubularSegments: 12 }, scene); break;
      case 'capsule':
        mesh = MeshBuilder.CreateCapsule('d', { radius: sz * 0.5, height: sz * 2.4 }, scene); break;
      case 'cylinder':
        mesh = MeshBuilder.CreateCylinder('d', { diameter: sz, height: sz * 2 }, scene); break;
      case 'tube':
        mesh = MeshBuilder.CreateCylinder('d', { diameterTop: sz * 0.3, diameterBottom: sz * 0.9, height: sz * 3, tessellation: 12 }, scene); break;
      case 'plane':
        mesh = MeshBuilder.CreatePlane('d', { size: sz * 2.2 }, scene); break;
      case 'triangle':
        mesh = MeshBuilder.CreateDisc('d', { radius: sz, tessellation: 3 }, scene); break;
      case 'disc':
        mesh = MeshBuilder.CreateDisc('d', { radius: sz, tessellation: 24 }, scene); break;
      case 'dome':
        mesh = MeshBuilder.CreateSphere('d', { diameter: sz * 2, segments: 14, slice: 0.55 }, scene); break;
      case 'octahedron':
        mesh = MeshBuilder.CreatePolyhedron('d', { type: 1, size: sz }, scene); break;
      case 'icosphere':
        mesh = MeshBuilder.CreateIcoSphere('d', { radius: sz, subdivisions: 2 }, scene); break;
      case 'gear': {
        const hub = MeshBuilder.CreateCylinder('h', { diameter: sz * 1.5, height: sz * 0.4, tessellation: 12 }, scene);
        const teeth: Mesh[] = [hub];
        for (let k = 0; k < 8; k++) {
          const tth = MeshBuilder.CreateBox('t', { width: sz * 0.3, height: sz * 0.42, depth: sz * 0.5 }, scene);
          const a = (k / 8) * Math.PI * 2;
          tth.position.set(Math.cos(a) * sz * 0.85, 0, Math.sin(a) * sz * 0.85);
          tth.rotation.y = -a;
          teeth.push(tth);
        }
        mesh = Mesh.MergeMeshes(teeth, true, true, undefined, false, false) ?? hub;
        break;
      }
      case 'blob':
      default: {
        mesh = MeshBuilder.CreateSphere('d', { diameter: sz * 2, segments: 12 }, scene);
        if (shape === 'blob') {
          // push vertices around so no two blobs are the same
          const pos = mesh.getVerticesData('position');
          if (pos) {
            for (let k = 0; k < pos.length; k += 3) {
              const j = 0.82 + rng() * 0.42;
              pos[k] *= j; pos[k + 1] *= j; pos[k + 2] *= j;
            }
            mesh.updateVerticesData('position', pos);
            mesh.createNormals(true);
          }
        }
        break;
      }
    }

    // ---- material from the palette; emissive keeps it from ever going black ----
    const c = this.spec.palette[i % this.spec.palette.length];
    const mat = new StandardMaterial('dm', scene);
    const w = this.spec.weirdness * this.p.weirdness;
    mat.diffuseColor = new Color3(c[0], c[1], c[2]);
    mat.emissiveColor = new Color3(c[0] * 0.35 * (0.4 + w), c[1] * 0.35 * (0.4 + w), c[2] * 0.35 * (0.4 + w));
    mat.specularColor = new Color3(0.6, 0.6, 0.7);
    mat.specularPower = 48;
    if (this.spec.traits.includes('jellyfish') || this.spec.traits.includes('liquid')) {
      mat.alpha = 0.45 + rng() * 0.35;
    }
    if (this.spec.traits.includes('paper')) {
      mat.specularColor = Color3.Black();
      mat.backFaceCulling = false;
    }
    if (this.spec.traits.includes('monochrome')) {
      const g = (c[0] + c[1] + c[2]) / 3;
      mat.diffuseColor = new Color3(g, g, g);
    }
    mesh.material = mat;

    // ---- placement ----
    const r = 18 + rng() * 130;
    const theta = rng() * Math.PI * 2;
    const phi = Math.acos(2 * rng() - 1);
    let pos = new Vector3(
      r * Math.sin(phi) * Math.cos(theta),
      r * Math.cos(phi) * 0.55,
      r * Math.sin(phi) * Math.sin(theta));
    if (this.spec.traits.includes('upside-down')) pos.y = -Math.abs(pos.y);
    mesh.position.copyFrom(pos);
    mesh.rotation.set(rng() * 6.28, rng() * 6.28, rng() * 6.28);
    if (this.spec.traits.includes('upside-down')) mesh.rotation.z += Math.PI;
    mesh.isPickable = false;

    return {
      mesh,
      spin: new Vector3((rng() - 0.5) * 1.2, (rng() - 0.5) * 1.2, (rng() - 0.5) * 1.2),
      orbit: theta,
      orbitSpeed: (rng() - 0.5) * 0.22,
      bobAmp: rng() * 4,
      bobPhase: rng() * 6.28,
      base: pos.clone()
    };
  }

  /** A rip in reality; entering one moves you to another dimension. */
  private makeTear(rng: () => number, i: number): Mesh {
    const scene = this.ctx.scene;
    const c = this.spec.palette[(i + 2) % this.spec.palette.length];
    const m = MeshBuilder.CreateTorus('tear',
      { diameter: 16, thickness: 1.1, tessellation: 48 }, scene);
    const mat = new StandardMaterial('tearMat', scene);
    mat.emissiveColor = new Color3(
      Math.min(1, c[0] + 0.35), Math.min(1, c[1] + 0.35), Math.min(1, c[2] + 0.35));
    mat.diffuseColor = Color3.Black();
    mat.specularColor = Color3.Black();
    mat.disableLighting = true;
    m.material = mat;
    const a = (i / 3) * Math.PI * 2 + rng();
    m.position.set(Math.cos(a) * 95, (rng() - 0.5) * 30, Math.sin(a) * 95);
    m.rotation.set(rng() * 3, a, rng() * 3);
    m.isPickable = false;
    return m;
  }

  /* --------------------------------- travel --------------------------------- */

  /** Falls deeper: a new dimension one level down, and further back in time. */
  goDeeper(): void {
    this.spec = descend(this.spec);
    this.history.push(this.spec);
    this.realise();
  }

  /** Rips sideways into a different reality at the same depth. */
  tear(): void {
    this.spec = tearSideways(this.spec);
    this.history.push(this.spec);
    this.realise();
  }

  /** Returns to the previous dimension, if there is one. */
  goBack(): boolean {
    if (this.history.length < 2) return false;
    this.history.pop();
    this.spec = this.history[this.history.length - 1];
    this.realise();
    return true;
  }

  /** Jumps to a specific seed, so a dimension can be shared or revisited. */
  jumpTo(seed: number, depth: number): void {
    this.spec = generateDimension(seed >>> 0, depth);
    this.history.push(this.spec);
    this.realise();
  }

  currentSpec(): DimensionSpec {
    return this.spec;
  }

  /* --------------------------------- update --------------------------------- */

  update(dt: number, _ctx: WorldContext): void {
    const s = this.spec;
    // time direction is part of the dimension, not a UI toggle
    const flow = dt * s.timeScale * this.p.timeScale * s.timeDirection;
    this.t += flow;

    const g = s.gravity * this.p.gravity;
    const drift = this.p.driftRate;
    const spin = this.p.spinRate;

    for (const d of this.drifters) {
      d.mesh.rotation.x += d.spin.x * flow * spin;
      d.mesh.rotation.y += d.spin.y * flow * spin;
      d.mesh.rotation.z += d.spin.z * flow * spin;

      // slow orbital drift around the centre
      d.orbit += d.orbitSpeed * flow * 0.12 * drift;
      const r = Math.hypot(d.base.x, d.base.z);
      d.mesh.position.x = Math.cos(d.orbit) * r;
      d.mesh.position.z = Math.sin(d.orbit) * r;

      // gravity here can be inverted, so things fall upward
      const bob = Math.sin(this.t * 0.7 + d.bobPhase) * d.bobAmp;
      d.mesh.position.y = d.base.y + bob - g * Math.sin(this.t * 0.25 + d.bobPhase) * 3;
    }

    for (let i = 0; i < this.tears.length; i++) {
      const m = this.tears[i];
      m.rotation.z += flow * 0.6;
      m.rotation.x += flow * 0.25;
      const pulse = 1 + Math.sin(this.t * 2 + i) * 0.08;
      m.scaling.setAll(pulse);
    }
  }

  /* ------------------------------- UI surface ------------------------------- */

  getParams(): WorldParam[] {
    return [
      { key: 'gravity', label: 'Gravity', min: -3, max: 3, step: 0.05, value: this.p.gravity, unit: '×' },
      { key: 'timeScale', label: 'Time Flow', min: 0, max: 4, step: 0.05, value: this.p.timeScale, unit: '×' },
      { key: 'density', label: 'Object Density', min: 0.1, max: 3, step: 0.1, value: this.p.density, unit: '×' },
      { key: 'scale', label: 'Object Scale', min: 0.1, max: 5, step: 0.1, value: this.p.scale, unit: '×' },
      { key: 'weirdness', label: 'Weirdness', min: 0, max: 2, step: 0.05, value: this.p.weirdness, unit: '×' },
      { key: 'fog', label: 'Fog Density', min: 0, max: 4, step: 0.05, value: this.p.fog, unit: '×' },
      { key: 'spinRate', label: 'Spin Rate', min: 0, max: 4, step: 0.05, value: this.p.spinRate, unit: '×' },
      { key: 'driftRate', label: 'Drift Rate', min: 0, max: 4, step: 0.05, value: this.p.driftRate, unit: '×' }
    ];
  }

  setParam(key: string, value: number): void {
    (this.p as Record<string, number>)[key] = value;
    // these change the world's contents, so rebuild
    if (key === 'density' || key === 'scale') this.realise();
    if (key === 'fog') this.ctx.scene.fogDensity = this.spec.fogDensity * value;
  }

  getActions(): WorldAction[] {
    return [
      { key: 'deeper', label: 'Fall Deeper', glyph: '🕳' },
      { key: 'tear', label: 'Tear Reality', glyph: '⚡' },
      { key: 'back', label: 'Go Back', glyph: '↩' },
      { key: 'reroll', label: 'New Dimension', glyph: '🎲' },
      { key: 'surface', label: 'Return to Depth 0', glyph: '⬆' },
      { key: 'plunge', label: 'Plunge 5 Levels', glyph: '⏬' },
      { key: 'ancient', label: 'Go Back In Time', glyph: '⏳' }
    ];
  }

  runAction(key: string, _ctx: WorldContext): void {
    if (key === 'deeper') this.goDeeper();
    else if (key === 'tear') this.tear();
    else if (key === 'back') this.goBack();
    else if (key === 'reroll') {
      this.spec = generateDimension((Math.random() * 0xffffffff) >>> 0, this.spec.depth);
      this.history.push(this.spec);
      this.realise();
    } else if (key === 'surface') {
      this.spec = generateDimension(this.spec.seed, 0);
      this.history.push(this.spec);
      this.realise();
    } else if (key === 'plunge') {
      for (let i = 0; i < 5; i++) this.spec = descend(this.spec);
      this.history.push(this.spec);
      this.realise();
    } else if (key === 'ancient') {
      // dive until time runs backwards
      let guard = 0;
      do { this.spec = descend(this.spec); guard++; }
      while (this.spec.timeDirection > 0 && guard < 40);
      this.history.push(this.spec);
      this.realise();
    }
  }

  getStats(): Record<string, string> {
    return {
      ...describeDimension(this.spec),
      'Objects': String(this.drifters.length),
      'Exits': String(this.tears.length),
      'Visited': String(this.history.length)
    };
  }

  captureState(): unknown {
    return { p: { ...this.p }, seed: this.spec.seed, depth: this.spec.depth };
  }

  restoreState(state: any): void {
    if (!state) return;
    Object.assign(this.p, state.p ?? {});
    if (typeof state.seed === 'number') {
      this.spec = generateDimension(state.seed, state.depth ?? 0);
      this.realise();
    }
  }

  dispose(): void {
    this.clearWorld();
    const scene = this.ctx?.scene;
    if (scene) {
      scene.fogMode = Scene.FOGMODE_NONE;
      scene.fogDensity = 0;
    }
  }
}
