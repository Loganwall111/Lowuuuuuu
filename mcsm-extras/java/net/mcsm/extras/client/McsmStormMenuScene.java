package net.mcsm.extras.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

/**
 * Build #375: the literal 3D Wither Storm for the main menu.
 *
 * The panorama is deleted and replaced with the real creature: the blocky
 * three-headed command-block wither on its sandstone platform, rendered as
 * genuine 3D geometry on a turntable - drag to spin it, scroll to zoom.
 * Pure GUI-graphics rectangles (the exact primitive set the base config
 * screen's 3D preview proves on 26.2): yaw rotation + pitch projection,
 * painter's-algorithm depth sort, per-face light shading, a drifting field
 * of black debris cubes and a soft dark storm-core wash behind it.
 *
 *   - auto-rotates slowly (the "alive" feel);
 *   - left-drag anywhere on the menu orbits it (dragX -> yaw, dragY -> a
 *     little pitch);
 *   - the mouse wheel zooms (0.45x - 2.2x);
 *   - a gentle vertical bob so it never looks frozen.
 */
public final class McsmStormMenuScene {

    /** Model units: 16 units = 1 block (the PartPose pixel scale). */
    private static final float U = 1.0F / 16.0F;

    // orbit state (radians) -------------------------------------------------
    private static float yaw = 0.9F;
    private static float pitch = 14.0F;      // degrees, like the base preview
    private static float zoom = 1.0F;
    private static float bobPhase = 0.0F;
    private static long lastNs = 0L;

    private McsmStormMenuScene() {
    }

    // ---- orbit control (called from the title screen hooks) --------------

    public static void orbitBy(double dragX, double dragY) {
        yaw += (float) (dragX * 0.011D);
        pitch = Mth.clamp(pitch + (float) (dragY * 0.05D), 2.0F, 40.0F);
    }

    public static void zoomBy(double amount) {
        zoom = Mth.clamp(zoom * (1.0F - (float) (amount * 0.08D)), 0.45F, 2.2F);
    }

    public static boolean zoomedIn() {
        return zoom > 1.12F;
    }

    /** Advance the idle rotation + bob. dtSec is clamped internally. */
    public static void tick() {
        long now = System.nanoTime();
        double dt = lastNs == 0L ? 0.016D : Mth.clamp((now - lastNs) / 1.0E9D, 0.0D, 0.1D);
        lastNs = now;
        yaw += (float) dt * 0.12F;            // slow stately turntable
        bobPhase += (float) dt;
    }

    // ---- the blocky wither geometry ---------------------------------------
    // Each box: centre (x, y, z) in model units + half extents (hx, hy, hz).
    // y grows up. Colours: (r, g, b) body tone; the face boxes carry the
    // cyan eye/teeth detail on their front face.

    private record Box(float cx, float cy, float cz, float hx, float hy, float hz,
                       int r, int g, int b, boolean face) {
    }

    private static final Box[] BOXES = buildModel();

