package net.dabicco.devouringstorms.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record RetrieverContents(int tnt, List<ItemStack> rockets) {
   public static final int MAX = 4;
   public static final RetrieverContents EMPTY = new RetrieverContents(0, List.of());
   public static final Codec<RetrieverContents> CODEC = RecordCodecBuilder.create(
      i -> i.group(
               Codec.intRange(0, 4).optionalFieldOf("tnt", 0).forGetter(RetrieverContents::tnt),
               ItemStack.CODEC.listOf().optionalFieldOf("rockets", List.of()).forGetter(RetrieverContents::rockets)
            )
            .apply(i, RetrieverContents::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, RetrieverContents> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, RetrieverContents::tnt, ItemStack.OPTIONAL_LIST_STREAM_CODEC, RetrieverContents::rockets, RetrieverContents::new
   );

   public int rocketCount() {
      return this.rockets.size();
   }

   public boolean isEmpty() {
      return this.tnt == 0 && this.rockets.isEmpty();
   }

   public boolean canShoot() {
      return this.tnt > 0 && !this.rockets.isEmpty();
   }

   public RetrieverContents withTnt(int v) {
      return new RetrieverContents(clamp(v), this.rockets);
   }

   public RetrieverContents addRocket(ItemStack rocket) {
      if (this.rockets.size() >= 4) {
         return this;
      } else {
         List<ItemStack> next = new ArrayList<>(this.rockets);
         ItemStack one = rocket.copyWithCount(1);
         next.add(one);
         return new RetrieverContents(this.tnt, next);
      }
   }

   public ItemStack lastRocket() {
      return this.rockets.isEmpty() ? ItemStack.EMPTY : this.rockets.get(this.rockets.size() - 1);
   }

   public RetrieverContents consumeOne() {
      int t = Math.max(0, this.tnt - 1);
      if (this.rockets.isEmpty()) {
         return new RetrieverContents(t, this.rockets);
      } else {
         List<ItemStack> next = new ArrayList<>(this.rockets);
         next.remove(next.size() - 1);
         return new RetrieverContents(t, next);
      }
   }

   private static int clamp(int v) {
      return Math.max(0, Math.min(4, v));
   }
}
