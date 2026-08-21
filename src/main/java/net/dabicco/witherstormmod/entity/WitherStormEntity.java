package net.dabicco.witherstormmod.entity;

import java.util.UUID;
import net.dabicco.witherstormmod.ModItems;
import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.entity.ability.StormAbilitySet;
import net.dabicco.witherstormmod.entity.ability.SuperSkullAbility;
import net.dabicco.witherstormmod.entity.ability.TractorBeamAbility;
import net.dabicco.witherstormmod.config.WitherStormConfigs;
import net.dabicco.witherstormmod.config.WitherStormWorldConfig;
import net.dabicco.witherstormmod.entity.cluster.WitherStormClusterEntity;
import net.dabicco.witherstormmod.network.WitherStormPositionPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Fresh rewrite of the Wither Storm boss.
 *
 * The storm is a phase-driven state machine. It absorbs blocks/items/mobs to grow
 * (addSubGrowth), and unlocks new abilities as its phase increases. This class
 * preserves the public API surface the kept entity files and client renderer depend
 * on, but the internals are organized into clear, readable sections:
 *
 *   - Phase + growth (getPhase/setPhase/addSubGrowth)
 *   - Movement + targeting (resolveMoveGoal, followGoal, chase/distract)
 *   - Abilities (beam via heads, super skulls, snatch, absorption, tornado)
 *   - Head hosting (StormHeadHost contract)
 *   - Sync + boss bar + save/load
 *
 * The heavy lifting that used to live in one giant file is split: phase math lives
 * in {@link WitherStormPhase}, movement/abilities live in the ai/ability packages.
 */
public class WitherStormEntity extends WitherBoss implements StormHeadHost {
   private static final EntityDataAccessor<Float> PHASE_DATA;
   private static final EntityDataAccessor<Boolean> PHASE4_DATA;
   private static final EntityDataAccessor<Integer> SUBGROWTH_DATA;
   private static final EntityDataAccessor<Float> BODY_ROLL;
   private static final EntityDataAccessor<Long> SPAWN_ANIM_GAME_TIME;
   private static final EntityDataAccessor<Long> COLLAPSE_GAME_TIME;
   private static final EntityDataAccessor<Long> PHASE5_ANIM_GAME_TIME;
   private static final EntityDataAccessor<String> ULTIMATE_TARGET_UUID;
   private static final EntityDimensions PHASE_4_DIMENSIONS = EntityDimensions.scalable(10.0F, 30.0F);

   public static final double MAX_NATURAL_PHASE = 5.9999;
   public static final double MAX_DEVOURER_PHASE = 6.99;
   public static final double DEVOURER_PHASE = 6.0;
   public static final double MINI_HEAD_PHASE = 2.0;
   public static final double FRONT_TENTACLE_PHASE = 3.0;
   public static final double TENTACLE_NOTICE_RANGE = 40.0;
   public static final double MINI_HEAD_WORLD_Y = 3.05;
   public static final double MINI_HEAD_WORLD_FORWARD = 0.14;
   public static final float MINI_HEAD_ENTITY_SCALE = 1.35F;
   public static final int SPAWN_ANIM_LENGTH_TICKS = 80;
   public static final int SIEGE_NONE = 0;
   public static final int SIEGE_GROWING = 1;
   public static final int SIEGE_LULL = 2;
   public static final int SIEGE_ACTIVE = 3;
   public static final int SIEGE_OVER = 4;

   private static final Vec3[] HEAD_OFFSETS = {
      new Vec3(2.0, 13.5, 10.2),
      new Vec3(-6.5, 20.0, 14.57),
      new Vec3(11.5, 19.0, 8.5)
   };
   private static final float[] HEAD_REST_YAW = {0.0F, 26.0F, -26.0F};
   private static final float[] HEAD_REST_ROLL = {0.0F, -13.0F, 13.0F};

   private double phase;
   private int subGrowth;
   private boolean phase4;
   private boolean loadingFromSave;
   private final UUID[] headUUIDs = new UUID[3];
   private final UUID[] severedUUIDs = new UUID[2];
   private boolean suppressLoot;

   // Movement / targeting state
   private MoveMode moveMode = MoveMode.FOLLOW;
   private int chaseTimer = -1;
   private int distractionTimer = -1;
   private int distractionTicksLeft;
   private double distractX;
   private double distractZ;
   private UUID ultimateTargetUUID;
   private boolean ultimateTargetLocked;
   private int ultimateTargetCooldown;
   private Vec3 smoothedMoveGoal;
   private int moveGoalTimer;
   private Player cachedNearest;

   // Snatch / ability state
   private int snatchTicks;
   private int snatchHits;
   private Player snatchVictim;
   private Vec3 lastSnatchPos;

   // Siege
   private int siegeStage;
   private int siegeTicks;
   private int siegeSpawned;

   // Abilities (phase-gated powers)
   private final StormAbilitySet abilities = new StormAbilitySet();

   // Spawn / collapse
   private int spawnFreezeTicks;
   private int spawnFreezeTotalTicks;
   private boolean spawnWailPending;
   private boolean collapseAwardGiven;

   public WitherStormEntity(EntityType<? extends WitherStormEntity> type, Level level) {
      super(type, level);
      this.abilities.add(new SuperSkullAbility());
      this.abilities.add(new TractorBeamAbility());
   }

