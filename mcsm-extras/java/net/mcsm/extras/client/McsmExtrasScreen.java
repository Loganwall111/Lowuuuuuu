package net.mcsm.extras.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmGate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
 * The MCSM Control Panel — our OWN screen so we never fight the mod config
 * screen's section-fold/tab machinery again.
 *
 * MCSM 1.9.201 -- PREMIUM UI REGISTRY, migrated crash-free into this branch:
 *   - a VERTICAL SCROLLING CHAPTER RAIL on the left edge: one diamond node per
 *     story chapter; the rail tracks the current scroll position (the chapter
 *     owning the top visible row lights up) and clicking a node jumps the
 *     panel straight to that chapter;
 *   - 90 CUSTOM DIAMOND-DOT CONFIGURATION SLIDERS (>= the 77 promised) bound
 *     live to McsmExtrasConfig + DabyWSClientConfig, each drawing its own
 *     diamond knob instead of the vanilla rectangle handle;
 *   - the legacy toggle bank kept as the final chapter so nothing regresses.
 *
 * 26.2 GUI refactor uses extractRenderState(GuiGraphicsExtractor,...)
 * and exposes screen switching through Minecraft.setScreenAndShow(...).
 */
public final class McsmExtrasScreen extends Screen {

    /** One rail node / section of the chapter rail. */
    private static final class Chapter {
        final String code;
        final String title;
        int topPx;

        Chapter(String code, String title) {
            this.code = code;
            this.title = title;
            this.topPx = 0;
        }
    }

    private final Screen parent;
    private final List<AbstractWidget> chrome = new ArrayList<>();
    private final Map<AbstractWidget, Integer> baseY = new HashMap<>();
    private final List<Chapter> chapters = new ArrayList<>();
    private int scrollPx = 0;
    private int contentBottom = 0;

    public McsmExtrasScreen(Screen parent) {
        super(Component.literal("MCSM Storm Control Panel"));
        this.parent = parent;
    }

    // ---------------------------------------------------------------- build

