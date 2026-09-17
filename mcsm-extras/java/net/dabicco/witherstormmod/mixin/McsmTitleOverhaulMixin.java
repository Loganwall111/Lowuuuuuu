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
// Build #376: priority 1500 (> the base StoryModeTitleScreenMixin's default
// 1000). Mixin applies lower-priority mixins first and inserts each
// later-applied mixin's TAIL callback AFTER the earlier ones, so at
// extractRenderState TAIL this mixin's chrome executes AFTER the base's
// banner - which is what lets dabyws$mcsMenuChrome paint the banner away.
@Mixin(value = TitleScreen.class, priority = 1500)
public abstract class McsmTitleOverhaulMixin extends Screen {

    /** The DS title icon (new menu asset, 128x128). */
    private static final net.minecraft.resources.Identifier DS_ICON =
            net.minecraft.resources.Identifier.fromNamespaceAndPath("mcsm", "menu/ds_icon.png");

    /** Linear ARGB mix, so every eased chrome colour lands between its two ends. */
    private static int mcsm$lerpArgb(int from, int to, float t) {
        if (t <= 0.0F) return from;
        if (t >= 1.0F) return to;
        int a = (from >>> 24) & 0xFF, r = (from >> 16) & 0xFF, g = (from >> 8) & 0xFF, b = from & 0xFF;
        int a2 = (to >>> 24) & 0xFF, r2 = (to >> 16) & 0xFF, g2 = (to >> 8) & 0xFF, b2 = to & 0xFF;
        int aa = Math.round(a + (a2 - a) * t), rr = Math.round(r + (r2 - r) * t);
        int gg = Math.round(g + (g2 - g) * t), bb = Math.round(b + (b2 - b) * t);
        return (aa & 0xFF) << 24 | (rr & 0xFF) << 16 | (gg & 0xFF) << 8 | (bb & 0xFF);
    }

    /** Per-button animation state (Build #375 3D button effects). */
    private static final java.util.Map<Object, Boolean> MC$HOVER = new java.util.IdentityHashMap<>();
    private static final java.util.Map<Object, Long> MC$HOVER_MS = new java.util.IdentityHashMap<>();
    private static final java.util.Map<Object, Long> MC$PRESS_MS = new java.util.IdentityHashMap<>();
    /** Build #416 -- entrance clock. The panels used to appear as one block the
     *  instant the screen built; they now rise in sequence (45 ms apart) so the
     *  menu assembles instead of popping, and no two button animations start on
     *  the same frame. */
    private static long MC$ENTER_MS = 0L;
    /** Stagger between consecutive panels on the entrance animation. */
    private static final long MC$ENTER_SLOT = 45L;

    /** Storm turntable input state (GLFW poll, Build #375). */
    private static double MC$CUR_X = -1.0D;
    private static double MC$CUR_Y = -1.0D;

    protected McsmTitleOverhaulMixin(Component title) {
        super(title);
    }

