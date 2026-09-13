package net.mcsm.extras;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.config.WitherStormConfigs;
import net.dabicco.witherstormmod.config.WitherStormWorldConfig;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
     * MCSM 1.9.112 -- memory of every value this gate writes, keyed by field.
     *
     * BUILD #371 -- UNFROZEN: the Extras panel no longer re-arms this gate on
     * every toggle (that re-arm was the mechanism silently undoing look
     * presets and base-screen changes -- "presets change nothing, it goes
     * back to normal"). The gate now runs once per session; only the
     * explicit "Re-apply MCSM Look now" button (clearMemory + reset) forces
     * the baseline again.
     *
     * Memory rule (still in force): on a re-run, a field that still holds the
     * value we wrote gets kept; a field anything else has changed since is
     * never touched again.
     */
    private static final Map<String, Object> LAST_SET = new ConcurrentHashMap<>();

    /** Forget every recorded value; the next gate run forces the look again. */
    public static void clearMemory() {
        LAST_SET.clear();
    }

    private static String memKey(Class<?> owner, Object instance, String name) {
        return owner.getName() + ":" + name
                + (instance == null ? ":static" : "@" + System.identityHashCode(instance));
    }

    private static double readNum(Field f, Object instance) throws IllegalAccessException {
        Class<?> t = f.getType();
        if (t == int.class) return f.getInt(instance);
        if (t == long.class) return f.getLong(instance);
        if (t == float.class) return f.getFloat(instance);
        return f.getDouble(instance);
    }

    private static double writeNum(Field f, Object instance, double v) throws IllegalAccessException {
        Class<?> t = f.getType();
        if (t == int.class) { int nv = (int) Math.round(v); f.setInt(instance, nv); return nv; }
        if (t == long.class) { long nv = Math.round(v); f.setLong(instance, nv); return nv; }
        if (t == float.class) { float nv = (float) v; f.setFloat(instance, nv); return nv; }
        f.setDouble(instance, v);
        return v;
    }

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
            // BUILD #371 — OG "SAGE MCSM" 3D sun glow. Restored to the base
            // default (full strength 2.2) instead of the old 0.45 ceiling that
            // dimmed it. ON by default; the Extras panel toggle/slider can
            // turn it off or re-strength it (applied directly, no re-run).
            if (McsmExtrasConfig.ogSunGlow) {
                changed += setBool(c, "sunGlow", true);
                changed += setExactNum(c, null, "sunGlowStrength", McsmExtrasConfig.ogSunGlowStrength);
            } else {
                // explicit user choice: the sun glow stays off across sessions
                changed += setBool(c, "sunGlow", false);
            }
            changed += setBool(c, "blackGlare", true);
            changed += setBool(c, "glareEjecta", false);
            changed += setBool(c, "headEyeGlow", true);
            changed += setBool(c, "devourerDebrisGlow", false);

            // ---- ground shadows for trees and mobs (user request) ---------
            changed += setBool(c, "trailerShadows", true);
            changed += setBool(c, "stormShadow", true);
            changed += setBool(c, "stormShadowTerrain", true);
            changed += setBool(c, "stormShadowSoftEdge", true);
            changed += setBool(c, "stormShadowHeightmap", false);

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
            changed += ceilingField(c, null, "debrisDustParticles", 0.0);
            changed += ceilingField(c, null, "debrisAmount", 0.0);
            changed += floorField(c, null, "volumetricFogDensity", 0.6);
            changed += floorField(c, null, "stormGlowStrength", 1.0);
            // (sunGlowStrength is no longer ceilinged — see the OG sun block above)
            changed += ceilingField(c, null, "blackGlareStrength", 0.65);
            changed += floorField(c, null, "stormShadowStrength", 1.0);
            changed += floorField(c, null, "glowStrength", 1.0);
            // Full-res HDR storm bloom is the native-memory pressure point in
            // the user's Iris/Sodium logs. Teeth/eyes stay emissive cyan through
            // their render pass, but the expensive full-screen bloom buffer is
            // off by default for stability.
            changed += ceilingField(c, null, "bloomStrength", 0.0);
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
            String key = memKey(owner, null, name);
            boolean cur = f.getBoolean(null);
            Object prev = LAST_SET.get(key);
            if (prev instanceof Boolean b && cur != b) {
                return 0;   // changed after us (preset/player): leave it alone
            }
            f.setBoolean(null, value);
            LAST_SET.put(key, value);
            return 1;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** Raise static or instance numeric fields without assuming int/double type. */
    private static int floorField(Class<?> owner, Object instance, String name, double min) {
        try {
            Field f = owner.getField(name);
            String key = memKey(owner, instance, name);
            double cur = readNum(f, instance);
            Object prev = LAST_SET.get(key);
            if (prev instanceof Double d && Math.abs(cur - d) > 1e-9) {
                return 0;   // changed after us (preset/player): respect it
            }
            double nv = writeNum(f, instance, Math.max(cur, min));
            LAST_SET.put(key, nv);
            return 1;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /**
     * Set a static or instance numeric field to an EXACT value (BUILD #371).
     * Unlike floor/ceiling, this writes the target directly so the OG sun
     * glow lands at its authentic base default (2.2). LAST_SET memory applies:
     * a value the user changes afterwards is respected on later passes.
     */
    private static int setExactNum(Class<?> owner, Object instance, String name, double target) {
        try {
            Field f = owner.getField(name);
            String key = memKey(owner, instance, name);
            double cur = readNum(f, instance);
            Object prev = LAST_SET.get(key);
            if (prev instanceof Double d && Math.abs(cur - d) > 1e-9) {
                return 0;   // changed after us (user/preset): respect it
            }
            double nv = writeNum(f, instance, target);
            LAST_SET.put(key, nv);
            return 1;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** Intervals count DOWN in desirability: a smaller positive number fires sooner. */
    private static int ceilingField(Class<?> owner, Object instance, String name, double max) {
        try {
            Field f = owner.getField(name);
            String key = memKey(owner, instance, name);
            double cur = readNum(f, instance);
            Object prev = LAST_SET.get(key);
            if (prev instanceof Double d && Math.abs(cur - d) > 1e-9) {
                return 0;
            }
            double nv = writeNum(f, instance, Math.min(cur, max));
            LAST_SET.put(key, nv);
            return 1;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private static int ceilInterval(Class<?> owner, Object instance, String name, int max) {
        try {
            Field f = owner.getField(name);
            String key = memKey(owner, instance, name);
            double cur = readNum(f, instance);
            Object prev = LAST_SET.get(key);
            if (prev instanceof Double d && Math.abs(cur - d) > 1e-9) {
                return 0;   // changed after us: respect it
            }
            double want = (cur <= 0.0 || cur > max) ? max : cur;
            double nv = writeNum(f, instance, want);
            LAST_SET.put(key, nv);
            return 1;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** Called on session teardown so a world change re-applies the gates. */
    public static synchronized void reset() {
        clientDone = false;
        worldDone = false;
    }

    // ---------------------------------------------------------------------
    // BUILD #371 — UNFREEZE: direct single-field apply (no gate re-run).
    //
    // The Extras panel no longer calls reset() on every toggle: re-arming the
    // whole force-look pass on each click was the mechanism silently wiping
    // base-screen changes and look presets ("it goes back to normal").
    // Controls that map to a base DabyWSClientConfig field now apply that ONE
    // field directly. These are explicit user actions, so the value is
    // recorded in LAST_SET: a normal gate pass respects it afterwards, and
    // only the explicit "Re-apply MCSM Look now" button (clearMemory+reset)
    // forces the baseline again.
    // ---------------------------------------------------------------------

    /** Read a base client boolean (default if the field is gone/renamed). */
    public static boolean clientBoolGet(String name, boolean dflt) {
        try {
            return DabyWSClientConfig.class.getField(name).getBoolean(null);
        } catch (Throwable t) {
            return dflt;
        }
    }

    /** Read a base client number (default if the field is gone/renamed). */
    public static double clientNumGet(String name, double dflt) {
        try {
            Field f = DabyWSClientConfig.class.getField(name);
            Class<?> t = f.getType();
            if (t == int.class) return f.getInt(null);
            if (t == long.class) return f.getLong(null);
            if (t == float.class) return f.getFloat(null);
            return f.getDouble(null);
        } catch (Throwable t) {
            return dflt;
        }
    }

    /** Write ONE base client boolean directly (explicit user action). */
    public static void clientBool(String name, boolean value) {
        try {
            DabyWSClientConfig.class.getField(name).setBoolean(null, value);
            LAST_SET.put(memKey(DabyWSClientConfig.class, null, name), value);
        } catch (Throwable ignored) {
        }
    }

    /** Write ONE base client number directly (explicit user action). */
    public static void clientNum(String name, double value) {
        try {
            Field f = DabyWSClientConfig.class.getField(name);
            double nv = writeNum(f, null, value);
            LAST_SET.put(memKey(DabyWSClientConfig.class, null, name), nv);
        } catch (Throwable ignored) {
        }
    }

    private McsmGate() {}
}
