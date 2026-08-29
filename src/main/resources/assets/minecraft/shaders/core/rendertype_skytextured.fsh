#version 330

// REBUILD SKY SHADER — textured variant
// Natively samples custom time-of-day skybox sheets with clock-locked lavender transition.

uniform sampler2D Sampler0;
uniform float GameTime;
uniform float FogShape;

in vec2 UV0;
in vec3 skyDir;

out vec4 fragColor;

void main() {
    // Time-of-day lock: lavender loop -> night without freezing
    float clock = GameTime;
    float phase = fract(clock / 24000.0);

    // Sample custom skybox sheet
    vec4 sample0 = texture(Sampler0, UV0);

    // Dynamic lavender gradient based on sun angle (simulated via time)
    float sunHeight = sin(phase * 6.28318530718); // approximate solar arc
    float lavenderBlend = smoothstep(-0.3, 0.3, sunHeight);

    vec3 lavender = vec3(0.72, 0.55, 1.0);
    vec3 skyColor = mix(sample0.rgb, mix(lavender, vec3(0.02, 0.01, 0.08), smoothstep(0.6, 1.0, 1.0 - lavenderBlend)), 0.4);

    float alpha = sample0.a;
    fragColor = vec4(skyColor, alpha);
}
