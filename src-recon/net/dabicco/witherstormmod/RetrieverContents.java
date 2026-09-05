package net.dabicco.witherstormmod.item;

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
   public static final net.dabicco.witherstormmod.item.RetrieverContents EMPTY = new net.dabicco.witherstormmod.item.RetrieverContents(0, List.of());
   public static final Codec<net.dabicco.witherstormmod.item.RetrieverContents> CODEC = RecordCodecBuilder.create(
      i -> i.group(
            Codec.intRange(0, 4).optionalFieldOf("tnt", 0).forGetter(net.dabicco.witherstormmod.item.RetrieverContents::tnt),
            ItemStack.CODEC.listOf().optionalFieldOf("rockets", List.of()).forGetter(net.dabicco.witherstormmod.item.RetrieverContents::rockets)
         )
         .apply(i, net.dabicco.witherstormmod.item.RetrieverContents::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, net.dabicco.witherstormmod.item.RetrieverContents> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      net.dabicco.witherstormmod.item.RetrieverContents::tnt,
      ItemStack.OPTIONAL_LIST_STREAM_CODEC,
      net.dabicco.witherstormmod.item.RetrieverContents::rockets,
      net.dabicco.witherstormmod.item.RetrieverContents::new
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

   public net.dabicco.witherstormmod.item.RetrieverContents withTnt(int v) {
      return new net.dabicco.witherstormmod.item.RetrieverContents(clamp(v), this.rockets);
   }

   public net.dabicco.witherstormmod.item.RetrieverContents addRocket(ItemStack rocket) {
      if (this.rockets.size() >= 4) {
         return this;
      } else {
         List<ItemStack> next = new ArrayList<>(this.rockets);
         ItemStack one = rocket.copyWithCount(1);
         next.add(one);
         return new net.dabicco.witherstormmod.item.RetrieverContents(this.tnt, next);
      }
   }

   public ItemStack lastRocket() {
      return this.rockets.isEmpty() ? ItemStack.EMPTY : this.rockets.get(this.rockets.size() - 1);
   }

   public net.dabicco.witherstormmod.item.RetrieverContents consumeOne() {
      int t = Math.max(0, this.tnt - 1);
      if (this.rockets.isEmpty()) {
         return new net.dabicco.witherstormmod.item.RetrieverContents(t, this.rockets);
      } else {
         List<ItemStack> next = new ArrayList<>(this.rockets);
         next.remove(next.size() - 1);
         return new net.dabicco.witherstormmod.item.RetrieverContents(t, next);
      }
   }

   private static int clamp(int v) {
      return Math.max(0, Math.min(4, v));
   }
}
