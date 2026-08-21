package net.dabicco.witherstormmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CaveRumblePayload(int durationTicks, float intensity) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<CaveRumblePayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("dabywitherstormmod", "cave_rumble"));
   public static final StreamCodec<RegistryFriendlyByteBuf, CaveRumblePayload> CODEC = StreamCodec.of((buf, payload) -> {
      buf.writeVarInt(payload.durationTicks());
      buf.writeFloat(payload.intensity());
   }, (buf) -> new CaveRumblePayload(buf.readVarInt(), buf.readFloat()));

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
