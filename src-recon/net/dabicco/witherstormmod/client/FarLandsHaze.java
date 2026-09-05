package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;

public final class FarLandsHaze {
   private FarLandsHaze() {
   }

   public static float fogScale(double camX, double camZ) {
      if (DabyWSClientConfig.farLandsHaze && !(DabyWSClientConfig.farLandsStrength <= 0.0)) {
         double distance = Math.sqrt(camX * camX + camZ * camZ);
         if (distance <= DabyWSClientConfig.farLandsDistance) {
            return 1.0F;
         } else {
            double band = Math.max(DabyWSClientConfig.farLandsDistance, 2000.0);
            double t = Math.min(1.0, (distance - DabyWSClientConfig.farLandsDistance) / band);
            float scale = (float)(1.0 - t * DabyWSClientConfig.farLandsStrength);
            return Math.max(scale, 0.05F);
         }
      } else {
         return 1.0F;
      }
   }
}
