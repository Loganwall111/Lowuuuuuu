package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dabicco.devouringstorms.config.DevouringStormsClientConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * StormSkyCanopy — the horizon half of the Story-Mode sky.
 *
 * Design rule learned the hard way: this composes WITH the vanilla sky, it
 * never replaces it. The vanilla sky already gives us the azure zenith, the
 * sun, the moon and the twinkling starfield; what the reference shots add is
 * a distinctive band of colour hugging the horizon —
 *
 *  - Day: a warm pale-blue haze settled on the horizon, fading up into the
 *    vanilla blue within a hundred blocks of height.
 *  - Sunset/Dusk: a glowing orange/pink horizon line (the Season 2 look).
 *  - Night: a faint cyan/teal glow low on the horizon under the stars.
 *
 * When a storm claims the sky the haze drifts toward the storm palette, and a
 * soft glow ARC appears behind the storm's bearing only — silhouetting the
 * body against lit sky the way the shots read. It is deliberately an arc,
 * not a ring: a full 360 degree band reads as a wall, and walls read as
 * billboards.
 */
public final class StormSkyCanopy {
   private static final double BAND_RADIUS = 480.0;
   private static final double BAND_BOTTOM = -46.0;
   private static final double BAND_TOP = 108.0;
   private static final int BAND_SEGMENTS = 28;
   private static final int FULL_BRIGHT = 15728880;
   private static final Identifier WHITE_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/white.png");
   private static final Identifier BAND_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/halo_band.png");
   /** Half-width, in radians, of the storm back-glow arc. */
   private static final double ARC_HALF_WIDTH = 0.62;

   /** horizon colour anchors per time of day: [day, sunset, night]. */
   private static final float[] DAY_HORIZON = {0.74F, 0.86F, 0.98F};
   private static final float[] SUNSET_HORIZON = {1.0F, 0.55F, 0.26F};
   private static final float[] NIGHT_HORIZON = {0.14F, 0.40F, 0.46F};

   private StormSkyCanopy() {
   }

   /** Time-of-day weights: [day, sunset, night] for an overworld clock value. */
   static void dayWeights(double clock, float[] out) {
      double t = (clock % 24000L + 24000L) % 24000L;
      float day = StormCloudDeck.smooth((float)t, -500.0F, 1300.0F) * (1.0F - StormCloudDeck.smooth((float)t, 10800.0F, 13000.0F));
      float night = StormCloudDeck.smooth((float)t, 12200.0F, 14200.0F) * (1.0F - StormCloudDeck.smooth((float)t, 21400.0F, 23300.0F));
      float sunset = Mth.clamp(1.0F - day - night, 0.0F, 1.0F);
      out[0] = day;
      out[1] = sunset;
      out[2] = night;
   }

   private static void horizonColor(float[] weights, float[] out) {
      for (int i = 0; i < 3; i++) {
         out[i] = DAY_HORIZON[i] * weights[0] + SUNSET_HORIZON[i] * weights[1] + NIGHT_HORIZON[i] * weights[2];
      }
   }

