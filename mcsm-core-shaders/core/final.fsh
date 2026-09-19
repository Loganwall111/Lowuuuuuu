#version 150
#moj_import <minecraft:fog.glsl>

// final.fsh - Iridescent Cosmic Fluid Shaders & Glowing Water Pools + Reality-Glitch Nightmare
// Build #493 / 7000.0.21-M - Procedural Void-Glitch Generator & Reality Mutation Engine
// Phase 2: Screen-Space Refraction Waves inside final.fsh – tie chromatic aberration and glitch-scanline filters to procedural spikes,
// flash localized white-emissive rim-light on major warp to simulate HUD breaking
// Phase 2: Blinding Gaze Warp – sync colossal Creator face to glitch noise, flare purple emissive eyes to 6.0x bloom + glitch particles

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

// BUILD #493: High-frequency sine/cosine noise matrix for glitch
float glitchNoise(vec2 uv, float time) {
    float n = 0.0;
    n += sin(uv.x * 10.0 + time * 1.3) * cos(uv.y * 7.0 + time * 0.9) * 0.5;
    n += sin(uv.x * 23.0 - uv.y * 11.0 + time * 2.3) * 0.25;
    n += sin(uv.x * 47.0 + uv.y * 23.0 - time * 4.7) * 0.125;
    n += sin(uv.x * 91.0 - uv.y * 37.0 + time * 9.1) * 0.0625;
    return n;
}

float computeGlitchFactor(float playerY, float time, float fallDist) {
    float depthFactor = 0.0;
    if (playerY <= -2032.0) depthFactor = 1.0;
    else if (playerY <= -1200.0) depthFactor = 0.85 + 0.15 * ((-1200.0 - playerY) / (-1200.0 + 2032.0));
    else if (playerY <= -500.0) depthFactor = 0.55 + 0.30 * ((-500.0 - playerY) / (-500.0 + 1200.0));
    else if (playerY <= -180.0) depthFactor = 0.25 + 0.30 * ((-180.0 - playerY) / (-180.0 + 500.0));
    else if (playerY <= -60.0) depthFactor = 0.05 + 0.20 * ((-60.0 - playerY) / (-60.0 + 180.0));
    else depthFactor = 0.0;

    float fallFactor = clamp(fallDist / 100.0, 0.0, 1.0);

    float noise = 0.0;
    noise += sin(time * 1.0) * 0.5;
    noise += sin(time * 2.3 + fallDist * 0.01) * 0.25;
    noise += sin(time * 4.7 + playerY * 0.005) * 0.125;
    noise += sin(time * 9.1 + fallDist * 0.02) * 0.0625;
    noise = (noise + 1.0) * 0.5;

    float glitch = depthFactor * (0.6 + 0.4 * fallFactor) * (0.5 + 0.5 * noise);

    float spikePhase = time * 0.003;
    float spike = 0.0;
    if (sin(spikePhase) > 0.85) {
        spike = (sin(spikePhase) - 0.85) / 0.15;
    }

    return clamp(glitch + spike * depthFactor * 0.8, 0.0, 1.0);
}

