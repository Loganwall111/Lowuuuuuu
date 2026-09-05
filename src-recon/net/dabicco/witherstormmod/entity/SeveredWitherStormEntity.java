package net.dabicco.witherstormmod.entity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import net.dabicco.witherstormmod.ModSounds;
import net.dabicco.witherstormmod.client.GroundProbe;
import net.dabicco.witherstormmod.config.WitherStormConfigs;
import net.dabicco.witherstormmod.config.WitherStormWorldConfig;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SeveredWitherStormEntity extends Mob implements net.dabicco.witherstormmod.entity.StormHeadHost {
   private static final EntityDataAccessor<Float> PHASE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.SeveredWitherStormEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Boolean> MIRRORED = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.SeveredWitherStormEntity.class, EntityDataSerializers.BOOLEAN
   );
   private static final EntityDataAccessor<Float> BODY_ROLL = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.SeveredWitherStormEntity.class, EntityDataSerializers.FLOAT
   );
   private static final EntityDataAccessor<Integer> HEADS = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.SeveredWitherStormEntity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Long> COLLAPSE = SynchedEntityData.defineId(
      net.dabicco.witherstormmod.entity.SeveredWitherStormEntity.class, EntityDataSerializers.LONG
   );
   private static final float BODY_WIDTH = 26.0F;
   private static final float BODY_HEIGHT = 24.0F;
   private static final double BODY_LIFT = 3.0;
   private static final EntityDimensions DIMENSIONS = EntityDimensions.scalable(26.0F, 24.0F);
   public static final double SIDE_OFFSET = 58.0;
   private static final double BEHIND_OFFSET = 20.0;
   private static final double MIN_HOST_DISTANCE = 50.0;
   private static final double FOLLOW_GAIN = 0.012;
   private static final double MAX_FOLLOW_SPEED = 0.9;
   private static final double DRIFT_LATERAL = 9.0;
   private static final double DRIFT_FORWARD = 7.0;
   private static final double DRIFT_VERTICAL = 11.0;
   private static final double DRIFT_LATERAL_PERIOD = 620.0;
   private static final double DRIFT_FORWARD_PERIOD = 830.0;
   private static final double DRIFT_VERTICAL_PERIOD = 470.0;
   private static final float DRIFT_YAW = 24.0F;
   private static final double DRIFT_YAW_PERIOD = 910.0;
   private static final double DRIFT_YAW_PERIOD_2 = 637.0;
   private static final double TARGET_LEAN = 14.0;
   private static final double TARGET_STANDOFF = 34.0;
   private static final double MIN_BEHIND = 6.0;
   private static final double MIN_LATERAL = 48.0;
   private static final float MAX_YAW_OFF_HOST = 55.0F;
   private static final int HEAD_COUNT = 3;
   private static final Vec3[] HEAD_OFFSETS = new Vec3[]{new Vec3(0.0, 13.5, 7.5), new Vec3(-5.5, 12.8, 4.5), new Vec3(5.5, 12.8, 4.5)};
   private static final float[] HEAD_REST_YAW = new float[]{0.0F, 20.0F, -20.0F};
   private static final float[] HEAD_REST_ROLL = new float[]{0.0F, -9.0F, 9.0F};
   private static final float[] HEAD_SCALES = new float[]{5.4F, 4.7F, 4.7F};
   private static final float[] HEAD_YAW_RANGE = new float[]{30.0F, 34.0F, 34.0F};
   private final UUID[] headUUIDs = new UUID[3];
   private UUID hostUUID;
   private int side = 1;
   private float bodyYawVel;
   public final GroundProbe groundProbe = new GroundProbe();
   private int cachedHostId = -1;
   private Vec3 launchVel = null;
   private static final float LONE_HEAD_YAW_RANGE = 62.0F;
   public static final double SEVERED_DROP_Y = 2.0;
   private final Set<ChunkPos> forcedByUs = new HashSet<>();
   private int lastChunkX = Integer.MIN_VALUE;
   private int lastChunkZ = Integer.MIN_VALUE;
   private static final int FORCE_RADIUS = 1;
   private static final int REASSERT_INTERVAL = 100;
   private static final double COLLAPSE_REST_HEIGHT = -1.5;
   private int scavengeCooldown = 0;
   private static final float YAW_SMOOTH_TIME = 1.15F;
   private static final float ROLL_PER_TURN = 9.0F;
   private static final float ROLL_MAX = 16.0F;
   private static final float ROLL_EASE = 0.035F;
   private static final float SWAY_AMOUNT = 3.5F;
   private static final double SWAY_PERIOD = 540.0;
   private static final float PITCH_MAX = 7.0F;
   private static final double PITCH_AT_SPEED = 0.45;
   private static final float PITCH_EASE = 0.03F;
   private float bodyRoll = 0.0F;
   private float bodyPitch = 0.0F;
   private static final double ORBIT_FOLLOW = 0.34;
   private static final double VERTICAL_FOLLOW = 0.18;
   private static final double SLACK = 14.0;
   private static final double GOAL_EASE = 0.02;
   private double smoothLateral = Double.NaN;
   private double smoothBehind = 0.0;
   private static final double CATCH_UP_MARGIN = 0.55;
   private static final double MAX_ERRAND_LATERAL = 96.0;
   private static final double MAX_ERRAND_BEHIND = 70.0;
   private static final double ERRAND_RANGE = 88.0;
   private LivingEntity errandTarget = null;
   private int errandRepickAt = 0;
   private double lastHostX;
   private double lastHostY;
   private double lastHostZ;
   private float lastHostYaw;
   private boolean hostFrameValid;
   private int headLoadGrace = 100;

   public void clientSyncSide(int s) {
      this.side = s < 0 ? -1 : 1;
      this.entityData.set(MIRRORED, this.side < 0);
   }

   @Override
   public float headYawOffsetFor(int index) {
      return HEAD_REST_YAW[Mth.clamp(index, 0, HEAD_REST_YAW.length - 1)];
   }

   @Override
   public float headRollOffsetFor(int index) {
      return HEAD_REST_ROLL[Mth.clamp(index, 0, HEAD_REST_ROLL.length - 1)];
   }

   public static Vec3[] previewHeadOffsets(boolean mirrored) {
      Vec3[] out = new Vec3[3];

      for (int i = 0; i < 3; i++) {
         Vec3 off = HEAD_OFFSETS[i];
         out[i] = mirrored ? new Vec3(-off.x, off.y, off.z) : off;
      }

      return out;
   }

   public static float[] previewHeadScales() {
      return (float[])HEAD_SCALES.clone();
   }

   public SeveredWitherStormEntity(EntityType<? extends Mob> type, Level level) {
      super(type, level);
      this.setNoGravity(true);
      this.noPhysics = true;
      this.setNoAi(true);
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(PHASE, 6.0F);
      builder.define(MIRRORED, false);
      builder.define(BODY_ROLL, 0.0F);
      builder.define(HEADS, 1);
      builder.define(COLLAPSE, -1L);
   }

   public void bindTo(net.dabicco.witherstormmod.entity.WitherStormEntity host, int side) {
      this.hostUUID = host.getUUID();
      this.side = side < 0 ? -1 : 1;
      this.entityData.set(MIRRORED, this.side < 0);
   }

   public int getSide() {
      return this.side;
   }

   public float hostCollapseTicks(float partialTick) {
      long start = (Long)(Object)this.entityData.get(COLLAPSE);
      return start < 0L ? -1.0F : (float)(this.level().getGameTime() - start) + partialTick;
   }

   public void clientSyncCollapse(long stamp) {
      this.entityData.set(COLLAPSE, stamp);
   }

   private Entity findHostOnClient() {
      if (this.cachedHostId >= 0) {
         if (this.level().getEntity(this.cachedHostId) instanceof net.dabicco.witherstormmod.entity.WitherStormEntity ws && this.hostUUID.equals(ws.getUUID())) {
            return ws;
         }

         this.cachedHostId = -1;
      }

      for (Entity e : this.level().getEntities(this, this.getBoundingBox().inflate(256.0))) {
         if (e instanceof net.dabicco.witherstormmod.entity.WitherStormEntity ws && this.hostUUID.equals(ws.getUUID())) {
            this.cachedHostId = ws.getId();
            return ws;
         }
      }

      return null;
   }

   public void launch(Vec3 velocity) {
      this.launchVel = velocity;
   }

   public void adoptHead(net.dabicco.witherstormmod.entity.WitherStormHeadEntity head) {
      this.headUUIDs[0] = head.getUUID();
   }

   public float getPhase() {
      return (Float)(Object)this.entityData.get(PHASE);
   }

   public boolean isMirrored() {
      return (Boolean)(Object)this.entityData.get(MIRRORED);
   }

   @Override
   public float getBodyRoll() {
      return (Float)(Object)this.entityData.get(BODY_ROLL);
   }

   public UUID getHostUUID() {
      return this.hostUUID;
   }

   public int activeHeadCount() {
      return (Integer)(Object)this.entityData.get(HEADS);
   }

   @Override
   public boolean isDevourerForm() {
      return true;
   }

   @Override
   public boolean headsDistressed() {
      return this.level() instanceof ServerLevel sl
         && sl.getEntity(this.getHostUUID()) instanceof net.dabicco.witherstormmod.entity.WitherStormEntity host
         && host.headsDistressed();
   }

   @Override
   public Vec3 headOffsetFor(int index) {
      Vec3 off = HEAD_OFFSETS[Mth.clamp(index, 0, 2)];
      return this.isMirrored() ? new Vec3(-off.x, off.y, off.z) : off;
   }

   @Override
   public float headScaleFor(int index) {
      return HEAD_SCALES[Mth.clamp(index, 0, 2)];
   }

   @Override
   public float headYawRangeFor(int index) {
      return index == 0 && this.getPhase() < 6.1 ? 62.0F : HEAD_YAW_RANGE[Mth.clamp(index, 0, 2)];
   }

   @Override
   public float attachYaw(float partialTick) {
      return net.dabicco.witherstormmod.entity.CollapseAnim.severedSpin(this.hostCollapseTicks(partialTick), this.side);
   }

   @Override
   public float attachPitch(float partialTick) {
      return net.dabicco.witherstormmod.entity.CollapseAnim.severedPitch(this.hostCollapseTicks(partialTick));
   }

   @Override
   public float attachRoll(float partialTick) {
      return net.dabicco.witherstormmod.entity.CollapseAnim.severedRoll(this.hostCollapseTicks(partialTick), this.side);
   }

   @Override
   public double attachPivotY() {
      return 12.0;
   }

   @Override
   public double attachDrop(float partialTick) {
      return 2.0 * net.dabicco.witherstormmod.entity.CollapseAnim.down(this.hostCollapseTicks(partialTick));
   }

   @Override
   public float headLitFor(int index) {
      if (this.level() instanceof ServerLevel server && this.hostUUID != null) {
         float var10000;
         if (server.getEntity(this.hostUUID) instanceof net.dabicco.witherstormmod.entity.WitherStormEntity host) {
            var10000 = net.dabicco.witherstormmod.entity.CollapseAnim.headLit(host.collapseTicks(), index);
         } else {
            var10000 = 1.0F;
         }

         return var10000;
      } else {
         return 1.0F;
      }
   }

   protected EntityDimensions getDefaultDimensions(Pose pose) {
      return DIMENSIONS;
   }

   protected AABB makeBoundingBox(Vec3 position) {
      float down = net.dabicco.witherstormmod.entity.CollapseAnim.down(this.hostCollapseTicks(0.0F));
      float w = Mth.lerp(down, 26.0F, 27.599998F);
      float h = Mth.lerp(down, 24.0F, 11.7F);
      double lift = 3.0 * (1.0 - down);
      float halfWidth = w * 0.5F;
      return new AABB(position.x - halfWidth, position.y + lift, position.z - halfWidth, position.x + halfWidth, position.y + lift + h, position.z + halfWidth);
   }

   protected void applyEffectsFromBlocks() {
   }

   protected void applyEffectsFromBlocksForLastMovements() {
   }

   protected boolean updateFluidInteraction() {
      return false;
   }

   public boolean isInWall() {
      return false;
   }

   protected void pushEntities() {
   }

   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel server) {
         net.dabicco.witherstormmod.entity.WitherStormEntity var10000;
         if (this.hostUUID == null) {
            var10000 = null;
         } else if (server.getEntity(this.hostUUID) instanceof net.dabicco.witherstormmod.entity.WitherStormEntity ws) {
            var10000 = ws;
         } else {
            var10000 = null;
         }

         if (var10000 != null && var10000.isUnderSiege()) {
            this.tickEndermanCatch(server);
         }

         if (var10000 != null && net.dabicco.witherstormmod.entity.CollapseAnim.isImpactTick(var10000.collapseTicks())) {
            server.playSound(
               (Entity)null,
               this.getX(),
               this.getY(),
               this.getZ(),
               ModSounds.STORM_THUMP,
               SoundSource.HOSTILE,
               4.0F,
               0.94F + server.getRandom().nextFloat() * 0.12F
            );
         }

         if (var10000 != null && var10000.isAlive() && var10000.isDevourer()) {
            this.tickScavenge(server, var10000);
            this.entityData.set(PHASE, (float)var10000.getPhase());
            this.entityData.set(HEADS, var10000.activeHeadCount());
            this.entityData.set(COLLAPSE, var10000.getCollapseGameTime());
            if (var10000.isCollapsed()) {
               this.tickCollapseFall(server, var10000);
            } else {
               this.launchVel = null;
               this.followHost(var10000);
            }

            this.updateHeads(server);
            this.updateChunkLoading(server);
         } else {
            this.discard();
         }
      }
   }

   private void updateChunkLoading(ServerLevel server) {
      int cx = this.chunkPosition().x();
      int cz = this.chunkPosition().z();
      boolean moved = cx != this.lastChunkX || cz != this.lastChunkZ;
      boolean reassert = this.tickCount % 100 == 0;
      if (moved || reassert) {
         this.lastChunkX = cx;
         this.lastChunkZ = cz;
         Set<ChunkPos> desired = new HashSet<>();

         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               ChunkPos pos = new ChunkPos(cx + x, cz + z);
               desired.add(pos);
               if (reassert || !this.forcedByUs.contains(pos)) {
                  net.dabicco.witherstormmod.entity.ChunkForceRegistry.acquire(server, this.getUUID(), pos);
                  this.forcedByUs.add(pos);
               }
            }
         }

         Iterator<ChunkPos> it = this.forcedByUs.iterator();

         while (it.hasNext()) {
            ChunkPos pos = it.next();
            if (!desired.contains(pos)) {
               net.dabicco.witherstormmod.entity.ChunkForceRegistry.release(server, this.getUUID(), pos);
               it.remove();
            }
         }
      }
   }

   private void releaseChunks() {
      if (this.level() instanceof ServerLevel server) {
         net.dabicco.witherstormmod.entity.ChunkForceRegistry.releaseAll(server, this.getUUID(), this.forcedByUs);
         this.forcedByUs.clear();
      }
   }

   private void tickCollapseFall(ServerLevel server, net.dabicco.witherstormmod.entity.WitherStormEntity host) {
      float t = host.collapseTicks();
      if (t >= 1000.0F) {
         this.launchVel = null;
         this.followHost(host);
      } else {
         this.hostFrameValid = false;
         double restY = this.groundBelow(server) + -1.5;
         if (this.launchVel != null) {
            Vec3 next = this.position().add(this.launchVel);
            if (next.y <= restY) {
               this.setPos(next.x, restY, next.z);
               this.launchVel = null;
            } else {
               this.setPos(next.x, next.y, next.z);
               this.launchVel = new Vec3(this.launchVel.x * 0.94, this.launchVel.y - 0.09, this.launchVel.z * 0.94);
            }
         } else {
            this.setPos(this.getX(), restY, this.getZ());
         }

         this.setDeltaMovement(Vec3.ZERO);
         this.yBodyRot = this.getYRot();
         this.yHeadRot = this.getYRot();
      }
   }

   private double groundBelow(ServerLevel server) {
      return server.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(this.getX()), Mth.floor(this.getZ()));
   }

   private void tickScavenge(ServerLevel server, net.dabicco.witherstormmod.entity.WitherStormEntity host) {
      WitherStormWorldConfig cfg = WitherStormConfigs.get(server);
      if (cfg.severedScavenge != 0 && --this.scavengeCooldown <= 0) {
         int interval = (int)Math.max(20.0, cfg.severedScavengeInterval * 20.0);
         this.scavengeCooldown = interval + this.random.nextInt(Math.max(1, interval / 2));
         host.spawnScavengedCluster(this);
      }
   }

   private void followHost(net.dabicco.witherstormmod.entity.WitherStormEntity host) {
      Vec3 wasAt = this.position();
      this.carryWithHost(host);
      float hostYaw = host.getYRot();
      double rad = Math.toRadians(hostYaw);
      double fx = -Math.sin(rad);
      double fz = Math.cos(rad);
      double rx = -fz;
      double t = this.tickCount + (this.side > 0 ? 0.0 : 613.0);
      double driftSide = Math.sin(t / 620.0 * Math.PI * 2.0) * 9.0;
      double driftFwd = Math.sin(t / 830.0 * Math.PI * 2.0) * 7.0;
      double driftUp = Math.sin(t / 470.0 * Math.PI * 2.0) * 11.0;
      double lateral = 58.0 + driftSide;
      double behind = 20.0 - driftFwd;
      LivingEntity errand = this.currentErrand(host);
      if (errand != null) {
         Vec3 toIt = errand.position().subtract(host.position());
         double outward = (toIt.x * rx + toIt.z * fx) * this.side;
         double back = -(toIt.x * fx + toIt.z * fz);
         lateral = Mth.clamp(outward, 48.0, 96.0);
         behind = Mth.clamp(back, 6.0, 70.0);
      }

      behind = Math.max(6.0, behind);
      lateral = Math.max(48.0, lateral);
      double radius = Math.sqrt(lateral * lateral + behind * behind);
      if (radius < 50.0) {
         double push = 50.0 / radius;
         lateral *= push;
         behind *= push;
      }

      if (Double.isNaN(this.smoothLateral)) {
         this.smoothLateral = lateral;
         this.smoothBehind = behind;
      }

      this.smoothLateral = this.smoothLateral + (lateral - this.smoothLateral) * 0.02;
      this.smoothBehind = this.smoothBehind + (behind - this.smoothBehind) * 0.02;
      Vec3 goal = host.position()
         .add(rx * this.smoothLateral * this.side, driftUp, fx * this.smoothLateral * this.side)
         .subtract(fx * this.smoothBehind, 0.0, fz * this.smoothBehind);
      Vec3 toGoal = goal.subtract(this.position());
      double gap = toGoal.length();
      double over = Mth.clamp((gap - 14.0) / 14.0, 0.0, 1.0);
      if (over > 0.0) {
         double ease = over * over * (3.0 - 2.0 * over);
         double hostSpeed = host.getDeltaMovement().length();
         double top = Math.max(0.9, hostSpeed + 0.55);
         Vec3 step = toGoal.scale(0.012 * ease);
         if (step.length() > top) {
            step = step.normalize().scale(top);
         }

         Vec3 next = this.position().add(step);
         this.setPos(next.x, next.y, next.z);
      }

      this.setDeltaMovement(Vec3.ZERO);
      float wanted = hostYaw;
      if (errand != null) {
         Vec3 look = errand.position().subtract(this.position());
         if (look.horizontalDistanceSqr() > 4.0) {
            wanted = (float)(Mth.atan2(look.z, look.x) * (180.0 / Math.PI)) - 90.0F;
         }
      }

      wanted += (float)(Math.sin(t / 910.0 * Math.PI * 2.0) * 24.0 + Math.sin(t / 637.0 * Math.PI * 2.0 + 1.7) * 24.0 * 0.55);
      wanted = hostYaw + Mth.clamp(Mth.degreesDifference(hostYaw, wanted), -55.0F, 55.0F);
      float before = this.getYRot();
      float[] box = new float[]{this.bodyYawVel};
      this.setYRot(this.smoothDampAngle(this.getYRot(), wanted, box, 1.15F));
      this.bodyYawVel = box[0];
      this.yBodyRot = this.getYRot();
      this.yHeadRot = this.getYRot();
      float turnRate = Mth.degreesDifference(before, this.getYRot());
      float wantRoll = Mth.clamp(-turnRate * 9.0F, -16.0F, 16.0F) + (float)Math.sin(t / 540.0 * Math.PI * 2.0) * 3.5F;
      this.bodyRoll = this.bodyRoll + (wantRoll - this.bodyRoll) * 0.035F;
      this.entityData.set(BODY_ROLL, this.bodyRoll);
      double travelled = this.position().distanceTo(wasAt);
      float wantPitch = (float)Mth.clamp(travelled / 0.45, 0.0, 1.0) * 7.0F;
      this.bodyPitch = this.bodyPitch + (wantPitch - this.bodyPitch) * 0.03F;
      this.setXRot(this.bodyPitch);
   }

   private LivingEntity currentErrand(net.dabicco.witherstormmod.entity.WitherStormEntity host) {
      if (!(this.level() instanceof ServerLevel server)) {
         return null;
      } else {
         for (UUID id : this.headUUIDs) {
            if (id != null && server.getEntity(id) instanceof net.dabicco.witherstormmod.entity.WitherStormHeadEntity head) {
               LivingEntity t = head.getHeadTarget();
               if (t != null && t.isAlive()) {
                  return t;
               }
            }
         }

         if (this.errandTarget != null
            && (
               !this.errandTarget.isAlive()
                  || this.errandTarget.isRemoved()
                  || this.errandTarget.level() != this.level()
                  || !this.onOwnSide(host, this.errandTarget)
                  || this.errandTarget.distanceToSqr(this) > 30976.0
            )) {
            this.errandTarget = null;
         }

         if (this.errandTarget == null && this.tickCount >= this.errandRepickAt) {
            this.errandRepickAt = this.tickCount + 60;
            LivingEntity best = null;
            double bestSq = Double.MAX_VALUE;

            for (LivingEntity e : server.getEntitiesOfClass(
               LivingEntity.class,
               this.getBoundingBox().inflate(88.0),
               c -> c != this
                  && c.isAlive()
                  && !c.isRemoved()
                  && !(c instanceof net.dabicco.witherstormmod.entity.WitherStormEntity)
                  && !(c instanceof net.dabicco.witherstormmod.entity.SeveredWitherStormEntity)
                  && !(c instanceof Player p && (p.isCreative() || p.isSpectator()))
            )) {
               if (this.onOwnSide(host, e)) {
                  double d = e.distanceToSqr(this);
                  if (d < bestSq) {
                     bestSq = d;
                     best = e;
                  }
               }
            }

            this.errandTarget = best;
         }

         return this.errandTarget;
      }
   }

   private boolean onOwnSide(net.dabicco.witherstormmod.entity.WitherStormEntity host, LivingEntity e) {
      double rad = Math.toRadians(host.getYRot());
      double fx = -Math.sin(rad);
      double fz = Math.cos(rad);
      double rx = -fz;
      double dx = e.getX() - host.getX();
      double dz = e.getZ() - host.getZ();
      return (dx * rx + dz * fx) * this.side > 0.0 && -(dx * fx + dz * fz) > -6.0;
   }

   private void carryWithHost(net.dabicco.witherstormmod.entity.WitherStormEntity host) {
      float hostYaw = host.getYRot();
      if (this.hostFrameValid) {
         Vec3 rel = this.position().subtract(new Vec3(this.lastHostX, this.lastHostY, this.lastHostZ));
         double d = Math.toRadians(Mth.degreesDifference(this.lastHostYaw, hostYaw) * 0.34);
         double cos = Math.cos(d);
         double sin = Math.sin(d);
         double rx = rel.x * cos - rel.z * sin;
         double rz = rel.x * sin + rel.z * cos;
         double dyHost = host.getY() - this.lastHostY;
         this.setPos(host.getX() + rx, this.getY() + dyHost * 0.18, host.getZ() + rz);
      }

      this.lastHostX = host.getX();
      this.lastHostY = host.getY();
      this.lastHostZ = host.getZ();
      this.lastHostYaw = hostYaw;
      this.hostFrameValid = true;
   }

   private LivingEntity currentAim(net.dabicco.witherstormmod.entity.WitherStormEntity host) {
      if (this.level() instanceof ServerLevel server) {
         for (UUID id : this.headUUIDs) {
            if (id != null && server.getEntity(id) instanceof net.dabicco.witherstormmod.entity.WitherStormHeadEntity head) {
               LivingEntity t = head.getHeadTarget();
               if (t != null && t.isAlive()) {
                  return t;
               }
            }
         }

         UUID ultimate = host.getUltimateTargetUUID();
         if (ultimate != null && server.getEntity(ultimate) instanceof LivingEntity t && t.isAlive()) {
            return t;
         }
      }

      return null;
   }

   private float smoothDampAngle(float current, float target, float[] velocity, float smoothTime) {
      float diff = Mth.degreesDifference(current, target);
      float omega = 2.0F / Math.max(1.0E-4F, smoothTime);
      float x = omega * 0.05F;
      float exp = 1.0F / (1.0F + x + 0.48F * x * x + 0.235F * x * x * x);
      float change = -diff;
      float temp = (velocity[0] + omega * change) * 0.05F;
      velocity[0] = (velocity[0] - omega * temp) * exp;
      return Mth.wrapDegrees(target + (change + temp) * exp);
   }

   private void updateHeads(ServerLevel server) {
      if (this.headLoadGrace > 0) {
         this.headLoadGrace--;
      }

      float bodyYaw = this.getYRot();
      double rad = Math.toRadians(bodyYaw);
      double cos = Math.cos(rad);
      double sin = Math.sin(rad);
      double rollRad = Math.toRadians(this.getBodyRoll());
      double cosR = Math.cos(rollRad);
      double sinR = Math.sin(rollRad);
      int active = this.activeHeadCount();

      for (int i = 0; i < 3; i++) {
         net.dabicco.witherstormmod.entity.WitherStormHeadEntity var10000;
         if (this.headUUIDs[i] != null && server.getEntity(this.headUUIDs[i]) instanceof net.dabicco.witherstormmod.entity.WitherStormHeadEntity h) {
            var10000 = h;
         } else {
            var10000 = null;
         }

         net.dabicco.witherstormmod.entity.WitherStormHeadEntity head = var10000;
         if (i >= active) {
            if (var10000 != null) {
               var10000.discard();
               this.headUUIDs[i] = null;
            }
         } else {
            if (var10000 == null || var10000.isRemoved()) {
               head = this.findExistingHead(server, i);
               if (head == null && this.headUUIDs[i] != null && this.headLoadGrace > 0) {
                  continue;
               }

               if (head == null) {
                  head = (net.dabicco.witherstormmod.entity.WitherStormHeadEntity)net.dabicco.witherstormmod.entity.ModEntityTypes.WITHER_STORM_HEAD
                     .create(server, EntitySpawnReason.EVENT);
                  if (head == null) {
                     continue;
                  }

                  head.setStormData(this.getUUID(), i);
                  head.setPos(this.getX(), this.getY() + HEAD_OFFSETS[i].y, this.getZ());
                  server.addFreshEntity(head);
               }

               this.headUUIDs[i] = head.getUUID();
            }

            Vec3 offset = this.headOffsetFor(i);
            double px = offset.x * cosR - offset.y * sinR;
            double py = offset.x * sinR + offset.y * cosR;
            head.setPos(this.getX() + (px * cos - offset.z * sin), this.getY() + py, this.getZ() + px * sin + offset.z * cos);
            head.setBaseYaw(bodyYaw + this.headYawOffsetFor(i));
         }
      }
   }

   public void tickEndermanCatch(ServerLevel server) {
      net.dabicco.witherstormmod.entity.WitherStormEntity.tickCaughtEndermen(server);
      net.dabicco.witherstormmod.entity.WitherStormEntity.catchEndermenInBeams(server, this, 3);
   }

   public void collectHeadAim(ServerLevel server, float[] yawOut, float[] pitchOut) {
      for (int i = 0; i < 3 && i < yawOut.length; i++) {
         if (this.headUUIDs[i] != null && server.getEntity(this.headUUIDs[i]) instanceof net.dabicco.witherstormmod.entity.WitherStormHeadEntity head) {
            yawOut[i] = head.getLocalYaw();
            pitchOut[i] = head.getXRot();
         }
      }
   }

   public void collectHeadState(ServerLevel server, int[] fireOut, boolean[] beamOut, double[] bxOut, double[] byOut, double[] bzOut) {
      long now = server.getGameTime();

      for (int i = 0; i < 3 && i < fireOut.length; i++) {
         fireOut[i] = -1;
         if (this.headUUIDs[i] != null && server.getEntity(this.headUUIDs[i]) instanceof net.dabicco.witherstormmod.entity.WitherStormHeadEntity head) {
            long fireStart = head.getFireStartTime();
            if (fireStart >= 0L && now - fireStart < 25L) {
               fireOut[i] = (int)(now - fireStart);
            }

            beamOut[i] = head.isBeamActive();
            if (beamOut[i]) {
               Vec3 end = head.getBeamEndExact();
               bxOut[i] = end.x;
               byOut[i] = end.y;
               bzOut[i] = end.z;
            }
         }
      }
   }

   @Override
   public net.dabicco.witherstormmod.entity.WitherStormHeadEntity hostHead(ServerLevel server, int index) {
      if (index >= 0 && index < 3 && this.headUUIDs[index] != null) {
         net.dabicco.witherstormmod.entity.WitherStormHeadEntity var10000;
         if (server.getEntity(this.headUUIDs[index]) instanceof net.dabicco.witherstormmod.entity.WitherStormHeadEntity head) {
            var10000 = head;
         } else {
            var10000 = null;
         }

         return var10000;
      } else {
         return null;
      }
   }

   private net.dabicco.witherstormmod.entity.WitherStormHeadEntity findExistingHead(ServerLevel server, int index) {
      for (net.dabicco.witherstormmod.entity.WitherStormHeadEntity head : server.getEntitiesOfClass(
         net.dabicco.witherstormmod.entity.WitherStormHeadEntity.class,
         this.getBoundingBox().inflate(96.0),
         h -> h.isAlive() && this.getUUID().equals(h.getStormUUID())
      )) {
         if (head.getHeadIndex() == index) {
            return head;
         }
      }

      return null;
   }

   public void remove(RemovalReason reason) {
      if (reason != RemovalReason.UNLOADED_TO_CHUNK) {
         this.releaseChunks();
      }

      if (this.level() instanceof ServerLevel server) {
         for (int i = 0; i < 3; i++) {
            if (this.headUUIDs[i] != null && server.getEntity(this.headUUIDs[i]) instanceof net.dabicco.witherstormmod.entity.WitherStormHeadEntity head) {
               head.discard();
            }

            this.headUUIDs[i] = null;
         }
      }

      super.remove(reason);
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 500.0).add(Attributes.FOLLOW_RANGE, 64.0);
   }

   public boolean removeWhenFarAway(double distanceSquared) {
      return false;
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   protected void doPush(Entity entity) {
   }

   public boolean isNoGravity() {
      return true;
   }

   public boolean shouldRenderAtSqrDistance(double dist) {
      return true;
   }

   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      if (this.hostUUID != null) {
         output.putString("HostUUID", this.hostUUID.toString());
      }

      output.putInt("Side", this.side);
      StringBuilder forced = new StringBuilder();

      for (ChunkPos pos : this.forcedByUs) {
         if (forced.length() > 0) {
            forced.append(",");
         }

         forced.append(pos.x()).append(":").append(pos.z());
      }

      output.putString("ForcedChunks", forced.toString());

      for (int i = 0; i < 3; i++) {
         if (this.headUUIDs[i] != null) {
            output.putString("Head" + i, this.headUUIDs[i].toString());
         }
      }
   }

   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      String host = input.getStringOr("HostUUID", "");

      try {
         this.hostUUID = host.isEmpty() ? null : UUID.fromString(host);
      } catch (IllegalArgumentException var12) {
         this.hostUUID = null;
      }

      this.side = input.getIntOr("Side", 1);
      this.entityData.set(MIRRORED, this.side < 0);
      this.forcedByUs.clear();
      String forced = input.getStringOr("ForcedChunks", "");
      if (!forced.isEmpty()) {
         for (String entry : forced.split(",")) {
            String[] xz = entry.split(":");
            if (xz.length == 2) {
               try {
                  this.forcedByUs.add(new ChunkPos(Integer.parseInt(xz[0]), Integer.parseInt(xz[1])));
               } catch (NumberFormatException var11) {
               }
            }
         }
      }

      for (int i = 0; i < 3; i++) {
         String id = input.getStringOr("Head" + i, "");

         try {
            this.headUUIDs[i] = id.isEmpty() ? null : UUID.fromString(id);
         } catch (IllegalArgumentException var10) {
            this.headUUIDs[i] = null;
         }
      }
   }
}
