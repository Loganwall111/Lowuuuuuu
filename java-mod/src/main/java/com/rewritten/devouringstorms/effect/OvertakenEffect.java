package com.rewritten.devouringstorms.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * OVERTAKEN — the Monstrosity's channel is broadcasting through your retina.
 * Harmless in the numbers; the client overlay interprets it as the colourful world-glitch:
 * the sky and the ground both go to colour-static for the duration.
 */
public class OvertakenEffect extends MobEffect {

    public OvertakenEffect() {
        super(MobEffectCategory.HARMFUL, 0xff3fc8);
    }
}
