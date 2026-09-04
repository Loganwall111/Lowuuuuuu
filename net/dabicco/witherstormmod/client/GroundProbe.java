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

         for(int i = 0; i < 8; ++i) {
            double a = (double)i * (Math.PI / 4D);
            int sx = Mth.floor(x + Math.cos(a) * radius);
            int sz = Mth.floor(z + Math.sin(a) * radius);
            height[i] = (float)level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, sx, sz);
            this.drop[i] = (float)((double)height[i] - y);
         }

         double gx = (double)0.0F;
         double gz = (double)0.0F;

         for(int i = 0; i < 8; ++i) {
            double a = (double)i * (Math.PI / 4D);
            gx += Math.cos(a) * (double)height[i];
            gz += Math.sin(a) * (double)height[i];
         }

         gx /= (double)4.0F * radius;
         gz /= (double)4.0F * radius;
         double rad = Math.toRadians((double)bodyYawDeg);
         double fx = -Math.sin(rad);
         double fz = Math.cos(rad);
         double rx = -fz;
         double alongForward = gx * fx + gz * fz;
         double alongRight = gx * rx + gz * fx;
         this.pitch = (float)Mth.clamp(Math.toDegrees(Math.atan(alongForward)), (double)-38.0F, (double)38.0F);
         this.roll = (float)Mth.clamp(Math.toDegrees(Math.atan(alongRight)), (double)-38.0F, (double)38.0F);
      }
   }

   public float bias(int i, float scale) {
      return Mth.clamp(this.drop[Math.floorMod(i, 8)] / scale, -1.0F, 1.0F);
   }
}
