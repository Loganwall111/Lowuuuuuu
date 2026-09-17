package dev.siftcore.client;

import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;

/**
 * A narrow client-side guard for the one screen vanilla may display while a
 * respawn packet is constructing the new world. It is armed only by a Sift
 * respawn packet and expires quickly if the packet is unrelated to loading.
 */
public final class SiftTransitionClient {
    private static int skipTerrainScreenTicks;

    private SiftTransitionClient() {
    }

    public static void arm() {
        skipTerrainScreenTicks = 20;
    }

    public static void tick() {
        if (skipTerrainScreenTicks > 0) {
            skipTerrainScreenTicks--;
        }
    }

    /** Clears a pending packet guard when the client leaves a world. */
    public static void clear() {
        skipTerrainScreenTicks = 0;
    }

    public static boolean consumeLoadingScreen(Screen screen) {
        if (skipTerrainScreenTicks > 0 && screen instanceof DownloadingTerrainScreen) {
            skipTerrainScreenTicks = 0;
            return true;
        }
        return false;
    }
}
