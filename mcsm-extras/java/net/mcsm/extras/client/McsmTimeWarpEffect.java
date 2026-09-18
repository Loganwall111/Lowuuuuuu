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
 * V2 NEXT-GEN Phase 4 - Time Warp Effect: Realistic time warp, not just overlay
 * When flipping Time Machine, realistic time warp appears: seamless portal, 3D colored lighting affect over camera blur blue/green/black shifting, complete warp scene showing universes being passed by.
 * Time router at top shows time rewind in real time when looking at elevator window.
 * Physical active building summon, watch rest of world as time passes.
 * Sodium-safe
 */
public final class McsmTimeWarpEffect {

    private static final Identifier WARP_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/vfx/time_warp.png");
    private static final Identifier WARP_TUNNEL = Identifier.fromNamespaceAndPath("mcsm", "textures/vfx/warp_tunnel.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");

    private static float warpProgress = 0f;
    private static boolean isWarping = false;
    private static String targetEra = "PRESENT_DAY";
    private static float warpTime = 0f;
    private static float chromaticShift = 0f;

    private McsmTimeWarpEffect() {}

    public static void startWarp(String era) {
        isWarping = true;
        warpProgress = 0f;
        targetEra = era;
        warpTime = 0f;
        chromaticShift = 0f;
    }

    public static void tick() {
        if (isWarping) {
            warpProgress += 0.015f;
            warpTime += 0.08f;
            chromaticShift += 0.05f;
            if (warpProgress >= 1f) {
                isWarping = false;
                warpProgress = 0f;
            }
        }
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            if (ctx == null) return;
            if (!isWarping) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;

            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float t = warpTime + partial * 0.1f;

            // Warp progress 0-1
            float prog = warpProgress;
            float invProg = 1f - prog;

            // Full-screen chromatic aberration blur - blue/green/black shifting
            // 3 layers offset for chromatic effect
            float shiftAmount = Mth.sin(chromaticShift) * 2.5f * prog + prog * 1.5f;

            // Blue layer
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                (pose, consumer) -> {
                    double size = 40.0 + prog * 80.0;
                    double x = cam.x + shiftAmount * 0.3;
                    double y = cam.y;
                    double z = cam.z;
                    int alpha = (int)(prog * 90 * invProg * 2);
                    if (alpha < 2) return;
                    // Tunnel effect - many rings passing by, showing universes
                    int rings = 20;
                    for (int i = 0; i < rings; i++) {
                        float ringProg = (t * 0.5f + i * 0.3f) % 1f;
                        float ringSize = 2f + ringProg * 18f;
                        float ringAlpha = (1f - ringProg) * alpha * 0.6f;
                        double rx = x + Mth.sin(ringProg * Mth.TWO_PI + i) * 1.2;
                        double ry = y + Mth.cos(ringProg * Mth.TWO_PI * 0.7f + i * 0.8f) * 1.0;
                        double rz = z - ringProg * 12f - 3f; // Tunnel forward
                        // Color shifts blue
                        int r = 40 + (int)(Mth.sin(t + i) * 20);
                        int g = 80 + (int)(Mth.cos(t * 0.7f + i) * 30);
                        int b = 200 + (int)(Mth.sin(t * 0.5f + i * 1.2f) * 55);
                        emitRing(pose, consumer, new Vec3(rx, ry, rz), ringSize, 16, r, g, b, (int)ringAlpha, cam);
                    }
                });

