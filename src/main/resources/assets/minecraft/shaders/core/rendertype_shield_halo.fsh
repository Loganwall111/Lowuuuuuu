#version 330

// SHIELD HALO FRAGMENT — native core shader array for resource pack
// Alpha-blending transparency with localized edge fading.

in vec3 sphereCenter;
in float edgeFade;
in float alphaBlend;

uniform float GameTime;
uniform float ShieldIntensity;

out vec4 fragColor;

void main() {
    float pulse = 0.5 + 0.5 * sin(GameTime * 0.08);
    float intensity = alphaBlend * (0.7 + 0.3 * pulse);
    vec3 shieldColor = vec3(0.78, 0.2, 0.95);
    float alpha = intensity * edgeFade;
    fragColor = vec4(shieldColor * intensity, alpha);
}
