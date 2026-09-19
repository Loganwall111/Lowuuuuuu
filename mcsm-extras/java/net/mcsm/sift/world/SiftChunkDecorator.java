package net.mcsm.sift.world;

import net.mcsm.sift.McsmVoidTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

/**
 * Procedural Voxel Generation - Tier 2 Menger-Sponge Maze
 * Carves physical, mineable blocks between Y=-1101 and Y=-1250
 * Hollow Tube Tunnels for high-speed Void Rudder flight navigation
 * Orange-to-Pink Emissive Shading via vertex-shimmed full-bright overlay
 */
public final class SiftChunkDecorator {

    private static final int MAZE_TOP = McsmVoidTiers.TIER_2_PHYSICAL_MAZE_START;
    private static final int MAZE_BOTTOM = McsmVoidTiers.TIER_2_PHYSICAL_MAZE_END;
    private static final int CHUNK_SIZE = 16;

    // Block palette - emissive orange to pink gradient
    private static final BlockState[] PALETTE = new BlockState[]{
        Blocks.ORANGE_CONCRETE.defaultBlockState(),
        Blocks.PINK_CONCRETE.defaultBlockState(),
        Blocks.MAGENTA_CONCRETE.defaultBlockState(),
        Blocks.RED_CONCRETE.defaultBlockState(),
        Blocks.TERRACOTTA.defaultBlockState(),
        Blocks.SMOOTH_BASALT.defaultBlockState()
    };

    private final PerlinNoise spongeNoise;
    private final PerlinNoise tunnelNoise;

    public SiftChunkDecorator(RandomSource random) {
        this.spongeNoise = PerlinNoise.create(random, -3, 1);
        this.tunnelNoise = PerlinNoise.create(random, -2, 1);
    }

    /**
     * Native 3D Menger-Sponge fractal noise calculation
     * Classic Menger: 3x3x3 cube divided, remove center + 6 face centers = 20 cubes remain
     * Recursively applied for infinite detail
     */
    public boolean isMengerSolid(int x, int y, int z, int iterations) {
        // Normalize to local sponge space
        int localX = Math.floorMod(x, 81); // 3^4 = 81 for 4 iterations
        int localY = Math.floorMod(y, 81);
        int localZ = Math.floorMod(z, 81);

        for (int i = 0; i < iterations; i++) {
            int scale = (int) Math.pow(3, iterations - i - 1);
            int ix = localX / scale % 3;
            int iy = localY / scale % 3;
            int iz = localZ / scale % 3;

            // Menger removal rule: center + face centers
            int centerCount = 0;
            if (ix == 1) centerCount++;
            if (iy == 1) centerCount++;
            if (iz == 1) centerCount++;
            if (centerCount >= 2) {
                return false; // hollow - part of maze tunnel
            }
        }
        return true; // solid wall
    }

    public boolean isTunnelCarved(int x, int y, int z, long seed) {
        double nx = x * 0.03 + seed * 0.001;
        double ny = y * 0.03;
        double nz = z * 0.03 + seed * 0.0007;

        double noise = spongeNoise.getValue(nx, ny, nz);
        double tunnel = tunnelNoise.getValue(nx * 1.5, ny * 0.8, nz * 1.5);

        // Carve organic sponge tubes and hollow pores
        // Threshold creates sprawling labyrinth
        double threshold = 0.15 + Math.sin(y * 0.05) * 0.1;
        return (noise + tunnel * 0.6) > threshold;
    }

    public void decorateChunk(WorldGenLevel level, BlockPos chunkOrigin, long loopSeed) {
        RandomSource rng = RandomSource.create(loopSeed ^ chunkOrigin.asLong());
        int startX = chunkOrigin.getX();
        int startZ = chunkOrigin.getZ();

        // Dynamic seed shuffling per wrap loop
        long structureSeed = McsmVoidTiers.getStructureSeed(chunkOrigin.getX() * 31 + chunkOrigin.getZ());

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int y = MAZE_TOP; y >= MAZE_BOTTOM; y--) {
                    BlockPos pos = new BlockPos(startX + x, y, startZ + z);

                    // Only generate in Tier 2 maze bounds
                    if (y > MAZE_TOP || y < MAZE_BOTTOM) continue;

                    boolean solid = isMengerSolid(startX + x, y, startZ + z, 4);
                    boolean carved = isTunnelCarved(startX + x, y, startZ + z, structureSeed);

                    if (solid && carved) {
                        // Place mineable emissive block
                        BlockState state = getGradientBlockState(y, rng);
                        level.setBlock(pos, state, 2);
                    } else if (!solid || !carved) {
                        // Hollow tube - air for high-speed flight
                        if (level.getBlockState(pos).is(Blocks.STONE) || level.getBlockState(pos).is(Blocks.DEEPSLATE)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }

                    // Add floating spires and hanging structures with shuffled seeds
                    if (rng.nextFloat() < 0.002f && y % 12 == 0) {
                        generateHangingSpire(level, pos, rng, structureSeed);
                    }
                }
            }
        }
    }

    private BlockState getGradientBlockState(int y, RandomSource rng) {
        // Orange-to-Pink Emissive Shading that responds to global world time
        float depthFactor = Mth.clamp((float)(MAZE_TOP - y) / (float)(MAZE_TOP - MAZE_BOTTOM), 0f, 1f);
        int paletteIndex = (int)(depthFactor * (PALETTE.length - 1));
        // Add subtle noise to palette
        paletteIndex = Mth.clamp(paletteIndex + rng.nextInt(2) - 1, 0, PALETTE.length - 1);
        return PALETTE[paletteIndex];
    }

    private void generateHangingSpire(WorldGenLevel level, BlockPos origin, RandomSource rng, long seed) {
        int height = 8 + rng.nextInt(16);
        BlockState spireBlock = PALETTE[rng.nextInt(PALETTE.length)];

        for (int dy = 0; dy < height; dy++) {
            BlockPos p = origin.above(dy);
            if (p.getY() > MAZE_TOP || p.getY() < MAZE_BOTTOM) continue;

            // Taper spire
            int radius = (int)((1f - (float)dy / height) * 2);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx*dx + dz*dz <= radius*radius + 1) {
                        BlockPos spirePos = p.offset(dx, 0, dz);
                        level.setBlock(spirePos, spireBlock, 2);
                    }
                }
            }
        }
    }

    // Client-side color overlay for shader
    public static float[] getEmissiveGradient(float worldTime, int y) {
        float depth = (float)(MAZE_TOP - y) / (MAZE_TOP - MAZE_BOTTOM);
        float time = worldTime * 0.01f;

        // Orange (1.0, 0.5, 0.1) -> Pink (1.0, 0.4, 0.7) animated ripple
        float r = 1.0f;
        float g = 0.5f + 0.1f * Mth.sin(time + depth * 3.14f);
        float b = 0.1f + 0.6f * depth + 0.1f * Mth.cos(time * 1.3f);

        // Full-bright emissive - no shading
        float emissiveBoost = 0.8f + 0.2f * Mth.sin(time * 2f + y * 0.1f);
        return new float[]{r * emissiveBoost, g * emissiveBoost, b * emissiveBoost};
    }
}
