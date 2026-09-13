#version 330

// Alias for 26.2 position.fsh matching sky.fsh
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:mcsm_visuals.glsl>

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec3 mcsmCamRay;

out vec4 fragColor;

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

vec3 mcsm_storm_sky(float height, float p, vec3 worldDir) {
    float tY = smoothstep(-0.20, 0.80, height);
    if (p < 4.42) return vec3(-1.0);

    vec3 greenZenith  = vec3(0.094, 0.184, 0.180);
    vec3 greenHorizon = vec3(0.165, 0.294, 0.251);
    vec3 col45 = mix(greenHorizon, greenZenith, tY);

    vec3 slateZenith  = vec3(0.114, 0.169, 0.169); // #1D2B2B
    vec3 slateHorizon = vec3(0.431, 0.471, 0.451); // #6E7873
    vec3 col5 = mix(slateHorizon, slateZenith, tY);

    vec3 purpleZenith  = vec3(0.102, 0.039, 0.165); // #1A0A2A
    vec3 magentaHorizon= vec3(0.498, 0.227, 0.651); // #7F3AA6
    vec3 col55 = mix(magentaHorizon, purpleZenith, tY);

    float backHalo = pow(clamp(1.0 - abs(worldDir.y), 0.0, 1.0), 3.0);
    col55 += vec3(0.25, 0.22, 0.30) * backHalo;

    vec3 plumZenith  = vec3(0.259, 0.180, 0.231); // #422E3B
    vec3 peachHorizon= vec3(0.627, 0.459, 0.494); // #A0757E
    vec3 col6 = mix(peachHorizon, plumZenith, tY);

    float w45 = mcsm_ramp(p, 4.42, 4.52) * (1.0 - mcsm_ramp(p, 4.88, 4.98));
    float w5  = mcsm_ramp(p, 4.95, 5.05) * (1.0 - mcsm_ramp(p, 5.38, 5.48));
    float w55 = mcsm_ramp(p, 5.42, 5.52) * (1.0 - mcsm_ramp(p, 5.92, 6.00));
    float w6  = mcsm_ramp(p, 5.95, 6.08);

    vec3 sky = col45 * w45 + col5 * w5 + col55 * w55 + col6 * w6;

    float zenithMask = smoothstep(0.0, 0.60, height);
    vec3 topDark = mix(vec3(0.05, 0.02, 0.08), plumZenith, mcsm_ramp(p, 5.95, 6.10));
    sky = mix(sky, topDark, zenithMask * 0.85);

    return sky;
}

void main() {
    float mcsmP = mcsm_phase(FogSkyEnd, FogColor, FogRenderDistanceEnd);
    float clock = mcsm_clock(GameTime);
    vec3 worldDir = normalize(transpose(mat3(ModelViewMat)) * normalize(mcsmCamRay));
    float height = clamp(worldDir.y, -1.0, 1.0);

    float isBody = step(0.10, length(ColorModulator.rgb - FogColor.rgb));

    float mcsmDt = mcsm_death(FogSkyEnd);
    if (mcsmDt >= 0.0) {
        vec3 ddir = mcsm_death_dir(worldDir, mcsmDt, clock);
        vec3 ddome = mcsm_storm_sky(ddir.y, 7.6, ddir);
        if (ddome.r < 0.0) ddome = vec3(0.05, 0.02, 0.08);
        ddome *= 1.0 - 0.78 * mcsm_ramp(mcsmDt, 0.0, 0.55);
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
        float up = clamp(height * 0.5 + 0.5, 0.0, 1.0);
        vec3 sky = mcsm_sky6(SKY_DAY[0], SKY_DAY[1], SKY_DAY[2], SKY_DAY[3], SKY_DAY[4], SKY_DAY[5], up);
        fragColor = vec4(max(mcsm_story_grade(sky), vec3(0.0)), 1.0);
        return;
    }

    vec3 dome = mcsm_storm_sky(height, mcsmP, worldDir);
    if (dome.r < 0.0) {
        dome = vec3(0.05, 0.02, 0.08);
    }

    float hide = mcsm_ramp(mcsmP, 5.05, 5.20);
    vec3 body = mcsm_sky_body_tint(mcsmP, ColorModulator.rgb);
    float a = mix(ColorModulator.a, 0.0, hide);
    fragColor = vec4(mcsm_story_grade(mix(dome, body, isBody)), mix(1.0, a, isBody));
}
