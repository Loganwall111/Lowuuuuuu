#version 150

uniform float GameTime;
uniform float Pulse;
uniform float Seed;
uniform vec2 ScreenSize;

in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;

vec3 drifterPalette(float value) {
    float segment = fract(value) * 4.0;
    vec3 mint = vec3(0.12, 0.96, 0.72);
    vec3 cyan = vec3(0.05, 0.55, 1.00);
    vec3 violet = vec3(0.48, 0.12, 1.00);
    vec3 pink = vec3(1.00, 0.08, 0.50);
    vec3 gold = vec3(1.00, 0.48, 0.08);
    if (segment < 1.0) {
        return mix(mint, cyan, smoothstep(0.0, 1.0, segment));
    }
    if (segment < 2.0) {
        return mix(cyan, violet, smoothstep(1.0, 2.0, segment));
    }
    if (segment < 3.0) {
        return mix(violet, pink, smoothstep(2.0, 3.0, segment));
    }
    return mix(pink, gold, smoothstep(3.0, 4.0, segment));
}

void main() {
    vec2 uv = texCoord0 * 2.0 - 1.0;
    float time = GameTime * 1.7 + Seed * 0.012;
    float angle = atan(uv.y, uv.x);
    float radius = length(uv);

    float body = 1.0 - smoothstep(0.10, 0.56, radius);
    float shell = 1.0 - smoothstep(0.48, 0.90, radius);
    float wingWave = 0.5 + 0.5 * sin(angle * 4.0 + time + sin(radius * 12.0 - time) * 1.8 + Seed);
    float wings = smoothstep(0.28, 0.92, abs(uv.x) + abs(uv.y) * 0.28) * shell;
    float tendrilWave = 1.0 - smoothstep(0.02, 0.16, abs(sin(angle * 7.0 + radius * 10.0 - time * 1.2 + Seed)));
    float tendrils = tendrilWave * smoothstep(0.42, 0.95, radius) * (1.0 - smoothstep(0.88, 1.05, radius));
    float eye = 1.0 - smoothstep(0.0, 0.075, distance(uv, vec2(0.15, 0.05)));
    float halo = 1.0 - smoothstep(0.30, 1.04, radius);

    vec3 color = drifterPalette(fract(0.10 + time * 0.025 + radius * 0.24 + Seed * 0.001));
    color *= 0.55 + wingWave * 0.45;
    color += drifterPalette(fract(time * 0.04 + angle * 0.12 + 0.31)) * wings * 0.30;
    color += vec3(0.76, 1.0, 0.92) * eye * (0.7 + Pulse * 0.6);
    color += drifterPalette(fract(0.62 + angle * 0.2)) * tendrils * 0.48;

    float alpha = max(body * 0.90, wings * 0.42);
    alpha = max(alpha, tendrils * 0.60);
    alpha += halo * 0.08 * (0.6 + Pulse * 0.4);
    alpha *= vertexColor.a;
    if (alpha < 0.012) {
        discard;
    }
    fragColor = vec4(color, clamp(alpha, 0.0, 0.92));
}
