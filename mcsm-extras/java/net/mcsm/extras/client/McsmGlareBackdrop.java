package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Build #375 -- the REAL Telltale glare backdrops.
 *
 * The user supplied the three actual glare/atmosphere images from the
 * Telltale game (used EXACTLY as given, never regenerated):
 *
 *   glare_1  the wide teal-grey smoke haze      -> far shell
 *   glare_2  the deep purple nebula swirl       -> mid shell
 *   glare_3  the purple + orange bloom          -> near shell
 *
 * Each storm gets all three as camera-facing billboard shells pushed out
 * to sky distance (the exact pattern McsmEarlyStormBackdrop proves on
 * 26.2: centre + view*SKY_OFFSET, right/up frame, GlowRenderTypes
 * .translucent so it alpha-blends, no fog, no cull, no depth write - the
 * shells ride the storm, move with it, and can never clip into terrain).
 *
 * "3D-like atmospheric effect": the three shells are different sizes,
 * each rotates its own texture at a different speed (one reverses), so
 * the haze visibly swirls with parallax - a layered volumetric vortex
 * instead of a flat card. The swirl angle is applied to the UVs per
 * vertex, so the geometry stays rock-solid while the smoke turns.
 */
public final class McsmGlareBackdrop {

    private static final Identifier TEAL = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/mcsm/glare/glare_1.png");
    private static final Identifier PURPLE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/mcsm/glare/glare_2.png");
    private static final Identifier BLOOM = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/mcsm/glare/glare_3.png");

    /** Push the shells out to sky distance (same trick as the early
     *  backdrop): the storm moves and they move with it, no clipping. */
    private static final double SKY_OFFSET = 150.0D;

    // per-shell: size factor, spin speed (rad/s), base alpha
    private static final double[] SIZE = new double[]{1.55D, 1.15D, 0.82D};
    private static final double[] SPIN = new double[]{0.011D, -0.017D, 0.026D};
    private static final float[] ALPHA = new float[]{0.30F, 0.38F, 0.46F};

    private McsmGlareBackdrop() {
    }

    public static void submit(LevelRenderContext ctx) {
        if (ctx == null || Minecraft.getInstance() == null
                || !McsmExtrasConfig.glareBackdrop) {
            return;
        }
        try {
            Vec3 camera = ctx.levelState().cameraRenderState.pos;
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            PoseStack poseStack = ctx.poseStack();
            Identifier[] textures = {TEAL, PURPLE, BLOOM};
            for (ClientDistantStormManager.StormData storm : ClientDistantStormManager.all()) {
                float phase = storm.phase;
                if (phase < 1.0F) {
                    continue;
                }
                Vec3 centre = new Vec3(storm.dispX, storm.dispY, storm.dispZ);
                Vec3 toStorm = centre.subtract(camera);
                double distance = toStorm.length();
                if (distance < 1.0E-4D || distance > 2100.0D) {
                    continue;
                }
                Vec3 view = toStorm.scale(1.0D / distance);
                float distanceFade = 1.0F
                        - Mth.clamp((float) ((distance - 900.0D) / 800.0D), 0.0F, 1.0F);
                if (distanceFade <= 0.004F) {
                    continue;
                }
                // the vortex swells with the phase (1 -> 9)
                double bodyRadius = 4.0D + 1.5D * phase;
                double baseRadius = Math.min(320.0D,
                        SKY_OFFSET * Math.min(0.92D, bodyRadius / Math.max(distance, 1.0D)) * 1.9D
                                * McsmExtrasConfig.glareBackdropSize);
                if (baseRadius < 2.0D) {
                    continue;
                }
                float phaseFade = Mth.clamp(0.35F + 0.10F * phase, 0.0F, 1.0F);
                double t = Minecraft.getInstance().level.getGameTime() / 20.0D;
                // near shell first so the far haze draws over it last? No -
                // blend order: far first, near last (near is on top)
                for (int i = 2; i >= 0; i--) {
                    double radius = baseRadius * SIZE[i];
                    float alpha = (int) (255.0F * ALPHA[i] * phaseFade * distanceFade
                            * McsmExtrasConfig.glareBackdropStrength);
                    submitQuad(poseStack, collector,
                            centre.add(view.scale(SKY_OFFSET)), view, radius,
                            textures[i], alpha, t * SPIN[i]);
                }
            }
        } catch (Throwable ignored) {
            // a backdrop must never break the level render
        }
    }

    /** One camera-facing billboard with its texture slowly rotated in UV
     *  space (the swirl). Geometry is the proven early-backdrop frame. */
    private static void submitQuad(PoseStack poseStack, SubmitNodeCollector collector,
            Vec3 at, Vec3 view, double radius, Identifier texture, int alpha, double spin) {
        if (alpha <= 2) {
            return;
        }
        Vec3 upHint = Math.abs(view.y) > 0.98D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = view.cross(upHint).normalize();
        Vec3 up = right.cross(view).normalize();
        Vec3 rx = right.scale(radius * 1.15D);
        Vec3 uy = up.scale(radius);
        final int a = Math.min(alpha, 255);
        final float cos = (float) Math.cos(spin);
        final float sin = (float) Math.sin(spin);
        collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(texture),
                (pose, consumer) -> {
                    vertex(pose, consumer, at.subtract(rx).subtract(uy), rot(0.0F, 1.0F, cos, sin), a);
                    vertex(pose, consumer, at.add(rx).subtract(uy), rot(1.0F, 1.0F, cos, sin), a);
                    vertex(pose, consumer, at.add(rx).add(uy), rot(1.0F, 0.0F, cos, sin), a);
                    vertex(pose, consumer, at.subtract(rx).add(uy), rot(0.0F, 0.0F, cos, sin), a);
                });
    }

    /** Rotate (u,v) around the texture centre by the swirl angle. The
     *  glare images have fully transparent borders, so the wrap-around
     *  corners sample transparency - no visible seam. */
    private static float[] rot(float u, float v, float cos, float sin) {
        float dx = u - 0.5F;
        float dy = v - 0.5F;
        return new float[]{
                0.5F + dx * cos - dy * sin,
                0.5F + dx * sin + dy * cos};
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 at,
            float[] uv, int alpha) {
        consumer.addVertex(pose, (float) at.x, (float) at.y, (float) at.z)
                .setColor(255, 255, 255, alpha)
                .setUv(uv[0], uv[1])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
