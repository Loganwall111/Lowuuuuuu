package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.BowelsFrame;
import net.dabicco.witherstormmod.BowelsTrace;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class BowelsAiStepMixin {
   @Inject(
      method = {"aiStep()V"},
      at = {@At("HEAD")}
   )
   private void dabyws$intoFrame(CallbackInfo ci) {
      LivingEntity self = (LivingEntity)(Object)(Object)this;
      boolean outer = BowelsFrame.active(self) == null;
      if (outer) {
         BowelsTrace.record(self, 0, self.getDeltaMovement(), false);
      }

      BowelsFrame.stepIn(self);
      if (outer) {
         BowelsTrace.record(self, 1, self.getDeltaMovement(), true);
      }
   }

   @Inject(
      method = {"aiStep()V"},
      at = {@At("RETURN")}
   )
   private void dabyws$outOfFrame(CallbackInfo ci) {
      LivingEntity self = (LivingEntity)(Object)(Object)this;
      BowelsTrace.record(self, 5, self.getDeltaMovement(), true);
      BowelsFrame.stepOut(self);
      if (BowelsFrame.active(self) == null) {
         BowelsTrace.record(self, 6, self.getDeltaMovement(), false);
      }
   }

   @Inject(
      method = {"travel(Lnet/minecraft/world/phys/Vec3;)V"},
      at = {@At("HEAD")}
   )
   private void dabyws$travelIn(Vec3 input, CallbackInfo ci) {
      LivingEntity self = (LivingEntity)(Object)(Object)this;
      BowelsTrace.record(self, 2, self.getDeltaMovement(), true);
      BowelsTrace.recordInput(self, input, self.getSpeed());
   }

   @Inject(
      method = {"maxUpStep()F"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$noVanillaStep(CallbackInfoReturnable<Float> cir) {
      LivingEntity self = (LivingEntity)(Object)(Object)this;
      if (BowelsFrame.boxAxis(self) != Direction.DOWN) {
         cir.setReturnValue(0.0F);
      }
   }

   @Redirect(
      method = {"tick()V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/LivingEntity;tickHeadTurn(F)V"
      )
   )
   private void dabyws$bodyYawInFrame(LivingEntity target, float yaw) {
      Direction gravity = BowelsFrame.boxAxis(target);
      if (gravity == Direction.DOWN) {
         this.tickHeadTurn(yaw);
      } else {
         Vec3 moved = BowelsFrame.toFrame(gravity, target.position().subtract(target.xo, target.yo, target.zo));
         float flat = (float)(moved.x * moved.x + moved.z * moved.z);
         float facing = target.yBodyRot;
         if (flat > 0.0025000002F) {
            facing = (float)Mth.atan2(moved.z, moved.x) * (180.0F / (float)Math.PI) - 90.0F;
            float off = Mth.abs(Mth.wrapDegrees(target.getYRot()) - facing);
            if (95.0F < off && off < 265.0F) {
               facing -= 180.0F;
            }
         }

         this.tickHeadTurn(facing);
      }
   }

   @Shadow
   protected abstract void tickHeadTurn(float var1);

   @Inject(
      method = {"wouldNotSuffocateAtTargetPose(Lnet/minecraft/world/entity/Pose;)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$suffocateTurned(Pose pose, CallbackInfoReturnable<Boolean> cir) {
      LivingEntity self = (LivingEntity)(Object)(Object)this;
      Direction gravity = BowelsFrame.boxAxis(self);
      if (gravity != Direction.DOWN) {
         EntityDimensions size = self.getDimensions(pose);
         AABB box = BowelsFrame.box(gravity, self.position(), size.width(), size.height());
         cir.setReturnValue(self.level().noBlockCollision(self, box));
      }
   }
}
