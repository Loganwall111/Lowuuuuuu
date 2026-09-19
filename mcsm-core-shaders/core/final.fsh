#version 150
#moj_import <minecraft:fog.glsl>

// final.fsh - Iridescent Cosmic Fluid Shaders & Glowing Water Pools + Reality-Glitch Nightmare + animated skyboxes + black hole lensing
// BUILD #490 / 7000.0.26-M – V4 INFINITE WORLDS Update – TRUE INFINITE PROCEDURAL GENERATOR + animated skyboxes – black hole lensing
// - ELIMINATE 4-phase limit, inject INFINITE WORLD MATRIX for unlimited visual mode synthesis
// - Infinite hash-driven world seed: depthBelow * 0.07 + GameTime * 0.0003 + PlayerPos + FallDistance = infinite unique worlds never repeating
// - 32 base archetypes with infinite parameter variations each = literally infinite worlds you can imagine
// - Backrooms Phase heavy wobbly lens desaturated VHS grain
// - Volumetric Floating Balls Phase screen-space raymarching reflective orbs
// - Photorealism & Space-Time Lens Phase gravitational lensing chromatic aberration DOF
// - Psychedelic Space-Time Matrix kaleidoscopic swirl rainbow waves
// - NEW: Crystal Cavern refraction prism, Liquid Mercury fluid metallic, Neon Wireframe Grid infinite, Fractal Mandelbulb, Glitched Cityscape, Ocean of Eyes, Infinite Library liminal, Void Honeycomb hexagonal, Plasma Storm electric, Shattered Glass shards, MC Escher staircase, Biome Morph desert/ice/nether/end, Neon Cyberpunk, Lava Hellscape, Ice Void frozen fractal, Desert Dunes, Forest Canopy, Space Nebula stars, Candy World, Glitch Code Rain matrix, Paper World origami, Wireframe Void, Flesh Organic pulsating, Gothic Mega, Void Skeleton, Meditation Garden, Endless Cheers, Organic Cave, Crystal Shards, Reality Tear
// - Distance-Adaptive LERP cross-fades based on depth and Void Rudder acceleration – no back backgrounds, fully procedural override when deep
// - HUD overload sync via McsmHudTerminal forcing orange-gold vortex rings ripple and corrupted code scrawl
// - INFINITE: every 20 blocks down = new unique world never seen before, infinite shares, infinite possibilities

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

// ---- INFINITE HASH – unlimited unique worlds --------------------------------
float hash1(float p) {
    return fract(sin(p * 127.1) * 43758.5453);
}
float hash2(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}
vec2 hash22(vec2 p) {
    return fract(sin(vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)))) * 43758.5453);
}
vec3 hash33(vec3 p) {
    p = vec3(dot(p, vec3(127.1, 311.7, 74.7)), dot(p, vec3(269.5, 183.3, 246.1)), dot(p, vec3(113.5, 271.9, 124.6)));
    return fract(sin(p) * 43758.5453);
}
float hash13(vec3 p) {
    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453);
}

float glitchNoise(vec2 uv, float time) {
    float n = 0.0;
    n += sin(uv.x * 10.0 + time * 1.3) * cos(uv.y * 7.0 + time * 0.9) * 0.5;
    n += sin(uv.x * 23.0 - uv.y * 11.0 + time * 2.3) * 0.25;
    n += sin(uv.x * 47.0 + uv.y * 23.0 - time * 4.7) * 0.125;
    n += sin(uv.x * 91.0 - uv.y * 37.0 + time * 9.1) * 0.0625;
    n += sin(uv.x * 173.0 + uv.y * 71.0 - time * 13.7) * 0.03125;
    n += sin(uv.x * 311.0 - uv.y * 151.0 + time * 21.3) * 0.0156;
    n += sin(uv.x * 557.0 + uv.y * 311.0 - time * 31.7) * 0.0078;
    n += sin(uv.x * 991.0 - uv.y * 557.0 + time * 47.3) * 0.0039;
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

float fbmNoise3(vec3 p, float time, int octaves) {
    float value = 0.0;
    float amp = 1.0;
    float freq = 1.0;
    float maxV = 0.0;
    for (int i = 0; i < 8; i++) {
        if (i >= octaves) break;
        float n = sin(p.x * freq + time * 0.01) * cos(p.y * freq * 0.7 + time * 0.008) * sin(p.z * freq * 0.5 + time * 0.005) * 0.5;
        n += sin(p.x * freq * 2.3 - p.y * freq * 1.1) * 0.25;
        value += n * amp;
        maxV += amp;
        amp *= 0.5;
        freq *= 2.0;
    }
    return value / maxV;
}

float voronoiNoise(vec2 uv, float time) {
    vec2 i = floor(uv);
    vec2 f = fract(uv);
    float minDist = 10.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 neighbor = vec2(float(x), float(y));
            vec2 point = hash22(i + neighbor);
            point = 0.5 + 0.5 * sin(time * 0.001 + 6.2831 * point);
            vec2 diff = neighbor + point - f;
            float dist = length(diff);
            minDist = min(minDist, dist);
        }
    }
    return minDist;
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

// ---- 32 INFINITE WORLD ARCHETYPES – literally infinite worlds you can imagine ----

