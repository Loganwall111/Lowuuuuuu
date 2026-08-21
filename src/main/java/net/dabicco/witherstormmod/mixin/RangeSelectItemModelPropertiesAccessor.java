package net.dabicco.witherstormmod.mixin;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({RangeSelectItemModelProperties.class})
public interface RangeSelectItemModelPropertiesAccessor {
   @Accessor("ID_MAPPER")
   static LateBoundIdMapper<Identifier, MapCodec<? extends RangeSelectItemModelProperty>> dabyws$idMapper() {
      throw new AssertionError();
   }
}
