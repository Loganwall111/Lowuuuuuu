package net.dabicco.witherstormmod.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record FormidibombProperty() implements SelectItemModelProperty<String> {
   public static final MapCodec<net.dabicco.witherstormmod.client.FormidibombProperty> MAP_CODEC = MapCodec.unit(
      net.dabicco.witherstormmod.client.FormidibombProperty::new
   );
   public static final Type<net.dabicco.witherstormmod.client.FormidibombProperty, String> TYPE = Type.create(MAP_CODEC, Codec.STRING);

   public String get(ItemStack stack, ClientLevel level, LivingEntity owner, int seed, ItemDisplayContext ctx) {
      return "old";
   }

   public Codec<String> valueCodec() {
      return Codec.STRING;
   }

   public Type<? extends SelectItemModelProperty<String>, String> type() {
      return TYPE;
   }
}
