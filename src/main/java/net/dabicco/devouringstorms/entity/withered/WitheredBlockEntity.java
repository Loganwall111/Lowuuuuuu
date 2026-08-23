package net.dabicco.devouringstorms.entity.withered;

import java.util.Iterator;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class WitheredBlockEntity extends Entity {
   private static final EntityDataAccessor<Integer> BLOCK_ID = SynchedEntityData.defineId(WitheredBlockEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(WitheredBlockEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> FLUNG = SynchedEntityData.defineId(WitheredBlockEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> SOLID = SynchedEntityData.defineId(WitheredBlockEntity.class, EntityDataSerializers.BOOLEAN);
   private int life;
   private static final int FLUNG_LIFETIME = 100;
   private static final float IMPACT_DAMAGE = 5.0F;

   public WitheredBlockEntity(EntityType<? extends WitheredBlockEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(BLOCK_ID, Block.getId(Blocks.STONE.defaultBlockState()));
      builder.define(OWNER_ID, -1);
      builder.define(FLUNG, false);
      builder.define(SOLID, false);
   }

   public void setSolid(boolean solid) {
      this.entityData.set(SOLID, solid);
   }

   public boolean isSolid() {
      return (Boolean)this.entityData.get(SOLID);
   }

   public boolean canBeCollidedWith(Entity other) {
      return this.isSolid() && !this.isFlung();
   }

   public boolean isPushable() {
      return false;
   }

   public void setBlockState(BlockState state) {
      this.entityData.set(BLOCK_ID, Block.getId(state));
   }

   public BlockState getBlockState() {
      return Block.stateById((Integer)this.entityData.get(BLOCK_ID));
   }

   public void setOwnerId(int id) {
      this.entityData.set(OWNER_ID, id);
   }

   public int getOwnerId() {
      return (Integer)this.entityData.get(OWNER_ID);
   }

   public boolean isFlung() {
      return (Boolean)this.entityData.get(FLUNG);
   }

   public boolean isNoGravity() {
      return true;
   }

   public boolean isPickable() {
      return this.isSolid() && !this.isFlung();
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      if (this.isSolid() && !this.isFlung() && source.getEntity() != null) {
         this.shatter(level);
         return true;
      } else {
         return false;
      }
   }

   public void fling(Vec3 velocity) {
      this.entityData.set(FLUNG, true);
      this.setDeltaMovement(velocity);
      this.life = 0;
      this.hurtMarked = true;
      this.level()
         .playSound(
            null,
            this.getX(),
            this.getY(),
            this.getZ(),
            (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(),
            SoundSource.HOSTILE,
            0.35F,
            1.6F + this.random.nextFloat() * 0.3F
         );
   }

   public void moveToHeld(Vec3 pos) {
      this.setPos(pos.x, pos.y, pos.z);
      this.setDeltaMovement(Vec3.ZERO);
   }

   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel server) {
         if (!this.isFlung()) {
            if (this.level().getEntity(this.getOwnerId()) == null) {
               this.shatter(server);
            }
         } else {
            this.life++;
            Vec3 vel = this.getDeltaMovement();
            Vec3 next = this.position().add(vel);
            this.setPos(next.x, next.y, next.z);
            this.setDeltaMovement(vel.scale(0.99).subtract(0.0, 0.035, 0.0));
            Entity owner = this.level().getEntity(this.getOwnerId());
            Iterator var5 = server.getEntities(this, this.getBoundingBox().inflate(0.6), e -> e instanceof LivingEntity && e.isAlive() && e != owner)
               .iterator();
            if (var5.hasNext()) {
               Entity hit = (Entity)var5.next();
               ((LivingEntity)hit).hurtServer(server, this.damageSources().thrown(this, owner), 5.0F);
               hit.setDeltaMovement(hit.getDeltaMovement().add(vel.scale(0.28)));
               hit.hurtMarked = true;
               this.shatter(server);
            } else {
               if (!this.level().getBlockState(this.blockPosition()).isAir() || this.life > 100) {
                  this.shatter(server);
               }
            }
         }
      }
   }

   private void shatter(ServerLevel server) {
      BlockState state = this.getBlockState();
      if (!state.isAir()) {
         server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), this.getX(), this.getY() + 0.5, this.getZ(), 28, 0.35, 0.35, 0.35, 0.12);
      }

      server.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 1.0F, 0.7F + this.random.nextFloat() * 0.3F);
      this.discard();
   }

   protected void readAdditionalSaveData(ValueInput input) {
      this.entityData.set(BLOCK_ID, input.getIntOr("BlockId", Block.getId(Blocks.STONE.defaultBlockState())));
      this.entityData.set(FLUNG, input.getBooleanOr("Flung", false));
      this.life = input.getIntOr("Life", 0);
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      output.putInt("BlockId", (Integer)this.entityData.get(BLOCK_ID));
      output.putBoolean("Flung", (Boolean)this.entityData.get(FLUNG));
      output.putInt("Life", this.life);
   }
}
