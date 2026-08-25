package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.StormSkyBox;
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

   /**
    * LAYER 1 of the Telltale sky architecture: while a storm owns the sky,
    * the native sky pass draws the storm's layered backdrop (energy plate /
    * anomaly plate / churning cloud bands / mutation flash bloom) in place of
    * the vanilla sun/moon/star pass. Everything is camera-locked at infinite
    * depth in the sky frame pass, with additive blending and no depth state,
    * so terrain can never clip it and it can never box against mountains.
    */
   @Inject(
      method = {"renderSunMoonAndStars"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$stormSkyBackdrop(CallbackInfo ci) {
      // No target args captured on purpose: an args-free handler can never
      // mismatch the real renderSunMoonAndStars signature, so a renamed or
      // re-typed vanilla parameter can never crash the game at apply time.
      if (StormSkyBox.renderSkyLayers()) {
         ci.cancel();
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
