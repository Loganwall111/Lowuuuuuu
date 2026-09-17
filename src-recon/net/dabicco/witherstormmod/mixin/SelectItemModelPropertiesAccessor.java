package net.dabicco.witherstormmod.mixin;

import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({SelectItemModelProperties.class})
public interface SelectItemModelPropertiesAccessor {
   @Accessor("ID_MAPPER")
   static LateBoundIdMapper<Identifier, Type<?, ?>> dabyws$idMapper() {
      throw new AssertionError();
   }
}
