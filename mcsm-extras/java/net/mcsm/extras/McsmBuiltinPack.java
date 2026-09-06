package net.mcsm.extras;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * Devouring Storms: the Story Look pack ships INSIDE the mod jar
 * (resourcepacks/storylook/) and turns itself on via Fabric's resource-loader
 * registerBuiltinResourcePack with DEFAULT_ENABLED - no separate download, no
 * pack-screen step, and still user-disableable like any pack.
 *
 * Everything is invoked reflectively on purpose: the fabric-api generation
 * shipped for MC 26.2 changed the overload (older builds take
 * (ResourceLocation, ModContainer, predicate); newer ones take a leading
 * ResourcePackType). Reflection binds whichever signature actually exists at
 * runtime, and any failure degrades to a log line instead of a crash - the
 * game simply behaves as if the pack were a normal optional download.
 */
public final class McsmBuiltinPack {

    private McsmBuiltinPack() {
    }

    private static boolean attempted = false;

    public static void register() {
        if (attempted) {
            return;
        }
        attempted = true;
        try {
            Class<?> loaderCls = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object loader = loaderCls.getMethod("getInstance").invoke(null);
            Object opt = loaderCls.getMethod("getModContainer", String.class)
                    .invoke(loader, "dabywitherstormmod");
            if (!(opt instanceof Optional<?>) || ((Optional<?>) opt).isEmpty()) {
                warn("mod container not found");
                return;
            }
            Object modContainer = ((Optional<?>) opt).get();

            Class<?> rlCls = Class.forName("net.minecraft.resources.ResourceLocation");
            Object id = null;
            for (Method m : rlCls.getMethods()) {
                Class<?>[] ps = m.getParameterTypes();
                if (Modifier.isStatic(m.getModifiers()) && m.getReturnType() == rlCls
                        && ps.length == 2 && ps[0] == String.class && ps[1] == String.class) {
                    id = m.invoke(null, "dabywitherstormmod", "storylook");
                    break;
                }
            }
            if (id == null) {
                warn("no ResourceLocation(String,String) factory on this minecraft version");
                return;
            }

            Class<?> rmhCls = Class.forName("net.fabricmc.fabric.api.resource.ResourceManagerHelper");
            Class<?> predCls = Class.forName("net.fabricmc.fabric.api.resource.ResourcePackActivationPredicate");
            Object predicate = null;
            try {
                Field f = predCls.getField("DEFAULT_ENABLED");
                predicate = f.get(null);
            } catch (NoSuchFieldException ignored) {
                // fall through to the enum scan below
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
                warn("no DEFAULT_ENABLED activation predicate in this fabric-api");
                return;
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
                    break; // legacy signature wins outright
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
                warn("no registerBuiltinResourcePack overload recognized");
                return;
            }
            if (target.getParameterCount() == 3) {
                target.invoke(null, id, modContainer, predicate);
            } else {
                target.invoke(null, packType, id, modContainer, predicate);
            }
            System.out.println("[ds] Story Look built-in resource pack registered (default enabled)");
        } catch (Throwable t) {
            warn("unavailable: " + t);
        }
    }

    private static void warn(String msg) {
        System.err.println("[ds] Story Look built-in pack " + msg
                + " - install the storylook zip manually if the world looks vanilla");
    }
}
