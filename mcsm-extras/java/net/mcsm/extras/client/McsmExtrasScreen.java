package net.mcsm.extras.client;

import net.mcsm.extras.McsmExtrasConfig;
import net.mcsm.extras.McsmGate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Devouring Storms — STORY MODE CONSOLE (Version 10000.0.0-PRE-RELEASE-ALPHA-1-DEVOURING-STORMS-338).
 *
 * BUILD #372 — TOTAL INTERFACE OVERHAUL (Telltale / episodic aesthetic).
 * The generic blocky vanilla Button / AbstractSliderButton widgets are gone:
 * everything is drawn as clean, minimal, dark translucent episodic panel
 * boxes.
 *
 * Layout:
 *   - top bar      : console title + the locked build string + close box
 *   - left sidebar : scrolling vertical chapter navigation (I, II, III ...) —
 *                    a data-driven registry (buildCategories) that scales to
 *                    the 200+ future configuration tabs: add a Category, it
 *                    appears in the sidebar with its own scroll
 *   - content      : one translucent card per control; boolean rows get a
 *                    sliding pill, value rows get a GEOMETRIC DOT TRACK
 *                    (click / drag to scrub in real time) plus a value chip
 *                    that opens a crisp text input field (EditBox) for exact
 *                    entry
 *   - footer       : Done + control hints
 *
 * No vanilla control widgets are used at all — input is handled directly
 * (mouse click / drag / scroll), and the only widget instantiated is the
 * transient EditBox while a value is being typed.
 *
 * BUILD #374 SYNC-FORWARD: the full Build #372 premium structure is intact
 * and authoritative — the scrolling chapter rail (data-driven Category
 * registry, chapters I-VIII), the dark translucent episodic cards, the
 * sliding pills, the geometric diamond-dot scrub tracks and the text-input
 * value chips. Chapters VII (SKY & BUILT-IN PACKS) and VIII (CINEMATICS)
 * ride the same rail/framework; the base config screen's entry rows
 * (McsmGuiExtrasRows) open this exact screen.
 */
public final class McsmExtrasScreen extends Screen {

    // ---- palette (Telltale: warm amber accent on deep charcoal) ----------
    private static final int BG_TOP      = 0xFF0D1016;
    private static final int BG_BOTTOM   = 0xFF07080C;
    private static final int CARD        = 0xD812151C;
    private static final int CARD_HOVER  = 0xE0171B24;
    private static final int CARD_EDGE   = 0xFF232833;
    private static final int CARD_EDGE_H = 0xFF3A4152;
    private static final int SIDE_BG     = 0xE60A0C11;
    private static final int ACCENT      = 0xFFD9A441;
    private static final int ACCENT_SOFT = 0x55D9A441;
    private static final int TEXT_HI     = 0xFFE8E2D0;
    private static final int TEXT_MID    = 0xFFB8BDC9;
    private static final int TEXT_DIM    = 0xFF6B7280;
    private static final int VALUE_GOLD  = 0xFFE8C07A;
    private static final int OFF_GRAY    = 0xFF555B66;
    private static final int TRACK_OFF   = 0xFF1A1E26;

    private static final int TOP_H   = 34;
    private static final int FOOT_H  = 26;
    private static final int SIDE_W  = 190;
    private static final int ROW_H   = 42;
    private static final int DOTS    = 7;
    private static final int DOT_GAP = 14;

    private final Screen parent;
    private final List<Category> categories = new ArrayList<>();
    private int currentCat = 0;
    private int sidebarScroll = 0;
    private int contentScroll = 0;
    private int hoverRow = -1;      // index within the current category
    private int hoverSide = -1;
    private int scrubRow = -1;      // dot track being dragged
    private Row editing = null;     // value row with an open EditBox
    private EditBox editBox = null;
    private int editBoxX = 0;
    private int editBoxY = 0;
    private String editText = "";
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    // ---- data model -------------------------------------------------------

    private static final class Row {
        final String label;
        final int kind;              // 0 = toggle, 1 = value, 2 = action
        final BooleanSupplier bGet;
        final Consumer<Boolean> bSet;
        final DoubleSupplier dGet;
        final Consumer<Double> dSet;
        final double lo;
        final double hi;
        final Runnable action;

        Row(String label, BooleanSupplier g, Consumer<Boolean> s) {
            this.label = label; this.kind = 0; this.bGet = g; this.bSet = s;
            this.dGet = null; this.dSet = null; this.lo = 0; this.hi = 1; this.action = null;
        }

        Row(String label, DoubleSupplier g, Consumer<Double> s, double lo, double hi) {
            this.label = label; this.kind = 1; this.bGet = null; this.bSet = null;
            this.dGet = g; this.dSet = s; this.lo = lo; this.hi = hi; this.action = null;
        }

        Row(String label, Runnable action) {
            this.label = label; this.kind = 2; this.bGet = null; this.bSet = null;
            this.dGet = null; this.dSet = null; this.lo = 0; this.hi = 1; this.action = action;
        }
    }

    private static final class Category {
        final String chapter;
        final String title;
        final String blurb;
        final List<Row> rows = new ArrayList<>();

        Category(String chapter, String title, String blurb) {
            this.chapter = chapter; this.title = title; this.blurb = blurb;
        }

        Category bool(String label, BooleanSupplier g, Consumer<Boolean> s) {
            rows.add(new Row(label, g, s));
            return this;
        }

