package net.dabicco.witherstormmod.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class BigTentacleShape {
   public static final float HANG_AT = 0.1F;
   public static final float HANG_DROP = 1.42F;
   public static final float RECOVER_BY = 0.55F;
   public static final float TIP_LIFT = -0.62F;
   public static final float TIP_SIDE = 0.85F;
   public static final double LENGTH = 30.0;
   public static final double MOUNT_SIDE = 6.5;
   public static final double MOUNT_UP = 15.0;
   public static final double MOUNT_BACK = 2.0;

   private BigTentacleShape() {
   }

   public static float hangDrop(float along, float heave, float lift) {
      if (along <= 0.1F) {
         return (1.42F + heave) * smoothstep(along / 0.1F);
      } else {
         float u = (along - 0.1F) / Math.max(0.45000002F, 0.05F);
         return Mth.lerp(smoothstep(u), 1.42F + heave, -0.62F + lift);
      }
   }

   public static float hangSide(float along) {
      return 0.85F * smoothstep(along);
   }

   public static float smoothstep(float t) {
      t = Mth.clamp(t, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   public static Vec3[] path(int index, float timeTicks, double originX, double originY, double originZ, float yawDegrees, int samples) {
      float t = timeTicks * 0.026F + index * 2.3F;
      float heave = Mth.sin(t * 0.61F + index * 1.7F) * 0.1F + Mth.sin(t * 0.27F + 2.4F) * 0.05F;
      float lift = Mth.sin(t * 0.43F + index * 2.9F) * 0.17F;
      float swing = Mth.sin(t * 0.35F + index * 1.1F) * 0.2F;
      double side = index == 0 ? 1.0 : -1.0;
      double yaw = Math.toRadians(-yawDegrees);
      double cos = Math.cos(yaw);
      double sin = Math.sin(yaw);
      Vec3[] out = new Vec3[samples + 1];
      double lx = 6.5 * side;
      double ly = 15.0;
      double lz = 2.0;
      double step = 30.0 / samples;
      out[0] = toWorld(lx, ly, lz, cos, sin, originX, originY, originZ);

      for (int i = 1; i <= samples; i++) {
         float along = (float)i / samples;
         float drop = hangDrop(along, heave, lift);
         float bow = hangSide(along) * (float)(side + swing * side);
         double horizontal = Math.cos(drop);
         lx += step * horizontal * Math.cos(bow) * side;
         ly -= step * Math.sin(drop);
         lz += step * horizontal * Math.sin(bow);
         out[i] = toWorld(lx, ly, lz, cos, sin, originX, originY, originZ);
      }

      return out;
   }

   private static Vec3 toWorld(double lx, double ly, double lz, double cos, double sin, double ox, double oy, double oz) {
      return new Vec3(ox + lx * cos - lz * sin, oy + ly, oz + lx * sin + lz * cos);
   }
}