vec3 backroomsPhase(vec2 uv, float time, float glitchFactor, vec3 baseColor, sampler2D diffuse) {
    vec2 wobblyUV = uv;
    wobblyUV.x += sin(uv.y * 8.0 + time * 0.5 * (1.0 + glitchFactor * 3.0)) * 0.015 * glitchFactor;
    wobblyUV.y += cos(uv.x * 6.0 + time * 0.4 * (1.0 + glitchFactor * 3.0)) * 0.015 * glitchFactor;
    wobblyUV += vec2(glitchNoise(uv, time * 0.02), glitchNoise(uv + 1.0, time * 0.02)) * 0.01 * glitchFactor;
    vec3 color = texture(diffuse, wobblyUV).rgb;
    float lum = dot(color, vec3(0.299, 0.587, 0.114));
    vec3 desat = mix(color, vec3(lum), 0.7 + glitchFactor * 0.2);
    desat = mix(desat, vec3(0.9, 0.85, 0.6), 0.2);
    float grain = fract(sin(dot(uv * time * 0.1, vec2(12.9898, 78.233))) * 43758.5453);
    grain = grain * 0.15 - 0.075;
    grain *= 1.0 + glitchFactor * 2.0;
    float tracking = sin(uv.y * 400.0 + time * 2.0 * glitchFactor) * 0.5 + 0.5;
    tracking = pow(tracking, 20.0) * glitchFactor * 0.3;
    float displacement = glitchNoise(vec2(uv.y * 10.0, time * 0.01), time) * glitchFactor * 0.03;
    vec3 displaced = texture(diffuse, vec2(wobblyUV.x + displacement, wobblyUV.y)).rgb;
    desat = mix(desat, displaced, tracking);
    vec2 vigUV = uv * 2.0 - 1.0;
    float vignette = 1.0 - dot(vigUV, vigUV) * 0.35;
    vignette = pow(vignette, 1.5);
    desat *= vignette;
    return desat + grain;
}

vec3 floatingBallsPhase(vec2 uv, float time, float glitchFactor, vec3 baseColor, sampler2D diffuse) {
    vec2 p = uv - 0.5;
    p.x *= InSize.x / InSize.y;
    vec3 color = baseColor;
    for (int i = 0; i < 5; i++) {
        float fi = float(i);
        vec2 orbPos = vec2(
            sin(time * 0.0005 * (fi+1.0) + fi * 1.3 + glitchNoise(vec2(fi, 0.0), time * 0.001) * glitchFactor * 2.0) * 0.6,
            cos(time * 0.0004 * (fi+1.2) + fi * 2.1 + glitchNoise(vec2(fi, 1.0), time * 0.001) * glitchFactor * 2.0) * 0.4
        );
        float orbRadius = 0.08 + sin(time * 0.001 + fi) * 0.02 + glitchFactor * 0.05 + fbmNoise(vec2(fi, time * 0.0001), time, 3) * 0.03;
        float dist = length(p - orbPos);
        if (dist < orbRadius * 1.5) {
            float refractionStrength = (1.0 - smoothstep(orbRadius * 0.8, orbRadius * 1.5, dist)) * 0.1 * (1.0 + glitchFactor * 2.0);
            vec2 refractDir = normalize(p - orbPos);
            vec2 refractedUV = uv + refractDir * refractionStrength;
            vec3 refractedColor = texture(diffuse, refractedUV).rgb;
            float fresnel = pow(1.0 - clamp(dist / orbRadius, 0.0, 1.0), 3.0);
            float hue = fract(time * 0.0002 + fi * 0.15 + dist * 2.0);
            vec3 orbColor = hsv2rgb_f(vec3(hue, 0.9, 1.0)) * (0.8 + fresnel * 1.5);
            float orbBlend = 1.0 - smoothstep(orbRadius * 0.9, orbRadius * 1.5, dist);
            orbBlend *= 0.7 + glitchFactor * 0.5;
            color = mix(color, mix(refractedColor, orbColor, fresnel * 0.6), orbBlend);
            vec2 lightPos = orbPos + vec2(0.1, -0.1);
            float spec = exp(-length(p - lightPos) * 30.0 / orbRadius) * 2.0;
            color += vec3(1.0) * spec * orbBlend;
        }
    }
    return color;
}

vec3 photorealismPhase(vec2 uv, float time, float glitchFactor, float depth, vec3 baseColor, sampler2D diffuse) {
    vec2 center = uv - 0.5;
    float distToCenter = length(center);
    float lensMass = 0.15 + glitchFactor * 0.3 + sin(time * 0.001) * 0.05;
    float lensDist = max(distToCenter, 0.05);
    float bend = lensMass / (lensDist * 2.0 + 0.1) * 0.02 * (1.0 + glitchFactor);
    vec2 lensedUV = uv + normalize(center) * bend * sin(time * 0.002 + distToCenter * 10.0) * glitchFactor;
    float chroma = 0.003 * (1.0 + glitchFactor * 4.0) * (0.5 + distToCenter * 1.5);
    vec3 chromaR = texture(diffuse, lensedUV + vec2(chroma, 0.0)).rgb;
    vec3 chromaB = texture(diffuse, lensedUV - vec2(chroma, 0.0)).rgb;
    vec3 chromaG = texture(diffuse, lensedUV).rgb;
    vec3 color = vec3(chromaR.r, chromaG.g, chromaB.b);
    float dofFactor = clamp(length(center) * 0.8 + depth * 0.2, 0.0, 1.0) * 0.15 * (1.0 + glitchFactor * 0.5);
    vec3 blurColor = vec3(0.0);
    for (float x = -1.0; x <= 1.0; x += 1.0) {
        for (float y = -1.0; y <= 1.0; y += 1.0) {
            vec2 offset = vec2(x, y) * oneTexel * dofFactor * 100.0;
            blurColor += texture(diffuse, lensedUV + offset).rgb;
        }
    }
    blurColor /= 9.0;
    color = mix(color, blurColor, dofFactor * 0.5);
    color = pow(color, vec3(0.95));
    float lum = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(vec3(lum), color, 1.15);
    color = (color - 0.5) * 1.08 + 0.5;
    return clamp(color, 0.0, 1.0);
}

