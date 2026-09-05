package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.BowelsFrame;
import net.dabicco.witherstormmod.client.BowelsView;
import net.minecraft.client.Camera;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Camera.class})
public abstract class BowelsCameraMixin {
   @Shadow
   private Entity entity;
   @Shadow
   private float eyeHeight;
   @Shadow
   private float eyeHeightOld;
   @Shadow
   @Final
   private Quaternionf rotation;
   @Shadow
   @Final
   private Vector3f forwards;
   @Shadow
   @Final
   private Vector3f up;
   @Shadow
   @Final
   private Vector3f left;

   @Shadow
   protected abstract void setPosition(Vec3 var1);

   @Shadow
   public abstract Vec3 position();

   @Inject(
      method = {"setRotation(FF)V"},
      at = {@At("TAIL")}
   )
   private void dabyws$bowelsReframe(float yRot, float xRot, CallbackInfo ci) {
      if (this.entity != null) {
         Quaternionf frame = BowelsView.cameraFrame(BowelsFrame.boxAxis(this.entity));
         if (frame != null) {
            this.rotation.premul(frame);
            frame.transform(this.forwards);
            frame.transform(this.up);
            frame.transform(this.left);
         }
      }
   }

   @Inject(
      method = {"alignWithEntity(F)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
         shift = Shift.AFTER
      )}
   )
   private void dabyws$bowelsEye(float partialTick, CallbackInfo ci) {
      if (this.entity != null) {
         Direction gravity = BowelsFrame.boxAxis(this.entity);
         if (gravity != Direction.DOWN) {
            float reach = Mth.lerp(partialTick, this.eyeHeightOld, this.eyeHeight);
            Vec3 feet = this.entity.getPosition(partialTick);
            this.setPosition(feet.subtract(BowelsFrame.down(gravity).scale(reach)));
         }
      }
   }
}
