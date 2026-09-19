package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.GlowRenderTypes;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * V2 - Wither storm particles emitting off the ground
 * Dust, debris, purple/blue particles rising from ground around storm
 */
public final class McsmGroundParticles {

    private static final Identifier PARTICLE_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/particle/ground_particle.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");

    private McsmGroundParticles() {}

    public static void submit(LevelRenderContext ctx) {
        try {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc == null ? null : mc.level;
            if (level == null) return;
            if (ClientDistantStormManager.all().isEmpty()) return;
            Vec3 cam = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float gt = (float)(level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

            for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
                if (d.phase < 4.0F) continue;
                double dx = d.dispX - cam.x;
                double dz = d.dispZ - cam.z;
                double dist = Math.sqrt(dx*dx + dz*dz);
                if (dist > 400.0) continue;
                float phase = d.phase;
                float[] tint = McsmTeethPhaseTint.aura(phase);
                int r = (int)(tint[0] * 255);
                int g = (int)(tint[1] * 255);
                int b = (int)(tint[2] * 255);
                // Blue sides per user request
                r = (int)(0.25f * 255);
                g = (int)(0.5f * 255);
                b = 255;

                final double stormX = d.dispX;
                final double stormZ = d.dispZ;
                final double stormY = d.dispY - 60.0; // ground under storm
                final float pr = r, pg = g, pb = b;
                final float time = gt;

                // Ground particles - 60 particles rising
                collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(WHITE),
                        (pose, consumer) -> {
                            for (int i = 0; i < 60; i++) {
                                float seed = i * 0.618034f;
                                float cycle = fract(time * 0.04f + seed);
                                float angle = fract(seed) * 6.28318f;
                                float radius = (20.0f + fract(seed * 1.7f) * 80.0f) * (0.5f + cycle * 0.5f);
                                float x = (float)(stormX + Math.cos(angle) * radius + Math.sin(time * 0.01f + i) * 5.0f);
                                float z = (float)(stormZ + Math.sin(angle) * radius + Math.cos(time * 0.012f + i) * 5.0f);
                                float y = (float)(stormY + cycle * 45.0f + Math.sin(time * 0.02f + i) * 2.0f);
                                float size = 0.6f + fract(seed * 3.3f) * 1.2f;
                                float alpha = (1.0f - cycle) * 0.9f;
                                if (i % 3 == 0) {
                                    // dust - gray
                                    quadBillboard(pose, consumer, x, y, z, size, 120, 120, 130, (int)(alpha * 180), cam);
                                } else {
                                    // storm color - blue/purple
                                    quadBillboard(pose, consumer, x, y, z, size, (int)pr, (int)pg, (int)pb, (int)(alpha * 200), cam);
                                }
                            }
                            // Debris chunks
                            for (int i = 0; i < 12; i++) {
                                float seed = i * 0.71f + 0.3f;
                                float cycle = fract(time * 0.015f + seed);
                                float ang = fract(seed * 2.1f) * 6.28318f;
                                float rad = 30.0f + fract(seed * 1.3f) * 50.0f;
                                float x = (float)(stormX + Math.cos(ang) * rad);
                                float z = (float)(stormZ + Math.sin(ang) * rad);
                                float y = (float)(stormY + cycle * 25.0f);
                                float sz = 1.2f + fract(seed * 5.1f) * 2.0f;
                                float spin = time * 0.5f + i;
                                quadBillboardSpin(pose, consumer, x, y, z, sz, 80, 70, 90, (int)((1.0f - cycle) * 220), cam, spin);
                            }
                        });
            }
        } catch (Throwable ignored) {}
    }

    private static void quadBillboard(Pose pose, VertexConsumer consumer, float x, float y, float z, float size, int r, int g, int b, int a, Vec3 cam) {
        Vec3 pos = new Vec3(x, y, z);
        Vec3 toCam = cam.subtract(pos).normalize();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = toCam.cross(up).normalize();
        if (right.lengthSqr() < 0.001) right = new Vec3(1, 0, 0);
        up = right.cross(toCam).normalize();
        Vec3 rx = right.scale(size);
        Vec3 uy = up.scale(size);
        vertex(pose, consumer, pos.subtract(rx).subtract(uy), 0, 1, r, g, b, a);
        vertex(pose, consumer, pos.add(rx).subtract(uy), 1, 1, r, g, b, a);
        vertex(pose, consumer, pos.add(rx).add(uy), 1, 0, r, g, b, a);
        vertex(pose, consumer, pos.subtract(rx).add(uy), 0, 0, r, g, b, a);
    }

    private static void quadBillboardSpin(Pose pose, VertexConsumer consumer, float x, float y, float z, float size, int r, int g, int b, int a, Vec3 cam, float spin) {
        Vec3 pos = new Vec3(x, y, z);
        Vec3 toCam = cam.subtract(pos).normalize();
        float cos = Mth.cos(spin);
        float sin = Mth.sin(spin);
        Vec3 right = new Vec3(cos, 0, sin).normalize();
        Vec3 up = new Vec3(-sin, 0, cos).normalize();
        // make it face camera slightly
        Vec3 rx = right.scale(size);
        Vec3 uy = up.scale(size);
        vertex(pose, consumer, pos.subtract(rx).subtract(uy), 0, 1, r, g, b, a);
        vertex(pose, consumer, pos.add(rx).subtract(uy), 1, 1, r, g, b, a);
        vertex(pose, consumer, pos.add(rx).add(uy), 1, 0, r, g, b, a);
        vertex(pose, consumer, pos.subtract(rx).add(uy), 0, 0, r, g, b, a);
    }

    private static void vertex(Pose pose, VertexConsumer consumer, Vec3 at, float u, float v, int r, int g, int b, int a) {
        consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0, 1, 0);
    }

    private static float fract(float x) {
        return x - (float)Math.floor(x);
    }
}
