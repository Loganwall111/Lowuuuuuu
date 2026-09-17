package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.mcsm.extras.McsmExtrasConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The original backdrop begins at the phase-4 changeover. This fills the
 * earlier phase-1 to phase-3 progression with the same smooth, world-directed
 * atmosphere without adding a mesh, ring, cylinder, or camera-locked card.
 */
public final class McsmEarlyStormBackdrop {
    private static final Identifier BLUE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/backdrop_phase4_blue.png");
    private static final Identifier PURPLE = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/backdrop_purple.png");
    private static final Identifier MAGENTA = Identifier.fromNamespaceAndPath(
            "dabywitherstormmod", "textures/misc/backdrop_purple_pink.png");
    private static final double SKY_OFFSET = 150.0D;

    private McsmEarlyStormBackdrop() {
    }

    public static void submit(LevelRenderContext context) {
        if (context == null || !DabyWSClientConfig.stormBackdropQuad
                || !DabyWSClientConfig.stormBackdrop) {
            return;
        }
        try {
            Vec3 camera = context.levelState().cameraRenderState.pos;
            SubmitNodeCollector collector = context.submitNodeCollector();
            PoseStack poseStack = context.poseStack();
            for (ClientDistantStormManager.StormData storm : ClientDistantStormManager.all()) {
                float phase = storm.phase;
                if (!(phase >= 1.0F) || !(phase < 3.9F)) {
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

                // Keep the early buildup restrained; the full phase-4 backdrop
                // takes over at 3.9 with its established size progression.
                double bodyRadius = 4.0D + 1.5D * phase;
                double radius = Math.min(150.0D,
                        SKY_OFFSET * Math.min(0.60D, bodyRadius / Math.max(distance, 1.0D)) * 1.35D
                                * DabyWSClientConfig.stormBackdropSize);
                if (radius < 1.0D) {
                    continue;
                }

                Vec3 at = centre.add(view.scale(SKY_OFFSET));
                Identifier texture = phase < 2.0F ? BLUE : (phase < 3.0F ? PURPLE : MAGENTA);
                float phaseFade = phase < 2.0F
                        ? 0.12F + 0.12F * phase
                        : phase < 3.0F
                            ? 0.30F + 0.10F * (phase - 2.0F)
                            : 0.44F + 0.12F * (phase - 3.0F);
                int alpha = (int) (255.0F * phaseFade * distanceFade
                        * (float) DabyWSClientConfig.stormBackdropStrength);
                submitQuad(poseStack, collector, at, view, radius, texture, alpha);
            }
        } catch (Throwable ignored) {
            // A presentation helper must never break the level render.
        }
    }

    private static void submitQuad(PoseStack poseStack, SubmitNodeCollector collector,
            Vec3 at, Vec3 view, double radius, Identifier texture, int alpha) {
        if (alpha <= 2) {
            return;
        }
        // BUILD #422 -- the storey above the storm is the upper half of a quad;
        // this one's lower half is stretched down past bedrock level so the
        // early storm's backdrop has a bottom as well as a top (the user's
        // "there's a top layer but there isn't a bottom layer"). Same colour,
        // same sheet, no edge: see McsmStickerStretchMixin for the phase-4.7+
        // pass, which is the same idea applied to the base mod's sticker.
        Vec3 upHint = Math.abs(view.y) > 0.98D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = view.cross(upHint).normalize();
        Vec3 up = right.cross(view).normalize();
        Vec3 rx = right.scale(radius * 1.15D);
        Vec3 uy = up.scale(radius);
        double stretch = Math.min(24.0D, Math.max(1.0D, McsmExtrasConfig.backdropBottomStretch));
        Vec3 low = up.scale(-radius * stretch);
        int a = Math.min(alpha, 255);
        collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(texture),
                (pose, consumer) -> {
                    // upper half: the sheet's top, unmoved
                    vertex(pose, consumer, at.add(uy).subtract(rx), 0.0F, 0.0F, a);
                    vertex(pose, consumer, at.add(uy).add(rx), 1.0F, 0.0F, a);
                    vertex(pose, consumer, at.add(rx), 1.0F, 0.5F, a);
                    vertex(pose, consumer, at.subtract(rx), 0.0F, 0.5F, a);
                    // lower half: same rows, stretched down out of the frustum
                    vertex(pose, consumer, at.subtract(rx), 0.0F, 0.5F, a);
                    vertex(pose, consumer, at.add(rx), 1.0F, 0.5F, a);
                    vertex(pose, consumer, at.add(low).add(rx), 1.0F, 1.0F, a);
                    vertex(pose, consumer, at.add(low).subtract(rx), 0.0F, 1.0F, a);
                });
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 at,
            float u, float v, int alpha) {
        consumer.addVertex(pose, (float) at.x, (float) at.y, (float) at.z)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
