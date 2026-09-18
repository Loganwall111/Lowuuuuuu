package net.mcsm.sift.world;

import net.mcsm.sift.McsmSiftMod;
import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.util.RandomSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Dimension handling - 8 LAYERS OF THE VOID CONCEPT - LARGE LUSH ISLANDS
 * Build #7000.0.12-M - THE LAYERS OF THE VOID - Concept Image Implementation
 * 
 * Concept:
 * 1. OVERWORLD Y> -64
 * 2. BEDROCK LEVEL -64 to -150
 * 3. FLOATING VOID ISLANDS -150 to -350 - LARGE LUSH like concept art, not small footed
 * 4. INFINITE NOTHING BARRIER -350 to -550
 * 5. INFINITE BLACKNESS -550 to -800
 * 6. SPONGE MAZE -800 to -1200
 * 7. LUMINESCENT POOLS -1200 to -1600 - RAINBOW WORLD
 * 8. ABYSSAL NIGHTMARE -1600 to -2032
 */
@Mod.EventBusSubscriber
public final class McsmSiftDimension {

    public static final ResourceKey<Level> SIFT_DIMENSION = ResourceKey.create(Registries.DIMENSION,
        new ResourceLocation("mcsm", "sift"));

    public static final ResourceKey<DimensionType> SIFT_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
        new ResourceLocation("mcsm", "sift_type"));

    private McsmSiftDimension() {}

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        int y = (int) entity.getY();
        if (!McsmVoidTiers.isInVoidDimension(y)) return;

        if (McsmVoidTiers.VOID_IS_POCKET_DIMENSION || McsmVoidTiers.DISABLE_VOID_SUFFOCATION) {
            var source = event.getSource();
            if (source.is(DamageTypes.IN_WALL)) {
                event.setCanceled(true);
                return;
            }
            String msgId = source.getMsgId();
            if (msgId.equals("inWall") || msgId.equals("in_wall") || msgId.contains("wall")) {
                event.setCanceled(true);
                return;
            }
            if (McsmVoidTiers.DISABLE_VOID_DROWN && (msgId.equals("drown") || source.is(DamageTypes.DROWN))) {
                event.setCanceled(true);
                return;
            }
            if (McsmVoidTiers.DISABLE_VOID_FALL_DAMAGE && (msgId.equals("fall") || source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL))) {
                event.setCanceled(true);
                entity.fallDistance = 0;
                return;
            }
            if (msgId.equals("outOfWorld") || source.is(DamageTypes.OUT_OF_WORLD) || source.is(DamageTypes.OUTSIDE_BORDER)) {
                event.setCanceled(true);
                if (entity instanceof ServerPlayer sp) {
                    InnerSpaceTransition.triggerReturnToOverworld(sp);
                }
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        BlockPos chunkOrigin = new BlockPos(chunkX * 16, level.getMinBuildHeight(), chunkZ * 16);

        try {
            generateMergedVoidInChunk(level, chunkOrigin);
        } catch (Exception e) {
            System.err.println("[MCSM Sift] Failed to generate merged void at " + chunkOrigin + ": " + e.getMessage());
        }
    }

    public static void generateMergedVoidInChunk(ServerLevel level, BlockPos chunkOrigin) {
        RandomSource rng = RandomSource.create(chunkOrigin.asLong() ^ level.getSeed());
        PerlinNoise crackNoise = PerlinNoise.create(rng, -2, 1);
        PerlinNoise islandNoise = PerlinNoise.create(RandomSource.create(rng.nextLong()), -2, 1);
        PerlinNoise spongeNoise = PerlinNoise.create(RandomSource.create(rng.nextLong()), -3, 1);
        PerlinNoise tunnelNoise = PerlinNoise.create(RandomSource.create(rng.nextLong()), -2, 1);
        PerlinNoise nightmareNoise = PerlinNoise.create(RandomSource.create(rng.nextLong()), -1, 1);

        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();

        // Generate LARGE LUSH FLOATING ISLANDS first - per chunk, not per x/z
        generateLargeLushIslands(level, chunkOrigin, rng, islandNoise);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = startX + x;
                int wz = startZ + z;

                // 2. BEDROCK LEVEL -64 to -150
                for (int y = McsmVoidTiers.BEDROCK_LEVEL_TOP; y >= McsmVoidTiers.BEDROCK_LEVEL_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    BlockState current = level.getBlockState(pos);
                    if (y > -100 && !current.is(Blocks.BEDROCK) && !current.is(Blocks.DEEPSLATE) && !current.isAir() && !current.is(Blocks.STONE)) {
                        continue;
                    }
                    double crack = crackNoise.getValue(wx * 0.05, y * 0.05, wz * 0.05);
                    boolean isCrack = crack > 0.3;
                    boolean isBroken = isCrack && rng.nextFloat() < 0.15f;
                    if (Math.abs(wx) < 80 && Math.abs(wz) < 80 && rng.nextFloat() < 0.03f) isBroken = true;

                    if (isBroken) {
                        level.setBlock(pos, McsmSiftMod.BROKEN_FABRIC.get().defaultBlockState(), 2);
                    } else {
                        if (rng.nextFloat() < 0.3f) {
                            level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 2);
                        } else {
                            level.setBlock(pos, McsmSiftMod.FABRIC_OF_REALITY.get().defaultBlockState(), 2);
                        }
                    }
                }

                // 4. INFINITE NOTHING BARRIER -350 to -550
                for (int y = McsmVoidTiers.NOTHING_BARRIER_TOP - 1; y >= McsmVoidTiers.NOTHING_BARRIER_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (y == McsmVoidTiers.NOTHING_BARRIER_TOP - 1 || y == McsmVoidTiers.NOTHING_BARRIER_BOTTOM) {
                        if (rng.nextFloat() < 0.7f) {
                            level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 2);
                        } else {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        }
                        continue;
                    }
                    if (rng.nextFloat() < 0.995f) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        continue;
                    }
                    float t = rng.nextFloat();
                    if (t < 0.4f) {
                        level.setBlock(pos, Blocks.GLASS.defaultBlockState(), 2);
                    } else if (t < 0.7f) {
                        level.setBlock(pos, Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                    }
                }

                // 5. INFINITE BLACKNESS -550 to -800
                for (int y = McsmVoidTiers.INFINITE_BLACKNESS_TOP - 1; y >= McsmVoidTiers.INFINITE_BLACKNESS_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (y % 30 == 0 && rng.nextFloat() < 0.02f) {
                        level.setBlock(pos, Blocks.BLACK_CONCRETE.defaultBlockState(), 2);
                        continue;
                    }
                    if (rng.nextFloat() < 0.9995f) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, Blocks.BLACK_STAINED_GLASS.defaultBlockState(), 2);
                    }
                }

                // 6. SPONGE MAZE -800 to -1200
                for (int y = McsmVoidTiers.SPONGE_MAZE_TOP; y >= McsmVoidTiers.SPONGE_MAZE_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (y < McsmVoidTiers.SPONGE_MAZE_PHYSICAL_START) {
                        boolean solid = isMengerSolid(wx, y, wz, 3);
                        double nx = wx * 0.03 + chunkOrigin.asLong() * 0.001;
                        double ny = y * 0.03;
                        double nz = wz * 0.03;
                        double n1 = spongeNoise.getValue(nx, ny, nz);
                        double n2 = tunnelNoise.getValue(nx * 1.5, ny * 0.8, nz * 1.5);
                        boolean carved = (n1 + n2 * 0.6) > 0.10;
                        if (solid && carved) {
                            float depthFactor = (float)(McsmVoidTiers.SPONGE_MAZE_TOP - y) / (McsmVoidTiers.SPONGE_MAZE_TOP - McsmVoidTiers.SPONGE_MAZE_BOTTOM);
                            BlockState state = getMazeBlockForDepth(depthFactor, rng);
                            level.setBlock(pos, state, 2);
                        } else {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    } else {
                        if (rng.nextFloat() < 0.88f) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        } else {
                            level.setBlock(pos, McsmSiftMod.MENGER_SPONGE.get().defaultBlockState(), 2);
                        }
                    }
                }

                // 7. LUMINESCENT POOLS -1200 to -1600 - RAINBOW WORLD
                for (int y = McsmVoidTiers.LUMINESCENT_POOLS_TOP - 1; y >= McsmVoidTiers.LUMINESCENT_POOLS_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    double crystalNoise = nightmareNoise.getValue(wx * 0.04, y * 0.04, wz * 0.04);
                    if (rng.nextFloat() < 0.70f) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        continue;
                    }
                    if (crystalNoise > 0.2 || rng.nextFloat() < 0.20f) {
                        float t = rng.nextFloat();
                        try {
                            if (t < 0.12f) {
                                level.setBlock(pos, McsmSiftMod.IRIDESCENT_GEL.get().defaultBlockState(), 2);
                            } else if (t < 0.20f) {
                                level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.RAINBOW_WATER.defaultBlockState(), 2);
                            } else if (t < 0.28f) {
                                level.setBlock(pos, net.mcsm.sift.block.McsmSiftContent.RAINBOW_BAND.defaultBlockState(), 2);
                            } else if (t < 0.36f) {
                                level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.BLUE_GRASS_BLOCK.defaultBlockState(), 2);
                            } else if (t < 0.44f) {
                                level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.PINK_GRASS_BLOCK.defaultBlockState(), 2);
                            } else if (t < 0.52f) {
                                level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.CYAN_MOSS.defaultBlockState(), 2);
                            } else if (t < 0.60f) {
                                level.setBlock(pos, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                            } else if (t < 0.68f) {
                                level.setBlock(pos, Blocks.GLOWSTONE.defaultBlockState(), 2);
                            } else if (t < 0.76f) {
                                level.setBlock(pos, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                            } else if (t < 0.84f) {
                                level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.GEL_CRYSTAL.defaultBlockState(), 2);
                            } else if (t < 0.92f) {
                                level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.VOID_LILY.defaultBlockState(), 2);
                            } else {
                                level.setBlock(pos, Blocks.PRISMARINE.defaultBlockState(), 2);
                            }
                        } catch (Exception e) {
                            level.setBlock(pos, McsmSiftMod.IRIDESCENT_GEL.get().defaultBlockState(), 2);
                        }
                    } else if (rng.nextFloat() < 0.08f) {
                        try {
                            if (rng.nextFloat() < 0.5f) level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                            else level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.BLACK_WATER.defaultBlockState(), 2);
                        } catch (Exception e) {
                            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                        }
                    }
                }

                // 8. ABYSSAL NIGHTMARE DIMENSION -1600 to -2032
                for (int y = McsmVoidTiers.ABYSSAL_NIGHTMARE_TOP - 1; y >= McsmVoidTiers.ABYSSAL_NIGHTMARE_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    double nightNoise = nightmareNoise.getValue(wx * 0.02, y * 0.02, wz * 0.02);
                    if (nightNoise > 0.3 && rng.nextFloat() < 0.65f) {
                        float t = rng.nextFloat();
                        try {
                            if (t < 0.15f) {
                                level.setBlock(pos, Blocks.SCULK.defaultBlockState(), 2);
                            } else if (t < 0.25f) {
                                level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.RED_GRASS_BLOCK.defaultBlockState(), 2);
                            } else if (t < 0.35f) {
                                level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.RED_ROCK.defaultBlockState(), 2);
                            } else if (t < 0.45f) {
                                level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.GREEN_ACID.defaultBlockState(), 2);
                            } else if (t < 0.55f) {
                                level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.BLACK_WATER.defaultBlockState(), 2);
                            } else if (t < 0.65f) {
                                level.setBlock(pos, McsmSiftMod.UNKNOWN_GROUND.defaultBlockState(), 2);
                            } else if (t < 0.75f) {
                                level.setBlock(pos, Blocks.SOUL_SAND.defaultBlockState(), 2);
                            } else if (t < 0.85f) {
                                level.setBlock(pos, Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                            } else if (t < 0.92f) {
                                level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 2);
                            } else {
                                level.setBlock(pos, McsmSiftMod.BOTTOM_FABRIC.get().defaultBlockState(), 2);
                            }
                        } catch (Exception e) {
                            level.setBlock(pos, Blocks.SCULK.defaultBlockState(), 2);
                        }
                    } else if (rng.nextFloat() < 0.18f) {
                        try {
                            float f = rng.nextFloat();
                            if (f < 0.3f) level.setBlock(pos, Blocks.SCULK_CATALYST.defaultBlockState(), 2);
                            else if (f < 0.6f) level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.FUNGUS_TREE.defaultBlockState(), 2);
                            else level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.RED_VINES.defaultBlockState(), 2);
                        } catch (Exception e) {
                            level.setBlock(pos, Blocks.SCULK_CATALYST.defaultBlockState(), 2);
                        }
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    // LARGE LUSH FLOATING ISLANDS - like in concept art, not small footed islands
    // Each island is 20-50 blocks wide, with lush detail: grass, trees, flowers, waterfalls, etc
    private static void generateLargeLushIslands(ServerLevel level, BlockPos chunkOrigin, RandomSource rng, PerlinNoise islandNoise) {
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();
        
        // Check 2-3 potential island centers per chunk
        for (int i = 0; i < 2; i++) {
            int centerX = startX + rng.nextInt(16);
            int centerZ = startZ + rng.nextInt(16);
            int centerY = McsmVoidTiers.FLOATING_VOID_ISLANDS_BOTTOM + rng.nextInt(
                McsmVoidTiers.FLOATING_VOID_ISLANDS_TOP - McsmVoidTiers.FLOATING_VOID_ISLANDS_BOTTOM);
            
            double noise = islandNoise.getValue(centerX * 0.01, centerY * 0.01, centerZ * 0.01);
            // Only generate island if noise is high enough - creates sparse large islands
            if (noise < 0.25 && rng.nextFloat() > 0.15f) continue;
            
            // Large island radius 12-28 blocks
            int radius = 12 + rng.nextInt(16);
            int height = 4 + rng.nextInt(6);
            
            // Island shape: circular with noise distortion for natural look
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double dist = Math.sqrt(dx*dx + dz*dz);
                    if (dist > radius) continue;
                    // Edge falloff for natural circular shape
                    double edgeFactor = 1.0 - (dist / radius);
                    if (rng.nextFloat() > edgeFactor * 1.2f) continue;
                    
                    int wx = centerX + dx;
                    int wz = centerZ + dz;
                    
                    // Generate island layers
                    for (int dy = 0; dy < height; dy++) {
                        int wy = centerY - dy;
                        if (wy < McsmVoidTiers.FLOATING_VOID_ISLANDS_BOTTOM || wy > McsmVoidTiers.FLOATING_VOID_ISLANDS_TOP) continue;
                        BlockPos pos = new BlockPos(wx, wy, wz);
                        
                        try {
                            if (dy == 0) {
                                // Top layer: lush grass - blue, pink, red, starlit, cosmic, regular
                                float grassType = rng.nextFloat();
                                if (grassType < 0.18f) {
                                    level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.BLUE_GRASS_BLOCK.defaultBlockState(), 2);
                                } else if (grassType < 0.32f) {
                                    level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.PINK_GRASS_BLOCK.defaultBlockState(), 2);
                                } else if (grassType < 0.44f) {
                                    level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.STARLIT_GRASS.defaultBlockState(), 2);
                                } else if (grassType < 0.54f) {
                                    level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.COSMIC_GRASS.defaultBlockState(), 2);
                                } else if (grassType < 0.62f) {
                                    level.setBlock(pos, net.mcsm.sift.block.SiftEcosystemBlocks.RED_GRASS_BLOCK.defaultBlockState(), 2);
                                } else {
                                    level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                                }
                            } else if (dy == 1 || dy == 2) {
                                // Middle: dirt
                                level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);
                            } else {
                                // Bottom: stone/cobble
                                if (rng.nextFloat() < 0.6f) {
                                    level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
                                } else {
                                    level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 2);
                                }
                            }
                        } catch (Exception e) {
                            level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                        }
                    }
                    
                    // Add trees and vegetation on top - lush detail like concept
                    if (dist < radius * 0.7 && rng.nextFloat() < 0.04f) {
                        BlockPos topPos = new BlockPos(wx, centerY + 1, wz);
                        if (level.getBlockState(topPos).isAir()) {
                            try {
                                float vegType = rng.nextFloat();
                                if (vegType < 0.15f) {
                                    // Purple tree - 4-5 blocks tall log + leaves
                                    for (int h = 0; h < 4 + rng.nextInt(3); h++) {
                                        BlockPos logPos = new BlockPos(wx, centerY + 1 + h, wz);
                                        level.setBlock(logPos, net.mcsm.sift.block.SiftEcosystemBlocks.PURPLE_TREE_LOG.defaultBlockState(), 2);
                                    }
                                    // Leaves canopy
                                    for (int lx = -2; lx <= 2; lx++) {
                                        for (int lz = -2; lz <= 2; lz++) {
                                            for (int ly = 0; ly <= 2; ly++) {
                                                if (Math.abs(lx) == 2 && Math.abs(lz) == 2) continue;
                                                BlockPos leafPos = new BlockPos(wx+lx, centerY + 5 + ly, wz+lz);
                                                if (level.getBlockState(leafPos).isAir() && rng.nextFloat() < 0.8f) {
                                                    level.setBlock(leafPos, net.mcsm.sift.block.SiftEcosystemBlocks.PURPLE_TREE_LEAVES.defaultBlockState(), 2);
                                                }
                                            }
                                        }
                                    }
                                } else if (vegType < 0.30f) {
                                    // Oak tree
                                    BlockPos logPos = new BlockPos(wx, centerY + 1, wz);
                                    level.setBlock(logPos, Blocks.OAK_LOG.defaultBlockState(), 2);
                                    level.setBlock(new BlockPos(wx, centerY + 2, wz), Blocks.OAK_LOG.defaultBlockState(), 2);
                                    level.setBlock(new BlockPos(wx, centerY + 3, wz), Blocks.OAK_LEAVES.defaultBlockState(), 2);
                                } else if (vegType < 0.50f) {
                                    level.setBlock(topPos, net.mcsm.sift.block.SiftEcosystemBlocks.FLUOR_PLANT.defaultBlockState(), 2);
                                } else if (vegType < 0.70f) {
                                    level.setBlock(topPos, net.mcsm.sift.block.SiftEcosystemBlocks.VOID_BLOSSOM.defaultBlockState(), 2);
                                } else if (vegType < 0.85f) {
                                    level.setBlock(topPos, net.mcsm.sift.block.SiftEcosystemBlocks.GLOWING_MUSHROOM.defaultBlockState(), 2);
                                } else {
                                    level.setBlock(topPos, net.mcsm.sift.block.SiftEcosystemBlocks.VOID_FERN.defaultBlockState(), 2);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                    
                    // Add small water pools / waterfalls on large islands - lush
                    if (dist < radius * 0.3 && rng.nextFloat() < 0.008f) {
                        BlockPos waterPos = new BlockPos(wx, centerY, wz);
                        try {
                            level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 2);
                            // Waterfall down
                            for (int wy = centerY - 1; wy >= centerY - 5; wy--) {
                                BlockPos fallPos = new BlockPos(wx, wy, wz);
                                if (level.getBlockState(fallPos).isAir()) {
                                    level.setBlock(fallPos, Blocks.WATER.defaultBlockState(), 2);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    private static boolean isMengerSolid(int x, int y, int z, int iterations) {
        int localX = Math.floorMod(x, 81);
        int localY = Math.floorMod(y, 81);
        int localZ = Math.floorMod(z, 81);
        for (int i = 0; i < iterations; i++) {
            int scale = (int) Math.pow(3, iterations - i - 1);
            int ix = localX / scale % 3;
            int iy = localY / scale % 3;
            int iz = localZ / scale % 3;
            int centerCount = 0;
            if (ix == 1) centerCount++;
            if (iy == 1) centerCount++;
            if (iz == 1) centerCount++;
            if (centerCount >= 2) return false;
        }
        return true;
    }

    private static BlockState getMazeBlockForDepth(float depth, RandomSource rng) {
        if (depth < 0.2f) return Blocks.ORANGE_CONCRETE.defaultBlockState();
        if (depth < 0.4f) return Blocks.ORANGE_TERRACOTTA.defaultBlockState();
        if (depth < 0.6f) return Blocks.PINK_CONCRETE.defaultBlockState();
        if (depth < 0.8f) return Blocks.MAGENTA_CONCRETE.defaultBlockState();
        return Blocks.PINK_TERRACOTTA.defaultBlockState();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        int y = (int) player.getY();
        Level lvl = player.level();

        if (McsmVoidTiers.isInVoidDimension(y)) {
            player.fallDistance = 0;
            if (McsmVoidTiers.DISABLE_VOID_SUFFOCATION) {
                if (player.isInWall()) {
                    player.setDeltaMovement(player.getDeltaMovement().add(0, 0.3, 0));
                    player.hasImpulse = true;
                }
            }
        }

        if (McsmVoidTiers.shouldTriggerInnerSpace(y)) {
            if (player instanceof ServerPlayer sp) {
                InnerSpaceTransition.triggerReturnToOverworld(sp);
                sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.BLINDNESS, 100, 0, false, false, false));
            }
            return;
        }

        if (McsmVoidTiers.shouldWrap(y)) {
            var result = McsmVoidTiers.performWrapWithHandshake(player);
            result.applyTo(player);
            if (player instanceof ServerPlayer sp) {
                sp.fallDistance = 0;
            }
        }

        if (y == McsmVoidTiers.BEDROCK_LEVEL_BOTTOM - 1 || y == McsmVoidTiers.BEDROCK_LEVEL_BOTTOM - 2) {
            if (player instanceof ServerPlayer sp) {
                InnerSpaceTransition.triggerEnterSift(sp);
            }
        }

        if (McsmVoidTiers.isInVoidDimension(y)) {
            if (lvl instanceof ServerLevel sLevel && lvl.dimension() == Level.OVERWORLD && player instanceof ServerPlayer sp) {
                if (player.tickCount % 20 == 0) {
                    int cx = player.blockPosition().getX() >> 4;
                    int cz = player.blockPosition().getZ() >> 4;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos origin = new BlockPos((cx+dx)*16, sLevel.getMinBuildHeight(), (cz+dz)*16);
                            try { generateMergedVoidInChunk(sLevel, origin); } catch (Exception ignored) {}
                        }
                    }
                }

                var tier = McsmVoidTiers.getTierForY(y);
                if (tier == McsmVoidTiers.Tier.INFINITE_BLACKNESS || tier == McsmVoidTiers.Tier.NOTHING_BARRIER || tier == McsmVoidTiers.Tier.INFINITE_NOTHING_BARRIER) {
                    if (player.getDeltaMovement().y > -2.2) {
                        player.setDeltaMovement(player.getDeltaMovement().add(0, -0.20, 0));
                        player.hasImpulse = true;
                    }
                    if (player.tickCount % 15 == 0) {
                        sLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,
                            player.getX(), player.getY(), player.getZ(),
                            5, 0.3, 0.3, 0.3, 0.1);
                    }
                }

                if (player.tickCount % 40 == 0) {
                    String msg;
                    if (tier == McsmVoidTiers.Tier.BEDROCK_LEVEL) {
                        msg = "§8[2] BEDROCK LEVEL " + y + " -> FLOATING ISLANDS at -150 | SIFT at -800";
                    } else if (tier == McsmVoidTiers.Tier.FLOATING_VOID_ISLANDS) {
                        int dist = Math.abs(y - McsmVoidTiers.SPONGE_MAZE_TOP);
                        msg = "§a[3] FLOATING VOID ISLANDS " + y + " -> LUSH LARGE ISLANDS! SIFT MAZE at -800 (" + dist + "b)";
                    } else if (tier == McsmVoidTiers.Tier.NOTHING_BARRIER || tier == McsmVoidTiers.Tier.INFINITE_NOTHING_BARRIER) {
                        int dist = Math.abs(y - McsmVoidTiers.SPONGE_MAZE_TOP);
                        msg = "§5[4] NOTHING BARRIER " + y + " -> SIFT at -800 (" + dist + "b) | Reality distorts";
                    } else if (tier == McsmVoidTiers.Tier.INFINITE_BLACKNESS) {
                        int dist = Math.abs(y - McsmVoidTiers.SPONGE_MAZE_TOP);
                        msg = "§0[5] INFINITE BLACKNESS " + y + " -> SIFT MAZE at -800 (" + dist + "b) | No light, no sound";
                    } else if (tier == McsmVoidTiers.Tier.SPONGE_MAZE) {
                        msg = "§6[6] *** SPONGE MAZE - SIFT AREA *** " + y + " - YOU ARE IN SIFT! ***";
                    } else if (tier == McsmVoidTiers.Tier.LUMINESCENT_POOLS) {
                        msg = "§b[7] LUMINESCENT POOLS " + y + " - Glowing waters, liquid crystals, RAINBOW WORLD";
                    } else if (tier == McsmVoidTiers.Tier.ABYSSAL_NIGHTMARE) {
                        msg = "§5[8] ABYSSAL NIGHTMARE DIMENSION " + y + " - Final realm, reality screams";
                    } else {
                        msg = "[" + tier.id.toUpperCase().replace("_", " ") + "] " + y + " | SIFT MAZE at -800 to -1200";
                    }
                    sp.displayClientMessage(net.minecraft.network.chat.Component.literal(msg), true);

                    if (tier == McsmVoidTiers.Tier.SPONGE_MAZE && player.tickCount % 200 == 0) {
                        sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                            net.minecraft.network.chat.Component.literal("§6§l[6] THE SPONGE MAZE").withStyle(net.minecraft.ChatFormatting.BOLD)));
                        sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                            net.minecraft.network.chat.Component.literal("§eSIFT AREA - Ancient sponge blocks")));
                        sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 60, 20));
                    }
                }
            }
        }
    }

    public static void ensureSiftChunkLoaded(ServerLevel level, BlockPos pos) {
        if (McsmVoidTiers.isInVoidDimension(pos.getY())) {
            if (level.dimension() == Level.OVERWORLD) {
                int cx = pos.getX() >> 4;
                int cz = pos.getZ() >> 4;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos origin = new BlockPos((cx+dx)*16, level.getMinBuildHeight(), (cz+dz)*16);
                        try { generateMergedVoidInChunk(level, origin); } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    public static boolean isInSiftDimension(Level level) {
        return level.dimension() == SIFT_DIMENSION || McsmVoidTiers.isInVoidDimension((int)level.getSeaLevel());
    }

    public static void trySpawnRealityRift(ServerLevel level, BlockPos pos) {
        if (level.random.nextFloat() < 0.001f) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
