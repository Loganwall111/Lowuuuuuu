package net.mcsm.extras.client;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmVoid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * BUILD #452 -- THE BOTTOM OF THE VOID: RGB LIGHT RAYS AND INFINITE WHITE RINGS.
 *
 * <p>The report: "it's very dark when you fall to the very bottom ... make a really
 * cool thing and add light rays, colour RGB light ... the bottom ... as a rainbow
 * casting infinite white rings". The dimension already has an invisible floor under
 * it ({@link McsmVoid#FLOOR_Y}, laid by the dimension's own generator as a barrier
 * layer, so it is solid and invisible and standing on it is possible without seeing
 * anything holding you up). This is what the bottom LOOKS like: the darkness is
 * answered, and the answer is drawn, not baked.
 *
 * <p>WHAT IS DRAWN, and why each part is what it is:
 *
 * <ul>
 *   <li><b>RGB RAYS.</b> Twelve shafts of light out of the floor, in spectrum order
 *       from red through violet, slowly rotating around the camera's own axis so the
 *       bottom is never a still image. Each shaft is a line of samples with a
 *       falloff, which is the only way to draw a diagonal with filled rectangles --
 *       and a filled rectangle is the one primitive this HUD is guaranteed to have.</li>
 *   <li><b>INFINITE WHITE RINGS.</b> Rings leave the floor for ever, one after
 *       another: each one is born at the centre, expands past the frame and is
 *       replaced by the next, so the count is unbounded even though only three are
 *       ever on screen. "Infinite" is a property of the timer, not of the drawing.</li>
 *   <li><b>THE LIFT.</b> A rainbow wash that gets stronger the closer to the bottom
 *       you are, so the fall tells you where you are before you land.</li>
 *   <li><b>AND IT STAYS OUT OF THE WAY.</b> Nothing here draws above
 *       {@link #RAY_Y}, nothing draws when the effect is switched off, and nothing
 *       draws when a screen is open -- the bottom of the void is a place, not a
 *       menu cover.</li>
 * </ul>
 */
public final class McsmVoidFloor {

    /** Everything below this height, in the void, is part of the bottom. */
    public static final float RAY_Y = 56.0F;
    /** How many shafts, and how far each one is sampled. */
    private static final int RAYS = 12;
    private static final int STEPS = 20;
    private static final int STEP_PX = 11;
    /** Rings alive at once, and how long one takes to cross the frame. */
    private static final int RING_LIVE = 3;
    private static final long RING_MS = 2600L;

    private static final int[] SPECTRUM = {
        0xFFFF3B30, 0xFFFF8A00, 0xFFFFD60A, 0xFF9BE00A, 0xFF34C759, 0xFF00C7BE,
        0xFF32ADE6, 0xFF3B5BFF, 0xFF7A4DFF, 0xFFC04DFF, 0xFFFF4DD2, 0xFFFF4D6D,
    };

    private McsmVoidFloor() {
    }

    /** True while the bottom of the void should be drawn. */
    public static boolean active() {
        if (!McsmExtrasConfig.voidLight) {
            return false;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null || mc.level == null) {
                return false;
            }
            if (McsmTerminalClient.currentScreen(mc) != null) {
                return false;
            }
            if (!mc.player.level().dimension().equals(McsmVoid.DIMENSION)) {
                return false;
            }
            return mc.player.getY() < RAY_Y;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * How strong the bottom is: 0 at {@link #RAY_Y}, 1 at the invisible floor. The
     * fall brightens, which is the whole point of it being visible at all.
     */
    private static float strength(float y) {
        float span = RAY_Y - McsmVoid.FLOOR_Y;
        if (span <= 0.0F) {
            return 1.0F;
        }
        float t = (RAY_Y - y) / span;
        return Math.max(0.0F, Math.min(1.0F, t));
    }

    /** Drawn every frame on the same proven HUD hook as the scenes and the ending. */
    public static void draw(GuiGraphicsExtractor g) {
        if (!active()) {
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.font == null) {
                return;
            }
            int w = g.guiWidth();
            int h = g.guiHeight();
            float strength = strength((float) mc.player.getY());
            if (strength <= 0.01F) {
                return;
            }
            int cx = w / 2;
            int cy = h / 2;
            long now = System.currentTimeMillis();

            // ---- the lift: the bottom is not dark, and it says so by getting warmer
            int wash = (int) (46 * strength);
            g.fill(0, 0, w, h, (wash << 24) | 0x2A1B4E);

            // ---- the RGB shafts, rotating around the camera
            double spin = (now % 24000L) / 24000.0D * Math.PI * 2.0D;
            int reach = (int) (Math.max(w, h) * (0.35D + 0.65D * strength));
            for (int i = 0; i < RAYS; i++) {
                double ang = spin + (i / (double) RAYS) * Math.PI * 2.0D;
                int colour = SPECTRUM[i];
                for (int s = 1; s <= STEPS; s++) {
                    double d = s * STEP_PX;
                    int alpha = (int) ((1.0D - (double) s / STEPS) * 90.0D * strength);
                    if (alpha <= 2) {
                        continue;
                    }
                    int x = cx + (int) Math.round(Math.cos(ang) * d);
                    int y = cy + (int) Math.round(Math.sin(ang) * d * 0.55D);
                    int size = 3 + (int) ((double) s / STEPS * 9.0D);
                    if (Math.hypot(x - cx, y - cy) > reach) {
                        break;
                    }
                    g.fill(x - size / 2, y - size / 2, x + size / 2, y + size / 2,
                            (alpha << 24) | (colour & 0xFFFFFF));
                }
            }

            // ---- the infinite rings: born at the centre, expanding past the frame
            int maxReach = (int) (Math.hypot(w, h) * 0.55D);
            for (int ring = 0; ring < RING_LIVE; ring++) {
                long age = (now + ring * (RING_MS / RING_LIVE)) % RING_MS;
                double t = age / (double) RING_MS;
                double radius = t * maxReach;
                int alpha = (int) ((1.0D - t) * 150.0D * strength);
                if (alpha <= 3) {
                    continue;
                }
                int colour = (alpha << 24) | 0xFFFFFF;
                int segments = 48;
                for (int a = 0; a < segments; a++) {
                    double ang = (a / (double) segments) * Math.PI * 2.0D;
                    int x = cx + (int) Math.round(Math.cos(ang) * radius);
                    int y = cy + (int) Math.round(Math.sin(ang) * radius * 0.55D);
                    if (x < -8 || y < -8 || x > w + 8 || y > h + 8) {
                        continue;
                    }
                    int size = 3;
                    g.fill(x - size, y - size, x + size, y + size, colour);
                }
            }

            // ---- the floor itself: the invisible thing you are standing on
            if (mc.player.onGround()) {
                g.centeredText(mc.font, "the invisible floor holds", cx,
                        h - 46, 0xFFDCCFF5);
                g.centeredText(mc.font, "y=" + McsmVoid.FLOOR_Y + " \u00b7 nothing and solid",
                        cx, h - 34, 0xFF9A86C8);
            }
        } catch (Throwable t) {
            // never break a frame over light
        }
    }
}
