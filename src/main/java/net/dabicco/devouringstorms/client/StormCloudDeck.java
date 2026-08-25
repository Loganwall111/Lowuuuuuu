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
 * StormCloudDeck — the elevated Story-Mode cloud ceiling.
 *
 * The deck renders a high, chunky, blocky voxel cloud grid (MCSM style) that
 * lives far up in the atmosphere instead of hanging around the storm:
 *
 *  - the baseline deck sits at Y=258+, stretched far past the old render
 *    bounds so the ceiling reads as a continuous sky without edge clipping;
 *  - prisms snap to a coarse world grid so the layout reads as the classic
 *    Story-Mode blocky cloud pattern rather than random blobs;
 *  - faces are directionally lit from the celestial sun/moon vector: crisp
 *    full-bright tops, softly shadowed sides, deep ambient-shadow bottoms
 *    with a translucent fade that blends into the sky fog;
 *  - with no storm anywhere this deck is the game's default cloud look
 *    (near-white by day, deep indigo at night); a storm only retints it.
 */
public final class StormCloudDeck {
   static final Identifier SLAB = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/mcsm_cloud.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final double MAX_VIEW_DIST = 1800.0;
   /** Baseline ceiling height for the always-on ambient deck. */
   private static final double AMBIENT_CLOUD_Y = 258.0;
   /** World grid (blocks) the chunky MCSM cloud pattern snaps to. */
   private static final double GRID = 24.0;

   private StormCloudDeck() {
   }

   /** Whether the stylized MCSM deck should take over from vanilla clouds right now. */
   public static boolean replacesVanillaClouds() {
      int mode = (int)Math.round(DevouringStormsClientConfig.stormCloudDeck);
      return mode > 0 && (ambientCloudsActive() || StormSkyDarken.globalCloudDeckActive() || StormSkyDarken.paletteBlend() > 0.03F && StormSkyDarken.palettePhase() >= 4.25F);
   }

   /** The MCSM clouds are the game's default cloud look, even with no storm anywhere. */
   public static boolean ambientCloudsActive() {
      if (!DevouringStormsClientConfig.ambientMcsmClouds) {
         return false;
      }
      // Only sky dimensions get the always-on deck - never the Nether's ceiling.
      Minecraft mc = Minecraft.getInstance();
      return mc.level != null && mc.level.dimensionType().hasSkyLight();
   }

   /** cheap deterministic hash -> [0,1) */
   static float hash01(int seed, int i, int slot) {
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
      float cataclysm = smooth(phase, 5.9F, 6.18F);
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

   /**
    * Normalized celestial light direction (points from the world toward the
    * sun during the day and the moon at night) plus a brightness factor.
    * The sun rides the vanilla east-west arc: noon at 6000, midnight 18000.
    */
   static double[] sunDirection(double overworldClock) {
      double t = (overworldClock % 24000L) / 24000.0;
      double ang = (t - 0.25) * Math.PI * 2.0;
      double sy = Math.cos(ang);
      double sx = Math.sin(ang);
      double len = Math.sqrt(sx * sx + sy * sy);
      if (len < 1.0E-4) {
         return new double[]{0.0, 1.0, 0.0, 1.0};
      }
      sx /= len;
      sy /= len;
      // Light strength on a smooth ramp: 1 at noon, ~0.37 at the horizon,
      // 0.16 floor at midnight. Moonlight is real but faint -- treating the
      // full moon as 80 percent of daylight made the whole cloud ceiling
      // glare near-white at midnight, which read as a broken night sky.
      double bright = 0.16 + 0.84 * (double)Mth.clamp((float)(sy * 1.4 + 0.25), 0.0F, 1.0F);
      // at night the moon is up: light comes from the mirrored arc
      double dirY = sy >= 0.0 ? sy : -sy;
      double dirX = sx;
      return new double[]{dirX, dirY, 0.0, bright};
   }

   /** Directional face shade for a unit side normal against the celestial vector. */
   static float sideShade(double nx, double ny, double nz, double[] sun) {
      float dot = (float)(nx * sun[0] + ny * sun[1] + nz * sun[2]);
      float lit = Mth.clamp(dot, 0.0F, 1.0F);
      return (0.60F + 0.40F * lit) * (float)sun[3];
   }

   static void slab(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z, double halfLen, double halfWid, double rot, int r, int g, int b, int a) {
      cloudPrism(consumer, pose, x, y, z, halfLen, halfWid, 0.14, rot, r, g, b, a, a, a);
   }

   /**
    * Legacy flat-shaded prism kept for compatibility; new deck geometry goes
    * through {@link #cloudPrismShaded}.
    */
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
      cloudPrismShaded(consumer, pose, x, y, z, halfLen, halfWid, halfTall, rot, r, g, b, topA, sideA, bottomA, 0.0, 1.0, 0.0, 1.0);
   }

