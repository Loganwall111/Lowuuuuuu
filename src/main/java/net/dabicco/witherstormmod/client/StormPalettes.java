package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.util.Mth;

/**
 * StormPalettes — phase-driven colour science for the whole atmosphere.
 *
 * Every ambient effect (fog, the pulse, the halo pair, the cloud deck, the
 * starfield, the ejecta tint) reads its colour from here so the world around a
 * storm changes together as the storm climbs through its phases:
 *
 *   phase < 4.6   the classic dark-purple gloom.
 *   phase  5.0    the fog swings turquoise (MCSM ep.2-4 look once the giant
 *                 form is up) - handled as a blend window 4.6 -> 5.4.
 *   phase  5.8+   the "cataclysm" palette: purple-black gloom, blue-purple
 *                 halo ring plus the white under-halo, black rim glare with
 *                 turquoise/green clusters ejecting from the silhouette.
 *
 * The palette is only consulted when the client config allows it;
 * {@link #strength()} scales how far it overrides the user's manual colours.
 */
public final class StormPalettes {
   /** Fog anchors. */
   private static final float[] FOG_PURPLE = {0.19F, 0.07F, 0.275F};
   private static final float[] FOG_GREEN = {0.12F, 0.40F, 0.17F};
   private static final float[] FOG_TURQUOISE = {0.031F, 0.42F, 0.36F};
   private static final float[] FOG_CATACLYSM = {0.055F, 0.028F, 0.10F};

   /** Pulse shells. */
   private static final float[] PULSE_EARLY = {0.42F, 0.33F, 0.95F};
   private static final float[] PULSE_FIVE = {0.38F, 0.52F, 0.98F};
   private static final float[] PULSE_CATACLYSM = {0.48F, 0.20F, 0.72F};

   /** Center blob anchors (phase 5.1 -> 5.9 colour journey). */
   private static final float[] BLOB_DARK_PURPLE = {0.16F, 0.05F, 0.30F};
   private static final float[] BLOB_MAGENTA = {0.82F, 0.14F, 0.72F};
   private static final float[] BLOB_PINK = {0.95F, 0.45F, 0.78F};
   private static final float[] BLOB_BLACK_PURPLE = {0.02F, 0.01F, 0.03F}; // WitherStormShaderSource body colour
   private static final float[] BLOB_BLUE = {0.26F, 0.16F, 0.80F};

   /** Halo pair (phase 5.8+). */
   private static final float[] HALO_RING = {0.36F, 0.46F, 1.0F};
   private static final float[] HALO_UNDER = {0.92F, 0.94F, 1.0F};
   /** Light-blue halo that lives at the storm centre from phase 4 to the end. */
   private static final float[] HALO_CENTER = {0.44F, 0.72F, 1.0F};

   /** Cloud deck tint. */
   private static final float[] CLOUD_PURPLE = {0.115F, 0.095F, 0.135F};
   private static final float[] CLOUD_TURQUOISE = {0.05F, 0.22F, 0.20F};
   private static final float[] CLOUD_CATACLYSM = {0.045F, 0.030F, 0.080F};

   /** Starfield. */
   private static final float[] STAR_WHITE = {0.90F, 0.92F, 1.0F};
   private static final float[] STAR_PURPLE = {0.74F, 0.58F, 1.0F};
   private static final float[] STAR_TEAL = {0.45F, 0.95F, 0.85F};

   /** Ejecta sparks (constant). */
   public static final float[] EJECTA_TEAL = {0.20F, 0.95F, 0.72F};
   public static final float[] EJECTA_GREEN = {0.35F, 0.88F, 0.30F};
   public static final float[] EJECTA_PALE = {0.80F, 0.68F, 1.0F};

   private StormPalettes() {
   }

   /** How far the palette may override the user's configured colours. */
   public static float strength() {
      return Mth.clamp((float)DabyWSClientConfig.paletteStrength, 0.0F, 1.0F);
   }

   /** Blend weights for a phase: [0] purple, [1] green (4.5+), [2] turquoise, [3] cataclysm. */
   public static void stageWeights(double phase, float[] w) {
      float green = Mth.clamp((float)((phase - 4.5) / 0.5), 0.0F, 1.0F) * (1.0F - Mth.clamp((float)((phase - 5.0) / 0.35), 0.0F, 1.0F));
      float cataclysm = Mth.clamp((float)((phase - 5.45) / 0.65), 0.0F, 1.0F);
      float turquoise = Mth.clamp((float)((phase - 5.0) / 0.4), 0.0F, 1.0F) * (1.0F - cataclysm);
      float purple = Math.max(0.0F, 1.0F - green - turquoise - cataclysm);
      w[0] = purple;
      w[1] = green;
      w[2] = turquoise;
      w[3] = cataclysm;
   }

   private static float[] tri(double phase, float[] a, float[] b, float[] c, float[] out) {
      float[] w = new float[4];
      stageWeights(phase, w);
      // the middle anchor covers both the green and turquoise eras
      float mid = w[1] + w[2];
      out[0] = a[0] * w[0] + b[0] * mid + c[0] * w[3];
      out[1] = a[1] * w[0] + b[1] * mid + c[1] * w[3];
      out[2] = a[2] * w[0] + b[2] * mid + c[2] * w[3];
      return out;
   }

