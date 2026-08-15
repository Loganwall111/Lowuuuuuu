/**
 * GalaxyField - the Milky Way as a real place you can fly into.
 *
 * THE PROBLEM THIS SOLVES
 *
 * The galaxy used to be painted on a dome at infinite distance and on
 * point shells locked to the camera. Both are backdrops: they translate
 * with you, so no matter how long you burn toward them the distance never
 * changes and nothing ever gets closer. Worse, because the shells sat at
 * radius 2,000-3,800 while real objects sit much nearer, the "sky" was
 * physically INSIDE the scene, so ordinary objects kept popping in front
 * of the galaxy. That is the rendering-order glitch: not a sorting bug but
 * a geometry one. The stars were never actually far away.
 *
 * Here the galaxy is instead a true coordinate grid: stars at honest XYZ
 * positions from radius 2,000 out to 50,000, on the same logarithmic
 * spiral the rest of the engine already uses, with volumetric gas sampled
 * from the same 3D noise field. Fly for long enough and you arrive; keep
 * going and you come out the far side into empty intergalactic space.
 *
 * THE DEPTH PROBLEM, AND WHY THERE IS A SECOND CAMERA
 *
 * A single camera cannot cover this. The scene needs minZ = 0.05 to let
 * you stand on a planet surface, and a 50,000-unit far plane against a
 * 0.05 near plane is a depth ratio of 1e6, which shreds a 24-bit depth
 * buffer and z-fights everything. So the galaxy is drawn by its own camera
 * with a near plane of 500 and a far plane of 200,000, in a layer the main
 * camera cannot see, before the main pass and without clearing colour.
 * Each camera then gets a sane depth range for the scale it draws, and
 * because the galaxy pass runs FIRST and never writes depth, real objects
 * always composite in front of it - which is the correct occlusion, since
 * everything in the scene is genuinely nearer than 2,000 units.
 *
 * WHAT STAYS PROCEDURAL
 *
 * Every star position, every gas puff and every colour comes from the
 * shared GalaxyShape maths. No textures, no image files.
 */
import { Color3 } from '@babylonjs/core/Maths/math.color';
import { Color4 } from '@babylonjs/core/Maths/math.color';
import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { PointsCloudSystem } from '@babylonjs/core/Particles/pointsCloudSystem';
import { UniversalCamera } from '@babylonjs/core/Cameras/universalCamera';
import { StandardMaterial } from '@babylonjs/core/Materials/standardMaterial';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import {
  GALAXY_POINT_SHADER, registerGalaxyPointShader
} from '../shaders/GalaxyPointShader';
import {
  GALAXY_FOG_SHADER, registerGalaxyFogShader
} from '../shaders/GalaxyFogShader';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import type { Scene } from '@babylonjs/core/scene';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Camera } from '@babylonjs/core/Cameras/camera';
import {
  MILKY_WAY, galaxyStar, nebulaDensity, nebulaColor, galaxyGasColor,
  photorealColor, observerPosition,
  type GalaxyConfig
} from './GalaxyShape';
import {
  galaxiesNear, nearestGalaxy, type GalaxyCell
} from './IntergalacticGrid';

/**
 * The layer only the galaxy camera can see.
 *
 * Babylon's default mask is 0x0FFFFFFF, so bit 29 is outside it: existing
 * meshes and cameras are untouched by adding this.
 */
export const GALAXY_LAYER = 0x20000000;

/** Inner edge of the star field, in world units. */
export const FIELD_INNER = 2000;
/** Outer edge. Beyond this is intergalactic emptiness. */
export const FIELD_OUTER = 50000;

/**
 * The shell the galaxy is projected into, in main-camera space.
 *
 * The main camera runs 0.05..4,000 because standing on a planet needs a
 * near plane that tight. A galaxy 200,000 units away cannot be drawn
 * through that, so each point is remapped along its own view direction
 * into this shell: direction is preserved exactly, only radial distance
 * is compressed. The result is visually identical to a distant galaxy -
 * angular position is all the eye has to go on at that range - while
 * living entirely inside the existing depth range.
 */
export const PROXY_INNER = 2600;
export const PROXY_OUTER = 3700;

/** Legacy names kept so callers and tests do not break. */
export const GALAXY_NEAR = 500;
export const GALAXY_FAR = 200000;

/**
 * Map a true distance onto the proxy shell.
 *
 * Logarithmic, so near galaxies still resolve as nearer than far ones and
 * the ordering the eye expects is preserved across five decades of range.
 * Pure, so the mapping can be tested without a GPU.
 */
export function proxyRadius(trueDist: number, radius = GALAXY_RADIUS_REF): number {
  if (!Number.isFinite(trueDist) || trueDist <= 0) return PROXY_INNER;
  const t = Math.log10(1 + trueDist / Math.max(radius, 1)) / Math.log10(1 + 400);
  const k = Math.max(0, Math.min(1, t));
  return PROXY_INNER + (PROXY_OUTER - PROXY_INNER) * k;
}

/** Reference scale for the log remap. */
export const GALAXY_RADIUS_REF = 50000;

