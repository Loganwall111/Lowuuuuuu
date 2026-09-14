#version 330

// ============================================================================
//  MCSM visuals - sky.fsh (version 7000.0.0-MCSM-CINEMATIC-FINAL)
//  Procedural Multi-Layer Sky Blending Overlay with smoothstep Y-axis interpolation
// ============================================================================

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:mcsm_visuals.glsl>

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec3 mcsmCamRay;

out vec4 fragColor;

// Story gradients
const vec3 SKY_DAY[6] = vec3[](
    vec3(0.514, 0.478, 0.906), vec3(0.580, 0.529, 0.980), vec3(0.651, 0.580, 0.984),
    vec3(0.710, 0.616, 0.988), vec3(0.749, 0.643, 0.984), vec3(0.796, 0.659, 0.988));
const vec3 SKY_NIGHT[6] = vec3[](
    vec3(0.067, 0.067, 0.278), vec3(0.082, 0.082, 0.345), vec3(0.098, 0.106, 0.431),
    vec3(0.133, 0.153, 0.565), vec3(0.176, 0.224, 0.714), vec3(0.247, 0.318, 0.871));
const vec3 SKY_DUSK[6] = vec3[](
    vec3(0.388, 0.122, 0.196), vec3(0.520, 0.150, 0.220), vec3(0.660, 0.200, 0.250),
    vec3(0.820, 0.290, 0.220), vec3(0.933, 0.400, 0.180), vec3(0.980, 0.560, 0.280));

vec3 mcsm_sky6(const vec3 c0, const vec3 c1, const vec3 c2, const vec3 c3,
               const vec3 c4, const vec3 c5, float up) {
    float t = clamp(up, 0.0, 1.0) * 5.0;
    int i = int(floor(t));
    float f = t - floor(t);
    vec3 a = c0, b = c1;
    if (i == 0)      { a = c0; b = c1; }
    else if (i == 1) { a = c1; b = c2; }
    else if (i == 2) { a = c2; b = c3; }
    else if (i == 3) { a = c3; b = c4; }
    else if (i == 4) { a = c4; b = c5; }
    else             { a = c5; b = c5; }
    return mix(a, b, f);
}

// Procedural Multi-Layer Sky Blending Overlay
vec3 mcsm_storm_sky(float height, float p, vec3 worldDir) {
    // Smooth Y-axis interpolation over horizon -> zenith
    float tY = smoothstep(-0.20, 0.80, height);

    // Phase 4: Vanilla atmosphere maintained (p < 4.42)
    if (p < 4.42) {
        return vec3(-1.0); // signal to use vanilla sky
    }

    // Phase 4.5: Green sky layer & thick green fog
    vec3 greenZenith  = vec3(0.094, 0.184, 0.180);
    vec3 greenHorizon = vec3(0.165, 0.294, 0.251);
    vec3 col45 = mix(greenHorizon, greenZenith, tY);

    // Phase 5: Deep slate-teal (#1D2B2B) zenith -> flat misty ash-gray (#6E7873) horizon
    vec3 slateZenith  = vec3(0.114, 0.169, 0.169); // #1D2B2B
    vec3 slateHorizon = vec3(0.431, 0.471, 0.451); // #6E7873
    vec3 col5 = mix(slateHorizon, slateZenith, tY);

    // Phase 5.5 Glow Pass: Pitch midnight-purple (#1A0A2A) zenith -> glowing magenta-violet (#7F3AA6) horizon
    vec3 purpleZenith  = vec3(0.102, 0.039, 0.165); // #1A0A2A
    vec3 magentaHorizon= vec3(0.498, 0.227, 0.651); // #7F3AA6
    vec3 col55 = mix(magentaHorizon, purpleZenith, tY);

    // Volumetric white halo around storm bounds in Phase 5.5+
    float backHalo = pow(clamp(1.0 - abs(worldDir.y), 0.0, 1.0), 3.0);
    col55 += vec3(0.25, 0.22, 0.30) * backHalo;

    // Phase 6+ Sunset Split: Dark plum-mauve (#422E3B) zenith -> soft horizon peach-rose (#A0757E)
    vec3 plumZenith  = vec3(0.259, 0.180, 0.231); // #422E3B
    vec3 peachHorizon= vec3(0.627, 0.459, 0.494); // #A0757E
    vec3 col6 = mix(peachHorizon, plumZenith, tY);

    // Phase blend weights
    float w45 = mcsm_ramp(p, 4.42, 4.52) * (1.0 - mcsm_ramp(p, 4.88, 4.98));
    float w5  = mcsm_ramp(p, 4.95, 5.05) * (1.0 - mcsm_ramp(p, 5.38, 5.48));
    float w55 = mcsm_ramp(p, 5.42, 5.52) * (1.0 - mcsm_ramp(p, 5.92, 6.00));
    float w6  = mcsm_ramp(p, 5.95, 6.08);

    vec3 sky = col45 * w45 + col5 * w5 + col55 * w55 + col6 * w6;

    // Zenith mask node wide enough vertically to permanently block out all overworld blue sky bleed
    float zenithMask = smoothstep(0.0, 0.60, height);
    vec3 topDark = mix(vec3(0.05, 0.02, 0.08), plumZenith, mcsm_ramp(p, 5.95, 6.10));
    sky = mix(sky, topDark, zenithMask * 0.85);

    return sky;
}

