package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class StormGlowRenderer {
   private static final Identifier GLOW_SPRITE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/entity/tractor_beam.png");
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

   private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 at, float u, float v, int[] rgb, int alpha) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z).setColor(rgb[0], rgb[1], rgb[2], alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }

   /**
    * Far-away purple pulse for the final stages (video). The storm glows with a
    * pulsing purple aura that is visible from very far away, growing with phase.
    */
   public static void submitDistantPulse(PoseStack poseStack, SubmitNodeCollector collector, Vec3 centre, Vec3 view, float phase, float gameTimeTicks) {
      if (phase < 4.0F) {
         return;
      }
      double radius = 6.0 + (phase - 4.0) * 3.0;
      float pulse = 0.85F + 0.15F * Mth.sin(gameTimeTicks * 0.08F);
      float amount = Mth.clamp((phase - 4.0F) / 2.0F, 0.0F, 1.0F) * pulse;
      float[] sizes = new float[]{1.0F, 0.65F, 0.35F};
      float[] alphas = new float[]{0.25F, 0.45F, 0.7F};
      int[][] colours = new int[][]{{80, 30, 120}, {60, 20, 100}, {40, 15, 80}};
      submitLight(poseStack, collector, centre, view, radius, sizes, alphas, colours, amount);
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
