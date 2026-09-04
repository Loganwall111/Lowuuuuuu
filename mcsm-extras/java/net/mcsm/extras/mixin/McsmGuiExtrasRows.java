package net.mcsm.extras.mixin;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmStormFx;
import net.mcsm.extras.McsmStormBeaconBlock;

import net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * MCSM - extras rows inside the mod's own config screen.
 *
 * Their screen builds every control through two package-visible pieces:
 * Row.toggle/Row.slider/Row.header factories and addRowWidget(Row). We
 * reach those reflectively and append an "MCSM Storm Gameplay" section at
 * the end of init(), so the tabs' search, presets and skip-section logic
 * keep working on our rows for free. Fully silent on any failure: a future
 * refactor of their GUI costs us the rows, never a crash.
 */
@Mixin(WitherStormConfigScreen.class)
public abstract class McsmGuiExtrasRows {

    @Inject(method = {"init"}, at = @At("TAIL"))
    private void mcsm$extrasRows(CallbackInfo ci) {
        try {
            McsmExtrasConfig.load();
            Object self = this;
            Class<?> screen = WitherStormConfigScreen.class;
            Class<?> rowCls = Class.forName("net.dabicco.witherstormmod.client.gui.WitherStormConfigScreen$Row");
            Method mHeader = rowCls.getDeclaredMethod("header", String.class, int.class);
            Method mToggle = rowCls.getDeclaredMethod("toggle", String.class, String.class, DoubleSupplier.class, DoubleConsumer.class, Runnable.class);
            Method mSlider = rowCls.getDeclaredMethod("slider", String.class, String.class, double.class, double.class,
                    String.class, DoubleSupplier.class, DoubleConsumer.class, Runnable.class);
            Method mAdd = screen.getDeclaredMethod("addRowWidget", rowCls);
            for (Method m : new Method[]{mHeader, mToggle, mSlider, mAdd}) m.setAccessible(true);

            final Runnable commit = McsmExtrasConfig::save;

            mAdd.invoke(self, mHeader.invoke(null, "MCSM Storm Gameplay", 0));
            mAdd.invoke(self, mToggle.invoke(null, "Tentacle Grab",
                    "The storm self-triggers tentacle slams at survival players.",
                    (DoubleSupplier) () -> McsmExtrasConfig.enableTentacleGrab ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.enableTentacleGrab = v >= 0.5), commit));
            mAdd.invoke(self, mSlider.invoke(null, "Grab Interval",
                    "Seconds between slams. 0 disables the grab.",
                    0.0, 30.0, "%.1f s",
                    (DoubleSupplier) () -> McsmExtrasConfig.grabIntervalSeconds,
                    (DoubleConsumer) (v -> McsmExtrasConfig.grabIntervalSeconds = v), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Lit Beacon Relay",
                    "Vanilla beacons fire a shockwave and the storm summon path when lit.",
                    (DoubleSupplier) () -> McsmExtrasConfig.enableBeaconStorm ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.enableBeaconStorm = v >= 0.5), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Storm Beacon Block",
                    "Adds dabywitherstormmod:storm_beacon (needs world re-entry after change).",
                    (DoubleSupplier) () -> McsmExtrasConfig.enableBeaconBlock ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.enableBeaconBlock = v >= 0.5), commit));
            mAdd.invoke(self, mSlider.invoke(null, "Beacon Cooldown",
                    "Seconds a beacon/relay rests between shockwaves.",
                    2.0, 120.0, "%.0f s",
                    (DoubleSupplier) () -> McsmExtrasConfig.beaconCooldownSeconds,
                    (DoubleConsumer) (v -> McsmExtrasConfig.beaconCooldownSeconds = v), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Rise Ground FX",
                    "Spark and dust rings tear off the ground while the storm ascends.",
                    (DoubleSupplier) () -> McsmExtrasConfig.enableRiseFx ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.enableRiseFx = v >= 0.5), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Counterclockwise Spiral",
                    "Debris always winds into the mouth counterclockwise.",
                    (DoubleSupplier) () -> McsmExtrasConfig.spiralCounterClockwise ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.spiralCounterClockwise = v >= 0.5), commit));

            // ---- MCSM 1.9.98 visuals & events (phase 29/30 user orders) ----
            mAdd.invoke(self, mHeader.invoke(null, "MCSM Storm Visuals & Events", 0));
            mAdd.invoke(self, mSlider.invoke(null, "Glare Size",
                    "Scale of the storm's sky glare mass. 1.18 = a touch bigger than stock.",
                    0.25, 3.05, "%.2fx",
                    (DoubleSupplier) () -> McsmExtrasConfig.glareSize,
                    (DoubleConsumer) (v -> McsmExtrasConfig.glareSize = v), commit));
            mAdd.invoke(self, mToggle.invoke(null, "In-Mod Aurora",
                    "Aurora borealis at night without a shader pack (cold-biome biased).",
                    (DoubleSupplier) () -> McsmExtrasConfig.auroraEnabled ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.auroraEnabled = v >= 0.5), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Death Cinematic",
                    "Distortion -> white cracks -> shaking implosion -> supernova rings -> segments.",
                    (DoubleSupplier) () -> McsmExtrasConfig.deathCinematic ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.deathCinematic = v >= 0.5), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Supernova Rings",
                    "Ring shockwaves on phase-4 rise, phase-7 rise and at death.",
                    (DoubleSupplier) () -> McsmExtrasConfig.supernovaRings ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.supernovaRings = v >= 0.5), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Smoke Screen + Sparks",
                    "Skull impacts kick up grey ground smoke and yellow electric sparks.",
                    (DoubleSupplier) () -> McsmExtrasConfig.smokeScreen ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.smokeScreen = v >= 0.5), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Purple Sky (5.5+)",
                    "Purple lightning strikes and floating motes once the storm passes 5.5.",
                    (DoubleSupplier) () -> McsmExtrasConfig.purpleSky ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.purpleSky = v >= 0.5), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Dust Waves",
                    "Swoops against blocks leave dust trails.",
                    (DoubleSupplier) () -> McsmExtrasConfig.dustWaves ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.dustWaves = v >= 0.5), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Reality Tear",
                    "After the storm dies, a black-aurora tear corrupts the world until healed.",
                    (DoubleSupplier) () -> McsmExtrasConfig.realityTear ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.realityTear = v >= 0.5), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Obliterate Flash",
                    "The command block can erase entities from existence in one flash.",
                    (DoubleSupplier) () -> McsmExtrasConfig.obliterateFlash ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.obliterateFlash = v >= 0.5), commit));
            mAdd.invoke(self, mToggle.invoke(null, "Obliterate Kicks Players",
                    "Prank variant: the flash also kicks players. Off by default.",
                    (DoubleSupplier) () -> McsmExtrasConfig.obliterateKick ? 1.0 : 0.0,
                    (DoubleConsumer) (v -> McsmExtrasConfig.obliterateKick = v >= 0.5), commit));

            // MCSM 1.9.98 -- extras-tab scroll fix. User report: the tab's
            // scrollbar acted like a +/- stepper with "nothing to scroll down
            // to" -- rows appended at TAIL never told the screen its content
            // grew. Nudge every zero-arg scroll/relayout/refresh-style hook so
            // the bounds recalculate; absent methods are simply skipped.
            try {
                for (Method m : screen.getDeclaredMethods()) {
                    String n = m.getName().toLowerCase();
                    if (m.getParameterCount() == 0
                            && (n.contains("scroll") || n.contains("relayout")
                                || n.contains("refresh") || n.contains("layout")
                                || n.contains("reposition"))) {
                        try {
                            m.setAccessible(true);
                            m.invoke(self);
                        } catch (Throwable ignored) {
                            // a hook that refuses is not fatal -- others may work
                        }
                    }
                }
            } catch (Throwable ignored) {
                // never let a GUI nicety crash the config screen
            }
        } catch (Throwable t) {
            System.err.println("[MCSM] extras GUI rows skipped: " + t);
        }
    }
}