/** How many stars and gas puffs the field is built from. */
export const STAR_COUNT = 30000;
export const GAS_COUNT = 9000;
/**
 * Gas points per distant galaxy.
 *
 * 26 star points each gave a flat single-tint smudge; this is the cloud
 * that goes inside it. 343 galaxies x 34 = ~11,700 points, against the
 * 39,000 the home galaxy already costs.
 */
export const FAR_GAS_PER = 34;
/** Star points per distant galaxy. The gas offset depends on this. */
export const FAR_STAR_PER = 26;

/** Middle of the proxy shell, used to calibrate on-screen point size. */
const PROXY_MID = (PROXY_INNER + PROXY_OUTER) / 2;
/** The screen height the point sizes were measured against. */
const REFERENCE_HEIGHT = 1080;

/**
 * Radius of the fog shell.
 *
 * Inside the camera's far plane (4000) and outside the proxy star shell
 * (2600..3700) so the fog encloses the stars rather than cutting through
 * them.
 */
const FOG_SHELL_R = 3850;

/**
 * A galaxy sized to span FIELD_INNER..FIELD_OUTER in real coordinates.
 *
 * MILKY_WAY's own bounds are in a different unit scale, so the shape is
 * reused but rescaled: same arms, same spiral, real distances.
 */
/**
 * The home galaxy is the photorealistic reference look, always.
 *
 * It is the one galaxy the player is guaranteed to see, so it is the one
 * that sets the visual baseline for the whole project.
 */
export const HOME_CLASS: 'photoreal' | 'anomaly' = 'photoreal';

/**
 * A GRAND-DESIGN TWO-ARM SPIRAL, like the reference photograph.
 *
 * MILKY_WAY's own 4 arms at armFactor 4.2 are kept for everything that
 * uses that config directly, but the rendered field overrides them, and
 * the override is load bearing rather than cosmetic.
 *
 * The number of radial cycles a spiral shows across its disc is
 *   arms * armFactor * ln(outerBound / innerBound) / 2pi
 * At 4 arms and armFactor 4.2 that is 7.71 cycles with a 13.4 degree
 * pitch angle: rendered, it reads as a stack of concentric rings rather
 * than as spiral arms, because the arms wrap so many times that adjacent
 * windings merge. At 2 arms and armFactor 2.6 it is 2.39 cycles with a
 * 21.0 degree pitch - which is what a grand-design spiral looks like, and
 * what the fog shader's structure constants are tuned against.
 */
export const FIELD_GALAXY: GalaxyConfig = {
  ...MILKY_WAY,
  arms: 2,
  armFactor: 2.6,
  innerBound: FIELD_INNER * 1.4,
  outerBound: FIELD_OUTER
};

/**
 * Where the galactic centre sits relative to world origin.
 *
 * The galaxy must NOT be centred on the origin: the playable scene lives
 * there, and centring put 4,876 of the 30,000 stars within 4,000 units of
 * the home system - the core would have been sitting on top of the
 * planets. Offsetting by the observer radius puts the player out in a
 * spiral arm where Earth actually is, with the core far away in one
 * direction and the rim in the other, both reachable by flying.
 */
export const GALAXY_CENTER: [number, number, number] =
  [-observerPosition(FIELD_GALAXY)[0], 0, 0];

/**
 * Inside this radius, individual stars are fully dissolved into the
 * volumetric nucleus. Galaxy-local units.
 */
export const NUCLEUS_MERGE_INNER = 700;
/** Past this radius stars are drawn at full strength again. */
export const NUCLEUS_MERGE_OUTER = 2600;

/** Smoothstep on a 0-1 result, used for occlusion ramps. */
export function smoothstep01(edge0: number, edge1: number, x: number): number {
  const d = edge1 - edge0;
  if (Math.abs(d) < 1e-9) return x < edge0 ? 0 : 1;
  const t = Math.max(0, Math.min(1, (x - edge0) / d));
  return t * t * (3 - 2 * t);
}

/** Deterministic PRNG, so the galaxy is the same place every session. */
export function makeRng(seed: number): () => number {
  let s = seed >>> 0;
  return () => {
    s = (Math.imul(s, 1664525) + 1013904223) >>> 0;
    return s / 4294967296;
  };
}

/**
 * How thick the gas is along the line of sight at a point.
 *
 * Pure, so the fog can be tested without a GPU. Returns 0 outside the
 * disc and rises smoothly toward the dust lanes, which is what lets the
 * cockpit actually fill with nebula as you cross the plane.
 */
export function fogAt(
  x: number, y: number, z: number, cfg: GalaxyConfig = FIELD_GALAXY
): number {
  if (!Number.isFinite(x) || !Number.isFinite(y) || !Number.isFinite(z)) return 0;
  // World -> galaxy-local, so the density field lines up with the stars.
  const d = nebulaDensity(
    x - GALAXY_CENTER[0], y - GALAXY_CENTER[1], z - GALAXY_CENTER[2], cfg);
  return Math.min(1, Math.max(0, d));
}

/**
 * Fog colour and strength for the camera's current position.
 *
 * Kept separate from fogAt so the colour can be checked independently of
 * the density curve.
 */
