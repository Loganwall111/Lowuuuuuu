package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.devouringstorms.entity.WitherStormEntity;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Faux volumetric / cataclysm pass for the user's requested giant late-game
 * sky takeover. This is intentionally geometry-based and cel-styled instead of
 * a realistic cloud shader: towering light shafts, a spiral crown tornado and
 * a pulsing singularity halo appear as the storm's infinite growth climbs far
 * past its normal story stages.
 */
public final class StormCataclysmFX {
   private static final Identifier SHAFT = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/tractor_beam.png");
   private static final Identifier RING = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/halo_ring.png");
   private static final Identifier CLOUD = StormCloudDeck.SLAB;
   private static final double MAX_VIEW_DIST = 2600.0;

   private StormCataclysmFX() {
   }

   public static void submit(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || ClientDistantStormManager.all().isEmpty()) {
         return;
      }

      float gt = (float)(mc.level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float nowSec = gt * 0.05F;
      Vec3 cam = ctx.levelState().cameraRenderState.pos;
      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();

      float[] outer = new float[3];
      float[] inner = new float[3];
      float[] sky = new float[3];

      for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         double rawGrowth = WitherStormEntity.clientGrowthScaleForPhase(Math.max(d.phase, d.expansionPhase));
         float growthScale = (float)Math.min(12.0, rawGrowth);
         float shaftAmount = Mth.clamp((growthScale - 1.25F) / 1.7F, 0.0F, 1.0F);
         float tornadoAmount = Mth.clamp((growthScale - 1.9F) / 2.6F, 0.0F, 1.0F);
         float singularityAmount = Mth.clamp((growthScale - 3.0F) / 3.2F, 0.0F, 1.0F);
         if (shaftAmount <= 0.01F && tornadoAmount <= 0.01F && singularityAmount <= 0.01F) {
            continue;
         }

         Vec3 centre = new Vec3(d.dispX, d.dispY, d.dispZ);
         double stormDist = centre.distanceTo(cam);
         if (stormDist > MAX_VIEW_DIST) {
            continue;
         }

         float distFade = Mth.clamp(1.0F - (float)((stormDist - 160.0) / (900.0 + growthScale * 220.0)), 0.0F, 1.0F);
         if (distFade <= 0.01F) {
            continue;
         }

         float pulse = 0.58F + 0.42F * (0.5F + 0.5F * Mth.sin(nowSec * (0.45F + growthScale * 0.02F) + d.entityId * 0.13F));
         float chaos = Mth.clamp(tornadoAmount * 0.72F + singularityAmount * 0.55F, 0.0F, 1.0F);
         StormCloudDeck.colorsForPhase(d.phase, outer, inner);
         StormPalettes.skyColor(d.phase, sky);
         tintCataclysm(outer, 0.82F, 0.24F, 0.95F, chaos);
         tintCataclysm(inner, 0.96F, 0.88F, 1.0F, chaos * 0.72F);
         tintCataclysm(sky, 0.78F, 0.18F, 0.88F, chaos);

         if (shaftAmount > 0.01F) {
            submitShafts(poseStack, collector, d, nowSec, growthScale, shaftAmount * distFade * pulse, outer, inner);
         }
         if (tornadoAmount > 0.01F) {
            submitTornado(poseStack, collector, d, nowSec, growthScale, tornadoAmount * distFade, outer, inner);
         }
         if (singularityAmount > 0.01F) {
            submitSingularity(poseStack, collector, cam, d, nowSec, growthScale, singularityAmount * distFade * pulse, sky, inner);
         }
      }
   }

   private static void submitShafts(PoseStack poseStack, SubmitNodeCollector collector, ClientDistantStormManager.StormData d, float nowSec, float growthScale, float amount, float[] outer, float[] inner) {
      int shaftCount = 6 + Mth.floor(amount * 8.0F);
      int or = (int)(Mth.clamp(outer[0], 0.0F, 1.0F) * 255.0F);
      int og = (int)(Mth.clamp(outer[1], 0.0F, 1.0F) * 255.0F);
      int ob = (int)(Mth.clamp(outer[2], 0.0F, 1.0F) * 255.0F);
      int ir = (int)(Mth.clamp(inner[0], 0.0F, 1.0F) * 255.0F);
      int ig = (int)(Mth.clamp(inner[1], 0.0F, 1.0F) * 255.0F);
      int ib = (int)(Mth.clamp(inner[2], 0.0F, 1.0F) * 255.0F);
      double crownBase = d.dispY + 22.0 + growthScale * 18.0;
      double topHeight = 90.0 + growthScale * 55.0;
      collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(SHAFT), (pose, consumer) -> {
         for (int i = 0; i < shaftCount; i++) {
            double ang = nowSec * (0.065 + i * 0.0025) + d.entityId * 0.11 + i * (Math.PI * 2.0 / shaftCount);
            double orbit = (18.0 + (i % 3) * 8.0) * (0.95 + growthScale * 0.28);
            double x = d.dispX + Math.cos(ang) * orbit;
            double z = d.dispZ + Math.sin(ang) * orbit;
            double y0 = crownBase + Math.sin(nowSec * 0.3 + i) * (4.0 + growthScale * 0.4);
            double y1 = y0 + topHeight * (0.78 + (i % 4) * 0.07);
            double halfW = (4.8 + (i % 4) * 1.1) * (0.9 + growthScale * 0.10);
            int outerA = (int)(92.0F * amount * (0.90F - i * 0.035F / shaftCount));
            int innerA = (int)(outerA * 0.62F);
            if (outerA <= 2) {
               continue;
            }
            crossShaft(consumer, pose, x, y0, z, y1, halfW, or, og, ob, outerA);
            crossShaft(consumer, pose, x, y0 + 1.2, z, y1 - 4.0, halfW * 0.48, ir, ig, ib, innerA);
         }
      });
   }

   private static void submitTornado(PoseStack poseStack, SubmitNodeCollector collector, ClientDistantStormManager.StormData d, float nowSec, float growthScale, float amount, float[] outer, float[] inner) {
      int layers = Mth.clamp(10 + (int)(amount * 18.0F), 10, 28);
      int or = (int)(Mth.clamp(outer[0], 0.0F, 1.0F) * 255.0F);
      int og = (int)(Mth.clamp(outer[1], 0.0F, 1.0F) * 255.0F);
      int ob = (int)(Mth.clamp(outer[2], 0.0F, 1.0F) * 255.0F);
      int ir = (int)(Mth.clamp(inner[0], 0.0F, 1.0F) * 255.0F);
      int ig = (int)(Mth.clamp(inner[1], 0.0F, 1.0F) * 255.0F);
      int ib = (int)(Mth.clamp(inner[2], 0.0F, 1.0F) * 255.0F);
      double baseY = d.dispY + 34.0 + growthScale * 18.0;
      double totalHeight = 110.0 + growthScale * 80.0;
      collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(CLOUD), (pose, consumer) -> {
         for (int layer = 0; layer < layers; layer++) {
            float t = layers <= 1 ? 0.0F : (float)layer / (float)(layers - 1);
            double helix = nowSec * (0.26 + amount * 0.12) + d.entityId * 0.07 + layer * (0.48 + amount * 0.25);
            double y = baseY + totalHeight * t;
            double funnel = (1.0 - t * 0.78);
            double orbit = (28.0 + growthScale * 17.0) * funnel * (1.0 + 0.22 * Math.sin(nowSec * 0.35 + layer));
            double x = d.dispX + Math.cos(helix) * orbit * 0.36;
            double z = d.dispZ + Math.sin(helix) * orbit * 0.36;
            double halfLen = Math.max(12.0, orbit * (0.86 + amount * 0.22));
            double halfWid = Math.max(5.0, orbit * (0.16 + amount * 0.06));
            double rot = helix + layer * 0.18;
            int outerA = (int)(150.0F * amount * (0.84F - t * 0.34F));
            int innerA = (int)(outerA * 0.52F);
            if (outerA <= 2) {
               continue;
            }
            StormCloudDeck.slab(consumer, pose, x, y, z, halfLen, halfWid, rot, or, og, ob, outerA);
            StormCloudDeck.slab(consumer, pose, x, y + 0.18 + t * 0.55, z, halfLen * 0.68, halfWid * 0.62, rot, ir, ig, ib, innerA);
         }
      });
   }

   private static void submitSingularity(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cam, ClientDistantStormManager.StormData d, float nowSec, float growthScale, float amount, float[] sky, float[] inner) {
      Vec3 centre = new Vec3(d.dispX, d.dispY + 80.0 + growthScale * 38.0, d.dispZ);
      Vec3 rel = centre.subtract(cam);
      double dist = rel.length();
      if (dist < 1.0E-4) {
         return;
      }

      Vec3 view = rel.scale(1.0 / dist);
      int sr = (int)(Mth.clamp(sky[0], 0.0F, 1.0F) * 255.0F);
      int sg = (int)(Mth.clamp(sky[1], 0.0F, 1.0F) * 255.0F);
      int sb = (int)(Mth.clamp(sky[2], 0.0F, 1.0F) * 255.0F);
      int ir = (int)(Mth.clamp(inner[0], 0.0F, 1.0F) * 255.0F);
      int ig = (int)(Mth.clamp(inner[1], 0.0F, 1.0F) * 255.0F);
      int ib = (int)(Mth.clamp(inner[2], 0.0F, 1.0F) * 255.0F);
      double radius = (12.0 + growthScale * 6.0) * (0.92 + 0.18 * Math.sin(nowSec * 0.5 + d.entityId * 0.03));
      collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(CLOUD), (pose, consumer) -> {
         billboard(pose, consumer, centre, view, radius * 0.82, radius * 0.82, 14, 8, 24, (int)(128.0F * amount));
         billboard(pose, consumer, centre, view, radius * 0.42, radius * 0.42, 0, 0, 0, (int)(220.0F * amount));
      });
      collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(RING), (pose, consumer) -> {
         billboard(pose, consumer, centre, view, radius * 1.45, radius * 1.45, sr, sg, sb, (int)(118.0F * amount));
         billboard(pose, consumer, centre, view, radius * 0.98, radius * 0.98, ir, ig, ib, (int)(82.0F * amount));
      });
   }

   private static void tintCataclysm(float[] rgb, float r, float g, float b, float amount) {
      rgb[0] = Mth.lerp(amount, rgb[0], r);
      rgb[1] = Mth.lerp(amount, rgb[1], g);
      rgb[2] = Mth.lerp(amount, rgb[2], b);
   }

   private static void crossShaft(VertexConsumer consumer, PoseStack.Pose pose, double x, double y0, double z, double y1, double halfW, int r, int g, int b, int a) {
      quad(consumer, pose, x - halfW, y0, z, x + halfW, y0, z, x + halfW, y1, z, x - halfW, y1, z, r, g, b, a);
      quad(consumer, pose, x, y0, z - halfW, x, y0, z + halfW, x, y1, z + halfW, x, y1, z - halfW, r, g, b, a);
   }

   private static void billboard(PoseStack.Pose pose, VertexConsumer consumer, Vec3 centre, Vec3 view, double halfW, double halfH, int r, int g, int b, int a) {
      Vec3 upHint = Math.abs(view.y) > 0.98 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
      Vec3 right = view.cross(upHint).normalize().scale(halfW);
      Vec3 up = right.cross(view).normalize().scale(halfH);
      Vec3 a0 = centre.subtract(right).subtract(up);
      Vec3 b0 = centre.add(right).subtract(up);
      Vec3 c0 = centre.add(right).add(up);
      Vec3 d0 = centre.subtract(right).add(up);
      quad(consumer, pose, a0.x, a0.y, a0.z, b0.x, b0.y, b0.z, c0.x, c0.y, c0.z, d0.x, d0.y, d0.z, r, g, b, a);
   }

   private static void quad(VertexConsumer consumer, PoseStack.Pose pose, double ax, double ay, double az, double bx, double by, double bz, double cx, double cy, double cz, double dx, double dy, double dz, int r, int g, int b, int a) {
      vertex(consumer, pose, ax, ay, az, 0.0F, 0.0F, r, g, b, a);
      vertex(consumer, pose, bx, by, bz, 1.0F, 0.0F, r, g, b, a);
      vertex(consumer, pose, cx, cy, cz, 1.0F, 1.0F, r, g, b, a);
      vertex(consumer, pose, dx, dy, dz, 0.0F, 1.0F, r, g, b, a);
   }

   private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z, float u, float v, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)x, (float)y, (float)z)
         .setColor(r, g, b, a)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(pose, 0.0F, 1.0F, 0.0F);
   }
}
