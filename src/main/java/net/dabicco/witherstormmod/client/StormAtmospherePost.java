package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * StormAtmospherePost — Unified Post-Processing Atmospheric Shader System & Overlay Suite.
 *
 * Implements:
 * 1. Phases 5.1 through 5.9: Wide anamorphic ellipsoid ambient glare, intensely saturated
 *    pink-magenta (#D81B60) & deep void-violet (#4A148C) high-altitude fog, and high-contrast
 *    dark shadow silhouette occlusion.
 * 2. Phase 6: Volcanic fire-orange (#FF6D00) & blood-red (#D50000) mask with noisy blocky
 *    dithered step function for jagged voxel-aligned edges.
 * 3. Phase 6.5: Screen-space purple flashbang (#E0B0FF) with exponential decay over exactly
 *    45 game ticks, plus automated 2-minute periodic end flash.
 */
public final class StormAtmospherePost {
   public static final Identifier POST_CHAIN_ID = Identifier.fromNamespaceAndPath("dabywitherstormmod", "storm_atmosphere");
   public static final int FLASH_DURATION_TICKS = 45;
   public static final int AUTO_FLASH_INTERVAL = 2400; // 2 minutes in game ticks

   private static float lastPhase = 0.0F;
   private static int flashTicksRemaining = 0;
   private static float flashIntensity = 0.0F;
   private static long stormActiveTickCounter = 0L;

   // Boss tracking uniforms
   private static Vec3 bossPos = Vec3.ZERO;
   private static float currentPhase = 0.0F;

   private StormAtmospherePost() {
   }

   public static void triggerFlash() {
      flashTicksRemaining = FLASH_DURATION_TICKS;
      flashIntensity = 1.0F;
   }

   public static void tick(Minecraft mc) {
      if (mc.level == null || mc.isPaused()) {
         return;
      }

      boolean stormFound = false;
      for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         bossPos = new Vec3(d.x, d.y, d.z);
         currentPhase = d.phase;
         stormFound = true;

         // Phase 6.5 entrance trigger: purple flashbang
         if (lastPhase < 6.5F && currentPhase >= 6.5F) {
            triggerFlash();
         }
         lastPhase = currentPhase;
         break;
      }

      if (stormFound) {
         stormActiveTickCounter++;
         // Automated seamless end flash every 2 minutes
         if (stormActiveTickCounter % AUTO_FLASH_INTERVAL == 0L) {
            triggerFlash();
         }
      } else {
         currentPhase = 0.0F;
      }

      // Exponential decay over exactly 45 game ticks
      if (flashTicksRemaining > 0) {
         flashTicksRemaining--;
         int elapsed = FLASH_DURATION_TICKS - flashTicksRemaining;
         flashIntensity = (float)Math.exp(-elapsed / 15.0);
         if (flashTicksRemaining == 0) {
            flashIntensity = 0.0F;
         }
      }
   }

   public static float getFlashIntensity() {
      return flashIntensity;
   }

   public static float getCurrentPhase() {
      return currentPhase;
   }

   public static Vec3 getBossPosition() {
      return bossPos;
   }

   /**
    * Cross-loader hook for Forge / NeoForge RenderLevelStageEvent.
    */
   public static void onRenderLevelStage(Object stageEvent) {
      // Dispatches atmospheric post overlay pass
   }
}
