package net.dabicco.witherstormmod.bowels;

import java.util.HashMap;
import java.util.Map;
import net.dabicco.witherstormmod.entity.WitherStormHeadEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BowelsMawEntity extends WitherStormHeadEntity {
   private static final EntityDataAccessor<Integer> SIDE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsMawEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> HELD = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsMawEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Boolean> BLIND = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsMawEntity.class, EntityDataSerializers.BOOLEAN
   );
   private static final EntityDataAccessor<Boolean> RIGHT_HAND = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsMawEntity.class, EntityDataSerializers.BOOLEAN
   );
   private static final EntityDataAccessor<Integer> PROMPT = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsMawEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> PROMPT_FOR = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsMawEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Float> CLIMB = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsMawEntity.class, EntityDataSerializers.FLOAT
   );
   private static final double OUT = 20.5;
   public static final double BEAM_R = 5.0;
   private static final double BEAM_R_MOUTH = 2.0;
   private static final double BEAM_R_END = 9.0;
   public static final double BEAM_REACH = 26.0;
   private static final double HAUL = 0.12;
   private static final double SWALLOWED = 3.0;
   private double homeX;
   private double homeZ;
   public static final int PROMPT_TICKS = 60;
   private static final double HANG_ABOVE = 9.0;
   private static final float AIM_UP = -90.0F;
   private Vec3 aimAt;
   private Vec3 aimGoal;
   private int regoalIn;
   private static final double AIM_MIN_RISE = 8.0;
   private static final double AIM_EASE = 0.045;
   private static final double BEAMS_APART = 9.0;
   private int lockedOn = -1;
   private final Map<Integer, Integer> ignoreUntil = new HashMap<>();
   private static final int IGNORE_TICKS = 160;
   private static final double AIM_LOCK_EASE = 0.22;
   private static final double CLIMB_OFF = 5.0;
   private float aimPitch = 45.0F;
   private float aimYaw;
   private static final double HUNT_PIECES = 30.0;
   private static final float CLIMB_TICKS = 26.0F;

   public BowelsMawEntity(EntityType<? extends net.dabicco.witherstormmod.bowels.BowelsMawEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(SIDE, 0);
      builder.define(HELD, -1);
      builder.define(BLIND, false);
      builder.define(RIGHT_HAND, false);
      builder.define(PROMPT, 0);
      builder.define(PROMPT_FOR, -1);
      builder.define(CLIMB, 0.0F);
   }

   public int getSide() {
      return (Integer)(Object)(Object)this.entityData.get(SIDE);
   }

   public boolean isBlind() {
      return (Boolean)(Object)(Object)this.entityData.get(BLIND);
   }

   public boolean isRightHand() {
      return (Boolean)(Object)(Object)this.entityData.get(RIGHT_HAND);
   }

   public int getHeldId() {
      return (Integer)(Object)(Object)this.entityData.get(HELD);
   }

   public int getPrompt() {
      return (Integer)(Object)(Object)this.entityData.get(PROMPT);
   }

   public int getPromptFor() {
      return (Integer)(Object)(Object)this.entityData.get(PROMPT_FOR);
   }

   public float getClimb() {
      return (Float)(Object)(Object)this.entityData.get(CLIMB);
   }

   public void placeAt(int side) {
      this.entityData.set(SIDE, side);
      this.entityData.set(RIGHT_HAND, side != 0);
      double angle = side == 0 ? Math.PI / 2 : Math.PI * 3.0 / 2.0;
      this.homeX = 177.0 + Math.cos(angle) * 20.5;
      this.homeZ = 0.0 + Math.sin(angle) * 20.5;
      this.setPos(this.homeX, this.hoverY(), this.homeZ);
      float inward = (float)Math.toDegrees(Math.atan2(0.0 - this.homeZ, 177.0 - this.homeX)) - 90.0F;
      this.setYRot(inward);
      this.yRotO = inward;
      this.setBaseYaw(inward);
      this.aimYaw = inward;
      Vec3 firstLook = new Vec3(177.0, 92.0, 0.0).subtract(new Vec3(this.homeX, this.hoverY(), this.homeZ));
      this.aimPitch = (float)(-Math.toDegrees(Math.atan2(firstLook.y, firstLook.horizontalDistance())));
   }

   private double hoverY() {
      return 69.0;
   }

   protected boolean needsHost() {
      return false;
   }

   protected boolean mayTakeBlocks() {
      return false;
   }

   protected float beamWidthScale() {
      return 0.4F;
   }

   protected boolean beamAlwaysOn() {
      return true;
   }

   protected float hostlessScale() {
      return 4.6F;
   }

   protected boolean mayFire() {
      return false;
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide()) {
         if (this.homeX == 0.0 && this.homeZ == 0.0) {
            this.homeX = this.getX();
            this.homeZ = this.getZ();
         }

         if (this.isBlind()) {
            this.entityData.set(HELD, -1);
            this.entityData.set(PROMPT, 0);
            this.setDeltaMovement(Vec3.ZERO);
            this.climb();
         } else if (this.getPrompt() > 0) {
            this.holdPrompt();
         } else {
            this.look();
            this.haul();
         }
      }
   }

   private void look() {
      this.setDeltaMovement(Vec3.ZERO);
      this.setPos(this.homeX, this.hoverY(), this.homeZ);
      this.chooseAim();
   }

   protected LivingEntity forcedTarget() {
      return null;
   }

   private void chooseAim() {
      double ceiling = 92.0;
      if (this.aimGoal == null) {
         this.aimGoal = new Vec3(177.0, ceiling, 0.0);
      }

      if (this.aimAt == null) {
         this.aimAt = this.aimGoal;
      }

      Player caught = this.playerInBeam();
      if (caught != null) {
         this.lockedOn = caught.getId();
         this.aimGoal = caught.position().add(0.0, caught.getBbHeight() * 0.5, 0.0);
         this.aimAt = this.aimAt.add(this.aimGoal.subtract(this.aimAt).scale(0.22));
         this.point();
      } else {
         if (this.lockedOn != -1) {
            this.ignoreUntil.put(this.lockedOn, this.tickCount + 160);
            this.lockedOn = -1;
            this.regoalIn = 0;
         }

         Player climber = this.climbingPlayer();
         if (climber != null && this.mineToAnswer(climber)) {
            this.aimGoal = climber.position().add(0.0, climber.getBbHeight() * 0.5, 0.0);
            this.aimAt = this.aimAt.add(this.aimGoal.subtract(this.aimAt).scale(0.22));
            this.point();
         } else {
            if (--this.regoalIn <= 0) {
               this.regoalIn = 50 + this.random.nextInt(70);
               net.dabicco.witherstormmod.bowels.SeveredTentacleEntity prize = this.nearestPiece();
               if (prize != null) {
                  this.aimGoal = new Vec3(prize.getX(), ceiling, prize.getZ());
               } else if (this.random.nextInt(3) == 0) {
                  this.aimGoal = new Vec3(177.0, ceiling, 0.0);
               } else {
                  double angle = this.random.nextDouble() * Math.PI * 2.0;
                  double r = 4.0 + this.random.nextDouble() * 14.0;
                  this.aimGoal = new Vec3(177.0 + Math.cos(angle) * r, ceiling, 0.0 + Math.sin(angle) * r);
               }
            }

            net.dabicco.witherstormmod.bowels.BowelsMawEntity twin = this.otherMaw();
            if (twin != null && twin.aimGoal != null && this.aimGoal.distanceToSqr(twin.aimGoal) < 81.0) {
               Vec3 away = this.aimGoal.subtract(twin.aimGoal);
               away = new Vec3(away.x, 0.0, away.z);
               if (away.lengthSqr() < 1.0E-4) {
                  away = new Vec3(1.0, 0.0, 0.0);
               }

               this.aimGoal = new Vec3(twin.aimGoal.x, this.aimGoal.y, twin.aimGoal.z).add(away.normalize().scale(10.799999999999999));
               this.regoalIn = 30;
            }

            double gx = this.aimGoal.x - 177.0;
            double gz = this.aimGoal.z - 0.0;
            double gd = Math.sqrt(gx * gx + gz * gz);
            double limit = 17.0;
            if (gd > limit) {
               this.aimGoal = new Vec3(177.0 + gx / gd * limit, this.aimGoal.y, 0.0 + gz / gd * limit);
            }

            this.aimAt = this.aimAt.add(this.aimGoal.subtract(this.aimAt).scale(0.045));
            this.point();
         }
      }
   }

   private void point() {
      double least = this.position().y + 8.0;
      if (this.aimAt.y < least) {
         this.aimAt = new Vec3(this.aimAt.x, least, this.aimAt.z);
      }

      Vec3 run = this.aimAt.subtract(this.position());
      double flat = run.horizontalDistance();
      this.aimYaw = (float)Math.toDegrees(Math.atan2(run.z, run.x)) - 90.0F;
      this.setBaseYaw(this.aimYaw);
      this.aimPitch = (float)(-Math.toDegrees(Math.atan2(run.y, flat)));
   }

   private boolean ignoring(Player player) {
      Integer until = this.ignoreUntil.get(player.getId());
      if (until == null) {
         return false;
      } else if (this.tickCount >= until) {
         this.ignoreUntil.remove(player.getId());
         return false;
      } else {
         return true;
      }
   }

   private Player playerInBeam() {
      for (Player player : this.level().players()) {
         if (!player.isSpectator()
            && !player.isCreative()
            && !this.ignoring(player)
            && (
               this.touchesBeam(player)
                  || this.inBeamAt(player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ())
                  || this.inBeamAt(player.getX(), player.getEyeY(), player.getZ())
            )) {
            return player;
         }
      }

      return null;
   }

   private Player climbingPlayer() {
      Player best = null;
      double bestD = Double.MAX_VALUE;

      for (Player player : this.level().players()) {
         if (!player.isSpectator() && !player.isCreative() && !this.ignoring(player) && !(96.0 - player.getY() < 5.0)) {
            double d = player.distanceToSqr(this);
            if (d < bestD) {
               bestD = d;
               best = player;
            }
         }
      }

      return best;
   }

   private boolean mineToAnswer(Player climber) {
      net.dabicco.witherstormmod.bowels.BowelsMawEntity twin = this.otherMaw();
      if (twin != null && !twin.isBlind()) {
         double mine = climber.distanceToSqr(this);
         double theirs = climber.distanceToSqr(twin);
         return mine < theirs || mine == theirs && this.getId() < twin.getId();
      } else {
         return true;
      }
   }

   private net.dabicco.witherstormmod.bowels.BowelsMawEntity otherMaw() {
      for (net.dabicco.witherstormmod.bowels.BowelsMawEntity maw : this.level()
         .getEntitiesOfClass(net.dabicco.witherstormmod.bowels.BowelsMawEntity.class, this.getBoundingBox().inflate(64.0))) {
         if (maw != this) {
            return maw;
         }
      }

      return null;
   }

   protected Float pinnedPitch() {
      return this.aimPitch;
   }

   protected Float pinnedYaw() {
      return this.aimYaw;
   }

   public float extraRoll() {
      return 180.0F;
   }

   private float facingIn() {
      return (float)Math.toDegrees(Math.atan2(0.0 - this.getZ(), 177.0 - this.getX())) - 90.0F;
   }

   private net.dabicco.witherstormmod.bowels.SeveredTentacleEntity nearestPiece() {
      net.dabicco.witherstormmod.bowels.SeveredTentacleEntity best = null;
      double bestD = 900.0;

      for (net.dabicco.witherstormmod.bowels.SeveredTentacleEntity piece : this.level()
         .getEntitiesOfClass(net.dabicco.witherstormmod.bowels.SeveredTentacleEntity.class, this.getBoundingBox().inflate(30.0))) {
         double d = piece.distanceToSqr(this);
         if (d < bestD) {
            bestD = d;
            best = piece;
         }
      }

      return best;
   }

   private void haul() {
      this.latchPieces();
      Entity held = this.level().getEntity(this.getHeldId());
      if (held == null || held.isRemoved() || !this.touchesBeam(held)) {
         if (held instanceof Player let) {
            this.release(let);
         }

         held = this.findPrey();
         this.entityData.set(HELD, held == null ? -1 : held.getId());
      }

      if (held != null) {
         Vec3 mouth = this.mouth();
         Vec3 toward = mouth.subtract(held.position());
         double d = toward.length();
         if (d < 3.0) {
            this.swallow(held);
            this.entityData.set(HELD, -1);
         } else {
            held.setDeltaMovement(held.getDeltaMovement().scale(0.82).add(toward.scale(0.12 / d)));
            held.hurtMarked = true;
         }
      }
   }

   private void latchPieces() {
      if (this.isBeamActive()) {
         for (net.dabicco.witherstormmod.bowels.SeveredTentacleEntity piece : this.level()
            .getEntitiesOfClass(net.dabicco.witherstormmod.bowels.SeveredTentacleEntity.class, this.getBoundingBox().inflate(26.0))) {
            if (piece.getEaterId() == -1 && this.touchesBeam(piece)) {
               piece.beginSuck(this);
            }
         }
      }
   }

   public void devour(net.dabicco.witherstormmod.bowels.SeveredTentacleEntity piece) {
      piece.discard();
      this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.6F, 0.5F);
   }

   public Vec3 mouth() {
      return new Vec3(this.getX(), this.getY(), this.getZ());
   }

   public Vec3 beamAxis() {
      if (!this.isBeamActive()) {
         return new Vec3(0.0, 1.0, 0.0);
      } else {
         Vec3 run = this.getBeamEndExact().subtract(this.mouth());
         return run.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 1.0, 0.0) : run.normalize();
      }
   }

   public boolean inBeam(Entity entity) {
      return this.inBeamAt(entity.getX(), entity.getY(), entity.getZ());
   }

   public boolean touchesBeam(Entity entity) {
      if (this.inBeam(entity)) {
         return true;
      } else {
         AABB box = entity.getBoundingBox();
         return this.inBeamAt(box.minX, box.minY, box.minZ)
            || this.inBeamAt(box.maxX, box.maxY, box.maxZ)
            || this.inBeamAt(box.getCenter().x, box.getCenter().y, box.getCenter().z);
      }
   }

   private Entity findPrey() {
      return this.eatABlock() ? null : this.playerInBeam();
   }

   private boolean eatABlock() {
      if (this.level() instanceof ServerLevel server) {
         BlockPos found = net.dabicco.witherstormmod.bowels.BowelsPlacedBlocks.anyIn(server, this);
         if (found == null) {
            return false;
         } else {
            this.spawnBeamCluster(server, found, 1);
            net.dabicco.witherstormmod.bowels.BowelsPlacedBlocks.forget(found);
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean inBeamAt(double x, double y, double z) {
      if (!this.isBeamActive()) {
         return false;
      } else {
         Vec3 axis = this.beamAxis();
         Vec3 rel = new Vec3(x, y, z).subtract(this.mouth());
         double along = rel.dot(axis);
         if (!(along < 0.0) && !(along > 26.0)) {
            double r = 2.0 + 7.0 * (along / 26.0);
            return rel.subtract(axis.scale(along)).lengthSqr() <= r * r;
         } else {
            return false;
         }
      }
   }

   private void swallow(Entity prey) {
      if (prey instanceof Player player) {
         this.openPrompt(player);
      }
   }

   private void openPrompt(Player player) {
      this.entityData.set(PROMPT, 60);
      this.entityData.set(PROMPT_FOR, player.getId());
      player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 4, false, false));
      this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 2.0F, 1.6F);
   }

   private void holdPrompt() {
      this.setDeltaMovement(Vec3.ZERO);
      this.setPos(this.homeX, this.hoverY(), this.homeZ);
      if (this.level().getEntity(this.getPromptFor()) instanceof Player player && !player.isRemoved() && !player.isSpectator()) {
         this.entityData.set(PROMPT, this.getPrompt() - 1);
         Vec3 hold = this.mouth().add(0.0, 2.4, 0.0);
         player.setDeltaMovement(hold.subtract(player.position()).scale(0.35));
         player.hurtMarked = true;
         player.fallDistance = 0.0;
         if (this.getPrompt() <= 0) {
            if (this.level() instanceof ServerLevel server) {
               player.hurtServer(server, this.damageSources().generic(), 4.0F);
            }

            Vec3 away = player.position().subtract(this.mouth());
            away = away.horizontalDistanceSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
            player.push(away.x * 1.4, 0.0, away.z * 1.4);
            player.hurtMarked = true;
            this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_BIG_FALL, SoundSource.HOSTILE, 1.4F, 0.6F);
            this.closePrompt();
         }
      } else {
         this.closePrompt();
      }
   }

   private void release(Player player) {
      player.setSwimming(false);
      player.setPose(Pose.STANDING);
      player.removeEffect(MobEffects.SLOWNESS);
      player.setDeltaMovement(Vec3.ZERO);
      player.hurtMarked = true;
   }

   private void closePrompt() {
      if (this.level().getEntity(this.getPromptFor()) instanceof Player caught) {
         this.release(caught);
      }

      if (this.getPromptFor() != -1) {
         this.ignoreUntil.put(this.getPromptFor(), this.tickCount + 160);
      }

      this.lockedOn = -1;
      this.entityData.set(PROMPT, 0);
      this.entityData.set(PROMPT_FOR, -1);
      this.entityData.set(HELD, -1);
   }

   public boolean answer(Player player, boolean rightHand) {
      if (this.getPrompt() <= 0 || this.getPromptFor() != player.getId()) {
         return false;
      } else if (rightHand != this.isRightHand()) {
         return false;
      } else {
         this.closePrompt();
         this.blind();
         player.setDeltaMovement(0.0, 0.0, 0.0);
         player.hurtMarked = true;
         return true;
      }
   }

   public void blind() {
      if (!this.isBlind()) {
         this.closePrompt();
         this.lockedOn = -1;
         this.entityData.set(BLIND, true);
         this.disableFor(36000);
         this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 2.0F, 0.6F);
         if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.0, this.getZ(), 40, 0.8, 0.8, 0.8, 0.12);
         }
      }
   }

   private void climb() {
      if (!(this.getClimb() >= 1.0F)) {
         Player rider = null;

         for (Player player : this.level().players()) {
            if (!player.isSpectator() && player.distanceToSqr(this) < 25.0) {
               rider = player;
               break;
            }
         }

         if (rider != null) {
            this.entityData.set(CLIMB, Math.min(1.0F, this.getClimb() + 0.03846154F));
            float t = this.getClimb();
            double lift = Math.sin(t * Math.PI * 0.5);
            double swing = Math.sin(t * Math.PI) * 1.6;
            Vec3 want = this.mouth()
               .add(0.0, 2.2 - 4.6 * lift, 0.0)
               .add(Math.cos(this.getYRot() * (float) (Math.PI / 180.0)) * swing, 0.0, Math.sin(this.getYRot() * (float) (Math.PI / 180.0)) * swing);
            rider.setDeltaMovement(want.subtract(rider.position()).scale(0.4));
            rider.hurtMarked = true;
            rider.fallDistance = 0.0;
         }
      }
   }

   public boolean canBeCollidedWith(Entity by) {
      return this.isBlind();
   }

   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.entityData.set(SIDE, input.getIntOr("Side", 0));
      this.entityData.set(BLIND, input.getBooleanOr("Blind", false));
      this.entityData.set(RIGHT_HAND, input.getBooleanOr("RightHand", this.getSide() != 0));
      this.entityData.set(CLIMB, input.getFloatOr("Climb", 0.0F));
      this.homeX = input.getDoubleOr("HomeX", this.getX());
      this.homeZ = input.getDoubleOr("HomeZ", this.getZ());
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putInt("Side", this.getSide());
      output.putBoolean("Blind", this.isBlind());
      output.putBoolean("RightHand", this.isRightHand());
      output.putFloat("Climb", this.getClimb());
      output.putDouble("HomeX", this.homeX);
      output.putDouble("HomeZ", this.homeZ);
   }
}
