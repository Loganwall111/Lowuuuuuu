package net.mcsm.extras.client;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.StormSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

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
     * BUILD #475 -- the phase from which the sky itself stops behaving: the rose
     * stop the references put at 6.0, read by the hallucinations as "the world is
     * wrong now" and nothing else. Named here rather than typed as a literal in a
     * second place, so the feed and the effect cannot drift apart.
     */
    public static final float SHELL_ROSE = 5.90F;

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
        {0.047F, 0.071F, 0.086F}, {0.050F, 0.075F, 0.091F}, {0.053F, 0.079F, 0.095F},
        {0.055F, 0.083F, 0.100F}, {0.058F, 0.087F, 0.104F}, {0.061F, 0.091F, 0.109F},
        {0.064F, 0.095F, 0.114F}, {0.067F, 0.099F, 0.118F}, {0.069F, 0.103F, 0.123F},
        {0.072F, 0.107F, 0.127F}, {0.075F, 0.111F, 0.132F}, {0.078F, 0.115F, 0.136F},
        {0.080F, 0.119F, 0.141F}, {0.083F, 0.123F, 0.145F}, {0.086F, 0.127F, 0.150F},
        {0.089F, 0.131F, 0.155F}, {0.091F, 0.135F, 0.158F}, {0.094F, 0.138F, 0.161F},
        {0.096F, 0.141F, 0.164F}, {0.098F, 0.144F, 0.167F}, {0.100F, 0.147F, 0.171F},
        {0.103F, 0.150F, 0.174F}, {0.105F, 0.153F, 0.177F}, {0.107F, 0.156F, 0.180F},
        {0.110F, 0.159F, 0.183F}, {0.112F, 0.162F, 0.186F}, {0.114F, 0.165F, 0.189F},
        {0.116F, 0.168F, 0.192F}, {0.119F, 0.171F, 0.195F}, {0.121F, 0.174F, 0.198F},
        {0.123F, 0.177F, 0.201F}, {0.125F, 0.180F, 0.204F},
};
    public static final float[][] SKY_PURPLE = {
        {0.086F, 0.039F, 0.129F}, {0.095F, 0.043F, 0.141F}, {0.104F, 0.046F, 0.152F},
        {0.114F, 0.050F, 0.164F}, {0.123F, 0.053F, 0.175F}, {0.132F, 0.057F, 0.186F},
        {0.141F, 0.060F, 0.198F}, {0.150F, 0.064F, 0.209F}, {0.159F, 0.068F, 0.220F},
        {0.168F, 0.071F, 0.232F}, {0.177F, 0.075F, 0.243F}, {0.186F, 0.078F, 0.255F},
        {0.196F, 0.082F, 0.266F}, {0.205F, 0.085F, 0.277F}, {0.214F, 0.089F, 0.289F},
        {0.223F, 0.092F, 0.300F}, {0.231F, 0.096F, 0.311F}, {0.240F, 0.099F, 0.320F},
        {0.248F, 0.102F, 0.330F}, {0.256F, 0.105F, 0.340F}, {0.264F, 0.108F, 0.349F},
        {0.272F, 0.111F, 0.359F}, {0.280F, 0.114F, 0.368F}, {0.288F, 0.117F, 0.378F},
        {0.296F, 0.120F, 0.388F}, {0.304F, 0.123F, 0.397F}, {0.312F, 0.126F, 0.407F},
        {0.321F, 0.129F, 0.416F}, {0.329F, 0.132F, 0.426F}, {0.337F, 0.135F, 0.436F},
        {0.345F, 0.138F, 0.445F}, {0.353F, 0.141F, 0.455F},
};
    public static final float[][] SKY_ROSE = {
        {0.114F, 0.082F, 0.098F}, {0.123F, 0.088F, 0.106F}, {0.132F, 0.094F, 0.113F},
        {0.142F, 0.101F, 0.121F}, {0.151F, 0.107F, 0.128F}, {0.161F, 0.113F, 0.136F},
        {0.170F, 0.119F, 0.144F}, {0.179F, 0.125F, 0.151F}, {0.189F, 0.131F, 0.159F},
        {0.198F, 0.137F, 0.166F}, {0.207F, 0.143F, 0.174F}, {0.217F, 0.149F, 0.182F},
        {0.226F, 0.155F, 0.189F}, {0.235F, 0.161F, 0.197F}, {0.245F, 0.167F, 0.204F},
        {0.254F, 0.173F, 0.212F}, {0.263F, 0.179F, 0.219F}, {0.272F, 0.185F, 0.227F},
        {0.280F, 0.190F, 0.234F}, {0.289F, 0.196F, 0.241F}, {0.298F, 0.202F, 0.249F},
        {0.306F, 0.207F, 0.256F}, {0.315F, 0.213F, 0.263F}, {0.323F, 0.218F, 0.271F},
        {0.332F, 0.224F, 0.278F}, {0.341F, 0.229F, 0.285F}, {0.349F, 0.235F, 0.293F},
        {0.358F, 0.240F, 0.300F}, {0.366F, 0.246F, 0.307F}, {0.375F, 0.252F, 0.315F},
        {0.384F, 0.257F, 0.322F}, {0.392F, 0.263F, 0.329F},
};
    /**
     * The fall the supplied sheets do not cover: phase 7.0 - 8.05 continues
     * toward the storyboard's ember/black end. Same six rows as sky.fsh's
     * EMBER_END, so the halo keeps following the sky right to phase 8.05
     * instead of freezing on the phase-6 rose.
     */
    public static final float[][] SKY_EMBER = {
        {0.118F, 0.329F, 0.369F}, {0.136F, 0.300F, 0.345F}, {0.154F, 0.271F, 0.321F},
        {0.172F, 0.242F, 0.297F}, {0.190F, 0.214F, 0.273F}, {0.208F, 0.185F, 0.249F},
        {0.226F, 0.156F, 0.225F}, {0.248F, 0.147F, 0.215F}, {0.270F, 0.144F, 0.208F},
        {0.293F, 0.141F, 0.202F}, {0.315F, 0.138F, 0.195F}, {0.338F, 0.135F, 0.189F},
        {0.360F, 0.131F, 0.183F}, {0.409F, 0.140F, 0.172F}, {0.476F, 0.156F, 0.158F},
        {0.543F, 0.172F, 0.144F}, {0.610F, 0.188F, 0.130F}, {0.677F, 0.204F, 0.116F},
        {0.744F, 0.220F, 0.102F}, {0.791F, 0.234F, 0.092F}, {0.810F, 0.244F, 0.089F},
        {0.829F, 0.253F, 0.085F}, {0.848F, 0.263F, 0.081F}, {0.866F, 0.273F, 0.077F},
        {0.885F, 0.282F, 0.073F}, {0.889F, 0.286F, 0.069F}, {0.832F, 0.265F, 0.062F},
        {0.776F, 0.244F, 0.056F}, {0.719F, 0.223F, 0.049F}, {0.663F, 0.202F, 0.043F},
        {0.606F, 0.181F, 0.036F}, {0.550F, 0.160F, 0.030F},
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

    // ------------------------------------------------------------------
    // BUILD #416 -- the storm's SIZE model, in one place.
    //
    // The halo, the atmosphere and the body all have to agree on how big the
    // storm is at a given moment; three private copies of a body-radius curve is
    // how a halo ends up hanging off a body that outgrew it. These are the
    // numbers everything tethering to the storm reads.
    // ------------------------------------------------------------------

    /** 0.0 at the phase-4.45 onset, 1.0 at phase 8.05: the storm's own growth ramp. */
    public static float growth(float p) {
        return Mth.clamp((p - PHASE_MIN) / (PHASE_MAX - PHASE_MIN), 0.0F, 1.0F);
    }

    /** Half-width of the body cluster in blocks. */
    public static double bodyRadius(float p) {
        if (p < 5.0F) {
            return 12.0D + Math.max(0.0F, p - PHASE_MIN) * 4.0D;
        }
        if (p < 6.0F) {
            return 18.0D + 22.0D * (p - 5.0F);
        }
        return Math.min(340.0D, 40.0D + 30.0D * (p - 6.0F));
    }

    /**
     * Top of the storm's silhouette in blocks -- past the top tentacle
     * attachments, which is where the light column has to reach.
     */
    public static double bodyHeight(float p) {
        return bodyRadius(p) * (2.05D + 0.55D * growth(p));
    }

    /**
     * The ENTITY's own scale multiplier, on top of the phase ramp. The storm
     * grows in two independent ways: the scripted phase stages, and the number of
     * heads it currently has attached (every attached head drags the cluster
     * wider and taller). Read live, so a severed head visibly pulls the light
     * column in instead of leaving it inflated around empty air.
     */
    public static float scaleMultiplier(float p, int activeHeads) {
        float heads = Mth.clamp((activeHeads - 2) / 4.0F, 0.0F, 1.0F);
        return 1.0F + 0.55F * growth(p) + 0.20F * heads;
    }

    /**
     * Half-angle of the conic light column, in degrees. It OPENS as the storm
     * grows: a phase-5 column is a near-cylinder hugging the body, a phase-8
     * column is a wide cone because the silhouette it has to wrap is wider.
     */
    public static double columnHalfAngleDeg(float p) {
        return 9.0D + 13.0D * growth(p);
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
    /**
     * BUILD #476 -- sample a column at t (0 zenith -> 1 horizon), for ANY row count.
     *
     * <p>This used to hard-code five (six stops). The three sheet columns are 32
     * rows now -- one per sampled row of the reference image, so the Java feed and
     * the shader read the same column the same way -- while the ember/day/night
     * columns stay six-stop. Sampling by {@code (len - 1)} keeps every one of them
     * 1:1 with its own table instead of quietly stretching a six-row table across
     * the sky.
     */
    private static float[] sample(float[][] col, float t) {
        int last = col.length - 1;
        if (last <= 0) {
            return new float[]{col[0][0], col[0][1], col[0][2]};
        }
        float u = Math.max(0.0F, Math.min(1.0F, t)) * last;
        int i = (int) Math.floor(u);
        float f = u - i;
        if (i >= last) {
            i = last - 1;
            f = 1.0F;
        }
        if (i < 0) {
            i = 0;
            f = 0.0F;
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
