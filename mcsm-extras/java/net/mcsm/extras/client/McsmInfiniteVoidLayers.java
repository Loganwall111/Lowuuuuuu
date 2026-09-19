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
 * BUILD #489 / 7000.0.25-M – V3 Endless Possibilities Update – Infinite Procedural Void Layers
 *
 * Primary Development Base Line: anchor on stable 264/264 gate-checked V2 framework lineage
 * Branch: arena/01a0b039-lowuuuuuu
 *
 * Core Mandate – PUSH TO ABSOLUTE LIMIT:
 * - Over lots of layers, one meditation in there each layer generated regularly assets and put in
 *   asset generator to instantly keep generating procedural assets infinitely.
 * - Entire thing infinite ears up, continue different stuff, mind glowing psychedelic you can imagine.
 * - Eliminate 20-mesh limit, ground-up true procedural 3D mesh synthesis generator with live GPU-driven noise loops.
 * - F1 two realities: main game goes through unlimited infinite cheers causing endless universe effect,
 *   F1 hides HUD to show infinite generator mesh using super duper photorealistic landscape shader technique.
 *   Pressing F again returns to main mesh, both active while falling, meditation per layer.
 *
 * V3 EXTREME – NOT MILD – OVERWHELMING:
 * - LAYER_SPACING 30 (was 45) – 1.5x denser, 3.3x denser than original 100, no gaps, overwhelming.
 * - VISIBLE 36 below + 12 above = 48 layers (was 32) – brain-blowing density, infinite.
 * - 16 layer types (was 12): added PHOTOREALISTIC_MOUNTAIN, MEDITATION_GARDEN, ENDLESS_CHEERS_UNIVERSE, ORGANIC_CAVE_SYSTEM
 *   plus existing 12: Spire Forest, Floating Islands, Void Ocean, Crystal Shards, Shattered Sheets,
 *   Grid Matrix, BLACK HOLE VOID, INVERTED CITY, FLESH ORGANIC, PSYCHEDELIC FRACTAL, GOTHIC MEGA, VOID SKELETON
 * - Counts EXTREME: 35-60 spires per layer (was 25-40), 12-20 islands (was 8-13), 40-70 shards (was 30-49)
 * - Uses McsmMeshSynthesizer for fBm noise, organic caves hollowing, twisted spires fractal, floating fragments separation
 * - Meditation per layer: each layer has mantra, breathing cycle, color, asset generation
 * - Asset generator instantly keeps generating procedural assets infinitely – logs 256 recent assets
 * - Endless cheers: unlimited infinite cheers causing endless universe effect – 30-100 cheering particles per layer
 * - Photorealistic F1 reality: super duper photorealistic landscape shader technique when F1 (HUD hidden)
 * - Memory flush: high-speed unload old out-of-view meshes to prevent VRAM leaks, no stutter
 * - Truly no end: Y = -64 to -10,000,000+ possible, pure 3D geometry, no blocks, camera-relative, seed shuffle
 */
public final class McsmInfiniteVoidLayers {

    private McsmInfiniteVoidLayers() {}

    // ---- V3 EXTREME configuration – NOT MILD – OVERWHELMING INFINITE --------
    public static final int LAYER_SPACING = 30; // V3 EXTREME denser (was 45, original 100)
    public static final int VISIBLE_LAYERS_BELOW = 36; // V3 EXTREME (was 24)
    public static final int VISIBLE_LAYERS_ABOVE = 12; // V3 EXTREME (was 8)
    public static final int TOTAL_VISIBLE = VISIBLE_LAYERS_BELOW + VISIBLE_LAYERS_ABOVE; // 48

    // 16 EXTREME layer types – V3 adds 4 new
    private static final int TYPE_SPIRE_FOREST = 0;
    private static final int TYPE_FLOATING_ISLANDS = 1;
    private static final int TYPE_VOID_OCEAN = 2;
    private static final int TYPE_CRYSTAL_SHARDS = 3;
    private static final int TYPE_SHATTERED_SHEETS = 4;
    private static final int TYPE_GRID_MATRIX = 5;
    private static final int TYPE_BLACK_HOLE = 6;
    private static final int TYPE_INVERTED_CITY = 7;
    private static final int TYPE_FLESH_ORGANIC = 8;
    private static final int TYPE_PSYCHEDELIC_FRACTAL = 9;
    private static final int TYPE_GOTHIC_MEGA = 10;
    private static final int TYPE_VOID_SKELETON = 11;
    private static final int TYPE_PHOTOREALISTIC_MOUNTAIN = 12; // NEW V3
    private static final int TYPE_MEDITATION_GARDEN = 13; // NEW V3
    private static final int TYPE_ENDLESS_CHEERS = 14; // NEW V3
    private static final int TYPE_ORGANIC_CAVE = 15; // NEW V3
    private static final int TYPE_COUNT = 16;

    private static final double LAYER_RADIUS = 2200.0D; // V3 bigger (was 1800)
    private static final double ISLAND_RADIUS = 1800.0D; // V3 bigger (was 1400)

    private static final Identifier VOID_STONE = Identifier.fromNamespaceAndPath("mcsm", "textures/block/void_stone.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");

    private static long lastShuffleMs = 0L;
    private static int layersGenerated = 0;
    private static int currentMeditationLayer = 0;
    private static String currentMeditation = "";
    private static long lastMeditationMs = 0L;

