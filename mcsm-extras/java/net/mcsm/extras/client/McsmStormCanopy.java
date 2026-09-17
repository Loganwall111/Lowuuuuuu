package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * BUILD #474 -- THE STORM'S OWN SKY, OVER THE WHOLE SKY.
 *
 * <p>The standing report is "the sky is not fully the sky yet". The painted cube
 * (#457) gave every world its own AIR and the cloud deck (#470) gave it WEATHER, but
 * both of those are the DIMENSIONS' skies. The Overworld -- where the story, the
 * cities, the storm and the player actually are -- kept the shader's flat phase
 * gradient plus a lobe of storm in one direction, which is why looking UP at a mature
 * Wither Storm still reads as "a sky with something in it" instead of as the storm.
 *
 * <p>So this is the ceiling: a camera-anchored shell of the storm's own sky, all
 * {@link #SEGMENTS} ways around the player and from the horizon to the zenith, drawn
 * out of ordinary geometry in the world. It works for a player with no shader pack at
 * all -- the rule this whole sky system is built on.
 *
 * <p>THE COLOURS ARE NOT INVENTED HERE. Every vertex takes its colour from
 * {@link McsmStormPhase#columnFor(float, float)}, i.e. from the same six-row reference
 * tables ({@code SKY_TEAL} / {@code SKY_PURPLE} / {@code SKY_ROSE} / {@code SKY_EMBER})
 * that {@code sky.fsh} samples and the halo and horizon band already follow, so the
 * ceiling can only ever deepen the phase's own sky -- never contradict the stills.
 * Colours match palettes; the shell supplies coverage, structure and mass.
 *
 * <p>AND IT IS THE STORM'S, NOT A LID ON EVERYTHING. Nothing is drawn below
 * {@link #SHELL_IN} (that is the clear sky and its build-up), the shell ramps in to
 * full over {@link #SHELL_FULL}, and it fades out again at the carrier's own end
 * ({@link McsmStormPhase#PHASE_MAX}) so the sky is released when the storm is.
 * Density is highest toward the storm itself ({@link #ALPHA_NEAR} inside
 * {@link #NEAR_DEG}) and thins away from it ({@link #ALPHA_FAR}) rather than becoming
 * one flat haze -- the "fog in the sky" this project rejected -- and it thickens
 * downwards: dense near the horizon, thinner overhead where the storm's own core
 * shows through.
 */
public final class McsmStormCanopy {

    private static final Identifier WHITE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/storm_white.png");

    /** Quads around the full circle. 24 x 3 bands + a cap is still one draw. */
    private static final int SEGMENTS = 24;

    /** The bands, as elevation above the horizon in degrees: low, mid, high. */
    private static final double[][] BANDS = {
        {0.0D, 22.0D},
        {22.0D, 45.0D},
        {45.0D, 70.0D},
    };

    /** How many quads close the top, so the shell is a sky and not a tube. */
    private static final int CAP_QUADS = 12;

    /** The elevation the cap starts at, i.e. where the last band ends. */
    private static final double CAP_ELEV = 70.0D;

    /** How far out the shell stands: outside the floor band's 240, inside the cube. */
    private static final double RADIUS = 248.0D;

    /** How much of the cover each band keeps: low, mid, high. */
    private static final float[] BAND_WEIGHT = {1.0F, 0.72F, 0.50F};

    /** Alpha toward the storm, where the mass is. */
    private static final float ALPHA_NEAR = 0.62F;

    /** Alpha on the far side of the sky -- present, but the distance is thinner. */
    private static final float ALPHA_FAR = 0.24F;

    /** Inside this angle of the storm's bearing the cover is at its densest. */
    private static final double NEAR_DEG = 55.0D;

    /** Below this phase there is no canopy: the sky is still its own. */
    public static final float SHELL_IN = 4.60F;

    /** The canopy is fully up here -- the storm owns the sky from this phase. */
    public static final float SHELL_FULL = 5.10F;

    /** And it is released as the carrier runs out, so nothing is left hanging. */
    public static final float SHELL_OUT = 7.90F;

    /** How wide the top of the fan is: a cap, not a spike at the pole. */
    private static final double CAP_INNER = 62.0D;

    private McsmStormCanopy() {
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            if (!McsmExtrasConfig.paintedSky || ctx == null) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) {
                return;
            }
            float phase = McsmStormPhase.phase();
            if (phase < SHELL_IN) {
                return; // no storm yet: the sky belongs to itself
            }
            final float amp = ramp(phase, SHELL_IN, SHELL_FULL)
                    * (1.0F - ramp(phase, SHELL_OUT, McsmStormPhase.PHASE_MAX));
            if (amp <= 0.01F) {
                return;
            }
            final Vec3 cam = ctx.levelState().cameraRenderState.pos;
            // which way the mass is -- the canopy is densest over it, and only
            // thinner away from it, never absent
            final double[] bearing = bearing(level, cam);
            final long clock = level.getGameTime() % 200000L;
            // a slow turn, so the ceiling is weather rather than a painted bowl
            final double spin = clock * 0.00052D;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();

            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(WHITE),
                    (pose, consumer) -> {
                        for (int b = 0; b < BANDS.length; b++) {
                            // the band edges breathe, so the layers never line up into
                            // a grid the eye can read as geometry
                            double wobble = Math.sin(clock * 0.004D + b * 1.9D) * 1.6D;
                            double elevLo = BANDS[b][0] + wobble;
                            double elevHi = BANDS[b][1] + wobble;
                            ring(pose, consumer, cam, phase, amp, spin, bearing,
                                    elevLo, elevHi, BAND_WEIGHT[b]);
                        }
                        cap(pose, consumer, cam, phase, amp, spin, bearing);
                    });
        } catch (Throwable ignored) {
            // a sky that throws is worse than a sky that is vanilla
        }
    }

    /** One full turn of the shell between two elevations. */
    private static void ring(Pose pose, VertexConsumer consumer, Vec3 cam, float phase,
            float amp, double spin, double[] bearing, double loDeg, double hiDeg,
            float weight) {
        for (int i = 0; i < SEGMENTS; i++) {
            double a0 = 2.0D * Math.PI * i / SEGMENTS + spin;
            double a1 = 2.0D * Math.PI * (i + 1) / SEGMENTS + spin;
            double midA = (a0 + a1) * 0.5D;
            float near = nearness(midA, bearing);
            float alpha = alphaAt(amp, weight, near);
            // the mass darkens what it covers, on the phase's own colour
            float dark = 1.0F - 0.20F * near;
            float[] cLo = McsmStormPhase.columnFor(phase, vertical(loDeg));
            float[] cHi = McsmStormPhase.columnFor(phase, vertical(hiDeg));
            if (cLo == null || cHi == null || cLo.length < 3 || cHi.length < 3) {
                return;
            }
            quad(pose, consumer, cam, a0, a1, loDeg, hiDeg, cLo, cHi, dark, alpha);
        }
    }

    /** The top: fan quads from the last band up to the zenith, so the sky closes. */
    private static void cap(Pose pose, VertexConsumer consumer, Vec3 cam, float phase,
            float amp, double spin, double[] bearing) {
        float[] cTop = McsmStormPhase.columnFor(phase, vertical(90.0D));
        float[] cLo = McsmStormPhase.columnFor(phase, vertical(CAP_ELEV));
        if (cTop == null || cLo == null || cTop.length < 3 || cLo.length < 3) {
            return;
        }
        for (int i = 0; i < CAP_QUADS; i++) {
            double a0 = 2.0D * Math.PI * i / CAP_QUADS + spin;
            double a1 = 2.0D * Math.PI * (i + 1) / CAP_QUADS + spin;
            double midA = (a0 + a1) * 0.5D;
            float near = nearness(midA, bearing);
            float alpha = alphaAt(amp, BAND_WEIGHT[2] * 0.8F, near);
            float dark = 1.0F - 0.20F * near;
            // the cap is the one place the shell meets itself: the two radial edges
            // of the fan sit on tiny gaps at this radius, so it is drawn as quads
            // whose outer edge is a short arc rather than a point
            double x0 = Math.cos(a0), z0 = Math.sin(a0);
            double x1 = Math.cos(a1), z1 = Math.sin(a1);
            double r = RADIUS * Math.cos(Math.toRadians(CAP_ELEV));
            double y = cam.y + RADIUS * Math.sin(Math.toRadians(CAP_ELEV));
            double yTop = cam.y + RADIUS;
            int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
            int tr = rgb(cTop[0] * dark), tg = rgb(cTop[1] * dark), tb = rgb(cTop[2] * dark);
            int lr = rgb(cLo[0] * dark), lg = rgb(cLo[1] * dark), lb = rgb(cLo[2] * dark);
            // outer pair (on the band's own elevation), then the apex pair just off
            // the pole so every quad is a real quad
            consumer.addVertex(pose, (float) (cam.x + x0 * r), (float) y, (float) (cam.z + z0 * r))
                    .setColor(lr, lg, lb, a).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880).setNormal(pose, 0.0F, -1.0F, 0.0F);
            consumer.addVertex(pose, (float) (cam.x + x1 * r), (float) y, (float) (cam.z + z1 * r))
                    .setColor(lr, lg, lb, a).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880).setNormal(pose, 0.0F, -1.0F, 0.0F);
            consumer.addVertex(pose, (float) (cam.x + x1 * CAP_INNER), (float) yTop,
                    (float) (cam.z + z1 * CAP_INNER))
                    .setColor(tr, tg, tb, a).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880).setNormal(pose, 0.0F, -1.0F, 0.0F);
            consumer.addVertex(pose, (float) (cam.x + x0 * CAP_INNER), (float) yTop,
                    (float) (cam.z + z0 * CAP_INNER))
                    .setColor(tr, tg, tb, a).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880).setNormal(pose, 0.0F, -1.0F, 0.0F);
        }
    }

    private static void quad(Pose pose, VertexConsumer consumer, Vec3 cam,
            double a0, double a1, double loDeg, double hiDeg,
            float[] cLo, float[] cHi, float dark, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        int lr = rgb(cLo[0] * dark), lg = rgb(cLo[1] * dark), lb = rgb(cLo[2] * dark);
        int hr = rgb(cHi[0] * dark), hg = rgb(cHi[1] * dark), hb = rgb(cHi[2] * dark);
        vertex(pose, consumer, cam, a0, loDeg, lr, lg, lb, a, 0.0F, 0.0F);
        vertex(pose, consumer, cam, a1, loDeg, lr, lg, lb, a, 1.0F, 0.0F);
        vertex(pose, consumer, cam, a1, hiDeg, hr, hg, hb, a, 1.0F, 1.0F);
        vertex(pose, consumer, cam, a0, hiDeg, hr, hg, hb, a, 0.0F, 1.0F);
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 cam, double bearingRad,
            double elevDeg, int r, int g, int b, int a, float u, float v) {
        double ce = Math.cos(Math.toRadians(elevDeg));
        double x = cam.x + Math.cos(bearingRad) * ce * RADIUS;
        double y = cam.y + Math.sin(Math.toRadians(elevDeg)) * RADIUS;
        double z = cam.z + Math.sin(bearingRad) * ce * RADIUS;
        consumer.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, -1.0F, 0.0F);
    }

    /** 0 overhead, 1 at the horizon -- the axis the phase tables are sampled on. */
    private static float vertical(double elevDeg) {
        return Mth.clamp((float) (1.0D - elevDeg / 90.0D), 0.0F, 1.0F);
    }

    /** How far this bearing is from the storm's, 0 (far side) to 1 (over the mass). */
    private static float nearness(double bearingRad, double[] storm) {
        if (storm == null) {
            return 0.5F; // no bearing to favour: an even sky
        }
        double dot = Math.cos(bearingRad) * storm[0] + Math.sin(bearingRad) * storm[1];
        double cosNear = Math.cos(Math.toRadians(NEAR_DEG));
        float t = (float) ((dot - cosNear) / (1.0D - cosNear));
        t = Mth.clamp(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float alphaAt(float amp, float weight, float near) {
        return (ALPHA_FAR + (ALPHA_NEAR - ALPHA_FAR) * near) * weight * amp;
    }

    /** The nearest live storm's horizontal direction from the eye, or null. */
    private static double[] bearing(ClientLevel level, Vec3 cam) {
        try {
            double bestD = Double.MAX_VALUE;
            double bx = 0.0D;
            double bz = 0.0D;
            boolean any = false;
            for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                if (d.phase < SHELL_IN) {
                    continue;
                }
                double dx = d.dispX - cam.x;
                double dz = d.dispZ - cam.z;
                double dd = dx * dx + dz * dz;
                if (dd < bestD) {
                    bestD = dd;
                    bx = dx;
                    bz = dz;
                    any = true;
                }
            }
            if (!any) {
                return null;
            }
            double len = Math.sqrt(bestD);
            if (len < 1.0E-4D) {
                return null;
            }
            return new double[]{bx / len, bz / len};
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int rgb(float channel) {
        return Mth.clamp((int) (channel * 255.0F), 0, 255);
    }

    private static float ramp(float v, float lo, float hi) {
        if (hi <= lo) {
            return v >= hi ? 1.0F : 0.0F;
        }
        return Mth.clamp((v - lo) / (hi - lo), 0.0F, 1.0F);
    }
}
