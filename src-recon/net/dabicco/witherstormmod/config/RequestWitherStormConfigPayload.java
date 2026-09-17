package net.dabicco.witherstormmod.config;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record RequestWitherStormConfigPayload() implements CustomPacketPayload {
   public static final Type<net.dabicco.witherstormmod.config.RequestWitherStormConfigPayload> TYPE = new Type(
      Identifier.fromNamespaceAndPath("witherstormmod", "request_config")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.dabicco.witherstormmod.config.RequestWitherStormConfigPayload> CODEC = StreamCodec.of(
      (buf, payload) -> {}, buf -> new net.dabicco.witherstormmod.config.RequestWitherStormConfigPayload()
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
