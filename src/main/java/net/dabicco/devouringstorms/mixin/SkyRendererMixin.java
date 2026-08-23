package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.StormSkyDarken;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SkyRenderer.class})
public class SkyRendererMixin {
   @Inject(
      method = {"extractRenderState(Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/SkyRenderState;)V"},
      at = {@At("TAIL")}
   )
   private void dabyws$darkenSkyDome(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
      float darken = StormSkyDarken.factor();
      float palette = StormSkyDarken.paletteBlend();
      float dome = Mth.clamp(darken * 0.72F + palette * 0.52F, 0.0F, 1.0F);
      if (!(dome <= 0.0F)) {
         float keep = 1.0F - Mth.clamp(darken * 0.88F, 0.0F, 1.0F);
         state.skyColor = blendToFloor(state.skyColor, dome);
         state.sunriseAndSunsetColor = blendToFloor(state.sunriseAndSunsetColor, Mth.clamp(dome * 0.82F, 0.0F, 1.0F));
         state.starBrightness *= keep;
         state.rainBrightness *= keep;
      }
   }

   private static int blendToFloor(int argb, float darken) {
      float keep = 1.0F - darken;
      int a = argb & 0xFF000000;
      int r = (int)((float)(argb >> 16 & 0xFF) * keep + StormSkyDarken.skyR() * 255.0F * darken);
      int g = (int)((float)(argb >> 8 & 0xFF) * keep + StormSkyDarken.skyG() * 255.0F * darken);
      int b = (int)((float)(argb & 0xFF) * keep + StormSkyDarken.skyB() * 255.0F * darken);
      return a | Math.min(r, 255) << 16 | Math.min(g, 255) << 8 | Math.min(b, 255);
   }
}
