package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
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
        // Build #384: purged with the legacy stage merge - early phases keep
        // the strict vanilla sky (no backdrop disc), Phase 4+ glare comes
        // from McsmStormBlob / McsmGlareBackdrop only.
        return;
    }

    @SuppressWarnings("unused")
    private static void submitLegacy(LevelRenderContext context) {
        if (context == null || !DabyWSClientConfig.stormBackdropQuad
                || !DabyWSClientConfig.stormBackdrop) {
            return;
        }
        if (McsmSkybox.skyPassCancellable()) {
            // Build #381: the rendered phase-sky sphere fully covers the view
            // now; this quad would only show as a wrong pale disc floating on
            // top of the intended sky (the "giant lavender disc" artifact).
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
        Vec3 upHint = Math.abs(view.y) > 0.98D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = view.cross(upHint).normalize();
        Vec3 up = right.cross(view).normalize();
        Vec3 rx = right.scale(radius * 1.15D);
        Vec3 uy = up.scale(radius);
        int a = Math.min(alpha, 255);
        collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(texture),
                (pose, consumer) -> {
                    vertex(pose, consumer, at.subtract(rx).subtract(uy), 0.0F, 1.0F, a);
                    vertex(pose, consumer, at.add(rx).subtract(uy), 1.0F, 1.0F, a);
                    vertex(pose, consumer, at.add(rx).add(uy), 1.0F, 0.0F, a);
                    vertex(pose, consumer, at.subtract(rx).add(uy), 0.0F, 0.0F, a);
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
