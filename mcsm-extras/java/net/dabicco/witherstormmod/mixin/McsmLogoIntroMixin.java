package net.dabicco.witherstormmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.mcsm.extras.client.McsmCinematic;

/**
 * Build #375: two jobs on the vanilla logo.
 *
 * 1. THE INTRO MOVES TO THE MOJANG LOADING SCENE. The boot cinematic
 *    (pre-game cutscene + command block burst) now plays while the
 *    Mojang logo scene is up - the very moment the game "first starts" -
 *    covering the logo with the full-screen sequence. It consumes the
 *    one-shot boot state, so the title screen never replays it (the
 *    standing order: "not when the game first starts, but while the
 *    loading scene is going").
 *
 * 2. THE "JAVA EDITION" LINE IS GONE. On the title screen the vanilla
 *    logo (with its edition/version line under the Minecraft wordmark)
 *    is not drawn at all - the Devouring Storms title takes its place
 *    (McsmTitleOverhaulMixin draws the DS wordmark + icon).
 */
@Mixin(LogoRenderer.class)
public abstract class McsmLogoIntroMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IF)V",
            at = @At("TAIL"), require = 0)
    private void mcsm$introOnLogoScene(GuiGraphicsExtractor g, int x, float partialTick, CallbackInfo ci) {
        // BUILD #469 -- fault-isolated: this runs on the boot/logo scene, the very
        // frame the report describes ("the Mojang logo loads ... and then just
        // completely black"). A throw here used to propagate out of the logo
        // renderer's extraction.
        try {
            mcsm$playIntro(g);
        } catch (Throwable t) {
            net.mcsm.extras.client.McsmMenuGuard.fault("logo-intro", t);
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IFII)V",
            at = @At("TAIL"), require = 0)
    private void mcsm$introOnLogoScene4(GuiGraphicsExtractor g, int x, float partialTick,
                                        int a, int b, CallbackInfo ci) {
        try {
            mcsm$playIntro(g);
        } catch (Throwable t) {
            net.mcsm.extras.client.McsmMenuGuard.fault("logo-intro", t);
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IF)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void mcsm$noLogoOnTitle(GuiGraphicsExtractor g, int x, float partialTick, CallbackInfo ci) {
        // BUILD #469 -- guarded here too, so the gate rule is uniform: every handler
        // this build injects into a render method carries its own catch.
        try {
            hideVanillaLogo(ci);
        } catch (Throwable t) {
            net.mcsm.extras.client.McsmMenuGuard.fault("logo-hide", t);
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IFII)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void mcsm$noLogoOnTitle4(GuiGraphicsExtractor g, int x, float partialTick,
                                     int a, int b, CallbackInfo ci) {
        try {
            hideVanillaLogo(ci);
        } catch (Throwable t) {
            net.mcsm.extras.client.McsmMenuGuard.fault("logo-hide", t);
        }
    }

    private static void hideVanillaLogo(CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mcsm$currentScreen(mc);
            if (screen instanceof TitleScreen) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
            // logo suppression is cosmetic; never fatal
        }
    }

    private static void mcsm$playIntro(GuiGraphicsExtractor g) {
        try {
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mcsm$currentScreen(mc);
            // the logo scene is the startup phase (no screen yet) - once the
            // title (or any screen) is up, the intro belongs to the menu
            if (screen != null) {
                return;
            }
            if (McsmCinematic.tickLogo()) {
                // guiWidth/guiHeight are the proven GUI-space dimensions
                // (the base HUD terminal uses the same pair)
                McsmCinematic.drawLogoSequence(g, g.guiWidth(), g.guiHeight());
            }
        } catch (Throwable ignored) {
            // a boot cinematic must never break a frame
        }
    }

    private static Screen mcsm$currentScreen(Minecraft mc) {
        try {
            for (java.lang.reflect.Field f : Minecraft.class.getDeclaredFields()) {
                if (Screen.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return (Screen) f.get(mc);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
