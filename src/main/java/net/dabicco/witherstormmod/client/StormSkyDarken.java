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
   /** smoothed phase of the strongest storm in range, drives the fog palette */
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

   /** Smoothed phase driving this frame's atmosphere palette (0 when no storm is in range). */
   public static float palettePhase() {
      return palettePhase;
   }

   /** True when a storm is actually present within range (pure peace = false). */
   public static boolean stormActive() {
      return palettePhase > 0.05F || displayed > 0.002F;
   }

   // ---- Peaceful (no-storm) Story Mode atmosphere anchors -----------------
   // These only ever apply when there is NO active Wither Storm, so the purple
   // storm atmosphere can never leak into peaceful exploration loops.
   private static final float[] DAY_TINT = {0.98F, 0.90F, 0.80F};      // warm yellowish-lavender daylight
   private static final float[] NOON_HORIZON = {0.96F, 0.82F, 0.72F};  // warm amber horizon
   private static final float[] NIGHT_TINT = {0.035F, 0.045F, 0.11F};  // deep cinematic bluish-black

   /**
    * Peaceful base sky tint for the restored Story Mode day/night loop. Called
    * only when {@link #stormActive()} is false, so the warm yellowish-lavender
    * daylight and deep bluish-black night are the *only* colours a peaceful
    * exploration loop ever sees. {@code night} is 0 at noon and 1 at deep night.
    */
   public static float[] peacefulSkyTint(float night, float[] out) {
      // day: warm yellow-lavender zenith; night: deep bluish-black; blend at dusk/dawn.
      out[0] = Mth.lerp(night, DAY_TINT[0], NIGHT_TINT[0]);
      out[1] = Mth.lerp(night, DAY_TINT[1], NIGHT_TINT[1]);
      out[2] = Mth.lerp(night, DAY_TINT[2], NIGHT_TINT[2]);
      return out;
   }

   /** Peaceful horizon tint (amber by day, low lavender at dusk, dark at night). */
   public static float[] peacefulHorizonTint(float night, float[] out) {
      out[0] = Mth.lerp(night, NOON_HORIZON[0], NIGHT_TINT[0]);
      out[1] = Mth.lerp(night, NOON_HORIZON[1], NIGHT_TINT[1]);
      out[2] = Mth.lerp(night, NOON_HORIZON[2], NIGHT_TINT[2]);
      return out;
   }

   public static float fogR() {
      if (DabyWSClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.fogColor(palettePhase, new float[3]);
         return Mth.lerp(StormPalettes.strength(), DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorR : floorR(), c[0]);
      } else {
         return DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorR : floorR();
      }
   }

   public static float fogG() {
      if (DabyWSClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.fogColor(palettePhase, new float[3]);
         return Mth.lerp(StormPalettes.strength(), DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorG : floorG(), c[1]);
      } else {
         return DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorG : floorG();
      }
   }

   public static float fogB() {
      if (DabyWSClientConfig.phaseFogPalettes) {
         float[] c = StormPalettes.fogColor(palettePhase, new float[3]);
         return Mth.lerp(StormPalettes.strength(), DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorB : floorB(), c[2]);
      } else {
         return DabyWSClientConfig.separateFogColor ? (float)DabyWSClientConfig.fogColorB : floorB();
      }
   }

   private StormSkyDarken() {
   }

   public static float factor() {
      return Math.min(1.0F, displayed + SpawnTowerGloom.darken());
   }

   /**
    * Lavender zenith tint for the restored Story Mode skybox loop. Blends
    * toward the palette's fog colour so the sky turns green at phase 4.5,
    * turquoise at phase 5, and purple-black from the cataclysm onward.
    */
   public static float[] skyTint(float[] out) {
      float[] f = fogColor3();
      out[0] = Mth.lerp(0.55F, 0.549F, f[0]);
      out[1] = Mth.lerp(0.55F, 0.529F, f[1]);
      out[2] = Mth.lerp(0.55F, 0.910F, f[2]);
      return out;
   }

   /** Warm orange horizon glow that answers the palette (magenta at cataclysm). */
   public static float[] sunsetTint(float[] out) {
      float[] f = fogColor3();
      out[0] = Mth.lerp(0.72F, 0.973F, f[0]);
      out[1] = Mth.lerp(0.72F, 0.714F, f[1]);
      out[2] = Mth.lerp(0.72F, 0.282F, f[2]);
      return out;
   }

   private static float[] fogColor3() {
      return StormPalettes.fogColor(palettePhase, new float[3]);
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

      // palette phase: strongest nearby storm with proximity weighting, so the
      // fog can swing turquoise at phase 5 and purple-black from phase 5.8 on.
      float phaseTarget = 0.0F;

      for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         double dx = d.dispX - cameraPos.x;
         double dy = d.dispY - cameraPos.y;
         double dz = d.dispZ - cameraPos.z;
         double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (dist <= 620.0F && d.phase >= 0.5F) {
            float proximity = 1.0F;
            if (dist > 240.0F) {
               proximity = (float)(1.0F - (dist - 240.0F) / 380.0F);
            }

            if (proximity > 0.05F && d.phase * proximity > phaseTarget) {
               phaseTarget = d.phase * proximity;
            }
         }
      }

      palettePhase += (phaseTarget - palettePhase) * 0.045F;
      if (palettePhase < 0.01F) {
         palettePhase = 0.0F;
      }

   }

   public static void clear() {
      displayed = 0.0F;
      palettePhase = 0.0F;
   }
}
