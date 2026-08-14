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

/**
 * Disc half-height as a fraction of the galaxy radius.
 *
 * Fixed rather than proportional to local radius: a proportional envelope
 * flares outward into a cone, which is what produced the tall vertical
 * cloud instead of a flat plane.
 */
const float DISC_HEIGHT = 0.040;
/** How tightly gas bunches into the arms. Higher = crisper streams. */
const float ARM_SHARPNESS = 4.0;
/** Density between the arms. Small but non-zero, so gaps are gas not holes. */
const float ARM_FLOOR = 0.06;
/** Overall disc density. */
const float DISC_GAIN = 2.4;
/** Bulge extent as a fraction of galaxy radius. */
const float BULGE_RADIUS = 0.085;
/** Bulge peak density. Must stay modest or it saturates over the arms. */
const float BULGE_GAIN = 1.35;
/** Vertical squash of the bulge. */
const float BULGE_FLATTEN = 0.55;
/** Spatial frequency of the dust lanes. High = fine winding filaments. */
const float DUST_FREQ = 7.0;
/** How crisp each lane is. */
const float DUST_SHARPNESS = 2.2;
/** How much light a dust lane removes. 0 = none, 1 = total. */
const float DUST_CUT = 0.88;
/**
 * Only ridge values above this become dust.
 *
 * The single most important dust constant: below it the lanes merge into a
 * sheet that covers the disc, above it they separate into filaments.
 */
const float DUST_THRESHOLD = 0.95;

/** Extent of the blazing nucleus, as a fraction of galaxy radius. */
const float NUCLEUS_RADIUS = 0.028;
/** How fast the blaze falls off. Higher = tighter, hotter core. */
const float NUCLEUS_FALLOFF = 1.9;
/** Emission strength of the nucleus. */
const float NUCLEUS_GAIN = 7.5;
/** Vertical squash of the nucleus. */
const float NUCLEUS_FLATTEN = 0.78;
/** Colour of the blaze: creamy gold running to white at the very centre. */
const vec3 NUCLEUS_COLOR = vec3(1.00, 0.93, 0.74);

/** Spatial frequency of the rare anomaly strands. */
const float ANOMALY_FREQ = 5.5;
/** Ridge cut for the strands, same reasoning as the dust threshold. */
const float ANOMALY_THRESHOLD = 0.80;
/** How strongly the neon overrides the photoreal base where it falls. */
const float ANOMALY_STRENGTH = 0.95;
/** How much gas the strands themselves contribute. */
const float ANOMALY_DENSITY = 1.10;

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

  // ---- disc plane ----
  //
  // FLAT. The height scale is a fixed fraction of the galaxy radius, not a
  // fraction of the local radius, so the disc does not flare into a cone
  // toward the rim. The small floor keeps the very centre from pinching to
  // a knife edge.
  float h = max(outerR * DISC_HEIGHT + r * 0.012, outerR * 0.008);
  float plane = exp(-(p.y * p.y) / (2.0 * h * h));

  // ---- logarithmic spiral arms ----
  //
  // A real spiral arm is a logarithmic curve: the angle a point should sit
  // at grows with the log of its radius. Measuring how far the point's
  // actual angle is from that ideal gives the arm mask.
  //
  // Sharpening it with a high power is what turns a soft radial gradient
  // into crisp winding streams with genuinely dark vacuum between them.
  float ang = atan(p.z, p.x);
  float arm = armFactor * log(max(r, innerR) / max(innerR, 1.0));
  float armWave = cos(ang * arms - arm * arms) * 0.5 + 0.5;
  // pow(mask, 4) is the difference between "haze that is slightly brighter
  // in places" and "arms". The floor is small but non-zero so the
  // inter-arm gaps read as thin gas rather than as hard cut-outs.
  float armMask = ARM_FLOOR + (1.0 - ARM_FLOOR) * pow(armWave, ARM_SHARPNESS);

  // Arms are only a disc feature. Near the centre the bulge takes over, so
  // the spiral is faded out there rather than winding into the nucleus.
  armMask = mix(1.0, armMask, smoothstep(innerR * 0.5, outerR * 0.22, r));

  // ---- radial falloff of the disc ----
  // Exponential, the way a real disc's surface brightness behaves.
  float radial = exp(-r / (outerR * 0.42)) * rim;

  // ---- clumping ----
  // Broad continuous swells rather than isolated spikes, so the medium
  // reads as cloud instead of grain.
  vec3 q = p * (1.6 / max(outerR, 1.0));
  float clump = fbm(q * 2.2 + vec3(time * 0.004, 0.0, time * 0.003), 5);
  clump = 0.35 + 0.65 * smoothstep(0.10, 0.88, clump);

  float disc = plane * radial * armMask * clump * DISC_GAIN;

  // ---- central bulge ----
  //
  // A flattened spheroid of old starlight. It must NOT swallow the disc:
  // at radius 0.30 x outerR and amplitude 9.0 this term clamped the density
  // to 1.0 out to 25,000 units - half the galaxy - and a saturated sphere
  // has no structure at all. That is exactly the airbrushed blob. It is now
  // tight and moderate, so it reads as a bright nucleus that the arms wind
  // out of rather than a ball the arms are buried inside.
  float br = length(vec3(p.x, p.y / BULGE_FLATTEN, p.z));
  float bulge = exp(-pow(br / max(outerR * BULGE_RADIUS, 1.0), 1.7)) * BULGE_GAIN;

  // A Class-C anomaly's strands carry their OWN gas.
  //
  // Recolouring the gaps alone did nothing visible: the inter-arm gaps are
  // 17x thinner than the arm crests, so tinting them painted colour onto
  // almost no medium and the "legendary find" was invisible. The strands
  // therefore ADD density where they fall, which is also physically right -
  // they are meant to be glowing gas threaded between the arms, not a
  // filter over empty space.
  float neonGas = anomaly > 0.5 ? anomalyStrand(p) * ANOMALY_DENSITY : 0.0;

  return clamp(disc + bulge + neonGas, 0.0, 1.0);
}

