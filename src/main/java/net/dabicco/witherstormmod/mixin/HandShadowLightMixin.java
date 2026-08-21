package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StormShadow;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({ItemInHandRenderer.class})
public abstract class HandShadowLightMixin {
   private static final float DABYWS$MAX_SKY_LOSS = 0.72F;

   @ModifyVariable(
      method = {"renderItem"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private int dabyws$darkenInStormShadow(int packedLight) {
      float shadow = StormShadow.cameraShadowAmount();
      if (shadow <= 0.0F) {
         return packedLight;
      } else {
         int sky = packedLight >> 20 & 15;
         int block = packedLight >> 4 & 15;
         int dimmed = Mth.clamp(Mth.floor((float)sky * (1.0F - 0.72F * shadow)), 0, sky);
         return dimmed << 20 | block << 4;
      }
   }
}
