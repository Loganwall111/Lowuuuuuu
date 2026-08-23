package net.dabicco.devouringstorms.network;

import net.dabicco.devouringstorms.client.FormidibombBlast;
import net.dabicco.devouringstorms.client.FormidibombFlash;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record FormidibombFlashPayload(double x, double y, double z) implements CustomPacketPayload {
   public static final double FULL_RADIUS = 350.0;
   public static final double SEE_RADIUS = 2000.0;
   public static final Type<FormidibombFlashPayload> TYPE = new Type(Identifier.fromNamespaceAndPath("devouringstorms", "formidibomb_flash"));
   public static final StreamCodec<RegistryFriendlyByteBuf, FormidibombFlashPayload> CODEC = StreamCodec.of((buf, pkt) -> {
      buf.writeDouble(pkt.x());
      buf.writeDouble(pkt.y());
      buf.writeDouble(pkt.z());
   }, buf -> new FormidibombFlashPayload(buf.readDouble(), buf.readDouble(), buf.readDouble()));

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(FormidibombFlashPayload payload, Context context) {
      context.client().execute(() -> {
         FormidibombBlast.trigger(payload.x(), payload.y(), payload.z());
         FormidibombFlash.trigger(payload.x(), payload.y(), payload.z());
      });
   }
}
