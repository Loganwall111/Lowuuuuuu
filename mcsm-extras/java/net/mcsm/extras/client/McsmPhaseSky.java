package net.mcsm.extras.client;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;

/**
 * Compatibility hook retained for old mixin registrations.  It deliberately
 * emits nothing: McsmStormBlob is the single geometric atmospheric overlay.
 */
public final class McsmPhaseSky {
    private McsmPhaseSky() {
    }

    public static void submit(LevelRenderContext ctx) {
        // no second camera-relative or phase-4 sky card
    }
}
