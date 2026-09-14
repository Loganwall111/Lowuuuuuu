package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Devouring Storms: Main menu overhaul with Image 3 silver pixel border frame.
 *
 * BUILD #374 — STORY MODE ACCURATE MAIN MENU (the whole section). The
 * backdrop is the base's own OG Story Mode cube panorama + OG sun/moon (the
 * in-scene Wither Storm shot — the standing "panorama + sun/moon OG"
 * order), no longer replaced by a procedural scene. On top of it this mixin
 * adds the Telltale episodic framing: a light readability vignette, dark
 * translucent panel boxes with thin edges behind every menu button (gold
 * frame + corner ticks on hover), the base story-mode banner (top), the
 * silver pixel border frame, and the cinematic bottom bar with the saga
 * line + build number. Toggle: "Cinematic Story-Mode Menu" in the Story
 * Mode Console (off = the plain gradient). The #371 procedural scene
 * helpers remain in this file as reference material.
 */
@Mixin(TitleScreen.class)
public abstract class McsmTitleOverhaulMixin extends Screen {

    /** The DS title icon (new menu asset, 128x128). */
    private static final net.minecraft.resources.Identifier DS_ICON =
            net.minecraft.resources.Identifier.fromNamespaceAndPath("mcsm", "menu/ds_icon.png");

    /** Per-button animation state (Build #375 3D button effects). */
    private static final java.util.Map<Object, Boolean> MC$HOVER = new java.util.IdentityHashMap<>();
    private static final java.util.Map<Object, Long> MC$HOVER_MS = new java.util.IdentityHashMap<>();
    private static final java.util.Map<Object, Long> MC$PRESS_MS = new java.util.IdentityHashMap<>();

    /** Storm turntable input state (GLFW poll, Build #375). */
    private static double MC$CUR_X = -1.0D;
    private static double MC$CUR_Y = -1.0D;

    protected McsmTitleOverhaulMixin(Component title) {
        super(title);
    }

    @Inject(method = "added", at = @At("TAIL"))
    private void dabyws$menuOpenSound(CallbackInfo ci) {
        try {
            net.mcsm.extras.client.McsmButtonSounds.menuOpen();
        } catch (Throwable ignored) {
            // sound only
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void dabyws$resetTurntableInput(CallbackInfo ci) {
        MC$CUR_X = -1.0D;
        MC$CUR_Y = -1.0D;
    }

    /**
     * Build #375: the turntable's mouse input. Screen/TitleScreen do not
     * declare mouseDragged/mouseScrolled in 26.2 (they are ContainerEvent
     * Handler interface defaults), so instead of a fragile interface mixin
     * this polls the cursor + wheel straight from GLFW - the exact
     * reflection pattern the console hotkey (McsmQuickConfigKeyMixin)
     * already proves on this version:
     *   - left-drag anywhere  -> orbit the storm (yaw + a little pitch)
     *   - mouse wheel         -> zoom (0.45x - 2.2x)
     * Called once per frame from the title's render hook.
     */
    private static void mcsm$pollStormInput() {
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
                return;
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
                return;
            }
            Class<?> glfw = Class.forName("org.lwjgl.glfw.GLFW");
            long[] pos = new long[2];
            glfw.getMethod("glfwGetCursorPos", long.class, long[].class)
                    .invoke(null, handle, pos);
            double sx = pos[0];
            double sy = pos[1];

            // wheel -> zoom (GLFW accumulates the delta since last read)
            Method scrollM = glfw.getMethod("glfwGetScrollDelta", long.class);
            double scroll = ((Number) scrollM.invoke(null, handle)).doubleValue();
            if (Math.abs(scroll) > 0.001D) {
                net.mcsm.extras.client.McsmStormMenuScene.zoomBy(scroll);
            }

            // left-drag -> orbit; a huge one-frame jump (window focus,
            // cursor teleport) is dropped so it never yanks the turntable
            int press = ((Number) glfw.getField("GLFW_PRESS").get(null)).intValue();
            int left = ((Number) glfw.getMethod("glfwGetMouseButton", long.class, int.class)
                    .invoke(null, handle, 0)).intValue();
            if (left == press && MC$CUR_X >= 0.0D) {
                double dx = sx - MC$CUR_X;
                double dy = sy - MC$CUR_Y;
                if (Math.abs(dx) < 90.0D && Math.abs(dy) < 90.0D) {
                    net.mcsm.extras.client.McsmStormMenuScene.orbitBy(dx, dy);
                }
            }
            MC$CUR_X = sx;
            MC$CUR_Y = sy;
        } catch (Throwable ignored) {
            MC$CUR_X = -1.0D;
            MC$CUR_Y = -1.0D;
        }
    }

