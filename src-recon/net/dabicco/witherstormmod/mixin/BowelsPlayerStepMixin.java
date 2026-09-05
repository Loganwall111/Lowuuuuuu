package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.BowelsFrame;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Player.class})
public abstract class BowelsPlayerStepMixin {
   @Inject(
      method = {"aiStep()V"},
      at = {@At("HEAD")}
   )
   private void dabyws$intoFrame(CallbackInfo ci) {
      BowelsFrame.stepIn((Player)this);
   }

   @Inject(
      method = {"aiStep()V"},
      at = {@At("RETURN")}
   )
   private void dabyws$outOfFrame(CallbackInfo ci) {
      BowelsFrame.stepOut((Player)this);
   }

   @Inject(
      method = {"canPlayerFitWithinBlocksAndEntitiesWhen(Lnet/minecraft/world/entity/Pose;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$fitsTurned(Pose pose, CallbackInfoReturnable<Boolean> cir) {
      Player self = (Player)(Object)this;
      Direction gravity = BowelsFrame.boxAxis(self);
      if (gravity != Direction.DOWN) {
         EntityDimensions size = self.getDimensions(pose);
         AABB box = BowelsFrame.box(gravity, self.position(), size.width(), size.height()).deflate(1.0E-7);
         cir.setReturnValue(self.level().noCollision(self, box));
      }
   }

   @Inject(
      method = {"maybeBackOffFromEdge(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)Lnet/minecraft/world/phys/Vec3;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$edgeInFrame(Vec3 movement, MoverType mover, CallbackInfoReturnable<Vec3> cir) {
      Player self = (Player)(Object)this;
      Direction gravity = BowelsFrame.boxAxis(self);
      if (gravity != Direction.DOWN
         && !self.getAbilities().flying
         && self.isShiftKeyDown()
         && self.onGround()
         && (mover == MoverType.SELF || mover == MoverType.PLAYER)) {
         cir.setReturnValue(BowelsFrame.backOffFromEdge(self, gravity, movement));
      }
   }
}
