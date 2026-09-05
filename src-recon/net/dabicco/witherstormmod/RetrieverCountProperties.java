package net.dabicco.witherstormmod.client;

import com.mojang.serialization.MapCodec;
import net.dabicco.witherstormmod.item.RocketRetrieverItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

public final class RetrieverCountProperties {
   private RetrieverCountProperties() {
   }

   public record RocketCount() implements RangeSelectItemModelProperty {
      public static final MapCodec<net.dabicco.witherstormmod.client.RetrieverCountProperties.RocketCount> MAP_CODEC = MapCodec.unit(
         net.dabicco.witherstormmod.client.RetrieverCountProperties.RocketCount::new
      );

      public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
         return RocketRetrieverItem.contents(stack).rocketCount();
      }

      public MapCodec<? extends RangeSelectItemModelProperty> type() {
         return MAP_CODEC;
      }
   }

   public record TntCount() implements RangeSelectItemModelProperty {
      public static final MapCodec<net.dabicco.witherstormmod.client.RetrieverCountProperties.TntCount> MAP_CODEC = MapCodec.unit(
         net.dabicco.witherstormmod.client.RetrieverCountProperties.TntCount::new
      );

      public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
         return RocketRetrieverItem.contents(stack).tnt();
      }

      public MapCodec<? extends RangeSelectItemModelProperty> type() {
         return MAP_CODEC;
      }
   }
}