    private static Box[] buildModel() {
        Box[] b = new Box[22];
        int i = 0;
        // sandstone command-block platform (the reference's base)
        b[i++] = new Box(0, 2 * U, 0, 9 * U, 1 * U, 9 * U, 186, 172, 124, false);
        b[i++] = new Box(0, 4 * U, 0, 7.5F * U, 1 * U, 7.5F * U, 168, 154, 110, false);
        // the flat command-block body plate (20x3x3)
        b[i++] = new Box(0, 6.5F * U, 1 * U, 10 * U, 1.5F * U, 1.5F * U, 24, 22, 30, false);
        // the pillar core (3x10x3)
        b[i++] = new Box(0, 14 * U, 1 * U, 1.5F * U, 5 * U, 1.5F * U, 30, 27, 38, false);
        // arm beams out to the nether-brick pillars
        b[i++] = new Box(-7.5F * U, 11 * U, 1 * U, 6 * U, 1 * U, 1 * U, 28, 25, 34, false);
        b[i++] = new Box(7.5F * U, 11 * U, 1 * U, 6 * U, 1 * U, 1 * U, 28, 25, 34, false);
        // nether-brick end pillars (the red accents)
        b[i++] = new Box(-13 * U, 9.5F * U, 1 * U, 0.6F * U, 2.5F * U, 0.6F * U, 96, 38, 48, false);
        b[i++] = new Box(13 * U, 9.5F * U, 1 * U, 0.6F * U, 2.5F * U, 0.6F * U, 96, 38, 48, false);
        // the main face head (8x8x8) - carries the eyes + teeth
        b[i++] = new Box(0, 22 * U, 1 * U, 4 * U, 4 * U, 4 * U, 34, 31, 44, true);
        // the two side heads (6x6x6)
        b[i++] = new Box(-9 * U, 20 * U, 0.5F * U, 3 * U, 3 * U, 3 * U, 30, 27, 40, false);
        b[i++] = new Box(9 * U, 20 * U, 0.5F * U, 3 * U, 3 * U, 3 * U, 30, 27, 40, false);
        return java.util.Arrays.copyOf(b, i);
    }

    // ---- projection --------------------------------------------------------

    private static double rotX(double x, double z, double cos, double sin) {
        return x * cos - z * sin;
    }

    private static double rotZ(double x, double z, double cos, double sin) {
        return x * sin + z * cos;
    }

    /** Projects a model point to screen space. Returns {x, y, depth}. */
    private static double[] project(double mx, double my, double mz,
                                    double cosY, double sinY, double cosP, double sinP,
                                    int cx, int cy, double scale) {
        double rx = rotX(mx, mz, cosY, sinY);
        double rz = rotZ(mx, mz, cosY, sinY);
        double sx = cx + scale * (-rx);
        double sy = cy + scale * (-(my * cosP - rz * sinP));
        return new double[]{sx, sy, rz};
    }

    // ---- the draw ----------------------------------------------------------

    public static void draw(GuiGraphicsExtractor g, int w, int h, float partialTick) {
        tick();
        int cx = w / 2;
        int cy = h / 2 + 14 + (int) (Math.sin(bobPhase * 0.8D) * 4.0D);
        double scale = Math.min(w / 34.0D, h / 30.0D) * zoom;
        double cosY = Math.cos(yaw);
        double sinY = Math.sin(yaw);
        double cosP = Math.cos(Math.toRadians(pitch));
        double sinP = Math.sin(Math.toRadians(pitch));

        drawCoreWash(g, cx, cy, (float) scale);
        drawDebris(g, cx, cy, (float) scale, cosY, sinY, cosP, sinP);

        // painter's algorithm: far boxes first
        Integer[] order = new Integer[BOXES.length];
        for (int i = 0; i < BOXES.length; i++) {
            order[i] = i;
        }
        double[] depth = new double[BOXES.length];
        for (int i = 0; i < BOXES.length; i++) {
            Box box = BOXES[i];
            depth[i] = project(box.cx, box.cy, box.cz, cosY, sinY, cosP, sinP, cx, cy, scale)[2];
        }
        java.util.Arrays.sort(order, (a, b) -> Double.compare(depth[b], depth[a]));

        for (int idx : order) {
            drawBox(g, BOXES[idx], cosY, sinY, cosP, sinP, cx, cy, scale);
        }
    }