   /**
    * Voxel cloud prism with directional lighting: tops stay full-bright, each
    * side face is softly shadowed from the celestial sun/moon vector, and the
    * bottom face drops into deep ambient shadow with a translucent fade that
    * melts into the sky fog below the deck.
    */
   static void cloudPrismShaded(
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
      int bottomA,
      double sunX,
      double sunY,
      double sunZ,
      double sunBright
   ) {
      if (topA <= 1 || halfLen <= 0.01 || halfWid <= 0.01 || halfTall <= 0.01) {
         return;
      }
      double[] sun = new double[]{sunX, sunY, sunZ, sunBright};

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

      // top: crisp full-bright white-ish cap
      face(consumer, pose, t0, t1, t2, t3, r, g, b, topA);
      if (bottomA > 1) {
         // bottom: deep ambient shadow + translucent fade into the sky fog
         int br = (int)(r * 0.42F);
         int bg = (int)(g * 0.44F);
         int bb = (int)(b * 0.52F);
         face(consumer, pose, b3, b2, b1, b0, br, bg, bb, bottomA);
      }
      if (sideA > 1) {
         // four sides, each shaded by its own outward normal vs the sun arc
         float sNv = sideShade(-sinR, 0.0, -cosR, sun);
         float sUv = sideShade(cosR, 0.0, -sinR, sun);
         float sPv = sideShade(sinR, 0.0, cosR, sun);
         float sNu = sideShade(-cosR, 0.0, sinR, sun);
         face(consumer, pose, t0, b0, b1, t1, sh(r, sNv), sh(g, sNv), sh(b, sNv), sideA);
         face(consumer, pose, t1, b1, b2, t2, sh(r, sUv), sh(g, sUv), sh(b, sUv), sideA);
         face(consumer, pose, t2, b2, b3, t3, sh(r, sPv), sh(g, sPv), sh(b, sPv), sideA);
         face(consumer, pose, t3, b3, b0, t0, sh(r, sNu), sh(g, sNu), sh(b, sNu), sideA);
      }
   }

   private static int sh(int channel, float shade) {
      return Mth.clamp((int)((float)channel * shade), 0, 255);
   }

