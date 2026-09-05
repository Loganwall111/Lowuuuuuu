package net.dabicco.witherstormmod.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
   targets = {"net.minecraft.class_3898$class_3208"}
)
public interface EntityTrackerAccessor {
   @Accessor("entity")
   Entity getEntity();
}
