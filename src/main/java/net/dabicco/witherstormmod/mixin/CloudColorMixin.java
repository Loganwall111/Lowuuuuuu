package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StormSkyDarken;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({CloudRenderer.class})
public abstract class CloudColorMixin {
   @ModifyVariable(
      method = {"render"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private int dabyws$stormCloudColor(int color) {
      float f = StormSkyDarken.factor() * (float)DabyWSClientConfig.cloudDarkenStrength;
      if (f <= 0.0F) {
         return color;
      } else {
         f = Mth.clamp(f, 0.0F, 1.0F);
         float r = Mth.lerp(f, (float)ARGB.red(color) / 255.0F, (float)DabyWSClientConfig.cloudColorR);
         float g = Mth.lerp(f, (float)ARGB.green(color) / 255.0F, (float)DabyWSClientConfig.cloudColorG);
         float b = Mth.lerp(f, (float)ARGB.blue(color) / 255.0F, (float)DabyWSClientConfig.cloudColorB);
         return ARGB.color(ARGB.alpha(color), Mth.floor(r * 255.0F), Mth.floor(g * 255.0F), Mth.floor(b * 255.0F));
      }
   }
}
