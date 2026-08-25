package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class StormGlowRenderer {
   private static final Identifier GLOW_SPRITE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/halo_gradient.png");
   private static final int FULL_BRIGHT = 15728880;

   private StormGlowRenderer() {
   }

   /**
    * A point glow at a world position. WORLD-SPACE, never a billboard: the
    * gradient is carried by three orthogonal world-axis-aligned planes
    * (XZ / XY / ZY) through the centre -- a classic glow cross. From any
    * angle the additive sum of the visible planes reads as a soft volumetric
    * light, and unlike a camera-facing quad it never swings around the
    * subject as you orbit it. The {@code view} argument is kept for call-site
    * compatibility and deliberately ignored.
    */
   public static void submitLight(PoseStack poseStack, SubmitNodeCollector collector, Vec3 centre, Vec3 view, double radius, float[] sizes, float[] alphas, int[][] colours, float amount) {
      if (!(amount <= 0.004F) && !(radius <= 0.001)) {
         for(int i = sizes.length - 1; i >= 0; --i) {
            int alpha = (int)(Mth.clamp(alphas[i] * amount, 0.0F, 1.0F) * 255.0F);
            if (alpha > 2) {
               double r = radius * (double)sizes[i];
               int[] c = colours[i];
               collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(GLOW_SPRITE), (pose, consumer) -> {
                  glowCross(consumer, pose, centre, r, c, alpha);
               });
            }
         }

      }
   }

   private static void glowCross(VertexConsumer consumer, PoseStack.Pose pose, Vec3 at, double r, int[] rgb, int alpha) {
      double x = at.x;
      double y = at.y;
      double z = at.z;
      // XZ plane (horizontal)
      vertex(pose, consumer, new Vec3(x - r, y, z - r), 0.0F, 0.0F, rgb, alpha);
      vertex(pose, consumer, new Vec3(x + r, y, z - r), 1.0F, 0.0F, rgb, alpha);
      vertex(pose, consumer, new Vec3(x + r, y, z + r), 1.0F, 1.0F, rgb, alpha);
      vertex(pose, consumer, new Vec3(x - r, y, z + r), 0.0F, 1.0F, rgb, alpha);
      // XY plane
      vertex(pose, consumer, new Vec3(x - r, y - r, z), 0.0F, 0.0F, rgb, alpha);
      vertex(pose, consumer, new Vec3(x + r, y - r, z), 1.0F, 0.0F, rgb, alpha);
      vertex(pose, consumer, new Vec3(x + r, y + r, z), 1.0F, 1.0F, rgb, alpha);
      vertex(pose, consumer, new Vec3(x - r, y + r, z), 0.0F, 1.0F, rgb, alpha);
      // ZY plane
      vertex(pose, consumer, new Vec3(x, y - r, z - r), 0.0F, 0.0F, rgb, alpha);
      vertex(pose, consumer, new Vec3(x, y - r, z + r), 1.0F, 0.0F, rgb, alpha);
      vertex(pose, consumer, new Vec3(x, y + r, z + r), 1.0F, 1.0F, rgb, alpha);
      vertex(pose, consumer, new Vec3(x, y + r, z - r), 0.0F, 1.0F, rgb, alpha);
   }

   private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 at, float u, float v, int[] rgb, int alpha) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z).setColor(rgb[0], rgb[1], rgb[2], alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
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
