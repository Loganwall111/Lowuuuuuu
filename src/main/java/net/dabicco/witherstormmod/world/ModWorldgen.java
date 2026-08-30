package net.dabicco.witherstormmod.world;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** Registers the overhaul's code-backed world-generation types. */
public final class ModWorldgen {
   public static final StructureType<AbandonedCityStructure> ABANDONED_CITY = () -> AbandonedCityStructure.CODEC;
   public static final StructureType<FloatingIslandStructure> FLOATING_ISLAND = () -> FloatingIslandStructure.CODEC;
   private static boolean registered;

   private ModWorldgen() { }

   public static void register() {
      if (registered) return;
      registered = true;
      Registry.register(BuiltInRegistries.STRUCTURE_TYPE, DabyWitherStormMod.id("abandoned_city"), ABANDONED_CITY);
      Registry.register(BuiltInRegistries.STRUCTURE_TYPE, DabyWitherStormMod.id("floating_island"), FLOATING_ISLAND);
   }
}
