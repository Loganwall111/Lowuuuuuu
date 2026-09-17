#version 150

// MCSM Sift Cosmos - Visual Library
// Build #482 - Infinite Sift Cosmos
// Pixar-VFX triple-A animation environment

#define TIER_1_TOP -251
#define TIER_2_TOP -701
#define TIER_2_BOTTOM -1250
#define TIER_3_TOP -1251
#define TIER_4_TOP -1501
#define TIER_5_TOP -1801
#define ABSOLUTE_FLOOR -2032

uniform float GameTime;
uniform vec3 PlayerPos;
uniform int LoopCount;
uniform float DepthFactor;

// God rays calculation - volumetric light shafts from above
vec3 calculateGodRays(vec3 worldPos, vec3 viewDir, float depth) {
    vec3 lightDir = normalize(vec3(0.2, 1.0, 0.1));
    float NdotL = max(dot(viewDir, lightDir), 0.0);
    
    // Volumetric scattering
    float scattering = pow(NdotL, 8.0) * 0.8;
    scattering += pow(NdotL, 32.0) * 0.5;
    
    // Depth-based intensity - stronger near top
    float depthIntensity = 1.0 - clamp((PlayerPos.y - TIER_1_TOP) / -500.0, 0.0, 1.0);
    
    // Colored lights - different colored lights
    vec3 rayColor1 = vec3(0.6, 0.9, 1.0); // cyan
    vec3 rayColor2 = vec3(1.0, 0.6, 0.8); // pink
    vec3 rayColor3 = vec3(0.8, 0.6, 1.0); // purple
    
    float colorShift = sin(GameTime * 0.01 + worldPos.x * 0.01) * 0.5 + 0.5;
    vec3 rayColor = mix(mix(rayColor1, rayColor2, colorShift), rayColor3, sin(GameTime * 0.007) * 0.5 + 0.5);
    
    // Animate ray movement
    float wave = sin(worldPos.x * 0.02 + GameTime * 0.02) * sin(worldPos.z * 0.02 + GameTime * 0.015) * 0.3 + 0.7;
    
    return rayColor * scattering * depthIntensity * wave;
}

