package net.mcsm.extras.client;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.mcsm.extras.McsmExtrasConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * Devouring Storms: how far the storm's sky reaches (mandate D.8).
 *
 * THE QUESTION. The user asked whether the regular overworld sky -- day, dusk,
 * midnight -- and the three storm-phase skies are still domes, and asked for the
 * sky to fade back to normal "as you get far away from hundreds of blocks, 500
 * blocks".
 *
 * THE HONEST ANSWER, IN CODE:
 *   * a vanilla dome is never drawn by us. In the overworld away from any storm
 *     the sky is Minecraft's own, untouched, at any hour;
 *   * inside the decayed reality the sky is OURS, unconditionally (see
 *     McsmNativeSkyRenderer), because that dimension has no business showing a
 *     vanilla dome;
 *   * around a storm the native sky is re-authored per phase -- that is the
 *     storm's sky, and until now it simply switched on with the phase and
 *     switched off when the storm was unloaded, with no distance in the
 *     decision at all (McsmStormPhase.resolve takes the furthest-along storm
 *     anywhere). So a storm three thousand blocks away still repainted the sky.
 *
 * This class is the missing distance term, in one place, and it owns the
 * fade-back the user asked for:
 *
 *     distance <= reach * FULL       -> the storm owns the sky completely
 *     reach * FULL .. reach * END    -> smoothstep back to the vanilla sky
 *     beyond reach * END             -> the storm contributes nothing at all
 *
 * `reach` is the configurable fade distance (default 500 blocks, the number the
 * user gave), so the sky starts handing itself back at half of it and is fully
 * vanilla by 900. Because the near end is a blend rather than a switch, a player
 * flying away from a storm watches the phase sky dissolve into daylight or
 * midnight instead of popping.
 */
public final class McsmSkyReach {

    /** Full storm sky inside this fraction of the reach. */
    public static final double FULL = 0.5D;
    /** Fully vanilla past this multiple of the reach. */
    public static final double END = 1.8D;
    /** Below this influence the storm does not own the sky at all. */
    public static final float CUTOFF = 0.02F;

    private McsmSkyReach() {
    }

    /** The configured fade distance in blocks (500 by default). */
    public static double reach() {
        try {
            return Math.max(120.0D, McsmExtrasConfig.skyFadeDistance);
        } catch (Throwable t) {
            return 500.0D;
        }
    }

    /** Distance to the nearest storm of phase 4 or later, or -1 when there is none. */
    public static double distance() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.player == null) {
                return -1.0D;
            }
            double best = Double.MAX_VALUE;
            var pos = mc.player.position();
            for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                if (d.phase < 4.0F) {
                    continue;
                }
                double dx = d.dispX - pos.x;
                double dy = d.dispY - pos.y;
                double dz = d.dispZ - pos.z;
                best = Math.min(best, dx * dx + dy * dy + dz * dz);
            }
            return best == Double.MAX_VALUE ? -1.0D : Math.sqrt(best);
        } catch (Throwable t) {
            return -1.0D;
        }
    }

    /**
     * 1.0 while the storm's sky is fully in charge, falling to 0.0 as the player
     * leaves its reach. Pure arithmetic, so the server, the HUD and the sky can
     * all use the same number.
     */
    public static float influence() {
        double dist = distance();
        if (dist < 0.0D) {
            return 0.0F;
        }
        double reach = reach();
        double full = reach * FULL;
        double end = reach * END;
        if (dist <= full) {
            return 1.0F;
        }
        if (dist >= end) {
            return 0.0F;
        }
        float t = (float) ((end - dist) / (end - full));
        return t * t * (3.0F - 2.0F * t);   // smoothstep: no pop at either end
    }

    /** True when the storm's sky should be on screen at all. */
    public static boolean owns(float phase) {
        return phase >= McsmStormPhase.PHASE_MIN && influence() > CUTOFF;
    }

    /** One line for the HUD / console: where the fade is right now. */
    public static String describe(float phase) {
        double dist = distance();
        if (dist < 0.0D) {
            return "no storm in range (vanilla sky)";
        }
        int pct = Math.round(influence() * 100.0F);
        return "storm " + Math.round(dist) + " blocks away, sky " + pct + "% storm / "
                + (100 - pct) + "% vanilla (fade reaches "
                + Math.round(reach() * END) + " blocks)";
    }

    /** Blend two packed ARGB colours; kept here so every caller fades the same way. */
    public static int mix(int from, int to, float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        if (t <= 0.0F) return from;
        if (t >= 1.0F) return to;
        int a = (from >>> 24) & 0xFF, r = (from >> 16) & 0xFF, g = (from >> 8) & 0xFF, b = from & 0xFF;
        int a2 = (to >>> 24) & 0xFF, r2 = (to >> 16) & 0xFF, g2 = (to >> 8) & 0xFF, b2 = to & 0xFF;
        int aa = Math.round(a + (a2 - a) * t), rr = Math.round(r + (r2 - r) * t);
        int gg = Math.round(g + (g2 - g) * t), bb = Math.round(b + (b2 - b) * t);
        return (aa & 0xFF) << 24 | (rr & 0xFF) << 16 | (gg & 0xFF) << 8 | (bb & 0xFF);
    }
}
