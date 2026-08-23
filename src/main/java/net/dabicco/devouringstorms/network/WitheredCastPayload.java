package net.dabicco.devouringstorms.network;

import net.dabicco.devouringstorms.client.ClientWitheredManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record WitheredCastPayload(int casterId, int ability, int duration, String command, int targetId) implements CustomPacketPayload {
   public static final Type<WitheredCastPayload> TYPE = new Type(Identifier.fromNamespaceAndPath("devouringstorms", "withered_cast"));
   public static final StreamCodec<RegistryFriendlyByteBuf, WitheredCastPayload> CODEC = StreamCodec.of((buf, pkt) -> {
      buf.writeVarInt(pkt.casterId());
      buf.writeVarInt(pkt.ability());
      buf.writeVarInt(pkt.duration());
      buf.writeUtf(pkt.command(), 160);
      buf.writeVarInt(pkt.targetId() + 1);
   }, buf -> new WitheredCastPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(160), buf.readVarInt() - 1));

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(WitheredCastPayload payload, Context context) {
      context.client().execute(() -> ClientWitheredManager.onCast(payload));
   }
}