export function fogStateAt(
  x: number, y: number, z: number, cfg: GalaxyConfig = FIELD_GALAXY
): { density: number; color: [number, number, number] } {
  const d = fogAt(x, y, z, cfg);
  if (d <= 0) return { density: 0, color: [0, 0, 0] };
  const c = galaxyGasColor(HOME_CLASS, d,
    x - GALAXY_CENTER[0], y - GALAXY_CENTER[1], z - GALAXY_CENTER[2], cfg);
  // THE GREY SKY.
  //
  // Babylon's EXP fog lerps the whole frame toward fogColor, so whatever is
  // passed here is painted over empty space as well as over geometry. The
  // raw gas colour at the player's start position is (0.225, 0.326, 0.338) -
  // a mid grey-teal - and at density 0.52 that turned the entire background
  // into flat grey, burying the starfield. Interstellar gas does not
  // illuminate the void; it only slightly veils it. So the fog keeps the
  // hue but is taken right down in value.
  return { density: d, color: [c[0] * 0.11, c[1] * 0.11, c[2] * 0.13] };
}

export class GalaxyField {
  private scene: Scene | null = null;
  private main: Camera | null = null;
  /** True positions, kept so the proxy can be recomputed each frame. */
  private truePos: Float64Array | null = null;
  private farTruePos: Float64Array | null = null;
  private clouds: PointsCloudSystem[] = [];
  /** Soft-point materials, kept so camPos and viewport can be fed. */
  private pointMats: ShaderMaterial[] = [];
  /** The volumetric fog shell and its material. */
  private fogMesh: Mesh | null = null;
  private fogMat: ShaderMaterial | null = null;
  /** Class of the galaxy the fog volume is currently representing. */
  private fogClass: string = HOME_CLASS;
  private fogTime = 0;
  private meshes: Mesh[] = [];
  private built = false;

  /** Total points placed. */
  count = 0;
  /** Whether the field is currently shown. */
  visible = true;
  /** Other galaxies, as real points at real coordinates. */
  private farMesh: Mesh | null = null;
  private farCloud: PointsCloudSystem | null = null;
  private farGasMesh: Mesh | null = null;
  private farGasCloud: PointsCloudSystem | null = null;
  private farCells: GalaxyCell[] = [];

  get isBuilt(): boolean { return this.built; }

  /**
   * Create the galaxy camera and register the render pass.
   *
   * The main camera keeps its own near/far; this one covers the large
   * scale, and the two are composited by rendering the galaxy first.
   */
  attach(scene: Scene, main: Camera): void {
    this.detach();
    this.scene = scene;
    this.main = main;
    try {
      // NO SECOND CAMERA.
      //
      // The galaxy used to render through its own UniversalCamera at
      // 500..200,000 while the main camera stayed at 0.05..4,000. That
      // worked in isolation and failed in the real app, because
      // DefaultRenderingPipeline is attached to the MAIN camera only: a
      // post-process pipeline renders into its own framebuffer and blits
      // the result over the backbuffer, erasing whatever the first camera
      // had drawn. The galaxy was being rendered every frame and then
      // painted over - which is exactly "the galaxies are gone".
      //
      // Attaching the pipeline to both cameras would run bloom twice.
      // Instead the galaxy is drawn by the ordinary camera, in rendering
      // group 0, as a PROXY scaled down into a shell that fits inside the
      // existing far plane. Direction is preserved exactly, so the galaxy
      // looks identical; only the radial distance is remapped. Parallax
      // still works because the remap is recomputed from the true
      // coordinates every frame.
      scene.activeCameras = null;
      scene.activeCamera = main;
      scene.autoClear = true;
    } catch (e) {
      // The galaxy is scenery. Losing it must not lose the frame.
      console.warn('Galaxy field unavailable:', e);
      this.detach();
    }
  }

