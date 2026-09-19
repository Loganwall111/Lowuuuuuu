package net.mcsm.extras.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
 * BUILD #456 -- THE VOID LURKER. "the void mini-boss (avoid)".
 *
 * <p>The one mob in the mod the player is meant to run from, and its body says so:
 * a maw, and six tentacles that hang under it
 * ({@link net.mcsm.extras.client.McsmMobModels.VoidLurkerModel}). Its behaviour is
 * the three verbs the mandate gives it, in order:
 *
 * <pre>
 *   REACH   anything inside six and a half blocks is already in its arms. It
 *           does not have to touch you to start pulling.
 *   PULL    a grip every two seconds: the target is dragged off their feet and
 *           toward the maw, with the tentacle sound.
 *   SWALLOW inside two and a half blocks it simply eats -- heavy damage, the
 *           wither, and a hard pull into the middle of it.
 * </pre>
 *
 * <p>It is slow (0.24) and patient (64-block sight, never despawns), so the way to
 * survive one is the way the mandate says: avoid it.
 */
public class McsmVoidLurker extends Monster {

    /** How far the tentacles work. */
    public static final double REACH = 6.5D;
    /** How close the maw has to be. */
    public static final double SWALLOW_RANGE = 2.6D;

    private int grip = 40 + (int) (Math.random() * 40);
    private int swallow = 70 + (int) (Math.random() * 60);
    private int voice = 80 + (int) (Math.random() * 160);

    public McsmVoidLurker(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setCanPickUpLoot(false);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 260.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 48.0F, 0.9F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        try {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distance = this.distanceTo(target);
                Vec3 inward = this.position().subtract(target.position());
                if (inward.lengthSqr() > 1.0E-4D) {
                    inward = inward.normalize();
                }
                Level level = this.level();
                // SWALLOW -- the maw, at the end of the reach
                if (distance <= SWALLOW_RANGE && --this.swallow <= 0) {
                    this.swallow = 90 + this.getRandom().nextInt(60);
                    target.push(inward.x * 1.1D, -0.35D, inward.z * 1.1D);
                    target.hurtMarked = true;
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1, false, false));
                    if (level instanceof ServerLevel server) {
                        target.hurtServer(server, server.damageSources().generic(), 14.0F);
                    }
                    level.playSound(null, this.getX(), this.getY(), this.getZ(),
                            McsmSounds.MASSG_ROAR, SoundSource.HOSTILE, 2.0F, 0.7F);
                    for (int i = 0; i < 60; i++) {
                        level.addParticle(ParticleTypes.SQUID_INK,
                                this.getX() + (this.getRandom().nextDouble() - 0.5D) * 3.0D,
                                this.getY() + 1.0D + this.getRandom().nextDouble() * 2.0D,
                                this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 3.0D,
                                inward.x * 0.20D, 0.05D, inward.z * 0.20D);
                    }
                } else if (distance <= REACH && --this.grip <= 0) {
                    // PULL -- the tentacles, at the length of the arms
                    this.grip = 40 + this.getRandom().nextInt(30);
                    double strength = 0.55D - Math.min(0.30D, distance * 0.04D);
                    target.push(inward.x * strength, 0.22D, inward.z * strength);
                    target.hurtMarked = true;
                    if (level instanceof ServerLevel server) {
                        target.hurtServer(server, server.damageSources().generic(), 4.0F);
                    }
                    level.playSound(null, this.getX(), this.getY(), this.getZ(),
                            McsmSounds.MASSG_WHISPER, SoundSource.HOSTILE, 1.6F, 0.8F);
                    for (int i = 0; i < 18; i++) {
                        level.addParticle(ParticleTypes.PORTAL,
                                this.getX() + (this.getRandom().nextDouble() - 0.5D) * 2.0D,
                                this.getY() + this.getRandom().nextDouble() * 3.0D,
                                this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 2.0D,
                                inward.x * -0.15D, 0.10D, inward.z * -0.15D);
                    }
                }
            }
            // the tentacles breathe even when idle
            if (this.tickCount % 5 == 0) {
                this.level().addParticle(ParticleTypes.DUST_PLUME,
                        this.getX() + (this.getRandom().nextDouble() - 0.5D) * 2.4D,
                        this.getY() + this.getRandom().nextDouble() * 1.4D,
                        this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 2.4D,
                        0.0D, 0.004D, 0.0D);
            }
            if (--this.voice <= 0) {
                this.voice = 120 + this.getRandom().nextInt(220);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        McsmSounds.OBLIVION_DRONE, SoundSource.HOSTILE, 2.0F, 0.55F);
            }
        } catch (Throwable ignored) {
            // the lurker must never break the tick loop
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