            // Green layer - offset
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                (pose, consumer) -> {
                    double x = cam.x - shiftAmount * 0.2;
                    double y = cam.y + shiftAmount * 0.1;
                    double z = cam.z;
                    int alpha = (int)(prog * 85 * invProg * 2);
                    if (alpha < 2) return;
                    int rings = 18;
                    for (int i = 0; i < rings; i++) {
                        float ringProg = (t * 0.48f + i * 0.32f + 0.15f) % 1f;
                        float ringSize = 2.2f + ringProg * 16f;
                        float ringAlpha = (1f - ringProg) * alpha * 0.55f;
                        double rx = x + Mth.cos(ringProg * Mth.TWO_PI * 0.9f + i * 1.1f) * 1.0;
                        double ry = y + Mth.sin(ringProg * Mth.TWO_PI + i) * 0.8;
                        double rz = z - ringProg * 11f - 2.5f;
                        int r = 60 + (int)(Mth.cos(t * 0.6f + i) * 30);
                        int g = 180 + (int)(Mth.sin(t + i * 0.7f) * 50);
                        int b = 100 + (int)(Mth.cos(t * 0.8f + i) * 40);
                        emitRing(pose, consumer, new Vec3(rx, ry, rz), ringSize, 14, r, g, b, (int)ringAlpha, cam);
                    }
                });

            // Black layer - central darkness with colored edges
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(WHITE),
                (pose, consumer) -> {
                    double x = cam.x;
                    double y = cam.y;
                    double z = cam.z - 2;
                    int alpha = (int)(prog * 120);
                    // Central black vortex
                    double vortexSize = 1.5 + prog * 12;
                    emitSphere(pose, consumer, new Vec3(x, y, z), vortexSize, 16, 10, 5, 5, 10, alpha, t);
                    // Edge colored shifting - blue/green/black
                    double edgeSize = vortexSize * 1.4;
                    float hue = (chromaticShift * 0.1f) % 1f;
                    float[] rgb = hsvToRgb(hue, 0.9f, 1f);
                    int r = (int)(rgb[0] * 80);
                    int g = (int)(rgb[1] * 100);
                    int b = (int)(rgb[2] * 120);
                    emitSphere(pose, consumer, new Vec3(x, y, z), edgeSize, 14, 8, r, g, b, alpha / 2, t);
                });

            // Universes passing by - show different universe being passed
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WARP_TEX),
                (pose, consumer) -> {
                    int universeCount = 8;
                    for (int u = 0; u < universeCount; u++) {
                        float uProg = (t * 0.12f + u * 0.7f) % 1f;
                        if (uProg > 0.85f) continue;
                        double ux = cam.x + Mth.sin(uProg * Mth.TWO_PI * 1.5f + u * 2.1f) * (3 + uProg * 8);
                        double uy = cam.y + Mth.cos(uProg * Mth.TWO_PI * 0.8f + u) * (2 + uProg * 4);
                        double uz = cam.z - uProg * 18f - 4f;
                        float size = 0.8f + uProg * 2.5f;
                        float alphaF = (1f - uProg) * 0.9f * prog;
                        int alpha = (int)(alphaF * 200);
                        // Universe color based on era
                        float[] col = colorForEra(targetEra, u);
                        int r = (int)(col[0] * 255 * alphaF);
                        int g = (int)(col[1] * 255 * alphaF);
                        int b = (int)(col[2] * 255 * alphaF);
                        quadBillboard(pose, consumer, ux, uy, uz, size, r, g, b, alpha, cam);
                        // Trail
                        quadBillboard(pose, consumer, ux, uy, uz - 0.5, size * 0.6f, r, g, b, alpha / 2, cam);
                    }
                });

            // Time router at top - shows time rewind in real time when looking at elevator window
            if (prog > 0.2f && prog < 0.9f) {
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                    (pose, consumer) -> {
                        double routerY = cam.y + 8 + Mth.sin(t) * 0.5;
                        double routerX = cam.x;
                        double routerZ = cam.z - 6;
                        // Router display - time rewinding
                        float rewindProg = prog;
                        long displayTime = (long)(24000 * (1f - rewindProg));
                        // Digits as glowing quads
                        for (int d = 0; d < 5; d++) {
                            double dx = routerX + (d - 2) * 0.6;
                            int digitAlpha = 180 + (int)(Mth.sin(t * 2f + d) * 40);
                            int digitVal = (int)((displayTime / Math.pow(10, 4 - d)) % 10);
                            // Color based on digit
                            int r = 100 + digitVal * 15;
                            int g = 200 + (int)(Mth.sin(t + d) * 30);
                            int b = 255;
                            quadBillboard(pose, consumer, dx, routerY, routerZ, 0.35f, r, g, b, digitAlpha, cam);
                        }
                        // Label
                        quadBillboard(pose, consumer, routerX, routerY + 0.8, routerZ, 0.5f, 80, 180, 255, 150, cam);
                    });
            }

            // Final flash at end of warp
            if (prog > 0.85f) {
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                    (pose, consumer) -> {
                        float flashAlpha = (prog - 0.85f) / 0.15f;
                        flashAlpha = 1f - flashAlpha;
                        flashAlpha = flashAlpha * flashAlpha;
                        int alpha = (int)(flashAlpha * 200);
                        double flashSize = 20 + flashAlpha * 60;
                        emitSphere(pose, consumer, new Vec3(cam.x, cam.y, cam.z - 1), flashSize, 12, 8, 255, 255, 255, alpha, t);
                    });
            }

        } catch (Throwable ignored) {}
    }

    private static void emitRing(Pose pose, VertexConsumer consumer, Vec3 center, double radius, int segs, int r, int g, int b, int a, Vec3 cam) {
        for (int i = 0; i < segs; i++) {
            float a0 = (float)i / segs * Mth.TWO_PI;
            float a1 = (float)(i + 1) / segs * Mth.TWO_PI;
            double x0 = center.x + Math.cos(a0) * radius;
            double y0 = center.y + Math.sin(a0) * radius * 0.3;
            double z0 = center.z;
            double x1 = center.x + Math.cos(a1) * radius;
            double y1 = center.y + Math.sin(a1) * radius * 0.3;
            double z1 = center.z;
            // Thickness
            double thickness = 0.15;
            quad(pose, consumer,
                x0, y0 - thickness, z0, 0, 0,
                x1, y1 - thickness, z1, 1, 0,
                x1, y1 + thickness, z1, 1, 1,
                x0, y0 + thickness, z0, 0, 1,
                r, g, b, a, 0, 0, 1);
        }
    }

    private static void emitSphere(Pose pose, VertexConsumer consumer, Vec3 center, double radius, int hSegs, int vSegs, int r, int g, int b, int a, float time) {
        for (int v = 0; v < vSegs; v++) {
            double phi0 = Math.PI * v / vSegs;
            double phi1 = Math.PI * (v + 1) / vSegs;
            double y0 = center.y + Math.cos(phi0) * radius;
            double y1 = center.y + Math.cos(phi1) * radius;
            double r0 = Math.sin(phi0) * radius;
            double r1 = Math.sin(phi1) * radius;
            for (int h = 0; h < hSegs; h++) {
                double theta0 = 2.0 * Math.PI * h / hSegs;
                double theta1 = 2.0 * Math.PI * (h + 1) / hSegs;
                double x00 = center.x + Math.cos(theta0) * r0;
                double z00 = center.z + Math.sin(theta0) * r0;
                double x10 = center.x + Math.cos(theta1) * r0;
                double z10 = center.z + Math.sin(theta1) * r0;
                double x01 = center.x + Math.cos(theta0) * r1;
                double z01 = center.z + Math.sin(theta0) * r1;
                double x11 = center.x + Math.cos(theta1) * r1;
                double z11 = center.z + Math.sin(theta1) * r1;
                float u0 = (float)h / hSegs;
                float u1 = (float)(h + 1) / hSegs;
                float v0 = (float)v / vSegs;
                float v1 = (float)(v + 1) / vSegs;
                quad(pose, consumer,
                    x00, y0, z00, u0, v0,
                    x10, y0, z10, u1, v0,
                    x11, y1, z11, u1, v1,
                    x01, y1, z01, u0, v1,
                    r, g, b, a, (float)(x00 - center.x), (float)(y0 - center.y), (float)(z00 - center.z));
            }
        }
    }

        private static void quadBillboard(Pose pose, VertexConsumer consumer, double x, double y, double z, float size, int r, int g, int b, int a, Vec3 cam) {
        Vec3 pos = new Vec3(x, y, z);
        Vec3 toCam = cam.subtract(pos).normalize();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = toCam.cross(up).normalize();
        if (right.lengthSqr() < 0.001) right = new Vec3(1, 0, 0);
        up = right.cross(toCam).normalize();
        Vec3 rx = right.scale(size);
        Vec3 uy = up.scale(size);
        vertex(pose, consumer, pos.x - rx.x - uy.x, pos.y - rx.y - uy.y, pos.z - rx.z - uy.z, 0, 1, r, g, b, a, 0, 1, 0);
        vertex(pose, consumer, pos.x + rx.x - uy.x, pos.y + rx.y - uy.y, pos.z + rx.z - uy.z, 1, 1, r, g, b, a, 0, 1, 0);
        vertex(pose, consumer, pos.x + rx.x + uy.x, pos.y + rx.y + uy.y, pos.z + rx.z + uy.z, 1, 0, r, g, b, a, 0, 1, 0);
        vertex(pose, consumer, pos.x - rx.x + uy.x, pos.y - rx.y + uy.y, pos.z - rx.z + uy.z, 0, 0, r, g, b, a, 0, 1, 0);
    }

    private static void vertex(Pose pose, VertexConsumer consumer,
            double x, double y, double z, float u, float v,
            int r, int g, int b, int a, float nx, float ny, float nz) {
        consumer.addVertex(pose, (float) x, (float) y, (float) z)
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

    private static float[] colorForEra(String era, int idx) {
        return switch (era) {
            case "BEGINNING_EARTH" -> new float[]{0.9f, 0.8f, 0.6f};
            case "DINOSAUR_AGE" -> new float[]{0.4f, 0.7f, 0.3f};
            case "ROMAN_EMPIRE" -> new float[]{0.8f, 0.6f, 0.4f};
            case "MEDIEVAL_KINGS" -> new float[]{0.5f, 0.5f, 0.7f};
            case "CYBERPUNK_FUTURE" -> new float[]{0.2f, 0.9f, 0.9f};
            case "SPACE_AGE" -> new float[]{0.6f, 0.4f, 0.9f};
            default -> new float[]{0.7f, 0.7f, 0.8f};
        };
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

    public static boolean isWarping() { return isWarping; }
    public static float getProgress() { return warpProgress; }
    public static String getTargetEra() { return targetEra; }
}
