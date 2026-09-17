package dev.siftcore.mob;

import dev.siftcore.SiftCore;
import dev.siftcore.SiftDimensions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

/** Keeps a small population of non-colliding Sift drifters near falling players. */
public final class SiftDrifterSpawner {
    private static final int MAX_NEARBY_DRIFTERS = 6;
    private static final double DRIFTER_RANGE = 112.0D;

    private SiftDrifterSpawner() {
    }

    public static void tick(ServerWorld world) {
        if (!SiftDimensions.isSift(world) || world.getTime() % 40L != 0L) {
            return;
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            int nearby = world.getEntitiesByType(
                    SiftCore.SIFT_DRIFTER,
                    drifter -> drifter.squaredDistanceTo(player) < DRIFTER_RANGE * DRIFTER_RANGE
            ).size();
            if (nearby < MAX_NEARBY_DRIFTERS) {
                spawnAround(world, player, MAX_NEARBY_DRIFTERS - nearby);
            }
        }
    }

    public static void spawnWelcomeDrifters(ServerPlayerEntity player) {
        if (SiftDimensions.isSift(player.getWorld())) {
            spawnAround(player.getServerWorld(), player, 3);
        }
    }

    private static void spawnAround(ServerWorld world, ServerPlayerEntity player, int amount) {
        for (int index = 0; index < amount; index++) {
            SiftDrifterEntity drifter = SiftCore.SIFT_DRIFTER.create(world);
            if (drifter == null) {
                continue;
            }

            double angle = world.getRandom().nextDouble() * Math.PI * 2.0D;
            double radius = 12.0D + world.getRandom().nextDouble() * 70.0D;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            double y = player.getY() + (world.getRandom().nextDouble() - 0.5D) * 96.0D;
            drifter.refreshPositionAndAngles(
                    x,
                    MathHelper.clamp(y, world.getBottomY() + 4.0D, world.getTopY() - 4.0D),
                    z,
                    world.getRandom().nextFloat() * 360.0F,
                    0.0F
            );
            drifter.setDrifterScale(0.65F + world.getRandom().nextFloat() * 0.85F);
            drifter.setDrifterSeed(world.getRandom().nextFloat() * 1000.0F);
            drifter.setPersistent();
            world.spawnEntity(drifter);
        }
    }
}
