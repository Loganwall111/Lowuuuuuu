package net.mcsm.extras.client;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;

/**
 * Drives model teeth/eye glow colours from the nearest storm phase so the
 * base-mod teethBoost pass matches the MCSM frames without Iris:
 *   phase 3          no teeth glow
 *   phase 4          cyan-white glow on the three heads
 *   phase 5          whitish teeth with glow
 *   phase 5.5        cyan-blue glowing teeth
 *   phase 6          blue glowing teeth after the split
 *   phase 7          toxic green glowing teeth
 *   phase 8          white glowing teeth
 */
public final class McsmTeethPhaseTint {

    /**
     * BUILD #390 PHASE 2 -- the cosmic blue both the beam lasers and the lower
     * spotlight nodes are pinned to. #4D4DFF, i.e. 77/255, 77/255, 255/255: a
     * vibrant blue-violet that reads as "show-accurate" against the Story Mode
     * frames without tipping into the purple the halo rings already own.
     */
    public static final float COSMIC_BLUE_R = 0.302F;
    public static final float COSMIC_BLUE_G = 0.302F;
    public static final float COSMIC_BLUE_B = 1.000F;

    /** #4D4DFF as the 0..255 triple the glow pass wants. */
    public static int[] cosmicBlue() {
        return new int[] {(int) (COSMIC_BLUE_R * 255.0F + 0.5F),
                          (int) (COSMIC_BLUE_G * 255.0F + 0.5F),
                          (int) (COSMIC_BLUE_B * 255.0F + 0.5F)};
    }

    /** The same colour at a fraction of the intensity, for falloff layers. */
    public static int[] cosmicBlue(float mul) {
        int[] c = cosmicBlue();
        return new int[] {Math.min(255, (int) (c[0] * mul + 0.5F)),
                          Math.min(255, (int) (c[1] * mul + 0.5F)),
                          Math.min(255, (int) (c[2] * mul + 0.5F))};
    }

    private McsmTeethPhaseTint() {
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
            if (phase >= 8.0F) {
                r = 1.00F; g = 1.00F; b = 1.00F; inten = 2.20F; glow = true;   // phase 8: white
            } else if (phase >= 7.0F) {
                r = 0.55F; g = 1.00F; b = 0.20F; inten = 2.35F; glow = true;   // phase 7: toxic green
            } else if (phase >= 6.0F) {
                r = 0.20F; g = 0.45F; b = 1.00F; inten = 2.45F; glow = true;   // phase 6: blue
            } else if (phase >= 5.5F) {
                r = 0.35F; g = 0.90F; b = 1.00F; inten = 2.05F; glow = true;   // phase 5.5: cyan-blue
            } else if (phase >= 5.0F) {
                r = 0.92F; g = 1.00F; b = 0.96F; inten = 1.10F; glow = true;   // phase 5: whitish with glow
            } else if (phase >= 4.0F) {
                r = 0.72F; g = 0.98F; b = 1.00F; inten = 1.25F; glow = true;   // phase 4: cyan-white
            } else {
                r = 0.98F; g = 0.98F; b = 0.86F; inten = 0.0F; glow = false;  // phase 3: no glowing teeth
            }
            DabyWSClientConfig.eyeColorR = r;
            DabyWSClientConfig.eyeColorG = g;
            DabyWSClientConfig.eyeColorB = b;
            DabyWSClientConfig.turquoiseTeethIntensity = inten;
            DabyWSClientConfig.turquoiseTeeth = glow;

            // BUILD #390 PHASE 2 -- TRACTOR BEAMS GO COSMIC BLUE.
            //
            // The beams used to follow the storm family per phase (magenta at
            // 5.5, cyan at 6, green-blue at 7). The build note fixes them to a
            // single vibrant, high-intensity cosmic blue instead:
            // #4D4DFF == (77, 77, 255) == (0.302, 0.302, 1.0).
            //
            // Two things this table must do:
            //   * stay pinned at #4D4DFF for every phase (the base renderer
            //     multiplies by its own day-cycle term, so the table is NOT
            //     day-modulated any more -- that was what shifted the hue),
            //   * be re-written every tick, because the mod's own config presets
            //     (PRESET_MCSM / PRESET_LEGACY) reset beamColorR/G/B and would
            //     otherwise drag the beams back to purple/pink.
            //
            // The upper halo rings are untouched -- that is a different pass and
            // the note is explicit about leaving them alone.
            DabyWSClientConfig.beamColorR = COSMIC_BLUE_R;
            DabyWSClientConfig.beamColorG = COSMIC_BLUE_G;
            DabyWSClientConfig.beamColorB = COSMIC_BLUE_B;
        } catch (Throwable ignored) {
        }
    }
}
