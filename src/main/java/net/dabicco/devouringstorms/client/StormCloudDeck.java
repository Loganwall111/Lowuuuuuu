package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
 * StormCloudDeck — the blocky Story-Mode weather mass that replaces vanilla
 * clouds around an active storm.
 *
 * The user asked for two things this pass:
 *  - the deck should read as MCSM's chunky slabs instead of vanilla clouds
 *  - those slabs should carry a pink-leaning white inner body plus the long
 *    hanging cloud legs seen in the references, fading away inside themselves
 *    instead of ending as hard flat bars.
 *
 * We therefore draw each slab as a tinted outer shell plus a smaller brighter
 * inner sheet, then hang translucent crossed curtains from many slabs so the
 * cloud mass trails downward in soft square legs. A separate upper-sky canopy
 * still fills the top of the sky so the storm colour reaches full strength
 * without requiring manual config tweaks.
 */
public final class StormCloudDeck {
   static final Identifier SLAB = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/mcsm_cloud.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final double MAX_VIEW_DIST = 900.0;

   private StormCloudDeck() {
   }

   /** Whether the stylized MCSM deck should take over from vanilla clouds right now. */
   public static boolean replacesVanillaClouds() {
      int mode = (int)Math.round(DevouringStormsClientConfig.stormCloudDeck);
      return mode > 0 && (StormSkyDarken.globalCloudDeckActive() || StormSkyDarken.paletteBlend() > 0.03F && StormSkyDarken.palettePhase() >= 4.25F);
   }

   /** cheap deterministic hash -> [0,1) */
   private static float hash01(int seed, int i, int slot) {
      int h = seed * 7919 + i * 104729 + slot * 130363;
      h ^= h >>> 13;
      h *= 1274126177;
      h ^= h >>> 16;
      return (float)(h & 0xFFFF) / 65536.0F;
   }

