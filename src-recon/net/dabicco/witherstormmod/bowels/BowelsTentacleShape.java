package net.dabicco.witherstormmod.bowels;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class BowelsTentacleShape {
   public static final float[] BONE_LEN = new float[]{12.0F, 8.0F, 16.0F, 18.1F, 17.9F, 10.0046F, 15.9954F, 7.0F, 10.0F, 7.0F};
   public static final int BONES = BONE_LEN.length + 1;
   private static final float[][] PIVOT = new float[][]{
      {0.0F, 0.0F, 0.0F},
      {2.0F, 1.0F, 12.0F},
      {1.0F, 3.0F, 8.0F},
      {-1.0F, 3.0F, 16.0F},
      {-1.0F, -3.0F, 18.1F},
      {0.0F, 0.0F, 17.9F},
      {-0.94F, -2.0014F, 10.0046F},
      {1.54F, -0.9986F, 15.9954F},
      {0.0F, 1.0F, 7.0F},
      {0.4F, -1.0F, 10.0F},
      {-1.5F, 0.5F, 7.0F}
   };
   private static final float HOOK_KNEE = 0.2F;
   private static final float HOOK_KNEE_OUT = 0.55F;
   private static final float HOOK_BAND = 0.1F;
   public static final double CLASP_REACH = 2.682;
   public static final double CLASP_TIP_UP = -0.178;
   public static final double NATURAL_LENGTH;
   private static final float REACH_FLOOR = 0.45F;
   private static final float RIGID_BAND = 0.08F;
   private static final float RIGID_FLOOR = 0.1F;
   private static final float COIL_FROM = 0.72F;
   private static final float COIL_PITCH = 0.62F;
   private static final float COIL_YAW = 0.78F;
   private static final int WRAP_KNEE = 3;
   private static final float WRAP_BASE = -1.34F;
   private static final float WRAP_PITCH = 0.5F;
   private static final float WRAP_YAW = 1.46F;

   private BowelsTentacleShape() {
   }

   public static double reachTo(int bone) {
      double at = 0.0;

      for (int i = 0; i < Math.min(bone, BONE_LEN.length); i++) {
         at += BONE_LEN[i] / 16.0;
      }

      return at;
   }

   public static float[][] joints(int bones, float curl, float time, float phase, float aimPitch, float aimYaw, float aim, float open, float sway) {
      return joints(bones, curl, time, phase, aimPitch, aimYaw, aim, open, sway, 0.0F);
   }

   public static float[][] joints(int bones, float curl, float time, float phase, float aimPitch, float aimYaw, float aim, float open, float sway, float rigid) {
      return joints(bones, curl, time, phase, aimPitch, aimYaw, aim, open, sway, rigid, 0.0F);
   }

   public static float[][] joints(
      int bones, float curl, float time, float phase, float aimPitch, float aimYaw, float aim, float open, float sway, float rigid, float coil
   ) {
      return joints(bones, curl, time, phase, aimPitch, aimYaw, aim, open, sway, rigid, coil, 0.0F);
   }

   public static float[][] joints(
      int bones, float curl, float time, float phase, float aimPitch, float aimYaw, float aim, float open, float sway, float rigid, float coil, float wrap
   ) {
      bones = Mth.clamp(bones, 1, BONES);
      float[][] out = new float[BONES][2];

      for (int i = 0; i < bones; i++) {
         float along = (float)i / (BONES - 1);
         float hookShape = smoothstep((along - 0.2F) / 0.1F) * (1.0F - smoothstep((along - 0.55F) / 0.1F));
         float clasped = i == 0 ? -1.54F : 0.808F * hookShape;
         float unclasped = i == 0 ? -1.585F : 0.08F * hookShape;
         float guardX = Mth.lerp(open, clasped, unclasped);
         float guardY = 0.0F;
         float wave = time * 0.075F + phase;
         float huntX = Mth.lerp(along, -0.155F, 0.055F) + Mth.sin(wave - i * 0.55F) * 0.085F;
         float huntY = Mth.cos(wave * 0.8F - i * 0.4F + 0.5F) * 0.11F;
         float x = Mth.lerp(curl, huntX, guardX);
         float y = Mth.lerp(curl, huntY, guardY);
         if (wrap > 0.0F) {
            boolean past = i >= 3;
            float wx = i == 0 ? -1.34F : (past ? 0.5F : 0.0F);
            float wy = i == 0 ? 0.0F : (past ? 1.46F : 0.0F);
            x = Mth.lerp(wrap, x, wx);
            y = Mth.lerp(wrap, y, wy);
         }

         if (aim > 0.0F && i > 0) {
            float perJoint = (aimPitch - out[0][0]) / (bones - 1);
            x = Mth.lerp(aim, x, perJoint);
            y = Mth.lerp(aim, y, aimYaw / (bones - 1));
         }

         if (sway > 0.0F) {
            float rate = (0.082F + Math.abs(phase) % 1.0F * 0.03F) * (1.0F + 0.55F * Math.max(0.0F, sway - 1.0F));
            float slow = Mth.sin(time * rate + phase + i * 0.3F);
            float fast = Mth.sin(time * rate * 2.37F + phase * 2.1F + i * 0.52F);
            float lash = snap(slow * 0.66F + fast * 0.34F);
            float reach = rigid > 0.0F ? 0.45F + 0.55F * Mth.clamp((along - rigid) / Math.max(0.001F, 1.0F - rigid), 0.0F, 1.0F) : along;
            y += (lash * 0.34F + bias(phase)) * sway * reach;
            float rear = rigid > 0.0F ? 0.0F : 0.055F;
            x -= (rear + snap(Mth.sin(time * rate * 1.63F + phase * 1.3F + i * 0.22F)) * 0.2F) * sway * reach;
         }

         if (rigid > 0.0F) {
            float free = 0.1F + 0.9F * smoothstep((along - rigid) / 0.08F);
            x *= free;
            y *= free;
         }

         if (coil > 0.0F) {
            float tail = Mth.clamp((along - 0.72F) / 0.27999997F, 0.0F, 1.0F);
            if (tail > 0.0F) {
               x += 0.62F * tail * coil;
               y += 0.78F * tail * coil;
            }
         }

         out[i][0] = x;
         out[i][1] = y;
      }

      return out;
   }

   public static Vec3[] path(int bones, float scale, float[][] joints) {
      bones = Mth.clamp(bones, 1, BONES);
      Vec3[] out = new Vec3[bones];
      double[] m = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
      double px = 0.0;
      double py = 0.0;
      double pz = 0.0;
      out[0] = Vec3.ZERO;

      for (int i = 0; i + 1 < bones; i++) {
         m = mulYX(m, joints[i][1], joints[i][0]);
         double ox = PIVOT[i + 1][0] / 16.0 * scale;
         double oy = PIVOT[i + 1][1] / 16.0 * scale;
         double oz = PIVOT[i + 1][2] / 16.0 * scale;
         px += m[0] * ox + m[1] * oy + m[2] * oz;
         py += m[3] * ox + m[4] * oy + m[5] * oz;
         pz += m[6] * ox + m[7] * oy + m[8] * oz;
         out[i + 1] = new Vec3(px, py, pz);
      }

      return out;
   }

   private static double[] mulYX(double[] a, double yaw, double pitch) {
      double cy = Math.cos(yaw);
      double sy = Math.sin(yaw);
      double cx = Math.cos(pitch);
      double sx = Math.sin(pitch);
      double[] r = new double[]{cy, sy * sx, sy * cx, 0.0, cx, -sx, -sy, cy * sx, cy * cx};
      double[] o = new double[9];

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 3; col++) {
            o[row * 3 + col] = a[row * 3] * r[col] + a[row * 3 + 1] * r[3 + col] + a[row * 3 + 2] * r[6 + col];
         }
      }

      return o;
   }

   public static Vec3[] toWorld(Vec3[] local, double originX, double originY, double originZ, float yawDegrees) {
      double yaw = Math.toRadians(yawDegrees);
      double cos = Math.cos(yaw);
      double sin = Math.sin(yaw);
      Vec3[] out = new Vec3[local.length];

      for (int i = 0; i < local.length; i++) {
         Vec3 p = local[i];
         out[i] = new Vec3(originX + p.x * cos - p.z * sin, originY + p.y, originZ + p.x * sin + p.z * cos);
      }

      return out;
   }

   public static Vec3 toLocal(double originX, double originY, double originZ, float yawDegrees, double wx, double wy, double wz) {
      double yaw = Math.toRadians(yawDegrees);
      double cos = Math.cos(yaw);
      double sin = Math.sin(yaw);
      double dx = wx - originX;
      double dy = wy - originY;
      double dz = wz - originZ;
      return new Vec3(dx * cos + dz * sin, dy, -dx * sin + dz * cos);
   }

   public static float[] aimAngles(Vec3 local) {
      double len = local.length();
      return len < 1.0E-4
         ? new float[]{0.0F, 0.0F}
         : new float[]{(float)(-Math.asin(Mth.clamp(local.y / len, -1.0, 1.0))), (float)Math.atan2(local.x, local.z)};
   }

   private static float snap(float v) {
      return Mth.clamp(v * (1.55F - 0.55F * v * v), -1.0F, 1.0F);
   }

   private static float bias(float phase) {
      return (Math.abs(phase) * 0.618F % 1.0F - 0.5F) * 0.42F;
   }

   public static float smoothstep(float t) {
      t = Mth.clamp(t, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   static {
      double total = 0.0;

      for (float len : BONE_LEN) {
         total += len / 16.0;
      }

      NATURAL_LENGTH = total;
   }
}
