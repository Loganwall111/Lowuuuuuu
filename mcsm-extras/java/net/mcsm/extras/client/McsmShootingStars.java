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
import java.util.Iterator;
import java.util.List;

/**
 * V2 NEXT-GEN - Shooting stars, twinkling stars, full bright distant stars
 * Real shooting stars streak across sky, twinkling stars pulse, bright stars glow.
 * Sodium-safe
 */
public final class McsmShootingStars {

    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");
    private static final Identifier STAR_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/twinkling_star.png");

    private static class ShootingStar {
        Vec3 start, end;
        float progress;
        float speed;
        int r, g, b;
        float size;
        boolean active;

        ShootingStar(Vec3 s, Vec3 e, float sp, int r, int g, int b, float sz) {
            start = s; end = e; progress = 0f; speed = sp; this.r = r; this.g = g; this.b = b; size = sz; active = true;
        }
    }

    private static final List<ShootingStar> stars = new ArrayList<>();
    private static float twinkleTime = 0f;
    private static int tickCounter = 0;

    private McsmShootingStars() {}

    public static void tick() {
        twinkleTime += 0.05f;
        tickCounter++;
        // Random shooting star spawn
        if (tickCounter % 80 == 0 && Math.random() < 0.35) {
            spawnShootingStar();
        }
        // Update
        Iterator<ShootingStar> it = stars.iterator();
        while (it.hasNext()) {
            ShootingStar s = it.next();
            s.progress += s.speed;
            if (s.progress >= 1f) it.remove();
        }
    }

    private static void spawnShootingStar() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Vec3 cam = mc.player != null ? mc.player.position() : new Vec3(0,0,0);
            double dist = 360.0;
            double angle1 = Math.random() * Mth.TWO_PI;
            double angle2 = Math.random() * 0.6 + 0.3; // elevation
            double sx = cam.x + Math.cos(angle1) * dist * Math.cos(angle2);
            double sy = cam.y + 260 + Math.sin(angle2) * 80 + Math.random() * 40;
            double sz = cam.z + Math.sin(angle1) * dist * Math.cos(angle2);
            Vec3 start = new Vec3(sx, sy, sz);
            // End - streak direction
            double len = 70 + Math.random() * 50;
            double dirX = (Math.random() - 0.5) * 0.6;
            double dirZ = (Math.random() - 0.5) * 0.6;
            double dirY = -0.3 - Math.random() * 0.4;
            Vec3 end = start.add(dirX * len, dirY * len, dirZ * len);
            float speed = 0.04f + (float)Math.random() * 0.04f;
            // Color - white, blue, yellow, pink shooting stars
            float hue = (float)Math.random() * 0.3f + (Math.random() < 0.3 ? 0.6f : 0f);
            if (hue > 1f) hue -= 1f;
            float[] rgb = hsvToRgb(hue, 0.3f + (float)Math.random() * 0.4f, 1f);
            int r = (int)(rgb[0] * 255);
            int g = (int)(rgb[1] * 255);
            int b = (int)(rgb[2] * 255);
            float size = 1.2f + (float)Math.random() * 1.0f;
            stars.add(new ShootingStar(start, end, speed, r, g, b, size));
        } catch (Throwable ignored) {}
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            if (ctx == null) return;
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;
            float dayTime = ((level.getGameTime() % 24000L) / 24000.0f).getGameTimeDeltaPartialTick(false));
            if (dayTime > 0.25f && dayTime < 0.75f) return; // Night only

            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float t = level.getGameTime() + partial;

            // Twinkling stars background - many small stars that pulse
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(STAR_TEX),
                (pose, consumer) -> {
                    int starCount = 120;
                    for (int i = 0; i < starCount; i++) {
                        float ang1 = (float)i / starCount * Mth.TWO_PI * 3.7f + i * 0.8f;
                        float ang2 = Mth.sin(i * 1.3f) * 0.6f + 0.5f;
                        double dist = 380.0 + Mth.sin(i * 2.1f) * 20;
                        double sx = cam.x + Math.cos(ang1) * dist * Math.cos(ang2);
                        double sy = cam.y + 200 + Math.sin(ang2) * 180 + Mth.cos(ang1 * 1.5f) * 30;
                        double sz = cam.z + Math.sin(ang1) * dist * Math.cos(ang2);
                        // Twinkle
                        float twinkle = Mth.sin(twinkleTime + i * 0.9f) * 0.4f + 0.6f;
                        float twinkle2 = Mth.sin(twinkleTime * 1.7f + i * 1.2f) * 0.3f + 0.7f;
                        float size = (0.5f + Mth.sin(i * 0.7f) * 0.3f) * twinkle;
                        // Color variation - white, blue, yellow
                        float hue = (i * 0.07f) % 0.25f;
                        if (i % 7 == 0) hue = 0.6f + (i % 3) * 0.05f; // blue stars
                        if (i % 11 == 0) hue = 0.12f; // yellow
                        float[] rgb = hsvToRgb(hue, 0.2f + (i % 5) * 0.05f, 0.9f + twinkle * 0.1f);
                        int r = (int)(rgb[0] * 255 * twinkle2);
                        int g = (int)(rgb[1] * 255 * twinkle2);
                        int b = (int)(rgb[2] * 255 * twinkle2);
                        int alpha = (int)(200 * twinkle);
                        quadBillboard(pose, consumer, sx, sy, sz, size, r, g, b, alpha, cam);
                        // Bright core for full bright distant stars
                        if (i % 8 == 0) {
                            quadBillboard(pose, consumer, sx, sy, sz, size * 0.5f, 255, 255, 255, (int)(alpha * 1.2), cam);
                        }
                    }
                });

            // Shooting stars streaks
            if (!stars.isEmpty()) {
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                    (pose, consumer) -> {
                        for (ShootingStar s : stars) {
                            if (!s.active) continue;
                            float prog = s.progress;
                            // Interpolate position
                            double x = Mth.lerp(prog, s.start.x, s.end.x);
                            double y = Mth.lerp(prog, s.start.y, s.end.y);
                            double z = Mth.lerp(prog, s.start.z, s.end.z);
                            // Trail - previous positions
                            float trailLen = 0.15f;
                            for (int j = 0; j < 6; j++) {
                                float p = prog - j * 0.03f;
                                if (p < 0) continue;
                                double tx = Mth.lerp(p, s.start.x, s.end.x);
                                double ty = Mth.lerp(p, s.start.y, s.end.y);
                                double tz = Mth.lerp(p, s.start.z, s.end.z);
                                float alphaF = 1f - j * 0.16f - prog * 0.2f;
                                if (alphaF <= 0) continue;
                                int alpha = (int)(alphaF * 200);
                                float sz = s.size * (1f - j * 0.12f);
                                // Color fades
                                int r = (int)(s.r * alphaF);
                                int g = (int)(s.g * alphaF);
                                int b = (int)(s.b * alphaF);
                                quadBillboard(pose, consumer, tx, ty, tz, sz, r, g, b, alpha, cam);
                            }
                            // Head - brightest
                            quadBillboard(pose, consumer, x, y, z, s.size * 1.6f, 255, 255, 255, 255, cam);
                        }
                    });
            }

        } catch (Throwable ignored) {}
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
