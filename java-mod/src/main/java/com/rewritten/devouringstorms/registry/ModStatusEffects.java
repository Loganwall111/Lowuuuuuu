package com.rewritten.devouringstorms.registry;

import com.rewritten.devouringstorms.DevouringStorms;
import com.rewritten.devouringstorms.effect.DecayEffect;
import com.rewritten.devouringstorms.effect.GazedEffect;
import com.rewritten.devouringstorms.effect.OvertakenEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

/**
 * Status effects.
 *  - DECAY: the plague. Wither-like damage over time; cured/mitigated by the Amulet of Decay.
 *  - GAZED: the Watcher's mark. Darkness, slowness, and paranoia particles.
 *  - OVERTAKEN: the Monstrosity's channel broadcasting through your retinas.
 */
public final class ModStatusEffects {

    public static final Holder<MobEffect> DECAY = Registry.registerForHolder(
        BuiltInRegistries.MOB_EFFECT, DevouringStorms.id("decay"), new DecayEffect());

    public static final Holder<MobEffect> GAZED = Registry.registerForHolder(
        BuiltInRegistries.MOB_EFFECT, DevouringStorms.id("gazed"), new GazedEffect());

    /** OVERTAKEN: the Monstrosity's colourful broadcast. The sky is not the sky right now. */
    public static final Holder<MobEffect> OVERTAKEN = Registry.registerForHolder(
        BuiltInRegistries.MOB_EFFECT, DevouringStorms.id("overtaken"), new OvertakenEffect());

    private ModStatusEffects() {
    }

    public static void register() {
        // Static initialisation only.
    }
}
