package net.dabicco.devouringstorms.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record TentaclePathPayload(int stormId, float[] points) implements CustomPacketPayload {
   public static final int POINTS_PER_LIMB = 10;
   public static final int LIMBS = 2;
   public static final int FLOATS = 60;
   public static final Type<TentaclePathPayload> TYPE = new Type(Identifier.fromNamespaceAndPath("devouringstorms", "tentacle_path"));
   public static final StreamCodec<RegistryFriendlyByteBuf, TentaclePathPayload> CODEC = StreamCodec.of((buf, payload) -> {
      buf.writeVarInt(payload.stormId());

      for (int i = 0; i < 60; i++) {
         buf.writeFloat(payload.points()[i]);
      }
   }, buf -> {
      int id = buf.readVarInt();
      float[] pts = new float[60];

      for (int i = 0; i < 60; i++) {
         pts[i] = buf.readFloat();
      }

      return new TentaclePathPayload(id, pts);
   });

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
