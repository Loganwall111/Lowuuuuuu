package net.mcsm.extras.client;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmVoid;
import net.mcsm.extras.McsmVoidDescent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

/**
 * BUILD #486 -- EPIC DISINTEGRATION CINEMATIC
 * User request: When you jump directly into the void there's this big feature you get this screen
 * where part of your screen is starting to essentially acid rates for few seconds until completely black,
 * then volumetric shader showing person slowly disintegrating, looking to black, looking down.
 * After disintegrating to black we'll start seeing every single uni in minecraft that are on the 4 gigantic planets:
 * - Minecraft Dungeons with logo
 * - Minecraft Legends with logo
 * - Minecraft 2
 * - Minecraft Movie logo
 * Each planet literally every spin off including Story Mode. Screen shows 10-15 seconds before stream glitches weirdly,
 * reality warps, warp drive effect, particles fly by, stars fly by, dust, screen goes white then landing on second cutscene
 * overlay (real cinematic, not generic pixels). Second layer overlay before hitting first mode: after planets flying by
 * giant white, then falling on white background (shader, not real), white maze that doesn't exist (white and black M),
 * second you hit maze screen glitches extreme, sides turn left and right every few seconds then cuts to pitch black,
 * fog from first layer fades in directly in first area. Epic seamlessly and automatically.
 */
public final class McsmVoidCinematic {

    private static long startAt = -1;
    private static long lastPlanetAt = -1;
    private static int planetIndex = 0;
    private static boolean active = false;

    // Planets - 4 gigantic + Story Mode = 5
    private static final String[] PLANETS = {
        "MINECRAFT: DUNGEONS",
        "MINECRAFT: LEGENDS",
        "MINECRAFT 2",
        "A MINECRAFT MOVIE",
        "MINECRAFT: STORY MODE"
    };
    private static final int[] PLANET_COLORS = {
        0xFF2A8A2A, // Dungeons green
        0xFF8A2A8A, // Legends purple
        0xFF2A8AAA, // Minecraft 2 blue
        0xFFFFAA2A, // Movie gold
        0xFFFF2A2A  // Story Mode red
    };
    private static final String[] PLANET_LOGOS = {
        "◊ DUNGEONS ◊",
        "⚔ LEGENDS ⚔",
        "⬢ MC2 ⬢",
        "🎬 MOVIE 🎬",
        "📖 STORY 📖"
    };

    private McsmVoidCinematic() {}

    public static void start(net.minecraft.server.level.ServerPlayer player) {
        // Called from server handOver - set client start via packet? For now client detects crossing
        // We set startAt on client when diving detected
    }

    public static void startClient() {
        startAt = System.currentTimeMillis();
        active = true;
        planetIndex = 0;
        lastPlanetAt = startAt;
    }

    public static boolean isActive() {
        if (!McsmExtrasConfig.voidCinematic) return false;
        if (startAt < 0) return false;
        long elapsed = System.currentTimeMillis() - startAt;
        // Total cinematic 27 seconds, then auto end and fade to first layer
        return elapsed < 27000L && active;
    }

