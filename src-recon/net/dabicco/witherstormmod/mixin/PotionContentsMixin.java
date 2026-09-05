package net.dabicco.witherstormmod.mixin;

import java.util.function.Consumer;
import net.dabicco.witherstormmod.ModEffects;
import net.dabicco.witherstormmod.ModPotions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({PotionContents.class})
public class PotionContentsMixin {
   @ModifyVariable(
      method = {"forEachEffect(Ljava/util/function/Consumer;F)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private float dabyws$fullDurationOnApply(float durationScale) {
      PotionContents self = (PotionContents)(Object)this;
      return self.potion().filter(ModPotions::isHyperInvisibility).isPresent() ? 1.0F : durationScale;
   }

   @ModifyVariable(
      method = {"addPotionTooltip(Ljava/lang/Iterable;Ljava/util/function/Consumer;FF)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private static float dabyws$fullDurationOnTooltip(
      float durationScale, Iterable<MobEffectInstance> effects, Consumer<Component> lines, float unusedScale, float tickRate
   ) {
      for (MobEffectInstance instance : effects) {
         if (instance.getEffect().equals(ModEffects.HYPER_INVISIBILITY)) {
            return 1.0F;
         }
      }

      return durationScale;
   }
}
