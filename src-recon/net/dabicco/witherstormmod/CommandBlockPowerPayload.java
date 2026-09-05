package net.dabicco.witherstormmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record CommandBlockPowerPayload(double x, double y, double z) implements CustomPacketPayload {
   public static final Type<net.dabicco.witherstormmod.network.CommandBlockPowerPayload> TYPE = new Type(
      Identifier.fromNamespaceAndPath("dabywitherstormmod", "cb_power")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.dabicco.witherstormmod.network.CommandBlockPowerPayload> CODEC = StreamCodec.of((buf, pkt) -> {
      buf.writeDouble(pkt.x());
      buf.writeDouble(pkt.y());
      buf.writeDouble(pkt.z());
   }, buf -> new net.dabicco.witherstormmod.network.CommandBlockPowerPayload(buf.readDouble(), buf.readDouble(), buf.readDouble()));

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
