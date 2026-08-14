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

/**
 * How fast the sector hue varies across the galaxy. Low so a "sector" is a
 * large region you fly through, rather than noise you fly past.
 */
const float SECTOR_FREQ = 0.12;
/** How strongly the sector hue overrides the radial ramp. */
const float TINT_AMOUNT = 1.0;
/**
 * Post-integration saturation recovery.
 *
 * Front-to-back integration sums ~48 samples of DIFFERENT hues, and a sum
 * of many hues is grey - measured at 0.14 saturation even with fully
 * saturated source colours. Pushing each channel away from the pixel's own
 * mean afterwards restores the dominant hue that the averaging destroyed,
 * taking it to ~0.37 with strong sector-to-sector variation.
 */
const float SATURATION_RECOVERY = 2.0;

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
  // Fade out rather than cut out. Returning zero at the rim puts a
  // visible spherical edge on the fog.
  float rim = 1.0 - smoothstep(outerR * 0.86, outerR * 1.30, r);
  if (rim <= 0.0) return 0.0;

  // Vertical: gas is thin and hugs the plane, but the floor keeps the
  // envelope from pinching to a knife edge near the centre, which is what
  // made the core read as a hard bright sheet.
  float h = max(r * thickness, outerR * 0.012);
  float plane = exp(-(p.y * p.y) / (2.0 * h * h));

  // Radial: hollow in the very centre, fading out past the rim.
  float inner = 1.0 - exp(-r / max(innerR, 1e-4));
  float outer = rim;

  // Spiral arms. The angle a point "should" sit at grows with log(radius);
  // how close it is to that angle decides whether it is in an arm.
  float ang = atan(p.z, p.x);
  float spiral = ang - armFactor * log(max(r, 1.0) / max(innerR, 1.0));
  float armWave = cos(spiral * arms) * 0.5 + 0.5;
  // Arms are broad near the core and tighten outward; never fully empty
  // between them, or the disc looks like a pinwheel cut-out. Smoothstep
  // rather than pow so the arm edges dissolve instead of ramping.
  float armMask = mix(0.45, 1.0, smoothstep(0.0, 1.0, armWave));

  // Clumping, drifting very slowly. No sine, no pulsing - the drift is a
  // constant translation of the noise field.
  //
  // The exponent here decides whether the medium reads as cloud or as
  // grain. Sharpening it (1.7) pushed the field toward isolated high
  // spikes with empty gaps - which is precisely the "glitter storm" look.
  // Softening it, and running the result through a smoothstep, spreads
  // each clump into a broad continuous swell with no countable pieces.
  vec3 q = p * (1.6 / max(outerR, 1.0));
  float clump = fbm(q * 2.2 + vec3(time * 0.004, 0.0, time * 0.003), 5);
  clump = smoothstep(0.18, 0.92, clump) * 1.5;

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
    vec3 CORE = vec3(1.00, 0.78, 0.38);
    vec3 DISC = vec3(0.45, 0.62, 1.00);
    vec3 HALO = vec3(0.12, 0.14, 0.40);
    base = t < 0.32
      ? mix(CORE, DISC, t / 0.32)
      : mix(DISC, HALO, (t - 0.32) / 0.68);

    // Sector tinting by DOMINANT HUE, not by stacked layers.
    //
    // Mixing crimson, then teal, then orange one after another averages
    // them: every sector ends up some version of the same warm grey, which
    // measured at only 0.14 saturation. Instead the three fields COMPETE -
    // each is sharpened into a weight and the winner takes the sector - so
    // a region is decisively crimson OR teal OR gold, and neighbouring
    // regions differ from each other instead of converging.
    float sf = SECTOR_FREQ / max(outerR, 1.0);
    float f1 = fbm(p * sf + 4.1, 3);
    float f2 = fbm(p * (sf * 1.3) - 11.7, 3);
    float f3 = fbm(p * (sf * 1.7) + 27.3, 3);

    // Saturated emission lines, not pastels.
    vec3 CRIMSON = vec3(1.00, 0.13, 0.26);   // H-alpha
    vec3 TEAL    = vec3(0.06, 0.92, 0.88);   // O-III
    vec3 ORANGE  = vec3(1.00, 0.52, 0.08);   // S-II

    float w1 = pow(smoothstep(0.35, 0.75, f1), 3.0);
    float w2 = pow(smoothstep(0.35, 0.75, f2), 3.0);
    float w3 = pow(smoothstep(0.35, 0.75, f3), 3.0);
    float wsum = w1 + w2 + w3;
    if (wsum > 1e-4) {
      vec3 hue = (CRIMSON * w1 + TEAL * w2 + ORANGE * w3) / wsum;
      base = mix(base, hue, smoothstep(0.0, 0.55, wsum) * TINT_AMOUNT);
    }
  }

  // Denser gas is hotter, but it must NEVER be pushed toward white.
  //
  // The old line added a flat vec3(0.35) to every channel, which walks any
  // colour toward grey and then to white as it accumulates - that is what
  // bleached the core into a solid white mask and destroyed the hue there.
  // Instead, lift saturation slightly and keep the hue: dense gas becomes
  // a more intense version of its own colour, not a whiter one.
  vec3 hot = base * 1.22;
  base = mix(base, hot, smoothstep(0.25, 0.95, d));

  // HARD ANTI-BLEACH CLAMP. Normalise anything that has climbed past 1.0
  // back down by its own brightest channel, so the RATIO between channels -
  // which is the hue - survives no matter how bright the medium gets. A
  // naive clamp() would drive (2.0, 1.4, 0.9) to (1,1,0.9) and lose the
  // colour; this maps it to (1.0, 0.70, 0.45) and keeps it.
  float peak = max(base.r, max(base.g, base.b));
  if (peak > 1.0) base /= peak;
  return base;
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
      // The step is ~2,700 units long, so extinction per step saturates
      // almost instantly at the old 0.0016 coefficient: alpha measured
      // 1.000 everywhere over the disc, i.e. a fully opaque sheet, which
      // is what read as a flat white mask over the core. At 1e-5 the
      // medium is genuinely translucent (alpha ~0.66 looking through the
      // core) so stars and background survive behind it.
      float ext = (d * 0.85 + dust * 1.9) * dt * density * 0.00001;

      // Absorb THEN emit, so a dense sample cannot both block the light
      // behind it and add its own at full strength in the same step. That
      // double-counting is what let the core stack toward pure white.
      float absorbed = 1.0 - exp(-ext);
      vec3 emit = gasColor(pos, d);
      acc += emit * trans * absorbed;
      trans *= exp(-ext);
      if (trans < 0.004) break;
    }
  }

  // Tone map. The march can accumulate well past 1.0 looking down the long
  // axis of the disc, and clipping that would flatten the core into a white
  // disc with a hard edge.
  //
  // Reinhard is applied PER CHANNEL, which desaturates as it compresses:
  // the brightest channel is pulled down hardest, so a saturated gold core
  // converges on white exactly where it is most intense. Tone mapping on
  // luminance instead, and rescaling all three channels by the same factor,
  // compresses the brightness while leaving the hue untouched.
  float lum = dot(acc, vec3(0.2126, 0.7152, 0.0722));
  float mapped = lum / (lum + 0.85);
  vec3 col = lum > 1e-5 ? acc * (mapped / lum) : vec3(0.0);

  // Recover the hue that integration averaged away.
  float mean = (col.r + col.g + col.b) / 3.0;
  col = max(vec3(0.0), mean + (col - mean) * (1.0 + SATURATION_RECOVERY));

  // Final guard: never let any channel exceed a shade below full white, so
  // there is always some colour left in the brightest part of the galaxy.
  float pk = max(col.r, max(col.g, col.b));
  if (pk > 0.94) col *= 0.94 / pk;

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
