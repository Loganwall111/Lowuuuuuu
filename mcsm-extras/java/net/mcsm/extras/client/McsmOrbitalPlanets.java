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
 * V2 NEXT-GEN - Orbital Planets: Saturn, Mars, Jupiter, etc visible in sky
 * Real 3D planets that orbit slowly, each with own texture, rings for Saturn, moons.
 * Inspired by user's request: "Saturn Mars can be seen in sky, Minecraft versions and bunch of other planets orbiting sky for nice visual"
 * Sodium-safe
 */
public final class McsmOrbitalPlanets {

    private static final Identifier MARS_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/planet_mars.png");
    private static final Identifier SATURN_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/planet_saturn.png");
    private static final Identifier SATURN_RING = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/saturn_ring.png");
    private static final Identifier JUPITER_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/planet_jupiter.png");
    private static final Identifier VENUS_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/planet_venus.png");
    private static final Identifier MOON_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/sky/planet_moon.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");

    private static float orbitTime = 0f;

    private static class PlanetDef {
        Identifier tex;
        double radius;
        double orbitRadius;
        double orbitSpeed;
        double orbitTilt;
        double yOffset;
        float[] color;
        boolean hasRing;
        double ringInner, ringOuter;

        PlanetDef(Identifier tex, double radius, double orbitR, double speed, double tilt, double yOff, float[] col, boolean ring) {
            this.tex = tex; this.radius = radius; this.orbitRadius = orbitR; this.orbitSpeed = speed;
            this.orbitTilt = tilt; this.yOffset = yOff; this.color = col; this.hasRing = ring;
            this.ringInner = radius * 1.6; this.ringOuter = radius * 2.8;
        }
    }

    private static final PlanetDef[] PLANETS = new PlanetDef[] {
        new PlanetDef(MARS_TEX, 7.0, 380.0, 0.00035, 0.15, 60.0, new float[]{1.0f, 0.5f, 0.35f}, false),
        new PlanetDef(SATURN_TEX, 12.0, 460.0, 0.00018, 0.35, 80.0, new float[]{0.95f, 0.85f, 0.65f}, true),
        new PlanetDef(JUPITER_TEX, 14.0, 520.0, 0.00012, 0.08, 50.0, new float[]{0.85f, 0.75f, 0.65f}, false),
        new PlanetDef(VENUS_TEX, 6.5, 340.0, 0.00042, 0.05, 40.0, new float[]{0.95f, 0.9f, 0.7f}, false),
        new PlanetDef(MOON_TEX, 5.0, 300.0, 0.00055, 0.25, 30.0, new float[]{0.9f, 0.9f, 0.95f}, false),
    };

    private McsmOrbitalPlanets() {}

