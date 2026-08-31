#version 120

#include "/lib.glsl"
#include "/worldpos.glsl"

#define CEL_BANDS
#define CLOUD_SHADOWS
#define TERRAIN_AO
#define TORCH_TINT

varying vec4 texcoord;
varying vec4 lmcoord;
varying vec4 glcolor;
varying vec3 normal;
varying vec3 worldPos;

uniform sampler2D texture;
uniform mat4  gbufferModelView;
uniform mat4  gbufferModelViewInverse;
uniform mat4  gbufferProjection;
uniform mat4  gbufferProjectionInverse;
uniform vec3  cameraPosition;
uniform vec3  sunPosition;
uniform vec3  moonPosition;
uniform vec3  upPosition;
uniform float viewWidth;
uniform float viewHeight;
uniform float frameTimeCounter;
uniform float sunAngle;

uniform int   biome;
uniform float rainStrength;
uniform float wetness;
uniform float nightVision;
uniform float blindness;
uniform float darknessFactor;

uniform int   worldTime;

void main() {
    vec3 nrm = normalize(normal);
    vec3 pos = worldPos + cameraPosition;

    // ---------- flat Story Mode lightmap bands (posterized, no smooth AO)
    vec2 luv = lmcoord.xy;
    float skyLight  = luv.x * 1.06;
    float torch = 1.0 - luv.y;
    float ao = clamp(torch * torch, 0.0, 1.0);
    float band = ceil(skyLight * 4.0) * 0.25;            // quantized bands

    vec3 light = vec3(band * band);
    vec3 torchColor = vec3(1.0, 0.82, 0.62);

    // ---------- blocky directional shading + real-time shadows
    vec3 sunDir = normalize(sunPosition);
    vec3 moonDir = normalize(moonPosition);
    bool isNight = sunAngle < 0.45;
    vec3 lightDir = isNight ? moonDir : sunDir;

    float ndl = max(dot(nrm, lightDir), 0.0);
#ifdef CEL_BANDS
    float fac = ceil(ndl * 3.0) / 3.0;                   // 3 hard steps (cel look)
#else
    float fac = ndl;
#endif
    float sh = getShadow(pos, nrm);

    // cloud footprint shadows sweep across the terrain (hard-edged at dusk/dawn)
#ifdef CLOUD_SHADOWS
    float cloudShadow = getCloudShadow(pos);
    float cloudMix = smoothstep(0.15, 0.75, abs(sunAngle - 0.5));
    float cloudStrength = 1.0 - cloudShadow * 0.45 * mix(1.0, 0.25, cloudMix);
#else
    float cloudStrength = 1.0;
#endif

    light *= mix(vec3(0.28, 0.33, 0.52), vec3(1.08, 0.97, 0.84), fac) * sh * cloudStrength;

    // ---------- torch tinting (emissive saturated lighting)
#ifdef TORCH_TINT
    vec3 warm = torchColor * torch;
    vec3 soul = vec3(0.35, 0.95, 1.0) * torch;
    float soulWeight = clamp(biome == 8 ? 0.4 : 0.0, 0.0, 1.0);
    vec3 torchLight = mix(warm, soul, soulWeight);
    light += torchLight * (0.9 + 0.5 * ao);
#endif

    // ---------- contact AO lines where blocks meet the ground
#ifdef TERRAIN_AO
    float contact = getContactAO(pos, nrm);
    light *= mix(contact, 1.0, smoothstep(0.0, 0.6, abs(nrm.y)));
#endif

    // ---------- albedo
    vec4 albedo = texture2D(texture, texcoord.xy) * glcolor;
    vec3 color = albedo.rgb * light;

    // ---------- biome-specific gradient fog (Story Mode per-area mist)
    vec4 fog = sampledFog(pos);
    float dist = length(pos - cameraPosition);
    float fogMul = isNight ? 0.6 : 1.0;
    float fogF = 1.0 - exp(-fog.a * fogMul * dist * 0.0022);
    color = mix(color, fog.rgb, clamp(fogF, 0.0, 0.94));

    // ---------- hand-held item glow-up + story vignette keep this pass simple
    if (albedo.a < 0.12) discard;

    gl_FragData[0] = vec4(color, albedo.a);
    gl_FragData[1] = vec4(nrm * 0.5 + 0.5, 1.0);         // encoded normal buffer
}
