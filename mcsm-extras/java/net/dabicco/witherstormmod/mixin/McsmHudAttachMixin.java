package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.dabicco.witherstormmod.client.StormAtmosphereOverlay;
import net.mcsm.extras.client.McsmHudTerminal;

/**
 * Attaches the Story Mode holographic terminal to the base mod's own HUD
 * element. StormAtmosphereOverlay.render(GuiGraphicsExtractor, DeltaTracker)
 * is registered in HudElementRegistry by DabyWitherStormModClient, so it
 * runs every frame with a live graphics extractor - the safest possible
 * per-frame hook (compiled base class, exact signature, no vanilla Gui
 * mixin, no Fabric rendering API on the compile classpath).
 */
@Mixin(StormAtmosphereOverlay.class)
public abstract class McsmHudAttachMixin {

    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private static void dabyws$terminal(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        McsmHudTerminal.paint(g, delta);
    }
}
