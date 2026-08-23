package net.dabicco.devouringstorms.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SuperTntEntity extends Entity {
   public static final float POWER = 40.0F;
   private static final int DEFAULT_FUSE = 80;
   private static final EntityDataAccessor<Integer> DATA_FUSE;
   private LivingEntity owner;

   public SuperTntEntity(EntityType<? extends SuperTntEntity> type, Level level) {
      super(type, level);
      this.blocksBuilding = true;
   }

   public SuperTntEntity(Level level, double x, double y, double z, LivingEntity igniter) {
      this(ModEntityTypes.SUPER_TNT, level);
      this.setPos(x, y, z);
      double a = level.getRandom().nextDouble() * (Math.PI * 2D);
      this.setDeltaMovement(-Math.sin(a) * 0.02, 0.2, -Math.cos(a) * 0.02);
      this.setFuse(80);
      this.xo = x;
      this.yo = y;
      this.zo = z;
      this.owner = igniter;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(DATA_FUSE, 80);
   }

   public void setFuse(int fuse) {
      this.entityData.set(DATA_FUSE, fuse);
   }

   public int getFuse() {
      return (Integer)this.entityData.get(DATA_FUSE);
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   public boolean isAlwaysTicking() {
      return true;
   }

   protected double getDefaultGravity() {
      return 0.04;
   }

   public boolean isCurrentlyGlowing() {
      return false;
   }

   public void tick() {
      this.handlePortal();
      this.applyGravity();
      this.move(MoverType.SELF, this.getDeltaMovement());
      this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
      if (this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, (double)-0.5F, 0.7));
      }

      int fuse = this.getFuse() - 1;
      this.setFuse(fuse);
      if (fuse <= 0) {
         this.discard();
         if (!this.level().isClientSide()) {
            this.explode();
         }
      } else if (this.level().isClientSide()) {
         this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + (double)0.5F, this.getZ(), (double)0.0F, (double)0.0F, (double)0.0F);
      }

   }

   private void explode() {
      this.level().explode(this, this.getX(), this.getY((double)0.0625F), this.getZ(), 40.0F, ExplosionInteraction.TNT);
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

   public float getFlash(float partialTick) {
      int fuse = this.getFuse();
      return fuse < 10 ? 1.0F - ((float)fuse - partialTick) / 10.0F : 0.0F;
   }

   protected void readAdditionalSaveData(ValueInput input) {
      this.setFuse(input.getShortOr("fuse", (short)80));
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      output.putShort("fuse", (short)this.getFuse());
   }

   static {
      DATA_FUSE = SynchedEntityData.defineId(SuperTntEntity.class, EntityDataSerializers.INT);
   }
}