vec3 mcsm_horizon_glow(float hy, float dayW, float duskW) {
    float g = exp(-hy * 6.0) * 0.18;
    return vec3(1.000, 0.850, 0.600) * g * dayW + vec3(1.000, 0.520, 0.250) * g * 1.4 * duskW;
}

void main() {
    float mcsmP = mcsm_phase(FogSkyEnd, FogColor, FogRenderDistanceEnd);
    float clock = mcsm_clock(GameTime);
    vec3 worldDir = normalize(transpose(mat3(ModelViewMat)) * normalize(mcsmCamRay));
    float height = clamp(worldDir.y, -1.0, 1.0);

    float isBody = step(0.10, length(ColorModulator.rgb - FogColor.rgb));

    // Death cinematic
    float mcsmDt = mcsm_death(FogSkyEnd);
    if (mcsmDt >= 0.0) {
        vec3 ddir = mcsm_death_dir(worldDir, mcsmDt, clock);
        float upd = clamp(ddir.y * 0.5 + 0.5, 0.0, 1.0);
        upd = smoothstep(0.0, 1.0, upd);
        vec3 ddome = mcsm_storm_sky(ddir.y, 7.6, ddir);
        if (ddome.r < 0.0) ddome = vec3(0.05, 0.02, 0.08);
        ddome *= 1.0 - 0.78 * mcsm_ramp(mcsmDt, 0.0, 0.55);
        vec3 dustC = vec3(0.16, 0.13, 0.12);
        float dustW = mcsm_ramp(mcsmDt, 0.62, 0.78) * (1.0 - mcsm_ramp(mcsmDt, 0.86, 1.0));
        ddome = mix(ddome, dustC, dustW * 0.5 * clamp(1.0 - ddir.y, 0.0, 1.0));
        vec3 camWd = vec3(CameraBlockPos) + CameraOffset;
        vec4 aimD = mcsm_boss_dir(camWd);
        vec3 dadd = mcsm_death_cracks(ddir, mcsmDt, clock);
        if (aimD.w > 0.5) {
            dadd += mcsm_death_implosion(worldDir, aimD.xyz, mcsmDt, clock);
            dadd += mcsm_supernova(ddir, aimD.xyz, mcsmDt, clock);
        }
        vec3 dsky = ddome + dadd + vec3(1.0, 0.98, 0.97) * mcsm_death_flash(mcsmDt);
        dsky *= 0.35 + 0.65 * (1.0 - mcsm_ramp(mcsmDt, 0.95, 1.0));
        fragColor = vec4(mcsm_story_grade(dsky), isBody > 0.5 ? 0.0 : 1.0);
        return;
    }

    if (!mcsm_sky_active(mcsmP) || mcsmP < 4.42) {
        if (isBody > 0.5) {
            fragColor = apply_fog(ColorModulator, sphericalVertexDistance,
                                  cylindricalVertexDistance, 0.0,
                                  FogSkyEnd, FogSkyEnd, FogSkyEnd, FogColor);
            return;
        }
        float t = fract(clock / 24000.0) * 24000.0;
        float dayW   = smoothstep(1000.0, 3000.0, t) * (1.0 - smoothstep(9500.0, 12000.0, t));
        float nightW = smoothstep(12500.0, 15000.0, t) * (1.0 - smoothstep(21000.0, 23500.0, t));
        float duskW  = clamp(1.0 - dayW - nightW, 0.0, 1.0);
        float up = clamp(height * 0.5 + 0.5, 0.0, 1.0);
        vec3 sky = mcsm_sky6(SKY_DAY[0], SKY_DAY[1], SKY_DAY[2], SKY_DAY[3], SKY_DAY[4], SKY_DAY[5], up) * dayW
                 + mcsm_sky6(SKY_NIGHT[0], SKY_NIGHT[1], SKY_NIGHT[2], SKY_NIGHT[3], SKY_NIGHT[4], SKY_NIGHT[5], up) * nightW
                 + mcsm_sky6(SKY_DUSK[0], SKY_DUSK[1], SKY_DUSK[2], SKY_DUSK[3], SKY_DUSK[4], SKY_DUSK[5], up) * duskW;
        sky += mcsm_horizon_glow(1.0 - up, dayW, duskW);
        // BUILD #393 -- vanilla hours fold into the day/night/sunset-split
        // sheets the same way: one LERP at the baked row-triangle position.
        sky = mcsm_sheet_fold(sky, mcsm_sheet_vanilla(up, dayW, nightW, duskW),
                              0.45 * mcsm_sheet_w_vanilla(0.0));
        fragColor = vec4(max(mcsm_story_grade(sky), vec3(0.0)), 1.0);
        return;
    }

    // Storm Sky Overlay
    vec3 dome = mcsm_storm_sky(height, mcsmP, worldDir);
    if (dome.r < 0.0) {
        dome = vec3(0.05, 0.02, 0.08);
    }

    // Storm flashes / lightning in late phases
    {
        float mcflGate = mcsm_ramp(mcsmP, 6.00, 6.20) * (1.0 - mcsm_ramp(mcsmP, 8.06, 8.10));
        float mcflWin  = floor(clock / 11.0);
        float mcflRnd  = fract(sin(mcflWin * 91.7) * 4313.7);
        float mcflT    = clock - mcflWin * 11.0 - mcflRnd * 7.5;
        float mcflA    = exp(-max(mcflT, 0.0) * 16.0) * step(0.0, mcflT);
        mcflA         += 0.55 * exp(-max(mcflT - 0.30, 0.0) * 16.0) * step(0.30, mcflT);
        dome += mcflA * mcflGate * (0.26 + 0.5 * clamp(height, 0.0, 1.0))
              * vec3(0.82, 0.66, 1.0) * 0.46;
    }

    // BUILD #393 -- fold the procedural dome into the packed gradient sheets:
    // one smooth LERP (mcsm_sheet_fold) toward the baked sheet row for this
    // phase -- teal at 5, purple through 5.5-5.9, salmon from 6 -- with the
    // neighbouring rows cross-fading, so the sky folds sheet-to-sheet as the
    // storm evolves. Master's halo/topDark character stays dominant (0.55 cap).
    dome = mcsm_sheet_fold(dome, mcsm_sheet_storm(clamp(height, 0.0, 1.0), mcsmP),
                           0.55 * mcsm_sheet_w_storm(mcsmP, 0.0));

    vec3 camWorld = vec3(CameraBlockPos) + CameraOffset;
    vec4 aim = mcsm_boss_dir(camWorld);
    if (aim.w > 0.5 && mcsmP >= 5.10 && mcsmP <= 5.90) {
        vec4 blob = mcsm_blob(worldDir, aim.xyz, mcsmP, clock, dome);
        dome = dome * (1.0 - blob.w) + blob.rgb;
    }

    float hide = mcsm_ramp(mcsmP, 5.05, 5.20);
    vec3 body = mcsm_sky_body_tint(mcsmP, ColorModulator.rgb);
    float a = mix(ColorModulator.a, 0.0, hide);
    fragColor = vec4(mcsm_story_grade(mix(dome, body, isBody)), mix(1.0, a, isBody));
}
