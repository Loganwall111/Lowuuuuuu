package net.mcsm.sift.world;

import net.mcsm.sift.McsmSiftMod;
import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

/**
 * Fabric of Reality generation - infinite gigantic ground at overworld bottom - MERGED VOID
 * Fabric of reality stone and broken fabric - now directly under overworld hundreds blocks down
 * 
 * NEW MERGED FLOW (fits within -2032):
 * Overworld Y> -64 normal
 * Fabric layer Y -64 to -200: infinite gigantic ground, animated pitch black with stars, cosmic purple, cracks glowing
 * Emptiness Y -200 to -600: pitch black void of stars, 40-50 sec fall with fireworks (compressed from -1000)
 * Tier 1 Gel Horizon Y -600 to -900: liquid where floats, god rays appear, sky changes
 * Tier 2 Menger Maze Y -900 to -1300: orange-to-pink AND pitch black starry sponge
 * Tier 3 Rift Field Y -1300 to -1500: reality rifts
 * Tier 4 Displacement Y -1500 to -1700: rainbow bands
 * Tier 5 Iridescent Gel Y -1700 to -1900: rainbow water pools
 * Bottom fabric Y -1900 to -2000: second purple pink brown rainbow layer, color of skybox, leads to Unknown
 * Unknown Y -2000 to -2032: bouncy distortion, ground decay, no bedrock -> triggers return to overworld sky
 * Inner Space: when breakthrough black fabric, fall from sky back to overworld - continuous loop
 * Skybox merges slowly to color of that area as you fall
 */
public final class FabricGeneration {

    private final PerlinNoise crackNoise;
    private final PerlinNoise starNoise;

    public FabricGeneration(RandomSource random) {
        this.crackNoise = PerlinNoise.create(random, -2, 1);
        this.starNoise = PerlinNoise.create(random, -1, 1);
    }

    public void generateTopFabric(WorldGenLevel level, BlockPos chunkOrigin) {
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();
        RandomSource rng = level.getRandom();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = McsmVoidTiers.FABRIC_TOP; y >= McsmVoidTiers.FABRIC_BOTTOM; y--) {
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
                        level.setBlock(pos, McsmSiftMod.FABRIC_OF_REALITY.get().defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    public void generateBottomFabric(WorldGenLevel level, BlockPos chunkOrigin) {
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();
        RandomSource rng = level.getRandom();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = McsmVoidTiers.BOTTOM_FABRIC_TOP; y >= McsmVoidTiers.BOTTOM_FABRIC_BOTTOM; y--) {
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

    public void generateEmptinessDecoration(WorldGenLevel level, BlockPos chunkOrigin) {
        RandomSource rng = level.getRandom();
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();

        if (rng.nextFloat() < 0.05f) {
            int x = startX + rng.nextInt(16);
            int y = McsmVoidTiers.EMPTINESS_TOP - 10 - rng.nextInt(McsmVoidTiers.EMPTINESS_TOP - McsmVoidTiers.EMPTINESS_BOTTOM - 20);
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

        if (rng.nextFloat() < 0.01f) {
            int x = startX + rng.nextInt(16);
            int y = McsmVoidTiers.EMPTINESS_TOP - 50 - rng.nextInt(200);
            int z = startZ + rng.nextInt(16);
            BlockPos pos = new BlockPos(x, y, z);
            level.setBlock(pos, McsmSiftMod.IRIDESCENT_GEL.get().defaultBlockState(), 2);
        }
    }

    public void generateGelHorizon(WorldGenLevel level, BlockPos chunkOrigin) {
        RandomSource rng = level.getRandom();
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (rng.nextFloat() < 0.9f) continue;
                int y = McsmVoidTiers.TIER_1_GEL_HORIZON_TOP - rng.nextInt(McsmVoidTiers.TIER_1_GEL_HORIZON_TOP - McsmVoidTiers.TIER_1_GEL_HORIZON_BOTTOM);
                BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                level.setBlock(pos, McsmSiftMod.IRIDESCENT_GEL.get().defaultBlockState(), 2);
            }
        }
    }
}
