package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.ClientDistantStormManager;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.client.renderer.Projection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({Projection.class})
public class ProjectionMixin {
   @ModifyVariable(
      method = {"setupPerspective"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 2
   )
   private float dabyws$extendFarForDistantStorm(float zFar) {
      return zFar > 50.0F && !DevouringStormsClientConfig.legacyDistantRenderer && !ClientDistantStormManager.all().isEmpty() ? Math.max(zFar, 10000.0F) : zFar;
   }
}
