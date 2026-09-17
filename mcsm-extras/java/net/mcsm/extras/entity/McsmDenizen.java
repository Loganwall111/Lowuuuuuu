package net.mcsm.extras.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
 * BUILD #460 -- EVERY WORLD HAS ITS OWN PEOPLE. The decayed reality's and the
 * infinite dimension's own creatures.
 *
 * <p>THE ASK, in the identity list: "own blocks, items, mobs, locks, VFX; no
 * re-use". Until now neither world had a mob of its own: the decayed reality sent
 * a roll from the STORM's bestiary at the player, and the chambers the infinite
 * dimension builds forever released that same roll ({@link
 * net.mcsm.extras.McsmCreatures#release}). Two worlds, one bestiary -- the same
 * complaint as the blocks, one layer up.
 *
 * <p>So each of them gets a body, and the bodies are not each other's:
 *
 * <ul>
 *   <li><b>{@link McsmDrifter} -- the decayed reality.</b> What the decay left
 *       standing: a hooded shape in rags, ash falling off it, and when it has you
 *       in sight it <b>flickers</b> -- a short burst of ground it should not be
 *       able to cover, a spray of ash, a glitch in its own voice.</li>
 *   <li><b>{@link McsmKeeper} -- the infinite dimension.</b> The one that keeps
 *       the chambers: tall, brimmed, lantern in hand, slow, and hard to put down.
 *       Its lamp throws pale motes as it walks, which is how a player sees one
 *       coming from across the endless field.</li>
 * </ul>
 *
 * <p>Both are built the way this workspace's other creatures are: a
 * {@code Monster} with the vanilla goal set, attributes through
 * {@code Monster.createMonsterAttributes()}, particles from the list the rest of
 * the mod already uses (ASH, DUST_PLUME), and the mod's own sounds. Nothing here
 * is an API guess, and every tick body is wrapped so a creature can never take the
 * server loop down with it.
 */
public abstract class McsmDenizen extends Monster {

    /** Which world this body belongs to -- {@link net.mcsm.extras.McsmIdentity}. */
    public static final String DECAYED = "decayed";
    public static final String ADAMS = "adams";

    private final String world;
    /** Ticks until the drifter's next flicker. */
    private int flicker = 80 + (int) (Math.random() * 120);
    /** Ticks until it speaks again. */
    private int voice = 120 + (int) (Math.random() * 240);

    protected McsmDenizen(EntityType<? extends Monster> type, Level level, String world) {
        super(type, level);
        this.world = world;
        this.setPersistenceRequired();
        this.setCanPickUpLoot(false);
    }

    /** The world this creature belongs to. */
    public String world() {
        return this.world;
    }

    // ------------------------------------------------------------------
    // Attributes: one set per world, and they do not feel the same to fight
    // ------------------------------------------------------------------
    /** The drifter: quick, thin, and it hits like something that fell on you. */
    public static AttributeSupplier.Builder drifterAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 26.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    /** The keeper: slower than you, and it does not care. */
    public static AttributeSupplier.Builder keeperAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90.0D)
                .add(Attributes.ATTACK_DAMAGE, 11.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ARMOR, 9.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 40.0F, 0.9F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        try {
            if (DECAYED.equals(this.world)) {
                drift();
            } else {
                keep();
            }
        } catch (Throwable ignored) {
            // a creature must never break the tick loop
        }
    }

    /** THE DRIFTER: ash comes off it, and it flickers the ground closed. */
    private void drift() {
        if (this.tickCount % 5 == 0) {
            this.level().addParticle(ParticleTypes.ASH,
                    this.getX() + (this.getRandom().nextDouble() - 0.5D) * 0.7D,
                    this.getY() + 0.4D + this.getRandom().nextDouble() * 1.6D,
                    this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 0.7D,
                    0.0D, -0.02D, 0.0D);
        }
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            double distance = this.distanceTo(target);
            if (distance > 7.0D && distance < 32.0D && --this.flicker <= 0) {
                this.flicker = 100 + this.getRandom().nextInt(120);
                Vec3 to = target.position().subtract(this.position()).normalize();
                this.setDeltaMovement(to.x * 1.15D, 0.22D, to.z * 1.15D);
                this.hurtMarked = true;
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        McsmSounds.OBLIVION_GLITCH, SoundSource.HOSTILE, 1.1F, 0.85F);
                for (int i = 0; i < 18; i++) {
                    this.level().addParticle(ParticleTypes.ASH,
                            this.getX() + (this.getRandom().nextDouble() - 0.5D) * 1.2D,
                            this.getY() + this.getRandom().nextDouble() * 1.9D,
                            this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 1.2D,
                            -to.x * 0.30D, -0.10D, -to.z * 0.30D);
                }
            }
        }
        if (--this.voice <= 0) {
            this.voice = 160 + this.getRandom().nextInt(300);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    McsmSounds.MASSG_WHISPER, SoundSource.HOSTILE, 0.8F,
                    0.75F + this.getRandom().nextFloat() * 0.2F);
        }
    }

    /** THE KEEPER: slow, lit, and its lamp throws motes as it walks. */
    private void keep() {
        if (this.tickCount % 4 == 0) {
            this.level().addParticle(ParticleTypes.DUST_PLUME,
                    this.getX() + (this.getRandom().nextDouble() - 0.5D) * 0.9D,
                    this.getY() + 1.2D + this.getRandom().nextDouble() * 1.4D,
                    this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 0.9D,
                    0.0D, 0.012D, 0.0D);
        }
        if (--this.voice <= 0) {
            this.voice = 200 + this.getRandom().nextInt(320);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    McsmSounds.MASSG_HEART, SoundSource.HOSTILE, 0.7F,
                    0.9F + this.getRandom().nextFloat() * 0.15F);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    /**
     * Raise {@code count} of one of the world's own at a spot, named, out of arm's
     * reach. The same call shape {@code McsmVoid.spawnDwellers} and
     * {@code McsmCreatures.spawnCreature} use: create, finalize with an explicit
     * null group data, name, persist, snap, add. Returns how many actually landed,
     * so a caller can fall back if the registry is not there.
     */
    public static int spawn(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.entity.EntityType<?> type,
            net.minecraft.core.BlockPos at, int count, String name) {
        if (type == null) {
            return 0;
        }
        int spawned = 0;
        try {
            net.minecraft.util.RandomSource rng = level.getRandom();
            for (int i = 0; i < count; i++) {
                net.minecraft.world.entity.Entity created = type.create(level,
                        net.minecraft.world.entity.EntitySpawnReason.EVENT);
                if (!(created instanceof net.minecraft.world.entity.Mob mob)) {
                    continue;
                }
                double angle = rng.nextDouble() * Math.PI * 2.0D;
                double distance = 11.0D + rng.nextInt(13);
                int x = at.getX() + (int) Math.round(Math.cos(angle) * distance);
                int z = at.getZ() + (int) Math.round(Math.sin(angle) * distance);
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(at),
                        net.minecraft.world.entity.EntitySpawnReason.EVENT,
                        (net.minecraft.world.entity.SpawnGroupData) null);
                mob.setCustomName(net.minecraft.network.chat.Component.literal(name));
                mob.setCustomNameVisible(false);
                mob.setPersistenceRequired();
                mob.snapTo(x + 0.5D, at.getY(), z + 0.5D, rng.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(mob);
                spawned++;
            }
        } catch (Throwable ignored) {
            // a world with nothing in it is still a world
        }
        return spawned;
    }

    /** THE DECAYED REALITY'S OWN. */
    public static class McsmDrifter extends McsmDenizen {
        public McsmDrifter(EntityType<? extends Monster> type, Level level) {
            super(type, level, DECAYED);
            this.xpReward = 25;
        }
    }

    /** THE INFINITE DIMENSION'S OWN. */
    public static class McsmKeeper extends McsmDenizen {
        public McsmKeeper(EntityType<? extends Monster> type, Level level) {
            super(type, level, ADAMS);
            this.xpReward = 60;
        }

        @Override
        public boolean fireImmune() {
            return true;
        }
    }
}
