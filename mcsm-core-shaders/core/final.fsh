#version 330

// final.fsh - Iridescent Cosmic Fluid Shaders & Glowing Water Pools
// Build #482 - Infinite Sift Cosmos + V2 Black Hole Backdrop
// Screen-Space Cosmic Liquid for Layer 5 gel void bounds (-1801 to -2032)
// Iridescent color shims, refraction & intersection foams
// V2: Black hole backdrop dynamic lensing centered middle growing bigger perspective interactive enterable
// Validation: black hole lensing, photon ring, accretion disk rainbow, chromatic aberration, full sky not bands

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform float GameTime;
uniform vec3 PlayerPos;
uniform int LoopCount;
uniform vec3 FogColor;

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
    
    // TIER 5: Iridescent Cosmic Fluid - specialized fluid rendering path
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
    
    // ========================================================================
    // V2 - BLACK HOLE BACKDROP DYNAMIC LENSING - real distortion lensing
    // Centered middle growing bigger perspective interactive enterable
    // ========================================================================
    {
        vec2 uv = texCoord;
        vec2 center = vec2(0.5, 0.5); // centered middle
        vec2 toCenter = uv - center;
        float dist = length(toCenter);
        
        // Growing bigger perspective - size increases with GameTime
        float bhTime = GameTime * 0.0005;
        float bhSize = 0.15 + fract(bhTime) * 0.25 + sin(GameTime * 0.001) * 0.02; // 0.15 to 0.4 growing
        float bhInner = bhSize * 0.35;
        float bhPhoton = bhSize * 0.42;
        float bhDisk = bhSize * 0.9;
        float bhLensing = bhSize * 1.6;

        // Only in void/sift dimensions (IsInSift) or always subtle
        float bhFactor = IsInSift > 0.5 ? 1.0 : 0.25;

        if (dist < bhLensing && bhFactor > 0.1) {
            // Real distortion lensing - gravitational lensing bends background
            float lensStrength = 0.12 / (0.08 + dist * 3.0);
            // Einstein ring distortion
            vec2 lensedUV = uv + normalize(toCenter) * lensStrength * 0.03 * sin(dist * 30.0 - GameTime * 0.02) * bhFactor;
            // Sample lensed background
            vec3 lensedColor = texture(DiffuseSampler, lensedUV).rgb;
            float lensBlend = smoothstep(bhDisk, bhDisk * 0.6, dist) * 0.5;
            finalColor = mix(finalColor, lensedColor * 1.15, lensBlend * bhFactor);

            // Event horizon - pitch black, interactive enterable
            if (dist < bhInner) {
                float horizonFade = smoothstep(bhInner, bhInner * 0.7, dist);
                vec3 black = vec3(0.01, 0.005, 0.02);
                // Enterable glow pulse
                float enterPulse = pow(1.0 - dist / bhInner, 3.0) * (0.8 + sin(GameTime * 0.01) * 0.2);
                black += vec3(0.5, 0.15, 0.9) * enterPulse * 0.4;
                finalColor = mix(finalColor, black, (1.0 - horizonFade * 0.2) * bhFactor);
            }
            // Photon ring - bright
            else if (dist < bhPhoton) {
                float ring = smoothstep(bhInner, bhPhoton, dist) * (1.0 - smoothstep(bhPhoton, bhPhoton * 1.08, dist));
                vec3 photon = vec3(1.0, 0.92, 0.7) * ring * 3.0;
                // Rainbow shifting
                float hue = fract(GameTime * 0.0005 + dist * 2.0);
                vec3 rainbow = hsv2rgb_f(vec3(hue, 0.9, 1.0)) * ring * 1.8;
                photon = mix(photon, rainbow, 0.6);
                finalColor += photon * bhFactor;
            }
            // Accretion disk - rainbow rotating
            else if (dist < bhDisk) {
                float diskT = (dist - bhPhoton) / (bhDisk - bhPhoton);
                float angle = atan(toCenter.y, toCenter.x) + GameTime * 0.002 * (1.5 - diskT);
                float pattern = sin(angle * 4.0 + diskT * 12.0) * 0.5 + 0.5;
                pattern *= sin(angle * 9.0 - GameTime * 0.005) * 0.3 + 0.7;
                float hue = fract(angle / 6.2831 + GameTime * 0.0003 + diskT * 0.2);
                vec3 diskCol = hsv2rgb_f(vec3(hue, 0.85, 1.0)) * (1.0 - diskT * 0.4) * (0.7 + pattern * 0.5);
                float diskAlpha = (1.0 - diskT) * 0.7 * smoothstep(bhPhoton, bhPhoton * 1.15, dist);
                finalColor = mix(finalColor, finalColor + diskCol * 1.3, diskAlpha * bhFactor);
            }

            // Chromatic aberration from lensing - majestic VFX
            if (dist < bhLensing * 0.9 && dist > bhInner) {
                float chroma = (bhLensing - dist) / bhLensing * 0.015 * bhFactor;
                float r = texture(DiffuseSampler, uv + vec2(chroma, 0)).r;
                float b = texture(DiffuseSampler, uv - vec2(chroma, 0)).b;
                finalColor.r = mix(finalColor.r, r, 0.3 * bhFactor);
                finalColor.b = mix(finalColor.b, b, 0.3 * bhFactor);
            }
        }

        // Second black hole at bottom for Sift - bottom fabric skybox similar
        vec2 bottomCenter = vec2(0.5, 0.15);
        vec2 toBottom = uv - bottomCenter;
        float bottomDist = length(toBottom);
        float bottomSize = bhSize * 0.6;
        if (bottomDist < bottomSize * 1.3 && IsInSift > 0.5) {
            float bottomFade = 1.0 - smoothstep(bottomSize * 0.4, bottomSize * 1.3, bottomDist);
            vec3 bottomCol = vec3(0.12, 0.04, 0.22) * bottomFade;
            bottomCol += vec3(0.7, 0.25, 0.55) * bottomFade * 0.3 * (sin(GameTime * 0.005 + bottomDist * 15.0) * 0.5 + 0.5);
            finalColor = mix(finalColor, finalColor + bottomCol, bottomFade * 0.5);
        }
    }

    // Animated skyboxes - majestic insane VFX - hue shift over time
    {
        float animTime = GameTime * 0.0008;
        float hueShift = sin(animTime) * 0.03;
        finalColor.r += hueShift * 0.08;
        finalColor.b -= hueShift * 0.04;
        // Iridescent shimmer for full sky not bands
        vec2 uv = texCoord;
        float shimmer = sin(uv.x * 12.0 + animTime * 3.0) * cos(uv.y * 10.0 + animTime * 2.5) * 0.02;
        finalColor += vec3(shimmer * 0.5, shimmer * 0.8, shimmer) * TierFactor;
    }

    // Global Pixar-VFX triple-A grading - make it look extremely cool, not generic minecraft
    finalColor = pow(finalColor, vec3(0.95));
    finalColor += FogColor * 0.05 * TierFactor;
    
    vec2 vigUV = texCoord * 2.0 - 1.0;
    float vignette = 1.0 - dot(vigUV, vigUV) * 0.15;
    finalColor *= vignette;
    
    fragColor = vec4(finalColor, baseColor.a);
}
