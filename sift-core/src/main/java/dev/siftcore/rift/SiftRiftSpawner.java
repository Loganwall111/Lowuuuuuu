package dev.siftcore.rift;

import dev.siftcore.SiftCore;
import dev.siftcore.SiftDimensions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

/** Keeps a sparse, deterministic field of rifts around players in the empty dimension. */
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
                spawnAround(world, player, MAX_NEARBY_RIFTS - nearby);
            }
        }
    }

    public static void spawnWelcomeRifts(ServerPlayerEntity player) {
        if (!SiftDimensions.isSift(player.getWorld())) {
            return;
        }

        ServerWorld world = player.getServerWorld();
        spawnAround(world, player, 7);
    }

    private static void spawnAround(ServerWorld world, ServerPlayerEntity player, int amount) {
        for (int index = 0; index < amount; index++) {
            SiftRiftEntity rift = SiftCore.SIFT_RIFT.create(world);
            if (rift == null) {
                continue;
            }

            double angle = world.getRandom().nextDouble() * Math.PI * 2.0D;
            double radius = 14.0D + world.getRandom().nextDouble() * 72.0D;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            double y = player.getY() + (world.getRandom().nextDouble() - 0.35D) * 180.0D;

            rift.refreshPositionAndAngles(
                    x,
                    MathHelper.clamp(y, world.getBottomY() + 4.0D, world.getTopY() - 4.0D),
                    z,
                    world.getRandom().nextFloat() * 360.0F,
                    0.0F
            );
            rift.setRiftScale(1.5F + world.getRandom().nextFloat() * 3.5F);
            rift.setRiftSeed(world.getRandom().nextFloat() * 1000.0F);
            world.spawnEntity(rift);
        }
    }
}