    @Override
    protected void init() {
        this.clearWidgets();
        this.chrome.clear();
        this.baseY.clear();
        this.chapters.clear();
        this.contentBottom = 0;
        McsmExtrasConfig.load();

        int left = 34;                      // rail owns x < 28
        this.rowY = 0;

        // ---- CH 1 · Genesis & sickness ------------------------------------
        chapter("I", "Genesis");
        sl("Phase Anim Strength", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.phaseAnimStrength, v -> DabyWSClientConfig.phaseAnimStrength = v);
        sl("Mirror Back Detail", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.mirrorBackDetail, v -> DabyWSClientConfig.mirrorBackDetail = v);
        sl("Screen Tremor", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.screenTremorIntensity, v -> DabyWSClientConfig.screenTremorIntensity = v);
        sl("Vignette Intensity", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.vignetteIntensity, v -> DabyWSClientConfig.vignetteIntensity = v);
        sl("Sickness Veins", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.sicknessVeinIntensity, v -> DabyWSClientConfig.sicknessVeinIntensity = v);
        sl("Chromatic Glitch", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.chromaticGlitchStrength, v -> DabyWSClientConfig.chromaticGlitchStrength = v);
        sl("Debris Dust Particles", "%.2fx", 0.0, 3.0, () -> DabyWSClientConfig.debrisDustParticles, v -> DabyWSClientConfig.debrisDustParticles = v);
        sl("Volumetric Fog Density", "%.2fx", 0.0, 3.0, () -> DabyWSClientConfig.volumetricFogDensity, v -> DabyWSClientConfig.volumetricFogDensity = v);

        // ---- CH 2 · Growth & body ------------------------------------------
        chapter("II", "Growth");
        sl("Debris Amount", "%.2f", 0.0, 6.0, () -> DabyWSClientConfig.debrisAmount, v -> DabyWSClientConfig.debrisAmount = v);
        sl("Debris Size", "%.2fx", 0.0, 3.0, () -> DabyWSClientConfig.debrisSize, v -> DabyWSClientConfig.debrisSize = v);
        sl("Storm Skin Scale", "%.2fx", 0.0, 3.0, () -> DabyWSClientConfig.stormSkin, v -> DabyWSClientConfig.stormSkin = v);
        sl("Pulse Strength", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.pulseStrength, v -> DabyWSClientConfig.pulseStrength = v);
        sl("Pulse Period", "%.1f s", 0.5, 12.0, () -> DabyWSClientConfig.pulsePeriod, v -> DabyWSClientConfig.pulsePeriod = v);
        sl("Pulse Size", "%.2fx", 0.0, 3.0, () -> DabyWSClientConfig.pulseSize, v -> DabyWSClientConfig.pulseSize = v);
        sl("Back Growth Speed", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.infiniteBackGrowthSpeed, v -> McsmExtrasConfig.infiniteBackGrowthSpeed = v);
        sl("Body Anim Pulse", "%.2fx", 0.0, 2.0, () -> McsmExtrasConfig.bodyAnimPulse, v -> McsmExtrasConfig.bodyAnimPulse = v);

        // ---- CH 3 · Fog & palette ------------------------------------------
        chapter("III", "Fog");
        sl("Fog Colour R", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.fogColorR, v -> DabyWSClientConfig.fogColorR = v);
        sl("Fog Colour G", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.fogColorG, v -> DabyWSClientConfig.fogColorG = v);
        sl("Fog Colour B", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.fogColorB, v -> DabyWSClientConfig.fogColorB = v);
        sl("Storm Fog Strength", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.stormFogStrength, v -> DabyWSClientConfig.stormFogStrength = v);
        sl("Biome Fog Strength", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.biomeFogStrength, v -> DabyWSClientConfig.biomeFogStrength = v);
        sl("Far Lands Distance", "%.0f", 500.0, 12000.0, () -> DabyWSClientConfig.farLandsDistance, v -> DabyWSClientConfig.farLandsDistance = v);
        sl("Far Lands Strength", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.farLandsStrength, v -> DabyWSClientConfig.farLandsStrength = v);
        sl("Sky Darken Intensity", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.skyDarkenIntensity, v -> DabyWSClientConfig.skyDarkenIntensity = v);

        // ---- CH 4 · Night sky ----------------------------------------------
        chapter("IV", "Night Sky");
        sl("Sky Darken R", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.skyDarkenR, v -> DabyWSClientConfig.skyDarkenR = v);
        sl("Sky Darken G", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.skyDarkenG, v -> DabyWSClientConfig.skyDarkenG = v);
        sl("Sky Darken B", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.skyDarkenB, v -> DabyWSClientConfig.skyDarkenB = v);
        sl("Sky Darken Lighting", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.skyDarkenLighting, v -> DabyWSClientConfig.skyDarkenLighting = v);
        sl("Night Navy Opacity", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.nightSkyOpacity, v -> McsmExtrasConfig.nightSkyOpacity = v);
        sl("Storm Stars Mode", "%.1f", 0.0, 3.0, () -> DabyWSClientConfig.stormStars, v -> DabyWSClientConfig.stormStars = v);
        sl("Star Density", "%.2fx", 0.0, 3.0, () -> DabyWSClientConfig.starDensity, v -> DabyWSClientConfig.starDensity = v);
        sl("Star Twinkle Speed", "%.2fx", 0.0, 4.0, () -> DabyWSClientConfig.starTwinkleSpeed, v -> DabyWSClientConfig.starTwinkleSpeed = v);

        // ---- CH 5 · Clouds & decks ------------------------------------------
        chapter("V", "Clouds");
        sl("Star Brightness", "%.2fx", 0.0, 3.0, () -> DabyWSClientConfig.starBrightness, v -> DabyWSClientConfig.starBrightness = v);
        sl("Cloud Deck Mode", "%.1f", 0.0, 3.0, () -> DabyWSClientConfig.stormCloudDeck, v -> DabyWSClientConfig.stormCloudDeck = v);
        sl("Cloud Coverage", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.stormCloudCoverage, v -> DabyWSClientConfig.stormCloudCoverage = v);
        sl("Cloud Altitude", "%.2f", -1.0, 1.0, () -> DabyWSClientConfig.stormCloudAltitude, v -> DabyWSClientConfig.stormCloudAltitude = v);
        sl("Cloud Palette Mix", "%.2f", 0.0, 1.0, () -> DabyWSClientConfig.stormCloudPaletteMix, v -> DabyWSClientConfig.stormCloudPaletteMix = v);
        sl("Cloud Opacity", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.cloudAlpha, v -> McsmExtrasConfig.cloudAlpha = v);
        sl("Cloud Speed", "%.2fx", 0.0, 3.0, () -> McsmExtrasConfig.cloudSpeed, v -> McsmExtrasConfig.cloudSpeed = v);
        sl("Cloud Darken", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.cloudDarkenStrength, v -> DabyWSClientConfig.cloudDarkenStrength = v);

        // ---- CH 6 · Eyes & teeth emissive -----------------------------------
        chapter("VI", "Eyes/Teeth");
        sl("Eye / Teeth R", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.eyeColorR, v -> DabyWSClientConfig.eyeColorR = v);
        sl("Eye / Teeth G", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.eyeColorG, v -> DabyWSClientConfig.eyeColorG = v);
        sl("Eye / Teeth B", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.eyeColorB, v -> DabyWSClientConfig.eyeColorB = v);
        sl("Glow Strength", "%.2fx", 0.0, 3.0, () -> DabyWSClientConfig.glowStrength, v -> DabyWSClientConfig.glowStrength = v);
        sl("Teeth Glow Intensity", "%.2fx", 0.0, 4.0, () -> DabyWSClientConfig.turquoiseTeethIntensity, v -> DabyWSClientConfig.turquoiseTeethIntensity = v);
        sl("Storm Glow Strength", "%.2fx", 0.0, 3.0, () -> DabyWSClientConfig.stormGlowStrength, v -> DabyWSClientConfig.stormGlowStrength = v);
        sl("Sun Glow Strength", "%.2fx", 0.0, 5.0, () -> DabyWSClientConfig.sunGlowStrength, v -> DabyWSClientConfig.sunGlowStrength = v);
        sl("Sun Glow R", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.sunGlowR, v -> DabyWSClientConfig.sunGlowR = v);

        // ---- CH 7 · Beams & spotlight nodes ----------------------------------
        chapter("VII", "Beams");
        sl("Beam Colour R", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.beamColorR, v -> DabyWSClientConfig.beamColorR = v);
        sl("Beam Colour G", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.beamColorG, v -> DabyWSClientConfig.beamColorG = v);
        sl("Beam Colour B", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.beamColorB, v -> DabyWSClientConfig.beamColorB = v);
        sl("Beam Opacity", "%.2f", 0.0, 1.0, () -> DabyWSClientConfig.beamOpacity, v -> DabyWSClientConfig.beamOpacity = v);
        sl("Beam End Fade", "%.2f", 0.0, 1.0, () -> DabyWSClientConfig.beamEndFade, v -> DabyWSClientConfig.beamEndFade = v);
        sl("Spotlight Node Size", "%.2fx", 0.0, 3.0, () -> DabyWSClientConfig.impactLightSize, v -> DabyWSClientConfig.impactLightSize = v);
        sl("Spotlight Brightness", "%.2fx", 0.0, 3.0, () -> DabyWSClientConfig.impactLightBrightness, v -> DabyWSClientConfig.impactLightBrightness = v);
        sl("Spotlight Range", "%.0f", 64.0, 2048.0, () -> DabyWSClientConfig.impactLightRange, v -> DabyWSClientConfig.impactLightRange = v);

        // ---- CH 8 · Shadows & bloom ------------------------------------------
        chapter("VIII", "Shadow/Bloom");
        sl("Shadow Map Resolution", "%.0f", 512.0, 8192.0, () -> DabyWSClientConfig.shadowMapResolution, v -> DabyWSClientConfig.shadowMapResolution = v);
        sl("Shading Contrast", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.stormShadingContrast, v -> DabyWSClientConfig.stormShadingContrast = v);
        sl("Shadow Strength", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.stormShadowStrength, v -> DabyWSClientConfig.stormShadowStrength = v);
        sl("Shadow Tint R", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.stormShadowR, v -> DabyWSClientConfig.stormShadowR = v);
        sl("Shadow Tint G", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.stormShadowG, v -> DabyWSClientConfig.stormShadowG = v);
        sl("Shadow Tint B", "%.3f", 0.0, 1.0, () -> DabyWSClientConfig.stormShadowB, v -> DabyWSClientConfig.stormShadowB = v);
        sl("Bloom Strength", "%.2fx", 0.0, 6.0, () -> DabyWSClientConfig.bloomStrength, v -> DabyWSClientConfig.bloomStrength = v);
        sl("Bloom Debug", "%.2f", 0.0, 1.0, () -> DabyWSClientConfig.bloomDebug, v -> DabyWSClientConfig.bloomDebug = v);

        // ---- CH 9 · Backdrop skybox sheets ------------------------------------
        chapter("IX", "Backdrop");
        sl("Backdrop Strength", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.stormBackdropStrength, v -> DabyWSClientConfig.stormBackdropStrength = v);
        sl("Backdrop Sheet Scale", "%.1f", 1.0, 12.0, () -> DabyWSClientConfig.stormBackdropSize, v -> DabyWSClientConfig.stormBackdropSize = v);
        sl("Backdrop Pulse", "%.2fx", 0.0, 4.0, () -> DabyWSClientConfig.stormBackdropPulse, v -> DabyWSClientConfig.stormBackdropPulse = v);
        sl("Black Sheet Strength", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.stormBackdropBlackStrength, v -> DabyWSClientConfig.stormBackdropBlackStrength = v);
        sl("Phase-4 Sheet Strength", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.stormBackdropPhase4Strength, v -> DabyWSClientConfig.stormBackdropPhase4Strength = v);
        sl("Ember Sheet Strength", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.stormBackdropEmberStrength, v -> DabyWSClientConfig.stormBackdropEmberStrength = v);
        sl("Story Clouds Strength", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.storyModeCloudStrength, v -> DabyWSClientConfig.storyModeCloudStrength = v);
        sl("Precipitation Intensity", "%.2fx", 0.0, 2.0, () -> McsmExtrasConfig.precipitationIntensity, v -> McsmExtrasConfig.precipitationIntensity = v);

        // ---- CH 10 · Sound -----------------------------------------------------
        chapter("X", "Sound");
        sl("Ambience Volume", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.ambienceVolume, v -> DabyWSClientConfig.ambienceVolume = v);
        sl("Head Sounds Volume", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.headSoundsVolume, v -> DabyWSClientConfig.headSoundsVolume = v);
        sl("Beam Sounds Volume", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.beamSoundsVolume, v -> DabyWSClientConfig.beamSoundsVolume = v);
        sl("Beam Hum Volume", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.beamHumVolume, v -> DabyWSClientConfig.beamHumVolume = v);
        sl("Beam Hum Range", "%.0f", 8.0, 128.0, () -> DabyWSClientConfig.beamHumRange, v -> DabyWSClientConfig.beamHumRange = v);
        sl("Infected Mob Volume", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.infectedMobSoundVolume, v -> DabyWSClientConfig.infectedMobSoundVolume = v);
        sl("Storm Music Volume", "%.2fx", 0.0, 2.0, () -> DabyWSClientConfig.stormMusicVolume, v -> DabyWSClientConfig.stormMusicVolume = v);
        sl("Storm Music Range", "%.0f", 32.0, 512.0, () -> DabyWSClientConfig.stormMusicRange, v -> DabyWSClientConfig.stormMusicRange = v);
        sl("Music Cave Cutoff", "%.1f", 0.0, 15.0, () -> DabyWSClientConfig.stormMusicCaveCutoff, v -> DabyWSClientConfig.stormMusicCaveCutoff = v);

        // ---- CH 11 · Panel & story ----------------------------------------------
        chapter("XI", "Panel");
        sl("Glare Size", "%.2fx", 0.25, 3.05, () -> McsmExtrasConfig.glareSize, v -> McsmExtrasConfig.glareSize = v);
        sl("Smudge Scale", "%.2fx", 0.10, 2.00, () -> McsmExtrasConfig.smudgeScale, v -> McsmExtrasConfig.smudgeScale = v);
        sl("Phase 5.5 Threshold", "%.2f", 5.0, 6.0, () -> McsmExtrasConfig.phase55Threshold, v -> McsmExtrasConfig.phase55Threshold = v);
        sl("Phase 5.9 Pink Mult", "%.2fx", 0.2, 3.0, () -> McsmExtrasConfig.phase5_9PinkIntensity, v -> McsmExtrasConfig.phase5_9PinkIntensity = v);
        sl("Glare Anim Phase", "%.2f", 0.0, 1.0, () -> McsmExtrasConfig.glareAnimPhase, v -> McsmExtrasConfig.glareAnimPhase = v);
        sl("Glare Anim Intensity", "%.2fx", 0.0, 2.0, () -> McsmExtrasConfig.glareAnimIntensity, v -> McsmExtrasConfig.glareAnimIntensity = v);
        sl("Grab Interval", "%.1f s", 0.0, 30.0, () -> McsmExtrasConfig.grabIntervalSeconds, v -> McsmExtrasConfig.grabIntervalSeconds = v);
        sl("Beacon Cooldown", "%.0f s", 2.0, 120.0, () -> McsmExtrasConfig.beaconCooldownSeconds, v -> McsmExtrasConfig.beaconCooldownSeconds = v);

        // ---- CH 12 · Toggle bank (legacy rows, kept crash-free) ------------------
        chapter("XII", "Toggles");
        tg("Non-Euclidean Glare", () -> McsmExtrasConfig.glareNonEuclidean, v -> McsmExtrasConfig.glareNonEuclidean = v);
        tg("In-Mod Aurora", () -> McsmExtrasConfig.auroraEnabled, v -> McsmExtrasConfig.auroraEnabled = v);
        tg("Aurora Ribbons (4-Color)", () -> McsmExtrasConfig.auroraRibbons, v -> McsmExtrasConfig.auroraRibbons = v);
        tg("Snow Biome Blue Band", () -> McsmExtrasConfig.snowSkyBand, v -> McsmExtrasConfig.snowSkyBand = v);
        tg("Twinkling Multi Stars", () -> McsmExtrasConfig.twinklingStars, v -> McsmExtrasConfig.twinklingStars = v);
        tg("Night Comets / Streaks", () -> McsmExtrasConfig.comets, v -> McsmExtrasConfig.comets = v);
        tg("Magical Sparkles (W/P/P)", () -> McsmExtrasConfig.coloredSparkles, v -> McsmExtrasConfig.coloredSparkles = v);
        tg("Biome Mist & Fog VFX", () -> McsmExtrasConfig.biomeAtmospherics, v -> McsmExtrasConfig.biomeAtmospherics = v);
        tg("Nether Crimson Fog+Sparks", () -> McsmExtrasConfig.netherRedFog, v -> McsmExtrasConfig.netherRedFog = v);
        tg("Underwater God Rays & Haze", () -> McsmExtrasConfig.waterGodRays, v -> McsmExtrasConfig.waterGodRays = v);
        tg("End Sky Vortex & Rip", () -> McsmExtrasConfig.endSkyVortex, v -> McsmExtrasConfig.endSkyVortex = v);
        tg("Beacon Luminous Glow", () -> McsmExtrasConfig.beaconGlow, v -> McsmExtrasConfig.beaconGlow = v);
        tg("Nether & End Portal Lights", () -> McsmExtrasConfig.portalLights, v -> McsmExtrasConfig.portalLights = v);
        tg("Global Shadows & Contrast", () -> McsmExtrasConfig.globalShadows, v -> McsmExtrasConfig.globalShadows = v);
        tg("OG CEM Models", () -> McsmExtrasConfig.ogCemModels, v -> McsmExtrasConfig.ogCemModels = v);
        tg("Built-in Shader Pack", () -> McsmExtrasConfig.embeddedShaderPack, v -> McsmExtrasConfig.embeddedShaderPack = v);
        tg("Enhanced Wither Storm AI", () -> McsmExtrasConfig.witherStormEnhancedAi, v -> McsmExtrasConfig.witherStormEnhancedAi = v);
        tg("NPC Walk/Speak Animations", () -> McsmExtrasConfig.npcWalkAnimations, v -> McsmExtrasConfig.npcWalkAnimations = v);
        tg("Death Cinematic", () -> McsmExtrasConfig.deathCinematic, v -> McsmExtrasConfig.deathCinematic = v);
        tg("Supernova Rings", () -> McsmExtrasConfig.supernovaRings, v -> McsmExtrasConfig.supernovaRings = v);
        tg("Smoke Screen + Sparks", () -> McsmExtrasConfig.smokeScreen, v -> McsmExtrasConfig.smokeScreen = v);
        tg("Purple Sky (5.5+)", () -> McsmExtrasConfig.purpleSky, v -> McsmExtrasConfig.purpleSky = v);
        tg("Dust Waves", () -> McsmExtrasConfig.dustWaves, v -> McsmExtrasConfig.dustWaves = v);
        tg("Reality Tear", () -> McsmExtrasConfig.realityTear, v -> McsmExtrasConfig.realityTear = v);
        tg("Obliterate Flash", () -> McsmExtrasConfig.obliterateFlash, v -> McsmExtrasConfig.obliterateFlash = v);
        tg("Obliterate Kicks Players", () -> McsmExtrasConfig.obliterateKick, v -> McsmExtrasConfig.obliterateKick = v);
        tg("Tentacle Grab", () -> McsmExtrasConfig.enableTentacleGrab, v -> McsmExtrasConfig.enableTentacleGrab = v);
        tg("Lit Beacon Relay", () -> McsmExtrasConfig.enableBeaconStorm, v -> McsmExtrasConfig.enableBeaconStorm = v);
        tg("Storm Beacon Block", () -> McsmExtrasConfig.enableBeaconBlock, v -> McsmExtrasConfig.enableBeaconBlock = v);
        tg("Rise Ground FX", () -> McsmExtrasConfig.enableRiseFx, v -> McsmExtrasConfig.enableRiseFx = v);
        tg("Counterclockwise Spiral", () -> McsmExtrasConfig.spiralCounterClockwise, v -> McsmExtrasConfig.spiralCounterClockwise = v);
        tg("Force MCSM Look", () -> McsmExtrasConfig.forceMcsmLook, v -> McsmExtrasConfig.forceMcsmLook = v);
        tg("Force MCSM World", () -> McsmExtrasConfig.forceMcsmWorld, v -> McsmExtrasConfig.forceMcsmWorld = v);
        tg("Command Block Wire", () -> McsmExtrasConfig.commandWire, v -> McsmExtrasConfig.commandWire = v);
        tg("MCSM Instructions", () -> McsmExtrasConfig.mcsmInstructions, v -> McsmExtrasConfig.mcsmInstructions = v);
        tg("Shader Pack Gate", () -> McsmExtrasConfig.shaderPackGate, v -> McsmExtrasConfig.shaderPackGate = v);

        this.contentBottom = 36 + this.rowY * 22 + 6;
        int panelW = Math.max(180, Math.min(420, this.width - 58));

        Button reapply = Button.builder(Component.literal("Re-apply MCSM Look now"), b -> {
            McsmGate.clearMemory();
            McsmGate.reset();
        }).bounds(left, this.contentBottom + 6, panelW, 20).build();
        this.addWidget(reapply);
        this.chrome.add(reapply);
        this.baseY.put(reapply, this.contentBottom + 6);
        this.contentBottom += 30;

        Button done = Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build();
        this.addWidget(done);
        this.chrome.add(done);

        applyScrollLayout();
    }

