package net.dabicco.devouringstorms.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record SpawnStructurePayload(boolean inside, double x, double floorY, double z) implements CustomPacketPayload {
   public static final Type<SpawnStructurePayload> TYPE = new Type(Identifier.fromNamespaceAndPath("devouringstorms", "spawn_structure"));
   public static final StreamCodec<RegistryFriendlyByteBuf, SpawnStructurePayload> CODEC = StreamCodec.of((buf, pkt) -> {
      buf.writeBoolean(pkt.inside);
      buf.writeDouble(pkt.x);
      buf.writeDouble(pkt.floorY);
      buf.writeDouble(pkt.z);
   }, buf -> new SpawnStructurePayload(buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readDouble()));

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