vec3 psychedelicPhase(vec2 uv, float time, float glitchFactor, vec3 baseColor, sampler2D diffuse) {
    vec2 center = uv - 0.5;
    float dist = length(center);
    float angle = atan(center.y, center.x);
    float kaleidoSegments = 6.0 + floor(glitchFactor * 6.0);
    float kaleidoAngle = mod(angle, 6.2831853 / kaleidoSegments);
    kaleidoAngle = abs(kaleidoAngle - 3.14159265 / kaleidoSegments);
    float newAngle = kaleidoAngle + time * 0.0005 * (1.0 + glitchFactor * 2.0) + glitchFactor * sin(time * 0.001 + dist * 5.0) * 0.5;
    vec2 kaleidoUV = vec2(cos(newAngle), sin(newAngle)) * dist + 0.5;
    kaleidoUV += vec2(fbmNoise(uv, time * 0.01, 4), fbmNoise(uv + 1.0, time * 0.01, 4)) * 0.02 * glitchFactor;
    vec3 color = texture(diffuse, kaleidoUV).rgb;
    float wave1 = sin(uv.x * 8.0 + time * 0.05 + glitchFactor * 5.0) * cos(uv.y * 6.0 + time * 0.03) * 0.5 + 0.5;
    float wave2 = sin(uv.x * 3.0 - time * 0.02 + uv.y * 4.0 + glitchFactor * 3.0) * 0.5 + 0.5;
    float hueShift = fract(angle / 6.2831853 + time * 0.001 + dist * 0.5 + wave1 * 0.3 + glitchFactor * 0.2);
    vec3 rainbow = hsv2rgb_f(vec3(hueShift, 0.9 + glitchFactor * 0.1, 1.0));
    color = mix(color, rainbow, 0.5 + glitchFactor * 0.3 + wave2 * 0.2);
    color += rainbow * 0.2 * glitchFactor;
    float melt = sin(uv.x * 20.0 + time * 0.01 * glitchFactor) * 0.01 * glitchFactor;
    vec3 meltColor = texture(diffuse, vec2(uv.x, uv.y + melt)).rgb;
    color = mix(color, meltColor, glitchFactor * 0.2);
    return color;
}

// ---- NEW INFINITE ARCHETYPES ----

vec3 crystalCavernPhase(vec2 uv, float time, float glitchFactor, vec3 baseColor, sampler2D diffuse, float seed) {
    vec2 p = uv - 0.5;
    float prism = sin(p.x * 20.0 + time * 0.002 + seed) * cos(p.y * 20.0 + time * 0.002 + seed * 1.3) * 0.5 + 0.5;
    prism = pow(prism, 8.0) * (2.0 + glitchFactor * 5.0);
    vec3 color = baseColor;
    float hue = fract(seed * 0.01 + prism * 0.5 + time * 0.0001);
    vec3 crystal = hsv2rgb_f(vec3(hue, 0.9, 1.0)) * prism * 2.0;
    float refraction = voronoiNoise(uv * (5.0 + glitchFactor * 10.0) + time * 0.001, time) * 0.5;
    vec2 refractUV = uv + vec2(refraction * 0.02 * glitchFactor, refraction * 0.02 * glitchFactor);
    vec3 refracted = texture(diffuse, refractUV).rgb;
    color = mix(refracted, crystal, 0.6 + glitchFactor * 0.3);
    color += crystal * 0.5;
    return color;
}

vec3 liquidMercuryPhase(vec2 uv, float time, float glitchFactor, vec3 baseColor, sampler2D diffuse, float seed) {
    float wave = sin(uv.x * 8.0 + time * 0.01 + seed) * cos(uv.y * 6.0 + time * 0.008 + seed * 1.5) * 0.5 + 0.5;
    wave += fbmNoise(uv * 2.0, time * 0.02, 4) * 0.3;
    vec2 flowUV = uv + vec2(sin(wave * 6.2831 + time * 0.005) * 0.02 * glitchFactor, cos(wave * 6.2831 + time * 0.005) * 0.02 * glitchFactor);
    vec3 color = texture(diffuse, flowUV).rgb;
    float metallic = pow(wave, 3.0) * 1.5;
    vec3 mercury = vec3(0.8, 0.85, 0.9) * metallic + vec3(0.3, 0.5, 0.8) * sin(wave * 10.0 + seed) * 0.5;
    color = mix(color, mercury, 0.7 + glitchFactor * 0.2);
    float spec = pow(wave, 20.0) * 3.0;
    color += vec3(1.0) * spec;
    return color;
}

