package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.biome.Biome;

/**
 * Biome-tinted storm fog. When enabled, the purple storm fog blends toward the
 * colour of the biome the player is standing in, so the storm's atmosphere takes
 * on the character of the land it's devouring. Returns a strength (0..1) and the
 * target biome fog colour components.
 */
public final class BiomeStormFog {
   private BiomeStormFog() {
   }

   /** How much the storm fog should blend toward the biome colour (0..1). */
   public static float strength() {
      if (!DabyWSClientConfig.biomeFogTint || DabyWSClientConfig.biomeFogStrength <= 0.0) {
         return 0.0F;
      }
      float closeness = StormFog.closeness();
      if (closeness <= 0.0F) {
         return 0.0F;
      }
      return closeness * (float)DabyWSClientConfig.biomeFogStrength;
   }

   public static float tintR() {
      return biomeComponent(0);
   }

   public static float tintG() {
      return biomeComponent(1);
   }

   public static float tintB() {
      return biomeComponent(2);
   }

   private static float biomeComponent(int channel) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.player == null) {
         return 0.5F;
      }
      Biome biome = mc.level.getBiome(mc.player.blockPosition()).value();
      int packed = biome.getFogColor();
      int r = (packed >> 16) & 0xFF;
      int g = (packed >> 8) & 0xFF;
      int b = packed & 0xFF;
      switch (channel) {
         case 1 -> {
            return (float)g / 255.0F;
         }
         case 2 -> {
            return (float)b / 255.0F;
         }
         default -> {
            return (float)r / 255.0F;
         }
      }
   }
}
