package net.dabicco.witherstormmod.mixin;

import java.util.Optional;
import net.dabicco.witherstormmod.BowelsBody;
import net.dabicco.witherstormmod.BowelsFrame;
import net.dabicco.witherstormmod.BowelsGravity;
import net.dabicco.witherstormmod.BowelsTrace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public abstract class BowelsEntityMixin implements BowelsBody {
   @Shadow
   private EntityDimensions dimensions;
   @Unique
   private Direction dabyws$settledAxis;
   @Unique
   private Direction dabyws$runningAxis;
   @Unique
   private int dabyws$runningDepth;
   @Unique
   private Vec3 dabyws$movedFrom;
   @Unique
   private Vec3 dabyws$movedWanted;
   @Unique
   private Direction dabyws$pullLastTick;
   @Unique
   private boolean dabyws$owesLanding;

   public Direction dabyws$settled() {
      return this.dabyws$settledAxis;
   }

   public void dabyws$setSettled(Direction settled) {
      this.dabyws$settledAxis = settled;
   }

   public Direction dabyws$stepAxis() {
      return this.dabyws$runningAxis;
   }

   public void dabyws$setStepAxis(Direction axis) {
      this.dabyws$runningAxis = axis;
   }

   public int dabyws$stepDepth() {
      return this.dabyws$runningDepth;
   }

   public void dabyws$setStepDepth(int depth) {
      this.dabyws$runningDepth = depth;
   }

   public Vec3 dabyws$moveFrom() {
      return this.dabyws$movedFrom;
   }

   public void dabyws$setMoveFrom(Vec3 from) {
      this.dabyws$movedFrom = from;
   }

   public Vec3 dabyws$moveWanted() {
      return this.dabyws$movedWanted;
   }

   public void dabyws$setMoveWanted(Vec3 wanted) {
      this.dabyws$movedWanted = wanted;
   }

   public Direction dabyws$lastPull() {
      return this.dabyws$pullLastTick;
   }

   public void dabyws$setLastPull(Direction pull) {
      this.dabyws$pullLastTick = pull;
   }

   public boolean dabyws$turnoverFall() {
      return this.dabyws$owesLanding;
   }

   public void dabyws$setTurnoverFall(boolean falling) {
      this.dabyws$owesLanding = falling;
   }

   @Inject(
      method = {"makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$turnedBox(Vec3 feet, CallbackInfoReturnable<AABB> cir) {
      Entity self = (Entity)(Object)this;
      if (self instanceof LivingEntity) {
         Direction gravity = BowelsFrame.boxAxis(self);
         if (gravity != Direction.DOWN) {
            cir.setReturnValue(BowelsFrame.box(gravity, feet, this.dimensions.width(), this.dimensions.height()));
         }
      }
   }

   @ModifyVariable(
      method = {"move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private Vec3 dabyws$intoWorld(Vec3 movement) {
      var self = (net.dabicco.witherstormmod.mixin.BowelsEntityMixin & Entity)this;
      Direction gravity = BowelsFrame.active(self);
      if (gravity == null) {
         return movement;
      } else {
         self.setDeltaMovement(BowelsFrame.toWorld(gravity, self.getDeltaMovement()));
         Vec3 world = BowelsFrame.toWorld(gravity, movement);
         ((BowelsBody)self).dabyws$setMoveFrom(self.position());
         ((BowelsBody)self).dabyws$setMoveWanted(world);
         BowelsTrace.record(self, 3, self.getDeltaMovement(), false);
         return world;
      }
   }

   @Inject(
      method = {"move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"},
      at = {@At("RETURN")}
   )
   private void dabyws$backToFrame(MoverType type, Vec3 movement, CallbackInfo ci) {
      var self = (net.dabicco.witherstormmod.mixin.BowelsEntityMixin & Entity)this;
      Direction gravity = BowelsFrame.active(self);
      if (gravity != null) {
         self.setDeltaMovement(BowelsFrame.toFrame(gravity, self.getDeltaMovement()));
         BowelsTrace.record(self, 4, self.getDeltaMovement(), true);
         AABB probe = self.getBoundingBox().deflate(0.001).move(BowelsFrame.down(gravity).scale(0.01));
         self.setOnGround(!self.level().noCollision(probe));
         Vec3 from = ((BowelsBody)self).dabyws$moveFrom();
         Vec3 wanted = ((BowelsBody)self).dabyws$moveWanted();
         boolean blocked = false;
         if (from != null && wanted != null) {
            Vec3 got = BowelsFrame.toFrame(gravity, self.position().subtract(from));
            Vec3 asked = BowelsFrame.toFrame(gravity, wanted);
            double lost = (asked.x - got.x) * (asked.x - got.x) + (asked.z - got.z) * (asked.z - got.z);
            blocked = lost > 1.0E-6;
            double sank = asked.y - got.y;
            self.horizontalCollision = blocked;
            self.minorHorizontalCollision = blocked && self.minorHorizontalCollision;
            self.verticalCollision = Math.abs(sank) > 1.0E-6;
            self.verticalCollisionBelow = self.verticalCollision && asked.y < 0.0;
         }

         double lift = BowelsFrame.climb(self, gravity, blocked);
         if (lift > 0.0) {
            Vec3 up = BowelsFrame.toWorld(gravity, new Vec3(0.0, lift, 0.0));
            self.setPos(self.getX() + up.x, self.getY() + up.y, self.getZ() + up.z);
         }
      }
   }

   @Inject(
      method = {"maxUpStep()F"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$noVanillaStep(CallbackInfoReturnable<Float> cir) {
      Entity self = (Entity)(Object)this;
      if (BowelsFrame.boxAxis(self) != Direction.DOWN) {
         cir.setReturnValue(0.0F);
      }
   }

   @ModifyVariable(
      method = {"doCheckFallDamage(DDDZ)V"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 1
   )
   private double dabyws$fellAlongPull(double y) {
      var self = (net.dabicco.witherstormmod.mixin.BowelsEntityMixin & Entity)this;
      Direction gravity = BowelsFrame.boxAxis(self);
      if (gravity == Direction.DOWN) {
         return y;
      } else {
         Vec3 from = ((BowelsBody)self).dabyws$moveFrom();
         return from == null ? y : BowelsFrame.toFrame(gravity, self.position().subtract(from)).y;
      }
   }

   @ModifyVariable(
      method = {"setOnGroundWithMovement(ZZLnet/minecraft/world/phys/Vec3;)V"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private boolean dabyws$groundInFrame(boolean onGround) {
      Entity self = (Entity)(Object)this;
      if (!(self instanceof LivingEntity)) {
         return onGround;
      } else {
         Direction gravity = BowelsFrame.boxAxis(self);
         if (gravity == Direction.DOWN) {
            return onGround;
         } else {
            AABB probe = self.getBoundingBox().deflate(0.001).move(BowelsFrame.down(gravity).scale(0.01));
            return !self.level().noCollision(probe);
         }
      }
   }

   @Inject(
      method = {"checkSupportingBlock(ZLnet/minecraft/world/phys/Vec3;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$supportInFrame(boolean onGround, Vec3 movement, CallbackInfo ci) {
      Entity self = (Entity)(Object)this;
      Direction gravity = BowelsFrame.boxAxis(self);
      if (gravity != Direction.DOWN) {
         ci.cancel();
         self.mainSupportingBlockPos = onGround
            ? self.level().findSupportingBlock(self, BowelsFrame.footprint(gravity, self.getBoundingBox()))
            : Optional.empty();
      }
   }

   @Inject(
      method = {"getEyePosition()Lnet/minecraft/world/phys/Vec3;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$eye(CallbackInfoReturnable<Vec3> cir) {
      Entity self = (Entity)(Object)this;
      Direction gravity = BowelsFrame.boxAxis(self);
      if (gravity != Direction.DOWN) {
         cir.setReturnValue(BowelsFrame.eye(gravity, self.position(), self.getEyeHeight()));
      }
   }

   @Inject(
      method = {"getEyeY()D"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$eyeY(CallbackInfoReturnable<Double> cir) {
      Entity self = (Entity)(Object)this;
      Direction gravity = BowelsFrame.boxAxis(self);
      if (gravity != Direction.DOWN) {
         cir.setReturnValue(BowelsFrame.eye(gravity, self.position(), self.getEyeHeight()).y);
      }
   }

   @Inject(
      method = {"getEyePosition(F)Lnet/minecraft/world/phys/Vec3;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$eyeAt(float partialTick, CallbackInfoReturnable<Vec3> cir) {
      Entity self = (Entity)(Object)this;
      Direction gravity = BowelsFrame.boxAxis(self);
      if (gravity != Direction.DOWN) {
         cir.setReturnValue(BowelsFrame.eye(gravity, self.getPosition(partialTick), self.getEyeHeight()));
      }
   }

   @Inject(
      method = {"calculateViewVector(FF)Lnet/minecraft/world/phys/Vec3;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void dabyws$turnedLook(float xRot, float yRot, CallbackInfoReturnable<Vec3> cir) {
      Entity self = (Entity)(Object)this;
      if (BowelsFrame.active(self) == null) {
         Direction gravity = BowelsFrame.boxAxis(self);
         if (gravity != Direction.DOWN) {
            cir.setReturnValue(BowelsFrame.toWorld(gravity, (Vec3)cir.getReturnValue()));
         }
      }
   }

   @Inject(
      method = {"checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$noFallDamage(double dropped, boolean onGround, BlockState state, BlockPos pos, CallbackInfo ci) {
      Entity self = (Entity)(Object)this;
      if (BowelsGravity.isBowels(self.level())) {
         self.resetFallDistance();
         ci.cancel();
      }
   }
}
