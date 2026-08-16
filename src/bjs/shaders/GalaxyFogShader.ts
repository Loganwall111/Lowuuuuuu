/**
 * GalaxyFogShader — a photorealistic spiral galaxy, raymarched.
 *
 * WHAT THIS IS. For every pixel on the screen a ray is walked through a
 * continuous 3D density field and the light along it is integrated. There
 * are no particles, no billboards and no image files: the structure and
 * the colour are both evaluated from noise in real time. Flying into the
 * disc genuinely thickens the medium because the ray spends more of its
 * length inside dense material.
 *
 * ---------------------------------------------------------------------
 * WHY THE PREVIOUS VERSION LOOKED WRONG, MEASURED RATHER THAN GUESSED
 * ---------------------------------------------------------------------
 * The old shader rendered a violet knot with a small dim core. Four
 * separate causes were found by rendering it offline and measuring it,
 * and all four are fixed here:
 *
 *  1. UNDERSAMPLING. The march covered the galaxy's 130,000-unit bounding
 *     sphere in 48 steps - a 2,700-unit step - while the disc is only
 *     about 1,500 units thick. A face-on ray therefore took LESS THAN ONE
 *     sample inside the disc, so every fine feature was stepped straight
 *     over and what survived was a soft airbrushed smudge. The march is
 *     now clipped to the slab that actually contains the disc, which puts
 *     all the samples in the material.
 *
 *  2. ONE STELLAR POPULATION. A single colour ramp keyed to radius cannot
 *     produce a galaxy, because a galaxy's colours come from two
 *     populations that live in different places: old gold stars
 *     concentrated in the bulge, and young blue-white stars confined to
 *     a thin layer along the arms. They are modelled separately here.
 *
 *  3. NO REAL EXTINCTION. Dust removed 0.5% of the light passing through
 *     it, so the dust lanes were invisible and the disc read as a
 *     uniform haze. Extinction is now strong AND chromatic - dust blocks
 *     blue harder than red, which is what makes real dust lanes warm
 *     brown rather than neutral grey.
 *
 *  4. AN UNTRUNCATED BULGE. A Sersic profile with an index below 1 has a
 *     tail that never reaches zero; measured, it lit the ENTIRE sky to
 *     luminance 0.14, including sightlines pointing away from the galaxy.
 *     Deep space was therefore grey rather than black. The bulge is now
 *     truncated.
 *
 * A fifth defect appeared only from INSIDE the disc: faint inter-arm gas
 * that should read as empty sky was lifted by the 1/2.2 gamma curve into
 * a flat grey wash across the whole screen. A filmic toe now crushes that
 * floor to true black without touching the bright core.
 *
 * ---------------------------------------------------------------------
 * THE GEOMETRY IS THE APP'S, NOT THE TEST'S
 * ---------------------------------------------------------------------
 * This is tuned against arms = 2 and armFactor = 2.6 - a grand-design
 * two-arm spiral, like the reference photograph. That choice is load
 * bearing: the number of radial cycles a spiral shows is
 * arms * armFactor * ln(outerR / innerR) / 2pi, so the previous 4-arm
 * armFactor 4.2 configuration produced 7.7 cycles and rendered as a set
 * of concentric rings rather than as arms. Two arms at 2.6 gives 2.4
 * cycles and a 21-degree pitch angle, which is what a grand-design
 * spiral actually looks like.
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
  // direction: every pixel of the screen gets its own ray without needing
  // to invert a projection matrix.
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

// ----------------------------------------------------------- structure

/** Disc scale height as a fraction of galaxy radius. Fixed, not
 *  proportional to local radius - a proportional envelope flares the disc
 *  outward into a cone. */
const float DISC_HEIGHT = 0.024;
/** Exponential radial scale length of the disc. */
const float DISC_SCALE = 0.38;
/** How tightly gas bunches onto the arm ridge. Higher = crisper arms. */
const float ARM_SHARPNESS = 3.4;
/** Density between the arms. Small but non-zero: gaps are thin gas, not
 *  holes. Measured - at 0.18 the inter-arm pedestal filled the sky when
 *  seen from inside the disc. */
const float ARM_FLOOR = 0.05;
/** How far the arms are allowed to wander off the ideal logarithmic
 *  spiral. Real arms are ragged and flocculent; a clean cosine reads as
 *  machinery. */
const float ARM_NOISE = 2.10;

/** Effective radius of the bulge, as a fraction of galaxy radius. */
const float BULGE_RE = 0.055;
/** Sersic-like index. Below 1 gives the steep core and broad shoulder a
 *  real bulge has. */
