package net.dabicco.witherstormmod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * CorruptionEntity — A glitching anomaly that warps reality around it.
 * When players get close, their screen distorts with visual glitches.
 */
public class CorruptionEntity extends Entity {

    private static final EntityDataAccessor<Float> GLITCH_INTENSITY =
        SynchedEntityData.defineId(CorruptionEntity.class, EntityDataSerializers.FLOAT);

    private float glitchRadius = 16.0f;

    public CorruptionEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(GLITCH_INTENSITY, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            var player = this.level().getNearestPlayer(this, glitchRadius);
            if (player != null) {
                float intensity = (float)(1.0 - (this.distanceTo(player) / glitchRadius));
                intensity = Math.max(0, Math.min(1, intensity));
                this.entityData.set(GLITCH_INTENSITY, intensity);
            } else {
                this.entityData.set(GLITCH_INTENSITY, 0.0f);
            }
        }
    }

    public float getGlitchIntensity() {
        return this.entityData.get(GLITCH_INTENSITY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("GlitchRadius")) {
            this.glitchRadius = tag.getFloat("GlitchRadius");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("GlitchRadius", glitchRadius);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
