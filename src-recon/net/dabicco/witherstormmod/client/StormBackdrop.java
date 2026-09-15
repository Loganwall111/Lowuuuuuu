package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

/**
 * MCSM 1.9.201 -- 2D BACKGROUND SKYBOX STICKER ARCHITECTURE.
 *
 * The old build placed the atmospheric backdrop as WORLD-SPACE quads 220 blocks
 * in front of the camera, aimed at the storm centre (a rigid geometric card
 * that clipped through the camera and read as a "disc in the sky"). That whole
 * rendering path is purged: no dome meshes, no glare discs, no camera-clipping
 * bulbs, no world-anchored cards.
 *
 * The backdrop now lives on Minecraft's native BACKGROUND SKY LAYER: three flat
 * smoky alpha sheets are pinned to the camera axes at a fixed sky-plane depth
 * (an infinite, parallax-free cinematic canvas) and submitted through the
 * storm_translucent pipeline, whose GREATER_THAN_OR_EQUAL depth test with no
 * depth write means the sheets can ONLY paint pixels where the depth buffer is
 * still the cleared sky value. Terrain, entities, beams and the storm body all
 * write depth closer than the sky plane, so the sticker sits safely behind the
 * main entity and every world object, exactly like the vanilla sky dome.
 *
 * Sheet bindings (storyboard):
 *   phase 5.00-5.50  organic teal sheet          backdrop_sheet_phase5_teal.png
 *   phase 5.50-5.95  deep violet sheet           backdrop_sheet_phase55_violet.png
 *   phase 5.95+      plum-to-salmon twilight     backdrop_sheet_phase6_plum.png
 * Outside an active storm the sheets fade to zero alpha and sky.fsh hands the
 * frame back to the untouched vanilla overworld daylight cycle.
 */
public final class StormBackdrop {
   private static final Identifier SHEET_TEAL = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/backdrop_sheet_phase5_teal.png");
   private static final Identifier SHEET_VIOLET = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/backdrop_sheet_phase55_violet.png");
   private static final Identifier SHEET_PLUM = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/backdrop_sheet_phase6_plum.png");
   private static final int FULL_BRIGHT = 15728880;
   /** Fixed sky-plane distance: far beyond any world geometry, inside the far clip. */
   private static final double SKY_PLANE = 4096.0;

   private StormBackdrop() {
   }

   private static float ramp(float v, float lo, float hi) {
      if (hi <= lo) {
         return v >= hi ? 1.0F : 0.0F;
      } else {
         float t = Mth.clamp((v - lo) / (hi - lo), 0.0F, 1.0F);
         return t * t * (3.0F - 2.0F * t);
      }
   }

   public static void submit(LevelRenderContext ctx) {
      if (!DabyWSClientConfig.stormBackdrop) {
         return;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || net.dabicco.witherstormmod.client.ClientDistantStormManager.all().isEmpty()) {
         return;
      }

      float master = (float)DabyWSClientConfig.stormBackdropStrength;
      if (master <= 0.004F) {
         return;
      }

      // Highest phase among the tracked storms drives the sheet selection. The
      // manager is now a pure data feed: nothing here reads a world position.
      float phase = 0.0F;

      for (net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData d : net.dabicco.witherstormmod.client.ClientDistantStormManager.all()) {
         phase = Math.max(phase, d.phase);
      }

      if (phase < 4.55F) {
         return;
      }

      float gt = (float)(mc.level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float nowSec = gt * 0.05F;
      Camera camera = mc.gameRenderer.mainCamera();
      if (!camera.isInitialized()) {
         return;
      }

      Vec3 cam = ctx.levelState().cameraRenderState.pos;
      // Camera-locked axes: the canvas rides the view rotation only, never the
      // view position, so it has no parallax and no world anchor -- it IS the
      // background sky layer.
      Vector3fc fwd = camera.forwardVector();
      Vector3fc up = camera.upVector();
      Vector3fc left = camera.leftVector();
      Vec3 view = new Vec3((double)fwd.x(), (double)fwd.y(), (double)fwd.z()).normalize();
      Vec3 upV = new Vec3((double)up.x(), (double)up.y(), (double)up.z()).normalize();
      Vec3 rightV = new Vec3(-(double)left.x(), -(double)left.y(), -(double)left.z()).normalize();

      float scale = Mth.clamp((float)(DabyWSClientConfig.stormBackdropSize / 6.0), 0.35F, 2.5F);
      // Over-cover the frustum at the sky plane; the sheets' own alpha does the
      // framing, so no hard quad edge is ever on screen.
      double half = SKY_PLANE * 1.15 * (double)scale;
      float breathe = 0.88F + 0.12F * Mth.sin(nowSec * 0.045F * (float)DabyWSClientConfig.stormBackdropPulse);

      float wTeal = ramp(phase, 4.55F, 4.95F) * (1.0F - ramp(phase, 5.42F, 5.58F));
      float wViolet = ramp(phase, 5.42F, 5.58F) * (1.0F - ramp(phase, 5.90F, 6.06F));
      float wPlum = ramp(phase, 5.90F, 6.06F);

      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();

      // Back-to-front layering on the sky plane: twilight sheet deepest, then
      // violet, teal on top; each at its own depth sliver so the submissions
      // keep a stable order in the background queue.
      if (wPlum > 0.004F && DabyWSClientConfig.stormBackdropPink) {
         sticker(poseStack, collector, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(SHEET_PLUM), cam, view, rightV, upV, SKY_PLANE + 192.0, half * 1.12, master * wPlum * breathe);
      }

      if (wViolet > 0.004F && DabyWSClientConfig.stormBackdropPurple) {
         sticker(poseStack, collector, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(SHEET_VIOLET), cam, view, rightV, upV, SKY_PLANE + 128.0, half * 1.06, master * wViolet * breathe);
      }

      if (wTeal > 0.004F && DabyWSClientConfig.stormBackdropTurquoise) {
         sticker(poseStack, collector, net.dabicco.witherstormmod.client.GlowRenderTypes.translucent(SHEET_TEAL), cam, view, rightV, upV, SKY_PLANE + 64.0, half, master * wTeal * breathe);
      }
   }

   /**
    * One flat, screen-locked backdrop sheet on the background sky layer. The
    * quad is a pure function of the camera AXES (never the storm's world
    * position or distance), so it cannot clip the camera, cannot parallax and
    * cannot read as a 3D object in the world grid.
    */
   private static void sticker(
      PoseStack poseStack,
      SubmitNodeCollector collector,
      RenderType type,
      Vec3 cam,
      Vec3 view,
      Vec3 right,
      Vec3 up,
      double dist,
      double half,
      float alpha
   ) {
      int a = (int)(Mth.clamp(alpha, 0.0F, 1.0F) * 235.0F);
      if (a > 2) {
         Vec3 at = cam.add(view.scale(dist));
         Vec3 rx = right.scale(half * 1.15);
         Vec3 uy = up.scale(half);
         collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            vertex(pose, consumer, at.subtract(rx).subtract(uy), 0.0F, 1.0F, a);
            vertex(pose, consumer, at.add(rx).subtract(uy), 1.0F, 1.0F, a);
            vertex(pose, consumer, at.add(rx).add(uy), 1.0F, 0.0F, a);
            vertex(pose, consumer, at.subtract(rx).add(uy), 0.0F, 0.0F, a);
         });
      }
   }

   private static void vertex(Pose pose, VertexConsumer consumer, Vec3 at, float u, float v, int a) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z)
         .setColor(255, 255, 255, a)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(pose, 0.0F, 1.0F, 0.0F);
   }
}
