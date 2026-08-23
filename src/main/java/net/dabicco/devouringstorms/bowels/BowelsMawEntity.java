package net.dabicco.devouringstorms.bowels;

import java.util.HashMap;
import java.util.Map;
import net.dabicco.devouringstorms.entity.WitherStormHeadEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
   private static final EntityDataAccessor<Integer> SIDE;
   private static final EntityDataAccessor<Integer> HELD;
   private static final EntityDataAccessor<Boolean> BLIND;
   private static final EntityDataAccessor<Boolean> RIGHT_HAND;
   private static final EntityDataAccessor<Integer> PROMPT;
   private static final EntityDataAccessor<Integer> PROMPT_FOR;
   private static final EntityDataAccessor<Float> CLIMB;
   private static final double OUT = (double)20.5F;
   public static final double BEAM_R = (double)5.0F;
   private static final double BEAM_R_MOUTH = (double)2.0F;
   private static final double BEAM_R_END = (double)9.0F;
   public static final double BEAM_REACH = (double)26.0F;
   private static final double HAUL = 0.12;
   private static final double SWALLOWED = (double)3.0F;
   private double homeX;
   private double homeZ;
   public static final int PROMPT_TICKS = 60;
   private static final double HANG_ABOVE = (double)9.0F;
   private static final float AIM_UP = -90.0F;
   private Vec3 aimAt;
   private Vec3 aimGoal;
   private int regoalIn;
   private static final double AIM_MIN_RISE = (double)8.0F;
   private static final double AIM_EASE = 0.045;
   private static final double BEAMS_APART = (double)9.0F;
   private int lockedOn = -1;
   private final Map<Integer, Integer> ignoreUntil = new HashMap();
   private static final int IGNORE_TICKS = 160;
   private static final double AIM_LOCK_EASE = 0.22;
   private static final double CLIMB_OFF = (double)5.0F;
   private float aimPitch = 45.0F;
   private float aimYaw;
   private static final double HUNT_PIECES = (double)30.0F;
   private static final float CLIMB_TICKS = 26.0F;

   public BowelsMawEntity(EntityType<? extends BowelsMawEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
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
      return (Integer)this.entityData.get(SIDE);
   }

   public boolean isBlind() {
      return (Boolean)this.entityData.get(BLIND);
   }

   public boolean isRightHand() {
      return (Boolean)this.entityData.get(RIGHT_HAND);
   }

   public int getHeldId() {
      return (Integer)this.entityData.get(HELD);
   }

   public int getPrompt() {
      return (Integer)this.entityData.get(PROMPT);
   }

   public int getPromptFor() {
      return (Integer)this.entityData.get(PROMPT_FOR);
   }

   public float getClimb() {
      return (Float)this.entityData.get(CLIMB);
   }

   public void placeAt(int side) {
      this.entityData.set(SIDE, side);
      this.entityData.set(RIGHT_HAND, side != 0);
      double angle = side == 0 ? (Math.PI / 2D) : (Math.PI * 1.5D);
      this.homeX = (double)177.0F + Math.cos(angle) * (double)20.5F;
      this.homeZ = (double)0.0F + Math.sin(angle) * (double)20.5F;
      this.setPos(this.homeX, this.hoverY(), this.homeZ);
      float inward = (float)Math.toDegrees(Math.atan2((double)0.0F - this.homeZ, (double)177.0F - this.homeX)) - 90.0F;
      this.setYRot(inward);
      this.yRotO = inward;
      this.setBaseYaw(inward);
      this.aimYaw = inward;
      Vec3 firstLook = (new Vec3((double)177.0F, (double)92.0F, (double)0.0F)).subtract(new Vec3(this.homeX, this.hoverY(), this.homeZ));
      this.aimPitch = (float)(-Math.toDegrees(Math.atan2(firstLook.y, firstLook.horizontalDistance())));
   }

   private double hoverY() {
      return (double)69.0F;
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
         if (this.homeX == (double)0.0F && this.homeZ == (double)0.0F) {
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
      double ceiling = (double)92.0F;
      if (this.aimGoal == null) {
         this.aimGoal = new Vec3((double)177.0F, ceiling, (double)0.0F);
      }

      if (this.aimAt == null) {
         this.aimAt = this.aimGoal;
      }

      Player caught = this.playerInBeam();
      if (caught != null) {
         this.lockedOn = caught.getId();
         this.aimGoal = caught.position().add((double)0.0F, (double)caught.getBbHeight() * (double)0.5F, (double)0.0F);
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
            this.aimGoal = climber.position().add((double)0.0F, (double)climber.getBbHeight() * (double)0.5F, (double)0.0F);
            this.aimAt = this.aimAt.add(this.aimGoal.subtract(this.aimAt).scale(0.22));
            this.point();
         } else {
            if (--this.regoalIn <= 0) {
               this.regoalIn = 50 + this.random.nextInt(70);
               SeveredTentacleEntity prize = this.nearestPiece();
               if (prize != null) {
                  this.aimGoal = new Vec3(prize.getX(), ceiling, prize.getZ());
               } else if (this.random.nextInt(3) == 0) {
                  this.aimGoal = new Vec3((double)177.0F, ceiling, (double)0.0F);
               } else {
                  double angle = this.random.nextDouble() * Math.PI * (double)2.0F;
                  double r = (double)4.0F + this.random.nextDouble() * (double)14.0F;
                  this.aimGoal = new Vec3((double)177.0F + Math.cos(angle) * r, ceiling, (double)0.0F + Math.sin(angle) * r);
               }
            }

            BowelsMawEntity twin = this.otherMaw();
            if (twin != null && twin.aimGoal != null && this.aimGoal.distanceToSqr(twin.aimGoal) < (double)81.0F) {
               Vec3 away = this.aimGoal.subtract(twin.aimGoal);
               away = new Vec3(away.x, (double)0.0F, away.z);
               if (away.lengthSqr() < 1.0E-4) {
                  away = new Vec3((double)1.0F, (double)0.0F, (double)0.0F);
               }

               this.aimGoal = (new Vec3(twin.aimGoal.x, this.aimGoal.y, twin.aimGoal.z)).add(away.normalize().scale(10.799999999999999));
               this.regoalIn = 30;
            }

            double gx = this.aimGoal.x - (double)177.0F;
            double gz = this.aimGoal.z - (double)0.0F;
            double gd = Math.sqrt(gx * gx + gz * gz);
            double limit = (double)17.0F;
            if (gd > limit) {
               this.aimGoal = new Vec3((double)177.0F + gx / gd * limit, this.aimGoal.y, (double)0.0F + gz / gd * limit);
            }

            this.aimAt = this.aimAt.add(this.aimGoal.subtract(this.aimAt).scale(0.045));
            this.point();
         }
      }
   }

   private void point() {
      double least = this.position().y + (double)8.0F;
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
      Integer until = (Integer)this.ignoreUntil.get(player.getId());
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
      for(Player player : this.level().players()) {
         if (!player.isSpectator() && !player.isCreative() && !this.ignoring(player) && (this.touchesBeam(player) || this.inBeamAt(player.getX(), player.getY() + (double)player.getBbHeight() * (double)0.5F, player.getZ()) || this.inBeamAt(player.getX(), player.getEyeY(), player.getZ()))) {
            return player;
         }
      }

      return null;
   }

   private Player climbingPlayer() {
      Player best = null;
      double bestD = Double.MAX_VALUE;

      for(Player player : this.level().players()) {
         if (!player.isSpectator() && !player.isCreative() && !this.ignoring(player) && !((double)96.0F - player.getY() < (double)5.0F)) {
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
      BowelsMawEntity twin = this.otherMaw();
      if (twin != null && !twin.isBlind()) {
         double mine = climber.distanceToSqr(this);
         double theirs = climber.distanceToSqr(twin);
         return mine < theirs || mine == theirs && this.getId() < twin.getId();
      } else {
         return true;
      }
   }

   private BowelsMawEntity otherMaw() {
      for(BowelsMawEntity maw : this.level().getEntitiesOfClass(BowelsMawEntity.class, this.getBoundingBox().inflate((double)64.0F))) {
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
      return (float)Math.toDegrees(Math.atan2((double)0.0F - this.getZ(), (double)177.0F - this.getX())) - 90.0F;
   }

   private SeveredTentacleEntity nearestPiece() {
      SeveredTentacleEntity best = null;
      double bestD = (double)900.0F;

      for(SeveredTentacleEntity piece : this.level().getEntitiesOfClass(SeveredTentacleEntity.class, this.getBoundingBox().inflate((double)30.0F))) {
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
         if (held instanceof Player) {
            Player let = (Player)held;
            this.release(let);
         }

         held = this.findPrey();
         this.entityData.set(HELD, held == null ? -1 : held.getId());
      }

      if (held != null) {
         Vec3 mouth = this.mouth();
         Vec3 toward = mouth.subtract(held.position());
         double d = toward.length();
         if (d < (double)3.0F) {
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
         for(SeveredTentacleEntity piece : this.level().getEntitiesOfClass(SeveredTentacleEntity.class, this.getBoundingBox().inflate((double)26.0F))) {
            if (piece.getEaterId() == -1 && this.touchesBeam(piece)) {
               piece.beginSuck(this);
            }
         }

      }
   }

   public void devour(SeveredTentacleEntity piece) {
      piece.discard();
      this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.6F, 0.5F);
   }

   public Vec3 mouth() {
      return new Vec3(this.getX(), this.getY(), this.getZ());
   }

   public Vec3 beamAxis() {
      if (!this.isBeamActive()) {
         return new Vec3((double)0.0F, (double)1.0F, (double)0.0F);
      } else {
         Vec3 run = this.getBeamEndExact().subtract(this.mouth());
         return run.lengthSqr() < 1.0E-6 ? new Vec3((double)0.0F, (double)1.0F, (double)0.0F) : run.normalize();
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
         return this.inBeamAt(box.minX, box.minY, box.minZ) || this.inBeamAt(box.maxX, box.maxY, box.maxZ) || this.inBeamAt(box.getCenter().x, box.getCenter().y, box.getCenter().z);
      }
   }

   private Entity findPrey() {
      return this.eatABlock() ? null : this.playerInBeam();
   }

   private boolean eatABlock() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel server) {
         BlockPos found = BowelsPlacedBlocks.anyIn(server, this);
         if (found == null) {
            return false;
         } else {
            this.spawnBeamCluster(server, found, 1);
            BowelsPlacedBlocks.forget(found);
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
         Vec3 rel = (new Vec3(x, y, z)).subtract(this.mouth());
         double along = rel.dot(axis);
         if (!(along < (double)0.0F) && !(along > (double)26.0F)) {
            double r = (double)2.0F + (double)7.0F * (along / (double)26.0F);
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
      Entity target = this.level().getEntity(this.getPromptFor());
      if (target instanceof Player player) {
         if (!player.isRemoved() && !player.isSpectator()) {
            this.entityData.set(PROMPT, this.getPrompt() - 1);
            Vec3 hold = this.mouth().add((double)0.0F, 2.4, (double)0.0F);
            player.setDeltaMovement(hold.subtract(player.position()).scale(0.35));
            player.hurtMarked = true;
            player.fallDistance = (double)0.0F;
            if (this.getPrompt() > 0) {
               return;
            }

            Level var5 = this.level();
            if (var5 instanceof ServerLevel) {
               ServerLevel server = (ServerLevel)var5;
               player.hurtServer(server, this.damageSources().generic(), 4.0F);
            }

            Vec3 away = player.position().subtract(this.mouth());
            away = away.horizontalDistanceSqr() < 1.0E-4 ? new Vec3((double)1.0F, (double)0.0F, (double)0.0F) : away.normalize();
            player.push(away.x * 1.4, (double)0.0F, away.z * 1.4);
            player.hurtMarked = true;
            this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_BIG_FALL, SoundSource.HOSTILE, 1.4F, 0.6F);
            this.closePrompt();
            return;
         }
      }

      this.closePrompt();
   }

   private void release(Player player) {
      player.setSwimming(false);
      player.setPose(Pose.STANDING);
      player.removeEffect(MobEffects.SLOWNESS);
      player.setDeltaMovement(Vec3.ZERO);
      player.hurtMarked = true;
   }

   private void closePrompt() {
      Entity var2 = this.level().getEntity(this.getPromptFor());
      if (var2 instanceof Player caught) {
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
      if (this.getPrompt() > 0 && this.getPromptFor() == player.getId()) {
         if (rightHand != this.isRightHand()) {
            return false;
         } else {
            this.closePrompt();
            this.blind();
            player.setDeltaMovement((double)0.0F, (double)0.0F, (double)0.0F);
            player.hurtMarked = true;
            return true;
         }
      } else {
         return false;
      }
   }

   public void blind() {
      if (!this.isBlind()) {
         this.closePrompt();
         this.lockedOn = -1;
         this.entityData.set(BLIND, true);
         this.disableFor(36000);
         this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 2.0F, 0.6F);
         Level var2 = this.level();
         if (var2 instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)var2;
            server.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + (double)1.0F, this.getZ(), 40, 0.8, 0.8, 0.8, 0.12);
         }

      }
   }

   private void climb() {
      if (!(this.getClimb() >= 1.0F)) {
         Player rider = null;

         for(Player player : this.level().players()) {
            if (!player.isSpectator() && player.distanceToSqr(this) < (double)25.0F) {
               rider = player;
               break;
            }
         }

         if (rider != null) {
            this.entityData.set(CLIMB, Math.min(1.0F, this.getClimb() + 0.03846154F));
            float t = this.getClimb();
            double lift = Math.sin((double)t * Math.PI * (double)0.5F);
            double swing = Math.sin((double)t * Math.PI) * 1.6;
            Vec3 want = this.mouth().add((double)0.0F, 2.2 - 4.6 * lift, (double)0.0F).add(Math.cos((double)(this.getYRot() * ((float)Math.PI / 180F))) * swing, (double)0.0F, Math.sin((double)(this.getYRot() * ((float)Math.PI / 180F))) * swing);
            rider.setDeltaMovement(want.subtract(rider.position()).scale(0.4));
            rider.hurtMarked = true;
            rider.fallDistance = (double)0.0F;
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

   static {
      SIDE = SynchedEntityData.defineId(BowelsMawEntity.class, EntityDataSerializers.INT);
      HELD = SynchedEntityData.defineId(BowelsMawEntity.class, EntityDataSerializers.INT);
      BLIND = SynchedEntityData.defineId(BowelsMawEntity.class, EntityDataSerializers.BOOLEAN);
      RIGHT_HAND = SynchedEntityData.defineId(BowelsMawEntity.class, EntityDataSerializers.BOOLEAN);
      PROMPT = SynchedEntityData.defineId(BowelsMawEntity.class, EntityDataSerializers.INT);
      PROMPT_FOR = SynchedEntityData.defineId(BowelsMawEntity.class, EntityDataSerializers.INT);
      CLIMB = SynchedEntityData.defineId(BowelsMawEntity.class, EntityDataSerializers.FLOAT);
   }
}
