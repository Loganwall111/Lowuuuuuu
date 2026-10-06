package com.sift.passthrough;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * SIFT Overhaul x Minecraft Dungeons Cross-Game Data Streamer.
 *
 * <p>Operates strictly as an external communication wrapper over local loopback
 * ({@code 127.0.0.1:8080}) and temporary shared memory ({@code /dev/shm/SiftPassthroughFrame}
 * / {@code Local\MCPassthroughFrame}), with a clean {@code GLFW_KEY_ESCAPE (256)}
 * thread kill-switch that closes the socket without stalling the render thread.
 */
public final class SiftDataStreamer {

    public static final String BRIDGE_HOST = System.getProperty("sift.passthrough.host", "127.0.0.1");
    public static final int BRIDGE_PORT = Integer.getInteger("sift.passthrough.port", 8080);
    public static final int GLFW_KEY_ESCAPE = 256;
    public static final int GLFW_KEY_F8 = 297;
    public static final int MCPT_MAGIC = 0x5450434D;
    public static final int MCPT_HEADER_SIZE = 4096;

    private static final AtomicReference<String> LATEST_PACKET = new AtomicReference<>();
    private static final AtomicBoolean STREAMER_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean KILL_SWITCH_TRIPPED = new AtomicBoolean(false);
    private static final AtomicBoolean ESCAPE_LATCH = new AtomicBoolean(false);
    private static final AtomicBoolean F8_LATCH = new AtomicBoolean(false);
    private static final AtomicLong FRAME_SEQ = new AtomicLong(0L);

    private static volatile Thread streamerThread;
    private static volatile Socket activeSocket;
    private static volatile long lastFrameNs = System.nanoTime();

    private static boolean glfwResolved;
    private static boolean glfwUsable;
    private static long glfwWindowHandle;
    private static Method glfwGetKeyMethod;
    private static int glfwPressValue = 1;