    private static void drawBox(GuiGraphicsExtractor g, Box box,
                                double cosY, double sinY, double cosP, double sinP,
                                int cx, int cy, double scale) {
        float bob = 0.0F;
        double top = box.cy + box.hy;
        double bot = box.cy - box.hy;

        // ---- the two vertical faces that face the camera -------------------
        // front (z + hz) and back (z - hz) project to axis-aligned rectangles;
        // exactly one side (x+ or x-) is also camera-facing.
        double[][] corners = {
                {box.cx - box.hx, top, box.cz + box.hz},
                {box.cx + box.hx, top, box.cz + box.hz},
                {box.cx - box.hx, bot, box.cz + box.hz},
                {box.cx + box.hx, bot, box.cz + box.hz},
        };
        double[] p0 = project(corners[0][0], corners[0][1], corners[0][2], cosY, sinY, cosP, sinP, cx, cy, scale);
        double[] p1 = project(corners[1][0], corners[1][1], corners[1][2], cosY, sinY, cosP, sinP, cx, cy, scale);
        double[] p3 = project(corners[3][0], corners[3][1], corners[3][2], cosY, sinY, cosP, sinP, cx, cy, scale);
        double frontDepth = (p0[2] + p1[2]) / 2.0D;
        int frontA = shade(box.r, box.g, box.b, 0.92F);
        g.fill((int) Math.min(p0[0], p1[0]), (int) Math.max(p0[1], p3[1]),
                (int) Math.max(p0[0], p1[0]), (int) Math.min(p0[1], p3[1]), frontA);
        if (box.face) {
            drawFaceDetail(g, (int) Math.min(p0[0], p1[0]), (int) Math.max(p0[1], p3[1]),
                    (int) Math.max(p0[0], p1[0]), (int) Math.min(p0[1], p3[1]), frontDepth);
        }

        // the visible side face: whichever of x+ / x- has a positive
        // camera-facing normal after the yaw rotation
        double sideX = (sinY >= 0.0D) ? box.cx - box.hx : box.cx + box.hx;
        double[][] sc = {
                {sideX, top, box.cz - box.hz},
                {sideX, top, box.cz + box.hz},
                {sideX, bot, box.cz - box.hz},
                {sideX, bot, box.cz + box.hz},
        };
        double[] q0 = project(sc[0][0], sc[0][1], sc[0][2], cosY, sinY, cosP, sinP, cx, cy, scale);
        double[] q1 = project(sc[1][0], sc[1][1], sc[1][2], cosY, sinY, cosP, sinP, cx, cy, scale);
        double[] q3 = project(sc[3][0], sc[3][1], sc[3][2], cosY, sinY, cosP, sinP, cx, cy, scale);
        double sideDepth = (q0[2] + q1[2]) / 2.0D;
        if (sideDepth < frontDepth) {
            g.fill((int) Math.min(q0[0], q1[0]), (int) Math.max(q0[1], q3[1]),
                    (int) Math.max(q0[0], q1[0]), (int) Math.min(q0[1], q3[1]),
                    shade(box.r, box.g, box.b, 0.62F));
        }

        // ---- the top face: a horizontal strip (pitch-foreshortened) --------
        double[][] tc = {
                {box.cx - box.hx, top, box.cz - box.hz},
                {box.cx + box.hx, top, box.cz + box.hz},
        };
        double[] t0 = project(tc[0][0], tc[0][1], tc[0][2], cosY, sinY, cosP, sinP, cx, cy, scale);
        double[] t1 = project(tc[1][0], tc[1][1], tc[1][2], cosY, sinY, cosP, sinP, cx, cy, scale);
        double[] t2 = project(box.cx - box.hx, top, box.cz + box.hz, cosY, sinY, cosP, sinP, cx, cy, scale);
        double[] t3 = project(box.cx + box.hx, top, box.cz - box.hz, cosY, sinY, cosP, sinP, cx, cy, scale);
        int tx0 = (int) Math.min(Math.min(t0[0], t2[0]), Math.min(t1[0], t3[0]));
        int tx1 = (int) Math.max(Math.max(t0[0], t2[0]), Math.max(t1[0], t3[0]));
        int ty0 = (int) Math.min(Math.min(t0[1], t2[1]), Math.min(t1[1], t3[1]));
        int ty1 = (int) Math.max(Math.max(t0[1], t2[1]), Math.max(t1[1], t3[1]));
        if (tx1 - tx0 > 1 && ty1 - ty0 > 0) {
            g.fill(tx0, ty0, tx1, ty1, shade(box.r, box.g, box.b, 1.18F));
        }
    }

