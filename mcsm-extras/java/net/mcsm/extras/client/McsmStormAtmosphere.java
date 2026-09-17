package net.mcsm.extras.client;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * Storm-only sky/fog colour from mcsm_atmosphere palettes.
 * Calm day/night never go purple — only active storm phases do.
 * Wired every client tick; does not touch FabricSkyboxes.
 */
public final class McsmStormAtmosphere {

    private McsmStormAtmosphere() {
    }

    private static float ramp(float v, float lo, float hi) {
        if (hi <= lo) {
            return v >= hi ? 1.0F : 0.0F;
        }
        float t = Mth.clamp((v - lo) / (hi - lo), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    /** Nearest active storm phase, or 0 if none / far. */
    public static float nearestPhase() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.player == null) {
                return 0.0F;
            }
            float best = 0.0F;
            double bestD = Double.MAX_VALUE;
            var pos = mc.player.position();
            for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                if (d.phase < 4.0F) {
                    continue;
                }
                double dx = d.dispX - pos.x;
                double dy = d.dispY - pos.y;
                double dz = d.dispZ - pos.z;
                double dd = dx * dx + dy * dy + dz * dz;
                if (dd < bestD) {
                    bestD = dd;
                    best = d.phase;
                }
            }
            // beyond this range the sky/fog is vanilla Story Mode calm again
            if (bestD > 1700.0 * 1700.0) {
                return 0.0F;
            }
            return best;
        } catch (Throwable t) {
            return 0.0F;
        }
    }

    public static float distanceInfluence() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.player == null) return 0.0F;
            double bestD = Double.MAX_VALUE;
            var pos = mc.player.position();
            for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                if (d.phase < 4.0F) continue;
                double dx = d.dispX - pos.x;
                double dy = d.dispY - pos.y;
                double dz = d.dispZ - pos.z;
                bestD = Math.min(bestD, dx * dx + dy * dy + dz * dz);
            }
            if (bestD == Double.MAX_VALUE) return 0.0F;
            double dist = Math.sqrt(bestD);
            return 1.0F - Mth.clamp((float)((dist - 900.0D) / 800.0D), 0.0F, 1.0F);
        } catch (Throwable t) {
            return 0.0F;
        }
    }

    /**
     * Write storm sky RGB into out[3] when storm owns the sky.
     * Returns blend 0..1 (0 = pure calm StoryModeSkyTint).
     */
    /** Compatibility colour for older fog callers; native SkyRenderer is authoritative. */
    public static float skyBlend(float[] out) {
        float phase = nearestPhase();
        if (phase < 4.45F || out == null || out.length < 3) {
            // Phase 4 remains entirely vanilla.
            return 0.0F;
        }
        // BUILD #402 -- 1:1 extracted zenith colours from the reference
        // frames: P5 teal day (2026-09-06 143811), P5.5 midnight magenta
        // (2026-09-14 065011), P6 split-storm plum (2026-09-06 152252).
        // Bands cross-fade over their full width (100% blend gradients).
        float[] zen5  = {0.150F, 0.240F, 0.230F};
        float[] zen55 = {0.102F, 0.039F, 0.165F};
        float[] zen6  = {0.420F, 0.360F, 0.460F};
        if (phase < 5.5F) {
            blend(zen5, zen55, ramp(phase, 5.0F, 5.5F), out);
        } else {
            blend(zen55, zen6, ramp(phase, 5.5F, 6.0F), out);
        }
        float density = 0.90F;
        return Mth.clamp(density * distanceInfluence(), 0.0F, 0.90F);
    }

    /** BUILD #402: matching 1:1 extracted horizon colours per band. */
    public static float skyHorizonBlend(float[] out) {
        float phase = nearestPhase();
        if (phase < 4.45F || out == null || out.length < 3) {
            return 0.0F;
        }
        float[] hor5  = {0.700F, 0.750F, 0.690F}; // pale sage (143811)
        float[] hor55 = {0.720F, 0.330F, 0.520F}; // pink-magenta (065011)
        float[] hor6  = {0.900F, 0.680F, 0.620F}; // peach-salmon (152252)
        if (phase < 5.5F) {
            blend(hor5, hor55, ramp(phase, 5.0F, 5.5F), out);
        } else {
            blend(hor55, hor6, ramp(phase, 5.5F, 6.0F), out);
        }
        float density = 0.90F;
        return Mth.clamp(density * distanceInfluence(), 0.0F, 0.90F);
    }

    private static void blend(float[] a, float[] b, float t, float[] out) {
        out[0] = a[0] + (b[0] - a[0]) * t;
        out[1] = a[1] + (b[1] - a[1]) * t;
        out[2] = a[2] + (b[2] - a[2]) * t;
    }

    public static void tick() {
        // reserved — StoryModeSkyTint / fog mixins call skyBlend
    }
}
