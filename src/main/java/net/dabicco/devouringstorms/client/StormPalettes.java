package net.dabicco.devouringstorms.client;

import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.minecraft.util.Mth;

/**
 * StormPalettes — phase-driven colour science for the whole atmosphere.
 *
 * The user's latest screenshot notes asked for a later, slower colour handoff:
 * the world should stay normal through phase 4, turn green at roughly 4.5,
 * swing turquoise at phase 5, then only start drifting pink/purple after 5.4
 * before finally collapsing into the deep cataclysm palette.
 *
 * We keep every atmosphere reader centralized here so fog, sky, clouds, pulses,
 * halos and star tint all answer the same phase curve.
 */
public final class StormPalettes {
   /** Fog anchors. */
   private static final float[] FOG_GREEN = {0.10F, 0.30F, 0.15F};
   private static final float[] FOG_TURQUOISE = {0.031F, 0.42F, 0.36F};
   private static final float[] FOG_PINK = {0.26F, 0.11F, 0.28F};
   private static final float[] FOG_CATACLYSM = {0.055F, 0.028F, 0.10F};

   /** Upper sky / dome anchors, tuned to the screenshot progression. */
   private static final float[] SKY_GREEN = {0.16F, 0.36F, 0.20F};
   private static final float[] SKY_TURQUOISE = {0.090F, 0.46F, 0.50F};
   private static final float[] SKY_PINK = {0.31F, 0.14F, 0.33F};
   private static final float[] SKY_CATACLYSM = {0.040F, 0.018F, 0.072F};

   /** Attached storm-back backdrop / glare bubble colours. */
   private static final float[] BACKDROP_VOID_WHITE = {0.90F, 0.90F, 0.95F};
   private static final float[] BACKDROP_VOID_TEAL = {0.08F, 0.12F, 0.18F};
   private static final float[] BACKDROP_VOID_PURPLE = {0.08F, 0.03F, 0.14F};
   private static final float[] BACKDROP_VOID_CATACLYSM = {0.02F, 0.01F, 0.04F};
   private static final float[] BACKDROP_RIM_WHITE = {0.98F, 0.98F, 1.0F};
   private static final float[] BACKDROP_RIM_TEAL = {0.18F, 0.36F, 0.42F};
   private static final float[] BACKDROP_RIM_PURPLE = {0.76F, 0.34F, 0.88F};
   private static final float[] BACKDROP_RIM_CATACLYSM = {0.94F, 0.38F, 0.82F};
   private static final float[] BACKDROP_WARM = {0.95F, 0.56F, 0.26F};

   /** Pulse shells. */
   private static final float[] PULSE_GREEN = {0.58F, 0.80F, 0.42F};
   private static final float[] PULSE_FIVE = {0.38F, 0.52F, 0.98F};
   private static final float[] PULSE_PINK = {0.76F, 0.34F, 0.86F};
   private static final float[] PULSE_CATACLYSM = {0.48F, 0.20F, 0.72F};

   /** Halo pair (late phase 5+). */
   private static final float[] HALO_RING = {0.36F, 0.46F, 1.0F};
   private static final float[] HALO_UNDER = {0.92F, 0.94F, 1.0F};

