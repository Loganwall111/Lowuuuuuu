package dev.siftbridge.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Camera poses the host sends us (reverse direction: D2 driving Minecraft).
 *
 * <p>Two modes, selected by the host's {@code mode} message:
 * <ul>
 * <li>{@code free} (default) — Minecraft plays normally; only telemetry flows out.</li>
 * <li>{@code hosted} — the local player is placed at the host camera each client tick,
 *     frame-level smoothing is left to a later revision (upstream does it in the camera mixin).</li>
 * </ul>
 */
public final class HostState {
	public record Pose(double x, double y, double z, float yaw, float pitch, float roll, float fov,
			boolean firstPerson, long hostFrame) {
	}

	private static final AtomicReference<Pose> LIVE = new AtomicReference<>();
	private static volatile boolean hosted;

	private HostState() {
	}

	public static boolean hosted() {
		return hosted;
	}

	public static Pose live() {
		return LIVE.get();
	}

	static void mode(final String m) {
		hosted = "hosted".equals(m);
	}

	static void accept(final JsonObject m) {
		JsonArray p = m.getAsJsonArray("p");
		JsonArray r = m.getAsJsonArray("r");
		LIVE.set(new Pose(
			p.get(0).getAsDouble(), p.get(1).getAsDouble(), p.get(2).getAsDouble(),
			r.get(0).getAsFloat(), r.get(1).getAsFloat(), r.size() > 2 ? r.get(2).getAsFloat() : 0.0F,
			m.has("fov") ? m.get("fov").getAsFloat() : 70.0F,
			!m.has("fp") || m.get("fp").getAsBoolean(),
			m.has("f") ? m.get("f").getAsLong() : 0L
		));
	}

	static void clear() {
		LIVE.set(null);
	}
}
