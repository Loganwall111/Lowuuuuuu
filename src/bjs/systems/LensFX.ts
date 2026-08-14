/**
 * LensFX — gravitational lensing everywhere, not just in one world.
 *
 * The warp used to live inside BlackHoleWorld's full-screen raymarcher, so
 * you only ever saw it if you switched to that world. In one continuous
 * universe you fly past black holes anywhere, and they must bend the light
 * of whatever is actually on screen.
 *
 * This is a screen-space deflection pass: it takes the rendered frame and
 * displaces each pixel's lookup toward the hole, with the deflection falling
 * off like a real photon path (~rs/r). It composites over any world, costs
 * one pass, and switches itself off when no hole is near.
 *
 * It is deliberately an *approximation* of the full geodesic integration
 * BlackHoleWorld does. That one is physically integrated per pixel; this one
 * has to run on top of an arbitrary scene every frame.
 */

import { PostProcess } from '@babylonjs/core/PostProcesses/postProcess';
import { Effect } from '@babylonjs/core/Materials/effect';
// THE BLACK SCREEN.
//
// Every PostProcess is a full-screen quad drawn with the shared
// "postprocess" vertex shader. In Babylon's tree-shaken ES build that
// shader is registered by a side-effect import and by nothing else -
// importing PostProcess itself registers no shaders at all. Without it the
// effect can never become ready, so this pass contributes nothing to the
// frame while still sitting in the camera's chain, and the screen is black
// at full frame rate with every mesh present and correct.
//
// It cannot be caught by the headless suite: jsdom's WebGL stub reports
// every compile and link as successful, so the missing shader is invisible
// there. This import is load-bearing - removing it turns the screen black.
import '@babylonjs/core/Shaders/postprocess.vertex';
import { Vector3 } from '@babylonjs/core/Maths/math.vector';
import { safeAspect } from '../SafeUniforms';
import { Matrix } from '@babylonjs/core/Maths/math.vector';
import { Viewport } from '@babylonjs/core/Maths/math.viewport';
import type { Camera } from '@babylonjs/core/Cameras/camera';
import type { Scene } from '@babylonjs/core/scene';
import type { LensProfile } from './LensProfiles';