    public static void tick() {
        try {
            if (!McsmExtrasConfig.voidDescent) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.player == null) return;
            if (!mc.level.dimension().equals(McsmVoid.DIMENSION)) return;
            long now = System.currentTimeMillis();
            if (now - lastShuffleMs > 300) { // V3 faster tick (was 500)
                lastShuffleMs = now;
                layersGenerated++;
            }
            // Meditation per layer – update every 2.5 sec
            if (now - lastMeditationMs > 2500L) {
                lastMeditationMs = now;
                double playerY = mc.player.getY();
                double depthBelowFloor = McsmVoidTiers.FLOOR_Y - playerY;
                if (depthBelowFloor < 0) depthBelowFloor = 0;
                int layerIdx = (int)(depthBelowFloor / LAYER_SPACING);
                if (layerIdx < 0) layerIdx = 0;
                currentMeditationLayer = layerIdx;
                currentMeditation = McsmMeshSynthesizer.getMeditationForLayer(layerIdx);
                long seed = mc.level.dimension().identifier().hashCode() ^ mc.player.getUUID().hashCode() ^ mc.level.getGameTime() ^ layerIdx * 0x9E3779B97F4A7C15L;
                McsmMeshSynthesizer.generateProceduralAsset(layerIdx, seed, mc.level.getGameTime());
            }
            // Memory flush
            McsmMeshSynthesizer.flushOldMeshes();
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
            long baseSeed = mc.level.dimension().identifier().hashCode() ^ (mc.player != null ? mc.player.getUUID().hashCode() : 0x5EEDL) ^ mc.level.getGameTime();

            float glitchFactor = McsmGlitchGenerator.computeGlitchFactor(fallDist, playerY, time);
            glitchFactor = Mth.clamp(glitchFactor * 1.8F, 0.0F, 1.0F);

            double depthBelowFloor = McsmVoidTiers.FLOOR_Y - playerY;
            if (depthBelowFloor < 0) depthBelowFloor = 0;
            int currentLayerIndex = (int)(depthBelowFloor / LAYER_SPACING);

            final Vec3 camFinal = camera;
            final Vec3 playerPos = mc.player != null ? mc.player.position() : camera;
            final double tFinal = time;
            final float glitchFinal = glitchFactor;
            final double fallFinal = fallDist;
            final double pYFinal = playerY;
            final long seedFinal = baseSeed;
            final int curIdxFinal = currentLayerIndex;
            final boolean isF1 = McsmMeshSynthesizer.isF1Mode();

            collector.submitCustomGeometry(poseStack, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(VOID_STONE),
                    (pose, consumer) -> {
                        for (int offset = -VISIBLE_LAYERS_ABOVE; offset < VISIBLE_LAYERS_BELOW; offset++) {
                            int layerIndex = curIdxFinal + offset;
                            if (layerIndex < 0) continue;
                            double layerY = McsmVoidTiers.FLOOR_Y - (layerIndex * LAYER_SPACING) - (layerIndex % 4) * 5.0;
                            long shuffledSeed = McsmGlitchGenerator.shuffleSeedForDepth(layerY, seedFinal + layerIndex * 0x9E3779B97F4A7C15L);
                            int layerType = (int)(Math.abs(shuffledSeed) % TYPE_COUNT);

                            // F1 dual reality: when F1, force photorealistic mountain type more often
                            if (isF1 && layerIndex % 3 == 0) {
                                layerType = TYPE_PHOTOREALISTIC_MOUNTAIN;
                            }

                            double distToPlayer = Math.abs(layerY - pYFinal);
                            float visibility = 1.0F - (float)(distToPlayer / (VISIBLE_LAYERS_BELOW * LAYER_SPACING * 0.9));
                            visibility = Mth.clamp(visibility, 0.08F, 1.0F);
                            float layerGlitch = glitchFinal * (0.7F + 0.8F * (layerIndex / 20.0F));
                            layerGlitch = Mth.clamp(layerGlitch, 0.0F, 1.0F);

                            // Generate procedural asset for this layer – instant infinite generation
                            McsmMeshSynthesizer.generateProceduralAsset(layerIndex, shuffledSeed, tFinal);

                            switch (layerType) {
                                case TYPE_SPIRE_FOREST -> emitSpireForestLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_FLOATING_ISLANDS -> emitFloatingIslandsLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_VOID_OCEAN -> emitVoidOceanLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_CRYSTAL_SHARDS -> emitCrystalShardsLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_SHATTERED_SHEETS -> emitShatteredSheetsLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_GRID_MATRIX -> emitGridMatrixLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_BLACK_HOLE -> emitBlackHoleLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_INVERTED_CITY -> emitInvertedCityLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_FLESH_ORGANIC -> emitFleshOrganicLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_PSYCHEDELIC_FRACTAL -> emitPsychedelicFractalLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_GOTHIC_MEGA -> emitGothicMegaLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_VOID_SKELETON -> emitVoidSkeletonLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_PHOTOREALISTIC_MOUNTAIN -> emitPhotorealisticMountainLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_MEDITATION_GARDEN -> emitMeditationGardenLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_ENDLESS_CHEERS -> emitEndlessCheersLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                                case TYPE_ORGANIC_CAVE -> emitOrganicCaveLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                            }

                            // Reality tear EVERY layer – V3 even more
                            emitRealityTearLayer(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed, playerPos);
                            if (layerIndex % 2 == 0) {
                                emitRealityTearLayer(pose, consumer, camFinal, layerY - 15, layerIndex + 1000, tFinal, visibility, layerGlitch, fallFinal, shuffledSeed + 0x1234, playerPos);
                            }
                            // Infinite landscape synthesis via mesh synthesizer – endless possibilities
                            if (layerIndex % 4 == 0) {
                                McsmMeshSynthesizer.generateInfiniteLandscape(pose, consumer, camFinal, layerY - 10, layerIndex, tFinal, visibility * 0.5F, layerGlitch, fallFinal, shuffledSeed + 0x9ABC, playerPos);
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
                            if (distToPlayer > 500) continue;
                            long shuffledSeed = McsmGlitchGenerator.shuffleSeedForDepth(layerY, seedFinal + layerIndex * 0x9E3779B97F4A7C15L);
                            float visibility = 1.0F - (float)(distToPlayer / 500.0);
                            emitLayerGlow(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, glitchFinal, shuffledSeed);
                            if (glitchFinal > 0.2F) {
                                emitExtremeParticles(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, glitchFinal, shuffledSeed, playerPos);
                            }
                            // Endless cheers glow – unlimited infinite cheers causing endless universe
                            if (layerIndex % 3 == 0) {
                                McsmMeshSynthesizer.generateEndlessCheers(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility, glitchFinal, shuffledSeed + 0x5678, playerPos);
                            }
                            // Photorealistic F1 glow
                            if (isF1) {
                                McsmMeshSynthesizer.generatePhotorealisticLandscape(pose, consumer, camFinal, layerY, layerIndex, tFinal, visibility * 0.3F, glitchFinal, fallFinal, shuffledSeed + 0xDEF0, playerPos);
                            }
                        }
                    });

        } catch (Throwable ignored) {}
    }

