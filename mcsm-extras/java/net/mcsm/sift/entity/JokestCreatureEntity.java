package net.mcsm.sift.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/**
 * Jokest creatures - small funny creatures in skybox panorama
 * Couple jokest creatures and fish-like creatures during skybox panorama
 * Small, playful, bounce around blue and pink grass blocks
 */
public class JokestCreatureEntity extends PathfinderMob {

    private static final EntityDataAccessor<Integer> JOKEST_TYPE = SynchedEntityData.defineId(JokestCreatureEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_BOUNCING = SynchedEntityData.defineId(JokestCreatureEntity.class, EntityDataSerializers.BOOLEAN);

    private float bounceAnim = 0f;
    private float prevBounceAnim = 0f;

    public enum JokestType {
        BLUE_BUN(0, "blue_bun", 0xFF55AAFF, "blue bun - cute blue mushroom creature"),
        PINK_PUFF(1, "pink_puff", 0xFFFF55AA, "pink puff - bounces on pink grass"),
        FLUOR_SPRITE(2, "fluor_sprite", 0xFF55FFAA, "fluor sprite - glowing plant creature"),
        RED_ROCKLING(3, "red_rockling", 0xFFFF5555, "red rockling - lives on red rock");

        public final int id;
        public final String name;
        public final int color;
        public final String desc;

        JokestType(int id, String name, int color, String desc) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.desc = desc;
        }
    }

    public JokestCreatureEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 10.0D)
            .add(Attributes.FLYING_SPEED, 1.2D)
            .add(Attributes.MOVEMENT_SPEED, 0.6D)
            .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(JOKEST_TYPE, 0);
        this.entityData.define(IS_BOUNCING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8f));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(3, new BounceOnGrassGoal(this));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    public void tick() {
        super.tick();
        prevBounceAnim = bounceAnim;
        bounceAnim += 0.2f;

        // Bouncy on blue and pink grass blocks
        if (onGround()) {
            this.entityData.set(IS_BOUNCING, true);
            // Bounce like on trampoline
            this.setDeltaMovement(this.getDeltaMovement().add(0, 0.3 + random.nextDouble() * 0.2, 0));
            this.hasImpulse = true;
            
            // Check grass type
            BlockPos below = blockPosition().below();
            var state = level().getBlockState(below);
            // Would check if blue_grass_block or pink_grass_block or red_grass_block
            if (level().isClientSide && random.nextFloat() < 0.3f) {
                level().addParticle(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    getX(), getY() + 0.5, getZ(),
                    0, 0.1, 0);
            }
        } else {
            this.entityData.set(IS_BOUNCING, false);
        }

        // Floating in skybox panorama - gentle bob
        double bob = Math.sin(bounceAnim) * 0.01;
        setDeltaMovement(getDeltaMovement().add(0, bob, 0));
    }

    public JokestType getJokestType() {
        int id = this.entityData.get(JOKEST_TYPE);
        return JokestType.values()[Mth.clamp(id, 0, JokestType.values().length - 1)];
    }

    public float getBounceAnim(float partial) {
        return Mth.lerp(partial, prevBounceAnim, bounceAnim);
    }

    public boolean isBouncing() {
        return this.entityData.get(IS_BOUNCING);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.AXOLOTL_IDLE_GROUND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AXOLOTL_HURT;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("JokestType", this.entityData.get(JOKEST_TYPE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("JokestType")) this.entityData.set(JOKEST_TYPE, tag.getInt("JokestType"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        this.entityData.set(JOKEST_TYPE, level.getRandom().nextInt(JokestType.values().length));
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    static class BounceOnGrassGoal extends Goal {
        private final JokestCreatureEntity jokest;
        BounceOnGrassGoal(JokestCreatureEntity jokest) { this.jokest = jokest; }
        @Override public boolean canUse() { return jokest.onGround() && jokest.random.nextFloat() < 0.2f; }
        @Override public void tick() {
            // Bounce to nearby grass
            BlockPos target = jokest.blockPosition().offset(
                jokest.random.nextInt(6) - 3,
                1,
                jokest.random.nextInt(6) - 3
            );
            jokest.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.5);
        }
    }
}
