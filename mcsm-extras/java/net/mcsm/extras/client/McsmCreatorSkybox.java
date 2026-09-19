package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.McsmVoidTiers;

/**
 * BUILD #493 / 7000.0.21-M – EXTREME CREATOR SKYBOX + BLINDING GAZE WARP
 *
 * Mandate: Creator moves around skybox in ALL dimensions, built-in garden visible as part of skybox,
 * world pushed up Y>300, watching effect.
 * Phase 2 addition: Blinding Gaze Warp – sync colossal Creator face to glitch noise, flare purple emissive eyes
 * to 6.0x bloom + glitch particles on violent mutation.
 */
public final class McsmCreatorSkybox {

    private static final double CREATOR_DISTANCE = 3500.0D;
    private static final double CREATOR_HEAD_SIZE = 120.0D;
    private static final double CREATOR_BODY_SIZE = 800.0D;
    private static final double GARDEN_RADIUS = 400.0D;

    private static double creatorPosX = 0.0D;
    private static double creatorPosZ = 0.0D;
    private static double creatorTargetX = 500.0D;
    private static double creatorTargetZ = 500.0D;
    private static long nextMoveMs = 0L;
    private static float walkPhase = 0.0F;
    private static float walkBob = 0.0F;

    private static double gazeX = 0.0D;
    private static double gazeZ = 0.0D;

    private static final float[] SKIN_DARK = new float[]{0.08F, 0.10F, 0.18F};
    private static final float[] SKIN_LIGHT = new float[]{0.18F, 0.20F, 0.32F};
    private static final float[] PURPLE_EYE = new float[]{0.541F, 0.168F, 0.886F};
    private static final float[] GOLD_CROWN = new float[]{0.85F, 0.68F, 0.18F};
    private static final float[] GARDEN_GREEN = new float[]{0.15F, 0.45F, 0.15F};
    private static final float[] GARDEN_FLOWER = new float[]{0.9F, 0.3F, 0.6F};

    private static final Identifier VOID_STONE = Identifier.fromNamespaceAndPath("mcsm", "textures/block/void_stone.png");
    private static final Identifier CREATOR_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/entity/creator.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");
    private static final Identifier EYES_TEX = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/teeth_glow_white.png");

    private McsmCreatorSkybox() {}

