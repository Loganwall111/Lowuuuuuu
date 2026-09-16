package net.mcsm.extras.client;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.StormSkins;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;

/**
 * Drives model teeth/eye glow colours from the nearest storm phase so the
 * base-mod teethBoost pass matches the MCSM frames without Iris:
 *   phase 3          no teeth glow
 *   phase 4          small cool-white/cyan glow on the three heads
 *   phase 5          flat white teeth, no big glow
 *   phase 5.5        white teeth with glow
 *   phase 4 through 5.5  neon-cyan eye/teeth channels (#00F3FF)
 *   phase 6+              sea-green eye/teeth channels (#00A877)
 *   all phases            dedicated full-bright emissive texture paths
 */
public final class McsmTeethPhaseTint {

    private McsmTeethPhaseTint() {
    }

    /**
     * BUILD #415 -- THE canonical evolution-track palette, one row per phase
     * band: {red, green, blue, glowIntensity}. Java, the core shaders
     * (mcsm_mouth_color in mcsm_visuals.glsl), the storm-glow shader and the
     * Iris final pass all read these same six bands, so the model's emissive
     * layer, the additive glow pool and the post-pass bloom can no longer
     * disagree about what colour the mouth is this phase:
     *   4.0  cyan-white / 5.0 pure white / 5.5 cyan-blue /
     *   6.0  cinematic blue / 7.0 toxic green / 8.0 blinding white
     * Anything below phase 4 is the non-glowing show default.
     */
    private static final float[][] PHASE_TRACK = {
        //  r      g      b      intensity
        { 0.98F, 0.98F, 0.86F, 0.00F },  // phase 3-  no glowing teeth
        { 0.72F, 0.98F, 1.00F, 3.60F },  // phase 4   cyan-white
        { 1.00F, 1.00F, 1.00F, 3.60F },  // phase 5   pure white
        { 0.40F, 0.80F, 1.00F, 3.90F },  // phase 5.5 cyan-blue
        { 0.22F, 0.50F, 1.00F, 4.20F },  // phase 6   cinematic blue
        { 0.36F, 1.00F, 0.28F, 4.20F },  // phase 7   toxic green
        { 1.00F, 1.00F, 1.00F, 4.30F },  // phase 8   blinding white
    };

    /** Index into PHASE_TRACK for an evolution phase. */
    public static int trackIndex(double phase) {
        if (phase >= 8.0D) return 6;
        if (phase >= 7.0D) return 5;
        if (phase >= 6.0D) return 4;
        if (phase >= 5.5D) return 3;
        if (phase >= 5.0D) return 2;
        if (phase >= 4.0D) return 1;
        return 0;
    }

    /** {r, g, b, intensity} for the nearest storm's phase. */
    public static float[] track(double phase) {
        return PHASE_TRACK[trackIndex(phase)];
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
