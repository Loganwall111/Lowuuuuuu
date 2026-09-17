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
const vec3 PHASE5_TEAL[32] = vec3[](
    vec3(0.047, 0.071, 0.086), vec3(0.050, 0.075, 0.091), vec3(0.053, 0.079, 0.095),
    vec3(0.055, 0.083, 0.100), vec3(0.058, 0.087, 0.104), vec3(0.061, 0.091, 0.109),
    vec3(0.064, 0.095, 0.114), vec3(0.067, 0.099, 0.118), vec3(0.069, 0.103, 0.123),
    vec3(0.072, 0.107, 0.127), vec3(0.075, 0.111, 0.132), vec3(0.078, 0.115, 0.136),
    vec3(0.080, 0.119, 0.141), vec3(0.083, 0.123, 0.145), vec3(0.086, 0.127, 0.150),
    vec3(0.089, 0.131, 0.155), vec3(0.091, 0.135, 0.158), vec3(0.094, 0.138, 0.161),
    vec3(0.096, 0.141, 0.164), vec3(0.098, 0.144, 0.167), vec3(0.100, 0.147, 0.171),
    vec3(0.103, 0.150, 0.174), vec3(0.105, 0.153, 0.177), vec3(0.107, 0.156, 0.180),
    vec3(0.110, 0.159, 0.183), vec3(0.112, 0.162, 0.186), vec3(0.114, 0.165, 0.189),
    vec3(0.116, 0.168, 0.192), vec3(0.119, 0.171, 0.195), vec3(0.121, 0.174, 0.198),
    vec3(0.123, 0.177, 0.201), vec3(0.125, 0.180, 0.204)
);
const vec3 PHASE55_PUR[32] = vec3[](
    vec3(0.086, 0.039, 0.129), vec3(0.095, 0.043, 0.141), vec3(0.104, 0.046, 0.152),
    vec3(0.114, 0.050, 0.164), vec3(0.123, 0.053, 0.175), vec3(0.132, 0.057, 0.186),
    vec3(0.141, 0.060, 0.198), vec3(0.150, 0.064, 0.209), vec3(0.159, 0.068, 0.220),
    vec3(0.168, 0.071, 0.232), vec3(0.177, 0.075, 0.243), vec3(0.186, 0.078, 0.255),
    vec3(0.196, 0.082, 0.266), vec3(0.205, 0.085, 0.277), vec3(0.214, 0.089, 0.289),
    vec3(0.223, 0.092, 0.300), vec3(0.231, 0.096, 0.311), vec3(0.240, 0.099, 0.320),
    vec3(0.248, 0.102, 0.330), vec3(0.256, 0.105, 0.340), vec3(0.264, 0.108, 0.349),
    vec3(0.272, 0.111, 0.359), vec3(0.280, 0.114, 0.368), vec3(0.288, 0.117, 0.378),
    vec3(0.296, 0.120, 0.388), vec3(0.304, 0.123, 0.397), vec3(0.312, 0.126, 0.407),
    vec3(0.321, 0.129, 0.416), vec3(0.329, 0.132, 0.426), vec3(0.337, 0.135, 0.436),
    vec3(0.345, 0.138, 0.445), vec3(0.353, 0.141, 0.455)
);
const vec3 PHASE6_ROSE[32] = vec3[](
    vec3(0.114, 0.082, 0.098), vec3(0.123, 0.088, 0.106), vec3(0.132, 0.094, 0.113),
    vec3(0.142, 0.101, 0.121), vec3(0.151, 0.107, 0.128), vec3(0.161, 0.113, 0.136),
    vec3(0.170, 0.119, 0.144), vec3(0.179, 0.125, 0.151), vec3(0.189, 0.131, 0.159),
    vec3(0.198, 0.137, 0.166), vec3(0.207, 0.143, 0.174), vec3(0.217, 0.149, 0.182),
    vec3(0.226, 0.155, 0.189), vec3(0.235, 0.161, 0.197), vec3(0.245, 0.167, 0.204),
    vec3(0.254, 0.173, 0.212), vec3(0.263, 0.179, 0.219), vec3(0.272, 0.185, 0.227),
    vec3(0.280, 0.190, 0.234), vec3(0.289, 0.196, 0.241), vec3(0.298, 0.202, 0.249),
    vec3(0.306, 0.207, 0.256), vec3(0.315, 0.213, 0.263), vec3(0.323, 0.218, 0.271),
    vec3(0.332, 0.224, 0.278), vec3(0.341, 0.229, 0.285), vec3(0.349, 0.235, 0.293),
    vec3(0.358, 0.240, 0.300), vec3(0.366, 0.246, 0.307), vec3(0.375, 0.252, 0.315),
    vec3(0.384, 0.257, 0.322), vec3(0.392, 0.263, 0.329)
);