/**
 * Nucleus glare: the blaze at the dead centre of the galaxy.
 *
 * Kept OUT of galaxyDensity on purpose. Density is clamped to 1.0, so once
 * the bulge saturates - which it did, flat, out to 6,000 units - piling
 * more density on cannot make the centre any brighter and only flattens it
 * into a featureless white pad. Emission is not clamped, so the glare is
 * added as light instead: it can blaze at the very middle and fall off
 * smoothly without ever pinning the density field.
 *
 * Returned separately so the march can add it as pure emission that the
 * dust lanes still absorb, which is what makes the nucleus appear to
 * illuminate the lanes crossing in front of it.
 */
/**
 * The rare Class-C anomaly's strand field: 1 inside a neon filament, 0
 * outside. Shared by the density and the colour so the two cannot drift
 * apart and leave coloured gas where there is no gas, or vice versa.
 *
 * Placed in the inter-arm GAPS by inverting the arm mask, so the strands
 * thread between the spiral tracks instead of covering them.
 */
float anomalyStrand(vec3 p){
  float r = length(p.xz);
  float t = clamp(r / max(outerR, 1.0), 0.0, 1.0);
  float ang = atan(p.z, p.x);
  float arm = armFactor * log(max(r, innerR) / max(innerR, 1.0));
  float wave = cos(ang * arms - arm * arms) * 0.5 + 0.5;
  float gaps = pow(1.0 - wave, 2.0);

  vec3 sq = p * (ANOMALY_FREQ / max(outerR, 1.0));
  float sn = fbm(sq + 51.7, 4);
  float strand = smoothstep(ANOMALY_THRESHOLD, 1.0, 1.0 - abs(sn - 0.5) * 2.0);

  // Confined to the disc plane and kept off the nucleus.
  float band = smoothstep(0.05, 0.20, t) * (1.0 - smoothstep(0.72, 1.05, t));
  float h = max(outerR * DISC_HEIGHT * 1.4, 1.0);
  float layer = exp(-(p.y * p.y) / (2.0 * h * h));
  return clamp(strand * gaps * band * layer, 0.0, 1.0);
}

float nucleusGlare(vec3 p){
  float gr = length(vec3(p.x, p.y / NUCLEUS_FLATTEN, p.z));
  float core = exp(-pow(gr / max(outerR * NUCLEUS_RADIUS, 1.0), NUCLEUS_FALLOFF));
  // A wider, fainter halo around the blaze so it fades into the bulge
  // rather than ending on a visible edge.
  float halo = exp(-pow(gr / max(outerR * NUCLEUS_RADIUS * 3.4, 1.0), 1.5));
  return core * NUCLEUS_GAIN + halo * NUCLEUS_GAIN * 0.22;
}