const float BULGE_POW = 0.62;
/** Peak brightness of the bulge. */
const float BULGE_GAIN = 6.0;
/** Where the bulge starts being truncated, and where it ends. WITHOUT
 *  THIS the profile's tail never reaches zero and lights the whole sky. */
const float BULGE_CUT_IN = 0.14;
const float BULGE_CUT_OUT = 0.34;

/** The blazing nucleus at the dead centre. */
const float NUCLEUS_RE = 0.013;
const float NUCLEUS_POW = 1.10;
const float NUCLEUS_GAIN = 13.0;

/** Dust lane frequency, phase offset from the arm ridge, and profile. */
const float DUST_FREQ = 3.2;
/** Dust sits on the INNER edge of an arm, not on top of it, which is
 *  where a density wave compresses it in a real spiral. */
const float DUST_PHASE = 0.55;
const float DUST_BAND = 1.7;
const float DUST_GAIN = 2.1;
/** Dust hugs the mid-plane more tightly than the gas does. */
const float DUST_HEIGHT = 0.016;

/** HII regions: the pink beads strung along the arms of the reference. */
const float HII_FREQ = 9.0;
const float HII_THRESH = 0.72;
const float HII_GAIN = 4.2;

/** Young blue-white population: strength, and the star-forming ring. */
const float YOUNG_GAIN = 2.10;
const float YOUNG_RING = 3.4;
const float YOUNG_R0 = 0.40;
const float YOUNG_RW = 0.22;
/** Young stars live in a THIN layer - they have not had time to scatter
 *  out of the gas they formed in. Load bearing for the view from inside
 *  the disc: a thick young layer puts bright gas in every direction and
 *  washes the sky to grey. */
const float YOUNG_H = 0.010;

/** How strongly the OLD population follows the arms. At 0.75 the smooth
 *  old disc laid a pedestal over the structure and dropped the measured
 *  ring contrast from 11:1 to 2.6:1. */
const float OLD_ARM = 0.92;

/** Extinction cross-sections for gas and for dust. */
const float K_GAS = 0.9;
const float K_DUST = 26.0;
/** Overall extinction scale. */
const float SIGMA = 0.00006;
/** Overall emission scale. */
const float EMIT = 0.00011;
/** How much of a sample's own light a dust lane blocks. Extinction alone
 *  only dims what is BEHIND a lane; this is what makes a lane also blank
 *  the gas it runs through, producing hard dark filaments. */
const float DUST_CUT = 0.85;

/** Tone mapping. */
const float WHITE = 1.15;
/** Filmic toe. Faint gas must resolve to BLACK rather than to the grey
 *  that gamma would otherwise lift it to. */
const float TOE = 0.055;
/** Post-integration saturation recovery: summing many hues along a ray
 *  averages toward grey, so the dominant hue is pushed back afterwards. */
const float SATURATION_RECOVERY = 0.48;

/** Half-thickness of the slab the march is clipped to. */
const float SLAB_H = 0.12;

/** The rare Class-C anomaly's neon strands. */
const float ANOMALY_FREQ = 5.5;
const float ANOMALY_THRESHOLD = 0.80;
const float ANOMALY_GAIN = 2.6;

// ------------------------------------------------------------- palette

/** Creamy white-gold at the very centre. */
const vec3 C_NUCLEUS = vec3(1.00, 0.97, 0.90);
/** The gold bulge. */
const vec3 C_BULGE = vec3(1.00, 0.83, 0.53);
/** Warm tan of the inner disc. */
const vec3 C_INNER = vec3(0.98, 0.74, 0.45);
/** Blue-white young stars on the arms. */
const vec3 C_ARM = vec3(0.66, 0.79, 1.00);
/** Deep blue of the outer disc and halo. */
const vec3 C_OUTER = vec3(0.32, 0.38, 0.70);
/** H-alpha pink of the HII knots. */
const vec3 C_HII = vec3(1.00, 0.34, 0.50);
/** Anomaly emission lines. */
const vec3 C_HA = vec3(1.00, 0.10, 0.62);
const vec3 C_OIII = vec3(0.05, 0.95, 0.85);

/** Chromatic extinction. Dust blocks blue harder than red - this ratio is
 *  what makes a dust lane read as warm brown instead of neutral grey, and
 *  it reddens the core seen through the lanes exactly as in the
 *  reference. */
const vec3 EXT_RGB = vec3(1.00, 1.30, 1.75);

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

// ------------------------------------------------------------ structure

/**
 * Phase along the spiral. Zero on an arm ridge, growing to pi in the gap.
 *
 * The defining relation of a logarithmic spiral is that angle grows with
 * the log of radius. The fBm term perturbs that ideal so the arms are
 * ragged rather than mathematically clean.
 */
