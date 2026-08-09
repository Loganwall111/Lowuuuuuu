package com.rewritten.devouringstorms.item;

import com.rewritten.devouringstorms.registry.ModStatusEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * AMULET OF DECAY. Woven from storm dust and sheer refusal.
 * While carried, it burns the plague out of your blood every few seconds.
 */
public class AmuletOfDecayItem extends Item {

    private static final int PURGE_INTERVAL = 80; // ticks

    public AmuletOfDecayItem(Properties props) {
        super(props);
    }

    // Mappings note: 1.21.2+ passes an EquipmentSlot here instead of (slot, selected).
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(level instanceof ServerLevel server) || !(entity instanceof LivingEntity living)) return;
        if (level.getGameTime() % PURGE_INTERVAL != 0) return;
        if (!living.hasEffect(ModStatusEffects.DECAY)) return;

        living.removeEffect(ModStatusEffects.DECAY);
        living.heal(1.0f);
        server.sendParticles(ParticleTypes.LAVA, living.getX(), living.getY() + 1.0, living.getZ(), 4, 0.3, 0.4, 0.3, 0.0);
    }
}
