/*
============================================================================
 STORY MODE VISUALS - shadow / cloud / contact-AO library (gbuffer-safe)
 ----------------------------------------------------------------------------
 All uniforms used here are DECLARED HERE so every program that includes
 this file is self-contained - no reliance on loader auto-injection.
============================================================================
*/

// 1.0 / ortho distance of the nearest shadow far plane (default if the
// loader does not provide its own macro).
#ifndef MC_SHADOW_ORTHO
#define MC_SHADOW_ORTHO 256.0
#endif

uniform float frameTimeCounter;
uniform float CLOUD_SPEED; //settings speed

uniform mat4  shadowProjection;
uniform mat4  shadowModelView;
uniform sampler2D shadowtex0;

// ------------------------------------------------ Story Mode cloud shadows
// A 3D noise shadowmap swept horizontally by a time-based wind: large soft
// cells that drift across the landscape. Returns 0..1 coverage.
float getCloudShadow(vec3 worldPos) {
#ifdef CLOUD_SHADOWS
    float anim = frameTimeCounter * 4.5 * CLOUD_SPEED;
    vec3 p = worldPos * 0.020 + vec3(anim * 0.14, 0.0, anim * 0.07);
    float n = vnoise(p) * 0.6 + vnoise(p * 2.4 + 31.0) * 0.4;
    n = smoothstep(0.06, 0.55, n);
    n *= smoothstep(0.0, 140.0, distance(worldPos.xz, cameraPosition.xz));
    return n;
#else
    return 0.0;
#endif
}

// ------------------------------------------- hard directional shadow lookup
// Story Mode keeps shadows hard, flat and blocky - PCF off by default.
float getShadow(vec3 worldPos, vec3 nrm) {
#ifdef SHADOW
    if (worldPos.y < -63.0) return 1.0;
    worldPos += nrm * 0.075;
    vec4 shadowSpace = shadowProjection * shadowModelView * vec4(worldPos, 1.0);
    if (abs(shadowSpace.w) < 1e-5) return 1.0;
    vec3 ndc = shadowSpace.xyz / shadowSpace.w;
    if (clamp(ndc.xy, vec2(-1.0, -1.0), vec2(1.0, 1.0)) != ndc.xy) return 1.0;
    if (ndc.z > 1.0 || ndc.z < -1.0) return 1.0;

    float mc = 0.5 / MC_SHADOW_ORTHO;
    vec3 m = vec3(mc, mc, 0.015);
#ifdef SHADOW_SOFT
    float sum = 0.0;
    for (int i = 0; i < 9; i++) {
        vec2 o = (vec2(float(i % 3), float(i / 3)) - 1.0) * m.xy;
        float sm = texture2D(shadowtex0, ndc.xy * 0.5 + 0.5 + o).r;
        sum += step(ndc.z - m.z, sm + m.x);
    }
    return sum / 9.0;
#else
    float n = 0.0;
    for (int i = 0; i < 4; i++) {
        vec2 o = hash22(floor(worldPos.xy * 128.0) + float(i) * 7.0) - 0.5;
        float sm = texture2D(shadowtex0, ndc.xy * 0.5 + 0.5 + o * mc).r;
        n += step(ndc.z - m.z, sm + m.x);
    }
    return n * 0.25;
#endif
#else
    return 1.0;
#endif
}

// ------------------------------------ contact AO (geometry-based, gbuffer-safe)
// Darkens the base of vertical faces and the undersides of blocks without
// any buffer reads, so it works inside every gbuffers pass. worldPos must be
// the raw (camera-relative, model-space) vertex position.
float getContactAO(vec3 worldPos, vec3 nrm) {
    if (nrm.y > 0.85) return 1.0;
    float yFrac = fract(worldPos.y);
    if (nrm.y < -0.5) return 0.82;                          // undersides
    return mix(0.85, 1.0, smoothstep(0.0, 0.22, yFrac));    // wall bases
}
