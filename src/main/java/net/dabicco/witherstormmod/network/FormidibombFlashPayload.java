package net.dabicco.witherstormmod.network;

import net.dabicco.witherstormmod.client.FormidibombFlash;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.ClientPayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FormidibombFlashPayload(double x, double y, double z) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<FormidibombFlashPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("dabywitherstormmod", "formidibomb_flash"));
   public static final StreamCodec<RegistryFriendlyByteBuf, FormidibombFlashPayload> CODEC = StreamCodec.of((buf, payload) -> {
      buf.writeDouble(payload.x());
      buf.writeDouble(payload.y());
      buf.writeDouble(payload.z());
   }, (buf) -> new FormidibombFlashPayload(buf.readDouble(), buf.readDouble(), buf.readDouble()));

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(FormidibombFlashPayload payload, ClientPayloadContext context) {
      context.client().execute(() -> FormidibombFlash.trigger(payload.x(), payload.y(), payload.z()));
   }
}
