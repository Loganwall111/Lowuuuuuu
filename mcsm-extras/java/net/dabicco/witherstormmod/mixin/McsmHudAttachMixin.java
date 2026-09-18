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
            // BUILD #479 -- and the gel over it: the glow above, the violet below, the
            // silhouettes and the life. Drawn after the floor's light show so the deep
            // owns the frame the way the concept image does.
            net.mcsm.extras.client.McsmVoidDeep.draw(g);
            // BUILD #483 -- and the dweller that is talking: its line, set down in
            // the frame letter by letter, out of its own synced data.
            net.mcsm.extras.client.McsmVoidDwellerTalk.draw(g);
            // BUILD #486 -- epic disintegration cinematic: acid to black, volumetric disintegration,
            // 4 planets (Dungeons, Legends, MC2, Movie + Story Mode), warp drive, white maze, glitch to first layer
            net.mcsm.extras.client.McsmVoidCinematic.draw(g);
            // BUILD #485 -- merged void: skybox slowly turns color when falling hundreds blocks
            net.mcsm.extras.client.McsmVoidMerged.draw(g);
        } catch (Throwable t) {
            net.mcsm.extras.client.McsmMenuGuard.fault("hud-void-floor", t);
        }
        // BUILD #475 -- THE HALLUCINATIONS. The owed half of the two switches that
        // have been on the settings screen since D.8 and read by nothing: torn
        // bands, a figure at the edge of vision, a false sky and the whispering.
        // Drawn LAST, over everything the world and the mod have already put up,
        // because a hallucination is the topmost thing a broken reality does.
        try {
            net.mcsm.extras.client.McsmHallucinations.paint(g, delta);
        } catch (Throwable t) {
            net.mcsm.extras.client.McsmMenuGuard.fault("hud-hallucinations", t);
        }
    }
}
