package net.dabicco.witherstormmod.mixin;

import net.mcsm.extras.client.McsmVoidDeep;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BUILD #479 -- NO LOADING SCREEN ON THE WAY INTO THE FALL.
 *
 * <p>The player: "there's no loading screen, you just fall into it". A dimension
 * change is the one thing the game insists on putting a screen in front of, and the
 * screen it reaches for here is the world-loading one. So while a dive is in flight,
 * that screen draws nothing of its own -- no grey plate, no progress bar, no "Building
 * terrain" -- and the deep paints the gel in its place ({@link McsmVoidDeep#paintEntryFrame}).
 *
 * <p>It is deliberately narrow: only while a descent is actually mid-flight, only on
 * the world-loading screen, and never anywhere else. Loading a world by any other
 * route keeps its own screen exactly as it was.
 */
@Mixin(LevelLoadingScreen.class)
public abstract class McsmVoidDeepLoadingMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void mcsm$deepEntryFrame(GuiGraphicsExtractor g, int mouseX, int mouseY,
            float partialTick, CallbackInfo ci) {
        try {
            if (!McsmVoidDeep.suppressLoadingScreen() || g == null) {
                return;
            }
            McsmVoidDeep.paintEntryFrame(g);
            ci.cancel();
        } catch (Throwable ignored) {
            // the screen draws itself, which is what it was going to do anyway
        }
    }
}
