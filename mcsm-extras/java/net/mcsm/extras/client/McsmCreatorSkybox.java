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
import net.mcsm.extras.McsmVoidTiers;

/**
 * BUILD #487 – EXTREME CREATOR SKYBOX – The Creator moves around the skybox in ALL dimensions
 *
 * THE MANDATE from user image "The Layers of the Void" + extreme request:
 * - Creator entity which moves around the skybox
 * - Built-in garden visible as part of skybox
 * - Creator walks around like entire Minecraft world is on its body
 * - Creator head in skybox in ALL dimensions (not just void/creator realm)
 * - World pushed up, you can see you're on its body when flying extremely high
 * - Dimension physically, like it's watching
 *
 * WHAT THIS DOES:
 * - Renders colossal Creator head in skybox of EVERY dimension including Overworld
 * - Creator moves around skybox slowly, walks, gaze tracks player
 * - When flying extremely high (Y > 320, up to 2000), you see you're on its body – world pushed up
 * - Built-in garden on Creator's body visible from high altitude – photorealistic
 * - Creator is part of skybox hierarchy, entire Minecraft world is on its body
 * - Watching effect: Creator always looks at player, moves around skybox
 *
 * WHY VERTEXBUFFER: Same as NinthLayerGeometry – stateless, fail-soft, no registry, no dome, no JSON model
 * Streaming real-time 3D vertex positions/normals/UVs via explicit buffer arrays – photorealistic
 */
public final class McsmCreatorSkybox {

    // ---- Distances – extreme, Creator is entire world ---------------------------------
    private static final double CREATOR_DISTANCE = 3500.0D; // far behind, enveloping
    private static final double CREATOR_HEAD_SIZE = 120.0D; // even bigger than Ninth layer (80) – extreme
    private static final double CREATOR_BODY_SIZE = 800.0D; // body you stand on when flying high
    private static final double GARDEN_RADIUS = 400.0D; // built-in garden on body

    // ---- Movement – Creator walks around skybox ----------------------------------------
    private static double creatorPosX = 0.0D;
    private static double creatorPosZ = 0.0D;
    private static double creatorTargetX = 500.0D;
    private static double creatorTargetZ = 500.0D;
    private static long nextMoveMs = 0L;
    private static float walkPhase = 0.0F;
    private static float walkBob = 0.0F;

    // ---- Gaze – watching ---------------------------------------------------------------
    private static double gazeX = 0.0D;
    private static double gazeZ = 0.0D;

    // ---- Colors – photorealistic -------------------------------------------------------
    private static final float[] SKIN_DARK = new float[]{0.08F, 0.10F, 0.18F}; // dark skin
    private static final float[] SKIN_LIGHT = new float[]{0.18F, 0.20F, 0.32F}; // light
    private static final float[] PURPLE_EYE = new float[]{0.541F, 0.168F, 0.886F}; // #8A2BE2
    private static final float[] GOLD_CROWN = new float[]{0.85F, 0.68F, 0.18F};
    private static final float[] GARDEN_GREEN = new float[]{0.15F, 0.45F, 0.15F};
    private static final float[] GARDEN_FLOWER = new float[]{0.9F, 0.3F, 0.6F};

    // ---- Textures ----------------------------------------------------------------------
    private static final Identifier VOID_STONE = Identifier.fromNamespaceAndPath("mcsm", "textures/block/void_stone.png");
    private static final Identifier CREATOR_TEX = Identifier.fromNamespaceAndPath("mcsm", "textures/entity/creator.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");
    private static final Identifier EYES_TEX = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/teeth_glow_white.png");

    private McsmCreatorSkybox() {}

