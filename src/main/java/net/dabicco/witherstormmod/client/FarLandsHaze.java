package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;

/**
 * Far-lands haze. As the player travels far from the world origin the fog slowly
 * closes in, giving that lonely Story-Mode far-lands feeling. Returns a fog
 * multiplier in (0, 1]; 1 = no effect, smaller = denser.
 */
public final class FarLandsHaze {
   private FarLandsHaze() {
   }

   public static float fogScale(double camX, double camZ) {
      if (!DabyWSClientConfig.farLandsHaze || DabyWSClientConfig.farLandsStrength <= 0.0) {
         return 1.0F;
      }

      double distance = Math.sqrt(camX * camX + camZ * camZ);
      if (distance <= DabyWSClientConfig.farLandsDistance) {
         return 1.0F;
      }

      // Past the threshold, ramp the haze from 0 up to full strength across a
      // generous band so it never pops in suddenly.
      double band = Math.max(DabyWSClientConfig.farLandsDistance, 2000.0);
      double t = Math.min(1.0, (distance - DabyWSClientConfig.farLandsDistance) / band);
      float scale = (float)(1.0 - t * DabyWSClientConfig.farLandsStrength);
      return Math.max(scale, 0.05F);
   }
}
