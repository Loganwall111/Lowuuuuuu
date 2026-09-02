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

/**
 * Repaints the actual sky dome with the Story Mode palette.
 *
 * The previous pass only tinted FOG, which is why the world went lavender but
 * the sky itself stayed vanilla blue — fog colour and sky colour are two
 * different things in Minecraft, and the dome is driven by
 * {@code SkyRenderState.skyColor}.
 *
 * This runs at HEAD, before {@link SkyRendererMixin} applies the storm's own
 * darkening at TAIL, so the storm still wins when it is close: baseline first,
 * storm on top.
 */
@Mixin(value = {SkyRenderer.class}, priority = 900)
public class StoryModeSkyDomeMixin {

   @Inject(
      method = {"extractRenderState(Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/SkyRenderState;)V"},
      at = {@At("TAIL")}
   )
   private void dabyws$storyModeSkyDome(ClientLevel level, float partialTick, Camera camera,
                                        SkyRenderState state, CallbackInfo ci) {
      float s = StoryModeSkyTint.strength();
      if (s <= 0.0F || level == null) {
         return;
      }
      float[] sky = new float[3];
      StoryModeSkyTint.skyColor(level.getOverworldClockTime(), sky);
      state.skyColor = blend(state.skyColor, sky, s);

      // The sunrise/sunset band gets the warm dusk colour so the horizon
      // matches the dome instead of staying vanilla orange.
      float[] horizon = new float[3];
      StoryModeSkyTint.horizonColor(level.getOverworldClockTime(), horizon);
      state.sunriseAndSunsetColor = blend(state.sunriseAndSunsetColor, horizon, s * 0.85F);

      /* The storm's own sky, painted over the Story Mode baseline.
       *
       * This is the dynamic skybox: not a quad behind the creature but the
       * dome itself, which is why it can cover the whole frame the way the
       * reference screenshots do. */
      StormSkyDome.update(camera.position());
      float storm = StormSkyDome.strength();
      if (storm > 0.0F) {
         float[] sc = new float[3];
         StormSkyDome.skyColor(sc);
         /* Ambient only. StormSkyGradient paints the big directional gradient
          * behind the storm; this just tints the rest of the dome so the two
          * meet without a seam. */
         state.skyColor = blend(state.skyColor, sc, storm * 0.45F);
         // the core drags the whole dome toward black as the storm matures
         float core = StormSkyDome.coreStrength() * storm * 0.35F;
         if (core > 0.0F) {
            state.skyColor = blend(state.skyColor, new float[] {0.0F, 0.0F, 0.0F}, core);
         }
         // kill the sunrise band underneath it, otherwise orange bleeds through
         state.sunriseAndSunsetColor = blend(state.sunriseAndSunsetColor, sc, storm * 0.6F);
      }
   }

   private static int blend(int argb, float[] rgb, float amount) {
      float keep = 1.0F - amount;
      int a = argb & 0xFF000000;
      int r = (int)((float)(argb >> 16 & 0xFF) * keep + rgb[0] * 255.0F * amount);
      int g = (int)((float)(argb >> 8 & 0xFF) * keep + rgb[1] * 255.0F * amount);
      int b = (int)((float)(argb & 0xFF) * keep + rgb[2] * 255.0F * amount);
      return a | Math.min(r, 255) << 16 | Math.min(g, 255) << 8 | Math.min(b, 255);
   }
}
