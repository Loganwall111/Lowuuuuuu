package net.dabicco.devouringstorms.config;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpdateWitherStormConfigPayload(double[] values) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<UpdateWitherStormConfigPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("devouringstorms", "update_config"));
   public static final StreamCodec<RegistryFriendlyByteBuf, UpdateWitherStormConfigPayload> CODEC = StreamCodec.of((buf, payload) -> {
      buf.writeVarInt(payload.values().length);

      for(double v : payload.values()) {
         buf.writeDouble(v);
      }

   }, (buf) -> {
      double[] values = new double[buf.readVarInt()];

      for(int i = 0; i < values.length; ++i) {
         values[i] = buf.readDouble();
      }

      return new UpdateWitherStormConfigPayload(values);
   });

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
