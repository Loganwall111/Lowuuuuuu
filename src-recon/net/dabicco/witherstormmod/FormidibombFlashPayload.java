package net.dabicco.witherstormmod.network;

import net.dabicco.witherstormmod.client.FormidibombBlast;
import net.dabicco.witherstormmod.client.FormidibombFlash;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record FormidibombFlashPayload(double x, double y, double z) implements CustomPacketPayload {
   public static final double FULL_RADIUS = 350.0;
   public static final double SEE_RADIUS = 2000.0;
   public static final Type<net.dabicco.witherstormmod.network.FormidibombFlashPayload> TYPE = new Type(
      Identifier.fromNamespaceAndPath("dabywitherstormmod", "formidibomb_flash")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.dabicco.witherstormmod.network.FormidibombFlashPayload> CODEC = StreamCodec.of((buf, pkt) -> {
      buf.writeDouble(pkt.x());
      buf.writeDouble(pkt.y());
      buf.writeDouble(pkt.z());
   }, buf -> new net.dabicco.witherstormmod.network.FormidibombFlashPayload(buf.readDouble(), buf.readDouble(), buf.readDouble()));

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(net.dabicco.witherstormmod.network.FormidibombFlashPayload payload, Context context) {
      context.client().execute(() -> {
         FormidibombBlast.trigger(payload.x(), payload.y(), payload.z());
         FormidibombFlash.trigger(payload.x(), payload.y(), payload.z());
      });
   }
}
