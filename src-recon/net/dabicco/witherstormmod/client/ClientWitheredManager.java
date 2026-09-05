package net.dabicco.witherstormmod.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.dabicco.witherstormmod.network.WitheredCastPayload;

public final class ClientWitheredManager {
   private static final long CHAR_MILLIS = 11L;
   private static final long LINGER_MILLIS = 900L;
   private static final Map<Integer, net.dabicco.witherstormmod.client.ClientWitheredManager.Cast> CASTS = new HashMap<>();

   private ClientWitheredManager() {
   }

   public static void onCast(WitheredCastPayload payload) {
      if (payload.ability() != 0 && !payload.command().isEmpty()) {
         net.dabicco.witherstormmod.client.ClientWitheredManager.Cast cast = CASTS.computeIfAbsent(
            payload.casterId(), k -> new net.dabicco.witherstormmod.client.ClientWitheredManager.Cast()
         );
         if (cast.ability != payload.ability() || cast.abilityStartedAt == 0L) {
            cast.abilityStartedAt = System.currentTimeMillis();
         }

         cast.ability = payload.ability();
         cast.targetId = payload.targetId();
         if (!payload.command().equals(cast.command)) {
            cast.command = payload.command();
            cast.startedAt = System.currentTimeMillis();
         }

         cast.endsAt = System.currentTimeMillis() + payload.duration() * 50L + 900L;
      } else {
         CASTS.remove(payload.casterId());
      }
   }

   public static net.dabicco.witherstormmod.client.ClientWitheredManager.Cast get(int entityId) {
      net.dabicco.witherstormmod.client.ClientWitheredManager.Cast cast = CASTS.get(entityId);
      if (cast == null) {
         return null;
      } else if (System.currentTimeMillis() > cast.endsAt) {
         CASTS.remove(entityId);
         return null;
      } else {
         return cast;
      }
   }

   public static boolean isEmpty() {
      return CASTS.isEmpty();
   }

   public static void clear() {
      CASTS.clear();
   }

   public static void prune() {
      long now = System.currentTimeMillis();
      Iterator<net.dabicco.witherstormmod.client.ClientWitheredManager.Cast> it = CASTS.values().iterator();

      while (it.hasNext()) {
         if (now > it.next().endsAt) {
            it.remove();
         }
      }
   }

   public static final class Cast {
      public int ability;
      public String command = "";
      public long startedAt;
      public long endsAt;
      public int targetId = -1;
      public long abilityStartedAt;

      public float abilityTicks(long now) {
         return (float)(now - this.abilityStartedAt) / 50.0F;
      }

      public int typed(long now) {
         return (int)Math.min((long)this.command.length(), Math.max(0L, (now - this.startedAt) / 11L));
      }

      public boolean stillTyping(long now) {
         return this.typed(now) < this.command.length();
      }
   }
}