        Category val(String label, DoubleSupplier g, Consumer<Double> s, double lo, double hi) {
            rows.add(new Row(label, g, s, lo, hi));
            return this;
        }

        Category act(String label, Runnable a) {
            rows.add(new Row(label, a));
            return this;
        }
    }

    /**
     * THE TAB REGISTRY — the structural framework for the 200+ future
     * configuration tabs: every Category added here automatically gets a
     * sidebar chapter entry, its own scroll position, and full control
     * rendering. Add a Category below and the sidebar grows.
     */
    private void buildCategories() {
        categories.clear();

        Category c1 = new Category("I", "VISUALS & SKY", "Atmosphere, celestial body and the storm's skin.");
        c1.bool("OG Traced Shading Body (Ph 4-5.5)", () -> McsmExtrasConfig.tracedShadingBody, v -> McsmExtrasConfig.tracedShadingBody = v)
          .bool("Blockbench Custom Mesh (Preset)", () -> McsmExtrasConfig.customMeshModel, v -> McsmExtrasConfig.customMeshModel = v)
          .bool("OG 3D Sun Slab (SAGE MCSM)", () -> McsmExtrasConfig.ogSunGlow, v -> {
              McsmExtrasConfig.ogSunGlow = v;
              McsmGate.clientBool("sunGlow", v);
          })
          .val("OG Sun Slab Strength", () -> McsmExtrasConfig.ogSunGlowStrength, v -> {
              McsmExtrasConfig.ogSunGlowStrength = v;
              if (McsmExtrasConfig.ogSunGlow) McsmGate.clientNum("sunGlowStrength", v);
          }, 0.0, 3.0)
          .bool("Cinematic Story-Mode Menu", () -> McsmExtrasConfig.storyMenuBackdrop, v -> McsmExtrasConfig.storyMenuBackdrop = v)
          .bool("Multi-Layer Sky Blending", () -> McsmGate.clientBoolGet("cloudDeckLayer", true), v -> McsmGate.clientBool("cloudDeckLayer", v))
          .val("Phase 5.5 Threshold", () -> McsmExtrasConfig.phase55Threshold, v -> McsmExtrasConfig.phase55Threshold = v, 5.0, 6.0)
          .val("Phase 5.9 Pink Intensity", () -> McsmExtrasConfig.phase5_9PinkIntensity, v -> McsmExtrasConfig.phase5_9PinkIntensity = v, 0.2, 3.0)
          .val("Night Navy Opacity", () -> McsmExtrasConfig.nightSkyOpacity, v -> McsmExtrasConfig.nightSkyOpacity = v, 0.0, 1.0)
          .val("Cloud Alpha", () -> McsmExtrasConfig.cloudAlpha, v -> McsmExtrasConfig.cloudAlpha = v, 0.0, 1.0)
          .val("Cloud Speed", () -> McsmExtrasConfig.cloudSpeed, v -> McsmExtrasConfig.cloudSpeed = v, 0.0, 3.0)
          .bool("In-Mod Aurora", () -> McsmExtrasConfig.auroraEnabled, v -> McsmExtrasConfig.auroraEnabled = v)
          .bool("4-Color Aurora Ribbons", () -> McsmExtrasConfig.auroraRibbons, v -> McsmExtrasConfig.auroraRibbons = v)
          .bool("Snow Biome Blue Band", () -> McsmExtrasConfig.snowSkyBand, v -> McsmExtrasConfig.snowSkyBand = v)
          .bool("Twinkling Multi Stars", () -> McsmExtrasConfig.twinklingStars, v -> McsmExtrasConfig.twinklingStars = v)
          .bool("Night Comets / Streaks", () -> McsmExtrasConfig.comets, v -> McsmExtrasConfig.comets = v)
          .val("Glare Size Multiplier", () -> McsmExtrasConfig.glareSize, v -> McsmExtrasConfig.glareSize = v, 0.25, 3.05)
          .val("Smudge Scale", () -> McsmExtrasConfig.smudgeScale, v -> McsmExtrasConfig.smudgeScale = v, 0.1, 2.0)
          .bool("Non-Euclidean Glare", () -> McsmExtrasConfig.glareNonEuclidean, v -> McsmExtrasConfig.glareNonEuclidean = v)
          .bool("Magical Sparkles (W/P/P)", () -> McsmExtrasConfig.coloredSparkles, v -> McsmExtrasConfig.coloredSparkles = v)
          .bool("Biome Mist & Fog VFX", () -> McsmExtrasConfig.biomeAtmospherics, v -> McsmExtrasConfig.biomeAtmospherics = v)
          .bool("Nether Crimson Fog+Sparks", () -> McsmExtrasConfig.netherRedFog, v -> McsmExtrasConfig.netherRedFog = v)
          .bool("Underwater God Rays", () -> McsmExtrasConfig.waterGodRays, v -> McsmExtrasConfig.waterGodRays = v)
          .bool("End Sky Vortex & Rip", () -> McsmExtrasConfig.endSkyVortex, v -> McsmExtrasConfig.endSkyVortex = v)
          .bool("Beacon Luminous Glow", () -> McsmExtrasConfig.beaconGlow, v -> McsmExtrasConfig.beaconGlow = v)
          .bool("Nether & End Portal Lights", () -> McsmExtrasConfig.portalLights, v -> McsmExtrasConfig.portalLights = v)
          .bool("Global Shadows & Contrast", () -> McsmExtrasConfig.globalShadows, v -> McsmExtrasConfig.globalShadows = v);
        categories.add(c1);

        Category c2 = new Category("II", "STORM PALETTE", "Eyes, teeth, beams and the body tint.");
        c2.bool("Use Custom Colors", () -> McsmExtrasConfig.useCustomColors, v -> McsmExtrasConfig.useCustomColors = v)
          .val("Eye Glow Red", () -> McsmExtrasConfig.customEyeR, v -> McsmExtrasConfig.customEyeR = v, 0.0, 1.0)
          .val("Eye Glow Green", () -> McsmExtrasConfig.customEyeG, v -> McsmExtrasConfig.customEyeG = v, 0.0, 1.0)
          .val("Eye Glow Blue", () -> McsmExtrasConfig.customEyeB, v -> McsmExtrasConfig.customEyeB = v, 0.0, 1.0)
          .val("Teeth Glow Red", () -> McsmExtrasConfig.customTeethR, v -> McsmExtrasConfig.customTeethR = v, 0.0, 1.0)
          .val("Teeth Glow Green", () -> McsmExtrasConfig.customTeethG, v -> McsmExtrasConfig.customTeethG = v, 0.0, 1.0)
          .val("Teeth Glow Blue", () -> McsmExtrasConfig.customTeethB, v -> McsmExtrasConfig.customTeethB = v, 0.0, 1.0)
          .val("Beam Color Red", () -> McsmExtrasConfig.customBeamR, v -> McsmExtrasConfig.customBeamR = v, 0.0, 1.0)
          .val("Beam Color Green", () -> McsmExtrasConfig.customBeamG, v -> McsmExtrasConfig.customBeamG = v, 0.0, 1.0)
          .val("Beam Color Blue", () -> McsmExtrasConfig.customBeamB, v -> McsmExtrasConfig.customBeamB = v, 0.0, 1.0)
          .val("Skin Tint Red", () -> McsmExtrasConfig.customSkinTintR, v -> McsmExtrasConfig.customSkinTintR = v, 0.0, 1.0)
          .val("Skin Tint Green", () -> McsmExtrasConfig.customSkinTintG, v -> McsmExtrasConfig.customSkinTintG = v, 0.0, 1.0)
          .val("Skin Tint Blue", () -> McsmExtrasConfig.customSkinTintB, v -> McsmExtrasConfig.customSkinTintB = v, 0.0, 1.0)
          .bool("Lock Canonical Texture Map", () -> McsmExtrasConfig.lockCanonicalTexture, v -> McsmExtrasConfig.lockCanonicalTexture = v)
          .act("Open Storm Texture Painter (Make Your Own Texture)", () -> {
              net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
              if (mc != null) {
                  mc.setScreenAndShow(new net.mcsm.extras.client.McsmTexturePainterScreen(this));
              }
          });
        categories.add(c2);

        Category c3 = new Category("III", "DEBRIS & PHYSICS", "Native block debris, rescaling and ground behaviour.");
        c3.bool("Force Native Block Debris", () -> McsmExtrasConfig.forceNativeBlockDebris, v -> McsmExtrasConfig.forceNativeBlockDebris = v)
          .val("Debris Scale Multiplier", () -> McsmExtrasConfig.debrisScaleMultiplier, v -> McsmExtrasConfig.debrisScaleMultiplier = v, 1.0, 5.0)
          .val("Ambient Purple Coil Scale", () -> McsmExtrasConfig.purpleCoilScale, v -> McsmExtrasConfig.purpleCoilScale = v, 0.1, 1.0)
          .val("Debris Movement Direction", () -> (double) McsmExtrasConfig.debrisMovementDirection, v -> McsmExtrasConfig.debrisMovementDirection = (int) Math.round(v), 0.0, 5.0)
          .bool("Dust Waves on Sweep", () -> McsmExtrasConfig.dustWaves, v -> McsmExtrasConfig.dustWaves = v)
          .bool("Smoke Screen + Sparks", () -> McsmExtrasConfig.smokeScreen, v -> McsmExtrasConfig.smokeScreen = v)
          .bool("Rise Ground FX", () -> McsmExtrasConfig.enableRiseFx, v -> McsmExtrasConfig.enableRiseFx = v)
          .bool("Counterclockwise Spiral", () -> McsmExtrasConfig.spiralCounterClockwise, v -> McsmExtrasConfig.spiralCounterClockwise = v)
          .bool("Command Block Wire", () -> McsmExtrasConfig.commandWire, v -> McsmExtrasConfig.commandWire = v);
        categories.add(c3);

        Category c4 = new Category("IV", "MOTION", "The storm's live animation controller.");
        c4.val("Idle Animation Speed", () -> McsmExtrasConfig.animIdleSpeed, v -> McsmExtrasConfig.animIdleSpeed = v, 0.1, 3.0)
          .val("Roar Intensity", () -> McsmExtrasConfig.animRoarIntensity, v -> McsmExtrasConfig.animRoarIntensity = v, 0.1, 3.0)
          .val("Jaw Slack Factor", () -> McsmExtrasConfig.animJawSlack, v -> McsmExtrasConfig.animJawSlack = v, 0.1, 3.0)
          .val("Tentacle Slam Force", () -> McsmExtrasConfig.animTentacleSlamForce, v -> McsmExtrasConfig.animTentacleSlamForce = v, 0.1, 3.0)
          .val("Head Sway Gain", () -> McsmExtrasConfig.animHeadSwayGain, v -> McsmExtrasConfig.animHeadSwayGain = v, 0.1, 3.0)
          .bool("Body Sway & Tilt", () -> McsmExtrasConfig.stormBodySway, v -> McsmExtrasConfig.stormBodySway = v)
          .bool("NPC Walk/Speak Poses", () -> McsmExtrasConfig.npcWalkAnimations, v -> McsmExtrasConfig.npcWalkAnimations = v);
        categories.add(c4);

        Category c5 = new Category("V", "NIGHTGLOW & DEATH", "Silhouette halos and the end-game sequence.");
        c5.bool("Outline Whole Body (Tail to Top)", () -> McsmExtrasConfig.nightglowBodyOutline, v -> McsmExtrasConfig.nightglowBodyOutline = v)
          .bool("Bluish Glow Base", () -> McsmExtrasConfig.nightglowBluishGlow, v -> McsmExtrasConfig.nightglowBluishGlow = v)
          .bool("Blue Storm Halo (Phase 4+, Chassis-Welded)", () -> McsmExtrasConfig.stormHaloEnabled, v -> McsmExtrasConfig.stormHaloEnabled = v)
          .bool("Purple Glow (Phases 5.1-5.9)", () -> McsmExtrasConfig.nightglowPurpleGlow55, v -> McsmExtrasConfig.nightglowPurpleGlow55 = v)
          .bool("Thick Black Core Glow", () -> McsmExtrasConfig.nightglowThickBlackGlow, v -> McsmExtrasConfig.nightglowThickBlackGlow = v)
          .bool("Death Cinematic", () -> McsmExtrasConfig.deathCinematic, v -> McsmExtrasConfig.deathCinematic = v)
          .bool("Supernova Rings", () -> McsmExtrasConfig.supernovaRings, v -> McsmExtrasConfig.supernovaRings = v)
          .bool("Phase 6+ Giant End Flashes", () -> McsmExtrasConfig.endFlashesPhase6, v -> McsmExtrasConfig.endFlashesPhase6 = v)
          .bool("Transparent Horizon Wings", () -> McsmExtrasConfig.transparentHorizonWings, v -> McsmExtrasConfig.transparentHorizonWings = v)
          .bool("Post-Death Reality Tear", () -> McsmExtrasConfig.realityTear, v -> McsmExtrasConfig.realityTear = v);
        categories.add(c5);

        Category c6 = new Category("VI", "GAMEPLAY & SYSTEMS", "Storm AI, beacon relay, gates and the sun shadow.");
        c6.bool("Enhanced Wither Storm AI (Hunts You)", () -> McsmExtrasConfig.witherStormEnhancedAi, v -> {
            McsmExtrasConfig.witherStormEnhancedAi = v;
            McsmExtrasConfig.save();
        })
          .bool("3D Storm Model in Config Menu", () -> McsmExtrasConfig.giantPreviewEnabled, v -> {
              McsmExtrasConfig.giantPreviewEnabled = v;
              McsmExtrasConfig.save();
          })
          .act("Player Model Versions — how to switch (F8 or 1/2/3)", () -> {
              net.mcsm.extras.client.McsmClientChat.say("[ds] Player models: press  F8  in-game to cycle  vanilla -> Telltale Hero -> Scout -> Storm Guardian -> vanilla");
              net.mcsm.extras.client.McsmClientChat.say("[ds] Or manually:  /scoreboard objectives add mcsmplyr dummy  then  /scoreboard players set @s mcsmplyr 1 | 2 | 3 | 0");
              net.mcsm.extras.client.McsmClientChat.say("[ds]  1 = Telltale Hero (big head)  2 = Scout (slim)  3 = Storm Guardian (brawny)  0 = vanilla. (Needs the EMF/CEM mod installed.)");
          })
          .bool("Tentacle Grab", () -> McsmExtrasConfig.enableTentacleGrab, v -> McsmExtrasConfig.enableTentacleGrab = v)
          .val("Grab Interval", () -> McsmExtrasConfig.grabIntervalSeconds, v -> McsmExtrasConfig.grabIntervalSeconds = v, 0.0, 30.0)
          .bool("Lit Beacon Relay", () -> McsmExtrasConfig.enableBeaconStorm, v -> McsmExtrasConfig.enableBeaconStorm = v)
          .val("Beacon Cooldown", () -> McsmExtrasConfig.beaconCooldownSeconds, v -> McsmExtrasConfig.beaconCooldownSeconds = v, 2.0, 120.0)
          .bool("Storm Beacon Block", () -> McsmExtrasConfig.enableBeaconBlock, v -> McsmExtrasConfig.enableBeaconBlock = v)
          .bool("Force MCSM Look", () -> McsmExtrasConfig.forceMcsmLook, v -> McsmExtrasConfig.forceMcsmLook = v)
          .bool("Force MCSM World", () -> McsmExtrasConfig.forceMcsmWorld, v -> McsmExtrasConfig.forceMcsmWorld = v)
          .bool("Silver Border Lines (Image 3)", () -> McsmExtrasConfig.uiBorderLines, v -> McsmExtrasConfig.uiBorderLines = v)
          .bool("MCSM Instructions", () -> McsmExtrasConfig.mcsmInstructions, v -> McsmExtrasConfig.mcsmInstructions = v)
          .bool("Cast A Sun Shadow", () -> McsmGate.clientBoolGet("stormShadow", true), v -> McsmGate.clientBool("stormShadow", v))
          .val("Shadow Darkness", () -> McsmGate.clientNumGet("stormShadowStrength", 1.0), v -> McsmGate.clientNum("stormShadowStrength", v), 0.0, 2.0)
          .bool("Soft Shadow Edge", () -> McsmGate.clientBoolGet("stormShadowSoftEdge", true), v -> McsmGate.clientBool("stormShadowSoftEdge", v))
          .val("Shadow Colour Red", () -> McsmGate.clientNumGet("stormShadowR", 0.0), v -> McsmGate.clientNum("stormShadowR", v), 0.0, 1.0)
          .val("Shadow Colour Green", () -> McsmGate.clientNumGet("stormShadowG", 0.0), v -> McsmGate.clientNum("stormShadowG", v), 0.0, 1.0)
          .val("Shadow Colour Blue", () -> McsmGate.clientNumGet("stormShadowB", 0.0), v -> McsmGate.clientNum("stormShadowB", v), 0.0, 1.0)
          .act("Re-apply MCSM Look now", () -> {
              McsmExtrasConfig.save();
              McsmGate.clearMemory();
              McsmGate.reset();
          });
        categories.add(c6);

        // Build #374 -- the new options: the real cube skybox and the
        // built-in pack summoning (resource + shader packs).
        Category c7 = new Category("VII", "SKY & BUILT-IN PACKS", "The real Story Mode skybox and the summoned packs.");
        c7.bool("MCSM Skybox (Real Cube, Not Dome)", () -> McsmExtrasConfig.skyboxEnabled, v -> {
            McsmExtrasConfig.skyboxEnabled = v;
            McsmExtrasConfig.save();
        })
          .val("Skybox Fade (sec)", () -> McsmExtrasConfig.skyboxFadeSeconds, v -> {
              McsmExtrasConfig.skyboxFadeSeconds = v;
              McsmExtrasConfig.save();
          }, 0.5, 20.0)
          .val("Skybox Size", () -> McsmExtrasConfig.skyboxSize, v -> {
              McsmExtrasConfig.skyboxSize = v;
              McsmExtrasConfig.save();
          }, 0.5, 1.5)
          .bool("Telltale Glare Backdrops (3 Real Images)", () -> McsmExtrasConfig.glareBackdrop, v -> {
              McsmExtrasConfig.glareBackdrop = v;
              McsmExtrasConfig.save();
          })
          .val("Glare Backdrop Size", () -> McsmExtrasConfig.glareBackdropSize, v -> {
              McsmExtrasConfig.glareBackdropSize = v;
              McsmExtrasConfig.save();
          }, 0.3, 2.5)
          .val("Glare Backdrop Strength", () -> McsmExtrasConfig.glareBackdropStrength, v -> {
              McsmExtrasConfig.glareBackdropStrength = v;
              McsmExtrasConfig.save();
          }, 0.0, 2.0)
          .bool("Embedded Shader Pack (Auto-Install)", () -> McsmExtrasConfig.embeddedShaderPack, v -> {
              McsmExtrasConfig.embeddedShaderPack = v;
              McsmExtrasConfig.save();
          })
          .act("Re-summon packs + auto-select shader now", () -> net.mcsm.extras.McsmBuiltinPack.resummon());
        categories.add(c7);

        // Build #374 -- cinematic boot + epic depth animation controls.
        Category c8 = new Category("VIII", "CINEMATICS", "The boot cutscene, the command block burst and the world cracks.");
        c8.bool("Cinematic Boot (Pre-Game Cutscene + Command Block Burst)", () -> McsmExtrasConfig.cinematicBootEnabled, v -> {
            McsmExtrasConfig.cinematicBootEnabled = v;
            McsmExtrasConfig.save();
        })
          .bool("World Crack Intro (World Loads Cracking Apart)", () -> McsmExtrasConfig.worldCrackIntro, v -> {
            McsmExtrasConfig.worldCrackIntro = v;
            McsmExtrasConfig.save();
        });
        categories.add(c8);
    }

