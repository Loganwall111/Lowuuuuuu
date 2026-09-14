package net.dabicco.witherstormmod.client;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * MCSM: fog tint that drives the shader storm gate — ONLY while a real
 * phase-5+ storm is nearby. Calm night stays deep blue; purple/pink/teal fog
 * is phase-locked to the user strips.
 */
public final class StormSkyDome {
   // phase 5 teal
   private static final float[] TEAL = new float[]{0.220F, 0.145F, 0.325F};
   // phase 5.4 purple
   private static final float[] PURP = new float[]{0.220F, 0.070F, 0.320F};
   // phase 5.5 pink-magenta, restrained so normal night does not become purple
   private static final float[] PINK = new float[]{0.380F, 0.120F, 0.320F};
   // phase 6+ storm-grey with only a little purple undertone
   private static final float[] SIX = new float[]{0.190F, 0.170F, 0.210F};
   private static final double RANGE = 1700.0;
   private static float displayed;
   private static float displayedCore;
   private static float phaseSeen;

   private StormSkyDome() {
   }

   public static void update(Vec3 camera) {
      float influence = 0.0F;
      float core = 0.0F;
      float selectedPhase = 0.0F;
      net.dabicco.witherstormmod.client.ClientDistantStormManager.StormData state =
         net.mcsm.extras.client.McsmStormOrigin.nearest(camera);
      if (state == null) {
         displayed = 0.0F;
         displayedCore = 0.0F;
         phaseSeen = 0.0F;
         return;
      }
      if (state != null) {
         Vec3 origin = net.mcsm.extras.client.McsmStormOrigin.getStormOrigin(state);
         double distance = origin.distanceTo(camera);
         if (distance > RANGE) {
            displayed = 0.0F;
            displayedCore = 0.0F;
            phaseSeen = 0.0F;
            return;
         }
         if (distance <= RANGE) {
            double fraction = distance / RANGE;
            float distanceWeight = fraction <= 0.55D
               ? 1.0F : smooth((float) (1.0D - (fraction - 0.55D) / 0.45D));
            influence = distanceWeight * ramp(state.phase, 5.0F, 5.12F);
            core = distanceWeight * ramp(state.phase, 5.0F, 5.35F);
            selectedPhase = state.phase;
         }
      }
      displayed += (influence - displayed) * 0.05F;
      displayedCore += (core - displayedCore) * 0.05F;
      if (displayed < 0.002F) displayed = 0.0F;
      if (displayedCore < 0.002F) displayedCore = 0.0F;
      if (selectedPhase > 0.0F) phaseSeen = selectedPhase;
      else if (displayed < 0.01F) phaseSeen = 0.0F;
   }

   public static float strength() {
      // MCSM: fog tint only while a real phase-5+ storm is nearby AND displayed.
      // Cap low so residual fog cannot purple-wash the calm night vault.
      if (!DabyWSClientConfig.stormBackdrop) {
         return 0.0F;
      }
      if (phaseSeen < 5.0F) {
         return 0.0F;
      }
      return Mth.clamp(displayed * (float)DabyWSClientConfig.stormBackdropStrength, 0.0F, 1.0F);
   }

   public static float coreStrength() {
      return Mth.clamp(displayedCore, 0.0F, 1.0F);
   }

   public static float phase() {
      return phaseSeen;
   }

   public static void skyColor(float[] var0) {
      float p = phaseSeen;
      float wTeal = ramp(p, 5.0F, 5.10F) * (1.0F - ramp(p, 5.30F, 5.42F));
      float wPurp = ramp(p, 5.30F, 5.42F) * (1.0F - ramp(p, 5.52F, 5.65F));
      float wPink = ramp(p, 5.52F, 5.65F) * (1.0F - ramp(p, 5.92F, 6.10F));
      float wSix  = ramp(p, 5.92F, 6.15F);
      float tot = wTeal + wPurp + wPink + wSix;
      if (tot <= 1.0E-4F || p < 5.0F) {
         // no phase tint — leave fog alone (calm blue night)
         var0[0] = 0.012F;
         var0[1] = 0.035F;
         var0[2] = 0.360F;
      } else {
         var0[0] = (TEAL[0] * wTeal + PURP[0] * wPurp + PINK[0] * wPink + SIX[0] * wSix) / tot;
         var0[1] = (TEAL[1] * wTeal + PURP[1] * wPurp + PINK[1] * wPink + SIX[1] * wSix) / tot;
         var0[2] = (TEAL[2] * wTeal + PURP[2] * wPurp + PINK[2] * wPink + SIX[2] * wSix) / tot;
      }
   }

   private static float ramp(float var0, float var1, float var2) {
      if (var2 <= var1) {
         return var0 >= var2 ? 1.0F : 0.0F;
      } else {
         return smooth(Mth.clamp((var0 - var1) / (var2 - var1), 0.0F, 1.0F));
      }
   }

   private static float smooth(float var0) {
      var0 = Mth.clamp(var0, 0.0F, 1.0F);
      return var0 * var0 * (3.0F - 2.0F * var0);
   }
}
