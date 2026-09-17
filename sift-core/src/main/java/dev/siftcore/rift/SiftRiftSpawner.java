package dev.siftcore.rift;

import dev.siftcore.SiftCore;
import dev.siftcore.SiftDimensions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

/** Keeps a sparse, deterministic field of rifts around players in the open Sift. */
public final class SiftRiftSpawner {
    private static final int MAX_NEARBY_RIFTS = 12;
    private static final double RIFT_RANGE = 128.0D;

    private SiftRiftSpawner() {
    }

    public static void tick(ServerWorld world) {
        if (!SiftDimensions.isSift(world) || world.getTime() % 80L != 0L) {
            return;
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            int nearby = world.getEntitiesByType(
                    SiftCore.SIFT_RIFT,
                    rift -> rift.squaredDistanceTo(player) < RIFT_RANGE * RIFT_RANGE
            ).size();
            if (nearby < MAX_NEARBY_RIFTS) {
                spawnAround(world, player, MAX_NEARBY_RIFTS - nearby, nearby);
            }
        }
    }

    public static void spawnWelcomeRifts(ServerPlayerEntity player) {
        if (!SiftDimensions.isSift(player.getWorld())) {
            return;
        }

        ServerWorld world = player.getServerWorld();
        spawnAround(world, player, 7, 0);
    }

    private static void spawnAround(ServerWorld world, ServerPlayerEntity player, int amount, int slotOffset) {
        for (int index = 0; index < amount; index++) {
            SiftRiftEntity rift = SiftCore.SIFT_RIFT.create(world);
            if (rift == null) {
                continue;
            }

            SiftRiftFormation.Placement placement = SiftRiftFormation.placement(
                    world,
                    player,
                    (slotOffset + index) % SiftRiftFormation.SLOTS
            );
            rift.refreshPositionAndAngles(
                    placement.x(),
                    MathHelper.clamp(placement.y(), world.getBottomY() + 4.0D, world.getTopY() - 4.0D),
                    placement.z(),
                    (float) (placement.seed() * 0.37D),
                    0.0F
            );
            rift.setRiftScale(placement.scale());
            rift.setRiftSeed(placement.seed());
            world.spawnEntity(rift);
        }
    }
}
