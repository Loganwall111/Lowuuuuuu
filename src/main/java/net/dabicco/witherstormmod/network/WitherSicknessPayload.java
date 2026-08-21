package net.dabicco.witherstormmod.network;

import net.dabicco.witherstormmod.client.ClientSicknessManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.ClientPayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record WitherSicknessPayload(int entityId, float progress, boolean withered) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<WitherSicknessPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("dabywitherstormmod", "wither_sickness"));
   public static final StreamCodec<RegistryFriendlyByteBuf, WitherSicknessPayload> CODEC = StreamCodec.of((buf, payload) -> {
      buf.writeVarInt(payload.entityId());
      buf.writeFloat(payload.progress());
      buf.writeBoolean(payload.withered());
   }, (buf) -> new WitherSicknessPayload(buf.readVarInt(), buf.readFloat(), buf.readBoolean()));

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(WitherSicknessPayload payload, ClientPayloadContext context) {
      context.client().execute(() -> ClientSicknessManager.set(payload.entityId(), payload.progress(), payload.withered()));
   }
}
