package net.dabicco.witherstormmod.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.dabicco.witherstormmod.ModAdvancements;
import net.dabicco.witherstormmod.ModEffects;
import net.dabicco.witherstormmod.ModEnchantments;
import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.config.WitherStormConfigs;
import net.dabicco.witherstormmod.config.WitherStormWorldConfig;
import net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity;
import net.dabicco.witherstormmod.entity.withered.WitheredMobs;
import net.dabicco.witherstormmod.mixin.FireworkRocketEntityAccessor;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class WitherStormHeadEntity extends Entity {
   private static final EntityDataAccessor<Integer> STORM_ID;
   private static final EntityDataAccessor<Integer> HEAD_INDEX_DATA;
   private static final EntityDataAccessor<Float> HEAD_LOCAL_YAW;
   private static final EntityDataAccessor<Float> HEAD_ROLL;
   private static final EntityDataAccessor<Long> SPAWN_GAME_TIME;
   private static final EntityDataAccessor<Long> FIRE_START_TIME;
   private static final EntityDataAccessor<Long> HURT_START_TIME;
   private static final EntityDataAccessor<Long> ROAR_START_TIME;
   private static final EntityDataAccessor<Long> VOCAL_TIME;
   private static final EntityDataAccessor<Integer> SUCKED_ID;
   private static final EntityDataAccessor<Optional<BlockPos>> BEAM_END;
   private static final EntityDataAccessor<Vector3fc> BEAM_END_EXACT;
   public static final int SPAWN_ANIM_LENGTH_TICKS = 48;
   public static final float SPAWN_ANIM_TIME_SCALE = 0.6666667F;
   public static final int FIRE_ANIM_LENGTH_TICKS = 25;
   public static final int HURT_ANIM_LENGTH_TICKS = 43;
   private static final int DAMAGED_DURATION_TICKS = 600;
   private int fireCooldown = 100 + (int)(Math.random() * (double)100.0F);
   private static final float TURN_SMOOTH_TIME = 1.0F;
   private float yawVel = 0.0F;
   private float pitchVel = 0.0F;
   private float headRoll = 0.0F;
   private float rollVel = 0.0F;
   private static final float ROLL_PER_YAWVEL = 0.55F;
   private static final float MAX_ROLL = 32.0F;
   private static final float ROLL_SMOOTH_TIME = 0.9F;
   private static final float ROLL_IDLE_AMP = 3.0F;
   private float filteredWantYaw = Float.NaN;
   private float filteredWantPitch = Float.NaN;
   private int startleTicks = 0;
   public float clientLocalYaw = Float.NaN;
   public float clientPitch = Float.NaN;
   public long clientSmoothLastMillis = 0L;
   public float clientLegacyYaw = Float.NaN;
   private UUID stormUUID;
   private int headIndex;
   private float baseYaw = 0.0F;
   private float baseRoll = 0.0F;
   private boolean distressed = false;
   private static final float DISTRESS_OVERREACH = 22.0F;
   private static final float MAX_YAW_ANGLE = 90.0F;
   private static final float MAX_PITCH_ANGLE = 90.0F;
   private static final double FIREWORK_NOTICE_RANGE = (double)90.0F;
   private static final int FIREWORK_SCAN_INTERVAL = 10;
   private static final int FIREWORK_HOLD_TICKS = 60;
   private float yawRange = 90.0F;
   private static final EntityDataAccessor<Float> LIT;
   private LivingEntity target;
   private int targetlessTicks = 0;
   private static final int TARGETLESS_FIRE_DELAY = 100;
   private int retargetCooldown = 0;
   private int targetUnseenTicks = 0;
   private int orphanTicks = 0;
   private static final int ORPHAN_DISCARD_TICKS = 200;
   private static final int TARGET_UNSEEN_DROP_TICKS = 60;
   private static final int LOS_CHECK_INTERVAL = 10;
   private static final int MAX_LOS_CHECKS_PER_SEARCH = 8;
   private static final double TARGET_SEARCH_RANGE = (double)96.0F;
   private static final double TARGET_DROP_RANGE = (double)128.0F;
   private static final int RETARGET_INTERVAL = 20;
   private int wanderRetimer = 0;
   private float wanderYaw = 0.0F;
   private float wanderPitch = 0.0F;
   private static final double MIN_BEAM_SEP = (double)9.0F;
   private static final float MAX_SEP_NUDGE = 22.0F;
   private float jawAngle = 0.0F;
   private float jawVel = 0.0F;
   private float prevYawForJaw = 0.0F;
   private static final float JAW_STIFFNESS = 180.0F;
   private static final float JAW_DAMPING = 14.0F;
   private static final float JAW_TURN_INFLUENCE = 2.5F;
   private static final float JAW_MAX = 25.0F;
   private static final float JAW_TURN_DEADBAND = 1.5F;
   private static final float HURT_PITCH = 0.9F;
   private static final float ROAR_PITCH = 0.94F;
   private static final float ROAR_PITCH_DROP = 0.08F;
   public static final int BEAM_GROUND_RADIUS = 3;
   private static final int BEAM_CLUSTER_INTERVAL = 70;
   private static final double BEAM_MAX_LENGTH = (double)120.0F;
   private int beamClusterTimer = 40;
   public Vec3 clientBeamEnd;
   private Vec3 forcedLookPoint;
   private int forcedLookTicks;
   private final List<Echo> pendingEchoes = new ArrayList();
   private int vocalCooldown = 100 + (int)(Math.random() * (double)260.0F);
   private long lastVocalKick = -1L;
   private boolean beamWasActive = false;
   private Vec3 lastBeamExact;
   private int beamSnapCooldown = 0;
   private boolean beamOutputOn = true;
   private boolean beamDesiredOn = true;
   private int beamMoodTimer = 0;
   private int beamFlickerTicks = 0;
   private int beamOffSoundCooldown = 0;
   private int skullShotDelay = -1;
   private static final HashMap<UUID, Long> justEscaped;
   private static final long ESCAPE_GRACE_TICKS = 30L;
   private static final HashMap<UUID, Long> FORGIVEN_UNTIL;
   private static final long MAX_FORGIVE_TICKS = 12000L;
   private final List<LivingEntity> victims = new ArrayList();
   private final Map<Player, Vec3> lastPos = new IdentityHashMap();
   private final Map<Player, Vec3> lastPullVelMap = new IdentityHashMap();
   private final Map<Player, Integer> heldTicks = new IdentityHashMap();
   private static final int SWIM_DELAY_TICKS = 60;
   private LivingEntity suckedEntity;
   private Vec3 lastVictimPos;
   private int pullPacketTick;
   private Vec3 lastPullVel;
   private static final double SIDEWAYS_MAX = 0.42;
   private int chompTicks;
   private static final int CHOMP_KILL_TICK = 20;
   private static final int CHOMP_LENGTH = 25;
   public static final ResourceKey<DamageType> CHOMP_DAMAGE;
   public static final int SONIC_DISABLE_TICKS = 240;
   private int sonicDisableTicks;
   private static final int DEFIANCE_INTERVAL = 25;
   private int defianceCooldown;
   private UUID wardenCharging;
   private int wardenChargeTicks;
   private static final int WARDEN_CHARGE_TICKS = 80;
   private static final int WARDEN_RECHARGE_TICKS = 500;
   private int wardenCooldown;
   private ResourceKey<DamageType> chompDamageOverride;
   private static final Vec3 DEFAULT_MODEL_OFFSET;

   private float hostPitchRange() {
      Entity var2 = this.level().getEntity(this.getStormId());
      float var10000;
      if (var2 instanceof StormHeadHost host) {
         var10000 = Math.min(90.0F, host.headPitchRangeFor(this.getHeadIndex()));
      } else {
         var10000 = 90.0F;
      }

      return var10000;
   }

   private void tickFireworkDistraction(ServerLevel server) {
      if (this.tickCount % 10 == 0) {
         if (this.forcedLookTicks <= 0) {
            for(FireworkRocketEntity fw : server.getEntitiesOfClass(FireworkRocketEntity.class, this.getBoundingBox().inflate((double)90.0F))) {
               FireworkRocketEntityAccessor acc = (FireworkRocketEntityAccessor)fw;
               if (acc.getLifetime() - acc.getLife() <= 10) {
                  Fireworks contents = (Fireworks)fw.getItem().get(DataComponents.FIREWORKS);
                  if (contents != null && !contents.explosions().isEmpty()) {
                     this.lookAt(fw.position());
                     this.forcedLookTicks = 60;
                     this.target = null;
                     this.retargetCooldown = 40;
                     return;
                  }
               }
            }

         }
      }
   }

   private double hostPhase() {
      Entity var2 = this.level().getEntity(this.getStormId());
      double var10000;
      if (var2 instanceof WitherStormEntity ws) {
         var10000 = ws.getPhase();
      } else {
         var10000 = (double)4.0F;
      }

      return var10000;
   }

   public float renderScale() {
      Entity var2 = this.level().getEntity(this.getStormId());
      float var10000;
      if (var2 instanceof StormHeadHost host) {
         var10000 = Math.max(0.2F, host.headScaleFor(this.getHeadIndex()));
      } else {
         var10000 = this.hostlessScale();
      }

      return var10000;
   }

   public float beamScale() {
      Entity var2 = this.level().getEntity(this.getStormId());
      float var10000;
      if (var2 instanceof StormHeadHost host) {
         var10000 = Math.max(0.05F, host.beamScaleFor(this.getHeadIndex()));
      } else {
         var10000 = 1.0F;
      }

      return var10000;
   }

   public float getLit() {
      return (Float)this.entityData.get(LIT);
   }

   public boolean isDormant() {
      return this.getLit() < 0.999F;
   }

   public LivingEntity getHeadTarget() {
      return this.target;
   }

   private float roarVolume(float fraction) {
      Level var5 = this.level();
      double var10000;
      if (var5 instanceof ServerLevel sl) {
         var10000 = WitherStormConfigs.get(sl).roarRange;
      } else {
         var10000 = (double)260.0F;
      }

      double range = var10000;
      return (float)Math.max((double)1.0F, range * (double)fraction / (double)16.0F);
   }

   private float beamVolume() {
      Level var4 = this.level();
      double var10000;
      if (var4 instanceof ServerLevel sl) {
         var10000 = WitherStormConfigs.get(sl).beamSoundRange;
      } else {
         var10000 = (double)190.0F;
      }

      double range = var10000;
      return (float)Math.max((double)1.0F, range / (double)16.0F);
   }

   public float getJawAngle() {
      return this.jawAngle;
   }

   public void lookAt(Vec3 point) {
      this.forcedLookPoint = point;
      this.forcedLookTicks = 10;
      this.target = null;
   }

   public WitherStormHeadEntity(EntityType<? extends WitherStormHeadEntity> type, Level level) {
      super(type, level);
      this.lastPullVel = Vec3.ZERO;
      this.chompTicks = -1;
      this.sonicDisableTicks = 0;
      this.defianceCooldown = 0;
      this.wardenCharging = null;
      this.wardenChargeTicks = 0;
      this.wardenCooldown = 0;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(STORM_ID, -1);
      builder.define(HEAD_INDEX_DATA, 0);
      builder.define(HEAD_LOCAL_YAW, 0.0F);
      builder.define(HEAD_ROLL, 0.0F);
      builder.define(SPAWN_GAME_TIME, -1L);
      builder.define(FIRE_START_TIME, -1L);
      builder.define(HURT_START_TIME, -1L);
      builder.define(ROAR_START_TIME, -1L);
      builder.define(VOCAL_TIME, -1L);
      builder.define(BEAM_END, Optional.empty());
      builder.define(LIT, 1.0F);
      builder.define(BEAM_END_EXACT, new Vector3f());
      builder.define(SUCKED_ID, -1);
   }

   public int getSuckedId() {
      return (Integer)this.entityData.get(SUCKED_ID);
   }

   public void markJustSpawned() {
      this.entityData.set(SPAWN_GAME_TIME, this.level().getGameTime());
   }

   public long getSpawnGameTime() {
      return (Long)this.entityData.get(SPAWN_GAME_TIME);
   }

   public long getFireStartTime() {
      return (Long)this.entityData.get(FIRE_START_TIME);
   }

   public long getHurtStartTime() {
      return (Long)this.entityData.get(HURT_START_TIME);
   }

   public long getRoarStartTime() {
      return (Long)this.entityData.get(ROAR_START_TIME);
   }

   public boolean isFiring() {
      long start = this.getFireStartTime();
      if (start < 0L) {
         return false;
      } else {
         long elapsed = this.level().getGameTime() - start;
         return elapsed >= 0L && elapsed < 25L;
      }
   }

   public boolean isDamaged() {
      long start = this.getHurtStartTime();
      if (start < 0L) {
         return false;
      } else {
         long elapsed = this.level().getGameTime() - start;
         return elapsed >= 0L && elapsed < 600L;
      }
   }

   public Optional<BlockPos> getBeamEnd() {
      return (Optional)this.entityData.get(BEAM_END);
   }

   public boolean isBeamActive() {
      return this.getBeamEnd().isPresent();
   }

   public Vec3 getBeamEndExact() {
      Vector3fc v = (Vector3fc)this.entityData.get(BEAM_END_EXACT);
      return new Vec3((double)v.x(), (double)v.y(), (double)v.z());
   }

   public void setStormData(UUID stormUUID, int headIndex) {
      this.stormUUID = stormUUID;
      this.headIndex = headIndex;
      Level var4 = this.level();
      if (var4 instanceof ServerLevel sl) {
         Entity storm = sl.getEntity(stormUUID);
         if (storm != null) {
            this.entityData.set(STORM_ID, storm.getId());
         }

         this.entityData.set(HEAD_INDEX_DATA, headIndex);
      }

   }

   public int getStormId() {
      return (Integer)this.entityData.get(STORM_ID);
   }

   public UUID getStormUUID() {
      return this.stormUUID;
   }

   public int getHeadIndex() {
      return (Integer)this.entityData.get(HEAD_INDEX_DATA);
   }

   public float getLocalYaw() {
      return (Float)this.entityData.get(HEAD_LOCAL_YAW);
   }

   public float getRoll() {
      return (Float)this.entityData.get(HEAD_ROLL);
   }

   public void setBaseYaw(float baseYaw) {
      this.baseYaw = baseYaw;
   }

   private void updateBeamMood() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel sl) {
         if (WitherStormConfigs.get(sl).beamShutoff == 0) {
            this.beamDesiredOn = true;
            this.beamOutputOn = true;
            this.beamFlickerTicks = 0;
            return;
         }
      }

      if (this.forcedLookTicks > 0) {
         this.beamDesiredOn = true;
         this.beamOutputOn = true;
         this.beamFlickerTicks = 0;
      } else if (this.victims.isEmpty() && (this.suckedEntity == null || !this.suckedEntity.isAlive())) {
         if (this.beamAlwaysOn()) {
            this.setBeamDesired(true);
            this.beamOutputOn = true;
            this.beamFlickerTicks = 0;
         } else if (--this.beamMoodTimer <= 0) {
            this.beamMoodTimer = 80 + this.random.nextInt(140);
            boolean wants;
            if (this.target != null) {
               wants = this.random.nextFloat() < 0.85F;
            } else {
               wants = this.random.nextFloat() < 0.6F;
            }

            this.setBeamDesired(wants);
         }
      } else {
         this.setBeamDesired(true);
      }
   }

   private void setBeamDesired(boolean on) {
      if (on != this.beamDesiredOn) {
         this.beamDesiredOn = on;
         this.beamFlickerTicks = 8 + this.random.nextInt(5);
      }

   }

   private boolean beamLitThisTick() {
      if (this.beamOffSoundCooldown > 0) {
         --this.beamOffSoundCooldown;
      }

      if (this.beamFlickerTicks > 0) {
         --this.beamFlickerTicks;
         if (this.beamFlickerTicks == 0) {
            this.beamOutputOn = this.beamDesiredOn;
            return this.beamOutputOn;
         } else {
            return (this.beamFlickerTicks / 2 & 1) == 0 ? this.beamDesiredOn : !this.beamDesiredOn;
         }
      } else {
         return this.beamOutputOn;
      }
   }

   private void playSound(SoundEvent event, float volume, float pitch, boolean echo) {
      if (!this.isDormant()) {
         this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), event, SoundSource.HOSTILE, volume, pitch);
         if (echo) {
            this.pendingEchoes.add(new Echo(new int[]{26}, event, volume * 0.12F, pitch * 0.9F));
         }

      }
   }

   private void tickEchoes() {
      if (this.isDormant()) {
         this.pendingEchoes.clear();
      } else {
         Iterator<Echo> it = this.pendingEchoes.iterator();

         while(it.hasNext()) {
            Echo e = (Echo)it.next();
            if (--e.delay()[0] <= 0) {
               this.level().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), e.event(), SoundSource.HOSTILE, e.volume(), e.pitch());
               it.remove();
            }
         }

      }
   }

   private void tickVocals() {
      if (this.chompTicks < 0) {
         if (--this.vocalCooldown <= 0) {
            this.vocalCooldown = 160 + this.random.nextInt(340);
            int roll = this.random.nextInt(5);
            if (roll == 4) {
               this.triggerRoar(false);
            } else {
               SoundEvent var10000;
               switch (roll) {
                  case 0:
                  case 1:
                     var10000 = ModSounds.HEAD_GROWL;
                     break;
                  case 2:
                     var10000 = ModSounds.HEAD_SHORT_GROWL;
                     break;
                  default:
                     var10000 = ModSounds.HEAD_SNARL;
               }

               SoundEvent ev = var10000;
               this.entityData.set(VOCAL_TIME, this.level().getGameTime());
               this.playSound(ev, this.roarVolume(0.55F), 0.95F + this.random.nextFloat() * 0.05F, false);
            }

         }
      }
   }

   public void triggerRoar(boolean powerful) {
      if (!this.isDormant() || powerful) {
         this.entityData.set(ROAR_START_TIME, this.level().getGameTime());
         this.vocalCooldown = Math.max(this.vocalCooldown, 120);
         this.playSound(powerful ? ModSounds.HEAD_POWERFUL_ROAR : ModSounds.HEAD_ROAR, this.roarVolume(powerful ? 1.25F : 1.0F), 0.94F - this.random.nextFloat() * 0.08F, true);
      }
   }

   private void clientJawTick() {
      float yawDelta = Mth.degreesDifference(this.prevYawForJaw, this.getYRot());
      this.prevYawForJaw = this.getYRot();
      float turning = Math.max(Math.abs(yawDelta) - 1.5F, 0.0F);
      float jawTarget = Mth.clamp(turning * 2.5F, 0.0F, 25.0F);
      long vocal = (Long)this.entityData.get(VOCAL_TIME);
      long now = this.level().getGameTime();
      if (vocal >= 0L && vocal != this.lastVocalKick && now - vocal >= 0L && now - vocal < 40L) {
         this.lastVocalKick = vocal;
         this.jawVel += 12.0F + this.random.nextFloat() * 6.0F;
      }

      float dt = 0.05F;
      float springAccel = 180.0F * (jawTarget - this.jawAngle) - 14.0F * this.jawVel;
      this.jawVel += springAccel * dt;
      this.jawAngle += this.jawVel * dt;
      this.jawAngle = Mth.clamp(this.jawAngle, 0.0F, 37.5F);
   }

   public void startle(Vec3 point) {
      this.startleTicks = 90;
      this.target = null;
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         this.clientJawTick();
      }

      if (!this.level().isClientSide()) {
         this.tickEchoes();
         this.tickVocals();
         Level var2 = this.level();
         if (var2 instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)var2;
            if ((Integer)this.entityData.get(STORM_ID) == -1 && this.stormUUID != null) {
               Entity storm = sl.getEntity(this.stormUUID);
               if (storm != null) {
                  this.entityData.set(STORM_ID, storm.getId());
               }
            }

            if ((Integer)this.entityData.get(HEAD_INDEX_DATA) != this.headIndex) {
               this.entityData.set(HEAD_INDEX_DATA, this.headIndex);
            }

            Entity stormE = sl.getEntity((Integer)this.entityData.get(STORM_ID));
            if (stormE instanceof StormHeadHost) {
               StormHeadHost host = (StormHeadHost)stormE;
               this.baseYaw = stormE.getYRot() + host.headYawOffsetFor(this.getHeadIndex());
               this.baseRoll = host.headRollOffsetFor(this.getHeadIndex());
               this.distressed = host.headsDistressed();
               this.yawRange = host.headYawRangeFor(this.getHeadIndex());
               float lit = host.headLitFor(this.getHeadIndex());
               if ((Float)this.entityData.get(LIT) != lit) {
                  this.entityData.set(LIT, lit);
               }
            }

            if (this.isDormant()) {
               if (this.isBeamActive()) {
                  this.entityData.set(BEAM_END, Optional.empty());
               }

               if (!this.victims.isEmpty() || this.suckedEntity != null) {
                  this.releaseAll(sl, true);
               }

               this.target = null;
               return;
            }

            Entity owner = this.stormUUID != null ? sl.getEntity(this.stormUUID) : stormE;
            if (this.needsHost() && (!(owner instanceof StormHeadHost) || !owner.isAlive())) {
               if (++this.orphanTicks > 200) {
                  this.discard();
                  return;
               }
            } else {
               this.orphanTicks = 0;
            }
         }

         this.yRotO = this.getYRot();
         this.xRotO = this.getXRot();
         this.updateTarget();
         float wantedYaw;
         float wantedPitch;
         if (this.target != null) {
            double dx = this.target.getX() - this.getX();
            double dy = this.target.getEyeY() - this.getEyeY();
            double dz = this.target.getZ() - this.getZ();
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            wantedYaw = (float)(Mth.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
            wantedPitch = (float)(-(Mth.atan2(dy, horizontalDist) * (180D / Math.PI)));
         } else {
            this.updateWander();
            wantedYaw = this.wanderYaw;
            wantedPitch = this.wanderPitch;
         }

         if (this.startleTicks > 0) {
            --this.startleTicks;
            this.target = null;
            float st = (float)this.tickCount + (float)this.getHeadIndex() * 37.0F;
            float roamYaw = Mth.sin((double)(st * 0.11F)) * 55.0F + Mth.sin((double)(st * 0.047F + 1.7F)) * 35.0F + Mth.sin((double)(st * 0.31F)) * 6.0F;
            float roamPitch = 18.0F + Mth.sin((double)(st * 0.09F + 0.6F)) * 22.0F + Mth.sin((double)(st * 0.27F)) * 4.0F;
            wantedYaw = this.baseYaw + roamYaw;
            wantedPitch = roamPitch;
         }

         if (this.forcedLookTicks > 0) {
            --this.forcedLookTicks;
            if (this.forcedLookPoint != null) {
               double dx = this.forcedLookPoint.x - this.getX();
               double dy = this.forcedLookPoint.y - this.getEyeY();
               double dz = this.forcedLookPoint.z - this.getZ();
               double horiz = Math.sqrt(dx * dx + dz * dz);
               wantedYaw = (float)(Mth.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
               wantedPitch = (float)(-(Mth.atan2(dy, horiz) * (180D / Math.PI)));
            }
         }

         wantedYaw += this.beamSeparationNudge();
         wantedYaw = this.clampToBase(wantedYaw, this.baseYaw, this.yawRange);
         wantedPitch = Mth.clamp(wantedPitch, 0.0F, 90.0F);
         if (Float.isNaN(this.filteredWantYaw)) {
            this.filteredWantYaw = wantedYaw;
         }

         if (Float.isNaN(this.filteredWantPitch)) {
            this.filteredWantPitch = wantedPitch;
         }

         float yawErr = Mth.degreesDifference(this.filteredWantYaw, wantedYaw);
         if (Math.abs(yawErr) > 2.0F) {
            this.filteredWantYaw = Mth.wrapDegrees(this.filteredWantYaw + yawErr * 0.3F);
         }

         float pitchErr = wantedPitch - this.filteredWantPitch;
         if (Math.abs(pitchErr) > 1.5F) {
            this.filteredWantPitch += pitchErr * 0.3F;
         }

         wantedYaw = this.clampToBase(this.filteredWantYaw, this.baseYaw, this.yawRange);
         wantedPitch = Mth.clamp(this.filteredWantPitch, 0.0F, 90.0F);
         boolean straining = this.distressed;
         if (straining) {
            float sweep = Mth.sin((double)((float)this.tickCount * 0.028F + (float)this.getHeadIndex() * 2.1F));
            float strain = Mth.sin((double)((float)this.tickCount * 0.31F + (float)this.getHeadIndex())) * 7.0F;
            wantedYaw = this.baseYaw + Math.signum(sweep) * (this.yawRange + 44.0F) + strain;
            wantedPitch = 4.0F + Mth.sin((double)((float)this.tickCount * 0.11F)) * 6.0F;
         }

         float turnRange = straining ? this.yawRange + 22.0F : this.yawRange;
         float[] yawVelBox = new float[]{this.yawVel};
         Float heldYaw = this.pinnedYaw();
         float resultYaw;
         if (heldYaw != null) {
            resultYaw = heldYaw;
            this.yawVel = 0.0F;
         } else {
            resultYaw = this.smoothDampAngle(this.getYRot(), wantedYaw, yawVelBox, straining ? 0.45F : 1.0F);
            this.yawVel = yawVelBox[0];
            resultYaw = this.clampToBase(resultYaw, this.baseYaw, turnRange);
         }

         this.setYRot(resultYaw);
         this.entityData.set(HEAD_LOCAL_YAW, Mth.wrapDegrees(resultYaw - this.baseYaw));
         float idleRoll = 3.0F * Mth.sin((double)((float)this.tickCount * 0.045F + (float)this.getHeadIndex() * 1.7F));
         float rest = this.baseRoll * (0.65F + 0.35F * Mth.sin((double)((float)this.tickCount * 0.017F + (float)this.getHeadIndex() * 2.4F)));
         float targetRoll = Mth.clamp(this.yawVel * 0.55F, -32.0F, 32.0F) + idleRoll + rest;
         float[] rollVelBox = new float[]{this.rollVel};
         this.headRoll = this.smoothDampAngle(this.headRoll, targetRoll, rollVelBox, 0.9F);
         this.rollVel = rollVelBox[0];
         this.entityData.set(HEAD_ROLL, this.headRoll);
         float yawDelta = Mth.degreesDifference(this.prevYawForJaw, this.getYRot());
         this.prevYawForJaw = this.getYRot();
         float jawTarget = Mth.clamp(Math.abs(yawDelta) * 2.5F, 0.0F, 25.0F);
         float dt = 0.05F;
         float springAccel = 180.0F * (jawTarget - this.jawAngle) - 14.0F * this.jawVel;
         this.jawVel += springAccel * dt;
         this.jawAngle += this.jawVel * dt;
         this.jawAngle = Mth.clamp(this.jawAngle, 0.0F, 37.5F);
         Float pinned = this.pinnedPitch();
         if (pinned != null) {
            this.setXRot(pinned);
            this.pitchVel = 0.0F;
         } else {
            float[] pitchVelBox = new float[]{this.pitchVel};
            float resultPitch = this.smoothDampAngle(this.getXRot(), wantedPitch, pitchVelBox, 1.0F);
            this.pitchVel = pitchVelBox[0];
            resultPitch = Mth.clamp(resultPitch, 0.0F, this.hostPitchRange());
            this.setXRot(resultPitch);
         }

         Level var54 = this.level();
         if (var54 instanceof ServerLevel) {
            ServerLevel fwLevel = (ServerLevel)var54;
            this.tickFireworkDistraction(fwLevel);
         }

         if (this.fireCooldown > 0) {
            --this.fireCooldown;
         }

         Entity var22 = this.level().getEntity(this.getStormId());
         double var10000;
         if (var22 instanceof WitherStormEntity) {
            WitherStormEntity fireWs = (WitherStormEntity)var22;
            var10000 = fireWs.getPhase();
         } else {
            var10000 = (double)4.0F;
         }

         double firePhase = var10000;
         if (this.target == null) {
            if (this.targetlessTicks < Integer.MAX_VALUE) {
               ++this.targetlessTicks;
            }
         } else {
            this.targetlessTicks = 0;
         }

         boolean idleShot = this.targetlessTicks > 100;
         if (this.mayFire() && !this.isSonicDisabled() && !this.isFiring() && this.chompTicks < 0 && this.fireCooldown <= 0 && (idleShot || this.target != null && this.target.isAlive() && this.canSee(this.target))) {
            this.entityData.set(FIRE_START_TIME, this.level().getGameTime());
            int interval = (int)((float)Math.max(WitherStormConfigs.get(this.level()).headFireInterval, 20) * fireIntervalMultiplier(firePhase));
            if (idleShot) {
               interval *= 3;
            }

            this.fireCooldown = interval + this.random.nextInt(interval);
            this.skullShotDelay = 9;
            this.playSound(ModSounds.HEAD_SHOOT, 14.0F, 0.95F + this.random.nextFloat() * 0.1F, true);
            Entity var24 = this.level().getEntity(this.getStormId());
            if (var24 instanceof WitherStormEntity) {
               WitherStormEntity ws = (WitherStormEntity)var24;
               ws.onHeadFired(this.getHeadIndex());
            }
         }

         if (this.skullShotDelay > 0 && --this.skullShotDelay == 0) {
            this.shootSkull();
         }

         this.tickBeam();
         this.entityData.set(SUCKED_ID, this.syncedVictimId());
      }

   }

   private void tickBeam() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel server) {
         long spawnTime = this.getSpawnGameTime();
         if (spawnTime >= 0L && server.getGameTime() - spawnTime < 52L) {
            if (this.isBeamActive()) {
               this.entityData.set(BEAM_END, Optional.empty());
            }

            this.beamWasActive = false;
         } else {
            Entity var5 = this.level().getEntity(this.getStormId());
            if (var5 instanceof StormHeadHost) {
               StormHeadHost host = (StormHeadHost)var5;
               if (!host.headBeamAllowed(this.getHeadIndex())) {
                  if (this.isBeamActive()) {
                     this.entityData.set(BEAM_END, Optional.empty());
                  }

                  this.beamWasActive = false;
                  if (!this.victims.isEmpty() || this.suckedEntity != null) {
                     this.releaseAll(server, false);
                  }

                  return;
               }
            }

            if (this.sonicDisableTicks > 0) {
               --this.sonicDisableTicks;
               if (this.isBeamActive()) {
                  this.entityData.set(BEAM_END, Optional.empty());
               }

               this.beamWasActive = false;
               if (!this.victims.isEmpty() || this.suckedEntity != null) {
                  this.releaseAll(server, false);
               }

            } else if (this.isDamaged() && this.forcedLookTicks <= 0) {
               if (this.isBeamActive()) {
                  this.entityData.set(BEAM_END, Optional.empty());
               }

               this.beamWasActive = false;
               if (!this.victims.isEmpty() || this.suckedEntity != null) {
                  this.releaseAll(server, true);
               }

               if ((Integer)this.entityData.get(SUCKED_ID) != -1) {
                  this.entityData.set(SUCKED_ID, -1);
               }

            } else {
               this.updateBeamMood();
               if (!this.beamLitThisTick()) {
                  if (this.isBeamActive()) {
                     if (this.beamOffSoundCooldown <= 0) {
                        Vec3 groundEnd = this.getBeamEndExact();
                        server.playSound((Entity)null, groundEnd.x, groundEnd.y, groundEnd.z, ModSounds.TRACTOR_BEAM_GROUND_DISABLE, SoundSource.HOSTILE, 2.2F, 0.95F + this.random.nextFloat() * 0.1F);
                        this.beamOffSoundCooldown = 30;
                     }

                     this.entityData.set(BEAM_END, Optional.empty());
                  }

                  this.beamWasActive = false;
                  this.thawVictims();
                  if (this.beamFlickerTicks == 0 && (!this.victims.isEmpty() || this.suckedEntity != null && this.chompTicks < 0)) {
                     boolean eat = WitherStormConfigs.get(server).mobPickup != 0 && this.level().getEntity(this.getStormId()) instanceof WitherStormEntity;
                     WitherStormEntity ws = eat ? (WitherStormEntity)this.level().getEntity(this.getStormId()) : null;
                     if (ws != null) {
                        for(LivingEntity v : new ArrayList(this.victims)) {
                           if (!(v instanceof Player)) {
                              ws.doomMob(v);
                              this.victims.remove(v);
                              this.forgetVictim(v);
                           }
                        }

                        if (this.suckedEntity != null && this.chompTicks < 0 && !(this.suckedEntity instanceof Player)) {
                           ws.doomMob(this.suckedEntity);
                           this.suckedEntity = null;
                        }
                     }

                     this.releaseAll(server, true);
                  }

               } else {
                  WitherStormWorldConfig cfg = WitherStormConfigs.get(server);
                  int groundRadius = Math.max(Math.round((float)cfg.beamGroundRadius * this.beamScale() * this.beamWidthScale()), 1);
                  double yawRad = Math.toRadians((double)this.getYRot());
                  double pitchRad = Math.toRadians((double)this.getXRot());
                  double cosP = Math.cos(pitchRad);
                  Vec3 dir = new Vec3(-Math.sin(yawRad) * cosP, -Math.sin(pitchRad), Math.cos(yawRad) * cosP);
                  Vec3 start = this.position();
                  Vec3 rayEnd = start.add(dir.scale((double)120.0F));
                  BlockHitResult hit = server.clip(new ClipContext(start, rayEnd, Block.COLLIDER, Fluid.NONE, this));
                  BlockPos end;
                  Vec3 exact;
                  if (hit.getType() != Type.MISS) {
                     end = hit.getBlockPos();
                     exact = hit.getLocation();
                  } else {
                     end = server.getHeightmapPos(WitherStormConfigs.get(server).groundHeightmap(), new BlockPos((int)Math.floor(rayEnd.x), 0, (int)Math.floor(rayEnd.z))).below();
                     exact = new Vec3((double)end.getX() + (double)0.5F, (double)end.getY() + (double)1.0F, (double)end.getZ() + (double)0.5F);
                  }

                  Vec3 dodged = this.avoidHiddenPlayers(server, exact, groundRadius);
                  if (dodged != exact) {
                     exact = dodged;
                     end = BlockPos.containing(dodged.x, dodged.y, dodged.z);
                  }

                  this.entityData.set(BEAM_END, Optional.of(end));
                  this.entityData.set(BEAM_END_EXACT, new Vector3f((float)exact.x, (float)exact.y, (float)exact.z));
                  if (!this.beamWasActive) {
                     this.beamWasActive = true;
                     if (this.beamFlickerTicks == 0) {
                        this.playSound(ModSounds.HEAD_ACTIVATE_BEAM, this.beamVolume(), 1.0F, true);
                     }
                  }

                  if (this.beamSnapCooldown > 0) {
                     --this.beamSnapCooldown;
                  }

                  if (this.lastBeamExact != null && this.beamSnapCooldown <= 0 && exact.distanceTo(this.lastBeamExact) > (double)16.0F) {
                     this.beamSnapCooldown = 60;
                     this.level().playSound((Entity)null, exact.x, exact.y, exact.z, ModSounds.HEAD_BEAM_SNAP, SoundSource.HOSTILE, 5.0F, 0.95F + this.random.nextFloat() * 0.1F);
                  }

                  this.lastBeamExact = exact;
                  this.tickSuction(server, exact, groundRadius);
                  if (this.tickCount % 2 == 0) {
                     this.spawnCrumbParticles(server, end, groundRadius);
                  }

                  if (--this.beamClusterTimer <= 0) {
                     this.beamClusterTimer = Math.max(cfg.beamClusterInterval, 10);
                     if (this.mayTakeBlocks()) {
                        this.spawnBeamCluster(server, end, groundRadius);
                     }
                  }

               }
            }
         }
      }
   }

   private Vec3 avoidHiddenPlayers(ServerLevel server, Vec3 end, int groundRadius) {
      double keepOut = (double)groundRadius + (double)4.0F;

      for(Player p : server.players()) {
         if (ModEffects.isHyperInvisible(p)) {
            double dx = end.x - p.getX();
            double dz = end.z - p.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (!(dist > keepOut)) {
               Vec3 away = dist < 1.0E-4 ? (new Vec3(this.random.nextDouble() - (double)0.5F, (double)0.0F, this.random.nextDouble() - (double)0.5F)).normalize() : new Vec3(dx / dist, (double)0.0F, dz / dist);
               end = new Vec3(p.getX() + away.x * keepOut, end.y, p.getZ() + away.z * keepOut);
            }
         }
      }

      return end;
   }

   private static float fireIntervalMultiplier(double phase) {
      if (phase >= 6.1) {
         return 1.0F;
      } else if (phase >= (double)6.0F) {
         return 2.2F;
      } else if (phase >= 5.8) {
         return 3.0F;
      } else {
         return phase >= (double)5.0F ? 1.7F : 1.0F;
      }
   }

   private void shootSkull() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel server) {
         double var17 = Math.toRadians((double)this.getYRot());
         double pitchRad = Math.toRadians((double)this.getXRot());
         double cosP = Math.cos(pitchRad);
         Vec3 dir = new Vec3(-Math.sin(var17) * cosP, -Math.sin(pitchRad), Math.cos(var17) * cosP);
         float hs = this.renderScale() / 6.0F;
         Vec3 mouth = this.position().add(dir.scale((double)3.5F * (double)hs)).add((double)0.0F, (this.isHungUpsideDown() ? (double)1.0F : (double)-1.0F) * (double)hs, (double)0.0F);
         Vec3 aim = dir;
         if (this.target != null && this.target.isAlive()) {
            aim = this.target.position().add((double)0.0F, (double)this.target.getBbHeight() * (double)0.5F, (double)0.0F).subtract(mouth).normalize();
            double horiz = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
            double maxUp = horiz * 0.47;
            if (aim.y > maxUp) {
               aim = (new Vec3(aim.x, maxUp, aim.z)).normalize();
            }
         }

         SuperSkullEntity skull = new SuperSkullEntity(ModEntityTypes.SUPER_SKULL, server);
         skull.setPos(mouth.x, mouth.y, mouth.z);
         Entity var16 = this.level().getEntity(this.getStormId());
         double var10000;
         if (var16 instanceof WitherStormEntity ws) {
            var10000 = ws.getPhase();
         } else {
            var10000 = (double)0.0F;
         }

         double phase = var10000;
         double force = phase >= 6.1 ? 1.1 : (phase >= (double)5.0F ? 0.65 : 1.1);
         skull.shoot(aim.scale(force));
         server.addFreshEntity(skull);
      }
   }

   public static boolean recentlyEscaped(Player player, long gameTime) {
      Long until = (Long)justEscaped.get(player.getUUID());
      if (until == null) {
         return false;
      } else if (until > gameTime && until - gameTime <= 120L) {
         return true;
      } else {
         justEscaped.remove(player.getUUID());
         return false;
      }
   }

   public static boolean isForgiven(Player player, long gameTime) {
      Long until = (Long)FORGIVEN_UNTIL.get(player.getUUID());
      if (until == null) {
         return false;
      } else if (until - gameTime > 12000L) {
         FORGIVEN_UNTIL.remove(player.getUUID());
         return false;
      } else if (gameTime >= until) {
         FORGIVEN_UNTIL.remove(player.getUUID());
         return false;
      } else {
         return true;
      }
   }

   public static void forgive(ServerLevel level, Player player) {
      int secs = WitherStormConfigs.get(level).headForgiveSeconds;
      if (secs <= 0) {
         FORGIVEN_UNTIL.remove(player.getUUID());
      } else {
         long until = level.getGameTime() + (long)secs * 20L;
         Long existing = (Long)FORGIVEN_UNTIL.get(player.getUUID());
         if (existing == null || existing < until || existing - level.getGameTime() > 12000L) {
            FORGIVEN_UNTIL.put(player.getUUID(), until);
         }

      }
   }

   private void tickSuction(ServerLevel server, Vec3 beamGround, int groundRadius) {
      if (this.chompTicks >= 0) {
         this.tickChomp(server);
      } else {
         Vec3 headPos = this.position();
         Vec3 axis = headPos.subtract(beamGround);
         double axisLen = Math.max(axis.length(), (double)1.0F);
         Vec3 axisN = axis.scale((double)1.0F / axisLen);
         this.collectVictims(server, beamGround, groundRadius);
         if (this.defianceCooldown > 0) {
            --this.defianceCooldown;
         }

         Iterator<LivingEntity> it = this.victims.iterator();

         while(it.hasNext()) {
            LivingEntity victim = (LivingEntity)it.next();
            if (victim.isAlive() && !victim.isRemoved() && victim.level() == this.level()) {
               if (victim instanceof Player || !WitheredMobs.isWithered(victim) && !WitherSickness.isAboutToTurn(victim, this.hostPhase())) {
                  this.tickCaptiveDefiance(server, victim);
                  if (!victim.isRemoved() && victim.isAlive()) {
                     if (!(victim instanceof Player)) {
                        victim.setNoGravity(true);
                        if (victim instanceof Mob) {
                           Mob mob = (Mob)victim;
                           mob.setNoAi(true);
                        }
                     }

                     if (victim instanceof Player) {
                        Player p = (Player)victim;
                        Vec3 prev = (Vec3)this.lastPos.get(p);
                        boolean pearled = prev != null && p.position().distanceToSqr(prev) > (double)36.0F;
                        if (p.isCreative() || p.isSpectator() || p.isFallFlying() || pearled) {
                           forgive(server, p);
                           this.releaseOne(victim);
                           it.remove();
                           continue;
                        }

                        if (isForgiven(p, server.getGameTime()) || ModEffects.isHyperInvisible(p)) {
                           this.releaseOne(victim);
                           it.remove();
                           continue;
                        }
                     }

                     Vec3 tPos = victim.position();
                     Vec3 rel = tPos.subtract(beamGround);
                     double along = Mth.clamp(rel.dot(axisN), (double)0.0F, axisLen);
                     double offAxis = rel.subtract(axisN.scale(along)).length();
                     if (offAxis > (double)groundRadius + (double)4.5F) {
                        if (victim instanceof Player) {
                           Player p = (Player)victim;
                           forgive(server, p);
                        }

                        this.releaseOne(victim);
                        it.remove();
                     } else {
                        if (headPos.distanceTo(tPos) < (double)7.0F && this.chompTicks >= 0 && !(victim instanceof Player)) {
                           Entity var18 = this.level().getEntity(this.getStormId());
                           if (var18 instanceof WitherStormEntity) {
                              WitherStormEntity busyStorm = (WitherStormEntity)var18;
                              it.remove();
                              this.forgetVictim(victim);
                              busyStorm.doomMob(victim);
                              continue;
                           }
                        }

                        if (headPos.distanceTo(tPos) < (double)7.0F && this.chompTicks < 0) {
                           it.remove();
                           this.forgetVictim(victim);
                           if (victim instanceof Creeper) {
                              Creeper creeper = (Creeper)victim;
                              if (this.random.nextInt(4) == 0) {
                                 this.detonateInMaw(server, creeper);
                                 continue;
                              }
                           }

                           this.suckedEntity = victim;
                           this.beginChomp();
                        } else {
                           this.haulVictim(victim, headPos, tPos, beamGround, axisN, axisLen, along, rel, groundRadius);
                        }
                     }
                  } else {
                     it.remove();
                     this.forgetVictim(victim);
                  }
               } else {
                  this.releaseOne(victim);
                  it.remove();
               }
            } else {
               this.releaseOne(victim);
               it.remove();
            }
         }

      }
   }

   protected boolean needsHost() {
      return true;
   }

   protected boolean mayTakeBlocks() {
      return true;
   }

   protected float beamWidthScale() {
      return 1.0F;
   }

   protected Float pinnedPitch() {
      return null;
   }

   protected boolean mayFire() {
      return true;
   }

   public boolean isSonicDisabled() {
      return this.sonicDisableTicks > 0;
   }

   public void disableFor(int ticks) {
      this.sonicDisableTicks = Math.max(this.sonicDisableTicks, ticks);
      this.entityData.set(HURT_START_TIME, this.level().getGameTime());
      if (this.isBeamActive()) {
         this.entityData.set(BEAM_END, Optional.empty());
      }

   }

   private void tickCaptiveDefiance(ServerLevel server, LivingEntity victim) {
      if (!(victim instanceof Player)) {
         Vec3 from = victim.position().add((double)0.0F, (double)victim.getBbHeight() * 0.6, (double)0.0F);
         Vec3 to = this.position().add((double)0.0F, (double)-1.0F, (double)0.0F);
         Vec3 aim = to.subtract(from);
         if (!(aim.lengthSqr() < (double)1.0F)) {
            if (victim instanceof Warden) {
               Warden warden = (Warden)victim;
               this.tickWardenCharge(server, warden, from, aim);
            } else if (this.defianceCooldown <= 0) {
               if (!(this.random.nextFloat() > 0.07F)) {
                  this.defianceCooldown = 25;
                  if (victim instanceof AbstractSkeleton || victim instanceof Pillager) {
                     this.shootAtHead(server, victim, from, aim);
                  }

               }
            }
         }
      }
   }

   private void shootAtHead(ServerLevel server, LivingEntity shooter, Vec3 from, Vec3 aim) {
      Arrow arrow = new Arrow(server, shooter, new ItemStack(Items.ARROW), (ItemStack)null);
      arrow.setPos(from.x, from.y, from.z);
      arrow.setOwner(shooter);
      arrow.shoot(aim.x, aim.y, aim.z, 1.6F, 9.0F);
      server.addFreshEntity(arrow);
      server.playSound((Entity)null, from.x, from.y, from.z, shooter instanceof Pillager ? SoundEvents.CROSSBOW_SHOOT : SoundEvents.SKELETON_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F);
   }

   private void detonateInMaw(ServerLevel server, Creeper creeper) {
      Vec3 at = creeper.position();
      server.playSound((Entity)null, at.x, at.y, at.z, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 4.0F, 0.9F);
      server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y, at.z, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
      this.entityData.set(HURT_START_TIME, server.getGameTime());
      this.playSound(ModSounds.HEAD_HURT, this.roarVolume(0.75F), 0.9F, true);
      if (this.isBeamActive()) {
         this.entityData.set(BEAM_END, Optional.empty());
      }

      this.target = null;
      this.retargetCooldown = 60;
      creeper.discard();
   }

   private void tickWardenCharge(ServerLevel server, Warden warden, Vec3 from, Vec3 aim) {
      if (this.wardenCooldown > 0) {
         --this.wardenCooldown;
      } else {
         if (!warden.getUUID().equals(this.wardenCharging)) {
            this.wardenCharging = warden.getUUID();
            this.wardenChargeTicks = 0;
            server.playSound((Entity)null, from.x, from.y, from.z, SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 3.0F, 1.0F);
            warden.lookAt(Anchor.EYES, this.position());
         }

         ++this.wardenChargeTicks;
         int puff = 1 + this.wardenChargeTicks / 20;
         server.sendParticles(ParticleTypes.SCULK_SOUL, warden.getX(), warden.getY() + (double)warden.getBbHeight() * 0.8, warden.getZ(), puff, 0.35, 0.35, 0.35, 0.02);
         if (this.wardenChargeTicks >= 80) {
            this.wardenCharging = null;
            this.wardenChargeTicks = 0;
            this.wardenCooldown = 500;
            this.sonicBoomAt(server, warden, from, aim);
         }

      }
   }

   private void sonicBoomAt(ServerLevel server, LivingEntity warden, Vec3 from, Vec3 aim) {
      server.playSound((Entity)null, from.x, from.y, from.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 3.0F, 1.0F);
      Vec3 step = aim.normalize().scale(1.6);
      Vec3 at = from;
      int segments = (int)Math.min(aim.length() / 1.6, (double)40.0F);

      for(int i = 0; i < segments; ++i) {
         at = at.add(step);
         server.sendParticles(ParticleTypes.SONIC_BOOM, at.x, at.y, at.z, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
      }

      this.sonicDisableTicks = 240;
      this.entityData.set(HURT_START_TIME, server.getGameTime());
      this.playSound(ModSounds.HEAD_HURT, this.roarVolume(0.75F), 0.87F, true);
      if (this.isBeamActive()) {
         this.entityData.set(BEAM_END, Optional.empty());
      }

   }

   private void haulVictim(LivingEntity victim, Vec3 headPos, Vec3 tPos, Vec3 beamGround, Vec3 axisN, double axisLen, double along, Vec3 rel, int groundRadius) {
      Vec3 toHead = headPos.subtract(tPos);
      Vec3 dir = toHead.lengthSqr() > 1.0E-4 ? toHead.normalize() : new Vec3((double)0.0F, (double)1.0F, (double)0.0F);
      if (victim instanceof Player p) {
         Vec3 prev = (Vec3)this.lastPos.get(p);
         Vec3 travelled = prev != null ? tPos.subtract(prev) : Vec3.ZERO;
         Vec3 lastPull = this.lastPull(p);
         Vec3 input = travelled.subtract(lastPull);
         double alongIn = input.dot(dir);
         Vec3 sideways = input.subtract(dir.scale(alongIn));
         double sideSpeed = sideways.length();
         if (sideSpeed > 0.42) {
            sideways = sideways.scale(0.42 / sideSpeed);
         }

         float resist = ModEnchantments.dragResistance(p);
         Vec3 alongPull = dir.scale((double)0.5F * ((double)1.0F - (double)resist));
         if (p instanceof ServerPlayer sp) {
            ModAdvancements.grant(sp, "tractor_pull");
            if (resist > 0.0F) {
               ModAdvancements.grant(sp, "heavy_going");
            }
         }

         boolean correctSideways = (++this.pullPacketTick & 1) == 0;
         Vec3 vel = correctSideways ? sideways.add(alongPull) : alongPull;
         this.applyPull(victim, vel);
         this.lastPullVelMap.put(p, vel);
         this.lastPos.put(p, p.position());
         int heldFor = (Integer)this.heldTicks.merge(p, 1, Integer::sum);
         if (!p.onGround() && heldFor >= 60) {
            p.setSwimming(true);
            p.setPose(Pose.SWIMMING);
         }

         Entity var30 = this.level().getEntity(this.getStormId());
         if (var30 instanceof WitherStormEntity ws) {
            ws.lockRotationOn(p);
         }
      } else {
         double step = 0.55;
         double newAlong = Math.min(along + step, axisLen - (double)1.0F);
         Vec3 perp = rel.subtract(axisN.scale(along));
         double perpLen = perp.length();
         if (perpLen > (double)groundRadius) {
            perp = perp.scale((double)groundRadius / perpLen);
         }

         perp = perp.scale(0.92);
         Vec3 newPos = beamGround.add(axisN.scale(newAlong)).add(perp);
         victim.setPos(newPos.x, newPos.y, newPos.z);
         victim.setDeltaMovement(Vec3.ZERO);
         victim.fallDistance = (double)0.0F;
         victim.hurtMarked = true;
      }

   }

   private Vec3 lastPull(Player p) {
      Vec3 v = (Vec3)this.lastPullVelMap.get(p);
      return v != null ? v : Vec3.ZERO;
   }

   private int syncedVictimId() {
      if (this.suckedEntity != null) {
         return this.suckedEntity.getId();
      } else {
         for(LivingEntity v : this.victims) {
            if (v instanceof Player) {
               return v.getId();
            }
         }

         return this.victims.isEmpty() ? -1 : ((LivingEntity)this.victims.get(0)).getId();
      }
   }

   private void thawVictims() {
      for(LivingEntity victim : this.victims) {
         this.thawOne(victim);
      }

      if (this.suckedEntity != null && this.chompTicks < 0) {
         this.thawOne(this.suckedEntity);
      }

   }

   private void thawOne(LivingEntity victim) {
      if (!(victim instanceof Player)) {
         victim.setNoGravity(false);
         if (victim instanceof Mob) {
            Mob mob = (Mob)victim;
            mob.setNoAi(false);
         }

      }
   }

   private void releaseOne(LivingEntity victim) {
      if (victim instanceof Player p) {
         p.setSwimming(false);
         p.setPose(Pose.STANDING);
         p.refreshDimensions();
         justEscaped.put(p.getUUID(), this.level().getGameTime() + 30L);
      } else {
         victim.setNoGravity(false);
         if (victim instanceof Mob mob) {
            mob.setNoAi(false);
         }
      }

      this.forgetVictim(victim);
   }

   private void forgetVictim(LivingEntity victim) {
      if (victim instanceof Player p) {
         this.lastPos.remove(p);
         this.lastPullVelMap.remove(p);
         this.heldTicks.remove(p);
      }

   }

   private void releaseAll(ServerLevel server, boolean forgivePlayers) {
      for(LivingEntity victim : this.victims) {
         if (forgivePlayers && victim instanceof Player p) {
            forgive(server, p);
         }

         this.releaseOne(victim);
      }

      this.victims.clear();
      this.wardenCharging = null;
      this.wardenChargeTicks = 0;
      if ((Integer)this.entityData.get(SUCKED_ID) != -1) {
         this.entityData.set(SUCKED_ID, -1);
      }

      if (this.suckedEntity != null && this.chompTicks < 0) {
         if (forgivePlayers) {
            LivingEntity var7 = this.suckedEntity;
            if (var7 instanceof Player) {
               Player p = (Player)var7;
               forgive(server, p);
            }
         }

         this.releaseOne(this.suckedEntity);
         this.suckedEntity = null;
      }

      this.lastVictimPos = null;
      this.lastPullVel = Vec3.ZERO;
   }

   private void applyPull(LivingEntity e, Vec3 vel) {
      if (e instanceof EnderMan) {
         if (WitherStormEntity.isHeldByBeam(e)) {
            return;
         }

         Entity var4 = this.level().getEntity(this.getStormId());
         if (var4 instanceof StormHeadHost) {
            StormHeadHost host = (StormHeadHost)var4;
            if (host.headsDistressed()) {
               return;
            }
         }
      }

      e.setDeltaMovement(vel);
      e.fallDistance = (double)0.0F;
      e.hurtMarked = true;
      if (e instanceof ServerPlayer sp) {
         sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
      }

   }

   private void collectVictims(ServerLevel server, Vec3 beamGround, int groundRadius) {
      double grabR = (double)groundRadius + (double)2.5F;
      long gameTime = server.getGameTime();
      boolean takeMobs = WitherStormConfigs.get(server).mobPickup != 0;
      AABB beamBox = (new AABB(this.position(), beamGround)).inflate(grabR + (double)1.0F);

      for(LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, beamBox, (c) -> c.isAlive() && !(c instanceof WitherStormEntity))) {
         if (!this.victims.contains(e) && e != this.suckedEntity) {
            if (e instanceof Player) {
               Player p = (Player)e;
               if (p.isCreative() || p.isSpectator() || isForgiven(p, gameTime) || recentlyEscaped(p, gameTime) || ModEffects.isHyperInvisible(p)) {
                  continue;
               }
            } else if (e instanceof ArmorStand) {
               ArmorStand stand = (ArmorStand)e;
               if (stand.isMarker() || !takeMobs) {
                  continue;
               }
            } else if (!takeMobs || e instanceof IronGolem || WitheredMobs.isWithered(e) || WitherSickness.isAboutToTurn(e, this.hostPhase())) {
               continue;
            }

            if (this.inBeamColumn(e, beamGround, grabR)) {
               this.victims.add(e);
               if (e instanceof Player) {
                  Player p = (Player)e;
                  this.lastPos.put(p, p.position());
               }
            }
         }
      }

   }

   private boolean inBeamColumn(LivingEntity e, Vec3 beamGround, double radius) {
      Vec3 headPos = this.position();
      Vec3 axis = beamGround.subtract(headPos);
      double len = axis.length();
      if (len < 0.001) {
         return false;
      } else {
         Vec3 n = axis.scale((double)1.0F / len);
         Vec3 rel = e.position().add((double)0.0F, (double)e.getBbHeight() * (double)0.5F, (double)0.0F).subtract(headPos);
         double along = rel.dot(n);
         if (!(along < (double)0.0F) && !(along > len + (double)3.0F)) {
            if (rel.subtract(n.scale(along)).length() > radius) {
               return false;
            } else {
               Vec3 onAxis = headPos.add(n.scale(along));
               Vec3 at = e.position().add((double)0.0F, (double)e.getBbHeight() * (double)0.5F, (double)0.0F);
               return this.level().clip(new ClipContext(onAxis, at, Block.COLLIDER, Fluid.NONE, e)).getType() == Type.MISS;
            }
         } else {
            return false;
         }
      }
   }

   private boolean inGrabCircle(LivingEntity e, Vec3 beamGround, double radius) {
      double dx = e.getX() - beamGround.x;
      double dz = e.getZ() - beamGround.z;
      return dx * dx + dz * dz <= (radius + (double)1.0F) * (radius + (double)1.0F) && Math.abs(e.getY() - beamGround.y) < (double)6.0F;
   }

   public void chompVictim(LivingEntity victim) {
      this.chompVictim(victim, (ResourceKey)null);
   }

   public void chompVictim(LivingEntity victim, ResourceKey<DamageType> damageOverride) {
      this.suckedEntity = victim;
      this.lastVictimPos = null;
      this.chompDamageOverride = damageOverride;
      this.beginChomp();
   }

   public void playFireAnimation() {
      this.entityData.set(FIRE_START_TIME, this.level().getGameTime());
      this.skullShotDelay = -1;
      this.playSound(ModSounds.HEAD_SHOOT, 10.0F, 1.05F, true);
   }

   public void playBiteAnimation() {
      this.entityData.set(FIRE_START_TIME, this.level().getGameTime());
      this.skullShotDelay = -1;
      this.playSound(ModSounds.HEAD_SNARL, this.roarVolume(0.75F), 0.96F, true);
   }

   private void beginChomp() {
      this.chompTicks = 0;
      this.entityData.set(FIRE_START_TIME, this.level().getGameTime());
      this.skullShotDelay = -1;
      this.playSound(ModSounds.HEAD_SNARL, this.roarVolume(0.75F), 0.99F, true);
      this.playSound(ModSounds.HEAD_CHOMP, this.roarVolume(0.85F), 1.0F, true);
   }

   private void tickChomp(ServerLevel server) {
      ++this.chompTicks;
      if (this.suckedEntity != null && this.suckedEntity.isAlive()) {
         double yawRad = Math.toRadians((double)this.getYRot());
         double pitchRad = Math.toRadians((double)this.getXRot());
         double cosP = Math.cos(pitchRad);
         Vec3 dir = new Vec3(-Math.sin(yawRad) * cosP, -Math.sin(pitchRad), Math.cos(yawRad) * cosP);
         float hs = this.renderScale() / 6.0F;
         double jaw = this.isHungUpsideDown() ? 2.6 : -2.6;
         Vec3 mouth = this.position().add(dir.scale(3.2 * (double)hs)).add((double)0.0F, jaw * (double)hs, (double)0.0F);
         if (this.suckedEntity instanceof Player) {
            Vec3 vel = mouth.subtract(this.suckedEntity.position()).scale((double)0.5F);
            if (vel.length() > 1.2) {
               vel = vel.normalize().scale(1.2);
            }

            this.applyPull(this.suckedEntity, vel);
         } else {
            Vec3 toMouth = mouth.subtract(this.suckedEntity.position());
            double d = toMouth.length();
            Vec3 next = d <= 0.6 ? mouth : this.suckedEntity.position().add(toMouth.scale(Math.min((double)1.0F, 0.6 / d)));
            this.suckedEntity.setPos(next.x, next.y, next.z);
            this.suckedEntity.setDeltaMovement(Vec3.ZERO);
            this.suckedEntity.fallDistance = (double)0.0F;
            this.suckedEntity.hurtMarked = true;
         }

         LivingEntity var15 = this.suckedEntity;
         if (var15 instanceof Player) {
            Player p = (Player)var15;
            Entity var23 = this.level().getEntity(this.getStormId());
            if (var23 instanceof WitherStormEntity) {
               WitherStormEntity ws = (WitherStormEntity)var23;
               ws.lockRotationOn(p);
            }
         }

         if (this.chompTicks == 20) {
            LivingEntity type = this.suckedEntity;
            if (type instanceof ArmorStand) {
               ArmorStand stand = (ArmorStand)type;
               stand.discard();
            } else {
               Holder.Reference<DamageType> damageType = server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(this.chompDamageOverride != null ? this.chompDamageOverride : CHOMP_DAMAGE);
               this.suckedEntity.hurtServer(server, new DamageSource(damageType, this), Float.MAX_VALUE);
            }
         }

         if (this.chompTicks >= 25) {
            this.endChomp();
         }

      } else {
         this.endChomp();
      }
   }

   private void endChomp() {
      LivingEntity var3 = this.suckedEntity;
      if (var3 instanceof Player p) {
         if (p.isAlive()) {
            Level var4 = this.level();
            if (var4 instanceof ServerLevel) {
               ServerLevel sl = (ServerLevel)var4;
               forgive(sl, p);
            }
         }
      }

      this.chompTicks = -1;
      this.suckedEntity = null;
      this.lastVictimPos = null;
      this.chompDamageOverride = null;
      this.target = null;
      this.retargetCooldown = 100;
   }

   private void spawnCrumbParticles(ServerLevel server, BlockPos end, int groundRadius) {
      for(int i = 0; i < 3; ++i) {
         double ang = this.random.nextDouble() * Math.PI * (double)2.0F;
         double rr = (double)groundRadius * Math.sqrt(this.random.nextDouble());
         int bx = end.getX() + (int)Math.round(Math.cos(ang) * rr);
         int bz = end.getZ() + (int)Math.round(Math.sin(ang) * rr);
         BlockPos top = server.getHeightmapPos(WitherStormConfigs.get(server).groundHeightmap(), new BlockPos(bx, 0, bz)).below();
         if (Math.abs(top.getY() - end.getY()) > 5) {
            top = end;
         }

         BlockState state = server.getBlockState(top);
         if (!state.isAir()) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), (double)top.getX() + this.random.nextDouble(), (double)top.getY() + 1.1, (double)top.getZ() + this.random.nextDouble(), 2, (double)0.25F, 0.12, (double)0.25F, 0.06);
         }
      }

   }

   protected void spawnBeamCluster(ServerLevel server, BlockPos end, int groundRadius) {
      int dx = this.random.nextInt(groundRadius * 2 + 1) - groundRadius;
      int dz = this.random.nextInt(groundRadius * 2 + 1) - groundRadius;
      BlockPos surface = server.getHeightmapPos(WitherStormConfigs.get(server).groundHeightmap(), new BlockPos(end.getX() + dx, 0, end.getZ() + dz)).below();
      if (Math.abs(surface.getY() - end.getY()) > 6) {
         surface = end.offset(dx == 0 ? 0 : dx / Math.abs(dx), this.random.nextInt(3) - 1, dz == 0 ? 0 : dz / Math.abs(dz));
         if (server.getBlockState(surface).isAir()) {
            surface = end;
         }
      }

      BlockState state = server.getBlockState(surface);
      if (!state.isAir() && state.getFluidState().isEmpty()) {
         if (this.beamReaches(server, surface)) {
            WitherStormClusterEntity cluster = new WitherStormClusterEntity(ModEntityTypes.WITHER_STORM_CLUSTER, server);
            int radius = this.random.nextInt(2);
            cluster.setOrigin(surface);
            cluster.setRadius(radius);
            BlockPos spawnPos = WitherStormClusterEntity.adjustSpawnOrigin(surface, radius);
            cluster.setPos((double)spawnPos.getX() + (double)0.5F, (double)spawnPos.getY() + (double)0.5F, (double)spawnPos.getZ() + (double)0.5F);
            cluster.absorbBlocks(surface);
            cluster.setBeamHead(this);
            server.addFreshEntity(cluster);
            WitherStormClusterEntity.syncBlocksToTracking(cluster);
         }
      }
   }

   public float extraRoll() {
      return 0.0F;
   }

   protected Float pinnedYaw() {
      return null;
   }

   public Vec3 modelOffset() {
      return DEFAULT_MODEL_OFFSET;
   }

   protected float hostlessScale() {
      return 6.0F;
   }

   protected boolean beamAlwaysOn() {
      return false;
   }

   public boolean isHungUpsideDown() {
      return this.extraRoll() != 0.0F;
   }

   protected LivingEntity forcedTarget() {
      return null;
   }

   private void updateTarget() {
      LivingEntity forced = this.forcedTarget();
      if (forced != null) {
         this.target = forced;
      } else {
         Level var5 = this.level();
         double var10000;
         if (var5 instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)var5;
            var10000 = WitherStormConfigs.get(sl).headTargetRange;
         } else {
            var10000 = (double)96.0F;
         }

         double searchRange = var10000;
         double dropRange = searchRange * 1.4;
         Entity ownerEntity = this.level().getEntity(this.getStormId());
         WitherStormEntity var32;
         if (ownerEntity instanceof WitherStormEntity) {
            WitherStormEntity w = (WitherStormEntity)ownerEntity;
            var32 = w;
         } else {
            var32 = null;
         }

         WitherStormEntity ownerStorm = var32;
         if (ownerEntity instanceof SeveredWitherStormEntity) {
            SeveredWitherStormEntity s = (SeveredWitherStormEntity)ownerEntity;
         } else {
            Object var33 = null;
         }

         if (ownerStorm != null && this.target != null && this.isAbsorbingFood(ownerStorm, this.target)) {
            this.target = null;
            this.targetUnseenTicks = 0;
         }

         if (this.target != null && WitheredMobs.isWithered(this.target)) {
            this.target = null;
            this.targetUnseenTicks = 0;
         }

         if (this.target != null) {
            float yawToTarget = (float)(Mth.atan2(this.target.getZ() - this.getZ(), this.target.getX() - this.getX()) * (180D / Math.PI)) - 90.0F;
            boolean facable = Math.abs(Mth.wrapDegrees(yawToTarget - this.baseYaw)) <= this.yawRange + 10.0F;
            boolean stillValid = !this.distressed && facable && this.target.isAlive() && !this.target.isRemoved() && this.target.level() == this.level() && (double)this.distanceTo(this.target) <= dropRange && this.level().isLoaded(this.target.blockPosition());
            LivingEntity var14 = this.target;
            if (var14 instanceof Player) {
               label160: {
                  Player player = (Player)var14;
                  if (!player.isCreative() && !player.isSpectator() && !ModEffects.isHyperInvisible(player)) {
                     Level var27 = this.level();
                     if (!(var27 instanceof ServerLevel)) {
                        break label160;
                     }

                     ServerLevel sl2 = (ServerLevel)var27;
                     if (!isForgiven(player, sl2.getGameTime())) {
                        break label160;
                     }
                  }

                  stillValid = false;
               }
            }

            if (stillValid && this.tickCount % 10 == 0) {
               if (this.canSee(this.target)) {
                  this.targetUnseenTicks = 0;
               } else {
                  this.targetUnseenTicks += 10;
                  if (this.targetUnseenTicks >= 60) {
                     stillValid = false;
                  }
               }
            }

            if (!stillValid) {
               this.target = null;
               this.targetUnseenTicks = 0;
            }
         }

         if (this.target == null) {
            if (--this.retargetCooldown > 0) {
               return;
            }

            this.retargetCooldown = 20;
            AABB searchBox = this.getBoundingBox().inflate(searchRange);
            List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class, searchBox, (e) -> {
               boolean acceptable;
               if (e.isAlive() && !(e instanceof WitherStormEntity)) {
                  label55: {
                     if (e instanceof Player) {
                        Player p = (Player)e;
                        if (p.isCreative() || p.isSpectator()) {
                           break label55;
                        }
                     }

                     if (e instanceof Player) {
                        Player p = (Player)e;
                        Level patt0$temp = this.level();
                        if (patt0$temp instanceof ServerLevel) {
                           ServerLevel sl3 = (ServerLevel)patt0$temp;
                           if (isForgiven(p, sl3.getGameTime())) {
                              break label55;
                           }
                        }
                     }

                     if (e instanceof Player) {
                        Player p = (Player)e;
                        if (ModEffects.isHyperInvisible(p)) {
                           break label55;
                        }
                     }

                     if (e instanceof Player) {
                        Player p = (Player)e;
                        if (p.isBlocking()) {
                           break label55;
                        }
                     }

                     if ((ownerStorm == null || !this.isAbsorbingFood(ownerStorm, e)) && !WitheredMobs.isWithered(e) && !WitherSickness.isTurning(e) && !(e instanceof IronGolem)) {
                        acceptable = true;
                        return acceptable;
                     }
                  }
               }

               acceptable = false;
               return acceptable;
            });
            List<WitherStormHeadEntity> siblings = this.level().getEntitiesOfClass(WitherStormHeadEntity.class, this.getBoundingBox().inflate((double)96.0F), (h) -> h != this && h.getStormId() == this.getStormId());
            List<LivingEntity> eligible = new ArrayList();

            for(LivingEntity candidate : candidates) {
               float yawTo = (float)(Mth.atan2(candidate.getZ() - this.getZ(), candidate.getX() - this.getX()) * (180D / Math.PI)) - 90.0F;
               if (!(Math.abs(Mth.wrapDegrees(yawTo - this.baseYaw)) > this.yawRange - 15.0F)) {
                  boolean taken = false;

                  for(WitherStormHeadEntity sibling : siblings) {
                     if (sibling.target == candidate) {
                        taken = true;
                        break;
                     }
                  }

                  if (!taken) {
                     eligible.add(candidate);
                  }
               }
            }

            eligible.sort(Comparator.comparingDouble(this::distanceToSqr));
            LivingEntity chosen = null;
            int losChecks = 0;

            for(LivingEntity candidate : eligible) {
               ++losChecks;
               if (losChecks > 8) {
                  break;
               }

               if (this.canSee(candidate)) {
                  chosen = candidate;
                  break;
               }
            }

            this.target = chosen;
            this.targetUnseenTicks = 0;
         }

      }
   }

   private boolean isAbsorbingFood(WitherStormEntity storm, LivingEntity e) {
      if (storm != null && !(e instanceof Player)) {
         if (storm.isDoomed(e)) {
            return true;
         } else {
            Vec3 c = storm.getBoundingBox().getCenter();
            double dx = e.getX() - c.x;
            double dz = e.getZ() - c.z;
            return dx * dx + dz * dz < (double)576.0F;
         }
      } else {
         return false;
      }
   }

   private boolean beamReaches(ServerLevel server, BlockPos target) {
      Vec3 from = this.getEyePosition();
      Vec3 to = Vec3.atCenterOf(target);
      BlockHitResult hit = server.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, this));
      if (hit.getType() == Type.MISS) {
         return true;
      } else {
         BlockPos struck = hit.getBlockPos();
         return struck.equals(target) || struck.distSqr(target) <= (double)2.0F;
      }
   }

   private boolean canSee(LivingEntity entity) {
      BlockHitResult hit = this.level().clip(new ClipContext(this.getEyePosition(), entity.getEyePosition(), Block.COLLIDER, Fluid.NONE, this));
      return hit.getType() == Type.MISS;
   }

   private void updateWander() {
      if (--this.wanderRetimer <= 0) {
         this.wanderRetimer = 80 + this.random.nextInt(80);
         this.wanderYaw = this.baseYaw + (this.random.nextFloat() * 2.0F - 1.0F) * this.yawRange * 0.7F;
         this.wanderPitch = this.random.nextFloat() * 54.000004F;
      }

   }

   private float beamSeparationNudge() {
      if (this.isBeamActive() && this.level() instanceof ServerLevel) {
         Vec3 myEnd = this.getBeamEndExact();
         if (myEnd.lengthSqr() < 1.0E-6) {
            return 0.0F;
         } else {
            List<WitherStormHeadEntity> siblings = this.level().getEntitiesOfClass(WitherStormHeadEntity.class, this.getBoundingBox().inflate((double)96.0F), (h) -> h != this && h.getStormId() == this.getStormId() && h.isBeamActive());
            float nudge = 0.0F;

            for(WitherStormHeadEntity sib : siblings) {
               Vec3 sEnd = sib.getBeamEndExact();
               if (!(sEnd.lengthSqr() < 1.0E-6)) {
                  double dx = myEnd.x - sEnd.x;
                  double dz = myEnd.z - sEnd.z;
                  double dist = Math.sqrt(dx * dx + dz * dz);
                  if (!(dist >= (double)9.0F)) {
                     float myBearing = (float)(Mth.atan2(myEnd.z - this.getZ(), myEnd.x - this.getX()) * (180D / Math.PI));
                     float sibBearing = (float)(Mth.atan2(sEnd.z - this.getZ(), sEnd.x - this.getX()) * (180D / Math.PI));
                     float diff = Mth.degreesDifference(sibBearing, myBearing);
                     float dir = Math.abs(diff) < 0.5F ? (this.getHeadIndex() < sib.getHeadIndex() ? 1.0F : -1.0F) : Math.signum(diff);
                     float overlap = (float)(((double)9.0F - dist) / (double)9.0F);
                     nudge += dir * overlap * 22.0F;
                  }
               }
            }

            float bound = Math.max(this.yawRange * 0.5F, 4.0F);
            return Mth.clamp(nudge, -bound, bound);
         }
      } else {
         return 0.0F;
      }
   }

   private float clampToBase(float wanted, float base, float maxDelta) {
      float delta = Mth.wrapDegrees(wanted - base);
      delta = Mth.clamp(delta, -maxDelta, maxDelta);
      return base + delta;
   }

   private float smoothDampAngle(float current, float target, float[] velocity, float smoothTime) {
      float delta = Mth.wrapDegrees(target - current);
      target = current + delta;
      float omega = 2.0F / smoothTime;
      float x = omega * 0.05F;
      float exp = 1.0F / (1.0F + x + 0.48F * x * x + 0.235F * x * x * x);
      float change = current - target;
      float temp = (velocity[0] + omega * change) * 0.05F;
      velocity[0] = (velocity[0] - omega * temp) * exp;
      return target + (change + temp) * exp;
   }

   public boolean isNoGravity() {
      return true;
   }

   public boolean isPickable() {
      return !this.isRemoved();
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      this.entityData.set(HURT_START_TIME, level.getGameTime());
      this.playSound(ModSounds.HEAD_HURT, this.roarVolume(0.75F), 0.9F - this.random.nextFloat() * 0.05F, true);
      if (this.random.nextInt(3) == 0) {
         this.playSound(ModSounds.HEAD_STAB_EYE, this.roarVolume(0.75F), 1.0F, false);
      }

      return true;
   }

   public void remove(Entity.RemovalReason reason) {
      if (!this.level().isClientSide() && this.beamWasActive) {
         this.playSound(ModSounds.HEAD_BEAM_DEACTIVATE, this.beamVolume(), 1.0F, true);
      }

      if (!this.level().isClientSide()) {
         Level var3 = this.level();
         if (var3 instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)var3;
            this.releaseAll(sl, true);
         }
      }

      super.remove(reason);
   }

   protected void readAdditionalSaveData(ValueInput input) {
      String storm = input.getStringOr("StormUUID", "");
      this.stormUUID = storm.isEmpty() ? null : UUID.fromString(storm);
      this.headIndex = input.getIntOr("HeadIndex", 0);
      this.entityData.set(SPAWN_GAME_TIME, input.getLongOr("SpawnGameTime", -1L));
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      if (this.stormUUID != null) {
         output.putString("StormUUID", this.stormUUID.toString());
      }

      output.putInt("HeadIndex", this.headIndex);
      output.putLong("SpawnGameTime", (Long)this.entityData.get(SPAWN_GAME_TIME));
   }

   static {
      STORM_ID = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.INT);
      HEAD_INDEX_DATA = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.INT);
      HEAD_LOCAL_YAW = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.FLOAT);
      HEAD_ROLL = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.FLOAT);
      SPAWN_GAME_TIME = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.LONG);
      FIRE_START_TIME = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.LONG);
      HURT_START_TIME = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.LONG);
      ROAR_START_TIME = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.LONG);
      VOCAL_TIME = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.LONG);
      SUCKED_ID = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.INT);
      BEAM_END = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
      BEAM_END_EXACT = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.VECTOR3);
      LIT = SynchedEntityData.defineId(WitherStormHeadEntity.class, EntityDataSerializers.FLOAT);
      justEscaped = new HashMap();
      FORGIVEN_UNTIL = new HashMap();
      CHOMP_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("dabywitherstormmod", "chomp"));
      DEFAULT_MODEL_OFFSET = new Vec3((double)0.0F, (double)-1.25F, (double)0.0F);
   }

   private static record Echo(int[] delay, SoundEvent event, float volume, float pitch) {
   }
}
