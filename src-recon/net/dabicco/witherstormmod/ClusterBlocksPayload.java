package net.dabicco.witherstormmod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record ClusterBlocksPayload(int clusterEntityId, List<Integer> blockStateIds, List<BlockPos> offsets) implements CustomPacketPayload {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("dabywitherstormmod", "cluster_blocks");
   public static final Type<ClusterBlocksPayload> TYPE = new Type(ID);
   public static final StreamCodec<RegistryFriendlyByteBuf, ClusterBlocksPayload> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      ClusterBlocksPayload::clusterEntityId,
      ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.VAR_INT),
      ClusterBlocksPayload::blockStateIds,
      BlockPos.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)),
      ClusterBlocksPayload::offsets,
      ClusterBlocksPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
