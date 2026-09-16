package net.mcsm.extras.client;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * BUILD #416 (D.8, phase 6) -- THE SKY TERMINAL.
 *
 * The user's brief: "a gigantic terminal appears in the sky counting from 99 down
 * to 1 February 2027", plus "screen flicker, colour glitches". This is that
 * terminal, and the flicker that comes with it.
 *
 * WHY IT IS DRAWN BY THE CLIENT AND NOT A BLOCK IN THE WORLD. The creature cannot
 * be killed and the world cannot be put back, but a screen in the sky is a piece
 * of interface: it belongs on the client, it costs nothing to send (one small
 * packet every two seconds carries the number), it stays legible at any distance,
 * and it can flicker the way a hacked display flickers without touching a block.
 *
 * The number is NOT computed here. The server owns it (McsmMassg.counter), sends
 * it in the terminal channel, and the client only draws the last number it was
 * told -- so every player sees the same count on the same day, and a client that
 * joins late is immediately in sync.
 */
public final class McsmMassgSky {

    /** How long the last number stays on screen after it arrives. */
    private static final long HOLD_MS = 2600L;
    /** The flicker pass: how long one glitch burst lasts. */
    private static final long BURST_MS = 260L;

    private static int number = -1;
    private static String line = "";
    private static long receivedAt = 0L;
    private static long burstAt = 0L;
    private static int burstSeed = 0;

    private McsmMassgSky() {
    }

    /** How often the client looks for the creature again. */
    private static final int RESCAN_TICKS = 20;
    private static int rescan = 0;
    private static boolean present = false;

