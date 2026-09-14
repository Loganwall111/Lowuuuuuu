#ifndef MCSM_SKY_RENDER_GLSL
#define MCSM_SKY_RENDER_GLSL
// 7000.0.0-MCSM-CINEMATIC-FINAL.406 -- restored sky renderer.
// The upstream Super Duper Vanilla import shipped WITHOUT this file, so
// getSkyBasic / getFullSkyRender / getSkyFogRender / getSkyReflection were
// undefined and Iris dropped the whole deferred/composite chain: no colorful
// skies, no stars, no clouds, no water sheen. This rewrite implements them in
// the Minecraft: Story Mode look per the reference frames:
//   * vivid per-time-of-day gradient skies (teal-black night, cyan-band dusk,
//     salmon sunset, lavender day, bright noon)
//   * teal aurora curtains + twinkling stars at night
//   * soft sun/moon discs with glow
//   * THREE chunky "story mode" cloud decks that are part of the sky but are
//     real ray/plane layers at y=176/224/288, so you can fly above the
//     infinite deck, then the blocky deck, then the wispy one -- exactly like
//     the Story Mode cutscene skies.

// Uniforms dayCycle / dayCycleAdjust / rainStrength / fragmentFrameTime are
// declared by the including programs (main/composite.glsl, main/deferred1.glsl).
// GLSL forbids re-declaring them here (Iris error: 'dayCycle' : redefinition,
// build .406). Dimensions that force-disable the day cycle / weather skip
// those declarations, so supply compile-time constants there instead.
#ifdef FORCE_DISABLE_DAY_CYCLE
    const float dayCycle = 2.0;
    const float dayCycleAdjust = 1.0;
#endif

#ifdef FORCE_DISABLE_WEATHER
    const float rainStrength = 0.0;
#endif