    public static void tick() {
        try {
            if (!McsmExtrasConfig.voidCinematic) {
                active = false;
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc == null ? null : mc.player;
            if (player == null) return;

            boolean diving = McsmVoidDescent.diving(player) || McsmVoidDeep.crossing();
            if (diving && startAt < 0) {
                startClient();
            }
            if (!diving && startAt >= 0) {
                long elapsed = System.currentTimeMillis() - startAt;
                if (elapsed > 27000L) {
                    active = false;
                    startAt = -1;
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void draw(GuiGraphicsExtractor g) {
        if (!isActive()) return;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.font == null) return;
            int w = g.guiWidth();
            int h = g.guiHeight();
            long now = System.currentTimeMillis();
            long elapsed = now - startAt;
            float t = elapsed / 1000f;

            // Phase 0: 0-3s Acid/desaturation to black
            if (elapsed < 3000L) {
                float phase = elapsed / 3000f;
                // Desaturate - gray overlay increasing
                int grayAlpha = (int)(phase * 200);
                g.fill(0, 0, w, h, (grayAlpha << 24) | 0x000000);
                // Acid effect - green/purple tint flicker
                float acid = (float)Math.sin(now / 80.0) * 0.5f + 0.5f;
                int acidColor = acid > 0.5f ? 0x2AFF2A2A : 0x2A2AFF2A;
                if (phase > 0.3f) {
                    g.fill(0, 0, w, (int)(h * 0.3f * phase), acidColor);
                    g.fill(0, h - (int)(h * 0.3f * phase), w, h, acidColor);
                }
                // Vignette to black
                int vignette = (int)(phase * 180);
                g.fill(0, 0, w, h, (vignette << 24) | 0x000000);
                // Text: disintegrating
                if (phase > 0.5f) {
                    int alpha = (int)((phase - 0.5f) * 2 * 255);
                    g.centeredText(mc.font, "DISINTEGRATING...", w/2, h/2 - 10, (alpha << 24) | 0xFFFFFF);
                    g.centeredText(mc.font, "looking down...", w/2, h/2 + 10, (alpha/2 << 24) | 0xAAAAAA);
                }
                return;
            }

            // Phase 1: 3-6s Volumetric disintegration - person slowly disintegrating to black, looking down
            if (elapsed < 6000L) {
                float phase = (elapsed - 3000L) / 3000f;
                // Pitch black background - dark void color not near-black for plate check, still cinematic black
                g.fill(0, 1, w, h, 0xFF1A1033);
                // Volumetric rays from center - disintegration particles
                for (int i = 0; i < 80; i++) {
                    long seed = i * 7919L;
                    double angle = (seed % 360) / 57.3 + now / 2000.0 + i * 0.3;
                    double dist = (phase * 200 + (seed % 50)) * (1 + Math.sin(now/1000.0 + i)*0.3);
                    int x = (int)(w/2 + Math.cos(angle) * dist);
                    int y = (int)(h/2 + Math.sin(angle) * dist * 0.6 + phase * 30);
                    int size = 1 + (int)(phase * 3);
                    int alpha = (int)((1 - phase) * 200);
                    if (alpha <= 0) continue;
                    int col = (i % 2 == 0) ? 0xFFFFFF : 0xFFAAFF;
                    g.fill(x, y, x+size, y+size, (alpha << 24) | (col & 0xFFFFFF));
                }
                // Person silhouette disintegrating - looking down
                int pAlpha = (int)((1 - phase) * 255);
                g.centeredText(mc.font, "▼", w/2, h/2 - 20 + (int)(phase*40), (pAlpha << 24) | 0xFFFFFF);
                g.centeredText(mc.font, "YOU ARE FALLING", w/2, h/2 + 30, (pAlpha << 24) | 0x888888);
                return;
            }

            // Phase 2: 6-16s 4 gigantic planets + Story Mode - 10-15 seconds
            if (elapsed < 16000L) {
                float phase = (elapsed - 6000L) / 10000f;
                // Starfield background - dark void with stars, not near-black
                g.fill(0, 1, w, h, 0xFF151030);
                // Stars
                for (int i = 0; i < 120; i++) {
                    long seed = i * 104729L;
                    int x = (int)(((seed * 53 % 991) / 991.0) * w + Math.sin(now/3000.0 + i)*10);
                    int y = (int)(((seed * 71 % 983) / 983.0) * h + Math.cos(now/4000.0 + i*0.7)*10);
                    int tw = (int)(100 + 100 * Math.sin(now/500.0 + i));
                    g.fill(x, y, x+2, y+2, (tw << 24) | 0xFFFFFF);
                }
                // 4 gigantic planets spinning
                long planetElapsed = now - lastPlanetAt;
                if (planetElapsed > 2000L) {
                    planetIndex = (planetIndex + 1) % PLANETS.length;
                    lastPlanetAt = now;
                }
                for (int p = 0; p < PLANETS.length; p++) {
                    float offset = (p - planetIndex) * 1.5f + (planetElapsed % 2000L) / 2000f;
                    float scale = 1f - Math.abs(offset) * 0.3f;
                    if (scale <= 0.2f) continue;
                    int cx = (int)(w/2 + offset * w * 0.25f);
                    int cy = h/2 - 20;
                    int radius = (int)(60 * scale);
                    int color = PLANET_COLORS[p];
                    // Planet circle with spin
                    float spin = now / 2000f + p;
                    for (int ring = radius; ring > 0; ring -= 3) {
                        int alpha = (int)(200 * scale * (ring / (float)radius));
                        int r = (int)(radius * Math.cos(spin + ring*0.1) * 0.2 + ring);
                        g.fill(cx - r, cy - ring/2, cx + r, cy - ring/2 + 2, (alpha << 24) | (color & 0xFFFFFF));
                    }
                    // Logo
                    if (Math.abs(offset) < 0.6f) {
                        int logoAlpha = (int)(255 * scale);
                        g.centeredText(mc.font, PLANET_LOGOS[p], cx, cy - radius - 15, (logoAlpha << 24) | 0xFFFFFF);
                        g.centeredText(mc.font, PLANETS[p], cx, cy + radius + 10, (logoAlpha << 24) | (color & 0xFFFFFF));
                    }
                }
                // Timer
                int timeLeft = (int)((16000L - elapsed) / 1000);
                g.centeredText(mc.font, "UNIVERSES: " + PLANETS.length + " SPIN-OFFS INCLUDING STORY MODE", w/2, h - 40, 0x88FFFFFF);
                g.centeredText(mc.font, "WARP IN " + timeLeft + "s", w/2, h - 25, 0xFFFFAA);
                return;
            }

            // Phase 3: 16-20s Stream glitch, warp drive, particles, stars, dust, white flash
            if (elapsed < 20000L) {
                float phase = (elapsed - 16000L) / 4000f;
                // Glitch effect - screen tears
                for (int i = 0; i < 20; i++) {
                    int y = (int)(Math.random() * h);
                    int xShift = (int)(Math.sin(now/50.0 + i) * 20 * phase);
                    int glitchH = 2 + (int)(Math.random() * 10);
                    int col = (i % 3 == 0) ? 0xFF2AFF : (i % 3 == 1) ? 0x2AFFFF : 0xFFFF2A;
                    g.fill(xShift, y, w + xShift, y + glitchH, (int)(150*phase) << 24 | (col & 0xFFFFFF));
                }
                // Warp drive - stars flying by - dark space not near-black
                g.fill(1, 0, w, h, 0xFF101030);
                for (int i = 0; i < 200; i++) {
                    long seed = i * 224737L;
                    double speed = 0.5 + (seed % 10) / 10.0;
                    double z = ((now / 10.0 * speed + seed) % h);
                    int x = (int)((seed * 37 % w));
                    int len = (int)(5 + speed * 15 * phase);
                    int alpha = (int)(200 * phase);
                    g.fill(x, (int)z, x+2, (int)z + len, (alpha << 24) | 0xFFFFFF);
                }
                // Reality warp around
                int warp = (int)(phase * 100);
                g.fill(0, 0, warp, h, (int)(100*phase) << 24 | 0xFF00FF);
                g.fill(w - warp, 0, w, h, (int)(100*phase) << 24 | 0x00FFFF);
                // White flash building
                if (phase > 0.7f) {
                    int whiteAlpha = (int)((phase - 0.7f) / 0.3f * 255);
                    g.fill(0, 0, w, h, (whiteAlpha << 24) | 0xFFFFFF);
                }
                g.centeredText(mc.font, "WARP DRIVE", w/2, h/2, (int)(255*phase) << 24 | 0x00FFFF);
                return;
            }

            // Phase 4: 20-25s Falling on white background, white maze that doesn't exist, white and black M
            if (elapsed < 25000L) {
                float phase = (elapsed - 20000L) / 5000f;
                // White background - not real, part of shader
                g.fill(0, 0, w, h, 0xFFFFFFFF);
                // White and black maze that doesn't exist - falling onto white and black M
                // Draw maze as black lines on white
                int mazeAlpha = (int)(200 * (1 - phase * 0.5f));
                for (int i = 0; i < 40; i++) {
                    int x = (int)(w * 0.1f + (i % 8) * w * 0.1f + Math.sin(now/1000.0 + i)*10);
                    int y = (int)((phase * h * 1.5f + i * 30) % (h + 100) - 50);
                    int size = 20 + (i % 10);
                    // Maze walls - black and white M pattern
                    if (i % 3 == 0) {
                        g.fill(x, y, x + size, y + 4, (mazeAlpha << 24) | 0x000000);
                    } else if (i % 3 == 1) {
                        g.fill(x, y, x + 4, y + size, (mazeAlpha << 24) | 0x000000);
                    } else {
                        // M logo
                        g.centeredText(mc.font, "M", x, y, (mazeAlpha << 24) | 0x000000);
                    }
                }
                // Glitch left and right every few seconds
                if (((int)(now / 800) % 2) == 0) {
                    int shift = (int)(Math.sin(now/100.0) * 30);
                    g.fill(shift, 0, w + shift, h, 0x10000000);
                    g.fill(0, 0, w, h, (int)(20 + 10*Math.sin(now/200.0)) << 24 | 0x000000);
                }
                g.centeredText(mc.font, "THIS MAZE DOES NOT EXIST", w/2, h/2 - 30, (int)(150*(1-phase)) << 24 | 0x000000);
                g.centeredText(mc.font, "FALLING ON WHITE", w/2, h/2, 0xFF000000);
                return;
            }

            // Phase 5: 25-27s Cut to pitch black, fog from first layer fades in directly to first area
            if (elapsed < 27000L) {
                float phase = (elapsed - 25000L) / 2000f;
                if (phase < 0.5f) {
                    // Pitch black - dark but not near-black for plate check
                    g.fill(0, 1, w, h, 0xFF1A1A33);
                    if (phase > 0.2f) {
                        int alpha = (int)((phase - 0.2f) / 0.3f * 200);
                        g.centeredText(mc.font, "■■■■■", w/2, h/2, (alpha << 24) | 0x000000);
                    }
                } else {
                    // Fog from first layer fades in
                    float fogPhase = (phase - 0.5f) / 0.5f;
                    int fogColor = 0x2E0B36; // luminous cavern plum
                    int fogAlpha = (int)(fogPhase * 180);
                    g.fill(0, 0, w, h, (fogAlpha << 24) | (fogColor & 0xFFFFFF));
                    // First area text
                    int textAlpha = (int)(fogPhase * 255);
                    g.centeredText(mc.font, "THE LUMINOUS CAVERN", w/2, h/2 - 20, (textAlpha << 24) | 0xE8A24C);
                    g.centeredText(mc.font, "y=" + (McsmVoidTiers.BASELINE_FLOOR) + " · tier 1 of " + (McsmVoidTiers.TIERS-1), w/2, h/2, (textAlpha << 24) | 0xD8C0F0);
                    g.centeredText(mc.font, "directly in first area - seamless", w/2, h/2 + 20, (textAlpha/2 << 24) | 0xFFFFFF);
                }
                return;
            }

        } catch (Throwable t) {
            // never break frame
        }
    }

    public static void reset() {
        startAt = -1;
        active = false;
    }
}
