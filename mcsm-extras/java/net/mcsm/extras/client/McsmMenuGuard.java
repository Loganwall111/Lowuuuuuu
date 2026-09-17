package net.mcsm.extras.client;

/**
 * Fault-isolate menu/title chrome. If extractBackground/extractRenderState
 * throws, 26.2 draws an empty (black) frame. After a few faults we stand the
 * chrome down so vanilla paints instead.
 */
public final class McsmMenuGuard {

    public static final int FAULT_LIMIT = 3;
    public static final long WINDOW_MS = 8000L;
    public static final long COOLDOWN_MS = 30000L;

    private static long windowStart = -1L;
    private static int windowFaults = 0;
    private static long stoodDownUntil = -1L;
    private static int totalFaults = 0;
    private static String lastTag = "";
    private static String lastDetail = "";

    private McsmMenuGuard() {
    }

    public static boolean ok() {
        try {
            if (stoodDownUntil < 0L) {
                return true;
            }
            if (System.currentTimeMillis() < stoodDownUntil) {
                return false;
            }
            stoodDownUntil = -1L;
            windowStart = -1L;
            windowFaults = 0;
            return true;
        } catch (Throwable ignored) {
            return true;
        }
    }

    public static void fault(String tag, Throwable t) {
        try {
            long now = System.currentTimeMillis();
            totalFaults++;
            lastTag = tag == null ? "?" : tag;
            lastDetail = t == null ? "?" : String.valueOf(t.getClass().getSimpleName());
            if (windowStart < 0L || now - windowStart > WINDOW_MS) {
                windowStart = now;
                windowFaults = 0;
            }
            windowFaults++;
            if (windowFaults >= FAULT_LIMIT && stoodDownUntil < 0L) {
                stoodDownUntil = now + COOLDOWN_MS;
            }
        } catch (Throwable ignored) {
        }
    }

    public static String state() {
        try {
            if (totalFaults == 0) {
                return "menu render: ok (painting, no faults)";
            }
            return "menu render: " + (ok() ? "recovered" : "STOOD DOWN")
                    + " - " + totalFaults + " fault(s), last: " + lastTag + " " + lastDetail;
        } catch (Throwable ignored) {
            return "menu render: ?";
        }
    }

    public static void reset() {
        windowStart = -1L;
        windowFaults = 0;
        stoodDownUntil = -1L;
        totalFaults = 0;
        lastTag = "";
        lastDetail = "";
    }
}
