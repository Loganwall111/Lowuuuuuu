package net.dabicco.witherstormmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CommandBlockPowerPayload(double x, double y, double z) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<CommandBlockPowerPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("dabywitherstormmod", "command_block_power"));
   public static final StreamCodec<RegistryFriendlyByteBuf, CommandBlockPowerPayload> CODEC = StreamCodec.of((buf, payload) -> {
      buf.writeDouble(payload.x());
      buf.writeDouble(payload.y());
      buf.writeDouble(payload.z());
   }, (buf) -> new CommandBlockPowerPayload(buf.readDouble(), buf.readDouble(), buf.readDouble()));

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
