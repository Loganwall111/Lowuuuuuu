package net.dabicco.witherstormmod.entity;

import net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity;
import net.dabicco.witherstormmod.entity.withered.WitheredBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.level.Level;

public class ModEntityTypes {
   public static final EntityType<WitherStormEntity> WITHER_STORM;
   public static final EntityType<WitherStormClusterEntity> WITHER_STORM_CLUSTER;
   public static final EntityType<WitherStormHeadEntity> WITHER_STORM_HEAD;
   public static final EntityType<SuperSkullEntity> SUPER_SKULL;
   public static final EntityType<GrabTentacleEntity> GRAB_TENTACLE;
   public static final EntityType<CrossDimensionalEntity> CROSS_DIMENSIONAL;
   public static final EntityType<NetherScaleEntity> NETHER_SCALE;
   public static final EntityType<SuperTntEntity> SUPER_TNT;
   public static final EntityType<FormidibombEntity> FORMIDIBOMB;
   public static final EntityType<GrappledTntEntity> GRAPPLED_TNT;
   public static final EntityType<BlackHoleEntity> BLACK_HOLE;
   public static final EntityType<WitheredBlockEntity> WITHERED_BLOCK;
   public static final EntityType<SeveredWitherStormEntity> SEVERED_WITHER_STORM;

   private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
      ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("dabywitherstormmod", name));
      return (EntityType)Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
   }

   public static void registerModEntityTypes() {
   }

   public static void registerAttributes() {
      FabricDefaultAttributeRegistry.register(WITHER_STORM, WitherStormEntity.createAttributes());
      FabricDefaultAttributeRegistry.register(SEVERED_WITHER_STORM, SeveredWitherStormEntity.createAttributes());
   }

   static {
      WITHER_STORM = register("wither_storm", Builder.of(WitherStormEntity::new, MobCategory.MONSTER).sized(0.9F, 3.5F).clientTrackingRange(10000).updateInterval(1));
      WITHER_STORM_CLUSTER = register("wither_storm_cluster", Builder.of(WitherStormClusterEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(10000).updateInterval(1));
      WITHER_STORM_HEAD = register("wither_storm_head", Builder.of(WitherStormHeadEntity::new, MobCategory.MISC).sized(3.2F, 3.2F).clientTrackingRange(10000).updateInterval(1));
      SUPER_SKULL = register("super_skull", Builder.of(SuperSkullEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(10000).updateInterval(1));
      GRAB_TENTACLE = register("grab_tentacle", Builder.of(GrabTentacleEntity::new, MobCategory.MISC).sized(3.0F, 3.0F).clientTrackingRange(10000).updateInterval(1));
      CROSS_DIMENSIONAL = register("cross_dimensional", Builder.of(CrossDimensionalEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(10000).updateInterval(1));
      NETHER_SCALE = register("nether_scale", Builder.of(NetherScaleEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(10000).updateInterval(1));
      SUPER_TNT = register("super_tnt", Builder.of((EntityType<? extends SuperTntEntity> t, Level l) -> new SuperTntEntity(t, l), MobCategory.MISC).sized(0.98F, 0.98F).clientTrackingRange(10000).updateInterval(10).fireImmune());
      FORMIDIBOMB = register("formidibomb", Builder.of((EntityType<? extends FormidibombEntity> t, Level l) -> new FormidibombEntity(t, l), MobCategory.MISC).sized(0.98F, 0.98F).clientTrackingRange(10000).updateInterval(1).fireImmune());
      GRAPPLED_TNT = register("grappled_tnt", Builder.of((EntityType<? extends GrappledTntEntity> t, Level l) -> new GrappledTntEntity(t, l), MobCategory.MISC).sized(0.98F, 0.98F).clientTrackingRange(10000).updateInterval(1).fireImmune());
      BLACK_HOLE = register("black_hole", Builder.of(BlackHoleEntity::new, MobCategory.MISC).sized(2.0F, 2.0F).clientTrackingRange(10000).updateInterval(1));
      WITHERED_BLOCK = register("withered_block", Builder.of(WitheredBlockEntity::new, MobCategory.MISC).sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(1));
      SEVERED_WITHER_STORM = register("severed_wither_storm", Builder.of(SeveredWitherStormEntity::new, MobCategory.MISC).sized(0.9F, 3.5F).clientTrackingRange(10000).updateInterval(1));
   }
}
