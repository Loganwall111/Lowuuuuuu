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

void main() {

    vec3 nrm = normalize(normal);
    vec3 pos = worldPos + cameraPosition;

    vec4 albedo = texture2D(texture, texcoord.xy) * glcolor;
    bool isLava = albedo.r > 0.55 && albedo.g < 0.4 && albedo.b < 0.2;

    float band = ceil(lmcoord.x * 4.0) * 0.25;
    vec3 light = vec3(band * band) + vec3(1.0, 0.82, 0.62) * (1.0 - lmcoord.y);

    vec3 lightDir = (sunAngle < 0.45) ? normalize(moonPosition) : normalize(sunPosition);
    float ndl = max(dot(nrm, lightDir), 0.0);
    light *= mix(vec3(0.30, 0.35, 0.55), vec3(1.08, 0.97, 0.84), ceil(ndl * 3.0) / 3.0);
    light *= 1.0 - getCloudShadow(pos) * 0.5;

    vec3 color = albedo.rgb * light;

    // blocky "glitter": hard highlights snapped to a grid
    vec3 r = normalize(reflect(-lightDir, nrm));
    vec3 v = normalize(cameraPosition - pos);
    float spec = pow(max(dot(r, v), 0.0), isLava ? 40.0 : 90.0);
    spec = floor(spec * 4.0) / 4.0;
    color += vec3(spec, spec, spec) * (isLava ? vec3(1.0, 0.55, 0.25) : vec3(0.7, 0.9, 1.0)) * 0.8;

    color = isLava ? color * vec3(1.12, 0.95, 0.90) : color * vec3(0.95, 1.05, 1.12);

    // biome-tinted foam edge (stylized water touching the shore)
#ifdef WATER_FOAM
    float foam = vnoise(pos * 0.65 + vec3(frameTimeCounter * 0.05, 0.0, 0.0));
    foam = smoothstep(0.55, 0.72, foam);
    foam *= smoothstep(0.0, 0.12, lmcoord.x);
    color += vec3(0.92, 0.97, 1.0) * foam * 0.55;
#endif

    vec4 fog = sampledFog(pos);
    float dist = length(pos - cameraPosition);
    float fogF = 1.0 - exp(-fog.a * dist * 0.0022);
    color = mix(color, fog.rgb, clamp(fogF, 0.0, 0.94));

    float depth = gl_FragCoord.z;
    gl_FragData[0] = vec4(color, 1.0);                   // opaque Telltale fluid
    gl_FragData[1] = vec4(depth, depth * depth, depth, 1.0);
    gl_FragData[2] = vec4(nrm * 0.5 + 0.5, 1.0);
}
