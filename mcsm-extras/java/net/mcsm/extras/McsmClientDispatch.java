package net.mcsm.extras;

/**
 * BUILD #416 (D.8, phase 5) -- the one door between the common code and the
 * client screens.
 *
 * The overlay is compiled as a single source set, so a class in
 * {@code net.mcsm.extras} can be loaded on a dedicated server. Calling a client
 * screen type from there directly would be a latent crash (and, on some loaders,
 * a hard failure). Every client call therefore goes through this class, which
 * only ever names the screen BY STRING and only ever runs where a client exists.
 *
 * "Show, don't tell" -- but here it is genuinely "don't reference": this is the
 * same discipline the rest of this build uses for boss bars and entity
 * registration.
 */
public final class McsmClientDispatch {

    private McsmClientDispatch() {
    }

    /**
     * Open the story terminal on the client. {@code mode} is "terminal" for the
     * restricted console and "guide" for the field guide; the screen itself
     * decides what is unlocked (see McsmTerminalScreen).
     */
    public static void openTerminal(String mode) {
        try {
            Class<?> screen = Class.forName("net.mcsm.extras.client.McsmTerminalScreen");
            String start = "guide".equals(mode) ? "guide" : "login";
            String body = "guide".equals(mode)
                    ? "FIELD GUIDE :: opened from the book. The pages follow."
                    : "RESTRICTED AREA\n\nEnter the admin password to continue.\n"
                      + "The set is locked to the operator who buried this world.";
            screen.getMethod("show", String.class, String.class)
                    .invoke(null, start, body);
        } catch (Throwable ignored) {
            // no client (or no screen): using the item still costs nothing
        }
    }
}
