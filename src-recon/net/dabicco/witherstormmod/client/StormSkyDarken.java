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
      net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData owner =
         net.dabicco.witherstormmod.client.ClientDistantStormManager.nearestCustomWeather(cameraPos);
      if (owner != null) {
         Vec3 origin = owner.getStormOrigin();
         double dx = origin.x - cameraPos.x;
         double dy = origin.y - cameraPos.y;
         double dz = origin.z - cameraPos.z;
         double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (dist <= 420.0D) {
            float phaseRamp = (float) Mth.clamp((owner.phase - 5.0D) / 0.8D, 0.0D, 1.0D);
            double frac = dist / 420.0D;
            float proximity = frac <= 0.6D
               ? 1.0F
               : (float) (1.0D - (frac - 0.6D) / 0.4D);
            proximity = proximity * proximity * (3.0F - 2.0F * proximity);
            target = proximity * phaseRamp * 0.94F * (float) DabyWSClientConfig.skyDarkenIntensity;
         }
      }

      if (owner == null || target <= 0.0F && owner.getStormOrigin().distanceToSqr(cameraPos) > 420.0D * 420.0D) {
         displayed = 0.0F;
         palettePhase = 0.0F;
         return;
      }
      displayed += (target - displayed) * 0.06F;
      if (displayed < 0.002F) displayed = 0.0F;
      float phaseTarget = owner.phase;
      if (owner != null) {
         Vec3 origin = owner.getStormOrigin();
         double dx = origin.x - cameraPos.x;
         double dz = origin.z - cameraPos.z;
         double dist = Math.sqrt(dx * dx + dz * dz);
         if (dist > 620.0D) phaseTarget = 0.0F;
      }
      palettePhase += (phaseTarget - palettePhase) * 0.045F;
      if (palettePhase < 0.01F) palettePhase = 0.0F;
   }

}
