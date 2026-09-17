package dev.siftcore.encounter;

import dev.siftcore.SiftCore;
import dev.siftcore.SiftDimensions;
import dev.siftcore.rift.SiftRiftEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Drives the first authored Sift encounter beat without adding a collision or
 * interrupting the fall. Each deterministic rift formation swells, snaps, and
 * settles on the same 800-tick cadence used by the formation seed.
 */
public final class SiftEncounterController {
    private static final long FORMATION_CYCLE = 800L;

    private SiftEncounterController() {
    }

    public static void tick(ServerWorld world) {
        if (!SiftDimensions.isSift(world) || world.getTime() % 20L != 0L) {
            return;
        }

        long time = world.getTime();
        for (SiftRiftEntity rift : world.getEntitiesByType(SiftCore.SIFT_RIFT, entity -> true)) {
            rift.setChoreography(choreography(time, rift.getRiftSeed()));
        }
    }

    private static float choreography(long time, float seed) {
        double cycle = (time % FORMATION_CYCLE) / (double) FORMATION_CYCLE;
        double phase = cycle * Math.PI * 2.0D + seed * 0.031D;
        double swell = smoothPulse(Math.sin(phase));
        double snap = smoothPulse(Math.sin(phase * 2.0D - 0.7D));
        double settle = 1.0D - smoothPulse(Math.sin(phase - 2.35D));
        return (float) clamp(swell * 0.54D + snap * 0.31D + settle * 0.15D, 0.0D, 1.0D);
    }

    private static double smoothPulse(double value) {
        double normalized = value * 0.5D + 0.5D;
        return normalized * normalized * (3.0D - 2.0D * normalized);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
