package net.dabicco.devouringstorms;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class ModEffects {
   public static final Holder<MobEffect> HYPER_INVISIBILITY;

   private ModEffects() {
   }

   public static boolean isHyperInvisible(LivingEntity entity) {
      return entity.hasEffect(HYPER_INVISIBILITY);
   }

   public static void initialize() {
   }

   static {
      HYPER_INVISIBILITY = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath("devouringstorms", "hyper_invisibility"), new HyperInvisibility());
   }

   private static final class HyperInvisibility extends MobEffect {
      HyperInvisibility() {
         super(MobEffectCategory.BENEFICIAL, 5922416);
      }
   }
}
