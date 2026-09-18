#version 150

#moj_import <minecraft:mcsm_visuals.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GameTime;
uniform vec3 PlayerPos;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord2;
in vec3 normal;
in vec3 worldPos;
in float tierFactor;
in vec3 emissiveColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    
    // Apply emissive shading for Sift tiers
    if (tierFactor > 0.5) {
        // Full-bright color overlay pass - orange-to-pink gradient ripple
        color.rgb = mix(color.rgb, emissiveColor, 0.6);
        color.rgb += emissiveColor * 0.3; // emissive boost
        // No fog darkening for emissive
    } else if (tierFactor > 0.1) {
        // Iridescent fluid tint
        color.rgb = mix(color.rgb, emissiveColor, 0.4);
        color.rgb += emissiveColor * 0.2;
    }
    
    // God rays lighting
    if (PlayerPos.y > -1250.0 && PlayerPos.y < -251.0) {
        vec3 godRays = calculateGodRays(worldPos, normalize(-worldPos), vertexDistance);
        color.rgb += godRays * 0.3 * (1.0 - tierFactor);
    }
    
    // Fog with tier colors
    float fogFactor = clamp((vertexDistance - FogStart) / (FogEnd - FogStart), 0.0, 1.0);
    vec3 fogCol = FogColor.rgb;
    if (PlayerPos.y < -251.0) {
        // Use Sift fog colors
        if (PlayerPos.y > -700.0) fogCol = vec3(0.6, 0.85, 1.0);
        else if (PlayerPos.y > -1250.0) fogCol = vec3(1.0, 0.5, 0.4);
        else if (PlayerPos.y > -1500.0) fogCol = vec3(0.3, 0.15, 0.5);
        else if (PlayerPos.y > -1800.0) fogCol = vec3(0.4, 0.6, 0.9);
        else fogCol = vec3(0.2, 0.8, 0.8);
    }
    
    color.rgb = mix(color.rgb, fogCol, fogFactor * (1.0 - tierFactor * 0.8));
    
    fragColor = color;
}
