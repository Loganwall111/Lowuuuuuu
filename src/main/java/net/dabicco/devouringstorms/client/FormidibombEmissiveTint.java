package net.dabicco.devouringstorms.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record FormidibombEmissiveTint(int light) implements ItemTintSource {
   public static final MapCodec<FormidibombEmissiveTint> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Codec.INT.optionalFieldOf("light", 0).forGetter(FormidibombEmissiveTint::light)).apply(instance, FormidibombEmissiveTint::new));

   public int calculate(ItemStack stack, ClientLevel level, LivingEntity owner) {
      double t = (double)(System.currentTimeMillis() % 3600000L) / (double)1000.0F;
      double offset = (double)this.light * 2.39996;
      double hue = 0.55 * Math.sin(t * 0.37 + offset) + 0.3 * Math.sin(t * 0.19 + offset * 1.7 + 1.3) + 0.15 * Math.sin(t * 0.83 + offset * 0.6);
      hue = (hue + (double)1.0F) * (double)0.5F;
      float saturation = 0.82F + 0.16F * (float)Math.sin(t * 0.27 + offset * 2.1);
      float value = 0.9F + 0.1F * (float)Math.sin(t * 0.53 + offset);
      return Mth.hsvToArgb((float)hue, Mth.clamp(saturation, 0.0F, 1.0F), Mth.clamp(value, 0.0F, 1.0F), 255);
   }

   public MapCodec<? extends ItemTintSource> type() {
      return MAP_CODEC;
   }
}
