package net.mcsm.extras.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;

/**
 * BUILD #424 -- THE KEYBOARD, READ DIRECTLY.
 *
 * THE REPORT. "The password section in all the other sections you made where I
 * cannot click enter, there's no enter button... the keys do not work or
 * function." Both are true of the terminal as it was: the login screen had no
 * button at all (the chip row was only drawn after the code was accepted), and
 * the only way to submit was the ENTER key reaching the screen, which this
 * overlay never handled.
 *
 * WHAT THIS IS. The terminal screen's own keyboard, polled per frame straight
 * off the window with the exact reflection the Shift+C quick-panel mixin already
 * uses in this build (Minecraft.getWindow() -> Window.getWindow() -> GLFW), so
 * it does not depend on any screen-level key hook this game version may or may
 * not route. If GLFW cannot be reached, everything here reports "nothing
 * pressed" and the on-screen buttons remain the whole story.
 *
 * It reports EDGES: a key is delivered once, on the frame it goes down.
 */
public final class McsmKeyboard {

    // GLFW key codes (fixed by the GLFW spec, so they need no lookup).
    public static final int ENTER = 257;
    public static final int ESCAPE = 256;
    public static final int BACKSPACE = 259;
    public static final int SPACE = 32;
    public static final int MINUS = 45;
    public static final int H = 72;
    public static final int C = 67;
    public static final int B = 66;
    public static final int W = 87;
    public static final int S = 83;

    private McsmKeyboard() {
    }

    private static boolean resolved;
    private static boolean usable;
    private static long handle;
    private static Method getKey;
    private static int press;

    private static final Set<Integer> HELD = new HashSet<>();
    private static final List<Integer> EDGES = new ArrayList<>();

    /** The keys the terminal cares about; polling a fixed set keeps this cheap. */
    private static final int[] WATCHED = buildWatched();

    private static int[] buildWatched() {
        int[] keys = new int[40];
        int n = 0;
        for (int c = 65; c <= 90; c++) {          // A-Z
            keys[n++] = c;
        }
        for (int d = 48; d <= 57; d++) {          // 0-9
            keys[n++] = d;
        }
        keys[n++] = SPACE;
        keys[n++] = MINUS;
        keys[n++] = ENTER;
        keys[n++] = ESCAPE;
        keys[n++] = BACKSPACE;
        return keys;
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            Minecraft mc = Minecraft.getInstance();
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
            handle = h;
            getKey = glfw.getMethod("glfwGetKey", long.class, int.class);
            Field f = glfw.getField("GLFW_PRESS");
            press = ((Number) f.get(null)).intValue();
            usable = true;
        } catch (Throwable ignored) {
            usable = false;
        }
    }

    public static boolean usable() {
        resolve();
        return usable;
    }

    /**
     * One frame's worth of newly pressed keys. Call once per rendered frame; the
     * list is rebuilt by the call.
     */
    public static List<Integer> poll() {
        EDGES.clear();
        resolve();
        if (!usable) {
            return EDGES;
        }
        try {
            Set<Integer> now = new HashSet<>();
            for (int key : WATCHED) {
                int state = ((Number) getKey.invoke(null, handle, key)).intValue();
                if (state == press) {
                    now.add(Integer.valueOf(key));
                    if (!HELD.contains(Integer.valueOf(key))) {
                        EDGES.add(Integer.valueOf(key));
                    }
                }
            }
            HELD.clear();
            HELD.addAll(now);
        } catch (Throwable t) {
            usable = false;   // the window went away: stop asking
        }
        return EDGES;
    }

    /** The character a key stands for, or 0 for keys that are not text. */
    public static char textOf(int key) {
        if (key >= 65 && key <= 90) {
            return (char) key;
        }
        if (key >= 48 && key <= 57) {
            return (char) key;
        }
        if (key == SPACE) {
            return ' ';
        }
        if (key == MINUS) {
            return '-';
        }
        return 0;
    }
}
