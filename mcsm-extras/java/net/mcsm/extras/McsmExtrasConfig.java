package net.mcsm.extras;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * MCSM extras config (Version 7001.0.0-MCSM-CINEMATIC-FINAL).
 * Drives all MCSM additions, atmosphere parameters, debris editor, color customization,
 * animation controller, nightglow halo expansion, and sci-fi UI parameters.
 */
public final class McsmExtrasConfig {
    public static final String BUILD_VERSION = "7001.0.0-MCSM-CINEMATIC-FINAL";

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

    // ---- Nightglow & Silhouette Halo Expansion ---------------------------
    public static boolean nightglowBodyOutline = true; // Outline whole body bottom tail to top of phase 5
    public static boolean nightglowBluishGlow = true; // Bluish halo base
    public static boolean nightglowPurpleGlow55 = true; // Gigantic purple glow for phases 5.1-5.9
    public static boolean nightglowThickBlackGlow = true; // Thick black core glow
    public static double  nightglowRadiusMultiplier = 1.5;

    // ---- End Flashes & Cinematic Death Pass --------------------------------
    public static boolean endFlashesPhase6 = true; // Giant End flashes for Phase 6+
    public static boolean transparentHorizonWings = true; // Transparent giant wings across horizon during death
    public static boolean deathSupernovaImplosion = true;

    // ---- UI & Sci-Fi Control Panel Styling --------------------------------
    public static boolean uiBorderLines = true; // Image 3 pixelated silver border frame
    public static boolean sciFiPanelLayout = true;
    public static double  glareSize = 0.58;
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
    public static boolean shaderPackGate = false;
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
    public static boolean npcWalkAnimations = true;
    public static boolean stormBodySway = true;
    public static boolean infiniteBackGrowth = false;
    public static double infiniteBackGrowthSpeed = 0.10;
    public static boolean embeddedShaderPack = false;

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
            p.setProperty("nightglow_body_outline", String.valueOf(nightglowBodyOutline));
            p.setProperty("nightglow_bluish_glow", String.valueOf(nightglowBluishGlow));
            p.setProperty("nightglow_purple_glow55", String.valueOf(nightglowPurpleGlow55));
            p.setProperty("nightglow_thick_black_glow", String.valueOf(nightglowThickBlackGlow));
            p.setProperty("end_flashes_phase6", String.valueOf(endFlashesPhase6));
            p.setProperty("transparent_horizon_wings", String.valueOf(transparentHorizonWings));
            p.setProperty("ui_border_lines", String.valueOf(uiBorderLines));
            p.setProperty("glare_size", String.valueOf(glareSize));
            p.setProperty("aurora_enabled", String.valueOf(auroraEnabled));
            p.setProperty("death_cinematic", String.valueOf(deathCinematic));
            p.setProperty("supernova_rings", String.valueOf(supernovaRings));
            p.setProperty("smoke_screen", String.valueOf(smokeScreen));
            p.setProperty("purple_sky", String.valueOf(purpleSky));
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
            try (OutputStream out = new FileOutputStream(f)) {
                p.store(out, "MCSM Devouring Storms Config Version 7001.0.0-MCSM-CINEMATIC-FINAL");
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
            nightglowPurpleGlow55 = bool(p, "nightglow_purple_glow55", nightglowPurpleGlow55);
            nightglowThickBlackGlow = bool(p, "nightglow_thick_black_glow", nightglowThickBlackGlow);
            endFlashesPhase6 = bool(p, "end_flashes_phase6", endFlashesPhase6);
            transparentHorizonWings = bool(p, "transparent_horizon_wings", transparentHorizonWings);
            uiBorderLines = bool(p, "ui_border_lines", uiBorderLines);
            forceMcsmLook = bool(p, "force_mcsm_look", forceMcsmLook);
            forceMcsmWorld = bool(p, "force_mcsm_world", forceMcsmWorld);
            deathCinematic = bool(p, "death_cinematic", deathCinematic);
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

    private McsmExtrasConfig() {}
}
