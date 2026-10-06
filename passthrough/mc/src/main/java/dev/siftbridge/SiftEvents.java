package dev.siftbridge;

import java.util.function.Consumer;

/**
 * Server-side hooks (explosions, rift pulses) publish through this sink. The client entrypoint
 * wires it to the live socket; while no client is up the events are dropped, never queued —
 * the bridge must never accumulate state of its own.
 */
public final class SiftEvents {
	private static volatile Consumer<String> out = json -> {
	};

	private SiftEvents() {
	}

	public static void bind(final Consumer<String> sink) {
		out = sink;
	}

	public static void emit(final String json) {
		out.accept(json);
	}
}
