package dev.siftcore.physics;

import dev.siftcore.SiftDimensions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/**
 * Applies a gentle horizontal current inside a visual fluid volume. The
 * vertical component is intentionally untouched: falling through a pool must
 * retain the player's tumble instead of becoming a colliding water volume.
 */
public final class SiftCurrentPhysics {
    private static final double MAX_HORIZONTAL_SPEED = 0.42D;

    private SiftCurrentPhysics() {
    }

    public static void tick(ServerWorld world) {
        if (!SiftDimensions.isSift(world)) {
            return;
        }

        long time = world.getTime();
        for (ServerPlayerEntity player : world.getPlayers()) {
            int layer = SiftFluidField.nearestLayerIndex(player.getY());
            double xPosition = player.getX();
            double zPosition = player.getZ();
            double falloff = SiftFluidField.currentFalloff(player.getY())
                    * SiftFluidField.currentVolume(xPosition, zPosition, layer);
            if (falloff <= 0.0D) {
                continue;
            }
            double strength = 0.0028D * falloff;
            Vec3d velocity = player.getVelocity();
            double x = velocity.x + SiftFluidField.currentX(time, xPosition, zPosition) * strength;
            double z = velocity.z + SiftFluidField.currentZ(time, xPosition, zPosition) * strength;
            double horizontal = Math.sqrt(x * x + z * z);
            if (horizontal > MAX_HORIZONTAL_SPEED) {
                double scale = MAX_HORIZONTAL_SPEED / horizontal;
                x *= scale;
                z *= scale;
            }

            player.setVelocity(x, velocity.y, z);
            player.velocityModified = true;
        }
    }
}
