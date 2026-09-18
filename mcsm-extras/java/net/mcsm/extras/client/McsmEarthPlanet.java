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
 * V2 NEXT-GEN - Earth-like planet with animated clouds that spins across world
 * Real 3D planet, not flat texture. Spins, has cloud layer drifting, atmosphere glow.
 * Visible in sky, moves slowly, shows continents/oceans + animated cloud cover.
 * Sodium-safe
 */
public final class McsmEarthPlanet {

    private static final Identifier EARTH_TEXTURE = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/earth_planet.png");
    private static final Identifier EARTH_CLOUDS = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/earth_clouds.png");
    private static final Identifier EARTH_NIGHT = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/earth_night_lights.png");
    private static final Identifier EARTH_ATMOS = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/earth_atmosphere.png");

    private static float earthSpin = 0f;
    private static float cloudSpin = 0f;

    private McsmEarthPlanet() {}

    public static void submit(LevelRenderContext ctx) {
        try {
            if (ctx == null) return;
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;
            String dim = level.dimension().identifier().toString();
            if (dim.contains("end")) return; // End has its own planets

            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            long gameTime = level.getGameTime();
            float t = gameTime + partial;

            earthSpin += 0.008f;
            cloudSpin += 0.012f;

            // Earth position - orbits slowly across sky, opposite sun
            float dayTime = ((level.getGameTime() % 24000L) / 24000.0f);
            float earthOrbit = (dayTime * 0.3f + t * 0.0002f) % Mth.TWO_PI;
            double earthDist = 420.0;
            double earthX = cam.x + Mth.cos(earthOrbit) * earthDist * 0.7;
            double earthZ = cam.z + Mth.sin(earthOrbit) * earthDist * 0.7;
            double earthY = cam.y + 220.0 + Mth.sin(earthOrbit * 0.7f) * 40.0;
            Vec3 earthPos = new Vec3(earthX, earthY, earthZ);

            // Only visible at certain times (evening/night/morning) for realism
            boolean visible = dayTime > 0.45f || dayTime < 0.3f;
            if (!visible) return;

            double earthRadius = 18.0;
            double cloudRadius = earthRadius * 1.02;
            double atmosRadius = earthRadius * 1.18;

            // Earth core sphere - spinning continents
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(EARTH_TEXTURE),
                (pose, consumer) -> {
                    emitSpinningSphere(pose, consumer, earthPos, earthRadius, 22, 16, earthSpin, 0.4f, 120, 180, 255, 220, t, false);
                });

            // Night lights layer (city lights on dark side)
            float nightFactor = Mth.clamp((dayTime > 0.5f ? 1f : 0.6f), 0f, 1f);
            if (nightFactor > 0.3f) {
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(EARTH_NIGHT),
                    (pose, consumer) -> {
                        int alpha = (int)(nightFactor * 110);
                        emitSpinningSphere(pose, consumer, earthPos, earthRadius * 1.001, 20, 14, earthSpin, 0.4f, 255, 230, 150, alpha, t, true);
                    });
            }

            // Animated clouds layer - spins slightly faster, drifts
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(EARTH_CLOUDS),
                (pose, consumer) -> {
                    emitSpinningSphere(pose, consumer, earthPos, cloudRadius, 20, 14, cloudSpin, 0.4f, 255, 255, 255, 160, t, false);
                });

            // Atmosphere glow - bluish halo
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(EARTH_ATMOS),
                (pose, consumer) -> {
                    int alpha = 35 + (int)(Mth.sin(t * 0.02f) * 8);
                    emitSphere(pose, consumer, earthPos, atmosRadius, 16, 10, 100, 160, 255, alpha, t);
                });

        } catch (Throwable ignored) {}
    }

    private static void emitSpinningSphere(Pose pose, VertexConsumer consumer, Vec3 center, double radius, int hSegs, int vSegs, float rotY, float tilt, int r, int g, int b, int a, float time, boolean emissive) {
        float cosTilt = Mth.cos(tilt);
        float sinTilt = Mth.sin(tilt);
        for (int v = 0; v < vSegs; v++) {
            double phi0 = Math.PI * v / vSegs;
            double phi1 = Math.PI * (v + 1) / vSegs;
            double y0 = Math.cos(phi0);
            double y1 = Math.cos(phi1);
            double r0 = Math.sin(phi0);
            double r1 = Math.sin(phi1);
            for (int h = 0; h < hSegs; h++) {
                double theta0 = 2.0 * Math.PI * h / hSegs + rotY;
                double theta1 = 2.0 * Math.PI * (h + 1) / hSegs + rotY;

                double x00 = Math.cos(theta0) * r0;
                double z00 = Math.sin(theta0) * r0;
                double x10 = Math.cos(theta1) * r0;
                double z10 = Math.sin(theta1) * r0;
                double x01 = Math.cos(theta0) * r1;
                double z01 = Math.sin(theta0) * r1;
                double x11 = Math.cos(theta1) * r1;
                double z11 = Math.sin(theta1) * r1;

                // Tilt
                double yy00 = y0 * cosTilt - z00 * sinTilt;
                double zz00 = y0 * sinTilt + z00 * cosTilt;
                double yy10 = y0 * cosTilt - z10 * sinTilt;
                double zz10 = y0 * sinTilt + z10 * cosTilt;
                double yy01 = y1 * cosTilt - z01 * sinTilt;
                double zz01 = y1 * sinTilt + z01 * cosTilt;
                double yy11 = y1 * cosTilt - z11 * sinTilt;
                double zz11 = y1 * sinTilt + z11 * cosTilt;

                double px00 = center.x + x00 * radius;
                double py00 = center.y + yy00 * radius;
                double pz00 = center.z + zz00 * radius;
                double px10 = center.x + x10 * radius;
                double py10 = center.y + yy10 * radius;
                double pz10 = center.z + zz10 * radius;
                double px11 = center.x + x11 * radius;
                double py11 = center.y + yy11 * radius;
                double pz11 = center.z + zz11 * radius;
                double px01 = center.x + x01 * radius;
                double py01 = center.y + yy01 * radius;
                double pz01 = center.z + zz01 * radius;

                float u0 = (float)h / hSegs;
                float u1 = (float)(h + 1) / hSegs;
                float v0 = (float)v / vSegs;
                float v1 = (float)(v + 1) / vSegs;

                // Simple day/night shading based on sun direction (approx)
                float shade = 0.6f + 0.4f * Mth.cos((float)theta0);
                int rr = (int)(r * shade);
                int gg = (int)(g * shade);
                int bb = (int)(b * shade);
                if (emissive) {
                    rr = r; gg = g; bb = b;
                }

                quad(pose, consumer,
                    px00, py00, pz00, u0, v0,
                    px10, py10, pz10, u1, v0,
                    px11, py11, pz11, u1, v1,
                    px01, py01, pz01, u0, v1,
                    rr, gg, bb, a, (float)x00, (float)yy00, (float)zz00);
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
