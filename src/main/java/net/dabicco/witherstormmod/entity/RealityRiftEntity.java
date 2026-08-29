package net.dabicco.witherstormmod.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * RealityRiftEntity — A pixelated rift portal that tears through dimensions.
 */
public class RealityRiftEntity extends Entity {

    private static final EntityDataAccessor<Float> RIFT_SIZE =
        SynchedEntityData.defineId(RealityRiftEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Boolean> IS_OPEN =
        SynchedEntityData.defineId(RealityRiftEntity.class, EntityDataSerializers.BOOLEAN);

    private int lifetime = 0;
    private static final int MAX_LIFETIME = 6000; // 5 minutes

    public RealityRiftEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RIFT_SIZE, 3.0f);
        builder.define(IS_OPEN, true);
    }

    @Override
    public void tick() {
        super.tick();
        lifetime++;

        if (lifetime > MAX_LIFETIME) {
            this.entityData.set(IS_OPEN, false);
            if (this.level().isClientSide()) {
                // Spawn closing particles
            } else {
                this.discard();
            }
        }

        if (this.level().isClientSide() && this.entityData.get(IS_OPEN)) {
            // Pixelated rift visual effect handled by renderer
        }
    }

    public float getRiftSize() {
        return this.entityData.get(RIFT_SIZE);
    }

    public boolean isOpen() {
        return this.entityData.get(IS_OPEN);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.lifetime = input.getIntOr("Lifetime", 0);
        this.entityData.set(RIFT_SIZE, input.getFloatOr("RiftSize", 3.0f));
        this.entityData.set(IS_OPEN, input.getBooleanOr("IsOpen", true));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("Lifetime", lifetime);
        output.putFloat("RiftSize", this.entityData.get(RIFT_SIZE));
        output.putBoolean("IsOpen", this.entityData.get(IS_OPEN));
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }
}
