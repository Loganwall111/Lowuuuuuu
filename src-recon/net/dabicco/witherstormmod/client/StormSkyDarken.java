package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class StormSkyDarken {
   private static final double RANGE = 420.0;
   private static final double CORE_FRAC = 0.6;
   private static final double DARKEN_START_PHASE = 5.0;
   private static final double DARKEN_FULL_PHASE = 5.8;
   private static final float MAX_DARKEN = 0.94F;
   private static float displayed;
   private static float palettePhase;

   public static float floorR() {
      return (float)DabyWSClientConfig.skyDarkenR;
   }

   public static float floorG() {
      return (float)DabyWSClientConfig.skyDarkenG;
   }

   public static float floorB() {
      return (float)DabyWSClientConfig.skyDarkenB;
   }

   public static float palettePhase() {
      return palettePhase;
   }

   public static float fogR() {
      if (DabyWSClientConfig.phaseFogPalettes) {
         float[] c = net.dabicco.witherstormmod.client.StormPalettes.fogColor(palettePhase, new float[3]);
         return Mth.lerp(
            net.dabicco.witherstormmod.client.StormPalettes.strength(),
            DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorR : floorR(),
            c[0]
         );
      } else {
         return DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorR : floorR();
      }
   }

   public static float fogG() {
      if (DabyWSClientConfig.phaseFogPalettes) {
         float[] c = net.dabicco.witherstormmod.client.StormPalettes.fogColor(palettePhase, new float[3]);
         return Mth.lerp(
            net.dabicco.witherstormmod.client.StormPalettes.strength(),
            DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorG : floorG(),
            c[1]
         );
      } else {
         return DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorG : floorG();
      }
   }

   public static float fogB() {
      if (DabyWSClientConfig.phaseFogPalettes) {
         float[] c = net.dabicco.witherstormmod.client.StormPalettes.fogColor(palettePhase, new float[3]);
         return Mth.lerp(
            net.dabicco.witherstormmod.client.StormPalettes.strength(),
            DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorB : floorB(),
            c[2]
         );
      } else {
         return DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorB : floorB();
      }
   }

   private StormSkyDarken() {
   }

   public static float factor() {
      return Math.min(1.0F, displayed + net.dabicco.witherstormmod.client.SpawnTowerGloom.darken());
   }

   public static void update(Vec3 cameraPos, float partialTick) {
      float target = 0.0F;

      for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
         if (!(d.phase < 5.0)) {
            double dx = d.dispX - cameraPos.x;
            double dy = d.dispY - cameraPos.y;
            double dz = d.dispZ - cameraPos.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (!(dist > 420.0)) {
               float phaseRamp = (float)Mth.clamp((d.phase - 5.0) / 0.7999999999999998, 0.0, 1.0);
               double frac = dist / 420.0;
               float proximity;
               if (frac <= 0.6) {
                  proximity = 1.0F;
               } else {
                  float edge = (float)(1.0 - (frac - 0.6) / 0.4);
                  proximity = edge * edge * (3.0F - 2.0F * edge);
               }

               float intensity = (float)DabyWSClientConfig.skyDarkenIntensity;
               target = Math.max(target, proximity * phaseRamp * 0.94F * intensity);
            }
         }
      }

      displayed = displayed + (target - displayed) * 0.06F;
      if (displayed < 0.002F) {
         displayed = 0.0F;
      }

      float phaseTarget = 0.0F;

      for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData dx : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
         double dxx = dx.dispX - cameraPos.x;
         double dy = dx.dispY - cameraPos.y;
         double dz = dx.dispZ - cameraPos.z;
         double dist = Math.sqrt(dxx * dxx + dy * dy + dz * dz);
         if (dist <= 620.0 && dx.phase >= 0.5F) {
            float proximity = 1.0F;
            if (dist > 240.0) {
               proximity = (float)(1.0 - (dist - 240.0) / 380.0);
            }

            if (proximity > 0.05F && dx.phase * proximity > phaseTarget) {
               phaseTarget = dx.phase * proximity;
            }
         }
      }

      palettePhase = palettePhase + (phaseTarget - palettePhase) * 0.045F;
      if (palettePhase < 0.01F) {
         palettePhase = 0.0F;
      }
   }

   public static void clear() {
      displayed = 0.0F;
      palettePhase = 0.0F;
   }
}
