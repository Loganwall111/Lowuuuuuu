package net.mcsm.extras.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.mcsm.extras.McsmSounds;

/**
 * BUILD #456 -- THE VOIDWALKER: the thing in the nothing.
 *
 * <p>"voidwalkers (zombie-like)". A real entity with its own body
 * ({@link net.mcsm.extras.client.McsmMobModels.VoidwalkerModel}), its own skin and
 * its own behaviour -- not a vanilla zombie with the colour changed, which is what
 * the void used to send at people.
 *
 * <p>How it moves: it walks when it can see you, and when it cannot reach you it
 * <b>falls at you</b> -- a burst of downward-and-forward speed, a spray of void
 * particles, and its own sound, on a short cooldown. In a dimension whose whole
 * floor is a long way down, a mob that throws itself across the gap is the right
 * kind of wrong.
 */
public class McsmVoidwalker extends Monster {

    /** Ticks until it can throw itself at a target again. */
    private int dash = 60 + (int) (Math.random() * 80);
    /** Ticks until it makes a sound again. */
    private int voice = 100 + (int) (Math.random() * 200);

    public McsmVoidwalker(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setCanPickUpLoot(false);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 32.0F, 0.9F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        try {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distance = this.distanceTo(target);
                // the throw: too far to walk, close enough to want
                if (distance > 9.0D && distance < 40.0D && --this.dash <= 0) {
                    this.dash = 90 + this.getRandom().nextInt(90);
                    Vec3 to = target.position().subtract(this.position()).normalize();
                    this.setDeltaMovement(to.x * 1.35D, to.y * 0.5D + 0.30D, to.z * 1.35D);
                    this.hurtMarked = true;
                    Level level = this.level();
                    level.playSound(null, this.getX(), this.getY(), this.getZ(),
                            McsmSounds.OBLIVION_WARP, SoundSource.HOSTILE, 1.4F, 1.5F);
                    for (int i = 0; i < 24; i++) {
                        level.addParticle(ParticleTypes.PORTAL,
                                this.getX() + (this.getRandom().nextDouble() - 0.5D) * 1.4D,
                                this.getY() + this.getRandom().nextDouble() * 2.0D,
                                this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 1.4D,
                                -to.x * 0.35D, -0.25D, -to.z * 0.35D);
                    }
                }
            }
            // a thin trail of nothing follows it everywhere
            if (this.tickCount % 6 == 0) {
                this.level().addParticle(ParticleTypes.SQUID_INK,
                        this.getX() + (this.getRandom().nextDouble() - 0.5D) * 0.7D,
                        this.getY() + this.getRandom().nextDouble() * 1.8D,
                        this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 0.7D,
                        0.0D, -0.006D, 0.0D);
            }
            if (--this.voice <= 0) {
                this.voice = 140 + this.getRandom().nextInt(260);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        McsmSounds.MASSG_WHISPER, SoundSource.HOSTILE, 0.9F,
                        1.35F + this.getRandom().nextFloat() * 0.2F);
            }
        } catch (Throwable ignored) {
            // a walker must never break the tick loop
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