/** Dark dust: ridged filaments that absorb rather than glow. */
float dustAt(vec3 p){
  float r = length(p.xz);

  // HIGH-FREQUENCY fBm, sheared along the spiral.
  //
  // Frequency matters more than amplitude here. Low-frequency dust is just
  // a soft shadow across the disc; the sharp winding filaments that give a
  // real galaxy its contrast only appear once the noise is fine enough to
  // resolve lanes narrower than an arm. Shearing the sample by the spiral
  // angle makes the lanes follow the arms rather than cutting across them.
  float ang = atan(p.z, p.x);
  float wind = armFactor * log(max(r, innerR) / max(innerR, 1.0));
  float sheared = ang - wind;
  vec3 q = vec3(cos(sheared) * r, p.y * 2.4, sin(sheared) * r)
         * (DUST_FREQ / max(outerR, 1.0));

  float n = fbm(q, 5);
  // Ridged noise: the ABSOLUTE value of a signed field creates creases,
  // which is what a filament is. A plain fbm gives blobs instead.
  float ridged = 1.0 - abs(n - 0.5) * 2.0;

  // THRESHOLD, NOT JUST A POWER.
  //
  // fbm concentrates hard around 0.5, so this ridge function sits near 1.0
  // across almost the whole disc - measured median 0.85, with 80% of the
  // disc reading as lane. Raising it to a power only dims that blanket
  // instead of breaking it up, so the dust came out as a uniform grey wash
  // rather than as filaments. Cutting everything below the crest line and
  // rescaling what survives is what leaves isolated winding lanes with
  // clean gas between them.
  ridged = smoothstep(DUST_THRESHOLD, 1.0, ridged);

  // Dust lives in the disc, not in the halo and not in the nucleus.
  float band = smoothstep(innerR * 0.7, outerR * 0.30, r)
             * (1.0 - smoothstep(outerR * 0.72, outerR * 1.05, r));
  // A thin layer: dust hugs the mid-plane more tightly than the gas does.
  float layer = exp(-(p.y * p.y) / (2.0 * pow(outerR * 0.022, 2.0)));

  return pow(max(ridged, 0.0), DUST_SHARPNESS) * band * layer;
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
  {
    // Photoreal is now the ONLY base layout, for every galaxy including the
    // anomalies. Brilliant creamy solar gold at the bulge, cooling through
    // blue-white to a deep indigo halo - the Andromeda ramp.
    //
    // The anomaly used to REPLACE this entirely with flat neon, which threw
    // away the spiral structure and the gold core that took this long to
    // get right. It is now an overlay woven between the arms instead, so a
    // rare galaxy is a recognisable galaxy wearing something extraordinary
    // rather than a different object.
    vec3 CORE = vec3(1.00, 0.90, 0.60);
    vec3 DISC = vec3(0.50, 0.64, 1.00);
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
      // THE BULGE KEEPS ITS GOLD.
      //
      // At full strength the sector hue overwrote the core too, and the
      // centre of the galaxy came out blue-violet (0.25, 0.31, 0.57)
      // instead of creamy gold. In a real galaxy the bulge is old dense
      // starlight, not emission nebulae, so the coloured gas belongs in
      // the ARMS. Fading the tint out toward the centre keeps the
      // Andromeda look: gold heart, coloured arms.
      // The nucleus keeps its gold and the arms carry the emission
      // colour, with a wide crossfade so the bulge grows smoothly out into
      // the arms rather than ending on a visible ring.
      float armsOnly = smoothstep(0.06, 0.34, t);
      base = mix(base, hue, smoothstep(0.0, 0.55, wsum) * TINT_AMOUNT * armsOnly);
    }
  }

  // ---- RARE CLASS-C ANOMALY: NEON STRANDS BETWEEN THE ARMS ----
  //
  // Woven into the GAPS, not painted over the whole galaxy. The inter-arm
  // regions are where the arm mask is weakest, so recomputing it here and
  // inverting it puts the strands exactly in the dark lanes between the
  // spiral tracks - which is what makes them read as something threaded
  // through the galaxy rather than a recolour of it.
  if (anomaly > 0.5) {
    // Exactly the field the density used, so the colour lands on the gas
    // the strands actually created.
    float strand = anomalyStrand(p);
    // Which line is glowing here: H-alpha magenta or O-III teal, chosen by
    // its own low-frequency field so the two separate into regions.
    float pick = fbm(p * (0.5 / max(outerR, 1.0)) + 8.3, 3);
    vec3 HA   = vec3(1.00, 0.10, 0.62);
    vec3 OIII = vec3(0.05, 0.95, 0.85);
    vec3 neon = mix(HA, OIII, smoothstep(0.40, 0.60, pick));
    base = mix(base, neon, clamp(strand * ANOMALY_STRENGTH, 0.0, 1.0));
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

/**
 * Ray vs the galaxy's bounding sphere, centred on the galactic origin.
 *
 * Returns the entry and exit distances along the ray, or a degenerate
 * range when the ray misses entirely.
 */
vec2 galaxySpan(vec3 ro, vec3 rd, float R){
  float b = dot(ro, rd);
  float c = dot(ro, ro) - R * R;
  float h = b * b - c;
  if (h < 0.0) return vec2(1.0, -1.0);   // miss
  h = sqrt(h);
  return vec2(-b - h, -b + h);
}

void main(void) {
  vec3 dir = normalize(vDir);

  // Step count is fixed: a per-pixel loop bound would be a dynamic branch
  // and WebGL1 will not compile it. 48 steps is enough for smooth cloud at
  // this scale without stalling integrated GPUs.
  const int STEPS = 48;

  // CONCENTRATE THE SAMPLES ON THE GALAXY.
  //
  // Marching a fixed 130,000 units gives a 2,708-unit step, but the disc is
  // only 600-3,000 units thick. Seen face-on a ray therefore crossed the
  // whole galaxy in well under one sample - measured alpha 0.003 - so the
  // galaxy almost vanished from outside while still looking dense edge-on,
  // where the ray runs ALONG the disc and lands many samples. That single
  // sampling failure is why it read as a faint local wisp instead of a
  // galaxy.
  //
  // Clipping the march to the galaxy's bounding sphere makes the step size
  // adapt: distant face-on rays now spend all 48 samples inside the disc
  // instead of throwing most of them away in empty space.
  float R = outerR * 1.30;
  vec2 span = galaxySpan(camPos, dir, R);
  if (span.y < span.x) { gl_FragColor = vec4(0.0); return; }

  float t0 = max(span.x, 0.0);
  float t1 = min(span.y, max(marchFar, 1.0));
  if (t1 <= t0) { gl_FragColor = vec4(0.0); return; }

  float far = t1 - t0;
  float dt = far / float(STEPS);

  // Dither the entry point. Without this the fixed step size lays down
  // visible concentric shells - the classic raymarch banding artefact.
  float jitter = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233)))
                       * 43758.5453);

  vec3 acc = vec3(0.0);
  float trans = 1.0;

  for (int i = 0; i < STEPS; i++){
    float s = t0 + (float(i) + jitter) * dt;
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
      // 5e-5, not 1e-5. Sharpening the arms and carving the dust lanes
      // removed most of the gas by design, which dropped the whole galaxy
      // to alpha 0.06 - structurally correct but nearly invisible. The
      // extinction coefficient is what restores its presence WITHOUT
      // refilling the gaps, since it scales what is there rather than
      // adding anything back.
      float ext = (d * 0.85 + dust * 1.9) * dt * density * 0.00005;

      // Absorb THEN emit, so a dense sample cannot both block the light
      // behind it and add its own at full strength in the same step. That
      // double-counting is what let the core stack toward pure white.
      float absorbed = 1.0 - exp(-ext);
      vec3 emit = gasColor(pos, d);

      // The nucleus blaze is emission, not density: it is added to the
      // light leaving this sample so it can out-shine everything without
      // saturating the medium.
      float glare = nucleusGlare(pos);
      emit += NUCLEUS_COLOR * glare;

      // DUST CARVES THE GAS.
      //
      // Extinction alone only dims what is BEHIND a lane, which at these
      // densities is a barely visible haze. Subtracting the dust from the
      // emission as well means a lane also blanks the gas it runs through,
      // which is what produces hard dark filaments cutting across bright
      // arms instead of a soft airbrushed wash.
      emit *= 1.0 - DUST_CUT * dust;

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
