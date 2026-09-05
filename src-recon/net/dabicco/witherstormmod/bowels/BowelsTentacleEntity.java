package net.dabicco.witherstormmod.bowels;

import net.dabicco.witherstormmod.BowelsEndRoom;
import net.dabicco.witherstormmod.BowelsFlip;
import net.dabicco.witherstormmod.BowelsFrame;
import net.dabicco.witherstormmod.ModSounds;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BowelsTentacleEntity extends Entity {
   public static final int MODE_CURLED = 0;
   public static final int MODE_UNRAVEL = 1;
   public static final int MODE_HUNT = 2;
   public static final int MODE_STRIKE = 3;
   public static final int MODE_SQUIRM = 4;
   public static final int MODE_RETRACT = 5;
   public static final int MODE_SWAY = 6;
   public static final int MODE_GRAB = 7;
   public static final int MODE_DYING = 9;
   public static final int MODE_LURK = 8;
   private static final EntityDataAccessor<Integer> BONES = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Float> CURL = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> MOUNT_YAW = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> PHASE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Float> AIM = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Boolean> GUARD = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.BOOLEAN
   );
   private static final EntityDataAccessor<Integer> ORDER = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Boolean> ON_END = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.BOOLEAN
   );
   private static final EntityDataAccessor<Float> OPEN = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> SWAY = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Boolean> WHACK = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.BOOLEAN
   );
   private static final EntityDataAccessor<Float> EMERGE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> RIGID = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Float> COIL = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Integer> STANCE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Boolean> CUTTABLE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.BOOLEAN
   );
   private static final EntityDataAccessor<Float> WRAP = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final int OPEN_STAGGER = 2;
   private static final int WHIP_TICKS = 9;
   private static final int RECOIL_TICKS = 17;
   private static final float WHIP_PEAK = 1.05F;
   private static final float ANGRY_OPEN = 1.0F;
   private static final float STANCE_STEP = 0.045F;
   private static final double SWING_AT = 9.0;
   public static final int ARCH_OPEN_TICKS = 36;
   private static final float SWAY_FADE_TICKS = 22.0F;
   private static final float LURK_SWAY = 0.22F;
   private static final double EMERGE_RANGE = 20.0;
   private static final double EMERGE_CONE = 0.15;
   private static final float EMERGE_TICKS = 28.0F;
   private static final int SQUIRM_TICKS = 34;
   private static final double NATURAL_REACH = net.dabicco.witherstormmod.bowels.BowelsTentacleShape.NATURAL_LENGTH;
   private static final double HIT_RANGE = 1.9;
   private static final float HIT_DAMAGE = 4.0F;
   private static final double WHACK_PUSH = 1.65;
   private static final double SEVER_CLEAR_PUSH = 0.55;
   private static final int GRAB_TICKS = 32;
   private static final double GRAB_HOLD = 0.55;
   private static final double THROW_FORCE = 2.35;
   private static final double THROW_LIFT = 0.22;
   private static final float COIL_TICKS = 7.0F;
   private static final int MIN_CUT = 2;
   private static final int FUNCTIONAL_BONES = 6;
   private static final int FUNCTIONAL_FREE = 4;
   private static final int MIN_BONES = 2;
   private static final double CUT_SLACK = 1.8;
   private static final double CUT_RANGE = 9.0;
   private static final int CUT_STEPS = 4;
   private static final int HIT_STEPS = 3;
   private static final int STRIKE_COMMIT = 30;
   private static final int STRIKE_GIVE_UP = 90;
   private static final double BLOCK_AIM_RANGE = 8.0;
   private static final double BLOCK_AIM_SLACK = 2.5;
   private static final double LIMB_MUST_BEAT = 0.35;
   private static final int DIE_CURL = 34;
   private static final int DIE_FALL_TICKS = 70;
   private static final float DIE_SHAKE = 0.55F;
   private static final double DIE_GRAVITY = 0.045;
   private double dieFall;
   private int modeTicks;
   private int strikeCooldown;
   private float animTicks;
   private static final int REGROW_TICKS = 80;
   private int regrowAt;
   private double anchorX;
   private double anchorY;
   private double anchorZ;
   private double outX;
   private double outZ;
   private double travel;
   private double mouthX;
   private double mouthZ;
   private int holeIndex = -1;
   private static final EntityDataAccessor<Integer> GRAB_PROMPT = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Integer> GRAB_FOR = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Boolean> GRAB_RIGHT = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.BOOLEAN
   );
   private static final EntityDataAccessor<Integer> RIDER = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Float> RIDE_AT = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, EntityDataSerializers.FLOAT
   );
   private static final float RIDE_MIN = 0.25F;
   private static final int GRAB_PROMPT_TICKS = 40;
   private static final double GRAB_OFFER = 4.5;
   private static final float CLIMB_RATE = 0.035F;
   private static final double GRAB_LEVEL = 2.2;
   private static final double SEAT_DROP = 0.9;
   private float clientRigid = -1.0F;

   private static boolean canCut(LivingEntity attacker) {
      ItemStack held = attacker.getMainHandItem();
      return held.has(DataComponents.TOOL) || held.has(DataComponents.WEAPON);
   }

   public BowelsTentacleEntity(EntityType<? extends net.dabicco.witherstormmod.bowels.BowelsTentacleEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(BONES, net.dabicco.witherstormmod.bowels.BowelsTentacleShape.BONES);
      builder.define(SCALE, 1.0F);
      builder.define(MODE, 2);
      builder.define(CURL, 0.0F);
      builder.define(MOUNT_YAW, 0.0F);
      builder.define(PHASE, 0.0F);
      builder.define(TARGET, -1);
      builder.define(AIM, 0.0F);
      builder.define(GUARD, false);
      builder.define(ORDER, 0);
      builder.define(ON_END, false);
      builder.define(GRAB_PROMPT, 0);
      builder.define(GRAB_FOR, -1);
      builder.define(GRAB_RIGHT, false);
      builder.define(RIDER, -1);
      builder.define(RIDE_AT, 0.0F);
      builder.define(OPEN, 0.0F);
      builder.define(SWAY, 0.0F);
      builder.define(WHACK, false);
      builder.define(EMERGE, 0.0F);
      builder.define(RIGID, 0.0F);
      builder.define(COIL, 0.0F);
      builder.define(STANCE, 0);
      builder.define(CUTTABLE, false);
      builder.define(WRAP, 0.0F);
   }

   public float getEmerge() {
      return (Float)(Object)(Object)this.entityData.get(EMERGE);
   }

   public float getRigid() {
      return (Float)(Object)(Object)this.entityData.get(RIGID);
   }

   public float getCoil() {
      return (Float)(Object)(Object)this.entityData.get(COIL);
   }

   public int getStance() {
      return (Integer)(Object)(Object)this.entityData.get(STANCE);
   }

   public boolean isCuttable() {
      return (Boolean)(Object)(Object)this.entityData.get(CUTTABLE);
   }

   public float getWrap() {
      return (Float)(Object)(Object)this.entityData.get(WRAP);
   }

   public boolean stillFunctional() {
      return this.getRigid() <= 0.0F ? this.getBones() >= 6 : this.getBones() - this.firstFreeBone() >= 4;
   }

   public boolean intact() {
      return this.getBones() >= net.dabicco.witherstormmod.bowels.BowelsTentacleShape.BONES;
   }

   public void setStance(int value, boolean cuttable) {
      this.entityData.set(STANCE, value);
      this.entityData.set(CUTTABLE, cuttable);
   }

   public int getHoleIndex() {
      return this.holeIndex;
   }

   public void setHole(int index, Vec3 face, Vec3 out) {
      this.holeIndex = index;
      this.outX = out.x;
      this.outZ = out.z;
      this.mouthX = face.x;
      this.mouthZ = face.z;
      double length = net.dabicco.witherstormmod.bowels.BowelsTentacleShape.NATURAL_LENGTH * this.getScale();
      this.anchorX = face.x - out.x * length;
      this.anchorY = face.y;
      this.anchorZ = face.z - out.z * length;
      this.travel = Math.max(0.0, length - 9.0);
      this.setPos(this.anchorX, this.anchorY, this.anchorZ);
   }

   public int getBones() {
      return (Integer)(Object)(Object)this.entityData.get(BONES);
   }

   public void setBones(int v) {
      this.entityData.set(BONES, Mth.clamp(v, 1, net.dabicco.witherstormmod.bowels.BowelsTentacleShape.BONES));
   }

   public float getScale() {
      return (Float)(Object)(Object)this.entityData.get(SCALE);
   }

   public void setScale(float v) {
      this.entityData.set(SCALE, v);
   }

   public double reach() {
      double whole = net.dabicco.witherstormmod.bowels.BowelsTentacleShape.reachTo(this.getBones());
      double buried = net.dabicco.witherstormmod.bowels.BowelsTentacleShape.reachTo(this.firstFreeBone());
      return Math.max(0.0, whole - buried) * this.getScale();
   }

   public int getMode() {
      return (Integer)(Object)(Object)this.entityData.get(MODE);
   }

   public float getCurl() {
      return (Float)(Object)(Object)this.entityData.get(CURL);
   }

   public void setCurl(float v) {
      this.entityData.set(CURL, Mth.clamp(v, 0.0F, 1.0F));
   }

   public float getMountYaw() {
      return (Float)(Object)(Object)this.entityData.get(MOUNT_YAW);
   }

   public void setMountYaw(float v) {
      this.entityData.set(MOUNT_YAW, v);
   }

   public float getPhase() {
      return (Float)(Object)(Object)this.entityData.get(PHASE);
   }

   public void setPhase(float v) {
      this.entityData.set(PHASE, v);
   }

   public float getAim() {
      return (Float)(Object)(Object)this.entityData.get(AIM);
   }

   public int getTargetId() {
      return (Integer)(Object)(Object)this.entityData.get(TARGET);
   }

   public boolean isGuard() {
      return (Boolean)(Object)(Object)this.entityData.get(GUARD);
   }

   public void setGuard(boolean v) {
      this.entityData.set(GUARD, v);
   }

   public int getOrder() {
      return (Integer)(Object)(Object)this.entityData.get(ORDER);
   }

   public void setOrder(int v) {
      this.entityData.set(ORDER, v);
   }

   public boolean isOnEnd() {
      return (Boolean)(Object)(Object)this.entityData.get(ON_END);
   }

   public int getGrabPrompt() {
      return (Integer)(Object)(Object)this.entityData.get(GRAB_PROMPT);
   }

   public int getGrabFor() {
      return (Integer)(Object)(Object)this.entityData.get(GRAB_FOR);
   }

   public boolean isGrabRight() {
      return (Boolean)(Object)(Object)this.entityData.get(GRAB_RIGHT);
   }

   public static boolean ridingAny(Level level, Player player) {
      for (net.dabicco.witherstormmod.bowels.BowelsTentacleEntity limb : level.getEntitiesOfClass(
         net.dabicco.witherstormmod.bowels.BowelsTentacleEntity.class, player.getBoundingBox().inflate(48.0)
      )) {
         if (limb.getRiderId() == player.getId()) {
            return true;
         }
      }

      return false;
   }

   public int getRiderId() {
      return (Integer)(Object)(Object)this.entityData.get(RIDER);
   }

   public float getRideAt() {
      return (Float)(Object)(Object)this.entityData.get(RIDE_AT);
   }

   private void ridership() {
      if (this.level().getEntity(this.getRiderId()) instanceof Player rider && !rider.isRemoved()) {
         this.carry(rider);
      } else {
         this.entityData.set(RIDER, -1);
         if (this.getGrabPrompt() > 0) {
            this.entityData.set(GRAB_PROMPT, this.getGrabPrompt() - 1);
            if (this.getGrabPrompt() <= 0) {
               this.entityData.set(GRAB_FOR, -1);
            }
         } else if (BowelsFlip.flipped() && this.isGuard()) {
            for (Player player : this.level().players()) {
               if (!player.isSpectator()
                  && !player.onGround()
                  && !(this.nearestOn(player) > 4.5)
                  && !(Math.abs(this.nearestPointOn(player).y - player.getY()) > 2.2)) {
                  this.entityData.set(GRAB_PROMPT, 40);
                  this.entityData.set(GRAB_FOR, player.getId());
                  this.entityData.set(GRAB_RIGHT, player.getX() > this.getX());
                  this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.6F, 1.7F);
                  return;
               }
            }
         }
      }
   }

   private double nearestOn(Player player) {
      return Math.sqrt(this.nearestPointOn(player).distanceToSqr(player.position()));
   }

   private Vec3 nearestPointOn(Player player) {
      Vec3[] path = this.aimPath();
      Vec3 best = path[path.length - 1];
      double bestD = Double.MAX_VALUE;

      for (Vec3 p : path) {
         double d = p.distanceToSqr(player.position());
         if (d < bestD) {
            bestD = d;
            best = p;
         }
      }

      return best;
   }

   public boolean answerGrab(Player player, boolean rightHand) {
      if (this.getGrabPrompt() <= 0 || this.getGrabFor() != player.getId()) {
         return false;
      } else if (rightHand != this.isGrabRight()) {
         return false;
      } else {
         this.entityData.set(GRAB_PROMPT, 0);
         this.entityData.set(GRAB_FOR, -1);
         this.entityData.set(RIDER, player.getId());
         this.entityData.set(RIDE_AT, this.alongAt(player));
         this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_ATTACK, SoundSource.HOSTILE, 1.4F, 0.8F);
         return true;
      }
   }

   private float alongAt(Player player) {
      Vec3[] path = this.aimPath();
      int best = 0;
      double bestD = Double.MAX_VALUE;

      for (int i = 0; i < path.length; i++) {
         double d = path[i].distanceToSqr(player.position());
         if (d < bestD) {
            bestD = d;
            best = i;
         }
      }

      return path.length <= 1 ? 0.0F : (float)best / (path.length - 1);
   }

   private void carry(Player rider) {
      if (this.stillFunctional() && !rider.isSpectator()) {
         Input var10000;
         if (rider instanceof ServerPlayer sp) {
            var10000 = sp.getLastClientInput();
         } else {
            var10000 = null;
         }

         boolean up = var10000 != null ? var10000.forward() : rider.zza > 0.0F;
         boolean down = var10000 != null ? var10000.backward() : rider.zza < 0.0F;
         boolean letGo = var10000 != null ? var10000.shift() : rider.isShiftKeyDown();
         if (letGo) {
            this.entityData.set(RIDER, -1);
            rider.setDeltaMovement(rider.getDeltaMovement().add(0.0, -0.25, 0.0));
            rider.hurtMarked = true;
         } else {
            if (up) {
               this.entityData.set(RIDE_AT, Math.max(0.25F, this.getRideAt() - 0.035F));
            } else if (down) {
               this.entityData.set(RIDE_AT, Math.min(1.0F, this.getRideAt() + 0.035F));
            }

            Vec3[] path = this.aimPath();
            float exact = this.getRideAt() * (path.length - 1);
            int at = Mth.clamp((int)exact, 0, path.length - 2);
            Vec3 on = path[at].lerp(path[at + 1], exact - at);
            double seat = BowelsFrame.of(rider) == Direction.UP ? 0.9 : -0.9;
            Vec3 outward = new Vec3(on.x - 177.0, 0.0, on.z - 0.0);
            outward = outward.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : outward.normalize();
            double swing = Math.sin(Mth.clamp(this.getRideAt(), 0.0F, 1.0F) * Math.PI * 0.5);
            on = on.add(outward.scale(-Math.abs(seat) * swing)).add(0.0, seat * (1.0 - swing), 0.0);
            rider.setDeltaMovement(on.subtract(rider.position()).scale(0.45));
            rider.hurtMarked = true;
            rider.fallDistance = 0.0;
            rider.resetFallDistance();
         }
      } else {
         this.entityData.set(RIDER, -1);
      }
   }

   public void setOnEnd(boolean v) {
      this.entityData.set(ON_END, v);
   }

   public float getOpen() {
      return (Float)(Object)(Object)this.entityData.get(OPEN);
   }

   public float getSway() {
      return (Float)(Object)(Object)this.entityData.get(SWAY);
   }

   public boolean isWhack() {
      return (Boolean)(Object)(Object)this.entityData.get(WHACK);
   }

   public void setWhack(boolean v) {
      this.entityData.set(WHACK, v);
   }

   private Player onPedestalNearby() {
      Player best = null;
      double bestDist = this.reach() * this.reach();
      Vec3 block = BowelsEndRoom.daisTop();

      for (Player player : this.level().players()) {
         if (!player.isSpectator() && !player.isCreative() && !(player.distanceToSqr(block.x, block.y, block.z) > 81.0) && this.onMySide(player)) {
            double d = player.distanceToSqr(this);
            if (d < bestDist) {
               bestDist = d;
               best = player;
            }
         }
      }

      return best;
   }

   public void setMode(int mode) {
      this.entityData.set(MODE, mode);
      this.modeTicks = 0;
   }

   public float animClock(float partialTick) {
      return (float)this.level().getGameTime() + partialTick;
   }

   public boolean isNoGravity() {
      return true;
   }

   public boolean isPickable() {
      return this.getMode() != 0;
   }

   public boolean isAttackable() {
      return this.isPickable();
   }

   public boolean canBeCollidedWith(Entity by) {
      return false;
   }

   public float[][] joints(float partialTick) {
      float[] aim = net.dabicco.witherstormmod.bowels.BowelsTentacleShape.aimAngles(this.aimLocal());
      return net.dabicco.witherstormmod.bowels.BowelsTentacleShape.joints(
         this.getBones(),
         this.getCurl(),
         this.animClock(partialTick),
         this.getPhase(),
         aim[0],
         aim[1],
         this.getAim(),
         this.getOpen(),
         this.getSway(),
         this.getRigid(),
         this.getCoil(),
         this.getWrap()
      );
   }

   public Vec3[] aimPath() {
      Vec3[] whole = this.path(0.0F);
      int from = Math.min(this.firstFreeBone(), whole.length - 2);
      if (from <= 0) {
         return whole;
      } else {
         Vec3[] out = new Vec3[whole.length - from];
         System.arraycopy(whole, from, out, 0, out.length);
         return out;
      }
   }

   public Vec3[] path(float partialTick) {
      Vec3 off = this.mountOffset();
      return net.dabicco.witherstormmod.bowels.BowelsTentacleShape.toWorld(
         net.dabicco.witherstormmod.bowels.BowelsTentacleShape.path(this.getBones(), this.getScale(), this.joints(partialTick)),
         this.getX() + off.x,
         this.getY(),
         this.getZ() + off.z,
         this.getMountYaw()
      );
   }

   private Vec3 aimLocal() {
      Entity target = this.level().getEntity(this.getTargetId());
      return target == null
         ? Vec3.ZERO
         : net.dabicco.witherstormmod.bowels.BowelsTentacleShape.toLocal(
            this.getX(), this.getY(), this.getZ(), this.getMountYaw(), target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ()
         );
   }

   public void tick() {
      super.tick();
      this.animTicks++;
      this.modeTicks++;
      this.refreshBox();
      if (this.level().isClientSide()) {
         this.chaseRigid();
      } else {
         if (this.strikeCooldown > 0) {
            this.strikeCooldown--;
         }

         this.regrow();
         this.ridership();
         this.followBore();
         if (this.getMode() != 7 && this.getCoil() > 0.0F) {
            this.entityData.set(COIL, Math.max(0.0F, this.getCoil() - 0.14285715F));
         }

         switch (this.getMode()) {
            case 0:
               this.setCurl(1.0F);
               this.entityData.set(SWAY, 0.0F);
               this.entityData.set(WRAP, 0.0F);
               break;
            case 1:
               this.setCurl(1.0F);
               int began = this.modeTicks - this.getOrder() * 2;
               if (began >= 0) {
                  if (began < 9) {
                     float tx = began / 9.0F;
                     this.entityData.set(OPEN, 1.05F * (1.0F - (1.0F - tx) * (1.0F - tx)));
                  } else {
                     float tx = Mth.clamp((began - 9) / 17.0F, 0.0F, 1.0F);
                     this.entityData.set(OPEN, Mth.lerp(net.dabicco.witherstormmod.bowels.BowelsTentacleShape.smoothstep(tx), 1.05F, 1.0F));
                     if (tx >= 1.0F) {
                        this.setMode(this.isGuard() ? 6 : 2);
                     }
                  }

                  this.entityData.set(SWAY, 0.0F);
               }
               break;
            case 2:
               this.setCurl(0.0F);
               this.entityData.set(OPEN, 0.0F);
               this.entityData.set(SWAY, Math.min(1.0F, this.getSway() + 0.045454547F));
               this.hunt();
               break;
            case 3:
               this.strike();
               break;
            case 4:
               this.entityData.set(AIM, 0.0F);
               this.entityData.set(TARGET, -1);
               this.entityData.set(SWAY, 1.8F);
               if (this.modeTicks >= 34) {
                  this.setMode(this.restingMode());
               }
               break;
            case 5:
               if (this.modeTicks % 3 == 0) {
                  this.setBones(this.getBones() - 1);
                  if (this.getBones() <= 1) {
                     this.discard();
                  }
               }
               break;
            case 6:
               this.setCurl(1.0F);
               this.entityData.set(OPEN, 1.0F);
               this.entityData.set(SWAY, Math.min(1.0F, this.getSway() + 0.045454547F));
               if (this.modeTicks > 22.0F && this.strikeCooldown <= 0) {
                  Player victim = this.onPedestalNearby();
                  if (victim != null) {
                     this.entityData.set(TARGET, victim.getId());
                     this.setMode(3);
                  } else if (this.isWhack()) {
                     this.setWhack(false);
                  }
               }
               break;
            case 7:
               this.grab();
               break;
            case 8:
               this.lurk();
               break;
            case 9:
               float t = Math.min(1.0F, this.modeTicks / 34.0F);
               float ease = net.dabicco.witherstormmod.bowels.BowelsTentacleShape.smoothstep(t);
               this.entityData.set(OPEN, Mth.lerp(ease, this.getOpen(), 0.0F));
               this.setCurl(Mth.lerp(ease * 0.25F, this.getCurl(), 1.0F));
               this.entityData.set(AIM, Math.max(0.0F, this.getAim() - 0.05F));
               this.entityData.set(TARGET, -1);
               float shake = Mth.sin(this.modeTicks * 0.9F) * 0.55F * ease * (1.0F - ease * 0.6F);
               this.entityData.set(SWAY, 1.0F + Math.abs(shake) * 3.0F);
               this.entityData.set(PHASE, this.getPhase() + shake * 0.05F);
               if (this.modeTicks >= 34) {
                  if (this.getRiderId() != -1) {
                     this.entityData.set(RIDER, -1);
                  }

                  this.dieFall += 0.045;
                  this.setPos(this.getX(), this.getY() - this.dieFall, this.getZ());
                  if (this.modeTicks > 104) {
                     this.discard();
                  }
               }
         }
      }
   }

   private void regrow() {
      if (this.getMode() != 5 && !this.intact() && ++this.regrowAt >= 80) {
         this.regrowAt = 0;
         this.setBones(this.getBones() + 1);
         if (this.getMode() == 4 && this.stillFunctional()) {
            this.setMode(this.restingMode());
         }

         if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY(), this.getZ(), 4, 0.35, 0.35, 0.35, 0.02);
         }
      }
   }

   private void hunt() {
      if (!this.stillFunctional()) {
         this.entityData.set(TARGET, -1);
         this.entityData.set(AIM, Math.max(0.0F, this.getAim() - 0.06F));
      } else {
         Player nearest = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), this.reach(), false);
         if (nearest != null && this.strikeCooldown <= 0) {
            this.entityData.set(TARGET, nearest.getId());
            this.setMode(3);
         } else {
            this.entityData.set(TARGET, -1);
            this.entityData.set(AIM, Math.max(0.0F, this.getAim() - 0.06F));
         }
      }
   }

   private boolean onMySide(Player player) {
      Vec3 middle = BowelsEndRoom.daisTop();
      double mx = this.getX() - middle.x;
      double mz = this.getZ() - middle.z;
      double px = player.getX() - middle.x;
      double pz = player.getZ() - middle.z;
      double mine = mx * mx + mz * mz;
      double theirs = px * px + pz * pz;
      return !(mine < 1.0E-4) && !(theirs < 2.25) ? mx * px + mz * pz > 0.0 : true;
   }

   private void strike() {
      if (this.level().getEntity(this.getTargetId()) instanceof Player player
         && !player.isSpectator()
         && !(player.distanceToSqr(this) > this.reach() * this.reach())) {
         this.entityData.set(AIM, Math.min(1.0F, this.getAim() + 0.085F));
         Vec3[] path = this.path(0.0F);
         boolean touching = this.sweepsThrough(path, player);
         if (!touching && this.modeTicks < 30) {
            if (this.modeTicks > 90) {
               this.strikeCooldown = 30;
               this.entityData.set(TARGET, -1);
               this.setMode(this.restingMode());
            }
         } else if (!this.isGuard() && touching && this.intact() && this.strikeCooldown <= 0) {
            this.setMode(7);
         } else {
            if (this.level() instanceof ServerLevel server) {
               player.hurtServer(server, this.damageSources().thrown(this, (Entity)null), 4.0F);
               boolean clearing = this.isWhack();
               Vec3 from = clearing ? BowelsEndRoom.daisTop() : this.position();
               Vec3 away = player.position().subtract(from);
               away = away.horizontalDistanceSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
               double force = clearing ? 1.65 : 0.75;
               player.push(away.x * force, clearing ? 0.72 : 0.42, away.z * force);
               player.hurtMarked = true;
               server.playSound(
                  (Entity)null,
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  SoundEvents.SLIME_ATTACK,
                  SoundSource.HOSTILE,
                  clearing ? 1.8F : 1.2F,
                  clearing ? 0.42F : 0.55F
               );
               if (clearing) {
                  this.setWhack(false);
                  net.dabicco.witherstormmod.bowels.BowelsBoss.onPedestalCleared(server);
               }
            }

            this.strikeCooldown = this.isOnEnd() ? 90 : 70;
            this.entityData.set(TARGET, -1);
            this.setMode(this.restingMode());
         }
      } else {
         this.entityData.set(TARGET, -1);
         this.setMode(this.restingMode());
      }
   }

   private void grab() {
      if (this.level().getEntity(this.getTargetId()) instanceof Player player && !player.isSpectator()) {
         this.entityData.set(AIM, 1.0F);
         this.entityData.set(COIL, Math.min(1.0F, this.getCoil() + 0.14285715F));
         Vec3[] path = this.path(0.0F);
         Vec3 tip = path[path.length - 1];
         if (this.modeTicks < 32) {
            Vec3 to = tip.subtract(player.position());
            player.push(to.x * 0.55 - player.getDeltaMovement().x, to.y * 0.55 - player.getDeltaMovement().y + 0.04, to.z * 0.55 - player.getDeltaMovement().z);
            player.hurtMarked = true;
            player.resetFallDistance();
            if (this.modeTicks % 7 == 0 && this.level() instanceof ServerLevel server) {
               server.playSound((Entity)null, tip.x, tip.y, tip.z, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.HOSTILE, 1.1F, 0.5F);
            }
         } else {
            Vec3 middle = BowelsEndRoom.daisTop();
            Vec3 out = player.position().subtract(middle);
            out = out.horizontalDistanceSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(out.x, 0.0, out.z).normalize();
            player.push(out.x * 2.35, 0.22, out.z * 2.35);
            player.hurtMarked = true;
            if (this.level() instanceof ServerLevel server) {
               player.hurtServer(server, this.damageSources().thrown(this, (Entity)null), 4.0F);
               server.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_ATTACK, SoundSource.HOSTILE, 1.7F, 0.42F);
            }

            this.strikeCooldown = 70;
            this.entityData.set(TARGET, -1);
            this.setMode(this.restingMode());
         }
      } else {
         this.entityData.set(TARGET, -1);
         this.setMode(this.restingMode());
      }
   }

   private void lurk() {
      this.setCurl(0.0F);
      this.entityData.set(OPEN, 0.0F);
      this.entityData.set(SWAY, 0.22F);
      boolean wanted = false;

      for (Player player : this.level().players()) {
         if (!player.isSpectator() && !player.isCreative()) {
            double dx = player.getX() - this.mouthX;
            double dz = player.getZ() - this.mouthZ;
            double d2 = dx * dx + dz * dz;
            if (!(d2 > 400.0) && !(Math.abs(player.getY() - this.anchorY) > 8.0)) {
               double d = Math.sqrt(d2);
               if (d < 0.001) {
                  wanted = true;
                  break;
               }

               double ahead = (dx * this.outX + dz * this.outZ) / d;
               if (ahead >= 0.15) {
                  wanted = true;
                  break;
               }
            }
         }
      }

      float now = this.getEmerge();
      if (!(now <= 0.0F) || wanted) {
         if (now <= 0.0F && this.level() instanceof ServerLevel server) {
            server.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.HOSTILE, 1.8F, 0.45F);
         }

         float next = Math.min(1.0F, now + 0.035714287F);
         this.entityData.set(EMERGE, next);
         if (next >= 0.999F) {
            this.setMode(2);
         }
      }
   }

   private void followBore() {
      if (this.outX != 0.0 || this.outZ != 0.0) {
         this.setPos(this.mouthX, this.anchorY, this.mouthZ);
         double length = net.dabicco.witherstormmod.bowels.BowelsTentacleShape.NATURAL_LENGTH * this.getScale();
         double buried = length - net.dabicco.witherstormmod.bowels.BowelsTentacleShape.smoothstep(this.getEmerge()) * this.travel;
         this.entityData.set(RIGID, (float)Mth.clamp(buried / Math.max(length, 0.001), 0.0, 1.0));
      }
   }

   public Vec3 mountOffset() {
      float rigid = this.level().isClientSide() ? this.clientRigid : this.getRigid();
      if (rigid <= 0.0F) {
         return Vec3.ZERO;
      } else {
         double buried = rigid * net.dabicco.witherstormmod.bowels.BowelsTentacleShape.NATURAL_LENGTH * this.getScale();
         double yaw = Math.toRadians(this.getMountYaw());
         return new Vec3(Math.sin(yaw) * buried, 0.0, -Math.cos(yaw) * buried);
      }
   }

   private void chaseRigid() {
      float want = this.getRigid();
      if (this.clientRigid < 0.0F) {
         this.clientRigid = want;
      } else {
         this.clientRigid = this.clientRigid + (want - this.clientRigid) * 0.35F;
      }
   }

   private net.dabicco.witherstormmod.bowels.BowelsHeartEntity blockUnderAim(LivingEntity attacker) {
      Vec3 eye = attacker.getEyePosition();
      Vec3 look = attacker.getLookAngle();
      net.dabicco.witherstormmod.bowels.BowelsHeartEntity heart = null;
      double bestBlock = Double.MAX_VALUE;

      for (net.dabicco.witherstormmod.bowels.BowelsHeartEntity h : this.level()
         .getEntitiesOfClass(net.dabicco.witherstormmod.bowels.BowelsHeartEntity.class, attacker.getBoundingBox().inflate(8.0))) {
         Vec3 middle = h.position().add(0.0, 0.5, 0.0);
         double along = middle.subtract(eye).dot(look);
         if (!(along < 0.0) && !(along > 8.0)) {
            double off = middle.distanceToSqr(eye.add(look.scale(along)));
            if (off < bestBlock) {
               bestBlock = off;
               heart = h;
            }
         }
      }

      if (heart != null && !(bestBlock > 6.25)) {
         double blockWins = bestBlock * 0.35;
         Vec3[] path = this.path(0.0F);

         for (int i = Math.max(1, this.firstFreeBone()); i < path.length; i++) {
            Vec3 p = path[i];
            double along = p.subtract(eye).dot(look);
            if (!(along < 0.0) && !(along > 8.0) && p.distanceToSqr(eye.add(look.scale(along))) < blockWins) {
               return null;
            }
         }

         return heart;
      } else {
         return null;
      }
   }

   private boolean sweepsThrough(Vec3[] path, Player player) {
      Vec3 feet = player.position();
      Vec3 eyes = player.getEyePosition();
      double slack = 3.61;

      for (int i = 1; i < path.length; i++) {
         Vec3 from = path[i - 1];
         Vec3 to = path[i];

         for (int step = 0; step <= 3; step++) {
            Vec3 p = from.lerp(to, step / 3.0);
            if (p.distanceToSqr(feet) < slack || p.distanceToSqr(eyes) < slack) {
               return true;
            }
         }
      }

      return false;
   }

   private int restingMode() {
      return this.isGuard() ? 6 : 2;
   }

   private void refreshBox() {
      Vec3[] path = this.path(0.0F);
      int from = this.firstFreeBone();
      Vec3 head = path[Math.min(from, path.length - 1)];
      double minX = head.x;
      double minY = head.y;
      double minZ = head.z;
      double maxX = minX;
      double maxY = minY;
      double maxZ = minZ;

      for (int i = from; i < path.length; i++) {
         Vec3 p = path[i];
         minX = Math.min(minX, p.x);
         maxX = Math.max(maxX, p.x);
         minY = Math.min(minY, p.y);
         maxY = Math.max(maxY, p.y);
         minZ = Math.min(minZ, p.z);
         maxZ = Math.max(maxZ, p.z);
      }

      double pad = 0.5;
      this.setBoundingBox(new AABB(minX - pad, minY - pad, minZ - pad, maxX + pad, maxY + pad, maxZ + pad));
   }

   private int firstFreeBone() {
      float rigid = this.getRigid();
      if (rigid <= 0.0F) {
         return 0;
      } else {
         int at = (int)Math.floor(rigid * (net.dabicco.witherstormmod.bowels.BowelsTentacleShape.BONES - 1));
         return Mth.clamp(at, 0, this.getBones() - 1);
      }
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      if (source.getEntity() instanceof LivingEntity attacker) {
         if (attacker instanceof Player rider && ridingAny(this.level(), rider)) {
            return false;
         } else if (!canCut(attacker)) {
            level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.HOSTILE, 1.0F, 0.8F);
            return false;
         } else {
            if (this.isGuard()) {
               Entity var15 = this.blockUnderAim(attacker);
               if (var15 != null) {
                  return var15.hurtServer(level, source, amount);
               }
            }

            int cut = this.cutIndex(attacker);
            if (cut < 0) {
               level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.HOSTILE, 1.0F, 0.6F);
               return false;
            } else {
               if (this.getMode() == 7) {
                  this.entityData.set(TARGET, -1);
                  this.entityData.set(AIM, 0.0F);
               }

               boolean whole = cut < 2;
               int from = whole ? 1 : cut;
               net.dabicco.witherstormmod.bowels.SeveredTentacleEntity piece = (net.dabicco.witherstormmod.bowels.SeveredTentacleEntity)net.dabicco.witherstormmod.bowels.ModBowelsEntities.SEVERED_TENTACLE
                  .create(level, EntitySpawnReason.TRIGGERED);
               if (piece != null) {
                  Vec3[] path = this.path(0.0F);
                  Vec3 at = path[Math.min(from, path.length - 1)];
                  piece.setPos(at.x, at.y, at.z);
                  piece.setup(
                     from,
                     this.getBones(),
                     this.getMountYaw(),
                     this.getCurl(),
                     this.getPhase(),
                     this.animClock(0.0F),
                     this.getScale(),
                     this.getOpen(),
                     this.getSway()
                  );
                  Vec3 fling = at.subtract(this.getX(), this.getY(), this.getZ())
                     .normalize()
                     .scale(0.25)
                     .add(attacker.getLookAngle().scale(0.18))
                     .add(0.0, 0.22, 0.0);
                  if (this.isGuard() && BowelsEndRoom.onPedestal(at.x, at.y, at.z)) {
                     Vec3 middle = BowelsEndRoom.daisTop();
                     Vec3 off = at.subtract(middle);
                     off = off.horizontalDistanceSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(off.x, 0.0, off.z).normalize();
                     fling = fling.add(off.scale(0.55)).add(0.0, 0.18, 0.0);
                  }

                  piece.setDeltaMovement(fling);
                  level.addFreshEntity(piece);
               }

               level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.4F, 0.6F);
               level.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), ModSounds.HEAD_CHOMP, SoundSource.HOSTILE, 1.2F, 1.35F);
               level.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY(), this.getZ(), 24, 0.6, 0.6, 0.6, 0.05);
               if (whole) {
                  this.discard();
                  return true;
               } else {
                  this.setBones(cut);
                  this.regrowAt = 0;
                  if (this.isGuard()) {
                     this.entityData.set(OPEN, 1.0F);
                  }

                  this.setMode(4);
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private int cutIndex(LivingEntity attacker) {
      Vec3 eye = attacker.getEyePosition();
      Vec3 look = attacker.getLookAngle();
      Vec3[] path = this.path(0.0F);
      double bestDist = 3.24;
      int best = -1;
      int start = Math.max(2, this.firstFreeBone());

      for (int i = start; i < path.length; i++) {
         Vec3 from = path[i - 1];
         Vec3 to = path[i];

         for (int step = 0; step <= 4; step++) {
            Vec3 p = from.lerp(to, step / 4.0);
            double along = p.subtract(eye).dot(look);
            if (!(along < 0.0) && !(along > 9.0)) {
               double off = p.distanceToSqr(eye.add(look.scale(along)));
               if (off < bestDist) {
                  bestDist = off;
                  best = i;
               }
            }
         }
      }

      return best;
   }

   protected void readAdditionalSaveData(ValueInput input) {
      this.setBones(input.getIntOr("Bones", net.dabicco.witherstormmod.bowels.BowelsTentacleShape.BONES));
      this.setGuard(input.getBooleanOr("Guard", false));
      this.setOrder(input.getIntOr("Order", 0));
      this.setOnEnd(input.getBooleanOr("OnEnd", false));
      this.setWhack(input.getBooleanOr("Whack", false));
      this.entityData.set(EMERGE, input.getFloatOr("Emerge", 0.0F));
      this.anchorX = input.getDoubleOr("AnchorX", this.getX());
      this.anchorY = input.getDoubleOr("AnchorY", this.getY());
      this.anchorZ = input.getDoubleOr("AnchorZ", this.getZ());
      this.outX = input.getDoubleOr("OutX", 0.0);
      this.outZ = input.getDoubleOr("OutZ", 0.0);
      this.travel = input.getDoubleOr("Travel", 0.0);
      this.mouthX = input.getDoubleOr("MouthX", 0.0);
      this.mouthZ = input.getDoubleOr("MouthZ", 0.0);
      this.holeIndex = input.getIntOr("Hole", -1);
      this.setScale(input.getFloatOr("Scale", 1.0F));
      this.entityData.set(MODE, input.getIntOr("Mode", 2));
      this.setCurl(input.getFloatOr("Curl", 0.0F));
      this.setMountYaw(input.getFloatOr("MountYaw", 0.0F));
      this.setPhase(input.getFloatOr("Phase", 0.0F));
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      output.putInt("Bones", this.getBones());
      output.putBoolean("Guard", this.isGuard());
      output.putInt("Order", this.getOrder());
      output.putBoolean("OnEnd", this.isOnEnd());
      output.putBoolean("Whack", this.isWhack());
      output.putFloat("Emerge", this.getEmerge());
      output.putDouble("AnchorX", this.anchorX);
      output.putDouble("AnchorY", this.anchorY);
      output.putDouble("AnchorZ", this.anchorZ);
      output.putDouble("OutX", this.outX);
      output.putDouble("OutZ", this.outZ);
      output.putDouble("Travel", this.travel);
      output.putDouble("MouthX", this.mouthX);
      output.putDouble("MouthZ", this.mouthZ);
      output.putInt("Hole", this.holeIndex);
      output.putFloat("Scale", this.getScale());
      output.putInt("Mode", this.getMode());
      output.putFloat("Curl", this.getCurl());
      output.putFloat("MountYaw", this.getMountYaw());
      output.putFloat("Phase", this.getPhase());
   }
}
