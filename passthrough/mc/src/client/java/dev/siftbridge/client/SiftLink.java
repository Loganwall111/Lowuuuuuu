package dev.siftbridge.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.siftbridge.SiftBridgeMod;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * The host connection: Minecraft is the WebSocket **client**, the host game listens on
 * {@code 127.0.0.1:8080} (override with {@code -Dsift.port} / {@code -Dsift.host}).
 *
 * <p>Outbound: {@code hello} on connect, then the {@link Telemetry} camera stream at ~60 Hz and
 * {@code pos} events every client tick. Inbound (host → us): {@code cam}, {@code mode},
 * {@code key}, {@code slot}, {@code hud}, {@code view} — see passthrough/PROTOCOL.md.
 *
 * <p>If no host is up yet we keep reconnecting every 2 s, so either game can start first.
 * Everything runs on daemon threads; {@link #shutdown()} is the ESC kill switch.
 */
public final class SiftLink {
	private static final int PORT = Integer.getInteger("sift.port", 8080);
	private static final String HOST = System.getProperty("sift.host", "127.0.0.1");
	private static final long RECONNECT_MS = 2000L;
	private static final long CAM_PERIOD_MS = 16L;    // ~60 Hz, decoupled from both ticks

	private static volatile WebSocketClient conn;
	private static volatile boolean connected;
	private static volatile boolean running = true;
	private static long frameCursor;

	private SiftLink() {
	}

	public static boolean connected() {
		return connected;
	}

	/** Starts the reconnect loop and the telemetry broadcaster. Returns immediately. */
	public static void launch() {
		Thread dial = new Thread(SiftLink::dialLoop, "sift-link");
		dial.setDaemon(true);
		dial.start();
		Thread broadcast = new Thread(SiftLink::broadcastLoop, "sift-telemetry");
		broadcast.setDaemon(true);
		broadcast.start();
		SiftBridgeMod.LOG.info("SIFT Bridge dialling ws://{}:{} (guest mode)", HOST, PORT);
	}

	/** ESC / F8 kill switch: say goodbye, drop the socket, release the frame ring. */
	public static void shutdown() {
		if (!running) {
			return;
		}
		running = false;
		try {
			WebSocketClient c = conn;
			if (c != null && c.isOpen()) {
				c.send("{\"t\":\"bye\"}");
				c.closeBlocking();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		FrameExporter.release();
		SiftBridgeMod.LOG.info("SIFT Bridge shut down cleanly");
	}

	private static void dialLoop() {
		while (running) {
			CountDownLatch closed = new CountDownLatch(1);
			try {
				WebSocketClient c = new WebSocketClient(URI.create("ws://" + HOST + ":" + PORT)) {
					@Override
					public void onOpen(final ServerHandshake handshake) {
						conn = this;
						connected = true;
						send(String.format(Locale.ROOT, "{\"t\":\"hello\",\"v\":1,\"shm\":\"%s\",\"pid\":%d,\"mc\":\"26.3\"}",
								FrameExporter.mappingName().replace("\\", "\\\\"), ProcessHandle.current().pid()));
						SiftBridgeMod.LOG.info("SIFT Bridge: host connected");
					}

					@Override
					public void onMessage(final String message) {
						try {
							JsonObject m = JsonParser.parseString(message).getAsJsonObject();
							switch (m.get("t").getAsString()) {
								case "cam" -> HostState.accept(m);
								case "mode" -> HostState.mode(m.get("m").getAsString());
								case "key" -> {
									JsonObject copy = m;
									Minecraft.getInstance().execute(() -> ClientInput.key(copy));
								}
								case "slot", "scroll", "hud" -> {
									JsonObject copy = m;
									Minecraft.getInstance().execute(() -> ClientInput.misc(copy));
								}
								default -> SiftBridgeMod.LOG.debug("ignored host message {}", message);
							}
						} catch (RuntimeException e) {
							SiftBridgeMod.LOG.warn("bad host message: {}", e.toString());
						}
					}

					@Override
					public void onClose(final int code, final String reason, final boolean remote) {
						connected = false;
						HostState.clear();
						closed.countDown();
					}

					@Override
					public void onError(final Exception e) {
						SiftBridgeMod.LOG.debug("link error: {}", e.toString());
					}
				};
				c.setConnectionLostTimeout(5);
				c.connectBlocking(4, TimeUnit.SECONDS);
				closed.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			} catch (RuntimeException e) {
				SiftBridgeMod.LOG.debug("dial failed: {}", e.toString());
			}
			try {
				Thread.sleep(RECONNECT_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	/** The 60 Hz camera broadcast — off the render thread, so the render tick never waits on us. */
	private static void broadcastLoop() {
		StringBuilder sb = new StringBuilder(192);
		while (running) {
			try {
				Thread.sleep(CAM_PERIOD_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			WebSocketClient c = conn;
			if (c == null || !c.isOpen()) {
				continue;
			}
			Telemetry.Pose p = Telemetry.latest();
			if (p == null || p.frame() == frameCursor) {
				continue;                       // window unfocused / no new frame
			}
			frameCursor = p.frame();
			sb.setLength(0);
			sb.append("{\"t\":\"cam\",\"f\":").append(p.frame())
				.append(",\"p\":[").append(fmt(p.x())).append(',').append(fmt(p.y())).append(',').append(fmt(p.z()))
				.append("],\"r\":[").append(fmt(p.yaw())).append(',').append(fmt(p.pitch())).append(',').append(fmt(p.roll()))
				.append("],\"fov\":").append(fmt(p.fov()))
				.append(",\"fp\":").append(p.firstPerson())
				.append(",\"ts\":").append(p.ts()).append('}');
			c.send(sb.toString());
		}
	}

	private static String fmt(final double v) {
		return String.format(Locale.ROOT, "%.4f", v);
	}

	/** Tick-rate pose/event messages (called on the client thread, not the render thread). */
	public static void sendEvent(final String json) {
		WebSocketClient c = conn;
		if (c != null && c.isOpen()) {
			c.send(json);
		}
	}
}
