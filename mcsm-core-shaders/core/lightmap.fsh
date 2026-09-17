#version 150

// lightmap.fsh - Colored lighting, reflections, direct shinger
// Build #482 + Fabric expansion - shader that changes lighting reflections coloured lighting etc
// Direct shinger - makes game lighting reflect colors from Sift tiers

#moj_import <mcsm_visuals.glsl>

uniform sampler2D Sampler0;
uniform float GameTime;
uniform vec3 PlayerPos;
uniform float IsInSift;
uniform float DepthFactor;
uniform int Tier;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 lightmap = texture(Sampler0, texCoord);
    
    if (IsInSift < 0.5) {
        fragColor = lightmap;
        return;
    }
    
    float y = PlayerPos.y;
    vec3 coloredLight = lightmap.rgb;
    
    // Tier-based colored lighting - reflections
    if (y >= -200.0 && y <= -64.0) {
        // Fabric of Reality - cosmic purple with star reflections
        vec3 fabricColor = vec3(0.4, 0.2, 0.8); // cosmic purple
        float starPulse = sin(GameTime * 0.02 + texCoord.x * 10.0) * 0.5 + 0.5;
        coloredLight = mix(coloredLight, fabricColor, 0.3 + starPulse * 0.1);
        // Direct shinger - reflections
        coloredLight += fabricColor * 0.2 * starPulse;
    } else if (y >= -1000.0 && y < -200.0) {
        // Emptiness - pitch black void of stars - dim lighting with star sparkles
        vec3 voidColor = vec3(0.05, 0.05, 0.15);
        coloredLight = mix(coloredLight, voidColor, 0.7);
        // Fireworks reflections during 40-50 sec fall
        float firework = pow(sin(GameTime * 0.05 + texCoord.x * 5.0), 20.0) * 0.5;
        coloredLight += vec3(1.0, 0.8, 0.4) * firework;
    } else if (y >= -1450.0 && y < -1000.0) {
        // Tier 1 Gel Horizon - cyan god rays, colored lighting
        vec3 gelColor = vec3(0.3, 0.8, 1.0);
        coloredLight = mix(coloredLight, gelColor, 0.2);
        // God rays reflection
        float godRay = pow(max(dot(normalize(vec3(texCoord - 0.5, 1.0)), normalize(vec3(0.2, 1.0, 0.1))), 0.0), 4.0);
        coloredLight += gelColor * godRay * 0.4;
    } else if (y >= -1950.0 && y < -1450.0) {
        // Tier 2 Menger Maze - orange to pink emissive
        float depth = clamp((-1450.0 - y) / 500.0, 0.0, 1.0);
        vec3 orange = vec3(1.0, 0.5, 0.1);
        vec3 pink = vec3(1.0, 0.4, 0.7);
        vec3 mazeColor = mix(orange, pink, depth);
        coloredLight = mix(coloredLight, mazeColor, 0.35);
        // Emissive reflections
        coloredLight += mazeColor * 0.3;
    } else if (y >= -2200.0 && y < -1950.0) {
        // Tier 3 Rift Field - neon purple, magenta rim reflections
        vec3 riftColor = mix(vec3(0.8, 0.2, 1.0), vec3(1.0, 0.2, 0.6), sin(GameTime * 0.01) * 0.5 + 0.5);
        coloredLight = mix(coloredLight, riftColor, 0.25);
        coloredLight += riftColor * 0.25;
    } else if (y >= -2450.0 && y < -2200.0) {
        // Tier 4 Displacement - rainbow bands, colored lighting shifts
        float hue = fract(GameTime * 0.005 + texCoord.x * 0.2);
        vec3 rainbow = hsv2rgb(vec3(hue, 0.7, 1.0));
        coloredLight = mix(coloredLight, rainbow, 0.2);
        coloredLight += rainbow * 0.15;
    } else if (y >= -2800.0 && y < -2450.0) {
        // Tier 5 Iridescent Gel - teal, amethyst, magenta reflections
        float hue = fract(atan(texCoord.x - 0.5, texCoord.y - 0.5) / 6.2831 + GameTime * 0.002);
        vec3 irid = hsv2rgb(vec3(hue, 0.8, 1.0));
        vec3 teal = vec3(0.15, 1.0, 0.85);
        vec3 amethyst = vec3(0.55, 0.25, 1.0);
        vec3 magenta = vec3(1.0, 0.15, 0.65);
        vec3 gel = mix(mix(teal, amethyst, sin(GameTime * 0.01) * 0.5 + 0.5), magenta, cos(GameTime * 0.008) * 0.5 + 0.5);
        gel = mix(gel, irid, 0.4);
        coloredLight = mix(coloredLight, gel, 0.4);
        // Full-bright reflections - glowing water pools
        coloredLight += gel * 0.4;
    } else if (y < -2800.0) {
        // Unknown dimension - bouncy, distortion, ground decay effect
        vec3 unknownColor = vec3(0.2, 0.1, 0.3) + vec3(0.3, 0.2, 0.5) * sin(GameTime * 0.02 + texCoord.x * 3.0);
        coloredLight = mix(coloredLight, unknownColor, 0.5);
    }
    
    // Global direct shinger - makes lighting reflect and shimmer
    float shimmer = sin(GameTime * 0.03 + texCoord.x * 8.0 + texCoord.y * 6.0) * 0.05 + 0.95;
    coloredLight *= shimmer;
    
    // Add subtle vignette for Pixar-VFX triple-A feel
    vec2 vig = texCoord * 2.0 - 1.0;
    float vignette = 1.0 - dot(vig, vig) * 0.1;
    coloredLight *= vignette;
    
    fragColor = vec4(coloredLight, lightmap.a);
}
