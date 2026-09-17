package net.dabicco.witherstormmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record CaveRumblePayload(int durationTicks, float intensity) implements CustomPacketPayload {
   public static final Type<net.dabicco.witherstormmod.network.CaveRumblePayload> TYPE = new Type(
      Identifier.fromNamespaceAndPath("dabywitherstormmod", "cave_rumble")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.dabicco.witherstormmod.network.CaveRumblePayload> CODEC = StreamCodec.of((buf, pkt) -> {
      buf.writeVarInt(pkt.durationTicks());
      buf.writeFloat(pkt.intensity());
   }, buf -> new net.dabicco.witherstormmod.network.CaveRumblePayload(buf.readVarInt(), buf.readFloat()));

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
