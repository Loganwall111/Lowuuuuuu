package net.dabicco.witherstormmod.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class ChunkForceRegistry {
   private static final Map<ResourceKey<Level>, Map<Long, Set<UUID>>> OWNERS = new HashMap<>();

   private ChunkForceRegistry() {
   }

   private static Map<Long, Set<UUID>> forLevel(ServerLevel level) {
      return OWNERS.computeIfAbsent(level.dimension(), k -> new HashMap<>());
   }

   public static void acquire(ServerLevel level, UUID owner, ChunkPos pos) {
      forLevel(level).computeIfAbsent(pos.pack(), k -> new HashSet<>()).add(owner);
      level.setChunkForced(pos.x(), pos.z(), true);
   }

   public static void release(ServerLevel level, UUID owner, ChunkPos pos) {
      Map<Long, Set<UUID>> map = forLevel(level);
      Set<UUID> holders = map.get(pos.pack());
      if (holders == null) {
         level.setChunkForced(pos.x(), pos.z(), false);
      } else {
         holders.remove(owner);
         if (holders.isEmpty()) {
            map.remove(pos.pack());
            level.setChunkForced(pos.x(), pos.z(), false);
         }
      }
   }

   public static void releaseAll(ServerLevel level, UUID owner, Iterable<ChunkPos> positions) {
      for (ChunkPos pos : positions) {
         release(level, owner, pos);
      }
   }
}
