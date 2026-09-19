package net.dabicco.witherstormmod.client;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class GroundProbe {
   public static final int DIRS = 8;
   public float pitch;
   public float roll;
   public final float[] drop = new float[8];
   private long stamp = Long.MIN_VALUE;

   public void update(Level level, double x, double y, double z, float bodyYawDeg, double radius) {
      long now = level.getGameTime();
      if (now != this.stamp) {
         this.stamp = now;
         float[] height = new float[8];

         for (int i = 0; i < 8; i++) {
            double a = i * (Math.PI / 4);
            int sx = Mth.floor(x + Math.cos(a) * radius);
            int sz = Mth.floor(z + Math.sin(a) * radius);
            height[i] = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, sx, sz);
            this.drop[i] = (float)(height[i] - y);
         }

         double gx = 0.0;
         double gz = 0.0;

         for (int i = 0; i < 8; i++) {
            double a = i * (Math.PI / 4);
            gx += Math.cos(a) * height[i];
            gz += Math.sin(a) * height[i];
         }

         gx /= 4.0 * radius;
         gz /= 4.0 * radius;
         double rad = Math.toRadians(bodyYawDeg);
         double fx = -Math.sin(rad);
         double fz = Math.cos(rad);
         double rx = -fz;
         double alongForward = gx * fx + gz * fz;
         double alongRight = gx * rx + gz * fx;
         this.pitch = (float)Mth.clamp(Math.toDegrees(Math.atan(alongForward)), -38.0, 38.0);
         this.roll = (float)Mth.clamp(Math.toDegrees(Math.atan(alongRight)), -38.0, 38.0);
      }
   }

   public float bias(int i, float scale) {
      return Mth.clamp(this.drop[Math.floorMod(i, 8)] / scale, -1.0F, 1.0F);
   }
}
