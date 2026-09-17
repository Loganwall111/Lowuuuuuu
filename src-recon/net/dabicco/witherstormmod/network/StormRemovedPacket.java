package net.dabicco.witherstormmod.network;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record StormRemovedPacket(int entityId) implements CustomPacketPayload {
   public static final Type<net.dabicco.witherstormmod.network.StormRemovedPacket> TYPE = new Type(
      Identifier.fromNamespaceAndPath("dabywitherstormmod", "storm_removed")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.dabicco.witherstormmod.network.StormRemovedPacket> CODEC = StreamCodec.of(
      (buf, pkt) -> buf.writeVarInt(pkt.entityId()), buf -> new net.dabicco.witherstormmod.network.StormRemovedPacket(buf.readVarInt())
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(net.dabicco.witherstormmod.network.StormRemovedPacket payload, Context context) {
      context.client().execute(() -> ClientDistantStormManager.remove(payload.entityId()));
   }
}
