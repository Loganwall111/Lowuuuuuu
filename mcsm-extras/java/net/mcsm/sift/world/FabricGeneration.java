package net.mcsm.sift.world;

import net.mcsm.sift.McsmSiftMod;
import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

/**
 * Fabric of Reality generation - 8 LAYERS OF THE VOID CONCEPT - 7000.0.12-M
 * 
 * Concept from user images:
 * 1. OVERWORLD Y> -64 normal
 * 2. BEDROCK LEVEL -64 to -150 - lowest solid layer, unbreakable, foundation
 * 3. FLOATING VOID ISLANDS -150 to -350 - fractured remnants of impossible worlds
 * 4. INFINITE NOTHING BARRIER -350 to -550 - surreal boundary, reality distorts
 * 5. INFINITE BLACKNESS -550 to -800 - endless pure void, no light, no sound
 * 6. SPONGE MAZE -800 to -1200 - colossal maze of ancient sponge blocks (SIFT)
 * 7. LUMINESCENT POOLS -1200 to -1600 - glowing waters, liquid crystals
 * 8. ABYSSAL NIGHTMARE -1600 to -2032 - final realm, terror, chaos, ancient evil
 * 
 * MERGED VOID: second dimension directly underneath overworld hundreds blocks down
 * Skybox merges slowly to color of that area as you fall
 */
public final class FabricGeneration {

    private final PerlinNoise crackNoise;
    private final PerlinNoise starNoise;
    private final PerlinNoise islandNoise;

    public FabricGeneration(RandomSource random) {
        this.crackNoise = PerlinNoise.create(random, -2, 1);
        this.starNoise = PerlinNoise.create(random, -1, 1);
        this.islandNoise = PerlinNoise.create(RandomSource.create(random.nextLong()), -2, 1);
    }

    // 2. BEDROCK LEVEL -64 to -150
    public void generateTopFabric(WorldGenLevel level, BlockPos chunkOrigin) {
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();
        RandomSource rng = level.getRandom();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = McsmVoidTiers.BEDROCK_LEVEL_TOP; y >= McsmVoidTiers.BEDROCK_LEVEL_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                    
                    var current = level.getBlockState(pos);
                    if (!current.is(Blocks.BEDROCK) && !current.is(Blocks.DEEPSLATE) && !current.isAir() && !current.is(Blocks.STONE) && y > -120) {
                        continue;
                    }

                    double crack = crackNoise.getValue(
                        (startX + x) * 0.05,
                        y * 0.05,
                        (startZ + z) * 0.05
                    );
                    double stars = starNoise.getValue(
                        (startX + x) * 0.1,
                        y * 0.1,
                        (startZ + z) * 0.1
                    );

                    boolean isCrack = crack > 0.3 || stars > 0.6;
                    boolean isBroken = isCrack && rng.nextFloat() < 0.12f;
                    if (Math.abs(startX + x) < 200 && Math.abs(startZ + z) < 200 && rng.nextFloat() < 0.03f) isBroken = true;

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
            }
        }
    }

    // 8. ABYSSAL NIGHTMARE DIMENSION bottom fabric
    public void generateBottomFabric(WorldGenLevel level, BlockPos chunkOrigin) {
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();
        RandomSource rng = level.getRandom();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = McsmVoidTiers.ABYSSAL_NIGHTMARE_TOP; y >= McsmVoidTiers.ABYSSAL_NIGHTMARE_TOP - 100; y--) {
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                    
                    double crack = crackNoise.getValue(
                        (startX + x) * 0.03,
                        y * 0.03,
                        (startZ + z) * 0.03
                    );

                    boolean isBroken = crack > 0.2 || rng.nextFloat() < 0.30f;

                    if (isBroken) {
                        level.setBlock(pos, McsmSiftMod.BROKEN_FABRIC.get().defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, McsmSiftMod.BOTTOM_FABRIC.get().defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    // 3. FLOATING VOID ISLANDS -150 to -350
    public void generateFloatingIslands(WorldGenLevel level, BlockPos chunkOrigin) {
        RandomSource rng = level.getRandom();
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = startX + x;
                int wz = startZ + z;
                for (int y = McsmVoidTiers.FLOATING_VOID_ISLANDS_TOP - 1; y >= McsmVoidTiers.FLOATING_VOID_ISLANDS_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    double island = islandNoise.getValue(wx * 0.02, y * 0.02, wz * 0.02);
                    boolean isIsland = island > 0.15 && rng.nextFloat() < 0.35f;
                    if (y % 25 == 0 && rng.nextFloat() < 0.08f) isIsland = true;
                    if (isIsland) {
                        float type = rng.nextFloat();
                        if (type < 0.25f) {
                            level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                        } else if (type < 0.45f) {
                            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);
                        } else if (type < 0.65f) {
                            level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
                        } else if (type < 0.75f) {
                            level.setBlock(pos, Blocks.OAK_LOG.defaultBlockState(), 2);
                        } else {
                            level.setBlock(pos, McsmSiftMod.FABRIC_OF_REALITY.get().defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }

    public void generateEmptinessDecoration(WorldGenLevel level, BlockPos chunkOrigin) {
        RandomSource rng = level.getRandom();
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();

        // Nothing Barrier markers
        if (rng.nextFloat() < 0.08f) {
            int x = startX + rng.nextInt(16);
            int y = McsmVoidTiers.NOTHING_BARRIER_TOP - 10 - rng.nextInt(150);
            int z = startZ + rng.nextInt(16);
            BlockPos pos = new BlockPos(x, y, z);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx*dx + dz*dz <= 4) {
                        level.setBlock(pos.offset(dx, 0, dz), 
                            McsmSiftMod.FABRIC_OF_REALITY.get().defaultBlockState(), 2);
                    }
                }
            }
        }

        if (rng.nextFloat() < 0.02f) {
            int x = startX + rng.nextInt(16);
            int y = McsmVoidTiers.INFINITE_BLACKNESS_TOP - 20 - rng.nextInt(180);
            int z = startZ + rng.nextInt(16);
            BlockPos pos = new BlockPos(x, y, z);
            level.setBlock(pos, Blocks.BLACK_CONCRETE.defaultBlockState(), 2);
        }
    }

    public void generateGelHorizon(WorldGenLevel level, BlockPos chunkOrigin) {
        RandomSource rng = level.getRandom();
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (rng.nextFloat() < 0.85f) continue;
                int y = McsmVoidTiers.LUMINESCENT_POOLS_TOP - rng.nextInt(300);
                BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                if (rng.nextFloat() < 0.5f) {
                    level.setBlock(pos, McsmSiftMod.IRIDESCENT_GEL.get().defaultBlockState(), 2);
                } else {
                    level.setBlock(pos, Blocks.SEA_LANTERN.defaultBlockState(), 2);
                }
            }
        }
    }
}
