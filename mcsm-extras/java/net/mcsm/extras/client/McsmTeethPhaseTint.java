package net.mcsm.extras.client;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.StormSkins;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;

/**
 * Drives model teeth/eye glow colours from the nearest storm phase.
 *
 * BUILD #416 -- THE TEETH AND THE AURA ARE TWO DIFFERENT COLOURS.
 *
 * The user's spec, in order, phase 4 -> 8:
 *   phase 4         white teeth, BLUISH aura
 *   phase 5         pure white glowing teeth (white aura)
 *   phase 5.2-5.9   glowing white teeth, BLUISH aura around them
 *   phase 6         white teeth, PURE BLUE aura
 *   phase 7         white teeth, TOXIC GREEN aura
 *   phase 8         pure white teeth, BLUE aura
 *
 * So the teeth are WHITE at every phase from 4 up, and the AURA is the part
 * that changes colour. The old table made the teeth themselves cyan / blue /
 * green, which is why the mouths never matched the reference frames.
 *
 * Two tracks, both fed from here so Java, the core shaders (mcsm_teeth_color /
 * mcsm_aura_color in mcsm_visuals.glsl), the storm-glow pool and the Iris pack
 * cannot disagree:
 *   TEETH -> DabyWSClientConfig.eyeColorR/G/B (the emissive model layers)
 *   AURA  -> shader side, from the phase; also used by our own glow emitters
 */
public final class McsmTeethPhaseTint {

    private McsmTeethPhaseTint() {
    }

    /**
     * THE TEETH TRACK -- {red, green, blue, glowIntensity}, one row per band.
     * V2 revamp per user: teeth should be pink/purple, bluish-purple, not white.
     * User: "teeth should look pink and purple, bluish purple"
     */
    private static final float[][] PHASE_TRACK = {
        //  r      g      b      intensity
        { 0.98F, 0.82F, 0.96F, 0.00F },  // phase 3-  no glowing teeth - soft pink
        { 1.00F, 0.45F, 0.85F, 4.20F },  // phase 4   pink teeth - bluish purple aura
        { 1.00F, 0.35F, 0.90F, 4.50F },  // phase 5   hot pink teeth - brightest
        { 0.85F, 0.40F, 1.00F, 4.80F },  // phase 5.2 purple-pink glowing
        { 0.70F, 0.35F, 1.00F, 5.00F },  // phase 6   bluish-purple teeth
        { 0.90F, 0.30F, 0.95F, 5.20F },  // phase 7   magenta-pink teeth
        { 0.80F, 0.40F, 1.00F, 5.50F },  // phase 8   pink-purple final - epic
    };

    /**
     * THE AURA TRACK -- the colour AROUND the teeth, same row indexing.
     * V2 revamp: sides should be BLUE not purple per user.
     * User: "sides of it to have BL is currently a tint on it but it's purple I would like you to change it to blue"
     * So aura = blue for all phases
     */
    private static final float[][] AURA_TRACK = {
        { 0.20F, 0.45F, 1.00F },  // phase 3-  blue
        { 0.25F, 0.50F, 1.00F },  // phase 4   blue aura
        { 0.30F, 0.55F, 1.00F },  // phase 5   bright blue
        { 0.22F, 0.48F, 1.00F },  // phase 5.2 blue
        { 0.18F, 0.42F, 1.00F },  // phase 6   pure blue
        { 0.20F, 0.50F, 1.00F },  // phase 7   blue (was toxic green)
        { 0.25F, 0.55F, 1.00F },  // phase 8   blue
    };

    /**
     * Index into the tracks for an evolution phase. The 5.2 boundary is the
     * user's own: the bluish aura comes back at 5.2 and holds to 5.9.
     */
    public static int trackIndex(double phase) {
        if (phase >= 8.0D) return 6;
        if (phase >= 7.0D) return 5;
        if (phase >= 6.0D) return 4;
        if (phase >= 5.2D) return 3;
        if (phase >= 5.0D) return 2;
        if (phase >= 4.0D) return 1;
        return 0;
    }

    /** {r, g, b, intensity} teeth colour for the nearest storm's phase. */
    public static float[] track(double phase) {
        return PHASE_TRACK[trackIndex(phase)];
    }

