#version 330

// REBUILD SKY SHADER — native sample of custom time-of-day skybox sheets
// Monitors live game uniform engine clock for lavender -> night transition.

uniform sampler2D Sampler0; // time-of-day skybox sheet
uniform float GameTime;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec3 Position;
in vec2 UV0;

out vec4 fragColor;

void main() {
    // Sample the custom skybox sheet stored in the folder tree
    vec4 skySample = texture(Sampler0, UV0);

    // Monitor live game uniform engine clock for smooth transition
    float timePhase = fract(GameTime / 24000.0); // 0..1 over full day cycle

    // Lavender background loop transitions smoothly into night
    vec3 lavenderSky = mix(skySample.rgb, vec3(0.15, 0.05, 0.35), smoothstep(0.75, 1.0, timePhase));
    float alpha = skySample.a;

    fragColor = vec4(lavenderSky, alpha);
}
