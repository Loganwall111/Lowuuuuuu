package net.mcsm.extras;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Installs the two visual resource packs as real resource-pack zips as well as
 * built-ins. Some loaders/pack menus do not show DEFAULT_ENABLED built-ins
 * clearly, which made users think the sky/OG packs never shipped. This makes
 * them visible and selects them in options.txt on the next reload/launch.
 */
public final class McsmResourcePackInstall {
    private static final String STORY_RES = "/assets/dabywitherstormmod/resourcepacks/storylook.zip";
    private static final String OGS_RES = "/assets/dabywitherstormmod/resourcepacks/ogs-cem.zip";
    private static final String STORY_FILE = "DevouringStorms-StoryLook.zip";
    private static final String OGS_FILE = "DevouringStorms-OGS-CEM.zip";
    private static boolean attempted;

    private McsmResourcePackInstall() {}

    public static synchronized void install() {
        if (attempted) return;
        attempted = true;
        try {
            File gameDir = gameDir();
            if (gameDir == null) return;
            File dir = new File(gameDir, "resourcepacks");
            dir.mkdirs();
            boolean story = copy(STORY_RES, new File(dir, STORY_FILE));
            boolean ogs = copy(OGS_RES, new File(dir, OGS_FILE));
            if (story || ogs) {
                select(gameDir, STORY_FILE, OGS_FILE);
                System.out.println("[ds] installed visible Story Look / OGS resource packs into resourcepacks/");
            }
        } catch (Throwable t) {
            System.err.println("[ds] resource pack installer skipped: " + t);
        }
    }

    private static boolean copy(String res, File out) {
        try (InputStream in = McsmResourcePackInstall.class.getResourceAsStream(res)) {
            if (in == null) return false;
            Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static void select(File gameDir, String... files) {
        try {
            File options = new File(gameDir, "options.txt");
            List<String> lines = options.isFile()
                    ? Files.readAllLines(options.toPath()) : new ArrayList<>();
            String value = null;
            int idx = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("resourcePacks:")) {
                    idx = i;
                    value = lines.get(i).substring("resourcePacks:".length());
                    break;
                }
            }
            if (value == null || value.isBlank()) {
                value = "[]";
            }
            for (String f : files) {
                String entry = "file/" + f;
                if (!value.contains(entry)) {
                    String quoted = "\"" + entry.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
                    if (value.endsWith("]") && value.length() > 1) {
                        value = value.substring(0, value.length() - 1) + "," + quoted + "]";
                    } else {
                        value = "[" + quoted + "]";
                    }
                }
            }
            String line = "resourcePacks:" + value;
            if (idx >= 0) lines.set(idx, line); else lines.add(line);
            try (OutputStream out = new FileOutputStream(options)) {
                for (String l : lines) {
                    out.write(l.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    out.write('\n');
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static File gameDir() {
        try {
            Class<?> loaderCls = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object loader = loaderCls.getMethod("getInstance").invoke(null);
            Object path = loaderCls.getMethod("getGameDir").invoke(loader);
            return new File(path.toString());
        } catch (Throwable t) {
            return null;
        }
    }
}
