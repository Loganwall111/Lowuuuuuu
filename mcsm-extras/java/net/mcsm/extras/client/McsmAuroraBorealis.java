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

/**
 * V2 NEXT-GEN - Aurora Borealis
 * Real 3D curtains of light that ripple across northern sky at night.
 * Color shifts green/purple/pink/blue, animated waves, Sodium-safe.
 */
public final class McsmAuroraBorealis {

    private static final Identifier AURORA_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/aurora_borealis.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");

    private static float waveTime = 0f;

    private McsmAuroraBorealis() {}

    public static void submit(LevelRenderContext ctx) {
        try {
            if (ctx == null) return;
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;
            String dim = level.dimension().identifier().toString();
            if (dim.contains("end") || dim.contains("nether")) return; // Overworld + MCSM dims only

            float dayTime = ((level.getGameTime() % 24000L) / 24000.0f);
            // Only at night
            if (dayTime > 0.3f && dayTime < 0.7f) return;

            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float t = level.getGameTime() + partial;

            waveTime += 0.015f;

            // Aurora appears in northern sky, high up, curtains
            double auroraDist = 320.0;
            double auroraHeightBase = 180.0;
            double auroraHeightTop = 340.0;

            // Multiple curtains
            int curtains = 5;
            for (int c = 0; c < curtains; c++) {
                float curtainOffset = (float)c / curtains * Mth.TWO_PI * 0.6f;
                float hueBase = 0.35f + c * 0.15f; // green -> cyan -> blue -> purple -> pink
                if (hueBase > 1f) hueBase -= 1f;

                collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(AURORA_TEX),
                    (pose, consumer) -> {
                        int segments = 24;
                        double width = 120.0 + Mth.sin(waveTime * 0.5f + c) * 20.0;
                        double halfWidth = width / 2.0;
                        double centerX = cam.x + Mth.cos(curtainOffset) * auroraDist * 0.3;
                        double centerZ = cam.z + Mth.sin(curtainOffset) * auroraDist + auroraDist * 0.8; // North
                        // Curtain as vertical ribbon with wave
                        for (int i = 0; i < segments; i++) {
                            float u0 = (float)i / segments;
                            float u1 = (float)(i + 1) / segments;
                            double x0 = centerX + (u0 - 0.5) * width;
                            double x1 = centerX + (u1 - 0.5) * width;
                            double z = centerZ;
                            // Wave displacement
                            float wave0 = Mth.sin(waveTime + u0 * 6f + c * 1.2f) * 12f + Mth.cos(waveTime * 0.7f + u0 * 4f) * 8f;
                            float wave1 = Mth.sin(waveTime + u1 * 6f + c * 1.2f) * 12f + Mth.cos(waveTime * 0.7f + u1 * 4f) * 8f;
                            double yBottom0 = cam.y + auroraHeightBase + wave0 * 0.3;
                            double yTop0 = cam.y + auroraHeightTop + wave0 + Mth.sin(waveTime * 0.8f + u0 * 5f) * 18f;
                            double yBottom1 = cam.y + auroraHeightBase + wave1 * 0.3;
                            double yTop1 = cam.y + auroraHeightTop + wave1 + Mth.sin(waveTime * 0.8f + u1 * 5f) * 18f;

                            // Color shift along curtain
                            float hue = (hueBase + u0 * 0.15f + Mth.sin(waveTime * 0.3f + c) * 0.05f) % 1f;
                            float[] rgb = hsvToRgb(hue, 0.85f, 1f);
                            // Brightness pulse
                            float pulse = 0.7f + 0.3f * Mth.sin(waveTime * 0.9f + u0 * 3f + c);
                            int r = (int)(rgb[0] * 255 * pulse);
                            int g = (int)(rgb[1] * 255 * pulse);
                            int b = (int)(rgb[2] * 255 * pulse);
                            int alpha = (int)(90 * pulse + 20);

                            // Vertical gradient - more transparent at bottom
                            quad(pose, consumer,
                                x0, yBottom0, z, u0, 1,
                                x1, yBottom1, z, u1, 1,
                                x1, yTop1, z, u1, 0,
                                x0, yTop0, z, u0, 0,
                                r, g, b, alpha, 0, 0, 1);

                            // Second layer for depth - slightly offset
                            float hue2 = (hue + 0.08f) % 1f;
                            float[] rgb2 = hsvToRgb(hue2, 0.75f, 1f);
                            int r2 = (int)(rgb2[0] * 255 * pulse * 0.7f);
                            int g2 = (int)(rgb2[1] * 255 * pulse * 0.7f);
                            int b2 = (int)(rgb2[2] * 255 * pulse * 0.7f);
                            quad(pose, consumer,
                                x0, yBottom0, z + 3, u0, 1,
                                x1, yBottom1, z + 3, u1, 1,
                                x1, yTop1, z + 3, u1, 0,
                                x0, yTop0, z + 3, u0, 0,
                                r2, g2, b2, alpha / 2, 0, 0, 1);
                        }
                    });
            }

            // Additional glow layer - faint overall aurora haze
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                (pose, consumer) -> {
                    double hazeY = cam.y + 260;
                    double hazeR = 280;
                    int segs = 20;
                    for (int i = 0; i < segs; i++) {
                        float a0 = (float)i / segs * Mth.PI * 0.8f - Mth.PI * 0.4f; // Northern arc
                        float a1 = (float)(i + 1) / segs * Mth.PI * 0.8f - Mth.PI * 0.4f;
                        double x0 = cam.x + Math.cos(a0) * hazeR;
                        double z0 = cam.z + Math.sin(a0) * hazeR + hazeR * 0.6;
                        double x1 = cam.x + Math.cos(a1) * hazeR;
                        double z1 = cam.z + Math.sin(a1) * hazeR + hazeR * 0.6;
                        float hue = 0.55f + Mth.sin(waveTime * 0.2f + i * 0.3f) * 0.15f;
                        float[] rgb = hsvToRgb(hue, 0.6f, 1f);
                        int r = (int)(rgb[0] * 60);
                        int g = (int)(rgb[1] * 90);
                        int b = (int)(rgb[2] * 120);
                        quad(pose, consumer,
                            x0, cam.y + 160, z0, 0, 1,
                            x1, cam.y + 160, z1, 1, 1,
                            x1, hazeY + Mth.sin(waveTime + i * 0.5f) * 12, z1, 1, 0,
                            x0, hazeY + Mth.sin(waveTime + i * 0.5f) * 12, z0, 0, 0,
                            r, g, b, 18, 0, 1, 0);
                    }
                });

        } catch (Throwable ignored) {}
    }

    private static void vertex(Pose pose, VertexConsumer consumer, double x, double y, double z, float u, float v, int r, int g, int b, int a, float nx, float ny, float nz) {
        consumer.addVertex(pose, (float)x, (float)y, (float)z)
            .setColor(r, g, b, a)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(15728880)
            .setNormal(pose, nx, ny, nz);
    }

    private static void quad(Pose pose, VertexConsumer consumer,
                             double x0, double y0, double z0, float u0, float v0,
                             double x1, double y1, double z1, float u1, float v1,
                             double x2, double y2, double z2, float u2, float v2,
                             double x3, double y3, double z3, float u3, float v3,
                             int r, int g, int b, int a, float nx, float ny, float nz) {
        vertex(pose, consumer, x0, y0, z0, u0, v0, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, x1, y1, z1, u1, v1, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, x2, y2, z2, u2, v2, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, x3, y3, z3, u3, v3, r, g, b, a, nx, ny, nz);
    }

    private static float[] hsvToRgb(float h, float s, float v) {
        float r, g, b;
        int i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            case 5 -> { r = v; g = p; b = q; }
            default -> { r = 0; g = 0; b = 0; }
        }
        return new float[]{r, g, b};
    }
}
