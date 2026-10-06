package com.sift.passthrough;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * SIFT Overhaul x Minecraft Dungeons Passthrough Standalone Client Entrypoint.
 */
public final class SiftPassthroughMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[SIFT-Passthrough] Initializing Port " + SiftDataStreamer.BRIDGE_PORT
                + " Camera/Depth Streamer + MCPT Seqlock Bridge (Escape-key kill switch armed)");
        ClientTickEvents.END_CLIENT_TICK.register(client -> SiftDataStreamer.tick());
    }
}
