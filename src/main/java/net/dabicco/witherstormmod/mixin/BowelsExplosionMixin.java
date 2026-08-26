package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.BowelsGravity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({ServerLevel.class})
public abstract class BowelsExplosionMixin {
   @ModifyVariable(
      method = {"explode"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private ExplosionInteraction dabyws$noDiggingInHere(ExplosionInteraction given) {
      return BowelsGravity.isBowels((Level)(Object)this) ? ExplosionInteraction.NONE : given;
   }
}
