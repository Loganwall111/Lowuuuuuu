package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * StoryModeClouds — flat, solid, time-of-day tinted clouds, the MCSM way.
 *
 * Vanilla shades each cloud face differently (top bright, sides darker), which
 * is what makes clouds read as 3D lumps. Story Mode clouds are FLAT: every
 * face is the same colour, so they read as paper cut-outs on the sky, and that
 * colour shifts through the day — white-lavender at noon, peach at sunset,
 * deep blue at night.
 *
 * WHY THIS IS JAVA AND NOT A RESOURCE PACK
 * ----------------------------------------
 * A pack that overrides assets/minecraft/shaders/core/clouds.fsh is bypassed
 * the moment the player enables a shader pack (Iris/OptiFine take over the
 * cloud pipeline), and is lost whenever packs are reordered. Computing the
 * colour in Java and feeding it to CloudRenderer through a mixin means the
 * tint applies regardless, and composes with the storm darkening instead of
 * fighting it.
 *
 * The flat-shading half of the effect is the mixin forcing one colour for
 * every face — exactly what the reference .fsh did by using CloudColor
 * directly instead of multiplying by vertexColor.
 */
public final class StoryModeClouds {

   private StoryModeClouds() {
   }

   /* Story Mode cloud key colours, sampled from the reference frames. */
   private static final float[] CLOUD_DAY = {0.965F, 0.960F, 1.000F};
   private static final float[] CLOUD_SUNSET = {0.980F, 0.760F, 0.620F};
   private static final float[] CLOUD_NIGHT = {0.300F, 0.360F, 0.680F};
   private static final float[] CLOUD_DAWN = {0.940F, 0.800F, 0.860F};

   private static void mix(float[] out, float[] a, float[] b, float t) {
      out[0] = a[0] + (b[0] - a[0]) * t;
      out[1] = a[1] + (b[1] - a[1]) * t;
      out[2] = a[2] + (b[2] - a[2]) * t;
   }

   private static float ease(float t) {
      t = Mth.clamp(t, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   /**
    * The Story Mode cloud colour for the current time of day.
    *
    * Minecraft clock time runs 0..24000:
    *   0     sunrise
    *   6000  noon
    *   12000 sunset
    *   13000 night begins
    *   23000 dawn begins
    */
   public static void timeOfDayColor(long clockTime, float[] out) {
      float t = (float)(clockTime % 24000L);

      if (t < 1500.0F) {
         mix(out, CLOUD_DAWN, CLOUD_DAY, ease(t / 1500.0F));
      } else if (t < 10500.0F) {
         out[0] = CLOUD_DAY[0];
         out[1] = CLOUD_DAY[1];
         out[2] = CLOUD_DAY[2];
      } else if (t < 12500.0F) {
         mix(out, CLOUD_DAY, CLOUD_SUNSET, ease((t - 10500.0F) / 2000.0F));
      } else if (t < 14000.0F) {
         mix(out, CLOUD_SUNSET, CLOUD_NIGHT, ease((t - 12500.0F) / 1500.0F));
      } else if (t < 22000.0F) {
         out[0] = CLOUD_NIGHT[0];
         out[1] = CLOUD_NIGHT[1];
         out[2] = CLOUD_NIGHT[2];
      } else {
         mix(out, CLOUD_NIGHT, CLOUD_DAWN, ease((t - 22000.0F) / 2000.0F));
      }
   }

   /**
    * Transform the colour vanilla was about to use:
    *   1. replace vanilla's tint with the flat Story Mode time-of-day colour
    *   2. let the storm drag it toward the storm cloud colour as it nears
    *   3. fade the clouds out when a storm's black backdrop is behind them
    */
   public static int tint(int color) {
      Minecraft mc = Minecraft.getInstance();
      int a = ARGB.alpha(color);
      float r = (float)ARGB.red(color) / 255.0F;
      float g = (float)ARGB.green(color) / 255.0F;
      float b = (float)ARGB.blue(color) / 255.0F;

      if (DabyWSClientConfig.storyModeClouds && mc.level != null) {
         float[] c = new float[3];
         timeOfDayColor(mc.level.getOverworldClockTime(), c);
         float amt = Mth.clamp((float)DabyWSClientConfig.storyModeCloudStrength, 0.0F, 1.0F);
         r = Mth.lerp(amt, r, c[0]);
         g = Mth.lerp(amt, g, c[1]);
         b = Mth.lerp(amt, b, c[2]);
      }

      float f = StormSkyDarken.factor() * (float)DabyWSClientConfig.cloudDarkenStrength;
      if (f > 0.0F) {
         f = Mth.clamp(f, 0.0F, 1.0F);
         r = Mth.lerp(f, r, (float)DabyWSClientConfig.cloudColorR);
         g = Mth.lerp(f, g, (float)DabyWSClientConfig.cloudColorG);
         b = Mth.lerp(f, b, (float)DabyWSClientConfig.cloudColorB);
      }

      if (DabyWSClientConfig.storyModeCloudFade) {
         float fade = StormSkyDarken.factor() * (float)DabyWSClientConfig.storyModeCloudFadeAmount;
         fade = Mth.clamp(fade, 0.0F, 0.95F);
         a = (int)((float)a * (1.0F - fade));
      }

      return ARGB.color(a, Mth.floor(r * 255.0F), Mth.floor(g * 255.0F), Mth.floor(b * 255.0F));
   }
}
