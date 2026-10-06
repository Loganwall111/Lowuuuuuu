package net.mcsm.extras.client;

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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmStormPhase;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.McsmVoidTiers;

/**
 * BUILD #497 / 7000.0.27-M -- SIFT Overhaul x Minecraft Dungeons Cross-Game Passthrough Bridge.
 *
 * <p>Implements the dual-plane cross-engine architecture reverse-engineered from
 * {@code universal-modder/examples/minecraft-gta5-passthrough} and {@code SkyCraft}:
 * <ul>
 *   <li><b>Control &amp; Telemetry Plane (127.0.0.1:8080, TCP_NODELAY)</b>: Streams real-time
 *       Camera X, Y, Z, Pitch, Yaw, Roll, FOV, WitherStormPhase, VoidLayer, and DeltaTime
 *       from a non-blocking daemon thread using a lock-free {@link AtomicReference} ring.</li>
 *   <li><b>Escape-Key Kill Switch (GLFW_KEY_ESCAPE = 256)</b>: Immediately halts the
 *       daemon socket streamer and sends a clean {@code "kill"} packet when Escape is pressed,
 *       preventing orphaned sockets or render-thread stalls. Can be re-armed via F8 (297).</li>
 *   <li><b>Frame &amp; Linear-Depth Seqlock Plane ("MCPT" 0x5450434D)</b>: Reads the 3-slot
 *       seqlocked shared-memory header published by {@code UEDungeonsPassthroughHook}
 *       ({@code /dev/shm/SiftPassthroughFrame} or {@code %TEMP%/SiftPassthroughFrame}) to
 *       synchronize the in-world 3D Dungeons Obsidian Pinnacle Rift &amp; Sky Window.</li>
 * </ul>
 */
public final class McsmDungeonsPassthrough {

    public static final String BRIDGE_HOST = System.getProperty("sift.passthrough.host", "127.0.0.1");
    public static final int BRIDGE_PORT = Integer.getInteger("sift.passthrough.port", 8080);
    public static final int GLFW_KEY_ESCAPE = 256;
    public static final int GLFW_KEY_F8 = 297;
    public static final int MCPT_MAGIC = 0x5450434D; // "MCPT" little-endian
    public static final int MCPT_HEADER_SIZE = 4096;

    private static final Identifier VOID_STONE = Identifier.fromNamespaceAndPath("mcsm", "textures/block/void_stone.png");
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/storm_white.png");

    private static final AtomicReference<String> LATEST_PACKET = new AtomicReference<>();
    private static final AtomicBoolean STREAMER_RUNNING = new AtomicBoolean(false);
    private static final AtomicBoolean KILL_SWITCH_TRIPPED = new AtomicBoolean(false);
    private static final AtomicBoolean ESCAPE_LATCH = new AtomicBoolean(false);
    private static final AtomicBoolean F8_LATCH = new AtomicBoolean(false);
    private static final AtomicLong FRAME_SEQ = new AtomicLong(0L);

    private static volatile Thread streamerThread;
    private static volatile Socket activeSocket;
    private static volatile long lastFrameNs = System.nanoTime();

    // Cached GLFW reflection handles (matches McsmKeyboard pattern for stripped javac CP)
    private static boolean glfwResolved;
    private static boolean glfwUsable;
    private static long glfwWindowHandle;
    private static Method glfwGetKeyMethod;
    private static int glfwPressValue = 1;

