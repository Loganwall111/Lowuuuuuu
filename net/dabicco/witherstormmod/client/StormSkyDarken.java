package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class StormSkyDarken {
   private static final double RANGE = (double)420.0F;
   private static final double CORE_FRAC = 0.6;
   private static final double DARKEN_START_PHASE = (double)5.0F;
   private static final double DARKEN_FULL_PHASE = 5.8;
   private static final float MAX_DARKEN = 0.94F;
   private static float displayed;

   public static float floorR() {
      return (float)DabyWSClientConfig.skyDarkenR;
   }

   public static float floorG() {
      return (float)DabyWSClientConfig.skyDarkenG;
   }

   public static float floorB() {
      return (float)DabyWSClientConfig.skyDarkenB;
   }

   public static float fogR() {
      return DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorR : floorR();
   }

   public static float fogG() {
      return DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorG : floorG();
   }

   public static float fogB() {
      return DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorB : floorB();
   }

   private StormSkyDarken() {
   }

   public static float factor() {
      return Math.min(1.0F, displayed + SpawnTowerGloom.darken());
   }

   public static void update(Vec3 cameraPos, float partialTick) {
      float target = 0.0F;

      for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         if (!((double)d.phase < (double)5.0F)) {
            double dx = d.dispX - cameraPos.x;
            double dy = d.dispY - cameraPos.y;
            double dz = d.dispZ - cameraPos.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (!(dist > (double)420.0F)) {
               float phaseRamp = (float)Mth.clamp(((double)d.phase - (double)5.0F) / 0.7999999999999998, (double)0.0F, (double)1.0F);
               double frac = dist / (double)420.0F;
               float proximity;
               if (frac <= 0.6) {
                  proximity = 1.0F;
               } else {
                  float edge = (float)((double)1.0F - (frac - 0.6) / 0.4);
                  proximity = edge * edge * (3.0F - 2.0F * edge);
               }

               float intensity = (float)DabyWSClientConfig.skyDarkenIntensity;
               target = Math.max(target, proximity * phaseRamp * 0.94F * intensity);
            }
         }
      }

      displayed += (target - displayed) * 0.06F;
      if (displayed < 0.002F) {
         displayed = 0.0F;
      }

   }

   public static void clear() {
      displayed = 0.0F;
   }
}
