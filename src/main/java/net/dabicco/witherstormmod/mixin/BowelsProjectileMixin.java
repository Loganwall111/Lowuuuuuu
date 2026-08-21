package net.dabicco.witherstormmod.mixin;

import net.dabicco.witherstormmod.BowelsFrame;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Projectile.class})
public abstract class BowelsProjectileMixin {
   @Shadow
   public abstract void shoot(double var1, double var3, double var5, float var7, float var8);

   @Inject(
      method = {"shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void dabyws$shootInFrame(Entity shooter, float xRot, float yRot, float zRot, float velocity, float inaccuracy, CallbackInfo ci) {
      Direction gravity = BowelsFrame.boxAxis(shooter);
      if (gravity != Direction.DOWN) {
         Projectile self = (Projectile)(Object)this;
         Vec3 from = shooter.getEyePosition().add(BowelsFrame.down(gravity).scale(0.1));
         self.setPos(from.x, from.y, from.z);
         float pitch = xRot * (float) (Math.PI / 180.0);
         float yaw = yRot * (float) (Math.PI / 180.0);
         float lean = (xRot + zRot) * (float) (Math.PI / 180.0);
         Vec3 aim = BowelsFrame.toWorld(
            gravity,
            new Vec3(
               (double)(-Mth.sin((double)yaw) * Mth.cos((double)pitch)),
               (double)(-Mth.sin((double)lean)),
               (double)(Mth.cos((double)yaw) * Mth.cos((double)pitch))
            )
         );
         this.shoot(aim.x, aim.y, aim.z, velocity, inaccuracy);
         ci.cancel();
      }
   }
}