const LENS_FRAG = `
precision highp float;
varying vec2 vUV;
uniform sampler2D textureSampler;

/**
 * Up to MAX_HOLES lenses at once.
 *
 * A single-hole pass was the reason "universal" lensing was not universal:
 * fly into a binary pair, or past a cluster, and only the nearest hole bent
 * anything while the others sat on a perfectly straight starfield. Light
 * does not take turns, so every hole on screen contributes its own
 * deflection to the same sample.
 *
 * The count is fixed because GLSL ES 1.00 cannot index a uniform array by a
 * non-constant expression on every driver; the loop below is bounded by a
 * constant and skips inactive slots.
 */
const int MAX_HOLES = 4;

uniform vec2  holeUV[MAX_HOLES];
uniform float holeR[MAX_HOLES];     // apparent horizon radius, UV units
/**
 * Einstein radius, UV units - the angular scale of the LENSING, which is a
 * completely different quantity from the size of the shadow.
 *
 * This is the fix for "nothing is bent". The deflection law below has the
 * right shape (displacement = scale^2 / theta, the standard thin-lens
 * result) but it was fed the apparent horizon as its scale. At a typical
 * viewing distance the horizon subtends ~0.05 UV while the Einstein radius
 * is ~0.35 UV, so every displacement came out roughly 40x too small -
 * a fraction of one pixel, which is indistinguishable from no lensing at
 * all. The horizon is still used for the shadow; only the bend changes.
 */
uniform float lensR[MAX_HOLES];
uniform float strength[MAX_HOLES];
uniform float falloff[MAX_HOLES];
uniform float ringAmt[MAX_HOLES];
uniform float ringRadius[MAX_HOLES];
uniform float symmetry[MAX_HOLES];
uniform float distortion[MAX_HOLES];
uniform float twist[MAX_HOLES];
uniform float chroma[MAX_HOLES];
uniform vec3  tint[MAX_HOLES];
uniform float holeOn[MAX_HOLES];    // 0 = this slot is unused
uniform float aspect;
uniform float active;       // 0 disables the whole pass

void main(void){
  vec2 uv = vUV;

  if (active < 0.5){
    gl_FragColor = texture2D(textureSampler, uv);
    return;
  }

  // Accumulated deflection from every hole on screen, and the deepest
  // shadow any of them casts over this pixel.
  vec2 totalOff = vec2(0.0);
  float shadow = 0.0;
  vec3 shadowTint = vec3(0.0);
  vec3 rings = vec3(0.0);
  float maxChroma = 0.0;

  for (int i = 0; i < MAX_HOLES; i++){
    if (holeOn[i] < 0.5) continue;

    // Work in aspect-corrected space so each lens stays circular.
    vec2 d = uv - holeUV[i];
    d.x *= aspect;
    float r = length(d);
    float ang = atan(d.y, d.x);

    // Alien lens shapes: the same knobs the raymarcher uses, so a hole looks
    // like itself whether you are inside its world or flying past it.
    float shape = 1.0 + cos(ang * symmetry[i]) * distortion[i]
                      + sin(ang + twist[i] / max(r, 0.35)) * distortion[i] * 0.6;
    shape = max(shape, 0.02);

    // Deflection falls off with distance like a real photon path.
    //
    // For falloff = 2 this is exactly lensR^2 / r, the thin-lens
    // displacement. Scaled by the Einstein radius, a star just outside the
    // shadow moves a large fraction of the screen and the field visibly
    // wraps around the hole; scaled by the horizon it moved a sub-pixel
    // amount and the sky looked dead straight.
    // Close to the shadow the thin-lens formula diverges - it is derived
    // for weak deflection and stops being valid where light actually
    // orbits. Left unbounded it asks for a displacement of twice the
    // screen, which samples clamped edge pixels and turns the rim into a
    // smear. Softening the denominator caps the shift near the shadow
    // while leaving the far field exactly as the formula says.
    float rr = max(r, lensR[i] * 0.45);
    float bend = strength[i] * lensR[i]
               * pow(clamp(lensR[i] / rr, 0.0, 1.0), max(falloff[i] - 1.0, 0.0))
               * shape;

    // Inside the horizon there is nothing to sample.
    //
    // THIS IS WHAT MADE APPROACHING A HOLE GO BLACK. holeR is the horizon's
    // apparent size in UV, and it grows without limit as you close in - by a
    // few horizon radii away it already exceeds the corner of the screen.
    // Every pixel then satisfies r < holeR*0.86 at once, so the inside
    // term is 1 everywhere and the whole frame becomes a near-black wash,
    // with the rest of the universe drawn underneath but entirely hidden.
    //
    // Clamping the shadow radius keeps the horizon a shape on the screen
    // instead of the whole screen.
    float shadowR = min(holeR[i], 0.42);
    float inside = smoothstep(shadowR * 1.02, shadowR * 0.86, r);
    if (inside > shadow){
      shadow = inside;
      shadowTint = tint[i];
    }

    // Pull the sample toward this hole. Deflections from several holes add,
    // which is what makes a binary pair bend the sky between them.
    vec2 dir = normalize(d + 1e-6);
    vec2 off = dir * bend;
    off.x /= aspect;
    totalOff += off;
    maxChroma = max(maxChroma, chroma[i]);

    // Photon ring: a bright circle at the last stable orbit. Some holes have
    // none at all, which is why ringAmt can be zero.
    if (ringAmt[i] > 0.001){
      // The ring forms at the lensing scale, not the shadow's edge.
      float rw = max(lensR[i] * 0.05, 0.004);
      float ring = exp(-pow((r - lensR[i] * ringRadius[i]) / max(rw, 1e-4), 2.0));
      rings += tint[i] * ring * ringAmt[i] * 1.6;
    }
  }

  // Chromatic split: each channel bends slightly differently. Applied once
  // to the summed deflection so overlapping lenses do not multiply the
  // fringing into a rainbow smear.
  vec2 uvR = uv - totalOff * (1.0 + maxChroma * 0.06);
  vec2 uvG = uv - totalOff;
  vec2 uvB = uv - totalOff * (1.0 - maxChroma * 0.06);

  // This is the sampling that bends the REAL scene - planets, nebulae,
  // station hulls, other ships - and not merely a procedural starfield.
  // Whatever was drawn before this pass is what gets warped.
  vec3 col = vec3(
    texture2D(textureSampler, clamp(uvR, 0.001, 0.999)).r,
    texture2D(textureSampler, clamp(uvG, 0.001, 0.999)).g,
    texture2D(textureSampler, clamp(uvB, 0.001, 0.999)).b
  );

  col += rings;

  // Even inside the shadow, keep a trace of the lensed background rather
  // than a flat fill: a completely uniform region reads as a rendering
  // failure, and the user reported exactly that. The horizon stays clearly
  // the darkest thing on screen without ever being a dead rectangle.
  col = mix(col, col * 0.06 + shadowTint * 0.035, shadow);
  gl_FragColor = vec4(col, 1.0);
}
`;