    /**
     * Build #375 -- THE PANORAMA IS DELETED. The standing "OG panorama"
     * order is reversed: the main menu's backdrop is now the LITERAL 3D
     * Wither Storm (McsmStormMenuScene) - a genuine blocky three-headed
     * model on a turntable you can drag to spin and scroll to zoom - over a
     * deep storm-space field. The base cube panorama never draws at all.
     */
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void dabyws$stormBackdrop(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        ci.cancel(); // the panorama is gone, on or off
        int w = this.width;
        int h = this.height;
        g.fillGradient(0, 0, w, h, 0xFF07050E, 0xFF0B0716);
        g.fillGradient(0, h * 2 / 3, w, h, 0x00000000, 0x552A1A4A);
    }

    /**
     * Drawn AFTER the base backdrop, BEFORE the widgets: the Telltale
     * episodic framing. Panels sit behind the vanilla buttons so the menu
     * reads like a Story Mode episode select: dark boxes, thin edge, gold
     * frame on hover.
     */
    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void dabyws$storyModeFraming(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        int w = this.width;
        int h = this.height;

        // turntable input: left-drag orbits, wheel zooms (per-frame poll)
        mcsm$pollStormInput();

        // THE 3D WITHER STORM - the literal scrollable/movable creature
        // (drag to orbit, scroll to zoom), replacing the deleted panorama.
        // Unconditional: it IS the main-menu backdrop now.
        try {
            net.mcsm.extras.client.McsmStormMenuScene.draw(g, w, h, partialTick);
        } catch (Throwable ignored) {
            // the menu creature must never break a frame
        }

        if (!McsmExtrasConfig.storyMenuBackdrop) {
            return; // panels/chrome are the toggleable part
        }

        // readability vignette (kept light so the OG panorama stays vivid)
        g.fillGradient(0, 0, w, 42, 0x66030208, 0x00030208);
        g.fillGradient(0, h - 96, w, h - 32, 0x00030208, 0xAA030208);
        g.fillGradient(0, 0, 26, h, 0x44030208, 0x00030208);
        g.fillGradient(w - 26, 0, w, h, 0x00030208, 0x44030208);

        // episodic panels behind every menu button
        long nowMs = System.currentTimeMillis();
        for (Object child : this.children()) {
            if (!(child instanceof net.minecraft.client.gui.components.AbstractButton)) {
                continue;
            }
            net.minecraft.client.gui.components.AbstractButton b =
                    (net.minecraft.client.gui.components.AbstractButton) child;
            if (!b.visible || !b.active) {
                continue;
            }
            boolean hov = b.isHovered();
            if (hov && !Boolean.TRUE.equals(MC$HOVER.get(b))) {
                MC$HOVER_MS.put(b, nowMs);
                try {
                    net.mcsm.extras.client.McsmButtonSounds.hover();
                } catch (Throwable ignored) {
                }
            }
            MC$HOVER.put(b, hov);
            if (!hov) {
                MC$HOVER_MS.remove(b);
            }

            // 3D lift: the panel rises 2px and gains a drop shadow on hover
            int lift = hov ? 2 : 0;
            int px = b.getX() - 10;
            int py = b.getY() - 5 - lift;
            int pw = b.getWidth() + 20;
            int ph = b.getHeight() + 10;
            if (hov) {
                g.fill(px + 2, py + ph + 2, px + pw + 2, py + ph + 3, 0x55000000);
            }
            g.fill(px, py, px + pw, py + ph, hov ? 0xF0171B24 : 0xE60A0C11);
            int edge = hov ? 0xFFD9A441 : 0xFF2A3140;
            // pulsing glow frame while hovered (the "crazy cool" idle energy)
            if (hov) {
                long sinceHover = nowMs - (MC$HOVER_MS.getOrDefault(b, nowMs));
                float pulse = (float) (Math.sin(sinceHover * 0.012D) * 0.5D + 0.5D);
                int glowA = (int) (40 + pulse * 70);
                g.fill(px - 2, py - 2, px + pw + 2, py - 1, (glowA << 24) | 0xFFD9A441);
                g.fill(px - 2, py + ph + 1, px + pw + 2, py + ph + 2, (glowA << 24) | 0xFFD9A441);
                g.fill(px - 2, py - 2, px - 1, py + ph + 2, (glowA << 24) | 0xFF9FEFFF);
                g.fill(px + pw + 1, py - 2, px + pw + 2, py + ph + 2, (glowA << 24) | 0xFF9FEFFF);
            }
            g.fill(px, py, px + pw, py + 1, edge);
            g.fill(px, py + ph - 1, px + pw, py + ph, edge);
            g.fill(px, py, px + 1, py + ph, edge);
            g.fill(px + pw - 1, py, px + pw, py + ph, edge);
            if (hov) {
                // gold corner ticks — the Telltale hover signature
                g.fill(px, py, px + 7, py + 2, 0xFFD9A441);
                g.fill(px, py, px + 2, py + 7, 0xFFD9A441);
                g.fill(px + pw - 7, py, px + pw, py + 2, 0xFFD9A441);
                g.fill(px + pw - 2, py, px + pw, py + 7, 0xFFD9A441);
                g.fill(px, py + ph - 2, px + 7, py + ph, 0xFFD9A441);
                g.fill(px, py + ph - 7, px + 2, py + ph, 0xFFD9A441);
                g.fill(px + pw - 7, py + ph - 2, px + pw, py + ph, 0xFFD9A441);
                g.fill(px + pw - 2, py + ph - 7, px + pw, py + ph, 0xFFD9A441);
                // sparkle burst on hover-in (700ms, hash-driven, stateless)
                long sparkT = nowMs - MC$HOVER_MS.getOrDefault(b, nowMs);
                if (sparkT >= 0L && sparkT < 700L) {
                    dabyws$sparkles(g, px, py, pw, ph, sparkT);
                }
            }
            // press flash: a bright gold wash for 160ms after the click
            Long press = MC$PRESS_MS.get(b);
            if (press != null) {
                long pt = nowMs - press;
                if (pt >= 0L && pt < 160L) {
                    float fade = 1.0F - (float) (pt / 160.0D);
                    int a = (int) (fade * 120.0F);
                    g.fill(px, py, px + pw, py + ph, (a << 24) | 0xFFFFF0CE);
                } else {
                    MC$PRESS_MS.remove(b);
                }
            }
        }
    }

