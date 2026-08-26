package net.dabicco.witherstormmod.bowels;

import net.dabicco.witherstormmod.BowelsEndRoom;
import net.dabicco.witherstormmod.BowelsFlip;
import net.dabicco.witherstormmod.BowelsFrame;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class SeveredTentacleEntity extends Entity {
   private static final EntityDataAccessor<Integer> START;
   private static final EntityDataAccessor<Integer> COUNT;
   private static final EntityDataAccessor<Float> YAW;
   private static final EntityDataAccessor<Float> CURL;
   private static final EntityDataAccessor<Float> PHASE;
   private static final EntityDataAccessor<Float> FROZEN_AT;
   private Direction fellUnder;
   private static final EntityDataAccessor<Integer> RESTED;
   private static final EntityDataAccessor<Integer> EATER;
   private static final EntityDataAccessor<Float> SCALE;
   private static final EntityDataAccessor<Float> OPEN;
   private static final EntityDataAccessor<Float> SWAY;
   private static final int LIFETIME = 4800;
   private SeveredRope rope;
   private static final double SUCK = 0.11;
   private static final double SUCK_MAX = 0.32;
   private static final double SWALLOWED = 2.6;

   public SeveredTentacleEntity(EntityType<? extends SeveredTentacleEntity> type, Level level) {
      super(type, level);
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(START, 1);
      builder.define(COUNT, 4);
      builder.define(YAW, 0.0F);
      builder.define(CURL, 0.0F);
      builder.define(PHASE, 0.0F);
      builder.define(FROZEN_AT, 0.0F);
      builder.define(RESTED, -1);
      builder.define(EATER, -1);
      builder.define(SCALE, 1.0F);
      builder.define(OPEN, 0.0F);
      builder.define(SWAY, 0.0F);
   }

   public void setup(int startBone, int totalBones, float yaw, float curl, float phase, float frozenAt, float scale, float open, float sway) {
      this.entityData.set(START, startBone);
      this.entityData.set(COUNT, Math.max(1, totalBones));
      this.entityData.set(YAW, yaw);
      this.entityData.set(CURL, curl);
      this.entityData.set(PHASE, phase);
      this.entityData.set(FROZEN_AT, frozenAt);
      this.entityData.set(SCALE, scale);
      this.entityData.set(OPEN, open);
      this.entityData.set(SWAY, sway);
   }

   public float getScale() {
      return (Float)this.entityData.get(SCALE);
   }

   public float[][] ropeJoints() {
      this.ensureRope();
      return this.rope == null ? this.joints() : this.rope.joints(this.getStart());
   }

   public Vec3 ropeAnchor(float partialTick) {
      this.ensureRope();
      if (this.rope == null) {
         return Vec3.ZERO;
      } else {
         Vec3[] prefix = BowelsTentacleShape.path(this.getStart() + 1, this.getScale(), this.ropeJoints());
         Vec3 lead = prefix[Math.min(this.getStart(), prefix.length - 1)];
         return this.rope.points()[0].subtract(this.position()).subtract(lead);
      }
   }

   private void ensureRope() {
      if (this.rope == null && this.level().isClientSide()) {
         Vec3[] local = BowelsTentacleShape.path(this.getCount(), this.getScale(), this.joints());
         int from = Mth.clamp(this.getStart(), 0, local.length - 1);
         Vec3 base = local[from];
         Vec3[] world = new Vec3[local.length - from];
         double yaw = Math.toRadians((double)(-this.getYaw()));
         double cos = Math.cos(yaw);
         double sin = Math.sin(yaw);

         for(int i = from; i < local.length; ++i) {
            Vec3 rel = local[i].subtract(base);
            world[i - from] = this.position().add(rel.x * cos + rel.z * sin, rel.y, -rel.x * sin + rel.z * cos);
         }

         this.rope = new SeveredRope(world, this.getDeltaMovement());
      }
   }

   public float[][] joints() {
      return BowelsTentacleShape.joints(this.getCount(), this.getCurl(), this.getFrozenAt(), this.getPhase(), 0.0F, 0.0F, 0.0F, (Float)this.entityData.get(OPEN), (Float)this.entityData.get(SWAY));
   }

   public Vec3 cutOffset() {
      Vec3[] whole = BowelsTentacleShape.path(this.getCount(), this.getScale(), this.joints());
      int from = Mth.clamp(this.getStart(), 0, whole.length - 1);
      return whole[from];
   }

   public void beginSuck(BowelsMawEntity maw) {
      if (!this.level().isClientSide() && (Integer)this.entityData.get(EATER) == -1) {
         this.entityData.set(EATER, maw.getId());
         this.entityData.set(RESTED, -1);
         this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 0.7F, 1.4F);
      }
   }

   public int getEaterId() {
      return (Integer)this.entityData.get(EATER);
   }

   private BowelsMawEntity eater() {
      int id = (Integer)this.entityData.get(EATER);
      if (id == -1) {
         return null;
      } else {
         Entity found = this.level().getEntity(id);
         if (found instanceof BowelsMawEntity) {
            BowelsMawEntity maw = (BowelsMawEntity)found;
            if (!maw.isRemoved() && !maw.isBlind()) {
               return maw;
            }
         }

         if (!this.level().isClientSide()) {
            this.entityData.set(EATER, -1);
         }

         this.noPhysics = false;
         return null;
      }
   }

   private void suck(BowelsMawEntity maw) {
      this.noPhysics = true;
      this.entityData.set(RESTED, -1);
      Vec3 toward = maw.mouth().subtract(this.position());
      double d = toward.length();
      if (d < 2.6) {
         maw.devour(this);
      } else {
         Vec3 motion = this.getDeltaMovement().scale(0.82).add(toward.scale(0.11 / d));
         if (motion.lengthSqr() > 0.1024) {
            motion = motion.normalize().scale(0.32);
         }

         this.setDeltaMovement(motion);
         this.setPos(this.getX() + this.getDeltaMovement().x, this.getY() + this.getDeltaMovement().y, this.getZ() + this.getDeltaMovement().z);
         this.hurtMarked = true;
      }
   }

   public int getStart() {
      return (Integer)this.entityData.get(START);
   }

   public int getCount() {
      return (Integer)this.entityData.get(COUNT);
   }

   public float getYaw() {
      return (Float)this.entityData.get(YAW);
   }

   public float getCurl() {
      return (Float)this.entityData.get(CURL);
   }

   public float getPhase() {
      return (Float)this.entityData.get(PHASE);
   }

   public float getFrozenAt() {
      return (Float)this.entityData.get(FROZEN_AT);
   }

   public boolean isResting() {
      return (Integer)this.entityData.get(RESTED) >= 0;
   }

   public int startBone() {
      return (Integer)this.entityData.get(START);
   }

   public boolean isPickable() {
      return false;
   }

   public boolean canBeCollidedWith(Entity by) {
      return false;
   }

   public boolean shouldRenderAtSqrDistance(double distanceSq) {
      return distanceSq < (double)9216.0F;
   }

   public double ropeReach() {
      return BowelsTentacleShape.NATURAL_LENGTH * (double)this.getScale() + (double)1.0F;
   }

   private Vec3 offTheHole(Vec3 motion) {
      if (BowelsFinale.running(this.level().getGameTime())) {
         return motion;
      } else {
         double dx = this.getX() - (double)177.0F;
         double dz = this.getZ() - (double)0.0F;
         double d = Math.sqrt(dx * dx + dz * dz);
         double clear = BowelsEndRoom.holeClearance();
         if (d >= clear) {
            return motion;
         } else {
            if (d < 0.001) {
               dx = (double)1.0F;
               dz = (double)0.0F;
               d = (double)1.0F;
            }

            double out = ((double)1.0F - d / clear) * 0.22 + 0.06;
            return new Vec3(motion.x + dx / d * out, motion.y, motion.z + dz / d * out);
         }
      }
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

   public void tick() {
      super.tick();
      Direction pull = BowelsFrame.of(this);
      double weight = pull == Direction.UP ? (double)1.0F : (double)-1.0F;
      BowelsMawEntity maw = this.eater();
      if (this.level().isClientSide()) {
         this.ensureRope();
         if (this.rope != null) {
            this.rope.tick(this.level(), weight, maw == null ? null : maw.mouth(), maw == null ? null : this.position());
         }

      } else if (maw != null) {
         this.suck(maw);
      } else {
         Vec3 motion = this.getDeltaMovement();
         if (this.fellUnder != null && this.fellUnder != pull) {
            this.entityData.set(RESTED, -1);
         }

         if (this.isResting() && BowelsFlip.rolling(this.level().getGameTime())) {
            this.entityData.set(RESTED, -1);
         }

         this.fellUnder = pull;
         if (!this.isResting()) {
            motion = motion.add((double)0.0F, weight * 0.045, (double)0.0F).scale(0.985);
            this.setDeltaMovement(this.offTheHole(motion));
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (this.onGround() || this.verticalCollision) {
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.45, (double)0.0F, 0.45));
               if (this.getDeltaMovement().horizontalDistanceSqr() < 0.004) {
                  this.entityData.set(RESTED, this.tickCount);
                  this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 0.8F, 0.45F);
               }
            }
         }

         if (this.tickCount > 4800) {
            this.discard();
         }

      }
   }

   protected void readAdditionalSaveData(ValueInput input) {
      this.entityData.set(START, input.getIntOr("Start", 1));
      this.entityData.set(COUNT, input.getIntOr("Count", 4));
      this.entityData.set(YAW, input.getFloatOr("Yaw", 0.0F));
      this.entityData.set(CURL, input.getFloatOr("Curl", 0.0F));
      this.entityData.set(PHASE, input.getFloatOr("Phase", 0.0F));
      this.entityData.set(FROZEN_AT, input.getFloatOr("FrozenAt", 0.0F));
      this.entityData.set(RESTED, input.getIntOr("Rested", -1));
      this.entityData.set(SCALE, input.getFloatOr("Scale", 1.0F));
      this.entityData.set(OPEN, input.getFloatOr("Open", 0.0F));
      this.entityData.set(SWAY, input.getFloatOr("Sway", 0.0F));
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      output.putInt("Start", this.getStart());
      output.putInt("Count", this.getCount());
      output.putFloat("Yaw", this.getYaw());
      output.putFloat("Curl", this.getCurl());
      output.putFloat("Phase", this.getPhase());
      output.putFloat("FrozenAt", this.getFrozenAt());
      output.putInt("Rested", (Integer)this.entityData.get(RESTED));
      output.putFloat("Scale", this.getScale());
      output.putFloat("Open", (Float)this.entityData.get(OPEN));
      output.putFloat("Sway", (Float)this.entityData.get(SWAY));
   }

   static {
      START = SynchedEntityData.defineId(SeveredTentacleEntity.class, EntityDataSerializers.INT);
      COUNT = SynchedEntityData.defineId(SeveredTentacleEntity.class, EntityDataSerializers.INT);
      YAW = SynchedEntityData.defineId(SeveredTentacleEntity.class, EntityDataSerializers.FLOAT);
      CURL = SynchedEntityData.defineId(SeveredTentacleEntity.class, EntityDataSerializers.FLOAT);
      PHASE = SynchedEntityData.defineId(SeveredTentacleEntity.class, EntityDataSerializers.FLOAT);
      FROZEN_AT = SynchedEntityData.defineId(SeveredTentacleEntity.class, EntityDataSerializers.FLOAT);
      RESTED = SynchedEntityData.defineId(SeveredTentacleEntity.class, EntityDataSerializers.INT);
      EATER = SynchedEntityData.defineId(SeveredTentacleEntity.class, EntityDataSerializers.INT);
      SCALE = SynchedEntityData.defineId(SeveredTentacleEntity.class, EntityDataSerializers.FLOAT);
      OPEN = SynchedEntityData.defineId(SeveredTentacleEntity.class, EntityDataSerializers.FLOAT);
      SWAY = SynchedEntityData.defineId(SeveredTentacleEntity.class, EntityDataSerializers.FLOAT);
   }
}