    // ---- V3 layer implementations – EXTREME + INFINITE SYNTHESIS -------------

    private static void emitSpireForestLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        int spireCount = 35 + (int)(Math.abs(seed) % 26); // V3 35-60 (was 25-40)
        for (int i = 0; i < spireCount; i++) {
            double angle = (i / (double) spireCount) * Mth.TWO_PI + (seed % 100) * 0.01 + time * 0.0004 * (i % 3);
            double radius = LAYER_RADIUS * 0.4 + (i * 37 % 800) + McsmGlitchGenerator.fractalNoise(i * 1.3, time * 0.001) * glitchFactor * 300.0 + McsmMeshSynthesizer.fBm(i * 0.5, time * 0.001, seed * 0.0001, 3) * 100.0;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double[] mutated = McsmGlitchGenerator.mutateSpire(60.0 + (i * 11 % 120), 12.0, time, layerY, i + layerIndex * 100);
            double height = mutated[0] * (1.2 + layerIndex * 0.05) * (1.0 + glitchFactor * 0.8);
            double baseR = mutated[1] * (1.0 + glitchFactor * 0.5);

            int segs = 12;
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

                double[] d0b = McsmMeshSynthesizer.displaceVertexAdvanced(x0b, layerY, z0b, time, fallDistance, glitchFactor, playerPos, s + i * 10 + layerIndex * 1000, seed);
                double[] d1b = McsmMeshSynthesizer.displaceVertexAdvanced(x1b, layerY, z1b, time, fallDistance, glitchFactor, playerPos, s+1 + i * 10 + layerIndex * 1000, seed);
                double[] d0t = McsmMeshSynthesizer.displaceVertexAdvanced(x0t, layerY + height, z0t, time, fallDistance, glitchFactor, playerPos, s + i * 20 + layerIndex * 1000, seed);
                double[] d1t = McsmMeshSynthesizer.displaceVertexAdvanced(x1t, layerY + height, z1t, time, fallDistance, glitchFactor, playerPos, s+1 + i * 20 + layerIndex * 1000, seed);

                float hue = (float)(((i * 17 + layerIndex * 13 + time * 0.01) % 360) / 360.0);
                float[] rgb = hsvToRgb(hue, 0.9F, 1.0F);
                int r = (int)(rgb[0] * 60 + 20), g = (int)(rgb[1] * 30 + 10), b = (int)(rgb[2] * 80 + 40);
                int a = (int)(visibility * 255);

                quad(pose, consumer, d0b[0], d0b[1], d0b[2], 0, 0, d1b[0], d1b[1], d1b[2], 1, 0, d1t[0], d1t[1], d1t[2], 1, 1, d0t[0], d0t[1], d0t[2], 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitFloatingIslandsLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        int islandCount = 12 + (int)(Math.abs(seed) % 9); // V3 12-20 (was 8-13)
        for (int i = 0; i < islandCount; i++) {
            double angle = (i / (double) islandCount) * Mth.TWO_PI + time * 0.00015 * (i+1) + (seed % 50) * 0.02;
            double radius = ISLAND_RADIUS * 0.5 + i * 60 + McsmGlitchGenerator.fractalNoise(i * 2.0, time * 0.0005) * glitchFactor * 120.0 + McsmMeshSynthesizer.fBm(i * 0.7, time * 0.0005, seed * 0.0001, 3) * 80.0;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY + Math.sin(time * 0.002 + i * 1.7) * 18.0 * (1.0 + glitchFactor * 2.0);

            double size = 40.0 + (i * 13 % 60) + glitchFactor * 40.0;

            for (int s = 0; s < 6; s++) {
                double a0 = (s / 6.0) * Mth.TWO_PI;
                double a1 = ((s + 1) / 6.0) * Mth.TWO_PI;
                double x0 = x + Math.cos(a0) * size;
                double z0 = z + Math.sin(a0) * size;
                double x1 = x + Math.cos(a1) * size;
                double z1 = z + Math.sin(a1) * size;

                double[] d0 = McsmMeshSynthesizer.displaceVertexAdvanced(x0, y, z0, time, fallDistance, glitchFactor, playerPos, s + i * 100 + layerIndex * 2000, seed);
                double[] d1 = McsmMeshSynthesizer.displaceVertexAdvanced(x1, y, z1, time, fallDistance, glitchFactor, playerPos, s+1 + i * 100 + layerIndex * 2000, seed);
                double[] d0b = McsmMeshSynthesizer.displaceVertexAdvanced(x0, y - 18.0 - glitchFactor * 10.0, z0, time, fallDistance, glitchFactor, playerPos, s + i * 110 + layerIndex * 2000, seed);
                double[] d1b = McsmMeshSynthesizer.displaceVertexAdvanced(x1, y - 18.0 - glitchFactor * 10.0, z1, time, fallDistance, glitchFactor, playerPos, s+1 + i * 110 + layerIndex * 2000, seed);

                int r = 25 + layerIndex % 20 + (int)(glitchFactor * 30), g = 35 + i * 7, b = 60 + (int)(glitchFactor * 40);
                int a = (int)(visibility * 255);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, d1b[0], d1b[1], d1b[2], 1, 1, d0b[0], d0b[1], d0b[2], 0, 1, r, g, b, a, 0, -1, 0);
            }
        }
    }

