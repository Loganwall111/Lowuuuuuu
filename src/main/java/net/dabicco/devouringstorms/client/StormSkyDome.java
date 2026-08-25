package net.dabicco.devouringstorms.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collection;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * StormSkyDome — the official-texture, entity-tethered phase skybox.
 *
 * A dome centred on the storm's core coordinates (not the camera) that
 * carries the uploaded Telltale-style sky textures and crossfades between
 * them as the storm progresses:
 *
 *   Phase 4+      : the blue/cyan energy focus (phase4_energy.png) glowing
 *                   around the storm's core.
 *   Phase 5.5-6.0 : crossfade into the deep purple / black / orange anomaly
 *                   (phase59_anomaly.png).
 *   Phases 6-8    : the anomaly, driven toward vibrant red / orange / magenta
 *                   as the storm mutates past the split.
 *
 * The dome samples its textures VERTICALLY by view-ray elevation (dense rings
 * near the horizon, sparse near the zenith) and horizontally by azimuth so a
 * panorama's variation is used rather than one column. Intensity fades with
 * the player's distance to the storm, and the whole thing is ADDITIVE with
 * no depth write, so terrain in front of it is never clipped and the vanilla
 * sky behind it still breathes through at the edges.
 */
public final class StormSkyDome {
   private static final Identifier ENERGY_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/sky/phase4_energy.png");
   private static final Identifier ANOMALY_TEXTURE = Identifier.fromNamespaceAndPath("devouringstorms", "textures/sky/phase59_anomaly.png");
   private static final int FULL_BRIGHT = 15728880;
   /** Dome radius around the storm core, in blocks. */
   private static final double RADIUS = 520.0;
   /** Ring elevations, in degrees above the storm's horizon plane. */
   private static final float[] ELEVATIONS = new float[]{0.0F, 10.0F, 22.0F, 38.0F, 58.0F};
   /** Per-ring alpha weights: strongest at the horizon, melting away up high. */
   private static final float[] RING_WEIGHTS = new float[]{1.0F, 0.82F, 0.6F, 0.36F, 0.18F};
   private static final int SEGMENTS = 24;

   private StormSkyDome() {
   }

   /**
    * How much the anomaly sky owns the frame (0-1). Past ~5.5 the storm deck
    * prisms fade out under this veil so the anomaly sky reads clean, the way
    * the uploaded "sky only, no clouds" plate is meant to.
    */
   public static float domeVeil(float phase) {
      return StormCloudDeck.smooth(phase, 5.4F, 5.7F);
   }

   /** Phase 6/7/8 mutation tint: deep orange at 6, red at 7, magenta by 8. */
   private static void mutationTint(float phase, float[] out) {
      float t = Mth.clamp((phase - 6.0F) / 2.0F, 0.0F, 1.0F);
      // orange (1.0, 0.42, 0.12) -> red (1.0, 0.16, 0.12) -> magenta (1.0, 0.16, 0.62)
      float r = 1.0F;
      float g = Mth.lerp(Math.min(t * 2.0F, 1.0F), 0.42F, 0.16F);
      float b = t < 0.5F ? Mth.lerp(t * 2.0F, 0.12F, 0.12F) : Mth.lerp((t - 0.5F) * 2.0F, 0.12F, 0.62F);
      out[0] = r;
      out[1] = g;
      out[2] = b;
   }

   private static void domeVertex(VertexConsumer consumer, PoseStack.Pose pose, Vec3 at, double elevRad, double azimuth, int r, int g, int b, int a) {
      // vertical texture coordinate from view-ray elevation; horizontal from
      // azimuth across the middle band of the plate so a panorama's variation
      // is used without ever sampling past its edges
      float v = Mth.clamp((float)(0.55 - elevRad * 0.42), 0.05F, 0.55F);
      float u = Mth.clamp(0.5F + 0.35F * (float)Math.sin(azimuth), 0.1F, 0.9F);
      consumer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z).setColor(r, g, b, a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
   }

