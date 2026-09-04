package net.dabicco.witherstormmod.beacon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class WitheredBeacons {
   public static final int SPREAD = 16;
   private static final Map<ResourceKey<Level>, Set<BlockPos>> WITHERED = new HashMap();
   private static final Map<BlockPos, Long> COOLING = new HashMap();

   private WitheredBeacons() {
   }

   public static void add(Level level, BlockPos pos) {
      ((Set)WITHERED.computeIfAbsent(level.dimension(), (k) -> new HashSet())).add(pos.immutable());
   }

   public static void remove(Level level, BlockPos pos) {
      Set<BlockPos> set = (Set)WITHERED.get(level.dimension());
      if (set != null) {
         set.remove(pos);
      }

   }

   public static boolean anyNear(Level level, BlockPos pos) {
      Set<BlockPos> set = (Set)WITHERED.get(level.dimension());
      if (set != null && !set.isEmpty()) {
         for(BlockPos at : set) {
            if (!at.equals(pos) && at.distSqr(pos) <= (double)256.0F) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static void justPopped(Level level, BlockPos pos) {
      COOLING.put(pos.immutable(), level.getGameTime() + 40L);
   }

   public static boolean cooling(Level level, BlockPos pos) {
      Long until = (Long)COOLING.get(pos);
      if (until == null) {
         return false;
      } else if (level.getGameTime() >= until) {
         COOLING.remove(pos);
         return false;
      } else {
         return true;
      }
   }

   public static void award(ServerPlayer player, String path) {
      MinecraftServer server = player.level().getServer();
      if (server != null) {
         Identifier id = Identifier.fromNamespaceAndPath("dabywitherstormmod", path);
         AdvancementHolder holder = server.getAdvancements().get(id);
         if (holder != null) {
            player.getAdvancements().award(holder, "done");
         }
      }
   }
}
