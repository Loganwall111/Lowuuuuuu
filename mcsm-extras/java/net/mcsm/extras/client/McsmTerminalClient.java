package net.mcsm.extras.client;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcsm.extras.net.McsmTerminalC2S;
import net.mcsm.extras.net.McsmTerminalS2C;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;

/**
 * BUILD #416 (D.8, phase 5) -- the CLIENT half of the terminal.
 *
 * WHY THE SCREEN IS OPENED FROM A TICK AND NOT FROM THE PACKET. A packet arrives
 * on the render thread, and this Minecraft asserts when a screen is opened while
 * a screen is already being rendered -- the mod's own config payload handles this
 * by hopping through {@code context.client().execute(...)}, and this goes one
 * step further: the payload is only queued here, and the queue is drained on the
 * client tick. That is also what makes the terminal work when it is opened BY A
 * SCREEN (the field guide used in hand) rather than by an item.
 *
 * TWO ENTRY POINTS, ONE TERMINAL:
 *   * the server sends "open" (the antenna, or the C key asking for the console);
 *   * the player presses C anywhere, which asks the server to open it -- the key
 *     is bound in the controls screen ("Open story terminal", default C) so a
 *     player can rebind it, and pressing it again closes the terminal.
 */
public final class McsmTerminalClient {

    private static final List<McsmTerminalS2C> QUEUE = new ArrayList<>();
    private static boolean registered = false;

    private McsmTerminalClient() {
    }

    /** Called from the mod's client initializer (McsmStoryRendererMixin's hook). */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        try {
            ClientPlayNetworking.registerGlobalReceiver(McsmTerminalS2C.TYPE, (payload, context) ->
                    context.client().execute(() -> {
                        synchronized (QUEUE) {
                            QUEUE.add(payload);
                        }
                    }));
            // ClientTickEvents.START_CLIENT_TICK with a one-argument lambda is
            // the base mod's own registration (DabyWitherStormModClient), so the
            // event and its SAM shape are both proven.
            ClientTickEvents.START_CLIENT_TICK.register(client -> {
                drain(client);
                pollKey();
            });
            bindKey();
            System.out.println("[ds] terminal client channel up");
        } catch (Throwable t) {
            System.err.println("[ds] terminal client channel failed: " + t);
        }
    }

    // ---------------------------------------------------------------------
    // The C key
    // ---------------------------------------------------------------------
    // "The control C button doesn't really do anything": there was no binding at
    // all. A KeyMapping has to be registered through Fabric's keybinding helper,
    // which this overlay's compile classpath does not carry, so the mapping is
    // built and registered REFLECTIVELY -- constructor, helper and consumeClick
    // all by name -- inside one try/catch. If any part of that is unavailable the
    // mod simply has no key and everything else (the antenna, the guide book, the
    // server's own opens) still works exactly as before. The key is registered in
    // the controls screen under Miscellaneous, so it can be rebound.

    private static Object keyMapping;

    private static void bindKey() {
        try {
            Class<?> mapping = Class.forName("net.minecraft.client.KeyMapping");
            Class<?> helper = Class.forName(
                    "net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
            java.lang.reflect.Constructor<?> ctor = mapping.getConstructor(
                    String.class, int.class, String.class);
            Object key = ctor.newInstance("key.mcsm.terminal", 67, "key.categories.misc");
            Object registered = helper.getMethod("registerKeyBinding", mapping)
                    .invoke(null, key);
            keyMapping = registered == null ? key : registered;
        } catch (Throwable t) {
            keyMapping = null;
        }
    }

    private static void pollKey() {
        if (keyMapping == null) {
            return;
        }
        try {
            java.lang.reflect.Method consume = keyMapping.getClass()
                    .getMethod("consumeClick");
            boolean clicked = false;
            while (Boolean.TRUE.equals(consume.invoke(keyMapping))) {
                clicked = true;
            }
            if (clicked) {
                onKeyPressed();
            }
        } catch (Throwable t) {
            // a key that cannot be read is not a key: stop asking
            keyMapping = null;
        }
    }

    /** The C key (or a rebind) asks the server for the console; ESC closes it. */
    public static void onKeyPressed() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) {
                return;
            }
            Screen current = currentScreen(mc);
            if (current instanceof McsmTerminalScreen terminal) {
                terminal.onClose();
                return;
            }
            if (current != null) {
                // a screen is open (a chest, the console): do not steal the key
                return;
            }
            ClientPlayNetworking.send(new McsmTerminalC2S("open", "", ""));
        } catch (Throwable ignored) {
            // a key that does nothing is better than one that crashes
        }
    }

    /**
     * The screen on show. {@code mc.gui.screen()} is the accessor this overlay
     * already uses (McsmGuiExtrasRows), so it is the one used here rather than a
     * field that may not exist under this mapping.
     */
    private static Screen currentScreen(Minecraft mc) {
        try {
            if (mc != null && mc.gui != null) {
                return mc.gui.screen();
            }
        } catch (Throwable ignored) {
            // no gui yet (during startup) -- no screen, no problem
        }
        return null;
    }

    private static void drain(Minecraft client) {
        McsmTerminalS2C next;
        synchronized (QUEUE) {
            if (QUEUE.isEmpty()) {
                return;
            }
            next = QUEUE.remove(0);
            QUEUE.clear();
        }
        if (client == null) {
            return;
        }
        String action = next.action() == null ? "" : next.action();
        if (currentScreen(client) instanceof McsmTerminalScreen terminal) {
            terminal.accept(action, next.a(), next.b());
            return;
        }
        if ("open".equals(action)) {
            McsmTerminalScreen.show(next.a(), next.b());
        }
    }
}
