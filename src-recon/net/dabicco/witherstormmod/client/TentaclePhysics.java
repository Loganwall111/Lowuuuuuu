package net.dabicco.witherstormmod.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.dabicco.witherstormmod.entity.state.WitherStormRenderState;
import net.dabicco.witherstormmod.mixin.ModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class TentaclePhysics {
   private static final int MIN_CHAIN = 8;
   private static final Map<ModelPart, List<net.dabicco.witherstormmod.client.TentaclePhysics.Chain>> CHAINS = new HashMap<>();
   private static final Map<Long, net.dabicco.witherstormmod.client.TentaclePhysics.Sim> SIMS = new HashMap<>();

   private static float gravity() {
      return (float)DabyWSClientConfig.verletGravity;
   }

   private static float sway() {
      return (float)DabyWSClientConfig.verletSway;
   }

   private static float damping() {
      return (float)DabyWSClientConfig.verletDamping;
   }

   private static float writhe() {
      return (float)DabyWSClientConfig.verletWrithe;
   }

   private static float writheSpeed() {
      return (float)DabyWSClientConfig.verletWritheSpeed;
   }

   static List<net.dabicco.witherstormmod.client.TentaclePhysics.Chain> chainsFor(ModelPart root) {
      return CHAINS.computeIfAbsent(root, net.dabicco.witherstormmod.client.TentaclePhysics::discoverChains);
   }

   private TentaclePhysics() {
   }

   public static void apply(ModelPart root, WitherStormRenderState state) {
      apply(root, state, 0);
   }

   public static void apply(ModelPart root, WitherStormRenderState state, int namespace) {
      apply(root, state, namespace, 0.0F);
   }

   public static void apply(ModelPart root, WitherStormRenderState state, int namespace, float maxDeviation) {
      List<net.dabicco.witherstormmod.client.TentaclePhysics.Chain> chains = chainsFor(root);
      if (!chains.isEmpty()) {
         Matrix3f model = new Matrix3f()
            .rotateY((float)Math.toRadians(180.0F - state.bodyRot))
            .rotateZ((float)Math.toRadians(-state.bodyRoll))
            .rotateX((float)Math.toRadians(state.xRot))
            .scale(-1.0F, -1.0F, 1.0F);
         Matrix3f inv = model.invert(new Matrix3f());
         Vector3f gravity = inv.transform(new Vector3f(0.0F, -1.0F, 0.0F)).mul(gravity());
         Vector3f sway = inv.transform(new Vector3f((float)state.velX, (float)state.velY, (float)state.velZ)).mul(-sway() * 16.0F);
         float now = state.idleTimeTicks;

         for (int c = 0; c < chains.size(); c++) {
            net.dabicco.witherstormmod.client.TentaclePhysics.Chain chain = chains.get(c);
            net.dabicco.witherstormmod.client.TentaclePhysics.Sim sim = SIMS.computeIfAbsent(
               (long)state.stormId << 12 | (long)namespace << 8 | c, k -> newSim(chain)
            );
            step(chain, sim, gravity, sway, now, c);
            if (maxDeviation > 0.0F) {
               limitReach(chain, sim, maxDeviation);
            }

            pose(chain, sim);
         }

         if (SIMS.size() > 64) {
            SIMS.clear();
         }
      }
   }

   public static void curlAndVanish(ModelPart root, float progress) {
      if (!(progress <= 0.0F)) {
         for (net.dabicco.witherstormmod.client.TentaclePhysics.Chain chain : chainsFor(root)) {
            float curl = progress * 0.3F;

            for (ModelPart bone : chain.bones) {
               bone.xRot += curl;
            }

            ModelPart base = chain.bones[0];
            float s = Math.max(0.0F, 1.0F - progress);
            base.xScale *= s;
            base.yScale *= s;
            base.zScale *= s;
         }
      }
   }

   private static net.dabicco.witherstormmod.client.TentaclePhysics.Sim newSim(net.dabicco.witherstormmod.client.TentaclePhysics.Chain chain) {
      net.dabicco.witherstormmod.client.TentaclePhysics.Sim sim = new net.dabicco.witherstormmod.client.TentaclePhysics.Sim();
      sim.pos = new Vector3f[chain.restPos.length];
      sim.prev = new Vector3f[chain.restPos.length];

      for (int i = 0; i < chain.restPos.length; i++) {
         sim.pos[i] = new Vector3f(chain.restPos[i]);
         sim.prev[i] = new Vector3f(chain.restPos[i]);
      }

      return sim;
   }

   private static void step(
      net.dabicco.witherstormmod.client.TentaclePhysics.Chain chain,
      net.dabicco.witherstormmod.client.TentaclePhysics.Sim sim,
      Vector3f gravity,
      Vector3f sway,
      float now,
      int chainIdx
   ) {
      float dt = Float.isNaN(sim.lastTime) ? 1.0F : Mth.clamp(now - sim.lastTime, 0.0F, 3.0F);
      sim.lastTime = now;
      if (!(dt <= 0.0F)) {
         float dt2 = dt * dt;
         int n = sim.pos.length;

         for (int i = 1; i < n; i++) {
            Vector3f p = sim.pos[i];
            Vector3f q = sim.prev[i];
            float along = (float)i / n;
            float amp = writhe() * (0.35F + 0.65F * along);
            float p1 = now * writheSpeed() - i * 0.16F + chainIdx * 2.1F;
            float p2 = now * writheSpeed() * 0.55F - i * 0.11F + chainIdx * 4.7F;
            float wx = (Mth.sin(p1) + 0.6F * Mth.sin(p2 + 0.9F)) * amp;
            float wz = (Mth.cos(p1 * 0.9F + 1.3F) + 0.6F * Mth.cos(p2)) * amp * 0.85F;
            float wy = Mth.sin(p2 + chainIdx) * amp * 0.6F;
            float nx = p.x + (p.x - q.x) * damping() + (gravity.x + sway.x + wx) * dt2;
            float ny = p.y + (p.y - q.y) * damping() + (gravity.y + sway.y + wy) * dt2;
            float nz = p.z + (p.z - q.z) * damping() + (gravity.z + sway.z + wz) * dt2;
            q.set(p);
            p.set(nx, ny, nz);
         }

         sim.pos[0].set(chain.restPos[0]);

         for (int pass = 0; pass < 3; pass++) {
            for (int i = 0; i < n - 1; i++) {
               Vector3f a = sim.pos[i];
               Vector3f b = sim.pos[i + 1];
               float dx = b.x - a.x;
               float dy = b.y - a.y;
               float dz = b.z - a.z;
               float dist = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
               if (!(dist < 1.0E-4F)) {
                  float corr = (dist - chain.segLen[i]) / dist;
                  b.sub(dx * corr, dy * corr, dz * corr);
               }
            }
         }
      }
   }

   private static void limitReach(
      net.dabicco.witherstormmod.client.TentaclePhysics.Chain chain, net.dabicco.witherstormmod.client.TentaclePhysics.Sim sim, float maxDeviation
   ) {
      float maxSq = maxDeviation * maxDeviation;

      for (int i = 1; i < sim.pos.length; i++) {
         Vector3f p = sim.pos[i];
         Vector3f rest = chain.restPos[i];
         float dx = p.x - rest.x;
         float dy = p.y - rest.y;
         float dz = p.z - rest.z;
         float d2 = dx * dx + dy * dy + dz * dz;
         if (!(d2 <= maxSq) && !(d2 < 1.0E-8F)) {
            float scale = maxDeviation / (float)Math.sqrt(d2);
            p.set(rest.x + dx * scale, rest.y + dy * scale, rest.z + dz * scale);
         }
      }

      int n = sim.pos.length;
      sim.pos[0].set(chain.restPos[0]);

      for (int pass = 0; pass < 2; pass++) {
         for (int ix = 0; ix < n - 1; ix++) {
            Vector3f a = sim.pos[ix];
            Vector3f b = sim.pos[ix + 1];
            float dx = b.x - a.x;
            float dy = b.y - a.y;
            float dz = b.z - a.z;
            float dist = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (!(dist < 1.0E-4F)) {
               float corr = (dist - chain.segLen[ix]) / dist;
               b.sub(dx * corr, dy * corr, dz * corr);
            }
         }
      }
   }

   private static void pose(net.dabicco.witherstormmod.client.TentaclePhysics.Chain chain, net.dabicco.witherstormmod.client.TentaclePhysics.Sim sim) {
      Quaternionf accum = new Quaternionf();
      Quaternionf q = new Quaternionf();
      Vector3f dir = new Vector3f();

      for (int i = 0; i < chain.bones.length; i++) {
         ModelPart bone = chain.bones[i];
         bone.resetPose();
         dir.set(sim.pos[i + 1]).sub(sim.pos[i]);
         if (dir.isFinite() && !(dir.lengthSquared() < 1.0E-6F)) {
            dir.normalize();
            accum.transformInverse(dir);
            q.rotationTo(chain.restDir[i], dir);
            bone.rotateBy(q);
            accum.mul(q);
         }
      }
   }

   private static List<net.dabicco.witherstormmod.client.TentaclePhysics.Chain> discoverChains(ModelPart root) {
      List<net.dabicco.witherstormmod.client.TentaclePhysics.Chain> out = new ArrayList<>();
      collect(root, out);
      return out;
   }

   private static Map<String, ModelPart> children(ModelPart part) {
      return ((ModelPartAccessor)(Object)part).getChildren();
   }

   private static void collect(ModelPart part, List<net.dabicco.witherstormmod.client.TentaclePhysics.Chain> out) {
      for (ModelPart child : children(part).values()) {
         List<ModelPart> run = new ArrayList<>();

         ModelPart cur;
         for (cur = child; children(cur).size() == 1; cur = children(cur).values().iterator().next()) {
            run.add(cur);
         }

         run.add(cur);
         if (run.size() >= 8) {
            out.add(buildChain(run));
         }

         collect(cur, out);
      }
   }

   private static net.dabicco.witherstormmod.client.TentaclePhysics.Chain buildChain(List<ModelPart> run) {
      net.dabicco.witherstormmod.client.TentaclePhysics.Chain chain = new net.dabicco.witherstormmod.client.TentaclePhysics.Chain();
      chain.bones = run.toArray(new ModelPart[0]);
      int n = chain.bones.length;
      chain.restPos = new Vector3f[n + 1];
      chain.restDir = new Vector3f[n];
      chain.segLen = new float[n];
      chain.restPos[0] = new Vector3f();
      Vector3f lastOffset = new Vector3f(0.0F, 2.0F, 0.0F);

      for (int i = 0; i < n; i++) {
         Vector3f off;
         if (i + 1 < n) {
            PartPose pose = chain.bones[i + 1].getInitialPose();
            off = new Vector3f(pose.x(), pose.y(), pose.z());
         } else {
            off = new Vector3f(lastOffset);
         }

         if (off.lengthSquared() < 1.0E-4F) {
            off = new Vector3f(lastOffset);
         }

         lastOffset = off;
         chain.segLen[i] = off.length();
         chain.totalLen = chain.totalLen + chain.segLen[i];
         chain.restDir[i] = new Vector3f(off).normalize();
         chain.restPos[i + 1] = new Vector3f(chain.restPos[i]).add(off);
      }

      return chain;
   }

   static final class Chain {
      ModelPart[] bones;
      Vector3f[] restPos;
      Vector3f[] restDir;
      float[] segLen;
      float totalLen;
   }

   private static final class Sim {
      Vector3f[] pos;
      Vector3f[] prev;
      float lastTime = Float.NaN;
   }
}
