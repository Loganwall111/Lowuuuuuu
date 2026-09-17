package net.mcsm.extras.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.mcsm.extras.McsmIdentity;

/**
 * BUILD #464 -- THE MOD'S SCREENS ARE LIT BY THE MOD'S OWN SKY.
 *
 * <p>The report: "fix the main menu be black. Like when I loaded it to the game
 * and it just goes completely black." Two of this mod's own screens were painting
 * an opaque near-black plate over everything behind them -- the title screen's
 * opted-out backdrop (#07050E..#0B0716) and the reskin every other screen wears
 * (#08060D..#0C0714) -- so "turn the panorama off" and "open any screen that is
 * not the title" both meant "a black screen".
 *
 * <p>This is the one sky those screens paint instead, and it is not invented: it is
 * the Decayed Reality's own air, read from {@link McsmIdentity} -- the same hexes
 * the dimension, its biome, its fog and its painted cube use. A screen with this
 * behind it is a place, and every screen that used to be black is the same place.
 *
 * <p>Draw-only, allocation-free apart from one lookup, and wrapped: a backdrop may
 * never cost a frame.
 */
public final class McsmMenuSky {

    private McsmMenuSky() {
    }

    /** Stars over the upper air: enough to read as a night, few enough to be calm. */
    private static final int STARS = 54;

    /**
     * Paints the whole screen with the decayed reality's sky. {@code dim} below 1
     * darkens it (screens that must stay readable use 0.85).
     */
    public static void paint(GuiGraphicsExtractor g, int w, int h, float dim) {
        try {
            if (g == null || w <= 0 || h <= 0) {
                return;
            }
            float d = Math.max(0.35F, Math.min(1.0F, dim));
            McsmIdentity.Skin skin = McsmIdentity.skin(McsmIdentity.DECAYED);
            String skyHex = skin == null ? "1B1026" : skin.sky();
            String fogHex = skin == null ? "140A1E" : skin.fog();
            String horizonHex = skin == null ? "6E3E2A" : skin.horizon();
            String glowHex = skin == null ? "8A5CFF" : skin.glow();
            // the air: deepest at the top, its own colour where the world ends
            g.fillGradient(0, 0, w, h, colour(skyHex, 0.55F * d, 0xFF),
                    colour(skyHex, 1.0F * d, 0xFF));
            g.fillGradient(0, h / 2, w, h, colour(fogHex, 1.0F * d, 0x00),
                    colour(fogHex, 1.35F * d, 0xFF));
            // the band the world ends on, and the glow hanging over it
            g.fillGradient(0, h * 3 / 4, w, h, colour(horizonHex, 0.60F * d, 0x00),
                    colour(horizonHex, 1.0F * d, 0xFF));
            g.fillGradient(0, h / 2, w, h * 3 / 4, colour(glowHex, 0.75F * d, 0x00),
                    colour(glowHex, 0.75F * d, 0x3C));
            for (int i = 0; i < STARS; i++) {
                long seed = (i + 1) * 0x9E3779B97F4A7C15L;
                int sx = (int) (((seed >>> 40) & 0xFFFFL) * w / 65536L);
                int sy = (int) (((seed >>> 16) & 0xFFFFL) * h * 0.55D / 65536.0D);
                int lum = (int) ((120 + (int) ((seed >>> 8) & 0x5FL)) * d);
                int alpha = (i % 3 == 0) ? 0xC0 : 0x80;
                g.fill(sx, sy, sx + 1, sy + 1, (alpha << 24) | (lum << 16) | (lum << 8)
                        | Math.min(255, lum + 30));
            }
        } catch (Throwable ignored) {
            // a backdrop can never cost a frame
        }
    }

    /** "RRGGBB" -> one ARGB colour, scaled and given an alpha. */
    public static int colour(String hex, float scale, int alpha) {
        float[] c = McsmIdentity.rgb(hex);
        if (c == null) {
            c = new float[]{0.10F, 0.06F, 0.15F};
        }
        int r = (int) Math.max(0.0F, Math.min(255.0F, c[0] * 255.0F * scale));
        int gg = (int) Math.max(0.0F, Math.min(255.0F, c[1] * 255.0F * scale));
        int b = (int) Math.max(0.0F, Math.min(255.0F, c[2] * 255.0F * scale));
        return (alpha << 24) | (r << 16) | (gg << 8) | b;
    }
}
