package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.entity.withered.WitheredMobs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractArrow.class})
public abstract class AbstractArrowMixin {
   @Unique
   private static final double DABYWS_WITHERED_ARROW_DAMAGE = 0.6;

   @Inject(
      method = {"canHitEntity"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$passThroughWithered(Entity target, CallbackInfoReturnable<Boolean> cir) {
      if (target instanceof LivingEntity living && WitheredMobs.isWithered(living)) {
         cir.setReturnValue(false);
      }
   }

   @Inject(
      method = {"setOwner(Lnet/minecraft/world/entity/Entity;)V"},
      at = {@At("HEAD")}
   )
   private void dabyws$weakenWitheredArrows(Entity owner, CallbackInfo ci) {
      AbstractArrow self = (AbstractArrow)this;
      if (self.getOwner() == null && owner instanceof LivingEntity living && WitheredMobs.isWithered(living)) {
         self.setBaseDamage(0.6);
      }
   }
}
