package com.rewritten.devouringstorms.entity.ai;

import com.rewritten.devouringstorms.entity.MassgEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

/**
 * Slow, inevitable drift. The storm does not chase — it approaches.
 * Movement is hand-rolled (noGravity flight), accelerating towards its wanted point.
 */
public class MassgMoveControl extends MoveControl {

    private final MassgEntity massg;

    public MassgMoveControl(MassgEntity massg) {
        super(massg);
        this.massg = massg;
    }

    @Override
    public void tick() {
        if (this.operation != Operation.MOVE_TO) {
            // idle: barely perceptible drift, like weather
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.96));
            return;
        }
        Vec3 toTarget = new Vec3(
            this.wantedX - this.mob.getX(),
            this.wantedY - this.mob.getY(),
            this.wantedZ - this.mob.getZ()
        );
        double dist = toTarget.length();
        if (dist < 2.0) {
            this.operation = Operation.WAIT;
            return;
        }

        double maxStep = 0.55 + this.massg.getPhase().ordinal() * 0.08; // faster as it evolves
        Vec3 accel = toTarget.normalize().scale(Math.min(maxStep * 0.05, dist * 0.0016));
        Vec3 velocity = this.mob.getDeltaMovement().add(accel);
        double speed = velocity.length();
        if (speed > maxStep) velocity = velocity.normalize().scale(maxStep);
        this.mob.setDeltaMovement(velocity);

        if (speed > 0.05) {
            float targetYaw = (float) (Mth.atan2(velocity.z, velocity.x) * Mth.RAD_TO_DEG) - 90.0f;
            this.mob.setYRot(Mth.rotLerp(this.mob.getYRot(), targetYaw, 1.2f));
            this.mob.setYBodyRot(this.mob.getYRot());
        }
    }
}
