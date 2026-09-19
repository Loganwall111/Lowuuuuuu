package net.mcsm.sift.entity.rift;

import net.mcsm.sift.McsmVoidTiers;
import net.mcsm.sift.client.SiftRiftRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Reality Rift Entity - gigantic shader, not actual block, dramatic opening animation
 * Basically open worlds and like they feel them like every been really really strange at occasions they just opened randomly
 * They take it to the void dim
 */
public class SiftRiftEntity extends Entity {

    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(SiftRiftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(SiftRiftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> OPEN_PROGRESS = SynchedEntityData.defineId(SiftRiftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_OPENING = SynchedEntityData.defineId(SiftRiftEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RIFT_TYPE = SynchedEntityData.defineId(SiftRiftEntity.class, EntityDataSerializers.INT);

    public enum RiftType {
        COSMIC_WINDOW(0, "cosmic_window", 0xFFAA55FF),
        GEL_PORTAL(1, "gel_portal", 0xFF55FFAA),
        VOID_TEAR(2, "void_tear", 0xFFFF55AA);

        public final int id;
        public final String name;
        public final int color;

        RiftType(int id, String name, int color) {
            this.id = id;
            this.name = name;
            this.color = color;
        }
    }

    private float wavePhase = 0f;
    private float cosmicOffset = 0f;
    private int age = 0;
    private static final int OPEN_TIME = 60; // 3 seconds dramatic opening
    private static final int LIFETIME = 600; // 30 seconds total

    public SiftRiftEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(WIDTH, 8f);
        this.entityData.define(HEIGHT, 12f);
        this.entityData.define(OPEN_PROGRESS, 0f);
        this.entityData.define(IS_OPENING, true);
        this.entityData.define(RIFT_TYPE, 0);
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        wavePhase += 0.02f;
        cosmicOffset += 0.005f;

        if (level().isClientSide) {
            // Update renderer
            if (age % 5 == 0) {
                SiftRiftRenderer.INSTANCE.spawnRift(this.position(), getWidth(), this.random.nextLong());
            }
        }

        // Dramatic opening animation - from tiny slit to full rift
        if (isOpening()) {
            float progress = (float) age / OPEN_TIME;
            progress = 1f - (1f - progress) * (1f - progress); // ease out cubic
            progress = Mth.clamp(progress, 0f, 1f);
            this.entityData.set(OPEN_PROGRESS, progress);

            if (progress >= 1f) {
                this.entityData.set(IS_OPENING, false);
                // Play opening sound
                if (!level().isClientSide) {
                    level().playSound(null, blockPosition(), 
                        net.minecraft.sounds.SoundEvents.PORTAL_AMBIENT, 
                        net.minecraft.sounds.SoundSource.AMBIENT, 2f, 0.5f);
                }
            }
        } else {
            // Wavy spacetime - liquid-like wave animation loop
            // Check if should close
            if (age > LIFETIME) {
                // Close animation
                float closeProgress = 1f - (float)(age - LIFETIME) / 40f;
                this.entityData.set(OPEN_PROGRESS, Mth.clamp(closeProgress, 0f, 1f));
                if (closeProgress <= 0f) {
                    this.discard();
                }
            }
        }

        // Teleport entities that touch rift to void dimension - take it to the void dim
        if (!level().isClientSide && getOpenProgress() > 0.8f) {
            var entities = level().getEntities(this, this.getBoundingBox().inflate(1.0));
            for (var e : entities) {
                if (e instanceof net.minecraft.world.entity.player.Player player) {
                    // Only if player is not already in Sift
                    if (player.getY() > McsmVoidTiers.TOP_BOUNDARY) {
                        // Teleport to Sift - instantly fall down right below everywhere
                        player.teleportTo(player.getX(), McsmVoidTiers.TOP_BOUNDARY - 10, player.getZ());
                        player.setDeltaMovement(new Vec3(0, -0.8, 0));
                        player.playSound(net.minecraft.sounds.SoundEvents.PORTAL_TRAVEL, 1f, 0.5f);
                    }
                }
            }
        }
    }

    public float getWidth() {
        return this.entityData.get(WIDTH);
    }

    public float getHeight() {
        return this.entityData.get(HEIGHT);
    }

    public float getOpenProgress() {
        return this.entityData.get(OPEN_PROGRESS);
    }

    public boolean isOpening() {
        return this.entityData.get(IS_OPENING);
    }

    public RiftType getRiftType() {
        int id = this.entityData.get(RIFT_TYPE);
        return RiftType.values()[Mth.clamp(id, 0, RiftType.values().length - 1)];
    }

    public float getWavePhase() {
        return wavePhase;
    }

    public float getCosmicOffset() {
        return cosmicOffset;
    }

    public void setSize(float w, float h) {
        this.entityData.set(WIDTH, w);
        this.entityData.set(HEIGHT, h);
        // Update bounding box
        this.setBoundingBox(this.getBoundingBox().inflate(w/2, h/2, 0.5));
    }

    public void setRiftType(RiftType type) {
        this.entityData.set(RIFT_TYPE, type.id);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Width")) this.entityData.set(WIDTH, tag.getFloat("Width"));
        if (tag.contains("Height")) this.entityData.set(HEIGHT, tag.getFloat("Height"));
        if (tag.contains("RiftType")) this.entityData.set(RIFT_TYPE, tag.getInt("RiftType"));
        age = tag.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Width", getWidth());
        tag.putFloat("Height", getHeight());
        tag.putInt("RiftType", getRiftType().id);
        tag.putInt("Age", age);
    }

    @Override
    public boolean isPickable() {
        return false; // shader, not actual block
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