float armPhase(vec3 p, float r){
  float ang = atan(p.z, p.x);
  float wind = armFactor * log(max(r, innerR) / max(innerR, 1.0));
  float k = 1.9 / max(outerR, 1.0);
  float jitter = (fbm(vec3(p.x * k, p.y * k * 2.0, p.z * k), 3) - 0.5) * ARM_NOISE;
  return ang * arms - wind * arms + jitter * arms;
}

/** Smooth vertical + radial envelope of the disc. */
float discProfile(vec3 p, float r){
  float h = outerR * DISC_HEIGHT + r * 0.010;
  float plane = exp(-(p.y * p.y) / (2.0 * h * h));
  float radial = exp(-r / (outerR * DISC_SCALE));
  float rim = 1.0 - smoothstep(outerR * 0.88, outerR * 1.25, r);
  return plane * radial * rim;
}

/** How much of the arm ridge is at this point: ARM_FLOOR..1. */
float armMask(vec3 p, float r){
  float wave = cos(armPhase(p, r)) * 0.5 + 0.5;
  float m = ARM_FLOOR + (1.0 - ARM_FLOOR) * pow(wave, ARM_SHARPNESS);
  // Break the arms out of lock-step. Without this every arm is the same
  // curve at the same strength, and the spiral degenerates into a set of
  // concentric rings instead of reading as distinct ragged arms.
  float bk = 0.9 / max(outerR, 1.0);
  float brk = fbm(vec3(p.x * bk + 3.7, p.y * bk, p.z * bk - 2.1), 3);
  m *= 0.55 + 0.95 * brk;
  // Arms dissolve into the bulge near the centre rather than winding into
  // the nucleus.
  return mix(1.0, m, smoothstep(innerR * 0.5, outerR * 0.20, r));
}

/** The old gold population's bulge, truncated so it cannot light the sky. */
float bulgeAt(vec3 p){
  float br = length(vec3(p.x, p.y / 0.62, p.z));
  float core = exp(-pow(br / max(outerR * BULGE_RE, 1.0), BULGE_POW));
  float cut = 1.0 - smoothstep(outerR * BULGE_CUT_IN, outerR * BULGE_CUT_OUT, br);
  return core * cut * BULGE_GAIN;
}

/** The blazing nucleus. */
float nucleusAt(vec3 p){
  float nr = length(vec3(p.x, p.y / 0.80, p.z));
  return exp(-pow(nr / max(outerR * NUCLEUS_RE, 1.0), NUCLEUS_POW)) * NUCLEUS_GAIN;
}

/** Gas: what the medium is made of, used for extinction. */
float gasAt(vec3 p){
  float r = length(p.xz);
  float disc = discProfile(p, r) * armMask(p, r);
  float k = 2.4 / max(outerR, 1.0);
  float clump = 0.18 + 0.82 * smoothstep(0.15, 0.85,
    fbm(vec3(p.x * k, p.y * k * 2.0, p.z * k) + vec3(time * 0.004, 0.0, time * 0.003), 4));
  return disc * clump;
}

/** Dark dust: ragged lanes on the inner edge of each arm. */
float dustAt(vec3 p){
  float r = length(p.xz);
  float wave = cos(armPhase(p, r) + DUST_PHASE) * 0.5 + 0.5;
  float lane = pow(wave, DUST_BAND);
  float k = DUST_FREQ / max(outerR, 1.0);
  float rag = smoothstep(0.32, 0.78, fbm(vec3(p.x * k, p.y * k * 3.0, p.z * k), 4));
  float h = outerR * DUST_HEIGHT;
  float layer = exp(-(p.y * p.y) / (2.0 * h * h));
  float band = smoothstep(outerR * 0.02, outerR * 0.14, r)
             * (1.0 - smoothstep(outerR * 0.70, outerR * 1.00, r));
  return clamp(lane * rag * layer * band * DUST_GAIN, 0.0, 1.0);
}

/** The rare Class-C anomaly's strand field, woven into the inter-arm gaps. */
float anomalyStrand(vec3 p){
  float r = length(p.xz);
  float t = clamp(r / max(outerR, 1.0), 0.0, 1.0);
  float wave = cos(armPhase(p, r)) * 0.5 + 0.5;
  // Inverted arm mask: the strands thread BETWEEN the spiral tracks.
  float gaps = pow(1.0 - wave, 2.0);
  float k = ANOMALY_FREQ / max(outerR, 1.0);
  float sn = fbm(vec3(p.x * k + 51.7, p.y * k, p.z * k + 51.7), 4);
  float strand = smoothstep(ANOMALY_THRESHOLD, 1.0, 1.0 - abs(sn - 0.5) * 2.0);
  float band = smoothstep(0.05, 0.20, t) * (1.0 - smoothstep(0.72, 1.05, t));
  float h = max(outerR * DISC_HEIGHT * 1.4, 1.0);
  float layer = exp(-(p.y * p.y) / (2.0 * h * h));
  return clamp(strand * gaps * band * layer, 0.0, 1.0);
}

