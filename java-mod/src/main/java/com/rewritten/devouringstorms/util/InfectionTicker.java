package com.rewritten.devouringstorms.util;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.entity.MassgEntity;
import com.rewritten.devouringstorms.registry.ModEntities;
import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.ModStatusEffects;
import com.rewritten.devouringstorms.storm.MassgPhase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * THE INFECTION. "Then the plague came."
 *
 * The corruption is not a place, it is a process. MASSG seeds Decay into the blood of
 * everything hostile that drifts too close; mobs that soak in it long enough stop being
 * what they were — they come back as Withered Symbionts. Inside the Decayed Reality the
 * same conversion takes anything the rot has touched. The Decay block spreads through
 * terrain on its own; this system handles the living.
 *
 * Runs slowly (every 60 server ticks) and only around players, to keep the world honest.
 */
public final class InfectionTicker {

    private static final ResourceKey<Level> DECAYED_REALITY =
        ResourceKey.create(Registries.DIMENSION, DevouringStorms.id("decayed_reality"));

    private static final double SCAN_RADIUS = 48.0;
    private static final double STORM_AURA = 40.0;
    private static final double CONVERT_CHANCE = 0.22;
    private static final double REALM_CONVERT_CHANCE = 0.35;

    private InfectionTicker() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(InfectionTicker::onWorldTick);
    }

    private static void onWorldTick(ServerLevel level) {
        if (!DevouringConfig.getBool("infection", true)) return;
        if (level.getGameTime() % 60 != 0) return;
        if (level.players().isEmpty()) return;

        // ---- the storm sows the plague around itself ----
        for (MassgEntity storm : level.getEntitiesOfClass(MassgEntity.class, wholeWorld(level))) {
            if (storm.getDeadTicks() >= 0) continue;                       // playing dead sows nothing
            if (!storm.getPhase().atLeast(MassgPhase.HUNGER)) continue;    // it must be feeding to infect
            AABB aura = storm.getBoundingBox().inflate(STORM_AURA);
            for (Monster mob : level.getEntitiesOfClass(Monster.class, aura, InfectionTicker::isConvertible)) {
                if (mob.hasEffect(ModStatusEffects.DECAY)) continue;
                mob.addEffect(new MobEffectInstance(ModStatusEffects.DECAY, 400, 0));
            }
        }

        boolean inRealm = level.dimension().equals(DECAYED_REALITY);
        // ---- prolonged decay converts the living ----
        for (var player : level.players()) {
            AABB box = player.getBoundingBox().inflate(SCAN_RADIUS);
            for (Monster mob : level.getEntitiesOfClass(Monster.class, box, InfectionTicker::isConvertible)) {
                if (!mob.hasEffect(ModStatusEffects.DECAY)) continue;
                double chance = inRealm ? REALM_CONVERT_CHANCE : CONVERT_CHANCE;
                if (mob.getRandom().nextDouble() >= chance) continue;

                // Inside the realm the soak goes deeper: they come back as THE TAKEN —
                // slower, stronger, angrier. Outside, the gentler symbiont bloom.
                var spawnAt = mob.position();
                var rotation = new float[] { mob.getYRot(), mob.getXRot() };
                var risen = inRealm ? (net.minecraft.world.entity.Mob) ModEntities.THE_TAKEN.create(level)
                                    : ModEntities.WITHERED_SYMBIONT.create(level);
                if (risen == null) continue;
                risen.moveTo(spawnAt.x, spawnAt.y, spawnAt.z, rotation[0], rotation[1]);
                level.addFreshEntity(risen);
                level.sendParticles(ModParticles.GLITCH,
                    mob.getX(), mob.getY() + 0.8, mob.getZ(), 30, 0.5, 0.6, 0.5, 0.1);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,
                    mob.getX(), mob.getY() + 0.8, mob.getZ(), 12, 0.4, 0.5, 0.4, 0.05);
                mob.discard();
            }

            // ---- the taken: villagers hold out the longest, then the decay wins ----
            for (net.minecraft.world.entity.npc.villager.Villager villager :
                    level.getEntitiesOfClass(net.minecraft.world.entity.npc.villager.Villager.class, box)) {
                double rate = inRealm ? REALM_CONVERT_CHANCE : CONVERT_CHANCE * 0.35;
                if (!inRealm && !villager.hasEffect(ModStatusEffects.DECAY)) {
                    if (villager.getRandom().nextDouble() < 0.002)
                        villager.addEffect(new MobEffectInstance(ModStatusEffects.DECAY, 400, 0));
                    continue;
                }
                if (villager.getRandom().nextDouble() >= rate) continue;
                var taken = ModEntities.THE_TAKEN.create(level);
                if (taken == null) continue;
                taken.moveTo(villager.getX(), villager.getY(), villager.getZ(),
                    villager.getYRot(), villager.getXRot());
                level.addFreshEntity(taken);
                level.sendParticles(ModParticles.GLITCH,
                    villager.getX(), villager.getY() + 0.9, villager.getZ(), 40, 0.5, 0.7, 0.5, 0.12);
                villager.discard();
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§2§oYou hear a door that will never open again.§r"));
            }
        }
    }

    private static AABB wholeWorld(ServerLevel level) {
        return new AABB(
            level.getWorldBorder().getMinX(), level.getMinY(), level.getWorldBorder().getMinZ(),
            level.getWorldBorder().getMaxX(), level.getMaxY(), level.getWorldBorder().getMaxZ());
    }

    /** Hostile, living, and not already one of ours. */
    private static boolean isConvertible(LivingEntity entity) {
        return entity.isAlive();
    }
}
