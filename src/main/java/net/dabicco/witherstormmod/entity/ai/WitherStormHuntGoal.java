package net.dabicco.witherstormmod.entity.ai;

import java.util.EnumSet;
import net.dabicco.witherstormmod.entity.WitherStormEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/**
 * Clean hunt goal.
 *
 * Phase 4+ Wither Storms relentlessly pursue a living target. Pre-phase 4 it behaves
 * more like a normal Wither and only moves toward the nearest player opportunistically.
 */
public class WitherStormHuntGoal extends Goal {
   private final WitherStormEntity storm;
   private int repathTimer;

   public WitherStormHuntGoal(WitherStormEntity storm) {
      this.storm = storm;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      return this.storm.isPhase4() && this.storm.isAlive();
   }

   @Override
   public boolean canContinueToUse() {
      return this.canUse() && this.storm.getTarget() != null && this.storm.getTarget().isAlive();
   }

   @Override
   public void start() {
      this.repathTimer = 0;
   }

   @Override
   public void tick() {
      LivingEntity target = this.storm.getTarget();
      if (target == null) {
         Player nearest = this.storm.getLevel() instanceof net.minecraft.server.level.ServerLevel sl
            ? sl.getNearestPlayer(this.storm, 128.0)
            : null;
         if (nearest != null) {
            this.storm.setTarget(nearest);
         }
         return;
      }
      if (--this.repathTimer <= 0) {
         this.repathTimer = 20;
         this.storm.getNavigation().moveTo(target, 1.0);
      }
   }
}
