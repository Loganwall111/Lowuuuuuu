package net.dabicco.witherstormmod.bowels;

import net.dabicco.witherstormmod.network.ActionButtonPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public final class BowelsActionKeys {
   private static final double REACH = 24.0;

   private BowelsActionKeys() {
   }

   public static void pressed(ServerPlayer player, boolean rightHand) {
      ServerLevel level = player.level();
      AABB near = player.getBoundingBox().inflate(24.0);

      for (net.dabicco.witherstormmod.bowels.BowelsMawEntity maw : level.getEntitiesOfClass(net.dabicco.witherstormmod.bowels.BowelsMawEntity.class, near)) {
         if (maw.answer(player, rightHand)) {
            return;
         }
      }

      for (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity limb : level.getEntitiesOfClass(
         net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, near
      )) {
         if (limb.answerGrab(player, rightHand)) {
            return;
         }
      }
   }

   public static void listen() {
      PayloadTypeRegistry.serverboundPlay().register(ActionButtonPayload.TYPE, ActionButtonPayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(
         ActionButtonPayload.TYPE, (payload, context) -> context.server().execute(() -> pressed(context.player(), payload.rightHand()))
      );
   }
}
