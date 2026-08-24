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
 * StormSkyCanopy — the MCSM skybox backdrop.
 *
 * Rebuilds the top of the sky as a smooth multi-stop gradient from horizon to
 * zenith, drawn as a huge world-space dome around the camera:
 *
 *  - Day: warm pale blue at the horizon melting into a vibrant azure zenith.
 *  - Sunset/Dusk: a glowing orange/pink horizon line fading through deep teal
 *    into indigo overhead (the Story Mode Season 2 palette).
 *  - Night: a bright cyan/teal horizon glow sinking into deep cosmic dark
 *    blue at the zenith - the twinkling starfield layers on top of this.
 *
 * When a storm claims the sky, the dome's stops drift toward the storm
 * palette, and a soft horizon back-glow ring wraps the storm's bearing so the
 * dark body is silhouetted against glowing sky (turquoise at 5.0, purple and
 * pink-purple through 5.5-5.9, orange/red after the phase-6 split).
 */
public final class StormSkyCanopy {
   private static final double DOME_RADIUS = 640.0;
   private static final double DOME_BOTTOM = -64.0;
   private static final double DOME_TOP = 560.0;
   private static final int DOME_SEGMENTS = 24;
   private static final int FULL_BRIGHT = 15728880;
   private static final Identifier WHITE_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/entity/white.png");
   private static final Identifier BAND_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/misc/halo_band.png");

   /** horizon / mid / zenith anchor stops per time of day. */
   private static final float[] DAY_STOPS = {0.72F, 0.85F, 0.98F, 0.46F, 0.68F, 0.98F, 0.20F, 0.46F, 0.94F};
   private static final float[] SUNSET_STOPS = {1.0F, 0.56F, 0.27F, 0.86F, 0.40F, 0.46F, 0.13F, 0.20F, 0.42F};
   private static final float[] NIGHT_STOPS = {0.16F, 0.44F, 0.50F, 0.055F, 0.10F, 0.21F, 0.014F, 0.022F, 0.062F};

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

   /** Blend the three time-of-day gradient stop sets into one set of stops. */
   static void gradientStops(float[] weights, float[] stops) {
      for (int i = 0; i < 9; i++) {
         stops[i] = DAY_STOPS[i] * weights[0] + SUNSET_STOPS[i] * weights[1] + NIGHT_STOPS[i] * weights[2];
      }
   }

   /** Interpolate the gradient at height fraction f in [0,1] (0 = horizon). */
   private static float[] stopAt(float f, float[] stops, float[] out) {
      float t = Mth.clamp(f, 0.0F, 1.0F);
      float r;
      float g;
      float b;
      if (t < 0.42F) {
         float k = t / 0.42F;
         r = Mth.lerp(k, stops[0], stops[3]);
         g = Mth.lerp(k, stops[1], stops[4]);
         b = Mth.lerp(k, stops[2], stops[5]);
      } else {
         float k = (t - 0.42F) / 0.58F;
         r = Mth.lerp(k, stops[3], stops[6]);
         g = Mth.lerp(k, stops[4], stops[7]);
         b = Mth.lerp(k, stops[5], stops[8]);
      }

      out[0] = r;
      out[1] = g;
      out[2] = b;
      return out;
   }