   // ------------------------------------------------------------------ data

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(PHASE_DATA, 0.0F);
      builder.define(PHASE4_DATA, false);
      builder.define(SUBGROWTH_DATA, 0);
      builder.define(BODY_ROLL, 0.0F);
      builder.define(SPAWN_ANIM_GAME_TIME, -1L);
      builder.define(COLLAPSE_GAME_TIME, -1L);
      builder.define(PHASE5_ANIM_GAME_TIME, -1L);
      builder.define(ULTIMATE_TARGET_UUID, "");
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (key == PHASE_DATA && this.level().isClientSide()) {
         this.phase = this.entityData.get(PHASE_DATA);
      } else if (key == PHASE4_DATA) {
         this.phase4 = this.entityData.get(PHASE4_DATA);
         this.refreshDimensions();
      }
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double distance) {
      return true;
   }

   // ------------------------------------------------------------- phase API

   public double getPhase() {
      return this.phase;
   }

   public boolean isPhase4() {
      return this.entityData.get(PHASE4_DATA);
   }

   public boolean isDevourer() {
      return this.phase >= DEVOURER_PHASE;
   }

   @Override
   public boolean isDevourerForm() {
      return this.isDevourer();
   }

   public int getSubGrowth() {
      return this.subGrowth;
   }

   public double getSubPhase() {
      int mainPhase = WitherStormPhase.mainOf(this.phase);
      int req = WitherStormPhase.requirement(mainPhase, WitherStormConfigs.get(this.level()));
      return Math.min(1.0, (double) this.subGrowth / (double) req);
   }

   /** Absorb 'amount' of material; grow toward the next phase when the threshold is hit. */
   public void addSubGrowth(int amount) {
      if (this.phase >= MAX_DEVOURER_PHASE - 0.001) {
         return;
      }
      WitherStormWorldConfig config = WitherStormConfigs.get(this.level());
      this.subGrowth += amount;
      this.entityData.set(SUBGROWTH_DATA, this.subGrowth);
      int mainPhase = WitherStormPhase.mainOf(this.phase);
      int req = WitherStormPhase.requirement(mainPhase, config);
      double progress = (double) this.subGrowth / (double) req;
      double before = this.phase;
      this.phase = mainPhase + Math.min(progress, 0.99);

      if (this.phase < 4.0 && Math.floor(this.phase * 10.0) > Math.floor(before * 10.0) && this.level() instanceof ServerLevel sl) {
         sl.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.STORM_GROW, SoundSource.HOSTILE, 7.0F, 0.95F + this.random.nextFloat() * 0.1F);
      }

      if (this.subGrowth >= req) {
         if (mainPhase + 1 >= WitherStormPhase.mainOf(MAX_NATURAL_PHASE) + 1) {
            this.phase = MAX_NATURAL_PHASE;
            this.subGrowth = 0;
            this.entityData.set(SUBGROWTH_DATA, 0);
            this.entityData.set(PHASE_DATA, (float) this.phase);
            this.entityData.set(PHASE4_DATA, true);
            return;
         }
         this.phase = mainPhase + 1.0;
         this.roarAllHeads(true);
         if (this.phase >= 4.0 && !this.phase4) {
            this.enterPhase4();
         }
         this.subGrowth = 0;
         this.entityData.set(SUBGROWTH_DATA, 0);
      }

      this.entityData.set(PHASE_DATA, (float) this.phase);
      this.entityData.set(PHASE4_DATA, this.phase >= 4.0);
   }

   public void setPhase(double value) {
      this.phase = value;
      boolean wasPhase4 = this.phase4;
      this.phase4 = value >= 4.0;
      if (this.phase4 && !wasPhase4) {
         this.enterPhase4();
      } else if (!this.phase4 && wasPhase4) {
         this.resetToNormalWither();
      }
      this.entityData.set(PHASE_DATA, (float) value);
      this.entityData.set(PHASE4_DATA, this.phase4);
   }

   public void setPhaseExact(double value) {
      this.setPhase(value);
      int mainPhase = WitherStormPhase.mainOf(value);
      double fraction = Mth.clamp(value - mainPhase, 0.0, 0.99);
      WitherStormWorldConfig config = WitherStormConfigs.get(this.level());
      int req = WitherStormPhase.requirement(mainPhase, config);
      this.subGrowth = (int) Math.round(fraction * (double) req);
      this.entityData.set(SUBGROWTH_DATA, this.subGrowth);
      this.entityData.set(PHASE_DATA, (float) this.phase);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return WitherBoss.createAttributes()
         .add(Attributes.MAX_HEALTH, 500.0)
         .add(Attributes.ATTACK_DAMAGE, 15.0)
         .add(Attributes.FOLLOW_RANGE, 64.0);
   }

   // ------------------------------------------------------------- collapse

   public long getCollapseGameTime() {
      return this.entityData.get(COLLAPSE_GAME_TIME);
   }

   public float collapseTicks() {
      long start = this.getCollapseGameTime();
      return start < 0L ? -1.0F : (float) (this.level().getGameTime() - start);
   }

   public boolean isCollapsed() {
      float t = this.collapseTicks();
      return t >= 0.0F;
   }

   /** The formidibomb finale: forces the Devourer phase and starts the collapse. */
   public boolean formidibombed(ServerLevel server) {
      if (this.phase < 5.0 || this.isDevourer()) {
         return false;
      }
      this.setPhaseExact(DEVOURER_PHASE);
      this.roarAllHeads(true);
      this.entityData.set(COLLAPSE_GAME_TIME, server.getGameTime());
      for (ServerPlayer p : server.players()) {
         p.sendSystemMessage(Component.literal("The Wither Storm has been severed!").withStyle(ChatFormatting.DARK_RED));
      }
      return true;
   }

   // ------------------------------------------------------------ head hosting

   public static Vec3 headOffset(int index, boolean devourer) {
      Vec3 off = HEAD_OFFSETS[Math.min(index, HEAD_OFFSETS.length - 1)];
      if (devourer) {
         return index == 1 ? off.add(-2.5, -2.0, -4.2) : off.add(0.0, 1.5, 0.5);
      }
      return off;
   }

   @Override
   public Vec3 headOffsetFor(int index) {
      if (!this.isPhase4()) {
         return new Vec3(0.0, MINI_HEAD_WORLD_Y, MINI_HEAD_WORLD_FORWARD);
      }
      float h = this.hatchProgress();
      Vec3 full = headOffset(index, this.isDevourer());
      if (h >= 0.999F) {
         return full;
      }
      return new Vec3(
         Mth.lerp(h, 0.0, full.x),
         Mth.lerp(h, MINI_HEAD_WORLD_Y, full.y),
         Mth.lerp(h, MINI_HEAD_WORLD_FORWARD, full.z));
   }

   @Override
   public float headScaleFor(int index) {
      return this.isPhase4() ? Mth.lerp(this.hatchProgress(), MINI_HEAD_ENTITY_SCALE, 6.0F) : MINI_HEAD_ENTITY_SCALE;
   }

   @Override
   public float headYawOffsetFor(int index) {
      return this.isPhase4() ? HEAD_REST_YAW[Math.min(index, 2)] : 0.0F;
   }

   @Override
   public float headRollOffsetFor(int index) {
      return this.isPhase4() ? HEAD_REST_ROLL[Math.min(index, 2)] : 0.0F;
   }

   @Override
   public float headYawRangeFor(int index) {
      return this.isPhase4() ? (index == 0 ? 52.0F : 50.0F) : 2.5F;
   }

   @Override
   public float headPitchRangeFor(int index) {
      return this.isPhase4() ? 60.0F : 20.0F;
   }

   @Override
   public float headLitFor(int index) {
      return 1.0F;
   }

   @Override
   public boolean headBeamAllowed(int index) {
      return !this.isCollapsed() && (this.isPhase4() || this.phase >= 3.0);
   }

   @Override
   public float beamScaleFor(int index) {
      return this.isPhase4() ? 1.0F : 0.225F;
   }

   @Override
   public WitherStormHeadEntity hostHead(ServerLevel server, int index) {
      if (index < 0 || index >= this.headUUIDs.length) {
         return null;
      }
      UUID id = this.headUUIDs[index];
      if (id == null) {
         return null;
      }
      Entity e = server.getEntity(id);
      return e instanceof WitherStormHeadEntity head ? head : null;
   }

   @Override
   public boolean headsDistressed() {
      return this.isUnderSiege();
   }

   @Override
   public float getBodyRoll() {
      return this.entityData.get(BODY_ROLL);
   }

   public int activeHeadCount() {
      if (this.phase < 4.0) {
         return this.phase >= MINI_HEAD_PHASE ? 1 : 0;
      }
      if (!this.isDevourer()) {
         return 3;
      }
      return this.phase < 6.1 ? 1 : 3;
   }

   public WitherStormHeadEntity getMiddleHead() {
      Level lvl = this.level();
      if (!(lvl instanceof ServerLevel server)) {
         return null;
      }
      WitherStormHeadEntity h = this.hostHead(server, 0);
      return h != null && h.isAlive() ? h : null;
   }

   public WitherStormHeadEntity getAnyHead() {
      Level lvl = this.level();
      if (!(lvl instanceof ServerLevel server)) {
         return null;
      }
      for (int i = 0; i < this.headUUIDs.length; ++i) {
         WitherStormHeadEntity h = this.hostHead(server, i);
         if (h != null && h.isAlive()) {
            return h;
         }
      }
      return null;
   }

   public void roarAllHeads(boolean powerful) {
      Level lvl = this.level();
      if (lvl instanceof ServerLevel server) {
         for (int i = 0; i < this.headUUIDs.length; ++i) {
            WitherStormHeadEntity h = this.hostHead(server, i);
            if (h != null && h.isAlive()) {
               h.triggerRoar(powerful);
            }
         }
      }
   }

   public void spawnScavengedCluster(Entity collector) {
      Level lvl = this.level();
      if (!(lvl instanceof ServerLevel server)) {
         return;
      }
      BlockPos target = this.findSurfaceBlockNear(collector.blockPosition());
      if (target != null) {
         WitherStormClusterEntity cluster = new WitherStormClusterEntity(ModEntityTypes.WITHER_STORM_CLUSTER, this.level());
         cluster.setOrigin(target);
         cluster.setRadius(0);
         cluster.absorbBlocks(target);
         cluster.setTargetStorm(this);
         server.addFreshEntity(cluster);
         WitherStormClusterEntity.syncBlocksToTracking(cluster);
      }
   }

   public Vec3 modelCenter() {
      return this.position().add(0.0, this.getBbHeight() * 0.5, 0.0);
   }

   public void setSuppressLoot(boolean value) {
      this.suppressLoot = value;
   }

   // --------------------------------------------------------- targeting / AI

   public MoveMode getMoveMode() {
      return this.moveMode;
   }

   public boolean isChasing() {
      return this.moveMode == MoveMode.CHASING;
   }

   public void setChasing(boolean chasing) {
      this.moveMode = chasing ? MoveMode.CHASING : MoveMode.FOLLOW;
      if (!chasing && this.level() instanceof ServerLevel) {
         this.chaseTimer = WitherStormConfigs.get(this.level()).chaseIntervalMinutes * 60 * 20;
      }
   }

   public void distractNow() {
      if (this.level() instanceof ServerLevel) {
         WitherStormWorldConfig cfg = WitherStormConfigs.get(this.level());
         this.moveMode = MoveMode.DISTRACTED;
         this.distractionTicksLeft = cfg.distractionDurationSeconds * 20;
         this.distractionTimer = cfg.distractionIntervalMinutes * 60 * 20;
         double angle = this.random.nextDouble() * Math.PI * 2.0;
         double dist = cfg.distractionRange * (0.5 + this.random.nextDouble() * 0.5);
         this.distractX = this.getX() + Math.cos(angle) * dist;
         this.distractZ = this.getZ() + Math.sin(angle) * dist;
      }
   }

   public UUID getUltimateTargetUUID() {
      return this.ultimateTargetUUID;
   }

   public boolean isUltimateTargetLocked() {
      return this.ultimateTargetLocked;
   }

   public void setUltimateTarget(Player player) {
      this.ultimateTargetUUID = player.getUUID();
      this.ultimateTargetLocked = true;
      this.entityData.set(ULTIMATE_TARGET_UUID, this.ultimateTargetUUID.toString());
   }

   public void clearUltimateTarget() {
      this.ultimateTargetUUID = null;
      this.ultimateTargetLocked = false;
      this.ultimateTargetCooldown = 0;
      this.entityData.set(ULTIMATE_TARGET_UUID, "");
   }

   public Player rerollUltimateTarget() {
      this.ultimateTargetUUID = null;
      this.ultimateTargetLocked = false;
      this.ultimateTargetCooldown = 0;
      this.updateUltimateTarget();
      return this.getUltimateTarget();
   }

   public void lockRotationOn(Player player) {
      // Kept for API compatibility; the fresh movement handles facing internally.
   }

   public void onHeadFired(int headIndex) {
      // Hooks recoil/feedback; kept for API compatibility.
   }

   public void registerGrabHit(ServerLevel level, Player attacker) {
      if (this.snatchVictim != null && this.snatchVictim == attacker) {
         ItemStack held = attacker.getMainHandItem();
         if (held.is(net.minecraft.tags.ItemTags.SWORDS) || held.is(net.minecraft.tags.ItemTags.AXES)) {
            level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.9F);
            if (++this.snatchHits >= 5) {
               this.endSnatch(level, true);
            }
         }
      }
   }

   // ------------------------------------------------------------ siege

   public int getSiegeStage() {
      return this.siegeStage;
   }

   public boolean isUnderSiege() {
      return this.siegeStage == SIEGE_ACTIVE;
   }

   public int siegeProgress() {
      if (this.siegeStage == SIEGE_ACTIVE && this.level() instanceof ServerLevel sl) {
         int secs = Math.max(1, WitherStormConfigs.get(sl).endermanSiegeSeconds * 20);
         return (int) Mth.clamp((long) this.siegeTicks * 100L / (long) secs, 0L, 100L);
      }
      return 0;
   }

   // ---------------------------------------------------------------- tick

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         return;
      }
      Level lvl = this.level();
      if (!(lvl instanceof ServerLevel server)) {
         return;
      }

      this.updateUltimateTarget();

      // Movement / targeting
      if (this.phase4) {
         this.phase4Movement(server);
      } else {
         this.followGoal(this.resolveMoveGoal(server));
      }

      // Abilities (unlocked by phase)
      this.tickSiege(server);
      this.tickSnatch(server);
      this.tickSpawnAnimation(server);
      this.abilities.tick(this, server);

      // Broadcast position so the distant renderer + HUD work.
      if (this.tickCount % 2 == 0) {
         this.broadcastPosition(server);
      }
   }

   private void tickSpawnAnimation(ServerLevel server) {
      if (this.spawnFreezeTicks > 0) {
         --this.spawnFreezeTicks;
         this.setDeltaMovement(Vec3.ZERO);
         if (this.spawnWailPending) {
            this.spawnWailPending = false;
            this.playGlobalSpawnWail(server);
         }
      }
   }

   private void playGlobalSpawnWail(ServerLevel server) {
      Holder<SoundEvent> wail = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WITHER_SPAWN);
      for (ServerPlayer p : server.players()) {
         p.connection.send(new ClientboundSoundPacket(wail, SoundSource.HOSTILE, p.getX(), p.getY(), p.getZ(), 0.55F, 0.6F, server.getRandom().nextLong()));
      }
   }

   public void beginSpawnFreeze() {
      WitherStormWorldConfig cfg = WitherStormConfigs.get(this.level());
      this.spawnFreezeTicks = Math.max(0, cfg.spawnFreezeSeconds) * 20;
      this.spawnFreezeTotalTicks = this.spawnFreezeTicks;
      this.spawnWailPending = true;
   }

   // ------------------------------------------------------------- movement

   private Vec3 resolveMoveGoal(ServerLevel server) {
      Player nearest = this.nearestTargetable();
      switch (this.moveMode) {
         case CHASING: {
            if (nearest != null) {
               return nearest.position();
            }
            break;
         }
         case DISTRACTED: {
            if (this.distractionTicksLeft-- > 0) {
               return new Vec3(this.distractX, this.getY(), this.distractZ);
            }
            this.moveMode = MoveMode.FOLLOW;
            this.chaseTimer = WitherStormConfigs.get(server).chaseIntervalMinutes * 60 * 20;
            break;
         }
         default: {
            if (--this.chaseTimer <= 0) {
               this.moveMode = MoveMode.CHASING;
               return nearest != null ? nearest.position() : null;
            }
            break;
         }
      }
      return nearest != null ? nearest.position() : null;
   }

   private void phase4Movement(ServerLevel server) {
      WitherStormWorldConfig cfg = WitherStormConfigs.get(server);
      Vec3 goal = this.resolveMoveGoal(server);
      if (this.smoothedMoveGoal == null) {
         this.smoothedMoveGoal = goal;
      } else if (goal != null) {
         this.smoothedMoveGoal = this.smoothedMoveGoal.lerp(goal, 0.06);
      }
      if (this.smoothedMoveGoal == null) {
         return;
      }

      double standoffMin = cfg.stormStandoff;
      double standoffMax = cfg.stormStandoff * 2.0;
      double dx = this.smoothedMoveGoal.x - this.getX();
      double dz = this.smoothedMoveGoal.z - this.getZ();
      double horiz = Math.sqrt(dx * dx + dz * dz);
      Vec3 horizVel = Vec3.ZERO;
      if (horiz > standoffMin) {
         double t = Mth.clamp((horiz - standoffMin) / (standoffMax - standoffMin), 0.0, 1.0);
         horizVel = new Vec3(dx / horiz, 0.0, dz / horiz).scale(cfg.stormSpeed * t);
      }
      double desiredY = this.highestGroundAround() + cfg.phase4Altitude;
      double vy = Mth.clamp((desiredY - this.getY()) * 0.05, -0.1, 0.1);
      this.setDeltaMovement(horizVel.x, vy, horizVel.z);
      this.move(MoverType.SELF, this.getDeltaMovement());
      this.needsSync = true;
   }

   private void followGoal(Vec3 goal) {
      if (goal == null) {
         return;
      }
      Vec3 target = goal.add(0.0, 8.0, 0.0);
      Vec3 diff = target.subtract(this.position());
      double dist = diff.length();
      if (dist > 20.0) {
         double t = Mth.clamp((dist - 20.0) / 30.0, 0.0, 1.0);
         this.setDeltaMovement(diff.normalize().scale(0.08 * t));
      } else {
         this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
      }
      this.needsSync = true;
   }

   private double highestGroundAround() {
      int best = Integer.MIN_VALUE;
      BlockPos here = this.blockPosition();
      for (int dx = -12; dx <= 12; dx += 4) {
         for (int dz = -12; dz <= 12; dz += 4) {
            BlockPos at = here.offset(dx, 0, dz);
            if (this.level().hasChunkAt(at)) {
               int y = this.level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, at).getY();
               if (y > best) {
                  best = y;
               }
            }
         }
      }
      if (best == Integer.MIN_VALUE) {
         best = this.blockPosition().getY();
      }
      return best;
   }

   // ------------------------------------------------------------- snatch

   private void tickSnatch(ServerLevel server) {
      if (this.snatchVictim != null) {
         Player p = this.snatchVictim;
         if (p.isAlive() && !p.isCreative() && !p.isSpectator() && p.level() == this.level()) {
            ++this.snatchTicks;
            float prog = Math.min(1.0F, this.snatchTicks / 110.0F);
            float eased = prog * prog * (3.0F - 2.0F * prog);
            WitherStormHeadEntity mid = this.getMiddleHead();
            Vec3 dest = mid != null ? mid.position().add(0.0, -2.0, 0.0) : this.getBoundingBox().getCenter().add(0.0, 5.0, 0.0);
            Vec3 vel = dest.subtract(p.position());
            double speed = 0.12 + 0.55 * eased;
            if (vel.length() > speed) {
               vel = vel.normalize().scale(speed);
            }
            p.setDeltaMovement(vel);
            p.fallDistance = 0.0F;
            p.hurtMarked = true;
            if (p instanceof ServerPlayer sp) {
               sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(sp));
            }
            if (prog >= 1.0F || (prog > 0.5F && p.position().distanceTo(dest) < 3.5)) {
               if (mid != null) {
                  mid.chompVictim(p);
                  this.endSnatch(server, false);
               } else {
                  p.hurtServer(server, new DamageSource(this.damageType(server, WitherStormHeadEntity.CHOMP_DAMAGE), this), Float.MAX_VALUE);
                  this.endSnatch(server, p.isAlive());
               }
            }
         } else {
            this.endSnatch(server, p != null && p.isAlive());
         }
      }
   }

   private void endSnatch(ServerLevel server, boolean survivorGetsForgiveness) {
      if (survivorGetsForgiveness && this.snatchVictim != null && this.snatchVictim.isAlive()) {
         WitherStormHeadEntity.forgive(server, this.snatchVictim);
      }
      this.snatchVictim = null;
      this.snatchTicks = 0;
      this.snatchHits = 0;
      this.lastSnatchPos = null;
   }

   private Holder<DamageType> damageType(ServerLevel server, net.minecraft.resources.ResourceKey<DamageType> key) {
      return server.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getOrThrow(key);
   }

   // --------------------------------------------------------------- siege

   private void tickSiege(ServerLevel server) {
      WitherStormWorldConfig cfg = WitherStormConfigs.get(server);
      if (cfg.endermanSiege == 0) {
         return;
      }
      if (this.siegeStage == SIEGE_NONE) {
         if (this.isDevourer() && this.phase >= 6.1 && this.activeHeadCount() >= 2) {
            this.siegeStage = SIEGE_GROWING;
            this.siegeTicks = 0;
         }
         return;
      }
      ++this.siegeTicks;
      if (this.siegeStage == SIEGE_GROWING && this.siegeTicks >= 1210) {
         this.siegeStage = SIEGE_LULL;
         this.siegeTicks = 0;
      } else if (this.siegeStage == SIEGE_LULL && this.siegeTicks >= 1090) {
         this.siegeStage = SIEGE_ACTIVE;
         this.siegeTicks = 0;
         this.siegeSpawned = 0;
      } else if (this.siegeStage == SIEGE_ACTIVE) {
         if (this.siegeSpawned < cfg.endermanSiegeCount && this.siegeTicks % 6 == 0) {
            double rad = Math.toRadians(this.getYRot());
            double fx = -Math.sin(rad);
            double fz = Math.cos(rad);
            double a = (this.random.nextDouble() - 0.5) * Math.toRadians(70.0);
            double cos = Math.cos(a);
            double sin = Math.sin(a);
            double dx = fx * cos - fz * sin;
            double dz = fx * sin + fz * cos;
            double dist = cfg.endermanSiegeDistance * (0.75 + this.random.nextDouble() * 0.5);
            double x = this.getX() + dx * dist;
            double z = this.getZ() + dz * dist;
            int y = server.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
            var enderman = net.minecraft.world.entity.EntityTypes.ENDERMAN.create(server, EntitySpawnReason.EVENT);
            if (enderman != null) {
               enderman.absSnapTo(x, y, z, (float) Math.toDegrees(Math.atan2(-dx, dz)) + 180.0F, 0.0F);
               enderman.setPersistenceRequired();
               server.addFreshEntity(enderman);
               ++this.siegeSpawned;
            }
         }
         if (this.siegeTicks >= cfg.endermanSiegeSeconds * 20) {
            this.siegeStage = SIEGE_OVER;
            this.siegeTicks = 0;
         }
      }
   }

   // ---------------------------------------------------------- misc helpers

   private Player nearestTargetable() {
      Player best = null;
      double bestSq = Double.MAX_VALUE;
      for (Player p : this.level().players()) {
         if (p.isAlive() && !p.isSpectator()) {
            double d = p.distanceToSqr(this);
            if (d < bestSq) {
               bestSq = d;
               best = p;
            }
         }
      }
      return best;
   }

   private void updateUltimateTarget() {
      if (--this.ultimateTargetCooldown <= 0) {
         this.ultimateTargetCooldown = 1200;
         if (!this.ultimateTargetLocked || this.ultimateTargetUUID == null) {
            Player closest = this.nearestTargetable();
            if (closest != null) {
               this.ultimateTargetUUID = closest.getUUID();
               this.entityData.set(ULTIMATE_TARGET_UUID, this.ultimateTargetUUID.toString());
            } else {
               this.ultimateTargetUUID = null;
               this.entityData.set(ULTIMATE_TARGET_UUID, "");
            }
         }
      }
   }

   private Player getUltimateTarget() {
      if (this.ultimateTargetUUID == null) {
         return null;
      }
      if (this.level() instanceof ServerLevel server) {
         return server.getServer().getPlayerList().getPlayer(this.ultimateTargetUUID);
      }
      return null;
   }

   private void broadcastPosition(ServerLevel server) {
      long gameTime = server.getGameTime();
      WitherStormPositionPacket.HeadData[] heads = new WitherStormPositionPacket.HeadData[3];
      for (int i = 0; i < 3; ++i) {
         WitherStormHeadEntity h = this.hostHead(server, i);
         if (h == null) {
            heads[i] = WitherStormPositionPacket.HeadData.EMPTY;
         } else {
            long fireStart = h.getFireStartTime();
            int fireElapsed = fireStart >= 0L && gameTime - fireStart < 25L ? (int) (gameTime - fireStart) : -1;
            boolean beam = h.isBeamActive();
            Vec3 beamEnd = beam ? h.getBeamEndExact() : Vec3.ZERO;
            heads[i] = new WitherStormPositionPacket.HeadData(h.getLocalYaw(), h.getXRot(), fireElapsed, beam, beamEnd.x, beamEnd.y, beamEnd.z);
         }
      }
      WitherStormPositionPacket pkt = new WitherStormPositionPacket(
         this.getId(), this.getX(), this.getY(), this.getZ(),
         this.getYRot(), this.getXRot(), this.getBodyRoll(),
         (float) this.phase,
         this.elapsed(this.getPhase5AnimGameTime(), gameTime), this.elapsed(this.getPhase58AnimGameTime(), gameTime),
         this.activeHeadCount(), heads, this.isCollapsed(), (int) Math.max(-1, this.collapseTicks()),
         this.getSiegeStage(), this.siegeProgress(), new WitherStormPositionPacket.SeveredData[0]);
      for (ServerPlayer p : PlayerLookup.level(server)) {
         ServerPlayNetworking.send(p, pkt);
      }
   }

   private int elapsed(long stamp, long gameTime) {
      return stamp < 0L ? -1 : (int) Math.min(Integer.MAX_VALUE, Math.max(0L, gameTime - stamp));
   }

   public long getPhase5AnimGameTime() {
      return this.entityData.get(PHASE5_ANIM_GAME_TIME);
   }

   public long getPhase58AnimGameTime() {
      return this.entityData.get(PHASE5_ANIM_GAME_TIME);
   }

   public long getMiniHeadAnimGameTime() {
      return this.entityData.get(PHASE5_ANIM_GAME_TIME);
   }

   public long getFrontTentacleAnimGameTime() {
      return this.entityData.get(PHASE5_ANIM_GAME_TIME);
   }

   public float hatchProgress() {
      long stamp = this.entityData.get(SPAWN_ANIM_GAME_TIME);
      if (stamp < 0L) {
         return 1.0F;
      }
      float since = this.level().getGameTime() - stamp;
      float g = Mth.clamp(since / 55.0F, 0.0F, 1.0F);
      return g * g * (3.0F - 2.0F * g);
   }

   public long getSpawnAnimGameTime() {
      return this.entityData.get(SPAWN_ANIM_GAME_TIME);
   }

   public boolean isPlayingSpawnAnimation() {
      long start = this.getSpawnAnimGameTime();
      if (start < 0L) {
         return false;
      }
      long elapsed = this.level().getGameTime() - start;
      return elapsed >= 0L && elapsed < SPAWN_ANIM_LENGTH_TICKS;
   }

   public float getAnimationProgress() {
      long start = this.getSpawnAnimGameTime();
      if (start < 0L) {
         return 1.0F;
      }
      return Mth.clamp((this.level().getGameTime() - start) / 80.0F, 0.0F, 1.0F);
   }

   public void clientSyncPose(float bodyRoll, boolean phase4) {
      this.entityData.set(BODY_ROLL, bodyRoll);
      this.entityData.set(PHASE4_DATA, phase4);
      this.entityData.set(SPAWN_ANIM_GAME_TIME, -1L);
   }

   public void clientSyncPhase(float phase) {
      this.phase = phase;
      this.entityData.set(PHASE_DATA, phase);
   }

   public double behaviourPhase() {
      return this.isDevourer() ? 5.0 : this.phase;
   }

   public boolean isGrappleHittable(Vec3 point) {
      double h = this.getBbHeight();
      Vec3 body = this.position().add(0.0, h * 0.55, 0.0);
      double bodyR = this.phase >= 5.0 ? 8.0 : (this.phase >= 4.0 ? 6.0 : 2.5);
      return point.distanceTo(body) <= bodyR;
   }

   public boolean isDoomed(LivingEntity mob) {
      return false;
   }

   public void doomMob(LivingEntity mob) {
      // Fresh implementation: absorbed mobs are handled by the cluster/head systems.
   }

   public void carveAlong(float[] points) {
      // Terrain carving is handled by the tentacle/cluster systems; kept as a hook.
   }

   private BlockPos findSurfaceBlockNear(BlockPos around) {
      Level lvl = this.level();
      if (!(lvl instanceof ServerLevel server)) {
         return null;
      }
      for (int i = 0; i < 8; ++i) {
         int x = around.getX() + this.random.nextInt(25) - 12;
         int z = around.getZ() + this.random.nextInt(25) - 12;
         int y = server.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
         BlockPos pos = new BlockPos(x, y, z);
         BlockState state = server.getBlockState(pos);
         if (!state.isAir() && state.getFluidState().isEmpty()) {
            return pos;
         }
      }
      return null;
   }

   // ----------------------------------------------------------- damage / loot

   @Override
   protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
      if (!this.suppressLoot) {
         this.spawnAtLocation(level, new ItemStack(ModItems.WITHERED_NETHER_STAR));
      }
   }

   @Override
   public boolean isPushable() {
      return !this.isPhase4() && super.isPushable();
   }

   @Override
   public void push(double x, double y, double z) {
      if (!this.isPhase4()) {
         super.push(x, y, z);
      }
   }

   @Override
   public void push(Entity other) {
      if (!this.isPhase4()) {
         super.push(other);
      }
   }

   @Override
   protected EntityDimensions getDefaultDimensions(Pose pose) {
      return this.phase4 ? PHASE_4_DIMENSIONS : super.getDefaultDimensions(pose);
   }

   @Override
   protected AABB makeBoundingBox(Vec3 position) {
      if (this.phase4) {
         float half = 5.0F;
         float h = 30.0F;
         return new AABB(position.x - half, position.y, position.z - half, position.x + half, position.y + h, position.z + half);
      }
      return super.makeBoundingBox(position);
   }

   @Override
   public boolean canBeAffected(MobEffectInstance effect) {
      return this.isPhase4() && effect.getEffect() == MobEffects.WITHER ? false : super.canBeAffected(effect);
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      if (source.is(DamageTypeTags.IS_FALL)) {
         return false;
      }
      if (this.isPhase4() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return false;
      }
      return super.hurtServer(level, source, amount);
   }

   @Override
   public boolean isPickable() {
      return this.isPhase4() ? false : super.isPickable();
   }

   private void enterPhase4() {
      this.phase4 = true;
      this.setInvulnerable(true);
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(5000.0);
      this.setHealth(5000.0F);
      this.entityData.set(SPAWN_ANIM_GAME_TIME, this.level().getGameTime());
      this.entityData.set(PHASE5_ANIM_GAME_TIME, this.level().getGameTime());
      this.moveMode = MoveMode.CHASING;
      this.refreshDimensions();
      this.updateBossBar();
   }

   private void resetToNormalWither() {
      this.phase4 = false;
      this.setInvulnerable(false);
      this.entityData.set(BODY_ROLL, 0.0F);
      this.entityData.set(SPAWN_ANIM_GAME_TIME, -1L);
      this.moveMode = MoveMode.FOLLOW;
      this.refreshDimensions();
      this.updateBossBar();
   }

   private void updateBossBar() {
      // WitherBoss already owns a boss bar. Changing the overlay for phase 4+ requires
      // access to the protected event; the original used a mixin accessor for this.
      // The vanilla boss bar remains visible and functional, so we leave it as-is.
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return this.isCollapsed() ? null : super.getAmbientSound();
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource source) {
      return this.isCollapsed() ? null : super.getHurtSound(source);
   }

   // ------------------------------------------------------------- save/load

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putDouble("Phase", this.phase);
      output.putInt("SubGrowth", this.subGrowth);
      output.putBoolean("Phase4", this.phase4);
      if (this.ultimateTargetUUID != null) {
         output.putString("UltimateTarget", this.ultimateTargetUUID.toString());
      }
      output.putString("MoveMode", this.moveMode.name());
      output.putLong("SpawnAnimGameTime", this.entityData.get(SPAWN_ANIM_GAME_TIME));
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.loadingFromSave = true;
      this.phase = input.getDoubleOr("Phase", 0.0);
      this.subGrowth = input.getIntOr("SubGrowth", 0);
      this.phase4 = input.getBooleanOr("Phase4", this.phase >= 4.0);
      String uuid = input.getStringOr("UltimateTarget", "");
      this.ultimateTargetUUID = uuid.isEmpty() ? null : UUID.fromString(uuid);
      try {
         this.moveMode = MoveMode.valueOf(input.getStringOr("MoveMode", "FOLLOW"));
      } catch (IllegalArgumentException e) {
         this.moveMode = MoveMode.FOLLOW;
      }
      this.entityData.set(PHASE_DATA, (float) this.phase);
      this.entityData.set(PHASE4_DATA, this.phase4);
      this.entityData.set(SUBGROWTH_DATA, this.subGrowth);
      this.entityData.set(SPAWN_ANIM_GAME_TIME, input.getLongOr("SpawnAnimGameTime", -1L));
      this.updateBossBar();
      this.loadingFromSave = false;
   }

   public static enum MoveMode {
      FOLLOW,
      CHASING,
      DISTRACTED
   }

   static {
      PHASE_DATA = SynchedEntityData.defineId(WitherStormEntity.class, EntityDataSerializers.FLOAT);
      PHASE4_DATA = SynchedEntityData.defineId(WitherStormEntity.class, EntityDataSerializers.BOOLEAN);
      SUBGROWTH_DATA = SynchedEntityData.defineId(WitherStormEntity.class, EntityDataSerializers.INT);
      BODY_ROLL = SynchedEntityData.defineId(WitherStormEntity.class, EntityDataSerializers.FLOAT);
      SPAWN_ANIM_GAME_TIME = SynchedEntityData.defineId(WitherStormEntity.class, EntityDataSerializers.LONG);
      COLLAPSE_GAME_TIME = SynchedEntityData.defineId(WitherStormEntity.class, EntityDataSerializers.LONG);
      PHASE5_ANIM_GAME_TIME = SynchedEntityData.defineId(WitherStormEntity.class, EntityDataSerializers.LONG);
      ULTIMATE_TARGET_UUID = SynchedEntityData.defineId(WitherStormEntity.class, EntityDataSerializers.STRING);
   }
}