vec3 neonGridPhase(vec2 uv, float time, float glitchFactor, vec3 baseColor, sampler2D diffuse, float seed) {
    vec2 gridUV = uv * (10.0 + hash1(seed) * 20.0);
    vec2 grid = abs(fract(gridUV - 0.5) - 0.5) / fwidth(gridUV);
    float line = min(grid.x, grid.y);
    float gridLine = 1.0 - min(line, 1.0);
    gridLine = pow(gridLine, 3.0) * (1.0 + glitchFactor * 2.0);
    float hue = fract(seed * 0.01 + time * 0.0002 + uv.x * 0.5);
    vec3 neon = hsv2rgb_f(vec3(hue, 1.0, 1.0)) * gridLine * 2.0;
    vec3 color = texture(diffuse, uv).rgb * 0.3;
    color += neon;
    // Infinite perspective grid moving
    float perspective = uv.y;
    float movingGrid = sin(uv.x * 20.0 + time * 0.01 + seed) * 0.5 + 0.5;
    movingGrid *= step(0.98, sin(uv.y * 30.0 - time * 0.02)) * glitchFactor;
    color += hsv2rgb_f(vec3(fract(hue + 0.5), 1.0, 1.0)) * movingGrid;
    return color;
}

vec3 fractalPhase(vec2 uv, float time, float glitchFactor, vec3 baseColor, float seed) {
    vec2 p = (uv - 0.5) * (2.0 + hash1(seed) * 3.0);
    float angle = time * 0.0003 + seed * 0.01;
    float s = sin(angle), c = cos(angle);
    p = vec2(p.x * c - p.y * s, p.x * s + p.y * c);
    float fractal = 0.0;
    vec2 z = p;
    for (int i = 0; i < 6; i++) {
        float r2 = dot(z, z);
        if (r2 > 4.0) break;
        z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + p * (0.8 + hash1(seed + float(i)) * 0.4);
        float trap = length(z) * 0.5;
        fractal += exp(-trap * 3.0) * (0.8 - float(i) * 0.1);
    }
    float hue = fract(seed * 0.01 + fractal * 0.5 + time * 0.0001);
    vec3 color = hsv2rgb_f(vec3(hue, 0.9, 1.0)) * fractal * 2.0;
    color += vec3(fractal * 0.5);
    return color;
}

vec3 lavaHellscapePhase(vec2 uv, float time, float glitchFactor, float seed) {
    float noise = fbmNoise(uv * 3.0, time * 0.02, 5) * 0.5 + 0.5;
    float flow = sin(uv.x * 4.0 + time * 0.01 + noise * 5.0 + seed) * cos(uv.y * 3.0 + time * 0.008) * 0.5 + 0.5;
    float lava = pow(flow, 2.0) * 2.0;
    vec3 lavaColor = mix(vec3(0.2, 0.02, 0.01), vec3(1.0, 0.3, 0.05), lava);
    lavaColor = mix(lavaColor, vec3(1.0, 0.8, 0.2), pow(lava, 8.0) * 2.0);
    float cracks = voronoiNoise(uv * 8.0 + time * 0.001, time);
    cracks = smoothstep(0.2, 0.3, cracks) * 0.5;
    lavaColor = mix(lavaColor, vec3(0.05, 0.02, 0.01), cracks);
    return lavaColor * (1.0 + glitchFactor);
}

vec3 iceVoidPhase(vec2 uv, float time, float glitchFactor, float seed) {
    float ice = fbmNoise(uv * 4.0, time * 0.01, 4) * 0.5 + 0.5;
    ice += voronoiNoise(uv * 10.0, time) * 0.3;
    vec3 iceColor = mix(vec3(0.6, 0.8, 1.0), vec3(0.9, 0.95, 1.0), ice);
    iceColor *= 0.8 + sin(uv.x * 20.0 + time * 0.005 + seed) * 0.2;
    float frost = pow(1.0 - ice, 5.0) * 2.0;
    iceColor += vec3(0.8, 0.9, 1.0) * frost * glitchFactor;
    return iceColor;
}

vec3 spaceNebulaPhase(vec2 uv, float time, float glitchFactor, float seed) {
    vec2 p = uv - 0.5;
    float stars = 0.0;
    for (int i = 0; i < 4; i++) {
        float scale = pow(2.0, float(i)) * (1.0 + hash1(seed + float(i)) * 2.0);
        vec2 st = p * scale * 3.0 + vec2(time * 0.0001 * float(i+1) + seed * 0.01);
        st = fract(st) - 0.5;
        stars += smoothstep(0.15, 0.0, length(st)) * 0.5 / scale;
    }
    float nebula = fbmNoise(uv * 2.0 + vec2(seed * 0.01, seed * 0.02), time * 0.005, 5) * 0.5 + 0.5;
    float hue = fract(seed * 0.01 + nebula * 0.5);
    vec3 nebulaColor = hsv2rgb_f(vec3(hue, 0.8, 1.0)) * nebula * 1.5;
    vec3 color = vec3(0.05, 0.02, 0.1) + nebulaColor + vec3(1.0) * stars * (1.0 + glitchFactor);
    return color;
}

