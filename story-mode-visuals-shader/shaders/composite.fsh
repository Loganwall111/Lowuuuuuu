#version 120

#include "/lib.glsl"
#include "/worldpos.glsl"

varying vec2 texcoord;

uniform sampler2D colortex0;
uniform sampler2D depthtex1;
uniform sampler2D depthtex0;

uniform mat4 gbufferProjection;
uniform mat4 gbufferProjectionInverse;
uniform mat4 gbufferModelViewInverse;

uniform vec3  cameraPosition;
uniform vec3  sunPosition;
uniform vec3  moonPosition;
uniform vec3  upPosition;
uniform vec3  skyColor;
uniform vec3  fogColor;
uniform float frameTimeCounter;
uniform float viewWidth;
uniform float viewHeight;
uniform float near;
uniform float far;
uniform float sunAngle;
uniform float rainStrength;
uniform int   worldTime;
uniform float wetness;

uniform float FOG_STR; //settings fog
uniform float MOONSHINE; //settings moonlight

void main() {
    vec2 uv = texcoord;
    vec3 scene = texture2D(colortex0, uv).rgb;
    vec3 pos = getWorldPos(uv);

    // ==================== SSAO: dark contact lines where geometry meets ====
#ifdef SSAO
    float d1 = texture2D(depthtex1, uv).r;
    if (pos.y < 1000000.0 && d1 < 0.9999) {
        vec3 nrm = texture2D(colortex1, uv).rgb * 2.0 - 1.0;
        float depth = texture2D(depthtex1, uv).r;
        vec3 viewN = normalize(mat3(gbufferModelViewInverse) * nrm);

        float occ = 0.0;
        float radius = 2.6 / max(depth * 900.0, 0.12);
        vec2 rnd = hash22(uv * vec2(viewWidth, viewHeight) + frameTimeCounter) * 6.283;
        mat2 rot = mat2(cos(rnd.x), -sin(rnd.x), sin(rnd.x), cos(rnd.x));

        for (int i = 0; i < 12; i++) {
            float a = (float(i) / 12.0) * 6.2831;
            float rr = radius * (0.4 + 0.6 * hash12(vec2(float(i), 3.0)));
            vec2 dir = rot * vec2(cos(a), sin(a)) * rr;
            float sd = texture2D(depthtex1, uv + dir).r;
            float diff = depth - sd;
            float dist = length(dir * vec2(viewWidth / 2.0, viewHeight / 2.0)) / 160.0;
            occ += clamp(diff * 2400.0 - dist * 0.06, 0.0, 1.0) * (0.35 + 0.65 * max(dot(viewN, vec3(0.0, 1.0, 0.0)), 0.0));
        }
        float ao = 1.0 - clamp(occ / 12.0, 0.0, 1.0) * 0.65;
        scene *= ao;
    }
#endif

    // ==================== BIOME FOG (Story Mode per-area mist) ==============
    float dist = length(pos - cameraPosition);
    vec4 fog = sampledFog(pos);
    float nightMul = (sunAngle < 0.45) ? 0.6 : 1.0;
    float fogF = 1.0 - exp(-fog.a * nightMul * FOG_STR * dist * 0.0022);
    scene = mix(scene, fog.rgb, clamp(fogF, 0.0, 0.94));

    // ==================== DESERT HEAT SHIMMER ================================
#ifdef HEAT_SHIMMER
    float desertW = biomeMatch(pos, 4.0) + biomeMatch(pos, 5.0) * 0.7;
    float glare = desertW * (1.0 - rainStrength) * sstep(0.0, 0.5, sunAngle);
    if (glare > 0.01) {
        vec2 shimmer = vec2(vnoise(vec3(pos.x * 0.35, pos.y * 0.35, frameTimeCounter * 0.9)),
                            vnoise(vec3(pos.z * 0.35, pos.y * 0.35, frameTimeCounter * 0.9))) - 0.5;
        shimmer *= 0.004 * glare * smoothstep(8.0, 90.0, dist);
        vec3 shifted = texture2D(colortex0, uv + shimmer).rgb;
        scene = mix(scene, shifted, 0.6 * glare);
        scene += vec3(1.0, 0.82, 0.55) * glare * 0.05;   // golden heat bloom
    }
#endif

    // ==================== AURORA OVERLAY (cold biomes) =======================
#ifdef AURORA
    float auroraW = biomeMatch(pos, 8.0) + biomeMatch(pos, 9.0) * 0.8;
    if (auroraW > 0.01 && sunAngle < 0.45 && rainStrength < 0.8) {
        vec3 dir = normalize(pos - cameraPosition);
        float aDir = dot(normalize(dir.xz + 0.0001), vec2(0.3, 0.95));
        float ribbon = smoothstep(0.2, 0.9, fbm3(vec3(aDir * 3.5, dir.y * 4.0 - frameTimeCounter * 0.06)));
        float band = smoothstep(0.0, 0.55, dir.y) * smoothstep(0.85, 0.55, dir.y);
        float aur = ribbon * band * auroraW * MOONSHINE * 0.6;
        scene += vec3(0.15, 0.85, 0.55) * aur;
        scene += vec3(0.65, 0.25, 0.75) * aur * 0.5;
    }
#endif

    // ==================== GOD RAYS ==========================================
#ifdef GODRAYS
    if (rainStrength < 0.9) {
        vec3 celest = (sunAngle < 0.45) ? normalize(moonPosition) : normalize(sunPosition);
        vec4 clip = gbufferProjection * gbufferModelViewInverse * vec4(celest * 200.0, 1.0);
        vec2 sunUv = clip.xy / clip.w * 0.5 + 0.5;
        if (clip.w > 0.0 && clamp(sunUv, vec2(0.0), vec2(1.0)) == sunUv) {
            vec2 dirToSun = (sunUv - uv) * 0.028;
            vec3 rays = vec3(0.0);
            vec2 su = uv;
            for (int i = 0; i < 14; i++) {
                su += dirToSun;
                float d = texture2D(depthtex1, clamp(su, 0.002, 0.998)).r;
                float luma = dot(texture2D(colortex0, clamp(su, 0.002, 0.998)).rgb, vec3(0.299, 0.587, 0.114));
                float visible = step(d, 0.99999);           // rays blocked by geometry
                rays += vec3(luma) * pow(max(1.0 - float(i) / 14.0, 0.0), 2.0) * visible;
            }
            vec3 rayCol = (sunAngle < 0.45) ? vec3(0.55, 0.7, 1.0) : vec3(1.0, 0.82, 0.6);
            scene += rayCol * rays * 0.10 * (1.0 - rainStrength);
        }
    }
#endif

    // ==================== BLOOM =============================================
#ifdef BLOOM
    vec3 bloom = vec3(0.0);
    float bweights = 0.0;
    for (int x = -2; x <= 2; x++) {
        for (int y = -2; y <= 2; y++) {
            vec2 o = (vec2(float(x), float(y)) * 2.0 + 1.0) / vec2(viewWidth, viewHeight);
            vec3 s = texture2D(colortex0, uv + o).rgb;
            float w = 1.0 / (1.0 + dot(vec2(x, y), vec2(x, y)));
            float l = dot(s, vec3(0.299, 0.587, 0.114));
            bloom += s * max(l - 0.75, 0.0) * w;
            bweights += w;
        }
    }
    bloom /= bweights;
    scene += bloom * 0.6;
#endif

    gl_FragData[0] = vec4(scene, 1.0);
}
