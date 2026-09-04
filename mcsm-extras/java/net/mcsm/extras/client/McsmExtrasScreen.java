package net.mcsm.extras.client;

import net.mcsm.extras.McsmExtrasConfig;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * The MCSM Control Panel — our OWN screen so we never fight the mod config
 * screen's section-fold/tab machinery again (2026-09-04 bug: inline rows never
 * saw a relayout; the header showed [-] with an empty body).
 *
 * Deliberately conservative API surface: CycleButton.onOffBuilder(...) and
 * AbstractSliderButton are the two longest-lived widgets in Mojmap; the screen
 * overrides only init() and onClose(), so vanilla owns all rendering/extraction
 * — nothing here depends on the 26.2 render-refactor signatures.
 * Compile-verified only in CI; runtime-safe (on failure the extras button in
 * the mod's own screen simply does nothing further).
 */
public final class McsmExtrasScreen extends Screen {

    private final Screen parent;

    public McsmExtrasScreen(Screen parent) {
        super(Component.literal("MCSM Storm Control Panel"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        McsmExtrasConfig.load();

        int cols = 2;
        int colW = Math.min(250, (this.width - 48 - 24) / cols);
        int left = (this.width - (colW * cols + 24)) / cols;
        int top = 40;
        int rowH = 22;

        // ---- column 1: visuals ----------------------------------------------
        int c = 0, r = 0;
        addSlider(left + c * (colW + 24), top + r++ * rowH, colW, "Glare Size",
                "%.2fx", 0.25, 3.05, () -> McsmExtrasConfig.glareSize, v -> McsmExtrasConfig.glareSize = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "In-Mod Aurora",
                () -> McsmExtrasConfig.auroraEnabled, v -> McsmExtrasConfig.auroraEnabled = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Death Cinematic",
                () -> McsmExtrasConfig.deathCinematic, v -> McsmExtrasConfig.deathCinematic = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Supernova Rings",
                () -> McsmExtrasConfig.supernovaRings, v -> McsmExtrasConfig.supernovaRings = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Smoke Screen + Sparks",
                () -> McsmExtrasConfig.smokeScreen, v -> McsmExtrasConfig.smokeScreen = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Purple Sky (5.5+)",
                () -> McsmExtrasConfig.purpleSky, v -> McsmExtrasConfig.purpleSky = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Dust Waves",
                () -> McsmExtrasConfig.dustWaves, v -> McsmExtrasConfig.dustWaves = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Reality Tear",
                () -> McsmExtrasConfig.realityTear, v -> McsmExtrasConfig.realityTear = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "OG CEM Models",
                () -> McsmExtrasConfig.ogCemModels, v -> McsmExtrasConfig.ogCemModels = v);
        addSlider(left + c * (colW + 24), top + r++ * rowH, colW, "Smudge Scale",
                "%.2fx", 0.10, 2.00, () -> McsmExtrasConfig.smudgeScale, v -> McsmExtrasConfig.smudgeScale = v);

        // ---- column 2: gameplay ---------------------------------------------
        c = 1; r = 0;
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Obliterate Flash",
                () -> McsmExtrasConfig.obliterateFlash, v -> McsmExtrasConfig.obliterateFlash = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Obliterate Kicks Players",
                () -> McsmExtrasConfig.obliterateKick, v -> McsmExtrasConfig.obliterateKick = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Tentacle Grab",
                () -> McsmExtrasConfig.enableTentacleGrab, v -> McsmExtrasConfig.enableTentacleGrab = v);
        addSlider(left + c * (colW + 24), top + r++ * rowH, colW, "Grab Interval",
                "%.1f s", 0.0, 30.0, () -> McsmExtrasConfig.grabIntervalSeconds, v -> McsmExtrasConfig.grabIntervalSeconds = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Lit Beacon Relay",
                () -> McsmExtrasConfig.enableBeaconStorm, v -> McsmExtrasConfig.enableBeaconStorm = v);
        addSlider(left + c * (colW + 24), top + r++ * rowH, colW, "Beacon Cooldown",
                "%.0f s", 2.0, 120.0, () -> McsmExtrasConfig.beaconCooldownSeconds, v -> McsmExtrasConfig.beaconCooldownSeconds = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Storm Beacon Block",
                () -> McsmExtrasConfig.enableBeaconBlock, v -> McsmExtrasConfig.enableBeaconBlock = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Rise Ground FX",
                () -> McsmExtrasConfig.enableRiseFx, v -> McsmExtrasConfig.enableRiseFx = v);
        addToggle(left + c * (colW + 24), top + r++ * rowH, colW, "Counterclockwise Spiral",
                () -> McsmExtrasConfig.spiralCounterClockwise, v -> McsmExtrasConfig.spiralCounterClockwise = v);

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private void addToggle(int x, int y, int w, String label, BooleanSupplier get, Consumer<Boolean> set) {
        this.addRenderableWidget(CycleButton.onOffBuilder(get.getAsBoolean()).create(
                x, y, w, 20, Component.literal(label),
                (btn, val) -> { set.accept(val); McsmExtrasConfig.save(); }));
    }

    private void addSlider(int x, int y, int w, String label, String fmt,
                           double lo, double hi, DoubleSupplier get, Consumer<Double> set) {
        double norm = (get.getAsDouble() - lo) / (hi - lo);
        this.addRenderableWidget(new AbstractSliderButton(x, y, w, 20,
                Component.literal(String.format(label + " " + fmt, get.getAsDouble())), norm) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal(String.format(label + " " + fmt, lo + this.value * (hi - lo))));
            }

            @Override
            protected void applyValue() {
                set.accept(lo + this.value * (hi - lo));
                McsmExtrasConfig.save();
            }
        });
    }

    @Override
    public void onClose() {
        McsmExtrasConfig.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
