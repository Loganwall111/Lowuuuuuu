package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

/**
 * Regional & Biome Atmospheric Engine.
 *
 * Splits atmospheric storm rendering across distinct world regions:
 *  - Plains / Forests: Story Mode Twilight (deep purple gloom, starry sky, dark cloud deck)
 *  - Desert / Savannas / Badlands: Smoldering Void Storm (amber-crimson ash, scorching haze)
 *  - Ocean / Swamps: Abyssal Tempest (deep dark turquoise-teal mist, deluge rains)
 *  - Mountains / Frozen / Far Lands: Cosmic Reality Rift (deep violet-indigo, nebula stars)
 *  - Nether Dimension: Crimson Necrosis (blood-purple atmospheric ash)
 *  - Bowels Dimension: Visceral Interior (flesh purple-black, glowing command core)
 */
public final class BiomeStormFog {
   private BiomeStormFog() {
   }

   public enum RegionAtmosphere {
      TEMPERATE_TWILIGHT(0.19F, 0.07F, 0.28F, "Story Mode Twilight"),
      DESERT_SMOLDER(0.32F, 0.11F, 0.09F, "Smoldering Void Ash"),
      OCEAN_ABYSSAL(0.04F, 0.20F, 0.24F, "Abyssal Tempest"),
      MOUNTAIN_RIFT(0.24F, 0.08F, 0.36F, "Far Lands Reality Rift"),
      NETHER_NECROSIS(0.35F, 0.05F, 0.14F, "Crimson Necrosis"),
      BOWELS_VISCERAL(0.11F, 0.03F, 0.15F, "Visceral Void");

      public final float r;
      public final float g;
      public final float b;
      public final String displayName;

      RegionAtmosphere(float r, float g, float b, String displayName) {
         this.r = r;
         this.g = g;
         this.b = b;
         this.displayName = displayName;
      }
   }

   public static RegionAtmosphere regionAt(ClientLevel level, Vec3 cameraPos) {
      if (level == null || cameraPos == null) {
         return RegionAtmosphere.TEMPERATE_TWILIGHT;
      }

      if (level.dimension() == Level.NETHER) {
         return RegionAtmosphere.NETHER_NECROSIS;
      }
      if (level.dimension().location().getPath().contains("bowels")) {
         return RegionAtmosphere.BOWELS_VISCERAL;
      }

      double distFromOrigin = Math.sqrt(cameraPos.x * cameraPos.x + cameraPos.z * cameraPos.z);
      if (distFromOrigin >= 10000.0) {
         return RegionAtmosphere.MOUNTAIN_RIFT;
      }

      BlockPos pos = BlockPos.containing(cameraPos);
      Holder<Biome> biomeHolder = level.getBiome(pos);
      if (biomeHolder != null && biomeHolder.unwrapKey().isPresent()) {
         String biomePath = biomeHolder.unwrapKey().get().location().getPath().toLowerCase();
         if (biomePath.contains("desert") || biomePath.contains("badlands") || biomePath.contains("savanna") || biomePath.contains("mesa")) {
            return RegionAtmosphere.DESERT_SMOLDER;
         }
         if (biomePath.contains("ocean") || biomePath.contains("swamp") || biomePath.contains("river") || biomePath.contains("beach") || biomePath.contains("mangrove")) {
            return RegionAtmosphere.OCEAN_ABYSSAL;
         }
         if (biomePath.contains("peak") || biomePath.contains("mountain") || biomePath.contains("frozen") || biomePath.contains("ice") || biomePath.contains("snow")) {
            return RegionAtmosphere.MOUNTAIN_RIFT;
         }
      }

      return RegionAtmosphere.TEMPERATE_TWILIGHT;
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
      return Math.min(1.0F, closeness * (float)DabyWSClientConfig.biomeFogStrength);
   }

   /** Apply regional atmosphere tint to fog color */
   public static void applyRegionalTint(ClientLevel level, Camera camera, float[] rgb, float factor) {
      if (!DabyWSClientConfig.biomeFogTint || factor <= 0.0F) {
         return;
      }
      RegionAtmosphere region = regionAt(level, camera.position());
      float str = strength() * factor;
      rgb[0] = rgb[0] * (1.0F - str) + region.r * str;
      rgb[1] = rgb[1] * (1.0F - str) + region.g * str;
      rgb[2] = rgb[2] * (1.0F - str) + region.b * str;
   }
}
