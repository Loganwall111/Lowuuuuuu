package net.mcsm.extras;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.config.WitherStormConfigs;
import net.dabicco.witherstormmod.config.WitherStormWorldConfig;
import net.minecraft.world.level.Level;

/**
 * MCSM 1.9.100 -- the "it's already written, it's just switched off" gate.
 *
 * A whole-jar invoke scan (glslcheck/whocalls.py) proved the features the user
 * reports as MISSING are not missing at all:
 *
 *     die()                 -> deathBlast(ServerLevel)          (death shockwave)
 *     addSubGrowth(...)     -> phaseUpShockwave(ServerLevel)    (phase shockwave)
 *     aiStep(...)           -> tickAmbientBuildingTear(...)     (reality tear / corruption)
 *
 * and DabyWSClientConfig exposes 335 public static non-final fields holding the
 * look: trailerShadows (ground shadows for terrain and mobs),
 * stormProximityVignette (the smoke screen), cloudDeckLayer, customSkyboxes,
 * purpleLightningSparks, sunGlow, blackGlare, stormShadowTerrain and so on.
 *
 * So the work is not to reimplement them -- it is to open the gates. This runs
 * ONCE per session (a static latch, not per frame), flips the MCSM look on, and
 * then never touches the config again, so anything the player changes in the
 * mod's own config screen sticks for the rest of the session.
 *
 * Booleans are only ever forced ON. Numeric values are only RAISED to a floor,
 * never lowered, so a player who already turned something up keeps their value.
 *
 * Both halves are wrapped in a blanket catch: a renamed field after a mod update
 * must cost a visual, never a crash.
 */
public final class McsmGate {

    private static boolean clientDone = false;
    private static boolean worldDone = false;

    /**
     * Client-side: force the Story Mode look on. Called from the frame driver
     * (McsmGradientTickPatch); the latch makes it a no-op after the first frame.
     */
    public static synchronized void openClient() {
        if (clientDone) {
            return;
        }
        McsmExtrasConfig.load();
        clientDone = true;
        if (!McsmExtrasConfig.forceMcsmLook) {
            return;
        }
        try {
            // ---- storm body + sky -----------------------------------------
            DabyWSClientConfig.distantStorms = true;
            DabyWSClientConfig.distantFog = true;
            DabyWSClientConfig.customSkyboxes = true;
            DabyWSClientConfig.cloudDeckLayer = true;
            DabyWSClientConfig.regionalBiomeFog = true;
            DabyWSClientConfig.phaseAnim = true;
            DabyWSClientConfig.filledSubphases = true;
            DabyWSClientConfig.scaledSubphaseGrowth = true;
            DabyWSClientConfig.tentaclePhysics = true;
            DabyWSClientConfig.optimizeDistantAnimations = true;
            DabyWSClientConfig.flatbackFlipFix = true;

            // ---- the halo / glare the user has been chasing ----------------
            DabyWSClientConfig.sunGlow = true;
            DabyWSClientConfig.blackGlare = true;
            DabyWSClientConfig.glareEjecta = true;
            DabyWSClientConfig.headEyeGlow = true;
            DabyWSClientConfig.devourerDebrisGlow = true;

            // ---- ground shadows for trees and mobs (user request) ---------
            DabyWSClientConfig.trailerShadows = true;
            DabyWSClientConfig.stormShadow = true;
            DabyWSClientConfig.stormShadowTerrain = true;
            DabyWSClientConfig.stormShadowSoftEdge = true;
            DabyWSClientConfig.stormShadowHeightmap = true;

            // ---- screen: smoke screen, tremor, sickness, glitch -----------
            DabyWSClientConfig.stormProximityVignette = true;
            DabyWSClientConfig.sicknessVeinOverlay = true;
            DabyWSClientConfig.groundShakingTremors = true;
            DabyWSClientConfig.dynamicScreenShake = true;
            DabyWSClientConfig.purpleLightningSparks = true;

            // ---- presentation ---------------------------------------------
            DabyWSClientConfig.storyModeBossbar = true;
            DabyWSClientConfig.storyModeTitleScreen = true;
            DabyWSClientConfig.stormAmbience = true;
            DabyWSClientConfig.beamHum = true;
            DabyWSClientConfig.beamDeactivateSound = true;
            DabyWSClientConfig.infectedMobSound = true;

            // ---- numeric floors (raise only, never lower) ------------------
            DabyWSClientConfig.vignetteIntensity = floor(DabyWSClientConfig.vignetteIntensity, 0.85);
            DabyWSClientConfig.sicknessVeinIntensity = floor(DabyWSClientConfig.sicknessVeinIntensity, 0.7);
            DabyWSClientConfig.screenTremorIntensity = floor(DabyWSClientConfig.screenTremorIntensity, 0.8);
            DabyWSClientConfig.chromaticGlitchStrength = floor(DabyWSClientConfig.chromaticGlitchStrength, 0.35);
            DabyWSClientConfig.debrisDustParticles = floor(DabyWSClientConfig.debrisDustParticles, 1.0);
            DabyWSClientConfig.debrisAmount = floor(DabyWSClientConfig.debrisAmount, 1.0);
            DabyWSClientConfig.volumetricFogDensity = floor(DabyWSClientConfig.volumetricFogDensity, 0.6);
            DabyWSClientConfig.stormGlowStrength = floor(DabyWSClientConfig.stormGlowStrength, 1.0);
            DabyWSClientConfig.sunGlowStrength = floor(DabyWSClientConfig.sunGlowStrength, 1.0);
            DabyWSClientConfig.blackGlareStrength = floor(DabyWSClientConfig.blackGlareStrength, 1.0);
            DabyWSClientConfig.stormShadowStrength = floor(DabyWSClientConfig.stormShadowStrength, 1.0);
            DabyWSClientConfig.glowStrength = floor(DabyWSClientConfig.glowStrength, 1.0);
            DabyWSClientConfig.ambienceVolume = floor(DabyWSClientConfig.ambienceVolume, 0.8);
            DabyWSClientConfig.headSoundsVolume = floor(DabyWSClientConfig.headSoundsVolume, 0.8);
            DabyWSClientConfig.beamSoundsVolume = floor(DabyWSClientConfig.beamSoundsVolume, 0.8);
            DabyWSClientConfig.infectedMobSoundVolume = floor(DabyWSClientConfig.infectedMobSoundVolume, 0.8);
            DabyWSClientConfig.phaseAnimStrength = floor(DabyWSClientConfig.phaseAnimStrength, 1.0);
            DabyWSClientConfig.mirrorBackDetail = floor(DabyWSClientConfig.mirrorBackDetail, 1.0);
        } catch (Throwable ignored) {
            // A renamed field after a mod update costs a visual, never a crash.
        }
    }

