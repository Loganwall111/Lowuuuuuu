package net.dabicco.witherstormmod.bowels;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class SeveredRope {
   private static final double DRAG = 0.94;
   private static final double GRAVITY = 0.055;
   private static final int RELAX = 6;
   private static final double FRICTION = 0.55;
   private static final double SUCK_DRAG = 0.975;
   private static final double SUCK = 0.06;
   private static final double SUCK_WEIGHT = 0.18;
   private final Vec3[] now;
   private final Vec3[] was;
   private final double[] length;

   public SeveredRope(Vec3[] worldPoints, Vec3 launch) {
      this.now = (Vec3[])worldPoints.clone();
      this.was = new Vec3[worldPoints.length];
      this.length = new double[Math.max(0, worldPoints.length - 1)];

      for (int i = 0; i < worldPoints.length; i++) {
         this.was[i] = worldPoints[i].subtract(launch);
      }

      for (int i = 0; i < this.length.length; i++) {
         this.length[i] = worldPoints[i].distanceTo(worldPoints[i + 1]);
      }
   }

   public Vec3[] points() {
      return this.now;
   }

   public boolean settled() {
      for (int i = 0; i < this.now.length; i++) {
         if (this.now[i].distanceToSqr(this.was[i]) > 1.0E-4) {
            return false;
         }
      }

      return true;
   }

   public void tick(Level level, double weight) {
      this.tick(level, weight, (Vec3)null, (Vec3)null);
   }

   public void tick(Level level, double weight, Vec3 mouth, Vec3 anchor) {
      boolean hauled = mouth != null && anchor != null;
      double gravity = 0.055 * (hauled ? 0.18 : 1.0);
      int last = Math.max(1, this.now.length - 1);

      for (int i = 0; i < this.now.length; i++) {
         Vec3 velocity = this.now[i].subtract(this.was[i]).scale(hauled ? 0.975 : 0.94);
         if (!hauled && this.onGround(level, this.now[i], weight)) {
            velocity = velocity.multiply(0.55, 1.0, 0.55);
         }

         Vec3 next = this.now[i].add(velocity).add(0.0, weight * gravity, 0.0);
         if (hauled) {
            Vec3 toward = mouth.subtract(this.now[i]);
            double d = toward.length();
            if (d > 1.0E-4) {
               next = next.add(toward.scale(0.06 * (1.0 - 0.8 * i / last) / d));
            }
         }

         this.was[i] = this.now[i];
         this.now[i] = next;
      }

      for (int pass = 0; pass < 6; pass++) {
         for (int i = 0; i < this.length.length; i++) {
            Vec3 a = this.now[i];
            Vec3 b = this.now[i + 1];
            Vec3 apart = b.subtract(a);
            double d = apart.length();
            if (!(d < 1.0E-6)) {
               double push = (d - this.length[i]) * 0.5;
               Vec3 step = apart.scale(push / d);
               this.now[i] = a.add(step);
               this.now[i + 1] = b.subtract(step);
            }
         }

         if (hauled) {
            this.now[0] = anchor;
         } else {
            this.collide(level, weight);
         }
      }
   }

   private void collide(Level level, double weight) {
      for (int i = 0; i < this.now.length; i++) {
         Vec3 p = this.now[i];
         BlockPos at = BlockPos.containing(p);
         if (!level.getBlockState(at).isAir() && level.getBlockState(at).isSolidRender()) {
            if (weight < 0.0) {
               double top = at.getY() + 1.02;
               if (p.y < top) {
                  this.now[i] = new Vec3(p.x, top, p.z);
               }
            } else {
               double under = at.getY() - 0.02;
               if (p.y > under) {
                  this.now[i] = new Vec3(p.x, under, p.z);
               }
            }
         }
      }
   }

   private boolean onGround(Level level, Vec3 p, double weight) {
      BlockPos below = BlockPos.containing(p.x, p.y + (weight < 0.0 ? -0.08 : 0.08), p.z);
      return level.getBlockState(below).isSolidRender();
   }

   public float[][] joints(int from) {
      float[][] out = new float[net.dabicco.witherstormmod.bowels.BowelsTentacleShape.BONES][2];
      float lastPitch = 0.0F;
      float lastYaw = 0.0F;

      for (int i = 0; i + 1 < this.now.length; i++) {
         int bone = from + i;
         if (bone >= out.length) {
            break;
         }

         Vec3 d = this.now[i + 1].subtract(this.now[i]);
         double len = d.length();
         if (!(len < 1.0E-6)) {
            float pitch = (float)(-Math.asin(Mth.clamp(d.y / len, -1.0, 1.0)));
            float yaw = (float)Math.atan2(d.x, d.z);
            out[bone][0] = pitch - lastPitch;
            out[bone][1] = Mth.degreesDifference((float)Math.toDegrees(lastYaw), (float)Math.toDegrees(yaw)) * (float) (Math.PI / 180.0);
            lastPitch = pitch;
            lastYaw = yaw;
         }
      }

      return out;
   }
}
