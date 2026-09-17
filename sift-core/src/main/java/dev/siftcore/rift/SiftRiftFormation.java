package dev.siftcore.rift;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Deterministic placement math for the first authored Sift formation.
 *
 * <p>The formation is intentionally local to the falling player: a twelve-slot
 * broken ring is mixed with three vertical strata, so the player sees a shaped
 * field instead of a cloud of unrelated random entities. The cell and cycle
 * inputs keep the same formation stable while it is in view and change it only
 * after the previous set has aged out.</p>
 */
public final class SiftRiftFormation {
    public static final int SLOTS = 12;
    private static final int CELL_SIZE = 64;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));

    private SiftRiftFormation() {
    }

    public static Placement placement(ServerWorld world, ServerPlayerEntity player, int slot) {
        int cellX = (int) Math.floor(player.getX() / CELL_SIZE);
        int cellZ = (int) Math.floor(player.getZ() / CELL_SIZE);
        long cycle = world.getTime() / 800L;
        long seed = mix(((long) cellX * 0x9E3779B97F4A7C15L)
                ^ ((long) cellZ * 0xC2B2AE3D27D4EB4FL)
                ^ (cycle * 0x165667B19E3779F9L)
                ^ slot);

        double phase = unit(seed) * Math.PI * 2.0D;
        double angle = phase + slot * GOLDEN_ANGLE;
        double radius = 24.0D + unit(seed + 1L) * 46.0D;
        double stratum = (slot % 3 - 1) * 24.0D;
        double wave = Math.sin(angle * 2.0D + phase) * 28.0D;
        double x = player.getX() + Math.cos(angle) * radius;
        double z = player.getZ() + Math.sin(angle) * radius;
        double y = player.getY() + stratum + wave + (unit(seed + 2L) - 0.5D) * 20.0D;
        float scale = (float) (1.75D + unit(seed + 3L) * 3.0D);
        float visualSeed = (float) (unit(seed + 4L) * 1000.0D);
        return new Placement(x, y, z, scale, visualSeed);
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double unit(long value) {
        return (mix(value) >>> 11) * 0x1.0p-53;
    }

    public record Placement(double x, double y, double z, float scale, float seed) {
    }
}
