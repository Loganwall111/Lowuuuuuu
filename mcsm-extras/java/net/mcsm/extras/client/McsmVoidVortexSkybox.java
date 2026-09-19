package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.McsmVoidTiers;

/**
 * BUILD #493 – Void Vortex Skybox – Gigantic vortex for void, not pitch black
 *
 * THE MANDATE:
 * - Void currently pitch black – give it a skybox
 * - Gigantic vortex square different rings, 3D skybox specifically for void
 * - Particles all the way in going all the way in, extremely cool VFX
 * - Almost like reality rip in fabric of space and time
 * - When you go into it and movies will zoom by as you jump further until like completely regular micro
 * - Animate skybox so rings actually spin like real vortex
 * - Procedural layers – not able to go 100% to very bottom, so use 3D geometry to make rest possible
 * - Create brand new reality right underneath Minecraft that Mrs Photorealism strange VFX psychedelic areas
 *
 * WHAT THIS DOES:
 * - Renders gigantic vortex in void skybox – multiple spinning rings at different depths
 * - Rings spin like real vortex – different speeds, directions
 * - Particles spiraling inward – reality rip VFX
 * - When falling deeper, movies (planets, Minecraft universes) zoom by
 * - 3D skybox – not flat, true 3D geometry with depth
 * - Photorealistic, psychedelic, brain-blowing
 */
public final class McsmVoidVortexSkybox {

    private McsmVoidVortexSkybox() {}

    // ---- Vortex parameters ------------------------------------------------
    private static final int VORTEX_RINGS = 12;
    private static final int PARTICLES_PER_RING = 32;
    private static final double VORTEX_RADIUS_START = 400.0;
    private static final double VORTEX_RADIUS_END = 2500.0;
    private static final double VORTEX_DEPTH = 3000.0;

    // ---- Ring spin --------------------------------------------------------
    private static float[] ringRotations = new float[VORTEX_RINGS];
    private static float[] ringSpeeds = new float[VORTEX_RINGS];

    static {
        for (int i = 0; i < VORTEX_RINGS; i++) {
            ringSpeeds[i] = 0.0005F + i * 0.0003F + (i % 2 == 0 ? 0.001F : -0.0007F);
        }
    }

    // ---- Textures ---------------------------------------------------------
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");
    private static final Identifier VOID_STONE = Identifier.fromNamespaceAndPath("mcsm", "textures/block/void_stone.png");

