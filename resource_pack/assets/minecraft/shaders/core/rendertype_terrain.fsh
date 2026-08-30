#version 330

// HARDCODE COLOR LIGHTING & FAKE RESHADE METRIC SHADOWS
// Custom screen-space vignette, local block-face ambient occlusion,
// dynamic magenta/purple color-grading filter over final output buffer.

uniform sampler2D Sampler0; // color texture
uniform sampler2D Sampler1; // shadow depth / occlusion
uniform sampler2D Sampler2; // lightmap
uniform float GameTime;
uniform vec2 OutSize;

in vec2 texCoord0;
in vec3 Position;
in vec4 vertexColor;

out vec4 fragColor;

float screenVignette(vec2 uv, float intensity, float radius) {
    float dist = length((uv - 0.5) * 2.0);
    return smoothstep(radius, radius - 0.3, dist) * intensity;
}

float ambientOcclusion(vec2 coord) {
    // Fake local block-face ambient occlusion using lightmap sample
    float light = texture(Sampler2, coord).r;
    return mix(0.35, 1.0, light);
}

void main() {
    vec4 baseColor = texture(Sampler0, texCoord0);

    // Ambient occlusion math per pixel
    float ao = ambientOcclusion(texCoord0);
    baseColor.rgb *= ao;

    // Dynamic magenta / purple color-grading filter over final output
    vec3 magentaGrade = vec3(0.85, 0.35, 0.95);
    float gradeStrength = 0.35 + 0.15 * sin(GameTime * 0.02);
    baseColor.rgb = mix(baseColor.rgb, baseColor.rgb * magentaGrade, gradeStrength);

    // Screen-space vignette (dark atmospheric fog transition)
    float vignette = screenVignette(texCoord0 / vec2(1.0), 0.45, 0.75);
    baseColor.rgb *= vignette;

    // Simulated dynamic shadow metric (fake shadow based on position depth)
    float shadowMetric = clamp(Position.y * 0.01 + 0.5, 0.0, 1.0);
    baseColor.rgb *= mix(0.65, 1.0, shadowMetric);

    // Dark atmospheric fog transitions
    float fogFactor = smoothstep(0.6, 1.0, shadowMetric);
    vec3 fogColor = vec3(0.12, 0.05, 0.28);
    baseColor.rgb = mix(fogColor, baseColor.rgb, fogFactor);

    fragColor = baseColor * vertexColor;
}
