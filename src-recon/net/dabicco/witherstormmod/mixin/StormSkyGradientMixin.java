package net.dabicco.witherstormmod.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.witherstormmod.client.StormSkyGradient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public abstract class StormSkyGradientMixin {
   @Shadow
   private SkyRenderer skyRenderer;

   @Inject(
      method = {"lambda$addSkyPass$0"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunriseAndSunset(Lcom/mojang/blaze3d/vertex/PoseStack;FI)V",
         shift = Shift.AFTER
      )},
      require = 1
   )
   private void dabyws$stormSkyGradient(GpuBufferSlice var1, SkyRenderState var2, CallbackInfo var3) {
      if (StormSkyGradient.fogStampActive()) {
         int var4 = StormSkyGradient.color();
         if (var4 >>> 24 >= 3) {
            float var5 = (float)Math.toRadians(StormSkyGradient.yaw());
            PoseStack var6 = new PoseStack();
            this.skyRenderer.renderSunriseAndSunset(var6, var5, var4);
         }
      }
   }
}
