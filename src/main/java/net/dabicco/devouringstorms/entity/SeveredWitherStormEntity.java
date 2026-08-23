package net.dabicco.devouringstorms.entity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import net.dabicco.devouringstorms.ModSounds;
import net.dabicco.devouringstorms.client.GroundProbe;
import net.dabicco.devouringstorms.config.WitherStormConfigs;
import net.dabicco.devouringstorms.config.WitherStormWorldConfig;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SeveredWitherStormEntity extends Mob implements StormHeadHost {
   private static final EntityDataAccessor<Float> PHASE;
   private static final EntityDataAccessor<Boolean> MIRRORED;
   private static final EntityDataAccessor<Float> BODY_ROLL;
   private static final EntityDataAccessor<Integer> HEADS;
   private static final EntityDataAccessor<Long> COLLAPSE;
   private static final float BODY_WIDTH = 26.0F;
   private static final float BODY_HEIGHT = 24.0F;
   private static final double BODY_LIFT = (double)3.0F;
   private static final EntityDimensions DIMENSIONS;
   public static final double SIDE_OFFSET = (double)58.0F;
   private static final double BEHIND_OFFSET = (double)20.0F;
   private static final double MIN_HOST_DISTANCE = (double)50.0F;
   private static final double FOLLOW_GAIN = 0.012;
   private static final double MAX_FOLLOW_SPEED = 0.9;
   private static final double DRIFT_LATERAL = (double)9.0F;
   private static final double DRIFT_FORWARD = (double)7.0F;
   private static final double DRIFT_VERTICAL = (double)11.0F;
   private static final double DRIFT_LATERAL_PERIOD = (double)620.0F;
   private static final double DRIFT_FORWARD_PERIOD = (double)830.0F;
   private static final double DRIFT_VERTICAL_PERIOD = (double)470.0F;
   private static final float DRIFT_YAW = 24.0F;
   private static final double DRIFT_YAW_PERIOD = (double)910.0F;
   private static final double DRIFT_YAW_PERIOD_2 = (double)637.0F;
   private static final double TARGET_LEAN = (double)14.0F;
   private static final double TARGET_STANDOFF = (double)34.0F;
   private static final double MIN_BEHIND = (double)6.0F;
   private static final double MIN_LATERAL = (double)48.0F;
   private static final float MAX_YAW_OFF_HOST = 55.0F;
   private static final int HEAD_COUNT = 3;
   private static final Vec3[] HEAD_OFFSETS;
   private static final float[] HEAD_REST_YAW;
   private static final float[] HEAD_REST_ROLL;
   private static final float[] HEAD_SCALES;
   private static final float[] HEAD_YAW_RANGE;
   private final UUID[] headUUIDs = new UUID[3];
   private UUID hostUUID;
   private int side = 1;
   private float bodyYawVel;
   public final GroundProbe groundProbe = new GroundProbe();
   private int cachedHostId = -1;
   private Vec3 launchVel = null;
   private static final float LONE_HEAD_YAW_RANGE = 62.0F;
   public static final double SEVERED_DROP_Y = (double)2.0F;
   private final Set<ChunkPos> forcedByUs = new HashSet();
   private int lastChunkX = Integer.MIN_VALUE;
   private int lastChunkZ = Integer.MIN_VALUE;
   private static final int FORCE_RADIUS = 1;
   private static final int REASSERT_INTERVAL = 100;
   private static final double COLLAPSE_REST_HEIGHT = (double)-1.5F;
   private int scavengeCooldown = 0;
   private static final float YAW_SMOOTH_TIME = 1.15F;
   private static final float ROLL_PER_TURN = 9.0F;
   private static final float ROLL_MAX = 16.0F;
   private static final float ROLL_EASE = 0.035F;
   private static final float SWAY_AMOUNT = 3.5F;
   private static final double SWAY_PERIOD = (double)540.0F;
   private static final float PITCH_MAX = 7.0F;
   private static final double PITCH_AT_SPEED = 0.45;
   private static final float PITCH_EASE = 0.03F;
   private float bodyRoll = 0.0F;
   private float bodyPitch = 0.0F;
   private static final double ORBIT_FOLLOW = 0.34;
   private static final double VERTICAL_FOLLOW = 0.18;
   private static final double SLACK = (double)14.0F;
   private static final double GOAL_EASE = 0.02;
   private double smoothLateral = Double.NaN;
   private double smoothBehind = (double)0.0F;
   private static final double CATCH_UP_MARGIN = 0.55;
   private static final double MAX_ERRAND_LATERAL = (double)96.0F;
   private static final double MAX_ERRAND_BEHIND = (double)70.0F;
   private static final double ERRAND_RANGE = (double)88.0F;
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

   public float headYawOffsetFor(int index) {
      return HEAD_REST_YAW[Mth.clamp(index, 0, HEAD_REST_YAW.length - 1)];
   }

   public float headRollOffsetFor(int index) {
      return HEAD_REST_ROLL[Mth.clamp(index, 0, HEAD_REST_ROLL.length - 1)];
   }

   public static Vec3[] previewHeadOffsets(boolean mirrored) {
      Vec3[] out = new Vec3[3];

      for(int i = 0; i < 3; ++i) {
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

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(PHASE, 6.0F);
      builder.define(MIRRORED, false);
      builder.define(BODY_ROLL, 0.0F);
      builder.define(HEADS, 1);
      builder.define(COLLAPSE, -1L);
   }

   public void bindTo(WitherStormEntity host, int side) {
      this.hostUUID = host.getUUID();
      this.side = side < 0 ? -1 : 1;
      this.entityData.set(MIRRORED, this.side < 0);
   }

   public int getSide() {
      return this.side;
   }

   public float hostCollapseTicks(float partialTick) {
      long start = (Long)this.entityData.get(COLLAPSE);
      return start < 0L ? -1.0F : (float)(this.level().getGameTime() - start) + partialTick;
   }

   public void clientSyncCollapse(long stamp) {
      this.entityData.set(COLLAPSE, stamp);
   }

   private Entity findHostOnClient() {
      if (this.cachedHostId >= 0) {
         Entity cached = this.level().getEntity(this.cachedHostId);
         if (cached instanceof WitherStormEntity) {
            WitherStormEntity ws = (WitherStormEntity)cached;
            if (this.hostUUID.equals(ws.getUUID())) {
               return ws;
            }
         }

         this.cachedHostId = -1;
      }

      for(Entity e : this.level().getEntities(this, this.getBoundingBox().inflate((double)256.0F))) {
         if (e instanceof WitherStormEntity ws) {
            if (this.hostUUID.equals(ws.getUUID())) {
               this.cachedHostId = ws.getId();
               return ws;
            }
         }
      }

      return null;
   }

   public void launch(Vec3 velocity) {
      this.launchVel = velocity;
   }

   public void adoptHead(WitherStormHeadEntity head) {
      this.headUUIDs[0] = head.getUUID();
   }

   public float getPhase() {
      return (Float)this.entityData.get(PHASE);
   }

   public boolean isMirrored() {
      return (Boolean)this.entityData.get(MIRRORED);
   }

   public float getBodyRoll() {
      return (Float)this.entityData.get(BODY_ROLL);
   }

   public UUID getHostUUID() {
      return this.hostUUID;
   }

   public int activeHeadCount() {
      return (Integer)this.entityData.get(HEADS);
   }

   public boolean isDevourerForm() {
      return true;
   }

   public boolean headsDistressed() {
      Level var3 = this.level();
      boolean var10000;
      if (var3 instanceof ServerLevel sl) {
         Entity var4 = sl.getEntity(this.getHostUUID());
         if (var4 instanceof WitherStormEntity host) {
            if (host.headsDistressed()) {
               var10000 = true;
               return var10000;
            }
         }
      }

      var10000 = false;
      return var10000;
   }

   public Vec3 headOffsetFor(int index) {
      Vec3 off = HEAD_OFFSETS[Mth.clamp(index, 0, 2)];
      return this.isMirrored() ? new Vec3(-off.x, off.y, off.z) : off;
   }

   public float headScaleFor(int index) {
      return HEAD_SCALES[Mth.clamp(index, 0, 2)];
   }

   public float headYawRangeFor(int index) {
      return index == 0 && (double)this.getPhase() < 6.1 ? 62.0F : HEAD_YAW_RANGE[Mth.clamp(index, 0, 2)];
   }

   public float attachYaw(float partialTick) {
      return CollapseAnim.severedSpin(this.hostCollapseTicks(partialTick), this.side);
   }

   public float attachPitch(float partialTick) {
      return CollapseAnim.severedPitch(this.hostCollapseTicks(partialTick));
   }

   public float attachRoll(float partialTick) {
      return CollapseAnim.severedRoll(this.hostCollapseTicks(partialTick), this.side);
   }

   public double attachPivotY() {
      return (double)12.0F;
   }

   public double attachDrop(float partialTick) {
      return (double)2.0F * (double)CollapseAnim.down(this.hostCollapseTicks(partialTick));
   }

   public float headLitFor(int index) {
      Level var3 = this.level();
      if (var3 instanceof ServerLevel server) {
         if (this.hostUUID != null) {
            Entity var4 = server.getEntity(this.hostUUID);
            float var10000;
            if (var4 instanceof WitherStormEntity) {
               WitherStormEntity host = (WitherStormEntity)var4;
               var10000 = CollapseAnim.headLit(host.collapseTicks(), index);
            } else {
               var10000 = 1.0F;
            }

            return var10000;
         }
      }

      return 1.0F;
   }

   protected EntityDimensions getDefaultDimensions(Pose pose) {
      return DIMENSIONS;
   }

   protected AABB makeBoundingBox(Vec3 position) {
      float down = CollapseAnim.down(this.hostCollapseTicks(0.0F));
      float w = Mth.lerp(down, 26.0F, 27.599998F);
      float h = Mth.lerp(down, 24.0F, 11.7F);
      double lift = (double)3.0F * ((double)1.0F - (double)down);
      float halfWidth = w * 0.5F;
      return new AABB(position.x - (double)halfWidth, position.y + lift, position.z - (double)halfWidth, position.x + (double)halfWidth, position.y + lift + (double)h, position.z + (double)halfWidth);
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
      Level var2 = this.level();
      if (var2 instanceof ServerLevel server) {
         WitherStormEntity var10000;
         if (this.hostUUID == null) {
            var10000 = null;
         } else {
            Entity var4 = server.getEntity(this.hostUUID);
            if (var4 instanceof WitherStormEntity) {
               WitherStormEntity ws = (WitherStormEntity)var4;
               var10000 = ws;
            } else {
               var10000 = null;
            }
         }

         WitherStormEntity host = var10000;
         if (host != null && host.isUnderSiege()) {
            this.tickEndermanCatch(server);
         }

         if (host != null && CollapseAnim.isImpactTick(host.collapseTicks())) {
            server.playSound((Entity)null, this.getX(), this.getY(), this.getZ(), ModSounds.STORM_THUMP, SoundSource.HOSTILE, 4.0F, 0.94F + server.getRandom().nextFloat() * 0.12F);
         }

         if (host != null && host.isAlive() && host.isDevourer()) {
            this.tickScavenge(server, host);
            this.entityData.set(PHASE, (float)host.getPhase());
            this.entityData.set(HEADS, host.activeHeadCount());
            this.entityData.set(COLLAPSE, host.getCollapseGameTime());
            if (host.isCollapsed()) {
               this.tickCollapseFall(server, host);
            } else {
               this.launchVel = null;
               this.followHost(host);
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
         Set<ChunkPos> desired = new HashSet();

         for(int x = -1; x <= 1; ++x) {
            for(int z = -1; z <= 1; ++z) {
               ChunkPos pos = new ChunkPos(cx + x, cz + z);
               desired.add(pos);
               if (reassert || !this.forcedByUs.contains(pos)) {
                  ChunkForceRegistry.acquire(server, this.getUUID(), pos);
                  this.forcedByUs.add(pos);
               }
            }
         }

         Iterator<ChunkPos> it = this.forcedByUs.iterator();

         while(it.hasNext()) {
            ChunkPos pos = (ChunkPos)it.next();
            if (!desired.contains(pos)) {
               ChunkForceRegistry.release(server, this.getUUID(), pos);
               it.remove();
            }
         }

      }
   }

   private void releaseChunks() {
      Level var2 = this.level();
      if (var2 instanceof ServerLevel server) {
         ChunkForceRegistry.releaseAll(server, this.getUUID(), this.forcedByUs);
         this.forcedByUs.clear();
      }
   }

   private void tickCollapseFall(ServerLevel server, WitherStormEntity host) {
      float t = host.collapseTicks();
      if (t >= 1000.0F) {
         this.launchVel = null;
         this.followHost(host);
      } else {
         this.hostFrameValid = false;
         double restY = this.groundBelow(server) + (double)-1.5F;
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
      return (double)server.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(this.getX()), Mth.floor(this.getZ()));
   }

   private void tickScavenge(ServerLevel server, WitherStormEntity host) {
      WitherStormWorldConfig cfg = WitherStormConfigs.get(server);
      if (cfg.severedScavenge != 0) {
         if (--this.scavengeCooldown <= 0) {
            int interval = (int)Math.max((double)20.0F, cfg.severedScavengeInterval * (double)20.0F);
            this.scavengeCooldown = interval + this.random.nextInt(Math.max(1, interval / 2));
            host.spawnScavengedCluster(this);
         }
      }
   }

   private void followHost(WitherStormEntity host) {
      Vec3 wasAt = this.position();
      this.carryWithHost(host);
      float hostYaw = host.getYRot();
      double rad = Math.toRadians((double)hostYaw);
      double fx = -Math.sin(rad);
      double fz = Math.cos(rad);
      double rx = -fz;
      double t = (double)this.tickCount + (this.side > 0 ? (double)0.0F : (double)613.0F);
      double driftSide = Math.sin(t / (double)620.0F * Math.PI * (double)2.0F) * (double)9.0F;
      double driftFwd = Math.sin(t / (double)830.0F * Math.PI * (double)2.0F) * (double)7.0F;
      double driftUp = Math.sin(t / (double)470.0F * Math.PI * (double)2.0F) * (double)11.0F;
      double lateral = (double)58.0F + driftSide;
      double behind = (double)20.0F - driftFwd;
      LivingEntity errand = this.currentErrand(host);
      if (errand != null) {
         Vec3 toIt = errand.position().subtract(host.position());
         double outward = (toIt.x * rx + toIt.z * fx) * (double)this.side;
         double back = -(toIt.x * fx + toIt.z * fz);
         lateral = Mth.clamp(outward, (double)48.0F, (double)96.0F);
         behind = Mth.clamp(back, (double)6.0F, (double)70.0F);
      }

      behind = Math.max((double)6.0F, behind);
      lateral = Math.max((double)48.0F, lateral);
      double radius = Math.sqrt(lateral * lateral + behind * behind);
      if (radius < (double)50.0F) {
         double push = (double)50.0F / radius;
         lateral *= push;
         behind *= push;
      }

      if (Double.isNaN(this.smoothLateral)) {
         this.smoothLateral = lateral;
         this.smoothBehind = behind;
      }

      this.smoothLateral += (lateral - this.smoothLateral) * 0.02;
      this.smoothBehind += (behind - this.smoothBehind) * 0.02;
      Vec3 goal = host.position().add(rx * this.smoothLateral * (double)this.side, driftUp, fx * this.smoothLateral * (double)this.side).subtract(fx * this.smoothBehind, (double)0.0F, fz * this.smoothBehind);
      Vec3 toGoal = goal.subtract(this.position());
      double gap = toGoal.length();
      double over = Mth.clamp((gap - (double)14.0F) / (double)14.0F, (double)0.0F, (double)1.0F);
      if (over > (double)0.0F) {
         double ease = over * over * ((double)3.0F - (double)2.0F * over);
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
         if (look.horizontalDistanceSqr() > (double)4.0F) {
            wanted = (float)(Mth.atan2(look.z, look.x) * (180D / Math.PI)) - 90.0F;
         }
      }

      wanted += (float)(Math.sin(t / (double)910.0F * Math.PI * (double)2.0F) * (double)24.0F + Math.sin(t / (double)637.0F * Math.PI * (double)2.0F + 1.7) * (double)24.0F * 0.55);
      wanted = hostYaw + Mth.clamp(Mth.degreesDifference(hostYaw, wanted), -55.0F, 55.0F);
      float before = this.getYRot();
      float[] box = new float[]{this.bodyYawVel};
      this.setYRot(this.smoothDampAngle(this.getYRot(), wanted, box, 1.15F));
      this.bodyYawVel = box[0];
      this.yBodyRot = this.getYRot();
      this.yHeadRot = this.getYRot();
      float turnRate = Mth.degreesDifference(before, this.getYRot());
      float wantRoll = Mth.clamp(-turnRate * 9.0F, -16.0F, 16.0F) + (float)Math.sin(t / (double)540.0F * Math.PI * (double)2.0F) * 3.5F;
      this.bodyRoll += (wantRoll - this.bodyRoll) * 0.035F;
      this.entityData.set(BODY_ROLL, this.bodyRoll);
      double travelled = this.position().distanceTo(wasAt);
      float wantPitch = (float)Mth.clamp(travelled / 0.45, (double)0.0F, (double)1.0F) * 7.0F;
      this.bodyPitch += (wantPitch - this.bodyPitch) * 0.03F;
      this.setXRot(this.bodyPitch);
   }

   private LivingEntity currentErrand(WitherStormEntity host) {
      Level var3 = this.level();
      if (!(var3 instanceof ServerLevel server)) {
         return null;
      } else {
         for(UUID id : this.headUUIDs) {
            if (id != null) {
               Entity var8 = server.getEntity(id);
               if (var8 instanceof WitherStormHeadEntity) {
                  WitherStormHeadEntity head = (WitherStormHeadEntity)var8;
                  LivingEntity t = head.getHeadTarget();
                  if (t != null && t.isAlive()) {
                     return t;
                  }
               }
            }
         }

         if (this.errandTarget != null && (!this.errandTarget.isAlive() || this.errandTarget.isRemoved() || this.errandTarget.level() != this.level() || !this.onOwnSide(host, this.errandTarget) || this.errandTarget.distanceToSqr(this) > (double)30976.0F)) {
            this.errandTarget = null;
         }

         if (this.errandTarget == null && this.tickCount >= this.errandRepickAt) {
            this.errandRepickAt = this.tickCount + 60;
            LivingEntity best = null;
            double bestSq = Double.MAX_VALUE;

            for(LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate((double)88.0F), (c) -> {
               boolean var10000;
               label33: {
                  if (c != this && c.isAlive() && !c.isRemoved() && !(c instanceof WitherStormEntity) && !(c instanceof SeveredWitherStormEntity)) {
                     if (!(c instanceof Player)) {
                        break label33;
                     }

                     Player p = (Player)c;
                     if (!p.isCreative() && !p.isSpectator()) {
                        break label33;
                     }
                  }

                  var10000 = false;
                  return var10000;
               }

               var10000 = true;
               return var10000;
            })) {
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

   private boolean onOwnSide(WitherStormEntity host, LivingEntity e) {
      double rad = Math.toRadians((double)host.getYRot());
      double fx = -Math.sin(rad);
      double fz = Math.cos(rad);
      double rx = -fz;
      double dx = e.getX() - host.getX();
      double dz = e.getZ() - host.getZ();
      return (dx * rx + dz * fx) * (double)this.side > (double)0.0F && -(dx * fx + dz * fz) > (double)-6.0F;
   }

   private void carryWithHost(WitherStormEntity host) {
      float hostYaw = host.getYRot();
      if (this.hostFrameValid) {
         Vec3 rel = this.position().subtract(new Vec3(this.lastHostX, this.lastHostY, this.lastHostZ));
         double d = Math.toRadians((double)Mth.degreesDifference(this.lastHostYaw, hostYaw) * 0.34);
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

   private LivingEntity currentAim(WitherStormEntity host) {
      Level var3 = this.level();
      if (var3 instanceof ServerLevel server) {
         for(UUID id : this.headUUIDs) {
            if (id != null) {
               Entity var8 = server.getEntity(id);
               if (var8 instanceof WitherStormHeadEntity) {
                  WitherStormHeadEntity head = (WitherStormHeadEntity)var8;
                  LivingEntity t = head.getHeadTarget();
                  if (t != null && t.isAlive()) {
                     return t;
                  }
               }
            }
         }

         UUID ultimate = host.getUltimateTargetUUID();
         if (ultimate != null) {
            Entity var12 = server.getEntity(ultimate);
            if (var12 instanceof LivingEntity) {
               LivingEntity t = (LivingEntity)var12;
               if (t.isAlive()) {
                  return t;
               }
            }
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
         --this.headLoadGrace;
      }

      float bodyYaw = this.getYRot();
      double rad = Math.toRadians((double)bodyYaw);
      double cos = Math.cos(rad);
      double sin = Math.sin(rad);
      double rollRad = Math.toRadians((double)this.getBodyRoll());
      double cosR = Math.cos(rollRad);
      double sinR = Math.sin(rollRad);
      int active = this.activeHeadCount();

      for(int i = 0; i < 3; ++i) {
         WitherStormHeadEntity var10000;
         label53: {
            if (this.headUUIDs[i] != null) {
               Entity var19 = server.getEntity(this.headUUIDs[i]);
               if (var19 instanceof WitherStormHeadEntity) {
                  WitherStormHeadEntity h = (WitherStormHeadEntity)var19;
                  var10000 = h;
                  break label53;
               }
            }

            var10000 = null;
         }

         WitherStormHeadEntity head = var10000;
         if (i >= active) {
            if (head != null) {
               head.discard();
               this.headUUIDs[i] = null;
            }
         } else {
            if (head == null || head.isRemoved()) {
               head = this.findExistingHead(server, i);
               if (head == null && this.headUUIDs[i] != null && this.headLoadGrace > 0) {
                  continue;
               }

               if (head == null) {
                  head = (WitherStormHeadEntity)ModEntityTypes.WITHER_STORM_HEAD.create(server, EntitySpawnReason.EVENT);
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
      WitherStormEntity.tickCaughtEndermen(server);
      WitherStormEntity.catchEndermenInBeams(server, this, 3);
   }

   public void collectHeadAim(ServerLevel server, float[] yawOut, float[] pitchOut) {
      for(int i = 0; i < 3 && i < yawOut.length; ++i) {
         if (this.headUUIDs[i] != null) {
            Entity var6 = server.getEntity(this.headUUIDs[i]);
            if (var6 instanceof WitherStormHeadEntity) {
               WitherStormHeadEntity head = (WitherStormHeadEntity)var6;
               yawOut[i] = head.getLocalYaw();
               pitchOut[i] = head.getXRot();
            }
         }
      }

   }

   public void collectHeadState(ServerLevel server, int[] fireOut, boolean[] beamOut, double[] bxOut, double[] byOut, double[] bzOut) {
      long now = server.getGameTime();

      for(int i = 0; i < 3 && i < fireOut.length; ++i) {
         fireOut[i] = -1;
         if (this.headUUIDs[i] != null) {
            Entity var11 = server.getEntity(this.headUUIDs[i]);
            if (var11 instanceof WitherStormHeadEntity) {
               WitherStormHeadEntity head = (WitherStormHeadEntity)var11;
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

   }

   public WitherStormHeadEntity hostHead(ServerLevel server, int index) {
      if (index >= 0 && index < 3 && this.headUUIDs[index] != null) {
         Entity var4 = server.getEntity(this.headUUIDs[index]);
         WitherStormHeadEntity var10000;
         if (var4 instanceof WitherStormHeadEntity) {
            WitherStormHeadEntity head = (WitherStormHeadEntity)var4;
            var10000 = head;
         } else {
            var10000 = null;
         }

         return var10000;
      } else {
         return null;
      }
   }

   private WitherStormHeadEntity findExistingHead(ServerLevel server, int index) {
      for(WitherStormHeadEntity head : server.getEntitiesOfClass(WitherStormHeadEntity.class, this.getBoundingBox().inflate((double)96.0F), (h) -> h.isAlive() && this.getUUID().equals(h.getStormUUID()))) {
         if (head.getHeadIndex() == index) {
            return head;
         }
      }

      return null;
   }

   public void remove(Entity.RemovalReason reason) {
      if (reason != RemovalReason.UNLOADED_TO_CHUNK) {
         this.releaseChunks();
      }

      Level var3 = this.level();
      if (var3 instanceof ServerLevel server) {
         for(int i = 0; i < 3; ++i) {
            if (this.headUUIDs[i] != null) {
               Entity var5 = server.getEntity(this.headUUIDs[i]);
               if (var5 instanceof WitherStormHeadEntity) {
                  WitherStormHeadEntity head = (WitherStormHeadEntity)var5;
                  head.discard();
               }
            }

            this.headUUIDs[i] = null;
         }
      }

      super.remove(reason);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double)500.0F).add(Attributes.FOLLOW_RANGE, (double)64.0F);
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

      for(ChunkPos pos : this.forcedByUs) {
         if (forced.length() > 0) {
            forced.append(",");
         }

         forced.append(pos.x()).append(":").append(pos.z());
      }

      output.putString("ForcedChunks", forced.toString());

      for(int i = 0; i < 3; ++i) {
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
         for(String entry : forced.split(",")) {
            String[] xz = entry.split(":");
            if (xz.length == 2) {
               try {
                  this.forcedByUs.add(new ChunkPos(Integer.parseInt(xz[0]), Integer.parseInt(xz[1])));
               } catch (NumberFormatException var11) {
               }
            }
         }
      }

      for(int i = 0; i < 3; ++i) {
         String id = input.getStringOr("Head" + i, "");

         try {
            this.headUUIDs[i] = id.isEmpty() ? null : UUID.fromString(id);
         } catch (IllegalArgumentException var10) {
            this.headUUIDs[i] = null;
         }
      }

   }

   static {
      PHASE = SynchedEntityData.defineId(SeveredWitherStormEntity.class, EntityDataSerializers.FLOAT);
      MIRRORED = SynchedEntityData.defineId(SeveredWitherStormEntity.class, EntityDataSerializers.BOOLEAN);
      BODY_ROLL = SynchedEntityData.defineId(SeveredWitherStormEntity.class, EntityDataSerializers.FLOAT);
      HEADS = SynchedEntityData.defineId(SeveredWitherStormEntity.class, EntityDataSerializers.INT);
      COLLAPSE = SynchedEntityData.defineId(SeveredWitherStormEntity.class, EntityDataSerializers.LONG);
      DIMENSIONS = EntityDimensions.scalable(26.0F, 24.0F);
      HEAD_OFFSETS = new Vec3[]{new Vec3((double)0.0F, (double)13.5F, (double)7.5F), new Vec3((double)-5.5F, 12.8, (double)4.5F), new Vec3((double)5.5F, 12.8, (double)4.5F)};
      HEAD_REST_YAW = new float[]{0.0F, 20.0F, -20.0F};
      HEAD_REST_ROLL = new float[]{0.0F, -9.0F, 9.0F};
      HEAD_SCALES = new float[]{5.4F, 4.7F, 4.7F};
      HEAD_YAW_RANGE = new float[]{30.0F, 34.0F, 34.0F};
   }
}