// continuation past the supplied sheets: phase 7-8 keeps falling toward the
// ember/black end of the storyboard (sampled from the sunset trace plus the
// established phase-8 dark red), so the fade never stops mid-air.
const vec3 EMBER_END[32] = vec3[](
    vec3(0.118, 0.329, 0.369), vec3(0.136, 0.300, 0.345), vec3(0.154, 0.271, 0.321),
    vec3(0.172, 0.242, 0.297), vec3(0.190, 0.214, 0.273), vec3(0.208, 0.185, 0.249),
    vec3(0.226, 0.156, 0.225), vec3(0.248, 0.147, 0.215), vec3(0.270, 0.144, 0.208),
    vec3(0.293, 0.141, 0.202), vec3(0.315, 0.138, 0.195), vec3(0.338, 0.135, 0.189),
    vec3(0.360, 0.131, 0.183), vec3(0.409, 0.140, 0.172), vec3(0.476, 0.156, 0.158),
    vec3(0.543, 0.172, 0.144), vec3(0.610, 0.188, 0.130), vec3(0.677, 0.204, 0.116),
    vec3(0.744, 0.220, 0.102), vec3(0.791, 0.234, 0.092), vec3(0.810, 0.244, 0.089),
    vec3(0.829, 0.253, 0.085), vec3(0.848, 0.263, 0.081), vec3(0.866, 0.273, 0.077),
    vec3(0.885, 0.282, 0.073), vec3(0.889, 0.286, 0.069), vec3(0.832, 0.265, 0.062),
    vec3(0.776, 0.244, 0.056), vec3(0.719, 0.223, 0.049), vec3(0.663, 0.202, 0.043),
    vec3(0.606, 0.181, 0.036), vec3(0.550, 0.160, 0.030)
);

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
// BUILD #476 -- "real skies, 1:1 with the stills."
//
// The three sheet columns are 32 rows now (palette_tables.STOPS): one stop per
// sampled row of the sheet, written by ci/trace_sky_sheets.py --from-hex (or,
// when the sheets are present, traced straight out of them). Six stops could only
// ever be the artist's three anchors and a straight line between them; 32 keeps
// the column the sheet actually shows. This is the sampler for them.
vec3 mcsm_sky_column32(const vec3[32] col, float t) {
    float u = clamp(t, 0.0, 1.0) * 31.0;
    int i = int(floor(u));
    float f = u - floor(u);
    if (i >= 31) {
        return col[31];
    }
    if (i < 0) {
        return col[0];
    }
    return mix(col[i], col[i + 1], f);
}

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
    vec3 teal = mcsm_sky_column32(PHASE5_TEAL, t);
    vec3 pur = mcsm_sky_column32(PHASE55_PUR, t);
    vec3 rose = mcsm_sky_column32(PHASE6_ROSE, t);
    vec3 ember = mcsm_sky_column32(EMBER_END, t);

    // BUILD #430 -- ONE nested easing, the same three ramps the pack's position
    // program, McsmStormPhase.java and McsmBackdropPalette.java use, all of them
    // expanded from the supplied sheets' anchors.
    //
    // The branchy version this replaces ran the purple->rose easing across
    // 5.75-6.05 but stopped TAKING that branch at p = 5.9: crossing 5.9 snapped
    // the sky from a half-blended purple/rose to a flat rose, i.e. it changed
    // band a sixth of a phase early, in one frame, with no blend. That is one of
    // the "the colours haven't really changed at all" seams.
    vec3 col = mix(mix(teal, pur, mcsm_ramp(p, 5.1, 5.5)), rose, mcsm_ramp(p, 5.75, 6.05));
    return mix(col, ember, mcsm_ramp(p, 7.0, 8.05));
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

    // ========================================================================
    //  V2 - BLACK HOLE BACKDROP DYNAMIC SKYBOX + ANIMATED SKYBOXES
    //  Real distortion lensing centered middle growing bigger perspective
    //  Interactive enterable - turn some sift skyboxes to similar
    //  Majestic insane VFX - full sky not bands
    // ========================================================================
    
    // Animated skyboxes - hue shift over time for insane VFX
    float v2Time = clock * 0.02;
    float hueShift = sin(v2Time) * 0.05;
    // Slight rainbow shift for entire sky - majestic
    col.r += hueShift * 0.1;
    col.b -= hueShift * 0.05;
    col = clamp(col, 0.0, 1.0);

    // Black hole backdrop - centered middle, growing bigger perspective
    // Detect void/sift dimension via fog color or phase - use time as growth
    // Black hole at zenith (0,1,0) - centered middle
    vec3 blackHoleDir = vec3(0.0, 1.0, 0.0); // centered middle
    float bhDot = dot(ray, blackHoleDir); // 1 = looking directly at black hole
    
    // Growing bigger perspective - size increases with time
    float bhGrowth = 0.3 + sin(v2Time * 0.3) * 0.05 + fract(v2Time * 0.0005) * 0.8; // 0.3 to 1.1 growing
    float bhSize = bhGrowth;
    float bhInnerSize = bhSize * 0.4; // event horizon - pitch black
    float bhPhotonRing = bhSize * 0.45; // photon ring
    float bhAccretionDisk = bhSize * 1.2; // accretion disk outer
    
    float bhDist = acos(clamp(bhDot, -1.0, 1.0)); // angular distance from center
    
    // Only show black hole when looking up (up > 0.3) or in void/sift (phase < 1 or fog dark)
    float voidFactor = 1.0;
    // If in overworld day, make black hole subtle; if night or void, strong
    if (p < 4.4) {
        // Check if night - sunY < 0 means night
        float day01 = fract(clock / 1200.0);
        float sunY = mcsm_sun_true(day01).y;
        voidFactor = sunY < 0.0 ? 0.8 : 0.15; // night = visible, day = subtle
        // Also if fog is dark purple (void), boost
        float fogDark = 1.0 - (FogColor.r + FogColor.g + FogColor.b) / 3.0;
        voidFactor = max(voidFactor, fogDark * 0.9);
    } else {
        voidFactor = 0.9; // storm sky - black hole visible
    }

    if (bhDist < bhAccretionDisk && up > -0.2 && voidFactor > 0.1) {
        // Gravitational lensing - bend light around black hole
        float lensStrength = 0.15 / (0.05 + bhDist * 2.0);
        // Distort background color via lensing
        float lensedT = t + lensStrength * 0.1 * sin(bhDist * 20.0 + v2Time * 2.0);
        vec3 lensedCol = p > 4.4 ? mcsm_sky_reference(lensedT, p) : mcsm_sky_regular(lensedT, clock);
        
        // Blend lensed background
        float lensBlend = smoothstep(bhAccretionDisk, bhAccretionDisk * 0.7, bhDist) * 0.6;
        col = mix(col, lensedCol * 1.2, lensBlend * voidFactor);

        // Event horizon - pitch black center, enterable
        if (bhDist < bhInnerSize) {
            float horizonFade = smoothstep(bhInnerSize, bhInnerSize * 0.8, bhDist);
            // Pitch black with subtle purple edge - singularity
            vec3 black = vec3(0.01, 0.005, 0.02) * (1.0 - horizonFade);
            // Interactive enterable glow when looking directly
            float enterGlow = pow(max(bhDot, 0.0), 80.0) * 0.5 * (0.8 + sin(v2Time * 3.0) * 0.2);
            black += vec3(0.6, 0.2, 1.0) * enterGlow;
            col = mix(col, black, (1.0 - horizonFade * 0.3) * voidFactor);
        }
        // Photon ring - bright white/yellow ring around event horizon
        else if (bhDist < bhPhotonRing) {
            float ring = smoothstep(bhInnerSize, bhPhotonRing, bhDist) * (1.0 - smoothstep(bhPhotonRing, bhPhotonRing * 1.05, bhDist));
            vec3 photonColor = vec3(1.0, 0.9, 0.6) * ring * 2.5;
            // Rainbow shifting photon ring
            float ringHue = fract(v2Time * 0.1 + bhDist * 2.0);
            vec3 rainbowRing = vec3(0.5 + 0.5 * sin(ringHue * 6.2831), 0.5 + 0.5 * sin(ringHue * 6.2831 + 2.0), 0.5 + 0.5 * sin(ringHue * 6.2831 + 4.0));
            photonColor = mix(photonColor, rainbowRing * ring * 1.5, 0.6);
            col += photonColor * voidFactor;
        }
        // Accretion disk - rotating rainbow disk
        else if (bhDist < bhAccretionDisk) {
            float diskT = (bhDist - bhPhotonRing) / (bhAccretionDisk - bhPhotonRing); // 0 at inner, 1 at outer
            float angle = atan(ray.x, ray.z) + v2Time * (1.5 - diskT); // rotate faster inner
            float diskPattern = sin(angle * 3.0 + diskT * 10.0) * 0.5 + 0.5;
            diskPattern *= sin(angle * 7.0 - v2Time * 2.0) * 0.3 + 0.7;
            
            // Rainbow accretion disk - teal to magenta to orange
            float hue = fract(angle / 6.2831 + v2Time * 0.05 + diskT * 0.3);
            vec3 diskColor = vec3(0.5 + 0.5 * sin(hue * 6.2831), 0.5 + 0.5 * sin(hue * 6.2831 + 2.094), 0.5 + 0.5 * sin(hue * 6.2831 + 4.188));
            diskColor *= (1.0 - diskT * 0.5) * (0.8 + diskPattern * 0.4);
            
            // Brightness falloff
            float diskAlpha = (1.0 - diskT) * 0.8 * smoothstep(bhPhotonRing, bhPhotonRing * 1.1, bhDist);
            // Only show disk when not edge-on (up component)
            diskAlpha *= smoothstep(-0.2, 0.3, up) * 0.8 + 0.2;
            
            col = mix(col, col + diskColor * 1.5, diskAlpha * voidFactor);
        }

        // Lensed starlight - stars bent around black hole
        float starLensing = smoothstep(bhAccretionDisk, bhAccretionDisk * 1.5, bhDist) * (1.0 - smoothstep(bhAccretionDisk * 1.5, bhAccretionDisk * 2.0, bhDist));
        if (starLensing > 0.01) {
            float starAngle = atan(ray.x, ray.z) * 5.0;
            float stars = sin(starAngle + v2Time) * cos(starAngle * 1.3 - v2Time * 0.7);
            stars = pow(max(stars, 0.0), 8.0) * starLensing;
            col += vec3(1.0, 0.9, 0.8) * stars * 0.6 * voidFactor;
        }
    }

    // Enhance existing side boxes - currently only bands not entire sky
    // Now full sky: add additional color layers for full 360 coverage
    // Sift skyboxes turned to black hole similar - add subtle second black hole at opposite side
    vec3 secondBHDir = vec3(0.0, -0.6, 0.0); // below
    float secondDot = dot(ray, secondBHDir);
    float secondDist = acos(clamp(secondDot, -1.0, 1.0));
    float secondSize = bhSize * 0.6;
    if (secondDist < secondSize * 1.2 && up < 0.2 && voidFactor > 0.3) {
        float secondFade = 1.0 - smoothstep(secondSize * 0.5, secondSize * 1.2, secondDist);
        vec3 secondCol = vec3(0.15, 0.05, 0.25) * secondFade * 0.5;
        // Iridescent shimmer for bottom fabric sky
        secondCol += vec3(0.8, 0.3, 0.6) * secondFade * 0.2 * sin(v2Time * 2.0 + secondDist * 10.0) * 0.5 + 0.5;
        col = mix(col, col + secondCol, secondFade * voidFactor * 0.6);
    }

    // ---- story grade ------------------------------------------------------
    col = mcsm_story_grade(col);

    fragColor = vec4(max(col, vec3(0.0)), 1.0);
}
