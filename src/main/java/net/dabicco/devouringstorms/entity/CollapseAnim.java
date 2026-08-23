package net.dabicco.devouringstorms.entity;

import net.minecraft.util.Mth;

public final class CollapseAnim {
   public static final int FALL_TICKS = 75;
   public static final int DOWN_TICKS = 1000;
   public static final int RISE_TICKS = 170;
   public static final int TOTAL_TICKS = 1170;
   private static final int RELIGHT_GAP = 34;
   private static final float RELIGHT_FADE = 26.0F;
   private static final float IMPACT_AT = 0.82F;
   public static final int IMPACT_TICK = 61;

   private CollapseAnim() {
   }

   public static boolean active(float ticks) {
      return ticks >= 0.0F && ticks < 1170.0F;
   }

   public static boolean falling(float ticks) {
      return ticks >= 0.0F && ticks < 75.0F;
   }

   public static boolean isImpactTick(float ticks) {
      return ticks >= 61.0F && ticks < 62.0F;
   }

   public static float down(float ticks) {
      if (ticks < 0.0F) {
         return 0.0F;
      } else if (ticks >= 1170.0F) {
         return 0.0F;
      } else if (ticks < 75.0F) {
         float p = ticks / 75.0F;
         if (p < 0.82F) {
            return p * p / 0.6724F;
         } else {
            float q = (p - 0.82F) / 0.18F;
            return 1.0F - 0.085F * Mth.sin((double)(q * (float)Math.PI * 1.5F)) * (1.0F - q);
         }
      } else if (ticks < 1000.0F) {
         return 1.0F;
      } else {
         float r = Mth.clamp((ticks - 1000.0F) / 170.0F, 0.0F, 1.0F);
         return 1.0F - r * r * (3.0F - 2.0F * r);
      }
   }

   public static float bodyPitch(float ticks) {
      return -94.0F * down(ticks);
   }

   public static float droop(float ticks) {
      return down(ticks);
   }

   public static float jawSlack(float ticks) {
      return 26.0F * down(ticks);
   }

   public static float headLit(float ticks, int index) {
      if (!(ticks < 0.0F) && !(ticks >= 1170.0F)) {
         float on = 1000.0F + (float)index * 34.0F;
         return ticks < on ? 0.0F : Mth.clamp((ticks - on) / 26.0F, 0.0F, 1.0F);
      } else {
         return 1.0F;
      }
   }

   public static boolean headDormant(float ticks, int index) {
      return headLit(ticks, index) < 0.999F;
   }

   public static float severedSpin(float ticks, int side) {
      return ticks < 0.0F ? 0.0F : 0.0F;
   }

   public static float severedRoll(float ticks, int side) {
      return 0.0F;
   }

   public static float severedPitch(float ticks) {
      return 0.0F;
   }
}
