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
        } catch (Throwable t) {
            System.err.println("[MCSM] extras GUI rows skipped: " + t);
        }
    }
}
