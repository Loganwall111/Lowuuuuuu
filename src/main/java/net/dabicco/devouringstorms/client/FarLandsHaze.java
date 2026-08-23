package net.dabicco.devouringstorms.client;

import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;

/**
 * Far-lands haze. As the player travels far from the world origin the fog slowly
 * closes in, giving that lonely Story-Mode far-lands feeling. Returns a fog
 * multiplier in (0, 1]; 1 = no effect, smaller = denser.
 */
public final class FarLandsHaze {
   private FarLandsHaze() {
   }

   public static float fogScale(double camX, double camZ) {
      if (!DevouringStormsClientConfig.farLandsHaze || DevouringStormsClientConfig.farLandsStrength <= 0.0) {
         return 1.0F;
      }

      double distance = Math.sqrt(camX * camX + camZ * camZ);
      if (distance <= DevouringStormsClientConfig.farLandsDistance) {
         return 1.0F;
      }

      // Past the threshold, ramp the haze from 0 up to full strength across a
      // generous band so it never pops in suddenly.
      double band = Math.max(DevouringStormsClientConfig.farLandsDistance, 2000.0);
      double t = Math.min(1.0, (distance - DevouringStormsClientConfig.farLandsDistance) / band);
      float scale = (float)(1.0 - t * DevouringStormsClientConfig.farLandsStrength);
      return Math.max(scale, 0.05F);
   }
}
