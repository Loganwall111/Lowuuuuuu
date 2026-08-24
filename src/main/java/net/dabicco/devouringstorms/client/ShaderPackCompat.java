package net.dabicco.devouringstorms.client;

import java.lang.reflect.Method;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ShaderPackCompat {
   private static final long RECHECK_INTERVAL_MS = 500L;
   private static boolean resolved;
   private static boolean installed;
   private static Method getInstance;
   private static Method isShaderPackInUse;
   private static Method openMainIrisScreenObj;
   private static Method getConfig;
   private static Method areShadersEnabled;
   private static Method setShadersEnabledAndApply;
   private static Object apiInstance;
   private static Object apiConfig;
   private static boolean cached;
   private static long cachedAt;
   private static String lastError = "";

   private ShaderPackCompat() {
   }

   public static boolean installed() {
      resolve();
      return installed;
   }

   public static boolean canOpenMainScreen() {
      resolve();
      return installed && apiInstance != null && openMainIrisScreenObj != null;
   }

   public static boolean canToggleShaders() {
      resolve();
      return installed && apiConfig != null && areShadersEnabled != null && setShadersEnabledAndApply != null;
   }

   public static String statusText() {
      if (!installed()) {
         return "Not Installed";
      } else if (active()) {
         return "Pack Active";
      } else if (shadersEnabled()) {
         return "Enabled";
      } else if (canOpenMainScreen()) {
         return "Installed";
      } else {
         return "Limited API";
      }
   }

   public static String lastError() {
      return lastError;
   }

   public static boolean active() {
      resolve();
      long now = System.currentTimeMillis();
      if (now - cachedAt < 500L) {
         return cached;
      } else {
         cachedAt = now;
         cached = queryActive();
         return cached;
      }
   }

   public static boolean shadersEnabled() {
      resolve();
      if (apiConfig != null && areShadersEnabled != null) {
         try {
            return Boolean.TRUE.equals(areShadersEnabled.invoke(apiConfig));
         } catch (Throwable t) {
            noteError(t);
         }
      }

      return active();
   }

   public static boolean openMainScreen(Screen parent) {
      resolve();
      if (apiInstance != null && openMainIrisScreenObj != null) {
         try {
            Object next = openMainIrisScreenObj.invoke(apiInstance, parent);
            if (next instanceof Screen screen) {
               Minecraft.getInstance().gui.setScreen(screen);
               return true;
            }
         } catch (Throwable t) {
            noteError(t);
         }
      }

      return false;
   }

   public static boolean toggleShadersEnabled() {
      resolve();
      if (apiConfig != null && setShadersEnabledAndApply != null) {
         try {
            boolean next = !shadersEnabled();
            setShadersEnabledAndApply.invoke(apiConfig, next);
            cachedAt = 0L;
            cached = queryActive();
            return true;
         } catch (Throwable t) {
            noteError(t);
         }
      }

      return false;
   }

   public static float companionGlowGain() {
      if (!active()) {
         return 1.0F;
      } else {
         float manual = Math.max(0.25F, (float)DevouringStormsClientConfig.shaderPackEmissiveGain);
         int profile = (int)Math.round(DevouringStormsClientConfig.shaderPackProfile);
         float preset;
         switch (profile) {
            case 1:
               preset = 1.2F;
               break;
            case 2:
               preset = 1.5F;
               break;
            default:
               int bloom = (int)Math.round(DevouringStormsClientConfig.bloomStrength);
               switch (bloom) {
                  case 0:
                     preset = 0.9F;
                     break;
                  case 1:
                     preset = 1.15F;
                     break;
                  case 2:
                  default:
                     preset = 1.35F;
                     break;
                  case 3:
                     preset = 1.7F;
               }
         }

         return preset * manual;
      }
   }

   public static int emissiveAlphaFloor(boolean boost) {
      int base = boost ? 220 : 180;
      if (!active()) {
         return base;
      } else {
         return Math.max(base, Math.min(255, Math.round((float)base * companionGlowGain())));
      }
   }

   private static void resolve() {
      if (!resolved) {
         resolved = true;
         installed = FabricLoader.getInstance().isModLoaded("iris") || FabricLoader.getInstance().isModLoaded("oculus");
         if (installed) {
            try {
               Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
               getInstance = api.getMethod("getInstance");
               apiInstance = getInstance.invoke((Object)null);
               isShaderPackInUse = api.getMethod("isShaderPackInUse");

               try {
                  openMainIrisScreenObj = api.getMethod("openMainIrisScreenObj", Object.class);
               } catch (Throwable var5) {
               }

               try {
                  getConfig = api.getMethod("getConfig");
                  apiConfig = getConfig.invoke(apiInstance);
               } catch (Throwable var4) {
               }

               if (apiConfig != null) {
                  try {
                     areShadersEnabled = apiConfig.getClass().getMethod("areShadersEnabled");
                  } catch (Throwable var3) {
                  }

                  try {
                     setShadersEnabledAndApply = apiConfig.getClass().getMethod("setShadersEnabledAndApply", Boolean.TYPE);
                  } catch (Throwable var2) {
                  }
               }
            } catch (Throwable t) {
               noteError(t);
               isShaderPackInUse = null;
               apiInstance = null;
               apiConfig = null;
            }
         }
      }
   }

   private static boolean queryActive() {
      resolve();
      if (isShaderPackInUse != null && apiInstance != null) {
         try {
            return Boolean.TRUE.equals(isShaderPackInUse.invoke(apiInstance));
         } catch (Throwable t) {
            noteError(t);
            return false;
         }
      } else {
         return false;
      }
   }

   private static void noteError(Throwable t) {
      if (t != null) {
         lastError = t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage());
      }
   }
}
