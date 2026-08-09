package com.rewritten.devouringstorms.util;

import com.rewritten.devouringstorms.world.ModDimensions;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Rift travel between the Overworld and the Decayed Reality.
 *
 * NOTE: the cross-dimension teleport call is isolated here on purpose — it is the one API whose
 * signature shifts between mappings/versions (teleportTo vs TeleportTransition). If your target
 * version renamed it, adjust THIS METHOD only.
 */
public final class RiftTravel {

    private RiftTravel() {
    }

    /** Send an entity to the other side: Decayed Reality ⟷ Overworld. */
    public static void travel(Entity entity, ResourceKey<Level> targetDimension) {
        if (!(entity.level() instanceof ServerLevel current)) return;
        ServerLevel destination = current.getServer().getLevel(targetDimension);
        if (destination == null) return;

        BlockPos landing = findLanding(destination, entity);
        double x = landing.getX() + 0.5;
        double y = landing.getY() + 1.0;
        double z = landing.getZ() + 0.5;

        // Mojang-mapped cross-dimension teleport (26.x). See class-level NOTE.
        entity.teleportTo(destination, x, y, z, Set.of(), entity.getYRot(), entity.getXRot(), false);
    }

    /** Send an entity to an exact spot in a target level (belly pockets, outposts, ring stops). */
    public static void travelTo(Entity entity, ServerLevel destination, net.minecraft.world.phys.Vec3 position) {
        // Mojang-mapped cross-dimension teleport (26.x). See class-level NOTE.
        entity.teleportTo(destination, position.x, position.y, position.z,
            Set.of(), entity.getYRot(), entity.getXRot(), false);
    }

    private static BlockPos findLanding(ServerLevel level, Entity entity) {
        if (level.dimension() == ModDimensions.DECAYED_LEVEL_KEY) {
            // arrive beside the platform, not inside the return portal
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 3);
            if (y < level.getMinY()) y = 64;
            return new BlockPos(0, y, 3);
        }
        BlockPos base = entity.blockPosition();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base.getX(), base.getZ());
        return new BlockPos(base.getX(), Math.max(y, level.getSeaLevel()), base.getZ());
    }
}
