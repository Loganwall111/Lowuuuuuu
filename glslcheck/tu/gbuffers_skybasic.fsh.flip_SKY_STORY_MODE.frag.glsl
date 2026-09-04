#version 330 core
uniform mat4 gbufferModelView, gbufferProjection, gbufferProjectionInverse;
uniform mat4 modelViewMatrix, projectionMatrix, textureMatrix, modelViewMatrixInverse, gbufferPreviousModelView;
uniform vec3 sunPosition, moonPosition, shadowLightPosition, cameraPosition, upPosition, previousCameraPosition;
uniform float sunAngle, viewWidth, viewHeight, aspectRatio, far, near, blindness, nightVision, wetness;
uniform int worldDay, isEyeInWater, heldBlockLightValue, frameCounter, entityId;
uniform vec4 entityColor;
uniform sampler2D gtexture, lightmap, colortex0, colortex1, colortex2, colortex3, colortex4, depthtex0, depthtex1, noisetex, gaux1, gaux2, tex;
out vec4 mcsm_FragData[1];


/*
 * Wither Storm Skies — sky pass.
 *
 * Paints the Minecraft Story Mode sky: a broad vertical gradient (lavender by
 * day, deep indigo at night, warm violet at dusk) with the aurora ribbons and
 * the storm's purple/magenta wash from the reference frames.
 *
 * This is the correct place for it. gbuffers_skybasic draws the sky dome
 * itself, so the colour lands on real sky geometry that surrounds the whole
 * world — not a quad hung behind the creature.
 */

in vec4 starData;      // rgb = star colour, a > 0.5 marks a star vertex
in vec3 viewPos;

uniform int worldTime;
uniform float frameTimeCounter;
uniform float rainStrength;
uniform vec3 fogColor;
uniform vec3 skyColor;
uniform mat4 gbufferModelViewInverse;

/* user options ------------------------------------------------------------ */
#define SKY_STORY_MODE  1     // [0 1]
#define SKY_SATURATION  1.00  // [0.50 0.75 1.00 1.15 1.30 1.60 2.00]
#define HORIZON_GLOW    1     // [0 1]
#define AURORA          1     // [0 1]
#define AURORA_STRENGTH 1.00  // [0.00 0.25 0.50 0.75 1.00 1.50 2.00]
#define AURORA_SPEED    1.00  // [0.25 0.50 1.00 1.50 2.00]
#define AURORA_HEIGHT   0.30  // [0.10 0.20 0.30 0.45 0.60]
#define AURORA_R        0.35  // [0.00 0.35 0.60 1.00]
#define AURORA_G        1.00  // [0.00 0.35 0.60 1.00]
#define AURORA_B        0.80  // [0.00 0.35 0.60 1.00]
#define AURORA_COVERAGE 0.55  // [0.20 0.35 0.55 0.75 0.95]
#define AURORA_TIP_R    0.62  // [0.00 0.30 0.62 1.00]
#define AURORA_TIP_G    0.28  // [0.00 0.28 0.60 1.00]
#define AURORA_TIP_B    0.95  // [0.00 0.35 0.60 0.95]

/* per-channel sky tuning */
#define SKY_DAY_R   0.514 // [0.000 0.250 0.400 0.525 0.700 0.900 1.000]
#define SKY_DAY_G   0.478 // [0.000 0.250 0.400 0.498 0.700 0.900 1.000]
#define SKY_DAY_B   0.906 // [0.000 0.250 0.400 0.600 0.800 0.945 1.000]
#define SKY_NIGHT_R 0.067 // [0.000 0.067 0.150 0.300 0.500]
#define SKY_NIGHT_G 0.067 // [0.000 0.071 0.150 0.300 0.500]
#define SKY_NIGHT_B 0.278 // [0.000 0.150 0.302 0.500 0.800]
#define SKY_DUSK_R  0.082 // [0.000 0.200 0.392 0.600 0.900]
#define SKY_DUSK_G  0.255 // [0.000 0.118 0.300 0.500 0.800]
#define SKY_DUSK_B  0.341 // [0.000 0.200 0.541 0.750 1.000]