    @Inject(method = "added", at = @At("TAIL"))
    private void dabyws$menuOpenSound(CallbackInfo ci) {
        try {
            // Build #380: the boot cinematic is a one-shot on the startup
            // loading scene. The instant the title screen exists, mark it
            // consumed so it never re-plays or bleeds onto the menu (it used
            // to overlap the menu buttons after boot).
            net.mcsm.extras.client.McsmCinematic.markBootDone();
            net.mcsm.extras.client.McsmButtonSounds.menuOpen();
            MC$ENTER_MS = System.currentTimeMillis();
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
     * Build #416 -- THE PANORAMA IS RESTORED.
     *
     * #375 deleted it outright ("the panorama is gone, on or off") and put a
     * flat storm-space gradient in its place; the menu then read as a static
     * card. The vanilla cube panorama draws again by default -- what the
     * screen renders is the real, slowly rotating world cube -- and this hook
     * only steps in for the explicit opt-out. The grade that palette-matches
     * it to Devouring Storms is applied afterwards, in
     * {@link #dabyws$storyModeFraming}, so it layers ON the panorama instead
     * of hiding underneath it.
     */
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void dabyws$stormBackdrop(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        if (McsmExtrasConfig.menuPanorama) {
            return; // vanilla panorama + vanilla background: untouched
        }
        ci.cancel(); // explicit opt-out: the mod's OWN sky, never a black plate
        int w = this.width;
        int h = this.height;
        // BUILD #464 -- "fix the main menu be black".
        //
        // This path used to paint an opaque #07050E..#0B0716 plate over the whole
        // screen, so the switch that reads "Vivid Panorama Backdrop" meant "or a
        // black screen" the moment it was turned off. It is the decayed reality's
        // own sky now -- the same hexes its dimension, its biome, its fog and its
        // painted cube use, through the one painter every screen of ours shares
        // (McsmMenuSky), so the menu is a place rather than a hole.
        net.mcsm.extras.client.McsmMenuSky.paint(g, w, h, 1.0F);
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

        // --- Build #416 (D.8, phase 5): the corner entry is retired ----------
        // The base mod puts a purple "\u00a75\u00a7l\u26a1 Storm Config" button in the TOP-RIGHT
        // CORNER of the menu. The user's rule for this build: "the config entry
        // has to be at the bottom right, inside the Minecraft logo button, not in
        // the side/corner". The replacement is drawn by dabyws$mcsMenuChrome and
        // clicked in dabyws$cinematicClick; this hides the old one so there is
        // exactly ONE way in from the menu.
        for (Object child : this.children()) {
            if (!(child instanceof net.minecraft.client.gui.components.AbstractButton)) {
                continue;
            }
            net.minecraft.client.gui.components.AbstractButton cb =
                    (net.minecraft.client.gui.components.AbstractButton) child;
            if (cb.getMessage() == null) {
                continue;
            }
            String msg = cb.getMessage().getString();
            if (msg.contains("Storm Config") && cb.getY() <= 12) {
                cb.visible = false;
                cb.active = false;
            }
        }

        // --- Build #376: remove the base "3D Storm Preview" button ---------
        // The base StoryModeTitleScreenMixin adds it at (width-142, 27,
        // 136, 18) in init. User order: no "Preview" wording. Hiding it here
        // (before the widgets render) takes it off the screen; the "Storm
        // Config" button directly above it (y=6) is untouched.
        for (Object child : this.children()) {
            if (!(child instanceof net.minecraft.client.gui.components.AbstractButton)) {
                continue;
            }
            net.minecraft.client.gui.components.AbstractButton pb =
                    (net.minecraft.client.gui.components.AbstractButton) child;
            if (pb.getY() == 27 && pb.getWidth() == 136 && pb.getHeight() == 18) {
                pb.visible = false;
                pb.active = false;
            }
        }

        // BUILD #416 (D.8, phase 5) -- THE FULL-SCREEN GRADE IS GONE.
        //
        // It used to lay a violet-to-plum wash (0x66..0x80 alpha) over the whole
        // panorama, plus a second wash over the bottom third. The user's report:
        // "there's a big tint / vignette around the screen edges which makes the
        // main menu and the panorama hard to see -- remove it". That wash was it.
        // The menu now shows the real panorama at full strength; the Devouring
        // Storms identity is carried by the wordmark, the chrome and the storm's
        // own scene instead of by a film over everything.
        if (McsmExtrasConfig.menuPanorama) {
            // A single soft band at the very bottom, where the cinematic bar
            // already sits: it grounds the wordmark without dimming the view.
            g.fillGradient(0, h - 96, w, h, 0x00000000, 0x33000000);
        }

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
        int entranceIdx = 0;
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

            // Build #416 -- ONE animation at a time per button. The press wash,
            // the hover glow/sparkles and the shatter used to run together, so a
            // click read as the hover animation flickering under the flash.
            // A press wins outright for its 160 ms; the hover layer is simply
            // not drawn for that button while the wash is on screen.
            Long press0 = MC$PRESS_MS.get(b);
            long pressT = press0 == null ? -1L : (nowMs - press0);
            boolean pressing = pressT >= 0L && pressT < 160L;
            boolean hovAnim = hov && !pressing;

            // entrance: staggered rise (see MC$ENTER_MS)
            long entT = Math.max(0L, nowMs - MC$ENTER_MS - MC$ENTER_SLOT * (entranceIdx++));
            float ent = Math.min(1.0F, entT / 320.0F);
            int rise = (int) ((1.0F - ent) * 14.0F);

            // BUILD #416 (D.8 UI pass) -- EASED hover.
            //
            // Everything below used to switch on the raw hover boolean, so the
            // panel fill, the edge colour and the glow all snapped in one frame.
            // They now ride a smoothstepped 0..1 factor measured from the same
            // hover timestamp the sound already uses, which is what makes the
            // menu feel smooth rather than blinking. Pressing still wins
            // outright (see hovAnim above).
            float hoverT = 0.0F;
            if (hov) {
                hoverT = Math.min(1.0F, (nowMs - MC$HOVER_MS.getOrDefault(b, nowMs)) / 140.0F);
            }
            float hoverEase = hoverT * hoverT * (3.0F - 2.0F * hoverT);

            // 3D lift: the panel rises up to 2px and gains a drop shadow on hover
            int lift = (int) (2.0F * hoverEase + 0.5F) + rise;
            int px = b.getX() - 10;
            int py = b.getY() - 5 - lift;
            int pw = b.getWidth() + 20;
            int ph = b.getHeight() + 10;
            if (hoverEase > 0.01F) {
                int shadowA = (int) (0x55 * hoverEase);
                g.fill(px + 2, py + ph + 2, px + pw + 2, py + ph + 3, (shadowA << 24));
                // the shine sweep: one soft band rides across the panel as the
                // pointer settles, then fades -- the same trick the config
                // console's header rule uses, so the two screens feel related
                int sweepW = 18;
                int sweepX = px + (int) ((pw + sweepW) * hoverT) - sweepW;
                int sweepA = (int) (70.0F * (1.0F - hoverT) * (1.0F - hoverT));
                if (sweepA > 2) {
                    g.fill(Math.max(px, sweepX), py, Math.min(px + pw, sweepX + sweepW),
                            py + ph, (sweepA << 24) | 0x00E8F4FF);
                }
            }
            g.fill(px, py, px + pw, py + ph,
                    mcsm$lerpArgb(0xE60A0C11, 0xF0171B24, hoverEase));
            int edge = mcsm$lerpArgb(0xFF2A3140, 0xFFD9A441, hoverEase);
            // pulsing glow frame while hovered (the "crazy cool" idle energy)
            if (hovAnim) {
                long sinceHover = nowMs - (MC$HOVER_MS.getOrDefault(b, nowMs));
                float pulse = (float) (Math.sin(sinceHover * 0.012D) * 0.5D + 0.5D);
                int glowA = (int) ((40 + pulse * 70) * Math.max(0.25F, hoverEase));
                g.fill(px - 2, py - 2, px + pw + 2, py - 1, (glowA << 24) | 0xFFD9A441);
                g.fill(px - 2, py + ph + 1, px + pw + 2, py + ph + 2, (glowA << 24) | 0xFFD9A441);
                g.fill(px - 2, py - 2, px - 1, py + ph + 2, (glowA << 24) | 0xFF9FEFFF);
                g.fill(px + pw + 1, py - 2, px + pw + 2, py + ph + 2, (glowA << 24) | 0xFF9FEFFF);
            }
            g.fill(px, py, px + pw, py + 1, edge);
            g.fill(px, py + ph - 1, px + pw, py + ph, edge);
            g.fill(px, py, px + 1, py + ph, edge);
            g.fill(px + pw - 1, py, px + pw, py + ph, edge);
            if (hovAnim) {
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
            if (press0 != null) {
                if (pressing) {
                    float fade = 1.0F - (float) (pressT / 160.0D);
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
            // the bottom-right logo button: menu -> the holographic terminal.
            // The terminal is opened with the code screen up; from there its own
            // CONFIG button reaches every switch this build has.
            if (MC$LOGO_X >= 0 && event.x() >= MC$LOGO_X
                    && event.x() < MC$LOGO_X + MC$LOGO_W
                    && event.y() >= MC$LOGO_Y && event.y() < MC$LOGO_Y + MC$LOGO_H) {
                try {
                    net.mcsm.extras.client.McsmTerminalScreen.show("login",
                            "RESTRICTED AREA\n\nEnter the admin password to continue.\n"
                            + "The code is not on this screen: it is written down in the world.");
                    net.mcsm.extras.client.McsmButtonSounds.click();
                } catch (Throwable ignored) {
                    // if the terminal cannot open, the menu must still work
                }
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

        // --- Build #376: remove the base "MINECRAFT: STORY MODE | WITHER
        //     STORM ULTIMATE" banner ---------------------------------------
        // The base StoryModeTitleScreenMixin still paints its dark band +
        // two text lines (top strip) and a bottom band at extractRenderState
        // TAIL. This hook runs AFTER the base banner (mixin priority 1500
        // vs the base's 1000) and repaints the top strip with the clean
        // storm-space gradient. The bottom band is already covered by the
        // cinematic bar below.
        // BUILD #416 (D.8 UI pass) -- the wordmark's own band.
        //
        // The plate used to stop at y=54 while the icon + wordmark start at
        // y=58 and run 44px tall, so the logo was painted straight over whatever
        // the base banner had left there (the user's "devouring storms logo
        // ... stamped in front of all this stuff and it's overlapping"). The
        // plate now covers the whole title band, 0..118, so the wordmark sits on
        // its own ground, and it is skipped entirely when the window is too
        // narrow for it to fit beside the frame.
        //
        // BUILD #448 -- "this is currently a giant Devouring Storms watermark
        // blocking it. It's very black."
        //
        // Two things were doing that, and both are answered here:
        //
        //   * THE BAND WAS AN OPAQUE BLACK BAR 118 PIXELS TALL. It is a LIFT and a
        //     soft shadow now: a brightness wash over the whole backdrop (so the
        //     panorama the player picked is actually visible), and a band half the
        //     height that fades out instead of stopping.
        //   * THE WORDMARK. The mark is a switch now, and it is OFF by default --
        //     the title screen is the panorama and the menu, and a brand block over
        //     it is a watermark, not a title. Turn it back on in the panel if you
        //     want it ("Title wordmark (DEVOURING STORMS)").
        double lift = Math.max(0.0D, Math.min(0.35D, McsmExtrasConfig.menuLift));
        if (lift > 0.001D) {
            int a = (int) Math.round(lift * 255.0D);
            g.fill(0, 0, w, h, (a << 24) | 0xE8E2FF);
        }
        int titleBandBottom = McsmExtrasConfig.titleWordmark ? 96 : 0;
        if (titleBandBottom > 0) {
            g.fillGradient(0, 0, w, titleBandBottom, 0xB007050E, 0x00070510);
            g.fillGradient(0, titleBandBottom - 14, w, titleBandBottom, 0x00070510, 0x00000000);
        }
        if (w < 420) {
            titleBandBottom = 0;   // too small for the wordmark: keep the sky clear
        }
        if (titleBandBottom > 0) {

        // --- Build #375: the DS title --------------------------------------
        // The vanilla logo (with its "Java Edition" line) is not drawn on
        // the title anymore; this is the mod's own identity: the new icon +
        // a crisp two-line wordmark (native font size - measured, centred,
        // never squished).
        } // end of the title band guard
        if (titleBandBottom > 0) {
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
        } // end of the wordmark block

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

        // --- BUILD #416 (D.8, phase 5): THE LOGO BUTTON ---------------------
        //
        // The one way into the config from the menu, per the user: bottom-right,
        // built as a logo button (the same DS icon the wordmark uses, on a
        // square plate with the caption under it), never a corner row. Clicking
        // it opens the holographic terminal -- see dabyws$cinematicClick -- and
        // the terminal's own CONFIG button opens the settings console inside it,
        // so the trip is menu -> terminal -> config and back.
        mcsm$drawLogoButton(g, font, w, h, mouseX, mouseY);
    }

    /** Rect of the bottom-right logo button, for the click handler. */
    private static int MC$LOGO_X = -1;
    private static int MC$LOGO_Y = -1;
    private static final int MC$LOGO_W = 108;
    private static final int MC$LOGO_H = 84;

    private void mcsm$drawLogoButton(GuiGraphicsExtractor g,
            net.minecraft.client.gui.Font font, int w, int h, int mouseX, int mouseY) {
        int bx = w - MC$LOGO_W - 14;
        int by = h - MC$LOGO_H - 12;
        if (w < 420 || h < 260) {
            MC$LOGO_X = -1;
            MC$LOGO_Y = -1;
            return;
        }
        MC$LOGO_X = bx;
        MC$LOGO_Y = by;
        boolean hot = mouseX >= bx && mouseX < bx + MC$LOGO_W
                && mouseY >= by && mouseY < by + MC$LOGO_H;
        // plate
        g.fill(bx, by, bx + MC$LOGO_W, by + MC$LOGO_H, hot ? 0xE6101A2A : 0xC6080C16);
        g.fill(bx, by, bx + MC$LOGO_W, by + 1, 0xFF39E0FF);
        g.fill(bx, by + MC$LOGO_H - 1, bx + MC$LOGO_W, by + MC$LOGO_H, 0xFF12455A);
        g.fill(bx, by, bx + 1, by + MC$LOGO_H, 0xFF12455A);
        g.fill(bx + MC$LOGO_W - 1, by, bx + MC$LOGO_W, by + MC$LOGO_H, 0xFF39E0FF);
        // the icon itself: this IS the Minecraft-logo slot on this menu, and the
        // DS icon is what stands in it, so the button reads as part of the logo
        int icon = 46;
        int ix = bx + (MC$LOGO_W - icon) / 2;
        int iy = by + 8;
        try {
            g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, DS_ICON,
                    ix, iy, 0.0F, 0.0F, icon, icon, 128, 128);
        } catch (Throwable ignored) {
            g.fill(ix, iy, ix + icon, iy + icon, 0xFF1A1426);
        }
        g.centeredText(font, "\u00a76\u00a7lCONFIG", bx + MC$LOGO_W / 2, by + 58, 0xFFF2E3C2);
        g.centeredText(font, hot ? "\u00a7fopen the terminal" : "\u00a78story mode console",
                bx + MC$LOGO_W / 2, by + 68, hot ? 0xFFFFFFFF : 0xFF7F8CA8);
    }
}
