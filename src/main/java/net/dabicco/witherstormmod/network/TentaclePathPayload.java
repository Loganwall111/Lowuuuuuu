package net.dabicco.witherstormmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TentaclePathPayload(int stormId, float[] points) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<TentaclePathPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("dabywitherstormmod", "tentacle_path"));
   public static final StreamCodec<RegistryFriendlyByteBuf, TentaclePathPayload> CODEC = StreamCodec.of((buf, payload) -> {
      buf.writeVarInt(payload.stormId());
      float[] points = payload.points();
      buf.writeVarInt(points == null ? 0 : points.length);
      if (points != null) {
         for(float v : points) {
            buf.writeFloat(v);
         }
      }
   }, (buf) -> {
      int stormId = buf.readVarInt();
      int n = buf.readVarInt();
      float[] points = new float[n];
      for(int i = 0; i < n; ++i) {
         points[i] = buf.readFloat();
      }
      return new TentaclePathPayload(stormId, points);
   });

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
