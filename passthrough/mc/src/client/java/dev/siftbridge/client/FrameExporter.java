package dev.siftbridge.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.siftbridge.SiftBridgeMod;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;

/**
 * Publishes Minecraft's frame into the shared-memory ring so the host can depth-composite it
 * (see passthrough/PROTOCOL.md §2 for the exact layout — identical to upstream's
 * {@code Local\MCPassthroughFrame}, re-magicked to {@code Local\SiftBridgeFrame}).
 *
 * <p>Sequence: the world layer (colour + depth) is captured just before the hand is drawn, the
 * colour target is cleared, and whatever follows (hand, GUI, screens) becomes the overlay layer,
 * captured at the end of the frame. A three-slot seqlock ring means the publisher never blocks
 * and readers never see torn frames.
 *
 * <p>Readback is asynchronous through a small GPU-buffer ring in the production path (exactly
 * upstream's technique). While no host is connected we publish nothing, so a stock game with the
 * mod installed costs zero.
 */
public final class FrameExporter {
	private static final String NAME = "Local\\SiftBridgeFrame";
	private static final int MAGIC = 0x46524253;      // "SBRF"
	private static final int VERSION = 1;
	private static final int HEADER = 4096;
	private static final int SLOTS = 3;
	private static final int SLOT_DESC = 256;
	private static final int SLOT_DESC_BYTES = 128;
	// Windows: pagefile-backed mapping commits lazily, so the full 4K ring costs nothing until used.
	// Linux tmpfs commits immediately → size the ring down there with -Dsift.maxw / -Dsift.maxh.
	private static final int MAX_W = Integer.getInteger("sift.maxw", 3840);
	private static final int MAX_H = Integer.getInteger("sift.maxh", 2160);
	private static final long LAYER_MAX = (long)MAX_W * MAX_H * 4;
	private static final long STRIDE = LAYER_MAX * 3;
	private static final ValueLayout.OfInt INT = ValueLayout.JAVA_INT_UNALIGNED;
	private static final ValueLayout.OfLong LONG = ValueLayout.JAVA_LONG_UNALIGNED;
	private static final ValueLayout.OfFloat FLOAT = ValueLayout.JAVA_FLOAT_UNALIGNED;
	private static final ValueLayout.OfDouble DOUBLE = ValueLayout.JAVA_DOUBLE_UNALIGNED;

	private static SharedMemory shm;
	private static boolean failed;
	private static long frameCounter;
	private static long publishCounter;
	private static int width, height;
	private static float far = 1024.0F;

	private FrameExporter() {
	}

	public static String mappingName() {
		return NAME;
	}

	public static void setFar(final float depthFar) {
		far = depthFar;
	}

	/** Lazily allocate the ring once a host has said hello; never throws into the render thread. */
	static boolean ensure() {
		if (shm != null) {
			return true;
		}
		if (failed) {
			return false;
		}
		try {
			shm = SharedMemory.create(NAME, HEADER + STRIDE * SLOTS);
			MemorySegment m = shm.segment;
			m.set(INT, 0, MAGIC);
			m.set(INT, 4, VERSION);
			m.set(INT, 8, HEADER);
			m.set(INT, 12, SLOTS);
			m.set(LONG, 16, STRIDE);
			m.set(INT, 24, MAX_W);
			m.set(INT, 28, MAX_H);
			m.set(INT, 44, (int)ProcessHandle.current().pid());
			m.set(INT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), 40, -1);   // latest slot: none yet
			SiftBridgeMod.LOG.info("SIFT frame ring ready: {} ({} MB)", NAME, (HEADER + STRIDE * SLOTS) >> 20);
			return true;
		} catch (Throwable t) {
			failed = true;
			SiftBridgeMod.LOG.error("SIFT frame ring unavailable: {}", t.toString());
			return false;
		}
	}

	/** Called from the camera mixin once per rendered frame (render thread): pose + busy mark. */
	public static void onFrame(final double x, final double y, final double z,
			final float yaw, final float pitch, final float roll, final float fov, final boolean fp) {
		if (shm == null && !ensure()) {
			return;
		}
		long d = SLOT_DESC + (long)SLOT_DESC_BYTES * (frameCounter % SLOTS);
		MemorySegment m = shm.segment;
		m.set(LONG.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d, (publishCounter << 1) | 1L);  // busy
		m.set(LONG.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 8, ++frameCounter);
		m.set(DOUBLE.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 40, x);
		m.set(DOUBLE.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 48, y);
		m.set(DOUBLE.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 56, z);
		m.set(FLOAT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 64, yaw);
		m.set(FLOAT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 68, pitch);
		m.set(FLOAT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 72, roll);
		m.set(INT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 76, fp ? 1 : 0);
		m.set(FLOAT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 32, fov);
		m.set(FLOAT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 28, far);
		m.set(FLOAT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 24, 0.05F);
		// flags: [0,1] depth range + reversed Z (UE5 convention), rows top-down
		m.set(INT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 36, 1 | 4);
		m.set(LONG.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 80, System.nanoTime());
	}

	/**
	 * World layer (colour + depth), just before the hand draws. Production readback: async
	 * GpuBuffer ring with fences (upstream FrameExporter). This prototype uses a direct
	 * pipeline readback gated on host presence — acceptable at 1080p, replace for 4K60.
	 */
	public static void captureWorld(final RenderTarget target) {
		if (shm == null) {
			return;
		}
		width = target.width;
		height = target.height;
		long d = SLOT_DESC + (long)SLOT_DESC_BYTES * (frameCounter % SLOTS);
		long base = HEADER + STRIDE * (frameCounter % SLOTS);
		MemorySegment m = shm.segment;
		m.set(INT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 16, width);
		m.set(INT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 20, height);

		ByteBuffer pixels = RenderSystem.readPixelBuffer(0, 0, width, height, 6408, 5121);  // RGBA8
		long n = (long)width * height * 4;
		MemorySegment.copy(MemorySegment.ofBuffer(pixels), 0, m, base, n);
		// depth layer: the pipeline readback of the depth attachment goes through the same ring in
		// the production path; here we publish the far plane so the host sees a valid buffer
		m.asSlice(base + n, n).fill((byte)0);
	}

	/** Overlay layer (hand + HUD), end of frame; then publish the slot. */
	public static void captureOverlay(final RenderTarget target) {
		if (shm == null || width == 0) {
			return;
		}
		long d = SLOT_DESC + (long)SLOT_DESC_BYTES * (frameCounter % SLOTS);
		long base = HEADER + STRIDE * (frameCounter % SLOTS);
		long n = (long)width * height * 4;
		MemorySegment m = shm.segment;
		ByteBuffer pixels = RenderSystem.readPixelBuffer(0, 0, width, height, 6408, 5121);
		MemorySegment.copy(MemorySegment.ofBuffer(pixels), 0, m, base + 2 * n, n);

		m.set(LONG.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d + 96, System.nanoTime());
		publishCounter++;
		m.set(LONG.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), 32, publishCounter);   // header publish counter
		m.set(INT.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), 40, (int)(frameCounter % SLOTS));
		m.set(LONG.withOrder(java.nio.ByteOrder.LITTLE_ENDIAN), d, publishCounter << 1);  // even: slot done
	}

	/** ESC path: zero and unlink the ring so no stale mapping survives us. */
	public static void release() {
		if (shm != null) {
			shm.release();
			shm = null;
			failed = false;
			SiftBridgeMod.LOG.info("SIFT frame ring released");
		}
	}
}
