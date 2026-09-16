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
        // Build #374: the world-load "cracks apart" sequence + sky shockwaves
        // ride the same proven per-frame HUD hook.
        net.mcsm.extras.client.McsmCinematic.tickWorld();
        net.mcsm.extras.client.McsmCinematic.drawWorldCracks(g, delta);
        // BUILD #427 -- the ending: the white scene, the ripping cracks and the
        // colour shockwaves, on the same proven per-frame hook.
        net.mcsm.extras.client.McsmCinematic.tickEnding();
        net.mcsm.extras.client.McsmCinematic.drawEnding(g);
        // BUILD #447 -- the in-world cutscenes, drawn over the world on the same
        // proven per-frame hook (bars, title card, typed lines).
        net.mcsm.extras.client.McsmScenes.draw(g, delta);
    }
}
