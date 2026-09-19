package net.mcsm.extras.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmSounds;
import net.mcsm.extras.McsmUiSounds;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.McsmVoidTiers;

/**
 * BUILD #493 / 7000.0.21-M – Procedural Void-Glitch Generator & Reality Mutation Engine
 *
 * THE MANDATE:
 * - Tier 9 as completely separate independent ninth layer, true 3D scene-graph VertexBuffer
 * - Photorealistic 3D geometry landscapes, gothic hanging castles, colossal Creator visage
 * - Now with procedural generation to warp 3D meshes, needle spires, landscapes with vertex math
 *   vectors deeper player falls.
 *
 * Phase 1 – Dynamic Mesh Displacements:
 *   Low-level vertex modulation loop inside 3D Scene-Graph pipeline feeding GameTime and FallDistance
 *   into high-frequency sine/cosine noise matrix via McsmGlitchGenerator.
 *   Each vertex pushed/pulled/warped on fly.
 *   As sink into deep negative coords, neat geometric shapes glitch into impossible organic fractal pillars,
 *   overlapping wireframes, reality-shattered cuts morphing continuously.
 *
 * Phase 1 – Reality-Glitched Chunk Loader:
 *   Background thread shuffles structure generation seeds by depth tier ensuring no two infinite falls same layout.
 *   Implemented via McsmGlitchGenerator.shuffleSeedForDepth() – globalSeed XOR baseSeed XOR tier.
 *
 * Phase 2 – Visual integration with final.fsh and Creator skybox handled in respective files.
 */
public final class McsmNinthLayerGeometry {

    public static final String TIER_ID = "mcsm:reality_glitch_nightmare";
    public static final String UNINPOSSIBLE_ID = "mcsm:uninpossible_layer";

    private static final int GOTHIC_TOWERS = 12;
    private static final int HANGING_CASTLES = 5;
    private static final int RIDGE_SEGMENTS = 32;
    private static final int CREATOR_ARMS = 7;
    private static final int CREATOR_SEGMENTS = 26;
    private static final int CASTLE_SPIRES_PER_CASTLE = 4;

    private static final double MAX_DISTANCE = 4000.0D;
    private static final double CASTLE_RADIUS = 900.0D;
    private static final double TOWER_RADIUS = 1200.0D;
    private static final double RIDGE_RADIUS = 1800.0D;
    private static final double CREATOR_DISTANCE = 2800.0D;
    private static final double CREATOR_HEAD_SIZE = 80.0D;
    private static final double CREATOR_CROWN_HEIGHT = 24.0D;

    private static final float[] MATTE_BLACK = new float[]{0.0392F, 0.0549F, 0.0784F};
    private static final float[] VOID_BLACK = new float[]{0.0F, 0.0F, 0.0F};
    private static final float[] PURPLE_LENS = new float[]{0.541F, 0.168F, 0.886F};
    private static final float[] RADIANT_PURPLE = new float[]{0.615F, 0.0F, 1.0F};
    private static final float[] CROWN_GOLD = new float[]{0.85F, 0.68F, 0.18F};
    private static final float BLOOM_FACTOR = 4.5F;
    private static final float BLOOM_WARP = 6.0F;

    private static double gazeTargetX = 0.0D;
    private static double gazeTargetZ = 0.0D;
    private static double gazeCurrentX = 0.0D;
    private static double gazeCurrentZ = 0.0D;
    private static long nextGazeShiftMs = 0L;
    private static float gazeLerp = 0.0F;

    private static long lastStompMs = 0L;
    private static int stompCount = 0;
    private static float skyboxDriftX = 0.0F;
    private static float skyboxDriftZ = 0.0F;

    private static final Identifier VOID_STONE = Identifier.fromNamespaceAndPath("mcsm", "textures/block/void_stone.png");
    private static final Identifier CREATOR_SKIN = Identifier.fromNamespaceAndPath("mcsm", "textures/entity/creator.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");
    private static final Identifier EYES_TEX = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/teeth_glow_white.png");

    private McsmNinthLayerGeometry() {}

