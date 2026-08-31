uniform float CLOUD_SPEED; //settings speed
/*
============================================================================
 STORY MODE VISUALS — world position + shadow mapping library
 ----------------------------------------------------------------------------
 getWorldPos      : reconstructs world-space position from the depth buffer
 getCloudShadow   : Story Mode cloud footprint shadows sweeping the terrain
                     (3D-value-noise shadowmap so they move with the wind and
                     project straight down, independent of the sun)
 getShadow        : hard-edged directional shadow map lookup (blocky
                     Story Mode shadows, no filtering), with fade at the map
                     border and sun/moon direction tracked per frame
 getContactAO     : dark contact line where geometry meets geometry
============================================================================
*/

// 1.0 / MC_ORTHO?  ortho distance of the nearest far plane used by Iris
#ifndef MC_SHADOW_ORTHO
#define MC_SHADOW_ORTHO 256.0
#endif

vec3 getWorldPos(vec2 uv) {
    float depth = texture2D(depthtex0, uv).r;
    if (depth > 0.99999) return vec3(1000000.0);
    vec3 clip = vec3(uv * 2.0 - 1.0, depth * 2.0 - 1.0);
    vec4 world = gbufferProjectionInverse * vec4(clip, 1.0);
    world /= world.w;
    return world.xyz + cameraPosition;
}

// ------------------------------------------------ Story Mode cloud shadows
// A 3D noise shadowmap swept horizontally by a time-based wind: large soft
// cells that drift across the landscape. Returns [0..1] coverage.
float getCloudShadow(vec3 worldPos) {
    float anim = frameTimeCounter * 4.5 * CLOUD_SPEED;
    vec3 p = worldPos * vec3(0.020, 0.020, 0.020) + vec3(anim * 0.14, 0.0, anim * 0.07);
    float n = vnoise(p) * 0.6 + vnoise(p * 2.4 + 31.0) * 0.4;
    n = smoothstep(0.06, 0.55, n);                       // soft footprint edges
    n *= smoothstep(0.0, 140.0, distance(worldPos.xz, cameraPosition.xz));
    return n;
}

// ------------------------------------------- hard directional shadow lookup
// Vanilla 1.20.1 shadows are a plain boolean depth map: sharp edges only.
// Story Mode deliberately keeps them hard and flat - no PCF filtering.
float getShadow(vec3 worldPos, vec3 nrm) {
    if (worldPos.y < -63.0) return 1.0;                  // ignore below void
    worldPos += nrm * 0.075;                             // one-bias acne fix
    vec4 shadowSpace = shadowProjection * shadowModelView * vec4(worldPos, 1.0);
    if (abs(shadowSpace.w) < 1e-5) return 1.0;
    vec3 ndc = shadowSpace.xyz / shadowSpace.w;
    if (clamp(ndc.xy, vec2(-1.0), vec2(1.0)) != ndc.xy) return 1.0;   // outside map
    if (ndc.z > 1.0 || ndc.z < -1.0) return 1.0;

    float mc = 0.5 / MC_SHADOW_ORTHO;
    vec3 m = vec3(mc, mc, 0.015);                        // blocky texel + z tolerance
    float n = 0.0;
    for (int i = 0; i < 4; i++) {
        vec2 o = hash22(floor(worldPos.xy * 128.0) + float(i) * 7.0) - 0.5;
        float sm = texture2D(shadowtex0, ndc.xy * 0.5 + 0.5 + o * mc).r;
        n += step(ndc.z - m.z, sm + m.x);
    }
    return n * 0.25;
}

// ------------------------------------ contact AO (cheap SSAO-alike contact)
float getContactAO(vec3 worldPos, vec3 nrm) {
    if (nrm.y > 0.85) return 1.0;                        // only ground contact
    vec2 cell = floor(worldPos.xz);
    vec2 rnd = hash22(cell) * 6.283;
    vec2 ofs = vec2(cos(rnd.x), sin(rnd.x)) * 0.5 + vec2(cos(rnd.y), sin(rnd.y)) * 0.25;
    vec2 uv = worldPos.xz + nrm.xz * 0.5 + ofs * 0.35;
    float depth = texture2D(depthtex0, uv / vec2(viewWidth, viewHeight)).r;
    if (depth > 0.99999) return 1.0;
    vec3 hit = getWorldPos(uv / vec2(viewWidth, viewHeight));
    float dist = length(hit - worldPos);
    return clamp(dist / 2.6, 0.35, 1.0);
}