  /**
   * Place the stars and gas at real coordinates.
   *
   * Async because a PointsCloudSystem builds its mesh asynchronously; the
   * caller can ignore the promise safely.
   */
  async build(seed = 20240617): Promise<void> {
    const scene = this.scene;
    if (!scene) return;
    const cfg = FIELD_GALAXY;

    try {
      // ---- stars ----
      const starRng = makeRng(seed);
      const stars = new PointsCloudSystem('galaxyStars', 1, scene);
      stars.addPoints(STAR_COUNT, (p: any) => {
        const st = galaxyStar(starRng, cfg);
        // Real coordinates, offset so the player sits in an arm rather
        // than inside the galactic core. No shell, no camera lock.
        p.position = new Vector3(
          st.x + GALAXY_CENTER[0], st.y + GALAXY_CENTER[1],
          st.z + GALAXY_CENTER[2]);
        const b = Math.max(0.05, Math.min(1, st.bright));
        // Hot blue-white in the arms, warmer in the bulge, dim in the halo.
        const c = st.kind === 'bulge'
          ? new Color4(1.0, 0.86 * b + 0.1, 0.62 * b + 0.1, 1)
          : st.kind === 'halo'
            ? new Color4(0.86 * b, 0.88 * b, 1.0 * b, 1)
            : new Color4(0.78 * b + 0.2, 0.86 * b + 0.12, 1.0, 1);

        // ---- THE NUCLEUS IS A GLOW, NOT A PILE OF DOTS ----
        //
        // 4,726 of the 30,000 stars land within 2,000 units of the centre.
        // Drawn as individual 2px sprites that reads as a loose scatter of
        // yellow specks sitting ON TOP of the fog's bulge, which is exactly
        // the "dim collection of dots" in the report - and no amount of
        // work on the fog could fix it, because the sprites draw over it.
        //
        // Real telescope imagery cannot resolve individual stars in a
        // galactic bulge either; they blend into continuous light. So the
        // innermost stars are faded out and the volumetric nucleus is left
        // to supply the light there. They are faded rather than removed so
        // there is no hard edge where the point cloud stops.
        const rc = Math.hypot(st.x, st.y, st.z);
        const merge = smoothstep01(NUCLEUS_MERGE_INNER, NUCLEUS_MERGE_OUTER, rc);
        c.a *= merge;
        c.r *= merge; c.g *= merge; c.b *= merge;
        p.color = c;
      });
      const starMesh = await stars.buildMeshAsync();
      this.applyState(starMesh, 2.0);
      this.clouds.push(stars);
      this.meshes.push(starMesh);

      // ---- gas ----
      //
      // THE GAS IS NO LONGER PARTICLES.
      //
      // It used to be 9,000 additive sprites. Sprites cannot be fog: small
      // they read as dots, large as squares, softened as soft dots. Three
      // separate attempts to tune size and falloff all still looked like
      // particles, because a finite set of billboards is not a continuous
      // medium and no amount of tuning makes it one.
      //
      // The gas is now a raymarched volume - see GalaxyFogShader. The stars
      // stay as points, because in the reference photograph the fog is
      // continuous while individual stars are still crisp.
      this.buildFog(scene);

      // Keep the true coordinates: the proxy overwrites the vertex buffer
      // every frame, so the real positions have to live somewhere else.
      this.truePos = this.capturePositions([starMesh]);

      // ---- other galaxies ----
      // Each is a cluster of points at its true coordinates, so the smudge
      // you steer toward is genuinely there and grows as you close on it.
      await this.buildFarGalaxies();

      this.count = STAR_COUNT + GAS_COUNT;
      this.built = true;
    } catch (e) {
      console.warn('Galaxy field build failed:', e);
      this.built = false;
    }
  }

