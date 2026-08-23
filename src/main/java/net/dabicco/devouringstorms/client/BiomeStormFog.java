package net.dabicco.devouringstorms.client;

import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;

/**
 * Biome-tinted storm fog. When enabled, the storm's purple fog blends back toward
 * the biome fog colour Minecraft already computed for the environment, scaled by
 * how close the storm is. (The colour itself is read from the fog renderer; this
 * helper only supplies the blend strength.)
 */
public final class BiomeStormFog {
   private BiomeStormFog() {
   }

   /** How much the storm fog should blend toward the biome colour (0..1). */
   public static float strength() {
      if (!DevouringStormsClientConfig.biomeFogTint || DevouringStormsClientConfig.biomeFogStrength <= 0.0) {
         return 0.0F;
      }
      float closeness = StormFog.closeness();
      if (closeness <= 0.0F) {
         return 0.0F;
      }
      return Math.min(1.0F, closeness * (float)DevouringStormsClientConfig.biomeFogStrength);
   }
}
