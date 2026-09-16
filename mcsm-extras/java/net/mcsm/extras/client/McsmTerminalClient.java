package net.mcsm.extras.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * BUILD #416 (D.8, phase 5) -- the CLIENT half of the terminal.
 *
 * WHAT CHANGED, AND WHY. This class used to receive two custom payload types. It
 * turns out the overlay is compiled against the client jar and the four Fabric
 * RENDERING modules -- Fabric's networking module is not on that classpath at
 * all -- so the channel could not compile, and run 500 failed at javac. The
 * channel is gone, and nothing here needs one:
 *
 *   * the C key opens the console (interface, and the player is here);
 *   * the antenna and the field guide open it from their own {@code use};
 *   * the world's half of the story -- the radio signals, the operator's report,
 *     the MASSG -- is the SERVER's job, and it speaks in chat and in world state,
 *     channels that exist without anybody registering anything;
 *   * the one thing the screen needs from the world -- the MASSG's countdown --
 *     is read from the creature's own name, which the game already syncs (see
 *     {@link McsmMassgSky}).
 *
 * THE C KEY. There was no binding at all, which is why it "doesn't really do
 * anything". A KeyMapping has to be registered through Fabric's keybinding
 * helper, and that helper is not on this classpath either, so the mapping is
 * built and registered REFLECTIVELY -- constructor, helper and consumeClick all
 * by name -- inside one try/catch. If any part of that is unavailable the mod
 * simply has no key and everything else still works.
 */
public final class McsmTerminalClient {

    private static boolean registered = false;
    private static Object keyMapping;

    private McsmTerminalClient() {
    }

    /** Called from the mod's client initializer. */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        try {
            // ClientTickEvents.START_CLIENT_TICK with a one-argument lambda is
            // the base mod's own registration (DabyWitherStormModClient), so the
            // event and its SAM shape are both proven.
            ClientTickEvents.START_CLIENT_TICK.register(client -> {
                pollKey();
                McsmMassgSky.tick();
            });
            bindKey();
            System.out.println("[ds] the terminal's C key is live");
        } catch (Throwable t) {
            System.err.println("[ds] the terminal client failed: " + t);
        }
    }

    // ---------------------------------------------------------------------
    // The C key
    // ---------------------------------------------------------------------

    private static void bindKey() {
        try {
            Class<?> mapping = Class.forName("net.minecraft.client.KeyMapping");
            Class<?> helper = Class.forName(
                    "net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
            java.lang.reflect.Constructor<?> ctor = mapping.getConstructor(
                    String.class, int.class, String.class);
            Object key = ctor.newInstance("key.mcsm.terminal", 67, "key.categories.misc");
            Object registeredKey = helper.getMethod("registerKeyBinding", mapping)
                    .invoke(null, key);
            keyMapping = registeredKey == null ? key : registeredKey;
        } catch (Throwable t) {
            keyMapping = null;
        }
    }

    private static void pollKey() {
        if (keyMapping == null) {
            return;
        }
        try {
            java.lang.reflect.Method consume = keyMapping.getClass().getMethod("consumeClick");
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

    /** C opens the console; C again, or ESC, closes it. */
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
                // a screen is open (a chest, the config): do not steal the key
                return;
            }
            McsmTerminalScreen.show("login",
                    "RESTRICTED AREA\n\nEnter the admin password to continue.\n"
                    + "The set is locked to the operator who buried this world.");
        } catch (Throwable ignored) {
            // a key that does nothing is better than one that crashes
        }
    }

    /** The screen on show, read the way this overlay reads it elsewhere. */
    static Screen currentScreen(Minecraft mc) {
        try {
            if (mc != null && mc.gui != null) {
                return mc.gui.screen();
            }
        } catch (Throwable ignored) {
            // no gui yet (during startup) -- no screen, no problem
        }
        return null;
    }
}
