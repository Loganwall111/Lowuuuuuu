/**
 * WarpTunnel — the full-screen half of going fast.
 *
 * The thin-instanced streaks in WarpSystem give you objects rushing past,
 * but on their own they read as "some sticks are moving": there is no sense
 * that SPACE is doing something. This adds the screen-space half - the
 * radial rush toward the vanishing point, the tunnel walls closing in, the
 * blue/violet shift at the edges of vision.
 *
 * It is a shader, not geometry, for the same reason the black hole is: a
 * tunnel built out of a cone mesh has a mouth you can fly out of the side
 * of, and it has to be positioned, scaled and re-oriented every frame to
 * stay glued to the camera. A screen-space pass is always exactly where the
 * eye is, costs one quad, and cannot be escaped.
 *
 * NO PERIODIC TIME LOOPS. The tunnel is animated by an accumulating
 * DISTANCE, not by sin(time). `phase` only ever increases, driven by how
 * far you have actually flown, so nothing pulses, breathes or ripples on a
 * timer - it flows exactly as fast as you are travelling and stops dead
 * when you do.
 *
 * ORDERING. This must run BEFORE the gravitational lens: warp streaks are
 * light in the scene, and light in the scene is what a black hole bends. A
 * tunnel composited after the lens would sit flat on top of a warped image
 * and instantly read as an overlay.
 */

import { Effect } from '@babylonjs/core/Materials/effect';
import { PostProcess } from '@babylonjs/core/PostProcesses/postProcess';
import { Texture } from '@babylonjs/core/Materials/Textures/texture';
import type { Camera } from '@babylonjs/core/Cameras/camera';
import type { Scene } from '@babylonjs/core/scene';

/** Registered once; Babylon looks shaders up by this name. */
export const WARP_TUNNEL_EFFECT = 'warpTunnel';

export const WARP_TUNNEL_FRAG = `
precision highp float;
varying vec2 vUV;
uniform sampler2D textureSampler;

/** 0 = no warp, 1 = full. Everything scales off this. */
uniform float amount;
/** Accumulated travel. Only ever increases. Never a time loop. */
uniform float phase;
/** Aspect ratio, so the tunnel is round rather than an ellipse. */
uniform float aspect;
/** Where the vanishing point is, in UV. Follows the direction of travel. */
uniform vec2  focus;
/** Tint of the rush. Shifts with speed. */
uniform vec3  tintNear;
uniform vec3  tintFar;

/* Cheap hash noise. No trig, no loops, no periodicity. */
float hash21(vec2 p){
  p = fract(p * vec2(443.897, 441.423));
  p += dot(p, p + 19.19);
  return fract(p.x * p.y);
}

void main(void){
  vec4 base = texture2D(textureSampler, vUV);

  if (amount < 0.002){
    gl_FragColor = base;
    return;
  }

  // Work in aspect-corrected space centred on the vanishing point, so the
  // rush converges where the ship is actually pointed rather than always
  // at the middle of the screen.
  vec2 d = vUV - focus;
  d.x *= aspect;
  float r = length(d);
  float ang = atan(d.y, d.x);

  // ---- radial smear -------------------------------------------------
  // Pull the image outward along the radius. This is what makes the whole
  // frame appear to be flying past rather than sitting still behind some
  // streaks. Strength grows with radius so the centre stays readable -
  // you can still see where you are going at full warp.
  float smear = amount * 0.055 * smoothstep(0.03, 0.85, r);
  vec2 dir = r > 1e-5 ? d / r : vec2(0.0);
  dir.x /= max(aspect, 1e-5);

  // Four taps along the smear direction, weighted toward the true pixel.
  // Chromatic: the red tap is pulled slightly further than the blue one,
  // which is what gives the edges of a warp their prismatic fringe.
  vec3 acc = base.rgb * 0.40;
  acc += texture2D(textureSampler, vUV - dir * smear * 0.45).rgb * 0.26;
  acc += texture2D(textureSampler, vUV - dir * smear * 0.90).rgb * 0.20;
  acc += texture2D(textureSampler, vUV - dir * smear * 1.45).rgb * 0.14;
  float rTap = texture2D(textureSampler, vUV - dir * smear * 1.75).r;
  float bTap = texture2D(textureSampler, vUV - dir * smear * 0.55).b;
  acc.r = mix(acc.r, rTap, amount * 0.45);
  acc.b = mix(acc.b, bTap, amount * 0.45);

  // ---- the tunnel ----------------------------------------------------
  // Streaks in polar space, advancing on the phase uniform. Because it is
  // distance travelled, these flow at exactly the rate you are moving and
  // freeze when you stop - no oscillation anywhere.
  //
  // 1/r is the projection of a line receding to infinity, which is why
  // this reads as depth rather than as a flat pinwheel.
  float depth = 1.0 / max(r, 0.035);
  float lane  = floor(ang * 9.5);
  float jitter = hash21(vec2(lane, floor(depth * 0.55 + phase * 0.6)));
  float along = fract(depth * 0.55 + phase * 0.6 + jitter * 0.9);

  // A hard leading edge and a long tail: the shape of something moving
  // fast, rather than a symmetric blob.
  float streak = pow(1.0 - along, 5.0);
  // Thin in angle, so these are filaments and not wedges.
  float across = 1.0 - smoothstep(0.0, 0.34, abs(fract(ang * 9.5) - 0.5) * 2.0 - 0.66);
  across = clamp(across, 0.0, 1.0);

  // Fade the tunnel out of the centre: at the vanishing point a streak has
  // no length and would just be a dot sitting in the middle of the view.
  float radialMask = smoothstep(0.045, 0.42, r) * (1.0 - smoothstep(0.72, 1.25, r) * 0.55);
  float tunnel = streak * across * radialMask * jitter;

  // Colour: hotter and whiter near the middle, cold violet at the rim,
  // like real relativistic beaming rather than a flat blue wash.
  vec3 tint = mix(tintNear, tintFar, smoothstep(0.08, 0.8, r));
  acc += tint * tunnel * amount * 2.6;

  // Distance-triggered compression fronts: thin luminous shells crossing
  // the canopy as the drive folds another volume of space. They are tied to
  // travelled phase, not clock time, so cutting thrust freezes them exactly.
  float frontPos = fract(phase * 0.075) * 1.28;
  float front = exp(-abs(r - frontPos) * 72.0)
              * smoothstep(0.05, 0.22, r) * (1.0 - smoothstep(0.9, 1.3, r));
  float echoPos = fract(phase * 0.075 + 0.47) * 1.28;
  float echo = exp(-abs(r - echoPos) * 46.0) * 0.38;
  acc += mix(tintNear, vec3(0.72,0.48,1.0), r) * (front + echo) * amount * 0.72;

  // ---- vignette ------------------------------------------------------
  // The walls closing in. Subtle - it frames the rush without pretending
  // to be damage or a filter.
  float vig = 1.0 - smoothstep(0.42, 1.15, r) * amount * 0.5;
  acc *= vig;

  gl_FragColor = vec4(acc, base.a);
}
`;

