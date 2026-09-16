#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:mcsm_visuals.glsl>

// ============================================================================
//  Devouring Storms - sky.fsh   [BUILD #416 - real fading sky, no domes]
//
//  WHAT CHANGED AND WHY
//  --------------------
//  Every previous build reached the storm sky by SUBMITTING GEOMETRY: a dome,
//  a world-space quad, a camera-locked card, or a stack of flat "sticker"
//  sheets. All of them are shapes, and every shape has an edge - which is why
//  the screenshots kept showing a horizon line, a clipped strip and a sky with
//  a visible "top". 26.2 renders the sky through this program, so the sky is
//  now painted as a FUNCTION OF THE VIEW RAY: no geometry, no silhouette, no
//  edge is possible. Zenith -> horizon is one continuous fade.
//
//  THE THREE REFERENCE GRADIENTS (user-supplied artist sheets, re-traced to
//  stop tables so the exact colours survive - the tables below are the sampled
//  values, t = 0 at the zenith, t = 1 at the horizon):
//      PHASE5_TEAL   "phase 5 turquoise sky.png"          -> phase 5.0 - 5.1
//      PHASE55_PUR   "phase5sky0purple sky.png"           -> phase 5.5 - 5.9
//      PHASE6_ROSE   "phase6sky 6 witherstorm.png"        -> phase 6.0 - 7.0
//  The order and the phase windows are exactly as specified: the green/teal
//  sheet owns 5.0-5.1, purple/black/magenta owns 5.5-5.9, and the black-smudge
//  purple/magenta/pink/orange/salmon sheet owns 6.0 and up.
//
//  Vanilla: the regular day/night cycle is now a real gradient too (traced
//  day / midnight / sunset columns cross-faded by true sun elevation), so the
//  sky fades everywhere, storm or not.
//
//  Bodies: stars and the sunrise/sunset fan are suppressed by the Java state
//  hook once the storm matures, so the only sky content is the fade itself.
//
//  NO CUSTOM UNIFORM IS DECLARED HERE. A core pipeline's bind group is fixed
//  by the vanilla layout; declaring a uniform the layout does not carry is a
//  hard Vulkan crash (see storm_glow.fsh). The phase therefore arrives through
//  the Fog block (FogSkyEnd carrier) - see mcsm_witherstorm_phase() - and Iris
//  packs additionally get the real `witherstorm_Phase` uniform.
// ============================================================================

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec3 mcsmCamRay;

out vec4 fragColor;

// ---- reference gradients: index 0 = zenith (t=0), index 5 = horizon (t=1) --
// Traced from the three attached artist sheets; values are 0..1.
const vec3 PHASE5_TEAL[6] = vec3[](
    vec3(0.078, 0.149, 0.157), vec3(0.140, 0.220, 0.215), vec3(0.290, 0.360, 0.350),
    vec3(0.470, 0.530, 0.505), vec3(0.600, 0.660, 0.615), vec3(0.620, 0.675, 0.625));
const vec3 PHASE55_PUR[6] = vec3[](
    vec3(0.165, 0.059, 0.196), vec3(0.240, 0.100, 0.270), vec3(0.361, 0.165, 0.400),
    vec3(0.525, 0.275, 0.557), vec3(0.640, 0.345, 0.690), vec3(0.660, 0.361, 0.706));
const vec3 PHASE6_ROSE[6] = vec3[](
    vec3(0.329, 0.282, 0.329), vec3(0.400, 0.340, 0.380), vec3(0.478, 0.392, 0.439),
    vec3(0.612, 0.510, 0.533), vec3(0.760, 0.600, 0.640), vec3(0.784, 0.612, 0.651));

// continuation past the supplied sheets: phase 7-8 keeps falling toward the
// ember/black end of the storyboard (sampled from the sunset trace plus the
// established phase-8 dark red), so the fade never stops mid-air.
const vec3 EMBER_END[6] = vec3[](
    vec3(0.118, 0.329, 0.369), vec3(0.230, 0.150, 0.220), vec3(0.369, 0.130, 0.180),
    vec3(0.784, 0.230, 0.094), vec3(0.900, 0.290, 0.070), vec3(0.550, 0.160, 0.030));

// ---- regular cycle (no storm): traced day / midnight / sunset -------------
const vec3 SKY_DAY[6] = vec3[](
    vec3(0.482, 0.482, 0.878), vec3(0.520, 0.510, 0.900), vec3(0.610, 0.560, 0.930),
    vec3(0.760, 0.660, 0.950), vec3(0.851, 0.714, 0.961), vec3(0.867, 0.737, 0.969));
const vec3 SKY_NIGHT[6] = vec3[](
    vec3(0.051, 0.051, 0.157), vec3(0.090, 0.100, 0.260), vec3(0.137, 0.161, 0.408),
    vec3(0.200, 0.235, 0.620), vec3(0.290, 0.353, 0.831), vec3(0.230, 0.260, 0.640));