let registered = false;
function register(): void {
  if (registered) return;
  Effect.ShadersStore['universalLensFragmentShader'] = LENS_FRAG;
  registered = true;
}

/** How many holes can bend the screen at once. Must match the shader. */
export const MAX_LENSES = 4;

/** One hole's screen-space lens state. */
interface LensSlot {
  uv: { x: number; y: number };
  /** Apparent horizon radius in UV - the size of the black shadow. */
  radius: number;
  /** Einstein radius in UV - the angular scale over which light bends. */
  lensRadius: number;
  profile: LensProfile | null;
}

/**
 * Angular radius of the Einstein ring, in radians.
 *
 * theta_E = sqrt(2 * rs / D) for a source far behind a lens of Schwarzschild
 * radius rs at distance D. This is the scale that governs how far light is
 * displaced, and it is much larger than the horizon's angular size: at 388
 * units from a typical hole the horizon subtends about 0.05 rad while the
 * Einstein radius is about 0.35. Using the horizon for both is what made
 * the bend sub-pixel and the starfield look untouched.
 */
export function einsteinRadius(horizon: number, distance: number): number {
  if (!(horizon > 0) || !(distance > 0)) return 0;
  const v = Math.sqrt((2 * horizon) / distance);
  return Number.isFinite(v) ? v : 0;
}

export class LensFX {
  private pp: PostProcess | null = null;
  private scene: Scene | null = null;
  /**
   * Screen-space state, recomputed each frame - one entry per visible hole.
   *
   * Light from behind a cluster is bent by every hole in it, not only the
   * closest one, so the pass carries a set rather than a single centre.
   */
  private slots: LensSlot[] = [];
  private on = false;
  /** Multiplies every hole's strength; 0 disables lensing globally. */
  intensity = 1;