    public McsmExtrasScreen(Screen parent) {
        super(Component.literal("Devouring Storms Story Mode Console"));
        this.parent = parent;
    }

    // ---- layout constants -------------------------------------------------

    private int contentX() { return 12 + SIDE_W; }
    private int contentW() { return this.width - contentX() - 12; }
    private int contentTop() { return TOP_H + 46; }
    private int contentBottom() { return this.height - FOOT_H - 8; }
    private int sideTop() { return TOP_H + 8; }
    private int sideBottom() { return this.height - FOOT_H - 8; }

    // ---- init -------------------------------------------------------------

    @Override
    protected void init() {
        this.clearWidgets();
        McsmExtrasConfig.load();
        if (categories.isEmpty()) {
            buildCategories();
        }
        if (currentCat >= categories.size()) {
            currentCat = 0;
        }
        // The ONLY widget ever created: the transient text input while editing.
        if (editing != null) {
            Row r = editing;
            int x = contentX() + contentW() - 128;
            int y = rowY(categories.get(currentCat).rows.indexOf(r)) + 11;
            editBoxX = x;
            editBoxY = y;
            editBox = new EditBox(this.font, x, y, 112, 20, Component.literal("value"));
            editBox.setMaxLength(12);
            editBox.setValue(editText);
            editBox.setHint(Component.literal("enter value"));
            editBox.setResponder(text -> this.editText = text);
            editBox.setCanLoseFocus(true);
            editBox.setFocused(true);
            this.addWidget(editBox);
        }
    }

