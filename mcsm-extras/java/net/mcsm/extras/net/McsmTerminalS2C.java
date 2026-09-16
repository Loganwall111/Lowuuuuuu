package net.mcsm.extras.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

/**
 * BUILD #416 (D.8, phase 5) -- what the terminal tells the client.
 *
 * The shape is the base mod's own (see its ClientConfigCommandPayload): a record
 * with a Type and a StreamCodec built from writeUtf/readUtf, registered by the
 * mod's initializer. Three strings carry everything the console screens and the
 * radio need:
 *
 *   action : "open" | "close" | "line" | "radio" | "granted" | "denied"
 *   a      : the screen's mode ("login" | "console" | "guide" | "radio")
 *   b      : the payload (a console line, a station name, the guide page)
 */
public record McsmTerminalS2C(String action, String a, String b) implements CustomPacketPayload {

    public static final Type<McsmTerminalS2C> TYPE = new Type(
            Identifier.fromNamespaceAndPath("mcsm", "terminal_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, McsmTerminalS2C> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.action() == null ? "" : payload.action(), 256);
                buf.writeUtf(payload.a() == null ? "" : payload.a(), 1024);
                buf.writeUtf(payload.b() == null ? "" : payload.b(), 8192);
            },
            buf -> new McsmTerminalS2C(buf.readUtf(256), buf.readUtf(1024), buf.readUtf(8192)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