/* biome skies */
#define BIOME_SKIES      0     // [0 1]
#define BIOME_STRENGTH   0.65  // [0.00 0.25 0.45 0.65 0.85 1.00]
#define BIOME_DESERT_R   0.98  // [0.00 0.50 0.75 0.98]
#define BIOME_DESERT_G   0.82  // [0.00 0.50 0.75 0.82]
#define BIOME_DESERT_B   0.58  // [0.00 0.35 0.58 0.90]
#define BIOME_SNOW_R     0.72  // [0.00 0.50 0.72 1.00]
#define BIOME_SNOW_G     0.84  // [0.00 0.50 0.84 1.00]
#define BIOME_SNOW_B     1.00  // [0.00 0.50 0.80 1.00]
#define BIOME_SWAMP_R    0.42  // [0.00 0.25 0.42 0.70]
#define BIOME_SWAMP_G    0.55  // [0.00 0.30 0.55 0.90]
#define BIOME_SWAMP_B    0.40  // [0.00 0.20 0.40 0.70]
#define BIOME_NETHER_R   0.61  // [0.00 0.30 0.61 1.00]
#define BIOME_NETHER_G   0.07  // [0.00 0.07 0.30 0.60]
#define BIOME_NETHER_B   0.48  // [0.00 0.20 0.48 0.90]

/* Story Mode palette, sampled from the reference screenshots ---------------- */
const vec3 DAY_TOP    = vec3(SKY_DAY_R, SKY_DAY_G, SKY_DAY_B);   // #746DCD sampled
const vec3 DAY_MID    = vec3(0.690, 0.604, 0.988);  // #B09AFC sampled
const vec3 DAY_BOT    = vec3(0.816, 0.675, 0.992);  // #D0ACFD sampled
const vec3 NIGHT_TOP  = vec3(SKY_NIGHT_R, SKY_NIGHT_G, SKY_NIGHT_B);
const vec3 NIGHT_MID  = vec3(0.063, 0.055, 0.255);  // #100E41 sampled
const vec3 NIGHT_BOT  = vec3(0.118, 0.102, 0.455);  // #1E1A74 sampled
const vec3 DUSK_TOP   = vec3(SKY_DUSK_R, SKY_DUSK_G, SKY_DUSK_B);
const vec3 DUSK_MID   = vec3(0.933, 0.341, 0.141);  // #EE5724 sampled
const vec3 DUSK_BOT   = vec3(0.388, 0.122, 0.196);  // #631F32 sampled

float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

float wsNoise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1, 0)), f.x),
               mix(hash(i + vec2(0, 1)), hash(i + vec2(1, 1)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    for (int i = 0; i < 4; i++) { v += a * wsNoise(p); p *= 2.03; a *= 0.5; }
    return v;
}

/* smooth day -> dusk -> night -> dawn weights from worldTime */
void timeWeights(out float day, out float dusk, out float night) {
    float t = float(worldTime);
    day   = smoothstep(23000.0, 500.0, t) + smoothstep(1000.0, 2000.0, t) * step(t, 11000.0);
    day   = clamp(day, 0.0, 1.0);
    dusk  = smoothstep(11000.0, 12800.0, t) * (1.0 - smoothstep(13600.0, 14400.0, t));
    dusk += smoothstep(22000.0, 23200.0, t) * (1.0 - smoothstep(23600.0, 24000.0, t));
    night = smoothstep(13200.0, 14200.0, t) * (1.0 - smoothstep(22200.0, 23400.0, t));
    day   = clamp(1.0 - dusk - night, 0.0, 1.0);
}

vec3 storyModeSky(vec3 dir) {
    float h = clamp(dir.y * 0.5 + 0.5, 0.0, 1.0);
    float day, dusk, night;
    timeWeights(day, dusk, night);

    vec3 dayC   = h > 0.5 ? mix(DAY_MID, DAY_TOP, (h - 0.5) * 2.0)
                          : mix(DAY_BOT, DAY_MID, h * 2.0);
    vec3 nightC = h > 0.5 ? mix(NIGHT_MID, NIGHT_TOP, (h - 0.5) * 2.0)
                          : mix(NIGHT_BOT, NIGHT_MID, h * 2.0);
    vec3 duskC  = h > 0.5 ? mix(DUSK_MID, DUSK_TOP, (h - 0.5) * 2.0)
                          : mix(DUSK_BOT, DUSK_MID, h * 2.0);

    vec3 c = dayC * day + nightC * night + duskC * dusk;

#if HORIZON_GLOW
    // soft bloom right at the horizon line, strongest at dusk
    float band = exp(-abs(dir.y) * 7.0);
    c += duskC * band * 0.34 * (dusk + 0.25);
#endif

    float l = dot(c, vec3(0.2126, 0.7152, 0.0722));
    return mix(vec3(l), c, SKY_SATURATION);
}

/* Aurora borealis ribbons, as requested from the reference frames. */
vec3 aurora(vec3 dir, float night) {
#if AURORA == 0
    return vec3(0.0);
#else
    if (dir.y < 0.02) return vec3(0.0);
    float t = frameTimeCounter * 0.06 * AURORA_SPEED;
    // project onto a plane well above the player so ribbons stretch to the horizon
    vec2 p = dir.xz / max(dir.y, 0.05);
    float acc = 0.0;
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        vec2 q = p * (0.45 + fi * 0.22) + vec2(t * (1.0 + fi * 0.35), -t * 0.5);
        float ribbon = fbm(q + vec2(fbm(q * 0.5 + t), 0.0) * 1.6);
        // thin the field into curtains
        ribbon = pow(smoothstep(0.42, 0.86, ribbon), 2.4);
        acc += ribbon * (1.0 - fi * 0.22);
    }
    acc *= smoothstep(0.02, AURORA_HEIGHT + 0.25, dir.y);
    acc *= night;                       // only after dark
    acc *= (1.0 - rainStrength * 0.8);
    vec3 col = vec3(AURORA_R, AURORA_G, AURORA_B);
    // green core fading to violet at the tips, like the real thing
    col = mix(col, vec3(AURORA_TIP_R, AURORA_TIP_G, AURORA_TIP_B), clamp(acc * 0.55, 0.0, 1.0));
    return col * acc * AURORA_COVERAGE * AURORA_STRENGTH;
#endif
}

