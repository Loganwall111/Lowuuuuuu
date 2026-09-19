#version 150
#moj_import <minecraft:fog.glsl>

// final.fsh - Iridescent Cosmic Fluid Shaders & Glowing Water Pools + Reality-Glitch Nightmare
// BUILD #489 / 7000.0.25-M – V3 Endless Possibilities Update – Continuous Distance-Adaptive Procedural Shader Generator
// - Eliminate static post-processing, inject live noise matrix for infinite screen distortions
// - Backrooms Phase heavy wobbly lens desaturated VHS grain
// - Volumetric Floating Balls Phase screen-space raymarching reflective orbs
// - Photorealism & Space-Time Lens Phase gravitational lensing chromatic aberration DOF
// - Psychedelic Space-Time Matrix kaleidoscopic swirl rainbow waves
// - Distance-Adaptive LERP cross-fades based on depth and Void Rudder acceleration
// - HUD overload sync via McsmHudTerminal forcing orange-gold vortex rings ripple and corrupted code scrawl

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

float glitchNoise(vec2 uv, float time) {
    float n = 0.0;
    n += sin(uv.x * 10.0 + time * 1.3) * cos(uv.y * 7.0 + time * 0.9) * 0.5;
    n += sin(uv.x * 23.0 - uv.y * 11.0 + time * 2.3) * 0.25;
    n += sin(uv.x * 47.0 + uv.y * 23.0 - time * 4.7) * 0.125;
    n += sin(uv.x * 91.0 - uv.y * 37.0 + time * 9.1) * 0.0625;
    // V3 extra octaves for infinite possibilities
    n += sin(uv.x * 173.0 + uv.y * 71.0 - time * 13.7) * 0.03125;
    n += sin(uv.x * 311.0 - uv.y * 151.0 + time * 21.3) * 0.0156;
    return n;
}

float fbmNoise(vec2 uv, float time, int octaves) {
    float value = 0.0;
    float amplitude = 1.0;
    float frequency = 1.0;
    float maxValue = 0.0;
    for (int i = 0; i < 8; i++) {
        if (i >= octaves) break;
        float nx = uv.x * frequency + time * 0.01 * float(i+1);
        float ny = uv.y * frequency + time * 0.008 * float(i+1);
        float n = sin(nx * 1.0 + ny * 0.7) * cos(nx * 0.3 - ny * 1.1) * 0.5;
        n += sin(nx * 2.3 - ny * 1.1) * 0.25;
        n += sin(nx * 4.7 + ny * 2.3) * 0.125;
        value += n * amplitude;
        maxValue += amplitude;
        amplitude *= 0.5;
        frequency *= 2.0;
    }
    return value / maxValue;
}

float computeGlitchFactor(float playerY, float time, float fallDist) {
    float depthFactor = 0.0;
    if (playerY <= -2032.0) depthFactor = 1.0;
    else if (playerY <= -1200.0) depthFactor = 0.85 + 0.15 * ((-1200.0 - playerY) / (-1200.0 + 2032.0));
    else if (playerY <= -500.0) depthFactor = 0.65 + 0.35 * ((-500.0 - playerY) / (-500.0 + 1200.0));
    else if (playerY <= -180.0) depthFactor = 0.35 + 0.35 * ((-180.0 - playerY) / (-180.0 + 500.0));
    else if (playerY <= -60.0) depthFactor = 0.15 + 0.30 * ((-60.0 - playerY) / (-60.0 + 180.0));
    else depthFactor = 0.05;

    float fallFactor = clamp(fallDist / 60.0, 0.0, 1.0);

    float noise = 0.0;
    noise += sin(time * 1.0) * 0.4;
    noise += sin(time * 2.3 + fallDist * 0.02) * 0.25;
    noise += sin(time * 4.7 + playerY * 0.01) * 0.15;
    noise += sin(time * 9.1 + fallDist * 0.04) * 0.1;
    noise += sin(time * 17.3 + playerY * 0.02) * 0.05;
    noise += sin(time * 31.1 + fallDist * 0.08) * 0.05;
    noise = (noise + 1.0) * 0.5;

    float glitch = depthFactor * (0.7 + 0.5 * fallFactor) * (0.6 + 0.6 * noise);

    float spikePhase = time * 0.008;
    float spike = 0.0;
    if (sin(spikePhase) > 0.65) {
        spike = (sin(spikePhase) - 0.65) / 0.35;
    }

    return clamp(glitch + spike * depthFactor * 1.2, 0.0, 1.0);
}

