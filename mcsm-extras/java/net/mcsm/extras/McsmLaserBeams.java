package net.mcsm.extras;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * V2 Phase 5 - Laserbeam shooting stars
 * Rare VFX event where laserbeam shoots across sky like shooting star but with beam
 */
public final class McsmLaserBeams {

    private static long lastBeamTime = 0;

    private McsmLaserBeams() {}

    public static void boot() {
        try {
            ServerTickEvents.END_LEVEL_TICK.register(level -> tick(level));
        } catch (Throwable ignored) {}
    }

    private static void tick(ServerLevel level) {
        try {
            if (level == null) return;
            String dim = level.dimension().identifier().toString();
            if (!dim.contains("overworld") && !dim.equals("minecraft:overworld")) return;

            long gameTime = level.getGameTime();
            if (gameTime - lastBeamTime < 6000) return; // at most every 5 min
            if (gameTime % 100 != 0) return;

            // Night only, rare
            long dayTime = gameTime % 24000;
            boolean isNight = dayTime > 13000 && dayTime < 23000;
            if (!isNight) return;
            if (Math.random() > 0.015) return; // 1.5% chance every 5 sec at night

            for (ServerPlayer player : level.players()) {
                if (player == null) continue;
                if (Math.random() < 0.7) {
                    triggerLaserBeam(level, player);
                    lastBeamTime = gameTime;
                    break;
                }
            }

        } catch (Throwable ignored) {}
    }

    private static void triggerLaserBeam(ServerLevel level, ServerPlayer player) {
        try {
            Vec3 start = new Vec3(
                player.getX() + (Math.random() - 0.5) * 300,
                player.getY() + 180 + Math.random() * 80,
                player.getZ() + (Math.random() - 0.5) * 300
            );
            Vec3 end = new Vec3(
                start.x + (Math.random() - 0.5) * 200,
                player.getY() + 20 + Math.random() * 40,
                start.z + (Math.random() - 0.5) * 200
            );

            // Server side - particles along beam
            Vec3 dir = end.subtract(start);
            double length = dir.length();
            Vec3 norm = dir.normalize();
            int steps = (int)(length / 3);

            for (int i = 0; i < steps; i++) {
                final double prog = (double)i / steps;
                final double x = start.x + norm.x * prog * length;
                final double y = start.y + norm.y * prog * length;
                final double z = start.z + norm.z * prog * length;

                // Laser beam core - blue/green shifting
                if (i % 2 == 0) {
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                        x, y, z, 1, 0.1, 0.1, 0.1, 0.01);
                }
                // Chromatic blur
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                    x, y, z, 1, 0.2, 0.2, 0.2, 0.02);
            }

            // Impact explosion
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                end.x, end.y, end.z, 2, 0.5, 0.5, 0.5, 0.1);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.GLOW,
                end.x, end.y, end.z, 20, 1, 1, 1, 0.2);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§b§l[LASERBEAM] §fShooting star with laserbeam! §9Blue §aGreen §0Black §fshifting... §7Impact at " + (int)end.x + "," + (int)end.z));

        } catch (Throwable ignored) {}
    }
}
