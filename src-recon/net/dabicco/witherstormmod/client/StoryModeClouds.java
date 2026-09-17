package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class StoryModeClouds {
   private static final float[] CLOUD_DAY = new float[]{0.965F, 0.96F, 1.0F};
   private static final float[] CLOUD_SUNSET = new float[]{0.98F, 0.76F, 0.62F};
   private static final float[] CLOUD_NIGHT = new float[]{0.3F, 0.36F, 0.68F};
   private static final float[] CLOUD_DAWN = new float[]{0.94F, 0.8F, 0.86F};

   private StoryModeClouds() {
   }

   private static void mix(float[] out, float[] a, float[] b, float t) {
      out[0] = a[0] + (b[0] - a[0]) * t;
      out[1] = a[1] + (b[1] - a[1]) * t;
      out[2] = a[2] + (b[2] - a[2]) * t;
   }

   private static float ease(float t) {
      t = Mth.clamp(t, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

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

   public static int tint(int color) {
      Minecraft mc = Minecraft.getInstance();
      int a = ARGB.alpha(color);
      float r = ARGB.red(color) / 255.0F;
      float g = ARGB.green(color) / 255.0F;
      float b = ARGB.blue(color) / 255.0F;
      if (DabyWSClientConfig.storyModeClouds && mc.level != null) {
         float[] c = new float[3];
         timeOfDayColor(mc.level.getOverworldClockTime(), c);
         float amt = Mth.clamp((float)DabyWSClientConfig.storyModeCloudStrength, 0.0F, 1.0F);
         r = Mth.lerp(amt, r, c[0]);
         g = Mth.lerp(amt, g, c[1]);
         b = Mth.lerp(amt, b, c[2]);
      }

      float f = net.dabicco.witherstormmod.client.StormSkyDarken.factor() * (float)DabyWSClientConfig.cloudDarkenStrength;
      if (f > 0.0F) {
         f = Mth.clamp(f, 0.0F, 1.0F);
         r = Mth.lerp(f, r, (float)DabyWSClientConfig.cloudColorR);
         g = Mth.lerp(f, g, (float)DabyWSClientConfig.cloudColorG);
         b = Mth.lerp(f, b, (float)DabyWSClientConfig.cloudColorB);
      }

      if (DabyWSClientConfig.storyModeCloudFade) {
         float fade = net.dabicco.witherstormmod.client.StormSkyDarken.factor() * (float)DabyWSClientConfig.storyModeCloudFadeAmount;
         fade = Mth.clamp(fade, 0.0F, 0.95F);
         a = (int)(a * (1.0F - fade));
      }

      return ARGB.color(a, Mth.floor(r * 255.0F), Mth.floor(g * 255.0F), Mth.floor(b * 255.0F));
   }
}