/**
 * Emission: colour AND intensity together.
 *
 * Two stellar populations that live in different places, plus HII knots.
 * This is the heart of the rewrite - a single radius-keyed ramp cannot
 * make a galaxy look real, because the gold and the blue in the reference
 * are not two ends of one gradient, they are two different populations
 * with different spatial distributions.
 */
vec3 emissionAt(vec3 p, float anom){
  float r = length(p.xz);
  float t = clamp(r / max(outerR, 1.0), 0.0, 1.0);
  float disc = discProfile(p, r);
  float armM = armMask(p, r);

  // ---- old population: gold, smooth, concentrated in the bulge ----
  float oldDisc = disc * 0.9 * mix(1.0, armM, OLD_ARM);
  float oldAmt = bulgeAt(p) + oldDisc;
  vec3 oldCol = mix(C_BULGE, C_INNER, smoothstep(0.02, 0.22, t));
  oldCol = mix(oldCol, C_OUTER, smoothstep(0.25, 0.85, t));

  // ---- young population: blue-white, thin, only on the arm ridges ----
  float ridge = pow(clamp((armM - ARM_FLOOR) / (1.0 - ARM_FLOOR), 0.0, 1.0), 1.4);
  float ringBoost = 1.0 + YOUNG_RING
    * exp(-pow((t - YOUNG_R0) / YOUNG_RW, 2.0));
  float yh = outerR * YOUNG_H;
  float thin = exp(-(p.y * p.y) / (2.0 * yh * yh));
  float youngAmt = disc * thin * ridge * YOUNG_GAIN * ringBoost
                 * smoothstep(0.05, 0.20, t);

  // ---- HII knots: pink beads strung along the arms ----
  float hk = HII_FREQ / max(outerR, 1.0);
  float hn = fbm(vec3(p.x * hk + 17.3, p.y * hk * 3.0, p.z * hk + 4.1), 3);
  float hii = smoothstep(HII_THRESH, 0.95, hn) * ridge * disc * HII_GAIN * ringBoost;

  vec3 col = oldCol * oldAmt
           + C_NUCLEUS * nucleusAt(p)
           + C_ARM * youngAmt
           + C_HII * hii;

  // ---- the rare anomaly, as an OVERLAY on a normal galaxy ----
  //
  // Not a replacement. The photoreal layout is the standard for every
  // galaxy; an anomaly is a recognisable spiral wearing something
  // extraordinary, with neon gas woven into its inter-arm gaps.
  if (anom > 0.5) {
    float strand = anomalyStrand(p);
    float pick = fbm(p * (0.5 / max(outerR, 1.0)) + 8.3, 3);
    vec3 neon = mix(C_HA, C_OIII, smoothstep(0.40, 0.60, pick));
    col += neon * strand * ANOMALY_GAIN;
  }
  return col;
}

// ----------------------------------------------------------------- march

/** Ray vs the galaxy's bounding sphere, centred on the galactic origin. */
vec2 galaxySpan(vec3 ro, vec3 rd, float R){
  float b = dot(ro, rd);
  float c = dot(ro, ro) - R * R;
  float h = b * b - c;
  if (h < 0.0) return vec2(1.0, -1.0);
  h = sqrt(h);
  return vec2(-b - h, -b + h);
}

