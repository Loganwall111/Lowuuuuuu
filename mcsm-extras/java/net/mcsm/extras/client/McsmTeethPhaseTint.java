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

    /** Packed full-bright ARGB used by the exact native teeth hook. */
    public static int teethTintArgb() {
        // Use the renderer's phase hint as well as the distant-storm tracker;
        // local storms do not always publish a DistantStormData entry.
        double phase = Math.max(StormSkins.phaseHint(), McsmStormAtmosphere.nearestPhase());
        if (phase >= 6.0D) {
            return rgb(0.0F, 0.659F, 0.467F); // #00A877
        }
        if (phase >= 4.0D) {
            return rgb(0.0F, 0.953F, 1.0F);   // #00F3FF
        }
        return rgb((float) DabyWSClientConfig.eyeColorR,
                (float) DabyWSClientConfig.eyeColorG,
                (float) DabyWSClientConfig.eyeColorB);
    }

    /** Packed full-bright ARGB used by the exact native eye hook. */
    public static int eyeTintArgb() {
        // Dedicated eyes stay on RenderType.eyes and use the same phase track
        // as the teeth, without inheriting world light or shadow attenuation.
        double phase = Math.max(StormSkins.phaseHint(), McsmStormAtmosphere.nearestPhase());
        return phase >= 6.0D
                ? rgb(0.0F, 0.659F, 0.467F) // #00A877
                : phase >= 4.0D
                    ? rgb(0.0F, 0.953F, 1.0F) // #00F3FF
                    : rgb((float) DabyWSClientConfig.eyeColorR,
                          (float) DabyWSClientConfig.eyeColorG,
                          (float) DabyWSClientConfig.eyeColorB);
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
            float r, g, b, inten;
            boolean glow;
            if (phase >= 6.0F) {
                // Sea-green begins at Phase 6 and stays emissive through the
                // final skull/devourer phases.
                r = 0.00F; g = 0.659F; b = 0.467F; inten = 4.20F; glow = true; // #00A877
            } else if (phase >= 4.0F) {
                // Neon-cyan runs through the Phase 5.5 transition.
                r = 0.00F; g = 0.953F; b = 1.00F; inten = 3.90F; glow = true; // #00F3FF
            } else {
                r = 0.98F; g = 0.98F; b = 0.86F; inten = 0.0F; glow = false;
            }
            DabyWSClientConfig.eyeColorR = r;
            DabyWSClientConfig.eyeColorG = g;
            DabyWSClientConfig.eyeColorB = b;
            DabyWSClientConfig.turquoiseTeethIntensity = inten;
            DabyWSClientConfig.turquoiseTeeth = glow;
            if (phase >= 5.0F) {
                // The native head renderer owns both eye lenses and the teeth
                // overlay. Keep both emissive submissions alive for the
                // phase-5 model even when a migrated config carried an old
                // zero glow setting; the render type is full-bright and bloom
                // remains fail-soft in the base renderer.
                DabyWSClientConfig.headEyeGlow = true;
                DabyWSClientConfig.glowStrength = Math.max(DabyWSClientConfig.glowStrength, 1.0);
            }

            // Keep beam and eye materials on the same full-bright phase track.
            if (phase >= 6.0F) {
                DabyWSClientConfig.beamColorR = 0.00F;
                DabyWSClientConfig.beamColorG = 0.659F;
                DabyWSClientConfig.beamColorB = 0.467F;
            } else if (phase >= 4.0F) {
                DabyWSClientConfig.beamColorR = 0.00F;
                DabyWSClientConfig.beamColorG = 0.953F;
                DabyWSClientConfig.beamColorB = 1.00F;
            }
        } catch (Throwable ignored) {
        }
    }
}