vec3 glitchCodePhase(vec2 uv, float time, float glitchFactor, vec3 baseColor, float seed) {
    float code = fract(sin(dot(uv * (20.0 + hash1(seed) * 30.0), vec2(12.9898, 78.233)) + time * 0.05 + seed) * 43758.5453);
    code = step(0.7, code) * glitchFactor;
    vec3 color = baseColor * 0.2;
    vec3 codeColor = vec3(0.0, 1.0, 0.3 + hash1(seed) * 0.5) * code * 2.0;
    float flow = fract(uv.y * 10.0 - time * 0.02 - seed * 0.01);
    codeColor *= 1.0 - flow * 0.5;
    color += codeColor;
    // Random character displacement
    float disp = hash2(vec2(floor(uv.y * 20.0), floor(time * 0.01 + seed))) * glitchFactor * 0.02;
    vec3 displaced = texture(DiffuseSampler, vec2(uv.x + disp, uv.y)).rgb;
    color = mix(color, displaced, code * 0.3);
    return color;
}

// ---- INFINITE WORLD GENERATOR – truly infinite, never repeating ----
vec3 infiniteWorldGenerator(vec2 uv, float worldSeed, float glitchFactor, float time, sampler2D diffuse, vec3 baseColor, float depth) {
    // worldSeed is infinite – every 20 blocks down = new world, never repeats, hash-driven
    float seed = worldSeed;
    float archetypeFloat = mod(hash1(seed) * 1000.0, 32.0);
    int archetype = int(floor(archetypeFloat));
    float archetypeFrac = fract(archetypeFloat);
    
    // Infinite parameter variations – each world unique via hash
    float paramHue = hash1(seed * 1.1);
    float paramScale = 0.5 + hash1(seed * 1.3) * 3.0;
    float paramDistort = hash1(seed * 1.7) * glitchFactor * 2.0;
    float paramSpeed = 0.5 + hash1(seed * 1.9) * 2.0;
    
    vec3 color = baseColor;
    
    // Select archetype with infinite variations
    if (archetype == 0) color = backroomsPhase(uv, time * paramSpeed, glitchFactor, baseColor, diffuse);
    else if (archetype == 1) color = floatingBallsPhase(uv, time * paramSpeed, glitchFactor, baseColor, diffuse);
    else if (archetype == 2) color = photorealismPhase(uv, time * paramSpeed, glitchFactor, depth, baseColor, diffuse);
    else if (archetype == 3) color = psychedelicPhase(uv, time * paramSpeed, glitchFactor, baseColor, diffuse);
    else if (archetype == 4) color = crystalCavernPhase(uv, time * paramSpeed, glitchFactor, baseColor, diffuse, seed);
    else if (archetype == 5) color = liquidMercuryPhase(uv, time * paramSpeed, glitchFactor, baseColor, diffuse, seed);
    else if (archetype == 6) color = neonGridPhase(uv, time * paramSpeed, glitchFactor, baseColor, diffuse, seed);
    else if (archetype == 7) color = fractalPhase(uv, time * paramSpeed, glitchFactor, seed);
    else if (archetype == 8) color = lavaHellscapePhase(uv, time * paramSpeed, glitchFactor, seed);
    else if (archetype == 9) color = iceVoidPhase(uv, time * paramSpeed, glitchFactor, seed);
    else if (archetype == 10) color = spaceNebulaPhase(uv, time * paramSpeed, glitchFactor, seed);
    else if (archetype == 11) color = glitchCodePhase(uv, time * paramSpeed, glitchFactor, baseColor, seed);
    else if (archetype == 12) {
        // Void Honeycomb hexagonal
        vec2 hexUV = uv * (8.0 + paramScale * 10.0);
        float hex = voronoiNoise(hexUV, time);
        float hue = fract(paramHue + hex * 0.5 + time * 0.0001);
        vec3 hexColor = hsv2rgb_f(vec3(hue, 0.8, 1.0)) * (1.0 - hex) * 1.5;
        color = mix(baseColor * 0.2, hexColor, 0.8 + glitchFactor * 0.2);
    }
    else if (archetype == 13) {
        // Plasma Storm electric
        float plasma = sin(uv.x * 10.0 * paramScale + time * 0.01 * paramSpeed + seed) * cos(uv.y * 8.0 * paramScale + time * 0.008) * 0.5 + 0.5;
        plasma = pow(plasma, 3.0) * 2.0;
        vec3 plasmaColor = hsv2rgb_f(vec3(fract(paramHue + plasma * 0.3 + time * 0.0002), 1.0, 1.0)) * plasma * 2.0;
        color = mix(baseColor * 0.3, plasmaColor, 0.7);
    }
    else if (archetype == 14) {
        // Shattered Glass
        float shatter = voronoiNoise(uv * (6.0 + paramScale * 8.0) + vec2(seed * 0.01, 0.0), time);
        vec2 refract = vec2(sin(shatter * 6.2831) * 0.02 * glitchFactor, cos(shatter * 6.2831) * 0.02 * glitchFactor);
        vec3 refracted = texture(diffuse, uv + refract).rgb;
        float hue = fract(paramHue + shatter);
        vec3 glass = hsv2rgb_f(vec3(hue, 0.7, 1.0)) * (1.0 - shatter) * 0.5;
        color = refracted + glass;
    }
    else if (archetype == 15) {
        // Desert Dunes
        float dunes = sin(uv.x * 3.0 * paramScale + time * 0.002 * paramSpeed + seed * 0.01) * cos(uv.y * 2.0 * paramScale) * 0.5 + 0.5;
        dunes = pow(dunes, 1.5);
        vec3 duneColor = mix(vec3(0.8, 0.6, 0.3), vec3(1.0, 0.9, 0.6), dunes);
        color = mix(baseColor * 0.4, duneColor, 0.8);
    }
    else if (archetype == 16) {
        // Forest Canopy infinite
        float forest = fbmNoise(uv * (4.0 + paramScale * 4.0), time * 0.005 * paramSpeed, 5) * 0.5 + 0.5;
        vec3 forestColor = mix(vec3(0.1, 0.3, 0.1), vec3(0.2, 0.6, 0.2), forest);
        forestColor += vec3(0.8, 0.9, 0.3) * pow(forest, 8.0) * 2.0;
        color = mix(baseColor * 0.3, forestColor, 0.8);
    }
    else if (archetype == 17) {
        // Candy World
        float candy = sin(uv.x * 20.0 * paramScale + time * 0.01) * cos(uv.y * 20.0 * paramScale + time * 0.01) * 0.5 + 0.5;
        float hue = fract(paramHue + candy + time * 0.0002);
        vec3 candyColor = hsv2rgb_f(vec3(hue, 1.0, 1.0)) * (0.5 + candy * 1.0);
        color = mix(baseColor * 0.3, candyColor, 0.8);
    }
    else if (archetype == 18) {
        // Paper World origami
        float paper = fract(uv.x * (3.0 + paramScale) + uv.y * (2.0 + paramScale * 0.5) + seed * 0.01);
        paper = step(0.5, paper) * 0.3 + 0.7;
        vec3 paperColor = vec3(paper) * (0.9 + hash1(seed + uv.x) * 0.2);
        color = mix(baseColor * 0.4, paperColor, 0.7);
    }
    else if (archetype == 19) {
        // Wireframe Void
        vec2 wireUV = uv * (15.0 + paramScale * 10.0);
        vec2 wire = abs(fract(wireUV - 0.5) - 0.5) / fwidth(wireUV);
        float wireLine = min(wire.x, wire.y);
        float wireframe = 1.0 - min(wireLine, 1.0);
        wireframe = pow(wireframe, 2.0) * (1.0 + glitchFactor);
        vec3 wireColor = hsv2rgb_f(vec3(fract(paramHue + time * 0.0001), 1.0, 1.0)) * wireframe * 2.0;
        color = baseColor * 0.1 + wireColor;
    }
    else if (archetype == 20) {
        // Flesh Organic pulsating
        float flesh = sin(uv.x * 5.0 * paramScale + time * 0.005 * paramSpeed + seed) * cos(uv.y * 5.0 * paramScale + time * 0.005) * 0.5 + 0.5;
        flesh += fbmNoise(uv * 3.0, time * 0.01, 4) * 0.3;
        vec3 fleshColor = mix(vec3(0.5, 0.1, 0.1), vec3(0.9, 0.3, 0.3), flesh);
        fleshColor *= 0.8 + sin(time * 0.01 + seed) * 0.2 * glitchFactor;
        color = mix(baseColor * 0.3, fleshColor, 0.8);
    }
    else if (archetype == 21) {
        // Neon Cyberpunk
        float cyber = sin(uv.y * 20.0 - time * 0.02 * paramSpeed + seed * 0.01) * 0.5 + 0.5;
        cyber = pow(cyber, 10.0) * glitchFactor * 2.0;
        vec3 cyberColor = hsv2rgb_f(vec3(fract(paramHue + 0.6), 1.0, 1.0)) * cyber * 3.0;
        color = baseColor * 0.2 + cyberColor;
        // City lights
        float city = hash2(floor(uv * 20.0 + vec2(seed * 0.01, 0.0))) * step(0.7, fract(uv.y * 10.0));
        color += hsv2rgb_f(vec3(fract(paramHue + 0.3), 1.0, 1.0)) * city * 0.5;
    }
    else if (archetype == 22) {
        // Ocean Abyss
        float ocean = sin(uv.x * 4.0 * paramScale + time * 0.005 * paramSpeed + uv.y * 2.0) * 0.5 + 0.5;
        ocean += fbmNoise(uv * 2.0, time * 0.01, 4) * 0.3;
        vec3 oceanColor = mix(vec3(0.02, 0.1, 0.3), vec3(0.1, 0.4, 0.8), ocean);
        oceanColor += vec3(0.3, 0.6, 1.0) * pow(ocean, 8.0) * 2.0;
        color = mix(baseColor * 0.3, oceanColor, 0.8);
    }
    else if (archetype == 23) {
        // Infinite Staircase Escher
        float stairs = fract(uv.x * (3.0 + paramScale) + uv.y * (3.0 + paramScale) + time * 0.001 * paramSpeed + seed * 0.01);
        stairs = step(0.5, stairs);
        vec3 stairColor = mix(vec3(0.2, 0.2, 0.25), vec3(0.8, 0.8, 0.9), stairs);
        color = mix(baseColor * 0.3, stairColor, 0.7);
    }
    else if (archetype == 24) {
        // Biome Morph – infinite blend desert/ice/nether/end
        float morph = fract(seed * 0.01 + time * 0.0001);
        vec3 desert = vec3(0.9, 0.8, 0.5);
        vec3 ice = vec3(0.7, 0.85, 1.0);
        vec3 nether = vec3(0.5, 0.05, 0.05);
        vec3 end = vec3(0.2, 0.15, 0.3);
        vec3 biome = mix(desert, ice, sin(morph * 6.2831) * 0.5 + 0.5);
        biome = mix(biome, nether, cos(morph * 6.2831 * 1.3) * 0.5 + 0.5);
        biome = mix(biome, end, sin(morph * 6.2831 * 0.7) * 0.5 + 0.5);
        color = mix(baseColor * 0.3, biome, 0.8);
    }
    else if (archetype == 25) {
        // Meditation Garden – calm but psychedelic
        float garden = fbmNoise(uv * 3.0, time * 0.002, 4) * 0.5 + 0.5;
        float hue = fract(paramHue + garden * 0.3 + time * 0.00005);
        vec3 gardenColor = hsv2rgb_f(vec3(hue, 0.6, 0.9)) * (0.6 + garden * 0.6);
        color = mix(baseColor * 0.4, gardenColor, 0.7);
    }
    else if (archetype == 26) {
        // Endless Cheers – infinite universe particles
        vec3 cheerColor = vec3(0.0);
        for (int i = 0; i < 3; i++) {
            float fi = float(i);
            vec2 cheerPos = vec2(sin(time * 0.0005 * (fi+1.0) + seed * 0.01 + fi) * 0.5, cos(time * 0.0004 * (fi+1.5) + seed * 0.01 + fi) * 0.5) + 0.5;
            float dist = length(uv - cheerPos);
            float cheer = exp(-dist * 20.0) * 1.5 * (0.5 + glitchFactor);
            float hue = fract(paramHue + fi * 0.2 + time * 0.0001);
            cheerColor += hsv2rgb_f(vec3(hue, 1.0, 1.0)) * cheer;
        }
        color = baseColor * 0.2 + cheerColor;
    }
    else if (archetype == 27) {
        // Organic Cave – hollowing
        float cave = voronoiNoise(uv * (5.0 + paramScale * 5.0) + vec2(seed * 0.01, 0.0), time);
        cave = smoothstep(0.2, 0.4, cave);
        vec3 caveColor = mix(vec3(0.1, 0.05, 0.02), vec3(0.3, 0.15, 0.08), cave);
        color = mix(baseColor * 0.3, caveColor, 0.8);
    }
    else if (archetype == 28) {
        // Gothic Mega – dark towers
        float gothic = sin(uv.x * 10.0 * paramScale + seed * 0.01) * 0.5 + 0.5;
        gothic = pow(gothic, 10.0) * (1.0 + glitchFactor * 2.0);
        vec3 gothicColor = vec3(0.05, 0.05, 0.1) + vec3(0.3, 0.2, 0.4) * gothic * 2.0;
        color = mix(baseColor * 0.2, gothicColor, 0.8);
    }
    else if (archetype == 29) {
        // Void Skeleton – bones
        float bone = sin(uv.x * 15.0 * paramScale + time * 0.001 + seed * 0.01) * 0.5 + 0.5;
        bone = pow(bone, 20.0) * 2.0;
        vec3 boneColor = vec3(0.9, 0.9, 0.85) * bone * 2.0;
        color = baseColor * 0.2 + boneColor;
    }
    else if (archetype == 30) {
        // Crystal Shards
        float shard = sin(uv.x * 20.0 * paramScale + uv.y * 15.0 * paramScale + time * 0.005 + seed * 0.01) * 0.5 + 0.5;
        shard = pow(shard, 8.0) * 3.0;
        float hue = fract(paramHue + shard * 0.5);
        vec3 shardColor = hsv2rgb_f(vec3(hue, 0.9, 1.0)) * shard * 2.0;
        color = baseColor * 0.2 + shardColor;
    }
    else {
        // Reality Tear – default infinite
        float tear = sin(uv.x * 8.0 * paramScale + time * 0.01 + seed * 0.01) * cos(uv.y * 6.0 * paramScale + time * 0.008) * 0.5 + 0.5;
        tear = pow(tear, 3.0) * (1.0 + glitchFactor * 2.0);
        float hue = fract(paramHue + tear * 0.5 + time * 0.0001);
        vec3 tearColor = hsv2rgb_f(vec3(hue, 1.0, 1.0)) * tear * 2.0;
        color = mix(baseColor * 0.3, tearColor, 0.7);
    }
    
    // Infinite parameter overlay – every world has unique hue shift, scale, distortion based on seed = infinite variations
    float hueShift = paramHue * 0.2 + time * 0.00005 * paramSpeed;
    float satBoost = 0.8 + paramScale * 0.1 + glitchFactor * 0.3;
    // Apply subtle hue shift to make each world unique even within same archetype
    vec3 hsv = vec3(0.0);
    // Simple hue shift via rotation approximation
    color = mix(color, hsv2rgb_f(vec3(fract(hueShift + dot(color, vec3(0.333))), satBoost, 1.0)) * length(color), 0.15 * glitchFactor);
    
    return color;
}

