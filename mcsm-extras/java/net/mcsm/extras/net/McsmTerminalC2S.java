package net.mcsm.extras.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

/**
 * BUILD #416 (D.8, phase 5) -- what the client tells the terminal.
 *
 *   action : "open"    -- the player used the antenna (or the C key), open it
 *            "code"    -- the password the player typed (a = the code)
 *            "guide"   -- the guide book was used (a = the page requested)
 *            "close"   -- the console was closed
 *   a      : the code, or the page, or ""
 *   b      : free, for later passes
 */
public record McsmTerminalC2S(String action, String a, String b) implements CustomPacketPayload {

    public static final Type<McsmTerminalC2S> TYPE = new Type(
            Identifier.fromNamespaceAndPath("mcsm", "terminal_c2s"));

    public static final StreamCodec<RegistryFriendlyByteBuf, McsmTerminalC2S> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.action() == null ? "" : payload.action(), 256);
                buf.writeUtf(payload.a() == null ? "" : payload.a(), 1024);
                buf.writeUtf(payload.b() == null ? "" : payload.b(), 1024);
            },
            buf -> new McsmTerminalC2S(buf.readUtf(256), buf.readUtf(1024), buf.readUtf(1024)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
