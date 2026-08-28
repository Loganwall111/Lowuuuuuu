package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * StormBlobFX — the pure-shader-style storm atmosphere elements (no solid 3D
 * shells, no PNG cloud walls — everything here is translucent/glow geometry
 * driven by phase and the live game clock, exactly how the MCSM atmosphere is
 * supposed to read):
 *
 *  - Light-blue centre halo: lives at the very centre of the storm from
 *    phase 4 onward and stays there to the very end.
 *  - Giant centre blob (phase 5.1 -> 5.9): a roiling colour-shifting mass at
 *    the storm's core (dark purple -> magenta -> pink/blue/black-purple).
 *  - "Back" cloud (phase 5.1+): a heavy magenta/purple/pink/black fog layer
 *    attached to the rear of the storm; it moves with the storm.
 *  - Phase-6 flash: a bright pulse placed right above the storm that fires
 *    once every two minutes (2400 ticks) — phase 6 and up only.
 *  - Vortex (phase 7/8): the Vortex model mesh (converted from the provided
 *    Vortex.bbmodel) rendered additively on top of the storm, rotating.
 */
public final class StormBlobFX {
   private static final Identifier SOFT = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/tractor_beam.png");
   private static final Identifier HALO = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/halo_ring.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final int FLASH_PERIOD_TICKS = 2400; // 2 minutes
   private static final int FLASH_WINDOW_TICKS = 44;

   private StormBlobFX() {
   }

   /** Approximate visual body radius for a phase (mirrors StormPresenceFX). */
   private static double bodyRadius(float phase) {
      if (phase < 4.0F) {
         return 4.0 + phase * 1.5;
      }
      if (phase < 5.0F) {
         return 10.0 + (phase - 4.0F) * 12.0;
      }
      return 22.0 + Math.min(phase - 5.0F, 1.99F) * 9.0;
   }

