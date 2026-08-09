package com.rewritten.devouringstorms.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * SKY TENTACLE — the Forger's signature, delivered next-day.
 * Falling streak of sinew and intent; harmless mid-air, decisive at arrival: it strikes the
 * ground, bruises everything in the crush radius, and dissolves back into being an anecdote.
 */
public class SkyTentacleEntity extends Monster {

    private boolean landed = false;
    private int landedAt = -1;

    public SkyTentacleEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createTentacleAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 16.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
            .add(Attributes.ATTACK_DAMAGE, 12.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        // crush while falling
        if (!landed) {
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(1.6), e -> e.isAlive() && e != this)) {
                victim.hurt(this.damageSources().generic(), 12.0f);
                victim.setDeltaMovement(victim.getDeltaMovement().add(0, 0.6, 0));
                victim.hurtMarked = true;
            }
        }

        if ((this.onGround() || this.position().y <= level.getMinY() + 1) && !landed) {
            landed = true;
            landedAt = this.tickCount;
            // arrival: a small local verdict
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(3.2), e -> e.isAlive() && e != this)) {
                victim.hurt(this.damageSources().generic(), 18.0f);
                victim.setDeltaMovement(victim.getDeltaMovement().add(
                    (victim.getX() - this.getX()) * 0.4, 0.9, (victim.getZ() - this.getZ()) * 0.4));
                victim.hurtMarked = true;
            }
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                this.getX(), this.getY() + 0.2, this.getZ(), 30, 2.0, 0.3, 2.0, 0.04);
        }

        if (landed && this.tickCount > landedAt + 30) {
            // being stepped on is a short career
            this.discard();
        }
    }
}