    public static void tick() {
        try {
            if (!McsmExtrasConfig.voidDescent) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.player == null) return;
            if (!mc.level.dimension().equals(McsmVoid.DIMENSION)) return;
            double y = mc.player.getY();
            if (y > McsmVoidTiers.GEL_FLOOR) return;
            long now = System.currentTimeMillis();
            if (now >= nextGazeShiftMs) {
                gazeTargetX = (mc.level.getRandom().nextDouble() - 0.5D) * 1200.0D;
                gazeTargetZ = (mc.level.getRandom().nextDouble() - 0.5D) * 1200.0D;
                nextGazeShiftMs = now + 15000L + (long)(mc.level.getRandom().nextDouble() * 15000.0D);
                gazeLerp = 0.0F;
            }
            gazeLerp = Mth.clamp(gazeLerp + 0.008F, 0.0F, 1.0F);
            double lerp = smoothstep(gazeLerp);
            gazeCurrentX = Mth.lerp(lerp, gazeCurrentX, gazeTargetX);
            gazeCurrentZ = Mth.lerp(lerp, gazeCurrentZ, gazeTargetZ);
            skyboxDriftX += 0.0015F;
            skyboxDriftZ += 0.0009F;
            float drift = Mth.sin(skyboxDriftX * 3.0F) * 0.5F + Mth.cos(skyboxDriftZ * 2.1F) * 0.5F;
            long stompInterval = 4000L + (long)(Math.abs(drift) * 3000.0D);
            if (now - lastStompMs >= stompInterval) {
                lastStompMs = now;
                stompCount++;
                try {
                    if (mc.level != null && mc.player != null) {
                        float pitch = 0.45F + 0.15F * Mth.sin(stompCount * 0.7F);
                        float volume = 0.22F + 0.18F * Math.abs(drift);
                        if (McsmSounds.OBLIVION_DRONE != null) {
                            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                                    McsmSounds.OBLIVION_DRONE, net.minecraft.sounds.SoundSource.AMBIENT,
                                    volume, pitch, false);
                        }
                        if (stompCount % 3 == 0 && McsmSounds.MASSG_HEART != null) {
                            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                                    McsmSounds.MASSG_HEART, net.minecraft.sounds.SoundSource.AMBIENT,
                                    volume * 0.8F, pitch * 0.6F, false);
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            if (!McsmExtrasConfig.voidDescent) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || ctx == null) return;
            if (!mc.level.dimension().equals(McsmVoid.DIMENSION)) return;
            double playerY = mc.player != null ? mc.player.getY() : ctx.levelState().cameraRenderState.pos.y;
            double fallDistTmp = mc.player != null ? mc.player.fallDistance : 0.0D;
            if (playerY > McsmVoidTiers.GEL_FLOOR + 2) return;
            Vec3 camera = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            double timeTmp = (double)(mc.level.getGameTime() % 240000L) + partial;
            final float glitchFactorTmp = McsmGlitchGenerator.computeGlitchFactor(fallDistTmp, playerY, timeTmp);
            final long shuffledSeed = McsmGlitchGenerator.shuffleSeedForDepth(playerY, mc.level.dimension().identifier().hashCode() ^ (mc.player != null ? mc.player.getUUID().hashCode() : 0x5EEDL) ^ mc.level.getGameTime());
            float depthFactorTmp = 1.0F;
            if (playerY > McsmVoidTiers.ABYSSAL_NIGHTMARE_FLOOR) depthFactorTmp = 0.25F;
            else if (playerY > McsmVoidTiers.REALITY_GLITCH_NIGHTMARE_FLOOR + 1) depthFactorTmp = 0.65F;
            else depthFactorTmp = 1.0F;
            final float depthFactor = depthFactorTmp;
            final double time = timeTmp;
            final double fallDistance = fallDistTmp;
            final float glitchFactor = glitchFactorTmp;
            final Vec3 camFinal = camera;

            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(VOID_STONE),
                    (pose, consumer) -> {
                        for (int i = 0; i < GOTHIC_TOWERS; i++) {
                            emitGothicTower(pose, consumer, camFinal, i, time, depthFactor, glitchFactor, fallDistance, shuffledSeed);
                        }
                        if (glitchFactor > 0.5F) {
                            for (int i = 0; i < GOTHIC_TOWERS / 2; i++) {
                                emitFractalPillar(pose, consumer, camFinal, i, time, depthFactor, glitchFactor, fallDistance);
                            }
                        }
                    });

            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(VOID_STONE),
                    (pose, consumer) -> {
                        for (int i = 0; i < HANGING_CASTLES; i++) {
                            emitHangingFortress(pose, consumer, camFinal, i, time, depthFactor, glitchFactor, fallDistance);
                        }
                        if (glitchFactor > 0.7F) {
                            for (int i = 0; i < HANGING_CASTLES; i++) {
                                emitWireframeFortress(pose, consumer, camFinal, i, time, depthFactor, glitchFactor);
                            }
                        }
                    });

            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(VOID_STONE),
                    (pose, consumer) -> {
                        emitJaggedRidges(pose, consumer, camFinal, time, depthFactor, glitchFactor, fallDistance);
                        if (glitchFactor > 0.6F) {
                            emitRealityShatteredCuts(pose, consumer, camFinal, time, depthFactor, glitchFactor, fallDistance);
                        }
                    });

            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(CREATOR_SKIN),
                    (pose, consumer) -> {
                        emitCreatorHead(pose, consumer, camFinal, time, depthFactor, glitchFactor, fallDistance);
                        emitCreatorCrown(pose, consumer, camFinal, time, depthFactor, glitchFactor);
                        for (int arm = 0; arm < CREATOR_ARMS; arm++) {
                            emitCreatorArm(pose, consumer, camFinal, arm, time, depthFactor, glitchFactor, fallDistance);
                        }
                    });

            collector.submitCustomGeometry(poseStack, RenderTypes.eyes(EYES_TEX),
                    (pose, consumer) -> {
                        emitCreatorEyes(pose, consumer, camFinal, time, depthFactor, glitchFactor);
                    });

            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.glow(WHITE),
                    (pose, consumer) -> {
                        emitRadiantAura(pose, consumer, camFinal, time, depthFactor, glitchFactor);
                        if (McsmGlitchGenerator.isMajorWarp(glitchFactor)) {
                            emitGlitchParticles(pose, consumer, camFinal, time, glitchFactor);
                        }
                    });

            if (glitchFactor > 0.75F) {
                collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.glow(WHITE),
                        (pose, consumer) -> {
                            emitGlitchScanlineOverlay(pose, consumer, camFinal, time, glitchFactor, fallDistance);
                        });
            }

        } catch (Throwable ignored) {}
    }

    private static void emitGothicTower(Pose pose, VertexConsumer consumer, Vec3 camera, int index, double time, float visibility, float glitchFactor, double fallDistance, long shuffledSeed) {
        double angle = (index / (double) GOTHIC_TOWERS) * Math.PI * 2.0D + time * 0.0003D * (index % 3 + 1);
        angle += ((shuffledSeed >> (index * 2)) & 0xFF) * 0.001 * glitchFactor;
        double radius = TOWER_RADIUS + Math.sin(time * 0.001D + index * 1.7D) * 80.0D;
        double x = camera.x + Math.cos(angle) * radius;
        double z = camera.z + Math.sin(angle) * radius;
        double baseY = McsmVoidTiers.FLOOR_Y + 12.0D + (index % 4) * 8.0D;
        double[] mutated = McsmGlitchGenerator.mutateSpire(120.0D + (index * 13 % 90), 14.0D, time, baseY, index);
        double height = mutated[0] + Math.sin(time * 0.002D + index) * 10.0D * (1.0 + glitchFactor);
        double baseRadius = mutated[1];

        int segments = 12;
        for (int s = 0; s < segments; s++) {
            double a0 = (s / (double) segments) * Math.PI * 2.0D;
            double a1 = ((s + 1) / (double) segments) * Math.PI * 2.0D;
            double r0 = baseRadius - (s % 2) * 2.0D + fractalNoise(a0 * 2.0, time * 0.0001) * 1.5;
            double r1 = 3.0D;

            double x0b = x + Math.cos(a0) * r0;
            double z0b = z + Math.sin(a0) * r0;
            double x1b = x + Math.cos(a1) * r0;
            double z1b = z + Math.sin(a1) * r0;
            double x0t = x + Math.cos(a0) * r1;
            double z0t = z + Math.sin(a0) * r1;
            double x1t = x + Math.cos(a1) * r1;
            double z1t = z + Math.sin(a1) * r1;

            // Dynamic mesh displacement – push/pull/warp vertices on fly
            double[] d0b = McsmGlitchGenerator.displaceVertex(x0b, baseY, z0b, time, fallDistance, glitchFactor, s + index * 10);
            double[] d1b = McsmGlitchGenerator.displaceVertex(x1b, baseY, z1b, time, fallDistance, glitchFactor, s+1 + index * 10);
            double[] d0t = McsmGlitchGenerator.displaceVertex(x0t, baseY + height, z0t, time, fallDistance, glitchFactor, s + index * 20);
            double[] d1t = McsmGlitchGenerator.displaceVertex(x1t, baseY + height, z1t, time, fallDistance, glitchFactor, s+1 + index * 20);

            float shade = 0.08F + 0.04F * (float)Math.sin(a0 * 2.0D) + (float)fractalNoise(a0, time * 0.0002) * 0.03F;
            float ao = 0.85F + 0.15F * (float)Math.sin(s * 1.5);
            int r = (int)((MATTE_BLACK[0] + shade) * 255 * ao);
            int g = (int)((MATTE_BLACK[1] + shade) * 255 * ao);
            int b = (int)((MATTE_BLACK[2] + shade) * 255 * ao);
            int a = (int)(visibility * 220);

            quad(pose, consumer,
                    d0b[0], d0b[1], d0b[2], 0, 0,
                    d1b[0], d1b[1], d1b[2], 1, 0,
                    d1t[0], d1t[1], d1t[2], 1, 1,
                    d0t[0], d0t[1], d0t[2], 0, 1,
                    r, g, b, a, 0, 1, 0);

            if (s % 2 == 0) {
                double buttressOut = r0 + 8.0D;
                double bx0 = x + Math.cos(a0) * buttressOut;
                double bz0 = z + Math.sin(a0) * buttressOut;
                double by = baseY + height * 0.6D;
                double[] dbx = McsmGlitchGenerator.displaceVertex(bx0, by, bz0, time, fallDistance, glitchFactor, s + index * 30);
                quad(pose, consumer,
                        d0b[0], baseY + height * 0.3D, d0b[2], 0, 0,
                        dbx[0], dbx[1], dbx[2], 1, 0,
                        dbx[0], dbx[1] + 4, dbx[2], 1, 1,
                        d0b[0], baseY + height * 0.3D + 4, d0b[2], 0, 1,
                        r, g, b, a, 0, 1, 0);
            }

            if (s % 3 == 0 && index % 2 == 0) {
                double winY = baseY + height * (0.3 + (s % 4) * 0.15);
                double winX = x + Math.cos(a0) * (r0 + 0.2);
                double winZ = z + Math.sin(a0) * (r0 + 0.2);
                double winSize = 2.5 + glitchFactor * 1.0;
                int wr = 255, wg = 200, wb = 100;
                int wa = (int)(visibility * 180 * (0.7 + 0.3 * Math.sin(time * 0.005 + index + s)));
                quadFullBright(pose, consumer,
                        winX - winSize, winY - winSize, winZ, 0, 0,
                        winX + winSize, winY - winSize, winZ, 1, 0,
                        winX + winSize, winY + winSize, winZ, 1, 1,
                        winX - winSize, winY + winSize, winZ, 0, 1,
                        wr, wg, wb, wa);
            }
        }

        double tipY = baseY + height;
        for (int s = 0; s < segments; s++) {
            double a0 = (s / (double) segments) * Math.PI * 2.0D;
            double a1 = ((s + 1) / (double) segments) * Math.PI * 2.0D;
            double x0 = x + Math.cos(a0) * 3.0D;
            double z0 = z + Math.sin(a0) * 3.0D;
            double x1 = x + Math.cos(a1) * 3.0D;
            double z1 = z + Math.sin(a1) * 3.0D;
            float metalShine = 0.9F + 0.1F * (float)Math.sin(time * 0.01 + s) + glitchFactor * 0.2F;
            double[] d0 = McsmGlitchGenerator.displaceVertex(x0, tipY, z0, time, fallDistance, glitchFactor, s + index * 40);
            double[] d1 = McsmGlitchGenerator.displaceVertex(x1, tipY, z1, time, fallDistance, glitchFactor, s+1 + index * 40);
            double[] dt = McsmGlitchGenerator.displaceVertex(x, tipY + 18.0D, z, time, fallDistance, glitchFactor, index * 50);
            quad(pose, consumer,
                    d0[0], d0[1], d0[2], 0, 0,
                    d1[0], d1[1], d1[2], 1, 0,
                    dt[0], dt[1], dt[2], 0.5F, 1,
                    dt[0], dt[1], dt[2], 0.5F, 1,
                    (int)(20 * metalShine), (int)(24 * metalShine), (int)(38 * metalShine), (int)(visibility * 255), 0, 1, 0);
        }
    }

    // ---- NEW: Fractal pillar – impossible organic fractal when glitch high ----
    private static void emitFractalPillar(Pose pose, VertexConsumer consumer, Vec3 camera, int index, double time, float visibility, float glitchFactor, double fallDistance) {
        double angle = (index / (double) (GOTHIC_TOWERS/2)) * Math.PI * 2.0D + time * 0.0005D * (index+1) + glitchFactor * 0.5;
        double radius = TOWER_RADIUS * 0.7 + index * 60.0 + McsmGlitchGenerator.fractalNoise(index * 2.0, time * 0.001) * glitchFactor * 100.0;
        double x = camera.x + Math.cos(angle) * radius;
        double z = camera.z + Math.sin(angle) * radius;
        double baseY = McsmVoidTiers.FLOOR_Y + 8.0D;
        double height = 80.0D + McsmGlitchGenerator.fractalNoise(index * 1.5, time * 0.002) * glitchFactor * 120.0D;

        int segs = 8;
        for (int s = 0; s < segs; s++) {
            double a0 = (s / (double) segs) * Math.PI * 2.0D + time * 0.001 * glitchFactor;
            double a1 = ((s + 1) / (double) segs) * Math.PI * 2.0D + time * 0.001 * glitchFactor;
            double r = 6.0D + McsmGlitchGenerator.fractalNoise(a0 * 3.0, time * 0.002) * glitchFactor * 10.0;

            double x0b = x + Math.cos(a0) * r;
            double z0b = z + Math.sin(a0) * r;
            double x1b = x + Math.cos(a1) * r;
            double z1b = z + Math.sin(a1) * r;
            double x0t = x + Math.cos(a0) * (r * 0.3);
            double z0t = z + Math.sin(a0) * (r * 0.3);
            double x1t = x + Math.cos(a1) * (r * 0.3);
            double z1t = z + Math.sin(a1) * (r * 0.3);

            double[] d0b = McsmGlitchGenerator.displaceVertex(x0b, baseY, z0b, time, fallDistance, glitchFactor, s + index * 100);
            double[] d1b = McsmGlitchGenerator.displaceVertex(x1b, baseY, z1b, time, fallDistance, glitchFactor, s+1 + index * 100);
            double[] d0t = McsmGlitchGenerator.displaceVertex(x0t, baseY + height, z0t, time, fallDistance, glitchFactor, s + index * 110);
            double[] d1t = McsmGlitchGenerator.displaceVertex(x1t, baseY + height, z1t, time, fallDistance, glitchFactor, s+1 + index * 110);

            int col = (int)(60 + glitchFactor * 80);
            quad(pose, consumer,
                    d0b[0], d0b[1], d0b[2], 0, 0,
                    d1b[0], d1b[1], d1b[2], 1, 0,
                    d1t[0], d1t[1], d1t[2], 1, 1,
                    d0t[0], d0t[1], d0t[2], 0, 1,
                    col, col/2, 120 + col/2, (int)(visibility * 150 * glitchFactor), 0, 1, 0);
        }
    }

    private static void emitHangingFortress(Pose pose, VertexConsumer consumer, Vec3 camera, int index, double time, float visibility, float glitchFactor, double fallDistance) {
        double angle = (index / (double) HANGING_CASTLES) * Math.PI * 2.0D + time * 0.00015D + index * 1.2D;
        double radius = CASTLE_RADIUS + index * 45.0D + glitchFactor * 20.0 * Math.sin(time * 0.001 + index);
        double x = camera.x + Math.cos(angle) * radius;
        double z = camera.z + Math.sin(angle) * radius;
        double y = McsmVoidTiers.FLOOR_Y + 85.0D + Math.sin(time * 0.001D + index * 2.3D) * 12.0D;

        double size = 40.0D + index * 6.0D + glitchFactor * 10.0;
        for (int s = 0; s < 4; s++) {
            double a0 = (s / 4.0D) * Math.PI * 2.0D;
            double a1 = ((s + 1) / 4.0D) * Math.PI * 2.0D;
            double x0 = x + Math.cos(a0) * size;
            double z0 = z + Math.sin(a0) * size;
            double x1 = x + Math.cos(a1) * size;
            double z1 = z + Math.sin(a1) * size;
            double[] d0 = McsmGlitchGenerator.displaceVertex(x0, y, z0, time, fallDistance, glitchFactor, s + index * 200);
            double[] d1 = McsmGlitchGenerator.displaceVertex(x1, y, z1, time, fallDistance, glitchFactor, s+1 + index * 200);
            double[] d0b = McsmGlitchGenerator.displaceVertex(x0, y - 18.0D, z0, time, fallDistance, glitchFactor, s + index * 210);
            double[] d1b = McsmGlitchGenerator.displaceVertex(x1, y - 18.0D, z1, time, fallDistance, glitchFactor, s+1 + index * 210);
            int r = 12, g = 18, b = 32;
            int a = (int)(visibility * 200);
            quad(pose, consumer,
                    d0[0], d0[1], d0[2], 0, 0,
                    d1[0], d1[1], d1[2], 1, 0,
                    d1b[0], d1b[1], d1b[2], 1, 1,
                    d0b[0], d0b[1], d0b[2], 0, 1,
                    r, g, b, a, 0, -1, 0);
        }

        for (int spire = 0; spire < CASTLE_SPIRES_PER_CASTLE; spire++) {
            double sa = (spire / (double) CASTLE_SPIRES_PER_CASTLE) * Math.PI * 2.0D;
            double sx = x + Math.cos(sa) * (size * 0.6D);
            double sz = z + Math.sin(sa) * (size * 0.6D);
            double sy = y - 18.0D;
            double sh = 28.0D + spire * 4.0D + glitchFactor * 15.0 * Math.sin(time * 0.002 + spire);
            double[] dBase0 = McsmGlitchGenerator.displaceVertex(sx - 3, sy, sz - 3, time, fallDistance, glitchFactor, spire + index * 300);
            double[] dBase1 = McsmGlitchGenerator.displaceVertex(sx + 3, sy, sz - 3, time, fallDistance, glitchFactor, spire + index * 301);
            double[] dTop0 = McsmGlitchGenerator.displaceVertex(sx + 3, sy - sh, sz + 3, time, fallDistance, glitchFactor, spire + index * 302);
            double[] dTop1 = McsmGlitchGenerator.displaceVertex(sx - 3, sy - sh, sz + 3, time, fallDistance, glitchFactor, spire + index * 303);
            quad(pose, consumer,
                    dBase0[0], dBase0[1], dBase0[2], 0, 0,
                    dBase1[0], dBase1[1], dBase1[2], 1, 0,
                    dTop0[0], dTop0[1], dTop0[2], 1, 1,
                    dTop1[0], dTop1[1], dTop1[2], 0, 1,
                    18, 24, 42, (int)(visibility * 220), 0, -1, 0);
        }

        for (int c = 0; c < 3; c++) {
            double ca = (c / 3.0D) * Math.PI * 2.0D + time * 0.002D;
            double cx = x + Math.cos(ca) * (size * 0.3D);
            double cz = z + Math.sin(ca) * (size * 0.3D);
            for (int link = 0; link < 8; link++) {
                double ly = y + link * 6.0D + Math.sin(time * 0.01D + c + link) * 1.5D + glitchFactor * Math.sin(time * 0.005 + link) * 4.0;
                double[] d = McsmGlitchGenerator.displaceVertex(cx, ly, cz, time, fallDistance, glitchFactor, link + c * 10 + index * 400);
                quad(pose, consumer,
                        d[0] - 1, d[1], d[2] - 1, 0, 0,
                        d[0] + 1, d[1], d[2] - 1, 1, 0,
                        d[0] + 1, d[1] + 4, d[2] + 1, 1, 1,
                        d[0] - 1, d[1] + 4, d[2] + 1, 0, 1,
                        30, 30, 35, (int)(visibility * 160), 0, 1, 0);
            }
        }
    }

    // ---- NEW: Overlapping wireframes – duplicate vertices with offset when glitch high ----
    private static void emitWireframeFortress(Pose pose, VertexConsumer consumer, Vec3 camera, int index, double time, float visibility, float glitchFactor) {
        double angle = (index / (double) HANGING_CASTLES) * Math.PI * 2.0D + time * 0.00015D + index * 1.2D + glitchFactor;
        double radius = CASTLE_RADIUS + index * 45.0D;
        double x = camera.x + Math.cos(angle) * radius;
        double z = camera.z + Math.sin(angle) * radius;
        double y = McsmVoidTiers.FLOOR_Y + 85.0D;
        double size = 40.0D + index * 6.0D;

        for (int s = 0; s < 4; s++) {
            double a0 = (s / 4.0D) * Math.PI * 2.0D;
            double a1 = ((s + 1) / 4.0D) * Math.PI * 2.0D;
            double x0 = x + Math.cos(a0) * size + Math.sin(time * 0.01 + s) * glitchFactor * 5.0;
            double z0 = z + Math.sin(a0) * size + Math.cos(time * 0.01 + s) * glitchFactor * 5.0;
            double x1 = x + Math.cos(a1) * size + Math.sin(time * 0.01 + s + 1) * glitchFactor * 5.0;
            double z1 = z + Math.sin(a1) * size + Math.cos(time * 0.01 + s + 1) * glitchFactor * 5.0;

            int r = 120 + (int)(glitchFactor * 80);
            int g = 60;
            int b = 255;
            int a = (int)(visibility * 80 * glitchFactor);

            quadFullBright(pose, consumer,
                    x0, y, z0, 0, 0,
                    x1, y, z1, 1, 0,
                    x1, y - 18.0D, z1, 1, 1,
                    x0, y - 18.0D, z0, 0, 1,
                    r, g, b, a);
        }
    }

    private static void emitJaggedRidges(Pose pose, VertexConsumer consumer, Vec3 camera, double time, float visibility, float glitchFactor, double fallDistance) {
        double baseY = McsmVoidTiers.FLOOR_Y + 4.0D;
        for (int i = 0; i < RIDGE_SEGMENTS; i++) {
            double a0 = (i / (double) RIDGE_SEGMENTS) * Math.PI * 2.0D;
            double a1 = ((i + 1) / (double) RIDGE_SEGMENTS) * Math.PI * 2.0D;
            double r0 = RIDGE_RADIUS + fractalNoise(a0 * 3.0D, time * 0.0005D) * 120.0D + glitchFactor * McsmGlitchGenerator.fractalNoise(a0 * 5.0, time * 0.001) * 80.0;
            double r1 = RIDGE_RADIUS + fractalNoise(a1 * 3.0D, time * 0.0005D) * 120.0D + glitchFactor * McsmGlitchGenerator.fractalNoise(a1 * 5.0, time * 0.001) * 80.0;
            double h0 = 45.0D + fractalNoise(a0 * 5.0D, time * 0.0003D) * 60.0D + glitchFactor * 30.0 * Math.sin(time * 0.001 + i);
            double h1 = 45.0D + fractalNoise(a1 * 5.0D, time * 0.0003D) * 60.0D + glitchFactor * 30.0 * Math.sin(time * 0.001 + i + 1);

            double x0 = camera.x + Math.cos(a0) * r0;
            double z0 = camera.z + Math.sin(a0) * r0;
            double x1 = camera.x + Math.cos(a1) * r1;
            double z1 = camera.z + Math.sin(a1) * r1;

            double[] d0 = McsmGlitchGenerator.displaceVertex(x0, baseY, z0, time, fallDistance, glitchFactor, i * 2);
            double[] d1 = McsmGlitchGenerator.displaceVertex(x1, baseY, z1, time, fallDistance, glitchFactor, i * 2 + 1);
            double[] d0h = McsmGlitchGenerator.displaceVertex(x0, baseY + h0, z0, time, fallDistance, glitchFactor, i * 2 + 100);
            double[] d1h = McsmGlitchGenerator.displaceVertex(x1, baseY + h1, z1, time, fallDistance, glitchFactor, i * 2 + 101);

            int r = 8, g = 12, b = 22;
            int a = (int)(visibility * 180);
            quad(pose, consumer,
                    d0[0], d0[1], d0[2], 0, 0,
                    d1[0], d1[1], d1[2], 1, 0,
                    d1h[0], d1h[1], d1h[2], 1, 1,
                    d0h[0], d0h[1], d0h[2], 0, 1,
                    r, g, b, a, 0, 1, 0);

            double mx = (x0 + x1) * 0.5D;
            double mz = (z0 + z1) * 0.5D;
            double mh = Math.max(h0, h1) + 18.0D + fractalNoise(a0 * 7.0D, time * 0.0007D) * 20.0D + glitchFactor * 20.0;
            double[] dm = McsmGlitchGenerator.displaceVertex(mx, baseY + mh, mz, time, fallDistance, glitchFactor, i * 2 + 200);
            quad(pose, consumer,
                    d0h[0], d0h[1], d0h[2], 0, 0,
                    d1h[0], d1h[1], d1h[2], 1, 0,
                    dm[0], dm[1], dm[2], 0.5F, 1,
                    dm[0], dm[1], dm[2], 0.5F, 1,
                    r + 4, g + 4, b + 8, a, 0, 1, 0);
        }
    }

    // ---- NEW: Reality-shattered cuts – sudden jumps, tearing geometry ----
    private static void emitRealityShatteredCuts(Pose pose, VertexConsumer consumer, Vec3 camera, double time, float visibility, float glitchFactor, double fallDistance) {
        int cuts = (int)(glitchFactor * 8) + 2;
        for (int i = 0; i < cuts; i++) {
            double angle = (i / (double) cuts) * Math.PI * 2.0D + time * 0.0002 * (i+1) + McsmGlitchGenerator.fractalNoise(i * 1.3, time * 0.001) * glitchFactor;
            double radius = RIDGE_RADIUS * 0.6 + i * 80.0 + Mth.sin((float)(time * 0.001 + i)) * glitchFactor * 100.0;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = McsmVoidTiers.FLOOR_Y + 10.0 + Math.sin(time * 0.002 + i * 1.5) * 20.0 * glitchFactor;
            double len = 30.0 + glitchFactor * 60.0 + Math.random() * 20.0;
            double thick = 2.0 + glitchFactor * 5.0;

            double[] p0 = McsmGlitchGenerator.displaceVertex(x - thick, y, z - thick, time, fallDistance, glitchFactor, i * 500);
            double[] p1 = McsmGlitchGenerator.displaceVertex(x + thick, y, z - thick, time, fallDistance, glitchFactor, i * 501);
            double[] p2 = McsmGlitchGenerator.displaceVertex(x + thick + len * 0.5, y + len, z + thick + len * 0.3, time, fallDistance, glitchFactor, i * 502);
            double[] p3 = McsmGlitchGenerator.displaceVertex(x - thick + len * 0.5, y + len, z - thick + len * 0.3, time, fallDistance, glitchFactor, i * 503);

            int r = 200 + (int)(glitchFactor * 55);
            int g = 100 + (int)(glitchFactor * 100);
            int b = 255;
            int a = (int)(visibility * 120 * glitchFactor);

            quadFullBright(pose, consumer,
                    p0[0], p0[1], p0[2], 0, 0,
                    p1[0], p1[1], p1[2], 1, 0,
                    p2[0], p2[1], p2[2], 1, 1,
                    p3[0], p3[1], p3[2], 0, 1,
                    r, g, b, a);
        }
    }

    private static void emitCreatorHead(Pose pose, VertexConsumer consumer, Vec3 camera, double time, float visibility, float glitchFactor, double fallDistance) {
        double angle = time * 0.00005D + glitchFactor * 0.001 * Math.sin(time * 0.0001);
        double x = camera.x + Math.cos(angle) * CREATOR_DISTANCE * 0.2D;
        double z = camera.z + Math.sin(angle) * CREATOR_DISTANCE * 0.2D;
        double y = McsmVoidTiers.FLOOR_Y + 180.0D + Math.sin(time * 0.0004D) * 12.0D + glitchFactor * Math.sin(time * 0.002) * 10.0;

        double lookX = x + gazeCurrentX * 0.15D;
        double lookZ = z + gazeCurrentZ * 0.15D;
        double size = CREATOR_HEAD_SIZE + glitchFactor * 10.0 * Math.sin(time * 0.001);

        float pulse = 0.85F + 0.15F * (float)Math.sin(time * 0.002D) + glitchFactor * 0.1F;
        int r = (int)(18 * pulse), g = (int)(22 * pulse), b = (int)(38 * pulse);
        int a = (int)(visibility * 255);

        double[] d0 = McsmGlitchGenerator.displaceVertex(x - size * 0.6D, y - size * 0.3D, z + size * 0.5D, time, fallDistance, glitchFactor, 1000);
        double[] d1 = McsmGlitchGenerator.displaceVertex(x + size * 0.6D, y - size * 0.3D, z + size * 0.5D, time, fallDistance, glitchFactor, 1001);
        double[] d2 = McsmGlitchGenerator.displaceVertex(x + size * 0.6D, y + size * 0.7D, z + size * 0.5D, time, fallDistance, glitchFactor, 1002);
        double[] d3 = McsmGlitchGenerator.displaceVertex(x - size * 0.6D, y + size * 0.7D, z + size * 0.5D, time, fallDistance, glitchFactor, 1003);

        quad(pose, consumer,
                d0[0], d0[1], d0[2], 0, 0,
                d1[0], d1[1], d1[2], 1, 0,
                d2[0], d2[1], d2[2], 1, 1,
                d3[0], d3[1], d3[2], 0, 1,
                r, g, b, a, 0, 0, 1);

        double backScale = 1.4D + glitchFactor * 0.2;
        double[] db0 = McsmGlitchGenerator.displaceVertex(x - size * 0.6D * backScale, y - size * 0.3D, z - size * 0.5D, time, fallDistance, glitchFactor, 1010);
        double[] db1 = McsmGlitchGenerator.displaceVertex(x + size * 0.6D * backScale, y - size * 0.3D, z - size * 0.5D, time, fallDistance, glitchFactor, 1011);
        double[] db2 = McsmGlitchGenerator.displaceVertex(x + size * 0.6D * backScale, y + size * 0.7D * backScale, z - size * 0.5D, time, fallDistance, glitchFactor, 1012);
        double[] db3 = McsmGlitchGenerator.displaceVertex(x - size * 0.6D * backScale, y + size * 0.7D * backScale, z - size * 0.5D, time, fallDistance, glitchFactor, 1013);
        quad(pose, consumer,
                db0[0], db0[1], db0[2], 0, 0,
                db1[0], db1[1], db1[2], 1, 0,
                db2[0], db2[1], db2[2], 1, 1,
                db3[0], db3[1], db3[2], 0, 1,
                r - 4, g - 4, b - 4, (int)(a * 0.8F), 0, 0, -1);

        double tear = Math.sin(time * 0.003D) * 4.0D + glitchFactor * 8.0 * Math.sin(time * 0.01);
        double[] ds0 = McsmGlitchGenerator.displaceVertex(x - size * 0.6D, y - size * 0.3D, z - size * 0.5D, time, fallDistance, glitchFactor, 1020);
        double[] ds1 = McsmGlitchGenerator.displaceVertex(x - size * 0.6D, y - size * 0.3D, z + size * 0.5D, time, fallDistance, glitchFactor, 1021);
        double[] ds2 = McsmGlitchGenerator.displaceVertex(x - size * 0.6D + tear, y + size * 0.7D, z + size * 0.5D, time, fallDistance, glitchFactor, 1022);
        double[] ds3 = McsmGlitchGenerator.displaceVertex(x - size * 0.6D - tear, y + size * 0.7D, z - size * 0.5D, time, fallDistance, glitchFactor, 1023);
        quad(pose, consumer,
                ds0[0], ds0[1], ds0[2], 0, 0,
                ds1[0], ds1[1], ds1[2], 1, 0,
                ds2[0], ds2[1], ds2[2], 1, 1,
                ds3[0], ds3[1], ds3[2], 0, 1,
                r, g, b, a, -1, 0, 0);

        double[] ds4 = McsmGlitchGenerator.displaceVertex(x + size * 0.6D, y - size * 0.3D, z + size * 0.5D, time, fallDistance, glitchFactor, 1030);
        double[] ds5 = McsmGlitchGenerator.displaceVertex(x + size * 0.6D, y - size * 0.3D, z - size * 0.5D, time, fallDistance, glitchFactor, 1031);
        double[] ds6 = McsmGlitchGenerator.displaceVertex(x + size * 0.6D - tear, y + size * 0.7D, z - size * 0.5D, time, fallDistance, glitchFactor, 1032);
        double[] ds7 = McsmGlitchGenerator.displaceVertex(x + size * 0.6D + tear, y + size * 0.7D, z + size * 0.5D, time, fallDistance, glitchFactor, 1033);
        quad(pose, consumer,
                ds4[0], ds4[1], ds4[2], 0, 0,
                ds5[0], ds5[1], ds5[2], 1, 0,
                ds6[0], ds6[1], ds6[2], 1, 1,
                ds7[0], ds7[1], ds7[2], 0, 1,
                r, g, b, a, 1, 0, 0);
    }

    private static void emitCreatorCrown(Pose pose, VertexConsumer consumer, Vec3 camera, double time, float visibility, float glitchFactor) {
        double angle = time * 0.00005D;
        double x = camera.x + Math.cos(angle) * CREATOR_DISTANCE * 0.2D;
        double z = camera.z + Math.sin(angle) * CREATOR_DISTANCE * 0.2D;
        double y = McsmVoidTiers.FLOOR_Y + 180.0D + CREATOR_HEAD_SIZE * 0.7D + Math.sin(time * 0.0004D) * 12.0D;
        double size = CREATOR_HEAD_SIZE * 0.6D;
        int points = 7;
        for (int i = 0; i < points; i++) {
            double a0 = (i / (double) points) * Math.PI * 2.0D;
            double a1 = ((i + 1) / (double) points) * Math.PI * 2.0D;
            double r0 = size * 0.7D + glitchFactor * Math.sin(time * 0.002 + i) * 3.0;
            double r1 = size * 0.5D;
            double x0 = x + Math.cos(a0) * r0;
            double z0 = z + Math.sin(a0) * r0;
            double x1 = x + Math.cos(a1) * r0;
            double z1 = z + Math.sin(a1) * r0;
            double xt = x + Math.cos((a0 + a1) * 0.5D) * r1;
            double zt = z + Math.sin((a0 + a1) * 0.5D) * r1;
            double yt = y + CREATOR_CROWN_HEIGHT + Math.sin(time * 0.002D + i) * 2.0D + glitchFactor * Math.sin(time * 0.005 + i) * 5.0;

            int cr = (int)(CROWN_GOLD[0] * 255);
            int cg = (int)(CROWN_GOLD[1] * 255);
            int cb = (int)(CROWN_GOLD[2] * 255);
            int a = (int)(visibility * 255);

            quad(pose, consumer,
                    x0, y, z0, 0, 0,
                    x1, y, z1, 1, 0,
                    xt, yt, zt, 0.5F, 1,
                    xt, yt, zt, 0.5F, 1,
                    cr, cg, cb, a, 0, 1, 0);
        }
    }

    private static void emitCreatorArm(Pose pose, VertexConsumer consumer, Vec3 camera, int armIndex, double time, float visibility, float glitchFactor, double fallDistance) {
        double baseAngle = (armIndex / (double) CREATOR_ARMS) * Math.PI * 2.0D + time * 0.0002D + glitchFactor * 0.2 * Math.sin(time * 0.001 + armIndex);
        double creatorX = camera.x + Math.cos(time * 0.00005D) * CREATOR_DISTANCE * 0.2D;
        double creatorZ = camera.z + Math.sin(time * 0.00005D) * CREATOR_DISTANCE * 0.2D;
        double creatorY = McsmVoidTiers.FLOOR_Y + 180.0D;

        double mouthRadius = CREATOR_HEAD_SIZE * 0.9D;
        double mouthX = creatorX + Math.cos(baseAngle) * mouthRadius;
        double mouthZ = creatorZ + Math.sin(baseAngle) * mouthRadius;
        double mouthY = creatorY + 10.0D;

        double hang = 260.0D + armIndex * 22.0D + glitchFactor * 40.0;
        double reach = 320.0D + Math.sin(time * 0.001D + armIndex) * 30.0D + glitchFactor * 50.0;

        double thick0 = 12.0D + armIndex * 1.5D;

        for (int seg = 0; seg < CREATOR_SEGMENTS; seg++) {
            double t = seg / (double) CREATOR_SEGMENTS;
            double thick = thick0 * (1.0D - t * 0.85D) * (1.0 + glitchFactor * 0.3);
            double sway = Math.sin(time * 0.002D + armIndex * 1.3D + seg * 0.4D) * 12.0D + glitchFactor * Math.sin(time * 0.005 + seg) * 15.0;
            double x = mouthX + Math.cos(baseAngle + sway * 0.05D) * (t * reach);
            double y = mouthY - t * hang + Math.sin(time * 0.001D + armIndex + seg * 0.2D) * 6.0D;
            double z = mouthZ + Math.sin(baseAngle + sway * 0.05D) * (t * reach);

            double[] d = McsmGlitchGenerator.displaceVertex(x, y, z, time, fallDistance, glitchFactor, seg + armIndex * 1000);

            float[] col = seg > CREATOR_SEGMENTS - 4 ? PURPLE_LENS : MATTE_BLACK;
            int r = (int)(col[0] * 255);
            int g = (int)(col[1] * 255);
            int b = (int)(col[2] * 255);
            int a = (int)(visibility * (180 - t * 80));

            double half = thick * 0.5D;
            quad(pose, consumer,
                    d[0] - half, d[1] - half, d[2], 0, 0,
                    d[0] + half, d[1] - half, d[2], 1, 0,
                    d[0] + half, d[1] + half, d[2], 1, 1,
                    d[0] - half, d[1] + half, d[2], 0, 1,
                    r, g, b, a, 0, 1, 0);
        }
    }

    private static void emitCreatorEyes(Pose pose, VertexConsumer consumer, Vec3 camera, double time, float visibility, float glitchFactor) {
        double creatorX = camera.x + Math.cos(time * 0.00005D) * CREATOR_DISTANCE * 0.2D + gazeCurrentX * 0.15D;
        double creatorZ = camera.z + Math.sin(time * 0.00005D) * CREATOR_DISTANCE * 0.2D + gazeCurrentZ * 0.15D;
        double creatorY = McsmVoidTiers.FLOOR_Y + 180.0D + 12.0D;

        double eyeDist = 18.0D + glitchFactor * 5.0 * Math.sin(time * 0.005);
        double eyeY = creatorY + 8.0D;

        float bloom = McsmGlitchGenerator.computeEyeBloom(BLOOM_FACTOR, glitchFactor);

        for (int eye = 0; eye < 2; eye++) {
            double offset = (eye == 0 ? -1 : 1) * eyeDist;
            double x = creatorX + offset;
            double y = eyeY;
            double z = creatorZ + CREATOR_HEAD_SIZE * 0.5D + 2.0D;

            double gazeYaw = Math.atan2(gazeCurrentZ, gazeCurrentX);
            double lookX = Math.cos(gazeYaw) * 2.0D + glitchFactor * Math.sin(time * 0.01 + eye) * 3.0;
            double lookZ = Math.sin(gazeYaw) * 2.0D + glitchFactor * Math.cos(time * 0.01 + eye) * 3.0;

            double size = 6.0D + Math.sin(time * 0.01D + eye) * 0.8D + glitchFactor * 3.0;
            float pulse = 0.75F + 0.25F * (float)Math.sin(time * 0.008D + eye * 1.5D) + glitchFactor * 0.3F;

            int r = (int)(PURPLE_LENS[0] * 255 * pulse * bloom);
            int g = (int)(PURPLE_LENS[1] * 255 * pulse * bloom);
            int b = (int)(PURPLE_LENS[2] * 255 * pulse * bloom);
            r = Math.min(255, r);
            g = Math.min(255, g);
            b = Math.min(255, b);
            int a = (int)(visibility * 255);

            quadFullBright(pose, consumer,
                    x - size + lookX, y - size, z + lookZ, 0, 0,
                    x + size + lookX, y - size, z + lookZ, 1, 0,
                    x + size + lookX, y + size, z + lookZ, 1, 1,
                    x - size + lookX, y + size, z + lookZ, 0, 1,
                    r, g, b, a);

            double pupilSize = size * 0.45D;
            quadFullBright(pose, consumer,
                    x - pupilSize + lookX, y - pupilSize, z + lookZ + 0.1D, 0, 0,
                    x + pupilSize + lookX, y - pupilSize, z + lookZ + 0.1D, 1, 0,
                    x + pupilSize + lookX, y + pupilSize, z + lookZ + 0.1D, 1, 1,
                    x - pupilSize + lookX, y + pupilSize, z + lookZ + 0.1D, 0, 1,
                    20, 0, 40, a);
        }
    }

    private static void emitRadiantAura(Pose pose, VertexConsumer consumer, Vec3 camera, double time, float visibility, float glitchFactor) {
        double creatorX = camera.x + Math.cos(time * 0.00005D) * CREATOR_DISTANCE * 0.2D;
        double creatorZ = camera.z + Math.sin(time * 0.00005D) * CREATOR_DISTANCE * 0.2D;
        double creatorY = McsmVoidTiers.FLOOR_Y + 180.0D;

        float bloom = McsmGlitchGenerator.computeEyeBloom(BLOOM_FACTOR, glitchFactor);

        int rings = 6;
        for (int i = 0; i < rings; i++) {
            double radius = CREATOR_HEAD_SIZE * (0.9D + i * 0.35D) + glitchFactor * 15.0 * Math.sin(time * 0.002 + i);
            double y = creatorY + Math.sin(time * 0.001D + i) * 3.0D + glitchFactor * Math.sin(time * 0.005 + i) * 8.0;
            float hue = (float)((time * 0.0005D + i * 0.15D) % 1.0D);
            float[] rgb = hsvToRgb(hue, 0.85F, 1.0F);
            float mix = 0.6F;
            int r = (int)((rgb[0] * (1 - mix) + RADIANT_PURPLE[0] * mix) * 255 * bloom);
            int g = (int)((rgb[1] * (1 - mix) + RADIANT_PURPLE[1] * mix) * 255 * bloom);
            int b = (int)((rgb[2] * (1 - mix) + RADIANT_PURPLE[2] * mix) * 255 * bloom);
            r = Math.min(255, r);
            g = Math.min(255, g);
            b = Math.min(255, b);
            int a = (int)(visibility * (40 - i * 4) * (1.0 + glitchFactor * 0.5));

            int segs = 24;
            for (int s = 0; s < segs; s++) {
                double a0 = (s / (double) segs) * Math.PI * 2.0D;
                double a1 = ((s + 1) / (double) segs) * Math.PI * 2.0D;
                double x0 = creatorX + Math.cos(a0) * radius;
                double z0 = creatorZ + Math.sin(a0) * radius;
                double x1 = creatorX + Math.cos(a1) * radius;
                double z1 = creatorZ + Math.sin(a1) * radius;
                double thickness = 1.5D + glitchFactor * 1.0;

                quad(pose, consumer,
                        x0, y - thickness, z0, 0, 0,
                        x1, y - thickness, z1, 1, 0,
                        x1, y + thickness, z1, 1, 1,
                        x0, y + thickness, z0, 0, 1,
                        r, g, b, a, 0, 1, 0);
            }
        }

        double skirtY = creatorY - CREATOR_HEAD_SIZE * 0.5D;
        double skirtRadius = CREATOR_HEAD_SIZE * 1.8D + glitchFactor * 20.0;
        int skirtSegs = 20;
        for (int i = 0; i < skirtSegs; i++) {
            double a0 = (i / (double) skirtSegs) * Math.PI * 2.0D;
            double a1 = ((i + 1) / (double) skirtSegs) * Math.PI * 2.0D;
            double x0 = creatorX + Math.cos(a0) * skirtRadius;
            double z0 = creatorZ + Math.sin(a0) * skirtRadius;
            double x1 = creatorX + Math.cos(a1) * skirtRadius;
            double z1 = creatorZ + Math.sin(a1) * skirtRadius;
            double wave = Math.sin(time * 0.002D + a0 * 3.0D) * 8.0D + glitchFactor * Math.sin(time * 0.01 + a0 * 5.0) * 15.0;

            float hue = (float)((a0 / (Math.PI * 2.0D) + time * 0.0003D) % 1.0D);
            float[] rgb = hsvToRgb(hue, 0.9F, 1.0F);
            int r = (int)(rgb[0] * 255 * 0.6F * bloom);
            int g = (int)(rgb[1] * 255 * 0.6F * bloom);
            int b = (int)(rgb[2] * 255 * 0.6F * bloom);
            r = Math.min(255, r);
            g = Math.min(255, g);
            b = Math.min(255, b);
            int a = (int)(visibility * 60 * (1.0 + glitchFactor * 0.5));

            quad(pose, consumer,
                    x0, skirtY, z0, 0, 0,
                    x1, skirtY, z1, 1, 0,
                    x1, skirtY - 30.0D + wave, z1, 1, 1,
                    x0, skirtY - 30.0D + wave, z0, 0, 1,
                    r, g, b, a, 0, -1, 0);
        }
    }

    // ---- NEW: Glitch particles – violent mutation particles on Creator gaze warp ----
    private static void emitGlitchParticles(Pose pose, VertexConsumer consumer, Vec3 camera, double time, float glitchFactor) {
        double creatorX = camera.x + Math.cos(time * 0.00005D) * CREATOR_DISTANCE * 0.2D;
        double creatorZ = camera.z + Math.sin(time * 0.00005D) * CREATOR_DISTANCE * 0.2D;
        double creatorY = McsmVoidTiers.FLOOR_Y + 180.0D + 20.0D;

        int count = (int)(glitchFactor * 20) + 5;
        for (int i = 0; i < count; i++) {
            double angle = (i / (double) count) * Math.PI * 2.0D + time * 0.005 * (i % 2 == 0 ? 1 : -1);
            double radius = 30.0 + i * 5.0 + Mth.sin((float)(time * 0.01 + i)) * glitchFactor * 20.0;
            double x = creatorX + Math.cos(angle) * radius + McsmGlitchGenerator.fractalNoise(i * 0.7, time * 0.002) * glitchFactor * 30.0;
            double z = creatorZ + Math.sin(angle) * radius + McsmGlitchGenerator.fractalNoise(i * 0.9, time * 0.002) * glitchFactor * 30.0;
            double y = creatorY + Mth.sin((float)(time * 0.003 + i)) * 20.0 * glitchFactor + i * 2.0;

            double size = 1.5 + glitchFactor * 3.0;
            int r = 200 + (int)(55 * Math.sin(time * 0.02 + i));
            int g = 80 + (int)(50 * Math.cos(time * 0.015 + i));
            int b = 255;
            int a = (int)(200 * glitchFactor);

            quadFullBright(pose, consumer,
                    x - size, y - size, z, 0, 0,
                    x + size, y - size, z, 1, 0,
                    x + size, y + size, z, 1, 1,
                    x - size, y + size, z, 0, 1,
                    r, g, b, a);
        }
    }

    // ---- NEW: Glitch scanline overlay – HUD breaking effect in world space ----
    private static void emitGlitchScanlineOverlay(Pose pose, VertexConsumer consumer, Vec3 camera, double time, float glitchFactor, double fallDistance) {
        double y = camera.y - 20.0 - fallDistance * 0.05;
        double size = 200.0 + glitchFactor * 100.0;
        for (int i = 0; i < 5; i++) {
            double scanY = y + i * 15.0 + Math.sin(time * 0.01 + i) * glitchFactor * 10.0;
            double offsetX = Math.sin(time * 0.02 + i * 1.5) * glitchFactor * 20.0;

            int r = 255;
            int g = 255;
            int b = 255;
            int a = (int)(30 * glitchFactor * (0.5 + 0.5 * Math.sin(time * 0.02 + i)));

            quadFullBright(pose, consumer,
                    camera.x - size + offsetX, scanY - 0.5, camera.z, 0, 0,
                    camera.x + size + offsetX, scanY - 0.5, camera.z, 1, 0,
                    camera.x + size + offsetX, scanY + 0.5, camera.z, 1, 1,
                    camera.x - size + offsetX, scanY + 0.5, camera.z, 0, 1,
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
        vertex(pose, consumer, x0, y0, z0, u0, v0, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, x1, y1, z1, u1, v1, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, x2, y2, z2, u2, v2, r, g, b, a, nx, ny, nz);
        vertex(pose, consumer, x3, y3, z3, u3, v3, r, g, b, a, nx, ny, nz);
    }

    private static void quadFullBright(Pose pose, VertexConsumer consumer,
                                       double x0, double y0, double z0, float u0, float v0,
                                       double x1, double y1, double z1, float u1, float v1,
                                       double x2, double y2, double z2, float u2, float v2,
                                       double x3, double y3, double z3, float u3, float v3,
                                       int r, int g, int b, int a) {
        vertexFullBright(pose, consumer, x0, y0, z0, u0, v0, r, g, b, a);
        vertexFullBright(pose, consumer, x1, y1, z1, u1, v1, r, g, b, a);
        vertexFullBright(pose, consumer, x2, y2, z2, u2, v2, r, g, b, a);
        vertexFullBright(pose, consumer, x3, y3, z3, u3, v3, r, g, b, a);
    }

    private static void vertex(Pose pose, VertexConsumer consumer,
                               double x, double y, double z, float u, float v,
                               int r, int g, int b, int a,
                               float nx, float ny, float nz) {
        consumer.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, nx, ny, nz);
    }

    private static void vertexFullBright(Pose pose, VertexConsumer consumer,
                                         double x, double y, double z, float u, float v,
                                         int r, int g, int b, int a) {
        consumer.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static double fractalNoise(double x, double y) {
        double n = 0.0D;
        n += Math.sin(x * 1.0D + y * 0.7D) * 0.5D;
        n += Math.sin(x * 2.3D - y * 1.1D) * 0.25D;
        n += Math.sin(x * 4.7D + y * 2.3D) * 0.125D;
        n += Math.sin(x * 9.1D - y * 3.7D) * 0.0625D;
        return n;
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

    private static double smoothstep(double t) {
        double c = Mth.clamp(t, 0.0D, 1.0D);
        return c * c * (3.0D - 2.0D * c);
    }

    private static float smoothstep(float t) {
        float c = Mth.clamp(t, 0.0F, 1.0F);
        return c * c * (3.0F - 2.0F * c);
    }

    public static double gazeOffsetX(long now) {
        return gazeCurrentX + Math.sin(now / 5000.0D) * 8.0D;
    }
    public static double gazeOffsetZ(long now) {
        return gazeCurrentZ + Math.cos(now / 6000.0D) * 8.0D;
    }
    public static double gazeOffsetX() {
        return gazeCurrentX;
    }
    public static double gazeOffsetZ() {
        return gazeCurrentZ;
    }

    public static String state() {
        return "ninth: towers=" + GOTHIC_TOWERS + " castles=" + HANGING_CASTLES
                + " creator@" + (int) CREATOR_DISTANCE + " gaze=(" + (int) gazeCurrentX + "," + (int) gazeCurrentZ + ")"
                + " stomp=" + stompCount + " bloom=" + BLOOM_FACTOR + "->" + BLOOM_WARP + "x glitch=" + McsmGlitchGenerator.getChunkShufflePhase()
                + " matte=" + String.format("#%02X%02X%02X", (int)(MATTE_BLACK[0]*255), (int)(MATTE_BLACK[1]*255), (int)(MATTE_BLACK[2]*255))
                + "/#000000";
    }
}
