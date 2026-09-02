package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * The storm's dynamic sky.
 *
 * Measuring the reference frames settled a long argument with myself: the
 * purple covers 99-100% of the screen. It is not an object hanging behind the
 * creature, it is the sky itself. Any quad, at any size, reads as a curtain
 * sliding past the camera -- which is exactly what the user kept rejecting.
 *
 * So this drives {@code SkyRenderState.skyColor} directly. There is no
 * geometry, nothing to walk behind, nothing to clip through, and no parallax,
 * because the sky dome is already infinitely far away by construction.
 *
 * Colour progression, per the user's timeline:
 *   below 4.5   nothing at all
 *   4.5 - 6.0   dark turquoise / green (holds through the END of phase 5)
 *   6.0 +       purple
 *   6.3 +       magenta, deepening toward violet-pink as it grows
 * with red mixed into the late gradient and a black core throughout.
 */
public final class StormSkyDome {

   /* palette, sampled from the user's reference screenshots */
   private static final float[] TURQ = { 0.094F, 0.184F, 0.180F };  // #182F2E
   private static final float[] PURP = { 0.220F, 0.145F, 0.325F };  // #382553
   private static final float[] MAGE = { 0.463F, 0.102F, 0.404F };  // #761A67
   private static final float[] PINK = { 0.639F, 0.180F, 0.573F };  // #A32E92
   private static final float[] RED  = { 0.400F, 0.075F, 0.145F };  // #661326

   /** How far out the sky reacts to the storm at all. */
   private static final double RANGE = 900.0;

   private static float displayed;      // smoothed 0..1 overall influence
   private static float displayedCore;  // smoothed 0..1 black-core weight
   private static float phaseSeen;      // phase of the dominant storm

   private StormSkyDome() {
   }

   /** Recomputed once per frame from the client-side storm list. */
   public static void update(Vec3 cameraPos) {
      float target = 0.0F;
      float core = 0.0F;
      float bestPhase = 0.0F;

      for (ClientDistantStormManager.StormData d : ClientDistantStormManager.all()) {
         if (d.phase < 4.5F) {
            continue;                       // nothing happens before 4.5
         }
         double dx = d.dispX - cameraPos.x;
         double dy = d.dispY - cameraPos.y;
         double dz = d.dispZ - cameraPos.z;
         double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
         if (dist > RANGE) {
            continue;
         }
         // full strength up to 55% of range, then ease out
         double frac = dist / RANGE;
         float proximity = frac <= 0.55
            ? 1.0F
            : smooth((float)(1.0 - (frac - 0.55) / 0.45));

         float onset = ramp(d.phase, 4.45F, 4.90F);
         float w = proximity * onset;
         if (w > target) {
            target = w;
            bestPhase = d.phase;
         }
         // the black core tightens as the storm grows
         core = Math.max(core, proximity * ramp(d.phase, 4.45F, 5.20F));
      }

      displayed += (target - displayed) * 0.05F;
      displayedCore += (core - displayedCore) * 0.05F;
      if (displayed < 0.002F) {
         displayed = 0.0F;
      }
      if (displayedCore < 0.002F) {
         displayedCore = 0.0F;
      }
      if (bestPhase > 0.0F) {
         phaseSeen = bestPhase;
      }
   }

   /** Overall influence of the storm sky, 0 when disabled or far away. */
   public static float strength() {
      if (!DabyWSClientConfig.stormBackdrop) {
         return 0.0F;
      }
      return Mth.clamp(displayed * (float)DabyWSClientConfig.stormBackdropStrength, 0.0F, 1.0F);
   }

   /** Weight of the black core that sits behind the body. */
   public static float coreStrength() {
      return Mth.clamp(displayedCore, 0.0F, 1.0F);
   }

   public static float phase() {
      return phaseSeen;
   }

   /**
    * Colour of the storm sky for the current phase, written into {@code out}.
    *
    * Weights deliberately overlap so the transitions cross-fade rather than
    * snap: green holds all the way through phase 5, purple takes over from
    * 6.0, and pink keeps deepening past 6.3 with red mixed in.
    */
   public static void skyColor(float[] out) {
      float p = phaseSeen;

      float wTurq = ramp(p, 4.45F, 4.90F) * (1.0F - ramp(p, 6.00F, 6.35F));
      float wPurp = ramp(p, 6.00F, 6.35F) * (1.0F - ramp(p, 6.30F, 6.90F));
      float wMage = ramp(p, 6.30F, 6.90F) * (1.0F - ramp(p, 6.80F, 7.40F));
      float wPink = ramp(p, 6.80F, 7.40F);
      float wRed  = ramp(p, 6.50F, 7.60F) * 0.30F;   // red embedded in the late gradient

      float total = wTurq + wPurp + wMage + wPink + wRed;
      if (total <= 0.0001F) {
         out[0] = TURQ[0];
         out[1] = TURQ[1];
         out[2] = TURQ[2];
         return;
      }
      for (int i = 0; i < 3; i++) {
         out[i] = (TURQ[i] * wTurq + PURP[i] * wPurp + MAGE[i] * wMage
                   + PINK[i] * wPink + RED[i] * wRed) / total;
      }
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
