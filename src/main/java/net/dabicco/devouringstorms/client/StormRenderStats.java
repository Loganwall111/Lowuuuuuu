package net.dabicco.devouringstorms.client;

import net.dabicco.devouringstorms.DevouringStormsMod;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;

public final class StormRenderStats {
   private static long lastReport;
   private static int frames;

   private StormRenderStats() {
   }

   public static void report() {
      int submits = CubeReveal.statSubmits;
      int cubes = CubeReveal.statCubes;
      CubeReveal.statSubmits = 0;
      CubeReveal.statCubes = 0;
      ++frames;
      if (DevouringStormsClientConfig.stormRenderStats) {
         long now = System.currentTimeMillis();
         if (now - lastReport >= 1000L) {
            lastReport = now;
            if (submits != 0 || cubes != 0) {
               DevouringStormsMod.LOGGER.info("[dabyws] model: {} submits, {} cubes, ~{} vertices per frame | shadow: {} vertices | frames since last: {}", new Object[]{submits, cubes, cubes * 24, StormShadowMap.capturedVertices(), frames});
               frames = 0;
            }
         }
      }
   }
}
