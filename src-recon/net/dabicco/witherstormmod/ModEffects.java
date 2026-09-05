package net.dabicco.witherstormmod;

import net.dabicco.witherstormmod.ModEffects.HyperInvisibility;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public final class ModEffects {
   public static final Holder<MobEffect> HYPER_INVISIBILITY = Registry.registerForHolder(
      BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath("dabywitherstormmod", "hyper_invisibility"), new HyperInvisibility()
   );

   private ModEffects() {
   }

   public static boolean isHyperInvisible(LivingEntity entity) {
      return entity.hasEffect(HYPER_INVISIBILITY);
   }

   public static void initialize() {
   }
}
