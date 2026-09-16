package net.mcsm.extras;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * MCSM extras config (Version 10000.0.0-PRE-RELEASE-ALPHA-1-DEVOURING-STORMS-338).
 * Drives all MCSM additions, atmosphere parameters, debris editor, color customization,
 * animation controller, nightglow halo expansion, and sci-fi UI parameters.
 */
public final class McsmExtrasConfig {
    public static final String BUILD_VERSION = "7000.0.0-M";

    // ---- Core Gameplay & Storm Mechanics -----------------------------------
    public static boolean enableTentacleGrab = true;
    public static double  grabIntervalSeconds = 11.0;
    public static boolean enableBeaconStorm = true;
    public static double  beaconCooldownSeconds = 30.0;
    public static boolean enableRiseFx = true;
    public static boolean spiralCounterClockwise = true;
    public static boolean enableBeaconBlock = true;
    public static boolean ogCemModels = true;
    public static double  smudgeScale = 0.5;

    // ---- Debris Editor & Native Block Rescaling ----------------------------
    /** 0 = Clockwise, 1 = Counterclockwise, 2 = Upright, 3 = Downward, 4 = Spiral, 5 = Radial */
    public static int     debrisMovementDirection = 0;
    /** Force debris particles to pull pre-existing native block states (Obsidian / Crying Obsidian) */
    public static boolean forceNativeBlockDebris = true;
    /** Scale multiplier for native debris blocks (default 3.0 = 300% rescaled 1.5F to 4.0F) */
    public static double  debrisScaleMultiplier = 3.0;
    /** Scale for ambient purple coils (default 0.35 = 35% shrunk) */
    public static double  purpleCoilScale = 0.35;

    // ---- Wither Storm Color & Texture Customization -----------------------
    public static boolean useCustomColors = true;
    public static double  customEyeR = 0.00; // Electrical Neon Cyan (#00F3FF) or Sea-Green (#00A877)
    public static double  customEyeG = 0.95;
    public static double  customEyeB = 1.00;
    public static double  customTeethR = 0.00;
    public static double  customTeethG = 0.95;
    public static double  customTeethB = 1.00;
    public static double  customBeamR = 0.00;
    public static double  customBeamG = 0.85;
    public static double  customBeamB = 1.00;
    public static double  customSkinTintR = 0.15;
    public static double  customSkinTintG = 0.15;
    public static double  customSkinTintB = 0.18;

    // ---- Animation Controller & In-Game Editing ---------------------------
    public static double  animIdleSpeed = 1.0;
    public static double  animRoarIntensity = 1.2;
    public static double  animJawSlack = 1.0;
    public static double  animTentacleSlamForce = 1.5;
    public static double  animHeadSwayGain = 1.0;
    public static boolean lockCanonicalTexture = true;

    // ---- Native Tattletale Gloss Sheen (Build #368) -----------------------
    // Secondary translucent pass that re-renders the storm body against the
    // scrolling storm_gloss.png sheet. On by default: shiny out of the box,
    // no external shader pack required.
    public static boolean nativeGlossSheen = true;

    // ---- Build #371 — Dual Geometry, Traced Shading, OG Sun & Story Menu --
    // Dual-mode WitherStormP4 body. FALSE (default) = ORIGINAL base-mod
    // geometry verbatim (fixes the summon-time vertex-overflow crash).
    // TRUE = the clean 495-cube Blockbench/StageB matrix, bound strictly to
    // the Netflix / Custom preset path (McsmPresetMeshSync + panel toggle).
    public static boolean customMeshModel = false;
    // OG traced-shading body texture (the authentic StageB mottle) as the
    // DEFAULT for phases 4-5.5. TRUE (default) = traced look out of the box;
    // FALSE = the regular flat deep-black canonical sheet ("the OG ones"
    // stay the plain option). Phases 1-5.5 never use the cosmic sheet.
    public static boolean tracedShadingBody = true;
    // OG "SAGE MCSM" 3D sun glow (screen-space storm_sun_glow post shader),
    // restored to the base default strength. On by default, configurable.
    public static boolean  ogSunGlow = true;
    public static double   ogSunGlowStrength = 2.2; // base default, range 0-3
    // Build #371 — cinematic STORY-MODE main menu backdrop (procedural
    // panorama: night sky, sun slab, moon, nebula, mountain silhouettes).
    // TRUE (default) = the cinematic scene; FALSE = the plain gradient.
    public static boolean  storyMenuBackdrop = true;
    /** Build #416 -- restore the vanilla cube panorama behind the main menu.
     *  #375 deleted it ("the panorama is gone, on or off") and replaced the
     *  whole backdrop with a flat storm-space gradient; the revamp's own
     *  release notes then read as if the panorama had never existed. It is
     *  back by default, graded into the DS palette instead of removed, and
     *  this toggle is the explicit opt-out (flat storm space, #375 look). */
    public static boolean  menuPanorama = true;

