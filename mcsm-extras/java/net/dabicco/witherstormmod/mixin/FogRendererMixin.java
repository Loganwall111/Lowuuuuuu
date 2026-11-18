package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.client.BiomeStormFog;
import net.dabicco.witherstormmod.client.FarLandsHaze;
import net.dabicco.witherstormmod.client.SpawnTowerGloom;
import net.dabicco.witherstormmod.client.StormFog;
import net.dabicco.witherstormmod.client.StormSkyDarken;
import net.dabicco.witherstormmod.client.StormSkyDome;
import net.dabicco.witherstormmod.client.StoryModeSkyTint;
import net.dabicco.witherstormmod.client.BiomeStormFog.RegionAtmosphere;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.phys.Vec3;
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
      float origR = color.x;
      float origG = color.y;
      float origB = color.z;
      float stormSky = StormSkyDome.strength();
      if (stormSky > 0.0F) {
         float[] ssc = new float[3];
         StormSkyDome.skyColor(ssc);
         float fogAmt = stormSky * 0.75F;
         color.x = color.x * (1.0F - fogAmt) + ssc[0] * fogAmt;
         color.y = color.y * (1.0F - fogAmt) + ssc[1] * fogAmt;
         color.z = color.z * (1.0F - fogAmt) + ssc[2] * fogAmt;
      }

      float smStrength = StoryModeSkyTint.fogStrength();
      if (smStrength > 0.0F && level != null) {
         float[] sm = new float[3];
         StoryModeSkyTint.skyColor(level.getOverworldClockTime(), sm);
         color.x = color.x * (1.0F - smStrength) + sm[0] * smStrength;
         color.y = color.y * (1.0F - smStrength) + sm[1] * smStrength;
         color.z = color.z * (1.0F - smStrength) + sm[2] * smStrength;
      }

      StormSkyDarken.update(camera.position(), partialTick);
      float darken = StormSkyDarken.factor();
      if (!(darken <= 0.0F)) {
         color.x = color.x * (1.0F - darken) + StormSkyDarken.fogR() * darken;
         color.y = color.y * (1.0F - darken) + StormSkyDarken.fogG() * darken;
         color.z = color.z * (1.0F - darken) + StormSkyDarken.fogB() * darken;
      }

      float biomeBlend = BiomeStormFog.strength();
      if (biomeBlend > 0.0F) {
         RegionAtmosphere region = BiomeStormFog.regionAt(level, camera.position());
         color.x = color.x * (1.0F - biomeBlend) + region.r * biomeBlend;
         color.y = color.y * (1.0F - biomeBlend) + region.g * biomeBlend;
         color.z = color.z * (1.0F - biomeBlend) + region.b * biomeBlend;
      }
   }

   @Inject(
      method = {"setupFog"},
      at = {@At("RETURN")}
   )
   private void dabyws$towerFog(Camera camera, int renderDistance, DeltaTracker delta, float f, ClientLevel level, CallbackInfoReturnable<FogData> cir) {
      float scale = SpawnTowerGloom.fogScale();
      float storm = StormFog.fogScale();
      Vec3 camPos = camera.position();
      float farLands = FarLandsHaze.fogScale(camPos.x, camPos.z);
      float combined = Math.min(scale, Math.min(storm, farLands));
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
