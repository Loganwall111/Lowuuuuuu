package net.mcsm.extras.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Readable sky overlay — never an opaque near-black plate.
 * Channel floor keeps every stop well above "black screen" luminance.
 */
public final class McsmMenuSky {

    private static final int CHANNEL_MIN = 0x28;
    private static final int STARS = 54;
    public static final float DIM_MIN = 0.55F;

    private McsmMenuSky() {
    }

    /** Translucent cinematic grade over whatever is already on the frame. */
    public static void paintVignette(GuiGraphicsExtractor g, int w, int h) {
        try {
            if (g == null || w <= 0 || h <= 0) {
                return;
            }
            g.fillGradient(0, 0, w, h / 3, 0x661A1440, 0x00000000);
            g.fillGradient(0, h * 2 / 3, w, h, 0x00000000, 0x553F255A);
        } catch (Throwable ignored) {
        }
    }

    /** Opaque-but-bright sky for screens that have no panorama behind them. */
    public static void paint(GuiGraphicsExtractor g, int w, int h, float dim) {
        try {
            if (g == null || w <= 0 || h <= 0) {
                return;
            }
            float d = Math.max(DIM_MIN, Math.min(1.0F, dim));
            int zenith = stop(0x3A2A68, d, 0xFF);
            int mid = stop(0x4A4652, d, 0xFF);
            int horizon = stop(0x8A5A3A, d, 0xFF);
            g.fillGradient(0, 0, w, h / 2, zenith, mid);
            g.fillGradient(0, h / 2, w, h, mid, horizon);
            g.fillGradient(0, h / 2, w, h * 3 / 4, 0x008A5CFF, 0x3C8A5CFF);
            for (int i = 0; i < STARS; i++) {
                long seed = (i + 1) * 0x9E3779B97F4A7C15L;
                int sx = (int) (((seed >>> 40) & 0xFFFFL) * w / 65536L);
                int sy = (int) (((seed >>> 16) & 0xFFFFL) * h * 0.45D / 65536.0D);
                int lum = Math.max(CHANNEL_MIN + 0x40, 150 + (int) ((seed >>> 8) & 0x5FL));
                int alpha = (i % 3 == 0) ? 0xC0 : 0x80;
                g.fill(sx, sy, sx + 1, sy + 1, (alpha << 24) | (lum << 16) | (lum << 8)
                        | Math.min(255, lum + 30));
            }
        } catch (Throwable ignored) {
        }
    }

    private static int stop(int rgb, float scale, int alpha) {
        int r = clamp(((rgb >> 16) & 0xFF) * scale);
        int gg = clamp(((rgb >> 8) & 0xFF) * scale);
        int b = clamp((rgb & 0xFF) * scale);
        return (alpha << 24) | (r << 16) | (gg << 8) | b;
    }

    private static int clamp(float v) {
        int n = (int) v;
        return Math.max(CHANNEL_MIN, Math.min(255, n));
    }
}
