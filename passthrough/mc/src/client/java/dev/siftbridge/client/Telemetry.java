package dev.siftbridge.client;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The crosshair/camera vector stream (the Phase-2 "Data Streamer").
 *
 * <p>The render thread publishes one immutable pose per frame into an {@link AtomicReference};
 * the link thread reads it at its own cadence and serialises to JSON there. Publishing is a
 * single store — the render tick never blocks, never touches the socket, and the ~60 tiny
 * records per second are trivial garbage next to what a frame already allocates.
 */
public final class Telemetry {
	/** One crosshair/camera sample, Minecraft coordinates. */
	public record Pose(long frame, double x, double y, double z, float yaw, float pitch, float roll,
			float fov, boolean firstPerson, long ts) {
	}

	private static final AtomicReference<Pose> LATEST = new AtomicReference<>();

	private Telemetry() {
	}

	/** Called from the camera mixin once per rendered frame. */
	public static void capture(final long frameCounter, final double px, final double py, final double pz,
			final float yawDeg, final float pitchDeg, final float rollDeg, final float verticalFov, final boolean fp) {
		LATEST.set(new Pose(frameCounter, px, py, pz, yawDeg, pitchDeg, rollDeg, verticalFov, fp, System.nanoTime()));
	}

	/** Latest pose for the link thread, or {@code null} before the first frame. */
	public static Pose latest() {
		return LATEST.get();
	}
}
