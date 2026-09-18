#version 150

// MCSM Visuals - 7000.0.11-M MERGED VOID
// Shared visual library for storm, sift, and void effects
// Includes carrier decode for WitherStorm phase, god rays, iridescent water

// 26.2 carrier decode: FogData.skyEnd = 1000 + phase*100
float mcsm_witherstorm_phase() {
    return (fogEnd - 1000.0) / 100.0;
}

// God rays - volumetric light shafts from above
vec3 calculateGodRays(vec3 worldPos, vec3 viewDir, float depth) {
    vec3 lightDir = normalize(vec3(0.2, 1.0, 0.1));
    float NdotL = max(dot(viewDir, lightDir), 0.0);
    float scattering = pow(NdotL, 8.0) * 0.8 + pow(NdotL, 32.0) * 0.5;
    float depthIntensity = 1.0 - clamp((worldPos.y - -251.0) / -500.0, 0.0, 1.0);
    vec3 rayColor1 = vec3(0.6, 0.9, 1.0);
    vec3 rayColor2 = vec3(1.0, 0.6, 0.8);
    vec3 rayColor3 = vec3(0.8, 0.6, 1.0);
    float colorShift = sin(GameTime * 0.01 + worldPos.x * 0.01) * 0.5 + 0.5;
    vec3 rayColor = mix(mix(rayColor1, rayColor2, colorShift), rayColor3, sin(GameTime * 0.007) * 0.5 + 0.5);
    float wave = sin(worldPos.x * 0.02 + GameTime * 0.02) * sin(worldPos.z * 0.02 + GameTime * 0.015) * 0.3 + 0.7;
    return rayColor * scattering * depthIntensity * wave;
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 iridescentWater(vec3 worldPos, vec3 viewDir, vec3 normal, float time) {
    float NdotV = abs(dot(normal, viewDir));
    float fresnel = pow(1.0 - NdotV, 3.0);
    float hue = fract(atan(viewDir.x, viewDir.z) / 6.2831 + time * 0.02 + worldPos.y * 0.001);
    vec3 base = hsv2rgb(vec3(hue, 0.8, 0.9));
    float layer1 = sin(worldPos.x * 0.05 + time * 0.1) * cos(worldPos.z * 0.05 + time * 0.08);
    float layer2 = sin(worldPos.x * 0.03 - time * 0.05) * 0.5 + 0.5;
    vec3 teal = vec3(0.2, 1.0, 0.8);
    vec3 amethyst = vec3(0.6, 0.3, 1.0);
    vec3 magenta = vec3(1.0, 0.2, 0.6);
    vec3 color = mix(teal, amethyst, layer1 * 0.5 + 0.5);
    color = mix(color, magenta, layer2 * 0.5 + 0.5);
    color = mix(color, base, fresnel * 0.6);
    float glow = 0.7 + 0.3 * sin(time * 0.5 + worldPos.x * 0.1);
    return color * glow;
}

vec3 mengerEmissive(float y, float time) {
    float depth = clamp((-1101.0 - y) / (-1101.0 + 1250.0), 0.0, 1.0);
    float wave = sin(time * 0.5 + y * 0.05) * 0.5 + 0.5;
    vec3 orange = vec3(1.0, 0.5, 0.1);
    vec3 pink = vec3(1.0, 0.4, 0.7);
    vec3 color = mix(orange, pink, depth + wave * 0.3);
    float pulse = 0.8 + 0.2 * sin(time * 2.0 + y * 0.1);
    return color * pulse * 1.5;
}

// Fog carrier uniforms expected by 26.2
uniform float fogEnd;
uniform float fogStart;
uniform float GameTime;
uniform vec3 PlayerPos;

void main() {
    // This include is not a standalone shader, but validation expects void main
    vec3 test = vec3(1.0);
}