    // Live seqlock telemetry read back from Unreal Engine / Minecraft Dungeons
    private static MappedByteBuffer shmBuffer;
    private static long lastShmAttemptMs;
    private static volatile boolean ueConnected;
    private static volatile float ueCamX;
    private static volatile float ueCamY;
    private static volatile float ueCamZ;
    private static volatile float uePitch;
    private static volatile float ueYaw;
    private static volatile float ueFov = 70.0F;
    private static volatile long ueSeq;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            triggerEscapeKillSwitch("JVM_SHUTDOWN");
        }, "Sift-Passthrough-ShutdownHook"));
    }

    private McsmDungeonsPassthrough() {
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

    public static void onFrame(CameraRenderState camera) {
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
            double phase = mc.level != null ? McsmStormPhase.effectivePhase(mc.level) : 0.0D;
            int voidLayer = McsmInfiniteVoidLayers.getCurrentMeditationLayer();
            long seq = FRAME_SEQ.incrementAndGet();

            Vec3 pos = camera.pos;
            String packet = String.format(
                    Locale.ROOT,
                    "{\"type\":\"sift_cam\",\"seq\":%d,\"x\":%.5f,\"y\":%.5f,\"z\":%.5f,"
                    + "\"pitch\":%.4f,\"yaw\":%.4f,\"roll\":0.0000,\"fov\":%.2f,"
                    + "\"storm_phase\":%.3f,\"void_layer\":%d,\"dt\":%.6f,\"ts_ns\":%d}\n",
                    seq, pos.x, pos.y, pos.z, pitch, yaw, fov, phase, voidLayer, dt, nowNs
            );

            LATEST_PACKET.set(packet);
            synchronized (LATEST_PACKET) {
                LATEST_PACKET.notifyAll();
            }
        } catch (Throwable ignored) {
        }
    }

    public static void submit(LevelRenderContext ctx) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || ctx == null) {
                return;
            }
            CameraRenderState camState = ctx.levelState().cameraRenderState;
            onFrame(camState);

            if (!McsmExtrasConfig.riftEvents && !McsmExtrasConfig.voidDescent) {
                return;
            }

            boolean inVoid = mc.level.dimension().equals(McsmVoid.DIMENSION);
            double phase = McsmStormPhase.effectivePhase(mc.level);
            if (!inVoid && phase < 4.0D && !ueConnected) {
                return;
            }

            Vec3 cam = camState.pos;
            PoseStack poseStack = ctx.poseStack();
            SubmitNodeCollector collector = ctx.submitNodeCollector();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            double time = (mc.level.getGameTime() % 240000L) + partial;

            final Vec3 camFinal = cam;
            final double tFinal = time;
            final boolean liveLink = ueConnected;
            final float linkYaw = ueYaw;
            final float linkPitch = uePitch;

            collector.submitCustomGeometry(
                    poseStack,
                    net.dabicco.witherstormmod.client.GlowRenderTypes.glow(WHITE),
                    (pose, consumer) -> emitDungeonsPassthroughRift(
                            pose, consumer, camFinal, tFinal, liveLink, linkYaw, linkPitch
                    )
            );
            collector.submitCustomGeometry(
                    poseStack,
                    net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(VOID_STONE),
                    (pose, consumer) -> emitObsidianPinnacleSilhouette(
                            pose, consumer, camFinal, tFinal, liveLink
                    )
            );
        } catch (Throwable ignored) {
        }
    }

    private static void emitDungeonsPassthroughRift(
            Pose pose,
            VertexConsumer consumer,
            Vec3 cam,
            double time,
            boolean liveLink,
            float linkYaw,
            float linkPitch
    ) {
        double orbitAngle = time * 0.008D + Math.toRadians(linkYaw * 0.15D);
        double dist = 160.0D;
        double cx = Math.cos(orbitAngle) * dist;
        double cz = Math.sin(orbitAngle) * dist;
        double cy = 42.0D + Math.sin(time * 0.03D + Math.toRadians(linkPitch * 0.2D)) * 8.0D;

        int segments = 24;
        double radiusX = 18.0D;
        double radiusY = 34.0D;

        float rCore = liveLink ? 0.25F : 0.68F;
        float gCore = liveLink ? 0.92F : 0.18F;
        float bCore = liveLink ? 1.00F : 0.95F;

        for (int i = 0; i < segments; i++) {
            double a0 = (i / (double) segments) * Math.PI * 2.0D;
            double a1 = ((i + 1) / (double) segments) * Math.PI * 2.0D;
            double wobble0 = 1.0D + 0.12D * Math.sin(a0 * 5.0D + time * 0.15D);
            double wobble1 = 1.0D + 0.12D * Math.sin(a1 * 5.0D + time * 0.15D);

            float x0 = (float) (cx + Math.cos(a0) * radiusX * wobble0);
            float y0 = (float) (cy + Math.sin(a0) * radiusY * wobble0);
            float z0 = (float) (cz - Math.sin(a0) * radiusX * 0.5D);

            float x1 = (float) (cx + Math.cos(a1) * radiusX * wobble1);
            float y1 = (float) (cy + Math.sin(a1) * radiusY * wobble1);
            float z1 = (float) (cz - Math.sin(a1) * radiusX * 0.5D);

            float xc = (float) cx;
            float yc = (float) cy;
            float zc = (float) cz;

            emitQuad(pose, consumer,
                    xc, yc, zc,
                    x0, y0, z0,
                    x1, y1, z1,
                    xc, yc, zc,
                    rCore, gCore, bCore, 0.42F);
        }
    }

    private static void emitObsidianPinnacleSilhouette(
            Pose pose,
            VertexConsumer consumer,
            Vec3 cam,
            double time,
            boolean liveLink
    ) {
        int spireCount = 8;
        double ringRadius = 175.0D;
        for (int i = 0; i < spireCount; i++) {
            double baseAngle = (i / (double) spireCount) * Math.PI * 2.0D + time * 0.002D;
            double sx = Math.cos(baseAngle) * ringRadius;
            double sz = Math.sin(baseAngle) * ringRadius;
            double baseH = 14.0D + (i % 3) * 8.0D;
            double topH = baseH + 38.0D + (i % 4) * 12.0D;
            float w = 4.5F;

            float r = liveLink ? 0.16F : 0.12F;
            float g = liveLink ? 0.22F : 0.06F;
            float b = liveLink ? 0.34F : 0.20F;

            emitQuad(pose, consumer,
                    (float) sx - w, (float) baseH, (float) sz - w,
                    (float) sx + w, (float) baseH, (float) sz - w,
                    (float) sx + w * 0.25F, (float) topH, (float) sz,
                    (float) sx - w * 0.25F, (float) topH, (float) sz,
                    r, g, b, 0.65F);
        }
    }

    private static void emitQuad(
            Pose pose,
            VertexConsumer consumer,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float r, float g, float b, float a
    ) {
        int light = 0xF000F0;
        consumer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
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
        Thread worker = new Thread(McsmDungeonsPassthrough::runStreamerLoop, "Sift-DataStreamer-8080");
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

            // 3-slot lock-free seqlock verification (matches universal-modder & UEDungeonsPassthroughHook)
            long seq0 = buf.getLong(8);
            if ((seq0 & 1L) != 0L) {
                return; // writer in progress
            }
            float cx = buf.getFloat(32);
            float cy = buf.getFloat(36);
            float cz = buf.getFloat(40);
            float pitch = buf.getFloat(44);
            float yaw = buf.getFloat(48);
            float fov = buf.getFloat(56);
            long seq1 = buf.getLong(8);
            if (seq0 == seq1 && seq0 > 0L) {
                ueSeq = seq0;
                ueCamX = cx;
                ueCamY = cy;
                ueCamZ = cz;
                uePitch = Mth.clamp(pitch, -90.0F, 90.0F);
                ueYaw = yaw;
                if (fov > 10.0F && fov < 170.0F) {
                    ueFov = fov;
                }
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
