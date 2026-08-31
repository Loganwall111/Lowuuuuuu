#version 120

#include "/lib.glsl"

varying vec4 texcoord;
varying vec4 lmcoord;
varying vec4 glcolor;
varying vec3 normal;
varying vec3 worldPos;

uniform sampler2D texture;
uniform vec3 cameraPosition;
uniform float frameTimeCounter;

uniform float ENT_AO; //settings entAO

void main() {
    vec2 luv = lmcoord.xy;
    vec3 band = vec3(ceil(luv.x * 3.0) / 3.0);
    vec3 torch = vec3(1.0, 0.82, 0.62) * (1.0 - luv.y);
    vec4 albedo = texture2D(texture, texcoord.xy) * glcolor;
    vec3 color = albedo.rgb * (band * band + torch);
    color *= mix(getContactAO(worldPos, normalize(normal)), 1.0, 1.0 - ENT_AO);

    vec4 fog = sampledFog(worldPos + cameraPosition);
    float dist = length(worldPos);
    float fogF = 1.0 - exp(-fog.a * dist * 0.0022);
    color = mix(color, fog.rgb, clamp(fogF, 0.0, 0.94));

    if (albedo.a < 0.12) discard;
    gl_FragData[0] = vec4(color, albedo.a);
    gl_FragData[1] = vec4(normal * 0.5 + 0.5, 1.0);
}
