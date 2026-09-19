package net.dabicco.witherstormmod.network;

import net.dabicco.witherstormmod.client.ClientWitheredManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record WitheredCastPayload(int casterId, int ability, int duration, String command, int targetId) implements CustomPacketPayload {
   public static final Type<net.dabicco.witherstormmod.network.WitheredCastPayload> TYPE = new Type(
      Identifier.fromNamespaceAndPath("dabywitherstormmod", "withered_cast")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.dabicco.witherstormmod.network.WitheredCastPayload> CODEC = StreamCodec.of(
      (buf, pkt) -> {
         buf.writeVarInt(pkt.casterId());
         buf.writeVarInt(pkt.ability());
         buf.writeVarInt(pkt.duration());
         buf.writeUtf(pkt.command(), 160);
         buf.writeVarInt(pkt.targetId() + 1);
      },
      buf -> new net.dabicco.witherstormmod.network.WitheredCastPayload(
         buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(160), buf.readVarInt() - 1
      )
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(net.dabicco.witherstormmod.network.WitheredCastPayload payload, Context context) {
      context.client().execute(() -> ClientWitheredManager.onCast(payload));
   }
}
