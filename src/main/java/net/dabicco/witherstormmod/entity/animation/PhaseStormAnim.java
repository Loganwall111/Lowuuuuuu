package net.dabicco.witherstormmod.entity.animation;

import net.dabicco.witherstormmod.config.DabyWSClientConfig;

/** Phase-shaped, opt-out multipliers for the storm's authored idle motion. */
public final class PhaseStormAnim {
   private static float phase;
   private PhaseStormAnim() {}
   public static void setPhase(float value) { phase = value; }
   public static float strength() { return DabyWSClientConfig.phaseAnim ? (float)DabyWSClientConfig.phaseAnimStrength : 0.0F; }
   private static float staged(float p0, float p4, float p5, float p58, float p65) {
      float p = phase;
      if (p <= 2) return lerp(p0, p4, p / 2F);
      if (p <= 4) return lerp(p4, p5, (p - 2F) / 2F);
      if (p <= 5) return lerp(p5, p58, p - 4F);
      if (p <= 5.8F) return lerp(p58, p65, (p - 5F) / .8F);
      return lerp(p65, p65, Math.min(1F, (p - 5.8F) / .7F));
   }
   private static float lerp(float a, float b, float t) { return a + (b-a) * Math.max(0F, Math.min(1F, t)); }
   private static float blend(float m) { return 1F + (m - 1F) * strength(); }
   public static float speed() { return blend(staged(.82F, 1F, 1.12F, 1.32F, 1.5F)); }
   public static float depth() { return blend(staged(.85F, 1F, 1.08F, 1.25F, 1.4F)); }
   public static float breath() { return blend(staged(1.1F, 1F, .95F, 1.15F, 1.3F)); }
   public static float agitation(float t) { return staged(0F,.02F,.05F,.09F,.12F) * strength() * (float)Math.sin(t * 9F + phase * 3.7F); }
}
