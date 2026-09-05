package net.mcsm.extras;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.config.WitherStormConfigs;
import net.dabicco.witherstormmod.config.WitherStormWorldConfig;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;

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
            McsmDiag.say("MCSM client gate disabled by mcsm_storm_extras.properties");
            return;
        }
        int changed = 0;
        try {
            Class<?> c = DabyWSClientConfig.class;

            // ---- storm body + sky -----------------------------------------
            changed += setBool(c, "distantStorms", true);
            changed += setBool(c, "distantFog", true);
            changed += setBool(c, "customSkyboxes", true);
            changed += setBool(c, "cloudDeckLayer", true);
            changed += setBool(c, "regionalBiomeFog", true);
            changed += setBool(c, "phaseAnim", true);
            changed += setBool(c, "filledSubphases", true);
            changed += setBool(c, "scaledSubphaseGrowth", true);
            changed += setBool(c, "tentaclePhysics", true);
            changed += setBool(c, "optimizeDistantAnimations", true);
            changed += setBool(c, "flatbackFlipFix", true);
            // Obsidian Gloss is the mod's built-in OG/MCSM texture set. The
            // user expects Force MCSM Look to make the body/teeth/command-block
            // textures stop falling back to the Classic orange/plain skin.
            changed += floorField(c, null, "stormSkin", 1.0);

            // ---- the halo / glare the user has been chasing ----------------
            changed += setBool(c, "sunGlow", true);
            changed += setBool(c, "blackGlare", true);
            changed += setBool(c, "glareEjecta", true);
            changed += setBool(c, "headEyeGlow", true);
            changed += setBool(c, "devourerDebrisGlow", true);

            // ---- ground shadows for trees and mobs (user request) ---------
            changed += setBool(c, "trailerShadows", true);
            changed += setBool(c, "stormShadow", true);
            changed += setBool(c, "stormShadowTerrain", true);
            changed += setBool(c, "stormShadowSoftEdge", true);
            changed += setBool(c, "stormShadowHeightmap", true);

            // ---- screen: smoke screen, tremor, sickness, glitch -----------
            changed += setBool(c, "stormProximityVignette", true);
            changed += setBool(c, "sicknessVeinOverlay", true);
            changed += setBool(c, "groundShakingTremors", true);
            changed += setBool(c, "dynamicScreenShake", true);
            changed += setBool(c, "purpleLightningSparks", true);

            // ---- presentation ---------------------------------------------
            changed += setBool(c, "storyModeBossbar", true);
            changed += setBool(c, "storyModeTitleScreen", true);
            changed += setBool(c, "stormAmbience", true);
            changed += setBool(c, "beamHum", true);
            changed += setBool(c, "beamDeactivateSound", true);
            changed += setBool(c, "infectedMobSound", true);

            // ---- numeric floors (raise only, never lower) ------------------
            changed += floorField(c, null, "vignetteIntensity", 0.85);
            changed += floorField(c, null, "sicknessVeinIntensity", 0.7);
            changed += floorField(c, null, "screenTremorIntensity", 0.8);
            changed += floorField(c, null, "chromaticGlitchStrength", 0.35);
            changed += floorField(c, null, "debrisDustParticles", 1.0);
            changed += floorField(c, null, "debrisAmount", 1.0);
            changed += floorField(c, null, "volumetricFogDensity", 0.6);
            changed += floorField(c, null, "stormGlowStrength", 1.0);
            changed += floorField(c, null, "sunGlowStrength", 1.0);
            changed += floorField(c, null, "blackGlareStrength", 1.0);
            changed += floorField(c, null, "stormShadowStrength", 1.0);
            changed += floorField(c, null, "glowStrength", 1.0);
            changed += floorField(c, null, "ambienceVolume", 0.8);
            changed += floorField(c, null, "headSoundsVolume", 0.8);
            changed += floorField(c, null, "beamSoundsVolume", 0.8);
            changed += floorField(c, null, "infectedMobSoundVolume", 0.8);
            changed += floorField(c, null, "phaseAnimStrength", 1.0);
            changed += floorField(c, null, "mirrorBackDetail", 1.0);
            McsmDiag.say("MCSM client gate opened: " + changed + " config fields raised/enabled");
        } catch (Throwable t) {
            // Only a class-level failure should land here. Individual renamed
            // fields are handled by the helpers below so one missing option can
            // no longer block every later MCSM visual.
            McsmDiag.say("MCSM client gate failed before field loop: " + t);
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
            Class<?> c = cfg.getClass();
            int changed = 0;

            // ---- reality tear: the storm rips buildings out of the world ---
            changed += floorField(c, cfg, "buildingDestruction", 1.0);
            changed += floorField(c, cfg, "buildingTearRadius", 28.0);
            changed += ceilInterval(c, cfg, "buildingTearInterval", 20);

            // ---- shockwaves you can see ------------------------------------
            changed += floorField(c, cfg, "groundShockwaveParticles", 600.0);

            // ---- structures: the storm raids them --------------------------
            changed += floorField(c, cfg, "structureRaid", 1.0);
            changed += ceilInterval(c, cfg, "structureRaidInterval", 5);
            changed += floorField(c, cfg, "structureRaidRadius", 96.0);
            changed += floorField(c, cfg, "structureTearClusters", 8.0);

            // ---- corruption: wither sickness + withered mobs ---------------
            changed += floorField(c, cfg, "witherSickness", 1.0);
            changed += floorField(c, cfg, "witheredMobs", 1.0);
            changed += floorField(c, cfg, "witheredMax", 32.0);
            changed += floorField(c, cfg, "witheredMaxCaves", 16.0);

            // ---- the ground itself reacts ----------------------------------
            changed += floorField(c, cfg, "caveRumble", 1.0);
            McsmDiag.say("MCSM world gate opened: " + changed + " world fields raised/enabled");
        } catch (Throwable t) {
            McsmDiag.say("MCSM world gate failed before field loop: " + t);
        }
    }

    private static int setBool(Class<?> owner, String name, boolean value) {
        try {
            Field f = owner.getField(name);
            f.setBoolean(null, value);
            return 1;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** Raise static or instance numeric fields without assuming int/double type. */
    private static int floorField(Class<?> owner, Object instance, String name, double min) {
        try {
            Field f = owner.getField(name);
            Class<?> t = f.getType();
            if (t == int.class) {
                int old = f.getInt(instance);
                int nv = Math.max(old, (int) Math.round(min));
                if (nv != old) f.setInt(instance, nv);
                return 1;
            }
            if (t == float.class) {
                float old = f.getFloat(instance);
                float nv = Math.max(old, (float) min);
                if (nv != old) f.setFloat(instance, nv);
                return 1;
            }
            if (t == double.class) {
                double old = f.getDouble(instance);
                double nv = Math.max(old, min);
                if (nv != old) f.setDouble(instance, nv);
                return 1;
            }
            if (t == long.class) {
                long old = f.getLong(instance);
                long nv = Math.max(old, (long) Math.round(min));
                if (nv != old) f.setLong(instance, nv);
                return 1;
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    /** Intervals count DOWN in desirability: a smaller positive number fires sooner. */
    private static int ceilInterval(Class<?> owner, Object instance, String name, int max) {
        try {
            Field f = owner.getField(name);
            Class<?> t = f.getType();
            if (t == int.class) {
                int old = f.getInt(instance);
                int nv = (old <= 0 || old > max) ? max : old;
                if (nv != old) f.setInt(instance, nv);
                return 1;
            }
            if (t == double.class) {
                double old = f.getDouble(instance);
                double nv = (old <= 0.0 || old > max) ? max : old;
                if (nv != old) f.setDouble(instance, nv);
                return 1;
            }
            if (t == float.class) {
                float old = f.getFloat(instance);
                float nv = (old <= 0.0F || old > max) ? (float) max : old;
                if (nv != old) f.setFloat(instance, nv);
                return 1;
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    /** Called on session teardown so a world change re-applies the gates. */
    public static synchronized void reset() {
        clientDone = false;
        worldDone = false;
    }

    private McsmGate() {}
}