    private int rowY(int rowIdx) {
        return contentTop() + rowIdx * ROW_H - contentScroll;
    }

    // ---- editing ----------------------------------------------------------

    private void startEdit(Row r) {
        this.editing = r;
        this.editText = String.format("%.3f", r.dGet.getAsDouble());
        this.init();
    }

    private void commitEdit() {
        if (editing == null) {
            return;
        }
        Row r = editing;
        editing = null;
        try {
            double v = Double.parseDouble(editText.trim());
            if (v < r.lo) v = r.lo;
            if (v > r.hi) v = r.hi;
            r.dSet.accept(v);
            McsmExtrasConfig.save();
        } catch (Throwable ignored) {
            // unparsable text: keep the previous value
        }
        this.init();
    }

    // ---- input ------------------------------------------------------------

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        int x = (int) event.x();
        int y = (int) event.y();
        if (event.button() != 0) {
            return super.mouseClicked(event, doubled);
        }
        // open text input takes priority: a click inside it is for the box
        if (this.editBox != null && x >= editBoxX && x < editBoxX + 112
                && y >= editBoxY && y < editBoxY + 20) {
            return super.mouseClicked(event, doubled);
        }
        if (this.editing != null) {
            commitEdit();
        }
        // close box
        if (x >= this.width - 24 && x <= this.width - 6 && y >= 8 && y <= 26) {
            this.onClose();
            return true;
        }
        // done
        if (x >= 14 && x <= 74 && y >= this.height - FOOT_H + 3 && y <= this.height - FOOT_H + 21) {
            this.onClose();
            return true;
        }
        // sidebar
        if (x < SIDE_W - 6) {
            int i = (y - (sideTop() + 12) + sidebarScroll) / 26;
            if (i >= 0 && i < categories.size()) {
                currentCat = i;
                contentScroll = 0;
                hoverRow = -1;
                this.init();
                return true;
            }
        }
        // content rows
        Category cat = categories.get(currentCat);
        int ri = (y - contentTop() + contentScroll) / ROW_H;
        if (y >= contentTop() && y <= contentBottom() && ri >= 0 && ri < cat.rows.size()) {
            Row r = cat.rows.get(ri);
            int ry = rowY(ri);
            if (r.kind == 0) {
                // pill on the right
                int px = contentX() + contentW() - 58;
                if (x >= px && x <= px + 46 && y >= ry + 11 && y <= ry + 29) {
                    boolean nv = !r.bGet.getAsBoolean();
                    r.bSet.accept(nv);
                    McsmExtrasConfig.save();
                    return true;
                }
            } else if (r.kind == 1) {
                int tx = contentX() + contentW() - 168;   // dot track origin
                int vx = contentX() + contentW() - 74;    // value chip
                if (x >= tx && x <= tx + DOTS * DOT_GAP && y >= ry + 10 && y <= ry + 32) {
                    scrubTo(r, x - tx);
                    scrubRow = ri;
                    return true;
                }
                if (x >= vx && x <= contentX() + contentW() - 8 && y >= ry + 10 && y <= ry + 32) {
                    startEdit(r);
                    return true;
                }
            } else {
                // action button: whole card
                r.action.run();
                this.init();
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }

    private void scrubTo(Row r, int dx) {
        double t = (double) dx / (DOTS * DOT_GAP);
        if (t < 0.0) t = 0.0;
        if (t > 1.0) t = 1.0;
        r.dSet.accept(r.lo + (r.hi - r.lo) * t);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (scrubRow >= 0) {
            Category cat = categories.get(currentCat);
            if (scrubRow < cat.rows.size()) {
                Row r = cat.rows.get(scrubRow);
                if (r.kind == 1) {
                    int tx = contentX() + contentW() - 168;
                    scrubTo(r, (int) dragX - tx);
                    return true;
                }
            }
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (scrubRow >= 0) {
            scrubRow = -1;
            McsmExtrasConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX < SIDE_W - 6) {
            sidebarScroll -= (int) Math.round(scrollY * 20.0);
            int max = Math.max(0, categories.size() * 26 - (sideBottom() - sideTop()));
            if (sidebarScroll < 0) sidebarScroll = 0;
            if (sidebarScroll > max) sidebarScroll = max;
        } else if (mouseX >= contentX() - 6 && mouseY > TOP_H && mouseY < this.height - FOOT_H) {
            contentScroll -= (int) Math.round(scrollY * 22.0);
            Category cat = categories.get(currentCat);
            int max = Math.max(0, cat.rows.size() * ROW_H - (contentBottom() - contentTop()));
            if (contentScroll < 0) contentScroll = 0;
            if (contentScroll > max) contentScroll = max;
        }
        return true;
    }

    @Override
    public void onClose() {
        commitEdit();
        McsmExtrasConfig.save();
        Minecraft.getInstance().setScreenAndShow(this.parent);
    }

    // ---- render -----------------------------------------------------------

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        int w = this.width;
        int h = this.height;

        // deep charcoal room
        g.fillGradient(0, 0, w, h, BG_TOP, BG_BOTTOM);
        // faint warm glow behind the top bar
        g.fillGradient(0, 0, w, 90, ACCENT_SOFT, 0x00D9A441);
        // vignette
        g.fillGradient(0, 0, w, 40, 0x88000000, 0x00000000);
        g.fillGradient(0, h - 40, w, h, 0x00000000, 0x88000000);

        drawTopBar(g, w);
        drawSidebar(g, mouseX, mouseY);
        drawContent(g, w, h, mouseX, mouseY);
        drawFooter(g, w, h);
    }

    private void drawTopBar(GuiGraphicsExtractor g, int w) {
        g.fill(0, 0, w, TOP_H, 0xF2080A0E);
        g.fill(0, TOP_H, w, TOP_H + 1, ACCENT);
        g.text(this.font, "§6§lDEVOURING STORMS §f— §eSTORY MODE CONSOLE", 14, 10, TEXT_HI, true);
        g.text(this.font, "§7Open Devouring Storms 10000.0.0-PRE-RELEASE-ALPHA-1-DEVOURING-STORMS-338",
                w - 30 - this.font.width("Open Devouring Storms 10000.0.0-PRE-RELEASE-ALPHA-1-DEVOURING-STORMS-338"), 12, TEXT_DIM, false);
        // close box
        g.fill(w - 24, 8, w - 6, 26, 0x00000000);
        g.fill(w - 20, 12, w - 10, 13, OFF_GRAY);
        g.fill(w - 15, 9, w - 14, 19, OFF_GRAY);
        g.fill(w - 20, 18, w - 10, 19, OFF_GRAY);
        g.fill(w - 16, 14, w - 13, 17, ACCENT);
    }

    private void drawSidebar(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.fill(0, TOP_H, SIDE_W, this.height - FOOT_H, SIDE_BG);
        g.fill(SIDE_W, TOP_H, SIDE_W + 1, this.height - FOOT_H, CARD_EDGE);
        g.text(this.font, "§7CHAPTERS", 12, sideTop() - 2, TEXT_DIM, false);
        int top = sideTop() + 12;
        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            int y = top + i * 26 - sidebarScroll;
            if (y + 26 < top || y > sideBottom()) {
                continue;
            }
            boolean active = i == currentCat;
            hoverSide = (mouseX < SIDE_W - 6 && mouseY >= y && mouseY < y + 26) ? i : hoverSide;
            if (active) {
                g.fill(0, y, SIDE_W - 6, y + 24, 0xCC141821);
                g.fill(0, y, 3, y + 24, ACCENT);
            } else if (hoverSide == i) {
                g.fill(0, y, SIDE_W - 6, y + 24, 0x66141821);
            }
            g.text(this.font, (active ? "§6" : "§7") + c.chapter, 14, y + 8, active ? VALUE_GOLD : TEXT_DIM, false);
            g.text(this.font, (active ? "§f" : "§8") + c.title, 32, y + 8, active ? TEXT_HI : TEXT_DIM, false);
            if (active) {
                g.text(this.font, "§7" + c.rows.size() + " controls", 32, y + 17, TEXT_DIM, false);
            }
        }
    }