    /**
     * Server-side: force destruction, the tear, corruption and shockwaves on.
     * Called from the storm's tick (McsmStormGrabPatch); latched per session.
     */
    public static synchronized void openWorld(Level level) {
        if (worldDone || level == null) {
            return;
        }
        McsmExtrasConfig.load();
        if (!McsmExtrasConfig.forceMcsmWorld) {
            worldDone = true;
            return;
        }
        try {
            WitherStormWorldConfig cfg = WitherStormConfigs.get(level);
            if (cfg == null) {
                return;
            }
            worldDone = true;

            // ---- reality tear: the storm rips buildings out of the world ---
            cfg.buildingDestruction = Math.max(cfg.buildingDestruction, 1);
            cfg.buildingTearRadius = floor(cfg.buildingTearRadius, 28.0);
            cfg.buildingTearInterval = lowerInterval(cfg.buildingTearInterval, 20);

            // ---- shockwaves you can see ------------------------------------
            cfg.groundShockwaveParticles = Math.max(cfg.groundShockwaveParticles, 600);

            // ---- structures: the storm raids them --------------------------
            cfg.structureRaid = Math.max(cfg.structureRaid, 1);
            cfg.structureRaidInterval = lowerInterval(cfg.structureRaidInterval, 5);
            cfg.structureRaidRadius = floor(cfg.structureRaidRadius, 96.0);
            cfg.structureTearClusters = Math.max(cfg.structureTearClusters, 8);

            // ---- corruption: wither sickness + withered mobs ---------------
            cfg.witherSickness = Math.max(cfg.witherSickness, 1);
            cfg.witheredMobs = Math.max(cfg.witheredMobs, 1);
            cfg.witheredMax = Math.max(cfg.witheredMax, 32);
            cfg.witheredMaxCaves = Math.max(cfg.witheredMaxCaves, 16);

            // ---- the ground itself reacts ----------------------------------
            cfg.caveRumble = Math.max(cfg.caveRumble, 1);
        } catch (Throwable ignored) {
            // World config is saved data; if its shape changed, skip quietly.
        }
    }

    private static double floor(double v, double min) {
        return v < min ? min : v;
    }

    /** Intervals count DOWN in desirability: a smaller number fires sooner. */
    private static int lowerInterval(int v, int max) {
        return (v <= 0 || v > max) ? max : v;
    }

    /** Called on session teardown so a world change re-applies the gates. */
    public static synchronized void reset() {
        clientDone = false;
        worldDone = false;
    }

    private McsmGate() {}
}
