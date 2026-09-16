package net.mcsm.extras.client;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.StormSkins;
import net.minecraft.client.Minecraft;

/**
 * BUILD #416 -- THE WitherStormPhase FEED.
 *
 * This is the Java half of the blueprint's phase uniform. In 26.2 a core
 * shader cannot take an arbitrary uniform: its bind group is the vanilla layout,
 * and a slot the layout does not carry is a hard Vulkan crash (the same trap
 * storm_glow.fsh documents). The value therefore travels through the FOG block
 * the sky/terrain/entity passes already bind -- FogData.skyEnd is stamped as
 * 1000 + phase*100, and the shaders decode it with mcsm_witherstorm_phase().
 * Iris/Oculus packs additionally receive the real `witherstorm_Phase` uniform.
 *
 * Every consumer (sky column, body shading, mouth palette, cloud decks) reads
 * the value from here, so there is exactly one definition of "what phase is it
 * right now" in the whole build.
 */
public final class McsmStormPhase {

    /** The FogSkyEnd carrier: skyEnd = 1000 + phase * 100 (see McsmFogCarrierMixin). */
    public static final float CARRIER_BASE = 1000.0F;
    public static final float CARRIER_SCALE = 100.0F;

    /** Phase window the story track is defined over. */
    public static final float PHASE_MIN = 4.45F;
    public static final float PHASE_MAX = 8.05F;

    /**
     * The three reference sheets, traced stop for stop (t = 0 zenith -> t = 1
     * horizon). These are the SAME numbers mcsm-core-shaders/core/sky.fsh
     * samples, kept here so the Java geometry (the halo) cannot drift away from
     * the shader sky:
     *   TEAL   "phase 5 turquoise sky.png"      = phase 5.0 - 5.1
     *   PURPLE "phase5sky0purple sky.png"       = phase 5.5 - 5.9
     *   ROSE   "phase6sky 6 witherstorm.png"    = phase 6.0 - 7.0
     */
    public static final float[][] SKY_TEAL = {
        {0.047F, 0.071F, 0.086F}, {0.064F, 0.096F, 0.115F}, {0.082F, 0.121F, 0.143F},
        {0.097F, 0.143F, 0.166F}, {0.111F, 0.162F, 0.185F}, {0.125F, 0.180F, 0.204F},
};
    public static final float[][] SKY_PURPLE = {
        {0.086F, 0.039F, 0.129F}, {0.143F, 0.061F, 0.200F}, {0.199F, 0.083F, 0.271F},
        {0.253F, 0.104F, 0.336F}, {0.303F, 0.122F, 0.395F}, {0.353F, 0.141F, 0.455F},
};
    public static final float[][] SKY_ROSE = {
        {0.114F, 0.082F, 0.098F}, {0.172F, 0.120F, 0.145F}, {0.230F, 0.158F, 0.192F},
        {0.285F, 0.194F, 0.238F}, {0.339F, 0.228F, 0.284F}, {0.392F, 0.263F, 0.329F},
};
    /**
     * The fall the supplied sheets do not cover: phase 7.0 - 8.05 continues
     * toward the storyboard's ember/black end. Same six rows as sky.fsh's
     * EMBER_END, so the halo keeps following the sky right to phase 8.05
     * instead of freezing on the phase-6 rose.
     */
    public static final float[][] SKY_EMBER = {
        {0.118F, 0.329F, 0.369F}, {0.230F, 0.150F, 0.220F}, {0.369F, 0.130F, 0.180F},
        {0.784F, 0.230F, 0.094F}, {0.900F, 0.290F, 0.070F}, {0.550F, 0.160F, 0.030F},
    };

    private static volatile float phase = 0.0F;
    private static volatile float horizonR = 0.0F;
    private static volatile float horizonG = 0.0F;
    private static volatile float horizonB = 0.0F;

    private McsmStormPhase() {
    }

    /** The live phase, 0.0 when no storm is tracked. */
    public static float phase() {
        return phase;
    }

    public static boolean active() {
        return phase >= PHASE_MIN;
    }

    /** The stage phase: horizon colour, in packed ARGB, for the native sky disc. */
    public static int horizonArgb() {
        return 0xFF000000
                | (clamp255(horizonR) << 16)
                | (clamp255(horizonG) << 8)
                | clamp255(horizonB);
    }

