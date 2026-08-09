package com.rewritten.devouringstorms.world;

import com.rewritten.devouringstorms.storm.StormDirector;
import com.rewritten.devouringstorms.util.ModTexts;
import com.rewritten.devouringstorms.util.RiftTravel;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * World-level events:
 *  - THE CYCLE: if you die inside the Decayed Reality, the quarantine does not let you leave.
 *    You respawn there. Reincarnation, until you earn your way out.
 *  - First load of the Decayed Reality builds the arrival platform (with Tazo waiting).
 */
public final class ModWorldEvents {

    private ModWorldEvents() {
    }

    public static void register() {
        // Reincarnation — "a quarantined world trapped within an endless cycle of
        // destruction, rebirth, and corruption."
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!(oldPlayer.level() instanceof ServerLevel oldWorld)) return;
            if (oldWorld.dimension() != ModDimensions.DECAYED_LEVEL_KEY) return;

            RiftTravel.travel(newPlayer, ModDimensions.DECAYED_LEVEL_KEY);
            newPlayer.sendSystemMessage(Component.literal(ModTexts.CYCLE_CONTINUES));
        });

        // Platform assembly on first dimension load.
        ServerWorldEvents.LOAD.register((server, level) ->
            StormDirector.ensureSpawnPlatform(level));
    }
}
