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
import { SandboxWorld } from './worlds/SandboxWorld';
import { TerraformWorld } from './worlds/TerraformWorld';
import { PostFX } from './PostFX';
import { MainMenu } from './ui/MainMenu';
import { HistorySystem } from './systems/HistorySystem';
import { SaveSystem } from './systems/SaveSystem';
import { QualitySystem, QUALITY, type QualityName } from './systems/QualitySystem';

const FACTORY: Record<string, () => World> = {
  planetary: () => new PlanetaryWorld(),
  ocean: () => new OceanWorld(),
  blackhole: () => new BlackHoleWorld(),
  sandbox: () => new SandboxWorld(),
  terraform: () => new TerraformWorld()
};

export class App {
  private engine!: AbstractEngine;
  private scene!: Scene;
  private camera!: ArcRotateCamera;
  private world: World | null = null;
  shell!: Shell;
  private ctx!: WorldContext;
  private paused = false;
  private currentId = 'planetary';
  private switching = false;
  booted = false;
  private menu: MainMenu | null = null;
  private postfx = new PostFX();
  history = new HistorySystem<any>(40);
  saves = new SaveSystem();
  quality = new QualitySystem('high');

  async init(): Promise<void> {
    const canvas = document.getElementById('renderCanvas') as HTMLCanvasElement;

    this.shell = new Shell({
      onWorld: (id) => this.loadWorld(id),
      onParam: (k, v) => this.world?.setParam(k, v),
      onPostFX: (k, v) => this.postfx.set(k, v),
      onQuality: (name) => this.applyQuality(name as QualityName),
      onAdaptive: (on) => { this.quality.adaptive = on; },
      getQuality: () => ({
        current: this.quality.current,
        scaling: this.quality.scaling,
        adaptive: this.quality.adaptive
      }),
      onSaveGame: (name) => {
        const w = this.world as any;
        if (!w?.captureState) return null;
        return this.saves.save(name, w.id, w.captureState());
      },
      onLoadGame: async (id) => {
        const entry = this.saves.load(id);
        if (!entry) return false;
        if (entry.world !== this.world?.id) await this.loadWorld(entry.world);
        (this.world as any)?.restoreState?.(entry.data);
        return true;
      },
      listGames: () => this.saves.list(),
      onDeleteGame: (id) => this.saves.remove(id),
      onSpawn: (id, scale) => {
        this.history.push('spawn ' + id);
        (this.world as any)?.spawnObject?.(id, scale, this.ctx);
      },
      onUndo: () => this.history.undo(),
      onRedo: () => this.history.redo(),
      onSaveSnapshot: (label) => this.history.save(label),
      onLoadSnapshot: (id) => this.history.load(id),
      listSnapshots: () => this.history.list(),
      canUndo: () => this.history.canUndo(),
      canRedo: () => this.history.canRedo(),
      onAction: (k) => {
        this.history.push(k);
        this.world?.runAction?.(k, this.ctx);
      },
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
    this.booted = true;

    // AAA front-end. The sim renders live behind it, so there is never a
    // black screen, and picking an entry drops straight into the sandbox.
    this.menu = new MainMenu((choice) => {
      this.menu = null;
      if (choice.world !== this.currentId) this.loadWorld(choice.world);
      if (choice.preset === 'chaos') {
        setTimeout(() => this.world?.runAction?.('chaos', this.ctx), 400);
      } else if (choice.preset === 'weird') {
        setTimeout(() => this.world?.runAction?.('weird', this.ctx), 400);
      }
      this.shell.onMenuClosed();
    });
  }

  private async loadWorld(id: string): Promise<void> {
    if (this.switching) return;
    this.switching = true;
    try {
      this.postfx.detach();
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
      this.postfx.attach(this.scene, this.camera);
      this.history.attach(
        typeof (w as any).captureState === 'function' ? (w as any) : null);
      this.shell.setWorld(w);
    } finally {
      this.switching = false;
    }
  }

  /** Applies a quality preset to the engine and the post-processing stack. */
  applyQuality(name: QualityName): void {
    const p = this.quality.set(name);
    try {
      this.engine.setHardwareScalingLevel(p.scaling);
    } catch (e) {
      console.warn('hardware scaling rejected:', e);
    }
    // effects follow the preset; each set() is individually guarded in PostFX
    this.postfx.set('bloom', p.bloom ? 0.55 : 0);
    this.postfx.set('grain', p.grain ? 3.0 : 0);
    this.postfx.set('chromatic', p.chromatic ? 2.0 : 0);
    this.postfx.set('sharpen', p.sharpen ? 0.25 : 0);
    this.postfx.set('fxaa', p.fxaa ? 1 : 0);
    this.saves.setPrefs({ quality: name, adaptive: this.quality.adaptive });
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

      // adaptive resolution defends the framerate
      const newScale = this.quality.sample(dt);
      if (newScale !== null) {
        try { this.engine.setHardwareScalingLevel(newScale); } catch { /* ignore */ }
      }

      // autosave so a crash or refresh never loses the session
      this.saves.tick(dt, () => {
        const w = this.world as any;
        return w?.captureState ? { world: w.id, data: w.captureState() } : null;
      });

      this.shell.tickHud(this.engine.getFps(), this.world?.name ?? '–');
    });
  }
}
