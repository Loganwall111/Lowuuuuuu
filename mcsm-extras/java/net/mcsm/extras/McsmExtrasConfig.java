package net.mcsm.extras;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * MCSM extras config (Version 7000.0.0-MCSM-CINEMATIC-FINAL).
 * Drives all MCSM additions, atmosphere parameters, debris editor, color customization,
 * animation controller, nightglow halo expansion, and sci-fi UI parameters.
 */
public final class McsmExtrasConfig {
    public static final String BUILD_VERSION = "7000.0.0-MCSM-CINEMATIC-FINAL.396";

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
    /** BUILD #390 -- the cosmic-blue spotlight nodes under the storm body
     *  (#4D4DFF), re-drawn by McsmStormBlob because the base mod's own lower
     *  light is lavender-white and lives in the sealed jar. */
    public static boolean cosmicSpotlights = true;
    /** Dust trails when the storm sweeps blocks. */
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
            p.setProperty("cosmic_spotlights", String.valueOf(cosmicSpotlights));
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
                p.store(out, "MCSM Devouring Storms Config Version 7000.0.0-MCSM-CINEMATIC-FINAL");
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
            spiralCounterClockwise = bool(p, "spiral_counter_clockwise", spiralCounterClockwise);
            enableBeaconBlock = bool(p, "enable_beacon_block", enableBeaconBlock);
            String cv = p.getProperty("config_version");
            ogCemModels        = bool(p, "og_cem_models", ogCemModels);
            if (cv == null || !BUILD_VERSION.equals(cv.trim())) {
                ogCemModels = true;
            }
            smudgeScale        = dbl(p, "smudge_scale", smudgeScale);
            glareSize          = dbl(p, "glare_size", glareSize);
            if ((cv == null || !BUILD_VERSION.equals(cv.trim())) && Math.abs(glareSize - 1.18) < 0.001) {
                glareSize = 0.58;
            }
            nightSkyOpacity = dbl(p, "night_sky_opacity", nightSkyOpacity);
            if ((cv == null || !BUILD_VERSION.equals(cv.trim())) && nightSkyOpacity > 0.85) {
                // 1.9.200: old defaults over-darkened/re-tinted normal night.
                nightSkyOpacity = 0.42;
            }
            phase55Threshold = dbl(p, "phase_55_threshold", phase55Threshold);
            phase5_9PinkIntensity = dbl(p, "phase_5_9_pink_intensity", phase5_9PinkIntensity);
            glareAnimPhase = dbl(p, "glare_anim_phase", glareAnimPhase);
            bodyAnimPulse = dbl(p, "body_anim_pulse", bodyAnimPulse);
            glareAnimIntensity = dbl(p, "glare_anim_intensity", glareAnimIntensity);
            glareNonEuclidean = bool(p, "glare_non_euclidean", glareNonEuclidean);
            cloudAlpha = dbl(p, "cloud_alpha", cloudAlpha);
            cloudSpeed = dbl(p, "cloud_speed", cloudSpeed);
            precipitationIntensity = dbl(p, "precipitation_intensity", precipitationIntensity);
            auroraEnabled      = bool(p, "aurora_enabled", auroraEnabled);
            shaderPackGate     = bool(p, "shader_pack_gate", shaderPackGate);
            if (cv == null || !BUILD_VERSION.equals(cv.trim())) {
                // 1.9.200: do not force custom storm pipelines while Iris is
                // running an external shaderpack; Iris reports those custom
                // programs missing from its override list and memory pressure
                // climbs. No-shader play is unaffected because Iris reports
                // inactive naturally.
                shaderPackGate = false;
            }
            deathCinematic     = bool(p, "death_cinematic", deathCinematic);
            supernovaRings     = bool(p, "supernova_rings", supernovaRings);
            smokeScreen        = bool(p, "smoke_screen", smokeScreen);
            purpleSky          = bool(p, "purple_sky", purpleSky);
            cosmicSpotlights = bool(p, "cosmic_spotlights", cosmicSpotlights);
            dustWaves          = bool(p, "dust_waves", dustWaves);
            realityTear        = bool(p, "reality_tear", realityTear);
            obliterateFlash    = bool(p, "obliterate_flash", obliterateFlash);
            obliterateKick     = bool(p, "obliterate_kick", obliterateKick);
            forceMcsmLook      = bool(p, "force_mcsm_look", forceMcsmLook);
            forceMcsmWorld     = bool(p, "force_mcsm_world", forceMcsmWorld);
            commandWire        = bool(p, "command_wire", commandWire);
            mcsmInstructions   = bool(p, "mcsm_instructions", mcsmInstructions);
            beaconGlow         = bool(p, "beacon_glow", beaconGlow);
            portalLights       = bool(p, "portal_lights", portalLights);
            biomeAtmospherics  = bool(p, "biome_atmospherics", biomeAtmospherics);
            netherRedFog       = bool(p, "nether_red_fog", netherRedFog);
            snowSkyBand        = bool(p, "snow_sky_band", snowSkyBand);
            auroraRibbons      = bool(p, "aurora_ribbons", auroraRibbons);
            twinklingStars     = bool(p, "twinkling_stars", twinklingStars);
            comets             = bool(p, "comets", comets);
            coloredSparkles    = bool(p, "colored_sparkles", coloredSparkles);
            waterGodRays       = bool(p, "water_god_rays", waterGodRays);
            endSkyVortex       = bool(p, "end_sky_vortex", endSkyVortex);
            globalShadows      = bool(p, "global_shadows", globalShadows);
            witherStormEnhancedAi = bool(p, "wither_storm_enhanced_ai", witherStormEnhancedAi);
            npcWalkAnimations  = bool(p, "npc_walk_animations", npcWalkAnimations);
            stormBodySway      = bool(p, "storm_body_sway", stormBodySway);
            infiniteBackGrowth = bool(p, "infinite_back_growth", infiniteBackGrowth);
            infiniteBackGrowthSpeed = dbl(p, "infinite_back_growth_speed", infiniteBackGrowthSpeed);
            embeddedShaderPack = bool(p, "embedded_shader_pack", embeddedShaderPack);
            if (cv == null || !BUILD_VERSION.equals(cv.trim())) {
                // 1.9.196 migration: old configs wrote embedded_shader_pack=true,
                // which kept auto-selecting the heavy Iris pack and caused
                // GL_OUT_OF_MEMORY/native AllocateHeap crashes. Flip only on
                // version migration; the player can opt back in afterwards.
                embeddedShaderPack = false;
                save();
            }
            // ---- master 7000.0.0 cinematic keys (debris physics, nightglow) --
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
        } catch (Throwable t) {
            // stay on defaults; never crash the game over a config file
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
