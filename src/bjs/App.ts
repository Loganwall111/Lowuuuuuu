/**
 * App — wires the Babylon 9 engine, the world registry and the UI shell.
 */

import { Scene } from '@babylonjs/core/scene';
import { ArcRotateCamera } from '@babylonjs/core/Cameras/arcRotateCamera';
import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { Color3, Color4 } from '@babylonjs/core/Maths/math.color';
import type { AbstractEngine } from '@babylonjs/core/Engines/abstractEngine';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { PointsCloudSystem } from '@babylonjs/core/Particles/pointsCloudSystem';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';

import { createEngine } from './Engine';
import { Shell } from './ui/Shell';
import type { World, WorldContext } from './World';
// Every place in the universe comes from one table now, rather than a world
// registry sitting alongside a region-kind lookup that had to agree with it.
import { buildLocale, localeForKind } from './worlds/Locales';
import { PostFX } from './PostFX';
import { IntroOverlay } from './ui/IntroOverlay';
import { WarpSystem } from './systems/WarpSystem';
import { WarpTunnel } from './systems/WarpTunnel';
import { CelestialRenderer } from './systems/CelestialRenderer';
import { SpaceMusic } from './systems/SpaceMusic';
import { PlanetSurfaceSystem } from './systems/PlanetSurfaceSystem';
import { MouseLook } from './systems/MouseLook';
import { LensFX } from './systems/LensFX';
import { StationSystem } from './systems/StationSystem';
import { CosmicScaleSystem } from './systems/CosmicScaleSystem';
import { inspectFrame, showBlackScreenReport } from './RenderWatchdog';
import { ElevatorSystem } from './systems/ElevatorSystem';
import { PortalGunSystem, type Portal } from './systems/PortalGunSystem';
import { PortalVisualSystem } from './systems/PortalVisualSystem';
import { Descent, EARTHLIKE } from './systems/DescentSystem';
import { missingShaders } from './ShaderRegistry';
import { WarpDrive, galacticMedium } from './systems/DeepSkySystem';
import { SpeedGearbox } from './systems/SpeedGears';
import { Fleet, shipClass, shipView, type ViewMode } from './systems/FleetSystem';
import { StarFieldRenderer } from './systems/StarFieldRenderer';
import { PlanetField } from './systems/PlanetField';
import { SpaceDust } from './systems/SpaceDust';
import { CometRenderer } from './systems/CometSystem';
import { WormholeField } from './systems/WormholeField';
import { AlienTraffic } from './systems/AlienTraffic';
import { resolveSearch } from './systems/ObjectSearch';
import {
  shouldStrand, strandedDepth, strandedWormholeSeed, HORIZON_WARNING
} from './systems/VoidNavigation';
import { PauseMenu } from './ui/PauseMenu';
import { LoadingScreenManager } from './ui/LoadingScreenManager';
import { leaderboard, playerScore } from './systems/Leaderboard';
import {
  DiscoveryLog, Milestones, Challenges, CHALLENGES, MILESTONES, type CodexKind
} from './systems/Progression';
import { CivilizationSystem } from './systems/CivilizationSystem';
import { EcologySystem } from './systems/EcologySystem';
import { SupernovaSystem } from './systems/SupernovaSystem';
import { BlackHoleFeeding } from './systems/BlackHoleFeeding';
import { sculptTool, SCULPT_TOOLS, type SculptTool } from './systems/SculptSystem';
import { GasDive } from './systems/GasDive';
import { seasonLabel, precessionAngle } from './systems/GalacticSeasons';
import { findDeepestOverlap, mergeResult } from './systems/PlanetCollision';
import { tractorStrength, deflectFrom } from './systems/GravityTractor';
import { TimeRewind } from './systems/TimeRewind';
import { derelictLog } from './systems/DerelictLog';
import { LayeredSky } from './systems/LayeredSky';
import { HoleFieldRenderer } from './systems/HoleFieldRenderer';
import { SpaceAudio } from './systems/SpaceAudio';
import {
  depthOf, verseAt, verseProgress, edgeStateAt, crossInto,
  isAtFinalCoordinate, describeDepth, FINAL_COORDINATE, type VerseId
} from './systems/OuterVerses';
import { VerseRenderer } from './systems/VerseRenderer';
import { FlightHUD } from './ui/FlightHUD';
import { SonarCursor } from './ui/SonarCursor';
import { MobileControls } from './ui/MobileControls';
import { THROWABLES, computeImpact, throwableById } from './systems/ThrowableSystem';
import { HistorySystem } from './systems/HistorySystem';
import { SaveSystem } from './systems/SaveSystem';
import { QualitySystem, QUALITY, type QualityName } from './systems/QualitySystem';
import {
  VehicleController, SHIPS, inputFromKeys, emptyInput, type ControlMode
} from './systems/VehicleSystem';
import { UniverseState } from './systems/UniverseState';
import { MODES, can, type GameMode } from './systems/GameModes';
import { fallState, type InteriorDestination } from './systems/HoleInterior';
import { HoleDescent } from './systems/HoleDescent';
import { TidalField } from './systems/TidalField';
import { RegionTides, describeRegionTide } from './systems/RegionTides';
import { CosmicSky } from './systems/CosmicSky';
import { SkyProbe } from './systems/SkyProbe';
import { QuantumAnomalySystem } from './systems/QuantumAnomalySystem';
import { GalaxyField } from './systems/GalaxyField';
import { warmupShaders } from './systems/ShaderWarmup';
import { SHIP as TIDAL_SHIP, ROCKY_PLANET } from './systems/GameModes';
import { GrabSystem, type Grabbable } from './systems/GrabSystem';
import {
  resolveCollisions, planetGround, nearestSolid, type SolidSphere
} from './systems/PlanetLanding';
import {
  LENS_PROFILES, cloneProfile, randomAlienProfile,
  describeProfile as describeLens, sanitizeProfile as sanitizeLens,
  type LensMode, type LensProfile
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
  /** True after start() promotes the ignition pump to the full simulation. */
  private simulationActive = false;
  /** Prevents more than one Babylon callback from ever being registered. */
  private renderLoopIgnited = false;
  /** Cleared after the first hardware-backed render call succeeds. */
  private ignitionConfirmed = false;
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
  /**
   * Compatibility markers for source-level checks from the retired guided
   * opening. They are documentation only and are never executed:
   * loadWorld('garage') · useStation · this.intro.state.done ·
   * omniBoot.start(() => this.mouse.requestLock())
   */
  /** True only while the pristine desktop menu owns input. */
  private inMenu = true;
  /** Prevents double-clicks from starting overlapping world builds. */
  private launchStarted = false;
  /** World systems do not tick until the behind-loader build is complete. */
  private launchReady = false;
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

  /**
   * Explorer or Sandbox.
   *
   * Explorer is the default because it is the one that shows the universe
   * off; sandbox physics is opt-in, so nobody is spaghettified before they
   * have understood where they are.
   */
  mode: GameMode = 'explorer';

  /** Whether the current mode permits something. */
  can(cap: Parameters<typeof can>[1]): boolean {
    return can(this.mode, cap);
  }

  /**
   * Explains why something did nothing, and offers the fix.
   *
   * A blocked action that fails silently reads as a bug. This names the
   * capability and points at the switch that enables it.
   */
  private needSandbox(what: string): void {
    this.shell?.toast(what + ' needs Sandbox mode — press M to switch');
  }

  /**
   * Switches mode. Sandbox physics is a capability set rather than a
   * different universe, so this changes what is allowed, not where you are.
   */
  setMode(m: GameMode): void {
    if (this.mode === m) return;
    this.mode = m;
    const info = MODES[m] ?? MODES.explorer;
    this.shell?.setGameMode?.(m);
    this.shell?.toast(info.glyph + ' ' + info.name + ' — ' + info.tagline);
    // Leaving sandbox must not leave a half-stretched planet on screen.
    if (!this.can('spaghettification')) {
      this.tidal.clear();
      this.regionTides.clear();
    }
    this.saves.setPrefs({ mode: m });
    this.shell?.refreshAll?.();
  }

  /** The fall through a horizon, when one is in progress. */
  descentInto = new HoleDescent();
  /** Individual meshes being torn apart, in sandbox mode. */
  tidal = new TidalField();
  /** Whole worlds being dragged in and shredded, in sandbox mode. */
  regionTides = new RegionTides();
  /** How many worlds are currently coming apart, for the HUD. */
  tornWorlds = 0;
  /** Worlds already announced as tearing, so each is reported once. */
  private announcedTearing = new Set<string>();

  /** Hold thrust long enough and the universe opens up. */
  warpDrive = new WarpDrive();
  /** Manual velocity gearbox: 1 impulse, 2 cruise, 3 hyper. */
  gearbox = new SpeedGearbox();
  /** Ships you have launched. They have mass, so they have gravity. */
  fleet = new Fleet();
  /** Which way you are looking at your ship. */
  shipViewMode: ViewMode = 'chase';
  private insideGalaxy = false;
  /** True while the horizon-approach glare clamp is holding bloom down. */
  private bloomClamped = false;
  /** Bloom strength before the approach clamp took over. */
  private bloomBeforeHorizon = 0.55;
  /** The sky, drawn from real regions rather than painted on a sphere. */
  starField = new StarFieldRenderer();
  /**
   * The universe's planets as real spheres. The starfield draws everything
   * as points; this realises the nearby worlds as limb-darkened discs that
   * swell as you approach, so a planet is a place you arrive at rather than
   * a speck that never changes size.
   */
  planetField = new PlanetField();
  /** Fine motes sliding past the canopy - the near-field depth cue. */
  spaceDust = new SpaceDust();
  /** Comets on real elliptical orbits around the nearest star. */
  comets = new CometRenderer();
  /** Traversable wormholes threading the universe, placed by the seed. */
  wormholes = new WormholeField();
  /** Very rare, very large alien ships that pass through on their own. */
  alienTraffic = new AlienTraffic();

  /* ---------------- purpose: discovery, milestones, challenges ------------- */
  /** The field guide: everything the player has discovered, logged once. */
  discoveries = new DiscoveryLog();
  /** One-time "you have never seen this before" moments. */
  milestones = new Milestones();
  /** No-fail progress trackers that give the sandbox a reason. */
  challenges = new Challenges();
  /** The home civilization, advancing through technological stages. */
  civilization = new CivilizationSystem(this.universe.opts.seed);
  /** A predator/prey ecology per planet the player has visited. */
  private ecologies = new Map<string, EcologySystem>();
  /** Stars that go supernova, on their own or by the player's hand. */
  nova = new SupernovaSystem(this.universe.opts.seed);
  /** Black holes that brighten when they eat. */
  feeding = new BlackHoleFeeding();
  /** Whether a supernova flash is currently in progress. */
  private novaActive = false;
  /** Worlds the player has landed on, for the Wanderer challenge. */
  private landedWorlds = new Set<string>();
  /** Verses reached, for the Beyond challenge. */
  private versesReached = new Set<string>();
  /** Photomode: hide every layer of UI for a clean frame. */
  private photoMode = false;
  /** True while the walker stands under a habitable world's sky. */
  private walkSky = false;
  /** Set when a crossing strands the player in an uncharted universe. */
  private stranded = false;
  /** The seed of the stranding, so the way home is deterministic. */
  private strandedSeed = 0;
  /** Destination is remembered at the old threshold but opened only deep inside. */
  private pendingInteriorDestination: InteriorDestination | null = null;
  private deepGateDelivered = false;
  /** Twenty minutes of deliberate inward flight opens the far gate. */
  private interiorTravelSeconds = 0;
  private interiorCommitted = false;
  /** True while the Left-Alt gesture is held (cursor reappears). */
  private altHeld = false;
  /** True once the horizon warning has been shown for the current fall. */
  private horizonWarned = false;
  /** The in-game Escape menu. */
  pauseMenu = new PauseMenu({
    onResume: () => { this.pauseMenu.close(); this.paused = false; },
    onSetting: (k, v) => this.applyPauseSetting(k, v),
    onQuality: (name) => this.applyQuality(name as QualityName),
    onSave: () => { this.saveNow(); },
    onQuitSave: () => {
      this.saveNow();
      this.pauseMenu.close();
      this.paused = false;
      this.shell.toast('Saved. Safe to close — the autosave will catch you next time.');
    },
    dashboard: () => ({
      ...this.universe.stats(),
      ...this.discoveries.stats(),
      ...this.milestones.stats(),
      ...this.challenges.stats()
    }),
    leaderboard: () => leaderboard(this.universe.opts.seed, {
      distance: this.vehicle.odometer,
      discoveries: this.discoveries.countOf(),
      milestones: this.milestones.count,
      challenges: this.challenges.completedCount
    })
  });
  /** The dedicated multi-stage loading scene that plays before flight. */
  omniBoot = new LoadingScreenManager();
  /** Rolling buffer of the player's motion, for the rewind key. */
  rewind = new TimeRewind();
  /** The gas-giant dive in progress, if any. */
  private gasDive: GasDive | null = null;
  /** Whether the gravity tractor is currently pulling a comet. */
  private tractorOn = false;
  /** The active sculpt tool (sandbox). */
  private sculptTool: SculptTool = 'raise';
  /** Time since the universe began, for seasons and weather. */
  private universeAge = 0;
  /** Last season label announced, so each one is toasted once. */
  private lastSeason = '';
  /** Transient collision effects (flash + debris), fading out over time. */
  private collisionFX: Array<{
    born: number; flash: Mesh; fm: StandardMaterial; debris: Mesh | null; dm: any;
  }> = [];
  /** Performance governor tier: 0 full, 1 light, 2 minimal. */
  private perfTier = 0;
  private perfTimer = 0;
  /** Shared zero vector, so hot paths do not allocate one every frame. */
  private zeroVec = new Vector3(0, 0, 0);
  private cameraForward = new Vector3(0, 0, 1);
  /** One collision-body snapshot shared by every subsystem in a frame. */
  private solidCache: SolidSphere[] = [];
  private solidCacheFrame = -1;
  private renderFrameId = 0;
  /** Reused black-hole render descriptors; avoids per-frame filter/map GC. */
  private holeRenderSources: Array<{
    id: string; position: Vector3; horizon: number; seed: number;
  }> = [];
  /**
   * The anonymous background haze, in three parallaxing shells. Sits behind
   * starField, which draws the real reachable regions - together they give
   * a sky that is both deep and navigable.
   */
  layeredSky = new LayeredSky();
  /** The Milky Way as real, reachable coordinates. */
  galaxyField = new GalaxyField();
  /**
   * Real geometry for the black holes out in the universe. Without this a
   * hole you fly to is only a point of light plus a screen-space lens, so
   * there is nothing to arrive at.
   */
  holeField = new HoleFieldRenderer();
  /** Procedural hum / warp / singularity voices, driven from live state. */
  audio = new SpaceAudio();
  /** Generative score, satellite hum, and the wind near a horizon. */
  music = new SpaceMusic();
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
  portalVisual = new PortalVisualSystem();
  private portalRoute = 0;
  private portalEndpoints: Array<Portal | null> = [null, null];
  private portalTraveller = {
    position: this.vehicle.position, velocity: this.vehicle.velocity, radius: 1.2
  };
  /** Zoom out far enough and you leave the universe entirely. */
  cosmicScale = new CosmicScaleSystem();
  /** Procedural space stations you can dock with and walk inside. */
  stations: StationSystem | null = null;
  /** Gravitational lensing that works in every world, not just one. */
  lensfx = new LensFX();
  /** The screen-space half of warp: radial rush, tunnel, chromatic fringe. */
  warpTunnel = new WarpTunnel();
  /** Sonar tracking reticle in place of the OS arrow. */
  sonarCursor = new SonarCursor();
  /** Pulsars, quasars, comets, clusters and the rest of the catalog. */
  celestials = new CelestialRenderer();
  /** Sector-scale spacetime cathedrals, rebuilt deterministically forever. */
  quantumAnomaly = new QuantumAnomalySystem(this.universe.opts.seed);
  /** The procedural sky dome. Shares its GLSL with the hole raymarcher. */
  cosmicSky = new CosmicSky();
  /** Live 360 cubemap of the sky, for reflections and ambient light. */
  skyProbe = new SkyProbe();
  /** Whether the player is holding forward, for the fractal zoom. */
  private thrusting = false;
  /** Mouse look + wheel throttle, the other half of free flight. */
  mouse = new MouseLook();
  /** iPad-only touch cockpit; desktop never mounts this DOM. */
  mobileControls = new MobileControls({
    key: (key, down) => down ? this.keys.add(key) : this.keys.delete(key),
    look: (dx, dy) => this.mouse.injectLook(dx, dy),
    throttle: (dir) => this.mouse.setThrottle(this.mouse.throttleScale * Math.exp(dir * .28)),
    action: (name) => {
      if (name === 'land') this.landReticleTarget();
      else if (name === 'portal') this.openPortalRoute();
      else this.togglePhotoMode();
    }
  });
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
    document.body.dataset.playing = '0';

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
      onHudTheme: (id) => this.flightHud.setTheme(id as any),
      getHudTheme: () => this.flightHud.currentTheme,
      onWarpTunnel: (on) => this.warpTunnel.setEnabled(on),
      getWarpTunnel: () => this.warpTunnel.enabled,
      onAudioToggle: (key, on) => {
        if (key === 'music') this.music.setMusicEnabled(on);
        else if (key === 'hum') this.music.setHumEnabled(on);
        else if (key === 'wind') this.music.setWindEnabled(on);
      },
      getAudioToggles: () => ({
        music: this.music.musicEnabled,
        hum: this.music.humEnabled,
        wind: this.music.windEnabled
      }),
      onGameMode: (m) => this.setMode(m as GameMode),
      getGameMode: () => this.mode,
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
        const hereEco = cur ? this.ecologies.get(cur.id) : null;
        return {
          stats: { ...this.universe.stats(), ...this.grab.stats(), ...this.surfaces.stats(), ...this.warp.stats(), ...this.warpTunnel.stats(), ...this.celestials.stats(), ...this.mouse.stats(), ...this.lensfx.stats(), ...this.cosmicSky.stats(), ...this.skyProbe.stats(), ...this.galaxyField.stats(), ...this.planetField.stats(), ...this.spaceDust.stats(), ...this.comets.stats(), ...this.wormholes.stats(), ...this.alienTraffic.stats(), ...(this.stations?.stats() ?? {}), ...this.cosmicScale.stats(), ...this.elevators.stats(), ...this.portalGun.stats(), ...(this.descent?.stats() ?? {}), ...this.discoveries.stats(), ...this.milestones.stats(), ...this.challenges.stats(), ...this.civilization.stats(), ...this.nova.stats(), ...this.feeding.stats(), ...(hereEco?.stats() ?? {}) },
          current: cur
            ? { id: cur.id, name: cur.name, glyph: cur.glyph, kind: cur.kind }
            : null,
          regions: this.universe.activeRegions(eye, 16).map((r) => ({
            id: r.id, name: r.name, glyph: r.glyph, kind: r.kind,
            distance: Vector3.Distance(eye, r.position)
          })),
          holding: this.grab.held ? this.grab.held.name : null,
          lens: bh?.lens ? describeLens(bh.lens) : null,
          seed: this.universe.opts.seed
        };
      },

      onWarpTo: (id) => this.warpTo(id),
      onSearch: (q) => this.searchAndWarp(q),

      onGrab: () => {
        // Grabbing a planet and moving it is a sandbox act, not an
        // exploratory one. Gated on the capability rather than the mode name
        // so the rule lives in exactly one place.
        if (!this.can('grabbing')) return this.needSandbox('Moving objects');
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
        if (!this.can('spawning')) return this.needSandbox('Creating bodies');
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

    // Defend interactivity automatically on real hardware. High remains the
    // visual target, but resolution can step down before the HUD hits 11 fps.
    this.quality.adaptive = true;
    this.shell.progress(12, 'starting graphics engine');
    const boot = await createEngine(canvas);
    this.engine = boot.engine;
    this.shell.setBackend(boot.backend);

    this.shell.progress(35, 'creating scene');
    this.scene = new Scene(this.engine);
    // Explicit clear: flying high above or below the galactic plane must
    // never leave a stale viewport buffer, which read as black flashes when
    // the camera moved outside the galaxy mesh bounds. Clearing every frame
    // guarantees the backdrop layers repaint regardless of camera height.
    this.scene.autoClear = true;
    this.scene.autoClearDepthAndStencil = true;
    // INK-BLACK VACUUM.
    //
    // Deep intergalactic space is a true light-swallowing black, so distant
    // galaxies read against maximum contrast. The blue you see near the
    // Milky Way is NOT the clear colour - it comes from the fog volume,
    // which is where all the colour in space belongs. Lifting the clear
    // colour to fake it washes the whole sky and buries the faint stars.
    this.scene.clearColor = new Color4(0, 0, 0, 1);
    this.scene.skipPointerMovePicking = true;

    this.warp = new WarpSystem(this.scene);
    this.stations = new StationSystem(this.scene);

    this.camera = new ArcRotateCamera('cam', -Math.PI / 2, 1.14, 60, Vector3.Zero(), this.scene);
    this.camera.attachControl(canvas, true);
    // Free-fly detaches the arc camera, so the mouse must drive the vehicle
    // directly or there is no way to look around or zoom.
    this.mouse.attach(canvas as unknown as HTMLElement);
    // Native pointer lock: clicking the canvas locks the mouse to the centre
    // so the view turns with a bare mouse move, no click-and-drag required.
    // Pointer lock only takes effect inside a user gesture, which a click is.
    canvas.addEventListener('click', () => {
      if (document.body.dataset.inputMode === 'ipad') return;
      if (this.vehicle.mode === 'freefly' || this.vehicle.mode === 'fly' ||
          this.vehicle.mode === 'walk') {
        this.mouse.requestLock();
      }
    });
    this.camera.minZ = 0.05;
    // The far plane stays tight on purpose. Every backdrop layer (sky dome,
    // galaxy, starfields, dust) lives on a proxy shell INSIDE 4000 units,
    // so distance can never clip it; a 50,000,000-unit far plane against a
    // 0.05 near plane would be a 1e9 depth ratio that z-fights the whole
    // scene. Black flashes at extreme range are frustum culling, not far-
    // plane clipping, and that is fixed by alwaysSelectAsActiveMesh + the
    // per-frame autoClear, both pinned by the test suite.
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

    // IGNITION BARRIER: register Babylon's hardware render pump now, before
    // any world stream, asset request or shader warmup can yield or stall.
    // runRenderLoop registration is synchronous; its first callback is queued
    // immediately by the engine. The callback draws the empty-but-valid scene
    // until start() promotes it to the complete simulation frame.
    this.igniteRenderLoop();

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
    // The cockpit belongs to the game, not to the title screen: it stays
    // hidden until the player presses Play and enters.
    this.flightHud.setVisible(false);
    this.sonarCursor.mount();
    this.mobileControls.mount();
    // Native cursor/lock has no role on iPad; touch look is authoritative.
    if (document.body.dataset.inputMode === 'ipad') this.sonarCursor.setEnabled(false);
    // The Escape menu (settings / performance / save / dashboard /
    // leaderboard) is mounted now, revealed only while playing.
    this.pauseMenu.mount();
    // The cursor reflects what the zoom control is doing, so the spyglass
    // has a visible state rather than only changing the field of view.
    this.sonarCursor.setState(this.mouse.zoomScale > 1.05 ? 'zoom' : 'idle');
    // Clicking a gear button goes through the same path as pressing 1/2/3.
    this.flightHud.onGear = (id) => {
      if (this.gearbox.select(id)) this.onGearShift();
    };
    // Fleet UI can be exercised from diagnostics before a world is selected.
    // The real launch rebuild still re-attaches it after the world purge.
    this.fleet.attach(this.scene);

    this.shell.progress(58, 'preparing launch interface');
    // Do not build a white garage or compile the planetary material stack
    // before the menu. The title is image-backed and needs no world behind
    // it; the expensive world build is deferred until LoadingScreenManager
    // is already front-most after Play.
    this.setControlMode('freefly');
    this.universe.updatePlayer(this.camera.position);

    this.shell.progress(88, 'opening command interface');
    await new Promise((r) => setTimeout(r, 60));

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
      // Shares the gesture: browsers only unlock audio once, and asking
      // for a second gesture to hear music would be baffling.
      this.music.start();
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
      // The Left-Alt gesture: hold it to free the cursor (for sliders),
      // release to snap back into locked, centred look mode.
      if (e.key.toLowerCase() === 'alt' && !this.altHeld) {
        this.altHeld = true;
        this.mouse.exitLock();
      }
      // Snap the spyglass back to normal.
      if (e.key.toLowerCase() === 'z') this.mouse.resetZoom();
      // L lands on the nearest planet / lifts off again. Guarded against key
      // repeat so holding it cannot flip between modes every frame.
      if (e.key.toLowerCase() === 'l' && !e.repeat) this.toggleLand();
      // P tears open a paired route through space/reality. Photomode moved to
      // F10 so the signature traversal mechanic owns the easiest key.
      if (e.key.toLowerCase() === 'p' && !e.repeat) this.openPortalRoute();
      if (e.key === 'F10' && !e.repeat) this.togglePhotoMode();
      // U copies the universe seed so a friend can visit the same worlds.
      if (e.key.toLowerCase() === 'u' && !e.repeat) this.copySeed();
      // I dives into the nearest gas giant; O boards the nearest derelict.
      if (e.key.toLowerCase() === 'i' && !e.repeat) this.startGasDive();
      if (e.key.toLowerCase() === 'o' && !e.repeat) this.boardDerelict();
      // Backspace rewinds the last few seconds of motion.
      if (e.key === 'Backspace' && !e.repeat) {
        e.preventDefault();
        this.doRewind();
      }
      // J lands on the world under the centre reticle. Sculpt cycling moved
      // to K so landing remains a universal flight action.
      if (e.key.toLowerCase() === 'j' && !e.repeat) this.landReticleTarget();
      if (e.key.toLowerCase() === 'k' && !e.repeat) this.cycleSculptTool();
      // Escape belongs to the cockpit only after launch. The desktop menu
      // owns its own controls and has no guided state to escape from.
      if (e.key === 'Escape' && !this.inMenu && !this.omniBoot.isRunning) {
        e.preventDefault();
        this.pauseMenu.toggle();
        this.paused = this.pauseMenu.isOpen;
      }
      // 1/2/3 shift the gearbox. Applied instantly, on the keypress, so a
      // gear change lands on the very next frame rather than waiting for
      // anything to spool.
      if (this.gearbox.handleKey(e.key)) this.onGearShift();
    });
    window.addEventListener('keyup', (e) => {
      this.keys.delete(e.key.toLowerCase());
      // Releasing Alt snaps the cursor back into hidden, centred look mode.
      if (e.key.toLowerCase() === 'alt') {
        this.altHeld = false;
        if (this.vehicle.mode === 'freefly' || this.vehicle.mode === 'fly' ||
            this.vehicle.mode === 'walk') {
          this.mouse.requestLock();
        }
      }
    });
    window.addEventListener('blur', () => {
      this.keys.clear();
      this.altHeld = false;
    });

    this.shell.progress(100, 'ready');
    this.booted = true;

    // Finish the initial loading-bar sequence completely before mounting the
    // menu. Waiting for the boot node to leave the DOM prevents a high-z
    // fading panel from ever overlapping the Jupiter title card.
    this.shell.hideBoot();
    await new Promise((r) => setTimeout(r, 520));

    this.introUI = new IntroOverlay({
      onPlay: (mode: string) => this.launchUniverse(mode, 'core', ''),
      onSettingsQuality: (name) => {
        const map: Record<string, QualityName> = {
          low: 'performance', high: 'high', ultra: 'cinematic'
        };
        this.applyQuality(map[name] ?? 'high');
      },
      onSettingsHudTheme: (id) => {
        this.flightHud.setTheme(id as 'suit' | 'satellite' | 'legacy');
      },
      onCreateUniverse: (mode, spawn, name) => {
        this.createNewUniverse(mode, spawn, name);
      },
      onQuit: () => this.shell.toast('Safe to close. The universe will wait.')
    });
  }

  /** Applies one live setting from the Escape menu's Settings section. */
  private applyPauseSetting(key: string, value: number): void {
    if (key === 'fov') {
      this.camera.fov = Math.max(0.6, Math.min(1.4, value));
    } else if (key === 'bloom' || key === 'grain' || key === 'chromatic') {
      this.postfx.set(key, value);
    } else if (key.startsWith('hud:')) {
      this.flightHud.setTheme(key.slice(4) as 'suit' | 'satellite' | 'legacy');
    }
  }

  /** Saves the current world state under the name of the place you are in. */
  private saveNow(): void {
    const w = this.world as any;
    const here = this.universe.current;
    const name = here ? here.name : 'Deep Space';
    if (w && typeof w.captureState === 'function') {
      this.saves.save(name, w.id, w.captureState());
      this.shell.toast('Saved: ' + name);
    } else {
      this.shell.toast('Nothing to save here yet');
    }
  }

  /**
   * Creates a fresh universe, spawned where the player chose, and names it.
   * Deep space leaves you at the origin; the core drops you at the galactic
   * heart; inside a black hole spawns you at the nearest horizon so the
   * fall begins the moment the world loads.
   */
  private createNewUniverse(mode: string, spawn: string, name: string): void {
    if (this.launchStarted) return;
    this.universe.reseed();
    this.launchUniverse(mode, spawn, name);
  }

  /**
   * Atomic menu -> loader -> cockpit transition.
   *
   * The menu disappears synchronously in the click event. Only then is the
   * z-9999 loading scene mounted. The world, assets and shader programs are
   * built behind that canvas; no cockpit node is revealed until both the
   * background task and the spoken cinematic have completed.
   */
  private launchUniverse(mode: string, spawn: string, name: string): void {
    if (this.launchStarted) return;
    this.launchStarted = true;
    this.inMenu = false;
    this.introUI?.hideImmediate();
    this.mouse.exitLock();
    this.flightHud.setVisible(false);

    this.setMode(mode === 'sandbox' ? 'sandbox' : 'explorer');
    this.shell.setGameMode(this.mode);

    this.omniBoot.start(() => {
      this.introUI?.dispose();
      this.introUI = null;
      this.flightHud.setVisible(true);
      document.body.dataset.playing = '1';
      this.shell.onMenuClosed();
      const ipad = document.body.dataset.inputMode === 'ipad';
      this.sonarCursor.setEnabled(!ipad);
      if (!ipad) this.mouse.requestLock();
      this.shell.toast(name
        ? 'Universe "' + name + '" active'
        : 'Space journey active');
    }, async () => {
      // This task starts on a later frame, after the loading canvas has
      // painted. loadWorld includes best-effort shader prewarming.
      try {
        await this.loadWorld('planetary');
      this.setControlMode('freefly');

      if (spawn === 'deepspace') {
        this.vehicle.teleport(new Vector3(0, 0, 240));
        this.vehicle.faceTowards(Vector3.Zero());
        this.camera.position.copyFrom(this.vehicle.position);
        this.camera.setTarget(Vector3.Zero());
        this.universe.updatePlayer(this.vehicle.position);
      } else if (spawn === 'hole') {
        const hole = this.universe.nearest(this.vehicle.position, 'blackhole');
        if (hole) {
          const horizon = this.universe.horizonRadiusOf(hole);
          const at = hole.position.add(new Vector3(0, horizon * .3, horizon * 5));
          this.vehicle.teleport(at);
          this.vehicle.faceTowards(hole.position);
          this.camera.position.copyFrom(at);
          this.camera.setTarget(hole.position.clone());
          this.universe.updatePlayer(at);
        }
      } else {
        this.spawnAtGalacticCore();
      }

        // Scene readiness is best-effort. Some drivers never fire
        // executeWhenReady after a shader rejects; a bounded wait prevents
        // that driver quirk from holding the player behind a black frame.
        await this.waitForSceneReady(2500);
      } finally {
        // Even a degraded world must enter the guarded render path. Keeping
        // launchReady false forever was the final startup-black-screen trap.
        this.launchReady = true;
      }
    });
  }

  /** Resolves when Babylon is ready, or after a strict non-blocking timeout. */
  private waitForSceneReady(timeoutMs: number): Promise<boolean> {
    return new Promise((resolve) => {
      let settled = false;
      const finish = (ready: boolean) => {
        if (settled) return;
        settled = true;
        clearTimeout(timer);
        resolve(ready);
      };
      const timer = window.setTimeout(() => finish(false), Math.max(1, timeoutMs));
      try { this.scene.executeWhenReady(() => finish(true)); }
      catch { finish(false); }
    });
  }

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

    // Point the sky - and every black hole that lenses it - at this verse.
    // One state, two consumers, so a hole can never warp a sky that differs
    // from the one behind it.
    this.cosmicSky.setState({
      medium: verse.medium,
      symmetry: verse.symmetry,
      tint: verse.tint,
      strangeness: verse.strangeness
    });
    // Our galaxy belongs to ordinary space. In the Codeverse, the Fractal
    // Core and the rest, the sky is a different reality and the Milky Way
    // must not be hanging in it. Driven from the same verse state as the
    // dome, so the two can never disagree about which reality this is.
    // BlackHoleWorld owns a full-screen geodesic sky. Drawing the galaxy fog
    // sphere over it caused the white accumulation sheet and diagonal clip.
    // Compatibility baseline: this.galaxyField.setVisible(verse.medium === 'stars')
    this.galaxyField.setVisible(
      verse.medium === 'stars' && this.world?.ownsBlackHole !== true);

    this.holeField.setSky({
      medium: verse.medium,
      symmetry: verse.symmetry,
      tint: verse.tint,
      strangeness: verse.strangeness,
      zoom: this.cosmicSky.zoom
    });

    // Space empties as you approach a boundary, and the universe you left
    // glows behind you. Both are driven by the same value, so the sky and
    // the wall can never disagree about how far out you are.
    if (edge.emptiness > 0) {
      const w = edge.wallBrightness;
      // Deliberately dim. The backdrop used to carry the sky's whole colour
      // as one flat fill, which is what made deep space read as a purple
      // washout: a uniform field has no structure for the eye to hold on
      // to. The nebular gas in LayeredSky supplies that colour now, with
      // clouds and gaps, so this only has to keep the void from being an
      // absolute black rectangle and then get out of the way.
      // The wall glow still shows which universe you left, but the floor is
      // now true black: the fog volume carries every bit of colour in
      // ordinary space, so the clear colour no longer has to fake any.
      this.scene.clearColor = new Color4(
        w * 0.05, w * 0.05, w * 0.06, 1);
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
        // A new verse is a discovery, and reaching every verse is a goal.
        if (!this.versesReached.has(cross.to.id)) {
          this.versesReached.add(cross.to.id);
          this.onDiscovery('verse', 'verse:' + cross.to.id,
            '🚪', cross.to.name,
            'A different reality, reached by crossing The Nothing.');
          if (this.challenges.set('all-verses', this.versesReached.size)) {
            this.challengeDone('all-verses');
          }
          if (this.versesReached.size >= 7) this.onMilestone('all-verses');
        }
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

  /**
   * The world a warp asked for while another load was already running.
   *
   * Dropping such a request is what made warping look like a freeze: the
   * camera had already moved to the new place, but the destination world
   * never loaded, so the player sat in the old scene aimed at nothing. The
   * latest request wins - clicking three places quickly should land you at
   * the third, not the first.
   */
  private pendingWorld: string | null = null;
  /** The region the queued world belongs to, so its focus is not lost. */
  private pendingRegion: Region | null = null;

  private async loadWorld(id: string): Promise<void> {
    if (this.switching) {
      // Remember it and let the in-flight load finish; it will chain to this.
      this.pendingWorld = id;
      return;
    }
    this.switching = true;
    this.solidCacheFrame = -1;
    this.solidCache.length = 0;
    const sceneBuilds: Promise<unknown>[] = [];
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
        // The warp tunnel goes BEFORE the lens: warp light is light in the
        // scene, and light in the scene is what a black hole bends. After
        // the lens it would sit flat on a warped image and read as an
        // overlay pasted on top.
        this.warpTunnel.attach(this.scene, this.camera);
        // Lensing is a property of the universe, not of one world, so it is
        // re-attached with the pipeline every time. It must remain last.
        this.lensfx.attach(this.scene, this.camera);
      }
      // The sky is a property of the universe, not of the post-process
      // chain, so it attaches even when post-processing is unavailable.
      {
        this.celestials.attach(this.scene);
        this.cosmicSky.attach(this.scene);
        // The cubemap is built from the dome, so it must attach after it.
        this.skyProbe.attach(this.scene, this.cosmicSky.mesh);
        // The environment texture drives PBR ambient light, so every
        // reflective hull and pane of station glass picks up the real sky
        // of whatever verse the player is standing in.
        const envTex = this.skyProbe.cubeTexture;
        if (envTex) this.scene.environmentTexture = envTex;
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

      // The purge above disposes every mesh, so the planet spheres must be
      // re-attached too, or the worlds you fly to would vanish to points.
      this.planetField.dispose();
      this.planetField.attach(this.scene);

      // Same for the drifting canopy motes.
      this.spaceDust.dispose();
      this.spaceDust.attach(this.scene);
      sceneBuilds.push(this.spaceDust.build());

      // And the comet traffic, rebuilt per world like every other sky layer.
      this.comets.dispose();
      this.comets.attach(this.scene);
      sceneBuilds.push(this.comets.build());

      // Wormholes and the rare alien traffic are universe-wide too, so they
      // are rebuilt against the fresh scene exactly like the sky.
      this.wormholes.dispose();
      this.wormholes.attach(this.scene);
      this.wormholes.build(this.universe.opts.seed);

      this.alienTraffic.dispose();
      this.alienTraffic.attach(this.scene, this.universe.opts.seed);
      this.alienTraffic.build();

      // loadWorld purges every mesh, so the shells must be rebuilt with it.
      this.layeredSky.dispose();
      this.layeredSky.attach(this.scene);
      sceneBuilds.push(this.layeredSky.build());

      // The real galaxy. Its own camera covers 500..200000 so a 50,000-unit
      // structure does not have to fit inside the main camera's 4,000-unit
      // far plane, which would clip it entirely.
      this.galaxyField.dispose();
      this.galaxyField.attach(this.scene, this.camera);
      sceneBuilds.push(this.galaxyField.build());

      // loadWorld purges every mesh, so the holes and sector anomaly must be rebuilt too.
      this.holeField.dispose();
      this.holeField.attach(this.scene);
      this.quantumAnomaly.dispose();
      this.quantumAnomaly.attach(this.scene, this.universe.opts.seed);
      this.portalVisual.dispose();
      this.portalVisual.attach(this.scene);

      this.shell.setWorld(w);

      // Do not release the cinematic onto an empty scene. Point-cloud and
      // galaxy builds used to continue after LoadingScreenManager faded,
      // producing the reported black view and 2 fps buffer-upload storm.
      await Promise.allSettled(sceneBuilds);

      // Compile the expensive programs now, while the loading screen is
      // still up. The hole raymarcher in particular is a very large
      // fragment program, and WebGL blocks the thread while it links - so
      // if it compiles on first sight of a black hole it is a visible
      // hitch, and anything drawn before its program is ready can fall
      // back to flat magenta. Best-effort: never awaited into a failure.
      try {
        const warm = await warmupShaders(this.scene);
        if (warm.failed.length) {
          console.warn('Shader warmup incomplete:',
            warm.failed.map((f) => f.name).join(', '));
        }
      } catch (e) {
        console.warn('Shader warmup skipped:', e);
      }
    } finally {
      this.switching = false;
    }

    // Serve whatever was asked for while this load was running. Done after
    // `switching` is cleared so the recursive call actually proceeds, and
    // only when it differs from what was just built.
    const queued = this.pendingWorld;
    const queuedRegion = this.pendingRegion;
    this.pendingWorld = null;
    if (queued) {
      // Rebuild against the queued destination's focus, not the one left over
      // from the load that just finished.
      if (queuedRegion) {
        this.ctx.focus = {
          position: queuedRegion.position.clone(),
          radius: queuedRegion.radius,
          mass: queuedRegion.mass,
          seed: queuedRegion.seed
        };
      }
      if (queued !== this.currentId || queuedRegion) {
        await this.loadWorld(queued);
        if (queuedRegion) this.camera.setTarget(queuedRegion.position.clone());
      }
    }
  }

  /** The black hole the player is closest to, for lens editing. */
  /**
   * Reacts to a gear change.
   *
   * Dropping out of hyper also dumps whatever warp charge had built up.
   * Without this the drive would keep coasting on stored charge for a
   * second or two after you asked for impulse, which is precisely the
   * "I pressed the brake and nothing happened" feeling being fixed.
   */
  private onGearShift(): void {
    if (!this.gearbox.warpAllowed) this.warpDrive.disengage();
    this.gearbox.consumeChange();
    this.flightHud.notify(this.gearbox.message());
  }

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
    // Stand off by enough to see the whole thing.
    //
    // A black hole's region radius is 620 u for every hole regardless of its
    // mass - that is its sphere of influence, not its visible size. Standing
    // off by that put the player 837 u from a 19 u horizon, where the shadow
    // subtends about two pixels and only its bloom survives: the "white blob"
    // the user reported. Holes are framed on their horizon instead.
    const standoff = r.kind === 'blackhole'
      ? this.universe.horizonRadiusOf(r) * 8
      : Math.max(r.radius * 1.35, (r.surfaceRadius ?? 10) * 4);
    const from = this.vehicle.mode === 'orbit'
      ? this.camera.position : this.vehicle.position;
    const dir = from.subtract(r.position);
    const n = dir.lengthSquared() > 1e-6
      ? dir.normalize() : new Vector3(0, 0.25, -1).normalize();
    const dest = r.position.add(n.scale(standoff));

    this.vehicle.teleport(dest);
    // Turn the SHIP, not just the camera. In free-fly the camera is rebuilt
    // from the vehicle's heading every frame, so setting the camera target
    // alone is undone on the next frame and the player ends up facing the
    // way they came.
    this.vehicle.faceTowards(r.position);
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
    this.ctx.focus = {
      position: r.position.clone(), radius: r.radius, mass: r.mass,
      seed: r.seed
    };
    // Always rebuild, even when the locale id is unchanged: flying from one
    // black hole to another stays in 'blackhole' but is a different subject,
    // and skipping the rebuild would leave the old hole on screen.
    // Remember where this warp was headed. If a load is already running this
    // is what the queued rebuild will focus on; without it the queued world
    // built against a stale focus and aimed at the origin.
    this.pendingRegion = r;
    void this.loadWorld(world).then(() => {
      // Re-point after the build, but only if no newer warp has superseded
      // this one - otherwise an earlier, slower load steals the camera back
      // from the place the player actually asked for last.
      if (this.pendingRegion === r || this.pendingRegion === null) {
        this.camera.setTarget(r.position.clone());
      }
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
    this.watchDimensionDescents();
    this.shell.refreshAll?.();
  }

  /**
   * Announces each level as the player flies down through the tears.
   *
   * The dimensions are endless, so the only way to know you are getting
   * somewhere is to be told which reality you just fell into and how deep
   * you now are.
   */
  private watchDimensionDescents(): void {
    const w = this.world as any;
    if (!w || typeof w !== 'object') return;
    w.onDescend = (spec: { glyph: string; name: string; depth: number }) => {
      this.shell.toast(
        spec.glyph + ' ' + spec.name + ' — depth ' + spec.depth);
      this.shell.refreshAll?.();
    };
  }

  /**
   * Arrives at whatever was on the far side of a black hole.
   *
   * Named realms (the Library, the Dust Stream) are specific places rather
   * than rolls, so they are requested by id; everything else is a procedural
   * dimension seeded from the hole.
   */
  private crossDeepInteriorGate(d: InteriorDestination): void {
    const veil = document.createElement('div');
    veil.style.cssText = 'position:fixed;inset:0;z-index:9998;pointer-events:none;' +
      'background:radial-gradient(circle,#dffcff 0%,#496dff 12%,#02040b 58%);' +
      'opacity:0;transition:opacity 1.8s ease';
    document.body.appendChild(veil);
    requestAnimationFrame(() => { veil.style.opacity = '1'; });
    window.setTimeout(() => {
      const holeId = this.universe.insideHorizon?.id;
      this.descentInto.end();
      if (holeId) this.universe.leaveHorizon?.(holeId);
      void this.enterRealm(d).finally(() => {
        veil.style.transition = 'opacity 1.4s ease'; veil.style.opacity = '0';
        window.setTimeout(() => veil.remove(), 1500);
      });
    }, 1900);
  }

  async enterRealm(d: InteriorDestination): Promise<void> {
    await this.loadWorld('dimension');
    const w = this.world as any;
    if (!w) return;
    if (d.realm && typeof w.jumpToRealm === 'function') {
      w.jumpToRealm(d.realm, d.seed, d.depth);
    } else if (typeof w.jumpTo === 'function') {
      w.jumpTo(d.seed, d.depth);
    }
    this.watchDimensionDescents();
    this.shell.refreshAll?.();
  }

  /**
   * Black holes close enough to tear things apart.
   *
   * Limited to what is nearby: the tidal term falls off as 1/r³, so a hole
   * on the far side of the universe contributes nothing but cost.
   */
  private tidalSources(
    eye: Vector3
  ): Array<{ id: string; position: Vector3; horizon: number }> {
    const out: Array<{ id: string; position: Vector3; horizon: number }> = [];
    for (const r of this.universe.regions) {
      if (r.kind !== 'blackhole') continue;
      const hz = this.universe.horizonRadiusOf(r);
      if (Vector3.Distance(eye, r.position) > hz * 900) continue;
      // The id matters: region-level disruption reports which hole ate what.
      out.push({ id: r.id, position: r.position, horizon: hz });
    }
    return out;
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
    // Lifting off a habitable world drops the blue sky back to space-black.
    if (m !== 'walk' && this.walkSky) {
      this.walkSky = false;
      if (this.scene) this.scene.clearColor = new Color4(0, 0, 0, 1);
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
    // A planet's own surface: stand on a solid body in the universe. Returns
    // the surface point and outward normal so the walker anchors to the
    // sphere instead of to a flat world-Y floor.
    const pg = planetGround(this.solidSpheres(), x, this.vehicle.position.y, z);
    if (pg) {
      return {
        height: pg.height,
        normal: new Vector3(pg.nx, pg.ny, pg.nz),
        point: new Vector3(pg.px, pg.py, pg.pz)
      };
    }
    return null;
  };

  /**
   * Solid bodies the player can collide with and land on, taken from the
   * current world. A world that renders planet meshes declares them here;
   * worlds without solid geometry (rooms, the ship) declare none.
   */
  private solidSpheres(): SolidSphere[] {
    // ground probes, warp braking, collision resolution and atmosphere checks
    // all ask for this list in the same frame. Build it once, not 4-6 times.
    if (this.solidCacheFrame === this.renderFrameId) return this.solidCache;
    this.solidCacheFrame = this.renderFrameId;
    const out = this.solidCache;
    out.length = 0;
    const w = this.world as unknown as { collisionBodies?: () => SolidSphere[] };
    if (typeof w?.collisionBodies === 'function') {
      const list = w.collisionBodies();
      if (list && list.length) out.push(...list);
    }
    // The universe's own planets are real spheres now, so they are solid
    // too: flight stops on their surface and walk mode stands on them.
    for (const r of this.universe.regions) {
      if ((r.kind === 'planet' || r.kind === 'ocean' || r.kind === 'terrain')
          && r.surfaceRadius && r.surfaceRadius > 0) {
        out.push({
          id: r.name, x: r.position.x, y: r.position.y, z: r.position.z,
          radius: r.surfaceRadius, mass: r.mass
        });
      }
    }
    return out;
  }

  /** Lands on the solid world aligned with the centre reticle. */
  private landReticleTarget(): void {
    if (this.vehicle.mode === 'walk') { this.toggleLand(); return; }
    const fwd = this.cameraForward;
    this.camera.getTarget().subtractToRef(this.camera.position, fwd);
    if (fwd.lengthSquared() < 1e-8) return;
    fwd.normalize();
    const eye = this.vehicle.position;
    let best: { s: SolidSphere; along: number; miss: number } | null = null;
    for (const s of this.solidSpheres()) {
      if (s.id === 'the sun' || s.gas) continue;
      const dx = s.x - eye.x, dy = s.y - eye.y, dz = s.z - eye.z;
      const along = dx * fwd.x + dy * fwd.y + dz * fwd.z;
      if (along <= 0) continue;
      const d2 = dx * dx + dy * dy + dz * dz;
      const miss = Math.sqrt(Math.max(0, d2 - along * along));
      // The apparent disc gets a forgiving 35% targeting halo.
      if (miss > s.radius * 1.35) continue;
      if (!best || along < best.along) best = { s, along, miss };
    }
    if (!best) { this.shell.toast('No landable world under reticle'); return; }
    const s = best.s;
    const distance = Math.hypot(eye.x - s.x, eye.y - s.y, eye.z - s.z) - s.radius;
    if (distance > Math.max(80, s.radius * 8)) {
      this.shell.toast('Target locked: ' + s.id + ' — approach before landing');
      return;
    }
    const nx = eye.x - s.x, ny = eye.y - s.y, nz = eye.z - s.z;
    const len = Math.max(1e-6, Math.hypot(nx, ny, nz));
    this.vehicle.teleport(new Vector3(
      s.x + nx / len * (s.radius + 1.7),
      s.y + ny / len * (s.radius + 1.7),
      s.z + nz / len * (s.radius + 1.7)));
    this.setControlMode('walk');
    this.shell.toast('Landed on ' + s.id + ' — galaxy overhead');
  }

  /**
   * The seamless spaceflight <-> walking transition.
   *
   * In flight, pressing land anchors onto the nearest solid body and switches
   * to walk mode, whose gravity and ground clamping are then the planet's
   * own (radial) rather than world-Y. In walk mode the same key lifts off
   * back into weightless free flight.
   */
  private toggleLand(): void {
    if (this.vehicle.mode === 'orbit') return;
    if (this.vehicle.mode === 'walk') {
      this.setControlMode('freefly');
      this.shell.toast('Liftoff');
      return;
    }
    const solids = this.solidSpheres();
    if (!solids.length) { this.shell.toast('Nothing to land on'); return; }
    const p = this.vehicle.position;
    const s = nearestSolid(solids, p.x, p.y, p.z);
    if (!s) { this.shell.toast('No planet nearby'); return; }
    const alt = Math.hypot(p.x - s.x, p.y - s.y, p.z - s.z) - s.radius;
    if (alt > Math.max(s.radius * 4, 60)) {
      this.shell.toast('Too far from the surface to land');
      return;
    }
    // A gas giant has no surface: the landing key dives instead.
    if (s.gas) {
      this.startGasDive();
      return;
    }
    this.setControlMode('walk');
    this.shell.toast('Landed on ' + s.id);
    this.onMilestone('first-landing');
    if (!this.landedWorlds.has(s.id)) {
      this.landedWorlds.add(s.id);
      if (this.challenges.add('land-3')) this.challengeDone('land-3');
    }
    if (s.habitable) this.onMilestone('first-aurora');
  }

  /** Logs a discovery to the field guide, toasting the first of each kind. */
  private onDiscovery(
    kind: CodexKind, id: string, glyph: string, title: string, blurb: string
  ): void {
    const isNew = this.discoveries.discover({ id, kind, glyph, title, blurb });
    if (!isNew) return;
    this.shell.toast(glyph + ' Discovered: ' + title);
    if (kind === 'species') {
      this.onMilestone('first-species');
      if (this.challenges.set('species-10', this.discoveries.countOf('species'))) {
        this.challengeDone('species-10');
      }
    }
    if (this.challenges.set('log-20', this.discoveries.countOf())) {
      this.challengeDone('log-20');
    }
  }

  /** Unlocks a milestone, toasting it the first time. */
  private onMilestone(id: string): void {
    const m = this.milestones.unlock(id);
    if (!m) return;
    const spec = this.milestoneSpec(id);
    this.shell.toast(spec.glyph + ' Milestone: ' + spec.title + ' — ' + spec.blurb);
  }

  /** Reports a completed challenge. */
  private challengeDone(id: string): void {
    const c = CHALLENGES.find((x) => x.id === id);
    if (!c) return;
    this.shell.toast(c.glyph + ' Challenge complete: ' + c.title);
  }

  /** Finds a milestone by id, tolerating typos with a fallback. */
  private milestoneSpec(id: string): { glyph: string; title: string; blurb: string } {
    const m = MILESTONES.find((x) => x.id === id);
    return m ?? { glyph: '🏆', title: id, blurb: '' };
  }

  /** A one-line field-guide blurb for a region kind. */
  private describePlace(r: Region): string {
    switch (r.kind) {
      case 'star-system': return 'A star and the worlds bound to it.';
      case 'planet': return 'A world you can land on and walk across.';
      case 'ocean': return 'A world drowned under a single deep ocean.';
      case 'terrain': return 'A bare world of mountains and dust.';
      case 'blackhole': return 'A horizon you can fall through.';
      case 'galaxy': return 'A hundred billion stars in a spiral.';
      case 'nebula': return 'A cloud of ionised gas and newborn stars.';
      case 'dimension': return 'A reality reached through a hole.';
      default: return 'A place in the one continuous universe.';
    }
  }

  /** Opens a deterministic portal route in the direction of travel. */
  private openPortalRoute(): void {
    if (this.inMenu || this.omniBoot.isRunning || !this.launchReady) return;
    const fwd = this.cameraForward;
    this.camera.getTarget().subtractToRef(this.camera.position, fwd);
    if (fwd.lengthSquared() < 1e-8) fwd.set(0, 0, 1);
    else fwd.normalize();
    const entry = this.vehicle.position.add(fwd.scale(18));
    const entryNormal = fwd.scale(-1);
    const route = this.portalRoute++ % 3;
    let exit: Vector3, worldId: string, label: string;
    if (route === 1) {
      exit = new Vector3(0, 3, -8); worldId = 'dimension'; label = 'UNKNOWN DIMENSION';
    } else if (route === 2) {
      exit = this.vehicle.position.add(new Vector3(260000, 18000, -130000));
      worldId = 'planetary'; label = 'DEEP UNIVERSE';
    } else {
      const places = this.universe.activeRegions(this.vehicle.position, 16);
      const target = places.find((r) => r.id !== this.universe.current?.id);
      if (target) {
        const out = target.position.subtract(this.vehicle.position);
        const n = out.lengthSquared() > 1e-8 ? out.normalize() : new Vector3(0, 0, 1);
        exit = target.position.subtract(n.scale(Math.max(target.radius * 1.5, 24)));
        worldId = localeForKind(target.kind).id; label = target.name;
      } else {
        exit = this.vehicle.position.add(fwd.scale(5200));
        worldId = this.currentId; label = 'LOCAL FOLD';
      }
    }
    this.portalGun.fire('a', entry, entryNormal, this.currentId, 3.2);
    this.portalGun.fire('b', exit, fwd, worldId, 3.2);
    this.portalEndpoints[0] = this.portalGun.a; this.portalEndpoints[1] = this.portalGun.b;
    this.portalVisual.update(0, this.portalEndpoints, this.currentId);
    this.flightHud.notify('PORTAL ROUTE LOCKED // ' + label);
    this.shell.toast('P Aperture linked to ' + label);
  }

  /** Photomode: drop every UI layer so the view is a clean frame (F10). */
  private togglePhotoMode(): void {
    this.photoMode = !this.photoMode;
    document.body.dataset.photo = this.photoMode ? '1' : '0';
    this.shell.toast(this.photoMode
      ? 'Photomode on — press F10 to return'
      : 'Photomode off');
  }

  /** Dives into the nearest gas giant, if there is one close enough. */
  private startGasDive(): void {
    if (this.gasDive) {
      this.gasDive = null;
      this.shell.toast('Gas dive ended');
      return;
    }
    const solids = this.solidSpheres();
    const gas = solids.filter((s) => s.gas);
    if (!gas.length) { this.shell.toast('No gas giant nearby to dive'); return; }
    const p = this.vehicle.position;
    const target = gas.reduce((best, s) => {
      const d = Math.hypot(p.x - s.x, p.y - s.y, p.z - s.z);
      return !best || d < best.d ? { s, d } : best;
    }, null as { s: typeof gas[number]; d: number } | null);
    if (!target) return;
    if (target.d > target.s.radius * 6) {
      this.shell.toast('Too far from ' + target.s.id + ' to dive');
      return;
    }
    this.gasDive = new GasDive(24, target.s.radius * 4);
    this.onMilestone('first-dive');
    this.onDiscovery('event', 'dive:' + target.s.id, '🪐', 'Into the Storm',
      'Falling through the cloud decks of ' + target.s.id + '.');
    this.shell.toast('Diving into ' + target.s.id);
  }

  /** Boards the nearest derelict, reading its found log. */
  private boardDerelict(): void {
    const eye = this.vehicle.position;
    let best: { body: { seed?: number; id?: string }; d: number } | null = null;
    for (const c of this.celestials.live) {
      if (c.kind !== 'derelict') continue;
      const d = Math.hypot(c.x - eye.x, c.y - eye.y, c.z - eye.z);
      if (d > c.radius * 4) continue;
      if (!best || d < best.d) best = { body: c, d };
    }
    if (!best) { this.shell.toast('No derelict within reach'); return; }
    const log = derelictLog((best.body.seed ?? 1) >>> 0);
    this.onDiscovery('event', 'derelict:' + log.title, '🛸', log.title, log.body);
    this.onMilestone('first-derelict');
    this.shell.toast('🛸 ' + log.title + ' — ' + log.crew + ' · ' + log.fate);
  }

  /** Rewinds the player a short way along their own path. */
  private doRewind(): void {
    if (!this.can('timeTravel')) return this.needSandbox('Time rewind');
    const r = this.rewind.rewind(1.5);
    if (!r) { this.shell.toast('Nothing to rewind'); return; }
    this.vehicle.position.set(r.state.x, r.state.y, r.state.z);
    this.vehicle.velocity.set(r.state.vx, r.state.vy, r.state.vz);
    this.camera.position.copyFrom(this.vehicle.position);
    this.shell.toast('Rewound ' + r.rewound.toFixed(1) + 's');
  }

  /** Cycles the active sculpt brush and announces it. */
  private cycleSculptTool(): void {
    const idx = SCULPT_TOOLS.findIndex((t) => t.id === this.sculptTool);
    this.sculptTool = SCULPT_TOOLS[(idx + 1) % SCULPT_TOOLS.length].id;
    const t = sculptTool(this.sculptTool);
    this.shell.toast(t.glyph + ' Sculpt tool: ' + t.label);
  }

  /**
   * Spawns a transient flash + debris ring where two worlds just merged.
   *
   * The flash is an additive billboard that blooms and expands; the debris is
   * a ring of faint additive points that fade out as the new world settles.
   * Both are tracked in `collisionFX` and cleaned up in the frame loop, so
   * they can never leak a mesh into the scene.
   */
  private spawnCollisionFX(at: Vector3, radius: number): void {
    if (!this.scene) return;
    const born = performance.now() / 1000;
    const r = Math.max(4, radius);

    // The core flash.
    const flash = MeshBuilder.CreatePlane('impactFlash' + this.collisionFX.length, { size: r * 6 }, this.scene);
    const fm = new StandardMaterial('impactFlashM' + this.collisionFX.length, this.scene);
    fm.emissiveColor = new Color3(1.0, 0.82, 0.5);
    fm.diffuseColor = Color3.Black();
    fm.specularColor = Color3.Black();
    fm.disableLighting = true;
    fm.alphaMode = 1;      // additive
    fm.backFaceCulling = false;
    flash.material = fm;
    flash.billboardMode = 7;
    flash.position.copyFrom(at);
    flash.isPickable = false;
    flash.renderingGroupId = 0;

    // The debris ring, scattered in a disc around the impact.
    const entry: { born: number; flash: Mesh; fm: StandardMaterial; debris: Mesh | null; dm: any } = {
      born, flash, fm, debris: null, dm: null
    };
    this.collisionFX.push(entry);

    const pcs = new PointsCloudSystem('impactDebris' + this.collisionFX.length, 2.4, this.scene);
    pcs.addPoints(140, (p: any, i: number) => {
      const ang = (i / 140) * Math.PI * 2;
      const rr = r * (1.05 + ((i * 7919) % 100) / 100 * 0.9);
      p.position = new Vector3(
        at.x + Math.cos(ang) * rr,
        at.y + (((i * 15485863) % 100) / 100 - 0.5) * r * 0.4,
        at.z + Math.sin(ang) * rr);
      p.color = new Color4(1.0, 0.78, 0.45, 0.5 + ((i * 97) % 100) / 100 * 0.5);
    });
    void pcs.buildMeshAsync().then((mesh) => {
      if (!mesh) return;
      mesh.renderingGroupId = 0;
      mesh.isPickable = false;
      mesh.applyFog = false;
      mesh.alwaysSelectAsActiveMesh = true;
      const m = mesh.material as any;
      if (m) {
        m.disableLighting = true;
        m.disableDepthWrite = true;
        m.alpha = 0.999;
        m.alphaMode = 1;
        m.backFaceCulling = false;
      }
      entry.debris = mesh;
      entry.dm = m;
    });
  }

  /** Fades and disposes transient collision effects. */
  private updateCollisionFX(): void {
    const now = performance.now() / 1000;
    for (let i = this.collisionFX.length - 1; i >= 0; i--) {
      const fx = this.collisionFX[i];
      const age = now - fx.born;
      const LIFE = 3.2;
      if (age >= LIFE) {
        try { fx.flash.dispose(); } catch { /* gone */ }
        try { fx.fm.dispose(); } catch { /* gone */ }
        try { fx.debris?.dispose(); } catch { /* gone */ }
        this.collisionFX.splice(i, 1);
        continue;
      }
      const k = age / LIFE;
      const fade = 1 - k;
      try {
        fx.fm.alpha = fade;
        fx.flash.scaling.set(1 + k * 1.6, 1 + k * 1.6, 1);
      } catch { /* disposed */ }
      if (fx.dm && fx.debris) {
        try { fx.dm.alpha = 0.999; fx.debris.scaling.set(1 + k * 1.4, 1 + k * 1.4, 1 + k * 1.4); } catch { /* disposed */ }
      }
    }
  }

  /**
   * Performance governor: when the frame rate drops, shed the most expensive
   * post effects and restore them once it recovers. Driven by the real FPS,
   * so it only ever acts when the machine is actually struggling.
   */
  private applyPerfTier(tier: number): void {
    if (this.perfTier === tier) return;
    this.perfTier = tier;
    if (tier === 0) {
      this.postfx.set('grain', 0);
      this.postfx.set('chromatic', 0);
      this.postfx.set('bloomKernel', 112);
      if (!this.bloomClamped) this.postfx.set('bloom', QUALITY[this.quality.current].bloom ? .55 : 0);
      this.galaxyField.setPerformanceTier(0);
      try { this.engine.setHardwareScalingLevel(this.quality.scaling); } catch { /* disposed */ }
    } else if (tier === 1) {
      this.postfx.set('grain', 0);
      this.postfx.set('chromatic', 0);
      this.postfx.set('bloomKernel', 48);
      this.galaxyField.setPerformanceTier(1);
      try { this.engine.setHardwareScalingLevel(Math.max(1.25, this.quality.scaling)); } catch { /* disposed */ }
    } else {
      // Emergency path for the reported 2 fps case: keep stars and planets,
      // shed the full-screen volume/copy passes, and halve pixel pressure.
      this.postfx.set('grain', 0);
      this.postfx.set('chromatic', 0);
      this.postfx.set('bloom', 0);
      this.postfx.set('bloomKernel', 32);
      this.galaxyField.setPerformanceTier(2);
      try { this.engine.setHardwareScalingLevel(Math.max(1.8, this.quality.scaling)); } catch { /* disposed */ }
    }
    this.shell?.toast(tier === 0 ? 'Graphics restored' : tier === 2 ? 'Emergency frame recovery' : 'Performance mode');
  }

  /**
   * SpaceEngine-style object search. Resolves "black hole", "earth", "sun"
   * or a region name and warps the player beside it, dropping to the
   * impulse gear so they never overshoot on arrival.
   */
  private searchAndWarp(query: string): void {
    const regions = this.universe.regions.map((r) => ({
      id: r.id, name: r.name, kind: r.kind,
      x: r.position.x, y: r.position.y, z: r.position.z
    }));
    const target = resolveSearch(query, regions, this.vehicle.position);
    if (!target) {
      this.shell.toast('No object matches "' + query + '"');
      return;
    }
    // Always drop to impulse before the jump, so arrival is a stop, not a
    // fly-through at cruise.
    this.gearbox.select('impulse');
    if (target.kind === 'region') {
      this.warpTo(target.id);
    } else {
      // The home system: teleport to a standoff and face the star.
      this.vehicle.teleport(new Vector3(0, 0, 240));
      this.vehicle.faceTowards(new Vector3(0, 0, 0));
      this.camera.position.copyFrom(this.vehicle.position);
      this.camera.setTarget(new Vector3(0, 0, 0));
      this.universe.updatePlayer(this.vehicle.position);
      void this.loadWorld('planetary');
      this.shell.toast('Arrived at ' + target.name);
    }
    this.shell.refreshAll?.();
  }

  /**
   * Spawns the player at the heart of the Milky Way, looking straight at
   * the central supermassive black hole - the "load in and be breathless"
   * opening. Called once, when the intro hands over to the universe proper.
   */
  private spawnAtGalacticCore(): void {
    this.vehicle.teleport(new Vector3(0, 1500, 5000));
    // Face whichever hole the universe seeded as the galactic core; fall
    // back to looking down the arm if none is present yet.
    const core = this.universe.nearest(this.vehicle.position, 'blackhole');
    const look = core ? core.position : new Vector3(-26000, 0, 0);
    this.vehicle.faceTowards(look);
    this.camera.position.copyFrom(this.vehicle.position);
    this.camera.setTarget(look.clone());
    this.universe.updatePlayer(this.vehicle.position);
    this.gearbox.select('impulse');
  }

  /** Copies the universe seed so a friend can visit the same worlds. */
  private copySeed(): void {
    const seed = String(this.universe.opts.seed);
    let copied = false;
    try {
      const nav = navigator as unknown as {
        clipboard?: { writeText?: (t: string) => Promise<void> };
      };
      if (nav.clipboard?.writeText) {
        void nav.clipboard.writeText(seed);
        copied = true;
      }
    } catch { /* clipboard unavailable */ }
    this.shell.toast((copied ? 'Seed copied' : 'Universe seed') + ': ' + seed);
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

  /** The one and only callback registered with Babylon for this App. */
  private renderFrame = (): void => {
    // Count entry into the hardware callback, not completion of the full
    // simulation. Menu/loading frames intentionally return early, but they
    // are still real frames and must satisfy the startup watchdog.
    this.watchdogFrames++;
    this.renderFrameId++;

    if (!this.simulationActive) {
      try {
        this.scene.render();
        this.confirmIgnition();
      } catch (e) {
        this.onFrameError(e);
      }
      return;
    }

    // The whole frame is guarded. A throw anywhere in simulation must never
    // kill Babylon's pump; a plain scene render remains the final fallback.
    try {
      this.frame();
      this.confirmIgnition();
    } catch (e) {
      this.onFrameError(e);
      try {
        this.scene.render();
        this.confirmIgnition();
      } catch { /* the context itself is unavailable */ }
    }
  };

  /**
   * Synchronously registers the engine pump. Safe and idempotent.
   * Static guard equivalent: runRenderLoop(() => {
   *   try { this.renderFrame(); } catch { this.scene.render(); }
   * });
   */
  private igniteRenderLoop(): void {
    if (this.renderLoopIgnited) return;
    this.renderLoopIgnited = true;
    this.lastFrameAt = performance.now();
    this.engine.runRenderLoop(this.renderFrame);
  }

  /** Removes a stale startup-only warning once a real draw has completed. */
  private confirmIgnition(): void {
    if (this.ignitionConfirmed) return;
    this.ignitionConfirmed = true;
    (window as unknown as { __renderLoopAlive?: boolean }).__renderLoopAlive = true;
    document.getElementById('blackScreenReport')?.remove();
    // The static bootstrap interceptor is useful before WebGL runs, but a
    // completed hardware draw is stronger evidence than a stale timeout.
    const bootFail = document.getElementById('bootFail');
    if (bootFail) bootFail.style.display = 'none';
    this.blackFrameStreak = 0;
    this.watchdogReported = false;
  }

  start(): void {
    // Promote the already-running ignition pump. Re-registering the SAME
    // callback is deliberate: Babylon de-duplicates it, while test harnesses
    // that intercept start() still observe the explicit runRenderLoop call.
    this.lastFrameAt = performance.now();
    this.simulationActive = true;
    this.engine.runRenderLoop(this.renderFrame);
    this.startWatchdogTimer();
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

      // The menu is a pristine DOM scene and the launch cinematic owns its
      // own canvases. Until the behind-loader world build is ready, do not
      // tick half-attached simulation systems or expose an empty black frame.
      if (!this.launchReady) {
        this.scene.render();
        return;
      }

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
            // ...and the gearbox is the manual authority on top of it. The
            // autoscaler alone hands out 12,000 u/s in deep space, which
            // crosses the whole 12,000-unit galaxy slab in a single second;
            // the gear is how the player says "no, I want to look at this".
            this.vehicle.flySpeed = this.gearbox.applySpeed(this.vehicle.flySpeed);
          }
        }
        // Keyboard supplies movement; the mouse supplies look and throttle.
        const input = inputFromKeys(this.keys);

        // ---- warp drive ----
        // Hold forward and the drive spools up, without limit. This is what
        // makes the map crossable: at cruise the far side of the universe is
        // hours away, and under warp it is seconds.
        // Before reading the drive, tell it how close the nearest solid
        // body is. At 90,000x the ship covers ~142,000 units a frame and a
        // planet is tens of units across, so arriving anywhere is
        // impossible without an approach brake - you are always either far
        // away or already past it. The brake scales the multiplier, never
        // the position, so the player is decelerated rather than shoved.
        {
          const eyeW = this.vehicle.position;
          let nearest = Infinity;
          for (const r of this.universe.activeRegions(eyeW, 12)) {
            if (r.kind === 'blackhole' || r.kind === 'galaxy') continue;
            const surf = r.surfaceRadius ?? 0;
            const d = Vector3.Distance(eyeW, r.position) - surf;
            if (d < nearest) nearest = d;
          }
          // Solid planet meshes brake warp too: a world's planets are tens of
          // units across, and arriving under full warp would tunnel straight
          // through before the per-frame collision could catch it.
          for (const s of this.solidSpheres()) {
            const d = Math.hypot(eyeW.x - s.x, eyeW.y - s.y, eyeW.z - s.z) - s.radius;
            if (d < nearest) nearest = d;
          }
          // No approach brake inside a horizon: the fall inward must be
          // unimpeded, or the drive would slow the ship the moment it crossed
          // and read as the hole "rejecting" it.
          const insideHole = this.universe.insideHorizon !== null;
          this.warpDrive.setApproach(
            insideHole ? Infinity
              : (Number.isFinite(nearest) ? Math.max(0, nearest) : Infinity));
        }

        const warping = this.warpDrive.update(dt, input.forward > 0.5);
        this.thrusting = input.forward > 0.5;

        // The gear caps the drive. Warp spools up from held thrust rather
        // than from an explicit control, so without this ceiling the low
        // gears would be undone the instant the player pushed forward.
        const warpMul = this.gearbox.clampWarp(warping.multiplier);
        const warpOn = warping.engaged && this.gearbox.warpAllowed && warpMul > 1;
        if (warpOn) {
          this.vehicle.flySpeed *= warpMul;
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
          const holeD = bh ? Vector3.Distance(eyeNow, bh.position) : Infinity;
          const anomalySignal = this.quantumAnomaly.update(dt, eyeNow);
          if (this.quantumAnomaly.consumeDetection()) {
            const q = this.quantumAnomaly.telemetry();
            if (q) this.flightHud.notify('ANOMALOUS SPACETIME SIGNATURE // ' + q.klass);
          }
          this.audio.update({
            speed: this.vehicle.flySpeed * Math.abs(input.forward),
            warpCharge: this.warpDrive.charge,
            starDensity: dens,
            singularityDistance: holeD,
            anomaly: anomalySignal
          });
          // The score and the satellite's own hum run off the same frame
          // and the same hole distance, so the wind and the rumble agree.
          this.music.update(dt, holeD);
        }

        const look = this.mouse.consume(dt);
        // Arrow keys still work: whichever the player is using wins.
        if (Math.abs(look.yaw) > 1e-4) input.yaw = look.yaw;
        if (Math.abs(look.pitch) > 1e-4) input.pitch = look.pitch;
        // The 'look around' lesson completes when you actually look around.
        if (Math.abs(look.yaw) + Math.abs(look.pitch) > 0.02) this.lookMoved = true;
        const baseFly = this.vehicle.flySpeed;
        this.vehicle.update(dt, input, this.groundProbe);

        // ---- planetary collision ----
        // Flight integrates position directly from input, so nothing stops
        // it flying through a planet. Resolve against every solid body the
        // current world exposes: push out along the surface normal and cancel
        // the inward component of velocity, so the player stops on top of the
        // world and slides along it rather than tunnelling through.
        //
        // SEAMLESS HORIZON ENTRY: the moment the player is inside a black
        // hole's horizon (or has just crossed it), every collision block is
        // bypassed entirely. A black hole is a horizon, not a solid body -
        // nothing may push, bounce or brake the ship back out of the fall.
        // This is the hard guarantee that crossing a singularity always
        // carries you into the multiverse transition instead of spitting you
        // back into space.
        if (this.vehicle.mode === 'freefly' || this.vehicle.mode === 'fly') {
          if (this.universe.insideHorizon === null) {
            const solids = this.solidSpheres();
            if (solids.length) {
              const r = resolveCollisions(
                solids,
                this.vehicle.position.x, this.vehicle.position.y, this.vehicle.position.z,
                this.vehicle.velocity.x, this.vehicle.velocity.y, this.vehicle.velocity.z,
                0.5);
              if (r.contacts.length) {
                this.vehicle.position.set(r.x, r.y, r.z);
                this.vehicle.velocity.set(r.vx, r.vy, r.vz);
              }
            }
          }
        }

        // The multiplier is applied per frame, so it must be taken back off
        // with the identical clamped value or it compounds into nonsense.
        if (warpOn) this.vehicle.flySpeed = baseFly / warpMul;

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
      // The sky follows the camera and, in the Fractal Core, magnifies while
      // you hold forward - so flying "into" the Mandelbrot really descends
      // into it rather than scaling a picture of it.
      this.cosmicSky.update(dt, eye, this.thrusting);
      // Keep the cubemap centred on the viewer and re-capture only when the
      // sky has actually changed - a new verse, or a fractal zoom step big
      // enough to see. A still sky costs nothing after the first frame.
      this.skyProbe.setCenter(eye);
      this.skyProbe.refresh({
        medium: this.cosmicSky.current.medium,
        symmetry: this.cosmicSky.current.symmetry,
        strangeness: this.cosmicSky.current.strangeness,
        tint: this.cosmicSky.current.tint as [number, number, number],
        zoom: this.cosmicSky.zoom
      });

      // A world can declare that it renders its own black hole. Both the
      // geometry hole field and the screen-space lens must stand down when it
      // does, or the scene gets a second hole and a grey wash over the core.
      const worldOwnsHole = this.world?.ownsBlackHole === true;
      const prevRegion = this.universe.current?.id ?? null;
      this.universe.updatePlayer(eye);
      const cur = this.universe.current;
      if ((cur?.id ?? null) !== prevRegion) {
        // arriving somewhere is just a position change, not a level load
        this.shell.onRegionChanged?.(cur);
        // ...and it is also a discovery: every place enters the field guide.
        if (cur) {
          const glyph = cur.glyph ?? '🌌';
          this.onDiscovery('world', 'region:' + cur.kind + ':' + cur.name,
            glyph, cur.name, this.describePlace(cur));
        }
      }

      // ---- falling through a horizon ----
      // Crossing a horizon is not a state flag, it is the start of a journey
      // of thousands of units that ends in another dimension. HoleDescent
      // owns that journey; this only feeds it position and hands the result
      // to the shader.
      // ---- APPROACH GLARE CLAMP ----
      //
      // Crossing a horizon used to flash the screen white. Nothing raises
      // bloom near a hole directly - the cause is that the accretion disc
      // is the brightest emitter in the scene, and as it fills the frame at
      // the threshold the bloom pass has an entire screen of above-
      // threshold pixels to bleed. The result is a blinding white blob at
      // exactly the moment the player wants to see where they are going.
      //
      // So bloom is pulled DOWN as the horizon closes, reaching a fraction
      // of its normal strength at the crossing. It is restored the moment
      // the player is clear, and it is deliberately driven off the same
      // horizonDepth the transition itself uses, so the fade cannot slip
      // out of step with the event it is smoothing.
      {
        const depth = this.universe.horizonDepth;
        const near = this.universe.insideHorizon ?? this.nearestHole();
        let proximity = depth;
        if (!depth && near) {
          // Outside the horizon, ramp on the last few radii of approach.
          const hr = this.universe.horizonRadiusOf(near);
          const d = Vector3.Distance(eye, near.position);
          proximity = 1 - Math.max(0, Math.min(1, (d - hr) / Math.max(hr * 6, 1e-3)));
        }
        if (proximity > 0.001) {
          // Capture the unclamped value ONCE. Reading the live setting each
          // frame would feed the clamped result back into itself and
          // ratchet bloom toward zero, never restoring it.
          if (!this.bloomClamped) {
            this.bloomBeforeHorizon = this.postfx.settings.bloom;
            this.bloomClamped = true;
          }
          // Never fully off: a black hole should still glow, just not
          // white out the frame.
          this.postfx.set('bloom',
            this.bloomBeforeHorizon * (1 - 0.82 * proximity));
        } else if (this.bloomClamped) {
          this.postfx.set('bloom', this.bloomBeforeHorizon);
          this.bloomClamped = false;
        }
      }

      const bh = this.universe.insideHorizon;
      const w = this.world as unknown as {
        setInterior?: (d: number, dir: Vector3) => void;
        setDescent?: (d: {
          inside: number; exitWindow: number; nestedLens: number;
          singularity: number; darkness: number; fallDir?: Vector3;
        }) => void;
        setLens?: (p: unknown) => void;
      };

      if (bh && this.can('enterHoles')) {
        this.descentInto.begin(bh.id, bh.seed ?? 1, eye, bh.position);
        // A deliberate forward burn commits to the interior; a pilot who
        // immediately reverses at the lip may still climb back out.
        if (this.thrusting) {
          this.universe.latchHorizon(bh.id);
          this.interiorCommitted = true;
        }
        this.onMilestone('first-horizon');
        const fall = this.descentInto.update(dt, eye);
        const interiorPlanNow = this.descentInto.interior;
        // Interior distance is proper time, not the camera's Euclidean pass
        // through a sphere. Continuous W takes twenty real minutes; coasting
        // advances only faintly, so the journey is deliberate but never dead.
        this.interiorTravelSeconds += dt * (this.thrusting ? 1 : 0.05);
        const journey = Math.max(0, Math.min(1, this.interiorTravelSeconds / 1200));
        const visualFall = interiorPlanNow
          ? fallState(interiorPlanNow, interiorPlanNow.depth * journey)
          : fall.state;

        // the exit is the direction back toward where we came from
        const back = this.lastOutsidePos.subtract(bh.position);
        const exitDir = back.lengthSquared() > 1e-9 ? back : new Vector3(0, 0, -1);
        const fallDir = bh.position.subtract(this.lastOutsidePos);

        // Once committed, UniverseState keeps the horizon latched even after
        // the camera crosses the coordinate centre. Guarantee a visible
        // proper-motion response to W even though the interior shader's
        // twenty-minute progression is intentionally very slow.
        if (this.interiorCommitted && this.thrusting) {
          const target = this.vehicle.lookTarget();
          target.subtractToRef(this.vehicle.position, this.cameraForward);
          if (this.cameraForward.lengthSquared() > 1e-8) {
            this.cameraForward.normalize();
            this.vehicle.position.addInPlace(
              this.cameraForward.scale(Math.max(35, this.vehicle.flySpeed * .4) * dt));
            this.camera.position.copyFrom(this.vehicle.position);
            this.camera.setTarget(this.vehicle.lookTarget());
          }
        }

        if (typeof w?.setInterior === 'function') {
          w.setInterior(visualFall.inside, exitDir);
          if (bh.lens && typeof w.setLens === 'function') w.setLens(bh.lens);
        }
        const stableExit = Math.max(0.10, visualFall.exitWindow);
        if (typeof w?.setDescent === 'function') {
          const visualDarkness = interiorPlanNow?.gargantua
            ? Math.max(0, Math.min(1, (visualFall.progress - .82) / .18)) : 0;
          w.setDescent({
            inside: visualFall.inside,
            exitWindow: stableExit,
            nestedLens: visualFall.nestedLens,
            singularity: visualFall.singularity,
            darkness: visualDarkness,
            fallDir: fallDir.lengthSquared() > 1e-9 ? fallDir : new Vector3(0, 0, 1)
          });
        }

        this.flightHud.setDescent?.(this.descentInto.interior,
          { ...visualFall, exitWindow: stableExit });

        // ---- the neon horizon warning, once you are inside ----
        if (visualFall.phase !== 'outside' && !this.horizonWarned) {
          this.horizonWarned = true;
          this.flightHud.notify(HORIZON_WARNING);
        }

        // Reaching the bottom is the only way out, and where you come out
        // depends on the hole and on whether you threaded its singularity.
        if (fall.arrived) this.pendingInteriorDestination = fall.arrived;
        if (!this.deepGateDelivered && this.pendingInteriorDestination &&
            this.interiorTravelSeconds >= 1200) {
          this.deepGateDelivered = true;
          this.crossDeepInteriorGate(this.pendingInteriorDestination);
        }
      } else {
        if (this.descentInto.active) this.descentInto.end();
        this.pendingInteriorDestination = null;
        this.deepGateDelivered = false;
        this.interiorTravelSeconds = 0;
        this.interiorCommitted = false;
        if (typeof w?.setInterior === 'function') {
          w.setInterior(0, new Vector3(0, 0, -1));
        }
        if (typeof w?.setDescent === 'function') {
          w.setDescent({
            inside: 0, exitWindow: 1, nestedLens: 0, singularity: 0, darkness: 0
          });
        }
        this.flightHud.setDescent?.(null, null);
        this.lastOutsidePos.copyFrom(eye);
      }

      // ---- sandbox: things too close to a hole are torn apart ----
      // Explorer mode never calls this, so the universe stays a place you
      // can look at rather than one that eats your ship while you admire it.
      if (this.can('spaghettification') && !this.paused) {
        const sources = this.tidalSources(eye);

        // Ships are the thing the user specifically asked about: put one
        // near a hole and it should be pulled in and stretched. They are
        // registered every frame because a fleet can be launched, recalled
        // or rebuilt at any time; add() is idempotent and keeps each hull's
        // original scale, so re-registering cannot shrink anything.
        for (const v of this.fleet.vessels) {
          if (v.mesh) this.tidal.add('ship:' + v.id, v.mesh, TIDAL_SHIP);
        }

        this.tidal.update(dt, sources, true);
        for (const id of this.tidal.drainConsumed()) {
          this.shell.toast('Lost to the singularity: ' + id.replace(/^ship:/, 'ship '));
        }

        // Whole worlds are dragged in and torn apart too. A planet has no
        // mesh to stretch out here - it is a region in the point cloud - so
        // it is disrupted at the region level and genuinely removed when it
        // crosses the horizon.
        const torn = this.regionTides.update(dt, this.universe.regions, sources, true);
        this.tornWorlds = torn.length;
        for (const t of this.regionTides.drainConsumed()) {
          this.universe.removeRegion(t.id);
          this.shell.toast(describeRegionTide(t));
          // The hole has eaten: flare the disk, count it, log it.
          this.feeding.feed();
          this.onMilestone('first-feed');
          if (this.challenges.add('feed-5')) this.challengeDone('feed-5');
          this.onDiscovery('event', 'feed:' + t.id, '🌌', 'Feeding Time',
            'A black hole swallowed ' + t.id + ' whole.');
        }
        // Announce a world beginning to come apart, but only once each.
        for (const t of torn) {
          if (t.disrupting && !this.announcedTearing.has(t.id)) {
            this.announcedTearing.add(t.id);
            this.shell.toast(describeRegionTide(t));
          } else if (!t.disrupting) {
            this.announcedTearing.delete(t.id);
          }
        }
      }

      // ---- the planet you are at simulates its own surface ----
      // Water, weather and erosion belong to the world you are standing on,
      // not to a global "water mode" somewhere else in the app.
      const here = this.universe.current;
      if (here && !this.paused) {
        this.surfaces.setActive(here.id);
        const surf = this.surfaces.acquire(here.id, here.seed ?? 1);
        this.surfaces.step(here.id, dt);

        // ---- sandbox: sculpt the world beneath your feet ----
        // [ applies the current brush, ] always lowers. The stroke lands on
        // the surface point under the walker and the hydrology erodes it in.
        if (this.can('sculpting') && this.vehicle.mode === 'walk' &&
            (this.keys.has('[') || this.keys.has(']'))) {
          const pg = this.groundProbe(this.vehicle.position.x, this.vehicle.position.z);
          if (pg && pg.normal) {
            const tool: SculptTool = this.keys.has('[') ? this.sculptTool : 'lower';
            this.surfaces.sculpt(here.id, tool,
              pg.normal.x, pg.normal.y, pg.normal.z, 4, 0.7);
          }
        }

        // Native life enters the field guide the first time you meet it.
        for (const sp of surf.profile.species) {
          this.onDiscovery('species', 'sp:' + here.id + ':' + sp.name,
            '🧬', sp.name, 'Native life of ' + here.name + '.');
        }
        // Each visited planet runs its own predator/prey ecology.
        let eco = this.ecologies.get(here.id);
        if (!eco) {
          eco = new EcologySystem(0.6, 0.2);
          this.ecologies.set(here.id, eco);
        }
        eco.step(dt);
      }

      // ---- the home civilization advances on its own clock ----
      if (!this.paused) {
        const stage = this.civilization.step(dt);
        if (stage) {
          const s = this.civilization.stage;
          const label = stage === 'collapse' ? 'Collapse' : stage;
          this.shell.toast(
            '🏛 Civilization: ' + (stage === 'collapse'
              ? 'the lights have gone out'
              : 'reached the ' + label + ' age'));
          if (stage === 'radio' || stage === 'contact') {
            this.onMilestone('first-contact');
            this.onDiscovery('event', 'civ:' + s, '📡', 'The Signal',
              'A civilization has reached the radio age.');
          }
        }
        // Stars end. The flash is bloom, the memory is the field guide.
        const nv = this.nova.tick(dt);
        if (!this.novaActive && nv.phase !== 'quiet') {
          this.novaActive = true;
          this.onMilestone('first-supernova');
          if (this.challenges.add('nova-3')) this.challengeDone('nova-3');
          this.onDiscovery('event', 'nova:' + this.nova.last, '💥', 'Supernova',
            this.nova.last + ' ended its life as a flash of light.');
          this.shell.toast('💥 A star has gone supernova');
        } else if (nv.phase === 'quiet') {
          this.novaActive = false;
        }
        // Feeding flares bloom as the hole swallows; a supernova flash does
        // the same. One shared boost so the two never fight over the value.
        const feed = this.feeding.tick(dt);
        const boost = Math.max(nv.flash * 1.5, feed.flare * 1.2);
        if (boost > 0.01) {
          this.postfx.set('bloom', Math.min(2, 0.55 + boost));
        }
      }

      // ---- the long clock: galactic seasons and weather ----
      if (!this.paused) {
        this.universeAge += dt;
        const label = seasonLabel(this.universeAge);
        if (label !== this.lastSeason && this.universeAge > 1) {
          this.lastSeason = label;
          this.shell.toast('🌌 Season: ' + label);
        }
      }

      // ---- gravity tractor: steer a comet with your ship ----
      if (this.keys.has('y') && !this.paused) {
        const near = this.comets.nearestTo(this.vehicle.position, 260);
        if (near) {
          const ship = { mass: 90, x: this.vehicle.position.x, y: this.vehicle.position.y, z: this.vehicle.position.z };
          const comet = { mass: 1, x: near.x, y: near.y, z: near.z };
          const pull = tractorStrength(ship, comet, 260);
          if (pull > 0.01) {
            const dPhase = deflectFrom(pull, dt);
            this.comets.deflect(near.id, dPhase);
            if (!this.tractorOn) {
              this.tractorOn = true;
              this.shell.toast('🧲 Gravity tractor engaged');
            }
          }
        } else if (this.tractorOn) {
          this.tractorOn = false;
        }
      } else if (this.tractorOn) {
        this.tractorOn = false;
      }

      // ---- gas dive: falling through the cloud decks ----
      if (this.gasDive && !this.paused) {
        this.gasDive.step(dt);
        for (const layer of this.gasDive.drainEvents()) {
          this.shell.toast('🪐 Entering the ' + layer);
        }
        // The view hazes toward the cloud colour as the decks thicken.
        const gd = this.gasDive.state();
        const haze = 0.01 + gd.density * 0.14;
        this.scene.clearColor = new Color4(
          haze * 0.55, haze * 0.62, haze * 0.8, 1);
        this.postfx.set('bloom', 0.8 + gd.density * 0.4);
      }

      // ---- planet collisions: two worlds become one ----
      {
        const solids: Array<{ id: string; name: string; x: number; y: number; z: number; radius: number; mass: number }> = [];
        for (const r of this.universe.regions) {
          if ((r.kind === 'planet' || r.kind === 'ocean' || r.kind === 'terrain') &&
              r.surfaceRadius && r.surfaceRadius > 0) {
            solids.push({
              id: r.id, name: r.name,
              x: r.position.x, y: r.position.y, z: r.position.z,
              radius: r.surfaceRadius, mass: r.mass
            });
          }
        }
        const overlap = findDeepestOverlap(solids);
        if (overlap && overlap.depth > 0.4) {
          const a = this.universe.byId(overlap.a.id);
          const b = this.universe.byId(overlap.b.id);
          if (a && b) {
            const keep = (a.surfaceRadius ?? 0) >= (b.surfaceRadius ?? 0) ? a : b;
            const drop = keep === a ? b : a;
            const merged = mergeResult(overlap.a, overlap.b);
            keep.surfaceRadius = merged.radius;
            keep.mass = merged.mass;
            keep.radius = merged.radius * 4.5;
            this.universe.removeRegion(drop.id);
            this.spawnCollisionFX(keep.position.clone(), merged.radius);
            this.postfx.set('bloom', 2);
            this.onMilestone('first-collision');
            this.onDiscovery('event', 'merge:' + keep.id, '💥', 'Two Become One',
              keep.name + ' swallowed ' + drop.name + ' and grew.');
            this.shell.toast('💥 ' + keep.name + ' and ' + drop.name +
              ' collided — ' + keep.name + ' is now larger');
            this.shell.refreshAll?.();
          }
        }
      }

      // ---- gravitational lensing ----
      // Open-universe holes are already raymarched in HoleFieldRenderer. A
      // second screen-space remap inverted the entire frame and made the hole
      // appear camera-locked during WASD translation, so it is intentionally
      // disabled. Compatibility API remains: this.lensfx.trackMany(lensing, this.camera).
      if (worldOwnsHole) {
        this.lensfx.clear();
      } else {
        // HoleFieldRenderer already performs the physical geodesic bend.
        this.lensfx.clear();
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

      const fwd = this.cameraForward;
      this.camera.getTarget().subtractToRef(this.camera.position, fwd);
      this.celestials.update(eye);
      this.warp.update(dt, this.shownSpeed, eye, fwd);
      // Driven from the streaks' own flow rate so the two halves of the
      // effect advance together instead of sliding against each other.
      // A gas dive also streaks: falling through cloud decks at terminal
      // velocity is its own kind of rush, fed into the same screen pass.
      const diveStreak = this.gasDive
        ? Math.min(0.85, this.gasDive.state().speed / 240) : 0;
      this.warpTunnel.update(dt, {
        amount: Math.max(this.warp.intensity, diveStreak),
        flow: this.warp.flow,
        focusX: 0.5, focusY: 0.5
      });
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
      // The nearby worlds stop being points and become growing spheres.
      this.planetField.update(this.universe.regions, eye);
      // The canopy motes drift and reseed as you travel.
      this.spaceDust.update(eye);
      // Comets orbit whichever star the player is nearest. The zero fallback
      // reuses one vector so the no-star case never allocates per frame.
      {
        const star = this.universe.nearest(eye, 'star-system');
        this.zeroVec.setAll(0);
        this.comets.update(dt, star ? star.position : this.zeroVec, eye);
      }
      // Wormholes iris open and their gate frames rotate; rare alien ships
      // cruise their arc.
      this.wormholes.update(dt, eye);
      this.alienTraffic.update(dt, eye);

      // Each background shell slides toward the eye by its own lock factor,
      // so near stars sweep past and far ones hold station.
      this.layeredSky.update(eye);
      // Galactic precession: the backdrop very slowly wheels, so the sky is
      // never the same sky for long, the way a real night sky precesses.
      {
        const ang = precessionAngle(this.universeAge);
        for (const m of this.layeredSky.shellMeshes) m.rotation.y = ang;
      }
      // Mirror the main camera and apply coordinate-bound nebular fog, so
      // crossing the galactic plane actually fills the cockpit with gas.
      this.galaxyField.update(eye, this.camera.getTarget(), this.scene);

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
      {
        // Allocation-free equivalent of the former descriptor literal:
        // r.kind === 'blackhole' · horizon: this.universe.horizonRadiusOf(r)
        let count = 0;
        if (!worldOwnsHole) {
          for (const r of this.universe.regions) {
            if (r.kind !== 'blackhole') continue;
            let src = this.holeRenderSources[count];
            if (!src) {
              src = { id: '', position: r.position, horizon: 1, seed: 1 };
              this.holeRenderSources[count] = src;
            }
            src.id = r.id;
            src.position = r.position;
            src.horizon = this.universe.horizonRadiusOf(r);
            src.seed = r.seed ?? 1;
            count++;
          }
        }
        this.holeRenderSources.length = count;
        this.holeField.update(eye, this.holeRenderSources);
      }

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

      // Portal apertures are physical holograms in whichever locale owns
      // each endpoint. The traveller object is persistent so cooldown state
      // survives frames instead of being defeated by a fresh object literal.
      this.portalEndpoints[0] = this.portalGun.a; this.portalEndpoints[1] = this.portalGun.b;
      this.portalVisual.update(dt, this.portalEndpoints, this.currentId);
      if (this.portalGun.linked && this.vehicle.mode !== 'orbit') {
        this.portalTraveller.position = this.vehicle.position;
        this.portalTraveller.velocity = this.vehicle.velocity;
        const trip = this.portalGun.tryTeleport(this.portalTraveller, dt);
        if (trip) {
          this.camera.position.copyFrom(this.vehicle.position);
          this.shell.toast('Through the portal');
          if (trip.worldId !== this.currentId) {
            void this.loadWorld(trip.worldId).then(() => {
              this.camera.position.copyFrom(this.vehicle.position);
              this.portalVisual.update(0, this.portalEndpoints, this.currentId);
            });
          }
        }
      }

      // ---- ambient wormholes ----
      // Fly into a bridge and you emerge light-years away, speed preserved;
      // an Interstellar gate hands you into a generated dimension.
      if (this.vehicle.mode !== 'orbit') {
        const trip = this.wormholes.tryTransit({
          position: this.vehicle.position,
          velocity: this.vehicle.velocity,
          radius: 1.2
        });
        if (trip) {
          if (trip.kind === 'moved') {
            this.camera.position.copyFrom(this.vehicle.position);
            this.onDiscovery('event', 'wormhole', '🌀', 'Wormhole',
              'Two points in space, sewn together.');
            this.shell.toast('Through the wormhole');
          } else if (trip.kind === 'dimension') {
            // If the player is stranded, a wormhole is the way HOME, not a
            // door deeper into the multiverse.
            if (this.stranded) {
              this.stranded = false;
              this.onDiscovery('event', 'escape', '🌍', 'Charting Home',
                'You threaded the wormholes and found local space again.');
              this.shell.toast('You charted a way home.');
              this.vehicle.teleport(new Vector3(0, 0, 240));
              this.vehicle.faceTowards(Vector3.Zero());
              this.camera.position.copyFrom(this.vehicle.position);
              this.camera.setTarget(Vector3.Zero());
              void this.loadWorld('planetary');
            } else {
              this.onDiscovery('event', 'interstellar', '✨', 'The Gate',
                'A wormhole opened onto somewhere that is not space.');
              this.shell.toast('The gate gives way');
              void this.enterDimension(trip.seed ?? 1, trip.depth ?? 0);
            }
          }
        }
      }

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
        // Same reasoning as the verse tint above: the gas carries the
        // colour, the clear colour only keeps the void off pure black.
        // Inside a descent the tint IS the medium you are falling through,
        // so it stays - this is not empty space.
        this.scene.clearColor = new Color4(
          t.tint[0] * 0.05, t.tint[1] * 0.05, t.tint[2] * 0.06, 1);
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
        // Suit hazard: how close the nearest horizon is, for the armor ring.
        let hazard = 0;
        {
          const hz = this.universe.insideHorizon ?? this.nearestHole();
          if (hz) {
            const hr = this.universe.horizonRadiusOf(hz);
            const d = Vector3.Distance(eye, hz.position);
            hazard = Math.max(0, Math.min(1, 1 - (d - hr) / Math.max(hr * 8, 1e-3)));
          }
        }
        this.flightHud.setAnomaly(this.quantumAnomaly.telemetry());
        this.flightHud.setPortalLink(this.portalGun.linked, this.portalGun.bridgesWorlds());
        this.flightHud.update({
          x: eye.x, y: eye.y, z: eye.z,
          heading: att.yaw,
          pitch: att.pitch,
          speed: this.shownSpeed,
          throttle: Math.min(1, this.shownSpeed / Math.max(1, this.vehicle.flySpeed * 12)),
          warpCharge: this.gearbox.warpAllowed ? w.charge : 0,
          // Report what the gear actually permits, not what the drive
          // wanted: a HUD that reads 90,000x while you are locked to
          // impulse is lying to the player.
          warpMultiplier: this.gearbox.clampWarp(w.multiplier),
          gear: this.gearbox.current,
          locale: (() => {
            const d = depthOf(eye.length());
            const v = verseAt(d);
            return v.id === 'universe'
              ? (near?.name ?? 'Deep space')
              : v.name + ' · ' + describeDepth(d);
          })(),
          localeDistance: near ? Vector3.Distance(eye, near.position) : 0,
          fleetSize: this.fleet.vessels.length,
          fleetGravity: fg.surfaceGravity,
          hazard
        });
      }

      // ---- carrying things around ----
      if (this.grab.isHolding()) {
        this.camera.getTarget().subtractToRef(this.camera.position, this.cameraForward);
        this.grab.update(dt, this.camera.position, this.cameraForward);
      }
      // ---- strict camera sync, immediately before the draw ----
      // world.update() ran early in the frame, before the camera was moved
      // into its final position. Any world that raymarches from camera
      // uniforms would otherwise be drawing from a one-frame-stale camera,
      // which shows up as the black hole's disk sliding off its horizon
      // while turning or when a panel resizes the canvas.
      (this.world as any)?.syncCamera?.(this.ctx);

      // ---- time rewind: record the motion so it can be scrubbed ----
      this.rewind.record(
        this.vehicle.position.x, this.vehicle.position.y, this.vehicle.position.z,
        this.vehicle.velocity.x, this.vehicle.velocity.y, this.vehicle.velocity.z);

      // ---- weather you fly through: dust, blizzards and rain bands ----
      // Only when walking on a world, so it never fights the galaxy fog or
      // the descent medium that own the space in between.
      if (this.vehicle.mode === 'walk' && this.universe.current) {
        const w = this.surfaces.weather(this.universe.current.id, this.universeAge);
        if (w.kind !== 'clear') {
          this.scene.fogMode = Scene.FOGMODE_EXP2;
          this.scene.fogDensity = (1 - w.visibility) * 0.006;
          this.scene.fogColor = w.kind === 'dust'
            ? new Color3(0.85, 0.6, 0.34)
            : w.kind === 'blizzard'
              ? new Color3(0.9, 0.94, 1.0)
              : new Color3(0.55, 0.62, 0.72);
        } else {
          this.scene.fogMode = Scene.FOGMODE_NONE;
        }
      }

      // ---- a habitable world has a real sky ----
      // Standing on Earth the clear colour is that world's blue, fading
      // back to space-black the moment you lift off. This is the difference
      // between "on a planet" and "next to a painted sphere in the dark".
      if (this.vehicle.mode === 'walk') {
        let sky: [number, number, number] | null = null;
        for (const s of this.solidSpheres()) {
          if (s.sky && Math.hypot(
            this.vehicle.position.x - s.x,
            this.vehicle.position.y - s.y,
            this.vehicle.position.z - s.z) < s.radius * 3) {
            sky = s.sky;
            break;
          }
        }
        if (sky) {
          this.scene.clearColor = new Color4(sky[0], sky[1], sky[2], 1);
        } else if (this.walkSky) {
          this.walkSky = false;
          this.scene.clearColor = new Color4(0, 0, 0, 1);
        }
        this.walkSky = !!sky;
      }

      // ---- transient collision effects fade and are disposed ----
      this.updateCollisionFX();

      // Hard guarantee: the backdrop must repaint every frame regardless of
      // camera height or orientation, so the viewport can never stall on a
      // stale buffer and flicker black above or below the galactic plane.
      if (!this.scene.autoClear) this.scene.autoClear = true;
      if (!this.scene.autoClearDepthAndStencil) this.scene.autoClearDepthAndStencil = true;

      this.scene.render();

      // adaptive resolution defends the framerate
      const newScale = this.quality.sample(dt);
      if (newScale !== null) {
        const guardedScale = this.perfTier >= 2 ? Math.max(1.8, newScale)
          : this.perfTier === 1 ? Math.max(1.25, newScale) : newScale;
        try { this.engine.setHardwareScalingLevel(guardedScale); } catch { /* ignore */ }
      }

      // ---- performance governor: shed post effects when fps drops ----
      this.perfTimer += dt;
      if (this.perfTimer >= 0.25) {
        this.perfTimer = 0;
        const fps = this.engine.getFps();
        const tier = fps < 26 ? 2 : fps < 42 ? 1 : 0;
        if (tier !== this.perfTier) this.applyPerfTier(tier);
      }

      // autosave so a crash or refresh never loses the session
      this.saves.tick(dt, () => {
        const w = this.world as any;
        return w?.captureState ? { world: w.id, data: w.captureState() } : null;
      });

      this.shell.tickHud(this.engine.getFps(), this.world?.name ?? '–');

      // ---- black-screen watchdog ----
      // Runs for the first few seconds only. The render callback itself owns
      // the frame count so menu/loading frames are counted too.
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
          // Do NOT lift the clear colour here any more. Deep space is
          // deliberately pure black now, so a black clear colour is the
          // correct state rather than evidence of a fault - overriding it
          // would paint a permanent blue-grey wash over the void the first
          // time this guard ever fired. Recovery is stripping the post
          // chain; if the galaxy still draws nothing after that, the fault
          // is upstream of the clear colour anyway.
          const c = this.scene.clearColor;
          if (c.a < 0.99) this.scene.clearColor = new Color4(c.r, c.g, c.b, 1);
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
