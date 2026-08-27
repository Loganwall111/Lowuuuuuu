package net.dabicco.witherstormmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * VortexRenderer — Renders the dynamic swirling atmospheric vortex that sits
 * right above the Wither Storm body once it reaches Phases 7 and 8.
 */
public final class VortexRenderer {
   private static final Identifier VORTEX_TEXTURE = Identifier.fromNamespaceAndPath("dabywitherstormmod", "textures/misc/halo_ring.png");

   private VortexRenderer() {
   }

   public static void render(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return;
      }

      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();
      Vec3 cam = ctx.levelState().cameraRenderState.pos;

      long gt = mc.level.getGameTime();
      float frac = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float nowSec = ((float)(gt % 100000L) + frac) * 0.05F;

      for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         if (d.phase < 7.0F) {
            continue;
         }

         double bodyR = StormPresenceFX.bodyRadius(d.phase);
         Vec3 vortexPos = new Vec3(d.x, d.y + bodyR * 1.85, d.z).subtract(cam);

         poseStack.pushPose();
         poseStack.translate(vortexPos.x, vortexPos.y, vortexPos.z);
         // Swirling vortex rotation
         poseStack.mulPose(Axis.YP.rotationDegrees(nowSec * 24.0F));

         float vortexRadius = (float)(bodyR * 1.4);
         collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(VORTEX_TEXTURE), (pose, consumer) -> {
            // Emits swirling layered disc matrix
            for (int layer = 0; layer < 3; layer++) {
               float r = vortexRadius * (0.8F + layer * 0.25F);
               float yOff = layer * 3.5F;
               int alpha = 140 - layer * 30;
               consumer.addVertex(pose, -r, yOff, -r).setColor(30, 10, 45, alpha).setUv(0.0F, 0.0F).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
               consumer.addVertex(pose, r, yOff, -r).setColor(30, 10, 45, alpha).setUv(1.0F, 0.0F).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
               consumer.addVertex(pose, r, yOff, r).setColor(30, 10, 45, alpha).setUv(1.0F, 1.0F).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
               consumer.addVertex(pose, -r, yOff, r).setColor(30, 10, 45, alpha).setUv(0.0F, 1.0F).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(pose, 0.0F, 1.0F, 0.0F);
            }
         });

         poseStack.popPose();
      }
   }
}
