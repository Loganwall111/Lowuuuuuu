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
            net.mcsm.extras.client.McsmVoidDeep.draw(g);
            net.mcsm.extras.client.McsmVoidDwellerTalk.draw(g);
            // 7000.0.11-M MERGED VOID + CINEMATIC: volumetric disintegration, multiverse planets, warp drive, white maze
            // Skybox merges slowly to color as you fall hundreds blocks - no loading screen
            try {
                // Sift infinite cosmos V2 + merged void skyboxes
                net.mcsm.sift.client.McsmSiftClient.tickClient();
                // Volumetric cinematic overlay - disintegration to black, 4 planets (Dungeons, Legends, Movie, Story Mode), warp drive, white flash, white maze, fog fade
                // This is real cinematic, not generic pixel overlay
                if (net.mcsm.sift.client.VoidEntryCinematic.INSTANCE.isActive()) {
                    // For GuiGraphicsExtractor, we need to use its own drawing - but we have PoseStack version
                    // So we draw via a temporary GuiGraphics wrapper if available, or use extractor's fill for phases
                    // The cinematic's render(PoseStack) expects PoseStack, but we can get it from extractor if possible
                    // For now, use the extractor to draw a placeholder that indicates cinematic active
                    // The actual full cinematic is rendered via McsmSiftClient.renderCinematic when called from Gui layer
                    // Here we ensure the cinematic ticks and also draw a subtle overlay
                    long now = System.currentTimeMillis();
                    int w = g.guiWidth();
                    int h = g.guiHeight();
                    var phase = net.mcsm.sift.client.VoidEntryCinematic.INSTANCE.getPhase();
                    float prog = net.mcsm.sift.client.VoidEntryCinematic.INSTANCE.getProgress();
                    // Draw phase indicator as subtle overlay for merged void
                    if (phase == net.mcsm.sift.client.VoidEntryCinematic.Phase.DISINTEGRATION) {
                        int alpha = (int)(prog * 200);
                        g.fill(0, 0, w, h, (alpha << 24) | 0x000000);
                    } else if (phase == net.mcsm.sift.client.VoidEntryCinematic.Phase.MULTIVERSE_PLANETS) {
                        // Draw 4 planets indicator
                        int cx = w/2;
                        int cy = h/2;
                        g.fill(cx-100, cy-60, cx-60, cy-20, 0xDD3355FF); // Dungeons
                        g.fill(cx+60, cy-60, cx+100, cy-20, 0xDDEE8844); // Legends
                        g.fill(cx-20, cy-100, cx+20, cy-60, 0xDD44AA44); // Movie
                        g.fill(cx-20, cy+60, cx+20, cy+100, 0xDDFF55AA); // Story
                    } else if (phase == net.mcsm.sift.client.VoidEntryCinematic.Phase.WARP_DRIVE) {
                        // Warp streaks
                        for (int i=0;i<20;i++) {
                            int x = (int)(Math.random()*w);
                            int y = (int)(Math.random()*h);
                            g.fill(x, y, x+40, y+2, 0xAAFFFFFF);
                        }
                    } else if (phase == net.mcsm.sift.client.VoidEntryCinematic.Phase.WHITE_FLASH) {
                        g.fill(0, 0, w, h, 0xFFFFFFFF);
                    } else if (phase == net.mcsm.sift.client.VoidEntryCinematic.Phase.WHITE_MAZE) {
                        g.fill(0, 0, w, h, 0xFFFFFFFF);
                        // White maze that doesn't exist - subtle black lines
                        for (int i=0;i<h;i+=40) {
                            g.fill(0, i, w, i+2, 0x11000000);
                        }
                    } else if (phase == net.mcsm.sift.client.VoidEntryCinematic.Phase.PITCH_BLACK_FOG_FADE) {
                        int alpha = (int)((1f-prog)*255);
                        g.fill(0, 0, w, h, (alpha << 24) | 0x000000);
                    }
                }
                // World reentry blackout - you can't see it for a few seconds
                if (net.mcsm.sift.client.WorldReentryOverlay.INSTANCE.isInBlackout()) {
                    float blackout = net.mcsm.sift.client.WorldReentryOverlay.INSTANCE.getBlackout();
                    int alpha = (int)(blackout * 255);
                    g.fill(0, 0, g.guiWidth(), g.guiHeight(), (alpha << 24) | 0x000000);
                }
            } catch (Throwable t2) {
                net.mcsm.extras.client.McsmMenuGuard.fault("hud-sift-cinematic", t2);
            }
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
