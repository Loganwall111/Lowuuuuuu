#version 120

#include "/lib.glsl"
#include "/worldpos.glsl"

varying vec4 texcoord;
varying vec4 lmcoord;
varying vec4 glcolor;
varying vec3 normal;
varying vec3 worldPos;

uniform sampler2D texture;
uniform vec3 cameraPosition;
uniform vec3 sunPosition;
uniform vec3 moonPosition;
uniform float frameTimeCounter;
uniform float sunAngle;

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
    light *= mix(sh, 1.0, 0.15);                          // soft-ish for entities
    light *= 1.0 - getCloudShadow(pos) * 0.25;
    light += vec3(1.0, 0.82, 0.62) * torch;               // torch glow

    vec4 albedo = texture2D(texture, texcoord.xy) * glcolor;
    vec3 color = albedo.rgb * light;

    vec4 fog = sampledFog(pos);
    float dist = length(pos - cameraPosition);
    float fogF = 1.0 - exp(-fog.a * dist * 0.0022);
    color = mix(color, fog.rgb, clamp(fogF, 0.0, 0.94));

    if (albedo.a < 0.12) discard;
    gl_FragData[0] = vec4(color, albedo.a);
    gl_FragData[1] = vec4(nrm * 0.5 + 0.5, 1.0);
}
