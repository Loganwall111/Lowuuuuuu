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

import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * The MCSM Control Panel — our OWN screen so we never fight the mod config
 * screen's section-fold/tab machinery again.
 *
 * 26.2 GUI refactor uses extractRenderState(GuiGraphicsExtractor,...)
 * and exposes screen switching through Minecraft.setScreenAndShow(...).
 *
 * BUILD #390 (phase 1) ports the two premium UI pieces onto this stable
 * framework:
 *   * a vertical CHAPTER RAIL down the left edge — diamond nodes on a hairline
 *     track, hover/active states, click-to-jump to that chapter (the panel is
 *     one long scroll, the rail is the vertical navigation), and
 *   * DIAMOND-DOT SLIDERS — every slider handle is a rotated square drawn
 *     through the extractor's pose stack, with a cosmic-blue fill on the track.
 * Both are drawn with the exact primitive set already proven in this codebase
 * (fill / fillGradient / text / pose() pushMatrix-translate-rotate-scale-pop),
 * and no widget is re-implemented from a 26.1-era signature.
 */
public final class McsmExtrasScreen extends Screen {

    private static final int RAIL_X = 5;
    private static final int RAIL_W = 12;
    private static final int RAIL_TOP = 30;
    private static final int RAIL_BOTTOM_PAD = 40;

    /** A chapter anchor on the rail: a label plus the panel row it starts at. */
    private static final class Chapter {
        final String label;
        int y;
        Chapter(String label, int y) {
            this.label = label;
            this.y = y;
        }
    }

    private final Screen parent;
    private final List<AbstractWidget> chrome = new ArrayList<>();
    private final Map<AbstractWidget, Integer> baseY = new HashMap<>();
    private final List<Chapter> chapters = new ArrayList<>();
    private int scrollPx = 0;
    private int contentBottom = 0;
    private int railHover = -1;

