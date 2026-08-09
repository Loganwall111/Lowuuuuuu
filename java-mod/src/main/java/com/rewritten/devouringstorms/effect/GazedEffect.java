package com.rewritten.devouringstorms.effect;

import com.rewritten.devouringstorms.DevouringStorms;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * THE WATCHER'S MARK. You looked back.
 * Movement feels heavier; the edges of your vision crawl — the client overlay does the rest.
 */
public class GazedEffect extends MobEffect {

    public GazedEffect() {
        super(MobEffectCategory.HARMFUL, 0x0a0a12);
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            DevouringStorms.id("gazed_slowness"),
            -0.15,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}
