package dev.siftcore.transfer;

import dev.siftcore.SiftDimensions;
import dev.siftcore.mob.SiftDrifterSpawner;
import dev.siftcore.rift.SiftRiftSpawner;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Server-authoritative dimension transfer used by both the void hook and test commands. */
public final class SiftTransfer {
    public static final double OVERWORLD_VOID_Y = -64.0D;
    private static final double SAFE_RETURN_Y = 96.0D;

    private SiftTransfer() {
    }

    /**
     * Called from the Entity tick mixin. It intentionally only arms in the
     * Overworld, so falling inside The Sift can never bounce a player back.
     */
    public static boolean tryVoidHandshake(ServerPlayerEntity player) {
        if (player.getWorld().isClient()
                || !player.getWorld().getRegistryKey().equals(World.OVERWORLD)
                || player.getY() >= OVERWORLD_VOID_Y) {
            return false;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }

        ServerWorld destination = server.getWorld(SiftDimensions.THE_SIFT);
        return destination != null && transferKeepingMotion(player, destination, player.getPos(), player.getVelocity());
    }

    /** Enters The Sift without treating a normal command invocation as a void fall. */
    public static boolean enterFromCommand(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null || SiftDimensions.isSift(player.getWorld())) {
            return false;
        }

        ServerWorld destination = server.getWorld(SiftDimensions.THE_SIFT);
        if (destination == null) {
            return false;
        }

        Vec3d position = player.getPos();
        if (position.y < OVERWORLD_VOID_Y) {
            position = new Vec3d(position.x, SAFE_RETURN_Y, position.z);
        }
        return transferKeepingMotion(player, destination, position, player.getVelocity());
    }

    /** Returns a player to a safe Overworld height so the next tick cannot re-enter The Sift. */
    public static boolean returnToOverworld(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null || !SiftDimensions.isSift(player.getWorld())) {
            return false;
        }

        ServerWorld destination = server.getWorld(World.OVERWORLD);
        if (destination == null) {
            return false;
        }

        return transferKeepingMotion(
                player,
                destination,
                new Vec3d(player.getX(), SAFE_RETURN_Y, player.getZ()),
                Vec3d.ZERO
        );
    }

    /**
     * Performs the vanilla server transfer, then restores the complete movement
     * state captured before it. moveToWorld normally chooses a destination spawn
     * point; restoring after it is what makes the handshake feel continuous.
     */
    public static boolean transferKeepingMotion(
            ServerPlayerEntity player,
            ServerWorld destination,
            Vec3d position,
            Vec3d velocity
    ) {
        float yaw = player.getYaw();
        float pitch = player.getPitch();
        float headYaw = player.getHeadYaw();
        float bodyYaw = player.getBodyYaw();
        float fallDistance = player.fallDistance;
        boolean noGravity = player.hasNoGravity();

        // The Sift is deliberately empty, so synchronously warm the small
        // destination window before the respawn packet is sent. This removes
        // the avoidable terrain-generation pause without creating a portal or
        // changing the player's coordinates.
        warmDestinationChunks(destination, position);

        Entity moved = player.moveToWorld(destination);
        if (!(moved instanceof ServerPlayerEntity target)) {
            return false;
        }

        target.refreshPositionAndAngles(position.x, position.y, position.z, yaw, pitch);
        target.setHeadYaw(headYaw);
        target.setBodyYaw(bodyYaw);
        target.setVelocity(velocity);
        target.velocityModified = true;
        target.setOnGround(false);
        target.setNoGravity(noGravity);
        target.setPortalCooldown(0);
        target.fallDistance = fallDistance;

        // The rifts and drifters are visual/ambient entities; they do not
        // participate in the handshake or add a collision surface.
        SiftRiftSpawner.spawnWelcomeRifts(target);
        SiftDrifterSpawner.spawnWelcomeDrifters(target);
        return true;
    }

    private static void warmDestinationChunks(ServerWorld destination, Vec3d position) {
        int centerChunkX = (int) Math.floor(position.x / 16.0D);
        int centerChunkZ = (int) Math.floor(position.z / 16.0D);
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                destination.getChunk(centerChunkX + offsetX, centerChunkZ + offsetZ);
            }
        }
    }
}
