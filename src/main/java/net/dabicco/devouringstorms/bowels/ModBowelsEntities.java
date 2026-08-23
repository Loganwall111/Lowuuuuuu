package net.dabicco.devouringstorms.bowels;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public final class ModBowelsEntities {
   public static final EntityType<BowelsHeartEntity> HEART;
   public static final EntityType<BowelsTentacleEntity> TENTACLE;
   public static final EntityType<SeveredTentacleEntity> SEVERED_TENTACLE;
   public static final EntityType<BowelsPedestalEntity> PEDESTAL;
   public static final EntityType<BowelsMawEntity> MAW;

   private ModBowelsEntities() {
   }

   private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
      ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("devouringstorms", name));
      return (EntityType)Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
   }

   public static void register() {
   }

   static {
      HEART = register("bowels_heart", Builder.of(BowelsHeartEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(8).updateInterval(1));
      TENTACLE = register("bowels_tentacle", Builder.of(BowelsTentacleEntity::new, MobCategory.MISC).sized(2.0F, 2.0F).clientTrackingRange(8).updateInterval(1));
      SEVERED_TENTACLE = register("severed_tentacle", Builder.of(SeveredTentacleEntity::new, MobCategory.MISC).sized(0.9F, 0.9F).clientTrackingRange(8).updateInterval(3));
      PEDESTAL = register("bowels_pedestal", Builder.of(BowelsPedestalEntity::new, MobCategory.MISC).sized(14.0F, 12.0F).clientTrackingRange(6).updateInterval(1));
      MAW = register("bowels_maw", Builder.of(BowelsMawEntity::new, MobCategory.MISC).sized(4.0F, 3.0F).clientTrackingRange(6).updateInterval(1));
   }
}