    public McsmExtrasScreen(Screen parent) {
        super(Component.literal("MCSM Storm Control Panel"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.chrome.clear();
        this.baseY.clear();
        this.chapters.clear();
        this.contentBottom = 0;
        McsmExtrasConfig.load();

        int rowH = 22;
        int gap = 10;
        int colW = Math.min(250, Math.max(120, (this.width - 54 - gap) / 2));
        int left = 34;   // clears the chapter rail
        int top = 36;
        final int fColW = colW;

        // Build header
        Button ver = Button.builder(
                Component.literal("Story Mode Controls " + McsmExtrasConfig.BUILD_VERSION), b -> { })
                .bounds(left, top, fColW, 20).build();
        ver.active = false;
        this.addWidget(ver);
        this.chrome.add(ver);
        this.baseY.put(ver, top);

        // ---- Column 1: Visuals, Atmosphere & Shaders ------------------------
        int r1 = 1;
        chapter("VISUALS", top + 1 * rowH);
        addSlider(0, r1++, fColW, gap, left, top, rowH, "Glare Size", "%.2fx",
                0.25, 3.05, () -> McsmExtrasConfig.glareSize, v -> McsmExtrasConfig.glareSize = v);
        addSlider(0, r1++, fColW, gap, left, top, rowH, "Smudge Scale", "%.2fx",
                0.10, 2.00, () -> McsmExtrasConfig.smudgeScale, v -> McsmExtrasConfig.smudgeScale = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Non-Euclidean Glare",
                () -> McsmExtrasConfig.glareNonEuclidean, v -> McsmExtrasConfig.glareNonEuclidean = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Cosmic Blue Spotlights",
                () -> McsmExtrasConfig.cosmicSpotlights, v -> McsmExtrasConfig.cosmicSpotlights = v);
        addSlider(0, r1++, fColW, gap, left, top, rowH, "Night Navy Opacity", "%.2f",
                0.0, 1.0, () -> McsmExtrasConfig.nightSkyOpacity, v -> McsmExtrasConfig.nightSkyOpacity = v);
        addSlider(0, r1++, fColW, gap, left, top, rowH, "Phase 5.5 Threshold", "%.2f",
                5.0, 6.0, () -> McsmExtrasConfig.phase55Threshold, v -> McsmExtrasConfig.phase55Threshold = v);
        addSlider(0, r1++, fColW, gap, left, top, rowH, "Phase 5.9 Pink Mult", "%.2fx",
                0.2, 3.0, () -> McsmExtrasConfig.phase5_9PinkIntensity, v -> McsmExtrasConfig.phase5_9PinkIntensity = v);
        addSlider(0, r1++, fColW, gap, left, top, rowH, "Cloud Opacity", "%.2f",
                0.0, 1.0, () -> McsmExtrasConfig.cloudAlpha, v -> McsmExtrasConfig.cloudAlpha = v);
        addSlider(0, r1++, fColW, gap, left, top, rowH, "Cloud Speed", "%.2fx",
                0.0, 3.0, () -> McsmExtrasConfig.cloudSpeed, v -> McsmExtrasConfig.cloudSpeed = v);
        chapter("ATMOSPHERE", top + r1 * rowH);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "In-Mod Aurora",
                () -> McsmExtrasConfig.auroraEnabled, v -> McsmExtrasConfig.auroraEnabled = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Aurora Ribbons (4-Color)",
                () -> McsmExtrasConfig.auroraRibbons, v -> McsmExtrasConfig.auroraRibbons = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Snow Biome Blue Band",
                () -> McsmExtrasConfig.snowSkyBand, v -> McsmExtrasConfig.snowSkyBand = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Twinkling Multi Stars",
                () -> McsmExtrasConfig.twinklingStars, v -> McsmExtrasConfig.twinklingStars = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Night Comets / Streaks",
                () -> McsmExtrasConfig.comets, v -> McsmExtrasConfig.comets = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Magical Sparkles (W/P/P)",
                () -> McsmExtrasConfig.coloredSparkles, v -> McsmExtrasConfig.coloredSparkles = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Biome Mist & Fog VFX",
                () -> McsmExtrasConfig.biomeAtmospherics, v -> McsmExtrasConfig.biomeAtmospherics = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Nether Crimson Fog+Sparks",
                () -> McsmExtrasConfig.netherRedFog, v -> McsmExtrasConfig.netherRedFog = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Underwater God Rays & Haze",
                () -> McsmExtrasConfig.waterGodRays, v -> McsmExtrasConfig.waterGodRays = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "End Sky Vortex & Rip",
                () -> McsmExtrasConfig.endSkyVortex, v -> McsmExtrasConfig.endSkyVortex = v);
        chapter("LIGHTING", top + r1 * rowH);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Beacon Luminous Glow",
                () -> McsmExtrasConfig.beaconGlow, v -> McsmExtrasConfig.beaconGlow = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Nether & End Portal Lights",
                () -> McsmExtrasConfig.portalLights, v -> McsmExtrasConfig.portalLights = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Global Shadows & Contrast",
                () -> McsmExtrasConfig.globalShadows, v -> McsmExtrasConfig.globalShadows = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "OG CEM Models",
                () -> McsmExtrasConfig.ogCemModels, v -> McsmExtrasConfig.ogCemModels = v);
        addToggle(0, r1++, fColW, gap, left, top, rowH, "Built-in Shader Pack",
                () -> McsmExtrasConfig.embeddedShaderPack, v -> McsmExtrasConfig.embeddedShaderPack = v);

        // ---- Column 2: Gameplay, Story VFX & Entity AI ----------------------
        int r2 = 1;
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Enhanced Wither Storm AI",
                () -> McsmExtrasConfig.witherStormEnhancedAi, v -> McsmExtrasConfig.witherStormEnhancedAi = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "NPC Walk/Speak Animations",
                () -> McsmExtrasConfig.npcWalkAnimations, v -> McsmExtrasConfig.npcWalkAnimations = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Death Cinematic",
                () -> McsmExtrasConfig.deathCinematic, v -> McsmExtrasConfig.deathCinematic = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Supernova Rings",
                () -> McsmExtrasConfig.supernovaRings, v -> McsmExtrasConfig.supernovaRings = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Smoke Screen + Sparks",
                () -> McsmExtrasConfig.smokeScreen, v -> McsmExtrasConfig.smokeScreen = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Purple Sky (5.5+)",
                () -> McsmExtrasConfig.purpleSky, v -> McsmExtrasConfig.purpleSky = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Dust Waves",
                () -> McsmExtrasConfig.dustWaves, v -> McsmExtrasConfig.dustWaves = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Reality Tear",
                () -> McsmExtrasConfig.realityTear, v -> McsmExtrasConfig.realityTear = v);
        chapter("STORY VFX", top + r2 * rowH + 14);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Obliterate Flash",
                () -> McsmExtrasConfig.obliterateFlash, v -> McsmExtrasConfig.obliterateFlash = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Obliterate Kicks Players",
                () -> McsmExtrasConfig.obliterateKick, v -> McsmExtrasConfig.obliterateKick = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Tentacle Grab",
                () -> McsmExtrasConfig.enableTentacleGrab, v -> McsmExtrasConfig.enableTentacleGrab = v);
        addSlider(1, r2++, fColW, gap, left, top, rowH, "Grab Interval", "%.1f s",
                0.0, 30.0, () -> McsmExtrasConfig.grabIntervalSeconds, v -> McsmExtrasConfig.grabIntervalSeconds = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Lit Beacon Relay",
                () -> McsmExtrasConfig.enableBeaconStorm, v -> McsmExtrasConfig.enableBeaconStorm = v);
        addSlider(1, r2++, fColW, gap, left, top, rowH, "Beacon Cooldown", "%.0f s",
                2.0, 120.0, () -> McsmExtrasConfig.beaconCooldownSeconds, v -> McsmExtrasConfig.beaconCooldownSeconds = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Storm Beacon Block",
                () -> McsmExtrasConfig.enableBeaconBlock, v -> McsmExtrasConfig.enableBeaconBlock = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Rise Ground FX",
                () -> McsmExtrasConfig.enableRiseFx, v -> McsmExtrasConfig.enableRiseFx = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Counterclockwise Spiral",
                () -> McsmExtrasConfig.spiralCounterClockwise, v -> McsmExtrasConfig.spiralCounterClockwise = v);
        chapter("AI & WORLD", top + r2 * rowH + 14);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Force MCSM Look",
                () -> McsmExtrasConfig.forceMcsmLook, v -> McsmExtrasConfig.forceMcsmLook = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Force MCSM World",
                () -> McsmExtrasConfig.forceMcsmWorld, v -> McsmExtrasConfig.forceMcsmWorld = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Command Block Wire",
                () -> McsmExtrasConfig.commandWire, v -> McsmExtrasConfig.commandWire = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "MCSM Instructions",
                () -> McsmExtrasConfig.mcsmInstructions, v -> McsmExtrasConfig.mcsmInstructions = v);
        addToggle(1, r2++, fColW, gap, left, top, rowH, "Shader Pack Gate",
                () -> McsmExtrasConfig.shaderPackGate, v -> McsmExtrasConfig.shaderPackGate = v);

        // Re-apply button
        Button reapply = Button.builder(Component.literal("Re-apply MCSM Look now"), b -> {
            McsmGate.clearMemory();
            McsmGate.reset();
        }).bounds(left + fColW + gap, top + r2 * rowH, fColW, 20).build();
        this.addWidget(reapply);
        this.chrome.add(reapply);
        this.baseY.put(reapply, top + r2 * rowH);

        int maxRow = Math.max(r1, r2 + 1);
        this.contentBottom = top + maxRow * rowH + 10;
        applyScrollLayout();

        Button done = Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build();
        this.addWidget(done);
        this.chrome.add(done);
    }

    /**
     * Record a rail anchor. `y` is the panel y of the chapter's first row
     * (already including the column offset), so clicking a node scrolls that
     * chapter to the top of the viewport.
     */
    private void chapter(String label, int y) {
        for (Chapter c : this.chapters) {
            if (c.label.equals(label)) {
                c.y = y;
                return;
            }
        }
        this.chapters.add(new Chapter(label, y));
    }

    private static Component toggleLabel(String label, boolean on) {
        return Component.literal(label + ": " + (on ? "\u00a7aON" : "\u00a7cOFF"));
    }

    private void addToggle(int col, int row, int colW, int gap, int left, int top, int rowH,
                           String label, BooleanSupplier get, Consumer<Boolean> set) {
        int x = left + col * (colW + gap) + col * 18;
        int y = top + row * rowH + col * 14;
        Button b = Button.builder(toggleLabel(label, get.getAsBoolean()), btn -> {
            set.accept(!get.getAsBoolean());
            McsmExtrasConfig.save();
            McsmGate.reset();
            btn.setMessage(toggleLabel(label, get.getAsBoolean()));
        }).bounds(x, y, colW, 20).build();
        this.addWidget(b);
        this.chrome.add(b);
        this.baseY.put(b, y);
        this.contentBottom = Math.max(this.contentBottom, y + 20);
    }

    private void addSlider(int col, int row, int colW, int gap, int left, int top, int rowH,
                           String label, String fmt, double lo, double hi,
                           DoubleSupplier get, Consumer<Double> set) {
        int x = left + col * (colW + gap) + col * 18;
        int y = top + row * rowH + col * 14;
        Slider s = new Slider(x, y, colW, label, fmt, lo, hi, get, set);
        this.addWidget(s);
        this.chrome.add(s);
        this.baseY.put(s, y);
        this.contentBottom = Math.max(this.contentBottom, y + 20);
    }

    /**
     * BUILD #390 -- the diamond-dot slider.
     *
     * AbstractSliderButton still owns the value/drag plumbing (setValueFromMouse
     * / onDrag / mouseClicked are not touched), but the drawing is ours: a thin
     * recessed track, a cosmic-blue fill up to the value, and a rotated-square
     * handle with a hot white core. Rotation goes through the extractor's own
     * pose stack, which is the exact mechanism the shipped HUD terminal already
     * uses for its scaled item icons.
     */
    private static final class Slider extends AbstractSliderButton {
        private static final int DIAMOND = 0xFF4D4DFF;
        private static final int DIAMOND_HOT = 0xFFEAF0FF;
        private static final int TRACK = 0x88120A1E;
        private static final int TRACK_FILL = 0xCC6A8FF7;

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

        @Override
        public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
            int x = this.getX();
            int y = this.getY();
            int w = this.getWidth();
            int h = this.getHeight();
            int cy = y + h / 2;

            // track + fill up to the handle
            g.fill(x, cy - 2, x + w, cy + 2, TRACK);
            int fillTo = x + (int) Math.round(this.value * w);
            if (fillTo > x) {
                g.fill(x, cy - 2, fillTo, cy + 2, TRACK_FILL);
            }
            g.fill(x, cy - 2, x + w, cy - 1, 0x33FFFFFF);
            g.fill(x, cy + 1, x + w, cy + 2, 0x66000000);

            // diamond handle
            int hx = clampInt(fillTo, x + 2, x + w - 2);
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.translate(hx, cy);
            pose.rotate((float) (Math.PI / 4.0));
            boolean hot = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
            g.fill(-5, -5, 5, 5, 0xAA05030A);
            g.fill(-4, -4, 4, 4, DIAMOND);
            if (hot) {
                g.fill(-2, -2, 2, 2, DIAMOND_HOT);
            }
            pose.popMatrix();

            // label centred in the track (the vanilla slider look, kept)
            var font = Minecraft.getInstance().font;
            String text = this.getMessage().getString();
            g.centeredText(font, text, x + w / 2, y + h / 2 - 4, 0xFFFFFFFF);
        }

        private static int clampInt(int v, int lo, int hi) {
            return v < lo ? lo : (v > hi ? hi : v);
        }
    }

    private void applyScrollLayout() {
        int max = Math.max(0, this.contentBottom - (this.height - 36));
        if (this.scrollPx < 0) this.scrollPx = 0;
        if (this.scrollPx > max) this.scrollPx = max;
        for (Map.Entry<AbstractWidget, Integer> e : this.baseY.entrySet()) {
            AbstractWidget w = e.getKey();
            int y = e.getValue() - this.scrollPx;
            w.setY(y);
            boolean show = y >= 28 && y <= this.height - 42;
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

    /**
     * BUILD #390 -- the chapter rail's click target. A rail entry scrolls the
     * panel so that chapter's first row sits at the top of the viewport; the
     * existing scroll clamp keeps it from overshooting the end.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int hit = railHit(mouseX, mouseY);
        if (hit >= 0) {
            Chapter c = this.chapters.get(hit);
            this.scrollPx = Math.max(0, c.y - 36);
            applyScrollLayout();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** Which rail node is under the cursor, or -1. */
    private int railHit(double mouseX, double mouseY) {
        if (mouseX < RAIL_X - 2 || mouseX > RAIL_X + RAIL_W + 2) {
            return -1;
        }
        int y0 = RAIL_TOP;
        int y1 = this.height - RAIL_BOTTOM_PAD;
        int n = Math.max(1, this.chapters.size());
        int step = Math.max(14, (y1 - y0) / n);
        for (int i = 0; i < this.chapters.size(); i++) {
            int cy = y0 + i * step + step / 2;
            if (Math.abs(mouseY - cy) <= step / 2) {
                return i;
            }
        }
        return -1;
    }

    private void drawChapterRail(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (this.chapters.isEmpty()) {
            return;
        }
        int y0 = RAIL_TOP;
        int y1 = this.height - RAIL_BOTTOM_PAD;
        int n = this.chapters.size();
        int step = Math.max(14, (y1 - y0) / n);
        var font = Minecraft.getInstance().font;

        // hairline track
        int cyFirst = y0 + step / 2;
        int cyLast = y0 + (n - 1) * step + step / 2;
        g.fill(RAIL_X + RAIL_W / 2, cyFirst, RAIL_X + RAIL_W / 2 + 1, cyLast, 0x55C8D4F0);

        // active chapter = the last anchor scrolled past the top of the viewport
        int active = 0;
        for (int i = 0; i < n; i++) {
            if (this.chapters.get(i).y - this.scrollPx <= 40) {
                active = i;
            }
        }
        this.railHover = railHit(mouseX, mouseY);

        for (int i = 0; i < n; i++) {
            int cy = y0 + i * step + step / 2;
            boolean isActive = i == active;
            boolean isHover = i == this.railHover;
            int size = isActive ? 5 : (isHover ? 4 : 3);
            int col = isActive ? 0xFF4D4DFF : (isHover ? 0xFF9FB4D8 : 0x669AA6C0);
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.translate(RAIL_X + RAIL_W / 2, cy);
            pose.rotate((float) (Math.PI / 4.0));
            g.fill(-size, -size, size, size, isActive ? 0xFF05030A : 0x00000000);
            g.fill(-size + 1, -size + 1, size - 1, size - 1, col);
            if (isActive) {
                g.fill(-2, -2, 2, 2, 0xFFEAF0FF);
            }
            pose.popMatrix();
            if (isActive || isHover) {
                g.text(font, this.chapters.get(i).label, RAIL_X + RAIL_W + 4, cy - 4,
                        isActive ? 0xFFEAF2FF : 0xFFA0B4D8, true);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, 0xF0120A1E, 0xF005030A);
        g.fillGradient(0, 0, 18, this.height, 0xCC6A8FF7, 0x223F255A);
        g.fillGradient(this.width - 18, 0, this.width, this.height, 0x223F255A, 0xCC6A8FF7);
        g.fillGradient(0, 0, this.width, 3, 0xFF6A8FF7, 0xFF3F255A);
        g.fillGradient(0, this.height - 3, this.width, this.height, 0xFF3F255A, 0xFF6A8FF7);
        applyScrollLayout();
        drawChapterRail(g, mouseX, mouseY);
        g.text(this.font, "§bStory Mode Controls §8· §7" + McsmExtrasConfig.BUILD_VERSION, 34, 12, 0xFFEAF2FF, false);
        g.text(this.font, "§8Rail jumps chapters · scroll wheel moves the panel · Shift+C opens this anywhere.", 34, 24, 0xFFA0A0A0, false);
        if (this.contentBottom > this.height - 36) {
            g.centeredText(this.font, "scroll " + this.scrollPx + "/" + Math.max(0, this.contentBottom - (this.height - 36)), this.width - 62, 12, 0xA0A0A0);
        }
        for (AbstractWidget widget : this.chrome) {
            widget.extractRenderState(g, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void onClose() {
        McsmExtrasConfig.save();
        Minecraft.getInstance().setScreenAndShow(this.parent);
    }
}
