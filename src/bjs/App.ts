/**
 * App — wires the Babylon 9 engine, the world registry and the UI shell.
 */

import { Scene } from '@babylonjs/core/scene';
import { ArcRotateCamera } from '@babylonjs/core/Cameras/arcRotateCamera';
import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import type { AbstractEngine } from '@babylonjs/core/Engines/abstractEngine';

import { createEngine } from './Engine';
import { Shell } from './ui/Shell';
import type { World, WorldContext } from './World';
// Every place in the universe comes from one table now, rather than a world
// registry sitting alongside a region-kind lookup that had to agree with it.
import { buildLocale, localeForKind } from './worlds/Locales';
import { PostFX } from './PostFX';
import { IntroSequence } from './systems/IntroSequence';
import { IntroOverlay } from './ui/IntroOverlay';
import { WarpSystem } from './systems/WarpSystem';
import { PlanetSurfaceSystem } from './systems/PlanetSurfaceSystem';
import { MouseLook } from './systems/MouseLook';
import { LensFX } from './systems/LensFX';
import { StationSystem } from './systems/StationSystem';
import { CosmicScaleSystem } from './systems/CosmicScaleSystem';
import { inspectFrame, showBlackScreenReport } from './RenderWatchdog';
import { ElevatorSystem } from './systems/ElevatorSystem';
import { PortalGunSystem } from './systems/PortalGunSystem';
import { Descent, EARTHLIKE } from './systems/DescentSystem';
import { missingShaders } from './ShaderRegistry';
import { WarpDrive, galacticMedium } from './systems/DeepSkySystem';
import { Fleet, shipClass, shipView, type ViewMode } from './systems/FleetSystem';
import { StarFieldRenderer } from './systems/StarFieldRenderer';
import { LayeredSky } from './systems/LayeredSky';
import { HoleFieldRenderer } from './systems/HoleFieldRenderer';
import { SpaceAudio } from './systems/SpaceAudio';
import {
  depthOf, verseAt, verseProgress, edgeStateAt, crossInto,
  isAtFinalCoordinate, describeDepth, FINAL_COORDINATE, type VerseId
} from './systems/OuterVerses';
import { VerseRenderer } from './systems/VerseRenderer';
import { FlightHUD } from './ui/FlightHUD';
import { THROWABLES, computeImpact, throwableById } from './systems/ThrowableSystem';
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
  private introUI: IntroOverlay | null = null;
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
  /** Set once the player has moved the mouse, for the 'look' lesson. */
  private lookMoved = false;
  /** Title -> garage -> lessons -> portal -> ship. Replaces the main menu. */
  intro = new IntroSequence();
  /**
   * The current fall onto a world, if any. Null when not descending.
   * Created when the player drops toward a planet, so the sky, the heat and
   * the growing horizon all come from one physical model.
   */
  descent: Descent | null = null;

  /** Begins a fall onto a world from a given altitude in km. */
  beginDescent(altitudeKm = 120, speed = 0): void {
    this.descent = new Descent(
      EARTHLIKE,
      { mass: 90, area: 0.8, dragCoefficient: 1.0, noseRadius: 0.4 },
      altitudeKm, speed
    );
    this.shell.toast('Entering atmosphere');
  }

  /** Hold thrust long enough and the universe opens up. */
  warpDrive = new WarpDrive();
  /** Ships you have launched. They have mass, so they have gravity. */
  fleet = new Fleet();
  /** Which way you are looking at your ship. */
  shipViewMode: ViewMode = 'chase';
  private insideGalaxy = false;
  /** The sky, drawn from real regions rather than painted on a sphere. */
  starField = new StarFieldRenderer();
  /**
   * The anonymous background haze, in three parallaxing shells. Sits behind
   * starField, which draws the real reachable regions - together they give
   * a sky that is both deep and navigable.
   */
  layeredSky = new LayeredSky();
  /**
   * Real geometry for the black holes out in the universe. Without this a
   * hole you fly to is only a point of light plus a screen-space lens, so
   * there is nothing to arrive at.
   */
  holeField = new HoleFieldRenderer();
  /** Procedural hum / warp / singularity voices, driven from live state. */
  audio = new SpaceAudio();
  /** Whichever verse you are currently standing in. */
  verseRenderer = new VerseRenderer();
  /** Which verse that is. Changes only by crossing through The Nothing. */
  currentVerse: VerseId = 'universe';
  private reachedFinal = false;
  private crossing = false;
  /** The instrument panel you fly by. */
  flightHud = new FlightHUD();

  /** Planet-to-orbit tethers you can ride. */
  elevators = new ElevatorSystem();
  /** Two holes in the universe, and the walk between them. */
  portalGun = new PortalGunSystem();
  /** Zoom out far enough and you leave the universe entirely. */
  cosmicScale = new CosmicScaleSystem();
  /** Procedural space stations you can dock with and walk inside. */
  stations: StationSystem | null = null;
  /** Gravitational lensing that works in every world, not just one. */
  lensfx = new LensFX();
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
      onLaunchFleet: (cls, count) => {
        const c = shipClass(cls);
        if (!c) return;
        // Launched ahead of you, so you watch them arrive rather than
        // finding them already parked.
        const ahead = this.camera.getTarget().subtract(this.camera.position);
        const at = this.vehicle.position.add(
          (ahead.lengthSquared() > 1e-9 ? ahead.normalize() : new Vector3(0, 0, 1))
            .scale(140));
        this.fleet.launch(c, count, at);
        const g = this.fleet.gravity();
        this.shell.toast(
          count + ' × ' + c.name + ' launched' +
          (g.significant
            ? ' — the formation is generating ' + g.surfaceGravity.toFixed(2) + ' m/s²'
            : ''));
      },
      onClearFleet: () => { this.fleet.clear(); this.shell.toast('Fleet recalled'); },
      onShipView: (m) => { this.shipViewMode = m as ViewMode; },
      getFleet: () => {
        const g = this.fleet.gravity();
        return {
          size: this.fleet.vessels.length,
          mass: g.mass,
          gravity: g.surfaceGravity,
          bound: g.selfBinding,
          view: this.shipViewMode
        };
      },
      onHudElement: (name, on) => this.flightHud.setElement(name as any, on),
      getHudElements: () => ({ ...this.flightHud.elements }),
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
          stats: { ...this.universe.stats(), ...this.grab.stats(), ...this.surfaces.stats(), ...this.warp.stats(), ...this.mouse.stats(), ...this.lensfx.stats(), ...(this.stations?.stats() ?? {}), ...this.cosmicScale.stats(), ...this.elevators.stats(), ...this.portalGun.stats(), ...(this.descent?.stats() ?? {}) },
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
    this.stations = new StationSystem(this.scene);

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
      // Set by warpTo() just before the destination world is built, so the
      // world knows which hole/system it is a view of instead of assuming
      // it owns the origin.
      focus: null,
      setCameraTarget: (t: Vector3, r: number) => {
        this.camera.setTarget(t.clone());
        this.camera.radius = r;
        this.camera.upperRadiusLimit = Math.max(r * 12, 400);
      },
      enterDimension: (seed: number, depth: number) => {
        void this.enterDimension(seed, depth);
      }
    };

    // The flight instruments live outside the window layer so panels can be
    // closed without losing the ability to navigate.
    this.flightHud.mount();

    this.shell.progress(58, 'compiling shaders');
    // Boot into the garage: the title card renders over it, so clicking
    // Play puts you in a room that is already there.
    await this.loadWorld('garage');

    // Start in free flight inside the one continuous universe, rather than
    // parked in an orbit camera waiting for a menu choice.
    this.setControlMode('freefly');
    this.universe.updatePlayer(this.camera.position);

    this.shell.progress(88, 'warming pipeline');
    await new Promise((r) => setTimeout(r, 120));

    // A resize while the canvas is collapsed (a panel animating open, a
    // hidden tab, a layout reflow) makes the backbuffer 0xN. Every aspect
    // ratio computed from it is then 0/0 = NaN, which propagates into the
    // raymarcher and blanks the frame. Skip those resizes entirely and let
    // the next real one through - the canvas is not visible anyway.
    window.addEventListener('resize', () => {
      try {
        const c = this.engine.getRenderingCanvas();
        if (c && (c.clientWidth < 1 || c.clientHeight < 1)) return;
        this.engine.resize();
      } catch { /* engine already disposed */ }
    });

    // ---- vehicle input. Ignored while typing into a field. ----
    const typing = (e: KeyboardEvent) => {
      const t = e.target as HTMLElement | null;
      return !!t && (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA' || t.isContentEditable);
    };
    // Audio may only begin inside a user gesture, so arm it on the first
    // interaction of any kind and then stop listening.
    const armAudio = () => {
      if (this.audio.start()) this.audio.resume();
      window.removeEventListener('pointerdown', armAudio);
      window.removeEventListener('keydown', armAudio);
      const c2 = this.engine.getRenderingCanvas();
      c2?.removeEventListener('click', armAudio);
      c2?.removeEventListener('pointerdown', armAudio);
    };
    window.addEventListener('pointerdown', armAudio);
    window.addEventListener('keydown', armAudio);
    // The canvas swallows pointer events for mouse-look, so listen there
    // too - clicking into the sim is the most likely first gesture.
    const audioCanvas = this.engine.getRenderingCanvas();
    audioCanvas?.addEventListener('click', armAudio);
    audioCanvas?.addEventListener('pointerdown', armAudio);

    window.addEventListener('keydown', (e) => {
      if (typing(e)) return;
      this.keys.add(e.key.toLowerCase());
      // Space would otherwise scroll the page while flying
      if (e.key === ' ' && this.vehicle.mode !== 'orbit') e.preventDefault();
      // Pointer lock: look around without holding the button down.
      if (e.key.toLowerCase() === 'c') this.mouse.toggleLock();
      // Snap the spyglass back to normal.
      if (e.key.toLowerCase() === 'z') this.mouse.resetZoom();
    });
    window.addEventListener('keyup', (e) => this.keys.delete(e.key.toLowerCase()));
    window.addEventListener('blur', () => this.keys.clear());

    this.shell.progress(100, 'ready');
    setTimeout(() => this.shell.hideBoot(), 260);
    this.booted = true;

    // The main menu is gone. You get a title, then you are inside the
    // world: an infinite white garage, people who teach you the rules, a
    // portal, and a ship whose consoles are the menu. The sim renders live
    // behind all of it, so there is never a black screen.
    this.introUI = new IntroOverlay(this.intro, {
      onPlay: () => {
        this.intro.advance();            // title -> garage
        this.startWalking();
        this.shell.onMenuClosed();
      },
      onSkip: () => this.finishIntro(),
      onAdvance: () => this.advanceIntro()
    });
  }

  /** Walk mode, standing on the floor, for the garage and the ship. */
  private startWalking(): void {
    this.setControlMode('walk');
    this.vehicle.position.set(0, 1.7, 0);
    this.vehicle.velocity.set(0, 0, 0);
  }

  /**
   * One step forward in the opening. Each stage knows what it leads to, so
   * this stays a single path rather than a web of special cases.
   */
  private advanceIntro(): void {
    const st = this.intro.state;
    switch (st.stage) {
      case 'garage':
        this.intro.advance();            // -> lesson
        break;
      case 'lesson':
        this.intro.nextLesson();         // rolls into 'portal' on the last one
        break;
      case 'portal':
        this.intro.advance();            // -> ship
        this.loadWorld('ship').then(() => this.startWalking());
        break;
      case 'ship':
        this.finishIntro();
        break;
      default:
        break;
    }
  }

  /** Ends the intro and drops the player into the universe proper. */
  private finishIntro(): void {
    this.intro.skip();
    this.introUI?.dispose();
    this.introUI = null;
    if (this.currentId !== 'planetary') this.loadWorld('planetary');
    setTimeout(() => this.setControlMode('freefly'), 300);
    this.shell.onMenuClosed();
    this.shell.toast('Welcome to the sandbox. There is no objective.');
  }

  /**
   * Watches where the player is during the opening and moves it along.
   * Each stage has exactly one trigger, so there is no ambiguity about
   * what advances what.
   */
  /**
   * The journey outward: thinning stars, The Nothing, and crossing into the
   * verses beyond.
   *
   * Nothing here stops the player. Reaching the emptiness is not a wall -
   * it is a door, and going into it moves you somewhere genuinely else.
   */
  private updateOuterJourney(eye: Vector3): void {
    const depth = depthOf(eye.length());
    const verse = verseAt(depth);
    const progress = verseProgress(depth);
    const edge = edgeStateAt(progress);

    // Space empties as you approach a boundary, and the universe you left
    // glows behind you. Both are driven by the same value, so the sky and
    // the wall can never disagree about how far out you are.
    if (edge.emptiness > 0) {
      const w = edge.wallBrightness;
      this.scene.clearColor = new Color4(
        verse.tint[0] * 0.16 + w * 0.05,
        verse.tint[1] * 0.16 + w * 0.06,
        verse.tint[2] * 0.16 + w * 0.10,
        1);
    }

    // Entering The Nothing carries you into the next verse.
    if (edge.inNothing && !this.crossing) {
      const cross = crossInto(this.currentVerse);
      if (cross) {
        this.crossing = true;
        this.currentVerse = cross.to.id;
        // Placed just inside the new verse, so there is somewhere to go.
        const dir = eye.lengthSquared() > 1e-9
          ? eye.normalize() : new Vector3(0, 0, 1);
        this.vehicle.position.copyFrom(dir.scale(cross.arriveAt));
        this.verseRenderer.show(cross.to, 2400, cross.to.depth + 1);
        this.shell.toast(cross.message);
        // Cleared once you are clear of the boundary, so one crossing does
        // not immediately trigger the next.
        window.setTimeout(() => { this.crossing = false; }, 1200);
      }
    }

    // Draw whichever verse this is. Skips instantly when unchanged.
    if (verse.id !== 'universe') {
      this.verseRenderer.show(verse, 2400, verse.depth + 1);
    } else if (this.verseRenderer.current) {
      this.verseRenderer.clear();
    }

    // The very end.
    if (!this.reachedFinal && isAtFinalCoordinate(depth)) {
      this.reachedFinal = true;
      this.shell.toast(
        'You reached the final coordinate. There is nothing past this. ' +
        FINAL_COORDINATE.slice(0, 24) + '…(' + FINAL_COORDINATE.length + ' digits)');
    }
  }

  private updateIntro(eye: Vector3): void {
    const st = this.intro.state;

    if (st.stage === 'garage') {
      // Walking near the door starts the lessons.
      if (eye.z > 14) {
        this.intro.advance();
        this.world?.runAction?.('door:open', this.ctx);
      }
      return;
    }

    if (st.stage === 'lesson') {
      // Lessons that ask you to do something complete when you do it,
      // rather than making everything a click-through.
      const l = this.intro.currentLesson;
      if (!l) return;
      if (l.requires === 'move' && this.vehicle.velocity.length() > 1.2) {
        this.intro.didAction('move');
      } else if (l.requires === 'look' && this.lookMoved) {
        this.intro.didAction('look');
      } else if (l.requires === 'jump' && this.vehicle.velocity.y > 1.5) {
        this.intro.didAction('jump');
      }
      return;
    }

    if (st.stage === 'portal') {
      // Stepping into the ring takes you to the ship.
      const w = this.world as unknown as { portalPosition?: () => Vector3 };
      const pp = w.portalPosition?.() ?? new Vector3(0, 3, 18);
      if (Vector3.Distance(eye, pp) < 3.2) this.advanceIntro();
      return;
    }

    if (st.stage === 'ship') {
      // The ship is the menu: standing at a console and pressing E uses it.
      const w = this.world as unknown as
        { activeStation?: () => { id: string } | null };
      const at = w.activeStation?.();
      if (at && this.keys.has('e')) {
        this.keys.delete('e');
        this.useStation(at.id);
      }
    }
  }

  /** Runs a ship console. The ship is the menu, so this is the menu handler. */
  private useStation(id: string): void {
    switch (id) {
      case 'play':
        this.finishIntro();
        break;
      case 'universe': {
        const seed = this.universe.reseed();
        this.finishIntro();
        setTimeout(() => {
          this.shell.toast(`New universe - seed ${seed}`);
          this.shell.refreshAll();
        }, 420);
        break;
      }
      case 'graphics':
        this.finishIntro();
        setTimeout(() => this.shell.wm.Open('graphics'), 420);
        break;
      case 'presets':
        this.finishIntro();
        setTimeout(() => this.shell.wm.Open('presets'), 420);
        break;
      case 'library':
        this.finishIntro();
        setTimeout(() => this.shell.wm.Open('library'), 420);
        break;
      case 'load':
        this.finishIntro();
        setTimeout(() => this.shell.wm.Open('snapshots'), 420);
        break;
      default:
        break;
    }
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

      // One table describes every place; there is no separate world registry.
      const w = buildLocale(id);
      await w.build(this.ctx);
      this.world = w;
      this.currentId = id;
      // Post-processing without its shaders draws a black frame, so this is
      // checked before anything is attached rather than diagnosed after.
      const absent = missingShaders();
      if (absent.length) {
        console.error(
          'Post-process shaders are not registered, so post-processing is ' +
          'disabled to keep the picture visible. Missing: ' + absent.join(', '));
        this.shell.toast('Post-processing unavailable on this build');
      } else {
        this.postfx.attach(this.scene, this.camera);
        // Lensing is a property of the universe, not of one world, so it is
        // re-attached with the pipeline every time.
        this.lensfx.attach(this.scene, this.camera);
      }
      this.history.attach(
        typeof (w as any).captureState === 'function' ? (w as any) : null);

      // The purge above disposes every mesh, including the sky, so the star
      // field is re-attached and rebuilt rather than left pointing at a
      // disposed mesh.
      // Same for the fleet: the purge above disposed its hulls, so it is
      // reset rather than left holding disposed meshes.
      this.fleet.dispose();
      this.fleet.attach(this.scene);

      this.verseRenderer.dispose();
      this.verseRenderer.attach(this.scene);

      this.starField.dispose();
      this.starField.attach(this.scene);
      this.starField.rebuild(
        StarFieldRenderer.toSkyObjects(this.universe.regions),
        this.vehicle.position);

      // loadWorld purges every mesh, so the shells must be rebuilt with it.
      this.layeredSky.dispose();
      this.layeredSky.attach(this.scene);
      void this.layeredSky.build();

      // loadWorld purges every mesh, so the holes must be rebuilt too.
      this.holeField.dispose();
      this.holeField.attach(this.scene);

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

    // Arriving somewhere loads what that place actually is. This is why
    // there is no world list any more: an ocean world is a place you fly
    // to, not an entry you click.
    // Arriving resolves the region kind straight to a locale.
    const world = localeForKind(r.kind).id;
    // Tell the destination world WHICH place this is. Without it a world
    // renders its subject at the origin while the player stands beside the
    // real region coordinates, seeing nothing at all.
    this.ctx.focus = { position: r.position.clone(), radius: r.radius };
    // Always rebuild, even when the locale id is unchanged: flying from one
    // black hole to another stays in 'blackhole' but is a different subject,
    // and skipping the rebuild would leave the old hole on screen.
    void this.loadWorld(world).then(() => {
      // Re-point after the build. loadWorld purges the scene and the world
      // frames its own subject, so the aim must settle afterwards.
      this.camera.setTarget(r.position.clone());
    });
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
    // Aboard a station, the deck is the ground.
    const deck = this.stations?.floorAt(x, z);
    if (deck) return deck;
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
    this.lastFrameAt = performance.now();
    this.startWatchdogTimer();
    this.engine.runRenderLoop(() => {
      // The whole frame is guarded. A throw anywhere in here - a missing
      // Babylon side-effect import, a shader that will not compile on this
      // driver, a null world mid-switch - otherwise kills the render loop
      // permanently and the user just sees a black screen with no clue why.
      // Simulation is allowed to fail; drawing something is not.
      try {
        this.frame();
      } catch (e) {
        this.onFrameError(e);
        // Still put *something* on screen, so a broken subsystem degrades
        // to a visible scene instead of a black rectangle.
        try { this.scene.render(); } catch { /* nothing more we can do */ }
      }
    });
  }

  /** How many frames have thrown, and what the first failure was. */
  private lastFrameAt = 0;
  private frameErrors = 0;
  private frameErrorMsg = '';

  /**
   * Reports a frame failure once, loudly, instead of letting it repeat
   * silently sixty times a second.
   */
  private onFrameError(e: unknown): void {
    this.frameErrors++;
    const msg = e instanceof Error ? (e.stack ?? e.message) : String(e);
    if (this.frameErrors === 1) {
      this.frameErrorMsg = msg;
      console.error('Frame error (rendering continues):', msg);
      try {
        this.shell.toast('Render error - see console. The view may be degraded.');
      } catch { /* shell may not be up yet */ }
    }
    // A subsystem that fails every frame gets switched off rather than
    // spamming, so the rest of the sim keeps running.
    if (this.frameErrors === 120) {
      console.error('Frame errors are persistent; disabling lensing as a precaution.');
      try { this.lensfx.detach(); } catch { /* ignore */ }
    }
  }

  /** Diagnostics for the telemetry panel. */
  renderHealth(): Record<string, string> {
    return {
      'Frame errors': String(this.frameErrors),
      'First error': this.frameErrorMsg ? this.frameErrorMsg.split('\n')[0].slice(0, 60) : 'none'
    };
  }

  /** One simulation + render step. Called only from the guarded loop above. */
  private frame(): void {
    {
      const now = performance.now();
      const dt = Math.min((now - this.lastFrameAt) / 1000, 0.1);
      this.lastFrameAt = now;

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

        // ---- warp drive ----
        // Hold forward and the drive spools up, without limit. This is what
        // makes the map crossable: at cruise the far side of the universe is
        // hours away, and under warp it is seconds.
        const warping = this.warpDrive.update(dt, input.forward > 0.5);
        if (warping.engaged) {
          this.vehicle.flySpeed *= warping.multiplier;
        }
        // ---- audio, driven from live simulation state ----
        // Browsers only allow audio after a gesture, so the graph is started
        // lazily here; before that this is a no-op.
        {
          const bh = this.nearestHole();
          const eyeNow = this.vehicle.position;
          // Star density comes from the same galactic-medium model that
          // drives the fog, so the hiss and the visuals always agree.
          let dens = 0;
          const galA = this.universe.nearest(eyeNow, 'galaxy');
          if (galA) {
            const m = galacticMedium(eyeNow, galA.position, galA.radius);
            if (m.inside) dens = Math.min(1, m.depth);
          }
          this.audio.update({
            speed: this.vehicle.flySpeed * Math.abs(input.forward),
            warpCharge: this.warpDrive.charge,
            starDensity: dens,
            singularityDistance: bh
              ? Vector3.Distance(eyeNow, bh.position)
              : Infinity
          });
        }

        const look = this.mouse.consume(dt);
        // Arrow keys still work: whichever the player is using wins.
        if (Math.abs(look.yaw) > 1e-4) input.yaw = look.yaw;
        if (Math.abs(look.pitch) > 1e-4) input.pitch = look.pitch;
        // The 'look around' lesson completes when you actually look around.
        if (Math.abs(look.yaw) + Math.abs(look.pitch) > 0.02) this.lookMoved = true;
        const baseFly = this.vehicle.flySpeed;
        this.vehicle.update(dt, input, this.groundProbe);
        // The multiplier is applied per frame, so it must be taken back off
        // again or it would compound into nonsense within a second.
        if (warping.engaged) this.vehicle.flySpeed = baseFly / warping.multiplier;

        // The ship views are derived from one basis, so cockpit and chase
        // can never disagree about where the ship is pointing.
        if (this.shipViewMode !== 'chase' && this.vehicle.mode === 'fly') {
          const fwd = this.vehicle.lookTarget().subtract(this.vehicle.position);
          const view = shipView(this.shipViewMode, this.vehicle.position,
                                fwd, new Vector3(0, 1, 0), 26);
          this.camera.position.copyFrom(view.position);
          this.camera.setTarget(view.target);
        } else {
          this.camera.position.copyFrom(this.vehicle.position);
          this.camera.setTarget(this.vehicle.lookTarget());
        }
      }

      // ---- one continuous universe: where am I, and what is near me ----
      const eye = this.vehicle.mode === 'orbit'
        ? this.camera.position
        : this.vehicle.position;
      // A world can declare that it renders its own black hole. Both the
      // geometry hole field and the screen-space lens must stand down when it
      // does, or the scene gets a second hole and a grey wash over the core.
      const worldOwnsHole = this.world?.ownsBlackHole === true;
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

      // ---- gravitational lensing, wherever you happen to be ----
      // BlackHoleWorld integrates photon paths properly for its own view;
      // this bends whatever is actually on screen as you fly past a hole in
      // any world, which is what makes it feel like one universe.
      {
        // A world that raymarches its own hole already draws a physically
        // correct black core. The screen-space shadow floors at
        // col*0.06 + tint*0.035 - linear (0.035,0.022,0.010), a warm grey -
        // so painting it on top turned the core grey instead of black.
        const hole = worldOwnsHole
          ? null
          : (this.universe.insideHorizon ?? this.nearestHole());
        if (hole) {
          const hr = this.universe.horizonRadiusOf(hole);
          const d = Vector3.Distance(eye, hole.position);
          // Only worth doing when you are close enough to notice.
          if (d < hr * 260) {
            this.lensfx.track(hole.position, hr, this.camera, hole.lens ?? null);
          } else {
            this.lensfx.clear();
          }
        } else {
          this.lensfx.clear();
        }
      }

      // ---- speed, distance and the warp effect ----
      // Measure actual travelled distance rather than trusting a throttle
      // value, so the readout matches what you can see happening.
      const moved = Vector3.Distance(eye, this.prevEye);
      const instant = dt > 1e-6 ? moved / dt : 0;
      // Smooth it or the number is unreadable at high framerates.
      this.shownSpeed += (instant - this.shownSpeed) * Math.min(1, dt * 6);
      this.prevEye.copyFrom(eye);

      // Optical zoom drives the real camera FOV, so shift+wheel actually
      // magnifies the view instead of only changing a speed number.
      const wantFov = 0.9 / Math.max(1, this.mouse.zoomScale);
      this.camera.fov += (wantFov - this.camera.fov) * Math.min(1, dt * 8);

      const fwd = this.camera.getTarget().subtract(this.camera.position);
      this.warp.update(dt, this.shownSpeed, eye, fwd);
      this.stations?.update(dt);
      this.elevators.update(dt);
      this.fleet.update(dt);

      // ---- endless space ----
      // Chunks are generated as you approach and forgotten behind you, so
      // there is no edge to the universe and no total held in memory.
      this.universe.streamAround(eye);

      // ---- the real sky ----
      // Points are placed from actual region positions, so the sky
      // parallaxes as you fly and every light in it is a destination.
      this.starField.update(
        StarFieldRenderer.toSkyObjects(this.universe.regions), eye);

      // Each background shell slides toward the eye by its own lock factor,
      // so near stars sweep past and far ones hold station.
      this.layeredSky.update(eye);

      // ---- black holes you can actually reach ----
      // Give every nearby hole a horizon sphere and an accretion disk, both
      // driven from one centre so they cannot drift apart.
      //
      // A world that renders its own hole (BlackHoleWorld raymarches the
      // core, disk and lensing together) is the sole authority on where that
      // hole is. Running the geometry field there too put a second, unrelated
      // hole in the same scene: the user saw a bare black circle on one side
      // and the lensed disk on the other. Passing an empty list releases any
      // geometry already built instead of leaving it stranded in the scene.
      this.holeField.update(eye, worldOwnsHole ? [] : this.universe.regions
        .filter((r) => r.kind === 'blackhole')
        .map((r) => ({
          id: r.id,
          position: r.position,
          horizon: this.universe.horizonRadiusOf(r),
          seed: r.seed ?? 1
        })));

      // ---- flying into a galaxy ----
      // The interstellar medium thickens as you approach the core, so a
      // galaxy is something you enter rather than a sprite you pass. Fog is
      // driven by the same model that decides how crowded the stars are.
      {
        const gal = this.universe.nearest(eye, 'galaxy');
        if (gal) {
          const med = galacticMedium(eye, gal.position, gal.radius);
          if (med.inside) {
            this.scene.fogMode = Scene.FOGMODE_EXP2;
            this.scene.fogDensity = med.fogDensity;
            this.scene.fogColor = new Color3(
              med.fogColor[0], med.fogColor[1], med.fogColor[2]);
            if (!this.insideGalaxy) {
              this.insideGalaxy = true;
              this.shell.toast('Entering ' + gal.name);
            }
          } else if (this.insideGalaxy) {
            this.insideGalaxy = false;
            this.scene.fogMode = Scene.FOGMODE_NONE;
            this.shell.toast('Leaving the galaxy');
          }
        }
      }

      // An active descent drives the sky colour and the entry glow, so the
      // atmosphere thickens around you as you fall rather than cutting in.
      if (this.descent) {
        const d = this.descent.step(dt);
        const sky = d.skyColor;
        this.scene.clearColor = new Color4(sky[0], sky[1], sky[2], 1);
        // Re-entry heat blooms the frame.
        if (d.reentryGlow > 0.01) {
          this.postfx.set('bloom', 0.95 + d.reentryGlow * 1.4);
        }
        if (d.landed) {
          this.shell.toast('Touchdown');
          this.postfx.set('bloom', 0.95);
          this.descent = null;
        }
      }

      // The portal gun works on the player like anything else: walk into
      // one and you come out of the other, carrying your momentum.
      if (this.portalGun.linked && this.vehicle.mode !== 'orbit') {
        const trip = this.portalGun.tryTeleport({
          position: this.vehicle.position,
          velocity: this.vehicle.velocity,
          radius: 1.2
        }, dt);
        if (trip) {
          this.camera.position.copyFrom(this.vehicle.position);
          this.shell.toast('Through the portal');
        }
      }

      // ---- opening sequence triggers ----
      // Progress comes from where you walk, not from clicking through
      // prompts: reach the door and the instructors start, reach the
      // portal and you step through to the ship.
      if (!this.intro.state.done) this.updateIntro(eye);

      // Fly far enough from the centre and you cross out of the universe
      // into the tier above it. Each tier recolours the void so the change
      // is something you see, not something you read in a panel.
      // ---- the outward journey ----
      // Distance is tracked as a depth in digits, because the far end of
      // this scale is a 414-digit number that no float can hold. Depth is
      // an integer 0-414, so the whole span stays representable.
      this.updateOuterJourney(eye);

      const scaleState = this.cosmicScale.update(eye.length());
      if (scaleState.changed) {
        const t = scaleState.tier;
        this.scene.clearColor = new Color4(
          t.tint[0] * 0.16, t.tint[1] * 0.16, t.tint[2] * 0.16, 1);
        this.shell.toast(
          (scaleState.direction > 0 ? 'Exited into ' : 'Fell back into ') +
          t.name + ' - ' + t.tagline);
      }

      this.shell.setFlight(
        this.shownSpeed,
        eye.length(),
        this.universe.current?.name ?? 'Deep space'
      );

      // ---- flight instruments ----
      // Fed from the same state the camera uses, so the numbers on screen
      // always describe the frame you are actually looking at.
      {
        const near = this.universe.nearest(eye);
        const fg = this.fleet.gravity();
        const att = this.vehicle.attitude();
        const w = this.warpDrive.state();
        this.flightHud.update({
          x: eye.x, y: eye.y, z: eye.z,
          heading: att.yaw,
          pitch: att.pitch,
          speed: this.shownSpeed,
          throttle: Math.min(1, this.shownSpeed / Math.max(1, this.vehicle.flySpeed * 12)),
          warpCharge: w.charge,
          warpMultiplier: w.multiplier,
          locale: (() => {
            const d = depthOf(eye.length());
            const v = verseAt(d);
            return v.id === 'universe'
              ? (near?.name ?? 'Deep space')
              : v.name + ' · ' + describeDepth(d);
          })(),
          localeDistance: near ? Vector3.Distance(eye, near.position) : 0,
          fleetSize: this.fleet.vessels.length,
          fleetGravity: fg.surfaceGravity
        });
      }

      // ---- carrying things around ----
      if (this.grab.isHolding()) {
        const dir = this.camera.getTarget().subtract(this.camera.position);
        this.grab.update(dt, this.camera.position, dir);
      }
      // ---- strict camera sync, immediately before the draw ----
      // world.update() ran early in the frame, before the camera was moved
      // into its final position. Any world that raymarches from camera
      // uniforms would otherwise be drawing from a one-frame-stale camera,
      // which shows up as the black hole's disk sliding off its horizon
      // while turning or when a panel resizes the canvas.
      (this.world as any)?.syncCamera?.(this.ctx);

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

      // ---- black-screen watchdog ----
      // Runs for the first few seconds only. If the canvas really is blank,
      // say why on screen instead of leaving the user guessing.
      this.watchdogFrames++;
      // Sampled at several points, and a report needs repeated agreement -
      // one unlucky read during a world switch or a resize is not evidence.
      if (this.watchdogFrames === 90 || this.watchdogFrames === 150 ||
          this.watchdogFrames === 300 || this.watchdogFrames === 600) {
        this.checkForBlackScreen();
      }
    }
  }

  private watchdogFrames = 0;
  private watchdogReported = false;
  private watchdogTimer: number | null = null;
  /** Post-processing is stripped once, automatically, on a black frame. */
  private blackScreenRecoveryTried = false;
  /** Consecutive black samples; one is never enough to report. */
  private blackFrameStreak = 0;

  /**
   * The frame-counted watchdog can only fire if frames are happening. A dead
   * render loop is exactly the case that produces a black screen, so the real
   * check has to be driven by a timer that does not depend on the loop.
   */
  private startWatchdogTimer(): void {
    let ticks = 0;
    const seenFrames = () => this.watchdogFrames;
    const before = seenFrames();
    this.watchdogTimer = window.setInterval(() => {
      ticks++;
      if (this.watchdogReported || ticks > 6) {
        if (this.watchdogTimer !== null) window.clearInterval(this.watchdogTimer);
        this.watchdogTimer = null;
        return;
      }
      // Give it ~2.5s of grace, then judge.
      if (ticks < 3) return;

      if (seenFrames() === before) {
        // The loop never ran a single frame.
        this.watchdogReported = true;
        showBlackScreenReport({
          painting: false,
          luminance: 0,
          diagnosis: this.frameErrors > 0
            ? 'The render loop is throwing every frame: ' +
              (this.frameErrorMsg.split('\n')[0] || 'unknown error')
            : 'The render loop never started, so no frame was ever drawn.',
          warnings: [
            'frames rendered: 0',
            'frame errors: ' + this.frameErrors,
            'meshes: ' + (this.scene?.meshes.length ?? 0)
          ]
        });
        return;
      }
      // Frames ARE happening, so the loop is alive. Deliberately do not
      // sample pixels here: readPixels outside the render loop reads the
      // composited buffer, which the browser is entitled to have already
      // cleared, so it returns zeros for a frame the user can plainly see.
      // That produced a "the screen is black" panel over a working scene.
      // Pixel sampling only happens inside frame(), where the buffer is
      // guaranteed valid; this timer's only job is catching a dead loop.
      if (this.watchdogTimer !== null) window.clearInterval(this.watchdogTimer);
      this.watchdogTimer = null;
    }, 850);
  }

  /** Reads the real framebuffer and reports a blank one. */
  checkForBlackScreen(): void {
    if (this.watchdogReported) return;
    try {
      const canvas = this.engine.getRenderingCanvas() as HTMLCanvasElement;
      const report = inspectFrame({
        canvas,
        gl: (this.engine as unknown as { _gl?: WebGL2RenderingContext })._gl,
        meshCount: () => this.scene?.meshes.length ?? 0,
        frameErrors: () => this.frameErrors,
        firstError: () => this.frameErrorMsg.split('\n')[0] || 'none',
        fps: () => this.engine.getFps()
      });
      if (report.painting) {
        // A single good frame permanently clears suspicion.
        this.blackFrameStreak = 0;
        return;
      }

      // Require consecutive black samples before believing it. Transient
      // black frames are normal during a world switch, a resize, or the
      // moment a render target is rebuilt, and reporting one of those as a
      // failure is worse than useless - it puts an alarming panel over a
      // perfectly good picture.
      this.blackFrameStreak++;
      if (this.blackFrameStreak < 2) return;

      {
        // Reporting a black screen is not good enough - recover from it.
        // If the frame is being drawn (meshes present, frames ticking) but
        // every pixel is black, the overwhelmingly likely culprit is the
        // post-process chain resolving to nothing. Strip it and re-test
        // before bothering the user: an unfiltered image beats no image.
        if (!this.blackScreenRecoveryTried && report.luminance <= 0.0001) {
          this.blackScreenRecoveryTried = true;
          console.warn('[black screen] stripping post-processing and retrying');
          try { this.lensfx.detach(); } catch { /* already gone */ }
          try { this.postfx.detach(); } catch { /* already gone */ }
          // Guarantee the clear colour itself is not black, so even an
          // empty scene shows something.
          const c = this.scene.clearColor;
          if (c.r + c.g + c.b < 0.02) {
            this.scene.clearColor = new Color4(0.05, 0.07, 0.13, 1);
          }
          this.shell.toast('Recovering from a black frame - post-processing disabled');
          // Reset the streak and let the in-frame sampler judge the result.
          // Re-checking from a timer would read the composited buffer and
          // could report black for a frame that is now fine.
          this.blackFrameStreak = 0;
          return;
        }

        this.watchdogReported = true;
        console.error('[black screen]', report.diagnosis, report.warnings);
        showBlackScreenReport(report);
      }
    } catch (e) {
      console.warn('Watchdog could not run:', e);
    }
  }
}