    /** Eight hash-driven sparkle motes flying off the panel corners,
     *  fading over the burst lifetime. Gold + cyan, Telltale style. */
    private static void dabyws$sparkles(GuiGraphicsExtractor g, int px, int py, int pw, int ph, long tMs) {
        float t = (float) (tMs / 700.0D);
        int[] cornersX = {px, px + pw, px, px + pw, px + pw / 2, px + pw / 2, px + 8, px + pw - 8};
        int[] cornersY = {py, py, py + ph, py + ph, py, py + ph, py, py + ph};
        for (int i = 0; i < 8; i++) {
            long seed = (i + 1) * 0x9E3779B97F4A7CL;
            double ang = ((seed >>> 40) / (double) (1L << 24)) * 6.28318D + i * 0.8D;
            double spd = 26.0D + ((seed >>> 20) & 0xFF) / 255.0D * 30.0D;
            double sx = cornersX[i] + Math.cos(ang) * spd * t;
            double sy = cornersY[i] + Math.sin(ang) * spd * t + 8.0D * t * t;
            int a = (int) (200.0F * (1.0F - t));
            if (a <= 0) {
                continue;
            }
            int rgb = (i % 3 == 0) ? 0xFF9FEFFF : ((i % 3 == 1) ? 0xFFD9A441 : 0xFFF4EAD0);
            g.fill((int) sx, (int) sy, (int) sx + 2, (int) sy + 2, (a << 24) | rgb);
        }
    }

    // ------------------------------------------------------------------
    // BUILD #371 — the cinematic scene
    // ------------------------------------------------------------------

