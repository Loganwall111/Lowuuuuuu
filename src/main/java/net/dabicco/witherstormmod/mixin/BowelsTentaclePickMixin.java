package net.dabicco.witherstormmod.mixin;

import java.util.List;
import java.util.function.Predicate;
import net.dabicco.witherstormmod.bowels.BowelsTentacleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ProjectileUtil.class})
public abstract class BowelsTentaclePickMixin {
   @Inject(
      method = {"getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private static void dabyws$pickTheLimbYouAreAimingAt(
      Level level, Entity from, Vec3 eye, Vec3 end, AABB box, Predicate<Entity> filter, float margin, CallbackInfoReturnable<EntityHitResult> cir
   ) {
      EntityHitResult hit = (EntityHitResult)cir.getReturnValue();
      if (hit != null && hit.getEntity() instanceof BowelsTentacleEntity) {
         List<Entity> nearby = level.getEntities(from, box, filter);
         if (nearby.size() >= 2) {
            Vec3 along = end.subtract(eye);
            double span = along.length();
            if (!(span < 1.0E-6)) {
               Vec3 look = along.scale(1.0 / span);
               BowelsTentacleEntity best = null;
               double bestOff = Double.MAX_VALUE;

               for (Entity candidate : nearby) {
                  if (candidate instanceof BowelsTentacleEntity) {
                     BowelsTentacleEntity limb = (BowelsTentacleEntity)candidate;
                     if (!limb.getBoundingBox().inflate((double)margin).clip(eye, end).isEmpty()) {
                        double off = dabyws$aimOffset(limb, eye, look, span);
                        if (off < bestOff) {
                           bestOff = off;
                           best = limb;
                        }
                     }
                  }
               }

               if (best != null && best != hit.getEntity()) {
                  cir.setReturnValue(new EntityHitResult(best, dabyws$aimPoint(best, eye, look, span)));
               }
            }
         }
      }
   }

   private static double dabyws$aimOffset(BowelsTentacleEntity limb, Vec3 eye, Vec3 look, double span) {
      Vec3 near = dabyws$aimPoint(limb, eye, look, span);
      double at = near.subtract(eye).dot(look);
      return near.distanceToSqr(eye.add(look.scale(at)));
   }

   private static Vec3 dabyws$aimPoint(BowelsTentacleEntity limb, Vec3 eye, Vec3 look, double span) {
      Vec3[] path = limb.aimPath();
      Vec3 best = path[path.length - 1];
      double bestOff = Double.MAX_VALUE;

      for (int i = 1; i < path.length; i++) {
         for (int step = 0; step <= 4; step++) {
            Vec3 p = path[i - 1].lerp(path[i], (double)step / 4.0);
            double at = p.subtract(eye).dot(look);
            if (!(at < 0.0) && !(at > span)) {
               double off = p.distanceToSqr(eye.add(look.scale(at)));
               if (off < bestOff) {
                  bestOff = off;
                  best = p;
               }
            }
         }
      }

      return best;
   }
}
