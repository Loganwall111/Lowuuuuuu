package net.mcsm.extras.client;

/**
 * BUILD #469 -- THE MENU CANNOT GO BLACK, EVEN WHEN THE MOD'S OWN CHROME BREAKS.
 *
 * <p>The report keeps coming back: "It's still black ... it's definitely a render
 * issue ... the Mojang logo loads ... and then just completely black." Every plate
 * that could paint a black frame has been repainted (see {@link McsmMenuSky} and
 * the gate that scans the whole source tree for one), so what is left is the other
 * way a screen goes black: a screen-extraction hook that <b>throws</b>. In 26.2 a
 * screen's frame is built in {@code extractRenderState} / {@code extractBackground};
 * if one of the mod's injections into that path raises, the screen's whole frame
 * comes out empty -- a black window with the title bar still up, which is exactly
 * what the screenshot shows.
 *
 * <p>So the mod's menu path is fault-isolated here. Every one of our hooks either
 * runs clean or reports itself here and gets out of the way:
 *
 * <ul>
 *   <li>{@link #ok()} -- may the mod's chrome paint this frame at all? When three
 *       faults land inside {@link #WINDOW_MS}, the guard <b>stands the chrome down
 *       for {@link #COOLDOWN_MS}</b> (30 s). Crucially, standing down also means the
 *       mod stops cancelling the vanilla title background, so the player gets the
 *       plain vanilla menu instead of an empty frame.</li>
 *   <li>{@link #fault(String, Throwable)} -- record it, in {@code /ds menu} and in
 *       the game log (reflected, so this class never needs slf4j at compile time).
 *       The record is the point: a black screen nobody can explain is a black
 *       screen that survives the next three fixes.</li>
 * </ul>
 *
 * <p>The guard is deliberately tiny, allocation-free on the clean path, and never
 * throws itself -- a fault handler that faults is the one bug that would matter
 * here.
 */
public final class McsmMenuGuard {

    /** Faults inside one window before the chrome stands down. */
    public static final int FAULT_LIMIT = 3;

    /** Faults this close together are the same fault, repeating every frame. */
    public static final long WINDOW_MS = 8000L;

    /** How long the chrome stays off after tripping (vanilla menu meanwhile). */
    public static final long COOLDOWN_MS = 30000L;

    private static long windowStart = -1L;
    private static int windowFaults = 0;
    private static long stoodDownUntil = -1L;
    private static int totalFaults = 0;
    private static String lastTag = "";
    private static String lastDetail = "";
    private static long lastAtMs = 0L;
    private static boolean logged = false;

    private McsmMenuGuard() {
    }

    /**
     * True when the mod's menu chrome may paint. False while the guard is stood
     * down: the caller must then draw NOTHING and (for the title backdrop) must
     * NOT cancel the vanilla background either, so the frame is vanilla rather
     * than empty.
     */
    public static boolean ok() {
        try {
            if (stoodDownUntil < 0L) {
                return true;
            }
            if (System.currentTimeMillis() < stoodDownUntil) {
                return false;
            }
            // the cooldown expired: try the mod's look again, from a clean slate
            stoodDownUntil = -1L;
            windowStart = -1L;
            windowFaults = 0;
            return true;
        } catch (Throwable ignored) {
            return true;
        }
    }

    /** Records a fault from one of the menu-path hooks. Never throws. */
    public static void fault(String tag, Throwable t) {
        try {
            long now = System.currentTimeMillis();
            totalFaults++;
            lastTag = tag == null ? "?" : tag;
            lastDetail = describe(t);
            lastAtMs = now;
            if (windowStart < 0L || now - windowStart > WINDOW_MS) {
                windowStart = now;
                windowFaults = 0;
                logged = false;
            }
            windowFaults++;
            if (windowFaults >= FAULT_LIMIT && stoodDownUntil < 0L) {
                stoodDownUntil = now + COOLDOWN_MS;
            }
            if (!logged) {
                logged = true;
                log("[mcsm] menu render fault #" + totalFaults + " (" + lastTag + "): "
                        + lastDetail + " -- the mod's menu chrome is isolated and will "
                        + "stand down by itself; /ds menu reports it");
            }
        } catch (Throwable ignored) {
            // a fault recorder must never be the second fault
        }
    }

    /** One line for {@code /ds menu} and the config panel. */
    public static String state() {
        try {
            long now = System.currentTimeMillis();
            if (totalFaults == 0) {
                return "menu render: ok (" + (ok() ? "painting" : "stood down")
                        + ", no faults)";
            }
            StringBuilder sb = new StringBuilder("menu render: ");
            if (!ok()) {
                sb.append("STOOD DOWN for another ")
                        .append(Math.max(0L, (stoodDownUntil - now + 999L) / 1000L))
                        .append("s (vanilla menu until then) - ");
            } else {
                sb.append("recovered - ");
            }
            sb.append(totalFaults).append(" fault").append(totalFaults == 1 ? "" : "s")
                    .append(", last: ").append(lastTag).append(' ').append(lastDetail);
            return sb.toString();
        } catch (Throwable ignored) {
            return "menu render: ?";
        }
    }

    /** Faults since launch, for the gate and the panel. */
    public static int faults() {
        return totalFaults;
    }

    /** Clears the record and lets the chrome paint again at once. */
    public static void reset() {
        windowStart = -1L;
        windowFaults = 0;
        stoodDownUntil = -1L;
        totalFaults = 0;
        lastTag = "";
        lastDetail = "";
        logged = false;
    }

    /** "Class: message @ top mod frame", so a report names the line that broke. */
    private static String describe(Throwable t) {
        if (t == null) {
            return "?";
        }
        String name = t.getClass().getName();
        String msg = t.getMessage();
        StringBuilder sb = new StringBuilder(name);
        if (msg != null && !msg.isEmpty()) {
            sb.append(": ").append(msg.length() > 120 ? msg.substring(0, 120) : msg);
        }
        StackTraceElement[] st = t.getStackTrace();
        if (st != null && st.length > 0) {
            StackTraceElement pick = st[0];
            for (StackTraceElement e : st) {
                String cn = e.getClassName();
                if (cn != null && (cn.contains("mcsm") || cn.contains("dabicco"))) {
                    pick = e;
                    break;
                }
            }
            sb.append(" @ ").append(pick.getClassName()).append('.')
                    .append(pick.getMethodName()).append(':').append(pick.getLineNumber());
        }
        return sb.toString();
    }

    /**
     * One WARN into the game's own log, reflectively: this class is compiled
     * against a jar that does not carry slf4j, and a missing log line must never
     * be a compile failure.
     */
    private static void log(String line) {
        try {
            Object logger = net.dabicco.witherstormmod.DabyWitherStormMod.class
                    .getField("LOGGER").get(null);
            if (logger == null) {
                return;
            }
            logger.getClass().getMethod("warn", String.class).invoke(logger, line);
        } catch (Throwable ignored) {
            // no logger reachable: /ds menu still has the record
        }
    }
}
