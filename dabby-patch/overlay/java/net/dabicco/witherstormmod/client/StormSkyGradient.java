package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The storm's directional sky gradient.
 *
 * The user's own description of what this should be, which turned out to be
 * exactly right: a second sky that follows the Wither Storm and BLENDS INTO the
 * vanilla sky, producing a huge black-and-purple gradient behind the creature
 * that moves as it moves. Not a skybox that rotates around you, and not an
 * object -- so there is nothing to clip through.
 *
 * Vanilla already does precisely this for sunrise and sunset: a radial gradient
 * fan, painted on the inside of the sky dome, rotated to face the sun's compass
 * bearing and fading to transparent at its edges. That is why sunsets glow on
 * one side of the sky and blend seamlessly into blue on the other.
 *
 * So this computes the same two numbers vanilla's sunrise pass needs -- a
 * compass bearing and an ARGB colour -- but aimed at the storm rather than the
 * sun. {@link net.dabicco.witherstormmod.mixin.StormSkyGradientMixin} then asks
 * SkyRenderer to run its own sunrise geometry a second time with those values.
 *
 * Because it reuses the vanilla pass:
 *   - it is drawn as sky, at sky depth, so terrain and clouds sit in front
 *   - it fades to transparent at the edges, so it blends rather than cuts
 *   - it cannot be walked behind or clipped through, having no geometry in
 *     the world at all
 */
public final class StormSkyGradient {

   /* Palette, sampled from the user's reference frames. */
   /* Resampled from the user's three Story Mode reference frames.
    *
    * Measured sky medians were (61,30,87), (53,49,90) and (86,42,121) -- every
    * one purple-dominant, with 0.00% green-dominant pixels in all three. My
    * previous palette held turquoise from 4.5 all the way to 6.0, which made
    * the sky read green for the entire mid game. That was wrong: the green
    * belongs to the FOG at 4.5, not to the sky behind the storm. */
   private static final float[] DUSK = { 0.239F, 0.192F, 0.341F };  // early haze, violet-grey
   private static final float[] PURP = { 0.290F, 0.145F, 0.420F };  // #4A256 core purple
   private static final float[] DEEP = { 0.212F, 0.118F, 0.353F };  // darker purple mass
   private static final float[] MAGE = { 0.520F, 0.140F, 0.470F };  // magenta
   private static final float[] PINK = { 0.690F, 0.200F, 0.620F };  // violet-pink
   private static final float[] RED  = { 0.520F, 0.110F, 0.190F };  // embedded red

   /** Beyond this the storm no longer paints the sky. */
   private static final double RANGE = 1400.0;

   private static float strength;      // smoothed 0..1
   private static float yawDeg;        // compass bearing of the storm
   private static float phase;
   private static boolean active;

   private StormSkyGradient() {
   }

   /** Recomputed once per frame, before the sky is drawn. */
   public static void update(Vec3 cameraPos) {
      float best = 0.0F;
      float bestYaw = 0.0F;
      float bestPhase = 0.0F;

      for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         if (d.phase < 4.5F) {
            continue;                      // nothing before 4.5
         }
         double dx = d.dispX - cameraPos.x;
         double dz = d.dispZ - cameraPos.z;
         double dist = Math.sqrt(dx * dx + dz * dz);
         if (dist > RANGE) {
            continue;
         }
         float proximity = dist <= RANGE * 0.5
            ? 1.0F
            : smooth((float)(1.0 - (dist - RANGE * 0.5) / (RANGE * 0.5)));
         float onset = ramp(d.phase, 4.45F, 4.90F);
         float w = proximity * onset;
         if (w > best) {
            best = w;
            // atan2(dz, dx) gives the bearing; vanilla's fan is built around
            // the +X axis, so no extra offset is needed beyond degrees.
            bestYaw = (float)(Math.atan2(dz, dx) * (180.0 / Math.PI));
            bestPhase = d.phase;
         }
      }

      strength += (best - strength) * 0.05F;
      if (strength < 0.003F) {
         strength = 0.0F;
      }
      if (bestPhase > 0.0F) {
         phase = bestPhase;
         yawDeg = bestYaw;
      }
      active = strength > 0.0F;
   }

   public static boolean active() {
      return active && DabyWSClientConfig.stormBackdrop;
   }

   /** Compass bearing of the storm, in degrees. */
   public static float yaw() {
      return yawDeg;
   }

   /**
    * Packed ARGB for the gradient.
    *
    * Alpha carries the overall strength, which is what makes it blend into the
    * vanilla sky instead of replacing it.
    */
   public static int color() {
      float p = phase;

      /* Purple from the moment the sky reacts, deepening into magenta and then
       * violet-pink as the storm grows. No green at any point. */
      float wDusk = ramp(p, 4.45F, 4.90F) * (1.0F - ramp(p, 4.90F, 5.40F));
      float wPurp = ramp(p, 4.90F, 5.40F) * (1.0F - ramp(p, 5.80F, 6.30F));
      float wDeep = ramp(p, 5.80F, 6.30F) * (1.0F - ramp(p, 6.30F, 6.90F));
      float wMage = ramp(p, 6.30F, 6.90F) * (1.0F - ramp(p, 6.80F, 7.40F));
      float wPink = ramp(p, 6.80F, 7.40F);
      float wRed  = ramp(p, 6.20F, 7.60F) * 0.28F;

      float total = wDusk + wPurp + wDeep + wMage + wPink + wRed;
      float r;
      float g;
      float b;
      if (total <= 0.0001F) {
         r = DUSK[0];
         g = DUSK[1];
         b = DUSK[2];
      } else {
         r = (DUSK[0] * wDusk + PURP[0] * wPurp + DEEP[0] * wDeep + MAGE[0] * wMage + PINK[0] * wPink + RED[0] * wRed) / total;
         g = (DUSK[1] * wDusk + PURP[1] * wPurp + DEEP[1] * wDeep + MAGE[1] * wMage + PINK[1] * wPink + RED[1] * wRed) / total;
         b = (DUSK[2] * wDusk + PURP[2] * wPurp + DEEP[2] * wDeep + MAGE[2] * wMage + PINK[2] * wPink + RED[2] * wRed) / total;
      }

      // Darken toward the centre of the fan as the storm matures: this is the
      // black core the user described sitting inside the purple.
      float dark = 1.0F - 0.22F * ramp(p, 4.45F, 5.60F);
      r *= dark;
      g *= dark;
      b *= dark;

      float a = Mth.clamp(strength * (float)DabyWSClientConfig.stormBackdropStrength, 0.0F, 1.0F);
      return (int)(a * 255.0F) << 24
           | (int)(Mth.clamp(r, 0.0F, 1.0F) * 255.0F) << 16
           | (int)(Mth.clamp(g, 0.0F, 1.0F) * 255.0F) << 8
           | (int)(Mth.clamp(b, 0.0F, 1.0F) * 255.0F);
   }

   private static float ramp(float v, float lo, float hi) {
      if (hi <= lo) {
         return v >= hi ? 1.0F : 0.0F;
      }
      return smooth(Mth.clamp((v - lo) / (hi - lo), 0.0F, 1.0F));
   }

   private static float smooth(float t) {
      t = Mth.clamp(t, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }
}