    // ---- Build #416 (D.8): the Decayed Reality ---------------
    /** The new gameplay layer: a real dimension, its content, its creatures,
     *  its events. Off only if a pack explicitly wants the old scope. */
    public static boolean decayedReality = true;
    /** Abandoned-city generation inside the decayed reality (phase 2). */
    public static boolean abandonedCities = true;
    /** Reality-glitch hallucination pass: torn frames, duplicating silhouettes,
     *  false storms, whispering chat (phase 4). */
    public static boolean realityGlitches = true;
    /** Hallucination intensity, 0..1 (drives both the visual pass and the audio). */
    public static double hallucinationIntensity = 0.6;
    /** The Black Hole event: a collapsing singularity that eats the arena. */
    public static boolean blackHoleEvent = true;
    /**
     * How long a black hole this build opens stays open, in seconds. The hole is
     * collapsed by the same code that opened it, so an event can never become a
     * permanent hole in someone's world.
     */
    public static double blackHoleSeconds = 180.0;
    /** Mega-tornadoes that rip across the decayed reality. */
    public static boolean megaTornadoes = true;
    /** BUILD #433 -- the sky vortexes that open over the player and drop things. */
    public static boolean skyVortexes = true;
    /**
     * BUILD #434 -- the sky's bottom layer: the wall that carries the
     * horizon colour downward under the world, so the sky has a floor as
     * well as a ceiling. It is drawn geometry, not a shader, so it works
     * with no shader pack installed.
     */
    public static boolean skyFloorBand = true;
    /** Reality creatures: the new hostile spawns (phase 3). */
    public static boolean realityCreatures = true;
    /** Story Mode quest + lore layer: dialogue, objectives, chapter log. */
    public static boolean storyQuests = true;

    // ---- Build #416 (D.8, phase 5): the story terminal ---------------------
    /** The holographic terminal: the antenna's restricted console, the field
     *  guide and the in-game C key. Off = the items do nothing when used and C
     *  is a dead key again. */
    public static boolean storyTerminal = true;
    /** The antenna picks up radio signals while it is carried. */
    public static boolean antennaSignals = true;
    /** Seconds between two signals on the same antenna (5 - 600). */
    public static double antennaSignalSeconds = 45.0;
    /** Every player is handed the antenna the first time this build sees them,
     *  which is the user's "given on spawn": it arrives with a line of lore in
     *  chat, once, and is never handed out again if they still have one. */
    public static boolean antennaOnSpawn = true;

    // ---- Build #416 (D.8, phase 6): THE MASSG ------------------------------
    /** The creature, its hallucinations and its sky terminal. */
    public static boolean massgEnabled = true;
    /** It cannot be killed. Leave this on: it is the whole point of it. */
    public static boolean massgUnkillable = true;
    /** Its size. 3 is a tower; 8 is a skyline. */
    public static double massgScale = 5.0;
    /** Hallucinated shapes, false storms in chat, things swimming in the air. */
    public static boolean massgHallucinations = true;

    // ---- Build #422: the sky and the backdrop ------------------------------
    /** BUILD #422 (Phase 1) -- how far the 2D backdrop sticker's bottom half is
     *  stretched below the horizon, as a multiple of its own height. 6 puts the
     *  bottom edge of the sheet far past bedrock level, so there is no bottom
     *  edge left to see; 1 or less leaves the base sticker alone. */
    public static double backdropBottomStretch = 6.0;

    /** BUILD #423 -- weld the phase-5.5 upper back to the body. The huge back's
     *  own centre is ~11.7 blocks from its model origin, so enlarging it by 1.72x
     *  about that origin throws it ~34 world blocks up and back off the storm.
     *  On (the default) the enlargement is taken about the model's own centre
     *  instead, so the back grows where it already is. */
    public static boolean hugeBackCentred = true;

    /** BUILD #426 -- "the purple colour still renders on top of the wither
     *  storm." The phase glare layers are camera-facing cards centred on the
     *  storm, so their purple was drawn over the body. On (the default) every
     *  glare card is pushed a body radius and a bit behind the storm, where the
     *  depth test keeps it off the silhouette while the oversized skin still
     *  rings it. Off restores the old flat-card look. */
    public static boolean glareBehindBody = true;

    /** BUILD #426 -- the faint purple pool the blob pass cast on the ground
     *  under the beams. It is a second purple wash across the storm's base, and
     *  it was visible from a distance as a disc on top of the body, so it is
     *  OFF by default now; the switch is here for anyone who wants it back. */
    public static boolean stormGroundPool = false;