    private void drawContent(GuiGraphicsExtractor g, int w, int h, int mouseX, int mouseY) {
        Category cat = categories.get(currentCat);
        int cx = contentX();
        int cw = contentW();

        // chapter header
        g.text(this.font, "§6§lCHAPTER " + cat.chapter, cx, TOP_H + 8, VALUE_GOLD, true);
        g.text(this.font, "§f§l" + cat.title, cx + this.font.width("CHAPTER " + cat.chapter) + this.font.width("§6§l") + 8, TOP_H + 8, TEXT_HI, true);
        g.text(this.font, "§7" + cat.blurb, cx, TOP_H + 22, TEXT_DIM, false);
        g.fill(cx, TOP_H + 36, cx + cw, TOP_H + 37, CARD_EDGE);

        hoverRow = -1;
        for (int i = 0; i < cat.rows.size(); i++) {
            Row r = cat.rows.get(i);
            int y = rowY(i);
            if (y + ROW_H < contentTop() || y > contentBottom()) {
                continue;
            }
            boolean hov = mouseX >= cx && mouseX <= cx + cw && mouseY >= y && mouseY < y + ROW_H;
            if (hov) {
                hoverRow = i;
            }
            // translucent episodic card
            g.fill(cx, y, cx + cw, y + ROW_H, hov ? CARD_HOVER : CARD);
            g.fill(cx, y, cx + cw, y + 1, CARD_EDGE);
            g.fill(cx, y + ROW_H - 1, cx + cw, y + ROW_H, CARD_EDGE);
            g.fill(cx, y, cx + 1, y + ROW_H, hov ? CARD_EDGE_H : CARD_EDGE);
            g.fill(cx + cw - 1, y, cx + cw, y + ROW_H, hov ? CARD_EDGE_H : CARD_EDGE);

            g.text(this.font, (hov ? "§f" : "§e") + r.label, cx + 14, y + 8, hov ? TEXT_HI : TEXT_MID, false);
            if (r.kind == 0) {
                drawPill(g, cx + cw - 58, y + 11, r.bGet.getAsBoolean(), hov);
            } else if (r.kind == 1) {
                drawDotTrack(g, cx + cw - 168, y + 21, r.dGet.getAsDouble(), r.lo, r.hi);
                int vx = cx + cw - 74;
                String val = String.format("%.2f", r.dGet.getAsDouble());
                boolean overVal = mouseX >= vx && mouseX <= cx + cw - 8 && mouseY >= y + 10 && mouseY < y + 32;
                g.text(this.font, (overVal ? "§6" : "§e") + val, vx + 12 - this.font.width(val), y + 13, overVal ? VALUE_GOLD : VALUE_GOLD, false);
                if (overVal) {
                    g.fill(vx + 10, y + 27, cx + cw - 10, y + 28, ACCENT);
                }
            } else {
                // action button
                int bx = cx + cw - 172;
                boolean overAct = mouseX >= bx && mouseX <= bx + 164 && mouseY >= y + 8 && mouseY < y + 34;
                g.fill(bx, y + 8, bx + 164, y + 34, overAct ? ACCENT_SOFT : 0x00000000);
                g.fill(bx, y + 8, bx + 164, y + 9, ACCENT);
                g.fill(bx, y + 33, bx + 164, y + 34, ACCENT);
                g.fill(bx, y + 8, bx + 1, y + 34, ACCENT);
                g.fill(bx + 163, y + 8, bx + 164, y + 34, ACCENT);
                g.text(this.font, "§6" + r.label, bx + 10, y + 16, VALUE_GOLD, true);
            }
        }
    }

