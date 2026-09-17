package net.mcsm.sift.world;

import net.mcsm.sift.McsmSiftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

/**
 * Fabric of Reality generation - infinite gigantic ground at overworld bottom
 * Fabric of reality stone and broken fabric
 * 
 * Flow:
 * Overworld Y> -64 normal
 * Fabric layer Y -64 to -200: infinite gigantic ground, animated pitch black with stars, cosmic purple, cracks glowing
 * Emptiness Y -200 to -1000: pitch black void of stars, 40-50 sec fall with fireworks
 * Tier 1 Gel Horizon Y -1000 onwards
 * Bottom fabric Y -2700 to -2800: rainbow purple pink brown, leads to Unknown
 * Unknown: bouncy distortion, no bedrock, leads to Inner Space back to overworld
 */
public final class FabricGeneration {

    private final PerlinNoise crackNoise;
    private final PerlinNoise starNoise;

    public FabricGeneration(RandomSource random) {
        this.crackNoise = PerlinNoise.create(random, -2, 1);
        this.starNoise = PerlinNoise.create(random, -1, 1);
    }

    public void generateTopFabric(WorldGenLevel level, BlockPos chunkOrigin) {
        // Generate infinite gigantic ground at overworld bottom - fabric of reality
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();
        RandomSource rng = level.getRandom();

        // Top fabric layer: Y -64 to -200
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = -64; y >= -200; y--) {
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                    
                    // Only replace bedrock / deepslate / air at bottom
                    var current = level.getBlockState(pos);
                    if (!current.is(Blocks.BEDROCK) && !current.is(Blocks.DEEPSLATE) && !current.isAir() && y > -150) {
                        continue;
                    }

                    // Perlin noise for cracks - little reality cracks glowing
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

                    // 85% solid fabric stone, 15% broken fabric (portal)
                    // Broken fabric acts as glue and entry points
                    boolean isCrack = crack > 0.3 || stars > 0.6;
                    boolean isBroken = isCrack && rng.nextFloat() < 0.15f;

                    if (isBroken) {
                        // Broken fabric of reality - invisible, no collision, how you enter well
                        level.setBlock(pos, McsmSiftMod.BROKEN_FABRIC.get().defaultBlockState(), 2);
                    } else {
                        // Fabric of Reality Stone - pitch black with stars, cosmic purple, animated End Gateway style
                        level.setBlock(pos, McsmSiftMod.FABRIC_OF_REALITY.get().defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    public void generateBottomFabric(WorldGenLevel level, BlockPos chunkOrigin) {
        // Bottom fabric: second purple pink brown layer, rainbow one at very bottom
        // Color of skybox of brand new sift void dementia - leads to Unknown
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();
        RandomSource rng = level.getRandom();

        // Bottom fabric: Y -2700 to -2800
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = -2700; y >= -2800; y--) {
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                    
                    double crack = crackNoise.getValue(
                        (startX + x) * 0.03,
                        y * 0.03,
                        (startZ + z) * 0.03
                    );

                    // More broken fabric at bottom - easier to fall through to Unknown
                    boolean isBroken = crack > 0.2 || rng.nextFloat() < 0.25f;

                    if (isBroken) {
                        level.setBlock(pos, McsmSiftMod.BROKEN_FABRIC.get().defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, McsmSiftMod.FABRIC_OF_REALITY.get().defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    public void generateEmptinessDecoration(WorldGenLevel level, BlockPos chunkOrigin) {
        // Emptiness Y -200 to -1000: pitch black void of stars, nothing
        // But add occasional floating islands / fireworks platforms for 40-50 sec fall visualization
        RandomSource rng = level.getRandom();
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();

        if (rng.nextFloat() < 0.02f) {
            int x = startX + rng.nextInt(16);
            int y = -200 - rng.nextInt(800); // between -200 and -1000
            int z = startZ + rng.nextInt(16);
            BlockPos pos = new BlockPos(x, y, z);

            // Small floating fabric island in emptiness - for visual reference during long fall
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx*dx + dz*dz <= 4) {
                        level.setBlock(pos.offset(dx, 0, dz), 
                            McsmSiftMod.FABRIC_OF_REALITY.get().defaultBlockState(), 2);
                    }
                }
            }
        }
    }
}
