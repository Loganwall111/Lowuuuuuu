package net.dabicco.witherstormmod.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class GrabTentacleEntity extends Entity {
   private static final EntityDataAccessor<Integer> STORM_ID = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.GrabTentacleEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> VICTIM_ID = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.GrabTentacleEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Boolean> GRABBED = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.GrabTentacleEntity.class, EntityDataSerializers.BOOLEAN
   );

   public GrabTentacleEntity(EntityType<? extends net.dabicco.witherstormmod.entity.GrabTentacleEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(STORM_ID, -1);
      builder.define(VICTIM_ID, -1);
      builder.define(GRABBED, false);
   }

   public void setStormId(int id) {
      this.entityData.set(STORM_ID, id);
   }

   public int getStormId() {
      return (Integer)(Object)this.entityData.get(STORM_ID);
   }

   public void setVictimId(int id) {
      this.entityData.set(VICTIM_ID, id);
   }

   public int getVictimId() {
      return (Integer)(Object)this.entityData.get(VICTIM_ID);
   }

   public void setGrabbed(boolean g) {
      this.entityData.set(GRABBED, g);
   }

   public boolean isGrabbed() {
      return (Boolean)(Object)this.entityData.get(GRABBED);
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

   private net.dabicco.witherstormmod.entity.WitherStormEntity storm() {
      net.dabicco.witherstormmod.entity.WitherStormEntity var10000;
      if (this.level().getEntity(this.getStormId()) instanceof net.dabicco.witherstormmod.entity.WitherStormEntity ws) {
         var10000 = ws;
      } else {
         var10000 = null;
      }

      return var10000;
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide() && this.storm() == null) {
         this.discard();
      }
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      net.dabicco.witherstormmod.entity.WitherStormEntity ws = this.storm();
      if (ws != null && source.getEntity() instanceof Player p) {
         ws.registerGrabHit(level, p);
      }

      return false;
   }

   protected void readAdditionalSaveData(ValueInput input) {
   }

   protected void addAdditionalSaveData(ValueOutput output) {
   }
}
