/**
 * CosmicSky — the procedural sky dome.
 *
 * Draws `cosmicSky()` onto a large inverted sphere around the camera. The
 * same GLSL function is compiled into the black hole raymarcher, so a hole
 * standing in front of this sky bends exactly what is behind it: matrix rain
 * in the Codeverse, Mandelbrot spirals in the Fractal Core, dust lanes and
 * emission nebulae in ordinary space.
 *
 * This is the "dynamic cubemap" requirement, satisfied without a cubemap.
 * A cube map is a function from direction to colour; capturing one into six
 * textures would cost six extra renders per frame and cap the sky at the
 * cube's resolution - which is worst exactly at an Einstein ring, where a
 * tiny patch of sky is magnified enormously. Sharing the function instead
 * is infinitely sharp, costs nothing to capture, and cannot go stale.
 *
 * Safety rules, same as the star shells:
 *   - never writes depth, so it can never punch holes in the scene
 *   - rendering group 0, drawn before everything
 *   - follows the camera, so its edge can never be reached
 */

import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { MeshBuilder } from '@babylonjs/core/Meshes/meshBuilder';
import { ShaderMaterial } from '@babylonjs/core/Materials/shaderMaterial';
import { Effect } from '@babylonjs/core/Materials/effect';
import type { Mesh } from '@babylonjs/core/Meshes/mesh';
import type { Scene } from '@babylonjs/core/scene';
import {
  SKY_VERT, SKY_FRAG, COSMIC_SKY_SHADER, mediumId
} from '../shaders/CosmicSkyShader';

let registered = false;
function register(): void {
  if (registered) return;
  Effect.ShadersStore[COSMIC_SKY_SHADER + 'VertexShader'] = SKY_VERT;
  Effect.ShadersStore[COSMIC_SKY_SHADER + 'FragmentShader'] = SKY_FRAG;
  registered = true;
}

/** What the sky should currently look like. */
export interface SkyState {
  medium: string;
  symmetry: number;
  tint: [number, number, number];
  strangeness: number;
  /** Mandelbrot magnification, driven by sustained forward flight. */
  zoom: number;
  exposure: number;
}

export const DEFAULT_SKY: SkyState = {
  medium: 'stars',
  symmetry: 0,
  tint: [0.06, 0.10, 0.22],
  strangeness: 0,
  zoom: 1,
  exposure: 1
};

/** Radius of the dome. Comfortably inside the 4000-unit far plane. */
export const SKY_RADIUS = 3600;

/**
 * How fast the fractal magnifies while flying forward, per second.
 *
 * Exponential, because the Mandelbrot's detail is self-similar: linear zoom
 * would feel like it stops moving almost immediately.
 */
export const ZOOM_RATE = 1.55;
/**
 * Deepest useful magnification.
 *
 * Measured, not guessed: with the shader's 340-iteration ceiling this
 * centre resolves 159 distinct escape times at 1e5 and collapses to 3 by
 * 1e6 - a featureless void. Flying past the point where there is anything
 * left to see is worse than stopping, so the zoom stops here.
 */
export const MAX_ZOOM = 2e5;

/**
 * Advances the fractal zoom.
 *
 * Only the Fractal Core zooms, and only while genuinely flying forward.
 * Everything else holds at 1 so the sky is stable.
 */
export function advanceZoom(
  zoom: number, dt: number, thrusting: boolean, medium: string
): number {
  const z = Number.isFinite(zoom) && zoom > 0 ? zoom : 1;
  const step = Number.isFinite(dt) ? Math.max(0, Math.min(0.25, dt)) : 0;
  if (medium !== 'fractal') return 1;
  if (!thrusting) return z;
  const next = z * Math.pow(ZOOM_RATE, step);
  return Math.min(MAX_ZOOM, Math.max(1, next));
}

export class CosmicSky {
  private scene: Scene | null = null;
  private dome: Mesh | null = null;
  private mat: ShaderMaterial | null = null;
  private time = 0;
  private state: SkyState = { ...DEFAULT_SKY };

  /** The dome mesh, exposed so tests can verify its render state. */
  get mesh(): Mesh | null { return this.dome; }
  get zoom(): number { return this.state.zoom; }

