package com.rewritten.devouringstorms.entity.ai;

import com.rewritten.devouringstorms.entity.MassgEntity;
import com.rewritten.devouringstorms.storm.MassgPhase;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * The storm hunts — drifting towards the largest concentration of players it can find,
 * hovering above them like weather that decided to be hungry.
 */
public class MassgChaseGoal extends Goal {

    private final MassgEntity massg;
    private Player target;
    private int recalcTimer;

    public MassgChaseGoal(MassgEntity massg) {
        this.massg = massg;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        MassgPhase phase = this.massg.getPhase();
        return phase != MassgPhase.SLEEPING && this.massg.getDeadTicks() < 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.massg.getDeadTicks() < 0;
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        if (--this.recalcTimer > 0) return;
        this.recalcTimer = 60; // re-aim every 3 s; the storm is patient

        this.target = findPrey();

        double wantedX, wantedY, wantedZ;
        if (this.target != null) {
            wantedX = this.target.getX();
            wantedZ = this.target.getZ();
            // hover 10-20 blocks above the prey; closer and heavier as it evolves
            wantedY = this.target.getY() + 14.0 - this.massg.getPhase().ordinal() * 1.5
                + Math.sin(this.massg.tickCount * 0.01) * 3.0;
        } else {
            // no prey: wander the sky, slowly
            wantedX = this.massg.getX() + (this.massg.getRandom().nextDouble() - 0.5) * 80.0;
            wantedY = Math.max(this.massg.level().getSeaLevel() + 24.0, this.massg.getY() + (this.massg.getRandom().nextDouble() - 0.5) * 16.0);
            wantedZ = this.massg.getZ() + (this.massg.getRandom().nextDouble() - 0.5) * 80.0;
        }
        this.massg.getMoveControl().setWantedPosition(wantedX, wantedY, wantedZ, 1.0);
    }

    /** Prefers the densest cluster of players (DR behaviour: it hunts groups). */
    private Player findPrey() {
        var players = this.massg.level().getEntitiesOfClass(Player.class,
            this.massg.getBoundingBox().inflate(160.0),
            p -> p.isAlive() && !p.isSpectator());
        if (players.isEmpty()) return null;

        Player best = null;
        int bestCrowd = -1;
        for (Player p : players) {
            int crowd = this.massg.level().getEntitiesOfClass(Player.class,
                p.getBoundingBox().inflate(32.0)).size();
            if (crowd > bestCrowd) {
                bestCrowd = crowd;
                best = p;
            }
        }
        return best;
    }

    public Vec3 debugInfo() {
        return this.target != null ? this.target.position() : Vec3.ZERO;
    }
}
