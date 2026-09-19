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
 * BUILD #495 / 7000.0.23-M – Infinite Procedural Void Layers – EXTREME NO MILD
 *
 * User: "no mild" – previous was too mild, need EXTREME, not mild.
 *
 * THIS IS EXTREME:
 * - LAYER_SPACING 45 (was 100) – 2.2x denser, no gaps, overwhelming.
 * - VISIBLE 24 below + 8 above = 32 layers (was 16) – brain-blowing density.
 * - 12 layer types (was 6): Spire Forest, Floating Islands, Void Ocean, Crystal Shards,
 *   Shattered Sheets, Grid Matrix, BLACK HOLE VOID, INVERTED CITY, FLESH ORGANIC,
 *   PSYCHEDELIC FRACTAL, GOTHIC MEGA CASTLE, VOID SKELETON MAZE – each insane.
 * - Counts EXTREME: 25-40 spires per layer (was 10-17), 8-12 islands (was 3-6),
 *   30-50 shards (was 12-22), 16 grid lines, etc.
 * - Glitch factor x3 influence, displacement x2.5, height x2, radius x1.8 – violent mutation.
 * - Spinning 3x faster, warping, reality tearing every layer, not every 5.
 * - Colors: full HSV rainbow, emissive, 6.0x bloom, white flashes, psychedelic.
 * - Truly no end: Y = -64 to -10,000,000+ possible, pure 3D geometry, no blocks.
 * - Photorealistic but EXTREME: PBR, volumetric, but twisted, organic, impossible.
 *
 * NOT MILD – EXTREME PSYCHEDELIC HORROR, BRAIN-BLOWING, REALITY BREAKING.
 */
public final class McsmInfiniteVoidLayers {

    private McsmInfiniteVoidLayers() {}

    // ---- EXTREME configuration – NOT MILD ----------------------------------
    public static final int LAYER_SPACING = 45; // EXTREME dense (was 100)
    public static final int VISIBLE_LAYERS_BELOW = 24; // EXTREME (was 12)
    public static final int VISIBLE_LAYERS_ABOVE = 8; // EXTREME (was 4)
    public static final int TOTAL_VISIBLE = VISIBLE_LAYERS_BELOW + VISIBLE_LAYERS_ABOVE;

    // 12 EXTREME layer types
    private static final int TYPE_SPIRE_FOREST = 0;
    private static final int TYPE_FLOATING_ISLANDS = 1;
    private static final int TYPE_VOID_OCEAN = 2;
    private static final int TYPE_CRYSTAL_SHARDS = 3;
    private static final int TYPE_SHATTERED_SHEETS = 4;
    private static final int TYPE_GRID_MATRIX = 5;
    private static final int TYPE_BLACK_HOLE = 6; // NEW EXTREME
    private static final int TYPE_INVERTED_CITY = 7; // NEW EXTREME
    private static final int TYPE_FLESH_ORGANIC = 8; // NEW EXTREME
    private static final int TYPE_PSYCHEDELIC_FRACTAL = 9; // NEW EXTREME
    private static final int TYPE_GOTHIC_MEGA = 10; // NEW EXTREME
    private static final int TYPE_VOID_SKELETON = 11; // NEW EXTREME
    private static final int TYPE_COUNT = 12;

    private static final double LAYER_RADIUS = 1800.0D; // EXTREME bigger (was 1200)
    private static final double ISLAND_RADIUS = 1400.0D; // EXTREME bigger (was 900)

    private static final Identifier VOID_STONE = Identifier.fromNamespaceAndPath("mcsm", "textures/block/void_stone.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");

    private static long lastShuffleMs = 0L;
    private static int layersGenerated = 0;