// ---- V3 Four Phases --------------------------------------------------------

// Phase 0: Backrooms Phase – heavy wobbly lens distortion, desaturated VHS film-grain
vec3 backroomsPhase(vec2 uv, float time, float glitchFactor, vec3 baseColor, sampler2D diffuse) {
    vec2 wobblyUV = uv;
    // Heavy wobbly lens distortion
    wobblyUV.x += sin(uv.y * 8.0 + time * 0.5 * (1.0 + glitchFactor * 3.0)) * 0.015 * glitchFactor;
    wobblyUV.y += cos(uv.x * 6.0 + time * 0.4 * (1.0 + glitchFactor * 3.0)) * 0.015 * glitchFactor;
    wobblyUV += vec2(glitchNoise(uv, time * 0.02), glitchNoise(uv + 1.0, time * 0.02)) * 0.01 * glitchFactor;

    vec3 color = texture(diffuse, wobblyUV).rgb;

    // Desaturated – Backrooms yellowish fluorescent
    float lum = dot(color, vec3(0.299, 0.587, 0.114));
    vec3 desat = mix(color, vec3(lum), 0.7 + glitchFactor * 0.2);
    desat = mix(desat, vec3(0.9, 0.85, 0.6), 0.2); // yellowish tint

    // VHS film-grain – heavy
    float grain = fract(sin(dot(uv * time * 0.1, vec2(12.9898, 78.233))) * 43758.5453);
    grain = grain * 0.15 - 0.075;
    grain *= 1.0 + glitchFactor * 2.0;

    // VHS tracking lines
    float tracking = sin(uv.y * 400.0 + time * 2.0 * glitchFactor) * 0.5 + 0.5;
    tracking = pow(tracking, 20.0) * glitchFactor * 0.3;
    float displacement = glitchNoise(vec2(uv.y * 10.0, time * 0.01), time) * glitchFactor * 0.03;
    vec3 displaced = texture(diffuse, vec2(wobblyUV.x + displacement, wobblyUV.y)).rgb;
    desat = mix(desat, displaced, tracking);

    // Vignette heavy for Backrooms
    vec2 vigUV = uv * 2.0 - 1.0;
    float vignette = 1.0 - dot(vigUV, vigUV) * 0.35;
    vignette = pow(vignette, 1.5);
    desat *= vignette;

    return desat + grain;
}

// Phase 1: Volumetric Floating Balls – screen-space raymarching reflective orbs
vec3 floatingBallsPhase(vec2 uv, float time, float glitchFactor, vec3 baseColor, sampler2D diffuse) {
    vec2 p = uv - 0.5;
    p.x *= InSize.x / InSize.y;

    vec3 color = baseColor;

    // Raymarch up to 5 floating reflective orbs
    for (int i = 0; i < 5; i++) {
        float fi = float(i);
        // Orb position – floating, procedural via fBm
        vec2 orbPos = vec2(
            sin(time * 0.0005 * (fi+1.0) + fi * 1.3 + glitchNoise(vec2(fi, 0.0), time * 0.001) * glitchFactor * 2.0) * 0.6,
            cos(time * 0.0004 * (fi+1.2) + fi * 2.1 + glitchNoise(vec2(fi, 1.0), time * 0.001) * glitchFactor * 2.0) * 0.4
        );
        float orbRadius = 0.08 + sin(time * 0.001 + fi) * 0.02 + glitchFactor * 0.05 + fbmNoise(vec2(fi, time * 0.0001), time, 3) * 0.03;

        float dist = length(p - orbPos);
        if (dist < orbRadius * 1.5) {
            // Inside orb influence – distort world behind (refraction)
            float refractionStrength = (1.0 - smoothstep(orbRadius * 0.8, orbRadius * 1.5, dist)) * 0.1 * (1.0 + glitchFactor * 2.0);
            vec2 refractDir = normalize(p - orbPos);
            vec2 refractedUV = uv + refractDir * refractionStrength;
            vec3 refractedColor = texture(diffuse, refractedUV).rgb;

            // Reflective orb surface – Fresnel
            float fresnel = pow(1.0 - clamp(dist / orbRadius, 0.0, 1.0), 3.0);
            float hue = fract(time * 0.0002 + fi * 0.15 + dist * 2.0);
            vec3 orbColor = hsv2rgb_f(vec3(hue, 0.9, 1.0)) * (0.8 + fresnel * 1.5);

            // Blend
            float orbBlend = 1.0 - smoothstep(orbRadius * 0.9, orbRadius * 1.5, dist);
            orbBlend *= 0.7 + glitchFactor * 0.5;

            color = mix(color, mix(refractedColor, orbColor, fresnel * 0.6), orbBlend);

            // Specular highlight
            vec2 lightPos = orbPos + vec2(0.1, -0.1);
            float spec = exp(-length(p - lightPos) * 30.0 / orbRadius) * 2.0;
            color += vec3(1.0) * spec * orbBlend;
        }
    }

    return color;
}