/* Biome sky.
 *
 * Iris hands us fogColor, which Minecraft already varies per biome. Using its
 * hue as the classifier means every biome gets its own sky without needing a
 * biome ID uniform, which gbuffers_skybasic does not receive. */
vec3 biomeTint(vec3 base) {
#if BIOME_SKIES == 0
    return base;
#else
    float r = fogColor.r, g = fogColor.g, b = fogColor.b;
    float mx = max(max(r, g), b), mn = min(min(r, g), b);
    float sat = mx - mn;

    vec3 tint = base;
    // warm + bright  -> desert / badlands
    float desert = smoothstep(0.05, 0.22, r - b) * step(0.45, mx);
    // pale + blue    -> snowy
    float snow   = smoothstep(0.03, 0.18, b - r) * step(0.55, mx);
    // green dominant -> swamp / jungle
    float swamp  = smoothstep(0.02, 0.14, g - max(r, b));
    // dark + red     -> nether
    float nether = smoothstep(0.06, 0.25, r - g) * (1.0 - step(0.45, mx));

    tint = mix(tint, vec3(BIOME_DESERT_R, BIOME_DESERT_G, BIOME_DESERT_B), desert * BIOME_STRENGTH);
    tint = mix(tint, vec3(BIOME_SNOW_R,   BIOME_SNOW_G,   BIOME_SNOW_B),   snow   * BIOME_STRENGTH);
    tint = mix(tint, vec3(BIOME_SWAMP_R,  BIOME_SWAMP_G,  BIOME_SWAMP_B),  swamp  * BIOME_STRENGTH);
    tint = mix(tint, vec3(BIOME_NETHER_R, BIOME_NETHER_G, BIOME_NETHER_B), nether * BIOME_STRENGTH);
    return tint;
#endif
}

void main() {
    vec3 dir = normalize(mat3(gbufferModelViewInverse) * viewPos);
    vec4 outc;

    if (starData.a > 0.5) {
        // star vertex: keep vanilla stars, just cool them slightly
        outc = vec4(starData.rgb * vec3(0.86, 0.90, 1.0), 1.0);
    } else {
        // MCSM v3: aurora is independent of the story-mode sky override, so
        // turning the override off (new default) still leaves the aurora on.
        float day, dusk, night;
        timeWeights(day, dusk, night);
#if SKY_STORY_MODE
        vec3 c = storyModeSky(dir);
        c = biomeTint(c);
#else
        vec3 c = skyColor;
#endif
        c += aurora(dir, night);
        outc = vec4(c, 1.0);
    }
    mcsm_FragData[0] = outc;
}