export function registerWarpTunnelShader(): void {
  Effect.ShadersStore[WARP_TUNNEL_EFFECT + 'FragmentShader'] = WARP_TUNNEL_FRAG;
}

export interface WarpTunnelState {
  /** 0..1 engagement. */
  amount: number;
  /** Apparent flow this frame, world units. Accumulates into phase. */
  flow: number;
  /** Vanishing point in UV, usually the screen centre. */
  focusX: number;
  focusY: number;
}

export class WarpTunnel {
  private pp: PostProcess | null = null;
  private phase = 0;
  private amount = 0;
  private focus: [number, number] = [0.5, 0.5];
  private on = true;

  /** Whether the player has the effect switched on in settings. */
  get enabled(): boolean { return this.on; }
  setEnabled(v: boolean): void {
    this.on = v;
    if (!v) this.amount = 0;
  }

  get intensity(): number { return this.amount; }

  attach(scene: Scene, camera: Camera): void {
    if (this.pp) return;
    registerWarpTunnelShader();
    this.pp = new PostProcess(
      WARP_TUNNEL_EFFECT, WARP_TUNNEL_EFFECT,
      ['amount', 'phase', 'aspect', 'focus', 'tintNear', 'tintFar'],
      null, 1, camera, Texture.BILINEAR_SAMPLINGMODE, scene.getEngine(), false
    );
    this.pp.onApply = (effect) => {
      const e = scene.getEngine();
      const w = e.getRenderWidth() || 1;
      const h = e.getRenderHeight() || 1;
      effect.setFloat('amount', this.amount);
      effect.setFloat('phase', this.phase);
      effect.setFloat('aspect', w / Math.max(1, h));
      effect.setFloat2('focus', this.focus[0], this.focus[1]);
      // Warmer core, colder rim. At higher engagement the whole thing
      // shifts bluer, which is the closest honest nod to relativistic
      // beaming without pretending to simulate it.
      const a = this.amount;
      effect.setFloat3('tintNear', 0.85 - a * 0.2, 0.93, 1.0);
      effect.setFloat3('tintFar', 0.34 + a * 0.1, 0.42, 0.95);
    };
  }

  /**
   * @param dt    seconds
   * @param state engagement and apparent flow from WarpSystem
   */
  update(dt: number, state: WarpTunnelState): void {
    if (!Number.isFinite(dt) || dt <= 0) return;
    const target = this.on
      ? Math.max(0, Math.min(1, Number.isFinite(state.amount) ? state.amount : 0))
      : 0;
    // Ease, so the tunnel never pops on or off.
    this.amount += (target - this.amount) * Math.min(1, dt * 3.2);
    if (this.amount < 1e-4) this.amount = 0;

    // Phase advances on distance flown, never on wall-clock time. This is
    // what keeps the animation from being a periodic loop.
    const flow = Number.isFinite(state.flow) ? Math.max(0, state.flow) : 0;
    this.phase += flow * dt * 0.012;
    // Wrap to keep float precision sane over a long session, at a period
    // far longer than any single streak's life so no visible seam occurs.
    if (this.phase > 4096) this.phase -= 4096;

    if (Number.isFinite(state.focusX)) this.focus[0] = state.focusX;
    if (Number.isFinite(state.focusY)) this.focus[1] = state.focusY;
  }

  stats(): Record<string, string> {
    return {
      'Warp tunnel': !this.on ? 'off'
        : this.amount > 0.01 ? (this.amount * 100).toFixed(0) + '%' : 'idle'
    };
  }

  dispose(): void {
    this.pp?.dispose();
    this.pp = null;
  }
}
