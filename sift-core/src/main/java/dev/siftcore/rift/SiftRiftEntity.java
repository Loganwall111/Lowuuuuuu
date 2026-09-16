package dev.siftcore.rift;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** A visual-only spacetime aperture. It has no collision or gameplay hitbox. */
public final class SiftRiftEntity extends Entity {
    private static final TrackedData<Float> SCALE = DataTracker.registerData(
            SiftRiftEntity.class,
            TrackedDataHandlerRegistry.FLOAT
    );
    private static final TrackedData<Float> SEED = DataTracker.registerData(
            SiftRiftEntity.class,
            TrackedDataHandlerRegistry.FLOAT
    );

    public SiftRiftEntity(EntityType<? extends SiftRiftEntity> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(SCALE, 3.0f);
        this.dataTracker.startTracking(SEED, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient() && this.age > 800) {
            this.discard();
        }
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
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    public float getRiftScale() {
        return this.dataTracker.get(SCALE);
    }

    public void setRiftScale(float scale) {
        this.dataTracker.set(SCALE, MathHelper.clamp(scale, 0.5f, 8.0f));
    }

    public float getRiftSeed() {
        return this.dataTracker.get(SEED);
    }

    public void setRiftSeed(float seed) {
        this.dataTracker.set(SEED, seed);
    }

    public float getPulse(float tickDelta) {
        return (float) (0.78D + 0.22D * Math.sin((this.age + tickDelta) * 0.12D + this.getRiftSeed()));
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.setRiftScale(nbt.getFloat("Scale"));
        this.setRiftSeed(nbt.getFloat("Seed"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putFloat("Scale", this.getRiftScale());
        nbt.putFloat("Seed", this.getRiftSeed());
    }
}
