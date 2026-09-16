package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

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
import net.mcsm.extras.McsmReality;

/**
 * BUILD #434 -- THE BOTTOM LAYER OF THE SKY.
 *
 * <p>The report, twice over: "the skies are still only the top layer. you need to
 * add a bottom layer covering the whole bottom" and "there's a top layer but
 * there isn't a bottom layer".
 *
 * <p>WHY THE SHADER FIX WAS NOT ENOUGH. #422 stretched the sky's floor row
 * downward inside {@code sky.fsh} -- and {@code sky.fsh} is a program in the
 * SHADER PACK. A player who has not installed the pack never runs it, and what
 * they see under the horizon is the vanilla sky, which simply has no colour
 * there. That is the whole report: the top layer is the storm's sky, the bottom
 * layer is nothing. So the band is drawn here, in the world, out of ordinary
 * geometry, and it does not care whether any shader is installed.
 *
 * <p>WHAT IT IS. A camera-anchored cylinder wall: {@link #SEGMENTS} quads all
 * the way around the player, starting at the horizon and running
 * {@link #DEPTH} blocks straight down, deep past bedrock and into the void under
 * the world. Its top edge carries the live horizon colour of whatever owns the
 * sky -- the storm's own band, read from the same {@link McsmStormPhase} feed the
 * native sky and the halo use, or the decayed reality's own purple -- and it
 * deepens as it falls, with the exact curve {@code sky.fsh} uses, so the wall
 * and the sky above it are one continuous surface with no seam and no edge.
 *
 * <p>WHY IT CANNOT COVER ANYTHING IT SHOULD NOT. It is submitted with the same
 * depth-tested translucent type the atmosphere wall uses, at
 * {@link #RADIUS} blocks out, and it is opaque-but-for-alpha: terrain, mobs and
 * the player are all nearer than that, so the depth test keeps every one of them
 * in front of it. It only draws where the vanilla sky would otherwise show
 * through -- under the horizon and past the fog.
 */
public final class McsmSkyFloorBand {

    private static final Identifier WHITE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/storm_white.png");

    /** Quads around the full circle. 64 * 240 blocks of wall is still one draw. */
    private static final int SEGMENTS = 64;
    /** How far out the wall stands. Terrain inside this is always in front of it. */
    private static final double RADIUS = 240.0D;
    /** How far down it runs: past bedrock, into the void, with no bottom edge. */
    private static final double DEPTH = 512.0D;
    /** The seam guard: the top of the wall eases in over this many blocks. */
    private static final double FADE = 8.0D;
    /** Alpha at the very top, where the wall meets the live sky (invisible). */
    private static final float SEAM_ALPHA = 0.0F;
    /** Alpha everywhere below the seam guard: the sky under the world, solid. */
    private static final float FLOOR_ALPHA = 0.94F;

    private McsmSkyFloorBand() {
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            if (!McsmExtrasConfig.skyFloorBand || ctx == null) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) {
                return;
            }
            float phase = McsmStormPhase.phase();
            boolean decayed = McsmReality.inside(level);
            // The band belongs to whatever owns the sky: the storm's band from
            // its onset upward, or the decayed reality, whose sky is the mod's
            // own purple whether or not a storm is in it.
            if (!decayed && phase < McsmStormPhase.PHASE_MIN) {
                return;
            }
            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            // The colour is whatever the sky above the horizon is wearing right
            // now, read from the same feed the native sky and the halo use --
            // including the decayed reality's own violet, so the wall under it is
            // the dimension's colour and not a second, invented one.
            float[] horizon;
            if (decayed && phase < McsmStormPhase.PHASE_MIN) {
                horizon = new float[]{0x4A / 255.0F, 0x2A / 255.0F, 0x6E / 255.0F};
            } else {
                horizon = McsmStormPhase.horizonFor(Math.max(phase, McsmStormPhase.PHASE_MIN));
            }
            if (horizon == null || horizon.length < 3) {
                return;
            }

            final double top = cam.y + 0.5D;
            final double bottom = top - DEPTH;
            final float r = Mth.clamp(horizon[0], 0.0F, 1.0F);
            final float g = Mth.clamp(horizon[1], 0.0F, 1.0F);
            final float b = Mth.clamp(horizon[2], 0.0F, 1.0F);
            final double cx = cam.x;
            final double cz = cam.z;

            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(WHITE),
                    (pose, consumer) -> {
                        for (int i = 0; i < SEGMENTS; i++) {
                            double a0 = 2.0D * Math.PI * i / SEGMENTS;
                            double a1 = 2.0D * Math.PI * (i + 1) / SEGMENTS;
                            double x0 = cx + Math.cos(a0) * RADIUS;
                            double z0 = cz + Math.sin(a0) * RADIUS;
                            double x1 = cx + Math.cos(a1) * RADIUS;
                            double z1 = cz + Math.sin(a1) * RADIUS;
                            // Two bands per segment: the eased top (so the seam
                            // with the live sky is invisible) and the long fall
                            // down to the void, where the colour deepens on the
                            // same curve sky.fsh uses.
                            quad(pose, consumer, x0, top, z0, x1, top, z1,
                                    x1, top - FADE, z1, x0, top - FADE, z0,
                                    r, g, b, SEAM_ALPHA, FLOOR_ALPHA);
                            quad(pose, consumer, x0, top - FADE, z0, x1, top - FADE, z1,
                                    x1, bottom, z1, x0, bottom, z0,
                                    r, g, b, FLOOR_ALPHA, FLOOR_ALPHA);
                        }
                    });
        } catch (Throwable ignored) {
            // a sky that throws is worse than a sky with one layer
        }
    }

    /**
     * One wall quad: vertices 0 and 1 are the UPPER edge, 2 and 3 the LOWER one,
     * so a band can hand its two edge alphas straight through -- which is how the
     * top of the wall fades in against the live sky and the bottom of it never
     * ends.
     */
    private static void quad(Pose pose, VertexConsumer consumer,
            double x0, double y0, double z0, double x1, double y1, double z1,
            double x2, double y2, double z2, double x3, double y3, double z3,
            float r, float g, float b, float alphaUpper, float alphaLower) {
        vertex(pose, consumer, x0, y0, z0, r, g, b, alphaUpper, 0.0F);
        vertex(pose, consumer, x1, y1, z1, r, g, b, alphaUpper, 1.0F);
        vertex(pose, consumer, x2, y2, z2, r, g, b, alphaLower, 0.0F);
        vertex(pose, consumer, x3, y3, z3, r, g, b, alphaLower, 1.0F);
    }

    private static void vertex(Pose pose, VertexConsumer consumer, double x, double y, double z,
            float r, float g, float b, float alpha, float u) {
        consumer.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor(r, g, b, alpha)
                .setUv(u, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