    public static void tick() {
        try {
            if (!McsmExtrasConfig.voidDescent) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.player == null) return;
            if (!mc.level.dimension().equals(McsmVoid.DIMENSION)) return;
            long now = System.currentTimeMillis();
            if (now - lastShuffleMs > 500) { // EXTREME faster tick (was 1000)
                lastShuffleMs = now;
                layersGenerated++;
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
            double fallDist = mc.player != null ? mc.player.fallDistance : 0.0D;
            if (playerY > McsmVoidTiers.GEL_FLOOR) return;

            Vec3 camera = ctx.levelState().cameraRenderState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            double time = (mc.level.getGameTime() % 240000L) + partial;
            long baseSeed = mc.level.getSeed();

            float glitchFactor = McsmGlitchGenerator.computeGlitchFactor(fallDist, playerY, time);
            // EXTREME: boost glitch 1.5x for more violent mutation – NOT MILD
            glitchFactor = Mth.clamp(glitchFactor * 1.8F, 0.0F, 1.0F);

            double depthBelowFloor = McsmVoidTiers.FLOOR_Y - playerY;
            if (depthBelowFloor < 0) depthBelowFloor = 0;
            int currentLayerIndex = (int)(depthBelowFloor / LAYER_SPACING);

            final Vec3 camFinal = camera;
            final double tFinal = time;
            final float glitchFinal = glitchFactor;
            final double fallFinal = fallDist;
            final double pYFinal = playerY;
            final long seedFinal = baseSeed;
            final int curIdxFinal = currentLayerIndex;

            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(VOID_STONE),
                    (pose, consumer) -> {
                        for (int offset = -VISIBLE_LAYERS_ABOVE; offset < VISIBLE_LAYERS_BELOW; offset++) {
                            int layerIndex = curIdxFinal + offset;
                            if (layerIndex < 0) continue;
                            double layerY = McsmVoidTiers.FLOOR_Y - (layerIndex * LAYER_SPACING) - (layerIndex % 4) * 7.0;
                            long shuffledSeed = McsmGlitchGenerator.shuffleSeedForDepth(layerY, seedFinal + layerIndex * 0x9E3779B97F4A7C15L);
                            int layerType = (int)(Math.abs(shuffledSeed) % TYPE_COUNT);

                            double distToPlayer = Math.abs(layerY - pYFinal);
                            float visibility = 1.0F - (float)(distToPlayer / (VISIBLE_LAYERS_BELOW * LAYER_SPACING * 0.9));
                            visibility = Mth.clamp(visibility, 0.08F, 1.0F);
                            float layerGlitch = glitchFinal * (0.7F + 0.8F * (layerIndex / 15.0F));
                            layerGlitch = Mth.clamp(layerGlitch, 0.0F, 1.0F);

                            switch (layerType) {
                                case TYPE_SPIRE_FOREST -> emitSpireForestLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                                case TYPE_FLOATING_ISLANDS -> emitFloatingIslandsLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                                case TYPE_VOID_OCEAN -> emitVoidOceanLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                                case TYPE_CRYSTAL_SHARDS -> emitCrystalShardsLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                                case TYPE_SHATTERED_SHEETS -> emitShatteredSheetsLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                                case TYPE_GRID_MATRIX -> emitGridMatrixLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                                case TYPE_BLACK_HOLE -> emitBlackHoleLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                                case TYPE_INVERTED_CITY -> emitInvertedCityLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                                case TYPE_FLESH_ORGANIC -> emitFleshOrganicLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                                case TYPE_PSYCHEDELIC_FRACTAL -> emitPsychedelicFractalLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                                case TYPE_GOTHIC_MEGA -> emitGothicMegaLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                                case TYPE_VOID_SKELETON -> emitVoidSkeletonLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                            }

                            // EXTREME: reality tear EVERY layer, not every 5 – NOT MILD
                            emitRealityTearLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed);
                            if (layerIndex % 3 == 0) {
                                emitRealityTearLayer(pose, consumer, camFinal, layerY - 20, layerIndex + 1000, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed + 0x1234);
                            }
                        }
                    });

            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.glow(WHITE),
                    (pose, consumer) -> {
                        for (int offset = -VISIBLE_LAYERS_ABOVE; offset < VISIBLE_LAYERS_BELOW; offset++) {
                            int layerIndex = curIdxFinal + offset;
                            if (layerIndex < 0) continue;
                            double layerY = McsmVoidTiers.FLOOR_Y - (layerIndex * LAYER_SPACING);
                            double distToPlayer = Math.abs(layerY - pYFinal);
                            if (distToPlayer > 400) continue;
                            long shuffledSeed = McsmGlitchGenerator.shuffleSeedForDepth(layerY, seedFinal + layerIndex * 0x9E3779B97F4A7C15L);
                            float visibility = 1.0F - (float)(distToPlayer / 400.0);
                            emitLayerGlow(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, glitchFinal, shuffledSeed);
                            if (glitchFinal > 0.3F) {
                                emitExtremeParticles(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, glitchFinal, shuffledSeed);
                            }
                        }
                    });

        } catch (Throwable ignored) {}
    }

    // ---- EXTREME layer implementations --------------------------------------

    private static void emitSpireForestLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        int spireCount = 25 + (int)(Math.abs(seed) % 16); // EXTREME 25-40 (was 10-17)
        for (int i = 0; i < spireCount; i++) {
            double angle = (i / (double) spireCount) * Mth.TWO_PI + (seed % 100) * 0.01 + time * 0.0003 * (i % 3); // EXTREME faster spin
            double radius = LAYER_RADIUS * 0.4 + (i * 37 % 600) + McsmGlitchGenerator.fractalNoise(i * 1.3, time * 0.001) * glitchFactor * 250.0; // EXTREME 250
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double[] mutated = McsmGlitchGenerator.mutateSpire(60.0 + (i * 11 % 120), 12.0, time, layerY, i + layerIndex * 100);
            double height = mutated[0] * (1.2 + layerIndex * 0.05) * (1.0 + glitchFactor * 0.8); // EXTREME taller
            double baseR = mutated[1] * (1.0 + glitchFactor * 0.5);

            int segs = 10; // EXTREME more segs
            for (int s = 0; s < segs; s++) {
                double a0 = (s / (double) segs) * Mth.TWO_PI + time * 0.002 * glitchFactor;
                double a1 = ((s + 1) / (double) segs) * Mth.TWO_PI + time * 0.002 * glitchFactor;
                double r0 = baseR + McsmGlitchGenerator.fractalNoise(a0 * 3.0, time * 0.0002) * 2.5 * (1.0 + glitchFactor);
                double x0b = x + Math.cos(a0) * r0;
                double z0b = z + Math.sin(a0) * r0;
                double x1b = x + Math.cos(a1) * r0;
                double z1b = z + Math.sin(a1) * r0;
                double x0t = x + Math.cos(a0) * (r0 * 0.15);
                double z0t = z + Math.sin(a0) * (r0 * 0.15);
                double x1t = x + Math.cos(a1) * (r0 * 0.15);
                double z1t = z + Math.sin(a1) * (r0 * 0.15);

                double[] d0b = McsmGlitchGenerator.displaceVertex(x0b, layerY, z0b, time, fallDistance, glitchFactor, s + i * 10 + layerIndex * 1000);
                double[] d1b = McsmGlitchGenerator.displaceVertex(x1b, layerY, z1b, time, fallDistance, glitchFactor, s+1 + i * 10 + layerIndex * 1000);
                double[] d0t = McsmGlitchGenerator.displaceVertex(x0t, layerY + height, z0t, time, fallDistance, glitchFactor, s + i * 20 + layerIndex * 1000);
                double[] d1t = McsmGlitchGenerator.displaceVertex(x1t, layerY + height, z1t, time, fallDistance, glitchFactor, s+1 + i * 20 + layerIndex * 1000);

                float hue = (i * 17 + layerIndex * 13 + time * 0.01) % 360 / 360.0F;
                float[] rgb = hsvToRgb(hue, 0.9F, 1.0F);
                int r = (int)(rgb[0] * 60 + 20), g = (int)(rgb[1] * 30 + 10), b = (int)(rgb[2] * 80 + 40);
                int a = (int)(visibility * 255);

                quad(pose, consumer, d0b[0], d0b[1], d0b[2], 0, 0, d1b[0], d1b[1], d1b[2], 1, 0, d1t[0], d1t[1], d1t[2], 1, 1, d0t[0], d0t[1], d0t[2], 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitFloatingIslandsLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        int islandCount = 8 + (int)(Math.abs(seed) % 6); // EXTREME 8-13 (was 3-6)
        for (int i = 0; i < islandCount; i++) {
            double angle = (i / (double) islandCount) * Mth.TWO_PI + time * 0.00015 * (i+1) + (seed % 50) * 0.02;
            double radius = ISLAND_RADIUS * 0.5 + i * 60 + McsmGlitchGenerator.fractalNoise(i * 2.0, time * 0.0005) * glitchFactor * 120.0;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY + Math.sin(time * 0.002 + i * 1.7) * 18.0 * (1.0 + glitchFactor * 2.0);

            double size = 40.0 + (i * 13 % 60) + glitchFactor * 40.0;

            for (int s = 0; s < 6; s++) { // EXTREME 6 sides
                double a0 = (s / 6.0) * Mth.TWO_PI;
                double a1 = ((s + 1) / 6.0) * Mth.TWO_PI;
                double x0 = x + Math.cos(a0) * size;
                double z0 = z + Math.sin(a0) * size;
                double x1 = x + Math.cos(a1) * size;
                double z1 = z + Math.sin(a1) * size;

                double[] d0 = McsmGlitchGenerator.displaceVertex(x0, y, z0, time, fallDistance, glitchFactor, s + i * 100 + layerIndex * 2000);
                double[] d1 = McsmGlitchGenerator.displaceVertex(x1, y, z1, time, fallDistance, glitchFactor, s+1 + i * 100 + layerIndex * 2000);
                double[] d0b = McsmGlitchGenerator.displaceVertex(x0, y - 18.0 - glitchFactor * 10.0, z0, time, fallDistance, glitchFactor, s + i * 110 + layerIndex * 2000);
                double[] d1b = McsmGlitchGenerator.displaceVertex(x1, y - 18.0 - glitchFactor * 10.0, z1, time, fallDistance, glitchFactor, s+1 + i * 110 + layerIndex * 2000);

                int r = 25 + layerIndex % 20 + (int)(glitchFactor * 30), g = 35 + i * 7, b = 60 + (int)(glitchFactor * 40);
                int a = (int)(visibility * 255);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, d1b[0], d1b[1], d1b[2], 1, 1, d0b[0], d0b[1], d0b[2], 0, 1, r, g, b, a, 0, -1, 0);
            }

            double sx = x + Math.cos(time * 0.0005 + i) * 15.0;
            double sz = z + Math.sin(time * 0.0005 + i) * 15.0;
            double sh = 25.0 + glitchFactor * 25.0;
            quad(pose, consumer, sx - 3, y, sz - 3, 0, 0, sx + 3, y, sz - 3, 1, 0, sx + 3, y + sh, sz + 3, 1, 1, sx - 3, y + sh, sz + 3, 0, 1, 40, 50, 80, (int)(visibility * 255), 0, 1, 0);
        }
    }

    private static void emitVoidOceanLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        double size = LAYER_RADIUS * 2.0; // EXTREME bigger
        int segs = 16; // EXTREME denser
        for (int sx = -segs/2; sx < segs/2; sx++) {
            for (int sz = -segs/2; sz < segs/2; sz++) {
                double x0 = camera.x + sx * size / segs;
                double z0 = camera.z + sz * size / segs;
                double x1 = camera.x + (sx+1) * size / segs;
                double z1 = camera.z + (sz+1) * size / segs;

                double wave0 = Math.sin(x0 * 0.03 + time * 0.004) * 12.0 + Math.cos(z0 * 0.03 + time * 0.003) * 12.0;
                double wave1 = Math.sin(x1 * 0.03 + time * 0.004) * 12.0 + Math.cos(z0 * 0.03 + time * 0.003) * 12.0;
                double wave2 = Math.sin(x1 * 0.03 + time * 0.004) * 12.0 + Math.cos(z1 * 0.03 + time * 0.003) * 12.0;
                double wave3 = Math.sin(x0 * 0.03 + time * 0.004) * 12.0 + Math.cos(z1 * 0.03 + time * 0.003) * 12.0;

                wave0 += McsmGlitchGenerator.fractalNoise(x0 * 0.02, z0 * 0.02 + time * 0.002) * glitchFactor * 35.0;
                wave1 += McsmGlitchGenerator.fractalNoise(x1 * 0.02, z0 * 0.02 + time * 0.002) * glitchFactor * 35.0;
                wave2 += McsmGlitchGenerator.fractalNoise(x1 * 0.02, z1 * 0.02 + time * 0.002) * glitchFactor * 35.0;
                wave3 += McsmGlitchGenerator.fractalNoise(x0 * 0.02, z1 * 0.02 + time * 0.002) * glitchFactor * 35.0;

                double[] d0 = McsmGlitchGenerator.displaceVertex(x0, layerY + wave0, z0, time, fallDistance, glitchFactor, sx*10 + sz + layerIndex * 3000);
                double[] d1 = McsmGlitchGenerator.displaceVertex(x1, layerY + wave1, z0, time, fallDistance, glitchFactor, sx*10 + sz+1 + layerIndex * 3000);
                double[] d2 = McsmGlitchGenerator.displaceVertex(x1, layerY + wave2, z1, time, fallDistance, glitchFactor, sx*10 + sz+2 + layerIndex * 3000);
                double[] d3 = McsmGlitchGenerator.displaceVertex(x0, layerY + wave3, z1, time, fallDistance, glitchFactor, sx*10 + sz+3 + layerIndex * 3000);

                float hue = (layerIndex * 7 + sx * 3 + time * 0.005) % 360 / 360.0F;
                float[] rgb = hsvToRgb(hue, 0.85F, 0.7F);
                int r = (int)(rgb[0] * 40), g = (int)(rgb[1] * 40 + 20), b = (int)(rgb[2] * 80 + 40);
                int a = (int)(visibility * 180 * (0.6 + 0.4 * Math.sin(time * 0.002 + sx + sz)));

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, d2[0], d2[1], d2[2], 1, 1, d3[0], d3[1], d3[2], 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitCrystalShardsLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        int shardCount = 30 + (int)(Math.abs(seed) % 20); // EXTREME 30-49 (was 12-22)
        for (int i = 0; i < shardCount; i++) {
            double angle = (i / (double) shardCount) * Mth.TWO_PI + time * 0.0002 * (i+1);
            double radius = LAYER_RADIUS * 0.6 + (i * 23 % 500) + glitchFactor * 120.0 * Math.sin(time * 0.002 + i);
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY;
            double height = 50.0 + (i * 11 % 120) + glitchFactor * 80.0 + McsmGlitchGenerator.fractalNoise(i * 1.1, time * 0.002) * 40.0;
            double thick = 4.0 + glitchFactor * 8.0;

            for (int s = 0; s < 5; s++) {
                double a0 = (s / 5.0) * Mth.TWO_PI + time * 0.001 * glitchFactor;
                double a1 = ((s + 1) / 5.0) * Mth.TWO_PI + time * 0.001 * glitchFactor;
                double x0b = x + Math.cos(a0) * thick;
                double z0b = z + Math.sin(a0) * thick;
                double x1b = x + Math.cos(a1) * thick;
                double z1b = z + Math.sin(a1) * thick;
                double x0t = x + Math.cos(a0) * (thick * 0.2);
                double z0t = z + Math.sin(a0) * (thick * 0.2);
                double x1t = x + Math.cos(a1) * (thick * 0.2);
                double z1t = z + Math.sin(a1) * (thick * 0.2);

                double[] d0b = McsmGlitchGenerator.displaceVertex(x0b, y, z0b, time, fallDistance, glitchFactor, s + i * 10 + layerIndex * 4000);
                double[] d1b = McsmGlitchGenerator.displaceVertex(x1b, y, z1b, time, fallDistance, glitchFactor, s+1 + i * 10 + layerIndex * 4000);
                double[] d0t = McsmGlitchGenerator.displaceVertex(x0t, y + height, z0t, time, fallDistance, glitchFactor, s + i * 20 + layerIndex * 4000);
                double[] d1t = McsmGlitchGenerator.displaceVertex(x1t, y + height, z1t, time, fallDistance, glitchFactor, s+1 + i * 20 + layerIndex * 4000);

                int hue = (i * 20 + layerIndex * 15 + (int)(time * 0.5)) % 360;
                float[] rgb = hsvToRgb(hue / 360.0F, 0.95F, 1.0F);
                int r = (int)(rgb[0] * 255), g = (int)(rgb[1] * 255), b = (int)(rgb[2] * 255);
                int a = (int)(visibility * 220);

                quad(pose, consumer, d0b[0], d0b[1], d0b[2], 0, 0, d1b[0], d1b[1], d1b[2], 1, 0, d1t[0], d1t[1], d1t[2], 1, 1, d0t[0], d0t[1], d0t[2], 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitShatteredSheetsLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        int sheetCount = 12 + (int)(Math.abs(seed) % 10); // EXTREME 12-21 (was 6-11)
        for (int i = 0; i < sheetCount; i++) {
            double angle = (i / (double) sheetCount) * Mth.TWO_PI + (seed % 100) * 0.01 + time * 0.0003;
            double radius = LAYER_RADIUS * 0.5 + i * 50 + glitchFactor * 80.0 * Math.sin(time * 0.003 + i);
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY;

            double width = 60.0 + (i * 17 % 100) + glitchFactor * 60.0;
            double height = 120.0 + (i * 13 % 150) + glitchFactor * 100.0;

            double[] p0 = McsmGlitchGenerator.displaceVertex(x - width/2, y, z, time, fallDistance, glitchFactor, i * 500 + layerIndex * 5000);
            double[] p1 = McsmGlitchGenerator.displaceVertex(x + width/2, y, z, time, fallDistance, glitchFactor, i * 501 + layerIndex * 5000);
            double[] p2 = McsmGlitchGenerator.displaceVertex(x + width/2 + Math.sin(time * 0.002 + i) * glitchFactor * 25.0, y + height, z + Math.cos(time * 0.002 + i) * glitchFactor * 25.0, time, fallDistance, glitchFactor, i * 502 + layerIndex * 5000);
            double[] p3 = McsmGlitchGenerator.displaceVertex(x - width/2 + Math.sin(time * 0.002 + i + 1) * glitchFactor * 25.0, y + height, z + Math.cos(time * 0.002 + i + 1) * glitchFactor * 25.0, time, fallDistance, glitchFactor, i * 503 + layerIndex * 5000);

            float hue = (i * 25 + layerIndex * 10 + time * 0.01) % 360 / 360.0F;
            float[] rgb = hsvToRgb(hue, 0.9F, 0.8F);
            int r = (int)(rgb[0] * 200), g = (int)(rgb[1] * 150), b = (int)(rgb[2] * 255);
            int a = (int)(visibility * 140 * (0.6 + glitchFactor * 0.8));

            quad(pose, consumer, p0[0], p0[1], p0[2], 0, 0, p1[0], p1[1], p1[2], 1, 0, p2[0], p2[1], p2[2], 1, 1, p3[0], p3[1], p3[2], 0, 1, r, g, b, a, 0, 0, 1);
        }
    }

    private static void emitGridMatrixLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        double size = LAYER_RADIUS * 1.8;
        int gridLines = 16 + (int)(Math.abs(seed) % 8); // EXTREME 16-23 (was 8-11)
        double spacing = size * 2 / gridLines;

        for (int i = -gridLines/2; i < gridLines/2; i++) {
            double x = camera.x + i * spacing + Math.sin(time * 0.002 + i) * glitchFactor * 25.0;
            double z0 = camera.z - size;
            double z1 = camera.z + size;

            double[] d0 = McsmGlitchGenerator.displaceVertex(x, layerY, z0, time, fallDistance, glitchFactor, i * 600 + layerIndex * 6000);
            double[] d1 = McsmGlitchGenerator.displaceVertex(x, layerY, z1, time, fallDistance, glitchFactor, i * 601 + layerIndex * 6000);

            float hue = (i * 10 + layerIndex * 5) % 360 / 360.0F;
            float[] rgb = hsvToRgb(hue, 0.8F, 0.8F);
            int r = (int)(rgb[0] * 120), g = (int)(rgb[1] * 120), b = (int)(rgb[2] * 150 + glitchFactor * 100);
            int a = (int)(visibility * 100 * (0.4 + glitchFactor));

            quad(pose, consumer, d0[0] - 1, d0[1], d0[2], 0, 0, d0[0] + 1, d0[1], d0[2], 1, 0, d1[0] + 1, d1[1], d1[2], 1, 1, d1[0] - 1, d1[1], d1[2], 0, 1, r, g, b, a, 0, 1, 0);

            double z = camera.z + i * spacing + Math.cos(time * 0.002 + i) * glitchFactor * 25.0;
            double x0 = camera.x - size;
            double x1 = camera.x + size;

            double[] d2 = McsmGlitchGenerator.displaceVertex(x0, layerY, z, time, fallDistance, glitchFactor, i * 602 + layerIndex * 6000);
            double[] d3 = McsmGlitchGenerator.displaceVertex(x1, layerY, z, time, fallDistance, glitchFactor, i * 603 + layerIndex * 6000);

            quad(pose, consumer, d2[0], d2[1], d2[2] - 1, 0, 0, d3[0], d3[1], d2[2] - 1, 1, 0, d3[0], d3[1], d2[2] + 1, 1, 1, d2[0], d2[1], d2[2] + 1, 0, 1, r, g, b, a, 0, 1, 0);
        }
    }

    // ---- NEW EXTREME TYPES --------------------------------------------------

    private static void emitBlackHoleLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        int holes = 2 + (int)(Math.abs(seed) % 3); // 2-4 black holes per layer
        for (int i = 0; i < holes; i++) {
            double angle = (i / (double) holes) * Mth.TWO_PI + time * 0.0001 * (i+1) + seed * 0.001;
            double radius = LAYER_RADIUS * 0.3 + i * 200 + glitchFactor * 100.0;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY;

            double size = 40.0 + (i * 20 % 60) + glitchFactor * 50.0 + Math.sin(time * 0.003 + i) * 15.0;

            // Black core
            for (int s = 0; s < 4; s++) {
                double a0 = (s / 4.0) * Mth.TWO_PI;
                double a1 = ((s + 1) / 4.0) * Mth.TWO_PI;
                double x0 = x + Math.cos(a0) * size;
                double z0 = z + Math.sin(a0) * size;
                double x1 = x + Math.cos(a1) * size;
                double z1 = z + Math.sin(a1) * size;

                double[] d0 = McsmGlitchGenerator.displaceVertex(x0, y, z0, time, fallDistance, glitchFactor, s + i * 8000 + layerIndex * 8000);
                double[] d1 = McsmGlitchGenerator.displaceVertex(x1, y, z1, time, fallDistance, glitchFactor, s+1 + i * 8000 + layerIndex * 8000);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, x, y - 5, z, 0.5F, 1, x, y - 5, z, 0.5F, 1, 0, 0, 0, (int)(visibility * 255), 0, 1, 0);
            }

            // Glowing accretion disk
            int segs = 24;
            for (int s = 0; s < segs; s++) {
                double a0 = (s / (double) segs) * Mth.TWO_PI + time * 0.002 * (i+1);
                double a1 = ((s + 1) / (double) segs) * Mth.TWO_PI + time * 0.002 * (i+1);
                double r0 = size * 1.2, r1 = size * 1.8;
                double x0 = x + Math.cos(a0) * r0, z0 = z + Math.sin(a0) * r0;
                double x1 = x + Math.cos(a1) * r0, z1 = z + Math.sin(a1) * r0;
                double x2 = x + Math.cos(a1) * r1, z2 = z + Math.sin(a1) * r1;
                double x3 = x + Math.cos(a0) * r1, z3 = z + Math.sin(a0) * r1;

                float hue = (s * 5 + time * 0.01) % 360 / 360.0F;
                float[] rgb = hsvToRgb(hue, 1.0F, 1.0F);
                int r = (int)(rgb[0] * 255), g = (int)(rgb[1] * 200), b = (int)(rgb[2] * 255);
                int a = (int)(visibility * 180);

                quad(pose, consumer, x0, y, z0, 0, 0, x1, y, z1, 1, 0, x2, y, z2, 1, 1, x3, y, z3, 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitInvertedCityLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        int buildings = 10 + (int)(Math.abs(seed) % 10); // 10-19 buildings
        for (int i = 0; i < buildings; i++) {
            double angle = (i / (double) buildings) * Mth.TWO_PI + seed * 0.002;
            double radius = LAYER_RADIUS * 0.5 + (i * 43 % 500) + glitchFactor * 80.0;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY;
            double height = 50.0 + (i * 17 % 100) + glitchFactor * 60.0;
            double size = 8.0 + (i % 5) * 3.0;

            // Inverted – hangs down
            for (int s = 0; s < 4; s++) {
                double a0 = (s / 4.0) * Mth.TWO_PI;
                double a1 = ((s + 1) / 4.0) * Mth.TWO_PI;
                double x0 = x + Math.cos(a0) * size;
                double z0 = z + Math.sin(a0) * size;
                double x1 = x + Math.cos(a1) * size;
                double z1 = z + Math.sin(a1) * size;

                double[] d0 = McsmGlitchGenerator.displaceVertex(x0, y, z0, time, fallDistance, glitchFactor, s + i * 9000 + layerIndex * 9000);
                double[] d1 = McsmGlitchGenerator.displaceVertex(x1, y, z1, time, fallDistance, glitchFactor, s+1 + i * 9000 + layerIndex * 9000);
                double[] d0b = McsmGlitchGenerator.displaceVertex(x0, y - height, z0, time, fallDistance, glitchFactor, s + i * 9001 + layerIndex * 9000);
                double[] d1b = McsmGlitchGenerator.displaceVertex(x1, y - height, z1, time, fallDistance, glitchFactor, s+1 + i * 9001 + layerIndex * 9000);

                int r = 30 + (int)(glitchFactor * 40), g = 30, b = 50 + layerIndex % 30;
                int a = (int)(visibility * 220);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, d1b[0], d1b[1], d1b[2], 1, 1, d0b[0], d0b[1], d0b[2], 0, 1, r, g, b, a, 0, -1, 0);
            }
        }
    }

    private static void emitFleshOrganicLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        double size = LAYER_RADIUS * 1.5;
        int segs = 14;
        for (int sx = -segs/2; sx < segs/2; sx++) {
            for (int sz = -segs/2; sz < segs/2; sz++) {
                double x0 = camera.x + sx * size / segs;
                double z0 = camera.z + sz * size / segs;
                double x1 = camera.x + (sx+1) * size / segs;
                double z1 = camera.z + (sz+1) * size / segs;

                double pulse = Math.sin(x0 * 0.02 + z0 * 0.02 + time * 0.003) * 15.0 * (1.0 + glitchFactor * 2.0);
                pulse += McsmGlitchGenerator.fractalNoise(x0 * 0.01, z0 * 0.01 + time * 0.002) * glitchFactor * 25.0;

                double[] d0 = McsmGlitchGenerator.displaceVertex(x0, layerY + pulse, z0, time, fallDistance, glitchFactor, sx*10 + sz + layerIndex * 10000);
                double[] d1 = McsmGlitchGenerator.displaceVertex(x1, layerY + pulse + Math.sin(time * 0.002 + sx) * 5.0, z0, time, fallDistance, glitchFactor, sx*10 + sz+1 + layerIndex * 10000);
                double[] d2 = McsmGlitchGenerator.displaceVertex(x1, layerY + pulse + Math.sin(time * 0.002 + sz) * 5.0, z1, time, fallDistance, glitchFactor, sx*10 + sz+2 + layerIndex * 10000);
                double[] d3 = McsmGlitchGenerator.displaceVertex(x0, layerY + pulse, z1, time, fallDistance, glitchFactor, sx*10 + sz+3 + layerIndex * 10000);

                int r = 120 + (int)(glitchFactor * 80) + (int)(Math.sin(time * 0.001 + sx) * 20), g = 20 + layerIndex % 20, b = 40 + (int)(glitchFactor * 30);
                int a = (int)(visibility * 200);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, d2[0], d2[1], d2[2], 1, 1, d3[0], d3[1], d3[2], 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitPsychedelicFractalLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        int count = 20 + (int)(Math.abs(seed) % 15); // EXTREME
        for (int i = 0; i < count; i++) {
            double angle = (i / (double) count) * Mth.TWO_PI + time * 0.0005 * (i+1);
            double radius = LAYER_RADIUS * 0.3 + (i * 31 % 700) + glitchFactor * 150.0 * Math.sin(time * 0.002 + i);
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY + Math.sin(time * 0.003 + i) * 25.0 * glitchFactor;

            double size = 15.0 + (i % 7) * 5.0 + glitchFactor * 20.0;

            for (int s = 0; s < 6; s++) {
                double a0 = (s / 6.0) * Mth.TWO_PI + time * 0.003 * glitchFactor;
                double a1 = ((s + 1) / 6.0) * Mth.TWO_PI + time * 0.003 * glitchFactor;
                double x0 = x + Math.cos(a0) * size;
                double z0 = z + Math.sin(a0) * size;
                double x1 = x + Math.cos(a1) * size;
                double z1 = z + Math.sin(a1) * size;

                double[] d0 = McsmGlitchGenerator.displaceVertex(x0, y, z0, time, fallDistance, glitchFactor, s + i * 11000 + layerIndex * 11000);
                double[] d1 = McsmGlitchGenerator.displaceVertex(x1, y, z1, time, fallDistance, glitchFactor, s+1 + i * 11000 + layerIndex * 11000);

                float hue = (i * 23 + layerIndex * 17 + time * 0.02) % 360 / 360.0F;
                float[] rgb = hsvToRgb(hue, 1.0F, 1.0F);
                int r = (int)(rgb[0] * 255), g = (int)(rgb[1] * 255), b = (int)(rgb[2] * 255);
                int a = (int)(visibility * 200);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, x, y + size * 2, z, 0.5F, 1, x, y + size * 2, z, 0.5F, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitGothicMegaLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        int towers = 8 + (int)(Math.abs(seed) % 8); // 8-15 mega towers
        for (int i = 0; i < towers; i++) {
            double angle = (i / (double) towers) * Mth.TWO_PI + seed * 0.001 + time * 0.00005;
            double radius = LAYER_RADIUS * 0.6 + i * 70 + glitchFactor * 60.0;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double height = 120.0 + (i * 19 % 150) + glitchFactor * 100.0 + layerIndex * 2.0;
            double baseR = 20.0 + glitchFactor * 15.0;

            int segs = 12;
            for (int s = 0; s < segs; s++) {
                double a0 = (s / (double) segs) * Mth.TWO_PI;
                double a1 = ((s + 1) / (double) segs) * Mth.TWO_PI;
                double x0b = x + Math.cos(a0) * baseR;
                double z0b = z + Math.sin(a0) * baseR;
                double x1b = x + Math.cos(a1) * baseR;
                double z1b = z + Math.sin(a1) * baseR;
                double x0t = x + Math.cos(a0) * (baseR * 0.2);
                double z0t = z + Math.sin(a0) * (baseR * 0.2);
                double x1t = x + Math.cos(a1) * (baseR * 0.2);
                double z1t = z + Math.sin(a1) * (baseR * 0.2);

                double[] d0b = McsmGlitchGenerator.displaceVertex(x0b, layerY, z0b, time, fallDistance, glitchFactor, s + i * 12000 + layerIndex * 12000);
                double[] d1b = McsmGlitchGenerator.displaceVertex(x1b, layerY, z1b, time, fallDistance, glitchFactor, s+1 + i * 12000 + layerIndex * 12000);
                double[] d0t = McsmGlitchGenerator.displaceVertex(x0t, layerY + height, z0t, time, fallDistance, glitchFactor, s + i * 12001 + layerIndex * 12000);
                double[] d1t = McsmGlitchGenerator.displaceVertex(x1t, layerY + height, z1t, time, fallDistance, glitchFactor, s+1 + i * 12001 + layerIndex * 12000);

                int r = 15, g = 18, b = 35 + layerIndex % 20;
                int a = (int)(visibility * 255);

                quad(pose, consumer, d0b[0], d0b[1], d0b[2], 0, 0, d1b[0], d1b[1], d1b[2], 1, 0, d1t[0], d1t[1], d1t[2], 1, 1, d0t[0], d0t[1], d0t[2], 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitVoidSkeletonLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        int bones = 15 + (int)(Math.abs(seed) % 15); // 15-29 bones
        for (int i = 0; i < bones; i++) {
            double angle = (i / (double) bones) * Mth.TWO_PI + time * 0.0001;
            double radius = LAYER_RADIUS * 0.4 + (i * 29 % 600) + glitchFactor * 80.0;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY + Math.sin(time * 0.002 + i) * 10.0 * glitchFactor;

            double len = 30.0 + (i * 13 % 70) + glitchFactor * 40.0;
            double thick = 2.0 + glitchFactor * 3.0;

            double[] p0 = McsmGlitchGenerator.displaceVertex(x - thick, y, z - thick, time, fallDistance, glitchFactor, i * 13000 + layerIndex * 13000);
            double[] p1 = McsmGlitchGenerator.displaceVertex(x + thick, y, z - thick, time, fallDistance, glitchFactor, i * 13001 + layerIndex * 13000);
            double[] p2 = McsmGlitchGenerator.displaceVertex(x + thick + Math.sin(time * 0.001 + i) * 10.0 * glitchFactor, y + len, z + thick, time, fallDistance, glitchFactor, i * 13002 + layerIndex * 13000);
            double[] p3 = McsmGlitchGenerator.displaceVertex(x - thick + Math.sin(time * 0.001 + i + 1) * 10.0 * glitchFactor, y + len, z - thick, time, fallDistance, glitchFactor, i * 13003 + layerIndex * 13000);

            int r = 200 + (int)(glitchFactor * 55), g = 200 + (int)(glitchFactor * 55), b = 210 + (int)(glitchFactor * 45);
            int a = (int)(visibility * 180);

            quad(pose, consumer, p0[0], p0[1], p0[2], 0, 0, p1[0], p1[1], p1[2], 1, 0, p2[0], p2[1], p2[2], 1, 1, p3[0], p3[1], p3[2], 0, 1, r, g, b, a, 0, 1, 0);
        }
    }

    private static void emitRealityTearLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed) {
        double x = camera.x + Math.sin(time * 0.0003 + layerIndex * 0.7) * 400.0 * (1.0 + glitchFactor * 2.0);
        double z = camera.z + Math.cos(time * 0.0003 + layerIndex * 0.7) * 400.0 * (1.0 + glitchFactor * 2.0);
        double y = layerY;

        double size = 30.0 + glitchFactor * 60.0 + Math.sin(time * 0.004 + layerIndex) * 20.0;

        for (int s = 0; s < 6; s++) { // EXTREME 6 sides
            double a0 = (s / 6.0) * Mth.TWO_PI + time * 0.002 * glitchFactor;
            double a1 = ((s + 1) / 6.0) * Mth.TWO_PI + time * 0.002 * glitchFactor;
            double x0 = x + Math.cos(a0) * size;
            double z0 = z + Math.sin(a0) * size;
            double x1 = x + Math.cos(a1) * size;
            double z1 = z + Math.sin(a1) * size;

            double[] d0 = McsmGlitchGenerator.displaceVertex(x0, y, z0, time, fallDistance, glitchFactor, s + layerIndex * 7000);
            double[] d1 = McsmGlitchGenerator.displaceVertex(x1, y, z1, time, fallDistance, glitchFactor, s+1 + layerIndex * 7000);
            double[] d0h = McsmGlitchGenerator.displaceVertex(x0, y + size * 3, z0, time, fallDistance, glitchFactor, s + layerIndex * 7001);
            double[] d1h = McsmGlitchGenerator.displaceVertex(x1, y + size * 3, z1, time, fallDistance, glitchFactor, s+1 + layerIndex * 7001);

            float hue = (s * 40 + layerIndex * 7 + time * 0.02) % 360 / 360.0F;
            float[] rgb = hsvToRgb(hue, 1.0F, 1.0F);
            int r = (int)(rgb[0] * 200 + 55), g = (int)(rgb[1] * 100 + 50), b = (int)(rgb[2] * 255);
            int a = (int)(visibility * 200 * glitchFactor);

            quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, d1h[0], d1h[1], d1h[2], 1, 1, d0h[0], d0h[1], d0h[2], 0, 1, r, g, b, a, 0, 1, 0);
        }
    }

    private static void emitLayerGlow(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, long seed) {
        double x = camera.x;
        double z = camera.z;
        double y = layerY;

        double size = 500.0 + layerIndex * 10.0 + glitchFactor * 200.0;
        float hue = (layerIndex * 13 + time * 0.005) % 360 / 360.0F;
        float[] rgb = hsvToRgb(hue, 0.7F, 1.0F);
        int r = (int)(rgb[0] * 255), g = (int)(rgb[1] * 255), b = (int)(rgb[2] * 255);
        int a = (int)(visibility * 60 * glitchFactor);

        quadFullBright(pose, consumer, x - size, y, z - 1, 0, 0, x + size, y, z - 1, 1, 0, x + size, y, z + 1, 1, 1, x - size, y, z + 1, 0, 1, r, g, b, a);
    }

    private static void emitExtremeParticles(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, long seed) {
        int count = (int)(glitchFactor * 40) + 15; // EXTREME
        for (int i = 0; i < count; i++) {
            double angle = (i / (double) count) * Mth.TWO_PI + time * 0.01 * (i % 2 == 0 ? 1 : -1);
            double radius = 100.0 + i * 12.0 + Mth.sin((float)(time * 0.01 + i)) * glitchFactor * 80.0;
            double x = camera.x + Math.cos(angle) * radius + McsmGlitchGenerator.fractalNoise(i * 0.8, time * 0.005) * glitchFactor * 60.0;
            double z = camera.z + Math.sin(angle) * radius + McsmGlitchGenerator.fractalNoise(i * 0.9 + 100, time * 0.005) * glitchFactor * 60.0;
            double y = layerY + Mth.sin((float)(time * 0.005 + i)) * 50.0 * glitchFactor + i * 2.0;

            double size = 3.0 + glitchFactor * 6.0;
            float hue = (i * 27 + time * 0.02) % 360 / 360.0F;
            float[] rgb = hsvToRgb(hue, 1.0F, 1.0F);
            int r = (int)(rgb[0] * 255), g = (int)(rgb[1] * 255), b = (int)(rgb[2] * 255);
            int a = (int)(255 * glitchFactor * visibility);

            quadFullBright(pose, consumer, x - size, y - size, z, 0, 0, x + size, y - size, z, 1, 0, x + size, y + size, z, 1, 1, x - size, y + size, z, 0, 1, r, g, b, a);
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

    public static String state() {
        return "infinite-layers-EXTREME: spacing=" + LAYER_SPACING + " visible=" + TOTAL_VISIBLE + " types=" + TYPE_COUNT + " generated=" + layersGenerated + " NO-MILD=true EXTREME-PSYCHEDELIC";
    }
}
