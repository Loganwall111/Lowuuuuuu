package net.dabicco.devouringstorms;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModParticles {
   public static final SimpleParticleType BEAM_MOTE = FabricParticleTypes.simple(true);

   public static void register() {
      Registry.register(BuiltInRegistries.PARTICLE_TYPE, DevouringStormsMod.id("beam_mote"), BEAM_MOTE);
   }
}
