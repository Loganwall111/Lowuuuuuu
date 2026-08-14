/**
 * GalaxyFogShader — the Milky Way as actual volumetric fog.
 *
 * WHY THIS EXISTS. The gas was 9,000 additive sprites. Sprites cannot be
 * fog. Make them small and you see dots; make them large and you see
 * squares; soften their edges and you see soft dots. Every one of those was
 * tried and every one still read as particles, because a finite set of
 * discrete billboards is simply not a continuous medium. The only way to
 * get cloud is to march a ray through a density field and integrate it.
 *
 * So this is a single inward-facing shell around the camera. For every
 * pixel it walks a ray outward through the galaxy's density function,
 * accumulating colour and extinction. What comes out is continuous: it has
 * no edges, no elements, no countable pieces. Flying into it genuinely
 * thickens because the ray spends more of its length inside dense material.
 *
 * WHAT IT IS NOT. It is not a skybox texture, and there are no image files
 * anywhere in it - the density and the colour are both evaluated from noise
 * in real time, which is the standing requirement for every sky in this
 * project.
 *
 * COLOUR. Deep space is pitch black; the fog is where all the colour lives.
 * Hue is driven by three independent low-frequency noise fields sampled in
 * world space, so different sectors of the galaxy are different colours -
 * crimson here, teal there, orange and magenta elsewhere - and they flow
 * into each other rather than switching. The dust lanes are carved by a
 * separate ridged field that ABSORBS instead of emitting, which is what
 * gives a real galaxy its dark filaments.
 */

import { Effect } from '@babylonjs/core/Materials/effect';

export const GALAXY_FOG_SHADER = 'galaxyFog';

export const GALAXY_FOG_VERT = `
precision highp float;
attribute vec3 position;
uniform mat4 worldViewProjection;
varying vec3 vDir;

void main(void) {
  // The shell is centred on the camera, so the vertex position IS the view
  // direction. No matrix inversion needed and no dependence on where the
  // camera actually is in world space.
  vDir = position;
  gl_Position = worldViewProjection * vec4(position, 1.0);
}
`;

