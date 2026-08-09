package com.rewritten.devouringstorms.entity.ai;

import com.rewritten.devouringstorms.entity.WatcherEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * The Watcher keeps its distance and its silence: it closes in when you are far,
 * and backs away, slowly, if you find the nerve to approach it.
 */
public class WatcherStalkGoal extends Goal {

    private static final double TOO_CLOSE = 7.0;
    private static final double TOO_FAR = 26.0;

    private final WatcherEntity watcher;

    public WatcherStalkGoal(WatcherEntity watcher) {
        this.watcher = watcher;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.watcher.getFocus() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.watcher.getFocus() != null && this.watcher.getFocus().isAlive();
    }

    @Override
    public void tick() {
        var focus = this.watcher.getFocus();
        if (focus == null) return;

        // never stops watching
        this.watcher.getLookControl().setLookAt(focus, 10.0f, this.watcher.getMaxHeadXRot());

        double dist = this.watcher.distanceTo(focus);
        if (dist > TOO_FAR) {
            this.watcher.getMoveControl().setWantedPosition(focus.getX(), focus.getY(), focus.getZ(), 0.9);
        } else if (dist < TOO_CLOSE) {
            Vec3 away = this.watcher.position().subtract(focus.position()).normalize().scale(12.0);
            this.watcher.getMoveControl().setWantedPosition(
                this.watcher.getX() + away.x, this.watcher.getY(), this.watcher.getZ() + away.z, 1.0);
        }
    }
}
