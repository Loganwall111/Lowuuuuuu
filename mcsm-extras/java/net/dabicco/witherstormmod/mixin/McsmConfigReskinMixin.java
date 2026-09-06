package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Devouring Storms: the MCSM config/UI revamp - the Storm Config screen
 * gets the same cinematic treatment as the main menu.
 *
 * HEAD lays a deep violet-night plate with a faint horizon glow under
 * everything (the pickers' own translucent dim blends over it, and the
 * 3D preview's black canvas sits on top unharmed). TAIL frames the whole
 * screen with thin blue-violet accent edges and a small brand tag in the
 * free strip above the tabs (tabs start at y=21, panel at y=58, bottom
 * buttons end at h-7 - the chrome never touches a widget zone).
 *
 * Target is the base mod's own compiled screen class; the injected
 * method (extractRenderState) and every extractor call are verified from
 * the CI-decompiled source and the 26.2 extractor dump.
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
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"), remap = false)
    private void dabyws$mcsChrome(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        WitherStormConfigScreen self = (WitherStormConfigScreen) (Object) this;
        int w = self.width;
        int h = self.height;
        // cinematic accent frame
        g.fillGradient(0, 0, w, 3, 0xFF6A8FF7, 0xFF3F255A);
        g.fillGradient(0, h - 3, w, h, 0xFF3F255A, 0xFF6A8FF7);
        g.fillGradient(0, 0, 3, h, 0x886A8FF7, 0x223F255A);
        g.fillGradient(w - 3, 0, w, h, 0x886A8FF7, 0x223F255A);
        // brand tag in the free strip above the tabs
        g.text(Minecraft.getInstance().font,
                "\u00a75\u00a7l\u26a1 DEVOURING STORMS \u00a78\u00b7 storm config",
                8, 8, 0xFF9FB4D8, false);
    }
}
