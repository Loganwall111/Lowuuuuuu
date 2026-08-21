package net.dabicco.witherstormmod.network;

import net.dabicco.witherstormmod.client.ClientWitheredManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.ClientPayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record WitheredCastPayload(int casterId, int ability, String command, int targetId, int duration) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<WitheredCastPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("dabywitherstormmod", "withered_cast"));
   public static final StreamCodec<RegistryFriendlyByteBuf, WitheredCastPayload> CODEC = StreamCodec.of((buf, payload) -> {
      buf.writeVarInt(payload.casterId());
      buf.writeVarInt(payload.ability());
      buf.writeUtf(payload.command());
      buf.writeVarInt(payload.targetId());
      buf.writeVarInt(payload.duration());
   }, (buf) -> new WitheredCastPayload(buf.readVarInt(), buf.readVarInt(), buf.readUtf(), buf.readVarInt(), buf.readVarInt()));

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(WitheredCastPayload payload, ClientPayloadContext context) {
      context.client().execute(() -> ClientWitheredManager.onCast(payload));
   }
}
