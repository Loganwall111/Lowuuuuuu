package com.rewritten.devouringstorms.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * SEVERED STORM. During the Sunderer phase, MASSG tears living fragments off itself.
 * Each fragment is a small storm that wants very much to be a big storm again — and to be fed.
 */
public class SeveredStormEntity extends Monster {

    /** Which storm-straits colour it remembers being torn from. */
    private com.rewritten.devouringstorms.entity.MassgVariant variant =
        com.rewritten.devouringstorms.entity.MassgVariant.CLASSIC;

    public void setVariant(com.rewritten.devouringstorms.entity.MassgVariant v) { this.variant = v; }
    public com.rewritten.devouringstorms.entity.MassgVariant getVariant() { return this.variant; }

    public SeveredStormEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createSeveredAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 60.0)
            .add(Attributes.ATTACK_DAMAGE, 6.0)
            .add(Attributes.FLYING_SPEED, 0.7)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 64.0)
            .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SeveredHuntGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            // storm fragments shed little motes of storm
            if (this.getRandom().nextInt(10) == 0) {
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                    this.getX() + (this.getRandom().nextDouble() - 0.5) * 0.8,
                    this.getY() + this.getRandom().nextDouble() * 0.6,
                    this.getZ() + (this.getRandom().nextDouble() - 0.5) * 0.8,
                    0, 0.02, 0);
            }
            return;
        }
        // contact bite
        if (this.tickCount % 8 == 0) {
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(0.9), e -> e != this && !(e instanceof MassgEntity))) {
                this.doHurtTarget((net.minecraft.server.level.ServerLevel) this.level(), entity);
            }
        }
    }

    /** Flies to its target and hovers just above its head. */
    private static class SeveredHuntGoal extends Goal {
        private final SeveredStormEntity storm;
        private int wanderTimer;

        SeveredHuntGoal(SeveredStormEntity storm) {
            this.storm = storm;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.storm.getTarget();
            if (target != null && target.isAlive()) {
                this.storm.getMoveControl().setWantedPosition(
                    target.getX(), target.getY() + target.getBbHeight() + 1.0, target.getZ(), 1.4);
                return;
            }
            if (--this.wanderTimer <= 0) {
                this.wanderTimer = 80;
                Vec3 drift = this.storm.position().add((this.storm.getRandom().nextDouble() - 0.5) * 24.0,
                    (this.storm.getRandom().nextDouble() - 0.5) * 8.0,
                    (this.storm.getRandom().nextDouble() - 0.5) * 24.0);
                this.storm.getMoveControl().setWantedPosition(drift.x, Math.max(drift.y,
                    this.storm.level().getSeaLevel() + 6.0), drift.z, 0.7);
            }
        }
    }
}
