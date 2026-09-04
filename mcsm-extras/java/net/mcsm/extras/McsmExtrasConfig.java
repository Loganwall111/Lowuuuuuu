package net.mcsm.extras;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * MCSM extras config. Lives beside the mod's own config (their in-game
 * config screen + /dabyws still drive the mod itself; this file only drives
 * the MCSM additions: tentacle grab cadence, beacon storm path, rise fx and
 * the counterclockwise spiral pin). Written with defaults on first launch.
 */
public final class McsmExtrasConfig {
    public static boolean enableTentacleGrab = true;
    public static double  grabIntervalSeconds = 11.0;
    public static boolean enableBeaconStorm = true;
    public static double  beaconCooldownSeconds = 30.0;
    public static boolean enableRiseFx = true;
    public static boolean spiralCounterClockwise = true;
    public static boolean enableBeaconBlock = true;

    private static boolean loaded = false;
    private static long stamp = -1L;

    private static File file() {
        return new File(new File(System.getProperty("user.dir", "."), "config"), "mcsm_storm_extras.properties");
    }

    /** Write current values back (used by the config-screen rows). */
    public static synchronized void save() {
        try {
            File f = file();
            f.getParentFile().mkdirs();
            Properties p = new Properties();
            p.setProperty("enable_tentacle_grab", String.valueOf(enableTentacleGrab));
            p.setProperty("grab_interval_seconds", String.valueOf(grabIntervalSeconds));
            p.setProperty("enable_beacon_storm", String.valueOf(enableBeaconStorm));
            p.setProperty("beacon_cooldown_seconds", String.valueOf(beaconCooldownSeconds));
            p.setProperty("enable_rise_fx", String.valueOf(enableRiseFx));
            p.setProperty("spiral_counter_clockwise", String.valueOf(spiralCounterClockwise));
            p.setProperty("enable_beacon_block", String.valueOf(enableBeaconBlock));
            try (OutputStream out = new FileOutputStream(f)) {
                p.store(out, "MCSM - storm gameplay patches (grab, beacon, rise fx, spiral)");
            }
            stamp = f.lastModified();
        } catch (Throwable t) {
            // ignore; file stays as-is
        }
    }

    /** Cheap stat per call; reloads whenever the file is edited in game. */
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
                f.getParentFile().mkdirs();
                p.setProperty("enable_tentacle_grab", "true");
                p.setProperty("grab_interval_seconds", "11.0");
                p.setProperty("enable_beacon_storm", "true");
                p.setProperty("beacon_cooldown_seconds", "30.0");
                p.setProperty("enable_rise_fx", "true");
                p.setProperty("spiral_counter_clockwise", "true");
                p.setProperty("enable_beacon_block", "true");
                try (OutputStream out = new FileOutputStream(f)) {
                    p.store(out, "MCSM - storm gameplay patches (grab, beacon, rise fx, spiral)");
                }
            }
            enableTentacleGrab = bool(p, "enable_tentacle_grab", enableTentacleGrab);
            grabIntervalSeconds = dbl(p, "grab_interval_seconds", grabIntervalSeconds);
            enableBeaconStorm  = bool(p, "enable_beacon_storm", enableBeaconStorm);
            beaconCooldownSeconds = dbl(p, "beacon_cooldown_seconds", beaconCooldownSeconds);
            enableRiseFx       = bool(p, "enable_rise_fx", enableRiseFx);
            spiralCounterClockwise = bool(p, "spiral_counter_clockwise", spiralCounterClockwise);
            enableBeaconBlock = bool(p, "enable_beacon_block", enableBeaconBlock);
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
