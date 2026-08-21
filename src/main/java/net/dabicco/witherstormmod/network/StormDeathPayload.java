package net.dabicco.witherstormmod.network;

import net.dabicco.witherstormmod.client.StormDeathCinematic;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.ClientPayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client: tells clients to play the Wither Storm's death cinematic.
 *
 * Mirrors the video's finale: a bright white pulse, a brief full-screen pure-white
 * hold, then a huge explosion with purple glass shards flying out and a screen-glitch
 * (chromatic-aberration style offset) during the blast.
 */
public record StormDeathPayload(double x, double y, double z, boolean fromBomb) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<StormDeathPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("dabywitherstormmod", "storm_death"));
   public static final StreamCodec<RegistryFriendlyByteBuf, StormDeathPayload> CODEC = StreamCodec.of((buf, payload) -> {
      buf.writeDouble(payload.x());
      buf.writeDouble(payload.y());
      buf.writeDouble(payload.z());
      buf.writeBoolean(payload.fromBomb());
   }, (buf) -> new StormDeathPayload(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean()));

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handleClient(StormDeathPayload payload, ClientPayloadContext context) {
      context.client().execute(() -> StormDeathCinematic.trigger(payload.x(), payload.y(), payload.z(), payload.fromBomb()));
   }
}