void main() {
    vec4 baseColor = texture(DiffuseSampler, texCoord);
    float depth = texture(DepthSampler, texCoord).r;
    
    if (IsInSift < 0.5) {
        fragColor = baseColor;
        return;
    }
    
    vec3 finalColor = baseColor.rgb;
    float y = PlayerPos.y;
    float time = GameTime * 0.01;
    float timeFast = GameTime * 0.05;
    
    // BUILD #493: Compute glitch factor from GameTime + FallDistance (simulated via LoopCount/time) + playerY
    float fallDistSim = float(LoopCount % 1000) * 0.1 + abs(sin(GameTime * 0.01)) * 20.0;
    float glitchFactor = computeGlitchFactor(y, timeFast, fallDistSim);
    float isMajorWarp = step(0.75, glitchFactor);
    
    // BUILD #485-486 + 493 -- TIER 9: Reality-Glitch Nightmare / Uninpossible Layer - PHOTOREALISTIC + PROCEDURAL GLITCH
    if ((y >= -2032.0 && y <= -1801.0) || (y <= -60.0 && y >= -64.0)) {
        vec2 uv = texCoord;
        
        float isUninpossible = 0.0;
        if (y <= -62.0) isUninpossible = 1.0;
        else if (y <= -60.0) isUninpossible = clamp((-60.0 - y) / 2.0, 0.0, 1.0);
        else isUninpossible = clamp((-1801.0 - y) / 231.0, 0.0, 1.0);
        
        vec3 matteBlack = vec3(0.0392, 0.0549, 0.0784);
        vec3 voidBlack = vec3(0.0, 0.0, 0.0);
        vec3 baseNightmare = mix(matteBlack, voidBlack, isUninpossible);
        
        float depthFog = clamp((y + 64.0) / -4.0, 0.0, 1.0);
        float volumetricFog = exp(-depth * 2.5) * isUninpossible * 0.6;
        vec3 fogColor = mix(matteBlack, vec3(0.15, 0.1, 0.25), depthFog * 0.5);
        
        float luminance = dot(finalColor, vec3(0.299, 0.587, 0.114));
        vec3 darkened = mix(finalColor * 0.15, baseNightmare, 0.85 * isUninpossible);
        finalColor = mix(finalColor, darkened, 0.7 * isUninpossible);
        finalColor = mix(finalColor, fogColor, volumetricFog * 0.4);
        
        // ---- BUILD #493: Screen-Space Refraction Waves tied to glitch spikes ----
        // Refraction wave intensity based on glitchFactor
        vec2 refractionWave = vec2(0.0);
        if (glitchFactor > 0.2) {
            refractionWave.x = sin(uv.y * 20.0 + GameTime * 0.1 * (1.0 + glitchFactor * 3.0)) * 0.005 * glitchFactor;
            refractionWave.y = cos(uv.x * 15.0 + GameTime * 0.08 * (1.0 + glitchFactor * 3.0)) * 0.005 * glitchFactor;
            refractionWave += vec2(glitchNoise(uv, GameTime * 0.02), glitchNoise(uv + 1.0, GameTime * 0.02)) * 0.003 * glitchFactor;
        }
        vec2 refractedUV = uv + refractionWave;
        vec3 refractedColor = texture(DiffuseSampler, refractedUV).rgb;
        finalColor = mix(finalColor, refractedColor, glitchFactor * 0.3 * isUninpossible);
        
        vec2 center = uv - 0.5;
        float distToCenter = length(center);
        float angleToCenter = atan(center.y, center.x);
        
        vec2 creatorPos = vec2(0.0 + sin(GameTime * 0.0005) * 0.08, -0.25 + cos(GameTime * 0.0004) * 0.05);
        // Warp creator pos with glitch
        creatorPos += vec2(glitchNoise(uv, GameTime * 0.005), glitchNoise(uv + 2.0, GameTime * 0.005)) * glitchFactor * 0.05;
        float distToCreator = length(uv - creatorPos - 0.5);
        
        vec3 purpleLens = vec3(0.541, 0.168, 0.886);
        float eyeGlow = 0.0;
        vec2 leftEye = creatorPos + vec2(-0.06, 0.02) + vec2(sin(GameTime * 0.001 + 1.0) * 0.02, cos(GameTime * 0.001 + 0.5) * 0.01);
        vec2 rightEye = creatorPos + vec2(0.06, 0.02) + vec2(sin(GameTime * 0.001 + 1.0) * 0.02, cos(GameTime * 0.001 + 0.5) * 0.01);
        leftEye += vec2(glitchNoise(leftEye, GameTime * 0.01), glitchNoise(rightEye, GameTime * 0.01)) * glitchFactor * 0.02;
        rightEye += vec2(glitchNoise(rightEye, GameTime * 0.01), glitchNoise(leftEye + 1.0, GameTime * 0.01)) * glitchFactor * 0.02;
        float leftDist = length(uv - leftEye - 0.5);
        float rightDist = length(uv - rightEye - 0.5);
        float leftIris = sin(leftDist * 80.0 + GameTime * 0.1) * 0.5 + 0.5;
        float rightIris = sin(rightDist * 80.0 + GameTime * 0.1) * 0.5 + 0.5;
        eyeGlow += exp(-leftDist * 35.0) * 2.5 * (0.8 + leftIris * 0.4);
        eyeGlow += exp(-rightDist * 35.0) * 2.5 * (0.8 + rightIris * 0.4);
        // BUILD #493: Blinding Gaze Warp – flare purple emissive eyes to 6.0x bloom + glitch particles
        float bloomFactor = mix(4.5, 6.0, clamp((glitchFactor - 0.7) / 0.3, 0.0, 1.0));
        bloomFactor += sin(GameTime * 0.1) * step(0.7, glitchFactor) * 0.5;
        eyeGlow *= bloomFactor;
        eyeGlow *= isUninpossible;
        
        vec3 eyeColor = purpleLens * eyeGlow;
        float leftPupil = smoothstep(0.025, 0.015, leftDist);
        float rightPupil = smoothstep(0.025, 0.015, rightDist);
        eyeColor *= (1.0 - leftPupil * 0.8);
        eyeColor *= (1.0 - rightPupil * 0.8);
        float leftSpec = exp(-length(uv - leftEye - 0.5 - vec2(0.01, -0.01)) * 120.0) * 3.0;
        float rightSpec = exp(-length(uv - rightEye - 0.5 - vec2(0.01, -0.01)) * 120.0) * 3.0;
        eyeColor += vec3(1.0) * (leftSpec + rightSpec) * isUninpossible;
        // Extra glitch flare on major warp
        if (glitchFactor > 0.75) {
            float flare = exp(-leftDist * 15.0) + exp(-rightDist * 15.0);
            flare *= glitchFactor * isMajorWarp * 2.0;
            eyeColor += vec3(1.0, 0.8, 1.0) * flare;
        }
        
        float aura = exp(-distToCreator * 3.5) * 0.6;
        aura *= isUninpossible;
        float godRay = pow(max(dot(normalize(vec3(center, 1.0)), vec3(0.0, 0.0, 1.0)), 0.0), 4.0) * aura;
        vec3 radiantPurple = vec3(0.615, 0.0, 1.0);
        vec3 auraColor = mix(purpleLens, radiantPurple, sin(GameTime * 0.02 + distToCreator * 5.0) * 0.5 + 0.5) * aura * bloomFactor;
        auraColor += radiantPurple * godRay * 0.8;
        auraColor += radiantPurple * glitchNoise(uv, GameTime * 0.01) * glitchFactor * 0.3 * isUninpossible;
        
        float skirtY = 1.0 - uv.y;
        float skirtFactor = smoothstep(0.7, 1.0, skirtY) * isUninpossible;
        float hueShift = fract(angleToCenter / 6.2831 + GameTime * 0.0003 + y * 0.001);
        vec3 skirtColor = hsv2rgb_f(vec3(hueShift, 0.9, 1.0));
        skirtColor = mix(skirtColor, radiantPurple, 0.6);
        float skirtWave = sin(uv.x * 12.0 + GameTime * 0.02 + skirtY * 8.0) * 0.5 + 0.5;
        skirtWave += glitchNoise(uv, GameTime * 0.005) * glitchFactor * 0.5;
        float fabricDetail = sin(uv.x * 80.0) * sin(uv.y * 80.0) * 0.1 + 0.9;
        skirtColor *= skirtWave * skirtFactor * bloomFactor * fabricDetail;
        
        float towerNoise = sin(uv.x * 40.0 + GameTime * 0.005) * 0.5 + 0.5;
        towerNoise = pow(towerNoise, 12.0) * 0.3 * isUninpossible;
        vec3 towerColor = matteBlack * towerNoise;
        float windowLight = 0.0;
        for (int i = 0; i < 3; i++) {
            vec2 winPos = vec2(fract(uv.x * 20.0 + float(i) * 1.3), fract(uv.y * 30.0 + float(i) * 0.7));
            float winDist = length(winPos - 0.5);
            windowLight += exp(-winDist * 15.0) * 0.3 * step(0.7, sin(uv.x * 40.0 + float(i)));
        }
        towerColor += vec3(1.0, 0.8, 0.4) * windowLight * isUninpossible * 0.8;
        
        float ridge = 0.0;
        if (skirtY > 0.85) {
            float ridgeNoise = sin(uv.x * 25.0 + GameTime * 0.001) * 0.5 + 0.5;
            ridgeNoise += sin(uv.x * 7.0 - GameTime * 0.0008) * 0.3;
            ridge = smoothstep(0.4, 0.7, ridgeNoise) * isUninpossible * 0.5;
        }
        vec3 ridgeColor = mix(matteBlack, voidBlack, 0.5) * ridge;
        float snowCap = smoothstep(0.9, 1.0, skirtY) * ridge * isUninpossible;
        ridgeColor += vec3(0.9, 0.95, 1.0) * snowCap * 0.6;
        
        // ---- BUILD #493: Chromatic aberration tied to procedural spikes ----
        float chromaIntensity = 0.002 * (1.0 + glitchFactor * 5.0) * isUninpossible;
        vec2 chromaOffset = vec2(chromaIntensity, 0.0);
        // On major warp, intense chromatic aberration
        if (glitchFactor > 0.5) {
            chromaOffset *= (1.0 + glitchFactor * 2.0 + sin(GameTime * 0.1) * glitchFactor);
            vec3 chromaR = texture(DiffuseSampler, uv + chromaOffset + refractionWave).rgb;
            vec3 chromaB = texture(DiffuseSampler, uv - chromaOffset - refractionWave).rgb;
            finalColor.r = mix(finalColor.r, chromaR.r, glitchFactor * 0.6);
            finalColor.b = mix(finalColor.b, chromaB.b, glitchFactor * 0.6);
            finalColor.g = mix(finalColor.g, texture(DiffuseSampler, uv + refractionWave * 0.5).g, glitchFactor * 0.2);
        }
        
        // ---- BUILD #493: Glitch-scanline filters tied to procedural spikes ----
        float scanline = 0.0;
        float scanlineTime = fract(GameTime * 0.01 + glitchFactor * 2.0);
        if (glitchFactor > 0.3) {
            // Horizontal scanlines that tear
            float line = sin(uv.y * 800.0 + GameTime * 0.5 * glitchFactor) * 0.5 + 0.5;
            line = pow(line, 50.0) * glitchFactor;
            scanline = line * isUninpossible;
            
            // Random horizontal displacement per scanline
            float displacement = glitchNoise(vec2(uv.y * 10.0, GameTime * 0.01), GameTime) * glitchFactor * 0.02;
            vec3 displaced = texture(DiffuseSampler, vec2(uv.x + displacement, uv.y)).rgb;
            finalColor = mix(finalColor, displaced, scanline * 0.5);
        }
        
        // Glitch sides left/right every few seconds – enhanced with glitch factor
        float glitchTime = fract(GameTime * 0.0004 + glitchFactor * 0.1);
        float glitch = 0.0;
        if (glitchTime < 0.08 + glitchFactor * 0.1) {
            float side = step(0.97 - glitchFactor * 0.05, uv.x) + step(uv.x, 0.03 + glitchFactor * 0.05);
            glitch = side * (0.5 + 0.5 * sin(GameTime * 0.5 + glitchFactor * 10.0)) * isUninpossible;
            glitch *= (1.0 + glitchFactor * 2.0);
        }
        vec3 glitchColor = mix(purpleLens, radiantPurple, glitchTime * 5.0) * glitch * 3.0;
        glitchColor += vec3(glitchNoise(uv, GameTime * 0.1)) * glitch * glitchFactor * 2.0;
        
        // ---- BUILD #493: White-emissive rim-light flash on major warp – HUD breaking ----
        vec3 rimFlash = vec3(0.0);
        if (isMajorWarp > 0.5) {
            // Localized white flash at edges and around eyes on violent mutation
            float edge = smoothstep(0.8, 1.0, abs(uv.x - 0.5) * 2.0) + smoothstep(0.8, 1.0, abs(uv.y - 0.5) * 2.0);
            edge *= isMajorWarp * glitchFactor;
            float eyeRim = exp(-leftDist * 20.0) + exp(-rightDist * 20.0);
            eyeRim *= isMajorWarp * 2.0;
            
            // White flash that simulates HUD breaking
            float flashPulse = sin(GameTime * 0.3 + glitchFactor * 10.0) * 0.5 + 0.5;
            flashPulse = pow(flashPulse, 3.0) * isMajorWarp;
            
            rimFlash = vec3(1.0) * (edge * 0.6 + eyeRim * 1.2) * flashPulse;
            rimFlash += vec3(1.0, 1.0, 0.9) * scanline * isMajorWarp * 2.0;
        }
        
        float grain = fract(sin(dot(uv * GameTime, vec2(12.9898, 78.233))) * 43758.5453) * 0.015 - 0.0075;
        grain *= isUninpossible;
        grain *= (1.0 + glitchFactor * 2.0);
        
        float dof = clamp(length(center) * 0.5, 0.0, 1.0) * isUninpossible * 0.15;
        
        finalColor += eyeColor;
        finalColor += auraColor;
        finalColor += skirtColor;
        finalColor += towerColor;
        finalColor += ridgeColor;
        finalColor += glitchColor;
        finalColor += rimFlash;
        finalColor += grain;
        finalColor = mix(finalColor, finalColor * (1.0 - dof) + texture(DiffuseSampler, uv + vec2(dof * 0.001) + refractionWave * 0.5).rgb * dof, dof * 0.3);
        
        finalColor = mix(finalColor, baseNightmare, 0.25 * isUninpossible + volumetricFog * 0.3);
        
        if (y >= -2032.0 && y <= -1801.0) {
        } else {
            finalColor = pow(finalColor, vec3(0.92));
            finalColor = mix(finalColor, finalColor * vec3(1.05, 1.02, 0.98), 0.15);
            finalColor = finalColor * 1.1 - 0.05;
            finalColor = clamp(finalColor, 0.0, 1.0);
            
            vec2 vigUV = texCoord * 2.0 - 1.0;
            float vignette = 1.0 - dot(vigUV, vigUV) * 0.28;
            vignette = pow(vignette, 1.2);
            // On major warp, vignette pulses white
            if (isMajorWarp > 0.5) {
                vignette *= 1.0 + sin(GameTime * 0.2) * glitchFactor * 0.3;
            }
            finalColor *= vignette;
            
            float bloomLum = dot(finalColor, vec3(0.299, 0.587, 0.114));
            float bloom = smoothstep(0.6, 1.0, bloomLum) * 0.4 * isUninpossible;
            bloom *= (1.0 + glitchFactor * 0.8);
            finalColor += finalColor * bloom;
            finalColor += rimFlash * 0.5;
            
            fragColor = vec4(finalColor, baseColor.a);
            return;
        }
    }
    
    // TIER 5: Iridescent Cosmic Fluid with glitch refraction
    if (y >= -2032.0 && y <= -1801.0) {
        float fluidFactor = clamp(( -1801.0 - y) / (-1801.0 + 2032.0), 0.0, 1.0);
        
        vec2 uv = texCoord;
        vec3 viewDir = normalize(vec3(uv - 0.5, 1.0));
        
        float hueShift = fract(atan(viewDir.x, viewDir.z) / 6.2831 + GameTime * 0.01 + y * 0.001 + glitchFactor * 0.2);
        vec3 iridescent = hsv2rgb_f(vec3(hueShift, 0.85, 1.0));
        
        vec3 teal = vec3(0.15, 1.0, 0.85);
        vec3 amethyst = vec3(0.55, 0.25, 1.0);
        vec3 magenta = vec3(1.0, 0.15, 0.65);
        
        float wave1 = sin(uv.x * 8.0 + GameTime * 0.05 + glitchFactor * 5.0) * cos(uv.y * 6.0 + GameTime * 0.03) * 0.5 + 0.5;
        float wave2 = sin(uv.x * 3.0 - GameTime * 0.02 + uv.y * 4.0 + glitchFactor * 3.0) * 0.5 + 0.5;
        
        vec3 fluidColor = mix(teal, amethyst, wave1);
        fluidColor = mix(fluidColor, magenta, wave2);
        fluidColor = mix(fluidColor, iridescent, 0.4 + glitchFactor * 0.3);
        
        vec2 refractUV = uv + vec2(sin(uv.y * 10.0 + GameTime * 0.1) * 0.01, cos(uv.x * 8.0 + GameTime * 0.08) * 0.01) * fluidFactor;
        refractUV += vec2(glitchNoise(uv, GameTime * 0.01), glitchNoise(uv + 1.0, GameTime * 0.01)) * glitchFactor * 0.01;
        vec3 refracted = texture(DiffuseSampler, refractUV).rgb;
        
        float depthDiff = abs(depth - texture(DepthSampler, refractUV).r);
        float foam = smoothstep(0.0, 0.02, depthDiff) * smoothstep(0.05, 0.02, depthDiff);
        foam *= 0.8 + 0.2 * sin(GameTime * 2.0 + uv.x * 20.0);
        foam *= fluidFactor;
        
        vec3 foamColor = vec3(1.0, 1.0, 1.0) * foam * 2.5;
        // On major warp, foam flashes white-emissive
        foamColor += vec3(1.0) * isMajorWarp * foam * 2.0;
        
        float fluidAlpha = 0.85 + 0.15 * sin(GameTime * 0.3 + uv.x * 5.0);
        
        finalColor = mix(refracted, fluidColor, 0.6 * fluidFactor);
        finalColor += foamColor;
        finalColor += fluidColor * 0.3 * fluidFactor;
        
        float sparkle = pow(sin(uv.x * 100.0 + GameTime + glitchFactor * 20.0) * cos(uv.y * 80.0 + GameTime * 1.3), 20.0);
        finalColor += vec3(1.0) * sparkle * 0.5 * fluidFactor;
    }
    
    if (y >= -1800.0 && y <= -1251.0) {
        vec2 uv = texCoord;
        float time = GameTime * 0.01;
        
        float riftNoise = sin(uv.x * 5.0 + time + glitchFactor * 2.0) * cos(uv.y * 4.0 + time * 0.7);
        riftNoise = smoothstep(0.3, 0.6, riftNoise);
        
        if (riftNoise > 0.5) {
            vec2 disp = vec2(sin(uv.y * 15.0 + time * 2.0) * 0.015, cos(uv.x * 12.0 + time * 1.5) * 0.015);
            disp += vec2(glitchNoise(uv, GameTime * 0.01), glitchNoise(uv + 1.0, GameTime * 0.01)) * glitchFactor * 0.02;
            vec2 riftUV = uv + disp * riftNoise;
            
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
            
            float dist = length(p);
            float rim = smoothstep(0.25, 0.3, dist) * (1.0 - smoothstep(0.3, 0.35, dist));
            vec3 rimColor = mix(vec3(0.8, 0.2, 1.0), vec3(1.0, 0.2, 0.6), sin(time * 2.0) * 0.5 + 0.5) * rim * 3.0;
            rimColor += vec3(1.0) * isMajorWarp * rim * 2.0;
            
            finalColor = mix(finalColor, cosmic, riftNoise * 0.7);
            finalColor += rimColor * riftNoise;
        }
    }
    
    if (y >= -1250.0 && y <= -251.0) {
        vec2 uv = texCoord;
        vec3 lightDir = normalize(vec3(0.2, 1.0, 0.1));
        float godRay = pow(max(dot(normalize(vec3(uv - 0.5, 1.0)), lightDir), 0.0), 8.0);
        godRay *= 0.3 * (1.0 - DepthFactor);
        
        vec3 rayColor = mix(vec3(0.6, 0.9, 1.0), vec3(1.0, 0.8, 0.6), sin(GameTime * 0.01) * 0.5 + 0.5);
        finalColor += rayColor * godRay;
        
        if (y >= -1250.0 && y <= -701.0) {
            float depthF = clamp((-701.0 - y) / (-701.0 + 1250.0), 0.0, 1.0);
            vec3 menger = mix(vec3(1.0, 0.5, 0.1), vec3(1.0, 0.4, 0.7), depthF + sin(GameTime * 0.02) * 0.1);
            finalColor = mix(finalColor, finalColor + menger * 0.3, depthF * 0.5);
        }
    }
    
    finalColor = pow(finalColor, vec3(0.95));
    finalColor += FogColor.rgb * 0.05 * TierFactor;
    
    vec2 vigUV = texCoord * 2.0 - 1.0;
    float vignette = 1.0 - dot(vigUV, vigUV) * 0.15;
    finalColor *= vignette;
    
    fragColor = vec4(finalColor, baseColor.a);
}
