package net.dabicco.witherstormmod.entity.model;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

/**
 * Model layer registry for the clean rewrite.
 *
 * The Wither Storm uses several layers because it changes shape dramatically between
 * phases (commanded Wither -> giant Devourer). Each layer is baked from a model the
 * user builds in Blockbench. Model layer locations are defined here so renderers and
 * the client can bake them by name.
 */
public final class ModEntityModelLayers {
   public static final ModelLayerLocation WITHER_STORM = layer("wither_storm", "main");
   public static final ModelLayerLocation WITHER_STORM_HEAD = layer("wither_storm_head", "main");
   public static final ModelLayerLocation WITHER_STORM_HEAD_GLOW = layer("wither_storm_head_glow", "main");
   public static final ModelLayerLocation SUPER_SKULL = layer("super_skull", "main");
   public static final ModelLayerLocation GRAB_TENTACLE = layer("grab_tentacle", "main");

   private ModEntityModelLayers() {
   }

   private static ModelLayerLocation layer(String name, String variant) {
      return new ModelLayerLocation(DabyWitherStormMod.id(name), variant);
   }

   /**
    * Register every model layer. The Blockbench classes referenced here are created by
    * the user; until they exist, comment the corresponding registerModelLayer line.
    */
   public static void registerModelLayers() {
      // Example (uncomment as the user drops in Blockbench models):
      // ModelLayerRegistry.registerModelLayer(WITHER_STORM, WitherStormModel::createBodyLayer);
      // ModelLayerRegistry.registerModelLayer(WITHER_STORM_HEAD, WitherStormHeadModel::createBodyLayer);
   }
}