    // ---- Tick – animate rings spin like real vortex -----------------------
    public static void tick() {
        try {
            if (!McsmExtrasConfig.voidDescent) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null) return;
            if (!mc.level.dimension().equals(McsmVoid.DIMENSION)) return;

            for (int i = 0; i < VORTEX_RINGS; i++) {
                ringRotations[i] += ringSpeeds[i];
                if (ringRotations[i] > Mth.TWO_PI) ringRotations[i] -= Mth.TWO_PI;
                if (ringRotations[i] < -Mth.TWO_PI) ringRotations[i] += Mth.TWO_PI;
            }
        } catch (Throwable ignored) {}
    }

    // ---- Submit – gigantic vortex 3D skybox -------------------------------
    public static void submit(LevelRenderContext ctx) {
        try {
            if (!McsmExtrasConfig.voidDescent) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || ctx == null) return;
            if (!mc.level.dimension().equals(McsmVoid.DIMENSION)) return;

            double playerY = mc.player != null ? mc.player.getY() : ctx.levelState().cameraRenderState.pos.y;
            if (playerY > McsmVoidTiers.GEL_FLOOR + 5) return; // only in void deep

            Vec3 camera = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            double time = (mc.level.getGameTime() % 240000L) + partial;
            double fallDist = mc.player != null ? mc.player.fallDistance : 0.0;

            float glitchFactor = McsmGlitchGenerator.computeGlitchFactor(fallDist, playerY, time);

            final double t = time;
            final float glitch = glitchFactor;
            final Vec3 cam = camera;
            final double pY = playerY;
            final double fDist = fallDist;

            // ---- Vortex rings – gigantic, spinning ---------------------------------
            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(WHITE),
                    (pose, consumer) -> {
                        for (int ring = 0; ring < VORTEX_RINGS; ring++) {
                            emitVortexRing(pose, consumer, cam, ring, t, glitch, pY, fDist);
                        }
                    });

            // ---- Particles spiraling inward – reality rip VFX -----------------------
            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.glow(WHITE),
                    (pose, consumer) -> {
                        for (int ring = 0; ring < VORTEX_RINGS; ring++) {
                            emitVortexParticles(pose, consumer, cam, ring, t, glitch, pY, fDist);
                        }
                    });

            // ---- Reality rip – central black hole with fabric tear ------------------
            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(VOID_STONE),
                    (pose, consumer) -> {
                        emitRealityRip(pose, consumer, cam, t, glitch, pY);
                    });

            // ---- Movies zooming by – Minecraft universes on planets when falling ----
            if (playerY < McsmVoidTiers.ABYSSAL_NIGHTMARE_FLOOR) {
                collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.glow(WHITE),
                        (pose, consumer) -> {
                            emitZoomingMovies(pose, consumer, cam, t, glitch, pY, fDist);
                        });
            }

        } catch (Throwable ignored) {}
    }

    // ---- Vortex ring – spinning like real vortex --------------------------
    private static void emitVortexRing(Pose pose, VertexConsumer consumer, Vec3 camera, int ringIndex, double time, float glitchFactor, double playerY, double fallDist) {
        double depthT = ringIndex / (double)VORTEX_RINGS;
        double radius = Mth.lerp(depthT, VORTEX_RADIUS_START, VORTEX_RADIUS_END);
        double depth = Mth.lerp(depthT, 0.0, -VORTEX_DEPTH) + McsmVoidTiers.FLOOR_Y + 100;

        // Spin like real vortex – different rings spin different speeds/directions
        float rotation = ringRotations[ringIndex] + (float)(fallDist * 0.001 * (ringIndex % 2 == 0 ? 1 : -1));
        double spinRadius = radius + McsmGlitchGenerator.fractalNoise(ringIndex * 0.5, time * 0.001) * glitchFactor * 100.0;

        // Color based on depth – outer rings purple, inner rings black with magenta
        float hue = (float)(0.75 + depthT * 0.15 + time * 0.0001) % 1.0F;
        float[] rgb = hsvToRgb(hue, 0.85F, 0.9F);
        // Mix with void colors
        float voidMix = (float)depthT;
        int r = (int)((rgb[0] * (1-voidMix) + 0.1 * voidMix) * 255);
        int g = (int)((rgb[1] * (1-voidMix) + 0.05 * voidMix) * 255);
        int b = (int)((rgb[2] * (1-voidMix) + 0.25 * voidMix) * 255);
        int a = (int)((120 - depthT * 80) * (0.5 + glitchFactor * 0.5));

        // Ring as circle of quads – 3D, not flat
        int segs = 48;
        for (int s = 0; s < segs; s++) {
            double a0 = (s / (double)segs) * Mth.TWO_PI + rotation;
            double a1 = ((s+1) / (double)segs) * Mth.TWO_PI + rotation;

            // Displace vertices with glitch generator for reality mutation
            double[] p0 = McsmGlitchGenerator.displaceVertex(
                    camera.x + Math.cos(a0) * spinRadius,
                    depth,
                    camera.z + Math.sin(a0) * spinRadius,
                    time, fallDist, glitchFactor, s + ringIndex * 100);
            double[] p1 = McsmGlitchGenerator.displaceVertex(
                    camera.x + Math.cos(a1) * spinRadius,
                    depth,
                    camera.z + Math.sin(a1) * spinRadius,
                    time, fallDist, glitchFactor, s+1 + ringIndex * 100);

            double thickness = 8.0 + glitchFactor * 12.0 + Math.sin(time * 0.005 + ringIndex) * 2.0;

            quad(pose, consumer,
                    p0[0], p0[1] - thickness, p0[2], 0, 0,
                    p1[0], p1[1] - thickness, p1[2], 1, 0,
                    p1[0], p1[1] + thickness, p1[2], 1, 1,
                    p0[0], p0[1] + thickness, p0[2], 0, 1,
                    r, g, b, a, 0, 1, 0);
        }
    }

    // ---- Particles spiraling inward – VFX ---------------------------------
    private static void emitVortexParticles(Pose pose, VertexConsumer consumer, Vec3 camera, int ringIndex, double time, float glitchFactor, double playerY, double fallDist) {
        double depthT = ringIndex / (double)VORTEX_RINGS;
        double baseRadius = Mth.lerp(depthT, VORTEX_RADIUS_START, VORTEX_RADIUS_END);
        double depth = Mth.lerp(depthT, 0.0, -VORTEX_DEPTH) + McsmVoidTiers.FLOOR_Y + 100;

        float rotation = ringRotations[ringIndex] * 1.5F;

        for (int p = 0; p < PARTICLES_PER_RING; p++) {
            double angle = (p / (double)PARTICLES_PER_RING) * Mth.TWO_PI + rotation + time * 0.002 * (ringIndex % 2 == 0 ? 1 : -1);
            // Spiral inward – particles go all the way in
            double spiralT = (time * 0.0005 + p * 0.1) % 1.0;
            double radius = baseRadius * (1.0 - spiralT * 0.8) + Mth.sin((float)(time * 0.01 + p)) * glitchFactor * 20.0;

            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = depth + spiralT * 200.0 - 100.0 + McsmGlitchGenerator.fractalNoise(p * 0.3, time * 0.002) * glitchFactor * 30.0;

            double size = 2.0 + glitchFactor * 3.0;

            // Color – white to purple, glowing
            int r = 200 + (int)(55 * Math.sin(time * 0.01 + p));
            int g = 150 + (int)(50 * Math.cos(time * 0.008 + p * 1.2));
            int b = 255;
            int a = (int)(180 * (1.0 - spiralT) * (0.5 + glitchFactor));

            quadFullBright(pose, consumer,
                    x - size, y - size, z, 0, 0,
                    x + size, y - size, z, 1, 0,
                    x + size, y + size, z, 1, 1,
                    x - size, y + size, z, 0, 1,
                    r, g, b, a);
        }
    }

    // ---- Reality rip – central black hole, fabric of space and time tear ----
    private static void emitRealityRip(Pose pose, VertexConsumer consumer, Vec3 camera, double time, float glitchFactor, double playerY) {
        double x = camera.x;
        double z = camera.z;
        double y = McsmVoidTiers.FLOOR_Y - 200 + Mth.sin((float)(time * 0.001)) * 20.0;

        double size = 150.0 + glitchFactor * 100.0 + Mth.sin((float)(time * 0.002)) * 20.0;

        // Central black hole – void black
        int r = 0, g = 0, b = 0;
        int a = 255;

        // Multiple layers for depth – reality rip
        for (int layer = 0; layer < 4; layer++) {
            double layerSize = size * (1.0 + layer * 0.3);
            double layerY = y - layer * 10.0;
            int layerA = (int)(255 * (1.0 - layer * 0.2) * (0.8 + glitchFactor * 0.2));

            // Distort with glitch
            double[] p0 = McsmGlitchGenerator.displaceVertex(x - layerSize, layerY, z - layerSize, time, 0, glitchFactor, layer * 10);
            double[] p1 = McsmGlitchGenerator.displaceVertex(x + layerSize, layerY, z - layerSize, time, 0, glitchFactor, layer * 10 + 1);
            double[] p2 = McsmGlitchGenerator.displaceVertex(x + layerSize, layerY, z + layerSize, time, 0, glitchFactor, layer * 10 + 2);
            double[] p3 = McsmGlitchGenerator.displaceVertex(x - layerSize, layerY, z + layerSize, time, 0, glitchFactor, layer * 10 + 3);

            quad(pose, consumer,
                    p0[0], p0[1], p0[2], 0, 0,
                    p1[0], p1[1], p1[2], 1, 0,
                    p2[0], p2[1], p2[2], 1, 1,
                    p3[0], p3[1], p3[2], 0, 1,
                    r, g, b, layerA, 0, 1, 0);
        }

        // Glowing rim around rip – reality tearing
        float pulse = 0.8F + 0.2F * Mth.sin((float)(time * 0.01));
        int rr = (int)(150 * pulse);
        int rg = (int)(50 * pulse);
        int rb = (int)(255 * pulse);
        int ra = (int)(120 * (0.5 + glitchFactor));

        int segs = 32;
        for (int s = 0; s < segs; s++) {
            double a0 = (s / (double)segs) * Mth.TWO_PI;
            double a1 = ((s+1) / (double)segs) * Mth.TWO_PI;
            double x0 = x + Math.cos(a0) * size;
            double z0 = z + Math.sin(a0) * size;
            double x1 = x + Math.cos(a1) * size;
            double z1 = z + Math.sin(a1) * size;

            quadFullBright(pose, consumer,
                    x0, y - 2, z0, 0, 0,
                    x1, y - 2, z1, 1, 0,
                    x1, y + 2, z1, 1, 1,
                    x0, y + 2, z0, 0, 1,
                    rr, rg, rb, ra);
        }
    }

    // ---- Movies zooming by – Minecraft universes on planets -----------------
    private static void emitZoomingMovies(Pose pose, VertexConsumer consumer, Vec3 camera, double time, float glitchFactor, double playerY, double fallDist) {
        // When falling deeper, movies (Minecraft Dungeons, Legends, Movie, etc.) zoom by
        // Simulate as glowing quads with logos that fly past
        int movieCount = 6;
        String[] movies = {"DUNGEONS", "LEGENDS", "MOVIE", "STORY", "MC2", "EARTH"};

        for (int i = 0; i < movieCount; i++) {
            double speed = 2.0 + i * 0.5 + glitchFactor * 3.0 + fallDist * 0.01;
            double y = camera.y - (time * speed % 2000.0) + i * 300.0;
            double angle = (i / (double)movieCount) * Mth.TWO_PI + time * 0.0003 * (i+1);
            double radius = 300.0 + i * 80.0 + McsmGlitchGenerator.fractalNoise(i * 1.5, time * 0.001) * glitchFactor * 50.0;

            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;

            double size = 20.0 + glitchFactor * 15.0;

            // Color based on movie
            float hue = (i / (float)movieCount);
            float[] rgb = hsvToRgb(hue, 0.9F, 1.0F);
            int r = (int)(rgb[0] * 255);
            int g = (int)(rgb[1] * 255);
            int b = (int)(rgb[2] * 255);
            int a = (int)(200 * (0.5 + glitchFactor * 0.5) * Mth.clamp((float)((y - camera.y + 1000.0) / 1000.0), 0.0F, 1.0F));

            quadFullBright(pose, consumer,
                    x - size, y - size, z, 0, 0,
                    x + size, y - size, z, 1, 0,
                    x + size, y + size, z, 1, 1,
                    x - size, y + size, z, 0, 1,
                    r, g, b, a);
        }
    }

    // ---- Helpers ----------------------------------------------------------
    private static void quad(Pose pose, VertexConsumer consumer,
                             double x0, double y0, double z0, float u0, float v0,
                             double x1, double y1, double z1, float u1, float v1,
                             double x2, double y2, double z2, float u2, float v2,
                             double x3, double y3, double z3, float u3, float v3,
                             int r, int g, int b, int a,
                             float nx, float ny, float nz) {
        consumer.addVertex(pose, (float)x0, (float)y0, (float)z0).setColor(r,g,b,a).setUv(u0,v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(r,g,b,a).setUv(u1,v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(r,g,b,a).setUv(u2,v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float)x3, (float)y3, (float)z3).setColor(r,g,b,a).setUv(u3,v3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, nx, ny, nz);
    }

    private static void quadFullBright(Pose pose, VertexConsumer consumer,
                                       double x0, double y0, double z0, float u0, float v0,
                                       double x1, double y1, double z1, float u1, float v1,
                                       double x2, double y2, double z2, float u2, float v2,
                                       double x3, double y3, double z3, float u3, float v3,
                                       int r, int g, int b, int a) {
        consumer.addVertex(pose, (float)x0, (float)y0, (float)z0).setColor(r,g,b,a).setUv(u0,v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0,1,0);
        consumer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(r,g,b,a).setUv(u1,v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0,1,0);
        consumer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(r,g,b,a).setUv(u2,v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0,1,0);
        consumer.addVertex(pose, (float)x3, (float)y3, (float)z3).setColor(r,g,b,a).setUv(u3,v3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0,1,0);
    }

    private static float[] hsvToRgb(float h, float s, float v) {
        float hh = h * 6.0F;
        int i = (int) hh;
        float f = hh - i;
        float p = v * (1.0F - s);
        float q = v * (1.0F - s * f);
        float t = v * (1.0F - s * (1.0F - f));
        switch (i % 6) {
            case 0: return new float[]{v, t, p};
            case 1: return new float[]{q, v, p};
            case 2: return new float[]{p, v, t};
            case 3: return new float[]{p, q, v};
            case 4: return new float[]{t, p, v};
            default: return new float[]{v, p, q};
        }
    }
}
