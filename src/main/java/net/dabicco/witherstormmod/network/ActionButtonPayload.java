package net.dabicco.witherstormmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ActionButtonPayload(boolean rightHand) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<ActionButtonPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("dabywitherstormmod", "action_button"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ActionButtonPayload> CODEC = StreamCodec.of((buf, payload) -> buf.writeBoolean(payload.rightHand()), (buf) -> new ActionButtonPayload(buf.readBoolean()));

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
