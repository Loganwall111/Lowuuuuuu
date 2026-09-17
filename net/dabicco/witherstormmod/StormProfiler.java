package net.dabicco.witherstormmod;

import java.util.HashMap;
import java.util.Map;

public final class StormProfiler {
   private static final long REPORT_THRESHOLD_NS = 2000000L;
   private static final long REPEAT_COOLDOWN_MS = 1000L;
   private static final Map<String, Long> LAST_REPORT = new HashMap();
   private static boolean enabled = Boolean.getBoolean("dabyws.profile");

   private StormProfiler() {
   }

   public static void setEnabled(boolean on) {
      enabled = on;
   }

   public static boolean enabled() {
      return enabled;
   }

   public static long start() {
      return enabled ? System.nanoTime() : 0L;
   }

   public static void end(String section, long startNs) {
      if (enabled && startNs != 0L) {
         long tookNs = System.nanoTime() - startNs;
         if (tookNs >= 2000000L) {
            long now = System.currentTimeMillis();
            Long last = (Long)LAST_REPORT.get(section);
            if (last == null || now - last >= 1000L) {
               LAST_REPORT.put(section, now);
               System.out.printf("[dabywitherstormmod][perf] %s took %.1f ms%n", section, (double)tookNs / (double)1000000.0F);
            }
         }
      }
   }
}
