package net.dabicco.devouringstorms.config;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientConfigCommandPayload(int mode, String key, double value) implements CustomPacketPayload {
   public static final int MODE_GET = 0;
   public static final int MODE_SET = 1;
   public static final int MODE_LIST = 2;
   public static final int MODE_OPEN_GUI = 3;
   public static final CustomPacketPayload.Type<ClientConfigCommandPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("witherstormmod", "client_config_command"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ClientConfigCommandPayload> CODEC = StreamCodec.of((buf, payload) -> {
      buf.writeVarInt(payload.mode());
      buf.writeUtf(payload.key());
      buf.writeDouble(payload.value());
   }, (buf) -> new ClientConfigCommandPayload(buf.readVarInt(), buf.readUtf(), buf.readDouble()));

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
