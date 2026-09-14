package net.mcsm.extras.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import net.mcsm.extras.McsmExtrasConfig;

/**
 * Build #374 -- the CINEMATIC BOOT + epic depth animations.
 *
 *  1. PRE-GAME CINEMATIC (menu): a 4.2s side-view cutscene drawn over the
 *     main menu on every launch — black frame, moon, a Wither Storm
 *     silhouette drifting in and growing, two lightning strikes, the saga
 *     title fade. Any key skips.
 *  2. COMMAND BLOCK BURST (menu): the storm's command block grows in the
 *     centre of the menu, the command symbol glows, then it bursts into the
 *     main menu (expanding ring + radial spokes + fragment spray + flash) —
 *     the OG panorama is already waiting behind it.
 *  3. WORLD CRACKS (in-world): on every world load the world "cracks
 *     apart" for 2.2s — nine jagged fissures rip from the centre, two
 *     giant sky shockwave rings expand, a white flash at the peak.
 *  4. BUTTON BREAK-APART (menu): pressing a main-menu button shatters it —
 *     18 gold/white fragments fly out and fade over the screen the button
 *     opens (drawn from the vanilla Screen hook, so the spray survives the
 *     screen transition).
 *
 * Everything is wall-clock driven and draw-only (no input state is eaten),
 * and every call is a proven fill/fillGradient/centeredText primitive.
 * Toggles: "Cinematic Boot" and "World Crack Intro" in the console.
 */
public final class McsmCinematic {

    public static final long PRE_GAME_MS = 4200L;
    public static final long BURST_MS = 2600L;
    public static final long CRACK_MS = 2200L;
    public static final long SHATTER_MS = 340L;

    // pre-game / burst (menu)
    private static boolean preGameDone = false;
    private static long preGameStartMs = -1L;
    private static long burstStartMs = -1L;

    // world cracks
    private static long crackStartMs = -1L;
    private static net.minecraft.client.multiplayer.ClientLevel lastCrackLevel = null;

    // button shatter
    private static long shatterStartMs = -1L;
    private static int shatterX = 0, shatterY = 0;
    private static final int FRAGS = 18;
    private static final double[] FX = new double[FRAGS];
    private static final double[] FY = new double[FRAGS];
    private static final int[] FCOL = new int[FRAGS];

    private McsmCinematic() {
    }

    // ------------------------------------------------------------------
    // state transitions (called from the proven per-frame hooks)
    // ------------------------------------------------------------------

    /** Called from the HUD hook every in-world frame. */
    public static void tickWorld() {
        Minecraft mc = Minecraft.getInstance();
        net.minecraft.client.multiplayer.ClientLevel level =
                mc == null ? null : mc.level;
        if (level != null && level != lastCrackLevel) {
            lastCrackLevel = level;
            if (McsmExtrasConfig.worldCrackIntro) {
                crackStartMs = System.currentTimeMillis();
            }
        }
        if (level == null) {
            lastCrackLevel = null;
        }
    }

    /**
     * Build #375: the intro plays DURING the Mojang loading scene.
     * Called from the LogoRenderer hook (the startup logo phase, before any
     * screen exists). It consumes the same one-shot boot state as
     * tickMenu(): the sequence starts on the first logo frame and the title
     * screen never replays it afterwards. Any key skips it, exactly like
     * the title version.
     */
    public static boolean tickLogo() {
        if (!McsmExtrasConfig.cinematicBootEnabled) {
            preGameDone = true;
            return false;
        }
        long now = System.currentTimeMillis();
        if (!preGameDone) {
            if (preGameStartMs < 0L) {
                preGameStartMs = now;
            }
            long t = now - preGameStartMs;
            if (t >= PRE_GAME_MS || anyKeyPressed()) {
                preGameDone = true;
                burstStartMs = now;
            }
            return true;
        }
        if (burstStartMs > 0L && now - burstStartMs < BURST_MS) {
            return true;
        }
        return false;
    }

    /** The full boot sequence drawn full-screen (used on the logo scene,
     *  where no Screen object exists yet). */
    public static void drawLogoSequence(GuiGraphicsExtractor g, int w, int h) {
        drawMenuSequenceRaw(g, w, h);
    }

