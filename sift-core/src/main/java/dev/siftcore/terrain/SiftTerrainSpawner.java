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
    private static final double NEIGHBOUR_SHELF_CHANCE = 0.28D;

    private SiftTerrainSpawner() {
    }

    public static void tick(ServerWorld world) {
        if (!SiftDimensions.isSift(world) || world.getTime() % TICK_INTERVAL != 0L) {
            return;
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            generateShelvesAround(world, player);
        }
    }

    public static void generateWelcomeShelf(ServerPlayerEntity player) {
        if (SiftDimensions.isSift(player.getWorld())) {
            generateShelvesAround(player.getServerWorld(), player);
        }
    }

    private static void generateShelvesAround(ServerWorld world, ServerPlayerEntity player) {
        int cellX = (int) Math.floor(player.getX() / CELL_SIZE);
        int cellZ = (int) Math.floor(player.getZ() / CELL_SIZE);
        int cellY = (int) Math.floor(player.getY() / CELL_SIZE);
        generateCell(world, cellX, cellZ, cellY);

        // A few neighbouring cells make the descent feel like a sparse terrain
        // field instead of one platform repeatedly appearing on the centerline.
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                if (offsetX == 0 && offsetZ == 0) {
                    continue;
                }
                long neighbourSeed = cellSeed(cellX + offsetX, cellZ + offsetZ, cellY);
                if (unit(neighbourSeed) < NEIGHBOUR_SHELF_CHANCE) {
                    generateCell(world, cellX + offsetX, cellZ + offsetZ, cellY);
                }
            }
        }
    }

    private static void generateCell(ServerWorld world, int cellX, int cellZ, int cellY) {
        int centerX = cellX * CELL_SIZE + CELL_SIZE / 2;
        int centerZ = cellZ * CELL_SIZE + CELL_SIZE / 2;
        int baseY = cellY * CELL_SIZE + 4;
        if (baseY < world.getBottomY() + 2 || baseY + 6 >= world.getTopY()) {
            return;
        }

        long seed = cellSeed(cellX, cellZ, cellY);
        BlockPos marker = new BlockPos(centerX, baseY + 2, centerZ);
        if (world.getBlockState(marker).isOf(SiftBlocks.SIFT_MOSS)) {
            return;
        }

        double outlinePhase = unit(seed ^ 0xD1B54A32D192ED03L) * Math.PI * 2.0D;
        for (int offsetX = -SHELF_RADIUS; offsetX <= SHELF_RADIUS; offsetX++) {
            for (int offsetZ = -SHELF_RADIUS; offsetZ <= SHELF_RADIUS; offsetZ++) {
                double distance = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
                double angle = Math.atan2(offsetZ, offsetX);
                double outlineRadius = SHELF_RADIUS - 0.35D
                        + Math.sin(angle * 3.0D + outlinePhase) * 1.25D
                        + Math.sin(angle * 7.0D - outlinePhase * 0.63D) * 0.65D;
                if (distance > outlineRadius) {
                    continue;
                }

                long tileSeed = seed
                        ^ ((long) (offsetX + CELL_SIZE) * 0x9E3779B97F4A7C15L)
                        ^ ((long) (offsetZ + CELL_SIZE) * 0xC2B2AE3D27D4EB4FL);
                double tileVariation = unit(tileSeed);
                boolean centerPatch = offsetX == 0 && offsetZ == 0;
                int thickness = centerPatch
                        ? 2
                        : (distance > SHELF_RADIUS * 0.66D || tileVariation < 0.08D ? 1 : 2);
                for (int depth = 0; depth < thickness; depth++) {
                    placeIfAir(
                            world,
                            new BlockPos(centerX + offsetX, baseY + depth, centerZ + offsetZ),
                            SiftBlocks.SIFTSTONE
                    );
                }

                if (distance < SHELF_RADIUS * 0.82D && (centerPatch || tileVariation > 0.16D)) {
                    placeIfAir(
                            world,
                            new BlockPos(centerX + offsetX, baseY + thickness, centerZ + offsetZ),
                            SiftBlocks.SIFT_MOSS
                    );
                }

                // Sparse underside teeth break up the perfect disk silhouette
                // while staying entirely inside the generated shelf cell.
                if (distance > SHELF_RADIUS * 0.42D && tileVariation > 0.91D) {
                    placeIfAir(
                            world,
                            new BlockPos(centerX + offsetX, baseY - 1, centerZ + offsetZ),
                            SiftBlocks.SIFTSTONE
                    );
                    if (tileVariation > 0.975D) {
                        placeIfAir(
                                world,
                                new BlockPos(centerX + offsetX, baseY - 2, centerZ + offsetZ),
                                SiftBlocks.SIFTSTONE
                        );
                    }
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

    private static long cellSeed(int cellX, int cellZ, int cellY) {
        return mix(((long) cellX * 0x9E3779B97F4A7C15L)
                ^ ((long) cellZ * 0xC2B2AE3D27D4EB4FL)
                ^ ((long) cellY * 0x165667B19E3779F9L));
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
