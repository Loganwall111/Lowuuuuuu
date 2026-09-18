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

import java.util.ArrayList;
import java.util.List;

/**
 * V2 NEXT-GEN Phase 2 - Ambient World: fireflies emitting lights, luminescent water glow, wolf howling, crickets
 * Fireflies as 3D glowing particles, bioluminescent water creatures, ambient sound triggers.
 * Sodium-safe
 */
public final class McsmAmbientWorld {

    private static final Identifier FIREFLY_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/vfx/firefly.png");
    private static final Identifier BIOLUM_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/water/bioluminescent.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");

    private static class Firefly {
        Vec3 pos;
        Vec3 vel;
        float phase;
        float size;
        int r, g, b;
        float life;

        Firefly(Vec3 p, Vec3 v, float ph, float sz, int r, int g, int b) {
            pos = p; vel = v; phase = ph; size = sz; this.r = r; this.g = g; this.b = b; life = 0f;
        }
    }

    private static final List<Firefly> fireflies = new ArrayList<>();
    private static float ambientTime = 0f;
    private static int tickCounter = 0;
    private static float lastWolfHowl = 0f;
    private static float lastCricket = 0f;

    private McsmAmbientWorld() {}

    public static void tick() {
        ambientTime += 0.02f;
        tickCounter++;
        // Spawn fireflies at night in forests
        if (tickCounter % 15 == 0 && Math.random() < 0.6) {
            spawnFirefly();
        }
        // Update fireflies
        fireflies.removeIf(f -> {
            f.pos = f.pos.add(f.vel.x * 0.08, f.vel.y * 0.08 + Mth.sin(ambientTime + f.phase) * 0.02, f.vel.z * 0.08);
            f.vel = f.vel.add((Math.random() - 0.5) * 0.02, (Math.random() - 0.5) * 0.01, (Math.random() - 0.5) * 0.02);
            f.vel = f.vel.scale(0.98);
            f.life += 0.01f;
            return f.life > 1f;
        });

        // Ambient sounds - trigger logic (actual sound playing handled elsewhere, here we just track timing)
        if (tickCounter % 200 == 0) {
            if (Math.random() < 0.15) {
                // Wolf howl at night
                lastWolfHowl = ambientTime;
            }
            if (Math.random() < 0.3) {
                // Crickets chirping
                lastCricket = ambientTime;
            }
        }
    }

    private static void spawnFirefly() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            Vec3 cam = mc.player != null ? mc.player.position() : new Vec3(0,0,0);
            double angle = Math.random() * Mth.TWO_PI;
            double dist = 8 + Math.random() * 24;
            double x = cam.x + Math.cos(angle) * dist;
            double z = cam.z + Math.sin(angle) * dist;
            double y = cam.y + (Math.random() - 0.3) * 6;
            Vec3 pos = new Vec3(x, y, z);
            Vec3 vel = new Vec3((Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.1, (Math.random() - 0.5) * 0.3);
            float phase = (float)Math.random() * Mth.TWO_PI;
            float size = 0.18f + (float)Math.random() * 0.15f;
            // Firefly colors - yellow, greenish, warm
            float hue = 0.15f + (float)Math.random() * 0.1f;
            float[] rgb = hsvToRgb(hue, 0.7f, 1f);
            int r = (int)(rgb[0] * 255);
            int g = (int)(rgb[1] * 255);
            int b = (int)(rgb[2] * 255);
            fireflies.add(new Firefly(pos, vel, phase, size, r, g, b));
            if (fireflies.size() > 40) fireflies.remove(0);
        } catch (Throwable ignored) {}
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            if (ctx == null) return;
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;

            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float t = level.getGameTime() + partial;
            float dayTime = ((level.getGameTime() % 24000L) / 24000.0f);

            boolean isNight = dayTime > 0.7f || dayTime < 0.25f;
            boolean isEvening = dayTime > 0.6f && dayTime < 0.8f;

