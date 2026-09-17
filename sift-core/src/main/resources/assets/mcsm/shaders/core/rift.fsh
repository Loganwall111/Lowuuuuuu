#version 150

uniform float GameTime;
uniform float Pulse;
uniform float Seed;
uniform float Choreography;
uniform float Pass;
uniform vec2 ScreenSize;

in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(127.1, 311.7));
    return fract(sin(dot(p, vec2(269.5, 183.3))) * 43758.5453);
}

vec3 energyPalette(float value) {
    vec3 teal = vec3(0.04, 0.78, 0.70);
    vec3 purple = vec3(0.42, 0.10, 0.95);
    vec3 magenta = vec3(1.00, 0.04, 0.50);
    vec3 amber = vec3(1.00, 0.46, 0.08);
    vec3 first = mix(teal, purple, smoothstep(0.10, 0.48, value));
    vec3 second = mix(magenta, amber, smoothstep(0.48, 0.92, value));
    return mix(first, second, step(0.48, value));
}

void main() {
    vec2 uv = texCoord0 * 2.0 - 1.0;
    float time = GameTime * 1.8 + Seed * 0.013;
    float angle = atan(uv.y, uv.x);
    float radius = length(uv);

    // Passes one and two are the rift's atmospheric envelope. They are drawn
    // behind the core so wisps and pressure halos bleed into the surrounding fog.
    if (Pass > 0.5) {
        float shell = smoothstep(0.24, 0.82, radius) * (1.0 - smoothstep(0.92, 1.18, radius));
        float smoke = 0.5 + 0.5 * sin(radius * 13.0 - time * 1.15 + sin(angle * 3.0 + time) * 2.4 + Choreography * 1.7);
        float curl = 0.5 + 0.5 * sin(angle * 5.0 + radius * 8.0 - time * 1.35 + Seed + Choreography * 2.1);
        float filament = 1.0 - smoothstep(0.02, 0.19, abs(sin(angle * 6.0 + radius * 10.0 + time * 0.8 + Seed + Choreography)));
        float envelope = 1.0 - smoothstep(0.34, 1.16, radius);
        float passStrength = Pass > 1.5 ? 1.0 : 0.58;
        vec3 atmosphere = energyPalette(fract(angle * 0.13 + time * 0.025 + Seed * 0.002 + Choreography * 0.06));
        atmosphere *= 0.34 + smoke * 0.40 + curl * 0.24;
        float alpha = (envelope * (0.10 + smoke * 0.10) + shell * filament * (0.22 + Choreography * 0.10))
                * vertexColor.a * passStrength * (0.72 + Pulse * 0.42) * (0.82 + Choreography * 0.48);
        if (alpha < 0.006) {
            discard;
        }
        fragColor = vec4(atmosphere, clamp(alpha, 0.0, 0.48));
        return;
    }

    // A moving displacement wave stands in for the future captured screen texture.
    // The server-authored encounter beat widens the wave without changing the
    // aperture's collision-free gameplay footprint.
    float ripple = sin(radius * 22.0 - time * 2.2 + sin(angle * 5.0 + time) * 1.8)
            * (0.035 + Choreography * 0.022);
    vec2 warped = uv + normalize(uv + vec2(0.0001)) * ripple;
    float warpedRadius = length(warped);

    float interior = 1.0 - smoothstep(0.68, 0.98, warpedRadius);
    float innerDepth = 0.5 + 0.5 * sin(warped.x * 8.0 + sin(warped.y * 5.0 + time) * 1.7 - time);
    vec3 cosmic = mix(vec3(0.005, 0.008, 0.010), vec3(0.05, 0.075, 0.055), innerDepth);
    cosmic += energyPalette(fract(innerDepth + Seed * 0.001)) * pow(interior, 2.0) * 0.16;

    for (int index = 0; index < 14; index++) {
        float fi = float(index);
        vec2 cell = vec2(fi * 3.17 + Seed, fi * 9.11 - Seed);
        vec2 star = vec2(hash21(cell), hash21(cell + 4.2)) * 1.55 - 0.775;
        float point = 1.0 - smoothstep(0.0, 0.018 + hash21(cell + 1.0) * 0.018, distance(warped, star));
        cosmic += vec3(0.55, 0.90, 0.76) * point * interior;
    }

    float ringWave = sin(angle * 9.0 + time * 1.25 + sin(angle * 3.0) * 2.2 + Choreography * 1.2) * (0.055 + Choreography * 0.018);
    float ringRadius = 0.735 + ringWave + sin(time * 0.7 + Seed) * 0.012 + (Choreography - 0.5) * 0.024;
    float ring = 1.0 - smoothstep(0.025, 0.085, abs(radius - ringRadius));
    float broken = smoothstep(-0.45, 0.25, sin(angle * 7.0 + Seed) + sin(angle * 13.0 - time) * 0.28);
    vec3 rim = energyPalette(fract(angle * 0.16 + time * 0.035 + Seed * 0.004));
    rim *= ring * (0.60 + broken * 0.75) * (0.75 + Pulse * 0.45 + Choreography * 0.35);

    float halo = 1.0 - smoothstep(0.66, 1.04, radius);
    vec3 color = cosmic * interior + rim + energyPalette(fract(angle * 0.09 + time * 0.02 + Choreography * 0.04)) * halo * (0.045 + Choreography * 0.018);
    float alpha = clamp(max(interior * (0.92 + Choreography * 0.08), ring * (0.72 + broken * 0.28 + Choreography * 0.10)) * vertexColor.a, 0.0, 0.97);
    if (alpha < 0.012) {
        discard;
    }
    fragColor = vec4(color, alpha);
}
