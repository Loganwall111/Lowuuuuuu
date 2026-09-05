package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StoryModeClouds;
import net.minecraft.client.renderer.CloudRenderer;
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
      return StoryModeClouds.tint(color);
   }
}
