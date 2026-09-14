package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.mcsm.extras.client.McsmCinematic;

/**
 * Build #374: the main-menu button break-apart spray. Drawn from the
 * vanilla Screen hook (TAIL of extractRenderState) on ANY screen, so the
 * gold fragment spray survives the screen transition the button press
 * triggers and fades out over the destination screen.
 */
@Mixin(Screen.class)
public abstract class McsmCinematicScreenMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At("TAIL"))
    private void dabyws$shatterSpray(GuiGraphicsExtractor g, int mouseX, int mouseY,
                                     float partialTick, CallbackInfo ci) {
        try {
            McsmCinematic.drawShatter(g, (Screen) (Object) this);
        } catch (Throwable ignored) {
            // cosmetic only
        }
    }
}
