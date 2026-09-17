package net.mcsm.extras.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.mcsm.extras.McsmSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;

/**
 * BUILD #463 -- what the client knows about who the void is taking.
 *
 * <p>Keyed by entity id, exactly like the base mod's own sickness tracker, and
 * stale entries are dropped the same way: a value older than eight seconds stops
 * counting, so a player who left, died or logged out goes back to their own skin
 * rather than staying discoloured forever.
 */
public final class McsmVoidAgingClient {

    private static final long STALE_MILLIS = 8000L;

    private static final Map<Integer, Entry> AGED = new HashMap<>();
    private static int heartbeat;

    private McsmVoidAgingClient() {
    }

    private static final class Entry {
        float age;
        long lastUpdate;
    }

    public static void set(int entityId, float age) {
        Entry entry = AGED.computeIfAbsent(entityId, k -> new Entry());
        entry.age = Math.max(0.0F, Math.min(1.0F, age));
        entry.lastUpdate = System.currentTimeMillis();
    }

    /** 0..1 for one entity, or 0 when nothing recent is known about it. */
    public static float ageOf(int entityId) {
        Entry entry = AGED.get(entityId);
        if (entry == null) {
            return 0.0F;
        }
        if (System.currentTimeMillis() - entry.lastUpdate > STALE_MILLIS) {
            AGED.remove(entityId);
            return 0.0F;
        }
        return entry.age;
    }

    public static void clear() {
        AGED.clear();
        heartbeat = 0;
    }

    public static void prune() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, Entry>> it = AGED.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue().lastUpdate > STALE_MILLIS) {
                it.remove();
            }
        }
    }

    /**
     * The player's own half of the effect: once the void has most of them, they
     * hear it in their chest. Called from the same per-frame sink the blasts and
     * the dimension air use; it steps at most once per game tick.
     */
    public static void tick() {
        try {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) {
                clear();
                return;
            }
            prune();
            float mine = ageOf(player.getId());
            if (mine < 0.6F) {
                return;
            }
            heartbeat++;
            int every = mine >= 0.9F ? 40 : 70;
            if (heartbeat % every != 0) {
                return;
            }
            // the exact local-sound form the dimension air already plays through:
            // (x, y, z, event, source, volume, pitch, distanceDelay)
            player.level().playLocalSound(player.getX(), player.getY() + 1.0D, player.getZ(),
                    mine >= 0.9F ? McsmSounds.MASSG_GIGGLE : McsmSounds.MASSG_HEART,
                    SoundSource.PLAYERS, 0.25F + 0.3F * mine, 0.95F, false);
        } catch (Throwable ignored) {
            // a heartbeat that does not play is not worth a crash
        }
    }
}
