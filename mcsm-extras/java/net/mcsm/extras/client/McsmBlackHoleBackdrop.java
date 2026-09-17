package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * V2 EPIC - Black Hole 3D Backdrop Interactive Sky
 * Real 3D sphere, not billboard. Every dimension has its own pretty cool sky plus wither storm with different colours and gradients.
 * Features:
 * - Real 3D sphere mesh (lat/long) for black hole core
 * - Growing perspective, interactive enterable
 * - Accretion disk as 3D torus
 * - Photon ring, lensed starlight, event horizon
 * - Dimension-specific colours: void purple, sift cyan/pink, end void black/purple, overworld storm blue
 * - Sodium-safe: uses SubmitNodeCollector / GlowRenderTypes
 */
public final class McsmBlackHoleBackdrop {

    private static final Identifier BLACK_HOLE_TEXTURE = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/black_hole_sky.png");
    private static final Identifier ACCRETION_DISK = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/accretion_disk.png");
    private static final Identifier LENSED_STARLIGHT_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/lensed_starlight.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");

    private static float time = 0f;
    private static float blackHoleSize = 0.35f;
    private static Vec3 blackHoleWorldPos = new Vec3(0, 400, 0);
    private static boolean isEntering = false;
    private static float enterProgress = 0f;

    private static final float MAX_SIZE = 2.2f;
    private static final float GROWTH_RATE = 0.0004f;

    private McsmBlackHoleBackdrop() {}

    public static void tick() {
        time += 0.02f;
        if (blackHoleSize < MAX_SIZE) {
            blackHoleSize += GROWTH_RATE;
        }
        if (isEntering) {
            enterProgress += 0.018f;
            if (enterProgress >= 1f) {
                enterProgress = 0f;
                isEntering = false;
                triggerBlackHoleEnter();
            }
        }
    }

    /** Called from world render - Sodium-safe 3D sphere */
    public static void submitWorld(LevelRenderContext ctx) {
        try {
            if (ctx == null) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;
            if (!shouldRender(level)) return;

            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float t = Mth.lerp(partial, time - 0.02f, time);
            float size = blackHoleSize + Mth.sin(t * 0.5f) * 0.02f;

            // Determine dimension colours
            String dimId = level.dimension().identifier().toString();
            float[] col = colorForDimension(dimId, t);
            int r = (int)(col[0] * 255);
            int g = (int)(col[1] * 255);
            int b = (int)(col[2] * 255);

            // Black hole world position - centered above, but also for End
            Vec3 bhPos = getBlackHolePos(level, cam, t);
            double distToCam = bhPos.subtract(cam).length();
            if (distToCam < 10.0) return;

            // Sphere radius based on size and distance
            double sphereRadius = 18.0 + size * 28.0;
            // Accretion disk radius
            double diskInner = sphereRadius * 1.6;
            double diskOuter = sphereRadius * 3.8;

            // Render black hole core as real 3D sphere mesh
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(BLACK_HOLE_TEXTURE),
                    (pose, consumer) -> {
                        emitSphere(pose, consumer, bhPos, sphereRadius, 24, 16, 0, 0, 0, 255, t);
                    });

            // Photon ring - bright edge around sphere
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                    (pose, consumer) -> {
                        double ringR = sphereRadius * 1.08;
                        int segments = 48;
                        for (int i = 0; i < segments; i++) {
                            float a0 = (float)i / segments * Mth.TWO_PI;
                            float a1 = (float)(i + 1) / segments * Mth.TWO_PI;
                            double x0 = bhPos.x + Math.cos(a0) * ringR;
                            double z0 = bhPos.z + Math.sin(a0) * ringR;
                            double x1 = bhPos.x + Math.cos(a1) * ringR;
                            double z1 = bhPos.z + Math.sin(a1) * ringR;
                            double y = bhPos.y;
                            float hue = (a0 / Mth.TWO_PI + t * 0.01f) % 1f;
                            float[] rgb = hsvToRgb(hue, 0.9f, 1f);
                            // blend dimension color with rainbow
                            int rr = (int)((rgb[0] * 0.6 + col[0] * 0.4) * 255);
                            int gg = (int)((rgb[1] * 0.6 + col[1] * 0.4) * 255);
                            int bb = (int)((rgb[2] * 0.6 + col[2] * 0.4) * 255);
                            // quad for ring thickness
                            double thickness = 1.2;
                            quad(pose, consumer,
                                    x0, y - thickness, z0, 0, 0,
                                    x1, y - thickness, z1, 1, 0,
                                    x1, y + thickness, z1, 1, 1,
                                    x0, y + thickness, z0, 0, 1,
                                    rr, gg, bb, 200, 0, 1, 0);
                        }
                    });

