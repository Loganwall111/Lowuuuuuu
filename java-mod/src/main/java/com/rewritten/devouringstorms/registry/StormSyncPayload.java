package com.rewritten.devouringstorms.registry;

import com.rewritten.devouringstorms.DevouringStorms;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server→client storm state. Drives the horror presentation: sky corruption overlay,
 * phase music switching, and the critical-music drop when the storm is devolving.
 *
 * @param phase       MASSG phase id (see storm/MassgPhase), or -1 when no storm is active
 * @param growth      growth towards the next phase (0..1)
 * @param critical    true while the storm is devolving / close to death — "the music turns critical"
 * @param stormActive true while a MASSG exists somewhere in the world
 * @param intensity   presentation intensity 0..1 (drives overlay alpha, fog grade, glitch amount)
 */
public record StormSyncPayload(int phase, float growth, boolean critical, boolean stormActive, float intensity)
    implements CustomPacketPayload {

    public static final Type<StormSyncPayload> TYPE = new Type<>(DevouringStorms.id("storm_sync"));

    public static final StreamCodec<FriendlyByteBuf, StormSyncPayload> CODEC = StreamCodec.of(
        (buf, v) -> {
            buf.writeInt(v.phase());
            buf.writeFloat(v.growth());
            buf.writeBoolean(v.critical());
            buf.writeBoolean(v.stormActive());
            buf.writeFloat(v.intensity());
        },
        buf -> new StormSyncPayload(buf.readInt(), buf.readFloat(), buf.readBoolean(), buf.readBoolean(), buf.readFloat())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
