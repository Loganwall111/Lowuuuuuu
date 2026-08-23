package net.dabicco.devouringstorms.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GrabTentacleEntity extends Entity {
   private static final EntityDataAccessor<Integer> STORM_ID;
   private static final EntityDataAccessor<Integer> VICTIM_ID;
   private static final EntityDataAccessor<Boolean> GRABBED;

   public GrabTentacleEntity(EntityType<? extends GrabTentacleEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(STORM_ID, -1);
      builder.define(VICTIM_ID, -1);
      builder.define(GRABBED, false);
   }

   public void setStormId(int id) {
      this.entityData.set(STORM_ID, id);
   }

   public int getStormId() {
      return (Integer)this.entityData.get(STORM_ID);
   }

   public void setVictimId(int id) {
      this.entityData.set(VICTIM_ID, id);
   }

   public int getVictimId() {
      return (Integer)this.entityData.get(VICTIM_ID);
   }

   public void setGrabbed(boolean g) {
      this.entityData.set(GRABBED, g);
   }

   public boolean isGrabbed() {
      return (Boolean)this.entityData.get(GRABBED);
   }

   public boolean isNoGravity() {
      return true;
   }

   public boolean isPickable() {
      return true;
   }

   public boolean isAttackable() {
      return true;
   }

   private WitherStormEntity storm() {
      Entity var2 = this.level().getEntity(this.getStormId());
      WitherStormEntity var10000;
      if (var2 instanceof WitherStormEntity ws) {
         var10000 = ws;
      } else {
         var10000 = null;
      }

      return var10000;
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide()) {
         if (this.storm() == null) {
            this.discard();
         }

      }
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      WitherStormEntity ws = this.storm();
      if (ws != null) {
         Entity var6 = source.getEntity();
         if (var6 instanceof Player) {
            Player p = (Player)var6;
            ws.registerGrabHit(level, p);
         }
      }

      return false;
   }

   protected void readAdditionalSaveData(ValueInput input) {
   }

   protected void addAdditionalSaveData(ValueOutput output) {
   }

   static {
      STORM_ID = SynchedEntityData.defineId(GrabTentacleEntity.class, EntityDataSerializers.INT);
      VICTIM_ID = SynchedEntityData.defineId(GrabTentacleEntity.class, EntityDataSerializers.INT);
      GRABBED = SynchedEntityData.defineId(GrabTentacleEntity.class, EntityDataSerializers.BOOLEAN);
   }
}
