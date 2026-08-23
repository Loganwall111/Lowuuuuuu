package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.StormRainShelter;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Level.class})
public class LevelRainShelterMixin {
   @Inject(
      method = {"getRainLevel"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void dabyws$shelteredFromRain(float partialTick, CallbackInfoReturnable<Float> cir) {
      float level = (Float)cir.getReturnValue();
      if (!(level <= 0.0F) && ((Level)(Object)this).isClientSide()) {
         float cover = StormRainShelter.cover();
         if (cover > 0.0F) {
            cir.setReturnValue(level * (1.0F - cover));
         }
      }
   }
}
