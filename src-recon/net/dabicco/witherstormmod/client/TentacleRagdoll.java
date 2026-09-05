package net.dabicco.witherstormmod.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dabicco.witherstormmod.mixin.ModelPartAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class TentacleRagdoll {
   private static final float GRAVITY = 22.0F;
   private static final float DAMPING = 0.12F;
   private static final int RELAX_PASSES = 6;
   private static final float GROUND_FRICTION = 0.55F;
   private static final float MAX_STEP = 3.0F;
   private static final float MAX_DT = 0.05F;
   private static final int GROUND_CACHE_TICKS = 20;
   private static final float GROUND_CLEARANCE = 0.35F;
   private static final Map<Long, net.dabicco.witherstormmod.client.TentacleRagdoll.Rope> ROPES = new HashMap<>();
   private static Level boundLevel;
   private static final Map<ModelPart, Map<ModelPart, List<ModelPart>>> PATHS = new HashMap<>();

   private TentacleRagdoll() {
   }

   public static void settle(List<ModelPart> chain, long key, Matrix4f toWorld, Quaternionf ancestorQ, float amount) {
      if (!(amount <= 0.001F) && chain.size() >= 2) {
         Minecraft mc = Minecraft.getInstance();
         Level level = mc.level;
         if (level != null) {
            if (level != boundLevel) {
               boundLevel = level;
               ROPES.clear();
            }

            int n = chain.size() + 1;
            Vector3f[] rest = restLayout(chain, toWorld, ancestorQ);
            net.dabicco.witherstormmod.client.TentacleRagdoll.Rope rope = ROPES.get(key);
            if (rope == null || rope.x == null || rope.x.length != n) {
               rope = new net.dabicco.witherstormmod.client.TentacleRagdoll.Rope();
               rope.x = new float[n];
               rope.y = new float[n];
               rope.z = new float[n];
               rope.px = new float[n];
               rope.py = new float[n];
               rope.pz = new float[n];
               rope.len = new float[n - 1];
               rope.groundY = new float[n];
               rope.groundAt = new int[n];
               Arrays.fill(rope.groundAt, Integer.MIN_VALUE);
               if (ROPES.size() > 256) {
                  ROPES.clear();
               }

               ROPES.put(key, rope);
            }

            if (!rope.seeded) {
               for (int i = 0; i < n; i++) {
                  rope.x[i] = rope.px[i] = rest[i].x;
                  rope.y[i] = rope.py[i] = rest[i].y;
                  rope.z[i] = rope.pz[i] = rest[i].z;
               }

               rope.seeded = true;
            }

            for (int i = 0; i < n - 1; i++) {
               rope.len[i] = rest[i].distance(rest[i + 1]);
            }

            long now = Util.getMillis();
            float dt = rope.lastMillis == 0L ? 0.05F : Mth.clamp((float)(now - rope.lastMillis) / 1000.0F, 0.0F, 0.05F);
            rope.lastMillis = now;
            step(rope, rest, level, dt);
            apply(chain, rope, toWorld, ancestorQ, amount);
         }
      }
   }

   private static void step(net.dabicco.witherstormmod.client.TentacleRagdoll.Rope rope, Vector3f[] rest, Level level, float dt) {
      int n = rope.x.length;
      if (!(dt <= 0.0F)) {
         float keep = (float)Math.pow(0.12F, dt);
         float g = 22.0F * dt * dt;

         for (int i = 1; i < n; i++) {
            float vx = (rope.x[i] - rope.px[i]) * keep;
            float vy = (rope.y[i] - rope.py[i]) * keep;
            float vz = (rope.z[i] - rope.pz[i]) * keep;
            float speed = Mth.sqrt(vx * vx + vy * vy + vz * vz);
            if (speed > 3.0F) {
               float k = 3.0F / speed;
               vx *= k;
               vy *= k;
               vz *= k;
            }

            rope.px[i] = rope.x[i];
            rope.py[i] = rope.y[i];
            rope.pz[i] = rope.z[i];
            float[] var10000 = rope.x;
            var10000[i] += vx;
            var10000 = rope.y;
            var10000[i] += vy - g;
            var10000 = rope.z;
            var10000[i] += vz;
         }

         rope.px[0] = rope.x[0];
         rope.py[0] = rope.y[0];
         rope.pz[0] = rope.z[0];
         rope.x[0] = rest[0].x;
         rope.y[0] = rest[0].y;
         rope.z[0] = rest[0].z;

         for (int pass = 0; pass < 6; pass++) {
            for (int i = 0; i < n - 1; i++) {
               float dx = rope.x[i + 1] - rope.x[i];
               float dy = rope.y[i + 1] - rope.y[i];
               float dz = rope.z[i + 1] - rope.z[i];
               float d = Mth.sqrt(dx * dx + dy * dy + dz * dz);
               if (d < 1.0E-4F) {
                  dx = 0.0F;
                  dy = -1.0F;
                  dz = 0.0F;
                  d = 1.0F;
               }

               float corr = (d - rope.len[i]) / d;
               float wA = i == 0 ? 0.0F : 0.5F;
               float wB = 1.0F - wA;
               float[] var24 = rope.x;
               var24[i] += dx * corr * wA;
               var24 = rope.y;
               var24[i] += dy * corr * wA;
               var24 = rope.z;
               var24[i] += dz * corr * wA;
               var24 = rope.x;
               var24[i + 1] = var24[i + 1] - dx * corr * wB;
               var24 = rope.y;
               var24[i + 1] = var24[i + 1] - dy * corr * wB;
               var24 = rope.z;
               var24[i + 1] = var24[i + 1] - dz * corr * wB;
            }

            collide(rope, level);
         }
      }
   }

   private static void collide(net.dabicco.witherstormmod.client.TentacleRagdoll.Rope rope, Level level) {
      int tick = (int)(level.getGameTime() & 1073741823L);

      for (int i = 0; i < rope.x.length; i++) {
         if (tick - rope.groundAt[i] > 20) {
            rope.groundAt[i] = tick;
            rope.groundY[i] = groundAt(level, rope.x[i], rope.z[i]);
         }

         float floor = rope.groundY[i] + 0.35F;
         if (rope.y[i] < floor) {
            rope.y[i] = floor;
            rope.py[i] = rope.y[i];
            rope.px[i] = rope.x[i] - (rope.x[i] - rope.px[i]) * 0.55F;
            rope.pz[i] = rope.z[i] - (rope.z[i] - rope.pz[i]) * 0.55F;
         }
      }
   }

   private static float groundAt(Level level, float x, float z) {
      BlockPos pos = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(x, 0.0, z));
      return pos.getY();
   }

   private static Vector3f[] restLayout(List<ModelPart> chain, Matrix4f toWorld, Quaternionf ancestorQ) {
      int n = chain.size() + 1;
      Vector3f[] out = new Vector3f[n];
      Quaternionf q = new Quaternionf(ancestorQ);
      Vector3f at = new Vector3f();

      for (int i = 0; i < chain.size(); i++) {
         ModelPart bone = chain.get(i);
         Vector3f offset = new Vector3f(bone.x / 16.0F, bone.y / 16.0F, bone.z / 16.0F);
         q.transform(offset);
         at.add(offset);
         out[i] = new Vector3f(at);
         q.mul(new Quaternionf().rotationZYX(bone.zRot, bone.yRot, bone.xRot));
      }

      Vector3f tipDir = new Vector3f(0.0F, 1.0F, 0.0F);
      if (chain.size() >= 2) {
         ModelPart last = chain.get(chain.size() - 1);
         tipDir.set(last.x, last.y, last.z);
         if (tipDir.lengthSquared() < 1.0E-6F) {
            tipDir.set(0.0F, 1.0F, 0.0F);
         }
      }

      q.transform(tipDir.normalize().mul(tipLength(chain) / 16.0F));
      out[n - 1] = new Vector3f(at).add(tipDir);

      for (Vector3f v : out) {
         Vector4f w = toWorld.transform(new Vector4f(v.x, v.y, v.z, 1.0F));
         v.set(w.x, w.y, w.z);
      }

      return out;
   }

   private static float tipLength(List<ModelPart> chain) {
      ModelPart last = chain.get(chain.size() - 1);
      float l = Mth.sqrt(last.x * last.x + last.y * last.y + last.z * last.z);
      return l > 0.01F ? l : 6.0F;
   }

   private static void apply(
      List<ModelPart> chain, net.dabicco.witherstormmod.client.TentacleRagdoll.Rope rope, Matrix4f toWorld, Quaternionf ancestorQ, float amount
   ) {
      Matrix4f worldToPose = new Matrix4f(toWorld).invert();
      Quaternionf q = new Quaternionf(ancestorQ);
      Quaternionf inv = new Quaternionf();
      Vector3f desired = new Vector3f();
      Vector3f restDir = new Vector3f();
      Vector3f euler = new Vector3f();

      for (int i = 0; i < chain.size(); i++) {
         ModelPart bone = chain.get(i);
         Vector4f a = worldToPose.transform(new Vector4f(rope.x[i], rope.y[i], rope.z[i], 1.0F));
         Vector4f b = worldToPose.transform(new Vector4f(rope.x[i + 1], rope.y[i + 1], rope.z[i + 1], 1.0F));
         desired.set(b.x - a.x, b.y - a.y, b.z - a.z);
         if (!(desired.lengthSquared() < 1.0E-8F)) {
            desired.normalize();
            inv.set(q).conjugate().transform(desired);
            if (i + 1 < chain.size()) {
               ModelPart child = chain.get(i + 1);
               restDir.set(child.x, child.y, child.z);
            } else {
               restDir.set(bone.x, bone.y, bone.z);
            }

            if (restDir.lengthSquared() < 1.0E-6F) {
               restDir.set(0.0F, 1.0F, 0.0F);
            }

            restDir.normalize();
            Quaternionf want = new Quaternionf().rotationTo(restDir, desired);
            Quaternionf have = new Quaternionf().rotationZYX(bone.zRot, bone.yRot, bone.xRot);
            have.slerp(want, amount);
            have.getEulerAnglesZYX(euler);
            bone.xRot = euler.x;
            bone.yRot = euler.y;
            bone.zRot = euler.z;
            q.mul(have);
         }
      }
   }

   public static Matrix4f parentTransform(ModelPart modelRoot, ModelPart target, Quaternionf outQ) {
      List<ModelPart> path = PATHS.computeIfAbsent(modelRoot, rx -> new HashMap<>()).computeIfAbsent(target, t -> {
         List<ModelPart> acc = new ArrayList<>();
         return findPath(modelRoot, t, acc) ? acc : List.of();
      });
      if (path.isEmpty()) {
         return null;
      } else {
         Matrix4f m = new Matrix4f();
         outQ.identity();

         for (int i = 0; i < path.size() - 1; i++) {
            ModelPart p = path.get(i);
            m.translate(p.x / 16.0F, p.y / 16.0F, p.z / 16.0F);
            Quaternionf r = new Quaternionf().rotationZYX(p.zRot, p.yRot, p.xRot);
            m.rotate(r);
            outQ.mul(r);
         }

         return m;
      }
   }

   private static boolean findPath(ModelPart at, ModelPart target, List<ModelPart> acc) {
      acc.add(at);
      if (at == target) {
         return true;
      } else {
         for (ModelPart child : ((ModelPartAccessor)(Object)at).getChildren().values()) {
            if (findPath(child, target, acc)) {
               return true;
            }
         }

         acc.remove(acc.size() - 1);
         return false;
      }
   }

   private static final class Rope {
      float[] x;
      float[] y;
      float[] z;
      float[] px;
      float[] py;
      float[] pz;
      float[] len;
      float[] groundY;
      int[] groundAt;
      long lastMillis;
      boolean seeded;
   }
}