   /** Cloud deck tint: pale early slabs, then the phase-5 purple story-cloud look. */
   private static final float[] CLOUD_GREEN = {0.26F, 0.31F, 0.28F};
   private static final float[] CLOUD_TURQUOISE = {0.30F, 0.26F, 0.38F};
   private static final float[] CLOUD_PINK = {0.34F, 0.24F, 0.40F};
   private static final float[] CLOUD_CATACLYSM = {0.11F, 0.08F, 0.16F};

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
      return Mth.clamp((float)DevouringStormsClientConfig.paletteStrength, 0.0F, 1.0F);
   }

   /** Blend weights: [0] = green, [1] = turquoise, [2] = pink/purple, [3] = cataclysm. */
   public static void stageWeights(double phase, float[] w) {
      for (int i = 0; i < w.length; i++) {
         w[i] = 0.0F;
      }
      if (w.length < 4) {
         return;
      }
      if (phase <= 4.5) {
         w[0] = 1.0F;
         return;
      }
      if (phase < 5.0) {
         float t = smoothPhase(phase, 4.5F, 5.0F);
         w[0] = 1.0F - t;
         w[1] = t;
         return;
      }
      if (phase < 5.15) {
         float t = smoothPhase(phase, 5.0F, 5.15F);
         w[1] = 1.0F - t * 0.08F;
         w[2] = t * 0.08F;
         return;
      }
      if (phase < 5.45) {
         float t = smoothPhase(phase, 5.15F, 5.45F);
         w[1] = 0.92F * (1.0F - t);
         w[2] = 0.08F + 0.92F * t;
         return;
      }
      if (phase < 5.95) {
         w[2] = 1.0F;
         return;
      }
      if (phase < 6.15) {
         float t = smoothPhase(phase, 5.95F, 6.15F);
         w[2] = 1.0F - t;
         w[3] = t;
         return;
      }
      w[3] = 1.0F;
   }

   private static float smoothPhase(double phase, float start, float end) {
      float t = Mth.clamp((float)((phase - (double)start) / (double)(end - start)), 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   private static float[] curve(double phase, float[] a, float[] b, float[] c, float[] d, float[] out) {
      float[] w = new float[4];
      stageWeights(phase, w);
      out[0] = a[0] * w[0] + b[0] * w[1] + c[0] * w[2] + d[0] * w[3];
      out[1] = a[1] * w[0] + b[1] * w[1] + c[1] * w[2] + d[1] * w[3];
      out[2] = a[2] * w[0] + b[2] * w[1] + c[2] * w[2] + d[2] * w[3];
      return out;
   }

   /** Fog colour for a phase, honouring the user's manual anchors when present. */
   public static float[] fogColor(double phase, float[] out) {
      float[] green = FOG_GREEN;
      float[] teal = new float[]{(float)DevouringStormsClientConfig.turquoiseFogR, (float)DevouringStormsClientConfig.turquoiseFogG, (float)DevouringStormsClientConfig.turquoiseFogB};
      float[] pink = brighten(teal, FOG_PINK, 0.58F);
      float[] cata = new float[]{(float)DevouringStormsClientConfig.cataclysmFogR, (float)DevouringStormsClientConfig.cataclysmFogG, (float)DevouringStormsClientConfig.cataclysmFogB};
      if (DevouringStormsClientConfig.separateFogColor) {
         green = brighten(new float[]{(float)DevouringStormsClientConfig.fogColorR, (float)DevouringStormsClientConfig.fogColorG, (float)DevouringStormsClientConfig.fogColorB}, FOG_GREEN, 0.72F);
      }
      return curve(phase, green, teal, pink, cata, out);
   }

   /** Pulse shell colour for a phase. */
   public static float[] pulseColor(double phase, float[] out) {
      return curve(phase, PULSE_GREEN, PULSE_FIVE, PULSE_PINK, PULSE_CATACLYSM, out);
   }

   /** Cloud deck tint for a phase. */
   public static float[] cloudColor(double phase, float[] out) {
      return curve(phase, CLOUD_GREEN, CLOUD_TURQUOISE, CLOUD_PINK, CLOUD_CATACLYSM, out);
   }

   /** Upper-sky / dome tint for a phase, keeping all Batch 17 colour edits centralized here. */
   public static float[] skyColor(double phase, float[] out) {
      float[] teal = brighten(new float[]{(float)DevouringStormsClientConfig.turquoiseFogR, (float)DevouringStormsClientConfig.turquoiseFogG, (float)DevouringStormsClientConfig.turquoiseFogB}, SKY_TURQUOISE, 0.62F);
      float[] cata = brighten(new float[]{(float)DevouringStormsClientConfig.cataclysmFogR, (float)DevouringStormsClientConfig.cataclysmFogG, (float)DevouringStormsClientConfig.cataclysmFogB}, SKY_CATACLYSM, 0.35F);
      return curve(phase, SKY_GREEN, teal, SKY_PINK, cata, out);
   }

   private static float[] brighten(float[] base, float[] anchor, float amount) {
      return new float[]{Mth.lerp(amount, base[0], anchor[0]), Mth.lerp(amount, base[1], anchor[1]), Mth.lerp(amount, base[2], anchor[2])};
   }

   public static float phaseAmount(double phase, float start, float end) {
      return smoothPhase(phase, start, end);
   }

   public static float[] backdropVoidColor(double phase, float[] out) {
      return curve(phase, BACKDROP_VOID_WHITE, BACKDROP_VOID_TEAL, BACKDROP_VOID_PURPLE, BACKDROP_VOID_CATACLYSM, out);
   }

   public static float[] backdropRimColor(double phase, float[] out) {
      return curve(phase, BACKDROP_RIM_WHITE, BACKDROP_RIM_TEAL, BACKDROP_RIM_PURPLE, BACKDROP_RIM_CATACLYSM, out);
   }

   public static float[] backdropWarmColor(double phase, float[] out) {
      float warm = phaseAmount(phase, 5.85F, 6.25F);
      float pink = phaseAmount(phase, 5.05F, 5.55F);
      float[] rim = backdropRimColor(phase, new float[3]);
      out[0] = Mth.lerp(warm, rim[0], BACKDROP_WARM[0]);
      out[1] = Mth.lerp(warm, rim[1], BACKDROP_WARM[1]);
      out[2] = Mth.lerp(warm, rim[2], BACKDROP_WARM[2]);
      out[0] = Mth.lerp(pink * 0.25F, out[0], Math.max(out[0], 0.78F));
      out[1] = Mth.lerp(pink * 0.10F, out[1], out[1] * 0.92F);
      out[2] = Mth.lerp(pink * 0.18F, out[2], Math.max(out[2], 0.58F));
      return out;
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