    public static void tick() {
        try {
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.player == null) return;
            long now = System.currentTimeMillis();
            if (now >= nextMoveMs) {
                creatorTargetX = (mc.level.getRandom().nextDouble() - 0.5) * 2000.0;
                creatorTargetZ = (mc.level.getRandom().nextDouble() - 0.5) * 2000.0;
                nextMoveMs = now + 20000L + (long)(mc.level.getRandom().nextDouble() * 20000L);
            }
            double dx = creatorTargetX - creatorPosX;
            double dz = creatorTargetZ - creatorPosZ;
            creatorPosX += dx * 0.0015;
            creatorPosZ += dz * 0.0015;
            walkPhase += 0.02F;
            walkBob = Mth.sin(walkPhase) * 8.0F;
            if (mc.player != null) {
                gazeX = mc.player.getX() * 0.05;
                gazeZ = mc.player.getZ() * 0.05;
            }
            // Tick vortex too
            McsmVoidVortexSkybox.tick();
        } catch (Throwable ignored) {}
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || ctx == null) return;
            ClientLevel level = mc.level;
            if (level == null) return;
            Vec3 camera = ctx.levelState().cameraRenderState.pos;
            double playerY = mc.player != null ? mc.player.getY() : camera.y;
            double fallDist = mc.player != null ? mc.player.fallDistance : 0.0;
            boolean isOverworld = !level.dimension().equals(net.mcsm.extras.McsmVoid.DIMENSION)
                    && !level.dimension().equals(net.mcsm.extras.McsmCreatorRealm.DIMENSION)
                    && !level.dimension().equals(net.mcsm.extras.McsmAdams.ADAMS)
                    && !net.mcsm.extras.McsmReality.inside(level);

            float visibility = 1.0F;
            if (isOverworld) {
                if (playerY < 260) return;
                visibility = Mth.clamp((float)((playerY - 260) / 100.0), 0.0F, 1.0F);
            } else {
                if (playerY > McsmVoidTiers.GEL_FLOOR + 10 && level.dimension().equals(net.mcsm.extras.McsmVoid.DIMENSION)) {
                    visibility = 0.3F;
                }
            }

            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            double time = (level.getGameTime() % 240000L) + partial;

            // BUILD #493: Glitch factor for blinding gaze warp
            final float glitchFactor = McsmGlitchGenerator.computeGlitchFactor(fallDist, playerY, time);

            final double t = time;
            final float vis = visibility;
            final Vec3 cam = camera;
            final double cX = creatorPosX;
            final double cZ = creatorPosZ;
            final float bob = walkBob;
            final double gX = gazeX;
            final double gZ = gazeZ;
            final float glitch = glitchFactor;
            final double fDist = fallDist;
            final double pY = playerY;

            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(CREATOR_TEX),
                    (pose, consumer) -> {
                        emitCreatorHeadSkybox(pose, consumer, cam, cX, cZ, bob, gX, gZ, t, vis, isOverworld, pY, glitch, fDist);
                    });

            if (playerY > 300 || !isOverworld) {
                collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(VOID_STONE),
                        (pose, consumer) -> {
                            emitCreatorBodyAsWorld(pose, consumer, cam, cX, cZ, bob, t, vis, pY, isOverworld, glitch, fDist);
                        });

                collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(WHITE),
                        (pose, consumer) -> {
                            emitBuiltInGarden(pose, consumer, cam, cX, cZ, bob, t, vis, pY, glitch);
                        });
            }

            collector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.eyes(EYES_TEX),
                    (pose, consumer) -> {
                        emitCreatorEyesSkybox(pose, consumer, cam, cX, cZ, gX, gZ, t, vis, glitch);
                    });

            // Blinding Gaze Warp: glitch particles on violent mutation
            if (McsmGlitchGenerator.isMajorWarp(glitch)) {
                collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.glow(WHITE),
                        (pose, consumer) -> {
                            emitGazeWarpParticles(pose, consumer, cam, cX, cZ, gX, gZ, t, vis, glitch);
                        });
            }

            // Void vortex skybox specifically for void – submit here too so it shows in all void depths
            if (level.dimension().equals(McsmVoid.DIMENSION)) {
                McsmVoidVortexSkybox.submit(ctx);
            }

        } catch (Throwable ignored) {}
    }

    private static void emitCreatorHeadSkybox(Pose pose, VertexConsumer consumer, Vec3 camera,
                                              double cX, double cZ, float bob, double gX, double gZ,
                                              double time, float visibility, boolean isOverworld, double playerY,
                                              float glitchFactor, double fallDistance) {
        double baseX = camera.x + cX * 0.8 + Math.cos(time * 0.0001) * CREATOR_DISTANCE * 0.15;
        double baseZ = camera.z + cZ * 0.8 + Math.sin(time * 0.0001) * CREATOR_DISTANCE * 0.15;
        double baseY = isOverworld ? camera.y + 400 + bob + Math.sin(time * 0.0003) * 20 : McsmVoidTiers.FLOOR_Y + 250 + bob;
        if (isOverworld && playerY > 350) {
            baseY = camera.y + 200 + (playerY - 350) * 0.5;
        }

        // Sync colossal Creator face to glitch noise – warp position
        baseX += McsmGlitchGenerator.fractalNoise(time * 0.001, cX * 0.01) * glitchFactor * 20.0;
        baseZ += McsmGlitchGenerator.fractalNoise(time * 0.001 + 100, cZ * 0.01) * glitchFactor * 20.0;
        baseY += Math.sin(time * 0.005 + glitchFactor * 10.0) * glitchFactor * 15.0;

        double size = CREATOR_HEAD_SIZE * (isOverworld ? 1.5 : 1.0) + glitchFactor * 15.0 * Math.sin(time * 0.002);

        double lookX = gX * 0.2 + glitchFactor * Math.sin(time * 0.01) * 5.0;
        double lookZ = gZ * 0.2 + glitchFactor * Math.cos(time * 0.01) * 5.0;

        float pulse = 0.9F + 0.1F * Mth.sin((float)time * 0.002F) + glitchFactor * 0.2F;
        int r = (int)(SKIN_DARK[0] * 255 * pulse);
        int g = (int)(SKIN_DARK[1] * 255 * pulse);
        int b = (int)(SKIN_DARK[2] * 255 * pulse);
        int a = (int)(visibility * 255);

        double[] d0 = McsmGlitchGenerator.displaceVertex(baseX - size*0.6 + lookX, baseY - size*0.3, baseZ + size*0.5 + lookZ, time, fallDistance, glitchFactor, 1);
        double[] d1 = McsmGlitchGenerator.displaceVertex(baseX + size*0.6 + lookX, baseY - size*0.3, baseZ + size*0.5 + lookZ, time, fallDistance, glitchFactor, 2);
        double[] d2 = McsmGlitchGenerator.displaceVertex(baseX + size*0.6 + lookX, baseY + size*0.7, baseZ + size*0.5 + lookZ, time, fallDistance, glitchFactor, 3);
        double[] d3 = McsmGlitchGenerator.displaceVertex(baseX - size*0.6 + lookX, baseY + size*0.7, baseZ + size*0.5 + lookZ, time, fallDistance, glitchFactor, 4);

        quad(pose, consumer,
                d0[0], d0[1], d0[2], 0, 0,
                d1[0], d1[1], d1[2], 1, 0,
                d2[0], d2[1], d2[2], 1, 1,
                d3[0], d3[1], d3[2], 0, 1,
                r, g, b, a, 0, 0, 1);

        double crownY = baseY + size*0.7;
        int points = 7;
        for (int i = 0; i < points; i++) {
            double a0 = (i / (double)points) * Math.PI * 2;
            double a1 = ((i+1) / (double)points) * Math.PI * 2;
            double r0 = size*0.7 + glitchFactor * Math.sin(time * 0.005 + i) * 5.0;
            double r1 = size*0.5;
            double x0 = baseX + Math.cos(a0)*r0;
            double z0 = baseZ + Math.sin(a0)*r0;
            double x1 = baseX + Math.cos(a1)*r0;
            double z1 = baseZ + Math.sin(a1)*r0;
            double xt = baseX + Math.cos((a0+a1)*0.5)*r1;
            double zt = baseZ + Math.sin((a0+a1)*0.5)*r1;
            double yt = crownY + 30 + Mth.sin((float)(time*0.002 + i))*3 + glitchFactor * Math.sin(time * 0.01 + i) * 8.0;

            int cr = (int)(GOLD_CROWN[0]*255);
            int cg = (int)(GOLD_CROWN[1]*255);
            int cb = (int)(GOLD_CROWN[2]*255);

            quad(pose, consumer,
                    x0, crownY, z0, 0, 0,
                    x1, crownY, z1, 1, 0,
                    xt, yt, zt, 0.5F, 1,
                    xt, yt, zt, 0.5F, 1,
                    cr, cg, cb, a, 0, 1, 0);
        }
    }

    private static void emitCreatorBodyAsWorld(Pose pose, VertexConsumer consumer, Vec3 camera,
                                               double cX, double cZ, float bob, double time,
                                               float visibility, double playerY, boolean isOverworld,
                                               float glitchFactor, double fallDistance) {
        double bodyX = camera.x + cX * 0.3;
        double bodyZ = camera.z + cZ * 0.3;
        double bodyY = isOverworld ? camera.y - 200 - (playerY > 400 ? (playerY - 400)*0.8 : 0) : McsmVoidTiers.FLOOR_Y - 20;

        double size = CREATOR_BODY_SIZE * (isOverworld ? 2.0 : 1.2);

        int segments = 16;
        for (int sx = -segments/2; sx < segments/2; sx++) {
            for (int sz = -segments/2; sz < segments/2; sz++) {
                double x0 = bodyX + sx * size/segments;
                double z0 = bodyZ + sz * size/segments;
                double x1 = bodyX + (sx+1) * size/segments;
                double z1 = bodyZ + (sz+1) * size/segments;

                double y0 = bodyY + Math.sin(sx*0.3)*10 + Math.cos(sz*0.3)*10 + fractalNoise(sx*0.5, sz*0.5)*5;
                double y1 = bodyY + Math.sin((sx+1)*0.3)*10 + Math.cos(sz*0.3)*10 + fractalNoise((sx+1)*0.5, sz*0.5)*5;
                double y2 = bodyY + Math.sin((sx+1)*0.3)*10 + Math.cos((sz+1)*0.3)*10 + fractalNoise((sx+1)*0.5, (sz+1)*0.5)*5;
                double y3 = bodyY + Math.sin(sx*0.3)*10 + Math.cos((sz+1)*0.3)*10 + fractalNoise(sx*0.5, (sz+1)*0.5)*5;

                // Apply glitch displacement to body – world warps when deep
                double[] d0 = McsmGlitchGenerator.displaceVertex(x0, y0, z0, time, fallDistance, glitchFactor, sx*10 + sz);
                double[] d1 = McsmGlitchGenerator.displaceVertex(x1, y1, z0, time, fallDistance, glitchFactor, sx*10 + sz + 1);
                double[] d2 = McsmGlitchGenerator.displaceVertex(x1, y2, z1, time, fallDistance, glitchFactor, sx*10 + sz + 2);
                double[] d3 = McsmGlitchGenerator.displaceVertex(x0, y3, z1, time, fallDistance, glitchFactor, sx*10 + sz + 3);

                float shade = 0.1F + 0.05F * (float)fractalNoise(sx, sz);
                int r = (int)((SKIN_DARK[0]+shade)*255);
                int g = (int)((SKIN_DARK[1]+shade)*255);
                int b = (int)((SKIN_DARK[2]+shade)*255);
                int a = (int)(visibility * 200 * (isOverworld ? 0.6 : 1.0));

                quad(pose, consumer,
                        d0[0], d0[1], d0[2], 0, 0,
                        d1[0], d1[1], d1[2], 1, 0,
                        d2[0], d2[1], d2[2], 1, 1,
                        d3[0], d3[1], d3[2], 0, 1,
                        r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitBuiltInGarden(Pose pose, VertexConsumer consumer, Vec3 camera,
                                          double cX, double cZ, float bob, double time,
                                          float visibility, double playerY, float glitchFactor) {
        double gardenX = camera.x + cX * 0.3 + 100;
        double gardenZ = camera.z + cZ * 0.3 + 100;
        double gardenY = camera.y - 180 - (playerY > 400 ? (playerY - 400)*0.8 : 0);
        if (gardenY < McsmVoidTiers.FLOOR_Y) gardenY = McsmVoidTiers.FLOOR_Y + 5;

        double size = GARDEN_RADIUS;

        int patches = 8;
        for (int i = 0; i < patches; i++) {
            double angle = (i / (double)patches) * Math.PI * 2 + time * 0.0002;
            double dist = (i % 3) * 40 + 20;
            double x = gardenX + Math.cos(angle) * dist + glitchFactor * Math.sin(time * 0.002 + i) * 5.0;
            double z = gardenZ + Math.sin(angle) * dist + glitchFactor * Math.cos(time * 0.002 + i) * 5.0;
            double y = gardenY + Math.sin(time*0.001 + i)*2;

            double patchSize = 15 + (i%4)*5 + glitchFactor * 3.0;

            int r = (int)(GARDEN_GREEN[0]*255);
            int g = (int)(GARDEN_GREEN[1]*255);
            int b = (int)(GARDEN_GREEN[2]*255);
            int a = (int)(visibility * 220);

            quad(pose, consumer,
                    x - patchSize, y, z - patchSize, 0, 0,
                    x + patchSize, y, z - patchSize, 1, 0,
                    x + patchSize, y, z + patchSize, 1, 1,
                    x - patchSize, y, z + patchSize, 0, 1,
                    r, g, b, a, 0, 1, 0);

            if (i % 2 == 0) {
                double fx = x + 3;
                double fz = z + 3;
                double fy = y + 1;
                double fs = 2 + glitchFactor;
                int fr = (int)(GARDEN_FLOWER[0]*255);
                int fg = (int)(GARDEN_FLOWER[1]*255);
                int fb = (int)(GARDEN_FLOWER[2]*255);

                quadFullBright(pose, consumer,
                        fx - fs, fy, fz - fs, 0, 0,
                        fx + fs, fy, fz - fs, 1, 0,
                        fx + fs, fy, fz + fs, 1, 1,
                        fx - fs, fy, fz + fs, 0, 1,
                        fr, fg, fb, (int)(visibility*200));
            }
        }

        for (int t = 0; t < 4; t++) {
            double angle = (t / 4.0) * Math.PI * 2 + time * 0.0001;
            double x = gardenX + Math.cos(angle) * (size*0.6);
            double z = gardenZ + Math.sin(angle) * (size*0.6);
            double y = gardenY;

            quad(pose, consumer,
                    x - 1, y, z - 1, 0, 0,
                    x + 1, y, z - 1, 1, 0,
                    x + 1, y + 12, z + 1, 1, 1,
                    x - 1, y + 12, z + 1, 0, 1,
                    60, 40, 20, (int)(visibility*200), 0, 1, 0);

            double ly = y + 12;
            quad(pose, consumer,
                    x - 4, ly, z - 4, 0, 0,
                    x + 4, ly, z - 4, 1, 0,
                    x + 4, ly + 6, z + 4, 1, 1,
                    x - 4, ly + 6, z + 4, 0, 1,
                    20, 80, 20, (int)(visibility*200), 0, 1, 0);
        }
    }

    private static void emitCreatorEyesSkybox(Pose pose, VertexConsumer consumer, Vec3 camera,
                                              double cX, double cZ, double gX, double gZ,
                                              double time, float visibility, float glitchFactor) {
        double baseX = camera.x + cX * 0.8 + Math.cos(time * 0.0001) * CREATOR_DISTANCE * 0.15;
        double baseZ = camera.z + cZ * 0.8 + Math.sin(time * 0.0001) * CREATOR_DISTANCE * 0.15;
        double baseY = camera.y + 400 + Mth.sin((float)(time*0.0003))*20;

        // Blinding Gaze Warp: flare purple emissive eyes to 6.0x bloom
        float bloom = McsmGlitchGenerator.computeEyeBloom(4.5F, glitchFactor);

        double eyeDist = 28.0 + glitchFactor * 5.0 * Math.sin(time * 0.005);
        double eyeY = baseY + 15;

        for (int eye = 0; eye < 2; eye++) {
            double offset = (eye == 0 ? -1 : 1) * eyeDist;
            double x = baseX + offset + gX*0.3 + glitchFactor * Math.sin(time * 0.01 + eye) * 4.0;
            double z = baseZ + CREATOR_HEAD_SIZE*0.5 + 5 + gZ*0.3 + glitchFactor * Math.cos(time * 0.01 + eye) * 4.0;
            double y = eyeY + glitchFactor * Math.sin(time * 0.008 + eye) * 5.0;

            double size = 10.0 + Mth.sin((float)(time*0.01 + eye))*1.2 + glitchFactor * 5.0;
            float pulse = 0.8F + 0.2F * Mth.sin((float)(time*0.008 + eye*1.5F)) + glitchFactor * 0.4F;

            int r = (int)(PURPLE_EYE[0]*255*pulse*bloom);
            int g = (int)(PURPLE_EYE[1]*255*pulse*bloom);
            int b = (int)(PURPLE_EYE[2]*255*pulse*bloom);
            r = Math.min(255, r); g = Math.min(255, g); b = Math.min(255, b);
            int a = (int)(visibility*255);

            quadFullBright(pose, consumer,
                    x - size, y - size, z, 0, 0,
                    x + size, y - size, z, 1, 0,
                    x + size, y + size, z, 1, 1,
                    x - size, y + size, z, 0, 1,
                    r, g, b, a);
        }
    }

    // ---- NEW: Gaze warp particles – glitch particles on violent mutation ----
    private static void emitGazeWarpParticles(Pose pose, VertexConsumer consumer, Vec3 camera,
                                              double cX, double cZ, double gX, double gZ,
                                              double time, float visibility, float glitchFactor) {
        double baseX = camera.x + cX * 0.8 + Math.cos(time * 0.0001) * CREATOR_DISTANCE * 0.15;
        double baseZ = camera.z + cZ * 0.8 + Math.sin(time * 0.0001) * CREATOR_DISTANCE * 0.15;
        double baseY = camera.y + 400 + Mth.sin((float)(time*0.0003))*20 + 15;

        int count = (int)(glitchFactor * 30) + 10;
        for (int i = 0; i < count; i++) {
            double angle = (i / (double) count) * Math.PI * 2.0 + time * 0.008 * (i % 2 == 0 ? 1 : -1);
            double radius = 20.0 + i * 3.0 + Mth.sin((float)(time * 0.01 + i)) * glitchFactor * 25.0;
            double x = baseX + Math.cos(angle) * radius + McsmGlitchGenerator.fractalNoise(i * 0.8, time * 0.003) * glitchFactor * 40.0;
            double z = baseZ + Math.sin(angle) * radius + McsmGlitchGenerator.fractalNoise(i * 0.9 + 100, time * 0.003) * glitchFactor * 40.0;
            double y = baseY + Mth.sin((float)(time * 0.005 + i)) * 30.0 * glitchFactor + i * 1.5;

            double size = 2.0 + glitchFactor * 4.0;
            int r = 180 + (int)(75 * Math.sin(time * 0.02 + i));
            int g = 60 + (int)(60 * Math.cos(time * 0.018 + i));
            int b = 255;
            int a = (int)(220 * glitchFactor * visibility);

            quadFullBright(pose, consumer,
                    x - size, y - size, z, 0, 0,
                    x + size, y - size, z, 1, 0,
                    x + size, y + size, z, 1, 1,
                    x - size, y + size, z, 0, 1,
                    r, g, b, a);
        }
    }

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

    private static double fractalNoise(double x, double y) {
        double n = 0;
        n += Math.sin(x*1.0 + y*0.7)*0.5;
        n += Math.sin(x*2.3 - y*1.1)*0.25;
        n += Math.sin(x*4.7 + y*2.3)*0.125;
        return n;
    }

    public static String state() {
        return "creator-skybox: pos=("+ (int)creatorPosX + "," + (int)creatorPosZ + ") target=("+ (int)creatorTargetX + "," + (int)creatorTargetZ + ") walk="+walkPhase+" gaze=("+ (int)gazeX + "," + (int)gazeZ + ")";
    }
}