    /** BUILD #429 -- "I would like it that they summon in the regular [world]."
     *  The abandoned-city generator was reachable only from the decayed reality;
     *  with this on it also raises districts in the overworld, never within 384
     *  blocks of world spawn. */
    public static boolean citiesInOverworld = true;
    // ---- Build #374 — REAL Story Mode skybox (cube, not dome) -------------
    // Textured cube around the camera; 6 phase skies (lavender day, midnight
    // blue, sunset, turquoise, purple, witherstorm brown-purple) follow the
    // nearest storm's phase and fade back to the regular vanilla sky.
    // FALSE = the vanilla variant (plain regular Minecraft sky).
    public static boolean  skyboxEnabled = true;
    /** Seconds for the sky cross-fade / fade-back-to-vanilla (0.25 - 20). */
    public static double   skyboxFadeSeconds = 2.5;
    /** Cube size multiplier (0.5 - 1.5). */
    public static double   skyboxSize = 1.0;
    // ---- Build #375 — the REAL Telltale glare shells ----------------------
    // The three actual Telltale glare/atmosphere images, layered as big
    // slowly-swirling sky shells around every storm (the "3D-like
    // atmospheric effect"). FALSE = off (no glare shells).
    public static boolean  glareBackdrop = true; // 3 real Telltale glare sheets (glare_1/2/3.png) bound to the moving storm origin
    /** Glare shell size multiplier (0.3 - 2.5). */
    // BUILD #416 (D.8 glitch pass): 1.0 drew the glare at storm scale, which is
    // what made it a "smudgy circular object hovering right above the storm,
    // made absolutely huge". The reference frames show a tight aura, so the
    // default is now roughly a third of that; the panel slider still goes to 2.0
    // for anyone who wants the big dish back.
    public static double   glareBackdropSize = 0.62;
    /** Glare shell opacity multiplier (0.0 - 2.0). */
    public static double   glareBackdropStrength = 1.0;
    /** When the cube is fully opaque, cancel the vanilla sky pass entirely so
     *  the base storm-darken/void tint cannot show through as a second layer. */
    public static boolean  skyPassCancel = true;
    /** Build #375: the Telltale black blur - a soft dark ring hugging the storm
     *  silhouette (attached to the storm, moves with it). */
    public static boolean  stormBlurEnabled = true;
    /** Blur ring size multiplier (0.4 - 2.5). */
    public static double   stormBlurSize = 1.0;
    /** Blur ring darkness multiplier (0 - 2). */
    public static double   stormBlurStrength = 1.0;
    // ---- Build #374 — cinematic boot + epic depth animations -------------
    /** Pre-game side-view cutscene + command block burst into the main menu. */
    public static boolean  cinematicBootEnabled = true;
    /** The world "cracks apart" (fissures + sky shockwaves) on world load. */
    public static boolean  worldCrackIntro = true;

    // ---- Nightglow & Silhouette Halo Expansion ---------------------------
    public static boolean nightglowBodyOutline = true; // Outline whole body bottom tail to top of phase 5
    public static boolean nightglowBluishGlow = true; // Bluish halo base
    /** Build #374: the restored working blue halo — chassis-welded ring that
     *  ramps in at phase 4.0 and holds through every later stage. */
    public static boolean stormHaloEnabled = true;
    // BUILD #416 (D.8, phase 5) -- OFF by default. The user's second report:
    // "the purple glint you added around the storm in its later phases ... turn
    // it off by default, but keep it as an option". This IS that layer, so the
    // default flips and the panel row stays exactly where it was, which keeps
    // the option available for anyone who wants the purple dish back. A config
    // file written by an older build stored `true`, so `purpleGlintMigrated`
    // flips it off once for existing installs (see load()).
    public static boolean nightglowPurpleGlow55 = false; // Gigantic purple glow for phases 5.1-5.9 (opt-in)
    /** Set once the purple glint has been defaulted off for an existing config. */
    public static boolean purpleGlintMigrated = false;
    public static boolean nightglowThickBlackGlow = true; // Thick black core glow
    public static double  nightglowRadiusMultiplier = 1.5;

    // ---- End Flashes & Cinematic Death Pass --------------------------------
    public static boolean endFlashesPhase6 = true; // Giant End flashes for Phase 6+
    public static boolean transparentHorizonWings = true; // Transparent giant wings across horizon during death
    public static boolean deathSupernovaImplosion = true;

    // ---- UI & Sci-Fi Control Panel Styling --------------------------------
    public static boolean uiBorderLines = true; // Image 3 pixelated silver border frame
    public static boolean sciFiPanelLayout = true;
    // same pass: 0.58 -> 0.34, see glareBackdropSize above
    public static double  glareSize = 0.34;
    public static boolean auroraEnabled = true;
    public static boolean deathCinematic = true;
    public static boolean supernovaRings = true;
    public static boolean smokeScreen = true;
    public static boolean purpleSky = true;
    public static boolean dustWaves = true;
    public static boolean realityTear = true;
    public static boolean obliterateFlash = true;
    public static boolean obliterateKick = false;