   public static void submit(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || ClientDistantStormManager.all().isEmpty() || !DabyWSClientConfig.stormBlobFX) {
         return;
      }
      float gt = (float)(mc.level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float nowSec = gt * 0.05F;
      Vec3 cam = ctx.levelState().cameraRenderState.pos;
      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();

      for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         float phase = d.phase;
         Vec3 centre = new Vec3(d.dispX, d.dispY, d.dispZ);
         double bodyR = bodyRadius(phase);
         Vec3 view = centre.subtract(cam).normalize();

         /* ---- light-blue centre halo (phase 4+ to the very end) ---- */
         if (phase >= 4.0F) {
            float ramp = Mth.clamp((phase - 4.0F) / 0.8F, 0.0F, 1.0F);
            float breathe = 0.75F + 0.25F * (float)Math.sin(nowSec * 1.3F + d.entityId);
            float[] col = StormPalettes.haloCenterColor(new float[3]);
            int a = (int)(Mth.clamp(ramp * 0.55F * breathe, 0.0F, 1.0F) * 255.0F);
            quad(poseStack, collector, GlowRenderTypes.glow(HALO), cam, centre, view, bodyR * 1.15, (int)(col[0] * 255.0F), (int)(col[1] * 255.0F), (int)(col[2] * 255.0F), a);
         }

         /* ---- giant centre blob (phase 5.1 -> 5.9), colour-shifting ---- */
         if (phase >= 5.1F && phase < 5.95F) {
            float rampIn = Mth.clamp((phase - 5.1F) / 0.15F, 0.0F, 1.0F);
            float rampOut = Mth.clamp((5.95F - phase) / 0.35F, 0.0F, 1.0F);
            float amount = rampIn * rampOut;
            if (amount > 0.01F) {
               float wobble = (float)(nowSec * 0.05 + d.entityId * 0.618);
               float[] col = StormPalettes.blobColor(phase, wobble, new float[3]);
               // nested soft shells so the blob reads as a thick roiling mass
               float[][] shells = new float[][]{{0.42F, 0.17F}, {0.62F, 0.10F}, {0.85F, 0.055F}};
               for (float[] shell : shells) {
                  int a = (int)(Mth.clamp(shell[1] * amount * 2.4F, 0.0F, 1.0F) * 255.0F);
                  if (a <= 2) {
                     continue;
                  }
                  quad(poseStack, collector, GlowRenderTypes.glow(SOFT), cam, centre, view, bodyR * shell[0], (int)(col[0] * 255.0F), (int)(col[1] * 255.0F), (int)(col[2] * 255.0F), a);
               }
            }
         }

         /* ---- "back" cloud: heavy magenta/purple/pink/black fog layer
                  attached to the rear of the storm (phase 5.1+) ---- */
         if (phase >= 5.1F) {
            float ramp = Mth.clamp((phase - 5.1F) / 0.5F, 0.0F, 1.0F);
            if (ramp > 0.02F) {
               Vec3 back = centre.subtract(view.scale(bodyR * 1.45 + 18.0));
               float wobble = (float)(nowSec * 0.11 + d.entityId * 0.31);
               float[] col = StormPalettes.blobColor(phase, wobble, new float[3]);
               // magenta haze
               int aM = (int)(Mth.clamp(0.16F * ramp * (0.7F + 0.3F * (float)Math.sin(nowSec * 0.7F)), 0.0F, 1.0F) * 255.0F);
               quad(poseStack, collector, GlowRenderTypes.translucent(SOFT), cam, back, back.subtract(cam).normalize(), bodyR * 1.9, 210, 40, 185, aM);
               // black-purple core
               int aB = (int)(Mth.clamp(0.13F * ramp, 0.0F, 1.0F) * 255.0F);
               quad(poseStack, collector, GlowRenderTypes.translucent(SOFT), cam, back, back.subtract(cam).normalize(), bodyR * 1.4, (int)(col[0] * 255.0F), (int)(col[1] * 255.0F), (int)(col[2] * 255.0F), aB);
            }
         }

         /* ---- phase-6 flash: bright pulse above the storm every 2 minutes ---- */
         if (phase >= 6.0F) {
            long gtTicks = mc.level.getGameTime();
            long inWindow = gtTicks % FLASH_PERIOD_TICKS;
            if (inWindow < FLASH_WINDOW_TICKS) {
               float t = (float)inWindow / (float)FLASH_WINDOW_TICKS;
               float env = Math.min(t / 0.18F, (1.0F - t) / 0.82F); // quick rise, slow fall
               float amount = Mth.clamp(env * 1.35F, 0.0F, 1.0F);
               float[] col = StormPalettes.flashColor(new float[3]);
               Vec3 above = centre.add(0.0, bodyR * 1.5, 0.0);
               Vec3 upView = above.subtract(cam).normalize();
               int a = (int)(amount * 255.0F);
               quad(poseStack, collector, GlowRenderTypes.glow(SOFT), cam, above, upView, bodyR * 1.7, (int)(col[0] * 255.0F), (int)(col[1] * 255.0F), (int)(col[2] * 255.0F), a);
            }
         }

         /* ---- vortex on top (phases 7/8): the Vortex model, additive ---- */
         if (phase >= 7.0F && phase < 9.0F) {
            float ramp = Mth.clamp((phase - 7.0F) / 0.5F, 0.0F, 1.0F);
            BakedMesh.Mesh vortex = BakedMesh.mesh("vortex");
            if (vortex.tris().length > 0) {
               Vec3 at = centre.add(0.0, bodyR * (0.85 + 0.25 * ramp), 0.0);
               float scale = 0.045F + 0.02F * ramp;
               float yaw = nowSec * 14.0F + d.entityId;
               float tumble = 4.0F + 3.0F * ramp;
               int a = (int)(Mth.clamp(0.5F + 0.3F * ramp, 0.0F, 1.0F) * 255.0F);
               collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(SOFT), (pose, consumer) -> BakedMesh.emit(consumer, pose, vortex, at, yaw, tumble, scale, 165, 95, 245, a, FULL_BRIGHT));
            }
         }
      }
   }

   /** camera-facing textured quad */
   private static void quad(PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.rendertype.RenderType type, Vec3 cam, Vec3 centre, Vec3 view, double radius, int r, int g, int b, int alpha) {
      if (alpha <= 2) {
         return;
      }
      Vec3 upHint = Math.abs(view.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
      Vec3 right = view.cross(upHint).normalize();
      Vec3 up = right.cross(view).normalize();
      Vec3 rx = right.scale(radius);
      Vec3 uy = up.scale(radius);
      collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
         vertex(pose, consumer, centre.subtract(rx).subtract(uy), 0.0F, 0.0F, r, g, b, alpha);
         vertex(pose, consumer, centre.add(rx).subtract(uy), 1.0F, 0.0F, r, g, b, alpha);
         vertex(pose, consumer, centre.add(rx).add(uy), 1.0F, 1.0F, r, g, b, alpha);
         vertex(pose, consumer, centre.subtract(rx).add(uy), 0.0F, 1.0F, r, g, b, alpha);
      });
   }

   private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 at, float u, float v, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }
}