    /** The cyan eyes + white teeth ring on the main head's front face. */
    private static void drawFaceDetail(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, double depth) {
        if (depth > 0.0D) {
            return; // face pointing away: no face detail
        }
        int fw = x1 - x0;
        int fh = y1 - y0;
        if (fw < 12 || fh < 12) {
            return;
        }
        int eyeR = Math.max(2, fw / 9);
        int eyeY = y0 + fh / 4;
        int exL = x0 + fw / 5;
        int exR = x1 - fw / 5 - eyeR;
        g.fill(exL, eyeY, exL + eyeR, eyeY + eyeR, 0xFF9FEFFF);
        g.fill(exR, eyeY, exR + eyeR, eyeY + eyeR, 0xFF9FEFFF);
        // the teeth band
        int ty = y0 + fh * 2 / 3;
        int th = Math.max(2, fh / 12);
        g.fill(x0 + fw / 6, ty, x1 - fw / 6, ty + th, 0xFFDFFCFF);
    }

    /** Soft dark storm-core wash behind the creature (the Telltale blur,
     *  flattened into the menu backdrop - banded like the base preview). */
    private static void drawCoreWash(GuiGraphicsExtractor g, int cx, int cy, float scale) {
        int bands = 9;
        int rx = (int) (scale * 24.0D);
        int ry = (int) (scale * 15.0D);
        for (int i = bands - 1; i >= 0; i--) {
            float t = i / (float) (bands - 1);
            int a = (int) (34.0F * (1.0F - t) * (1.0F - t));
            if (a <= 0) {
                continue;
            }
            int x0 = cx - (int) (rx * (1.0F - 0.25F * t)) / 2;
            int x1 = cx + (int) (rx * (1.0F - 0.25F * t)) / 2;
            int y0 = cy - (int) (ry * (1.0F - 0.25F * t));
            int y1 = cy + (int) (ry * (1.0F - 0.35F * t));
            g.fill(x0, y0, x1, y1, (0xFF04030A & 0x00FFFFFF) | (a << 24));
        }
    }

    /** Drifting black debris cubes - hash-based, stateless, like the
     *  in-game Phase 6 particle field. */
    private static void drawDebris(GuiGraphicsExtractor g, int cx, int cy, float scale,
                                   double cosY, double sinY, double cosP, double sinP) {
        float tt = bobPhase * 0.5F;
        for (int i = 0; i < 26; i++) {
            float sd = i * 0.618034F;
            float cyc = fract(tt * 0.05F + fract(sd));
            float ang = fract(sd * 3.1F) * 6.28318F + tt * 0.06F;
            double mx = Math.cos(ang) * (18.0D + 14.0D * fract(sd * 5.7D)) * cyc + (fract(sd * 9.3F) - 0.5D) * 10.0D;
            double my = 26.0D + Math.sin(ang) * (10.0D + 8.0D * fract(sd * 7.1D)) - cyc * 26.0D;
            double mz = Math.sin(ang) * (16.0D + 12.0D * fract(sd * 4.3D));
            double[] p = project(mx, my, mz, cosY, sinY, cosP, sinP, cx, cy, scale);
            int s = Math.max(1, (int) (scale * (1.2D + 1.4D * fract(sd * 7.3D))));
            int a = (int) (150.0F * (1.0F - cyc * 0.8F));
            if (a > 8) {
                g.fill((int) p[0] - s / 2, (int) p[1] - s / 2, (int) p[0] + s / 2, (int) p[1] + s / 2,
                        (0xFF0A0810 & 0x00FFFFFF) | (a << 24));
            }
        }
    }

    private static float fract(float x) {
        return x - (float) Math.floor(x);
    }

    private static int shade(int r, int g, int b, float f) {
        return 0xFF000000
                | (Mth.clamp((int) (r * f), 0, 255) << 16)
                | (Mth.clamp((int) (g * f), 0, 255) << 8)
                | Mth.clamp((int) (b * f), 0, 255);
    }
}
