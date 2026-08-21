package net.dabicco.witherstormmod.entity.ability;

import java.util.ArrayList;
import java.util.List;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.server.level.ServerLevel;

/**
 * Holds and ticks all registered abilities for a Wither Storm.
 *
 * The phase machine consults the unlocked abilities each tick; abilities that have not
 * reached their phase threshold are skipped. This keeps the entity's tick loop small
 * and the ability logic decoupled.
 */
public final class StormAbilitySet {
   private final List<StormAbility> abilities = new ArrayList<>();

   public void add(StormAbility ability) {
      this.abilities.add(ability);
   }

   public void tick(WitherStormEntity storm, ServerLevel level) {
      for (StormAbility ability : this.abilities) {
         if (ability.available(storm)) {
            ability.tick(storm, level);
         }
      }
   }
}
