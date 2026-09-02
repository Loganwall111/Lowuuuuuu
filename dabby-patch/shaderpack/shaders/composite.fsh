#version 330 compatibility

/*
 * Wither Storm Skies — colour grade, bloom and storm wash.
 *
 * Runs after the world is drawn. Three jobs:
 *   1. bloom, so the turquoise teeth and purple beams bleed light the way they
 *      do in the reference frames
 *   2. the storm's purple/magenta wash and vignette
 *   3. tonemap + contrast + vibrance for the flat, saturated Story Mode look
 */

in vec2 texcoord;

uniform sampler2D colortex0;
uniform sampler2D depthtex0;
uniform float viewWidth;
uniform float viewHeight;
uniform float frameTimeCounter;
uniform int worldTime;

#define BLOOM           1     // [0 1]
#define BLOOM_STRENGTH  0.60  // [0.00 0.20 0.40 0.60 0.90 1.30]
#define TONEMAP         1     // [0 1]
#define EXPOSURE        1.05  // [0.60 0.80 1.00 1.05 1.20 1.50]
#define CONTRAST        1.10  // [0.80 1.00 1.10 1.25 1.40]
#define VIBRANCE        1.20  // [0.50 0.80 1.00 1.20 1.50 1.90]
#define STORM_PURPLE_ON 0     // [0 1]
#define STORM_PURPLE    0.50  // [0.00 0.25 0.50 0.75 1.00]
#define STORM_VIGNETTE_ON 1   // [0 1]
#define STORM_VIGNETTE  0.35  // [0.00 0.20 0.35 0.55 0.80]

const vec3 STORM_TINT = vec3(0.42, 0.20, 0.62);

vec3 bloomPass(vec2 uv) {
    vec2 px = 1.0 / vec2(viewWidth, viewHeight);
    vec3 sum = vec3(0.0);
    float total = 0.0;
    // 13-tap cross-shaped blur on bright pixels only
    for (int i = -3; i <= 3; i++) {
        for (int j = -3; j <= 3; j++) {
            if (abs(i) + abs(j) > 4) continue;
            vec2 o = vec2(float(i), float(j)) * px * 2.4;
            vec3 c = texture(colortex0, uv + o).rgb;
            float lum = dot(c, vec3(0.2126, 0.7152, 0.0722));
            float w = smoothstep(0.62, 1.0, lum);
            sum += c * w;
            total += w;
        }
    }
    return total > 0.0 ? sum / total : vec3(0.0);
}

vec3 tonemapACES(vec3 x) {
    const float a = 2.51, b = 0.03, c = 2.43, d = 0.59, e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

void main() {
    vec3 col = texture(colortex0, texcoord).rgb;

#if BLOOM
    vec3 b = bloomPass(texcoord);
    col += b * BLOOM_STRENGTH;
#endif

#if STORM_PURPLE_ON
    col = mix(col, col * STORM_TINT * 2.0, STORM_PURPLE * 0.5);
#endif

    col *= EXPOSURE;

#if TONEMAP
    col = tonemapACES(col);
#endif

    // contrast about mid grey
    col = (col - 0.5) * CONTRAST + 0.5;

    // vibrance: push muted colours harder than already-saturated ones
    float lum = dot(col, vec3(0.2126, 0.7152, 0.0722));
    float sat = max(max(col.r, col.g), col.b) - min(min(col.r, col.g), col.b);
    col = mix(vec3(lum), col, 1.0 + (VIBRANCE - 1.0) * (1.0 - sat));

#if STORM_VIGNETTE_ON
    vec2 v = texcoord - 0.5;
    float vig = 1.0 - dot(v, v) * STORM_VIGNETTE * 1.8;
    col *= clamp(vig, 0.0, 1.0);
#endif

    gl_FragData[0] = vec4(clamp(col, 0.0, 1.0), 1.0);
}
