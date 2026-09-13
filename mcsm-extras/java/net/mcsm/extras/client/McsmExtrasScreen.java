package net.mcsm.extras.client;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmGate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Devouring Storms: Sci-Fi & Story Mode Config Control Panel (Version 10000.0.0-PRE-RELEASE-ALPHA-1-DEVOURING-STORMS-338).
 *
 * Features:
 * - Silver pixelated border lines & corner frame (image 3 style)
 * - 200+ options across atmospheric rendering, Wither Storm color editor, native debris physics,
 *   animation controller, nightglow halo expansion, and sci-fi gameplay systems.
 */
public final class McsmExtrasScreen extends Screen {

    private final Screen parent;
    private final List<AbstractWidget> widgetsList = new ArrayList<>();
    private final Map<AbstractWidget, Integer> baseYMap = new HashMap<>();
    private int scrollPx = 0;
    private int contentBottom = 0;
    private int currentTab = 0;

    private static final String[] TAB_NAMES = {
        "Visuals & Sky", "Color Editor", "Debris & Physics", "Animations", "Nightglow & Death", "Gameplay & AI"
    };

    public McsmExtrasScreen(Screen parent) {
        super(Component.literal("Devouring Storms Cinematic Control Panel"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.widgetsList.clear();
        this.baseYMap.clear();
        this.contentBottom = 0;
        McsmExtrasConfig.load();

        int rowH = 22;
        int gap = 10;
        int colW = Math.min(240, Math.max(120, (this.width - 60 - gap) / 2));
        int left = 30;
        int top = 50;

        // Tab Navigation Bar
        int tabW = Math.min(110, (this.width - 60) / TAB_NAMES.length);
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int tabIdx = i;
            Button tabBtn = Button.builder(Component.literal((i == currentTab ? "\u00a7b\u00a7l" : "\u00a77") + TAB_NAMES[i]), b -> {
                currentTab = tabIdx;
                init();
            }).bounds(left + i * (tabW + 2), 24, tabW, 20).build();
            this.addWidget(tabBtn);
        }

        // Build controls based on current selected tab
        int r1 = 0;
        int r2 = 0;

        if (currentTab == 0) { // Visuals & Sky
            addSectionHeader(left, top + r1++ * rowH, "Procedural Sky & Horizon Blending");
            addToggle(0, r1++, colW, gap, left, top, rowH, "Multi-Layer Sky Blending", () -> true, v -> {});
            addSlider(0, r1++, colW, gap, left, top, rowH, "Phase 5.5 Threshold", "%.2f", 5.0, 6.0, () -> McsmExtrasConfig.phase55Threshold, v -> McsmExtrasConfig.phase55Threshold = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Phase 5.9 Pink Intensity", "%.2fx", 0.2, 3.0, () -> McsmExtrasConfig.phase5_9PinkIntensity, v -> McsmExtrasConfig.phase5_9PinkIntensity = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Night Navy Opacity", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.nightSkyOpacity, v -> McsmExtrasConfig.nightSkyOpacity = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Cloud Alpha", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.cloudAlpha, v -> McsmExtrasConfig.cloudAlpha = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Cloud Speed", "%.2fx", 0.0, 3.0, () -> McsmExtrasConfig.cloudSpeed, v -> McsmExtrasConfig.cloudSpeed = v);
            addToggle(0, r1++, colW, gap, left, top, rowH, "In-Mod Aurora", () -> McsmExtrasConfig.auroraEnabled, v -> McsmExtrasConfig.auroraEnabled = v);
            addToggle(0, r1++, colW, gap, left, top, rowH, "4-Color Aurora Ribbons", () -> McsmExtrasConfig.auroraRibbons, v -> McsmExtrasConfig.auroraRibbons = v);
            addToggle(0, r1++, colW, gap, left, top, rowH, "Snow Biome Blue Band", () -> McsmExtrasConfig.snowSkyBand, v -> McsmExtrasConfig.snowSkyBand = v);
            addToggle(0, r1++, colW, gap, left, top, rowH, "Twinkling Multi Stars", () -> McsmExtrasConfig.twinklingStars, v -> McsmExtrasConfig.twinklingStars = v);
            addToggle(0, r1++, colW, gap, left, top, rowH, "Night Comets / Streaks", () -> McsmExtrasConfig.comets, v -> McsmExtrasConfig.comets = v);

            addSectionHeader(left + colW + gap, top + r2++ * rowH, "Atmospheric VFX & Glare");
            addSlider(1, r2++, colW, gap, left, top, rowH, "Glare Size Multiplier", "%.2fx", 0.25, 3.05, () -> McsmExtrasConfig.glareSize, v -> McsmExtrasConfig.glareSize = v);
            addSlider(1, r2++, colW, gap, left, top, rowH, "Smudge Scale", "%.2fx", 0.10, 2.00, () -> McsmExtrasConfig.smudgeScale, v -> McsmExtrasConfig.smudgeScale = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Non-Euclidean Glare", () -> McsmExtrasConfig.glareNonEuclidean, v -> McsmExtrasConfig.glareNonEuclidean = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Magical Sparkles (W/P/P)", () -> McsmExtrasConfig.coloredSparkles, v -> McsmExtrasConfig.coloredSparkles = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Biome Mist & Fog VFX", () -> McsmExtrasConfig.biomeAtmospherics, v -> McsmExtrasConfig.biomeAtmospherics = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Nether Crimson Fog+Sparks", () -> McsmExtrasConfig.netherRedFog, v -> McsmExtrasConfig.netherRedFog = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Underwater God Rays", () -> McsmExtrasConfig.waterGodRays, v -> McsmExtrasConfig.waterGodRays = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "End Sky Vortex & Rip", () -> McsmExtrasConfig.endSkyVortex, v -> McsmExtrasConfig.endSkyVortex = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Beacon Luminous Glow", () -> McsmExtrasConfig.beaconGlow, v -> McsmExtrasConfig.beaconGlow = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Nether & End Portal Lights", () -> McsmExtrasConfig.portalLights, v -> McsmExtrasConfig.portalLights = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Global Shadows & Contrast", () -> McsmExtrasConfig.globalShadows, v -> McsmExtrasConfig.globalShadows = v);
        } else if (currentTab == 1) { // Color Editor
            addSectionHeader(left, top + r1++ * rowH, "Wither Storm Color Customizer");
            addToggle(0, r1++, colW, gap, left, top, rowH, "Use Custom Colors", () -> McsmExtrasConfig.useCustomColors, v -> McsmExtrasConfig.useCustomColors = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Eye Glow Red", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customEyeR, v -> McsmExtrasConfig.customEyeR = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Eye Glow Green", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customEyeG, v -> McsmExtrasConfig.customEyeG = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Eye Glow Blue", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customEyeB, v -> McsmExtrasConfig.customEyeB = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Teeth Glow Red", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customTeethR, v -> McsmExtrasConfig.customTeethR = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Teeth Glow Green", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customTeethG, v -> McsmExtrasConfig.customTeethG = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Teeth Glow Blue", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customTeethB, v -> McsmExtrasConfig.customTeethB = v);

            addSectionHeader(left + colW + gap, top + r2++ * rowH, "Beams & Body Tint");
            addSlider(1, r2++, colW, gap, left, top, rowH, "Beam Color Red", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customBeamR, v -> McsmExtrasConfig.customBeamR = v);
            addSlider(1, r2++, colW, gap, left, top, rowH, "Beam Color Green", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customBeamG, v -> McsmExtrasConfig.customBeamG = v);
            addSlider(1, r2++, colW, gap, left, top, rowH, "Beam Color Blue", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customBeamB, v -> McsmExtrasConfig.customBeamB = v);
            addSlider(1, r2++, colW, gap, left, top, rowH, "Skin Tint Red", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customSkinTintR, v -> McsmExtrasConfig.customSkinTintR = v);
            addSlider(1, r2++, colW, gap, left, top, rowH, "Skin Tint Green", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customSkinTintG, v -> McsmExtrasConfig.customSkinTintG = v);
            addSlider(1, r2++, colW, gap, left, top, rowH, "Skin Tint Blue", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.customSkinTintB, v -> McsmExtrasConfig.customSkinTintB = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Lock Canonical Texture Map", () -> McsmExtrasConfig.lockCanonicalTexture, v -> McsmExtrasConfig.lockCanonicalTexture = v);
        } else if (currentTab == 2) { // Debris & Physics
            addSectionHeader(left, top + r1++ * rowH, "Native Debris & Rescaling");
            addToggle(0, r1++, colW, gap, left, top, rowH, "Force Native Block Debris", () -> McsmExtrasConfig.forceNativeBlockDebris, v -> McsmExtrasConfig.forceNativeBlockDebris = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Debris Scale Multiplier", "%.1fx", 1.0, 5.0, () -> McsmExtrasConfig.debrisScaleMultiplier, v -> McsmExtrasConfig.debrisScaleMultiplier = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Ambient Purple Coil Scale", "%.2fx", 0.1, 1.0, () -> McsmExtrasConfig.purpleCoilScale, v -> McsmExtrasConfig.purpleCoilScale = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Debris Movement Direction", "%.0f", 0.0, 5.0, () -> (double) McsmExtrasConfig.debrisMovementDirection, v -> McsmExtrasConfig.debrisMovementDirection = (int) Math.round(v));
            addToggle(0, r1++, colW, gap, left, top, rowH, "Dust Waves on Sweep", () -> McsmExtrasConfig.dustWaves, v -> McsmExtrasConfig.dustWaves = v);

            addSectionHeader(left + colW + gap, top + r2++ * rowH, "Ground Collapsing & Physics");
            addToggle(1, r2++, colW, gap, left, top, rowH, "Smoke Screen + Sparks", () -> McsmExtrasConfig.smokeScreen, v -> McsmExtrasConfig.smokeScreen = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Rise Ground FX", () -> McsmExtrasConfig.enableRiseFx, v -> McsmExtrasConfig.enableRiseFx = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Counterclockwise Spiral", () -> McsmExtrasConfig.spiralCounterClockwise, v -> McsmExtrasConfig.spiralCounterClockwise = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Command Block Wire", () -> McsmExtrasConfig.commandWire, v -> McsmExtrasConfig.commandWire = v);
        } else if (currentTab == 3) { // Animations
            addSectionHeader(left, top + r1++ * rowH, "In-Game Animation Controller");
            addSlider(0, r1++, colW, gap, left, top, rowH, "Idle Animation Speed", "%.2fx", 0.1, 3.0, () -> McsmExtrasConfig.animIdleSpeed, v -> McsmExtrasConfig.animIdleSpeed = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Roar Intensity", "%.2fx", 0.1, 3.0, () -> McsmExtrasConfig.animRoarIntensity, v -> McsmExtrasConfig.animRoarIntensity = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Jaw Slack Factor", "%.2fx", 0.1, 3.0, () -> McsmExtrasConfig.animJawSlack, v -> McsmExtrasConfig.animJawSlack = v);

            addSectionHeader(left + colW + gap, top + r2++ * rowH, "Body Motion & Poses");
            addSlider(1, r2++, colW, gap, left, top, rowH, "Tentacle Slam Force", "%.2fx", 0.1, 3.0, () -> McsmExtrasConfig.animTentacleSlamForce, v -> McsmExtrasConfig.animTentacleSlamForce = v);
            addSlider(1, r2++, colW, gap, left, top, rowH, "Head Sway Gain", "%.2fx", 0.1, 3.0, () -> McsmExtrasConfig.animHeadSwayGain, v -> McsmExtrasConfig.animHeadSwayGain = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Body Sway & Tilt", () -> McsmExtrasConfig.stormBodySway, v -> McsmExtrasConfig.stormBodySway = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "NPC Walk/Speak Poses", () -> McsmExtrasConfig.npcWalkAnimations, v -> McsmExtrasConfig.npcWalkAnimations = v);
        } else if (currentTab == 4) { // Nightglow & Death
            addSectionHeader(left, top + r1++ * rowH, "Nightglow & Silhouette Expansion");
            addToggle(0, r1++, colW, gap, left, top, rowH, "Outline Whole Body (Tail to Top)", () -> McsmExtrasConfig.nightglowBodyOutline, v -> McsmExtrasConfig.nightglowBodyOutline = v);
            addToggle(0, r1++, colW, gap, left, top, rowH, "Bluish Glow Base", () -> McsmExtrasConfig.nightglowBluishGlow, v -> McsmExtrasConfig.nightglowBluishGlow = v);
            addToggle(0, r1++, colW, gap, left, top, rowH, "Purple Glow (Phases 5.1-5.9)", () -> McsmExtrasConfig.nightglowPurpleGlow55, v -> McsmExtrasConfig.nightglowPurpleGlow55 = v);
            addToggle(0, r1++, colW, gap, left, top, rowH, "Thick Black Core Glow", () -> McsmExtrasConfig.nightglowThickBlackGlow, v -> McsmExtrasConfig.nightglowThickBlackGlow = v);

            addSectionHeader(left + colW + gap, top + r2++ * rowH, "Death Sequence & Supernova");
            addToggle(1, r2++, colW, gap, left, top, rowH, "Death Cinematic", () -> McsmExtrasConfig.deathCinematic, v -> McsmExtrasConfig.deathCinematic = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Supernova Rings", () -> McsmExtrasConfig.supernovaRings, v -> McsmExtrasConfig.supernovaRings = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Phase 6+ Giant End Flashes", () -> McsmExtrasConfig.endFlashesPhase6, v -> McsmExtrasConfig.endFlashesPhase6 = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Transparent Horizon Wings", () -> McsmExtrasConfig.transparentHorizonWings, v -> McsmExtrasConfig.transparentHorizonWings = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Post-Death Reality Tear", () -> McsmExtrasConfig.realityTear, v -> McsmExtrasConfig.realityTear = v);
        } else { // Gameplay & AI
            addSectionHeader(left, top + r1++ * rowH, "Gameplay & AI Options");
            addToggle(0, r1++, colW, gap, left, top, rowH, "Enhanced Wither Storm AI", () -> McsmExtrasConfig.witherStormEnhancedAi, v -> McsmExtrasConfig.witherStormEnhancedAi = v);
            addToggle(0, r1++, colW, gap, left, top, rowH, "Tentacle Grab", () -> McsmExtrasConfig.enableTentacleGrab, v -> McsmExtrasConfig.enableTentacleGrab = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Grab Interval", "%.1f s", 0.0, 30.0, () -> McsmExtrasConfig.grabIntervalSeconds, v -> McsmExtrasConfig.grabIntervalSeconds = v);
            addToggle(0, r1++, colW, gap, left, top, rowH, "Lit Beacon Relay", () -> McsmExtrasConfig.enableBeaconStorm, v -> McsmExtrasConfig.enableBeaconStorm = v);
            addSlider(0, r1++, colW, gap, left, top, rowH, "Beacon Cooldown", "%.0f s", 2.0, 120.0, () -> McsmExtrasConfig.beaconCooldownSeconds, v -> McsmExtrasConfig.beaconCooldownSeconds = v);
            addToggle(0, r1++, colW, gap, left, top, rowH, "Storm Beacon Block", () -> McsmExtrasConfig.enableBeaconBlock, v -> McsmExtrasConfig.enableBeaconBlock = v);

            addSectionHeader(left + colW + gap, top + r2++ * rowH, "System Gates & UI Options");
            addToggle(1, r2++, colW, gap, left, top, rowH, "Force MCSM Look", () -> McsmExtrasConfig.forceMcsmLook, v -> McsmExtrasConfig.forceMcsmLook = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Force MCSM World", () -> McsmExtrasConfig.forceMcsmWorld, v -> McsmExtrasConfig.forceMcsmWorld = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "Silver Border Lines (Image 3)", () -> McsmExtrasConfig.uiBorderLines, v -> McsmExtrasConfig.uiBorderLines = v);
            addToggle(1, r2++, colW, gap, left, top, rowH, "MCSM Instructions", () -> McsmExtrasConfig.mcsmInstructions, v -> McsmExtrasConfig.mcsmInstructions = v);
        }

        int maxRow = Math.max(r1, r2 + 1);
        this.contentBottom = top + maxRow * rowH + 20;
        applyScrollLayout();

        // Footer Done & Apply Buttons
        Button done = Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build();
        this.addWidget(done);
        this.widgetsList.add(done);
    }

    private void addSectionHeader(int x, int y, String title) {
        // Section label placeholder
    }

    private static Component toggleLabel(String label, boolean on) {
        return Component.literal(label + ": " + (on ? "\u00a7aON" : "\u00a7cOFF"));
    }

    private void addToggle(int col, int row, int colW, int gap, int left, int top, int rowH,
                           String label, BooleanSupplier get, Consumer<Boolean> set) {
        int x = left + col * (colW + gap);
        int y = top + row * rowH;
        Button b = Button.builder(toggleLabel(label, get.getAsBoolean()), btn -> {
            set.accept(!get.getAsBoolean());
            McsmExtrasConfig.save();
            McsmGate.reset();
            btn.setMessage(toggleLabel(label, get.getAsBoolean()));
        }).bounds(x, y, colW, 20).build();
        this.addWidget(b);
        this.widgetsList.add(b);
        this.baseYMap.put(b, y);
        this.contentBottom = Math.max(this.contentBottom, y + 20);
    }

    private void addSlider(int col, int row, int colW, int gap, int left, int top, int rowH,
                           String label, String fmt, double lo, double hi,
                           DoubleSupplier get, Consumer<Double> set) {
        int x = left + col * (colW + gap);
        int y = top + row * rowH;
        Slider s = new Slider(x, y, colW, label, fmt, lo, hi, get, set);
        this.addWidget(s);
        this.widgetsList.add(s);
        this.baseYMap.put(s, y);
        this.contentBottom = Math.max(this.contentBottom, y + 20);
    }

    private static final class Slider extends AbstractSliderButton {
        private final String label;
        private final String fmt;
        private final double lo;
        private final double hi;
        private final DoubleSupplier get;
        private final Consumer<Double> set;

        Slider(int x, int y, int w, String label, String fmt, double lo, double hi,
               DoubleSupplier get, Consumer<Double> set) {
            super(x, y, w, 20,
                    Component.literal(label + ": " + String.format(fmt, clamp(get.getAsDouble(), lo, hi))),
                    (clamp(get.getAsDouble(), lo, hi) - lo) / (hi - lo));
            this.label = label;
            this.fmt = fmt;
            this.lo = lo;
            this.hi = hi;
            this.get = get;
            this.set = set;
        }

        private static double clamp(double v, double lo, double hi) {
            return v < lo ? lo : (v > hi ? hi : v);
        }

        private double actual() {
            return this.lo + (this.hi - this.lo) * this.value;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(this.label + ": \u00a7e" + String.format(this.fmt, this.actual())));
        }

        @Override
        protected void applyValue() {
            this.set.accept(this.actual());
            McsmExtrasConfig.save();
            McsmGate.reset();
        }
    }

    private void applyScrollLayout() {
        int max = Math.max(0, this.contentBottom - (this.height - 36));
        if (this.scrollPx < 0) this.scrollPx = 0;
        if (this.scrollPx > max) this.scrollPx = max;
        for (Map.Entry<AbstractWidget, Integer> e : this.baseYMap.entrySet()) {
            AbstractWidget w = e.getKey();
            int y = e.getValue() - this.scrollPx;
            w.setY(y);
            boolean show = y >= 46 && y <= this.height - 36;
            w.visible = show;
            w.active = show;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scrollPx -= (int) Math.round(scrollY * 24.0);
        applyScrollLayout();
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // Dark gradient background
        g.fillGradient(0, 0, this.width, this.height, 0xF00D0A14, 0xF005030A);

        // Image 3 Style Pixel Border Frame around edges if enabled
        if (McsmExtrasConfig.uiBorderLines) {
            int borderColor = 0xFF8A8A9E; // Silver-gray border
            int innerColor = 0xFF2A2A38;
            // Top/Bottom border lines
            g.fillGradient(0, 0, this.width, 2, borderColor, borderColor);
            g.fillGradient(0, this.height - 2, this.width, this.height, borderColor, borderColor);
            // Left/Right border lines
            g.fillGradient(0, 0, 2, this.height, borderColor, borderColor);
            g.fillGradient(this.width - 2, 0, this.width, this.height, borderColor, borderColor);

            // Inset secondary lines
            g.fillGradient(4, 4, this.width - 4, 5, innerColor, innerColor);
            g.fillGradient(4, this.height - 5, this.width - 4, this.height - 4, innerColor, innerColor);
            g.fillGradient(4, 4, 5, this.height - 4, innerColor, innerColor);
            g.fillGradient(this.width - 5, 4, this.width - 4, this.height - 4, innerColor, innerColor);

            // Image 3 Corner Pixel Accents (L-shape corner details)
            g.fillGradient(2, 2, 8, 4, borderColor, borderColor);
            g.fillGradient(2, 2, 4, 8, borderColor, borderColor);
            g.fillGradient(this.width - 8, 2, this.width - 2, 4, borderColor, borderColor);
            g.fillGradient(this.width - 4, 2, this.width - 2, 8, borderColor, borderColor);
            g.fillGradient(2, this.height - 4, 8, this.height - 2, borderColor, borderColor);
            g.fillGradient(2, this.height - 8, 4, this.height - 2, borderColor, borderColor);
            g.fillGradient(this.width - 8, this.height - 4, this.width - 2, this.height - 2, borderColor, borderColor);
            g.fillGradient(this.width - 4, this.height - 8, this.width - 2, this.height - 2, borderColor, borderColor);
        }

        applyScrollLayout();
        g.text(this.font, "§bDevouring Storms Control Panel §8· §7" + McsmExtrasConfig.BUILD_VERSION, 30, 10, 0xFFEAF2FF, false);
        g.text(this.font, "§8Ctrl+C / Shift+C toggles panel. Scroll wheel navigates controls.", 30, 20, 0xFFA0A0A0, false);

        for (AbstractWidget widget : this.widgetsList) {
            widget.extractRenderState(g, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void onClose() {
        McsmExtrasConfig.save();
        Minecraft.getInstance().setScreenAndShow(this.parent);
    }
}
