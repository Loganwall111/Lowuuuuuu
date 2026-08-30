package net.dabicco.witherstormmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** A hostile manifestation of the spreading Decayed Reality corruption. */
public class CorruptionEntity extends Monster {
   public CorruptionEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
   }
}
