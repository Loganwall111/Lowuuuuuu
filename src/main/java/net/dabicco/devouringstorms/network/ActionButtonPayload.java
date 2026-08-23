package net.dabicco.devouringstorms.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record ActionButtonPayload(boolean rightHand) implements CustomPacketPayload {
   public static final Type<ActionButtonPayload> TYPE = new Type(Identifier.fromNamespaceAndPath("devouringstorms", "action_button"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ActionButtonPayload> CODEC = StreamCodec.of(
      (buf, pkt) -> buf.writeBoolean(pkt.rightHand()), buf -> new ActionButtonPayload(buf.readBoolean())
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
