package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class StormGlowRenderer {
   private static final Identifier GLOW_SPRITE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/tractor_beam.png");
   private static final int FULL_BRIGHT = 15728880;

   private StormGlowRenderer() {
   }

   public static void submitLight(PoseStack poseStack, SubmitNodeCollector collector, Vec3 centre, Vec3 view, double radius, float[] sizes, float[] alphas, int[][] colours, float amount) {
      if (!(amount <= 0.004F) && !(radius <= 0.001)) {
         Vec3 upHint = Math.abs(view.y) > 0.98 ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : new Vec3((double)0.0F, (double)1.0F, (double)0.0F);
         Vec3 right = view.cross(upHint).normalize();
         Vec3 up = right.cross(view).normalize();

         for(int i = sizes.length - 1; i >= 0; --i) {
            int alpha = (int)(Mth.clamp(alphas[i] * amount, 0.0F, 1.0F) * 255.0F);
            if (alpha > 2) {
               double r = radius * (double)sizes[i];
               Vec3 rx = right.scale(r);
               Vec3 uy = up.scale(r);
               int[] c = colours[i];
               collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(GLOW_SPRITE), (pose, consumer) -> {
                  vertex(pose, consumer, centre.subtract(rx).subtract(uy), 0.0F, 0.0F, c, alpha);
                  vertex(pose, consumer, centre.add(rx).subtract(uy), 1.0F, 0.0F, c, alpha);
                  vertex(pose, consumer, centre.add(rx).add(uy), 1.0F, 1.0F, c, alpha);
                  vertex(pose, consumer, centre.subtract(rx).add(uy), 0.0F, 1.0F, c, alpha);
               });
            }
         }

      }
   }

   /**
    * A single camera-facing quad billboarded to the view vector every frame so
    * aura/halo reads stay volumetric light instead of static 2D walls. Works
    * with additive glow types and alpha-blended translucent types alike; the
    * pipelines behind both never write depth, so no square Z artifacts remain.
    */
   public static void submitBillboardPlane(PoseStack poseStack, SubmitNodeCollector collector, Vec3 centre, Vec3 view, double halfW, double halfH, int r, int g, int b, int alpha, RenderType type) {
      if (alpha <= 2 || halfW <= 0.01 || halfH <= 0.01 || type == null) {
         return;
      }
      Vec3 upHint = Math.abs(view.y) > 0.98 ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : new Vec3((double)0.0F, (double)1.0F, (double)0.0F);
      Vec3 right = view.cross(upHint).normalize();
      Vec3 up = right.cross(view).normalize();
      Vec3 rx = right.scale(halfW);
      Vec3 uy = up.scale(halfH);
      collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
         vertex(pose, consumer, centre.subtract(rx).subtract(uy), 0.0F, 0.0F, r, g, b, alpha);
         vertex(pose, consumer, centre.add(rx).subtract(uy), 1.0F, 0.0F, r, g, b, alpha);
         vertex(pose, consumer, centre.add(rx).add(uy), 1.0F, 1.0F, r, g, b, alpha);
         vertex(pose, consumer, centre.subtract(rx).add(uy), 0.0F, 1.0F, r, g, b, alpha);
      });
   }

   private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 at, float u, float v, int[] rgb, int alpha) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z).setColor(rgb[0], rgb[1], rgb[2], alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }

   private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 at, float u, float v, int r, int g, int b, int alpha) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z).setColor(r, g, b, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }

   public static float nightFactor(Level level) {
      long t = level.getOverworldClockTime() % 24000L;
      if (t >= 12500L && t <= 23500L) {
         float in = (float)(t - 12500L) / 1000.0F;
         float out = (float)(23500L - t) / 1000.0F;
         return Mth.clamp(Math.min(in, out), 0.0F, 1.0F);
      } else {
         return 0.0F;
      }
   }
}
