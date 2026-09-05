package net.dabicco.witherstormmod.client;

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
         return 44.0;
      } else if (phase >= 5.8F) {
         return 40.0;
      } else if (phase >= 5.0F) {
         return 32.0;
      } else {
         return phase >= 4.0F ? 26.0 : 0.0;
      }
   }

   private static double coverBehind(float phase) {
      if (phase >= 5.8F) {
         return 26.0;
      } else {
         return phase >= 5.0F ? 14.0 : 6.0;
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
         float ease = 1.0F - (float)Math.exp(-((float)dtMillis / 1000.0F) * rate);
         factor = factor + (target - factor) * ease;
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
         double slack = lenient ? 1.15 : 1.0;
         double px = mc.player.getX();
         double py = mc.player.getEyeY();
         double pz = mc.player.getZ();

         for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
            if (!(d.phase < 4.0F) && !(py > d.dispY + 6.0)) {
               double radius = coverRadius(d.phase) * slack;
               if (!(radius <= 0.0)) {
                  double rad = Math.toRadians(d.dispYaw);
                  double fx = -Math.sin(rad);
                  double fz = Math.cos(rad);
                  double dx = px - d.dispX;
                  double dz = pz - d.dispZ;
                  double behind = -(dx * fx + dz * fz);
                  double lateral = dx * -fz + dz * fx;
                  double along = behind > 0.0 ? behind / (radius + coverBehind(d.phase)) : behind / radius;
                  double across = lateral / radius;
                  if (along * along + across * across <= 1.0) {
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
