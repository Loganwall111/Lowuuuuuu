package net.dabicco.devouringstorms.mixin;

import net.dabicco.devouringstorms.client.ClientConfigCache;
import net.dabicco.devouringstorms.client.StormSkyDarken;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LightmapRenderStateExtractor.class})
public abstract class LightmapWorldDarkenMixin {
   @Shadow
   private boolean needsUpdate;
   @Unique
   private float dabyws$lastDarkening = -1.0F;

   @Inject(
      method = {"extract"},
      at = {@At("HEAD")}
   )
   private void dabyws$askForRebuild(LightmapRenderState state, float partialTick, CallbackInfo ci) {
      float amount = dabyws$amount();
      if (amount > 0.0F || this.dabyws$lastDarkening > 0.0F) {
         this.dabyws$lastDarkening = amount;
         this.needsUpdate = true;
      }
   }

   @Unique
   private static float dabyws$amount() {
      float server = (float)ClientConfigCache.cfg.worldDarkening / 100.0F;
      return StormSkyDarken.factor() * (float)DevouringStormsClientConfig.skyDarkenLighting * server;
   }

   @Inject(
      method = {"extract"},
      at = {@At("RETURN")}
   )
   private void dabyws$stormDarkensTheWorld(LightmapRenderState state, float partialTick, CallbackInfo ci) {
      float amount = dabyws$amount();
      if (!(amount <= 0.0F)) {
         state.bossOverlayWorldDarkening = Math.max(state.bossOverlayWorldDarkening, Math.min(1.0F, amount));
      }
   }
}
