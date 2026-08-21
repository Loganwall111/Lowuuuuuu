package net.dabicco.witherstormmod.entity;

import net.dabicco.witherstormmod.config.WitherStormWorldConfig;

/**
 * Clean phase progression for the Wither Storm.
 *
 * The storm grows through distinct body stages (mirroring Minecraft: Story Mode):
 * it starts as a commanded Wither and eventually becomes the Devourer. Each phase
 * unlocks abilities and changes movement/targeting. Phase is stored as a double
 * (0.0 .. 6.99) so sub-growth within a main phase is possible; this enum encodes the
 * main-phase boundaries and the per-phase requirements from the world config.
 */
public enum WitherStormPhase {
   COMMANDED(0.0),
   MINI_HEAD(1.0),
   TENTACLES(2.0),
   COCOON(3.0),
   STORM(4.0),
   STORM_DEVOURING(5.0),
   DEVOURER(6.0);

   private final double min;

   WitherStormPhase(double min) {
      this.min = min;
   }

   public double min() {
      return this.min;
   }

   /** Main phase (integer part) for a raw phase value. */
   public static int mainOf(double phase) {
      return (int) Math.floor(phase);
   }

   /** True when the given phase value is inside this main phase (0-1 sub-range). */
   public boolean contains(double phase) {
      return mainOf(phase) == (int) Math.floor(this.min);
   }

   /**
    * Number of absorbed/consumed units required to leave a given main phase.
    * Phase 0-3 ramp up slowly, phase 4+ requires far more (the storm is huge then).
    */
   public static int requirement(int mainPhase, WitherStormWorldConfig config) {
      double mod = config.phaseRequirementModifier;
      switch (mainPhase) {
         case 0: return (int) (25.0 * mod);
         case 1: return (int) (50.0 * mod);
         case 2: return (int) (100.0 * mod);
         case 3: return (int) (200.0 * mod);
         case 4: return (int) (config.phase4Requirement * mod);
         default: return (int) (config.phase5Requirement * mod);
      }
   }

   /** Highest naturally reachable phase (before the formidibomb finale). */
   public static final double MAX_NATURAL = 5.9999;
   /** Devourer ceiling. */
   public static final double MAX_DEVOURER = 6.99;
}
