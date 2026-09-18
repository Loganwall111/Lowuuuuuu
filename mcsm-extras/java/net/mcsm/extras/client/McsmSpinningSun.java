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
 * V2 NEXT-GEN - 3D Spinning Sun that moves across world
 * Real 3D sphere, not vanilla billboard. Moves across sky like real sun, spins on axis,
 * with animated solar flares, corona bloom, and dimension-specific tints.
 * Sodium-safe: uses SubmitNodeCollector / GlowRenderTypes
 */
public final class McsmSpinningSun {

    private static final Identifier SUN_TEXTURE = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/spinning_sun.png");
    private static final Identifier SUN_CORONA = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/sun_corona.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");

    private static float spin = 0f;
    private static float flarePulse = 0f;

    private McsmSpinningSun() {}

    public static void submit(LevelRenderContext ctx) {
        try {
            if (ctx == null) return;
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;
            if (level.dimension().identifier().toString().contains("end")) return; // End has no sun

            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            long gameTime = level.getGameTime();
            float dayTime = ((level.getGameTime() % 24000L) / 24000.0f); // 0-1
            // Sun moves across sky - real arc
            float sunAngle = dayTime * Mth.TWO_PI; // 0 = sunrise, PI = sunset
            // Don't render at night (below horizon)
            if (dayTime > 0.52f && dayTime < 0.95f) return;

            spin += 0.015f;
            flarePulse += 0.03f;

            // Sun position - huge distance, moves across world
            double sunDistance = 380.0;
            double sunHeight = 280.0 + Mth.sin(sunAngle) * 80.0;
            double sunX = cam.x + Mth.cos(sunAngle) * sunDistance;
            double sunZ = cam.z + Mth.sin(sunAngle) * sunDistance * 0.6;
            double sunY = cam.y + sunHeight;
            Vec3 sunPos = new Vec3(sunX, sunY, sunZ);

            float t = gameTime + partial;
            double radius = 22.0 + Mth.sin(flarePulse * 0.7f) * 1.2; // pulsing

            // Core 3D spinning sphere
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(SUN_TEXTURE),
                (pose, consumer) -> {
                    // Spin on axis
                    float spinX = spin * 0.3f;
                    float spinY = spin;
                    emitSpinningSphere(pose, consumer, sunPos, radius, 20, 14, spinX, spinY, 255, 220, 120, 255, t);
                });

            // Corona bloom - larger glow
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(SUN_CORONA),
                (pose, consumer) -> {
                    double coronaR = radius * 1.8 + Mth.sin(flarePulse) * 1.5;
                    int alpha = 60 + (int)(Mth.sin(flarePulse * 1.3f) * 15);
                    // Outer glow
                    emitSphere(pose, consumer, sunPos, coronaR, 16, 10, 255, 240, 180, alpha, t);
                    // Inner bright corona
                    double innerCorona = radius * 1.25;
                    emitSphere(pose, consumer, sunPos, innerCorona, 14, 8, 255, 250, 200, 90, t);
                });

            // Solar flares - random 3D spikes
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                (pose, consumer) -> {
                    int flares = 6;
                    for (int i = 0; i < flares; i++) {
                        float a = (float)i / flares * Mth.TWO_PI + spin * 0.2f + i * 0.7f;
                        float phi = Mth.sin(t * 0.01f + i) * 0.6f;
                        double fx = sunPos.x + Math.cos(a) * Math.cos(phi) * radius * 1.1;
                        double fy = sunPos.y + Math.sin(phi) * radius * 1.1;
                        double fz = sunPos.z + Math.sin(a) * Math.cos(phi) * radius * 1.1;
                        // flare extends outward
                        double flareLen = 4.0 + Mth.sin(flarePulse + i * 1.3f) * 2.0;
                        Vec3 dir = new Vec3(fx - sunPos.x, fy - sunPos.y, fz - sunPos.z).normalize().scale(flareLen);
                        Vec3 tip = new Vec3(fx, fy, fz).add(dir);
                        // small billboard at flare tip
                        float flareSize = 1.2f + Mth.sin(flarePulse * 2f + i) * 0.5f;
                        quadBillboard(pose, consumer, tip.x, tip.y, tip.z, flareSize, 255, 200, 80, 180, cam);
                    }
                });

        } catch (Throwable ignored) {}
    }

    private static void emitSpinningSphere(Pose pose, VertexConsumer consumer, Vec3 center, double radius, int hSegs, int vSegs, float rotX, float rotY, int r, int g, int b, int a, float time) {
        for (int v = 0; v < vSegs; v++) {
            double phi0 = Math.PI * v / vSegs;
            double phi1 = Math.PI * (v + 1) / vSegs;
            for (int h = 0; h < hSegs; h++) {
                double theta0 = 2.0 * Math.PI * h / hSegs + rotY;
                double theta1 = 2.0 * Math.PI * (h + 1) / hSegs + rotY;
                // Apply rotX tilt
                double y0 = Math.cos(phi0);
                double y1 = Math.cos(phi1);
                double r0 = Math.sin(phi0);
                double r1 = Math.sin(phi1);
                // Rotate around X
                double cosRX = Math.cos(rotX);
                double sinRX = Math.sin(rotX);
                double x00 = Math.cos(theta0) * r0;
                double z00 = Math.sin(theta0) * r0;
                double x10 = Math.cos(theta1) * r0;
                double z10 = Math.sin(theta1) * r0;
                double x01 = Math.cos(theta0) * r1;
                double z01 = Math.sin(theta0) * r1;
                double x11 = Math.cos(theta1) * r1;
                double z11 = Math.sin(theta1) * r1;
                // X rotation
                double y00 = y0 * cosRX - z00 * sinRX;
                double zz00 = y0 * sinRX + z00 * cosRX;
                double y10 = y0 * cosRX - z10 * sinRX;
                double zz10 = y0 * sinRX + z10 * cosRX;
                double y01 = y1 * cosRX - z01 * sinRX;
                double zz01 = y1 * sinRX + z01 * cosRX;
                double y11 = y1 * cosRX - z11 * sinRX;
                double zz11 = y1 * sinRX + z11 * cosRX;

                double px00 = center.x + x00 * radius;
                double py00 = center.y + y00 * radius;
                double pz00 = center.z + zz00 * radius;
                double px10 = center.x + x10 * radius;
                double py10 = center.y + y10 * radius;
                double pz10 = center.z + zz10 * radius;
                double px11 = center.x + x11 * radius;
                double py11 = center.y + y11 * radius;
                double pz11 = center.z + zz11 * radius;
                double px01 = center.x + x01 * radius;
                double py01 = center.y + y01 * radius;
                double pz01 = center.z + zz01 * radius;

                float u0 = (float)h / hSegs;
                float u1 = (float)(h + 1) / hSegs;
                float v0 = (float)v / vSegs;
                float v1 = (float)(v + 1) / vSegs;

                // Solar surface variation
                float noise = Mth.sin((float)(theta0 * 3 + time * 0.02)) * 0.1f + Mth.cos((float)(phi0 * 4 + time * 0.015f)) * 0.1f;
                int rr = Mth.clamp((int)(r + noise * 40), 200, 255);
                int gg = Mth.clamp((int)(g + noise * 30), 150, 255);
                int bb = Mth.clamp((int)(b + noise * 20), 80, 255);

                quad(pose, consumer,
                    px00, py00, pz00, u0, v0,
                    px10, py10, pz10, u1, v0,
                    px11, py11, pz11, u1, v1,
                    px01, py01, pz01, u0, v1,
                    rr, gg, bb, a, (float)x00, (float)y00, (float)zz00);
            }
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
}
