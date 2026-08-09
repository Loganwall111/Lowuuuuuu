package com.rewritten.devouringstorms.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/**
 * DEVOURING STORMS config — config/devouring-storms.properties.
 * Plain-text, no libraries: edit values, restart (or let the lazy loader re-read on change).
 * Missing file is written with all defaults + comments on first load.
 *
 * Every gameplay/visual toggle the storm consults lives here.
 */
public final class DevouringConfig {

    private static final String[] DEFAULTS = {
        "# == DEVOURING STORMS configuration ==",
        "# visual layer intensity: 0.0 (off) .. 1.0 (full analog-horror)",
        "overlay_intensity=1.0",
        "# phase fog ladder (teal sky at Signal, bruised at Devourer, pink at the Bowels...)",
        "fog_ladder=true",
        "# YouTube-style title cards when a storm changes phase",
        "storm_title_cards=true",
        "# the Watcher's paranoia: heartbeat vignette, blind frames, afterimage strips",
        "watcher_paranoia=true",
        "# the phase-5.5 rupture cinematic (split, pour, rise, shockwave)",
        "bowels_cinematic=true",
        "# orbiting debris rings around the storm",
        "debris_rings=true",
        "# ground-shaking earthquakes under SUNDERER and up",
        "earthquakes=true",
        "# the plague: mobs near the storm / in the realm convert into infected things",
        "infection=true",
        "# the storm never stops growing while GENESIS lives",
        "infinite_growth=true",
        "# void maw black holes wander the fray and the wastes",
        "void_maw=true",
        "# the belly: flying into the open bowels takes you INSIDE the storm",
        "stomach_interior=true",
        "# the crater vision / VHS jukebox / Monstrosity broadcast tape overlay",
        "vhs_overlay=true",
        "# the creator over the abyss. spawn it with /summon devouring_storms:creator",
        "creator=true",
        "# the monstrosity's colourful world-glitch spread",
        "monstrosity_glitch=true",
        "# the forger's tentacle rain and rift seams",
        "forger=true",
        "# planets + rocket key ring (aurth, volmar, nexus)",
        "planets=true",
    };

    private static volatile Properties props = null;
    private static volatile long loadedMtime = -1L;

    private DevouringConfig() {
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("devouring-storms.properties");
    }

    public static Properties get() {
        try {
            Path p = path();
            long mtime = Files.exists(p) ? Files.getLastModifiedTime(p).toMillis() : -1L;
            if (props == null || mtime != loadedMtime) {
                Properties next = new Properties();
                if (Files.exists(p)) {
                    try (InputStream in = Files.newInputStream(p)) {
                        next.load(in);
                    }
                } else {
                    try (OutputStream out = Files.newOutputStream(p)) {
                        Files.createDirectories(p.getParent());
                        for (String line : DEFAULTS) {
                            out.write((line + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        }
                    }
                }
                for (String line : DEFAULTS) {
                    if (line.startsWith("#") || !line.contains("=")) continue;
                    String key = line.substring(0, line.indexOf('='));
                    String value = line.substring(line.indexOf('=') + 1);
                    next.putIfAbsent(key, value);
                }
                props = next;
                loadedMtime = mtime;
            }
        } catch (IOException e) {
            // Defaults are the storm's will; a broken config run still runs.
            Properties fallback = new Properties();
            for (String line : DEFAULTS) {
                if (line.startsWith("#") || !line.contains("=")) continue;
                fallback.put(line.substring(0, line.indexOf('=')), line.substring(line.indexOf('=') + 1));
            }
            return fallback;
        }
        return props;
    }

    public static boolean getBool(String key, boolean def) {
        return Boolean.parseBoolean(get().getProperty(key, String.valueOf(def)));
    }

    public static float getFloat(String key, float def) {
        try {
            return Float.parseFloat(get().getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException nfe) {
            return def;
        }
    }
}
