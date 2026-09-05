package net.dabicco.witherstormmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class GrappleAnchor {
   private GrappleAnchor() {
   }

   public static Vec3 muzzle(Entity owner, float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      boolean firstPersonSelf = owner == mc.player && mc.options.getCameraType().isFirstPerson();
      Vec3 eye = owner.getEyePosition(partialTick);
      Vec3 look = owner.getViewVector(partialTick).normalize();
      if (firstPersonSelf) {
         Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
         return eye.add(look.scale(0.05)).add(right.scale(0.06)).add(0.0, -0.45, 0.0);
      } else {
         float var10000;
         if (owner instanceof LivingEntity le) {
            var10000 = Mth.rotLerp(partialTick, le.yBodyRotO, le.yBodyRot);
         } else {
            var10000 = owner.getYRot();
         }

         double yawRad = Math.toRadians(var10000);
         Vec3 forward = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad));
         Vec3 right = new Vec3(Math.cos(yawRad), 0.0, Math.sin(yawRad));
         double shoulderY = owner.getY() + owner.getEyeHeight() * 0.86;
         Vec3 hand = new Vec3(owner.getX(), shoulderY, owner.getZ()).add(right.scale(0.38)).add(forward.scale(0.22));
         return hand.add(look.scale(0.45));
      }
   }
}
