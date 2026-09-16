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
    vec3(0.047, 0.071, 0.086), vec3(0.064, 0.096, 0.115), vec3(0.082, 0.121, 0.143),
    vec3(0.097, 0.143, 0.166), vec3(0.111, 0.162, 0.185), vec3(0.125, 0.180, 0.204)
);
const vec3 PHASE55_PUR[6] = vec3[](
    vec3(0.086, 0.039, 0.129), vec3(0.143, 0.061, 0.200), vec3(0.199, 0.083, 0.271),
    vec3(0.253, 0.104, 0.336), vec3(0.303, 0.122, 0.395), vec3(0.353, 0.141, 0.455)
);
const vec3 PHASE6_ROSE[6] = vec3[](
    vec3(0.114, 0.082, 0.098), vec3(0.172, 0.120, 0.145), vec3(0.230, 0.158, 0.192),
    vec3(0.285, 0.194, 0.238), vec3(0.339, 0.228, 0.284), vec3(0.392, 0.263, 0.329)
);

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

// ============================================================================
//  BUILD #422 -- THE FLOOR STRETCH.
//
//  t = 1 - up, so t = 1 is the horizon and t > 1 is BELOW the player: the lower
//  world void, all the way down past bedrock. The column used to clamp at the
//  horizon row (clamp(t, 0.0, 1.0)), which means the gradient stopped existing
//  the moment the ray tipped under the horizon -- and that is exactly the seam
//  the user kept seeing: "there's a top layer but there isn't a bottom layer".
//
//  The floor row is now stretched downward INFINITELY. It is one continuous
//  wall of the horizon colour with an asymptotic deepening (it settles at 0.86
//  of the horizon row and never reaches it), so there is no second band, no
//  second gradient and no edge anywhere under the player: the wall simply keeps
//  going as far down as the world does.
// ============================================================================
vec3 mcsm_sky_floor(vec3 horizonCol, float t) {
    float below = max(t - 1.0, 0.0);
    // 1 - 0.14 * (1 - e^-1.6d): d = 0 -> 1.0, d -> inf -> 0.86. Monotonic, so
    // the wall never bands and never returns to the sky above it.
    float deepen = 1.0 - 0.14 * (1.0 - exp(-below * 1.6));
    return horizonCol * deepen;
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

// ============================================================================
//  BUILD #422 -- THE TIME-SCALAR OVERLAY.
//
//  The world clock already reaches this program (GameTime -> mcsm_clock), and
//  the storm's own palettes are NEVER overwritten by it. What the clock does is
//  put a thin ambient film over whatever the active band is drawing:
//
//    * DAY   -- a very subtle WARM lift (a hair of gain plus a warm bias), so
//               daylight reads as daylight on the storm band and on the
//               ordinary sky.
//    * NIGHT -- a deep midnight-indigo (#050510) film over the canvas margins:
//               strongest at the zenith and at the bottom of the frame, zero
//               across the middle of the view, so the storm's colour survives
//               in the middle of the picture while the edges go to midnight.
//
//  Both are driven by the TRUE sun elevation (mcsm_sun_true), so `/time set day`
//  and `/time set midnight` move the overlay the instant they move the clock.
// ============================================================================
const vec3 MCSM_MIDNIGHT_INDIGO = vec3(0.0196, 0.0196, 0.0627);   // #050510

vec3 mcsm_time_overlay(vec3 col, float clock, float up) {
    float day01 = fract(clock / 1200.0);
    float sunY = mcsm_sun_true(day01).y;
    float dayW = clamp(sunY * 3.2, 0.0, 1.0);          // 1 = noon
    float nightW = clamp(-sunY * 3.2, 0.0, 1.0);       // 1 = midnight
    // very subtle warm ambient, mixed in as a gain + a warm bias
    col += col * (0.035 * dayW);
    col += vec3(0.020, 0.013, 0.004) * dayW;
    // midnight film on the canvas margins: 1 at the zenith, 1 at the very
    // bottom, 0 through the middle band of the view
    float margin = max(smoothstep(0.15, 0.85, up), smoothstep(-0.10, -0.75, up));
    col = mix(col, MCSM_MIDNIGHT_INDIGO, nightW * margin * 0.55);
    return col;
}

void main() {
    vec3 ray = normalize(mcsmCamRay);
    float clock = mcsm_clock(GameTime);
    float p = mcsm_witherstorm_phase();

    // ---- death cinematic ---------------------------------------------------
    // BUILD #416 -- THE JOIN THAT WAS MISSING. The Java driver has stamped the
    // 1906..2906 band (and kept the aim alive) since the phase-31 work, and the
    // whole death stack -- cracks, implosion, supernova rings, flash -- has sat in
    // mcsm_visuals.glsl with NO caller: nothing ever read the band, so the finale
    // had never once rendered. This is where it renders, and it is the cool-white
    // finish the brief asks for.
    //
    // dt < 0 means the band is not live and the frame is untouched, so the normal
    // sky (and every other path in this file) is bit-for-bit what it was.
    float dt = mcsm_death(FogSkyEnd);
    if (dt >= 0.0) {
        // Act I: space itself wobbles, applied to the SAMPLING direction so the
        // wobble is continuous across the whole sky rather than a screen effect.
        ray = mcsm_death_dir(ray, dt, clock);
    }
    // While the death band owns FogSkyEnd, the phase carrier is overwritten by dt,
    // so the phase falls back to the fog-colour signature -- which can drop to 0
    // (calm) for a frame in the middle of the white flash, snapping the sky to the
    // ordinary one mid-finale. The finale holds the LATE-STORM sky instead: the
    // storm is at its biggest when it dies, so the rose/ember end of the reference
    // table is where it belongs, and the sequence's own tail (which eases off from
    // dt ~0.92) is what hands the sky back as the carrier is released.
    if (dt >= 0.0) {
        p = max(p, 6.6);
    }
    float up = ray.y;
    float t = 1.0 - up;                    // 0 zenith .. 1 horizon .. >1 below

    vec3 col = p > 4.4
        ? mcsm_sky_reference(t, p)
        : mcsm_sky_regular(t, clock);

    // Rise out of the regular sky as the storm takes hold (phase 4.45-4.9) so
    // the handover is a fade, not a cut.
    if (p > 4.4 && p < 4.9) {
        col = mix(mcsm_sky_regular(t, clock), col, mcsm_ramp(p, 4.45, 4.9));
    }

    // ---- BUILD #422 -- the floor stretch ----------------------------------
    // Everything below the horizon keeps the horizon row, deepened with depth.
    // The floor is taken from the SAME band that owns the sky above it, so the
    // wall under the player is the storm's own colour and never a second
    // gradient: no seam, no bottom layer, nothing left for the vanilla sky to
    // bleed through.
    if (t > 1.0) {
        vec3 floorRow = p > 4.4 ? mcsm_sky_reference(1.0, p) : mcsm_sky_regular(1.0, clock);
        col = mcsm_sky_floor(floorRow, t);
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

    // ---- the death stack itself -------------------------------------------
    // Additive on top of the graded sky: white filaments crawling over the sky,
    // the implosion contracting toward where the storm died, the expanding cool
    // rings, and finally the whole-sky white spike. Every term is zero outside its
    // window, so this costs a few branches and changes nothing when dt < 0.
    if (dt >= 0.0) {
        vec3 bossDir = mcsm_death_aim();
        col += mcsm_death_cracks(ray, dt, clock);
        col += mcsm_death_implosion(ray, bossDir, dt, clock);
        col += mcsm_supernova(ray, bossDir, dt, clock);
        col *= 1.0 + mcsm_death_flash(dt);
    }

    // ---- BUILD #422 -- the ambient time scalar ----------------------------
    col = mcsm_time_overlay(col, clock, up);

    // ---- story grade ------------------------------------------------------
    col = mcsm_story_grade(col);

    fragColor = vec4(max(col, vec3(0.0)), 1.0);
}
