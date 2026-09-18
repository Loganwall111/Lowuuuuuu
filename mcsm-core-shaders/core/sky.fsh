#version 150
#moj_import <minecraft:fog.glsl>

// sky.fsh - Animated skyboxes for each Sift layer
// Build #482 - God rays, colored lights, rainbow water

#moj_import <minecraft:mcsm_visuals.glsl>

uniform float GameTime;
uniform vec3 PlayerPos;
uniform float DepthFactor;
uniform int Tier;
uniform float IsInSift;

in vec3 Position;
in vec3 Color;
in vec2 TexCoord;

out vec4 fragColor;

vec3 hsv2rgb2(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    if (IsInSift < 0.5) {
        fragColor = vec4(Color, 1.0);
        return;
    }
    
    vec3 dir = normalize(Position);
    float y = PlayerPos.y;
    float time = GameTime * 0.01;
    
    vec3 skyColor = Color;
    
    // Tier-based skybox
    if (y > -700.0) {
        // Tier 1 - Gel Horizon - cyan with god rays
        float godRay = pow(max(dot(dir, normalize(vec3(0.2, 1.0, 0.1))), 0.0), 12.0);
        vec3 ray1 = vec3(0.6, 0.9, 1.0) * godRay * 1.5;
        vec3 ray2 = vec3(1.0, 0.8, 0.9) * pow(godRay, 0.5) * 0.8;
        
        // Animated sky with rainbow water reflection below
        float hue = fract(atan(dir.x, dir.z) / 6.2831 + time * 0.01);
        vec3 rainbow = hsv2rgb2(vec3(hue, 0.3, 0.9));
        
        skyColor = mix(vec3(0.5, 0.8, 1.0), rainbow, 0.3);
        skyColor += ray1 + ray2;
        
        // Floating islands silhouette - distant
        float islands = sin(dir.x * 2.0 + time) * cos(dir.z * 1.5 + time * 0.7) * 0.5 + 0.5;
        islands = smoothstep(0.6, 0.7, islands) * 0.2;
        skyColor = mix(skyColor, vec3(0.3, 0.5, 0.7), islands);
        
    } else if (y > -1250.0) {
        // Tier 2 - Menger Maze - orange to pink gradient that responds to global world time
        float depth = clamp((-701.0 - y) / 549.0, 0.0, 1.0);
        vec3 orange = vec3(1.0, 0.5, 0.1);
        vec3 pink = vec3(1.0, 0.4, 0.7);
        float wave = sin(time * 0.5 + y * 0.02) * 0.5 + 0.5;
        skyColor = mix(orange, pink, depth + wave * 0.2) * 1.2;
        skyColor *= 0.6 + 0.4 * sin(time + dir.x * 2.0);
        
    } else if (y > -1500.0) {
        // Tier 3 - Rift Field - dark with cosmic windows
        skyColor = vec3(0.08, 0.04, 0.18);
        // Stars
        float stars = pow(fract(sin(dot(dir.xz, vec2(12.9898, 78.233))) * 43758.5453), 20.0);
        skyColor += vec3(0.6, 0.7, 1.0) * stars * 0.8;
        
        // Rift glow
        float riftGlow = sin(dir.x * 3.0 + time) * cos(dir.z * 2.0 + time * 0.5) * 0.5 + 0.5;
        riftGlow = smoothstep(0.7, 0.9, riftGlow);
        skyColor += vec3(0.8, 0.2, 1.0) * riftGlow * 0.6;
        
    } else if (y > -1800.0) {
        // Tier 4 - Displacement Bands - wavy rainbow bands
        float band = sin(dir.y * 10.0 + time * 0.5) * 0.5 + 0.5;
        float hue = fract(time * 0.02 + dir.x * 0.1 + band * 0.3);
        skyColor = hsv2rgb2(vec3(hue, 0.8, 0.9)) * (0.5 + band * 0.5);
        
        // Wavy distortion
        float wave = sin(dir.x * 5.0 + time) * 0.1;
        skyColor += vec3(wave);
        
    } else {
        // Tier 5 - Iridescent Gel Void - most beautiful, rainbow water, god rays from below
        float hue = fract(atan(dir.x, dir.z) / 6.2831 + time * 0.02 + dir.y * 0.1);
        vec3 irid = hsv2rgb2(vec3(hue, 0.75, 1.0));
        
        // Teal, amethyst, magenta
        vec3 teal = vec3(0.15, 1.0, 0.85);
        vec3 amethyst = vec3(0.55, 0.25, 1.0);
        vec3 magenta = vec3(1.0, 0.15, 0.65);
        
        float l1 = sin(dir.x * 2.0 + time * 0.3) * 0.5 + 0.5;
        float l2 = cos(dir.z * 1.5 + time * 0.2) * 0.5 + 0.5;
        
        skyColor = mix(teal, amethyst, l1);
        skyColor = mix(skyColor, magenta, l2 * 0.6);
        skyColor = mix(skyColor, irid, 0.4);
        
        // God rays from below (inverted)
        float godRay = pow(max(dot(dir, normalize(vec3(0.1, -1.0, 0.2))), 0.0), 8.0);
        skyColor += vec3(0.4, 0.8, 1.0) * godRay * 1.2;
        
        // Glowing water pools reflection
        float waterGlow = smoothstep(-0.2, 0.0, dir.y) * 0.5;
        skyColor += vec3(0.2, 0.9, 0.8) * waterGlow;
    }
    
    // Global time-based hue shift for Pixar feel
    skyColor = pow(skyColor, vec3(0.9));
    
    // Add floating particles - legal particles wait for something, you're not really flying, you're floating
    float particles = 0.0;
    for (int i = 0; i < 2; i++) {
        vec2 p = dir.xz * (1.0 + float(i)) + time * 0.05 * float(i+1);
        p = fract(p) - 0.5;
        particles += smoothstep(0.05, 0.0, length(p)) * 0.3;
    }
    skyColor += vec3(1.0, 0.9, 0.6) * particles * 0.4;
    
    fragColor = vec4(skyColor, 1.0);
}
