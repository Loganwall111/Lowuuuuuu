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
 * The latest pass drops the old floating-strip look and instead builds each
 * storm cloud from shallow voxel prisms: chunky tops, darker sides, and softer
 * semi-transparent undersides so the mass feels like native Minecraft clouds
 * that have been storm-tinted, not billboard slabs hanging in the air.
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
      return mode > 0 && (ambientCloudsActive() || StormSkyDarken.globalCloudDeckActive() || StormSkyDarken.paletteBlend() > 0.03F && StormSkyDarken.palettePhase() >= 4.25F);
   }

   /** The MCSM clouds are the game's default cloud look, even with no storm anywhere. */
   public static boolean ambientCloudsActive() {
      return DevouringStormsClientConfig.ambientMcsmClouds;
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
      float purple = smooth(phase, 5.14F, 5.48F);
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

   static void slab(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z, double halfLen, double halfWid, double rot, int r, int g, int b, int a) {
      cloudPrism(consumer, pose, x, y, z, halfLen, halfWid, 0.14, rot, r, g, b, a, a, a);
   }

   static void cloudPrism(
      VertexConsumer consumer,
      PoseStack.Pose pose,
      double x,
      double y,
      double z,
      double halfLen,
      double halfWid,
      double halfTall,
      double rot,
      int r,
      int g,
      int b,
      int topA,
      int sideA,
      int bottomA
   ) {
      if (topA <= 1 || halfLen <= 0.01 || halfWid <= 0.01 || halfTall <= 0.01) {
         return;
      }

      double cosR = Math.cos(rot);
      double sinR = Math.sin(rot);
      double ux = cosR * halfLen;
      double uz = sinR * halfLen;
      double vx = -sinR * halfWid;
      double vz = cosR * halfWid;

      Vec3 t0 = new Vec3(x - ux - vx, y + halfTall, z - uz - vz);
      Vec3 t1 = new Vec3(x + ux - vx, y + halfTall, z + uz - vz);
      Vec3 t2 = new Vec3(x + ux + vx, y + halfTall, z + uz + vz);
      Vec3 t3 = new Vec3(x - ux + vx, y + halfTall, z - uz + vz);
      Vec3 b0 = new Vec3(x - ux - vx, y - halfTall, z - uz - vz);
      Vec3 b1 = new Vec3(x + ux - vx, y - halfTall, z + uz - vz);
      Vec3 b2 = new Vec3(x + ux + vx, y - halfTall, z + uz + vz);
      Vec3 b3 = new Vec3(x - ux + vx, y - halfTall, z - uz + vz);

      face(consumer, pose, t0, t1, t2, t3, r, g, b, topA);
      if (bottomA > 1) {
         face(consumer, pose, b3, b2, b1, b0, r, g, b, bottomA);
      }
      if (sideA > 1) {
         face(consumer, pose, t0, b0, b1, t1, r, g, b, sideA);
         face(consumer, pose, t1, b1, b2, t2, r, g, b, sideA);
         face(consumer, pose, t2, b2, b3, t3, r, g, b, sideA);
         face(consumer, pose, t3, b3, b0, t0, r, g, b, sideA);
      }
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
         int innerA = (int)(outerA * 0.70F);
         if (outerA <= 2) {
            continue;
         }

         double halfLen = (24.0 + 68.0 * hash01(entityId, i, 9)) * (0.95 + 0.32 * growthScale);
         double halfWid = (10.0 + 26.0 * hash01(entityId, i, 10)) * (0.95 + 0.26 * growthScale);
         double rot = hash01(entityId, i, 11) * Math.PI * 2.0 + nowSec * drift * 1.7;
         double halfTall = (3.5 + 10.0 * hash01(entityId, i, 12)) * (0.95 + 0.22 * growthScale);
         int outerSideA = (int)(outerA * 0.88F);
         int outerBottomA = (int)(outerA * 0.36F);
         cloudPrism(consumer, pose, x, y, z, halfLen, halfWid, halfTall, rot, or, og, ob, outerA, outerSideA, outerBottomA);

         double innerAlong = (hash01(entityId, i, 13) - 0.5) * halfLen * 0.18;
         double innerAcross = (hash01(entityId, i, 14) - 0.5) * halfWid * 0.16;
         double innerX = x + Math.cos(rot) * innerAlong - Math.sin(rot) * innerAcross;
         double innerZ = z + Math.sin(rot) * innerAlong + Math.cos(rot) * innerAcross;
         cloudPrism(consumer, pose, innerX, y + halfTall * 0.24, innerZ, halfLen * 0.64, halfWid * 0.62, halfTall * 0.58, rot, ir, ig, ib, innerA, (int)(innerA * 0.84F), (int)(innerA * 0.20F));

         float legRamp = smooth(phase, 4.55F, 5.35F);
         if (legRamp > 0.02F && hash01(entityId, i, 15) < (mode >= 2 ? 0.92F : 0.72F)) {
            int drops = 1 + (hash01(entityId, i, 16) < 0.42F ? 1 : 0) + (mode >= 2 && hash01(entityId, i, 17) < 0.18F ? 1 : 0);
            for (int part = 0; part < drops; part++) {
               double along = (hash01(entityId, i, 18 + part * 3) - 0.5) * halfLen * 0.72;
               double across = (hash01(entityId, i, 19 + part * 3) - 0.5) * halfWid * 0.68;
               double dropX = x + Math.cos(rot) * along - Math.sin(rot) * across;
               double dropZ = z + Math.sin(rot) * along + Math.cos(rot) * across;
               double dropHalfLen = Math.max(5.0, halfLen * (0.16 + 0.16 * hash01(entityId, i, 30 + part)));
               double dropHalfWid = Math.max(4.0, halfWid * (0.22 + 0.18 * hash01(entityId, i, 34 + part)));
               double dropHalfTall = (3.0 + 9.0 * hash01(entityId, i, 38 + part)) * (0.84 + 0.34 * growthScale) * (0.55 + 0.45 * legRamp);
               double dropY = y - halfTall - dropHalfTall * (0.70 + part * 1.16);
               int dropA = (int)(outerA * (0.56F + 0.18F * legRamp));
               int dropInnerA = (int)(innerA * (0.42F + 0.20F * legRamp));
               cloudPrism(consumer, pose, dropX, dropY, dropZ, dropHalfLen, dropHalfWid, dropHalfTall, rot, or, og, ob, dropA, (int)(dropA * 0.84F), (int)(dropA * 0.18F));
               cloudPrism(consumer, pose, dropX, dropY + dropHalfTall * 0.22, dropZ, dropHalfLen * 0.58, dropHalfWid * 0.56, dropHalfTall * 0.50, rot, ir, ig, ib, dropInnerA, (int)(dropInnerA * 0.82F), (int)(dropInnerA * 0.12F));
            }
         }
      }
   }

   public static void submit(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      int mode = (int)Math.round(DevouringStormsClientConfig.stormCloudDeck);
      var storms = ClientDistantStormManager.all();
      boolean global = StormSkyDarken.globalCloudDeckActive();
      boolean ambient = ambientCloudsActive();
      if (mode <= 0 || mc.level == null || storms.isEmpty() && !global && !ambient) {
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

         // Default game clouds: with no storm around, the same chunky voxel
         // language runs as the world's normal weather in neutral colours -
         // Story-Mode-shaped clouds that feel native, not a temporary effect.
         if (storms.isEmpty() && !global && ambient) {
            renderAmbient(consumer, pose, cam, nowSec, coverage, mc);
         }
      });
   }

   /** Neutral day/night tint for the always-on MCSM cloud deck. */
   private static void renderAmbient(VertexConsumer consumer, PoseStack.Pose pose, Vec3 cam, float nowSec, float coverage, Minecraft mc) {
      long t = mc.level.getOverworldClockTime() % 24000L;
      float day;
      if (t < 12500L) {
         day = 1.0F;
      } else if (t < 13500L) {
         day = 1.0F - (float)(t - 12500L) / 1000.0F;
      } else if (t < 22500L) {
         day = 0.0F;
      } else if (t < 23500L) {
         day = (float)(t - 22500L) / 1000.0F;
      } else {
         day = 1.0F;
      }

      float bright = 0.24F + 0.76F * day;
      // day: near-white with a hint of blue; night: deep indigo / storm-blue
      float or = Mth.lerp(bright, 0.115F, 0.965F);
      float og = Mth.lerp(bright, 0.115F, 0.975F);
      float ob = Mth.lerp(bright, 0.165F, 0.995F);
      float ir = Mth.lerp(bright, 0.16F, 0.99F);
      float ig = Mth.lerp(bright, 0.15F, 0.995F);
      float ib = Mth.lerp(bright, 0.235F, 1.0F);
      int orI = (int)(Mth.clamp(or, 0.0F, 1.0F) * 255.0F);
      int ogI = (int)(Mth.clamp(og, 0.0F, 1.0F) * 255.0F);
      int obI = (int)(Mth.clamp(ob, 0.0F, 1.0F) * 255.0F);
      int irI = (int)(Mth.clamp(ir, 0.0F, 1.0F) * 255.0F);
      int igI = (int)(Mth.clamp(ig, 0.0F, 1.0F) * 255.0F);
      int ibI = (int)(Mth.clamp(ib, 0.0F, 1.0F) * 255.0F);

      // anchor the field to a coarse world grid so the clouds feel world-fixed
      double anchorX = Math.round(cam.x / 384.0) * 384.0;
      double anchorZ = Math.round(cam.z / 384.0) * 384.0;
      double cloudY = 132.0 + (double)DevouringStormsClientConfig.stormCloudAltitude * 2.0;
      int slabs = Mth.clamp((int)(44.0F * coverage), 10, 110);
      int seed = 24601;

      for (int i = 0; i < slabs; i++) {
         double rad = (0.18 + 0.92 * hash01(seed, i, 1)) * 340.0;
         double drift = (0.0011 + 0.0022 * hash01(seed, i, 3)) * (hash01(seed, i, 4) < 0.5 ? -1.0 : 1.0);
         double ang = hash01(seed, i, 2) * Math.PI * 2.0 + nowSec * drift;
         double x = anchorX + Math.cos(ang) * rad;
         double z = anchorZ + Math.sin(ang) * rad;
         double y = cloudY + (hash01(seed, i, 5) - 0.5) * 14.0 + Math.sin(nowSec * 0.02 + hash01(seed, i, 7) * 6.28) * 1.5;
         double dist = cam.distanceTo(new Vec3(x, y, z));
         if (dist > MAX_VIEW_DIST) {
            continue;
         }

         float distFade = dist < 70.0 ? (float)(dist / 70.0) : Mth.clamp(1.0F - (float)((dist - 640.0) / 260.0), 0.0F, 1.0F);
         float alpha = 0.62F * (0.7F + 0.3F * hash01(seed, i, 8)) * distFade;
         int outerA = (int)(alpha * 255.0F);
         if (outerA <= 2) {
            continue;
         }

         double halfLen = (26.0 + 58.0 * hash01(seed, i, 9));
         double halfWid = (12.0 + 22.0 * hash01(seed, i, 10));
         double rot = hash01(seed, i, 11) * Math.PI * 2.0 + nowSec * drift * 1.4;
         double halfTall = 3.5 + 6.5 * hash01(seed, i, 12);
         // semi-transparent bottoms (~0.4-0.6 of the top alpha) so the sky
         // shows through from below, chunky darker sides, solid tops
         cloudPrism(consumer, pose, x, y, z, halfLen, halfWid, halfTall, rot, orI, ogI, obI, outerA, (int)(outerA * 0.88F), (int)(outerA * 0.52F));
         cloudPrism(consumer, pose, x, y + halfTall * 0.26, z, halfLen * 0.62, halfWid * 0.6, halfTall * 0.55, rot, irI, igI, ibI, (int)(outerA * 0.6F), (int)(outerA * 0.5F), (int)(outerA * 0.3F));
      }
   }

   private static void face(VertexConsumer consumer, PoseStack.Pose pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int r, int g, int bl, int alpha) {
      Vec3 normal = b.subtract(a).cross(c.subtract(a)).normalize();
      vertex(consumer, pose, a, 0.0F, 0.0F, r, g, bl, alpha, normal);
      vertex(consumer, pose, b, 1.0F, 0.0F, r, g, bl, alpha, normal);
      vertex(consumer, pose, c, 1.0F, 1.0F, r, g, bl, alpha, normal);
      vertex(consumer, pose, d, 0.0F, 1.0F, r, g, bl, alpha, normal);
   }

   private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 at, float u, float v, int r, int g, int b, int a, Vec3 normal) {
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z)
         .setColor(r, g, b, a)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(FULL_BRIGHT)
         .setNormal(pose, (float)normal.x, (float)normal.y, (float)normal.z);
   }
}
