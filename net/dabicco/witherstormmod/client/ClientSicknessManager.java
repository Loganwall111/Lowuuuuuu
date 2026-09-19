package net.dabicco.witherstormmod.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientSicknessManager {
   private static final long STALE_MILLIS = 8000L;
   private static final Map<Integer, Entry> INFECTED = new HashMap();

   private ClientSicknessManager() {
   }

   public static void set(int entityId, float progress, boolean withered) {
      Entry e = (Entry)INFECTED.computeIfAbsent(entityId, (k) -> new Entry());
      e.progress = progress;
      e.withered = withered;
      e.lastUpdate = System.currentTimeMillis();
   }

   private static Entry live(int entityId) {
      Entry e = (Entry)INFECTED.get(entityId);
      if (e == null) {
         return null;
      } else if (System.currentTimeMillis() - e.lastUpdate > 8000L) {
         INFECTED.remove(entityId);
         return null;
      } else {
         return e;
      }
   }

   public static float getInfection(int entityId) {
      Entry e = live(entityId);
      return e == null ? 0.0F : e.progress;
   }

   public static boolean isWithered(int entityId) {
      Entry e = live(entityId);
      return e != null && e.withered;
   }

   public static void clear() {
      INFECTED.clear();
   }

   public static void prune() {
      long now = System.currentTimeMillis();
      Iterator<Entry> it = INFECTED.values().iterator();

      while(it.hasNext()) {
         if (now - ((Entry)it.next()).lastUpdate > 8000L) {
            it.remove();
         }
      }

   }

   private static final class Entry {
      float progress;
      boolean withered;
      long lastUpdate;
   }
}