    private static void dabyws$paintCinematic(GuiGraphicsExtractor g, int w, int h) {
        double t = System.currentTimeMillis() * 0.001D;
        int horizon = h * 72 / 100;

        // 1. sky: deep space indigo -> violet, ember at the horizon
        g.fillGradient(0, 0, w, h / 3, 0xFF050310, 0xFF0E0824);
        g.fillGradient(0, h / 3, w, h * 2 / 3, 0xFF0E0824, 0xFF22104A);
        g.fillGradient(0, h * 2 / 3, w, horizon, 0xFF22104A, 0xFF4A1E5C);
        g.fillGradient(0, horizon - h / 20, w, horizon, 0x006A2A5A, 0xAA8A3A5E);
        // ground below the horizon
        g.fillGradient(0, horizon, w, h, 0xFF0A0616, 0xFF030208);

        // 2. drifting nebula wisps
        dabyws$nebulaBand(g, w, h * 16 / 100, h * 9 / 100, 0x265A2A8A, t, 0.0D);
        dabyws$nebulaBand(g, w, h * 26 / 100, h * 12 / 100, 0x1E2A6A8A, t, 2.1D);
        dabyws$nebulaBand(g, w, h * 44 / 100, h * 10 / 100, 0x1A6A3A6A, t, 4.3D);

        // 3. stars (three brightness tiers, twinkle)
        dabyws$stars(g, w, (int) (horizon * 0.94), 150, t);

        // 4. cool moon (kept clear of the vanilla logo and the top banner)
        dabyws$disc(g, (int) (w * 0.30), (int) (h * 0.17), 22, 0x18C8D8FF);
        dabyws$disc(g, (int) (w * 0.30), (int) (h * 0.17), 13, 0xFFDCE6FF);
        int mx = (int) (w * 0.30);
        int my = (int) (h * 0.17);
        g.fill(mx - 5, my - 5, mx - 2, my - 2, 0xBBA9BCE6);
        g.fill(mx + 1, my + 1, mx + 5, my + 4, 0xBBA9BCE6);
        g.fill(mx - 6, my + 3, mx - 4, my + 5, 0xBBA9BCE6);

        // 5. the OG 3D sun-slab on the horizon, right of centre
        dabyws$sunSlab(g, (int) (w * 0.62), horizon, (int) (h * 0.42), t);

        // 6. three layers of dark mountain silhouettes (far -> near)
        dabyws$ridge(g, w, horizon, h, 0xFF241138, 0.16, 0.011, 0.0D);
        dabyws$ridge(g, w, horizon, h, 0xFF120A22, 0.26, 0.017, 2.6D);
        dabyws$ridge(g, w, horizon, h, 0xFF05030C, 0.38, 0.013, 5.2D);

        // 7. cinematic vignette
        g.fillGradient(0, 0, w, h * 8 / 100, 0x99030208, 0x00030208);
        g.fillGradient(0, h - h * 8 / 100, w, h, 0x00030208, 0x99030208);
        g.fillGradient(0, 0, w * 6 / 100, h, 0x55030208, 0x00030208);
        g.fillGradient(w - w * 6 / 100, 0, w, h, 0x00030208, 0x55030208);
    }

    /** A soft horizontal nebula band with feathered top and bottom edges. */
    private static void dabyws$nebulaBand(GuiGraphicsExtractor g, int w, int y, int bandH, int color, double t, double phase) {
        int drift = (int) (Math.sin(t * 0.05D + phase) * w * 0.02D);
        int x1 = (int) (w * 0.04D) + drift;
        int x2 = (int) (w * 0.96D) + drift;
        g.fillGradient(x1, y, x2, y + bandH / 3, 0x00000000, color);
        g.fillGradient(x1, y + bandH / 3, x2, y + bandH * 2 / 3, color, color);
        g.fillGradient(x1, y + bandH * 2 / 3, x2, y + bandH, color, 0x00000000);
    }

