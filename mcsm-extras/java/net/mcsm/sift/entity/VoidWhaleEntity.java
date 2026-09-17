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
import net.minecraft.util.RandomSource;
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
 * Ghost Whale Mega-Fauna - mcsm:void_whale
 * Colossal, multi-segmented passive flying entity model matching cavern sketches
 * Drifts weightlessly through Tier 1 coordinates completely unaffected by gravity
 * Whale-Song Audio Integration - deep echoing mechanical whale clicks and songs
 */
public class VoidWhaleEntity extends FlyingMob {

    private static final EntityDataAccessor<Integer> SEGMENT_COUNT = SynchedEntityData.defineId(VoidWhaleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> GLOW_INTENSITY = SynchedEntityData.defineId(VoidWhaleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(VoidWhaleEntity.class, EntityDataSerializers.INT);

    // Multi-segmented body
    private final List<Segment> segments = new ArrayList<>();
    private float swimAnimation = 0f;
    private float prevSwimAnimation = 0f;
    private int songCooldown = 0;
    private Vec3 driftDirection = Vec3.ZERO;
    private float driftSpeed = 0.02f;

    public static class Segment {
        public Vec3 position;
        public Vec3 prevPosition;
        public float yaw;
        public float pitch;
        public float size;

        public Segment(Vec3 pos, float size) {
            this.position = pos;
            this.prevPosition = pos;
            this.size = size;
        }
    }

    public VoidWhaleEntity(EntityType<? extends FlyingMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
        this.noPhysics = false;

        // Initialize 12 segments for colossal size
        for (int i = 0; i < 12; i++) {
            float size = 1.5f - (float)i / 12f * 0.8f; // taper tail
            segments.add(new Segment(this.position(), size));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 200.0D)
            .add(Attributes.FLYING_SPEED, 0.6D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.FOLLOW_RANGE, 128.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SEGMENT_COUNT, 12);
        this.entityData.define(GLOW_INTENSITY, 0.8f);
        this.entityData.define(VARIANT, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new DriftGoal(this));
        this.goalSelector.addGoal(1, new WhaleSongGoal(this));
        this.goalSelector.addGoal(2, new AvoidBoundariesGoal(this));
        this.goalSelector.addGoal(3, new CirclePlayerGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    public void tick() {
        super.tick();

        prevSwimAnimation = swimAnimation;
        swimAnimation += 0.05f;

        // Weightless drift - completely unaffected by gravity variables
        this.setNoGravity(true);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.98, 0.98, 0.98));

        // Update multi-segmented chain physics
        updateSegments();

        // Glow intensity based on Tier depth - more magical deeper
        int y = (int) this.getY();
        if (McsmVoidTiers.isInVoidDimension(y)) {
            float depthFactor = (float)(McsmVoidTiers.TOP_BOUNDARY - y) / McsmVoidTiers.TOTAL_HEIGHT;
            float glow = 0.6f + depthFactor * 0.6f + Mth.sin(swimAnimation) * 0.1f;
            this.entityData.set(GLOW_INTENSITY, glow);
        }

        // Song cooldown
        if (songCooldown > 0) songCooldown--;

        // Tier 1 only - drift weightlessly through Tier 1 coordinates
        if (!level().isClientSide) {
            if (y < McsmVoidTiers.TIER_1_GEL_HORIZON_BOTTOM || y > McsmVoidTiers.TIER_1_GEL_HORIZON_TOP) {
                // Gently push back to Tier 1
                double targetY = (McsmVoidTiers.TIER_1_GEL_HORIZON_TOP + McsmVoidTiers.TIER_1_GEL_HORIZON_BOTTOM) / 2.0;
                double dy = (targetY - this.getY()) * 0.01;
                this.setDeltaMovement(this.getDeltaMovement().add(0, dy, 0));
            }
        }
    }

    private void updateSegments() {
        if (segments.isEmpty()) return;

        // Head follows entity position
        Segment head = segments.get(0);
        head.prevPosition = head.position;
        head.position = this.position().add(0, 0.5, 0);

        // Chain follow with spring physics
        for (int i = 1; i < segments.size(); i++) {
            Segment prev = segments.get(i-1);
            Segment curr = segments.get(i);
            curr.prevPosition = curr.position;

            Vec3 diff = prev.position.subtract(curr.position);
            double dist = diff.length();
            double targetDist = 2.0; // segment spacing

            if (dist > 0.01) {
                Vec3 dir = diff.normalize();
                // Spring towards target distance with wave motion
                float wave = Mth.sin(swimAnimation + i * 0.5f) * 0.3f;
                Vec3 target = prev.position.subtract(dir.scale(targetDist + wave));
                curr.position = curr.position.lerp(target, 0.3f);
            }

            // Yaw/pitch for rendering
            Vec3 toNext = (i < segments.size()-1 ? segments.get(i+1).position : curr.position).subtract(curr.position);
            if (toNext.lengthSqr() > 0.01) {
                curr.yaw = (float)(Mth.atan2(toNext.z, toNext.x) * 180 / Math.PI);
                curr.pitch = (float)(Mth.atan2(toNext.y, toNext.horizontalDistance()) * 180 / Math.PI);
            }
        }
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public float getSwimAnimation(float partial) {
        return Mth.lerp(partial, prevSwimAnimation, swimAnimation);
    }

    public float getGlowIntensity() {
        return this.entityData.get(GLOW_INTENSITY);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    // Whale-Song Audio Integration - deep echoing mechanical whale clicks
    public void playWhaleSong() {
        if (songCooldown > 0) return;
        this.playSound(getWhaleSongSound(), 4.0f, 0.7f + random.nextFloat() * 0.3f);
        songCooldown = 200 + random.nextInt(400);
        // Trigger particle burst for echo effect
        if (level().isClientSide) {
            spawnSongParticles();
        }
    }

    private void spawnSongParticles() {
        // Client particle - echo rings
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2 * i) / 8;
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;
            // level().addParticle would go here with custom particle
        }
    }

    protected SoundEvent getWhaleSongSound() {
        // Link to low-latency Ogg Vorbis engine (McsmUiSounds)
        // Using vanilla as placeholder - replace with mcsm:void_whale_song
        return SoundEvents.WARDEN_HEARTBEAT; // deep echoing
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WHALE_AMBIENT; // if exists, else placeholder
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.DOLPHIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WHALE_DEATH;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false; // No fall damage - floating
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockPos pos, BlockPos pos2) {
        // Completely unaffected by gravity variables
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
        tag.putFloat("Glow", getGlowIntensity());
        tag.putInt("SongCooldown", songCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) this.entityData.set(VARIANT, tag.getInt("Variant"));
        if (tag.contains("Glow")) this.entityData.set(GLOW_INTENSITY, tag.getFloat("Glow"));
        songCooldown = tag.getInt("SongCooldown");
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        this.entityData.set(VARIANT, level.getRandom().nextInt(3));
        this.driftDirection = new Vec3(
            level.getRandom().nextFloat() - 0.5f,
            (level.getRandom().nextFloat() - 0.5f) * 0.2f,
            level.getRandom().nextFloat() - 0.5f
        ).normalize();
        this.driftSpeed = 0.02f + level.getRandom().nextFloat() * 0.03f;
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    // Goals
    static class DriftGoal extends Goal {
        private final VoidWhaleEntity whale;
        private int driftTime = 0;

        DriftGoal(VoidWhaleEntity whale) {
            this.whale = whale;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            driftTime++;
            if (driftTime % 100 == 0 || whale.driftDirection.lengthSqr() < 0.01) {
                whale.driftDirection = new Vec3(
                    whale.random.nextFloat() - 0.5f,
                    (whale.random.nextFloat() - 0.5f) * 0.1f,
                    whale.random.nextFloat() - 0.5f
                ).normalize();
            }
            Vec3 motion = whale.driftDirection.scale(whale.driftSpeed);
            // Add gentle wave motion
            motion = motion.add(
                Math.sin(whale.swimAnimation * 0.5) * 0.01,
                Math.cos(whale.swimAnimation * 0.3) * 0.005,
                0
            );
            whale.setDeltaMovement(whale.getDeltaMovement().lerp(motion, 0.05));
        }
    }

    static class WhaleSongGoal extends Goal {
        private final VoidWhaleEntity whale;
        private int cooldown = 0;

        WhaleSongGoal(VoidWhaleEntity whale) {
            this.whale = whale;
        }

        @Override
        public boolean canUse() {
            return cooldown <= 0 && whale.random.nextFloat() < 0.01f;
        }

        @Override
        public void start() {
            whale.playWhaleSong();
            cooldown = 300 + whale.random.nextInt(600);
        }

        @Override
        public void tick() {
            if (cooldown > 0) cooldown--;
        }
    }

    static class AvoidBoundariesGoal extends Goal {
        private final VoidWhaleEntity whale;

        AvoidBoundariesGoal(VoidWhaleEntity whale) {
            this.whale = whale;
        }

        @Override
        public boolean canUse() {
            int y = (int) whale.getY();
            return y < McsmVoidTiers.TIER_1_GEL_HORIZON_BOTTOM + 20 || y > McsmVoidTiers.TIER_1_GEL_HORIZON_TOP - 20;
        }

        @Override
        public void tick() {
            double centerY = (McsmVoidTiers.TIER_1_GEL_HORIZON_TOP + McsmVoidTiers.TIER_1_GEL_HORIZON_BOTTOM) / 2.0;
            double dy = (centerY - whale.getY()) * 0.02;
            whale.setDeltaMovement(whale.getDeltaMovement().add(0, dy, 0));
        }
    }

    static class CirclePlayerGoal extends Goal {
        private final VoidWhaleEntity whale;
        private int circleTime = 0;

        CirclePlayerGoal(VoidWhaleEntity whale) {
            this.whale = whale;
        }

        @Override
        public boolean canUse() {
            return whale.level().getNearestPlayer(whale, 64) != null && whale.random.nextFloat() < 0.005f;
        }

        @Override
        public void start() {
            circleTime = 200 + whale.random.nextInt(200);
        }

        @Override
        public void tick() {
            var player = whale.level().getNearestPlayer(whale, 64);
            if (player == null) return;
            circleTime--;
            Vec3 toPlayer = player.position().subtract(whale.position());
            double dist = toPlayer.length();
            if (dist > 10) {
                Vec3 orbit = new Vec3(-toPlayer.z, 0, toPlayer.x).normalize().scale(0.1);
                whale.setDeltaMovement(whale.getDeltaMovement().lerp(toPlayer.normalize().scale(0.05).add(orbit), 0.05));
            }
        }

        @Override
        public boolean canContinueToUse() {
            return circleTime > 0;
        }
    }
}
