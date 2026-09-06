package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;

/**
 * Devouring Storms: the vanilla BOTTOM hotbar is removed at engine level -
 * the hotbar itself now lives top-left, bigger, MCSM-style, drawn by
 * McsmHudTerminal through the base mod's atmosphere overlay.
 *
 * Target verified by the CI GUI-surface probe against the 26.2 client jar:
 * Hud#extractItemHotbar(GuiGraphicsExtractor, DeltaTracker) is the private
 * method that extracts ONLY the item hotbar (sprite, slots, items,
 * selection, offhand wings). Cancelling it leaves the crosshair, hearts,
 * food, armor, air and XP bar exactly where vanilla puts them.
 */
@Mixin(Hud.class)
public abstract class McsmHotbarHideMixin {

    @Inject(method = "extractItemHotbar", at = @At("HEAD"), cancellable = true)
    private void dabyws$hotbarLivesTopLeftNow(GuiGraphicsExtractor extractor,
            DeltaTracker delta, CallbackInfo ci) {
        ci.cancel();
    }
}