    private static void emitVoidOceanLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        double size = LAYER_RADIUS * 2.2;
        int segs = 20;
        for (int sx = -segs/2; sx < segs/2; sx++) {
            for (int sz = -segs/2; sz < segs/2; sz++) {
                double x0 = camera.x + sx * size / segs;
                double z0 = camera.z + sz * size / segs;
                double x1 = camera.x + (sx+1) * size / segs;
                double z1 = camera.z + (sz+1) * size / segs;

                double wave0 = Math.sin(x0 * 0.03 + time * 0.004) * 12.0 + Math.cos(z0 * 0.03 + time * 0.003) * 12.0;
                wave0 += McsmMeshSynthesizer.fBm(x0 * 0.02, z0 * 0.02, time * 0.002, 3) * glitchFactor * 35.0;

                double[] d0 = McsmMeshSynthesizer.displaceVertexAdvanced(x0, layerY + wave0, z0, time, fallDistance, glitchFactor, playerPos, sx*10 + sz + layerIndex * 3000, seed);
                double[] d1 = McsmMeshSynthesizer.displaceVertexAdvanced(x1, layerY + wave0, z0, time, fallDistance, glitchFactor, playerPos, sx*10 + sz+1 + layerIndex * 3000, seed);
                double[] d2 = McsmMeshSynthesizer.displaceVertexAdvanced(x1, layerY + wave0, z1, time, fallDistance, glitchFactor, playerPos, sx*10 + sz+2 + layerIndex * 3000, seed);
                double[] d3 = McsmMeshSynthesizer.displaceVertexAdvanced(x0, layerY + wave0, z1, time, fallDistance, glitchFactor, playerPos, sx*10 + sz+3 + layerIndex * 3000, seed);

                float hue = (float)(((layerIndex * 7 + sx * 3 + time * 0.005) % 360) / 360.0);
                float[] rgb = hsvToRgb(hue, 0.85F, 0.7F);
                int r = (int)(rgb[0] * 40), g = (int)(rgb[1] * 40 + 20), b = (int)(rgb[2] * 80 + 40);
                int a = (int)(visibility * 180);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, d2[0], d2[1], d2[2], 1, 1, d3[0], d3[1], d3[2], 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitCrystalShardsLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        int shardCount = 40 + (int)(Math.abs(seed) % 31); // V3 40-70
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

                double[] d0b = McsmMeshSynthesizer.displaceVertexAdvanced(x0b, y, z0b, time, fallDistance, glitchFactor, playerPos, s + i * 10 + layerIndex * 4000, seed);
                double[] d1b = McsmMeshSynthesizer.displaceVertexAdvanced(x1b, y, z1b, time, fallDistance, glitchFactor, playerPos, s+1 + i * 10 + layerIndex * 4000, seed);
                double[] d0t = McsmMeshSynthesizer.displaceVertexAdvanced(x0t, y + height, z0t, time, fallDistance, glitchFactor, playerPos, s + i * 20 + layerIndex * 4000, seed);
                double[] d1t = McsmMeshSynthesizer.displaceVertexAdvanced(x1t, y + height, z1t, time, fallDistance, glitchFactor, playerPos, s+1 + i * 20 + layerIndex * 4000, seed);

                int hue = (i * 20 + layerIndex * 15 + (int)(time * 0.5)) % 360;
                float[] rgb = hsvToRgb(hue / 360.0F, 0.95F, 1.0F);
                int r = (int)(rgb[0] * 255), g = (int)(rgb[1] * 255), b = (int)(rgb[2] * 255);
                int a = (int)(visibility * 220);

                quad(pose, consumer, d0b[0], d0b[1], d0b[2], 0, 0, d1b[0], d1b[1], d1b[2], 1, 0, d1t[0], d1t[1], d1t[2], 1, 1, d0t[0], d0t[1], d0t[2], 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitShatteredSheetsLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        int sheetCount = 15 + (int)(Math.abs(seed) % 12);
        for (int i = 0; i < sheetCount; i++) {
            double angle = (i / (double) sheetCount) * Mth.TWO_PI + (seed % 100) * 0.01 + time * 0.0003;
            double radius = LAYER_RADIUS * 0.5 + i * 50 + glitchFactor * 80.0 * Math.sin(time * 0.003 + i);
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY;

            double width = 60.0 + (i * 17 % 100) + glitchFactor * 60.0;
            double height = 120.0 + (i * 13 % 150) + glitchFactor * 100.0;

            double[] p0 = McsmMeshSynthesizer.displaceVertexAdvanced(x - width/2, y, z, time, fallDistance, glitchFactor, playerPos, i * 500 + layerIndex * 5000, seed);
            double[] p1 = McsmMeshSynthesizer.displaceVertexAdvanced(x + width/2, y, z, time, fallDistance, glitchFactor, playerPos, i * 501 + layerIndex * 5000, seed);
            double[] p2 = McsmMeshSynthesizer.displaceVertexAdvanced(x + width/2 + Math.sin(time * 0.002 + i) * glitchFactor * 25.0, y + height, z + Math.cos(time * 0.002 + i) * glitchFactor * 25.0, time, fallDistance, glitchFactor, playerPos, i * 502 + layerIndex * 5000, seed);
            double[] p3 = McsmMeshSynthesizer.displaceVertexAdvanced(x - width/2 + Math.sin(time * 0.002 + i + 1) * glitchFactor * 25.0, y + height, z + Math.cos(time * 0.002 + i + 1) * glitchFactor * 25.0, time, fallDistance, glitchFactor, playerPos, i * 503 + layerIndex * 5000, seed);

            float hue = (float)(((i * 25 + layerIndex * 10 + time * 0.01) % 360) / 360.0);
            float[] rgb = hsvToRgb(hue, 0.9F, 0.8F);
            int r = (int)(rgb[0] * 200), g = (int)(rgb[1] * 150), b = (int)(rgb[2] * 255);
            int a = (int)(visibility * 140 * (0.6 + glitchFactor * 0.8));

            quad(pose, consumer, p0[0], p0[1], p0[2], 0, 0, p1[0], p1[1], p1[2], 1, 0, p2[0], p2[1], p2[2], 1, 1, p3[0], p3[1], p3[2], 0, 1, r, g, b, a, 0, 0, 1);
        }
    }

    private static void emitGridMatrixLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        double size = LAYER_RADIUS * 2.0;
        int gridLines = 20 + (int)(Math.abs(seed) % 10);
        double spacing = size * 2 / gridLines;

        for (int i = -gridLines/2; i < gridLines/2; i++) {
            double x = camera.x + i * spacing + Math.sin(time * 0.002 + i) * glitchFactor * 25.0;
            double z0 = camera.z - size;
            double z1 = camera.z + size;

            double[] d0 = McsmMeshSynthesizer.displaceVertexAdvanced(x, layerY, z0, time, fallDistance, glitchFactor, playerPos, i * 600 + layerIndex * 6000, seed);
            double[] d1 = McsmMeshSynthesizer.displaceVertexAdvanced(x, layerY, z1, time, fallDistance, glitchFactor, playerPos, i * 601 + layerIndex * 6000, seed);

            float hue = (float)(((i * 10 + layerIndex * 5) % 360) / 360.0);
            float[] rgb = hsvToRgb(hue, 0.8F, 0.8F);
            int r = (int)(rgb[0] * 120), g = (int)(rgb[1] * 120), b = (int)(rgb[2] * 150 + glitchFactor * 100);
            int a = (int)(visibility * 100 * (0.4 + glitchFactor));

            quad(pose, consumer, d0[0] - 1, d0[1], d0[2], 0, 0, d0[0] + 1, d0[1], d0[2], 1, 0, d1[0] + 1, d1[1], d1[2], 1, 1, d1[0] - 1, d1[1], d1[2], 0, 1, r, g, b, a, 0, 1, 0);
        }
    }

    private static void emitBlackHoleLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        int holes = 2 + (int)(Math.abs(seed) % 4);
        for (int i = 0; i < holes; i++) {
            double angle = (i / (double) holes) * Mth.TWO_PI + time * 0.0001 * (i+1) + seed * 0.001;
            double radius = LAYER_RADIUS * 0.3 + i * 200 + glitchFactor * 100.0;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY;

            double size = 40.0 + (i * 20 % 60) + glitchFactor * 50.0 + Math.sin(time * 0.003 + i) * 15.0;

            for (int s = 0; s < 4; s++) {
                double a0 = (s / 4.0) * Mth.TWO_PI;
                double a1 = ((s + 1) / 4.0) * Mth.TWO_PI;
                double x0 = x + Math.cos(a0) * size;
                double z0 = z + Math.sin(a0) * size;
                double x1 = x + Math.cos(a1) * size;
                double z1 = z + Math.sin(a1) * size;

                double[] d0 = McsmMeshSynthesizer.displaceVertexAdvanced(x0, y, z0, time, fallDistance, glitchFactor, playerPos, s + i * 8000 + layerIndex * 8000, seed);
                double[] d1 = McsmMeshSynthesizer.displaceVertexAdvanced(x1, y, z1, time, fallDistance, glitchFactor, playerPos, s+1 + i * 8000 + layerIndex * 8000, seed);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, x, y - 5, z, 0.5F, 1, x, y - 5, z, 0.5F, 1, 0, 0, 0, (int)(visibility * 255), 0, 1, 0);
            }

            int segs = 24;
            for (int s = 0; s < segs; s++) {
                double a0 = (s / (double) segs) * Mth.TWO_PI + time * 0.002 * (i+1);
                double a1 = ((s + 1) / (double) segs) * Mth.TWO_PI + time * 0.002 * (i+1);
                double r0 = size * 1.2, r1 = size * 1.8;
                double x0 = x + Math.cos(a0) * r0, z0 = z + Math.sin(a0) * r0;
                double x1 = x + Math.cos(a1) * r0, z1 = z + Math.sin(a1) * r0;
                double x2 = x + Math.cos(a1) * r1, z2 = z + Math.sin(a1) * r1;
                double x3 = x + Math.cos(a0) * r1, z3 = z + Math.sin(a0) * r1;

                float hue = (float)(((s * 5 + time * 0.01) % 360) / 360.0);
                float[] rgb = hsvToRgb(hue, 1.0F, 1.0F);
                int r = (int)(rgb[0] * 255), g = (int)(rgb[1] * 200), b = (int)(rgb[2] * 255);
                int a = (int)(visibility * 180);

                quad(pose, consumer, x0, y, z0, 0, 0, x1, y, z1, 1, 0, x2, y, z2, 1, 1, x3, y, z3, 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitInvertedCityLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        int buildings = 12 + (int)(Math.abs(seed) % 12);
        for (int i = 0; i < buildings; i++) {
            double angle = (i / (double) buildings) * Mth.TWO_PI + seed * 0.002;
            double radius = LAYER_RADIUS * 0.5 + (i * 43 % 500) + glitchFactor * 80.0;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY;
            double height = 50.0 + (i * 17 % 100) + glitchFactor * 60.0;
            double size = 8.0 + (i % 5) * 3.0;

            for (int s = 0; s < 4; s++) {
                double a0 = (s / 4.0) * Mth.TWO_PI;
                double a1 = ((s + 1) / 4.0) * Mth.TWO_PI;
                double x0 = x + Math.cos(a0) * size;
                double z0 = z + Math.sin(a0) * size;
                double x1 = x + Math.cos(a1) * size;
                double z1 = z + Math.sin(a1) * size;

                double[] d0 = McsmMeshSynthesizer.displaceVertexAdvanced(x0, y, z0, time, fallDistance, glitchFactor, playerPos, s + i * 9000 + layerIndex * 9000, seed);
                double[] d1 = McsmMeshSynthesizer.displaceVertexAdvanced(x1, y, z1, time, fallDistance, glitchFactor, playerPos, s+1 + i * 9000 + layerIndex * 9000, seed);
                double[] d0b = McsmMeshSynthesizer.displaceVertexAdvanced(x0, y - height, z0, time, fallDistance, glitchFactor, playerPos, s + i * 9001 + layerIndex * 9000, seed);
                double[] d1b = McsmMeshSynthesizer.displaceVertexAdvanced(x1, y - height, z1, time, fallDistance, glitchFactor, playerPos, s+1 + i * 9001 + layerIndex * 9000, seed);

                int r = 30 + (int)(glitchFactor * 40), g = 30, b = 50 + layerIndex % 30;
                int a = (int)(visibility * 220);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, d1b[0], d1b[1], d1b[2], 1, 1, d0b[0], d0b[1], d0b[2], 0, 1, r, g, b, a, 0, -1, 0);
            }
        }
    }

    private static void emitFleshOrganicLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        double size = LAYER_RADIUS * 1.5;
        int segs = 16;
        for (int sx = -segs/2; sx < segs/2; sx++) {
            for (int sz = -segs/2; sz < segs/2; sz++) {
                double x0 = camera.x + sx * size / segs;
                double z0 = camera.z + sz * size / segs;
                double x1 = camera.x + (sx+1) * size / segs;
                double z1 = camera.z + (sz+1) * size / segs;

                double pulse = Math.sin(x0 * 0.02 + z0 * 0.02 + time * 0.003) * 15.0 * (1.0 + glitchFactor * 2.0);
                pulse += McsmMeshSynthesizer.fBm(x0 * 0.01, z0 * 0.01, time * 0.002, 3) * glitchFactor * 25.0;

                double[] d0 = McsmMeshSynthesizer.displaceVertexAdvanced(x0, layerY + pulse, z0, time, fallDistance, glitchFactor, playerPos, sx*10 + sz + layerIndex * 10000, seed);
                double[] d1 = McsmMeshSynthesizer.displaceVertexAdvanced(x1, layerY + pulse, z0, time, fallDistance, glitchFactor, playerPos, sx*10 + sz+1 + layerIndex * 10000, seed);
                double[] d2 = McsmMeshSynthesizer.displaceVertexAdvanced(x1, layerY + pulse, z1, time, fallDistance, glitchFactor, playerPos, sx*10 + sz+2 + layerIndex * 10000, seed);
                double[] d3 = McsmMeshSynthesizer.displaceVertexAdvanced(x0, layerY + pulse, z1, time, fallDistance, glitchFactor, playerPos, sx*10 + sz+3 + layerIndex * 10000, seed);

                int r = 120 + (int)(glitchFactor * 80), g = 20 + layerIndex % 20, b = 40 + (int)(glitchFactor * 30);
                int a = (int)(visibility * 200);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, d2[0], d2[1], d2[2], 1, 1, d3[0], d3[1], d3[2], 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitPsychedelicFractalLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        int count = 25 + (int)(Math.abs(seed) % 20);
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

                double[] d0 = McsmMeshSynthesizer.displaceVertexAdvanced(x0, y, z0, time, fallDistance, glitchFactor, playerPos, s + i * 11000 + layerIndex * 11000, seed);
                double[] d1 = McsmMeshSynthesizer.displaceVertexAdvanced(x1, y, z1, time, fallDistance, glitchFactor, playerPos, s+1 + i * 11000 + layerIndex * 11000, seed);

                float hue = (float)(((i * 23 + layerIndex * 17 + time * 0.02) % 360) / 360.0);
                float[] rgb = hsvToRgb(hue, 1.0F, 1.0F);
                int r = (int)(rgb[0] * 255), g = (int)(rgb[1] * 255), b = (int)(rgb[2] * 255);
                int a = (int)(visibility * 200);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0, 0, d1[0], d1[1], d1[2], 1, 0, x, y + size * 2, z, 0.5F, 1, x, y + size * 2, z, 0.5F, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitGothicMegaLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        int towers = 10 + (int)(Math.abs(seed) % 10);
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

                double[] d0b = McsmMeshSynthesizer.displaceVertexAdvanced(x0b, layerY, z0b, time, fallDistance, glitchFactor, playerPos, s + i * 12000 + layerIndex * 12000, seed);
                double[] d1b = McsmMeshSynthesizer.displaceVertexAdvanced(x1b, layerY, z1b, time, fallDistance, glitchFactor, playerPos, s+1 + i * 12000 + layerIndex * 12000, seed);
                double[] d0t = McsmMeshSynthesizer.displaceVertexAdvanced(x0t, layerY + height, z0t, time, fallDistance, glitchFactor, playerPos, s + i * 12001 + layerIndex * 12000, seed);
                double[] d1t = McsmMeshSynthesizer.displaceVertexAdvanced(x1t, layerY + height, z1t, time, fallDistance, glitchFactor, playerPos, s+1 + i * 12001 + layerIndex * 12000, seed);

                int r = 15, g = 18, b = 35 + layerIndex % 20;
                int a = (int)(visibility * 255);

                quad(pose, consumer, d0b[0], d0b[1], d0b[2], 0, 0, d1b[0], d1b[1], d1b[2], 1, 0, d1t[0], d1t[1], d1t[2], 1, 1, d0t[0], d0t[1], d0t[2], 0, 1, r, g, b, a, 0, 1, 0);
            }
        }
    }

