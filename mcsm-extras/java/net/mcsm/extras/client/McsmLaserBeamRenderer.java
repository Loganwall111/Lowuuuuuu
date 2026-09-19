package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * V2 Phase 5 - Laserbeam shooting stars client VFX
 * Renders laserbeam across sky with chromatic blur blue/green/black shifting
 */
public final class McsmLaserBeamRenderer {

    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");

    private static Vec3 beamStart = Vec3.ZERO;
    private static Vec3 beamEnd = Vec3.ZERO;
    private static float beamProgress = 0f;
    private static boolean active = false;
    private static long lastTrigger = 0;

    private McsmLaserBeamRenderer() {}

    public static void submit(LevelRenderContext ctx) {
        try {
            if (ctx == null) return;
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;
            float dayTime = ((level.getGameTime() % 24000L) / 24000.0f);
            if (dayTime > 0.25f && dayTime < 0.75f) return; // Night only

            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            if (poseStack == null || collector == null) return;

            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float t = level.getGameTime() + partial;

            if (!active) {
                // Rare trigger - laserbeam shooting star
                if (level.getGameTime() - lastTrigger > 6000 && Math.random() < 0.008) {
                    double sx = cam.x + (Math.random() * 2 - 1) * 400;
                    double sz = cam.z + (Math.random() * 2 - 1) * 400;
                    double sy = cam.y + 150 + Math.random() * 100;
                    double ex = sx + (Math.random() * 2 - 1) * 300;
                    double ez = sz + (Math.random() * 2 - 1) * 300;
                    double ey = cam.y + 20 + Math.random() * 50;
                    beamStart = new Vec3(sx, sy, sz);
                    beamEnd = new Vec3(ex, ey, ez);
                    beamProgress = 0f;
                    active = true;
                    lastTrigger = level.getGameTime();
                }
                if (!active) return;
            }

            beamProgress += partial * 0.02f;
            if (beamProgress > 1.3f) {
                active = false;
                beamProgress = 0f;
                return;
            }

            final Vec3 finalStart = beamStart;
            final Vec3 finalEnd = beamEnd;
            final float finalProgress = beamProgress;
            final float finalT = t;

            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                (pose, consumer) -> {
                    Vec3 dir = finalEnd.subtract(finalStart);
                    double len = dir.length();
                    if (len < 1) return;
                    Vec3 norm = dir.normalize();
                    double traveled = len * Mth.clamp(finalProgress, 0f, 1f);
                    Vec3 currentEnd = finalStart.add(norm.scale(traveled));

                    int segments = 18;
                    for (int i = 0; i < segments; i++) {
                        float segProg = (float)i / segments;
                        if (segProg > finalProgress) break;

                        double x = Mth.lerp(segProg, finalStart.x, currentEnd.x);
                        double y = Mth.lerp(segProg, finalStart.y, currentEnd.y);
                        double z = Mth.lerp(segProg, finalStart.z, currentEnd.z);

                        float hueShift = (finalT * 0.02f + segProg * 3f) % 1f;
                        int r, g, b;
                        if (hueShift < 0.33f) {
                            r = 40; g = 120; b = 255;
                        } else if (hueShift < 0.66f) {
                            r = 40; g = 255; b = 120;
                        } else {
                            r = 20; g = 20; b = 20;
                        }

                        float size = 1.5f + Mth.sin(segProg * 10f + finalT * 0.05f) * 0.5f;
                        int alpha = (int)(200 * (1f - segProg * 0.5f));

                        quadBillboard(pose, consumer, x, y, z, size, r, g, b, alpha, cam);

                        if (i % 2 == 0) {
                            quadBillboard(pose, consumer, x, y, z, size * 2.2f, r, g, b, alpha / 3, cam);
                        }
                    }

                    if (finalProgress > 0.8f) {
                        float flashProg = (finalProgress - 0.8f) / 0.5f;
                        float flashAlpha = 1f - flashProg;
                        int alpha = (int)(flashAlpha * 255);
                        double ix = currentEnd.x;
                        double iy = currentEnd.y;
                        double iz = currentEnd.z;
                        float flashSize = 5f + flashProg * 15f;
                        quadBillboard(pose, consumer, ix, iy, iz, flashSize, 255, 255, 255, alpha, cam);
                        quadBillboard(pose, consumer, ix, iy, iz, flashSize * 1.5f, 100, 180, 255, alpha / 2, cam);
                    }
                });

        } catch (Throwable ignored) {}
    }

    private static void quadBillboard(PoseStack.Pose pose, VertexConsumer consumer, double x, double y, double z, float size, int r, int g, int b, int a, Vec3 cam) {
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

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer,
            double x, double y, double z, float u, float v,
            int r, int g, int b, int a, float nx, float ny, float nz) {
        consumer.addVertex(pose, (float) x, (float) y, (float) z)
            .setColor(r, g, b, a)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(15728880)
            .setNormal(pose, nx, ny, nz);
    }
}