    // chapter + row bookkeeping ------------------------------------------------

    private int rowY = 0;

    private void chapter(String code, String title) {
        Chapter c = new Chapter(code, title);
        c.topPx = this.rowY * 22;
        this.chapters.add(c);
        this.rowY += 1;   // header row
    }

    private void sl(String label, String fmt, double lo, double hi, DoubleSupplier get, Consumer<Double> set) {
        int y = 36 + this.rowY * 22;
        DiamondSlider s = new DiamondSlider(34, y, Math.max(180, Math.min(420, this.width - 58)), label, fmt, lo, hi, get, set);
        this.addWidget(s);
        this.chrome.add(s);
        this.baseY.put(s, y);
        this.rowY += 1;
    }

    private void tg(String label, BooleanSupplier get, Consumer<Boolean> set) {
        int y = 36 + this.rowY * 22;
        Button b = Button.builder(toggleLabel(label, get.getAsBoolean()), btn -> {
            set.accept(!get.getAsBoolean());
            McsmExtrasConfig.save();
            McsmGate.reset();
            btn.setMessage(toggleLabel(label, get.getAsBoolean()));
        }).bounds(34, y, Math.max(180, Math.min(420, this.width - 58)), 20).build();
        this.addWidget(b);
        this.chrome.add(b);
        this.baseY.put(b, y);
        this.rowY += 1;
    }

