#version 150
#moj_import <minecraft:fog.glsl>

// final.fsh - Iridescent Cosmic Fluid Shaders & Glowing Water Pools + Reality-Glitch Nightmare
// Build #485 - The Ninth Layer: Reality-Glitch Nightmare / Uninpossible Layer
// Screen-Space Cosmic Liquid for Layer 5 gel void bounds (-1801 to -2032)
// Iridescent color shims, refraction & intersection foams
// + Matte-black & radiant shading filters for Tier 9: #0A0E14 deep navy-black and #000000 void-black
// + Face emission auras and phase-shifting color skirts at 4.5x bloom glow
// + Gothic towers, hanging fortresses, jagged ridges, colossal Creator mesh

#moj_import <minecraft:mcsm_visuals.glsl>

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform float GameTime;
uniform vec3 PlayerPos;
uniform int LoopCount;

out vec4 fragColor;

// Cosmic fluid uniforms
uniform float IsInSift;
uniform float TierFactor;
uniform float DepthFactor;

vec3 hsv2rgb_f(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec4 baseColor = texture(DiffuseSampler, texCoord);
    float depth = texture(DepthSampler, texCoord).r;
    
    // Only apply Sift effects when in void dimension
    if (IsInSift < 0.5) {
        fragColor = baseColor;
        return;
    }
    
    vec3 finalColor = baseColor.rgb;
    float y = PlayerPos.y;
    
    // BUILD #485 -- TIER 9: Reality-Glitch Nightmare / Uninpossible Layer
    // Deep sub-bedrock coordinate handshake: handle both plan-space -2032..-1801 and safe -64..-60
    // Matte-black #0A0E14 and void-black #000000 with 4.5x bloom, face emission auras, phase-shifting skirts
    if ((y >= -2032.0 && y <= -1801.0) || (y <= -60.0 && y >= -64.0)) {
        vec2 uv = texCoord;
        float time = GameTime * 0.01;
        
        // Determine if in deepest nightmare (uninpossible)
        float isUninpossible = 0.0;
        if (y <= -62.0) isUninpossible = 1.0;
        else if (y <= -60.0) isUninpossible = clamp((-60.0 - y) / 2.0, 0.0, 1.0);
        else isUninpossible = clamp((-1801.0 - y) / 231.0, 0.0, 1.0);
        
        // Matte-black & radiant shading filters
        vec3 matteBlack = vec3(0.0392, 0.0549, 0.0784); // #0A0E14 deep navy-black
        vec3 voidBlack = vec3(0.0, 0.0, 0.0); // #000000
        vec3 baseNightmare = mix(matteBlack, voidBlack, isUninpossible);
        
        // Apply matte filter - darken base but keep luminance for bloom areas
        float luminance = dot(finalColor, vec3(0.299, 0.587, 0.114));
        vec3 darkened = mix(finalColor * 0.15, baseNightmare, 0.85 * isUninpossible);
        finalColor = mix(finalColor, darkened, 0.7 * isUninpossible);
        
        // Face emission auras - glowing purple lenses of Creator mesh (full-bright emissive)
        vec2 center = uv - 0.5;
        float distToCenter = length(center);
        float angleToCenter = atan(center.y, center.x);
        
        // Creator head position simulation - top center with gaze tracking
        vec2 creatorPos = vec2(0.0 + sin(GameTime * 0.0005) * 0.08, -0.25 + cos(GameTime * 0.0004) * 0.05);
        float distToCreator = length(uv - creatorPos - 0.5);
        
        // Glowing purple lenses #8A2BE2 full-bright emissive RenderTypes.eyes
        vec3 purpleLens = vec3(0.541, 0.168, 0.886); // #8A2BE2
        float eyeGlow = 0.0;
        // Two eyes
        vec2 leftEye = creatorPos + vec2(-0.06, 0.02) + vec2(sin(GameTime * 0.001 + 1.0) * 0.02, cos(GameTime * 0.001 + 0.5) * 0.01);
        vec2 rightEye = creatorPos + vec2(0.06, 0.02) + vec2(sin(GameTime * 0.001 + 1.0) * 0.02, cos(GameTime * 0.001 + 0.5) * 0.01);
        float leftDist = length(uv - leftEye - 0.5);
        float rightDist = length(uv - rightEye - 0.5);
        eyeGlow += exp(-leftDist * 35.0) * 2.5; // sharp eye
        eyeGlow += exp(-rightDist * 35.0) * 2.5;
        eyeGlow *= 4.5; // 4.5x bloom glow
        eyeGlow *= isUninpossible;
        
        vec3 eyeColor = purpleLens * eyeGlow;
        // Pupil dark center
        float leftPupil = smoothstep(0.025, 0.015, leftDist);
        float rightPupil = smoothstep(0.025, 0.015, rightDist);
        eyeColor *= (1.0 - leftPupil * 0.8);
        eyeColor *= (1.0 - rightPupil * 0.8);
        
        // Face emission aura - soft glow around creator head
        float aura = exp(-distToCreator * 3.5) * 0.6;
        aura *= isUninpossible;
        vec3 radiantPurple = vec3(0.615, 0.0, 1.0); // #9D00FF radiant
        vec3 auraColor = mix(purpleLens, radiantPurple, sin(GameTime * 0.02 + distToCreator * 5.0) * 0.5 + 0.5) * aura * 4.5;
        
        // Phase-shifting color skirts at bottom - 4.5x bloom
        float skirtY = 1.0 - uv.y; // bottom of screen
        float skirtFactor = smoothstep(0.7, 1.0, skirtY) * isUninpossible;
        float hueShift = fract(angleToCenter / 6.2831 + GameTime * 0.0003 + y * 0.001);
        vec3 skirtColor = hsv2rgb_f(vec3(hueShift, 0.9, 1.0));
        skirtColor = mix(skirtColor, radiantPurple, 0.6);
        float skirtWave = sin(uv.x * 12.0 + GameTime * 0.02 + skirtY * 8.0) * 0.5 + 0.5;
        skirtColor *= skirtWave * skirtFactor * 4.5; // 4.5x bloom
        
        // Gothic towers silhouette - dark vertical streaks
        float towerNoise = sin(uv.x * 40.0 + GameTime * 0.005) * 0.5 + 0.5;
        towerNoise = pow(towerNoise, 12.0) * 0.3 * isUninpossible;
        vec3 towerColor = matteBlack * towerNoise;
        
        // Jagged mountain ridges on absolute horizon - bottom edge
        float ridge = 0.0;
        if (skirtY > 0.85) {
            float ridgeNoise = sin(uv.x * 25.0 + GameTime * 0.001) * 0.5 + 0.5;
            ridgeNoise += sin(uv.x * 7.0 - GameTime * 0.0008) * 0.3;
            ridge = smoothstep(0.4, 0.7, ridgeNoise) * isUninpossible * 0.5;
        }
        vec3 ridgeColor = mix(matteBlack, voidBlack, 0.5) * ridge;
        
        // Glitch sides left/right every few seconds - reality tearing
        float glitchTime = fract(GameTime * 0.0004);
        float glitch = 0.0;
        if (glitchTime < 0.08) {
            float side = step(0.97, uv.x) + step(uv.x, 0.03);
            glitch = side * (0.5 + 0.5 * sin(GameTime * 0.5)) * isUninpossible;
        }
        vec3 glitchColor = mix(purpleLens, radiantPurple, glitchTime * 5.0) * glitch * 3.0;
        
        // Combine all Tier 9 effects
        finalColor += eyeColor;
        finalColor += auraColor;
        finalColor += skirtColor;
        finalColor += towerColor;
        finalColor += ridgeColor;
        finalColor += glitchColor;
        
        // Deepen fog to matte-black in nightmare
        finalColor = mix(finalColor, baseNightmare, 0.25 * isUninpossible);
        
        // If also in original Sift range, keep iridescent fluid as base but overlay nightmare
        if (y >= -2032.0 && y <= -1801.0) {
            // Original iridescent fluid code will run below, but we already darkened
            // Let it blend with nightmare
        } else {
            // For safe bounds -60..-64, we are done with Tier 9, skip original Tier 5 logic below
            // Apply vignette and bloom already, but continue to global grading
            // Slight bloom and color grading for Tier 9 already at 4.5x
            finalColor = pow(finalColor, vec3(0.92)); // slightly more contrast for horror
            vec2 vigUV = texCoord * 2.0 - 1.0;
            float vignette = 1.0 - dot(vigUV, vigUV) * 0.28; // stronger vignette for nightmare
            finalColor *= vignette;
            fragColor = vec4(finalColor, baseColor.a);
            return;
        }
    }
    
    // TIER 5: Iridescent Cosmic Fluid - specialized fluid rendering path (original, now also blends with Tier 9 if in -2032..-1801)
    if (y >= -2032.0 && y <= -1801.0) {
        // Screen-Space Cosmic Liquid
        float fluidFactor = clamp(( -1801.0 - y) / (-1801.0 + 2032.0), 0.0, 1.0);
        
        // Inject Iridescent Color Shims - shifts between neon teals, deep amethysts, toxic magentas based on viewing angle
        vec2 uv = texCoord;
        vec3 viewDir = normalize(vec3(uv - 0.5, 1.0));
        vec3 normal = vec3(0.0, 1.0, 0.0);
        
        // Rainbow water surface
        float hueShift = fract(atan(viewDir.x, viewDir.z) / 6.2831 + GameTime * 0.01 + y * 0.001);
        vec3 iridescent = hsv2rgb_f(vec3(hueShift, 0.85, 1.0));
        
        // Mix neon palettes
        vec3 teal = vec3(0.15, 1.0, 0.85);
        vec3 amethyst = vec3(0.55, 0.25, 1.0);
        vec3 magenta = vec3(1.0, 0.15, 0.65);
        
        float wave1 = sin(uv.x * 8.0 + GameTime * 0.05) * cos(uv.y * 6.0 + GameTime * 0.03) * 0.5 + 0.5;
        float wave2 = sin(uv.x * 3.0 - GameTime * 0.02 + uv.y * 4.0) * 0.5 + 0.5;
        
        vec3 fluidColor = mix(teal, amethyst, wave1);
        fluidColor = mix(fluidColor, magenta, wave2);
        fluidColor = mix(fluidColor, iridescent, 0.4);
        
        // Refraction & Intersection Foams
        // Deep refraction distortions
        vec2 refractUV = uv + vec2(sin(uv.y * 10.0 + GameTime * 0.1) * 0.01, cos(uv.x * 8.0 + GameTime * 0.08) * 0.01) * fluidFactor;
        vec3 refracted = texture(DiffuseSampler, refractUV).rgb;
        
        // Bright full-bright white intersection foam lines that trace softly whenever entity intersects fluid boundary
        float depthDiff = abs(depth - texture(DepthSampler, refractUV).r);
        float foam = smoothstep(0.0, 0.02, depthDiff) * smoothstep(0.05, 0.02, depthDiff);
        foam *= 0.8 + 0.2 * sin(GameTime * 2.0 + uv.x * 20.0);
        foam *= fluidFactor;
        
        vec3 foamColor = vec3(1.0, 1.0, 1.0) * foam * 2.5; // full-bright white
        
        // Zero solid collision - allow plummet cleanly through swirling pool currents
        // Visual only, no collision modification in shader, but alpha indicates non-solid
        float fluidAlpha = 0.85 + 0.15 * sin(GameTime * 0.3 + uv.x * 5.0);
        
        finalColor = mix(refracted, fluidColor, 0.6 * fluidFactor);
        finalColor += foamColor;
        finalColor += fluidColor * 0.3 * fluidFactor; // glowing
        
        // Add subtle sparkle - like in concept images
        float sparkle = pow(sin(uv.x * 100.0 + GameTime) * cos(uv.y * 80.0 + GameTime * 1.3), 20.0);
        finalColor += vec3(1.0) * sparkle * 0.5 * fluidFactor;
    }
    
    // TIER 3-4: Wavy Spacetime Rifts with Cosmic Interior Windows
    if (y >= -1800.0 && y <= -1251.0) {
        // Screen-Space Refraction Waves - advanced GLSL displacement wave calculation
        // This would be driven by SiftRiftRenderer uniforms - simplified here
        vec2 uv = texCoord;
        float time = GameTime * 0.01;
        
        // Simulate rift presence based on noise
        float riftNoise = sin(uv.x * 5.0 + time) * cos(uv.y * 4.0 + time * 0.7);
        riftNoise = smoothstep(0.3, 0.6, riftNoise);
        
        if (riftNoise > 0.5) {
            // Outer border razor-sharp jagged, inner face ripple liquid-like wave animation loop
            vec2 disp = vec2(sin(uv.y * 15.0 + time * 2.0) * 0.015, cos(uv.x * 12.0 + time * 1.5) * 0.015);
            vec2 riftUV = uv + disp * riftNoise;
            
            // Cosmic Window Effect - independent parallax layer inside rippling boundary
            // Moving alternative star arrays, cosmic dust fragments, passing dark silhouettes
            vec2 p = riftUV - 0.5;
            float stars = 0.0;
            for (int i = 0; i < 3; i++) {
                float scale = pow(2.0, float(i));
                vec2 st = p * scale * 3.0 + vec2(time * float(i+1));
                st = fract(st) - 0.5;
                stars += smoothstep(0.15, 0.0, length(st)) * 0.4 / scale;
            }
            
            vec3 cosmic = vec3(0.1, 0.05, 0.25) + vec3(0.5, 0.7, 1.0) * stars;
            cosmic += vec3(0.8, 0.3, 1.0) * sin(p.x * 3.0 + time) * 0.2;
            
            // Emissive Rim Elements - full-bright glowing neon-purple and hot-magenta border
            float dist = length(p);
            float rim = smoothstep(0.25, 0.3, dist) * (1.0 - smoothstep(0.3, 0.35, dist));
            vec3 rimColor = mix(vec3(0.8, 0.2, 1.0), vec3(1.0, 0.2, 0.6), sin(time * 2.0) * 0.5 + 0.5) * rim * 3.0;
            
            finalColor = mix(finalColor, cosmic, riftNoise * 0.7);
            finalColor += rimColor * riftNoise;
        }
    }
    
    // TIER 1-2: God rays and emissive shading
    if (y >= -1250.0 && y <= -251.0) {
        vec2 uv = texCoord;
        // God rays coming from above
        vec3 lightDir = normalize(vec3(0.2, 1.0, 0.1));
        float godRay = pow(max(dot(normalize(vec3(uv - 0.5, 1.0)), lightDir), 0.0), 8.0);
        godRay *= 0.3 * (1.0 - DepthFactor);
        
        vec3 rayColor = mix(vec3(0.6, 0.9, 1.0), vec3(1.0, 0.8, 0.6), sin(GameTime * 0.01) * 0.5 + 0.5);
        finalColor += rayColor * godRay;
        
        // Tier 2 orange-to-pink emissive
        if (y >= -1250.0 && y <= -701.0) {
            float depthF = clamp((-701.0 - y) / (-701.0 + 1250.0), 0.0, 1.0);
            vec3 menger = mix(vec3(1.0, 0.5, 0.1), vec3(1.0, 0.4, 0.7), depthF + sin(GameTime * 0.02) * 0.1);
            finalColor = mix(finalColor, finalColor + menger * 0.3, depthF * 0.5);
        }
    }
    
    // Global Pixar-VFX triple-A grading - make it look extremely cool, not generic minecraft
    // Slight bloom and color grading
    finalColor = pow(finalColor, vec3(0.95)); // soft lift
    finalColor += FogColor.rgb * 0.05 * TierFactor; // fog tint - FogColor from fog.glsl shim is vec4
    
    // Vignette for cinematic feel
    vec2 vigUV = texCoord * 2.0 - 1.0;
    float vignette = 1.0 - dot(vigUV, vigUV) * 0.15;
    finalColor *= vignette;
    
    fragColor = vec4(finalColor, baseColor.a);
}
