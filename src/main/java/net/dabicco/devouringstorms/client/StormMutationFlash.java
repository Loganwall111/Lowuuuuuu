package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.HashMap;
import java.util.Map;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * StormMutationFlash — the LOCALIZED purple flash-bang for phase 6+.
 *
 * When the storm mutates past the split (phase 6) and again at 7 and 8, and
 * at slow intervals while it rampages, a bright purple flash blooms around
 * the storm's bearing IN THE SKY LAYER (the Telltale architecture's dedicated
 * flash pass — exactly like vanilla's own end-dragon flash quad):
 *
 *  - rendered inside the native sky pass as an additive radial bloom centred
 *    on the storm's sky bearing, so it lives at infinite depth with the
 *    backdrop instead of on the world;
 *  - it is NOT a full-screen overlay and never touches the lightmap or fog —
 *    the bloom stays localized around the storm and the terrain keeps its
 *    own lighting;
 *  - it is a separate effect from the purple anomaly skybox — same colour
 *    family, different layer and timing.
 */
public final class StormMutationFlash {
   private static final Identifier GLOW_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/halo_gradient.png");
   private static final Identifier RING_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/halo_ring.png");
   /** Flashes live for this many ticks after the bang. */
   private static final float LIFE_TICKS = 26.0F;
   /** Ticks from bang to full brightness. */
   private static final float ATTACK_TICKS = 2.0F;
   /** Sky-depth radius the bloom quads sit at (the vanilla sun band). */
   private static final float SKY_R = 240.0F;
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

   /**
    * BISECT PROBE: sky bloom disabled while hunting the compile failure.
    */
   public static void renderSkyBloom(org.joml.Vector3f target) {
   }


   /** One billboard quad on the tangent plane of the storm bearing, sky depth. */
   private static void skyQuad(BufferBuilder bb, float cx, float cy, float cz, Vector3f t1, Vector3f t2, float half, int r, int g, int b, int a) {
      bb.addVertex(cx - t1.x * half - t2.x * half, cy - t1.y * half - t2.y * half, cz - t1.z * half - t2.z * half).setUv(0.0F, 0.0F).setColor(r, g, b, a);
      bb.addVertex(cx + t1.x * half - t2.x * half, cy + t1.y * half - t2.y * half, cz + t1.z * half - t2.z * half).setUv(1.0F, 0.0F).setColor(r, g, b, a);
      bb.addVertex(cx + t1.x * half + t2.x * half, cy + t1.y * half + t2.y * half, cz + t1.z * half + t2.z * half).setUv(1.0F, 1.0F).setColor(r, g, b, a);
      bb.addVertex(cx - t1.x * half + t2.x * half, cy - t1.y * half + t2.y * half, cz - t1.z * half + t2.z * half).setUv(0.0F, 1.0F).setColor(r, g, b, a);
   }

   /** Reset transient state (world leave etc.). */
   public static void clear() {
      java.util.Arrays.fill(FLASHES, null);
      LAST_PHASE.clear();
   }
}
