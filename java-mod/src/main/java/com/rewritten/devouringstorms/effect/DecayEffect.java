package com.rewritten.devouringstorms.effect;

import com.rewritten.devouringstorms.registry.ModDamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * THE DECAY. The plague in the blood.
 * Corruption damage pulses through you until it runs its course — or the amulet burns it out.
 */
public class DecayEffect extends MobEffect {

    public DecayEffect() {
        super(MobEffectCategory.HARMFUL, 0x7b2f9e);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.getHealth() > 1.0f) {
            entity.hurtServer(level, ModDamageTypes.decay(level, null), 1.0f);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
