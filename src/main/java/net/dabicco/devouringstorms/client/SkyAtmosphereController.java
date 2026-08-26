package net.dabicco.devouringstorms.client;

import java.util.Collection;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * SkyAtmosphereController — the central brain of the Telltale-style sky.
 *
 * One place computes everything the layered atmosphere needs from the nearest
 * storm's live phase: which sky plates own the frame, how wide the storm's
 * angular cone is, the mutation tint, the churn speed, fog compression and
 * the palette used by the fog / world tint synchronization.
 *
 * Consumers:
 *  - StormSkyBox (the native SkyRenderer pass) for the layered backdrop,
 *  - StormMutationFlash for the sky-layer flash bloom,
 *  - the vanilla-sky compositors (canopy, cataclysm fx) so they stand down
 *    the moment the storm skybox owns the frame,
 *  - FogRendererMixin for horizon fog density.
 *
 * Phase story (user spec):
 *  - Phase 4 (Energy Focus): black/purple void + blue/cyan energy highlights
 *    (phase4_energy.png) + yellow horizon accents.
 *  - Phase 5.5-6.0: crossfade into the deep purple / void black / orange
 *    anomaly plate (sky_only_no_clouds.png = phase 5.9).
 *  - Phases 6-8 (Mutation): the cone widens to swallow the whole sky and the
 *    colour channels drift into mutated red / magenta / orange.
 */
public final class SkyAtmosphereController {
   /** Azimuth segments / elevation rings are owned by StormSkyBox. */
   private static final double MAX_RANGE = 2400.0;

   private static boolean valid;
   private static float phase;
   /** 0..1 overall claim on the sky (distance + phase window). */
   private static float intensity;
   /** Texture blend weights. */
   private static float energyWeight;
   private static float anomalyWeight;
   /** Storm cloud band weight (the wired-in cloud layers). */
   private static float cloudWeight;
   /** Cone half-angle, radians, around the camera->storm direction. */
   private static float coneRadians = 0.9F;
   /** World-space unit vector camera -> storm core (horizontal biased). */
   private static Vec3 stormDir = new Vec3(0.0, 0.35, 0.94);
   private static double stormX;
   private static double stormY;
   private static double stormZ;
   /** Fog compression for the active phases (1 = untouched). */
   private static float fogScale = 1.0F;
   /** Slow churn phase for skybox UV rotation. */
   private static float churn;

   private SkyAtmosphereController() {
   }

   public static boolean active() {
      return valid && intensity > 0.02F;
   }

   public static float phase() {
      return phase;
   }

   public static float intensity() {
      return intensity;
   }

   public static float energyWeight() {
      return energyWeight;
   }

   public static float anomalyWeight() {
      return anomalyWeight;
   }

   public static float cloudWeight() {
      return cloudWeight;
   }

   public static float coneRadians() {
      return coneRadians;
   }

   public static Vec3 stormDir() {
      return stormDir;
   }

   public static double stormX() {
      return stormX;
   }

   public static double stormY() {
      return stormY;
   }

   public static double stormZ() {
      return stormZ;
   }

   public static float churn() {
      return churn;
   }

   /** How much to compress world fog during the active storm sky (1 = none). */
   public static float fogScale() {
      return fogScale;
   }

   /** Recompute from the live storm data. Cheap; call once per consumer frame. */
   public static void update(Vec3 cam, float tickDelta, long gameTimeTicks) {
      Collection<ClientDistantStormManager.StormData> storms = ClientDistantStormManager.all();
      valid = false;
      intensity = 0.0F;
      energyWeight = 0.0F;
      anomalyWeight = 0.0F;
      cloudWeight = 0.0F;
      fogScale = 1.0F;
      if (storms.isEmpty()) {
         return;
      }

      ClientDistantStormManager.StormData best = null;
      double bestSq = Double.MAX_VALUE;
      for (ClientDistantStormManager.StormData d : storms) {
         double dsq = (d.x - cam.x) * (d.x - cam.x) + (d.z - cam.z) * (d.z - cam.z);
         if (dsq < bestSq) {
            bestSq = dsq;
            best = d;
         }
      }
      if (best == null || best.phase < 4.0F) {
         return;
      }

      valid = true;
      phase = best.phase;
      stormX = best.x;
      stormY = best.y;
      stormZ = best.z;

      // bearing to the storm, biased toward the horizon band so the cone reads
      // as a sky event, not a spot directly overhead
      Vec3 raw = new Vec3(stormX - cam.x, (stormY + 30.0 - cam.y) * 0.55, stormZ - cam.z);
      double len = raw.length();
      if (len < 1.0E-4) {
         stormDir = new Vec3(0.0, 0.35, 0.94);
      } else {
         stormDir = raw.normalize();
      }

      // distance keeps the sky polite: full claim near the storm, a hint far away
      double dist = Math.sqrt(bestSq);
      float proximity = Mth.clamp((float)(1.25 - dist / MAX_RANGE), 0.16F, 1.0F);

      // phase windows: energy focus from 4, crossfading to the anomaly 5.5-6.0
      float window = StormCloudDeck.smooth(phase, 4.0F, 4.4F);
      float toAnomaly = StormCloudDeck.smooth(phase, 5.5F, 6.0F);
      intensity = proximity * window;
      energyWeight = (1.0F - toAnomaly);
      anomalyWeight = toAnomaly;
      // the storm's own cloud bands ride along, melting partly under the
      // full anomaly plate (which is a sky-only look) but never vanishing
      cloudWeight = (0.85F - 0.45F * toAnomaly) * StormCloudDeck.smooth(phase, 4.0F, 4.5F);

      // the angular cone around the storm grows as the storm mutates, until it
      // swallows the sky: ~38 deg at phase 4 -> full takeover past 6.5
      float grow = StormCloudDeck.smooth(phase, 4.0F, 6.5F);
      coneRadians = Mth.lerp(grow, 0.66F, 2.1F);

      // horizon fog compression for the active sky: denser fog blends the
      // terrain edge into the backdrop and masks chunk boundaries
      fogScale = Mth.lerp(Mth.clamp(intensity, 0.0F, 1.0F), 1.0F, 0.78F);

      // slow churn so the backdrop clouds rotate lazily around the storm
      churn = (float)((double)(gameTimeTicks % 1000000L) + (double)tickDelta) * 0.0045F;
   }

   /** Mutation tint for phases 6-8: deep orange at 6 -> red at 7 -> magenta by 8. */
   public static void mutationTint(float[] out) {
      float t = Mth.clamp((phase - 6.0F) / 2.0F, 0.0F, 1.0F);
      out[0] = 1.0F;
      out[1] = Mth.lerp(Math.min(t * 2.0F, 1.0F), 0.44F, 0.17F);
      out[2] = t < 0.5F ? 0.18F : Mth.lerp((t - 0.5F) * 2.0F, 0.18F, 0.66F);
   }
}
