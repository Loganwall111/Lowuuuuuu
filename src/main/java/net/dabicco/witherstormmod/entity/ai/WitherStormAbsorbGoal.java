package net.dabicco.witherstormmod.entity.ai;

import java.util.EnumSet;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

/**
 * Clean absorb goal.
 *
 * The storm vacuums nearby item entities into its body, gaining growth material. This
 * is the simplest expression of "the storm consumes the world" and feeds the phase
 * machine's {@code addSubGrowth}. Cluster/block absorption is handled separately by the
 * cluster system, so this goal focuses on loose items (fragments are protected so the
 * player's progress isn't eaten).
 */
public class WitherStormAbsorbGoal extends Goal {
   private final WitherStormEntity storm;
   private int scanTimer;

   public WitherStormAbsorbGoal(WitherStormEntity storm) {
      this.storm = storm;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      return this.storm.isAlive() && this.storm.getPhase() >= 1.0;
   }

   @Override
   public void tick() {
      if (--this.scanTimer > 0) {
         return;
      }
      this.scanTimer = 10;
      AABB box = this.storm.getBoundingBox().inflate(24.0, 48.0, 24.0);
      for (ItemEntity item : this.storm.level().getEntitiesOfClass(ItemEntity.class, box)) {
         if (item.getItem().is(net.dabicco.witherstormmod.ModItems.WITHER_FRAGMENT)) {
            continue; // protect the player's progression item
         }
         item.setDeltaMovement(item.getDeltaMovement().scale(0.7)
            .add(this.storm.getBoundingBox().getCenter().subtract(item.position()).normalize().scale(0.45)));
         item.hurtMarked = true;
         if (item.distanceToSqr(this.storm) < 6.0 * 6.0) {
            this.storm.addSubGrowth(1);
            item.discard();
         }
      }
   }
}
