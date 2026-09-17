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
        // BUILD #469 -- fault-isolated. The whole in-world overlay rides this one
        // hook (terminal, cut-scenes, void floor, cracks): one throw in any of them
        // used to take the base atmosphere overlay's render with it, and a render
        // that throws is a frame that does not draw. Each pass is reported instead
        // of propagating.
        try {
            McsmHudTerminal.paint(g, delta);
        } catch (Throwable t) {
            net.mcsm.extras.client.McsmMenuGuard.fault("hud-terminal", t);
        }
        // Build #374: the world-load "cracks apart" sequence + sky shockwaves
        // ride the same proven per-frame HUD hook.
        try {
            net.mcsm.extras.client.McsmCinematic.tickWorld();
            net.mcsm.extras.client.McsmCinematic.drawWorldCracks(g, delta);
        } catch (Throwable t) {
            net.mcsm.extras.client.McsmMenuGuard.fault("hud-world-cracks", t);
        }
        // BUILD #427 -- the ending: the white scene, the ripping cracks and the
        // colour shockwaves, on the same proven per-frame hook.
        try {
            net.mcsm.extras.client.McsmCinematic.tickEnding();
            net.mcsm.extras.client.McsmCinematic.drawEnding(g);
        } catch (Throwable t) {
            net.mcsm.extras.client.McsmMenuGuard.fault("hud-ending", t);
        }
        // BUILD #447 -- the in-world cutscenes, drawn over the world on the same
        // proven per-frame hook (bars, title card, typed lines).
        try {
            net.mcsm.extras.client.McsmScenes.draw(g, delta);
        } catch (Throwable t) {
            net.mcsm.extras.client.McsmMenuGuard.fault("hud-scenes", t);
        }
        // BUILD #452 -- and the bottom of the void, which is a place with a show
        // in it: RGB shafts out of the invisible floor and white rings for ever.
        try {
            net.mcsm.extras.client.McsmVoidFloor.draw(g);
        } catch (Throwable t) {
            net.mcsm.extras.client.McsmMenuGuard.fault("hud-void-floor", t);
        }
    }
}
