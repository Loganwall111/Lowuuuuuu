package net.mcsm.extras.mixin;

import net.mcsm.extras.McsmExtrasConfig;

import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

/**
 * MCSM - extras entry inside the mod's own config screen.
 *
 * MCSM 1.9.98 -- ROOT-CAUSED FIX for "clicking [+]/- shows no options"
 * (user screenshot 2026-09-04 145751). Their screen folds content by section
 * (collapsed set) and keys rows to tabs; rows we appended at init() TAIL were
 * laid out by repositionRows() BEFORE we added them, so the "MCSM extras"
 * header got a [-] state but its rows never received bounds -> empty section.
 * Two attempted generations of direct row injection (toggles/sliders inline)
 * hit exactly this.
 *
 * New contract, deliberately tiny: ONE header row + ONE button row. The button
 * opens our own full panel (net.mcsm.extras.client.McsmExtrasScreen), which we
 * control end to end -- no dependence on their fold internals at all. Then we
 * call their repositionRows() by exact name so the two rows get laid out
 * (verified from the shipped jar's method table: rebuild() regenerates and
 * would drop us -- never call it).
 *
 * Fully silent on any failure: a future refactor of their GUI costs us the
 * two rows, never a crash.
 */
@Mixin(WitherStormConfigScreen.class)
public abstract class McsmGuiExtrasRows {

    @Inject(method = {"init"}, at = @At("TAIL"))
    private void mcsm$extrasRows(CallbackInfo ci) {
        try {
            McsmExtrasConfig.load();
            final Object self = this;
            Class<?> screen = WitherStormConfigScreen.class;
            Class<?> rowCls = Class.forName("net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen$Row");
            Method mHeader = rowCls.getDeclaredMethod("header", String.class, int.class);
            Method mButton = rowCls.getDeclaredMethod("button", String.class, String.class, Runnable.class);
            Method mAdd = screen.getDeclaredMethod("addRowWidget", rowCls);
            for (Method m : new Method[]{mHeader, mButton, mAdd}) m.setAccessible(true);

            mAdd.invoke(self, mHeader.invoke(null, "MCSM extras 1.9.98", 0));
            mAdd.invoke(self, mButton.invoke(null,
                    "Open the MCSM Control Panel",
                    "Glare size, aurora, death cinematic, supernova rings, smoke screen, purple sky, dust waves, reality tear, obliterate flash, and the gameplay patches.",
                    // 26.2: screen switching moved to Minecraft.gui.setScreen(...)
                    (Runnable) () -> net.minecraft.client.Minecraft.getInstance().gui.setScreen(
                            new net.mcsm.extras.client.McsmExtrasScreen((net.minecraft.client.gui.screens.Screen) self))));

            // exact-name relayout (see class doc for why repositionRows, not rebuild)
            try {
                Method mReposition = screen.getDeclaredMethod("repositionRows");
                mReposition.setAccessible(true);
                mReposition.invoke(self);
                System.err.println("[MCSM] extras rows: relayout OK (repositionRows)");
            } catch (Throwable t2) {
                System.err.println("[MCSM] extras rows relayout skipped: " + t2);
            }
        } catch (Throwable t) {
            System.err.println("[MCSM] extras GUI rows skipped: " + t);
        }
    }
}