    // ---- Gates & System Controls -------------------------------------------
    public static boolean forceMcsmLook = true;
    public static boolean forceMcsmWorld = true;
    // BUILD #416 (D.8, glitch pass) -- TRUE again, and it is the reason the
    // storm's teeth and eye glow were invisible.
    //
    // Under Iris the base mod hands the whole look to the shader pack: its own
    // teeth glow, eye glow, halo bloom, sun glow, shadow map and impact lights
    // are all switched OFF behind ShaderPackCompat.active(). The embedded
    // Devouring Storms pack does not draw any of them, so with the gate off the
    // teeth rendered as plain unlit geometry -- the exact report ("the storm's
    // teeth do not glow nor the aura around the teeth"). Forcing the gate true
    // makes the mod draw its own emissive teeth/eyes/halo whether a pack is
    // loaded or not. The panel row still exists for A/B testing, but a config
    // file written by an older build is migrated to true once, because the
    // stored false is what caused the invisible teeth.
    public static boolean shaderPackGate = true;
    /** Set once the gate has been migrated to true (see the migration in load()). */
    public static boolean shaderPackGateMigrated = false;

    // BUILD #416 (D.8) -- how far the storm's sky reaches.
    //
    // The user: "as you get far away from hundreds of blocks, 500 blocks, it
    // starts to fade back to normal". This is that distance: the storm owns the
    // sky completely inside half of it, hands it back smoothly between half and
    // 1.8x, and beyond that the sky is pure vanilla again. Before this the
    // decision had no distance in it at all, so a storm on the far side of the
    // world still repainted the sky over the player's head.
    public static double skyFadeDistance = 500.0;

    // BUILD #416 (D.8, phase 3) -- the bestiary, the boss ladder and the Creator.
    //
    // Phase 3 is the creatures. "Reality Creatures" (above, from the original
    // mandate list) is the bestiary's own switch, kept exactly where it was so a
    // player who already turned it off does not get mobs after this build. The
    // two new switches are the parts the user named separately:
    //
    //   * bossLadder -- the five rungs, summoned once per storm as it grows, each
    //     fought with a health bar, adds and a staged second half. Turning it off
    //     leaves the bestiary running and the ladder never summons.
    //   * creatorArms -- the top rung's payoff: seven arms coming down through
    //     rips in the sky, drawn as world geometry and (server-side) opening the
    //     sky over whoever is standing under them.
    public static boolean bossLadder = true;
    public static boolean creatorArms = true;
    /** How heavy the Creator's arms read, 0.5x to 2x. */
    public static double creatorArmScale = 1.0;
    public static boolean commandWire = true;
    public static boolean mcsmInstructions = true;

    // ---- Atmosphere Parameters --------------------------------------------
    public static double nightSkyOpacity = 0.42;
    public static double phase55Threshold = 5.5;
    public static double phase5_9PinkIntensity = 1.0;
    public static double cloudAlpha = 1.0;
    public static double cloudSpeed = 1.0;
    public static double precipitationIntensity = 1.0;
    public static double glareAnimPhase = 0.0;
    public static double bodyAnimPulse = 0.0;
    public static double glareAnimIntensity = 0.0;
    public static boolean glareNonEuclidean = false;

    // ---- Advanced Atmospheric VFX & Lighting ------------------------------
    public static boolean beaconGlow = true;
    public static boolean portalLights = true;
    public static boolean biomeAtmospherics = true;
    public static boolean netherRedFog = true;
    public static boolean snowSkyBand = true;
    public static boolean auroraRibbons = true;
    public static boolean twinklingStars = true;
    public static boolean comets = true;
    public static boolean coloredSparkles = true;
    public static boolean waterGodRays = true;
    public static boolean endSkyVortex = true;
    public static boolean globalShadows = true;
    public static boolean witherStormEnhancedAi = true;
    /**
     * Build #374: the giant 3D storm preview in the base config screen.
     * DEFAULT FALSE = removed (user order: "remove the giant preview in the
     * config menu"); the console toggle brings it back.
     */
    public static boolean giantPreviewEnabled = false;
    public static boolean npcWalkAnimations = true;
    public static boolean stormBodySway = true;
    public static boolean infiniteBackGrowth = false;
    public static double infiniteBackGrowthSpeed = 0.10;
    /**
     * Build #374 -- DEFAULT ON. The user's standing order: "on first play the
     * built-in resource pack and shader pack get summoned/extracted and
     * applied". With the default false the installer ran in uninstall mode on
     * every launch (deleted the zip, reset Iris to (internal)) which is why
     * the game booted into "Loaded Shaderpack: (off) (fallback)". The
     * panel toggle still lets a player switch it off.
     */
    public static boolean embeddedShaderPack = true;

    private static boolean loaded = false;
    private static long stamp = -1L;

    private static File file() {
        return new File(new File(System.getProperty("user.dir", "."), "config"), "mcsm_storm_extras.properties");
    }

