package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * StormSkyCanopy — the broad upper-sky mass that helps the storm fully claim
 * the top of the sky without forcing the user to manually overcrank the sky
 * darken sliders.
 *
 * It is intentionally separate from the nearer cloud deck: this layer is about
 * the giant coloured ceiling and horizon wash, while {@link StormCloudDeck}
 * handles the readable blocky slabs around the storm itself.
 */
public final class StormSkyCanopy {
   private static final double MAX_VIEW_DIST = 1250.0;

   private StormSkyCanopy() {
   }

   private static void renderCanopy(
      com.mojang.blaze3d.vertex.VertexConsumer consumer,
      PoseStack.Pose pose,
      Vec3 cam,
      float nowSec,
      int mode,
      int entityId,
      float phase,
      float expansionPhase,
      double dispX,
      double dispY,
      double dispZ,
      float blend,
      boolean fixedBlend,
      float[] outer,
      float[] inner
   ) {
      if (phase < 4.35F || blend <= 0.01F) {
         return;
      }

      Vec3 centre = new Vec3(dispX, dispY, dispZ);
      double stormDist = centre.distanceTo(cam);
      if (stormDist > MAX_VIEW_DIST) {
         return;
      }

      float phaseRamp = StormCloudDeck.smooth(phase, 4.35F, 5.25F);
      float growthScale = (float)WitherStormEntity.clientGrowthScaleForPhase(Math.max(phase, expansionPhase));
      float distFade = Mth.clamp(1.0F - (float)((stormDist - 180.0) / (850.0 + growthScale * 220.0)), 0.0F, 1.0F);
      float canopyBlend = fixedBlend ? blend : Math.max(blend, distFade * 0.75F);
      if (phaseRamp * canopyBlend <= 0.01F) {
         return;
      }

      StormCloudDeck.colorsForPhase(phase, outer, inner);
      float[] sky = StormPalettes.skyColor(phase, new float[3]);
      outer[0] = Mth.lerp(0.45F, outer[0], sky[0]);
      outer[1] = Mth.lerp(0.45F, outer[1], sky[1]);
      outer[2] = Mth.lerp(0.45F, outer[2], sky[2]);
      inner[0] = Mth.lerp(0.32F, inner[0], 1.0F);
      inner[1] = Mth.lerp(0.32F, inner[1], 1.0F);
      inner[2] = Mth.lerp(0.32F, inner[2], 1.0F);

      int or = (int)(Mth.clamp(outer[0], 0.0F, 1.0F) * 255.0F);
      int og = (int)(Mth.clamp(outer[1], 0.0F, 1.0F) * 255.0F);
      int ob = (int)(Mth.clamp(outer[2], 0.0F, 1.0F) * 255.0F);
      int ir = (int)(Mth.clamp(inner[0], 0.0F, 1.0F) * 255.0F);
      int ig = (int)(Mth.clamp(inner[1], 0.0F, 1.0F) * 255.0F);
      int ib = (int)(Mth.clamp(inner[2], 0.0F, 1.0F) * 255.0F);

      double anchorX = Mth.lerp(0.38, cam.x, dispX);
      double anchorZ = Mth.lerp(0.38, cam.z, dispZ);
      double altitudeBase = Math.max(cam.y + 110.0, dispY + (95.0 + growthScale * 18.0) + DevouringStormsClientConfig.stormCloudAltitude);

      for (int layer = 0; layer < 4; layer++) {
         double radius = (220.0 + layer * 95.0) * (0.9 + 0.15 * layer) * Math.max(1.0, DevouringStormsClientConfig.stormCloudCoverage) * (0.9 + 0.38 * growthScale);
         double halfLen = radius * (1.05 + 0.12 * layer);
         double halfWid = radius * (0.42 + 0.05 * layer);
         double drift = (layer % 2 == 0 ? 1.0 : -1.0) * (0.0035 + layer * 0.0017);
         double rot = nowSec * drift + entityId * 0.071 + layer * 0.9;
         double y = altitudeBase + layer * 18.0 + Math.sin(nowSec * 0.03 + layer + entityId * 0.1) * 6.0;
         int outerA = (int)(255.0F * phaseRamp * canopyBlend * (mode >= 2 ? 0.18F : 0.12F) * (1.0F - layer * 0.14F));
         int innerA = (int)(outerA * 0.50F);
         if (outerA <= 2) {
            continue;
         }
         StormCloudDeck.slab(consumer, pose, anchorX, y, anchorZ, halfLen, halfWid, rot, or, og, ob, outerA);
         StormCloudDeck.slab(consumer, pose, anchorX, y + 0.2 + layer * 0.03, anchorZ, halfLen * 0.72, halfWid * 0.66, rot, ir, ig, ib, innerA);
      }
   }

   public static void submit(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      int mode = (int)Math.round(DevouringStormsClientConfig.stormCloudDeck);
      var storms = ClientDistantStormManager.all();
      boolean global = StormSkyDarken.globalCloudDeckActive();
      if (mode <= 0 || mc.level == null || storms.isEmpty() && !global) {
         return;
      }

      float gt = (float)(mc.level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float nowSec = gt * 0.05F;
      Vec3 cam = ctx.levelState().cameraRenderState.pos;
      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();

      collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(StormCloudDeck.SLAB), (pose, consumer) -> {
         float[] outer = new float[3];
         float[] inner = new float[3];

         for (ClientDistantStormManager.StormData d : storms) {
            renderCanopy(consumer, pose, cam, nowSec, mode, d.entityId, d.phase, d.expansionPhase, d.dispX, d.dispY, d.dispZ, StormSkyDarken.paletteBlend(), false, outer, inner);
         }

         if (global) {
            float phase = StormSkyDarken.globalPhase();
            renderCanopy(consumer, pose, cam, nowSec, mode, 86420, phase, phase, cam.x, cam.y + 96.0 + (double)(phase - 4.5F) * 10.0, cam.z, StormSkyDarken.globalBlend(), true, outer, inner);
         }
      });
   }
}