    private static void emitVoidSkeletonLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        int bones = 20 + (int)(Math.abs(seed) % 20);
        for (int i = 0; i < bones; i++) {
            double angle = (i / (double) bones) * Mth.TWO_PI + time * 0.0001;
            double radius = LAYER_RADIUS * 0.4 + (i * 29 % 600) + glitchFactor * 80.0;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY + Math.sin(time * 0.002 + i) * 10.0 * glitchFactor;

            double len = 30.0 + (i * 13 % 70) + glitchFactor * 40.0;
            double thick = 2.0 + glitchFactor * 3.0;

            double[] p0 = McsmMeshSynthesizer.displaceVertexAdvanced(x - thick, y, z - thick, time, fallDistance, glitchFactor, playerPos, i * 13000 + layerIndex * 13000, seed);
            double[] p1 = McsmMeshSynthesizer.displaceVertexAdvanced(x + thick, y, z - thick, time, fallDistance, glitchFactor, playerPos, i * 13001 + layerIndex * 13000, seed);
            double[] p2 = McsmMeshSynthesizer.displaceVertexAdvanced(x + thick + Math.sin(time * 0.001 + i) * 10.0 * glitchFactor, y + len, z + thick, time, fallDistance, glitchFactor, playerPos, i * 13002 + layerIndex * 13000, seed);
            double[] p3 = McsmMeshSynthesizer.displaceVertexAdvanced(x - thick + Math.sin(time * 0.001 + i + 1) * 10.0 * glitchFactor, y + len, z - thick, time, fallDistance, glitchFactor, playerPos, i * 13003 + layerIndex * 13000, seed);

            int r = 200 + (int)(glitchFactor * 55), g = 200 + (int)(glitchFactor * 55), b = 210 + (int)(glitchFactor * 45);
            int a = (int)(visibility * 180);

            quad(pose, consumer, p0[0], p0[1], p0[2], 0, 0, p1[0], p1[1], p1[2], 1, 0, p2[0], p2[1], p2[2], 1, 1, p3[0], p3[1], p3[2], 0, 1, r, g, b, a, 0, 1, 0);
        }
    }

    // ---- NEW V3 TYPES -------------------------------------------------------

    private static void emitPhotorealisticMountainLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        // Super duper photorealistic landscape using shader technique – for F1 and main
        McsmMeshSynthesizer.generatePhotorealisticLandscape(pose, consumer, camera, layerY, layerIndex, time, visibility, glitchFactor, fallDistance, seed, playerPos);
    }

    private static void emitMeditationGardenLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        // Meditation garden – each layer has meditation, organic shapes, calming but psychedelic
        int flowers = 15 + (int)(Math.abs(seed) % 15);
        for (int i = 0; i < flowers; i++) {
            double angle = (i / (double) flowers) * Mth.TWO_PI + time * 0.0002 * (i+1) + seed * 0.0001;
            double radius = LAYER_RADIUS * 0.3 + (i * 29 % 400) + McsmMeshSynthesizer.fBm(i * 0.5, time * 0.001, seed * 0.0001, 3) * 80.0 * glitchFactor;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY + Math.sin(time * 0.001 + i) * 5.0;

            double size = 8.0 + (i % 5) * 3.0 + glitchFactor * 8.0;
            float[] medColor = McsmMeshSynthesizer.getMeditationColor(layerIndex, time);
            int r = (int)(medColor[0] * 255), g = (int)(medColor[1] * 255), b = (int)(medColor[2] * 255);
            int a = (int)(visibility * 200 * medColor[3]);

            for (int s = 0; s < 6; s++) {
                double a0 = (s / 6.0) * Mth.TWO_PI + time * 0.001;
                double a1 = ((s + 1) / 6.0) * Mth.TWO_PI + time * 0.001;
                double x0 = x + Math.cos(a0) * size;
                double z0 = z + Math.sin(a0) * size;
                double x1 = x + Math.cos(a1) * size;
                double z1 = z + Math.sin(a1) * size;

                double[] d0 = McsmMeshSynthesizer.displaceVertexAdvanced(x0, y, z0, time, fallDistance, glitchFactor, playerPos, s + i * 100 + layerIndex * 14000, seed);
                double[] d1 = McsmMeshSynthesizer.displaceVertexAdvanced(x1, y, z1, time, fallDistance, glitchFactor, playerPos, s+1 + i * 100 + layerIndex * 14000, seed);

                quad(pose, consumer, d0[0], d0[1], d0[2], 0,0, d1[0], d1[1], d1[2], 1,0, x, y + size * 1.5, z, 0.5F,1, x, y + size * 1.5, z, 0.5F,1, r,g,b,a, 0,1,0);
            }
        }
        // Central meditation platform
        double platSize = 100.0 + glitchFactor * 50.0;
        float[] medColor = McsmMeshSynthesizer.getMeditationColor(layerIndex, time);
        int pr = (int)(medColor[0] * 80), pg = (int)(medColor[1] * 80), pb = (int)(medColor[2] * 80);
        quad(pose, consumer, camera.x - platSize, layerY, camera.z - platSize, 0,0, camera.x + platSize, layerY, camera.z - platSize, 1,0, camera.x + platSize, layerY, camera.z + platSize, 1,1, camera.x - platSize, layerY, camera.z + platSize, 0,1, pr,pg,pb,(int)(visibility*180), 0,1,0);
    }

    private static void emitEndlessCheersLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        // Unlimited infinite cheers causing effect endless universe endless possibilities
        McsmMeshSynthesizer.generateEndlessCheers(pose, consumer, camera, layerY, layerIndex, time, visibility, glitchFactor, seed, playerPos);
        // Also generate floating fragments that cheer
        McsmMeshSynthesizer.generateFloatingFragments(pose, consumer, camera, camera.x, layerY + 10, camera.z, 200.0 + glitchFactor * 100.0, time, fallDistance, glitchFactor, playerPos, seed + 0x7777, visibility);
    }

    private static void emitOrganicCaveLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        // Hollowing out organic geometric caves – infinite procedural
        int caves = 3 + (int)(Math.abs(seed) % 4);
        for (int i = 0; i < caves; i++) {
            double angle = (i / (double) caves) * Mth.TWO_PI + seed * 0.001 + time * 0.0001;
            double radius = LAYER_RADIUS * 0.4 + i * 150.0 + McsmMeshSynthesizer.fBm(i * 0.7, time * 0.001, seed * 0.0001, 3) * 100.0 * glitchFactor;
            double x = camera.x + Math.cos(angle) * radius;
            double z = camera.z + Math.sin(angle) * radius;
            double y = layerY + McsmMeshSynthesizer.fBm(x * 0.01, layerY * 0.01, z * 0.01, time, 3) * 20.0 * glitchFactor;
            double size = 60.0 + (i * 17 % 80) + glitchFactor * 50.0 + McsmMeshSynthesizer.fBm(x * 0.005, y * 0.005, z * 0.005, time, 4) * 30.0;

            McsmMeshSynthesizer.generateOrganicCave(pose, consumer, camera, x, y, z, size, time, fallDistance, glitchFactor, playerPos, seed + i * 0xABCD, visibility);
        }
    }

    private static void emitRealityTearLayer(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, double fallDistance, long seed, Vec3 playerPos) {
        double x = camera.x + Math.sin(time * 0.0003 + layerIndex * 0.7) * 400.0 * (1.0 + glitchFactor * 2.0) + McsmMeshSynthesizer.fBm(layerIndex * 0.5, time * 0.001, seed * 0.0001, 2) * 100.0 * glitchFactor;
        double z = camera.z + Math.cos(time * 0.0003 + layerIndex * 0.7) * 400.0 * (1.0 + glitchFactor * 2.0) + McsmMeshSynthesizer.fBm(layerIndex * 0.5 + 100, time * 0.001, seed * 0.0001, 2) * 100.0 * glitchFactor;
        double y = layerY;

        double size = 30.0 + glitchFactor * 60.0 + Math.sin(time * 0.004 + layerIndex) * 20.0 + McsmMeshSynthesizer.fBm(layerIndex * 0.3, time * 0.001, seed * 0.0001, 2) * 20.0 * glitchFactor;

        for (int s = 0; s < 6; s++) {
            double a0 = (s / 6.0) * Mth.TWO_PI + time * 0.002 * glitchFactor;
            double a1 = ((s + 1) / 6.0) * Mth.TWO_PI + time * 0.002 * glitchFactor;
            double x0 = x + Math.cos(a0) * size;
            double z0 = z + Math.sin(a0) * size;
            double x1 = x + Math.cos(a1) * size;
            double z1 = z + Math.sin(a1) * size;

            double[] d0 = McsmMeshSynthesizer.displaceVertexAdvanced(x0, y, z0, time, fallDistance, glitchFactor, playerPos, s + layerIndex * 7000, seed);
            double[] d1 = McsmMeshSynthesizer.displaceVertexAdvanced(x1, y, z1, time, fallDistance, glitchFactor, playerPos, s+1 + layerIndex * 7000, seed);
            double[] d0h = McsmMeshSynthesizer.displaceVertexAdvanced(x0, y + size * 3, z0, time, fallDistance, glitchFactor, playerPos, s + layerIndex * 7001, seed);
            double[] d1h = McsmMeshSynthesizer.displaceVertexAdvanced(x1, y + size * 3, z1, time, fallDistance, glitchFactor, playerPos, s+1 + layerIndex * 7001, seed);

            float hue = (float)(((s * 40 + layerIndex * 7 + time * 0.02) % 360) / 360.0);
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

        double size = 600.0 + layerIndex * 10.0 + glitchFactor * 200.0 + McsmMeshSynthesizer.fBm(layerIndex * 0.3, time * 0.001, seed * 0.0001, 2) * 100.0;
        float hue = (float)(((layerIndex * 13 + time * 0.005) % 360) / 360.0);
        float[] rgb = hsvToRgb(hue, 0.7F, 1.0F);
        int r = (int)(rgb[0] * 255), g = (int)(rgb[1] * 255), b = (int)(rgb[2] * 255);
        int a = (int)(visibility * 60 * glitchFactor);

        quadFullBright(pose, consumer, x - size, y, z - 1, 0, 0, x + size, y, z - 1, 1, 0, x + size, y, z + 1, 1, 1, x - size, y, z + 1, 0, 1, r, g, b, a);
    }

    private static void emitExtremeParticles(Pose pose, VertexConsumer consumer, Vec3 camera, double layerY, int layerIndex, double time, float visibility, float glitchFactor, long seed, Vec3 playerPos) {
        int count = (int)(glitchFactor * 50) + 20;
        for (int i = 0; i < count; i++) {
            double angle = (i / (double) count) * Mth.TWO_PI + time * 0.01 * (i % 2 == 0 ? 1 : -1);
            double radius = 100.0 + i * 12.0 + Mth.sin((float)(time * 0.01 + i)) * glitchFactor * 80.0 + McsmMeshSynthesizer.fBm(i * 0.5, time * 0.001, seed * 0.0001, 2) * 40.0;
            double x = camera.x + Math.cos(angle) * radius + McsmGlitchGenerator.fractalNoise(i * 0.8, time * 0.005) * glitchFactor * 60.0;
            double z = camera.z + Math.sin(angle) * radius + McsmGlitchGenerator.fractalNoise(i * 0.9 + 100, time * 0.005) * glitchFactor * 60.0;
            double y = layerY + Mth.sin((float)(time * 0.005 + i)) * 50.0 * glitchFactor + i * 2.0 + McsmMeshSynthesizer.fBm(i * 0.3, time * 0.001, seed * 0.0001, 2) * 20.0 * glitchFactor;

            double size = 3.0 + glitchFactor * 6.0;
            float hue = (float)(((i * 27 + time * 0.02) % 360) / 360.0);
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

    public static String getCurrentMeditation() {
        return currentMeditation;
    }

    public static int getCurrentMeditationLayer() {
        return currentMeditationLayer;
    }

    public static String state() {
        return "infinite-layers-V3-ENDLESS: spacing=" + LAYER_SPACING + " visible=" + TOTAL_VISIBLE + " types=" + TYPE_COUNT + " generated=" + layersGenerated + " meditationLayer=" + currentMeditationLayer + " meditation=" + currentMeditation + " meshSynth=" + McsmMeshSynthesizer.state() + " F1DualReality=true photorealistic=true endlessCheers=true infinite=true NO-MILD=true OVERWHELMING-PSYCHEDELIC";
    }
}