  attach(scene: Scene): void {
    this.detach();
    register();
    this.scene = scene;
    try {
      const dome = MeshBuilder.CreateSphere(
        'cosmicSkyDome', { diameter: SKY_RADIUS * 2, segments: 24 }, scene);
      // Seen from the inside.
      dome.flipFaces(true);

      const mat = new ShaderMaterial('cosmicSkyM', scene, COSMIC_SKY_SHADER, {
        attributes: ['position'],
        uniforms: [
          'worldViewProjection', 'medium', 'symmetry', 'tint',
          'strangeness', 'time', 'zoom', 'exposure'
        ]
      });

      // THE RULE THAT KEEPS THE SKY A BACKDROP. Writing depth at the dome's
      // radius would cull everything drawn after it.
      mat.disableDepthWrite = true;
      mat.backFaceCulling = false;
      dome.material = mat;
      dome.renderingGroupId = 0;
      dome.isPickable = false;
      dome.applyFog = false;
      dome.infiniteDistance = false;
      // The dome surrounds the camera; culling it is wasted work and can
      // pop the whole sky out of view.
      dome.alwaysSelectAsActiveMesh = true;

      this.dome = dome;
      this.mat = mat;
      this.apply();
    } catch (e) {
      // A sky that fails to build must never stop the scene rendering.
      console.warn('Procedural sky unavailable:', e);
      this.dome = null;
      this.mat = null;
    }
  }

  /** Points the sky at a verse. */
  setState(next: Partial<SkyState>): void {
    const s = this.state;
    if (typeof next.medium === 'string') s.medium = next.medium;
    if (Number.isFinite(next.symmetry as number)) s.symmetry = next.symmetry as number;
    if (Array.isArray(next.tint) && next.tint.length === 3 &&
        next.tint.every((v) => Number.isFinite(v))) {
      s.tint = [next.tint[0], next.tint[1], next.tint[2]];
    }
    if (Number.isFinite(next.strangeness as number)) {
      s.strangeness = Math.max(0, Math.min(1, next.strangeness as number));
    }
    if (Number.isFinite(next.zoom as number)) {
      s.zoom = Math.max(1, Math.min(MAX_ZOOM, next.zoom as number));
    }
    if (Number.isFinite(next.exposure as number)) {
      s.exposure = Math.max(0, Math.min(4, next.exposure as number));
    }
    this.apply();
  }

  private apply(): void {
    const m = this.mat;
    if (!m) return;
    const s = this.state;
    m.setFloat('medium', mediumId(s.medium));
    m.setFloat('symmetry', s.symmetry > 0 ? s.symmetry : 4);
    m.setVector3('tint', new Vector3(s.tint[0], s.tint[1], s.tint[2]));
    m.setFloat('strangeness', s.strangeness);
    m.setFloat('time', this.time);
    m.setFloat('zoom', s.zoom);
    m.setFloat('exposure', s.exposure);
  }

  /**
   * Per-frame update.
   *
   * @param dt        seconds since the last frame
   * @param eye       camera position - the dome follows it
   * @param thrusting whether the player is flying forward, which drives the
   *                  fractal zoom
   */
  update(dt: number, eye: Vector3 | null, thrusting = false): void {
    const step = Number.isFinite(dt) ? Math.max(0, Math.min(0.25, dt)) : 0;
    this.time += step;
    this.state.zoom = advanceZoom(this.state.zoom, step, thrusting, this.state.medium);
    if (this.dome && eye) {
      // Locked to the eye, so the sky is unreachable no matter how far you
      // fly. Parallax comes from the star shells in front of it.
      this.dome.position.copyFrom(eye);
    }
    this.apply();
  }

  stats(): Record<string, string> {
    return {
      'Sky': this.dome ? this.state.medium : 'off',
      'Sky zoom': this.state.zoom > 1.001
        ? this.state.zoom.toExponential(2) + 'x'
        : '—'
    };
  }

  detach(): void {
    try { this.mat?.dispose(); } catch { /* already gone */ }
    try { this.dome?.dispose(); } catch { /* already gone */ }
    this.mat = null;
    this.dome = null;
    this.scene = null;
  }

  dispose(): void { this.detach(); }
}