    private static Component toggleLabel(String label, boolean on) {
        return Component.literal(label + ": " + (on ? "\u00a7aON" : "\u00a7cOFF"));
    }

    // ---------------------------------------------------------------- rail

    private int railX() {
        return 6;
    }

    private int railRowH() {
        return Math.max(14, Math.min(26, (this.height - 80) / Math.max(1, this.chapters.size())));
    }

    private int activeChapter() {
        int pos = this.scrollPx;
        int idx = 0;
        for (int i = 0; i < this.chapters.size(); i++) {
            if (this.chapters.get(i).topPx <= pos + 8) {
                idx = i;
            }
        }
        return idx;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0 && mouseX >= railX() - 2 && mouseX <= railX() + 22 && mouseY >= 40 && mouseY <= 40 + this.chapters.size() * railRowH()) {
            int idx = (int) ((mouseY - 40) / railRowH());
            if (idx >= 0 && idx < this.chapters.size()) {
                this.scrollPx = Math.max(0, this.chapters.get(idx).topPx - 4);
                applyScrollLayout();
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }

    // ---------------------------------------------------------------- layout

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

    // ---------------------------------------------------------------- render

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        g.fillGradient(0, 0, this.width, this.height, 0xF0120A1E, 0xF005030A);
        g.fillGradient(0, 0, this.width, 3, 0xFF6A8FF7, 0xFF3F255A);
        g.fill(0, this.height - 3, this.width, this.height, 0xFF3F255A);

        // ---- chapter rail: vertical scrolling tracking sidebar -------------
        int rh = railRowH();
        int active = activeChapter();
        g.fill(railX() - 2, 36, railX() + 22, 40 + this.chapters.size() * rh + 4, 0xAA0A0E14);
        for (int i = 0; i < this.chapters.size(); i++) {
            int cy = 40 + i * rh + rh / 2;
            boolean on = i == active;
            drawDiamond(g, railX() + 8, cy, on ? 5 : 3, on ? 0xFF7DF0FF : 0xFF3A4356);
            if (on) {
                drawDiamond(g, railX() + 8, cy, 2, 0xFF0A0E14);
                g.text(font, "\u00a7b" + this.chapters.get(i).code + " \u00a77" + this.chapters.get(i).title, railX() + 18, cy - 4, 0xFFEAF2FF, false);
            }
        }
        // scroll tracker bead riding the rail
        int max = Math.max(1, this.contentBottom - (this.height - 36));
        int bead = 40 + (int) ((this.chapters.size() * rh - 6L) * Math.min(1.0, this.scrollPx / (double) max));
        g.fill(railX() - 4, bead, railX() - 2, bead + 6, 0xFFEAF2FF);

        g.text(font, "\u00a7bStory Mode Controls \u00a78\u00b7 \u00a77" + McsmExtrasConfig.BUILD_VERSION, 34, 12, 0xFFEAF2FF, false);
        g.text(font, "\u00a78Chapter rail jumps sections. Scroll wheel moves panel. Shift+C opens anywhere.", 34, 24, 0xFFA0A0A0, false);
        if (this.contentBottom > this.height - 36) {
            g.centeredText(font, "scroll " + this.scrollPx + "/" + max, this.width - 62, 12, 0xA0A0A0);
        }

        // chapter headers ride between the widgets
        for (int i = 0; i < this.chapters.size(); i++) {
            int y = 36 + this.chapters.get(i).topPx - this.scrollPx;
            if (y >= 28 && y <= this.height - 42) {
                g.fill(34, y + 18, this.width - 24, y + 19, 0xFF3F255A);
                g.text(font, "\u00a7d" + this.chapters.get(i).code + " \u00a7f" + this.chapters.get(i).title, 34, y + 4, 0xFFEAF2FF, false);
            }
        }

        for (AbstractWidget widget : this.chrome) {
            widget.extractRenderState(g, mouseX, mouseY, partialTick);
        }
    }

