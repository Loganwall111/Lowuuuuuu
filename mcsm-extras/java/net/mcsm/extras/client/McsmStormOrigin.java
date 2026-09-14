package net.mcsm.extras.client;

import java.lang.reflect.Method;

import net.dabicco.witherstormmod.client.ClientDistantStormManager;
import net.minecraft.world.phys.Vec3;

/**
 * One source of truth for atmospheric placement.  The sky tint, distant dome,
 * and soft storm volume all ask this adapter for the same origin; none of them
 * is allowed to derive a second centre from a head, billboard, or camera.
 */
public final class McsmStormOrigin {
    private static final Method ORIGIN_METHOD = findMethod("getStormOrigin");
    // The shipping overlay compiles against the pinned base jar, while the
    // whole-source build supplies the newer per-frame capture API. Reflect on
    // it so both build tracks remain compatible.
    private static final Method CAPTURE_METHOD = findManagerMethod("captureAtmosphere", Vec3.class);
    private static final Method NEAREST_METHOD = findManagerMethod("nearestCustomWeather", Vec3.class);

    private McsmStormOrigin() {
    }

    public static Vec3 getStormOrigin(ClientDistantStormManager.StormData state) {
        if (state == null) {
            return Vec3.ZERO;
        }
        try {
            if (ORIGIN_METHOD != null) {
                Object value = ORIGIN_METHOD.invoke(state);
                if (value instanceof Vec3 origin) {
                    return origin;
                }
            }
        } catch (Throwable ignored) {
            // Older delivery jars fall back to the authoritative packet position.
        }
        return new Vec3(state.x, state.y, state.z);
    }

    public static ClientDistantStormManager.StormData nearest(Vec3 camera) {
        // The source build captures this once at LevelRenderer.render HEAD.
        // The pinned delivery jar predates that API, so retain a reflective
        // fallback rather than making the overlay uncompilable against it.
        if (CAPTURE_METHOD != null && NEAREST_METHOD != null) {
            try {
                return (ClientDistantStormManager.StormData) NEAREST_METHOD.invoke(null, camera);
            } catch (Throwable ignored) {
                // Fall through to the compatible legacy scan.
            }
        }

        ClientDistantStormManager.StormData best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ClientDistantStormManager.StormData state : ClientDistantStormManager.all()) {
            if (state.phase < 5.0F) {
                continue;
            }
            double distance = getStormOrigin(state).distanceToSqr(camera);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = state;
            }
        }
        return best;
    }

    private static Method findManagerMethod(String name, Class<?>... parameterTypes) {
        try {
            return ClientDistantStormManager.class.getMethod(name, parameterTypes);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method findMethod(String name) {
        try {
            return ClientDistantStormManager.StormData.class.getMethod(name);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
