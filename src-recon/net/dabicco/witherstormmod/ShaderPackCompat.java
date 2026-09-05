package net.dabicco.witherstormmod.client;

import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;

public final class ShaderPackCompat {
   private static final long RECHECK_INTERVAL_MS = 500L;
   private static boolean resolved;
   private static Method getInstance;
   private static Method isShaderPackInUse;
   private static Object apiInstance;
   private static boolean cached;
   private static long cachedAt;

   private ShaderPackCompat() {
   }

   public static boolean active() {
      long now = System.currentTimeMillis();
      if (now - cachedAt < 500L) {
         return cached;
      } else {
         cachedAt = now;
         cached = query();
         return cached;
      }
   }

   private static boolean query() {
      if (!resolved) {
         resolved = true;
         if (!FabricLoader.getInstance().isModLoaded("iris") && !FabricLoader.getInstance().isModLoaded("oculus")) {
            return false;
         }

         try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            getInstance = api.getMethod("getInstance");
            apiInstance = getInstance.invoke(null);
            isShaderPackInUse = api.getMethod("isShaderPackInUse");
         } catch (Throwable var2) {
            isShaderPackInUse = null;
         }
      }

      if (isShaderPackInUse != null && apiInstance != null) {
         try {
            return Boolean.TRUE.equals(isShaderPackInUse.invoke(apiInstance));
         } catch (Throwable var11) {
            return false;
         }
      } else {
         return false;
      }
   }
}
