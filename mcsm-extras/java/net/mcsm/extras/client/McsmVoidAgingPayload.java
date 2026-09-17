package net.mcsm.extras.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

/**
 * BUILD #463 -- the one message void aging needs.
 *
 * <p>Ageing is decided on the server (it is a consequence, and consequences are
 * not the client's to decide) and it is VISIBLE on the client, because what it
 * changes is the player's own body. This carries one entity id and one 0..1
 * value: who is being taken, and how far in. It is registered and handled exactly
 * the way the base mod's own wither-sickness payload is -- same TYPE/CODEC shape,
 * same client receiver, same context.execute hop -- because that path is already
 * running in this jar.
 */
public record McsmVoidAgingPayload(int entityId, float age) implements CustomPacketPayload {

    public static final Type<McsmVoidAgingPayload> TYPE =
            new Type(Identifier.fromNamespaceAndPath("mcsm", "void_aging"));

    public static final StreamCodec<RegistryFriendlyByteBuf, McsmVoidAgingPayload> CODEC =
            StreamCodec.of((buf, pkt) -> {
                buf.writeVarInt(pkt.entityId());
                buf.writeFloat(pkt.age());
            }, buf -> new McsmVoidAgingPayload(buf.readVarInt(), buf.readFloat()));

    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(McsmVoidAgingPayload payload, Context context) {
        context.client().execute(() -> McsmVoidAgingClient.set(payload.entityId(),
                payload.age()));
    }
}
