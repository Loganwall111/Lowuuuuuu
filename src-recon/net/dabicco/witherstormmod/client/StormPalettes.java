package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.util.Mth;

public final class StormPalettes {
   private static final float[] FOG_PURPLE = new float[]{0.19F, 0.07F, 0.275F};
   private static final float[] FOG_TURQUOISE = new float[]{0.031F, 0.42F, 0.36F};
   private static final float[] FOG_CATACLYSM = new float[]{0.055F, 0.028F, 0.1F};
   private static final float[] PULSE_EARLY = new float[]{0.42F, 0.33F, 0.95F};
   private static final float[] PULSE_FIVE = new float[]{0.38F, 0.52F, 0.98F};
   private static final float[] PULSE_CATACLYSM = new float[]{0.48F, 0.2F, 0.72F};
   private static final float[] HALO_RING = new float[]{0.36F, 0.46F, 1.0F};
   private static final float[] HALO_UNDER = new float[]{0.92F, 0.94F, 1.0F};
   private static final float[] CLOUD_PURPLE = new float[]{0.115F, 0.095F, 0.135F};
   private static final float[] CLOUD_TURQUOISE = new float[]{0.05F, 0.22F, 0.2F};
   private static final float[] CLOUD_CATACLYSM = new float[]{0.045F, 0.03F, 0.08F};
   private static final float[] STAR_WHITE = new float[]{0.9F, 0.92F, 1.0F};
   private static final float[] STAR_PURPLE = new float[]{0.74F, 0.58F, 1.0F};
   private static final float[] STAR_TEAL = new float[]{0.45F, 0.95F, 0.85F};
   public static final float[] EJECTA_TEAL = new float[]{0.2F, 0.95F, 0.72F};
   public static final float[] EJECTA_GREEN = new float[]{0.35F, 0.88F, 0.3F};
   public static final float[] EJECTA_PALE = new float[]{0.8F, 0.68F, 1.0F};

   private StormPalettes() {
   }

   public static float strength() {
      return Mth.clamp((float)DabyWSClientConfig.paletteStrength, 0.0F, 1.0F);
   }

   public static void stageWeights(double phase, float[] w) {
      float turquoise = Mth.clamp((float)((phase - 4.6) / 0.8), 0.0F, 1.0F);
      float cataclysm = Mth.clamp((float)((phase - 5.45) / 0.65), 0.0F, 1.0F);
      turquoise *= 1.0F - cataclysm;
      float purple = Math.max(0.0F, 1.0F - turquoise - cataclysm);
      w[0] = purple;
      w[1] = turquoise;
      w[2] = cataclysm;
   }

   private static float[] tri(double phase, float[] a, float[] b, float[] c, float[] out) {
      float[] w = new float[3];
      stageWeights(phase, w);
      out[0] = a[0] * w[0] + b[0] * w[1] + c[0] * w[2];
      out[1] = a[1] * w[0] + b[1] * w[1] + c[1] * w[2];
      out[2] = a[2] * w[0] + b[2] * w[1] + c[2] * w[2];
      return out;
   }

   public static float[] fogColor(double phase, float[] out) {
      float[] purple = FOG_PURPLE;
      float[] teal = FOG_TURQUOISE;
      float[] cata = FOG_CATACLYSM;
      if (DabyWSClientConfig.separateFogColor) {
         purple = new float[]{(float)DabyWSClientConfig.fogColorR, (float)DabyWSClientConfig.fogColorG, (float)DabyWSClientConfig.fogColorB};
      }

      teal = new float[]{(float)DabyWSClientConfig.turquoiseFogR, (float)DabyWSClientConfig.turquoiseFogG, (float)DabyWSClientConfig.turquoiseFogB};
      cata = new float[]{(float)DabyWSClientConfig.cataclysmFogR, (float)DabyWSClientConfig.cataclysmFogG, (float)DabyWSClientConfig.cataclysmFogB};
      return tri(phase, purple, teal, cata, out);
   }

   public static float[] pulseColor(double phase, float[] out) {
      return tri(phase, PULSE_EARLY, PULSE_FIVE, PULSE_CATACLYSM, out);
   }

   public static float[] cloudColor(double phase, float[] out) {
      return tri(phase, CLOUD_PURPLE, CLOUD_TURQUOISE, CLOUD_CATACLYSM, out);
   }

   public static float[] haloRingColor(float[] out) {
      out[0] = HALO_RING[0];
      out[1] = HALO_RING[1];
      out[2] = HALO_RING[2];
      return out;
   }

   public static float[] haloUnderColor(float[] out) {
      out[0] = HALO_UNDER[0];
      out[1] = HALO_UNDER[1];
      out[2] = HALO_UNDER[2];
      return out;
   }

   public static float[] starColor(int kind, float tealAmount, float[] out) {
      float[] base = kind == 1 ? STAR_PURPLE : STAR_WHITE;
      out[0] = Mth.lerp(tealAmount, base[0], STAR_TEAL[0]);
      out[1] = Mth.lerp(tealAmount, base[1], STAR_TEAL[1]);
      out[2] = Mth.lerp(tealAmount, base[2], STAR_TEAL[2]);
      return out;
   }
}