  attach(scene: Scene, camera: Camera): void {
    this.detach();
    register();
    try {
      this.pp = new PostProcess(
        'universalLens', 'universalLens',
        ['holeUV', 'holeR', 'strength', 'falloff', 'ringAmt', 'ringRadius',
         'symmetry', 'distortion', 'twist', 'chroma', 'tint', 'holeOn',
         'lensR', 'aspect', 'active'],
        null, 1.0, camera
      );
      this.scene = scene;

      // A post-process whose shader will not compile on this GPU is a
      // black-screen machine: Babylon skips the pass, and the frame it was
      // supposed to blit to the screen never arrives. jsdom's WebGL stub
      // compiles everything happily, so this can only be caught at runtime.
      // If the effect is still not ready after a couple of seconds, drop the
      // pass entirely and render unlensed rather than render nothing.
      this.compileWatch = 0;

      this.pp.onApply = (effect) => {
        // Uniform arrays are set wholesale: every slot must be written every
        // frame, or a slot left over from a previous frame keeps bending
        // light around a hole that is no longer there.
        const uvs: number[] = [];
        const radii: number[] = [];
        const lensRadii: number[] = [];
        const strengths: number[] = [];
        const falloffs: number[] = [];
        const ringAmts: number[] = [];
        const ringRadii: number[] = [];
        const symmetries: number[] = [];
        const distortions: number[] = [];
        const twists: number[] = [];
        const chromas: number[] = [];
        const tints: number[] = [];
        const ons: number[] = [];

        for (let i = 0; i < MAX_LENSES; i++) {
          const s = this.slots[i];
          const p = s?.profile ?? null;
          uvs.push(s ? s.uv.x : 0.5, s ? s.uv.y : 0.5);
          radii.push(s ? s.radius : 0);
          lensRadii.push(s ? s.lensRadius : 0);
          strengths.push((p?.strength ?? 1) * this.intensity);
          falloffs.push(p?.falloff ?? 2);
          ringAmts.push(p?.ring ?? 0.6);
          ringRadii.push(p?.ringRadius ?? 1.5);
          symmetries.push(p?.symmetry ?? 0);
          distortions.push(p?.distortion ?? 0);
          twists.push(p?.twist ?? 0);
          chromas.push(p?.chroma ?? 1);
          const t = p?.tint ?? [0.7, 0.8, 1];
          tints.push(t[0], t[1], t[2]);
          ons.push(s ? 1 : 0);
        }

        effect.setArray2('holeUV', uvs);
        effect.setArray('holeR', radii);
        effect.setArray('lensR', lensRadii);
        effect.setArray('strength', strengths);
        effect.setArray('falloff', falloffs);
        effect.setArray('ringAmt', ringAmts);
        effect.setArray('ringRadius', ringRadii);
        effect.setArray('symmetry', symmetries);
        effect.setArray('distortion', distortions);
        effect.setArray('twist', twists);
        effect.setArray('chroma', chromas);
        effect.setArray3('tint', tints);
        effect.setArray('holeOn', ons);
        const eng = this.scene?.getEngine();
        // Same NaN hazard as the black hole world: a canvas mid-resize
        // gives 0/0. See SafeUniforms.
        effect.setFloat('aspect',
          eng ? safeAspect(eng.getRenderWidth(), eng.getRenderHeight()) : 16 / 9);
        effect.setFloat('active', this.on && this.intensity > 0.001 ? 1 : 0);
      };
    } catch (e) {
      // Never let a missing post-process stop the sim rendering.
      console.warn('Gravitational lensing unavailable:', e);
      this.pp = null;
    }
  }

  /** Frames spent waiting for the lens shader to compile. */
  private compileWatch = 0;

  /**
   * Drops the pass if its shader never becomes ready. Returns true if the
   * lens is healthy, false once it has been disabled.
   */
  private ensureCompiles(): boolean {
    const pp = this.pp;
    if (!pp) return false;
    if (this.compileWatch < 0) return true;      // already verified good
    let ready = false;
    try { ready = pp.isReady(); } catch { ready = false; }
    if (ready) { this.compileWatch = -1; return true; }
    this.compileWatch++;
    if (this.compileWatch > 150) {
      console.warn('Lens shader never compiled on this GPU - disabling lensing so the scene still draws.');
      this.detach();
      return false;
    }
    return true;
  }

  /**
   * Points the lens at a world-space position.
   *
   * @param center  the hole, in world space
   * @param horizon its horizon radius in world units
   * @param camera  the viewing camera
   * @param profile which lens type this hole uses
   */
  track(center: Vector3, horizon: number, camera: Camera, profile: LensProfile | null): void {
    this.trackMany([{ center, horizon, profile }], camera);
  }

