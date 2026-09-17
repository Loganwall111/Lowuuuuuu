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
import net.mcsm.extras.client.McsmMenuGuard;
import net.mcsm.extras.client.McsmMenuSky;

/**
 * MCSM-style title chrome without blacking the frame.
 *
 * Cancelling extractBackground and filling 0xFF05030A was the black menu:
 * Mojang logo, then a full-screen black plate. Vanilla panorama draws;
 * we only add a translucent vignette and the cinematic bars.
 */
@Mixin(TitleScreen.class)
public abstract class McsmTitleOverhaulMixin extends Screen {

    protected McsmTitleOverhaulMixin(Component title) {
        super(title);
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void dabyws$stormBackdrop(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        if (!McsmMenuGuard.ok()) {
            return;
        }
        try {
            McsmMenuSky.paintVignette(g, this.width, this.height);
        } catch (Throwable t) {
            McsmMenuGuard.fault("title-backdrop", t);
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void dabyws$mcsMenuChrome(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        if (!McsmMenuGuard.ok()) {
            return;
        }
        try {
            int w = this.width;
            int h = this.height;
            Font font = Minecraft.getInstance().font;

            g.fillGradient(0, 0, w, 3, 0xFF6A8FF7, 0xFF3F255A);
            g.fillGradient(0, 0, 4, h, 0xAA6A8FF7, 0x223F255A);
            g.fillGradient(w - 4, 0, w, h, 0x223F255A, 0xAA6A8FF7);

            g.fill(0, h - 34, w, h, 0xC8100C1A);
            g.fillGradient(0, h - 36, w, h - 34, 0xFF3F255A, 0xFF6A8FF7);
            g.centeredText(font,
                    "\u00a77An Episode in Five Acts \u00a78\u2014 \u00a75The Wither Storm Saga",
                    w / 2, h - 28, 0xFFB9C6E2);
            g.centeredText(font,
                    "\u00a78build " + McsmExtrasConfig.BUILD_VERSION + " \u00a77\u00b7 \u00a78MCSM menu",
                    w / 2, h - 16, 0xFF7F8CA8);
        } catch (Throwable t) {
            McsmMenuGuard.fault("title-chrome", t);
        }
    }
}
