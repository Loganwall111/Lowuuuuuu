package net.mcsm.extras.client;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;

/**
 * Native Teeth & Eye Glow Pass:
 * Keeps eye pupils and phase-shifting teeth bone vertex grids fully emissive and bright.
 * Color tracks:
 *   - Electrical Neon Cyan (#00F3FF) up through Phase 5.5
 *   - Toxic Glowing Sea-Green (#00A877) starting in Phase 6
 */
public final class McsmTeethPhaseTint {

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
            boolean glow = true;

            if (phase >= 6.0F) {
                // Toxic Sea-Green (#00A877)
                r = 0.000F;
                g = 0.659F;
                b = 0.467F;
                inten = 2.50F;
            } else {
                // Electrical Neon Cyan (#00F3FF)
                r = 0.000F;
                g = 0.953F;
                b = 1.000F;
                inten = 2.20F;
            }

            DabyWSClientConfig.eyeColorR = r;
            DabyWSClientConfig.eyeColorG = g;
            DabyWSClientConfig.eyeColorB = b;
            DabyWSClientConfig.turquoiseTeethIntensity = inten;
            DabyWSClientConfig.turquoiseTeeth = glow;

            // Tractor beam color follows phase palette
            float day = 0.55F + 0.45F * (float) Math.sin((mc.level.getGameTime() % 24000L) / 24000.0D * Math.PI * 2.0D);
            float br = phase >= 6.0F ? 0.00F : 0.00F;
            float bg = phase >= 6.0F ? 0.66F : 0.95F;
            float bb = phase >= 6.0F ? 0.47F : 1.00F;
            DabyWSClientConfig.beamColorR = br * (0.82F + 0.18F * day);
            DabyWSClientConfig.beamColorG = bg * (0.82F + 0.18F * day);
            DabyWSClientConfig.beamColorB = bb;
        } catch (Throwable ignored) {
        }
    }
}