export const GALAXY_FOG_FRAG = `
precision highp float;

varying vec3 vDir;

/** Camera position in GALAXY-LOCAL coordinates. */
uniform vec3 camPos;
/** Disc geometry, galaxy units. */
uniform float innerR;
uniform float outerR;
uniform float thickness;
/** Arm shape. */
uniform float arms;
uniform float armFactor;
/** 0 = photoreal palette, 1 = the rare neon anomaly. */
uniform float anomaly;
/** Overall brightness of the medium. */
uniform float density;
/** Slow drift so the medium is alive without pulsing. */
uniform float time;
/** How far the ray is allowed to travel, galaxy units. */
uniform float marchFar;

// ---------------------------------------------------------------- noise

vec3 hash33(vec3 p){
  p = vec3(dot(p, vec3(127.1, 311.7, 74.7)),
           dot(p, vec3(269.5, 183.3, 246.1)),
           dot(p, vec3(113.5, 271.9, 124.6)));
  return fract(sin(p) * 43758.5453123) * 2.0 - 1.0;
}

float vnoise(vec3 p){
  vec3 i = floor(p);
  vec3 f = fract(p);
  vec3 u = f * f * (3.0 - 2.0 * f);
  return mix(
    mix(mix(dot(hash33(i + vec3(0,0,0)), f - vec3(0,0,0)),
            dot(hash33(i + vec3(1,0,0)), f - vec3(1,0,0)), u.x),
        mix(dot(hash33(i + vec3(0,1,0)), f - vec3(0,1,0)),
            dot(hash33(i + vec3(1,1,0)), f - vec3(1,1,0)), u.x), u.y),
    mix(mix(dot(hash33(i + vec3(0,0,1)), f - vec3(0,0,1)),
            dot(hash33(i + vec3(1,0,1)), f - vec3(1,0,1)), u.x),
        mix(dot(hash33(i + vec3(0,1,1)), f - vec3(0,1,1)),
            dot(hash33(i + vec3(1,1,1)), f - vec3(1,1,1)), u.x), u.y),
    u.z) * 0.5 + 0.5;
}

float fbm(vec3 p, int oct){
  float s = 0.0, a = 0.5, n = 0.0;
  for (int i = 0; i < 6; i++){
    if (i >= oct) break;
    s += vnoise(p) * a;
    n += a;
    a *= 0.5;
    p *= 2.07;
  }
  return n > 0.0 ? s / n : 0.0;
}

// ------------------------------------------------------------- structure

/**
 * Gas density at a point in galaxy space.
 *
 * Disc confinement x radial extent x spiral arms x clumping. Every term is
 * smooth, so the result is a genuinely continuous medium.
 */
float galaxyDensity(vec3 p){
  float r = length(p.xz);
  if (r > outerR * 1.15) return 0.0;

  // Vertical: gas is thin and hugs the plane.
  float h = max(r * thickness, 1e-4);
  float plane = exp(-(p.y * p.y) / (2.0 * h * h));

  // Radial: hollow in the very centre, fading out past the rim.
  float inner = 1.0 - exp(-r / max(innerR, 1e-4));
  float outer = 1.0 - smoothstep(outerR * 0.62, outerR * 1.08, r);

  // Spiral arms. The angle a point "should" sit at grows with log(radius);
  // how close it is to that angle decides whether it is in an arm.
  float ang = atan(p.z, p.x);
  float spiral = ang - armFactor * log(max(r, 1.0) / max(innerR, 1.0));
  float armWave = cos(spiral * arms) * 0.5 + 0.5;
  // Arms are broad near the core and tighten outward; never fully empty
  // between them, or the disc looks like a pinwheel cut-out.
  float armMask = mix(0.35, 1.0, pow(armWave, 1.6));

  // Clumping, drifting very slowly. No sine, no pulsing - the drift is a
  // constant translation of the noise field.
  vec3 q = p * (1.6 / max(outerR, 1.0));
  float clump = fbm(q * 3.4 + vec3(time * 0.004, 0.0, time * 0.003), 5);
  clump = pow(clamp(clump, 0.0, 1.0), 1.7) * 2.1;

  return clamp(plane * inner * outer * armMask * clump, 0.0, 1.0);
}

/** Dark dust: ridged filaments that absorb rather than glow. */
float dustAt(vec3 p){
  float r = length(p.xz);
  vec3 q = p * (1.9 / max(outerR, 1.0));
  float ang = atan(p.z, p.x) * 0.5;
  float n = fbm(q * 2.2 + vec3(cos(ang) * 1.6, 0.0, sin(ang) * 1.6), 4);
  float ridged = 1.0 - abs(n - 0.5) * 2.0;
  float band = smoothstep(innerR * 0.7, outerR * 0.35, r)
             * (1.0 - smoothstep(outerR * 0.72, outerR * 1.05, r));
  return pow(max(ridged, 0.0), 3.0) * band;
}

// --------------------------------------------------------------- colour

/**
 * Emission colour of the medium at a point.
 *
 * Multiple hues flowing across sectors, chosen by their own low-frequency
 * fields so the colour varies with WHERE you are rather than with how dense
 * the gas is. This is what makes different regions of the galaxy read as
 * different places.
 */
vec3 gasColor(vec3 p, float d){
  float r = length(p.xz);
  float t = clamp(r / max(outerR, 1.0), 0.0, 1.0);

  vec3 base;
  if (anomaly > 0.5) {
    // The rare neon galaxy: hard magenta / teal emission.
    vec3 HA   = vec3(0.95, 0.13, 0.42);
    vec3 OIII = vec3(0.10, 0.88, 0.80);
    float sel = fbm(p * (2.6 / max(outerR, 1.0)) + 19.4, 4);
    base = mix(HA, OIII, smoothstep(0.42, 0.62, sel));
  } else {
    // Photoreal: brilliant creamy gold core, cooling outward to blue-white
    // and finally to a deep indigo halo.
    vec3 CORE = vec3(1.00, 0.90, 0.68);
    vec3 DISC = vec3(0.72, 0.80, 1.00);
    vec3 HALO = vec3(0.16, 0.20, 0.48);
    base = t < 0.32
      ? mix(CORE, DISC, t / 0.32)
      : mix(DISC, HALO, (t - 0.32) / 0.68);

    // Sector tinting. Three independent fields at different frequencies and
    // offsets, so patches of the disc lean crimson, teal or orange without
    // any of them dominating. This is the "different colours mixed through
    // the fog" that a single ramp can never produce.
    float f1 = fbm(p * (0.9 / max(outerR, 1.0)) + 4.1, 3);
    float f2 = fbm(p * (1.7 / max(outerR, 1.0)) - 11.7, 3);
    float f3 = fbm(p * (2.9 / max(outerR, 1.0)) + 27.3, 3);

    vec3 CRIMSON = vec3(1.00, 0.32, 0.30);
    vec3 TEAL    = vec3(0.30, 0.92, 0.86);
    vec3 ORANGE  = vec3(1.00, 0.62, 0.26);

    base = mix(base, CRIMSON, smoothstep(0.55, 0.80, f1) * 0.55);
    base = mix(base, TEAL,    smoothstep(0.58, 0.82, f2) * 0.45);
    base = mix(base, ORANGE,  smoothstep(0.56, 0.81, f3) * 0.40);
  }

  // Denser gas is hotter and whiter at its core.
  return mix(base, base + vec3(0.35), pow(d, 2.2) * 0.35);
}

// ---------------------------------------------------------------- march

void main(void) {
  vec3 dir = normalize(vDir);

  // Step count is fixed: a per-pixel loop bound would be a dynamic branch
  // and WebGL1 will not compile it. 48 steps is enough for smooth cloud at
  // this scale without stalling integrated GPUs.
  const int STEPS = 48;
  float far = max(marchFar, 1.0);
  float dt = far / float(STEPS);

  // Dither the entry point. Without this the fixed step size lays down
  // visible concentric shells - the classic raymarch banding artefact.
  float jitter = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233)))
                       * 43758.5453);

  vec3 acc = vec3(0.0);
  float trans = 1.0;

  for (int i = 0; i < STEPS; i++){
    float s = (float(i) + jitter) * dt;
    vec3 pos = camPos + dir * s;

    float d = galaxyDensity(pos);
    if (d > 0.002) {
      // Dust removes light from everything behind it.
      float dust = dustAt(pos);
      float ext = (d * 0.85 + dust * 1.9) * dt * density * 0.0016;

      vec3 emit = gasColor(pos, d) * d;
      // Standard front-to-back integration.
      acc += emit * trans * dt * density * 0.0016;
      trans *= exp(-ext);
      if (trans < 0.004) break;
    }
  }

  // Tone map. The march can accumulate well past 1.0 looking down the long
  // axis of the disc, and clipping that would flatten the core into a white
  // disc with a hard edge.
  vec3 col = acc / (acc + vec3(0.85));
  col = pow(max(col, 0.0), vec3(1.0 / 2.2));

  // Additive over the black void: alpha carries how much of the background
  // this pixel's medium has replaced.
  float a = clamp(1.0 - trans, 0.0, 1.0);
  gl_FragColor = vec4(col, a);
}
`;

let registered = false;

/** Registers the fog shader once. */
export function registerGalaxyFogShader(): void {
  if (registered) return;
  Effect.ShadersStore[GALAXY_FOG_SHADER + 'VertexShader'] = GALAXY_FOG_VERT;
  Effect.ShadersStore[GALAXY_FOG_SHADER + 'FragmentShader'] = GALAXY_FOG_FRAG;
  registered = true;
}
