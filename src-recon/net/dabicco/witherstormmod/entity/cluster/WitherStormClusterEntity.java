package net.dabicco.witherstormmod.entity.cluster;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.dabicco.witherstormmod.ClusterBlocksPayload;
import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.client.ClusterMesh;
import net.dabicco.witherstormmod.config.WitherStormConfigs;
import net.dabicco.witherstormmod.config.WitherStormWorldConfig;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WitherStormClusterEntity extends Entity {
   private static final double MISSING_BLOCK_CHANCE = 0.12;
   private static final int MAX_CLUSTER_BLOCKS = 400;
   private static final int SHAKE_DURATION = 20;
   private static final double MAX_BEAM_FOLLOW_SPEED = 1.5;
   private static final double MAX_RELEASE_SPEED = 0.9;
   private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(WitherStormClusterEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_BRIGHTNESS = SynchedEntityData.defineId(WitherStormClusterEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_SCALE = SynchedEntityData.defineId(WitherStormClusterEntity.class, EntityDataSerializers.FLOAT);
   private BlockPos origin;
   private final List<BlockState> blocks = new ArrayList<>();
   private final List<BlockPos> blockOffsets = new ArrayList<>();
   private final List<boolean[]> blockFaceVisibility = new ArrayList<>();
   private net.dabicco.witherstormmod.entity.WitherStormEntity targetStorm;
   private Entity flyTarget;
   private boolean countsForGrowth = true;
   private UUID pendingStormUUID;
   private int stormResolveTicks;
   private int age;
   private static final int STALE_TICKS = 6000;
   private long bornGameTime = -1L;
   private double startDistance = -1.0;
   private double spiralAngle = 0.0;
   private float roll;
   private float oldYaw;
   private float oldPitch;
   private float oldRoll;
   private Vec3 emergeTarget;
   private int travelTicks;
   private float travelProgress;
   private boolean spiralClockwise;
   private int velocityBlendTicks = 0;
   private static final int VELOCITY_BLEND_LENGTH = 15;
   private net.dabicco.witherstormmod.entity.WitherStormHeadEntity beamHead;
   private final List<BlockPos> pendingStragglers = new ArrayList<>();
   private double beamProgress = 0.02;
   private boolean flybySounded;
   private static final double FLYBY_RANGE = 26.0;
   private static final double FLYBY_AFTER = 0.12;
   private Vec3 beamAnchor;
   private static final double BEAM_RELEASE_PROGRESS = 0.8;
   private static final double BEAM_PULL_SPEED = 0.28;
   private static final double BEAM_CLIMB_TICKS = 90.0;
   private static final double MAX_TICK_DISPLACEMENT = 3.0;
   private static final double MAX_ORBIT_DISPLACEMENT = 1.6;
   private boolean spinInitialized = false;
   private float spinYawRate;
   private float spinPitchRate;
   private int radius = 1;
   private boolean hasSplit = false;
   public float clientScale = 1.0F;
   private static final int CLING_TICKS = 26;
   private Vec3 clingOrigin;
   private int clingTicks;
   private Entity targetHole;
   private int debrisNoClipTicks = 0;
   private static final int MAX_STRAGGLERS = 12;
   private final Map<BlockPos, BlockState> offsetToState = new HashMap<>();
   private ClusterMesh clientMesh;
   private boolean invalidOnLoad = false;
   private int blockSyncsSent = 0;

   public void setScavengedBy(Entity collector) {
      this.flyTarget = collector;
      this.countsForGrowth = false;
   }

   private void tickFlybySound() {
      if (!this.flybySounded && !(this.beamProgress < 0.12) && this.level() instanceof ServerLevel server) {
         Player near = server.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 26.0, false);
         if (near != null) {
            this.flybySounded = true;
            server.playSound(
               (Entity)null,
               this.getX(),
               this.getY(),
               this.getZ(),
               ModSounds.CLUSTER_FX,
               SoundSource.HOSTILE,
               1.1F,
               0.92F + server.getRandom().nextFloat() * 0.16F
            );
         }
      }
   }

   public int getBlockCount() {
      return this.blocks.size();
   }

   public WitherStormClusterEntity(EntityType<? extends WitherStormClusterEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public boolean isPushable() {
      return false;
   }

   public boolean ignoreExplosion(Explosion explosion) {
      return true;
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return true;
   }

   protected void defineSynchedData(Builder entityData) {
      entityData.define(DATA_STATE, WitherStormClusterEntity.ClusterState.SHAKING.ordinal());
      entityData.define(DATA_BRIGHTNESS, 1.0F);
      entityData.define(DATA_SCALE, 1.0F);
   }

   public float getRenderScale() {
      return (Float)(Object)(Object)this.entityData.get(DATA_SCALE);
   }

   public WitherStormClusterEntity.ClusterState getClusterState() {
      return WitherStormClusterEntity.ClusterState.values()[this.entityData.get(DATA_STATE)];
   }

   private void setClusterState(WitherStormClusterEntity.ClusterState state) {
      this.entityData.set(DATA_STATE, state.ordinal());
   }

   public void setTargetStorm(net.dabicco.witherstormmod.entity.WitherStormEntity storm) {
      this.targetStorm = storm;
   }

   public void setBeamHead(net.dabicco.witherstormmod.entity.WitherStormHeadEntity head) {
      this.beamHead = head;
   }

   public static BlockPos adjustSpawnOrigin(BlockPos rawOrigin, int radius) {
      return radius <= 0 ? rawOrigin : rawOrigin.above();
   }

   public float getInterpolatedYaw(float partialTick) {
      return this.oldYaw + Mth.wrapDegrees(this.getYRot() - this.oldYaw) * partialTick;
   }

   public float getInterpolatedPitch(float partialTick) {
      return this.oldPitch + Mth.wrapDegrees(this.getXRot() - this.oldPitch) * partialTick;
   }

   public float getInterpolatedRoll(float partialTick) {
      return this.oldRoll + Mth.wrapDegrees(this.roll - this.oldRoll) * partialTick;
   }

   public void tick() {
      this.oldYaw = this.getYRot();
      this.oldPitch = this.getXRot();
      this.oldRoll = this.roll;
      this.xOld = this.getX();
      this.yOld = this.getY();
      this.zOld = this.getZ();
      super.tick();
      this.age++;
      if (!this.level().isClientSide() && this.bornGameTime < 0L) {
         this.bornGameTime = this.level().getGameTime();
      }

      if (!this.level().isClientSide()
         && this.level().getGameTime() - this.bornGameTime > 6000L
         && this.getClusterState() != WitherStormClusterEntity.ClusterState.FALLING) {
         this.targetStorm = null;
         this.beginFalling();
      }

      if (!this.level().isClientSide() && !this.blocks.isEmpty() && (this.blockSyncsSent == 0 || this.blockSyncsSent == 1 && this.age > 20)) {
         this.blockSyncsSent++;
         syncBlocksToTracking(this);
      }

      if (this.level().isClientSide()) {
         this.tickClient();
      } else {
         Vec3 tickStartPos = this.position();
         if (!this.pendingStragglers.isEmpty() && this.level() instanceof ServerLevel stragglerLevel) {
            List<BlockPos> batch = new ArrayList<>(this.pendingStragglers);
            this.pendingStragglers.clear();
            this.spawnStragglers(stragglerLevel, batch);
         }

         this.tickServer();
         WitherStormClusterEntity.ClusterState endState = this.getClusterState();
         double stormSpeed = this.targetStorm != null && this.targetStorm.isAlive() ? this.targetStorm.getDeltaMovement().length() : 0.0;
         double ceiling = endState != WitherStormClusterEntity.ClusterState.FALLING && this.debrisNoClipTicks <= 0 ? 1.6 + stormSpeed : 3.0;
         Vec3 moved = this.position().subtract(tickStartPos);
         double movedLen = moved.length();
         if (movedLen > ceiling) {
            Vec3 capped = tickStartPos.add(moved.scale(ceiling / movedLen));
            this.setPos(capped);
            this.setDeltaMovement(this.getDeltaMovement().scale(ceiling / movedLen));
         }
      }
   }

   private void tickClient() {
      this.setPos(this.position().add(this.getDeltaMovement()));
      WitherStormClusterEntity.ClusterState state = this.getClusterState();
      if (state != WitherStormClusterEntity.ClusterState.SHAKING) {
         if (state == WitherStormClusterEntity.ClusterState.FALLING) {
            this.setYRot(Mth.rotLerp(0.12F, this.getYRot(), 0.0F));
            this.setXRot(Mth.rotLerp(0.12F, this.getXRot(), 0.0F));
            this.roll = Mth.rotLerp(0.12F, this.roll, 0.0F);
         } else {
            if (!this.spinInitialized) {
               this.spinYawRate = (this.random.nextFloat() - 0.5F) * 1.4F;
               this.spinPitchRate = (this.random.nextFloat() - 0.5F) * 0.9F;
               this.spinInitialized = true;
            }

            Vec3 velocity = this.getDeltaMovement();
            if (velocity.lengthSqr() > 0.001) {
               float yaw = (float)(Math.atan2(velocity.z, velocity.x) * (180.0 / Math.PI)) - 90.0F;
               float pitch = (float)(-Math.atan2(velocity.y, Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)) * (180.0 / Math.PI));
               float yawDiff = yaw - this.getYRot();

               while (yawDiff > 180.0F) {
                  yawDiff -= 360.0F;
               }

               while (yawDiff < -180.0F) {
                  yawDiff += 360.0F;
               }

               this.setYRot(this.getYRot() + yawDiff * 0.05F);
               this.setXRot(this.getXRot() + (pitch - this.getXRot()) * 0.05F);
            }

            this.setYRot(this.getYRot() + this.spinYawRate);
            this.setXRot(this.getXRot() + this.spinPitchRate);
            this.roll = this.roll + ((float)velocity.length() * 1.0F + 0.3F);
         }
      }
   }

   private void tickServer() {
      if (this.invalidOnLoad) {
         this.discard();
      } else {
         if (this.targetStorm != null && (!this.targetStorm.isAlive() || this.targetStorm.isRemoved())) {
            this.targetStorm = null;
         }

         if (this.targetStorm != null && this.targetStorm.isCollapsed() && this.getClusterState() != WitherStormClusterEntity.ClusterState.FALLING) {
            Vec3 away = this.position().subtract(this.getStormCenter());
            if (away.lengthSqr() < 1.0E-4) {
               away = new Vec3(this.random.nextDouble() - 0.5, 0.4, this.random.nextDouble() - 0.5);
            }

            this.launchAsDebris(away.normalize().scale(0.55).add(0.0, 0.28, 0.0));
            this.targetStorm = null;
         } else {
            if (this.targetStorm == null && this.pendingStormUUID != null && this.level() instanceof ServerLevel sl) {
               if (this.stormResolveTicks++ % 20 == 0
                  && sl.getEntity(this.pendingStormUUID) instanceof net.dabicco.witherstormmod.entity.WitherStormEntity storm
                  && storm.isAlive()) {
                  this.targetStorm = storm;
                  this.pendingStormUUID = null;
                  if (this.getClusterState() == WitherStormClusterEntity.ClusterState.TRAVELING) {
                     this.beginTraveling();
                  }
               } else if (this.stormResolveTicks > 200) {
                  this.pendingStormUUID = null;
               }
            }

            this.updateSyncedBrightness();
            WitherStormClusterEntity.ClusterState state = this.getClusterState();
            if (state == WitherStormClusterEntity.ClusterState.FALLING) {
               this.tickFalling();
            } else if (state == WitherStormClusterEntity.ClusterState.SHAKING) {
               this.setDeltaMovement((this.random.nextDouble() - 0.5) * 0.1, (this.random.nextDouble() - 0.5) * 0.1, (this.random.nextDouble() - 0.5) * 0.1);
               this.move(MoverType.SELF, this.getDeltaMovement());
               if (this.age >= 20) {
                  this.emergeTarget = this.findNearestAirPocket(this.blockPosition());
                  this.setClusterState(WitherStormClusterEntity.ClusterState.EMERGING);
               }
            } else if (state == WitherStormClusterEntity.ClusterState.EMERGING) {
               this.tickEmerging();
            } else if (state == WitherStormClusterEntity.ClusterState.BEAM_ATTACHED) {
               this.tickBeamAttached();
            } else {
               this.tickTraveling();
            }
         }
      }
   }

   private void updateSyncedBrightness() {
      if (this.tickCount % 4 == 0) {
         float brightness = 1.0F;
         if (this.targetStorm != null && this.targetStorm.isAlive()) {
            double dist = this.position().distanceTo(this.getStormCenter());
            float darkFloor = 0.15F;
            float t = (float)Mth.clamp((dist - 8.0) / 52.0, 0.0, 1.0);
            brightness = darkFloor + (1.0F - darkFloor) * t;
         }

         if (Math.abs((Float)(Object)(Object)this.entityData.get(DATA_BRIGHTNESS) - brightness) > 0.02F) {
            this.entityData.set(DATA_BRIGHTNESS, brightness);
         }
      }
   }

   private void tickBeamAttached() {
      boolean beamGone = this.beamHead == null || !this.beamHead.isAlive() || !this.beamHead.isBeamActive();
      if (!beamGone && this.targetStorm == null) {
         this.resolveStormFromBeamHead();
      }

      if (beamGone) {
         this.releaseFromBeam();
      } else {
         Vec3 groundEnd = this.beamHead.getBeamEndExact();
         if (this.beamAnchor == null) {
            this.beamAnchor = groundEnd;
         } else {
            this.beamAnchor = this.beamAnchor.lerp(groundEnd, 0.15);
         }

         Vec3 headPos = this.beamHead.position();
         double beamLength = Math.max(headPos.distanceTo(this.beamAnchor), 1.0);
         double pull = Math.max(0.28, beamLength / 90.0);
         this.beamProgress += pull / beamLength;
         this.tickFlybySound();
         if (this.beamProgress >= 0.8) {
            this.releaseFromBeam();
         } else {
            Vec3 basePos = this.beamAnchor.lerp(headPos, this.beamProgress);
            Vec3 axis = headPos.subtract(this.beamAnchor).normalize();
            Vec3 side = axis.cross(new Vec3(0.0, 1.0, 0.0));
            if (side.lengthSqr() < 1.0E-4) {
               side = new Vec3(1.0, 0.0, 0.0);
            }

            side = side.normalize();
            Vec3 side2 = side.cross(axis).normalize();
            double swirlAngle = this.age * 0.12 + this.getId() * 1.7;
            double swirlRadius = (1.0 - this.beamProgress) * 1.2;
            Vec3 swirl = side.scale(Math.cos(swirlAngle) * swirlRadius).add(side2.scale(Math.sin(swirlAngle) * swirlRadius));
            Vec3 newPos = basePos.add(swirl);
            Vec3 delta = newPos.subtract(this.position());
            if (delta.length() > 1.5) {
               delta = delta.normalize().scale(1.5);
            }

            this.setDeltaMovement(delta);
            this.setPos(this.position().add(delta));
         }
      }
   }

   private void resolveStormFromBeamHead() {
      if (this.level() instanceof ServerLevel server
         && this.beamHead != null
         && this.beamHead.getStormUUID() != null
         && server.getEntity(this.beamHead.getStormUUID()) instanceof net.dabicco.witherstormmod.entity.WitherStormEntity storm) {
         this.targetStorm = storm;
      }
   }

   private void releaseFromBeam() {
      this.resolveStormFromBeamHead();
      this.beamHead = null;
      this.beamAnchor = null;
      this.beamProgress = 0.02;
      Vec3 vel = this.getDeltaMovement();
      if (vel.length() > 0.9) {
         this.setDeltaMovement(vel.normalize().scale(0.9));
      }

      this.velocityBlendTicks = 15;
      this.beginTraveling();
   }

   private void tickEmerging() {
      if (this.emergeTarget == null) {
         this.emergeTarget = this.position().add(0.0, 3.0, 0.0);
      }

      if (this.clingOrigin == null) {
         this.clingOrigin = this.position();
      }

      this.clingTicks++;
      float loose = Mth.clamp(this.clingTicks / 26.0F, 0.0F, 1.0F);
      if (this.isInOpenAir() && loose >= 1.0F) {
         this.releaseFromGround();
      } else {
         Vec3 toTarget = this.emergeTarget.subtract(this.position());
         double dist = toTarget.length();
         if (dist < 0.4 && loose >= 1.0F) {
            this.releaseFromGround();
         } else {
            double pull = 0.012 + 0.055 * (loose * loose);
            Vec3 desired = toTarget.normalize().scale(pull);
            float strainAmp = 0.035F * loose * (1.0F - loose) * 4.0F;
            double phase = this.age * 1.9 + this.getId();
            Vec3 strain = new Vec3(Math.sin(phase) * strainAmp, Math.sin(phase * 1.37 + 1.1) * strainAmp * 0.5, Math.cos(phase * 0.83) * strainAmp);
            Vec3 tether = this.clingOrigin.subtract(this.position()).scale(0.12 * (1.0F - loose));
            Vec3 steering = desired.add(strain).add(tether).subtract(this.getDeltaMovement()).scale(0.25);
            this.setDeltaMovement(this.getDeltaMovement().add(steering));
            this.setPos(this.position().add(this.getDeltaMovement()));
         }
      }
   }

   private void releaseFromGround() {
      Vec3 out = this.emergeTarget != null && this.emergeTarget.distanceToSqr(this.position()) > 0.01
         ? this.emergeTarget.subtract(this.position()).normalize()
         : new Vec3(0.0, 1.0, 0.0);
      this.setDeltaMovement(this.getDeltaMovement().add(out.scale(0.22)));
      this.hurtMarked = true;
      this.spinInitialized = false;
      this.beginTraveling();
   }

   private Vec3 getStormCenter() {
      return this.flyTarget != null && this.flyTarget.isAlive() ? this.flyTarget.getBoundingBox().getCenter() : this.targetStorm.getBoundingBox().getCenter();
   }

   private void beginTraveling() {
      if (this.beamHead != null && this.beamHead.isAlive() && this.beamHead.isBeamActive()) {
         this.setClusterState(WitherStormClusterEntity.ClusterState.BEAM_ATTACHED);
         this.travelTicks = 0;
      } else {
         this.setClusterState(WitherStormClusterEntity.ClusterState.TRAVELING);
         this.travelTicks = 0;
         this.travelProgress = 0.0F;
         this.spiralClockwise = (this.getId() & 1) == 0;
         if (this.targetStorm != null) {
            Vec3 stormCenter = this.getStormCenter();
            Vec3 offset = this.position().subtract(stormCenter);
            this.spiralAngle = Math.atan2(offset.z, offset.x);
            this.startDistance = Math.max(offset.length(), 0.001);
         }
      }
   }

   public void setTargetHole(Entity hole) {
      this.targetHole = hole;
   }

   private void tickTraveling() {
      this.travelTicks++;
      if (this.targetStorm == null) {
         if (this.targetHole != null && this.targetHole.isAlive()) {
            this.setPos(this.position().add(this.getDeltaMovement()));
         } else if (this.pendingStormUUID != null) {
            this.setPos(this.position().add(this.getDeltaMovement()));
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
         } else {
            this.beginFalling();
         }
      } else {
         WitherStormWorldConfig config = WitherStormConfigs.get(this.level());
         Vec3 stormCenter = this.getStormCenter();
         Vec3 offset = this.position().subtract(stormCenter);
         double distance = offset.length();
         double stormSpeed = this.targetStorm.getDeltaMovement().length();
         double maxRange = Math.max(config.pickupRange() * 2.0, 160.0) + stormSpeed * 120.0;
         if (distance > maxRange) {
            this.beginFalling();
         } else {
            this.travelProgress = 1.0F - Mth.clamp((float)(distance / this.startDistance), 0.0F, 1.0F);
            this.trySplit(distance);
            if (!this.isRemoved()) {
               if (distance > 0.001) {
                  double horizDist = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
                  double inwardSpeed = config.maxClusterSpeed;
                  double tangentialSpeed = Math.min(config.spiralStrength * 25.0, 1.2);
                  double angularStep = tangentialSpeed / Math.max(horizDist, 2.0);
                  if (!this.spiralClockwise) {
                     angularStep = -angularStep;
                  }

                  this.spiralAngle = Math.atan2(offset.z, offset.x) + angularStep;
                  double VY_MAX = 0.5;
                  double ticksHoriz = horizDist / Math.max(inwardSpeed, 0.01);
                  double ticksVert = Math.abs(offset.y) / 0.5;
                  if (ticksVert > ticksHoriz && ticksVert > 1.0) {
                     inwardSpeed = Math.max(horizDist / ticksVert, 0.05);
                  }

                  double vy = Mth.clamp(-offset.y / Math.max(ticksHoriz, 1.0), -0.5, 0.5);
                  double newYOff = offset.y + vy;
                  double newHoriz = Math.max(horizDist - inwardSpeed, 0.0);
                  Vec3 newPos = new Vec3(
                     stormCenter.x + Math.cos(this.spiralAngle) * newHoriz, stormCenter.y + newYOff, stormCenter.z + Math.sin(this.spiralAngle) * newHoriz
                  );
                  Vec3 desiredVelocity = newPos.subtract(this.position());
                  double maxSpeed = inwardSpeed + tangentialSpeed + 0.4 + stormSpeed;
                  if (desiredVelocity.length() > maxSpeed) {
                     desiredVelocity = desiredVelocity.normalize().scale(maxSpeed);
                  }

                  if (this.velocityBlendTicks > 0) {
                     float blend = 1.0F - this.velocityBlendTicks / 15.0F;
                     desiredVelocity = this.getDeltaMovement().lerp(desiredVelocity, blend);
                     this.velocityBlendTicks--;
                  }

                  double prevSpeed = this.getDeltaMovement().length();
                  double speedCap = Math.min(maxSpeed, Math.max(prevSpeed + 0.035, stormSpeed + 0.1));
                  if (desiredVelocity.length() > speedCap && speedCap > 0.01) {
                     desiredVelocity = desiredVelocity.normalize().scale(speedCap);
                  }

                  if (desiredVelocity.length() > maxSpeed) {
                     desiredVelocity = desiredVelocity.normalize().scale(maxSpeed);
                  }

                  this.setDeltaMovement(desiredVelocity);
               }

               this.setPos(this.position().add(this.getDeltaMovement()));
               double shrinkRange = Math.max(6.0, (double)config.absorptionRadius);
               if (distance < shrinkRange) {
                  float shrink = (float)Mth.clamp((distance - 2.0) / (shrinkRange - 2.0), 0.05, 1.0);
                  this.entityData.set(DATA_SCALE, Math.min((Float)(Object)(Object)this.entityData.get(DATA_SCALE), shrink));
               }

               boolean tookTooLong = this.travelTicks > 1800;
               if (distance < 3.5 || tookTooLong) {
                  if (this.countsForGrowth) {
                     this.targetStorm.addSubGrowth(this.getBlockCount());
                  }

                  this.discard();
               }
            }
         }
      }
   }

   private void trySplit(double distanceToStorm) {
      if (!this.hasSplit && this.radius >= 2 && this.blocks.size() >= 8 && this.targetStorm != null && this.travelTicks >= 15 && !(distanceToStorm < 12.0)) {
         float chance = Mth.clamp(0.008F + this.blocks.size() * 0.0011F, 0.008F, 0.16F);
         if (!(this.random.nextFloat() > chance) && this.level() instanceof ServerLevel server) {
            double var18 = this.random.nextDouble() * Math.PI * 2.0;
            Vec3 axis = new Vec3(Math.cos(var18), 0.0, Math.sin(var18));
            double centre = 0.0;

            for (BlockPos o : this.blockOffsets) {
               centre += o.getX() * axis.x + o.getZ() * axis.z;
            }

            centre /= Math.max(1, this.blockOffsets.size());
            List<BlockState> loB = new ArrayList<>();
            List<BlockState> hiB = new ArrayList<>();
            List<BlockPos> loO = new ArrayList<>();
            List<BlockPos> hiO = new ArrayList<>();

            for (int i = 0; i < this.blockOffsets.size(); i++) {
               BlockPos o = this.blockOffsets.get(i);
               double v = o.getX() * axis.x + o.getZ() * axis.z;
               if (v < centre) {
                  loB.add(this.blocks.get(i));
                  loO.add(o);
               } else {
                  hiB.add(this.blocks.get(i));
                  hiO.add(o);
               }
            }

            if (!loB.isEmpty() && !hiB.isEmpty()) {
               double sep = Math.max(1.0, this.radius * 0.6);
               this.spawnHalf(server, loB, loO, this.position().subtract(axis.scale(sep)));
               this.spawnHalf(server, hiB, hiO, this.position().add(axis.scale(sep)));
               this.hasSplit = true;
               this.discard();
            }
         }
      }
   }

   private void spawnHalf(ServerLevel server, List<BlockState> halfBlocks, List<BlockPos> halfOffsets, Vec3 pos) {
      long sx = 0L;
      long sy = 0L;
      long sz = 0L;

      for (BlockPos o : halfOffsets) {
         sx += o.getX();
         sy += o.getY();
         sz += o.getZ();
      }

      int n = halfOffsets.size();
      int cx = Math.round((float)sx / n);
      int cy = Math.round((float)sy / n);
      int cz = Math.round((float)sz / n);
      WitherStormClusterEntity child = new WitherStormClusterEntity(net.dabicco.witherstormmod.entity.ModEntityTypes.WITHER_STORM_CLUSTER, server);
      child.setRadius(Math.max(1, this.radius - 1));
      child.hasSplit = true;
      child.blocks.addAll(halfBlocks);

      for (BlockPos o : halfOffsets) {
         child.blockOffsets.add(o.offset(-cx, -cy, -cz));
      }

      child.recomputeFaceVisibility();
      child.setOrigin(this.blockPosition());
      child.setPos(pos.x, pos.y, pos.z);
      child.setTargetStorm(this.targetStorm);
      child.flyTarget = this.flyTarget;
      child.countsForGrowth = this.countsForGrowth;
      child.setDeltaMovement(this.getDeltaMovement());
      child.beginTraveling();
      server.addFreshEntity(child);
      syncBlocksToTracking(child);
   }

   public void launchAsDebris(Vec3 velocity) {
      this.setClusterState(WitherStormClusterEntity.ClusterState.FALLING);
      this.noPhysics = true;
      this.debrisNoClipTicks = 12;
      this.setDeltaMovement(velocity);
      this.hurtMarked = true;
   }

   private void beginFalling() {
      this.setClusterState(WitherStormClusterEntity.ClusterState.FALLING);
      this.noPhysics = false;
   }

   private void tickFalling() {
      if (this.debrisNoClipTicks > 0 && --this.debrisNoClipTicks == 0) {
         this.noPhysics = false;
      }

      Vec3 vel = this.getDeltaMovement().multiply(0.9, 1.0, 0.9).add(0.0, -0.05, 0.0);
      if (vel.y < -1.2) {
         vel = new Vec3(vel.x, -1.2, vel.z);
      }

      this.setDeltaMovement(vel);
      this.move(MoverType.SELF, this.getDeltaMovement());
      if (this.onGround() || this.verticalCollision) {
         this.placeBlocksAtRest();
         this.discard();
      } else if (this.age > 2400) {
         this.discard();
      }
   }

   private void placeBlocksAtRest() {
      if (this.level() instanceof ServerLevel server && !this.blocks.isEmpty()) {
         double yawRad = Math.toRadians(this.getYRot());
         double pitchRad = Math.toRadians(this.getXRot());
         double rollRad = Math.toRadians(this.roll);
         double cy = Math.cos(yawRad);
         double sy = Math.sin(yawRad);
         double cp = Math.cos(pitchRad);
         double sp = Math.sin(pitchRad);
         double cr = Math.cos(rollRad);
         double sr = Math.sin(rollRad);
         Vec3 base = this.position();

         for (int i = 0; i < this.blocks.size(); i++) {
            BlockState state = this.blocks.get(i);
            if (!state.isAir()) {
               BlockPos off = this.blockOffsets.get(i);
               double x = off.getX();
               double y = off.getY();
               double z = off.getZ();
               double rx = x * cr - y * sr;
               double ry = x * sr + y * cr;
               double py = ry * cp - z * sp;
               double pz = ry * sp + z * cp;
               double wx = rx * cy - pz * sy;
               double wz = rx * sy + pz * cy;
               BlockPos target = BlockPos.containing(base.x + wx, base.y + py, base.z + wz);
               BlockState existing = server.getBlockState(target);
               if (existing.isAir() || existing.canBeReplaced()) {
                  server.setBlockAndUpdate(target, state);
               }
            }
         }
      }
   }

   private boolean isInOpenAir() {
      BlockPos pos = this.blockPosition();
      BlockState state = this.level().getBlockState(pos);
      return state.isAir() || state.getCollisionShape(this.level(), pos).isEmpty();
   }

   private Vec3 findNearestAirPocket(BlockPos start) {
      Set<BlockPos> visited = new HashSet<>();
      ArrayDeque<BlockPos> queue = new ArrayDeque<>();
      queue.add(start);
      visited.add(start);
      int maxChecks = 400;
      int checks = 0;

      while (!queue.isEmpty() && checks < maxChecks) {
         BlockPos pos = queue.poll();
         checks++;
         BlockState state = this.level().getBlockState(pos);
         if (state.isAir() || state.getCollisionShape(this.level(), pos).isEmpty()) {
            return Vec3.atCenterOf(pos);
         }

         BlockPos[] neighbors = new BlockPos[]{pos.above(), pos.below(), pos.north(), pos.south(), pos.east(), pos.west()};

         for (BlockPos neighbor : neighbors) {
            if (neighbor.distSqr(start) <= 64.0 && visited.add(neighbor)) {
               queue.add(neighbor);
            }
         }
      }

      return null;
   }

   public float getProgress() {
      return this.getClusterState() != WitherStormClusterEntity.ClusterState.TRAVELING ? 0.0F : this.travelProgress;
   }

   public float getDarknessFactor() {
      return (Float)(Object)(Object)this.entityData.get(DATA_BRIGHTNESS);
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
      return false;
   }

   public void setRadius(int radius) {
      this.radius = radius;
   }

   public int getRadius() {
      return this.radius;
   }

   public void absorbBlocks(BlockPos center) {
      this.blocks.clear();
      this.blockOffsets.clear();
      Set<BlockPos> blocksToRemove = new HashSet<>();
      if (this.radius > 0) {
         center = center.above();
      }

      if (this.radius <= 0) {
         BlockState state = this.level().getBlockState(center);
         if (this.isValidClusterBlock(state, center)) {
            this.blocks.add(state);
            this.blockOffsets.add(BlockPos.ZERO);
            blocksToRemove.add(center);
         }
      } else {
         int half = this.radius;
         double sphereRadius = this.radius;
         double sphereRadiusSq = sphereRadius * sphereRadius;
         int blockCount = 0;
         List<BlockPos> skipped = new ArrayList<>();

         label89:
         for (int x = -half; x < half; x++) {
            for (int y = -half; y < half; y++) {
               for (int z = -half; z < half; z++) {
                  if (blockCount >= 400) {
                     break label89;
                  }

                  double dx = x + 0.5;
                  double dy = y + 0.5;
                  double dz = z + 0.5;
                  if (!(dx * dx + dy * dy + dz * dz > sphereRadiusSq)) {
                     BlockPos pos = center.offset(x, y, z);
                     BlockState state = this.level().getBlockState(pos);
                     if (this.isValidClusterBlock(state, pos)) {
                        if (this.random.nextDouble() < 0.12) {
                           skipped.add(pos);
                        } else {
                           this.blocks.add(state);
                           this.blockOffsets.add(new BlockPos(x, y, z));
                           blocksToRemove.add(pos);
                           blockCount++;
                        }
                     }
                  }
               }
            }
         }

         if (this.radius >= 1 && !skipped.isEmpty()) {
            this.pendingStragglers.addAll(skipped);
         }
      }

      int CLIENTS_ONLY = 2;
      BlockState air = Blocks.AIR.defaultBlockState();

      for (BlockPos pos : blocksToRemove) {
         this.level().setBlock(pos, air, 2);
      }

      if (this.level() instanceof ServerLevel fluidLevel) {
         for (BlockPos pos : blocksToRemove) {
            for (Direction dir : Direction.values()) {
               BlockPos side = pos.relative(dir);
               if (!blocksToRemove.contains(side)) {
                  FluidState fluid = fluidLevel.getFluidState(side);
                  if (!fluid.isEmpty()) {
                     fluidLevel.scheduleTick(side, fluid.getType(), fluid.getType().getTickDelay(fluidLevel));
                  }
               }
            }
         }
      }

      this.recomputeFaceVisibility();
   }

   private void spawnStragglers(ServerLevel server, List<BlockPos> skipped) {
      int budget = Math.min(skipped.size(), 12);

      for (int i = 0; i < budget; i++) {
         BlockPos pos = skipped.get(i);
         if (this.isValidClusterBlock(server.getBlockState(pos), pos)) {
            WitherStormClusterEntity straggler = new WitherStormClusterEntity(net.dabicco.witherstormmod.entity.ModEntityTypes.WITHER_STORM_CLUSTER, server);
            straggler.setRadius(0);
            straggler.setOrigin(pos);
            straggler.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (this.targetStorm != null) {
               straggler.setTargetStorm(this.targetStorm);
            } else {
               net.dabicco.witherstormmod.entity.WitherStormEntity nearest = (net.dabicco.witherstormmod.entity.WitherStormEntity)server.getNearestEntity(
                  net.dabicco.witherstormmod.entity.WitherStormEntity.class,
                  TargetingConditions.forNonCombat().ignoreLineOfSight().range(256.0),
                  (LivingEntity)null,
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  this.getBoundingBox().inflate(256.0)
               );
               if (nearest != null) {
                  straggler.setTargetStorm(nearest);
               }
            }

            if (this.beamHead != null) {
               straggler.setBeamHead(this.beamHead);
            }

            server.addFreshEntity(straggler);
            straggler.absorbBlocks(pos);
            syncBlocksToTracking(straggler);
         }
      }
   }

   private boolean isValidClusterBlock(BlockState state, BlockPos pos) {
      if (state.isAir()) {
         return false;
      } else {
         return !state.getFluidState().isEmpty() && WitherStormConfigs.get(this.level()).clustersTakeLiquids == 0
            ? false
            : !(state.getDestroySpeed(this.level(), pos) < 0.0F);
      }
   }

   public Map<BlockPos, BlockState> getOffsetToState() {
      return this.offsetToState;
   }

   public ClusterMesh getOrBakeMesh() {
      if (this.clientMesh == null && !this.blocks.isEmpty() && this.level() instanceof ClientLevel clientLevel) {
         this.clientMesh = ClusterMesh.bake(this.blocks, this.blockOffsets, this.blockFaceVisibility, clientLevel, this.blockPosition());
      }

      return this.clientMesh;
   }

   private void recomputeFaceVisibility() {
      this.blockFaceVisibility.clear();
      this.offsetToState.clear();
      this.clientMesh = null;

      for (int i = 0; i < this.blockOffsets.size() && i < this.blocks.size(); i++) {
         this.offsetToState.put(this.blockOffsets.get(i), this.blocks.get(i));
      }

      BlockState air = Blocks.AIR.defaultBlockState();

      for (int i = 0; i < this.blockOffsets.size(); i++) {
         BlockPos offset = this.blockOffsets.get(i);
         BlockState own = i < this.blocks.size() ? this.blocks.get(i) : air;
         boolean[] visible = new boolean[6];

         for (Direction direction : Direction.values()) {
            BlockState neighbour = this.offsetToState.getOrDefault(offset.relative(direction), air);
            visible[direction.ordinal()] = Block.shouldRenderFace(own, neighbour, direction);
         }

         this.blockFaceVisibility.add(visible);
      }
   }

   public List<boolean[]> getBlockFaceVisibility() {
      return this.blockFaceVisibility;
   }

   public void setOrigin(BlockPos pos) {
      this.origin = pos;
   }

   public List<BlockState> getBlocks() {
      return this.blocks;
   }

   public float getRoll() {
      return this.roll;
   }

   public List<BlockPos> getBlockOffsets() {
      return this.blockOffsets;
   }

   public List<AABB> getCollisionBoxes() {
      WitherStormClusterEntity.ClusterState st = this.getClusterState();
      if (st == WitherStormClusterEntity.ClusterState.SHAKING || st == WitherStormClusterEntity.ClusterState.EMERGING) {
         return Collections.emptyList();
      } else if (this.blockOffsets.isEmpty()) {
         return Collections.emptyList();
      } else {
         float scale = this.getRenderScale();
         if (scale < 0.35F) {
            return Collections.emptyList();
         } else {
            double yaw = Math.toRadians(this.getYRot());
            double pitch = Math.toRadians(this.getXRot());
            double cy = Math.cos(yaw);
            double sy = Math.sin(yaw);
            double cp = Math.cos(pitch);
            double sp = Math.sin(pitch);
            Vec3 origin = this.position();
            double half = 0.5 * scale;
            List<AABB> out = new ArrayList<>(this.blockOffsets.size());

            for (BlockPos off : this.blockOffsets) {
               double lx = off.getX() * scale;
               double ly = (off.getY() + 0.5) * scale;
               double lz = off.getZ() * scale;
               double py = ly * cp - lz * sp;
               double pz = ly * sp + lz * cp;
               double wx = lx * cy + pz * sy;
               double wz = -lx * sy + pz * cy;
               double cxw = origin.x + wx;
               double cyw = origin.y + py;
               double czw = origin.z + wz;
               out.add(new AABB(cxw - half, cyw - half, czw - half, cxw + half, cyw + half, czw + half));
            }

            return out;
         }
      }
   }

   private void updateRotation() {
      Vec3 velocity = this.getDeltaMovement();
      if (velocity.lengthSqr() > 0.001) {
         float yaw = (float)(Math.atan2(velocity.z, velocity.x) * (180.0 / Math.PI)) - 90.0F;
         float pitch = (float)(-Math.atan2(velocity.y, Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)) * (180.0 / Math.PI));
         float yawDiff = yaw - this.getYRot();

         while (yawDiff > 180.0F) {
            yawDiff -= 360.0F;
         }

         while (yawDiff < -180.0F) {
            yawDiff += 360.0F;
         }

         this.setYRot(this.getYRot() + yawDiff * 0.15F);
         this.setXRot(this.getXRot() + (pitch - this.getXRot()) * 0.15F);
      }
   }

   protected void readAdditionalSaveData(ValueInput input) {
      this.bornGameTime = input.getLongOr("Born", -1L);
      this.radius = input.getIntOr("Radius", this.radius);
      this.blocks.clear();
      this.blockOffsets.clear();
      Optional<List<BlockState>> blocksOpt = input.read("Blocks", BlockState.CODEC.listOf());
      blocksOpt.ifPresent(this.blocks::addAll);
      Optional<List<BlockPos>> offsetsOpt = input.read("Offsets", BlockPos.CODEC.listOf());
      offsetsOpt.ifPresent(this.blockOffsets::addAll);
      if (!this.blocks.isEmpty() && this.blockOffsets.size() == this.blocks.size()) {
         this.recomputeFaceVisibility();
         String storm = input.getStringOr("StormUUID", "");
         this.pendingStormUUID = storm.isEmpty() ? null : UUID.fromString(storm);
         int stateOrd = input.getIntOr("ClusterState", WitherStormClusterEntity.ClusterState.TRAVELING.ordinal());
         WitherStormClusterEntity.ClusterState saved = WitherStormClusterEntity.ClusterState.values()[Math.min(
            Math.max(stateOrd, 0), WitherStormClusterEntity.ClusterState.values().length - 1
         )];
         if (saved == WitherStormClusterEntity.ClusterState.FALLING) {
            this.beginFalling();
         } else {
            this.setClusterState(WitherStormClusterEntity.ClusterState.TRAVELING);
         }
      } else {
         this.invalidOnLoad = true;
      }
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      output.putLong("Born", this.bornGameTime);
      output.putInt("Radius", this.radius);
      output.putInt("ClusterState", this.getClusterState().ordinal());
      if (this.targetStorm != null && this.targetStorm.isAlive()) {
         output.putString("StormUUID", this.targetStorm.getUUID().toString());
      } else if (this.pendingStormUUID != null) {
         output.putString("StormUUID", this.pendingStormUUID.toString());
      }

      output.store("Blocks", BlockState.CODEC.listOf(), List.copyOf(this.blocks));
      output.store("Offsets", BlockPos.CODEC.listOf(), List.copyOf(this.blockOffsets));
   }

   public static void syncBlocksToTracking(WitherStormClusterEntity cluster) {
      for (ServerPlayer player : PlayerLookup.tracking(cluster)) {
         cluster.sendBlocksTo(player);
      }
   }

   public void sendBlocksTo(ServerPlayer player) {
      if (!this.blocks.isEmpty()) {
         List<Integer> stateIds = new ArrayList<>();

         for (BlockState state : this.blocks) {
            stateIds.add(Block.getId(state));
         }

         ServerPlayNetworking.send(player, new ClusterBlocksPayload(this.getId(), stateIds, new ArrayList<>(this.blockOffsets)));
      }
   }

   public void setClientBlockData(List<BlockState> states, List<BlockPos> offsets) {
      this.blocks.clear();
      this.blocks.addAll(states);
      this.blockOffsets.clear();
      this.blockOffsets.addAll(offsets);
      this.recomputeFaceVisibility();
   }

   public static enum ClusterState {
      SHAKING,
      EMERGING,
      TRAVELING,
      BEAM_ATTACHED,
      FALLING;

      private static WitherStormClusterEntity.ClusterState[] $values() {
         return new WitherStormClusterEntity.ClusterState[]{SHAKING, EMERGING, TRAVELING, BEAM_ATTACHED, FALLING};
      }
   }
}
