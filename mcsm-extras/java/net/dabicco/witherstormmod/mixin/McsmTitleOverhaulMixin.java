package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    protected McsmTitleOverhaulMixin(Component title) {
        super(title);
    }

    /**
     * Build #374 -- STORY MODE ACCURATE main menu. The #371 procedural night
     * scene is retired as the ON-path: the user's standing order is the OG
     * panorama + OG sun/moon (the base's own Story Mode menu backdrop, the
     * in-scene Wither Storm shot), framed like a Telltale episode menu. So:
     *  - toggle ON  -> do NOT cancel: the base draws its OG cube panorama and
     *                  sun, then the TAIL hook adds the episodic chrome
     *                  (readability vignette + dark translucent panels with
     *                  gold hover frames behind every menu button).
     *  - toggle OFF -> the plain gradient (pre-#371 look), base cancelled.
     */
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void dabyws$stormBackdrop(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        if (!McsmExtrasConfig.storyMenuBackdrop) {
            ci.cancel();
            int w = this.width;
            int h = this.height;
            g.fillGradient(0, 0, w, h, 0xFF120A1E, 0xFF05030A);
            g.fillGradient(0, h * 2 / 3, w, h, 0x00000000, 0x553F255A);
        }
        // ON: fall through — the base paints the OG panorama + sun/moon.
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
        if (!McsmExtrasConfig.storyMenuBackdrop) {
            return;
        }
        int w = this.width;
        int h = this.height;

        // readability vignette (kept light so the OG panorama stays vivid)
        g.fillGradient(0, 0, w, 42, 0x66030208, 0x00030208);
        g.fillGradient(0, h - 96, w, h - 32, 0x00030208, 0xAA030208);
        g.fillGradient(0, 0, 26, h, 0x44030208, 0x00030208);
        g.fillGradient(w - 26, 0, w, h, 0x00030208, 0x44030208);

        // episodic panels behind every menu button
        for (Object child : this.children()) {
            if (!(child instanceof net.minecraft.client.gui.components.AbstractButton)) {
                continue;
            }
            net.minecraft.client.gui.components.AbstractButton b =
                    (net.minecraft.client.gui.components.AbstractButton) child;
            if (!b.visible || !b.active) {
                continue;
            }
            int px = b.getX() - 10;
            int py = b.getY() - 5;
            int pw = b.getWidth() + 20;
            int ph = b.getHeight() + 10;
            boolean hov = b.isHovered();
            g.fill(px, py, px + pw, py + ph, hov ? 0xF0171B24 : 0xE60A0C11);
            int edge = hov ? 0xFFD9A441 : 0xFF2A3140;
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
            }
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
