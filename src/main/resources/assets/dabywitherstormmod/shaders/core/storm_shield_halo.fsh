#version 330

// SHIELD HALO FRAGMENT — alpha-blending transparency and edge fading
// Renders the protective barrier as a persistent emissive spherical mesh.

in vec3 sphereCenter;
in float edgeFade;
in float alphaBlend;

uniform float GameTime;
uniform float ShieldIntensity;

out vec4 fragColor;

void main() {
    // Dynamic glow pulse synchronized with storm phase
    float pulse = 0.5 + 0.5 * sin(GameTime * 0.08);
    float intensity = alphaBlend * (0.7 + 0.3 * pulse);

    // Purple/magenta shield tint
    vec3 shieldColor = vec3(0.78, 0.2, 0.95);
    // Edge fading transparency
    float alpha = intensity * edgeFade;

    fragColor = vec4(shieldColor * intensity, alpha);
}