   static float smooth(float value, float start, float end) {
      float t = Mth.clamp((value - start) / (end - start), 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   static void colorsForPhase(float phase, float[] outer, float[] inner) {
      float[] tint = StormPalettes.cloudColor(phase, new float[3]);
      float purple = smooth(phase, 5.34F, 5.76F);
      float cataclysm = smooth(phase, 5.90F, 6.18F);
      float purpleR = Mth.lerp(cataclysm, 0.54F, 0.24F);
      float purpleG = Mth.lerp(cataclysm, 0.41F, 0.16F);
      float purpleB = Mth.lerp(cataclysm, 0.63F, 0.32F);
      outer[0] = Mth.lerp(purple, tint[0], purpleR);
      outer[1] = Mth.lerp(purple, tint[1], purpleG);
      outer[2] = Mth.lerp(purple, tint[2], purpleB);

      float whiteMix = Mth.lerp(cataclysm, 0.84F, 0.44F);
      inner[0] = Mth.lerp(whiteMix, outer[0], 0.98F);
      inner[1] = Mth.lerp(whiteMix, outer[1], 0.985F);
      inner[2] = Mth.lerp(whiteMix, outer[2], 1.0F);
   }

   static void slab(
      VertexConsumer consumer,
      PoseStack.Pose pose,
      double x,
      double y,
      double z,
      double halfLen,
      double halfWid,
      double rot,
      int r,
      int g,
      int b,
      int a
   ) {
      double cosR = Math.cos(rot);
      double sinR = Math.sin(rot);
      double ux = cosR * halfLen;
      double uz = sinR * halfLen;
      double vx = -sinR * halfWid;
      double vz = cosR * halfWid;
      vertex(consumer, pose, x - ux - vx, y, z - uz - vz, 0.0F, 0.0F, r, g, b, a);
      vertex(consumer, pose, x + ux - vx, y, z + uz - vz, 1.0F, 0.0F, r, g, b, a);
      vertex(consumer, pose, x + ux + vx, y, z + uz + vz, 1.0F, 1.0F, r, g, b, a);
      vertex(consumer, pose, x - ux + vx, y, z - uz + vz, 0.0F, 1.0F, r, g, b, a);
   }

   static void hangingLeg(
      VertexConsumer consumer,
      PoseStack.Pose pose,
      double x,
      double topY,
      double z,
      double halfSpan,
      double rot,
      double height,
      int r,
      int g,
      int b,
      int topA,
      int bottomA
   ) {
      double cosR = Math.cos(rot);
      double sinR = Math.sin(rot);
      double ux = cosR * halfSpan;
      double uz = sinR * halfSpan;
      legVertex(consumer, pose, x - ux, topY, z - uz, 0.0F, 0.0F, r, g, b, topA);
      legVertex(consumer, pose, x + ux, topY, z + uz, 1.0F, 0.0F, r, g, b, topA);
      legVertex(consumer, pose, x + ux, topY - height, z + uz, 1.0F, 1.0F, r, g, b, bottomA);
      legVertex(consumer, pose, x - ux, topY - height, z - uz, 0.0F, 1.0F, r, g, b, bottomA);
   }

   static void crossLegs(
      VertexConsumer consumer,
      PoseStack.Pose pose,
      double x,
      double topY,
      double z,
      double halfSpan,
      double height,
      double rot,
      int outerR,
      int outerG,
      int outerB,
      int innerR,
      int innerG,
      int innerB,
      int outerA,
      int innerA
   ) {
      hangingLeg(consumer, pose, x, topY, z, halfSpan, rot, height, outerR, outerG, outerB, outerA, 0);
      hangingLeg(consumer, pose, x, topY, z, halfSpan * 0.92, rot + Math.PI / 2.0, height * 0.92, outerR, outerG, outerB, (int)(outerA * 0.86F), 0);
      hangingLeg(consumer, pose, x, topY + 0.08, z, halfSpan * 0.56, rot, height * 0.76, innerR, innerG, innerB, innerA, 0);
      hangingLeg(consumer, pose, x, topY + 0.08, z, halfSpan * 0.48, rot + Math.PI / 2.0, height * 0.72, innerR, innerG, innerB, (int)(innerA * 0.82F), 0);
   }

   private static void renderField(
      VertexConsumer consumer,
      PoseStack.Pose pose,
      Vec3 cam,
      float nowSec,
      int mode,
      float coverage,
      float baseAlpha,
      int entityId,
      float phase,
      float expansionPhase,
      double dispX,
      double dispY,
      double dispZ,
      float presence,
      float[] outer,
      float[] inner
   ) {
      if (phase < 4.25F || presence <= 0.01F) {
         return;
      }
      float phaseRamp = smooth(phase, 4.25F, 5.05F);
      float growthScale = (float)WitherStormEntity.clientGrowthScaleForPhase(Math.max(phase, expansionPhase));
      double spread = (130.0 + 90.0 * Math.min(phase, 6.0F)) * (0.9 + 0.45 * growthScale);
      int slabs = Mth.clamp((int)(28.0F * coverage * (mode >= 2 ? 1.8F : 1.0F) * (0.45F + 0.55F * phaseRamp) * Math.min(2.6F, 0.8F + growthScale * 0.6F) * (0.55F + 0.45F * presence)), 6, 160);
      colorsForPhase(phase, outer, inner);
      float outerLum = 0.82F + 0.12F * Mth.sin((nowSec / Math.max(0.5F, (float)DevouringStormsClientConfig.pulsePeriod) + (entityId % 977) * 0.6183F) * (float)(Math.PI * 2.0));
      int or = (int)(Mth.clamp(outer[0] * outerLum, 0.0F, 1.0F) * 255.0F);
      int og = (int)(Mth.clamp(outer[1] * outerLum, 0.0F, 1.0F) * 255.0F);
      int ob = (int)(Mth.clamp(outer[2] * outerLum, 0.0F, 1.0F) * 255.0F);
      int ir = (int)(Mth.clamp(inner[0], 0.0F, 1.0F) * 255.0F);
      int ig = (int)(Mth.clamp(inner[1], 0.0F, 1.0F) * 255.0F);
      int ib = (int)(Mth.clamp(inner[2], 0.0F, 1.0F) * 255.0F);

      for (int i = 0; i < slabs; i++) {
         double rad = (0.45 + 0.8 * hash01(entityId, i, 1)) * spread;
         double ang0 = hash01(entityId, i, 2) * Math.PI * 2.0;
         double drift = (0.004 + 0.012 * hash01(entityId, i, 3)) * (hash01(entityId, i, 4) < 0.5 ? -1.0 : 1.0);
         double ang = ang0 + nowSec * drift;
         double x = dispX + Math.cos(ang) * rad;
         double z = dispZ + Math.sin(ang) * rad;
         double alt = (hash01(entityId, i, 5) - 0.35) * (50.0 + 30.0 * Math.min(phase, 6.0F)) * (0.9 + 0.22 * growthScale) + (float)DevouringStormsClientConfig.stormCloudAltitude;
         double y = dispY - 20.0 + alt + Math.sin(nowSec * 0.05 * (0.5 + hash01(entityId, i, 6)) + hash01(entityId, i, 7) * 6.28) * (4.0 + growthScale * 1.5);
         double dist = cam.distanceTo(new Vec3(x, y, z));
         if (dist > MAX_VIEW_DIST) {
            continue;
         }
         float distFade = dist < 60.0 ? (float)(dist / 60.0) : Mth.clamp(1.0F - (float)((dist - 650.0) / 250.0), 0.0F, 1.0F);
         float alpha = baseAlpha * presence * phaseRamp * (0.58F + 0.42F * hash01(entityId, i, 8)) * distFade;
         int outerA = (int)(alpha * 255.0F);
         int innerA = (int)(outerA * 0.68F);
         if (outerA <= 2) {
            continue;
         }

         double halfLen = (24.0 + 68.0 * hash01(entityId, i, 9)) * (0.95 + 0.32 * growthScale);
         double halfWid = (10.0 + 26.0 * hash01(entityId, i, 10)) * (0.95 + 0.26 * growthScale);
         double rot = hash01(entityId, i, 11) * Math.PI * 2.0 + nowSec * drift * 1.7;
         slab(consumer, pose, x, y, z, halfLen, halfWid, rot, or, og, ob, outerA);
         slab(consumer, pose, x, y + 0.12, z, halfLen * 0.72, halfWid * 0.62, rot, ir, ig, ib, innerA);

         float legRamp = smooth(phase, 4.55F, 5.35F);
         if (legRamp > 0.02F && hash01(entityId, i, 12) < (mode >= 2 ? 0.84F : 0.58F)) {
            double along = (hash01(entityId, i, 13) - 0.5) * halfLen * 0.78;
            double across = (hash01(entityId, i, 14) - 0.5) * halfWid * 0.60;
            double legX = x + Math.cos(rot) * along - Math.sin(rot) * across;
            double legZ = z + Math.sin(rot) * along + Math.cos(rot) * across;
            double legHeight = (12.0 + 48.0 * hash01(entityId, i, 15)) * (0.88 + 0.42 * growthScale) * (0.55 + 0.45 * legRamp);
            double legSpan = Math.max(5.5, Math.min(halfLen, halfWid) * (0.42 + 0.34 * hash01(entityId, i, 16)));
            int legOuterA = (int)(outerA * (0.44F + 0.22F * legRamp));
            int legInnerA = (int)(innerA * (0.42F + 0.24F * legRamp));
            crossLegs(consumer, pose, legX, y - 0.06, legZ, legSpan, legHeight, rot, or, og, ob, ir, ig, ib, legOuterA, legInnerA);
         }
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
      float coverage = (float)DevouringStormsClientConfig.stormCloudCoverage;
      if (coverage <= 0.05F) {
         return;
      }

      float gt = (float)(mc.level.getGameTime() % 240000L) + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      float nowSec = gt * 0.05F;
      Vec3 cam = ctx.levelState().cameraRenderState.pos;
      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();
      float paletteClaim = Math.max(Mth.clamp(StormSkyDarken.paletteBlend(), 0.0F, 1.0F), StormSkyDarken.globalBlend());
      float baseAlpha = (mode >= 2 ? 0.21F : 0.14F) * Mth.lerp(paletteClaim, 0.82F, 1.24F);

      collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(SLAB), (pose, consumer) -> {
         float[] outer = new float[3];
         float[] inner = new float[3];

         for (ClientDistantStormManager.StormData d : storms) {
            renderField(consumer, pose, cam, nowSec, mode, coverage, baseAlpha, d.entityId, d.phase, d.expansionPhase, d.dispX, d.dispY, d.dispZ, 1.0F, outer, inner);
         }

         if (global) {
            float phase = StormSkyDarken.globalPhase();
            float strength = StormSkyDarken.globalBlend();
            renderField(consumer, pose, cam, nowSec, mode, coverage, baseAlpha, 246810, phase, phase, cam.x, cam.y + 92.0 + (double)(phase - 4.5F) * 8.0, cam.z, strength, outer, inner);
         }
      });
   }

   private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z, float u, float v, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)x, (float)y, (float)z).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }

   private static void legVertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z, float u, float v, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)x, (float)y, (float)z).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
   }
}