    /** Deterministic twinkling starfield (LCG — same sky every launch). */
    private static void dabyws$stars(GuiGraphicsExtractor g, int w, int maxY, int count, double t) {
        long seed = 20260913L;
        for (int i = 0; i < count; i++) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int sx = (int) Math.floorMod(seed >> 33, Math.max(1, w));
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int sy = (int) Math.floorMod(seed >> 33, Math.max(1, maxY));
            double tw = Math.sin(t * 1.1D + i * 1.7D) * 0.5D + 0.5D;
            int tier = i % 7;
            int size = tier == 0 ? 2 : 1;
            int base = tier == 0 ? 170 : (tier < 3 ? 120 : 70);
            int alpha = (int) (base * (0.4D + tw * 0.6D));
            int rgb = tier == 0 ? 0xFFFFFF : 0xC8D8FF;
            g.fill(sx, sy, sx + size, sy + size, (alpha << 24) | rgb);
        }
    }

    /** Filled disc, one fill call per scanline (moon, halos). */
    private static void dabyws$disc(GuiGraphicsExtractor g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int rr = r * r - dy * dy;
            int dx = (int) Math.sqrt(rr);
            g.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }

    /**
     * The OG "SAGE MCSM" 3D sun-slab: a wide golden slab sitting on the
     * horizon with a rising glow, a horizontal god-ray band and a warm bloom.
     * Pulses slowly like a real star.
     */
    private static void dabyws$sunSlab(GuiGraphicsExtractor g, int cx, int horizon, int maxW, double t) {
        double pulse = 0.5D + 0.5D * Math.sin(t * 0.6D);
        int slabW = maxW;
        int slabH = Math.max(16, maxW / 7);
        int top = horizon - slabH + 3;

        // wide ambient bloom
        int bloomA = (int) (30 + pulse * 22);
        g.fill(cx - slabW, top - slabH * 2, cx + slabW, horizon + slabH, (bloomA << 24) | 0x8A4A2A);
        // soft glow rising above the slab
        int glowA = (int) (36 + pulse * 30);
        g.fillGradient(cx - slabW / 2, top - slabH * 3, cx + slabW / 2, top, 0x00FFB84D, (glowA << 24) | 0xFFB84D);
        // horizontal god-ray band across the whole screen
        int rayA = (int) (44 + pulse * 38);
        g.fillGradient(cx - slabW * 2, top + slabH / 2 - 5, cx + slabW * 2, top + slabH / 2 + 5,
                0x00FFB84D, (rayA << 24) | 0xFFB84D);
        // the slab itself: warm gold -> white-hot core
        g.fill(cx - slabW / 2, top, cx + slabW / 2, top + slabH, 0xFFE0903C);
        g.fill(cx - slabW / 3, top + 2, cx + slabW / 3, top + slabH - 2, 0xFFF2B964);
        g.fill(cx - slabW / 6, top + 4, cx + slabW / 6, top + slabH - 4, 0xFFFFF0CE);
    }

    /**
     * A continuous dark mountain silhouette: column fills whose height is a
     * layered sine noise, base sitting on the horizon (gap-free).
     */
    private static void dabyws$ridge(GuiGraphicsExtractor g, int w, int horizon, int h, int color,
            double amp, double freq, double phase) {
        int step = 8;
        int baseH = (int) (h * 0.035D);
        for (int x = -step; x <= w + step; x += step) {
            double n = Math.sin(x * freq + phase) * 0.55D
                    + Math.sin(x * freq * 2.7D + phase * 1.7D) * 0.30D
                    + Math.sin(x * freq * 0.6D + phase * 0.3D) * 0.45D;
            n = n * 0.5D + 0.5D;
            int peakH = Math.max(4, (int) (baseH + h * amp * n));
            g.fill(x, horizon - peakH, x + step, horizon + 2, color);
        }
    }

    /**
     * Build #374: while the boot cinematic owns the screen (pre-game
     * cutscene or the command block burst), the chrome stands down and the
     * sequence draws on top of everything.
     */
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void dabyws$cinematicBoot(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        try {
            if (net.mcsm.extras.client.McsmCinematic.tickMenu()) {
                net.mcsm.extras.client.McsmCinematic.drawMenuSequence(g, this);
            }
        } catch (Throwable ignored) {
            // the cinematic must never break a frame
        }
    }

    /**
     * Build #374: input guard for the boot cinematic + the button
     * break-apart trigger. While a sequence is playing the clicks are
     * swallowed (any key skips); otherwise a click on a menu button spawns
     * the shatter spray at the button centre.
     */
    @Inject(method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z",
            at = @At("HEAD"), cancellable = true)
    private void dabyws$cinematicClick(net.minecraft.client.input.MouseButtonEvent event,
            boolean doubleClick,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        try {
            if (net.mcsm.extras.client.McsmCinematic.tickMenu()) {
                cir.setReturnValue(true);
                return;
            }
            for (Object child : this.children()) {
                if (!(child instanceof net.minecraft.client.gui.components.AbstractButton)) {
                    continue;
                }
                net.minecraft.client.gui.components.AbstractButton b =
                        (net.minecraft.client.gui.components.AbstractButton) child;
                if (!b.active || !b.visible || !b.isMouseOver(event.x(), event.y())) {
                    continue;
                }
                int cx = b.getX() + b.getWidth() / 2;
                int cy = b.getY() + b.getHeight() / 2;
                net.mcsm.extras.client.McsmCinematic.shatterButton(cx, cy);
                // Build #375: the press flash + the new chime sound
                MC$PRESS_MS.put(b, System.currentTimeMillis());
                try {
                    net.mcsm.extras.client.McsmButtonSounds.click();
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
            // cosmetic only
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void dabyws$mcsMenuChrome(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        // Build #374: no chrome while the boot cinematic owns the screen
        if (net.mcsm.extras.client.McsmCinematic.isSequenceActive()) {
            return;
        }
        int w = this.width;
        int h = this.height;
        Font font = Minecraft.getInstance().font;

        // --- Build #375: the DS title --------------------------------------
        // The vanilla logo (with its "Java Edition" line) is not drawn on
        // the title anymore; this is the mod's own identity: the new icon +
        // a crisp two-line wordmark (native font size - measured, centred,
        // never squished).
        String word = "§6§lDEVOURING STORMS";
        String sub = "§5The Point of No Return";
        int wordW = font.width(word);
        int subW = font.width(sub);
        int iconSize = 44;
        int titleW = iconSize + 8 + Math.max(wordW, subW);
        int tx0 = (w - titleW) / 2;
        int ty0 = 58;
        // icon with a soft glow frame
        g.fill(tx0 - 2, ty0 - 2, tx0 + iconSize + 2, ty0 + iconSize + 2, 0x662A1A4A);
        try {
            g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, DS_ICON,
                    tx0, ty0, 0.0F, 0.0F, iconSize, iconSize, 128, 128);
        } catch (Throwable ignored) {
            g.fill(tx0, ty0, tx0 + iconSize, ty0 + iconSize, 0xFF1A1426);
        }
        int textX = tx0 + iconSize + 8;
        long pulseMs = System.currentTimeMillis();
        float subPulse = (float) (Math.sin(pulseMs * 0.003D) * 0.5D + 0.5D);
        int subCol = 0xFF000000 | 0x9F << 16 | (int) (0x6A + subPulse * 0x40) << 8 | 0xD9;
        g.text(font, word, textX + (Math.max(wordW, subW) - wordW) / 2, ty0 + 6, 0xFFF2E3C2, true);
        g.text(font, sub, textX + (Math.max(wordW, subW) - subW) / 2, ty0 + 27, subCol, false);

        // Image 3 Silver Pixel Border Frame
        if (McsmExtrasConfig.uiBorderLines) {
            int borderCol = 0xFF8A8A9E; // Silver-gray
            int innerCol  = 0xFF2A2A38;
            g.fill(0, 0, w, 2, borderCol);
            g.fill(0, h - 2, w, h, borderCol);
            g.fill(0, 0, 2, h, borderCol);
            g.fill(w - 2, 0, w, h, borderCol);

            g.fill(4, 4, w - 4, 5, innerCol);
            g.fill(4, h - 5, w - 4, h - 4, innerCol);
            g.fill(4, 4, 5, h - 4, innerCol);
            g.fill(w - 5, 4, w - 4, h - 4, innerCol);

            // L-shape Corner Accents
            g.fill(2, 2, 10, 4, borderCol);
            g.fill(2, 2, 4, 10, borderCol);
            g.fill(w - 10, 2, w - 2, 4, borderCol);
            g.fill(w - 4, 2, w - 2, 10, borderCol);
            g.fill(2, h - 4, 10, h - 2, borderCol);
            g.fill(2, h - 10, 4, h - 2, borderCol);
            g.fill(w - 10, h - 4, w - 2, h - 2, borderCol);
            g.fill(w - 4, h - 10, w - 2, h - 2, borderCol);
        }

        // --- bottom cinematic bar -------------------------------------------
        g.fill(0, h - 34, w, h, 0xF20A0612);
        g.fillGradient(0, h - 36, w, h - 34, 0xFF3F255A, 0xFF6A8FF7);
        g.centeredText(font,
                "\u00a77An Episode in Five Acts \u00a78\u2014 \u00a75The Wither Storm Saga",
                w / 2, h - 28, 0xFFB9C6E2);
        g.centeredText(font,
                "\u00a78build " + McsmExtrasConfig.BUILD_VERSION + " \u00a77\u00b7 \u00a78MCSM menu",
                w / 2, h - 16, 0xFF7F8CA8);
    }
}
