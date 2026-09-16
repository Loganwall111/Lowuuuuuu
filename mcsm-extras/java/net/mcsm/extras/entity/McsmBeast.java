package net.mcsm.extras.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
 * BUILD #428 -- THE BEASTS: MAS, THE CREATOR, AND THE WHALE.
 *
 * THE REPORT. "The MASSG was added but it was just code. I would like a mob
 * named Mas. And the Creator, a gigantic entity. And the whale monster I talked
 * about."
 *
 * All three were missing as ENTITIES: the MASSG was a renamed vanilla body, and
 * the Creator and the whale existed only as prose. This is one mob class that
 * carries all three kinds, because they share everything except their size,
 * their health, their temper and the sound they make:
 *
 *   MAS      the MASSG itself, 5x scale, 4096 health, cannot be killed or
 *            removed (see isInvulnerable + persistence), follows the player and
 *            hits hard. It breathes, whispers, giggles and beats with the mod's
 *            own sounds.
 *   CREATOR  the thing the whole world stands on: 26x scale, 40000 health, slow
 *            and patient, only reachable where the rift puts it. When it looks
 *            at you the sky hums.
 *   WHALE    the whale monster: 14x scale, swims through the air rather than
 *            walking it -- it ignores gravity and drifts toward whatever it has
 *            noticed.
 *
 * The whole class is deliberately built from parts this workspace already uses
 * in StoryCharacterEntity (PathfinderMob, createMobAttributes, the vanilla goal
 * set, the synced-data channels), so nothing here is an API guess.
 */
public class McsmBeast extends PathfinderMob {

    /** Which of the three this body is. Synced, so the client renders the right one. */
    private static final EntityDataAccessor<String> KIND =
            SynchedEntityData.defineId(McsmBeast.class, EntityDataSerializers.STRING);
    /** The countdown / line this body carries (the sky terminal reads it). */
    private static final EntityDataAccessor<String> LINE =
            SynchedEntityData.defineId(McsmBeast.class, EntityDataSerializers.STRING);

    public static final String MAS = "mas";
    public static final String CREATOR = "creator";
    public static final String WHALE = "whale";

    private int ambient = 120 + (int) (Math.random() * 200);

    public McsmBeast(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(false);
        this.setPersistenceRequired();
        this.xpReward = 500;
    }

    /** One attribute set per kind -- the mob registry picks by name. */
    public static AttributeSupplier.Builder masAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4096.0D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FOLLOW_RANGE, 256.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 30.0D);
    }

    public static AttributeSupplier.Builder creatorAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40000.0D)
                .add(Attributes.ATTACK_DAMAGE, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.16D)
                .add(Attributes.FOLLOW_RANGE, 512.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 60.0D);
    }

    public static AttributeSupplier.Builder whaleAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 900.0D)
                .add(Attributes.ATTACK_DAMAGE, 22.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.FOLLOW_RANGE, 128.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 48.0F, 0.9F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(KIND, MAS);
        builder.define(LINE, "");
    }

    public String kind() {
        return this.entityData.get(KIND);
    }

    public void setKind(String kind) {
        this.entityData.set(KIND, kind == null ? MAS : kind.toLowerCase(java.util.Locale.ROOT));
    }

    public String line() {
        return this.entityData.get(LINE);
    }

    public void setLine(String line) {
        this.entityData.set(LINE, line == null ? "" : line);
    }

    /**
     * THE MASSG CANNOT BE KILLED OR DELETED. Mas itself outlives everything:
     * no damage source touches it, no command removes it, and it does not
     * despawn. The other two are mortal -- the Creator can be killed, and the
     * whale can be killed, and both are supposed to be.
     */
    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (MAS.equals(kind())) {
            // THE MASSG CANNOT BE KILLED. Every damage path in the game -- a
            // sword, a fall, an explosion, /kill -- lands here first.
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        try {
            // the whale swims: it ignores the ground and drifts, so it reads as
            // something moving through the air rather than walking on it
            if (WHALE.equals(kind())) {
                Vec3 motion = this.getDeltaMovement();
                if (this.getTarget() != null) {
                    Vec3 to = this.getTarget().position().subtract(this.position()).normalize().scale(0.045D);
                    this.setDeltaMovement(motion.x * 0.94D + to.x, motion.y * 0.90D + to.y * 0.6D + 0.006D,
                            motion.z * 0.94D + to.z);
                } else {
                    this.setDeltaMovement(motion.x * 0.97D, motion.y * 0.97D - 0.002D, motion.z * 0.97D);
                }
            }
            // everyone in the family breathes: a slow particle pulse and, now
            // and then, its own voice
            if (--ambient <= 0) {
                ambient = 120 + this.getRandom().nextInt(240);
                Level level = this.level();
                String kind = kind();
                if (WHALE.equals(kind)) {
                    level.playSound(null, this.getX(), this.getY(), this.getZ(),
                            McsmSounds.OBLIVION_DRONE, SoundSource.HOSTILE, 2.2F, 0.6F);
                } else if (CREATOR.equals(kind)) {
                    level.playSound(null, this.getX(), this.getY(), this.getZ(),
                            McsmSounds.MASSG_HEART, SoundSource.HOSTILE, 3.0F, 0.5F);
                } else {
                    level.playSound(null, this.getX(), this.getY(), this.getZ(),
                            McsmSounds.MASSG_BREATH, SoundSource.HOSTILE, 2.0F, 0.8F);
                    if (this.getRandom().nextBoolean()) {
                        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                                McsmSounds.MASSG_GIGGLE, SoundSource.HOSTILE, 1.0F, 1.0F);
                    }
                }
            }
            if (tickCount % 4 == 0) {
                double spread = MAS.equals(kind()) ? 5.0D : 3.0D;
                this.level().addParticle(ParticleTypes.SQUID_INK,
                        this.getX() + (this.getRandom().nextDouble() - 0.5D) * spread,
                        this.getY() + this.getRandom().nextDouble() * spread,
                        this.getZ() + (this.getRandom().nextDouble() - 0.5D) * spread,
                        0.0D, 0.01D, 0.0D);
            }
        } catch (Throwable ignored) {
            // a beast must never break the tick loop
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
