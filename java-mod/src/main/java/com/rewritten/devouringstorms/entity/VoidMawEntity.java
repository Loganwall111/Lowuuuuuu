package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * THE VOID MAW. A black hole that got lost in the multiverse and liked the menu.
 * It hovers where the sky got thin — the Fray, the edges of the quarantine — and it pulls
 * everything that wanders too close into its quiet. Everymeal makes it a mouth with more mass.
 * You cannot kill it. You can only not be light.
 */
public class VoidMawEntity extends Monster {

    private static final double PULL_RADIUS = 26.0;
    private static final double KILL_RADIUS = 2.2;

    /** Everything it has eaten. It never stays full. */
    private float mass = 0.0f;

    public VoidMawEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setNoAi(true);
    }

    public static AttributeSupplier.Builder createMawAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 200.0)
            .add(Attributes.MOVEMENT_SPEED, 0.05)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
            .add(Attributes.FOLLOW_RANGE, 64.0)
            .add(Attributes.SCALE, 1.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        // ---- photon ring: light choosing a side ----
        if (this.tickCount % 3 == 0) {
            double r = 1.5 * this.getBbWidth();
            double a = this.tickCount * 0.21;
            level.sendParticles(ParticleTypes.END_ROD,
                this.getX() + Math.cos(a) * r, this.getY() + 0.2, this.getZ() + Math.sin(a) * r,
                2, 0.12, 0.04, 0.12, 0.02);
            level.sendParticles(ModParticles.GLITCH,
                this.getX() - Math.cos(a) * r * 0.85, this.getY() + 0.1, this.getZ() - Math.sin(a) * r * 0.85,
                2, 0.1, 0.08, 0.1, 0.02);
        }
        if (this.tickCount % 47 == 0) {
            level.playSound(null, this, ModSounds.AMBIENT_RIFT_HUM, SoundSource.HOSTILE, 2.0f, 0.4f);
        }

        // ---- gravity, gently: everything near bends inward ----
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(PULL_RADIUS), e -> e != this)) {
            Vec3 pull = this.position().subtract(victim.position());
            double d = pull.length();
            if (d < 0.001) continue;
            double strength = 0.05 * (1.0 - d / PULL_RADIUS) + 0.012;
            victim.setDeltaMovement(victim.getDeltaMovement().add(pull.normalize().scale(strength)));
            victim.fallDistance = 0.0f;
            if (d < KILL_RADIUS) {
                victim.hurt(this.damageSources().magic(), 8.0f);
                if (!victim.isAlive()) {
                    // conservation the multiverse way: their mass becomes its mass
                    this.mass += 0.15f;
                    var scaleAttr = this.getAttribute(Attributes.SCALE);
                    if (scaleAttr != null && scaleAttr.getBaseValue() < 2.4f) {
                        scaleAttr.setBaseValue(1.0 + mass);
                        this.refreshDimensions();
                    }
                    level.playSound(null, this, ModSounds.MASSG_DEVOUR, SoundSource.HOSTILE, 2.0f, 0.3f);
                }
            }
        }

        // ---- slow brownian drift, because a maw that sits still is just a hole ----
        if (this.tickCount % 55 == 0) {
            this.setDeltaMovement((this.getRandom().nextGaussian()) * 0.04,
                this.getRandom().nextGaussian() * 0.02, this.getRandom().nextGaussian() * 0.04);
        }
    }
}
