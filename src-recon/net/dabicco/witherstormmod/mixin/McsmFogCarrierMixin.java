package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StormSkyGradient;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({FogRenderer.class})
public abstract class McsmFogCarrierMixin {
   @Inject(
      method = {"updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V"},
      at = {@At("HEAD")}
   )
   private void dabyws$stampCarriers(FogData var1, CallbackInfo var2) {
      if (StormSkyGradient.fogStampActive()) {
         float var3 = StormSkyGradient.phase();
         if (!(var3 < 4.42F) && !(var3 > 8.06F)) {
            var1.skyEnd = 1000.0F + var3 * 100.0F;
            var1.cloudEnd = 1200.0F + (StormSkyGradient.yaw() + 180.0F) * 2.0F + (StormSkyGradient.pitch() + 90.0F) * 0.5F;
         }
      }
   }
}
