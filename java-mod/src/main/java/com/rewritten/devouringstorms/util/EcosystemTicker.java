package com.rewritten.devouringstorms.util;

import com.rewritten.devouringstorms.world.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * THE LIVING ECOSYSTEMS. "ecosystems that start at the Stone Age and go up to the Multiverse
 * Age, generating and living in real time." Each planet carries an age counter in its own
 * timeline; ages turn over roughly every real-forty-minutes and the planet announces it —
 * and the age changes what the air feels like: denser spores when young, heavier static when old.
 */
public final class EcosystemTicker {

    /** The ages each planet moves through, in broadcast order. The last one loops forever. */
    private static final String[] AGES = {
        "§eTHE STONE AGE §7— the morning planet",
        "§eTHE BRONZE AGE §7— the first fires were fires twice",
        "§6THE IRON AGE §7— iron answers iron, politely",
        "§6THE INDUSTRIAL AGE §7— the hum starts where the light ends",
        "§dTHE DIGITAL AGE §7— things begin listening to each other",
        "§5§lTHE MULTIVERSE AGE §r§d— the broadcast plays to itself",
    };

    /** Game-ticks between ages. Roughly three quarters of an hour each. */
    private static final int AGE_PERIOD = 54000;

    /** Session-scoped age memory per dimension path. */
    private static final java.util.concurrent.ConcurrentHashMap<String, Integer> SEEN_AGES =
        new java.util.concurrent.ConcurrentHashMap<>();

    private EcosystemTicker() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(EcosystemTicker::onWorldTick);
    }

    private static void onWorldTick(ServerLevel level) {
        if (!DevouringConfig.getBool("planets", true)) return;
        var dimKey = level.dimension();
        boolean isPlanet = dimKey.equals(ModDimensions.AURTH_LEVEL_KEY)
            || dimKey.equals(ModDimensions.VOLMAR_LEVEL_KEY)
            || dimKey.equals(ModDimensions.NEXUS_LEVEL_KEY);
        if (!isPlanet) return;

        long t = level.getGameTime();
        int age = (int) Math.min(AGES.length - 1, t / AGE_PERIOD);
        String key = dimKey.identifier().getPath();
        Integer seen = SEEN_AGES.get(key);

        // cross the boundary: the planet announces its age (session-scoped — eras are loud)
        if (seen == null || age > seen) {
            SEEN_AGES.put(key, age);
            String label = dimKey.equals(ModDimensions.AURTH_LEVEL_KEY) ? "AURTH"
                : dimKey.equals(ModDimensions.VOLMAR_LEVEL_KEY) ? "VOLMAR" : "NEXUS";
            for (var player : level.players()) {
                player.sendSystemMessage(Component.literal("§f§o>><< era shift on §r§l" + label
                    + "§r. §f" + AGES[age]));
                player.sendSystemMessage(Component.literal("§8§othe ecosystem rebalances. the spores differ. the light remembers."));
            }
        }

        // ambient age décor: young air sparkles, old air streaks
        if (t % 40 == 0 && !level.players().isEmpty()) {
            var player = level.players().get(0);
            var random = level.random;
            double ox = player.getX() + random.nextGaussian() * 20;
            double oy = player.getY() + 4 + random.nextGaussian() * 6;
            double oz = player.getZ() + random.nextGaussian() * 20;
            var particle = switch (Math.min(age, 5)) {
                case 0, 1 -> net.minecraft.core.particles.ParticleTypes.ASH;
                case 2, 3 -> net.minecraft.core.particles.ParticleTypes.SMOKE;
                default -> com.rewritten.devouringstorms.registry.ModParticles.GLITCH;
            };
            level.sendParticles(particle, ox, oy, oz, 2, 0.6, 0.5, 0.6, 0.01);
        }
    }
}
