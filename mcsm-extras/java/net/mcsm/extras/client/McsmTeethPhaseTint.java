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
     * Index 0 is the non-glowing show default below phase 4. Every other row is
     * white: the teeth do NOT take the phase colour any more.
     */
    private static final float[][] PHASE_TRACK = {
        //  r      g      b      intensity
        { 0.98F, 0.98F, 0.86F, 0.00F },  // phase 3-  no glowing teeth
        { 1.00F, 1.00F, 1.00F, 3.60F },  // phase 4   white teeth
        { 1.00F, 1.00F, 1.00F, 3.70F },  // phase 5   pure white, brightest
        { 1.00F, 1.00F, 1.00F, 3.90F },  // phase 5.2 glowing white
        { 1.00F, 1.00F, 1.00F, 4.20F },  // phase 6   white teeth
        { 1.00F, 1.00F, 1.00F, 4.20F },  // phase 7   white teeth
        { 1.00F, 1.00F, 1.00F, 4.30F },  // phase 8   pure white
    };

    /**
     * THE AURA TRACK -- the colour AROUND the teeth, same row indexing.
     *   bluish (4) / white (5.0) / bluish (5.2-5.9) / pure blue (6) /
     *   toxic green (7) / blue (8)
     */
    private static final float[][] AURA_TRACK = {
        { 0.55F, 0.80F, 1.00F },  // phase 3-  (no glow; row kept for indexing)
        { 0.55F, 0.80F, 1.00F },  // phase 4   bluish aura
        { 1.00F, 1.00F, 1.00F },  // phase 5   white aura
        { 0.50F, 0.78F, 1.00F },  // phase 5.2 bluish aura around white teeth
        { 0.22F, 0.42F, 1.00F },  // phase 6   pure blue aura
        { 0.36F, 1.00F, 0.28F },  // phase 7   toxic green aura
        { 0.35F, 0.58F, 1.00F },  // phase 8   blue aura
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

    /** Packed full-bright ARGB used by the exact native eye hook. */
    public static int eyeTintArgb() {
        // Dedicated eyes stay on RenderType.eyes and use the same phase track
        // as the teeth, without inheriting world light or shadow attenuation.
        float[] t = track(nearestPhase());
        return rgb(t[0], t[1], t[2]);
    }

    private static int rgb(float r, float g, float b) {
        int ir = Math.max(0, Math.min(255, Math.round(r * 255.0F)));
        int ig = Math.max(0, Math.min(255, Math.round(g * 255.0F)));
        int ib = Math.max(0, Math.min(255, Math.round(b * 255.0F)));
        return 0xFF000000 | ir << 16 | ig << 8 | ib;
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
            if (phase >= 6.0F && phase < 7.0F) {
                DabyWSClientConfig.beamColorR = 0.30F;
                DabyWSClientConfig.beamColorG = 0.42F;
                DabyWSClientConfig.beamColorB = 1.00F;
            } else {
                DabyWSClientConfig.beamColorR = 0.55F;
                DabyWSClientConfig.beamColorG = 0.15F;
                DabyWSClientConfig.beamColorB = 1.00F;
            }
            if (phase >= 5.0F) {
                // The native head renderer owns both eye lenses and the teeth
                // overlay. Keep both emissive submissions alive for the
                // phase-5 model even when a migrated config carried an old
                // zero glow setting; the render type is full-bright and bloom
                // remains fail-soft in the base renderer.
                DabyWSClientConfig.headEyeGlow = true;
                DabyWSClientConfig.glowStrength = Math.max(DabyWSClientConfig.glowStrength, 2.5); // #404
            }

        } catch (Throwable ignored) {
        }
    }
}
