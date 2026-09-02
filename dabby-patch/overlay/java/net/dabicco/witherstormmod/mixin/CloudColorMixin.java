package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.StoryModeClouds;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Recolours the clouds before vanilla uploads the tint.
 *
 * All the colour science lives in {@link StoryModeClouds#tint(int)}: the flat
 * Story Mode time-of-day palette, the storm proximity darkening this mixin
 * originally did on its own, and the transparency fade near the storm.
 *
 * Doing it here rather than in a resource-pack .fsh means it keeps working
 * when the player turns on a shader pack, which is the whole point.
 */
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
