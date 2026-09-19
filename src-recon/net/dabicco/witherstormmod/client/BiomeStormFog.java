package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

public final class BiomeStormFog {
   private BiomeStormFog() {
   }

   public static net.dabicco.witherstormmod.client.BiomeStormFog.RegionAtmosphere regionAt(ClientLevel level, Vec3 cameraPos) {
      if (level != null && cameraPos != null) {
         if (level.dimension() == Level.NETHER) {
            return net.dabicco.witherstormmod.client.BiomeStormFog.RegionAtmosphere.NETHER_NECROSIS;
         } else if (level.dimension().identifier().getPath().contains("bowels")) {
            return net.dabicco.witherstormmod.client.BiomeStormFog.RegionAtmosphere.BOWELS_VISCERAL;
         } else {
            double distFromOrigin = Math.sqrt(cameraPos.x * cameraPos.x + cameraPos.z * cameraPos.z);
            if (distFromOrigin >= 10000.0) {
               return net.dabicco.witherstormmod.client.BiomeStormFog.RegionAtmosphere.MOUNTAIN_RIFT;
            } else {
               BlockPos pos = BlockPos.containing(cameraPos);
               Holder<Biome> biomeHolder = level.getBiome(pos);
               if (biomeHolder != null && biomeHolder.unwrapKey().isPresent()) {
                  String biomePath = ((ResourceKey)biomeHolder.unwrapKey().get()).identifier().getPath().toLowerCase();
                  if (biomePath.contains("desert") || biomePath.contains("badlands") || biomePath.contains("savanna") || biomePath.contains("mesa")) {
                     return net.dabicco.witherstormmod.client.BiomeStormFog.RegionAtmosphere.DESERT_SMOLDER;
                  }

                  if (biomePath.contains("ocean")
                     || biomePath.contains("swamp")
                     || biomePath.contains("river")
                     || biomePath.contains("beach")
                     || biomePath.contains("mangrove")) {
                     return net.dabicco.witherstormmod.client.BiomeStormFog.RegionAtmosphere.OCEAN_ABYSSAL;
                  }

                  if (biomePath.contains("peak")
                     || biomePath.contains("mountain")
                     || biomePath.contains("frozen")
                     || biomePath.contains("ice")
                     || biomePath.contains("snow")) {
                     return net.dabicco.witherstormmod.client.BiomeStormFog.RegionAtmosphere.MOUNTAIN_RIFT;
                  }
               }

               return net.dabicco.witherstormmod.client.BiomeStormFog.RegionAtmosphere.TEMPERATE_TWILIGHT;
            }
         }
      } else {
         return net.dabicco.witherstormmod.client.BiomeStormFog.RegionAtmosphere.TEMPERATE_TWILIGHT;
      }
   }

   public static float strength() {
      if (DabyWSClientConfig.biomeFogTint && !(DabyWSClientConfig.biomeFogStrength <= 0.0)) {
         float closeness = net.dabicco.witherstormmod.client.StormFog.closeness();
         return closeness <= 0.0F ? 0.0F : Math.min(1.0F, closeness * (float)DabyWSClientConfig.biomeFogStrength);
      } else {
         return 0.0F;
      }
   }

   public static void applyRegionalTint(ClientLevel level, Camera camera, float[] rgb, float factor) {
      if (DabyWSClientConfig.biomeFogTint && !(factor <= 0.0F)) {
         net.dabicco.witherstormmod.client.BiomeStormFog.RegionAtmosphere region = regionAt(level, camera.position());
         float str = strength() * factor;
         rgb[0] = rgb[0] * (1.0F - str) + region.r * str;
         rgb[1] = rgb[1] * (1.0F - str) + region.g * str;
         rgb[2] = rgb[2] * (1.0F - str) + region.b * str;
      }
   }

   public static enum RegionAtmosphere {
      TEMPERATE_TWILIGHT(0.19F, 0.07F, 0.28F, "Story Mode Twilight"),
      DESERT_SMOLDER(0.32F, 0.11F, 0.09F, "Smoldering Void Ash"),
      OCEAN_ABYSSAL(0.04F, 0.2F, 0.24F, "Abyssal Tempest"),
      MOUNTAIN_RIFT(0.24F, 0.08F, 0.36F, "Far Lands Reality Rift"),
      NETHER_NECROSIS(0.35F, 0.05F, 0.14F, "Crimson Necrosis"),
      BOWELS_VISCERAL(0.11F, 0.03F, 0.15F, "Visceral Void");

      public final float r;
      public final float g;
      public final float b;
      public final String displayName;

      private RegionAtmosphere(float r, float g, float b, String displayName) {
         this.r = r;
         this.g = g;
         this.b = b;
         this.displayName = displayName;
      }
   }
}
