#version 150

uniform float GameTime;
uniform float Layer;
uniform float PlayerDelta;
uniform vec2 ScreenSize;

in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

vec3 fluidPalette(float value) {
    vec3 teal = vec3(0.02, 0.74, 0.68);
    vec3 amethyst = vec3(0.32, 0.08, 0.75);
    vec3 magenta = vec3(0.96, 0.02, 0.42);
    vec3 amber = vec3(1.00, 0.30, 0.07);
    vec3 first = mix(teal, amethyst, smoothstep(0.12, 0.52, value));
    vec3 second = mix(magenta, amber, smoothstep(0.52, 0.94, value));
    return mix(first, second, step(0.52, value));
}

void main() {
    vec2 uv = texCoord0;
    vec2 centered = uv * 2.0 - 1.0;
    float time = GameTime * 1.25 + Layer * 0.71;

    float waveA = sin(centered.x * 17.0 + time * 1.8 + sin(centered.y * 8.0) * 1.7);
    float waveB = cos(centered.y * 21.0 - time * 1.35 + sin(centered.x * 6.0) * 2.1);
    vec2 displaced = centered + vec2(waveB, waveA) * 0.045;
    float radial = length(displaced);
    float poolMask = 1.0 - smoothstep(0.73, 1.02, radial);

    float iridescence = fract(0.32 + Layer * 0.17 + displaced.x * 0.25 + displaced.y * 0.18 + waveA * 0.08 + time * 0.035);
    vec3 color = fluidPalette(iridescence);
    color *= 0.48 + 0.35 * (0.5 + 0.5 * waveB);

    // Bright ripples become intersection foam when the player crosses a sheet.
    float foamWave = abs(sin(radial * 31.0 - time * 2.3 + waveA * 2.0));
    float foam = smoothstep(0.80, 0.98, foamWave) * smoothstep(0.25, 0.92, poolMask);
    float intersection = exp(-max(PlayerDelta, 0.0) * 3.8);
    color += vec3(1.0, 0.96, 0.86) * foam * intersection * 1.6;

    float edgeGlow = smoothstep(0.48, 0.92, radial) * poolMask;
    color += fluidPalette(fract(iridescence + 0.21)) * edgeGlow * 0.22;
    float alpha = poolMask * (0.18 + 0.18 * (0.5 + 0.5 * waveA)) + foam * intersection * 0.38;
    alpha *= vertexColor.a;

    if (alpha < 0.012) {
        discard;
    }
    fragColor = vec4(color, clamp(alpha, 0.0, 0.92));
}
