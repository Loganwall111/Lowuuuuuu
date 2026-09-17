package net.dabicco.witherstormmod.bowels;

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
   public static final EntityType<net.dabicco.witherstormmod.bowels.BowelsHeartEntity> HEART = register(
      "bowels_heart",
      Builder.of(net.dabicco.witherstormmod.bowels.BowelsHeartEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(8).updateInterval(1)
   );
   public static final EntityType<net.dabicco.witherstormmod.bowels.BowelsTentacleEntity> TENTACLE = register(
      "bowels_tentacle",
      Builder.of(net.dabicco.witherstormmod.bowels.BowelsTentacleEntity::new, MobCategory.MISC).sized(2.0F, 2.0F).clientTrackingRange(8).updateInterval(1)
   );
   public static final EntityType<net.dabicco.witherstormmod.bowels.SeveredTentacleEntity> SEVERED_TENTACLE = register(
      "severed_tentacle",
      Builder.of(net.dabicco.witherstormmod.bowels.SeveredTentacleEntity::new, MobCategory.MISC).sized(0.9F, 0.9F).clientTrackingRange(8).updateInterval(3)
   );
   public static final EntityType<net.dabicco.witherstormmod.bowels.BowelsPedestalEntity> PEDESTAL = register(
      "bowels_pedestal",
      Builder.of(net.dabicco.witherstormmod.bowels.BowelsPedestalEntity::new, MobCategory.MISC).sized(14.0F, 12.0F).clientTrackingRange(6).updateInterval(1)
   );
   public static final EntityType<net.dabicco.witherstormmod.bowels.BowelsMawEntity> MAW = register(
      "bowels_maw",
      Builder.of(net.dabicco.witherstormmod.bowels.BowelsMawEntity::new, MobCategory.MISC).sized(4.0F, 3.0F).clientTrackingRange(6).updateInterval(1)
   );

   private ModBowelsEntities() {
   }

   private static <T extends Entity> EntityType<T> register(String name, Builder<T> builder) {
      ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("dabywitherstormmod", name));
      return (EntityType<T>)Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
   }

   public static void register() {
   }
}
