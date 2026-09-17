package net.mcsm.sift.entity;

import net.mcsm.sift.McsmVoidTiers;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Colossal Octopus - rainbow colored water with sparkles, colossal octopus
 * Huge octopus entity for Sift - rainbow tentacles, floats in iridescent gel
 */
public class ColossalOctopusEntity extends FlyingMob {

    private static final EntityDataAccessor<Float> TENTACLE_ANIM = SynchedEntityData.defineId(ColossalOctopusEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR_VARIANT = SynchedEntityData.defineId(ColossalOctopusEntity.class, EntityDataSerializers.INT);

    private float swimAnim = 0f;
    private float prevSwimAnim = 0f;
    private final List<Tentacle> tentacles = new ArrayList<>();

    public static class Tentacle {
        public Vec3 base;
        public Vec3[] segments = new Vec3[6];
        public float waveOffset;

        public Tentacle(Vec3 base, float offset) {
            this.base = base;
            this.waveOffset = offset;
            for (int i = 0; i < 6; i++) {
                segments[i] = base.add(0, -i * 0.8, 0);
            }
        }
    }

    public ColossalOctopusEntity(EntityType<? extends FlyingMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 15, true);
        this.setNoGravity(true);
        for (int i = 0; i < 8; i++) {
            float angle = (float)i / 8 * Mth.TWO_PI;
            Vec3 base = new Vec3(Mth.cos(angle) * 1.5, -0.5, Mth.sin(angle) * 1.5);
            tentacles.add(new Tentacle(base, i * 0.8f));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 300.0D)
            .add(Attributes.FLYING_SPEED, 0.4D)
            .add(Attributes.MOVEMENT_SPEED, 0.2D)
            .add(Attributes.ATTACK_DAMAGE, 15.0D)
            .add(Attributes.FOLLOW_RANGE, 64.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TENTACLE_ANIM, 0f);
        this.entityData.define(COLOR_VARIANT, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatTentacleGoal(this));
        this.goalSelector.addGoal(1, new RainbowSwimGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    public void tick() {
        super.tick();
        prevSwimAnim = swimAnim;
        swimAnim += 0.03f;
        this.entityData.set(TENTACLE_ANIM, swimAnim);

        // Update tentacles - rainbow colored water with sparkles - tentacles wave like in water
        for (Tentacle t : tentacles) {
            for (int i = 0; i < t.segments.length; i++) {
                float wave = Mth.sin(swimAnim + t.waveOffset + i * 0.5f) * (1f + i * 0.2f);
                float wave2 = Mth.cos(swimAnim * 0.7f + t.waveOffset + i * 0.3f) * 0.5f;
                Vec3 base = this.position().add(t.base);
                t.segments[i] = base.add(wave * 0.3, -i * 0.8 + wave2 * 0.2, wave2 * 0.3);
            }
        }

        // Keep in iridescent gel / black water / green acid
        int y = (int) this.getY();
        if (McsmVoidTiers.Tier.TIER_5_IRIDESCENT_GEL.contains(y) || y < -200) {
            // Happy in water
        } else {
            // Try to go down to water
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.02, 0));
        }

        // Sparkles in rainbow water
        if (level().isClientSide && random.nextFloat() < 0.1f) {
            level().addParticle(net.minecraft.core.particles.ParticleTypes.GLOW,
                getX() + (random.nextDouble() - 0.5) * 4,
                getY() + random.nextDouble() * 2,
                getZ() + (random.nextDouble() - 0.5) * 4,
                0, 0.05, 0);
        }
    }

    public List<Tentacle> getTentacles() {
        return tentacles;
    }

    public float getTentacleAnim(float partial) {
        return Mth.lerp(partial, prevSwimAnim, swimAnim);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SQUID_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SQUID_HURT;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ColorVariant", this.entityData.get(COLOR_VARIANT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ColorVariant")) this.entityData.set(COLOR_VARIANT, tag.getInt("ColorVariant"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        this.entityData.set(COLOR_VARIANT, level.getRandom().nextInt(4));
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    static class FloatTentacleGoal extends Goal {
        private final ColossalOctopusEntity octo;
        FloatTentacleGoal(ColossalOctopusEntity octo) { this.octo = octo; }
        @Override public boolean canUse() { return true; }
        @Override public void tick() {
            if (octo.random.nextFloat() < 0.05f) {
                Vec3 dir = new Vec3(octo.random.nextFloat() - 0.5, (octo.random.nextFloat() - 0.5) * 0.2, octo.random.nextFloat() - 0.5).normalize().scale(0.05);
                octo.setDeltaMovement(octo.getDeltaMovement().lerp(dir, 0.1));
            }
        }
    }

    static class RainbowSwimGoal extends Goal {
        private final ColossalOctopusEntity octo;
        RainbowSwimGoal(ColossalOctopusEntity octo) { this.octo = octo; }
        @Override public boolean canUse() { return octo.level().getNearestPlayer(octo, 32) != null; }
        @Override public void tick() {
            var player = octo.level().getNearestPlayer(octo, 32);
            if (player == null) return;
            Vec3 toPlayer = player.position().subtract(octo.position());
            if (toPlayer.length() > 8) {
                octo.setDeltaMovement(octo.getDeltaMovement().lerp(toPlayer.normalize().scale(0.03), 0.05));
            }
        }
    }
}
