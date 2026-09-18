package net.mcsm.extras;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

/**
 * BUILD #485 -- COMPLETELY DISABLE SUFFOCATING IN VOID ENTIRELY
 * User request: make an option to completely disable suffocating in void entirely.
 * So that means giving regular void but it's also a pocket dimension.
 * Option voidNoSuffocation = true disables IN_WALL and DROWN damage in void dimension.
 */
public final class McsmVoidNoSuffocation {

    private McsmVoidNoSuffocation() {}

    public static void register() {
        try {
            ServerLivingEntityEvents.ALLOW_DAMAGE.register((LivingEntity entity, net.minecraft.world.damagesource.DamageSource source, float amount) -> {
                try {
                    if (!McsmExtrasConfig.voidNoSuffocation) return true;
                    if (!(entity instanceof ServerPlayer player)) return true;
                    if (!player.level().dimension().equals(McsmVoid.DIMENSION)) {
                        // Also protect during long merged fall in overworld void (hundreds blocks)
                        if (McsmExtrasConfig.voidMerged && player.getY() < player.level().getMinY() - 10) {
                            if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.FALL) || source.is(DamageTypes.OUT_OF_WORLD)) {
                                return false;
                            }
                        }
                        return true;
                    }
                    // In void dimension, disable suffocation, drowning, fall, out of world (if no knife)
                    if (source.is(DamageTypes.IN_WALL)) return false;
                    if (source.is(DamageTypes.DROWN)) return false;
                    if (source.is(DamageTypes.FALL)) return false;
                    // OUT_OF_WORLD only disabled if has knife/rudder to cut open, else still protect for merged hundreds blocks feel
                    if (source.is(DamageTypes.OUT_OF_WORLD) && McsmExtrasConfig.voidMerged) {
                        // Allow void damage only if not in protected descent? For merged, we want no damage during hundreds blocks fall
                        if (McsmVoidDescent.diving(player) || McsmExtrasConfig.voidNoSuffocation) {
                            return false;
                        }
                    }
                } catch (Throwable ignored) {}
                return true;
            });
            System.out.println("[ds] void no-suffocation guard active (voidNoSuffocation=" + McsmExtrasConfig.voidNoSuffocation + " merged=" + McsmExtrasConfig.voidMerged + ")");
        } catch (Throwable t) {
            System.err.println("[ds] could not hook no-suffocation guard: " + t);
        }
    }
}
