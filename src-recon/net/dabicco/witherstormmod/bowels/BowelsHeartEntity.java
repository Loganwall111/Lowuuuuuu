package net.dabicco.witherstormmod.bowels;

import java.util.UUID;
import net.dabicco.witherstormmod.BowelsFlip;
import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.network.CaveRumblePayload;
import net.dabicco.witherstormmod.network.CommandBlockPowerPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class BowelsHeartEntity extends Entity {
   public static final int HITS = 4;
   private static final EntityDataAccessor<Integer> CRACKS = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsHeartEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> FIGHT = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsHeartEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> HURT_TIME = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsHeartEntity.class, EntityDataSerializers.INT
   );
   private static final int HIT_COOLDOWN = 25;
   private static final double BLAST_STAGGER = 0.3;
   private static final double BLAST_RADIUS = 22.0;
   private int hitCooldown;
   private int stance = 0;
   private int stanceIn;
   private static final int STANCE_EVERY = 5;
   private int powerBackIn;
   private static final int POWER_RETURN_DELAY = 9;
   private boolean risen;
   private int wallSpawnAt;
   private int waveOwed;
   private int waveSpawned;
   private UUID slayer;
   private static final int ROLL_SHAKE = 98;

   public int getStance() {
      return this.stance;
   }

   public int getWallSpawnAt() {
      return this.wallSpawnAt;
   }

   public void setWallSpawnAt(int at) {
      this.wallSpawnAt = at;
   }

   public int getWaveOwed() {
      return this.waveOwed;
   }

   public int getWaveSpawned() {
      return this.waveSpawned;
   }

   public void queueWave(int size) {
      this.waveOwed = size;
      this.waveSpawned = 0;
   }

   public void oneMoreOut() {
      if (this.waveOwed > 0) {
         this.waveOwed--;
      }

      this.waveSpawned++;
   }

   public BowelsHeartEntity(EntityType<? extends net.dabicco.witherstormmod.bowels.BowelsHeartEntity> type, Level level) {
      super(type, level);
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(CRACKS, 0);
      builder.define(FIGHT, -1);
      builder.define(HURT_TIME, 0);
   }

   public int getCracks() {
      return (Integer)(Object)this.entityData.get(CRACKS);
   }

   public boolean isFighting() {
      return (Integer)(Object)this.entityData.get(FIGHT) >= 0;
   }

   public int getFightTicks() {
      return (Integer)(Object)this.entityData.get(FIGHT);
   }

   public int getHurtTime() {
      return (Integer)(Object)this.entityData.get(HURT_TIME);
   }

   public boolean hasRisen() {
      return this.risen;
   }

   public void setRisen() {
      this.risen = true;
   }

   public boolean isPickable() {
      return true;
   }

   public boolean isAttackable() {
      return true;
   }

   public boolean canBeCollidedWith(Entity by) {
      return true;
   }

   public boolean isPushable() {
      return false;
   }

   public void tick() {
      super.tick();
      Vec3 motion = this.getDeltaMovement();
      if (!this.onGround()) {
         motion = motion.add(0.0, -0.055, 0.0);
         this.setDeltaMovement(new Vec3(0.0, motion.y, 0.0));
         this.move(MoverType.SELF, this.getDeltaMovement());
      } else {
         this.setDeltaMovement(Vec3.ZERO);
      }

      BowelsFlip.set(this.getCracks() >= 3);
      if (!this.level().isClientSide()) {
         if (this.level() instanceof ServerLevel server && --this.stanceIn <= 0) {
            this.stanceIn = 5;
            int wantStance = net.dabicco.witherstormmod.bowels.BowelsBoss.guardStance(server, this);
            if (wantStance != this.stance) {
               this.stance = wantStance;

               for (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity limb : server.getEntitiesOfClass(
                  net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class,
                  this.getBoundingBox().inflate(24.0),
                  net.dabicco.witherstormmod.bowels.BowelsTentacleEntity::isGuard
               )) {
                  limb.setStance(wantStance, true);
               }
            }
         }

         if (this.hitCooldown > 0) {
            this.hitCooldown--;
         }

         if (this.powerBackIn > 0 && --this.powerBackIn == 0 && this.level() instanceof ServerLevel serverx) {
            for (ServerPlayer nearby : serverx.players()) {
               if (!(nearby.distanceToSqr(this) > 4096.0)) {
                  ServerPlayNetworking.send(nearby, new CommandBlockPowerPayload(this.getX(), this.getY(), this.getZ()));
               }
            }
         }

         if (this.getHurtTime() > 0) {
            this.entityData.set(HURT_TIME, this.getHurtTime() - 1);
         }

         if (this.isFighting()) {
            this.entityData.set(FIGHT, this.getFightTicks() + 1);
         }
      }
   }

   public UUID getSlayer() {
      return this.slayer;
   }

   public void clearHitGate() {
      this.hitCooldown = 0;
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      if (source.getEntity() instanceof LivingEntity attacker) {
         if (this.hitCooldown > 0) {
            return false;
         } else if (this.isFighting() && !net.dabicco.witherstormmod.bowels.BowelsBoss.waveCleared(level, this)) {
            level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.HOSTILE, 1.2F, 0.5F);
            level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.6, this.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
            if (attacker instanceof ServerPlayer sp) {
               sp.sendSystemMessage(Component.literal("It will not break while they still stand."), true);
            }

            this.hitCooldown = 8;
            return false;
         } else {
            this.hitCooldown = 25;
            if (attacker instanceof ServerPlayer sp) {
               this.slayer = sp.getUUID();
            }

            boolean first = !this.isFighting();
            this.entityData.set(CRACKS, Math.min(4, this.getCracks() + 1));
            this.entityData.set(HURT_TIME, 10);
            boolean last = this.getCracks() >= 4;
            level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), ModSounds.CB_HIT, SoundSource.HOSTILE, 1.0F, 1.0F);
            level.playSound(
               (Entity)null, this.getX(), this.getY(), this.getZ(), last ? ModSounds.CB_DAMAGE_FINAL : ModSounds.CB_DAMAGE, SoundSource.HOSTILE, 1.0F, 1.0F
            );
            level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), ModSounds.CB_UNPOWER, SoundSource.HOSTILE, 1.0F, 1.0F);
            if (last) {
               level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), ModSounds.CB_DESTRUCT, SoundSource.HOSTILE, 1.0F, 1.0F);
               level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 1.4F, 0.7F);
            } else {
               this.powerBackIn = 9;
            }

            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.5, this.getZ(), 30, 0.4, 0.4, 0.4, 0.25);
            if (first) {
               this.begin(level);
            }

            if (this.getCracks() == 3) {
               this.rollOver(level);
            }

            if (this.getCracks() >= 4) {
               net.dabicco.witherstormmod.bowels.BowelsBoss.finish(level, this);
               return true;
            } else {
               net.dabicco.witherstormmod.bowels.BowelsBoss.onHitSurvived(level, this);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private void rollOver(ServerLevel level) {
      BowelsFlip.startRoll(level.getGameTime());

      for (ServerPlayer player : level.players()) {
         ServerPlayNetworking.send(player, new CaveRumblePayload(98, 1.4F));
      }

      level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 4.0F, 0.32F);
      level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 0.5F);
      net.dabicco.witherstormmod.bowels.BowelsBoss.onRoomRolled(level, this);
   }

   private void begin(ServerLevel level) {
      this.entityData.set(FIGHT, 0);

      for (Player player : level.players()) {
         if (!(player.distanceToSqr(this) > 484.0)) {
            Vec3 away = player.position().subtract(this.position());
            away = away.horizontalDistanceSqr() < 1.0E-4 ? new Vec3(0.0, 1.0, 0.0) : away.normalize();
            double falloff = 1.0 - Math.sqrt(player.distanceToSqr(this)) / 22.0;
            player.push(away.x * 0.3 * falloff, 0.16 * falloff, away.z * 0.3 * falloff);
            player.hurtMarked = true;
         }
      }

      for (ServerPlayer playerx : level.players()) {
         ServerPlayNetworking.send(playerx, new CaveRumblePayload(160, 1.0F));
      }

      level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 3.0F, 0.42F);
      net.dabicco.witherstormmod.bowels.BowelsBoss.onFightBegan(level, this);
   }

   protected void readAdditionalSaveData(ValueInput input) {
      this.entityData.set(CRACKS, input.getIntOr("Cracks", 0));
      this.entityData.set(FIGHT, input.getIntOr("Fight", -1));
      this.risen = input.getBooleanOr("Risen", false);
      this.wallSpawnAt = input.getIntOr("WallSpawnAt", 0);
      this.waveOwed = input.getIntOr("WaveOwed", 0);
      this.waveSpawned = input.getIntOr("WaveSpawned", 0);
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      output.putInt("Cracks", this.getCracks());
      output.putInt("Fight", this.getFightTicks());
      output.putBoolean("Risen", this.risen);
      output.putInt("WallSpawnAt", this.wallSpawnAt);
      output.putInt("WaveOwed", this.waveOwed);
      output.putInt("WaveSpawned", this.waveSpawned);
   }
}
