package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StormSkyDome;
import net.dabicco.witherstormmod.client.StoryModeSkyTint;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {SkyRenderer.class},
   priority = 900
)
public class StoryModeSkyDomeMixin {
   @Inject(
      method = {"extractRenderState(Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/SkyRenderState;)V"},
      at = {@At("TAIL")}
   )
   private void dabyws$storyModeSkyDome(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
      float s = StoryModeSkyTint.strength();
      if (!(s <= 0.0F) && level != null) {
         float[] sky = new float[3];
         StoryModeSkyTint.skyColor(level.getOverworldClockTime(), sky);
         state.skyColor = blend(state.skyColor, sky, s);
         float[] horizon = new float[3];
         StoryModeSkyTint.horizonColor(level.getOverworldClockTime(), horizon);
         state.sunriseAndSunsetColor = blend(state.sunriseAndSunsetColor, horizon, s * 0.85F);
         StormSkyDome.update(camera.position());
         float storm = StormSkyDome.strength();
         if (storm > 0.0F) {
            float[] sc = new float[3];
            StormSkyDome.skyColor(sc);
            state.skyColor = blend(state.skyColor, sc, storm * 0.45F);
            float core = StormSkyDome.coreStrength() * storm * 0.35F;
            if (core > 0.0F) {
               state.skyColor = blend(state.skyColor, new float[]{0.0F, 0.0F, 0.0F}, core);
            }

            state.sunriseAndSunsetColor = blend(state.sunriseAndSunsetColor, sc, storm * 0.6F);
         }
      }
   }

   private static int blend(int argb, float[] rgb, float amount) {
      float keep = 1.0F - amount;
      int a = argb & 0xFF000000;
      int r = (int)((argb >> 16 & 0xFF) * keep + rgb[0] * 255.0F * amount);
      int g = (int)((argb >> 8 & 0xFF) * keep + rgb[1] * 255.0F * amount);
      int b = (int)((argb & 0xFF) * keep + rgb[2] * 255.0F * amount);
      return a | Math.min(r, 255) << 16 | Math.min(g, 255) << 8 | Math.min(b, 255);
   }
}
