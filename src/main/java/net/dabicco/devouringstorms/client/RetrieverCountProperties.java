package net.dabicco.devouringstorms.client;

import com.mojang.serialization.MapCodec;
import net.dabicco.devouringstorms.item.RocketRetrieverItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

public final class RetrieverCountProperties {
   private RetrieverCountProperties() {
   }

   public static record TntCount() implements RangeSelectItemModelProperty {
      public static final MapCodec<TntCount> MAP_CODEC = MapCodec.unit(TntCount::new);

      public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
         return (float)RocketRetrieverItem.contents(stack).tnt();
      }

      public MapCodec<? extends RangeSelectItemModelProperty> type() {
         return MAP_CODEC;
      }
   }

   public static record RocketCount() implements RangeSelectItemModelProperty {
      public static final MapCodec<RocketCount> MAP_CODEC = MapCodec.unit(RocketCount::new);

      public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
         return (float)RocketRetrieverItem.contents(stack).rocketCount();
      }

      public MapCodec<? extends RangeSelectItemModelProperty> type() {
         return MAP_CODEC;
      }
   }
}