// Iridescent Cosmic Fluid Shaders - rainbow water
vec3 iridescentWater(vec3 worldPos, vec3 viewDir, vec3 normal, float time) {
    // Viewing angle based color shift - shimmering neon teals, deep amethysts, toxic magentas
    float NdotV = abs(dot(normal, viewDir));
    float fresnel = pow(1.0 - NdotV, 3.0);
    
    // Hue shift based on viewing angle
    float hue = fract(atan(viewDir.x, viewDir.z) / 6.2831 + time * 0.02 + worldPos.y * 0.001);
    vec3 base = hsv2rgb(vec3(hue, 0.8, 0.9));
    
    // Additional iridescent layers
    float layer1 = sin(worldPos.x * 0.05 + time * 0.1) * cos(worldPos.z * 0.05 + time * 0.08);
    float layer2 = sin(worldPos.x * 0.03 - time * 0.05) * 0.5 + 0.5;
    
    vec3 teal = vec3(0.2, 1.0, 0.8);
    vec3 amethyst = vec3(0.6, 0.3, 1.0);
    vec3 magenta = vec3(1.0, 0.2, 0.6);
    
    vec3 color = mix(teal, amethyst, layer1 * 0.5 + 0.5);
    color = mix(color, magenta, layer2 * 0.5 + 0.5);
    color = mix(color, base, fresnel * 0.6);
    
    // Glowing
    float glow = 0.7 + 0.3 * sin(time * 0.5 + worldPos.x * 0.1);
    return color * glow;
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

// Menger Sponge emissive gradient - orange to pink ripple
vec3 mengerEmissive(float y, float time) {
    float depth = clamp(( -1101.0 - y) / (-1101.0 + 1250.0), 0.0, 1.0);
    float wave = sin(time * 0.5 + y * 0.05) * 0.5 + 0.5;
    
    vec3 orange = vec3(1.0, 0.5, 0.1);
    vec3 pink = vec3(1.0, 0.4, 0.7);
    vec3 color = mix(orange, pink, depth + wave * 0.3);
    
    // Full-bright vertex-shimmed
    float pulse = 0.8 + 0.2 * sin(time * 2.0 + y * 0.1);
    return color * pulse * 1.5; // emissive boost
}

// Wavy Spacetime Rift - displacement wave calculation
vec2 riftDisplacement(vec2 uv, float time, float wavePhase) {
    float wave = sin(uv.x * 10.0 + time * 0.5 + wavePhase) * 0.02;
    wave += sin(uv.y * 8.0 + time * 0.3 + wavePhase * 1.3) * 0.015;
    wave += sin(length(uv - 0.5) * 20.0 - time * 0.8) * 0.01;
    
    // Razor-sharp jagged border preserved, inner face ripples liquid-like
    float dist = length(uv - 0.5);
    float edge = smoothstep(0.4, 0.45, dist); // sharp edge
    return vec2(wave) * (1.0 - edge); // only displace interior
}

// Cosmic Window Effect - independent parallax rendering layer inside rippling wave boundary
vec3 cosmicWindow(vec2 uv, float time, float cosmicOffset) {
    // Moving alternative star arrays, cosmic dust fragments, passing dark silhouettes
    vec2 p = uv - 0.5;
    p *= 2.0;
    
    // Star field parallax
    float stars = 0.0;
    for (int i = 0; i < 3; i++) {
        float scale = pow(2.0, float(i));
        vec2 st = p * scale + vec2(time * 0.01 * float(i+1) + cosmicOffset);
        st = fract(st) - 0.5;
        float d = length(st);
        stars += smoothstep(0.1, 0.0, d) * 0.5 / scale;
    }
    
    // Cosmic dust
    float dust = sin(p.x * 3.0 + time * 0.1) * cos(p.y * 2.0 + time * 0.07) * 0.5 + 0.5;
    dust = pow(dust, 3.0) * 0.6;
    
    // Dark silhouettes passing - like ghost whales in distance
    float silhouette = 0.0;
    float t = time * 0.05 + cosmicOffset;
    vec2 silPos = vec2(sin(t) * 0.5, cos(t * 0.7) * 0.3);
    float silDist = length(p - silPos);
    silhouette = smoothstep(0.3, 0.2, silDist) * 0.4;
    
    vec3 starColor = vec3(0.6, 0.8, 1.0) * stars;
    vec3 dustColor = vec3(0.8, 0.4, 1.0) * dust;
    vec3 bg = vec3(0.1, 0.05, 0.3) + starColor + dustColor;
    bg = mix(bg, vec3(0.0), silhouette); // dark silhouettes
    
    return bg;
}

// Emissive Rim - full-bright glowing neon-purple and hot-magenta border
vec3 riftRim(float dist, float time, float pulse) {
    // dist = distance from center, rim at 0.45
    float rim = smoothstep(0.4, 0.45, dist) * (1.0 - smoothstep(0.45, 0.5, dist));
    float flicker = sin(time * 5.0) * 0.1 + 0.9;
    
    vec3 purple = vec3(0.8, 0.2, 1.0);
    vec3 magenta = vec3(1.0, 0.2, 0.6);
    vec3 color = mix(purple, magenta, sin(time * 0.5) * 0.5 + 0.5);
    
    return color * rim * pulse * flicker * 3.0; // full-bright
}

// Refraction & Intersection Foams for cosmic fluid
vec3 fluidRefraction(vec3 color, vec3 worldPos, float depth, float time) {
    // Deep refraction distortions
    float distort = sin(worldPos.x * 0.1 + time) * cos(worldPos.z * 0.1 + time * 0.8) * 0.05;
    
    // Bright full-bright white intersection foam lines that trace softly whenever entity intersects fluid boundary
    float foam = 0.0;
    float intersect = fract(worldPos.y * 0.5 + time * 0.1);
    foam = smoothstep(0.0, 0.1, intersect) * smoothstep(0.2, 0.1, intersect);
    foam *= 0.8 + 0.2 * sin(time * 3.0 + worldPos.x);
    
    vec3 foamColor = vec3(1.0) * foam * 2.0;
    return color + foamColor + vec3(distort);
}

// Rainbow water color shift based on viewing angle
vec3 rainbowWaterShift(vec3 viewDir, vec3 normal, float time) {
    float angle = dot(viewDir, normal);
    float hue = fract(angle * 0.5 + time * 0.02);
    return hsv2rgb(vec3(hue, 0.7, 1.0));
}

// Floating particle glow
vec3 particleGlow(vec3 pos, float time) {
    float pulse = sin(time + pos.x * 0.1 + pos.y * 0.1 + pos.z * 0.1) * 0.5 + 0.5;
    float hue = fract(pos.x * 0.01 + pos.y * 0.01 + time * 0.01);
    return hsv2rgb(vec3(hue, 0.6, 1.0)) * pulse * 0.8;
}