    /**
     * {r, g, b} aura colour for a phase, CROSS-FADED on exactly the boundaries
     * mcsm_aura_color() uses in the shaders so the mouth squares, the model's
     * emissive layers and the glow pool never step at a different moment:
     *   5.00 pure white is reached exactly at phase 5 and holds to 5.15,
     *   the bluish aura is back by 5.25, pure blue at 6.0, green at 7.0,
     *   blue at 8.0.
     */
    public static float[] aura(double phase) {
        if (phase < 4.0D) {
            return new float[]{0.0F, 0.0F, 0.0F};
        }
        float[] c = {AURA_TRACK[1][0], AURA_TRACK[1][1], AURA_TRACK[1][2]};   // bluish
        toward(c, AURA_TRACK[2], ramp(phase, 4.92D, 5.00D));                  // pure white
        toward(c, AURA_TRACK[3], ramp(phase, 5.15D, 5.25D));                  // bluish again
        toward(c, AURA_TRACK[4], ramp(phase, 5.85D, 6.00D));                  // pure blue
        toward(c, AURA_TRACK[5], ramp(phase, 6.90D, 7.05D));                  // toxic green
        toward(c, AURA_TRACK[6], ramp(phase, 7.90D, 8.00D));                  // blue
        return c;
    }

    /**
     * The pupil / emitter cube that floats above each mouth: MAGENTA in every
     * phase. The reference frames keep it constant while everything else about
     * the mouth shifts, so it is a fixed colour rather than a track, and it is
     * single-sourced here so our own emitters and the model agree.
     */
    public static final int PUPIL_R = 232;
    public static final int PUPIL_G = 40;
    public static final int PUPIL_B = 255;

    /**
     * THE EYE TRACK -- V2 revamp: eyes should be PINK per user.
     * User: "eyes should be pink" + bloom effect
     */
    private static final float[][] EYE_TRACK = {
        { 1.00F, 0.40F, 0.70F },  // phase 3-  pink
        { 1.00F, 0.35F, 0.75F },  // phase 4   pink
        { 1.00F, 0.30F, 0.80F },  // phase 5   hot pink
        { 1.00F, 0.25F, 0.85F },  // phase 5.2 magenta-pink
        { 1.00F, 0.35F, 0.78F },  // phase 6   pink-purple
        { 1.00F, 0.30F, 0.82F },  // phase 7   pink
        { 1.00F, 0.40F, 0.75F },  // phase 8   soft pink final
    };

    /** The eye glow right now, cross-faded on the aura's own boundaries. */
    public static float[] eye(double phase) {
        if (phase < 4.0D) {
            return new float[]{0.0F, 0.0F, 0.0F};
        }
        float[] c = {EYE_TRACK[1][0], EYE_TRACK[1][1], EYE_TRACK[1][2]};   // violet
        toward(c, EYE_TRACK[2], ramp(phase, 4.92D, 5.00D));                // pale violet
        toward(c, EYE_TRACK[3], ramp(phase, 5.15D, 5.25D));                // magenta
        toward(c, EYE_TRACK[4], ramp(phase, 5.85D, 6.00D));                // bluish violet
        toward(c, EYE_TRACK[5], ramp(phase, 6.90D, 7.05D));                // magenta
        toward(c, EYE_TRACK[6], ramp(phase, 7.90D, 8.00D));                // blue violet
        return c;
    }

    /** The eye glow right now, packed ARGB -- for our own emitters. */
    public static int eyeArgb() {
        float[] e = eye(nearestPhase());
        return rgb(e[0], e[1], e[2]);
    }

    private static void toward(float[] c, float[] target, float w) {
        float t = Math.max(0.0F, Math.min(1.0F, w));
        for (int i = 0; i < 3; i++) {
            c[i] = c[i] + (target[i] - c[i]) * t;
        }
    }

    private static float ramp(double v, double lo, double hi) {
        if (hi <= lo) {
            return v >= hi ? 1.0F : 0.0F;
        }
        double t = Math.max(0.0D, Math.min(1.0D, (v - lo) / (hi - lo)));
        return (float) (t * t * (3.0D - 2.0D * t));
    }

    /** The aura colour right now, packed ARGB -- for our own glow emitters. */
    public static int auraArgb() {
        float[] a = aura(nearestPhase());
        return rgb(a[0], a[1], a[2]);
    }

    /** {r, g, b} aura colour right now. */
    public static float[] auraNow() {
        return aura(nearestPhase());
    }

    private static double nearestPhase() {
        // Use the renderer's phase hint as well as the distant-storm tracker;
        // local storms do not always publish a DistantStormData entry.
        return Math.max(StormSkins.phaseHint(), McsmStormAtmosphere.nearestPhase());
    }

    /** Packed full-bright ARGB used by the exact native teeth hook. */
    public static int teethTintArgb() {
        float[] t = track(nearestPhase());
        return rgb(t[0], t[1], t[2]);
    }

