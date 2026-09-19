package net.dabicco.witherstormmod.config;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record SyncWitherStormConfigPayload(double[] values, boolean canEdit) implements CustomPacketPayload {
   public static final Type<net.dabicco.witherstormmod.config.SyncWitherStormConfigPayload> TYPE = new Type(
      Identifier.fromNamespaceAndPath("witherstormmod", "sync_config")
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.dabicco.witherstormmod.config.SyncWitherStormConfigPayload> CODEC = StreamCodec.of(
      (buf, payload) -> {
         buf.writeVarInt(payload.values().length);

         for (double v : payload.values()) {
            buf.writeDouble(v);
         }

         buf.writeBoolean(payload.canEdit());
      }, buf -> {
         double[] values = new double[buf.readVarInt()];

         for (int i = 0; i < values.length; i++) {
            values[i] = buf.readDouble();
         }

         return new net.dabicco.witherstormmod.config.SyncWitherStormConfigPayload(values, buf.readBoolean());
      }
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
