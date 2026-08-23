package net.dabicco.devouringstorms.bowels;

import java.util.UUID;
import net.dabicco.devouringstorms.BowelsEndRoom;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.dabicco.devouringstorms.entity.WitheredStarEntity;
import net.dabicco.devouringstorms.network.CaveRumblePayload;
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
   private static final double REACH = (double)26.0F;
   private static UUID slayer;
   private static boolean settled;

   private BowelsFinale() {
   }

   public static void begin(ServerLevel level, UUID by) {
      slayer = by;
      settled = false;
      began = level.getGameTime();

      for(ServerPlayer player : level.players()) {
         ServerPlayNetworking.send(player, new CaveRumblePayload(240, 1.2F));
      }

   }

   public static boolean running(long gameTime) {
      return gameTime >= began && gameTime < began + 240L;
   }

   public static void pull(ServerLevel level, Entity entity) {
      if (running(level.getGameTime())) {
         if (BowelsEndRoom.holds(entity.getX(), entity.getY(), entity.getZ())) {
            double dx = (double)177.0F - entity.getX();
            double dz = (double)0.0F - entity.getZ();
            double flat = Math.sqrt(dx * dx + dz * dz);
            if (!(flat > (double)26.0F)) {
               double urgency = 0.35 + 0.65 * Math.min((double)1.0F, flat / (double)26.0F);
               Vec3 push = flat < 0.001 ? Vec3.ZERO : new Vec3(dx / flat * 0.16 * urgency, (double)0.0F, dz / flat * 0.16 * urgency);
               if (flat < BowelsEndRoom.holeClearance()) {
                  push = push.add((double)0.0F, 0.22, (double)0.0F);
               }

               entity.setDeltaMovement(entity.getDeltaMovement().add(push));
               entity.hurtMarked = true;
               if (entity instanceof LivingEntity) {
                  LivingEntity alive = (LivingEntity)entity;
                  alive.fallDistance = (double)0.0F;
               }

            }
         }
      }
   }

   private static void killTheStorm(ServerLevel level) {
      MinecraftServer server = level.getServer();
      if (server != null) {
         ServerPlayer winner = slayer == null ? null : server.getPlayerList().getPlayer(slayer);

         for(ServerLevel other : server.getAllLevels()) {
            for(WitherStormEntity storm : other.getEntitiesOfClass(WitherStormEntity.class, new AABB((double)-3.0E7F, (double)-1000.0F, (double)-3.0E7F, (double)3.0E7F, (double)1000.0F, (double)3.0E7F))) {
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
            for(ServerPlayer player : (ServerPlayer[])level.players().toArray(new ServerPlayer[0])) {
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