void main(void) {
  vec3 dir = normalize(vDir);

  // Fixed step count: a per-pixel loop bound is a dynamic branch that
  // WebGL1 will not compile.
  const int STEPS = 48;

  float R = outerR * 1.30;
  vec2 span = galaxySpan(camPos, dir, R);
  if (span.y < span.x) { gl_FragColor = vec4(0.0); return; }

  float t0 = max(span.x, 0.0);
  float t1 = min(span.y, max(marchFar, 1.0));

  // ---- SLAB CLIPPING: put the samples where the galaxy actually is ----
  //
  // The bounding sphere is 130,000 units across; the disc is about 1,500
  // thick. Marching the sphere uniformly gave a 2,700-unit step, so a
  // face-on ray took less than ONE sample inside the disc and stepped
  // straight over every dust lane, arm ridge and HII knot. That
  // undersampling - not the density function - is why the galaxy rendered
  // as a soft smudge no matter how the density was tuned.
  //
  // Clipping to the slab that contains the disc concentrates all 48
  // samples in the material. Edge-on rays run along the slab and are
  // unaffected; face-on rays go from under one sample to all of them.
  float SLAB = outerR * SLAB_H;
  if (abs(dir.y) > 1e-6) {
    float a = (-SLAB - camPos.y) / dir.y;
    float b = (SLAB - camPos.y) / dir.y;
    t0 = max(t0, min(a, b));
    t1 = min(t1, max(a, b));
  } else if (abs(camPos.y) > SLAB) {
    gl_FragColor = vec4(0.0); return;
  }
  if (t1 <= t0) { gl_FragColor = vec4(0.0); return; }

  float dt = (t1 - t0) / float(STEPS);

  // Dither the entry point, or the fixed step lays down visible concentric
  // shells - the classic raymarch banding artefact.
  float jitter = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233)))
                       * 43758.5453);

  vec3 acc = vec3(0.0);
  // Transmittance is PER CHANNEL, because dust extinction is chromatic.
  vec3 trans = vec3(1.0);

  for (int i = 0; i < STEPS; i++){
    float s = t0 + (float(i) + jitter) * dt;
    vec3 pos = camPos + dir * s;

    float g = gasAt(pos);
    float d = dustAt(pos);
    vec3 e = emissionAt(pos, anomaly);
    if (g < 1e-4 && dot(e, vec3(1.0)) < 1e-4) continue;

    float sigma = (g * K_GAS + d * K_DUST) * dt * density * SIGMA;
    // A lane blanks the gas it runs through, not just what is behind it.
    float blocked = 1.0 - DUST_CUT * d;

    acc += e * blocked * trans * dt * EMIT;
    trans *= exp(-sigma * EXT_RGB);
    if (dot(trans, vec3(1.0)) < 0.01) break;
  }

  // ---- tone mapping ----
  //
  // On LUMINANCE, not per channel: per-channel Reinhard pulls the
  // brightest channel down hardest, so a saturated gold core converges on
  // white exactly where it is most intense.
  float lum = dot(acc, vec3(0.2126, 0.7152, 0.0722));
  float mapped = lum * (1.0 + lum / (WHITE * WHITE)) / (1.0 + lum);

  // FILMIC TOE. Measured from inside the disc, core-to-zenith contrast was
  // already 45:1 in linear light - the structure was right - but gamma
  // 1/2.2 lifts a linear 0.016 to sRGB 0.16, so thin inter-arm gas that
  // should read as empty sky came out as a flat grey wash over the whole
  // screen. This crushes that floor to true black while leaving the core
  // untouched, and being smooth it cannot band the way a hard black-point
  // subtraction does.
  mapped *= (lum * lum) / (lum * lum + TOE * TOE);

  vec3 col = lum > 1e-6 ? acc * (mapped / lum) : vec3(0.0);

  // Recover the hue that integrating many samples averaged away.
  float mean = (col.r + col.g + col.b) / 3.0;
  col = max(vec3(0.0), mean + (col - mean) * (1.0 + SATURATION_RECOVERY));

  // Normalise by the BRIGHTEST CHANNEL, after the saturation recovery
  // rather than before it. Recovery pushes channels apart, so a clamp
  // applied first can be undone by it and let a channel back past 1.0.
  // Dividing preserves the ratio between channels - which is the hue - so
  // the core cannot bleach: a naive clamp() would drive (1.03, 0.93, 0.74)
  // to (1.00, 0.93, 0.74) and shift it toward white.
  float pk = max(col.r, max(col.g, col.b));
  if (pk > 1.0) col /= pk;

  col = pow(max(col, 0.0), vec3(1.0 / 2.2));

  // ---- PREMULTIPLIED OUTPUT ----
  //
  // col is the light that actually reached the eye, and trans is how much
  // of the background survived - so the correct composite is
  //   result = col + background * trans
  // which is premultiplied alpha with alpha = 1 - trans.
  //
  // Under the ordinary ALPHA_COMBINE blend the material used before, the
  // GPU computes col * alpha + background * (1 - alpha) instead, which
  // multiplies the emission by its own coverage a SECOND time. Measured,
  // the galactic core came out at alpha 0.124, so the brightest object in
  // the scene was drawn at 12% of its brightness. That is a large part of
  // why the nucleus kept reading as dim no matter how hard it was driven.
  // The material sets ALPHA_PREMULTIPLIED to match this.
  float a = clamp(1.0 - dot(trans, vec3(1.0)) / 3.0, 0.0, 1.0);
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