// Phase 2: Photorealism & Space-Time Lens – gravitational lensing, chromatic aberration, DOF
vec3 photorealismPhase(vec2 uv, float time, float glitchFactor, float depth, vec3 baseColor, sampler2D diffuse) {
    vec2 center = uv - 0.5;
    float distToCenter = length(center);

    // Gravitational lensing – bend light around center (simulating black hole / massive object)
    float lensMass = 0.15 + glitchFactor * 0.3 + sin(time * 0.001) * 0.05;
    float lensDist = max(distToCenter, 0.05);
    float bend = lensMass / (lensDist * 2.0 + 0.1) * 0.02 * (1.0 + glitchFactor);
    vec2 lensedUV = uv + normalize(center) * bend * sin(time * 0.002 + distToCenter * 10.0) * glitchFactor;

    // Chromatic aberration – edge
    float chroma = 0.003 * (1.0 + glitchFactor * 4.0) * (0.5 + distToCenter * 1.5);
    vec3 chromaR = texture(diffuse, lensedUV + vec2(chroma, 0.0)).rgb;
    vec3 chromaB = texture(diffuse, lensedUV - vec2(chroma, 0.0)).rgb;
    vec3 chromaG = texture(diffuse, lensedUV).rgb;

    vec3 color = vec3(chromaR.r, chromaG.g, chromaB.b);

    // Dynamic depth-of-field blur – based on depth texture and glitch
    float dofFactor = clamp(length(center) * 0.8 + depth * 0.2, 0.0, 1.0) * 0.15 * (1.0 + glitchFactor * 0.5);
    vec3 blurColor = vec3(0.0);
    float blurSamples = 4.0;
    for (float x = -1.0; x <= 1.0; x += 1.0) {
        for (float y = -1.0; y <= 1.0; y += 1.0) {
            vec2 offset = vec2(x, y) * oneTexel * dofFactor * 100.0;
            blurColor += texture(diffuse, lensedUV + offset).rgb;
        }
    }
    blurColor /= 9.0;
    color = mix(color, blurColor, dofFactor * 0.5);

    // Photorealistic grade – slight contrast and saturation lift
    color = pow(color, vec3(0.95));
    float lum = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(vec3(lum), color, 1.15); // saturation
    color = (color - 0.5) * 1.08 + 0.5; // contrast
    color = clamp(color, 0.0, 1.0);

    return color;
}

