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
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.*;

/**
 * V2 EPIC - Volumetric 3D Cloud Meshes / Fog Volumes
 * Real 3D meshes, not 2D bands. Epic volumetric clouds with depth.
 * Each dimension has its own pretty cool sky + wither storm with different colours and gradients.
 */
public final class McsmVolumetricCloudMesh {

    private static final double SPAN = 400.0D;
    private static final int LAYERS = 5;
    private static final int GRID = 6;

    private static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath("mcsm", "textures/sky/" + name + ".png");
    }

    private static final Identifier DECAYED = tex("clouds_decayed");
    private static final Identifier ADAMS = tex("clouds_adams");
    private static final Identifier VOID = tex("clouds_void");
    private static final Identifier CREATOR = tex("clouds_creator");
    private static final Identifier SIFT = tex("clouds_sift");

    private McsmVolumetricCloudMesh() {}

    private static Identifier sheetFor(ClientLevel level) {
        try {
            if (McsmReality.inside(level)) return DECAYED;
            if (level.dimension().equals(McsmAdams.ADAMS)) return ADAMS;
            if (level.dimension().equals(McsmCreatorRealm.DIMENSION)) return CREATOR;
            if (level.dimension().location().toString().contains("sift")) return SIFT;
            if (level.dimension().equals(McsmVoid.DIMENSION)) return VOID;
        } catch (Throwable ignored) {}
        return VOID;
    }

    private static float[] colorFor(ClientLevel level) {
        try {
            if (McsmReality.inside(level)) return new float[]{0.6f, 0.7f, 0.85f};
            if (level.dimension().equals(McsmAdams.ADAMS)) return new float[]{0.9f, 0.75f, 0.55f};
            if (level.dimension().equals(McsmCreatorRealm.DIMENSION)) return new float[]{1.0f, 0.95f, 0.85f};
            if (level.dimension().location().toString().contains("sift")) return new float[]{0.45f, 0.85f, 1.0f};
            return new float[]{0.5f, 0.3f, 0.9f}; // void purple
        } catch (Throwable t) {
            return new float[]{0.5f, 0.3f, 0.9f};
        }
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            if (!McsmExtrasConfig.paintedSky || ctx == null) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;
            Identifier sheet = sheetFor(level);
            float[] col = colorFor(level);
            int baseR = (int)(col[0] * 255);
            int baseG = (int)(col[1] * 255);
            int baseB = (int)(col[2] * 255);

            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            double clock = level.getGameTime() % 200000L;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            double step = SPAN * 2.0 / GRID;

            // Render volumetric layers as true 3D blocks of fog
            for (int layer = 0; layer < LAYERS; layer++) {
                final int li = layer;
                final double yBase = cam.y + 25.0 + li * 22.0;
                final double drift = clock * (0.001 + li * 0.0005);
                final int alpha = 60 - li * 8;
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(sheet),
                        (pose, consumer) -> {
                            for (int gx = 0; gx < GRID; gx++) {
                                for (int gz = 0; gz < GRID; gz++) {
                                    double x0 = cam.x - SPAN + gx * step + Math.sin(drift + gx) * 8.0;
                                    double x1 = x0 + step * 0.9;
                                    double z0 = cam.z - SPAN + gz * step + Math.cos(drift + gz) * 8.0;
                                    double z1 = z0 + step * 0.9;
                                    double y0 = yBase + Math.sin(x0 * 0.01 + clock * 0.001 + li) * 6.0 + Math.cos(z0 * 0.01 + clock * 0.001) * 4.0;
                                    double y1 = y0 + 12.0 + Math.sin(clock * 0.002 + li + gx) * 3.0;
                                    double size = step * 0.45;
                                    // Top face
                                    float u0 = (float)(x0 / 64.0 + drift);
                                    float v0 = (float)(z0 / 64.0 + drift * 0.5);
                                    quad(pose, consumer,
                                            x0, y1, z0, u0, v0,
                                            x1, y1, z0, u0 + 1, v0,
                                            x1, y1, z1, u0 + 1, v0 + 1,
                                            x0, y1, z1, u0, v0 + 1,
                                            baseR, baseG, baseB, alpha, 0, 1, 0);
                                    // Bottom
                                    quad(pose, consumer,
                                            x0, y0, z0, u0, v0,
                                            x0, y0, z1, u0, v0 + 1,
                                            x1, y0, z1, u0 + 1, v0 + 1,
                                            x1, y0, z0, u0 + 1, v0,
                                            baseR, baseG, baseB, alpha / 2, 0, -1, 0);
                                    // Side walls - makes it volumetric
                                    quad(pose, consumer,
                                            x0, y0, z0, u0, v0,
                                            x1, y0, z0, u0 + 1, v0,
                                            x1, y1, z0, u0 + 1, v0 + 1,
                                            x0, y1, z0, u0, v0 + 1,
                                            baseR, baseG, baseB, alpha / 2, 0, 0, -1);
                                    quad(pose, consumer,
                                            x0, y0, z1, u0, v0,
                                            x0, y1, z1, u0, v0 + 1,
                                            x1, y1, z1, u0 + 1, v0 + 1,
                                            x1, y0, z1, u0 + 1, v0,
                                            baseR, baseG, baseB, alpha / 2, 0, 0, 1);
                                    quad(pose, consumer,
                                            x0, y0, z0, u0, v0,
                                            x0, y1, z0, u0, v0 + 1,
                                            x0, y1, z1, u0 + 1, v0 + 1,
                                            x0, y0, z1, u0 + 1, v0,
                                            baseR, baseG, baseB, alpha / 2, -1, 0, 0);
                                    quad(pose, consumer,
                                            x1, y0, z0, u0, v0,
                                            x1, y0, z1, u0 + 1, v0,
                                            x1, y1, z1, u0 + 1, v0 + 1,
                                            x1, y1, z0, u0, v0 + 1,
                                            baseR, baseG, baseB, alpha / 2, 1, 0, 0);
                                }
                            }
                            // Epic fog volumes - large billowing clouds
                            for (int f = 0; f < 6; f++) {
                                double ang = clock * 0.0003 + f * 1.1 + li * 0.4;
                                double dist = 120.0 + f * 35.0;
                                double fx = cam.x + Math.cos(ang) * dist;
                                double fz = cam.z + Math.sin(ang) * dist;
                                double fy = yBase + Math.sin(clock * 0.0008 + f) * 8.0;
                                double fs = 50.0 + f * 10.0 + Math.sin(clock * 0.001 + f) * 5.0;
                                double fh = 60.0 + f * 5.0;
                                float fu = (float)(drift + f * 0.3);
                                int fa = 22 - f * 2;
                                // top of fog volume
                                quad(pose, consumer,
                                        fx - fs, fy + fh, fz - fs, fu, fu,
                                        fx + fs, fy + fh, fz - fs, fu + 1, fu,
                                        fx + fs, fy + fh, fz + fs, fu + 1, fu + 1,
                                        fx - fs, fy + fh, fz + fs, fu, fu + 1,
                                        baseR, baseG, baseB, fa, 0, 1, 0);
                                // sides
                                quad(pose, consumer,
                                        fx - fs, fy, fz - fs, fu, fu,
                                        fx + fs, fy, fz - fs, fu + 1, fu,
                                        fx + fs, fy + fh, fz - fs, fu + 1, fu + 1,
                                        fx - fs, fy + fh, fz - fs, fu, fu + 1,
                                        baseR, baseG, baseB, fa, 0, 0, -1);
                            }
                        });
            }
        } catch (Throwable ignored) {}
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

    private static void vertex(Pose pose, VertexConsumer consumer,
                               double x, double y, double z, float u, float v,
                               int r, int g, int b, int a, float nx, float ny, float nz) {
        consumer.addVertex(pose, (float)x, (float)y, (float)z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728810)
                .setNormal(pose, nx, ny, nz);
    }
}