            // Accretion disk - 3D torus-like flat disk with rotation
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(ACCRETION_DISK),
                    (pose, consumer) -> {
                        int segs = 48;
                        float rot = t * 0.25f;
                        for (int i = 0; i < segs; i++) {
                            float a0 = (float)i / segs * Mth.TWO_PI + rot;
                            float a1 = (float)(i + 1) / segs * Mth.TWO_PI + rot;
                            double x0i = bhPos.x + Math.cos(a0) * diskInner;
                            double z0i = bhPos.z + Math.sin(a0) * diskInner;
                            double x0o = bhPos.x + Math.cos(a0) * diskOuter;
                            double z0o = bhPos.z + Math.sin(a0) * diskOuter;
                            double x1i = bhPos.x + Math.cos(a1) * diskInner;
                            double z1i = bhPos.z + Math.sin(a1) * diskInner;
                            double x1o = bhPos.x + Math.cos(a1) * diskOuter;
                            double z1o = bhPos.z + Math.sin(a1) * diskOuter;
                            double y = bhPos.y + Math.sin(a0 * 2.0 + t * 0.1) * 1.5;
                            float hue = (a0 / Mth.TWO_PI + t * 0.008f) % 1f;
                            float[] rgb = hsvToRgb(hue, 0.85f, 1f);
                            int rr = (int)((rgb[0] * 0.5 + col[0] * 0.5) * 255);
                            int gg = (int)((rgb[1] * 0.5 + col[1] * 0.5) * 255);
                            int bb = (int)((rgb[2] * 0.5 + col[2] * 0.5) * 255);
                            int alpha = 90 + (int)(Math.sin(a0 * 3 + t * 0.2) * 20);
                            // disk quad with slight warping for 3D effect
                            quad(pose, consumer,
                                    x0i, y, z0i, 0, 0,
                                    x1i, y, z1i, 1, 0,
                                    x1o, y + 0.5, z1o, 1, 1,
                                    x0o, y + 0.5, z0o, 0, 1,
                                    rr, gg, bb, alpha, 0, 1, 0);
                        }
                    });

            // Lensed starlight - stars bent around black hole
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(LENSED_STARLIGHT_TEX),
                    (pose, consumer) -> {
                        for (int i = 0; i < 40; i++) {
                            float ang = (float)i / 40f * Mth.TWO_PI + t * 0.05f;
                            float rad = (float)(diskOuter * (1.1 + Math.sin(t * 0.3 + i) * 0.15));
                            double x = bhPos.x + Math.cos(ang) * rad;
                            double z = bhPos.z + Math.sin(ang) * rad;
                            double y = bhPos.y + Math.sin(ang * 2 + t * 0.1) * 4.0;
                            float starSize = 0.8f + Mth.sin(t * 2f + i) * 0.4f;
                            // lensing stretch
                            float stretch = 1f + 0.15f * (float)(sphereRadius / rad * 10.0);
                            // billboard star
                            quadBillboard(pose, consumer, x, y, z, starSize, stretch, 255, 255, 255, 180, cam);
                        }
                    });

            // Event horizon glow - interactive
            Camera camera = ctx.levelState().cameraRenderState.camera;
            Vec3 toBH = bhPos.subtract(cam).normalize();
            if (isPlayerLookingAtBlackHole(camera, toBH)) {
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                        (pose, consumer) -> {
                            float pulse = Mth.sin(t * 3f) * 0.2f + 0.8f;
                            double glowR = sphereRadius * 1.4 * pulse;
                            // glow sphere slightly larger
                            emitSphere(pose, consumer, bhPos, glowR, 16, 10, r, g, b, 35, t);
                        });
                if (mc.player != null && mc.player.isShiftKeyDown() && !isEntering) {
                    isEntering = true;
                    enterProgress = 0f;
                }
            }

            // Entering effect - screen warp
            if (isEntering) {
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                        (pose, consumer) -> {
                            double expand = sphereRadius * (1.0 + enterProgress * 8.0);
                            int alpha = (int)((1.0f - enterProgress) * 120);
                            emitSphere(pose, consumer, bhPos, expand, 12, 8, r, g, b, alpha, t);
                        });
            }

        } catch (Throwable ignored) {}
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
                // black hole core is black, but with slight color at edges
                int rr = r, gg = g, bb = b, aa = a;
                if (v == 0 || v == vSegs - 1) {
                    // poles darker
                    rr = rr / 3; gg = gg / 3; bb = bb / 3;
                }
                if (r == 0 && g == 0 && b == 0) {
                    // pure black core
                    rr = 5; gg = 3; bb = 10;
                }
                // two triangles as quad
                quad(pose, consumer,
                        x00, y0, z00, u0, v0,
                        x10, y0, z10, u1, v0,
                        x11, y1, z11, u1, v1,
                        x01, y1, z01, u0, v1,
                        rr, gg, bb, aa,
                        (float)(x00 - center.x), (float)(y0 - center.y), (float)(z00 - center.z));
            }
        }
    }

    private static void quadBillboard(Pose pose, VertexConsumer consumer, double x, double y, double z, float size, float stretch, int r, int g, int b, int a, Vec3 cam) {
        Vec3 pos = new Vec3(x, y, z);
        Vec3 toCam = cam.subtract(pos).normalize();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = toCam.cross(up).normalize();
        if (right.lengthSqr() < 0.001) right = new Vec3(1, 0, 0);
        up = right.cross(toCam).normalize();
        Vec3 rx = right.scale(size);
        Vec3 uy = up.scale(size * stretch);
        vertex(pose, consumer, pos.subtract(rx).subtract(uy), 0, 1, r, g, b, a, 0, 1, 0);
        vertex(pose, consumer, pos.add(rx).subtract(uy), 1, 1, r, g, b, a, 0, 1, 0);
        vertex(pose, consumer, pos.add(rx).add(uy), 1, 0, r, g, b, a, 0, 1, 0);
        vertex(pose, consumer, pos.subtract(rx).add(uy), 0, 0, r, g, b, a, 0, 1, 0);
    }

    private static float[] colorForDimension(String dimId, float time) {
        float pulse = 0.85f + 0.15f * Mth.sin(time * 0.5f);
        if (dimId.contains("end") || dimId.contains("the_end")) {
            // End - black hole epic purple/black
            return new float[]{0.6f * pulse, 0.2f * pulse, 0.9f * pulse};
        } else if (dimId.contains("void")) {
            // Void - purple pink blue
            return new float[]{0.5f + 0.2f * Mth.sin(time * 0.3f), 0.3f, 0.9f * pulse};
        } else if (dimId.contains("sift")) {
            // Sift - cyan pink
            return new float[]{0.4f, 0.7f * pulse, 1.0f * pulse};
        } else if (dimId.contains("adams")) {
            // Adams infinity - warm amber
            return new float[]{0.9f * pulse, 0.6f, 0.4f};
        } else if (dimId.contains("creator")) {
            // Creator - gold white
            return new float[]{1.0f * pulse, 0.9f * pulse, 0.6f};
        } else {
            // Overworld / decayed - wither storm blue
            return new float[]{0.25f, 0.45f, 1.0f * pulse};
        }
    }

    private static Vec3 getBlackHolePos(ClientLevel level, Vec3 cam, float time) {
        String dimId = level.dimension().identifier().toString();
        if (dimId.contains("end") || dimId.contains("the_end")) {
            // In End, black hole centered high above, epic
            return new Vec3(0, cam.y + 350 + Math.sin(time * 0.1) * 10, 0);
        } else if (dimId.contains("void")) {
            return new Vec3(cam.x + Math.sin(time * 0.05) * 20, cam.y + 380, cam.z + Math.cos(time * 0.05) * 20);
        } else if (dimId.contains("sift")) {
            return new Vec3(cam.x + 100 + Math.sin(time * 0.03) * 30, cam.y + 320, cam.z);
        } else {
            // Other dims - subtle distant black hole
            return new Vec3(cam.x + 200, cam.y + 400, cam.z + 200);
        }
    }

    private static boolean shouldRender(ClientLevel level) {
        try {
            if (!McsmExtrasConfig.paintedSky) return false;
            String dim = level.dimension().identifier().toString();
            // Render in all MCSM dimensions + End + when black hole size large
            return dim.contains("void") || dim.contains("sift") || dim.contains("mcsm") || dim.contains("end") || dim.contains("adams") || dim.contains("creator") || blackHoleSize > 0.8f;
        } catch (Throwable t) {
            return true;
        }
    }

    private static boolean isPlayerLookingAtBlackHole(Camera camera, Vec3 toBlackHole) {
        Vec3 look = Vec3.directionFromRotation(camera.getXRot(), camera.getYRot());
        double dot = look.dot(toBlackHole);
        return dot > 0.93;
    }

    private static void triggerBlackHoleEnter() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5§lEntering singularity... Reality folds §8[§dSHIFT§8]"), false);
            }
        } catch (Throwable ignored) {}
    }

    public static float getSize() { return blackHoleSize; }
    public static float getEnterProgress() { return enterProgress; }
    public static boolean isEntering() { return isEntering; }
    public static void reset() { blackHoleSize = 0.35f; isEntering = false; enterProgress = 0f; }
    public static void setWorldPos(Vec3 pos) { blackHoleWorldPos = pos; }

    // Legacy render method kept for compatibility but now delegates
    public static void render(com.mojang.blaze3d.vertex.PoseStack poseStack, org.joml.Matrix4f projection, float partialTicks, Camera camera) {
        // No-op: new path uses submitWorld
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

    private static void vertex(Pose pose, VertexConsumer consumer, double x, double y, double z, float u, float v, int r, int g, int b, int a, float nx, float ny, float nz) {
        consumer.addVertex(pose, (float)x, (float)y, (float)z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728810)
                .setNormal(pose, nx, ny, nz);
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