// Phase 3: Psychedelic Space-Time Matrix – kaleidoscopic swirl rainbow waves
vec3 psychedelicPhase(vec2 uv, float time, float glitchFactor, vec3 baseColor, sampler2D diffuse) {
    vec2 center = uv - 0.5;
    float dist = length(center);
    float angle = atan(center.y, center.x);

    // Kaleidoscopic – mirror angle
    float kaleidoSegments = 6.0 + floor(glitchFactor * 6.0); // 6-12 segments
    float kaleidoAngle = mod(angle, 6.2831853 / kaleidoSegments);
    kaleidoAngle = abs(kaleidoAngle - 3.14159265 / kaleidoSegments);
    float newAngle = kaleidoAngle + time * 0.0005 * (1.0 + glitchFactor * 2.0) + glitchFactor * sin(time * 0.001 + dist * 5.0) * 0.5;

    vec2 kaleidoUV = vec2(cos(newAngle), sin(newAngle)) * dist + 0.5;
    kaleidoUV += vec2(fbmNoise(uv, time * 0.01, 4), fbmNoise(uv + 1.0, time * 0.01, 4)) * 0.02 * glitchFactor;

    vec3 color = texture(diffuse, kaleidoUV).rgb;

    // Fluid melting rainbow waves – warp rendering vectors
    float wave1 = sin(uv.x * 8.0 + time * 0.05 + glitchFactor * 5.0) * cos(uv.y * 6.0 + time * 0.03) * 0.5 + 0.5;
    float wave2 = sin(uv.x * 3.0 - time * 0.02 + uv.y * 4.0 + glitchFactor * 3.0) * 0.5 + 0.5;
    float hueShift = fract(angle / 6.2831853 + time * 0.001 + dist * 0.5 + wave1 * 0.3 + glitchFactor * 0.2);
    vec3 rainbow = hsv2rgb_f(vec3(hueShift, 0.9 + glitchFactor * 0.1, 1.0));

    color = mix(color, rainbow, 0.5 + glitchFactor * 0.3 + wave2 * 0.2);
    color += rainbow * 0.2 * glitchFactor;

    // Melting effect – vertical smear based on glitch
    float melt = sin(uv.x * 20.0 + time * 0.01 * glitchFactor) * 0.01 * glitchFactor;
    vec3 meltColor = texture(diffuse, vec2(uv.x, uv.y + melt)).rgb;
    color = mix(color, meltColor, glitchFactor * 0.2);

    return color;
}

