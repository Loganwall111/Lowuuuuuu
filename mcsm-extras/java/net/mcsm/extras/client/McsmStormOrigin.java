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
    private static final Method ORIGIN_METHOD = findOriginMethod();

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
        // The base manager captures this once at LevelRenderer.render HEAD.
        // Never run a second nearest-storm search in an atmospheric pass.
        return ClientDistantStormManager.nearestCustomWeather(camera);
    }

    private static Method findOriginMethod() {
        try {
            return ClientDistantStormManager.StormData.class.getMethod("getStormOrigin");
        } catch (Throwable ignored) {
            return null;
        }
    }
}
