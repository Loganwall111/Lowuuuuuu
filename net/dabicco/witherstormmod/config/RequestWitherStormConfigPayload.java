package net.dabicco.witherstormmod.config;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RequestWitherStormConfigPayload() implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<RequestWitherStormConfigPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("witherstormmod", "request_config"));
   public static final StreamCodec<RegistryFriendlyByteBuf, RequestWitherStormConfigPayload> CODEC = StreamCodec.of((buf, payload) -> {
   }, (buf) -> new RequestWitherStormConfigPayload());

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
