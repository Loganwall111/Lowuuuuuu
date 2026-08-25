package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * StormMutationFlash — the LOCALIZED purple flash-bang for phase 6+.
 *
 * When the storm mutates past the split (phase 6) and again at 7 and 8, and at
 * slow intervals while it rampages, a bright purple flash bursts around the
 * storm's core - the way the mutating mass crackles in the Story Mode shots.
 *
 * Deliberately NOT a screen flash:
 *  - everything renders in WORLD space, anchored to the storm core, so the
 *    burst is a local event at the storm, not an overlay;
 *  - it uses the additive glow pipelines that DEPTH-TEST without writing
 *    depth, so hills, terrain and buildings in front of the storm occlude it
 *    - it can never light up the player's whole screen;
 *  - it never touches the lightmap, fog or sky dome - it is a separate effect
 *    from the purple anomaly skybox, it just shares the colour family.
 */
public final class StormMutationFlash {
   private static final Identifier GLOW_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/halo_gradient.png");
   private static final Identifier RING_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/halo_ring.png");
   private static final int FULL_BRIGHT = 15728880;
   /** Flashes live for this many ticks after the bang. */
   private static final float LIFE_TICKS = 26.0F;
   /** Ticks from bang to full brightness. */
   private static final float ATTACK_TICKS = 2.0F;
   /** Past this distance from the player the flash is not worth drawing. */
   private static final double MAX_DIST = 1600.0;
   /** Mutation purple with a hot magenta core edge. */
   private static final int[] CORE_COLOR = new int[]{186, 62, 255};
   private static final int[] EDGE_COLOR = new int[]{255, 92, 214};

   /** One active burst. */
   private static final class Flash {
      final double x;
      final double y;
      final double z;
      final float strength;
      final double bodyR;
      final long startMs;

