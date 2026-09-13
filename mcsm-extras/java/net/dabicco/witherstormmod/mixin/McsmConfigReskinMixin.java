package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.mcsm.extras.McsmExtrasConfig;

/**
 * Devouring Storms: WitherStormConfigScreen reskin with Image 3 silver pixel border frame.
 *
 * Build #371 — MENU UNFREEZE. The ctor injection that forced {@code previewShown = false}
 * has been REMOVED: the 3D preview (which contains the Phase / Sub-phase picker buttons)
 * is now shown by its base default, so the Stage sub-phase buttons are fully unlocked,
 * interactive and toggleable out of the box.
 */
@Mixin(WitherStormConfigScreen.class)
public abstract class McsmConfigReskinMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"), remap = false)
    private void dabyws$mcsPlate(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        WitherStormConfigScreen self = (WitherStormConfigScreen) (Object) this;
        int w = self.width;
        int h = self.height;
        g.fillGradient(0, 0, w, h, 0xFF120A1E, 0xFF05030A);
        g.fillGradient(0, h * 3 / 4, w, h, 0x00000000, 0x443F255A);
        // side panels
        g.fillGradient(0, 0, 18, h, 0xAA0A0612, 0x22140622);
        g.fillGradient(w - 18, 0, w, h, 0x22140622, 0xAA0A0612);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"), remap = false)
    private void dabyws$mcsChrome(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        WitherStormConfigScreen self = (WitherStormConfigScreen) (Object) this;
        int w = self.width;
        int h = self.height;

        // Image 3 Silver Pixel Border Frame
        if (McsmExtrasConfig.uiBorderLines) {
            int borderCol = 0xFF8A8A9E;
            int innerCol  = 0xFF2A2A38;
            g.fill(0, 0, w, 2, borderCol);
            g.fill(0, h - 2, w, h, borderCol);
            g.fill(0, 0, 2, h, borderCol);
            g.fill(w - 2, 0, w, h, borderCol);

            g.fill(4, 4, w - 4, 5, innerCol);
            g.fill(4, h - 5, w - 4, h - 4, innerCol);
            g.fill(4, 4, 5, h - 4, innerCol);
            g.fill(w - 5, 4, w - 4, h - 4, innerCol);

            g.fill(2, 2, 10, 4, borderCol);
            g.fill(2, 2, 4, 10, borderCol);
            g.fill(w - 10, 2, w - 2, 4, borderCol);
            g.fill(w - 4, 2, w - 2, 10, borderCol);
            g.fill(2, h - 4, 10, h - 2, borderCol);
            g.fill(2, h - 10, 4, h - 2, borderCol);
            g.fill(w - 10, h - 4, w - 2, h - 2, borderCol);
            g.fill(w - 4, h - 10, w - 2, h - 2, borderCol);
        }
    }
}