    /** The phase this build's colour tables are keyed on. */
    public static float resolve() {
        float p = 0.0F;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null) {
                return 0.0F;
            }
            for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                if (d.phase > p) {
                    p = d.phase;
                }
            }
            float hint = (float) StormSkins.phaseHint();
            float near = McsmStormAtmosphere.nearestPhase();
            if (hint > p) {
                p = hint;
            }
            if (near > p) {
                p = near;
            }
        } catch (Throwable ignored) {
        }
        if (p > 0.0F && p < PHASE_MIN) {
            p = PHASE_MIN;
        }
        return Math.min(p, PHASE_MAX);
    }

    /**
     * Publish the current phase and its reference horizon colour. Called once
     * per frame from the sky state hook, before the sky pass draws.
     */
    public static void publish(float p) {
        phase = p;
        float[] c = horizonFor(p);
        horizonR = c[0];
        horizonG = c[1];
        horizonB = c[2];
    }

    /**
     * The horizon (t = 1) row of whichever reference sheet owns the phase --
     * the same three tables sky.fsh samples, so the disc colour and the shader
     * gradient agree exactly and no seam can open between them.
     *   teal   "phase 5 turquoise sky"    5.0 - 5.1
     *   purple "phase5sky0purple sky"     5.5 - 5.9
     *   rose   "phase6sky 6 witherstorm"  6.0 - 7.0
     * Values below are the traced bottom stops of those sheets.
     */
    public static float[] horizonFor(float p) {
        return columnFor(p, 1.0F);
    }

    /**
     * One colour out of the reference sky at vertical t (0 zenith -> 1 horizon),
     * interpolated across whichever sheet owns the phase, with the same ramps
     * sky.fsh uses. The halo ring, the sky disc and the shader gradient are all
     * this one function, so the storm's glow can never disagree with its sky.
     */
    public static float[] columnFor(float p, float t) {
        float[] out = new float[3];
        if (p <= 4.4F) {
            return out;
        }
        float tealToPurple = ramp(p, 5.1F, 5.5F);
        float purpleToRose = ramp(p, 5.75F, 6.05F);
        float intoEmber = ramp(p, 7.0F, 8.05F);
        float[] a = sample(SKY_TEAL, t);
        float[] b = sample(SKY_PURPLE, t);
        float[] c = sample(SKY_ROSE, t);
        float[] e = sample(SKY_EMBER, t);
        mix(a, b, tealToPurple, out);
        mix(out, c, purpleToRose, out);
        mix(out, e, intoEmber, out);
        return out;
    }

    /** Sample one traced 6-stop column at t (0 zenith -> 1 horizon). */
    private static float[] sample(float[][] col, float t) {
        float u = Math.max(0.0F, Math.min(1.0F, t)) * 5.0F;
        int i = (int) Math.floor(u);
        float f = u - i;
        if (i > 4) {
            i = 4;
            f = 1.0F;
        }
        return new float[]{
            col[i][0] + (col[i + 1][0] - col[i][0]) * f,
            col[i][1] + (col[i + 1][1] - col[i][1]) * f,
            col[i][2] + (col[i + 1][2] - col[i][2]) * f,
        };
    }

    /** Packed ARGB for a colour out of {@link #columnFor}. */
    public static int argb(float[] c) {
        return 0xFF000000 | (clamp255(c[0]) << 16) | (clamp255(c[1]) << 8) | clamp255(c[2]);
    }

    private static float ramp(float v, float lo, float hi) {
        if (hi <= lo) {
            return v >= hi ? 1.0F : 0.0F;
        }
        float t = Math.max(0.0F, Math.min(1.0F, (v - lo) / (hi - lo)));
        return t * t * (3.0F - 2.0F * t);
    }

    private static void mix(float[] a, float[] b, float t, float[] out) {
        out[0] = a[0] + (b[0] - a[0]) * t;
        out[1] = a[1] + (b[1] - a[1]) * t;
        out[2] = a[2] + (b[2] - a[2]) * t;
    }

    private static int clamp255(float v) {
        int i = Math.round(v * 255.0F);
        return i < 0 ? 0 : (i > 255 ? 255 : i);
    }
}
