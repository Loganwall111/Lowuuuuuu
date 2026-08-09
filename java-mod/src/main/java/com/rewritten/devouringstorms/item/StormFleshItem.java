package com.rewritten.devouringstorms.item;

import com.rewritten.devouringstorms.registry.ModStatusEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * STORM FLESH. Cut from what feeds on everything.
 * It is not food. You will eat it anyway.
 */
public class StormFleshItem extends Item {

    private static final FoodProperties STORM_MEAL = new FoodProperties.Builder()
        .nutrition(4)
        .saturationModifier(0.3f)
        .alwaysEdible()
        .build();

    public StormFleshItem(Properties props) {
        super(props.food(STORM_MEAL));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()) {
            // "We have been changed forever."
            entity.addEffect(new MobEffectInstance(ModStatusEffects.DECAY, 120, 0));
        }
        return result;
    }
}
