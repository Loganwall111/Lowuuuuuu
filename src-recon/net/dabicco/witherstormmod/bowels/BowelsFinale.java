package net.dabicco.witherstormmod.bowels;

import java.util.UUID;
import net.dabicco.witherstormmod.BowelsEndRoom;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.dabicco.witherstormmod.entity.WitheredStarEntity;
import net.dabicco.witherstormmod.network.CaveRumblePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BowelsFinale {
   private static long began = Long.MIN_VALUE;
   private static final int RUNS_FOR = 240;
   private static final int STRAGGLER_GRACE = 80;
   private static final double PULL = 0.16;
   private static final double REACH = 26.0;
   private static UUID slayer;
   private static boolean settled;

   private BowelsFinale() {
   }

   public static void begin(ServerLevel level, UUID by) {
      slayer = by;
      settled = false;
      began = level.getGameTime();

      for (ServerPlayer player : level.players()) {
         ServerPlayNetworking.send(player, new CaveRumblePayload(240, 1.2F));
      }
   }

   public static boolean running(long gameTime) {
      return gameTime >= began && gameTime < began + 240L;
   }

   public static void pull(ServerLevel level, Entity entity) {
      if (running(level.getGameTime()) && BowelsEndRoom.holds(entity.getX(), entity.getY(), entity.getZ())) {
         double dx = 177.0 - entity.getX();
         double dz = 0.0 - entity.getZ();
         double flat = Math.sqrt(dx * dx + dz * dz);
         if (!(flat > 26.0)) {
            double urgency = 0.35 + 0.65 * Math.min(1.0, flat / 26.0);
            Vec3 push = flat < 0.001 ? Vec3.ZERO : new Vec3(dx / flat * 0.16 * urgency, 0.0, dz / flat * 0.16 * urgency);
            if (flat < BowelsEndRoom.holeClearance()) {
               push = push.add(0.0, 0.22, 0.0);
            }

            entity.setDeltaMovement(entity.getDeltaMovement().add(push));
            entity.hurtMarked = true;
            if (entity instanceof LivingEntity alive) {
               alive.fallDistance = 0.0;
            }
         }
      }
   }

   private static void killTheStorm(ServerLevel level) {
      MinecraftServer server = level.getServer();
      if (server != null) {
         ServerPlayer winner = slayer == null ? null : server.getPlayerList().getPlayer(slayer);

         for (ServerLevel other : server.getAllLevels()) {
            for (WitherStormEntity storm : other.getEntitiesOfClass(WitherStormEntity.class, new AABB(-3.0E7, -1000.0, -3.0E7, 3.0E7, 1000.0, 3.0E7))) {
               storm.setSuppressLoot(true);
               storm.kill(other);
            }
         }

         if (winner != null) {
            WitheredStarEntity.sendTo(winner);
         }
      }
   }

   public static void sweepStragglers(ServerLevel level) {
      if (began != Long.MIN_VALUE) {
         long since = level.getGameTime() - began;
         if (since >= 80L) {
            for (ServerPlayer player : level.players().toArray(new ServerPlayer[0])) {
               boolean inRoom = BowelsEndRoom.holds(player.getX(), player.getY(), player.getZ());
               if (!inRoom || since >= 240L) {
                  BowelsEndRoom.eject(player);
               }
            }

            if (!settled && level.players().isEmpty()) {
               settled = true;
               killTheStorm(level);
            }

            if (since >= 240L) {
               began = Long.MIN_VALUE;
            }
         }
      }
   }
}