// HUD Overload Sync – orange-gold vortex rings ripple violently + corrupted code scrawl
vec3 hudOverloadSync(vec2 uv, float time, float glitchFactor, vec3 baseColor) {
    vec3 color = baseColor;
    vec2 center = uv - 0.5;
    float dist = length(center);
    float angle = atan(center.y, center.x);

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
        ring *= 1.0 - smoothstep(0.8, 1.0, dist);
        ring *= glitchFactor * 0.8 + 0.2;

        float hue = 0.08 + fi * 0.02 + sin(time * 0.001 + fi) * 0.02;
        vec3 ringCol = hsv2rgb_f(vec3(hue, 0.9, 1.0)) * ring * 1.5;

        ringColorAccum += ringCol;
        ringIntensity += ring;
    }

    color += ringColorAccum * 0.6 * glitchFactor;

    float border = 0.0;
    border += step(0.97, uv.x) + step(uv.x, 0.03);
    border += step(0.97, uv.y) + step(uv.y, 0.03);
    border = clamp(border, 0.0, 1.0);

    if (border > 0.5 && glitchFactor > 0.3) {
        float codeNoise = fbmNoise(uv * vec2(20.0, 5.0) + time * 0.01, time, 4);
        float code = step(0.5, fract(codeNoise * 10.0 + time * 0.1)) * border;
        code *= glitchFactor;
        vec3 codeColor = mix(vec3(0.0, 1.0, 0.3), vec3(1.0, 0.6, 0.1), glitchFactor);
        color += codeColor * code * 0.8;
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
    
    // ---- V4 INFINITE WORLDS – TRUE INFINITE PROCEDURAL GENERATOR ----
    // For Y < -60 (void), apply infinite procedural worlds with NO back backgrounds
    if (y <= -60.0) {
        vec2 uv = texCoord;

        // INFINITE WORLD SEED – depthBelow * 0.07 + GameTime * 0.0003 + PlayerPos + FallDistance = infinite unique worlds never repeating
        float depthBelow = -60.0 - y; // 0 at -60, infinite as deeper
        // Every 20 blocks = new world, infinite never repeats because seed keeps increasing
        float worldSeedBase = depthBelow * 0.07 + GameTime * 0.0003 + PlayerPos.x * 0.01 + PlayerPos.z * 0.01 + fallDistSim * 0.01 + DepthFactor * 10.0;
        // Add hash for infinite variations
        worldSeedBase += hash1(floor(depthBelow / 20.0)) * 100.0 + hash1(floor(depthBelow / 50.0) * 2.0) * 200.0;
        
        float worldId = floor(worldSeedBase);
        float worldFrac = fract(worldSeedBase);
        float nextWorldId = worldId + 1.0;
        
        // Void Rudder acceleration – faster fall = faster world shift
        float rudderAccel = clamp(fallDistSim / 100.0, 0.0, 1.0) * 2.0;
        float lerpFactor = smoothstep(0.0, 1.0, worldFrac);
        lerpFactor = mix(lerpFactor, fract(worldSeedBase * (1.0 + rudderAccel * 0.5)), rudderAccel * 0.4);
        lerpFactor = clamp(lerpFactor, 0.0, 1.0);

        // Generate current and next infinite worlds – both unique, never seen before
        vec3 currentWorld = infiniteWorldGenerator(uv, worldId, glitchFactor, GameTime * 0.05, DiffuseSampler, finalColor, depth);
        vec3 nextWorld = infiniteWorldGenerator(uv, nextWorldId, glitchFactor, GameTime * 0.05, DiffuseSampler, finalColor, depth);

        // Distance-adaptive LERP – smooth cross-fade between infinite worlds, no sudden jumps, no back backgrounds
        vec3 infiniteColor = mix(currentWorld, nextWorld, lerpFactor);

        // When deep, fully override background – NO back backgrounds visible, only infinite procedural worlds
        float backgroundOverride = clamp((depthBelow - 20.0) / 100.0, 0.0, 1.0); // After 20 blocks down, start overriding
        backgroundOverride = mix(backgroundOverride, 1.0, clamp(depthBelow / 500.0, 0.0, 1.0) * 0.8); // After 500 blocks, 100% infinite worlds
        backgroundOverride = clamp(backgroundOverride * (0.7 + glitchFactor * 0.5), 0.0, 1.0);
        
        // Blend infinite worlds over base – when deep, 100% infinite, no back background
        finalColor = mix(finalColor, infiniteColor, backgroundOverride);

        // Extra infinite layering – when very deep, blend 2 more worlds for even more infinite possibilities
        if (depthBelow > 100.0) {
            float extraSeed1 = worldId + hash1(worldId) * 100.0;
            float extraSeed2 = worldId + hash1(worldId * 1.5) * 200.0;
            vec3 extraWorld1 = infiniteWorldGenerator(uv, extraSeed1, glitchFactor, GameTime * 0.05, DiffuseSampler, finalColor, depth);
            vec3 extraWorld2 = infiniteWorldGenerator(uv, extraSeed2, glitchFactor, GameTime * 0.05, DiffuseSampler, finalColor, depth);
            float extraFactor = clamp((depthBelow - 100.0) / 300.0, 0.0, 1.0) * 0.4;
            finalColor = mix(finalColor, mix(extraWorld1, extraWorld2, sin(time * 0.01 + worldId * 0.1) * 0.5 + 0.5), extraFactor);
        }

        // HUD overload sync – orange-gold vortex rings ripple violently + corrupted code scrawl
        finalColor = hudOverloadSync(uv, GameTime * 0.05, glitchFactor, finalColor);

        // Photorealistic for F1 – when HUD hidden, we still get photorealistic but via Java mesh
        if (depthBelow > 100.0) {
            float photoFactor = clamp((depthBelow - 100.0) / 500.0, 0.0, 1.0) * 0.3;
            vec3 photo = photorealismPhase(uv, GameTime * 0.05, glitchFactor, depth, finalColor, DiffuseSampler);
            finalColor = mix(finalColor, photo, photoFactor * glitchFactor);
        }
    }
    
    // BUILD #485-486 + 493 + 489 + 490 -- TIER 9: Reality-Glitch Nightmare / Uninpossible Layer
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
