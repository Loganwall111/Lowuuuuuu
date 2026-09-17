package dev.siftcore.physics;

import dev.siftcore.SiftDimensions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/**
 * Applies a gentle horizontal current near a visual fluid sheet. The vertical
 * component is intentionally untouched: falling through a pool must retain the
 * player's tumble instead of becoming a colliding water volume.
 */
public final class SiftCurrentPhysics {
    private static final double LAYER_SPACING = 48.0D;
    private static final double CURRENT_BAND = 7.0D;
    private static final double MAX_HORIZONTAL_SPEED = 0.42D;

    private SiftCurrentPhysics() {
    }

    public static void tick(ServerWorld world) {
        if (!SiftDimensions.isSift(world)) {
            return;
        }

        long time = world.getTime();
        for (ServerPlayerEntity player : world.getPlayers()) {
            double fluidY = Math.rint((player.getY() - 16.0D) / LAYER_SPACING) * LAYER_SPACING + 16.0D;
            double distance = Math.abs(player.getY() - fluidY);
            if (distance > CURRENT_BAND) {
                continue;
            }

            double falloff = 1.0D - distance / CURRENT_BAND;
            double phase = time * 0.035D + player.getX() * 0.012D + player.getZ() * 0.009D;
            double strength = 0.0028D * falloff;
            Vec3d velocity = player.getVelocity();
            double x = velocity.x + Math.cos(phase) * strength;
            double z = velocity.z + Math.sin(phase * 1.31D) * strength;
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
