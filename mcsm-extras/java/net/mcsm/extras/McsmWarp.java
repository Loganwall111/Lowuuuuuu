package net.mcsm.extras;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;

/**
 * Mega-phase 6b: the warp entry sequence (user order: "approaching a portal
 * = screen distorts and pulls the camera through instead of an instant
 * loading screen").
 *
 * The base mod teleports the instant a player touches the storm mouth
 * (BowelsPortal.send). A common mixin cancels that call and registers the
 * player here instead; the client HUD then plays the distortion/pull
 * sequence and, when it finishes, asks the server thread to run the
 * original send exactly once (thread-local bypass so the mixin lets that
 * one call through).
 *
 * Singleplayer: the mixin transforms the class in this JVM and the
 * integrated server shares it, so the sequence always plays. Dedicated
 * server without the mod: untransformed send, instant teleport - the
 * vanilla-of-the-mod fallback, never a crash.
 */
public final class McsmWarp {

    /** Seconds the warp sequence lasts before the real teleport runs. */
    public static final float WARP_SECONDS = 1.7F;

    private static final Map<UUID, Long> PENDING = new HashMap<>();
    private static final ThreadLocal<Boolean> BYPASS = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private McsmWarp() {
    }

    /**
     * Called from the send() interceptor. Returns true when the teleport
     * should be CANCELLED (sequence started, or already running).
     */
    public static boolean begin(ServerPlayer player) {
        if (BYPASS.get()) {
            return false;
        }
        synchronized (PENDING) {
            if (PENDING.containsKey(player.getUUID())) {
                return true;
            }
            PENDING.put(player.getUUID(), System.nanoTime());
        }
        return true;
    }

    public static boolean warping(UUID id) {
        synchronized (PENDING) {
            return PENDING.containsKey(id);
        }
    }

    /** 0..1 progress of the client sequence. */
    public static float progress(UUID id) {
        Long start;
        synchronized (PENDING) {
            start = PENDING.get(id);
        }
        if (start == null) {
            return 0.0F;
        }
        float t = (System.nanoTime() - start) / 1.0E9F;
        return Math.min(Math.max(t / WARP_SECONDS, 0.0F), 1.0F);
    }

    public static void cancel(UUID id) {
        synchronized (PENDING) {
            PENDING.remove(id);
        }
    }

    /** Runs on the server thread: the original teleport, once, uncancelled. */
    public static void release(ServerPlayer player) {
        synchronized (PENDING) {
            PENDING.remove(player.getUUID());
        }
        BYPASS.set(Boolean.TRUE);
        try {
            Class<?> bp = Class.forName("net.dabicco.witherstormmod.BowelsPortal");
            Method send = bp.getMethod("send", ServerPlayer.class);
            send.invoke(null, player);
        } catch (Throwable ignored) {
            // no portal code on this jar: the sequence was pure spectacle
        } finally {
            BYPASS.set(Boolean.FALSE);
        }
    }
}
