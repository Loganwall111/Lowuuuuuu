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
import { DimensionWorld } from './worlds/DimensionWorld';
import { PostFX } from './PostFX';
import { MainMenu } from './ui/MainMenu';
import { WarpSystem } from './systems/WarpSystem';
import { PlanetSurfaceSystem } from './systems/PlanetSurfaceSystem';
import { MouseLook } from './systems/MouseLook';
import { HistorySystem } from './systems/HistorySystem';
import { SaveSystem } from './systems/SaveSystem';
import { QualitySystem, QUALITY, type QualityName } from './systems/QualitySystem';
import {
  VehicleController, SHIPS, inputFromKeys, emptyInput, type ControlMode
} from './systems/VehicleSystem';
import { UniverseState } from './systems/UniverseState';
import { GrabSystem, type Grabbable } from './systems/GrabSystem';
import {
  LENS_PROFILES, cloneProfile, randomAlienProfile,
  describeProfile as describeLens, sanitizeProfile as sanitizeLens,
  type LensMode
} from './systems/LensProfiles';
import type { Region } from './systems/UniverseState';

const FACTORY: Record<string, () => World> = {
  planetary: () => new PlanetaryWorld(),
  ocean: () => new OceanWorld(),
  blackhole: () => new BlackHoleWorld(),
  sandbox: () => new SandboxWorld(),
  terraform: () => new TerraformWorld(),
  dimension: () => new DimensionWorld()
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
  vehicle = new VehicleController();
  /** The single continuous universe. Everything lives here at once. */
  universe = new UniverseState();
  grab = new GrabSystem();
  /** Last position outside any horizon, so we know which way is "back". */
  private lastOutsidePos = new Vector3(0, 0, -220);
  /** Mouse look + wheel throttle, the other half of free flight. */
  mouse = new MouseLook();
  /** Every planet's own terrain, water, weather and life. */
  surfaces = new PlanetSurfaceSystem();
  /** Streaking starfield when the throttle is wound up. */
  private warp!: WarpSystem;
  /** Previous eye position, for measuring real travelled speed. */
  private prevEye = new Vector3(0, 0, 0);
  private shownSpeed = 0;
  private keys = new Set<string>();

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
      onControlMode: (m) => this.setControlMode(m as ControlMode),

      // ---- one continuous universe ----
      getUniverse: () => {
        const eye = this.vehicle.mode === 'orbit'
          ? this.camera.position : this.vehicle.position;
        const cur = this.universe.current;
        const bh = this.universe.insideHorizon
          ?? (cur?.kind === 'blackhole' ? cur : null);
        return {
          stats: { ...this.universe.stats(), ...this.grab.stats(), ...this.surfaces.stats(), ...this.warp.stats(), ...this.mouse.stats() },
          current: cur
            ? { id: cur.id, name: cur.name, glyph: cur.glyph, kind: cur.kind }
            : null,
          regions: this.universe.activeRegions(eye, 16).map((r) => ({
            id: r.id, name: r.name, glyph: r.glyph, kind: r.kind,
            distance: Vector3.Distance(eye, r.position)
          })),
          holding: this.grab.held ? this.grab.held.name : null,
          lens: bh?.lens ? describeLens(bh.lens) : null
        };
      },

      onWarpTo: (id) => this.warpTo(id),

      onGrab: () => {
        const dir = this.camera.getTarget().subtract(this.camera.position);
        const candidates: Grabbable[] = this.universe.regions.map((r) => ({
          id: r.id, name: r.name, position: r.position, radius: r.radius
        }));
        const got = this.grab.grab(this.camera.position, dir, candidates);
        this.shell.toast(got ? 'Holding ' + got.name : 'Nothing under the cursor');
      },

      onRelease: (thrown) => {
        const r = thrown ? this.grab.throwIt() : this.grab.release();
        if (r) this.shell.toast((thrown ? 'Threw ' : 'Released ') + r.name);
      },

      onSpawnRegion: (kind) => {
        // place it in front of the camera, at a sensible distance
        const dir = this.camera.getTarget().subtract(this.camera.position).normalize();
        const at = this.camera.position.add(dir.scale(400));
        const r = kind === 'blackhole'
          ? this.universe.spawnBlackHole(at)
          : this.universe.spawnStarSystem(at);
        this.shell.toast('Created ' + r.glyph + ' ' + r.name);
        this.shell.refreshAll?.();
      },

      onDeleteRegion: (id) => {
        const r = this.universe.byId(id);
        if (r && this.universe.removeRegion(id)) {
          this.shell.toast('Removed ' + r.name);
          this.shell.refreshAll?.();
        }
      },

      onLensMode: (mode) => {
        const bh = this.universe.insideHorizon ?? this.nearestHole();
        if (!bh) { this.shell.toast('No black hole nearby'); return; }
        bh.lens = mode === 'random'
          ? randomAlienProfile()
          : cloneProfile(LENS_PROFILES[mode as LensMode] ?? LENS_PROFILES.schwarzschild);
        this.applyLensToWorld(bh);
        this.shell.toast(bh.name + ': ' + bh.lens.name + ' lens');
      },

      onLensField: (key, value) => {
        const bh = this.universe.insideHorizon ?? this.nearestHole();
        if (!bh?.lens) return;
        (bh.lens as unknown as Record<string, number>)[key] = value;
        bh.lens = sanitizeLens(bh.lens);
        this.applyLensToWorld(bh);
      },

      onRandomLens: () => {
        const bh = this.universe.insideHorizon ?? this.nearestHole();
        if (!bh) { this.shell.toast('No black hole nearby'); return; }
        bh.lens = randomAlienProfile();
        this.applyLensToWorld(bh);
        this.shell.toast(bh.name + ': ' + bh.lens.name);
      },

      onEnterDimension: (seed, depth) => { void this.enterDimension(seed, depth); },
      onShip: (id) => this.vehicle.setShip(id),
      getVehicle: () => ({
        mode: this.vehicle.mode,
        ship: this.vehicle.ship.id,
        stats: this.vehicle.stats()
      }),
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

    this.warp = new WarpSystem(this.scene);

    this.camera = new ArcRotateCamera('cam', -Math.PI / 2, 1.14, 60, Vector3.Zero(), this.scene);
    this.camera.attachControl(canvas, true);
    // Free-fly detaches the arc camera, so the mouse must drive the vehicle
    // directly or there is no way to look around or zoom.
    this.mouse.attach(canvas as unknown as HTMLElement);
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
      },
      enterDimension: (seed: number, depth: number) => {
        void this.enterDimension(seed, depth);
      }
    };

    this.shell.progress(58, 'compiling shaders');
    await this.loadWorld('planetary');

    // Start in free flight inside the one continuous universe, rather than
    // parked in an orbit camera waiting for a menu choice.
    this.setControlMode('freefly');
    this.universe.updatePlayer(this.camera.position);

    this.shell.progress(88, 'warming pipeline');
    await new Promise((r) => setTimeout(r, 120));

    window.addEventListener('resize', () => this.engine.resize());

    // ---- vehicle input. Ignored while typing into a field. ----
    const typing = (e: KeyboardEvent) => {
      const t = e.target as HTMLElement | null;
      return !!t && (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA' || t.isContentEditable);
    };
    window.addEventListener('keydown', (e) => {
      if (typing(e)) return;
      this.keys.add(e.key.toLowerCase());
      // Space would otherwise scroll the page while flying
      if (e.key === ' ' && this.vehicle.mode !== 'orbit') e.preventDefault();
      // Pointer lock: look around without holding the button down.
      if (e.key.toLowerCase() === 'c') this.mouse.toggleLock();
    });
    window.addEventListener('keyup', (e) => this.keys.delete(e.key.toLowerCase()));
    window.addEventListener('blur', () => this.keys.clear());

    this.shell.progress(100, 'ready');
    setTimeout(() => this.shell.hideBoot(), 260);
    this.booted = true;

    // AAA front-end. The sim renders live behind it, so there is never a
    // black screen, and picking an entry drops straight into the sandbox.
    this.menu = new MainMenu((choice) => {
      this.menu = null;
      if (choice.world !== this.currentId) this.loadWorld(choice.world);

      // The menu offers actions now, not worlds. Honour whichever was picked
      // once the world has had a moment to build itself.
      const after = (fn: () => void) => setTimeout(fn, 400);
      switch (choice.action) {
        case 'new':
          // A genuinely different universe, not just a reloaded world.
          after(() => {
            const seed = this.universe.reseed();
            this.shell.toast?.(`New universe - seed ${seed}`);
            this.shell.refreshAll();
          });
          break;
        case 'customize':
          // Drop the player straight into the tweakables.
          after(() => {
            this.shell.wm.Open('presets');
            this.shell.wm.Open('graphics');
          });
          break;
        case 'settings':
          after(() => this.shell.wm.Open('graphics'));
          break;
        default:
          break;
      }

      if (choice.preset === 'chaos') {
        after(() => this.world?.runAction?.('chaos', this.ctx));
      } else if (choice.preset === 'weird') {
        after(() => this.world?.runAction?.('weird', this.ctx));
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

  /** The black hole the player is closest to, for lens editing. */
  private nearestHole(): Region | null {
    const eye = this.vehicle.mode === 'orbit'
      ? this.camera.position : this.vehicle.position;
    return this.universe.nearest(eye, 'blackhole');
  }

  /**
   * Pushes a region's lens into the live renderer, so edits are visible
   * immediately rather than on the next reload.
   */
  private applyLensToWorld(r: Region): void {
    const w = this.world as unknown as { lens?: unknown };
    if (w && r.lens && 'lens' in (this.world as object)) {
      w.lens = cloneProfile(r.lens);
    }
    this.shell.refreshAll?.();
  }

  /**
   * Flies the player to a place. This is navigation inside one universe -
   * it moves the camera, it does not load a level.
   */
  warpTo(id: string): void {
    const r = this.universe.byId(id);
    if (!r) return;
    // stand off by enough to see the whole thing
    const standoff = Math.max(r.radius * 1.35, (r.surfaceRadius ?? 10) * 4);
    const from = this.vehicle.mode === 'orbit'
      ? this.camera.position : this.vehicle.position;
    const dir = from.subtract(r.position);
    const n = dir.lengthSquared() > 1e-6
      ? dir.normalize() : new Vector3(0, 0.25, -1).normalize();
    const dest = r.position.add(n.scale(standoff));

    this.vehicle.teleport(dest);
    this.camera.position.copyFrom(dest);
    this.camera.setTarget(r.position.clone());
    this.universe.updatePlayer(dest);
    this.shell.toast('Arrived at ' + r.glyph + ' ' + r.name);
    this.shell.refreshAll?.();
  }

  /**
   * Travels to a specific procedural dimension. Used when a player enters a
   * space tear or falls through a black hole, so the destination they saw
   * through the portal is the one they actually arrive in.
   */
  async enterDimension(seed: number, depth = 0): Promise<void> {
    await this.loadWorld('dimension');
    const w = this.world as any;
    if (w && typeof w.jumpTo === 'function') w.jumpTo(seed, depth);
    this.shell.refreshAll?.();
  }

  /**
   * Switches between orbiting, flying and walking. Detaching the arc camera
   * is essential: otherwise its own input handlers fight the vehicle.
   */
  setControlMode(m: ControlMode): void {
    this.vehicle.setMode(m);
    const canvas = this.engine.getRenderingCanvas();
    if (m === 'orbit') {
      this.camera.attachControl(canvas as HTMLCanvasElement, true);
    } else {
      this.camera.detachControl();
      // start the vehicle where the camera already is, so the view does not jump
      this.vehicle.teleport(this.camera.position.clone());
    }
    this.shell.setControlMode?.(m);
  }

  /** Ground height probe for walk mode; delegates to the world if it has one. */
  private groundProbe = (x: number, z: number) => {
    const w = this.world as any;
    if (typeof w?.sampleGround === 'function') {
      const g = w.sampleGround(x, z);
      if (g) return g;
    }
    return null;
  };

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

      // ---- player-controlled flight / walking ----
      if (this.vehicle.mode !== 'orbit') {
        // Free-fly speed scales with how far the nearest thing is, so the
        // same controls work for inspecting a rock and crossing a galaxy.
        if (this.vehicle.mode === 'freefly') {
          const near = this.universe.nearest(this.vehicle.position);
          if (near) {
            const d = Vector3.Distance(this.vehicle.position, near.position) - near.radius;
            // The wheel scales that baseline, so scrolling is a real throttle
            // rather than a dead control.
            this.vehicle.setScaleSpeed(d * this.mouse.throttleScale);
          }
        }
        // Keyboard supplies movement; the mouse supplies look and throttle.
        const input = inputFromKeys(this.keys);
        const look = this.mouse.consume(dt);
        // Arrow keys still work: whichever the player is using wins.
        if (Math.abs(look.yaw) > 1e-4) input.yaw = look.yaw;
        if (Math.abs(look.pitch) > 1e-4) input.pitch = look.pitch;
        this.vehicle.update(dt, input, this.groundProbe);
        this.camera.position.copyFrom(this.vehicle.position);
        this.camera.setTarget(this.vehicle.lookTarget());
      }

      // ---- one continuous universe: where am I, and what is near me ----
      const eye = this.vehicle.mode === 'orbit'
        ? this.camera.position
        : this.vehicle.position;
      const prevRegion = this.universe.current?.id ?? null;
      this.universe.updatePlayer(eye);
      if ((this.universe.current?.id ?? null) !== prevRegion) {
        // arriving somewhere is just a position change, not a level load
        this.shell.onRegionChanged?.(this.universe.current);
      }

      // ---- falling through a horizon: keep the way back visible ----
      const bh = this.universe.insideHorizon;
      const w = this.world as unknown as {
        setInterior?: (d: number, dir: Vector3) => void;
        setLens?: (p: unknown) => void;
      };
      if (typeof w?.setInterior === 'function') {
        if (bh) {
          // the exit is the direction back toward where we came from
          const back = this.lastOutsidePos.subtract(bh.position);
          w.setInterior(this.universe.horizonDepth,
            back.lengthSquared() > 1e-9 ? back : new Vector3(0, 0, -1));
          if (bh.lens && typeof w.setLens === 'function') w.setLens(bh.lens);
        } else {
          w.setInterior(0, new Vector3(0, 0, -1));
          this.lastOutsidePos.copyFrom(eye);
        }
      }

      // ---- the planet you are at simulates its own surface ----
      // Water, weather and erosion belong to the world you are standing on,
      // not to a global "water mode" somewhere else in the app.
      const here = this.universe.current;
      if (here && !this.paused) {
        this.surfaces.setActive(here.id);
        this.surfaces.acquire(here.id, here.seed ?? 1);
        this.surfaces.step(here.id, dt);
      }

      // ---- speed, distance and the warp effect ----
      // Measure actual travelled distance rather than trusting a throttle
      // value, so the readout matches what you can see happening.
      const moved = Vector3.Distance(eye, this.prevEye);
      const instant = dt > 1e-6 ? moved / dt : 0;
      // Smooth it or the number is unreadable at high framerates.
      this.shownSpeed += (instant - this.shownSpeed) * Math.min(1, dt * 6);
      this.prevEye.copyFrom(eye);

      const fwd = this.camera.getTarget().subtract(this.camera.position);
      this.warp.update(dt, this.shownSpeed, eye, fwd);

      this.shell.setFlight(
        this.shownSpeed,
        eye.length(),
        this.universe.current?.name ?? 'Deep space'
      );

      // ---- carrying things around ----
      if (this.grab.isHolding()) {
        const dir = this.camera.getTarget().subtract(this.camera.position);
        this.grab.update(dt, this.camera.position, dir);
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
