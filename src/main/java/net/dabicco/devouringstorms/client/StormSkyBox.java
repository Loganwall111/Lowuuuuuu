package net.dabicco.devouringstorms.client;

/**
 * BISECT PROBE (temporary stub): the native storm sky pass is disabled while
 * hunting a compile failure. Everything else in the Telltale architecture
 * (controller, fog sync, canopy stand-down, entity core glow) stays live.
 */
public final class StormSkyBox {
   private StormSkyBox() {
   }

   /** @return false = never claim the frame (vanilla sun/moon/stars stay). */
   public static boolean renderSkyLayers() {
      return false;
   }
}