  /**
   * Points the lens at every hole that is currently on screen.
   *
   * Holes beyond MAX_LENSES are dropped nearest-first, because the nearest
   * hole dominates the deflection by a wide margin and spending a slot on a
   * distant one buys nothing visible.
   */
  trackMany(
    holes: Array<{ center: Vector3; horizon: number; profile: LensProfile | null }>,
    camera: Camera
  ): void {
    const scene = this.scene;
    if (!scene) { this.slots = []; this.on = false; return; }
    if (!this.ensureCompiles()) { this.slots = []; this.on = false; return; }
    if (!Array.isArray(holes) || !holes.length) {
      this.slots = []; this.on = false; return;
    }

    const eng = scene.getEngine();
    const w = eng.getRenderWidth() || 1;
    const h = eng.getRenderHeight() || 1;

    const view = camera.getViewMatrix();
    const proj = camera.getProjectionMatrix();
    // The forward vector is read straight out of the view matrix rather than
    // via camera.getForwardRay(). getForwardRay() constructs a Ray, and Ray
    // is a side-effect import in Babylon's tree-shaken build: calling it
    // without that import throws *inside the render loop*, killing the frame
    // and leaving a permanently black canvas. The third row of the view
    // matrix is the same vector with no dependency at all.
    const vm = view.m;
    const fwd = new Vector3(vm[2], vm[6], vm[10]);
    const vp = view.multiply(proj);
    const fov = (camera as unknown as { fov?: number }).fov ?? 0.9;

    const found: Array<LensSlot & { dist: number }> = [];

    for (const hole of holes) {
      if (!hole || !hole.center) continue;
      const toCam = hole.center.subtract(camera.position);
      const dist = toCam.length();
      // Behind the camera: nothing to bend.
      if (Vector3.Dot(toCam, fwd) <= 0) continue;

      const p = Vector3.Project(
        hole.center, Matrix.Identity(), vp, new Viewport(0, 0, w, h));

      const uv = { x: p.x / w, y: 1 - p.y / h };   // screen is top-down
      const apparent = dist > 1e-6
        ? Math.atan(hole.horizon / dist) / Math.max(fov * 0.5, 1e-4) * 0.5
        : 1;
      const radius = Math.max(0.0005, Math.min(1.5, apparent));

      // The lensing scale, in the same UV units. Clamped well above the
      // shadow so the bend is always visible, and below 0.9 so a close pass
      // cannot smear the entire frame into unreadable mush.
      const thetaE = dist > 1e-6
        ? einsteinRadius(hole.horizon, dist) / Math.max(fov * 0.5, 1e-4) * 0.5
        : 1;
      const lensRadius = Math.max(radius, Math.min(0.9, thetaE));

      const onScreen = uv.x > -0.6 && uv.x < 1.6 && uv.y > -0.6 && uv.y < 1.6;
      if (!onScreen || !(radius > 0.001) || !Number.isFinite(radius)) continue;

      found.push({ uv, radius, lensRadius, profile: hole.profile, dist });
    }

    // Nearest first, then keep only what the shader has room for.
    found.sort((a, b) => a.dist - b.dist);
    this.slots = found.slice(0, MAX_LENSES)
      .map(({ uv, radius, lensRadius, profile }) => ({ uv, radius, lensRadius, profile }));
    this.on = this.slots.length > 0;
  }

  /** No hole nearby: stop bending. */
  clear(): void { this.slots = []; this.on = false; }

  /** How many holes are bending the screen right now. */
  get activeCount(): number { return this.slots.length; }

  get isActive(): boolean { return this.on; }

  stats(): Record<string, string> {
    const biggest = this.slots.reduce((m, s) => Math.max(m, s.radius), 0);
    return {
      'Lensing': this.on ? 'active' : 'off',
      'Holes lensing': String(this.slots.length),
      'Lens size': this.on ? (biggest * 100).toFixed(1) + '% screen' : '—'
    };
  }

  detach(): void {
    try { this.pp?.dispose(); } catch { /* already gone */ }
    this.pp = null;
    this.scene = null;
    this.slots = [];
    this.on = false;
  }
}
