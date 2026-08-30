package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StormGlowRenderer;
import net.dabicco.witherstormmod.client.StormPalettes;
import net.dabicco.witherstormmod.client.StormSkyDarken;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.Mth;
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

      // Peaceful Story Mode timeline. Only when NO storm is present, so the warm
      // yellowish-lavender daylight and deep bluish-black night are the only
      // colours a peaceful exploration loop ever sees. The purple/magenta storm
      // atmosphere is hard-locked behind an active Wither Storm below.
      if (!StormSkyDarken.stormActive()) {
         float night = StormGlowRenderer.nightFactor(level);
         // Twilight window: near sunrise/sunset the horizon bridges amber -> lavender.
         float[] sky = StormSkyDarken.peacefulSkyTint(night, new float[3]);
         state.skyColor = blendTo(state.skyColor, 0.62F, sky);
         float[] horizon = StormSkyDarken.peacefulHorizonTint(night, new float[3]);
         state.sunriseAndSunsetColor = blendTo(state.sunriseAndSunsetColor, 0.45F, horizon);
         return;
      }

      // Restored Story Mode storm skybox loop: lavender zenith with a warm orange
      // horizon. The amount follows the nearest storm's smoothed phase, so the
      // sky reads lavender->orange normally, green at phase 4.5, turquoise at
      // phase 5+, and purple/magenta/black through the cataclysm — and the
      // world-time clock keeps running (this only re-tints vanilla's colours,
      // it never freezes them at tick 0).
      float phase = StormSkyDarken.palettePhase();
      if (phase > 0.05F) {
         float ramp = Mth.clamp(phase / 4.5F, 0.0F, 1.0F) * StormPalettes.strength();
         if (ramp > 0.004F) {
            float[] tint = StormSkyDarken.skyTint(new float[3]);
            state.skyColor = blendTo(state.skyColor, ramp, tint);
            float[] sunset = StormSkyDarken.sunsetTint(new float[3]);
            state.sunriseAndSunsetColor = blendTo(state.sunriseAndSunsetColor, ramp * 0.85F, sunset);
         }
      }
   }

   private static int blendToFloor(int argb, float darken) {
      float keep = 1.0F - darken;
      int a = argb & 0xFF000000;
      int r = (int)((float)(argb >> 16 & 0xFF) * keep + StormSkyDarken.floorR() * 255.0F * darken);
      int g = (int)((float)(argb >> 8 & 0xFF) * keep + StormSkyDarken.floorG() * 255.0F * darken);
      int b = (int)((float)(argb & 0xFF) * keep + StormSkyDarken.floorB() * 255.0F * darken);
      return a | Math.min(r, 255) << 16 | Math.min(g, 255) << 8 | Math.min(b, 255);
   }

   private static int blendTo(int argb, float amount, float[] rgb) {
      float keep = 1.0F - amount;
      int a = argb & 0xFF000000;
      int r = (int)((float)(argb >> 16 & 0xFF) * keep + rgb[0] * 255.0F * amount);
      int g = (int)((float)(argb >> 8 & 0xFF) * keep + rgb[1] * 255.0F * amount);
      int b = (int)((float)(argb & 0xFF) * keep + rgb[2] * 255.0F * amount);
      return a | Math.min(r, 255) << 16 | Math.min(g, 255) << 8 | Math.min(b, 255);
   }
}