const vec3 SKY_SUNSET[6] = vec3[](
    vec3(0.118, 0.329, 0.369), vec3(0.230, 0.360, 0.380), vec3(0.369, 0.416, 0.400),
    vec3(0.784, 0.345, 0.094), vec3(0.973, 0.376, 0.031), vec3(0.431, 0.157, 0.212));

// Six-stop column sample: t = 1 - up, so t = 0 looks straight up and t = 1
// sits on the horizon. Below the horizon the ray keeps the horizon row (the
// fade continues into the fog instead of stopping at a line).
vec3 mcsm_sky_column(const vec3[6] col, float t) {
    float u = clamp(t, 0.0, 1.0) * 5.0;
    int i = int(floor(u));
    float f = u - floor(u);
    vec3 a;
    vec3 b;
    if (i <= 0)      { a = col[0]; b = col[1]; }
    else if (i == 1) { a = col[1]; b = col[2]; }
    else if (i == 2) { a = col[2]; b = col[3]; }
    else if (i == 3) { a = col[3]; b = col[4]; }
    else             { a = col[4]; b = col[5]; }
    return mix(a, b, f);
}

// The three supplied sheets, in the order the storyboard gives them.
vec3 mcsm_sky_reference(float t, float p) {
    vec3 teal = mcsm_sky_column(PHASE5_TEAL, t);
    vec3 pur = mcsm_sky_column(PHASE55_PUR, t);
    vec3 rose = mcsm_sky_column(PHASE6_ROSE, t);
    vec3 ember = mcsm_sky_column(EMBER_END, t);

    if (p < 5.1) return mix(teal, pur, mcsm_ramp(p, 5.1, 5.5));          // 5.0-5.1 teal, then toward purple
    if (p < 5.5) return mix(teal, pur, mcsm_ramp(p, 5.1, 5.5));          // 5.1-5.5 blend
    if (p < 5.9) return mix(pur, rose, mcsm_ramp(p, 5.75, 6.05));        // 5.5-5.9 purple, easing to rose
    if (p < 7.0) return mix(rose, ember, mcsm_ramp(p, 7.0, 8.05));       // 6.0-7.0 rose -> ember
    return mix(rose, ember, mcsm_ramp(p, 7.0, 8.05));
}

// Regular cycle: cross-fade day -> sunset -> night by true sun elevation, so
// the vanilla sky is a fade as well rather than a flat disc colour.
vec3 mcsm_sky_regular(float t, float clock) {
    float day01 = fract(clock / 1200.0);
    float sunY = mcsm_sun_true(day01).y;
    vec3 day = mcsm_sky_column(SKY_DAY, t);
    vec3 night = mcsm_sky_column(SKY_NIGHT, t);
    vec3 dusk = mcsm_sky_column(SKY_SUNSET, t);
    // dusk is strongest as the sun crosses the horizon, either way
    float duskW = 1.0 - clamp(abs(sunY) * 3.2, 0.0, 1.0);
    float dayW = clamp(sunY * 3.2, 0.0, 1.0);
    vec3 base = mix(night, day, dayW);
    return mix(base, dusk, duskW * 0.85);
}

void main() {
    vec3 ray = normalize(mcsmCamRay);
    float up = ray.y;
    float t = 1.0 - up;                    // 0 zenith .. 1 horizon .. >1 below
    float clock = mcsm_clock(GameTime);
    float p = mcsm_witherstorm_phase();

    vec3 col = p > 4.4
        ? mcsm_sky_reference(t, p)
        : mcsm_sky_regular(t, clock);

    // Rise out of the regular sky as the storm takes hold (phase 4.45-4.9) so
    // the handover is a fade, not a cut.
    if (p > 4.4 && p < 4.9) {
        col = mix(mcsm_sky_regular(t, clock), col, mcsm_ramp(p, 4.45, 4.9));
    }

    // ---- horizon match ----------------------------------------------------
    // Fold the bottom of the column into the live fog colour. The sky and the
    // distance fog then agree at the horizon and the seam disappears: this is
    // the fix for the razor line that every dome/card build drew.
    float seam = 1.0 - clamp(abs(up) * 7.0, 0.0, 1.0);
    col = mix(col, FogColor.rgb, seam * 0.85 * FogColor.a);

    // ---- horizon glow (blueprint term) ------------------------------------
    // A small additive lift around the horizon so the fade has air in it.
    float horizonGradient = 1.0 - max(up, 0.0);
    float glowW = pow(horizonGradient, 3.5);
    // BUILD #416 -- the horizon air is lit by the storm's AURA, not by the
    // teeth colour: blue at 6, toxic green at 7, blue again at 8.
    vec3 glowTint = p > 4.4 ? mcsm_aura_color(p) * 0.06 : vec3(0.04, 0.02, 0.06);
    col += glowTint * glowW;

    // ---- story grade ------------------------------------------------------
    col = mcsm_story_grade(col);

    fragColor = vec4(max(col, vec3(0.0)), 1.0);
}
