#version 150

// ============================================================================
// dabywitherstormmod:core/rendertype_clouds.fsh — 100% procedural clouds
// ============================================================================
// Pairs with core/rendertype_clouds.vsh (the CloudFaces decode): consumes
// worldPosCoord / vertexColor / vertexDistance and generates the blocky
// Story Mode cloud pattern MATHEMATICALLY — zero texture image registers
// (no cloudTex0-7, no sampler2D, no PNG lookups). The shader itself is the
// cloud. Safe for the pure-vanilla core pipeline (no `long` uniforms here;
// those are reserved for the Iris shader pack programs): frameTimeCounter
// and sunPosition are injected by OptiFine/Iris and default to zero in a
// bare vanilla context, where the lavender day palette still renders.

precision highp float;
precision highp int;

in vec4 vertexColor;
in float vertexDistance;
in vec3 worldPosCoord;

out vec4 fragColor;

uniform float frameTimeCounter; // OptiFine/Iris inject this (0 in bare vanilla)
uniform vec3 sunPosition;       // OptiFine/Iris inject this (0 in bare vanilla)

/* ------------------------- noise toolkit ------------------------- */
float hash13(vec3 p) {
    p = fract(p * 0.3183099 + vec3(0.1, 0.2, 0.3));
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float noise(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    float n = i.x + i.y * 57.0 + 113.0 * i.z;
    return mix(
        mix(mix(hash13(vec3(n + 0.0)), hash13(vec3(n + 1.0)), f.x),
            mix(hash13(vec3(n + 57.0)), hash13(vec3(n + 58.0)), f.x), f.y),
        mix(mix(hash13(vec3(n + 113.0)), hash13(vec3(n + 114.0)), f.x),
            mix(hash13(vec3(n + 170.0)), hash13(vec3(n + 171.0)), f.x), f.y),
        f.z);
}

float fbm(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p = p * 2.02 + vec3(11.3, 7.1, 5.7);
        a *= 0.5;
    }
    return v;
}

void main() {
    vec3 p = worldPosCoord;

    // Slow wind drift (static in bare vanilla, animated under OptiFine/Iris).
    p.xz += vec2(frameTimeCounter * 0.0032, frameTimeCounter * 0.0011);

    float d = fbm(vec3(p.x * 0.014, p.y * 0.10, p.z * 0.014) + vec3(0.0, frameTimeCounter * 0.0008, 0.0));
    float d2 = fbm(vec3(p.x * 0.05, p.y * 0.22, p.z * 0.05) + vec3(7.0, frameTimeCounter * 0.0016, 13.0));
    d = d * 0.72 + d2 * 0.28;

    float density = smoothstep(0.34, 0.72, d);
    // Alpha: pattern density x the vertex-side fade/opacity carried by
    // vertexColor.a (CloudColor.a x fade factor from the vertex stage).
    float alpha = density * vertexColor.a;
    if (alpha < 0.02) {
        discard;
    }

    // Time-of-day palette. With no sunPosition (bare vanilla) this resolves to
    // the bright lavender Story Mode day sky.
    float sunY = length(sunPosition) > 0.01 ? normalize(sunPosition).y : 0.55;
    float dayAmt    = clamp(sunY * 3.0, 0.0, 1.0);
    float nightAmt  = clamp(-sunY * 3.0, 0.0, 1.0);
    float sunsetAmt = clamp(1.0 - abs(sunY) * 8.0, 0.0, 1.0);

    vec3 dayTop    = vec3(1.02, 0.99, 1.06);
    vec3 dayBot    = vec3(0.72, 0.68, 0.88);
    vec3 sunTop    = vec3(1.00, 0.72, 0.52);
    vec3 sunBot    = vec3(0.58, 0.30, 0.42);
    vec3 nightTop  = vec3(0.46, 0.50, 0.72);
    vec3 nightBot  = vec3(0.16, 0.16, 0.30);

    vec3 topCol = mix(nightTop, dayTop, dayAmt);
    topCol = mix(topCol, sunTop, sunsetAmt * (1.0 - nightAmt));
    vec3 botCol = mix(nightBot, dayBot, dayAmt);
    botCol = mix(botCol, sunBot, sunsetAmt * (1.0 - nightAmt));

    // Vertical gradient across the extruded slab (CloudHeight = 2.5 in the
    // vsh), then the per-face brightness carried by vertexColor.rgb.
    float h = clamp(worldPosCoord.y / 2.5, 0.0, 1.0);
    vec3 col = mix(botCol, topCol, h) * vertexColor.rgb;

    // Body core darkening + shredded magenta rim (WitherStormShaderSource look).
    float edge = smoothstep(0.34, 0.52, d) * (1.0 - smoothstep(0.60, 0.72, d));
    col = mix(col, vec3(0.02, 0.01, 0.03), 0.35 * density);
    col = mix(col, vec3(0.85, 0.12, 0.90), 0.55 * edge * (0.6 + 0.4 * h));

    // Distance haze using the vertexDistance channel handed over from the vsh.
    float fogF = clamp((vertexDistance - 120.0) / 320.0, 0.0, 1.0);
    col = mix(col, vec3(0.68, 0.60, 0.88), fogF * 0.5);

    fragColor = vec4(col, alpha);
}
