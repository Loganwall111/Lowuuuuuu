package net.dabicco.witherstormmod.mixin;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({FireworkRocketEntity.class})
public interface FireworkRocketEntityAccessor {
   @Accessor("lifetime")
   void setLifetime(int var1);

   @Accessor("lifetime")
   int getLifetime();

   @Accessor("life")
   int getLife();
}
