package net.dabicco.witherstormmod.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record ControlPanelLightTint(int light) implements ItemTintSource {
   public static final MapCodec<net.dabicco.witherstormmod.client.ControlPanelLightTint> MAP_CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(Codec.INT.optionalFieldOf("light", 0).forGetter(net.dabicco.witherstormmod.client.ControlPanelLightTint::light))
         .apply(instance, net.dabicco.witherstormmod.client.ControlPanelLightTint::new)
   );

   public int calculate(ItemStack stack, ClientLevel level, LivingEntity owner) {
      double t = System.currentTimeMillis() % 3600000L / 1000.0;
      double offset = this.light * 2.39996;
      double hue = 0.55 * Math.sin(t * 0.31 + offset) + 0.3 * Math.sin(t * 0.17 + offset * 1.7 + 1.3) + 0.15 * Math.sin(t * 0.73 + offset * 0.6);
      hue = (hue + 1.0) * 0.5;
      float saturation = 0.8F + 0.18F * (float)Math.sin(t * 0.23 + offset * 2.1);
      float value = 0.88F + 0.12F * (float)Math.sin(t * 0.47 + offset);
      return Mth.hsvToArgb((float)hue, Mth.clamp(saturation, 0.0F, 1.0F), Mth.clamp(value, 0.0F, 1.0F), 255);
   }

   public MapCodec<? extends ItemTintSource> type() {
      return MAP_CODEC;
   }
}
