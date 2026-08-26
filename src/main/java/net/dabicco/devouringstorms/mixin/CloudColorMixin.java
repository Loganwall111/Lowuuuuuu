package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.McsmSky;
import net.dabicco.devouringstorms.client.StormCloudDeck;
import net.dabicco.devouringstorms.client.StormPalettes;
import net.dabicco.devouringstorms.client.StormSkyDarken;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CloudRenderer.class})
public abstract class CloudColorMixin {
   private static long dabyws$levelTime = Long.MIN_VALUE;

   private static long dabyws$now() {
      net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
      return mc.level == null ? Long.MIN_VALUE : mc.level.getOverworldClockTime();
   }

   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$hideVanillaClouds(CallbackInfo ci) {
      if (StormCloudDeck.replacesVanillaClouds()) {
         ci.cancel();
      }
   }

   @ModifyVariable(
      method = {"render"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private int dabyws$stormCloudColor(int color) {
      float f = StormSkyDarken.factor() * (float)DevouringStormsClientConfig.cloudDarkenStrength;
      float paletteBlend = StormPalettes.strength() * StormSkyDarken.paletteBlend();
      if (f <= 0.0F && paletteBlend <= 0.0F) {
         // REGULAR game: the SAME clouds, tinted only by the time of day
         // (lavender-tinted days, warm dawn/dusk, indigo nights). The storm
         // phases take over below with their own palette.
         long dabyws$t = dabyws$now();
         if (DevouringStormsClientConfig.mainSkyMCSM && dabyws$t != Long.MIN_VALUE) {
            float[] tint = McsmSky.cloudTint(dabyws$t);
            float r = (float)ARGB.red(color) / 255.0F * tint[0];
            float g = (float)ARGB.green(color) / 255.0F * tint[1];
            float b = (float)ARGB.blue(color) / 255.0F * tint[2];
            return ARGB.color(ARGB.alpha(color), Mth.floor(Mth.clamp(r, 0.0F, 1.0F) * 255.0F), Mth.floor(Mth.clamp(g, 0.0F, 1.0F) * 255.0F), Mth.floor(Mth.clamp(b, 0.0F, 1.0F) * 255.0F));
         }
         return color;
      } else {
         f = Mth.clamp(f, 0.0F, 1.0F);
         float baseR = Mth.lerp(f, (float)ARGB.red(color) / 255.0F, StormSkyDarken.cloudBaseR());
         float baseG = Mth.lerp(f, (float)ARGB.green(color) / 255.0F, StormSkyDarken.cloudBaseG());
         float baseB = Mth.lerp(f, (float)ARGB.blue(color) / 255.0F, StormSkyDarken.cloudBaseB());
         float[] storm = StormPalettes.cloudColor(StormSkyDarken.palettePhase(), new float[3]);
         float mix = Mth.clamp(Math.max(f, paletteBlend), 0.0F, 1.0F);
         float r = Mth.lerp(mix, baseR, storm[0]);
         float g = Mth.lerp(mix, baseG, storm[1]);
         float b = Mth.lerp(mix, baseB, storm[2]);
         return ARGB.color(ARGB.alpha(color), Mth.floor(r * 255.0F), Mth.floor(g * 255.0F), Mth.floor(b * 255.0F));
      }
   }
}
