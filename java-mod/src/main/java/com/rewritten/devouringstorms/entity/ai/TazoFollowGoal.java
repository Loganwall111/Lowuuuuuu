package com.rewritten.devouringstorms.entity.ai;

import com.rewritten.devouringstorms.entity.TazoEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/** Follows its bonded player — stopping to fight, then hurrying to catch up. */
public class TazoFollowGoal extends Goal {

    private final TazoEntity tazo;
    private final double speed;
    private final float stopDistance;
    private final float startDistance;

    public TazoFollowGoal(TazoEntity tazo, double speed, float stopDistance, float startDistance) {
        this.tazo = tazo;
        this.speed = speed;
        this.stopDistance = stopDistance;
        this.startDistance = startDistance;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.tazo.getOwner();
        return owner != null && this.tazo.getTarget() == null && this.tazo.distanceTo(owner) > this.startDistance;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity owner = this.tazo.getOwner();
        return owner != null && this.tazo.getTarget() == null && this.tazo.distanceTo(owner) > this.stopDistance;
    }

    @Override
    public void tick() {
        LivingEntity owner = this.tazo.getOwner();
        if (owner != null) {
            this.tazo.getMoveControl().setWantedPosition(owner.getX(), owner.getY(), owner.getZ(), this.speed);
        }
    }
}
