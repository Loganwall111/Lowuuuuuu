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
 */
@Mixin(TitleScreen.class)
public abstract class McsmTitleOverhaulMixin extends Screen {

    protected McsmTitleOverhaulMixin(Component title) {
        super(title);
    }

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void dabyws$stormBackdrop(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        ci.cancel();
        int w = this.width;
        int h = this.height;
        // deep violet night -> near black
        g.fillGradient(0, 0, w, h, 0xFF120A1E, 0xFF05030A);
        // storm glow on the horizon
        g.fillGradient(0, h * 2 / 3, w, h, 0x00000000, 0x553F255A);
        // deterministic twinkling stars over upper two thirds
        long seed = 20260906L;
        for (int i = 0; i < 120; i++) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int sx = (int) Math.floorMod(seed >> 33, Math.max(1, w));
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int sy = (int) Math.floorMod(seed >> 33, Math.max(1, h * 2 / 3));
            double tw = Math.sin(System.currentTimeMillis() * 0.0011D + i * 1.7D) * 0.5D + 0.5D;
            int alpha = 60 + (int) (tw * 110);
            g.fill(sx, sy, sx + 1, sy + 1, (alpha << 24) | 0xC8D8FF);
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void dabyws$mcsMenuChrome(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
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