    /** Sliding pill toggle (Telltale on/off). */
    private void drawPill(GuiGraphicsExtractor g, int x, int y, boolean on, boolean hov) {
        int wpx = 46;
        int hpx = 18;
        g.fill(x, y, x + wpx, y + hpx, TRACK_OFF);
        g.fill(x, y, x + wpx, y + 1, on ? ACCENT : CARD_EDGE);
        g.fill(x, y + hpx - 1, x + wpx, y + hpx, on ? ACCENT : CARD_EDGE);
        g.fill(x, y, x + 1, y + hpx, on ? ACCENT : CARD_EDGE);
        g.fill(x + wpx - 1, y, x + wpx, y + hpx, on ? ACCENT : CARD_EDGE);
        if (on) {
            g.fill(x + 2, y + 2, x + wpx - 2, y + hpx - 2, ACCENT_SOFT);
        }
        int kx = on ? x + wpx - 16 : x + 4;
        g.fill(kx, y + 3, kx + 12, y + hpx - 3, on ? ACCENT : OFF_GRAY);
        g.fill(kx + 3, y + 6, kx + 9, y + hpx - 6, on ? 0xFFF4E3B2 : 0xFF3A3F49);
        g.text(this.font, on ? "§6ON" : "§8OFF", x + (on ? 8 : 20), y + 4, on ? 0xFFF4E3B2 : TEXT_DIM, false);
    }

