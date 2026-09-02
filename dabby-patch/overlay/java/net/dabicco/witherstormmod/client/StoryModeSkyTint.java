package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.util.Mth;

/**
 * StoryModeSkyTint — the lavender Story Mode sky, fog and light colour, shifted
 * by time of day.
 *
 * Before the Wither Storm shows up, MCSM's overworld is not vanilla blue: it is
 * a soft lavender by day, a deep indigo-to-blue at night, and a violet-over-
 * ember at dusk. This drives three things from one palette so they always
 * agree with each other:
 *
 *   - the fog colour (through FogRendererMixin)
 *   - the sky colour
 *   - the ambient light tint used for coloured lighting / shadow tinting
 *
 * The storm's own atmosphere is layered on top of this by StormSkyDarken and
 * StormPalettes; this is only the baseline world look.
 *
 * Colours are sampled from the user's Story Mode reference screenshots:
 *   day     zenith #867FF1 -> horizon #CCAAFB
 *   night   zenith #10114A -> horizon #4A67EC
 *   dusk    violet #34224E -> ember #F19267
 *   dawn    a paler rose version of dusk
 */
public final class StoryModeSkyTint {

   private StoryModeSkyTint() {
   }

   /* --- sky / fog anchors (RGB 0..1) --- */
   private static final float[] SKY_DAY = {0.596F, 0.549F, 0.965F};   // #987FF6-ish mid lavender
   private static final float[] SKY_NIGHT = {0.098F, 0.114F, 0.400F}; // deep indigo
   private static final float[] SKY_DUSK = {0.690F, 0.400F, 0.400F};  // violet->ember blend
   private static final float[] SKY_DAWN = {0.760F, 0.560F, 0.660F};  // pale rose

   /* --- ambient light tints: what the world's lighting is coloured by --- */
   private static final float[] LIGHT_DAY = {1.000F, 0.980F, 1.000F};
   private static final float[] LIGHT_NIGHT = {0.560F, 0.640F, 1.000F};
   private static final float[] LIGHT_DUSK = {1.000F, 0.780F, 0.640F};
   private static final float[] LIGHT_DAWN = {1.000F, 0.860F, 0.900F};

   private static void mix(float[] out, float[] a, float[] b, float t) {
      out[0] = a[0] + (b[0] - a[0]) * t;
      out[1] = a[1] + (b[1] - a[1]) * t;
      out[2] = a[2] + (b[2] - a[2]) * t;
   }

   private static float ease(float t) {
      t = Mth.clamp(t, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   /** Blend the four anchors by clock time into {@code out}. */
   private static void byTime(long clockTime, float[] day, float[] dusk, float[] night, float[] dawn, float[] out) {
      float t = (float)(clockTime % 24000L);
      if (t < 1500.0F) {
         mix(out, dawn, day, ease(t / 1500.0F));
      } else if (t < 10500.0F) {
         out[0] = day[0];
         out[1] = day[1];
         out[2] = day[2];
      } else if (t < 12500.0F) {
         mix(out, day, dusk, ease((t - 10500.0F) / 2000.0F));
      } else if (t < 14000.0F) {
         mix(out, dusk, night, ease((t - 12500.0F) / 1500.0F));
      } else if (t < 22000.0F) {
         out[0] = night[0];
         out[1] = night[1];
         out[2] = night[2];
      } else {
         mix(out, night, dawn, ease((t - 22000.0F) / 2000.0F));
      }
   }

   /** Story Mode sky/fog colour for this clock time. */
   public static void skyColor(long clockTime, float[] out) {
      byTime(clockTime, SKY_DAY, SKY_DUSK, SKY_NIGHT, SKY_DAWN, out);
   }

   /** Ambient light tint for this clock time (for coloured lighting/shadows). */
   public static void lightColor(long clockTime, float[] out) {
      byTime(clockTime, LIGHT_DAY, LIGHT_DUSK, LIGHT_NIGHT, LIGHT_DAWN, out);
   }


   /* --- horizon band: the sunrise/sunset ring around the dome --- */
   private static final float[] HORIZON_DAY = {0.760F, 0.720F, 0.980F};
   private static final float[] HORIZON_DUSK = {0.945F, 0.573F, 0.404F};  // #F19267
   private static final float[] HORIZON_NIGHT = {0.290F, 0.400F, 0.925F}; // #4A67EC
   private static final float[] HORIZON_DAWN = {0.960F, 0.660F, 0.760F};

   /** Colour of the sunrise/sunset band for this clock time. */
   public static void horizonColor(long clockTime, float[] out) {
      byTime(clockTime, HORIZON_DAY, HORIZON_DUSK, HORIZON_NIGHT, HORIZON_DAWN, out);
   }

   /** Torch/lamp light tint. Constant warm, so fire stays fire. */
   public static void blockLightColor(float[] out) {
      out[0] = 1.000F;
      out[1] = 0.826F;
      out[2] = 0.560F;
   }


   /** How far distance FOG is tinted.
    *
    * Deliberately far weaker than {@link #strength()}. Fog colour is the colour
    * distant terrain fades into, so pushing it hard erases the world into a
    * flat wash instead of tinting it. The sky dome takes the strong value; fog
    * only takes a hint. */
   public static float fogStrength() {
      return DabyWSClientConfig.storyModeSky
             ? Mth.clamp((float)DabyWSClientConfig.storyModeFogStrength, 0.0F, 1.0F)
             : 0.0F;
   }

   /** How strongly the Story Mode look overrides vanilla, 0 when disabled. */
   public static float strength() {
      return DabyWSClientConfig.storyModeSky
             ? Mth.clamp((float)DabyWSClientConfig.storyModeSkyStrength, 0.0F, 1.0F)
             : 0.0F;
   }

   /** How strongly the ambient light is tinted, 0 when disabled. */
   public static float lightStrength() {
      return DabyWSClientConfig.storyModeLighting
             ? Mth.clamp((float)DabyWSClientConfig.storyModeLightingStrength, 0.0F, 1.0F)
             : 0.0F;
   }
}
