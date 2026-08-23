package net.dabicco.devouringstorms;

import net.dabicco.devouringstorms.config.ClientConfigCommandPayload;
import net.dabicco.devouringstorms.config.RequestWitherStormConfigPayload;
import net.dabicco.devouringstorms.config.SyncWitherStormConfigPayload;
import net.dabicco.devouringstorms.config.UpdateWitherStormConfigPayload;
import net.dabicco.devouringstorms.config.WitherStormConfigs;
import net.dabicco.devouringstorms.config.WitherStormWorldConfig;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.dabicco.devouringstorms.network.TentaclePathPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SigeonNetwork {
   public static void register() {
      System.out.println("Registering config packets");
      PayloadTypeRegistry.serverboundPlay().register(UpdateWitherStormConfigPayload.TYPE, UpdateWitherStormConfigPayload.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(RequestWitherStormConfigPayload.TYPE, RequestWitherStormConfigPayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(SyncWitherStormConfigPayload.TYPE, SyncWitherStormConfigPayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(ClientConfigCommandPayload.TYPE, ClientConfigCommandPayload.CODEC);
      ServerPlayConnectionEvents.JOIN.register((ServerPlayConnectionEvents.Join)(handler, sender, server) -> server.execute(() -> syncTo(handler.player)));
      CustomPacketPayload.Type<TentaclePathPayload> tentacleType = TentaclePathPayload.TYPE;
      PayloadTypeRegistry.serverboundPlay().register(tentacleType, TentaclePathPayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(tentacleType, (payload, context) -> context.player().level().getServer().execute(() -> {
            Entity patt0$temp = context.player().level().getEntity(payload.stormId());
            if (patt0$temp instanceof WitherStormEntity storm) {
               storm.carveAlong(payload.points());
            }

         }));
      ServerPlayNetworking.registerGlobalReceiver(RequestWitherStormConfigPayload.TYPE, (payload, context) -> context.server().execute(() -> syncTo(context.player())));
      ServerPlayNetworking.registerGlobalReceiver(UpdateWitherStormConfigPayload.TYPE, (payload, context) -> context.server().execute(() -> {
            ServerPlayer player = context.player();
            if (!canEdit(player)) {
               System.out.println("Rejected config update from " + String.valueOf(player.getGameProfile()));
            } else {
               WitherStormWorldConfig cfg = WitherStormConfigs.get(player.level());
               cfg.applyArray(payload.values());
               cfg.markChanged();
               broadcastSync(player.level());
            }
         }));
   }

   public static boolean canEdit(ServerPlayer player) {
      MinecraftServer server = player.level().getServer();
      return server.isSingleplayer() || server.getPlayerList().isOp(player.nameAndId());
   }

   public static void syncTo(ServerPlayer player) {
      WitherStormWorldConfig cfg = WitherStormConfigs.get(player.level());
      ServerPlayNetworking.send(player, new SyncWitherStormConfigPayload(cfg.toArray(), canEdit(player)));
   }

   public static void broadcastSync(ServerLevel level) {
      WitherStormWorldConfig cfg = WitherStormConfigs.get(level);
      double[] values = cfg.toArray();

      for(ServerPlayer p : level.players()) {
         ServerPlayNetworking.send(p, new SyncWitherStormConfigPayload(values, canEdit(p)));
      }

   }
}