            // Fireflies - only at night/evening
            if ((isNight || isEvening) && !fireflies.isEmpty()) {
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(FIREFLY_TEX),
                    (pose, consumer) -> {
                        for (Firefly f : fireflies) {
                            float blink = Mth.sin(t * 0.15f + f.phase * 2f) * 0.5f + 0.5f;
                            // Fireflies blink - emit light
                            float glowPulse = Mth.sin(t * 0.08f + f.phase) * 0.3f + 0.7f;
                            int alpha = (int)(blink * 220 * glowPulse);
                            if (alpha < 10) continue;
                            float size = f.size * (0.8f + blink * 0.4f);
                            // Core bright
                            quadBillboard(pose, consumer, f.pos.x, f.pos.y, f.pos.z, size, 255, 255, 200, alpha, cam);
                            // Glow halo
                            quadBillboard(pose, consumer, f.pos.x, f.pos.y, f.pos.z, size * 2.2f, f.r, f.g, f.b, alpha / 3, cam);
                            // Light trail when moving
                            if (f.vel.length() > 0.05) {
                                Vec3 trail = f.pos.subtract(f.vel.normalize().scale(0.4));
                                quadBillboard(pose, consumer, trail.x, trail.y, trail.z, size * 0.6f, f.r, f.g, f.b, alpha / 2, cam);
                            }
                        }
                    });
            }

            // Bioluminescent water glow - underwater creatures that look cool, mirrors due to extreme reflections
            // Only when near water and at night
            if (isNight && mc.player != null && mc.player.isInWater()) {
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(BIOLUM_TEX),
                    (pose, consumer) -> {
                        int creatureCount = 12;
                        for (int i = 0; i < creatureCount; i++) {
                            float ang = (float)i / creatureCount * Mth.TWO_PI + t * 0.01f + i * 0.5f;
                            double dist = 4 + Mth.sin(t * 0.02f + i) * 2;
                            double x = cam.x + Math.cos(ang) * dist;
                            double z = cam.z + Math.sin(ang) * dist;
                            double y = cam.y + Mth.sin(t * 0.03f + i * 0.8f) * 1.5 - 1.0;
                            // Swim up to land behavior - occasionally rise
                            if (Mth.sin(t * 0.005f + i) > 0.85) {
                                y += (t % 100) * 0.02; // rising
                            }
                            float size = 0.4f + Mth.sin(t * 0.04f + i) * 0.15f;
                            // Bioluminescent colors - cyan, blue, purple
                            float hue = 0.55f + Mth.sin(t * 0.01f + i * 0.3f) * 0.15f;
                            float[] rgb = hsvToRgb(hue, 0.8f, 1f);
                            int r = (int)(rgb[0] * 255);
                            int g = (int)(rgb[1] * 255);
                            int b = (int)(rgb[2] * 255);
                            int alpha = 120 + (int)(Mth.sin(t * 0.08f + i) * 40);
                            quadBillboard(pose, consumer, x, y, z, size, r, g, b, alpha, cam);
                            // Glow trail
                            quadBillboard(pose, consumer, x, y, z, size * 2f, r, g, b, alpha / 4, cam);
                        }
                    });
            }

            // Water reflections - extreme reflections, mirrors exist
            if (mc.player != null && Math.abs(cam.y - 62) < 40) {
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(WHITE),
                    (pose, consumer) -> {
                        // Mirror-like reflection plane slightly below water
                        double waterY = 62.0;
                        double reflectY = waterY + 0.05;
                        // Large quad for reflection
                        double size = 80.0;
                        double x0 = cam.x - size / 2;
                        double x1 = cam.x + size / 2;
                        double z0 = cam.z - size / 2;
                        double z1 = cam.z + size / 2;
                        // Reflection color - slightly darker, with wave distortion
                        float wave = Mth.sin(t * 0.03f) * 0.1f;
                        int r = 80 + (int)(wave * 20);
                        int g = 120 + (int)(wave * 15);
                        int b = 180 + (int)(wave * 10);
                        int alpha = 35;
                        quad(pose, consumer,
                            x0, reflectY, z0, 0, 0,
                            x1, reflectY, z0, 1, 0,
                            x1, reflectY, z1, 1, 1,
                            x0, reflectY, z1, 0, 1,
                            r, g, b, alpha, 0, 1, 0);
                    });
            }

        } catch (Throwable ignored) {}
    }

    public static float getLastWolfHowl() { return lastWolfHowl; }
    public static float getLastCricket() { return lastCricket; }

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
        vertex(pose, consumer, new Vec3(x0, y0, z0), u0, v0, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, new Vec3(x1, y1, z1), u1, v1, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, new Vec3(x2, y2, z2), u2, v2, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, new Vec3(x3, y3, z3), u3, v3, r, g, b, a, nx, ny, nz);
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
