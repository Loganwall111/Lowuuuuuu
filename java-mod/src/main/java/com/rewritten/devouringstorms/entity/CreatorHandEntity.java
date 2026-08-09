package com.rewritten.devouringstorms.entity;

import com.rewritten.devouringstorms.registry.ModParticles;
import com.rewritten.devouringstorms.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * THE HAND — the part of the Creator that reaches down a continent and answers.
 * It hovers one breath above the named coordinates (that's the telegraph, that's the mercy),
 * then it comes down like a verdict: crater, shockwave, and everyone near it learning to fly
 * for one and a half seconds.
 */
public class CreatorHandEntity extends Entity {

    private static final int TELEGRAPH_TICKS = 26;
    private static final int RETRACT_TICKS = 50;

    private UUID owner;
    private Vec3 strikeAt;
    private int strikeCountdown = -1;

    public CreatorHandEntity(EntityType<? extends CreatorHandEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
        this.setNoGravity(true);
    }

    public void setOwner(UUID id) { this.owner = id; }
    public UUID getOwnerId() { return this.owner; }
    public void setStrikeAt(Vec3 at) { this.strikeAt = at; }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) return;

        // descent phases: hover-telegraph → slam → retract
        float life = this.tickCount;
        if (life <= TELEGRAPH_TICKS) {
            // the mercy pause: tremors, rising ash, a ring you should not stand in
            if (life % 6 == 0) {
                Vec3 at = strikeAt != null ? strikeAt : this.position();
                for (int i = 0; i < 10; i++) {
                    double a = i / 10.0 * Math.PI * 2;
                    level.sendParticles(ParticleTypes.LARGE_SMOKE,
                        at.x + Math.cos(a) * 4.5, at.y + 0.3, at.z + Math.sin(a) * 4.5,
                        2, 0.2, 0.1, 0.2, 0.02);
                }
            }
            this.setDeltaMovement(0, -0.04, 0);
        } else if (life < TELEGRAPH_TICKS + 12) {
            this.setDeltaMovement(0, -1.1, 0);
        } else {
            if (strikeCountdown < 0) strike(level);
            strikeCountdown++;
            this.setDeltaMovement(0, 0.6, 0);
            if (this.tickCount > TELEGRAPH_TICKS + RETRACT_TICKS) {
                this.discard();
                return;
            }
        }
        this.move(MoverType.SELF, this.getDeltaMovement());

        // crush anything beneath the palm while descending
        if (this.getDeltaMovement().y < -0.5) {
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(2.4), e -> e.isAlive())) {
                victim.hurt(this.damageSources().generic(), 24.0f);
                victim.setDeltaMovement(victim.getDeltaMovement().add(
                    (victim.getX() - this.getX()) * 0.4, 1.1, (victim.getZ() - this.getZ()) * 0.4));
                victim.hurtMarked = true;
            }
        }
    }

    private void strike(ServerLevel level) {
        if (strikeCountdown >= 0) return;
        strikeCountdown = 0;
        Vec3 at = strikeAt != null ? strikeAt : this.position();
        level.playSound(null, net.minecraft.core.BlockPos.containing(at),
            ModSounds.MASSG_DEVOUR, net.minecraft.sounds.SoundSource.HOSTILE, 4.0f, 0.45f);
        level.sendParticles(ModParticles.GLITCH, at.x, at.y + 0.6, at.z, 80, 4.0, 0.6, 4.0, 0.06);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 0.5, at.z, 160, 5.0, 0.8, 5.0, 0.08);
        // pity the palm-line
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(7.0), e -> e.isAlive())) {
            victim.hurt(this.damageSources().generic(), 34.0f);
            victim.setDeltaMovement(new Vec3((victim.getX() - at.x) * 0.35, 1.35, (victim.getZ() - at.z) * 0.35));
            victim.hurtMarked = true;
            victim.hurtTime = 10;
            victim.hurtDuration = 10;
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.world.entity.synchedentity.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