    /** Geometric dot track — diamond dots, click/drag to scrub. */
    private void drawDotTrack(GuiGraphicsExtractor g, int x, int cy, double value, double lo, double hi) {
        double t = hi > lo ? (value - lo) / (hi - lo) : 0.0;
        int active = (int) Math.round(t * (DOTS - 1));
        g.fill(x, cy, x + DOTS * DOT_GAP, cy + 1, 0xFF2A2F3A);
        for (int i = 0; i < DOTS; i++) {
            int dx = x + i * DOT_GAP;
            int col = i < active ? 0x88D9A441 : (i == active ? ACCENT : 0xFF3A3F49);
            g.fill(dx, cy - 4, dx + 1, cy + 5, col);      // vertical
            g.fill(dx - 1, cy - 2, dx + 2, cy + 3, col);  // upper band
            g.fill(dx - 2, cy, dx + 3, cy + 1, col);      // middle
            g.fill(dx - 1, cy + 2, dx + 2, cy + 3, col);  // lower band
            if (i == active) {
                g.fill(dx - 1, cy - 5, dx + 2, cy - 4, 0xFFF4E3B2);
            }
        }
    }

    private void drawFooter(GuiGraphicsExtractor g, int w, int h) {
        g.fill(0, h - FOOT_H, w, h, 0xF2080A0E);
        g.fill(0, h - FOOT_H, w, h - FOOT_H + 1, CARD_EDGE);
        // done
        g.fill(14, h - FOOT_H + 3, 74, h - FOOT_H + 21, 0x00000000);
        g.fill(14, h - FOOT_H + 3, 74, h - FOOT_H + 4, ACCENT);
        g.fill(14, h - FOOT_H + 20, 74, h - FOOT_H + 21, ACCENT);
        g.fill(14, h - FOOT_H + 3, 15, h - FOOT_H + 21, ACCENT);
        g.fill(73, h - FOOT_H + 3, 74, h - FOOT_H + 21, ACCENT);
        g.text(this.font, "§6DONE", 28, h - FOOT_H + 8, VALUE_GOLD, true);
        g.text(this.font, "§8Shift+A / Shift+C toggles console (Ctrl+C is free for copy) · click a value to type it · drag the diamond dots to scrub",
                88, h - FOOT_H + 9, TEXT_DIM, false);
    }
}