   private static void renderDome(VertexConsumer consumer, PoseStack.Pose pose, Vec3 core, float alpha, float[] tint) {
      int r = (int)(Mth.clamp(tint[0], 0.0F, 1.0F) * 255.0F);
      int g = (int)(Mth.clamp(tint[1], 0.0F, 1.0F) * 255.0F);
      int b = (int)(Mth.clamp(tint[2], 0.0F, 1.0F) * 255.0F);

      for (int i = 0; i < ELEVATIONS.length - 1; i++) {
         double lo = Math.toRadians((double)ELEVATIONS[i]);
         double hi = Math.toRadians((double)ELEVATIONS[i + 1]);
         double yLo = Math.sin(lo) * RADIUS;
         double rLo = Math.cos(lo) * RADIUS;
         double yHi = Math.sin(hi) * RADIUS;
         double rHi = Math.cos(hi) * RADIUS;
         int aLo = (int)(alpha * RING_WEIGHTS[i] * 235.0F);
         int aHi = (int)(alpha * RING_WEIGHTS[i + 1] * 235.0F);

         for (int s = 0; s < SEGMENTS; s++) {
            double az0 = Math.PI * 2.0 * (double)s / (double)SEGMENTS;
            double az1 = Math.PI * 2.0 * (double)(s + 1) / (double)SEGMENTS;
            Vec3 loA = new Vec3(core.x + Math.cos(az0) * rLo, core.y + yLo, core.z + Math.sin(az0) * rLo);
            Vec3 loB = new Vec3(core.x + Math.cos(az1) * rLo, core.y + yLo, core.z + Math.sin(az1) * rLo);
            Vec3 hiB = new Vec3(core.x + Math.cos(az1) * rHi, core.y + yHi, core.z + Math.sin(az1) * rHi);
            Vec3 hiA = new Vec3(core.x + Math.cos(az0) * rHi, core.y + yHi, core.z + Math.sin(az0) * rHi);
            domeVertex(consumer, pose, loA, lo, az0, r, g, b, aLo);
            domeVertex(consumer, pose, loB, lo, az1, r, g, b, aLo);
            domeVertex(consumer, pose, hiB, hi, az1, r, g, b, aHi);
            domeVertex(consumer, pose, hiA, hi, az0, r, g, b, aHi);
         }
      }
   }

   /** Nearest storm data, or null when no storm is tracked anywhere. */
   private static ClientDistantStormManager.StormData nearestStorm(Vec3 cam, Collection<ClientDistantStormManager.StormData> storms) {
      ClientDistantStormManager.StormData best = null;
      double bestSq = Double.MAX_VALUE;
      for (ClientDistantStormManager.StormData d : storms) {
         double dsq = (d.x - cam.x) * (d.x - cam.x) + (d.z - cam.z) * (d.z - cam.z);
         if (dsq < bestSq) {
            bestSq = dsq;
            best = d;
         }
      }

      return best;
   }

   public static void submit(LevelRenderContext ctx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return;
      }
      Vec3 cam = ctx.levelState().cameraRenderState.pos;
      Collection<ClientDistantStormManager.StormData> storms = ClientDistantStormManager.all();
      if (storms.isEmpty()) {
         return;
      }
      ClientDistantStormManager.StormData storm = nearestStorm(cam, storms);
      if (storm == null || storm.phase < 4.0F) {
         return;
      }
      float phase = storm.phase;

      // distance-based proximity intensity: the sky is owned outright near
      // the storm and still hinted at from far away
      double dist = Math.sqrt((storm.x - cam.x) * (storm.x - cam.x) + (storm.z - cam.z) * (storm.z - cam.z));
      float proximity = Mth.clamp((float)(1.25 - dist / 1200.0), 0.28F, 1.0F);
      float window = StormCloudDeck.smooth(phase, 4.0F, 4.42F);
      float intensity = proximity * window;
      if (intensity <= 0.02F) {
         return;
      }

      // crossfade windows: energy until ~5.5, anomaly from ~6.0
      float toAnomaly = StormCloudDeck.smooth(phase, 5.5F, 6.0F);
      float energyShare = (1.0F - toAnomaly) * intensity;
      float anomalyShare = toAnomaly * intensity;

      Vec3 core = new Vec3(storm.x, storm.y + 40.0, storm.z);
      PoseStack poseStack = ctx.poseStack();
      SubmitNodeCollector collector = ctx.submitNodeCollector();

      if (energyShare > 0.02F) {
         // Phase 4-5.5: blue/cyan energy focus, tinted by the cyan-blue end
         float[] cyanTint = new float[]{0.55F, 0.85F, 1.0F};
         float a = energyShare;
         collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(ENERGY_TEXTURE), (pose, consumer) -> {
            renderDome(consumer, pose, core, a, cyanTint);
         });
      }

      if (anomalyShare > 0.02F) {
         float[] tint = new float[3];
         mutationTint(phase, tint);
         // early anomaly keeps the purple/black/orange plate honest; the
         // mutation tint ramps it toward red/orange/magenta across 6->8
         float mutation = Mth.clamp((phase - 6.0F) / 2.0F, 0.0F, 1.0F);
         float[] anomalyTint = new float[]{Mth.lerp(mutation, 0.85F, tint[0]), Mth.lerp(mutation, 0.7F, tint[1]), Mth.lerp(mutation, 0.9F, tint[2])};
         float a = anomalyShare;
         collector.submitCustomGeometry(poseStack, GlowRenderTypes.glow(ANOMALY_TEXTURE), (pose, consumer) -> {
            renderDome(consumer, pose, core, a, anomalyTint);
         });
      }
   }
}
