#version 150

#moj_import <mcsm_visuals.glsl>

uniform vec4 ColorModulator;
uniform float GameTime;
uniform vec3 PlayerPos;

in vec4 vertexColor;
in vec3 worldPos;
in float depthFactor;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor * ColorModulator;
    
    // Tier-based color shift for position shader (used for sky, clouds, etc)
    if (PlayerPos.y < -251.0) {
        // Add iridescent tint based on depth
        float hue = fract(depthFactor + GameTime * 0.005);
        vec3 irid = hsv2rgb(vec3(hue, 0.5, 1.0));
        color.rgb = mix(color.rgb, irid, depthFactor * 0.3);
        
        // God rays
        vec3 god = calculateGodRays(worldPos, normalize(vec3(0.0, 1.0, 0.0)), depthFactor);
        color.rgb += god * 0.2;
    }
    
    fragColor = color;
}
