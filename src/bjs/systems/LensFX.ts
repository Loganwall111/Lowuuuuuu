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
import { Matrix } from '@babylonjs/core/Maths/math.vector';
import { Viewport } from '@babylonjs/core/Maths/math.viewport';
import type { Camera } from '@babylonjs/core/Cameras/camera';
import type { Scene } from '@babylonjs/core/scene';
import type { LensProfile } from './LensProfiles';

const LENS_FRAG = `
precision highp float;
varying vec2 vUV;
uniform sampler2D textureSampler;

/** Hole centre in screen space, plus how strongly it bends. */
uniform vec2  holeUV;
uniform float holeR;        // apparent radius of the horizon, in UV units
uniform float strength;     // overall bend
uniform float falloff;      // how quickly the bend decays with distance
uniform float ringAmt;      // photon ring brightness (0 = ringless)
uniform float ringRadius;
uniform float symmetry;     // 0 = radial, 6 = six-fold, ...
uniform float distortion;
uniform float twist;
uniform float chroma;
uniform vec3  tint;
uniform float aspect;
uniform float active;       // 0 disables the whole pass

void main(void){
  vec2 uv = vUV;

  if (active < 0.5){
    gl_FragColor = texture2D(textureSampler, uv);
    return;
  }

  // Work in aspect-corrected space so the lens stays circular.
  vec2 d = uv - holeUV;
  d.x *= aspect;
  float r = length(d);
  float ang = atan(d.y, d.x);

  // Alien lens shapes: the same knobs the raymarcher uses, so a hole looks
  // like itself whether you are inside its world or flying past it.
  float shape = 1.0 + cos(ang * symmetry) * distortion
                    + sin(ang + twist / max(r, 0.35)) * distortion * 0.6;
  shape = max(shape, 0.02);

  // Deflection falls off with distance like a real photon path.
  float rr = max(r, 1e-4);
  float bend = strength * holeR * pow(clamp(holeR / rr, 0.0, 1.0), max(falloff - 1.0, 0.0)) * shape;

  // Inside the horizon there is nothing to sample: fall to the tint rather
  // than pure black, so the screen is never dead.
  float inside = smoothstep(holeR * 1.02, holeR * 0.86, r);

  // Pull the sample toward the hole.
  vec2 dir = normalize(d + 1e-6);
  vec2 off = dir * bend;
  off.x /= aspect;

  // Chromatic split: each channel bends slightly differently.
  vec2 uvR = uv - off * (1.0 + chroma * 0.06);
  vec2 uvG = uv - off;
  vec2 uvB = uv - off * (1.0 - chroma * 0.06);

  vec3 col = vec3(
    texture2D(textureSampler, clamp(uvR, 0.001, 0.999)).r,
    texture2D(textureSampler, clamp(uvG, 0.001, 0.999)).g,
    texture2D(textureSampler, clamp(uvB, 0.001, 0.999)).b
  );

  // Photon ring: a bright circle at the last stable orbit. Some holes have
  // none at all, which is why ringAmt can be zero.
  if (ringAmt > 0.001){
    float rw = holeR * 0.06;
    float ring = exp(-pow((r - holeR * ringRadius) / max(rw, 1e-4), 2.0));
    col += tint * ring * ringAmt * 1.6;
  }

  col = mix(col, tint * 0.05, inside);
  gl_FragColor = vec4(col, 1.0);
}
`;

let registered = false;
function register(): void {
  if (registered) return;
  Effect.ShadersStore['universalLensFragmentShader'] = LENS_FRAG;
  registered = true;
}

export class LensFX {
  private pp: PostProcess | null = null;
  private scene: Scene | null = null;
  /** Screen-space state, recomputed each frame. */
  private uv = { x: 0.5, y: 0.5 };
  private radius = 0;
  private on = false;
  private profile: LensProfile | null = null;
  /** Multiplies every hole's strength; 0 disables lensing globally. */
  intensity = 1;