      Flash(double x, double y, double z, float strength, double bodyR, long startMs) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.strength = strength;
         this.bodyR = bodyR;
         this.startMs = startMs;
      }
   }

   private static final Flash[] FLASHES = new Flash[6];
   /** Last seen phase per storm id, to detect mutation threshold crossings. */
   private static final Map<Integer, Float> LAST_PHASE = new HashMap<>();

   private StormMutationFlash() {
   }

   /** Approximate visual body radius for a phase (kept in sync with StormPresenceFX). */
   private static double bodyRadius(float phase, float expansionPhase) {
      double base;
      if (phase < 4.0F) {
         base = 4.0 + phase * 1.5;
      } else if (phase < 5.0F) {
         base = 10.0 + (phase - 4.0F) * 12.0;
      } else {
         base = 22.0 + Math.min(phase - 5.0F, 1.99F) * 9.0;
      }

      return base * WitherStormEntity.clientGrowthScaleForPhase(Math.max(phase, expansionPhase));
   }

   /** Fire a localized purple burst at a storm's core. */
   public static void trigger(double x, double y, double z, float phase, float strength) {
      // recycle the oldest slot
      int slot = 0;
      long oldest = Long.MAX_VALUE;
      for (int i = 0; i < FLASHES.length; i++) {
         if (FLASHES[i] == null) {
            slot = i;
            oldest = Long.MIN_VALUE;
            break;
         }

         if (FLASHES[i].startMs < oldest) {
            oldest = FLASHES[i].startMs;
            slot = i;
         }
      }

      double bodyR = bodyRadius(phase, phase);
      FLASHES[slot] = new Flash(x, y + bodyR * 0.45, z, Mth.clamp(strength, 0.0F, 1.0F), bodyR, nowMs());
   }

   private static long nowMs() {
      return System.currentTimeMillis();
   }

   /** Client tick: watches storm phases and fires the mutation bursts. */
   public static void tick(Minecraft mc) {
      if (mc.level == null || mc.isPaused()) {
         return;
      }
      if (!DevouringStormsClientConfig.mutationFlashBang) {
         LAST_PHASE.clear();
         return;
      }

      long gameTime = mc.level.getGameTime();
      boolean anyStorm = false;

      for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         if (d.phase < 5.9F) {
            LAST_PHASE.remove(d.entityId);
            continue;
         }
         anyStorm = true;

         Float last = LAST_PHASE.get(d.entityId);
         float now = d.phase;
         if (last != null) {
            // mutation threshold crossings: the split at 6, then 7 and 8
            if (last < 6.0F && now >= 6.0F) {
               trigger(d.x, d.y, d.z, now, 1.0F);
            } else if (last < 7.0F && now >= 7.0F) {
               trigger(d.x, d.y, d.z, now, 0.85F);
            } else if (last < 8.0F && now >= 8.0F) {
               trigger(d.x, d.y, d.z, now, 0.9F);
            }
         }

         LAST_PHASE.put(d.entityId, now);

         // slow background crackle while the mutated storm rampages: one burst
         // every ~16-22 seconds per storm, jittered by storm id
         float ramp = Mth.clamp((now - 5.9F) / 0.6F, 0.0F, 1.0F);
         long period = 320L + (long)(d.entityId % 7) * 30L;
         if (ramp > 0.5F && (gameTime + (long)d.entityId * 71L) % period == 0L) {
            trigger(d.x, d.y, d.z, now, 0.45F + 0.3F * ramp);
         }
      }

      if (!anyStorm) {
         LAST_PHASE.clear();
      }
   }

   /** Brightness envelope: fast attack, quadratic decay. */
   private static float envelope(float ticks) {
      if (ticks <= ATTACK_TICKS) {
         return ticks / ATTACK_TICKS;
      }
      float t = (ticks - ATTACK_TICKS) / (LIFE_TICKS - ATTACK_TICKS);
      if (t >= 1.0F) {
         return 0.0F;
      }
      float fade = 1.0F - t;
      return fade * fade;
   }

   public static void submit(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || !DevouringStormsClientConfig.mutationFlashBang) {
         return;
      }
      long now = nowMs();
      boolean alive = false;

      for (int i = 0; i < FLASHES.length; i++) {
         Flash f = FLASHES[i];
         if (f == null) {
            continue;
         }
         float ticks = (float)(now - f.startMs) / 50.0F;
         if (ticks > LIFE_TICKS) {
            FLASHES[i] = null;
            continue;
         }
         alive = true;
      }

      if (!alive) {
         return;
      }

      Vec3 cam = ctx.levelState().cameraRenderState.pos;
      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();

      for (Flash f : FLASHES) {
         if (f == null) {
            continue;
         }
         float ticks = (float)(nowMs() - f.startMs) / 50.0F;
         float env = envelope(ticks);
         if (env <= 0.01F) {
            continue;
         }

         double dist = Math.sqrt((f.x - cam.x) * (f.x - cam.x) + (f.z - cam.z) * (f.z - cam.z));
         if (dist > MAX_DIST) {
            continue;
         }
         // distance keeps it polite: full presence up close, a hint from far
         float proximity = Mth.clamp((float)(1.2 - dist / MAX_DIST), 0.18F, 1.0F);
         float amount = env * f.strength * proximity;

         float progress = Mth.clamp(ticks / LIFE_TICKS, 0.0F, 1.0F);
         // core flash: bright, tight, shrinks as it decays
         float coreSize = (float)(f.bodyR * (1.35 - 0.35 * progress));
         int coreA = (int)(215.0F * amount);
         // shock ring: races outward and thins out
         float ringSize = (float)(f.bodyR * (1.25 + 2.1 * progress));
         int ringA = (int)(150.0F * amount * (1.0F - progress * 0.55F));
         if (coreA <= 2 && ringA <= 2) {
            continue;
         }

         Vec3 at = new Vec3(f.x, f.y, f.z);
         // camera-facing basis so the burst always reads as a disc/ring to the
         // viewer; everything stays WORLD-anchored at the storm core
         Vec3 view = at.subtract(cam).normalize();
         Vec3 upHint = Math.abs(view.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
         Vec3 right = view.cross(upHint).normalize();
         Vec3 up = right.cross(view).normalize();

         if (coreA > 2) {
            int r = CORE_COLOR[0];
            int g = CORE_COLOR[1];
            int b = CORE_COLOR[2];
            float s = coreSize;
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(GLOW_TEXTURE), (pose, consumer) -> {
               billboardQuad(consumer, pose, at, right, up, s * 0.72F, r, g, b, coreA);
               // hot magenta heart of the bang
               billboardQuad(consumer, pose, at, right, up, s * 0.4F, EDGE_COLOR[0], EDGE_COLOR[1], EDGE_COLOR[2], coreA);
            });
         }

         if (ringA > 2) {
            float thin = ringSize * 0.16F + 1.0F;
            int rr = EDGE_COLOR[0];
            int rg = EDGE_COLOR[1];
            int rb = EDGE_COLOR[2];
            collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(RING_TEXTURE), (pose, consumer) -> {
               billboardQuad(consumer, pose, at, right, up, ringSize, rr, rg, rb, ringA);
               billboardQuad(consumer, pose, at, right, up, thin, CORE_COLOR[0], CORE_COLOR[1], CORE_COLOR[2], ringA);
            });
         }
      }
   }

   /** One camera-facing additive quad in world space (depth-tested, never writes depth). */
   private static void billboardQuad(VertexConsumer consumer, PoseStack.Pose pose, Vec3 at, Vec3 right, Vec3 up, float half, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)(at.x - right.x * half - up.x * half), (float)(at.y - right.y * half - up.y * half), (float)(at.z - right.z * half - up.z * half)).setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, (float)up.x, (float)up.y, (float)up.z);
      consumer.addVertex(pose, (float)(at.x + right.x * half - up.x * half), (float)(at.y + right.y * half - up.y * half), (float)(at.z + right.z * half - up.z * half)).setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, (float)up.x, (float)up.y, (float)up.z);
      consumer.addVertex(pose, (float)(at.x + right.x * half + up.x * half), (float)(at.y + right.y * half + up.y * half), (float)(at.z + right.z * half + up.z * half)).setColor(r, g, b, a).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, (float)up.x, (float)up.y, (float)up.z);
      consumer.addVertex(pose, (float)(at.x - right.x * half + up.x * half), (float)(at.y - right.y * half + up.y * half), (float)(at.z - right.z * half + up.z * half)).setColor(r, g, b, a).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, (float)up.x, (float)up.y, (float)up.z);
   }

   /** Reset transient state (world leave etc.). */
   public static void clear() {
      java.util.Arrays.fill(FLASHES, null);
      LAST_PHASE.clear();
   }
}
