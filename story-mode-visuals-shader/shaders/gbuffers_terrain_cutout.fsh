#version 120
/* DRAWBUFFERS:012 */

#include "/lib.glsl"
#include "/worldpos.glsl"

varying vec4 texcoord;
varying vec4 lmcoord;
varying vec4 glcolor;
varying vec3 normal;
varying vec3 worldPos;

uniform sampler2D texture;
uniform vec3  cameraPosition;
uniform vec3  sunPosition;
uniform vec3  moonPosition;
uniform float sunAngle;
uniform float TERRAIN_AO_STR; //settings terrainAO
uniform float TORCH_SAT; //settings torchSat
uniform float FOG_STR; //settings fog

void main() {

    vec3 nrm = normalize(normal);
    vec3 pos = worldPos + cameraPosition;

    // ---------- flat Story Mode lightmap bands (posterized, no smooth AO)
    vec2 luv = lmcoord.xy;
    float skyLight  = luv.x * 1.06;
    float torch = 1.0 - luv.y;
    float ao = clamp(torch * torch, 0.0, 1.0);
    float band = ceil(skyLight * 4.0) * 0.25;

    vec3 light = vec3(band * band);
    vec3 torchColor = vec3(1.0, 0.82, 0.62);

    // ---------- blocky directional shading + real-time shadows
    vec3 lightDir = (sunAngle < 0.45) ? normalize(moonPosition) : normalize(sunPosition);
    float ndl = max(dot(nrm, lightDir), 0.0);
#ifdef CEL_BANDS
    float fac = ceil(ndl * 3.0) / 3.0;                   // 3 hard steps (cel look)
#else
    float fac = ndl;
#endif
#ifdef FLAT_LIGHTING
    fac = 1.0;                                           // pure lightmap look
#endif
    float sh = getShadow(pos, nrm);
    light *= mix(vec3(0.28, 0.33, 0.52), vec3(1.08, 0.97, 0.84), fac) * sh;

    // cloud footprint shadows sweep across the terrain
    float cloudShadow = getCloudShadow(pos);
    float cloudMix = smoothstep(0.15, 0.75, abs(sunAngle - 0.5));
    light *= 1.0 - cloudShadow * 0.45 * mix(1.0, 0.25, cloudMix);

    // ---------- torch tinting (emissive saturated lighting)
#ifdef TORCH_TINT
    vec3 torchLight = torchColor * torch;
    float tl = dot(torchLight, vec3(0.299, 0.587, 0.114));
    torchLight = mix(vec3(tl, tl, tl), torchLight, TORCH_SAT);   // saturation slider
    light += torchLight * (0.9 + 0.5 * ao);
#endif

    // ---------- contact AO lines where blocks meet the ground
#ifdef TERRAIN_AO
    float contact = getContactAO(worldPos, nrm);
    light *= mix(mix(contact, 1.0, smoothstep(0.0, 0.6, abs(nrm.y))), 1.0, 1.0 - TERRAIN_AO_STR);
#endif

    // ---------- albedo
    vec4 albedo = texture2D(texture, texcoord.xy) * glcolor;
    vec3 color = albedo.rgb * light;

    // ---------- biome-specific gradient fog (Story Mode per-area mist)
    vec4 fog = sampledFog(pos);
    float dist = length(pos - cameraPosition);
    float fogMul = (sunAngle < 0.45) ? 0.6 : 1.0;
    float fogF = 1.0 - exp(-fog.a * fogMul * FOG_STR * dist * 0.0022);
    color = mix(color, fog.rgb, clamp(fogF, 0.0, 0.94));

    if (albedo.a < 0.12) discard;

    float depth = gl_FragCoord.z;
    gl_FragData[0] = vec4(color, albedo.a);
    gl_FragData[1] = vec4(depth, depth * depth, depth, 1.0);
    gl_FragData[2] = vec4(nrm * 0.5 + 0.5, 1.0);
}
