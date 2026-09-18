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
 * V2 NEXT-GEN Phase 2 - VFX Light Rays (God Rays) to main overworld
 * Volumetric light shafts through clouds/trees, dust motes, Sodium-safe.
 */
public final class McsmLightRays {

    private static final Identifier LIGHT_RAY_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/vfx/light_ray.png");
    private static final Identifier DUST_MOTE = Identifier.fromNamespaceAndPath("mcsm", "textures/vfx/dust_mote.png");

    private static float rayTime = 0f;

    private McsmLightRays() {}

    public static void submit(LevelRenderContext ctx) {
        try {
            if (ctx == null) return;
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;
            String dim = level.dimension().identifier().toString();
            if (!dim.contains("overworld") && !dim.equals("minecraft:overworld")) return; // Overworld only

            float dayTime = level.getTimeOfDay(mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
            // Light rays only during day, when sun is out
            if (dayTime > 0.55f) return;
            if (level.isRaining() || level.isThundering()) return;

            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float t = level.getGameTime() + partial;
            rayTime += 0.01f;

            // Sun direction for rays
            float sunAngle = dayTime * Mth.TWO_PI;
            Vec3 sunDir = new Vec3(Mth.cos(sunAngle), Mth.sin(sunAngle), 0.2).normalize();

            // Light rays through clouds - shafts from sky
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(LIGHT_RAY_TEX),
                (pose, consumer) -> {
                    int rayCount = 6;
                    for (int r = 0; r < rayCount; r++) {
                        float offsetX = Mth.sin(rayTime * 0.3f + r * 1.7f) * 40f + r * 25f - 60f;
                        float offsetZ = Mth.cos(rayTime * 0.2f + r * 1.3f) * 40f + r * 18f - 40f;
                        double baseX = cam.x + offsetX;
                        double baseZ = cam.z + offsetZ;
                        double topY = cam.y + 180 + Mth.sin(rayTime + r) * 10;
                        double bottomY = cam.y - 20;

                        // Ray width varies with height - wider at top (volumetric)
                        double topWidth = 18.0 + Mth.sin(rayTime * 0.5f + r) * 4.0;
                        double bottomWidth = 4.0;

                        // Slight sway
                        double sway = Mth.sin(rayTime * 0.4f + r * 0.8f) * 3.0;

                        // Four vertices for ray quad that always faces camera slightly
                        // Top edge
                        double tx0 = baseX - topWidth / 2 + sway;
                        double tx1 = baseX + topWidth / 2 + sway;
                        // Bottom edge
                        double bx0 = baseX - bottomWidth / 2;
                        double bx1 = baseX + bottomWidth / 2;

                        // Sun color - warm golden
                        int rr = 255;
                        int gg = 240 + (int)(Mth.sin(rayTime + r) * 10);
                        int bb = 180 + (int)(Mth.sin(rayTime * 0.7f + r) * 20);
                        int alphaTop = 25 + (int)(Mth.sin(rayTime * 0.6f + r * 1.1f) * 8);
                        int alphaBottom = 5;

                        // Gradient ray
                        quadGradient(pose, consumer,
                            tx0, topY, baseZ, 0, 0, rr, gg, bb, alphaTop,
                            tx1, topY, baseZ, 1, 0, rr, gg, bb, alphaTop,
                            bx1, bottomY, baseZ, 1, 1, rr, gg, bb, alphaBottom,
                            bx0, bottomY, baseZ, 0, 1, rr, gg, bb, alphaBottom);
                    }
                });

            // Dust motes floating in light rays
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(DUST_MOTE),
                (pose, consumer) -> {
                    int moteCount = 30;
                    for (int i = 0; i < moteCount; i++) {
                        float ang = (float)i / moteCount * Mth.TWO_PI * 2.3f + i * 0.7f;
                        double mx = cam.x + Mth.cos(ang + rayTime * 0.1f) * (20 + i % 10 * 3) + Mth.sin(rayTime * 0.5f + i) * 5;
                        double mz = cam.z + Mth.sin(ang * 0.8f + rayTime * 0.08f) * (20 + i % 8 * 2.5f);
                        double my = cam.y + Mth.sin(rayTime * 0.3f + i * 0.4f) * 10 + (i % 15) - 5;
                        // Only within ray volume
                        if (my < cam.y - 15 || my > cam.y + 30) continue;
                        float size = 0.15f + Mth.sin(rayTime * 0.9f + i) * 0.08f;
                        int bright = 180 + (int)(Mth.sin(rayTime * 1.2f + i * 0.6f) * 40);
                        quadBillboard(pose, consumer, mx, my, mz, size, bright, bright, bright - 20, 120, cam);
                    }
                });

        } catch (Throwable ignored) {}
    }

    private static void quadGradient(Pose pose, VertexConsumer consumer,
                                     double x0, double y0, double z0, float u0, float v0, int r0, int g0, int b0, int a0,
                                     double x1, double y1, double z1, float u1, float v1, int r1, int g1, int b1, int a1,
                                     double x2, double y2, double z2, float u2, float v2, int r2, int g2, int b2, int a2,
                                     double x3, double y3, double z3, float u3, float v3, int r3, int g3, int b3, int a3) {
        consumer.addVertex(pose, (float)x0, (float)y0, (float)z0).setColor(r0, g0, b0, a0).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(r1, g1, b1, a1).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(r2, g2, b2, a2).setUv(u2, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, (float)x3, (float)y3, (float)z3).setColor(r3, g3, b3, a3).setUv(u3, v3).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0, 1, 0);
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
}
