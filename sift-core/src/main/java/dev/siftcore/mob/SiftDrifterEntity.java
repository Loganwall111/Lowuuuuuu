package dev.siftcore.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A small free-flying Sift creature. It is a real living mob for future combat
 * hooks, but its first pass is intentionally non-colliding and non-hostile so
 * it can inhabit the free-fall atmosphere without becoming a wall or a trap.
 */
public final class SiftDrifterEntity extends FlyingEntity {
    private static final TrackedData<Float> SCALE = DataTracker.registerData(
            SiftDrifterEntity.class,
            TrackedDataHandlerRegistry.FLOAT
    );
    private static final TrackedData<Float> SEED = DataTracker.registerData(
            SiftDrifterEntity.class,
            TrackedDataHandlerRegistry.FLOAT
    );

    public SiftDrifterEntity(EntityType<? extends FlyingEntity> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.24D);
    }

    @Override
    protected void initGoals() {
        // The first atmospheric creature uses a deterministic flight impulse;
        // authored goals can be added later without changing the entity id.
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(SCALE, 0.85F);
        this.dataTracker.startTracking(SEED, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient()) {
            return;
        }

        if (this.age > 1200) {
            this.discard();
            return;
        }

        double time = this.age * 0.035D + this.getDrifterSeed() * 0.01D;
        Vec3d velocity = this.getVelocity();
        double desiredX = Math.sin(time * 1.7D) * 0.012D;
        double desiredY = Math.cos(time * 1.13D) * 0.009D;
        double desiredZ = Math.sin(time * 1.41D + this.getX() * 0.01D) * 0.012D;
        this.setVelocity(
                velocity.x * 0.965D + desiredX,
                velocity.y * 0.965D + desiredY,
                velocity.z * 0.965D + desiredZ
        );
        this.velocityModified = true;
        this.setBodyYaw(this.getBodyYaw() + 1.2F);
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    public float getDrifterScale() {
        return this.dataTracker.get(SCALE);
    }

    public void setDrifterScale(float scale) {
        this.dataTracker.set(SCALE, MathHelper.clamp(scale, 0.45F, 2.4F));
    }

    public float getDrifterSeed() {
        return this.dataTracker.get(SEED);
    }

    public void setDrifterSeed(float seed) {
        this.dataTracker.set(SEED, seed);
    }

    public float getPulse(float tickDelta) {
        return (float) (0.72D + 0.28D * Math.sin((this.age + tickDelta) * 0.16D + this.getDrifterSeed()));
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.setDrifterScale(nbt.getFloat("Scale"));
        this.setDrifterSeed(nbt.getFloat("Seed"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putFloat("Scale", this.getDrifterScale());
        nbt.putFloat("Seed", this.getDrifterSeed());
    }
}
