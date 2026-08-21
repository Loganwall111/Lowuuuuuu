package net.dabicco.witherstormmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SpawnStructurePayload(boolean inside, double x, double floorY, double z) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<SpawnStructurePayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("dabywitherstormmod", "spawn_structure"));
   public static final StreamCodec<RegistryFriendlyByteBuf, SpawnStructurePayload> CODEC = StreamCodec.of((buf, payload) -> {
      buf.writeBoolean(payload.inside());
      buf.writeDouble(payload.x());
      buf.writeDouble(payload.floorY());
      buf.writeDouble(payload.z());
   }, (buf) -> new SpawnStructurePayload(buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readDouble()));

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