    /**
     * FIND THE MASSG, AND READ THE NUMBER OFF IT.
     *
     * The countdown lives in the creature's own custom name ("MASSG|47|..."),
     * which the game syncs to every client for free, so this pass is the whole
     * client side of the sky terminal: one entity scan a second over the loaded
     * world, one string parse, and the number on the panel is the number the
     * server wrote.
     */
    public static void tick() {
        try {
            if (++rescan < RESCAN_TICKS) {
                return;
            }
            rescan = 0;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || mc.player == null) {
                if (present) {
                    reset();
                }
                present = false;
                return;
            }
            boolean found = false;
            for (net.minecraft.world.entity.Mob mob : mc.level.getEntitiesOfClass(
                    net.minecraft.world.entity.Mob.class,
                    mc.player.getBoundingBox().inflate(512.0D))) {
                if (mob.getCustomName() == null) {
                    continue;
                }
                String name = mob.getCustomName().getString();
                if (!name.startsWith("MASSG|")) {
                    continue;
                }
                found = true;
                String[] parts = name.split("\\|", 3);
                int parsed = -1;
                try {
                    parsed = Integer.parseInt(parts.length > 1 ? parts[1] : "");
                } catch (Throwable ignored) {
                    parsed = -1;
                }
                String text = parts.length > 2 ? parts[2] : "";
                if (parsed != number) {
                    // every new number arrives with a colour glitch
                    burstAt = System.currentTimeMillis();
                    burstSeed = (int) (burstAt & 0x7FFF);
                }
                number = parsed;
                line = text;
                receivedAt = System.currentTimeMillis();
                break;
            }
            if (!found && present) {
                number = -1;
            }
            present = found;
        } catch (Throwable ignored) {
            // the sky terminal must never take a tick down
        }
    }

    /** Is the count on screen right now? */
    public static boolean active() {
        return number > 0 && System.currentTimeMillis() - receivedAt <= HOLD_MS;
    }

    /** The last number the server sent, or -1. */
    public static int number() {
        return number;
    }

    /**
     * One frame of the sky terminal and its glitches. Drawn by the story HUD, so
     * it shares that pass's screen size and its ordering with everything else.
     */
    public static void paint(GuiGraphicsExtractor g, int w, int h) {
        long now = System.currentTimeMillis();
        boolean burst = now - burstAt <= BURST_MS;

        // --- colour glitches: RGB-split bands, only during a burst ----------
        if (burst) {
            int bands = 5 + (burstSeed % 4);
            for (int i = 0; i < bands; i++) {
                int seed = burstSeed + i * 977;
                int y = Math.abs(seed) % Math.max(1, h);
                int bandH = 2 + Math.abs(seed / 7) % 7;
                int shift = (Math.abs(seed / 13) % 24) - 12;
                int a = 0x33;
                g.fill(shift, y, w + shift, y + bandH, (a << 24) | 0xFF2040);
                g.fill(-shift, y + 1, w - shift, y + bandH + 1, (a << 24) | 0x20FF80);
                g.fill(0, y + 2, w, y + bandH + 2, (a >> 1 << 24) | 0x4020FF);
            }
            // and one hard white tear across the picture
            int tearY = Math.abs(burstSeed / 3) % Math.max(1, h);
            g.fill(0, tearY, w, tearY + 1, 0x66FFFFFF);
        }

        if (!active()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) {
            return;
        }

        // --- THE GIGANTIC TERMINAL IN THE SKY ------------------------------
        //
        // A wide, slightly trapezoidal plate high on the screen with a scanline
        // field, the count in a huge monospace-ish block (the number itself is
        // scaled up by drawing it three times: outline, body, highlight), and the
        // line the server sent underneath.
        int pw = Math.min(460, Math.max(240, w * 2 / 3));
        int ph = 108;
        int px = (w - pw) / 2;
        int py = Math.max(18, h / 8);

        // flicker: the plate itself loses a few percent of its alpha at random
        int flicker = burst ? 150 : 205;
        if (Math.abs((now / 90L) % 7 - 3) == 0) {
            flicker -= 25;
        }

        g.fill(px - 6, py - 6, px + pw + 6, py + ph + 6, (0x22 << 24) | 0x4A0A6A);
        g.fill(px - 2, py - 2, px + pw + 2, py + ph + 2, (0x44 << 24) | 0x8A2CFF);
        g.fill(px, py, px + pw, py + ph, (flicker << 24) | 0x0A0616);
        g.fill(px, py, px + pw, py + 2, (0xEE << 24) | 0x39E0FF);
        g.fill(px, py + ph - 2, px + pw, py + ph, (0xEE << 24) | 0x6A1AC8);

        // scanlines
        for (int y = py + 4; y < py + ph - 4; y += 4) {
            g.fill(px + 3, y, px + pw - 3, y + 1, 0x1A1E5A78);
        }

        String title = "MASSG :: SKY TERMINAL";
        g.centeredText(mc.font, "\u00a7b" + title, px + pw / 2, py + 8, 0xFF9FEAFF);
        g.centeredText(mc.font, "\u00a75IT CANNOT BE DELETED", px + pw / 2, py + 20, 0xFFB98CFF);

        // the count, twice the font, in the middle of the plate
        String count = String.valueOf(number);
        float scale = 3.0F;
        try {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.translate((float) (px + pw / 2 - 30), (float) (py + 38));
            pose.scale(scale);
            g.centeredText(mc.font, "\u00a7f\u00a7l" + count, 0, 0, 0xFFFFFFFF);
            pose.popMatrix();
        } catch (Throwable ignored) {
            // if the pose stack is not available, the small count still shows
            g.centeredText(mc.font, "\u00a7f\u00a7l" + count, px + pw / 2, py + 40, 0xFFFFFFFF);
        }

        g.centeredText(mc.font, "\u00a77days until \u00a7f1 February 2027", px + pw / 2, py + 76, 0xFFBFE9FF);
        if (!line.isEmpty()) {
            g.centeredText(mc.font, "\u00a75" + line, px + pw / 2, py + 88, 0xFFB98CFF);
        }

        // the creature's own purple haze, low and wide over the picture
        g.fillGradient(0, h * 2 / 3, w, h, 0x00000000, 0x332A0A4A);
    }

    /** Reset on disconnect: the next world's count starts fresh. */
    public static void reset() {
        number = -1;
        line = "";
        receivedAt = 0L;
        burstAt = 0L;
    }

    /** A one-line summary for a debug line, if anything ever needs one. */
    public static Component summary() {
        return Component.literal("MASSG sky terminal: " + (active() ? number : -1));
    }
}
