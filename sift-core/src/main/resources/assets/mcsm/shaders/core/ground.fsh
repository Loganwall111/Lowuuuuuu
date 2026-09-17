#version 150

uniform float GameTime;
uniform float Depth;
uniform float FallSpeed;
uniform vec2 ScreenSize;

in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(127.1, 311.7));
    return fract(sin(dot(p, vec2(269.5, 183.3))) * 43758.5453);
}

vec3 groundPalette(float value) {
    float selector = fract(value);
    vec3 deep = vec3(0.006, 0.010, 0.012);
    vec3 green = vec3(0.025, 0.17, 0.13);
    vec3 violet = vec3(0.18, 0.035, 0.31);
    vec3 ember = vec3(0.68, 0.12, 0.055);
    vec3 first = mix(deep, green, smoothstep(0.0, 0.42, selector));
    vec3 second = mix(violet, ember, smoothstep(0.42, 1.0, selector));
    return mix(first, second, step(0.42, selector));
}

void main() {
    vec2 p = texCoord0 * 2.0 - 1.0;
    float time = GameTime * 0.65;
    float radius = length(p);
    float shelf = 1.0 - smoothstep(0.72, 1.10, radius);
    float depthGlow = 0.35 + Depth * 0.65;

    vec3 color = groundPalette(0.16 + p.x * 0.08 + p.y * 0.13 + Depth * 0.21);
    float strata = 0.5 + 0.5 * sin(radius * 34.0 - time * 1.4 + sin(p.x * 5.0) * 2.0);
    color += groundPalette(0.52 + strata * 0.28) * strata * 0.16;

    // Broken concentric bands make the distant shelf read as a living surface.
    float ringWave = abs(sin(radius * 44.0 - time * 1.2 + sin(p.x * 8.0 + p.y * 5.0) * 1.4));
    float rings = smoothstep(0.83, 0.98, ringWave) * smoothstep(0.12, 0.72, radius);
    color += vec3(0.10, 0.62, 0.47) * rings * (0.22 + depthGlow * 0.22);

    // Sparse glowing fissures suggest ground without adding a real collision
    // surface, so the player remains in free fall through the visual shelf.
    float crackWave = abs(sin(p.x * 17.0 + sin(p.y * 6.0 + time) * 2.5)
            + sin(p.y * 22.0 - time * 0.8 + p.x * 3.0) * 0.62);
    float cracks = 1.0 - smoothstep(0.0, 0.075, crackWave);
    float shardNoise = hash21(floor((p + 1.0) * 8.0));
    cracks *= smoothstep(0.35, 0.80, shardNoise + 0.12);
    color += vec3(0.30, 0.95, 0.66) * cracks * (0.24 + FallSpeed * 0.42);

    float rim = smoothstep(0.54, 0.90, radius) * shelf;
    color += groundPalette(fract(time * 0.025 + radius)) * rim * 0.22;
    color *= 0.82 + shelf * 0.28;

    float alpha = shelf * (0.56 + rings * 0.18 + cracks * 0.20);
    alpha *= vertexColor.a;
    if (alpha < 0.01) {
        discard;
    }
    fragColor = vec4(color, clamp(alpha, 0.0, 0.88));
}
