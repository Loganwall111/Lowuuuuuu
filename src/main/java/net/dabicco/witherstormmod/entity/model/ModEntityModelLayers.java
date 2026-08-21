package net.dabicco.witherstormmod.entity.model;

import net.dabicco.witherstormmod.DabyWitherStormMod;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

/**
 * Model layer registry for the clean rewrite.
 *
 * The Wither Storm changes shape dramatically between phases, so it uses several model
 * layers. The working box models (HunchbackGrowth, HugeAssBackModel) are already in
 * this package and are registered below. As the user drops in additional Blockbench
 * exports, add them here alongside the matching {@code createBodyLayer()} supplier.
 */
public final class ModEntityModelLayers {
   public static final ModelLayerLocation WITHER_STORM = layer("wither_storm", "main");
   public static final ModelLayerLocation WITHER_STORM_P4 = layer("wither_storm_p4", "main");
   public static final ModelLayerLocation WITHER_STORM_DEVOURER = layer("wither_storm_devourer", "main");
   public static final ModelLayerLocation WITHER_STORM_HEAD = layer("wither_storm_head", "main");
   public static final ModelLayerLocation WITHER_STORM_HEAD_GLOW = layer("wither_storm_head_glow", "main");
   public static final ModelLayerLocation HUGE_ASS_BACK = layer("huge_ass_back", "main");
   public static final ModelLayerLocation HUNCHBACK_GROWTH = layer("hunchback_growth", "main");
   public static final ModelLayerLocation SUPER_SKULL = layer("super_skull", "main");
   public static final ModelLayerLocation GRAB_TENTACLE = layer("grab_tentacle", "main");

   private ModEntityModelLayers() {
   }

   private static ModelLayerLocation layer(String name, String variant) {
      return new ModelLayerLocation(DabyWitherStormMod.id(name), variant);
   }

   /**
    * Register every model layer that has a concrete {@code createBodyLayer()} supplier.
    * Working box models are registered; Blockbench-only layers are left unregistered
    * (with a comment) until the corresponding export is added.
    */
   public static void registerModelLayers() {
      ModelLayerRegistry.registerModelLayer(HUNCHBACK_GROWTH, HunchbackGrowth::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(HUGE_ASS_BACK, HugeAssBackModel::createBodyLayer);
      // Add Blockbench exports here, e.g.:
      // ModelLayerRegistry.registerModelLayer(WITHER_STORM_P4, WitherStormP4Model::createBodyLayer);
   }
}