    /** Diamond dot: stacked 1px fills widening then narrowing -- no rotated matrices needed. */
    private static void drawDiamond(GuiGraphicsExtractor g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int w = r - Math.abs(dy);
            g.fill(cx - w, cy + dy, cx + w + 1, cy + dy + 1, color);
        }
    }

    @Override
    public void onClose() {
        McsmExtrasConfig.save();
        Minecraft.getInstance().setScreenAndShow(this.parent);
    }

    // ---------------------------------------------------------------- slider

    /**
     * Diamond-dot configuration slider: full custom paint (track + diamond
     * knob + label), drag/keyboard behaviour inherited from AbstractSliderButton.
     */
    private static final class DiamondSlider extends AbstractSliderButton {
        private final String label;
        private final String fmt;
        private final double lo;
        private final double hi;
        private final DoubleSupplier get;
        private final Consumer<Double> set;

        DiamondSlider(int x, int y, int w, String label, String fmt, double lo, double hi,
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

        /** The widget-level paint hook (AbstractWidget.extractRenderState is final). */
        @Override
        public void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
            if (!this.visible) {
                return;
            }
            Font font = Minecraft.getInstance().font;
            int w = this.getWidth();
            int trackY = this.getY() + 13;
            g.fill(this.getX(), trackY, this.getX() + w, trackY + 2, 0xFF20242E);
            g.fill(this.getX(), trackY, this.getX() + (int) (w * this.value), trackY + 2, 0xFF3F255A);
            int kx = this.getX() + 5 + (int) ((w - 10) * this.value);
            boolean hov = this.isHovered();
            drawDiamond(g, kx, trackY + 1, 5, hov ? 0xFFB7F7FF : 0xFF7DF0FF);
            drawDiamond(g, kx, trackY + 1, 2, 0xFF0A0E14);
            g.text(font, this.label, this.getX() + 2, this.getY() + 1, 0xFFEAF2FF, false);
            g.text(font, "\u00a7e" + String.format(this.fmt, this.actual()), this.getX() + w - 44, this.getY() + 1, 0xFFEAF2FF, false);
        }
    }
}