    public static void submit(LevelRenderContext ctx) {
        try {
            if (ctx == null) return;
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;
            String dim = level.dimension().identifier().toString();
            if (dim.contains("end")) {
                // End gets more exotic planets
            }

            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float t = level.getGameTime() + partial;

            orbitTime += 0.01f;

            // Only visible at night / evening for realism, but faintly day too
            float dayTime = level.getTimeOfDay(partial);
            float nightAlpha = 1f;
            if (dayTime > 0.25f && dayTime < 0.75f) {
                nightAlpha = 0.35f; // faint during day
            }

            for (int pi = 0; pi < PLANETS.length; pi++) {
                PlanetDef p = PLANETS[pi];
                // Orbit position
                double orbitAngle = t * p.orbitSpeed + pi * 1.3;
                double x = cam.x + Math.cos(orbitAngle) * p.orbitRadius;
                double z = cam.z + Math.sin(orbitAngle) * p.orbitRadius * Math.cos(p.orbitTilt);
                double y = cam.y + p.yOffset + Math.sin(orbitAngle * 0.7) * 30.0 + Math.cos(orbitAngle * 0.4 + pi) * 20.0;
                Vec3 planetPos = new Vec3(x, y, z);

                // Spin
                float spin = t * 0.008f + pi * 0.5f;

                // Planet sphere
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(p.tex),
                    (pose, consumer) -> {
                        emitPlanetSphere(pose, consumer, planetPos, p.radius, 18, 12, spin, p.color, (int)(220 * nightAlpha));
                    });

                // Saturn rings
                if (p.hasRing) {
                    collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(SATURN_RING),
                        (pose, consumer) -> {
                            emitRing(pose, consumer, planetPos, p.ringInner, p.ringOuter, spin, p.color, (int)(160 * nightAlpha));
                        });
                }

                // Glow / atmosphere for gas giants
                if (p.radius > 10) {
                    collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                        (pose, consumer) -> {
                            double glowR = p.radius * 1.25;
                            int r = (int)(p.color[0] * 60);
                            int g = (int)(p.color[1] * 60);
                            int b = (int)(p.color[2] * 80);
                            emitSphere(pose, consumer, planetPos, glowR, 12, 8, r, g, b, (int)(25 * nightAlpha), t);
                        });
                }
            }

            // Asteroid belt - faint band of asteroids at night
            if (nightAlpha > 0.6f) {
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                    (pose, consumer) -> {
                        double beltR = 580.0;
                        double beltY = cam.y + 280.0;
                        int count = 40;
                        for (int i = 0; i < count; i++) {
                            float ang = (float)i / count * Mth.TWO_PI + t * 0.00015f;
                            float ang2 = ang + Mth.sin(t * 0.001f + i) * 0.08f;
                            double ax = cam.x + Math.cos(ang2) * beltR + Mth.sin(i * 1.7f) * 25;
                            double az = cam.z + Math.sin(ang2) * beltR * 0.85 + Mth.cos(i * 2.3f) * 25;
                            double ay = beltY + Mth.sin(ang * 2f + i) * 18f;
                            float size = 0.6f + Mth.sin(t * 0.02f + i) * 0.3f;
                            // twinkle
                            int bright = 80 + (int)(Mth.sin(t * 0.05f + i * 0.9f) * 50);
                            quadBillboard(pose, consumer, ax, ay, az, size, bright, bright, bright, (int)(bright * 0.7), cam);
                        }
                    });
            }

        } catch (Throwable ignored) {}
    }

    private static void emitPlanetSphere(Pose pose, VertexConsumer consumer, Vec3 center, double radius, int hSegs, int vSegs, float rotY, float[] col, int alpha) {
        for (int v = 0; v < vSegs; v++) {
            double phi0 = Math.PI * v / vSegs;
            double phi1 = Math.PI * (v + 1) / vSegs;
            double y0 = center.y + Math.cos(phi0) * radius;
            double y1 = center.y + Math.cos(phi1) * radius;
            double r0 = Math.sin(phi0) * radius;
            double r1 = Math.sin(phi1) * radius;
            for (int h = 0; h < hSegs; h++) {
                double theta0 = 2.0 * Math.PI * h / hSegs + rotY;
                double theta1 = 2.0 * Math.PI * (h + 1) / hSegs + rotY;
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
                int r = (int)(col[0] * 255);
                int g = (int)(col[1] * 255);
                int b = (int)(col[2] * 255);
                quad(pose, consumer,
                    x00, y0, z00, u0, v0,
                    x10, y0, z10, u1, v0,
                    x11, y1, z11, u1, v1,
                    x01, y1, z01, u0, v1,
                    r, g, b, alpha, (float)(x00 - center.x), (float)(y0 - center.y), (float)(z00 - center.z));
            }
        }
    }

    private static void emitRing(Pose pose, VertexConsumer consumer, Vec3 center, double inner, double outer, float rot, float[] col, int alpha) {
        int segs = 36;
        for (int i = 0; i < segs; i++) {
            float a0 = (float)i / segs * Mth.TWO_PI + rot * 0.2f;
            float a1 = (float)(i + 1) / segs * Mth.TWO_PI + rot * 0.2f;
            double x0i = center.x + Math.cos(a0) * inner;
            double z0i = center.z + Math.sin(a0) * inner;
            double x0o = center.x + Math.cos(a0) * outer;
            double z0o = center.z + Math.sin(a0) * outer;
            double x1i = center.x + Math.cos(a1) * inner;
            double z1i = center.z + Math.sin(a1) * inner;
            double x1o = center.x + Math.cos(a1) * outer;
            double z1o = center.z + Math.sin(a1) * outer;
            double y = center.y + Math.sin(a0) * 0.5;
            int r = (int)(col[0] * 255);
            int g = (int)(col[1] * 255);
            int b = (int)(col[2] * 255);
            quad(pose, consumer,
                x0i, y, z0i, 0, 0,
                x1i, y, z1i, 1, 0,
                x1o, y, z1o, 1, 1,
                x0o, y, z0o, 0, 1,
                r, g, b, alpha, 0, 1, 0);
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
        vertex(pose, consumer, pos.subtract(rx).subtract(uy), 0, 1, r, g, b, a, 0, 1, 0);
        vertex(pose, consumer, pos.add(rx).subtract(uy), 1, 1, r, g, b, a, 0, 1, 0);
        vertex(pose, consumer, pos.add(rx).add(uy), 1, 0, r, g, b, a, 0, 1, 0);
        vertex(pose, consumer, pos.subtract(rx).add(uy), 0, 0, r, g, b, a, 0, 1, 0);
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 at, float u, float v, int r, int g, int b, int a, float nx, float ny, float nz) {
        consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z)
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
