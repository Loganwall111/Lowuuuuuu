package net.mcsm.extras.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.mcsm.extras.McsmIdentity;

/**
 * BUILD #464/#466 -- THE MOD'S SCREENS ARE LIT BY THE MOD'S OWN SKY.
 *
 * <p>The report, twice: "fix the main menu be black. Like when I loaded it to the
 * game and it just goes completely black." Five screens of this mod's own were
 * painting an opaque near-black plate over everything behind them, and this is the
 * one sky they paint instead.
 *
 * <p>BUILD #466 -- and the first version of this painter was still black, which is
 * why the report came back. It composed the frame out of the skin's {@code fog} hex
 * (#140A1E for the decayed reality) and its dimension {@code sky} hex (#1B1026):
 * both are the near-black air of a torn world, and at the dim those screens ask for
 * (0.45 to 0.62) the top half of every one of them came out at #09050C. A screen
 * lit that way is a black screen with a warm strip along the bottom, which is what
 * a player sees and what they said. The gate passed it because the gate had been
 * written to the same recipe: it asserted that the skin's fog hex was in here.
 *
 * <p>So this paints the sky the world outside actually wears: the same three stops
 * the world's own painted cube is painted from (ci/make_skybox_textures.py,
 * DIMENSIONS -- violet zenith #2A1C4E, ash-grey air #2C2A2E, the ochre band the
 * world ends on #6E3E2A), with the world's own glow hanging over the horizon and
 * stars above it. Every stop is floored, so no dim value can take a screen to
 * black: a backdrop that can go black is the bug this class exists to kill.
 *
 * <p>Draw-only, allocation-free, and wrapped: a backdrop may never cost a frame.
 */
public final class McsmMenuSky {

    private McsmMenuSky() {
    }

    /** Stars over the upper air: enough to read as a night, few enough to be calm. */
    private static final int STARS = 54;

    /**
     * The sky the screens wear -- the Decayed Reality's, because that is the world
     * this mod opens into. These are the cube's own three stops, copied from the
     * generator that paints that cube, and ci/check_phase_uniform.py fails the build
     * if the two ever disagree.
     */
    private static final String ZENITH = "2A1C4E";
    private static final String MID = "2C2A2E";
    private static final String HORIZON = "6E3E2A";

    /** The violet the storm hangs over its horizon. */
    private static final String GLOW = "8A5CFF";

    /** The darkest a screen may ask for, and the darkest it may then be scaled to. */
    public static final float DIM_MIN = 0.35F;
    public static final float SCALE_MIN = 0.85F;

    /** No channel of the air is ever allowed under this, whatever the dim. */
    private static final int CHANNEL_MIN = 0x14;

    /**
     * Paints the whole screen with the decayed reality's sky. {@code dim} below 1
     * calms it (screens that must stay readable use 0.85); it can no longer black it
     * out.
     */
    public static void paint(GuiGraphicsExtractor g, int w, int h, float dim) {
        try {
            if (g == null || w <= 0 || h <= 0) {
                return;
            }
            float d = Math.max(DIM_MIN, Math.min(1.0F, dim));
            float s = SCALE_MIN + (1.0F - SCALE_MIN) * d;
            McsmIdentity.Skin skin = McsmIdentity.skin(McsmIdentity.DECAYED);
            String glowHex = skin == null ? GLOW : skin.glow();
            // the air, the way the world's own cube paints it: the violet zenith at
            // the top, ash grey across the middle, the ochre band where the world
            // ends
            g.fillGradient(0, 0, w, h / 2, stop(ZENITH, s, 0xFF), stop(MID, s, 0xFF));
            g.fillGradient(0, h / 2, w, h, stop(MID, s, 0xFF), stop(HORIZON, s, 0xFF));
            // the glow hanging over that band, and the stars above it
            g.fillGradient(0, h / 2, w, h * 3 / 4, stop(glowHex, 0.75F * d, 0x00),
                    stop(glowHex, 0.75F * d, 0x3C));
            for (int i = 0; i < STARS; i++) {
                long seed = (i + 1) * 0x9E3779B97F4A7C15L;
                int sx = (int) (((seed >>> 40) & 0xFFFFL) * w / 65536L);
                int sy = (int) (((seed >>> 16) & 0xFFFFL) * h * 0.45D / 65536.0D);
                int lum = (int) ((150 + (int) ((seed >>> 8) & 0x5FL)) * d);
                lum = Math.max(CHANNEL_MIN + 0x40, Math.min(255, lum));
                int alpha = (i % 3 == 0) ? 0xC0 : 0x80;
                g.fill(sx, sy, sx + 1, sy + 1, (alpha << 24) | (lum << 16) | (lum << 8)
                        | Math.min(255, lum + 30));
            }
        } catch (Throwable ignored) {
            // a backdrop can never cost a frame
        }
    }

    /**
     * "RRGGBB" -> one ARGB colour, scaled, floored and given an alpha. The floor is
     * the whole point: #09050C is what the old version of this painter produced.
     */
    public static int stop(String hex, float scale, int alpha) {
        float[] c = McsmIdentity.rgb(hex);
        if (c == null) {
            c = new float[]{0.16F, 0.11F, 0.30F};
        }
        int r = clamp(c[0], scale);
        int gg = clamp(c[1], scale);
        int b = clamp(c[2], scale);
        return (alpha << 24) | (r << 16) | (gg << 8) | b;
    }

    /** The same colour, named the way the first version of this class named it. */
    public static int colour(String hex, float scale, int alpha) {
        return stop(hex, scale, alpha);
    }

    private static int clamp(float channel, float scale) {
        int v = (int) (channel * 255.0F * scale);
        return Math.max(CHANNEL_MIN, Math.min(255, v));
    }

    /** What the screens are wearing, for {@code /ds menu}. */
    public static String state() {
        return "sky #" + ZENITH + " / #" + MID + " / #" + HORIZON
                + " (dim >= " + DIM_MIN + ", scale >= " + SCALE_MIN + ")";
    }
}
