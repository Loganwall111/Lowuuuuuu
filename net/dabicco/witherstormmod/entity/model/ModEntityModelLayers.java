package net.dabicco.witherstormmod.entity.model;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public class ModEntityModelLayers {
   public static final ModelLayerLocation WITHER_STORM = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "wither_storm"), "main");
   public static final ModelLayerLocation WITHER_STORM_P4 = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "wither_storm_p4"), "main");
   public static final ModelLayerLocation WITHER_STORM_DEVOURER = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "wither_storm_devourer"), "main");
   public static final ModelLayerLocation WITHER_STORM_TENTACLES_DEVOURER = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "wither_storm_tentacles_devourer"), "main");
   public static final ModelLayerLocation STORM_COVER = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "storm_cover"), "main");
   public static final ModelLayerLocation WITHER_STORM_HEAD = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "wither_storm_head"), "main");
   public static final ModelLayerLocation WITHER_STORM_HEAD_GLOW = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "wither_storm_head_glow"), "main");
   public static final ModelLayerLocation WITHER_STORM_HEAD_EYE_GLOW = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "wither_storm_head_eye_glow"), "main");
   public static final ModelLayerLocation WITHER_STORM_GROWTH5 = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "wither_storm_growth5"), "main");
   public static final ModelLayerLocation WITHER_STORM_TENTACLES5 = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "wither_storm_tentacles5"), "main");
   public static final ModelLayerLocation HUGE_ASS_BACK = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "huge_ass_back"), "main");
   public static final ModelLayerLocation HUNCHBACK_GROWTH = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "hunchback_growth"), "main");
   public static final ModelLayerLocation TENTACLE = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "tentacle"), "main");
   public static final ModelLayerLocation SEVERED_WITHER_STORM = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "severed_wither_storm"), "main");
   public static final ModelLayerLocation SUPER_SKULL = new ModelLayerLocation(Identifier.fromNamespaceAndPath("dabywitherstormmod", "super_skull"), "main");

   public static void registerModelLayers() {
      ModelLayerRegistry.registerModelLayer(SUPER_SKULL, SuperSkull::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(WITHER_STORM, WitherCommandBlock::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(WITHER_STORM_P4, WitherStormP4::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(WITHER_STORM_DEVOURER, WitherStormDevourer::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(WITHER_STORM_TENTACLES_DEVOURER, WitherStormTentaclesDevourer::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(STORM_COVER, StormCoverModel::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(WITHER_STORM_HEAD, WitherStormHead::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(WITHER_STORM_HEAD_GLOW, WitherStormHead::createGlowLayer);
      ModelLayerRegistry.registerModelLayer(WITHER_STORM_HEAD_EYE_GLOW, WitherStormHead::createEyeGlowLayer);
      ModelLayerRegistry.registerModelLayer(WITHER_STORM_GROWTH5, WitherStormGrowth5::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(HUNCHBACK_GROWTH, HunchbackGrowth::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(HUGE_ASS_BACK, HugeAssBackModel::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(WITHER_STORM_TENTACLES5, WitherStormTentacles5::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(TENTACLE, Tentacle::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(SEVERED_WITHER_STORM, SeveredWitherStorm::createBodyLayer);
   }
}
