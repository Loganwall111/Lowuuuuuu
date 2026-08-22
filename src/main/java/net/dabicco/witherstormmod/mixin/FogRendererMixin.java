package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.SpawnTowerGloom;
import net.dabicco.witherstormmod.client.StormFog;
import net.dabicco.witherstormmod.client.StormSkyDarken;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FogRenderer.class})
public class FogRendererMixin {
   @Inject(
      method = {"computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IFLorg/joml/Vector4f;)V"},
      at = {@At("TAIL")}
   )
   private void dabyws$darkenSky(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenAmount, Vector4f color, CallbackInfo ci) {
      StormSkyDarken.update(camera.position(), partialTick);
      float darken = StormSkyDarken.factor();
      if (!(darken <= 0.0F)) {
         color.x = color.x * (1.0F - darken) + StormSkyDarken.fogR() * darken;
         color.y = color.y * (1.0F - darken) + StormSkyDarken.fogG() * darken;
         color.z = color.z * (1.0F - darken) + StormSkyDarken.fogB() * darken;
      }
   }

   @Inject(
      method = {"setupFog"},
      at = {@At("RETURN")}
   )
   private void dabyws$towerFog(Camera camera, int renderDistance, DeltaTracker delta, float f, ClientLevel level, CallbackInfoReturnable<FogData> cir) {
      float scale = SpawnTowerGloom.fogScale();
      float storm = StormFog.fogScale();
      float combined = Math.min(scale, storm);
      if (!(combined >= 0.999F)) {
         FogData data = (FogData)cir.getReturnValue();
         if (data != null) {
            data.environmentalStart *= combined;
            data.environmentalEnd *= combined;
            data.renderDistanceStart *= combined;
            data.renderDistanceEnd *= combined;
         }
      }
   }
}
