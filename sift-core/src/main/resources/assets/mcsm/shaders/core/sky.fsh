#version 150

uniform float GameTime;
uniform float Depth;
uniform float FallSpeed;
uniform vec2 ScreenSize;

in vec2 vertexUV;
out vec4 fragColor;

const vec3 FOG_DARK = vec3(0.004, 0.006, 0.005);
const vec3 FOG_SIFT = vec3(0.110, 0.122, 0.086);

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float siftNoise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fogNoise(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int octave = 0; octave < 5; octave++) {
        value += siftNoise2(p) * amplitude;
        p = p * 2.03 + vec2(17.1, -9.3);
        amplitude *= 0.5;
    }
    return value;
}

vec3 rayColor(float index) {
    vec3 cyan = vec3(0.04, 0.72, 0.66);
    vec3 violet = vec3(0.38, 0.12, 0.92);
    vec3 orange = vec3(1.00, 0.27, 0.06);
    vec3 blue = vec3(0.08, 0.28, 1.00);
    float selector = fract(index * 0.37 + 0.13);
    return mix(mix(cyan, violet, smoothstep(0.0, 0.45, selector)),
               mix(orange, blue, smoothstep(0.45, 1.0, selector)),
               step(0.45, selector));
}

void main() {
    vec2 safeSize = max(ScreenSize, vec2(1.0));
    vec2 uv = gl_FragCoord.xy / safeSize;
    vec2 p = uv * 2.0 - 1.0;
    p.x *= safeSize.x / safeSize.y;

    float time = GameTime * 0.42;
    float deep = smoothstep(0.0, 0.82, clamp(Depth, 0.0, 1.0));
    float movingFog = fogNoise(p * (1.35 + deep * 1.4) + vec2(time * 0.08, -time * 0.045));

    // A readable vertical gradient keeps the Sift atmospheric even when the
    // optional procedural shader is running without any rift entities nearby.
    float horizonBlend = 1.0 - smoothstep(-0.92, 0.72, p.y);
    vec3 horizonColor = mix(
            vec3(0.035, 0.095, 0.075),
            vec3(0.085, 0.070, 0.185),
            deep * 0.82 + movingFog * 0.10
    );
    vec3 zenithColor = mix(
            vec3(0.002, 0.004, 0.009),
            vec3(0.012, 0.008, 0.038),
            deep
    );
    vec3 color = mix(zenithColor, horizonColor, horizonBlend);
    color = mix(color, FOG_SIFT, deep * (0.16 + movingFog * 0.18));
    color += vec3(0.008, 0.015, 0.010) * movingFog;

    // Slow aurora curtains add the cool cyan-violet-orange sky gradient from
    // the reference mood without requiring a texture or a post-process target.
    for (int index = 0; index < 6; index++) {
        float fi = float(index);
        float center = -0.62 + fi * 0.29 + sin(time * 0.55 + fi * 1.7) * 0.075;
        float width = 0.045 + 0.025 * (0.5 + 0.5 * sin(fi * 1.3 + time));
        float band = 1.0 - smoothstep(0.0, width, abs(p.y - center));
        float wave = 0.5 + 0.5 * sin(p.x * (4.0 + fi * 0.55) + time * (0.45 + fi * 0.07) + movingFog * 4.0 + fi);
        band *= 0.35 + wave * 0.65;
        color += rayColor(fi + 2.0) * band * (0.014 + deep * 0.072);
    }

    // Layered light shafts: finite ray steps keep this compatible with GLSL 1.50.
    for (int index = 0; index < 9; index++) {
        float fi = float(index);
        float drift = sin(time * (0.55 + fi * 0.035) + fi * 2.71) * 0.21;
        float center = (fi - 4.0) * 0.36 + drift;
        float width = 0.018 + 0.012 * sin(fi * 1.7 + time);
        float shaft = 1.0 - smoothstep(0.0, width, abs(p.x - center));
        shaft *= smoothstep(-1.2, 0.65, p.y + sin(p.x * 1.7 + time + fi) * 0.30);
        shaft *= (0.10 + 0.34 * deep) * (0.55 + FallSpeed * 0.75);
        color += rayColor(fi) * shaft;
    }

    // Small parallax-like star arrays are displaced by depth and fall speed.
    for (int index = 0; index < 36; index++) {
        float fi = float(index);
        vec2 cell = vec2(fi * 1.71, fi * 7.13);
        vec2 star = vec2(hash21(cell), hash21(cell + 8.41));
        star = star * 2.0 - 1.0;
        star.x *= safeSize.x / safeSize.y;
        star.x += sin(time * 0.18 + fi) * 0.006 * (0.4 + FallSpeed);
        star.y += fract(time * 0.015 + fi * 0.001) * 0.06;
        float point = 1.0 - smoothstep(0.0, 0.008 + hash21(cell + 2.0) * 0.012, distance(p, star));
        color += vec3(0.60, 0.78, 0.62) * point * deep * 0.75;
    }

    float horizonGlow = exp(-abs(p.y + 0.22) * 4.2) * (0.02 + deep * 0.07);
    color += vec3(0.12, 0.20, 0.13) * horizonGlow;
    color = 1.0 - exp(-color * (1.15 + deep * 1.5));
    fragColor = vec4(color, 1.0);
}
