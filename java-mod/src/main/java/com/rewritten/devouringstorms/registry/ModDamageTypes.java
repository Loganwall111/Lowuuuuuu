package com.rewritten.devouringstorms.registry;

import com.rewritten.devouringstorms.DevouringStorms;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** The Decay plague damage type (registered by data/devouring_storms/damage_type/decay.json). */
public final class ModDamageTypes {

    public static final ResourceKey<DamageType> DECAY =
        ResourceKey.create(Registries.DAMAGE_TYPE, DevouringStorms.id("decay"));

    private ModDamageTypes() {
    }

    public static DamageSource decay(Level level, @Nullable Entity attacker) {
        return new DamageSource(
            level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DECAY),
            attacker
        );
    }

    public static void register() {
        // Data-driven; no code registration required.
    }
}
