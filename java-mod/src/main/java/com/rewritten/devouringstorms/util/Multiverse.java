package com.rewritten.devouringstorms.util;

import com.rewritten.devouringstorms.registry.ModBlocks;
import com.rewritten.devouringstorms.registry.ModEntities;
import com.rewritten.devouringstorms.world.ModDimensions;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * MULTIVERSE ring stops. The first time anyone steps through a frayed tear into the Fray
 * or the Echo Fields, that node assembles its little waystation — a platform in the storm's
 * own palette, and a person the ring had already taken, waiting.
 *
 * The Fray has **Travis**. The Echo Fields have **Tonya**. Anna said there were others.
 */
public final class Multiverse {

    private static final Set<ResourceKey<Level>> ASSEMBLED = EnumSet.noneOf(ResourceKey.class);

    private Multiverse() {
    }

    /** Called by frayed tears right after a traveller crosses. */
    public static void ensurePocket(ServerLevel destination) {
        if (!ASSEMBLED.add(destination.dimension())) return;

        BlockPos base = new BlockPos(0, 0, 0);
        int y = destination.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
        if (y < destination.getMinY() + 2) y = destination.getSeaLevel() + 2;
        base = new BlockPos(0, y, 0);

        if (destination.dimension().equals(ModDimensions.FRAY_LEVEL_KEY)) {
            assembleOutpost(destination, base, ModBlocks.DECAYED_SOIL.defaultBlockState());
            var travis = ModEntities.TRAVIS.create(destination);
            if (travis != null) {
                travis.moveTo(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 2.5, 180.0f, 0.0f);
                destination.addFreshEntity(travis);
            }
        } else if (destination.dimension().equals(ModDimensions.ECHO_LEVEL_KEY)) {
            assembleOutpost(destination, base, net.minecraft.world.level.block.Blocks.END_STONE.defaultBlockState());
            var tonya = ModEntities.TONYA.create(destination);
            if (tonya != null) {
                tonya.moveTo(base.getX() + 0.5, base.getY() + 1.2, base.getZ() + 2.5, 180.0f, 0.0f);
                destination.addFreshEntity(tonya);
            }
        }
    }

    private static void assembleOutpost(ServerLevel level, BlockPos base, net.minecraft.world.level.block.state.BlockState floor) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                level.setBlock(base.offset(dx, -1, dz), floor, 3);
            }
        }
        for (int c : new int[] { -3, 3 }) {
            for (int h = 0; h < 3; h++) {
                level.setBlock(base.offset(c, h, 3), ModBlocks.ROT_LOG.defaultBlockState(), 3);
                level.setBlock(base.offset(-c, h, -3), ModBlocks.ROT_LOG.defaultBlockState(), 3);
            }
        }
        // the tear onward, north edge — the ring goes ever on
        level.setBlock(base.offset(0, 0, -3), ModBlocks.FRAYED_TEAR.defaultBlockState(), 3);
    }
}
