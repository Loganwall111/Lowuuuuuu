package net.dabicco.witherstormmod.entity.ability;

import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.server.level.ServerLevel;

/**
 * Clean ability contract.
 *
 * Abilities are per-phase powers the Wither Storm gains as it grows. Each ability
 * declares the phase at which it becomes available and a per-tick execution hook.
 * Keeping them as small, focused classes (rather than one giant tick method) makes the
 * AI readable and the tuning data-driven from the config.
 */
public interface StormAbility {
   /** The minimum phase at which this ability can fire. */
   double phaseThreshold();

   /**
    * Called on the server each tick while the ability is unlocked. Implementations
    * gate on their own cooldowns/conditions internally.
    */
   void tick(WitherStormEntity storm, ServerLevel level);

   /** Whether the ability is currently available given the storm's phase. */
   default boolean available(WitherStormEntity storm) {
      return storm.getPhase() >= this.phaseThreshold();
   }
}