  /**
   * Build the distant galaxies around the current origin.
   *
   * Each far galaxy gets a small cluster of points rather than a single
   * dot, so it reads as an extended object with a bright core - and so it
   * still looks like a galaxy while it is too far away to realise fully.
   */
  private async buildFarGalaxies(centre: Vector3 = Vector3.Zero()): Promise<void> {
    const scene = this.scene;
    if (!scene) return;
    try {
      this.disposeFar();
      const cells = galaxiesNear(centre.x, centre.y, centre.z)
        // The galaxy we are inside is drawn as real stars, not a smudge.
        .filter((g) => Math.hypot(g.x - centre.x, g.y - centre.y,
          g.z - centre.z) > g.radius * 1.2);
      this.farCells = cells;
      if (!cells.length) return;

      const PER = FAR_STAR_PER;
      const cloud = new PointsCloudSystem('farGalaxies', 1, scene);
      cloud.addPoints(cells.length * PER, (p: any, i: number) => {
        const g = cells[Math.floor(i / PER) % cells.length];
        const k = i % PER;
        // A tight core with a few outliers: recognisable at a glance,
        // and cheap enough that hundreds cost nothing.
        const t = k / PER;
        const spread = g.radius * (0.05 + t * t * 0.85);
        const a = (i * 2.399963) % (Math.PI * 2);
        const rr = spread * Math.sqrt((k % 7) / 7 + 0.05);
        const lx = Math.cos(a) * rr;
        const lz = Math.sin(a) * rr;
        const ly = (((i * 7919) % 100) / 100 - 0.5) * spread * 0.12;
        // Tilt the disc so not every galaxy faces the same way.
        const cx = Math.cos(g.tiltX), sx = Math.sin(g.tiltX);
        const y2 = ly * cx - lz * sx;
        const z2 = ly * sx + lz * cx;
        p.position = new Vector3(g.x + lx, g.y + y2, g.z + z2);
        const b = g.brightness * (k === 0 ? 1.0 : 0.42 * (1 - t) + 0.08);
        p.color = new Color4(g.tint[0] * b, g.tint[1] * b, g.tint[2] * b,
          Math.min(1, 0.35 + b));
      });
      const mesh = await cloud.buildMeshAsync();
      this.applyState(mesh, 2.4);
      mesh.setEnabled(this.visible);
      this.farCloud = cloud;
      this.farMesh = mesh;

      // ---- nebula gas inside every other galaxy ----
      //
      // The star cluster above carries `g.tint` and nothing else, and there
      // are only three tints in the whole grid, so 343 galaxies rendered as
      // three flat colours of smudge with no gas in them at all. Only the
      // Milky Way had coloured clouds. Give each of the others its own
      // emission field, drawn from the same ionisation species as the home
      // galaxy so the universe looks like one place.
      const gasCloud = new PointsCloudSystem('farGalaxyGas', 1, scene);
      gasCloud.addPoints(cells.length * FAR_GAS_PER, (p: any, i: number) => {
        const g = cells[Math.floor(i / FAR_GAS_PER) % cells.length];
        const k = i % FAR_GAS_PER;
        // Per-galaxy stream, so each galaxy's gas is its own shape and the
        // structure is stable frame to frame.
        const rnd = makeRng((g.seed ^ 0x5bf03635) + k * 2654435761);

        // Gas hugs the disc and the arms: sample a radius biased inward,
        // wrap it onto a loose spiral, and keep it thin vertically.
        //
        // Ellipticals skip that entirely. They have no arms and no thin
        // disc, so winding their gas onto a spiral would draw exactly the
        // structure they are defined by not having. They get a 3D Gaussian
        // ellipsoid instead - a smooth triaxial swarm.
        const t = Math.pow(rnd(), 0.65);
        let lx: number, ly: number, lz: number;
        if (g.klass === 'elliptical') {
          const gauss = (): number => {
            const u = Math.max(1e-9, rnd());
            return Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * rnd());
          };
          const sg = g.radius * 0.30;
          lx = gauss() * sg;
          ly = gauss() * sg * 0.62;
          lz = gauss() * sg * 0.84;
          const rr2 = Math.sqrt(lx * lx + ly * ly + lz * lz);
          if (rr2 > g.radius) {
            const f = g.radius / rr2; lx *= f; ly *= f; lz *= f;
          }
        } else {
          const rr = g.radius * (0.08 + t * 0.92);
          const arm = Math.floor(rnd() * 2) * Math.PI;
          const wind = g.winding * 2.6;
          const ang = arm + Math.log(1 + t * 6) * wind + (rnd() - 0.5) * 0.9;
          lx = Math.cos(ang) * rr;
          lz = Math.sin(ang) * rr;
          ly = (rnd() - 0.5) * g.radius * 0.06;
        }

        const cx = Math.cos(g.tiltX), sx = Math.sin(g.tiltX);
        const y2 = ly * cx - lz * sx;
        const z2 = ly * sx + lz * cx;
        p.position = new Vector3(g.x + lx, g.y + y2, g.z + z2);

        // Density falls off outward, exactly like the home galaxy's field,
        // so the colour ramp behaves the same way at the same radii.
        const dens = Math.max(0.12, 0.95 - t * 0.8);
        // Sample the shared emission palette in the galaxy's own local
        // coordinates. This is what gives every galaxy H-alpha crimson,
        // O-III teal and S-II orange strips instead of one flat tint.
        // Sampled against a config scaled to THIS galaxy's radius. Passing
        // raw coordinates against MILKY_WAY's 16,000-unit disc would push a
        // 50,000-unit galaxy far outside the frequency the palette was tuned
        // for, and the excitation term - which keys off radius - would read
        // the whole galaxy as cold outskirts.
        const c = galaxyGasColor(g.klass, dens, lx, ly, lz, {
          ...MILKY_WAY, outerBound: g.radius, innerBound: g.radius * 0.06
        });
        // The galaxy's own tint still shows through, so blue starbursts
        // stay blue and old red ellipticals stay red - the species mix
        // varies the colour WITHIN each galaxy rather than erasing it.
        const b = g.brightness * 0.55;
        // The tint is a lean bias, not a blend. At 0.32 it desaturated the
        // emission colours to a near-white mean hue of (0.97, 0.85, 1.00),
        // which is the flat look this change exists to remove.
        p.color = new Color4(
          (c[0] * 0.86 + g.tint[0] * 0.14) * b,
          (c[1] * 0.86 + g.tint[1] * 0.14) * b,
          (c[2] * 0.86 + g.tint[2] * 0.14) * b,
          Math.min(0.42, dens * 0.5));
      });
      const gasMesh = await gasCloud.buildMeshAsync();
      // Larger than the star points for the same reason the home galaxy's
      // gas is: haze has to cover area to read as haze. Still far below the
      // size where additive stacking clips.
      this.applyState(gasMesh, 9.0);
      gasMesh.setEnabled(this.visible);
      this.farGasCloud = gasCloud;
      this.farGasMesh = gasMesh;

      this.farTruePos = this.capturePositions([mesh, gasMesh]);
    } catch (e) {
      // Distant galaxies are scenery; losing them must not lose the frame.
      console.warn('Distant galaxies unavailable:', e);
    }
  }

  /** Release the soft-point materials. */
  /**
   * Build the volumetric fog shell.
   *
   * A single inward-facing sphere that always surrounds the camera. It is
   * NOT the galaxy's geometry - the galaxy is far larger than the far plane
   * - it is a screen for the raymarcher, and the march itself happens in
   * true galaxy coordinates, so flying across the disc genuinely changes
   * what the ray passes through.
   */
  private buildFog(scene: Scene): void {
    try {
      registerGalaxyFogShader();
      const shell = MeshBuilder.CreateSphere(
        'galaxyFog', { diameter: FOG_SHELL_R * 2, segments: 16 }, scene);
      shell.flipFaces(true);          // seen from inside

      const mat = new ShaderMaterial('galaxyFogM', scene, GALAXY_FOG_SHADER, {
        attributes: ['position'],
        uniforms: ['worldViewProjection', 'camPos', 'innerR', 'outerR',
          'thickness', 'arms', 'armFactor', 'anomaly', 'density', 'time',
          'marchFar']
      });
      const cfg = FIELD_GALAXY;
      mat.setFloat('innerR', cfg.innerBound);
      mat.setFloat('outerR', cfg.outerBound);
      mat.setFloat('thickness', cfg.thickness);
      mat.setFloat('arms', cfg.arms);
      mat.setFloat('armFactor', cfg.armFactor);
      mat.setFloat('anomaly', HOME_CLASS === 'anomaly' ? 1 : 0);
      mat.setFloat('density', 1);
      mat.setFloat('time', 0);
      // Far enough to cross the whole disc from outside it.
      mat.setFloat('marchFar', cfg.outerBound * 2.6);
      mat.setVector3('camPos', Vector3.Zero());

      mat.backFaceCulling = false;
      mat.disableDepthWrite = true;
      mat.alpha = 0.999;
      // PREMULTIPLIED alpha, not ALPHA_COMBINE.
      //
      // The shader outputs the light that actually reached the eye, plus
      // alpha = 1 - transmittance describing how much of the background it
      // blocked. The correct composite is therefore
      //   result = rgb + background * (1 - alpha)
      // which is exactly premultiplied blending.
      //
      // Under ALPHA_COMBINE the GPU instead computes
      //   rgb * alpha + background * (1 - alpha)
      // multiplying the emission by its own coverage a second time.
      // Measured, the galactic core renders at alpha 0.124, so the
      // brightest object in the scene was being drawn at 12% brightness -
      // a major reason the nucleus kept reading as dim. Extinction still
      // works: dust lanes raise alpha and darken what is behind them.
      mat.alphaMode = 7;   // Constants.ALPHA_PREMULTIPLIED

      shell.material = mat;
      shell.renderingGroupId = 0;
      shell.isPickable = false;
      shell.applyFog = false;
      shell.alwaysSelectAsActiveMesh = true;
      shell.infiniteDistance = false;

      this.fogMesh = shell;
      this.fogMat = mat;
      shell.setEnabled(this.visible);
    } catch (e) {
      // No fog is survivable; a black screen is not.
      console.warn('Volumetric galaxy fog unavailable:', e);
      this.fogMesh = null;
      this.fogMat = null;
    }
  }

  private disposeFog(): void {
    try { this.fogMesh?.dispose(); } catch { /* gone */ }
    try { this.fogMat?.dispose(); } catch { /* gone */ }
    this.fogMesh = null;
    this.fogMat = null;
  }

  private disposePointMats(): void {
    for (const m of this.pointMats) { try { m.dispose(); } catch { /* gone */ } }
    this.pointMats = [];
  }

  private disposeFar(): void {
    try { this.farCloud?.dispose(); } catch { /* gone */ }
    try { this.farGasCloud?.dispose(); } catch { /* gone */ }
    this.farCloud = null;
    this.farMesh = null;
    this.farGasCloud = null;
    this.farGasMesh = null;
    this.farCells = [];
  }

  /** The nearest galaxy to a point, for navigation. */
  nearest(x: number, y: number, z: number): { name: string; distance: number } | null {
    const n = nearestGalaxy(x, y, z);
    if (!n) return null;
    const g = n.galaxy;
    return {
      name: 'Galaxy ' + g.ix + '.' + g.iy + '.' + g.iz,
      distance: n.distance
    };
  }

  /**
   * Rewrite every point into the proxy shell around the eye.
   *
   * Called once a frame. Direction from the eye to the true position is
   * preserved exactly; only the radial distance is remapped, so the sky
   * looks right and parallax still happens - move sideways and near
   * galaxies shift against far ones, because their true positions are
   * what the direction is computed from.
   */
  private projectProxy(eye: Vector3): void {
    this.projectOne(this.meshes[0] ?? null, this.truePos, eye);
    // meshes[1] used to be the gas point cloud. The gas is a volume now,
    // so there is nothing else to reproject here.
    this.projectOne(this.farMesh, this.farTruePos, eye);
    // The gas positions live after the star positions in the same buffer.
    this.projectOne(this.farGasMesh, this.farTruePos, eye,
      this.farCells.length * FAR_STAR_PER);
  }

  private projectOne(
    mesh: Mesh | null, src: Float64Array | null, eye: Vector3, offset = 0
  ): void {
    if (!mesh || !src) return;
    try {
      const data = mesh.getVerticesData('position');
      if (!data) return;
      const n = Math.floor(data.length / 3);
      for (let i = 0; i < n; i++) {
        const j = (i + offset) * 3;
        if (j + 2 >= src.length) break;
        const dx = src[j] - eye.x;
        const dy = src[j + 1] - eye.y;
        const dz = src[j + 2] - eye.z;
        const d = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (!(d > 1e-6)) { continue; }
        const r = proxyRadius(d);
        const k = r / d;
        data[i * 3] = dx * k;
        data[i * 3 + 1] = dy * k;
        data[i * 3 + 2] = dz * k;
      }
      mesh.updateVerticesData('position', data, false, false);
      // The proxy is built around the eye, so the mesh itself sits there.
      mesh.position.copyFrom(eye);
      // Its bounds are now the shell, not the galaxy.
      mesh.refreshBoundingInfo();
    } catch {
      // A failed remap leaves last frame's positions, which is a stale
      // galaxy rather than no galaxy.
    }
  }

  /** Snapshot the world positions of some meshes into one flat array. */
  private capturePositions(meshes: (Mesh | null)[]): Float64Array {
    const parts: number[] = [];
    for (const m of meshes) {
      if (!m) continue;
      const d = m.getVerticesData('position');
      if (!d) continue;
      for (let i = 0; i < d.length; i++) parts.push(d[i]);
    }
    return Float64Array.from(parts);
  }

  /** Render state shared by both point clouds. */
  private applyState(mesh: Mesh, size: number): void {
    // Drawn by the ordinary camera now, so it uses the default layer.
    mesh.renderingGroupId = 0;
    mesh.isPickable = false;
    mesh.applyFog = false;
    mesh.alwaysSelectAsActiveMesh = true;

    // ---- soft points instead of hard squares ----
    //
    // PointsCloudSystem hands back a StandardMaterial with pointsCloud =
    // true, which draws square GL_POINTS. At the sizes the gas needs in
    // order to be visible those squares are plainly visible as digital
    // boxes. There is no fragment stage in that path to clip them with, so
    // the material is replaced outright with one that clips the quad to a
    // disc and decays its alpha outward.
    const scene = this.scene;
    if (scene) {
      try {
        registerGalaxyPointShader();
        const sizes: number[] = [];
        const n = Math.floor((mesh.getVerticesData('position')?.length ?? 0) / 3);
        for (let i = 0; i < n; i++) sizes.push(size);
        mesh.setVerticesData('pointSize', sizes, false, 1);

        const sm = new ShaderMaterial('galaxyPtM_' + mesh.name, scene,
          GALAXY_POINT_SHADER, {
            attributes: ['position', 'color', 'pointSize'],
            uniforms: ['world', 'worldViewProjection', 'camPos', 'sizeScale',
              'viewportHeight', 'gasDensity']
          });
        // Calibration. The vertex shader divides by distance, and every
        // point sits on the proxy shell at ~2600-3700 units, so a raw size
        // of 14 would land at ~5px and undo the visibility work that size
        // was measured for. Scaling by (shell / reference height) makes the
        // requested number mean what it meant before - pixels at the middle
        // of the shell - while still letting near points bloom and far
        // points tighten across the shell's depth.
        sm.setFloat('sizeScale', PROXY_MID / REFERENCE_HEIGHT);
        sm.setFloat('viewportHeight', 1);
        sm.setFloat('gasDensity', 1);
        sm.pointsCloud = true;
        sm.disableDepthWrite = true;
        sm.backFaceCulling = false;
        sm.alpha = 0.999;
        // Premultiplied in the shader, so the source factor is ONE.
        sm.alphaMode = 1;
        const old = mesh.material;
        mesh.material = sm;
        try { old?.dispose(); } catch { /* not ours */ }
        this.pointMats.push(sm);
        return;
      } catch (e) {
        // A shader failure must not cost the galaxy. Fall through to the
        // stock material, squares and all, rather than rendering nothing.
        console.warn('Soft galaxy points unavailable, using stock points:', e);
      }
    }

    const m = mesh.material as any;
    if (!m) return;
    m.disableLighting = true;
    // Never write depth: the galaxy is behind everything by construction.
    m.disableDepthWrite = true;
    m.forceDepthWrite = false;
    // Additive, and alpha nudged off 1.0 so Babylon actually arms the
    // blender (needAlphaBlending() is false while alpha === 1).
    m.alpha = 0.999;
    m.alphaMode = 1;
    m.backFaceCulling = false;
    if (m.pointSize !== undefined) m.pointSize = size;
  }

  /**
   * Mirror the main camera and apply volumetric fog.
   *
   * The galaxy camera shares the main camera's exact position and
   * orientation, so the two passes line up perfectly; only their depth
   * ranges differ.
   */
  /**
   * Show or hide the whole field.
   *
   * The Milky Way is a feature of ORDINARY space. Inside the Codeverse or
   * the Fractal Core the sky is a different reality entirely, and leaving
   * 39,000 real stars hanging in it would mean flying through the matrix
   * with our galaxy still visible behind the data streams.
   */
  setVisible(on: boolean): void {
    for (const m of this.meshes) {
      try { m.setEnabled(on); } catch { /* disposed */ }
    }
    try { this.farMesh?.setEnabled(on); } catch { /* disposed */ }
    try { this.farGasMesh?.setEnabled(on); } catch { /* disposed */ }
    try { this.fogMesh?.setEnabled(on); } catch { /* disposed */ }
    this.visible = on;
  }

  update(eye: Vector3, target: Vector3, scene: Scene | null = this.scene): void {
    if (!scene) return;
    // Remap the galaxy into the shell the main camera can actually see.
    this.projectProxy(eye);

    // Feed the soft-point shader. The points are remapped onto the proxy
    // shell every frame, so the shader must size them against the PROXY
    // position rather than the eye's true distance to the real galaxy -
    // hence camPos is the origin of the shell, not the world eye.
    // The fog shell rides with the camera, and the march reads the eye's
    // TRUE galaxy-local position, so crossing the disc really does thicken
    // the medium instead of looking the same from everywhere.
    if (this.fogMesh && this.fogMat) {
      this.fogTime += 1 / 60;
      try {
        this.fogMesh.position.copyFrom(eye);

        // ---- WHICH GALAXY IS THIS FOG? ----
        //
        // The shell used to be hardwired to the home galaxy, which made the
        // rare anomaly class unreachable: only one fog volume is ever built,
        // its class came from the HOME_CLASS constant, and that constant is
        // photoreal. A player could fly to a Class-C cell for as long as
        // they liked and still see the standard palette, so the "legendary
        // find" did not exist in the fog at all.
        //
        // The volume now adopts whichever lattice galaxy the player is
        // actually inside, falling back to home out in intergalactic space.
        const host = nearestGalaxy(eye.x, eye.y, eye.z);
        const inHost = host && host.distance < host.galaxy.radius * 1.3;

        const cx = inHost ? host.galaxy.x : GALAXY_CENTER[0];
        const cy = inHost ? host.galaxy.y : GALAXY_CENTER[1];
        const cz = inHost ? host.galaxy.z : GALAXY_CENTER[2];
        const klass = inHost ? host.galaxy.klass : HOME_CLASS;

        this.fogMat.setVector3('camPos',
          new Vector3(eye.x - cx, eye.y - cy, eye.z - cz));
        // Only pushed when it changes: a uniform write per frame is cheap,
        // but a needless one on every galaxy is still waste.
        if (klass !== this.fogClass) {
          this.fogClass = klass;
          this.fogMat.setFloat('anomaly', klass === 'anomaly' ? 1 : 0);
        }
        this.fogMat.setFloat('time', this.fogTime);
      } catch { /* disposed mid-frame */ }
    }

    if (this.pointMats.length) {
      const h = scene.getEngine()?.getRenderHeight?.() ?? 1080;

      // ---- OCCLUSION: stars vanish INTO the smoke ----
      //
      // The star points are remapped onto a proxy shell every frame, so
      // they are always the same short distance from the eye no matter
      // where the camera really is. That means nothing about their
      // geometry can ever hide them - inside a thick cloud they kept
      // drawing at full brightness straight into the lens, which is the
      // "glitter storm flying into my face" artefact.
      //
      // The volumetric fog cannot occlude them either: it is alpha
      // blended with depth write off, so it never wins a depth test
      // against the points. So the occlusion is done analytically -
      // sample the same density field the fog marches, and fade the
      // points out as the medium around the camera thickens. Deep in a
      // dense cloud the stars are gone entirely and only colour remains,
      // exactly as when you fly into real cloud.
      const dens = fogAt(eye.x, eye.y, eye.z);
      // Full brightness in clear space, fully buried by the time the
      // medium is thick. Smoothstep so there is no pop at the threshold.
      const vis = 1 - smoothstep01(0.16, 0.62, dens);

      for (const m of this.pointMats) {
        try {
          m.setVector3('camPos', Vector3.Zero());
          m.setFloat('viewportHeight', h);
          m.setFloat('gasDensity', vis);
        } catch { /* material disposed mid-frame */ }
      }
    }

    // Coordinate-bound nebular fog: density comes from where you actually
    // are, so crossing the disc fills the cockpit and leaving it clears.
    // A hidden galaxy must not leave its fog behind, or the Codeverse
    // inherits nebula haze from a Milky Way that is not being drawn.
    if (!this.visible) { scene.fogMode = 0; return; }
    const f = fogStateAt(eye.x, eye.y, eye.z);
    if (f.density > 0.001) {
      // Scene fog now only tints NEARBY GEOMETRY - ships, planets, debris -
      // so objects seen through thick gas pick up its colour. The sky's own
      // haze is the raymarched volume, which handles the view into the
      // distance far better than a per-vertex exponential ever could.
      //
      // Keeping both at full strength double-counted the medium and was
      // what greyed the whole frame, so this stays deliberately faint.
      scene.fogMode = 2; // EXP
      scene.fogDensity = f.density * 0.0011;
      scene.fogColor = new Color3(f.color[0], f.color[1], f.color[2]);
    } else {
      scene.fogMode = 0;
    }
  }

  /**
   * Where the player starts, in world coordinates.
   *
   * The offset above is chosen so this is the origin: the existing scene
   * does not have to move, and it lands in a spiral arm.
   */
  static homePosition(): [number, number, number] {
    const o = observerPosition(FIELD_GALAXY);
    return [o[0] + GALAXY_CENTER[0], o[1] + GALAXY_CENTER[1],
      o[2] + GALAXY_CENTER[2]];
  }

  stats(): Record<string, string> {
    return {
      'Galaxy points': this.built && this.visible ? String(this.count) : 'off',
      'Galaxy span': FIELD_INNER + '-' + FIELD_OUTER
    };
  }

  detach(): void {
    this.disposeFar();
    this.disposeFog();
    this.disposePointMats();
    for (const c of this.clouds) { try { c.dispose(); } catch { /* gone */ } }
    this.clouds = [];
    this.meshes = [];
    this.main = null;
    this.truePos = null;
    this.farTruePos = null;
    this.built = false;
    this.count = 0;
    this.scene = null;
  }

  dispose(): void { this.detach(); }
}
