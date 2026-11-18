package net.dabicco.witherstormmod.entity.animation;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;

public final class PhaseStormAnim {
   private static float phase;

   private PhaseStormAnim() {
   }

   public static void setPhase(float value) {
      phase = value;
   }

   public static float strength() {
      return DabyWSClientConfig.phaseAnim ? (float)DabyWSClientConfig.phaseAnimStrength : 0.0F;
   }

   private static float staged(float p0, float p4, float p5, float p58, float p65) {
      float p = phase;
      if (p <= 2.0F) {
         return lerp(p0, p4, p / 2.0F);
      } else if (p <= 4.0F) {
         return lerp(p4, p5, (p - 2.0F) / 2.0F);
      } else if (p <= 5.0F) {
         return lerp(p5, p58, p - 4.0F);
      } else {
         return p <= 5.8F ? lerp(p58, p65, (p - 5.0F) / 0.8F) : lerp(p65, p65, Math.min(1.0F, (p - 5.8F) / 0.7F));
      }
   }

   private static float lerp(float a, float b, float t) {
      return a + (b - a) * Math.max(0.0F, Math.min(1.0F, t));
   }

   private static float blend(float m) {
      return 1.0F + (m - 1.0F) * strength();
   }

   public static float speed() {
      return blend(staged(0.82F, 1.0F, 1.12F, 1.32F, 1.5F));
   }

   public static float depth() {
      return blend(staged(0.85F, 1.0F, 1.08F, 1.25F, 1.4F));
   }

   public static float breath() {
      return blend(staged(1.1F, 1.0F, 0.95F, 1.15F, 1.3F));
   }

   public static float agitation(float t) {
      return staged(0.0F, 0.02F, 0.05F, 0.09F, 0.12F) * strength() * (float)Math.sin(t * 9.0F + phase * 3.7F);
   }
}
