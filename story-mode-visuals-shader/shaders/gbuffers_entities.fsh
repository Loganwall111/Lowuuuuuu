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
uniform float ENT_AO; //settings entAO

void main() {

    vec3 nrm = normalize(normal);
    vec3 pos = worldPos + cameraPosition;

    vec2 luv = lmcoord.xy;
    float torch = 1.0 - luv.y;
    float band = ceil(luv.x * 3.0) / 3.0;                // hard bands
    vec3 light = vec3(band * band);

    vec3 lightDir = (sunAngle < 0.45) ? normalize(moonPosition) : normalize(sunPosition);
    float ndl = max(dot(nrm, lightDir), 0.0);
    light *= mix(vec3(0.30, 0.35, 0.55), vec3(1.08, 0.97, 0.84), ceil(ndl * 3.0) / 3.0);

    float sh = getShadow(pos, nrm);
#ifdef ENTITY_SOFT_SHADOW
    light *= mix(sh, 1.0, 0.15);
#else
    light *= sh;
#endif
    light *= 1.0 - getCloudShadow(pos) * 0.25;
    light *= mix(getContactAO(worldPos, nrm), 1.0, 1.0 - ENT_AO);
    light += vec3(1.0, 0.82, 0.62) * torch;               // torch glow

    vec4 albedo = texture2D(texture, texcoord.xy) * glcolor;
    vec3 color = albedo.rgb * light;

    vec4 fog = sampledFog(pos);
    float dist = length(pos - cameraPosition);
    float fogF = 1.0 - exp(-fog.a * dist * 0.0022);
    color = mix(color, fog.rgb, clamp(fogF, 0.0, 0.94));

    if (albedo.a < 0.12) discard;

    float depth = gl_FragCoord.z;
    gl_FragData[0] = vec4(color, albedo.a);
    gl_FragData[1] = vec4(depth, depth * depth, depth, 1.0);
    gl_FragData[2] = vec4(nrm * 0.5 + 0.5, 1.0);
}
