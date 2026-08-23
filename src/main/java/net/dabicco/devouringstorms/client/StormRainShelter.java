package net.dabicco.devouringstorms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

public final class StormRainShelter {
   private static float factor;
   private static long lastMillis;
   private static long lastTestMillis;
   private static boolean lastTest;

   private StormRainShelter() {
   }

   private static double coverRadius(float phase) {
      if (phase >= 6.0F) {
         return (double)44.0F;
      } else if (phase >= 5.8F) {
         return (double)40.0F;
      } else if (phase >= 5.0F) {
         return (double)32.0F;
      } else {
         return phase >= 4.0F ? (double)26.0F : (double)0.0F;
      }
   }

   private static double coverBehind(float phase) {
      if (phase >= 5.8F) {
         return (double)26.0F;
      } else {
         return phase >= 5.0F ? (double)14.0F : (double)6.0F;
      }
   }

   public static float cover() {
      long now = Util.getMillis();
      long dtMillis = lastMillis == 0L ? 0L : now - lastMillis;
      if (dtMillis <= 0L) {
         return factor;
      } else {
         lastMillis = now;
         if (now - lastTestMillis >= 50L) {
            lastTestMillis = now;
            lastTest = insideFootprint(factor > 0.5F);
         }

         float target = lastTest ? 1.0F : 0.0F;
         float rate = target > factor ? 2.2F : 1.4F;
         float ease = 1.0F - (float)Math.exp((double)(-((float)dtMillis / 1000.0F) * rate));
         factor += (target - factor) * ease;
         if (factor < 0.001F) {
            factor = 0.0F;
         }

         if (factor > 0.999F) {
            factor = 1.0F;
         }

         return factor;
      }
   }

   private static boolean insideFootprint(boolean lenient) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         double slack = lenient ? 1.15 : (double)1.0F;
         double px = mc.player.getX();
         double py = mc.player.getEyeY();
         double pz = mc.player.getZ();

         for(ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
            if (!(d.phase < 4.0F) && !(py > d.dispY + (double)6.0F)) {
               double radius = coverRadius(d.phase) * slack;
               if (!(radius <= (double)0.0F)) {
                  double rad = Math.toRadians((double)d.dispYaw);
                  double fx = -Math.sin(rad);
                  double fz = Math.cos(rad);
                  double dx = px - d.dispX;
                  double dz = pz - d.dispZ;
                  double behind = -(dx * fx + dz * fz);
                  double lateral = dx * -fz + dz * fx;
                  double along = behind > (double)0.0F ? behind / (radius + coverBehind(d.phase)) : behind / radius;
                  double across = lateral / radius;
                  if (along * along + across * across <= (double)1.0F) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