   private static void bandVertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)x, (float)y, (float)z).setColor(r, g, b, a).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }

   /**
    * The horizon haze: a short open cylinder around the camera whose alpha is
    * strongest at the horizon and fades to nothing well above it. The vanilla
    * sky (and stars, sun, moon) shows through everywhere above the band.
    */
   private static void renderHorizonBand(VertexConsumer consumer, PoseStack.Pose pose, Vec3 cam, float[] horizon, float strength) {
      if (strength <= 0.01F) {
         return;
      }
      int r = (int)(Mth.clamp(horizon[0], 0.0F, 1.0F) * 255.0F);
      int g = (int)(Mth.clamp(horizon[1], 0.0F, 1.0F) * 255.0F);
      int b = (int)(Mth.clamp(horizon[2], 0.0F, 1.0F) * 255.0F);
      double y0 = cam.y + BAND_BOTTOM;
      double y1 = cam.y + BAND_TOP;
      // two stacked bands: bright at the horizon, fading out above
      double yMid = cam.y + 18.0;

      for (int i = 0; i < BAND_SEGMENTS; i++) {
         double a0 = Math.PI * 2.0 * (double)i / (double)BAND_SEGMENTS;
         double a1 = Math.PI * 2.0 * (double)(i + 1) / (double)BAND_SEGMENTS;
         double x0 = cam.x + Math.cos(a0) * BAND_RADIUS;
         double z0 = cam.z + Math.sin(a0) * BAND_RADIUS;
         double x1 = cam.x + Math.cos(a1) * BAND_RADIUS;
         double z1 = cam.z + Math.sin(a1) * BAND_RADIUS;

         int aLo = (int)(150.0F * strength);
         int aMid = (int)(96.0F * strength);
         int aHi = 0;
         // lower half: horizon colour at full band alpha
         bandVertex(consumer, pose, x0, y0, z0, r, g, b, aMid);
         bandVertex(consumer, pose, x1, y0, z1, r, g, b, aMid);
         bandVertex(consumer, pose, x1, yMid, z1, r, g, b, aLo);
         bandVertex(consumer, pose, x0, yMid, z0, r, g, b, aLo);
         // upper half: fade to transparent so the vanilla sky takes over
         bandVertex(consumer, pose, x0, yMid, z0, r, g, b, aLo);
         bandVertex(consumer, pose, x1, yMid, z1, r, g, b, aLo);
         bandVertex(consumer, pose, x1, y1, z1, r, g, b, aHi);
         bandVertex(consumer, pose, x0, y1, z0, r, g, b, aHi);
      }
   }

   /**
    * The storm back-glow: a soft additive arc hugging the horizon BEHIND the
    * nearest storm's bearing, so the dark body is silhouetted against lit
    * sky. Turquoise at 5.0-5.1, purple/pink through 5.1-5.9, orange/red after
    * the phase-6 split.
    */
   private static void renderStormArc(VertexConsumer consumer, PoseStack.Pose pose, Vec3 cam, double stormX, double stormZ, float phase, float blend) {
      if (blend <= 0.02F || phase < 4.4F) {
         return;
      }
      float turquoise = StormCloudDeck.smooth(phase, 4.95F, 5.05F) * (1.0F - StormCloudDeck.smooth(phase, 5.1F, 5.18F));
      float purple = StormCloudDeck.smooth(phase, 5.12F, 5.3F) * (1.0F - StormCloudDeck.smooth(phase, 5.88F, 5.98F));
      float cataclysm = StormCloudDeck.smooth(phase, 5.9F, 6.15F);
      int r = (int)(Mth.clamp(turquoise * 0.2F + purple * 0.62F + cataclysm, 0.0F, 1.0F) * 255.0F);
      int g = (int)(Mth.clamp(turquoise * 0.9F + purple * 0.26F + cataclysm * 0.34F, 0.0F, 1.0F) * 255.0F);
      int b = (int)(Mth.clamp(turquoise * 0.8F + purple * 0.9F + cataclysm * 0.2F, 0.0F, 1.0F) * 255.0F);
      int alpha = (int)(Mth.clamp((turquoise + purple * 0.85F + cataclysm * 0.9F) * blend, 0.0F, 1.0F) * 104.0F);
      if (alpha <= 2) {
         return;
      }

      double bearing = Math.atan2(stormZ - cam.z, stormX - cam.x);
      double radius = 430.0;
      double horizonY = cam.y + 30.0;
      double halfH = 38.0;
      int segments = 9;
      for (int i = 0; i < segments; i++) {
         double t0 = (double)i / (double)segments;
         double t1 = (double)(i + 1) / (double)segments;
         // smooth raised-cosine falloff across the arc so the ends melt away
         double c0 = 0.5 - 0.5 * Math.cos(t0 * Math.PI * 2.0);
         double c1 = 0.5 - 0.5 * Math.cos(t1 * Math.PI * 2.0);
         double a0 = bearing - ARC_HALF_WIDTH + c0 * ARC_HALF_WIDTH * 2.0;
         double a1 = bearing - ARC_HALF_WIDTH + c1 * ARC_HALF_WIDTH * 2.0;
         int arcA0 = (int)((float)alpha * (0.25F + 0.75F * (float)Math.sin(t0 * Math.PI)));
         int arcA1 = (int)((float)alpha * (0.25F + 0.75F * (float)Math.sin(t1 * Math.PI)));
         double x0 = cam.x + Math.cos(a0) * radius;
         double z0 = cam.z + Math.sin(a0) * radius;
         double x1 = cam.x + Math.cos(a1) * radius;
         double z1 = cam.z + Math.sin(a1) * radius;
         double midX = (x0 + x1) * 0.5;
         double midZ = (z0 + z1) * 0.5;
         consumer.addVertex(pose, (float)x0, (float)(horizonY - halfH), (float)z0).setColor(r, g, b, 0).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)midX, (float)(horizonY - halfH), (float)midZ).setColor(r, g, b, 0).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)midX, (float)horizonY, (float)midZ).setColor(r, g, b, arcA1).setUv(1.0F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)x0, (float)horizonY, (float)z0).setColor(r, g, b, arcA0).setUv(0.0F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)x0, (float)horizonY, (float)z0).setColor(r, g, b, arcA0).setUv(0.0F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)midX, (float)horizonY, (float)midZ).setColor(r, g, b, arcA1).setUv(1.0F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)midX, (float)(horizonY + halfH), (float)midZ).setColor(r, g, b, 0).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)x0, (float)(horizonY + halfH), (float)z0).setColor(r, g, b, 0).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
      }
   }

   public static void submit(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return;
      }
      Vec3 cam = ctx.levelState().cameraRenderState.pos;
      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();
      double clock = (double)mc.level.getOverworldClockTime() + (double)mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

      float[] weights = new float[3];
      dayWeights(clock, weights);
      float[] horizon = new float[3];
      horizonColor(weights, horizon);

      // storm takeover: drift the haze toward the storm palette
      float paletteBlend = Mth.clamp(StormSkyDarken.paletteBlend() + StormSkyDarken.globalBlend(), 0.0F, 1.0F);
      float strength = Mth.clamp((float)DevouringStormsClientConfig.paletteStrength, 0.0F, 1.0F);
      float claim = paletteBlend * strength;
      if (claim > 0.01F) {
         float phase = Math.max(StormSkyDarken.palettePhase(), StormSkyDarken.globalPhase());
         float[] storm = StormPalettes.skyColor(phase, new float[3]);
         for (int i = 0; i < 3; i++) {
            horizon[i] = Mth.lerp(claim * 0.7F, horizon[i], storm[i]);
         }
      }

      // the haze is quieter at night so the (twinkling) starfield leads
      float bandStrength = Mth.lerp(weights[2], 0.85F, 0.5F);
      collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(WHITE_TEXTURE), (pose, consumer) -> {
         renderHorizonBand(consumer, pose, cam, horizon, bandStrength);
      });
   }
}
