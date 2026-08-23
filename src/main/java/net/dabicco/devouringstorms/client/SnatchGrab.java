package net.dabicco.devouringstorms.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.dabicco.devouringstorms.entity.state.WitherStormRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SnatchGrab {
   private static final float BLEND_PER_FRAME = 0.12F;
   private static final float AIM_SMOOTH = 0.25F;
   private static final Map<Integer, Grab> GRABS = new HashMap();

   private SnatchGrab() {
   }

   public static void apply(ModelPart root, WitherStormRenderState state) {
      Grab grab = (Grab)GRABS.get(state.stormId);
      if (state.snatchActive || grab != null && !(grab.blend <= 0.0F)) {
         List<TentaclePhysics.Chain> chains = TentaclePhysics.chainsFor(root);
         if (!chains.isEmpty()) {
            if (grab == null) {
               grab = new Grab();
               GRABS.put(state.stormId, grab);
            }

            Matrix3f model = (new Matrix3f()).rotateY((float)Math.toRadians((double)(180.0F - state.bodyRot))).rotateZ((float)Math.toRadians((double)(-state.bodyRoll))).rotateX((float)Math.toRadians((double)state.xRot)).scale(-1.0F, -1.0F, 1.0F);
            Matrix3f inv = model.invert(new Matrix3f());
            if (state.snatchActive) {
               Vector3f target = inv.transform(new Vector3f((float)state.snatchRelX, (float)state.snatchRelY, (float)state.snatchRelZ));
               if (target.lengthSquared() > 1.0E-4F) {
                  target.normalize();
                  if (grab.chainIdx < 0) {
                     grab.chainIdx = pickChain(chains, target);
                     grab.aim.set(target);
                  }

                  grab.aim.lerp(target, 0.25F).normalize();
                  grab.blend = Math.min(1.0F, grab.blend + 0.12F);
               }
            } else {
               grab.blend = Math.max(0.0F, grab.blend - 0.12F);
            }

            if (grab.chainIdx >= 0 && grab.chainIdx < chains.size() && !(grab.blend <= 0.001F)) {
               reachChain((TentaclePhysics.Chain)chains.get(grab.chainIdx), grab.aim, grab.blend, state.idleTimeTicks);
               if (GRABS.size() > 32) {
                  GRABS.clear();
               }

            }
         }
      } else {
         if (grab != null) {
            GRABS.remove(state.stormId);
         }

      }
   }

   public static void reachUnderStorm(ModelPart tentaclesRoot, WitherStormRenderState state) {
      if (state.snatchActive) {
         List<TentaclePhysics.Chain> chains = TentaclePhysics.chainsFor(tentaclesRoot);
         if (!chains.isEmpty()) {
            Matrix3f model = (new Matrix3f()).rotateY((float)Math.toRadians((double)(180.0F - state.bodyRot))).rotateZ((float)Math.toRadians((double)(-state.bodyRoll))).scale(-1.0F, -1.0F, 1.0F);
            Vector3f aim = model.invert(new Matrix3f()).transform(new Vector3f((float)state.snatchRelX, (float)state.snatchRelY, (float)state.snatchRelZ));
            if (!(aim.lengthSquared() < 1.0E-4F)) {
               aim.normalize();
               int idx = pickChain(chains, aim);
               if (idx >= 0) {
                  reachChain((TentaclePhysics.Chain)chains.get(idx), aim, 1.0F, state.idleTimeTicks);
               }
            }
         }
      }
   }

   private static int pickChain(List<TentaclePhysics.Chain> chains, Vector3f targetDir) {
      float maxLen = 0.0F;

      for(TentaclePhysics.Chain c : chains) {
         maxLen = Math.max(maxLen, c.totalLen);
      }

      int best = -1;
      float bestScore = -Float.MAX_VALUE;
      Vector3f pivot = new Vector3f();

      for(int i = 0; i < chains.size(); ++i) {
         TentaclePhysics.Chain c = (TentaclePhysics.Chain)chains.get(i);
         if (!(c.totalLen < maxLen * 0.6F)) {
            PartPose pose = c.bones[0].getInitialPose();
            pivot.set(pose.x(), pose.y(), pose.z());
            float score = pivot.lengthSquared() < 0.001F ? 0.0F : pivot.normalize().dot(targetDir);
            if (score > bestScore) {
               bestScore = score;
               best = i;
            }
         }
      }

      return best;
   }

   private static void reachChain(TentaclePhysics.Chain chain, Vector3f aimDir, float blend, float timeTicks) {
      int n = chain.bones.length;
      Vector3f[] node = new Vector3f[n + 1];
      node[0] = new Vector3f(chain.restPos[0]);
      Vector3f restSeg = new Vector3f();
      Vector3f dir = new Vector3f();

      for(int i = 0; i < n; ++i) {
         restSeg.set(chain.restPos[i + 1]).sub(chain.restPos[i]);
         if (restSeg.lengthSquared() < 1.0E-6F) {
            restSeg.set(0.0F, 1.0F, 0.0F);
         }

         restSeg.normalize();
         float along = n > 1 ? (float)i / (float)(n - 1) : 1.0F;
         float curl = blend * (0.15F + 0.85F * along);
         float w = 0.06F * Mth.sin((double)(timeTicks * 0.2F + (float)i * 0.5F)) * blend;
         dir.set(restSeg).lerp(aimDir, curl);
         if (dir.lengthSquared() < 1.0E-6F) {
            dir.set(restSeg);
         }

         dir.normalize().rotateX(w).rotateZ(-w * 0.7F).normalize();
         node[i + 1] = (new Vector3f(node[i])).add(dir.x * chain.segLen[i], dir.y * chain.segLen[i], dir.z * chain.segLen[i]);
      }

      Quaternionf accum = new Quaternionf();
      Quaternionf q = new Quaternionf();
      Vector3f d = new Vector3f();

      for(int i = 0; i < n; ++i) {
         ModelPart bone = chain.bones[i];
         bone.resetPose();
         d.set(node[i + 1]).sub(node[i]);
         if (d.isFinite() && !(d.lengthSquared() < 1.0E-6F)) {
            d.normalize();
            accum.transformInverse(d);
            q.rotationTo(chain.restDir[i], d);
            bone.rotateBy(q);
            accum.mul(q);
         }
      }

   }

   private static final class Grab {
      float blend;
      int chainIdx = -1;
      final Vector3f aim = new Vector3f(0.0F, 1.0F, 0.0F);
   }
}