    public static synchronized void save() {
        try {
            File f = file();
            f.getParentFile().mkdirs();
            Properties p = new Properties();
            p.setProperty("config_version", BUILD_VERSION);
            p.setProperty("enable_tentacle_grab", String.valueOf(enableTentacleGrab));
            p.setProperty("grab_interval_seconds", String.valueOf(grabIntervalSeconds));
            p.setProperty("enable_beacon_storm", String.valueOf(enableBeaconStorm));
            p.setProperty("beacon_cooldown_seconds", String.valueOf(beaconCooldownSeconds));
            p.setProperty("enable_rise_fx", String.valueOf(enableRiseFx));
            p.setProperty("spiral_counter_clockwise", String.valueOf(spiralCounterClockwise));
            p.setProperty("enable_beacon_block", String.valueOf(enableBeaconBlock));
            p.setProperty("og_cem_models", String.valueOf(ogCemModels));
            p.setProperty("smudge_scale", String.valueOf(smudgeScale));
            p.setProperty("debris_movement_direction", String.valueOf(debrisMovementDirection));
            p.setProperty("force_native_block_debris", String.valueOf(forceNativeBlockDebris));
            p.setProperty("debris_scale_multiplier", String.valueOf(debrisScaleMultiplier));
            p.setProperty("purple_coil_scale", String.valueOf(purpleCoilScale));
            p.setProperty("use_custom_colors", String.valueOf(useCustomColors));
            p.setProperty("custom_eye_r", String.valueOf(customEyeR));
            p.setProperty("custom_eye_g", String.valueOf(customEyeG));
            p.setProperty("custom_eye_b", String.valueOf(customEyeB));
            p.setProperty("anim_idle_speed", String.valueOf(animIdleSpeed));
            p.setProperty("anim_roar_intensity", String.valueOf(animRoarIntensity));
            p.setProperty("native_gloss_sheen", String.valueOf(nativeGlossSheen));
            p.setProperty("custom_mesh_model", String.valueOf(customMeshModel));
            p.setProperty("traced_shading_body", String.valueOf(tracedShadingBody));
            p.setProperty("og_sun_glow", String.valueOf(ogSunGlow));
            p.setProperty("og_sun_glow_strength", String.valueOf(ogSunGlowStrength));
            p.setProperty("story_menu_backdrop", String.valueOf(storyMenuBackdrop));
            p.setProperty("menu_panorama", String.valueOf(menuPanorama));
            p.setProperty("decayed_reality", String.valueOf(decayedReality));
            p.setProperty("abandoned_cities", String.valueOf(abandonedCities));
            p.setProperty("reality_glitches", String.valueOf(realityGlitches));
            p.setProperty("hallucination_intensity", String.valueOf(hallucinationIntensity));
            p.setProperty("black_hole_event", String.valueOf(blackHoleEvent));
            p.setProperty("black_hole_seconds", String.valueOf(blackHoleSeconds));
            p.setProperty("mega_tornadoes", String.valueOf(megaTornadoes));
            p.setProperty("sky_vortexes", String.valueOf(skyVortexes));
            p.setProperty("sky_floor_band", String.valueOf(skyFloorBand));
            p.setProperty("reality_creatures", String.valueOf(realityCreatures));
            p.setProperty("story_quests", String.valueOf(storyQuests));
            p.setProperty("embedded_shader_pack", String.valueOf(embeddedShaderPack));
            p.setProperty("shader_pack_gate_migrated", String.valueOf(shaderPackGateMigrated));
            p.setProperty("sky_fade_distance", String.valueOf(skyFadeDistance));
            p.setProperty("boss_ladder", String.valueOf(bossLadder));
            p.setProperty("creator_arms", String.valueOf(creatorArms));
            p.setProperty("creator_arm_scale", String.valueOf(creatorArmScale));
            p.setProperty("skybox_enabled", String.valueOf(skyboxEnabled));
            p.setProperty("skybox_fade_seconds", String.valueOf(skyboxFadeSeconds));
            p.setProperty("skybox_size", String.valueOf(skyboxSize));
            p.setProperty("glare_backdrop", String.valueOf(glareBackdrop));
            p.setProperty("glare_backdrop_size", String.valueOf(glareBackdropSize));
            p.setProperty("glare_backdrop_strength", String.valueOf(glareBackdropStrength));
            p.setProperty("sky_pass_cancel", String.valueOf(skyPassCancel));
            p.setProperty("storm_blur_enabled", String.valueOf(stormBlurEnabled));
            p.setProperty("storm_blur_size", String.valueOf(stormBlurSize));
            p.setProperty("storm_blur_strength", String.valueOf(stormBlurStrength));
            p.setProperty("cinematic_boot_enabled", String.valueOf(cinematicBootEnabled));
            p.setProperty("world_crack_intro", String.valueOf(worldCrackIntro));
            p.setProperty("nightglow_body_outline", String.valueOf(nightglowBodyOutline));
            p.setProperty("nightglow_bluish_glow", String.valueOf(nightglowBluishGlow));
            p.setProperty("storm_halo_enabled", String.valueOf(stormHaloEnabled));
            p.setProperty("nightglow_purple_glow55", String.valueOf(nightglowPurpleGlow55));
            p.setProperty("purple_glint_migrated", String.valueOf(purpleGlintMigrated));
            p.setProperty("nightglow_thick_black_glow", String.valueOf(nightglowThickBlackGlow));
            p.setProperty("end_flashes_phase6", String.valueOf(endFlashesPhase6));
            p.setProperty("transparent_horizon_wings", String.valueOf(transparentHorizonWings));
            p.setProperty("ui_border_lines", String.valueOf(uiBorderLines));
            p.setProperty("glare_size", String.valueOf(glareSize));
            p.setProperty("aurora_enabled", String.valueOf(auroraEnabled));
            p.setProperty("death_cinematic", String.valueOf(deathCinematic));
            p.setProperty("experimental_story_mode_stage", String.valueOf(ENABLE_EXPERIMENTAL_STORY_MODE_STAGE));
            p.setProperty("auto_select_shader_pack", String.valueOf(autoSelectShaderPack));
            p.setProperty("auto_start_towns", String.valueOf(autoStartTowns));
            p.setProperty("phase9_ring_cubes", String.valueOf(phase9RingCubes));
            p.setProperty("storm_model_scale", String.valueOf(stormModelScale));
            p.setProperty("storm_rings", String.valueOf(stormRings));
            p.setProperty("story_mode_accurate_sun", String.valueOf(storyModeAccurateSunSun));
            p.setProperty("tentacle_girth", String.valueOf(tentacleGirth));
            p.setProperty("supernova_rings", String.valueOf(supernovaRings));
            p.setProperty("smoke_screen", String.valueOf(smokeScreen));
            p.setProperty("purple_sky", String.valueOf(purpleSky));
            p.setProperty("story_terminal", String.valueOf(storyTerminal));
            p.setProperty("antenna_signals", String.valueOf(antennaSignals));
            p.setProperty("antenna_signal_seconds", String.valueOf(antennaSignalSeconds));
            p.setProperty("antenna_on_spawn", String.valueOf(antennaOnSpawn));
            p.setProperty("massg_enabled", String.valueOf(massgEnabled));
            p.setProperty("massg_unkillable", String.valueOf(massgUnkillable));
            p.setProperty("massg_scale", String.valueOf(massgScale));
            p.setProperty("massg_hallucinations", String.valueOf(massgHallucinations));
            p.setProperty("backdrop_bottom_stretch", String.valueOf(backdropBottomStretch));
            p.setProperty("huge_back_centred", String.valueOf(hugeBackCentred));
            p.setProperty("glare_behind_body", String.valueOf(glareBehindBody));
            p.setProperty("storm_ground_pool", String.valueOf(stormGroundPool));
            p.setProperty("cities_in_overworld", String.valueOf(citiesInOverworld));
            p.setProperty("dust_waves", String.valueOf(dustWaves));
            p.setProperty("reality_tear", String.valueOf(realityTear));
            p.setProperty("force_mcsm_look", String.valueOf(forceMcsmLook));
            p.setProperty("force_mcsm_world", String.valueOf(forceMcsmWorld));
            p.setProperty("command_wire", String.valueOf(commandWire));
            p.setProperty("mcsm_instructions", String.valueOf(mcsmInstructions));
            p.setProperty("beacon_glow", String.valueOf(beaconGlow));
            p.setProperty("portal_lights", String.valueOf(portalLights));
            p.setProperty("biome_atmospherics", String.valueOf(biomeAtmospherics));
            p.setProperty("nether_red_fog", String.valueOf(netherRedFog));
            p.setProperty("snow_sky_band", String.valueOf(snowSkyBand));
            p.setProperty("global_shadows", String.valueOf(globalShadows));
            p.setProperty("wither_storm_enhanced_ai", String.valueOf(witherStormEnhancedAi));
            p.setProperty("giant_preview_enabled", String.valueOf(giantPreviewEnabled));
            try (OutputStream out = new FileOutputStream(f)) {
                p.store(out, "MCSM Devouring Storms Config Version 10000.0.0-PRE-RELEASE-ALPHA-1-DEVOURING-STORMS-338");
            }
            stamp = f.lastModified();
        } catch (Throwable ignored) {
        }
    }