  attach(scene: Scene, camera: Camera): void {
    this.detach();
    register();
    try {
      this.pp = new PostProcess(
        'universalLens', 'universalLens',
        ['holeUV', 'holeR', 'strength', 'falloff', 'ringAmt', 'ringRadius',
         'symmetry', 'distortion', 'twist', 'chroma', 'tint', 'aspect', 'active'],
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
        effect.setFloat2('holeUV', this.uv.x, this.uv.y);
        effect.setFloat('holeR', this.radius);
        const p = this.profile;
        effect.setFloat('strength', (p?.strength ?? 1) * this.intensity);
        effect.setFloat('falloff', p?.falloff ?? 2);
        effect.setFloat('ringAmt', p?.ring ?? 0.6);
        effect.setFloat('ringRadius', p?.ringRadius ?? 1.5);
        effect.setFloat('symmetry', p?.symmetry ?? 0);
        effect.setFloat('distortion', p?.distortion ?? 0);
        effect.setFloat('twist', p?.twist ?? 0);
        effect.setFloat('chroma', p?.chroma ?? 1);
        const t = p?.tint ?? [0.7, 0.8, 1];
        effect.setFloat3('tint', t[0], t[1], t[2]);
        const eng = this.scene?.getEngine();
        effect.setFloat('aspect', eng ? eng.getAspectRatio(camera) : 1.7);
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
    this.profile = profile;

    const scene = this.scene;
    if (!scene) { this.on = false; return; }
    if (!this.ensureCompiles()) { this.on = false; return; }

    // Project the hole into screen space.
    const eng = scene.getEngine();
    const w = eng.getRenderWidth() || 1;
    const h = eng.getRenderHeight() || 1;

    const view = camera.getViewMatrix();
    const proj = camera.getProjectionMatrix();
    const toCam = center.subtract(camera.position);
    const dist = toCam.length();

    // Behind the camera: nothing to bend.
    //
    // The forward vector is read straight out of the view matrix rather than
    // via camera.getForwardRay(). getForwardRay() constructs a Ray, and Ray
    // is a side-effect import in Babylon's tree-shaken build: calling it
    // without that import throws *inside the render loop*, killing the frame
    // and leaving a permanently black canvas. The third row of the view
    // matrix is the same vector with no dependency at all.
    const vm = view.m;
    const fwd = new Vector3(vm[2], vm[6], vm[10]);
    if (Vector3.Dot(toCam, fwd) <= 0) { this.on = false; return; }

    const p = Vector3.Project(
      center,
      Matrix.Identity(),
      view.multiply(proj),
      new Viewport(0, 0, w, h)
    );

    this.uv.x = p.x / w;
    // Screen space is top-down; UV is bottom-up.
    this.uv.y = 1 - p.y / h;

    // Apparent size of the horizon on screen, in UV units.
    const fov = (camera as unknown as { fov?: number }).fov ?? 0.9;
    const apparent = dist > 1e-6
      ? Math.atan(horizon / dist) / Math.max(fov * 0.5, 1e-4) * 0.5
      : 1;
    this.radius = Math.max(0.0005, Math.min(1.5, apparent));

    // Only bother when it is actually on screen and big enough to see.
    const onScreen = this.uv.x > -0.6 && this.uv.x < 1.6 &&
                     this.uv.y > -0.6 && this.uv.y < 1.6;
    this.on = onScreen && this.radius > 0.001 && Number.isFinite(this.radius);
  }

  /** No hole nearby: stop bending. */
  clear(): void { this.on = false; }

  get isActive(): boolean { return this.on; }

  stats(): Record<string, string> {
    return {
      'Lensing': this.on ? 'active' : 'off',
      'Lens size': this.on ? (this.radius * 100).toFixed(1) + '% screen' : '—'
    };
  }

  detach(): void {
    try { this.pp?.dispose(); } catch { /* already gone */ }
    this.pp = null;
    this.scene = null;
    this.on = false;
  }
}
