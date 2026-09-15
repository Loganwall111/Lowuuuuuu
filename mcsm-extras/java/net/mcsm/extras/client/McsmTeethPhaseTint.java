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
            // MCSM 1.9.201 -- TRUE PHASE-SHIFTING TEETH GLOW TRACK.
            // The emissive teeth/eye colour keys follow the evolution track
            // exactly as storyboarded; the shader pass reads the storm state
            // every tick and re-keys the native emissive layer:
            //   phase 4   cyan-white glow
            //   phase 5   pure white glow
            //   phase 5.5 cyan-blue glow
            //   phase 6   cinematic blue glow
            //   phase 7   toxic green glow
            //   phase 8   blinding white glow
            float r, g, b, inten;
            boolean glow;
            // SHOW-SPEC HUES + BUILD #403 GLOW INTENSITIES (migrated from the
            // 412 lineage): the teethBoost pass needs ~3.6-4.3 to read as
            // glowing on this renderer; hues stay on the evolution track:
            //   4 cyan-white / 5 pure white / 5.5 cyan-blue /
            //   6 cinematic blue / 7 toxic green / 8 blinding white
            if (phase >= 8.0F) {
                r = 1.00F; g = 1.00F; b = 1.00F; inten = 4.30F; glow = true;   // phase 8: blinding white
            } else if (phase >= 7.0F) {
                r = 0.36F; g = 1.00F; b = 0.28F; inten = 4.20F; glow = true;   // phase 7: toxic green
            } else if (phase >= 6.0F) {
                r = 0.22F; g = 0.50F; b = 1.00F; inten = 4.20F; glow = true;   // phase 6: cinematic blue
            } else if (phase >= 5.5F) {
                r = 0.40F; g = 0.80F; b = 1.00F; inten = 3.90F; glow = true;   // phase 5.5: cyan-blue
            } else if (phase >= 5.0F) {
                r = 1.00F; g = 1.00F; b = 1.00F; inten = 3.60F; glow = true;   // phase 5: pure white, glowing
            } else if (phase >= 4.0F) {
                r = 0.72F; g = 0.98F; b = 1.00F; inten = 3.60F; glow = true;   // phase 4: cyan-white
            } else {
                r = 0.98F; g = 0.98F; b = 0.86F; inten = 0.0F; glow = false;  // phase 3: no glowing teeth
            }
            DabyWSClientConfig.eyeColorR = r;
            DabyWSClientConfig.eyeColorG = g;
            DabyWSClientConfig.eyeColorB = b;
            DabyWSClientConfig.turquoiseTeethIntensity = inten;
            DabyWSClientConfig.turquoiseTeeth = glow;
            // BUILD #405 user override: phase 6 beams read bluish (reference
            // close-up frames); every other phase keeps show purple.
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