    private static void drawMenuSequenceRaw(GuiGraphicsExtractor g, int w, int h) {
        if (!preGameDone) {
            long t = System.currentTimeMillis() - preGameStartMs;
            if (t >= 0L) {
                drawPreGame(g, w, h, Math.min(1.0D, t / (double) PRE_GAME_MS));
            }
            return;
        }
        if (burstStartMs > 0L) {
            long bt = System.currentTimeMillis() - burstStartMs;
            if (bt >= 0L && bt < BURST_MS) {
                drawBurst(g, w, h, bt / (double) BURST_MS);
            }
        }
    }

    /** Called from the TitleScreen render hook. Returns true while a boot
     *  sequence (pre-game cinematic or command block burst) is playing. */
    public static boolean tickMenu() {
        if (!McsmExtrasConfig.cinematicBootEnabled) {
            preGameDone = true;
            return false;
        }
        long now = System.currentTimeMillis();
        if (!preGameDone) {
            if (preGameStartMs < 0L) {
                preGameStartMs = now;
            }
            long t = now - preGameStartMs;
            if (t >= PRE_GAME_MS || anyKeyPressed()) {
                preGameDone = true;
                burstStartMs = now;
            }
            return true; // pre-game cinematic is playing
        }
        if (burstStartMs > 0L && now - burstStartMs < BURST_MS) {
            return true; // burst is playing
        }
        return false;
    }

    /**
     * Build #380: the boot cinematic is a one-shot that belongs to the
     * startup (Mojang loading) scene. The moment the title screen exists,
     * this is called to mark the sequence fully consumed so it can never
     * bleed onto the main menu or in-game UI (user report: it was
     * "overlapping a bunch of other stuff" after boot). Idempotent.
     */
    public static void markBootDone() {
        // Only consume it if it actually started on the boot scene. If the
        // logo hook never ran (so the sequence is still pristine), leave the
        // state untouched so the menu can still show it once as a fallback -
        // the intro must never silently disappear.
        if (preGameStartMs >= 0L) {
            preGameDone = true;
            burstStartMs = -1L;
        }
    }

    /** State query (no side effects): is a boot sequence currently playing? */
    public static boolean isSequenceActive() {
        if (!preGameDone) {
            return true;
        }
        if (burstStartMs > 0L) {
            return System.currentTimeMillis() - burstStartMs < BURST_MS;
        }
        return false;
    }

