package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.StormBloom;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class GameRendererBloomMixin {
   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void dabyws$beginBloomFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
      if (renderLevel) {
         StormBloom.beginFrame();
      }
   }
}