   /** Snap a world coordinate onto the chunky MCSM cloud grid. */
   private static double snapGrid(double v) {
      return Math.round(v / GRID) * GRID;
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
      // the weather mass stretches wide and HIGH around a storm: it is a
      // ceiling above the storm now, not a low fog band
      double spread = (420.0 + 260.0 * Math.min(phase, 7.0F)) * (0.9 + 0.5 * growthScale);
      int slabs = Mth.clamp((int)(40.0F * coverage * (mode >= 2 ? 1.7F : 1.0F) * (0.5F + 0.5F * phaseRamp) * Math.min(2.4F, 0.9F + growthScale * 0.5F) * (0.55F + 0.45F * presence)), 8, 190);
      colorsForPhase(phase, outer, inner);
      Minecraft mc = Minecraft.getInstance();
      double clock = mc.level != null ? (double)mc.level.getOverworldClockTime() + (double)mc.getDeltaTracker().getGameTimeDeltaPartialTick(false) : 0.0;
      double[] sun = sunDirection(clock);
      float outerLum = 0.86F + 0.10F * Mth.sin((float)(nowSec / Math.max(0.5F, (float)DevouringStormsClientConfig.pulsePeriod) + (float)(entityId % 977) * 0.6183F) * (float)(Math.PI * 2.0));
      int or = (int)(Mth.clamp(outer[0] * outerLum, 0.0F, 1.0F) * 255.0F);
      int og = (int)(Mth.clamp(outer[1] * outerLum, 0.0F, 1.0F) * 255.0F);
      int ob = (int)(Mth.clamp(outer[2] * outerLum, 0.0F, 1.0F) * 255.0F);
      int ir = (int)(Mth.clamp(inner[0], 0.0F, 1.0F) * 255.0F);
      int ig = (int)(Mth.clamp(inner[1], 0.0F, 1.0F) * 255.0F);
      int ib = (int)(Mth.clamp(inner[2], 0.0F, 1.0F) * 255.0F);

      for (int i = 0; i < slabs; i++) {
         double rad = (0.3 + 0.78 * hash01(entityId, i, 1)) * spread;
         double ang0 = hash01(entityId, i, 2) * Math.PI * 2.0;
         double drift = (0.0022 + 0.006 * hash01(entityId, i, 3)) * (hash01(entityId, i, 4) < 0.5 ? -1.0 : 1.0);
         double ang = ang0 + nowSec * drift;
         double x = snapGrid(dispX + Math.cos(ang) * rad);
         double z = snapGrid(dispZ + Math.sin(ang) * rad);
         double alt = (40.0 + 130.0 * hash01(entityId, i, 5)) * (0.8 + 0.3 * Math.min(phase, 7.0F)) * (0.9 + 0.18 * growthScale);
         double y = snapGrid(dispY + alt) + (double)DevouringStormsClientConfig.stormCloudAltitude;
         double dist = cam.distanceTo(new Vec3(x, y, z));
         if (dist > MAX_VIEW_DIST) {
            continue;
         }

         float distFade = dist < 90.0 ? (float)(dist / 90.0) : Mth.clamp(1.0F - (float)((dist - 1250.0) / 550.0), 0.0F, 1.0F);
         float alpha = baseAlpha * presence * phaseRamp * (0.62F + 0.38F * hash01(entityId, i, 8)) * distFade;
         int outerA = (int)(alpha * 255.0F);
         int innerA = (int)(outerA * 0.7F);
         if (outerA <= 2) {
            continue;
         }

         double halfLen = (30.0 + 78.0 * hash01(entityId, i, 9)) * (0.95 + 0.3 * growthScale);
         double halfWid = (12.0 + 30.0 * hash01(entityId, i, 10)) * (0.95 + 0.24 * growthScale);
         double rot = Math.round((hash01(entityId, i, 11) * Math.PI * 2.0) / (Math.PI * 0.5)) * Math.PI * 0.5 + nowSec * drift * 1.2;
         double halfTall = (4.0 + 12.0 * hash01(entityId, i, 12)) * (0.95 + 0.2 * growthScale);
         int outerSideA = (int)(outerA * 0.9F);
         int outerBottomA = (int)(outerA * 0.5F);
         cloudPrismShaded(consumer, pose, x, y, z, halfLen, halfWid, halfTall, rot, or, og, ob, outerA, outerSideA, outerBottomA, sun[0], sun[1], sun[2], sun[3]);

         double innerAlong = (hash01(entityId, i, 13) - 0.5) * halfLen * 0.18;
         double innerAcross = (hash01(entityId, i, 14) - 0.5) * halfWid * 0.16;
         double innerX = x + Math.cos(rot) * innerAlong - Math.sin(rot) * innerAcross;
         double innerZ = z + Math.sin(rot) * innerAlong + Math.cos(rot) * innerAcross;
         cloudPrismShaded(consumer, pose, innerX, y + halfTall * 0.24, innerZ, halfLen * 0.64, halfWid * 0.62, halfTall * 0.58, rot, ir, ig, ib, innerA, (int)(innerA * 0.86F), (int)(innerA * 0.36F), sun[0], sun[1], sun[2], sun[3]);

         float legRamp = smooth(phase, 4.55F, 5.35F);
         if (legRamp > 0.02F && hash01(entityId, i, 15) < (mode >= 2 ? 0.9F : 0.7F)) {
            int drops = 1 + (hash01(entityId, i, 16) < 0.42F ? 1 : 0);
            for (int part = 0; part < drops; part++) {
               double along = (hash01(entityId, i, 18 + part * 3) - 0.5) * halfLen * 0.72;
               double across = (hash01(entityId, i, 19 + part * 3) - 0.5) * halfWid * 0.68;
               double dropX = snapGrid(x + Math.cos(rot) * along - Math.sin(rot) * across);
               double dropZ = snapGrid(z + Math.sin(rot) * along + Math.cos(rot) * across);
               double dropHalfLen = Math.max(6.0, halfLen * (0.16 + 0.16 * hash01(entityId, i, 30 + part)));
               double dropHalfWid = Math.max(6.0, halfWid * (0.22 + 0.18 * hash01(entityId, i, 34 + part)));
               double dropHalfTall = (3.0 + 10.0 * hash01(entityId, i, 38 + part)) * (0.84 + 0.3 * growthScale) * (0.55F + 0.45F * legRamp);
               double dropY = y - halfTall - dropHalfTall * (0.7 + part * 1.1);
               int dropA = (int)(outerA * (0.56F + 0.18F * legRamp));
               int dropInnerA = (int)(innerA * (0.42F + 0.2F * legRamp));
               cloudPrismShaded(consumer, pose, dropX, dropY, dropZ, dropHalfLen, dropHalfWid, dropHalfTall, rot, or, og, ob, dropA, (int)(dropA * 0.86F), (int)(dropA * 0.42F), sun[0], sun[1], sun[2], sun[3]);
               cloudPrismShaded(consumer, pose, dropX, dropY + dropHalfTall * 0.22, dropZ, dropHalfLen * 0.58, dropHalfWid * 0.56, dropHalfTall * 0.5, rot, ir, ig, ib, dropInnerA, (int)(dropInnerA * 0.84F), (int)(dropInnerA * 0.34F), sun[0], sun[1], sun[2], sun[3]);
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
      float baseAlpha = (mode >= 2 ? 0.3F : 0.2F) * Mth.lerp(paletteClaim, 0.9F, 1.25F);

      collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(SLAB), (pose, consumer) -> {
         float[] outer = new float[3];
         float[] inner = new float[3];

         for (ClientDistantStormManager.StormData d : storms) {
            // once the anomaly dome owns the sky (phase ~5.5+) the deck's
            // prisms melt away - the uploaded anomaly plate is a sky WITHOUT
            // clouds and our synthetic ones must not fight it
            float veil = 1.0F - StormSkyDome.domeVeil(d.phase);
            renderField(consumer, pose, cam, nowSec, mode, coverage, baseAlpha, d.entityId, d.phase, d.expansionPhase, d.dispX, d.dispY, d.dispZ, veil, outer, inner);
         }

         if (global) {
            float phase = StormSkyDarken.globalPhase();
            float strength = StormSkyDarken.globalBlend();
            renderField(consumer, pose, cam, nowSec, mode, coverage, baseAlpha, 246810, phase, phase, cam.x, AMBIENT_CLOUD_Y, cam.z, strength, outer, inner);
         }

         // Default game clouds: with no storm around, the elevated blocky deck
         // runs as the world's normal ceiling in neutral colours.
         if (storms.isEmpty() && !global && ambient) {
            renderAmbient(consumer, pose, cam, nowSec, coverage, mc);
         }
      });
   }

   /** Neutral day/night tint for the always-on MCSM cloud ceiling. */
   private static void renderAmbient(VertexConsumer consumer, PoseStack.Pose pose, Vec3 cam, float nowSec, float coverage, Minecraft mc) {
      double clock = (double)mc.level.getOverworldClockTime() + (double)mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      double[] sun = sunDirection(clock);
      float dayBright = (float)sun[3];

      // day: near-white with a hint of blue; night: deep indigo / storm-blue
      float or = Mth.lerp(dayBright, 0.128F, 0.955F);
      float og = Mth.lerp(dayBright, 0.126F, 0.965F);
      float ob = Mth.lerp(dayBright, 0.185F, 0.985F);
      float ir = Mth.lerp(dayBright, 0.175F, 0.99F);
      float ig = Mth.lerp(dayBright, 0.165F, 0.995F);
      float ib = Mth.lerp(dayBright, 0.245F, 1.0F);
      int orI = (int)(Mth.clamp(or, 0.0F, 1.0F) * 255.0F);
      int ogI = (int)(Mth.clamp(og, 0.0F, 1.0F) * 255.0F);
      int obI = (int)(Mth.clamp(ob, 0.0F, 1.0F) * 255.0F);
      int irI = (int)(Mth.clamp(ir, 0.0F, 1.0F) * 255.0F);
      int igI = (int)(Mth.clamp(ig, 0.0F, 1.0F) * 255.0F);
      int ibI = (int)(Mth.clamp(ib, 0.0F, 1.0F) * 255.0F);

      // world-anchored grid tiles so the ceiling feels fixed and endless
      double anchorX = Math.round(cam.x / (GRID * 4.0)) * GRID * 4.0;
      double anchorZ = Math.round(cam.z / (GRID * 4.0)) * GRID * 4.0;
      double cloudY = AMBIENT_CLOUD_Y + (double)DevouringStormsClientConfig.stormCloudAltitude;
      int slabs = Mth.clamp((int)(64.0F * coverage), 16, 170);
      int seed = 24601;

      for (int i = 0; i < slabs; i++) {
         double rad = (0.12 + 0.95 * hash01(seed, i, 1)) * 620.0;
         double drift = (0.0016 + 0.003 * hash01(seed, i, 3)) * (hash01(seed, i, 4) < 0.5 ? -1.0 : 1.0);
         double ang = hash01(seed, i, 2) * Math.PI * 2.0 + nowSec * drift;
         double x = snapGrid(anchorX + Math.cos(ang) * rad);
         double z = snapGrid(anchorZ + Math.sin(ang) * rad);
         double y = snapGrid(cloudY + (hash01(seed, i, 5) - 0.5) * 36.0);
         double dist = cam.distanceTo(new Vec3(x, y, z));
         if (dist > MAX_VIEW_DIST) {
            continue;
         }

         float distFade = dist < 110.0 ? (float)(dist / 110.0) : Mth.clamp(1.0F - (float)((dist - 1300.0) / 500.0), 0.0F, 1.0F);
         float alpha = 0.6F * (0.72F + 0.28F * hash01(seed, i, 8)) * distFade;
         int outerA = (int)(alpha * 255.0F);
         if (outerA <= 2) {
            continue;
         }

         double halfLen = 30.0 + 66.0 * hash01(seed, i, 9);
         double halfWid = 14.0 + 26.0 * hash01(seed, i, 10);
         double rot = Math.round((hash01(seed, i, 11) * Math.PI * 2.0) / (Math.PI * 0.5)) * Math.PI * 0.5 + nowSec * drift * 1.2;
         double halfTall = 4.0 + 9.0 * hash01(seed, i, 12);
         // tops bright, sides sun-shaded, bottoms deep shadow and translucent
         // (0.5 alpha) so the sky shows through from below
         cloudPrismShaded(consumer, pose, x, y, z, halfLen, halfWid, halfTall, rot, orI, ogI, obI, outerA, (int)(outerA * 0.92F), (int)(outerA * 0.5F), sun[0], sun[1], sun[2], sun[3]);
         cloudPrismShaded(consumer, pose, x, y + halfTall * 0.26, z, halfLen * 0.62, halfWid * 0.6, halfTall * 0.55, rot, irI, igI, ibI, (int)(outerA * 0.62F), (int)(outerA * 0.52F), (int)(outerA * 0.3F), sun[0], sun[1], sun[2], sun[3]);
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