    private static MappedByteBuffer shmBuffer;
    private static long lastShmAttemptMs;
    private static volatile boolean ueConnected;
    private static volatile long ueSeq;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            triggerEscapeKillSwitch("JVM_SHUTDOWN");
        }, "Sift-Passthrough-ShutdownHook"));
    }

    private SiftDataStreamer() {
    }

    public static void tick() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                return;
            }
            pollEscapeAndRearmKeys(mc);
            pollSharedMemoryHeader();
            if (!KILL_SWITCH_TRIPPED.get() && !STREAMER_RUNNING.get()) {
                startStreamerDaemon();
            }
        } catch (Throwable ignored) {
        }
    }

    public static void onFrame(CameraRenderState camera, boolean skyVisible) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || camera == null || camera.pos == null) {
                return;
            }
            pollEscapeAndRearmKeys(mc);
            if (KILL_SWITCH_TRIPPED.get()) {
                return;
            }
            if (!STREAMER_RUNNING.get()) {
                startStreamerDaemon();
            }

            long nowNs = System.nanoTime();
            long prevNs = lastFrameNs;
            lastFrameNs = nowNs;
            double dt = (nowNs - prevNs) * 1.0e-9D;
            if (dt <= 0.0D || dt > 1.0D) {
                dt = 1.0D / 60.0D;
            }

            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float pitch = mc.player != null ? mc.player.getViewXRot(partial) : 0.0F;
            float yaw = mc.player != null ? mc.player.getViewYRot(partial) : 0.0F;
            float fov = 70.0F;
            long seq = FRAME_SEQ.incrementAndGet();

            Vec3 pos = camera.pos;
            String packet = String.format(
                    Locale.ROOT,
                    "{\"type\":\"sift_cam\",\"seq\":%d,\"x\":%.5f,\"y\":%.5f,\"z\":%.5f,"
                    + "\"pitch\":%.4f,\"yaw\":%.4f,\"roll\":0.0000,\"fov\":%.2f,"
                    + "\"sky_visible\":%b,\"dt\":%.6f,\"ts_ns\":%d}\n",
                    seq, pos.x, pos.y, pos.z, pitch, yaw, fov, skyVisible, dt, nowNs
            );

            LATEST_PACKET.set(packet);
            synchronized (LATEST_PACKET) {
                LATEST_PACKET.notifyAll();
            }
        } catch (Throwable ignored) {
        }
    }

    private static void resolveGlfw(Minecraft mc) {
        if (glfwResolved) {
            return;
        }
        glfwResolved = true;
        try {
            Object windowObj = null;
            for (Method m : Minecraft.class.getMethods()) {
                if (m.getParameterCount() == 0 && m.getName().equals("getWindow")) {
                    windowObj = m.invoke(mc);
                    break;
                }
            }
            if (windowObj == null) {
                return;
            }
            long h = 0L;
            for (Method m : windowObj.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && m.getName().equals("getWindow")) {
                    Object v = m.invoke(windowObj);
                    if (v instanceof Number) {
                        h = ((Number) v).longValue();
                    }
                    break;
                }
            }
            if (h == 0L) {
                return;
            }
            Class<?> glfw = Class.forName("org.lwjgl.glfw.GLFW");
            glfwWindowHandle = h;
            glfwGetKeyMethod = glfw.getMethod("glfwGetKey", long.class, int.class);
            Field f = glfw.getField("GLFW_PRESS");
            glfwPressValue = ((Number) f.get(null)).intValue();
            glfwUsable = true;
        } catch (Throwable ignored) {
            glfwUsable = false;
        }
    }

    private static boolean isKeyDown(Minecraft mc, int glfwKey) {
        resolveGlfw(mc);
        if (!glfwUsable || glfwGetKeyMethod == null || glfwWindowHandle == 0L) {
            return false;
        }
        try {
            int state = ((Number) glfwGetKeyMethod.invoke(null, glfwWindowHandle, glfwKey)).intValue();
            return state == glfwPressValue;
        } catch (Throwable t) {
            glfwUsable = false;
            return false;
        }
    }

    private static void pollEscapeAndRearmKeys(Minecraft mc) {
        boolean escDown = isKeyDown(mc, GLFW_KEY_ESCAPE);
        if (escDown) {
            if (ESCAPE_LATCH.compareAndSet(false, true)) {
                triggerEscapeKillSwitch("GLFW_KEY_ESCAPE");
            }
        } else {
            ESCAPE_LATCH.set(false);
        }

        boolean f8Down = isKeyDown(mc, GLFW_KEY_F8);
        if (f8Down) {
            if (F8_LATCH.compareAndSet(false, true) && KILL_SWITCH_TRIPPED.compareAndSet(true, false)) {
                startStreamerDaemon();
            }
        } else {
            F8_LATCH.set(false);
        }
    }

    public static void triggerEscapeKillSwitch(String reason) {
        KILL_SWITCH_TRIPPED.set(true);
        STREAMER_RUNNING.set(false);
        LATEST_PACKET.set(null);

        synchronized (LATEST_PACKET) {
            LATEST_PACKET.notifyAll();
        }

        Thread t = streamerThread;
        if (t != null) {
            t.interrupt();
        }

        Socket sock = activeSocket;
        activeSocket = null;
        if (sock != null) {
            try {
                if (!sock.isClosed() && sock.isConnected()) {
                    OutputStream out = sock.getOutputStream();
                    String killMsg = String.format(
                            Locale.ROOT,
                            "{\"type\":\"kill\",\"reason\":\"%s\",\"code\":%d}\n",
                            reason, GLFW_KEY_ESCAPE
                    );
                    out.write(killMsg.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
            } catch (Throwable ignored) {
            } finally {
                try {
                    sock.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private static synchronized void startStreamerDaemon() {
        if (KILL_SWITCH_TRIPPED.get() || STREAMER_RUNNING.get()) {
            return;
        }
        STREAMER_RUNNING.set(true);
        Thread worker = new Thread(SiftDataStreamer::runStreamerLoop, "Sift-DataStreamer-8080");
        worker.setDaemon(true);
        streamerThread = worker;
        worker.start();
    }

    private static void runStreamerLoop() {
        while (STREAMER_RUNNING.get() && !KILL_SWITCH_TRIPPED.get() && !Thread.currentThread().isInterrupted()) {
            Socket sock = null;
            try {
                sock = new Socket();
                sock.setTcpNoDelay(true);
                sock.setKeepAlive(true);
                sock.connect(new InetSocketAddress(BRIDGE_HOST, BRIDGE_PORT), 750);
                activeSocket = sock;

                OutputStream out = new BufferedOutputStream(sock.getOutputStream(), 8192);
                byte[] hello = "{\"type\":\"hello\",\"source\":\"mc-sift-passthrough\",\"version\":2,\"port\":8080}\n"
                        .getBytes(StandardCharsets.UTF_8);
                out.write(hello);
                out.flush();

                while (STREAMER_RUNNING.get() && !KILL_SWITCH_TRIPPED.get() && !Thread.currentThread().isInterrupted()) {
                    String packet = LATEST_PACKET.getAndSet(null);
                    if (packet == null) {
                        synchronized (LATEST_PACKET) {
                            if (LATEST_PACKET.get() == null && STREAMER_RUNNING.get() && !KILL_SWITCH_TRIPPED.get()) {
                                LATEST_PACKET.wait(16L);
                            }
                        }
                        packet = LATEST_PACKET.getAndSet(null);
                    }
                    if (packet != null) {
                        out.write(packet.getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    }
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable ignored) {
                if (KILL_SWITCH_TRIPPED.get() || !STREAMER_RUNNING.get()) {
                    break;
                }
                try {
                    Thread.sleep(750L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } finally {
                activeSocket = null;
                if (sock != null) {
                    try {
                        sock.close();
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
        STREAMER_RUNNING.set(false);
    }

    private static void pollSharedMemoryHeader() {
        try {
            if (shmBuffer == null) {
                long now = System.currentTimeMillis();
                if (now - lastShmAttemptMs < 2000L) {
                    return;
                }
                lastShmAttemptMs = now;
                File shmFile = new File("/dev/shm/SiftPassthroughFrame");
                if (!shmFile.exists()) {
                    shmFile = new File(System.getProperty("java.io.tmpdir", "/tmp"), "SiftPassthroughFrame");
                }
                if (!shmFile.exists() || shmFile.length() < MCPT_HEADER_SIZE) {
                    return;
                }
                try (RandomAccessFile raf = new RandomAccessFile(shmFile, "r");
                     FileChannel ch = raf.getChannel()) {
                    MappedByteBuffer mapped = ch.map(FileChannel.MapMode.READ_ONLY, 0L, MCPT_HEADER_SIZE);
                    mapped.order(ByteOrder.LITTLE_ENDIAN);
                    shmBuffer = mapped;
                }
            }

            ByteBuffer buf = shmBuffer;
            if (buf == null) {
                return;
            }
            int magic = buf.getInt(0);
            int version = buf.getInt(4);
            if (magic != MCPT_MAGIC || version < 1) {
                ueConnected = false;
                return;
            }

            long seq0 = buf.getLong(8);
            if ((seq0 & 1L) != 0L) {
                return;
            }
            long seq1 = buf.getLong(8);
            if (seq0 == seq1 && seq0 > 0L) {
                ueSeq = seq0;
                ueConnected = true;
            }
        } catch (Throwable ignored) {
            ueConnected = false;
        }
    }

    public static boolean isUeConnected() {
        return ueConnected;
    }

    public static long getLastUeSeq() {
        return ueSeq;
    }
}