   private static float[] quad(double phase, float[] a, float[] b, float[] c, float[] d, float[] out) {
      float[] w = new float[4];
      stageWeights(phase, w);
      out[0] = a[0] * w[0] + b[0] * w[1] + c[0] * w[2] + d[0] * w[3];
      out[1] = a[1] * w[0] + b[1] * w[1] + c[1] * w[2] + d[1] * w[3];
      out[2] = a[2] * w[0] + b[2] * w[1] + c[2] * w[2] + d[2] * w[3];
      return out;
   }

   /** Fog colour for a phase, honouring the user's manual anchors when present. */
   public static float[] fogColor(double phase, float[] out) {
      float[] purple = FOG_PURPLE;
      float[] teal = FOG_TURQUOISE;
      float[] cata = FOG_CATACLYSM;
      if (DabyWSClientConfig.separateFogColor) {
         purple = new float[]{(float)DabyWSClientConfig.fogColorR, (float)DabyWSClientConfig.fogColorG, (float)DabyWSClientConfig.fogColorB};
      }
      teal = new float[]{(float)DabyWSClientConfig.turquoiseFogR, (float)DabyWSClientConfig.turquoiseFogG, (float)DabyWSClientConfig.turquoiseFogB};
      cata = new float[]{(float)DabyWSClientConfig.cataclysmFogR, (float)DabyWSClientConfig.cataclysmFogG, (float)DabyWSClientConfig.cataclysmFogB};
      return quad(phase, purple, FOG_GREEN, teal, cata, out);
   }

   /** Pulse shell colour for a phase. */
   public static float[] pulseColor(double phase, float[] out) {
      return tri(phase, PULSE_EARLY, PULSE_FIVE, PULSE_CATACLYSM, out);
   }

   /** Cloud deck tint for a phase. */
   public static float[] cloudColor(double phase, float[] out) {
      return tri(phase, CLOUD_PURPLE, CLOUD_TURQUOISE, CLOUD_CATACLYSM, out);
   }

   /** The blue-purple cataclysm halo ring colour. */
   public static float[] haloRingColor(float[] out) {
      out[0] = HALO_RING[0];
      out[1] = HALO_RING[1];
      out[2] = HALO_RING[2];
      return out;
   }

   /** The white under-halo colour. */
   public static float[] haloUnderColor(float[] out) {
      out[0] = HALO_UNDER[0];
      out[1] = HALO_UNDER[1];
      out[2] = HALO_UNDER[2];
      return out;
   }

   /** The light-blue halo that stays at the storm centre from phase 4 to the end. */
   public static float[] haloCenterColor(float[] out) {
      out[0] = HALO_CENTER[0];
      out[1] = HALO_CENTER[1];
      out[2] = HALO_CENTER[2];
      return out;
   }

   /** Phase-6 pulse flash colour: hot white-violet burst. */
   public static float[] flashColor(float[] out) {
      out[0] = 0.96F;
      out[1] = 0.82F;
      out[2] = 1.0F;
      return out;
   }

   /**
    * Colour of the giant center blob (phase 5.1 -> 5.9). It shifts through
    * dark purple -> magenta -> pink/blue/black-purple churn as the phase
    * climbs. {@code wobble} is a 0..1 slow oscillation so the blob surface
    * appears to roil instead of sitting static.
    */
   public static float[] blobColor(double phase, float wobble, float[] out) {
      float t = Mth.clamp((float)((phase - 5.1) / 0.8), 0.0F, 1.0F);
      float sw = (float)(0.5 + 0.5 * Math.sin(wobble * Math.PI * 2.0));
      float[] c = sw < 0.5F ? BLOB_PINK : BLOB_BLUE;
      float m1 = Mth.clamp(t * 2.0F, 0.0F, 1.0F);           // dark purple -> magenta by 5.5
      float m2 = Mth.clamp((t - 0.5F) * 2.0F, 0.0F, 1.0F);  // churn during 5.5..5.9
      for (int i = 0; i < 3; i++) {
         float x = Mth.lerp(m1, BLOB_DARK_PURPLE[i], BLOB_MAGENTA[i]);
         float y = Mth.lerp(sw, c[i], BLOB_BLACK_PURPLE[i]);
         out[i] = Mth.lerp(m2, x, y);
      }
      return out;
   }

   /**
    * Star colour. {@code kind} is a per-star hash: 0 = icy white (most),
    * 1 = violet. {@code tealAmount} shifts the sky teal once a nearby storm
    * feeds the phase-5 palette.
    */
   public static float[] starColor(int kind, float tealAmount, float[] out) {
      float[] base = kind == 1 ? STAR_PURPLE : STAR_WHITE;
      out[0] = Mth.lerp(tealAmount, base[0], STAR_TEAL[0]);
      out[1] = Mth.lerp(tealAmount, base[1], STAR_TEAL[1]);
      out[2] = Mth.lerp(tealAmount, base[2], STAR_TEAL[2]);
      return out;
   }
}
