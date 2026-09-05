package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;

public final class StormRenderStats {
   private static long lastReport;
   private static int frames;

   private StormRenderStats() {
   }

   public static void report() {
      int submits = net.dabicco.witherstormmod.client.CubeReveal.statSubmits;
      int cubes = net.dabicco.witherstormmod.client.CubeReveal.statCubes;
      net.dabicco.witherstormmod.client.CubeReveal.statSubmits = 0;
      net.dabicco.witherstormmod.client.CubeReveal.statCubes = 0;
      frames++;
      if (DabyWSClientConfig.stormRenderStats) {
         long now = System.currentTimeMillis();
         if (now - lastReport >= 1000L) {
            lastReport = now;
            if (submits != 0 || cubes != 0) {
               DabyWitherStormMod.LOGGER
                  .info(
                     "[dabyws] model: {} submits, {} cubes, ~{} vertices per frame | shadow: {} vertices | frames since last: {}",
                     new Object[]{submits, cubes, cubes * 24, net.dabicco.witherstormmod.client.StormShadowMap.capturedVertices(), frames}
                  );
               frames = 0;
            }
         }
      }
   }
}
