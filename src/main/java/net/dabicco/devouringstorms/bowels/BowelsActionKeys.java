package net.dabicco.devouringstorms.bowels;

import net.dabicco.devouringstorms.network.ActionButtonPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public final class BowelsActionKeys {
   private static final double REACH = (double)24.0F;

   private BowelsActionKeys() {
   }

   public static void pressed(ServerPlayer player, boolean rightHand) {
      ServerLevel level = player.level();
      AABB near = player.getBoundingBox().inflate((double)24.0F);

      for(BowelsMawEntity maw : level.getEntitiesOfClass(BowelsMawEntity.class, near)) {
         if (maw.answer(player, rightHand)) {
            return;
         }
      }

      for(BowelsTentacleEntity limb : level.getEntitiesOfClass(BowelsTentacleEntity.class, near)) {
         if (limb.answerGrab(player, rightHand)) {
            return;
         }
      }

   }

   public static void listen() {
      PayloadTypeRegistry.serverboundPlay().register(ActionButtonPayload.TYPE, ActionButtonPayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(ActionButtonPayload.TYPE, (payload, context) -> context.server().execute(() -> pressed(context.player(), payload.rightHand())));
   }
}
