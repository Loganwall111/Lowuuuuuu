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
import type { Scene } from '@babylonjs/core/scene';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Camera } from '@babylonjs/core/Cameras/camera';
import {
  MILKY_WAY, galaxyStar, nebulaDensity, nebulaColor, observerPosition,
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
 * A galaxy sized to span FIELD_INNER..FIELD_OUTER in real coordinates.
 *
 * MILKY_WAY's own bounds are in a different unit scale, so the shape is
 * reused but rescaled: same arms, same spiral, real distances.
 */
export const FIELD_GALAXY: GalaxyConfig = {
  ...MILKY_WAY,
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
  const c = nebulaColor(d,
    x - GALAXY_CENTER[0], y - GALAXY_CENTER[1], z - GALAXY_CENTER[2], cfg);
  return { density: d, color: [c[0], c[1], c[2]] };
}

export class GalaxyField {
  private scene: Scene | null = null;
  private main: Camera | null = null;
  /** True positions, kept so the proxy can be recomputed each frame. */
  private truePos: Float64Array | null = null;
  private farTruePos: Float64Array | null = null;
  private clouds: PointsCloudSystem[] = [];
  private meshes: Mesh[] = [];
  private built = false;

  /** Total points placed. */
  count = 0;
  /** Whether the field is currently shown. */
  visible = true;
  /** Other galaxies, as real points at real coordinates. */
  private farMesh: Mesh | null = null;
  private farCloud: PointsCloudSystem | null = null;
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
        p.color = c;
      });
      const starMesh = await stars.buildMeshAsync();
      this.applyState(starMesh, 2.0);
      this.clouds.push(stars);
      this.meshes.push(starMesh);

      // ---- gas ----
      // Rejection-sampled against the same density field the fog reads, so
      // the visible clouds and the fog you fly through are the same object.
      const gasRng = makeRng(seed ^ 0x9e3779b9);
      const gas = new PointsCloudSystem('galaxyGas', 1, scene);
      gas.addPoints(GAS_COUNT, (p: any) => {
        let placed = false;
        for (let tries = 0; tries < 24 && !placed; tries++) {
          const r = FIELD_INNER + gasRng() * (FIELD_OUTER - FIELD_INNER);
          const th = gasRng() * Math.PI * 2;
          const h = (gasRng() - 0.5) * 2 * r * cfg.thickness * 2.2;
          const x = Math.cos(th) * r, z = Math.sin(th) * r;
          const d = nebulaDensity(x, h, z, cfg);
          if (gasRng() < d) {
            p.position = new Vector3(
              x + GALAXY_CENTER[0], h + GALAXY_CENTER[1], z + GALAXY_CENTER[2]);
            const c = nebulaColor(d, x, h, z, cfg);
            p.color = new Color4(c[0], c[1], c[2], Math.min(0.5, d * 0.6));
            placed = true;
          }
        }
        if (!placed) {
          // Never leave a point at the origin as a visible clump.
          p.position = new Vector3(0, 0, 0);
          p.color = new Color4(0, 0, 0, 0);
        }
      });
      const gasMesh = await gas.buildMeshAsync();
      // THE PINK GLITCH.
      // This was 90.0. A 90-pixel additive quad, 9,000 of them along one
      // band, overlaps itself many times over: the gas colour is a dim
      // (0.42, 0.13, 0.31), but three overlapping points already saturate
      // red and blue to 1.0 while green lags - which is precisely the
      // magenta smear that filled the screen - and eight stack to white.
      // A few pixels lets the density read as haze instead of paint.
      //
      // But 4.0 overcorrected: it dropped the gas to 1.6% of the screen,
      // which is why the nebulae "vanished". Rasterising the real point set
      // through the proxy projection gives the tradeoff directly -
      //
      //   size  4 -> 1.6% of screen lit, 0.000% blown out
      //   size 14 -> 9.8% lit,           0.005% blown out
      //   size 20 -> 14.4% lit,          0.554% blown out  (pink returns)
      //   size 90 -> 27.7% lit,          13.2% blown out   (the glitch)
      //
      // 14 is the knee: six times the visible gas of 4.0 while the blown-out
      // fraction is still effectively zero. Raising the COLOUR instead (the
      // obvious "just make it brighter" move) is the wrong lever - an 8x
      // exposure multiplier barely moves coverage, 1.6% -> 1.7%, because the
      // points are tiny, but it drives peak intensity to 4.94 and saturates
      // 0.4% of the frame straight back to magenta.
      this.applyState(gasMesh, 14.0);
      this.clouds.push(gas);
      this.meshes.push(gasMesh);

      // Keep the true coordinates: the proxy overwrites the vertex buffer
      // every frame, so the real positions have to live somewhere else.
      this.truePos = this.capturePositions([starMesh, gasMesh]);

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

      const PER = 26;
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
      this.farTruePos = this.capturePositions([mesh]);
    } catch (e) {
      // Distant galaxies are scenery; losing them must not lose the frame.
      console.warn('Distant galaxies unavailable:', e);
    }
  }

  private disposeFar(): void {
    try { this.farCloud?.dispose(); } catch { /* gone */ }
    this.farCloud = null;
    this.farMesh = null;
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
    this.projectOne(this.meshes[1] ?? null, this.truePos, eye, STAR_COUNT);
    this.projectOne(this.farMesh, this.farTruePos, eye);
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
    this.visible = on;
  }

  update(eye: Vector3, target: Vector3, scene: Scene | null = this.scene): void {
    if (!scene) return;
    // Remap the galaxy into the shell the main camera can actually see.
    this.projectProxy(eye);

    // Coordinate-bound nebular fog: density comes from where you actually
    // are, so crossing the disc fills the cockpit and leaving it clears.
    // A hidden galaxy must not leave its fog behind, or the Codeverse
    // inherits nebula haze from a Milky Way that is not being drawn.
    if (!this.visible) { scene.fogMode = 0; return; }
    const f = fogStateAt(eye.x, eye.y, eye.z);
    if (f.density > 0.001) {
      scene.fogMode = 2; // EXP
      scene.fogDensity = f.density * 0.0016;
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