    /** Called from the TitleScreen click hook when a menu button is pressed. */
    public static void shatterButton(int cx, int cy) {
        shatterStartMs = System.currentTimeMillis();
        shatterX = cx;
        shatterY = cy;
        long seed = 0x5EEDL;
        for (int i = 0; i < FRAGS; i++) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            double a = (seed >>> 40) / (double) (1L << 24) * 2.0D * Math.PI;
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            double spd = 140.0D + (seed >>> 40) / (double) (1L << 24) * 320.0D;
            FX[i] = Math.cos(a) * spd;
            FY[i] = Math.sin(a) * spd - 60.0D; // slight upward bias
            FCOL[i] = i % 3 == 0 ? 0xFFE8C07A : (i % 3 == 1 ? 0xFFF4EAD0 : 0xFF8A6A2E);
        }
    }

    /** Shatter fragments, drawn from the vanilla Screen hook on ANY screen
     *  so the spray survives the transition the button click triggers. */
    public static void drawShatter(GuiGraphicsExtractor g, Screen screen) {
        if (shatterStartMs < 0L) {
            return;
        }
        long t = System.currentTimeMillis() - shatterStartMs;
        if (t >= SHATTER_MS) {
            shatterStartMs = -1L;
            return;
        }
        float f = (float) (t / (double) SHATTER_MS);
        float alpha = (1.0F - f) * 255.0F;
        for (int i = 0; i < FRAGS; i++) {
            double dx = FX[i] * f;
            double dy = FY[i] * f + 220.0D * f * f; // gravity
            int x = shatterX + (int) dx;
            int y = shatterY + (int) dy;
            int s = 3 + (i % 3);
            int a = (int) (alpha * (0.5D + 0.5D * Math.sin(i * 1.7)));
            if (a <= 0) {
                continue;
            }
            int col = (a << 24) | (FCOL[i] & 0xFFFFFF);
            g.fill(x, y, x + s, y + s, col);
        }
    }

    // ------------------------------------------------------------------
    // in-world: cracks + sky shockwaves
    // ------------------------------------------------------------------

    public static void drawWorldCracks(GuiGraphicsExtractor g, DeltaTracker delta) {
        if (crackStartMs < 0L) {
            return;
        }
        long t = System.currentTimeMillis() - crackStartMs;
        if (t >= CRACK_MS) {
            crackStartMs = -1L;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) {
            return;
        }
        double p = t / (double) CRACK_MS;
        int w = g.guiWidth();
        int h = g.guiHeight();
        int cx = w / 2, cy = h / 2;

        // two giant sky shockwave rings, staggered
        drawRing(g, cx, cy, (int) (p * w * 0.75D), (int) (5.0D * (1.0D - p) + 1), 0x66E8C07A);
        drawRing(g, cx, cy, (int) (Math.max(0.0D, p - 0.18D) * w * 0.95D), (int) (3.0D * (1.0D - p) + 1), 0x55F4EAD0);

        // nine jagged fissures rip outward from the centre
        double reach = easeOut(p) * Math.min(w, h) * 0.62D;
        long seed = 0xC1A44B1L;
        for (int i = 0; i < 9; i++) {
            double ang = i * (2.0D * Math.PI / 9.0D) + 0.35D;
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            double dirJitter = ((seed >>> 40) / (double) (1L << 24) - 0.5D) * 0.5D;
            drawCrack(g, cx, cy, ang + dirJitter, reach * (0.7D + 0.3D * Math.sin(i * 2.3D) + 0.15D), seed);
        }

        // peak flash
        if (p > 0.55D && p < 0.8D) {
            double fp = 1.0D - Math.abs((p - 0.55D) / 0.25D - 1.0D);
            int fa = (int) (fp * 130.0D);
            if (fa > 0) {
                g.fill(0, 0, w, h, (fa << 24) | 0xFFF4EAD0);
            }
        }
        // end fade-out of a warm tint
        if (p > 0.8D) {
            int fa = (int) ((1.0D - p) / 0.2D * 46.0D);
            if (fa > 0) {
                g.fill(0, 0, w, h, (fa << 24) | 0x3A2A5A);
            }
        }
    }

    private static void drawCrack(GuiGraphicsExtractor g, int cx, int cy, double ang, double reach, long seedBase) {
        double x = 0.0D, y = 0.0D;
        double dir = ang;
        long seed = seedBase;
        int segs = 13;
        for (int s = 0; s < segs; s++) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            double jitter = ((seed >>> 40) / (double) (1L << 24) - 0.5D) * 0.55D;
            dir += jitter;
            double segLen = reach / segs * (0.75D + 0.5D * ((seed >>> 20) & 0xFF) / 255.0D);
            double nx = x + Math.cos(dir) * segLen;
            double ny = y + Math.sin(dir) * segLen;
            // glow pass then hot core
            line(g, cx + (int) x, cy + (int) y, cx + (int) nx, cy + (int) ny, 5, 0x44E8C07A);
            line(g, cx + (int) x, cy + (int) y, cx + (int) nx, cy + (int) ny, 2, 0xE0FFF6E0);
            x = nx;
            y = ny;
        }
    }

    // ------------------------------------------------------------------
    // menu: pre-game side-view cinematic + command block burst
    // ------------------------------------------------------------------

    /** Drawn over the TitleScreen. Returns true while the sequence owns the
     *  screen (the menu chrome should stand down). */
    public static void drawMenuSequence(GuiGraphicsExtractor g, Screen screen) {
        drawMenuSequenceRaw(g, screen.width, screen.height);
    }

    private static void drawPreGame(GuiGraphicsExtractor g, int w, int h, double t) {
        net.minecraft.client.gui.Font font = Minecraft.getInstance().font;

        // black frame -> faint deep blue at the horizon
        g.fill(0, 0, w, h, 0xFF010103);
        int horizon = (int) (h * 0.78D);
        g.fillGradient(0, horizon - h / 8, w, horizon, 0x000A0A1E, 0x8814122E);
        // ground
        g.fill(0, horizon, w, h, 0xFF050509);
        // moon
        disc(g, (int) (w * 0.18D), (int) (h * 0.2D), 16, 0x33DCE6FF);
        disc(g, (int) (w * 0.18D), (int) (h * 0.2D), 10, 0xAAE8F0FF);

        // the Wither Storm silhouette: drifts in from the right, grows
        double sx = w * (1.12D - 0.62D * t);
        double syc = h * 0.5D;
        double R = Math.min(w, h) * (0.16D + 0.3D * t);
        stormSilhouette(g, sx, syc, R, t);

        // lightning strikes at t ~0.3 and ~0.62
        for (double lt : new double[]{0.30D, 0.62D}) {
            double dt = t - lt;
            if (dt > 0.0D && dt < 0.06D) {
                double fp = 1.0D - Math.abs(dt / 0.06D - 0.5D) * 2.0D;
                int fa = (int) (fp * 150.0D);
                if (fa > 0) {
                    g.fill(0, 0, w, h, (fa << 24) | 0xFFEAF2FF);
                    long seed = (long) (lt * 1000L) * 0x9E3779B97F4A7C15L;
                    double bx = w * (lt < 0.5D ? 0.3D : 0.68D);
                    double px = bx, py = 0.0D;
                    for (int s = 0; s < 9; s++) {
                        seed = seed * 6364136223846793005L + 1442695040888963407L;
                        double nx = px + ((seed >>> 40) / (double) (1L << 24) - 0.5D) * w * 0.08D;
                        double ny = py + horizon / 9.0D;
                        line(g, (int) px, (int) py, (int) nx, (int) ny, 2, 0xFFF2F6FF);
                        px = nx;
                        py = ny;
                    }
                }
            }
        }

        // title fade
        if (t > 0.66D) {
            double ft = Math.min(1.0D, (t - 0.66D) / 0.3D);
            int a = (int) (ft * 255.0D);
            g.centeredText(font, "MINECRAFT STORY MODE", w / 2, (int) (h * 0.84D), (a << 24) | 0xFFE8C07A);
            int a2 = (int) (ft * 190.0D);
            g.centeredText(font, "THE WITHER STORM SAGA", w / 2, (int) (h * 0.84D) + 14, (a2 << 24) | 0xFF9AA6C4);
            int a3 = (int) ((0.4D + 0.6D * Math.abs(Math.sin(System.currentTimeMillis() * 0.003D))) * 120.0D);
            g.centeredText(font, "press any key to skip", w / 2, h - 14, (a3 << 24) | 0xFF6B7280);
        }
    }

    private static void drawBurst(GuiGraphicsExtractor g, int w, int h, double t) {
        int cx = w / 2, cy = (int) (h * 0.44D);
        double base = Math.min(w, h) * 0.16D;

        if (t < 0.6D) {
            // grow + glow
            double gt = t / 0.6D;
            double s = base * 2.0D * easeOut(gt);
            double pulse = 0.5D + 0.5D * Math.sin(gt * Math.PI * 5.0D);
            drawGlow(g, cx, cy, (int) (s * (1.5D + pulse * 0.8D)), (int) (30 + pulse * 60));
            commandBlock(g, cx, cy, (int) s, 1.0F);
        } else {
            // burst: ring + spokes + spray + flash
            double bt = (t - 0.6D) / 0.4D;
            double ring = easeOut(bt) * Math.min(w, h) * 0.85D;
            drawRing(g, cx, cy, (int) ring, (int) (10.0D * (1.0D - bt) + 2), 0xAAE8C07A);
            long seed = 0xB0A57L;
            for (int i = 0; i < 24; i++) {
                seed = seed * 6364136223846793005L + 1442695040888963407L;
                double a = i * (2.0D * Math.PI / 24.0D) + ((seed >>> 40) / (double) (1L << 24)) * 0.2D;
                double r0 = ring * 0.35D;
                double r1 = ring * (0.85D + 0.15D * bt);
                int al = (int) ((1.0D - bt) * 200.0D);
                if (al > 0) {
                    line(g, cx + (int) (Math.cos(a) * r0), cy + (int) (Math.sin(a) * r0),
                            cx + (int) (Math.cos(a) * r1), cy + (int) (Math.sin(a) * r1),
                            2, (al << 24) | 0xFFF4EAD0);
                }
            }
            for (int i = 0; i < 20; i++) {
                seed = seed * 6364136223846793005L + 1442695040888963407L;
                double a = (seed >>> 40) / (double) (1L << 24) * 2.0D * Math.PI;
                seed = seed * 6364136223846793005L + 1442695040888963407L;
                double spd = base * (1.6D + (seed >>> 20) / 255.0D * 2.4D) * easeOut(bt);
                int s = 3 + i % 3;
                int al = (int) ((1.0D - bt) * 230.0D);
                if (al > 0) {
                    int col = (i % 4 == 0) ? 0xFFE8C07A : (i % 4 == 1 ? 0xFF8AE04A : (i % 4 == 2 ? 0xFFF4EAD0 : 0xFFB8862B));
                    g.fill(cx + (int) (Math.cos(a) * spd), cy + (int) (Math.sin(a) * spd),
                            cx + (int) (Math.cos(a) * spd) + s, cy + (int) (Math.sin(a) * spd) + s, (al << 24) | (col & 0xFFFFFF));
                }
            }
            if (bt < 0.3D) {
                int fa = (int) ((1.0D - bt / 0.3D) * 235.0D);
                g.fill(0, 0, w, h, (fa << 24) | 0xFFFFFFFF);
            }
        }
    }

    /** The command block: front face + isometric top + side, dark frame and
     *  the glowing command symbol. */
    private static void commandBlock(GuiGraphicsExtractor g, int cx, int cy, int s, float glow) {
        if (s < 6) {
            return;
        }
        int frontL = cx - s / 2;
        int frontT = cy - s / 2 + (int) (s * 0.18D);
        int topH = (int) (s * 0.26D);
        int skew = (int) (s * 0.22D);
        int sideW = (int) (s * 0.26D);

        // top face (bright orange), scanline parallelogram
        for (int y = frontT - topH; y < frontT; y++) {
            int off = (int) ((frontT - 1 - y) * (double) skew / topH);
            g.fill(frontL + off, y, frontL + s + off, y + 1, 0xFFD8A63C);
        }
        // side face (dark)
        for (int x = 0; x < sideW; x++) {
            int drop = (int) (x * (double) topH / sideW);
            g.fill(frontL + s + x, frontT - topH + drop, frontL + s + x + 1, frontT + s, 0xFF8A5E1E);
        }
        // front face
        g.fill(frontL, frontT, frontL + s, frontT + s, 0xFFB8862B);
        // frame
        g.fill(frontL, frontT, frontL + s, frontT + 2, 0xFF6B4A14);
        g.fill(frontL, frontT + s - 2, frontL + s, frontT + s, 0xFF6B4A14);
        g.fill(frontL, frontT, frontL + 2, frontT + s, 0xFF6B4A14);
        g.fill(frontL + s - 2, frontT, frontL + s, frontT + s, 0xFF6B4A14);
        // the command symbol: dark plate + pulsing green core
        int p = s / 4;
        g.fill(cx - p / 2, cy - p / 2 + (int) (s * 0.18D), cx + p / 2, cy + p / 2 + (int) (s * 0.18D), 0xFF3A2A0C);
        int coreA = (int) (120 + 100 * glow * Math.abs(Math.sin(System.currentTimeMillis() * 0.004D)));
        g.fill(cx - p / 4, cy - p / 4 + (int) (s * 0.18D), cx + p / 4, cy + p / 4 + (int) (s * 0.18D), (coreA << 24) | 0xFF8AE04A);
    }

    // ------------------------------------------------------------------
    // primitives (all proven fill-family calls)
    // ------------------------------------------------------------------

    static void line(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int w, int color) {
        int dx = x2 - x1, dy = y2 - y1;
        int steps = (int) (Math.max(Math.abs(dx), Math.abs(dy)) / 3.0D) + 1;
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0.0D : i / (double) steps;
            int x = (int) (x1 + dx * t);
            int y = (int) (y1 + dy * t);
            g.fill(x - w / 2, y - w / 2, x + w / 2, y + w / 2, color);
        }
    }

    static void drawRing(GuiGraphicsExtractor g, int cx, int cy, int r, int w, int color) {
        if (r < 2 || w <= 0) {
            return;
        }
        int steps = 48;
        for (int i = 0; i < steps; i++) {
            double a0 = i * (2.0D * Math.PI / steps);
            double a1 = (i + 1.4) * (2.0D * Math.PI / steps);
            line(g, cx + (int) (Math.cos(a0) * r), cy + (int) (Math.sin(a0) * r),
                    cx + (int) (Math.cos(a1) * r), cy + (int) (Math.sin(a1) * r), w, color);
        }
    }

    static void drawGlow(GuiGraphicsExtractor g, int cx, int cy, int r, int alpha) {
        int rings = 6;
        for (int i = rings; i > 0; i--) {
            int rr = r * i / rings;
            int a = alpha * (rings - i + 1) / (rings * 2);
            if (a <= 0) {
                continue;
            }
            g.fill(cx - rr, cy - rr, cx + rr, cy + rr, (a << 24) | 0xFFE8C07A);
        }
    }

    static void disc(GuiGraphicsExtractor g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int rr = r * r - dy * dy;
            int dx = (int) Math.sqrt(Math.max(0, rr));
            g.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }

    /** The side-view storm silhouette: a dark blob body with a rippling
     *  tentacle skirt, rim-lit purple, and two faint gold eyes. */
    static void stormSilhouette(GuiGraphicsExtractor g, double cx, double cy, double R, double t) {
        int step = 2;
        int bodyH = (int) (R * 0.85D);
        for (int y = -bodyH; y <= bodyH; y += step) {
            double v = y / (double) bodyH;
            double wfac = Math.sqrt(Math.max(0.0D, 1.0D - v * v));
            double wpx = R * wfac * (0.92D + 0.06D * Math.sin(y * 0.11D + t * 5.0D));
            int ypx = (int) (cy + y);
            // rim light
            if (wfac > 0.2D) {
                g.fill((int) (cx - wpx - 2), ypx, (int) (cx - wpx + 2), ypx + step, 0x2A3A2A5A);
                g.fill((int) (cx + wpx - 2), ypx, (int) (cx + wpx + 2), ypx + step, 0x2A3A2A5A);
            }
            g.fill((int) (cx - wpx), ypx, (int) (cx + wpx), ypx + step, 0xFF04040A);
        }
        // tentacle skirt: rippling columns below the body
        int cols = 9;
        for (int c = 0; c < cols; c++) {
            double fx = (c / (double) (cols - 1) - 0.5D) * 2.0D;
            double len = R * (1.1D - 0.45D * Math.abs(fx)) * (0.8D + 0.2D * Math.sin(t * 4.0D + c * 1.3D));
            double sway = Math.sin(t * 3.0D + c * 0.9D) * R * 0.16D;
            for (int s = 0; s < 10; s++) {
                double tt = s / 9.0D;
                double px = cx + fx * R * 0.8D + sway * tt * tt;
                double py = cy + bodyH * 0.55D + tt * len;
                int wpx = (int) (2.0D + (1.0D - tt) * R * 0.06D);
                g.fill((int) (px - wpx), (int) py, (int) (px + wpx), (int) py + step + 1, 0xFF050510);
            }
        }
        // eyes: two faint gold glints
        double pulse = 0.6D + 0.4D * Math.sin(t * 7.0D);
        int ea = (int) (150 * pulse);
        g.fill((int) (cx - R * 0.24D), (int) (cy - R * 0.18D), (int) (cx - R * 0.24D + 4), (int) (cy - R * 0.18D + 3), (ea << 24) | 0xFFE8C07A);
        g.fill((int) (cx + R * 0.16D), (int) (cy - R * 0.18D), (int) (cx + R * 0.16D + 4), (int) (cy - R * 0.18D + 3), (ea << 24) | 0xFFE8C07A);
    }

    private static double easeOut(double t) {
        return 1.0D - (1.0D - t) * (1.0D - t);
    }

    private static boolean anyKeyPressed() {
        try {
            Minecraft mc = Minecraft.getInstance();
            Object windowObj = null;
            for (java.lang.reflect.Method m : Minecraft.class.getMethods()) {
                if (m.getParameterCount() == 0 && m.getName().equals("getWindow")) {
                    windowObj = m.invoke(mc);
                    break;
                }
            }
            if (windowObj == null) {
                return false;
            }
            long handle = 0L;
            for (java.lang.reflect.Method m : windowObj.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && m.getName().equals("getWindow")) {
                    Object v = m.invoke(windowObj);
                    if (v instanceof Number) {
                        handle = ((Number) v).longValue();
                    }
                    break;
                }
            }
            if (handle == 0L) {
                return false;
            }
            Class<?> glfw = Class.forName("org.lwjgl.glfw.GLFW");
            java.lang.reflect.Method getKey = glfw.getMethod("glfwGetKey", long.class, int.class);
            int press = ((Number) glfw.getField("GLFW_PRESS").get(null)).intValue();
            int[] keys = {
                    (Integer) glfw.getField("GLFW_KEY_ENTER").get(null),
                    (Integer) glfw.getField("GLFW_KEY_SPACE").get(null),
                    (Integer) glfw.getField("GLFW_KEY_ESCAPE").get(null),
                    (Integer) glfw.getField("GLFW_KEY_R").get(null),
                    (Integer) glfw.getField("GLFW_KEY_P").get(null)
            };
            for (int k : keys) {
                if (((Number) getKey.invoke(null, handle, k)).intValue() == press) {
                    return true;
                }
            }
            return false;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