// HUD Overload Sync – orange-gold vortex rings ripple violently + corrupted code scrawl
vec3 hudOverloadSync(vec2 uv, float time, float glitchFactor, vec3 baseColor) {
    vec3 color = baseColor;
    vec2 center = uv - 0.5;
    float dist = length(center);
    float angle = atan(center.y, center.x);

    // Orange-gold vortex rings – ripple violently when chaotic
    float rippleFactor = 1.0;
    if (glitchFactor > 0.6) {
        rippleFactor = 1.0 + glitchFactor * 3.0 + sin(time * 0.02 + dist * 10.0) * glitchFactor * 2.0;
    }

    int rings = 6;
    float ringIntensity = 0.0;
    vec3 ringColorAccum = vec3(0.0);
    for (int i = 0; i < 6; i++) {
        float fi = float(i);
        float baseRadius = 0.15 + fi * 0.12;
        float radius = baseRadius * rippleFactor + sin(time * 0.003 + fi) * 0.02 * glitchFactor;
        float thickness = 0.015 + glitchFactor * 0.01;

        float ringDist = abs(dist - radius);
        float ring = 1.0 - smoothstep(0.0, thickness, ringDist);
        ring *= 1.0 - smoothstep(0.8, 1.0, dist); // fade at edges
        ring *= glitchFactor * 0.8 + 0.2;

        float hue = 0.08 + fi * 0.02 + sin(time * 0.001 + fi) * 0.02; // orange-gold
        vec3 ringCol = hsv2rgb_f(vec3(hue, 0.9, 1.0)) * ring * 1.5;

        ringColorAccum += ringCol;
        ringIntensity += ring;
    }

    color += ringColorAccum * 0.6 * glitchFactor;

    // Corrupted code scrawl on borders – pseudo-code text noise
    float border = 0.0;
    border += step(0.97, uv.x) + step(uv.x, 0.03);
    border += step(0.97, uv.y) + step(uv.y, 0.03);
    border = clamp(border, 0.0, 1.0);

    if (border > 0.5 && glitchFactor > 0.3) {
        // Generate code-like scrawl – random characters via noise
        float codeNoise = fbmNoise(uv * vec2(20.0, 5.0) + time * 0.01, time, 4);
        float code = step(0.5, fract(codeNoise * 10.0 + time * 0.1)) * border;
        code *= glitchFactor;

        // Greenish code color like matrix, but corrupted orange-gold when high glitch
        vec3 codeColor = mix(vec3(0.0, 1.0, 0.3), vec3(1.0, 0.6, 0.1), glitchFactor);
        color += codeColor * code * 0.8;

        // Extra glitch text displacement
        float textDisp = glitchNoise(vec2(uv.y * 20.0, time * 0.01), time) * glitchFactor * 0.02 * border;
        vec3 displaced = texture(DiffuseSampler, vec2(uv.x + textDisp, uv.y)).rgb;
        color = mix(color, displaced, code * 0.3);
    }

    return color;
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
    
    float fallDistSim = float(LoopCount % 1000) * 0.1 + abs(sin(GameTime * 0.01)) * 20.0 + DepthFactor * 50.0;
    float glitchFactor = computeGlitchFactor(y, timeFast, fallDistSim);
    float isMajorWarp = step(0.75, glitchFactor);
    
    // ---- V3 ENDLESS POSSIBILITIES – Continuous Distance-Adaptive Procedural Shader Generator ----
    // For Y < -60 (void), apply infinite procedural distortions with LERP cross-fades
    if (y <= -60.0) {
        vec2 uv = texCoord;

        // Distance-adaptive – compute depth index based on Y and fall distance (infinite)
        float depthBelow = -60.0 - y; // 0 at -60, infinite as deeper
        float depthIndex = floor(depthBelow / 30.0); // Using V3 spacing 30
        float depthFrac = fract(depthBelow / 30.0);

        // Phase float – continuous infinite, driven by GameTime, FallDistance, player vectors, depth
        float phaseFloat = depthIndex * 0.7 + GameTime * 0.002 * (1.0 + DepthFactor + glitchFactor * 2.0) + fallDistSim * 0.015 + PlayerPos.x * 0.001 + PlayerPos.z * 0.001;
        // Void Rudder acceleration simulation – faster fall = faster phase shift
        float rudderAccel = clamp(fallDistSim / 100.0, 0.0, 1.0) * 2.0;
        phaseFloat += rudderAccel * GameTime * 0.001;

        int currentPhase = int(mod(phaseFloat, 4.0));
        int nextPhase = (currentPhase + 1) % 4;
        float phaseFrac = fract(phaseFloat);

        // LERP cross-fade based on depth and Void Rudder acceleration – no sudden jumps
        float lerpFactor = smoothstep(0.0, 1.0, phaseFrac);
        // Accelerate LERP when rudder accelerating
        lerpFactor = mix(lerpFactor, fract(phaseFloat * (1.0 + rudderAccel)), rudderAccel * 0.3);
        lerpFactor = clamp(lerpFactor, 0.0, 1.0);

        // Compute colors for current and next phase
        vec3 currentColor = finalColor;
        vec3 nextColor = finalColor;

        // Current phase
        if (currentPhase == 0) currentColor = backroomsPhase(uv, GameTime * 0.05, glitchFactor, finalColor, DiffuseSampler);
        else if (currentPhase == 1) currentColor = floatingBallsPhase(uv, GameTime * 0.05, glitchFactor, finalColor, DiffuseSampler);
        else if (currentPhase == 2) currentColor = photorealismPhase(uv, GameTime * 0.05, glitchFactor, depth, finalColor, DiffuseSampler);
        else currentColor = psychedelicPhase(uv, GameTime * 0.05, glitchFactor, finalColor, DiffuseSampler);

        // Next phase
        if (nextPhase == 0) nextColor = backroomsPhase(uv, GameTime * 0.05, glitchFactor, finalColor, DiffuseSampler);
        else if (nextPhase == 1) nextColor = floatingBallsPhase(uv, GameTime * 0.05, glitchFactor, finalColor, DiffuseSampler);
        else if (nextPhase == 2) nextColor = photorealismPhase(uv, GameTime * 0.05, glitchFactor, depth, finalColor, DiffuseSampler);
        else nextColor = psychedelicPhase(uv, GameTime * 0.05, glitchFactor, finalColor, DiffuseSampler);

        // Distance-adaptive LERP
        finalColor = mix(currentColor, nextColor, lerpFactor);

        // Blend with depthFrac for extra smoothness between layers
        float depthLerp = smoothstep(0.0, 1.0, depthFrac);
        // Slight mix with base to avoid too harsh
        finalColor = mix(finalColor, mix(currentColor, nextColor, depthLerp), 0.2 + glitchFactor * 0.3);

        // HUD overload sync – orange-gold vortex rings ripple violently + corrupted code scrawl
        finalColor = hudOverloadSync(uv, GameTime * 0.05, glitchFactor, finalColor);

        // Extra photorealistic for F1 – when HUD hidden, we still get photorealistic but via Java mesh
        // Here we add subtle photorealistic grade when deep
        if (depthBelow > 100.0) {
            float photoFactor = clamp((depthBelow - 100.0) / 500.0, 0.0, 1.0) * 0.3;
            vec3 photo = photorealismPhase(uv, GameTime * 0.05, glitchFactor, depth, finalColor, DiffuseSampler);
            finalColor = mix(finalColor, photo, photoFactor * glitchFactor);
        }
    }
    
    // BUILD #485-486 + 493 + 489 -- TIER 9: Reality-Glitch Nightmare / Uninpossible Layer
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
        
        vec3 darkened = mix(finalColor * 0.15, baseNightmare, 0.85 * isUninpossible);
        finalColor = mix(finalColor, darkened, 0.7 * isUninpossible);
        finalColor = mix(finalColor, fogColor, volumetricFog * 0.4);
        
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
        float angleToCenter = atan(center.y, center.x);
        
        vec2 creatorPos = vec2(0.0 + sin(GameTime * 0.0005) * 0.08, -0.25 + cos(GameTime * 0.0004) * 0.05);
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
        
        float chromaIntensity = 0.002 * (1.0 + glitchFactor * 5.0) * isUninpossible;
        vec2 chromaOffset = vec2(chromaIntensity, 0.0);
        if (glitchFactor > 0.5) {
            chromaOffset *= (1.0 + glitchFactor * 2.0 + sin(GameTime * 0.1) * glitchFactor);
            vec3 chromaR = texture(DiffuseSampler, uv + chromaOffset + refractionWave).rgb;
            vec3 chromaB = texture(DiffuseSampler, uv - chromaOffset - refractionWave).rgb;
            finalColor.r = mix(finalColor.r, chromaR.r, glitchFactor * 0.6);
            finalColor.b = mix(finalColor.b, chromaB.b, glitchFactor * 0.6);
            finalColor.g = mix(finalColor.g, texture(DiffuseSampler, uv + refractionWave * 0.5).g, glitchFactor * 0.2);
        }
        
        float scanline = 0.0;
        if (glitchFactor > 0.3) {
            float line = sin(uv.y * 800.0 + GameTime * 0.5 * glitchFactor) * 0.5 + 0.5;
            line = pow(line, 50.0) * glitchFactor;
            scanline = line * isUninpossible;
            float displacement = glitchNoise(vec2(uv.y * 10.0, GameTime * 0.01), GameTime) * glitchFactor * 0.02;
            vec3 displaced = texture(DiffuseSampler, vec2(uv.x + displacement, uv.y)).rgb;
            finalColor = mix(finalColor, displaced, scanline * 0.5);
        }
        
        float glitchTime = fract(GameTime * 0.0004 + glitchFactor * 0.1);
        float glitch = 0.0;
        if (glitchTime < 0.08 + glitchFactor * 0.1) {
            float side = step(0.97 - glitchFactor * 0.05, uv.x) + step(uv.x, 0.03 + glitchFactor * 0.05);
            glitch = side * (0.5 + 0.5 * sin(GameTime * 0.5 + glitchFactor * 10.0)) * isUninpossible;
            glitch *= (1.0 + glitchFactor * 2.0);
        }
        vec3 glitchColor = mix(purpleLens, radiantPurple, glitchTime * 5.0) * glitch * 3.0;
        glitchColor += vec3(glitchNoise(uv, GameTime * 0.1)) * glitch * glitchFactor * 2.0;
        
        vec3 rimFlash = vec3(0.0);
        if (isMajorWarp > 0.5) {
            float edge = smoothstep(0.8, 1.0, abs(uv.x - 0.5) * 2.0) + smoothstep(0.8, 1.0, abs(uv.y - 0.5) * 2.0);
            edge *= isMajorWarp * glitchFactor;
            float eyeRim = exp(-leftDist * 20.0) + exp(-rightDist * 20.0);
            eyeRim *= isMajorWarp * 2.0;
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
        foamColor += vec3(1.0) * isMajorWarp * foam * 2.0;
        
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
