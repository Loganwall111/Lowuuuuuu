package net.dabicco.devouringstorms.network;

import net.dabicco.devouringstorms.client.StormPulseFX;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record StormPulsePayload(int entityId, double x, double y, double z, float phase, int kind) implements CustomPacketPayload {
   public static final Type<StormPulsePayload> TYPE = new Type(Identifier.fromNamespaceAndPath("devouringstorms", "storm_pulse"));
   public static final StreamCodec<RegistryFriendlyByteBuf, StormPulsePayload> CODEC = StreamCodec.of(
      (buf, pkt) -> {
         buf.writeVarInt(pkt.entityId());
         buf.writeDouble(pkt.x());
         buf.writeDouble(pkt.y());
         buf.writeDouble(pkt.z());
         buf.writeFloat(pkt.phase());
         buf.writeVarInt(pkt.kind());
      },
      buf -> new StormPulsePayload(buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readVarInt())
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(StormPulsePayload payload, Context context) {
      context.client().execute(() -> StormPulseFX.trigger(payload.entityId(), payload.x(), payload.y(), payload.z(), payload.phase(), payload.kind()));
   }
}
