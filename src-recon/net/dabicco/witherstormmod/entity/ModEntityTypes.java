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

public class ModEntityTypes {
   public static final EntityType<net.dabicco.witherstormmod.entity.WitherStormEntity> WITHER_STORM = register(
      "wither_storm",
      Builder.of(net.dabicco.witherstormmod.entity.WitherStormEntity::new, MobCategory.MONSTER).sized(0.9F, 3.5F).clientTrackingRange(10000).updateInterval(1)
   );
   public static final EntityType<WitherStormClusterEntity> WITHER_STORM_CLUSTER = register(
      "wither_storm_cluster", Builder.of(WitherStormClusterEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(10000).updateInterval(1)
   );
   public static final EntityType<net.dabicco.witherstormmod.entity.WitherStormHeadEntity> WITHER_STORM_HEAD = register(
      "wither_storm_head",
      Builder.of(net.dabicco.witherstormmod.entity.WitherStormHeadEntity::new, MobCategory.MISC).sized(3.2F, 3.2F).clientTrackingRange(10000).updateInterval(1)
   );
   public static final EntityType<net.dabicco.witherstormmod.entity.SuperSkullEntity> SUPER_SKULL = register(
      "super_skull",
      Builder.of(net.dabicco.witherstormmod.entity.SuperSkullEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(10000).updateInterval(1)
   );
   public static final EntityType<net.dabicco.witherstormmod.entity.GrabTentacleEntity> GRAB_TENTACLE = register(
      "grab_tentacle",
      Builder.of(net.dabicco.witherstormmod.entity.GrabTentacleEntity::new, MobCategory.MISC).sized(3.0F, 3.0F).clientTrackingRange(10000).updateInterval(1)
   );
   public static final EntityType<net.dabicco.witherstormmod.entity.CrossDimensionalEntity> CROSS_DIMENSIONAL = register(
      "cross_dimensional",
      Builder.of(net.dabicco.witherstormmod.entity.CrossDimensionalEntity::new, MobCategory.MISC)
         .sized(1.0F, 1.0F)
         .clientTrackingRange(10000)
         .updateInterval(1)
   );
   public static final EntityType<net.dabicco.witherstormmod.entity.NetherScaleEntity> NETHER_SCALE = register(
      "nether_scale",
      Builder.of(net.dabicco.witherstormmod.entity.NetherScaleEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(10000).updateInterval(1)
   );
   public static final EntityType<net.dabicco.witherstormmod.entity.SuperTntEntity> SUPER_TNT = register(
      "super_tnt",
      Builder.of((t, l) -> new net.dabicco.witherstormmod.entity.SuperTntEntity(t, l), MobCategory.MISC)
         .sized(0.98F, 0.98F)
         .clientTrackingRange(10000)
         .updateInterval(10)
         .fireImmune()
   );
   public static final EntityType<net.dabicco.witherstormmod.entity.FormidibombEntity> FORMIDIBOMB = register(
      "formidibomb",
      Builder.of((t, l) -> new net.dabicco.witherstormmod.entity.FormidibombEntity(t, l), MobCategory.MISC)
         .sized(0.98F, 0.98F)
         .clientTrackingRange(10000)
         .updateInterval(1)
         .fireImmune()
   );
   public static final EntityType<net.dabicco.witherstormmod.entity.GrappledTntEntity> GRAPPLED_TNT = register(
      "grappled_tnt",
      Builder.of((t, l) -> new net.dabicco.witherstormmod.entity.GrappledTntEntity(t, l), MobCategory.MISC)
         .sized(0.98F, 0.98F)
         .clientTrackingRange(10000)
         .updateInterval(1)
         .fireImmune()
   );
   public static final EntityType<net.dabicco.witherstormmod.entity.BlackHoleEntity> BLACK_HOLE = register(
      "black_hole",
      Builder.of(net.dabicco.witherstormmod.entity.BlackHoleEntity::new, MobCategory.MISC).sized(2.0F, 2.0F).clientTrackingRange(10000).updateInterval(1)
   );
   public static final EntityType<WitheredBlockEntity> WITHERED_BLOCK = register(
      "withered_block", Builder.of(WitheredBlockEntity::new, MobCategory.MISC).sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(1)
   );
   public static final EntityType<net.dabicco.witherstormmod.entity.SeveredWitherStormEntity> SEVERED_WITHER_STORM = register(
      "severed_wither_storm",
      Builder.of(net.dabicco.witherstormmod.entity.SeveredWitherStormEntity::new, MobCategory.MISC)
         .sized(0.9F, 3.5F)
         .clientTrackingRange(10000)
         .updateInterval(1)
   );

   private static <T extends Entity> EntityType<T> register(String name, Builder<T> builder) {
      ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("dabywitherstormmod", name));
      return (EntityType<T>)Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
   }

   public static void registerModEntityTypes() {
   }

   public static void registerAttributes() {
      FabricDefaultAttributeRegistry.register(WITHER_STORM, net.dabicco.witherstormmod.entity.WitherStormEntity.createAttributes());
      FabricDefaultAttributeRegistry.register(SEVERED_WITHER_STORM, net.dabicco.witherstormmod.entity.SeveredWitherStormEntity.createAttributes());
   }
}
