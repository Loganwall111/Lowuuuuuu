package net.dabicco.witherstormmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record ActionButtonPayload(boolean rightHand) implements CustomPacketPayload {
   public static final Type<net.dabicco.witherstormmod.network.ActionButtonPayload> TYPE = new Type(
      Identifier.fromNamespaceAndPath("dabywitherstormmod", "action_button")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.dabicco.witherstormmod.network.ActionButtonPayload> CODEC = StreamCodec.of(
      (buf, pkt) -> buf.writeBoolean(pkt.rightHand()), buf -> new net.dabicco.witherstormmod.network.ActionButtonPayload(buf.readBoolean())
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
