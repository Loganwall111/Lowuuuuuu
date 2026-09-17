package dev.siftcore.terrain;

import dev.siftcore.SiftDimensions;
import dev.siftcore.block.SiftBlocks;
import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Adds sparse generated Sift shelves while a player falls through the void.
 *
 * <p>This is intentionally a light-weight first terrain pass rather than a
 * replacement chunk generator: shelves are deterministic per horizontal and
 * vertical cell, use the custom Sift block palette, and only write into air.
 * The original free-fall route remains open between shelves.</p>
 */
public final class SiftTerrainSpawner {
    private static final int CELL_SIZE = 64;
    private static final int SHELF_RADIUS = 9;
    private static final int TICK_INTERVAL = 160;

    private SiftTerrainSpawner() {
    }

    public static void tick(ServerWorld world) {
        if (!SiftDimensions.isSift(world) || world.getTime() % TICK_INTERVAL != 0L) {
            return;
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            generateShelf(world, player);
        }
    }

    public static void generateWelcomeShelf(ServerPlayerEntity player) {
        if (SiftDimensions.isSift(player.getWorld())) {
            generateShelf(player.getServerWorld(), player);
        }
    }

    private static void generateShelf(ServerWorld world, ServerPlayerEntity player) {
        int cellX = (int) Math.floor(player.getX() / CELL_SIZE);
        int cellZ = (int) Math.floor(player.getZ() / CELL_SIZE);
        int cellY = (int) Math.floor(player.getY() / CELL_SIZE);
        int centerX = cellX * CELL_SIZE + CELL_SIZE / 2;
        int centerZ = cellZ * CELL_SIZE + CELL_SIZE / 2;
        int baseY = cellY * CELL_SIZE + 4;
        if (baseY < world.getBottomY() + 2 || baseY + 6 >= world.getTopY()) {
            return;
        }

        long seed = mix(((long) cellX * 0x9E3779B97F4A7C15L)
                ^ ((long) cellZ * 0xC2B2AE3D27D4EB4FL)
                ^ ((long) cellY * 0x165667B19E3779F9L));
        BlockPos marker = new BlockPos(centerX, baseY + 2, centerZ);
        if (world.getBlockState(marker).isOf(SiftBlocks.SIFT_MOSS)) {
            return;
        }

        for (int offsetX = -SHELF_RADIUS; offsetX <= SHELF_RADIUS; offsetX++) {
            for (int offsetZ = -SHELF_RADIUS; offsetZ <= SHELF_RADIUS; offsetZ++) {
                double distance = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
                if (distance > SHELF_RADIUS - 0.35D) {
                    continue;
                }

                int thickness = distance > SHELF_RADIUS * 0.66D ? 1 : 2;
                for (int depth = 0; depth < thickness; depth++) {
                    placeIfAir(world, new BlockPos(centerX + offsetX, baseY + depth, centerZ + offsetZ), SiftBlocks.SIFTSTONE);
                }
                if (distance < SHELF_RADIUS * 0.82D) {
                    placeIfAir(world, new BlockPos(centerX + offsetX, baseY + thickness, centerZ + offsetZ), SiftBlocks.SIFT_MOSS);
                }
            }
        }

        // A few deterministic crystal fins make each generated shelf readable
        // from a distance without turning it into a dense structure.
        for (int index = 0; index < 5; index++) {
            long crystalSeed = mix(seed + index * 31L);
            if (unit(crystalSeed) < 0.42D) {
                continue;
            }
            int offsetX = (int) (unit(crystalSeed + 1L) * 12.0D) - 6;
            int offsetZ = (int) (unit(crystalSeed + 2L) * 12.0D) - 6;
            int height = 1 + (int) (unit(crystalSeed + 3L) * 3.0D);
            for (int heightIndex = 0; heightIndex < height; heightIndex++) {
                placeIfAir(
                        world,
                        new BlockPos(centerX + offsetX, baseY + 3 + heightIndex, centerZ + offsetZ),
                        SiftBlocks.RIFT_CRYSTAL
                );
            }
        }
    }

    private static void placeIfAir(ServerWorld world, BlockPos pos, Block block) {
        if (world.getBlockState(pos).isAir()) {
            world.setBlockState(pos, block.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double unit(long value) {
        return (mix(value) >>> 11) * 0x1.0p-53;
    }
}