float mcsmHash(in vec2 p){
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

// Story Mode sky palette keys. s = dayCycle (0 midnight, 1 horizon, 2 noon).
void mcsmSkyKeys(in float s, out vec3 zen, out vec3 hor){
    vec3 z0 = vec3(0.016, 0.055, 0.075); vec3 h0 = vec3(0.030, 0.160, 0.150); // night: teal-black
    vec3 z1 = vec3(0.055, 0.095, 0.360); vec3 h1 = vec3(0.130, 0.760, 0.720); // dusk: deep blue + cyan band
    vec3 z2 = vec3(0.640, 0.270, 0.260); vec3 h2 = vec3(0.960, 0.640, 0.520); // sunset: salmon rose
    vec3 z3 = vec3(0.520, 0.490, 0.830); vec3 h3 = vec3(0.660, 0.680, 0.920); // day: lavender periwinkle
    vec3 z4 = vec3(0.300, 0.520, 0.920); vec3 h4 = vec3(0.500, 0.700, 0.950); // noon: bright blue

    zen = z0; hor = h0;
    float f1 = smoothstep(0.20, 0.60, s); zen = mix(zen, z1, f1); hor = mix(hor, h1, f1);
    float f2 = smoothstep(0.70, 1.00, s); zen = mix(zen, z2, f2); hor = mix(hor, h2, f2);
    float f3 = smoothstep(1.10, 1.45, s); zen = mix(zen, z3, f3); hor = mix(hor, h3, f3);
    float f4 = smoothstep(1.60, 2.00, s); zen = mix(zen, z4, f4); hor = mix(hor, h4, f4);
}

float mcsmNightAmount(in float s){ return 1.0 - smoothstep(0.25, 0.75, s); }
float mcsmSunsetAmount(in float s){ return 1.0 - smoothstep(0.35, 0.60, abs(s - 1.0)); }

// One chunky rectangular cloud deck (ray/plane intersect at a real height).
// 1.9.408: soft plate edges + per-plate shade out-param so decks read as
// lit chunky puffs instead of flat grey cardboard tiles.
float mcsmCloudDeck(in vec3 dir, in vec2 camXZ, in float camY, in float height,
                    in float scale, in float coverage, in float seed,
                    out float tOut, out float shadeOut){
    tOut = -1.0;
    shadeOut = 1.0;
    if(abs(dir.y) < 0.010) return 0.0;
    float t = (height - camY) / dir.y;
    if(t <= 0.0) return 0.0;
    tOut = t;

    vec2 uv = (camXZ + dir.xz * t) / scale + seed;
    vec2 cell = floor(uv);
    vec2 f = fract(uv);

    float presence = step(1.0 - coverage, mcsmHash(cell + seed));
    float wx = 0.40 + 0.55 * mcsmHash(cell + 11.7);
    float wy = 0.40 + 0.55 * mcsmHash(cell + 27.3);
    float lo_x = 0.5 - 0.5 * wx, hi_x = 0.5 + 0.5 * wx;
    float lo_y = 0.5 - 0.5 * wy, hi_y = 0.5 + 0.5 * wy;
    float plate = step(lo_x, f.x) * step(f.x, hi_x)
                * step(lo_y, f.y) * step(f.y, hi_y);
    // soften the rectangle borders so plates do not read as cut cardboard
    float edge = min(min(f.x - lo_x, hi_x - f.x), min(f.y - lo_y, hi_y - f.y));
    plate *= smoothstep(0.0, 0.10, edge);

    // secondary smaller puff grid for the chunky MCSM silhouette
    vec2 cell2 = floor(uv * 2.0);
    float puff = step(1.0 - coverage * 0.9, mcsmHash(cell2 + 5.1));

    // per-plate brightness variation (sun-lit chunkiness, not flat fill)
    shadeOut = 0.80 + 0.40 * mcsmHash(cell + 3.3);

    float a = presence * max(plate, puff * 0.9);
    a *= 1.0 - smoothstep(650.0, 950.0, t);      // distance fade
    a *= smoothstep(0.02, 0.14, abs(dir.y));     // no streaks at grazing angles
    return a;
}

vec3 getSkyBasic(in float nEyePosY, in float skyPosZ){
    vec3 zen, hor;
    mcsmSkyKeys(dayCycle, zen, hor);

    float h = saturate(nEyePosY);
    vec3 col = mix(hor, zen, pow(h, 0.42));
    // below the horizon the gradient settles to a dimmer horizon haze
    col = mix(col, hor * 0.55, saturate(-nEyePosY * 2.0));

    // warm glow around the current celestial body near the horizon
    vec3 glowCol = mix(vec3(1.00, 0.55, 0.40), vec3(1.00, 0.90, 0.70), dayCycleAdjust);
    col += glowCol * pow(max(skyPosZ, 0.0), 6.0) * (0.10 + 0.35 * mcsmSunsetAmount(dayCycle)) * (1.0 - h * 0.7);
    return col;
}

vec3 getFullSkyRender(in vec3 nEyePlayerPos, in vec3 skyPos, in vec3 baseCol){
    vec3 col = baseCol;

    float s = dayCycle;
    float nightAmt = mcsmNightAmount(s);
    float sunsetAmt = mcsmSunsetAmount(s);
    float dayAmt = dayCycleAdjust;

    // ---- stars ----
    vec2 sp = vec2(atan(nEyePlayerPos.z, nEyePlayerPos.x), asin(clamp(nEyePlayerPos.y, -1.0, 1.0)));
    vec2 starCell = floor(sp * vec2(90.0, 60.0));
    float sh = mcsmHash(starCell);
    float star = step(0.9975, sh) * (0.4 + 0.6 * fract(sh * 91.7));
    star *= 0.75 + 0.25 * sin(fragmentFrameTime * 2.0 + sh * 40.0);
    col += vec3(0.90, 0.95, 1.00) * star * nightAmt * smoothstep(0.02, 0.18, nEyePlayerPos.y) * (1.0 - rainStrength);

    // ---- aurora curtains ----
    float az = atan(nEyePlayerPos.z, nEyePlayerPos.x);
    float w = 0.5 + 0.5 * (sin(az * 4.0 + fragmentFrameTime * 0.050)
              + 0.60 * sin(az * 9.0 - fragmentFrameTime * 0.033)
              + 0.40 * sin(az * 17.0 + fragmentFrameTime * 0.021)) * 0.5;
    float curtain = smoothstep(0.62, 0.95, w)
                  * smoothstep(0.05, 0.30, nEyePlayerPos.y)
                  * (1.0 - smoothstep(0.45, 0.85, nEyePlayerPos.y));
    vec3 auroraCol = mix(vec3(0.10, 0.85, 0.70), vec3(0.45, 0.25, 0.80), smoothstep(0.25, 0.70, nEyePlayerPos.y));
    col += auroraCol * curtain * 0.30 * nightAmt * (1.0 - rainStrength);

    // ---- sun / moon disc + glow (skyPos.z points at the active body) ----
    float bodyDot = skyPos.z;
    vec3 bodyCol = mix(vec3(0.85, 0.90, 1.00), vec3(1.00, 0.92, 0.72), dayAmt);
    bodyCol = mix(bodyCol, vec3(1.00, 0.62, 0.42), sunsetAmt * dayAmt);
    float disc = smoothstep(0.99880, 0.99950, bodyDot);
    float glow = pow(max(bodyDot, 0.0), 24.0) * 0.35 + pow(max(bodyDot, 0.0), 4.0) * 0.10;
    col += bodyCol * (disc * 3.0 + glow) * (1.0 - rainStrength * 0.8);

    // ---- three chunky story-mode cloud decks ----
    vec2 camXZ = cameraPosition.xz;
    float camY = cameraPosition.y;
    float t1, t2, t3, s1, s2, s3;
    float a1 = mcsmCloudDeck(nEyePlayerPos, camXZ, camY, 176.0,  46.0, 0.42,  3.1, t1, s1);
    float a2 = mcsmCloudDeck(nEyePlayerPos, camXZ, camY, 224.0,  70.0, 0.30, 17.7, t2, s2);
    float a3 = mcsmCloudDeck(nEyePlayerPos, camXZ, camY, 288.0, 110.0, 0.18, 41.3, t3, s3);

    vec3 cloudCol = mix(vec3(0.13, 0.17, 0.26), vec3(0.98, 0.98, 1.00), dayAmt);
    cloudCol = mix(cloudCol, vec3(1.00, 0.74, 0.64), sunsetAmt * 0.65);
    cloudCol = mix(cloudCol, vec3(0.35, 0.38, 0.46), rainStrength * 0.7);
    // sun-lit tops, shaded undersides
    float topLit = mix(0.78, 1.12, smoothstep(-0.3, 0.3, nEyePlayerPos.y));
    vec3 litTop = cloudCol * topLit;

    // over-composite nearest deck first (order flips when looking down)
    if(nEyePlayerPos.y > 0.0){
        col = mix(col, litTop * s1, a1);
        col = mix(col, litTop * s2, a2);
        col = mix(col, litTop * s3, a3);
    }else{
        col = mix(col, litTop * s3, a3);
        col = mix(col, litTop * s2, a2);
        col = mix(col, litTop * s1, a1);
    }

    return col;
}

vec3 getSkyFogRender(in vec3 nEyePlayerPos){
    vec3 zen, hor;
    mcsmSkyKeys(dayCycle, zen, hor);
    return mix(hor, zen, 0.12 + 0.10 * saturate(nEyePlayerPos.y));
}

vec3 getSkyFogRender(in vec3 nEyePlayerPos, in vec3 skyPos, in vec3 currSkyCol){
    vec3 horFog = getSkyFogRender(nEyePlayerPos);
    // keep the fog hue continuous with whatever the sky gradient is doing
    return mix(horFog, currSkyCol, 0.30);
}

vec3 getSkyReflection(in vec3 reflectViewDir){
    vec3 d = normalize(mat3(gbufferModelViewInverse) * reflectViewDir);
    vec3 zen, hor;
    mcsmSkyKeys(dayCycle, zen, hor);
    float h = saturate(d.y);
    vec3 col = mix(hor, zen, pow(h, 0.45));
    col = mix(col, hor * 0.55, saturate(-d.y * 2.0));
    return col * 1.10;
}

#endif // MCSM_SKY_RENDER_GLSL