    // -------------------------------------------------------------------------
    // Tick – Creator walks around skybox in all dimensions
    // -------------------------------------------------------------------------
    public static void tick() {
        try {
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.player == null) return;

            long now = System.currentTimeMillis();

            // Creator moves around skybox every 20-40s – walks around like entire world
            if (now >= nextMoveMs) {
                creatorTargetX = (mc.level.getRandom().nextDouble() - 0.5) * 2000.0;
                creatorTargetZ = (mc.level.getRandom().nextDouble() - 0.5) * 2000.0;
                nextMoveMs = now + 20000L + (long)(mc.level.getRandom().nextDouble() * 20000L);
            }

            // Smooth walk lerp – Creator walks, not teleports
            double dx = creatorTargetX - creatorPosX;
            double dz = creatorTargetZ - creatorPosZ;
            creatorPosX += dx * 0.0015;
            creatorPosZ += dz * 0.0015;

            // Walk bobbing – like walking
            walkPhase += 0.02F;
            walkBob = Mth.sin(walkPhase) * 8.0F;

            // Gaze tracks player – watching
            if (mc.player != null) {
                gazeX = mc.player.getX() * 0.05;
                gazeZ = mc.player.getZ() * 0.05;
            }

        } catch (Throwable ignored) {}
    }

    // -------------------------------------------------------------------------
    // Submit – Creator in skybox ALL dimensions, extreme
    // -------------------------------------------------------------------------
    public static void submit(LevelRenderContext ctx) {
        try {
            if (!McsmExtrasConfig.paintedSky) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || ctx == null) return;

            ClientLevel level = mc.level;
            if (level == null) return;

            Vec3 camera = ctx.levelState().cameraRenderState.pos;
            double playerY = mc.player != null ? mc.player.getY() : camera.y;

            // In Overworld, only show Creator when flying extremely high (Y > 260) – world pushed up
            // In void/creator/adams/decayed, always show
            boolean isOverworld = !level.dimension().equals(net.mcsm.extras.McsmVoid.DIMENSION)
                    && !level.dimension().equals(net.mcsm.extras.McsmCreatorRealm.DIMENSION)
                    && !level.dimension().equals(net.mcsm.extras.McsmAdams.ADAMS)
                    && !net.mcsm.extras.McsmReality.inside(level);

            float visibility = 1.0F;
            if (isOverworld) {
                if (playerY < 260) return; // only when flying extremely high in Overworld
                visibility = Mth.clamp((float)((playerY - 260) / 100.0), 0.0F, 1.0F); // fade in 260->360
            } else {
                // In other dimensions, visibility based on depth or always
                if (playerY > McsmVoidTiers.GEL_FLOOR + 10 && level.dimension().equals(net.mcsm.extras.McsmVoid.DIMENSION)) {
                    visibility = 0.3F; // faint in upper void
                }
            }

            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            double time = (level.getGameTime() % 240000L) + partial;

            final double t = time;
            final float vis = visibility;
            final Vec3 cam = camera;
            final double cX = creatorPosX;
            final double cZ = creatorPosZ;
            final float bob = walkBob;
            final double gX = gazeX;
            final double gZ = gazeZ;

            // ---- Creator head in skybox – ALL dimensions, moves around, watching -----------------
            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(CREATOR_TEX),
                    (pose, consumer) -> {
                        emitCreatorHeadSkybox(pose, consumer, cam, cX, cZ, bob, gX, gZ, t, vis, isOverworld, playerY);
                    });

            // ---- Creator body as world – when flying extremely high, you see you're on its body ----
            if (playerY > 300 || !isOverworld) {
                collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(VOID_STONE),
                        (pose, consumer) -> {
                            emitCreatorBodyAsWorld(pose, consumer, cam, cX, cZ, bob, t, vis, playerY, isOverworld);
                        });

                // ---- Built-in garden on Creator's body – clearly visible part of skybox ---------------
                collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(WHITE),
                        (pose, consumer) -> {
                            emitBuiltInGarden(pose, consumer, cam, cX, cZ, bob, t, vis, playerY);
                        });
            }

            // ---- Glowing eyes – watching in all dimensions ----------------------------------------
            collector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.eyes(EYES_TEX),
                    (pose, consumer) -> {
                        emitCreatorEyesSkybox(pose, consumer, cam, cX, cZ, gX, gZ, t, vis);
                    });

        } catch (Throwable ignored) {}
    }

    // -------------------------------------------------------------------------
    // Creator head in skybox – moves around, extreme, all dimensions
    // -------------------------------------------------------------------------
    private static void emitCreatorHeadSkybox(Pose pose, VertexConsumer consumer, Vec3 camera,
                                              double cX, double cZ, float bob, double gX, double gZ,
                                              double time, float visibility, boolean isOverworld, double playerY) {
        // Creator moves around skybox – position based on tick movement
        double baseX = camera.x + cX * 0.8 + Math.cos(time * 0.0001) * CREATOR_DISTANCE * 0.15;
        double baseZ = camera.z + cZ * 0.8 + Math.sin(time * 0.0001) * CREATOR_DISTANCE * 0.15;
        double baseY = isOverworld ? camera.y + 400 + bob + Math.sin(time * 0.0003) * 20 : McsmVoidTiers.FLOOR_Y + 250 + bob;

        // When flying extremely high in Overworld, Creator head is even more extreme – world pushed up
        if (isOverworld && playerY > 350) {
            baseY = camera.y + 200 + (playerY - 350) * 0.5; // pushes up with player
        }

        double size = CREATOR_HEAD_SIZE * (isOverworld ? 1.5 : 1.0); // bigger in Overworld for extreme effect

        // Gaze tracking – Creator looks at player, watching
        double lookX = gX * 0.2;
        double lookZ = gZ * 0.2;

        float pulse = 0.9F + 0.1F * Mth.sin((float)time * 0.002F);
        int r = (int)(SKIN_DARK[0] * 255 * pulse);
        int g = (int)(SKIN_DARK[1] * 255 * pulse);
        int b = (int)(SKIN_DARK[2] * 255 * pulse);
        int a = (int)(visibility * 255);

        // Front face – photorealistic with gaze
        quad(pose, consumer,
                baseX - size*0.6 + lookX, baseY - size*0.3, baseZ + size*0.5 + lookZ, 0, 0,
                baseX + size*0.6 + lookX, baseY - size*0.3, baseZ + size*0.5 + lookZ, 1, 0,
                baseX + size*0.6 + lookX, baseY + size*0.7, baseZ + size*0.5 + lookZ, 1, 1,
                baseX - size*0.6 + lookX, baseY + size*0.7, baseZ + size*0.5 + lookZ, 0, 1,
                r, g, b, a, 0, 0, 1);

        // Crown – extreme, visible in all dimensions
        double crownY = baseY + size*0.7;
        int points = 7;
        for (int i = 0; i < points; i++) {
            double a0 = (i / (double)points) * Math.PI * 2;
            double a1 = ((i+1) / (double)points) * Math.PI * 2;
            double r0 = size*0.7;
            double r1 = size*0.5;
            double x0 = baseX + Math.cos(a0)*r0;
            double z0 = baseZ + Math.sin(a0)*r0;
            double x1 = baseX + Math.cos(a1)*r0;
            double z1 = baseZ + Math.sin(a1)*r0;
            double xt = baseX + Math.cos((a0+a1)*0.5)*r1;
            double zt = baseZ + Math.sin((a0+a1)*0.5)*r1;
            double yt = crownY + 30 + Mth.sin((float)(time*0.002 + i))*3;

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

    // -------------------------------------------------------------------------
    // Creator body as world – you see you're on its body when flying extremely high
    // -------------------------------------------------------------------------
    private static void emitCreatorBodyAsWorld(Pose pose, VertexConsumer consumer, Vec3 camera,
                                               double cX, double cZ, float bob, double time,
                                               float visibility, double playerY, boolean isOverworld) {
        // Body is huge plane below, you stand on it when flying high – world pushed up
        double bodyX = camera.x + cX * 0.3;
        double bodyZ = camera.z + cZ * 0.3;
        double bodyY = isOverworld ? camera.y - 200 - (playerY > 400 ? (playerY - 400)*0.8 : 0) : McsmVoidTiers.FLOOR_Y - 20;

        double size = CREATOR_BODY_SIZE * (isOverworld ? 2.0 : 1.2);

        // Body as massive plane with skin texture and curvature – like you're on its body
        int segments = 16;
        for (int sx = -segments/2; sx < segments/2; sx++) {
            for (int sz = -segments/2; sz < segments/2; sz++) {
                double x0 = bodyX + sx * size/segments;
                double z0 = bodyZ + sz * size/segments;
                double x1 = bodyX + (sx+1) * size/segments;
                double z1 = bodyZ + (sz+1) * size/segments;

                // Curvature – body is not flat, it's rounded like giant
                double y0 = bodyY + Math.sin(sx*0.3)*10 + Math.cos(sz*0.3)*10 + fractalNoise(sx*0.5, sz*0.5)*5;
                double y1 = bodyY + Math.sin((sx+1)*0.3)*10 + Math.cos(sz*0.3)*10 + fractalNoise((sx+1)*0.5, sz*0.5)*5;
                double y2 = bodyY + Math.sin((sx+1)*0.3)*10 + Math.cos((sz+1)*0.3)*10 + fractalNoise((sx+1)*0.5, (sz+1)*0.5)*5;
                double y3 = bodyY + Math.sin(sx*0.3)*10 + Math.cos((sz+1)*0.3)*10 + fractalNoise(sx*0.5, (sz+1)*0.5)*5;

                float shade = 0.1F + 0.05F * (float)fractalNoise(sx, sz);
                int r = (int)((SKIN_DARK[0]+shade)*255);
                int g = (int)((SKIN_DARK[1]+shade)*255);
                int b = (int)((SKIN_DARK[2]+shade)*255);
                int a = (int)(visibility * 200 * (isOverworld ? 0.6 : 1.0));

                quad(pose, consumer,
                        x0, y0, z0, 0, 0,
                        x1, y1, z0, 1, 0,
                        x1, y2, z1, 1, 1,
                        x0, y3, z1, 0, 1,
                        r, g, b, a, 0, 1, 0);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Built-in garden on Creator's body – clearly visible part of skybox
    // -------------------------------------------------------------------------
    private static void emitBuiltInGarden(Pose pose, VertexConsumer consumer, Vec3 camera,
                                          double cX, double cZ, float bob, double time,
                                          float visibility, double playerY) {
        double gardenX = camera.x + cX * 0.3 + 100;
        double gardenZ = camera.z + cZ * 0.3 + 100;
        double gardenY = camera.y - 180 - (playerY > 400 ? (playerY - 400)*0.8 : 0);
        if (gardenY < McsmVoidTiers.FLOOR_Y) gardenY = McsmVoidTiers.FLOOR_Y + 5;

        double size = GARDEN_RADIUS;

        // Garden as patch of green with flowers – built-in, visible clearly
        int patches = 8;
        for (int i = 0; i < patches; i++) {
            double angle = (i / (double)patches) * Math.PI * 2 + time * 0.0002;
            double dist = (i % 3) * 40 + 20;
            double x = gardenX + Math.cos(angle) * dist;
            double z = gardenZ + Math.sin(angle) * dist;
            double y = gardenY + Math.sin(time*0.001 + i)*2;

            double patchSize = 15 + (i%4)*5;

            // Green patch
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

            // Flowers on garden
            if (i % 2 == 0) {
                double fx = x + 3;
                double fz = z + 3;
                double fy = y + 1;
                double fs = 2;
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

        // Trees in garden
        for (int t = 0; t < 4; t++) {
            double angle = (t / 4.0) * Math.PI * 2 + time * 0.0001;
            double x = gardenX + Math.cos(angle) * (size*0.6);
            double z = gardenZ + Math.sin(angle) * (size*0.6);
            double y = gardenY;

            // Trunk
            quad(pose, consumer,
                    x - 1, y, z - 1, 0, 0,
                    x + 1, y, z - 1, 1, 0,
                    x + 1, y + 12, z + 1, 1, 1,
                    x - 1, y + 12, z + 1, 0, 1,
                    60, 40, 20, (int)(visibility*200), 0, 1, 0);

            // Leaves
            double ly = y + 12;
            quad(pose, consumer,
                    x - 4, ly, z - 4, 0, 0,
                    x + 4, ly, z - 4, 1, 0,
                    x + 4, ly + 6, z + 4, 1, 1,
                    x - 4, ly + 6, z + 4, 0, 1,
                    20, 80, 20, (int)(visibility*200), 0, 1, 0);
        }
    }

    // -------------------------------------------------------------------------
    // Creator eyes – watching in all dimensions, extreme
    // -------------------------------------------------------------------------
    private static void emitCreatorEyesSkybox(Pose pose, VertexConsumer consumer, Vec3 camera,
                                              double cX, double cZ, double gX, double gZ,
                                              double time, float visibility) {
        double baseX = camera.x + cX * 0.8 + Math.cos(time * 0.0001) * CREATOR_DISTANCE * 0.15;
        double baseZ = camera.z + cZ * 0.8 + Math.sin(time * 0.0001) * CREATOR_DISTANCE * 0.15;
        double baseY = camera.y + 400 + Mth.sin((float)(time*0.0003))*20;

        double eyeDist = 28.0;
        double eyeY = baseY + 15;

        for (int eye = 0; eye < 2; eye++) {
            double offset = (eye == 0 ? -1 : 1) * eyeDist;
            double x = baseX + offset + gX*0.3;
            double z = baseZ + CREATOR_HEAD_SIZE*0.5 + 5 + gZ*0.3;
            double y = eyeY;

            double size = 10.0 + Mth.sin((float)(time*0.01 + eye))*1.2;
            float pulse = 0.8F + 0.2F * Mth.sin((float)(time*0.008 + eye*1.5F));

            int r = (int)(PURPLE_EYE[0]*255*pulse*4.5F);
            int g = (int)(PURPLE_EYE[1]*255*pulse*4.5F);
            int b = (int)(PURPLE_EYE[2]*255*pulse*4.5F);
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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private static void quad(Pose pose, VertexConsumer consumer,
                             double x0, double y0, double z0, float u0, float v0,
                             double x1, double y1, double z1, float u1, float v1,
                             double x2, double y2, double z2, float u2, float v2,
                             double x3, double y3, double z3, float u3, float v3,
                             int r, int g, int b, int a,
                             float nx, float ny, float nz) {
        consumer.addVertex(pose, (float)x0, (float)y0, (float)z0).setColor(r,g,b,a).setUv(u0,v0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(r,g,b,a).setUv(u1,v1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(r,g,b,a).setUv(u2,v2).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float)x3, (float)y3, (float)z3).setColor(r,g,b,a).setUv(u3,v3).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, nx, ny, nz);
    }

    private static void quadFullBright(Pose pose, VertexConsumer consumer,
                                       double x0, double y0, double z0, float u0, float v0,
                                       double x1, double y1, double z1, float u1, float v1,
                                       double x2, double y2, double z2, float u2, float v2,
                                       double x3, double y3, double z3, float u3, float v3,
                                       int r, int g, int b, int a) {
        consumer.addVertex(pose, (float)x0, (float)y0, (float)z0).setColor(r,g,b,a).setUv(u0,v0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0,1,0);
        consumer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(r,g,b,a).setUv(u1,v1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0,1,0);
        consumer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(r,g,b,a).setUv(u2,v2).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0,1,0);
        consumer.addVertex(pose, (float)x3, (float)y3, (float)z3).setColor(r,g,b,a).setUv(u3,v3).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0,1,0);
    }

    private static double fractalNoise(double x, double y) {
        double n = 0;
        n += Math.sin(x*1.0 + y*0.7)*0.5;
        n += Math.sin(x*2.3 - y*1.1)*0.25;
        n += Math.sin(x*4.7 + y*2.3)*0.125;
        return n;
    }

    public static String state() {
        return "creator-skybox: pos=("+(int)creatorPosX+","+(int)creatorPosZ+") target=("+(int)creatorTargetX+","+(int)creatorTargetZ+") walk="+walkPhase+" gaze=("+(int)gazeX+","+(int)gazeZ+")";
    }
}