    public static synchronized void load() {
        File f = file();
        if (loaded) {
            long m = f.lastModified();
            if (m == stamp) return;
            stamp = m;
        } else {
            loaded = true; stamp = f.lastModified();
        }
        try {
            Properties p = new Properties();
            if (f.isFile()) {
                try (InputStream in = new FileInputStream(f)) { p.load(in); }
            } else {
                save();
                return;
            }
            enableTentacleGrab = bool(p, "enable_tentacle_grab", enableTentacleGrab);
            grabIntervalSeconds = dbl(p, "grab_interval_seconds", grabIntervalSeconds);
            witherStormEnhancedAi = bool(p, "wither_storm_enhanced_ai", witherStormEnhancedAi);
            giantPreviewEnabled = bool(p, "giant_preview_enabled", giantPreviewEnabled);
            enableBeaconStorm  = bool(p, "enable_beacon_storm", enableBeaconStorm);
            beaconCooldownSeconds = dbl(p, "beacon_cooldown_seconds", beaconCooldownSeconds);
            enableRiseFx       = bool(p, "enable_rise_fx", enableRiseFx);
            debrisMovementDirection = (int) dbl(p, "debris_movement_direction", debrisMovementDirection);
            forceNativeBlockDebris = bool(p, "force_native_block_debris", forceNativeBlockDebris);
            debrisScaleMultiplier = dbl(p, "debris_scale_multiplier", debrisScaleMultiplier);
            purpleCoilScale = dbl(p, "purple_coil_scale", purpleCoilScale);
            useCustomColors = bool(p, "use_custom_colors", useCustomColors);
            customEyeR = dbl(p, "custom_eye_r", customEyeR);
            customEyeG = dbl(p, "custom_eye_g", customEyeG);
            customEyeB = dbl(p, "custom_eye_b", customEyeB);
            animIdleSpeed = dbl(p, "anim_idle_speed", animIdleSpeed);
            animRoarIntensity = dbl(p, "anim_roar_intensity", animRoarIntensity);
            nightglowBodyOutline = bool(p, "nightglow_body_outline", nightglowBodyOutline);
            nightglowBluishGlow = bool(p, "nightglow_bluish_glow", nightglowBluishGlow);
            stormHaloEnabled = bool(p, "storm_halo_enabled", stormHaloEnabled);
            nativeGlossSheen = bool(p, "native_gloss_sheen", nativeGlossSheen);
            customMeshModel = bool(p, "custom_mesh_model", customMeshModel);
            tracedShadingBody = bool(p, "traced_shading_body", tracedShadingBody);
            ogSunGlow = bool(p, "og_sun_glow", ogSunGlow);
            ogSunGlowStrength = dbl(p, "og_sun_glow_strength", ogSunGlowStrength);
            storyMenuBackdrop = bool(p, "story_menu_backdrop", storyMenuBackdrop);
            menuPanorama = bool(p, "menu_panorama", menuPanorama);
            decayedReality = bool(p, "decayed_reality", decayedReality);
            abandonedCities = bool(p, "abandoned_cities", abandonedCities);
            realityGlitches = bool(p, "reality_glitches", realityGlitches);
            hallucinationIntensity = dbl(p, "hallucination_intensity", hallucinationIntensity);
            blackHoleEvent = bool(p, "black_hole_event", blackHoleEvent);
            blackHoleSeconds = dbl(p, "black_hole_seconds", blackHoleSeconds);
            megaTornadoes = bool(p, "mega_tornadoes", megaTornadoes);
            skyVortexes = bool(p, "sky_vortexes", skyVortexes);
            skyFloorBand = bool(p, "sky_floor_band", skyFloorBand);
            realityCreatures = bool(p, "reality_creatures", realityCreatures);
            storyQuests = bool(p, "story_quests", storyQuests);
            embeddedShaderPack = bool(p, "embedded_shader_pack", embeddedShaderPack);
            // one-shot migration: any config written before this build carries a
            // gate value that turns the storm's own glow off under a shader pack
            if (!bool(p, "shader_pack_gate_migrated", false)) {
                shaderPackGate = true;
                shaderPackGateMigrated = true;
            }
            skyFadeDistance = dbl(p, "sky_fade_distance", skyFadeDistance);
            bossLadder = bool(p, "boss_ladder", bossLadder);
            creatorArms = bool(p, "creator_arms", creatorArms);
            creatorArmScale = dbl(p, "creator_arm_scale", creatorArmScale);
            skyboxEnabled = bool(p, "skybox_enabled", skyboxEnabled);
            skyboxFadeSeconds = dbl(p, "skybox_fade_seconds", skyboxFadeSeconds);
            skyboxSize = dbl(p, "skybox_size", skyboxSize);
            glareBackdrop = bool(p, "glare_backdrop", glareBackdrop);
            glareBackdropSize = dbl(p, "glare_backdrop_size", glareBackdropSize);
            glareBackdropStrength = dbl(p, "glare_backdrop_strength", glareBackdropStrength);
            skyPassCancel = bool(p, "sky_pass_cancel", skyPassCancel);
            stormBlurEnabled = bool(p, "storm_blur_enabled", stormBlurEnabled);
            stormBlurSize = dbl(p, "storm_blur_size", stormBlurSize);
            stormBlurStrength = dbl(p, "storm_blur_strength", stormBlurStrength);
            cinematicBootEnabled = bool(p, "cinematic_boot_enabled", cinematicBootEnabled);
            worldCrackIntro = bool(p, "world_crack_intro", worldCrackIntro);
            nightglowPurpleGlow55 = bool(p, "nightglow_purple_glow55", nightglowPurpleGlow55);
            // BUILD #416 (D.8, phase 5) -- one-time migration, same shape as the
            // shaderPackGate migration above: the purple glint was ON by default
            // in every build up to this one, so a stored `true` means "the old
            // default", not "the player chose it". Flipped off once, then the
            // player's own choice is respected forever.
            if (!bool(p, "purple_glint_migrated", false)) {
                nightglowPurpleGlow55 = false;
                purpleGlintMigrated = true;
            } else {
                purpleGlintMigrated = true;
            }
            storyTerminal = bool(p, "story_terminal", storyTerminal);
            antennaSignals = bool(p, "antenna_signals", antennaSignals);
            antennaSignalSeconds = dbl(p, "antenna_signal_seconds", antennaSignalSeconds);
            antennaOnSpawn = bool(p, "antenna_on_spawn", antennaOnSpawn);
            massgEnabled = bool(p, "massg_enabled", massgEnabled);
            massgUnkillable = bool(p, "massg_unkillable", massgUnkillable);
            massgScale = dbl(p, "massg_scale", massgScale);
            massgHallucinations = bool(p, "massg_hallucinations", massgHallucinations);
            backdropBottomStretch = dbl(p, "backdrop_bottom_stretch", backdropBottomStretch);
            hugeBackCentred = bool(p, "huge_back_centred", hugeBackCentred);
            glareBehindBody = bool(p, "glare_behind_body", glareBehindBody);
            stormGroundPool = bool(p, "storm_ground_pool", stormGroundPool);
            citiesInOverworld = bool(p, "cities_in_overworld", citiesInOverworld);
            nightglowThickBlackGlow = bool(p, "nightglow_thick_black_glow", nightglowThickBlackGlow);
            endFlashesPhase6 = bool(p, "end_flashes_phase6", endFlashesPhase6);
            transparentHorizonWings = bool(p, "transparent_horizon_wings", transparentHorizonWings);
            uiBorderLines = bool(p, "ui_border_lines", uiBorderLines);
            forceMcsmLook = bool(p, "force_mcsm_look", forceMcsmLook);
            forceMcsmWorld = bool(p, "force_mcsm_world", forceMcsmWorld);
            deathCinematic = bool(p, "death_cinematic", deathCinematic);
            ENABLE_EXPERIMENTAL_STORY_MODE_STAGE = bool(p, "experimental_story_mode_stage", ENABLE_EXPERIMENTAL_STORY_MODE_STAGE);
            autoSelectShaderPack = bool(p, "auto_select_shader_pack", autoSelectShaderPack);
            autoStartTowns = bool(p, "auto_start_towns", autoStartTowns);
            phase9RingCubes = (int) dbl(p, "phase9_ring_cubes", phase9RingCubes);
            stormModelScale = dbl(p, "storm_model_scale", stormModelScale);
            stormRings = bool(p, "storm_rings", stormRings);
            storyModeAccurateSunSun = bool(p, "story_mode_accurate_sun", storyModeAccurateSunSun);
            tentacleGirth = dbl(p, "tentacle_girth", tentacleGirth);
            supernovaRings = bool(p, "supernova_rings", supernovaRings);
        } catch (Throwable ignored) {
        }
    }

    private static boolean bool(Properties p, String k, boolean d) {
        String v = p.getProperty(k);
        return v == null ? d : Boolean.parseBoolean(v.trim());
    }

    private static double dbl(Properties p, String k, double d) {
        try { return Double.parseDouble(p.getProperty(k).trim()); } catch (Throwable t) { return d; }
    }

    // ---- re-added on migration from the 7000.0.0-M lineage -----------------
    // Their config file is the superset (the 1.9.19x option set: cinematic UI,
    // skybox, glare backdrop, nightglow, animation controller, colour
    // overrides). These eight belong to our lineage's own code and were
    // referenced by files that are NOT part of that set, so they come back
    // verbatim with their persisted keys.
    public static boolean ENABLE_EXPERIMENTAL_STORY_MODE_STAGE = false;
    public static boolean autoSelectShaderPack = true;
    public static boolean autoStartTowns = false;
    public static int     phase9RingCubes = 10000;
    public static double  stormModelScale = 1.0;
    public static boolean stormRings = true;
    public static boolean storyModeAccurateSunSun = false;
    public static double  tentacleGirth = 2.8;

    private McsmExtrasConfig() {}
}