   private static void domeVertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z, int r, int g, int b, int a) {
      consumer.addVertex(pose, (float)x, (float)y, (float)z).setColor(r, g, b, a).setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, -1.0F, 0.0F);
   }

   /** The gradient dome itself: an open world-space cylinder plus zenith cap around the camera. */
   private static void renderGradientDome(VertexConsumer consumer, PoseStack.Pose pose, Vec3 cam, float[] stops, float strength) {
      if (strength <= 0.01F) {
         return;
      }
      float[] col = new float[3];
      double y0 = cam.y + DOME_BOTTOM;
      double y1 = cam.y + DOME_TOP;

      for (int i = 0; i < DOME_SEGMENTS; i++) {
         double a0 = Math.PI * 2.0 * (double)i / (double)DOME_SEGMENTS;
         double a1 = Math.PI * 2.0 * (double)(i + 1) / (double)DOME_SEGMENTS;
         double x0 = cam.x + Math.cos(a0) * DOME_RADIUS;
         double z0 = cam.z + Math.sin(a0) * DOME_RADIUS;
         double x1 = cam.x + Math.cos(a1) * DOME_RADIUS;
         double z1 = cam.z + Math.sin(a1) * DOME_RADIUS;

         float[] lo = stopAt(0.0F, stops, col);
         int lr = (int)(Mth.clamp(lo[0], 0.0F, 1.0F) * 255.0F);
         int lg = (int)(Mth.clamp(lo[1], 0.0F, 1.0F) * 255.0F);
         int lb = (int)(Mth.clamp(lo[2], 0.0F, 1.0F) * 255.0F);
         float[] hi = stopAt(1.0F, stops, col);
         int hr = (int)(Mth.clamp(hi[0], 0.0F, 1.0F) * 255.0F);
         int hg = (int)(Mth.clamp(hi[1], 0.0F, 1.0F) * 255.0F);
         int hb = (int)(Mth.clamp(hi[2], 0.0F, 1.0F) * 255.0F);
         int aLo = (int)(225.0F * strength);
         int aHi = (int)(245.0F * strength);

         // split each wall into 3 bands so the multi-stop gradient is smooth
         for (int band = 0; band < 3; band++) {
            float fA = (float)band / 3.0F;
            float fB = (float)(band + 1) / 3.0F;
            float[] cA = stopAt(fA, stops, col);
            int ar = (int)(Mth.clamp(cA[0], 0.0F, 1.0F) * 255.0F);
            int ag = (int)(Mth.clamp(cA[1], 0.0F, 1.0F) * 255.0F);
            int ab = (int)(Mth.clamp(cA[2], 0.0F, 1.0F) * 255.0F);
            float[] cB = stopAt(fB, stops, col);
            int br = (int)(Mth.clamp(cB[0], 0.0F, 1.0F) * 255.0F);
            int bg = (int)(Mth.clamp(cB[1], 0.0F, 1.0F) * 255.0F);
            int bb = (int)(Mth.clamp(cB[2], 0.0F, 1.0F) * 255.0F);
            int aA = (int)(Mth.lerp(fA, (float)aLo, (float)aHi) * 1.0F);
            int aB = (int)(Mth.lerp(fB, (float)aLo, (float)aHi) * 1.0F);
            double yA = Mth.lerp((double)fA, y0, y1);
            double yB = Mth.lerp((double)fB, y0, y1);
            domeVertex(consumer, pose, x0, yA, z0, ar, ag, ab, aA);
            domeVertex(consumer, pose, x1, yA, z1, ar, ag, ab, aA);
            domeVertex(consumer, pose, x1, yB, z1, br, bg, bb, aB);
            domeVertex(consumer, pose, x0, yB, z0, br, bg, bb, aB);
         }

         // zenith cap fan (flat color at the top stop)
         double cx = (x0 + x1) * 0.5;
         double cz = (z0 + z1) * 0.5;
         double apexY = cam.y + DOME_TOP + 160.0;
         domeVertex(consumer, pose, x0, y1, z0, hr, hg, hb, aHi);
         domeVertex(consumer, pose, x1, y1, z1, hr, hg, hb, aHi);
         domeVertex(consumer, pose, cx, apexY, cz, hr, hg, hb, aHi);
         domeVertex(consumer, pose, x0, y1, z0, hr, hg, hb, aHi);
      }
   }

   /**
    * Storm horizon back-glow: a soft world-space gradient band hugging the
    * horizon around the storm's bearing, so the dark storm body is silhouetted
    * against a glowing sky (this is the "halo on the horizon" reading of the
    * reference shots, not an entity billboard).
    */
   private static void renderStormHorizonGlow(VertexConsumer consumer, PoseStack.Pose pose, Vec3 cam, float phase, float blend) {
      if (blend <= 0.02F || phase < 4.4F) {
         return;
      }
      float turquoise = StormCloudDeck.smooth(phase, 4.95F, 5.05F) * (1.0F - StormCloudDeck.smooth(phase, 5.1F, 5.18F));
      float purple = StormCloudDeck.smooth(phase, 5.12F, 5.3F) * (1.0F - StormCloudDeck.smooth(phase, 5.88F, 5.98F));
      float cataclysm = StormCloudDeck.smooth(phase, 5.9F, 6.15F);
      int r = (int)(Mth.clamp(turquoise * 0.2F + purple * 0.62F + cataclysm * 1.0F, 0.0F, 1.0F) * 255.0F);
      int g = (int)(Mth.clamp(turquoise * 0.9F + purple * 0.26F + cataclysm * 0.34F, 0.0F, 1.0F) * 255.0F);
      int b = (int)(Mth.clamp(turquoise * 0.8F + purple * 0.9F + cataclysm * 0.2F, 0.0F, 1.0F) * 255.0F);
      int alpha = (int)(Mth.clamp((turquoise + purple * 0.8F + cataclysm * 0.9F) * blend, 0.0F, 1.0F) * 96.0F);
      if (alpha <= 2) {
         return;
      }

      double horizonY = cam.y + 34.0;
      double radius = 470.0;
      double halfH = 44.0;
      int segments = 14;
      for (int i = 0; i < segments; i++) {
         double a0 = Math.PI * 2.0 * (double)i / (double)segments;
         double a1 = Math.PI * 2.0 * (double)(i + 1) / (double)segments;
         double x0 = cam.x + Math.cos(a0) * radius;
         double z0 = cam.z + Math.sin(a0) * radius;
         double x1 = cam.x + Math.cos(a1) * radius;
         double z1 = cam.z + Math.sin(a1) * radius;
         double midX = (x0 + x1) * 0.5;
         double midZ = (z0 + z1) * 0.5;
         consumer.addVertex(pose, (float)x0, (float)(horizonY - halfH), (float)z0).setColor(r, g, b, 0).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)midX, (float)(horizonY - halfH), (float)midZ).setColor(r, g, b, 0).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)midX, (float)horizonY, (float)midZ).setColor(r, g, b, alpha).setUv(1.0F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)x0, (float)horizonY, (float)z0).setColor(r, g, b, alpha).setUv(0.0F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)x0, (float)horizonY, (float)z0).setColor(r, g, b, alpha).setUv(0.0F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
         consumer.addVertex(pose, (float)midX, (float)horizonY, (float)midZ).setColor(r, g, b, alpha).setUv(1.0F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
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
      float[] stops = new float[9];
      gradientStops(weights, stops);

      // storm takeover: drift the whole gradient toward the storm palette
      float paletteBlend = Mth.clamp(StormSkyDarken.paletteBlend() + StormSkyDarken.globalBlend(), 0.0F, 1.0F);
      float strength = Mth.clamp((float)DevouringStormsClientConfig.paletteStrength, 0.0F, 1.0F);
      float claim = paletteBlend * strength;
      if (claim > 0.01F) {
         float phase = StormSkyDarken.globalBlend() > paletteBlend * 0.5F ? StormSkyDarken.globalPhase() : StormSkyDarken.palettePhase();
         float[] storm = StormPalettes.skyColor(Math.max(phase, StormSkyDarken.palettePhase()), new float[3]);
         float darken = 1.0F - StormSkyDarken.factor() * 0.45F;
         for (int i = 0; i < 9; i++) {
            int band = i / 3;
            // horizon bends hardest toward the storm palette; zenith keeps
            // more of its own depth so the dome never flattens
            float k = claim * (0.72F - 0.22F * (float)band);
            stops[i] = Mth.lerp(k, stops[i], storm[i % 3]) * (band == 0 ? 1.0F : darken);
         }
      }

      float domeStrength = 0.85F + 0.15F * weights[2];
      collector.submitCustomGeometry(poseStack, GlowRenderTypes.translucent(WHITE_TEXTURE), (pose, consumer) -> {
         renderGradientDome(consumer, pose, cam, stops, domeStrength);
      });
      if (claim > 0.01F) {
         float glowPhase = Math.max(StormSkyDarken.palettePhase(), StormSkyDarken.globalBlend() > 0.5F ? StormSkyDarken.globalPhase() : 0.0F);
         collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(BAND_TEXTURE), (pose, consumer) -> {
            renderStormHorizonGlow(consumer, pose, cam, glowPhase, claim);
         });
      }
   }
}
