package com.rewritten.devouringstorms.registry;

import com.rewritten.devouringstorms.DevouringStorms;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.SimpleParticleType;

/** Custom particles. The glitch particle is used by Anna, the Watcher and the Terminal. */
public final class ModParticles {

    public static final SimpleParticleType GLITCH = Registry.register(
        BuiltInRegistries.PARTICLE_TYPE,
        DevouringStorms.id("glitch"),
        new SimpleParticleType(true)
    );

    private ModParticles() {
    }

    public static void register() {
        // Static initialisation only.
    }
}
