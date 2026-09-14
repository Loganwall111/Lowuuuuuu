package net.mcsm.extras;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * Devouring Storms built-in resource packs.
 *
 * Story Look stays default-enabled for the no-shader MCSM palette. The OGS CEM
 * pack is also registered as the default model/preset again per the user's
 * 1.9.198 correction: use Loganwall111/ogs-stuff/witherstormmod as the default
 * Wither Storm look instead of endlessly recolouring approximations. Shaderpacks
 * remain available but are not forced on because the user's machine hit
 * native/OpenGL/pagefile exhaustion when heavy shaders were active.
 */
public final class McsmBuiltinPack {

    private McsmBuiltinPack() {
    }

    private static boolean attempted = false;

    /** Per-pack outcome, reported once in chat by McsmClientChat. */
    private static volatile String storyLookStatus = "pending";
    private static volatile String ogcCemStatus = "pending";
    private static volatile String shaderStatus = "pending";

    public static void register() {
        if (attempted) {
            return;
        }
        attempted = true;
        shaderStatus = McsmShaderPackInstall.install();
        McsmCommandBlockUse.register();
        boolean story = registerBuiltIn("storylook", "Story Look");
        storyLookStatus = story ? "built-in, default enabled"
                : (summonZip("storylook", "Story Look.zip") ? "extracted to resourcepacks/ (enable it in the pack screen)" : "MISSING - install the storylook zip from the release");
        boolean cems = registerBuiltIn("ogs-cem", "OGS CEM preset/model pack");
        ogcCemStatus = cems ? "built-in, default enabled" : "built-in registration failed";
    }

    /**
     * Build #374 -- the "summon" fallback the user asked for: when the
     * Fabric built-in registration is not available on this loader/fabric-api
     * combination, extract the pack zip that already ships inside the jar
     * (assets/dabywitherstormmod/resourcepacks/<id>.zip, built by CI) into the
     * game's resourcepacks/ folder so it is at least one click away instead
     * of a manual download.
     */
    private static boolean summonZip(String packId, String zipName) {
        try {
            java.io.InputStream in = McsmBuiltinPack.class
                    .getResourceAsStream("/assets/dabywitherstormmod/resourcepacks/" + packId + ".zip");
            if (in == null) {
                return false;
            }
            try {
                java.io.File dir = new java.io.File(gameDir(), "resourcepacks");
                dir.mkdirs();
                java.io.File out = new java.io.File(dir, zipName);
                try (java.io.OutputStream os = new java.io.FileOutputStream(out)) {
                    byte[] buf = new byte[65536];
                    int n;
                    while ((n = in.read(buf)) > 0) {
                        os.write(buf, 0, n);
                    }
                }
                System.out.println("[ds] " + zipName + " summoned to " + out.getAbsolutePath());
                return true;
            } finally {
                in.close();
            }
        } catch (Throwable t) {
            warn("Story Look", "summon failed: " + t);
            return false;
        }
    }

    /** One chat line, e.g. "Story Look: built-in, default enabled | OG CEM: ... | shader: ...". */
    public static String summary() {
        return "Story Look: " + storyLookStatus
                + " | OG CEM: " + ogcCemStatus
                + " | shader pack: " + shaderStatus;
    }

    private static String gameDir() {
        try {
            Class<?> loaderCls = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object loader = loaderCls.getMethod("getInstance").invoke(null);
            return loaderCls.getMethod("getGameDir").invoke(loader).toString();
        } catch (Throwable t) {
            return System.getProperty("user.dir");
        }
    }

    private static boolean registerBuiltIn(String packId, String label) {
        try {
            Class<?> loaderCls = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object loader = loaderCls.getMethod("getInstance").invoke(null);
            Object opt = loaderCls.getMethod("getModContainer", String.class)
                    .invoke(loader, "dabywitherstormmod");
            if (!(opt instanceof Optional<?>) || ((Optional<?>) opt).isEmpty()) {
                warn(label, "mod container not found");
                return false;
            }
            Object modContainer = ((Optional<?>) opt).get();

            Class<?> rlCls = null;
            for (String n : new String[] {
                    "net.minecraft.resources.Identifier",
                    "net.minecraft.resources.ResourceLocation" }) {
                try {
                    rlCls = Class.forName(n);
                    break;
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (rlCls == null) {
                warn(label, "no Identifier/ResourceLocation class on this minecraft version");
                return false;
            }

            Object id = null;
            for (Method m : rlCls.getMethods()) {
                Class<?>[] ps = m.getParameterTypes();
                if (Modifier.isStatic(m.getModifiers()) && m.getReturnType() == rlCls
                        && ps.length == 2 && ps[0] == String.class && ps[1] == String.class) {
                    id = m.invoke(null, "dabywitherstormmod", packId);
                    break;
                }
            }
            if (id == null) {
                warn(label, "no ResourceLocation(String,String) factory on this minecraft version");
                return false;
            }

            Class<?> rmhCls = Class.forName("net.fabricmc.fabric.api.resource.ResourceManagerHelper");
            Class<?> predCls = Class.forName("net.fabricmc.fabric.api.resource.ResourcePackActivationPredicate");
            Object predicate = null;
            try {
                Field f = predCls.getField("DEFAULT_ENABLED");
                predicate = f.get(null);
            } catch (NoSuchFieldException ignored) {
            }
            if (predicate == null) {
                for (Object c : predCls.getEnumConstants()) {
                    if ("DEFAULT_ENABLED".equals(String.valueOf(c))) {
                        predicate = c;
                        break;
                    }
                }
            }
            if (predicate == null) {
                warn(label, "no DEFAULT_ENABLED activation predicate in this fabric-api");
                return false;
            }

            Method target = null;
            Object packType = null;
            for (Method m : rmhCls.getMethods()) {
                if (!"registerBuiltinResourcePack".equals(m.getName())) {
                    continue;
                }
                Class<?>[] ps = m.getParameterTypes();
                if (ps.length == 3 && ps[0] == rlCls) {
                    target = m;
                    packType = null;
                    break;
                }
                if (ps.length == 4 && ps[1] == rlCls && target == null) {
                    for (Object c : ps[0].getEnumConstants()) {
                        String name = String.valueOf(c);
                        if (name.contains("CLIENT") || name.contains("RESOURCE")) {
                            packType = c;
                            break;
                        }
                    }
                    if (packType != null) {
                        target = m;
                    }
                }
            }
            if (target == null) {
                warn(label, "no registerBuiltinResourcePack overload recognized");
                return false;
            }
            if (target.getParameterCount() == 3) {
                target.invoke(null, id, modContainer, predicate);
            } else {
                target.invoke(null, packType, id, modContainer, predicate);
            }
            System.out.println("[ds] " + label + " built-in resource pack registered (default enabled): " + packId);
            return true;
        } catch (Throwable t) {
            warn(label, "unavailable: " + t);
            return false;
        }
    }

    private static void warn(String label, String msg) {
        System.err.println("[ds] " + label + " built-in pack " + msg
                + " - install the matching release zip manually if the world looks vanilla");
    }
}
