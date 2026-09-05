package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StormSkyDarken;
import net.minecraft.client.Camera;
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
      if (!(darken <= 0.0F)) {
         float keep = 1.0F - darken;
         state.skyColor = blendToFloor(state.skyColor, darken);
         state.sunriseAndSunsetColor = blendToFloor(state.sunriseAndSunsetColor, darken);
         state.starBrightness *= keep;
         state.rainBrightness *= keep;
      }
   }

   private static int blendToFloor(int argb, float darken) {
      float keep = 1.0F - darken;
      int a = argb & 0xFF000000;
      int r = (int)((argb >> 16 & 0xFF) * keep + StormSkyDarken.floorR() * 255.0F * darken);
      int g = (int)((argb >> 8 & 0xFF) * keep + StormSkyDarken.floorG() * 255.0F * darken);
      int b = (int)((argb & 0xFF) * keep + StormSkyDarken.floorB() * 255.0F * darken);
      return a | Math.min(r, 255) << 16 | Math.min(g, 255) << 8 | Math.min(b, 255);
   }
}
