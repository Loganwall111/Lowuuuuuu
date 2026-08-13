/**
 * App — wires the Babylon 9 engine, the world registry and the UI shell.
 */

import { Scene } from '@babylonjs/core/scene';
import { ArcRotateCamera } from '@babylonjs/core/Cameras/arcRotateCamera';
import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color4 } from '@babylonjs/core/Maths/math.color';
import type { AbstractEngine } from '@babylonjs/core/Engines/abstractEngine';

import { createEngine } from './Engine';
import { Shell } from './ui/Shell';
import type { World, WorldContext } from './World';
import { PlanetaryWorld } from './worlds/PlanetaryWorld';
import { OceanWorld } from './worlds/OceanWorld';
import { BlackHoleWorld } from './worlds/BlackHoleWorld';

const FACTORY: Record<string, () => World> = {
  planetary: () => new PlanetaryWorld(),
  ocean: () => new OceanWorld(),
  blackhole: () => new BlackHoleWorld()
};

export class App {
  private engine!: AbstractEngine;
  private scene!: Scene;
  private camera!: ArcRotateCamera;
  private world: World | null = null;
  private shell!: Shell;
  private ctx!: WorldContext;
  private paused = false;
  private currentId = 'planetary';
  private switching = false;

  async init(): Promise<void> {
    const canvas = document.getElementById('renderCanvas') as HTMLCanvasElement;

    this.shell = new Shell({
      onWorld: (id) => this.loadWorld(id),
      onParam: (k, v) => this.world?.setParam(k, v),
      onAction: (k) => this.world?.runAction?.(k, this.ctx),
      onMode: () => {},
      onReset: () => this.loadWorld(this.currentId),
      onPause: (p) => { this.paused = p; }
    });

    this.shell.progress(12, 'starting graphics engine');
    const boot = await createEngine(canvas);
    this.engine = boot.engine;
    this.shell.setBackend(boot.backend);

    this.shell.progress(35, 'creating scene');
    this.scene = new Scene(this.engine);
    this.scene.clearColor = new Color4(0.004, 0.006, 0.014, 1);
    this.scene.skipPointerMovePicking = true;

    this.camera = new ArcRotateCamera('cam', -Math.PI / 2, 1.14, 60, Vector3.Zero(), this.scene);
    this.camera.attachControl(canvas, true);
    this.camera.minZ = 0.05;
    this.camera.maxZ = 4000;
    this.camera.lowerRadiusLimit = 3;
    this.camera.upperRadiusLimit = 800;
    this.camera.wheelDeltaPercentage = 0.02;
    this.camera.pinchDeltaPercentage = 0.02;
    this.camera.panningSensibility = 90;
    this.camera.inertia = 0.86;
    this.camera.angularSensibilityX = 900;
    this.camera.angularSensibilityY = 900;
    this.camera.useNaturalPinchZoom = true;

    this.ctx = {
      scene: this.scene,
      camera: this.camera,
      setCameraTarget: (t: Vector3, r: number) => {
        this.camera.setTarget(t.clone());
        this.camera.radius = r;
        this.camera.upperRadiusLimit = Math.max(r * 12, 400);
      }
    };

    this.shell.progress(58, 'compiling shaders');
    await this.loadWorld('planetary');

    this.shell.progress(88, 'warming pipeline');
    await new Promise((r) => setTimeout(r, 120));

    window.addEventListener('resize', () => this.engine.resize());

    this.shell.progress(100, 'ready');
    setTimeout(() => this.shell.hideBoot(), 260);
  }

  private async loadWorld(id: string): Promise<void> {
    if (this.switching) return;
    this.switching = true;
    try {
      this.world?.dispose();
      this.world = null;

      // Purge everything except the camera so worlds never leak into each other.
      [...this.scene.meshes].forEach((m) => m.dispose(false, true));
      [...this.scene.lights].forEach((l) => l.dispose());
      [...this.scene.materials].forEach((m) => m.dispose());
      [...this.scene.textures].forEach((t) => t.dispose());
      this.scene.customRenderTargets.length = 0;

      const make = FACTORY[id] ?? FACTORY.planetary;
      const w = make();
      await w.build(this.ctx);
      this.world = w;
      this.currentId = id;
      this.shell.setWorld(w);
    } finally {
      this.switching = false;
    }
  }

  start(): void {
    let last = performance.now();
    this.engine.runRenderLoop(() => {
      const now = performance.now();
      const dt = Math.min((now - last) / 1000, 0.1);
      last = now;

      if (this.world && !this.paused && !this.switching) {
        this.world.update(dt, this.ctx);
      }
      this.scene.render();
      this.shell.tickHud(this.engine.getFps(), this.world?.name ?? '–');
    });
  }
}
