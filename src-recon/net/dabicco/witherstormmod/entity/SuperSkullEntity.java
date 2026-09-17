package net.dabicco.witherstormmod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class SuperSkullEntity extends Entity {
   private static final float EXPLOSION_POWER = 4.2F;
   private static final int MAX_LIFETIME = 120;
   private static final int STRAIGHT_FLIGHT_TICKS = 25;
   private static final double ARC_GRAVITY = 0.012;
   private static final EntityDataAccessor<Boolean> EXTINGUISHED = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.SuperSkullEntity.class, EntityDataSerializers.BOOLEAN
   );
   private boolean waterDoused = false;

   public SuperSkullEntity(EntityType<? extends net.dabicco.witherstormmod.entity.SuperSkullEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(EXTINGUISHED, false);
   }

   public boolean isExtinguished() {
      return (Boolean)(Object)(Object)this.entityData.get(EXTINGUISHED);
   }

   public boolean isNoGravity() {
      return true;
   }

   public boolean isPickable() {
      return false;
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

   public void shoot(Vec3 velocity) {
      this.setDeltaMovement(velocity);
      this.faceVelocity();
   }

   private void faceVelocity() {
      Vec3 v = this.getDeltaMovement();
      if (!(v.lengthSqr() < 1.0E-6)) {
         double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
         this.setYRot((float)(Mth.atan2(v.z, v.x) * (180.0 / Math.PI)) - 90.0F);
         this.setXRot((float)(-(Mth.atan2(v.y, horiz) * (180.0 / Math.PI))));
      }
   }

   public void tick() {
      super.tick();
      this.yRotO = this.getYRot();
      this.xRotO = this.getXRot();
      this.faceVelocity();
      if (this.level().isClientSide()) {
         this.setPos(this.position().add(this.getDeltaMovement()));
      } else {
         Vec3 from = this.position();
         if (!this.waterDoused && this.isInWater()) {
            this.waterDoused = true;
            this.entityData.set(EXTINGUISHED, true);
         } else if (!this.isExtinguished() && this.tickCount % 5 == 0 && this.level().isRainingAt(this.blockPosition()) && this.random.nextInt(12) == 0) {
            this.entityData.set(EXTINGUISHED, true);
         }

         if (this.waterDoused) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.82));
            if (this.level() instanceof ServerLevel server) {
               server.sendParticles(ParticleTypes.CLOUD, from.x, from.y, from.z, 2, 0.2, 0.2, 0.2, 0.01);
            }

            if (this.getDeltaMovement().length() < 0.05) {
               this.level().explode(this, from.x, from.y, from.z, 1.2F, false, ExplosionInteraction.MOB);
               this.discard();
            } else {
               this.setPos(from.add(this.getDeltaMovement()));
            }
         } else {
            if (this.tickCount > 25) {
               this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.012, 0.0));
            }

            Vec3 to = from.add(this.getDeltaMovement());
            if (this.level() instanceof ServerLevel server) {
               if (this.isExtinguished()) {
                  server.sendParticles(ParticleTypes.LARGE_SMOKE, from.x, from.y, from.z, 3, 0.25, 0.25, 0.25, 0.012);
               } else {
                  server.sendParticles(ParticleTypes.FLAME, from.x, from.y, from.z, 3, 0.25, 0.25, 0.25, 0.015);
                  if (this.tickCount % 3 == 0) {
                     server.sendParticles(ParticleTypes.LARGE_SMOKE, from.x, from.y, from.z, 1, 0.15, 0.15, 0.15, 0.01);
                  }
               }
            }

            BlockHitResult hit = this.level().clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, this));
            if (hit.getType() != Type.MISS) {
               this.explode(hit.getLocation());
            } else {
               this.setPos(to);
               if (this.tickCount > 120) {
                  this.explode(this.position());
               }
            }
         }
      }
   }

   private void explode(Vec3 at) {
      this.level().explode(this, at.x, at.y, at.z, 4.2F, !this.isExtinguished(), ExplosionInteraction.MOB);
      this.discard();
   }

   protected void readAdditionalSaveData(ValueInput input) {
   }

   protected void addAdditionalSaveData(ValueOutput output) {
   }
}
