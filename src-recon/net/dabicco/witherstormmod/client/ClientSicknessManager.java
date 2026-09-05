package net.dabicco.witherstormmod.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientSicknessManager {
   private static final long STALE_MILLIS = 8000L;
   private static final Map<Integer, net.dabicco.witherstormmod.client.ClientSicknessManager.Entry> INFECTED = new HashMap<>();

   private ClientSicknessManager() {
   }

   public static void set(int entityId, float progress, boolean withered) {
      net.dabicco.witherstormmod.client.ClientSicknessManager.Entry e = INFECTED.computeIfAbsent(
         entityId, k -> new net.dabicco.witherstormmod.client.ClientSicknessManager.Entry()
      );
      e.progress = progress;
      e.withered = withered;
      e.lastUpdate = System.currentTimeMillis();
   }

   private static net.dabicco.witherstormmod.client.ClientSicknessManager.Entry live(int entityId) {
      net.dabicco.witherstormmod.client.ClientSicknessManager.Entry e = INFECTED.get(entityId);
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
      net.dabicco.witherstormmod.client.ClientSicknessManager.Entry e = live(entityId);
      return e == null ? 0.0F : e.progress;
   }

   public static boolean isWithered(int entityId) {
      net.dabicco.witherstormmod.client.ClientSicknessManager.Entry e = live(entityId);
      return e != null && e.withered;
   }

   public static void clear() {
      INFECTED.clear();
   }

   public static void prune() {
      long now = System.currentTimeMillis();
      Iterator<net.dabicco.witherstormmod.client.ClientSicknessManager.Entry> it = INFECTED.values().iterator();

      while (it.hasNext()) {
         if (now - it.next().lastUpdate > 8000L) {
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