    /**
     * Packed full-bright ARGB used by the exact native eye hook.
     *
     * BUILD #416 fix: this used to return the TEETH track, so the moment the
     * teeth went white the eyes went white with them and the show's violet eye
     * glow disappeared. The eyes use the EYE track, lifted like the base
     * renderer lifts the beam colour so the lenses still read as emitters.
     */
    public static int eyeTintArgb() {
        float[] e = eye(nearestPhase());
        float mx = Math.max(0.001F, Math.max(e[0], Math.max(e[1], e[2])));
        float lift = 0.94F / mx;
        return rgb(Math.min(1.0F, e[0] * lift), Math.min(1.0F, e[1] * lift),
                Math.min(1.0F, e[2] * lift));
    }

    private static int rgb(float r, float g, float b) {
        int ir = Math.max(0, Math.min(255, Math.round(r * 255.0F)));
        int ig = Math.max(0, Math.min(255, Math.round(g * 255.0F)));
        int ib = Math.max(0, Math.min(255, Math.round(b * 255.0F)));
        return 0xFF000000 | ir << 16 | ig << 8 | ib;
    }

    /**
     * The head eyes are a separate switch on the base mod's client config and it
     * is set by name, so a build that does not carry it simply skips this rather
     * than failing to load.
     */
    private static void mcsm$forceEyeGlow() {
        try {
            java.lang.reflect.Field f =
                    net.dabicco.witherstormmod.config.DabyWSClientConfig.class
                            .getField("headEyeGlow");
            f.setBoolean(null, true);
        } catch (Throwable ignored) {
            // no such switch on this build: the teeth pass is the one that matters
        }
    }

    public static void tick() {
        try {
            net.mcsm.extras.client.McsmStormAtmosphere.tick();
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null) {
                return;
            }
            float phase = 0.0F;
            for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                if (d.phase > phase) {
                    phase = d.phase;
                }
            }
            net.dabicco.witherstormmod.client.StormSkins.setPhaseHint(phase);
            if (phase < 0.5F) {
                return;
            }
            // MCSM 1.9.201 / BUILD #415 -- TRUE PHASE-SHIFTING TEETH GLOW
            // TRACK, now read straight out of the single canonical palette
            // above instead of a second hand-typed copy of the same if-chain.
            // The shader passes re-key the native emissive layer every tick.
            float[] t = track(phase);
            float r = t[0];
            float g = t[1];
            float b = t[2];
            float inten = t[3];
            boolean glow = inten > 0.0F;
            DabyWSClientConfig.eyeColorR = r;
            DabyWSClientConfig.eyeColorG = g;
            DabyWSClientConfig.eyeColorB = b;
            DabyWSClientConfig.turquoiseTeethIntensity = inten;
            DabyWSClientConfig.turquoiseTeeth = glow;
            // BUILD #422 -- THE GLOW IS GUARANTEED, NOT INFERRED.
            //
            // "For some reason the teeth and the eyes are still not glowing."
            // Two things were between the emissive atlases and the screen: the
            // atlases themselves (dim, and phase 4 nearly empty -- both fixed in
            // ci/make_emissive_whites.py, they are pure white masks now) and this
            // switch, which anything else in the mod can write. From phase 4 up
            // the module (a) forces the native emissive pass back on every tick
            // and (b) floors its intensity at the brief's 4.0x, so no other pass
            // -- and no config file written by an older build -- can quietly
            // leave a storm with no glow. Below phase 4 nothing is forced: no
            // glowing teeth is correct there, and that is what the user's own
            // phase table asks for.
            if (phase >= 4.0F) {
                DabyWSClientConfig.turquoiseTeeth = true;
                DabyWSClientConfig.turquoiseTeethIntensity = Math.max(inten, 4.0F);
                mcsm$forceEyeGlow();
            }
            // BUILD #405 user override: phase 6 beams read bluish (reference
            // close-up frames); every other phase keeps show purple.
            // BUILD #415 FIX: there used to be a SECOND beam block below this
            // one that unconditionally re-stamped the beams with the teeth
            // track (#00A877 sea-green from phase 6 up). It ran last, so it won
            // and every phase flew green tractor beams instead of the show's
            // purple cones. The beams now follow the storyboard: purple core
            // (#8C26FF) everywhere, bluish through phase 6 only. The lower
            // auxiliary spotlight emitters are separated out in
            // StormImpactLights as cosmic blue #4D4DFF.
            // V2 revamp: sides blue per user, not purple
            if (true) {
                DabyWSClientConfig.beamColorR = 0.20F;
                DabyWSClientConfig.beamColorG = 0.45F;
                DabyWSClientConfig.beamColorB = 1.00F;
            }
            if (phase >= 4.0F) {
                // V2 bloom effect over wither storm per user
                DabyWSClientConfig.headEyeGlow = true;
                DabyWSClientConfig.glowStrength = Math.max(DabyWSClientConfig.glowStrength, 4.5F); // bloom epic
            }

        } catch (Throwable ignored) {
        }
    }
}
