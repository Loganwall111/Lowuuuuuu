package net.dabicco.devouringstorms.client;

import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class StormSkyDarken {
   private static final double RANGE = (double)420.0F;
   private static final double CORE_FRAC = 0.6;
   private static final double DARKEN_START_PHASE = (double)5.0F;
   private static final double DARKEN_FULL_PHASE = 5.8;
   private static final float MAX_DARKEN = 0.94F;
   private static float displayed;
   /** smoothed phase of the dominant storm in range, drives the palette stage */
   private static float palettePhase;
   /** proximity/ownership of the current sky palette, 0 = no override, 1 = full storm takeover */
   private static float paletteBlend;

   public static float floorR() {
      return (float)DevouringStormsClientConfig.skyDarkenR;
   }

   public static float floorG() {
      return (float)DevouringStormsClientConfig.skyDarkenG;
   }

   public static float floorB() {
      return (float)DevouringStormsClientConfig.skyDarkenB;
   }

   /** Smoothed phase driving this frame's atmosphere palette (0 when no storm is in range). */
   public static float palettePhase() {
      return palettePhase;
   }

   /** How strongly the phase palette should override the user's manual colours right now. */
   public static float paletteBlend() {
      return paletteBlend;
   }

   public static float fogR() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.fogColor(palettePhase, new float[3]);
         return Mth.lerp(StormPalettes.strength() * paletteBlend, DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorR : floorR(), c[0]);
      } else {
         return DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorR : floorR();
      }
   }

   public static float fogG() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.fogColor(palettePhase, new float[3]);
         return Mth.lerp(StormPalettes.strength() * paletteBlend, DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorG : floorG(), c[1]);
      } else {
         return DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorG : floorG();
      }
   }

   public static float fogB() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.fogColor(palettePhase, new float[3]);
         return Mth.lerp(StormPalettes.strength() * paletteBlend, DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorB : floorB(), c[2]);
      } else {
         return DevouringStormsClientConfig.separateFogColor ? (float)DevouringStormsClientConfig.fogColorB : floorB();
      }
   }

   public static float skyR() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.skyColor(palettePhase, new float[3]);
         return Mth.lerp(StormPalettes.strength() * paletteBlend, floorR(), c[0]);
      }
      return floorR();
   }

   public static float skyG() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.skyColor(palettePhase, new float[3]);
         return Mth.lerp(StormPalettes.strength() * paletteBlend, floorG(), c[1]);
      }
      return floorG();
   }

   public static float skyB() {
      if (DevouringStormsClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.skyColor(palettePhase, new float[3]);
         return Mth.lerp(StormPalettes.strength() * paletteBlend, floorB(), c[2]);
      }
      return floorB();
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

               float intensity = (float)DevouringStormsClientConfig.skyDarkenIntensity;
               target = Math.max(target, proximity * phaseRamp * 0.94F * intensity);
            }
         }
      }

      displayed += (target - displayed) * 0.06F;
      if (displayed < 0.002F) {
         displayed = 0.0F;
      }

      // palette phase: pick the dominant nearby storm by proximity/ownership,
      // but preserve its actual phase so distant phase-5 storms stay turquoise
      // instead of being incorrectly downgraded back into early purple.
      float phaseTarget = 0.0F;
      float blendTarget = 0.0F;
      float bestScore = 0.0F;

      for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         double dx = d.dispX - cameraPos.x;
         double dy = d.dispY - cameraPos.y;
         double dz = d.dispZ - cameraPos.z;
         double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (dist <= 620.0F && d.phase >= 4.5F) {
            float proximity = 1.0F;
            if (dist > 240.0F) {
               proximity = (float)(1.0F - (dist - 240.0F) / 380.0F);
            }

            proximity = Mth.clamp(proximity, 0.0F, 1.0F);
            if (proximity > 0.05F) {
               float stage = Mth.clamp((d.phase - 4.5F) / 1.6F, 0.25F, 1.0F);
               float score = proximity * stage;
               if (score > bestScore) {
                  bestScore = score;
                  blendTarget = proximity;
                  phaseTarget = d.phase;
               }
            }
         }
      }

      palettePhase += (phaseTarget - palettePhase) * (phaseTarget > palettePhase ? 0.060F : 0.040F);
      paletteBlend += (blendTarget - paletteBlend) * 0.055F;
      if (palettePhase < 0.01F) {
         palettePhase = 0.0F;
      }
      if (paletteBlend < 0.01F) {
         paletteBlend = 0.0F;
      }

   }

   public static void clear() {
      displayed = 0.0F;
      palettePhase = 0.0F;
      paletteBlend = 0.0F;
   }
}
