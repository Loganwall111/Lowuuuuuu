package net.dabicco.witherstormmod.network;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.dabicco.witherstormmod.client.StormSkyDarken;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.ClientPayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StormRemovedPacket(int entityId) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<StormRemovedPacket> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("dabywitherstormmod", "storm_removed"));
   public static final StreamCodec<RegistryFriendlyByteBuf, StormRemovedPacket> CODEC = StreamCodec.of((buf, payload) -> buf.writeVarInt(payload.entityId()), (buf) -> new StormRemovedPacket(buf.readVarInt()));

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(StormRemovedPacket payload, ClientPayloadContext context) {
      context.client().execute(() -> {
         ClientDistantStormManager.remove(payload.entityId());
         StormSkyDarken.clear();
      });
   }
}
