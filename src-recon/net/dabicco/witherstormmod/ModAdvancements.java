package net.dabicco.witherstormmod;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public final class ModAdvancements {
   private static final double WITNESS_RANGE = 320.0;

   private ModAdvancements() {
   }

   public static void grant(ServerPlayer player, String name) {
      AdvancementHolder holder = player.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("dabywitherstormmod", name));
      if (holder != null) {
         AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
         if (!progress.isDone()) {
            player.getAdvancements().award(holder, "code");
         }
      }
   }

   public static void grantNearby(ServerLevel level, Entity source, String name) {
      grantNearby(level, source, name, 320.0);
   }

   public static void grantNearby(ServerLevel level, Entity source, String name, double range) {
      for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, AABB.ofSize(source.position(), range * 2.0, range * 2.0, range * 2.0))) {
         grant(p, name);
      }
   }
}
