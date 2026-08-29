package net.dabicco.witherstormmod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * RealityRiftEntity — A pixelated rift in reality.
 * Teleports players when they get close.
 */
public class RealityRiftEntity extends Entity {

    private static final EntityDataAccessor<Float> RIFT_SIZE =
        SynchedEntityData.defineId(RealityRiftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> RIFT_AGE =
        SynchedEntityData.defineId(RealityRiftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_ACTIVE =
        SynchedEntityData.defineId(RealityRiftEntity.class, EntityDataSerializers.BOOLEAN);

    private int teleportCooldown = 0;

    public RealityRiftEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RIFT_SIZE, 1.0f);
        builder.define(RIFT_AGE, 0);
        builder.define(IS_ACTIVE, true);
    }

    @Override
    public void tick() {
        super.tick();

        int age = this.entityData.get(RIFT_AGE);
        this.entityData.set(RIFT_AGE, age + 1);

        float size = this.entityData.get(RIFT_SIZE);
        if (size < 8.0f) {
            this.entityData.set(RIFT_SIZE, Math.min(8.0f, size + 0.005f));
        }

        if (teleportCooldown > 0) {
            teleportCooldown--;
        }

        if (!this.level().isClientSide() && teleportCooldown <= 0) {
            var players = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(3),
                p -> true
            );

            for (Player player : players) {
                if (player.distanceTo(this) < 3) {
                    double newX = this.position().x + (this.random.nextDouble() - 0.5) * 200;
                    double newZ = this.position().z + (this.random.nextDouble() - 0.5) * 200;
                    double newY = this.level().getHeightmapPos(
                        net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                        new net.minecraft.core.BlockPos((int)newX, 0, (int)newZ)
                    ).getY();
                    player.teleportTo(newX, newY, newZ);
                    teleportCooldown = 100;
                    break;
                }
            }
        }
    }

    public float getRiftSize() {
        return this.entityData.get(RIFT_SIZE);
    }

    public int getRiftAge() {
        return this.entityData.get(RIFT_AGE);
    }

    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("RiftSize")) {
            this.entityData.set(RIFT_SIZE, tag.getFloat("RiftSize"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("RiftSize", this.entityData.get(RIFT_SIZE));
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
