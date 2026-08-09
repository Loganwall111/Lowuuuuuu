package com.rewritten.devouringstorms.registry;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/** Packet registration. Payload records live in their own classes under this package. */
public final class ModPackets {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(StormSyncPayload.TYPE, StormSyncPayload.CODEC);
    }

    private ModPackets() {
    }
}
