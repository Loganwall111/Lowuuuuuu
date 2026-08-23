package net.dabicco.devouringstorms.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({LivingEntity.class})
public interface LivingEntitySwimAccessor {
   @Accessor("swimAmount")
   float dabyws$getSwimAmount();

   @Accessor("swimAmount")
   void dabyws$setSwimAmount(float var1);
}
