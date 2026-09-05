package net.dabicco.witherstormmod;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

public final class ModPotions {
   private static final int MINUTE = 1200;
   public static final Holder<Potion> HYPER_INVISIBILITY = register("hyper_invisibility", 5);
   public static final Holder<Potion> LONG_HYPER_INVISIBILITY = register("long_hyper_invisibility", 10);
   public static final Holder<Potion> EXTENDED_HYPER_INVISIBILITY = register("extended_hyper_invisibility", 15);

   private ModPotions() {
   }

   private static Holder<Potion> register(String id, int minutes) {
      return Registry.registerForHolder(
         BuiltInRegistries.POTION,
         Identifier.fromNamespaceAndPath("dabywitherstormmod", id),
         new Potion("hyper_invisibility", new MobEffectInstance[]{new MobEffectInstance(ModEffects.HYPER_INVISIBILITY, minutes * 1200)})
      );
   }

   public static boolean isHyperInvisibility(Holder<Potion> potion) {
      return potion != null
         && (
            potion.value() == HYPER_INVISIBILITY.value()
               || potion.value() == LONG_HYPER_INVISIBILITY.value()
               || potion.value() == EXTENDED_HYPER_INVISIBILITY.value()
         );
   }

   public static void initialize() {
   }
}
